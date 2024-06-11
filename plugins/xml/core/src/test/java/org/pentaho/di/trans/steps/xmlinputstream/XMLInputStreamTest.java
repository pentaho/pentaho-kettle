/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.xmlinputstream;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.SingleRowRowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Tatsiana_Kasiankova
 * 
 */
public class XMLInputStreamTest {
  private static final String INCORRECT_XML_DATA_VALUE_MESSAGE = "Incorrect xml data value - ";
  private static final String INCORRECT_XML_DATA_NAME_MESSAGE = "Incorrect xml data name - ";
  private static final String INCORRECT_XML_PATH_MESSAGE = "Incorrect xml path - ";
  private static final String INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE = "Incorrect xml data type description - ";

  private static final String ATTRIBUTE_2 = "ATTRIBUTE_2";

  private static final String ATTRIBUTE_1 = "ATTRIBUTE_1";

  private static final int START_ROW_IN_XML_TO_VERIFY = 9;

  private static StepMockHelper<XMLInputStreamMeta, XMLInputStreamData> stepMockHelper;

  private XMLInputStreamMeta xmlInputStreamMeta;

  private XMLInputStreamData xmlInputStreamData;

  private TestRowListener rl;

  private int typeDescriptionPos = 0;
  private int pathPos = 1;
  private int dataNamePos = 2;
  private int dataValue = 3;

  @Before
  public void setUp() throws KettleException {
    stepMockHelper =
        new StepMockHelper<XMLInputStreamMeta, XMLInputStreamData>( "XMLInputStreamTest", XMLInputStreamMeta.class,
            XMLInputStreamData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );

    xmlInputStreamMeta = new XMLInputStreamMeta();
    xmlInputStreamMeta.setDefault();

    xmlInputStreamData = new XMLInputStreamData();
    rl = new TestRowListener();

    // Turn off several options.
    // So there are fields: xml_data_type_description - xml_path - xml_data_name - xml_data_value
    xmlInputStreamMeta.setIncludeXmlParentPathField( false );
    xmlInputStreamMeta.setIncludeXmlParentElementIDField( false );
    xmlInputStreamMeta.setIncludeXmlElementIDField( false );
    xmlInputStreamMeta.setIncludeXmlElementLevelField( false );
  }

  @After
  public void tearDown() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testParseXmlWithPrefixes_WhenSetEnableNamespaceAsTrue() throws KettleException, IOException {
    xmlInputStreamMeta.setFilename( createTestFile( getXMLString( getGroupWithPrefix() ) ) );
    xmlInputStreamMeta.setEnableNamespaces( true );

    doTest();

    // Assertions
    // check StartElement for the ProductGroup element
    // when namespaces are enabled, we have additional NAMESPACE events - 3 for our test xml;
    int expectedRowNum = START_ROW_IN_XML_TO_VERIFY + 3;
    assertEquals( INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE, "START_ELEMENT",
        rl.getWritten().get( expectedRowNum )[typeDescriptionPos] );
    assertEquals( INCORRECT_XML_PATH_MESSAGE, "/Products/Product/Fruits:ProductGroup", rl.getWritten().get(
        expectedRowNum )[pathPos] );
    assertEquals( INCORRECT_XML_DATA_NAME_MESSAGE, "Fruits:ProductGroup",
        rl.getWritten().get( expectedRowNum )[dataNamePos] );

    // attributes
    // ATTRIBUTE_1
    expectedRowNum++;
    assertEquals( INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE, "ATTRIBUTE",
        rl.getWritten().get( expectedRowNum )[typeDescriptionPos] );
    assertEquals( INCORRECT_XML_PATH_MESSAGE, "/Products/Product/Fruits:ProductGroup", rl.getWritten().get(
        expectedRowNum )[pathPos] );
    assertEquals( INCORRECT_XML_DATA_NAME_MESSAGE, "Fruits:attribute",
        rl.getWritten().get( expectedRowNum )[dataNamePos] );
    assertEquals( INCORRECT_XML_DATA_VALUE_MESSAGE, ATTRIBUTE_1, rl.getWritten().get( expectedRowNum )[dataValue] );
    // ATTRIBUTE_2
    expectedRowNum++;
    assertEquals( INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE, "ATTRIBUTE",
        rl.getWritten().get( expectedRowNum )[typeDescriptionPos] );
    assertEquals( INCORRECT_XML_PATH_MESSAGE, "/Products/Product/Fruits:ProductGroup", rl.getWritten().get(
        expectedRowNum )[pathPos] );
    assertEquals( INCORRECT_XML_DATA_NAME_MESSAGE, "Fish:attribute", rl.getWritten().get( expectedRowNum )[dataNamePos] );
    assertEquals( INCORRECT_XML_DATA_VALUE_MESSAGE, ATTRIBUTE_2, rl.getWritten().get( expectedRowNum )[dataValue] );

    // check EndElement for the ProductGroup element
    expectedRowNum = expectedRowNum + 2;
    assertEquals( INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE, "END_ELEMENT",
        rl.getWritten().get( expectedRowNum )[typeDescriptionPos] );
    assertEquals( INCORRECT_XML_PATH_MESSAGE, "/Products/Product/Fruits:ProductGroup", rl.getWritten().get(
        expectedRowNum )[pathPos] );
    assertEquals( INCORRECT_XML_DATA_NAME_MESSAGE, "Fruits:ProductGroup",
        rl.getWritten().get( expectedRowNum )[dataNamePos] );
  }

