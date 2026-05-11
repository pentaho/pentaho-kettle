/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.cli.auth.store;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.cli.auth.BrokerAuthClient;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;

/**
 * {@link TokenStore} that encrypts credentials using <b>AES-256-GCM</b> before
 * writing to disk.
 *
 * <h2>Encryption scheme</h2>
 * <ul>
 * <li><b>Cipher:</b> AES-256-GCM (authenticated encryption — detects
 * tampering)</li>
 * <li><b>Key derivation:</b> PBKDF2WithHmacSHA256, 310 000 iterations (OWASP
 * 2023
 * recommendation), 256-bit output</li>
 * <li><b>KDF input:</b> {@code user.name + "@" + hostname} — binds the key to
 * the OS user on the current machine without requiring any user interaction or
 * external tools</li>
 * <li><b>Salt:</b> fixed per-application constant — the PBKDF2 salt exists to
 * make offline dictionary attacks expensive; it does not need to be secret</li>
 * <li><b>IV:</b> 12-byte random, re-generated on every write,
 * prepended to the ciphertext and stored with it</li>
 * <li><b>File format:</b> single Base64 line in
 * {@code ~/.kettle/.kettle-sessions-enc}</li>
 * </ul>
 *
 * <h2>Security notes</h2>
 * <p>
 * The encryption key is derived from public data (username + hostname). A
 * motivated attacker who obtains the ciphertext file <em>and</em> knows these
 * values could derive the key. This is intentionally weaker than OS-level
 * keystores (which bind the key to the user's login credentials in
 * hardware-backed storage). It is, however, significantly stronger than the
 * plaintext {@link FileTokenStore} and is the best achievable without OS
 * integration or a user-supplied master password. File permissions (POSIX 600)
 * are applied after every write as an additional layer.
 *
 * @see TokenStoreFactory
 */
public class EncryptedFileTokenStore implements TokenStore {

  static final String ENC_FILE_NAME = ".kettle-sessions-enc";
  private static final LogChannelInterface log = new LogChannel( "EncryptedFileTokenStore" );

  private static String message( String key, String... tokens ) {
    return BaseMessages.getString( BrokerAuthClient.class, key, tokens );
  }

  /**
   * GCM IV length — 12 bytes is the NIST-recommended size.
   */
  private static final int GCM_IV_LEN = 12;

  /**
   * GCM authentication tag length in bits.
   */
  private static final int GCM_TAG_BITS = 128;

  /**
   * PBKDF2 iteration count — OWASP 2023 recommendation for PBKDF2-HMAC-SHA256.
   */
  private static final int PBKDF2_ITERATIONS = 310_000;

  /**
   * Derived key length in bits.
   */
  private static final int KEY_LEN_BITS = 256;
  private static final int KDF_SALT_LEN = 16;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  /**
   * Legacy fixed salt kept only so previously written credentials remain
   * readable. New writes use a per-file random salt stored alongside the IV.
   */
  private static final byte[] LEGACY_KDF_SALT = "pentaho-pan-enc-store-v1".getBytes( StandardCharsets.UTF_8 );

  private final File encFile;

  /**
   * Production constructor — uses the standard Kettle directory.
   */
  public EncryptedFileTokenStore() {
    this( new File( Const.getKettleDirectory(), ENC_FILE_NAME ) );
  }

  EncryptedFileTokenStore( File encFile ) {
    this.encFile = encFile;
  }

  /**
   * Derives a 256-bit AES key from the current OS user identity.
   *
   * <p>
   * The input material is {@code user.name + "@" + hostname}. This binds
   * the ciphertext to the user account on the machine; the file cannot be
   * decrypted by a different user or a different host without the same input.
   */
  private static SecretKey deriveKey( byte[] salt ) throws GeneralSecurityException {
    String identity = System.getProperty( "user.name", "unknown" ) + "@" + getHostname();
    char[] password = identity.toCharArray();

    SecretKeyFactory factory = SecretKeyFactory.getInstance( "PBKDF2WithHmacSHA256" );
    KeySpec spec = new PBEKeySpec( password, salt, PBKDF2_ITERATIONS, KEY_LEN_BITS );
    byte[] keyBytes = factory.generateSecret( spec ).getEncoded();
    return new SecretKeySpec( keyBytes, "AES" );
  }

