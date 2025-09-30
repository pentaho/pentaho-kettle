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

import org.pentaho.di.base.AbstractBaseCommandExecutor;
import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.base.Params;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.util.FileUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.pan.delegates.PanTransformationDelegate;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * EnhancedPanCommandExecutor that uses the PanTransformationDelegate
 * for centralized execution logic.
 */
public class PanCommandExecutor extends AbstractBaseCommandExecutor {

  private PanTransformationDelegate transformationDelegate;
  private Repository repository;

  public PanCommandExecutor( Class<?> pkgClazz, LogChannelInterface log ) {
    setPkgClazz( pkgClazz );
    setLog( log );
    this.transformationDelegate = new PanTransformationDelegate( log );
  }

  public Result execute( final Params params, String[] arguments ) throws KettleException {

    getLog().logMinimal( BaseMessages.getString( getPkgClazz(), "Pan.Log.StartingToRun" ) );

    // Handle special commands that don't require transformation execution
    if ( handleSpecialCommands( params ) ) {
      return exitWithStatus( CommandExecutorCodes.Pan.SUCCESS.getCode() );
    }

    // Try to load from repository first if repository parameters are provided
    if ( !Utils.isEmpty( params.getRepoName() ) && !isEnabled( params.getBlockRepoConns() ) && repository == null ) {
      try {
        initializeRepository( params );
      } catch ( KettleException e ) {
        getLog().logError( "Failed to initialize repository", e );
        return exitWithStatus( CommandExecutorCodes.Pan.COULD_NOT_LOAD_TRANS.getCode() );
      }
    }
    // Load transformation from repository or filesystem
    CommandExecutorResult result = validateAndSetPluginContext( getLog(), params, repository );
    if ( result != null && result.getCode() != 0 ) {
      return exitWithStatus( result.getCode() );
    }
    Trans trans;
    try {
      trans = loadTransformation( params );
    } catch ( Exception e ) {
      getLog().logError( BaseMessages.getString( getPkgClazz(), "Pan.Error.ProcessStopError", e.getMessage() ) );
      return handleTransformationLoadError();
    }

    if ( trans == null ) {
      if ( !isEnabled( params.getListRepoFiles() ) && !isEnabled( params.getListRepoDirs() )
        && !isEnabled( params.getListRepos() ) && Utils.isEmpty( params.getExportRepo() ) ) {

        getLog().logMinimal( BaseMessages.getString( getPkgClazz(), "Pan.Error.CanNotLoadTrans" ) );
        return exitWithStatus( CommandExecutorCodes.Pan.COULD_NOT_LOAD_TRANS.getCode() );
      } else {
        return exitWithStatus( CommandExecutorCodes.Pan.SUCCESS.getCode() );
      }
    }
    trans.setLogLevel( getLog().getLogLevel() );
    configureParameters( trans, params.getNamedParams(), trans.getTransMeta() );

    trans.setSafeModeEnabled( isEnabled( params.getSafeMode() ) ); // run in safe mode if requested
    trans.setGatheringMetrics( isEnabled( params.getMetrics() ) ); // enable kettle metric gathering if requested
    // Handle parameter listing if requested
    if ( isEnabled( params.getListFileParams() ) ) {
      printTransformationParameters( trans );
      return exitWithStatus( CommandExecutorCodes.Pan.COULD_NOT_LOAD_TRANS.getCode() );
    }

    // Use delegate to execute transformation
    return executeWithDelegate( trans, params, arguments );
  }

  private Result handleTransformationLoadError() {
    if ( repository != null ) {
      repository.disconnect();
    }
    return exitWithStatus( CommandExecutorCodes.Pan.ERRORS_DURING_PROCESSING.getCode(), null );
  }

  /**
   * Handle special commands that don't require transformation execution.
   * Returns true if a special command was handled, false otherwise.
   */
  private boolean handleSpecialCommands( Params params ) throws KettleException {

    // Handle repository listing
    if ( isEnabled( params.getListRepos() ) ) {
      printRepositories( loadRepositoryInfo( "Pan.Log.LoadingAvailableRep", "Pan.Error.NoRepsDefined" ) );
      return true;
    }

    // Handle repository-based commands
    if ( !Utils.isEmpty( params.getRepoName() ) && !isEnabled( params.getBlockRepoConns() ) ) {
      try {
        initializeRepository( params );
      } catch ( KettleException e ) {
        return false; // Repository initialization failed, cannot handle repo-based commands
      }

      if ( isEnabled( params.getListRepoFiles() ) || isEnabled( params.getListRepoDirs() )
        || !Utils.isEmpty( params.getExportRepo() ) ) {

        executeRepositoryBasedCommand( repository, params.getInputDir(),
          params.getListRepoFiles(), params.getListRepoDirs(), params.getExportRepo() );
        return true;
      }
    }

    return false;
  }

