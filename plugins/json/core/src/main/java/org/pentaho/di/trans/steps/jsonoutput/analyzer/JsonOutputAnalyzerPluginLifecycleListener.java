/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jsonoutput.analyzer;

import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepExternalResourceConsumer;
import org.pentaho.platform.engine.core.system.PentahoSystem;

@LifecyclePlugin( id = "JsonOutputAnalyzerPlugin", name = "JsonOutputAnalyzerPlugin" )
public class JsonOutputAnalyzerPluginLifecycleListener implements LifecycleListener {

  @Override
  public void onStart( final LifeEventHandler lifeEventHandler ) throws LifecycleException {

    // instantiate a new analyzer
    final ExternalResourceStepAnalyzer analyzer = new JsonOutputAnalyzer();
    // construct the external resource consumer for the files that it reads from
    final IStepExternalResourceConsumer consumer = new JsonOutputExternalResourceConsumer();
    analyzer.setExternalResourceConsumer( consumer );

    // register the analyzer with PentahoSystem
    PentahoSystem.registerObject( analyzer );
    // register the consumer with PentahoSystem
    PentahoSystem.registerObject( consumer );
  }

  @Override
  public void onExit( LifeEventHandler lifeEventHandler ) throws LifecycleException {
  }
}
