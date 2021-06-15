/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thierry Lach - thierry.lach@bbdodetroit.com - Fix for Bug 37155
 *******************************************************************************/
package org.eclipse.jface.preference;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * An abstract field editor for a string type preference that presents
 * a string input field with a change button to its right to edit the
 * input field's content. When the user presses the change button, the
 * abstract framework method <code>changePressed()</code> gets called
 * to compute a new string.
 */
public abstract class StringButtonFieldEditor extends StringFieldEditor {

    /**
     * The change button, or <code>null</code> if none
     * (before creation and after disposal).
     */
    private Button changeButton;

    /**
     * The text for the change button, or <code>null</code> if missing.
     */
    private String changeButtonText;

    /**
     * Creates a new string button field editor 
     */
    protected StringButtonFieldEditor() {
    }

    /**
     * Creates a string button field editor.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
    protected StringButtonFieldEditor(String name, String labelText,
            Composite parent) {
        init(name, labelText);
        createControl(parent);
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void adjustForNumColumns(int numColumns) {
        ((GridData) getTextControl().getLayoutData()).horizontalSpan = numColumns - 2;
    }

    /**
     * Notifies that this field editor's change button has been pressed.
     * <p>
     * Subclasses must implement this method to provide a corresponding
     * new string for the text field. If the returned value is <code>null</code>,
     * the currently displayed value remains.
     * </p>
     *
     * @return the new string to display, or <code>null</code> to leave the
     *  old string showing
     */
    protected abstract String changePressed();

    /* (non-Javadoc)
     * Method declared on StringFieldEditor (and FieldEditor).
     */
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        super.doFillIntoGrid(parent, numColumns - 1);
        changeButton = getChangeControl(parent);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        int widthHint = convertHorizontalDLUsToPixels(changeButton,
                IDialogConstants.BUTTON_WIDTH);
        gd.widthHint = Math.max(widthHint, changeButton.computeSize(
                SWT.DEFAULT, SWT.DEFAULT, true).x);
        changeButton.setLayoutData(gd);
    }

    /**
     * Get the change control. Create it in parent if required.
     * @param parent
     * @return Button
     */
    protected Button getChangeControl(Composite parent) {
        if (changeButton == null) {
            changeButton = new Button(parent, SWT.PUSH);
            if (changeButtonText == null) {
				changeButtonText = JFaceResources.getString("openChange"); //$NON-NLS-1$
			}
            changeButton.setText(changeButtonText);
            changeButton.setFont(parent.getFont());
            changeButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent evt) {
                    String newValue = changePressed();
                    if (newValue != null) {
                        setStringValue(newValue);
                    }
                }
            });
            changeButton.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent event) {
                    changeButton = null;
                }
            });
        } else {
            checkParent(changeButton, parent);
        }
        return changeButton;
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    public int getNumberOfControls() {
        return 3;
    }

    /**
     * Returns this field editor's shell.
     *
     * @return the shell
     */
    protected Shell getShell() {
        if (changeButton == null) {
			return null;
		}
        return changeButton.getShell();
    }

    /**
     * Sets the text of the change button.
     *
     * @param text the new text
     */
    public void setChangeButtonText(String text) {
        Assert.isNotNull(text);
        changeButtonText = text;
        if (changeButton != null) {
			changeButton.setText(text);
			Point prefSize = changeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			GridData data = (GridData)changeButton.getLayoutData();
			data.widthHint = Math.max(SWT.DEFAULT, prefSize.x);
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditor#setEnabled(boolean, org.eclipse.swt.widgets.Composite)
     */
    public void setEnabled(boolean enabled, Composite parent) {
        super.setEnabled(enabled, parent);
        if (changeButton != null) {
            changeButton.setEnabled(enabled);
        }
    }

}