  private static String getHostname() {
    // InetAddress.getLocalHost() can fail in some container environments
    // (no reverse DNS); fall back to env vars that are always set in Linux.
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch ( Exception ignored ) {
      // Docker / some CI systems set HOSTNAME as an env var
      String envHostname = System.getenv( "HOSTNAME" );
      return ( envHostname != null && !envHostname.isBlank() ) ? envHostname : "localhost";
    }
  }

  private static String serializeToBase64( StoredCredential credential ) {
    try {
      Properties props = CredentialSerializer.toProperties( credential );
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      props.store( baos, null );
      return Base64.getEncoder().encodeToString( baos.toByteArray() );
    } catch ( IOException e ) {
      log.logError( message( "EncryptedFileTokenStore.SerializationFailed", e.getMessage() ) );
      return null;
    }
  }

  private static Optional<StoredCredential> deserializeFromBase64( String base64 ) {
    try {
      byte[] bytes = Base64.getDecoder().decode( base64.strip() );
      Properties props = new Properties();
      props.load( new ByteArrayInputStream( bytes ) );
      return Optional.of( CredentialSerializer.fromProperties( props ) );
    } catch ( IOException | IllegalArgumentException e ) {
      log.logError( message( "EncryptedFileTokenStore.DeserializationFailed", e.getMessage() ) );
      return Optional.empty();
    }
  }

  @VisibleForTesting
  record EncryptedPayload(byte[] salt, byte[] iv, byte[] ciphertext) {
    @Override
    public boolean equals( Object other ) {
      if ( this == other ) {
        return true;
      }
      if ( !( other instanceof EncryptedPayload that ) ) {
        return false;
      }
      return Arrays.equals( salt, that.salt )
        && Arrays.equals( iv, that.iv )
        && Arrays.equals( ciphertext, that.ciphertext );
    }

    @Override
    public int hashCode() {
      int result = Arrays.hashCode( salt );
      result = 31 * result + Arrays.hashCode( iv );
      result = 31 * result + Arrays.hashCode( ciphertext );
      return result;
    }

    @Override
    public String toString() {
      return "EncryptedPayload{"
        + "salt=" + Arrays.toString( salt )
        + ", iv=" + Arrays.toString( iv )
        + ", ciphertext=" + Arrays.toString( ciphertext )
        + '}';
    }
  }

  /**
   * Always {@code true} — this implementation relies only on {@code javax.crypto}
   * which is bundled with every JRE.
   */
  @Override
  public boolean isAvailable() {
    return true;
  }

  @Override
  public void save( StoredCredential credential ) {
    String blob = serializeToBase64( credential );
    if ( blob == null ) {
      return;
    }
    try {
      byte[] salt = new byte[ KDF_SALT_LEN ];
      SECURE_RANDOM.nextBytes( salt );
      SecretKey key = deriveKey( salt );
      byte[] iv = new byte[ GCM_IV_LEN ];
      SECURE_RANDOM.nextBytes( iv );

      Cipher cipher = Cipher.getInstance( "AES/GCM/NoPadding" );
      cipher.init( Cipher.ENCRYPT_MODE, key, new GCMParameterSpec( GCM_TAG_BITS, iv ) );
      byte[] ciphertext = cipher.doFinal( blob.getBytes( StandardCharsets.UTF_8 ) );

      // Format: [4-byte salt-length][salt][4-byte iv-length][iv][ciphertext]
      ByteBuffer buf = ByteBuffer.allocate( 8 + salt.length + iv.length + ciphertext.length );
      buf.putInt( salt.length );
      buf.put( salt );
      buf.putInt( iv.length );
      buf.put( iv );
      buf.put( ciphertext );
      String encoded = Base64.getEncoder().encodeToString( buf.array() );

      CredentialFileSupport.ensureParentDirectoryExists( encFile );
      try ( FileOutputStream fos = new FileOutputStream( encFile ) ) {
        fos.write( encoded.getBytes( StandardCharsets.UTF_8 ) );
      }
      CredentialFileSupport.applyOwnerOnlyPermissions( encFile, log, "encrypted credential file" );

      if ( log.isDebug() ) {
        log.logDebug( message( "EncryptedFileTokenStore.Saved", encFile.getAbsolutePath() ) );
      }
    } catch ( GeneralSecurityException | IOException e ) {
      log.logError( message( "EncryptedFileTokenStore.SaveFailed", e.getMessage() ) );
    }
  }

