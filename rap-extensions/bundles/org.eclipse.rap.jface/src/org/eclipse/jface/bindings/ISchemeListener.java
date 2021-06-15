/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.bindings;

/**
 * <p>
 * An instance of <code>ISchemeListener</code> can be used by clients to
 * receive notification of changes to one or more instances of
 * <code>IScheme</code>.
 * </p>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * 
 * @since 1.4
 * @see Scheme#addSchemeListener(ISchemeListener)
 * @see Scheme#removeSchemeListener(ISchemeListener)
 * @see SchemeEvent
 */
public interface ISchemeListener {

    /**
     * Notifies that one or more attributes of an instance of
     * <code>IScheme</code> have changed. Specific details are described in
     * the <code>SchemeEvent</code>.
     * 
     * @param schemeEvent
     *            the scheme event. Guaranteed not to be <code>null</code>.
     */
    void schemeChanged(SchemeEvent schemeEvent);
}
