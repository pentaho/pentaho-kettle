/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
//import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A StatusLine control is a SWT Composite with a horizontal layout which hosts
 * a number of status indication controls. Typically it is situated below the
 * content area of the window.
 * <p>
 * By default a StatusLine has two predefined status controls: a MessageLine and
 * a ProgressIndicator and it provides API for easy access.
 * </p>
 * <p>
 * This is an internal class, not intended to be used outside the JFace
 * framework.
 * </p>
 */
/* package */class StatusLine extends Composite implements IProgressMonitor {

	/** Horizontal gaps between items. */
	public static final int GAP = 3;

	/** Progress bar creation is delayed by this ms */
	public static final int DELAY_PROGRESS = 500;

	/** visibility state of the progressbar */
	protected boolean fProgressIsVisible = false;

	/** visibility state of the cancle button */
	protected boolean fCancelButtonIsVisible = false;

	/** enablement state of the cancle button */
	protected boolean fCancelEnabled = false;

	/** name of the task */
	protected String fTaskName;

	/** is the task is cancled */
	protected volatile boolean fIsCanceled;

	/** the start time of the task */
	protected long fStartTime;

	private Cursor fStopButtonCursor;

	/** the message text */
	protected String fMessageText;

	/** the message image */
	protected Image fMessageImage;

	/** the error text */
	protected String fErrorText;

	/** the error image */
	protected Image fErrorImage;

	/** the message label */
	protected CLabel fMessageLabel;

	/** the composite parent of the progress bar */
	protected Composite fProgressBarComposite;

	/** the progress bar */
	protected ProgressIndicator fProgressBar;

	/** the toolbar */
	protected ToolBar fToolBar;

	/** the cancle button */
	protected ToolItem fCancelButton;

	/** stop image descriptor */
	protected static ImageDescriptor fgStopImage = ImageDescriptor
			.createFromFile(StatusLine.class, "images/stop.gif");//$NON-NLS-1$
	// RAP [if] Clipboard
//	private MenuItem copyMenuItem;
	static {
		JFaceResources.getImageRegistry().put(
				"org.eclipse.jface.parts.StatusLine.stopImage", fgStopImage);//$NON-NLS-1$
	}

	/**
	 * Layout the contribution item controls on the status line.
	 */
	public class StatusLineLayout extends Layout {
		private final StatusLineLayoutData DEFAULT_DATA = new StatusLineLayoutData();

		public Point computeSize(Composite composite, int wHint, int hHint,
				boolean changed) {

			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
				return new Point(wHint, hHint);
			}

			Control[] children = composite.getChildren();
			int totalWidth = 0;
			int maxHeight = 0;
			int totalCnt = 0;
			for (int i = 0; i < children.length; i++) {
				boolean useWidth = true;
				Control w = children[i];
				if (w == fProgressBarComposite && !fProgressIsVisible) {
					useWidth = false;
				} else if (w == fToolBar && !fCancelButtonIsVisible) {
					useWidth = false;
				}
				StatusLineLayoutData data = (StatusLineLayoutData) w
						.getLayoutData();
				if (data == null) {
					data = DEFAULT_DATA;
				}
				Point e = w.computeSize(data.widthHint, data.heightHint,
						changed);
				if (useWidth) {
					totalWidth += e.x;
					totalCnt++;
				}
				maxHeight = Math.max(maxHeight, e.y);
			}
			if (totalCnt > 0) {
				totalWidth += (totalCnt - 1) * GAP;
			}
			if (totalWidth <= 0) {
				totalWidth = maxHeight * 4;
			}
			return new Point(totalWidth, maxHeight);
		}

		public void layout(Composite composite, boolean flushCache) {

			if (composite == null) {
				return;
			}

			// StatusLineManager skips over the standard status line widgets
			// in its update method. There is thus a dependency
			// between the layout of the standard widgets and the update method.

			// Make sure cancel button and progress bar are before
			// contributions.
			fMessageLabel.moveAbove(null);
			fToolBar.moveBelow(fMessageLabel);
			fProgressBarComposite.moveBelow(fToolBar);

			Rectangle rect = composite.getClientArea();
			Control[] children = composite.getChildren();
			int count = children.length;

			int ws[] = new int[count];

			int h = rect.height;
			int totalWidth = -GAP;
			for (int i = 0; i < count; i++) {
				Control w = children[i];
				if (w == fProgressBarComposite && !fProgressIsVisible) {
					continue;
				}
				if (w == fToolBar && !fCancelButtonIsVisible) {
					continue;
				}
				StatusLineLayoutData data = (StatusLineLayoutData) w
						.getLayoutData();
				if (data == null) {
					data = DEFAULT_DATA;
				}
				int width = w.computeSize(data.widthHint, h, flushCache).x;
				ws[i] = width;
				totalWidth += width + GAP;
			}

			int diff = rect.width - totalWidth;
			ws[0] += diff; // make the first StatusLabel wider

			// Check against minimum recommended width
			final int msgMinWidth = rect.width / 3;
			if (ws[0] < msgMinWidth) {
				diff = ws[0] - msgMinWidth;
				ws[0] = msgMinWidth;
			} else {
				diff = 0;
			}

			// Take space away from the contributions first.
			for (int i = count - 1; i >= 0 && diff < 0; --i) {
				int min = Math.min(ws[i], -diff);
				ws[i] -= min;
				diff += min + GAP;
			}

			int x = rect.x;
			int y = rect.y;
			for (int i = 0; i < count; i++) {
				Control w = children[i];
				/*
				 * Workaround for Linux Motif: Even if the progress bar and
				 * cancel button are not set to be visible ad of width 0, they
				 * still draw over the first pixel of the editor contributions.
				 * 
				 * The fix here is to draw the progress bar and cancel button
				 * off screen if they are not visible.
				 */
				if (w == fProgressBarComposite && !fProgressIsVisible
						|| w == fToolBar && !fCancelButtonIsVisible) {
					w.setBounds(x + rect.width, y, ws[i], h);
					continue;
				}
				w.setBounds(x, y, ws[i], h);
				if (ws[i] > 0) {
					x += ws[i] + GAP;
				}
			}
		}
	}

	/**
	 * Create a new StatusLine as a child of the given parent.
	 * 
	 * @param parent
	 *            the parent for this Composite
	 * @param style
	 *            the style used to create this widget
	 */
	public StatusLine(Composite parent, int style) {
		super(parent, style);
		
// RAP [if]	Accessibility not available
//		getAccessible().addAccessibleControlListener(new AccessibleControlAdapter() {
//			public void getRole(AccessibleControlEvent e) {
//				e.detail = ACC.ROLE_STATUSBAR;
//			}
//		});

		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				handleDispose();
			}
		});

		// StatusLineManager skips over the standard status line widgets
		// in its update method. There is thus a dependency
		// between this code defining the creation and layout of the standard
		// widgets and the update method.

		setLayout(new StatusLineLayout());

		fMessageLabel = new CLabel(this, SWT.NONE);// SWT.SHADOW_IN);

		// RAP [if] Clipboard
