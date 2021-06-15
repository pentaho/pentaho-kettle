/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *******************************************************************************/
package org.eclipse.jface.preference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Assert;

/**
 * A preference manager maintains a hierarchy of preference nodes and
 * associated preference pages.
 */
public class PreferenceManager implements Serializable {
    /**
     * Pre-order traversal means visit the root first,
     * then the children.
     */
    public static final int PRE_ORDER = 0;

    /**
     * Post-order means visit the children, and then the root.
     */
    public static final int POST_ORDER = 1;
    
    /**
     * The id of the root node.
     */
    private final static String ROOT_NODE_ID = ""; //$NON-NLS-1$

    /**
     * The root node.
     * Note that the root node is a special internal node
     * that is used to collect together all the nodes that
     * have no parent; it is not given out to clients.
     */
    PreferenceNode root;

    /**
     * The path separator character.
     */
    String separator;

    /**
     * Creates a new preference manager.
     */
    public PreferenceManager() {
        this('.', new PreferenceNode(ROOT_NODE_ID));
    }
    
    /**
     * Creates a new preference manager with the given
     * path separator.
     * 
     * @param separatorChar
     */
    public PreferenceManager(final char separatorChar) { 
    	this(separatorChar, new PreferenceNode(ROOT_NODE_ID));
    }

    /**
     * Creates a new preference manager with the given
     * path separator and root node.
     *
     * @param separatorChar the separator character
     * @param rootNode the root node. 
     *
     * @since 1.1
     */
    public PreferenceManager(final char separatorChar, PreferenceNode rootNode) {
        separator = new String(new char[] { separatorChar });
        this.root = rootNode;
    }

    /**
     * Adds the given preference node as a subnode of the
     * node at the given path.
     *
     * @param path the path
     * @param node the node to add
     * @return <code>true</code> if the add was successful,
     *  and <code>false</code> if there is no contribution at
     *  the given path
     */
    public boolean addTo(String path, IPreferenceNode node) {
        IPreferenceNode target = find(path);
        if (target == null) {
			return false;
		}
        target.add(node);
        return true;
    }

    /**
     * Adds the given preference node as a subnode of the
     * root.
     *
     * @param node the node to add, which must implement 
     *   <code>IPreferenceNode</code>
     */
    public void addToRoot(IPreferenceNode node) {
        Assert.isNotNull(node);
        root.add(node);
    }

    /**
     * Recursively enumerates all nodes at or below the given node
     * and adds them to the given list in the given order.
     * 
     * @param node the starting node
     * @param sequence a read-write list of preference nodes
     *  (element type: <code>IPreferenceNode</code>)
     *  in the given order
     * @param order the traversal order, one of 
     *	<code>PRE_ORDER</code> and <code>POST_ORDER</code>
     */
    protected void buildSequence(IPreferenceNode node, List sequence, int order) {
        if (order == PRE_ORDER) {
			sequence.add(node);
		}
        IPreferenceNode[] subnodes = node.getSubNodes();
        for (int i = 0; i < subnodes.length; i++) {
            buildSequence(subnodes[i], sequence, order);
        }
        if (order == POST_ORDER) {
			sequence.add(node);
		}
    }

    /**
     * Finds and returns the contribution node at the given path.
     *
     * @param path the path
     * @return the node, or <code>null</code> if none
     */
    public IPreferenceNode find(String path) {
       return find(path,root);
    }
    
    /**
     * Finds and returns the preference node directly
     * below the top at the given path.
     *
     * @param path the path
     * @param top top at the given path
     * @return the node, or <code>null</code> if none
     * 
     * @since 1.0
     */
    protected IPreferenceNode find(String path,IPreferenceNode top){
    	 Assert.isNotNull(path);
         StringTokenizer stok = new StringTokenizer(path, separator);
         IPreferenceNode node = top;
         while (stok.hasMoreTokens()) {
             String id = stok.nextToken();
             node = node.findSubNode(id);
             if (node == null) {
				return null;
			}
         }
         if (node == top) {
			return null;
		}
         return node;
    }

    /**
     * Returns all preference nodes managed by this
     * manager.
     *
     * @param order the traversal order, one of 
     *	<code>PRE_ORDER</code> and <code>POST_ORDER</code>
     * @return a list of preference nodes
     *  (element type: <code>IPreferenceNode</code>)
     *  in the given order
     */
    public List getElements(int order) {
        Assert.isTrue(order == PRE_ORDER || order == POST_ORDER,
                "invalid traversal order");//$NON-NLS-1$
        ArrayList sequence = new ArrayList();
        IPreferenceNode[] subnodes = getRoot().getSubNodes();
        for (int i = 0; i < subnodes.length; i++) {
			buildSequence(subnodes[i], sequence, order);
		}
        return sequence;
    }

    /**
     * Returns the root node.
     * Note that the root node is a special internal node
     * that is used to collect together all the nodes that
     * have no parent; it is not given out to clients.
     *
     * @return the root node
     */
    protected IPreferenceNode getRoot() {
        return root;
    }

	/**
	 * Returns the root level nodes of this preference manager.
	 * 
	 * @return an array containing the root nodes
	 * @since 1.0
	 */
	public final IPreferenceNode[] getRootSubNodes() {
		return getRoot().getSubNodes();
	}

    /**
	 * Removes the preference node at the given path.
	 * 
	 * @param path
	 *            the path
	 * @return the node that was removed, or <code>null</code> if there was no
	 *         node at the given path
	 */
    public IPreferenceNode remove(String path) {
        Assert.isNotNull(path);
        int index = path.lastIndexOf(separator);
        if (index == -1) {
			return root.remove(path);
		}
        // Make sure that the last character in the string isn't the "."
        Assert.isTrue(index < path.length() - 1, "Path can not end with a dot");//$NON-NLS-1$
        String parentPath = path.substring(0, index);
        String id = path.substring(index + 1);
        IPreferenceNode parentNode = find(parentPath);
        if (parentNode == null) {
			return null;
		}
        return parentNode.remove(id);
    }

    /**
     * Removes the given prefreence node if it is managed by
     * this contribution manager.
     *
     * @param node the node to remove
     * @return <code>true</code> if the node was removed,
     *  and <code>false</code> otherwise
     */
    public boolean remove(IPreferenceNode node) {
        Assert.isNotNull(node);

        return root.remove(node);
    }

    /**
     * Removes all contribution nodes known to this manager.
     */
    public void removeAll() {
        root = new PreferenceNode("");//$NON-NLS-1$
    }
}
