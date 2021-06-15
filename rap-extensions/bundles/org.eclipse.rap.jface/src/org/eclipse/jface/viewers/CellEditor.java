/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - bugfix in: 187963, 218336
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.io.Serializable;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.internal.util.SerializableListenerList;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Abstract base class for cell editors. Implements property change listener
 * handling, and SWT window management.
 * <p>
 * Subclasses implement particular kinds of cell editors. This package contains
 * various specialized cell editors:
 * <ul>
 * <li><code>TextCellEditor</code> - for simple text strings</li>
 * <li><code>ColorCellEditor</code> - for colors</li>
 * <li><code>ComboBoxCellEditor</code> - value selected from drop-down combo
 * box</li>
 * <li><code>CheckboxCellEditor</code> - boolean valued checkbox</li>
 * <li><code>DialogCellEditor</code> - value from arbitrary dialog</li>
 * </ul>
 * </p>
 * 
 * @since 1.2
 */
public abstract class CellEditor implements Serializable {

	/**
	 * List of cell editor listeners (element type:
	 * <code>ICellEditorListener</code>).
	 */
	private ListenerList listeners = new SerializableListenerList();

	/**
	 * List of cell editor property change listeners (element type:
	 * <code>IPropertyChangeListener</code>).
	 */
	private ListenerList propertyChangeListeners = new SerializableListenerList();

	/**
	 * Indicates whether this cell editor's current value is valid.
	 */
	private boolean valid = false;

	/**
	 * Optional cell editor validator; <code>null</code> if none.
	 */
	private ICellEditorValidator validator = null;

	/**
	 * The error message string to display for invalid values; <code>null</code>
	 * if none (that is, the value is valid).
	 */
	private String errorMessage = null;

	/**
	 * Indicates whether this cell editor has been changed recently.
	 */
	private boolean dirty = false;

	/**
	 * This cell editor's control, or <code>null</code> if not created yet.
	 */
	private Control control = null;

	/**
	 * Default cell editor style
	 */
	private static final int defaultStyle = SWT.NONE;

	/**
	 * This cell editor's style
	 */
	private int style = defaultStyle;

	/**
	 * Struct-like layout data for cell editors, with reasonable defaults for
	 * all fields.
	 * 
	 * @noextend This class is not intended to be subclassed by clients.
	 */
	public static class LayoutData {
		/**
		 * Horizontal alignment; <code>SWT.LEFT</code> by default.
		 */
		public int horizontalAlignment = SWT.LEFT;

		/**
		 * Indicates control grabs additional space; <code>true</code> by
		 * default.
		 */
		public boolean grabHorizontal = true;

		/**
		 * Minimum width in pixels; <code>50</code> pixels by default.
		 */
		public int minimumWidth = 50;

		/**
		 * Minimum height in pixels; by default the height is aligned to the
		 * row-height
		 */
		public int minimumHeight = SWT.DEFAULT;

		/**
		 * The vertical alignment; <code>SWT.CENTER</code> by default.
		 */
		public int verticalAlignment = SWT.CENTER;
	}

	/**
	 * Property name for the copy action
	 */
	public static final String COPY = "copy"; //$NON-NLS-1$

	/**
	 * Property name for the cut action
	 */
	public static final String CUT = "cut"; //$NON-NLS-1$

	/**
	 * Property name for the delete action
	 */
	public static final String DELETE = "delete"; //$NON-NLS-1$

	/**
	 * Property name for the find action
	 */
	public static final String FIND = "find"; //$NON-NLS-1$

	/**
	 * Property name for the paste action
	 */
	public static final String PASTE = "paste"; //$NON-NLS-1$

	/**
	 * Property name for the redo action
	 */
	public static final String REDO = "redo"; //$NON-NLS-1$

	/**
	 * Property name for the select all action
	 */
	public static final String SELECT_ALL = "selectall"; //$NON-NLS-1$

	/**
	 * Property name for the undo action
	 */
	public static final String UNDO = "undo"; //$NON-NLS-1$

	/**
	 * Creates a new cell editor with no control The cell editor has no cell
	 * validator.
	 */
	protected CellEditor() {
	}

	/**
	 * Creates a new cell editor under the given parent control. The cell editor
	 * has no cell validator.
	 *
	 * @param parent
	 *            the parent control
	 */
	protected CellEditor(Composite parent) {
		this(parent, defaultStyle);
	}

