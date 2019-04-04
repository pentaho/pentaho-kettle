/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Symmetric algorithm
 *
 * @author Samatar
 * @since 01-4-2011
 */
public class SymmetricCrypto {

  private static Class<?> PKG = SymmetricCrypto.class; // for i18n purposes, needed by Translator2!!

  private SymmetricCryptoMeta meta;

  private Cipher cipher;

  private SecretKeySpec secretKeySpec;

  /** Encryption/ decryption scheme **/
  private String scheme;

  /**
   * Construct a new Symetric SymmetricCrypto trans
   *
   * @param inf
   *          The Database Connection Info to construct the connection with.
   */
  public SymmetricCrypto( SymmetricCryptoMeta meta ) throws CryptoException {
    this.meta = meta;
    this.scheme = meta.getDefaultScheme();
    try {
      if ( this.scheme == null ) {
        throw new CryptoException( BaseMessages.getString( PKG, "SymmetricCrypto.SchemeMissing" ) );
      }
      this.cipher = Cipher.getInstance( this.scheme );
    } catch ( Exception e ) {
      throw new CryptoException( e );
    }
  }

  /**
   * Construct a new Symetric SymmetricCrypto trans
   *
   * @param inf
   *          The Database Connection Info to construct the connection with.
   */
  public SymmetricCrypto( SymmetricCryptoMeta meta, String xform ) throws CryptoException {
    this.meta = meta;
    this.scheme = Const.NVL( xform, meta.getDefaultScheme() );
    try {
      if ( this.scheme == null ) {
        throw new CryptoException( BaseMessages.getString( PKG, "SymmetricCrypto.SchemeMissing" ) );
      }
      this.cipher = Cipher.getInstance( this.scheme );
    } catch ( Exception e ) {
      throw new CryptoException( BaseMessages.getString( PKG, "SymmetricCrypto.SymmetricCrypto.Error.Cipher", e ) );
    }
  }

  public void setEncryptMode() throws CryptoException {
    try {
      this.cipher.init( Cipher.ENCRYPT_MODE, this.secretKeySpec );
    } catch ( Exception e ) {
      throw new CryptoException( e );
    }
  }

  public void setDecryptMode() throws CryptoException {
    try {
      this.cipher.init( Cipher.DECRYPT_MODE, this.secretKeySpec );
    } catch ( Exception e ) {
      throw new CryptoException( e );
    }
  }

  public void setSecretKey( String keyString ) throws CryptoKeyException {
    try {
      setSecretKey( Hex.decodeHex( keyString.toCharArray() ) );
    } catch ( Exception e ) {
      throw new CryptoKeyException( e );
    }
  }

  public void setSecretKey( byte[] keyBytes ) throws CryptoKeyException {
    try {
      // Convert the raw bytes to a secret key like this
      this.secretKeySpec = new SecretKeySpec( keyBytes, meta.getAlgorithm() );
    } catch ( Exception e ) {
      throw new CryptoKeyException( e );
    }
  }

  public void setSecretKeyFromFile( String filename ) throws CryptoKeyException {
    FileObject file = null;
    try {
      file = KettleVFS.getFileObject( filename );
      if ( !file.exists() ) {
        throw new CryptoException( BaseMessages.getString( PKG, "SymmetricCrypto.CanNotFindFile", file.getName() ) );
      }
      byte[] KeyBytes = new byte[(int) file.getContent().getSize()];

      setSecretKey( KeyBytes );

    } catch ( Exception e ) {
      throw new CryptoKeyException( e );
    } finally {
      if ( file != null ) {
        try {
          file.close();
        } catch ( Exception e ) { /* Ignore */
        }
      }
    }
  }

  public byte[] encrDecryptData( byte[] inpBytes ) throws CryptoException {
    try {
      return this.cipher.doFinal( inpBytes );
    } catch ( Exception e ) {
      throw new CryptoException( e );
    }
  }

  public byte[] generateKey( int keySize ) throws CryptoKeyException {
    try {
      // Get a key generator for algorithm
      KeyGenerator kg = KeyGenerator.getInstance( meta.getAlgorithm() );
      // SecureRandom random = new SecureRandom();
      kg.init( keySize );
      // Use it to generate a key
      SecretKey secretKey = kg.generateKey();

      return secretKey.getEncoded();

    } catch ( Exception e ) {
      throw new CryptoKeyException( e );
    }
  }

  public String generateKeyAsHex( int keySize ) throws CryptoKeyException {
    return new String( Hex.encodeHex( generateKey( keySize ) ) );
  }

  public String getCipherProviderName() {
    return this.cipher.getProvider().getName();
  }

  public void close() {
    this.cipher = null;
    this.secretKeySpec = null;
  }

}
