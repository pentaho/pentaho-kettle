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
package org.eclipse.jface.action;

import java.io.Serializable;

/**
 * A menu listener that gets informed when a menu is about to show.
 *
 * @see MenuManager#addMenuListener
 */
public interface IMenuListener extends Serializable {
    /**
     * Notifies this listener that the menu is about to be shown by
     * the given menu manager.
     *
     * @param manager the menu manager
     */
    public void menuAboutToShow(IMenuManager manager);
}
