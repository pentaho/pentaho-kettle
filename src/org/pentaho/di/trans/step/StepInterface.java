 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.trans.step;

import java.util.List;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;

/*
 * Created on 12-aug-2004
 *
 * @author Matt
 *
 */

public interface StepInterface
{
	/**
	 * Initialise and do work where other steps need to wait for...
	 * @param stepMetaInterface The metadata to work with
	 * @param stepDataInterface The data to initialize
	 */
	public boolean init(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface);
	
	/**
	 * Dispose of this step: close files, empty logs, etc. 
	 * @param sii The metadata to work with
	 * @param sdi The data to dispose of

	 */
	public void dispose(StepMetaInterface sii, StepDataInterface sdi);

	/**
	 * Mark the start time of the step. 
	 *
	 */
	public void markStart();
	
	/**
	 * Mark the end time of the step. 
	 *
	 */
	public void markStop();

	/**
	 * Starts the thread...
	 *
	 */
	public void     start();
	
	/**
	 * Stop running operations...
     * @param stepMetaInterface The metadata that might be needed by the step to stop running.
	 * @param stepDataInterface The interface to the step data containing the connections, resultsets, open files, etc.  
	 *
	 */
	public void stopRunning(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface) throws KettleException;
    
	/**
	 * Is the thread still alive?
	 * @return true if the thread is still alive...
	 */
	public boolean isAlive();

	/**
	 * @return True if the step is marked as stopped. Execution should stop immediate.
	 */
	public boolean isStopped();
	
	/**
	 * Flags all rowsets as stopped/completed/finished. 
	 */
	public void stopAll();

	/**
	 * Pause a running step
	 */
	public void pauseRunning();

	/**
	 * Resume a running step
	 */
	public void resumeRunning();

	/**
	 *  Run is where the action happens in a step...
	 */
	public void run();
	
	/**
	 * Get the name of the step.
	 * @return the name of the step
	 */
	public String getStepname();
    
    /**
     * @return the type ID of the step...
     */
    public String getStepID();
	
	/**
	 * Get the number of errors
	 * @return the number of errors
	 */
	public long getErrors();

	/**
	 * Sets the number of errors
	 * @param errors the number of errors to set
	 */
	public void setErrors(long errors);
	
	/**
     * @return Returns the linesInput.
     */
    public long getLinesInput();
    
    /**
     * @return Returns the linesOutput.
     */
    public long getLinesOutput();
    
    /**
     * @return Returns the linesRead.
     */
    public long getLinesRead();
    
    /**
     * @return Returns the linesWritten.
     */
    public long getLinesWritten();
    
    /**
     * @return Returns the linesUpdated.
     */
    public long getLinesUpdated();

	/**
	 * Process one row.
	 * @param smi The metadata to work with
	 * @param sdi The temporary working data to work with (database connections, resultsets, caches, temporary variables, etc.)
	 * @return false if no more rows can be processed or an error occurred.
	 * @throws KettleException
	 */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException;
    
    /**
     * Put a row on the destination rowsets.
     * @param row The row to send to the destinations steps
     */
    public void putRow(RowMetaInterface row, Object data[]) throws KettleException;
    
    /**
     * @return a row from the source step(s).
     */
    public Object[] getRow() throws KettleException;
    
    /**
     * Signal output done to destination steps
     */
    public void setOutputDone();
    
    /**
     * Add a rowlistener to the step allowing you to inspect (or manipulate, be careful) the rows coming in or exiting the step.
     * @param rowListener the rowlistener to add
     */
    public void addRowListener(RowListener rowListener);
    
    /**
     * Remove a rowlistener from this step.
     * @param rowListener the rowlistener to remove
     */
    public void removeRowListener(RowListener rowListener);

    /**
     * @return a list of the installed RowListeners
     */
    public List<RowListener> getRowListeners();
    
    /**
     * @return The list of active input rowsets for the step
     */
    public List<RowSet> getInputRowSets();

    /**
     * @return The list of active output rowsets for the step
     */
    public List<RowSet> getOutputRowSets();

    /**
     * @return true if the step is running partitioned
     */
    public boolean isPartitioned();
    
    /**
     * @param partitionID the partitionID to set
     */
    public void setPartitionID(String partitionID);

    /**
     * @return the steps partition ID
     */
    public String getPartitionID();

    /**
     * Call this method typically, after ALL the slave transformations in a clustered run have finished.
     */
	public void cleanup();
}
