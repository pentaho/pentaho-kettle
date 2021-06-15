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
package org.eclipse.jface.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * An abstract base class for dialogs with a status bar and OK/CANCEL buttons.
 * The status message is specified in an IStatus which can be of severity ERROR,
 * WARNING, INFO or OK. The OK button is enabled or disabled depending on the
 * status.
 * 
 * @since 1.0
 */
public abstract class StatusDialog extends TrayDialog {

	private Button fOkButton;

	private MessageLine fStatusLine;

	private IStatus fLastStatus;

	private String fTitle;

	private Image fImage;

	private boolean fStatusLineAboveButtons = true;

	/**
	 * A message line displaying a status.
	 */
	private class MessageLine extends CLabel {

		private Color fNormalMsgAreaBackground;

		/**
		 * Creates a new message line as a child of the given parent.
		 * 
		 * @param parent
		 */
		public MessageLine(Composite parent) {
			this(parent, SWT.LEFT);
		}

		/**
		 * Creates a new message line as a child of the parent and with the
		 * given SWT stylebits.
		 * 
		 * @param parent
		 * @param style
		 */
		public MessageLine(Composite parent, int style) {
			super(parent, style);
			fNormalMsgAreaBackground = getBackground();
		}

		/**
		 * Find an image assocated with the status.
		 * 
		 * @param status
		 * @return Image
		 */
		private Image findImage(IStatus status) {
			if (status.isOK()) {
				return null;
			} else if (status.matches(IStatus.ERROR)) {
				return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
			} else if (status.matches(IStatus.WARNING)) {
				return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
			} else if (status.matches(IStatus.INFO)) {
				return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
			}
			return null;
		}

		/**
		 * Sets the message and image to the given status.
		 * 
		 * @param status
		 *            IStatus or <code>null</code>. <code>null</code> will
		 *            set the empty text and no image.
		 */
		public void setErrorStatus(IStatus status) {
			if (status != null && !status.isOK()) {
				String message = status.getMessage();
				if (message != null && message.length() > 0) {
					setText(message);
					// unqualified call of setImage is too ambiguous for
					// Foundation 1.0 compiler
					// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=140576
					MessageLine.this.setImage(findImage(status));
					setBackground(JFaceColors.getErrorBackground(getDisplay()));
					return;
				}
			}
			setText(""); //$NON-NLS-1$	
			// unqualified call of setImage is too ambiguous for Foundation 1.0
			// compiler
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=140576
			MessageLine.this.setImage(null);
			setBackground(fNormalMsgAreaBackground);
		}
	}

	/**
	 * Creates an instance of a status dialog.
	 * 
	 * @param parent
	 *            the parent Shell of the dialog
	 */
	public StatusDialog(Shell parent) {
		super(parent);
		fLastStatus = new Status(IStatus.OK, Policy.JFACE, IStatus.OK,
				Util.ZERO_LENGTH_STRING, null);
	}

	/**
	 * Specifies whether status line appears to the left of the buttons
	 * (default) or above them.
	 * 
	 * @param aboveButtons
	 *            if <code>true</code> status line is placed above buttons; if
	 *            <code>false</code> to the right
	 */
	public void setStatusLineAboveButtons(boolean aboveButtons) {
		fStatusLineAboveButtons = aboveButtons;
	}

	/**
	 * Update the dialog's status line to reflect the given status. It is safe
	 * to call this method before the dialog has been opened.
	 * 
	 * @param status
	 *            the status to set
	 */
	protected void updateStatus(IStatus status) {
		fLastStatus = status;
		if (fStatusLine != null && !fStatusLine.isDisposed()) {
			updateButtonsEnableState(status);
			fStatusLine.setErrorStatus(status);
		}
	}

	/**
	 * Returns the last status.
	 * 
	 * @return IStatus
	 */
	public IStatus getStatus() {
		return fLastStatus;
	}

	/**
	 * Updates the status of the ok button to reflect the given status.
	 * Subclasses may override this method to update additional buttons.
	 * 
	 * @param status
	 *            the status.
	 */
	protected void updateButtonsEnableState(IStatus status) {
		if (fOkButton != null && !fOkButton.isDisposed()) {
			fOkButton.setEnabled(!status.matches(IStatus.ERROR));
		}
	}

	/*
	 * @see Window#create(Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (fTitle != null) {
			shell.setText(fTitle);
		}
	}

	/*
	 * @see Window#create()
	 */
	public void create() {
		super.create();
		if (fLastStatus != null) {
			// policy: dialogs are not allowed to come up with an error message
			if (fLastStatus.matches(IStatus.ERROR)) {
				// remove the message
				fLastStatus = new Status(IStatus.ERROR,
						fLastStatus.getPlugin(), fLastStatus.getCode(),
						"", fLastStatus.getException()); //$NON-NLS-1$
			}
			updateStatus(fLastStatus);
		}
	}

	/*
	 * @see Dialog#createButtonsForButtonBar(Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		fOkButton = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.get().OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.get().CANCEL_LABEL, false);
	}

	/*
	 * @see Dialog#createButtonBar(Composite)
	 */
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();

		if (fStatusLineAboveButtons) {
			layout.numColumns = 1;
		} else {
			layout.numColumns = 2;
		}

		layout.marginHeight = 0;
		layout.marginLeft = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (!fStatusLineAboveButtons && isHelpAvailable()) {
			createHelpControl(composite);
		}

		fStatusLine = new MessageLine(composite);
		fStatusLine.setAlignment(SWT.LEFT);
		GridData statusData = new GridData(GridData.FILL_HORIZONTAL);
		fStatusLine.setErrorStatus(null);
		if (fStatusLineAboveButtons && isHelpAvailable()) {
			statusData.horizontalSpan = 2;
			createHelpControl(composite);
		}

		fStatusLine.setLayoutData(statusData);
		applyDialogFont(composite);

		/*
		 * Create the rest of the button bar, but tell it not to create a help
		 * button (we've already created it).
		 */
		boolean helpAvailable = isHelpAvailable();
		setHelpAvailable(false);
		super.createButtonBar(composite);
		setHelpAvailable(helpAvailable);
		return composite;
	}

	/**
	 * Sets the title for this dialog.
	 * 
	 * @param title
	 *            the title.
	 */
	public void setTitle(String title) {
		fTitle = title != null ? title : ""; //$NON-NLS-1$
		Shell shell = getShell();
		if ((shell != null) && !shell.isDisposed()) {
			shell.setText(fTitle);
		}
	}

	/**
	 * Sets the image for this dialog.
	 * 
	 * @param image
	 *            the image.
	 */
	public void setImage(Image image) {
		fImage = image;
		Shell shell = getShell();
		if ((shell != null) && !shell.isDisposed()) {
			shell.setImage(fImage);
		}
	}

}
