/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import java.util.Comparator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.AcceptAllFilter;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.deferred.ConcurrentTableUpdator.Range;

/**
 * Contains the algorithm for performing background sorting and filtering in a virtual
 * table. This is the real implementation for <code>DeferredContentProvider</code>.
 * However, this class will work with anything that implements <code>AbstractVirtualTable</code>
 * rather than being tied to a <code>TableViewer</code>.
 * 
 * <p>
 * This is package visiblity since it currently only needs to be used in one place,
 * but it could potentially be made public if there was a need to use the same background
 * sorting algorithm for something other than a TableViewer. 
 * </p>
 * 
 * <p>
 * Information flow is like this:
 * </p>
 * <ol>
 * <li>IConcurrentModel sends unordered elements to BackgroundContentProvider (in a background thread)</li>
 * <li>BackgroundContentProvider sorts, filters, and sends element/index pairs to
 *     ConcurrentTableUpdator (in a background thread)</li>
 * <li>ConcurrentTableUpdator batches the updates and sends them to an AbstractVirtualTable 
 *     (in the UI thread)</li>  
 * </ol>
 * 
 * <p>
 * Internally, sorting is done using a <code>LazySortedCollection</code>. This data structure
 * allows the content provider to locate and sort the visible range without fully sorting
 * all elements in the table. It also supports fast cancellation, allowing the visible range
 * to change in the middle of a sort without discarding partially-sorted information from
 * the previous range.
 * </p>
 * 
 * @since 1.0
 */
