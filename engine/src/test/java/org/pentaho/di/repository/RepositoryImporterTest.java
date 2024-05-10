/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.mockito.Mockito.*;

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
    JobEntryInterface jobEntry = createJobEntry( "/userName" );
    StepMetaInterface stepMeta = createStepMeta( "" );
    RepositoryImporter importer = createRepositoryImporter( jobEntry, stepMeta, true );
    importer.setBaseDirectory( baseDirectory );

    importer.importJob( entityNode, feedback );
    verify( (HasRepositoryDirectories) jobEntry  ).setDirectories( new String[]{ ROOT_PATH + USER_NAME_PATH } );
  }

  @Test
  public void testImportJob_patchJobEntries_with_variable() throws KettleException {
    JobEntryInterface jobEntryInterface = createJobEntry( "${USER_VARIABLE}" );
    StepMetaInterface stepMeta = createStepMeta( "" );
    RepositoryImporter importer = createRepositoryImporter( jobEntryInterface, stepMeta, true );
    importer.setBaseDirectory( baseDirectory );

    importer.importJob( entityNode, feedback );
    verify( (HasRepositoryDirectories) jobEntryInterface ).setDirectories( new String[]{ "${USER_VARIABLE}" } );
  }

  @Test
  public void testImportJob_patchJobEntries_when_directory_path_starts_with_variable() throws KettleException {
    JobEntryInterface jobEntryInterface = createJobEntry( "${USER_VARIABLE}/myDir" );
    StepMetaInterface stepMeta = createStepMeta( "" );
    RepositoryImporter importer = createRepositoryImporter( jobEntryInterface, stepMeta, true );
    importer.setBaseDirectory( baseDirectory );

    importer.importJob( entityNode, feedback );
    verify( (HasRepositoryDirectories) jobEntryInterface ).setDirectories( new String[] { "${USER_VARIABLE}/myDir" } );

    JobEntryInterface jobEntryInterface2 = createJobEntry( "${USER_VARIABLE}/myDir" );
    RepositoryImporter importerWithCompatibilityImportPath =
        createRepositoryImporter( jobEntryInterface2, stepMeta, false );
    importerWithCompatibilityImportPath.setBaseDirectory( baseDirectory );

    importerWithCompatibilityImportPath.importJob( entityNode, feedback );
    verify( (HasRepositoryDirectories) jobEntryInterface2 ).setDirectories( new String[]{ ROOT_PATH + "/${USER_VARIABLE}/myDir" } );
  }

  @Test
  public void testImportJob_patchJobEntries_when_directory_path_ends_with_variable() throws KettleException {
    JobEntryInterface jobEntryInterface = createJobEntry( "/myDir/${USER_VARIABLE}" );
    StepMetaInterface stepMeta = createStepMeta( "" );
    RepositoryImporter importer = createRepositoryImporter( jobEntryInterface, stepMeta, true );
    importer.setBaseDirectory( baseDirectory );

    importer.importJob( entityNode, feedback );
    verify( (HasRepositoryDirectories) jobEntryInterface ).setDirectories( new String[] { "/myDir/${USER_VARIABLE}" } );

    JobEntryInterface jobEntryInterface2 = createJobEntry( "/myDir/${USER_VARIABLE}" );
    RepositoryImporter importerWithCompatibilityImportPath =
        createRepositoryImporter( jobEntryInterface2, stepMeta, false );
    importerWithCompatibilityImportPath.setBaseDirectory( baseDirectory );

    importerWithCompatibilityImportPath.importJob( entityNode, feedback );
    verify( (HasRepositoryDirectories) jobEntryInterface2 ).setDirectories( new String[] { ROOT_PATH + "/myDir/${USER_VARIABLE}" } );
  }

  @Test
  public void testImportTrans_patchTransEntries_without_variables() throws KettleException {
    JobEntryInterface jobEntryInterface = createJobEntry( "" );
    StepMetaInterface stepMeta = createStepMeta( "/userName" );
    RepositoryImporter importer = createRepositoryImporter( jobEntryInterface, stepMeta, true );
    importer.setBaseDirectory( baseDirectory );

    importer.importTransformation( entityNode, feedback );
    verify( (HasRepositoryDirectories) stepMeta ).setDirectories( new String[] { ROOT_PATH + USER_NAME_PATH } );
  }

  @Test
  public void testImportTrans_patchTransEntries_with_variable() throws KettleException {
    JobEntryInterface jobEntryInterface = createJobEntry( "" );
    StepMetaInterface stepMeta = createStepMeta(  "${USER_VARIABLE}" );
    RepositoryImporter importer = createRepositoryImporter( jobEntryInterface, stepMeta, true );
    importer.setBaseDirectory( baseDirectory );

    importer.importTransformation( entityNode, feedback );
    verify( (HasRepositoryDirectories) stepMeta ).setDirectories( new String[] { "${USER_VARIABLE}" } );
  }

  @Test
  public void testImportTrans_patchTransEntries_when_directory_path_starts_with_variable() throws KettleException {
    JobEntryInterface jobEntryInterface = createJobEntry( "" );
    StepMetaInterface stepMeta = createStepMeta( "${USER_VARIABLE}/myDir" );
    RepositoryImporter importer = createRepositoryImporter( jobEntryInterface, stepMeta, true );
    importer.setBaseDirectory( baseDirectory );

    importer.importTransformation( entityNode, feedback );
    verify( (HasRepositoryDirectories) stepMeta ).setDirectories( new String[] { "${USER_VARIABLE}/myDir" } );

    StepMetaInterface stepMeta2 = createStepMeta( "${USER_VARIABLE}/myDir" );
    RepositoryImporter importerWithCompatibilityImportPath =
        createRepositoryImporter( jobEntryInterface, stepMeta2, false );
    importerWithCompatibilityImportPath.setBaseDirectory( baseDirectory );

    importerWithCompatibilityImportPath.importTransformation( entityNode, feedback );
    verify( (HasRepositoryDirectories) stepMeta2 ).setDirectories( new String[] { ROOT_PATH + "/${USER_VARIABLE}/myDir" } );
  }

  @Test
  public void testImportTrans_patchTransEntries_when_directory_path_ends_with_variable() throws KettleException {
    JobEntryInterface jobEntryInterface = createJobEntry( "" );
    StepMetaInterface stepMeta = createStepMeta(  "/myDir/${USER_VARIABLE}" );
    RepositoryImporter importer = createRepositoryImporter( jobEntryInterface, stepMeta, true );
    importer.setBaseDirectory( baseDirectory );

    importer.importTransformation( entityNode, feedback );
    verify( (HasRepositoryDirectories) stepMeta ).setDirectories( new String[] { "/myDir/${USER_VARIABLE}" } );

    StepMetaInterface stepMeta2 = createStepMeta( "/myDir/${USER_VARIABLE}" );
    RepositoryImporter importerWithCompatibilityImportPath =
        createRepositoryImporter( jobEntryInterface, stepMeta2, false );
    importerWithCompatibilityImportPath.setBaseDirectory( baseDirectory );

    importerWithCompatibilityImportPath.importTransformation( entityNode, feedback );
    verify( (HasRepositoryDirectories) stepMeta2 ).setDirectories( new String[] { ROOT_PATH + "/myDir/${USER_VARIABLE}" } );
  }

  private static JobEntryInterface createJobEntry( String directory ) {
    JobEntryInterface jobEntryInterface = mock( JobEntryInterface.class, withSettings().extraInterfaces( HasRepositoryDirectories.class ) );
    when( jobEntryInterface.isReferencedObjectEnabled() ).thenReturn( new boolean[] { true } );
    doAnswer( invocationOnMock -> new ObjectLocationSpecificationMethod[] { ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME } )
      .when( ( (HasRepositoryDirectories) jobEntryInterface ) ).getSpecificationMethods();
    doAnswer( invocationOnMock -> new String[] { directory } )
      .when( (HasRepositoryDirectories) jobEntryInterface ).getDirectories();
    return jobEntryInterface;
  }

  private static StepMetaInterface createStepMeta( String directory ) {
    StepMetaInterface stepMetaInterface = mock( StepMetaInterface.class, withSettings().extraInterfaces( HasRepositoryDirectories.class ) );
    when( stepMetaInterface.isReferencedObjectEnabled() ).thenReturn( new boolean[] { true } );
    doAnswer( invocationOnMock -> new ObjectLocationSpecificationMethod[] { ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME } )
      .when( ( (HasRepositoryDirectories) stepMetaInterface ) ).getSpecificationMethods();
    doAnswer( invocationOnMock -> new String[] { directory } )
      .when( (HasRepositoryDirectories) stepMetaInterface ).getDirectories();
    return stepMetaInterface;
  }

  private static RepositoryImporter createRepositoryImporter( final JobEntryInterface jobEntryInterface, final
                                                              StepMetaInterface stepMetaInterface,
                                                              final boolean needToCheckPathForVariables ) {
    Repository repository = mock( Repository.class );
    LogChannelInterface log = mock( LogChannelInterface.class );
    RepositoryImporter importer = new RepositoryImporter( repository, log ) {

      @Override
      JobMeta createJobMetaForNode( Node jobnode ) throws KettleXMLException {
        JobMeta meta = mock( JobMeta.class );
        JobEntryCopy jec = mock( JobEntryCopy.class );
        when( jec.getEntry() ).thenReturn( jobEntryInterface );
        when( meta.getJobCopies() ).thenReturn( Collections.singletonList( jec ) );
        return meta;
      }

      @Override
      TransMeta createTransMetaForNode( Node transnode ) throws KettleMissingPluginsException, KettleXMLException {
        TransMeta meta = mock( TransMeta.class );
        StepMeta stepMeta = mock( StepMeta.class );
        when( stepMeta.getStepMetaInterface() ).thenReturn( stepMetaInterface );
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
