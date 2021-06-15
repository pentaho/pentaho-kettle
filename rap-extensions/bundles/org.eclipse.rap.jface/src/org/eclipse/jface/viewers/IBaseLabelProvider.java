/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import java.io.Serializable;

/**
 * A label provider maps an element of the viewer's model to
 * an optional image and optional text string used to display
 * the element in the viewer's control.  Certain label providers
 * may allow multiple labels per element.  
 * This is an "abstract interface", defining methods common 
 * to all label providers, but does not actually define the methods 
 * to get the label(s) for an element.  This interface should never
 * be directly implemented.
 * Most viewers will take either an <code>ILabelProvider</code> or
 * an <code>ITableLabelProvider</code>.
 * <p>
 * A label provider must not be shared between viewers 
 * since a label provider generally manages SWT resources (images),
 * which must be disposed when the viewer is disposed.
 * To simplify life cycle management, the current label provider 
 * of a viewer is disposed when the viewer is disposed.
 * </p>
 * <p>
 * Label providers can be used outside the context of viewers wherever
 * images are needed.  When label providers are used in this fashion
 * it is the responsibility of the user to ensure <code>dispose</code>
 * is called when the provider is no longer needed.
 * </p>
 *
 * @see ILabelProvider
 * @see ITableLabelProvider
 */
public interface IBaseLabelProvider extends Serializable {
    /**
     * Adds a listener to this label provider. 
     * Has no effect if an identical listener is already registered.
     * <p>
     * Label provider listeners are informed about state changes 
     * that affect the rendering of the viewer that uses this label provider.
     * </p>
     *
     * @param listener a label provider listener
     */
    public void addListener(ILabelProviderListener listener);

    /**
     * Disposes of this label provider.  When a label provider is
     * attached to a viewer, the viewer will automatically call
     * this method when the viewer is being closed.  When label providers
     * are used outside of the context of a viewer, it is the client's
     * responsibility to ensure that this method is called when the
     * provider is no longer needed.
     */
    public void dispose();

    /**
     * Returns whether the label would be affected 
     * by a change to the given property of the given element.
     * This can be used to optimize a non-structural viewer update.
     * If the property mentioned in the update does not affect the label,
     * then the viewer need not update the label.
     *
     * @param element the element
     * @param property the property
     * @return <code>true</code> if the label would be affected,
     *    and <code>false</code> if it would be unaffected
     */
    public boolean isLabelProperty(Object element, String property);

    /**
     * Removes a listener to this label provider.
     * Has no effect if an identical listener is not registered.
     *
     * @param listener a label provider listener
     */
    public void removeListener(ILabelProviderListener listener);
}
