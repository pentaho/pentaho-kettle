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

package org.pentaho.di.kitchen;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.FileUtil;
import org.pentaho.di.core.util.Utils;
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
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.ExecutorUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.pan.CommandLineOption;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.resource.ResourceUtil;
import org.pentaho.di.resource.TopLevelResource;
import org.pentaho.di.version.BuildVersion;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;


public class Kitchen {
  private static Class<?> PKG = Kitchen.class; // for i18n purposes, needed by Translator2!!

  public static final String STRING_KITCHEN = "Kitchen";

  private static FileLoggingEventListener fileAppender;

  public static void main( String[] a ) throws Exception {
    final ExecutorService executor = ExecutorUtil.getExecutor();
    final RepositoryPluginType repositoryPluginType = RepositoryPluginType.getInstance();

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
          return new AbstractMap.SimpleImmutableEntry<KettlePluginException, Future<KettleException>>( null, kettleEnvironmentInitFuture );
        }
      } );

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
    Job job = null;

    StringBuilder optionRepname, optionUsername, optionPassword, optionJobname, optionDirname, initialDir;
    StringBuilder optionFilename, optionLoglevel, optionLogfile, optionLogfileOld, optionListdir;
    StringBuilder optionListjobs, optionListrep, optionNorep, optionVersion, optionListParam, optionExport;
    NamedParams optionParams = new NamedParamsDefault();
    NamedParams customOptions = new NamedParamsDefault();

    CommandLineOption maxLogLinesOption =
      new CommandLineOption(
        "maxloglines", BaseMessages.getString( PKG, "Kitchen.CmdLine.MaxLogLines" ), new StringBuilder() );
    CommandLineOption maxLogTimeoutOption =
      new CommandLineOption(
        "maxlogtimeout", BaseMessages.getString( PKG, "Kitchen.CmdLine.MaxLogTimeout" ), new StringBuilder() );

    CommandLineOption[] options =
      new CommandLineOption[]{
        new CommandLineOption( "rep", BaseMessages.getString( PKG, "Kitchen.CmdLine.RepName" ), optionRepname =
          new StringBuilder() ),
        new CommandLineOption(
          "user", BaseMessages.getString( PKG, "Kitchen.CmdLine.RepUsername" ), optionUsername =
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
        new CommandLineOption(
          "custom", BaseMessages.getString( PKG, "Kitchen.ComdLine.Custom" ), customOptions, false ),
        maxLogLinesOption, maxLogTimeoutOption, };

    if ( args.size() == 2 ) { // 2 internal hidden argument (flag and value)
      CommandLineOption.printUsage( options );
      exitJVM( 9 );
    }

    LogChannelInterface log = new LogChannel( STRING_KITCHEN );

    CommandLineOption.parseArguments( args, options, log );

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

    if ( !Utils.isEmpty( optionVersion ) ) {
      BuildVersion buildVersion = BuildVersion.getInstance();
      log.logBasic( BaseMessages.getString(
        PKG, "Kitchen.Log.KettleVersion", buildVersion.getVersion(), buildVersion.getRevision(), buildVersion
          .getBuildDate() ) );
      if ( a.length == 1 ) {
        exitJVM( 6 );
      }
    }

    // Start the action...
    //
    if ( !Utils.isEmpty( optionRepname ) && !Utils.isEmpty( optionUsername ) ) {
      if ( log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString( PKG, "Kitchen.Log.RepUsernameSupplied" ) );
      }
    }

    log.logMinimal( BaseMessages.getString( PKG, "Kitchen.Log.Starting" ) );

    Date start, stop;
    Calendar cal;
    SimpleDateFormat df = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss.SSS" );
    cal = Calendar.getInstance();
    start = cal.getTime();

    if ( log.isDebug() ) {
      log.logDebug( BaseMessages.getString( PKG, "Kitchen.Log.AllocateNewJob" ) );
    }

    JobMeta jobMeta = new JobMeta();
    // In case we use a repository...
    Repository repository = null;

    try {
      // Read kettle job specified on command-line?
      if ( !Utils.isEmpty( optionRepname ) || !Utils.isEmpty( optionFilename ) ) {
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "Kitchen.Log.ParsingCommandLine" ) );
        }
        if ( !Utils.isEmpty( optionRepname ) && !"Y".equalsIgnoreCase( optionNorep.toString() ) ) {
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "Kitchen.Log.LoadingRep" ) );
          }

          RepositoriesMeta repsinfo = new RepositoriesMeta();
          repsinfo.getLog().setLogLevel( log.getLogLevel() );
          try {
            repsinfo.readData();
          } catch ( Exception e ) {
            throw new KettleException( BaseMessages.getString( PKG, "Kitchen.Error.NoRepDefinied" ), e );
          }

          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "Kitchen.Log.FindingRep", "" + optionRepname ) );
          }
          repositoryMeta = repsinfo.findRepository( optionRepname.toString() );
          if ( repositoryMeta != null ) {
            // Define and connect to the repository...
            if ( log.isDebug() ) {
              log.logDebug( BaseMessages.getString( PKG, "Kitchen.Log.Alocate&ConnectRep" ) );
            }

            repository =
              PluginRegistry.getInstance().loadClass(
                RepositoryPluginType.class, repositoryMeta, Repository.class );
            repository.init( repositoryMeta );
            repository.getLog().setLogLevel( log.getLogLevel() );
            repository.connect( optionUsername != null ? optionUsername.toString() : null, optionPassword != null
              ? optionPassword.toString() : null );

            repository.getSecurityProvider().validateAction( RepositoryOperation.EXECUTE_JOB );

            RepositoryDirectoryInterface directory = repository.loadRepositoryDirectoryTree(); // Default = root

            // Add the IMetaStore of the repository to our delegation
            //
            if ( repository.getMetaStore() != null ) {
              metaStore.addMetaStore( repository.getMetaStore() );
            }

            // Find the directory name if one is specified...
            if ( !Utils.isEmpty( optionDirname ) ) {
              directory = directory.findDirectory( optionDirname.toString() );
            }

            if ( directory != null ) {
              // Check username, password
              if ( log.isDebug() ) {
                log.logDebug( BaseMessages.getString( PKG, "Kitchen.Log.CheckUserPass" ) );
              }

              // Load a job
              if ( !Utils.isEmpty( optionJobname ) ) {
                if ( log.isDebug() ) {
                  log.logDebug( BaseMessages.getString( PKG, "Kitchen.Log.LoadingJobInfo" ) );
                }
                blockAndThrow( kettleInitFuture );
                jobMeta = repository.loadJob( optionJobname.toString(), directory, null, null ); // reads last version
                if ( log.isDebug() ) {
                  log.logDebug( BaseMessages.getString( PKG, "Kitchen.Log.AllocateJob" ) );
                }

                job = new Job( repository, jobMeta );
              } else if ( "Y".equalsIgnoreCase( optionListjobs.toString() ) ) {
                // List the jobs in the repository
                if ( log.isDebug() ) {
                  log.logDebug( BaseMessages.getString( PKG, "Kitchen.Log.GettingLostJobsInDirectory", ""
                    + directory ) );
                }

                String[] jobnames = repository.getJobNames( directory.getObjectId(), false );
                for ( int i = 0; i < jobnames.length; i++ ) {
                  System.out.println( jobnames[i] );
                }
              } else if ( "Y".equalsIgnoreCase( optionListdir.toString() ) ) {
                // List the directories in the repository
                String[] dirnames = repository.getDirectoryNames( directory.getObjectId() );
                for ( int i = 0; i < dirnames.length; i++ ) {
                  System.out.println( dirnames[i] );
                }
              }
            } else {
              System.out.println( BaseMessages.getString(
                PKG, "Kitchen.Error.CanNotFindSuppliedDirectory", optionDirname + "" ) );

              repositoryMeta = null;
            }
          } else {
            System.out.println( BaseMessages.getString( PKG, "Kitchen.Error.NoRepProvided" ) );
          }
        }

        // Try to load if from file anyway.
        if ( !Utils.isEmpty( optionFilename ) && job == null ) {
          blockAndThrow( kettleInitFuture );
          String fileName = optionFilename.toString();
          // If the filename starts with scheme like zip:, then isAbsolute() will return false even though
          // the path following the zip is absolute path. Check for isAbsolute only if the fileName does not
          // start with scheme
          if ( !KettleVFS.startsWithScheme( fileName ) && !FileUtil.isFullyQualified( fileName ) ) {
            fileName = initialDir.toString() + fileName;
          }

          jobMeta = new JobMeta( fileName, null, null );
          job = new Job( null, jobMeta );
        }
      } else if ( "Y".equalsIgnoreCase( optionListrep.toString() ) ) {
        RepositoriesMeta ri = new RepositoriesMeta();
        ri.readData();

        System.out.println( BaseMessages.getString( PKG, "Kitchen.Log.ListRep" ) );

        for ( int i = 0; i < ri.nrRepositories(); i++ ) {
          RepositoryMeta rinfo = ri.getRepository( i );
          System.out.println( "#"
            + ( i + 1 ) + " : " + rinfo.getName() + " [" + rinfo.getDescription() + "]  id=" + rinfo.getId() );
        }
      }
    } catch ( KettleException e ) {
      job = null;
      jobMeta = null;
      if ( repository != null ) {
        repository.disconnect();
      }
      System.out.println( BaseMessages.getString( PKG, "Kitchen.Error.StopProcess", e.getMessage() ) );
    }

    if ( job == null ) {
      if ( !"Y".equalsIgnoreCase( optionListjobs.toString() )
        && !"Y".equalsIgnoreCase( optionListdir.toString() ) && !"Y".equalsIgnoreCase( optionListrep.toString() ) ) {
        System.out.println( BaseMessages.getString( PKG, "Kitchen.Error.canNotLoadJob" ) );
      }

      exitJVM( 7 );
    }

    if ( !Utils.isEmpty( optionExport.toString() ) ) {

      try {
        // Export the resources linked to the currently loaded file...
        //
        TopLevelResource topLevelResource =
          ResourceUtil.serializeResourceExportInterface(
            optionExport.toString(), job.getJobMeta(), job, repository, metaStore );
        String launchFile = topLevelResource.getResourceName();
        String message = ResourceUtil.getExplanation( optionExport.toString(), launchFile, job.getJobMeta() );
        System.out.println();
        System.out.println( message );

        // Setting the list parameters option will make kitchen exit below in the parameters section
        //
        optionListParam = new StringBuilder( "Y" );
      } catch ( Exception e ) {
        System.out.println( Const.getStackTracker( e ) );
        exitJVM( 2 );
      }
    }

    Result result = null;

    int returnCode = 0;

    try {
      // Set the command line arguments on the job ...
      //
      if ( args.size() == 0 ) {
        job.setArguments( null );
      } else {
        job.setArguments( args.toArray( new String[args.size()] ) );
      }
      job.initializeVariablesFrom( null );
      job.setLogLevel( log.getLogLevel() );
      job.getJobMeta().setInternalKettleVariables( job );
      job.setRepository( repository );
      job.getJobMeta().setRepository( repository );
      job.getJobMeta().setMetaStore( metaStore );

      // Map the command line named parameters to the actual named parameters. Skip for
      // the moment any extra command line parameter not known in the job.
      String[] jobParams = jobMeta.listParameters();
      for ( String param : jobParams ) {
        String value = optionParams.getParameterValue( param );
        if ( value != null ) {
          job.getJobMeta().setParameterValue( param, value );
        }
      }
      job.copyParametersFrom( job.getJobMeta() );

      // Put the parameters over the already defined variable space. Parameters get priority.
      //
      job.activateParameters();

      // Set custom options in the job extension map as Strings
      //
      for ( String optionName : customOptions.listParameters() ) {
        String optionValue = customOptions.getParameterValue( optionName );
        if ( optionName != null && optionValue != null ) {
          job.getExtensionDataMap().put( optionName, optionValue );
        }
      }

      // List the parameters defined in this job
      // Then simply exit...
      //
      if ( "Y".equalsIgnoreCase( optionListParam.toString() ) ) {
        for ( String parameterName : job.listParameters() ) {
          String value = job.getParameterValue( parameterName );
          String deflt = job.getParameterDefault( parameterName );
          String descr = job.getParameterDescription( parameterName );

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

      job.start();
      job.waitUntilFinished();
      result = job.getResult(); // Execute the selected job.
    } finally {
      if ( repository != null ) {
        repository.disconnect();
      }
    }

    log.logMinimal( BaseMessages.getString( PKG, "Kitchen.Log.Finished" ) );

    if ( result != null && result.getNrErrors() != 0 ) {
      log.logError( BaseMessages.getString( PKG, "Kitchen.Error.FinishedWithErrors" ) );
      returnCode = 1;
    }
    cal = Calendar.getInstance();
    stop = cal.getTime();
    String begin = df.format( start ).toString();
    String end = df.format( stop ).toString();

    log.logMinimal( BaseMessages.getString( PKG, "Kitchen.Log.StartStop", begin, end ) );

    long seconds = ( stop.getTime() - start.getTime() ) / 1000;
    if ( seconds <= 60 ) {
      log.logMinimal( BaseMessages.getString( PKG, "Kitchen.Log.ProcessEndAfter", String.valueOf( seconds ) ) );
    } else if ( seconds <= 60 * 60 ) {
      int min = (int) ( seconds / 60 );
      int rem = (int) ( seconds % 60 );
      log.logMinimal( BaseMessages.getString(
        PKG, "Kitchen.Log.ProcessEndAfterLong", String.valueOf( min ), String.valueOf( rem ), String
          .valueOf( seconds ) ) );
    } else if ( seconds <= 60 * 60 * 24 ) {
      int rem;
      int hour = (int) ( seconds / ( 60 * 60 ) );
      rem = (int) ( seconds % ( 60 * 60 ) );
      int min = rem / 60;
      rem = rem % 60;
      log.logMinimal( BaseMessages.getString(
        PKG, "Kitchen.Log.ProcessEndAfterLonger", String.valueOf( hour ), String.valueOf( min ), String
          .valueOf( rem ), String.valueOf( seconds ) ) );
    } else {
      int rem;
      int days = (int) ( seconds / ( 60 * 60 * 24 ) );
      rem = (int) ( seconds % ( 60 * 60 * 24 ) );
      int hour = rem / ( 60 * 60 );
      rem = rem % ( 60 * 60 );
      int min = rem / 60;
      rem = rem % 60;
      log.logMinimal( BaseMessages.getString(
        PKG, "Kitchen.Log.ProcessEndAfterLongest", String.valueOf( days ), String.valueOf( hour ), String
          .valueOf( min ), String.valueOf( rem ), String.valueOf( seconds ) ) );
    }

    if ( fileAppender != null ) {
      fileAppender.close();
      KettleLogStore.getAppender().removeLoggingEventListener( fileAppender );
    }

    exitJVM( returnCode );

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

  private static final void exitJVM( int status ) {

    System.exit( status );
  }
}
