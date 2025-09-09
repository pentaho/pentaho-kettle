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
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.ExecutorUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.pan.CommandExecutorResult;
import org.pentaho.di.pan.CommandLineOption;
import org.pentaho.di.pan.CommandLineOptionProvider;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Kitchen {
  private static Class<?> PKG = Kitchen.class; // for i18n purposes, needed by Translator2!!

  public static final String STRING_KITCHEN = "Kitchen";

  private static KitchenCommandExecutor commandExecutor;

  private static FileLoggingEventListener fileAppender;

  public static void main( String[] a ) throws Exception {
    final ExecutorService executor = ExecutorUtil.getExecutor();
    final RepositoryPluginType repositoryPluginType = RepositoryPluginType.getInstance();
    Locale.setDefault( LanguageChoice.getInstance().getDefaultLocale() );
    final Future<Map.Entry<KettlePluginException, Future<KettleException>>> repositoryRegisterFuture =
      executor.submit( new Callable<Map.Entry<KettlePluginException, Future<KettleException>>>() {

        @Override
        public Map.Entry<KettlePluginException, Future<KettleException>> call() throws Exception {
          PluginRegistry.addPluginType( repositoryPluginType );
          try {
            KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.KITCHEN );
            KettleClientEnvironment.init();
          } catch ( KettlePluginException e ) {
            return new AbstractMap.SimpleImmutableEntry<KettlePluginException, Future<KettleException>>( e, null );
          }

          Future<KettleException> kettleEnvironmentInitFuture =
            executor.submit( new Callable<KettleException>() {

              @Override
              public KettleException call() throws Exception {
                try {
                  KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.KITCHEN );
                  KettleEnvironment.init();
                } catch ( KettleException e ) {
                  return e;
                }
                return null;
              }
            } );
          return new AbstractMap.SimpleImmutableEntry<KettlePluginException, Future<KettleException>>( null,
            kettleEnvironmentInitFuture );
        }
      } );

    List<String> args = new ArrayList<String>();
    for ( int i = 0; i < a.length; i++ ) {
      if ( a[ i ].length() > 0 ) {
        args.add( a[ i ] );
      }
    }

    StringBuilder optionRepname, optionUsername, optionTrustUser, optionPassword, optionJobname, optionDirname,
      initialDir;
    StringBuilder optionFilename, optionLoglevel, optionLogfile, optionLogfileOld, optionListdir;
    StringBuilder optionListjobs, optionListrep, optionNorep, optionVersion, optionListParam, optionExport,
      optionBase64Zip, optionUuid;
    StringBuilder pluginParam = new StringBuilder();
    NamedParams optionParams = new NamedParamsDefault();
    NamedParams customOptions = new NamedParamsDefault();

    CommandLineOption maxLogLinesOption =
      new CommandLineOption(
        "maxloglines", BaseMessages.getString( PKG, "Kitchen.CmdLine.MaxLogLines" ), new StringBuilder() );
    CommandLineOption maxLogTimeoutOption =
      new CommandLineOption(
        "maxlogtimeout", BaseMessages.getString( PKG, "Kitchen.CmdLine.MaxLogTimeout" ), new StringBuilder() );

    CommandLineOption[] options =
      new CommandLineOption[] {
        new CommandLineOption( "rep", BaseMessages.getString( PKG, "Kitchen.CmdLine.RepName" ), optionRepname =
          new StringBuilder() ),
        new CommandLineOption(
          "user", BaseMessages.getString( PKG, "Kitchen.CmdLine.RepUsername" ), optionUsername =
          new StringBuilder() ),
        new CommandLineOption(
          "trustuser", BaseMessages.getString( PKG, "Kitchen.ComdLine.RepUsername" ), optionTrustUser =
          new StringBuilder() ),
        new CommandLineOption(
          "pass", BaseMessages.getString( PKG, "Kitchen.CmdLine.RepPassword" ), optionPassword =
          new StringBuilder() ),
        new CommandLineOption(
          "job", BaseMessages.getString( PKG, "Kitchen.CmdLine.RepJobName" ), optionJobname =
          new StringBuilder() ),
        new CommandLineOption( "dir", BaseMessages.getString( PKG, "Kitchen.CmdLine.RepDir" ), optionDirname =
          new StringBuilder() ),
        new CommandLineOption(
          "file", BaseMessages.getString( PKG, "Kitchen.CmdLine.XMLJob" ), optionFilename =
          new StringBuilder() ),
        new CommandLineOption(
          "level", BaseMessages.getString( PKG, "Kitchen.CmdLine.LogLevel" ), optionLoglevel =
          new StringBuilder() ),
        new CommandLineOption(
          "logfile", BaseMessages.getString( PKG, "Kitchen.CmdLine.LogFile" ), optionLogfile =
          new StringBuilder() ),
        new CommandLineOption(
          "log", BaseMessages.getString( PKG, "Kitchen.CmdLine.LogFileOld" ), optionLogfileOld =
          new StringBuilder(), false, true ),
        new CommandLineOption(
          "listdir", BaseMessages.getString( PKG, "Kitchen.CmdLine.ListDir" ), optionListdir =
          new StringBuilder(), true, false ),
        new CommandLineOption(
          "listjobs", BaseMessages.getString( PKG, "Kitchen.CmdLine.ListJobsDir" ), optionListjobs =
          new StringBuilder(), true, false ),
        new CommandLineOption(
          "listrep", BaseMessages.getString( PKG, "Kitchen.CmdLine.ListAvailableReps" ), optionListrep =
          new StringBuilder(), true, false ),
        new CommandLineOption( "norep", BaseMessages.getString( PKG, "Kitchen.CmdLine.NoRep" ), optionNorep =
          new StringBuilder(), true, false ),
        new CommandLineOption(
          "version", BaseMessages.getString( PKG, "Kitchen.CmdLine.Version" ), optionVersion =
          new StringBuilder(), true, false ),
        new CommandLineOption(
          "param", BaseMessages.getString( PKG, "Kitchen.ComdLine.Param" ), optionParams, false ),
        new CommandLineOption(
          "listparam", BaseMessages.getString( PKG, "Kitchen.ComdLine.ListParam" ), optionListParam =
          new StringBuilder(), true, false ),
        new CommandLineOption(
          "export", BaseMessages.getString( PKG, "Kitchen.ComdLine.Export" ), optionExport =
          new StringBuilder(), true, false ),
        new CommandLineOption(
          "initialDir", null, initialDir =
          new StringBuilder(), false, true ),
        new CommandLineOption( "zip", "Base64Zip", optionBase64Zip = new StringBuilder(), false, true ),
        new CommandLineOption( "uuid", "UUID", optionUuid = new StringBuilder(), false, true ),
        new CommandLineOption(
          "custom", BaseMessages.getString( PKG, "Kitchen.ComdLine.Custom" ), customOptions, false ),
        maxLogLinesOption, maxLogTimeoutOption, };

    configureLogging( maxLogLinesOption, maxLogTimeoutOption );

    String kettleRepname = Const.getEnvironmentVariable( "KETTLE_REPOSITORY", null );
    String kettleUsername = Const.getEnvironmentVariable( "KETTLE_USER", null );
    String kettlePassword = Const.getEnvironmentVariable( "KETTLE_PASSWORD", null );

    if ( !Utils.isEmpty( kettleRepname ) ) {
      optionRepname = new StringBuilder( kettleRepname );
    }
    if ( !Utils.isEmpty( kettleUsername ) ) {
      optionUsername = new StringBuilder( kettleUsername );
    }
    if ( !Utils.isEmpty( kettlePassword ) ) {
      optionPassword = new StringBuilder( kettlePassword );
    }

    if ( Utils.isEmpty( optionLogfile ) && !Utils.isEmpty( optionLogfileOld ) ) {
      // if the old style of logging name is filled in, and the new one is not
      // overwrite the new by the old
      optionLogfile = optionLogfileOld;
    }

    Map.Entry<KettlePluginException, Future<KettleException>> repositoryRegisterResults =
      repositoryRegisterFuture.get();
    // It's a singleton map with one key-value pair (a Pair collection)
    KettlePluginException repositoryRegisterException = repositoryRegisterResults.getKey();
    if ( repositoryRegisterException != null ) {
      throw repositoryRegisterException;
    }
    Future<KettleException> kettleInitFuture = repositoryRegisterResults.getValue();
    List<CommandLineOption> updatedOptionList = getAdditionalCommandlineOption( options, pluginParam );
    options = updatedOptionList.toArray( new CommandLineOption[ 0 ] );
    LogChannelInterface log = new LogChannel( STRING_KITCHEN );

    if ( args.size() == 2 ) {
      CommandLineOption.printUsage( updatedOptionList.toArray( new CommandLineOption[ 0 ] ) );
      blockAndThrow( kettleInitFuture );
      exitJVM( 9 );
    }

    CommandLineOption.parseArguments( args, options, log );

    if ( !Utils.isEmpty( optionLogfile ) ) {
      fileAppender = new FileLoggingEventListener( optionLogfile.toString(), true );
      KettleLogStore.getAppender().addLoggingEventListener( fileAppender );
    } else {
      fileAppender = null;
    }

    if ( !Utils.isEmpty( optionLoglevel ) ) {
      log.setLogLevel( LogLevel.getLogLevelForCode( optionLoglevel.toString() ) );
      log.logMinimal( BaseMessages.getString( PKG, "Kitchen.Log.LogLevel", log.getLogLevel().getDescription() ) );
    }

    // Start the action...
    //
    Result result = new Result();

    try {

      if ( getCommandExecutor() == null ) {
        setCommandExecutor( new KitchenCommandExecutor( PKG, log, kettleInitFuture ) ); // init
      }

      if ( !Utils.isEmpty( optionVersion ) ) {
        getCommandExecutor().printVersion();
        if ( a.length == 1 ) {
          exitJVM( CommandExecutorCodes.Pan.KETTLE_VERSION_PRINT.getCode() );
        }
      }

      if ( !Utils.isEmpty( pluginParam.toString() ) ) {
        // make sure VFS providers are registered
        blockAndThrow( kettleInitFuture );
        CommandExecutorResult rslt = validateAndSetPluginParam( log, pluginParam.toString() );
        if ( rslt.getCode() != 0 ) {
          blockAndThrow( kettleInitFuture );
          log.logError( rslt.getDescription() );
          exitJVM( rslt.getCode() );
        }
      }

      Params.Builder builder =
        optionUuid.length() > 0 ? new Params.Builder( optionUuid.toString() ) : new Params.Builder();
      Params jobParams = ( builder )
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
        .build();

      result = getCommandExecutor().execute( jobParams, args.toArray( new String[ args.size() ] ) );

    } catch ( Throwable t ) {
      t.printStackTrace();
      result.setExitStatus( CommandExecutorCodes.Pan.UNEXPECTED_ERROR.getCode() );

    } finally {
      if ( fileAppender != null ) {
        fileAppender.close();
        KettleLogStore.getAppender().removeLoggingEventListener( fileAppender );
      }
    }

    exitJVM( result.getExitStatus() );

  }

  private static <T extends Throwable> void blockAndThrow( Future<T> future ) throws T {
    try {
      T e = future.get();
      if ( e != null ) {
        throw e;
      }
    } catch ( InterruptedException e ) {
      throw new RuntimeException( e );
    } catch ( ExecutionException e ) {
      throw new RuntimeException( e );
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
        throw new KettleException( BaseMessages.getString( PKG, "Kitchen.Error.InvalidNumberArgument", option
          .getOption(), option.getArgument() ) );
      }
    }
    return def;
  }

  private static List<CommandLineOption> getAdditionalCommandlineOption( CommandLineOption[] options,
                                                                         StringBuilder param ) {
    List<CommandLineOption> modifiableList = new ArrayList<>();

    Collections.addAll( modifiableList, options );
    try {
      for ( CommandLineOptionProvider provider : PluginServiceLoader.loadServices( CommandLineOptionProvider.class ) ) {
        provider.prepareAdditionalCommandlineOption( modifiableList, param );
      }
    } catch ( KettlePluginException e ) {
      System.out.println( "Exception loading CommandLineOptionProvider services" + e.toString() );
    }
    return modifiableList;
  }

  private static final void exitJVM( int status ) {

    System.exit( status );
  }

  public static KitchenCommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public static void setCommandExecutor( KitchenCommandExecutor commandExecutor ) {
    Kitchen.commandExecutor = commandExecutor;
  }

  private static CommandExecutorResult validateAndSetPluginParam( LogChannelInterface log, String param ) {
    CommandExecutorResult result = null;
    try {
      for ( CommandLineOptionProvider provider : PluginServiceLoader.loadServices( CommandLineOptionProvider.class ) ) {
        result = provider.handleParameter( log, param );
        if ( result.getCode() != 0 ) {
          break; // if result is NOT SUCCESS, break out of the loop
        }
      }
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
    return result;
  }
}
