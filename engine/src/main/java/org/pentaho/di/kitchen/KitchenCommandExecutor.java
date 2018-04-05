/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.base.AbstractBaseCommandExecutor;
import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.util.FileUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.resource.ResourceUtil;
import org.pentaho.di.resource.TopLevelResource;
import org.pentaho.di.i18n.BaseMessages;

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

  public int execute( String repoName, String noRepo, String username, String trustUser, String password, String dirName, String filename,
                      String jobName, String listJobs, String listDirs, String exportRepo, String initialDir,
                      String listRepos, String listParams, NamedParams params, NamedParams customParams, String[] arguments ) throws Throwable {

    getLog().logMinimal( BaseMessages.getString( getPkgClazz(), "Kitchen.Log.Starting" ) );

    Date start = Calendar.getInstance().getTime();

    logDebug( "Kitchen.Log.AllocateNewJob" );

    Job job = null;

    // In case we use a repository...
    Repository repository = null;

    try {

      if ( getMetaStore() == null ) {
        setMetaStore( createDefaultMetastore() );
      }

      // Read kettle job specified on command-line?
      if ( !Utils.isEmpty( repoName ) || !Utils.isEmpty( filename ) ) {

        logDebug( "Kitchen.Log.ParsingCommandLine" );

        if ( !Utils.isEmpty( repoName ) && !isEnabled( noRepo ) ) {

          /**
           * if set, _trust_user_ needs to be considered. See pur-plugin's:
           *
           * @link https://github.com/pentaho/pentaho-kettle/blob/8.0.0.0-R/plugins/pur/core/src/main/java/org/pentaho/di/repository/pur/PurRepositoryConnector.java#L97-L101
           * @link https://github.com/pentaho/pentaho-kettle/blob/8.0.0.0-R/plugins/pur/core/src/main/java/org/pentaho/di/repository/pur/WebServiceManager.java#L130-L133
           */
          if ( isEnabled( trustUser ) ) {
            System.setProperty( "pentaho.repository.client.attemptTrust", YES );
          }

          // In case we use a repository...
          // some commands are to load a Trans from the repo; others are merely to print some repo-related information
          RepositoryMeta repositoryMeta = loadRepositoryConnection( repoName, "Kitchen.Log.LoadingRep", "Kitchen.Error.NoRepDefinied", "Kitchen.Log.FindingRep" );

          repository = establishRepositoryConnection( repositoryMeta, username, password, RepositoryOperation.EXECUTE_JOB );

          job = executeRepositoryBasedCommand( repository, repositoryMeta, dirName, jobName, listJobs, listDirs );
        }

        // Try to load if from file anyway.
        if ( !Utils.isEmpty( filename ) && job == null ) {

          // Try to load the job from file, even if it failed to load from the repository
          job = executeFilesystemBasedCommand( initialDir, filename );
        }

      } else if ( isEnabled( listRepos ) ) {

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
      if ( !isEnabled( listJobs ) && !isEnabled( listDirs ) && !isEnabled( listRepos ) ) {
        System.out.println( BaseMessages.getString( getPkgClazz(), "Kitchen.Error.canNotLoadJob" ) );
      }

      return CommandExecutorCodes.Kitchen.COULD_NOT_LOAD_JOB.getCode();
    }

    if ( !Utils.isEmpty( exportRepo ) ) {

      try {
        // Export the resources linked to the currently loaded file...
        TopLevelResource topLevelResource = ResourceUtil.serializeResourceExportInterface( exportRepo, job.getJobMeta(), job, repository, getMetaStore() );
        String launchFile = topLevelResource.getResourceName();
        String message = ResourceUtil.getExplanation( exportRepo, launchFile, job.getJobMeta() );
        System.out.println();
        System.out.println( message );

        // Setting the list parameters option will make kitchen exit below in the parameters section
        listParams = YES;
      } catch ( Exception e ) {
        System.out.println( Const.getStackTracker( e ) );
        return CommandExecutorCodes.Kitchen.UNEXPECTED_ERROR.getCode();
      }
    }

    Result result = null;

    int returnCode = CommandExecutorCodes.Kitchen.SUCCESS.getCode();

    try {

      // Set the command line arguments on the job ...
      job.setArguments( arguments != null ? arguments : null );
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
        String value = params.getParameterValue( param );
        if ( value != null ) {
          job.getJobMeta().setParameterValue( param, value );
        }
      }
      job.copyParametersFrom( job.getJobMeta() );

      // Put the parameters over the already defined variable space. Parameters get priority.
      job.activateParameters();

      // Set custom options in the job extension map as Strings
      for ( String optionName : customParams.listParameters() ) {
        String optionValue = customParams.getParameterValue( optionName );
        if ( optionName != null && optionValue != null ) {
          job.getExtensionDataMap().put( optionName, optionValue );
        }
      }

      // List the parameters defined in this job, then simply exit...
      if ( isEnabled( listParams ) ) {

        printJobParameters( job );

        // stop right here...
        return CommandExecutorCodes.Kitchen.COULD_NOT_LOAD_JOB.getCode(); // same as the other list options
      }

      job.start();
      job.waitUntilFinished();
      result = job.getResult(); // Execute the selected job.
    } finally {
      if ( repository != null ) {
        repository.disconnect();
      }
      if ( isEnabled( trustUser ) ) {
        System.clearProperty( "pentaho.repository.client.attemptTrust" ); // we set it, now we sanitize it
      }
    }

    getLog().logMinimal( BaseMessages.getString( getPkgClazz(), "Kitchen.Log.Finished" ) );

    if ( result != null && result.getNrErrors() != 0 ) {
      getLog().logError( BaseMessages.getString( getPkgClazz(), "Kitchen.Error.FinishedWithErrors" ) );
      returnCode = CommandExecutorCodes.Kitchen.ERRORS_DURING_PROCESSING.getCode();
    }

    Date stop = Calendar.getInstance().getTime();

    calculateAndPrintElapsedTime( start, stop, "Kitchen.Log.StartStop", "Kitchen.Log.ProcessEndAfter", "Kitchen.Log.ProcessEndAfterLong",
            "Kitchen.Log.ProcessEndAfterLonger", "Kitchen.Log.ProcessEndAfterLongest" );

    return returnCode;
  }

  public int printVersion() {
    printVersion( "Kitchen.Log.KettleVersion" );
    return CommandExecutorCodes.Kitchen.KETTLE_VERSION_PRINT.getCode();
  }

  public Job executeRepositoryBasedCommand( Repository repository, RepositoryMeta repositoryMeta, final String dirName,
                                            final String jobName, final String listJobs, final String listDirs ) throws Exception {

    if ( repository != null && repositoryMeta != null  ) {
      // Define and connect to the repository...
      logDebug( "Kitchen.Log.Alocate&ConnectRep" );

      RepositoryDirectoryInterface directory = repository.loadRepositoryDirectoryTree(); // Default = root

      // Add the IMetaStore of the repository to our delegation
      if ( repository.getMetaStore() != null && getMetaStore() != null ) {
        getMetaStore().addMetaStore( repository.getMetaStore() );
      }

      // Find the directory name if one is specified...
      if ( !Utils.isEmpty( dirName ) ) {
        directory = directory.findDirectory( dirName );
      }

      if ( directory != null ) {

        // Check username, password
        logDebug( "Kitchen.Log.CheckUserPass" );

        // Load a job
        if ( !Utils.isEmpty( jobName ) ) {

          logDebug(  "Kitchen.Log.LoadingJobInfo" );
          blockAndThrow( getKettleInit() );
          JobMeta jobMeta = repository.loadJob( jobName, directory, null, null ); // reads last version
          logDebug(  "Kitchen.Log.AllocateJob" );

          return new Job( repository, jobMeta );

        } else if ( isEnabled( listJobs ) ) {

          printRepositoryStoredJobs( repository, directory ); // List the jobs in the repository

        } else if ( isEnabled( listDirs ) ) {

          printRepositoryDirectories( repository, directory ); // List the directories in the repository
        }

      } else {
        System.out.println( BaseMessages.getString( getPkgClazz(), "Kitchen.Error.CanNotFindSuppliedDirectory", dirName + "" ) );
        repositoryMeta = null;
      }

    } else {
      System.out.println( BaseMessages.getString( getPkgClazz(), "Kitchen.Error.NoRepProvided" ) );
    }

    return null;
  }

  public Job executeFilesystemBasedCommand( final String initialDir, final String filename ) throws Exception {

    if ( Utils.isEmpty( filename ) ) {
      return null;
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
}