  /**
   * Load transformation from repository or filesystem based on parameters.
   */
  protected Trans loadTransformation( Params params ) throws KettleException, IOException {

    Trans trans = null;
    if ( repository != null ) {
      // Validate PluginNamedParams when repository is connected
      trans = loadTransFromRepository( repository, params.getInputDir(), params.getInputFile() );
      if ( trans != null ) {
        return trans;
      }
    }

    // Try to load from filesystem if not loaded from repository
    if ( !Utils.isEmpty( params.getLocalFile() ) || !Utils.isEmpty( params.getLocalJarFile() ) ) {
      trans = loadTransFromFilesystem( params.getLocalInitialDir(),
        params.getLocalFile(), params.getLocalJarFile(), params.getBase64Zip() );
    }

    return trans;
  }

  /**
   * Execute transformation using the delegate pattern.
   * This method shows how to integrate the PanTransformationDelegate
   * into the existing command executor framework.
   */
  public Result executeWithDelegate( Trans trans, Params params, String[] arguments ) {

    // Initialize repository if repository parameters are provided
    try {
      initializeRepository( params );
    } catch ( Exception e ) {
      getLog().logError( "Failed to initialize repository", e );
      return exitWithStatus( CommandExecutorCodes.Pan.COULD_NOT_LOAD_TRANS.getCode() );
    }

    // Create execution configuration based on parameters
    TransExecutionConfiguration executionConfiguration = createExecutionConfigurationFromParams( params );

    // Set repository on the transformation delegate
    getTransformationDelegate().setRepository( repository );

    try {
      // Use the delegate to execute the transformation
      Result result = getTransformationDelegate().executeTransformation( trans, executionConfiguration, arguments );

      // Set the result for the command executor
      setResult( result );

      // Return appropriate exit code based on result
      if ( result.getNrErrors() == 0 ) {
        return exitWithStatus( CommandExecutorCodes.Pan.SUCCESS.getCode() );
      } else {
        return exitWithStatus( CommandExecutorCodes.Pan.ERRORS_DURING_PROCESSING.getCode() );
      }

    } catch ( KettleException e ) {
      getLog().logError( "Error executing transformation", e );
      return exitWithStatus( CommandExecutorCodes.Pan.UNEXPECTED_ERROR.getCode() );
    } finally {
      // Clean up repository connection if it was established
      if ( repository != null ) {
        try {
          repository.disconnect();
        } catch ( Exception e ) {
          getLog().logError( "Error disconnecting from repository", e );
        }
      }
    }
  }

  /**
   * Create execution configuration from command-line parameters.
   */
  protected TransExecutionConfiguration createExecutionConfigurationFromParams( Params params ) {

    TransExecutionConfiguration config = getTransformationDelegate().createDefaultExecutionConfiguration();

    // Set log level if specified
    if ( !Utils.isEmpty( params.getLogLevel() ) ) {
      try {
        LogLevel logLevel = LogLevel.getLogLevelForCode( params.getLogLevel() );
        config.setLogLevel( logLevel );
      } catch ( Exception e ) {
        // Use default log level if parsing fails
        getLog().logError( "Invalid log level specified: " + params.getLogLevel() + ", using default" );
      }
    }

    // Set safe mode
    if ( "Y".equalsIgnoreCase( params.getSafeMode() ) ) {
      config.setSafeModeEnabled( true );
    }

    // Set metrics gathering
    if ( "Y".equalsIgnoreCase( params.getMetrics() ) ) {
      config.setGatheringMetrics( true );
    }

    // Apply run configuration if specified
    if ( !Utils.isEmpty( params.getRunConfiguration() ) ) {
      config.setRunConfiguration( params.getRunConfiguration() );
    }

    return config;
  }

  // Getter for the transformation delegate
  public PanTransformationDelegate getTransformationDelegate() {
    return transformationDelegate;
  }

  // Setter for the transformation delegate
  public void setTransformationDelegate( PanTransformationDelegate transformationDelegate ) {
    this.transformationDelegate = transformationDelegate;
  }

