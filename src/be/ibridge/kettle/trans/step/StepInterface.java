 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.trans.step;

import java.util.List;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;

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
	 *
	 */
	public void     stopRunning();

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
	 *  Run is where the action happens in a step...
	 */
	public void     run();
	
	/**
	 * Get the name of the step.
	 * @return the name of the step
	 */
	public String   getStepname();
    
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
    public void putRow(Row row);
    
    /**
     * @return a row from the source step(s).
     */
    public Row getRow();
    
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
    public List getRowListeners();
}
