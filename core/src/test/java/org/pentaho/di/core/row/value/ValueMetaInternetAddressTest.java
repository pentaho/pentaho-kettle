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

import org.junit.Test;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ValueMetaInternetAddressTest {

  private static final String SAMPLE_IPV4_AS_STRING = "192.181.070.0";
  private static final byte[] SAMPLE_IPV4_AS_BYTES = { (byte) 192, (byte) 181, (byte) 70, (byte) 0 };
  private static final String SAMPLE_IPV6_AS_STRING = "950:0:2C:1000:208:800:200:A";
  private static final byte[] SAMPLE_IPV6_AS_BYTES = {
    (byte) 9, (byte) 80, // 950
    (byte) 0, (byte) 0,  // 0
    (byte) 0, (byte) 44, // 2C
    (byte) 16, (byte) 0, // 1000
    (byte) 2, (byte) 8,  // 208
    (byte) 8, (byte) 0,  // 800
    (byte) 2, (byte) 0,  // 200
    (byte) 0, (byte) 10  // A
  };
  static final BigInteger BI_256 = BigInteger.valueOf( 256L );

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
  public void testGetInteger_Success() throws UnknownHostException, KettleValueException {
    ValueMetaInternetAddress vm = new ValueMetaInternetAddress();
    String[] addresses = {
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
      vm.getInteger( addr );
    }
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
  public void testGetNumber_Success() throws UnknownHostException, KettleValueException {
    ValueMetaInternetAddress vm = new ValueMetaInternetAddress();
    String[] addresses = {
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
      vm.getNumber( addr );
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

  @Test
  public void testConvertStringIPv4() throws Exception {
    ValueMetaInterface vmia = new ValueMetaInternetAddress( "Test" );
    ValueMetaString vms = new ValueMetaString( "aString" );

    Object convertedIPv4 = vmia.convertData( vms, SAMPLE_IPV4_AS_STRING );
    assertNotNull( convertedIPv4 );
    assertTrue( convertedIPv4 instanceof InetAddress );
    assertArrayEquals( SAMPLE_IPV4_AS_BYTES, ( (InetAddress) convertedIPv4 ).getAddress() );
  }

  @Test
  public void testConvertStringIPv6() throws Exception {
    ValueMetaInterface vmia = new ValueMetaInternetAddress( "Test" );
    ValueMetaString vms = new ValueMetaString( "aString" );

    Object convertedIPv6 = vmia.convertData( vms, SAMPLE_IPV6_AS_STRING );
    assertNotNull( convertedIPv6 );
    assertTrue( convertedIPv6 instanceof InetAddress );
    assertArrayEquals( SAMPLE_IPV6_AS_BYTES, ( (InetAddress) convertedIPv6 ).getAddress() );
  }

  @Test
  public void testConvertIntegerIPv4() throws Exception {
    ValueMetaInterface vmia = new ValueMetaInternetAddress( "Test" );
    ValueMetaInteger vmi = new ValueMetaInteger( "anInteger" );

    Object convertedIPv4 = vmia.convertData( vmi, InetAddress2BigInteger( SAMPLE_IPV4_AS_BYTES ).longValue() );
    assertNotNull( convertedIPv4 );
    assertTrue( convertedIPv4 instanceof InetAddress );
    assertArrayEquals( SAMPLE_IPV4_AS_BYTES, ( (InetAddress) convertedIPv4 ).getAddress() );
  }

  @Test
  public void testConvertNumberIPv4() throws Exception {
    ValueMetaInterface vmia = new ValueMetaInternetAddress( "Test" );
    ValueMetaNumber vmn = new ValueMetaNumber( "aNumber" );

    Object convertedIPv4 = vmia.convertData( vmn, InetAddress2BigInteger( SAMPLE_IPV4_AS_BYTES ).doubleValue() );
    assertNotNull( convertedIPv4 );
    assertTrue( convertedIPv4 instanceof InetAddress );
    assertArrayEquals( SAMPLE_IPV4_AS_BYTES, ( (InetAddress) convertedIPv4 ).getAddress() );
  }

  @Test
  public void testConvertBigNumberIPv4() throws Exception {
    ValueMetaInterface vmia = new ValueMetaInternetAddress( "Test" );
    ValueMetaBigNumber vmbn = new ValueMetaBigNumber( "aBigNumber" );

    Object convertedIPv4 = vmia.convertData( vmbn, new BigDecimal( InetAddress2BigInteger( SAMPLE_IPV4_AS_BYTES ) ) );
    assertNotNull( convertedIPv4 );
    assertTrue( convertedIPv4 instanceof InetAddress );
    assertArrayEquals( SAMPLE_IPV4_AS_BYTES, ( (InetAddress) convertedIPv4 ).getAddress() );
  }

  @Test
  public void testConvertInternetAddressIPv4() throws Exception {
    ValueMetaInterface vmia = new ValueMetaInternetAddress( "Test" );
    ValueMetaInternetAddress vmia2 = new ValueMetaInternetAddress( "anInternetAddress" );

    Object convertedIPv4 = vmia.convertData( vmia2, InetAddress.getByAddress( SAMPLE_IPV4_AS_BYTES ) );
    assertNotNull( convertedIPv4 );
    assertTrue( convertedIPv4 instanceof InetAddress );
    assertArrayEquals( SAMPLE_IPV4_AS_BYTES, ( (InetAddress) convertedIPv4 ).getAddress() );
  }

  @Test
  public void testConvertInternetAddressIPv6() throws Exception {
    ValueMetaInterface vmia = new ValueMetaInternetAddress( "Test" );
    ValueMetaInternetAddress vmia2 = new ValueMetaInternetAddress( "anInternetAddress" );

    Object convertedIPv6 = vmia.convertData( vmia2, InetAddress.getByAddress( SAMPLE_IPV6_AS_BYTES ) );
    assertNotNull( convertedIPv6 );
    assertTrue( convertedIPv6 instanceof InetAddress );
    assertArrayEquals( SAMPLE_IPV6_AS_BYTES, ( (InetAddress) convertedIPv6 ).getAddress() );
  }

  /**
   * This test ensures that the conversion from an InetAddress to a number (BigInteger) is working as expected.
   */
  @Test
  public void testInternalInetAddress2BigInteger() {
    assertEquals( BigInteger.valueOf( 3233105408L ), InetAddress2BigInteger( SAMPLE_IPV4_AS_BYTES ) );

    assertEquals( ( new BigDecimal( "12378435710800297360489848630887317514" ) ).toBigInteger(),
      InetAddress2BigInteger( SAMPLE_IPV6_AS_BYTES ) );
  }

  private BigInteger InetAddress2BigInteger( byte[] bytes ) {
    BigInteger ret = BigInteger.ZERO;

    for ( byte b : bytes ) {
      ret = ret.shiftLeft( 8 ).add( BigInteger.valueOf( b & 0xFF ) );
    }

    return ret;
  }
}
