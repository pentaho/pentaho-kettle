/*******************************************************************************
 * Copyright (c) 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.lifecycle;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceStore;
import org.eclipse.swt.widgets.Control;


public final class ReparentedControls {

  private static final String REPARENT_LIST = ReparentedControls.class.getName() + "#reparentList";

  @SuppressWarnings("unchecked")
  public static void add( Control control ) {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    List<Control> reparentList = ( List<Control> )serviceStore.getAttribute( REPARENT_LIST );
    if( reparentList == null ) {
      reparentList = new LinkedList<>();
      serviceStore.setAttribute( REPARENT_LIST, reparentList );
    }
    if( !reparentList.contains( control ) ) {
      reparentList.add( control );
    }
  }

  @SuppressWarnings("unchecked")
  public static List<Control> getAll() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    List<Control> reparentList = ( List<Control> )serviceStore.getAttribute( REPARENT_LIST );
    return reparentList == null ? Collections.EMPTY_LIST : reparentList;
  }

  private ReparentedControls() {
    // prevent instantiation
  }

}
