/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.repository.pur;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.JobEntryLogTable;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.logging.StepLogTable;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.pur.model.EERepositoryObject;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.ui.repository.pur.services.ILockService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class PurRepositoryUnitTest {
  private VariableSpace mockedVariableSpace;
  private HasDatabasesInterface mockedHasDbInterface;

  @Before
  public void init() {
    System.setProperty( Const.KETTLE_TRANS_LOG_TABLE, "KETTLE_STEP_LOG_DB_VALUE" );
    mockedVariableSpace = mock( VariableSpace.class );
    mockedHasDbInterface = mock( HasDatabasesInterface.class );
  }

  @Test
  public void testGetObjectInformationGetsAclByFileId() throws KettleException {
    PurRepository purRepository = new PurRepository();
    IUnifiedRepository mockRepo = mock( IUnifiedRepository.class );
    RepositoryConnectResult result = mock( RepositoryConnectResult.class );
    when( result.getUnifiedRepository() ).thenReturn( mockRepo );

    RepositoryServiceRegistry registry = mock( RepositoryServiceRegistry.class );
    UnifiedRepositoryLockService lockService = new UnifiedRepositoryLockService( mockRepo );
    when( registry.getService( ILockService.class ) ).thenReturn( lockService );

    when( result.repositoryServiceRegistry() ).thenReturn( registry );
    IRepositoryConnector connector = mock( IRepositoryConnector.class );
    when( connector.connect( anyString(), anyString() ) ).thenReturn( result );
    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );
    purRepository.init( mockMeta );
    purRepository.setPurRepositoryConnector( connector );
    // purRepository.setTest( mockRepo );
    ObjectId objectId = mock( ObjectId.class );
    RepositoryFile mockFile = mock( RepositoryFile.class );
    RepositoryFile mockRootFolder = mock( RepositoryFile.class );
    RepositoryObjectType repositoryObjectType = RepositoryObjectType.TRANSFORMATION;
    RepositoryFileTree mockRepositoryTree = mock( RepositoryFileTree.class );
    String testId = "TEST_ID";
    String testFileId = "TEST_FILE_ID";
    when( objectId.getId() ).thenReturn( testId );
    when( mockRepo.getFileById( testId ) ).thenReturn( mockFile );
    when( mockFile.getPath() ).thenReturn( "/home/testuser/path.ktr" );
    when( mockFile.isLocked() ).thenReturn( false );
    when( mockFile.getId() ).thenReturn( testFileId );
    when( mockRepo.getTree( anyString(), anyInt(), anyString(), anyBoolean() ) ).thenReturn( mockRepositoryTree );
    when( mockRepositoryTree.getFile() ).thenReturn( mockRootFolder );
    when( mockRootFolder.getId() ).thenReturn( "/" );
    when( mockRootFolder.getPath() ).thenReturn( "/" );
    when( mockRepo.getFile( "/" ) ).thenReturn( mockRootFolder );
    purRepository.connect( "TEST_USER", "TEST_PASSWORD" );
    purRepository.getObjectInformation( objectId, repositoryObjectType );
    verify( mockRepo ).getAcl( testFileId );
  }


  @Test
  public void testRootIsNotVisible() throws KettleException {
    PurRepository purRepository = new PurRepository();
    IUnifiedRepository mockRepo = mock( IUnifiedRepository.class );
    RepositoryConnectResult result = mock( RepositoryConnectResult.class );
    when( result.getUnifiedRepository() ).thenReturn( mockRepo );
    IRepositoryConnector connector = mock( IRepositoryConnector.class );
    when( connector.connect( anyString(), anyString() ) ).thenReturn( result );
    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );
    purRepository.init( mockMeta );
    purRepository.setPurRepositoryConnector( connector );

    RepositoryFile mockRootFolder = mock( RepositoryFile.class );

    when( mockRootFolder.getId() ).thenReturn( "/" );
    when( mockRootFolder.getPath() ).thenReturn( "/" );
    when( mockRepo.getFile( "/" ) ).thenReturn( mockRootFolder );
    purRepository.connect( "TEST_USER", "TEST_PASSWORD" );

    RepositoryDirectoryInterface rootDir = purRepository.getRootDir();
    assertFalse( rootDir.isVisible() );
  }


  @Test
  public void testEtcIsNotThereInGetChildren() throws KettleException {
    PurRepository purRepository = new PurRepository();
    IUnifiedRepository mockRepo = mock( IUnifiedRepository.class );
    RepositoryConnectResult result = mock( RepositoryConnectResult.class );
    when( result.getUnifiedRepository() ).thenReturn( mockRepo );
    IRepositoryConnector connector = mock( IRepositoryConnector.class );
    when( connector.connect( anyString(), anyString() ) ).thenReturn( result );
    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );
    purRepository.init( mockMeta );
    purRepository.setPurRepositoryConnector( connector );
    // purRepository.setTest( mockRepo );
    ObjectId objectId = mock( ObjectId.class );
    RepositoryFile mockFile = mock( RepositoryFile.class );
    RepositoryFile mockRootFolder = mock( RepositoryFile.class );
    RepositoryObjectType repositoryObjectType = RepositoryObjectType.TRANSFORMATION;
    RepositoryFileTree mockRepositoryTree = mock( RepositoryFileTree.class );
    String testId = "TEST_ID";
    String testFileId = "TEST_FILE_ID";
    when( objectId.getId() ).thenReturn( testId );
    when( mockRepo.getFileById( testId ) ).thenReturn( mockFile );
    when( mockFile.getPath() ).thenReturn( "/etc" );
    when( mockFile.getId() ).thenReturn( testFileId );

    when( mockRepositoryTree.getFile() ).thenReturn( mockRootFolder );
    when( mockRootFolder.getId() ).thenReturn( "/" );
    when( mockRootFolder.getPath() ).thenReturn( "/" );

    List<RepositoryFile> rootChildren = new ArrayList<>( Collections.singletonList( mockFile ) );
    when( mockRepo.getChildren( argThat( IsInstanceOf.<RepositoryRequest>instanceOf( RepositoryRequest.class ) ) ) )
        .thenReturn( rootChildren );
    when( mockRepo.getFile( "/" ) ).thenReturn( mockRootFolder );
    purRepository.connect( "TEST_USER", "TEST_PASSWORD" );
    List<RepositoryDirectoryInterface> children = purRepository.getRootDir().getChildren();
    assertThat( children, empty() );
  }


  @Test
  public void testEtcIsNotThereInGetNrDirectories() throws KettleException {
    PurRepository purRepository = new PurRepository();
    IUnifiedRepository mockRepo = mock( IUnifiedRepository.class );
    RepositoryConnectResult result = mock( RepositoryConnectResult.class );
    when( result.getUnifiedRepository() ).thenReturn( mockRepo );
    IRepositoryConnector connector = mock( IRepositoryConnector.class );
    when( connector.connect( anyString(), anyString() ) ).thenReturn( result );
    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );
    purRepository.init( mockMeta );
    purRepository.setPurRepositoryConnector( connector );
    ObjectId objectId = mock( ObjectId.class );
    RepositoryFile mockEtcFolder = mock( RepositoryFile.class );
    RepositoryFile mockFolderVisible = mock( RepositoryFile.class );
    RepositoryFile mockRootFolder = mock( RepositoryFile.class );
    RepositoryFileTree mockRepositoryTree = mock( RepositoryFileTree.class );
    String testId = "TEST_ID";
    String visibleFolderId = testId + "2";

    when( objectId.getId() ).thenReturn( testId );
    when( mockRepo.getFileById( testId ) ).thenReturn( mockEtcFolder );
    when( mockRepo.getFile( ClientRepositoryPaths.getEtcFolderPath() ) ).thenReturn( mockEtcFolder );
    when( mockRepo.getFileById( visibleFolderId ) ).thenReturn( mockFolderVisible );


    when( mockEtcFolder.getPath() ).thenReturn( "/etc" );
    when( mockEtcFolder.isFolder() ).thenReturn( true );
    when( mockEtcFolder.getId() ).thenReturn( testId );

    when( mockFolderVisible.getPath() ).thenReturn( "/visible" );
    when( mockFolderVisible.isFolder() ).thenReturn( true );
    when( mockFolderVisible.getId() ).thenReturn( visibleFolderId );

    when( mockRepositoryTree.getFile() ).thenReturn( mockRootFolder );
    when( mockRootFolder.getId() ).thenReturn( "/" );
    when( mockRootFolder.getPath() ).thenReturn( "/" );

    List<RepositoryFile> rootChildren = new ArrayList<>( Arrays.asList( mockEtcFolder, mockFolderVisible ) );
    when( mockRepo.getChildren( argThat( IsInstanceOf.<RepositoryRequest>instanceOf( RepositoryRequest.class ) ) ) )
        .thenReturn( rootChildren );
    when( mockRepo.getFile( "/" ) ).thenReturn( mockRootFolder );
    purRepository.connect( "TEST_USER", "TEST_PASSWORD" );
    int children = purRepository.getRootDir().getNrSubdirectories();
    assertThat( children, equalTo( 1 ) );
  }

  @Test
  public void onlyGlobalVariablesOfLogTablesSetToNull() {
    PurRepositoryExporter purRepoExporter = new PurRepositoryExporter( mock( PurRepository.class ) );
    String hardcodedString = "hardcoded";
    String globalParam = "${" + Const.KETTLE_TRANS_LOG_TABLE + "}";

    StepLogTable stepLogTable = StepLogTable.getDefault( mockedVariableSpace, mockedHasDbInterface );
    stepLogTable.setConnectionName( hardcodedString );
    stepLogTable.setSchemaName( hardcodedString );
    stepLogTable.setTimeoutInDays( hardcodedString );
    stepLogTable.setTableName( globalParam );

    JobEntryLogTable jobEntryLogTable = JobEntryLogTable.getDefault( mockedVariableSpace, mockedHasDbInterface );
    jobEntryLogTable.setConnectionName( hardcodedString );
    jobEntryLogTable.setSchemaName( hardcodedString );
    jobEntryLogTable.setTimeoutInDays( hardcodedString );
    jobEntryLogTable.setTableName( globalParam );

    List<LogTableInterface> logTables = new ArrayList<>();
    logTables.add( jobEntryLogTable );
    logTables.add( stepLogTable );

    purRepoExporter.setGlobalVariablesOfLogTablesNull( logTables );

    for ( LogTableInterface logTable : logTables ) {
      assertEquals( logTable.getConnectionName(), hardcodedString );
      assertEquals( logTable.getSchemaName(), hardcodedString );
      assertEquals( logTable.getTimeoutInDays(), hardcodedString );
      assertEquals( logTable.getTableName(), null );
    }
  }


  @Test
  public void testRevisionsEnabled() throws KettleException {
    PurRepository purRepository = new PurRepository();
    IUnifiedRepository mockRepo = mock( IUnifiedRepository.class );
    RepositoryConnectResult result = mock( RepositoryConnectResult.class );
    when( result.getUnifiedRepository() ).thenReturn( mockRepo );
    IRepositoryConnector connector = mock( IRepositoryConnector.class );
    when( connector.connect( anyString(), anyString() ) ).thenReturn( result );

    RepositoryServiceRegistry registry = mock( RepositoryServiceRegistry.class );
    UnifiedRepositoryLockService lockService = new UnifiedRepositoryLockService( mockRepo );
    when( registry.getService( ILockService.class ) ).thenReturn( lockService );
    when( result.repositoryServiceRegistry() ).thenReturn( registry );

    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );
    purRepository.init( mockMeta );
    purRepository.setPurRepositoryConnector( connector );
    // purRepository.setTest( mockRepo );
    ObjectId objectId = mock( ObjectId.class );

    RepositoryFile mockFileVersioningEnabled = mock( RepositoryFile.class );
    RepositoryFile mockFileVersioningNotEnabled = mock( RepositoryFile.class );
    RepositoryFileTree mockRepositoryTreeChildVersioningEnabled = mock( RepositoryFileTree.class );
    RepositoryFileTree mockRepositoryTreeChildVersioningNotEnabled = mock( RepositoryFileTree.class );

    RepositoryFile mockRootFolder = mock( RepositoryFile.class );
    RepositoryObjectType repositoryObjectType = RepositoryObjectType.TRANSFORMATION;
    RepositoryFileTree mockRepositoryTree = mock( RepositoryFileTree.class );


    String testId = "TEST_ID";
    String testFileId = "TEST_FILE_ID";
    List<RepositoryFileTree> children =
        Arrays.asList( mockRepositoryTreeChildVersioningEnabled, mockRepositoryTreeChildVersioningNotEnabled );
    when( objectId.getId() ).thenReturn( testId );
    when( mockRepo.getFileById( testId ) ).thenReturn( mockFileVersioningEnabled );


    when( mockFileVersioningEnabled.getPath() ).thenReturn( "/home/testuser/path.ktr" );
    when( mockFileVersioningEnabled.getId() ).thenReturn( testFileId );
    when( mockFileVersioningEnabled.getName() ).thenReturn( "path.ktr" );

    when( mockFileVersioningNotEnabled.getPath() ).thenReturn( "/home/testuser/path2.ktr" );
    when( mockFileVersioningNotEnabled.getId() ).thenReturn( testFileId + "2" );
    when( mockFileVersioningNotEnabled.getName() ).thenReturn( "path2.ktr" );


    when( mockRepositoryTreeChildVersioningEnabled.getFile() ).thenReturn( mockFileVersioningEnabled );
    when( mockRepositoryTreeChildVersioningEnabled.getVersionCommentEnabled() ).thenReturn( true );
    when( mockRepositoryTreeChildVersioningEnabled.getVersioningEnabled() ).thenReturn( true );

    when( mockRepositoryTreeChildVersioningNotEnabled.getFile() ).thenReturn( mockFileVersioningEnabled );
    when( mockRepositoryTreeChildVersioningNotEnabled.getVersionCommentEnabled() ).thenReturn( false );
    when( mockRepositoryTreeChildVersioningNotEnabled.getVersioningEnabled() ).thenReturn( false );

    when( mockRepo.getTree( anyString(), anyInt(), anyString(), anyBoolean() ) ).thenReturn( mockRepositoryTree );
    when( mockRepo.getTree( any( RepositoryRequest.class ) ) ).thenReturn( mockRepositoryTree );
    when( mockRepositoryTree.getFile() ).thenReturn( mockRootFolder );
    when( mockRepositoryTree.getChildren() ).thenReturn( children );
    when( mockRootFolder.getId() ).thenReturn( "/" );
    when( mockRootFolder.getPath() ).thenReturn( "/" );
    when( mockRepo.getFile( "/" ) ).thenReturn( mockRootFolder );
    purRepository.connect( "TEST_USER", "TEST_PASSWORD" );
    List<RepositoryElementMetaInterface> repositoryObjects = purRepository.getRootDir().getRepositoryObjects();
    assertThat( repositoryObjects.size(), is( 2 ) );

    // Test Enabled
    RepositoryElementMetaInterface element = repositoryObjects.get( 0 );
    assertThat( element, is( instanceOf( EERepositoryObject.class ) ) );
    EERepositoryObject eeElement = (EERepositoryObject) element;
    assertThat( eeElement.getVersioningEnabled(), is( true ) );
    assertThat( eeElement.getVersionCommentEnabled(), is( true ) );

    // Test Not Enabled
    RepositoryElementMetaInterface element2 = repositoryObjects.get( 1 );
    assertThat( element2, is( instanceOf( EERepositoryObject.class ) ) );
    EERepositoryObject eeElement2 = (EERepositoryObject) element;
    assertThat( eeElement2.getVersioningEnabled(), is( true ) );
    assertThat( eeElement2.getVersionCommentEnabled(), is( true ) );
  }
}
