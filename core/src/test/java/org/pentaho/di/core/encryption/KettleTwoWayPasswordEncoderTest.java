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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleValueException;

/**
 * Test cases for encryption, to make sure that encrypted password remain the same between versions.
 *
 * @author Matt Casters
 */
public class KettleTwoWayPasswordEncoderTest {

  /**
   * Test password encryption.
   *
   * @throws KettleValueException
   */
  @Test
  public void testEncode1() throws KettleValueException {

    KettleTwoWayPasswordEncoder encoder = new KettleTwoWayPasswordEncoder();

    String encryption;

    encryption = encoder.encode( null, false );
    assertTrue( "".equals( encryption ) );

    encryption = encoder.encode( "", false );
    assertTrue( "".equals( encryption ) );

    encryption = encoder.encode( "     ", false );
    assertTrue( "2be98afc86aa7f2e4cb79ce309ed2ef9a".equals( encryption ) );

    encryption = encoder.encode( "Test of different encryptions!!@#$%", false );
    assertTrue( "54657374206f6620646966666572656e742067d0fbddb11ad39b8ba50aef31fed1eb9f".equals( encryption ) );

    encryption = encoder.encode( "  Spaces left", false );
    assertTrue( "2be98afe84af48285a81cbd30d297a9ce".equals( encryption ) );

    encryption = encoder.encode( "Spaces right", false );
    assertTrue( "2be98afc839d79387ae0aee62d795a7ce".equals( encryption ) );

    encryption = encoder.encode( "     Spaces  ", false );
    assertTrue( "2be98afe84a87d2c49809af73db81ef9a".equals( encryption ) );

    encryption = encoder.encode( "1234567890", false );
    assertTrue( "2be98afc86aa7c3d6f84dfb2689caf68a".equals( encryption ) );
  }

  /**
   * Test password decryption.
   *
   * @throws KettleValueException
   */
  @Test
  public void testDecode1() throws KettleValueException {
    KettleTwoWayPasswordEncoder encoder = new KettleTwoWayPasswordEncoder();

    String encryption;
    String decryption;

    encryption = encoder.encode( null );
    decryption = encoder.decode( encryption );
    assertTrue( "".equals( decryption ) );

    encryption = encoder.encode( "" );
    decryption = encoder.decode( encryption );
    assertTrue( "".equals( decryption ) );

    encryption = encoder.encode( "     " );
    decryption = encoder.decode( encryption );
    assertTrue( "     ".equals( decryption ) );

    encryption = encoder.encode( "Test of different encryptions!!@#$%" );
    decryption = encoder.decode( encryption );
    assertTrue( "Test of different encryptions!!@#$%".equals( decryption ) );

    encryption = encoder.encode( "  Spaces left" );
    decryption = encoder.decode( encryption );
    assertTrue( "  Spaces left".equals( decryption ) );

    encryption = encoder.encode( "Spaces right" );
    decryption = encoder.decode( encryption );
    assertTrue( "Spaces right".equals( decryption ) );

    encryption = encoder.encode( "     Spaces  " );
    decryption = encoder.decode( encryption );
    assertTrue( "     Spaces  ".equals( decryption ) );

    encryption = encoder.encode( "1234567890" );
    decryption = encoder.decode( encryption );
    assertTrue( "1234567890".equals( decryption ) );

    assertEquals( "", encoder.decode( null ) );
  }

  /**
   * Test password encryption (variable style).
   *
   * @throws KettleValueException
   */
  @Test
  public void testEncode2() throws KettleValueException {
    KettleTwoWayPasswordEncoder encoder = new KettleTwoWayPasswordEncoder();

    String encryption;

    encryption = encoder.encode( null );
    assertTrue( "Encrypted ".equals( encryption ) );

    encryption = encoder.encode( "" );
    assertTrue( "Encrypted ".equals( encryption ) );

    encryption = encoder.encode( "String" );
    assertTrue( "Encrypted 2be98afc86aa7f2e4cb799d64cc9ba1dd".equals( encryption ) );

    encryption = encoder.encode( " ${VAR} String" );
    assertTrue( " ${VAR} String".equals( encryption ) );

    encryption = encoder.encode( " %%VAR%% String" );
    assertTrue( " %%VAR%% String".equals( encryption ) );

    encryption = encoder.encode( " %% VAR String" );
    assertTrue( "Encrypted 2be988fed4f87a4a599599d64cc9ba1dd".equals( encryption ) );

    encryption = encoder.encode( "${%%$$$$" );
    assertTrue( "Encrypted 2be98afc86aa7f2e4ef02eb359ad6eb9e".equals( encryption ) );
  }

  /**
   * Test password decryption (variable style).
   *
   * @throws KettleValueException
   */
  @Test
  public void testDecode2() throws KettleValueException {
    KettleTwoWayPasswordEncoder encoder = new KettleTwoWayPasswordEncoder();

    String encryption;
    String decryption;

    encryption = encoder.encode( null );
    decryption = encoder.decode( encryption );
    assertTrue( "".equals( decryption ) );

    encryption = encoder.encode( "" );
    decryption = encoder.decode( encryption );
    assertTrue( "".equals( decryption ) );

    encryption = encoder.encode( "String" );
    decryption = encoder.decode( encryption );
    assertTrue( "String".equals( decryption ) );

    encryption = encoder.encode( " ${VAR} String", false );
    decryption = encoder.decode( encryption );
    assertTrue( " ${VAR} String".equals( decryption ) );

    encryption = encoder.encode( " %%VAR%% String", false );
    decryption = encoder.decode( encryption );
    assertTrue( " %%VAR%% String".equals( decryption ) );

    encryption = encoder.encode( " %% VAR String", false );
    decryption = encoder.decode( encryption );
    assertTrue( " %% VAR String".equals( decryption ) );

    encryption = encoder.encode( "${%%$$$$", false );
    decryption = encoder.decode( encryption );
    assertTrue( "${%%$$$$".equals( decryption ) );
  }

  @Test
  public void testEncodeDifferentSeed() {

    KettleTwoWayPasswordEncoder encoder = new KettleTwoWayPasswordEncoder( );
    String encodeWithDefaultSeed = encoder.encode( "Wibble", false );
    assertNotNull( encodeWithDefaultSeed );
    String decodeWithDefaultSeed = encoder.decode( encodeWithDefaultSeed );
    assertNotNull( decodeWithDefaultSeed );


    TestKettleTwoWayPasswordEncoder encoder2 = new TestKettleTwoWayPasswordEncoder();

    String encodeWithNondefaultSeed = encoder2.encode( "Wibble", false );
    assertNotNull( encodeWithNondefaultSeed );
    String decodeWithNondefaultSeed = encoder2.decode( encodeWithNondefaultSeed );
    assertNotNull( decodeWithNondefaultSeed );

    assertFalse( encodeWithDefaultSeed.equals( encodeWithNondefaultSeed ) ); // Make sure that if the seed changes, so does the the encoded value
    assertEquals( decodeWithDefaultSeed, decodeWithNondefaultSeed ); // Make sure that the decode from either is correct.

  }

  private class TestKettleTwoWayPasswordEncoder extends KettleTwoWayPasswordEncoder {

    public TestKettleTwoWayPasswordEncoder() {
      super();
    }

    protected String getSeed() {
      return "123456789012345435987";
    }

  }

}
