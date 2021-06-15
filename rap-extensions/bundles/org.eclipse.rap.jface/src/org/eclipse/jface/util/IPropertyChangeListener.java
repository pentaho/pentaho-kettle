/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.util;

import java.io.Serializable;
import java.util.EventListener;

/**
 * Listener for property changes.
 * <p>
 * Usage:
 * <pre>
 * IPropertyChangeListener listener =
 *   new IPropertyChangeListener() {
 *      public void propertyChange(PropertyChangeEvent event) {
 *         ... // code to deal with occurrence of property change
 *      }
 *   };
 * emitter.addPropertyChangeListener(listener);
 * ...
 * emitter.removePropertyChangeListener(listener);
 * </pre>
 * </p>
 */
public interface IPropertyChangeListener extends EventListener, Serializable {
    /**
     * Notification that a property has changed.
     * <p>
     * This method gets called when the observed object fires a property
     * change event.
     * </p>
     *
     * @param event the property change event object describing which property
     * changed and how
     */
    public void propertyChange(PropertyChangeEvent event);
}
