/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Abstract base implementation of a dialog page. All dialog pages are
 * subclasses of this one.
 */
public abstract class DialogPage implements IDialogPage, IMessageProvider {
    /**
     * The control for this dialog page.
     */
    private Control control;

    /**
     * Optional title; <code>null</code> if none.
     * 
     * @see #setTitle
     */
    private String title = null;

    /**
     * Optional description; <code>null</code> if none.
     * 
     * @see #setDescription
     */
    private String description = null;

    /**
     * Cached image; <code>null</code> if none.
     * 
     * @see #setImageDescriptor(ImageDescriptor)
     */
    private Image image = null;

    /**
     * Optional image; <code>null</code> if none.
     * 
     * @see #setImageDescriptor(ImageDescriptor)
     */
    private ImageDescriptor imageDescriptor = null;

    /**
     * The current message; <code>null</code> if none.
     */
    private String message = null;

    /**
     * The current message type; default value <code>NONE</code>.
     */
    private int messageType = NONE;

    /**
     * The current error message; <code>null</code> if none.
     */
    private String errorMessage = null;

    /**
     * Font metrics to use for determining pixel sizes.
     */
    private FontMetrics fontMetrics;

    /**
     * Creates a new empty dialog page.
     */
    protected DialogPage() {
        //No initial behaviour
    }

    /**
     * Creates a new dialog page with the given title.
     * 
     * @param title
     *            the title of this dialog page, or <code>null</code> if none
     */
    protected DialogPage(String title) {
        this.title = title;
    }

    /**
     * Creates a new dialog page with the given title and image.
     * 
     * @param title
     *            the title of this dialog page, or <code>null</code> if none
     * @param image
     *            the image for this dialog page, or <code>null</code> if none
     */
    protected DialogPage(String title, ImageDescriptor image) {
        this(title);
        imageDescriptor = image;
    }

    /**
     * Returns the number of pixels corresponding to the height of the given
     * number of characters.
     * <p>
     * This method may only be called after <code>initializeDialogUnits</code>
     * has been called.
     * </p>
     * <p>
     * Clients may call this framework method, but should not override it.
     * </p>
     * 
     * @param chars
     *            the number of characters
     * @return the number of pixels
     */
    protected int convertHeightInCharsToPixels(int chars) {
        // test for failure to initialize for backward compatibility
        if (fontMetrics == null) {
			return 0;
		}
        return Dialog.convertHeightInCharsToPixels(fontMetrics, chars);
    }

    /**
     * Returns the number of pixels corresponding to the given number of
     * horizontal dialog units.
     * <p>
     * This method may only be called after <code>initializeDialogUnits</code>
     * has been called.
     * </p>
     * <p>
     * Clients may call this framework method, but should not override it.
     * </p>
     * 
     * @param dlus
     *            the number of horizontal dialog units
     * @return the number of pixels
     */
    protected int convertHorizontalDLUsToPixels(int dlus) {
        // test for failure to initialize for backward compatibility
        if (fontMetrics == null) {
			return 0;
		}
        return Dialog.convertHorizontalDLUsToPixels(fontMetrics, dlus);
    }

    /**
     * Returns the number of pixels corresponding to the given number of
     * vertical dialog units.
     * <p>
     * This method may only be called after <code>initializeDialogUnits</code>
     * has been called.
     * </p>
     * <p>
     * Clients may call this framework method, but should not override it.
     * </p>
     * 
     * @param dlus
     *            the number of vertical dialog units
     * @return the number of pixels
     */
    protected int convertVerticalDLUsToPixels(int dlus) {
        // test for failure to initialize for backward compatibility
        if (fontMetrics == null) {
			return 0;
		}
        return Dialog.convertVerticalDLUsToPixels(fontMetrics, dlus);
    }

    /**
     * Returns the number of pixels corresponding to the width of the given
     * number of characters.
     * <p>
     * This method may only be called after <code>initializeDialogUnits</code>
     * has been called.
     * </p>
     * <p>
     * Clients may call this framework method, but should not override it.
     * </p>
     * 
     * @param chars
     *            the number of characters
     * @return the number of pixels
     */
    protected int convertWidthInCharsToPixels(int chars) {
        // test for failure to initialize for backward compatibility
        if (fontMetrics == null) {
			return 0;
		}
        return Dialog.convertWidthInCharsToPixels(fontMetrics, chars);
    }

    /**
     * The <code>DialogPage</code> implementation of this
     * <code>IDialogPage</code> method disposes of the page
     * image if it has one. 
     * Subclasses may extend.
     */
    public void dispose() {
        // deallocate SWT resources
        if (image != null) {
        	// RAP [bm]: Image#dispose
//            image.dispose();
            image = null;
        }
    }

    /**
     * Returns the top level control for this dialog page.
     * 
     * @return the top level control
     */
    public Control getControl() {
        return control;
    }

    /*
     * (non-Javadoc) Method declared on IDialogPage.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the symbolic font name used by dialog pages.
     * 
     * @return the symbolic font name
     */
    protected String getDialogFontName() {
        return JFaceResources.DIALOG_FONT;
    }

