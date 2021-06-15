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
package org.eclipse.jface.preference;

import java.io.Serializable;

import org.eclipse.swt.graphics.Image;

/**
 * Interface to a node in a preference dialog. 
 * A preference node maintains a label and image used to display the
 * node in a preference dialog (usually in the form of a tree), 
 * as well as the preference page this node stands for.
 *
 * The node may use lazy creation for its page
 *
 * Note that all preference nodes must be dispose their resources.
 * The node must dispose the page managed by this node, and any SWT resources
 * allocated by this node (Images, Fonts, etc).
 * However the node itself may be reused.
 */
public interface IPreferenceNode extends Serializable {
    /**
     * Adds the given preference node as a subnode of this
     * preference node.
     *
     * @param node the node to add
     */
    public void add(IPreferenceNode node);

    /**
     * Creates the preference page for this node.
     */
    public void createPage();

    /**
     * Release the page managed by this node, and any SWT resources
     * held onto by this node (Images, Fonts, etc).  
     *
     * Note that nodes are reused so this is not a call to dispose the
     * node itself.
     */
    public void disposeResources();

    /**
     * Returns the subnode of this contribution node with the given node id.
     *
     * @param id the preference node id
     * @return the subnode, or <code>null</code> if none
     */
    public IPreferenceNode findSubNode(String id);

    /**
     * Returns the id of this contribution node.
     * This id identifies a contribution node relative to its parent.
     *
     * @return the node id
     */
    public String getId();

    /**
     * Returns the image used to present this node in a preference dialog.
     *
     * @return the image for this node, or <code>null</code>
     *   if there is no image for this node
     */
    public Image getLabelImage();

    /**
     * Returns the text label used to present this node in a preference dialog.
     *
     * @return the text label for this node, or <code>null</code>
     *   if there is no label for this node
     */
    public String getLabelText();

    /**
     * Returns the preference page for this node.
     *
     * @return the preference page
     */
    public IPreferencePage getPage();

    /**
     * Returns an iterator over the subnodes (immediate children)
     * of this contribution node.
     *
     * @return an IPreferenceNode array containing the child nodes
     */
    public IPreferenceNode[] getSubNodes();

    /**
     * Removes the subnode of this preference node with the given node id.
     *
     * @param id the subnode id
     * @return the removed subnode, or <code>null</code> if none
     */
    public IPreferenceNode remove(String id);

    /**
     * Removes the given preference node from the list of subnodes
     * (immediate children) of this node.
     *
     * @param node the node to remove
     * @return <code>true</code> if the node was removed,
     *  and <code>false</code> otherwise
     */
    public boolean remove(IPreferenceNode node);
}
