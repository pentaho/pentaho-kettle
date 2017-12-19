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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

/**
 * Test for case using template file
 *
 * @author Pavel Sakun
 */
public class ExcelOutputTemplateTest {
  private static StepMockHelper<ExcelOutputMeta, ExcelOutputData> helper;

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
  public void testExceptionClosingWorkbook() throws Exception {
    ExcelOutput excelOutput =
        new ExcelOutput( helper.stepMeta, helper.stepDataInterface, 0, helper.transMeta, helper.trans );
    ExcelOutputMeta meta = createStepMeta();
    excelOutput.init( meta, helper.initStepDataInterface );
    Assert.assertEquals( "Step init error.", 0, excelOutput.getErrors() );
    helper.initStepDataInterface.formats = new HashMap<>();
    excelOutput.dispose( meta, helper.initStepDataInterface );
    Assert.assertEquals( "Step dispose error", 0, excelOutput.getErrors() );
  }

  private ExcelOutputMeta createStepMeta() throws IOException {
    File tempFile = File.createTempFile( "PDI_tmp", ".tmp" );
    tempFile.deleteOnExit();

    final ExcelOutputMeta meta = new ExcelOutputMeta();
    meta.setFileName( tempFile.getAbsolutePath() );
    meta.setTemplateEnabled( true );
    meta.setTemplateFileName( getClass().getResource( "chart-template.xls" ).getFile() );

    return meta;
  }
}
