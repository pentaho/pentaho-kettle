/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.laf;

import java.util.HashSet;
import java.util.Iterator;

/**
 * A factory delegate for a specific kind of LAFHandler
 *
 * @author dhushon
 *
 * @param <E>
 */
public class LAFDelegate<E extends Handler> {

  E handler;
  Class<E> handlerClass = null;
  Class<E> defaultHandlerClass = null;

  // Set of Listeners for a concrete handler - intended use... getListeners for a given class
  private HashSet<LAFChangeListener<E>> registry = new HashSet<LAFChangeListener<E>>();

  /**
   *
   */
  public LAFDelegate( Class<E> handler, Class<E> defaultHandler ) {
    handlerClass = handler;
    this.defaultHandlerClass = defaultHandler;
    // TODO: Remove this... needed because spoon hasn't yet been init'ed, fulfilling static initializers...
    init();

  }

  private void init() {
    if ( handler != null ) {
      handler = loadHandler( handlerClass );
    } else {
      handler = loadHandler( defaultHandlerClass );
    }
  }

  /**
   * load a concrete Handler for a given Interface (by String classname) if the class is not instantiable, will fallback
   * to default, and then fallback to an abstract implementation. Will always return non-null.
   *
   * @param classname
   * @return
   */
  @SuppressWarnings( "unchecked" )
  public E newHandlerInstance( String classname ) throws ClassNotFoundException {
    E h = null;
    Class<E> c = null;
    c = (Class<E>) Class.forName( classname );
    h = loadHandler( c );
    return h;
  }

  private E loadHandler( Class<E> c ) {
    E h = null;
    try {
      if ( handlerClass.isAssignableFrom( c ) ) {
        h = c.newInstance();
      }
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    if ( h != null ) {
      changeHandler( h );
    }
    return h;
  }

  public E registerChangeListener( LAFChangeListener<E> listener ) {
    // see if a handler has been instantiated for the requested Interface
    registry.add( listener );
    return handler;
  }

  /**
   * unregister a @see LAFChangeListener from the Map which will prevent notification on @see Handler change
   *
   * @param listener
   */
  public void unregisterChangeListener( LAFChangeListener<E> listener ) {
    registry.remove( listener );
  }

  public HashSet<LAFChangeListener<E>> getListeners() {
    return registry;
  }

  public void loadListeners( HashSet<LAFChangeListener<E>> listeners ) {
    registry = listeners;
  }

  public void changeHandler( E handler ) {
    this.handler = handler;
    notifyListeners();
  }

  protected void notifyListeners() {
    Iterator<LAFChangeListener<E>> iterator = registry.iterator();
    while ( iterator.hasNext() ) {
      iterator.next().notify( handler );
    }
  }

  public E getHandler() {
    return handler;
  }

}
