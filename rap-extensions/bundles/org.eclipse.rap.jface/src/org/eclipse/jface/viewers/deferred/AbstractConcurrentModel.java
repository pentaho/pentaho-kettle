/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers.deferred;

import org.eclipse.core.runtime.ListenerList;


/**
 * Abstract base class for all IConcurrentModel implementations. Clients should
 * subclass this class instead of implementing IConcurrentModel directly. 
 * 
 * @since 1.0
 */
public abstract class AbstractConcurrentModel implements
        IConcurrentModel {

    private ListenerList listeners = new ListenerList(); 
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.deferred.IConcurrentContentProvider#addListener(org.eclipse.jface.viewers.deferred.IConcurrentContentProviderListener)
     */
    public void addListener(IConcurrentModelListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Fires an add notification to all listeners
     * 
     * @param added objects added to the set
     */
    protected final void fireAdd(Object[] added) {
        Object[] listenerArray = listeners.getListeners();
        
        for (int i = 0; i < listenerArray.length; i++) {
            IConcurrentModelListener next = (IConcurrentModelListener) listenerArray[i];
            
            next.add(added);
        }
    }

    /**
     * Fires a remove notification to all listeners
     * 
     * @param removed objects removed from the set
     */
    protected final void fireRemove(Object[] removed) {
        Object[] listenerArray = listeners.getListeners();
        
        for (int i = 0; i < listenerArray.length; i++) {
            IConcurrentModelListener next = (IConcurrentModelListener) listenerArray[i];
            
            next.remove(removed);
        }
    }
    
    /**
     * Fires an update notification to all listeners
     * 
     * @param updated objects that have changed
     */
    protected final void fireUpdate(Object[] updated) {
        Object[] listenerArray = listeners.getListeners();
        
        for (int i = 0; i < listenerArray.length; i++) {
            IConcurrentModelListener next = (IConcurrentModelListener) listenerArray[i];
            
            next.update(updated);
        }
    }
    
    /**
     * Returns the array of listeners for this model
     * 
     * @return the array of listeners for this model 
     */
    protected final IConcurrentModelListener[] getListeners() {
    	Object[] l = listeners.getListeners();
    	IConcurrentModelListener[] result = new IConcurrentModelListener[l.length];
    	
    	for (int i = 0; i < l.length; i++) {
			result[i] = (IConcurrentModelListener)l[i];
		}
    	
    	return result;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.deferred.IConcurrentContentProvider#removeListener(org.eclipse.jface.viewers.deferred.IConcurrentContentProviderListener)
     */
    public void removeListener(IConcurrentModelListener listener) {
        listeners.remove(listener);
    }
}
