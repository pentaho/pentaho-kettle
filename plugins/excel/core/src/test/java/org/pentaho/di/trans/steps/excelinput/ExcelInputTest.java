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
import static org.junit.Assert.assertNull;
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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class ExcelInputTest {
  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  StepMockHelper<ExcelInputMeta, ExcelInputData> mockHelper;
  ExcelInput excelInput;
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
    excelInput = setupInput( mockHelper );
    excelInputMeta = mockHelper.processRowsStepMetaInterface;
    excelInput.setStepMetaInterface( mockHelper.processRowsStepMetaInterface );
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
    Mockito.doReturn( new String[]{"sheet0"} ).when( excelInputMeta ).getSheetName();

    excelInput.processingWorkbook( fields, excelInputMeta, workbook );

    assertEquals( 0, fields.size() );
  }

  @Test
  public void processingWorkbookForValidSheetSelectedWithoutReadingAllSheetsTest() throws Exception {
    Mockito.doReturn( false ).when( excelInputMeta ).readAllSheets();

    Mockito.doReturn( "sheet1" ).when( sheet ).getName();
    Mockito.doReturn( new String[]{"sheet1"} ).when( excelInputMeta ).getSheetName();

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
    excelInput.setStepMetaInterface( excelInputMeta );
    Mockito.doReturn( new String[]{"testFile"} ).when( excelInputMeta ).getFilePaths( any( Bowl.class ), any() );
    JSONObject jsonObject = excelInput.doAction( "getFiles", excelInputMeta,
        mockHelper.transMeta, mockHelper.trans, new HashMap<>() );

    assertNotNull( jsonObject );
    assertEquals( StepInterface.SUCCESS_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( jsonObject.get( "files" ) );
  }

  @Test
  public void getEmptyFilesActionTest() {
    Mockito.doReturn( new String[]{} ).when( excelInputMeta ).getFilePaths( any( Bowl.class ), any() );
    JSONObject jsonObject = excelInput.doAction( "getFiles", excelInputMeta,
        mockHelper.transMeta, mockHelper.trans, new HashMap<>() );

    assertNotNull( jsonObject );
    assertEquals( StepInterface.SUCCESS_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
    assertEquals( BaseMessages.getString( ExcelInputTest.class, "ExcelInputDialog.NoFilesFound.DialogMessage" ), jsonObject.get( "message" ) );
    assertNull( jsonObject.get( "files" ) );
  }

  @Test
  public void getSheetsActionTest() {
    FileInputList fileList = Mockito.mock( FileInputList.class );
    FileObject fileObject = Mockito.mock( FileObject.class );

    Mockito.doReturn( fileList ).when( excelInputMeta ).getFileList( any( Bowl.class ), any() );
    List<FileObject> fileInputListList = new ArrayList<>();
    fileInputListList.add( fileObject );
    Mockito.doReturn( fileInputListList ).when( fileList ).getFiles();
    Mockito.doReturn( 1 ).when( workbook ).getNumberOfSheets();
    Mockito.doReturn( "Sheet 1"  ).when( sheet ).getName();

    try ( MockedStatic<WorkbookFactory> workbookFactoryMockedStatic = mockStatic( WorkbookFactory.class ) ) {
      try ( MockedStatic<KettleVFS> kettleVFSMockedStatic = mockStatic( KettleVFS.class ) ) {
        workbookFactoryMockedStatic.when( () -> WorkbookFactory.getWorkbook( any( Bowl.class ), any(), any(), any(),
          any() ) ).thenReturn( workbook );
        kettleVFSMockedStatic.when( () -> KettleVFS.getFilename( any() ) ).thenReturn( "fileObject" );

        JSONObject jsonObject = excelInput.doAction( "getSheets", excelInputMeta,
            mockHelper.transMeta, mockHelper.trans, new HashMap<>() );

        assertNotNull( jsonObject );
        assertEquals( StepInterface.SUCCESS_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
        assertNotNull( jsonObject.get( "sheets" ) );
      }
    }
  }

  @Test
  public void getEmptySheetsActionTest() {
    FileInputList fileList = Mockito.mock( FileInputList.class );

    Mockito.doReturn( fileList ).when( mockHelper.processRowsStepMetaInterface ).getFileList( any( Bowl.class ), any() );
    Mockito.doReturn( new ArrayList<>() ).when( fileList ).getFiles();

    try ( MockedStatic<WorkbookFactory> workbookFactoryMockedStatic = mockStatic( WorkbookFactory.class ) ) {
      try ( MockedStatic<KettleVFS> kettleVFSMockedStatic = mockStatic( KettleVFS.class ) ) {
        workbookFactoryMockedStatic.when( () -> WorkbookFactory.getWorkbook( any( Bowl.class ), any(), any(), any(),
          any() ) ).thenReturn( workbook );
        kettleVFSMockedStatic.when( () -> KettleVFS.getFilename( any() ) ).thenReturn( "fileObject" );

        JSONObject jsonObject = excelInput.doAction( "getSheets", excelInputMeta,
            mockHelper.transMeta, mockHelper.trans, new HashMap<>() );

        assertNotNull( jsonObject );
        assertEquals( StepInterface.SUCCESS_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
        assertEquals( BaseMessages.getString( ExcelInputTest.class, "ExcelInputDialog.UnableToFindSheets.DialogMessage" ), jsonObject.get( "message" ) );
      }
    }
  }

  @Test
  public void getFailedSheetsActionTest() {
    FileInputList fileList = Mockito.mock( FileInputList.class );
    FileObject fileObject = Mockito.mock( FileObject.class );

    Mockito.doReturn( fileList ).when( excelInputMeta ).getFileList( any( Bowl.class ), any() );
    List<FileObject> fileInputListList = new ArrayList<>();
    fileInputListList.add( fileObject );
    Mockito.doReturn( fileInputListList ).when( fileList ).getFiles();

    try ( MockedStatic<WorkbookFactory> workbookFactoryMockedStatic = mockStatic( WorkbookFactory.class ) ) {
      try ( MockedStatic<KettleVFS> kettleVFSMockedStatic = mockStatic( KettleVFS.class ) ) {
        workbookFactoryMockedStatic.when( () -> WorkbookFactory.getWorkbook( any( Bowl.class ), any(), any(), any(),
          any() ) ).thenThrow( new KettleException( BaseMessages
            .getString( ExcelInputTest.class, "ExcelInputDialog.ErrorReadingFile.DialogMessage" ) ) );
        kettleVFSMockedStatic.when( () -> KettleVFS.getFilename( any() ) ).thenReturn( "fileObject" );

        JSONObject jsonObject = excelInput.doAction( "getSheets", excelInputMeta,
            mockHelper.transMeta, mockHelper.trans, new HashMap<>() );

        assertNotNull( jsonObject );
        assertEquals( StepInterface.FAILURE_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
        String errorMessage = (String) jsonObject.get( "errorMessage" );
        assertNotNull( errorMessage );
        assertEquals( BaseMessages.getString( ExcelInputTest.class, "ExcelInputDialog.ErrorReadingFile.DialogMessage" ), errorMessage.trim() );
      }
    }
  }

  @Test
  public void getFieldsActionTest() {
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

        JSONObject jsonObject = excelInput.doAction( "getFields", excelInputMeta,
            mockHelper.transMeta, mockHelper.trans, new HashMap<>() );

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
    FileInputList fileList = Mockito.mock( FileInputList.class );

    Mockito.doReturn( fileList ).when( excelInputMeta ).getFileList( any( Bowl.class ), any() );
    Mockito.doReturn( new ArrayList<>() ).when( fileList ).getFiles();

    JSONObject jsonObject = excelInput.doAction( "getFields", excelInputMeta,
        mockHelper.transMeta, mockHelper.trans, new HashMap<>() );

    assertNotNull( jsonObject );
    assertEquals( StepInterface.SUCCESS_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
    assertEquals( BaseMessages.getString( ExcelInputTest.class, "ExcelInputDialog.UnableToFindFields.DialogMessage" ), jsonObject.get( "message" ) );
  }

  @Test
  public void getFailedFieldsActionTest() {
    FileInputList fileList = Mockito.mock( FileInputList.class );
    FileObject fileObject = Mockito.mock( FileObject.class );

    Mockito.doReturn( fileList ).when( excelInputMeta ).getFileList( any( Bowl.class ), any() );
    List<FileObject> fileInputListList = new ArrayList<>();
    fileInputListList.add( fileObject );
    Mockito.doReturn( fileInputListList ).when( fileList ).getFiles();

    try ( MockedStatic<WorkbookFactory> workbookFactoryMockedStatic = mockStatic( WorkbookFactory.class ) ) {
      try ( MockedStatic<KettleVFS> kettleVFSMockedStatic = mockStatic( KettleVFS.class ) ) {
        workbookFactoryMockedStatic.when( () -> WorkbookFactory.getWorkbook( any( Bowl.class ), any(), any(), any(),
          any() ) ).thenThrow( new KettleException( BaseMessages.getString( ExcelInputTest.class,
            "ExcelInputDialog.ErrorReadingFile.DialogMessage" ) ) );
        kettleVFSMockedStatic.when( () -> KettleVFS.getFilename( any() ) ).thenReturn( "fileObject" );

        JSONObject jsonObject = excelInput.doAction( "getFields", excelInputMeta,
            mockHelper.transMeta, mockHelper.trans, new HashMap<>() );

        assertNotNull( jsonObject );
        assertEquals( StepInterface.FAILURE_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
        String errorMessage = (String) jsonObject.get( "errorMessage" );
        assertNotNull( errorMessage );
        assertEquals( BaseMessages.getString( ExcelInputTest.class, "ExcelInputDialog.ErrorReadingFile.DialogMessage" ), errorMessage.trim() );
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
