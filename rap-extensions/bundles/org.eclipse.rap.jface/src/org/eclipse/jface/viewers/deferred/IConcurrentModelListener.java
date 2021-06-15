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

import java.io.Serializable;

/**
 * Interface for objects that can listen to changes in an IConcurrentModel.
 * Elements in an IConcurrentModel are unordered.
 * 
 * @since 1.0
 */
public interface IConcurrentModelListener extends Serializable {
	
	/**
	 * Called when elements are added to the model 
	 * 
	 * @param added elements added to the model
	 */
    public void add(Object[] added);
    
    /**
     * Called when elements are removed from the model
     * 
     * @param removed elements removed from the model
     */
    public void remove(Object[] removed);
    
    /**
     * Called when elements in the model have changed
     * 
     * @param changed elements that have changed
     */
    public void update(Object[] changed);
    
    /**
     * Notifies the receiver about the complete set
     * of elements in the model. Most models will
     * not call this method unless the listener explicitly
     * requests it by calling 
     * <code>IConcurrentModel.requestUpdate</code>
     *  
     * @param newContents contents of the model
     */
    public void setContents(Object[] newContents);
}
