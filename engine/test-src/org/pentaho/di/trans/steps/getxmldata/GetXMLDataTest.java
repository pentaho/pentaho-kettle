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
package org.pentaho.di.trans.steps.getxmldata;

import java.util.List;

import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.dom4j.Document;
import org.dom4j.XPath;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LoggingObjectInterface;

/**
 * @author Tatsiana_Kasiankova
 * 
 */
public class GetXMLDataTest {

  private static final String TEXT_PART1 = "Text part one";
  private static final String TEXT_PART2 = "Text part two";
  private static final String NEW_LINE_CHARACTER = "\n";
  private static final String AMPERSAND = "&amp;";
  private static final String SPACE = " ";

  private static final String XPATH_LEVEL1_ELEMENT = "/root/level1[@test]/text()";
  private static final String TEST_ATTRIBUTE = "test";
  private static final String TEST_ATTRIBUTE_VALUE = "'test value'";

  private StepMockHelper<GetXMLDataMeta, StepDataInterface> stepMockHelper;

  @Before
  public void setup() {
    stepMockHelper =
        new StepMockHelper<GetXMLDataMeta, StepDataInterface>( "GET XML DATA", GetXMLDataMeta.class,
            StepDataInterface.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    when( stepMockHelper.initStepMetaInterface.getInputFields() ).thenReturn( new GetXMLDataField[] {} );
  }

  @Test
  public void testTextParsedIntoSeveralTextNodesNormalized() throws Exception {
    GetXMLDataData getXmlDataData = buildGetXMLDataData();
    GetXMLData getXmlData =
        new GetXMLData( stepMockHelper.stepMeta, getXmlDataData, 0, stepMockHelper.transMeta, stepMockHelper.trans );
    getXmlData.init( stepMockHelper.initStepMetaInterface, getXmlDataData );
    getXmlDataData.prunePath = null;

    getXmlData.setDocument( getXMLToReproduceParsingTextIntoSeveralNodes(), null, true, false );

    Document document = getXmlData.getData().document;
    XPath xpath = document.createXPath( XPATH_LEVEL1_ELEMENT );
    List selectedNodes = xpath.selectNodes( document );
    assertEquals( "Adjacent text nodes should be merged into the one. But it doesn't. Text consists of : "
        + selectedNodes.size() + " nodes.", 1, selectedNodes.size() );
  }

  @Test
  public void testTextWithNewLineParsedIntoSeveralTextNodesNormalized() throws Exception {
    GetXMLDataData getXmlDataData = buildGetXMLDataData();
    GetXMLData getXmlData =
        new GetXMLData( stepMockHelper.stepMeta, getXmlDataData, 0, stepMockHelper.transMeta, stepMockHelper.trans );
    getXmlData.init( stepMockHelper.initStepMetaInterface, getXmlDataData );
    getXmlDataData.prunePath = null;

    getXmlData.setDocument( getXMLToReproduceParsingTextIntoSeveralNodes_WithNewLineCharacters(), null, true, false );

    Document document = getXmlData.getData().document;
    XPath xpath = document.createXPath( XPATH_LEVEL1_ELEMENT );
    List selectedNodes = xpath.selectNodes( document );
    assertEquals( "Adjacent text nodes should be merged into the one. But it doesn't. Text consists of : "
        + selectedNodes.size() + " nodes.", 1, selectedNodes.size() );
  }

  private static String getXMLToReproduceParsingTextIntoSeveralNodes() {
    StringBuffer sb = new StringBuffer();
    sb.append( "<root>" );
    // element 1 - without any attribute
    sb.append( "<level1 >" );
    // added the text with & character. Potentially such text will be parsed as several test nodes
    sb.append( TEXT_PART1 ).append( SPACE ).append( AMPERSAND ).append( SPACE ).append( TEXT_PART2 ).append( SPACE )
        .append( "additional text" );
    sb.append( "</level1>" );

    // element 2 - with the attribute
    sb.append( "<level1 " ).append( TEST_ATTRIBUTE ).append( "=" ).append( TEST_ATTRIBUTE_VALUE ).append( ">" );
    // added the text with & character. Potentially such text will be parsed as several test nodes
    sb.append( TEXT_PART1 ).append( SPACE ).append( AMPERSAND ).append( SPACE ).append( TEXT_PART2 );
    sb.append( "</level1>" );

    sb.append( "</root>" );
    return sb.toString();
  }

  private static String getXMLToReproduceParsingTextIntoSeveralNodes_WithNewLineCharacters() {
    StringBuffer sb = new StringBuffer();
    sb.append( "<root>" ).append( NEW_LINE_CHARACTER );
    // element 1 - without any attribute
    sb.append( "<level1 >" );
    // added the text with & character. Potentially such text will be parsed as several test nodes
    sb.append( TEXT_PART1 ).append( SPACE ).append( AMPERSAND ).append( SPACE ).append( TEXT_PART2 ).append( SPACE )
        .append( "additional text" );
    sb.append( "</level1>" ).append( NEW_LINE_CHARACTER );

    // element 2 - with the attribute
    sb.append( "<level1 " ).append( TEST_ATTRIBUTE ).append( "=" ).append( TEST_ATTRIBUTE_VALUE ).append( ">" );
    // added the text with & character. Potentially such text will be parsed as several test nodes
    sb.append( TEXT_PART1 ).append( SPACE ).append( AMPERSAND ).append( SPACE ).append( TEXT_PART2 );
    sb.append( "</level1>" ).append( NEW_LINE_CHARACTER );

    sb.append( "</root>" );
    return sb.toString();
  }

  private GetXMLDataData buildGetXMLDataData() {
    GetXMLDataData getXmlDataData = new GetXMLDataData();
    getXmlDataData.outputRowMeta = new RowMeta();
    return getXmlDataData;
  }

  @After
  public void tearDown() {
    stepMockHelper.cleanUp();
  }

}
