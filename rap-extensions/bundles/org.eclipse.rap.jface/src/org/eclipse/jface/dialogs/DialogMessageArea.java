/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The DialogMessageArea is a resusable component for adding an accessible
 * message area to a dialog.
 * 
 * When the message is normal a CLabel is used but an errors replaces the
 * message area with a non editable text that can take focus for use by screen
 * readers.
 * 
 * @since 1.0
 */
public class DialogMessageArea extends Object {
    private Text messageText;

    private Label messageImageLabel;

    private Composite messageComposite;

    private String lastMessageText;

    private int lastMessageType;

    private CLabel titleLabel;

    /**
     * Create a new instance of the receiver.
     */
    public DialogMessageArea() {
        //No initial behaviour
    }

    /**
     * Create the contents for the receiver.
     * 
     * @param parent
     *            the Composite that the children will be created in
     */
    public void createContents(Composite parent) {
       
        // Message label
        titleLabel = new CLabel(parent, SWT.NONE);
        titleLabel.setFont(JFaceResources.getBannerFont());
        messageComposite = new Composite(parent, SWT.NONE);
        GridLayout messageLayout = new GridLayout();
        messageLayout.numColumns = 2;
        messageLayout.marginWidth = 0;
        messageLayout.marginHeight = 0;
        messageLayout.makeColumnsEqualWidth = false;
        messageComposite.setLayout(messageLayout);
        messageImageLabel = new Label(messageComposite, SWT.NONE);
        messageImageLabel.setImage(JFaceResources
                .getImage(Dialog.DLG_IMG_MESSAGE_INFO));
        messageImageLabel.setLayoutData(new GridData(
                GridData.VERTICAL_ALIGN_CENTER));
  
        messageText = new Text(messageComposite, SWT.NONE);
        messageText.setEditable(false);
  
        GridData textData = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
        messageText.setLayoutData(textData);
  
    }

    /**
     * Set the layoutData for the title area. In most cases this will be a copy
     * of the layoutData used in setMessageLayoutData.
     * 
     * @param layoutData
     *            the layoutData for the title
     * @see #setMessageLayoutData(Object)
     */
    public void setTitleLayoutData(Object layoutData) {
        titleLabel.setLayoutData(layoutData);
    }

    /**
     * Set the layoutData for the messageArea. In most cases this will be a copy
     * of the layoutData used in setTitleLayoutData.
     * 
     * @param layoutData
     *            the layoutData for the message area composite.
     * @see #setTitleLayoutData(Object)
     */
    public void setMessageLayoutData(Object layoutData) {
        messageComposite.setLayoutData(layoutData);
    }

    /**
     * Show the title.
     * 
     * @param titleMessage
     *            String for the titke
     * @param titleImage
     *            Image or <code>null</code>
     */
    public void showTitle(String titleMessage, Image titleImage) {
        titleLabel.setImage(titleImage);
        titleLabel.setText(titleMessage);
        restoreTitle();
        return;
    }

    /**
     * Enable the title and disable the message text and image.
     */
    public void restoreTitle() {
        titleLabel.setVisible(true);
        messageComposite.setVisible(false);
        lastMessageText = null;
        lastMessageType = IMessageProvider.NONE;
    }

    /**
     * Show the new message in the message text and update the image. Base the
     * background color on whether or not there are errors.
     * 
     * @param newMessage
     *            The new value for the message
     * @param newType
     *            One of the IMessageProvider constants. If newType is
     *            IMessageProvider.NONE show the title.
     * @see IMessageProvider
     */
    public void updateText(String newMessage, int newType) {
        Image newImage = null;
        switch (newType) {
        case IMessageProvider.NONE:
            if (newMessage == null) {
				restoreTitle();
			} else {
				showTitle(newMessage, null);
			}
            return;
        case IMessageProvider.INFORMATION:
            newImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
            break;
        case IMessageProvider.WARNING:
            newImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
            break;
        case IMessageProvider.ERROR:
            newImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);

            break;
        }
        messageComposite.setVisible(true);
        titleLabel.setVisible(false);
        // Any more updates required?
        // If the message text equals the tooltip (i.e. non-shortened text is the same)
        // and shortened text is the same (i.e. not a resize)
        // and the image is the same then nothing to do
        String shortText = Dialog.shortenText(newMessage,messageText);
        if (newMessage.equals(messageText.getToolTipText())
                && newImage == messageImageLabel.getImage()
                	&& shortText.equals(messageText.getText())) {
			return;
		}
        messageImageLabel.setImage(newImage);
        messageText.setText(Dialog.shortenText(newMessage,messageText));
        messageText.setToolTipText(newMessage);
        lastMessageText = newMessage;
 
    }


    /**
     * Clear the error message. Restore the previously displayed message if
     * there is one, if not restore the title label.
     *  
     */
    public void clearErrorMessage() {
        if (lastMessageText == null) {
			restoreTitle();
		} else {
			updateText(lastMessageText, lastMessageType);
		}
    }
}
