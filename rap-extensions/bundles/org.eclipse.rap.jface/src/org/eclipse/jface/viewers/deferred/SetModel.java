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

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.runtime.Assert;


/**
 * Trivial implementation of an <code>IConcurrentModel</code>. Implements 
 * an unordered set of elements that fires off change notifications whenever
 * elements are added or removed from the set. All notifications are sent
 * synchronously.
 * 
 * @since 1.0
 */
public class SetModel extends AbstractConcurrentModel {
    
    private HashSet data = new HashSet();
    
    /**
     * Return the contents of the model.
     * @return the array of elements
     * 
     */
    public Object[] getElements() {
        return data.toArray();
    }

    /**
     * Sets the contents to the given array of elements
     * 
     * @param newContents new contents of this set
     */
    public void set(Object[] newContents) {
    	Assert.isNotNull(newContents);
    	data.clear();
        for (int i = 0; i < newContents.length; i++) {
            Object object = newContents[i];
            
            data.add(object);
        }
    	
    	IConcurrentModelListener[] listeners = getListeners();
    	for (int i = 0; i < listeners.length; i++) {
			IConcurrentModelListener listener = listeners[i];
			
			listener.setContents(newContents);
		}
    }

    /**
     * Empties the set
     */
    public void clear() {
        Object[] removed = data.toArray();
        data.clear();
        fireRemove(removed);
    }
    
    /**
     * Adds the given elements to the set
     * 
     * @param toAdd elements to add
     */
    public void addAll(Object[] toAdd) {
    	Assert.isNotNull(toAdd);
        for (int i = 0; i < toAdd.length; i++) {
            Object object = toAdd[i];
            
            data.add(object);
        }
        
        fireAdd(toAdd);
    }
    
    /**
     * Adds the given elements to the set. Duplicate elements are ignored.
     * 
     * @param toAdd elements to add
     */
    public void addAll(Collection toAdd) {
    	Assert.isNotNull(toAdd);
        addAll(toAdd.toArray());
    }
    
    /**
     * Fires a change notification for all elements in the given array
     * 
     * @param changed array of elements that have changed
     */
    public void changeAll(Object[] changed) {
    	Assert.isNotNull(changed);
        fireUpdate(changed);
    }
    
    /**
     * Removes all of the given elements from the set.
     * 
     * @param toRemove elements to remove
     */
    public void removeAll(Object[] toRemove) {
    	Assert.isNotNull(toRemove);
        for (int i = 0; i < toRemove.length; i++) {
            Object object = toRemove[i];
            
            data.remove(object);
        }        
        
        fireRemove(toRemove);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.deferred.IConcurrentModel#requestUpdate(org.eclipse.jface.viewers.deferred.IConcurrentModelListener)
     */
    public void requestUpdate(IConcurrentModelListener listener) {
    	Assert.isNotNull(listener);
        listener.setContents(getElements());
    }
}
