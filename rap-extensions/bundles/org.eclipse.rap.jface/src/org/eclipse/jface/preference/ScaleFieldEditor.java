/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.preference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Scale;

/**
 * A field editor for an integer type preference. This class may be used as is,
 * or subclassed as required.
 * 
 * @since 1.0
 */
public class ScaleFieldEditor extends FieldEditor {

    /**
     * Value that will feed Scale.setIncrement(int).
     */
    private int incrementValue;

    /**
     * Value that will feed Scale.setMaximum(int).
     */
    private int maxValue;

    /**
     * Value that will feed Scale.setMinimum(int).
     */
    private int minValue;

    /**
     * Old integer value.
     */
    private int oldValue;

    /**
     * Value that will feed Scale.setPageIncrement(int).
     */
    private int pageIncrementValue;

    /**
     * The scale, or <code>null</code> if none.
     */
    protected Scale scale;

    /**
     * Creates a scale field editor.
     * 
     * @param name
     *            the name of the preference this field editor works on
     * @param labelText
     *            the label text of the field editor
     * @param parent
     *            the parent of the field editor's control
     */
    public ScaleFieldEditor(String name, String labelText, Composite parent) {
        super(name, labelText, parent);
        setDefaultValues();
    }

    /**
     * Creates a scale field editor with particular scale values.
     * 
     * @param name
     *            the name of the preference this field editor works on
     * @param labelText
     *            the label text of the field editor
     * @param parent
     *            the parent of the field editor's control
     * @param min
     *            the value used for Scale.setMinimum(int).
     * @param max
     *            the value used for Scale.setMaximum(int).
     * @param increment
     *            the value used for Scale.setIncrement(int).
     * @param pageIncrement
     *            the value used for Scale.setPageIncrement(int).
     */
    public ScaleFieldEditor(String name, String labelText, Composite parent,
            int min, int max, int increment, int pageIncrement) {
        super(name, labelText, parent);
        setValues(min, max, increment, pageIncrement);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
     */
    protected void adjustForNumColumns(int numColumns) {
        ((GridData) scale.getLayoutData()).horizontalSpan = numColumns - 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite,
     *      int)
     */
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        Control control = getLabelControl(parent);
        GridData gd = new GridData();
        control.setLayoutData(gd);

        scale = getScaleControl(parent);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.verticalAlignment = GridData.FILL;
        gd.horizontalSpan = numColumns - 1;
        gd.grabExcessHorizontalSpace = true;
        scale.setLayoutData(gd);
        updateScale();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditor#doLoad()
     */
    protected void doLoad() {
        if (scale != null) {
            int value = getPreferenceStore().getInt(getPreferenceName());
            scale.setSelection(value);
            oldValue = value;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
     */
    protected void doLoadDefault() {
        if (scale != null) {
            int value = getPreferenceStore().getDefaultInt(getPreferenceName());
            scale.setSelection(value);
        }
        valueChanged();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditor#doStore()
     */
    protected void doStore() {
        getPreferenceStore()
                .setValue(getPreferenceName(), scale.getSelection());
    }

    /**
     * Returns the value that will be used for Scale.setIncrement(int).
     * 
     * @return the value.
     * @see org.eclipse.swt.widgets.Scale#setIncrement(int)
     */
    public int getIncrement() {
        return incrementValue;
    }

    /**
     * Returns the value that will be used for Scale.setMaximum(int).
     * 
     * @return the value.
     * @see org.eclipse.swt.widgets.Scale#setMaximum(int)
     */
    public int getMaximum() {
        return maxValue;
    }

    /**
     * Returns the value that will be used for Scale.setMinimum(int).
     * 
     * @return the value.
     * @see org.eclipse.swt.widgets.Scale#setMinimum(int)
     */
    public int getMinimum() {
        return minValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
     */
    public int getNumberOfControls() {
        return 2;
    }

    /**
     * Returns the value that will be used for Scale.setPageIncrement(int).
     * 
     * @return the value.
     * @see org.eclipse.swt.widgets.Scale#setPageIncrement(int)
     */
    public int getPageIncrement() {
        return pageIncrementValue;
    }

    /**
     * Returns this field editor's scale control.
     * 
     * @return the scale control, or <code>null</code> if no scale field is
     *         created yet
     */
    public Scale getScaleControl() {
        return scale;
    }

    /**
     * Returns this field editor's scale control. The control is created if it
     * does not yet exist.
     * 
     * @param parent
     *            the parent
     * @return the scale control
     */
    private Scale getScaleControl(Composite parent) {
        if (scale == null) {
            scale = new Scale(parent, SWT.HORIZONTAL);
            scale.setFont(parent.getFont());
            scale.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    valueChanged();
                }
            });
            scale.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent event) {
                    scale = null;
                }
            });
        } else {
            checkParent(scale, parent);
        }
        return scale;
    }

    /**
     * Set default values for the various scale fields.  These defaults are:<br>
     * <ul>
     * <li>Minimum  = 0
     * <li>Maximim = 10
     * <li>Increment = 1
     * <li>Page Increment = 1
     * </ul> 
     */
    private void setDefaultValues() {
        setValues(0, 10, 1, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditor#setFocus()
     */
    public void setFocus() {
        if (scale != null && !scale.isDisposed()) {
            scale.setFocus();
        }
    }

    /**
     * Set the value to be used for Scale.setIncrement(int) and update the
     * scale.
     * 
     * @param increment
     *            a value greater than 0.
     * @see org.eclipse.swt.widgets.Scale#setIncrement(int)
     */
    public void setIncrement(int increment) {
        this.incrementValue = increment;
        updateScale();
    }

    /**
     * Set the value to be used for Scale.setMaximum(int) and update the
     * scale.
     * 
     * @param max
     *            a value greater than 0.
     * @see org.eclipse.swt.widgets.Scale#setMaximum(int)
     */
    public void setMaximum(int max) {
        this.maxValue = max;
        updateScale();
    }

    /**
     * Set the value to be used for Scale.setMinumum(int) and update the
     * scale.
     * 
     * @param min
     *            a value greater than 0.
     * @see org.eclipse.swt.widgets.Scale#setMinimum(int)
     */
    public void setMinimum(int min) {
        this.minValue = min;
        updateScale();
    }

    /**
     * Set the value to be used for Scale.setPageIncrement(int) and update the
     * scale.
     * 
     * @param pageIncrement
     *            a value greater than 0.
     * @see org.eclipse.swt.widgets.Scale#setPageIncrement(int)
     */
    public void setPageIncrement(int pageIncrement) {
        this.pageIncrementValue = pageIncrement;
        updateScale();
    }

    /**
     * Set all Scale values.
     * 
     * @param min
     *            the value used for Scale.setMinimum(int).
     * @param max
     *            the value used for Scale.setMaximum(int).
     * @param increment
     *            the value used for Scale.setIncrement(int).
     * @param pageIncrement
     *            the value used for Scale.setPageIncrement(int).
     */
    private void setValues(int min, int max, int increment, int pageIncrement) {
        this.incrementValue = increment;
        this.maxValue = max;
        this.minValue = min;
        this.pageIncrementValue = pageIncrement;
        updateScale();
    }

    /**
     * Update the scale particulars with set values.
     */
    private void updateScale() {
        if (scale != null && !scale.isDisposed()) {
            scale.setMinimum(getMinimum());
            scale.setMaximum(getMaximum());
            scale.setIncrement(getIncrement());
            scale.setPageIncrement(getPageIncrement());
        }
    }

    /**
     * Informs this field editor's listener, if it has one, about a change to
     * the value (<code>VALUE</code> property) provided that the old and new
     * values are different.
     * <p>
     * This hook is <em>not</em> called when the scale is initialized (or
     * reset to the default value) from the preference store.
     * </p>
     */
    protected void valueChanged() {
        setPresentsDefaultValue(false);

        int newValue = scale.getSelection();
        if (newValue != oldValue) {
            fireStateChanged(IS_VALID, false, true);
            fireValueChanged(VALUE, new Integer(oldValue),
                    new Integer(newValue));
            oldValue = newValue;
        }
    }
}
