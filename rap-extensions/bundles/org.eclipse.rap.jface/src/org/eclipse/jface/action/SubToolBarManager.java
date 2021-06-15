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

/**
 * A <code>SubToolBarManager</code> monitors the additional and removal of 
 * items from a parent manager so that visibility of the entire set can be changed as a
 * unit.
 */
public class SubToolBarManager extends SubContributionManager implements
        IToolBarManager {

    /**
     * Constructs a new manager.
     *
     * @param mgr the parent manager.  All contributions made to the 
     *      <code>SubToolBarManager</code> are forwarded and appear in the
     *      parent manager.
     */
    public SubToolBarManager(IToolBarManager mgr) {
        super(mgr);
    }

    /**
     * @return the parent toolbar manager that this sub-manager contributes to
     */
    protected final IToolBarManager getParentToolBarManager() {
        // Cast is ok because that's the only
        // thing we accept in the construtor.
        return (IToolBarManager) getParent();
    }

    /* (non-Javadoc)
     * Method declared on IToolBarManager.
     */
    public void update(boolean force) {
        // This method is not governed by visibility.  The client may
        // call <code>setVisible</code> and then force an update.  At that
        // point we need to update the parent.
        getParentToolBarManager().update(force);
    }
}
