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
package org.eclipse.rap.rwt.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.eclipse.rap.rwt.internal.service.ServletLog;
import org.eclipse.rap.rwt.internal.util.ParamCheck;


/**
 * A setting store implementation that persists all settings on the file system using Java
 * {@link Properties} files.
 *
 * @since 2.0
 */
public final class FileSettingStore implements SettingStore {

  /**
   * This key (value "org.eclipse.rap.rwt.service.FileSettingStore.dir") can be used to configure
   * the working directory for file settings stores. See {@link FileSettingStoreFactory}.
   */
  public static final String FILE_SETTING_STORE_DIR
    = "org.eclipse.rap.rwt.service.FileSettingStore.dir";

  private final File workDir;
  private final Properties props;
  private final Set<SettingStoreListener> listeners;
  private String id;

  /**
   * Creates an empty instance with a random unique ID. Use {@link #loadById(String)} to initialize
   * an existing store with previously persisted attributes.
   *
   * @param baseDirectory an existing directory to persist this store's settings in
   * @throws IllegalArgumentException if the given <code>workDir</code> is not a directory
   * @see #loadById(String)
   */
  public FileSettingStore( File baseDirectory ) {
    ParamCheck.notNull( baseDirectory, "baseDirectory" );
    checkWorkDir( baseDirectory );
    workDir = baseDirectory;
    props = new Properties();
    listeners = new HashSet<>();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public synchronized String getAttribute( String name ) {
    ParamCheck.notNull( name, "name" );
    return props.getProperty( name );
  }

  @Override
  public synchronized void setAttribute( String name, String value ) throws IOException {
    ParamCheck.notNull( name, "name" );
    if( value == null ) {
      removeAttribute( name );
    } else {
      String oldValue = ( String )props.setProperty( name, value );
      if( !value.equals( oldValue ) ) {
        notifyListeners( name, oldValue, value );
        persist();
      }
    }
  }

  @Override
  public synchronized Enumeration<String> getAttributeNames() {
    final Enumeration<Object> keys = props.keys();
    return new Enumeration<String>() {
      @Override
      public boolean hasMoreElements() {
        return keys.hasMoreElements();
      }
      @Override
      public String nextElement() {
        return ( String )keys.nextElement();
      }
    };
  }

  @Override
  public synchronized void loadById( String id ) throws IOException {
    ParamCheck.notNullOrEmpty( id, "id" );
    this.id = id;
    notifyForEachAttribute( true );
    props.clear();
    BufferedInputStream inputStream = getInputStream( id );
    if( inputStream != null ) {
      try {
        props.load( inputStream );
        notifyForEachAttribute( false );
      } finally {
        inputStream.close();
      }
    }
  }

  @Override
  public synchronized void removeAttribute( String name ) throws IOException {
    String oldValue = ( String )props.remove( name );
    if( oldValue != null ) {
      notifyListeners( name, oldValue, null );
      persist();
    }
  }

  @Override
  public synchronized void addSettingStoreListener( SettingStoreListener listener ) {
    ParamCheck.notNull( listener, "listener" );
    listeners.add( listener );
  }

  @Override
  public synchronized void removeSettingStoreListener( SettingStoreListener listener ) {
    ParamCheck.notNull( listener, "listener" );
    listeners.remove( listener );
  }

  //////////////////
  // helping methods

  /**
   * @return a BufferedInputStream or <code>null</code> if this file does not exist
   */
  private BufferedInputStream getInputStream( String streamId ) {
    BufferedInputStream result = null;
    File file = getStoreFile( streamId );
    if( file.exists() ) {
      try {
        result = new BufferedInputStream( new FileInputStream( file ) );
      } catch( FileNotFoundException fnf ) {
        log( "Should not happen", fnf );
      }
    }
    return result;
  }

  private BufferedOutputStream getOutputStream( String streamId )
  throws FileNotFoundException {
    File file = getStoreFile( streamId );
    return new BufferedOutputStream( new FileOutputStream( file ) );
  }

  private File getStoreFile( String fileName ) {
    return new File( workDir, fileName );
  }

  private static void log( String msg, Throwable throwable ) {
    ServletLog.log( msg, throwable );
  }

  private synchronized void notifyForEachAttribute( boolean removed ) {
    Enumeration<Object> attributes = props.keys();
    while( attributes.hasMoreElements() ) {
      String attribute = ( String )attributes.nextElement();
      String value = props.getProperty( attribute );
      if( removed ) {
        notifyListeners( attribute, value, null );
      } else {
        notifyListeners( attribute, null, value );
      }
    }
  }

  private synchronized void notifyListeners( String attribute, String oldValue, String newValue ) {
    SettingStoreEvent event = new SettingStoreEvent( this, attribute, oldValue, newValue );
    // TODO [rh] create a snapshot of listeners before invoking (possible concurrent modification)
    for( SettingStoreListener listener : listeners ) {
      try {
        listener.settingChanged( event );
      } catch( Exception exc ) {
        String msg = "Exception when invoking listener " + listener.getClass().getName();
        log( msg, exc );
      } catch( LinkageError le ) {
        String msg = "Linkage error when invoking listener " + listener.getClass().getName();
        log( msg, le );
      }
    }
  }

  private void persist() throws IOException {
    BufferedOutputStream outputStream = getOutputStream( id );
    try {
      props.store( outputStream, FileSettingStore.class.getName() );
    } finally {
      outputStream.close();
    }
  }

  private static void checkWorkDir( File workDir ) {
    if( !workDir.isDirectory() ) {
      throw new IllegalArgumentException( "workDir is not a directory: " + workDir );
    }
  }

}
