/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.preference;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Provides labels for <code>IPreferenceNode</code> objects.
 * 
 * @since 1.0
 */
public class PreferenceLabelProvider extends LabelProvider {

    /**
     * @param element must be an instance of <code>IPreferenceNode</code>.
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        return ((IPreferenceNode) element).getLabelText();
    }

    /**
     * @param element must be an instance of <code>IPreferenceNode</code>.
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {
        return ((IPreferenceNode) element).getLabelImage();
    }
}
