/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.getfilenames;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;

public class GetFileNamesIntIT {

  private static final String STEPNAME = "TestGetFileNames_Step";

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Test
  public void testParameterFolderName() throws KettleException, IOException {
    GetFileNamesMeta meta = new GetFileNamesMeta();
    meta.setDefault();

    meta.allocate( 1 );
    meta.setFileName( new String[]{ "${MY_FOLDER_PARAM}" } );
    meta.setFileMask( new String[]{ ".*" } );
    meta.setExcludeFileMask( new String[]{ "" } );
    meta.setFileRequired( new String[]{ "Y" } );
    meta.setIncludeSubFolders( new String[]{ "N" } );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, STEPNAME );
    //Remove the Injector hop, as it's not needed for this transformation
    TransHopMeta injectHop = transMeta.findTransHop( transMeta.findStep( TransTestFactory.INJECTOR_STEPNAME ),
      transMeta.findStep( STEPNAME ) );
    transMeta.removeTransHop( transMeta.indexOfTransHop( injectHop ) );

    transMeta.addParameterDefinition( "MY_FOLDER_PARAM", "C:\\ThisFolderDoesNotExist", "The folder to look in for files" );
    Variables varSpace = new Variables();
    varSpace.setVariable( "MY_FOLDER_PARAM", tempFolder.getRoot().getAbsolutePath() );

    // Content inside selected folder
    String expectedFilename = "file.tmp";
    String expectedSubFolderName = "subfolder";
    tempFolder.newFile( expectedFilename );
    tempFolder.newFolder( expectedSubFolderName );