	/**
	 * Creates a new cell editor under the given parent control. The cell editor
	 * has no cell validator.
	 *
	 * @param parent
	 *            the parent control
	 * @param style
	 *            the style bits
	 */
	protected CellEditor(Composite parent, int style) {
		this.style = style;
		create(parent);
	}

	/**
	 * Activates this cell editor.
	 * <p>
	 * The default implementation of this framework method does nothing.
	 * Subclasses may reimplement.
	 * </p>
	 */
	public void activate() {
	}

	/**
	 * Adds a listener to this cell editor. Has no effect if an identical
	 * listener is already registered.
	 *
	 * @param listener
	 *            a cell editor listener
	 */
	public void addListener(ICellEditorListener listener) {
		listeners.add(listener);
	}

	/**
	 * Adds a property change listener to this cell editor. Has no effect if an
	 * identical property change listener is already registered.
	 *
	 * @param listener
	 *            a property change listener
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.add(listener);
	}

	/**
	 * Creates the control for this cell editor under the given parent control.
	 * <p>
	 * This framework method must be implemented by concrete subclasses.
	 * </p>
	 *
	 * @param parent
	 *            the parent control
	 * @return the new control, or <code>null</code> if this cell editor has
	 *         no control
	 */
	protected abstract Control createControl(Composite parent);

	/**
	 * Creates the control for this cell editor under the given parent control.
	 *
	 * @param parent
	 *            the parent control
	 */
	public void create(Composite parent) {
		Assert.isTrue(control == null);
		control = createControl(parent);
		// See 1GD5CA6: ITPUI:ALL - TaskView.setSelection does not work
		// Control is created with getVisible()==true by default.
		// This causes composite.setFocus() to work incorrectly.
		// The cell editor's control grabs focus instead, even if it is not
		// active.
		// Make the control invisible here by default.
		deactivate();
	}

	/**
	 * Hides this cell editor's control. Does nothing if this cell editor is not
	 * visible.
	 */
	public void deactivate() {
		if (control != null && !control.isDisposed()) {
			control.setVisible(false);
		}
	}

	/**
	 * Disposes of this cell editor and frees any associated SWT resources.
	 */
	public void dispose() {
		if (control != null && !control.isDisposed()) {
			control.dispose();
		}
		control = null;
	}

	/**
	 * Returns this cell editor's value.
	 * <p>
	 * This framework method must be implemented by concrete subclasses.
	 * </p>
	 *
	 * @return the value of this cell editor
	 * @see #getValue
	 */
	protected abstract Object doGetValue();

	/**
	 * Sets the focus to the cell editor's control.
	 * <p>
	 * This framework method must be implemented by concrete subclasses.
	 * </p>
	 *
	 * @see #setFocus
	 */
	protected abstract void doSetFocus();

	/**
	 * Sets this cell editor's value.
	 * <p>
	 * This framework method must be implemented by concrete subclasses.
	 * </p>
	 *
	 * @param value
	 *            the value of this cell editor
	 * @see #setValue
	 */
	protected abstract void doSetValue(Object value);

	/**
	 * Notifies all registered cell editor listeners of an apply event. Only
	 * listeners registered at the time this method is called are notified.
	 *
	 * @see ICellEditorListener#applyEditorValue
	 */
	protected void fireApplyEditorValue() {
		Object[] array = listeners.getListeners();
		for (int i = 0; i < array.length; i++) {
			final ICellEditorListener l = (ICellEditorListener) array[i];
			SafeRunnable.run(new SafeRunnable() {
				public void run() {
					l.applyEditorValue();
				}
			});
		}
	}

	/**
	 * Notifies all registered cell editor listeners that editing has been
	 * canceled.
	 *
	 * @see ICellEditorListener#cancelEditor
	 */
	protected void fireCancelEditor() {
		Object[] array = listeners.getListeners();
		for (int i = 0; i < array.length; i++) {
			final ICellEditorListener l = (ICellEditorListener) array[i];
			SafeRunnable.run(new SafeRunnable() {
				public void run() {
					l.cancelEditor();
				}
			});
		}
	}

	/**
	 * Notifies all registered cell editor listeners of a value change.
	 *
	 * @param oldValidState
	 *            the valid state before the end user changed the value
	 * @param newValidState
	 *            the current valid state
	 * @see ICellEditorListener#editorValueChanged
	 */
	protected void fireEditorValueChanged(final boolean oldValidState,
			final boolean newValidState) {
		Object[] array = listeners.getListeners();
		for (int i = 0; i < array.length; i++) {
			final ICellEditorListener l = (ICellEditorListener) array[i];
			SafeRunnable.run(new SafeRunnable() {
				public void run() {
					l.editorValueChanged(oldValidState, newValidState);
				}
			});
		}
	}

