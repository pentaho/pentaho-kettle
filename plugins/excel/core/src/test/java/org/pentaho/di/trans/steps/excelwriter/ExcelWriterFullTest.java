/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2023-2024 by Hitachi Vantara : http://www.pentaho.com
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

import junit.framework.AssertionFailedError;
import org.apache.poi.ss.formula.DataValidationEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;
import org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Tests using full file reading and writing */
public class ExcelWriterFullTest {
  private StepMockHelper<ExcelWriterStepMeta, ExcelWriterStepData> helper;

  @BeforeClass
  public static void init() throws Exception {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() {
    helper = new StepMockHelper<>( "excel writer", ExcelWriterStepMeta.class, ExcelWriterStepData.class );
    when( helper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
        .thenReturn( helper.logChannelInterface );
    when( helper.trans.isRunning() ).thenReturn( true );
  }

  @Test
  public void testWriteStreamingTemplateOutFields() throws Exception {

    Path outDir = Files.createTempDirectory( ExcelWriterFullTest.class.getSimpleName() );
    try {
      ExcelWriterStepMeta meta = new ExcelWriterStepMeta();
      meta.setDefault();

      meta.setTemplateEnabled( true );
      meta.setTemplateFileName( ExcelWriterStepTest.getTemplateWithFormattingXlsx().getFile() );
      meta.setSheetname( "TicketData" );
      meta.setMakeSheetActive( true );
      meta.setIfSheetExists( ExcelWriterStepMeta.IF_SHEET_EXISTS_REUSE );
      meta.setHeaderEnabled( false );

      meta.setStreamingData( true );
      String outFile = outDir.resolve( "test_output" ).toString();
      meta.setFileName( outFile );
      meta.setExtension( "xlsx" );
      meta.setDateTimeFormat( "_yyyyMMdd" );

      ExcelWriterStepField[] fields = new ExcelWriterStepField[3];
      fields[0] = new ExcelWriterStepField( "str1", ValueMetaInterface.TYPE_STRING, null );
      fields[1] = new ExcelWriterStepField( "int2", ValueMetaInterface.TYPE_INTEGER, null );
      fields[2] = new ExcelWriterStepField( "int3", ValueMetaInterface.TYPE_INTEGER, "00000" );
      meta.setOutputFields( fields );

      meta.setStartingCell( "AW2" );

      RowSet inputs =
          createRowSet( createRowMeta( new ValueMetaString( "str1" ), new ValueMetaInteger( "int2" ),
            new ValueMetaInteger( "int3" ) ), row( "a", 1L, 1L ), row( "b", 2L, 2L ), row( "c", 3L, 3L ) );
      runStep( meta, inputs, 3 );

    } finally {
      removeDir( outDir );
    }
  }

  private void removeDir( Path outDir ) throws IOException {
    for ( File file : outDir.toFile().listFiles() ) {
      file.delete();
    }
    Files.delete( outDir );
  }

  @Test
  public void testWriteStreamingTemplate() throws Exception {

    Path outDir = Files.createTempDirectory( ExcelWriterFullTest.class.getSimpleName() );
    final String sheetName = "TicketData";
    try {
      ExcelWriterStepMeta meta = new ExcelWriterStepMeta();
      meta.setDefault();

      meta.setTemplateEnabled( true );
      meta.setTemplateFileName( ExcelWriterStepTest.getTemplateWithFormattingXlsx().getFile() );
      meta.setSheetname( sheetName );
      meta.setMakeSheetActive( true );
      meta.setIfSheetExists( ExcelWriterStepMeta.IF_SHEET_EXISTS_REUSE );
      meta.setHeaderEnabled( false );

      meta.setStreamingData( true );
      String outFile = outDir.resolve( "test_output" ).toString();
      meta.setFileName( outFile );
      meta.setExtension( "xlsx" );

      meta.setStartingCell( "AW2" );

      RowSet inputs =
          createRowSet( createRowMeta( new ValueMetaString( "str1" ), new ValueMetaInteger( "int2" ),
            new ValueMetaInteger( "int3" ) ), row( "a", 1L, 1L ), row( "b", 2L, 2L ), row( "c", 3L, 3L ) );
      runStep( meta, inputs, 3 );

      try ( Workbook wb = WorkbookFactory.create( new File( outFile + ".xlsx" ) ) ) {
        Sheet sheet = wb.getSheet( sheetName );
        assertEquals( "a", getCell( sheet, "AW2" ).getStringCellValue() );
        assertEquals( 1d, getCell( sheet, "AY2" ).getNumericCellValue(), 0 );
        assertEquals( "c", getCell( sheet, "AW4" ).getStringCellValue() );
        assertEquals( 3d, getCell( sheet, "AY4" ).getNumericCellValue(), 0 );
      }

    } finally {
      removeDir( outDir );
    }
  }

  @Test
  public void testDataValidationExtension() throws Exception {
    Path outDir = Files.createTempDirectory( ExcelWriterFullTest.class.getSimpleName() );

    // copy template to change it first
    Path origTemplate = Paths.get( ExcelWriterStepTest.getTemplateWithFormattingXlsx().toURI() );
    Path template = outDir.resolve( "validation_template.xlsx" );
    final String sheetName = "TicketData";

    // set up: add data validation constraint in a new template
    try ( InputStream in = KettleVFS.getInputStream( origTemplate.toString() );
        XSSFWorkbook wb = new XSSFWorkbook( in ) ) {

      XSSFSheet sheet = wb.getSheet( sheetName );
      DataValidationHelper validationHelper = sheet.getDataValidationHelper();
      // get a validation on B2
      DataValidationConstraint constr = validationHelper.createTextLengthConstraint( OperatorType.EQUAL, "2", null );
      CellRangeAddressList rangeList = new CellRangeAddressList( 1, 1, 1, 1 );
      DataValidation lenValidation = validationHelper.createValidation( constr, rangeList );
      sheet.addValidationData( lenValidation );

      try ( OutputStream out = KettleVFS.getOutputStream( template.toString(), false ) ) {
        wb.write( out );
      }
    }

    try {
      // actual test run
      ExcelWriterStepMeta meta = new ExcelWriterStepMeta();
      meta.setDefault();
      meta.setTemplateEnabled( true );
      meta.setTemplateFileName( template.toString() );
      meta.setSheetname( sheetName );
      meta.setStartingCell( "B2" );

      meta.setIfSheetExists( ExcelWriterStepMeta.IF_SHEET_EXISTS_REUSE );
      meta.setHeaderEnabled( false );

      meta.setStreamingData( true );
      String outFile = outDir.resolve( "test_output" ).toString();
      meta.setFileName( outFile );
      meta.setExtension( "xlsx" );

      meta.setExtendDataValidationRanges( true );

      RowSet inputs =
          createRowSet( createRowMeta( new ValueMetaString( "vals" ) ), row( "aa" ), row( "abc" ), row( "bb" ),
            row( "c" ) );
      runStep( meta, inputs, 4 );

      // check validation got extended to last row
      try ( InputStream in = KettleVFS.getInputStream( outFile + ".xlsx" ); XSSFWorkbook wb = new XSSFWorkbook( in ) ) {
        wb.setActiveSheet( 0 );
        XSSFFormulaEvaluator evaluatorProvider = new XSSFFormulaEvaluator( wb );
        DataValidationEvaluator evaluator = new DataValidationEvaluator( wb, evaluatorProvider );
        DataValidation validation = evaluator.getValidationForCell( new CellReference( sheetName + "!B4" ) );
        assertNotNull( "validation not extended", validation );
        DataValidationConstraint constraint = validation.getValidationConstraint();
        assertEquals( constraint.getValidationType(), ValidationType.TEXT_LENGTH );
        assertEquals( constraint.getOperator(), OperatorType.EQUAL );
        assertEquals( constraint.getFormula1(), "2" );
      }

    } finally {
      removeDir( outDir );
    }
  }

  @Test
  public void testCreateFolder() throws Exception {
    Path outDir = Files.createTempDirectory( ExcelWriterFullTest.class.getSimpleName() );
    try {
      ExcelWriterStepMeta meta = new ExcelWriterStepMeta();
      meta.setDefault();

      meta.setSheetname( "Sheet1" );
      meta.setMakeSheetActive( true );
      meta.setHeaderEnabled( false );

      meta.setStreamingData( true );
      String outFile = outDir.resolve( "out/there/test_output" ).toString();
      meta.setFileName( outFile );
      meta.setExtension( "xlsx" );

      meta.setStartingCell( "A1" );

      meta.setCreateParentFolders( true );

      RowSet inputs =
          createRowSet( createRowMeta( new ValueMetaString( "str1" ), new ValueMetaInteger( "int2" ),
            new ValueMetaInteger( "int3" ) ), row( "a", 1L, 1L ), row( "b", 2L, 2L ), row( "c", 3L, 3L ) );
      runStep( meta, inputs, 3 );

      File writtenFile = new File( outFile + ".xlsx" );
      assertTrue( writtenFile.exists() );

    } finally {
      removeDir( outDir.resolve( "out/there" ) );
      removeDir( outDir.resolve( "out" ) );
      removeDir( outDir );
    }
  }

  @Test
  public void testNullsBlank() throws Exception {
    Path outDir = Files.createTempDirectory( ExcelWriterFullTest.class.getSimpleName() );
    try {
      ExcelWriterStepMeta meta = new ExcelWriterStepMeta();
      meta.setDefault();

      meta.setSheetname( "Sheet1" );
      meta.setMakeSheetActive( true );
      meta.setHeaderEnabled( false );

      meta.setStreamingData( true );
      String outFile = outDir.resolve( "test_output" ).toString();
      meta.setFileName( outFile );
      meta.setExtension( "xlsx" );

      meta.setStartingCell( "A1" );

      meta.setRetainNullValues( true );

      RowSet inputs =
          createRowSet( createRowMeta( new ValueMetaString( "str1" ), new ValueMetaString( "int2" ),
            new ValueMetaInteger( "int3" ) ), row( "a", null, 1L ), row( "b", "not null", null ), row( "c", null, 3L ) );
      runStep( meta, inputs, 3 );

      try ( InputStream in = KettleVFS.getInputStream( outFile + ".xlsx" ); XSSFWorkbook wb = new XSSFWorkbook( in ) ) {
        XSSFSheet sheet1 = wb.getSheetAt( 0 );
        assertEquals( CellType.BLANK, getCell( sheet1, "B1" ).getCellType() );
        assertEquals( CellType.BLANK, getCell( sheet1, "C2" ).getCellType() );
      }

    } finally {
      removeDir( outDir );
    }
  }

  @Test
  public void testNullsEmptyStr() throws Exception {
    Path outDir = Files.createTempDirectory( ExcelWriterFullTest.class.getSimpleName() );
    try {
      ExcelWriterStepMeta meta = new ExcelWriterStepMeta();
      meta.setDefault();

      meta.setSheetname( "Sheet1" );
      meta.setMakeSheetActive( true );
      meta.setHeaderEnabled( false );

      meta.setStreamingData( true );
      String outFile = outDir.resolve( "test_output" ).toString();
      meta.setFileName( outFile );
      meta.setExtension( "xlsx" );

      meta.setStartingCell( "A1" );

      meta.setRetainNullValues( false );

      RowSet inputs =
          createRowSet( createRowMeta( new ValueMetaString( "str1" ), new ValueMetaString( "str2" ),
            new ValueMetaInteger( "int3" ) ), row( "a", null, 1L ), row( "b", "not null", null ), row( "c", null, 3L ) );
      runStep( meta, inputs, 3 );

      try ( InputStream in = KettleVFS.getInputStream( outFile + ".xlsx" ); XSSFWorkbook wb = new XSSFWorkbook( in ) ) {
        XSSFSheet sheet1 = wb.getSheetAt( 0 );
        Cell b1 = getCell( sheet1, "B1" );
        assertEquals( CellType.STRING, b1.getCellType() );
        assertEquals( "", b1.getStringCellValue() );
        Cell c2 = getCell( sheet1, "C2" );
        assertEquals( CellType.STRING, c2.getCellType() );
        assertEquals( "", c2.getStringCellValue() );
      }

    } finally {
      removeDir( outDir );
    }
  }

  public void runStep( ExcelWriterStepMeta meta, RowSet inputs, int expectedCalls ) throws KettleException {
    ExcelWriterStepData data = meta.getStepData();

    ExcelWriterStep step = meta.getStep( helper.stepMeta, data, 0, helper.transMeta, helper.trans );
    step.addRowSetToInputRowSets( inputs );
    step.setInputRowMeta( inputs.getRowMeta() );
    VariableSpace variables = new Variables();
    step.initializeVariablesFrom( variables );
    step.init( meta, data );

    int rowCount = 0;
    while ( step.processRow( meta, data ) ) {
      if ( ++rowCount > expectedCalls ) {
        throw new AssertionFailedError( String.format( "%d calls exceeded", expectedCalls ) );
      }
    }
    step.afterFinishProcessing( meta, data );
    step.dispose( meta, data );
    if ( rowCount < expectedCalls ) {
      long errors = step.getErrors();
      throw new AssertionFailedError(
          String.format( "%d calls expected, but got %d (there were %d errors)", expectedCalls, rowCount, errors ) );
    }
  }

  private static Cell getCell( Sheet sheet, String pos ) {
    CellReference cellRef = new CellReference( pos );
    Row row = sheet.getRow( cellRef.getRow() );
    return row.getCell( cellRef.getCol() );
  }

  public static RowMetaInterface createRowMeta( ValueMetaInterface... valueMetas ) {
    RowMeta rowMeta = new RowMeta();
    rowMeta.setValueMetaList( Arrays.asList( valueMetas ) );
    return rowMeta;
  }

  public static RowSet createRowSet( RowMetaInterface rowMeta, Object[]... inputRows ) {
    return createRowSet( rowMeta, Arrays.asList( inputRows ) );
  }

  public static Object[] row( Object... obj ) {
    return obj;
  }

  public static RowSet createRowSet( RowMetaInterface rowMeta, List<Object[]> inputRows ) {
    RowSet input = new BlockingRowSet( inputRows.size() + 1 );
    input.setRowMeta( rowMeta );
    inputRows.stream().forEach( row -> input.putRow( rowMeta, row ) );
    input.setDone();
    return input;
  }
}