//		// this would need extra work to make this accessible
//		// from the workbench command framework.
//		Menu menu = new Menu(fMessageLabel);
//		fMessageLabel.setMenu(menu);
//		copyMenuItem = new MenuItem(menu, SWT.PUSH);
//		copyMenuItem.setText(JFaceResources.getString("copy")); //$NON-NLS-1$
//		copyMenuItem.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				String text = fMessageLabel.getText();
//				if (text != null && text.length() > 0) {
//					text = LegacyActionTools.removeMnemonics(text);
//					Clipboard cp = new Clipboard(e.display);
//					cp.setContents(new Object[] { text },
//							new Transfer[] { TextTransfer.getInstance() });
//					cp.dispose();
//				}
//			}
//		});
		
		fProgressIsVisible = false;
		fCancelEnabled = false;

		fToolBar = new ToolBar(this, SWT.FLAT);
		fCancelButton = new ToolItem(fToolBar, SWT.PUSH);
		fCancelButton.setImage(fgStopImage.createImage());
		fCancelButton.setToolTipText(JFaceResources
				.getString("Cancel_Current_Operation")); //$NON-NLS-1$
		fCancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setCanceled(true);
			}
		});
    	// RAP [bm]: Image#dispose
//		fCancelButton.addDisposeListener(new DisposeListener() {
//			public void widgetDisposed(DisposeEvent e) {
//				Image i = fCancelButton.getImage();
//				if ((i != null) && (!i.isDisposed())) {
//					i.dispose();
//				}
//			}
//		});
		// RAPEND: [bm]

		// We create a composite to create the progress bar in
		// so that it can be centered. See bug #32331
		fProgressBarComposite = new Composite(this, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		fProgressBarComposite.setLayout(layout);
		fProgressBar = new ProgressIndicator(fProgressBarComposite);
		fProgressBar.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL));

		fStopButtonCursor = new Cursor(getDisplay(), SWT.CURSOR_ARROW);
	}

	/**
	 * Notifies that the main task is beginning.
	 * 
	 * @param name
	 *            the name (or description) of the main task
	 * @param totalWork
	 *            the total number of work units into which the main task is
	 *            been subdivided. If the value is 0 or UNKNOWN the
	 *            implemenation is free to indicate progress in a way which
	 *            doesn't require the total number of work units in advance. In
	 *            general users should use the UNKNOWN value if they don't know
	 *            the total amount of work units.
	 */
	public void beginTask(String name, int totalWork) {
		final long timestamp = System.currentTimeMillis();
		fStartTime = timestamp;
		final boolean animated = (totalWork == UNKNOWN || totalWork == 0);
		// make sure the progress bar is made visible while
		// the task is running. Fixes bug 32198 for the non-animated case.
		Runnable timer = new Runnable() {
			public void run() {
				StatusLine.this.startTask(timestamp, animated);
			}
		};
		if (fProgressBar == null) {
			return;
		}

		fProgressBar.getDisplay().timerExec(DELAY_PROGRESS, timer);
		if (!animated) {
			fProgressBar.beginTask(totalWork);
		}
		if (name == null) {
			fTaskName = Util.ZERO_LENGTH_STRING;
		} else {
			fTaskName = name;
		}
		setMessage(fTaskName);
	}

	/**
	 * Notifies that the work is done; that is, either the main task is
	 * completed or the user cancelled it. Done() can be called more than once;
	 * an implementation should be prepared to handle this case.
	 */
	public void done() {

		fStartTime = 0;

		if (fProgressBar != null) {
			fProgressBar.sendRemainingWork();
			fProgressBar.done();
		}
		setMessage(null);

		hideProgress();
	}

	/**
	 * Returns the status line's progress monitor
	 * 
	 * @return {@link IProgressMonitor} the progress monitor
	 */
	public IProgressMonitor getProgressMonitor() {
		return this;
	}

	/**
	 * @private
	 */
	protected void handleDispose() {
		if (fStopButtonCursor != null) {
			fStopButtonCursor.dispose();
			fStopButtonCursor = null;
		}
		if (fProgressBar != null) {
			fProgressBar.dispose();
			fProgressBar = null;
		}
	}

	/**
	 * Hides the Cancel button and ProgressIndicator.
	 * 
	 */
	protected void hideProgress() {

		if (fProgressIsVisible && !isDisposed()) {
			fProgressIsVisible = false;
			fCancelEnabled = false;
			fCancelButtonIsVisible = false;
			if (fToolBar != null && !fToolBar.isDisposed()) {
				fToolBar.setVisible(false);
			}
			if (fProgressBarComposite != null
					&& !fProgressBarComposite.isDisposed()) {
				fProgressBarComposite.setVisible(false);
			}
			layout();
		}
	}

	/**
	 * @see IProgressMonitor#internalWorked(double)
	 */
	public void internalWorked(double work) {
		if (!fProgressIsVisible) {
			if (System.currentTimeMillis() - fStartTime > DELAY_PROGRESS) {
				showProgress();
			}
		}

		if (fProgressBar != null) {
			fProgressBar.worked(work);
		}
	}

	/**
	 * Returns true if the user does some UI action to cancel this operation.
	 * (like hitting the Cancel button on the progress dialog). The long running
	 * operation typically polls isCanceled().
	 */
	public boolean isCanceled() {
		return fIsCanceled;
	}

	/**
	 * Returns
	 * <code>true</true> if the ProgressIndication provides UI for canceling
	 * a long running operation.
	 * @return <code>true</true> if the ProgressIndication provides UI for canceling
	 */
	public boolean isCancelEnabled() {
		return fCancelEnabled;
	}

	/**
	 * Sets the cancel status. This method is usually called with the argument
	 * false if a client wants to abort a cancel action.
	 */
	public void setCanceled(boolean b) {
		fIsCanceled = b;
		if (fCancelButton != null) {
			fCancelButton.setEnabled(!b);
		}
	}

	/**
	 * Controls whether the ProgressIndication provides UI for canceling a long
	 * running operation. If the ProgressIndication is currently visible calling
	 * this method may have a direct effect on the layout because it will make a
	 * cancel button visible.
	 * 
	 * @param enabled
	 *            <code>true</true> if cancel should be enabled
	 */
	public void setCancelEnabled(boolean enabled) {
		fCancelEnabled = enabled;
		if (fProgressIsVisible && !fCancelButtonIsVisible && enabled) {
			showButton();
			layout();
		}
		if (fCancelButton != null && !fCancelButton.isDisposed()) {
			fCancelButton.setEnabled(enabled);
		}
	}

	/**
	 * Sets the error message text to be displayed on the status line. The image
	 * on the status line is cleared.
	 * 
	 * @param message
	 *            the error message, or <code>null</code> for no error message
	 */
	public void setErrorMessage(String message) {
		setErrorMessage(null, message);
	}

	/**
	 * Sets an image and error message text to be displayed on the status line.
	 * 
	 * @param image
	 *            the image to use, or <code>null</code> for no image
	 * @param message
	 *            the error message, or <code>null</code> for no error message
	 */
	public void setErrorMessage(Image image, String message) {
		fErrorText = trim(message);
		fErrorImage = image;
		updateMessageLabel();
	}

	/**
	 * Applies the given font to this status line.
	 */
	public void setFont(Font font) {
		super.setFont(font);
		Control[] children = getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].setFont(font);
		}
	}

	/**
	 * Sets the message text to be displayed on the status line. The image on
	 * the status line is cleared.
	 * 
	 * @param message
	 *            the error message, or <code>null</code> for no error message
	 */
	public void setMessage(String message) {
		setMessage(null, message);
	}

	/**
	 * Sets an image and a message text to be displayed on the status line.
	 * 
	 * @param image
	 *            the image to use, or <code>null</code> for no image
	 * @param message
	 *            the message, or <code>null</code> for no message
	 */
	public void setMessage(Image image, String message) {
		fMessageText = trim(message);
		fMessageImage = image;
		updateMessageLabel();
	}

	/**
	 * @see IProgressMonitor#setTaskName(java.lang.String)
	 */
	public void setTaskName(String name) {
		if (name == null)
			fTaskName = Util.ZERO_LENGTH_STRING;
		else
			fTaskName = name;
	}

	/**
	 * Makes the Cancel button visible.
	 * 
	 */
	protected void showButton() {
		if (fToolBar != null && !fToolBar.isDisposed()) {
			fToolBar.setVisible(true);
			fToolBar.setEnabled(true);
			fToolBar.setCursor(fStopButtonCursor);
			fCancelButtonIsVisible = true;
		}
	}

	/**
	 * Shows the Cancel button and ProgressIndicator.
	 * 
	 */
	protected void showProgress() {
		if (!fProgressIsVisible && !isDisposed()) {
			fProgressIsVisible = true;
			if (fCancelEnabled) {
				showButton();
			}
			if (fProgressBarComposite != null
					&& !fProgressBarComposite.isDisposed()) {
				fProgressBarComposite.setVisible(true);
			}
			layout();
		}
	}

	/**
	 * @private
	 */
	void startTask(final long timestamp, final boolean animated) {
		if (!fProgressIsVisible && fStartTime == timestamp) {
			showProgress();
			if (animated) {
				if (fProgressBar != null && !fProgressBar.isDisposed()) {
					fProgressBar.beginAnimatedTask();
				}
			}
		}
	}

	/**
	 * Notifies that a subtask of the main task is beginning. Subtasks are
	 * optional; the main task might not have subtasks.
	 * 
	 * @param name
	 *            the name (or description) of the subtask
	 * @see IProgressMonitor#subTask(String)
	 */
	public void subTask(String name) {

		String newName;
		if (name == null)
			newName = Util.ZERO_LENGTH_STRING;
		else
			newName = name;

		String text;
		if (fTaskName == null || fTaskName.length() == 0) {
			text = newName;
		} else {
			text = JFaceResources.format(
					"Set_SubTask", new Object[] { fTaskName, newName });//$NON-NLS-1$
		}
		setMessage(text);
	}

	/**
	 * Trims the message to be displayable in the status line. This just pulls
	 * out the first line of the message. Allows null.
	 */
	String trim(String message) {
		if (message == null) {
			return null;
		}
		message = LegacyActionTools.escapeMnemonics(message);
		int cr = message.indexOf('\r');
		int lf = message.indexOf('\n');
		if (cr == -1 && lf == -1) {
			return message;
		}
		int len;
		if (cr == -1) {
			len = lf;
		} else if (lf == -1) {
			len = cr;
		} else {
			len = Math.min(cr, lf);
		}
		return message.substring(0, len);
	}

	/**
	 * Updates the message label widget.
	 */
	protected void updateMessageLabel() {
		if (fMessageLabel != null && !fMessageLabel.isDisposed()) {
			Display display = fMessageLabel.getDisplay();
			if ((fErrorText != null && fErrorText.length() > 0)
					|| fErrorImage != null) {
				fMessageLabel.setForeground(JFaceColors.getErrorText(display));
				fMessageLabel.setText(fErrorText);
				fMessageLabel.setImage(fErrorImage);
			} else {
				fMessageLabel.setForeground(display
						.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
				fMessageLabel.setText(fMessageText == null ? "" : fMessageText); //$NON-NLS-1$
				fMessageLabel.setImage(fMessageImage);
			}
			// RAP [if] Clipboard
//			if (copyMenuItem != null && !copyMenuItem.isDisposed()) {
//				String text = fMessageLabel.getText();
//				copyMenuItem.setEnabled(text != null && text.length() > 0);
//			}
		}
	}

	/**
	 * @see IProgressMonitor#worked(int)
	 */
	public void worked(int work) {
		internalWorked(work);
	}
}
