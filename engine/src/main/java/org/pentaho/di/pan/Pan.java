/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.FileUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
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
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.kitchen.Kitchen;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.version.BuildVersion;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;
import org.w3c.dom.Document;

public class Pan {
  private static Class<?> PKG = Pan.class; // for i18n purposes, needed by Translator2!!

  private static final String STRING_PAN = "Pan";

  private static FileLoggingEventListener fileLoggingEventListener;

  public static void main( String[] a ) throws Exception {
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.PAN );
    KettleEnvironment.init();

    List<String> args = new ArrayList<String>();
    for ( int i = 0; i < a.length; i++ ) {
      if ( a[i].length() > 0 ) {
        args.add( a[i] );
      }
    }

    DelegatingMetaStore metaStore = new DelegatingMetaStore();
    metaStore.addMetaStore( MetaStoreConst.openLocalPentahoMetaStore() );
    metaStore.setActiveMetaStoreName( metaStore.getName() );

    RepositoryMeta repositoryMeta = null;
    Trans trans = null;

    // The options:
    StringBuilder optionRepname, optionUsername, optionPassword, optionTransname, optionDirname;
    StringBuilder optionFilename, optionLoglevel, optionLogfile, optionLogfileOld, optionListdir;
    StringBuilder optionListtrans, optionListrep, optionExprep, optionNorep, optionSafemode;
    StringBuilder optionVersion, optionJarFilename, optionListParam, optionMetrics, initialDir;

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
          "metrics", BaseMessages.getString( PKG, "Pan.ComdLine.Metrics" ), optionMetrics =
          new StringBuilder(), true, false ), maxLogLinesOption, maxLogTimeoutOption };

    if ( args.size() == 2 ) { // 2 internal hidden argument (flag and value)
      CommandLineOption.printUsage( options );
      exitJVM( 9 );
    }

    LogChannelInterface log = new LogChannel( STRING_PAN );

    // Parse the options...
    if ( !CommandLineOption.parseArguments( args, options, log ) ) {
      log.logError( BaseMessages.getString( PKG, "Pan.Error.CommandLineError" ) );

      exitJVM( 8 );
    }

    Kitchen.configureLogging( maxLogLinesOption, maxLogTimeoutOption );

    String kettleRepname = Const.getEnvironmentVariable( "KETTLE_REPOSITORY", null );
    String kettleUsername = Const.getEnvironmentVariable( "KETTLE_USER", null );
    String kettlePassword = Const.getEnvironmentVariable( "KETTLE_PASSWORD", null );

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

    if ( !Utils.isEmpty( optionVersion ) ) {
      BuildVersion buildVersion = BuildVersion.getInstance();
      if ( log.isBasic() ) {
        log.logBasic( BaseMessages.getString(
          PKG, "Pan.Log.KettleVersion", buildVersion.getVersion(), buildVersion.getRevision(), buildVersion
            .getBuildDate() ) );
      }

      if ( a.length == 1 ) {
        exitJVM( 6 );
      }
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

    log.logMinimal( BaseMessages.getString( PKG, "Pan.Log.StartingToRun" ) );

    Date start, stop;
    Calendar cal;
    SimpleDateFormat df = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss.SSS" );
    cal = Calendar.getInstance();
    start = cal.getTime();

    if ( log.isDebug() ) {
      log.logDebug( BaseMessages.getString( PKG, "Pan.Log.AllocatteNewTrans" ) );
    }

    TransMeta transMeta = new TransMeta();
    // In case we use a repository...
    Repository rep = null;
    try {
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "Pan.Log.StartingToLookOptions" ) );
      }

      // Read kettle transformation specified on command-line?
      if ( !Utils.isEmpty( optionRepname )
        || !Utils.isEmpty( optionFilename ) || !Utils.isEmpty( optionJarFilename ) ) {
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "Pan.Log.ParsingCommandline" ) );
        }

        if ( !Utils.isEmpty( optionRepname ) && !"Y".equalsIgnoreCase( optionNorep.toString() ) ) {
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "Pan.Log.LoadingAvailableRep" ) );
          }

          RepositoriesMeta repsinfo = new RepositoriesMeta();
          repsinfo.getLog().setLogLevel( log.getLogLevel() );

          try {
            repsinfo.readData();
          } catch ( Exception e ) {
            throw new KettleException( BaseMessages.getString( PKG, "Pan.Error.NoRepsDefined" ), e );
          }

          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "Pan.Log.FindingRep", "" + optionRepname ) );
          }

          repositoryMeta = repsinfo.findRepository( optionRepname.toString() );
          if ( repositoryMeta != null ) {
            // Define and connect to the repository...
            if ( log.isDebug() ) {
              log.logDebug( BaseMessages.getString( PKG, "Pan.Log.Allocate&ConnectRep" ) );
            }

            rep =
              PluginRegistry.getInstance().loadClass(
                RepositoryPluginType.class, repositoryMeta, Repository.class );
            rep.init( repositoryMeta );
            rep.getLog().setLogLevel( log.getLogLevel() );
            rep.connect( optionUsername != null ? optionUsername.toString() : null, optionPassword != null
              ? optionPassword.toString() : null );

            rep.getSecurityProvider().validateAction( RepositoryOperation.EXECUTE_TRANSFORMATION );

            // Default is the root directory
            //
            RepositoryDirectoryInterface directory = rep.loadRepositoryDirectoryTree();

            // Add the IMetaStore of the repository to our delegation
            //
            if ( rep.getMetaStore() != null ) {
              metaStore.addMetaStore( rep.getMetaStore() );
            }

            // Find the directory name if one is specified...
            if ( !Utils.isEmpty( optionDirname ) ) {
              directory = directory.findDirectory( optionDirname.toString() );
            }

            if ( directory != null ) {
              // Check username, password
              if ( log.isDebug() ) {
                log.logDebug( BaseMessages.getString( PKG, "Pan.Log.CheckSuppliedUserPass" ) );
              }

              // Load a transformation
              if ( !Utils.isEmpty( optionTransname ) ) {
                if ( log.isDebug() ) {
                  log.logDebug( BaseMessages.getString( PKG, "Pan.Log.LoadTransInfo" ) );
                }

                transMeta = rep.loadTransformation( optionTransname.toString(), directory, null, true, null );
                if ( log.isDebug() ) {
                  log.logDebug( BaseMessages.getString( PKG, "Pan.Log.AllocateTrans" ) );
                }

                trans = new Trans( transMeta );
                trans.setRepository( rep );
                trans.setMetaStore( metaStore );

              } else if ( "Y".equalsIgnoreCase( optionListtrans.toString() ) ) {
                // List the transformations in the repository
                if ( log.isDebug() ) {
                  log
                    .logDebug( BaseMessages.getString( PKG, "Pan.Log.GettingListTransDirectory", "" + directory ) );
                }

                String[] transnames = rep.getTransformationNames( directory.getObjectId(), false );
                for ( int i = 0; i < transnames.length; i++ ) {
                  System.out.println( transnames[i] );
                }
              } else if ( "Y".equalsIgnoreCase( optionListdir.toString() ) ) {
                // List the directories in the repository
                String[] dirnames = rep.getDirectoryNames( directory.getObjectId() );
                for ( int i = 0; i < dirnames.length; i++ ) {
                  System.out.println( dirnames[i] );
                }
              } else if ( !Utils.isEmpty( optionExprep ) ) {
                // Export the repository
                System.out.println( BaseMessages.getString( PKG, "Pan.Log.ExportingObjectsRepToFile", ""
                  + optionExprep ) );

                rep.getExporter().exportAllObjects( null, optionExprep.toString(), directory, "all" );
                System.out.println( BaseMessages.getString( PKG, "Pan.Log.FinishedExportObjectsRepToFile", ""
                  + optionExprep ) );
              } else {
                System.out.println( BaseMessages.getString( PKG, "Pan.Error.NoTransNameSupplied" ) );
              }
            } else {
              System.out.println( BaseMessages.getString( PKG, "Pan.Error.CanNotFindSpecifiedDirectory", ""
                + optionDirname ) );
              repositoryMeta = null;
            }
          } else {
            System.out.println( BaseMessages.getString( PKG, "Pan.Error.NoRepProvided" ) );
          }
        }

        // Try to load the transformation from file, even if it failed to load
        // from the repository
        // You could implement some fail-over mechanism this way.
        //
        if ( trans == null && !Utils.isEmpty( optionFilename ) ) {

          String fileName = optionFilename.toString();
          // If the filename starts with scheme like zip:, then isAbsolute() will return false even though the
          // the path following the zip is absolute path. Check for isAbsolute only if the fileName does not
          // start with scheme
          if ( !KettleVFS.startsWithScheme( fileName ) && !FileUtil.isFullyQualified( fileName ) ) {
            fileName = initialDir.toString() + fileName;
          }

          if ( log.isDetailed() ) {
            log.logDetailed( BaseMessages.getString( PKG, "Pan.Log.LoadingTransXML", "" + fileName ) );
          }
          transMeta = new TransMeta( fileName );
          trans = new Trans( transMeta );
        }

        // Try to load the transformation from a jar file
        //
        if ( trans == null && !Utils.isEmpty( optionJarFilename ) ) {
          try {
            if ( log.isDetailed() ) {
              log.logDetailed( BaseMessages.getString( PKG, "Pan.Log.LoadingTransJar", "" + optionJarFilename ) );
            }

            InputStream inputStream = Pan.class.getResourceAsStream( optionJarFilename.toString() );
            StringBuilder xml = new StringBuilder();
            int c;
            while ( ( c = inputStream.read() ) != -1 ) {
              xml.append( (char) c );
            }
            inputStream.close();
            Document document = XMLHandler.loadXMLString( xml.toString() );
            transMeta = new TransMeta( XMLHandler.getSubNode( document, "transformation" ), null );
            trans = new Trans( transMeta );
          } catch ( Exception e ) {
            System.out.println( BaseMessages.getString( PKG, "Pan.Error.ReadingJar", e.toString() ) );

            System.out.println( Const.getStackTracker( e ) );
            throw e;
          }
        }
      }

      if ( "Y".equalsIgnoreCase( optionListrep.toString() ) ) {
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "Pan.Log.GettingListReps" ) );
        }

        RepositoriesMeta ri = new RepositoriesMeta();
        try {
          ri.readData();
        } catch ( Exception e ) {
          throw new KettleException( BaseMessages.getString( PKG, "Pan.Error.UnableReadXML" ), e );
        }

        System.out.println( BaseMessages.getString( PKG, "Pan.Log.ListReps" ) );

        for ( int i = 0; i < ri.nrRepositories(); i++ ) {
          RepositoryMeta rinfo = ri.getRepository( i );
          System.out.println( BaseMessages.getString(
            PKG, "Pan.Log.RepNameDesc", "" + ( i + 1 ), rinfo.getName(), rinfo.getDescription() ) );
        }
      }
    } catch ( Exception e ) {
      trans = null;
      transMeta = null;
      if ( rep != null ) {
        rep.disconnect();
      }
      System.out.println( BaseMessages.getString( PKG, "Pan.Error.ProcessStopError", e.getMessage() ) );

      e.printStackTrace();
      exitJVM( 1 );
    }

    if ( trans == null ) {
      if ( rep != null ) {
        rep.disconnect();
      }

      if ( !"Y".equalsIgnoreCase( optionListtrans.toString() )
        && !"Y".equalsIgnoreCase( optionListdir.toString() ) && !"Y".equalsIgnoreCase( optionListrep.toString() )
        && Utils.isEmpty( optionExprep ) ) {
        System.out.println( BaseMessages.getString( PKG, "Pan.Error.CanNotLoadTrans" ) );

        exitJVM( 7 );
      } else {
        exitJVM( 0 );
      }

    }

    try {
      trans.setLogLevel( log.getLogLevel() );
      configureParameters( trans, optionParams, transMeta );

      // See if we want to run in safe mode:
      if ( "Y".equalsIgnoreCase( optionSafemode.toString() ) ) {
        trans.setSafeModeEnabled( true );
      }

      // Enable kettle metric gathering if required:
      if ( "Y".equalsIgnoreCase( optionMetrics.toString() ) ) {
        trans.setGatheringMetrics( true );
      }

      // List the parameters defined in this transformation
      // Then simply exit...
      //
      if ( "Y".equalsIgnoreCase( optionListParam.toString() ) ) {
        for ( String parameterName : trans.listParameters() ) {
          String value = trans.getParameterValue( parameterName );
          String deflt = trans.getParameterDefault( parameterName );
          String descr = trans.getParameterDescription( parameterName );

          if ( deflt != null ) {
            System.out.println( "Parameter: "
              + parameterName + "=" + Const.NVL( value, "" ) + ", default=" + deflt + " : "
              + Const.NVL( descr, "" ) );
          } else {
            System.out.println( "Parameter: "
              + parameterName + "=" + Const.NVL( value, "" ) + " : " + Const.NVL( descr, "" ) );
          }
        }

        // stop right here...
        //
        exitJVM( 7 ); // same as the other list options
      }

      // allocate & run the required sub-threads
      try {
        trans.execute( args.toArray( new String[args.size()] ) );
      } catch ( KettleException e ) {
        System.out.println( BaseMessages.getString( PKG, "Pan.Error.UnablePrepareInitTrans" ) );

        exitJVM( 3 );
      }

      trans.waitUntilFinished();

      // Give the transformation up to 10 seconds to finish execution
      for ( int i = 0; i < 100; i++ ) {
        if ( !trans.isRunning() ) {
          break;
        }
        try {
          Thread.sleep( 100 );
        } catch ( Exception e ) {
          break;
        }
      }

      if ( trans.isRunning() ) {
        log.logError( BaseMessages.getString( PKG, "Pan.Log.NotStopping" ) );
      }

      log.logMinimal( BaseMessages.getString( PKG, "Pan.Log.Finished" ) );

      cal = Calendar.getInstance();
      stop = cal.getTime();
      String begin = df.format( start ).toString();
      String end = df.format( stop ).toString();

      log.logMinimal( BaseMessages.getString( PKG, "Pan.Log.StartStop", begin, end ) );

      long millis = stop.getTime() - start.getTime();
      int seconds = (int) ( millis / 1000 );
      if ( seconds <= 60 ) {
        log.logMinimal( BaseMessages.getString( PKG, "Pan.Log.ProcessingEndAfter", String.valueOf( seconds ) ) );
      } else if ( seconds <= 60 * 60 ) {
        int min = ( seconds / 60 );
        int rem = ( seconds % 60 );
        log.logMinimal( BaseMessages.getString(
          PKG, "Pan.Log.ProcessingEndAfterLong", String.valueOf( min ), String.valueOf( rem ), String
            .valueOf( seconds ) ) );
      } else if ( seconds <= 60 * 60 * 24 ) {
        int rem;
        int hour = ( seconds / ( 60 * 60 ) );
        rem = ( seconds % ( 60 * 60 ) );
        int min = rem / 60;
        rem = rem % 60;
        log.logMinimal( BaseMessages.getString(
          PKG, "Pan.Log.ProcessingEndAfterLonger", String.valueOf( hour ), String.valueOf( min ), String
            .valueOf( rem ), String.valueOf( seconds ) ) );
      } else {
        int rem;
        int days = ( seconds / ( 60 * 60 * 24 ) );
        rem = ( seconds % ( 60 * 60 * 24 ) );
        int hour = rem / ( 60 * 60 );
        rem = rem % ( 60 * 60 );
        int min = rem / 60;
        rem = rem % 60;
        log.logMinimal( BaseMessages.getString(
          PKG, "Pan.Log.ProcessingEndAfterLongest", String.valueOf( days ), String.valueOf( hour ), String
            .valueOf( min ), String.valueOf( rem ), String.valueOf( seconds ) ) );
      }

      if ( trans.getResult().getNrErrors() == 0 ) {
        trans.printStats( seconds );
        exitJVM( 0 );
      } else {

        String transJVMExitCode = trans.getVariable( Const.KETTLE_TRANS_PAN_JVM_EXIT_CODE );

        // If the trans has a return code to return to the OS, then we exit with that
        if ( !Utils.isEmpty( transJVMExitCode ) ) {
          try {
            exitJVM( Integer.valueOf( transJVMExitCode ) );
          } catch ( NumberFormatException nfe ) {
            log
              .logError( BaseMessages.getString(
                PKG, "Pan.Error.TransJVMExitCodeInvalid", Const.KETTLE_TRANS_PAN_JVM_EXIT_CODE,
                transJVMExitCode ) );
            log.logError( BaseMessages.getString( PKG, "Pan.Log.JVMExitCode", "1" ) );
            exitJVM( 1 );
          }
        } else { // the trans does not have a return code.
          exitJVM( 1 );
        }
      }
    } catch ( KettleException ke ) {
      System.out.println( BaseMessages.getString( PKG, "Pan.Log.ErrorOccurred", "" + ke.getMessage() ) );

      log.logError( BaseMessages.getString( PKG, "Pan.Log.UnexpectedErrorOccurred", "" + ke.getMessage() ) );

      exitJVM( 2 );
    } finally {
      if ( rep != null ) {
        rep.disconnect();
      }
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
    trans.initializeVariablesFrom( null );
    trans.getTransMeta().setInternalKettleVariables( trans );

    // Map the command line named parameters to the actual named parameters.
    // Skip for
    // the moment any extra command line parameter not known in the
    // transformation.
    String[] transParams = trans.listParameters();
    for ( String param : transParams ) {
      String value = optionParams.getParameterValue( param );
      if ( value != null ) {
        trans.setParameterValue( param, value );
        transMeta.setParameterValue( param, value );
      }
    }
    // Put the parameters over the already defined variable space. Parameters
    // get priority.
    trans.activateParameters();
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
}
