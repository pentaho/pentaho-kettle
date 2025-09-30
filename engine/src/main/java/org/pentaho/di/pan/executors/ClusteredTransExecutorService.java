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

package org.pentaho.di.pan.executors;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.cluster.TransSplitter;

/**
 * Service for executing transformations in clustered mode.
 */
public class ClusteredTransExecutorService implements TransExecutorService {
  private static Class<?> pkg = ClusteredTransExecutorService.class;

  TransSplitterExecutionService transSplitterExecutionService;
  private static final String DASHES = "-----------------------------------------------------";

  /**
   * Default constructor that initializes the service with a new TransSplitterExecutionService instance.
   */
  public ClusteredTransExecutorService() {
    this( new TransSplitterExecutionService() );
  }

  /**
   * Constructor that allows injecting a custom TransSplitterExecutionService instance.
   *
   * @param transSplitterExecutionService the TransSplitterExecutionService to use
   */
  public ClusteredTransExecutorService( TransSplitterExecutionService transSplitterExecutionService )  {
    this.transSplitterExecutionService = transSplitterExecutionService;
  }

  /**
   * Executes a transformation in clustered mode.
   *
   * @param extLog the log channel interface for logging
   * @param transMeta the transformation metadata
   * @param repository the repository (can be null)
   * @param executionConfiguration the execution configuration
   * @param arguments the command-line arguments
   * @return the result of the transformation execution
   * @throws KettleException if an error occurs during execution
   */
  @Override
  public Result execute( LogChannelInterface extLog, TransMeta transMeta, Repository repository,
                         TransExecutionConfiguration executionConfiguration, String[] arguments ) throws KettleException {

    extLog.logBasic( BaseMessages.getString( pkg, "PanTransformationDelegate.Log.ExecutingClustered" ) );

    final TransSplitter transSplitter = new TransSplitter( transMeta );
    transSplitter.splitOriginalTransformation();

    Result result =  executeClustered( extLog, transMeta, transSplitter, executionConfiguration );
    logClusteredResults( extLog, transMeta, result );
    return result;
  }


  /**
   * Executes a transformation in clustered mode using the provided TransSplitter and execution configuration.
   *
   * @param extLog the log channel interface for logging
   * @param transMeta the transformation metadata
   * @param transSplitter the TransSplitter instance for splitting the transformation
   * @param executionConfiguration the execution configuration
   * @return the result of the clustered transformation execution
   * @throws KettleException if an error occurs during execution
   */
  protected Result executeClustered( LogChannelInterface extLog, TransMeta transMeta, TransSplitter transSplitter,
                                     TransExecutionConfiguration executionConfiguration ) throws KettleException {

    extLog.logBasic( BaseMessages.getString( pkg, "PanTransformationDelegate.Log.ExecutingClustered" ) );
    // Inject certain internal variables to make it more intuitive
    for ( String transVar : Const.INTERNAL_TRANS_VARIABLES ) {
      executionConfiguration.getVariables().put( transVar, transMeta.getVariable( transVar ) );
    }

    // Parameters override the variables
    TransMeta originalTransformation = transSplitter.getOriginalTransformation();
    for ( String param : originalTransformation.listParameters() ) {
      String value = Const.NVL( originalTransformation.getParameterValue( param ),
        Const.NVL( originalTransformation.getParameterDefault( param ),
          originalTransformation.getVariable( param ) ) );
      if ( !Utils.isEmpty( value ) ) {
        executionConfiguration.getVariables().put( param, value );
      }
    }

    return transSplitterExecutionService.executeClustered(
      extLog, transSplitter, null, executionConfiguration
    );
  }


  /**
   * Logs the results of a clustered transformation execution.
   *
   * @param log the log channel interface for logging
   * @param transMeta the transformation metadata
   * @param result the result of the transformation execution
   */
  protected void logClusteredResults( LogChannelInterface log, TransMeta transMeta, Result result ) {
    log.logBasic( DASHES );
    log.logBasic( "Got result back from clustered transformation:" );
    log.logBasic( transMeta + DASHES );
    log.logBasic( transMeta + " Errors : " + result.getNrErrors() );
    log.logBasic( transMeta + " Input : " + result.getNrLinesInput() );
    log.logBasic( transMeta + " Output : " + result.getNrLinesOutput() );
    log.logBasic( transMeta + " Updated : " + result.getNrLinesUpdated() );
    log.logBasic( transMeta + " Read : " + result.getNrLinesRead() );
    log.logBasic( transMeta + " Written : " + result.getNrLinesWritten() );
    log.logBasic( transMeta + " Rejected : " + result.getNrLinesRejected() );
    log.logBasic( transMeta + DASHES );
  }
}
