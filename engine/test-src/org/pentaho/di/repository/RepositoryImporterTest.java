/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.repository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mapping.MappingMeta;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@RunWith( MockitoJUnitRunner.class )
public class RepositoryImporterTest {

  private static final String ROOT_PATH = "/test_root";

  private static final String USER_NAME_PATH = "/userName";

  @Mock
  private RepositoryImportFeedbackInterface feedback;

  private RepositoryDirectoryInterface baseDirectory;

  private Node entityNode;

  @Before
  public void beforeTest() {
    NodeList nodeList = mock( NodeList.class );

    entityNode = mock( Node.class );
    when( entityNode.getChildNodes() ).thenReturn( nodeList );

    baseDirectory = mock( RepositoryDirectoryInterface.class );
    when( baseDirectory.getPath() ).thenReturn( ROOT_PATH );
  }

  @Test
  public void testImportJob_patchJobEntries_without_variables() throws KettleException {
    JobEntryTrans jobEntryTrans = createJobEntryTrans( "/userName" );
    MappingMeta mappingMeta = createMappingMeta();
    RepositoryImporter importer = createRepositoryImporter( jobEntryTrans, mappingMeta, true );
    importer.setBaseDirectory( baseDirectory );

    importer.importJob( entityNode, feedback );
    verify( jobEntryTrans ).setDirectory( ROOT_PATH + USER_NAME_PATH );
  }

  @Test
  public void testImportJob_patchJobEntries_with_variable() throws KettleException {
    JobEntryTrans jobEntryTrans = createJobEntryTrans( "${USER_VARIABLE}" );
    MappingMeta mappingMeta = createMappingMeta();
    RepositoryImporter importer = createRepositoryImporter( jobEntryTrans, mappingMeta, true );
    importer.setBaseDirectory( baseDirectory );

    importer.importJob( entityNode, feedback );
    verify( jobEntryTrans ).setDirectory( "${USER_VARIABLE}" );
  }

  @Test
  public void testImportJob_patchJobEntries_when_directory_path_starts_with_variable() throws KettleException {
    JobEntryTrans jobEntryTrans = createJobEntryTrans( "${USER_VARIABLE}/myDir" );
    MappingMeta mappingMeta = createMappingMeta();
    RepositoryImporter importer = createRepositoryImporter( jobEntryTrans, mappingMeta, true );
    importer.setBaseDirectory( baseDirectory );

    importer.importJob( entityNode, feedback );
    verify( jobEntryTrans ).setDirectory( "${USER_VARIABLE}/myDir" );

    JobEntryTrans jobEntryTrans2 = createJobEntryTrans( "${USER_VARIABLE}/myDir" );
    RepositoryImporter importerWithCompatibilityImportPath =
        createRepositoryImporter( jobEntryTrans2, mappingMeta, false );
    importerWithCompatibilityImportPath.setBaseDirectory( baseDirectory );

    importerWithCompatibilityImportPath.importJob( entityNode, feedback );
    verify( jobEntryTrans2 ).setDirectory( ROOT_PATH + "/${USER_VARIABLE}/myDir" );
  }

  @Test
  public void testImportJob_patchJobEntries_when_directory_path_ends_with_variable() throws KettleException {
    JobEntryTrans jobEntryTrans = createJobEntryTrans( "/myDir/${USER_VARIABLE}" );
    MappingMeta mappingMeta = createMappingMeta();
    RepositoryImporter importer = createRepositoryImporter( jobEntryTrans, mappingMeta, true );
    importer.setBaseDirectory( baseDirectory );

    importer.importJob( entityNode, feedback );
    verify( jobEntryTrans ).setDirectory( "/myDir/${USER_VARIABLE}" );

    JobEntryTrans jobEntryTrans2 = createJobEntryTrans( "/myDir/${USER_VARIABLE}" );
    RepositoryImporter importerWithCompatibilityImportPath =
        createRepositoryImporter( jobEntryTrans2, mappingMeta, false );
    importerWithCompatibilityImportPath.setBaseDirectory( baseDirectory );

    importerWithCompatibilityImportPath.importJob( entityNode, feedback );
    verify( jobEntryTrans2 ).setDirectory( ROOT_PATH + "/myDir/${USER_VARIABLE}" );
  }

  @Test
  public void testImportTrans_patchTransEntries_without_variables() throws KettleException {
    JobEntryTrans jobEntryTrans = createJobEntryTrans();
    MappingMeta mappingMeta = createMappingMeta( "/userName" );
    RepositoryImporter importer = createRepositoryImporter( jobEntryTrans, mappingMeta, true );
    importer.setBaseDirectory( baseDirectory );

    importer.importTransformation( entityNode, feedback );
    verify( mappingMeta ).setDirectoryPath( ROOT_PATH + USER_NAME_PATH );
  }

