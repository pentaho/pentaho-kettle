/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
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
