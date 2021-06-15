/*******************************************************************************
 * Copyright (c) 2006, 2010 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *                                                 bugfix in 174739
 *     Eric Rizzo - bug 213315
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.text.MessageFormat; // Not using ICU to support standalone JFace
// scenario

import org.eclipse.core.runtime.Assert;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A cell editor that presents a list of items in a combo box. In contrast to
 * {@link ComboBoxCellEditor} it wraps the underlying {@link CCombo} using a
 * {@link ComboViewer}
 * @since 1.2
 */
public class ComboBoxViewerCellEditor extends AbstractComboBoxCellEditor {

	/**
	 * The custom combo box control.
	 */
	ComboViewer viewer;

	Object selectedValue;

	/**
	 * Default ComboBoxCellEditor style
	 */
	private static final int defaultStyle = SWT.NONE;

	/**
	 * Creates a new cell editor with a combo viewer and a default style
	 *
	 * @param parent
	 *            the parent control
	 */
	public ComboBoxViewerCellEditor(Composite parent) {
		this(parent, defaultStyle);
	}

	/**
	 * Creates a new cell editor with a combo viewer and the given style
	 *
	 * @param parent
	 *            the parent control
	 * @param style
	 *            the style bits
	 */
	public ComboBoxViewerCellEditor(Composite parent, int style) {
		super(parent, style);
		setValueValid(true);
	}

	/*
	 * (non-Javadoc) Method declared on CellEditor.
	 */
	protected Control createControl(Composite parent) {

		CCombo comboBox = new CCombo(parent, getStyle());
		comboBox.setFont(parent.getFont());
		viewer = new ComboViewer(comboBox);

		comboBox.addKeyListener(new KeyAdapter() {
			// hook key pressed - see PR 14201
			public void keyPressed(KeyEvent e) {
				keyReleaseOccured(e);
			}
		});

		comboBox.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				applyEditorValueAndDeactivate();
			}

			public void widgetSelected(SelectionEvent event) {
				ISelection selection = viewer.getSelection();
				if (selection.isEmpty()) {
					selectedValue = null;
				} else {
					selectedValue = ((IStructuredSelection) selection)
							.getFirstElement();
				}
			}
		});

		comboBox.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE
						|| e.detail == SWT.TRAVERSE_RETURN) {
					e.doit = false;
				}
			}
		});
// RAP [if] Use CANCEL_KEYS instead of doit = false
        String[] cancelKeys = new String[] {
          "ESC", "ENTER" //$NON-NLS-1$ //$NON-NLS-2$
        };
        comboBox.setData( RWT.CANCEL_KEYS, cancelKeys );
// ENDRAP

		comboBox.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				ComboBoxViewerCellEditor.this.focusLost();
			}
		});
		return comboBox;
	}

	/**
	 * The <code>ComboBoxCellEditor</code> implementation of this
	 * <code>CellEditor</code> framework method returns the zero-based index
	 * of the current selection.
	 *
	 * @return the zero-based index of the current selection wrapped as an
	 *         <code>Integer</code>
	 */
	protected Object doGetValue() {
		return selectedValue;
	}

	/*
	 * (non-Javadoc) Method declared on CellEditor.
	 */
	protected void doSetFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * The <code>ComboBoxCellEditor</code> implementation of this
	 * <code>CellEditor</code> framework method sets the minimum width of the
	 * cell. The minimum width is 10 characters if <code>comboBox</code> is
	 * not <code>null</code> or <code>disposed</code> eles it is 60 pixels
	 * to make sure the arrow button and some text is visible. The list of
	 * CCombo will be wide enough to show its longest item.
	 */
	public LayoutData getLayoutData() {
		LayoutData layoutData = super.getLayoutData();
		if ((viewer.getControl() == null) || viewer.getControl().isDisposed()) {
			layoutData.minimumWidth = 60;
		} else {
			// make the comboBox 10 characters wide
			GC gc = new GC(viewer.getControl());
			layoutData.minimumWidth = (gc.getFontMetrics()
					.getAverageCharWidth() * 10) + 10;
			gc.dispose();
		}
		return layoutData;
	}

	/**
	 * Set a new value
	 *
	 * @param value
	 *            the new value
	 */
	protected void doSetValue(Object value) {
	    Assert.isTrue(viewer != null);
	    selectedValue = value;
	    if (value == null) {
	        viewer.setSelection(StructuredSelection.EMPTY);
	    } else {
	        viewer.setSelection(new StructuredSelection(value));
	    }
	}

	/**
	 * @param labelProvider
	 *            the label provider used
	 * @see StructuredViewer#setLabelProvider(IBaseLabelProvider)
	 */
	public void setLabelProvider(IBaseLabelProvider labelProvider) {
		viewer.setLabelProvider(labelProvider);
	}

	/**
	 * @param provider
	 *            the content provider used
	 * @see StructuredViewer#setContentProvider(IContentProvider)
	 * @since 1.4
	 */
	public void setContentProvider(IStructuredContentProvider provider) {
		viewer.setContentProvider(provider);
	}

	/**
	 * @param provider
	 *            the content provider used
	 * @see StructuredViewer#setContentProvider(IContentProvider)
	 * @deprecated As of 1.4, replaced by
	 *             {@link #setContentProvider(IStructuredContentProvider)}	 
	 */
	public void setContenProvider(IStructuredContentProvider provider) {
		viewer.setContentProvider(provider);
	}

	/**
	 * @param input
	 *            the input used
	 * @see StructuredViewer#setInput(Object)
	 */
	public void setInput(Object input) {
		viewer.setInput(input);
	}

	/**
	 * @return get the viewer
	 */
	public ComboViewer getViewer() {
		return viewer;
	}

	/**
	 * Applies the currently selected value and deactiavates the cell editor
	 */
	void applyEditorValueAndDeactivate() {
		// must set the selection before getting value
		ISelection selection = viewer.getSelection();
		if (selection.isEmpty()) {
			selectedValue = null;
		} else {
			selectedValue = ((IStructuredSelection) selection)
					.getFirstElement();
		}

		Object newValue = doGetValue();
		markDirty();
		boolean isValid = isCorrect(newValue);
		setValueValid(isValid);

		if (!isValid) {
			MessageFormat.format(getErrorMessage(),
					new Object[] { selectedValue });
		}

		fireApplyEditorValue();
		deactivate();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.CellEditor#focusLost()
	 */
	protected void focusLost() {
		if (isActivated()) {
			applyEditorValueAndDeactivate();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.CellEditor#keyReleaseOccured(org.eclipse.swt.events.KeyEvent)
	 */
	protected void keyReleaseOccured(KeyEvent keyEvent) {
		if (keyEvent.character == '\u001b') { // Escape character
			fireCancelEditor();
		} else if (keyEvent.character == '\t') { // tab key
			applyEditorValueAndDeactivate();
// RAP [if] With CANCEL_KEYS in place we need to apply the editor value on key listener as
//          defaultSelection event is not fired if ENTER is canceled
        } else if (keyEvent.character == '\r') { // enter key
            applyEditorValueAndDeactivate();
        }
// ENDRAP
	}
}
