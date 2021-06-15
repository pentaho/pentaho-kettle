/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     											 - fix in bug: 174355,171126,,195908,198035,215069,227421
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.util.LinkedList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * TreeViewerRow is the Tree implementation of ViewerRow.
 *
 * @since 1.0
 *
 */
public class TreeViewerRow extends ViewerRow {
	private TreeItem item;

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param item
	 */
	TreeViewerRow(TreeItem item) {
		this.item = item;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ViewerRow#getBounds(int)
	 */
	public Rectangle getBounds(int columnIndex) {
		return item.getBounds(columnIndex);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ViewerRow#getBounds()
	 */
	public Rectangle getBounds() {
		return item.getBounds();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ViewerRow#getColumnCount()
	 */
	public int getColumnCount() {
		return item.getParent().getColumnCount();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ViewerRow#getItem()
	 */
	public Widget getItem() {
		return item;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ViewerRow#getBackground(int)
	 */
	public Color getBackground(int columnIndex) {
		return item.getBackground(columnIndex);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ViewerRow#getFont(int)
	 */
	public Font getFont(int columnIndex) {
		return item.getFont(columnIndex);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ViewerRow#getForeground(int)
	 */
	public Color getForeground(int columnIndex) {
		return item.getForeground(columnIndex);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ViewerRow#getImage(int)
	 */
	public Image getImage(int columnIndex) {
		return item.getImage(columnIndex);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ViewerRow#getText(int)
	 */
	public String getText(int columnIndex) {
		return item.getText(columnIndex);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ViewerRow#setBackground(int,
	 *      org.eclipse.swt.graphics.Color)
	 */
	public void setBackground(int columnIndex, Color color) {
		item.setBackground(columnIndex, color);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ViewerRow#setFont(int,
	 *      org.eclipse.swt.graphics.Font)
	 */
	public void setFont(int columnIndex, Font font) {
		item.setFont(columnIndex, font);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ViewerRow#setForeground(int,
	 *      org.eclipse.swt.graphics.Color)
	 */
	public void setForeground(int columnIndex, Color color) {
		item.setForeground(columnIndex, color);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ViewerRow#setImage(int,
	 *      org.eclipse.swt.graphics.Image)
	 */
	public void setImage(int columnIndex, Image image) {
		Image oldImage = item.getImage(columnIndex);
		if (image != oldImage) {
			item.setImage(columnIndex, image);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ViewerRow#setText(int, java.lang.String)
	 */
	public void setText(int columnIndex, String text) {
		item.setText(columnIndex, text == null ? "" : text); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ViewerRow#getControl()
	 */
	public Control getControl() {
		return item.getParent();
	}

	public ViewerRow getNeighbor(int direction, boolean sameLevel) {
		if (direction == ViewerRow.ABOVE) {
			return getRowAbove(sameLevel);
		} else if (direction == ViewerRow.BELOW) {
			return getRowBelow(sameLevel);
		} else {
			throw new IllegalArgumentException(
					"Illegal value of direction argument."); //$NON-NLS-1$
		}
	}

	private ViewerRow getRowBelow(boolean sameLevel) {
		Tree tree = item.getParent();

		// This means we have top-level item
		if (item.getParentItem() == null) {
			if (sameLevel || !item.getExpanded()) {
				int index = tree.indexOf(item) + 1;

				if (index < tree.getItemCount()) {
					return new TreeViewerRow(tree.getItem(index));
				}
			} else if (item.getExpanded() && item.getItemCount() > 0) {
				return new TreeViewerRow(item.getItem(0));
			}
		} else {
			if (sameLevel || !item.getExpanded()) {
				TreeItem parentItem = item.getParentItem();

				int nextIndex = parentItem.indexOf(item) + 1;
				int totalIndex = parentItem.getItemCount();

				TreeItem itemAfter;

				// This would mean that it was the last item
				if (nextIndex == totalIndex) {
					itemAfter = findNextItem(parentItem);
				} else {
					itemAfter = parentItem.getItem(nextIndex);
				}

				if (itemAfter != null) {
					return new TreeViewerRow(itemAfter);
				}

			} else if (item.getExpanded() && item.getItemCount() > 0) {
				return new TreeViewerRow(item.getItem(0));
			}
		}

		return null;
	}

	private ViewerRow getRowAbove(boolean sameLevel) {
		Tree tree = item.getParent();

		// This means we have top-level item
		if (item.getParentItem() == null) {
			int index = tree.indexOf(item) - 1;
			TreeItem nextTopItem = null;

			if (index >= 0) {
				nextTopItem = tree.getItem(index);
			}

			if (nextTopItem != null) {
				if (sameLevel) {
					return new TreeViewerRow(nextTopItem);
				}

				return new TreeViewerRow(findLastVisibleItem(nextTopItem));
			}
		} else {
			TreeItem parentItem = item.getParentItem();
			int previousIndex = parentItem.indexOf(item) - 1;

			TreeItem itemBefore;
			if (previousIndex >= 0) {
				if (sameLevel) {
					itemBefore = parentItem.getItem(previousIndex);
				} else {
					itemBefore = findLastVisibleItem(parentItem
							.getItem(previousIndex));
				}
			} else {
				itemBefore = parentItem;
			}

			if (itemBefore != null) {
				return new TreeViewerRow(itemBefore);
			}
		}

		return null;
	}

	private TreeItem findLastVisibleItem(TreeItem parentItem) {
		TreeItem rv = parentItem;

		while (rv.getExpanded() && rv.getItemCount() > 0) {
			rv = rv.getItem(rv.getItemCount() - 1);
		}

		return rv;
	}

	private TreeItem findNextItem(TreeItem item) {
		TreeItem rv = null;
		Tree tree = item.getParent();
		TreeItem parentItem = item.getParentItem();

		int nextIndex;
		int totalItems;

		if (parentItem == null) {
			nextIndex = tree.indexOf(item) + 1;
			totalItems = tree.getItemCount();
		} else {
			nextIndex = parentItem.indexOf(item) + 1;
			totalItems = parentItem.getItemCount();
		}

		// This is once more the last item in the tree
		// Search on
		if (nextIndex == totalItems) {
			if (item.getParentItem() != null) {
				rv = findNextItem(item.getParentItem());
			}
		} else {
			if (parentItem == null) {
				rv = tree.getItem(nextIndex);
			} else {
				rv = parentItem.getItem(nextIndex);
			}
		}

		return rv;
	}

	public TreePath getTreePath() {
		TreeItem tItem = item;
		LinkedList segments = new LinkedList();
		while (tItem != null) {
			Object segment = tItem.getData();
			Assert.isNotNull(segment);
			segments.addFirst(segment);
			tItem = tItem.getParentItem();
		}

		return new TreePath(segments.toArray());
	}

	void setItem(TreeItem item) {
		this.item = item;
	}

	public Object clone() {
		return new TreeViewerRow(item);
	}

	public Object getElement() {
		return item.getData();
	}

	public int getVisualIndex(int creationIndex) {
		int[] order = item.getParent().getColumnOrder();

		for (int i = 0; i < order.length; i++) {
			if (order[i] == creationIndex) {
				return i;
			}
		}

		return super.getVisualIndex(creationIndex);
	}

	public int getCreationIndex(int visualIndex) {
		if( item != null && ! item.isDisposed() && hasColumns() && isValidOrderIndex(visualIndex) ) {
			return item.getParent().getColumnOrder()[visualIndex];
		}
		return super.getCreationIndex(visualIndex);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#getTextBounds(int)
	 */
	public Rectangle getTextBounds(int index) {
		return item.getTextBounds(index);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerRow#getImageBounds(int)
	 */
	public Rectangle getImageBounds(int index) {
		return item.getImageBounds(index);
	}	
	
	private boolean hasColumns() {
		return this.item.getParent().getColumnCount() != 0;
	}

	private boolean isValidOrderIndex(int currentIndex) {
		return currentIndex < this.item.getParent().getColumnOrder().length;
	}

	int getWidth(int columnIndex) {
		return item.getParent().getColumn(columnIndex).getWidth();
	}
	
	protected boolean scrollCellIntoView(int columnIndex) {
		item.getParent().showItem(item);
		if( hasColumns() ) {
			item.getParent().showColumn(item.getParent().getColumn(columnIndex));	
		}
		return true;
	}
}
