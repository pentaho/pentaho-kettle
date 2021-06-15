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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A field editor for a font type preference.
 */
public class FontFieldEditor extends FieldEditor {

    /**
     * The change font button, or <code>null</code> if none
     * (before creation and after disposal).
     */
    private Button changeFontButton = null;

    /**
     * The text for the change font button, or <code>null</code>
     * if missing.
     */
    private String changeButtonText;

    /**
     * The text for the preview, or <code>null</code> if no preview is desired
     */
    private String previewText;

    /**
     * Font data for the chosen font button, or <code>null</code> if none.
     */
    private FontData[] chosenFont;

    /**
     * The label that displays the selected font, or <code>null</code> if none.
     */
    private Label valueControl;

    /**
     * The previewer, or <code>null</code> if none.
     */
    private DefaultPreviewer previewer;

    /**
     * Internal font previewer implementation.
     */
    private static class DefaultPreviewer {
        private Text text;

        private String string;

        private Font font;

        /**
         * Constructor for the previewer.
         * @param s
         * @param parent
         */
        public DefaultPreviewer(String s, Composite parent) {
            string = s;
            text = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
            text.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    if (font != null) {
						font.dispose();
					}
                }
            });
            if (string != null) {
				text.setText(string);
			}
        }

        /**
         * @return the control the previewer is using
         */
        public Control getControl() {
            return text;
        }

        /**
         * Set the font to display with
         * @param fontData
         */
        public void setFont(FontData[] fontData) {
            if (font != null) {
				font.dispose();
			}
            font = new Font(text.getDisplay(), fontData);
            text.setFont(font);
        }

        /**
         * @return the preferred size of the previewer.
         */
        public int getPreferredExtent() {
            return 40;
        }
    }

    /**
     * Creates a new font field editor 
     */
    protected FontFieldEditor() {
    }

    /**
     * Creates a font field editor with an optional preview area.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param previewAreaText the text used for the preview window. If it is
     * <code>null</code> there will be no preview area,
     * @param parent the parent of the field editor's control
     */
    public FontFieldEditor(String name, String labelText,
            String previewAreaText, Composite parent) {
        init(name, labelText);
        previewText = previewAreaText;
        changeButtonText = JFaceResources.getString("openChange"); //$NON-NLS-1$
        createControl(parent);

    }

    /**
     * Creates a font field editor without a preview.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
    public FontFieldEditor(String name, String labelText, Composite parent) {
        this(name, labelText, null, parent);

    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void adjustForNumColumns(int numColumns) {

        GridData data = new GridData();
        if (valueControl.getLayoutData() != null) {
			data = (GridData) valueControl.getLayoutData();
		}

        data.horizontalSpan = numColumns - getNumberOfControls() + 1;
        valueControl.setLayoutData(data);
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void applyFont() {
        if (chosenFont != null && previewer != null) {
			previewer.setFont(chosenFont);
		}
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        getLabelControl(parent);

        valueControl = getValueControl(parent);

        GridData gd = new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL);
        gd.horizontalSpan = numColumns - getNumberOfControls() + 1;
        valueControl.setLayoutData(gd);
        if (previewText != null) {
            previewer = new DefaultPreviewer(previewText, parent);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.heightHint = previewer.getPreferredExtent();
            gd.widthHint = previewer.getPreferredExtent();
            previewer.getControl().setLayoutData(gd);
        }

        changeFontButton = getChangeControl(parent);
        gd = new GridData();
        int widthHint = convertHorizontalDLUsToPixels(changeFontButton,
                IDialogConstants.BUTTON_WIDTH);
        gd.widthHint = Math.max(widthHint, changeFontButton.computeSize(
                SWT.DEFAULT, SWT.DEFAULT, true).x);
        changeFontButton.setLayoutData(gd);

    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doLoad() {
        if (changeFontButton == null) {
			return;
		}
        updateFont(PreferenceConverter.getFontDataArray(getPreferenceStore(),
                getPreferenceName()));
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doLoadDefault() {
        if (changeFontButton == null) {
			return;
		}
        updateFont(PreferenceConverter.getDefaultFontDataArray(
                getPreferenceStore(), getPreferenceName()));
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doStore() {
        if (chosenFont != null) {
			PreferenceConverter.setValue(getPreferenceStore(),
                    getPreferenceName(), chosenFont);
		}
    }

    /**
     * Returns the change button for this field editor.
     *
     * @param parent The Composite to create the button in if required.
     * @return the change button
     */
    protected Button getChangeControl(Composite parent) {
        if (changeFontButton == null) {
            changeFontButton = new Button(parent, SWT.PUSH);
            if (changeButtonText != null) {
				changeFontButton.setText(changeButtonText);
			}
            changeFontButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    FontDialog fontDialog = new FontDialog(changeFontButton
                            .getShell());
                    if (chosenFont != null) {
						fontDialog.setFontList(chosenFont);
					}
                    FontData font = fontDialog.open();
                    if (font != null) {
                        FontData[] oldFont = chosenFont;
                        if (oldFont == null) {
							oldFont = JFaceResources.getDefaultFont()
                                    .getFontData();
						}
                        setPresentsDefaultValue(false);
                        FontData[] newData = new FontData[1];
                        newData[0] = font;
                        updateFont(newData);
                        fireValueChanged(VALUE, oldFont[0], font);
                    }

                }
            });
            changeFontButton.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent event) {
                    changeFontButton = null;
                }
            });
            changeFontButton.setFont(parent.getFont());
            setButtonLayoutData(changeFontButton);
        } else {
            checkParent(changeFontButton, parent);
        }
        return changeFontButton;
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    public int getNumberOfControls() {
        if (previewer == null) {
			return 3;
		}
        
        return 4;
    }

    /**
     * Returns the preferred preview height. 
     *
     * @return the height, or <code>-1</code> if no previewer
     *  is installed
     */
    public int getPreferredPreviewHeight() {
        if (previewer == null) {
			return -1;
		}
        return previewer.getPreferredExtent();
    }

    /**
     * Returns the preview control for this field editor.
     *
     * @return the preview control
     */
    public Control getPreviewControl() {
        if (previewer == null) {
			return null;
		}

        return previewer.getControl();
    }

    /**
     * Returns the value control for this field editor. The value control
     * displays the currently selected font name.
     * @param parent The Composite to create the viewer in if required
     * @return the value control
     */
    protected Label getValueControl(Composite parent) {
        if (valueControl == null) {
            valueControl = new Label(parent, SWT.LEFT);
            valueControl.setFont(parent.getFont());
            valueControl.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent event) {
                    valueControl = null;
                }
            });
        } else {
            checkParent(valueControl, parent);
        }
        return valueControl;
    }

    /**
     * Sets the text of the change button.
     *
     * @param text the new text
     */
    public void setChangeButtonText(String text) {
        Assert.isNotNull(text);
        changeButtonText = text;
        if (changeFontButton != null) {
			changeFontButton.setText(text);
		}
    }

    /**
     * Updates the change font button and the previewer to reflect the
     * newly selected font.
     * @param font The FontData[] to update with.
     */
    private void updateFont(FontData font[]) {
        FontData[] bestFont = JFaceResources.getFontRegistry().filterData(
                font, valueControl.getDisplay());

        //if we have nothing valid do as best we can
        if (bestFont == null) {
			bestFont = getDefaultFontData();
		}

        //Now cache this value in the receiver
        this.chosenFont = bestFont;

        if (valueControl != null) {
            valueControl.setText(StringConverter.asString(chosenFont[0]));
        }
        if (previewer != null) {
            previewer.setFont(bestFont);
        }
    }

    /**
     * Store the default preference for the field
     * being edited
     */
    protected void setToDefault() {
        FontData[] defaultFontData = PreferenceConverter
                .getDefaultFontDataArray(getPreferenceStore(),
                        getPreferenceName());
        PreferenceConverter.setValue(getPreferenceStore(), getPreferenceName(),
                defaultFontData);
    }

    /**
     * Get the system default font data.
     * @return FontData[]
     */
    private FontData[] getDefaultFontData() {
        return valueControl.getDisplay().getSystemFont().getFontData();
    }

    /*
     * @see FieldEditor.setEnabled(boolean,Composite).
     */
    public void setEnabled(boolean enabled, Composite parent) {
        super.setEnabled(enabled, parent);
        getChangeControl(parent).setEnabled(enabled);
        getValueControl(parent).setEnabled(enabled);
    }

}
