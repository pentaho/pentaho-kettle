/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.lifecycle.IPhase.IInterruptible;
import org.eclipse.rap.rwt.internal.lifecycle.UIThread.UIThreadTerminatedError;
import org.eclipse.rap.rwt.internal.serverpush.ServerPushManager;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;
import org.eclipse.rap.rwt.internal.service.ServiceStore;
import org.eclipse.rap.rwt.internal.service.UISessionImpl;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.swt.widgets.Display;


@SuppressWarnings( "deprecation" )
public class RWTLifeCycle extends LifeCycle {

  private static final Integer ZERO = Integer.valueOf( 0 );

  private static final String CURRENT_PHASE = RWTLifeCycle.class.getName() + ".currentPhase";
  private static final String PHASE_ORDER = RWTLifeCycle.class.getName() + ".phaseOrder";
  private static final String UI_THREAD_WAITING_FOR_TERMINATION
    = UIThreadController.class.getName() + "#UIThreadWaitingForTermination";
  private static final String UI_THREAD_THROWABLE
    = UIThreadController.class.getName() + "#UIThreadThrowable";
  private static final String REQUEST_THREAD_RUNNABLE
    = RWTLifeCycle.class.getName() + "#requestThreadRunnable";

  private static final IPhase[] PHASE_ORDER_STARTUP = {
    new IInterruptible() {
      @Override
      public PhaseId execute( Display display ) throws IOException {
        return null;
      }
      @Override
      public PhaseId getPhaseId() {
        return PhaseId.PREPARE_UI_ROOT;
      }
    },
    new Render()
  };

  private static final IPhase[] PHASE_ORDER_SUBSEQUENT = {
    new IPhase() {
      @Override
      public PhaseId execute( Display display ) throws IOException {
        return null;
      }
      @Override
      public PhaseId getPhaseId() {
        return PhaseId.PREPARE_UI_ROOT;
      }
    },
    new ReadData(),
    new IInterruptible() {
      @Override
      public PhaseId execute( Display display ) throws IOException {
        new ProcessAction().execute( display );
        return null;
      }
      @Override
      public PhaseId getPhaseId() {
        return PhaseId.PROCESS_ACTION;
      }
    },
    new Render()
  };

  Runnable uiRunnable;

  public RWTLifeCycle( ApplicationContextImpl applicationContext ) {
    super( applicationContext );
    uiRunnable = new UIThreadController();
  }

  @Override
  public void execute() throws IOException {
    if( LifeCycleUtil.isStartup() ) {
      setPhaseOrder( PHASE_ORDER_STARTUP );
    } else {
      setPhaseOrder( PHASE_ORDER_SUBSEQUENT );
    }
    Runnable runnable = null;
    do {
      setRequestThreadRunnable( null );
      executeUIThread();
      runnable = getRequestThreadRunnable();
      if( runnable != null ) {
        runnable.run();
      }
    } while( runnable != null );
  }

  @Override
  public void requestThreadExec( Runnable runnable ) {
    setRequestThreadRunnable( runnable );
    getUIThreadHolder().switchThread();
  }

  private static void setRequestThreadRunnable( Runnable runnable ) {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    serviceStore.setAttribute( REQUEST_THREAD_RUNNABLE, runnable );
  }

