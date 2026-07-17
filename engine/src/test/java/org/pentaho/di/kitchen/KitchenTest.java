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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.base.Params;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.FileLoggingEventListener;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.pan.CommandLineOption;
import org.pentaho.di.pan.CommandLineOptionProvider;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.security.ExitInterceptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KitchenTest {

  private static final String TEST_PARAM_NAME = "testParam";
  private static final String DEFAULT_PARAM_VALUE = "default value";
  RepositoriesMeta mockRepositoriesMeta;
  RepositoryMeta mockRepositoryMeta;
  Repository mockRepository;
  RepositoryDirectoryInterface mockRepositoryDirectory;
  private ByteArrayOutputStream sysOutContent;
  private ByteArrayOutputStream sysErrContent;

  @Before
  public void setUp() {
    ExitInterceptor.enableIntercept();
    KettleLogStore.init();
    sysOutContent = new ByteArrayOutputStream();
    sysErrContent = new ByteArrayOutputStream();
    mockRepositoriesMeta = mock( RepositoriesMeta.class );
    mockRepositoryMeta = mock( RepositoryMeta.class );
    mockRepository = mock( Repository.class );
    mockRepositoryDirectory = mock( RepositoryDirectoryInterface.class );
  }

  @After
  public void tearDown() {
    ExitInterceptor.disableIntercept();
    sysOutContent = null;
    sysErrContent = null;
    mockRepositoriesMeta = null;
    mockRepositoryMeta = null;
    mockRepository = null;
    mockRepositoryDirectory = null;
  }

  @Test
  public void testKitchenStatusCodes() {

    assertNull( CommandExecutorCodes.Kitchen.getByCode( 9999 ) );
    assertNotNull( CommandExecutorCodes.Kitchen.getByCode( 0 ) );

    assertEquals( CommandExecutorCodes.Kitchen.UNEXPECTED_ERROR, CommandExecutorCodes.Kitchen.getByCode( 2 ) );
    assertEquals( CommandExecutorCodes.Kitchen.CMD_LINE_PRINT, CommandExecutorCodes.Kitchen.getByCode( 9 ) );

    assertEquals( "The job ran without a problem", CommandExecutorCodes.Kitchen.getByCode( 0 ).getDescription() );
    assertEquals( "The job couldn't be loaded from XML or the Repository",
      CommandExecutorCodes.Kitchen.getByCode( 7 ).getDescription() );

    assertTrue(
      CommandExecutorCodes.Kitchen.isFailedExecution( CommandExecutorCodes.Kitchen.COULD_NOT_LOAD_JOB.getCode() ) );
    assertTrue( CommandExecutorCodes.Kitchen.isFailedExecution(
      CommandExecutorCodes.Kitchen.ERROR_LOADING_STEPS_PLUGINS.getCode() ) );
    assertFalse( CommandExecutorCodes.Kitchen.isFailedExecution( CommandExecutorCodes.Kitchen.SUCCESS.getCode() ) );
    assertFalse( CommandExecutorCodes.Kitchen.isFailedExecution(
      CommandExecutorCodes.Kitchen.ERRORS_DURING_PROCESSING.getCode() ) );
  }

  @Test
  public void testConfigureParameters() throws DuplicateParamException, UnknownParamException {
    JobMeta jobMeta = new JobMeta();
    jobMeta.addParameterDefinition( TEST_PARAM_NAME, DEFAULT_PARAM_VALUE, "This tests a default parameter" );

    assertEquals( "Default parameter was not set correctly on JobMeta", DEFAULT_PARAM_VALUE,
      jobMeta.getParameterDefault( TEST_PARAM_NAME ) );

    assertEquals( "Parameter value should be blank in JobMeta", "", jobMeta.getParameterValue( TEST_PARAM_NAME ) );

    Job job = new Job( null, jobMeta );
    job.copyParametersFrom( jobMeta );

    assertEquals( "Default parameter was not set correctly on Job", DEFAULT_PARAM_VALUE,
      job.getParameterDefault( TEST_PARAM_NAME ) );

    assertEquals( "Parameter value should be blank in Job", "", job.getParameterValue( TEST_PARAM_NAME ) );
  }

  @Test
  public void testListRepos() {
    AtomicReference<Params> capturedParams = new AtomicReference<>();
    KitchenCommandExecutor commandExecutor = createSuccessfulCommandExecutor( capturedParams );

    Result executionResult = runKitchenCommand( commandExecutor, new String[] { "/listrep" } );

    assertNotNull( capturedParams.get() );
    assertFalse( capturedParams.get().getListRepos().isEmpty() );
    assertNotNull( executionResult );
    assertEquals( CommandExecutorCodes.Kitchen.SUCCESS.getCode(), executionResult.getExitStatus() );
  }

  @Test
  public void testListDirs() {
    AtomicReference<Params> capturedParams = new AtomicReference<>();
    KitchenCommandExecutor commandExecutor = createSuccessfulCommandExecutor( capturedParams );

    Result executionResult = runKitchenCommand( commandExecutor,
      new String[] { "/listdir:true", "/rep:test-repo", "/preferredidp:keycloak", "/level:Basic" } );

    assertNotNull( capturedParams.get() );
    assertEquals( "test-repo", capturedParams.get().getRepoName() );
    assertEquals( "keycloak", capturedParams.get().getPreferredIdp() );
    assertFalse( capturedParams.get().getListRepoDirs().isEmpty() );
    assertNotNull( executionResult );
    assertEquals( CommandExecutorCodes.Kitchen.SUCCESS.getCode(), executionResult.getExitStatus() );
  }

  @Test
  public void testListJobs() {
    AtomicReference<Params> capturedParams = new AtomicReference<>();
    KitchenCommandExecutor commandExecutor = createSuccessfulCommandExecutor( capturedParams );

    Result executionResult = runKitchenCommand( commandExecutor,
      new String[] { "/listjobs:Y", "/rep:test-repo", "/level:Basic" } );

    assertNotNull( capturedParams.get() );
    assertEquals( "test-repo", capturedParams.get().getRepoName() );
    assertFalse( capturedParams.get().getListRepoFiles().isEmpty() );
    assertNotNull( executionResult );
    assertEquals( CommandExecutorCodes.Kitchen.SUCCESS.getCode(), executionResult.getExitStatus() );
  }

  private Result runKitchenCommand( KitchenCommandExecutor commandExecutor, String[] arguments ) {
    PrintStream originalSysOut = System.out;
    PrintStream originalSysErr = System.err;

    try ( PrintStream redirectedOut = new PrintStream( sysOutContent );
          PrintStream redirectedErr = new PrintStream( sysErrContent ) ) {
      System.setOut( redirectedOut );
      System.setErr( redirectedErr );

      Kitchen.setCommandExecutor( commandExecutor );

      try {
        Kitchen.main( arguments );
      } catch ( SecurityException e ) {
        // Expected when ExitInterceptor blocks System.exit().
      } catch ( Exception e ) {
        throw new AssertionError( e );
      }

      return Kitchen.getCommandExecutor().getResult();
    } finally {
      Kitchen.setCommandExecutor( null );
      System.setOut( originalSysOut );
      System.setErr( originalSysErr );
    }
  }

  @Test
  public void testMainHandlesConfigCommandBeforeCreatingExecutor() throws Exception {
    String originalUserHome = System.getProperty( "user.home" );
    String originalKettleHome = System.getProperty( "KETTLE_HOME" );
    Path tempHome = Files.createTempDirectory( "kitchen-config-test" );
    PrintStream originalSysOut = System.out;
    PrintStream originalSysErr = System.err;

    try ( PrintStream redirectedOut = new PrintStream( sysOutContent );
          PrintStream redirectedErr = new PrintStream( sysErrContent ) ) {
      System.setProperty( "user.home", tempHome.toString() );
      System.setProperty( "KETTLE_HOME", tempHome.toString() );
      System.setOut( redirectedOut );
      System.setErr( redirectedErr );

      try {
        Kitchen.main( new String[] { "-config:path" } );
      } catch ( SecurityException expected ) {
        // ExitInterceptor blocks the early System.exit call.
      }

      assertNull( Kitchen.getCommandExecutor() );
      assertTrue( sysOutContent.toString().contains( "cli-config.properties" ) );
    } finally {
      System.setOut( originalSysOut );
      System.setErr( originalSysErr );
      if ( originalUserHome != null ) {
        System.setProperty( "user.home", originalUserHome );
      } else {
        System.clearProperty( "user.home" );
      }
      if ( originalKettleHome != null ) {
        System.setProperty( "KETTLE_HOME", originalKettleHome );
      } else {
        System.clearProperty( "KETTLE_HOME" );
      }
      Files.walk( tempHome )
        .sorted( Comparator.reverseOrder() )
        .forEach( path -> {
          try {
            Files.deleteIfExists( path );
          } catch ( IOException ignored ) {
            // best-effort temp cleanup
          }
        } );
    }
  }

  @Test
  public void testMainHandlesTokenCommandBeforeCreatingExecutor() throws Exception {
    PrintStream originalSysOut = System.out;
    PrintStream originalSysErr = System.err;

    try ( PrintStream redirectedOut = new PrintStream( sysOutContent );
          PrintStream redirectedErr = new PrintStream( sysErrContent ) ) {
      System.setOut( redirectedOut );
      System.setErr( redirectedErr );

      try {
        Kitchen.main( new String[] { "-auth:clear-token" } );
      } catch ( SecurityException expected ) {
        // ExitInterceptor blocks the early System.exit call.
      }

      assertNull( Kitchen.getCommandExecutor() );
    } finally {
      System.setOut( originalSysOut );
      System.setErr( originalSysErr );
    }
  }

  @Test
  public void testMainClearsServiceAccountFlagBeforeExecutingCommand() throws Exception {
    AtomicReference<Params> capturedParams = new AtomicReference<>();
    KitchenCommandExecutor commandExecutor = createSuccessfulCommandExecutor( capturedParams );

    try {
      Kitchen.setCommandExecutor( commandExecutor );
      Kitchen.main( new String[] { "/serviceaccount:Y" } );
    } catch ( SecurityException expected ) {
      // ExitInterceptor blocks the final System.exit call.
    } finally {
      Kitchen.setCommandExecutor( null );
    }

    assertNotNull( capturedParams.get() );
    assertEquals( "", capturedParams.get().getServiceAccount() );
  }

  @Test
  public void testMainHandlesUnexpectedExecutorFailure() throws Exception {
    KitchenCommandExecutor commandExecutor = mock( KitchenCommandExecutor.class );
    doThrow( new RuntimeException( "boom" ) ).when( commandExecutor )
      .execute( any( Params.class ), any( String[].class ) );

    try {
      Kitchen.setCommandExecutor( commandExecutor );
      Kitchen.main( new String[] { "/listrep:Y" } );
    } catch ( SecurityException expected ) {
      verify( commandExecutor ).execute( any( Params.class ), any( String[].class ) );
    } finally {
      Kitchen.setCommandExecutor( null );
    }
  }

  @Test
  public void testPrepareCommandLineOptionsPrintsUsageForHiddenArgumentPair() throws Exception {
    ArrayList<String> args = new ArrayList<>( Arrays.asList( "-help", "value" ) );
    CommandLineOption[] commandLineOptions = { new CommandLineOption( "file", "desc", new StringBuilder() ) };
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    CompletableFuture<KettleException> completedFuture = CompletableFuture.completedFuture( null );
    NamedParamsDefault pluginNamedParams = new NamedParamsDefault();

    try {
      Kitchen.prepareCommandLineOptions(
        args,
        commandLineOptions,
        pluginNamedParams,
        logChannelInterface,
        completedFuture );
      fail();
    } catch ( SecurityException e ) {
      assertNotNull( e );
    }
  }

  @Test
  public void testPrepareCommandLineOptionsExitsWhenParsingFails() throws Exception {
    ArrayList<String> args = new ArrayList<>( List.of( "/file" ) );
    CommandLineOption[] commandLineOptions = { new CommandLineOption( "file", "desc", new StringBuilder() ) };
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    CompletableFuture<KettleException> completedFuture = CompletableFuture.completedFuture( null );
    NamedParamsDefault pluginNamedParams = new NamedParamsDefault();

    try {
      Kitchen.prepareCommandLineOptions(
        args,
        commandLineOptions,
        pluginNamedParams,
        logChannelInterface,
        completedFuture );
      fail();
    } catch ( SecurityException e ) {
      assertNotNull( e );
    }
  }

  @Test
  public void testConfigureFileAppenderReturnsNullForEmptyLogfile() throws Exception {
    assertNull( Kitchen.configureFileAppender( new StringBuilder() ) );
  }

  @Test
  public void testConfigureAndCloseFileAppenderHandlesHappyPath() throws Exception {
    Path tempLog = Files.createTempFile( "kitchen-log", ".log" );
    FileLoggingEventListener appender = Kitchen.configureFileAppender( new StringBuilder( tempLog.toString() ) );

    assertNotNull( appender );
    Kitchen.closeFileAppender( appender );
  }

  @Test
  public void testCloseFileAppenderSwallowsCloseFailure() throws Exception {
    FileLoggingEventListener appender = mock( FileLoggingEventListener.class );
    doThrow( new RuntimeException( "boom" ) ).when( appender ).close();

    Kitchen.closeFileAppender( appender );

    verify( appender ).close();
  }

  @Test
  public void testParseIntArgumentReturnsDefaultWhenUnset() throws Exception {
    assertEquals( 9, Kitchen.parseIntArgument( new CommandLineOption( "max", "desc", new StringBuilder() ), 9 ) );
  }

  @Test
  public void testParseIntArgumentThrowsForInvalidNumber() {
    try {
      Kitchen.parseIntArgument( new CommandLineOption( "max", "desc", new StringBuilder( "not-a-number" ) ), 9 );
    } catch ( KettleException e ) {
      assertTrue( e.getMessage().contains( "max" ) );
    }
  }

  @Test
  public void testGetPluginNamedParamReturnsProviderParameters() throws Exception {
    LogChannelInterface log = mock( LogChannelInterface.class );
    CommandLineOptionProvider provider = mock( CommandLineOptionProvider.class );
    NamedParamsDefault pluginParams = new NamedParamsDefault();
    pluginParams.addParameterDefinition( "pluginOption", "", "plugin option" );

    try ( MockedStatic<PluginServiceLoader> mockedLoader = mockStatic( PluginServiceLoader.class ) ) {
      mockedLoader.when( () -> PluginServiceLoader.loadServices( CommandLineOptionProvider.class ) )
        .thenReturn( List.of( provider ) );
      when( provider.getAdditionalCommandlineOptions( log ) ).thenReturn( pluginParams );

      NamedParams result = Kitchen.getPluginNamedParam( log );

      assertEquals( 1, result.listParameters().length );
      assertEquals( "pluginOption", result.listParameters()[ 0 ] );
    }
  }

  @Test
  public void testGetPluginNamedParamLogsPluginExceptions() {
    LogChannelInterface log = mock( LogChannelInterface.class );

    try ( MockedStatic<PluginServiceLoader> mockedLoader = mockStatic( PluginServiceLoader.class ) ) {
      mockedLoader.when( () -> PluginServiceLoader.loadServices( CommandLineOptionProvider.class ) )
        .thenThrow( new KettlePluginException( "boom" ) );

      NamedParams result = Kitchen.getPluginNamedParam( log );

      assertNotNull( result );
      assertEquals( 0, result.listParameters().length );
      verify( log ).logError( any(), any( KettlePluginException.class ) );
    }
  }

  @Test
  public void testUpdateCommandlineOptionsAddsPluginOptions() throws Exception {
    NamedParamsDefault namedParams = new NamedParamsDefault();
    namedParams.addParameterDefinition( "pluginOption", "", "plugin option" );

    List<CommandLineOption> options = Kitchen.updateCommandlineOptions(
      new CommandLineOption[] { new CommandLineOption( "file", "desc", new StringBuilder() ) },
      namedParams,
      mock( LogChannelInterface.class ) );

    assertEquals( 2, options.size() );
    assertEquals( "pluginOption", options.get( 1 ).getOption() );
  }

  @Test
  public void testUpdatedPluginParamValueCopiesMatchingOptions() throws Exception {
    NamedParamsDefault namedParams = new NamedParamsDefault();
    namedParams.addParameterDefinition( "pluginOption", "", "plugin option" );

    Kitchen.updatedPluginParamValue(
      namedParams,
      new CommandLineOption[] { new CommandLineOption( "pluginOption", "desc", new StringBuilder( "value" ) ) },
      mock( LogChannelInterface.class ) );

    assertEquals( "value", namedParams.getParameterValue( "pluginOption" ) );
  }

  @Test
  public void testUpdatedPluginParamValueLogsUnknownParameterFailures() throws Exception {
    NamedParams namedParams = mock( NamedParams.class );
    LogChannelInterface log = mock( LogChannelInterface.class );
    when( namedParams.listParameters() ).thenReturn( new String[] { "pluginOption" } );
    doThrow( new UnknownParamException( "bad" ) ).when( namedParams ).setParameterValue( "pluginOption", "value" );

    Kitchen.updatedPluginParamValue(
      namedParams,
      new CommandLineOption[] { new CommandLineOption( "pluginOption", "desc", new StringBuilder( "value" ) ) },
      log );

    verify( log ).logError( any(), any( UnknownParamException.class ) );
  }

  @Test
  public void testKitchenOptionStateAppliesRepositoryEnvironmentOverrides() {
    Kitchen.KitchenOptionState optionState = new Kitchen.KitchenOptionState();
    optionState.getOptionLogfileOld().append( "legacy.log" );

    try ( MockedStatic<Const> mockedConst = mockStatic( Const.class ) ) {
      mockedConst.when( () -> Const.getEnvironmentVariable( "KETTLE_REPOSITORY", null ) )
        .thenReturn( "repo-env" );
      mockedConst.when( () -> Const.getEnvironmentVariable( "KETTLE_USER", null ) )
        .thenReturn( "user-env" );
      mockedConst.when( () -> Const.getEnvironmentVariable( "KETTLE_PASSWORD", null ) )
        .thenReturn( "pass-env" );

      optionState.applyRepositoryEnvironmentOverrides();
    }

    assertEquals( "repo-env", optionState.getOptionRepname().toString() );
    assertEquals( "user-env", optionState.getOptionUsername().toString() );
    assertEquals( "pass-env", optionState.getOptionPassword().toString() );
    assertEquals( "legacy.log", optionState.getOptionLogfile().toString() );
  }

  @Test
  public void testKitchenOptionStateApplyEnvOverrideSkipsBlankValues() {
    Kitchen.KitchenOptionState optionState = new Kitchen.KitchenOptionState();
    StringBuilder target = new StringBuilder( "keep-me" );

    optionState.applyEnvOverride( target, "" );

    assertEquals( "keep-me", target.toString() );
  }

  @Test
  public void testExitJvmUsesInterceptor() {
    try {
      Kitchen.exitJVM( 9 );
      fail();
    } catch ( SecurityException e ) {
      assertNotNull( e );
    }
  }

  private KitchenCommandExecutor createSuccessfulCommandExecutor( AtomicReference<Params> capturedParams ) {
    KitchenCommandExecutor commandExecutor = mock( KitchenCommandExecutor.class );
    Result result = new Result();
    result.setExitStatus( CommandExecutorCodes.Kitchen.SUCCESS.getCode() );
    when( commandExecutor.getResult() ).thenReturn( result );
    try {
      doAnswer( invocation -> {
        capturedParams.set( invocation.getArgument( 0 ) );
        return result;
      } ).when( commandExecutor ).execute( any( Params.class ), any( String[].class ) );
    } catch ( KettleException e ) {
      throw new AssertionError( e );
    }
    return commandExecutor;
  }
}
