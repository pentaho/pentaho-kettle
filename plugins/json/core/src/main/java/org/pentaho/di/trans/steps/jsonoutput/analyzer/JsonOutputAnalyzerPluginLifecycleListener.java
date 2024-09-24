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
