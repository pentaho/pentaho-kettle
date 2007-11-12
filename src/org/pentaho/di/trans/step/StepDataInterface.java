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

/**
 * This defines the basic interface for the data used by a thread.
 * This will allow us to stop execution of threads and restart them later on 
 * without loosing track of the situation.
 * Typically the StepDataInterface implementing class will contain resultsets, 
 * temporary data, caching indexes, etc.
 * 
 * @author Matt
 * @since 20-jan-2005
 */
public interface StepDataInterface
{
	public static final int STATUS_EMPTY         = 0;
	public static final int STATUS_INIT          = 1; // Initialising step
	public static final int STATUS_RUNNING       = 2; // Running
	public static final int STATUS_IDLE          = 3; // Waiting to be run, after init
	public static final int STATUS_FINISHED      = 4; // finished after running
    public static final int STATUS_STOPPED       = 5; // Stopped because of user request of error
	public static final int STATUS_DISPOSED      = 6; // cleaned out, step is gone
    public static final int STATUS_HALTED        = 7; // Not launching because init failed 
	public static final int STATUS_PAUSED        = 8; // Paused
    
	public void setStatus(int status);
	public int getStatus();
	
	public boolean isEmpty();
	public boolean isInitialising();
	public boolean isRunning();
	public boolean isIdle();
	public boolean isFinished();
	public boolean isDisposed();
}
