/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Chris Gross (schtoo@schtoo.com) - initial API and implementation
 *       (bug 49497 [RCP] JFace dependency on org.eclipse.core.runtime enlarges standalone JFace applications)
 *******************************************************************************/

package org.eclipse.jface.util;

import java.io.Serializable;

import org.eclipse.core.runtime.ISafeRunnable;

/**
 * Runs a safe runnables.
 * <p>
 * Clients may provide their own implementation to change
 * how safe runnables are run from within JFace.
 * </p>
 * 
 * @see SafeRunnable#getRunner()
 * @see SafeRunnable#setRunner(ISafeRunnableRunner)
 * @see SafeRunnable#run(ISafeRunnable)
 * @since 1.0
 */
public interface ISafeRunnableRunner extends Serializable {
	
	/**
	 * Runs the runnable.  All <code>ISafeRunnableRunners</code> must catch any exception
	 * thrown by the <code>ISafeRunnable</code> and pass the exception to 
	 * <code>ISafeRunnable.handleException()</code>. 
	 * @param code the code executed as a save runnable
	 *
	 * @see SafeRunnable#run(ISafeRunnable)
	 */
	public abstract void run(ISafeRunnable code);
	
}
