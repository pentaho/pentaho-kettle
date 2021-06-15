/*******************************************************************************
 * Copyright (c) 2012, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.remote;

import java.util.List;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.lifecycle.ProcessActionRunner;
import org.eclipse.rap.rwt.internal.protocol.ClientMessage;
import org.eclipse.rap.rwt.internal.protocol.Operation;
import org.eclipse.rap.rwt.internal.protocol.Operation.CallOperation;
import org.eclipse.rap.rwt.internal.protocol.Operation.NotifyOperation;
import org.eclipse.rap.rwt.internal.protocol.Operation.SetOperation;
import org.eclipse.rap.rwt.internal.protocol.ProtocolMessageWriter;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.remote.OperationHandler;


public class RemoteObjectLifeCycleAdapter {

  public static void readData( ClientMessage message ) {
    RemoteObjectRegistry registry = RemoteObjectRegistry.getInstance();
    for( RemoteObjectImpl remoteObject : registry.getRemoteObjects() ) {
      if( remoteObject instanceof DeferredRemoteObject ) {
        dispatchOperations( message, remoteObject );
      }
    }
  }

  public static void render() {
    RemoteObjectRegistry registry = RemoteObjectRegistry.getInstance();
    ProtocolMessageWriter writer = ContextProvider.getProtocolWriter();
    for( RemoteObjectImpl remoteObject : registry.getRemoteObjects() ) {
      if( remoteObject instanceof DeferredRemoteObject ) {
        ( ( DeferredRemoteObject )remoteObject ).render( writer );
      }
      if( remoteObject.isDestroyed() ) {
        RemoteObjectRegistry.getInstance().remove( remoteObject );
      }
    }
  }

  private static void dispatchOperations( ClientMessage message, RemoteObjectImpl remoteObject ) {
    List<Operation> operations = message.getAllOperationsFor( remoteObject.getId() );
    if( !operations.isEmpty() ) {
      OperationHandler handler = getHandler( remoteObject );
      for( Operation operation : operations ) {
        dispatchOperation( handler, operation );
      }
    }
  }

  private static OperationHandler getHandler( RemoteObjectImpl remoteObject ) {
    OperationHandler handler = remoteObject.getHandler();
    if( handler == null ) {
      String message = "No operation handler registered for remote object: "
                       + remoteObject.getId();
      throw new UnsupportedOperationException( message );
    }
    return handler;
  }

  private static void dispatchOperation( OperationHandler handler, Operation operation ) {
    if( operation instanceof SetOperation ) {
      handler.handleSet( ( ( SetOperation )operation ).getProperties() );
    } else if( operation instanceof CallOperation ) {
      CallOperation callOperation = ( CallOperation )operation;
      handler.handleCall( callOperation.getMethodName(), callOperation.getParameters() );
    } else if( operation instanceof NotifyOperation ) {
      NotifyOperation notifyOperation = ( NotifyOperation )operation;
      scheduleHandleNotify( handler, notifyOperation.getEventName(), notifyOperation.getProperties() );
    }
  }

  private static void scheduleHandleNotify( final OperationHandler handler,
                                            final String event,
                                            final JsonObject properties )
  {
    ProcessActionRunner.add( new Runnable() {
      @Override
      public void run() {
        handler.handleNotify( event, properties );
      }
    } );
  }

}
