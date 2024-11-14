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


package org.pentaho.di.trans.steps.metainject.lifecycle;

import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.trans.steps.metainject.analyzer.MetaInjectAnalyzer;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * Created by spravasahu on 08/02/22.
 */
@LifecyclePlugin( id = "KettleMetaInjectPlugin", name = "KettleMetaInjectPlugin" )
public class KettleMetaInjectPluginLifecycleListener implements LifecycleListener {

  @Override
  public void onStart( LifeEventHandler handler ) throws LifecycleException {
    MetaInjectAnalyzer getMetaInjectAnalyzer = new MetaInjectAnalyzer();

    PentahoSystem.registerObject( getMetaInjectAnalyzer );
  }

  @Override public void onExit( LifeEventHandler handler ) throws LifecycleException {
    // no-op
  }
}
