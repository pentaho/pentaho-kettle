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


package org.pentaho.di.trans.steps.excelinput;

import org.apache.commons.vfs2.FileObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KCellType;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class ExcelInputHelperTest {
  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  StepMockHelper<ExcelInputMeta, ExcelInputData> mockHelper;
  ExcelInputHelper excelInput;
  ExcelInputMeta excelInputMeta;
  KWorkbook workbook;
  KSheet sheet;
  RowMeta fields;
  KCell cell;
  PluginRegistry pluginRegistry;
  PluginInterface plugin;
  int fieldCount;

  @Before
  public void setUp() throws KettlePluginException {
    mockHelper = new StepMockHelper<>( "excelInput", ExcelInputMeta.class, ExcelInputData.class );
    excelInput = new ExcelInputHelper();
    excelInputMeta = Mockito.mock( ExcelInputMeta.class );
    workbook = Mockito.mock( KWorkbook.class );
    sheet = Mockito.mock( KSheet.class );
    pluginRegistry = Mockito.mock( PluginRegistry.class );
    plugin = Mockito.mock( PluginInterface.class );

    Mockito.doReturn( 1 ).when( workbook ).getNumberOfSheets();
    Mockito.doReturn( sheet ).when( workbook ).getSheet( 0 );

    cell = Mockito.mock( KCell.class );
    fields = new RowMeta();
    fieldCount = 400;

    for ( int i = 0; i <= fieldCount - 1; i++ ) {
      Mockito.doReturn( cell ).when( sheet ).getCell( i, 0 );
      Mockito.doReturn( cell ).when( sheet ).getCell( i, 1 );
    }

    Mockito.doReturn( "1" ).when( cell ).getContents();
    Mockito.doReturn( KCellType.NUMBER ).when( cell ).getType();

    Mockito.doReturn( true ).when( excelInputMeta ).readAllSheets();
    int[] startColumn = {0};
    Mockito.doReturn( startColumn ).when( excelInputMeta ).getStartColumn();
    int[] startRow = {0};
    Mockito.doReturn( startRow ).when( excelInputMeta ).getStartRow();

    Mockito.doReturn( plugin ).when( pluginRegistry ).getPlugin( ValueMetaPluginType.class, "1" );
    Mockito.doReturn( Mockito.mock( ValueMetaInterface.class ) ).when( pluginRegistry ).
        loadClass( plugin, ValueMetaInterface.class );
    ValueMetaFactory.pluginRegistry = pluginRegistry;
  }

  @Test
  public void processingWorkbookTest() throws Exception {
    excelInput.processingWorkbook( fields, excelInputMeta, workbook );

    assertEquals( fieldCount, fields.size() );
  }

  @Test
  public void processingWorkbookForInvalidSheetSelectedTest() throws Exception {
    Mockito.doReturn( false ).when( excelInputMeta ).readAllSheets();

    Mockito.doReturn( "sheet1" ).when( sheet ).getName();
    Mockito.doReturn( new String[] { "sheet0" } ).when( excelInputMeta ).getSheetName();

    excelInput.processingWorkbook( fields, excelInputMeta, workbook );

    assertEquals( 0, fields.size() );
  }

  @Test
  public void processingWorkbookForValidSheetSelectedWithoutReadingAllSheetsTest() throws Exception {
    Mockito.doReturn( false ).when( excelInputMeta ).readAllSheets();

    Mockito.doReturn( "sheet1" ).when( sheet ).getName();
    Mockito.doReturn( new String[] { "sheet1" } ).when( excelInputMeta ).getSheetName();

    excelInput.processingWorkbook( fields, excelInputMeta, workbook );

    assertEquals( 400, fields.size() );
  }

  @Test
  public void processingWorkbookWithEmptyContentsTest() throws Exception {
    Mockito.doReturn( "" ).when( cell ).getContents();

    excelInput.processingWorkbook( fields, excelInputMeta, workbook );

    assertEquals( 0, fields.size() );
  }

  @Test
  public void processingWorkbookForLabelCellTest() throws Exception {
    Mockito.doReturn( plugin ).when( pluginRegistry ).getPlugin( ValueMetaPluginType.class, "2" );
    Mockito.doReturn( Mockito.mock( ValueMetaInterface.class ) ).when( pluginRegistry ).
      loadClass( plugin, ValueMetaInterface.class );
    ValueMetaFactory.pluginRegistry = pluginRegistry;

    Mockito.doReturn( "test" ).when( cell ).getContents();
    Mockito.doReturn( KCellType.LABEL ).when( cell ).getType();

    excelInput.processingWorkbook( fields, excelInputMeta, workbook );

    assertEquals( fieldCount, fields.size() );
  }

  @Test
  public void processingWorkbookForBooleanCellTest() throws Exception {
    Mockito.doReturn( plugin ).when( pluginRegistry ).getPlugin( ValueMetaPluginType.class, "4" );
    Mockito.doReturn( Mockito.mock( ValueMetaInterface.class ) ).when( pluginRegistry ).
      loadClass( plugin, ValueMetaInterface.class );
    ValueMetaFactory.pluginRegistry = pluginRegistry;

    Mockito.doReturn( "false" ).when( cell ).getContents();
    Mockito.doReturn( KCellType.BOOLEAN ).when( cell ).getType();

    excelInput.processingWorkbook( fields, excelInputMeta, workbook );

    assertEquals( fieldCount, fields.size() );
  }

  @Test
  public void processingWorkbookForDateCellTest() throws Exception {
    Mockito.doReturn( plugin ).when( pluginRegistry ).getPlugin( ValueMetaPluginType.class, "3" );
    Mockito.doReturn( Mockito.mock( ValueMetaInterface.class ) ).when( pluginRegistry ).
      loadClass( plugin, ValueMetaInterface.class );
    ValueMetaFactory.pluginRegistry = pluginRegistry;

    Mockito.doReturn( "28/03/2025" ).when( cell ).getContents();
    Mockito.doReturn( KCellType.DATE ).when( cell ).getType();

    excelInput.processingWorkbook( fields, excelInputMeta, workbook );

    assertEquals( fieldCount, fields.size() );
  }

  @Test
  public void getFilesActionTest() {
    HashMap<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );

    Mockito.doReturn( mockHelper.stepMeta ).when( mockHelper.transMeta ).findStep( "testStep" );
    Mockito.doReturn( excelInputMeta ).when( mockHelper.stepMeta ).getStepMetaInterface();
    Mockito.doReturn( new String[] { "testFile" } ).when( excelInputMeta ).getFilePaths( any( Bowl.class ), any() );

    JSONObject jsonObject = excelInput.stepAction( "getFiles", mockHelper.transMeta, queryParams );

    assertNotNull( jsonObject );
    assertEquals( StepInterface.SUCCESS_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( jsonObject.get( "files" ) );
  }

  @Test
  public void getSheetsActionTest() {
    HashMap<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );

    Mockito.doReturn( mockHelper.stepMeta ).when( mockHelper.transMeta ).findStep( "testStep" );
    Mockito.doReturn( excelInputMeta ).when( mockHelper.stepMeta ).getStepMetaInterface();

    FileInputList fileList = Mockito.mock( FileInputList.class );
    FileObject fileObject = Mockito.mock( FileObject.class );

    Mockito.doReturn( fileList ).when( excelInputMeta ).getFileList( any( Bowl.class ), any() );
    List<FileObject> fileInputListList = new ArrayList<>();
    fileInputListList.add( fileObject );
    Mockito.doReturn( fileInputListList ).when( fileList ).getFiles();
    Mockito.doReturn( 1 ).when( workbook ).getNumberOfSheets();
    Mockito.doReturn( "Sheet 1" ).when( sheet ).getName();

    try ( MockedStatic<WorkbookFactory> workbookFactoryMockedStatic = mockStatic( WorkbookFactory.class ) ) {
      try ( MockedStatic<KettleVFS> kettleVFSMockedStatic = mockStatic( KettleVFS.class ) ) {
        workbookFactoryMockedStatic.when( () -> WorkbookFactory.getWorkbook( any( Bowl.class ), any(), any(), any(),
          any() ) ).thenReturn( workbook );
        kettleVFSMockedStatic.when( () -> KettleVFS.getFilename( any() ) ).thenReturn( "fileObject" );

        JSONObject jsonObject = excelInput.stepAction( "getSheets", mockHelper.transMeta, queryParams );

        assertNotNull( jsonObject );
        assertEquals( StepInterface.SUCCESS_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
        assertNotNull( jsonObject.get( "sheets" ) );
      }
    }
  }

  @Test
  public void getEmptySheetsActionTest() {
    HashMap<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );

    Mockito.doReturn( mockHelper.stepMeta ).when( mockHelper.transMeta ).findStep( "testStep" );
    Mockito.doReturn( excelInputMeta ).when( mockHelper.stepMeta ).getStepMetaInterface();

    FileInputList fileList = Mockito.mock( FileInputList.class );

    Mockito.doReturn( fileList ).when( excelInputMeta ).getFileList( any( Bowl.class ), any() );
    Mockito.doReturn( new ArrayList<>() ).when( fileList ).getFiles();

    JSONObject jsonObject = excelInput.stepAction( "getSheets", mockHelper.transMeta, queryParams );

    assertNotNull( jsonObject );
    assertEquals( StepInterface.SUCCESS_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( jsonObject.get( "message" ) );
  }

  @Test
  public void getFailedSheetsActionTest() {
    HashMap<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );

    Mockito.doReturn( mockHelper.stepMeta ).when( mockHelper.transMeta ).findStep( "testStep" );
    Mockito.doReturn( excelInputMeta ).when( mockHelper.stepMeta ).getStepMetaInterface();

    FileInputList fileList = Mockito.mock( FileInputList.class );
    FileObject fileObject = Mockito.mock( FileObject.class );

    Mockito.doReturn( fileList ).when( excelInputMeta ).getFileList( any( Bowl.class ), any() );
    List<FileObject> fileInputListList = new ArrayList<>();
    fileInputListList.add( fileObject );
    Mockito.doReturn( fileInputListList ).when( fileList ).getFiles();

    try ( MockedStatic<WorkbookFactory> workbookFactoryMockedStatic = mockStatic( WorkbookFactory.class ) ) {
      try ( MockedStatic<KettleVFS> kettleVFSMockedStatic = mockStatic( KettleVFS.class ) ) {
        workbookFactoryMockedStatic.when( () -> WorkbookFactory.getWorkbook( any( Bowl.class ), any(), any(), any(),
          any() ) ).thenThrow( new KettleException( "Test exception" ) );
        kettleVFSMockedStatic.when( () -> KettleVFS.getFilename( any() ) ).thenReturn( "fileObject" );

        JSONObject jsonObject = excelInput.stepAction( "getSheets", mockHelper.transMeta, queryParams );

        assertNotNull( jsonObject );
        assertEquals( StepInterface.FAILURE_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
      }
    }
  }

  @Test
  public void getFieldsActionTest() {
    HashMap<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );

    Mockito.doReturn( mockHelper.stepMeta ).when( mockHelper.transMeta ).findStep( "testStep" );
    Mockito.doReturn( excelInputMeta ).when( mockHelper.stepMeta ).getStepMetaInterface();

    FileInputList fileList = Mockito.mock( FileInputList.class );
    FileObject fileObject = Mockito.mock( FileObject.class );

    Mockito.doReturn( fileList ).when( excelInputMeta ).getFileList( any( Bowl.class ), any() );
    List<FileObject> fileInputListList = new ArrayList<>();
    fileInputListList.add( fileObject );
    Mockito.doReturn( fileInputListList ).when( fileList ).getFiles();

    try ( MockedStatic<WorkbookFactory> workbookFactoryMockedStatic = mockStatic( WorkbookFactory.class ) ) {
      try ( MockedStatic<KettleVFS> kettleVFSMockedStatic = mockStatic( KettleVFS.class ) ) {
        workbookFactoryMockedStatic.when( () -> WorkbookFactory.getWorkbook( any( Bowl.class ), any(), any(), any(),
          any() ) ).thenReturn( workbook );
        kettleVFSMockedStatic.when( () -> KettleVFS.getFilename( any() ) ).thenReturn( "fileObject" );

        JSONObject jsonObject = excelInput.stepAction( "getFields", mockHelper.transMeta, queryParams );

        assertEquals( StepInterface.SUCCESS_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
        assertNotNull( jsonObject );
        JSONObject fieldsObject = (JSONObject) jsonObject.get( "fields" );
        assertNotNull( fieldsObject );
        JSONArray rows = (JSONArray) fieldsObject.get( "rows" );
        assertNotNull( rows );
        assertEquals( fieldCount, rows.size() );
      }
    }
  }

  @Test
  public void getEmptyFieldsActionTest() {
    HashMap<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );

    Mockito.doReturn( mockHelper.stepMeta ).when( mockHelper.transMeta ).findStep( "testStep" );
    Mockito.doReturn( excelInputMeta ).when( mockHelper.stepMeta ).getStepMetaInterface();

    FileInputList fileList = Mockito.mock( FileInputList.class );

    Mockito.doReturn( fileList ).when( excelInputMeta ).getFileList( any( Bowl.class ), any() );
    Mockito.doReturn( new ArrayList<>() ).when( fileList ).getFiles();

    JSONObject jsonObject = excelInput.stepAction( "getFields", mockHelper.transMeta, queryParams );

    assertNotNull( jsonObject );
    assertEquals( StepInterface.SUCCESS_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( jsonObject.get( "message" ) );
  }

  @Test
  public void getFailedFieldsActionTest() {
    HashMap<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );

    Mockito.doReturn( mockHelper.stepMeta ).when( mockHelper.transMeta ).findStep( "testStep" );
    Mockito.doReturn( excelInputMeta ).when( mockHelper.stepMeta ).getStepMetaInterface();

    FileInputList fileList = Mockito.mock( FileInputList.class );
    FileObject fileObject = Mockito.mock( FileObject.class );

    Mockito.doReturn( fileList ).when( excelInputMeta ).getFileList( any( Bowl.class ), any() );
    List<FileObject> fileInputListList = new ArrayList<>();
    fileInputListList.add( fileObject );
    Mockito.doReturn( fileInputListList ).when( fileList ).getFiles();

    try ( MockedStatic<WorkbookFactory> workbookFactoryMockedStatic = mockStatic( WorkbookFactory.class ) ) {
      try ( MockedStatic<KettleVFS> kettleVFSMockedStatic = mockStatic( KettleVFS.class ) ) {
        workbookFactoryMockedStatic.when( () -> WorkbookFactory.getWorkbook( any( Bowl.class ), any(), any(), any(),
          any() ) ).thenThrow( new KettleException( "Test exception" ) );
        kettleVFSMockedStatic.when( () -> KettleVFS.getFilename( any() ) ).thenReturn( "fileObject" );

        JSONObject jsonObject = excelInput.stepAction( "getFields", mockHelper.transMeta, queryParams );

        assertNotNull( jsonObject );
        assertEquals( "Action successful", jsonObject.get( StepInterface.ACTION_STATUS ) );
      }
    }
  }
  private ExcelInput setupInput( StepMockHelper<ExcelInputMeta, ExcelInputData> mockHelper ) {
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
        .thenReturn( mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );
    when( mockHelper.stepMeta.getStepMetaInterface() ).thenReturn( new ExcelInputMeta() );

    return new ExcelInput( mockHelper.stepMeta, mockHelper.stepDataInterface, 0,
        mockHelper.transMeta, mockHelper.trans );
  }
}
