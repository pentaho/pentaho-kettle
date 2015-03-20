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

package org.pentaho.di.trans;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.stepsmetrics.StepsMetricsMeta;


public class SingleThreadedTransExecutorTest {

  @Test( expected = KettleException.class )
  public void failsWhenGivenNonSingleThreadSteps() throws Exception {
    KettleEnvironment.init();

    TransMeta transMeta = new TransMeta();
    transMeta.setName( "failsWhenGivenNonSingleThreadSteps" );

    StepsMetricsMeta metaInterface = new StepsMetricsMeta();

    PluginRegistry plugReg = PluginRegistry.getInstance();
    String id = plugReg.getPluginId( StepPluginType.class, metaInterface );

    StepMeta stepMeta = new StepMeta( id, "stepMetrics", metaInterface );
    stepMeta.setDraw( true );
    transMeta.addStep( stepMeta );

    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );

    SingleThreadedTransExecutor executor = new SingleThreadedTransExecutor( trans );
    executor.init();
  }
}