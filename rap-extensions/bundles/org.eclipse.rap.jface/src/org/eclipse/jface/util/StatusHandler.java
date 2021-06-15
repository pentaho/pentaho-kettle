/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.util;

import org.eclipse.core.runtime.IStatus;

/**
 * A mechanism to handle statuses throughout JFace.
 * <p>
 * Clients may provide their own implementation to change how statuses are
 * handled from within JFace.
 * </p>
 * 
 * @see org.eclipse.jface.util.Policy#getStatusHandler()
 * @see org.eclipse.jface.util.Policy#setStatusHandler(StatusHandler)
 * 
 * @since 1.1
 */
abstract public class StatusHandler {
    
	/**
	 * Show the given status.
	 * 
	 * @param status
	 *            status to handle
	 * @param title
	 *            title for the status
	 */
	abstract public void show(IStatus status, String title);
}
