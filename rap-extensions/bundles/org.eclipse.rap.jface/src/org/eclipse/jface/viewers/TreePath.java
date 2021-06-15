/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.core.runtime.Assert;

/**
 * A tree path denotes a model element in a tree viewer. Tree path objects have
 * value semantics. A model element is represented by a path of elements in the
 * tree from the root element to the leaf element.
 * <p>
 * Clients may instantiate this class. Not intended to be subclassed.
 * </p>
 * 
 * @since 1.0
 */
public final class TreePath {
	
	/**
	 * Constant for representing an empty tree path.
	 */
	public static final TreePath EMPTY = new TreePath(new Object[0]);
	
	private Object[] segments;

	private int hash;

	/**
	 * Constructs a path identifying a leaf node in a tree.
	 * 
	 * @param segments
	 *            path of elements to a leaf node in a tree, starting with the
	 *            root element
	 */
	public TreePath(Object[] segments) {
		Assert.isNotNull(segments);
		for (int i = 0; i < segments.length; i++) {
			Assert.isNotNull(segments[i]);
		}
		this.segments = segments;
	}

	/**
	 * Returns the element at the specified index in this path.
	 * 
	 * @param index
	 *            index of element to return
	 * @return element at the specified index
	 */
	public Object getSegment(int index) {
		return segments[index];
	}

	/**
	 * Returns the number of elements in this path.
	 * 
	 * @return the number of elements in this path
	 */
	public int getSegmentCount() {
		return segments.length;
	}

	/**
	 * Returns the first element in this path, or <code>null</code> if this
	 * path has no segments.
	 * 
	 * @return the first element in this path
	 */
	public Object getFirstSegment() {
		if (segments.length == 0) {
			return null;
		}
		return segments[0];
	}

	/**
	 * Returns the last element in this path, or <code>null</code> if this
	 * path has no segments.
	 * 
	 * @return the last element in this path
	 */
	public Object getLastSegment() {
		if (segments.length == 0) {
			return null;
		}
		return segments[segments.length - 1];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (!(other instanceof TreePath)) {
			return false;
		}
		return equals((TreePath) other, null);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (hash == 0) {
			hash = hashCode(null);
		}
		return hash;
	}

	/**
	 * Returns a hash code computed from the hash codes of the segments, using
	 * the given comparer to compute the hash codes of the segments.
	 * 
	 * @param comparer
	 *            comparer to use or <code>null</code> if the segments' hash
	 *            codes should be computed by calling their hashCode() methods.
	 * @return the computed hash code
	 */
	public int hashCode(IElementComparer comparer) {
		int result = 0;
		for (int i = 0; i < segments.length; i++) {
			if (comparer == null) {
				result += segments[i].hashCode();
			} else {
				result += comparer.hashCode(segments[i]);
			}
		}
		return result;
	}

	/**
	 * Returns whether this path is equivalent to the given path using the
	 * specified comparer to compare individual elements.
	 * 
	 * @param otherPath
	 *            tree path to compare to
	 * @param comparer
	 *            comparator to use or <code>null</code> if segments should be
	 *            compared using equals()
	 * @return whether the paths are equal
	 */
	public boolean equals(TreePath otherPath, IElementComparer comparer) {
		if (otherPath == null) {
			return false;
		}
		if (segments.length != otherPath.segments.length) {
			return false;
		}
		for (int i = 0; i < segments.length; i++) {
			if (comparer == null) {
				if (!segments[i].equals(otherPath.segments[i])) {
					return false;
				}
			} else {
				if (!comparer.equals(segments[i], otherPath.segments[i])) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns whether this path starts with the same segments as the given
	 * path, using the given comparer to compare segments.
	 * 
	 * @param treePath
	 *            path to compare to
	 * @param comparer
	 *            the comparer to use, or <code>null</code> if equals() should
	 *            be used to compare segments
	 * @return whether the given path is a prefix of this path, or the same as
	 *         this path
	 */
	public boolean startsWith(TreePath treePath, IElementComparer comparer) {
		int thisSegmentCount = getSegmentCount();
		int otherSegmentCount = treePath.getSegmentCount();
		if (otherSegmentCount == thisSegmentCount) {
			return equals(treePath, comparer);
		}
		if (otherSegmentCount > thisSegmentCount) {
			return false;
		}
		for (int i = 0; i < otherSegmentCount; i++) {
			Object otherSegment = treePath.getSegment(i);
			if (comparer == null) {
				if (!otherSegment.equals(segments[i])) {
					return false;
				}
			} else {
				if (!comparer.equals(otherSegment, segments[i])) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns a copy of this tree path with one segment removed from the end,
	 * or <code>null</code> if this tree path has no segments.
	 * @return a tree path
	 */
	public TreePath getParentPath() {
		int segmentCount = getSegmentCount();
		if (segmentCount < 1) {
			return null;
		} else if (segmentCount == 1) {
			return EMPTY;
		}
		Object[] parentSegments = new Object[segmentCount - 1];
		System.arraycopy(segments, 0, parentSegments, 0, segmentCount - 1);
		return new TreePath(parentSegments);
	}

	/**
	 * Returns a copy of this tree path with the given segment added at the end.
	 * @param newSegment 
	 * @return a tree path
	 */
	public TreePath createChildPath(Object newSegment) {
		int segmentCount = getSegmentCount();
		Object[] childSegments = new Object[segmentCount + 1];
		if(segmentCount>0) {
			System.arraycopy(segments, 0, childSegments, 0, segmentCount);
		}
		childSegments[segmentCount] = newSegment;
		return new TreePath(childSegments);
	}
}
