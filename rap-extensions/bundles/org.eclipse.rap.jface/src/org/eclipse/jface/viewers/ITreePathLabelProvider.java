/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.viewers;

/**
 * An extension to {@link ILabelProvider} that is given the 
 * path of the element being decorated, when it is available.
 * @since 1.0
 */
public interface ITreePathLabelProvider extends IBaseLabelProvider {
    
    /**
     * Updates the label for the given element.
     * 
     * @param label the label to update
     * @param elementPath the path of the element being decorated
     */
    public void updateLabel(ViewerLabel label, TreePath elementPath);
}
