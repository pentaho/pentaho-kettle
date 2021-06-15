/*******************************************************************************
 * Copyright (c) 2008, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.lifecycle;

import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;
import org.eclipse.rap.rwt.internal.service.ServletLog;
import org.eclipse.rap.rwt.internal.service.UISessionImpl;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.swt.widgets.Display;


@SuppressWarnings( "deprecation" )
final class UIThread extends Thread implements IUIThreadHolder, ISessionShutdownAdapter {

  static final class UIThreadTerminatedError extends ThreadDeath {
    private static final long serialVersionUID = 1L;
  }

  private ServiceContext serviceContext;
  private UISession uiSession;
  private Runnable shutdownCallback;
  private volatile boolean uiThreadTerminating;

  public UIThread( Runnable runnable ) {
    super( runnable );
  }

  //////////////////////////
  // interface IThreadHolder

  @Override
  public void setServiceContext( ServiceContext serviceContext ) {
    this.serviceContext = serviceContext;
  }

  @Override
  public void updateServiceContext() {
    if( ContextProvider.hasContext() ) {
      ContextProvider.releaseContextHolder();
    }
    ContextProvider.setContext( serviceContext );
  }

  @Override
  public void switchThread() {
    Object lock = getLock();
    synchronized( lock ) {
      checkAndReportTerminatedUIThread();
      lock.notifyAll();
      boolean done = false;
      while( !done ) {
        try {
          lock.wait();
          done = true;
        } catch( InterruptedException e ) {
          handleInterruptInSwitchThread( e );
        }
      }
    }
  }

  private void checkAndReportTerminatedUIThread() {
    // [rh] While working on bug 284202, there was the suspicion that a
    // request thread might wait infinitely on an already terminated UIThread.
    // To investigate this problem, we print to sys-err if this happens.
    if( !getThread().isAlive() ) {
      String msg
        = "Thread '"
        + Thread.currentThread()
        + "' is waiting for already terminated UIThread";
      ServletLog.log( "", new RuntimeException( msg ) );
    }
  }

  private void handleInterruptInSwitchThread( InterruptedException e )
    throws UIThreadTerminatedError
  {
    Thread.interrupted();
    if( uiThreadTerminating ) {
      // Equip the UI thread that is continuing its execution with a
      // service context and the proper phase (see terminateThread).
      updateServiceContext();
      CurrentPhase.set( PhaseId.PROCESS_ACTION );
      uiThreadTerminating = false;
      throw new UIThreadTerminatedError();
    }
    if( Thread.currentThread() != getThread() ) {
      String msg = "Received InterruptedException on request thread";
      ServletLog.log( msg, e );
    }
  }

  @Override
  public void run() {
    try {
      super.run();
    } finally {
      // TODO [rh] call lock.notifyAll()?
    }
  }

  @Override
  public void terminateThread() {
    // Prepare a service context to be used by the UI thread that may continue
    // to run as a result of the interrupt call
    ServiceContext serviceContext = ContextUtil.createFakeContext( uiSession );
    setServiceContext( serviceContext );
    uiThreadTerminating = true;
    // interrupt the UI thread that is expected to wait in switchThread or already be terminated
    synchronized( getLock() ) {
      getThread().interrupt();
    }
    try {
      getThread().join();
    } catch( InterruptedException e ) {
      String msg = "Received InterruptedException while terminating UIThread";
      ServletLog.log( msg, e );
    }
    uiThreadTerminating = false;
  }

  @Override
  public Thread getThread() {
    return this;
  }

  @Override
  public Object getLock() {
    // TODO [rh] use a distinct (final) lock object instead of 'this'
    return this;
  }


  ////////////////////////////////////
  // interface ISessionShutdownAdapter

  @Override
  public void setUISession( UISession uiSession ) {
    this.uiSession = uiSession;
  }

  @Override
  public void setShutdownCallback( Runnable shutdownCallback ) {
    this.shutdownCallback = shutdownCallback;
  }

  @Override
  public void interceptShutdown() {
    terminateThread();
  }

  @Override
  public void processShutdown() {
    updateServiceContext();
    try {
      // Simulate PROCESS_ACTION phase if the session times out
      CurrentPhase.set( PhaseId.PROCESS_ACTION );
      // TODO [rh] find a better decoupled way to dispose of the display
      Display display = LifeCycleUtil.getSessionDisplay( uiSession );
      // TODO [fappel]: Think about a better solution: isActivated() checks whether
      //                the applicationContext is still activated before starting
      //                cleanup. This is due to the missing possibility of OSGi HttpService
      //                to shutdown HttpContext instances. Therefore sessions will survive the
      //                deactivation of ApplicationContext instances. In case the HttpService
      //                gets halted the corresponding ApplicationContext instances have already
      //                been deactivated and this will cause a NPE.
      if( isApplicationContextActive() && display != null ) {
        display.dispose();
      }
      shutdownCallback.run();
    } finally {
      ContextProvider.disposeContext();
    }
  }

  private boolean isApplicationContextActive() {
    ApplicationContextImpl applicationContext = ( ( UISessionImpl )uiSession ).getApplicationContext();
    return applicationContext != null && applicationContext.isActive();
  }

}
