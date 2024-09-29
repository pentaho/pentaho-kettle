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
