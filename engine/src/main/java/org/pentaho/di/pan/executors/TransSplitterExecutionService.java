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

import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.cluster.TransSplitter;

public class TransSplitterExecutionService {

  /**
   * Executes a transformation in clustered mode.
   *
   * @param extLog the log channel interface for logging
   * @param transSplitter the TransSplitter instance for splitting the transformation
   * @param parentJob the parent job (can be null)
   * @param executionConfiguration the execution configuration
   * @return the result of the clustered transformation execution
   * @throws KettleException if an error occurs during execution
   */
  public Result executeClustered( LogChannelInterface extLog, TransSplitter transSplitter, Job parentJob,
                                  TransExecutionConfiguration executionConfiguration ) throws KettleException {
    executeClustered( extLog, transSplitter, executionConfiguration );
    // Monitor clustered transformation
    Trans.monitorClusteredTransformation( extLog, transSplitter, parentJob );
    return Trans.getClusteredTransformationResult( extLog, transSplitter, parentJob );
  }

  /**
   * Executes a clustered transformation.
   *
   * @param extLog the log channel interface for logging
   * @param transSplitter the TransSplitter instance for splitting the transformation
   * @param executionConfiguration the execution configuration
   * @throws KettleException if an error occurs during execution
   */
  protected void executeClustered( LogChannelInterface extLog, TransSplitter transSplitter, TransExecutionConfiguration executionConfiguration )
    throws KettleException {
    // Execute clustered transformation
    try {
      Trans.executeClustered( transSplitter, executionConfiguration );
    } catch ( KettleException e ) {
      cleanupClusterAfterError( extLog, transSplitter, e );
    }
  }

  /**
   * Cleans up the cluster in case of an error during execution.
   *
   * @param extLog the log channel interface for logging
   * @param transSplitter the TransSplitter instance for splitting the transformation
   * @param e the exception that occurred during execution
   * @throws KettleException if an error occurs during cleanup
   */
  protected void cleanupClusterAfterError( LogChannelInterface extLog, TransSplitter transSplitter, Exception e ) throws KettleException {
    // Clean up cluster in case of error
    try {
      Trans.cleanupCluster( extLog, transSplitter );
    } catch ( Exception cleanupException ) {
      throw new KettleException( "Error executing transformation and error cleaning up cluster", e );
    }
  }
}