  @Override
  public Optional<StoredCredential> load() {
    if ( !encFile.exists() ) {
      if ( log.isDebug() ) {
        log.logDebug( message( "EncryptedFileTokenStore.NoCredentialFile", encFile.getAbsolutePath() ) );
      }
      return Optional.empty();
    }
    try {
      byte[] raw = Files.readAllBytes( encFile.toPath() );
      byte[] decoded = Base64.getDecoder().decode( new String( raw, StandardCharsets.UTF_8 ).strip() );

      EncryptedPayload payload = readEncryptedPayload( decoded );
      SecretKey key = deriveKey( payload.salt() );
      Cipher cipher = Cipher.getInstance( "AES/GCM/NoPadding" );
      cipher.init( Cipher.DECRYPT_MODE, key, new GCMParameterSpec( GCM_TAG_BITS, payload.iv() ) );
      byte[] plaintext = cipher.doFinal( payload.ciphertext() );

      return deserializeFromBase64( new String( plaintext, StandardCharsets.UTF_8 ) );
    } catch ( javax.crypto.AEADBadTagException e ) {
      log.logError( message( "EncryptedFileTokenStore.DecryptionFailed" ) );
      delete();
      return Optional.empty();
    } catch ( GeneralSecurityException | IOException | IllegalArgumentException e ) {
      log.logError( message( "EncryptedFileTokenStore.LoadFailed", e.getMessage() ) );
      return Optional.empty();
    }
  }

  @Override
  public void delete() {
    CredentialFileSupport.deleteIfExists( encFile, log, "encrypted credential file" );
  }

  private EncryptedPayload readEncryptedPayload( byte[] decoded ) throws IOException {
    ByteBuffer buf = ByteBuffer.wrap( decoded );
    if ( buf.remaining() < Integer.BYTES + GCM_IV_LEN ) {
      throw new IOException( message( "EncryptedFileTokenStore.CorruptFile" ) );
    }

    int firstLen = buf.getInt();
    if ( firstLen == GCM_IV_LEN ) {
      return readLegacyPayload( buf, firstLen );
    }
    return readSaltedPayload( buf, firstLen );
  }

  private EncryptedPayload readLegacyPayload( ByteBuffer buf, int ivLen ) throws IOException {
    byte[] iv = new byte[ ivLen ];
    if ( buf.remaining() < ivLen ) {
      throw new IOException( message( "EncryptedFileTokenStore.CorruptLegacyFile" ) );
    }
    buf.get( iv );
    byte[] ciphertext = new byte[ buf.remaining() ];
    buf.get( ciphertext );
    return new EncryptedPayload( LEGACY_KDF_SALT, iv, ciphertext );
  }

  private EncryptedPayload readSaltedPayload( ByteBuffer buf, int saltLen ) throws IOException {
    if ( saltLen <= 0 || saltLen > 64 || buf.remaining() < saltLen + Integer.BYTES + GCM_IV_LEN ) {
      throw new IOException( message( "EncryptedFileTokenStore.CorruptFileSaltLength", String.valueOf( saltLen ) ) );
    }

    byte[] salt = new byte[ saltLen ];
    buf.get( salt );

    int ivLen = buf.getInt();
    if ( ivLen != GCM_IV_LEN || buf.remaining() < ivLen ) {
      throw new IOException( message( "EncryptedFileTokenStore.CorruptFileIvLength", String.valueOf( ivLen ) ) );
    }

    byte[] iv = new byte[ ivLen ];
    buf.get( iv );
    byte[] ciphertext = new byte[ buf.remaining() ];
    buf.get( ciphertext );
    return new EncryptedPayload( salt, iv, ciphertext );
  }
}
