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
	
	private int	delayInSeconds;
	
	private List<DelayListener> delayListeners;

	public DelayTimer(int delayInSeconds) {
		this.delayInSeconds = delayInSeconds;
		this.delayListeners = new ArrayList<DelayListener>();
	}

	public DelayTimer(int delayInSeconds, DelayListener delayListener) {
		this(delayInSeconds);
		addDelayListener(delayListener);
	}
	
	public void run() {
		long start = System.currentTimeMillis();
		while ( (System.currentTimeMillis()-start)<(delayInSeconds*1000) ) {
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
	
	public void addDelayListener(DelayListener delayListener) {
		delayListeners.add(delayListener);
	}

	/**
	 * @return the delayInSeconds
	 */
	public int getDelayInSeconds() {
		return delayInSeconds;
	}

	/**
	 * @param delayInSeconds the delayInSeconds to set
	 */
	public void setDelayInSeconds(int delayInSeconds) {
		this.delayInSeconds = delayInSeconds;
	}
}
