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

package org.pentaho.di.lifecycle;

import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.trans.steps.excelinput.ExcelInputExternalResourceConsumer;
import org.pentaho.di.trans.steps.excelinput.ExcelInputStepAnalyzer;
import org.pentaho.di.trans.steps.exceloutput.ExcelOutputExternalResourceConsumer;
import org.pentaho.di.trans.steps.exceloutput.ExcelOutputStepAnalyzer;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * Created by rfellows on 12/31/15.
 */
@LifecyclePlugin( id = "KettleExcelPlugin", name = "KettleExcelPlugin" )
public class KettleExcelPluginLifecycleListener implements LifecycleListener {

  @Override
  public void onStart( LifeEventHandler handler ) throws LifecycleException {
    ExcelInputStepAnalyzer getXMLDataStepAnalyzer = new ExcelInputStepAnalyzer();
    ExcelInputExternalResourceConsumer getXMLDataExternalResourceConsumer = new ExcelInputExternalResourceConsumer();
    getXMLDataStepAnalyzer.setExternalResourceConsumer( getXMLDataExternalResourceConsumer );

    ExcelOutputStepAnalyzer xmlOutputStepAnalyzer = new ExcelOutputStepAnalyzer();
    ExcelOutputExternalResourceConsumer xmlOutputExternalResourceConsumer = new ExcelOutputExternalResourceConsumer();
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
