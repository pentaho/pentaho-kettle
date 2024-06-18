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
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import java.util.List;

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

  /**
   * This test case uses a datagrid to test multiple issues when using the "Accept filenames from previous step"
   *    * Tests both files and folders as value of the same field.
   *    * Ensures additional fields match the expected value for each row.
   *   KTR:
   *      datagrid -> text file input -> switch (on nulls) -> dummys steps
   *   Cases:
   *     PDI-17117
   *     PDI-18752
   *     PDI-18818
   *     BACKLOG-34414
   * @throws KettleException
   */
  @Test
  public void testInputFilesAndFoldersFromPreviousStep() throws KettleException {
    final int TEXT_FILE_INPUT = 0;
    final int DUMMY_CATCH_NULLS = 3;
    final int DATA_GRID = 4;
    final String TEST_FOLDER_PATH = "${testfolder}/multiple";

    KettleEnvironment.init();
    String path = getClass().getResource( "text-file-input-from-datagrid.ktr" ).getPath();
    Variables variables = new Variables();
    variables.setVariable( "testfolder", getClass().getResource( "" ).getPath() );
    RowStepCollector collector = new RowStepCollector();

    TransMeta transMeta = new TransMeta( path, variables );
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );

    trans.getSteps().get( TEXT_FILE_INPUT ).step.addRowListener( collector );
    List<RowMetaAndData> rowsWritten = collector.getRowsWritten();

    trans.startThreads();
    trans.waitUntilFinished();

    //Did we read expected values from the ?
    assertEquals( 4, trans.getSteps().get( DATA_GRID ).step.getLinesWritten() );

    //Did we read expected amount of rows? ( Expect to read contents 6 times. 6 files * 3 rows = 18 total )
    assertEquals( 18, trans.getSteps().get( TEXT_FILE_INPUT ).step.getLinesWritten() );

    //Did we find any nulls?
    assertEquals( 0, trans.getSteps().get( DUMMY_CATCH_NULLS ).step.getLinesRead() );

    // Check Additional fields to work as expected.
    for ( int i = 0; i < rowsWritten.size( ); i++ ) {
      Object[] data = rowsWritten.get( i ).getData();

      switch ( i ) {
        case 0:
          // Single File "multiple/debug.csv"
          assertEquals( "${testfolder}/multiple/debug.csv", data[0] );
          assertEquals( "1111", data[1] );
          break;
        case 3:
        case 6:
          // Folder "multiple" file 1
          assertEquals( TEST_FOLDER_PATH, data[0] );
          assertEquals( "2222", data[1] );
          break;
        case 9:
          // Single File "multiple/debug2.csv"
          assertEquals( "${testfolder}/multiple/debug2.csv", data[0] );
          assertEquals( "3333", data[1] );
          break;
        case 12:
        case 15:
          // Folder "multiple" file 1
          assertEquals( TEST_FOLDER_PATH, data[0] );
          assertEquals( "4444", data[1] );
          break;
        default:
          /// Do nothing
          continue;
      }
    }
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

