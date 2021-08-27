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

package org.pentaho.di.trans.steps.userdefinedjavaclass;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaSerializable;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.InetAddress;
import java.sql.Timestamp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.reflect.Whitebox.getMethod;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( { FieldHelper.class, FieldHelperTest.class } )
public class FieldHelperTest {

  @Test
  public void getNativeDataTypeSimpleName_Unknown() throws Exception {
    KettleValueException e = new KettleValueException();

    ValueMetaInterface v = mock( ValueMetaInterface.class );
    doThrow( e ).when( v ).getNativeDataTypeClass();

    LogChannel log = mock( LogChannel.class );
    whenNew( LogChannel.class ).withAnyArguments().thenReturn( log );

    assertEquals( "Object", FieldHelper.getNativeDataTypeSimpleName( v ) );
    verify( log, times( 1 ) ).logDebug( "Unable to get name from data type" );
  }

  @Test
  public void getNativeDataTypeSimpleName_String() {
    ValueMetaString v = new ValueMetaString();
    assertEquals( "String", FieldHelper.getNativeDataTypeSimpleName( v ) );
  }

  @Test
  public void getNativeDataTypeSimpleName_InetAddress() {
    ValueMetaInternetAddress v = new ValueMetaInternetAddress();
    assertEquals( "InetAddress", FieldHelper.getNativeDataTypeSimpleName( v ) );
  }

  @Test
  public void getNativeDataTypeSimpleName_Timestamp() {
    ValueMetaTimestamp v = new ValueMetaTimestamp();
    assertEquals( "Timestamp", FieldHelper.getNativeDataTypeSimpleName( v ) );
  }

  @Test
  public void getNativeDataTypeSimpleName_Binary() {
    ValueMetaBinary v = new ValueMetaBinary();
    assertEquals( "Binary", FieldHelper.getNativeDataTypeSimpleName( v ) );
  }

  @Test
  public void getGetSignature_String() {
    ValueMetaString v = new ValueMetaString( "Name" );
    String accessor = FieldHelper.getAccessor( true, "Name" );

    assertEquals( "String Name = get(Fields.In, \"Name\").getString(r);", FieldHelper.getGetSignature( accessor, v ) );
    assertNotNull( getMethod( FieldHelper.class, "getString", Object[].class ) );
  }

  @Test
  public void getGetSignature_InetAddress() {
    ValueMetaInternetAddress v = new ValueMetaInternetAddress( "IP" );
    String accessor = FieldHelper.getAccessor( true, "IP" );

    assertEquals( "InetAddress IP = get(Fields.In, \"IP\").getInetAddress(r);", FieldHelper.getGetSignature( accessor, v ) );
    assertNotNull( getMethod( FieldHelper.class, "getInetAddress", Object[].class ) );
  }

  @Test
  public void getGetSignature_Timestamp() {
    ValueMetaTimestamp v = new ValueMetaTimestamp( "TS" );
    String accessor = FieldHelper.getAccessor( true, "TS" );

    assertEquals( "Timestamp TS = get(Fields.In, \"TS\").getTimestamp(r);", FieldHelper.getGetSignature( accessor, v ) );
    assertNotNull( getMethod( FieldHelper.class, "getTimestamp", Object[].class ) );
  }

  @Test
  public void getGetSignature_Binary() {
    ValueMetaBinary v = new ValueMetaBinary( "Data" );
    String accessor = FieldHelper.getAccessor( true, "Data" );

    assertEquals( "byte[] Data = get(Fields.In, \"Data\").getBinary(r);", FieldHelper.getGetSignature( accessor, v ) );
    assertNotNull( getMethod( FieldHelper.class, "getBinary", Object[].class ) );
  }

  @Test
  public void getGetSignature_BigNumber() {
    ValueMetaBigNumber v = new ValueMetaBigNumber( "Number" );
    String accessor = FieldHelper.getAccessor( true, "Number" );

    assertEquals( "BigDecimal Number = get(Fields.In, \"Number\").getBigDecimal(r);", FieldHelper.getGetSignature( accessor, v ) );
    assertNotNull( getMethod( FieldHelper.class, "getBigDecimal", Object[].class ) );
  }

  @Test
  public void getGetSignature_Boolean() {
    ValueMetaBoolean v = new ValueMetaBoolean( "Value" );
    String accessor = FieldHelper.getAccessor( true, "Value" );

    assertEquals( "Boolean Value = get(Fields.In, \"Value\").getBoolean(r);", FieldHelper.getGetSignature( accessor, v ) );
    assertNotNull( getMethod( FieldHelper.class, "getBoolean", Object[].class ) );
  }

  @Test
  public void getGetSignature_Date() {
    ValueMetaDate v = new ValueMetaDate( "DT" );
    String accessor = FieldHelper.getAccessor( true, "DT" );

    assertEquals( "Date DT = get(Fields.In, \"DT\").getDate(r);", FieldHelper.getGetSignature( accessor, v ) );
    assertNotNull( getMethod( FieldHelper.class, "getDate", Object[].class ) );
  }