  @Test
  public void testParseXmlWithPrefixes_WhenSetEnableNamespaceAsFalse() throws KettleException, IOException {
    xmlInputStreamMeta.setFilename( createTestFile( getXMLString( getGroupWithPrefix() ) ) );
    xmlInputStreamMeta.setEnableNamespaces( false );

    doTest();

    // Assertions
    // check StartElement for the ProductGroup element
    int expectedRowNum = START_ROW_IN_XML_TO_VERIFY;
    assertEquals( INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE, "START_ELEMENT",
        rl.getWritten().get( expectedRowNum )[typeDescriptionPos] );
    assertEquals( INCORRECT_XML_PATH_MESSAGE, "/Products/Product/ProductGroup",
        rl.getWritten().get( expectedRowNum )[pathPos] );
    assertEquals( INCORRECT_XML_DATA_NAME_MESSAGE, "ProductGroup", rl.getWritten().get( expectedRowNum )[dataNamePos] );

    // attributes
    // ATTRIBUTE_1
    expectedRowNum++;
    assertEquals( INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE, "ATTRIBUTE",
        rl.getWritten().get( expectedRowNum )[typeDescriptionPos] );
    assertEquals( INCORRECT_XML_PATH_MESSAGE, "/Products/Product/ProductGroup",
        rl.getWritten().get( expectedRowNum )[pathPos] );
    assertEquals( INCORRECT_XML_DATA_NAME_MESSAGE, "attribute", rl.getWritten().get( expectedRowNum )[dataNamePos] );
    assertEquals( INCORRECT_XML_DATA_VALUE_MESSAGE, ATTRIBUTE_1, rl.getWritten().get( expectedRowNum )[dataValue] );
    // ATTRIBUTE_2
    expectedRowNum++;
    assertEquals( INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE, "ATTRIBUTE",
        rl.getWritten().get( expectedRowNum )[typeDescriptionPos] );
    assertEquals( INCORRECT_XML_PATH_MESSAGE, "/Products/Product/ProductGroup",
        rl.getWritten().get( expectedRowNum )[pathPos] );
    assertEquals( INCORRECT_XML_DATA_NAME_MESSAGE, "attribute", rl.getWritten().get( expectedRowNum )[dataNamePos] );
    assertEquals( INCORRECT_XML_DATA_VALUE_MESSAGE, ATTRIBUTE_2, rl.getWritten().get( expectedRowNum )[dataValue] );

    // check EndElement for the ProductGroup element
    expectedRowNum = expectedRowNum + 2;
    assertEquals( INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE, "END_ELEMENT",
        rl.getWritten().get( expectedRowNum )[typeDescriptionPos] );
    assertEquals( INCORRECT_XML_PATH_MESSAGE, "/Products/Product/ProductGroup",
        rl.getWritten().get( expectedRowNum )[pathPos] );
    assertEquals( INCORRECT_XML_DATA_NAME_MESSAGE, "ProductGroup", rl.getWritten().get( expectedRowNum )[dataNamePos] );
  }

