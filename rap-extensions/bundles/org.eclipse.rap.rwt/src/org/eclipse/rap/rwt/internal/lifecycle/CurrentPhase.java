/*******************************************************************************
 * Copyright (c) 2002, 2014 Innoopract Informationssysteme GmbH and others.
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

import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceStore;


@SuppressWarnings( "deprecation" )
public final class CurrentPhase {

  private static final String ATTR_CURRENT_PHASE = CurrentPhase.class.getName() + "#value";

  private CurrentPhase() {
    // prevent instantiation
  }

  public static PhaseId get() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    if( serviceStore != null ) {
      return ( PhaseId )serviceStore.getAttribute( ATTR_CURRENT_PHASE );
    }
    return null;
  }

  public static void set( PhaseId phaseId ) {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    serviceStore.setAttribute( ATTR_CURRENT_PHASE, phaseId );
  }

}
