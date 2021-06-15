/*******************************************************************************
 * Copyright (c) 2008 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation (bug 174739)
 ******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Composite;

/**
 * Abstract base class for Cell-Editors presented as combo boxes
 *
 * @since 1.2
 *
 */
abstract class AbstractComboBoxCellEditor extends CellEditor {
	/**
	 * The list is dropped down when the activation is done through the mouse
	 */
	public static final int DROP_DOWN_ON_MOUSE_ACTIVATION = 1;

	/**
	 * The list is dropped down when the activation is done through the keyboard
	 */
	public static final int DROP_DOWN_ON_KEY_ACTIVATION = 1 << 1;

	/**
	 * The list is dropped down when the activation is done without
	 * ui-interaction
	 */
	public static final int DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION = 1 << 2;

	/**
	 * The list is dropped down when the activation is done by traversing from
	 * cell to cell
	 */
	public static final int DROP_DOWN_ON_TRAVERSE_ACTIVATION = 1 << 3;

	private int activationStyle = SWT.NONE;

	/**
	 * Create a new cell-editor
	 *
	 * @param parent
	 *            the parent of the combo
	 * @param style
	 *            the style used to create the combo
	 */
	AbstractComboBoxCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Creates a new cell editor with no control and no st of choices.
	 * Initially, the cell editor has no cell validator.
	 *
	 */
	AbstractComboBoxCellEditor() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.CellEditor#activate(org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent)
	 */
	public void activate(ColumnViewerEditorActivationEvent activationEvent) {
		super.activate(activationEvent);
		if (activationStyle != SWT.NONE) {
			boolean dropDown = false;
			if ((activationEvent.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION || activationEvent.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION)
					&& (activationStyle & DROP_DOWN_ON_MOUSE_ACTIVATION) != 0 ) {
				dropDown = true;
			} else if (activationEvent.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED
					&& (activationStyle & DROP_DOWN_ON_KEY_ACTIVATION) != 0 ) {
				dropDown = true;
			} else if (activationEvent.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
					&& (activationStyle & DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION) != 0) {
				dropDown = true;
			} else if (activationEvent.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
					&& (activationStyle & DROP_DOWN_ON_TRAVERSE_ACTIVATION) != 0) {
				dropDown = true;
			}

			if (dropDown) {
				getControl().getDisplay().asyncExec(new Runnable() {

					public void run() {
						((CCombo) getControl()).setListVisible(true);
					}

				});

			}
		}
	}

	/**
	 * This method allows to control how the combo reacts when activated
	 *
	 * @param activationStyle
	 *            the style used
	 */
	public void setActivationStyle(int activationStyle) {
		this.activationStyle = activationStyle;
	}
}
