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
package org.eclipse.jface.wizard;

import org.eclipse.swt.graphics.Point;

/**
 * A wizard node acts a placeholder for a real wizard in a wizard 
 * selection page. It is done in such a way that the actual creation
 * of a wizard can be deferred until the wizard is really needed.
 * <p>
 * When a wizard node comes into existence, its wizard may or may
 * not have been created yet; <code>isContentCreated</code> can
 * be used to determine which. A node may be asked for its wizard
 * using <code>getWizard</code>, which will force it to be created
 * if required. Once the client is done with a wizard node, its
 * <code>dispose</code>method must be called to free up the wizard;
 * once disposes, the node should no longer be used.
 * </p>
 * <p>
 * This interface should be implemented by clients wishing to
 * support this kind of wizard placeholder in a wizard selection page.
 * </p>
 *
 * @see WizardSelectionPage
 */
public interface IWizardNode {
    /**
     * Disposes the wizard managed by this node. Does nothing
     * if the wizard has not been created.
     * <p>
     * This is the last message that should ever be sent to this node.
     * </p>
     */
    public void dispose();

    /**
     * Returns the extent of the wizard for this node.
     * <p>
     * If the content has not yet been created, calling this method
     * does not trigger the creation of the wizard. This allows
     * this node to suggest an extent in advance of actually creating 
     * the wizard.
     * </p>
     *
     * @return the extent, or <code>(-1, -1)</code> extent is not known
     */
    public Point getExtent();

    /**
     * Returns the wizard this node stands for.
     * <p>
     * If the content has not been created beforehand, calling this
     * method triggers the creation of the wizard and caches it so that
     * the identical wizard object is returned on subsequent calls.
     * </p>
     *
     * @return the wizard
     */
    public IWizard getWizard();

    /**
     * Returns whether a wizard has been created for this node.
     *
     * @return <code>true</code> if a wizard has been created,
     *   and <code>false</code> otherwise
     */
    public boolean isContentCreated();
}
