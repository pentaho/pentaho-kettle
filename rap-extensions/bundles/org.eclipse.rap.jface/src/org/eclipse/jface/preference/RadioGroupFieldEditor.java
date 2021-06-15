/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.preference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * A field editor for an enumeration type preference.
 * The choices are presented as a list of radio buttons.
 */
public class RadioGroupFieldEditor extends FieldEditor {

    /**
     * List of radio button entries of the form [label,value].
     */
    private String[][] labelsAndValues;

    /**
     * Number of columns into which to arrange the radio buttons.
     */
    private int numColumns;

    /**
     * Indent used for the first column of the radion button matrix.
     */
    private int indent = HORIZONTAL_GAP;

    /**
     * The current value, or <code>null</code> if none.
     */
    private String value;

    /**
     * The box of radio buttons, or <code>null</code> if none
     * (before creation and after disposal).
     */
    private Composite radioBox;

    /**
     * The radio buttons, or <code>null</code> if none
     * (before creation and after disposal).
     */
    private Button[] radioButtons;

    /**
     * Whether to use a Group control.
     */
    private boolean useGroup;

    /**
     * Creates a new radio group field editor 
     */
    protected RadioGroupFieldEditor() {
    }

    /**
     * Creates a radio group field editor.  
     * This constructor does not use a <code>Group</code> to contain the radio buttons.
     * It is equivalent to using the following constructor with <code>false</code>
     * for the <code>useGroup</code> argument.
     * <p>
     * Example usage:
     * <pre>
     *		RadioGroupFieldEditor editor= new RadioGroupFieldEditor(
     *			"GeneralPage.DoubleClick", resName, 1,
     *			new String[][] {
     *				{"Open Browser", "open"},
     *				{"Expand Tree", "expand"}
     *			},
     *          parent);	
     * </pre>
     * </p>
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param numColumns the number of columns for the radio button presentation
     * @param labelAndValues list of radio button [label, value] entries;
     *  the value is returned when the radio button is selected
     * @param parent the parent of the field editor's control
     */
    public RadioGroupFieldEditor(String name, String labelText, int numColumns,
            String[][] labelAndValues, Composite parent) {
        this(name, labelText, numColumns, labelAndValues, parent, false);
    }

