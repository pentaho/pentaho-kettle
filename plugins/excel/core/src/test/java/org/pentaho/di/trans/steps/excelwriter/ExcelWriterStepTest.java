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

package org.pentaho.di.trans.steps.excelwriter;

import org.apache.commons.vfs2.FileObject;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.utils.TestUtils;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExcelWriterStepTest {

  public static URL getTemplateTestXlsx() {
    return ExcelWriterStepTest.class.getResource( "template_test.xlsx" );
  }

  public static URL getTemplateWithFormattingXlsx() {
    return ExcelWriterStepTest.class.getResource( "template_with_formatting.xlsx" );
  }

  private static final String SHEET_NAME = "Sheet1";
  private static final String XLS = "xls";
  private static final String DOT_XLS = '.' + XLS;
  private static final String XLSX = "xlsx";
  private static final String DOT_XLSX = '.' + XLSX;

  private Workbook wb;
  private StepMockHelper<ExcelWriterStepMeta, ExcelWriterStepData> mockHelper;
  private ExcelWriterStep step;

  private ExcelWriterStepMeta metaMock;
  private ExcelWriterStepData dataMock;

  @Before
  public void setUp() throws Exception {
    String path = TestUtils.createRamFile( getClass().getSimpleName() + "/testXLSProtect.xls" );
    FileObject xlsFile = TestUtils.getFileObject( path );
    wb = createWorkbook( xlsFile );
    mockHelper = new StepMockHelper<>( "Excel Writer Test", ExcelWriterStepMeta.class, ExcelWriterStepData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      mockHelper.logChannelInterface );
    step = spy( new ExcelWriterStep(
      mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans ) );

    metaMock = mock( ExcelWriterStepMeta.class );
    dataMock = mock( ExcelWriterStepData.class );
  }

  @After
  public void cleanUp() {
    mockHelper.cleanUp();
  }

  @Test
  public void testProtectSheet() throws Exception {
    step.protectSheet( wb.getSheet( SHEET_NAME ), "aa" );
    assertTrue( wb.getSheet( SHEET_NAME ).getProtect() );
  }

  @Test
  public void testMaxSheetNameLength() {

    // Return a 32 character name
    when( mockHelper.initStepMetaInterface.getSheetname() ).thenReturn( "12345678901234567890123456789012" );
    step.init( mockHelper.initStepMetaInterface, mockHelper.initStepDataInterface );

    try {
      step.prepareNextOutputFile();
      // An exception should have been thrown!
      fail();
    } catch ( KettleException e ) {
      String content = e.getMessage();

      // We expected this error message, the sheet name is too long for Excel
      assertTrue( content.contains( "12345678901234567890123456789012" ) );
    }
  }

  @Test
  public void testPrepareNextOutputFile() throws Exception {
    assertTrue( step.init( metaMock, dataMock ) );
    File outDir = Files.createTempDirectory( "" ).toFile();
    String testFileOut = outDir.getAbsolutePath() + File.separator + "test.xlsx";
    when( step.buildFilename( 0 ) ).thenReturn( testFileOut );
    when( metaMock.isTemplateEnabled() ).thenReturn( true );
    when( metaMock.isStreamingData() ).thenReturn( true );
    when( metaMock.isHeaderEnabled() ).thenReturn( true );
    when( metaMock.getExtension() ).thenReturn( XLSX );
    dataMock.createNewFile = true;
    dataMock.realTemplateFileName = getTemplateTestXlsx().getFile();
    dataMock.realSheetname = SHEET_NAME;
    step.prepareNextOutputFile();
  }

  @Test
  public void testWriteUsingTemplateWithFormatting() throws Exception {
    assertTrue( step.init( metaMock, dataMock ) );
    String path = Files.createTempDirectory( "" ).toFile().getAbsolutePath() + File.separator + "formatted.xlsx";

    dataMock.fieldnrs = new int[] { 0 };
    dataMock.linkfieldnrs = new int[] { -1 };
    dataMock.commentfieldnrs = new int[] { -1 };
    dataMock.createNewFile = true;
    dataMock.realTemplateFileName = getTemplateWithFormattingXlsx().getFile();
    dataMock.realSheetname = "TicketData";
    dataMock.inputRowMeta = mock( RowMetaInterface.class );

    ExcelWriterStepField field = new ExcelWriterStepField();
    ValueMetaInterface vmi = mock( ValueMetaInteger.class );
    doReturn( ValueMetaInterface.TYPE_INTEGER ).when( vmi ).getType();
    doReturn( "name" ).when( vmi ).getName();
    doReturn( 12.0 ).when( vmi ).getNumber( any() );

    doReturn( path ).when( step ).buildFilename( 0 );
    doReturn( true ).when( metaMock ).isTemplateEnabled();
    doReturn( true ).when( metaMock ).isStreamingData();
    doReturn( false ).when( metaMock ).isHeaderEnabled();
    doReturn( XLSX ).when( metaMock ).getExtension();
    doReturn( new ExcelWriterStepField[] { field } ).when( metaMock ).getOutputFields();

    doReturn( 10 ).when( dataMock.inputRowMeta ).size();
    doReturn( vmi ).when( dataMock.inputRowMeta ).getValueMeta( anyInt() );

    step.prepareNextOutputFile();

    assertTrue( "must use streaming", dataMock.sheet instanceof SXSSFSheet );
    dataMock.posY = 1;
    dataMock.sheet = spy( dataMock.sheet );
    step.writeNextLine( new Object[] { 12 } );

    verify( dataMock.sheet, times( 0 ) ).createRow( 1 );
    verify( dataMock.sheet ).getRow( 1 );
  }

  @Test
  public void testValueBigNumber() throws Exception {

    ValueMetaInterface vmi = mock( ValueMetaBigNumber.class, new DefaultAnswerThrowsException() );
    Object vObj = new Object();
    doReturn( ValueMetaInterface.TYPE_BIGNUMBER ).when( vmi ).getType();
    doReturn( "value_bigNumber" ).when( vmi ).getName();
    doReturn( Double.MAX_VALUE ).when( vmi ).getNumber( any() );

    testBaseXlsx( vmi, vObj, false, false );
  }

  @Test
  public void testValueBinary() throws Exception {

    ValueMetaInterface vmi = mock( ValueMetaBinary.class, new DefaultAnswerThrowsException() );
    Object vObj = new Object();
    doReturn( ValueMetaInterface.TYPE_BINARY ).when( vmi ).getType();
    doReturn( "value_binary" ).when( vmi ).getName();
    doReturn( "a1b2c3d4e5f6g7h8i9j0" ).when( vmi ).getString( any() );

    testBaseXlsx( vmi, vObj, false, false );
  }

  @Test
  public void testValueBoolean() throws Exception {

    ValueMetaInterface vmi = mock( ValueMetaInteger.class, new DefaultAnswerThrowsException() );
    Object vObj = new Object();
    doReturn( ValueMetaInterface.TYPE_BOOLEAN ).when( vmi ).getType();
    doReturn( "value_bool" ).when( vmi ).getName();
    doReturn( Boolean.FALSE ).when( vmi ).getBoolean( any() );

    testBaseXlsx( vmi, vObj, false, false );
  }

  @Test
  public void testValueDate() throws Exception {

    ValueMetaInterface vmi = mock( ValueMetaDate.class, new DefaultAnswerThrowsException() );
    Object vObj = new Object();
    doReturn( ValueMetaInterface.TYPE_DATE ).when( vmi ).getType();
    doReturn( "value_date" ).when( vmi ).getName();
    doReturn( new Date() ).when( vmi ).getDate( any() );

    testBaseXlsx( vmi, vObj, false, false );
  }

  @Test
  public void testValueInteger() throws Exception {

    ValueMetaInterface vmi = mock( ValueMetaInteger.class, new DefaultAnswerThrowsException() );
    Object vObj = new Object();
    doReturn( ValueMetaInterface.TYPE_INTEGER ).when( vmi ).getType();
    doReturn( "value_integer" ).when( vmi ).getName();
    doReturn( Double.MAX_VALUE ).when( vmi ).getNumber( any() );

    testBaseXlsx( vmi, vObj, false, false );
  }

  @Test
  public void testValueInternetAddress() throws Exception {

    ValueMetaInterface vmi = mock( ValueMetaInternetAddress.class, new DefaultAnswerThrowsException() );
    Object vObj = new Object();
    doReturn( ValueMetaInterface.TYPE_INET ).when( vmi ).getType();
    doReturn( "value_internetAddress" ).when( vmi ).getName();
    doReturn( "127.0.0.1" ).when( vmi ).getString( any() );

    testBaseXlsx( vmi, vObj, false, false );
  }

  @Test
  public void testValueNumber() throws Exception {

    ValueMetaInterface vmi = mock( ValueMetaNumber.class, new DefaultAnswerThrowsException() );
    Object vObj = new Object();
    doReturn( ValueMetaInterface.TYPE_NUMBER ).when( vmi ).getType();
    doReturn( "value_number" ).when( vmi ).getName();
    doReturn( Double.MIN_VALUE ).when( vmi ).getNumber( any() );

    testBaseXlsx( vmi, vObj, false, false );
  }

  @Test
  public void testValueString() throws Exception {

    ValueMetaInterface vmi = mock( ValueMetaString.class, new DefaultAnswerThrowsException() );
    Object vObj = new Object();
    doReturn( ValueMetaInterface.TYPE_STRING ).when( vmi ).getType();
    doReturn( "value_string" ).when( vmi ).getName();
    doReturn( "a_string" ).when( vmi ).getString( any() );

    testBaseXlsx( vmi, vObj, false, false );
  }

  @Test
  public void testValueTimestamp() throws Exception {

    ValueMetaInterface vmi = mock( ValueMetaTimestamp.class, new DefaultAnswerThrowsException() );
    Object vObj = new Object();
    doReturn( ValueMetaInterface.TYPE_INET ).when( vmi ).getType();
    doReturn( "value_timestamp" ).when( vmi ).getName();
    doReturn( "127.0.0.1" ).when( vmi ).getString( vObj );

    testBaseXlsx( vmi, vObj, false, false );
  }

  @Test
  public void test_Xlsx_Stream_NoTemplate() throws Exception {

    ValueMetaInterface vmi = mock( ValueMetaTimestamp.class, new DefaultAnswerThrowsException() );
    Object vObj = new Object();
    doReturn( ValueMetaInterface.TYPE_INET ).when( vmi ).getType();
    doReturn( "value_timestamp" ).when( vmi ).getName();
    doReturn( "127.0.0.1" ).when( vmi ).getString( vObj );

    testBaseXlsx( vmi, vObj, true, false );
  }

  @Test
  public void test_Xlsx_NoStream_NoTemplate() throws Exception {

    ValueMetaInterface vmi = mock( ValueMetaTimestamp.class, new DefaultAnswerThrowsException() );
    Object vObj = new Object();
    doReturn( ValueMetaInterface.TYPE_INET ).when( vmi ).getType();
    doReturn( "value_timestamp" ).when( vmi ).getName();
    doReturn( "127.0.0.1" ).when( vmi ).getString( vObj );

    testBaseXlsx( vmi, vObj, false, false );
  }

  @Test
  public void test_Xlsx_Stream_Template() throws Exception {

    ValueMetaInterface vmi = mock( ValueMetaTimestamp.class, new DefaultAnswerThrowsException() );
    Object vObj = new Object();
    doReturn( ValueMetaInterface.TYPE_INET ).when( vmi ).getType();
    doReturn( "value_timestamp" ).when( vmi ).getName();
    doReturn( "127.0.0.1" ).when( vmi ).getString( vObj );

    testBaseXlsx( vmi, vObj, true, true );
  }

  @Test
  public void test_Xlsx_NoStream_Template() throws Exception {

    ValueMetaInterface vmi = mock( ValueMetaTimestamp.class, new DefaultAnswerThrowsException() );
    Object vObj = new Object();
    doReturn( ValueMetaInterface.TYPE_INET ).when( vmi ).getType();
    doReturn( "value_timestamp" ).when( vmi ).getName();
    doReturn( "127.0.0.1" ).when( vmi ).getString( vObj );

    testBaseXlsx( vmi, vObj, false, true );
  }

  @Test
  public void test_Xls_NoTemplate() throws Exception {

    ValueMetaInterface vmi = mock( ValueMetaTimestamp.class, new DefaultAnswerThrowsException() );
    Object vObj = new Object();
    doReturn( ValueMetaInterface.TYPE_INET ).when( vmi ).getType();
    doReturn( "value_timestamp" ).when( vmi ).getName();
    doReturn( "127.0.0.1" ).when( vmi ).getString( vObj );

    testBaseXls( vmi, vObj, false );
  }

  @Test
  public void test_Xls_Template() throws Exception {

    ValueMetaInterface vmi = mock( ValueMetaTimestamp.class, new DefaultAnswerThrowsException() );
    Object vObj = new Object();
    doReturn( ValueMetaInterface.TYPE_INET ).when( vmi ).getType();
    doReturn( "value_timestamp" ).when( vmi ).getName();
    doReturn( "127.0.0.1" ).when( vmi ).getString( vObj );

    testBaseXls( vmi, vObj, true );
  }

  /**
   * <p>The base for testing if a field of a specific type is correctly handled for an XLSX.</p>
   *
   * @param vmi               {@link ValueMetaInterface}'s instance to be used
   * @param vObj              the {@link Object} to be used as the value
   * @param isStreaming       if it's to use streaming
   * @param isTemplateEnabled if it's to use a template
   */
  private void testBaseXlsx( ValueMetaInterface vmi, Object vObj, boolean isStreaming, boolean isTemplateEnabled )
    throws Exception {
    testBase( vmi, vObj, XLSX, DOT_XLSX, isStreaming, isTemplateEnabled );
  }

  /**
   * <p>The base for testing if a field of a specific type is correctly handled for an XLS.</p>
   *
   * @param vmi               {@link ValueMetaInterface}'s instance to be used
   * @param vObj              the {@link Object} to be used as the value
   * @param isTemplateEnabled if it's to use a template
   */
  private void testBaseXls( ValueMetaInterface vmi, Object vObj, boolean isTemplateEnabled )
    throws Exception {

    testBase( vmi, vObj, XLS, DOT_XLS, false, isTemplateEnabled );
  }

  /**
   * <p>The base for testing if a field of a specific type is correctly handled.</p>
   *
   * @param vmi               {@link ValueMetaInterface}'s instance to be used
   * @param vObj              the {@link Object} to be used as the value
   * @param extension         the extension to be used
   * @param isStreaming       if it's to use streaming
   * @param isTemplateEnabled if it's to use a template
   */
  private void testBase( ValueMetaInterface vmi, Object vObj, String extension, String dotExtension,
                         boolean isStreaming,
                         boolean isTemplateEnabled )
    throws Exception {
    Object[] vObjArr = { vObj };
    assertTrue( step.init( metaMock, dataMock ) );
    File tempFile = File.createTempFile( extension, dotExtension );
    tempFile.deleteOnExit();
    String path = tempFile.getAbsolutePath();

    if ( isTemplateEnabled ) {
      dataMock.realTemplateFileName = getClass().getResource( "template_test" + dotExtension ).getFile();
    }

    dataMock.fieldnrs = new int[] { 0 };
    dataMock.linkfieldnrs = new int[] { -1 };
    dataMock.commentfieldnrs = new int[] { -1 };
    dataMock.createNewFile = true;
    dataMock.realSheetname = SHEET_NAME;
    dataMock.inputRowMeta = mock( RowMetaInterface.class );

    doReturn( path ).when( step ).buildFilename( 0 );
    doReturn( isTemplateEnabled ).when( metaMock ).isTemplateEnabled();
    doReturn( isStreaming ).when( metaMock ).isStreamingData();
    doReturn( false ).when( metaMock ).isHeaderEnabled();
    doReturn( extension ).when( metaMock ).getExtension();
    ExcelWriterStepField field = new ExcelWriterStepField();
    doReturn( new ExcelWriterStepField[] { field } ).when( metaMock ).getOutputFields();

    doReturn( 1 ).when( dataMock.inputRowMeta ).size();
    doReturn( vmi ).when( dataMock.inputRowMeta ).getValueMeta( anyInt() );

    step.prepareNextOutputFile();

    assertNull( dataMock.sheet.getRow( 1 ) );

    // Unfortunately HSSFSheet is final and cannot be mocked, so we'll skip some validations
    dataMock.posY = 1;
    if ( null != dataMock.sheet && !( dataMock.sheet instanceof HSSFSheet ) ) {
      dataMock.sheet = spy( dataMock.sheet );
    }

    step.writeNextLine( vObjArr );

    if ( null != dataMock.sheet && !( dataMock.sheet instanceof HSSFSheet ) ) {
      verify( step ).writeField( eq( vObj ), eq( vmi ), eq( field ), any( Row.class ), eq( 0 ), any(), eq( 0 ),
        eq( Boolean.FALSE ) );

      verify( dataMock.sheet ).createRow( anyInt() );
      verify( dataMock.sheet ).getRow( 1 );
    }

    assertNotNull( dataMock.sheet.getRow( 1 ) );
  }

  /**
   * <p>Class to be used when mocking an Object so that, if not explicitly specified, any method called will throw an
   * exception.</p>
   */
  private static class DefaultAnswerThrowsException implements Answer<Object> {
    @Override
    public Object answer( InvocationOnMock invocation ) throws Throwable {
      throw new RuntimeException( "This method (" + invocation.getMethod() + ") shouldn't have been called." );
    }
  }

  private Workbook createWorkbook( FileObject file ) throws Exception {
    Workbook wb = null;
    OutputStream os = null;
    try {
      os = file.getContent().getOutputStream();
      wb = new HSSFWorkbook();
      wb.createSheet( SHEET_NAME );
      wb.write( os );
    } finally {
      os.flush();
      os.close();
    }
    return wb;
  }
}
