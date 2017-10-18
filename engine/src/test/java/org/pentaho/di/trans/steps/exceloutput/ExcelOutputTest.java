/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import jxl.Sheet;
import jxl.Workbook;
import jxl.write.WritableCellFormat;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Created by Yury_Bakhmutski on 12/12/2016.
 */
public class ExcelOutputTest {
  private static StepMockHelper<ExcelOutputMeta, ExcelOutputData> helper;
  private static final String PATH_TO_XLS = buildFilePath();
  private static final File XLS_FILE = new File( PATH_TO_XLS );

  private static String buildFilePath() {
    String pathToPackage =
      ExcelOutputTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    String pathToClass =
      ExcelOutputTest.class.getPackage().getName().replace( ".", File.separator );
    String fileName = "append-test";
    return pathToPackage + pathToClass + File.separator + fileName + ".xls";
  }

  @BeforeClass
  public static void setUp() throws KettleException {
    KettleEnvironment.init();
    helper =
      new StepMockHelper<ExcelOutputMeta, ExcelOutputData>( "ExcelOutputTest", ExcelOutputMeta.class,
        ExcelOutputData.class );
    when( helper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      helper.logChannelInterface );
    when( helper.trans.isRunning() ).thenReturn( true );
  }

  @Test
  /**
   * Tests http://jira.pentaho.com/browse/PDI-14420 issue
   */
  public void testExceptionClosingWorkbook() throws Exception {

    ValueMetaInterface vmi = new ValueMetaString( "new_row" );

    ExcelOutputData data = new ExcelOutputData();
    int[] ints = { 0 };
    data.fieldnrs = ints;
    RowMeta rowMetaToBeReturned = Mockito.spy( new RowMeta() );
    rowMetaToBeReturned.addValueMeta( 0, vmi );

    data.previousMeta = rowMetaToBeReturned;
    ExcelOutput excelOutput =
      Mockito.spy( new ExcelOutput( helper.stepMeta, data, 0, helper.transMeta, helper.trans ) );
    excelOutput.first = false;

    Object[] row = { new Date() };
    doReturn( row ).when( excelOutput ).getRow();
    doReturn( rowMetaToBeReturned ).when( excelOutput ).getInputRowMeta();

    ExcelOutputMeta meta = createStepMeta();

    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );

    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    excelOutput.dispose( meta, data );

    Workbook workbook = Workbook.getWorkbook( XLS_FILE );
    Sheet sheet = workbook.getSheet( 0 );
    int rows = sheet.getRows();
    Assert.assertSame( rows, 2 );

  }

  @Test
  /**
   * Tests http://jira.pentaho.com/browse/PDI-13487 issue
   */
  public void testClosingFile() throws Exception {

    ValueMetaInterface vmi = new ValueMetaString( "new_row" );

    ExcelOutputData data = new ExcelOutputData();
    int[] ints = { 0 };
    data.fieldnrs = ints;
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

    ExcelOutputMeta meta = createStepMeta();
    meta.setSplitEvery( 1 );

    excelOutput.init( meta, data );
    excelOutput.processRow( meta, data );
    Assert.assertNull( data.formats.get( testColumnName ) );

  }

  private ExcelOutputMeta createStepMeta() throws IOException {
    ExcelOutputMeta meta = new ExcelOutputMeta();
    meta.setFileName( PATH_TO_XLS );
    meta.setTemplateEnabled( false );
    meta.setAppend( true );
    ExcelField[] excelFields = { new ExcelField() };
    meta.setOutputFields( excelFields );

    return meta;
  }

  @AfterClass
  public static void destroy() throws KettleException {
    XLS_FILE.delete();
  }
}
