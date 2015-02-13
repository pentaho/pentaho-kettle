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

import java.security.KeyPair;
import java.util.Arrays;

import org.junit.Test;
import java.security.Key;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class CertificateGenEncryptUtilTest {
  private String pattern = "Test string \u2020";

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
    try {
      byte [] decr = CertificateGenEncryptUtil.decryptUsingKey( encr, kp1.getPublic() );
      assertFalse( Arrays.equals( encr, decr ) );
      fail();
    } catch ( Exception ex ) { }
    byte [] decr = CertificateGenEncryptUtil.decryptUsingKey( encr, kp.getPublic() );
    assertTrue( Arrays.equals( pat, decr ) );
  }

  @Test
  public void testPublicPublic() {
    byte[] pat = pattern.getBytes();
    try {
      KeyPair kp = CertificateGenEncryptUtil.generateKeyPair();

      byte [] encr = CertificateGenEncryptUtil.encryptUsingKey( pat, kp.getPublic() );
      byte [] decr = CertificateGenEncryptUtil.decryptUsingKey( encr, kp.getPublic() );

      assertFalse( Arrays.equals( encr, decr ) );
      fail();
    } catch ( Exception ex ) { }
  }

  @Test
  public void testPrivatePrivate() {
    byte[] pat = pattern.getBytes();
    try {
      KeyPair kp = CertificateGenEncryptUtil.generateKeyPair();

      byte [] encr = CertificateGenEncryptUtil.encryptUsingKey( pat, kp.getPrivate() );
      byte [] decr = CertificateGenEncryptUtil.decryptUsingKey( encr, kp.getPrivate() );

      assertFalse( Arrays.equals( encr, decr ) );
      fail();
    } catch ( Exception ex ) { }
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
}