  /**
   * Get the repository instance. If not already initialized, it will be null.
   * Call initializeRepository() first to set up the repository connection.
   */
  public Repository getRepository() {
    return repository;
  }

  /**
   * Initialize repository connection based on parameters.
   * This method extracts the repository initialization logic from PanCommandExecutor.
   */
  public void initializeRepository( Params params ) throws KettleException {

    // Check if repository parameters are provided
    if ( !Utils.isEmpty( params.getRepoName() ) && !isEnabled( params.getBlockRepoConns() ) ) {

      /**
       * if set, _trust_user_ needs to be considered. See pur-plugin's:
       *
       * @link https://github.com/pentaho/pentaho-kettle/blob/8.0.0.0-R/plugins/pur/core/src/main/java/org/pentaho/di/repository/pur/PurRepositoryConnector.java#L97-L101
       * @link https://github.com/pentaho/pentaho-kettle/blob/8.0.0.0-R/plugins/pur/core/src/main/java/org/pentaho/di/repository/pur/WebServiceManager.java#L130-L133
       */
      if ( isEnabled( params.getTrustRepoUser() ) ) {
        System.setProperty( "pentaho.repository.client.attemptTrust", "Y" );
      }

      // Load repository metadata
      RepositoryMeta repositoryMeta = loadRepositoryConnection(
        params.getRepoName(),
        "Pan.Log.LoadingAvailableRep",
        "Pan.Error.NoRepsDefined",
        "Pan.Log.FindingRep"
      );

      if ( repositoryMeta == null ) {
        getLog().logError( BaseMessages.getString( getPkgClazz(), "Pan.Error.CanNotConnectRep" ) );
        throw new KettleException( "Could not connect to repository: " + params.getRepoName() );
      }

      // Establish repository connection
      this.repository = establishRepositoryConnection(
        repositoryMeta,
        params.getRepoUsername(),
        params.getRepoPassword(),
        RepositoryOperation.EXECUTE_TRANSFORMATION
      );
    } else {
      // No repository parameters provided, keep repository as null
      this.repository = null;
    }
  }

  public int printVersion() {
    printVersion( "Pan.Log.KettleVersion" );
    return CommandExecutorCodes.Pan.KETTLE_VERSION_PRINT.getCode();
  }

  protected void executeRepositoryBasedCommand( Repository repository, String dirName, String listTrans,
                                                String listDirs, String exportRepo ) throws KettleException {

    RepositoryDirectoryInterface directory = loadRepositoryDirectory( repository, dirName, "Pan.Error.NoRepProvided",
      "Pan.Log.Allocate&ConnectRep", "Pan.Error.CanNotFindSpecifiedDirectory" );

    if ( directory == null ) {
      return; // not much we can do here
    }

    if ( isEnabled( listTrans ) ) {
      printRepositoryStoredTransformations( repository, directory ); // List the transformations in the repository

    } else if ( isEnabled( listDirs ) ) {
      printRepositoryDirectories( repository, directory ); // List the directories in the repository

    } else if ( !Utils.isEmpty( exportRepo ) ) {
      // Export the repository
      getLog().logMinimal(
        BaseMessages.getString( getPkgClazz(), "Pan.Log.ExportingObjectsRepToFile", "" + exportRepo ) );
      repository.getExporter().exportAllObjects( null, exportRepo, directory, "all" );
      getLog().logMinimal(
        BaseMessages.getString( getPkgClazz(), "Pan.Log.FinishedExportObjectsRepToFile", "" + exportRepo ) );
    }
  }

  public Trans loadTransFromRepository( Repository repository, String dirName, String transName ) throws KettleException {

    if ( Utils.isEmpty( transName ) ) {
      getLog().logMinimal( BaseMessages.getString( getPkgClazz(), "Pan.Error.NoTransNameSupplied" ) );
      return null;
    }

    RepositoryDirectoryInterface directory = loadRepositoryDirectory( repository, dirName, "Pan.Error.NoRepProvided",
      "Pan.Log.Allocate&ConnectRep", "Pan.Error.CanNotFindSpecifiedDirectory" );

    if ( directory == null ) {
      return null; // not much we can do here
    }

    logDebug( "Pan.Log.LoadTransInfo" );
    TransMeta transMeta = repository.loadTransformation( transName, directory, null, true, null );

    logDebug( "Pan.Log.AllocateTrans" );
    Trans trans = new Trans( transMeta );
    trans.setRepository( repository );
    trans.setMetaStore( getMetaStore() );

    return trans; // return transformation loaded from the repo
  }

