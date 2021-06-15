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
package org.eclipse.jface.operation;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * Interface for UI components which can execute a long-running operation
 * in the form of an <code>IRunnableWithProgress</code>.
 * The context is responsible for displaying a progress indicator and Cancel
 * button to the end user while the operation is in progress; the context
 * supplies a progress monitor to be used from code running inside the operation.
 * Note that an <code>IRunnableContext</code> is not a runnable itself.
 * <p>
 * For examples of UI components which implement this interface,
 * see <code>ApplicationWindow</code>, <code>ProgressMonitorDialog</code>,
 * and <code>WizardDialog</code>.
 * </p>
 *
 * @see IRunnableWithProgress
 * @see org.eclipse.jface.window.ApplicationWindow
 * @see org.eclipse.jface.dialogs.ProgressMonitorDialog
 * @see org.eclipse.jface.wizard.WizardDialog
 */
public interface IRunnableContext extends Serializable {
    /**
     * <p>
     * Runs the given <code>IRunnableWithProgress</code> in this context.
     * For example, if this is a <code>ProgressMonitorDialog</code> then the runnable
     * is run using this dialog's progress monitor.
     * </p>
     * <p>
     * If <code>fork</code> is <code>false</code>, the current thread is used
     * to run the runnable. Note that if <code>fork</code> is <code>true</code>,
     * it is unspecified whether or not this method blocks until the runnable
     * has been run. Implementers should document whether the runnable is run
     * synchronously (blocking) or asynchronously (non-blocking), or if no
     * assumption can be made about the blocking behaviour.
     * </p>
     *
     * @param fork <code>true</code> if the runnable should be run in a separate thread,
     *  and <code>false</code> to run in the same thread
     * @param cancelable <code>true</code> to enable the cancelation, and
     *  <code>false</code> to make the operation uncancellable
     * @param runnable the runnable to run
     *
     * @exception InvocationTargetException wraps any exception or error which occurs 
     *  while running the runnable
     * @exception InterruptedException propagated by the context if the runnable 
     *  acknowledges cancelation by throwing this exception.  This should not be thrown
     *  if cancelable is <code>false</code>.
     */
    public void run(boolean fork, boolean cancelable,
            IRunnableWithProgress runnable) throws InvocationTargetException,
            InterruptedException;
}
