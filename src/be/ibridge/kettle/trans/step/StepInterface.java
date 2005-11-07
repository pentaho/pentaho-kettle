 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.trans.step;

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
	public String   getName();
	
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
}
