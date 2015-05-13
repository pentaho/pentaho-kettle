/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.pentahoreporting;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.datagrid.DataGridMeta;

import java.io.File;
import java.util.Arrays;


/**
 * Created by Yury_Ilyukevich on 5/13/2015.
 */
public class PentahoReportingOutputTest {
  @Before
  public void setUp() throws Exception {
    KettleEnvironment.init();
  }

  @Test
  public void testTextOutput() throws Exception {

    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta( "testfiles/org/pentaho/di/trans/steps/pentahoreporting/pdi-13434.ktr" );
    StepMeta outputStep = transMeta.findStep( "Data Grid" );
    DataGridMeta metaGrid = (DataGridMeta) outputStep.getStepMetaInterface();
    metaGrid.getDataLines().clear();
    metaGrid.getDataLines().add( Arrays.asList( "testfiles/org/pentaho/di/trans/steps/pentahoreporting/pdi-13434.prpt",
      "testfiles/org/pentaho/di/trans/steps/pentahoreporting/pdi-13434.xls" ) );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();

    Assert.assertEquals( 0, trans.getErrors() );
    File tempFile = new File( "testfiles/org/pentaho/di/trans/steps/pentahoreporting/pdi-13434.xls" );
    Assert.assertTrue( tempFile.exists() );
    tempFile.delete();
  }
}