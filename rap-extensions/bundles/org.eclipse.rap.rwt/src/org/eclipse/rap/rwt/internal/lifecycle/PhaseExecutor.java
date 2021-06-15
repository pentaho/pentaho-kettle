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

import org.eclipse.swt.widgets.Display;


@SuppressWarnings( "deprecation" )
abstract class PhaseExecutor {

  private final PhaseListenerManager phaseListenerManager;
  private final IPhase[] phases;
  private final LifeCycle lifecycle;

  PhaseExecutor( PhaseListenerManager phaseListenerManager, IPhase[] phases, LifeCycle lifecycle )
  {
    this.phaseListenerManager = phaseListenerManager;
    this.phases = phases;
    this.lifecycle = lifecycle;
  }

  final void execute( PhaseId startPhaseId ) throws IOException {
    PhaseId currentPhaseId = startPhaseId;
    while( currentPhaseId != null ) {
      IPhase currentPhase = findPhase( currentPhaseId );
      CurrentPhase.set( currentPhaseId );
      phaseListenerManager.notifyBeforePhase( currentPhaseId, lifecycle );
      PhaseId nextPhaseId = currentPhase.execute( getDisplay() );
      phaseListenerManager.notifyAfterPhase( currentPhaseId, lifecycle );
      currentPhaseId = nextPhaseId;
    }
  }

  abstract Display getDisplay();

  private IPhase findPhase( PhaseId phaseId ) {
    IPhase result = null;
    for( int i = 0; result == null && i < phases.length; i++ ) {
      if( phases[ i ].getPhaseId().equals( phaseId ) ) {
        result = phases[ i ];
      }
    }
    return result;
  }

}
