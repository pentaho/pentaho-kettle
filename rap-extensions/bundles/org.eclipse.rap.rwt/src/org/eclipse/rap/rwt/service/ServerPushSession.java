/*******************************************************************************
 * Copyright (c) 2012, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.service;

import org.eclipse.rap.rwt.internal.lifecycle.CurrentPhase;
import org.eclipse.rap.rwt.internal.serverpush.ServerPushManager;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.widgets.Display;


/**
 * A server push session is used to enable UI updates from a background thread for a certain period
 * of time.
 * <p>
 * Due to the request-response nature of HTTP, the client does not automatically notice UI updates
 * that happen outside of the UI thread (i.e. outside of a request). This is the case when e.g.
 * <code>Display.asyncExec()</code> is called from a background thread. Therefore, the framework
 * provides a mechanism called <em>server push</em> that maintains a standing connection to refresh
 * the client immediately in case of a UI update.
 * </p>
 * <p>
 * To activate the server push, create and start a ServerPushSession. Normally, the server push is
 * only needed for a certain period of time, e.g. while a background task is running and keeps
 * updating a progress bar. The push session should be stopped when it is no longer needed to allow
 * the server to free resources related to the server push.
 * </p>
 * <p>
 * Several independent server push sessions can be created and started in parallel within the same
 * UISession. As long as there is at least one server push session running, the server push will
 * remain active. A server push session can be re-used, i.e. started and stopped multiple times. If
 * not stopped explicitly, all server push sessions will be stopped when the UI session terminates.
 * </p>
 * <p>
 * Example code:
 * </p>
 * <pre>
 * final ServerPushSession pushSession = new ServerPushSession();
 * Runnable bgRunnable = new Runnable() {
 *   public void run() {
 *     // do some background work ...
 *     // schedule the UI update
 *     display.asyncExec( new Runnable() {
 *       public void run() {
 *         if( !widget.isDisposed() ) {
 *           label.setText( "updated" );
 *         }
 *       }
 *     } );
 *     // close push session when finished
 *     pushSession.stop();
 *   } );
 * };
 * pushSession.start();
 * Thread bgThread = new Thread( bgRunnable );
 * bgThread.setDaemon( true );
 * bgThread.start();
 * </pre>
 *
 * @see Display#asyncExec
 * @see Display#syncExec
 * @see Display#wake
 * @since 2.0
 */
public class ServerPushSession implements SerializableCompatibility {

  private final UISession uiSession;

  /**
   * Creates a new ServerPushSession for the current UISession. This constructor must be called from
   * the UI thread.
   *
   * @throws IllegalStateException when called from a non-UI thread
   */
  public ServerPushSession() {
    checkThread();
    uiSession = ContextProvider.getUISession();
  }

  /**
   * Starts this server push session. If the framework's server push system is not yet active for
   * the current UI session, it will be activated. Calling this method while this server push
   * session is already running has no effect.
   * <p>
   * This method must be called from the UI thread.
   * </p>
   *
   * @throws IllegalStateException when called from a non-UI thread
   */
  public void start() {
    checkThread();
    ServerPushManager.getInstance().activateServerPushFor( this );
  }

  /**
   * Stops this server push session. If no other server push sessions are currently running, the
   * framework's server push system will be stopped for the current UI session. Calling this method
   * while this server push session is not running has no effect.
   * <p>
   * This method may be called from a background thread.
   * </p>
   */
  public void stop() {
    uiSession.exec( new Runnable() {
      @Override
      public void run() {
        ServerPushManager.getInstance().deactivateServerPushFor( ServerPushSession.this );
      }
    } );
  }

  private void checkThread() {
    if( !ContextProvider.hasContext()
        || uiSession != null && ContextProvider.getUISession() != uiSession
        || CurrentPhase.get() == null )
    {
      throw new IllegalStateException( "Invalid thread access" );
    }
  }

}
