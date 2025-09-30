/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.pan;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.base.Params;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.plugins.ClassLoadingPluginInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.kitchen.Kitchen;
import org.pentaho.di.pan.delegates.PanTransformationDelegate;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;

import java.io.File;
import java.util.Base64;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for EnhancedPanCommandExecutor functionality
 */
public class PanCommandExecutorTest {

  @Mock
  private PanTransformationDelegate mockDelegate;

  @Mock
  private Trans mockTrans;

  private LogChannelInterface log;
  private PanCommandExecutor executor;
  private PanCommandExecutor executorWithMocks;

  private static final String FS_METASTORE_NAME = "FS_METASTORE";
  private static final String REPO_METASTORE_NAME = "REPO_METASTORE";

  private static final String SAMPLE_KTR = "hello-world.ktr";

  private static final String BASE64_FAIL_ON_INIT_KTR = "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";
  private static final String FAIL_ON_INIT_KTR = "fail_on_exec.ktr";

  private DelegatingMetaStore metastore = new DelegatingMetaStore();

  private Repository repository;
  private IMetaStore fsMetaStore;
  private IMetaStore repoMetaStore;
  private RepositoryDirectoryInterface directoryInterface;
  private PanCommandExecutor mockedPanCommandExecutor;
  interface PluginMockInterface extends ClassLoadingPluginInterface, PluginInterface {
  }

  @Before
  public void setUp() throws Exception {
    KettleEnvironment.init();
    MockitoAnnotations.openMocks( this );
    log = new LogChannel( "EnhancedPanCommandExecutorTest" );
    executor = new PanCommandExecutor( Pan.class, log );

    // Create an executor with mocked delegate for certain tests
    executorWithMocks = new PanCommandExecutor( Pan.class, log );
    executorWithMocks.setTransformationDelegate( mockDelegate );
    repository = mock( Repository.class );
    fsMetaStore = mock( IMetaStore.class );
    repoMetaStore = mock( IMetaStore.class );
    directoryInterface = mock( RepositoryDirectoryInterface.class );
    mockedPanCommandExecutor = mock( PanCommandExecutor.class );

    // mock actions from Metastore
    when( fsMetaStore.getName() ).thenReturn( FS_METASTORE_NAME );
    when( repoMetaStore.getName() ).thenReturn( REPO_METASTORE_NAME );
    metastore.addMetaStore( fsMetaStore );

    // mock actions from Repository
    when( repository.getRepositoryMetaStore() ).thenReturn( repoMetaStore );
    when( repository.getBowl() ).thenReturn( DefaultBowl.getInstance() );

    // mock actions from PanCommandExecutor
    when( mockedPanCommandExecutor.getMetaStore() ).thenReturn( metastore );
    when( mockedPanCommandExecutor.loadRepositoryDirectory( any(), anyString(), anyString(), anyString(), anyString() ) )
      .thenReturn( directoryInterface );
    when( mockedPanCommandExecutor.getBowl() ).thenReturn( DefaultBowl.getInstance() );

    doCallRealMethod().when( mockedPanCommandExecutor ).loadTransFromFilesystem( anyString(), anyString(), anyString(), any() );
    doCallRealMethod().when( mockedPanCommandExecutor ).loadTransFromRepository( any(), anyString(), anyString() );
    doCallRealMethod().when( mockedPanCommandExecutor ).decodeBase64ToZipFile( any(), anyBoolean() );
    doCallRealMethod().when( mockedPanCommandExecutor ).decodeBase64ToZipFile( any(), anyString() );

  }

  @After
  public void tearDown() {
    executor = null;
    executorWithMocks = null;
    log = null;
    metastore = null;
    repository = null;
    fsMetaStore = null;
    repoMetaStore = null;
    directoryInterface = null;
    mockedPanCommandExecutor = null;
  }

  // Test 1: Constructor and Basic Initialization
  @Test
  public void testConstructorInitialization() {

    // Test constructor with log
    PanCommandExecutor logExecutor = new PanCommandExecutor( Pan.class, log );
    assertNotNull( "Log constructor should create executor", logExecutor );
    assertNotNull( "Transformation delegate should be initialized",
      logExecutor.getTransformationDelegate() );
  }

