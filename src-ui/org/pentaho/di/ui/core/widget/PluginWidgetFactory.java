/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.util.Assert;
import org.pentaho.di.core.variables.VariableSpace;

/**
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 * 
 */
public class PluginWidgetFactory {

    /**
     * The margin.
     */
    public static final int DEFAULT_MARGIN = 4;

    /**
     * The default middle.
     */
    public static final int DEFAULT_MIDDLE = 35;

    public static final int DEFAULT_RIGHT_OFFSET = 100;

    private final Shell shell;

    private final VariableSpace varSpace;

    private int margin = DEFAULT_MARGIN;

    private int middle = DEFAULT_MIDDLE;

    private int rightOffset = DEFAULT_RIGHT_OFFSET;

    /**
     * Constructor.
     * 
     * @param shell
     *            the shell to set.
     * @param varSpace
     *            the variableSpace to be used. e.g. for TextVar
     * @throws IllegalArgumentException
     *             if shell is null.
     */
    public PluginWidgetFactory(final Shell shell, final VariableSpace varSpace) throws IllegalArgumentException {
        Assert.assertNotNull(shell, "Shell cannot be null");
        Assert.assertNotNull(varSpace, "transMeta cannot be null");

        this.shell = shell;
        this.varSpace = varSpace;
    }

    /**
     * @return the margin
     */
    public int getMargin() {
        return this.margin;
    }

    /**
     * @param margin
     *            the margin to set
     */
    public void setMargin(final int margin) {
        this.margin = margin;
    }

    /**
     * @return the middle
     */
    public int getMiddle() {
        return this.middle;
    }

    /**
     * @param middle
     *            the middle to set
     */
    public void setMiddle(final int middle) {
        this.middle = middle;
    }

    /**
     * Create label.
     * 
     * @param text
     *            text to set.
     * @return new label.
     */
    public Label createRightLabel(final String text) {
        return this.createLabel(SWT.RIGHT, text);
    }

    /**
     * Create label.
     * 
     * @param style
     *            style to use.
     * @param text
     *            text to set.
     * @return new label.
     */
    public Label createLabel(final int style, final String text) {
        final Label label = new Label(this.shell, style);
        label.setText(text);
        return label;
    }

    /**
     * Convenience method to create FormData for labels.
     * 
     * @param topControl
     *            the control which is above the current label, or null if none above.
     * @return layoutData.
     */
    public FormData createLabelLayoutData(final Control topControl) {
        FormData formData = new FormData();

        formData.left = new FormAttachment(0, 0);
        formData.right = new FormAttachment(this.middle, -this.margin);
        if (topControl != null) {
            formData.top = new FormAttachment(topControl, this.margin);
        } else {
            formData.top = new FormAttachment(0, this.margin);
        }
        return formData;
    }

    /**
     * Convenience method to create FormData for Controls beside a label.
     * 
     * @param topControl
     *            the control which is above the current label, or null if none above.
     * @return layoutData.
     */
    public FormData createControlLayoutData(final Control topControl) {
        FormData formData = new FormData();

        formData.left = new FormAttachment(this.middle, 0);
        if (topControl != null) {
            formData.top = new FormAttachment(topControl, this.margin);
        } else {
            formData.top = new FormAttachment(0, this.margin);
        }
        formData.right = new FormAttachment(this.rightOffset, 0);

        return formData;
    }

    /**
     * @param text
     *            text to set.
     * @return text widget.
     */
    public Text createSingleTextLeft(final String text) {
        return this.createText(SWT.SINGLE | SWT.LEFT | SWT.BORDER, text);
    }

    /**
     * @param style
     *            style to use.
     * @param text
     *            text to set.
     * @return text widget.
     */
    public Text createText(final int style, final String text) {
        final Text textWidget = new Text(this.shell, style);
        textWidget.setText(text);
        return textWidget;
    }

    /**
     * @return new ...
     */
    public TextVar createSingleTextVarLeft() {
        return new TextVar(this.varSpace, this.shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    }

    /**
     * @param style
     *            style to use.
     * @param text
     *            text to set.
     * @return new button.
     */
    public Button createButton(final int style, final String text) {
        final Button button = new Button(this.shell, style);
        button.setText(text);
        return button;
    }

    /**
     * @param text
     *            text to set.
     * @return new button.
     */
    public Button createPushButton(final String text) {
        return this.createButton(SWT.PUSH, text);
    }

}
