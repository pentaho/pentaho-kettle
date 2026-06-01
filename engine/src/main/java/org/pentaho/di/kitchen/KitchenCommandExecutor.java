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
import org.pentaho.di.base.AbstractBaseCommandExecutor;
import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.base.Params;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.util.FileUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.pan.CommandExecutorResult;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.resource.ResourceUtil;
import org.pentaho.di.resource.TopLevelResource;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class KitchenCommandExecutor extends AbstractBaseCommandExecutor {

  private static final String COULD_NOT_LOAD_JOB_KEY = "Kitchen.Error.canNotLoadJob";
  private static final String KETTLE_INIT_INTERRUPTED = "Kitchen initialization interrupted";
  private static final String KETTLE_INIT_FAILED = "Kitchen initialization failed";

  private Future<KettleException> kettleInit;

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

  public Result execute( final Params params ) throws KettleException {
    return execute( params, null );
  }

  public Result execute( Params params, String[] arguments ) throws KettleException {
    getLog().logMinimal( BaseMessages.getString( getPkgClazz(), "Kitchen.Log.Starting" ) );
    logDebug( "Kitchen.Log.AllocateNewJob" );

    JobLoadOutcome loadOutcome = loadJobOutcome( params );
    Repository repository = loadOutcome.repository();

    try {
      if ( loadOutcome.shouldExit() ) {
        return exitWithStatus( loadOutcome.exitCode(), loadOutcome.job() );
      }

      Job job = loadOutcome.job();
      if ( job == null ) {
        return handleMissingJob( params );
      }

      Result exportResult = exportJobIfRequested( params, job, repository );
      if ( exportResult != null ) {
        return exportResult;
      }

      Date start = Calendar.getInstance().getTime();
      Result parameterResult = prepareJobExecution( job, repository, params, arguments );
      if ( parameterResult != null ) {
        return parameterResult;
      }

      startJob( job );
      return finishExecution( start );
    } finally {
      cleanupRepositorySession( repository, params );
    }
  }

  private JobLoadOutcome loadJobOutcome( Params params ) throws KettleException {
    initializeExecutionContext();
    if ( hasRequestedJobSource( params ) ) {
      return loadRequestedJob( params );
    }
    if ( isEnabled( params.getListRepos() ) ) {
      // list the repositories placed at repositories.xml
      printRepositories( loadRepositoryInfo( "Kitchen.Log.ListRep", "Kitchen.Error.NoRepDefinied" ) );
    }
    return JobLoadOutcome.continueWith( null, null );
  }

  private void initializeExecutionContext() {
    if ( getMetaStore() == null ) {
      setMetaStore( MetaStoreConst.getDefaultMetastore() );
    }
    ConnectionManager.getInstance().setMetastoreSupplier( MetaStoreConst.getDefaultMetastoreSupplier() );
  }

  private boolean hasRequestedJobSource( Params params ) {
    return !Utils.isEmpty( params.getRepoName() ) || !Utils.isEmpty( params.getLocalFile() );
  }

  private JobLoadOutcome loadRequestedJob( Params params ) throws KettleException {
    logDebug( "Kitchen.Log.ParsingCommandLine" );

    JobLoadOutcome repositoryOutcome = shouldUseRepository( params )
      ? loadRepositoryJob( params )
      : JobLoadOutcome.continueWith( null, null );
    if ( repositoryOutcome.shouldExit() ) {
      return repositoryOutcome;
    }

    Repository repository = repositoryOutcome.repository();
    Job job = repositoryOutcome.job();
    if ( repository == null ) {
      Integer validationExitCode = validatePluginContext( params, null );
      if ( validationExitCode != null ) {
        return JobLoadOutcome.exit( validationExitCode );
      }
    }

    if ( job == null ) {
      // Keep CLI behavior consistent with repository load failures: log and return the standard load-error status.
      try {
        job = loadJobFromFilesystem( params.getLocalInitialDir(), params.getLocalFile(), params.getBase64Zip() );
      } catch ( KettleException e ) {
        logStopProcess( e );
      }
    }
    return JobLoadOutcome.continueWith( job, repository );
  }

  private boolean shouldUseRepository( Params params ) {
    return !Utils.isEmpty( params.getRepoName() ) && !isEnabled( params.getBlockRepoConns() );
  }

  private JobLoadOutcome loadRepositoryJob( Params params ) {
    Repository repository = null;
    try {
      applyTrustedUserSetting( params );
      RepositoryMeta repositoryMeta = loadRepositoryMeta( params );
      if ( repositoryMeta == null ) {
        return JobLoadOutcome.exit( CommandExecutorCodes.Kitchen.COULD_NOT_LOAD_JOB.getCode() );
      }

      repository = establishRepositorySession( params, repositoryMeta );
      // Is the command a request to output some repo-related information ( list
      // directories, export repo
      // content, ... ) ?
      // If so, nothing else is needed ( other than executing the actual requested
      // operation )
      if ( isRepositoryListCommand( params ) ) {
        executeRepositoryBasedCommand( repository, params.getInputDir(), params.getListRepoFiles(),
          params.getListRepoDirs() );
        disconnectRepository( repository );
        return JobLoadOutcome.exit( CommandExecutorCodes.Kitchen.SUCCESS.getCode() );
      }

      // Validate PluginNamedParams
      Integer validationExitCode = validatePluginContext( params, repository );
      if ( validationExitCode != null ) {
        disconnectRepository( repository );
        return JobLoadOutcome.exit( validationExitCode );
      }

      Job job = loadJobFromRepository( repository, params.getInputDir(), params.getInputFile() );
      return JobLoadOutcome.continueWith( job, repository );
    } catch ( KettleException e ) {
      disconnectRepository( repository );
      logStopProcess( e );
      return JobLoadOutcome.continueWith( null, null );
    }
  }

  @VisibleForTesting
  void applyTrustedUserSetting( Params params ) {
    /*
     * if set, _trust_user_ needs to be considered. See pur-plugin's:
     *
     * @link https://github.com/pentaho/pentaho-kettle/blob/8.0.0
     *       .0-R/plugins/pur/core/src/main/java/org/pentaho/di/repository/pur/PurRepositoryConnector.java#L97-L101
     * @link https://github.com/pentaho/pentaho-kettle/blob/8.0.0
     *       .0-R/plugins/pur/core/src/main/java/org/pentaho/di/repository/pur/WebServiceManager.java#L130-L133
     */
    if ( isEnabled( params.getTrustRepoUser() ) ) {
      System.setProperty( "pentaho.repository.client.attemptTrust", YES );
    }
  }

  private RepositoryMeta loadRepositoryMeta( Params params ) throws KettleException {
    RepositoryMeta repositoryMeta = loadRepositoryConnection( params.getRepoName(),
      "Kitchen.Log.LoadingRep", "Kitchen.Error.NoRepDefinied", "Kitchen.Log.FindingRep" );
    if ( repositoryMeta == null ) {
      getLog().logError( BaseMessages.getString( getPkgClazz(), "Kitchen.Error.CanNotConnectRep" ) );
    }
    return repositoryMeta;
  }

  private Repository establishRepositorySession( Params params, RepositoryMeta repositoryMeta ) throws KettleException {
    logDebug( "Kitchen.Log.CheckUserPass" );
    boolean useServiceAccount = isEnabled( params.getServiceAccount() );
    boolean useBrowserAuth = shouldUseBrowserAuth( params, useServiceAccount );
    boolean useDeviceCode = shouldUseDeviceCode( params, useServiceAccount );
    return establishRepositoryConnectionWithBrowserAuth( repositoryMeta, params.getRepoUsername(),
      params.getRepoPassword(), useBrowserAuth, useDeviceCode, useServiceAccount,
      params.getPreferredIdp(), RepositoryOperation.EXECUTE_JOB );
  }

  private boolean shouldUseBrowserAuth( Params params, boolean useServiceAccount ) {
    if ( useServiceAccount ) {
      return false;
    }
    if ( !Utils.isEmpty( params.getRepoPassword() ) && !isEnabled( params.getBrowserAuth() ) ) {
      return false;
    }
    return Utils.isEmpty( params.getRepoPassword() ) || isEnabled( params.getBrowserAuth() );
  }

  private boolean shouldUseDeviceCode( Params params, boolean useServiceAccount ) {
    if ( useServiceAccount ) {
      return false;
    }
    return isEnabled( params.getDeviceCode() ) && isEnabled( params.getBrowserAuth() );
  }

  private boolean isRepositoryListCommand( Params params ) {
    return isEnabled( params.getListRepoFiles() ) || isEnabled( params.getListRepoDirs() );
  }

  private Integer validatePluginContext( Params params, Repository repository ) throws KettleException {
    blockAndThrow( getKettleInit() );
    CommandExecutorResult result = validateAndSetPluginContext( getLog(), params, repository );
    if ( result != null && result.getCode() != 0 ) {
      return result.getCode();
    }
    return null;
  }

  private void logStopProcess( KettleException e ) {
    getLog().logError( BaseMessages.getString( getPkgClazz(), "Kitchen.Error.StopProcess", e.getMessage() ), e );
  }

  private Result handleMissingJob( Params params ) {
    if ( !isEnabled( params.getListRepoFiles() ) && !isEnabled( params.getListRepoDirs() )
      && !isEnabled( params.getListRepos() ) ) {
      getLog().logError( BaseMessages.getString( getPkgClazz(), COULD_NOT_LOAD_JOB_KEY ) );
    }
    return exitWithStatus( CommandExecutorCodes.Kitchen.COULD_NOT_LOAD_JOB.getCode(), null );
  }

  private Result exportJobIfRequested( Params params, Job job, Repository repository ) {
    if ( Utils.isEmpty( params.getExportRepo() ) ) {
      return null;
    }
    // For export specifically, initialize props to honor "only used db connections"
    // setting
    Props.init( Props.TYPE_PROPERTIES_SPOON );
    try {
      // Export the resources linked to the currently loaded file...
      TopLevelResource topLevelResource = ResourceUtil.serializeResourceExportInterface( getBowl(),
        repository == null ? DefaultBowl.getInstance() : repository.getBowl(), params.getExportRepo(),
        job.getJobMeta(), job, repository, getMetaStore() );
      String launchFile = topLevelResource.getResourceName();
      String message = ResourceUtil.getExplanation( params.getExportRepo(), launchFile, job.getJobMeta() );
      getLog().logMinimal( "" );
      getLog().logMinimal( message );

      // Setting the list parameters option will make kitchen exit below in the
      // parameters section
      params.setListFileParams( YES );
      return null;
    } catch ( Exception e ) {
      getLog().logError( Const.getStackTracker( e ) );
      return exitWithStatus( CommandExecutorCodes.Kitchen.UNEXPECTED_ERROR.getCode() );
    }
  }

  private Result prepareJobExecution( Job job, Repository repository, Params params, String[] arguments )
    throws KettleException {
    configureJob( job, repository, arguments );
    applyNamedParameters( job, params );
    job.copyParametersFrom( job.getJobMeta() );

    // Put the parameters over the already defined variable space. Parameters get
    // priority.
    job.activateParameters();
    applyCustomNamedParameters( job, params );
    if ( isEnabled( params.getListFileParams() ) ) {
      try {
        printJobParameters( job );
      } catch ( UnknownParamException e ) {
        throw new KettleException( "Unable to list job parameters", e );
      }
      return exitWithStatus( CommandExecutorCodes.Kitchen.COULD_NOT_LOAD_JOB.getCode() );
    }
    return null;
  }

  private void configureJob( Job job, Repository repository, String[] arguments ) {
    // Set the command line arguments on the job ...
    job.setArguments( arguments );
    job.initializeVariablesFrom( getBowl().getADefaultVariableSpace() );
    job.setLogLevel( getLog().getLogLevel() );
    job.getJobMeta().setInternalKettleVariables( job );
    job.setRepository( repository );
    job.getJobMeta().setRepository( repository );
    job.getJobMeta().setMetaStore( getMetaStore() );
  }

  private void applyNamedParameters( Job job, Params params ) {
    // Map the command line named parameters to the actual named parameters. Skip
    // for the moment any extra command line parameter not known in the job.
    for ( String param : job.getJobMeta().listParameters() ) {
      try {
        String value = params.getNamedParams().getParameterValue( param );
        if ( value != null ) {
          job.getJobMeta().setParameterValue( param, value );
        }
      } catch ( UnknownParamException e ) {
        getLog().logDebug( "Ignoring unknown job parameter: " + param, e );
      }
    }
  }

  private void applyCustomNamedParameters( Job job, Params params ) {
    // Set custom options in the job extension map as Strings
    for ( String optionName : params.getCustomNamedParams().listParameters() ) {
      try {
        String optionValue = params.getCustomNamedParams().getParameterValue( optionName );
        if ( optionName != null && optionValue != null ) {
          job.getExtensionDataMap().put( optionName, optionValue );
        }
      } catch ( UnknownParamException e ) {
        getLog().logDebug( "Ignoring unknown custom option: " + optionName, e );
      }
    }
  }

  private void startJob( Job job ) {
    job.start();
    job.waitUntilFinished();
    setResult( job.getResult() );
  }

  @VisibleForTesting
  void cleanupRepositorySession( Repository repository, Params params ) {
    disconnectRepository( repository );
    if ( isEnabled( params.getTrustRepoUser() ) ) {
      System.clearProperty( "pentaho.repository.client.attemptTrust" );
    }
  }

  private void disconnectRepository( Repository repository ) {
    if ( repository != null ) {
      repository.disconnect();
    }
  }

  private Result finishExecution( Date start ) {
    getLog().logMinimal( BaseMessages.getString( getPkgClazz(), "Kitchen.Log.Finished" ) );
    int returnCode = getReturnCode();
    Date stop = Calendar.getInstance().getTime();
    calculateAndPrintElapsedTime( start, stop, "Kitchen.Log.StartStop", "Kitchen.Log.ProcessEndAfter",
      "Kitchen.Log.ProcessEndAfterLong", "Kitchen.Log.ProcessEndAfterLonger",
      "Kitchen.Log.ProcessEndAfterLongest" );
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

  protected void executeRepositoryBasedCommand( Repository repository, final String dirName, final String listJobs,
                                                final String listDirs ) throws KettleException {

    RepositoryDirectoryInterface directory = loadRepositoryDirectory( repository, dirName,
      "Kitchen.Error.NoRepProvided",
      "Kitchen.Log.Alocate&ConnectRep", "Kitchen.Error.CanNotFindSuppliedDirectory" );

    if ( directory == null ) {
      return; // not much we can do here
    }

    if ( isEnabled( listJobs ) ) {
      printRepositoryStoredJobs( repository, directory );
    } else if ( isEnabled( listDirs ) ) {
      printRepositoryDirectories( repository, directory );
    }
  }

  public Job loadJobFromRepository( Repository repository, String dirName, String jobName ) throws KettleException {

    if ( Utils.isEmpty( jobName ) ) {
      getLog().logError( BaseMessages.getString( getPkgClazz(), COULD_NOT_LOAD_JOB_KEY ) );
      return null;
    }

    RepositoryDirectoryInterface directory = loadRepositoryDirectory( repository, dirName,
      "Kitchen.Error.NoRepProvided",
      "Kitchen.Log.Alocate&ConnectRep", "Kitchen.Error.CanNotFindSuppliedDirectory" );

    if ( directory == null ) {
      return null;
    }

    // Load a job
    logDebug( "Kitchen.Log.LoadingJobInfo" );
    blockAndThrow( getKettleInit() );
    JobMeta jobMeta = repository.loadJob( jobName, directory, null, null ); // reads last version
    logDebug( "Kitchen.Log.AllocateJob" );

    return new Job( repository, jobMeta );
  }

  public Job loadJobFromFilesystem( String initialDir, String filename, Serializable base64Zip )
    throws KettleException {

    if ( Utils.isEmpty( filename ) ) {
      getLog().logError( BaseMessages.getString( getPkgClazz(), COULD_NOT_LOAD_JOB_KEY ) );
      return null;
    }

    File zip;
    try {
      zip = base64Zip == null ? null : decodeBase64ToZipFile( base64Zip, true );
    } catch ( IOException e ) {
      throw new KettleException( "Unable to decode the supplied zip payload", e );
    }
    if ( zip != null ) {
      // update filename to a meaningful, 'ETL-file-within-zip' syntax
      filename = "zip:file:" + File.separator + File.separator + zip.getAbsolutePath() + "!" + filename;
    }

    blockAndThrow( getKettleInit() );
    String fileName = filename;
    // If the filename starts with scheme like zip:, then isAbsolute() will return
    // false even though
    // the path following the zip is absolute path. Check for isAbsolute only if the
    // fileName does not start with scheme
    if ( !KettleVFS.startsWithScheme( fileName ) && !FileUtil.isFullyQualified( fileName ) ) {
      fileName = initialDir + fileName;
    }

    JobMeta jobMeta = new JobMeta( getBowl(), fileName, null, null );
    return new Job( null, jobMeta );
  }

  protected void printJobParameters( Job job ) throws UnknownParamException {
    if ( job != null && job.listParameters() != null ) {
      for ( String parameterName : job.listParameters() ) {
        printParameter( parameterName, job.getParameterValue( parameterName ),
          job.getParameterDefault( parameterName ), job.getParameterDescription( parameterName ) );
      }
    }
  }

  @SuppressWarnings( "java:S106" ) // Need to print to System.out (console) for the CLI user
  protected void printRepositoryStoredJobs( Repository repository, RepositoryDirectoryInterface directory )
    throws KettleException {
    logDebug( "Kitchen.Log.GettingLostJobsInDirectory", "" + directory );

    String[] jobNames = repository.getJobNames( directory.getObjectId(), false );
    if ( jobNames == null ) {
      return;
    }
    for ( String jobName : jobNames ) {
      System.out.println( jobName );
    }
  }

  @SuppressWarnings( "java:S106" ) // Need to print to System.out (console) for the CLI user
  protected void printRepositories( RepositoriesMeta repositoriesMeta ) {
    if ( repositoriesMeta != null ) {
      getLog().logMinimal( BaseMessages.getString( getPkgClazz(), "Kitchen.Log.ListRep" ) );
      for ( int i = 0; i < repositoriesMeta.nrRepositories(); i++ ) {
        RepositoryMeta repositoryMeta = repositoriesMeta.getRepository( i );
        System.out.println( "#" + ( i + 1 ) + " : " + repositoryMeta.getName() + " ["
          + repositoryMeta.getDescription() + "]  id=" + repositoryMeta.getId() );
      }
    }
  }

  private void blockAndThrow( Future<KettleException> future ) throws KettleException {
    if ( future == null ) {
      return;
    }

    try {
      KettleException e = future.get();
      if ( e != null ) {
        throw e;
      }
    } catch ( InterruptedException e ) {
      Thread.currentThread().interrupt();
      throw new KettleException( KETTLE_INIT_INTERRUPTED, e );
    } catch ( ExecutionException e ) {
      Throwable cause = e.getCause();
      if ( cause instanceof KettleException kettleException ) {
        throw kettleException;
      }
      throw new KettleException( KETTLE_INIT_FAILED, cause == null ? e : cause );
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

  private record JobLoadOutcome(Job job, Repository repository, Integer exitCode) {

    private static JobLoadOutcome continueWith( Job job, Repository repository ) {
      return new JobLoadOutcome( job, repository, null );
    }

    private static JobLoadOutcome exit( int exitCode ) {
      return new JobLoadOutcome( null, null, exitCode );
    }

    private boolean shouldExit() {
      return exitCode != null;
    }
  }
}