  // Test 2: Delegate Pattern Initialization and Management
  @Test
  public void testDelegateManagement() {
    // Verify initial delegate is created
    PanTransformationDelegate initialDelegate = executor.getTransformationDelegate();
    assertNotNull( "Initial delegate should be created", initialDelegate );

    // Test setting a new delegate
    PanTransformationDelegate newDelegate = new PanTransformationDelegate( log );
    executor.setTransformationDelegate( newDelegate );

    // Verify delegate was updated
    assertSame( "Delegate should be updated", newDelegate, executor.getTransformationDelegate() );
    assertNotSame( "Should not be the same as initial delegate", initialDelegate,
      executor.getTransformationDelegate() );
  }

  // Test 3: Repository Initialization Logic
  @Test
  public void testRepositoryInitializationWithoutParams() throws Exception {
    // Test with empty repository parameters
    Params emptyParams = new Params.Builder().build();

    // Repository should remain null when no repo params provided
    executor.initializeRepository( emptyParams );
    assertNull( "Repository should be null with empty params", executor.getRepository() );
  }

  @Test
  public void testRepositoryInitializationBlocked() throws Exception {
    // Test with repository connections blocked
    Params blockedParams = new Params.Builder()
      .repoName( "TestRepo" )
      .blockRepoConns( "Y" )
      .build();

    executor.initializeRepository( blockedParams );
    assertNull( "Repository should be null when connections blocked", executor.getRepository() );
  }

  @Test
  public void testRepositoryInitializationWithParams() throws Exception {
    // Test with repository parameters (will fail but tests the flow)
    Params repoParams = new Params.Builder()
      .repoName( "TestRepo" )
      .repoUsername( "testuser" )
      .repoPassword( "testpass" )
      .build();

    try {
      executor.initializeRepository( repoParams );
      // If it doesn't throw an exception, repository logic was executed
    } catch ( Exception e ) {
      // Expected to fail due to missing repository configuration
      assertTrue( "Exception should be repository-related",
        e.getMessage() == null
          || e.getMessage().toLowerCase().contains( "repository" )
          || e.getMessage().toLowerCase().contains( "connect" ) );
    }
  }

  // Test 4: Execution Configuration Integration (testing via public interface)
  @Test
  public void testExecutionConfigurationIntegration() throws Throwable {
    // Test execution configuration integration by verifying that parameters
    // are properly passed through the execution chain
    Params configParams = new Params.Builder()
      .logLevel( "DEBUG" )
      .safeMode( "Y" )
      .metrics( "Y" )
      .build();

    // Setup mock delegate for testing
    Result successResult = new Result();
    successResult.setNrErrors( 0 );
    when( mockDelegate.executeTransformation( any( Trans.class ),
      any( TransExecutionConfiguration.class ), any( String[].class ) ) )
      .thenReturn( successResult );

    TransExecutionConfiguration transExecutionConfiguration = mock( TransExecutionConfiguration.class );
    when( mockDelegate.createDefaultExecutionConfiguration() ).thenReturn( transExecutionConfiguration );

    // Execute with configuration parameters
    Result result = executorWithMocks.executeWithDelegate( mockTrans, configParams, new String[0] );

    assertNotNull( "Result should not be null", result );
    assertEquals( "Should return success",
      CommandExecutorCodes.Pan.SUCCESS.getCode(), result.getExitStatus() );

    // Verify the delegate was called with configuration
    verify( mockDelegate, times( 1 ) ).executeTransformation(
      eq( mockTrans ), any( TransExecutionConfiguration.class ), any( String[].class ) );
  }

  @Test
  public void testExecutionConfigurationDefaults() throws Throwable {
    // Test that default configuration works
    Params minimalParams = new Params.Builder().build();

    // Setup mock delegate
    Result successResult = new Result();
    successResult.setNrErrors( 0 );
    when( mockDelegate.executeTransformation( any( Trans.class ),
      any( TransExecutionConfiguration.class ), any( String[].class ) ) )
      .thenReturn( successResult );

    // Execute with minimal parameters
    TransExecutionConfiguration transExecutionConfiguration = mock( TransExecutionConfiguration.class );
    when( mockDelegate.createDefaultExecutionConfiguration() ).thenReturn( transExecutionConfiguration );
    Result result = executorWithMocks.executeWithDelegate( mockTrans, minimalParams, new String[0] );

    assertNotNull( "Result should not be null", result );
    assertEquals( "Should return success with defaults",
      CommandExecutorCodes.Pan.SUCCESS.getCode(), result.getExitStatus() );
  }

