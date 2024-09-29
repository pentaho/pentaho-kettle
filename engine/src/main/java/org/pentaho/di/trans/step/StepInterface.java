/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.step;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.HasLogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.metastore.api.IMetaStore;

/**
 * The interface that any transformation step or plugin needs to implement.
 *
 * Created on 12-AUG-2004
 *
 * @author Matt
 *
 */

public interface StepInterface extends VariableSpace, HasLogChannelInterface {
  /**
   * @return the transformation that is executing this step
   */
  Trans getTrans();

  /**
   * Perform the equivalent of processing one row. Typically this means reading a row from input (getRow()) and passing
   * a row to output (putRow)).
   *
   * @param smi
   *          The steps metadata to work with
   * @param sdi
   *          The steps temporary working data to work with (database connections, result sets, caches, temporary
   *          variables, etc.)
   * @return false if no more rows can be processed or an error occurred.
   * @throws KettleException
   */
  boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException;

  /**
   * This method is executed by Trans right before starting processing rows.
   *
   * @param smi
   *          The steps metadata to work with
   * @param sdi
   *          The steps temporary working data to work with (database connections, result sets, caches, temporary
   *          variables, etc.)
   * @throws KettleException
   */
  default boolean beforeStartProcessing( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    return true;
  }

  /**
   * This method is executed by Trans after finishing processing rows.
   *
   * @param smi
   *          The steps metadata to work with
   * @param sdi
   *          The steps temporary working data to work with (database connections, result sets, caches, temporary
   *          variables, etc.)
   * @throws KettleException
   */
  default boolean afterFinishProcessing( StepMetaInterface smi, StepDataInterface sdi ) {
    return true;
  }

  /**
   * This method checks if the step is capable of processing at least one row.
   * <p>
   * For example, if a step has no input records but needs at least one to function, it will return false.
   *
   * @return true if the step can process a row.
   *
   */
  boolean canProcessOneRow();

  /**
   * Initialize and do work where other steps need to wait for...
   *
   * @param stepMetaInterface
   *          The metadata to work with
   * @param stepDataInterface
   *          The data to initialize
   */
  boolean init( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface );

  /**
   * Dispose of this step: close files, empty logs, etc.
   *
   * @param sii
   *          The metadata to work with
   * @param sdi
   *          The data to dispose of
   */
  void dispose( StepMetaInterface sii, StepDataInterface sdi );

  /**
   * Mark the start time of the step.
   *
   */
  void markStart();

  /**
   * Mark the end time of the step.
   *
   */
  void markStop();

  /**
   * Stop running operations...
   *
   * @param stepMetaInterface
   *          The metadata that might be needed by the step to stop running.
   * @param stepDataInterface
   *          The interface to the step data containing the connections, resultsets, open files, etc.
   *
   */
  void stopRunning( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) throws KettleException;

  /**
   * @return true if the step is running after having been initialized
   */
  boolean isRunning();

  /**
   * Flag the step as running or not
   *
   * @param running
   *          the running flag to set
   */
  void setRunning( boolean running );

  /**
   * @return True if the step is marked as stopped. Execution should stop immediate.
   */
  boolean isStopped();

  /**
   * @param stopped
   *          true if the step needs to be stopped
   */
  void setStopped( boolean stopped );

  /**
   * @param stopped
   *          true if the step needs to be safe stopped
   */
  default void setSafeStopped( boolean stopped ) {
  }

  /**
   * @return true if step is safe stopped.
   */
  default boolean isSafeStopped() {
    return false;
  }

  /**
   * @return True if the step is paused
   */
  boolean isPaused();

  /**
   * Flags all rowsets as stopped/completed/finished.
   */
  void stopAll();

  /**
   * Pause a running step
   */
  void pauseRunning();

  /**
   * Resume a running step
   */
  void resumeRunning();

  /**
   * Get the name of the step.
   *
   * @return the name of the step
   */
  String getStepname();

  /**
   * @return The steps copy number (default 0)
   */
  int getCopy();

  /**
   * @return the type ID of the step...
   */
  String getStepID();

  /**
   * Get the number of errors
   *
   * @return the number of errors
   */
  long getErrors();

  /**
   * Sets the number of errors
   *
   * @param errors
   *          the number of errors to set
   */
  void setErrors( long errors );

  /**
   * @return Returns the linesInput.
   */
  long getLinesInput();

  /**
   * @return Returns the linesOutput.
   */
  long getLinesOutput();

  /**
   * @return Returns the linesRead.
   */
  long getLinesRead();

  /**
   * @return Returns the linesWritten.
   */
  long getLinesWritten();

  /**
   * @return Returns the linesUpdated.
   */
  long getLinesUpdated();

  /**
   * @param linesRejected
   *          steps the lines rejected by error handling.
   */
  void setLinesRejected( long linesRejected );

  /**
   * @return Returns the lines rejected by error handling.
   */
  long getLinesRejected();

  /**
   * Put a row on the destination rowsets.
   *
   * @param row
   *          The row to send to the destinations steps
   */
  void putRow( RowMetaInterface row, Object[] data ) throws KettleException;