	/**
	 * Notifies all registered property listeners of an enablement change.
	 *
	 * @param actionId
	 *            the id indicating what action's enablement has changed.
	 */
	protected void fireEnablementChanged(final String actionId) {
		Object[] array = propertyChangeListeners.getListeners();
		for (int i = 0; i < array.length; i++) {
			final IPropertyChangeListener l = (IPropertyChangeListener) array[i];
			SafeRunnable.run(new SafeRunnable() {
				public void run() {
					l.propertyChange(new PropertyChangeEvent(this, actionId,
							null, null));
				}
			});
		}
	}

	/**
	 * Sets the style bits for this cell editor.
	 *
	 * @param style
	 *            the SWT style bits for this cell editor
	 */
	public void setStyle(int style) {
		this.style = style;
	}

	/**
	 * Returns the style bits for this cell editor.
	 *
	 * @return the style for this cell editor
	 */
	public int getStyle() {
		return style;
	}

	/**
	 * Returns the control used to implement this cell editor.
	 *
	 * @return the control, or <code>null</code> if this cell editor has no
	 *         control
	 */
	public Control getControl() {
		return control;
	}

	/**
	 * Returns the current error message for this cell editor.
	 *
	 * @return the error message if the cell editor is in an invalid state, and
	 *         <code>null</code> if the cell editor is valid
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Returns a layout data object for this cell editor. This is called each
	 * time the cell editor is activated and controls the layout of the SWT
	 * table editor.
	 * <p>
	 * The default implementation of this method sets the minimum width to the
	 * control's preferred width. Subclasses may extend or reimplement.
	 * </p>
	 *
	 * @return the layout data object
	 */
	public LayoutData getLayoutData() {
		LayoutData result = new LayoutData();
		Control control = getControl();
		if (control != null) {
			result.minimumWidth = control.computeSize(SWT.DEFAULT, SWT.DEFAULT,
					true).x;
		}
		return result;
	}

	/**
	 * Returns the input validator for this cell editor.
	 *
	 * @return the input validator, or <code>null</code> if none
	 */
	public ICellEditorValidator getValidator() {
		return validator;
	}

	/**
	 * Returns this cell editor's value provided that it has a valid one.
	 *
	 * @return the value of this cell editor, or <code>null</code> if the cell
	 *         editor does not contain a valid value
	 */
	public final Object getValue() {
		if (!valid) {
			return null;
		}

		return doGetValue();
	}

	/**
	 * Returns whether this cell editor is activated.
	 *
	 * @return <code>true</code> if this cell editor's control is currently
	 *         activated, and <code>false</code> if not activated
	 */
	public boolean isActivated() {
		// Use the state of the visible style bit (getVisible()) rather than the
		// window's actual visibility (isVisible()) to get correct handling when
		// an ancestor control goes invisible, see bug 85331.
		return control != null && control.getVisible();
	}

	/**
	 * Returns <code>true</code> if this cell editor is able to perform the
	 * copy action.
	 * <p>
	 * This default implementation always returns <code>false</code>.
	 * </p>
	 * <p>
	 * Subclasses may override
	 * </p>
	 *
	 * @return <code>true</code> if copy is possible, <code>false</code>
	 *         otherwise
	 */
	public boolean isCopyEnabled() {
		return false;
	}

	/**
	 * Returns whether the given value is valid for this cell editor. This cell
	 * editor's validator (if any) makes the actual determination.
	 *
	 * @param value
	 *            the value to check for
	 *
	 * @return <code>true</code> if the value is valid, and <code>false</code>
	 *         if invalid
	 */
	protected boolean isCorrect(Object value) {
		errorMessage = null;
		if (validator == null) {
			return true;
		}

		errorMessage = validator.isValid(value);
		return (errorMessage == null || errorMessage.equals(""));//$NON-NLS-1$
	}

	/**
	 * Returns <code>true</code> if this cell editor is able to perform the
	 * cut action.
	 * <p>
	 * This default implementation always returns <code>false</code>.
	 * </p>
	 * <p>
	 * Subclasses may override
	 * </p>
	 *
	 * @return <code>true</code> if cut is possible, <code>false</code>
	 *         otherwise
	 */
	public boolean isCutEnabled() {
		return false;
	}