  @Test
  public void testParseXmlWithoutPrefixes_WhenSetEnableNamespaceAsTrue() throws KettleException, IOException {
    xmlInputStreamMeta.setFilename( createTestFile( getXMLString( getGroupWithoutPrefix() ) ) );
    xmlInputStreamMeta.setEnableNamespaces( true );

    doTest();

    // Assertions
    // check StartElement for the ProductGroup element
    // when namespaces are enabled, we have additional NAMESPACE events - 3 for our test xml;
    int expectedRowNum = START_ROW_IN_XML_TO_VERIFY + 3;
    assertEquals( INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE, "START_ELEMENT",
        rl.getWritten().get( expectedRowNum )[typeDescriptionPos] );
    assertEquals( INCORRECT_XML_PATH_MESSAGE, "/Products/Product/ProductGroup",
        rl.getWritten().get( expectedRowNum )[pathPos] );
    assertEquals( INCORRECT_XML_DATA_NAME_MESSAGE, "ProductGroup", rl.getWritten().get( expectedRowNum )[dataNamePos] );

    // attributes
    // ATTRIBUTE_1
    expectedRowNum++;
    assertEquals( INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE, "ATTRIBUTE",
        rl.getWritten().get( expectedRowNum )[typeDescriptionPos] );
    assertEquals( INCORRECT_XML_PATH_MESSAGE, "/Products/Product/ProductGroup",
        rl.getWritten().get( expectedRowNum )[pathPos] );
    assertEquals( INCORRECT_XML_DATA_NAME_MESSAGE, "attribute1", rl.getWritten().get( expectedRowNum )[dataNamePos] );
    assertEquals( INCORRECT_XML_DATA_VALUE_MESSAGE, ATTRIBUTE_1, rl.getWritten().get( expectedRowNum )[dataValue] );
    // ATTRIBUTE_2
    expectedRowNum++;
    assertEquals( INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE, "ATTRIBUTE",
        rl.getWritten().get( expectedRowNum )[typeDescriptionPos] );
    assertEquals( INCORRECT_XML_PATH_MESSAGE, "/Products/Product/ProductGroup",
        rl.getWritten().get( expectedRowNum )[pathPos] );
    assertEquals( INCORRECT_XML_DATA_NAME_MESSAGE, "attribute2", rl.getWritten().get( expectedRowNum )[dataNamePos] );
    assertEquals( INCORRECT_XML_DATA_VALUE_MESSAGE, ATTRIBUTE_2, rl.getWritten().get( expectedRowNum )[dataValue] );

    // check EndElement for the ProductGroup element
    expectedRowNum = expectedRowNum + 2;
    assertEquals( INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE, "END_ELEMENT",
        rl.getWritten().get( expectedRowNum )[typeDescriptionPos] );
    assertEquals( INCORRECT_XML_PATH_MESSAGE, "/Products/Product/ProductGroup",
        rl.getWritten().get( expectedRowNum )[pathPos] );
    assertEquals( INCORRECT_XML_DATA_NAME_MESSAGE, "ProductGroup", rl.getWritten().get( expectedRowNum )[dataNamePos] );
  }

  @Test
  public void multiLineDataReadAsMultipleElements() throws KettleException, IOException {
    xmlInputStreamMeta.setFilename( createTestFile( getMultiLineDataXml() ) );
    xmlInputStreamMeta.setEnableNamespaces( true );

    doTest();

    assertEquals( "CHARACTERS", rl.getWritten().get( 2 )[0] );
    assertEquals( "some data", rl.getWritten().get( 2 )[3] );
    assertEquals( "CHARACTERS", rl.getWritten().get( 3 )[0] );
    assertEquals( "other data", rl.getWritten().get( 3 )[3] );
  }

