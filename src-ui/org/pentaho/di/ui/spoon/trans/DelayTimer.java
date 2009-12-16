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
