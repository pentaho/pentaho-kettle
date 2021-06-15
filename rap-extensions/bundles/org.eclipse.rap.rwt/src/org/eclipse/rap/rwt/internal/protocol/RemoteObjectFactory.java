/*******************************************************************************
* Copyright (c) 2011, 2013 EclipseSource and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    EclipseSource - initial API and implementation
*******************************************************************************/
package org.eclipse.rap.rwt.internal.protocol;

import org.eclipse.rap.rwt.internal.lifecycle.DisplayUtil;
import org.eclipse.rap.rwt.internal.remote.LifeCycleRemoteObject;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectRegistry;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;


/**
 * The methods of this class are used to create remote objects, only for use in LCAs.
 *
 * @see RemoteObject
 */
public final class RemoteObjectFactory {

  public static RemoteObject getRemoteObject( Widget widget ) {
    ParamCheck.notNull( widget, "widget" );
    return getForId( WidgetUtil.getId( widget ) );
  }

  public static RemoteObject getRemoteObject( Display display ) {
    ParamCheck.notNull( display, "display" );
    return getForId( DisplayUtil.getId( display ) );
  }

  public static RemoteObject getRemoteObject( String id ) {
    ParamCheck.notNull( id, "id" );
    return getForId( id );
  }

  public static RemoteObject createRemoteObject( Widget widget, String type ) {
    ParamCheck.notNull( widget, "widget" );
    ParamCheck.notNull( type, "type" );
    return createForId( WidgetUtil.getId( widget ), type );
  }

  public static RemoteObject createRemoteObject( String id, String type ) {
    ParamCheck.notNull( id, "id" );
    ParamCheck.notNull( type, "type" );
    return createForId( id, type );
  }

  private static RemoteObject createForId( String id, String type ) {
    LifeCycleRemoteObject remoteObject = new LifeCycleRemoteObject( id, type );
    RemoteObjectRegistry.getInstance().register( remoteObject );
    return remoteObject;
  }

  private static RemoteObject getForId( String id ) {
    RemoteObjectImpl remoteObject = RemoteObjectRegistry.getInstance().get( id );
    // TODO [rst] Required for LCA tests, remove lazy initialization
    if( remoteObject == null ) {
      remoteObject = new LifeCycleRemoteObject( id, null );
      RemoteObjectRegistry.getInstance().register( remoteObject );
    }
    return remoteObject;
  }

  private RemoteObjectFactory() {
    // prevent instantiation
  }

}
