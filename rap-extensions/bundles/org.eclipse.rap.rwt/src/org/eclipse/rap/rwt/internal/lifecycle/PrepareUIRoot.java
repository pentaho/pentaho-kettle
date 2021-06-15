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

import javax.servlet.http.HttpServletRequest;

import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.swt.widgets.Display;


@SuppressWarnings( "deprecation" )
final class PrepareUIRoot implements IPhase {

  private final ApplicationContextImpl applicationContext;

  public PrepareUIRoot( ApplicationContextImpl applicationContext ) {
    this.applicationContext = applicationContext;
  }

  @Override
  public PhaseId getPhaseId() {
    return PhaseId.PREPARE_UI_ROOT;
  }

  @Override
  public PhaseId execute( Display display ) {
    PhaseId result;
    if( LifeCycleUtil.isStartup() ) {
      EntryPoint entryPoint = createEntryPoint();
      entryPoint.createUI();
      processPendingMessages();
      result = PhaseId.RENDER;
    } else {
      result = PhaseId.READ_DATA;
    }
    return result;
  }

  private EntryPoint createEntryPoint() {
    EntryPointManager entryPointManager = applicationContext.getEntryPointManager();
    HttpServletRequest request = ContextProvider.getRequest();
    EntryPointRegistration registration = entryPointManager.getEntryPointRegistration( request );
    return registration.getFactory().create();
  }

  private static void processPendingMessages() {
    Display display = Display.getCurrent();
    if( display != null ) {
      while( display.readAndDispatch() ) {
      }
    }
  }

}
