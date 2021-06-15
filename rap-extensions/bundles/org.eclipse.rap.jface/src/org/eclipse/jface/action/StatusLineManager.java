/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A status line manager is a contribution manager which realizes itself and its items
 * in a status line control.
 * <p>
 * This class may be instantiated; it may also be subclassed if a more
 * sophisticated layout is required.
 * </p>
 * @since 1.0
 */
public class StatusLineManager extends ContributionManager implements
        IStatusLineManager {

    /**
     * Identifier of group marker used to position contributions at the beginning
     * of the status line.
     * 
     */
    public static final String BEGIN_GROUP = "BEGIN_GROUP"; //$NON-NLS-1$

    /**
     * Identifier of group marker used to position contributions in the middle
     * of the status line.
     * 
     */
    public static final String MIDDLE_GROUP = "MIDDLE_GROUP"; //$NON-NLS-1$

    /**
     * Identifier of group marker used to position contributions at the end
     * of the status line.
     * 
     */
    public static final String END_GROUP = "END_GROUP"; //$NON-NLS-1$

    /**
     * The status line control; <code>null</code> before
     * creation and after disposal.
     */
    private Composite statusLine = null;

    /**
     * Creates a new status line manager.
     * Use the <code>createControl</code> method to create the 
     * status line control.
     */
    public StatusLineManager() {
    	add(new GroupMarker(BEGIN_GROUP));
        add(new GroupMarker(MIDDLE_GROUP));
        add(new GroupMarker(END_GROUP));
    }

    /**
     * Creates and returns this manager's status line control. 
     * Does not create a new control if one already exists.
     * <p>
     * Note: Since 3.0 the return type is <code>Control</code>.  Before 3.0, the return type was 
     *   the package-private class <code>StatusLine</code>.
     * </p>
     *
     * @param parent the parent control
     * @return the status line control
     */
    public Control createControl(Composite parent) {
        return createControl(parent, SWT.NONE);
    }

    /**
     * Creates and returns this manager's status line control. 
     * Does not create a new control if one already exists.
     *
     * @param parent the parent control
     * @param style the style for the control
     * @return the status line control
     */
    public Control createControl(Composite parent, int style) {
        if (!statusLineExist() && parent != null) {
            statusLine = new StatusLine(parent, style);
            update(false);
        }
        return statusLine;
    }

    /**
     * Disposes of this status line manager and frees all allocated SWT resources.
     * Notifies all contribution items of the dispose. Note that this method does
     * not clean up references between this status line manager and its associated
     * contribution items. Use <code>removeAll</code> for that purpose.
     */
    public void dispose() {
        if (statusLineExist()) {
			statusLine.dispose();
		}
        statusLine = null;

        IContributionItem items[] = getItems();
        for (int i = 0; i < items.length; i++) {
            items[i].dispose();
        }
    }

    /**
     * Returns the control used by this StatusLineManager.
     * 
     * @return the control used by this manager
     */
    public Control getControl() {
        return statusLine;
    }

    /**
     * Returns the progress monitor delegate. Override this method
     * to provide your own object used to handle progress.
     * 
     * @return the IProgressMonitor delegate
     */
    protected IProgressMonitor getProgressMonitorDelegate() {
        return (IProgressMonitor) getControl();
    }

    /*
     * (non-Javadoc)
     * Method declared on IStatusLineManager
     */
    public IProgressMonitor getProgressMonitor() {

        return new IProgressMonitorWithBlocking() {

            IProgressMonitor progressDelegate = getProgressMonitorDelegate();

            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
             */
            public void beginTask(String name, int totalWork) {
                progressDelegate.beginTask(name, totalWork);

            }

            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.IProgressMonitor#done()
             */
            public void done() {
                progressDelegate.done();
            }

            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
             */
            public void internalWorked(double work) {
                progressDelegate.internalWorked(work);

            }

            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
             */
            public boolean isCanceled() {
                return progressDelegate.isCanceled();
            }

            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
             */
            public void setCanceled(boolean value) {
                //Don't bother updating for disposed status
                if (statusLineExist()) {
                	progressDelegate.setCanceled(value);
				}
            }

            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
             */
            public void setTaskName(String name) {
                progressDelegate.setTaskName(name);

            }

            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
             */
            public void subTask(String name) {
                progressDelegate.subTask(name);

            }

            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
             */
            public void worked(int work) {
                progressDelegate.worked(work);
            }

            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#clearBlocked()
             */
            public void clearBlocked() {
                //Do nothing here as we let the modal context handle it
            }

            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#setBlocked(org.eclipse.core.runtime.IStatus)
             */
            public void setBlocked(IStatus reason) {
                //			Do nothing here as we let the modal context handle it
            }
        };
    }

    /* (non-Javadoc)
     * Method declared on IStatueLineManager
     */
    public boolean isCancelEnabled() {
        return statusLineExist() && ((StatusLine) statusLine).isCancelEnabled();
    }

    /* (non-Javadoc)
     * Method declared on IStatueLineManager
     */
    public void setCancelEnabled(boolean enabled) {
        if (statusLineExist()) {
			((StatusLine) statusLine).setCancelEnabled(enabled);
		}
    }

    /* (non-Javadoc)
     * Method declared on IStatusLineManager.
     */
    public void setErrorMessage(String message) {
        if (statusLineExist()) {
			((StatusLine) statusLine).setErrorMessage(message);
		}
    }

    /* (non-Javadoc)
     * Method declared on IStatusLineManager.
     */
    public void setErrorMessage(Image image, String message) {
        if (statusLineExist()) {
			((StatusLine) statusLine).setErrorMessage(image, message);
		}
    }

    /* (non-Javadoc)
     * Method declared on IStatusLineManager.
     */
    public void setMessage(String message) {
        if (statusLineExist()) {
			((StatusLine) statusLine).setMessage(message);
		}
    }

    /* (non-Javadoc)
     * Method declared on IStatusLineManager.
     */
    public void setMessage(Image image, String message) {
        if (statusLineExist()) {
			((StatusLine) statusLine).setMessage(image, message);
		}
    }

    /**
     * Returns whether the status line control is created
     * and not disposed.
     * 
     * @return <code>true</code> if the control is created
     *	and not disposed, <code>false</code> otherwise
     */
    private boolean statusLineExist() {
        return statusLine != null && !statusLine.isDisposed();
    }

    /* (non-Javadoc)
     * Method declared on IContributionManager.
     */
    public void update(boolean force) {

        //boolean DEBUG= false;

        if (isDirty() || force) {

            if (statusLineExist()) {
                statusLine.setRedraw(false);

                // NOTE: the update algorithm is non-incremental.
                // An incremental algorithm requires that SWT items can be created in the middle of the list
                // but the ContributionItem.fill(Composite) method used here does not take an index, so this
                // is not possible.

                Control ws[] = statusLine.getChildren();
                for (int i = 0; i < ws.length; i++) {
                    Control w = ws[i];
                    Object data = w.getData();
                    if (data instanceof IContributionItem) {
                        w.dispose();
                    }
                }

                int oldChildCount = statusLine.getChildren().length;
                IContributionItem[] items = getItems();
                for (int i = 0; i < items.length; ++i) {
                    IContributionItem ci = items[i];
                    if (ci.isVisible()) {
                        ci.fill(statusLine);
                        // associate controls with contribution item
                        Control[] newChildren = statusLine.getChildren();
                        for (int j = oldChildCount; j < newChildren.length; j++) {
                            newChildren[j].setData(ci);
                        }
                        oldChildCount = newChildren.length;
                    }
                }

                setDirty(false);

                statusLine.layout();
                statusLine.setRedraw(true);
            }
        }
    }

}
