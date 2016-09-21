/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.row.value;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaInternetAddressTest {

  @Test
  public void testCompare() throws UnknownHostException, KettleValueException {
    ValueMetaInternetAddress vm = new ValueMetaInternetAddress();
    InetAddress smaller = InetAddress.getByName( "127.0.0.1" );
    InetAddress larger = InetAddress.getByName( "127.0.1.1" );
    Assert.assertTrue( vm.isSortedAscending() );
    Assert.assertFalse( vm.isSortedDescending() );
    Assert.assertEquals( 0, vm.compare( null, null ) );
    Assert.assertEquals( -1, vm.compare( null, smaller ) );
    Assert.assertEquals( 1, vm.compare( smaller, null ) );
    Assert.assertEquals( 0, vm.compare( smaller, smaller ) );
    Assert.assertEquals( -1, vm.compare( smaller, larger ) );
    Assert.assertEquals( 1, vm.compare( larger, smaller ) );

    vm.setSortedDescending( true );
    Assert.assertFalse( vm.isSortedAscending() );
    Assert.assertTrue( vm.isSortedDescending() );
    Assert.assertEquals( 0, vm.compare( null, null ) );
    Assert.assertEquals( 1, vm.compare( null, smaller ) );
    Assert.assertEquals( -1, vm.compare( smaller, null ) );
    Assert.assertEquals( 0, vm.compare( smaller, smaller ) );
    Assert.assertEquals( 1, vm.compare( smaller, larger ) );
    Assert.assertEquals( -1, vm.compare( larger, smaller ) );
  }

  @Test
  public void testGetBinaryString() throws KettleValueException, UnknownHostException {
    // Test normal storage type
    ValueMetaInternetAddress vmInet = new ValueMetaInternetAddress();
    final ValueMetaString vmString = new ValueMetaString();
    InetAddress inetAddress = InetAddress.getByName( "127.0.0.1" );
    byte[] output = vmInet.getBinaryString( inetAddress );
    Assert.assertNotNull( output );
    Assert.assertArrayEquals( vmString.getBinaryString( "127.0.0.1" ), output );

    // Test binary string storage type
    vmInet.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    output = vmInet.getBinaryString( vmString.getBinaryString( "127.0.0.1" ) );
    Assert.assertNotNull( output );
    Assert.assertArrayEquals( vmString.getBinaryString( "127.0.0.1" ), output );

    // Test indexed storage
    vmInet.setStorageType( ValueMetaInterface.STORAGE_TYPE_INDEXED );
    vmInet.setIndex( new InetAddress[] { inetAddress } );
    Assert.assertArrayEquals( vmString.getBinaryString( "127.0.0.1" ), vmInet.getBinaryString( 0 ) );
    try {
      vmInet.getBinaryString( 1 );
      Assert.fail();
    } catch ( ArrayIndexOutOfBoundsException e ) {
      // expected
    }
  }
}
