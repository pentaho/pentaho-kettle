/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
 * Interface for a set of unordered elements that can fire change notifications.
 * IConcurrentModel returns its contents asynchronous. Rather than implementing 
 * "get" methods, listeners can request an update and the model fires back
 * information at its earliest convenience.
 * 
 * <p>
 * The model is allowed to send back notifications to its listeners in any thread,
 * and the listeners must not assume that the notifications will arrive in the UI
 * thread.
 * </p>
 * 
 * <p>
 * Not intended to be implemented by clients. Clients should subclass 
 * <code>AbstractConcurrentModel</code> instead.
 * </p>
 * 
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IConcurrentModel extends Serializable {

    /**
     * Requests that the receiver to call the given listener's setContents(...) 
     * method at its earliest convenience. The receiver is allowed to compute the 
     * elements asynchronously. That is, it can compute the result in a background 
     * thread and call setContents(...) once the result is ready. If the result is
     * too large to return in one batch, it can call setContents with an empty array
     * followed by a sequence of adds.
     * <p>
     * Has no effect if an update is already queued for an identical listener.
     * </p>
     * 
     * @param listener listener whose setContents method should be called. The
     * listener must have been previously registered with addListener.
     */
    public void requestUpdate(IConcurrentModelListener listener);
    
    /**
     * Adds a listener to this model. The listener should be given the model's
     * current contents (either through setContents or a sequence of adds) at the
     * receiver's earliest convenience. The receiver will notify the listener
     * about any changes in state until the listener is removed.
     * 
     * <p>
     * Has no effect if an identical listener is already registered.
     * </p>
     * 
     * @param listener listener to add
     */
    public void addListener(IConcurrentModelListener listener);
    
    /**
     * Removes a listener from this model. The receiver will stop sending
     * notifications to the given listener as soon as possible (although
     * some additional notifications may still if arrive if the receiver
     * was in the process of sending notifications in another thread).
     * Any pending updates for this listener will be cancelled.
     * <p>
     * Has no effect if the given listener is not known to this model.
     * </p>
     * 
     * @param listener listener to remove
     */
    public void removeListener(IConcurrentModelListener listener);
}
