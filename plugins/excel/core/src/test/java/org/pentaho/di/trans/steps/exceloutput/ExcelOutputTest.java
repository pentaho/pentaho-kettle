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

package org.pentaho.di.trans.steps.exceloutput;

import jxl.Workbook;
import jxl.write.WritableCellFormat;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Created by Yury_Bakhmutski on 12/12/2016.
 */
public class ExcelOutputTest {
  public static final String CREATED_SHEET_NAME = "Sheet1";
  private static StepMockHelper<ExcelOutputMeta, ExcelOutputData> helper;

  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setUp() throws KettleException {
    KettleEnvironment.init();
    helper =
      new StepMockHelper<>( "ExcelOutputTest", ExcelOutputMeta.class,
        ExcelOutputData.class );
    when( helper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      helper.logChannelInterface );
    when( helper.trans.isRunning() ).thenReturn( true );
  }

  @AfterClass
  public static void cleanUp() {
    helper.cleanUp();
  }

  private String buildFilePath() throws IOException {
    File outFile = File.createTempFile( "aaa", ".xls" );
    String fileFullPath = outFile.getCanonicalPath();
    outFile.delete();
    return fileFullPath;
  }

  private ExcelOutputMeta createStepMeta( String fileName, String templateFullPath, boolean isAppend )
    throws IOException {
    ExcelOutputMeta meta = new ExcelOutputMeta();
    meta.setFileName( fileName );
    if ( null == templateFullPath ) {
      meta.setTemplateEnabled( false );
    } else {
      meta.setTemplateEnabled( true );
      meta.setTemplateFileName( templateFullPath );
    }
    meta.setAppend( isAppend );
    ExcelField[] excelFields = { new ExcelField( "f1", ValueMetaInterface.TYPE_NUMBER, null ),
      new ExcelField( "f2", ValueMetaInterface.TYPE_STRING, null ) };
    meta.setOutputFields( excelFields );

    return meta;
  }