  @Test
  public void testImportTrans_patchTransEntries_with_variable() throws KettleException {
    JobEntryTrans jobEntryTrans = createJobEntryTrans();
    MappingMeta mappingMeta = createMappingMeta( "${USER_VARIABLE}" );
    RepositoryImporter importer = createRepositoryImporter( jobEntryTrans, mappingMeta, true );
    importer.setBaseDirectory( baseDirectory );

    importer.importTransformation( entityNode, feedback );
    verify( mappingMeta ).setDirectoryPath( "${USER_VARIABLE}" );
  }

  @Test
  public void testImportTrans_patchTransEntries_when_directory_path_starts_with_variable() throws KettleException {
    JobEntryTrans jobEntryTrans = createJobEntryTrans();
    MappingMeta mappingMeta = createMappingMeta( "${USER_VARIABLE}/myDir" );
    RepositoryImporter importer = createRepositoryImporter( jobEntryTrans, mappingMeta, true );
    importer.setBaseDirectory( baseDirectory );

    importer.importTransformation( entityNode, feedback );
    verify( mappingMeta ).setDirectoryPath( "${USER_VARIABLE}/myDir" );

    MappingMeta mappingMeta2 = createMappingMeta( "${USER_VARIABLE}/myDir" );
    RepositoryImporter importerWithCompatibilityImportPath =
        createRepositoryImporter( jobEntryTrans, mappingMeta2, false );
    importerWithCompatibilityImportPath.setBaseDirectory( baseDirectory );

    importerWithCompatibilityImportPath.importTransformation( entityNode, feedback );
    verify( mappingMeta2 ).setDirectoryPath( ROOT_PATH + "/${USER_VARIABLE}/myDir" );
  }

  @Test
  public void testImportTrans_patchTransEntries_when_directory_path_ends_with_variable() throws KettleException {
    JobEntryTrans jobEntryTrans = createJobEntryTrans();
    MappingMeta mappingMeta = createMappingMeta( "/myDir/${USER_VARIABLE}" );
    RepositoryImporter importer = createRepositoryImporter( jobEntryTrans, mappingMeta, true );
    importer.setBaseDirectory( baseDirectory );

    importer.importTransformation( entityNode, feedback );
    verify( mappingMeta ).setDirectoryPath( "/myDir/${USER_VARIABLE}" );

    MappingMeta mappingMeta2 = createMappingMeta( "/myDir/${USER_VARIABLE}" );
    RepositoryImporter importerWithCompatibilityImportPath =
        createRepositoryImporter( jobEntryTrans, mappingMeta2, false );
    importerWithCompatibilityImportPath.setBaseDirectory( baseDirectory );

    importerWithCompatibilityImportPath.importTransformation( entityNode, feedback );
    verify( mappingMeta2 ).setDirectoryPath( ROOT_PATH + "/myDir/${USER_VARIABLE}" );
  }

  private static JobEntryTrans createJobEntryTrans() {
    return createJobEntryTrans( "" );
  }

  private static JobEntryTrans createJobEntryTrans( String directory ) {
    JobEntryTrans jet = mock( JobEntryTrans.class );
    when( jet.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    when( jet.getDirectory() ).thenReturn( directory );
    return jet;
  }

  private static MappingMeta createMappingMeta() {
    return createMappingMeta( "" );
  }

  private static MappingMeta createMappingMeta( String directory ) {
    MappingMeta mappingMeta = mock( MappingMeta.class );
    when( mappingMeta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    when( mappingMeta.getDirectoryPath() ).thenReturn( directory );
    return mappingMeta;
  }

  private static RepositoryImporter createRepositoryImporter( final JobEntryTrans jobEntryTrans,
      final MappingMeta mappingMeta, final boolean needToCheckPathForVariables ) {
    Repository repository = mock( Repository.class );
    LogChannelInterface log = mock( LogChannelInterface.class );
    RepositoryImporter importer = new RepositoryImporter( repository, log ) {

      @Override
      JobMeta createJobMetaForNode( Node jobnode ) throws KettleXMLException {
        JobMeta meta = mock( JobMeta.class );
        JobEntryCopy jec = mock( JobEntryCopy.class );
        when( jec.isTransformation() ).thenReturn( true );
        when( jec.getEntry() ).thenReturn( jobEntryTrans );
        when( meta.getJobCopies() ).thenReturn( Collections.singletonList( jec ) );
        return meta;
      }

      @Override
      TransMeta createTransMetaForNode( Node transnode ) throws KettleMissingPluginsException, KettleXMLException {
        TransMeta meta = mock( TransMeta.class );
        StepMeta stepMeta = mock( StepMeta.class );
        when( stepMeta.isMapping() ).thenReturn( true );
        when( stepMeta.getStepMetaInterface() ).thenReturn( mappingMeta );
        when( meta.getSteps() ).thenReturn( Collections.singletonList( stepMeta ) );
        return meta;
      }

      @Override
      protected void replaceSharedObjects( JobMeta transMeta ) throws KettleException {
      }

      @Override
      protected void replaceSharedObjects( TransMeta transMeta ) throws KettleException {
      }

      @Override
      boolean needToCheckPathForVariables() {
        return needToCheckPathForVariables;
      }
    };
    return importer;
  }

}
