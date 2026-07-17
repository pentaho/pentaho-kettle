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

import org.junit.Rule;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.logging.KettleLogStore;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class EncryptedFileTokenStoreTest {

  private static final int GCM_IV_LEN = 12;
  private static final int GCM_TAG_BITS = 128;
  private static final int PBKDF2_ITERATIONS = 310_000;
  private static final int KEY_LEN_BITS = 256;
  private static final byte[] LEGACY_KDF_SALT = "pentaho-pan-enc-store-v1".getBytes( StandardCharsets.UTF_8 );

  @Rule
  public TemporaryFolder tempDir = new TemporaryFolder();

  @BeforeClass
  public static void initLogStore() {
    KettleLogStore.init();
  }

  @Test
  public void saveAndLoadRoundTripCredential() {
    File encFile = tempDir.getRoot().toPath().resolve( ".kettle-sessions-enc" ).toFile();
    EncryptedFileTokenStore store = new EncryptedFileTokenStore( encFile );
    StoredCredential credential = createCredential();

    store.save( credential );

    assertTrue( encFile.exists() );
    assertTrue( store.isAvailable() );
    assertEquals( Optional.of( credential ), store.load() );
  }

  @Test
  public void loadReturnsEmptyWhenFileMissingAndDeleteRemovesExistingFile() throws IOException {
    File encFile = tempDir.getRoot().toPath().resolve( ".kettle-sessions-enc" ).toFile();
    EncryptedFileTokenStore store = new EncryptedFileTokenStore( encFile );

    assertTrue( store.load().isEmpty() );

    Files.writeString( encFile.toPath(), "data" );
    store.delete();

    assertFalse( encFile.exists() );
  }

  @Test
  public void loadDeletesFileWhenAuthenticationTagIsInvalid() throws Exception {
    File encFile = tempDir.getRoot().toPath().resolve( ".kettle-sessions-enc" ).toFile();
    EncryptedFileTokenStore store = new EncryptedFileTokenStore( encFile );
    store.save( createCredential() );

    byte[] decoded = Base64.getDecoder().decode( Files.readString( encFile.toPath(), StandardCharsets.UTF_8 ).strip() );
    decoded[ decoded.length - 1 ] ^= 0x01;
    Files.writeString( encFile.toPath(), Base64.getEncoder().encodeToString( decoded ), StandardCharsets.UTF_8 );

    assertTrue( store.load().isEmpty() );
    assertFalse( encFile.exists() );
  }

  @Test
  public void loadSupportsLegacyPayloadFormat() throws Exception {
    File encFile = tempDir.getRoot().toPath().resolve( ".kettle-sessions-enc" ).toFile();
    EncryptedFileTokenStore store = new EncryptedFileTokenStore( encFile );
    StoredCredential credential = createCredential();

    Files.writeString( encFile.toPath(), createLegacyPayload( credential ), StandardCharsets.UTF_8 );

    assertEquals( Optional.of( credential ), store.load() );
  }

  @Test
  public void loadReturnsEmptyForCorruptSaltedPayloadOrInvalidDecryptedContent() throws Exception {
    File corruptFile = tempDir.getRoot().toPath().resolve( "corrupt.enc" ).toFile();
    EncryptedFileTokenStore corruptStore = new EncryptedFileTokenStore( corruptFile );
    byte[] invalidSaltLengthPayload = ByteBuffer.allocate( Integer.BYTES + 4 )
      .putInt( 65 )
      .putInt( 0 )
      .array();
    Files.writeString( corruptFile.toPath(), Base64.getEncoder().encodeToString( invalidSaltLengthPayload ),
      StandardCharsets.UTF_8 );

    assertTrue( corruptStore.load().isEmpty() );

    File invalidPlaintextFile = tempDir.getRoot().toPath().resolve( "invalid-plaintext.enc" ).toFile();
    EncryptedFileTokenStore invalidPlaintextStore = new EncryptedFileTokenStore( invalidPlaintextFile );
    Files.writeString( invalidPlaintextFile.toPath(), createSaltedPayload( "%%%not-base64%%%" ),
      StandardCharsets.UTF_8 );

    assertTrue( invalidPlaintextStore.load().isEmpty() );
  }

  @Test
  public void loadReturnsEmptyForNonBase64ContentCorruptLegacyPayloadAndInvalidIvLength() throws Exception {
    File invalidBase64File = tempDir.getRoot().toPath().resolve( "invalid-base64.enc" ).toFile();
    EncryptedFileTokenStore invalidBase64Store = new EncryptedFileTokenStore( invalidBase64File );
    Files.writeString( invalidBase64File.toPath(), "%%%", StandardCharsets.UTF_8 );

    assertTrue( invalidBase64Store.load().isEmpty() );

    File corruptLegacyFile = tempDir.getRoot().toPath().resolve( "corrupt-legacy.enc" ).toFile();
    EncryptedFileTokenStore corruptLegacyStore = new EncryptedFileTokenStore( corruptLegacyFile );
    byte[] invalidLegacyPayload = ByteBuffer.allocate( Integer.BYTES + 4 )
      .putInt( GCM_IV_LEN )
      .putInt( 7 )
      .array();
    Files.writeString( corruptLegacyFile.toPath(), Base64.getEncoder().encodeToString( invalidLegacyPayload ),
      StandardCharsets.UTF_8 );

    assertTrue( corruptLegacyStore.load().isEmpty() );

    File invalidIvFile = tempDir.getRoot().toPath().resolve( "invalid-iv.enc" ).toFile();
    EncryptedFileTokenStore invalidIvStore = new EncryptedFileTokenStore( invalidIvFile );
    byte[] salt = randomBytes( 16 );
    ByteBuffer invalidIvPayload = ByteBuffer.allocate( Integer.BYTES + salt.length + Integer.BYTES + 11 + 1 );
    invalidIvPayload.putInt( salt.length );
    invalidIvPayload.put( salt );
    invalidIvPayload.putInt( 11 );
    invalidIvPayload.put( randomBytes( 11 ) );
    invalidIvPayload.put( (byte) 1 );
    Files.writeString( invalidIvFile.toPath(), Base64.getEncoder().encodeToString( invalidIvPayload.array() ),
      StandardCharsets.UTF_8 );

    assertTrue( invalidIvStore.load().isEmpty() );
  }

  @Test
  public void saveSwallowsIoExceptionWhenTargetPathIsADirectory() {
    File encDir = tempDir.getRoot().toPath().resolve( "enc-dir" ).toFile();
    assertTrue( encDir.mkdir() );
    EncryptedFileTokenStore store = new EncryptedFileTokenStore( encDir );

    store.save( createCredential() );

    assertTrue( encDir.isDirectory() );
  }

  @Test
  public void encryptedPayloadUsesArrayContentForEqualityHashCodeAndToString() {
    EncryptedFileTokenStore.EncryptedPayload left = new EncryptedFileTokenStore.EncryptedPayload(
      new byte[] { 1, 2 }, new byte[] { 3, 4 }, new byte[] { 5, 6 } );
    EncryptedFileTokenStore.EncryptedPayload same = new EncryptedFileTokenStore.EncryptedPayload(
      new byte[] { 1, 2 }, new byte[] { 3, 4 }, new byte[] { 5, 6 } );
    EncryptedFileTokenStore.EncryptedPayload different = new EncryptedFileTokenStore.EncryptedPayload(
      new byte[] { 1 }, new byte[] { 3, 4 }, new byte[] { 5, 6 } );

    assertEquals( left, left );
    assertEquals( left, same );
    assertEquals( left.hashCode(), same.hashCode() );
    assertNotEquals( left, different );
    assertTrue( left.toString().contains( "salt=" ) );
  }

  private StoredCredential createCredential() {
    return StoredCredential.builder()
      .serverUrl( "http://localhost:8080/pentaho" )
      .sessionToken( "JSESSIONID" )
      .sessionCookie( "JSESSIONID=abc" )
      .username( "alice" )
      .sessionExpiry( 1234L )
      .oauthAccessToken( "access-token" )
      .oauthRefreshToken( "refresh-token" )
      .oauthTokenType( "Bearer" )
      .oauthIdpRegistrationId( "azure" )
      .oauthTokenExpiry( 5678L )
      .oauthRefreshHandle( "refresh-handle" )
      .oauthBrokerAuthHandle( "broker-auth-handle" )
      .build();
  }

  private String createLegacyPayload( StoredCredential credential ) throws Exception {
    String base64Credential = serializeCredential( credential );
    byte[] iv = randomBytes( GCM_IV_LEN );
    SecretKey key = deriveKey( LEGACY_KDF_SALT );

    Cipher cipher = Cipher.getInstance( "AES/GCM/NoPadding" );
    cipher.init( Cipher.ENCRYPT_MODE, key, new GCMParameterSpec( GCM_TAG_BITS, iv ) );
    byte[] ciphertext = cipher.doFinal( base64Credential.getBytes( StandardCharsets.UTF_8 ) );

    ByteBuffer buffer = ByteBuffer.allocate( Integer.BYTES + iv.length + ciphertext.length );
    buffer.putInt( iv.length );
    buffer.put( iv );
    buffer.put( ciphertext );
    return Base64.getEncoder().encodeToString( buffer.array() );
  }

  private String createSaltedPayload( String plaintext ) throws Exception {
    byte[] salt = randomBytes( 16 );
    byte[] iv = randomBytes( GCM_IV_LEN );
    SecretKey key = deriveKey( salt );

    Cipher cipher = Cipher.getInstance( "AES/GCM/NoPadding" );
    cipher.init( Cipher.ENCRYPT_MODE, key, new GCMParameterSpec( GCM_TAG_BITS, iv ) );
    byte[] ciphertext = cipher.doFinal( plaintext.getBytes( StandardCharsets.UTF_8 ) );

    ByteBuffer buffer = ByteBuffer.allocate( Integer.BYTES + salt.length + Integer.BYTES + iv.length
      + ciphertext.length );
    buffer.putInt( salt.length );
    buffer.put( salt );
    buffer.putInt( iv.length );
    buffer.put( iv );
    buffer.put( ciphertext );
    return Base64.getEncoder().encodeToString( buffer.array() );
  }

  private String serializeCredential( StoredCredential credential ) throws IOException {
    Properties properties = CredentialSerializer.toProperties( credential );
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    properties.store( outputStream, null );
    return Base64.getEncoder().encodeToString( outputStream.toByteArray() );
  }

  private SecretKey deriveKey( byte[] salt ) throws Exception {
    String identity = System.getProperty( "user.name", "unknown" ) + "@" + getHostname();
    SecretKeyFactory factory = SecretKeyFactory.getInstance( "PBKDF2WithHmacSHA256" );
    KeySpec spec = new PBEKeySpec( identity.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LEN_BITS );
    byte[] keyBytes = factory.generateSecret( spec ).getEncoded();
    return new SecretKeySpec( keyBytes, "AES" );
  }

  private String getHostname() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch ( Exception ignored ) {
      String envHostname = System.getenv( "HOSTNAME" );
      return ( envHostname != null && !envHostname.isBlank() ) ? envHostname : "localhost";
    }
  }

  private byte[] randomBytes( int length ) {
    byte[] bytes = new byte[ length ];
    new SecureRandom().nextBytes( bytes );
    return bytes;
  }
}
