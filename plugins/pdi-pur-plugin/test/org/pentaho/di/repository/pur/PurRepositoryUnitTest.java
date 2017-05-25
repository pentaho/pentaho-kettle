/*!
 * Copyright 2010 - 2017 Pentaho Corporation.  All rights reserved.
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

import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.mockito.Mockito;

public class PurRepositoryUnitTest {
  private VariableSpace mockedVariableSpace;
  private HasDatabasesInterface mockedHasDbInterface;

  @Before
  public void init() {
    System.setProperty( Const.KETTLE_TRANS_LOG_TABLE, "KETTLE_STEP_LOG_DB_VALUE" );
    mockedVariableSpace = Mockito.mock( VariableSpace.class );
    mockedHasDbInterface = Mockito.mock( HasDatabasesInterface.class );
  }

  @Test
  public void testGetObjectInformationGetsAclByFileId() throws KettleException {
    PurRepository purRepository = new PurRepository();
    IUnifiedRepository mockRepo = Mockito.mock( IUnifiedRepository.class );
    RepositoryConnectResult result = Mockito.mock( RepositoryConnectResult.class );
    Mockito.when( result.getUnifiedRepository() ).thenReturn( mockRepo );

    RepositoryServiceRegistry registry = Mockito.mock( RepositoryServiceRegistry.class );
    UnifiedRepositoryLockService lockService = new UnifiedRepositoryLockService( mockRepo );
    Mockito.when( registry.getService( ILockService.class ) ).thenReturn( lockService );

    Mockito.when( result.repositoryServiceRegistry() ).thenReturn( registry );
    IRepositoryConnector connector = Mockito.mock( IRepositoryConnector.class );
    Mockito.when( connector.connect( Mockito.anyString(), Mockito.anyString() ) ).thenReturn( result );
    PurRepositoryMeta mockMeta = Mockito.mock( PurRepositoryMeta.class );
    purRepository.init( mockMeta );
    purRepository.setPurRepositoryConnector( connector );
    // purRepository.setTest( mockRepo );
    ObjectId objectId = Mockito.mock( ObjectId.class );
    RepositoryFile mockFile = Mockito.mock( RepositoryFile.class );
    RepositoryFile mockRootFolder = Mockito.mock( RepositoryFile.class );
    RepositoryObjectType repositoryObjectType = RepositoryObjectType.TRANSFORMATION;
    RepositoryFileTree mockRepositoryTree = Mockito.mock( RepositoryFileTree.class );
    String testId = "TEST_ID";
    String testFileId = "TEST_FILE_ID";
    Mockito.when( objectId.getId() ).thenReturn( testId );
    Mockito.when( mockRepo.getFileById( testId ) ).thenReturn( mockFile );
    Mockito.when( mockFile.getPath() ).thenReturn( "/home/testuser/path.ktr" );
    Mockito.when( mockFile.isLocked() ).thenReturn( false );
    Mockito.when( mockFile.getId() ).thenReturn( testFileId );
    Mockito.when( mockRepo.getTree( Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean() ) ).thenReturn( mockRepositoryTree );
    Mockito.when( mockRepositoryTree.getFile() ).thenReturn( mockRootFolder );
    Mockito.when( mockRootFolder.getId() ).thenReturn( "/" );
    Mockito.when( mockRootFolder.getPath() ).thenReturn( "/" );
    Mockito.when( mockRepo.getFile( "/" ) ).thenReturn( mockRootFolder );
    purRepository.connect( "TEST_USER", "TEST_PASSWORD" );
    purRepository.getObjectInformation( objectId, repositoryObjectType );
    Mockito.verify( mockRepo ).getAcl( testFileId );
  }


  @Test
  public void testRootIsNotVisible() throws KettleException {
    PurRepository purRepository = new PurRepository();
    IUnifiedRepository mockRepo = Mockito.mock( IUnifiedRepository.class );
    RepositoryConnectResult result = Mockito.mock( RepositoryConnectResult.class );
    Mockito.when( result.getUnifiedRepository() ).thenReturn( mockRepo );
    IRepositoryConnector connector = Mockito.mock( IRepositoryConnector.class );
    Mockito.when( connector.connect( Mockito.anyString(), Mockito.anyString() ) ).thenReturn( result );
    PurRepositoryMeta mockMeta = Mockito.mock( PurRepositoryMeta.class );
    purRepository.init( mockMeta );
    purRepository.setPurRepositoryConnector( connector );

    RepositoryFile mockRootFolder = Mockito.mock( RepositoryFile.class );

    Mockito.when( mockRootFolder.getId() ).thenReturn( "/" );
    Mockito.when( mockRootFolder.getPath() ).thenReturn( "/" );
    Mockito.when( mockRepo.getFile( "/" ) ).thenReturn( mockRootFolder );
    purRepository.connect( "TEST_USER", "TEST_PASSWORD" );

    RepositoryDirectoryInterface rootDir = purRepository.getRootDir();
    Assert.assertFalse( rootDir.isVisible() );
  }


  @Test
  public void testEtcIsNotThereInGetChildren() throws KettleException {
    PurRepository purRepository = new PurRepository();
    IUnifiedRepository mockRepo = Mockito.mock( IUnifiedRepository.class );
    RepositoryConnectResult result = Mockito.mock( RepositoryConnectResult.class );
    Mockito.when( result.getUnifiedRepository() ).thenReturn( mockRepo );
    IRepositoryConnector connector = Mockito.mock( IRepositoryConnector.class );
    Mockito.when( connector.connect( Mockito.anyString(), Mockito.anyString() ) ).thenReturn( result );
    PurRepositoryMeta mockMeta = Mockito.mock( PurRepositoryMeta.class );
    purRepository.init( mockMeta );
    purRepository.setPurRepositoryConnector( connector );
    // purRepository.setTest( mockRepo );
    ObjectId objectId = Mockito.mock( ObjectId.class );
    RepositoryFile mockFile = Mockito.mock( RepositoryFile.class );
    RepositoryFile mockRootFolder = Mockito.mock( RepositoryFile.class );
    RepositoryObjectType repositoryObjectType = RepositoryObjectType.TRANSFORMATION;
    RepositoryFileTree mockRepositoryTree = Mockito.mock( RepositoryFileTree.class );
    String testId = "TEST_ID";
    String testFileId = "TEST_FILE_ID";
    Mockito.when( objectId.getId() ).thenReturn( testId );
    Mockito.when( mockRepo.getFileById( testId ) ).thenReturn( mockFile );
    Mockito.when( mockFile.getPath() ).thenReturn( "/etc" );
    Mockito.when( mockFile.getId() ).thenReturn( testFileId );

    Mockito.when( mockRepositoryTree.getFile() ).thenReturn( mockRootFolder );
    Mockito.when( mockRootFolder.getId() ).thenReturn( "/" );
    Mockito.when( mockRootFolder.getPath() ).thenReturn( "/" );

    List<RepositoryFile> rootChildren = new ArrayList<>( Collections.singletonList( mockFile ) );
    Mockito.when( mockRepo.getChildren( Mockito.argThat( IsInstanceOf.<RepositoryRequest>instanceOf( RepositoryRequest.class ) ) ) )
        .thenReturn( rootChildren );
    Mockito.when( mockRepo.getFile( "/" ) ).thenReturn( mockRootFolder );
    purRepository.connect( "TEST_USER", "TEST_PASSWORD" );
    List<RepositoryDirectoryInterface> children = purRepository.getRootDir().getChildren();
    Assert.assertThat( children, Matchers.empty() );
  }


  @Test
  public void testEtcIsNotThereInGetNrDirectories() throws KettleException {
    PurRepository purRepository = new PurRepository();
    IUnifiedRepository mockRepo = Mockito.mock( IUnifiedRepository.class );
    RepositoryConnectResult result = Mockito.mock( RepositoryConnectResult.class );
    Mockito.when( result.getUnifiedRepository() ).thenReturn( mockRepo );
    IRepositoryConnector connector = Mockito.mock( IRepositoryConnector.class );
    Mockito.when( connector.connect( Mockito.anyString(), Mockito.anyString() ) ).thenReturn( result );
    PurRepositoryMeta mockMeta = Mockito.mock( PurRepositoryMeta.class );
    purRepository.init( mockMeta );
    purRepository.setPurRepositoryConnector( connector );
    ObjectId objectId = Mockito.mock( ObjectId.class );
    RepositoryFile mockEtcFolder = Mockito.mock( RepositoryFile.class );
    RepositoryFile mockFolderVisible = Mockito.mock( RepositoryFile.class );
    RepositoryFile mockRootFolder = Mockito.mock( RepositoryFile.class );
    RepositoryFileTree mockRepositoryTree = Mockito.mock( RepositoryFileTree.class );
    String testId = "TEST_ID";
    String visibleFolderId = testId + "2";

    Mockito.when( objectId.getId() ).thenReturn( testId );
    Mockito.when( mockRepo.getFileById( testId ) ).thenReturn( mockEtcFolder );
    Mockito.when( mockRepo.getFile( ClientRepositoryPaths.getEtcFolderPath() ) ).thenReturn( mockEtcFolder );
    Mockito.when( mockRepo.getFileById( visibleFolderId ) ).thenReturn( mockFolderVisible );


    Mockito.when( mockEtcFolder.getPath() ).thenReturn( "/etc" );
    Mockito.when( mockEtcFolder.isFolder() ).thenReturn( true );
    Mockito.when( mockEtcFolder.getId() ).thenReturn( testId );

    Mockito.when( mockFolderVisible.getPath() ).thenReturn( "/visible" );
    Mockito.when( mockFolderVisible.isFolder() ).thenReturn( true );
    Mockito.when( mockFolderVisible.getId() ).thenReturn( visibleFolderId );

    Mockito.when( mockRepositoryTree.getFile() ).thenReturn( mockRootFolder );
    Mockito.when( mockRootFolder.getId() ).thenReturn( "/" );
    Mockito.when( mockRootFolder.getPath() ).thenReturn( "/" );

    List<RepositoryFile> rootChildren = new ArrayList<>( Arrays.asList( mockEtcFolder, mockFolderVisible ) );
    Mockito.when( mockRepo.getChildren( Mockito.argThat( IsInstanceOf.<RepositoryRequest>instanceOf( RepositoryRequest.class ) ) ) )
        .thenReturn( rootChildren );
    Mockito.when( mockRepo.getFile( "/" ) ).thenReturn( mockRootFolder );
    purRepository.connect( "TEST_USER", "TEST_PASSWORD" );
    int children = purRepository.getRootDir().getNrSubdirectories();
    Assert.assertThat( children, Matchers.equalTo( 1 ) );
  }

  @Test
  public void onlyGlobalVariablesOfLogTablesSetToNull() {
    try {
      System.setProperty( Const.KETTLE_GLOBAL_LOG_VARIABLES_CLEAR_ON_EXPORT, "true" );

      PurRepositoryExporter purRepoExporter = new PurRepositoryExporter( Mockito.mock( PurRepository.class ) );
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
        Assert.assertEquals( logTable.getConnectionName(), hardcodedString );
        Assert.assertEquals( logTable.getSchemaName(), hardcodedString );
        Assert.assertEquals( logTable.getTimeoutInDays(), hardcodedString );
        Assert.assertEquals( logTable.getTableName(), null );
      }
    } finally {
      System.setProperty( Const.KETTLE_GLOBAL_LOG_VARIABLES_CLEAR_ON_EXPORT, "false" );
    }
  }

  @Test
  public void globalVariablesOfLogTablesNotSetToNull() {
    PurRepositoryExporter purRepoExporter = new PurRepositoryExporter( Mockito.mock( PurRepository.class ) );
    String globalParam = "${" + Const.KETTLE_TRANS_LOG_TABLE + "}";

    StepLogTable stepLogTable = StepLogTable.getDefault( mockedVariableSpace, mockedHasDbInterface );
    stepLogTable.setConnectionName( globalParam );
    stepLogTable.setSchemaName( globalParam );
    stepLogTable.setTimeoutInDays( globalParam );
    stepLogTable.setTableName( globalParam );

    JobEntryLogTable jobEntryLogTable = JobEntryLogTable.getDefault( mockedVariableSpace, mockedHasDbInterface );
    jobEntryLogTable.setConnectionName( globalParam );
    jobEntryLogTable.setSchemaName( globalParam );
    jobEntryLogTable.setTimeoutInDays( globalParam );
    jobEntryLogTable.setTableName( globalParam );

    List<LogTableInterface> logTables = new ArrayList<>();
    logTables.add( jobEntryLogTable );
    logTables.add( stepLogTable );

    purRepoExporter.setGlobalVariablesOfLogTablesNull( logTables );

    for ( LogTableInterface logTable : logTables ) {
      Assert.assertEquals( logTable.getConnectionName(), globalParam );
      Assert.assertEquals( logTable.getSchemaName(), globalParam );
      Assert.assertEquals( logTable.getTimeoutInDays(), globalParam );
      Assert.assertEquals( logTable.getTableName(), globalParam );
    }
  }


  @Test
  public void testRevisionsEnabled() throws KettleException {
    PurRepository purRepository = new PurRepository();
    IUnifiedRepository mockRepo = Mockito.mock( IUnifiedRepository.class );
    RepositoryConnectResult result = Mockito.mock( RepositoryConnectResult.class );
    Mockito.when( result.getUnifiedRepository() ).thenReturn( mockRepo );
    IRepositoryConnector connector = Mockito.mock( IRepositoryConnector.class );
    Mockito.when( connector.connect( Mockito.anyString(), Mockito.anyString() ) ).thenReturn( result );

    RepositoryServiceRegistry registry = Mockito.mock( RepositoryServiceRegistry.class );
    UnifiedRepositoryLockService lockService = new UnifiedRepositoryLockService( mockRepo );
    Mockito.when( registry.getService( ILockService.class ) ).thenReturn( lockService );
    Mockito.when( result.repositoryServiceRegistry() ).thenReturn( registry );

    PurRepositoryMeta mockMeta = Mockito.mock( PurRepositoryMeta.class );
    purRepository.init( mockMeta );
    purRepository.setPurRepositoryConnector( connector );
    // purRepository.setTest( mockRepo );
    ObjectId objectId = Mockito.mock( ObjectId.class );

    RepositoryFile mockFileVersioningEnabled = Mockito.mock( RepositoryFile.class );
    RepositoryFile mockFileVersioningNotEnabled = Mockito.mock( RepositoryFile.class );
    RepositoryFileTree mockRepositoryTreeChildVersioningEnabled = Mockito.mock( RepositoryFileTree.class );
    RepositoryFileTree mockRepositoryTreeChildVersioningNotEnabled = Mockito.mock( RepositoryFileTree.class );

    RepositoryFile mockRootFolder = Mockito.mock( RepositoryFile.class );
    RepositoryObjectType repositoryObjectType = RepositoryObjectType.TRANSFORMATION;
    RepositoryFileTree mockRepositoryTree = Mockito.mock( RepositoryFileTree.class );


    String testId = "TEST_ID";
    String testFileId = "TEST_FILE_ID";
    List<RepositoryFileTree> children =
        Arrays.asList( mockRepositoryTreeChildVersioningEnabled, mockRepositoryTreeChildVersioningNotEnabled );
    Mockito.when( objectId.getId() ).thenReturn( testId );
    Mockito.when( mockRepo.getFileById( testId ) ).thenReturn( mockFileVersioningEnabled );


    Mockito.when( mockFileVersioningEnabled.getPath() ).thenReturn( "/home/testuser/path.ktr" );
    Mockito.when( mockFileVersioningEnabled.getId() ).thenReturn( testFileId );
    Mockito.when( mockFileVersioningEnabled.getName() ).thenReturn( "path.ktr" );

    Mockito.when( mockFileVersioningNotEnabled.getPath() ).thenReturn( "/home/testuser/path2.ktr" );
    Mockito.when( mockFileVersioningNotEnabled.getId() ).thenReturn( testFileId + "2" );
    Mockito.when( mockFileVersioningNotEnabled.getName() ).thenReturn( "path2.ktr" );


    Mockito.when( mockRepositoryTreeChildVersioningEnabled.getFile() ).thenReturn( mockFileVersioningEnabled );
    Mockito.when( mockRepositoryTreeChildVersioningEnabled.getVersionCommentEnabled() ).thenReturn( true );
    Mockito.when( mockRepositoryTreeChildVersioningEnabled.getVersioningEnabled() ).thenReturn( true );

    Mockito.when( mockRepositoryTreeChildVersioningNotEnabled.getFile() ).thenReturn( mockFileVersioningEnabled );
    Mockito.when( mockRepositoryTreeChildVersioningNotEnabled.getVersionCommentEnabled() ).thenReturn( false );
    Mockito.when( mockRepositoryTreeChildVersioningNotEnabled.getVersioningEnabled() ).thenReturn( false );

    Mockito.when( mockRepo.getTree( Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean() ) ).thenReturn( mockRepositoryTree );
    Mockito.when( mockRepo.getTree( Mockito.any( RepositoryRequest.class ) ) ).thenReturn( mockRepositoryTree );
    Mockito.when( mockRepositoryTree.getFile() ).thenReturn( mockRootFolder );
    Mockito.when( mockRepositoryTree.getChildren() ).thenReturn( children );
    Mockito.when( mockRootFolder.getId() ).thenReturn( "/" );
    Mockito.when( mockRootFolder.getPath() ).thenReturn( "/" );
    Mockito.when( mockRepo.getFile( "/" ) ).thenReturn( mockRootFolder );
    purRepository.connect( "TEST_USER", "TEST_PASSWORD" );
    List<RepositoryElementMetaInterface> repositoryObjects = purRepository.getRootDir().getRepositoryObjects();
    Assert.assertThat( repositoryObjects.size(), Is.is( 2 ) );

    // Test Enabled
    RepositoryElementMetaInterface element = repositoryObjects.get( 0 );
    Assert.assertThat( element, Is.is( Matchers.instanceOf( EERepositoryObject.class ) ) );
    EERepositoryObject eeElement = (EERepositoryObject) element;
    Assert.assertThat( eeElement.getVersioningEnabled(), Is.is( true ) );
    Assert.assertThat( eeElement.getVersionCommentEnabled(), Is.is( true ) );

    // Test Not Enabled
    RepositoryElementMetaInterface element2 = repositoryObjects.get( 1 );
    Assert.assertThat( element2, Is.is( Matchers.instanceOf( EERepositoryObject.class ) ) );
    EERepositoryObject eeElement2 = (EERepositoryObject) element;
    Assert.assertThat( eeElement2.getVersioningEnabled(), Is.is( true ) );
    Assert.assertThat( eeElement2.getVersionCommentEnabled(), Is.is( true ) );
  }
}