    List<RowMetaAndData> result =
      TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, STEPNAME,
        TransTestFactory.DUMMY_STEPNAME, new ArrayList<RowMetaAndData>(), null, varSpace );

    // Check that the expected file was located correctly
    assertNotNull( result );
    assertEquals( 2, result.size() );
    assertTrue( result.get( 0 ).getRowMeta().indexOfValue( "short_filename" ) >= 0 );
    assertEquals( expectedFilename, result.get( 0 ).getString( "short_filename", "failure" ) );
    assertTrue( result.get( 1 ).getRowMeta().indexOfValue( "short_filename" ) >= 0 );
    assertEquals( expectedSubFolderName, result.get( 1 ).getString( "short_filename", "failure" ) );
  }

  @Test
  public void testParameterFolderNameWithoutWildcard() throws KettleException, IOException {
    GetFileNamesMeta meta = new GetFileNamesMeta();
    meta.setDefault();

    meta.allocate( 1 );
    meta.setFileName( new String[]{ "${MY_FOLDER_PARAM}" } );
    meta.setFileMask( new String[]{ "" } );
    meta.setExcludeFileMask( new String[]{ "" } );
    meta.setFileRequired( new String[]{ "Y" } );
    meta.setIncludeSubFolders( new String[]{ "N" } );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, STEPNAME );
    //Remove the Injector hop, as it's not needed for this transformation
    TransHopMeta injectHop = transMeta.findTransHop( transMeta.findStep( TransTestFactory.INJECTOR_STEPNAME ),
      transMeta.findStep( STEPNAME ) );
    transMeta.removeTransHop( transMeta.indexOfTransHop( injectHop ) );

    transMeta.addParameterDefinition( "MY_FOLDER_PARAM", "C:\\ThisFolderDoesNotExist", "The folder to look in for files" );
    Variables varSpace = new Variables();
    varSpace.setVariable( "MY_FOLDER_PARAM", tempFolder.getRoot().getAbsolutePath() );

    // Content inside selected folder
    String expectedFilename = "file.tmp";
    String expectedSubFolderName = "subfolder";
    tempFolder.newFile( expectedFilename );
    tempFolder.newFolder( expectedSubFolderName );

    List<RowMetaAndData> result =
      TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, STEPNAME,
        TransTestFactory.DUMMY_STEPNAME, new ArrayList<RowMetaAndData>(), null, varSpace );

    // Check that the expected file was located correctly
    assertNotNull( result );
    assertEquals( 2, result.size() );
    assertTrue( result.get( 0 ).getRowMeta().indexOfValue( "short_filename" ) >= 0 );
    assertEquals( expectedFilename, result.get( 0 ).getString( "short_filename", "failure" ) );
    assertTrue( result.get( 1 ).getRowMeta().indexOfValue( "short_filename" ) >= 0 );
    assertEquals( expectedSubFolderName, result.get( 1 ).getString( "short_filename", "failure" ) );
  }

  @Test
  public void testParameterFolderNameOnlyFiles() throws KettleException, IOException {
    GetFileNamesMeta meta = new GetFileNamesMeta();
    meta.setDefault();

    meta.allocate( 1 );
    meta.setFileName( new String[]{ "${MY_FOLDER_PARAM}" } );
    meta.setFileMask( new String[]{ ".*" } );
    meta.setExcludeFileMask( new String[]{ "" } );
    meta.setFileRequired( new String[]{ "Y" } );
    meta.setIncludeSubFolders( new String[]{ "N" } );
    meta.setFilterFileType( 1 );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, STEPNAME );
    //Remove the Injector hop, as it's not needed for this transformation
    TransHopMeta injectHop = transMeta.findTransHop( transMeta.findStep( TransTestFactory.INJECTOR_STEPNAME ),
      transMeta.findStep( STEPNAME ) );
    transMeta.removeTransHop( transMeta.indexOfTransHop( injectHop ) );

    transMeta.addParameterDefinition( "MY_FOLDER_PARAM", "C:\\ThisFolderDoesNotExist", "The folder to look in for files" );
    Variables varSpace = new Variables();
    varSpace.setVariable( "MY_FOLDER_PARAM", tempFolder.getRoot().getAbsolutePath() );

    // Content inside selected folder
    String expectedFilename = "file.tmp";
    String expectedSubFolderName = "subfolder";
    tempFolder.newFile( expectedFilename );
    tempFolder.newFolder( expectedSubFolderName );

    List<RowMetaAndData> result =
      TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, STEPNAME,
        TransTestFactory.DUMMY_STEPNAME, new ArrayList<RowMetaAndData>(), null, varSpace );

    // Check that the expected file was located correctly
    assertNotNull( result );
    assertEquals( 1, result.size() );
    assertTrue( result.get( 0 ).getRowMeta().indexOfValue( "short_filename" ) >= 0 );
    assertEquals( expectedFilename, result.get( 0 ).getString( "short_filename", "failure" ) );
  }

  @Test
  public void testParameterFolderNameWithoutWildcardAndOnlyFiles() throws KettleException, IOException {
    GetFileNamesMeta meta = new GetFileNamesMeta();
    meta.setDefault();

    meta.allocate( 1 );
    meta.setFileName( new String[]{ "${MY_FOLDER_PARAM}" } );
    meta.setFileMask( new String[]{ "" } );
    meta.setExcludeFileMask( new String[]{ "" } );
    meta.setFileRequired( new String[]{ "Y" } );
    meta.setIncludeSubFolders( new String[]{ "N" } );
    meta.setFilterFileType( 1 );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, STEPNAME );
    //Remove the Injector hop, as it's not needed for this transformation
    TransHopMeta injectHop = transMeta.findTransHop( transMeta.findStep( TransTestFactory.INJECTOR_STEPNAME ),
      transMeta.findStep( STEPNAME ) );
    transMeta.removeTransHop( transMeta.indexOfTransHop( injectHop ) );

    transMeta.addParameterDefinition( "MY_FOLDER_PARAM", "C:\\ThisFolderDoesNotExist", "The folder to look in for files" );
    Variables varSpace = new Variables();
    varSpace.setVariable( "MY_FOLDER_PARAM", tempFolder.getRoot().getAbsolutePath() );

    // Content inside selected folder
    String expectedFilename = "file.tmp";
    String expectedSubFolderName = "subfolder";
    tempFolder.newFile( expectedFilename );
    tempFolder.newFolder( expectedSubFolderName );

    List<RowMetaAndData> result =
      TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, STEPNAME,
        TransTestFactory.DUMMY_STEPNAME, new ArrayList<RowMetaAndData>(), null, varSpace );

    // Check that the expected file was located correctly
    assertNotNull( result );
    assertEquals( 1, result.size() );
    assertTrue( result.get( 0 ).getRowMeta().indexOfValue( "short_filename" ) >= 0 );
    assertEquals( expectedFilename, result.get( 0 ).getString( "short_filename", "failure" ) );
  }

  @Test
  public void testParameterFolderNameOnlyFolders() throws KettleException, IOException {
    GetFileNamesMeta meta = new GetFileNamesMeta();
    meta.setDefault();

    meta.allocate( 1 );
    meta.setFileName( new String[]{ "${MY_FOLDER_PARAM}" } );
    meta.setFileMask( new String[]{ ".*" } );
    meta.setExcludeFileMask( new String[]{ "" } );
    meta.setFileRequired( new String[]{ "Y" } );
    meta.setIncludeSubFolders( new String[]{ "N" } );
    meta.setFilterFileType( 2 );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, STEPNAME );
    //Remove the Injector hop, as it's not needed for this transformation
    TransHopMeta injectHop = transMeta.findTransHop( transMeta.findStep( TransTestFactory.INJECTOR_STEPNAME ),
      transMeta.findStep( STEPNAME ) );
    transMeta.removeTransHop( transMeta.indexOfTransHop( injectHop ) );

    transMeta.addParameterDefinition( "MY_FOLDER_PARAM", "C:\\ThisFolderDoesNotExist", "The folder to look in for files" );
    Variables varSpace = new Variables();
    varSpace.setVariable( "MY_FOLDER_PARAM", tempFolder.getRoot().getAbsolutePath() );

    // Content inside selected folder
    String expectedFilename = "file.tmp";
    String expectedSubFolderName = "subfolder";
    tempFolder.newFile( expectedFilename );
    tempFolder.newFolder( expectedSubFolderName );

    List<RowMetaAndData> result =
      TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, STEPNAME,
        TransTestFactory.DUMMY_STEPNAME, new ArrayList<RowMetaAndData>(), null, varSpace );

    // Check that the expected file was located correctly
    assertNotNull( result );
    assertEquals( 1, result.size() );
    assertTrue( result.get( 0 ).getRowMeta().indexOfValue( "short_filename" ) >= 0 );
    assertEquals( expectedSubFolderName, result.get( 0 ).getString( "short_filename", "failure" ) );
  }

  @Test
  public void testParameterFolderNameWithoutWildcardAndOnlyFolders() throws KettleException, IOException {
    GetFileNamesMeta meta = new GetFileNamesMeta();
    meta.setDefault();

    meta.allocate( 1 );
    meta.setFileName( new String[]{ "${MY_FOLDER_PARAM}" } );
    meta.setFileMask( new String[]{ "" } );
    meta.setExcludeFileMask( new String[]{ "" } );
    meta.setFileRequired( new String[]{ "Y" } );
    meta.setIncludeSubFolders( new String[]{ "N" } );
    meta.setFilterFileType( 2 );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, STEPNAME );
    //Remove the Injector hop, as it's not needed for this transformation
    TransHopMeta injectHop = transMeta.findTransHop( transMeta.findStep( TransTestFactory.INJECTOR_STEPNAME ),
      transMeta.findStep( STEPNAME ) );
    transMeta.removeTransHop( transMeta.indexOfTransHop( injectHop ) );

    transMeta.addParameterDefinition( "MY_FOLDER_PARAM", "C:\\ThisFolderDoesNotExist", "The folder to look in for files" );
    Variables varSpace = new Variables();
    varSpace.setVariable( "MY_FOLDER_PARAM", tempFolder.getRoot().getAbsolutePath() );

    // Content inside selected folder
    String expectedFilename = "file.tmp";
    String expectedSubFolderName = "subfolder";
    tempFolder.newFile( expectedFilename );
    tempFolder.newFolder( expectedSubFolderName );

    List<RowMetaAndData> result =
      TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, STEPNAME,
        TransTestFactory.DUMMY_STEPNAME, new ArrayList<RowMetaAndData>(), null, varSpace );

    // Check that the expected file was located correctly
    assertNotNull( result );
    assertEquals( 1, result.size() );
    assertTrue( result.get( 0 ).getRowMeta().indexOfValue( "short_filename" ) >= 0 );
    assertEquals( expectedSubFolderName, result.get( 0 ).getString( "short_filename", "failure" ) );
  }
}
