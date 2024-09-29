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
