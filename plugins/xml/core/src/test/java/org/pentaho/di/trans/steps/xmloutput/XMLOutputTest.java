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
package org.pentaho.di.trans.steps.xmloutput;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.xmloutput.XMLField.ContentType;
import org.w3c.dom.Document;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tatsiana_Kasiankova
 *
 */
public class XMLOutputTest {

  private StepMockHelper<XMLOutputMeta, XMLOutputData> stepMockHelper;
  private XMLOutput xmlOutput;
  private XMLOutputMeta xmlOutputMeta;
  private XMLOutputData xmlOutputData;
  private Trans trans = mock( Trans.class );
  private static final String[] ILLEGAL_CHARACTERS_IN_XML_ATTRIBUTES = { "<", ">", "&", "\'", "\"" };

  private static Object[] rowWithData;
  private static Object[] rowWithNullData;

  private static final String XML_FILE_OUTPUT_PREFIX = "ResultFile";
  private static final String XML_FILE_OUTPUT_EXTENSION = ".xml";
  private static final String XML_FILE_NAME = XML_FILE_OUTPUT_PREFIX + XML_FILE_OUTPUT_EXTENSION;

  private static final XMLField xmlField1 =
    new XMLField( ContentType.getIfPresent( "Element" ), "FieldName1", "ElementName1", 2, Const.EMPTY_STRING, 0, 0,
      Const.EMPTY_STRING,
      Const.EMPTY_STRING, Const.EMPTY_STRING, Const.EMPTY_STRING );
  private static final XMLField xmlField2 =
    new XMLField( ContentType.getIfPresent( "Element" ), "FieldName2", "ElementName2", 2, Const.EMPTY_STRING, 0, 0,
      Const.EMPTY_STRING,
      Const.EMPTY_STRING, Const.EMPTY_STRING, Const.EMPTY_STRING );
  private static final XMLField xmlField3 =
    new XMLField( ContentType.getIfPresent( "Element" ), "FieldName3", "ElementName3", 2, Const.EMPTY_STRING, 0, 0,
      Const.EMPTY_STRING,
      Const.EMPTY_STRING, Const.EMPTY_STRING, "ABCD" );
  private final XMLField[] xmlFields = new XMLField[] { xmlField1, xmlField2, xmlField3 };

  private static final Object[] firstRow = new Object[] { "one", "two", "three" };
  private static final Object[] secondRow = new Object[] { "four", "", null };
  private final List<Object[]> rows = Arrays.asList( firstRow, secondRow );

  @BeforeClass
  public static void setUpBeforeClass() {

    rowWithData = initRowWithData( ILLEGAL_CHARACTERS_IN_XML_ATTRIBUTES );
    rowWithNullData = initRowWithNullData();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    FileUtils.deleteQuietly( Paths.get( XML_FILE_NAME ).toFile() );
  }

  @After
  public void tearDown() throws Exception {
    stepMockHelper.cleanUp();
  }

