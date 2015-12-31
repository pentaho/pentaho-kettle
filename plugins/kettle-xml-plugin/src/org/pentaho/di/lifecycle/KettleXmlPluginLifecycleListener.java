/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
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
 */

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