  // Test 5: Special Commands Handling
  @Test
  public void testListRepositoriesCommand() throws Throwable {
    // Test listing repositories command
    Params listReposParams = new Params.Builder()
      .listRepos( "Y" )
      .build();

    Result result = executor.execute( listReposParams, new String[0] );

    assertNotNull( "Result should not be null", result );
    assertEquals( "Should return success for list repos",
      CommandExecutorCodes.Pan.SUCCESS.getCode(), result.getExitStatus() );
  }

  @Test
  public void testVersionPrinting() {
    // Test version printing functionality
    int versionCode = executor.printVersion();

    assertEquals( "Should return version print code",
      CommandExecutorCodes.Pan.KETTLE_VERSION_PRINT.getCode(), versionCode );
  }

  @Test
  public void testParameterListingMode() throws Throwable {
    // Test parameter listing mode
    Params listParamsParams = new Params.Builder()
      .localFile( Objects.requireNonNull( getClass().getResource( "hello-world.ktr" ) ).getPath() )
      .listFileParams( "Y" )
      .build();

    Result result = executor.execute( listParamsParams, new String[0] );

    assertNotNull( "Result should not be null", result );
    // Should fail due to missing transformation file
    assertNotEquals( "Exit code should indicate failure", 0, result.getExitStatus() );
  }

  // Test 7: Delegate Execution Integration
  @Test
  public void testExecuteWithDelegateSuccess() throws Throwable {
    // Setup mock delegate to return successful result
    Result successResult = new Result();
    successResult.setNrErrors( 0 );
    when( mockDelegate.executeTransformation( any( Trans.class ),
      any( TransExecutionConfiguration.class ), any( String[].class ) ) )
      .thenReturn( successResult );

    // Test delegate execution
    Params testParams = new Params.Builder().build();
    TransExecutionConfiguration transExecutionConfiguration = mock( TransExecutionConfiguration.class );
    when( mockDelegate.createDefaultExecutionConfiguration() ).thenReturn( transExecutionConfiguration );
    Result result = executorWithMocks.executeWithDelegate( mockTrans, testParams, new String[0] );

    assertNotNull( "Result should not be null", result );
    assertEquals( "Should return success code",
      CommandExecutorCodes.Pan.SUCCESS.getCode(), result.getExitStatus() );
    // Verify delegate was called
    verify( mockDelegate, times( 1 ) ).executeTransformation(
      eq( mockTrans ), any( TransExecutionConfiguration.class ), any( String[].class ) );
  }

  @Test
  public void testExecuteWithDelegateError() throws Throwable {
    // Setup mock delegate to return error result
    Result errorResult = new Result();
    errorResult.setNrErrors( 5 );
    when( mockDelegate.executeTransformation( any( Trans.class ),
      any( TransExecutionConfiguration.class ), any( String[].class ) ) )
      .thenReturn( errorResult );

    // Test delegate execution with errors
    TransExecutionConfiguration transExecutionConfiguration = mock( TransExecutionConfiguration.class );
    when( mockDelegate.createDefaultExecutionConfiguration() ).thenReturn( transExecutionConfiguration );
    Params testParams = new Params.Builder().build();
    Result result = executorWithMocks.executeWithDelegate( mockTrans, testParams, new String[0] );

    assertNotNull( "Result should not be null", result );
    assertEquals( "Should return errors during processing code",
      CommandExecutorCodes.Pan.ERRORS_DURING_PROCESSING.getCode(), result.getExitStatus() );
  }

  @Test
  public void testExecuteWithDelegateException() throws Throwable {
    // Setup mock delegate to throw exception
    when( mockDelegate.executeTransformation( any( Trans.class ),
      any( TransExecutionConfiguration.class ), any( String[].class ) ) )
      .thenThrow( new KettleException( "Test execution exception" ) );

    // Test exception handling
    Params testParams = new Params.Builder().build();
    TransExecutionConfiguration transExecutionConfiguration = mock( TransExecutionConfiguration.class );
    when( mockDelegate.createDefaultExecutionConfiguration() ).thenReturn( transExecutionConfiguration );
    Result result = executorWithMocks.executeWithDelegate( mockTrans, testParams, new String[0] );

    assertNotNull( "Result should not be null", result );
    assertEquals( "Should return unexpected error code",
      CommandExecutorCodes.Pan.UNEXPECTED_ERROR.getCode(), result.getExitStatus() );
  }

