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

package org.pentaho.test.util;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.SingleThreadedTransExecutor;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import static org.junit.Assert.assertNotNull;

/**
 * This is a base class for creating guard tests, that check a step cannot be executed in the single-threaded mode
 *
 * @author Andrey Khayrutdinov
 */
public abstract class SingleThreadedExecutionGuarder<Meta extends StepMetaInterface> {

  @BeforeClass
  public static void setUp() throws Exception {
    KettleEnvironment.init();
  }

  protected abstract Meta createMeta();

  @Test( expected = KettleException.class )
  public void failsWhenGivenNonSingleThreadSteps() throws Exception {
    Meta metaInterface = createMeta();

    PluginRegistry plugReg = PluginRegistry.getInstance();
    String id = plugReg.getPluginId( StepPluginType.class, metaInterface );
    assertNotNull( "pluginId", id );

    StepMeta stepMeta = new StepMeta( id, "stepMetrics", metaInterface );
    stepMeta.setDraw( true );

    TransMeta transMeta = new TransMeta();
    transMeta.setName( "failsWhenGivenNonSingleThreadSteps" );
    transMeta.addStep( stepMeta );

    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );

    SingleThreadedTransExecutor executor = new SingleThreadedTransExecutor( trans );
    executor.init();
  }
}
