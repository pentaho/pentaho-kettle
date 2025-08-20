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

package org.pentaho.di.pan.delegates;

import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.base.Params;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.pan.PanCommandExecutor;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

/**
 * Enhanced PanCommandExecutor that uses the PanTransformationDelegate
 * for centralized execution logic.
 */
public class EnhancedPanCommandExecutor extends PanCommandExecutor {

  private PanTransformationDelegate transformationDelegate;
  private Repository repository;

  public EnhancedPanCommandExecutor(Class<?> pkgClazz) {
    super(pkgClazz);
  }

  public EnhancedPanCommandExecutor(Class<?> pkgClazz, LogChannelInterface log) {
    super(pkgClazz, log);
    this.transformationDelegate = new PanTransformationDelegate(log);
  }

  /**
   * Override the main execute method to use the delegate pattern.
   */
  @Override
  public Result execute(final Params params, String[] arguments) throws Throwable {

    getLog().logMinimal(BaseMessages.getString(getPkgClazz(), "Pan.Log.StartingToRun"));

    // Handle special commands that don't require transformation execution
    if (handleSpecialCommands(params)) {
      return exitWithStatus(CommandExecutorCodes.Pan.SUCCESS.getCode());
    }

    // Load transformation from repository or filesystem
    TransMeta transMeta = loadTransformation(params);
    
    if (transMeta == null) {
      if (!isEnabled(params.getListRepoFiles()) && !isEnabled(params.getListRepoDirs()) && 
          !isEnabled(params.getListRepos()) && Utils.isEmpty(params.getExportRepo())) {
        
        System.out.println(BaseMessages.getString(getPkgClazz(), "Pan.Error.CanNotLoadTrans"));
        return exitWithStatus(CommandExecutorCodes.Pan.COULD_NOT_LOAD_TRANS.getCode());
      } else {
        return exitWithStatus(CommandExecutorCodes.Pan.SUCCESS.getCode());
      }
    }

    // Handle parameter listing if requested
    if (isEnabled(params.getListFileParams())) {
      printTransformationParameters(new org.pentaho.di.trans.Trans(transMeta));
      return exitWithStatus(CommandExecutorCodes.Pan.COULD_NOT_LOAD_TRANS.getCode());
    }

    // Use delegate to execute transformation
    return executeWithDelegate(transMeta, params, arguments);
  }

  /**
   * Handle special commands that don't require transformation execution.
   * Returns true if a special command was handled, false otherwise.
   */
  private boolean handleSpecialCommands(Params params) throws Exception {
    
    // Handle repository listing
    if (isEnabled(params.getListRepos())) {
      printRepositories(loadRepositoryInfo("Pan.Log.LoadingAvailableRep", "Pan.Error.NoRepsDefined"));
      return true;
    }

    // Handle repository-based commands
    if (!Utils.isEmpty(params.getRepoName()) && !isEnabled(params.getBlockRepoConns())) {
      initializeRepository(params);
      
      if (isEnabled(params.getListRepoFiles()) || isEnabled(params.getListRepoDirs()) || 
          !Utils.isEmpty(params.getExportRepo())) {
        
        executeRepositoryBasedCommand(repository, params.getInputDir(), 
          params.getListRepoFiles(), params.getListRepoDirs(), params.getExportRepo());
        return true;
      }
    }

    return false;
  }

  /**
   * Load transformation from repository or filesystem based on parameters.
   */
  private TransMeta loadTransformation(Params params) throws Exception {
    
    TransMeta transMeta = null;

    // Try to load from repository first if repository parameters are provided
    if (!Utils.isEmpty(params.getRepoName()) && !isEnabled(params.getBlockRepoConns())) {
      
      if (repository == null) {
        initializeRepository(params);
      }
      
      if (repository != null) {
        org.pentaho.di.trans.Trans trans = loadTransFromRepository(repository, params.getInputDir(), params.getInputFile());
        if (trans != null) {
          transMeta = trans.getTransMeta();
        }
      }
    }

    // Try to load from filesystem if not loaded from repository
    if (transMeta == null && (!Utils.isEmpty(params.getLocalFile()) || !Utils.isEmpty(params.getLocalJarFile()))) {
      org.pentaho.di.trans.Trans trans = loadTransFromFilesystem(params.getLocalInitialDir(),
        params.getLocalFile(), params.getLocalJarFile(), params.getBase64Zip());
      if (trans != null) {
        transMeta = trans.getTransMeta();
      }
    }

    return transMeta;
  }

