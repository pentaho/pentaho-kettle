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
 * This class is the base class for the StepDataInterface and 
 * contains the methods to set and retrieve the status of the step data.
 * 
 * @author Matt
 * @since 20-jan-2005
 */
public class BaseStepData
{
	private int status;

	/**
	 * 
	 */
	public BaseStepData()
	{
		status = StepDataInterface.STATUS_EMPTY;
	}
	
	/**
	 * Set the status of the step data.
	 * @param status the new status.
	 */
	public void setStatus(int status)
	{
		this.status = status;
	}
	
	/**
	 * Get the status of this step data.
	 * @return the status of the step data
	 */
	public int getStatus()
	{
		return status;
	}

	public boolean isEmpty()         { return status == StepDataInterface.STATUS_EMPTY;    }
	public boolean isInitialising()  { return status == StepDataInterface.STATUS_INIT;     }
	public boolean isRunning()       { return status == StepDataInterface.STATUS_RUNNING;  }
	public boolean isIdle()          { return status == StepDataInterface.STATUS_IDLE;     }
	public boolean isFinished()      { return status == StepDataInterface.STATUS_FINISHED; }
    public boolean isStopped()       { return status == StepDataInterface.STATUS_STOPPED;  }
	public boolean isDisposed()      { return status == StepDataInterface.STATUS_DISPOSED; }

}
