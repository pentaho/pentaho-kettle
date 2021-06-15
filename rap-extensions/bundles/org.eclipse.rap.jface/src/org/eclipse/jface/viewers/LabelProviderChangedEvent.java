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
 * Event object describing a label provider state change.
 *
 * @see ILabelProviderListener
 */
public class LabelProviderChangedEvent extends EventObject {

    /**
     * Generated serial version UID for this class.
     * @since 1.0
     */
    private static final long serialVersionUID = 3258410612479309878L;
    
    /**
     * The elements whose labels need to be updated or <code>null</code>.
     */
    private Object[] elements;

    /**
     * Creates a new event for the given source, indicating that all labels
     * provided by the source are no longer valid and should be updated.
     *
     * @param source the label provider
     */
    public LabelProviderChangedEvent(IBaseLabelProvider source) {
        super(source);
    }

    /**
     * Creates a new event for the given source, indicating that the label
     * provided by the source for the given elements is no longer valid and should be updated.
     *
     * @param source the label provider
     * @param elements the element whose labels have changed
     */
    public LabelProviderChangedEvent(IBaseLabelProvider source,
            Object[] elements) {
        super(source);
        this.elements = elements;
    }

    /**
     * Creates a new event for the given source, indicating that the label
     * provided by the source for the given element is no longer valid and should be updated.
     *
     * @param source the label provider
     * @param element the element whose label needs to be updated
     */
    public LabelProviderChangedEvent(IBaseLabelProvider source, Object element) {
        super(source);
        this.elements = new Object[1];
        this.elements[0] = element;
    }

    /**
     * Returns the first element whose label needs to be updated,
     * or <code>null</code> if all labels need to be updated.
     *
     * @return the element whose label needs to be updated or <code>null</code>
     */
    public Object getElement() {
        if (this.elements == null || this.elements.length == 0) {
			return null;
		} else {
			return this.elements[0];
		}
    }

    /**
     * Returns the elements whose labels need to be updated,
     * or <code>null</code> if all labels need to be updated.
     *
     * @return the element whose labels need to be updated or <code>null</code>
     */
    public Object[] getElements() {
        if (this.elements == null) {
			return null;
		} else {
			return this.elements;
		}
    }
}