  /**
   * Execute transformation using the delegate pattern.
   * This method shows how to integrate the PanTransformationDelegate
   * into the existing command executor framework.
   */
  public Result executeWithDelegate(TransMeta transMeta, Params params, String[] arguments) throws Throwable {

    // Initialize repository if repository parameters are provided
    try {
      initializeRepository(params);
    } catch (Exception e) {
      getLog().logError("Failed to initialize repository", e);
      return exitWithStatus(CommandExecutorCodes.Pan.COULD_NOT_LOAD_TRANS.getCode());
    }

    // Create execution configuration based on parameters
    TransExecutionConfiguration executionConfiguration = createExecutionConfigurationFromParams(params);

    // Set repository on the transformation delegate
    transformationDelegate.setRepository(repository);

    try {
      // Use the delegate to execute the transformation
      Result result = transformationDelegate.executeTransformation(transMeta, executionConfiguration, arguments);
      
      // Set the result for the command executor
      setResult(result);
      
      // Return appropriate exit code based on result
      if (result.getNrErrors() == 0) {
        return exitWithStatus(CommandExecutorCodes.Pan.SUCCESS.getCode());
      } else {
        return exitWithStatus(CommandExecutorCodes.Pan.ERRORS_DURING_PROCESSING.getCode());
      }

    } catch (KettleException e) {
      getLog().logError("Error executing transformation", e);
      return exitWithStatus(CommandExecutorCodes.Pan.UNEXPECTED_ERROR.getCode());
    } finally {
      // Clean up repository connection if it was established
      if (repository != null) {
        try {
          repository.disconnect();
        } catch (Exception e) {
          getLog().logError("Error disconnecting from repository", e);
        }
      }
    }
  }

  /**
   * Create execution configuration from command-line parameters.
   */
  private TransExecutionConfiguration createExecutionConfigurationFromParams(Params params) {
    
    TransExecutionConfiguration config = PanTransformationDelegate.createDefaultExecutionConfiguration();

    // Set log level if specified
    if (!Utils.isEmpty(params.getLogLevel())) {
      try {
        LogLevel logLevel = LogLevel.getLogLevelForCode(params.getLogLevel());
        config.setLogLevel(logLevel);
      } catch (Exception e) {
        // Use default log level if parsing fails
        getLog().logError("Invalid log level specified: " + params.getLogLevel() + ", using default");
      }
    }

    // Set safe mode
    if ("Y".equalsIgnoreCase(params.getSafeMode())) {
      config.setSafeModeEnabled(true);
    }

    // Set metrics gathering
    if ("Y".equalsIgnoreCase(params.getMetrics())) {
      config.setGatheringMetrics(true);
    }

    // Apply run configuration if specified
    if (!Utils.isEmpty(params.getRunConfiguration())) {
      config.setRunConfiguration(params.getRunConfiguration());
      
      // Here you could add logic to determine execution type based on run configuration
      // For example, if run configuration specifies a remote server or cluster
      // you would set the appropriate execution type and server details
    }

    return config;
  }

  // Getter for the transformation delegate
  public PanTransformationDelegate getTransformationDelegate() {
    return transformationDelegate;
  }

  // Setter for the transformation delegate
  public void setTransformationDelegate(PanTransformationDelegate transformationDelegate) {
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
  public void initializeRepository(Params params) throws Exception {
    
    // Check if repository parameters are provided
    if (!Utils.isEmpty(params.getRepoName()) && !isEnabled(params.getBlockRepoConns())) {
      
      /**
       * if set, _trust_user_ needs to be considered. See pur-plugin's:
       *
       * @link https://github.com/pentaho/pentaho-kettle/blob/8.0.0.0-R/plugins/pur/core/src/main/java/org/pentaho/di/repository/pur/PurRepositoryConnector.java#L97-L101
       * @link https://github.com/pentaho/pentaho-kettle/blob/8.0.0.0-R/plugins/pur/core/src/main/java/org/pentaho/di/repository/pur/WebServiceManager.java#L130-L133
       */
      if (isEnabled(params.getTrustRepoUser())) {
        System.setProperty("pentaho.repository.client.attemptTrust", "Y");
      }

      // Load repository metadata
      RepositoryMeta repositoryMeta = loadRepositoryConnection(
        params.getRepoName(), 
        "Pan.Log.LoadingAvailableRep", 
        "Pan.Error.NoRepsDefined",
        "Pan.Log.FindingRep"
      );

      if (repositoryMeta == null) {
        getLog().logError(BaseMessages.getString(getPkgClazz(), "Pan.Error.CanNotConnectRep"));
        throw new KettleException("Could not connect to repository: " + params.getRepoName());
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
}
