/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.viewers;

import java.io.Serializable;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

/**
 * This class implementation the strategy how the table is navigated using the
 * keyboard.
 * 
 * <p>
 * <b>Subclasses can implement their custom navigation algorithms</b>
 * </p>
 * 
 * @since 1.2
 * 
 */
public class CellNavigationStrategy implements Serializable {
	/**
	 * is the given event an event which moves the selection to another cell
	 * 
	 * @param viewer
	 *            the viewer we are working for
	 * @param event
	 *            the key event
	 * @return <code>true</code> if a new cell is searched
	 */
	public boolean isNavigationEvent(ColumnViewer viewer, Event event) {
		switch (event.keyCode) {
		case SWT.ARROW_UP:
		case SWT.ARROW_DOWN:
		case SWT.ARROW_LEFT:
		case SWT.ARROW_RIGHT:
		case SWT.HOME:
		case SWT.PAGE_DOWN:
		case SWT.PAGE_UP:
		case SWT.END:
			return true;
		default:
			return false;
		}
	}

	/**
	 * @param viewer
	 *            the viewer we are working for
	 * @param cellToCollapse
	 *            the cell to collapse
	 * @param event
	 *            the key event
	 * @return <code>true</code> if this event triggers collapsing of a node
	 */
	public boolean isCollapseEvent(ColumnViewer viewer,
			ViewerCell cellToCollapse, Event event) {
		return false;
	}

	/**
	 * @param viewer
	 *            the viewer we are working for
	 * @param cellToExpand
	 *            the cell to expand
	 * @param event
	 *            the key event
	 * @return <code>true</code> if this event triggers expanding of a node
	 */
	public boolean isExpandEvent(ColumnViewer viewer, ViewerCell cellToExpand,
			Event event) {
		return false;
	}

	/**
	 * @param viewer
	 *            the viewer working for
	 * @param cellToExpand
	 *            the cell the user wants to expand
	 * @param event
	 *            the event triggering the expansion
	 */
	public void expand(ColumnViewer viewer, ViewerCell cellToExpand, Event event) {

	}

	/**
	 * @param viewer
	 *            the viewer working for
	 * @param cellToCollapse
	 *            the cell the user wants to collapse
	 * @param event
	 *            the event triggering the expansion
	 */
	public void collapse(ColumnViewer viewer, ViewerCell cellToCollapse,
			Event event) {

	}

	/**
	 * @param viewer
	 *            the viewer we are working for
	 * @param currentSelectedCell
	 *            the cell currently selected
	 * @param event
	 *            the key event
	 * @return the cell which is highlighted next or <code>null</code> if the
	 *         default implementation is taken. E.g. it's fairly impossible to
	 *         react on PAGE_DOWN requests
	 */
	public ViewerCell findSelectedCell(ColumnViewer viewer,
			ViewerCell currentSelectedCell, Event event) {

		switch (event.keyCode) {
		case SWT.ARROW_UP:
			if (currentSelectedCell != null) {
				return currentSelectedCell.getNeighbor(ViewerCell.ABOVE, false);
			}
			break;
		case SWT.ARROW_DOWN:
			if (currentSelectedCell != null) {
				return currentSelectedCell.getNeighbor(ViewerCell.BELOW, false);
			}
			break;
		case SWT.ARROW_LEFT:
			if (currentSelectedCell != null) {
				return currentSelectedCell.getNeighbor(ViewerCell.LEFT, true);
			}
			break;
		case SWT.ARROW_RIGHT:
			if (currentSelectedCell != null) {
				return currentSelectedCell.getNeighbor(ViewerCell.RIGHT, true);
			}
			break;
		}

		return null;
	}

	/**
	 * This method is consulted to decide whether an event has to be canceled or
	 * not. By default events who collapse/expand tree-nodes are canceled
	 * 
	 * @param viewer
	 *            the viewer working for
	 * @param event
	 *            the event
	 * @return <code>true</code> if the event has to be canceled
	 */
	public boolean shouldCancelEvent(ColumnViewer viewer, Event event) {
// RAP [if] Use CANCEL_KEYS instead of doit = false
	    if( viewer.getControl().getData( RWT.CANCEL_KEYS ) == null ) {
          String[] cancelKeys = new String[] {
            "ARROW_LEFT", "ARROW_RIGHT" //$NON-NLS-1$ //$NON-NLS-2$
          };
          viewer.getControl().setData( RWT.CANCEL_KEYS, cancelKeys );
	    }
// ENDRAP
		return event.keyCode == SWT.ARROW_LEFT
				|| event.keyCode == SWT.ARROW_RIGHT;
	}

	/**
	 * This method is called by the framework to initialize this navigation
	 * strategy object. Subclasses may extend.
	 */
	protected void init() {
	}
}
