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


package org.pentaho.di.lifecycle;

import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.trans.steps.getxmldata.GetXMLDataExternalResourceConsumer;
import org.pentaho.di.trans.steps.getxmldata.GetXMLDataStepAnalyzer;
import org.pentaho.di.trans.steps.xmloutput.XMLOutputExternalResourceConsumer;
import org.pentaho.di.trans.steps.xmloutput.XMLOutputStepAnalyzer;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * Created by rfellows on 12/31/15.
 */
@LifecyclePlugin( id = "KettleXmlPlugin", name = "KettleXmlPlugin" )
public class KettleXmlPluginLifecycleListener implements LifecycleListener {

  @Override
  public void onStart( LifeEventHandler handler ) throws LifecycleException {
    GetXMLDataStepAnalyzer getXMLDataStepAnalyzer = new GetXMLDataStepAnalyzer();
    GetXMLDataExternalResourceConsumer getXMLDataExternalResourceConsumer = new GetXMLDataExternalResourceConsumer();
    getXMLDataStepAnalyzer.setExternalResourceConsumer( getXMLDataExternalResourceConsumer );

    XMLOutputStepAnalyzer xmlOutputStepAnalyzer = new XMLOutputStepAnalyzer();
    XMLOutputExternalResourceConsumer xmlOutputExternalResourceConsumer = new XMLOutputExternalResourceConsumer();
    xmlOutputStepAnalyzer.setExternalResourceConsumer( xmlOutputExternalResourceConsumer );

    PentahoSystem.registerObject( getXMLDataStepAnalyzer );
    PentahoSystem.registerObject( getXMLDataExternalResourceConsumer );
    PentahoSystem.registerObject( xmlOutputStepAnalyzer );
    PentahoSystem.registerObject( xmlOutputExternalResourceConsumer );
  }

  @Override public void onExit( LifeEventHandler handler ) throws LifecycleException {
    // no-op
  }
}
