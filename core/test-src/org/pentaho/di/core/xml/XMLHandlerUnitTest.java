/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.xml;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.pentaho.di.core.Const;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.pentaho.di.core.xml.XMLHandler.buildCDATA;
import static org.pentaho.di.core.xml.XMLHandler.openTag;
import static org.pentaho.di.core.xml.XMLHandler.closeTag;
import static org.pentaho.di.core.xml.XMLHandler.addTagValue;

/**
 */
public class XMLHandlerUnitTest {
  private static final String cr = Const.CR;

  @Test
  public void openTagWithNotNull() {
    assertEquals( "<qwerty>", openTag( "qwerty" ) );
  }

  @Test
  public void openTagWithNull() {
    assertEquals( "<null>", openTag( null ) );
  }

  @Test
  public void openTagWithExternalBuilder() {
    StringBuilder builder = new StringBuilder( "qwe" );
    openTag( builder, "rty" );
    assertEquals( "qwe<rty>", builder.toString() );
  }

  @Test
  public void closeTagWithNotNull() {
    assertEquals( "</qwerty>", closeTag( "qwerty" ) );
  }

  @Test
  public void closeTagWithNull() {
    assertEquals( "</null>", closeTag( null ) );
  }

  @Test
  public void closeTagWithExternalBuilder() {
    StringBuilder builder = new StringBuilder( "qwe" );
    closeTag( builder, "rty" );
    assertEquals( "qwe</rty>", builder.toString() );
  }

  @Test
  public void buildCdataWithNotNull() {
    assertEquals( "<![CDATA[qwerty]]>", buildCDATA( "qwerty" ) );
  }

  @Test
  public void buildCdataWithNull() {
    assertEquals( "<![CDATA[]]>", buildCDATA( null ) );
  }

  @Test
  public void buildCdataWithExternalBuilder() {
    StringBuilder builder = new StringBuilder( "qwe" );
    buildCDATA( builder, "rty" );
    assertEquals( "qwe<![CDATA[rty]]>", builder.toString() );
  }

  @Test
  public void timestamp2stringTest() {
    String actual = XMLHandler.timestamp2string( null );
    assertNull( actual );
  }

  @Test
  public void date2stringTest() {
    String actual = XMLHandler.date2string( null );
    assertNull( actual );
  }

  public void addTagValueBigDecimal() {
    BigDecimal input = new BigDecimal( "1234567890123456789.01" );
    assertEquals( "<bigdec>1234567890123456789.01</bigdec>" + cr, addTagValue( "bigdec",  input ) );
    assertEquals( "<bigdec>1234567890123456789.01</bigdec>" + cr, addTagValue( "bigdec", input, true ) );
    assertEquals( "<bigdec>1234567890123456789.01</bigdec>", addTagValue( "bigdec", input, false ) );
  }

  @Test
  public void addTagValueBoolean() {
    assertEquals( "<abool>Y</abool>" + cr, addTagValue( "abool", true ) );
    assertEquals( "<abool>Y</abool>" + cr, addTagValue( "abool", true, true ) );
    assertEquals( "<abool>Y</abool>", addTagValue( "abool", true, false ) );
    assertEquals( "<abool>N</abool>" + cr, addTagValue( "abool", false ) );
    assertEquals( "<abool>N</abool>" + cr, addTagValue( "abool", false, true ) );
    assertEquals( "<abool>N</abool>", addTagValue( "abool", false, false ) );
  }

  @Test
  public void addTagValueDate() {
    String result = "2014&#x2f;12&#x2f;29 15&#x3a;59&#x3a;45.789";
    Calendar aDate = new GregorianCalendar();
    aDate.set( 2014, ( 12 - 1 ) , 29, 15, 59, 45 );
    aDate.set( Calendar.MILLISECOND, 789 );

    assertEquals( "<adate>" + result + "</adate>" + cr, addTagValue( "adate", aDate.getTime() ) );
    assertEquals( "<adate>" + result + "</adate>" + cr, addTagValue( "adate", aDate.getTime(), true ) );
    assertEquals( "<adate>" + result + "</adate>", addTagValue( "adate", aDate.getTime(), false ) );
  }

  @Test
  public void addTagValueLong() {
    long input = 123;
    assertEquals( "<along>123</along>" + cr, addTagValue( "along", input ) );
    assertEquals( "<along>123</along>" + cr, addTagValue( "along", input, true ) );
    assertEquals( "<along>123</along>", addTagValue( "along", input, false ) );

    assertEquals( "<along>" + String.valueOf( Long.MAX_VALUE ) + "</along>", addTagValue( "along", Long.MAX_VALUE, false ) );
    assertEquals( "<along>" + String.valueOf( Long.MIN_VALUE ) + "</along>", addTagValue( "along", Long.MIN_VALUE, false ) );
  }

  @Test
  public void addTagValueInt() {
    int input = 456;
    assertEquals( "<anint>456</anint>" + cr, addTagValue( "anint", input ) );
    assertEquals( "<anint>456</anint>" + cr, addTagValue( "anint", input, true ) );
    assertEquals( "<anint>456</anint>", addTagValue( "anint", input, false ) );

    assertEquals( "<anint>" + String.valueOf( Integer.MAX_VALUE ) + "</anint>", addTagValue( "anint", Integer.MAX_VALUE, false ) );
    assertEquals( "<anint>" + String.valueOf( Integer.MIN_VALUE ) + "</anint>", addTagValue( "anint", Integer.MIN_VALUE, false ) );
  }

  @Test
  public void addTagValueDouble() {
    double input = 123.45;
    assertEquals( "<adouble>123.45</adouble>" + cr, addTagValue( "adouble", input ) );
    assertEquals( "<adouble>123.45</adouble>" + cr, addTagValue( "adouble", input, true ) );
    assertEquals( "<adouble>123.45</adouble>", addTagValue( "adouble", input, false ) );

    assertEquals( "<adouble>" + String.valueOf( Double.MAX_VALUE ) + "</adouble>", addTagValue( "adouble", Double.MAX_VALUE, false ) );
    assertEquals( "<adouble>" + String.valueOf( Double.MIN_VALUE ) + "</adouble>", addTagValue( "adouble", Double.MIN_VALUE, false ) );
    assertEquals( "<adouble>" + String.valueOf( Double.MIN_NORMAL ) + "</adouble>", addTagValue( "adouble", Double.MIN_NORMAL, false ) );
  }

  @Test
  public void addTagValueBinary() throws IOException {
    byte[] input = new String( "Test Data" ).getBytes();
    String result = "H4sIAAAAAAAAAAtJLS5RcEksSQQAL4PL8QkAAAA&#x3d;";

    assertEquals( "<bytedata>" + result + "</bytedata>" + cr, addTagValue( "bytedata", input ) );
    assertEquals( "<bytedata>" + result + "</bytedata>" + cr, addTagValue( "bytedata", input, true ) );
    assertEquals( "<bytedata>" + result + "</bytedata>", addTagValue( "bytedata", input, false ) );
  }
}
