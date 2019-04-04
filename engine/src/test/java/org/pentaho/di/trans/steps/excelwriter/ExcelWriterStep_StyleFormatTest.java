/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Tests for applying Format and Style from cell (from a template) when writing fields
 */
public class ExcelWriterStep_StyleFormatTest {

  private StepMockHelper<ExcelWriterStepMeta, ExcelWriterStepData> stepMockHelper;
  private ExcelWriterStep step;
  private ExcelWriterStepMeta stepMeta;
  private ExcelWriterStepData stepData;

  @Before
  /**
   * Get mock helper
   */
  public void setUp() throws Exception {
    stepMockHelper =
      new StepMockHelper<ExcelWriterStepMeta, ExcelWriterStepData>(
        "Excel Writer Style Format Test", ExcelWriterStepMeta.class, ExcelWriterStepData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString() );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString(), any( Object[].class ) );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString(), (Throwable) anyObject() );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  @After
  /**
   * Clean-up objects
   */
  public void tearDown() {
    stepData.file = null;
    stepData.sheet = null;
    stepData.wb = null;
    stepData.clearStyleCache( 0 );

    stepMockHelper.cleanUp();
  }

  @Test
  /**
   * Test applying Format and Style from cell for XLS file format
   */
  public void testStyleFormatHssf() throws Exception {
    testStyleFormat( "xls" );
  }

  @Test
  /**
   * Test applying Format and Style from cell for XLSX file format
   */
  public void testStyleFormatXssf() throws Exception {
    testStyleFormat( "xlsx" );
  }

  /**
   * Test applying Format and Style from cell (from a template) when writing fields
   *
   * @param fileType
   * @throws Exception
   */
  private void testStyleFormat( String fileType ) throws Exception {
    setupStepMock( fileType );
    createStepMeta( fileType );
    createStepData( fileType );
    step.init( stepMeta, stepData );

    // We do not run transformation or executing the whole step
    // instead we just execute ExcelWriterStepData.writeNextLine() to write to Excel workbook object
    // Values are written in A2:D2 and A3:D3 rows
    List<Object[]> rows = createRowData();
    for ( int i = 0; i < rows.size(); i++ ) {
      step.writeNextLine( rows.get( i ) );
    }

    // Custom styles are loaded from G1 cell
    Row xlsRow = stepData.sheet.getRow( 0 );
    Cell baseCell = xlsRow.getCell( 6 );
    CellStyle baseCellStyle = baseCell.getCellStyle();
    DataFormat format = stepData.wb.createDataFormat();

    // Check style of the exported values in A3:D3
    xlsRow = stepData.sheet.getRow( 2 );
    for ( int i = 0; i < stepData.inputRowMeta.size(); i++ ) {
      Cell cell = xlsRow.getCell( i );
      CellStyle cellStyle = cell.getCellStyle();

      if ( i > 0 ) {
        assertEquals( cellStyle.getBorderRight(), baseCellStyle.getBorderRight() );
        assertEquals( cellStyle.getFillPattern(), baseCellStyle.getFillPattern() );
      } else {
        // cell A2/A3 has no custom style
        assertFalse( cellStyle.getBorderRight() == baseCellStyle.getBorderRight() );
        assertFalse( cellStyle.getFillPattern() == baseCellStyle.getFillPattern() );
      }

      if ( i != 1 ) {
        assertEquals( format.getFormat( cellStyle.getDataFormat() ), "0.00000" );
      } else {
        // cell B2/B3 use different format from the custom style
        assertEquals( format.getFormat( cellStyle.getDataFormat() ), "##0,000.0" );
      }
    }
  }

  /**
   * Setup any meta information for Excel Writer step
   *
   * @param fileType
   * @throws KettleException
   */
  private void createStepMeta( String fileType ) throws KettleException {
    stepMeta = new ExcelWriterStepMeta();
    stepMeta.setDefault();

    stepMeta.setFileName( "testExcel" );
    stepMeta.setExtension( fileType );
    stepMeta.setSheetname( "Sheet1" );
    stepMeta.setHeaderEnabled( true );
    stepMeta.setStartingCell( "A2" );

    // Try different combinations of specifying data format and style from cell
    //   1. Only format, no style
    //   2. No format, only style
    //   3. Format, and a different style without a format defined
    //   4. Format, and a different style with a different format defined but gets overridden
    ExcelWriterStepField[] outputFields = new ExcelWriterStepField[4];
    outputFields[0] = new ExcelWriterStepField( "col 1", ValueMetaFactory.getIdForValueMeta( "Integer" ), "0.00000" );
    outputFields[0].setStyleCell( "" );
    outputFields[1] = new ExcelWriterStepField( "col 2", ValueMetaFactory.getIdForValueMeta( "Number" ), "" );
    outputFields[1].setStyleCell( "G1" );
    outputFields[2] = new ExcelWriterStepField( "col 3", ValueMetaFactory.getIdForValueMeta( "BigNumber" ), "0.00000" );
    outputFields[2].setStyleCell( "F1" );
    outputFields[3] = new ExcelWriterStepField( "col 4", ValueMetaFactory.getIdForValueMeta( "Integer" ), "0.00000" );
    outputFields[3].setStyleCell( "G1" );
    stepMeta.setOutputFields( outputFields );
  }

  /**
   * Setup the data necessary for Excel Writer step
   *
   * @param fileType
   * @throws KettleException
   */
  private void createStepData( String fileType ) throws KettleException {
    stepData = new ExcelWriterStepData();
    stepData.inputRowMeta = step.getInputRowMeta().clone();
    stepData.outputRowMeta = step.getInputRowMeta().clone();

    // we don't run transformation so ExcelWriterStep.processRow() doesn't get executed
    // we populate the ExcelWriterStepData with bare minimum required values
    CellReference cellRef = new CellReference( stepMeta.getStartingCell() );
    stepData.startingRow = cellRef.getRow();
    stepData.startingCol = cellRef.getCol();
    stepData.posX = stepData.startingCol;
    stepData.posY = stepData.startingRow;

    int numOfFields = stepData.inputRowMeta.size();
    stepData.fieldnrs = new int[numOfFields];
    stepData.linkfieldnrs = new int[numOfFields];
    stepData.commentfieldnrs = new int[numOfFields];
    for ( int i = 0; i < numOfFields; i++ ) {
      stepData.fieldnrs[i] = i;
      stepData.linkfieldnrs[i] = -1;
      stepData.commentfieldnrs[i] = -1;
    }

    // we avoid reading/writing Excel files, so ExcelWriterStep.prepareNextOutputFile() doesn't get executed
    // create Excel workbook object
    stepData.wb = stepMeta.getExtension().equalsIgnoreCase( "xlsx" ) ? new XSSFWorkbook() : new HSSFWorkbook();
    stepData.sheet = stepData.wb.createSheet();
    stepData.file = null;
    stepData.clearStyleCache( numOfFields );

    // we avoid reading template file from disk
    // so set beforehand cells with custom style and formatting
    DataFormat format = stepData.wb.createDataFormat();
    Row xlsRow = stepData.sheet.createRow( 0 );

    // Cell F1 has custom style applied, used as template
    Cell cell = xlsRow.createCell( 5 );
    CellStyle cellStyle = stepData.wb.createCellStyle();
    cellStyle.setBorderRight( BorderStyle.THICK );
    cellStyle.setFillPattern( FillPatternType.FINE_DOTS );
    cell.setCellStyle( cellStyle );

    // Cell G1 has same style, but also a custom data format
    cellStyle = stepData.wb.createCellStyle();
    cellStyle.cloneStyleFrom( cell.getCellStyle() );
    cell = xlsRow.createCell( 6 );
    cellStyle.setDataFormat( format.getFormat( "##0,000.0" ) );
    cell.setCellStyle( cellStyle );
  }

  /**
   * Create ExcelWriterStep object and mock some of its required data
   *
   * @param fileType
   * @throws Exception
   */
  private void setupStepMock( String fileType ) throws Exception {
    step =
      new ExcelWriterStep(
        stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta, stepMockHelper.trans );
    step.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );

    List<Object[]> rows = createRowData();
    String[] outFields = new String[] { "col 1", "col 2", "col 3", "col 4" };
    RowSet inputRowSet = stepMockHelper.getMockInputRowSet( rows );
    RowMetaInterface inputRowMeta = createRowMeta();
    inputRowSet.setRowMeta( inputRowMeta );
    RowMetaInterface mockOutputRowMeta = mock( RowMetaInterface.class );
    when( mockOutputRowMeta.size() ).thenReturn( outFields.length );
    when( inputRowSet.getRowMeta() ).thenReturn( inputRowMeta );

    step.addRowSetToInputRowSets( inputRowSet );
    step.setInputRowMeta( inputRowMeta );
    step.addRowSetToOutputRowSets( inputRowSet );
  }

  /**
   * Create data rows that are passed to Excel Writer step
   *
   * @return
   * @throws Exception
   */
  private ArrayList<Object[]> createRowData() throws Exception {
    ArrayList<Object[]> rows = new ArrayList<Object[]>();
    Object[] row = new Object[] { new Long( 123456 ), new Double( 2.34e-4 ),
      new BigDecimal( "123456789.987654321" ), new Double( 504150 ) };
    rows.add( row );
    row = new Object[] { new Long( 1001001 ), new Double( 4.6789e10 ),
      new BigDecimal( 123123e-2 ), new Double( 12312300 ) };
    rows.add( row );
    return rows;
  }

  /**
   * Create meta information for rows that are passed to Excel Writer step
   *
   * @return
   * @throws KettleException
   */
  private RowMetaInterface createRowMeta() throws KettleException {
    RowMetaInterface rm = new RowMeta();
    try {
      ValueMetaInterface[] valuesMeta = {
        new ValueMetaInteger( "col 1" ),
        new ValueMetaNumber( "col 2" ),
        new ValueMetaBigNumber( "col 3" ),
        new ValueMetaNumber( "col 4" )
      };
      for ( int i = 0; i < valuesMeta.length; i++ ) {
        rm.addValueMeta( valuesMeta[i] );
      }
    } catch ( Exception ex ) {
      return null;
    }
    return rm;
  }
}