  @Before
  public void setup() throws Exception {

    stepMockHelper =
        new StepMockHelper<XMLOutputMeta, XMLOutputData>( "XML_OUTPUT_TEST", XMLOutputMeta.class, XMLOutputData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    StepMeta mockMeta = mock( StepMeta.class );
    when( stepMockHelper.transMeta.findStep( anyString() ) ).thenReturn( mockMeta );
    when( trans.getLogLevel() ).thenReturn( LogLevel.DEBUG );

    // Create and set Meta with some realistic data
    xmlOutputMeta = new XMLOutputMeta();
    xmlOutputMeta.setOutputFields( initOutputFields( rowWithData.length, ContentType.Attribute ) );
    // Set as true to prevent unnecessary for this test checks at initialization
    xmlOutputMeta.setDoNotOpenNewFileInit( false );

    xmlOutputData = new XMLOutputData();
    xmlOutputData.formatRowMeta = initRowMeta( rowWithData.length );
    xmlOutputData.fieldnrs = initFieldNmrs( rowWithData.length );
    xmlOutputData.OpenedNewFile = true;

    StepMeta stepMeta = new StepMeta( "StepMetaId", "StepMetaName", xmlOutputMeta );
    xmlOutput = spy( new XMLOutput( stepMeta, xmlOutputData, 0, stepMockHelper.transMeta, stepMockHelper.trans ) );
  }

  @Test
  public void testSpecialSymbolsInAttributeValuesAreEscaped() throws KettleException, XMLStreamException {
    xmlOutput.init( xmlOutputMeta, xmlOutputData );

    xmlOutputData.writer = mock( XMLStreamWriter.class );
    xmlOutput.writeRowAttributes( rowWithData );
    xmlOutput.dispose( xmlOutputMeta, xmlOutputData );
    verify( xmlOutputData.writer, times( rowWithData.length ) ).writeAttribute( any(), any() );
    verify( xmlOutput, atLeastOnce() ).closeOutputStream( any() );
  }

  @Test
  public void testNullInAttributeValuesAreEscaped() throws KettleException, XMLStreamException {

    testNullValuesInAttribute( 0 );
  }

  @Test
  public void testNullInAttributeValuesAreNotEscaped() throws KettleException, XMLStreamException {

    xmlOutput.setVariable( Const.KETTLE_COMPATIBILITY_XML_OUTPUT_NULL_VALUES, "Y" );

    testNullValuesInAttribute( rowWithNullData.length  );
  }

  /**
   * [PDI-15575] Testing to verify that getIfPresent defaults the XMLField ContentType value
   */
  @Test
  public void testDefaultXmlFieldContentType() {
    XMLField[] xmlFields = initOutputFields( 4, null );
    xmlFields[0].setContentType( ContentType.getIfPresent( "Element" ) );
    xmlFields[1].setContentType( ContentType.getIfPresent( "Attribute" ) );
    xmlFields[2].setContentType( ContentType.getIfPresent( "" ) );
    xmlFields[3].setContentType( ContentType.getIfPresent( "WrongValue" ) );
    assertEquals( xmlFields[0].getContentType(), ContentType.Element );
    assertEquals( xmlFields[1].getContentType(), ContentType.Attribute );
    assertEquals( xmlFields[2].getContentType(), ContentType.Element );
    assertEquals( xmlFields[3].getContentType(), ContentType.Element );
  }

  /**
   * [PDI-19286] Testing to verify that namespace is valid (URI)
   */
  @Test
  public void testIsValidNamespace() {
    String[] validNamespaces = {
      "http://www.foobar.com",
      "https://www.foobar.com%20foobar",
      "namespace",
      "%3Cnamespace%26%3E",
      "ftp://ftp.is.co.za/rfc/rfc1808.txt",
      "www.foobar.com",
      "www.foobar.com%7b%7D"
    };
    String[] invalidNamespaces = {
      "<namespace&>",
      "www.foobar.com{}",
      "www.foobar.com@##"
    };
    for ( String s : validNamespaces ) {
      assertTrue( XMLOutput.isValidNamespace( s ) );
    }
    for ( String s : invalidNamespaces ) {
      assertFalse( XMLOutput.isValidNamespace( s ) );
    }
  }

  @Test
  public void testFlow() throws Exception {
    XMLOutputTestHandler xmlOutput = createXmlOutputTestHandler();

    xmlOutput.init( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.initStepDataInterface );
    for ( Object[] row : rows ) {
      xmlOutput.setRow( row );
      xmlOutput.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.initStepDataInterface );
    }
    xmlOutput.setRow( null );
    xmlOutput.dispose( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.initStepDataInterface );

    Document document = parseXml( XML_FILE_NAME );
    assertNotNull( xmlOutput.getResultFiles() );
    assertEquals( 1, xmlOutput.getResultFiles().size() );
    assertEquals( "one", document.getElementsByTagName( "Row" ).item( 0 ).getFirstChild().getTextContent() );
    assertEquals( "four", document.getElementsByTagName( "Row" ).item( 1 ).getFirstChild().getTextContent() );
  }

  @Test
  public void testFlowWithKettlePropertyNullIfValueAllowed() throws Exception {
    XMLOutputTestHandler xmlOutput = createXmlOutputTestHandler();
    xmlOutput.setVariable( Const.KETTLE_COMPATIBILITY_XML_OUTPUT_NULL_IF_FIELD_VALUES, "Y" );

    xmlOutput.init( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.initStepDataInterface );
    for ( Object[] row : rows ) {
      xmlOutput.setRow( row );
      xmlOutput.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.initStepDataInterface );
    }
    xmlOutput.setRow( null );
    xmlOutput.dispose( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.initStepDataInterface );

