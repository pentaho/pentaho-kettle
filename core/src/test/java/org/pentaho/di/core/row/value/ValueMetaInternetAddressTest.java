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

package org.pentaho.di.core.row.value;

import java.math.BigDecimal;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ValueMetaInternetAddressTest {

  @Test
  public void testCompare() throws UnknownHostException, KettleValueException {
    ValueMetaInternetAddress vm = new ValueMetaInternetAddress();
    InetAddress smaller = InetAddress.getByName( "127.0.0.1" );
    InetAddress larger = InetAddress.getByName( "127.0.1.1" );

    assertTrue( vm.isSortedAscending() );
    assertFalse( vm.isSortedDescending() );
    assertEquals( 0, vm.compare( null, null ) );
    assertEquals( -1, vm.compare( null, smaller ) );
    assertEquals( 1, vm.compare( smaller, null ) );
    assertEquals( 0, vm.compare( smaller, smaller ) );
    assertEquals( -1, vm.compare( smaller, larger ) );
    assertEquals( 1, vm.compare( larger, smaller ) );

    vm.setSortedDescending( true );
    assertFalse( vm.isSortedAscending() );
    assertTrue( vm.isSortedDescending() );
    assertEquals( 0, vm.compare( null, null ) );
    assertEquals( 1, vm.compare( null, smaller ) );
    assertEquals( -1, vm.compare( smaller, null ) );
    assertEquals( 0, vm.compare( smaller, smaller ) );
    assertEquals( 1, vm.compare( smaller, larger ) );
    assertEquals( -1, vm.compare( larger, smaller ) );
  }

  @Test
  public void testCompare_PDI17270() throws UnknownHostException, KettleValueException {
    ValueMetaInternetAddress vm = new ValueMetaInternetAddress();

    InetAddress smaller = InetAddress.getByName( "0.0.0.0" );
    InetAddress larger = InetAddress.getByName( "255.250.200.128" );

    assertEquals( -1, vm.compare( smaller, larger ) );
    assertEquals( 1, vm.compare( larger, smaller ) );

    smaller = InetAddress.getByName( "0.0.0.0" );
    larger = InetAddress.getByName( "192.168.10.0" );

    assertEquals( -1, vm.compare( smaller, larger ) );
    assertEquals( 1, vm.compare( larger, smaller ) );

    smaller = InetAddress.getByName( "192.168.10.0" );
    larger = InetAddress.getByName( "255.250.200.128" );

    assertEquals( -1, vm.compare( smaller, larger ) );
    assertEquals( 1, vm.compare( larger, smaller ) );
  }

  @Test
  public void testCompare_Representations() throws UnknownHostException, KettleValueException {
    ValueMetaInternetAddress vm = new ValueMetaInternetAddress();

    InetAddress extended = InetAddress.getByName( "1080:0:0:0:8:800:200C:417A" );
    InetAddress condensed = InetAddress.getByName( "1080::8:800:200C:417A" );

    assertEquals( 0, vm.compare( extended, condensed ) );
    assertEquals( 0, vm.compare( condensed, extended ) );

    extended = InetAddress.getByName( "0:0:0:0:0:0:0:1" );
    condensed = InetAddress.getByName( "::1" );

    assertEquals( 0, vm.compare( extended, condensed ) );
    assertEquals( 0, vm.compare( condensed, extended ) );

    extended = InetAddress.getByName( "0:0:0:0:0:0:0:0" );
    condensed = InetAddress.getByName( "::0" );

    assertEquals( 0, vm.compare( extended, condensed ) );
    assertEquals( 0, vm.compare( condensed, extended ) );
  }

  @Test
  public void testGetBigNumber_NullParameter() throws UnknownHostException, KettleValueException {
    ValueMetaInternetAddress vm = new ValueMetaInternetAddress();

    assertNull( vm.getBigNumber( null ) );
  }

  @Test
  public void testGetBigNumber_Success() throws UnknownHostException, KettleValueException {
    ValueMetaInternetAddress vm = new ValueMetaInternetAddress();
    String[] addresses = {
      // Some IPv6 addresses
      "1080:0:0:0:8:800:200C:417A", "1080::8:800:200C:417A",
      "::1", "0:0:0:0:0:0:0:1",
      "::", "0:0:0:0:0:0:0:0",
      "::d",
      // Some IPv4-mapped IPv6 addresses
      "::ffff:0:0",
      "::ffff:d",
      "::ffff:127.0.0.1",
      // Some IPv4-compatible IPv6 addresses
      "::0.0.0.0",
      "::255.255.0.10",
      // Some IPv4 addresses
      "192.168.10.0",
      "0.0.0.1",
      "0.0.0.0",
      "127.0.0.1",
      "255.255.0.10",
      "192.0.2.235"
    };

    // No exception should be thrown in any of the following calls
    for ( String address : addresses ) {
      InetAddress addr = InetAddress.getByName( address );
      vm.getBigNumber( addr );
    }
  }

  @Test
  public void testGetBinaryString() throws KettleValueException, UnknownHostException {
    // Test normal storage type
    ValueMetaInternetAddress vmInet = new ValueMetaInternetAddress();
    final ValueMetaString vmString = new ValueMetaString();
    vmInet.setStorageMetadata( vmString );
    InetAddress inetAddress = InetAddress.getByName( "127.0.0.1" );

    byte[] output = vmInet.getBinaryString( inetAddress );
    assertNotNull( output );
    assertArrayEquals( vmString.getBinaryString( "127.0.0.1" ), output );
    assertEquals( inetAddress, vmInet.convertBinaryStringToNativeType( output ) );

    // Test binary string storage type
    vmInet.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    output = vmInet.getBinaryString( vmString.getBinaryString( "127.0.0.1" ) );
    assertNotNull( output );
    assertArrayEquals( vmString.getBinaryString( "127.0.0.1" ), output );
    assertEquals( inetAddress, vmInet.convertBinaryStringToNativeType( output ) );

    // Test indexed storage
    vmInet.setStorageType( ValueMetaInterface.STORAGE_TYPE_INDEXED );
    vmInet.setIndex( new InetAddress[] { inetAddress } );
    assertArrayEquals( vmString.getBinaryString( "127.0.0.1" ), vmInet.getBinaryString( 0 ) );
    assertEquals( inetAddress, vmInet.convertBinaryStringToNativeType( output ) );

    try {
      vmInet.getBinaryString( 1 );
      fail();
    } catch ( ArrayIndexOutOfBoundsException e ) {
      // expected
    }
  }

  @Test
  public void testGetNativeDataType() throws UnknownHostException, KettleValueException {
    ValueMetaInterface vmi = new ValueMetaInternetAddress( "Test" );
    InetAddress expected = InetAddress.getByAddress( new byte[] { (byte) 192, (byte) 168, 1, 1 } );

    assertEquals( ValueMetaInterface.TYPE_INET, vmi.getType() );
    assertEquals( ValueMetaInterface.STORAGE_TYPE_NORMAL, vmi.getStorageType() );
    assertSame( expected, vmi.getNativeDataType( expected ) );
  }
}
