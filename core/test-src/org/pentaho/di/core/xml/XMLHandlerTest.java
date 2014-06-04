/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

/**
 * Test class for the basic functionality of XMLHandler.
 *
 * @author Matt Tucker
 */
public class XMLHandlerTest extends TestCase {

  private String cr;
  private BigDecimal bigdec;
  private GregorianCalendar aDate;

  protected void setUp() {
    cr = System.getProperty( "line.separator" );

    bigdec = new BigDecimal( "1234567890123456789.01" );

    aDate = new GregorianCalendar();
    aDate.set( 2014, ( 12 - 1 ) , 29, 15, 59, 45 );
    aDate.set( Calendar.MILLISECOND, 789 );
  }

  private String encodeDate( String data ) {
    return data.replace( "/" , "&#x2f;" ).replace( ":", "&#x3a;" );
  }

  public void testopenTag() {
    assertEquals( "<a>", XMLHandler.openTag( "a"  ) );
  }

  public void testcloseTag() {
    assertEquals( "</b>", XMLHandler.closeTag( "b" ) );
  }

  public void testaddTagValue() {
    assertEquals( "<bigdec>1234567890123456789.01</bigdec>" + cr, XMLHandler.addTagValue( "bigdec",  bigdec ) );
    assertEquals( "<bigdec>1234567890123456789.01</bigdec>" + cr, XMLHandler.addTagValue( "bigdec", bigdec, true ) );
    assertEquals( "<bigdec>1234567890123456789.01</bigdec>", XMLHandler.addTagValue( "bigdec", bigdec, false ) );

    assertEquals( "<abool>Y</abool>" + cr, XMLHandler.addTagValue( "abool", true ) );
    assertEquals( "<abool>Y</abool>" + cr, XMLHandler.addTagValue( "abool", true, true ) );
    assertEquals( "<abool>Y</abool>", XMLHandler.addTagValue( "abool", true, false ) );
    assertEquals( "<abool>N</abool>" + cr, XMLHandler.addTagValue( "abool", false ) );
    assertEquals( "<abool>N</abool>" + cr, XMLHandler.addTagValue( "abool", false, true ) );
    assertEquals( "<abool>N</abool>", XMLHandler.addTagValue( "abool", false, false ) );

    assertEquals( "<adate>" + encodeDate( "2014/12/29 15:59:45.789" ) + "</adate>" + cr, XMLHandler.addTagValue( "adate", aDate.getTime() ) );
    assertEquals( "<adate>" + encodeDate( "2014/12/29 15:59:45.789" ) + "</adate>" + cr, XMLHandler.addTagValue( "adate", aDate.getTime(), true ) );
    assertEquals( "<adate>" + encodeDate( "2014/12/29 15:59:45.789" ) + "</adate>", XMLHandler.addTagValue( "adate", aDate.getTime(), false ) );
  }
}