  private void doTest() throws IOException, KettleException {
    XMLInputStream xmlInputStream =
        new XMLInputStream( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
            stepMockHelper.trans );

    RowMetaInterface inputRowMeta = new RowMeta(  );
    xmlInputStream.setInputRowMeta( inputRowMeta );
    xmlInputStream.init( xmlInputStreamMeta, xmlInputStreamData );
    xmlInputStream.addRowListener( rl );
    boolean haveRowsToRead;
    do {
      haveRowsToRead = !xmlInputStream.processRow( xmlInputStreamMeta, xmlInputStreamData );

    } while ( !haveRowsToRead );
  }

  @Test
  public void testFromPreviousStep() throws Exception {
    xmlInputStreamMeta.sourceFromInput = true;
    xmlInputStreamMeta.sourceFieldName = "inf";
    xmlInputStreamData.outputRowMeta = new RowMeta();

    RowMeta rm = new RowMeta();
    String xml = "<ProductGroup attribute1=\"v1\"/>";
    ValueMetaString ms = new ValueMetaString( "inf" );
    RowSet rs = new SingleRowRowSet();
    rs.putRow( rm, new Object[] { xml } );
    rs.setDone();

    XMLInputStream xmlInputStream =
        new XMLInputStream( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
            stepMockHelper.trans );
    xmlInputStream.setInputRowMeta( rm );
    xmlInputStream.getInputRowMeta().addValueMeta( ms );
    xmlInputStream.addRowSetToInputRowSets( rs );
    xmlInputStream.setOutputRowSets( new ArrayList<>() );

    xmlInputStream.init( xmlInputStreamMeta, xmlInputStreamData );
    xmlInputStream.addRowListener( rl );
    boolean haveRowsToRead;
    do {
      haveRowsToRead = !xmlInputStream.processRow( xmlInputStreamMeta, xmlInputStreamData );
    } while ( !haveRowsToRead );

    int expectedRowNum = 1;

    assertEquals( INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE, "<ProductGroup attribute1=\"v1\"/>",
        rl.getWritten().get( expectedRowNum )[typeDescriptionPos] );

    assertEquals( INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE, "START_ELEMENT", rl.getWritten().get(
        expectedRowNum )[typeDescriptionPos + 1] );
    assertEquals( INCORRECT_XML_PATH_MESSAGE, "/ProductGroup", rl.getWritten().get( expectedRowNum )[pathPos + 1] );
    assertEquals( INCORRECT_XML_DATA_NAME_MESSAGE, "ProductGroup", rl.getWritten().get( expectedRowNum )[dataNamePos + 1] );

    // attributes
    // ATTRIBUTE_1
    expectedRowNum++;
    assertEquals( INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE, "ATTRIBUTE", rl.getWritten().get(
        expectedRowNum )[typeDescriptionPos + 1] );
    assertEquals( INCORRECT_XML_PATH_MESSAGE, "/ProductGroup", rl.getWritten().get( expectedRowNum )[pathPos + 1] );
    assertEquals( INCORRECT_XML_DATA_NAME_MESSAGE, "attribute1", rl.getWritten().get( expectedRowNum )[dataNamePos + 1] );
    assertEquals( INCORRECT_XML_DATA_VALUE_MESSAGE, "v1", rl.getWritten().get( expectedRowNum )[dataValue + 1] );

    // check EndElement for the ProductGroup element
    expectedRowNum++;
    assertEquals( INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE, "END_ELEMENT", rl.getWritten().get(
        expectedRowNum )[typeDescriptionPos + 1] );
    assertEquals( INCORRECT_XML_PATH_MESSAGE, "/ProductGroup", rl.getWritten().get( expectedRowNum )[pathPos + 1] );
    assertEquals( INCORRECT_XML_DATA_NAME_MESSAGE, "ProductGroup", rl.getWritten().get( expectedRowNum )[dataNamePos + 1] );
  }

  @Test
  public void testFileNameEnteredManuallyWithIncomingHops() throws Exception {
    testCorrectFileSelected( getFile( "default.xml" ), 0 );
  }

