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

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.base.Params;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.FileLoggingEventListener;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.security.ExitInterceptor;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PanTest {
  @ClassRule
  public static final RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String TEST_PARAM_NAME = "testParam";
  private static final String DEFAULT_PARAM_VALUE = "default value";
  private static final String NOT_DEFAULT_PARAM_VALUE = "not the default value";
  RepositoriesMeta mockRepositoriesMeta;
  RepositoryMeta mockRepositoryMeta;
  Repository mockRepository;
  RepositoryDirectoryInterface mockRepositoryDirectory;
  private ByteArrayOutputStream sysOutContent;
  private ByteArrayOutputStream sysErrContent;

  @BeforeClass
  public static void setUpClass() throws KettleException {
    KettleEnvironment.init();
  }

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
  public void testPanStatusCodes() {

    assertNull( CommandExecutorCodes.Pan.getByCode( 9999 ) );
    assertNotNull( CommandExecutorCodes.Pan.getByCode( 0 ) );

    assertEquals( CommandExecutorCodes.Pan.UNEXPECTED_ERROR, CommandExecutorCodes.Pan.getByCode( 2 ) );
    assertEquals( CommandExecutorCodes.Pan.CMD_LINE_PRINT, CommandExecutorCodes.Pan.getByCode( 9 ) );

    assertEquals( "The transformation ran without a problem",
      CommandExecutorCodes.Pan.getByCode( 0 ).getDescription() );
    assertEquals( "The transformation couldn't be loaded from XML or the Repository",
      CommandExecutorCodes.Pan.getByCode( 7 ).getDescription() );

    assertTrue( CommandExecutorCodes.Pan.isFailedExecution( CommandExecutorCodes.Pan.COULD_NOT_LOAD_TRANS.getCode() ) );
    assertTrue(
      CommandExecutorCodes.Pan.isFailedExecution( CommandExecutorCodes.Pan.ERROR_LOADING_STEPS_PLUGINS.getCode() ) );
    assertFalse( CommandExecutorCodes.Pan.isFailedExecution( CommandExecutorCodes.Pan.SUCCESS.getCode() ) );
    assertFalse(
      CommandExecutorCodes.Pan.isFailedExecution( CommandExecutorCodes.Pan.ERRORS_DURING_PROCESSING.getCode() ) );
  }

  @Test
  public void testConfigureParameters() throws DuplicateParamException, UnknownParamException {
    TransMeta transMeta = new TransMeta();
    transMeta.addParameterDefinition( TEST_PARAM_NAME, DEFAULT_PARAM_VALUE, "This tests a default parameter" );

    assertEquals( "Default parameter was not set correctly on TransMeta",
      DEFAULT_PARAM_VALUE, transMeta.getParameterDefault( TEST_PARAM_NAME ) );

    assertEquals( "Parameter value should be blank in TransMeta", "", transMeta.getParameterValue( TEST_PARAM_NAME ) );

    Trans trans = new Trans( transMeta );

    assertEquals( "Default parameter was not set correctly on Trans",
      DEFAULT_PARAM_VALUE, trans.getParameterDefault( TEST_PARAM_NAME ) );

    assertEquals( "Parameter value should be blank in Trans", "", trans.getParameterValue( TEST_PARAM_NAME ) );

    NamedParams params = new NamedParamsDefault();
    params.addParameterDefinition( TEST_PARAM_NAME, NOT_DEFAULT_PARAM_VALUE, "This tests a non-default parameter" );
    params.setParameterValue( TEST_PARAM_NAME, NOT_DEFAULT_PARAM_VALUE );
    Pan.configureParameters( trans, params, transMeta );
    assertEquals( "Parameter was not set correctly in Trans",
      NOT_DEFAULT_PARAM_VALUE, trans.getParameterValue( TEST_PARAM_NAME ) );
    assertEquals( "Parameter was not set correctly in TransMeta",
      NOT_DEFAULT_PARAM_VALUE, transMeta.getParameterValue( TEST_PARAM_NAME ) );
  }

  @Test
  public void testListRepos() throws KettleException {

    final String TEST_REPO_DUMMY_NAME = "dummy-repo-name";
    final String TEST_REPO_DUMMY_DESC = "dummy-repo-description";

    when( mockRepositoryMeta.getName() ).thenReturn( TEST_REPO_DUMMY_NAME );
    when( mockRepositoryMeta.getDescription() ).thenReturn( TEST_REPO_DUMMY_DESC );

    when( mockRepositoriesMeta.nrRepositories() ).thenReturn( 1 );
    when( mockRepositoriesMeta.getRepository( 0 ) ).thenReturn( mockRepositoryMeta );

    PanCommandExecutor testPanCommandExecutor =
      spy( new PanCommandExecutor( Pan.class, new LogChannel( Pan.STRING_PAN ) ) );
    doReturn( mockRepositoriesMeta ).when( testPanCommandExecutor )
      .loadRepositoryInfo( anyString(), anyString() );

    Result executionResult = runPanCommand( testPanCommandExecutor, new String[] { "/listrep" } );

    assertTrue( sysOutContent.toString().contains( TEST_REPO_DUMMY_NAME ) );
    assertTrue( sysOutContent.toString().contains( TEST_REPO_DUMMY_DESC ) );
    assertNotNull( executionResult );
    assertEquals( CommandExecutorCodes.Pan.SUCCESS.getCode(), executionResult.getExitStatus() );
  }

  @Test
  public void testListDirs() throws KettleException {

    final String DUMMY_DIR_1 = "test-dir-1";
    final String DUMMY_DIR_2 = "test-dir-2";

    when( mockRepository.getDirectoryNames( any() ) ).thenReturn( new String[] { DUMMY_DIR_1, DUMMY_DIR_2 } );
    when( mockRepository.loadRepositoryDirectoryTree() ).thenReturn( mockRepositoryDirectory );

    PanCommandExecutor testPanCommandExecutor =
      spy( new PanCommandExecutor( Pan.class, new LogChannel( Pan.STRING_PAN ) ) );
    doReturn( mockRepositoryMeta ).when( testPanCommandExecutor )
      .loadRepositoryConnection( eq( "test-repo" ), anyString(), anyString(), anyString() );
    doReturn( mockRepository ).when( testPanCommandExecutor )
      .establishRepositoryConnectionWithBrowserAuth( same( mockRepositoryMeta ), anyString(), anyString(),
        anyBoolean(), anyBoolean(), anyBoolean(), anyString(), any( RepositoryOperation[].class ) );

    Result executionResult = runPanCommand( testPanCommandExecutor,
      new String[] { "/listdir:true", "/rep:test-repo", "/preferredidp:keycloak", "/level:Basic" } );

    assertTrue( sysOutContent.toString().contains( DUMMY_DIR_1 ) );
    assertTrue( sysOutContent.toString().contains( DUMMY_DIR_2 ) );
    verify( testPanCommandExecutor ).establishRepositoryConnectionWithBrowserAuth( same( mockRepositoryMeta ),
      anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), eq( "keycloak" ),
      any( RepositoryOperation[].class ) );
    assertNotNull( executionResult );
    assertEquals( CommandExecutorCodes.Pan.SUCCESS.getCode(), executionResult.getExitStatus() );
  }

  @Test
  public void testListTrans() throws KettleException {

    final String DUMMY_TRANS_1 = "test-trans-name-1";
    final String DUMMY_TRANS_2 = "test-trans-name-2";

    when( mockRepository.getTransformationNames( any(), anyBoolean() ) ).thenReturn(
      new String[] { DUMMY_TRANS_1, DUMMY_TRANS_2 } );
    when( mockRepository.loadRepositoryDirectoryTree() ).thenReturn( mockRepositoryDirectory );

    PanCommandExecutor testPanCommandExecutor =
      spy( new PanCommandExecutor( Pan.class, new LogChannel( Pan.STRING_PAN ) ) );
    doReturn( mockRepositoryMeta ).when( testPanCommandExecutor )
      .loadRepositoryConnection( eq( "test-repo" ), anyString(), anyString(), anyString() );
    doReturn( mockRepository ).when( testPanCommandExecutor )
      .establishRepositoryConnectionWithBrowserAuth( same( mockRepositoryMeta ), anyString(), anyString(),
        anyBoolean(), anyBoolean(), anyBoolean(), anyString(), any( RepositoryOperation[].class ) );

    Result executionResult = runPanCommand( testPanCommandExecutor,
      new String[] { "/listtrans:Y", "/rep:test-repo", "/level:Basic" } );

    assertTrue( sysOutContent.toString().contains( DUMMY_TRANS_1 ) );
    assertTrue( sysOutContent.toString().contains( DUMMY_TRANS_2 ) );
    assertNotNull( executionResult );
    assertEquals( CommandExecutorCodes.Pan.SUCCESS.getCode(), executionResult.getExitStatus() );
  }

  @Test
  public void testMainHandlesConfigCommandBeforeCreatingExecutor() throws IOException {
    String originalUserHome = System.getProperty( "user.home" );
    String originalKettleHome = System.getProperty( "KETTLE_HOME" );
    Path tempHome = Files.createTempDirectory( "pan-config-test" );
    PrintStream originalSysOut = System.out;
    PrintStream originalSysErr = System.err;

    try ( PrintStream redirectedOut = new PrintStream( sysOutContent );
          PrintStream redirectedErr = new PrintStream( sysErrContent ) ) {
      System.setProperty( "user.home", tempHome.toString() );
      System.setProperty( "KETTLE_HOME", tempHome.toString() );
      System.setOut( redirectedOut );
      System.setErr( redirectedErr );

      try {
        Pan.main( new String[] { "-config:path" } );
      } catch ( SecurityException expected ) {
        // ExitInterceptor blocks the early System.exit call.
      }

      assertNull( Pan.getCommandExecutor() );
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
  public void testMainHandlesTokenCommandBeforeCreatingExecutor() {
    PrintStream originalSysOut = System.out;
    PrintStream originalSysErr = System.err;

    try ( PrintStream redirectedOut = new PrintStream( sysOutContent );
          PrintStream redirectedErr = new PrintStream( sysErrContent ) ) {
      System.setOut( redirectedOut );
      System.setErr( redirectedErr );

      try {
        Pan.main( new String[] { "-auth:clear-token" } );
      } catch ( SecurityException expected ) {
        // ExitInterceptor blocks the early System.exit call.
      }

      assertNull( Pan.getCommandExecutor() );
    } finally {
      System.setOut( originalSysOut );
      System.setErr( originalSysErr );
    }
  }

  @Test
  public void testMainClearsServiceAccountFlagBeforeExecutingCommand() {
    AtomicReference<Params> capturedParams = new AtomicReference<>();
    Result result = new Result();
    result.setExitStatus( CommandExecutorCodes.Pan.SUCCESS.getCode() );
    PanCommandExecutor commandExecutor = mock( PanCommandExecutor.class );
    when( commandExecutor.getResult() ).thenReturn( result );
    try {
      doAnswer( invocation -> {
        capturedParams.set( invocation.getArgument( 0 ) );
        return result;
      } ).when( commandExecutor ).execute( any( Params.class ), any( String[].class ) );
    } catch ( KettleException e ) {
      throw new AssertionError( e );
    }

    try {
      Pan.setCommandExecutor( commandExecutor );
      Pan.main( new String[] { "/serviceaccount:Y" } );
    } catch ( SecurityException expected ) {
      // ExitInterceptor blocks the final System.exit call.
    } finally {
      Pan.setCommandExecutor( null );
    }

    assertNotNull( capturedParams.get() );
    assertEquals( "", capturedParams.get().getServiceAccount() );
  }

  @Test
  public void testMainHandlesUnexpectedExecutorFailure() throws KettleException {
    PanCommandExecutor commandExecutor = mock( PanCommandExecutor.class );
    doThrow( new RuntimeException( "boom" ) ).when( commandExecutor )
      .execute( any( Params.class ), any( String[].class ) );

    try {
      Pan.setCommandExecutor( commandExecutor );
      Pan.main( new String[] { "/listrep:Y" } );
    } catch ( SecurityException expected ) {
      verify( commandExecutor ).execute( any( Params.class ), any( String[].class ) );
    } finally {
      Pan.setCommandExecutor( null );
    }
  }

  @Test
  public void testPrepareCommandLineOptionsPrintsUsageForHiddenArgumentPair() {
    ArrayList<String> args = new ArrayList<>( Arrays.asList( "-help", "value" ) );
    CommandLineOption[] commandLineOptions = { new CommandLineOption( "file", "desc", new StringBuilder() ) };
    NamedParamsDefault pluginNamedParams = new NamedParamsDefault();
    LogChannel logChannel = new LogChannel( Pan.STRING_PAN );
    try {
      Pan.prepareCommandLineOptions(
        args,
        commandLineOptions,
        pluginNamedParams,
        logChannel );
      fail();
    } catch ( SecurityException e ) {
      assertNotNull( e );
    }
  }

  @Test
  public void testPrepareCommandLineOptionsExitsWhenParsingFails() {
    ArrayList<String> args = new ArrayList<>( List.of( "/file" ) );
    CommandLineOption[] commandLineOptions = { new CommandLineOption( "file", "desc", new StringBuilder() ) };
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    NamedParamsDefault pluginNamedParams = new NamedParamsDefault();
    try {
      Pan.prepareCommandLineOptions(
        args,
        commandLineOptions,
        pluginNamedParams,
        logChannelInterface );
      fail();
    } catch ( SecurityException e ) {
      assertNotNull( e );
    }
  }

  @Test
  public void testConfigureFileAppenderReturnsNullForEmptyLogfile() throws KettleException {
    assertNull( Pan.configureFileAppender( new StringBuilder() ) );
  }

  @Test
  public void testConfigureAndCloseFileAppenderHandlesHappyPath() throws KettleException, IOException {
    Path tempLog = Files.createTempFile( "pan-log", ".log" );
    FileLoggingEventListener appender = Pan.configureFileAppender( new StringBuilder( tempLog.toString() ) );

    assertNotNull( appender );
    Pan.closeFileAppender( appender, new LogChannel( Pan.STRING_PAN ) );
  }

  @Test
  public void testCloseFileAppenderSwallowsCloseFailure() throws KettleException {
    FileLoggingEventListener appender = mock( FileLoggingEventListener.class );
    LogChannelInterface log = mock( LogChannelInterface.class );
    doThrow( new RuntimeException( "boom" ) ).when( appender ).close();

    Pan.closeFileAppender( appender, log );

    verify( log ).logError( any(), any( RuntimeException.class ) );
  }

  @Test
  public void testFormatOptionValueRedactsSensitiveOptions() {
    assertEquals( "<redacted>", Pan.formatOptionValue(
      new CommandLineOption( "pass", "desc", new StringBuilder( "secret" ) ) ) );
    assertEquals( "value", Pan.formatOptionValue(
      new CommandLineOption( "file", "desc", new StringBuilder( "value" ) ) ) );
  }

  @Test
  public void testIsSensitiveOptionRecognizesSensitiveNames() {
    assertTrue( Pan.isSensitiveOption( "accessToken" ) );
    assertTrue( Pan.isSensitiveOption( "param" ) );
    assertFalse( Pan.isSensitiveOption( "file" ) );
  }

  @Test
  public void testLogDebugArgumentsUsesFormattedValues() {
    LogChannelInterface log = mock( LogChannelInterface.class );
    when( log.isDebug() ).thenReturn( true );

    Pan.logDebugArguments(
      log,
      new CommandLineOption[] {
        new CommandLineOption( "file", "desc", new StringBuilder( "sample.ktr" ) ),
        new CommandLineOption( "pass", "desc", new StringBuilder( "secret" ) )
      } );

    verify( log ).logDebug( "Arguments:" );
    verify( log ).logDebug( org.mockito.ArgumentMatchers.contains( "<redacted>" ) );
  }

  @Test
  public void testGetPluginNamedParamsReturnsProviderParameters() throws DuplicateParamException {
    LogChannelInterface log = mock( LogChannelInterface.class );
    CommandLineOptionProvider provider = mock( CommandLineOptionProvider.class );
    NamedParamsDefault pluginParams = new NamedParamsDefault();
    pluginParams.addParameterDefinition( "pluginOption", "", "plugin option" );

    try ( MockedStatic<PluginServiceLoader> mockedLoader = mockStatic( PluginServiceLoader.class ) ) {
      mockedLoader.when( () -> PluginServiceLoader.loadServices( CommandLineOptionProvider.class ) )
        .thenReturn( List.of( provider ) );
      when( provider.getAdditionalCommandlineOptions( log ) ).thenReturn( pluginParams );

      NamedParams result = Pan.getPluginNamedParams( log );

      assertArrayEquals( new String[] { "pluginOption" }, result.listParameters() );
    }
  }

  @Test
  public void testGetPluginNamedParamsLogsPluginExceptions() {
    LogChannelInterface log = mock( LogChannelInterface.class );

    try ( MockedStatic<PluginServiceLoader> mockedLoader = mockStatic( PluginServiceLoader.class ) ) {
      mockedLoader.when( () -> PluginServiceLoader.loadServices( CommandLineOptionProvider.class ) )
        .thenThrow( new KettlePluginException( "boom" ) );

      NamedParams result = Pan.getPluginNamedParams( log );

      assertNotNull( result );
      assertEquals( 0, result.listParameters().length );
      verify( log ).logError( any(), any( KettlePluginException.class ) );
    }
  }

  @Test
  public void testUpdatedPluginParamValueCopiesMatchingOptions() throws UnknownParamException, DuplicateParamException {
    NamedParamsDefault namedParams = new NamedParamsDefault();
    namedParams.addParameterDefinition( "pluginOption", "", "plugin option" );
    LogChannelInterface log = mock( LogChannelInterface.class );

    Pan.updatedPluginParamValue(
      namedParams,
      new CommandLineOption[] { new CommandLineOption( "pluginOption", "desc", new StringBuilder( "value" ) ) },
      log );

    assertEquals( "value", namedParams.getParameterValue( "pluginOption" ) );
  }

  @Test
  public void testUpdatedPluginParamValueLogsUnknownParameterFailures() throws UnknownParamException {
    NamedParams namedParams = mock( NamedParams.class );
    LogChannelInterface log = mock( LogChannelInterface.class );
    when( namedParams.listParameters() ).thenReturn( new String[] { "pluginOption" } );
    doThrow( new UnknownParamException( "bad" ) ).when( namedParams ).setParameterValue( "pluginOption", "value" );

    Pan.updatedPluginParamValue(
      namedParams,
      new CommandLineOption[] { new CommandLineOption( "pluginOption", "desc", new StringBuilder( "value" ) ) },
      log );

    verify( log ).logError( any(), any( UnknownParamException.class ) );
  }

  @Test
  public void testPanOptionStateAppliesRepositoryEnvironmentOverrides() {
    Pan.PanOptionState optionState = new Pan.PanOptionState();
    optionState.getOptionLogfileOld().append( "legacy.log" );

    try ( MockedStatic<org.pentaho.di.core.Const> mockedConst = mockStatic( org.pentaho.di.core.Const.class ) ) {
      mockedConst.when( () -> org.pentaho.di.core.Const.getEnvironmentVariable( "KETTLE_REPOSITORY", null ) )
        .thenReturn( "repo-env" );
      mockedConst.when( () -> org.pentaho.di.core.Const.getEnvironmentVariable( "KETTLE_USER", null ) )
        .thenReturn( "user-env" );
      mockedConst.when( () -> org.pentaho.di.core.Const.getEnvironmentVariable( "KETTLE_PASSWORD", null ) )
        .thenReturn( "pass-env" );

      optionState.applyRepositoryEnvironmentOverrides();
    }

    assertEquals( "repo-env", optionState.getOptionRepname().toString() );
    assertEquals( "user-env", optionState.getOptionUsername().toString() );
    assertEquals( "pass-env", optionState.getOptionPassword().toString() );
    assertEquals( "legacy.log", optionState.getOptionLogfile().toString() );
  }

  @Test
  public void testPanOptionStateApplyEnvOverrideSkipsBlankValues() {
    Pan.PanOptionState optionState = new Pan.PanOptionState();
    StringBuilder target = new StringBuilder( "keep-me" );

    optionState.applyEnvOverride( target, "" );

    assertEquals( "keep-me", target.toString() );
  }

  private Result runPanCommand( PanCommandExecutor commandExecutor, String[] arguments ) {
    PrintStream originalSysOut = System.out;
    PrintStream originalSysErr = System.err;

    try ( PrintStream redirectedOut = new PrintStream( sysOutContent );
          PrintStream redirectedErr = new PrintStream( sysErrContent ) ) {
      System.setOut( redirectedOut );
      System.setErr( redirectedErr );

      Pan.setCommandExecutor( commandExecutor );

      try {
        Pan.main( arguments );
      } catch ( SecurityException e ) {
        // Expected when ExitInterceptor blocks System.exit().
      }

      return Pan.getCommandExecutor().getResult();
    } finally {
      Pan.setCommandExecutor( null );
      System.setOut( originalSysOut );
      System.setErr( originalSysErr );
    }
  }

}
