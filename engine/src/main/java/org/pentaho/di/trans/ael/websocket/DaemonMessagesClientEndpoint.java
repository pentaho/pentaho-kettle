/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */
package org.pentaho.di.trans.ael.websocket;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.remote.ExecutionRequest;
import org.pentaho.di.engine.api.remote.Message;
import org.pentaho.di.engine.api.remote.MessageDecoder;
import org.pentaho.di.engine.api.remote.MessageEncoder;
import org.pentaho.di.engine.api.remote.StopMessage;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import java.io.IOException;
import java.net.URI;

/**
 * Created by fcamara on 8/17/17.
 */
@ClientEndpoint( encoders = MessageEncoder.class, decoders = MessageDecoder.class )
public class DaemonMessagesClientEndpoint {
  private static final String PRFX_WS = "ws://";
  private static final String PRFX_WS_SSL = "wss://";
  private final MessageEventService messageEventService;
  private Session userSession = null;

  public DaemonMessagesClientEndpoint( String host, String port, boolean ssl,
                                       MessageEventService messageEventService ) throws KettleException {
    try {
      String url = ( ssl ? PRFX_WS_SSL : PRFX_WS ) + host + ":" + port + "/execution";
      this.messageEventService = messageEventService;

      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
      container.connectToServer( this, new URI( url ) );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  /**
   * Callback hook for Connection open events.
   *
   * @param userSession the userSession which is opened.
   */
  @OnOpen
  public void onOpen( Session userSession ) {
    this.userSession = userSession;
    this.userSession.setMaxTextMessageBufferSize( 500000 );
    this.userSession.setMaxBinaryMessageBufferSize( 500000 );
  }

  /**
   * Callback hook for Connection close events.
   *
   * @param userSession the userSession which is getting closed.
   * @param reason      the reason for connection close
   */
  @OnClose
  public void onClose( Session userSession, CloseReason reason ) {
    this.userSession = null;
  }

  /**
   * Callback hook for Message Events. This method will be invoked when a client send a message.
   *
   * @param message The text message
   */
  @OnMessage
  public void onMessage( Message message, Session session ) throws KettleException {
    messageEventService.fireEvent( message );
  }

  /**
   * Send a message.
   *
   * @param request
   */
  public void sendMessage( ExecutionRequest request ) throws KettleException {
    try {
      this.userSession.getBasicRemote().sendObject( request );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  public void sendMessage( StopMessage stopMessage ) throws KettleException {
    try {
      this.userSession.getBasicRemote().sendObject( stopMessage );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  public void close() throws KettleException {
    try {
      if ( this.userSession != null && this.userSession.isOpen() ) {
        this.userSession.close();
      }
    } catch ( IOException e ) {
      throw new KettleException( e );
    }
  }
}
