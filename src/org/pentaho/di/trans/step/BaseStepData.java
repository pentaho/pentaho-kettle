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

import org.pentaho.di.i18n.BaseMessages;

/**
 * This class is the base class for the StepDataInterface and 
 * contains the methods to set and retrieve the status of the step data.
 * 
 * @author Matt
 * @since 20-jan-2005
 */
public class BaseStepData
{
	private static Class<?> PKG = BaseStep.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public enum StepExecutionStatus {
		
		STATUS_EMPTY(BaseMessages.getString(PKG, "BaseStep.status.Empty")), 
		STATUS_INIT(BaseMessages.getString(PKG, "BaseStep.status.Init")), 
		STATUS_RUNNING(BaseMessages.getString(PKG, "BaseStep.status.Running")), 
		STATUS_IDLE(BaseMessages.getString(PKG, "BaseStep.status.Idle")), 
		STATUS_FINISHED(BaseMessages.getString(PKG, "BaseStep.status.Finished")), 
		STATUS_STOPPED(BaseMessages.getString(PKG, "BaseStep.status.Stopped")), 
		STATUS_DISPOSED(BaseMessages.getString(PKG, "BaseStep.status.Disposed")), 
		STATUS_HALTED(BaseMessages.getString(PKG, "BaseStep.status.Halted")), 
		STATUS_PAUSED(BaseMessages.getString(PKG, "BaseStep.status.Paused")), 
		STATUS_HALTING(BaseMessages.getString(PKG, "BaseStep.status.Halting"));
		
		private String	description;
		private StepExecutionStatus(String description) {
			this.description = description;
		}
		
		public String getDescription() {
			return description;
		}

		@Override
		public String toString() {
			return description;
		}
	}

	private StepExecutionStatus status;

	/**
	 * 
	 */
	public BaseStepData()
	{
		status = StepExecutionStatus.STATUS_EMPTY;
	}
	
	/**
	 * Set the status of the step data.
	 * @param status the new status.
	 */
	public void setStatus(StepExecutionStatus status)
	{
		this.status = status;
	}
	
	/**
	 * Get the status of this step data.
	 * @return the status of the step data
	 */
	public StepExecutionStatus getStatus()
	{
		return status;
	}

	public boolean isEmpty()         { return status == StepExecutionStatus.STATUS_EMPTY;    }
	public boolean isInitialising()  { return status == StepExecutionStatus.STATUS_INIT;     }
	public boolean isRunning()       { return status == StepExecutionStatus.STATUS_RUNNING;  }
	public boolean isIdle()          { return status == StepExecutionStatus.STATUS_IDLE;     }
	public boolean isFinished()      { return status == StepExecutionStatus.STATUS_FINISHED; }
    public boolean isStopped()       { return status == StepExecutionStatus.STATUS_STOPPED;  }
	public boolean isDisposed()      { return status == StepExecutionStatus.STATUS_DISPOSED; }

}
