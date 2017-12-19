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

package org.pentaho.di.core.encryption;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleValueException;

/**
 * Test cases for encryption, to make sure that encrypted password remain the same between versions.
 *
 * @author Sven Boden
 */
public class EncrTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleClientEnvironment.init();
  }

  /**
   * Test password encryption.
   *
   * @throws KettleValueException
   */
  @Test
  public void testEncryptPassword() throws KettleValueException {
    String encryption;

    encryption = Encr.encryptPassword( null );
    assertTrue( "".equals( encryption ) );

    encryption = Encr.encryptPassword( "" );
    assertTrue( "".equals( encryption ) );

    encryption = Encr.encryptPassword( "     " );
    assertTrue( "2be98afc86aa7f2e4cb79ce309ed2ef9a".equals( encryption ) );

    encryption = Encr.encryptPassword( "Test of different encryptions!!@#$%" );
    assertTrue( "54657374206f6620646966666572656e742067d0fbddb11ad39b8ba50aef31fed1eb9f".equals( encryption ) );

    encryption = Encr.encryptPassword( "  Spaces left" );
    assertTrue( "2be98afe84af48285a81cbd30d297a9ce".equals( encryption ) );

    encryption = Encr.encryptPassword( "Spaces right" );
    assertTrue( "2be98afc839d79387ae0aee62d795a7ce".equals( encryption ) );

    encryption = Encr.encryptPassword( "     Spaces  " );
    assertTrue( "2be98afe84a87d2c49809af73db81ef9a".equals( encryption ) );

    encryption = Encr.encryptPassword( "1234567890" );
    assertTrue( "2be98afc86aa7c3d6f84dfb2689caf68a".equals( encryption ) );
  }

  /**
   * Test password decryption.
   *
   * @throws KettleValueException
   */
  @Test
  public void testDecryptPassword() throws KettleValueException {
    String encryption;
    String decryption;

    encryption = Encr.encryptPassword( null );
    decryption = Encr.decryptPassword( encryption );
    assertTrue( "".equals( decryption ) );

    encryption = Encr.encryptPassword( "" );
    decryption = Encr.decryptPassword( encryption );
    assertTrue( "".equals( decryption ) );

    encryption = Encr.encryptPassword( "     " );
    decryption = Encr.decryptPassword( encryption );
    assertTrue( "     ".equals( decryption ) );

    encryption = Encr.encryptPassword( "Test of different encryptions!!@#$%" );
    decryption = Encr.decryptPassword( encryption );
    assertTrue( "Test of different encryptions!!@#$%".equals( decryption ) );

    encryption = Encr.encryptPassword( "  Spaces left" );
    decryption = Encr.decryptPassword( encryption );
    assertTrue( "  Spaces left".equals( decryption ) );

    encryption = Encr.encryptPassword( "Spaces right" );
    decryption = Encr.decryptPassword( encryption );
    assertTrue( "Spaces right".equals( decryption ) );

    encryption = Encr.encryptPassword( "     Spaces  " );
    decryption = Encr.decryptPassword( encryption );
    assertTrue( "     Spaces  ".equals( decryption ) );

    encryption = Encr.encryptPassword( "1234567890" );
    decryption = Encr.decryptPassword( encryption );
    assertTrue( "1234567890".equals( decryption ) );
  }

  /**
   * Test password encryption (variable style).
   *
   * @throws KettleValueException
   */
  @Test
  public void testEncryptPasswordIfNotUsingVariables() throws KettleValueException {
    String encryption;

    encryption = Encr.encryptPasswordIfNotUsingVariables( null );
    assertTrue( "Encrypted ".equals( encryption ) );

    encryption = Encr.encryptPasswordIfNotUsingVariables( "" );
    assertTrue( "Encrypted ".equals( encryption ) );

    encryption = Encr.encryptPasswordIfNotUsingVariables( "String" );
    assertTrue( "Encrypted 2be98afc86aa7f2e4cb799d64cc9ba1dd".equals( encryption ) );

    encryption = Encr.encryptPasswordIfNotUsingVariables( " ${VAR} String" );
    assertTrue( " ${VAR} String".equals( encryption ) );

    encryption = Encr.encryptPasswordIfNotUsingVariables( " %%VAR%% String" );
    assertTrue( " %%VAR%% String".equals( encryption ) );

    encryption = Encr.encryptPasswordIfNotUsingVariables( " %% VAR String" );
    assertTrue( "Encrypted 2be988fed4f87a4a599599d64cc9ba1dd".equals( encryption ) );

    encryption = Encr.encryptPasswordIfNotUsingVariables( "${%%$$$$" );
    assertTrue( "Encrypted 2be98afc86aa7f2e4ef02eb359ad6eb9e".equals( encryption ) );
  }

  /**
   * Test password decryption (variable style).
   *
   * @throws KettleValueException
   */
  @Test
  public void testDecryptPasswordIfNotUsingVariables() throws KettleValueException {
    String encryption;
    String decryption;

    encryption = Encr.encryptPasswordIfNotUsingVariables( null );
    decryption = Encr.decryptPasswordOptionallyEncrypted( encryption );
    assertTrue( "".equals( decryption ) );

    encryption = Encr.encryptPasswordIfNotUsingVariables( "" );
    decryption = Encr.decryptPasswordOptionallyEncrypted( encryption );
    assertTrue( "".equals( decryption ) );

    encryption = Encr.encryptPasswordIfNotUsingVariables( "String" );
    decryption = Encr.decryptPasswordOptionallyEncrypted( encryption );
    assertTrue( "String".equals( decryption ) );

    encryption = Encr.encryptPasswordIfNotUsingVariables( " ${VAR} String" );
    decryption = Encr.decryptPasswordOptionallyEncrypted( encryption );
    assertTrue( " ${VAR} String".equals( decryption ) );

    encryption = Encr.encryptPasswordIfNotUsingVariables( " %%VAR%% String" );
    decryption = Encr.decryptPasswordOptionallyEncrypted( encryption );
    assertTrue( " %%VAR%% String".equals( decryption ) );

    encryption = Encr.encryptPasswordIfNotUsingVariables( " %% VAR String" );
    decryption = Encr.decryptPasswordOptionallyEncrypted( encryption );
    assertTrue( " %% VAR String".equals( decryption ) );

    encryption = Encr.encryptPasswordIfNotUsingVariables( "${%%$$$$" );
    decryption = Encr.decryptPasswordOptionallyEncrypted( encryption );
    assertTrue( "${%%$$$$".equals( decryption ) );
  }
}
