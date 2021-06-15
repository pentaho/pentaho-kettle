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

import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;


@SuppressWarnings( "deprecation" )
public abstract class LifeCycle {

  protected final ApplicationContextImpl applicationContext;
  protected final PhaseListenerManager phaseListenerManager;

  public LifeCycle( ApplicationContextImpl applicationContext ) {
    this.applicationContext = applicationContext;
    phaseListenerManager = applicationContext.getPhaseListenerManager();
  }

  public abstract void execute() throws IOException;

  public abstract void requestThreadExec( Runnable runnable );

  public void addPhaseListener( PhaseListener listener ) {
    phaseListenerManager.addPhaseListener( listener );
  }

  public void removePhaseListener( PhaseListener listener ) {
    phaseListenerManager.removePhaseListener( listener );
  }

  public abstract void sleep();

}
