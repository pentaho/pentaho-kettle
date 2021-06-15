/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     											   fix in bug 151295,167325,201905
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.io.Serializable;

import org.eclipse.core.runtime.Assert;

/**
 * EditingSupport is the abstract superclass of the support for cell editing.
 *
 * @since 1.0
 *
 */
public abstract class EditingSupport implements Serializable {

	private ColumnViewer viewer;

	/**
	 * @param viewer
	 *            a new viewer
	 */
	public EditingSupport(ColumnViewer viewer) {
		Assert.isNotNull(viewer, "Viewer is not allowed to be null"); //$NON-NLS-1$
		this.viewer = viewer;
	}

	/**
	 * The editor to be shown
	 *
	 * @param element
	 *            the model element
	 * @return the CellEditor
	 * @since 1.2
	 */
	protected abstract CellEditor getCellEditor(Object element);

	/**
	 * Is the cell editable
	 *
	 * @param element
	 *            the model element
	 * @return true if editable
	 */
	protected abstract boolean canEdit(Object element);

	/**
	 * Get the value to set to the editor
	 *
	 * @param element
	 *            the model element
	 * @return the value shown
	 */
	protected abstract Object getValue(Object element);

	/**
	 * Sets the new value on the given element. Note that implementers need to
	 * ensure that <code>getViewer().update(element, null)</code> or similar
	 * methods are called, either directly or through some kind of listener
	 * mechanism on the implementer's model, to cause the new value to appear in
	 * the viewer.
	 *
	 * <p>
	 * <b>Subclasses should overwrite.</b>
	 * </p>
	 *
	 * @param element
	 *            the model element
	 * @param value
	 *            the new value
	 */
	protected abstract void setValue(Object element, Object value);

	/**
	 * @return the viewer this editing support works for
	 */
	public ColumnViewer getViewer() {
		return viewer;
	}

	/**
	 * Initialize the editor. Frameworks like Databinding can hook in here and provide
	 * a customized implementation. <p><b>Standard customers should not overwrite this method but {@link #getValue(Object)}</b></p>
	 *
	 * @param cellEditor
	 *            the cell editor
	 * @param cell
	 *            the cell the editor is working for
	 * @since 1.2
	 */
	protected void initializeCellEditorValue(CellEditor cellEditor, ViewerCell cell) {
		Object value = getValue(cell.getElement());
		cellEditor.setValue(value);
	}

	/**
	 * Save the value of the cell editor back to the model. Frameworks like Databinding can hook in here and provide
	 * a customized implementation. <p><b>Standard customers should not overwrite this method but {@link #setValue(Object, Object)} </b></p>
	 * @param cellEditor
	 *            the cell-editor
	 * @param cell
	 * 			  the cell the editor is working for
	 * @since 1.2
	 */
	protected void saveCellEditorValue(CellEditor cellEditor, ViewerCell cell) {
		Object value = cellEditor.getValue();
		setValue(cell.getElement(), value);
	}

	boolean isLegacySupport() {
		return false;
	}
}
