/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
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
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.normaliser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.plugins.Plugin;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class NormaliserMetaInjectionIT {

  private static final String OUTPUT_FILENAME = "output_filename";

  private static final String EXPECTED_OUTPUT_FILE_NAME =
    "src/it/resources/org/pentaho/di/trans/steps/normaliser/normaliser-13761-expected-output.txt";

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

  @After
  public void tearDown() {
    String outputFileName = trans.getVariable( OUTPUT_FILENAME );
    File outputFile = new File( outputFileName );
    outputFile.delete();
  }

  @Test
  public void injectValuesToNormaliserTransformation() throws Exception {
    trans =
        createTransformationFromFile( "src/it/resources/org/pentaho/di/trans/steps/normaliser/injecting-trans-pdi-13761.ktr" );
    runTransformation( trans );
    String outputFileName = trans.getVariable( OUTPUT_FILENAME );
    File expectedOutputFile = new File( EXPECTED_OUTPUT_FILE_NAME );
    assertTrue( FileUtils.contentEquals( expectedOutputFile, new File( outputFileName ) ) );
  }

  @Test
  public void injectValuesToNormaliserTransformation_does_not_inject_fields() throws Exception {
    trans =
        createTransformationFromFile(
          "src/it/resources/org/pentaho/di/trans/steps/normaliser/injecting-trans-pdi-13761-does-not-inject-field.ktr" );
    runTransformation( trans );
    String outputFileName = trans.getVariable( OUTPUT_FILENAME );
    File expectedOutputFile = new File( EXPECTED_OUTPUT_FILE_NAME );
    assertTrue( FileUtils.contentEquals( expectedOutputFile, new File( outputFileName ) ) );
  }

  private static Trans createTransformationFromFile( String fileName ) throws KettleXMLException,
    KettleMissingPluginsException {
    TransMeta transMeta = new TransMeta( fileName );
    transMeta.setTransformationType( TransMeta.TransformationType.Normal );
    return new Trans( transMeta );
  }

  private static void runTransformation( Trans trans ) throws Exception {
    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();
    assertEquals( 0, trans.getErrors() );
  }

}
