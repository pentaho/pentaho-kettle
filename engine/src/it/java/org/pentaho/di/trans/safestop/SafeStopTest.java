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
package org.pentaho.di.trans.safestop;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import static org.junit.Assert.assertEquals;

public class SafeStopTest {
  @Test
  public void testDownStreamStepsFinishProcessing() throws KettleException {
    String path = getClass().getResource( "/safe-stop-gen-rows.ktr" ).getPath();
    TransMeta transMeta = new TransMeta( path, new Variables() );
    Trans trans = new Trans( transMeta );
    trans.execute( new String[] {} );
    trans.safeStop();
    trans.waitUntilFinished();
    assertEquals( trans.getSteps().get( 0 ).step.getLinesWritten(), trans.getSteps().get( 2 ).step.getLinesRead() );
  }

  @BeforeClass
  public static void init() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
  }
}
