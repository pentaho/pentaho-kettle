/*******************************************************************************
 * Copyright (c) 2007, 2017 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.serverpush;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceStore;
import org.eclipse.rap.rwt.internal.util.SerializableLock;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.rap.rwt.service.UISessionEvent;
import org.eclipse.rap.rwt.service.UISessionListener;
import org.eclipse.swt.internal.SerializableCompatibility;


public final class ServerPushManager implements SerializableCompatibility {

  private static final int DEFAULT_REQUEST_CHECK_INTERVAL = 30000;
  private static final String FORCE_PUSH = ServerPushManager.class.getName() + "#forcePush";

  private final ServerPushActivationTracker serverPushActivationTracker;
  private final SerializableLock lock;
  // Flag that indicates whether a request is processed. In that case no
  // notifications are sent to the client.
  private boolean uiThreadRunning;
  // indicates whether the display has runnables to execute
  private boolean hasRunnables;
  private int requestCheckInterval;
  private transient ServerPushRequestTracker serverPushRequestTracker;

  private ServerPushManager() {
    lock = new SerializableLock();
    serverPushActivationTracker = new ServerPushActivationTracker();
    uiThreadRunning = false;
    requestCheckInterval = DEFAULT_REQUEST_CHECK_INTERVAL;
    serverPushRequestTracker = new ServerPushRequestTracker();
  }

  public static ServerPushManager getInstance() {
    return SingletonUtil.getSessionInstance( ServerPushManager.class );
  }

  public boolean isCallBackRequestBlocked() {
    synchronized( lock ) {
      return !serverPushRequestTracker.hasActive();
    }
  }

  public void wakeClient() {
    synchronized( lock ) {
      if( !uiThreadRunning ) {
        releaseBlockedRequest();
      }
    }
  }

  public void releaseBlockedRequest() {
    synchronized( lock ) {
      lock.notifyAll();
    }
  }

  public void setHasRunnables( boolean hasRunnables ) {
    synchronized( lock ) {
      this.hasRunnables = hasRunnables;
    }
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    if( serviceStore != null && hasRunnables && isServerPushActive() ) {
      serviceStore.setAttribute( FORCE_PUSH, Boolean.TRUE );
    }
  }

  public void setRequestCheckInterval( int requestCheckInterval ) {
    this.requestCheckInterval = requestCheckInterval;
  }

  public void notifyUIThreadStart() {
    synchronized( lock ) {
      uiThreadRunning = true;
    }
  }

  public void notifyUIThreadEnd() {
    synchronized( lock ) {
      uiThreadRunning = false;
      if( hasRunnables ) {
        wakeClient();
      }
    }
  }

  public void activateServerPushFor( Object handle ) {
    serverPushActivationTracker.activate( handle );
  }

  public void deactivateServerPushFor( Object handle ) {
    serverPushActivationTracker.deactivate( handle );
    if( !serverPushActivationTracker.isActive() ) {
      releaseBlockedRequest();
    }
  }

  public boolean hasRunnables() {
    synchronized( lock ) {
      return hasRunnables;
    }
  }

  public boolean needsActivation() {
    return isServerPushActive() || forceServerPushForPendingRunnables();
  }

  void processRequest( HttpServletResponse response ) {
    synchronized( lock ) {
      if( isCallBackRequestBlocked() ) {
        releaseBlockedRequest();
      }
      if( mustBlockCallBackRequest() ) {
        long requestStartTime = System.currentTimeMillis();
        serverPushRequestTracker.activate( Thread.currentThread() );
        TerminationListener listener = attachTerminationListener();
        try {
          boolean canRelease = false;
          while( !canRelease ) {
            lock.wait( requestCheckInterval );
            canRelease = canReleaseBlockedRequest( response, requestStartTime );
          }
        } catch( @SuppressWarnings( "unused" ) InterruptedException ie ) {
          Thread.interrupted(); // Reset interrupted state, see bug 300254
        } finally {
          listener.detach();
          serverPushRequestTracker.deactivate( Thread.currentThread() );
        }
      }
    }
  }

  private boolean canReleaseBlockedRequest( HttpServletResponse response, long requestStartTime ) {
    boolean result = false;
    if( !mustBlockCallBackRequest() ) {
      result = true;
    } else if( isSessionExpired( requestStartTime ) ) {
      result = true;
    } else if( !isConnectionAlive( response ) ) {
      result = true;
    } else if( !serverPushRequestTracker.isActive( Thread.currentThread() ) ) {
      result = true;
    }
    return result;
  }

  boolean mustBlockCallBackRequest() {
    return isServerPushActive() && !hasRunnables;
  }

  public boolean isServerPushActive() {
    return serverPushActivationTracker.isActive();
  }

  private Object readResolve() {
    serverPushRequestTracker = new ServerPushRequestTracker();
    return this;
  }

  private static TerminationListener attachTerminationListener() {
    UISession uiSession = ContextProvider.getUISession();
    TerminationListener result = new TerminationListener( uiSession );
    result.attach();
    return result;
  }

  private static boolean isSessionExpired( long requestStartTime ) {
    return isSessionExpired( requestStartTime, System.currentTimeMillis() );
  }

  static boolean isSessionExpired( long requestStartTime, long currentTime ) {
    UISession uiSession = ContextProvider.getUISession();
    if( uiSession == null ) {
      return true;
    }
    HttpSession httpSession = uiSession.getHttpSession();
    int maxInactiveInterval = httpSession.getMaxInactiveInterval();
    if( maxInactiveInterval > 0 ) {
      return currentTime > requestStartTime + maxInactiveInterval * 1000;
    }
    return false;
  }

  private static boolean isConnectionAlive( HttpServletResponse response ) {
    try {
      PrintWriter writer = response.getWriter();
      writer.write( " " );
      return !writer.checkError();
    } catch( @SuppressWarnings( "unused" ) IOException ioe ) {
      return false;
    }
  }

  private static boolean forceServerPushForPendingRunnables() {
    boolean result = false;
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    if( serviceStore != null ) {
      result = Boolean.TRUE.equals( serviceStore.getAttribute( FORCE_PUSH ) );
    }
    return result;
  }

  private static class TerminationListener implements UISessionListener {

    private transient final Thread currentThread;
    private transient final UISession uiSession;

    private TerminationListener( UISession uiSession ) {
      this.uiSession = uiSession;
      currentThread = Thread.currentThread();
    }

    public void attach() {
      uiSession.addUISessionListener( this );
    }

    public void detach() {
      uiSession.removeUISessionListener( this );
    }

    @Override
    public void beforeDestroy( UISessionEvent event ) {
      currentThread.interrupt();
    }

  }

}
