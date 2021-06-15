/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.lifecycle;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;
import org.eclipse.rap.rwt.internal.service.UISessionImpl;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.swt.internal.widgets.IDisplayAdapter;
import org.eclipse.swt.widgets.Display;


@SuppressWarnings( "deprecation" )
public class SimpleLifeCycle extends LifeCycle {

  private final IPhase[] phases;

  public SimpleLifeCycle( ApplicationContextImpl applicationContext ) {
    super( applicationContext );
    phases = new IPhase[] {
      new PrepareUIRoot( applicationContext ),
      new ReadData(),
      new ProcessAction(),
      new Render()
    };
  }

  @Override
  public void execute() throws IOException {
    installSessionShutdownAdapter();
    UISession uiSession = ContextProvider.getUISession();
    attachThread( LifeCycleUtil.getSessionDisplay(), uiSession );
    try {
      PhaseExecutor phaseExecutor = new SessionDisplayPhaseExecutor( phases, this );
      phaseExecutor.execute( PhaseId.PREPARE_UI_ROOT );
    } finally {
      detachThread( LifeCycleUtil.getSessionDisplay(), uiSession );
    }
  }

  @Override
  public void requestThreadExec( Runnable runnable ) {
    runnable.run();
  }

  @Override
  public void sleep() {
    String msg = "Display#sleep() not supported in current operation mode.";
    throw new UnsupportedOperationException( msg );
  }

  private void installSessionShutdownAdapter() {
    UISessionImpl uiSession = ( UISessionImpl )ContextProvider.getUISession();
    if( uiSession.getShutdownAdapter() == null ) {
      uiSession.setShutdownAdapter( new SimpleSessionShutdownAdapter( applicationContext ) );
    }
  }

  private static void attachThread( Display display, UISession uiSession ) {
    if( display != null ) {
      IDisplayAdapter displayAdapter = display.getAdapter( IDisplayAdapter.class );
      displayAdapter.attachThread();
    }
    IUIThreadHolder uiThreadHolder = new SimpleUIThreadHolder( Thread.currentThread() );
    LifeCycleUtil.setUIThread( uiSession, uiThreadHolder );
  }

  private static void detachThread( Display display, UISession uiSession ) {
    if( display != null ) {
      IDisplayAdapter displayAdapter = display.getAdapter( IDisplayAdapter.class );
      displayAdapter.detachThread();
    }
    LifeCycleUtil.setUIThread( uiSession, null );
  }

  private class SessionDisplayPhaseExecutor extends PhaseExecutor {

    SessionDisplayPhaseExecutor( IPhase[] phases, LifeCycle lifecycle ) {
      super( phaseListenerManager, phases, lifecycle );
    }

    @Override
    Display getDisplay() {
      return LifeCycleUtil.getSessionDisplay();
    }

  }

  private static class SimpleUIThreadHolder implements IUIThreadHolder {
    private final Thread thread;

    public SimpleUIThreadHolder( Thread thread ) {
      this.thread = thread;
    }

    @Override
    public void updateServiceContext() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void terminateThread() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void switchThread() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setServiceContext( ServiceContext serviceContext ) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Thread getThread() {
      return thread;
    }

    @Override
    public Object getLock() {
      throw new UnsupportedOperationException();
    }
  }

  private static class SimpleSessionShutdownAdapter implements ISessionShutdownAdapter {
    private final ApplicationContextImpl applicationContext;
    private Runnable shutdownCallback;
    private UISession uiSession;

    SimpleSessionShutdownAdapter( ApplicationContextImpl applicationContext ) {
      this.applicationContext = applicationContext;
    }

    @Override
    public void setShutdownCallback( Runnable shutdownCallback ) {
      this.shutdownCallback = shutdownCallback;
    }

    @Override
    public void setUISession( UISession uiSession ) {
      this.uiSession = uiSession;
    }

    @Override
    public void interceptShutdown() {
      final Display display = LifeCycleUtil.getSessionDisplay( uiSession );
      ContextUtil.runNonUIThreadWithFakeContext( uiSession, new Runnable() {
        @Override
        public void run() {
          if( isDisplayActive( display ) && isApplicationContextActive() ) {
            attachThread( display, uiSession );
            CurrentPhase.set( PhaseId.PROCESS_ACTION );
            display.dispose();
          }
          shutdownCallback.run();
        }
      } );
    }

    @Override
    public void processShutdown() {
      throw new UnsupportedOperationException();
    }

    private static boolean isDisplayActive( Display display ) {
      return display != null && !display.isDisposed();
    }

    private boolean isApplicationContextActive() {
      return applicationContext != null && applicationContext.isActive();
    }
  }

}
