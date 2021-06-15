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
package org.eclipse.jface.viewers;

import java.util.EventObject;

/**
 * Event object describing a change to the checked state
 * of a viewer element.
 *
 * @see ICheckStateListener
 */
public class CheckStateChangedEvent extends EventObject {

    /**
     * Generated serial version UID for this class.
     * @since 1.0
     */
    private static final long serialVersionUID = 3256443603340244789L;

    /**
     * The viewer element.
     */
    private Object element;

    /**
     * The checked state.
     */
    private boolean state;

    /**
     * Creates a new event for the given source, element, and checked state.
     *
     * @param source the source
     * @param element the element
     * @param state the checked state
     */
    public CheckStateChangedEvent(ICheckable source, Object element,
            boolean state) {
        super(source);
        this.element = element;
        this.state = state;
    }

    /**
     * Returns the checkable that is the source of this event.
     *
     * @return the originating checkable
     */
    public ICheckable getCheckable() {
        return (ICheckable) source;
    }

    /**
     * Returns the checked state of the element.
     *
     * @return the checked state
     */
    public boolean getChecked() {
        return state;
    }

    /**
     * Returns the element whose check state changed.
     *
     * @return the element
     */
    public Object getElement() {
        return element;
    }
}
