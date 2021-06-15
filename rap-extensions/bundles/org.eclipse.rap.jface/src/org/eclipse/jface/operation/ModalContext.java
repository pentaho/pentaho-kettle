/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     EclipseSource - adaptation for RAP
 *******************************************************************************/
package org.eclipse.jface.operation;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.Policy;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.serverpush.ServerPushManager;
import org.eclipse.swt.widgets.Display;

/**
 * Utility class for supporting modal operations. The runnable passed to the
 * <code>run</code> method is executed in a separate thread, depending on the
 * value of the passed fork argument. If the runnable is executed in a separate
 * thread then the current thread either waits until the new thread ends or, if
 * the current thread is the UI thread, it polls the SWT event queue and
 * dispatches each event.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ModalContext {

	/**
	 * Indicated whether ModalContext is in debug mode; <code>false</code> by
	 * default.
	 */
	private static boolean debug = false;

	/**
	 * The number of nested modal runs, or 0 if not inside a modal run. This is
	 * global state.
	 */
	private static int modalLevel = 0;

	/**
	 * Indicates whether operations should be run in a separate thread. Defaults
	 * to true. For internal debugging use, set to false to run operations in
	 * the calling thread.
	 */
	private static boolean runInSeparateThread = true;

	/**
	 * Thread which runs the modal context.
	 */
	private static class ModalContextThread extends Thread {
		/**
		 * The operation to be run.
		 */
		private IRunnableWithProgress runnable;

		/**
		 * The exception thrown by the operation starter.
		 */
		private Throwable throwable;

		/**
		 * The progress monitor used for progress and cancelation.
		 */
		private IProgressMonitor progressMonitor;

		/**
		 * The display used for event dispatching.
		 */
		private Display display;

		/**
		 * Indicates whether to continue event queue dispatching.
		 */
		private volatile boolean continueEventDispatching = true;

		/**
		 * The thread that forked this modal context thread.
		 * 
		 */
		private Thread callingThread;

		/**
		 * Creates a new modal context.
		 * 
		 * @param operation
		 *            the runnable to run
		 * @param monitor
		 *            the progress monitor to use to display progress and
		 *            receive requests for cancelation
		 * @param display
		 *            the display to be used to read and dispatch events
		 */
		private ModalContextThread(IRunnableWithProgress operation,
				IProgressMonitor monitor, Display display) {
			super("ModalContext"); //$NON-NLS-1$
			Assert.isTrue(monitor != null && display != null);
			runnable = operation;
			progressMonitor = new AccumulatingProgressMonitor(monitor, display);
			this.display = display;
			this.callingThread = Thread.currentThread();
		}

		/*
		 * (non-Javadoc) Method declared on Thread.
		 */
		public void run() {
// RAP [rh] supply a fake-context for the entire execution of runnable and exception handling			
		    RWT.getUISession( display ).exec( new Runnable() {

				public void run() {
					try {
						if (runnable != null) {
							runnable.run(progressMonitor);
						}
					} catch (InvocationTargetException e) {
						throwable = e;
					} catch (InterruptedException e) {
						throwable = e;
					} catch (RuntimeException e) {
						throwable = e;
					} catch (ThreadDeath e) {
						// Make sure to propagate ThreadDeath, or threads will never
						// fully terminate
						throw e;
					} catch (Error e) {
						throwable = e;
					} finally {
						// notify the operation of change of thread of control
						if (runnable instanceof IThreadListener) {
							Throwable exception = 
								invokeThreadListener(((IThreadListener) runnable), callingThread);
							
							//Forward it if we don't already have one
							if(exception != null && throwable == null)
								throwable = exception;
						}
						
						// Make sure that all events in the asynchronous event queue
						// are dispatched.
						display.syncExec(new Runnable() {
							public void run() {
								// do nothing
							}
						});
						
						// Stop event dispatching
						continueEventDispatching = false;
						
						// Force the event loop to return from sleep () so that
						// it stops event dispatching.
						display.asyncExec(null);
						
						// RAP [fappel]: deactivate UI-Callback for this thread
						Object pushHandle = ModalContextThread.this;
						ServerPushManager.getInstance().deactivateServerPushFor( pushHandle );
						
					}
				}
				
			} ); // RAP [rh] end of fake-context runnable
		}

		/**
		 * Processes events or waits until this modal context thread terminates.
		 */
		public void block() {
			if (display == Display.getCurrent()) {
				int exceptionCount = 0;
				while (continueEventDispatching) {
					// Run the event loop. Handle any uncaught exceptions caused
					// by UI events.
					try {
						if (!display.readAndDispatch()) {
							display.sleep();
						}
						exceptionCount = 0;
					}
					// ThreadDeath is a normal error when the thread is dying.
					// We must
					// propagate it in order for it to properly terminate.
					catch (ThreadDeath e) {
						throw (e);
					}
					// For all other exceptions, log the problem.
					catch (Throwable t) {
						if (t instanceof VirtualMachineError) {
							throw (VirtualMachineError) t;
						}
						exceptionCount++;
						// We're counting exceptions in client code, such as asyncExecs,
						// so be generous about how many may fail consecutively before we
						// give up.
						if (exceptionCount > 50 || display.isDisposed()) {
			                if (t instanceof RuntimeException) {
								throw (RuntimeException) t;
							} else if (t instanceof Error) {
								throw (Error) t;
							} else {
								throw new RuntimeException(t);
							}
						}
						Policy
								.getLog()
								.log(
										new Status(
												IStatus.ERROR,
												Policy.JFACE,
												"Unhandled event loop exception during blocked modal context.",//$NON-NLS-1$
												t));
					}
				}
			} else {
				try {
					join();
				} catch (InterruptedException e) {
					throwable = e;
				}
			}
		}
	}

	/**
	 * Returns whether the first progress monitor is the same as, or a wrapper
	 * around, the second progress monitor.
	 * 
	 * @param monitor1
	 *            the first progress monitor
	 * @param monitor2
	 *            the second progress monitor
	 * @return <code>true</code> if the first is the same as, or a wrapper
	 *         around, the second
	 * @see ProgressMonitorWrapper
	 */
	public static boolean canProgressMonitorBeUsed(IProgressMonitor monitor1,
			IProgressMonitor monitor2) {
		if (monitor1 == monitor2) {
			return true;
		}

		while (monitor1 instanceof ProgressMonitorWrapper) {
			monitor1 = ((ProgressMonitorWrapper) monitor1)
					.getWrappedProgressMonitor();
			if (monitor1 == monitor2) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks with the given progress monitor and throws
	 * <code>InterruptedException</code> if it has been canceled.
	 * <p>
	 * Code in a long-running operation should call this method regularly so
	 * that a request to cancel will be honored.
	 * </p>
	 * <p>
	 * Convenience for:
	 * 
	 * <pre>
	 * if (monitor.isCanceled())
	 * 	throw new InterruptedException();
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param monitor
	 *            the progress monitor
	 * @exception InterruptedException
	 *                if cancelling the operation has been requested
	 * @see IProgressMonitor#isCanceled()
	 */
	public static void checkCanceled(IProgressMonitor monitor)
			throws InterruptedException {
		if (monitor.isCanceled()) {
			throw new InterruptedException();
		}
	}

	/**
	 * Returns the currently active modal context thread, or null if no modal
	 * context is active.
	 */
	private static ModalContextThread getCurrentModalContextThread() {
		Thread t = Thread.currentThread();
		if (t instanceof ModalContextThread) {
			return (ModalContextThread) t;
		}
		return null;
	}

	/**
	 * Returns the modal nesting level.
	 * <p>
	 * The modal nesting level increases by one each time the
	 * <code>ModalContext.run</code> method is called within the dynamic scope
	 * of another call to <code>ModalContext.run</code>.
	 * </p>
	 * 
	 * @return the modal nesting level, or <code>0</code> if this method is
	 *         called outside the dynamic scope of any invocation of
	 *         <code>ModalContext.run</code>
	 */
	public static int getModalLevel() {
		return modalLevel;
	}

	/**
	 * Returns whether the given thread is running a modal context.
	 * 
	 * @param thread
	 *            The thread to be checked
	 * @return <code>true</code> if the given thread is running a modal
	 *         context, <code>false</code> if not
	 */
	public static boolean isModalContextThread(Thread thread) {
		return thread instanceof ModalContextThread;
	}

	/**
	 * Runs the given runnable in a modal context, passing it a progress
	 * monitor.
	 * <p>
	 * The modal nesting level is increased by one from the perspective of the
	 * given runnable.
	 * </p>
	 * <p>
	 * If the supplied operation implements <code>IThreadListener</code>, it
	 * will be notified of any thread changes required to execute the operation.
	 * Specifically, the operation will be notified of the thread that will call
	 * its <code>run</code> method before it is called, and will be notified
	 * of the change of control back to the thread calling this method when the
	 * operation completes. These thread change notifications give the operation
	 * an opportunity to transfer any thread-local state to the execution thread
	 * before control is transferred to the new thread.
	 * </p>
	 * 
	 * @param operation
	 *            the runnable to run
	 * @param fork
	 *            <code>true</code> if the runnable should run in a separate
	 *            thread, and <code>false</code> if in the same thread
	 * @param monitor
	 *            the progress monitor to use to display progress and receive
	 *            requests for cancelation
	 * @param display
	 *            the display to be used to read and dispatch events
	 * @exception InvocationTargetException
	 *                if the run method must propagate a checked exception, it
	 *                should wrap it inside an
	 *                <code>InvocationTargetException</code>; runtime
	 *                exceptions and errors are automatically wrapped in an
	 *                <code>InvocationTargetException</code> by this method
	 * @exception InterruptedException
	 *                if the operation detects a request to cancel, using
	 *                <code>IProgressMonitor.isCanceled()</code>, it should
	 *                exit by throwing <code>InterruptedException</code>;
	 *                this method propagates the exception
	 */
	public static void run(IRunnableWithProgress operation, boolean fork,
			IProgressMonitor monitor, Display display)
			throws InvocationTargetException, InterruptedException {
		Assert.isTrue(operation != null && monitor != null);

		modalLevel++;
		try {
			if (monitor != null) {
				monitor.setCanceled(false);
			}
			// Is the runnable supposed to be execute in the same thread.
			if (!fork || !runInSeparateThread) {
				runInCurrentThread(operation, monitor);
			} else {
				ModalContextThread t = getCurrentModalContextThread();
				if (t != null) {
					Assert.isTrue(canProgressMonitorBeUsed(monitor,
							t.progressMonitor));
					runInCurrentThread(operation, monitor);
				} else {
					t = new ModalContextThread(operation, monitor, display);
					Throwable listenerException = null;
					if (operation instanceof IThreadListener) {
						listenerException = invokeThreadListener((IThreadListener) operation, t);
					}
					// RAP [fappel]: start UI-Callback to enable UI-updates
					Object pushHandle = t;
					ServerPushManager.getInstance().activateServerPushFor( pushHandle );
					
					if(listenerException == null){
						t.start();
						t.block();
					}
					else {
						if(t.throwable == null)
							t.throwable = listenerException;
					}
					Throwable throwable = t.throwable;
					if (throwable != null) {
						if (debug
								&& !(throwable instanceof InterruptedException)
								&& !(throwable instanceof OperationCanceledException)) {
							System.err
									.println("Exception in modal context operation:"); //$NON-NLS-1$
							throwable.printStackTrace();
							System.err.println("Called from:"); //$NON-NLS-1$
							// Don't create the InvocationTargetException on the
							// throwable,
							// otherwise it will print its stack trace (from the
							// other thread).
							new InvocationTargetException(null)
									.printStackTrace();
						}
						if (throwable instanceof InvocationTargetException) {
							throw (InvocationTargetException) throwable;
						} else if (throwable instanceof InterruptedException) {
							throw (InterruptedException) throwable;
						} else if (throwable instanceof OperationCanceledException) {
							// See 1GAN3L5: ITPUI:WIN2000 - ModalContext
							// converts OperationCancelException into
							// InvocationTargetException
							throw new InterruptedException(throwable
									.getMessage());
						} else {
							throw new InvocationTargetException(throwable);
						}
					}
				}
			}
		} finally {
			modalLevel--;
		}
	}

	/**
	 * Invoke the ThreadListener if there are any errors or RuntimeExceptions
	 * return them.
	 * 
	 * @param listener
	 * @param switchingThread
	 *            the {@link Thread} being switched to
	 */
	static Throwable invokeThreadListener(IThreadListener listener,
			Thread switchingThread) {
		try {
			listener.threadChange(switchingThread);
		} catch (ThreadDeath e) {
			// Make sure to propagate ThreadDeath, or threads will never
			// fully terminate
			throw e;
		} catch (Error e) {
			return e;
		}catch (RuntimeException e) {
			return e;
		}
		return null;
	}

	/**
	 * Run a runnable. Convert all thrown exceptions to either
	 * InterruptedException or InvocationTargetException
	 */
	private static void runInCurrentThread(IRunnableWithProgress runnable,
			IProgressMonitor progressMonitor) throws InterruptedException,
			InvocationTargetException {
		try {
			if (runnable != null) {
				runnable.run(progressMonitor);
			}
		} catch (InvocationTargetException e) {
			throw e;
		} catch (InterruptedException e) {
			throw e;
		} catch (OperationCanceledException e) {
			throw new InterruptedException();
		} catch (ThreadDeath e) {
			// Make sure to propagate ThreadDeath, or threads will never fully
			// terminate
			throw e;
		} catch (RuntimeException e) {
			throw new InvocationTargetException(e);
		} catch (Error e) {
			throw new InvocationTargetException(e);
		}
	}

	/**
	 * Sets whether ModalContext is running in debug mode.
	 * 
	 * @param debugMode
	 *            <code>true</code> for debug mode, and <code>false</code>
	 *            for normal mode (the default)
	 */
	public static void setDebugMode(boolean debugMode) {
		debug = debugMode;
	}

	/**
	 * Sets whether ModalContext may process events (by calling
	 * <code>Display.readAndDispatch()</code>) while running operations. By
	 * default, ModalContext will process events while running operations. Use
	 * this method to disallow event processing temporarily.
	 * 
	 * @param allowReadAndDispatch
	 *            <code>true</code> (the default) if events may be processed
	 *            while running an operation, <code>false</code> if
	 *            Display.readAndDispatch() should not be called from
	 *            ModalContext.
	 */
	public static void setAllowReadAndDispatch(boolean allowReadAndDispatch) {
		// use a separate thread if and only if it is OK to spin the event loop
		runInSeparateThread = allowReadAndDispatch;
	}
}