  private static Runnable getRequestThreadRunnable() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    return ( Runnable )serviceStore.getAttribute( REQUEST_THREAD_RUNNABLE );
  }


  //////////////////////////
  // readAndDispatch & sleep

  void continueLifeCycle() {
    int start = 0;
    IPhase[] phaseOrder = getPhaseOrder();
    if( phaseOrder != null ) {
      Integer currentPhase = getCurrentPhase();
      if( currentPhase != null ) {
        int phaseIndex = currentPhase.intValue();
        // A non-null currentPhase indicates that an IInterruptible phase was executed before. In
        // this case we now need to execute the AfterPhase events
        phaseListenerManager.notifyAfterPhase( phaseOrder[ phaseIndex ].getPhaseId(), this );
        start = currentPhase.intValue() + 1;
      }
      boolean interrupted = false;
      for( int i = start; !interrupted && i < phaseOrder.length; i++ ) {
        IPhase phase = phaseOrder[ i ];
        CurrentPhase.set( phase.getPhaseId() );
        phaseListenerManager.notifyBeforePhase( phase.getPhaseId(), this );
        if( phase instanceof IInterruptible ) {
          // IInterruptible phases return control to the user code, thus they don't call
          // Phase#execute()
          ServiceStore serviceStore = ContextProvider.getServiceStore();
          serviceStore.setAttribute( CURRENT_PHASE, Integer.valueOf( i ) );
          interrupted = true;
        } else {
          try {
            phase.execute( LifeCycleUtil.getSessionDisplay() );
          } catch( Throwable e ) {
            // Wrap exception in a ThreadDeath-derived error to break out of the application
            // call stack
            throw new PhaseExecutionError( e );
          }
          phaseListenerManager.notifyAfterPhase( phase.getPhaseId(), this );
        }
      }
      if( !interrupted ) {
        ContextProvider.getServiceStore().setAttribute( CURRENT_PHASE, null );
      }
    }
  }

  int createUI() {
    int result = -1;
    if( ZERO.equals( getCurrentPhase() ) && LifeCycleUtil.isStartup() ) {
      EntryPointManager entryPointManager = applicationContext.getEntryPointManager();
      HttpServletRequest request = ContextProvider.getRequest();
      EntryPointRegistration registration = entryPointManager.getEntryPointRegistration( request );
      EntryPoint entryPoint = registration.getFactory().create();
      result = entryPoint.createUI();
    }
    return result;
  }

  void executeUIThread() throws IOException {
    ServiceContext context = ContextProvider.getContext();
    UISession uiSession = ContextProvider.getUISession();
    IUIThreadHolder uiThread = getUIThreadHolder();
    if( uiThread == null ) {
      uiThread = createUIThread();
      // The serviceContext MUST be set before thread.start() is called
      uiThread.setServiceContext( context );
      synchronized( uiThread.getLock() ) {
        uiThread.getThread().start();
        uiThread.switchThread();
      }
    } else {
      uiThread.setServiceContext( context );
      // See bug 354368
      if( !Boolean.TRUE.equals( uiSession.getAttribute( UI_THREAD_WAITING_FOR_TERMINATION ) ) ) {
        uiThread.switchThread();
      }
    }
    // TODO [rh] consider moving this to UIThreadController#run
    if( !uiThread.getThread().isAlive() ) {
      LifeCycleUtil.setUIThread( uiSession, null );
    }
    handleUIThreadException();
  }

  private static void handleUIThreadException() throws IOException {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    Throwable throwable = ( Throwable )serviceStore.getAttribute( UI_THREAD_THROWABLE );
    if( throwable != null ) {
      if( throwable instanceof PhaseExecutionError ) {
        throwable = throwable.getCause();
      }
      if( throwable instanceof IOException ) {
        throw ( IOException )throwable;
      } else if( throwable instanceof RuntimeException ) {
        throw ( RuntimeException )throwable;
      } else if( throwable instanceof Error ) {
        throw ( Error )throwable;
      } else {
        throw new RuntimeException( throwable );
      }
    }
  }

  @Override
  public void sleep() {
    continueLifeCycle();
    IUIThreadHolder uiThread = getUIThreadHolder();
    ServerPushManager.getInstance().notifyUIThreadEnd();
    uiThread.switchThread();
    uiThread.updateServiceContext();
    ServerPushManager.getInstance().notifyUIThreadStart();
    continueLifeCycle();
  }

  private IUIThreadHolder createUIThread() {
    UISession uiSession = ContextProvider.getUISession();
    IUIThreadHolder result = new UIThread( uiRunnable );
    result.getThread().setDaemon( true );
    result.getThread().setName( "UIThread [" + uiSession.getId() + "]" );
    LifeCycleUtil.setUIThread( uiSession, result );
    setShutdownAdapter( ( ISessionShutdownAdapter )result );
    return result;
  }

  private static Integer getCurrentPhase() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    return ( Integer )serviceStore.getAttribute( CURRENT_PHASE );
  }

  private static void setShutdownAdapter( ISessionShutdownAdapter adapter ) {
    UISession uiSession = ContextProvider.getUISession();
    UISessionImpl uiSessionImpl = ( UISessionImpl )uiSession;
    uiSessionImpl.setShutdownAdapter( adapter );
  }

  public void setPhaseOrder( IPhase[] phaseOrder ) {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    serviceStore.setAttribute( PHASE_ORDER, phaseOrder );
  }

  IPhase[] getPhaseOrder() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    return ( IPhase[] )serviceStore.getAttribute( PHASE_ORDER );
  }

  public static IUIThreadHolder getUIThreadHolder() {
    return LifeCycleUtil.getUIThread( ContextProvider.getUISession() );
  }

  private static final class PhaseExecutionError extends ThreadDeath {
    public PhaseExecutionError( Throwable cause ) {
      initCause( cause );
    }
  }

  private final class UIThreadController implements Runnable {
    @Override
    public void run() {
      IUIThreadHolder uiThread = ( IUIThreadHolder )Thread.currentThread();
      try {
        // [rh] sync exception handling and switchThread (see bug 316676)
        synchronized( uiThread.getLock() ) {
          try {
            uiThread.updateServiceContext();
            ServerPushManager.getInstance().notifyUIThreadStart();
            continueLifeCycle();
            createUI();
            continueLifeCycle();
            ServerPushManager.getInstance().notifyUIThreadEnd();
          } catch( UIThreadTerminatedError thr ) {
            throw thr;
          } catch( Throwable thr ) {
            ServiceStore serviceStore = ContextProvider.getServiceStore();
            serviceStore.setAttribute( UI_THREAD_THROWABLE, thr );
          }
          // We have to prevent the ui thread from waking up at that point, otherwise
          // processShutdown would never be executed and session store would not be cleared.
          // See bug 354368
          UISession uiSession = ContextProvider.getUISession();
          uiSession.setAttribute( UI_THREAD_WAITING_FOR_TERMINATION, Boolean.TRUE );
          // In any case: wait for the thread to be terminated by session timeout
          uiThread.switchThread();
        }
      } catch( @SuppressWarnings( "unused" ) UIThreadTerminatedError e ) {
        // If we get here, the session is being invalidated, see UIThread#terminateThread()
        ( ( ISessionShutdownAdapter )uiThread ).processShutdown();
      }
    }
  }

}
