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
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

/**
 * Interface for services that execute transformations.
 */
public interface TransExecutorService {

  /**
   * Executes a transformation.
   *
   * @param log the log channel interface for logging
   * @param transMeta the transformation metadata
   * @param repository the repository (can be null)
   * @param executionConfiguration the execution configuration
   * @param arguments the command-line arguments
   * @return the result of the transformation execution
   * @throws KettleException if an error occurs during execution
   */
  Result execute( LogChannelInterface log, TransMeta transMeta, Repository repository, TransExecutionConfiguration executionConfiguration, String[] arguments ) throws
    KettleException;
}
