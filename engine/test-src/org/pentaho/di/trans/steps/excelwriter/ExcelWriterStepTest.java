/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class ExcelWriterStepTest {

  private static final String SHEET_NAME = "Sheet1";

  @Test
  public void testProtectSheet() throws Exception {

    // populate
    File xlsFile = File.createTempFile( "testXLSProtect", ".xls" );
    HSSFWorkbook wb = createWorkbook( xlsFile );
    StepMockHelper<ExcelWriterStepMeta, ExcelWriterStepData> mockHelper =
      new StepMockHelper<ExcelWriterStepMeta, ExcelWriterStepData>(
        "Excel Writer Test", ExcelWriterStepMeta.class, ExcelWriterStepData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      mockHelper.logChannelInterface );
    ExcelWriterStep step =
      new ExcelWriterStep(
        mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );

    // test
    step.protectSheet( wb.getSheet( SHEET_NAME ), "aa" );

    // verify
    assertTrue( wb.getSheet( SHEET_NAME ).getProtect() );

  }

  private HSSFWorkbook createWorkbook( File file ) throws Exception {
    HSSFWorkbook wb = null;
    OutputStream os = null;
    try {
      os = new FileOutputStream( file );
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
