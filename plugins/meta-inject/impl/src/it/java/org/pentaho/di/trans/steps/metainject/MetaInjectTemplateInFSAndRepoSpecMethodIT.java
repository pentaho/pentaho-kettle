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


package org.pentaho.di.trans.steps.metainject;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.plugins.Plugin;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.metainject.MetaInject;
import org.pentaho.di.trans.steps.metainject.MetaInjectMeta;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaInjectTemplateInFSAndRepoSpecMethodIT {

  private static final String EXPECTED_VALUE = "1.0";

  private TransMeta transMeta;

  private Trans trans;

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init( false );

    Map<Class<?>, String> classMap = new HashMap<>();
    classMap.put( StepMetaInterface.class, "org.pentaho.di.trans.steps.metainject.MetaInjectMeta" );
    List<String> libraries = new ArrayList<>();

    PluginInterface plugin =
      new Plugin( new String[] { "MetaInject" }, StepPluginType.class, StepMetaInterface.class, "Flow",
        "MetaInjectMeta", null, null, false, false, classMap, libraries, null, null );
    PluginRegistry.getInstance().registerPlugin( StepPluginType.class, plugin );
  }

  @Before
  public void setUp() throws Exception {
    transMeta = new TransMeta( "src/it/resources/org/pentaho/di/trans/steps/metainject/pdi-16420.ktr" );
    transMeta.setTransformationType( TransMeta.TransformationType.Normal );
    trans = new Trans( transMeta );
  }

  @Test
  public void testAddedFieldsAvailableOutsideInjectedTransformation() throws Exception {
    runTransformation( trans );
    String actualValue = trans.getVariable( "object_id" );
    assertEquals( EXPECTED_VALUE, actualValue );
  }

  private static void runTransformation( Trans trans ) throws Exception {
    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();
    assertEquals( 0, trans.getErrors() );
  }

}
