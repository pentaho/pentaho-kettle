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

import junit.framework.TestCase;

/**
 * Test class for the basic functionality of ValueInteger.
 *
 * @author Sven Boden
 */
public class ValueIntegerTest extends TestCase {
  /**
   * Constructor test 1.
   */
  public void testConstructor1() {
    ValueInteger vs = new ValueInteger();

    assertEquals( Value.VALUE_TYPE_INTEGER, vs.getType() );
    assertEquals( "Integer", vs.getTypeDesc() );
    assertEquals( 0, vs.getInteger() );
    assertEquals( -1, vs.getLength() );
    assertEquals( 0, vs.getPrecision() );

    // Precision is ignored
    ValueInteger vs1 = new ValueInteger( 10 );

    vs1.setLength( 2 );
    assertEquals( 2, vs1.getLength() );
    assertEquals( 0, vs1.getPrecision() );

    vs1.setLength( 4, 2 );
    assertEquals( 4, vs1.getLength() );
    assertEquals( 0, vs1.getPrecision() );

    vs1.setPrecision( 3 );
    assertEquals( 0, vs1.getPrecision() );
  }

  /**
   * Test the getters of ValueInteger
   */
  public void testGetters() {
    ValueInteger vs1 = new ValueInteger( -4 );
    ValueInteger vs2 = new ValueInteger( 0 );
    ValueInteger vs3 = new ValueInteger( 3 );

    assertEquals( true, vs1.getBoolean() );
    assertEquals( false, vs2.getBoolean() );
    assertEquals( true, vs3.getBoolean() );

    assertEquals( "-4", vs1.getString() );
    assertEquals( "0", vs2.getString() );
    assertEquals( "3", vs3.getString() );

    assertEquals( -4.0D, vs1.getNumber(), 0.001D );
    assertEquals( 0.0D, vs2.getNumber(), 0.001D );
    assertEquals( 3.0D, vs3.getNumber(), 0.001D );

    assertEquals( -4L, vs1.getInteger() );
    assertEquals( 0L, vs2.getInteger() );
    assertEquals( 3L, vs3.getInteger() );

    assertEquals( new BigDecimal( -4L ), vs1.getBigNumber() );
    assertEquals( new BigDecimal( 0L ), vs2.getBigNumber() );
    assertEquals( new BigDecimal( 3L ), vs3.getBigNumber() );

    assertEquals( -4L, vs1.getDate().getTime() );
    assertEquals( 0L, vs2.getDate().getTime() );
    assertEquals( 3L, vs3.getDate().getTime() );

    assertEquals( new Long( -4L ), vs1.getSerializable() );
    assertEquals( new Long( 0L ), vs2.getSerializable() );
    assertEquals( new Long( 3L ), vs3.getSerializable() );
  }

  /**
   * Test the setters of ValueInteger
   */
  public void testSetters() {
    ValueInteger vs = new ValueInteger( 0 );

    vs.setString( "unknown" );
    assertEquals( 0, vs.getInteger() );
    vs.setString( "-4.0" );
    assertEquals( 0, vs.getInteger() );
    vs.setString( "-4" );
    assertEquals( -4, vs.getInteger() );
    vs.setString( "0" );
    assertEquals( 0, vs.getInteger() );
    vs.setString( "3" );
    assertEquals( 3, vs.getInteger() );

    SimpleDateFormat format = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss.SSS z" );
    Date dt = null;
    try {
      dt = format.parse( "2006/06/07 01:02:03.004 CET" );
    } catch ( ParseException ex ) {
      dt = null;
    }
    vs.setDate( dt );

    // Epoch time conversion
    assertEquals( 1149638523004L, vs.getInteger() );

    vs.setBoolean( true );
    assertEquals( 1, vs.getInteger() );
    vs.setBoolean( false );
    assertEquals( 0, vs.getInteger() );

    vs.setNumber( 5 );
    assertEquals( 5, vs.getInteger() );
    vs.setNumber( 0 );
    assertEquals( 0, vs.getInteger() );

    vs.setInteger( 5L );
    assertEquals( 5, vs.getInteger() );
    vs.setInteger( 0L );
    assertEquals( 0, vs.getInteger() );

    vs.setBigNumber( new BigDecimal( 5 ) );
    assertEquals( 5, vs.getInteger() );
    vs.setBigNumber( new BigDecimal( 0 ) );
    assertEquals( 0, vs.getInteger() );

    // setSerializable is ignored ???
  }
}
