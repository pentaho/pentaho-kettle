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

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.crypto.Cipher;

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;

public class CertificateGenEncryptUtil {

  public static final int KEY_SIZE = 1024;
  public static final String ALGORYTHM = "RSA";
  private static final LoggingObjectInterface loggingObject = new SimpleLoggingObject(
      "Certificate Encryption Utility", LoggingObjectType.GENERAL, null );
  private static final LogChannel log = new LogChannel( loggingObject );

  public static KeyPair generateKeyPair() {
    KeyPair pair = null;
    try {
      KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance( ALGORYTHM );
      keyPairGen.initialize( KEY_SIZE );
      pair = keyPairGen.generateKeyPair();
    } catch ( Exception ex ) {
      log.logError( ex.getLocalizedMessage(), ex );
    }
    return pair;
  }

  public static byte[] encryptUsingKey( byte[] data, Key key ) {
    byte[] result = null;
    try {
      Cipher cipher = Cipher.getInstance( ALGORYTHM );
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
      Cipher cipher = Cipher.getInstance( ALGORYTHM );
      cipher.init( Cipher.DECRYPT_MODE, key );
      result = cipher.doFinal( data );
    } catch ( Exception ex ) {
      log.logError( ex.getLocalizedMessage(), ex );
    }
    return result;
  }
}
