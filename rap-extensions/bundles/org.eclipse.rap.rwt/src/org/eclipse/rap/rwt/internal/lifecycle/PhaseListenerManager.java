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

import static org.eclipse.rap.rwt.internal.service.ContextProvider.getApplicationContext;
import static org.eclipse.rap.rwt.internal.service.ContextProvider.getUISession;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.rap.rwt.internal.service.ServletLog;
import org.eclipse.rap.rwt.internal.util.ParamCheck;


@SuppressWarnings( "deprecation" )
public class PhaseListenerManager {

  private final Object lock;
  private final Set<PhaseListener> phaseListeners;

  public PhaseListenerManager() {
    lock = new Object();
    phaseListeners = new HashSet<>();
  }

  public void addPhaseListener( PhaseListener phaseListener ) {
    ParamCheck.notNull( phaseListener, "phaseListener" );
    synchronized( lock ) {
      phaseListeners.add( phaseListener );
    }
  }

  public void removePhaseListener( PhaseListener phaseListener ) {
    ParamCheck.notNull( phaseListener, "phaseListener" );
    synchronized( lock ) {
      phaseListeners.remove( phaseListener );
    }
  }

  public PhaseListener[] getPhaseListeners() {
    synchronized( lock ) {
      return phaseListeners.toArray( new PhaseListener[ 0 ] );
    }
  }

  void notifyBeforePhase( PhaseId phase, LifeCycle eventSource ) {
    if( PhaseId.PROCESS_ACTION.equals( phase ) ) {
      getApplicationContext().notifyEnterUIThread( getUISession() );
    }
    PhaseListener[] phaseListeners = getPhaseListeners();
    PhaseEvent event = new PhaseEvent( eventSource, phase );
    for( int i = 0; i < phaseListeners.length; i++ ) {
      PhaseListener phaseListener = phaseListeners[ i ];
      if( mustNotify( phase, phaseListener.getPhaseId() ) ) {
        try {
          phaseListener.beforePhase( event );
        } catch( Exception exception ) {
          logBeforePhaseException( phase, exception );
        }
      }
    }
  }

  void notifyAfterPhase( PhaseId phase, LifeCycle eventSource ) {
    if( PhaseId.PROCESS_ACTION.equals( phase ) ) {
      getApplicationContext().notifyLeaveUIThread( getUISession() );
    }
    PhaseListener[] phaseListeners = getPhaseListeners();
    PhaseEvent event = new PhaseEvent( eventSource, phase );
    for( int i = 0; i < phaseListeners.length; i++ ) {
      PhaseListener phaseListener = phaseListeners[ i ];
      if( mustNotify( phase, phaseListener.getPhaseId() ) ) {
        try {
          phaseListener.afterPhase( event );
        } catch( Exception exception ) {
          logAfterPhaseException( phase, exception );
        }
      }
    }
  }

  public void clear() {
    phaseListeners.clear();
  }

  private static boolean mustNotify( PhaseId phase, PhaseId listenerPhase ) {
    return listenerPhase == PhaseId.ANY || listenerPhase == phase;
  }

  private static void logBeforePhaseException( PhaseId phase, Exception exception ) {
    String text = "Failed to execute PhaseListener before phase ''{0}''.";
    String msg = MessageFormat.format( text, new Object[] { phase } );
    ServletLog.log( msg, exception );
  }

  private static void logAfterPhaseException( PhaseId phase, Exception exception ) {
    String text = "Failed to execute PhaseListener after phase ''{0}''.";
    String msg = MessageFormat.format( text, new Object[] { phase } );
    ServletLog.log( msg, exception );
  }

}