	/**
	 * Returns <code>true</code> if this cell editor is able to perform the
	 * delete action.
	 * <p>
	 * This default implementation always returns <code>false</code>.
	 * </p>
	 * <p>
	 * Subclasses may override
	 * </p>
	 *
	 * @return <code>true</code> if delete is possible, <code>false</code>
	 *         otherwise
	 */
	public boolean isDeleteEnabled() {
		return false;
	}

	/**
	 * Returns whether the value of this cell editor has changed since the last
	 * call to <code>setValue</code>.
	 *
	 * @return <code>true</code> if the value has changed, and
	 *         <code>false</code> if unchanged
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Marks this cell editor as dirty.
	 */
	protected void markDirty() {
		dirty = true;
	}

	/**
	 * Returns <code>true</code> if this cell editor is able to perform the
	 * find action.
	 * <p>
	 * This default implementation always returns <code>false</code>.
	 * </p>
	 * <p>
	 * Subclasses may override
	 * </p>
	 *
	 * @return <code>true</code> if find is possible, <code>false</code>
	 *         otherwise
	 */
	public boolean isFindEnabled() {
		return false;
	}

	/**
	 * Returns <code>true</code> if this cell editor is able to perform the
	 * paste action.
	 * <p>
	 * This default implementation always returns <code>false</code>.
	 * </p>
	 * <p>
	 * Subclasses may override
	 * </p>
	 *
	 * @return <code>true</code> if paste is possible, <code>false</code>
	 *         otherwise
	 */
	public boolean isPasteEnabled() {
		return false;
	}

	/**
	 * Returns <code>true</code> if this cell editor is able to perform the
	 * redo action.
	 * <p>
	 * This default implementation always returns <code>false</code>.
	 * </p>
	 * <p>
	 * Subclasses may override
	 * </p>
	 *
	 * @return <code>true</code> if redo is possible, <code>false</code>
	 *         otherwise
	 */
	public boolean isRedoEnabled() {
		return false;
	}

	/**
	 * Returns <code>true</code> if this cell editor is able to perform the
	 * select all action.
	 * <p>
	 * This default implementation always returns <code>false</code>.
	 * </p>
	 * <p>
	 * Subclasses may override
	 * </p>
	 *
	 * @return <code>true</code> if select all is possible, <code>false</code>
	 *         otherwise
	 */
	public boolean isSelectAllEnabled() {
		return false;
	}

	/**
	 * Returns <code>true</code> if this cell editor is able to perform the
	 * undo action.
	 * <p>
	 * This default implementation always returns <code>false</code>.
	 * </p>
	 * <p>
	 * Subclasses may override
	 * </p>
	 *
	 * @return <code>true</code> if undo is possible, <code>false</code>
	 *         otherwise
	 */
	public boolean isUndoEnabled() {
		return false;
	}

	/**
	 * Returns whether this cell editor has a valid value. The default value is
	 * false.
	 *
	 * @return <code>true</code> if the value is valid, and <code>false</code>
	 *         if invalid
	 *
	 * @see #setValueValid(boolean)
	 */
	public boolean isValueValid() {
		return valid;
	}

	/**
	 * Processes a key release event that occurred in this cell editor.
	 * <p>
	 * The default implementation of this framework method cancels editing when
	 * the ESC key is pressed. When the RETURN key is pressed the current value
	 * is applied and the cell editor deactivates. Subclasses should call this
	 * method at appropriate times. Subclasses may also extend or reimplement.
	 * </p>
	 *
	 * @param keyEvent
	 *            the key event
	 */
	protected void keyReleaseOccured(KeyEvent keyEvent) {
		if (keyEvent.character == '\u001b') { // Escape character
			fireCancelEditor();
		} else if (keyEvent.character == '\r') { // Return key
			fireApplyEditorValue();
			deactivate();
		}
	}

	/**
	 * Processes a focus lost event that occurred in this cell editor.
	 * <p>
	 * The default implementation of this framework method applies the current
	 * value and deactivates the cell editor. Subclasses should call this method
	 * at appropriate times. Subclasses may also extend or reimplement.
	 * </p>
	 */
	protected void focusLost() {
		if (isActivated()) {
			fireApplyEditorValue();
			deactivate();
		}
	}

	/**
	 * Performs the copy action. This default implementation does nothing.
	 * <p>
	 * Subclasses may override
	 * </p>
	 */
	public void performCopy() {
	}

	/**
	 * Performs the cut action. This default implementation does nothing.
	 * <p>
	 * Subclasses may override
	 * </p>
	 */
	public void performCut() {
	}