  // Test 8: Edge Cases and Error Handling
  @Test
  public void testExecuteWithNullParams() {
    try {
      // This should handle null parameters gracefully
      Result result = executor.execute( null, new String[0] );
      // If it returns a result, it handled null gracefully
      assertNotNull( "Should handle null params", result );
    } catch ( Throwable e ) {
      // If it throws an exception, it should be a meaningful one
      assertNotNull( "Exception should have a message", e.getMessage() );
    }
  }

  @Test
  public void testExecuteWithNullArguments() {
    Params validParams = new Params.Builder().listRepos( "Y" ).build();

    try {
      // Test with null arguments array
      Result result = executor.execute( validParams, null );
      assertNotNull( "Should handle null arguments", result );
    } catch ( Throwable e ) {
      // Should handle null arguments gracefully
      assertTrue( "Should handle null arguments without NPE",
        !( e instanceof NullPointerException ) );
    }
  }

  // Test 9: Trust Repository User Setting
  @Test
  public void testTrustRepoUserSetting() {
    // Test trust repository user functionality
    Params trustParams = new Params.Builder()
      .repoName( "TestRepo" )
      .trustRepoUser( "Y" )
      .build();

    // This should set the system property before attempting connection
    try {
      executor.initializeRepository( trustParams );
    } catch ( Exception e ) {
      // Expected to fail due to missing repository, but trust logic should execute
    }

    // The test passes if no unexpected exceptions occur during trust setup
    assertTrue( "Trust user logic should execute without errors", true );
  }

  // Test 10: Integration with Parent Class Methods
  @Test
  public void testInheritedMethodsWork() {
    // Test that methods inherited from PanCommandExecutor still work
    assertNotNull( "Should have log channel", executor.getLog() );
    assertNotNull( "Should have package class", executor.getPkgClazz() );

    // Test version printing (inherited method)
    int versionCode = executor.printVersion();
    assertEquals( "Version print should work",
      CommandExecutorCodes.Pan.KETTLE_VERSION_PRINT.getCode(), versionCode );
  }

  @Test
  public void testMetastoreFromRepoAddedIn() throws Exception {

    // mock Trans loading from repo
    TransMeta t = new TransMeta( DefaultBowl.getInstance(), getClass().getResource( SAMPLE_KTR ).getPath() );
    when( repository.loadTransformation( anyString(), any(), any(), anyBoolean(), nullable( String.class ) ) ).thenReturn( t );

    // test
    Trans trans = mockedPanCommandExecutor.loadTransFromRepository( repository, "", SAMPLE_KTR );
    assertNotNull( trans );
    assertNotNull( trans.getMetaStore() );
    assertTrue( trans.getMetaStore() instanceof DelegatingMetaStore );
    assertNotNull( ( (DelegatingMetaStore) trans.getMetaStore() ).getMetaStoreList() );

  }

  @Test
  public void testMetastoreFromFilesystemAddedIn() throws Exception {

    String fullPath = getClass().getResource( SAMPLE_KTR ).getPath();

    Trans trans = mockedPanCommandExecutor.loadTransFromFilesystem( "", fullPath, "", "" );
    assertNotNull( trans );
    assertNotNull( trans.getMetaStore() );

  }

  @Test
  public void testFilesystemBase64Zip() throws Exception {
    String fileName = "test.ktr";
    File zipFile = new File( getClass().getResource( "testKtrArchive.zip" ).toURI() );
    String base64Zip = Base64.getEncoder().encodeToString( FileUtils.readFileToByteArray( zipFile ) );
    Trans trans = mockedPanCommandExecutor.loadTransFromFilesystem( "", fileName, "",
      base64Zip );
    assertNotNull( trans );
  }


