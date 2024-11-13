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


package org.pentaho.di.trans.steps.metainject;


import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

public class MetaDataInjectionIT {

  @BeforeClass
  public static void setup() throws KettleException {
    KettleEnvironment.init();
  }

  @AfterClass
  public static void cleanup() {
    KettleEnvironment.shutdown();
  }

  @Test
  public void metaInjectWithFieldSplitter() throws KettleException {
    TransMeta transMeta = new TransMeta( "src/it/resources/org/pentaho/di/trans/steps/metainject/fieldsplit/PDI-15679-inject.ktr" );
    transMeta.setTransformationType( TransMeta.TransformationType.Normal );

    Trans trans = new Trans( transMeta );
    trans.setLogLevel( LogLevel.ERROR );

    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();

    Assert.assertEquals( 0, trans.getErrors() );
  }

}
