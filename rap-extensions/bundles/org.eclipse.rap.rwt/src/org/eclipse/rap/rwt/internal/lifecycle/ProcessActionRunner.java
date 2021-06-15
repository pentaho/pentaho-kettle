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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceStore;


@SuppressWarnings( "deprecation" )
public class ProcessActionRunner {

  private static final String ATTR_RUNNABLE_LIST = ProcessActionRunner.class.getName();

  @SuppressWarnings("unchecked")
  public static void add( Runnable runnable ) {
    PhaseId phaseId = CurrentPhase.get();
    if( PhaseId.PREPARE_UI_ROOT.equals( phaseId ) || PhaseId.PROCESS_ACTION.equals( phaseId ) ) {
      runnable.run();
    } else {
      ServiceStore serviceStore = ContextProvider.getServiceStore();
      List<Runnable> list = ( List<Runnable> )serviceStore.getAttribute( ATTR_RUNNABLE_LIST );
      if( list == null ) {
        list = new ArrayList<>();
        serviceStore.setAttribute( ATTR_RUNNABLE_LIST, list );
      }
      if( !list.contains( runnable ) ) {
        list.add( runnable );
      }
    }
  }

  @SuppressWarnings( "unchecked" )
  public static boolean executeNext() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    List<Runnable> list = ( List<Runnable> )serviceStore.getAttribute( ATTR_RUNNABLE_LIST );
    if( list != null && list.size() > 0 ) {
      Runnable runnable = list.remove( 0 );
      runnable.run();
      return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public static void execute() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    List<Runnable> list = ( List<Runnable> )serviceStore.getAttribute( ATTR_RUNNABLE_LIST );
    if( list != null ) {
      for( Runnable runnable : new ArrayList<>( list ) ) {
        // TODO: [fappel] think about exception handling.
        runnable.run();
      }
    }
  }

}
