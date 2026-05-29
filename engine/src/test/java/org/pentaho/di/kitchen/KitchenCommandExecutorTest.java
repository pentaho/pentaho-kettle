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


package org.pentaho.di.kitchen;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.base.Params;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.plugins.ClassLoadingPluginInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.pan.CommandExecutorResult;
import org.pentaho.di.pan.CommandLineOptionProvider;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.resource.ResourceUtil;
import org.pentaho.di.resource.TopLevelResource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KitchenCommandExecutorTest {

  private static final String TEST_REPOSITORY_NAME = "TestRepo";
  private static final String KEYCLOAK_IDP = "keycloak";
  private static final String TRUST_REPOSITORY_USER_PROPERTY = "pentaho.repository.client.attemptTrust";
  private static final String IGNORED_JOB_NAME = "ignored.kjb";

  private KitchenCommandExecutor mockedKitchenCommandExecutor;
  private Result result;
  private LogChannelInterface logChannelInterface;

  interface PluginMockInterface extends ClassLoadingPluginInterface, PluginInterface {
  }

  @BeforeClass
  public static void initKettle() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws KettleException, IOException {
    KettleLogStore.init();
    mockedKitchenCommandExecutor = mock( KitchenCommandExecutor.class );
    result = mock( Result.class );
    logChannelInterface = mock( LogChannelInterface.class );
    // call real methods for loadTransFromFilesystem(), loadTransFromRepository()
    when( mockedKitchenCommandExecutor.getBowl() ).thenReturn( DefaultBowl.getInstance() );
    doCallRealMethod().when( mockedKitchenCommandExecutor ).loadJobFromFilesystem( anyString(), anyString(), any() );
    doCallRealMethod().when( mockedKitchenCommandExecutor ).loadJobFromRepository( any(), anyString(), anyString() );
    doCallRealMethod().when( mockedKitchenCommandExecutor ).decodeBase64ToZipFile( any(), anyBoolean() );
    doCallRealMethod().when( mockedKitchenCommandExecutor ).decodeBase64ToZipFile( any(), anyString() );
    doCallRealMethod().when( mockedKitchenCommandExecutor ).getReturnCode();
  }

  @After
  public void tearDown() {
    mockedKitchenCommandExecutor = null;
    result = null;
    logChannelInterface = null;
  }

  @Test
  public void testFilesystemBase64Zip() throws Exception {
    String fileName = "hello-world.kjb";
    File zipFile = new File( Objects.requireNonNull( getClass().getResource( "testKjbArchive.zip" ) ).toURI() );
    String base64Zip = Base64.getEncoder().encodeToString( FileUtils.readFileToByteArray( zipFile ) );
    Job job = mockedKitchenCommandExecutor.loadJobFromFilesystem( "", fileName, base64Zip );
    assertNotNull( job );
  }

  @Test
  public void testReturnCodeSuccess() {
    when( mockedKitchenCommandExecutor.getResult() ).thenReturn( result );
    when( result.getResult() ).thenReturn( true );
    assertEquals( mockedKitchenCommandExecutor.getReturnCode(), CommandExecutorCodes.Kitchen.SUCCESS.getCode() );
  }

  @Test
  public void testReturnCodeWithErrors() {
    try ( MockedStatic<BaseMessages> baseMessagesMockedStatic = mockStatic( BaseMessages.class ) ) {
      baseMessagesMockedStatic.when( () -> BaseMessages.getString( any(), anyString() ) ).thenReturn( "" );
      when( result.getNrErrors() ).thenReturn( 1L );
      when( mockedKitchenCommandExecutor.getResult() ).thenReturn( result );
      when( mockedKitchenCommandExecutor.getLog() ).thenReturn( logChannelInterface );
      assertEquals( mockedKitchenCommandExecutor.getReturnCode(),
        CommandExecutorCodes.Kitchen.ERRORS_DURING_PROCESSING.getCode() );
    }
  }

  @Test
  public void testReturnCodeFailWithNoErrors() {
    when( mockedKitchenCommandExecutor.getResult() ).thenReturn( result );
    assertEquals( mockedKitchenCommandExecutor.getReturnCode(),
      CommandExecutorCodes.Kitchen.ERRORS_DURING_PROCESSING.getCode() );
  }

  @Test
  public void testExecuteWithInvalidRepository() {
    // Create Mock Objects
    Params params = mock( Params.class );
    KitchenCommandExecutor kitchenCommandExecutor = new KitchenCommandExecutor( Kitchen.class );

    try ( MockedStatic<BaseMessages> baseMessagesMockedStatic = mockStatic( BaseMessages.class ) ) {
      // Mock returns
      when( params.getRepoName() ).thenReturn( "NoExistingRepository" );
      baseMessagesMockedStatic.when( () -> BaseMessages.getString( any( Class.class ), anyString(), any() ) )
        .thenReturn( "" );

      try {
        Result executionResult = kitchenCommandExecutor.execute( params, null );
        Assert.assertEquals( CommandExecutorCodes.Kitchen.COULD_NOT_LOAD_JOB.getCode(),
          executionResult.getExitStatus() );
      } catch ( KettleException e ) {
        Assert.fail();
      }
    }
  }

  @Test
  public void testJobFailOnInitializationExtensionPointCall() throws Throwable {
    boolean kettleXMLExceptionThrown = false;

    PluginMockInterface pluginInterface = mock( PluginMockInterface.class );
    when( pluginInterface.getName() ).thenReturn( KettleExtensionPoint.JobFinish.id );
    doReturn( ExtensionPointInterface.class ).when( pluginInterface ).getMainType();
    when( pluginInterface.getIds() ).thenReturn( new String[] { "extensionpointId" } );

    ExtensionPointInterface extensionPoint = mock( ExtensionPointInterface.class );
    when( pluginInterface.loadClass( ExtensionPointInterface.class ) ).thenReturn( extensionPoint );

    PluginRegistry.addPluginType( ExtensionPointPluginType.getInstance() );
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, pluginInterface );

    String fullPath = Objects.requireNonNull( getClass().getResource( "brokenjob.kjb" ) ).getPath();
    Params params = mock( Params.class );

    when( params.getRepoName() ).thenReturn( "" );
    when( params.getLocalInitialDir() ).thenReturn( "" );
    when( params.getLocalFile() ).thenReturn( fullPath );
    when( params.getLocalJarFile() ).thenReturn( "" );
    when( params.getBase64Zip() ).thenReturn( "" );
    try {
      mockedKitchenCommandExecutor.loadJobFromFilesystem( "", fullPath, "" );
    } catch ( KettleXMLException e ) {
      kettleXMLExceptionThrown = true;
    }

    KitchenCommandExecutor kitchenCommandExecutor = new KitchenCommandExecutor( KitchenCommandExecutor.class );
    Result executionResult = kitchenCommandExecutor.execute( params );

    Assert.assertTrue( kettleXMLExceptionThrown );
    Assert.assertEquals( CommandExecutorCodes.Kitchen.COULD_NOT_LOAD_JOB.getCode(), executionResult.getExitStatus() );

    verify( extensionPoint, times( 1 ) ).callExtensionPoint( any( LogChannelInterface.class ), eq( null ) );
  }

  @Test
  public void testExecuteUsesServiceAccountAsExclusiveRepositoryAuthMode() throws Exception {
    KitchenRepoAuthCapture capture = new KitchenRepoAuthCapture();
    KitchenCommandExecutor executor = createRepositoryCapturingKitchenExecutor( capture );

    Result executionResult = executor.execute( new Params.Builder()
      .repoName( TEST_REPOSITORY_NAME )
      .repoUsername( "user" )
      .repoPassword( "" )
      .listRepoDirs( "Y" )
      .browserAuth( "Y" )
      .deviceCode( "Y" )
      .serviceAccount( "Y" )
      .preferredIdp( "azure" )
      .build() );

    assertEquals( CommandExecutorCodes.Kitchen.SUCCESS.getCode(), executionResult.getExitStatus() );
    assertFalse( capture.useBrowserAuth );
    assertFalse( capture.useDeviceCode );
    assertTrue( capture.useServiceAccount );
    assertEquals( "azure", capture.preferredIdp );
  }

  @Test
  public void testExecuteDoesNotEnableDeviceCodeWithoutExplicitBrowserFlag() throws Exception {
    KitchenRepoAuthCapture capture = new KitchenRepoAuthCapture();
    KitchenCommandExecutor executor = createRepositoryCapturingKitchenExecutor( capture );

    Result executionResult = executor.execute( new Params.Builder()
      .repoName( TEST_REPOSITORY_NAME )
      .repoUsername( "user" )
      .repoPassword( "" )
      .listRepoDirs( "Y" )
      .deviceCode( "Y" )
      .preferredIdp( KEYCLOAK_IDP )
      .build() );

    assertEquals( CommandExecutorCodes.Kitchen.SUCCESS.getCode(), executionResult.getExitStatus() );
    assertTrue( capture.useBrowserAuth );
    assertFalse( capture.useDeviceCode );
    assertFalse( capture.useServiceAccount );
    assertEquals( KEYCLOAK_IDP, capture.preferredIdp );
  }

  @Test
  public void testExecuteHonorsExplicitBrowserAuthWhenPasswordExists() throws Exception {
    KitchenRepoAuthCapture capture = new KitchenRepoAuthCapture();
    KitchenCommandExecutor executor = createRepositoryCapturingKitchenExecutor( capture );

    Result executionResult = executor.execute( new Params.Builder()
      .repoName( TEST_REPOSITORY_NAME )
      .repoUsername( "user" )
      .repoPassword( "secret" )
      .listRepoDirs( "Y" )
      .browserAuth( "Y" )
      .preferredIdp( KEYCLOAK_IDP )
      .build() );

    assertEquals( CommandExecutorCodes.Kitchen.SUCCESS.getCode(), executionResult.getExitStatus() );
    assertTrue( capture.useBrowserAuth );
    assertFalse( capture.useDeviceCode );
    assertFalse( capture.useServiceAccount );
    assertEquals( KEYCLOAK_IDP, capture.preferredIdp );
  }

  @Test
  public void testExecuteClearsTrustedRepositoryUserPropertyAfterRepositoryCommand() throws Exception {
    KitchenRepoAuthCapture capture = new KitchenRepoAuthCapture();
    KitchenCommandExecutor executor = createRepositoryCapturingKitchenExecutor( capture );
    System.clearProperty( TRUST_REPOSITORY_USER_PROPERTY );

    try {
      Result executionResult = executor.execute( new Params.Builder()
        .repoName( TEST_REPOSITORY_NAME )
        .repoUsername( "user" )
        .trustRepoUser( "Y" )
        .listRepoDirs( "Y" )
        .build() );

      assertEquals( CommandExecutorCodes.Kitchen.SUCCESS.getCode(), executionResult.getExitStatus() );
      verify( capture.repository ).disconnect();
      assertNull( System.getProperty( TRUST_REPOSITORY_USER_PROPERTY ) );
    } finally {
      System.clearProperty( TRUST_REPOSITORY_USER_PROPERTY );
    }
  }

  @Test
  public void testLoadJobFromRepositoryReturnsNullWhenJobNameMissing() throws Exception {
    KitchenCommandExecutor executor = new KitchenCommandExecutor( Kitchen.class );

    assertNull( executor.loadJobFromRepository( mock( Repository.class ), "/", "" ) );
  }

  @Test
  public void testLoadJobFromRepositoryReturnsNullWhenDirectoryCannotBeResolved() throws Exception {
    KitchenCommandExecutor executor = mock( KitchenCommandExecutor.class );
    Repository repository = mock( Repository.class );

    when( executor.getLog() ).thenReturn( logChannelInterface );
    doCallRealMethod().when( executor ).loadJobFromRepository( any(), anyString(), anyString() );
    when( executor.loadRepositoryDirectory( any(), anyString(), anyString(), anyString(), anyString() ) )
      .thenReturn( null );

    assertNull( executor.loadJobFromRepository( repository, "/", "job" ) );
  }

  @Test
  public void testLoadJobFromFilesystemReturnsNullWhenFilenameMissing() throws Exception {
    KitchenCommandExecutor executor = new KitchenCommandExecutor( Kitchen.class );

    assertNull( executor.loadJobFromFilesystem( "", "", null ) );
  }

  @Test
  public void testLoadJobFromFilesystemUsesRelativeInitialDirectory() throws Exception {
    KitchenCommandExecutor executor = new KitchenCommandExecutor( Kitchen.class );
    File jobFile = new File( Objects.requireNonNull(
      getClass().getResource( "/org/pentaho/di/job/one-step-job.kjb" ) ).toURI() );

    Job job = executor.loadJobFromFilesystem(
      jobFile.getParent() + File.separator, jobFile.getName(), null );

    assertNotNull( job );
    assertNotNull( job.getJobMeta() );
  }

  @Test
  public void testLoadJobFromFilesystemThrowsKettleInitFailureDirectly() {
    KitchenCommandExecutor executor = new KitchenCommandExecutor( Kitchen.class );
    executor.setKettleInit( CompletableFuture.completedFuture( new KettleException( "boom" ) ) );

    try {
      executor.loadJobFromFilesystem( "", IGNORED_JOB_NAME, null );
      fail();
    } catch ( KettleException e ) {
      assertTrue( e.getMessage().contains( "boom" ) );
    }
  }

  @Test
  public void testLoadJobFromFilesystemWrapsUnexpectedKettleInitFailure() throws Exception {
    KitchenCommandExecutor executor = new KitchenCommandExecutor( Kitchen.class );
    @SuppressWarnings( "unchecked" )
    Future<KettleException> future = mock( Future.class );
    when( future.get() ).thenThrow( new ExecutionException( new IllegalStateException( "boom" ) ) );
    executor.setKettleInit( future );

    try {
      executor.loadJobFromFilesystem( "", IGNORED_JOB_NAME, null );
      fail();
    } catch ( KettleException e ) {
      assertTrue( e.getMessage().contains( "Kitchen initialization failed" ) );
      assertTrue( e.getCause() instanceof IllegalStateException );
    }
  }

  @Test
  public void testLoadJobFromFilesystemRestoresInterruptFlagWhenKettleInitIsInterrupted() throws Exception {
    KitchenCommandExecutor executor = new KitchenCommandExecutor( Kitchen.class );
    @SuppressWarnings( "unchecked" )
    Future<KettleException> future = mock( Future.class );
    when( future.get() ).thenThrow( new InterruptedException( "stop" ) );
    executor.setKettleInit( future );

    try {
      executor.loadJobFromFilesystem( "", IGNORED_JOB_NAME, null );
      fail();
    } catch ( KettleException e ) {
      assertTrue( e.getMessage().contains( "Kitchen initialization interrupted" ) );
      assertTrue( Thread.currentThread().isInterrupted() );
    } finally {
      assertTrue( Thread.interrupted() );
    }
  }

  @SuppressWarnings( "java:S106" )
  @Test
  public void testPrintRepositoryStoredJobsWritesEachJobNameToConsole() throws Exception {
    KitchenCommandExecutor executor = new KitchenCommandExecutor( Kitchen.class );
    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface directory = mock( RepositoryDirectoryInterface.class );
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;

    when( repository.getJobNames( null, false ) ).thenReturn( new String[] { "jobA", "jobB" } );
    when( directory.getObjectId() ).thenReturn( null );

    try ( PrintStream redirectedOut = new PrintStream( output ) ) {
      System.setOut( redirectedOut );
      executor.printRepositoryStoredJobs( repository, directory );
    } finally {
      System.setOut( originalOut );
    }

    assertTrue( output.toString().contains( "jobA" ) );
    assertTrue( output.toString().contains( "jobB" ) );
  }

  @Test
  public void testExecuteReturnsValidationExitWhenFilesystemPluginContextFails() throws Exception {
    KitchenCommandExecutor executor = new KitchenCommandExecutor( Kitchen.class );
    Params params = mock( Params.class );
    CommandLineOptionProvider provider = mock( CommandLineOptionProvider.class );
    CommandExecutorResult failure = mock( CommandExecutorResult.class );

    when( params.getRepoName() ).thenReturn( "" );
    when( params.getLocalFile() ).thenReturn( "job.kjb" );
    when( params.getPluginParams() ).thenReturn( java.util.Collections.singletonMap( "plugin", "value" ) );
    when( failure.getCode() ).thenReturn( 8 );
    when( failure.getDescription() ).thenReturn( "bad plugin" );
    when( provider.handleParameter( any( LogChannelInterface.class ), any(), eq( null ) ) ).thenReturn( failure );

    try ( MockedStatic<PluginServiceLoader> mockedLoader = mockStatic( PluginServiceLoader.class ) ) {
      mockedLoader.when( () -> PluginServiceLoader.loadServices( CommandLineOptionProvider.class ) )
        .thenReturn( List.of( provider ) );

      Result executionResult = executor.execute( params, new String[0] );

      assertEquals( 8, executionResult.getExitStatus() );
    }
  }

  @Test
  public void testExecuteReturnsLoadErrorWhenFilesystemLoadThrows() throws Exception {
    KitchenExecutionStub stub = new KitchenExecutionStub();
    KitchenCommandExecutor executor = createStubKitchenCommandExecutor( stub );
    stub.filesystemLoadException = new KettleException( "boom" );

    Result executionResult = executor.execute( new Params.Builder().localFile( "job.kjb" ).build(), new String[0] );

    assertEquals( CommandExecutorCodes.Kitchen.COULD_NOT_LOAD_JOB.getCode(), executionResult.getExitStatus() );
  }

  @Test
  public void testExecuteDisconnectsRepositoryWhenValidationFails() throws Exception {
    KitchenExecutionStub stub = new KitchenExecutionStub();
    KitchenCommandExecutor executor = createStubKitchenCommandExecutor( stub );
    CommandLineOptionProvider provider = mock( CommandLineOptionProvider.class );
    CommandExecutorResult failure = mock( CommandExecutorResult.class );
    Params params = mock( Params.class );

    when( failure.getCode() ).thenReturn( 9 );
    when( failure.getDescription() ).thenReturn( "invalid plugin arg" );
    when( provider.handleParameter( any( LogChannelInterface.class ), any(), same( stub.repository ) ) )
      .thenReturn( failure );
    when( params.getRepoName() ).thenReturn( TEST_REPOSITORY_NAME );
    when( params.getRepoUsername() ).thenReturn( "user" );
    when( params.getInputFile() ).thenReturn( "job" );
    when( params.getPluginParams() ).thenReturn( java.util.Collections.singletonMap( "plugin", "value" ) );

    try ( MockedStatic<PluginServiceLoader> mockedLoader = mockStatic( PluginServiceLoader.class ) ) {
      mockedLoader.when( () -> PluginServiceLoader.loadServices( CommandLineOptionProvider.class ) )
        .thenReturn( List.of( provider ) );

      Result executionResult = executor.execute( params, new String[0] );

      assertEquals( 9, executionResult.getExitStatus() );
      verify( stub.repository ).disconnect();
    }
  }

  @Test
  public void testExecuteReturnsLoadErrorWhenRepositoryLoadThrows() throws Exception {
    KitchenExecutionStub stub = new KitchenExecutionStub();
    KitchenCommandExecutor executor = createStubKitchenCommandExecutor( stub );
    stub.repositoryLoadException = new KettleException( "boom" );
    stub.filesystemLoadException = new KettleException( "fallback boom" );

    Result executionResult = executor.execute( new Params.Builder()
      .repoName( TEST_REPOSITORY_NAME )
      .repoUsername( "user" )
      .inputFile( "job" )
      .namedParams( new NamedParamsDefault() )
      .customNamedParams( new NamedParamsDefault() )
      .build(), new String[0] );

    assertEquals( CommandExecutorCodes.Kitchen.COULD_NOT_LOAD_JOB.getCode(), executionResult.getExitStatus() );
    verify( stub.repository ).disconnect();
  }

  @Test
  public void testExecuteExportsJobThenListsParameters() throws Exception {
    KitchenExecutionStub stub = new KitchenExecutionStub();
    KitchenCommandExecutor executor = createStubKitchenCommandExecutor( stub );
    TopLevelResource topLevelResource = mock( TopLevelResource.class );

    when( topLevelResource.getResourceName() ).thenReturn( "launch.kjb" );

    try ( MockedStatic<Props> props = mockStatic( Props.class );
          MockedStatic<ResourceUtil> resourceUtil = mockStatic( ResourceUtil.class ) ) {
      props.when( () -> Props.init( Props.TYPE_PROPERTIES_SPOON ) ).thenAnswer( invocation -> null );
      resourceUtil.when( () -> ResourceUtil.serializeResourceExportInterface(
        any(), any(), eq( "export.zip" ), any(), any(), eq( null ), any() ) ).thenReturn( topLevelResource );
      resourceUtil.when( () -> ResourceUtil.getExplanation( "export.zip", "launch.kjb", stub.jobMeta ) )
        .thenReturn( "exported" );

      Params params = new Params.Builder()
        .localFile( "job.kjb" )
        .exportRepo( "export.zip" )
        .namedParams( new NamedParamsDefault() )
        .customNamedParams( new NamedParamsDefault() )
        .build();
      Result executionResult = executor.execute( params, new String[0] );

      assertEquals( CommandExecutorCodes.Kitchen.COULD_NOT_LOAD_JOB.getCode(), executionResult.getExitStatus() );
      assertTrue( stub.printJobParametersCalled );
      assertEquals( "Y", params.getListFileParams() );
    }
  }

  @Test
  public void testExecuteReturnsUnexpectedErrorWhenExportFails() throws Exception {
    KitchenExecutionStub stub = new KitchenExecutionStub();
    KitchenCommandExecutor executor = createStubKitchenCommandExecutor( stub );

    try ( MockedStatic<Props> props = mockStatic( Props.class );
          MockedStatic<ResourceUtil> resourceUtil = mockStatic( ResourceUtil.class ) ) {
      props.when( () -> Props.init( Props.TYPE_PROPERTIES_SPOON ) ).thenAnswer( invocation -> null );
      resourceUtil.when( () -> ResourceUtil.serializeResourceExportInterface(
        any(), any(), eq( "export.zip" ), any(), any(), eq( null ), any() ) )
        .thenThrow( new RuntimeException( "boom" ) );

      Result executionResult = executor.execute( new Params.Builder()
        .localFile( "job.kjb" )
        .exportRepo( "export.zip" )
        .namedParams( new NamedParamsDefault() )
        .customNamedParams( new NamedParamsDefault() )
        .build(), new String[0] );

      assertEquals( CommandExecutorCodes.Kitchen.UNEXPECTED_ERROR.getCode(), executionResult.getExitStatus() );
    }
  }

  @Test
  public void testExecuteIgnoresUnknownNamedParameters() throws Exception {
    KitchenExecutionStub stub = new KitchenExecutionStub();
    KitchenCommandExecutor executor = createStubKitchenCommandExecutor( stub );
    NamedParams namedParams = mock( NamedParams.class );
    when( namedParams.getParameterValue( "known" ) ).thenThrow( new UnknownParamException( "bad" ) );

    Result executionResult = executor.execute( new Params.Builder()
      .localFile( "job.kjb" )
      .namedParams( namedParams )
      .customNamedParams( new NamedParamsDefault() )
      .build(), new String[0] );

    assertEquals( CommandExecutorCodes.Kitchen.SUCCESS.getCode(), executionResult.getExitStatus() );
  }

  @Test
  public void testExecuteIgnoresUnknownCustomParameters() throws Exception {
    KitchenExecutionStub stub = new KitchenExecutionStub();
    KitchenCommandExecutor executor = createStubKitchenCommandExecutor( stub );
    NamedParams customNamedParams = mock( NamedParams.class );

    when( customNamedParams.listParameters() ).thenReturn( new String[] { "custom" } );
    when( customNamedParams.getParameterValue( "custom" ) ).thenThrow( new UnknownParamException( "bad" ) );

    Result executionResult = executor.execute( new Params.Builder()
      .localFile( "job.kjb" )
      .namedParams( new NamedParamsDefault() )
      .customNamedParams( customNamedParams )
      .build(), new String[0] );

    assertEquals( CommandExecutorCodes.Kitchen.SUCCESS.getCode(), executionResult.getExitStatus() );
  }

  @Test
  public void testPrintJobParametersPrintsEveryParameter() throws Exception {
    LogChannelInterface testLog = mock( LogChannelInterface.class );
    KitchenCommandExecutor executor = spy( new KitchenCommandExecutor( Kitchen.class ) );
    Job job = mock( Job.class );

    doReturn( testLog ).when( executor ).getLog();

    when( job.listParameters() ).thenReturn( new String[] { "alpha", "beta" } );
    when( job.getParameterValue( "alpha" ) ).thenReturn( "1" );
    when( job.getParameterDefault( "alpha" ) ).thenReturn( "" );
    when( job.getParameterDescription( "alpha" ) ).thenReturn( "first" );
    when( job.getParameterValue( "beta" ) ).thenReturn( "2" );
    when( job.getParameterDefault( "beta" ) ).thenReturn( "default" );
    when( job.getParameterDescription( "beta" ) ).thenReturn( "second" );

    executor.printJobParameters( job );

    verify( testLog, times( 1 ) ).logBasic( contains( "alpha" ) );
    verify( testLog, times( 1 ) ).logBasic( contains( "beta" ) );
  }

  @Test
  public void testExitWithStatusReturnsResultWhenExtensionPointThrows() {
    KitchenCommandExecutor executor = new KitchenCommandExecutor( Kitchen.class );
    Job job = mock( Job.class );

    try ( MockedStatic<ExtensionPointHandler> extensionPointHandler = mockStatic( ExtensionPointHandler.class ) ) {
      extensionPointHandler.when( () -> ExtensionPointHandler.callExtensionPoint(
        any( LogChannelInterface.class ), eq( KettleExtensionPoint.JobFinish.id ), same( job ) ) )
        .thenThrow( new KettleException( "boom" ) );

      Result executionResult = executor.exitWithStatus( CommandExecutorCodes.Kitchen.SUCCESS.getCode(), job );

      assertEquals( CommandExecutorCodes.Kitchen.SUCCESS.getCode(), executionResult.getExitStatus() );
    }
  }

  private KitchenCommandExecutor createRepositoryCapturingKitchenExecutor( KitchenRepoAuthCapture capture )
    throws Exception {
    KitchenCommandExecutor executor = spy( new KitchenCommandExecutor( Kitchen.class ) );

    doReturn( capture.repositoryMeta ).when( executor )
      .loadRepositoryConnection( anyString(), anyString(), anyString(), anyString() );

    doAnswer( invocation -> {
      capture.useBrowserAuth = invocation.getArgument( 3 );
      capture.useDeviceCode = invocation.getArgument( 4 );
      capture.useServiceAccount = invocation.getArgument( 5 );
      capture.preferredIdp = invocation.getArgument( 6 );
      return capture.repository;
    } ).when( executor ).establishRepositoryConnectionWithBrowserAuth( any(), nullable( String.class ),
      nullable( String.class ), anyBoolean(), anyBoolean(), anyBoolean(), nullable( String.class ),
      any( RepositoryOperation[].class ) );

    doAnswer( invocation -> null ).when( executor )
      .executeRepositoryBasedCommand( any(), anyString(), anyString(), anyString() );

    return executor;
  }

  private KitchenCommandExecutor createStubKitchenCommandExecutor( KitchenExecutionStub stub ) throws Exception {
    KitchenCommandExecutor executor = spy( new KitchenCommandExecutor( Kitchen.class ) );
    Result success = new Result();
    success.setResult( true );
    success.setNrErrors( 0 );

    when( stub.job.getJobMeta() ).thenReturn( stub.jobMeta );
    when( stub.job.getResult() ).thenReturn( success );
    when( stub.job.listParameters() ).thenReturn( new String[0] );
    when( stub.job.getExtensionDataMap() ).thenReturn( new HashMap<>() );
    when( stub.jobMeta.listParameters() ).thenReturn( new String[] { "known" } );

    doReturn( stub.repositoryMeta ).when( executor )
      .loadRepositoryConnection( anyString(), anyString(), anyString(), anyString() );
    doReturn( stub.repository ).when( executor ).establishRepositoryConnectionWithBrowserAuth( any(),
      nullable( String.class ), nullable( String.class ), anyBoolean(), anyBoolean(), anyBoolean(),
      nullable( String.class ), any( RepositoryOperation[].class ) );

    doAnswer( invocation -> {
      if ( stub.filesystemLoadException != null ) {
        throw stub.filesystemLoadException;
      }
      return stub.job;
    } ).when( executor ).loadJobFromFilesystem( nullable( String.class ), nullable( String.class ), any() );

    doAnswer( invocation -> {
      if ( stub.repositoryLoadException != null ) {
        throw stub.repositoryLoadException;
      }
      return stub.job;
    } ).when( executor ).loadJobFromRepository( any(), nullable( String.class ), nullable( String.class ) );

    doAnswer( invocation -> {
      stub.printJobParametersCalled = true;
      return null;
    } ).when( executor ).printJobParameters( any( Job.class ) );

    return executor;
  }

  private static final class KitchenRepoAuthCapture {
    private final Repository repository = mock( Repository.class );
    private final RepositoryMeta repositoryMeta = mock( RepositoryMeta.class );
    private boolean useBrowserAuth;
    private boolean useDeviceCode;
    private boolean useServiceAccount;
    private String preferredIdp;
  }

  private static final class KitchenExecutionStub {
    private final Repository repository = mock( Repository.class );
    private final RepositoryMeta repositoryMeta = mock( RepositoryMeta.class );
    private final Job job = mock( Job.class );
    private final JobMeta jobMeta = mock( JobMeta.class );
    private KettleException filesystemLoadException;
    private KettleException repositoryLoadException;
    private boolean printJobParametersCalled;
  }
}
