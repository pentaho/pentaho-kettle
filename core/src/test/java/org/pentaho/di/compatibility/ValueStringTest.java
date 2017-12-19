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

package org.pentaho.di.compatibility;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * Test class for the basic functionality of ValueString.
 *
 * @author Sven Boden
 */
public class ValueStringTest extends TestCase {
  /**
   * Constructor test 1.
   */
  public void testConstructor1() {
    ValueString vs = new ValueString();

    assertEquals( Value.VALUE_TYPE_STRING, vs.getType() );
    assertEquals( "String", vs.getTypeDesc() );
    assertEquals( -1, vs.getLength() );
    assertEquals( -1, vs.getPrecision() );

    ValueString vs1 = new ValueString( "Boden Test" );

    assertEquals( Value.VALUE_TYPE_STRING, vs1.getType() );
    assertEquals( "String", vs1.getTypeDesc() );
    // is the length of the field, not the length of the value
    assertEquals( -1, vs1.getLength() );
    assertEquals( -1, vs1.getPrecision() );

    ValueString vs2 = new ValueString();

    // precision is ignored
    vs2.setPrecision( 2 );
    assertEquals( -1, vs2.getPrecision() );
    vs2.setLength( 10 );
    assertEquals( 10, vs2.getLength() );
  }

  /**
   * Set the value to null and see what comes out on conversions.
   */
  public void testGetNullValue() {
    ValueString vs = new ValueString();

    assertNull( vs.getString() );
    assertEquals( 0.0D, vs.getNumber(), 0.0D );
    assertNull( vs.getDate() );
    assertEquals( false, vs.getBoolean() );
    assertEquals( 0, vs.getInteger() );
    assertEquals( null, vs.getBigNumber() );
    assertNull( vs.getSerializable() );
  }

  /**
   * Set the value to an integer number and see what comes out on conversions.
   */
  public void testGetNumericValue1() {
    ValueString vs = new ValueString( "1000" );

    assertEquals( "1000", vs.getString() );
    assertEquals( 1000.0D, vs.getNumber(), 0.0D );
    assertNull( vs.getDate() ); // will fail parsing
    assertEquals( false, vs.getBoolean() );
    assertEquals( 1000, vs.getInteger() );
    assertEquals( BigDecimal.valueOf( 1000 ), vs.getBigNumber() );
  }

  /**
   * Set the value to an "float" number and see what comes out on conversions.
   */
  public void testGetNumericValue2() {
    ValueString vs = new ValueString( "2.8" );

    assertEquals( "2.8", vs.getString() );
    assertEquals( 2.8D, vs.getNumber(), 0.0D );
    assertNull( vs.getDate() ); // will fail parsing
    assertEquals( false, vs.getBoolean() );
    assertEquals( 0, vs.getInteger() );
    assertEquals( 2.8D, vs.getBigNumber().doubleValue(), 0.1D );
  }

  /**
   * Set the value to a non numeric string.
   */
  public void testGetString() {
    ValueString vs = new ValueString( "Boden" );

    assertEquals( "Boden", vs.getString() );
    assertEquals( 0.0, vs.getNumber(), 0.0D );
    assertNull( vs.getDate() ); // will fail parsing
    assertEquals( false, vs.getBoolean() );
    assertEquals( 0, vs.getInteger() );
    try {
      vs.getBigNumber();
      fail( "Expected a NumberFormatException" );
    } catch ( NumberFormatException ex ) {
      vs = null;
    }
  }

  /**
   * Test setting a string.
   */
  public void testSetString() {
    ValueString vs = new ValueString();

    vs.setString( null );
    assertNull( vs.getString() );
    vs.setString( "" );
    assertEquals( "", vs.getString() );
    vs.setString( "Boden" );
    assertEquals( "Boden", vs.getString() );
  }

  /**
   * Test setting a number.
   *
   */
  public void testSetNumber() {
    ValueString vs = new ValueString();

    vs.setNumber( 0 );
    assertEquals( "0.0", vs.getString() );
    vs.setNumber( 1 );
    assertEquals( "1.0", vs.getString() );
    vs.setNumber( -1 );
    assertEquals( "-1.0", vs.getString() );
    vs.setNumber( 2.5 );
    assertEquals( "2.5", vs.getString() );
    vs.setNumber( 2.8 );
    assertEquals( "2.8", vs.getString() );
  }

  /**
   * Test dates in ValueString
   */
  public void testSetDate() throws ParseException {
    ValueString vs = new ValueString();
    SimpleDateFormat format = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss.SSS", Locale.US );

    try {
      vs.setDate( null );
      // assertNull(vs.getString());
      fail( "expected NullPointerException" );
    } catch ( NullPointerException ex ) {
      // This is the original behaviour
    }
    vs.setDate( format.parse( "2006/06/07 01:02:03.004" ) );
    assertEquals( "2006/06/07 01:02:03.004", vs.getString() );
  }

  /**
   * Test booleans in ValueString
   */
  public void testSetBoolean() {
    ValueString vs = new ValueString();

    vs.setBoolean( false );
    assertEquals( "N", vs.getString() );
    vs.setBoolean( true );
    assertEquals( "Y", vs.getString() );
  }

  public void testSetInteger() {
    ValueString vs = new ValueString();

    vs.setInteger( -1L );
    assertEquals( "-1", vs.getString() );
    vs.setInteger( 0L );
    assertEquals( "0", vs.getString() );
    vs.setInteger( 1L );
    assertEquals( "1", vs.getString() );
  }

  public void testSetBigNumber() {
    ValueString vs = new ValueString();

    try {
      vs.setBigNumber( null );
      // assertNull(vs.getString());
      fail( "expected NullPointerException" );
    } catch ( NullPointerException ex ) {
      // This is the original behaviour
    }

    vs.setBigNumber( BigDecimal.ZERO );
    assertEquals( "0", vs.getString() );
  }

  public void testClone() {
    ValueString vs = new ValueString( "Boden" );

    ValueString vs1 = (ValueString) vs.clone();
    assertFalse( vs.equals( vs1 ) ); // not the same object, equals not implement
    assertTrue( vs != vs1 ); // not the same object
    assertEquals( vs.getString(), vs1.getString() );
  }
}
