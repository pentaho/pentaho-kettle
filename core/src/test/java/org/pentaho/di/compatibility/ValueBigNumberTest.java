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
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.TestCase;

/**
 * Test class for the basic functionality of ValueNumber.
 *
 * @author Sven Boden
 */
public class ValueBigNumberTest extends TestCase {
  /**
   * Constructor test 1.
   */
  public void testConstructor1() {
    ValueBigNumber vs = new ValueBigNumber();

    assertEquals( Value.VALUE_TYPE_BIGNUMBER, vs.getType() );
    assertEquals( "BigNumber", vs.getTypeDesc() );
    assertNull( vs.getBigNumber() );
    assertEquals( -1, vs.getLength() );
    assertEquals( -1, vs.getPrecision() );

    ValueBigNumber vs1 = new ValueBigNumber( BigDecimal.ONE );

    vs1.setLength( 2 );
    assertEquals( 2, vs1.getLength() );
    assertEquals( -1, vs1.getPrecision() );

    vs1.setLength( 4, 2 );
    assertEquals( 4, vs1.getLength() );
    assertEquals( 2, vs1.getPrecision() );

    vs1.setPrecision( 3 );
    assertEquals( 3, vs1.getPrecision() );
  }

  /**
   * Test the getters of ValueBigNumber
   */
  public void testGetters() {
    ValueBigNumber vs1 = new ValueBigNumber();
    ValueBigNumber vs2 = new ValueBigNumber( BigDecimal.ZERO );
    ValueBigNumber vs3 = new ValueBigNumber( BigDecimal.ONE );

    assertEquals( false, vs1.getBoolean() );
    assertEquals( false, vs2.getBoolean() );
    assertEquals( true, vs3.getBoolean() );

    assertEquals( null, vs1.getString() );
    assertEquals( "0", vs2.getString() );
    assertEquals( "1", vs3.getString() );

    assertEquals( 0.0D, vs1.getNumber(), 0.001D );
    assertEquals( 0.0D, vs2.getNumber(), 0.001D );
    assertEquals( 1.0D, vs3.getNumber(), 0.001D );

    assertEquals( 0L, vs1.getInteger() );
    assertEquals( 0L, vs2.getInteger() );
    assertEquals( 1L, vs3.getInteger() );

    assertNull( vs1.getBigNumber() );
    assertEquals( new BigDecimal( 0L ), vs2.getBigNumber() );
    assertEquals( new BigDecimal( 1L ), vs3.getBigNumber() );

    assertNull( vs1.getDate() );
    assertEquals( 0L, vs2.getDate().getTime() );
    assertEquals( 1L, vs3.getDate().getTime() );

    assertNull( vs1.getSerializable() );
    assertEquals( BigDecimal.ZERO, vs2.getSerializable() );
    assertEquals( BigDecimal.ONE, vs3.getSerializable() );
  }

  /**
   * Test the setters of ValueBigNumber
   */
  public void testSetters() {
    TimeZone.setDefault( TimeZone.getTimeZone( "CET" ) );

    ValueBigNumber vs = new ValueBigNumber();

    vs.setString( "unknown" );
    assertEquals( BigDecimal.ZERO, vs.getBigNumber() );
    vs.setString( "-4.0" );
    assertEquals( BigDecimal.valueOf( -4.0D ), vs.getBigNumber() );
    vs.setString( "0.0" );
    assertEquals( BigDecimal.valueOf( 0.0D ), vs.getBigNumber() );
    vs.setString( "0" );
    assertEquals( BigDecimal.ZERO, vs.getBigNumber() );

    SimpleDateFormat format = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss.SSS", Locale.US );
    Date dt = null;
    try {
      dt = format.parse( "2006/06/07 01:02:03.004" );
    } catch ( ParseException ex ) {
      dt = null;
    }
    vs.setDate( dt );

    assertEquals( new BigDecimal( "1149634923004" ), vs.getBigNumber() );

    vs.setBoolean( true );
    assertEquals( BigDecimal.ONE, vs.getBigNumber() );
    vs.setBoolean( false );
    assertEquals( BigDecimal.ZERO, vs.getBigNumber() );

    vs.setNumber( 5.0D );
    assertEquals( BigDecimal.valueOf( 5.0D ), vs.getBigNumber() );
    vs.setNumber( 0.0D );
    assertEquals( BigDecimal.valueOf( 0.0D ), vs.getBigNumber() );

    vs.setInteger( 5L );
    assertEquals( BigDecimal.valueOf( 5L ), vs.getBigNumber() );
    vs.setInteger( 0L );
    assertEquals( BigDecimal.ZERO, vs.getBigNumber() );

    vs.setBigNumber( new BigDecimal( 5 ) );
    assertEquals( 5.0D, vs.getNumber(), 0.1D );
    vs.setBigNumber( new BigDecimal( 0 ) );
    assertEquals( 0.0D, vs.getNumber(), 0.1D );

    // setSerializable is ignored ???
  }
}