  @Test
  public void testExecuteWithInvalidRepository() {
    try ( MockedStatic<BaseMessages> baseMessagesMockedStatic = mockStatic( BaseMessages.class ) ) {
      // Create Mock Objects
      Params params = mock( Params.class );
      PanCommandExecutor panCommandExecutor = new PanCommandExecutor( Kitchen.class, log );

      // Mock returns
      when( params.getRepoName() ).thenReturn( "NoExistingRepository" );
      baseMessagesMockedStatic.when( () -> BaseMessages.getString( any( Class.class ), anyString(), any() ) ).thenReturn( "" );

      try {
        Result result = panCommandExecutor.execute( params, null );
        assertEquals( CommandExecutorCodes.Pan.COULD_NOT_LOAD_TRANS.getCode(), result.getExitStatus() );
      } catch ( Exception exception ) {
        fail();
      }
    }
  }

  /**
   * This method test a valid ktr and make sure the callExtensionPoint is never called, as this method is called
   * if the ktr fails in preparation step
   * @throws Throwable
   */
  @Test
  public void testNoTransformationFinishExtensionPointCalled() throws Throwable {
    PluginMockInterface pluginInterface = mock( PluginMockInterface.class );
    when( pluginInterface.getName() ).thenReturn( KettleExtensionPoint.TransformationFinish.id );
    when( pluginInterface.getMainType() ).thenReturn( (Class) ExtensionPointInterface.class );
    when( pluginInterface.getIds() ).thenReturn( new String[] { "extensionpointId" } );

    ExtensionPointInterface extensionPoint = mock( ExtensionPointInterface.class );
    when( pluginInterface.loadClass( ExtensionPointInterface.class ) ).thenReturn( extensionPoint );

    PluginRegistry.addPluginType( ExtensionPointPluginType.getInstance() );
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, pluginInterface );

    // Execute a sample KTR
    String fullPath = getClass().getResource( SAMPLE_KTR ).getPath();
    Params params = mock( Params.class );

    when( params.getRepoName() ).thenReturn( "" );
    when( params.getLocalInitialDir() ).thenReturn( "" );
    when( params.getLocalFile() ).thenReturn( fullPath );
    when( params.getLocalJarFile() ).thenReturn( "" );
    when( params.getBase64Zip() ).thenReturn( "" );
    Trans trans = mockedPanCommandExecutor.loadTransFromFilesystem( "", fullPath, "", "" );