    /*
     * (non-Javadoc) Method declared on IDialogPage.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns the default font to use for this dialog page.
     * 
     * @return the font
     */
    protected Font getFont() {
        return JFaceResources.getFontRegistry().get(getDialogFontName());
    }

    /*
     * (non-Javadoc) Method declared on IDialogPage.
     */
    public Image getImage() {
        if (image == null) {
            if (imageDescriptor != null) {
                image = imageDescriptor.createImage();
            }
        }
        return image;
    }

    /*
     * (non-Javadoc) Method declared on IDialogPage.
     */
    public String getMessage() {
        return message;
    }

    /*
     * (non-Javadoc) Method declared on IMessageProvider.
     */
    public int getMessageType() {
        return messageType;
    }

    /**
     * Returns this dialog page's shell. Convenience method for
     * <code>getControl().getShell()</code>. This method may only be called
     * after the page's control has been created.
     * 
     * @return the shell
     */
    public Shell getShell() {
        return getControl().getShell();
    }

    /*
     * (non-Javadoc) Method declared on IDialogPage.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the tool tip text for the widget with the given id.
     * <p>
     * The default implementation of this framework method does nothing and
     * returns <code>null</code>. Subclasses may override.
     * </p>
     * 
     * @param widgetId
     *            the id of the widget for which hover help is requested
     * @return the tool tip text, or <code>null</code> if none
     * @deprecated 
     */
    protected final String getToolTipText(int widgetId) {
        // return nothing by default
        return null;
    }

    /**
     * Initializes the computation of horizontal and vertical dialog units based
     * on the size of current font.
     * <p>
     * This method must be called before any of the dialog unit based conversion
     * methods are called.
     * </p>
     * 
     * @param testControl
     *            a control from which to obtain the current font
     */
    protected void initializeDialogUnits(Control testControl) {
        // Compute and store a font metric
        GC gc = new GC(testControl);
        gc.setFont(JFaceResources.getDialogFont());
        fontMetrics = gc.getFontMetrics();
        gc.dispose();
    }

    /**
     * Sets the <code>GridData</code> on the specified button to be one that
     * is spaced for the current dialog page units. The method
     * <code>initializeDialogUnits</code> must be called once before calling
     * this method for the first time.
     * 
     * @param button
     *            the button to set the <code>GridData</code>
     * @return the <code>GridData</code> set on the specified button
     */
    protected GridData setButtonLayoutData(Button button) {
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        data.widthHint = Math.max(widthHint, minSize.x);
        button.setLayoutData(data);
        return data;
    }

    /**
     * Tests whether this page's UI content has already been created.
     * 
     * @return <code>true</code> if the control has been created, and
     *         <code>false</code> if not
     */
    protected boolean isControlCreated() {
        return control != null;
    }

    /**
     * This default implementation of an <code>IDialogPage</code> method does
     * nothing. Subclasses should override to take some action in response to a
     * help request.
     */
    public void performHelp() {
        //No default help
    }

    /**
     * Set the control for the receiver.
     * @param newControl
     */
    protected void setControl(Control newControl) {
        control = newControl;
    }

    /*
     * (non-Javadoc) Method declared on IDialogPage.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets or clears the error message for this page.
     * 
     * @param newMessage
     *            the message, or <code>null</code> to clear the error message
     */
    public void setErrorMessage(String newMessage) {
        errorMessage = newMessage;
    }

    /*
     * (non-Javadoc) Method declared on IDialogPage.
     */
    public void setImageDescriptor(ImageDescriptor desc) {
        imageDescriptor = desc;
        if (image != null) {
        	// RAP [bm]: Image#dispose
//            image.dispose();
            image = null;
        }
    }

    /**
     * Sets or clears the message for this page.
     * <p>
     * This is a shortcut for <code>setMessage(newMesasge, NONE)</code>
     * </p>
     * 
     * @param newMessage
     *            the message, or <code>null</code> to clear the message
     */
    public void setMessage(String newMessage) {
        setMessage(newMessage, NONE);
    }

    /**
     * Sets the message for this page with an indication of what type of message
     * it is.
     * <p>
     * The valid message types are one of <code>NONE</code>,
     * <code>INFORMATION</code>,<code>WARNING</code>, or
     * <code>ERROR</code>.
     * </p>
     * <p>
     * Note that for backward compatibility, a message of type
     * <code>ERROR</code> is different than an error message (set using
     * <code>setErrorMessage</code>). An error message overrides the current
     * message until the error message is cleared. This method replaces the
     * current message and does not affect the error message.
     * </p>
     * 
     * @param newMessage
     *            the message, or <code>null</code> to clear the message
     * @param newType
     *            the message type
     */
    public void setMessage(String newMessage, int newType) {
        message = newMessage;
        messageType = newType;
    }

    /**
     * The <code>DialogPage</code> implementation of this
     * <code>IDialogPage</code> method remembers the title in an internal
     * state variable. Subclasses may extend.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * The <code>DialogPage</code> implementation of this
     * <code>IDialogPage</code> method sets the control to the given
     * visibility state. Subclasses may extend.
     */
    public void setVisible(boolean visible) {
        control.setVisible(visible);
    }
}
