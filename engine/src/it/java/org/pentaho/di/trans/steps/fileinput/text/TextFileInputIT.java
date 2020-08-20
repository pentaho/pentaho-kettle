/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.fileinput.text;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import static org.junit.Assert.assertEquals;

@RunWith( MockitoJUnitRunner.class )
public class TextFileInputIT {

  @Test
  public void testGetDataFromAFolderRecursivelyFromPreviousStep() throws KettleException {
    KettleEnvironment.init();
    String path = getClass().getResource( "text-file-input-get-data-from-folder-from-previous-step.ktr" ).getPath();
    Variables variables = new Variables();
    variables.setVariable( "testfolder", getClass().getResource( "" ).getPath() );
    TransMeta transMeta = new TransMeta( path, variables );
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();
    assertEquals( 14, trans.getSteps().get( 1 ).step.getLinesWritten() );
    assertEquals( 21, trans.getSteps().get( 1 ).step.getLinesInput() );
    // The path contains one entry of a folder
    assertEquals( 1, trans.getSteps().get( 0 ).step.getLinesWritten() );
  }

  @Test
  public void testGetDataFromListOfFilesFromPreviousStep() throws KettleException {
    KettleEnvironment.init();
    String path = getClass().getResource( "text-file-input-get-data-from-files-from-previous-step.ktr" ).getPath();
    Variables variables = new Variables();
    variables.setVariable( "testfolder", getClass().getResource( "" ).getPath() );
    TransMeta transMeta = new TransMeta( path, variables );
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();
    assertEquals( 14, trans.getSteps().get( 1 ).step.getLinesWritten() );
    assertEquals( 21, trans.getSteps().get( 1 ).step.getLinesInput() );
    // The path contains 7 entries containing csv file paths
    assertEquals( 7, trans.getSteps().get( 0 ).step.getLinesWritten() );
  }

  @Test
  public void testGetDataFromFolderRecursively() throws KettleException {
    KettleEnvironment.init();
    String path = getClass().getResource( "text-file-input-get-data-from-folder-step.ktr" ).getPath();
    Variables variables = new Variables();
    variables.setVariable( "testfolder", getClass().getResource( "" ).getPath() );
    TransMeta transMeta = new TransMeta( path, variables );
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();
    assertEquals( 14, trans.getSteps().get( 0 ).step.getLinesWritten() );
    assertEquals( 21, trans.getSteps().get( 0 ).step.getLinesInput() );
  }

  @Test
  public void testGetDataFromFromFiles() throws KettleException {
    KettleEnvironment.init();
    String path = getClass().getResource( "text-file-input-get-data-from-files-step.ktr" ).getPath();
    Variables variables = new Variables();
    variables.setVariable( "testfolder", getClass().getResource( "" ).getPath() );
    TransMeta transMeta = new TransMeta( path, variables );
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();
    assertEquals( 14, trans.getSteps().get( 0 ).step.getLinesWritten() );
    assertEquals( 21, trans.getSteps().get( 0 ).step.getLinesInput() );
  }

  @Test
  public void testGetDataFromFromFile() throws KettleException {
    KettleEnvironment.init();
    String path = getClass().getResource( "text-file-input-get-data-from-file-step.ktr" ).getPath();
    Variables variables = new Variables();
    variables.setVariable( "testfolder", getClass().getResource( "" ).getPath() );
    TransMeta transMeta = new TransMeta( path, variables );
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();
    assertEquals( 2, trans.getSteps().get( 0 ).step.getLinesWritten() );
    assertEquals( 3, trans.getSteps().get( 0 ).step.getLinesInput() );
  }
  @Test
  public void testGetDataFromFolderWithInvalidFieldName() throws KettleException {
    KettleEnvironment.init();
    String path = getClass().getResource( "text-file-input-get-data-from-folder-from-previous-step-negative.ktr" ).getPath();
    Variables variables = new Variables();
    variables.setVariable( "testfolder", getClass().getResource( "" ).getPath() );
    TransMeta transMeta = new TransMeta( path, variables );
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();
    assertEquals( 0, trans.getSteps().get( 1 ).step.getLinesWritten() );
    assertEquals( 0, trans.getSteps().get( 1 ).step.getLinesInput() );
    // The path contains one entry of a folder
    assertEquals( 1, trans.getSteps().get( 0 ).step.getLinesWritten() );
  }

  @Test
  public void testPDI18818() throws KettleException {
    KettleEnvironment.init();
    String path = getClass().getResource( "text-file-input-pdi-18818.ktr" ).getPath();
    Variables variables = new Variables();
    variables.setVariable( "testfolder", getClass().getResource( "" ).getPath() );
    TransMeta transMeta = new TransMeta( path, variables );
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();

    //Did we read both values?
    assertEquals( 1, trans.getSteps().get( 0 ).step.getLinesWritten() );

    //Did we read both files?
    assertEquals( 6, trans.getSteps().get( 1 ).step.getLinesWritten() );

    //Did we find any nulls?
    assertEquals( 0, trans.getSteps().get( 4 ).step.getLinesRead() );
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

