/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
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

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.JobEntryLogTable;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.logging.StepLogTable;
import org.pentaho.di.core.plugins.ClassLoadingPluginInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.imp.Import;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.RepositoryTestLazySupport;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.pur.model.EERepositoryObject;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.repository.pur.services.ILockService;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings( "squid:S1192" )
public class PurRepositoryUnitTest extends RepositoryTestLazySupport {
  private VariableSpace mockedVariableSpace;
  private HasDatabasesInterface mockedHasDbInterface;

  public PurRepositoryUnitTest( Boolean lazyRepo ) {
    super( lazyRepo );
  }

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
    RepositoryFileTree mockRepositoryFileTree = mock( RepositoryFileTree.class );
    when( mockRepositoryFileTree.getFile() ).thenReturn( mockRootFolder );
    when( mockRepo.getTree( "/", -1, null, true ) ).thenReturn( mockRepositoryFileTree );
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
    // for Lazy Repo
    when( mockRepo.getFile( "/" ) ).thenReturn( mockRootFolder );
    // for Eager Repo
    RepositoryFileTree repositoryFileTree = mock( RepositoryFileTree.class );
    when( mockRepo.getTree( "/", -1, null, true ) ).thenReturn( repositoryFileTree );
    when( repositoryFileTree.getFile() ).thenReturn( mockRootFolder );

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
    ObjectId objectId = mock( ObjectId.class );
    RepositoryFile mockFile = mock( RepositoryFile.class );
    RepositoryFile mockRootFolder = mock( RepositoryFile.class );
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
    when( mockRepo.getChildren( org.mockito.hamcrest.MockitoHamcrest.argThat( IsInstanceOf.<RepositoryRequest>instanceOf( RepositoryRequest.class ) ) ) )
      .thenReturn( rootChildren );
    // for Lazy Repo
    when( mockRepo.getFile( "/" ) ).thenReturn( mockRootFolder );
    // for Eager Repo
    RepositoryFileTree repositoryFileTree = mock( RepositoryFileTree.class );
    when( mockRepo.getTree( "/", -1, null, true ) ).thenReturn( repositoryFileTree );
    when( repositoryFileTree.getFile() ).thenReturn( mockRootFolder );
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
    when( mockEtcFolder.getName() ).thenReturn( "etc" );
    when( mockEtcFolder.isFolder() ).thenReturn( true );
    when( mockEtcFolder.getId() ).thenReturn( testId );

    when( mockFolderVisible.getPath() ).thenReturn( "/visible" );
    when( mockFolderVisible.getName() ).thenReturn( "visible" );
    when( mockFolderVisible.isFolder() ).thenReturn( true );
    when( mockFolderVisible.getId() ).thenReturn( visibleFolderId );

    when( mockRepositoryTree.getFile() ).thenReturn( mockRootFolder );
    when( mockRootFolder.getId() ).thenReturn( "/" );
    when( mockRootFolder.getPath() ).thenReturn( "/" );

