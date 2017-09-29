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
        + "  <description />\n"
        + "</annotations>\n";
    assertEquals( expected, result );
  }

  @Test
  public void testMultilineCData() {
    String original = "" +
            "<class_source>\n" +
            "    <![CDATA[" +
            " import java.util.*;\n" +
            "\n" +
            "private int yearIndex;\n" +
            "private Calendar calendar;\n" +
            "\n" +
            "public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException \n" +
            "{\n" +
            "  Object[] r=getRow();\n" +
            "  if (r==null)\n" +
            "  {\n" +
            "    setOutputDone();\n" +
            "\treturn false;\n" +
            "  }\n" +
            "\n" +
            "  if (first) {\n" +
            "     yearIndex = getInputRowMeta().indexOfValue(getParameter(\"YEAR\"));\n" +
            "     if (yearIndex<0) {\n" +
            "         throw new KettleException(\"Year field not found in the input row, check parameter 'YEAR'!\");\n" +
            "     }\n" +
            "\n" +
            "     calendar = Calendar.getInstance();\n" +
            "     calendar.clear();\n" +
            "\n" +
            "     first=false;\n" +
            "  }\n" +
            " \n" +
            "  Object[] outputRowData = RowDataUtil.resizeArray(r, data.outputRowMeta.size());\n" +
            "  int outputIndex = getInputRowMeta().size();\n" +
            "\n" +
            "  Long year = getInputRowMeta().getInteger(r, yearIndex);\n" +
            "  outputRowData[outputIndex++] = easterDate(year.intValue());\n" +
            "\n" +
            "  putRow(data.outputRowMeta, outputRowData);\n" +
            "\n" +
            "  return true;\n" +
            "}\n" +
            "\n" +
            "private Date easterDate(int year) {\n" +
            " int a = year % 19;\n" +
            " int b = (int)Math.floor(year / 100);\n" +
            " int c = year % 100;\n" +
            " int d = (int)Math.floor(b / 4);\n" +
            " int e = b % 4;\n" +
            " int f = (int)Math.floor((b + 8) / 25);\n" +
            " int g = (int)Math.floor((b - f + 1) / 3);\n" +
            " int h = (19 * a + b - d - g + 15) % 30;\n" +
            " int i = (int)Math.floor(c / 4);\n" +
            " int k = c % 4;\n" +
            " int L = (32 + 2 * e + 2 * i - h - k) % 7;\n" +
            " int m = (int)Math.floor((a + 11 * h + 22 * L) / 451);\n" +
            " int n = h + L - 7 * m + 114;\n" +
            " \n" +
            " calendar.set(year, (int)(Math.floor(n / 31) - 1), (int)((n % 31) + 1));\n" +
            " return calendar.getTime();\n" +
            "}" +
            "]]>" +
            "</class_source>\n";

    String formatted = XMLFormatter.format( original );
    String expected = "" +
            "<class_source><![CDATA[ import java.util.*;\n" +
            "\n" +
            "private int yearIndex;\n" +
            "private Calendar calendar;\n" +
            "\n" +
            "public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException \n" +
            "{\n" +
            "  Object[] r=getRow();\n" +
            "  if (r==null)\n" +
            "  {\n" +
            "    setOutputDone();\n" +
            "\treturn false;\n" +
            "  }\n" +
            "\n" +
            "  if (first) {\n" +
            "     yearIndex = getInputRowMeta().indexOfValue(getParameter(\"YEAR\"));\n" +
            "     if (yearIndex<0) {\n" +
            "         throw new KettleException(\"Year field not found in the input row, check parameter 'YEAR'!\");\n" +
            "     }\n" +
            "\n" +
            "     calendar = Calendar.getInstance();\n" +
            "     calendar.clear();\n" +
            "\n" +
            "     first=false;\n" +
            "  }\n" +
            " \n" +
            "  Object[] outputRowData = RowDataUtil.resizeArray(r, data.outputRowMeta.size());\n" +
            "  int outputIndex = getInputRowMeta().size();\n" +
            "\n" +
            "  Long year = getInputRowMeta().getInteger(r, yearIndex);\n" +
            "  outputRowData[outputIndex++] = easterDate(year.intValue());\n" +
            "\n" +
            "  putRow(data.outputRowMeta, outputRowData);\n" +
            "\n" +
            "  return true;\n" +
            "}\n" +
            "\n" +
            "private Date easterDate(int year) {\n" +
            " int a = year % 19;\n" +
            " int b = (int)Math.floor(year / 100);\n" +
            " int c = year % 100;\n" +
            " int d = (int)Math.floor(b / 4);\n" +
            " int e = b % 4;\n" +
            " int f = (int)Math.floor((b + 8) / 25);\n" +
            " int g = (int)Math.floor((b - f + 1) / 3);\n" +
            " int h = (19 * a + b - d - g + 15) % 30;\n" +
            " int i = (int)Math.floor(c / 4);\n" +
            " int k = c % 4;\n" +
            " int L = (32 + 2 * e + 2 * i - h - k) % 7;\n" +
            " int m = (int)Math.floor((a + 11 * h + 22 * L) / 451);\n" +
            " int n = h + L - 7 * m + 114;\n" +
            " \n" +
            " calendar.set(year, (int)(Math.floor(n / 31) - 1), (int)((n % 31) + 1));\n" +
            " return calendar.getTime();\n" +
            "}]]></class_source>\n";

    assertEquals( expected, formatted );
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
