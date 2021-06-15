/*******************************************************************************
 * Copyright (c) 2009, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 *    Ralf Zahn (ARS) - browser history support (Bug 283291)
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.client;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.rap.rwt.client.service.BrowserNavigationEvent;
import org.eclipse.rap.rwt.client.service.BrowserNavigationListener;
import org.eclipse.rap.rwt.internal.remote.ConnectionImpl;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;


public final class BrowserNavigationImpl implements BrowserNavigation {

  private final static String TYPE = "rwt.client.BrowserNavigation";
  private final static String PROP_NAVIGATION_LISTENER = "Navigation";
  private final static String METHOD_ADD_TO_HISTORY = "addToHistory";
  private final static String PROP_STATE = "state";
  private final static String PROP_TITLE = "title";
  private static final Object EVENT_HISTORY_NAVIGATED = "Navigation";
  private static final String EVENT_HISTORY_NAVIGATED_STATE = "state";

  private final Collection<BrowserNavigationListener> listeners;
  private final RemoteObject remoteObject;

  public BrowserNavigationImpl() {
    ConnectionImpl connection = ( ConnectionImpl )RWT.getUISession().getConnection();
    remoteObject = connection.createServiceObject( TYPE );
    remoteObject.setHandler( new NavigationOperationHandler() );
    listeners = new LinkedHashSet<>();
  }

  @Override
  public void pushState( String state, String title ) {
    ParamCheck.notNullOrEmpty( state, "state" );
    JsonObject properties = new JsonObject().add( PROP_STATE, state ).add( PROP_TITLE, title );
    remoteObject.call( METHOD_ADD_TO_HISTORY, properties );
  }

  @Override
  public void addBrowserNavigationListener( BrowserNavigationListener listener ) {
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    listeners.add( listener );
    if( listeners.size() == 1 ) {
      remoteObject.listen( PROP_NAVIGATION_LISTENER, true );
    }
  }

  @Override
  public void removeBrowserNavigationListener( BrowserNavigationListener listener ) {
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    listeners.remove( listener );
    if( listeners.isEmpty() ) {
      remoteObject.listen( PROP_NAVIGATION_LISTENER, false );
    }
  }

  void notifyListeners( BrowserNavigationEvent event ) {
    BrowserNavigationListener[] listeners = getListeners();
    for( BrowserNavigationListener listener : listeners ) {
      listener.navigated( event );
    }
  }

  private BrowserNavigationListener[] getListeners() {
    return listeners.toArray( new BrowserNavigationListener[ listeners.size() ] );
  }

  ////////////////
  // Inner classes

  private final class NavigationOperationHandler extends AbstractOperationHandler {
    @Override
    public void handleNotify( String event, JsonObject properties ) {
      if( EVENT_HISTORY_NAVIGATED.equals( event ) ) {
        String state = properties.get( EVENT_HISTORY_NAVIGATED_STATE ).asString();
        notifyListeners( new BrowserNavigationEvent( this, state ) );
      }
    }

  }

}
