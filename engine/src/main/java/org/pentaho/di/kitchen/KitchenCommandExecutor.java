/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.base.AbstractBaseCommandExecutor;
import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.base.Params;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.util.FileUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.resource.ResourceUtil;
import org.pentaho.di.resource.TopLevelResource;

import java.io.File;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class KitchenCommandExecutor extends AbstractBaseCommandExecutor {

  Future<KettleException> kettleInit;

  public KitchenCommandExecutor( Class<?> pkgClazz ) {
    this( pkgClazz, new LogChannel( Kitchen.STRING_KITCHEN ), null );
  }

  public KitchenCommandExecutor( Class<?> pkgClazz, LogChannelInterface log ) {
    this( pkgClazz, log, null );
  }

  public KitchenCommandExecutor( Class<?> pkgClazz, LogChannelInterface log, Future<KettleException> kettleInit ) {
    setPkgClazz( pkgClazz );
    setLog( log );
    setKettleInit( kettleInit );
  }

  public Result execute( final Params params ) throws Throwable {
    return execute( params, null );
  }

  public Result execute( Params params, String[] arguments ) throws Throwable {

    getLog().logMinimal( BaseMessages.getString( getPkgClazz(), "Kitchen.Log.Starting" ) );

    logDebug( "Kitchen.Log.AllocateNewJob" );

    Job job = null;

    // In case we use a repository...
    Repository repository = null;

    try {
      if ( getMetaStore() == null ) {
        setMetaStore( MetaStoreConst.getDefaultMetastore() );
      }

      ConnectionManager.getInstance().setMetastoreSupplier( MetaStoreConst.getDefaultMetastoreSupplier() );

      // Read kettle job specified on command-line?
      if ( !Utils.isEmpty( params.getRepoName() ) || !Utils.isEmpty( params.getLocalFile() ) ) {

        logDebug( "Kitchen.Log.ParsingCommandLine" );

        if ( !Utils.isEmpty( params.getRepoName() ) && !isEnabled( params.getBlockRepoConns() ) ) {

          /**
           * if set, _trust_user_ needs to be considered. See pur-plugin's:
           *
           * @link https://github.com/pentaho/pentaho-kettle/blob/8.0.0.0-R/plugins/pur/core/src/main/java/org/pentaho/di/repository/pur/PurRepositoryConnector.java#L97-L101
           * @link https://github.com/pentaho/pentaho-kettle/blob/8.0.0.0-R/plugins/pur/core/src/main/java/org/pentaho/di/repository/pur/WebServiceManager.java#L130-L133
           */
          if ( isEnabled( params.getTrustRepoUser() ) ) {
            System.setProperty( "pentaho.repository.client.attemptTrust", YES );
          }

          // In case we use a repository...
          // some commands are to load a Trans from the repo; others are merely to print some repo-related information
          RepositoryMeta repositoryMeta = loadRepositoryConnection( params.getRepoName(), "Kitchen.Log.LoadingRep", "Kitchen.Error.NoRepDefinied", "Kitchen.Log.FindingRep" );

          if ( repositoryMeta == null ) {
            System.out.println( BaseMessages.getString( getPkgClazz(), "Kitchen.Error.CanNotConnectRep" ) );
            return exitWithStatus( CommandExecutorCodes.Kitchen.COULD_NOT_LOAD_JOB.getCode() );
          }

          logDebug( "Kitchen.Log.CheckUserPass" );
          repository = establishRepositoryConnection( repositoryMeta, params.getRepoUsername(), params.getRepoPassword(), RepositoryOperation.EXECUTE_JOB );

          // Is the command a request to output some repo-related information ( list directories, export repo content, ... ) ?
          // If so, nothing else is needed ( other than executing the actual requested operation )
          if ( isEnabled( params.getListRepoFiles() ) || isEnabled( params.getListRepoDirs() ) ) {
            executeRepositoryBasedCommand( repository, params.getInputDir(), params.getListRepoFiles(), params.getListRepoDirs() );
            return exitWithStatus( CommandExecutorCodes.Kitchen.SUCCESS.getCode() );
          }

          job = loadJobFromRepository( repository, params.getInputDir(), params.getInputFile() );
        }

        // Try to load if from file
        if ( job == null ) {

          // Try to load the job from file, even if it failed to load from the repository
          job = loadJobFromFilesystem( params.getLocalInitialDir(), params.getLocalFile(), params.getBase64Zip() );
        }

      } else if ( isEnabled( params.getListRepos() ) ) {

        printRepositories( loadRepositoryInfo( "Kitchen.Log.ListRep", "Kitchen.Error.NoRepDefinied" ) ); // list the repositories placed at repositories.xml

      }
    } catch ( KettleException e ) {
      job = null;
      if ( repository != null ) {
        repository.disconnect();
      }
      System.out.println( BaseMessages.getString( getPkgClazz(), "Kitchen.Error.StopProcess", e.getMessage() ) );
    }

    if ( job == null ) {
      if ( !isEnabled( params.getListRepoFiles() ) && !isEnabled( params.getListRepoDirs() ) && !isEnabled( params.getListRepos() ) ) {
        System.out.println( BaseMessages.getString( getPkgClazz(), "Kitchen.Error.canNotLoadJob" ) );
      }

      return exitWithStatus( CommandExecutorCodes.Kitchen.COULD_NOT_LOAD_JOB.getCode(), job );
    }

    if ( !Utils.isEmpty( params.getExportRepo() ) ) {

      try {
        // Export the resources linked to the currently loaded file...
        TopLevelResource topLevelResource = ResourceUtil.serializeResourceExportInterface( params.getExportRepo(), job.getJobMeta(), job, repository, getMetaStore() );
        String launchFile = topLevelResource.getResourceName();
        String message = ResourceUtil.getExplanation( params.getExportRepo(), launchFile, job.getJobMeta() );
        System.out.println();
        System.out.println( message );

        // Setting the list parameters option will make kitchen exit below in the parameters section
        ( params ).setListFileParams( YES );
      } catch ( Exception e ) {
        System.out.println( Const.getStackTracker( e ) );
        return exitWithStatus( CommandExecutorCodes.Kitchen.UNEXPECTED_ERROR.getCode() );
      }
    }

    Date start = Calendar.getInstance().getTime();

    try {

      // Set the command line arguments on the job ...
      job.setArguments( arguments );
      job.initializeVariablesFrom( null );
      job.setLogLevel( getLog().getLogLevel() );
      job.getJobMeta().setInternalKettleVariables( job );
      job.setRepository( repository );
      job.getJobMeta().setRepository( repository );
      job.getJobMeta().setMetaStore( getMetaStore() );

      // Map the command line named parameters to the actual named parameters. Skip for
      // the moment any extra command line parameter not known in the job.
      String[] jobParams = job.getJobMeta().listParameters();
      for ( String param : jobParams ) {
        try {
          String value = params.getNamedParams().getParameterValue( param );
          if ( value != null ) {
            job.getJobMeta().setParameterValue( param, value );
          }
        } catch ( UnknownParamException e ) {
          /* no-op */
        }
      }
      job.copyParametersFrom( job.getJobMeta() );

      // Put the parameters over the already defined variable space. Parameters get priority.
      job.activateParameters();

      // Set custom options in the job extension map as Strings
      for ( String optionName : params.getCustomNamedParams().listParameters() ) {
        try {
          String optionValue = params.getCustomNamedParams().getParameterValue( optionName );
          if ( optionName != null && optionValue != null ) {
            job.getExtensionDataMap().put( optionName, optionValue );
          }
        } catch ( UnknownParamException e ) {
          /* no-op */
        }
      }

      // List the parameters defined in this job, then simply exit...
      if ( isEnabled( params.getListFileParams() ) ) {

        printJobParameters( job );

        // stop right here...
        return exitWithStatus( CommandExecutorCodes.Kitchen.COULD_NOT_LOAD_JOB.getCode() ); // same as the other list options
      }

      job.start(); // Execute the selected job.
      job.waitUntilFinished();
      setResult( job.getResult() ); // get the execution result
    } finally {
      if ( repository != null ) {
        repository.disconnect();
      }
      if ( isEnabled( params.getTrustRepoUser() ) ) {
        System.clearProperty( "pentaho.repository.client.attemptTrust" ); // we set it, now we sanitize it
      }
    }

    getLog().logMinimal( BaseMessages.getString( getPkgClazz(), "Kitchen.Log.Finished" ) );

    int returnCode = getReturnCode();

    Date stop = Calendar.getInstance().getTime();

    calculateAndPrintElapsedTime( start, stop, "Kitchen.Log.StartStop", "Kitchen.Log.ProcessEndAfter", "Kitchen.Log.ProcessEndAfterLong",
            "Kitchen.Log.ProcessEndAfterLonger", "Kitchen.Log.ProcessEndAfterLongest" );
    getResult().setElapsedTimeMillis( stop.getTime() - start.getTime() );

    return exitWithStatus( returnCode );
  }

  protected Result exitWithStatus( final int exitStatus, Job job ) {
    try {
      ExtensionPointHandler.callExtensionPoint( getLog(), KettleExtensionPoint.JobFinish.id, job );
    } catch ( KettleException e ) {
      getLog().logError( "A KettleException occurred when attempting to call JobFinish extension point", e );
    }
    return exitWithStatus( exitStatus );
  }

  public int printVersion() {
    printVersion( "Kitchen.Log.KettleVersion" );
    return CommandExecutorCodes.Kitchen.KETTLE_VERSION_PRINT.getCode();
  }

  protected void executeRepositoryBasedCommand( Repository repository, final String dirName, final String listJobs, final String listDirs ) throws Exception {

    RepositoryDirectoryInterface directory = loadRepositoryDirectory( repository, dirName, "Kitchen.Error.NoRepProvided",
            "Kitchen.Log.Alocate&ConnectRep", "Kitchen.Error.CanNotFindSuppliedDirectory" );

    if ( directory == null ) {
      return; // not much we can do here
    }

    if ( isEnabled( listJobs ) ) {
      printRepositoryStoredJobs( repository, directory ); // List the jobs in the repository

    } else if ( isEnabled( listDirs ) ) {
      printRepositoryDirectories( repository, directory ); // List the directories in the repository
    }
  }

  public Job loadJobFromRepository( Repository repository, String dirName, String jobName ) throws Exception {

    if ( Utils.isEmpty( jobName ) ) {
      System.out.println( BaseMessages.getString( getPkgClazz(), "Kitchen.Error.canNotLoadJob" ) );
      return null;
    }

    RepositoryDirectoryInterface directory = loadRepositoryDirectory( repository, dirName, "Kitchen.Error.NoRepProvided",
            "Kitchen.Log.Alocate&ConnectRep", "Kitchen.Error.CanNotFindSuppliedDirectory" );

    if ( directory == null ) {
      return null; // not much we can do here
    }

    // Load a job
    logDebug(  "Kitchen.Log.LoadingJobInfo" );
    blockAndThrow( getKettleInit() );
    JobMeta jobMeta = repository.loadJob( jobName, directory, null, null ); // reads last version
    logDebug(  "Kitchen.Log.AllocateJob" );

    return new Job( repository, jobMeta );
  }

  public Job loadJobFromFilesystem( String initialDir, String filename, Serializable base64Zip ) throws Exception {

    if ( Utils.isEmpty( filename ) ) {
      System.out.println( BaseMessages.getString( getPkgClazz(), "Kitchen.Error.canNotLoadJob" ) );
      return null;
    }

    File zip;
    if ( base64Zip != null && ( zip = decodeBase64ToZipFile( base64Zip, true ) ) != null ) {
      // update filename to a meaningful, 'ETL-file-within-zip' syntax
      filename = "zip:file:" + File.separator + File.separator + zip.getAbsolutePath() + "!" + filename;
    }

    blockAndThrow( getKettleInit() );
    String fileName = filename;
    // If the filename starts with scheme like zip:, then isAbsolute() will return false even though
    // the path following the zip is absolute path. Check for isAbsolute only if the fileName does not start with scheme
    if ( !KettleVFS.startsWithScheme( fileName ) && !FileUtil.isFullyQualified( fileName ) ) {
      fileName = initialDir + fileName;
    }

    JobMeta jobMeta = new JobMeta( fileName, null, null );
    return new Job( null, jobMeta );
  }

  protected void printJobParameters( Job job ) throws UnknownParamException {

    if ( job != null && job.listParameters() != null ) {

      for ( String pName : job.listParameters() ) {
        printParameter( pName, job.getParameterValue( pName ), job.getParameterDefault( pName ), job.getParameterDescription( pName ) );
      }
    }
  }

  protected void printRepositoryStoredJobs( Repository repository, RepositoryDirectoryInterface directory ) throws KettleException {

    logDebug( "Kitchen.Log.GettingLostJobsInDirectory", "" + directory );

    String[] jobnames = repository.getJobNames( directory.getObjectId(), false );
    for ( int i = 0; i < jobnames.length; i++ ) {
      System.out.println( jobnames[i] );
    }
  }

  protected void printRepositories( RepositoriesMeta repositoriesMeta ) {

    if ( repositoriesMeta != null ) {

      System.out.println( BaseMessages.getString( getPkgClazz(), "Kitchen.Log.ListRep" ) );

      for ( int i = 0; i < repositoriesMeta.nrRepositories(); i++ ) {
        RepositoryMeta rinfo = repositoriesMeta.getRepository( i );
        System.out.println( "#" + ( i + 1 ) + " : " + rinfo.getName() + " [" + rinfo.getDescription() + "]  id=" + rinfo.getId() );
      }
    }
  }

  private <T extends Throwable> void blockAndThrow( Future<T> future ) throws T {

    if ( future == null ) {
      return;
    }

    try {
      T e = future.get();
      if ( e != null ) {
        throw e;
      }
    } catch ( InterruptedException | ExecutionException e  ) {
      throw new RuntimeException( e );
    }
  }

  public Future<KettleException> getKettleInit() {
    return kettleInit;
  }

  public void setKettleInit( Future<KettleException> kettleInit ) {
    this.kettleInit = kettleInit;
  }

  @VisibleForTesting
  int getReturnCode() {

    int successCode = CommandExecutorCodes.Kitchen.SUCCESS.getCode();

    if ( getResult().getNrErrors() != 0 ) {
      getLog().logError( BaseMessages.getString( getPkgClazz(), "Kitchen.Error.FinishedWithErrors" ) );
      return CommandExecutorCodes.Kitchen.ERRORS_DURING_PROCESSING.getCode();
    }

    return getResult().getResult() ? successCode : CommandExecutorCodes.Kitchen.ERRORS_DURING_PROCESSING.getCode();

  }

}
