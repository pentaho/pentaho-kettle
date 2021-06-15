/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.remote;

import java.io.Serializable;

import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.swt.internal.widgets.IdGenerator;


public class ConnectionImpl implements Connection, Serializable {

  private final UISession uiSession;

  public ConnectionImpl( UISession uiSession ) {
    this.uiSession = uiSession;
  }

  @Override
  public RemoteObject createRemoteObject( String remoteType ) {
    ParamCheck.notNullOrEmpty( remoteType, "type" );
    String id = IdGenerator.getInstance( uiSession ).createId( "r" );
    RemoteObjectImpl remoteObject = new DeferredRemoteObject( id, remoteType );
    RemoteObjectRegistry.getInstance( uiSession ).register( remoteObject );
    return remoteObject;
  }

  /**
   * Creates an instance of RemoteObject for a given id that is agreed with the client, but does not
   * create the remote object on the client. The returned <code>RemoteObject</code> can be used to
   * receive messages from the client and to communicate with the remote object, provided that the
   * client knows the id.
   *
   * @return a representation of the remote object with the given id
   */
  // TODO [rst] Before this API is published, we should rethink the concept of "service" objects,
  //            i.e. remote objects that aren't created in the protocol, but used by agreed ids.
  public RemoteObject createServiceObject( String id ) {
    ParamCheck.notNullOrEmpty( id, "id" );
    RemoteObjectImpl remoteObject = new DeferredRemoteObject( id, null );
    RemoteObjectRegistry.getInstance( uiSession ).register( remoteObject );
    return remoteObject;
  }

}
