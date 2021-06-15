/*******************************************************************************
 * Copyright (c) 2014, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.service;

import static org.eclipse.rap.rwt.internal.protocol.ProtocolUtil.isInitialRequest;
import static org.eclipse.rap.rwt.internal.service.ContextProvider.getProtocolWriter;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.LifeCycleFactory;
import org.eclipse.rap.rwt.internal.protocol.ClientMessage;
import org.eclipse.rap.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.rap.rwt.internal.protocol.RequestMessage;
import org.eclipse.rap.rwt.internal.protocol.ResponseMessage;
import org.eclipse.rap.rwt.internal.remote.MessageFilter;
import org.eclipse.rap.rwt.internal.remote.MessageFilterChain;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectLifeCycleAdapter;


public class RWTMessageHandler implements MessageFilter {

  private final LifeCycleFactory lifeCycleFactory;

  public RWTMessageHandler( LifeCycleFactory lifeCycleFactory ) {
    this.lifeCycleFactory = lifeCycleFactory;
  }

  @Override
  public ResponseMessage handleMessage( RequestMessage request, MessageFilterChain chain ) {
    ClientMessage clientMessage = new ClientMessage( request );
    ProtocolUtil.setClientMessage( clientMessage );
    workAroundMissingReadData( clientMessage );
    executeLifeCycle();
    return getProtocolWriter().createMessage();
  }

  private static void workAroundMissingReadData( ClientMessage message ) {
    // TODO [tb] : This is usually done in DisplayLCA#readData, but the ReadData
    // phase is omitted in the first POST request. Since RemoteObjects may already be registered
    // at this point, this workaround is currently required. We should find a solution that
    // does not require RemoteObjectLifeCycleAdapter.readData to be called in different places.
    if( isInitialRequest( message ) ) {
      RemoteObjectLifeCycleAdapter.readData( message );
    }
  }

  private void executeLifeCycle() {
    try {
      lifeCycleFactory.getLifeCycle().execute();
    } catch( IOException exception ) {
      // TODO [rst]: LCAs should not need to do any I/O. However, IOException is declared in their
      // methods. Once LCAs are removed from our API, we can get rid of the declared IOException.
      throw new RuntimeException( exception );
    }
  }

}
