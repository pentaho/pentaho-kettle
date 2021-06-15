/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     EclipseSource - adaptation for RAP
 *******************************************************************************/
package org.eclipse.swt.widgets;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.serverpush.ServerPushManager;
import org.eclipse.rap.rwt.internal.util.SerializableLock;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.internal.Compatibility;
import org.eclipse.swt.internal.SerializableCompatibility;


/**
 * Instances of this class provide synchronization support
 * for displays. A default instance is created automatically
 * for each display, and this instance is sufficient for almost
 * all applications.
 * <p>
 * <b>IMPORTANT:</b> Typical application code <em>never</em>
 * needs to deal with this class. It is provided only to
 * allow applications which require non-standard
 * synchronization behavior to plug in the support they
 * require. <em>Subclasses which override the methods in
 * this class must ensure that the superclass methods are
 * invoked in their implementations</em>
 * </p>
 *
 * @see Display#setSynchronizer
 * <!--
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * -->
 * @since 1.3
 */
public class Synchronizer implements SerializableCompatibility {

  Display display;
  int messageCount;
  RunnableLock [] messages;
// RAP [rh] mesageLock must be serializable	(bug 345842)
//	Object messageLock = new Object ();
  Object messageLock = new SerializableLock();
  Thread syncThread;
  static final int GROW_SIZE = 4;
  static final int MESSAGE_LIMIT = 64;

  //TEMPORARY CODE
  static final boolean IS_CARBON = "carbon".equals (SWT.getPlatform ());
  static final boolean IS_COCOA = "cocoa".equals (SWT.getPlatform ());
  static final boolean IS_GTK = "gtk".equals (SWT.getPlatform ());

/**
 * Constructs a new instance of this class.
 *
 * @param display the display to create the synchronizer on
 */
public Synchronizer (Display display) {
  this.display = display;
}

void addLast (RunnableLock lock) {
  boolean wake = false;
  synchronized (messageLock) {
    if (messages == null) {
      messages = new RunnableLock [GROW_SIZE];
    }
    if (messageCount == messages.length) {
      RunnableLock[] newMessages = new RunnableLock [messageCount + GROW_SIZE];
      System.arraycopy (messages, 0, newMessages, 0, messageCount);
      messages = newMessages;
    }
    messages [messageCount++] = lock;
// RAP [rst] Notify server push mechanism when runnable was added to empty queue
    if( messageCount == 1 ) {
      RWT.getUISession( display ).exec( new Runnable() {
        @Override
        public void run() {
          ServerPushManager.getInstance().setHasRunnables( true );
        }
      } );
    }
    runnableAdded( lock.runnable );
// END RAP
    wake = messageCount == 1;
  }
  if (wake) {
    display.wakeThread ();
  }
}

@SuppressWarnings( "unused" )
protected void runnableAdded( Runnable runnable ) {
}

/**
 * Causes the <code>run()</code> method of the runnable to
 * be invoked by the user-interface thread at the next
 * reasonable opportunity. The caller of this method continues
 * to run in parallel, and is not notified when the
 * runnable has completed.
 *
 * @param runnable code to run on the user-interface thread.
 *
 * @see #syncExec
 */
protected void asyncExec (Runnable runnable) {
  if (runnable == null) {
    //TEMPORARY CODE
    if (!(IS_CARBON || IS_GTK || IS_COCOA)) {
      display.wake ();
      return;
    }
  }
  addLast (new RunnableLock (runnable));
}

int getMessageCount () {
  synchronized (messageLock) {
    return messageCount;
  }
}

void releaseSynchronizer () {
  // RAP [rh] release all threads that are waiting in syncExec(), see bug 352437
  RunnableLock runnableLock = removeFirst();
  while( runnableLock != null ) {
    if( runnableLock.thread != null ) {
      runnableLock.runnable = null;
      synchronized( runnableLock ) {
        runnableLock.notify();
      }
    }
    runnableLock = removeFirst();
  }
  // END RAP
//	display = null;
  messages = null;
  messageLock = null;
  syncThread = null;
}

RunnableLock removeFirst () {
  synchronized (messageLock) {
    if (messageCount == 0) {
      return null;
    }
    RunnableLock lock = messages [0];
    System.arraycopy (messages, 1, messages, 0, --messageCount);
    messages [messageCount] = null;
    if (messageCount == 0) {
      if (messages.length > MESSAGE_LIMIT) {
        messages = null;
      }
    }
// RAP [rst] Notify server push mechanism when last runnable has been removed
    if( messageCount == 0 ) {
      RWT.getUISession( display ).exec( new Runnable() {
        @Override
        public void run() {
          ServerPushManager.getInstance().setHasRunnables( false );
        }
      } );
    }
// END RAP
    return lock;
  }
}

boolean runAsyncMessages () {
  return runAsyncMessages (false);
}

boolean runAsyncMessages (boolean all) {
  boolean run = false;
  do {
    RunnableLock lock = removeFirst ();
    if (lock == null) {
      return run;
    }
    run = true;
    synchronized (lock) {
      syncThread = lock.thread;
      try {
        lock.run ();
      }	catch( ThreadDeath t ) {
        lock.throwable = t;
        // Don't trap ThreadDeath, see bug 284202
        throw t;
      } catch (Throwable t) {
        lock.throwable = t;
        SWT.error (SWT.ERROR_FAILED_EXEC, t);
      } finally {
        syncThread = null;
        lock.notifyAll ();
      }
    }
  } while (all);
  return run;
}

/**
 * Causes the <code>run()</code> method of the runnable to
 * be invoked by the user-interface thread at the next
 * reasonable opportunity. The thread which calls this method
 * is suspended until the runnable completes.
 *
 * @param runnable code to run on the user-interface thread.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_FAILED_EXEC - if an exception occurred when executing the runnable</li>
 * </ul>
 *
 * @see #asyncExec
 */
protected void syncExec (Runnable runnable) {
  RunnableLock lock = null;
// RAP [rh] replaced by deviceLock, see Device#deviceLock for more information
//	synchronized (Device.class) {
  synchronized (display.getDeviceLock()) {
    if (display == null || display.isDisposed ()) {
      SWT.error (SWT.ERROR_DEVICE_DISPOSED);
    }
    if (!display.isValidThread ()) {
      if (runnable == null) {
        display.wake ();
        return;
      }
      lock = new RunnableLock (runnable);
      /*
       * Only remember the syncThread for syncExec.
       */
      lock.thread = Thread.currentThread();
      addLast (lock);
    }
  }
  if (lock == null) {
    if (runnable != null) {
      runnable.run ();
    }
    return;
  }
  synchronized (lock) {
    boolean interrupted = false;
    while (!lock.done ()) {
      try {
        lock.wait ();
      } catch (@SuppressWarnings( "unused" ) InterruptedException e) {
        interrupted = true;
      }
    }
    if (interrupted) {
      Compatibility.interrupt();
    }
    if (lock.throwable != null) {
      SWT.error (SWT.ERROR_FAILED_EXEC, lock.throwable);
    }
  }
}

}
