/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.encryption;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;

public class CertificateGenEncryptUtil {

  public static final int KEY_SIZE = 1024;
  public static final String PUBLIC_KEY_ALGORITHM = "RSA";
  public static final String SINGLE_KEY_ALGORITHM = "AES";
  public static final String TRANSMISSION_CIPHER_PARAMS = "RSA/ECB/PKCS1Padding";
  private static final LoggingObjectInterface loggingObject = new SimpleLoggingObject(
      "Certificate Encryption Utility", LoggingObjectType.GENERAL, null );
  private static final LogChannel log = new LogChannel( loggingObject );

  public static KeyPair generateKeyPair() {
    KeyPair pair = null;
    try {
      KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance( PUBLIC_KEY_ALGORITHM );
      keyPairGen.initialize( KEY_SIZE );
      pair = keyPairGen.generateKeyPair();
    } catch ( Exception ex ) {
      log.logError( ex.getLocalizedMessage(), ex );
    }
    return pair;
  }

  public static Key generateSingleKey() throws NoSuchAlgorithmException {
    Key key = KeyGenerator.getInstance( SINGLE_KEY_ALGORITHM ).generateKey();
    return key;
  }

  public static byte[] encodeKeyForTransmission( Key encodingKey, Key keyToEncode ) throws NoSuchAlgorithmException,
    NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
    Cipher cipher = Cipher.getInstance( TRANSMISSION_CIPHER_PARAMS );
    cipher.init( Cipher.WRAP_MODE, encodingKey );
    byte[] encodedKey = cipher.wrap( keyToEncode );
    return encodedKey;
  }

  public static Key decodeTransmittedKey( byte[] sessionKey, byte[] transmittedKey, boolean privateKey )
      throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
    KeySpec keySpec = null;
    Key keyKey = null;
    if ( transmittedKey == null || sessionKey == null ) {
      return null;
    }
    if ( !privateKey ) {
      keySpec = new X509EncodedKeySpec( sessionKey );
      keyKey = KeyFactory.getInstance( PUBLIC_KEY_ALGORITHM ).generatePublic( keySpec );
    } else {
      keySpec = new PKCS8EncodedKeySpec( sessionKey );
      keyKey = KeyFactory.getInstance( PUBLIC_KEY_ALGORITHM ).generatePrivate( keySpec );
    }
    Cipher keyCipher = Cipher.getInstance( TRANSMISSION_CIPHER_PARAMS );
    keyCipher.init( Cipher.UNWRAP_MODE, keyKey );
    return keyCipher.unwrap( transmittedKey, SINGLE_KEY_ALGORITHM, Cipher.SECRET_KEY );
  }

  public static Cipher initDecryptionCipher( Key unwrappedKey, byte[] unencryptedKey )
      throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
    Cipher decryptionCip = Cipher.getInstance( SINGLE_KEY_ALGORITHM );
    if ( unwrappedKey != null ) {
      decryptionCip.init( Cipher.ENCRYPT_MODE, unwrappedKey );
    } else {
      SecretKeySpec sks = new SecretKeySpec( unencryptedKey, SINGLE_KEY_ALGORITHM );
      decryptionCip.init( Cipher.ENCRYPT_MODE, sks );
    }
    return decryptionCip;
  }

  public static byte[] encryptUsingKey( byte[] data, Key key ) {
    byte[] result = null;
    try {
      Cipher cipher = Cipher.getInstance( PUBLIC_KEY_ALGORITHM );
      cipher.init( Cipher.ENCRYPT_MODE, key );
      result = cipher.doFinal( data );
    } catch ( Exception ex ) {
      log.logError( ex.getLocalizedMessage(), ex );
    }
    return result;
  }

  public static byte[] decryptUsingKey( byte[] data, Key key ) {
    byte[] result = null;
    try {
      Cipher cipher = Cipher.getInstance( PUBLIC_KEY_ALGORITHM );
      cipher.init( Cipher.DECRYPT_MODE, key );
      result = cipher.doFinal( data );
    } catch ( Exception ex ) {
      log.logError( ex.getLocalizedMessage(), ex );
    }
    return result;
  }
}