    Document document = parseXml( XML_FILE_NAME );
    assertNotNull( xmlOutput.getResultFiles() );
    assertEquals( 1, xmlOutput.getResultFiles().size() );
    assertEquals( "one", document.getElementsByTagName( "Row" ).item( 0 ).getFirstChild().getTextContent() );
    assertEquals( "four", document.getElementsByTagName( "Row" ).item( 1 ).getFirstChild().getTextContent() );
    assertEquals( "ABCD", document.getElementsByTagName( "Row" ).item( 1 ).getLastChild().getTextContent() );
  }

  private XMLOutputTestHandler createXmlOutputTestHandler() {
    XMLOutputTestHandler xmlOutput =
      new XMLOutputTestHandler( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
        stepMockHelper.transMeta, stepMockHelper.trans );

    when( stepMockHelper.initStepMetaInterface.getOutputFields() ).thenReturn( xmlFields );
    when( stepMockHelper.initStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( true );

    when( stepMockHelper.processRowsStepMetaInterface.getOutputFields() ).thenReturn( xmlFields );
    when( stepMockHelper.processRowsStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( true );
    when( stepMockHelper.processRowsStepMetaInterface.isAddToResultFiles() ).thenReturn( true );
    when( stepMockHelper.processRowsStepMetaInterface.getNameSpace() ).thenReturn( Const.EMPTY_STRING );
    when( stepMockHelper.processRowsStepMetaInterface.getMainElement() ).thenReturn( "Rows" );
    when( stepMockHelper.processRowsStepMetaInterface.getRepeatElement() ).thenReturn( "Row" );
    when( stepMockHelper.processRowsStepMetaInterface.getFileName() ).thenReturn( XML_FILE_NAME );
    when( stepMockHelper.processRowsStepMetaInterface.buildFilename( any(), anyInt(), anyInt(), anyBoolean() ) ).
      thenReturn( XML_FILE_NAME );

    RowSet rowSet = stepMockHelper.getMockInputRowSet( rows );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    xmlOutput.setInputRowMeta( inputRowMeta );

    when( rowSet.getRowWait( anyInt(), any( TimeUnit.class ) ) )
      .thenReturn( rows.isEmpty() ? null : rows.iterator().next() );
    when( rowSet.getRowMeta() ).thenReturn( inputRowMeta );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );

    for ( int i = 0; i < xmlFields.length; i++ ) {
      String name = xmlFields[ i ].getFieldName();
      ValueMetaString valueMetaString = new ValueMetaString( name );
      when( inputRowMeta.getValueMeta( i ) ).thenReturn( valueMetaString );
      when( inputRowMeta.indexOfValue( name ) ).thenReturn( i );
      when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    }

    xmlOutput.addRowSetToInputRowSets( rowSet );
    xmlOutput.addRowSetToOutputRowSets( rowSet );
    return xmlOutput;
  }

  public Document parseXml( String filePath ) throws Exception {
    String xml = IOUtils.toString( KettleVFS.getInputStream( filePath ), StandardCharsets.UTF_8 );
    return XMLHandler.loadXMLString( xml );
  }

  private void testNullValuesInAttribute( int writeNullInvocationExpected ) throws KettleException, XMLStreamException {

    xmlOutput.init( xmlOutputMeta, xmlOutputData );

    xmlOutputData.writer = mock( XMLStreamWriter.class );
    xmlOutput.writeRowAttributes( rowWithNullData );
    xmlOutput.dispose( xmlOutputMeta, xmlOutputData );
    verify( xmlOutputData.writer, times( writeNullInvocationExpected ) ).writeAttribute( any(), any() );
    verify( xmlOutput, atLeastOnce() ).closeOutputStream( any() );
  }

  private static Object[] initRowWithData( String[] dt ) {

    Object[] data = new Object[dt.length * 3];
    for ( int i = 0; i < dt.length; i++ ) {
      data[3 * i] = dt[i] + "TEST";
      data[3 * i + 1] = "TEST" + dt[i] + "TEST";
      data[3 * i + 2] = "TEST" + dt[i];
    }
    return data;
  }

  private static Object[] initRowWithNullData() {

    Object[] data = new Object[15];
    for ( int i = 0; i < data.length; i++ ) {

      data[i] = null;
    }

    return data;
  }

  private RowMeta initRowMeta( int count ) {
    RowMeta rm = new RowMeta();
    for ( int i = 0; i < count; i++ ) {
      rm.addValueMeta( new ValueMeta( "string", ValueMetaInterface.TYPE_STRING ) );
    }
    return rm;
  }

  private XMLField[] initOutputFields( int i, ContentType attribute ) {

    XMLField[] fields = new XMLField[i];
    for ( int j = 0; j < fields.length; j++ ) {
      fields[j] =
          new XMLField( attribute, "Fieldname" + ( j + 1 ), "ElementName" + ( j + 1 ), 2, null, -1, -1, null, null,
              null, null );
    }

    return fields;
  }

  private int[] initFieldNmrs( int i ) {
    int[] fNmrs = new int[i];
    for ( int j = 0; j < fNmrs.length; j++ ) {
      fNmrs[j] = j;
    }
    return fNmrs;
  }

  protected static class XMLOutputTestHandler extends XMLOutput {
    public List<Throwable> errors = new ArrayList<>();
    private Object[] row;

    XMLOutputTestHandler( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
                          TransMeta transMeta, Trans trans ) {
      super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    }

    public void setRow( Object[] row ) {
      this.row = row;
    }

    @Override
    public Object[] getRow() throws KettleException {
      return row;
    }

    @Override
    public void putRow( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {

    }

    @Override
    public void logError( String message ) {
      errors.add( new KettleException( message ) );
    }

    @Override
    public void logError( String message, Throwable thr ) {
      errors.add( thr );
    }

    @Override
    public void logError( String message, Object... arguments ) {
      errors.add( new KettleException( message ) );
    }
  }
}
