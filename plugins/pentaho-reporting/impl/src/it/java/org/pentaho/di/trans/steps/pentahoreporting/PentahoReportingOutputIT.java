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
package org.pentaho.di.trans.steps.pentahoreporting;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.datagrid.DataGridMeta;

import java.io.File;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by Yury_Ilyukevich on 5/13/2015.
 */
public class PentahoReportingOutputIT {

  @BeforeClass
  public static void setUp() throws Exception {
    KettleEnvironment.init();
  }

  private TransMeta transMeta;

  @Before
  public void prepareCommon() throws Exception {
    transMeta = new TransMeta( "src/it/resources/org/pentaho/di/trans/steps/pentahoreporting/pdi-13434.ktr" );
  }

  @Test
  public void createReport_Xls() throws Exception {
    runTransformation( PentahoReportingOutputMeta.ProcessorType.Excel );
  }

  @Test
  public void createReport_Xlsx() throws Exception {
    runTransformation( PentahoReportingOutputMeta.ProcessorType.Excel_2007 );
  }

  @Test
  public void createReport_Csv() throws Exception {
    runTransformation( PentahoReportingOutputMeta.ProcessorType.CSV );
  }

  @Test
  public void createReport_Pdf() throws Exception {
    runTransformation( PentahoReportingOutputMeta.ProcessorType.PDF );
  }

  @Test
  public void createReport_Rtf() throws Exception {
    runTransformation( PentahoReportingOutputMeta.ProcessorType.RTF );
  }

  private void runTransformation( PentahoReportingOutputMeta.ProcessorType processorType ) throws Exception {
    File tmp = File.createTempFile( "PentahoReportingOutputTest", "PentahoReportingOutputTest" );
    tmp.deleteOnExit();

    StepMeta outputStep = transMeta.findStep( "Data Grid" );
    DataGridMeta metaGrid = (DataGridMeta) outputStep.getStepMetaInterface();
    metaGrid.getDataLines().clear();
    metaGrid.getDataLines()
      .add( asList( "src/it/resources/org/pentaho/di/trans/steps/pentahoreporting/pdi-13434.prpt",
        tmp.getAbsolutePath() ) );

    StepMeta reportingStep = transMeta.findStep( "Pentaho Reporting Output" );
    PentahoReportingOutputMeta reportingMeta = (PentahoReportingOutputMeta) reportingStep.getStepMetaInterface();
    reportingMeta.setOutputProcessorType( processorType );

    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();

    assertEquals( 0, trans.getErrors() );
    assertTrue( tmp.length() > 0 );
    tmp.delete();
  }
}