  @Test
  /**
   * Tests http://jira.pentaho.com/browse/PDI-14420 issue
   */
  public void testExceptionClosingWorkbook() throws Exception {

    ValueMetaInterface vmi = new ValueMetaString( "new_row" );

    ExcelOutputData data = new ExcelOutputData();
    data.fieldnrs = new int[] { 0 };
    RowMeta rowMetaToBeReturned = Mockito.spy( new RowMeta() );
    rowMetaToBeReturned.addValueMeta( 0, vmi );

    data.previousMeta = rowMetaToBeReturned;
    ExcelOutput excelOutput =
      Mockito.spy( new ExcelOutput( helper.stepMeta, data, 0, helper.transMeta, helper.trans ) );
    excelOutput.first = false;

    Object[] row = { new Date() };
    doReturn( row ).when( excelOutput ).getRow();
    doReturn( rowMetaToBeReturned ).when( excelOutput ).getInputRowMeta();

    String excelFileFullPath = buildFilePath();
    File excelFile = new File( excelFileFullPath );
    excelFile.deleteOnExit();
    ExcelOutputMeta meta = createStepMeta( excelFileFullPath, null, true );

    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );

    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );

    Workbook workbook = Workbook.getWorkbook( excelFile );
    Assert.assertEquals( 1, workbook.getSheets().length );
    Assert.assertEquals( 2, workbook.getSheet( CREATED_SHEET_NAME ).getRows() );
  }

  @Test
  /**
   * Tests http://jira.pentaho.com/browse/PDI-13487 issue
   */
  public void testClosingFile() throws Exception {

    ValueMetaInterface vmi = new ValueMetaString( "new_row" );

    ExcelOutputData data = new ExcelOutputData();
    data.fieldnrs = new int[] { 0 };
    String testColumnName = "testColumnName";
    data.formats.put( testColumnName, new WritableCellFormat() );
    RowMeta rowMetaToBeReturned = Mockito.spy( new RowMeta() );
    rowMetaToBeReturned.addValueMeta( 0, vmi );

    data.previousMeta = rowMetaToBeReturned;
    ExcelOutput excelOutput =
      Mockito.spy( new ExcelOutput( helper.stepMeta, data, 0, helper.transMeta, helper.trans ) );
    excelOutput.first = false;

    Object[] row = { new Date() };
    doReturn( row ).when( excelOutput ).getRow();
    doReturn( rowMetaToBeReturned ).when( excelOutput ).getInputRowMeta();
    doReturn( 1L ).when( excelOutput ).getLinesOutput();

    String excelFileFullPath = buildFilePath();
    File excelFile = new File( excelFileFullPath );
    excelFile.deleteOnExit();
    ExcelOutputMeta meta = createStepMeta( excelFileFullPath, null, true );

    meta.setSplitEvery( 1 );

    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    Assert.assertNull( data.formats.get( testColumnName ) );
  }

  @Test
  public void test_AppendNoTemplate() throws Exception {

    ValueMetaInterface vmi = new ValueMetaString( "new_row" );

    ExcelOutputData data = new ExcelOutputData();
    data.fieldnrs = new int[] { 0 };
    RowMeta rowMetaToBeReturned = Mockito.spy( new RowMeta() );
    rowMetaToBeReturned.addValueMeta( 0, vmi );

    data.previousMeta = rowMetaToBeReturned;
    ExcelOutput excelOutput =
      Mockito.spy( new ExcelOutput( helper.stepMeta, data, 0, helper.transMeta, helper.trans ) );
    excelOutput.first = false;

    Object[] row = { new Date() };
    doReturn( row ).when( excelOutput ).getRow();
    doReturn( rowMetaToBeReturned ).when( excelOutput ).getInputRowMeta();

    String excelFileFullPath = buildFilePath();
    File excelFile = new File( excelFileFullPath );
    excelFile.deleteOnExit();
    ExcelOutputMeta meta = createStepMeta( excelFileFullPath, null, true );

    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );

    Workbook workbook = Workbook.getWorkbook( excelFile );
    Assert.assertEquals( 1, workbook.getSheets().length );
    Assert.assertEquals( 4, workbook.getSheet( CREATED_SHEET_NAME ).getRows() );
  }

  @Test
  public void test_NoAppendNoTemplate() throws Exception {

    ValueMetaInterface vmi = new ValueMetaString( "new_row" );

    ExcelOutputData data = new ExcelOutputData();
    data.fieldnrs = new int[] { 0 };
    RowMeta rowMetaToBeReturned = Mockito.spy( new RowMeta() );
    rowMetaToBeReturned.addValueMeta( 0, vmi );

    data.previousMeta = rowMetaToBeReturned;
    ExcelOutput excelOutput =
      Mockito.spy( new ExcelOutput( helper.stepMeta, data, 0, helper.transMeta, helper.trans ) );
    excelOutput.first = false;

    Object[] row = { new Date() };
    doReturn( row ).when( excelOutput ).getRow();
    doReturn( rowMetaToBeReturned ).when( excelOutput ).getInputRowMeta();

    String excelFileFullPath = buildFilePath();
    File excelFile = new File( excelFileFullPath );
    excelFile.deleteOnExit();
    ExcelOutputMeta meta = createStepMeta( excelFileFullPath, null, false );

    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );

    Workbook workbook = Workbook.getWorkbook( excelFile );
    Assert.assertEquals( 1, workbook.getSheets().length );
    Assert.assertEquals( 1, workbook.getSheet( CREATED_SHEET_NAME ).getRows() );
  }

  @Test
  public void test_AppendTemplate() throws Exception {

    ValueMetaInterface vmi = new ValueMetaString( "new_row" );

    ExcelOutputData data = new ExcelOutputData();
    data.fieldnrs = new int[] { 0 };
    RowMeta rowMetaToBeReturned = Mockito.spy( new RowMeta() );
    rowMetaToBeReturned.addValueMeta( 0, vmi );

    data.previousMeta = rowMetaToBeReturned;
    ExcelOutput excelOutput =
      Mockito.spy( new ExcelOutput( helper.stepMeta, data, 0, helper.transMeta, helper.trans ) );
    excelOutput.first = false;

    Object[] row = { new Date() };
    doReturn( row ).when( excelOutput ).getRow();
    doReturn( rowMetaToBeReturned ).when( excelOutput ).getInputRowMeta();

    String excelFileFullPath = buildFilePath();
    File excelFile = new File( excelFileFullPath );
    excelFile.deleteOnExit();
    URL excelTemplateResource = this.getClass().getResource( "excel-template.xls" );
    String templateFullPath = excelTemplateResource.getFile();
    ExcelOutputMeta meta = createStepMeta( excelFileFullPath, templateFullPath, true );

    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );

    Workbook workbook = Workbook.getWorkbook( excelFile );
    Assert.assertEquals( 3, workbook.getSheets().length );
    // The existing sheets should be intact
    Assert.assertEquals( 4, workbook.getSheet( "SheetA" ).getRows() );
    Assert.assertEquals( 5, workbook.getSheet( "SheetB" ).getRows() );
    Assert.assertEquals( 4, workbook.getSheet( CREATED_SHEET_NAME ).getRows() );
  }

  @Test
  public void test_NoAppendTemplate() throws Exception {

    ValueMetaInterface vmi = new ValueMetaString( "new_row" );

    ExcelOutputData data = new ExcelOutputData();
    data.fieldnrs = new int[] { 0 };
    RowMeta rowMetaToBeReturned = Mockito.spy( new RowMeta() );
    rowMetaToBeReturned.addValueMeta( 0, vmi );

    data.previousMeta = rowMetaToBeReturned;
    ExcelOutput excelOutput =
      Mockito.spy( new ExcelOutput( helper.stepMeta, data, 0, helper.transMeta, helper.trans ) );
    excelOutput.first = false;

    Object[] row = { new Date() };
    doReturn( row ).when( excelOutput ).getRow();
    doReturn( rowMetaToBeReturned ).when( excelOutput ).getInputRowMeta();

    String excelFileFullPath = buildFilePath();
    File excelFile = new File( excelFileFullPath );
    excelFile.deleteOnExit();
    URL excelTemplateResource = this.getClass().getResource( "excel-template.xls" );
    String templateFullPath = excelTemplateResource.getFile();
    ExcelOutputMeta meta = createStepMeta( excelFileFullPath, templateFullPath, false );

    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );

    Workbook workbook = Workbook.getWorkbook( excelFile );
    Assert.assertEquals( 3, workbook.getSheets().length );
    // The existing sheets should be intact
    Assert.assertEquals( 4, workbook.getSheet( "SheetA" ).getRows() );
    Assert.assertEquals( 5, workbook.getSheet( "SheetB" ).getRows() );
    Assert.assertEquals( 1, workbook.getSheet( CREATED_SHEET_NAME ).getRows() );
  }

  @Test
  public void test_AppendTemplateWithSheet1() throws Exception {

    ValueMetaInterface vmi = new ValueMetaString( "new_row" );

    ExcelOutputData data = new ExcelOutputData();
    data.fieldnrs = new int[] { 0 };
    RowMeta rowMetaToBeReturned = Mockito.spy( new RowMeta() );
    rowMetaToBeReturned.addValueMeta( 0, vmi );

    data.previousMeta = rowMetaToBeReturned;
    ExcelOutput excelOutput =
      Mockito.spy( new ExcelOutput( helper.stepMeta, data, 0, helper.transMeta, helper.trans ) );
    excelOutput.first = false;

    Object[] row = { new Date() };
    doReturn( row ).when( excelOutput ).getRow();
    doReturn( rowMetaToBeReturned ).when( excelOutput ).getInputRowMeta();

    String excelFileFullPath = buildFilePath();
    File excelFile = new File( excelFileFullPath );
    excelFile.deleteOnExit();
    URL excelTemplateResource = this.getClass().getResource( "excel-template-withSheet1.xls" );
    String templateFullPath = excelTemplateResource.getFile();
    ExcelOutputMeta meta = createStepMeta( excelFileFullPath, templateFullPath, true );

    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );

    Workbook workbook = Workbook.getWorkbook( excelFile );
    Assert.assertEquals( 3, workbook.getSheets().length );
    // The existing sheets should be intact
    Assert.assertEquals( 4, workbook.getSheet( "SheetA" ).getRows() );
    Assert.assertEquals( 5, workbook.getSheet( "SheetB" ).getRows() );
    // There're already 4 rows
    Assert.assertEquals( 8, workbook.getSheet( CREATED_SHEET_NAME ).getRows() );
  }

  @Test
  public void test_NoAppendTemplateWithSheet1() throws Exception {

    ValueMetaInterface vmi = new ValueMetaString( "new_row" );

    ExcelOutputData data = new ExcelOutputData();
    data.fieldnrs = new int[] { 0 };
    RowMeta rowMetaToBeReturned = Mockito.spy( new RowMeta() );
    rowMetaToBeReturned.addValueMeta( 0, vmi );

    data.previousMeta = rowMetaToBeReturned;
    ExcelOutput excelOutput =
      Mockito.spy( new ExcelOutput( helper.stepMeta, data, 0, helper.transMeta, helper.trans ) );
    excelOutput.first = false;

    Object[] row = { new Date() };
    doReturn( row ).when( excelOutput ).getRow();
    doReturn( rowMetaToBeReturned ).when( excelOutput ).getInputRowMeta();

    String excelFileFullPath = buildFilePath();
    File excelFile = new File( excelFileFullPath );
    excelFile.deleteOnExit();
    URL excelTemplateResource = this.getClass().getResource( "excel-template-withSheet1.xls" );
    String templateFullPath = excelTemplateResource.getFile();
    ExcelOutputMeta meta = createStepMeta( excelFileFullPath, templateFullPath, false );

    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );
    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );

    Workbook workbook = Workbook.getWorkbook( excelFile );
    Assert.assertEquals( 3, workbook.getSheets().length );
    // The existing sheets should be intact
    Assert.assertEquals( 4, workbook.getSheet( "SheetA" ).getRows() );
    Assert.assertEquals( 5, workbook.getSheet( "SheetB" ).getRows() );
    // There're already 4 rows
    Assert.assertEquals( 5, workbook.getSheet( CREATED_SHEET_NAME ).getRows() );
  }
}
