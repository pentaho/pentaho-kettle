/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     											   fixes in bug 198665, 200731, 187963
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * This is an editor implementation for {@link Tree}
 *
 * @since 1.2
 */
public class TreeViewerEditor extends ColumnViewerEditor {
	/**
	 * This viewer's tree editor.
	 */
	private TreeEditor treeEditor;

	private SWTFocusCellManager focusCellManager;

	/**
	 * @param viewer
	 *            the viewer the editor is attached to
	 * @param focusCellManager
	 *            the cell focus manager if one used or <code>null</code>
	 * @param editorActivationStrategy
	 *            the strategy used to decide about the editor activation
	 * @param feature
	 *            the feature mask
	 */
	TreeViewerEditor(TreeViewer viewer, SWTFocusCellManager focusCellManager,
			ColumnViewerEditorActivationStrategy editorActivationStrategy,
			int feature) {
		super(viewer, editorActivationStrategy, feature);
		treeEditor = new TreeEditor(viewer.getTree());
		this.focusCellManager = focusCellManager;
	}

	/**
	 * Create a customized editor with focusable cells
	 *
	 * @param viewer
	 *            the viewer the editor is created for
	 * @param focusCellManager
	 *            the cell focus manager if one needed else <code>null</code>
	 * @param editorActivationStrategy
	 *            activation strategy to control if an editor activated
	 * @param feature
	 *            bit mask controlling the editor
	 *            <ul>
	 *            <li>{@link ColumnViewerEditor#DEFAULT}</li>
	 *            <li>{@link ColumnViewerEditor#TABBING_CYCLE_IN_ROW}</li>
	 *            <li>{@link ColumnViewerEditor#TABBING_HORIZONTAL}</li>
	 *            <li>{@link ColumnViewerEditor#TABBING_MOVE_TO_ROW_NEIGHBOR}</li>
	 *            <li>{@link ColumnViewerEditor#TABBING_VERTICAL}</li>
	 *            </ul>
	 * @see #create(TreeViewer, ColumnViewerEditorActivationStrategy, int)
	 */
	public static void create(TreeViewer viewer,
			SWTFocusCellManager focusCellManager,
			ColumnViewerEditorActivationStrategy editorActivationStrategy,
			int feature) {
		TreeViewerEditor editor = new TreeViewerEditor(viewer,
				focusCellManager, editorActivationStrategy, feature);
		viewer.setColumnViewerEditor(editor);
		if (focusCellManager != null) {
			focusCellManager.init();
		}
	}

	/**
	 * Create a customized editor whose activation process is customized
	 *
	 * @param viewer
	 *            the viewer the editor is created for
	 * @param editorActivationStrategy
	 *            activation strategy to control if an editor activated
	 * @param feature
	 *            bit mask controlling the editor
	 *            <ul>
	 *            <li>{@link ColumnViewerEditor#DEFAULT}</li>
	 *            <li>{@link ColumnViewerEditor#TABBING_CYCLE_IN_ROW}</li>
	 *            <li>{@link ColumnViewerEditor#TABBING_HORIZONTAL}</li>
	 *            <li>{@link ColumnViewerEditor#TABBING_MOVE_TO_ROW_NEIGHBOR}</li>
	 *            <li>{@link ColumnViewerEditor#TABBING_VERTICAL}</li>
	 *            </ul>
	 */
	public static void create(TreeViewer viewer,
			ColumnViewerEditorActivationStrategy editorActivationStrategy,
			int feature) {
		create(viewer, null, editorActivationStrategy, feature);
	}

	protected void setEditor(Control w, Item item, int fColumnNumber) {
		treeEditor.setEditor(w, (TreeItem) item, fColumnNumber);
	}

	protected void setLayoutData(LayoutData layoutData) {
		treeEditor.grabHorizontal = layoutData.grabHorizontal;
		treeEditor.horizontalAlignment = layoutData.horizontalAlignment;
		treeEditor.minimumWidth = layoutData.minimumWidth;
		treeEditor.verticalAlignment = layoutData.verticalAlignment;
		if( layoutData.minimumHeight != SWT.DEFAULT ) {
			treeEditor.minimumHeight = layoutData.minimumHeight;
		}
	}

	public ViewerCell getFocusCell() {
		if (focusCellManager != null) {
			return focusCellManager.getFocusCell();
		}

		return super.getFocusCell();
	}

	protected void updateFocusCell(ViewerCell focusCell,
			ColumnViewerEditorActivationEvent event) {
		// Update the focus cell when we activated the editor with these 2
		// events
		if (event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
				|| event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL) {

			List l = getViewer().getSelectionFromWidget();

			if (!l.contains(focusCell.getElement())) {
				getViewer().setSelection(
						new TreeSelection(focusCell.getViewerRow()
								.getTreePath()),true);
			}
			
			// Set the focus cell after the selection is updated because else
			// the cell is not scrolled into view
			if (focusCellManager != null) {
				focusCellManager.setFocusCell(focusCell);
			}
		}
	}
}
