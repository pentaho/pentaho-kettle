/*******************************************************************************
 * Copyright (c) 2009, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.displaykit;

import static org.eclipse.rap.rwt.internal.protocol.ProtocolUtil.handleOperation;

import org.eclipse.rap.rwt.internal.protocol.ClientMessage;
import org.eclipse.rap.rwt.internal.protocol.Operation;
import org.eclipse.rap.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectRegistry;
import org.eclipse.rap.rwt.remote.OperationHandler;
import org.eclipse.swt.internal.dnd.dragsourcekit.DragSourceOperationHandler;
import org.eclipse.swt.internal.dnd.droptargetkit.DropTargetOperationHandler;

public final class DNDSupport {

  private DNDSupport() {
    // prevent instantiation
  }

  public static void handleOperations() {
    ClientMessage clientMessage = ProtocolUtil.getClientMessage();
    for( Operation operation : clientMessage.getOperations() ) {
      OperationHandler handler = getOperationHandler( operation.getTarget() );
      if( isDNDOperationHandler( handler) ) {
        handleOperation( handler, operation );
      }
    }
  }

  private static OperationHandler getOperationHandler( String id ) {
    RemoteObjectImpl remoteObject = RemoteObjectRegistry.getInstance().get( id );
    if( remoteObject != null ) {
      return remoteObject.getHandler();
    }
    return null;
  }

  private static boolean isDNDOperationHandler( OperationHandler handler ) {
    return    handler instanceof DragSourceOperationHandler
           || handler instanceof DropTargetOperationHandler;
  }

}

