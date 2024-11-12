/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.jsoninput.analyzer;

import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.metaverse.api.analyzer.kettle.step.IStepExternalResourceConsumer;
import org.pentaho.platform.engine.core.system.PentahoSystem;

@LifecyclePlugin( id = "JsonInputAnalyzerPlugin", name = "JsonInputAnalyzerPlugin" )
public class JsonInputAnalyzerPluginLifecycleListener implements LifecycleListener {

  @Override
  public void onStart( final LifeEventHandler lifeEventHandler ) throws LifecycleException {

    // instantiate a new analyzer
    final JsonInputAnalyzer analyzer = new JsonInputAnalyzer();
    // construct the external resource consumer for the files that it reads from
    final IStepExternalResourceConsumer consumer = new JsonInputExternalResourceConsumer();
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
