/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.spoon.trans;

import java.util.ArrayList;
import java.util.List;

/**
 * A timer where you can attach a timer to.
 * Once the time is up, the listeners are fired off.
 * 
 * @author matt
 *
 */
public class DelayTimer implements Runnable {
	
	private int	delayInMiliseconds;
	private boolean stopped;
	
	private List<DelayListener> delayListeners;
	private long	start;

	public DelayTimer(int delayInMiliseconds) {
		this.delayInMiliseconds = delayInMiliseconds;
		this.delayListeners = new ArrayList<DelayListener>();
		
		stopped=false;
	}

	public DelayTimer(int delayInMilliseconds, DelayListener delayListener) {
		this(delayInMilliseconds);
		addDelayListener(delayListener);
	}
	
	public void reset() {
		start = System.currentTimeMillis();
	}
	
	public void run() {
		reset();
		while ( (System.currentTimeMillis()-start)<(delayInMiliseconds) && !stopped ) {
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				// Simply break out of the loop, nothing else
				//
				break;
			}
		}
		// Fire the listeners...
		//
		for (DelayListener delayListener : delayListeners) {
			delayListener.expired();
		}
	}
	
	public void stop() {
		stopped=true;
	}
	
	public void addDelayListener(DelayListener delayListener) {
		delayListeners.add(delayListener);
	}

	/**
	 * @return the delay in milliseconds
	 */
	public int getDelayInMilliseconds() {
		return delayInMiliseconds;
	}

	/**
	 * @param delayInMilliseconds the delay in milliseconds to set
	 */
	public void setDelayInSeconds(int delayInMilliseconds) {
		this.delayInMiliseconds = delayInMilliseconds;
	}

}