	/**
	 * Performs the delete action. This default implementation does nothing.
	 * <p>
	 * Subclasses may override
	 * </p>
	 */
	public void performDelete() {
	}

	/**
	 * Performs the find action. This default implementation does nothing.
	 * <p>
	 * Subclasses may override
	 * </p>
	 */
	public void performFind() {
	}

	/**
	 * Performs the paste action. This default implementation does nothing.
	 * <p>
	 * Subclasses may override
	 * </p>
	 */
	public void performPaste() {
	}

	/**
	 * Performs the redo action. This default implementation does nothing.
	 * <p>
	 * Subclasses may override
	 * </p>
	 */
	public void performRedo() {
	}

	/**
	 * Performs the select all action. This default implementation does nothing.
	 * <p>
	 * Subclasses may override
	 * </p>
	 */
	public void performSelectAll() {
	}

	/**
	 * Performs the undo action. This default implementation does nothing.
	 * <p>
	 * Subclasses may override
	 * </p>
	 */
	public void performUndo() {
	}

	/**
	 * Removes the given listener from this cell editor. Has no effect if an
	 * identical listener is not registered.
	 *
	 * @param listener
	 *            a cell editor listener
	 */
	public void removeListener(ICellEditorListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Removes the given property change listener from this cell editor. Has no
	 * effect if an identical property change listener is not registered.
	 *
	 * @param listener
	 *            a property change listener
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.remove(listener);
	}

	/**
	 * Sets or clears the current error message for this cell editor.
	 * <p>
	 * No formatting is done here, the message to be set is expected to be fully
	 * formatted before being passed in.
	 * </p>
	 *
	 * @param message
	 *            the error message, or <code>null</code> to clear
	 */
	protected void setErrorMessage(String message) {
		errorMessage = message;
	}

	/**
	 * Sets the focus to the cell editor's control.
	 */
	public void setFocus() {
		doSetFocus();
	}

	/**
	 * Sets the input validator for this cell editor.
	 *
	 * @param validator
	 *            the input validator, or <code>null</code> if none
	 */
	public void setValidator(ICellEditorValidator validator) {
		this.validator = validator;
	}

	/**
	 * Sets this cell editor's value.
	 *
	 * @param value
	 *            the value of this cell editor
	 */
	public final void setValue(Object value) {
		valid = isCorrect(value);
		dirty = false;
		doSetValue(value);
	}

	/**
	 * Sets the valid state of this cell editor. The default value is false.
	 * Subclasses should call this method on construction.
	 *
	 * @param valid
	 *            <code>true</code> if the current value is valid, and
	 *            <code>false</code> if invalid
	 *
	 * @see #isValueValid
	 */
	protected void setValueValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * The value has changed. Updates the valid state flag, marks this cell
	 * editor as dirty, and notifies all registered cell editor listeners of a
	 * value change.
	 *
	 * @param oldValidState
	 *            the valid state before the end user changed the value
	 * @param newValidState
	 *            the current valid state
	 * @see ICellEditorListener#editorValueChanged
	 */
	protected void valueChanged(boolean oldValidState, boolean newValidState) {
		valid = newValidState;
		dirty = true;
		fireEditorValueChanged(oldValidState, newValidState);
	}

	/**
	 * Activate the editor but also inform the editor which event triggered its
	 * activation. <b>The default implementation simply calls
	 * {@link #activate()}</b>
	 *
	 * @param activationEvent
	 *            the editor activation event
	 */
	public void activate(ColumnViewerEditorActivationEvent activationEvent) {
		activate();
	}

	/**
	 * The default implementation of this method returns true. Subclasses that
	 * hook their own focus listener should override this method and return
	 * false. See also bug 58777.
	 *
	 * @return <code>true</code> to indicate that a focus listener has to be
	 *         attached
	 */
	protected boolean dependsOnExternalFocusListener() {
		return true;
	}

	/**
	 * @param event
	 *            deactivation event
	 *
	 */
	protected void deactivate(ColumnViewerEditorDeactivationEvent event) {
		deactivate();
	}

	/**
	 * Returns the duration, in milliseconds, between the mouse button click
	 * that activates the cell editor and a subsequent mouse button click that
	 * will be considered a <em>double click</em> on the underlying control.
	 * Clients may override, in particular, clients can return 0 to denote that
	 * two subsequent mouse clicks in a cell should not be interpreted as a
	 * double click.
	 *
	 * @return the timeout or <code>0</code>
	 */
	protected int getDoubleClickTimeout() {
		return Display.getCurrent().getDoubleClickTime();
	}
}
