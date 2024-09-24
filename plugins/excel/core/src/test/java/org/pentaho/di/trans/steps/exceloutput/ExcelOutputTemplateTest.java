/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.exceloutput;

import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Test for case using template file
 *
 * @author Pavel Sakun
 */
public class ExcelOutputTemplateTest {
  private static StepMockHelper<ExcelOutputMeta, ExcelOutputData> helper;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

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

  @AfterClass
  public static void cleanUp() {
    helper.cleanUp();
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
