/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.pan;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.base.Params;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.logging.FileLoggingEventListener;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.kitchen.Kitchen;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

public class Pan {
  private static Class<?> PKG = Pan.class; // for i18n purposes, needed by Translator2!!

  public static final String STRING_PAN = "Pan";
  private static LogChannelInterface log = new LogChannel( STRING_PAN );

  private static FileLoggingEventListener fileLoggingEventListener;

  private static PanCommandExecutor commandExecutor;

  public static void main( String[] a ) throws Exception {
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
    KettleEnvironment.init();
    Locale.setDefault( LanguageChoice.getInstance().getDefaultLocale() );

    List<String> args = new ArrayList<String>();
    for ( int i = 0; i < a.length; i++ ) {
      if ( a[i].length() > 0 ) {
        args.add( a[i] );
      }
    }

    // The options:
    StringBuilder optionRepname, optionUsername, optionTrustUser,  optionPassword, optionTransname, optionDirname;
    StringBuilder optionFilename, optionLoglevel, optionLogfile, optionLogfileOld, optionListdir;
    StringBuilder optionListtrans, optionListrep, optionExprep, optionNorep, optionSafemode;
    StringBuilder optionVersion, optionJarFilename, optionListParam, optionMetrics, initialDir;
    StringBuilder optionResultSetStepName, optionResultSetCopyNumber;
    StringBuilder optionBase64Zip, optionUuid;

    NamedParams optionParams = new NamedParamsDefault();

    CommandLineOption maxLogLinesOption =
      new CommandLineOption(
        "maxloglines", BaseMessages.getString( PKG, "Pan.CmdLine.MaxLogLines" ), new StringBuilder() );
    CommandLineOption maxLogTimeoutOption =
      new CommandLineOption(
        "maxlogtimeout", BaseMessages.getString( PKG, "Pan.CmdLine.MaxLogTimeout" ), new StringBuilder() );

    CommandLineOption[] options =
      new CommandLineOption[]{
        new CommandLineOption( "rep", BaseMessages.getString( PKG, "Pan.ComdLine.RepName" ), optionRepname =
          new StringBuilder() ),
        new CommandLineOption(
          "user", BaseMessages.getString( PKG, "Pan.ComdLine.RepUsername" ), optionUsername =
          new StringBuilder() ),
        new CommandLineOption(
          "trustuser", BaseMessages.getString( PKG, "Pan.ComdLine.RepUsername" ), optionTrustUser =
          new StringBuilder() ),
        new CommandLineOption(
          "pass", BaseMessages.getString( PKG, "Pan.ComdLine.RepPassword" ), optionPassword =
          new StringBuilder() ),
        new CommandLineOption(
          "trans", BaseMessages.getString( PKG, "Pan.ComdLine.TransName" ), optionTransname =
          new StringBuilder() ),
        new CommandLineOption( "dir", BaseMessages.getString( PKG, "Pan.ComdLine.RepDir" ), optionDirname =
          new StringBuilder() ),
        new CommandLineOption(
          "file", BaseMessages.getString( PKG, "Pan.ComdLine.XMLTransFile" ), optionFilename =
          new StringBuilder() ),
        new CommandLineOption(
          "level", BaseMessages.getString( PKG, "Pan.ComdLine.LogLevel" ), optionLoglevel =
          new StringBuilder() ),
        new CommandLineOption(
          "logfile", BaseMessages.getString( PKG, "Pan.ComdLine.LogFile" ), optionLogfile =
          new StringBuilder() ),
        new CommandLineOption(
          "log", BaseMessages.getString( PKG, "Pan.ComdLine.LogOldFile" ), optionLogfileOld =
          new StringBuilder(), false, true ),
        new CommandLineOption(
          "listdir", BaseMessages.getString( PKG, "Pan.ComdLine.ListDirRep" ), optionListdir =
          new StringBuilder(), true, false ),
        new CommandLineOption(
          "listtrans", BaseMessages.getString( PKG, "Pan.ComdLine.ListTransDir" ), optionListtrans =
          new StringBuilder(), true, false ),
        new CommandLineOption(
          "listrep", BaseMessages.getString( PKG, "Pan.ComdLine.ListReps" ), optionListrep =
          new StringBuilder(), true, false ),
        new CommandLineOption(
          "exprep", BaseMessages.getString( PKG, "Pan.ComdLine.ExpObjectsXML" ), optionExprep =
          new StringBuilder(), true, false ),
        new CommandLineOption( "norep", BaseMessages.getString( PKG, "Pan.ComdLine.NoRep" ), optionNorep =
          new StringBuilder(), true, false ),
        new CommandLineOption(
          "safemode", BaseMessages.getString( PKG, "Pan.ComdLine.SafeMode" ), optionSafemode =
          new StringBuilder(), true, false ),
        new CommandLineOption(
          "version", BaseMessages.getString( PKG, "Pan.ComdLine.Version" ), optionVersion =
          new StringBuilder(), true, false ),
        new CommandLineOption(
          "jarfile", BaseMessages.getString( PKG, "Pan.ComdLine.JarFile" ), optionJarFilename =
          new StringBuilder(), false, true ),
        new CommandLineOption(
          "param", BaseMessages.getString( PKG, "Pan.ComdLine.Param" ), optionParams, false ),
        new CommandLineOption(
          "listparam", BaseMessages.getString( PKG, "Pan.ComdLine.ListParam" ), optionListParam =
          new StringBuilder(), true, false ),
        new CommandLineOption(
          "initialDir", null, initialDir =
          new StringBuilder(), false, true ),
        new CommandLineOption(
          "stepname", "ResultSetStepName", optionResultSetStepName =
          new StringBuilder(), false, true ),
        new CommandLineOption(
          "copynum", "ResultSetCopyNumber", optionResultSetCopyNumber =
          new StringBuilder(), false, true ),
        new CommandLineOption(
          "zip", "Base64Zip", optionBase64Zip =
          new StringBuilder(), false, true ),
        new CommandLineOption(
                "uuid", "UUID", optionUuid =
                new StringBuilder(), false, true ),
        new CommandLineOption(
          "metrics", BaseMessages.getString( PKG, "Pan.ComdLine.Metrics" ), optionMetrics =
          new StringBuilder(), true, false ), maxLogLinesOption, maxLogTimeoutOption };


    if ( args.size() == 2 ) { // 2 internal hidden argument (flag and value)
      CommandLineOption.printUsage( options );
      exitJVM( CommandExecutorCodes.Pan.CMD_LINE_PRINT.getCode() );
    }



    // Parse the options...
    if ( !CommandLineOption.parseArguments( args, options, log ) ) {
      log.logError( BaseMessages.getString( PKG, "Pan.Error.CommandLineError" ) );

      exitJVM( CommandExecutorCodes.Pan.ERROR_LOADING_STEPS_PLUGINS.getCode() );
    }

    Kitchen.configureLogging( maxLogLinesOption, maxLogTimeoutOption );

    String kettleRepname = Const.getEnvironmentVariable( Const.KETTLE_REPOSITORY, null );
    String kettleUsername = Const.getEnvironmentVariable( Const.KETTLE_USER, null );
    String kettlePassword = Const.getEnvironmentVariable(  Const.KETTLE_PASSWORD, null );

    if ( kettleRepname != null && kettleRepname.length() > 0 ) {
      optionRepname = new StringBuilder( kettleRepname );
    }
    if ( kettleUsername != null && kettleUsername.length() > 0 ) {
      optionUsername = new StringBuilder( kettleUsername );
    }
    if ( kettlePassword != null && kettlePassword.length() > 0 ) {
      optionPassword = new StringBuilder( kettlePassword );
    }

    if ( Utils.isEmpty( optionLogfile ) && !Utils.isEmpty( optionLogfileOld ) ) {
      // if the old style of logging name is filled in, and the new one is not
      // overwrite the new by the old
      optionLogfile = optionLogfileOld;
    }

    if ( !Utils.isEmpty( optionLogfile ) ) {
      fileLoggingEventListener = new FileLoggingEventListener( optionLogfile.toString(), true );
      KettleLogStore.getAppender().addLoggingEventListener( fileLoggingEventListener );
    } else {
      fileLoggingEventListener = null;
    }

    if ( !Utils.isEmpty( optionLoglevel ) ) {
      log.setLogLevel( LogLevel.getLogLevelForCode( optionLoglevel.toString() ) );
      log.logMinimal( BaseMessages.getString( PKG, "Pan.Log.Loglevel", log.getLogLevel().getDescription() ) );

    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////
    // This is where the action starts.
    // Print the options before we start processing when running in Debug or
    // Rowlevel
    //
    if ( log.isDebug() ) {
      System.out.println( "Arguments:" );
      for ( int i = 0; i < options.length; i++ ) {
        /* if (!options[i].isHiddenOption()) */
        System.out.println( Const.rightPad( options[i].getOption(), 12 ) + " : " + options[i].getArgument() );
      }
      System.out.println( "" );
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////

    try {

      if ( getCommandExecutor() == null ) {
        setCommandExecutor( new PanCommandExecutor( PKG, log ) ); // init
      }

      if ( !Utils.isEmpty( optionVersion ) ) {
        getCommandExecutor().printVersion();

        if ( a.length == 1 ) {
          exitJVM( CommandExecutorCodes.Pan.KETTLE_VERSION_PRINT.getCode() );
        }
      }

      Params.Builder builder = optionUuid.length() > 0 ? new Params.Builder( optionUuid.toString() ) : new Params.Builder();
      Params transParams = ( builder )
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
              .logLevel( "" )
              .maxLogLines( "" )
              .maxLogTimeout( "" )
              .logFile( "" )
              .oldLogFile( "" )
              .version( "" )
              .resultSetStepName( optionResultSetStepName.toString() )
              .resultSetCopyNumber( optionResultSetCopyNumber.toString() )
              .base64Zip( optionBase64Zip.toString() )
              .namedParams( optionParams )
              .build();

      Result result = getCommandExecutor().execute( transParams, args.toArray( new String[ args.size() ] )  );

      exitJVM( result.getExitStatus() );

    } catch ( Throwable t ) {
      t.printStackTrace();
      exitJVM( CommandExecutorCodes.Pan.UNEXPECTED_ERROR.getCode() );
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

  private static final void exitJVM( int status ) {

    // Let's not forget to close the log file we're writing to...
    //
    if ( fileLoggingEventListener != null ) {
      try {
        fileLoggingEventListener.close();
      } catch ( Exception e ) {
        e.printStackTrace( System.err );
        status = 1;
      }
      KettleLogStore.getAppender().removeLoggingEventListener( fileLoggingEventListener );
    }

    System.exit( status );
  }

  public static PanCommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public static void setCommandExecutor( PanCommandExecutor commandExecutor ) {
    Pan.commandExecutor = commandExecutor;
  }
}