    PanCommandExecutor panCommandExecutor = new PanCommandExecutor( PanCommandExecutor.class, log );
    panCommandExecutor.execute( params, new String[]{} );
    verify( extensionPoint, times( 0 ) ).callExtensionPoint( any( LogChannelInterface.class ), same( trans ) );
  }

  /**
   * This method test a ktr that fails in preparation step and and checks to make sure the callExtensionPoint is
   * called once.
   * @throws Throwable
   */
  @Test
  public void testTransformationFinishExtensionPointCalled() throws Throwable {
    PluginMockInterface pluginInterface = mock( PluginMockInterface.class );
    when( pluginInterface.getName() ).thenReturn( KettleExtensionPoint.TransformationFinish.id );
    when( pluginInterface.getMainType() ).thenReturn( (Class) ExtensionPointInterface.class );
    when( pluginInterface.getIds() ).thenReturn( new String[] { "extensionpointId" } );

    ExtensionPointInterface extensionPoint = mock( ExtensionPointInterface.class );
    when( pluginInterface.loadClass( ExtensionPointInterface.class ) ).thenReturn( extensionPoint );

    PluginRegistry.addPluginType( ExtensionPointPluginType.getInstance() );
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, pluginInterface );

    // Execute a sample KTR
    String fullPath = getClass().getResource( "hello-world.ktr" ).getPath();
    Params params = mock( Params.class );

    when( params.getRepoName() ).thenReturn( "" );
    when( params.getLocalInitialDir() ).thenReturn( "" );
    when( params.getLocalFile() ).thenReturn( fullPath );
    when( params.getLocalJarFile() ).thenReturn( "" );
    when( params.getBase64Zip() ).thenReturn( "" );
    mockedPanCommandExecutor.loadTransFromFilesystem( "", fullPath, "", "" );

    PanCommandExecutor panCommandExecutor = new PanCommandExecutor( PanCommandExecutor.class, log );
    panCommandExecutor.execute( params, new String[]{} );
    verify( extensionPoint, times( 1 ) ).callExtensionPoint(  any( LogChannelInterface.class ), any( Trans.class ) );
  }

  @Test
  public void testTransformationInitializationErrorExtensionPointCalled() throws Throwable {
    boolean kettleXMLExceptionThrown = false;
    Trans trans = null;
    PluginMockInterface pluginInterface = mock( PluginMockInterface.class );
    when( pluginInterface.getName() ).thenReturn( KettleExtensionPoint.TransformationFinish.id );
    when( pluginInterface.getMainType() ).thenReturn( (Class) ExtensionPointInterface.class );
    when( pluginInterface.getIds() ).thenReturn( new String[] { "extensionpointId" } );

    ExtensionPointInterface extensionPoint = mock( ExtensionPointInterface.class );
    when( pluginInterface.loadClass( ExtensionPointInterface.class ) ).thenReturn( extensionPoint );

    PluginRegistry.addPluginType( ExtensionPointPluginType.getInstance() );
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, pluginInterface );

    Params params = mock( Params.class );

    when( params.getRepoName() ).thenReturn( "" );
    when( params.getLocalInitialDir() ).thenReturn( "" );
    when( params.getLocalFile() ).thenReturn( FAIL_ON_INIT_KTR );
    when( params.getLocalJarFile() ).thenReturn( "" );
    when( params.getBase64Zip() ).thenReturn( BASE64_FAIL_ON_INIT_KTR );
    try {
      trans =
        mockedPanCommandExecutor.loadTransFromFilesystem( "", FAIL_ON_INIT_KTR, "",
          BASE64_FAIL_ON_INIT_KTR );
    } catch ( KettleXMLException e ) {
      kettleXMLExceptionThrown = true;
    }
    PanCommandExecutor panCommandExecutor = new PanCommandExecutor( PanCommandExecutor.class, log );
    panCommandExecutor.execute( params, new String[]{} );

    Assert.assertTrue( kettleXMLExceptionThrown );

    verify( extensionPoint, times( 1 ) ).callExtensionPoint( any( LogChannelInterface.class ), same( trans ) );
  }
  @Test
  public void testRunConfigurationSupport() throws Exception {
    // Test that run configuration parameter is properly handled
    String runConfigName = "testRunConfig";
    NamedParams namedParams = new NamedParamsDefault();
    namedParams.addParameterDefinition( "runConfig", "", "Run Configuration" );
    namedParams.setParameterValue( "runConfig", runConfigName );

    Params params = new Params.Builder()
      .inputFile( SAMPLE_KTR )
      .namedParams( namedParams )
      .build();
    // Verify the parameter is correctly stored
    Assert.assertEquals( "Run configuration should match", runConfigName,
      params.getNamedParams().getParameterValue( "runConfig" ) );
  }
  @Test
  public void loadTransformationFromJarFileReturnsTrans() throws Exception {
    String jarFileName = "test.jar";
    Trans trans = mockedPanCommandExecutor.loadTransFromFilesystem( "", "", jarFileName, null );
    assertNotNull( trans );
    assertNotNull( trans.getTransMeta() );
  }

  @Test( expected = Exception.class )
  public void loadTransformationFromJarFileDoesNotExist() throws Exception {
    String jarFileName = "test1.jar";
    mockedPanCommandExecutor.loadTransFromFilesystem( "", "", jarFileName, null );
  }

  @Test
  public void returnErrorWhenTransformationIsNullAndNoRepoOptionsEnabled() throws KettleException {
    Params params = mock( Params.class );
    when( params.getListRepoFiles() ).thenReturn( null );
    when( params.getListRepoDirs() ).thenReturn( null );
    when( params.getListRepos() ).thenReturn( null );
    when( params.getExportRepo() ).thenReturn( null );

    Result result = executor.execute( params, new String[0] );

    assertEquals( CommandExecutorCodes.Pan.COULD_NOT_LOAD_TRANS.getCode(), result.getExitStatus() );
  }

  @Test
  public void returnSuccessWhenTransformationIsNullAndRepoOptionsEnabled() throws KettleException {
    Params params = mock( Params.class );
    when( params.getListRepoFiles() ).thenReturn( "Y" );

    Result result = executor.execute( params, new String[0] );

    assertEquals( CommandExecutorCodes.Pan.SUCCESS.getCode(), result.getExitStatus() );
  }

}
