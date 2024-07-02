/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.connections.common.bucket.TestConnectionWithBucketsDetailsChild;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.encryption.Encr;

public class EncryptUtilsTest {

  private static String PASSWORD = "testpassword";
  private static String PASSWORD2 = "testpassword2";
  private static String PASSWORD3 = "testpassword3";


  @Before
  public void setUp() throws Exception {
    KettleClientEnvironment.init();
  }

  @Test
  public void testTransformFields() {
    TestConnectionWithBucketsDetailsChild testConnectionDetails = new TestConnectionWithBucketsDetailsChild();
    testConnectionDetails.setPassword( PASSWORD );
    testConnectionDetails.setPassword3( PASSWORD3 );
    EncryptUtils.transformFields( testConnectionDetails, Encr::encryptPasswordIfNotUsingVariables );
    Assert.assertTrue( testConnectionDetails.getPassword().startsWith( "Encrypted " ) );
  }

  @Test
  public void testGetValue() throws NoSuchFieldException {
    TestConnectionWithBucketsDetailsChild testConnectionDetails = new TestConnectionWithBucketsDetailsChild();
    testConnectionDetails.setPassword( PASSWORD );
    testConnectionDetails.setPassword3( PASSWORD3 );
    String value = EncryptUtils.getValue( testConnectionDetails,
      testConnectionDetails.getClass().getDeclaredField( "password3" ) );
    Assert.assertNotNull( value );
    Assert.assertEquals( PASSWORD3, value );
  }

  @Test
  public void testSetValue() throws NoSuchFieldException {
    TestConnectionWithBucketsDetailsChild testConnectionDetails = new TestConnectionWithBucketsDetailsChild();
    testConnectionDetails.setPassword( PASSWORD );
    testConnectionDetails.setPassword3( PASSWORD3 );
    EncryptUtils.setValue( testConnectionDetails,
      testConnectionDetails.getClass().getDeclaredField( "password3" ), PASSWORD2 );
    Assert.assertEquals( testConnectionDetails.getPassword3(), PASSWORD2 );
  }
}
