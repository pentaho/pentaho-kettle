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

import junit.framework.TestCase;

/**
 * Test class for the basic functionality of ValueNumber.
 *
 * @author Sven Boden
 */
public class ValueNumberTest extends TestCase {
  /**
   * Constructor test 1.
   */
  public void testConstructor1() {
    ValueNumber vs = new ValueNumber();

    assertEquals( Value.VALUE_TYPE_NUMBER, vs.getType() );
    assertEquals( "Number", vs.getTypeDesc() );
    assertEquals( 0.0D, vs.getNumber(), 0.001D );
    assertEquals( -1, vs.getLength() );
    assertEquals( -1, vs.getPrecision() );

    ValueNumber vs1 = new ValueNumber( 10.0D );

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
   * Test the getters of ValueNumber
   */
  public void testGetters() {
    ValueNumber vs1 = new ValueNumber( -4.0D );
    ValueNumber vs2 = new ValueNumber( 0.0D );
    ValueNumber vs3 = new ValueNumber( 3.0D );
    ValueNumber vs4 = new ValueNumber( 3.5D );

    assertEquals( true, vs1.getBoolean() );
    assertEquals( false, vs2.getBoolean() );
    assertEquals( true, vs3.getBoolean() );

    assertEquals( "-4.0", vs1.getString() );
    assertEquals( "0.0", vs2.getString() );
    assertEquals( "3.0", vs3.getString() );

    assertEquals( -4.0D, vs1.getNumber(), 0.001D );
    assertEquals( 0.0D, vs2.getNumber(), 0.001D );
    assertEquals( 3.0D, vs3.getNumber(), 0.001D );

    assertEquals( -4L, vs1.getInteger() );
    assertEquals( 0L, vs2.getInteger() );
    assertEquals( 3L, vs3.getInteger() );
    assertEquals( 4L, vs4.getInteger() );

    assertEquals( BigDecimal.valueOf( -4.0D ), vs1.getBigNumber() );
    assertEquals( BigDecimal.valueOf( 0.0D ), vs2.getBigNumber() );
    assertEquals( BigDecimal.valueOf( 3.0D ), vs3.getBigNumber() );
    assertEquals( BigDecimal.valueOf( 3.5D ), vs4.getBigNumber() );

    assertEquals( -4L, vs1.getDate().getTime() );
    assertEquals( 0L, vs2.getDate().getTime() );
    assertEquals( 3L, vs3.getDate().getTime() );
    assertEquals( 3L, vs4.getDate().getTime() );

    assertEquals( new Double( -4.0D ), vs1.getSerializable() );
    assertEquals( new Double( 0.0D ), vs2.getSerializable() );
    assertEquals( new Double( 3.0D ), vs3.getSerializable() );
  }

  /**
   * Test the setters of ValueNumber
   */
  public void testSetters() {
    ValueNumber vs = new ValueNumber( 0.0D );

    vs.setString( "unknown" );
    assertEquals( 0.0D, vs.getNumber(), 0.001D );
    vs.setString( "-4.0" );
    assertEquals( -4.0D, vs.getNumber(), 0.001D );
    vs.setString( "0.0" );
    assertEquals( 0.0D, vs.getNumber(), 0.001D );
    vs.setString( "0" );
    assertEquals( 0.0D, vs.getNumber(), 0.001D );
    vs.setString( "3.0" );

    SimpleDateFormat format = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss.SSS", Locale.US );
    Date dt = null;
    try {
      dt = format.parse( "2006/06/07 01:02:03.004" );
    } catch ( ParseException ex ) {
      dt = null;
    }
    vs.setDate( dt );
    assertEquals( 1.149634923004E12, vs.getNumber(), 0.0005E12D );

    vs.setBoolean( true );
    assertEquals( 1.0D, vs.getNumber(), 0.1D );
    vs.setBoolean( false );
    assertEquals( 0.0D, vs.getNumber(), 0.1D );

    vs.setNumber( 5.0D );
    assertEquals( 5.0D, vs.getNumber(), 0.1D );
    vs.setNumber( 0.0D );
    assertEquals( 0.0D, vs.getNumber(), 0.1D );

    vs.setInteger( 5L );
    assertEquals( 5.0D, vs.getNumber(), 0.1D );
    vs.setInteger( 0L );
    assertEquals( 0.0D, vs.getNumber(), 0.1D );

    vs.setBigNumber( new BigDecimal( 5 ) );
    assertEquals( 5.0D, vs.getNumber(), 0.1D );
    vs.setBigNumber( new BigDecimal( 0 ) );
    assertEquals( 0.0D, vs.getNumber(), 0.1D );

    // setSerializable is ignored ???
  }
}
