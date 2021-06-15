/*******************************************************************************
 * Copyright (c) 2007, 2015 Innoopract Informationssysteme GmbH and others.
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceStore;
import org.eclipse.swt.widgets.Widget;


// TODO [rh] consider to maintain the list of disposed widget in IDisplayAdapter
public final class DisposedWidgets {

  private static final String DISPOSAL_LIST = DisposedWidgets.class.getName() + "#disposalList";

  @SuppressWarnings("unchecked")
  public static void add( Widget widget ) {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    List<Widget> disposalList = ( List<Widget> )serviceStore.getAttribute( DISPOSAL_LIST );
    if( disposalList == null ) {
      disposalList = new LinkedList<>();
      serviceStore.setAttribute( DISPOSAL_LIST, disposalList );
    }
    disposalList.add( widget );
  }

  @SuppressWarnings("unchecked")
  public static List<Widget> getAll() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    List<Widget> disposalList = ( List<Widget> )serviceStore.getAttribute( DISPOSAL_LIST );
    return disposalList == null ? Collections.EMPTY_LIST : disposalList;
  }

  private DisposedWidgets() {
    // prevent instantiation
  }

}
