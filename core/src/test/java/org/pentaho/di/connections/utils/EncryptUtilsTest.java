/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
