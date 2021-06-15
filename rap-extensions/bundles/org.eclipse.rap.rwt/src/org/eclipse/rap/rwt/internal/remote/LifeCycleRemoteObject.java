/*******************************************************************************
* Copyright (c) 2011, 2015 EclipseSource and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    EclipseSource - initial API and implementation
*******************************************************************************/
package org.eclipse.rap.rwt.internal.remote;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ProtocolMessageWriter;
import org.eclipse.rap.rwt.internal.service.ContextProvider;


/**
 * A remote object implementation that writes directly to the message writer. Used for widgets and
 * other objects that are rendered by LCAs.
 */
public class LifeCycleRemoteObject extends RemoteObjectImpl {

  public LifeCycleRemoteObject( String id, String type ) {
    super( id );
    if( type != null ) {
      getWriter().appendCreate( id, type );
    }
  }

  @Override
  public void set( String name, int value ) {
    super.set( name, value );
    getWriter().appendSet( getId(), name, value );
  }

  @Override
  public void set( String name, double value ) {
    super.set( name, value );
    getWriter().appendSet( getId(), name, value );
  }

  @Override
  public void set( String name, boolean value ) {
    super.set( name, value );
    getWriter().appendSet( getId(), name, value );
  }

  @Override
  public void set( String name, String value ) {
    super.set( name, value );
    getWriter().appendSet( getId(), name, value );
  }

  @Override
  public void set( String name, JsonValue value ) {
    super.set( name, value );
    getWriter().appendSet( getId(), name, value );
  }

  @Override
  public void listen( String eventType, boolean listen ) {
    super.listen( eventType, listen );
    getWriter().appendListen( getId(), eventType, listen );
  }

  @Override
  public void call( String method, JsonObject parameters ) {
    super.call( method, parameters );
    getWriter().appendCall( getId(), method, parameters );
  }

  @Override
  public void destroy() {
    super.destroy();
    getWriter().appendDestroy( getId() );
  }

  private static ProtocolMessageWriter getWriter() {
    return ContextProvider.getProtocolWriter();
  }

}
