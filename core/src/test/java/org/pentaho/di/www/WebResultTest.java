/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.www;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleXMLException;

public class WebResultTest {

  @Test
  public void testStatics() {
    assertEquals( "webresult", WebResult.XML_TAG );
    assertEquals( "OK", WebResult.STRING_OK );
    assertEquals( "ERROR", WebResult.STRING_ERROR );
    assertNotNull( WebResult.OK );
    assertEquals( "OK", WebResult.OK.getResult() );
    assertNull( WebResult.OK.getMessage() );
    assertNull( WebResult.OK.getId() );
  }

  @Test
  public void testConstructors() {
    String expectedResult = UUID.randomUUID().toString();
    WebResult result = new WebResult( expectedResult );
    assertEquals( expectedResult, result.getResult() );

    String expectedMessage = UUID.randomUUID().toString();
    result = new WebResult( expectedResult, expectedMessage );
    assertEquals( expectedResult, result.getResult() );
    assertEquals( expectedMessage, result.getMessage() );

    String expectedId = UUID.randomUUID().toString();
    result = new WebResult( expectedResult, expectedMessage, expectedId );
    assertEquals( expectedResult, result.getResult() );
    assertEquals( expectedMessage, result.getMessage() );
    assertEquals( expectedId, result.getId() );
  }

  @Test
  public void testSerialization() throws KettleXMLException {
    WebResult original = new WebResult( UUID.randomUUID().toString(), UUID.randomUUID().toString(),
      UUID.randomUUID().toString() );

    String xml = original.getXML();
    WebResult copy = WebResult.fromXMLString( xml );

    assertNotNull( copy );
    assertNotSame( original, copy );
    assertEquals( original.getResult(), copy.getResult() );
    assertEquals( original.getMessage(), copy.getMessage() );
    assertEquals( original.getId(), copy.getId() );
  }

  @Test
  public void testSetters() {
    WebResult result = new WebResult( "" );
    assertEquals( "", result.getResult() );

    result.setMessage( "fakeMessage" );
    assertEquals( "fakeMessage", result.getMessage() );
    result.setResult( "fakeResult" );
    assertEquals( "fakeResult", result.getResult() );
    result.setId( "fakeId" );
    assertEquals( "fakeId", result.getId() );
  }
}
