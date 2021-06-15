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

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.ProtocolUtil.handleOperation;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;

import java.io.IOException;
import java.util.List;

import org.eclipse.rap.rwt.internal.protocol.ClientMessage;
import org.eclipse.rap.rwt.internal.protocol.Operation;
import org.eclipse.rap.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectRegistry;
import org.eclipse.rap.rwt.remote.OperationHandler;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.internal.widgets.WidgetRemoteAdapter;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;


public abstract class WidgetLCA<T extends Widget>  {

  public void render( T widget ) throws IOException {
    WidgetRemoteAdapter adapter = ( WidgetRemoteAdapter )WidgetUtil.getAdapter( widget );
    if( !adapter.isInitialized() ) {
      renderInitialization( widget );
    }
    renderChanges( widget );
    adapter.setInitialized( true );
  }

  public void readData( T widget ) {
    ClientMessage clientMessage = ProtocolUtil.getClientMessage();
    String id = getId( widget );
    List<Operation> operations = clientMessage.getAllOperationsFor( id );
    if( !operations.isEmpty() ) {
      OperationHandler handler = getOperationHandler( id );
      for( Operation operation : operations ) {
        handleOperation( handler, operation );
      }
    }
  }

  public abstract void preserveValues( T widget );

  public abstract void renderInitialization( T widget ) throws IOException;

  public abstract void renderChanges( T widget ) throws IOException;

  @SuppressWarnings( "unused" )
  public void renderDispose( T widget ) throws IOException {
    RemoteAdapter adapter = WidgetUtil.getAdapter( widget );
    RemoteObject remoteObject = getRemoteObject( widget );
    if( adapter.getParent() == null || !adapter.getParent().isDisposed() ) {
      remoteObject.destroy();
    } else {
      ( ( RemoteObjectImpl )remoteObject ).markDestroyed();
    }
  }

  @SuppressWarnings( "unused" )
  public void doRedrawFake( Control control ) {
  }

  private static OperationHandler getOperationHandler( String id ) {
    RemoteObjectImpl remoteObject = RemoteObjectRegistry.getInstance().get( id );
    if( remoteObject == null ) {
      throw new IllegalStateException( "No remote object found for widget: " + id );
    }
    OperationHandler handler = remoteObject.getHandler();
    if( handler == null ) {
      throw new IllegalStateException( "No operation handler found for widget: " + id );
    }
    return handler;
  }

}
