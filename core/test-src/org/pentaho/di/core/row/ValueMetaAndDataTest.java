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

package org.pentaho.di.core.row;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.value.ValueMetaString;

public class ValueMetaAndDataTest {

  @Test
  public void testConstructors() throws KettleValueException {
    ValueMetaAndData result;

    result = new ValueMetaAndData( new ValueMetaString( "ValueStringName" ), "testValue1" );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_STRING, result.getValueMeta().getType() );
    assertEquals( "ValueStringName", result.getValueMeta().getName() );
    assertEquals( "testValue1", result.getValueData() );

    result = new ValueMetaAndData( "StringName", "testValue2" );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_STRING, result.getValueMeta().getType() );
    assertEquals( "StringName", result.getValueMeta().getName() );
    assertEquals( "testValue2", result.getValueData() );

    result = new ValueMetaAndData( "NumberName", Double.valueOf( "123.45" ) );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_NUMBER, result.getValueMeta().getType() );
    assertEquals( "NumberName", result.getValueMeta().getName() );
    assertEquals( Double.valueOf( "123.45" ), result.getValueData() );

    result = new ValueMetaAndData( "IntegerName", Long.valueOf( 234 ) );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, result.getValueMeta().getType() );
    assertEquals( "IntegerName", result.getValueMeta().getName() );
    assertEquals( Long.valueOf( 234 ), result.getValueData() );

    Date testDate = Calendar.getInstance().getTime();
    result = new ValueMetaAndData( "DateName", testDate );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_DATE, result.getValueMeta().getType() );
    assertEquals( "DateName", result.getValueMeta().getName() );
    assertEquals( testDate, result.getValueData() );

    result = new ValueMetaAndData( "BigNumberName", new BigDecimal( "123456789.987654321" ) );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_BIGNUMBER, result.getValueMeta().getType() );
    assertEquals( "BigNumberName", result.getValueMeta().getName() );
    assertEquals( new BigDecimal( "123456789.987654321" ), result.getValueData() );

    result = new ValueMetaAndData( "BooleanName", Boolean.TRUE );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_BOOLEAN, result.getValueMeta().getType() );
    assertEquals( "BooleanName", result.getValueMeta().getName() );
    assertEquals( Boolean.TRUE, result.getValueData() );

    byte[] testBytes = new byte[50];
    new Random().nextBytes( testBytes );
    result = new ValueMetaAndData( "BinaryName", testBytes );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_BINARY, result.getValueMeta().getType() );
    assertEquals( "BinaryName", result.getValueMeta().getName() );
    assertArrayEquals( testBytes, (byte[]) result.getValueData() );

    result = new ValueMetaAndData( "SerializableName", new StringBuilder( "serializable test" ) );
    assertNotNull( result );
    assertEquals( ValueMetaInterface.TYPE_SERIALIZABLE, result.getValueMeta().getType() );
    assertEquals( "SerializableName", result.getValueMeta().getName() );
    assertTrue( result.getValueData() instanceof StringBuilder );
    assertEquals( "serializable test", result.getValueData().toString() );

  }
}
