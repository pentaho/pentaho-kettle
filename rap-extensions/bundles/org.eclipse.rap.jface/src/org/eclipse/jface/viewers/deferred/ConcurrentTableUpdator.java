/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers.deferred;

import java.io.Serializable;



/**
 * Allows a table to be accessed from a background thread. Provides a table-like public 
 * interface that can accessed from a background thread. As updates arrive from the 
 * background thread, it batches and schedules updates to the real table in the UI thread. 
 * This class can be used with any widget that can be wrapped in the 
 * <code>AbstractVirtualTable</code> interface.  
 * 
 * @since 1.0
 */
/* package */ final class ConcurrentTableUpdator implements Serializable {
	/**
	 * Wrapper for the real table. May only be accessed in the UI thread.
	 */
    private AbstractVirtualTable table;
    
    /**
     * The array of objects that have been sent to the UI. Elements are null
     * if they either haven't been sent yet or have been scheduled for clear. 
     * Maps indices onto elements.
     */
    private Object[] sentObjects = new Object[0];
    
    /**
     * Map of elements to object indices (inverse of the knownObjects array)
     */
    private IntHashMap knownIndices = new IntHashMap();
    
    /**
     * Contains all known objects that have been sent here from the background
     * thread.
     */
    private Object[] knownObjects = new Object[0];
    
    // Minimum length for the pendingFlushes stack
    private static final int MIN_FLUSHLENGTH = 64;
    
    /**
     * Array of element indices. Contains elements scheduled to be
     * cleared. Only the beginning of the array is used. The number
     * of used elements is stored in lastClear
     */
    private int[] pendingClears = new int[MIN_FLUSHLENGTH];
    
    /**
     * Number of pending clears in the pendingClears array (this is normally
     * used instead of pendingClears.length since the 
     * pendingClears array is usually larger than the actual number of pending
     * clears) 
     */
    private int lastClear = 0;
    
    /**
     * Last known visible range
     */    
    private volatile Range lastRange = new Range(0,0);
    
    /**
     * True iff a UI update has been scheduled
     */
    private volatile boolean updateScheduled;
    
    /**
     * True iff this object has been disposed
     */
    private volatile boolean disposed = false;
    
    /**
     * Object that holds a start index and length. Allows
     * the visible range to be returned as an atomic operation.
     */
    public final static class Range {
        int start = 0;
        int length = 0;
        
        /**
         * @param s
         * @param l
         */
        public Range(int s, int l) {
            start = s;
            length = l;
        }
    }
    
    /**
     * Runnable that can be posted with an asyncExec to schedule
     * an update to the real table.  
     */
    Runnable uiRunnable = new Runnable() {
        public void run() {
            updateScheduled = false;
            if(!table.getControl().isDisposed()) {
				updateTable();
			}
        }
    };
    
    /**
     * Creates a new table updator
     * 
     * @param table real table to update
     */
    public ConcurrentTableUpdator(AbstractVirtualTable table) {
        this.table = table;
    }
    
    /**
     * Cleans up the updator object (but not the table itself).
     */
    public void dispose() {
    	disposed = true;
    }
    
    /**
     * True iff this object has been disposed. 
     * 
     * @return true iff dispose() has been called
     */
    public boolean isDisposed() {
    	return disposed;
    }
    
    /**
     * Returns the currently visible range
     * 
     * @return the currently visible range
     */
    public Range getVisibleRange() {
    	return lastRange;
    }
    
    /**
     * Marks the given object as dirty. Will cause it to be cleared
     * in the table. 
     * 
     * @param toFlush
     */
    public void clear(Object toFlush) {
        synchronized(this) {
            int currentIdx = knownIndices.get(toFlush, -1);

            // If we've never heard of this object, bail out.
            if (currentIdx == -1) {
                return;
            }

            pushClear(currentIdx);
        }
        
    }
    
    /**
     * Sets the size of the table. Called from a background thread.
     * 
     * @param newTotal
     */
    public void setTotalItems(int newTotal) {
        synchronized (this) {
            if (newTotal != knownObjects.length) {
                if (newTotal < knownObjects.length) {
                    // Flush any objects that are being removed as a result of the resize
                    for (int i = newTotal; i < knownObjects.length; i++) {
                        Object toFlush = knownObjects[i];
                        
                        if (toFlush != null) {
                        	knownIndices.remove(toFlush);
                        }
                    }
                }
                
                int minSize = Math.min(knownObjects.length, newTotal);
                
	            Object[] newKnownObjects = new Object[newTotal];
	            System.arraycopy(knownObjects, 0, newKnownObjects, 0, minSize);
	            knownObjects = newKnownObjects;
	            	            
	            scheduleUIUpdate();
            }
        }
    }
    
    /**
     * Pushes an index onto the clear stack
     * 
     * @param toClear row to clear
     */
    private void pushClear(int toClear) {
        
    	// If beyond the end of the table
    	if (toClear >= sentObjects.length) {
    		return;
    	}
    	
    	// If already flushed or never sent
        if (sentObjects[toClear] == null) {
        	return;            
        }

        // Mark as flushed
        sentObjects[toClear] = null;
    	
        if (lastClear >= pendingClears.length) {
            int newCapacity = Math.min(MIN_FLUSHLENGTH, lastClear * 2);
            int[] newPendingClears = new int[newCapacity];
            System.arraycopy(pendingClears, 0, newPendingClears, 0, lastClear);
            pendingClears = newPendingClears;
        }
        
        pendingClears[lastClear++] = toClear;
    }
    
    /**
     * Sets the item on the given row to the given value. May be called from a background
     * thread. Schedules a UI update if necessary
     * 
     * @param idx row to change
     * @param value new value for the given row
     */
    public void replace(Object value, int idx) {        
        // Keep the synchronized block as small as possible, since the UI may
        // be waiting on it.
        synchronized(this) {
            Object oldObject = knownObjects[idx];
            
            if (oldObject != value) {
            	if (oldObject != null) {
            		knownIndices.remove(oldObject);
            	}
            	
                knownObjects[idx] = value;
                
            	if (value != null) {
	        		int oldIndex = knownIndices.get(value, -1);
	        		if (oldIndex != -1) {
	        			knownObjects[oldIndex] = null;
	        			pushClear(oldIndex);
	        		}
	        		
	        		knownIndices.put(value, idx);
            	}
                
                pushClear(idx);
                
                scheduleUIUpdate();
            }
        } 
    }

    /**
     * Schedules a UI update. Has no effect if an update has already been
     * scheduled.
     */
    private void scheduleUIUpdate() {
        synchronized(this) {
	        if (!updateScheduled) {
	            updateScheduled = true;
	            if(!table.getControl().isDisposed()) {
					table.getControl().getDisplay().asyncExec(uiRunnable);
				}
	        }
        }
    }
    
    
    /**
     * Called in the UI thread by a SetData callback. Refreshes the
     * table if necessary. Returns true iff a refresh is needed.
     * @param includeIndex the index that should be included in the visible range.
     */
    public void checkVisibleRange(int includeIndex) {
        int start = Math.min(table.getTopIndex() - 1, includeIndex);
        int length = Math.max(table.getVisibleItemCount(), includeIndex - start);
        Range r = lastRange;

    	if (start != r.start || length != r.length) {
    		updateTable();
    	}
    }
    
    /**
     * Updates the table. Sends any unsent items in the visible range to the table,
     * and clears any previously-visible items that have not yet been sent to the table.
     * Must be called from the UI thread.
     */
    private void updateTable() {    	
        
        synchronized(this) {

        	// Resize the table if necessary
	        if (sentObjects.length != knownObjects.length) {
	        	Object[] newSentObjects = new Object[knownObjects.length];
	        	System.arraycopy(newSentObjects, 0, sentObjects, 0, 
	        			Math.min(newSentObjects.length, sentObjects.length));
	        	sentObjects = newSentObjects;
	            table.setItemCount(newSentObjects.length);
	        }

	        // Compute the currently visible range
	        int start = Math.min(table.getTopIndex(), knownObjects.length);
	        int length = Math.min(table.getVisibleItemCount(), knownObjects.length - start);
	        int itemCount = table.getItemCount();
            
        	int oldStart = lastRange.start;
        	int oldLen = lastRange.length;
        	
        	// Store the visible range. Do it BEFORE sending any table.clear calls,
        	// since clearing a visible row will result in a SetData callback which
        	// cause another table update if the visible range is different from
        	// the stored values -- this could cause infinite recursion.
        	lastRange = new Range(start, length);
        	
			// Re-clear any items in the old range that were never filled in
			for(int idx = 0; idx < oldLen; idx++) {
				int row = idx + oldStart;
				
				// If this item is no longer visible
				if (row < itemCount && (row < start || row >= start + length)) {
					
					// Note: if we wanted to be really aggressive about clearing
					// items that are no longer visible, we could clear here unconditionally.
					// The current way of doing things won't clear a row if its contents are
					// up-to-date.
					if (sentObjects[row] == null) {
						table.clear(row);
					}
				}
			}
			
			// Process any pending clears
	        if (lastClear > 0) {
				for (int i = 0; i < lastClear; i++) {
					int row = pendingClears[i];
		
					if (row < sentObjects.length) {
						table.clear(row);
					}
				}
	
				if (pendingClears.length > MIN_FLUSHLENGTH) {
					pendingClears = new int[MIN_FLUSHLENGTH];
				}
				lastClear = 0;
	        }
		    
	        // Send any unsent items in the visible range
	        for (int idx = 0; idx < length; idx++) {
	        	int row = idx + start;
	        	
	        	Object obj = knownObjects[row];
	        	if (obj != null && obj != sentObjects[idx]) {
	        		table.replace(obj, row);
	        		sentObjects[idx] = obj;
	        	}
	        }
	        
        }
    }

	/**
	 * Return the array of all known objects that have been sent here from the background
     * thread.
	 * @return the array of all known objects
	 */
	public Object[] getKnownObjects() {
		return knownObjects;
	}
    
}