  /**
   * @return a row from the source step(s).
   */
  Object[] getRow() throws KettleException;

  /**
   * Signal output done to destination steps
   */
  void setOutputDone();

  /**
   * Add a rowlistener to the step allowing you to inspect (or manipulate, be careful) the rows coming in or exiting the
   * step.
   *
   * @param rowListener
   *          the rowlistener to add
   */
  void addRowListener( RowListener rowListener );

  /**
   * Remove a rowlistener from this step.
   *
   * @param rowListener
   *          the rowlistener to remove
   */
  void removeRowListener( RowListener rowListener );

  /**
   * @return a list of the installed RowListeners
   */
  List<RowListener> getRowListeners();

  /**
   * @return The list of active input rowsets for the step
   */
  List<RowSet> getInputRowSets();

  /**
   * @return The list of active output rowsets for the step
   */
  List<RowSet> getOutputRowSets();

  /**
   * @return true if the step is running partitioned
   */
  boolean isPartitioned();

  /**
   * @param partitionID
   *          the partitionID to set
   */
  void setPartitionID( String partitionID );

  /**
   * @return the steps partition ID
   */
  String getPartitionID();

  /**
   * Call this method typically, after ALL the slave transformations in a clustered run have finished.
   */
  void cleanup();

  /**
   * This method is executed by Trans right before the threads start and right after initialization.<br>
   * <br>
   * <b>!!! A plugin implementing this method should make sure to also call <i>super.initBeforeStart();</i> !!!</b>
   *
   * @throws KettleStepException
   *           In case there is an error
   */
  void initBeforeStart() throws KettleStepException;

  /**
   * Attach a step listener to be notified when a step arrives in a certain state. (finished)
   *
   * @param stepListener
   *          The listener to add to the step
   */
  void addStepListener( StepListener stepListener );

  /**
   * @return true if the thread is a special mapping step
   */
  boolean isMapping();

  /**
   * @return The metadata for this step
   */
  StepMeta getStepMeta();

  /**
   * @return the logging channel for this step
   */
  @Override LogChannelInterface getLogChannel();

  /**
   * @param usingThreadPriorityManagment
   *          set to true to actively manage priorities of step threads
   */
  void setUsingThreadPriorityManagment( boolean usingThreadPriorityManagment );

  /**
   * @return true if we are actively managing priorities of step threads
   */
  boolean isUsingThreadPriorityManagment();

  /**
   * @return The total amount of rows in the input buffers
   */
  int rowsetInputSize();

  /**
   * @return The total amount of rows in the output buffers
   */
  int rowsetOutputSize();

  /**
   * @return The number of "processed" lines of a step. Well, a representable metric for that anyway.
   */
  long getProcessed();

  /**
   * @return The result files for this step
   */
  Map<String, ResultFile> getResultFiles();

  /**
   * @return the description as in {@link StepDataInterface}
   */
  StepExecutionStatus getStatus();

  /**
   * @return The number of ms that this step has been running
   */
  long getRuntime();

  /**
   * To be used to flag an error output channel of a step prior to execution for performance reasons.
   */
  void identifyErrorOutput();

  /**
   * @param partitioned
   *          true if this step is partitioned
   */
  void setPartitioned( boolean partitioned );

  /**
   * @param partitioningMethod
   *          The repartitioning method
   */
  void setRepartitioning( int partitioningMethod );

  /**
   * Calling this method will alert the step that we finished passing a batch of records to the step. Specifically for
   * steps like "Sort Rows" it means that the buffered rows can be sorted and passed on.
   *
   * @throws KettleException
   *           In case an error occurs during the processing of the batch of rows.
   */
  void batchComplete() throws KettleException;

  /**
   * Pass along the metastore to use when loading external elements at runtime.
   *
   * @param metaStore
   *          The metastore to use
   */
  void setMetaStore( IMetaStore metaStore );

  /**
   * @return The metastore that the step uses to load external elements from.
   */
  IMetaStore getMetaStore();

  /**
   * @param repository
   *          The repository used by the step to load and reference Kettle objects with at runtime
   */
  void setRepository( Repository repository );

  /**
   * @return The repository used by the step to load and reference Kettle objects with at runtime
   */
  Repository getRepository();

  /**
   * @return the index of the active (current) output row set
   */
  int getCurrentOutputRowSetNr();

  /**
   * @param index
   *          Sets the index of the active (current) output row set to use.
   */
  void setCurrentOutputRowSetNr( int index );

  /**
   * @return the index of the active (current) input row set
   */
  int getCurrentInputRowSetNr();

  /**
   * @param index
   *          Sets the index of the active (current) input row set to use.
   */
  void setCurrentInputRowSetNr( int index );

  default Collection<StepStatus> subStatuses() {
    return Collections.emptyList();
  }

  default void addRowSetToInputRowSets( RowSet rowSet ) {
    getInputRowSets().add( rowSet );
  }

  default void addRowSetToOutputRowSets( RowSet rowSet ) {
    getOutputRowSets().add( rowSet );
  }

}
