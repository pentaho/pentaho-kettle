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

import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.base.Params;
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
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.cli.config.CliConfigCommandHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.kitchen.Kitchen;
import org.pentaho.di.cli.auth.TokenCommandHandler;
import org.pentaho.di.security.ExitInterceptor;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Pan {
  private static final Class<?> pkgClass = Pan.class; // for i18n purposes, needed by Translator2!!

  public static final String STRING_PAN = "Pan";

  private static PanCommandExecutor commandExecutor;

  public static void main( String[] a ) {
    try {
      if ( handleEarlyCommand( a ) ) {
        return;
      }
      Locale.setDefault( LanguageChoice.getInstance().getDefaultLocale() );
      executeMain( a );
    } catch ( Exception e ) {
      LogChannelInterface log = new LogChannel( STRING_PAN );
      log.logError( BaseMessages.getString( pkgClass, "Pan.Log.UnexpectedErrorOccurred", e.getMessage() ), e );
      exitJVM( CommandExecutorCodes.Pan.UNEXPECTED_ERROR.getCode() );
    }
  }

  private static boolean handleEarlyCommand( String[] args ) {
    // Handle config commands early, before full environment initialization
    // This allows 'Pan.bat -config:set key value' to work standalone
    if ( CliConfigCommandHandler.hasConfigCommand( args ) ) {
      // Minimal initialization for config commands
      KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
      CliConfigCommandHandler configHandler = new CliConfigCommandHandler();
      exitJVM( configHandler.execute( args ) );
      return true;
    }

    if ( TokenCommandHandler.hasTokenCommand( args ) ) {
      KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
      exitJVM( new TokenCommandHandler().execute( args ) );
      return true;
    }

    return false;
  }

  private static void executeMain( String[] rawArgs ) throws KettleException {
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
    KettleEnvironment.init();

    List<String> args = collectArguments( rawArgs );
    LogChannelInterface log = new LogChannel( STRING_PAN );
    PanOptionState optionState = new PanOptionState();
    NamedParams optionParams = new NamedParamsDefault();
    CommandLineOption maxLogLinesOption = new CommandLineOption(
      "maxloglines", BaseMessages.getString( pkgClass, "Pan.CmdLine.MaxLogLines" ), new StringBuilder() );
    CommandLineOption maxLogTimeoutOption = new CommandLineOption(
      "maxlogtimeout", BaseMessages.getString( pkgClass, "Pan.CmdLine.MaxLogTimeout" ), new StringBuilder() );

    CommandLineOption[] options = optionState.createOptions( optionParams, maxLogLinesOption, maxLogTimeoutOption );

    // Get command line Parameters added by plugins
    NamedParams pluginNamedParams = getPluginNamedParams( log );
    options = prepareCommandLineOptions( args, options, pluginNamedParams, log );

    Kitchen.configureLogging( maxLogLinesOption, maxLogTimeoutOption );
    optionState.applyRepositoryEnvironmentOverrides();

    FileLoggingEventListener fileAppender = configureFileAppender( optionState.optionLogfile );
    try {
      if ( !Utils.isEmpty( optionState.optionLoglevel ) ) {
        log.setLogLevel( LogLevel.getLogLevelForCode( optionState.optionLoglevel.toString() ) );
        log.logMinimal( BaseMessages.getString( pkgClass, "Pan.Log.Loglevel", log.getLogLevel().getDescription() ) );
      }

      if ( !Utils.isEmpty( optionState.optionServiceAccount ) ) {
        log.logMinimal( BaseMessages.getString( pkgClass, "Pan.Warn.ServiceAccountIgnored" ) );
        optionState.optionServiceAccount.setLength( 0 );
      }

      logDebugArguments( log, options );

      Result result = runPanCommand( rawArgs, args, optionState, optionParams, pluginNamedParams, options, log );
      exitJVM( result.getExitStatus() );
    } finally {
      closeFileAppender( fileAppender, log );
    }
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

  private static CommandLineOption[] prepareCommandLineOptions( List<String> args,
                                                                CommandLineOption[] options,
                                                                NamedParams pluginNamedParams,
                                                                LogChannelInterface log ) {
    List<CommandLineOption> updatedOptionList = updateCommandlineOptions( options, pluginNamedParams, log );
    CommandLineOption[] updatedOptions = updatedOptionList.toArray( new CommandLineOption[ 0 ] );
    if ( args.size() == 2 ) { // 2 internal hidden argument (flag and value)
      CommandLineOption.printUsage( updatedOptions );
      exitJVM( CommandExecutorCodes.Pan.CMD_LINE_PRINT.getCode() );
      return updatedOptions;
    }

    // Parse the options...
    if ( !CommandLineOption.parseArguments( args, updatedOptions, log ) ) {
      log.logError( BaseMessages.getString( pkgClass, "Pan.Error.CommandLineError" ) );
      exitJVM( CommandExecutorCodes.Pan.ERROR_LOADING_STEPS_PLUGINS.getCode() );
    }
    return updatedOptions;
  }

  private static FileLoggingEventListener configureFileAppender( StringBuilder optionLogfile ) throws KettleException {
    if ( Utils.isEmpty( optionLogfile ) ) {
      return null;
    }
    // PDI-18724: this throws an exception if the given log file is not accessible
    FileLoggingEventListener fileAppender = new FileLoggingEventListener( optionLogfile.toString(), true );
    KettleLogStore.getAppender().addLoggingEventListener( fileAppender );
    return fileAppender;
  }

  private static void closeFileAppender( FileLoggingEventListener fileAppender, LogChannelInterface log ) {
    if ( fileAppender == null ) {
      return;
    }

    try {
      fileAppender.close();
    } catch ( Exception e ) {
      log.logError( "Unable to close Pan log file appender", e );
    }
    KettleLogStore.getAppender().removeLoggingEventListener( fileAppender );
  }

  private static void logDebugArguments( LogChannelInterface log, CommandLineOption[] options ) {
    if ( !log.isDebug() ) {
      return;
    }

    log.logDebug( "Arguments:" );
    for ( CommandLineOption option : options ) {
      log.logDebug( Const.rightPad( option.getOption(), 12 ) + " : " + formatOptionValue( option ) );
    }
    log.logDebug( "" );
  }

  private static String formatOptionValue( CommandLineOption option ) {
    if ( isSensitiveOption( option.getOption() ) ) {
      return "<redacted>";
    }
    return String.valueOf( option.getArgument() );
  }

  private static boolean isSensitiveOption( String optionName ) {
    String normalizedOption = optionName == null ? "" : optionName.toLowerCase( Locale.ROOT );
    return normalizedOption.contains( "pass" )
      || normalizedOption.contains( "secret" )
      || normalizedOption.contains( "token" )
      || "param".equals( normalizedOption );
  }

  private static Result runPanCommand( String[] rawArgs,
                                       List<String> args,
                                       PanOptionState optionState,
                                       NamedParams optionParams,
                                       NamedParams pluginNamedParams,
                                       CommandLineOption[] options,
                                       LogChannelInterface log ) throws KettleException {
    if ( getCommandExecutor() == null ) {
      setCommandExecutor( new PanCommandExecutor( pkgClass, log ) );
    }

    if ( !Utils.isEmpty( optionState.optionVersion ) ) {
      getCommandExecutor().printVersion();
      if ( rawArgs.length == 1 ) {
        exitJVM( CommandExecutorCodes.Pan.KETTLE_VERSION_PRINT.getCode() );
      }
    }
    // Set the value of commandline param into the PluginNamedParams after parsing
    updatedPluginParamValue( pluginNamedParams, options, log );
    Params transParams = optionState.buildParams( optionParams, pluginNamedParams );
    return getCommandExecutor().execute( transParams, args.toArray( new String[ 0 ] ) );
  }

  private static NamedParams getPluginNamedParams( LogChannelInterface log ) {
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

  private static void updatedPluginParamValue( NamedParams namedParams,
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

  private static List<CommandLineOption> updateCommandlineOptions( CommandLineOption[] options,
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

  private static final class PanOptionState {
    private final StringBuilder optionRepname = new StringBuilder();
    private final StringBuilder optionUsername = new StringBuilder();
    private final StringBuilder optionTrustUser = new StringBuilder();
    private final StringBuilder optionPassword = new StringBuilder();
    private final StringBuilder optionTransname = new StringBuilder();
    private final StringBuilder optionDirname = new StringBuilder();
    private final StringBuilder optionFilename = new StringBuilder();
    private final StringBuilder optionLoglevel = new StringBuilder();
    private final StringBuilder optionLogfile = new StringBuilder();
    private final StringBuilder optionLogfileOld = new StringBuilder();
    private final StringBuilder optionListdir = new StringBuilder();
    private final StringBuilder optionListtrans = new StringBuilder();
    private final StringBuilder optionListrep = new StringBuilder();
    private final StringBuilder optionExprep = new StringBuilder();
    private final StringBuilder optionNorep = new StringBuilder();
    private final StringBuilder optionSafemode = new StringBuilder();
    private final StringBuilder optionVersion = new StringBuilder();
    private final StringBuilder optionJarFilename = new StringBuilder();
    private final StringBuilder optionListParam = new StringBuilder();
    private final StringBuilder optionMetrics = new StringBuilder();
    private final StringBuilder initialDir = new StringBuilder();
    private final StringBuilder optionResultSetStepName = new StringBuilder();
    private final StringBuilder optionResultSetCopyNumber = new StringBuilder();
    private final StringBuilder optionBase64Zip = new StringBuilder();
    private final StringBuilder optionUuid = new StringBuilder();
    private final StringBuilder optionRunConfiguration = new StringBuilder();
    private final StringBuilder optionBrowserAuth = new StringBuilder();
    private final StringBuilder optionDeviceCode = new StringBuilder();
    private final StringBuilder optionPreferredIdp = new StringBuilder();
    private final StringBuilder optionServiceAccount = new StringBuilder();

    private CommandLineOption[] createOptions( NamedParams optionParams,
                                               CommandLineOption maxLogLinesOption,
                                               CommandLineOption maxLogTimeoutOption ) {
      return new CommandLineOption[] {
        new CommandLineOption( "rep", BaseMessages.getString( pkgClass, "Pan.ComdLine.RepName" ), optionRepname ),
        new CommandLineOption( "user", BaseMessages.getString( pkgClass, "Pan.ComdLine.RepUsername" ), optionUsername ),
        new CommandLineOption( "trustuser", BaseMessages.getString( pkgClass, "Pan.ComdLine.RepUsername" ),
          optionTrustUser ),
        new CommandLineOption( "pass", BaseMessages.getString( pkgClass, "Pan.ComdLine.RepPassword" ), optionPassword ),
        new CommandLineOption( "trans", BaseMessages.getString( pkgClass, "Pan.ComdLine.TransName" ), optionTransname ),
        new CommandLineOption( "dir", BaseMessages.getString( pkgClass, "Pan.ComdLine.RepDir" ), optionDirname ),
        new CommandLineOption( "file", BaseMessages.getString( pkgClass, "Pan.ComdLine.XMLTransFile" ),
          optionFilename ),
        new CommandLineOption( "level", BaseMessages.getString( pkgClass, "Pan.ComdLine.LogLevel" ), optionLoglevel ),
        new CommandLineOption( "logfile", BaseMessages.getString( pkgClass, "Pan.ComdLine.LogFile" ), optionLogfile ),
        new CommandLineOption( "log", BaseMessages.getString( pkgClass, "Pan.ComdLine.LogOldFile" ), optionLogfileOld,
          false, true ),
        new CommandLineOption( "listdir", BaseMessages.getString( pkgClass, "Pan.ComdLine.ListDirRep" ), optionListdir,
          true, false ),
        new CommandLineOption( "listtrans", BaseMessages.getString( pkgClass, "Pan.ComdLine.ListTransDir" ),
          optionListtrans, true, false ),
        new CommandLineOption( "listrep", BaseMessages.getString( pkgClass, "Pan.ComdLine.ListReps" ), optionListrep,
          true, false ),
        new CommandLineOption( "exprep", BaseMessages.getString( pkgClass, "Pan.ComdLine.ExpObjectsXML" ), optionExprep,
          true, false ),
        new CommandLineOption( "norep", BaseMessages.getString( pkgClass, "Pan.ComdLine.NoRep" ), optionNorep, true,
          false ),
        new CommandLineOption( "safemode", BaseMessages.getString( pkgClass, "Pan.ComdLine.SafeMode" ), optionSafemode,
          true, false ),
        new CommandLineOption( "version", BaseMessages.getString( pkgClass, "Pan.ComdLine.Version" ), optionVersion,
          true, false ),
        new CommandLineOption( "jarfile", BaseMessages.getString( pkgClass, "Pan.ComdLine.JarFile" ), optionJarFilename,
          false, true ),
        new CommandLineOption( "param", BaseMessages.getString( pkgClass, "Pan.ComdLine.Param" ), optionParams, false ),
        new CommandLineOption( "listparam", BaseMessages.getString( pkgClass, "Pan.ComdLine.ListParam" ),
          optionListParam, true, false ),
        new CommandLineOption( "initialDir", null, initialDir, false, true ),
        new CommandLineOption( "stepname", "ResultSetStepName", optionResultSetStepName, false, true ),
        new CommandLineOption( "copynum", "ResultSetCopyNumber", optionResultSetCopyNumber, false, true ),
        new CommandLineOption( "zip", "Base64Zip", optionBase64Zip, false, true ),
        new CommandLineOption( "uuid", "UUID", optionUuid, false, true ),
        new CommandLineOption( "metrics", BaseMessages.getString( pkgClass, "Pan.ComdLine.Metrics" ), optionMetrics,
          true, false ),
        maxLogLinesOption,
        maxLogTimeoutOption,
        new CommandLineOption( "runConfig", BaseMessages.getString( pkgClass, "Pan.ComdLine.RunConfiguration" ),
          optionRunConfiguration ),
        new CommandLineOption( "browserauth", BaseMessages.getString( pkgClass, "Pan.ComdLine.BrowserAuth" ),
          optionBrowserAuth, true, false ),
        new CommandLineOption( "devicecode", BaseMessages.getString( pkgClass, "Pan.ComdLine.DeviceCode" ),
          optionDeviceCode, true, false ),
        new CommandLineOption( "preferredidp", BaseMessages.getString( pkgClass, "Pan.ComdLine.PreferredIdp" ),
          optionPreferredIdp, false, true ),
        new CommandLineOption( "serviceaccount", BaseMessages.getString( pkgClass, "Pan.ComdLine.ServiceAccount" ),
          optionServiceAccount, true, false ) };
    }

    private void applyRepositoryEnvironmentOverrides() {
      applyEnvOverride( optionRepname, Const.getEnvironmentVariable( Const.KETTLE_REPOSITORY, null ) );
      applyEnvOverride( optionUsername, Const.getEnvironmentVariable( Const.KETTLE_USER, null ) );
      applyEnvOverride( optionPassword, Const.getEnvironmentVariable( Const.KETTLE_PASSWORD, null ) );
      if ( Utils.isEmpty( optionLogfile ) && !Utils.isEmpty( optionLogfileOld ) ) {
        // if the old style of logging name is filled in, and the new one is not
        // overwrite the new by the old
        optionLogfile.append( optionLogfileOld );
      }
    }

    private void applyEnvOverride( StringBuilder target, String envValue ) {
      if ( Utils.isEmpty( envValue ) ) {
        return;
      }

      target.setLength( 0 );
      target.append( envValue );
    }

    private Params buildParams( NamedParams optionParams, NamedParams pluginNamedParams ) {
      Params.Builder builder = !optionUuid.isEmpty() ? new Params.Builder( optionUuid.toString() )
        : new Params.Builder();
      return builder
        .blockRepoConns( optionNorep.toString() )
        .repoName( optionRepname.toString() )
        .repoUsername( optionUsername.toString() )
        .trustRepoUser( optionTrustUser.toString() )
        .repoPassword( optionPassword.toString() )
        .inputDir( optionDirname.toString() )
        .inputFile( optionTransname.toString() )
        .listRepoFiles( optionListtrans.toString() )
        .listRepoDirs( optionListdir.toString() )
        .exportRepo( optionExprep.toString() )
        .localFile( optionFilename.toString() )
        .localJarFile( optionJarFilename.toString() )
        .localInitialDir( initialDir.toString() )
        .listRepos( optionListrep.toString() )
        .safeMode( optionSafemode.toString() )
        .metrics( optionMetrics.toString() )
        .listFileParams( optionListParam.toString() )
        .logLevel( optionLoglevel.toString() )
        .maxLogLines( "" )
        .maxLogTimeout( "" )
        .logFile( "" )
        .oldLogFile( "" )
        .version( "" )
        .resultSetStepName( optionResultSetStepName.toString() )
        .resultSetCopyNumber( optionResultSetCopyNumber.toString() )
        .base64Zip( optionBase64Zip.toString() )
        .namedParams( optionParams )
        .pluginNamedParams( pluginNamedParams )
        .runConfiguration( optionRunConfiguration.toString() )
        .browserAuth( optionBrowserAuth.toString() )
        .deviceCode( optionDeviceCode.toString() )
        .preferredIdp( optionPreferredIdp.toString() )
        .serviceAccount( optionServiceAccount.toString() )
        .build();
    }
  }

  /**
   * Configures the transformation with the given parameters and their values
   *
   * @param trans        the executable transformation object
   * @param optionParams the list of parameters to set for the transformation
   * @param transMeta    the transformation metadata
   * @throws UnknownParamException
   */
  protected static void configureParameters( Trans trans, NamedParams optionParams,
                                             TransMeta transMeta ) throws UnknownParamException {
    PanCommandExecutor.configureParameters( trans, optionParams, transMeta );
  }

  private static void exitJVM( int status ) {
    ExitInterceptor.exit( status );
  }

  public static PanCommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public static void setCommandExecutor( PanCommandExecutor commandExecutor ) {
    Pan.commandExecutor = commandExecutor;
  }
}