  @Test
  public void getGetSignature_Integer() {
    ValueMetaInteger v = new ValueMetaInteger( "Value" );
    String accessor = FieldHelper.getAccessor( true, "Value" );

    assertEquals( "Long Value = get(Fields.In, \"Value\").getLong(r);", FieldHelper.getGetSignature( accessor, v ) );
    assertNotNull( getMethod( FieldHelper.class, "getLong", Object[].class ) );
  }

  @Test
  public void getGetSignature_Number() {
    ValueMetaNumber v = new ValueMetaNumber( "Value" );
    String accessor = FieldHelper.getAccessor( true, "Value" );

    assertEquals( "Double Value = get(Fields.In, \"Value\").getDouble(r);", FieldHelper.getGetSignature( accessor, v ) );
    assertNotNull( getMethod( FieldHelper.class, "getDouble", Object[].class ) );
  }

  @Test
  public void getGetSignature_Serializable() throws Exception {
    LogChannel log = mock( LogChannel.class );
    whenNew( LogChannel.class ).withAnyArguments().thenReturn( log );

    ValueMetaSerializable v = new ValueMetaSerializable( "Data" );
    String accessor = FieldHelper.getAccessor( true, "Data" );

    assertEquals( "Object Data = get(Fields.In, \"Data\").getObject(r);", FieldHelper.getGetSignature( accessor, v ) );
    assertNotNull( getMethod( FieldHelper.class, "getObject", Object[].class ) );
  }

  @Test
  public void getInetAddress_Test() throws Exception {
    ValueMetaInternetAddress v = new ValueMetaInternetAddress( "IP" );

    RowMetaInterface row = mock( RowMetaInterface.class );
    doReturn( v ).when( row ).searchValueMeta( anyString() );
    doReturn( 0 ).when( row ).indexOfValue( anyString() );

    assertEquals( InetAddress.getLoopbackAddress(),
      new FieldHelper( row, "IP" ).getInetAddress( new Object[] { InetAddress.getLoopbackAddress() } ) );
  }

  @Test
  public void getTimestamp_Test() throws Exception {
    ValueMetaTimestamp v = new ValueMetaTimestamp( "TS" );

    RowMetaInterface row = mock( RowMetaInterface.class );
    doReturn( v ).when( row ).searchValueMeta( anyString() );
    doReturn( 0 ).when( row ).indexOfValue( anyString() );

    assertEquals( Timestamp.valueOf( "2018-07-23 12:40:55" ),
      new FieldHelper( row, "TS" ).getTimestamp( new Object[] { Timestamp.valueOf( "2018-07-23 12:40:55" ) } ) );
  }

  @Test
  public void getSerializable_Test() throws Exception {
    ValueMetaSerializable v = new ValueMetaSerializable( "Data" );

    RowMetaInterface row = mock( RowMetaInterface.class );
    doReturn( v ).when( row ).searchValueMeta( anyString() );
    doReturn( 0 ).when( row ).indexOfValue( anyString() );

    assertEquals( "...",
      new FieldHelper( row, "Data" ).getSerializable( new Object[] { "..." } ) );
  }

  @Test
  public void getBinary_Test() throws Exception {
    ValueMetaBinary v = new ValueMetaBinary( "Data" );

    RowMetaInterface row = mock( RowMetaInterface.class );
    doReturn( v ).when( row ).searchValueMeta( anyString() );
    doReturn( 0 ).when( row ).indexOfValue( anyString() );

    assertArrayEquals( new byte[] { 0, 1, 2 },
      new FieldHelper( row, "Data" ).getBinary( new Object[] { new byte[] { 0, 1, 2 } } ) );
  }

  @Test
  public void setValue_String() {
    ValueMetaString v = new ValueMetaString( "Name" );

    RowMetaInterface row = mock( RowMetaInterface.class );
    doReturn( v ).when( row ).searchValueMeta( anyString() );
    doReturn( 0 ).when( row ).indexOfValue( anyString() );

    Object[] data = new Object[1];
    new FieldHelper( row, "Name" ).setValue( data, "Hitachi Vantara" );

    assertEquals( "Hitachi Vantara", data[0] );
  }

  @Test
  public void setValue_InetAddress() throws Exception {
    ValueMetaInternetAddress v = new ValueMetaInternetAddress( "IP" );

    RowMetaInterface row = mock( RowMetaInterface.class );
    doReturn( v ).when( row ).searchValueMeta( anyString() );
    doReturn( 0 ).when( row ).indexOfValue( anyString() );

    Object[] data = new Object[1];
    new FieldHelper( row, "IP" ).setValue( data, InetAddress.getLoopbackAddress() );

    assertEquals( InetAddress.getLoopbackAddress(), data[0] );
  }

  @Test
  public void setValue_ValueMetaBinary() throws Exception {
    ValueMetaBinary v = new ValueMetaBinary( "Data" );

    RowMetaInterface row = mock( RowMetaInterface.class );
    doReturn( v ).when( row ).searchValueMeta( anyString() );
    doReturn( 0 ).when( row ).indexOfValue( anyString() );

    Object[] data = new Object[1];
    new FieldHelper( row, "Data" ).setValue( data, new byte[] { 0, 1, 2 } );

    assertArrayEquals( new byte[] { 0, 1, 2 }, (byte[]) data[0] );
  }
}
