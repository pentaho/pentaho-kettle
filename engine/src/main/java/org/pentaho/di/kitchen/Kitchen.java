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

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.base.Params;
import org.pentaho.di.cli.auth.TokenCommandHandler;
import org.pentaho.di.cli.config.CliConfigCommandHandler;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.FileLoggingEventListener;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.ExecutorUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.pan.CommandLineOption;
import org.pentaho.di.pan.CommandLineOptionProvider;
import org.pentaho.di.security.ExitInterceptor;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Kitchen {
  private static final Class<?> pkgClass = Kitchen.class; // for i18n purposes, needed by Translator2!!

  public static final String STRING_KITCHEN = "Kitchen";

  private static KitchenCommandExecutor commandExecutor;

  public static void main( String[] a ) throws Exception {
    if ( handleEarlyCommand( a ) ) {
      return;
    }

    Locale.setDefault( LanguageChoice.getInstance().getDefaultLocale() );
    executeMain( a );
  }

  private static boolean handleEarlyCommand( String[] args ) {
    // Handle config commands early, before full environment initialization
    // This allows 'Kitchen.bat -config:set key value' to work standalone
    if ( CliConfigCommandHandler.hasConfigCommand( args ) ) {
      KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.KITCHEN );
      CliConfigCommandHandler configHandler = new CliConfigCommandHandler();
      int exitCode = configHandler.execute( args );
      exitJVM( exitCode );
      return true;
    }

    if ( TokenCommandHandler.hasTokenCommand( args ) ) {
      KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.KITCHEN );
      int exitCode = new TokenCommandHandler().execute( args );
      exitJVM( exitCode );
      return true;
    }

    return false;
  }

  private static void executeMain( String[] rawArgs ) throws KettleException, ExecutionException, InterruptedException {
    final ExecutorService executor = ExecutorUtil.getExecutor();
    final Future<Map.Entry<KettlePluginException, Future<KettleException>>> repositoryRegisterFuture =
      submitRepositoryRegistration( executor );

    List<String> args = collectArguments( rawArgs );
    KitchenOptionState optionState = new KitchenOptionState();
    NamedParams optionParams = new NamedParamsDefault();
    NamedParams customOptions = new NamedParamsDefault();

    CommandLineOption maxLogLinesOption = new CommandLineOption(
      "maxloglines", BaseMessages.getString( pkgClass, "Kitchen.CmdLine.MaxLogLines" ), new StringBuilder() );
    CommandLineOption maxLogTimeoutOption = new CommandLineOption(
      "maxlogtimeout", BaseMessages.getString( pkgClass, "Kitchen.CmdLine.MaxLogTimeout" ), new StringBuilder() );

    CommandLineOption[] options = optionState.createOptions( optionParams, customOptions,
      maxLogLinesOption, maxLogTimeoutOption );

    configureLogging( maxLogLinesOption, maxLogTimeoutOption );
    optionState.applyRepositoryEnvironmentOverrides();

    Map.Entry<KettlePluginException, Future<KettleException>> repositoryRegisterResults = repositoryRegisterFuture
      .get();
    // It's a singleton map with one key-value pair (a Pair of collection)
    KettlePluginException repositoryRegisterException = repositoryRegisterResults.getKey();
    if ( repositoryRegisterException != null ) {
      throw repositoryRegisterException;
    }
    Future<KettleException> kettleInitFuture = repositoryRegisterResults.getValue();

    LogChannelInterface log = new LogChannel( STRING_KITCHEN );

    // Get command line Parameters added by plugins
    NamedParams pluginNamedParams = getPluginNamedParam( log );

    options = prepareCommandLineOptions( args, options, pluginNamedParams, log, kettleInitFuture );

    FileLoggingEventListener fileAppender = configureFileAppender( optionState.optionLogfile );
    if ( !Utils.isEmpty( optionState.optionLoglevel ) ) {
      log.setLogLevel( LogLevel.getLogLevelForCode( optionState.optionLoglevel.toString() ) );
      log.logMinimal( BaseMessages.getString( pkgClass, "Kitchen.Log.LogLevel", log.getLogLevel().getDescription() ) );
    }

    if ( !Utils.isEmpty( optionState.optionServiceAccount ) ) {
      log.logMinimal( BaseMessages.getString( pkgClass, "Kitchen.Warn.ServiceAccountIgnored" ) );
      optionState.optionServiceAccount.setLength( 0 );
    }

    try {
      KitchenCommandData commandData = new KitchenCommandData( args, pluginNamedParams, options, log,
        kettleInitFuture );
      KitchenExecutionContext executionContext = new KitchenExecutionContext( rawArgs, optionState,
        optionParams, customOptions, commandData );
      Result result = runKitchenCommand( executionContext );
      exitJVM( result.getExitStatus() );
    } catch ( Exception t ) {
      log.logError( "Unexpected error while executing Kitchen", t );
      exitJVM( CommandExecutorCodes.Pan.UNEXPECTED_ERROR.getCode() );
    } finally {
      closeFileAppender( fileAppender );
    }
  }

  private static Future<Map.Entry<KettlePluginException, Future<KettleException>>> submitRepositoryRegistration(
    ExecutorService executor ) {
    final RepositoryPluginType repositoryPluginType = RepositoryPluginType.getInstance();
    return executor.submit( () -> {
      PluginRegistry.addPluginType( repositoryPluginType );
      try {
        KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.KITCHEN );
        KettleClientEnvironment.init();
      } catch ( KettlePluginException e ) {
        return new AbstractMap.SimpleImmutableEntry<>( e, null );
      }

      Future<KettleException> kettleEnvironmentInitFuture = executor.submit( () -> {
        try {
          KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.KITCHEN );
          KettleEnvironment.init();
        } catch ( KettleException e ) {
          return e;
        }
        return null;
      } );
      return new AbstractMap.SimpleImmutableEntry<>( null, kettleEnvironmentInitFuture );
    } );
  }

  private static List<String> collectArguments( String[] rawArgs ) {
    List<String> args = new ArrayList<>();
    for ( String arg : rawArgs ) {
      if ( !arg.isEmpty() ) {
        args.add( arg );
      }
    }
    return args;
  }

  @VisibleForTesting
  static CommandLineOption[] prepareCommandLineOptions( List<String> args,
                                                        CommandLineOption[] options,
                                                        NamedParams pluginNamedParams,
                                                        LogChannelInterface log,
                                                        Future<KettleException> kettleInitFuture )
    throws KettleException {
    List<CommandLineOption> updatedOptionList = updateCommandlineOptions( options, pluginNamedParams, log );
    CommandLineOption[] updatedOptions = updatedOptionList.toArray( new CommandLineOption[ 0 ] );
    if ( args.size() == 2 ) {
      CommandLineOption.printUsage( updatedOptions );
      blockAndThrow( kettleInitFuture );
      exitJVM( 9 );
    }
    if ( !CommandLineOption.parseArguments( args, updatedOptions, log ) ) {
      log.logError( BaseMessages.getString( pkgClass, "Pan.Error.CommandLineError" ) );
      blockAndThrow( kettleInitFuture );
      exitJVM( CommandExecutorCodes.Pan.ERROR_LOADING_STEPS_PLUGINS.getCode() );
    }
    return updatedOptions;
  }

  @VisibleForTesting
  static FileLoggingEventListener configureFileAppender( StringBuilder optionLogfile ) throws KettleException {
    if ( Utils.isEmpty( optionLogfile ) ) {
      return null;
    }
    FileLoggingEventListener fileAppender = new FileLoggingEventListener( optionLogfile.toString(), true );
    KettleLogStore.getAppender().addLoggingEventListener( fileAppender );
    return fileAppender;
  }

  @VisibleForTesting
  static void closeFileAppender( FileLoggingEventListener fileAppender ) {
    if ( fileAppender == null ) {
      return;
    }
    try {
      fileAppender.close();
    } catch ( Exception e ) {
      new LogChannel( STRING_KITCHEN ).logError( "Unable to close Kitchen file appender", e );
    }
    KettleLogStore.getAppender().removeLoggingEventListener( fileAppender );
  }

  private static Result runKitchenCommand( KitchenExecutionContext context ) throws KettleException {

    if ( getCommandExecutor() == null ) {
      setCommandExecutor( new KitchenCommandExecutor( pkgClass, context.log, context.kettleInitFuture ) );
    }

    if ( !Utils.isEmpty( context.optionState.optionVersion ) ) {
      getCommandExecutor().printVersion();
      if ( context.rawArgs.length == 1 ) {
        exitJVM( CommandExecutorCodes.Pan.KETTLE_VERSION_PRINT.getCode() );
      }
    }

    // Set the value of commandline param into the PluginNamedParams after parsing
    updatedPluginParamValue( context.pluginNamedParams, context.options, context.log );
    Params jobParams = context.optionState.buildParams( context.optionParams, context.customOptions,
      context.pluginNamedParams );
    return getCommandExecutor().execute( jobParams, context.args.toArray( new String[ 0 ] ) );
  }

  private static void blockAndThrow( Future<KettleException> future ) throws KettleException {
    try {
      KettleException e = future.get();
      if ( e != null ) {
        throw e;
      }
    } catch ( InterruptedException e ) {
      Thread.currentThread().interrupt();
      throw new KitchenInitializationException( "Kitchen initialization interrupted", e );
    } catch ( ExecutionException e ) {
      throw new KitchenInitializationException( "Kitchen initialization failed", e.getCause() );
    }
  }

  /**
   * Configure the central log store from the provided command line options
   *
   * @param maxLogLinesOption   Option for maximum log lines
   * @param maxLogTimeoutOption Option for log timeout
   * @throws KettleException Error parsing command line arguments
   */
  public static void configureLogging( final CommandLineOption maxLogLinesOption,
                                       final CommandLineOption maxLogTimeoutOption ) throws KettleException {
    int maxLogLines = parseIntArgument( maxLogLinesOption, 0 );
    if ( Utils.isEmpty( maxLogLinesOption.getArgument() ) ) {
      maxLogLines = Const.toInt( EnvUtil.getSystemProperty( Const.KETTLE_MAX_LOG_SIZE_IN_LINES ), 5000 );
    }
    int maxLogTimeout = parseIntArgument( maxLogTimeoutOption, 0 );
    if ( Utils.isEmpty( maxLogTimeoutOption.getArgument() ) ) {
      maxLogTimeout = Const.toInt( EnvUtil.getSystemProperty( Const.KETTLE_MAX_LOG_TIMEOUT_IN_MINUTES ), 1440 );
    }
    KettleLogStore.init( maxLogLines, maxLogTimeout );
  }

  /**
   * Parse an argument as an integer.
   *
   * @param option Command Line Option to parse argument of
   * @param def    Default if the argument is not set
   * @return The parsed argument or the default if the argument was not specified
   * @throws KettleException Error parsing provided argument as an integer
   */
  protected static int parseIntArgument( final CommandLineOption option, final int def ) throws KettleException {
    if ( !Utils.isEmpty( option.getArgument() ) ) {
      try {
        return Integer.parseInt( option.getArgument().toString() );
      } catch ( NumberFormatException ex ) {
        throw new KettleException( BaseMessages.getString( pkgClass, "Kitchen.Error.InvalidNumberArgument", option
          .getOption(), option.getArgument() ) );
      }
    }
    return def;
  }

  @VisibleForTesting
  static NamedParams getPluginNamedParam( LogChannelInterface log ) {
    NamedParams newNamedParams = new NamedParamsDefault();
    try {
      for ( CommandLineOptionProvider provider : PluginServiceLoader
        .loadServices( CommandLineOptionProvider.class ) ) {
        newNamedParams = provider.getAdditionalCommandlineOptions( log );
      }
    } catch ( KettlePluginException e ) {
      log.logError( "Exception getting the named parameters", e );
    }
    return newNamedParams;
  }

  @VisibleForTesting
  static List<CommandLineOption> updateCommandlineOptions( CommandLineOption[] options,
                                                           NamedParams namedParams,
                                                           LogChannelInterface log ) {
    List<CommandLineOption> modifiableList = new ArrayList<>();
    Collections.addAll( modifiableList, options );
    try {
      if ( namedParams != null ) {
        // For each additional commandLine parameter, create new CommandLineOption
        for ( String key : namedParams.listParameters() ) {
          // Prepare the new commandline option
          CommandLineOption option = new CommandLineOption( key, namedParams.getParameterDescription( key ),
            new StringBuilder(), false, false );
          modifiableList.add( option );
        }
      }

    } catch ( UnknownParamException e ) {
      log.logError( "Exception getting the additional parameters", e );
    }
    return modifiableList;
  }

  @VisibleForTesting
  static void updatedPluginParamValue( NamedParams namedParams,
                                       CommandLineOption[] options,
                                       LogChannelInterface log ) {
    try {
      if ( namedParams != null ) {
        for ( String key : namedParams.listParameters() ) {
          for ( CommandLineOption option : options ) {
            if ( key.equals( option.getOption() ) ) {
              namedParams.setParameterValue( key, option.getArgument().toString() );
              break;
            }
          }
        }
      }
    } catch ( UnknownParamException e ) {
      log.logError( "Unknown parameter", e );
    }
  }

  private static final class KitchenInitializationException extends KettleException {
    private KitchenInitializationException( String message, Throwable cause ) {
      super( message, cause );
    }
  }

  private static final class KitchenExecutionContext {
    private final String[] rawArgs;
    private final KitchenOptionState optionState;
    private final NamedParams optionParams;
    private final NamedParams customOptions;
    private final List<String> args;
    private final NamedParams pluginNamedParams;
    private final CommandLineOption[] options;
    private final LogChannelInterface log;
    private final Future<KettleException> kettleInitFuture;

    private KitchenExecutionContext( String[] rawArgs,
                                     KitchenOptionState optionState,
                                     NamedParams optionParams,
                                     NamedParams customOptions,
                                     KitchenCommandData commandData ) {
      this.rawArgs = rawArgs;
      this.optionState = optionState;
      this.optionParams = optionParams;
      this.customOptions = customOptions;
      this.args = commandData.args;
      this.pluginNamedParams = commandData.pluginNamedParams;
      this.options = commandData.options;
      this.log = commandData.log;
      this.kettleInitFuture = commandData.kettleInitFuture;
    }
  }

  private static final class KitchenCommandData {
    private final List<String> args;
    private final NamedParams pluginNamedParams;
    private final CommandLineOption[] options;
    private final LogChannelInterface log;
    private final Future<KettleException> kettleInitFuture;

    private KitchenCommandData( List<String> args, NamedParams pluginNamedParams, CommandLineOption[] options,
                                LogChannelInterface log, Future<KettleException> kettleInitFuture ) {
      this.args = args;
      this.pluginNamedParams = pluginNamedParams;
      this.options = options;
      this.log = log;
      this.kettleInitFuture = kettleInitFuture;
    }
  }

  @VisibleForTesting
  static final class KitchenOptionState {
    private final StringBuilder optionRepname = new StringBuilder();
    private final StringBuilder optionUsername = new StringBuilder();
    private final StringBuilder optionTrustUser = new StringBuilder();
    private final StringBuilder optionPassword = new StringBuilder();
    private final StringBuilder optionJobname = new StringBuilder();
    private final StringBuilder optionDirname = new StringBuilder();
    private final StringBuilder initialDir = new StringBuilder();
    private final StringBuilder optionFilename = new StringBuilder();
    private final StringBuilder optionLoglevel = new StringBuilder();
    private final StringBuilder optionLogfile = new StringBuilder();
    private final StringBuilder optionLogfileOld = new StringBuilder();
    private final StringBuilder optionListdir = new StringBuilder();
    private final StringBuilder optionListjobs = new StringBuilder();
    private final StringBuilder optionListrep = new StringBuilder();
    private final StringBuilder optionNorep = new StringBuilder();
    private final StringBuilder optionVersion = new StringBuilder();
    private final StringBuilder optionListParam = new StringBuilder();
    private final StringBuilder optionExport = new StringBuilder();
    private final StringBuilder optionBase64Zip = new StringBuilder();
    private final StringBuilder optionUuid = new StringBuilder();
    private final StringBuilder optionBrowserAuth = new StringBuilder();
    private final StringBuilder optionDeviceCode = new StringBuilder();
    private final StringBuilder optionPreferredIdp = new StringBuilder();
    private final StringBuilder optionServiceAccount = new StringBuilder();

    private CommandLineOption[] createOptions( NamedParams optionParams,
                                               NamedParams customOptions,
                                               CommandLineOption maxLogLinesOption,
                                               CommandLineOption maxLogTimeoutOption ) {
      return new CommandLineOption[] {
        new CommandLineOption( "rep", BaseMessages.getString( pkgClass, "Kitchen.CmdLine.RepName" ), optionRepname ),
        new CommandLineOption( "user", BaseMessages.getString( pkgClass, "Kitchen.CmdLine.RepUsername" ),
          optionUsername ),
        new CommandLineOption( "trustuser", BaseMessages.getString( pkgClass, "Kitchen.ComdLine.RepUsername" ),
          optionTrustUser ),
        new CommandLineOption( "pass", BaseMessages.getString( pkgClass, "Kitchen.CmdLine.RepPassword" ),
          optionPassword ),
        new CommandLineOption( "job", BaseMessages.getString( pkgClass, "Kitchen.CmdLine.RepJobName" ), optionJobname ),
        new CommandLineOption( "dir", BaseMessages.getString( pkgClass, "Kitchen.CmdLine.RepDir" ), optionDirname ),
        new CommandLineOption( "file", BaseMessages.getString( pkgClass, "Kitchen.CmdLine.XMLJob" ), optionFilename ),
        new CommandLineOption( "level", BaseMessages.getString( pkgClass, "Kitchen.CmdLine.LogLevel" ),
          optionLoglevel ),
        new CommandLineOption( "logfile", BaseMessages.getString( pkgClass, "Kitchen.CmdLine.LogFile" ),
          optionLogfile ),
        new CommandLineOption( "log", BaseMessages.getString( pkgClass, "Kitchen.CmdLine.LogFileOld" ),
          optionLogfileOld, false, true ),
        new CommandLineOption( "listdir", BaseMessages.getString( pkgClass, "Kitchen.CmdLine.ListDir" ), optionListdir,
          true, false ),
        new CommandLineOption( "listjobs", BaseMessages.getString( pkgClass, "Kitchen.CmdLine.ListJobsDir" ),
          optionListjobs, true, false ),
        new CommandLineOption( "listrep", BaseMessages.getString( pkgClass, "Kitchen.CmdLine.ListAvailableReps" ),
          optionListrep, true, false ),
        new CommandLineOption( "norep", BaseMessages.getString( pkgClass, "Kitchen.CmdLine.NoRep" ), optionNorep, true,
          false ),
        new CommandLineOption( "version", BaseMessages.getString( pkgClass, "Kitchen.CmdLine.Version" ), optionVersion,
          true, false ),
        new CommandLineOption( "param", BaseMessages.getString( pkgClass, "Kitchen.ComdLine.Param" ), optionParams,
          false ),
        new CommandLineOption( "listparam", BaseMessages.getString( pkgClass, "Kitchen.ComdLine.ListParam" ),
          optionListParam, true, false ),
        new CommandLineOption( "export", BaseMessages.getString( pkgClass, "Kitchen.ComdLine.Export" ), optionExport,
          true, false ),
        new CommandLineOption( "initialDir", null, initialDir, false, true ),
        new CommandLineOption( "zip", "Base64Zip", optionBase64Zip, false, true ),
        new CommandLineOption( "uuid", "UUID", optionUuid, false, true ),
        new CommandLineOption( "custom", BaseMessages.getString( pkgClass, "Kitchen.ComdLine.Custom" ), customOptions,
          false ),
        maxLogLinesOption,
        maxLogTimeoutOption,
        new CommandLineOption( "browserauth", BaseMessages.getString( pkgClass, "Kitchen.ComdLine.BrowserAuth" ),
          optionBrowserAuth, true, false ),
        new CommandLineOption( "devicecode", BaseMessages.getString( pkgClass, "Kitchen.ComdLine.DeviceCode" ),
          optionDeviceCode, true, false ),
        new CommandLineOption( "preferredidp", BaseMessages.getString( pkgClass, "Kitchen.ComdLine.PreferredIdp" ),
          optionPreferredIdp, false, true ),
        new CommandLineOption( "serviceaccount", BaseMessages.getString( pkgClass, "Kitchen.ComdLine.ServiceAccount" ),
          optionServiceAccount, true, false ) };
    }

    @VisibleForTesting
    void applyRepositoryEnvironmentOverrides() {
      applyEnvOverride( optionRepname, Const.getEnvironmentVariable( "KETTLE_REPOSITORY", null ) );
      applyEnvOverride( optionUsername, Const.getEnvironmentVariable( "KETTLE_USER", null ) );
      applyEnvOverride( optionPassword, Const.getEnvironmentVariable( "KETTLE_PASSWORD", null ) );
      if ( Utils.isEmpty( optionLogfile ) && !Utils.isEmpty( optionLogfileOld ) ) {
        // if the old style of logging name is filled in, and the new one is not
        // overwrite the new by the old
        optionLogfile.append( optionLogfileOld );
      }
    }

    @VisibleForTesting
    void applyEnvOverride( StringBuilder target, String envValue ) {
      if ( Utils.isEmpty( envValue ) ) {
        return;
      }
      target.setLength( 0 );
      target.append( envValue );
    }

    private Params buildParams( NamedParams optionParams,
                                NamedParams customOptions,
                                NamedParams pluginNamedParams ) {
      Params.Builder builder = !optionUuid.isEmpty() ? new Params.Builder( optionUuid.toString() )
        : new Params.Builder();
      return builder
        .blockRepoConns( optionNorep.toString() )
        .repoName( optionRepname.toString() )
        .repoUsername( optionUsername.toString() )
        .trustRepoUser( optionTrustUser.toString() )
        .repoPassword( optionPassword.toString() )
        .inputDir( optionDirname.toString() )
        .inputFile( optionJobname.toString() )
        .listRepoFiles( optionListjobs.toString() )
        .listRepoDirs( optionListdir.toString() )
        .exportRepo( optionExport.toString() )
        .localFile( optionFilename.toString() )
        .localJarFile( "" )
        .localInitialDir( initialDir.toString() )
        .listRepos( optionListrep.toString() )
        .listFileParams( optionListParam.toString() )
        .logLevel( "" )
        .maxLogLines( "" )
        .maxLogTimeout( "" )
        .logFile( "" )
        .oldLogFile( "" )
        .version( "" )
        .resultSetStepName( "" )
        .resultSetCopyNumber( "" )
        .base64Zip( optionBase64Zip.toString() )
        .namedParams( optionParams )
        .customNamedParams( customOptions )
        .pluginNamedParams( pluginNamedParams )
        .browserAuth( optionBrowserAuth.toString() )
        .deviceCode( optionDeviceCode.toString() )
        .preferredIdp( optionPreferredIdp.toString() )
        .serviceAccount( optionServiceAccount.toString() )
        .build();
    }

    @VisibleForTesting
    StringBuilder getOptionRepname() {
      return optionRepname;
    }

    @VisibleForTesting
    StringBuilder getOptionUsername() {
      return optionUsername;
    }

    @VisibleForTesting
    StringBuilder getOptionPassword() {
      return optionPassword;
    }

    @VisibleForTesting
    StringBuilder getOptionLogfile() {
      return optionLogfile;
    }

    @VisibleForTesting
    StringBuilder getOptionLogfileOld() {
      return optionLogfileOld;
    }
  }

  @VisibleForTesting
  static void exitJVM( int status ) {

    ExitInterceptor.exit( status );
  }

  public static KitchenCommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public static void setCommandExecutor( KitchenCommandExecutor commandExecutor ) {
    Kitchen.commandExecutor = commandExecutor;
  }

}
