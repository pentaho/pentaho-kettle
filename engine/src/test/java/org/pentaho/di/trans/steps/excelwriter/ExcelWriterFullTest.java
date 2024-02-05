/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2023 by Hitachi Vantara : http://www.pentaho.com
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

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
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import junit.framework.AssertionFailedError;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Less mocky tests */
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
      for ( File file : outDir.toFile().listFiles() ) {
        file.delete();
      }
      Files.delete( outDir );
    }
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
      for ( File file : outDir.toFile().listFiles() ) {
        file.delete();
      }
      Files.delete( outDir );
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
      throw new AssertionFailedError( String.format( "%d calls expected, but got %d", expectedCalls, rowCount ) );
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
    // just to prevent formatter getting messing up on the {}
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
