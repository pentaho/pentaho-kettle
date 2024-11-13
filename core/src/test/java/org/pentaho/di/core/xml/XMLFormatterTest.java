/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.core.xml;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import org.custommonkey.xmlunit.XMLUnit;

public class XMLFormatterTest {

  @BeforeClass
  public static void setupClass() {
    XMLUnit.setIgnoreWhitespace( true );
  }

  @Test
  public void test1() throws Exception {
    String inXml, expectedXml;
    try ( InputStream in = XMLFormatterTest.class.getResourceAsStream( "XMLFormatterIn1.xml" ) ) {
      inXml = IOUtils.toString( in );
    }
    try ( InputStream in = XMLFormatterTest.class.getResourceAsStream( "XMLFormatterExpected1.xml" ) ) {
      expectedXml = IOUtils.toString( in );
    }

    String result = XMLFormatter.format( inXml );
    assertXMLEqual( expectedXml, result );
  }

  @Test
  public void test2() throws Exception {
    String inXml, expectedXml;
    try ( InputStream in = XMLFormatterTest.class.getResourceAsStream( "XMLFormatterIn2.xml" ) ) {
      inXml = IOUtils.toString( in );
    }
    try ( InputStream in = XMLFormatterTest.class.getResourceAsStream( "XMLFormatterExpected2.xml" ) ) {
      expectedXml = IOUtils.toString( in );
    }

    String result = XMLFormatter.format( inXml );
    assertXMLEqual( expectedXml, result );
  }

  @Test
  public void test3() throws Exception {
    String inXml, expectedXml;
    try ( InputStream in = XMLFormatterTest.class.getResourceAsStream( "XMLFormatterIn3cdata.xml" ) ) {
      inXml = IOUtils.toString( in );
    }
    try ( InputStream in = XMLFormatterTest.class.getResourceAsStream( "XMLFormatterExpected3cdata.xml" ) ) {
      expectedXml = IOUtils.toString( in );
    }

    String result = XMLFormatter.format( inXml );
    assertXMLEqual( expectedXml, result );
  }

  @Test
  public void test4() throws Exception {
    String inXml, expectedXml;
    try ( InputStream in = XMLFormatterTest.class.getResourceAsStream( "XMLFormatterIn4multilinecdata.xml" ) ) {
      inXml = IOUtils.toString( in );
    }
    try ( InputStream in = XMLFormatterTest.class.getResourceAsStream( "XMLFormatterExpected4multilinecdata.xml" ) ) {
      expectedXml = IOUtils.toString( in );
    }

    String result = XMLFormatter.format( inXml );
    assertXMLEqual( expectedXml, result );
  }
}
