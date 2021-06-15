///*******************************************************************************
// * Copyright (c) 2007, 2008 IBM Corporation and others.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     IBM Corporation - initial API and implementation
// *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
// *     											- fix in bug: 195908, 210752
// *******************************************************************************/
//
//package org.eclipse.jface.viewers;
//
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.widgets.Event;
//import org.eclipse.swt.widgets.Item;
//import org.eclipse.swt.widgets.Tree;
//import org.eclipse.swt.widgets.TreeItem;
//
///**
// * This class is responsible to provide the concept of cells for {@link Tree}.
// * This concept is needed to provide features like editor activation with the
// * keyboard
// *
// * @since 1.0
// *
// */
//public class TreeViewerFocusCellManager extends SWTFocusCellManager {
//	private static final CellNavigationStrategy TREE_NAVIGATE = new CellNavigationStrategy() {
//		public void collapse(ColumnViewer viewer, ViewerCell cellToCollapse,
//				Event event) {
//			if (cellToCollapse != null) {
//				((TreeItem) cellToCollapse.getItem()).setExpanded(false);
//			}
//		}
//
//		public void expand(ColumnViewer viewer, ViewerCell cellToExpand,
//				Event event) {
//			if (cellToExpand != null) {
//				TreeViewer v = (TreeViewer) viewer;
//				v.setExpandedState(v.getTreePathFromItem((Item) cellToExpand
//						.getItem()), true);
//			}
//		}
//
//		public boolean isCollapseEvent(ColumnViewer viewer,
//				ViewerCell cellToCollapse, Event event) {
//
//			if (cellToCollapse == null) {
//				return false;
//			}
//
//			return cellToCollapse != null
//					&& ((TreeItem) cellToCollapse.getItem()).getExpanded()
//					&& event.keyCode == SWT.ARROW_LEFT
//					&& isFirstColumnCell(cellToCollapse);
//		}
//
//		public boolean isExpandEvent(ColumnViewer viewer,
//				ViewerCell cellToExpand, Event event) {
//
//			if (cellToExpand == null) {
//				return false;
//			}
//
//			return cellToExpand != null
//					&& ((TreeItem) cellToExpand.getItem()).getItemCount() > 0
//					&& !((TreeItem) cellToExpand.getItem()).getExpanded()
//					&& event.keyCode == SWT.ARROW_RIGHT
//					&& isFirstColumnCell(cellToExpand);
//		}
//
//		private boolean isFirstColumnCell(ViewerCell cell) {
//			return cell.getViewerRow().getVisualIndex(cell.getColumnIndex()) == 0;
//		}
//	};
//
//	/**
//	 * Create a new manager using a default navigation strategy:
//	 * <ul>
//	 * <li><code>SWT.ARROW_UP</code>: navigate to cell above</li>
//	 * <li><code>SWT.ARROW_DOWN</code>: navigate to cell below</li>
//	 * <li><code>SWT.ARROW_RIGHT</code>: on first column (collapses if item
//	 * is expanded) else navigate to next visible cell on the right</li>
//	 * <li><code>SWT.ARROW_LEFT</code>: on first column (expands if item is
//	 * collapsed) else navigate to next visible cell on the left</li>
//	 * </ul>
//	 *
//	 * @param viewer
//	 *            the viewer the manager is bound to
//	 * @param focusDrawingDelegate
//	 *            the delegate responsible to highlight selected cell
//	 */
//	public TreeViewerFocusCellManager(TreeViewer viewer,
//			FocusCellHighlighter focusDrawingDelegate) {
//		this(viewer, focusDrawingDelegate, TREE_NAVIGATE);
//	}
//
//	/**
//	 * Create a new manager with a custom navigation strategy
//	 *
//	 * @param viewer
//	 *            the viewer the manager is bound to
//	 * @param focusDrawingDelegate
//	 *            the delegate responsible to highlight selected cell
//	 * @param navigationStrategy
//	 *            the strategy used to navigate the cells
//	 */
//	public TreeViewerFocusCellManager(TreeViewer viewer,
//			FocusCellHighlighter focusDrawingDelegate,
//			CellNavigationStrategy navigationStrategy) {
//		super(viewer, focusDrawingDelegate, navigationStrategy);
//	}
//
//	ViewerCell getInitialFocusCell() {
//		Tree tree = (Tree) getViewer().getControl();
//
//		if (tree.getItemCount() > 0) {
//			return getViewer().getViewerRowFromItem(tree.getItem(0)).getCell(0);
//		}
//
//		return null;
//	}
//}
