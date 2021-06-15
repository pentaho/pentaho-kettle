/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *                                                 fix in bug: 210752
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * This class is responsible to provide the concept of cells for {@link Table}.
 * This concept is needed to provide features like editor activation with the
 * keyboard
 *
 * @since 1.2
 *
 */
public class TableViewerFocusCellManager extends SWTFocusCellManager {
	private static final CellNavigationStrategy TABLE_NAVIGATE = new CellNavigationStrategy();

	/**
	 * Create a new manager with a default navigation strategy:
	 * <ul>
	 * <li><code>SWT.ARROW_UP</code>: navigate to cell above</li>
	 * <li><code>SWT.ARROW_DOWN</code>: navigate to cell below</li>
	 * <li><code>SWT.ARROW_RIGHT</code>: navigate to next visible cell on
	 * the right</li>
	 * <li><code>SWT.ARROW_LEFT</code>: navigate to next visible cell on the
	 * left</li>
	 * </ul>
	 *
	 * @param viewer
	 *            the viewer the manager is bound to
	 * @param focusDrawingDelegate
	 *            the delegate responsible to highlight selected cell
	 */
	public TableViewerFocusCellManager(TableViewer viewer,
			FocusCellHighlighter focusDrawingDelegate) {
		this(viewer, focusDrawingDelegate, TABLE_NAVIGATE);
	}

	/**
	 * Create a new manager
	 *
	 * @param viewer
	 *            the viewer the manager is bound to
	 * @param focusDrawingDelegate
	 *            the delegate responsible to highlight selected cell
	 * @param navigationStrategy
	 *            the strategy used to navigate the cells
	 */
	public TableViewerFocusCellManager(TableViewer viewer,
			FocusCellHighlighter focusDrawingDelegate,
			CellNavigationStrategy navigationStrategy) {
		super(viewer, focusDrawingDelegate, navigationStrategy);
	}	

	ViewerCell getInitialFocusCell() {
		Table table = (Table) getViewer().getControl();

		if (table.isDisposed() || table.getItemCount() == 0) {
		  return null;
		}
		TableItem topItem = table.getItem(table.getTopIndex());
		if (topItem != null && !topItem.isDisposed()) {
			final ViewerRow aViewerRow = getViewer().getViewerRowFromItem(topItem);
			if (table.getColumnCount() == 0) {
				return aViewerRow.getCell(0);
			}

			Rectangle clientArea = table.getClientArea();
			for (int i = 0; i < table.getColumnCount(); i++) {
				if (aViewerRow.getWidth(i) > 0 && columnInVisibleArea(clientArea,aViewerRow,i))
					return aViewerRow.getCell(i);
				}
			}

		return null;
	}
	
	private boolean columnInVisibleArea(Rectangle clientArea, ViewerRow row, int colIndex) {
		return row.getBounds(colIndex).x >= clientArea.x;
	}

// RAP [if]	361052: Focused cell is not highlighted if navigating with CTRL + Arrow Up/Down
//	        https://bugs.eclipse.org/bugs/show_bug.cgi?id=361052
//	public ViewerCell getFocusCell() {
//		ViewerCell cell = super.getFocusCell();
//		Table t = (Table) getViewer().getControl();
//
//		// It is possible that the selection has changed under the hood
//		if (cell != null) {
//			if (t.getSelection().length == 1
//					&& t.getSelection()[0] != cell.getItem()) {
//				setFocusCell(getViewer().getViewerRowFromItem(
//						t.getSelection()[0]).getCell(cell.getColumnIndex()));
//			}
//		}
//
//		return super.getFocusCell();
//	}
// RAPEND
}
