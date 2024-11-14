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


package org.pentaho.test.util;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.SingleThreadedTransExecutor;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import static org.junit.Assert.assertNotNull;

/**
 * This is a base class for creating guard tests, that check a step cannot be executed in the single-threaded mode
 *
 * @author Andrey Khayrutdinov
 */
public abstract class SingleThreadedExecutionGuarder<Meta extends StepMetaInterface> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setUp() throws Exception {
    KettleEnvironment.init();
  }

  protected abstract Meta createMeta();

  @Test( expected = KettleException.class )
  public void failsWhenGivenNonSingleThreadSteps() throws Exception {
    Meta metaInterface = createMeta();

    PluginRegistry plugReg = PluginRegistry.getInstance();
    String id = plugReg.getPluginId( StepPluginType.class, metaInterface );
    assertNotNull( "pluginId", id );

    StepMeta stepMeta = new StepMeta( id, "stepMetrics", metaInterface );
    stepMeta.setDraw( true );

    TransMeta transMeta = new TransMeta();
    transMeta.setName( "failsWhenGivenNonSingleThreadSteps" );
    transMeta.addStep( stepMeta );

    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );

    SingleThreadedTransExecutor executor = new SingleThreadedTransExecutor( trans );
    executor.init();
  }
}
