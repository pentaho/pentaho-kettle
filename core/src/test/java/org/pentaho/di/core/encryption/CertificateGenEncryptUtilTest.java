/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import java.security.KeyPair;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

import java.security.Key;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class CertificateGenEncryptUtilTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();
  private String pattern = "Test string \u2020";

  @BeforeClass
  public static void setupClass() {
    KettleLogStore.init();
  }

  @Test
  public void testPublicPrivate() {
    byte[] pat = pattern.getBytes();
    KeyPair kp = CertificateGenEncryptUtil.generateKeyPair();

    byte [] encr = CertificateGenEncryptUtil.encryptUsingKey( pat, kp.getPublic() );
    byte [] decr = CertificateGenEncryptUtil.decryptUsingKey( encr, kp.getPrivate() );

    assertTrue( Arrays.equals( pat, decr ) );
  }

  @Test
  public void testPrivatePublic() {
    byte[] pat = pattern.getBytes();
    KeyPair kp = CertificateGenEncryptUtil.generateKeyPair();

    byte [] encr = CertificateGenEncryptUtil.encryptUsingKey( pat, kp.getPrivate() );
    byte [] decr = CertificateGenEncryptUtil.decryptUsingKey( encr, kp.getPublic() );

    assertTrue( Arrays.equals( pat, decr ) );
  }

  @Test
  public void testPrivateAnotherPublic() {
    byte[] pat = pattern.getBytes();
    KeyPair kp = CertificateGenEncryptUtil.generateKeyPair();

    byte [] encr = CertificateGenEncryptUtil.encryptUsingKey( pat, kp.getPrivate() );
    KeyPair kp1 = CertificateGenEncryptUtil.generateKeyPair();
    byte [] decr = CertificateGenEncryptUtil.decryptUsingKey( encr, kp1.getPublic() );
    assertNotEquals( encr, decr );
    decr = CertificateGenEncryptUtil.decryptUsingKey( encr, kp.getPublic() );
    assertTrue( Arrays.equals( pat, decr ) );
  }

  @Test
  public void testPublicPublic() {
    byte[] pat = pattern.getBytes();
    KeyPair kp = CertificateGenEncryptUtil.generateKeyPair();

    byte [] encr = CertificateGenEncryptUtil.encryptUsingKey( pat, kp.getPublic() );
    byte [] decr = CertificateGenEncryptUtil.decryptUsingKey( encr, kp.getPublic() );

    assertNotEquals( encr, decr );
  }

  @Test
  public void testPrivatePrivate() {
    byte[] pat = pattern.getBytes();
    KeyPair kp = CertificateGenEncryptUtil.generateKeyPair();

    byte [] encr = CertificateGenEncryptUtil.encryptUsingKey( pat, kp.getPrivate() );
    byte [] decr = CertificateGenEncryptUtil.decryptUsingKey( encr, kp.getPrivate() );

    assertNotEquals( encr, decr );
  }

  @Test
  public void testRandomSessionKeyGeneration() throws Exception {
    Key key = CertificateGenEncryptUtil.generateSingleKey();
    Key key1 = CertificateGenEncryptUtil.generateSingleKey();
    assertFalse( key.equals( key1 ) );
    assertFalse( Arrays.equals( key.getEncoded(), key1.getEncoded() ) );
  }

  @Test
  public void testSessionKeyEncryptionDecryption() throws Exception {
    Key key = CertificateGenEncryptUtil.generateSingleKey();
    KeyPair kp = CertificateGenEncryptUtil.generateKeyPair();
    Key privateKey = kp.getPrivate();
    byte[] encryptedKey = CertificateGenEncryptUtil.encodeKeyForTransmission( privateKey, key );
    Key key1 = CertificateGenEncryptUtil.decodeTransmittedKey( kp.getPublic().getEncoded(), encryptedKey, false );
    assertTrue( key.equals( key1 ) );
  }

  @Test
  public void testSessionKeyEncryptionDecryption2() throws Exception {
    Key key = CertificateGenEncryptUtil.generateSingleKey();
    KeyPair kp = CertificateGenEncryptUtil.generateKeyPair();
    Key privateKey = kp.getPrivate();
    byte[] encryptedKey = CertificateGenEncryptUtil.encodeKeyForTransmission( kp.getPublic(), key );
    Key key1 = CertificateGenEncryptUtil.decodeTransmittedKey( privateKey.getEncoded(), encryptedKey, true );
    assertTrue( key.equals( key1 ) );
  }

  @Test( expected = Exception.class )
  public void testImproperSessionKeyEncryptionDecryption() throws Exception {
    Key key = CertificateGenEncryptUtil.generateSingleKey();
    KeyPair kp = CertificateGenEncryptUtil.generateKeyPair();
    Key privateKey = kp.getPrivate();
    byte[] encryptedKey = CertificateGenEncryptUtil.encodeKeyForTransmission( kp.getPublic(), key );
    CertificateGenEncryptUtil.decodeTransmittedKey( privateKey.getEncoded(), encryptedKey, false );
  }

  @Test( expected = Exception.class )
  public void testImproperSessionKeyEncryptionDecryption2() throws Exception {
    Key key = CertificateGenEncryptUtil.generateSingleKey();
    KeyPair kp = CertificateGenEncryptUtil.generateKeyPair();
    Key privateKey = kp.getPrivate();
    byte[] encryptedKey = CertificateGenEncryptUtil.encodeKeyForTransmission( privateKey, key );
    CertificateGenEncryptUtil.decodeTransmittedKey( kp.getPublic().getEncoded(), encryptedKey, true );
  }

  @Test( expected = Exception.class )
  public void testImproperSessionKeyEncryptionDecryption3() throws Exception {
    Key key = CertificateGenEncryptUtil.generateSingleKey();
    KeyPair kp = CertificateGenEncryptUtil.generateKeyPair();
    Key privateKey = kp.getPrivate();
    byte[] encryptedKey = CertificateGenEncryptUtil.encodeKeyForTransmission( kp.getPublic(), key );
    byte[] encryptedKey1 = new byte[encryptedKey.length];
    System.arraycopy( encryptedKey, 0, encryptedKey1, 0, encryptedKey.length );
    encryptedKey1[encryptedKey1.length - 1] = (byte) ( encryptedKey1[encryptedKey1.length - 1] - 1 );
    encryptedKey = encryptedKey1;
    CertificateGenEncryptUtil.decodeTransmittedKey( privateKey.getEncoded(), encryptedKey, true );
  }

  @Test( expected = Exception.class )
  public void testImproperSessionKeyEncryptionDecryption4() throws Exception {
    Key key = CertificateGenEncryptUtil.generateSingleKey();
    KeyPair kp = CertificateGenEncryptUtil.generateKeyPair();
    Key privateKey = kp.getPrivate();
    byte[] encryptedKey = CertificateGenEncryptUtil.encodeKeyForTransmission( privateKey, key );
    byte[] encryptedKey1 = new byte[encryptedKey.length];
    System.arraycopy( encryptedKey, 0, encryptedKey1, 0, encryptedKey.length );
    encryptedKey1[encryptedKey1.length - 1] = (byte) ( encryptedKey1[encryptedKey1.length - 1] - 1 );
    encryptedKey = encryptedKey1;
    CertificateGenEncryptUtil.decodeTransmittedKey( kp.getPublic().getEncoded(), encryptedKey, false );
  }

  @Test( expected = Exception.class )
  public void testImproperSessionKeyEncryptionDecryption5() throws Exception {
    Key key = CertificateGenEncryptUtil.generateSingleKey();
    KeyPair kp = CertificateGenEncryptUtil.generateKeyPair();
    Key privateKey = kp.getPrivate();
    byte[] encryptedKey = CertificateGenEncryptUtil.encodeKeyForTransmission( kp.getPublic(), key );
    byte[] encryptedKey1 = new byte[privateKey.getEncoded().length];
    System.arraycopy( privateKey.getEncoded(), 0, encryptedKey1, 0, privateKey.getEncoded().length );
    encryptedKey1[encryptedKey1.length - 1] = (byte) ( encryptedKey1[encryptedKey1.length - 1] - 1 );
    CertificateGenEncryptUtil.decodeTransmittedKey( encryptedKey1, encryptedKey, true );
  }

  @Test( expected = Exception.class )
  public void testImproperSessionKeyEncryptionDecryption6() throws Exception {
    Key key = CertificateGenEncryptUtil.generateSingleKey();
    KeyPair kp = CertificateGenEncryptUtil.generateKeyPair();
    Key privateKey = kp.getPrivate();
    byte[] encryptedKey = CertificateGenEncryptUtil.encodeKeyForTransmission( privateKey, key );
    byte[] encryptedKey1 = new byte[kp.getPublic().getEncoded().length];
    System.arraycopy( kp.getPublic().getEncoded(), 0, encryptedKey1, 0, kp.getPublic().getEncoded().length );
    encryptedKey1[encryptedKey1.length - 1] = (byte) ( encryptedKey1[encryptedKey1.length - 1] - 1 );
    CertificateGenEncryptUtil.decodeTransmittedKey( encryptedKey1, encryptedKey, false );
  }
}
