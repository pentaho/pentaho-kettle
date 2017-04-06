/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016-2017 by Pentaho : http://www.pentaho.com
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

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class XMLFormatterTest {
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
    assertEquals( expectedXml, result );
  }

  @Test
  public void testcdata() throws Exception {
    String input =
      "    <annotations>"
        + "  <annotation>"
        + "    <name>c44cc4ed-9647-44f2-b1d9-0523c01f0c94</name>\n"
        + "    <field>HiddenField</field>\n"
        + "    <type>CREATE_ATTRIBUTE</type>\n"
        + "     <properties>"
        + "        <property>"
        + "           <name>hidden</name>\n"
        + "           <value><![CDATA[true]]></value>"
        + "        </property>"
        + "    </properties>      "
        + "   </annotation>"
        + "  <sharedDimension>N</sharedDimension>\n"
        + "  <description/>\n"
        + "</annotations>";
    String result = XMLFormatter.format( input );
    String expected =
      "<annotations>\n"
        + "  <annotation>\n"
        + "    <name>c44cc4ed-9647-44f2-b1d9-0523c01f0c94</name>\n"
        + "    <field>HiddenField</field>\n"
        + "    <type>CREATE_ATTRIBUTE</type>\n"
        + "    <properties>\n"
        + "      <property>\n"
        + "        <name>hidden</name>\n"
        + "        <value><![CDATA[true]]></value>\n"
        + "      </property>\n"
        + "    </properties>\n"
        + "  </annotation>\n"
        + "  <sharedDimension>N</sharedDimension>\n"
        + "  <description/>\n"
        + "</annotations>\n";
    assertEquals( expected, result );
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
    assertEquals( expectedXml, result );
  }
}
