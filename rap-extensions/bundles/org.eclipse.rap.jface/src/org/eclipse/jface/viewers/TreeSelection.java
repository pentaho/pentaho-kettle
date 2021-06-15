/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;

/**
 * A concrete implementation of the <code>ITreeSelection</code> interface,
 * suitable for instantiating.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * 
 * @since 1.0
 */
public class TreeSelection extends StructuredSelection implements ITreeSelection {

	/* Implementation note.  This class extends StructuredSelection because many pre-existing
	 * JFace viewer clients assumed that the only implementation of IStructuredSelection 
	 * was StructuredSelection.  By extending StructuredSelection rather than implementing
	 * ITreeSelection directly, we avoid this problem.
	 * For more details, see Bug 121939 [Viewers] TreeSelection should subclass StructuredSelection. 
	 */
	
	private TreePath[] paths = null;
	private CustomHashtable element2TreePaths = null;

	/**
	 * The canonical empty selection. This selection should be used instead of
	 * <code>null</code>.
	 */
	public static final TreeSelection EMPTY = new TreeSelection();
	
	private static final TreePath[] EMPTY_TREE_PATHS= new TreePath[0];
	
	private static class InitializeData {
		List selection;
		TreePath[] paths;
		CustomHashtable element2TreePaths;
		
		private InitializeData(TreePath[] paths, IElementComparer comparer) {
			this.paths= new TreePath[paths.length];
			System.arraycopy(paths, 0, this.paths, 0, paths.length);
			element2TreePaths = new CustomHashtable(comparer);
			int size = paths.length;
			selection = new ArrayList(size);
			for (int i = 0; i < size; i++) {
				Object lastSegment= paths[i].getLastSegment();
				Object mapped= element2TreePaths.get(lastSegment);
				if (mapped == null) {
					selection.add(lastSegment);
					element2TreePaths.put(lastSegment, paths[i]);
				} else if (mapped instanceof List) {
					((List)mapped).add(paths[i]);
				} else {
					List newMapped= new ArrayList(2);
					newMapped.add(mapped);
					newMapped.add(paths[i]);
					element2TreePaths.put(lastSegment, newMapped);
				}
			}
		}
	}

	/**
	 * Constructs a selection based on the elements identified by the given tree
	 * paths.
	 * 
	 * @param paths
	 *            tree paths
	 */
	public TreeSelection(TreePath[] paths) {
		this(new InitializeData(paths, null));
	}

	/**
	 * Constructs a selection based on the elements identified by the given tree
	 * paths.
	 * 
	 * @param paths
	 *            tree paths
	 * @param comparer 
	 *            the comparer, or <code>null</code> if default equals is to be used
	 */
	public TreeSelection(TreePath[] paths, IElementComparer comparer) {
		this(new InitializeData(paths, comparer));
	}

	/**
	 * Constructs a selection based on the elements identified by the given tree
	 * path.
	 * 
	 * @param treePath
	 *            tree path, or <code>null</code> for an empty selection
	 */
	public TreeSelection(TreePath treePath) {
		this(treePath != null ? new TreePath[] { treePath } : EMPTY_TREE_PATHS, null);
	}

	/**
	 * Constructs a selection based on the elements identified by the given tree
	 * path.
	 * 
	 * @param treePath
	 *            tree path, or <code>null</code> for an empty selection
	 * @param comparer 
	 *            the comparer, or <code>null</code> if default equals is to be used
	 */
	public TreeSelection(TreePath treePath, IElementComparer comparer) {
		this(treePath != null ? new TreePath[] { treePath } : EMPTY_TREE_PATHS, comparer);
	}
	
	/**
	 * Creates a new tree selection based on the initialization data.
	 * 
	 * @param data the data
	 */
	private TreeSelection(InitializeData data) {
		super(data.selection);
		paths= data.paths;
		element2TreePaths= data.element2TreePaths;
	}

	/**
	 * Creates a new empty selection. See also the static field
	 * <code>EMPTY</code> which contains an empty selection singleton.
	 * 
	 * @see #EMPTY
	 */
	public TreeSelection() {
		super();
	}
	
	/**
	 * Returns the element comparer passed in when the tree selection
	 * has been created or <code>null</code> if no comparer has been
	 * provided.
	 * 
	 * @return the element comparer or <code>null</code>
	 * 
	 * @since 1.0
	 */
	public IElementComparer getElementComparer() {
		if (element2TreePaths == null)
			return null;
		return element2TreePaths.getComparer();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof TreeSelection)) {
			// Fall back to super implementation, see bug 135837.
			return super.equals(obj);
		}
		TreeSelection selection = (TreeSelection) obj;
		int size = getPaths().length;
		if (selection.getPaths().length == size) {
			IElementComparer comparerOrNull = (getElementComparer() == selection
					.getElementComparer()) ? getElementComparer() : null;
			if (size > 0) {
				for (int i = 0; i < paths.length; i++) {
					if (!paths[i].equals(selection.paths[i], comparerOrNull)) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	public int hashCode() {
		int code = getClass().hashCode();
		if (paths != null) {
			for (int i = 0; i < paths.length; i++) {
				code = code * 17 + paths[i].hashCode(getElementComparer());
			}
		}
		return code;
	}

	public TreePath[] getPaths() {
		return paths==null ? EMPTY_TREE_PATHS : (TreePath[]) paths.clone();
	}
	
	public TreePath[] getPathsFor(Object element) {
		Object value= element2TreePaths==null ? null : element2TreePaths.get(element);
		if (value == null) {
			return EMPTY_TREE_PATHS;
		} else if (value instanceof TreePath) {
			return new TreePath[] { (TreePath)value };
		} else if (value instanceof List) {
			List l= (List)value;
			return (TreePath[])l.toArray(new TreePath[l.size()]);
		} else {
			// should not happen:
			Assert.isTrue(false, "Unhandled case"); //$NON-NLS-1$
			return null;
		}
	}
}
