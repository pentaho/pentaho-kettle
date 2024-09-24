/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.i18n;

import org.pentaho.di.laf.LAFChangeListener;
import org.pentaho.di.laf.LAFFactory;

/**
 * BaseMessage is called by all Message classes to enable the delegation of message delivery, by key to be delegated to
 * the appropriately authoritative supplier as registered in the LAFFactory enabling both i18n as well as pluggable look
 * and feel (LAF)
 *
 * @author dhushon
 *
 */
public class BaseMessages implements LAFChangeListener<MessageHandler> {
  static BaseMessages instance = null;
  protected MessageHandler handler = null;
  Class<MessageHandler> clazz = MessageHandler.class;

  static {
    getInstance();
  }

  private BaseMessages() {
    init();
  }

  private void init() {
    // counting on LAFFactory to return a class conforming to @see MessageHandler
    handler = LAFFactory.getHandler( clazz );
  }

  public static BaseMessages getInstance() {
    if ( instance == null ) {
      instance = new BaseMessages();
    }
    return instance;
  }

  protected MessageHandler getHandler() {
    return handler;
  }

  protected static MessageHandler getInstanceHandler() {
    return getInstance().getHandler();
  }

  public static String getString( String key ) {
    return getInstanceHandler().getString( key );
  }

  public static String getString( String packageName, String key ) {
    return getInstanceHandler().getString( packageName, key, new String[] {} );
  }

  public static String getString( String packageName, String key, String... parameters ) {
    return getInstanceHandler().getString( packageName, key, parameters );
  }

  public static String getString( String packageName, String key, Class<?> resourceClass, String... parameters ) {
    return getInstanceHandler().getString( packageName, key, resourceClass, parameters );
  }

  public static String getString( Class<?> packageClass, String key, String... parameters ) {
    return getInstanceHandler().getString( packageClass.getPackage().getName(), key, packageClass, parameters );
  }

  public static String getString( Class<?> packageClass, String key, Class<?> resourceClass, String... parameters ) {
    return getInstanceHandler().getString( packageClass.getPackage().getName(), key, packageClass, parameters );
  }

  public static String getString( Class<?> packageClass, String key, Object... parameters ) {
    String[] strings = new String[parameters.length];
    for ( int i = 0; i < strings.length; i++ ) {
      strings[i] = parameters[i] != null ? parameters[i].toString() : "";
    }
    return getString( packageClass, key, strings );
  }

  @Override
  public void notify( MessageHandler changedObject ) {
    handler = changedObject;
  }
}
