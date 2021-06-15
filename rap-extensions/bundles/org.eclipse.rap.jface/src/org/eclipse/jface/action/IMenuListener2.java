/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.action;

/**
 * A menu listener that gets informed when a menu is about to hide.
 *
 * @see MenuManager#addMenuListener
 * @since 1.0
 */
public interface IMenuListener2 extends IMenuListener {
    /**
     * Notifies this listener that the menu is about to be hidden by
     * the given menu manager.
     *
     * @param manager the menu manager
     */
    public void menuAboutToHide(IMenuManager manager);
}
