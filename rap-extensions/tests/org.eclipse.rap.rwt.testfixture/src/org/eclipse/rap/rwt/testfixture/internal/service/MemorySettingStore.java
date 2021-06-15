/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.testfixture.internal.service;

import java.util.*;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.service.*;


/**
 * This {@link SettingStore} implementation "persists" all settings
 * in memory, for as long the application is running.
 * <p>
 * <b>This strategy results in an ever increasing memory
 * consumption over time</b>. We do <b>*not*</b> recommend using this
 * implementation in a production environment.
 */
public final class MemorySettingStore implements SettingStore {

  private static final Map<String,String> VALUES = new HashMap<String,String>();
  private static final Set<SettingStoreListener> LISTENERS = new HashSet<SettingStoreListener>();

  private String id;

  /**
   * Create a {@link MemorySettingStore} instance and containing the
   * attributes persisted under the given <code>id</code>.
   *
   * @param id a non-null; non-empty; non-whitespace-only String
   * @throws NullPointerException if id is <code>null</null>
   * @throws IllegalArgumentException if id is empty or composed
   *         entirely of whitespace
   */
  public MemorySettingStore( String id ) {
    ParamCheck.notNullOrEmpty( id, "id" );
    this.id = id;
  }


  ////////////////////////
  // SettingStore methods

  public String getId() {
    return id;
  }

  public synchronized void loadById( String id ) {
    ParamCheck.notNullOrEmpty( id, "id" );
    fakeRemoval();
    this.id = id;
    loadAttributes();
  }

  public synchronized String getAttribute( String name ) {
    ParamCheck.notNull( name, "name" );
    String key = id + name;
    return VALUES.get( key );
  }

  public synchronized Enumeration<String> getAttributeNames() {
    List<String> result = new ArrayList<String>();
    Iterator<String> iterator = VALUES.keySet().iterator();
    int nameBeginIndex = id.length();
    while( iterator.hasNext() ) {
      String key = iterator.next();
      if( key.startsWith( id ) ) {
        result.add( key.substring( nameBeginIndex ) );
      }
    }
    final Iterator<String> resultIterator = result.iterator();
    return new Enumeration<String>() {
      public boolean hasMoreElements() {
        return resultIterator.hasNext();
      }
      public String nextElement() {
        return resultIterator.next();
      }
    };
  }

  public synchronized void removeAttribute( String name ) {
    ParamCheck.notNull( name, "name" );
    String key = id + name;
    String oldValue = VALUES.remove( key );
    if( oldValue != null ) {
      notifyListeners( name, oldValue, null );
    }
  }

  public synchronized void setAttribute( String name, String value )
  {
    ParamCheck.notNull( name, "name" );
    if( value == null ) {
      removeAttribute( name );
    } else {
      ParamCheck.notNull( value, "value" );
      String key = id + name;
      String oldValue = VALUES.put( key, value );
      if( !value.equals( oldValue ) ) {
        notifyListeners( name, oldValue, value );
      }
    }
  }

  public synchronized void addSettingStoreListener( SettingStoreListener listener )
  {
    ParamCheck.notNull( listener, "listener" );
    LISTENERS.add( listener );
  }

  public synchronized void removeSettingStoreListener( SettingStoreListener listener )
  {
    ParamCheck.notNull( listener, "listener" );
    LISTENERS.remove( listener );
  }


  //////////////////
  // helping methods

  private void fakeRemoval() {
    Enumeration attributes = getAttributeNames();
    while( attributes.hasMoreElements() ) {
      String name = ( String )attributes.nextElement();
      String key = id + name;
      String value = VALUES.get( key );
      notifyListeners( name, value, null );
    }
  }

  private synchronized void loadAttributes() {
    Enumeration attributes = getAttributeNames();
    while( attributes.hasMoreElements() ) {
      String name = ( String )attributes.nextElement();
      String key = id + name;
      String value = VALUES.get( key );
      notifyListeners( name, null, value );
    }
  }

  private void log( String msg, Throwable throwable ) {
    RWT.getRequest().getSession().getServletContext().log( msg, throwable );
  }

  private synchronized void notifyListeners( String attribute, String oldValue, String newValue ) {
    SettingStoreEvent event
      = new SettingStoreEvent( this, attribute, oldValue, newValue );
    Iterator iter = LISTENERS.iterator();
    while( iter.hasNext() ) {
      SettingStoreListener listener = ( SettingStoreListener )iter.next();
      try {
        listener.settingChanged( event );
      } catch( Exception exc ) {
        String msg =   "Exception when invoking listener "
                     + listener.getClass().getName();
        log( msg, exc );
      } catch( LinkageError le ) {
        String msg =   "Linkage error when invoking listener "
                     + listener.getClass().getName();
        log( msg, le );
      }
    }
  }
}