    /**
     * Creates a radio group field editor.
     * <p>
     * Example usage:
     * <pre>
     *		RadioGroupFieldEditor editor= new RadioGroupFieldEditor(
     *			"GeneralPage.DoubleClick", resName, 1,
     *			new String[][] {
     *				{"Open Browser", "open"},
     *				{"Expand Tree", "expand"}
     *			},
     *          parent,
     *          true);	
     * </pre>
     * </p>
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param numColumns the number of columns for the radio button presentation
     * @param labelAndValues list of radio button [label, value] entries;
     *  the value is returned when the radio button is selected
     * @param parent the parent of the field editor's control
     * @param useGroup whether to use a Group control to contain the radio buttons
     */
    public RadioGroupFieldEditor(String name, String labelText, int numColumns,
            String[][] labelAndValues, Composite parent, boolean useGroup) {
        init(name, labelText);
        Assert.isTrue(checkArray(labelAndValues));
        this.labelsAndValues = labelAndValues;
        this.numColumns = numColumns;
        this.useGroup = useGroup;
        createControl(parent);
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void adjustForNumColumns(int numColumns) {
        Control control = getLabelControl();
        if (control != null) {
            ((GridData) control.getLayoutData()).horizontalSpan = numColumns;
        }
        ((GridData) radioBox.getLayoutData()).horizontalSpan = numColumns;
    }

    /**
     * Checks whether given <code>String[][]</code> is of "type" 
     * <code>String[][2]</code>.
     * @param table
     *
     * @return <code>true</code> if it is ok, and <code>false</code> otherwise
     */
    private boolean checkArray(String[][] table) {
        if (table == null) {
			return false;
		}
        for (int i = 0; i < table.length; i++) {
            String[] array = table[i];
            if (array == null || array.length != 2) {
				return false;
			}
        }
        return true;
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        if (useGroup) {
            Control control = getRadioBoxControl(parent);
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            control.setLayoutData(gd);
        } else {
            Control control = getLabelControl(parent);
            GridData gd = new GridData();
            gd.horizontalSpan = numColumns;
            control.setLayoutData(gd);
            control = getRadioBoxControl(parent);
            gd = new GridData();
            gd.horizontalSpan = numColumns;
            gd.horizontalIndent = indent;
            control.setLayoutData(gd);
        }

    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doLoad() {
        updateValue(getPreferenceStore().getString(getPreferenceName()));
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doLoadDefault() {
        updateValue(getPreferenceStore().getDefaultString(getPreferenceName()));
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doStore() {
        if (value == null) {
            getPreferenceStore().setToDefault(getPreferenceName());
            return;
        }

        getPreferenceStore().setValue(getPreferenceName(), value);
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    public int getNumberOfControls() {
        return 1;
    }

    /**
     * Returns this field editor's radio group control.
     * @param parent The parent to create the radioBox in
     * @return the radio group control
     */
    public Composite getRadioBoxControl(Composite parent) {
        if (radioBox == null) {

            Font font = parent.getFont();

            if (useGroup) {
                Group group = new Group(parent, SWT.NONE);
                group.setFont(font);
                String text = getLabelText();
                if (text != null) {
					group.setText(text);
				}
                radioBox = group;
                GridLayout layout = new GridLayout();
                layout.horizontalSpacing = HORIZONTAL_GAP;
                layout.numColumns = numColumns;
                radioBox.setLayout(layout);
            } else {
                radioBox = new Composite(parent, SWT.NONE);
                GridLayout layout = new GridLayout();
                layout.marginWidth = 0;
                layout.marginHeight = 0;
                layout.horizontalSpacing = HORIZONTAL_GAP;
                layout.numColumns = numColumns;
                radioBox.setLayout(layout);
                radioBox.setFont(font);
            }

            radioButtons = new Button[labelsAndValues.length];
            for (int i = 0; i < labelsAndValues.length; i++) {
                Button radio = new Button(radioBox, SWT.RADIO | SWT.LEFT);
                radioButtons[i] = radio;
                String[] labelAndValue = labelsAndValues[i];
                radio.setText(labelAndValue[0]);
                radio.setData(labelAndValue[1]);
                radio.setFont(font);
                radio.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent event) {
                        String oldValue = value;
                        value = (String) event.widget.getData();
                        setPresentsDefaultValue(false);
                        fireValueChanged(VALUE, oldValue, value);
                    }
                });
            }
            radioBox.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent event) {
                    radioBox = null;
                    radioButtons = null;
                }
            });
        } else {
            checkParent(radioBox, parent);
        }
        return radioBox;
    }

    /**
     * Sets the indent used for the first column of the radion button matrix.
     *
     * @param indent the indent (in pixels)
     */
    public void setIndent(int indent) {
        if (indent < 0) {
			this.indent = 0;
		} else {
			this.indent = indent;
		}
    }

    /**
     * Select the radio button that conforms to the given value.
     *
     * @param selectedValue the selected value
     */
    private void updateValue(String selectedValue) {
        this.value = selectedValue;
        if (radioButtons == null) {
			return;
		}

        if (this.value != null) {
            boolean found = false;
            for (int i = 0; i < radioButtons.length; i++) {
                Button radio = radioButtons[i];
                boolean selection = false;
                if (((String) radio.getData()).equals(this.value)) {
                    selection = true;
                    found = true;
                }
                radio.setSelection(selection);
            }
            if (found) {
				return;
			}
        }

        // We weren't able to find the value. So we select the first
        // radio button as a default.
        if (radioButtons.length > 0) {
            radioButtons[0].setSelection(true);
            this.value = (String) radioButtons[0].getData();
        }
        return;
    }

    /*
     * @see FieldEditor.setEnabled(boolean,Composite).
     */
    public void setEnabled(boolean enabled, Composite parent) {
        if (!useGroup) {
			super.setEnabled(enabled, parent);
		}
        for (int i = 0; i < radioButtons.length; i++) {
            radioButtons[i].setEnabled(enabled);
        }

    }
}