  @Test
  public void testFileNameSelectedFromIncomingHops() throws Exception {
    testCorrectFileSelected( "filename", 1 );
  }

  @Test( expected = KettleException.class )
  public void testNotValidFilePathAndFileField() throws Exception {
    testCorrectFileSelected( "notPathNorValidFieldName", 0 );
  }

  @Test( expected = KettleException.class )
  public void testEmptyFileField() throws Exception {
    testCorrectFileSelected( StringUtils.EMPTY, 0 );
  }

  private void testCorrectFileSelected( String filenameParam, int xmlTagsStartPosition ) throws KettleException {
    xmlInputStreamMeta.sourceFromInput = false;
    xmlInputStreamMeta.setFilename( filenameParam );
    xmlInputStreamData.outputRowMeta = new RowMeta();

    RowMeta rm = new RowMeta();
    String pathValue = getFile( "default.xml" );
    ValueMetaString ms = new ValueMetaString( "filename" );
    RowSet rs = new SingleRowRowSet();
    rs.putRow( rm, new Object[] { pathValue } );
    rs.setDone();

    when( stepMockHelper.transMeta.findNrPrevSteps( stepMockHelper.stepMeta ) ).thenReturn( 1 );

    XMLInputStream xmlInputStream =
            new XMLInputStream( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
                    stepMockHelper.trans );
    xmlInputStream.setInputRowMeta( rm );
    xmlInputStream.getInputRowMeta().addValueMeta( ms );
    xmlInputStream.addRowSetToInputRowSets( rs );
    xmlInputStream.setOutputRowSets( new ArrayList<>() );

    xmlInputStream.init( xmlInputStreamMeta, xmlInputStreamData );
    xmlInputStream.addRowListener( rl );
    boolean haveRowsToRead;
    do {
      haveRowsToRead = !xmlInputStream.processRow( xmlInputStreamMeta, xmlInputStreamData );
    } while ( !haveRowsToRead );
    assertEquals( INCORRECT_XML_DATA_TYPE_DESCRIPTION_MESSAGE, "START_ELEMENT", rl.getWritten().get(
        1 )[xmlTagsStartPosition] );
  }

  private String createTestFile( String xmlContent ) throws IOException {
    File tempFile = File.createTempFile( "Test", ".xml" );
    tempFile.deleteOnExit();
    Writer osw = new PrintWriter( tempFile, "UTF8" );
    System.out.println( xmlContent );
    osw.write( xmlContent );
    osw.close();

    return tempFile.getAbsolutePath();
  }

  private String getXMLString( String group ) {
    return "<Products xmlns:Fruits=\"http://dummy.example/fruits\" xmlns:Fish=\"http://dummy.example/fish\" xmlns=\"http://dummy.example/default\">"
        + getProduct( group ) + "</Products>";
  }

  private String getProduct( String group ) {
    return "<Product><Id>1</Id><Name>TEST_NAME</Name>" + group + "</Product>";
  }

  private String getGroupWithPrefix() {
    return "<Fruits:ProductGroup Fruits:attribute=\"" + ATTRIBUTE_1 + "\" Fish:attribute=\"" + ATTRIBUTE_2
        + "\">G</Fruits:ProductGroup>";
  }

  private String getGroupWithoutPrefix() {
    return "<ProductGroup attribute1=\"" + ATTRIBUTE_1 + "\" attribute2=\"" + ATTRIBUTE_2 + "\">G</ProductGroup>";
  }

  private String getMultiLineDataXml() {
    return "<tag>"
      + "some data\n"
      + "<![CDATA[other data]]></tag>";
  }

  private class TestRowListener extends RowAdapter {
    private List<Object[]> written = new ArrayList<Object[]>();

    public List<Object[]> getWritten() {
      return written;
    }

    @Override
    public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
      written.add( row );
    }
  }

  private String getFile( String filename ) {
    String inPrefix = '/' + this.getClass().getPackage().getName().replace( '.', '/' ) + "/files/";
    URL res = this.getClass().getResource( inPrefix + filename );
    return res.getFile();
  }

}
