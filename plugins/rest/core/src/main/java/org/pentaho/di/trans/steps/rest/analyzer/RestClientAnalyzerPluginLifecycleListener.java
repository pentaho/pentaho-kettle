/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.rest.analyzer;

import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepExternalResourceConsumer;
import org.pentaho.platform.engine.core.system.PentahoSystem;
@LifecyclePlugin( id = "RestClientAnalyzerPlugin", name = "RestClientAnalyzerPlugin" )
public class RestClientAnalyzerPluginLifecycleListener implements LifecycleListener {

  @Override
  public void onStart( final LifeEventHandler lifeEventHandler ) throws LifecycleException {
    RestClientStepAnalyzer restClientStepAnalyzer = new RestClientStepAnalyzer();
    // construct the external resource consumer for the files that it reads from
    final IStepExternalResourceConsumer consumer = RestClientExternalResourceConsumer.getInstance();
    restClientStepAnalyzer.setExternalResourceConsumer( consumer );

    // register the analyzer with PentahoSystem
    PentahoSystem.registerObject( restClientStepAnalyzer );
    // register the consumer with PentahoSystem
    PentahoSystem.registerObject( consumer );

  }

  public void onExit( LifeEventHandler lifeEventHandler ) throws LifecycleException {
  }

}