/* package */ final class BackgroundContentProvider implements Serializable {
    
	/**
	 * Sorting message string
	 */
    private static final String SORTING = JFaceResources.getString("Sorting"); //$NON-NLS-1$

    /**
     * Table limit. -1 if unlimited
     */
	private int limit = -1;
	
	/**
	 * Model that is currently providing input to this content provider.
	 */
    private IConcurrentModel model;
    
    /**
     * Current sort order 
     */
    private volatile Comparator sortOrder;
    
    /**
     * True iff the content provider has 
     */
    private volatile IFilter filter = AcceptAllFilter.getInstance();
    
    /**
     * Queued changes
     */
    private ChangeQueue changeQueue = new ChangeQueue();
    
    /**
     * Listener that gets callbacks from the model
     */
    private IConcurrentModelListener listener = new IConcurrentModelListener() {
    	/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.deferred.IConcurrentModelListener#add(java.lang.Object[])
		 */
		public void add(Object[] added) {
			BackgroundContentProvider.this.add(added);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.deferred.IConcurrentModelListener#remove(java.lang.Object[])
		 */
		public void remove(Object[] removed) {
			BackgroundContentProvider.this.remove(removed);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.deferred.IConcurrentModelListener#setContents(java.lang.Object[])
		 */
		public void setContents(Object[] newContents) {
			BackgroundContentProvider.this.setContents(newContents);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.deferred.IConcurrentModelListener#update(java.lang.Object[])
		 */
		public void update(Object[] changed) {
			BackgroundContentProvider.this.update(changed);
		}
		
    };

    /**
     * Object that posts updates to the UI thread. Must synchronize on
     * sortMutex when accessing. 
     */
    private ConcurrentTableUpdator updator;
    
    private IProgressMonitor sortingProgressMonitor = new NullProgressMonitor();
    private Thread sortThread = null;

	private volatile FastProgressReporter sortMon = new FastProgressReporter();

	private volatile Range range = new Range(0,0);
    
    /**
     * Creates a new background content provider
     *  
     * @param table table that will receive updates
     * @param model data source
     * @param sortOrder initial sort order
     */
    public BackgroundContentProvider(AbstractVirtualTable table, 
            IConcurrentModel model, Comparator sortOrder) {
        
        updator = new ConcurrentTableUpdator(table);
        this.model = model;
        this.sortOrder = sortOrder;
        model.addListener(listener);
    }
    
    /**
     * Cleans up this content provider, detaches listeners, frees up memory, etc. 
     * Must be the last public method called on this object.
     */
    public void dispose() {
        cancelSortJob();
        updator.dispose();
        model.removeListener(listener);
    }
    
    /**
     * Force a refresh. Asks the model to re-send its complete contents.
     */
    public void refresh() {
    	if (updator.isDisposed()) {
    		return;
    	}
        model.requestUpdate(listener);
    }

    /**
     * Called from sortJob. Sorts the elements defined by sortStart and sortLength.
     * Schedules a UI update when finished.
     * 
     * @param mon monitor where progress will be reported
     */
    private void doSort(IProgressMonitor mon) {        
        
        // Workaround for some weirdness in the Jobs framework: if you cancel a monitor
        // for a job that has ended and reschedule that same job, it will start 
        // the job with a monitor that is already cancelled. We can workaround this by
        // removing all references to the progress monitor whenever the job terminates,
        // but this would require additional synchronize blocks (which are slow) and more
        // complexity. Instead, we just un-cancel the monitor at the start of each job. 
        mon.setCanceled(false);

       	mon.beginTask(SORTING, 100);
       	
        // Create a LazySortedCollection
        Comparator order = sortOrder;
        IFilter f = filter;
        LazySortedCollection collection = new LazySortedCollection(order);
        
        // Fill it in with all existing known objects
        Object[] knownObjects = updator.getKnownObjects();
        for (int i = 0; i < knownObjects.length; i++) {
			Object object = knownObjects[i];
			if (object != null) {
				collection.add(object);
			}
		}

        boolean dirty = false;
        int prevSize = knownObjects.length;
        updator.setTotalItems(prevSize);
        
		// Start processing changes
        while(true) {
        	// If the sort order has changed, build a new LazySortedCollection with
        	// the new comparator
        	if (order != sortOrder) {
        		dirty = true;
        		order = sortOrder;
        		// Copy all elements from the old collection to the new one 
        		LazySortedCollection newCollection = new LazySortedCollection(order);
        		
        		Object[] items = collection.getItems(false);
        		for (int j = 0; j < items.length && order == sortOrder; j++) {
					Object item = items[j];
					
					newCollection.add(item);
				}
        		
        		// If the sort order changed again, re-loop
				if (order != sortOrder) {
					continue;
				}
				collection = newCollection;
				continue;
        	} 
        	
        	// If the filter has changed
        	if (f != filter) {
        		dirty = true;
        		f = filter;
        		
        		Object[] items = collection.getItems(false);
        		
        		// Remove any items that don't pass the new filter
        		for (int j = 0; j < items.length && f == filter; j++) {
					Object toTest = items[j];
					
					if (!f.select(toTest)) {
						collection.remove(toTest);
					}
				}
        		continue;
        	}
        
        	// If there are pending changes, process one of them
        	if (!changeQueue.isEmpty()) {
        		dirty = true;
	        	ChangeQueue.Change next = changeQueue.dequeue();
	        	
	        	switch(next.getType()) {
		        	case ChangeQueue.ADD: {
		            	filteredAdd(collection, next.getElements(), f);
		        		break;
		        	}
		        	case ChangeQueue.REMOVE: {
		        		Object[] toRemove = next.getElements();
	
		                flush(toRemove, collection);
		                collection.removeAll(toRemove);
	
		        		break;
		        	}
		        	case ChangeQueue.UPDATE: {
		        		Object[] items  = next.getElements();
		        		
	        	        for (int i = 0; i < items.length; i++) {
	        	            Object item = items[i];
	        	            
	        	            if (collection.contains(item)) {
	        	                // TODO: write a collection.update(...) method
	        	                collection.remove(item);
	        	                collection.add(item);
	        	                updator.clear(item);        	                
	        	            }
	        	        }
		        	        
		        		break;
		        	}
		        	case ChangeQueue.SET: {
		        		Object[] items = next.getElements();
		        		collection.clear();
		        		filteredAdd(collection, items, f);
		        	        
		        		break;
		        	}
	        	}
	        	
	        	continue;
        	}
        	
	        int totalElements = collection.size();
            if (limit != -1) {
                if (totalElements > limit) {
                    totalElements = limit;
                }
            }
            
            if (totalElements != prevSize) {
            	prevSize = totalElements;
	            // Send the total items to the updator ASAP -- the user may want
	            // to scroll to a different section of the table, which would
	            // cause our sort range to change and cause this job to get cancelled.
		        updator.setTotalItems(totalElements);
		        dirty = true;
            }
            
            // Terminate loop
            if (!dirty) {
            	break;
            }
        	
            try {
            	ConcurrentTableUpdator.Range updateRange = updator.getVisibleRange();
            	sortMon = new FastProgressReporter();
            	range = updateRange;
            	int sortStart = updateRange.start;
            	int sortLength = updateRange.length;
            
		        if (limit != -1) {
		            collection.retainFirst(limit, sortMon);
		        }

		        sortLength = Math.min(sortLength, totalElements - sortStart);
		        sortLength = Math.max(sortLength, 0);
		        
		        Object[] objectsOfInterest = new Object[sortLength];
		     
		        collection.getRange(objectsOfInterest, sortStart, true, sortMon);
		        
		        // Send the new elements to the table
		        for (int i = 0; i < sortLength; i++) {
					Object object = objectsOfInterest[i];
					updator.replace(object, sortStart + i);
				}
		        
			    objectsOfInterest = new Object[collection.size()];
			        
			    collection.getFirst(objectsOfInterest, true, sortMon);
		        
		        // Send the new elements to the table
		        for (int i = 0; i < totalElements; i++) {
					Object object = objectsOfInterest[i];
					updator.replace(object, i);
				}

            } catch (InterruptedException e) {
            	continue;
            }
            
            dirty = false;
	    }
        
        mon.done();
    }

	/**
	 * @param collection
	 * @param toAdd
	 */
	private static void filteredAdd(LazySortedCollection collection, Object[] toAdd, IFilter filter) {
		if (filter != AcceptAllFilter.getInstance()) { 
			for (int i = 0; i < toAdd.length; i++) {
				Object object = toAdd[i];
				
				if (filter.select(object)) {
					collection.add(object);
				}
			}
		} else {
			collection.addAll(toAdd);
		}
	}
    
    /**
     * Sets the sort order for this content provider
     * 
     * @param sorter sort order
     */
    public void setSortOrder(Comparator sorter) {
    	Assert.isNotNull(sorter);
        this.sortOrder = sorter;
    	sortMon.cancel();
        refresh();
    }
    
    /**
     * Sets the filter for this content provider
     * 
     * @param toSet filter to set
     */
    public void setFilter(IFilter toSet) {
    	Assert.isNotNull(toSet);
    	this.filter = toSet;
    	sortMon.cancel();
    	refresh();
    }
    
    /**
     * Sets the maximum table size. Based on the current sort order,
     * the table will be truncated if it grows beyond this size.
     * Using a limit improves memory usage and performance, and is
     * strongly recommended for large tables. 
     * 
     * @param limit maximum rows to show in the table or -1 if unbounded
     */
    public void setLimit(int limit) {
        this.limit = limit;
        refresh();
    }
    
    /**
     * Returns the maximum table size or -1 if unbounded
     * 
     * @return the maximum number of rows in the table or -1 if unbounded
     */
    public int getLimit() {
        return limit;
    }
    
    /**
     * Checks if currently visible range has changed, and triggers and update
     * and resort if necessary. Must be called in the UI thread, typically
     * within a SWT.SetData callback.
     * @param includeIndex the index that should be included in the visible range.
     */
    public void checkVisibleRange(int includeIndex) {
    	updator.checkVisibleRange(includeIndex);
		ConcurrentTableUpdator.Range newRange = updator.getVisibleRange();
		ConcurrentTableUpdator.Range oldRange = range;
		
		// If we're in the middle of processing an invalid range, cancel the sort
		if (newRange.start != oldRange.start || newRange.length != oldRange.length) {
			sortMon.cancel();
		}
    }
    
    /**
     * This lock protects the two boolean variables sortThreadStarted and resortScheduled.
     */
    private Object lock = new Object();

    /**
     * true if the sort thread is running
     */
    private boolean sortThreadStarted = false;

    /**
     * true if we need to sort
     */
    private boolean sortScheduled = false;
    
	private final class SortThread extends Thread {
		private SortThread(String name) {
			super(name);
		}

		public void run() {
			loop: while (true) {
				synchronized (lock) {
					sortScheduled = false;
				}
				try {
					// this is the main work
					doSort(sortingProgressMonitor);
				} catch (Exception ex) {
					// ignore
				}
				synchronized (lock) {
					if (sortScheduled) {
						continue loop;
					}
					sortThreadStarted = false;
					break loop;
				}
			}
		}
	}
    
    /**
     * Must be called whenever the model changes. Dirties this object and triggers a sort
     * if necessary. 
     */
    private void makeDirty() {
		synchronized (lock) {
			sortMon.cancel();
			// request sorting
			sortScheduled = true;
			if (!sortThreadStarted) {
				sortThreadStarted = true;
				sortThread = new SortThread(SORTING);
				sortThread.setDaemon(true);
				sortThread.setPriority(Thread.NORM_PRIORITY - 1);
				sortThread.start();
			}
		}
	}
    
    /**
	 * Cancels any sort in progress. Note that we try to use the
	 * FastProgresReporter if possible since this is more responsive than
	 * cancelling the sort job. However, it is not a problem to cancel in both
	 * ways.
	 */
    private void cancelSortJob() {
        sortMon.cancel();
        sortingProgressMonitor.setCanceled(true);
    }
    
    /**
	 * Called when new elements are added to the model.
	 * 
	 * @param toAdd
	 *            newly added elements
	 */
    private void add(Object[] toAdd) {
    	changeQueue.enqueue(ChangeQueue.ADD, toAdd);
    	makeDirty();
    }
    
    /**
     * Called with the complete contents of the model
     * 
     * @param contents new contents of the model
     */
    private void setContents(Object[] contents) {
    	changeQueue.enqueue(ChangeQueue.SET, contents);
    	makeDirty();
    }

    /**
     * Called when elements are removed from the model
     * 
     * @param toRemove elements removed from the model
     */
    private void remove(Object[] toRemove) {
        changeQueue.enqueue(ChangeQueue.REMOVE, toRemove);
        makeDirty();
        refresh();
    }

    /**
     * Notifies the updator that the given elements have changed 
     * 
     * @param toFlush changed elements
     * @param collection collection of currently-known elements
     */
    private void flush(Object[] toFlush, LazySortedCollection collection) {
        for (int i = 0; i < toFlush.length; i++) {
            Object item = toFlush[i];
            
            if (collection.contains(item)) {
                updator.clear(item);
            }
        }
    }


    /**
     * Called when elements in the model change
     * 
     * @param items changed items
     */
    private void update(Object[] items) {
        changeQueue.enqueue(ChangeQueue.UPDATE, items);
        makeDirty();
    }
}