    List<RepositoryFile> rootChildren = new ArrayList<>( Arrays.asList( mockEtcFolder, mockFolderVisible ) );
    when( mockRepo.getChildren( org.mockito.hamcrest.MockitoHamcrest.argThat( IsInstanceOf.<RepositoryRequest>instanceOf( RepositoryRequest.class ) ) ) )
      .thenReturn( rootChildren );
    // for Lazy Repo
    when( mockRepo.getFile( "/" ) ).thenReturn( mockRootFolder );
    // for Eager Repo
    RepositoryFileTree repositoryFileTree = mock( RepositoryFileTree.class );
    when( mockRepo.getTree( "/", -1, null, true ) ).thenReturn( repositoryFileTree );
    when( repositoryFileTree.getFile() ).thenReturn( mockRootFolder );
    RepositoryFileTree mockEtcFolderTree = mock( RepositoryFileTree.class );
    when( mockEtcFolderTree.getFile() ).thenReturn( mockEtcFolder );
    RepositoryFileTree mockFolderVisibleTree = mock( RepositoryFileTree.class );
    when( mockFolderVisibleTree.getFile() ).thenReturn( mockFolderVisible );
    when( repositoryFileTree.getChildren() ).thenReturn( new ArrayList<>( Arrays.asList(
      mockEtcFolderTree, mockFolderVisibleTree ) ) );
    purRepository.connect( "TEST_USER", "TEST_PASSWORD" );
    int children = purRepository.getRootDir().getNrSubdirectories();
    assertThat( children, equalTo( 1 ) );
  }

  @Test
  public void onlyGlobalVariablesOfLogTablesSetToNull() {
    try {
      System.setProperty( Const.KETTLE_GLOBAL_LOG_VARIABLES_CLEAR_ON_EXPORT, "true" );

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
        assertNull( logTable.getTableName() );
      }
    } finally {
      System.setProperty( Const.KETTLE_GLOBAL_LOG_VARIABLES_CLEAR_ON_EXPORT, "false" );
    }
  }

  @Test
  public void globalVariablesOfLogTablesNotSetToNull() {
    PurRepositoryExporter purRepoExporter = new PurRepositoryExporter( mock( PurRepository.class ) );
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
      assertEquals( logTable.getConnectionName(), globalParam );
      assertEquals( logTable.getSchemaName(), globalParam );
      assertEquals( logTable.getTimeoutInDays(), globalParam );
      assertEquals( logTable.getTableName(), globalParam );
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
    ObjectId objectId = mock( ObjectId.class );

    RepositoryFile mockFileVersioningEnabled = mock( RepositoryFile.class );
    RepositoryFile mockFileVersioningNotEnabled = mock( RepositoryFile.class );
    RepositoryFileTree mockRepositoryTreeChildVersioningEnabled = mock( RepositoryFileTree.class );
    RepositoryFileTree mockRepositoryTreeChildVersioningNotEnabled = mock( RepositoryFileTree.class );
    RepositoryFile publicFolder = mock( RepositoryFile.class );
    RepositoryFileTree publicFolderTree = mock( RepositoryFileTree.class );

    RepositoryFile mockRootFolder = mock( RepositoryFile.class );
    RepositoryFileTree mockRepositoryTree = mock( RepositoryFileTree.class );


    String testId = "TEST_ID";
    String testFileId = "TEST_FILE_ID";
    List<RepositoryFileTree> children =
      Arrays.asList( mockRepositoryTreeChildVersioningEnabled, mockRepositoryTreeChildVersioningNotEnabled );
    when( objectId.getId() ).thenReturn( testId );
    when( mockRepo.getFileById( testId ) ).thenReturn( mockFileVersioningEnabled );


    when( mockFileVersioningEnabled.getPath() ).thenReturn( "/public/path.ktr" );
    when( mockFileVersioningEnabled.getId() ).thenReturn( testFileId );
    when( mockFileVersioningEnabled.getName() ).thenReturn( "path.ktr" );

    when( mockFileVersioningNotEnabled.getPath() ).thenReturn( "/public/path2.ktr" );
    when( mockFileVersioningNotEnabled.getId() ).thenReturn( testFileId + "2" );
    when( mockFileVersioningNotEnabled.getName() ).thenReturn( "path2.ktr" );

    when( publicFolder.getPath() ).thenReturn( "/public" );
    when( publicFolder.getName() ).thenReturn( "public" );
    when( publicFolder.getId() ).thenReturn( testFileId + "3" );
    when( publicFolder.isFolder() ).thenReturn( true );
    when( publicFolderTree.getFile() ).thenReturn( publicFolder );


    when( mockRepositoryTreeChildVersioningEnabled.getFile() ).thenReturn( mockFileVersioningEnabled );
    when( mockRepositoryTreeChildVersioningEnabled.getVersionCommentEnabled() ).thenReturn( true );
    when( mockRepositoryTreeChildVersioningEnabled.getVersioningEnabled() ).thenReturn( true );

    when( mockRepositoryTreeChildVersioningNotEnabled.getFile() ).thenReturn( mockFileVersioningNotEnabled );
    when( mockRepositoryTreeChildVersioningNotEnabled.getVersionCommentEnabled() ).thenReturn( false );
    when( mockRepositoryTreeChildVersioningNotEnabled.getVersioningEnabled() ).thenReturn( false );

    when( mockRepo.getTree( anyString(), anyInt(), anyString(), anyBoolean() ) ).thenReturn( mockRepositoryTree );
    when( mockRepo.getTree( any( RepositoryRequest.class ) ) ).thenReturn( mockRepositoryTree );

    when( mockRepo.getTree( argThat( (ArgumentMatcher<RepositoryRequest>) new ArgumentMatcher() {
      @Override public boolean matches( Object argument ) {
        return ( (RepositoryRequest) argument ).getPath().equals( "/public" );
      }

      @Override public Class<?> type() {
        return RepositoryRequest.class;
      }
    } ) ) ).thenReturn( publicFolderTree );
    when( mockRepositoryTree.getFile() ).thenReturn( mockRootFolder );
    when( mockRepositoryTree.getChildren() ).thenReturn( new ArrayList<>(
      Collections.singletonList( publicFolderTree ) ) );
    when( publicFolderTree.getChildren() ).thenReturn( children );
    when( mockRootFolder.getId() ).thenReturn( "/" );
    when( mockRootFolder.getPath() ).thenReturn( "/" );
    when( mockRepo.getFile( "/" ) ).thenReturn( mockRootFolder );
    when( mockRepo.getFile( "/public" ) ).thenReturn( publicFolder );
    purRepository.connect( "TEST_USER", "TEST_PASSWORD" );
    when( mockRepo.getTree( "/", -1, null, true ) ).thenReturn( mockRepositoryTree );
    List<RepositoryElementMetaInterface> repositoryObjects =
      purRepository.findDirectory( "/public" ).getRepositoryObjects();
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

  interface PluginMockInterface extends ClassLoadingPluginInterface, PluginInterface {
  }

  @Test
  public void testTransRepoAfterSaveExtensionPoint() throws KettleException {
    PluginMockInterface pluginInterface = mock( PluginMockInterface.class );
    when( pluginInterface.getName() ).thenReturn( KettleExtensionPoint.TransImportAfterSaveToRepo.id );
    when( pluginInterface.getMainType() ).thenReturn( (Class) ExtensionPointInterface.class );
    when( pluginInterface.getIds() ).thenReturn( new String[] { "extensionpointId" } );

    ExtensionPointInterface extensionPoint = mock( ExtensionPointInterface.class );
    when( pluginInterface.loadClass( ExtensionPointInterface.class ) ).thenReturn( extensionPoint );

    PluginRegistry.addPluginType( ExtensionPointPluginType.getInstance() );
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, pluginInterface );

    PurRepository rep = mock( PurRepository.class );
    doCallRealMethod().when( rep )
      .saveTransOrJob( any( ISharedObjectsTransformer.class ), any( RepositoryElementInterface.class ), anyString(),
        any( Calendar.class ), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean() );
    IUnifiedRepository pur = mock( IUnifiedRepository.class );
    doCallRealMethod().when( rep ).setTest( same( pur ) );

    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );
    doCallRealMethod().when( rep ).init( same( mockMeta ) );
    rep.init( mockMeta );
    rep.setTest( pur );

    RepositoryFile file = mock( RepositoryFile.class );
    when( file.getId() ).thenReturn( "id" );
    when( pur.createFile( any( Serializable.class ), any( RepositoryFile.class ), any( IRepositoryFileData.class ),
      anyString() ) ).thenReturn( file );

    TransMeta trans = mock( TransMeta.class );
    when( trans.getRepositoryElementType() ).thenReturn( RepositoryObjectType.TRANSFORMATION );
    when( trans.getName() ).thenReturn( "trans" );
    RepositoryDirectory dir = mock( RepositoryDirectory.class );
    when( dir.getObjectId() ).thenReturn( new StringObjectId( "id" ) );
    when( trans.getRepositoryDirectory() ).thenReturn( dir );

    TransMeta transFromRepo = mock( TransMeta.class );
    when( rep.loadTransformation( any( ObjectId.class ), isNull( String.class ) ) ).thenReturn( transFromRepo );

    ISharedObjectsTransformer transformer = mock( ISharedObjectsTransformer.class );
    rep.saveTransOrJob( transformer, trans, "", Calendar.getInstance(), false, false, false, false, false );

    verify( extensionPoint, times( 1 ) ).callExtensionPoint( any( LogChannelInterface.class ), same( transFromRepo ) );
  }

  @Test( expected = KettleException.class )
  public void testSaveTransOrJob() throws KettleException {
    PurRepository purRepository = new PurRepository();
    RepositoryElementInterface element = Mockito.mock( RepositoryElementInterface.class );
    RepositoryDirectoryInterface directoryInterface = Mockito.mock( RepositoryDirectoryInterface.class );
    Mockito.when( element.getRepositoryDirectory() ).thenReturn( directoryInterface );
    Mockito.when( element.getRepositoryElementType() ).thenReturn( RepositoryObjectType.TRANSFORMATION );
    Mockito.when( directoryInterface.toString() ).thenReturn( Import.ROOT_DIRECTORY );

    purRepository.saveTransOrJob( null, element,
      null, null, false, false, false,
      false, false );
  }

  @Test
  public void testGetJobPathWithoutExtension() {
    PurRepository pur = new PurRepository();
    RepositoryDirectoryInterface rdi = mock( RepositoryDirectoryInterface.class );
    doReturn( mock( ObjectId.class ) ).when( rdi ).getObjectId();
    doReturn( "/home/admin" ).when( rdi ).getPath();

    assertEquals( "/home/admin/job.kjb", pur.getPath( "job", rdi, RepositoryObjectType.JOB ) );
  }

  @Test
  public void testGetJobPathWithExtension() {
    PurRepository pur = new PurRepository();
    RepositoryDirectoryInterface rdi = mock( RepositoryDirectoryInterface.class );
    doReturn( mock( ObjectId.class ) ).when( rdi ).getObjectId();
    doReturn( "/home/admin" ).when( rdi ).getPath();

    assertEquals( "/home/admin/job.kjb", pur.getPath( "job.kjb", rdi, RepositoryObjectType.JOB ) );
  }

  @Test
  public void testGetTransPathWithoutExtension() {
    PurRepository pur = new PurRepository();
    RepositoryDirectoryInterface rdi = mock( RepositoryDirectoryInterface.class );
    doReturn( mock( ObjectId.class ) ).when( rdi ).getObjectId();
    doReturn( "/home/admin" ).when( rdi ).getPath();

    assertEquals( "/home/admin/trans.ktr", pur.getPath( "trans", rdi, RepositoryObjectType.TRANSFORMATION ) );
  }

  @Test
  public void testGetTransPathWithExtension() {
    PurRepository pur = new PurRepository();
    RepositoryDirectoryInterface rdi = mock( RepositoryDirectoryInterface.class );
    doReturn( mock( ObjectId.class ) ).when( rdi ).getObjectId();
    doReturn( "/home/admin" ).when( rdi ).getPath();

    assertEquals( "/home/admin/trans.ktr", pur.getPath( "trans.ktr", rdi, RepositoryObjectType.TRANSFORMATION ) );
  }

  @Test
  public void testCreateValidRepositoryDirectoryForRootHomeFolder() throws KettleException {
    RepositoryDirectoryInterface treeMocked = mock( RepositoryDirectoryInterface.class );
    PurRepository repository = mock( PurRepository.class );
    LazyUnifiedRepositoryDirectory lazy = mock( LazyUnifiedRepositoryDirectory.class );
    String newDirectory = "home/admin1";
    //if root then we can create any folder at home or public folders
    when( treeMocked.isRoot() ).thenReturn( true );
    when( treeMocked.getPath() ).thenReturn( newDirectory );
    when( repository.findDirectory( anyString() ) ).thenReturn( lazy );
    when( repository.createRepositoryDirectory( treeMocked, newDirectory ) ).thenCallRealMethod();

    assertEquals( "/home/admin1", repository.createRepositoryDirectory( treeMocked, newDirectory ).getPath() );
  }

  @Test
  public void testCreateValidRepositoryDirectoryForRootPublicFolder() throws KettleException {
    RepositoryDirectoryInterface treeMocked = mock( RepositoryDirectoryInterface.class );
    PurRepository repository = mock( PurRepository.class );
    LazyUnifiedRepositoryDirectory lazy = mock( LazyUnifiedRepositoryDirectory.class );
    String newDirectory = "public/admin1";
    //if root then we can create any folder at home or public folders
    when( treeMocked.isRoot() ).thenReturn( true );
    when( treeMocked.getPath() ).thenReturn( newDirectory );
    when( repository.findDirectory( anyString() ) ).thenReturn( lazy );
    when( repository.createRepositoryDirectory( treeMocked, newDirectory ) ).thenCallRealMethod();

    assertEquals( "/public/admin1", repository.createRepositoryDirectory( treeMocked, newDirectory ).getPath() );
  }

  @Test( expected = KettleException.class )
  public void testCreateInvalidRepositoryDirectoryForRootAnyOtherFolders() throws KettleException {
    RepositoryDirectoryInterface treeMocked = mock( RepositoryDirectoryInterface.class );
    PurRepository repository = mock( PurRepository.class );
    LazyUnifiedRepositoryDirectory lazy = mock( LazyUnifiedRepositoryDirectory.class );
    String newDirectory = "dummy/admin1";
    //if root then we can ony create folders at home or public folders
    when( treeMocked.isRoot() ).thenReturn( true );
    when( treeMocked.getPath() ).thenReturn( newDirectory );
    when( repository.findDirectory( anyString() ) ).thenReturn( lazy );
    when( repository.createRepositoryDirectory( treeMocked, newDirectory ) ).thenCallRealMethod();

    assertNull( repository.createRepositoryDirectory( treeMocked, newDirectory ).getPath() );
  }

  @Test( expected = KettleException.class )
  public void testCreateInvalidRepositoryDirectoryForRoot() throws KettleException {
    RepositoryDirectoryInterface treeMocked = mock( RepositoryDirectoryInterface.class );
    PurRepository repository = mock( PurRepository.class );
    LazyUnifiedRepositoryDirectory lazy = mock( LazyUnifiedRepositoryDirectory.class );
    String newDirectory = "admin1";
    //if root then we can ony create folders at home or public folders
    when( treeMocked.isRoot() ).thenReturn( true );
    when( treeMocked.getPath() ).thenReturn( newDirectory );
    when( repository.findDirectory( anyString() ) ).thenReturn( lazy );
    when( repository.createRepositoryDirectory( treeMocked, newDirectory ) ).thenCallRealMethod();

    assertNull( repository.createRepositoryDirectory( treeMocked, newDirectory ).getPath() );
  }

  @Test
  public void testCreateValidRepositoryDirectoryForNotRoot() throws KettleException {
    RepositoryDirectoryInterface treeMocked = mock( RepositoryDirectoryInterface.class );
    PurRepository repository = mock( PurRepository.class );
    LazyUnifiedRepositoryDirectory lazy = mock( LazyUnifiedRepositoryDirectory.class );
    String newDirectory = "admin1";
    //if not root then we can create any folder
    when( treeMocked.isRoot() ).thenReturn( false );
    when( treeMocked.getPath() ).thenReturn( newDirectory );
    when( repository.findDirectory( anyString() ) ).thenReturn( lazy );
    when( repository.createRepositoryDirectory( treeMocked, newDirectory ) ).thenCallRealMethod();

    assertEquals( "/admin1", repository.createRepositoryDirectory( treeMocked, newDirectory ).getPath() );
  }

  @Test
  @SuppressWarnings( {"squid:S1075", "squid:S3655"} )
  public void testGetURI() throws URISyntaxException {
    PurRepository purRepo = new PurRepository();
    PurRepositoryMeta repoMeta = new PurRepositoryMeta();
    repoMeta.setRepositoryLocation( new PurRepositoryLocation( "http://localhost:8080/pentaho" ) );
    purRepo.init( repoMeta );
    assertTrue( purRepo.getUri().isPresent() );
    assertThat( purRepo.getUri().get(), equalTo( new URI( "http://localhost:8080/pentaho" ) ) );
  }

}