  public Trans loadTransFromFilesystem( String initialDir, String filename, String jarFilename,
                                        Serializable base64Zip ) throws IOException, KettleMissingPluginsException, KettleXMLException {

    Trans trans = null;

    File zip;
    if ( base64Zip != null && ( zip = decodeBase64ToZipFile( base64Zip, true ) ) != null ) {
      // update filename to a meaningful, 'ETL-file-within-zip' syntax
      filename = "zip:file:" + File.separator + File.separator + zip.getAbsolutePath() + "!" + filename;
    }

    // Try to load the transformation from file
    if ( !Utils.isEmpty( filename ) ) {

      String filepath = filename;
      // If the filename starts with scheme like zip:, then isAbsolute() will return false even though the
      // the path following the zip is absolute. Check for isAbsolute only if the fileName does not start with scheme
      if ( !KettleVFS.startsWithScheme( filename ) && !FileUtil.isFullyQualified( filename ) ) {
        filepath = initialDir + filename;
      }

      logDebug( "Pan.Log.LoadingTransXML", "" + filepath );
      TransMeta transMeta = new TransMeta( getBowl(), filepath );
      trans = new Trans( transMeta );
    }

    if ( !Utils.isEmpty( jarFilename ) ) {

      try {

        logDebug( "Pan.Log.LoadingTransJar", jarFilename );

        InputStream inputStream = PanCommandExecutor.class.getResourceAsStream( jarFilename );
        StringBuilder xml = new StringBuilder();
        int c;
        while ( ( c = inputStream.read() ) != -1 ) {
          xml.append( (char) c );
        }
        inputStream.close();
        Document document = XMLHandler.loadXMLString( xml.toString() );
        TransMeta transMeta = new TransMeta( XMLHandler.getSubNode( document, "transformation" ), null );
        trans = new Trans( transMeta );

      } catch ( Exception e ) {

        logDebug( "Pan.Error.ReadingJar", e.toString() );
        logDebug( Const.getStackTracker( e ) );
        throw e;
      }
    }

    if ( trans != null ) {
      trans.setMetaStore( getMetaStore() );
    }

    return trans;
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
    // Skip for the moment any extra command line parameter not known in the transformation.
    String[] transParams = trans.listParameters();
    for ( String param : transParams ) {
      String value = optionParams.getParameterValue( param );
      if ( value != null ) {
        trans.setParameterValue( param, value );
        transMeta.setParameterValue( param, value );
      }
    }

    // Put the parameters over the already defined variable space. Parameters get priority.
    trans.activateParameters();
  }

  protected void printTransformationParameters( Trans trans ) throws UnknownParamException {

    if ( trans != null && trans.listParameters() != null ) {

      for ( String pName : trans.listParameters() ) {
        printParameter( pName, trans.getParameterValue( pName ), trans.getParameterDefault( pName ),
          trans.getParameterDescription( pName ) );
      }
    }
  }

  @SuppressWarnings( "java:S106" ) // Need to print to System.out (console) for the CLI user
  protected void printRepositoryStoredTransformations( Repository repository, RepositoryDirectoryInterface directory )
    throws KettleException {

    logDebug( "Pan.Log.GettingListTransDirectory", "" + directory );
    String[] transformations = repository.getTransformationNames( directory.getObjectId(), false );

    if ( transformations != null ) {
      for ( String trans : transformations ) {
        System.out.println( trans );
      }
    }
  }

  @SuppressWarnings( "java:S106" ) // Need to print to System.out (console) for the CLI user
  protected void printRepositories( RepositoriesMeta repositoriesMeta ) {

    if ( repositoriesMeta != null ) {

      logDebug( "Pan.Log.GettingListReps" );

      for ( int i = 0; i < repositoriesMeta.nrRepositories(); i++ ) {
        RepositoryMeta repInfo = repositoriesMeta.getRepository( i );
        System.out.println( BaseMessages.getString( getPkgClazz(), "Pan.Log.RepNameDesc", "" + ( i + 1 ),
          repInfo.getName(), repInfo.getDescription() ) );
      }
    }
  }
  protected Result exitWithStatus( final int exitStatus, Trans trans ) {
    try {
      ExtensionPointHandler.callExtensionPoint( getLog(), KettleExtensionPoint.TransformationFinish.id, trans );
    } catch ( KettleException e ) {
      getLog().logError( "A KettleException occurred when attempting to call TransformationFinish extension point", e );
    }
    return exitWithStatus( exitStatus );
  }

}
