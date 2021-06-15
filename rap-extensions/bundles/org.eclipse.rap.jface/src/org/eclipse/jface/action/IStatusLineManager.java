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
package org.eclipse.jface.action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;

/**
 * The <code>IStatusLineManager</code> interface provides protocol
 * for displaying messages on a status line, for monitoring progress,
 * and for managing contributions to the status line.
 * <p>
 * <b>Note:</b> An error message overrides the current message until
 * the error message is cleared.
 * </p><p>
 * This package also provides a concrete status line manager implementation,
 * {@link StatusLineManager <code>StatusLineManager</code>}.
 * </p>
 */
public interface IStatusLineManager extends IContributionManager {
	/**
	 * Returns a progress monitor which reports progress in the status line.
	 * Note that the returned progress monitor may only be accessed from the UI
	 * thread.
	 * 
	 * @return the progress monitor
	 * 
	 *         Note: There is a delay after a beginTask message before the
	 *         monitor is shown. This may not be appropriate for all apps.
	 */
    public IProgressMonitor getProgressMonitor();

    /**
     * Returns whether the cancel button on the status line's progress monitor
     * is enabled.
     *
     * @return <code>true</code> if the cancel button is enabled, or <code>false</code> if not
     */
    public boolean isCancelEnabled();

    /**
     * Sets whether the cancel button on the status line's progress monitor
     * is enabled.
     *
     * @param enabled <code>true</code> if the cancel button is enabled, or <code>false</code> if not
     */
    public void setCancelEnabled(boolean enabled);

    /**
     * Sets the error message text to be displayed on the status line.
     * The image on the status line is cleared.
     * <p>
     * An error message overrides the current message until the error 
     * message is cleared (set to <code>null</code>).
     * </p>
     *
     * @param message the error message, or <code>null</code> to clear
     * 		the current error message.
     */
    public void setErrorMessage(String message);

    /**
     * Sets the image and error message to be displayed on the status line.
     * <p>
     * An error message overrides the current message until the error 
     * message is cleared (set to <code>null</code>).
     * </p>
     *
     * @param image the image to use, or <code>null</code> for no image
     * @param message the error message, or <code>null</code> to clear
     * 		the current error message.
     */
    public void setErrorMessage(Image image, String message);

    /**
     * Sets the message text to be displayed on the status line.
     * The image on the status line is cleared.
     * <p>
     * This method replaces the current message but does not affect the 
     * error message. That is, the error message, if set, will continue
     * to be displayed until it is cleared (set to <code>null</code>).
     * </p>
     *
     * @param message the message, or <code>null</code> for no message
     */
    public void setMessage(String message);

    /**
     * Sets the image and message to be displayed on the status line.
     * <p>
     * This method replaces the current message but does not affect the 
     * error message. That is, the error message, if set, will continue
     * to be displayed until it is cleared (set to <code>null</code>).
     * </p>
     *
     * @param image the image to use, or <code>null</code> for no image
     * @param message the message, or <code>null</code> for no message
     */
    public void setMessage(Image image, String message);
}
