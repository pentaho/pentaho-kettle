/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     <sgandon@nds.com> - Fix for bug 109389 - IntegerFieldEditor
 *     does not fire property change all the time
 *******************************************************************************/
package org.eclipse.jface.preference;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * A field editor for an integer type preference.
 */
public class IntegerFieldEditor extends StringFieldEditor {
    private int minValidValue = 0;

    private int maxValidValue = Integer.MAX_VALUE;

    private static final int DEFAULT_TEXT_LIMIT = 10;

    /**
     * Creates a new integer field editor 
     */
    protected IntegerFieldEditor() {
    }

    /**
     * Creates an integer field editor.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
    public IntegerFieldEditor(String name, String labelText, Composite parent) {
        this(name, labelText, parent, DEFAULT_TEXT_LIMIT);
    }

    /**
     * Creates an integer field editor.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     * @param textLimit the maximum number of characters in the text.
     */
    public IntegerFieldEditor(String name, String labelText, Composite parent,
            int textLimit) {
        init(name, labelText);
        setTextLimit(textLimit);
        setEmptyStringAllowed(false);
        setErrorMessage(JFaceResources
                .getString("IntegerFieldEditor.errorMessage"));//$NON-NLS-1$
        createControl(parent);
    }

    /**
     * Sets the range of valid values for this field.
     * 
     * @param min the minimum allowed value (inclusive)
     * @param max the maximum allowed value (inclusive)
     */
    public void setValidRange(int min, int max) {
        minValidValue = min;
        maxValidValue = max;
        setErrorMessage(JFaceResources.format(
        		"IntegerFieldEditor.errorMessageRange", //$NON-NLS-1$
        		new Object[] { new Integer(min), new Integer(max) }));
    }

    /* (non-Javadoc)
     * Method declared on StringFieldEditor.
     * Checks whether the entered String is a valid integer or not.
     */
    protected boolean checkState() {

        Text text = getTextControl();

        if (text == null) {
			return false;
		}

        String numberString = text.getText();
        try {
            int number = Integer.valueOf(numberString).intValue();
            if (number >= minValidValue && number <= maxValidValue) {
				clearErrorMessage();
				return true;
			}
            
			showErrorMessage();
			return false;
			
        } catch (NumberFormatException e1) {
            showErrorMessage();
        }

        return false;
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doLoad() {
        Text text = getTextControl();
        if (text != null) {
            int value = getPreferenceStore().getInt(getPreferenceName());
            text.setText("" + value);//$NON-NLS-1$
            oldValue = "" + value; //$NON-NLS-1$
        }

    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doLoadDefault() {
        Text text = getTextControl();
        if (text != null) {
            int value = getPreferenceStore().getDefaultInt(getPreferenceName());
            text.setText("" + value);//$NON-NLS-1$
        }
        valueChanged();
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doStore() {
        Text text = getTextControl();
        if (text != null) {
            Integer i = new Integer(text.getText());
            getPreferenceStore().setValue(getPreferenceName(), i.intValue());
        }
    }

    /**
     * Returns this field editor's current value as an integer.
     *
     * @return the value
     * @exception NumberFormatException if the <code>String</code> does not
     *   contain a parsable integer
     */
    public int getIntValue() throws NumberFormatException {
        return new Integer(getStringValue()).intValue();
    }
}
