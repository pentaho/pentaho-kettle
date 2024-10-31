/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.engine.api.remote.ExecutionRequest;
import org.pentaho.di.engine.api.remote.Message;
import org.pentaho.di.engine.api.remote.MessageDecoder;
import org.pentaho.di.engine.api.remote.MessageEncoder;
import org.pentaho.di.engine.api.remote.StopMessage;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.ael.websocket.exception.MessageEventFireEventException;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fcamara on 8/17/17.
 */

public class DaemonMessagesClientEndpoint extends Endpoint {
  /**
   * The package name, used for internationalization of messages.
   */
  private static Class<?> PKG = DaemonMessagesClientEndpoint.class; // for i18n purposes
  //i18n messages
  private static final String EXCEPTION_SESSION_CLOSED = "DaemonMessagesClientEndpoint.Exception.SessionIsClosed";

  private static final String KETTLE_AEL_PDI_DAEMON_PRINCIPAL = "KETTLE_AEL_PDI_DAEMON_PRINCIPAL";
  private static final String KETTLE_AEL_PDI_DAEMON_KEYTAB = "KETTLE_AEL_PDI_DAEMON_KEYTAB";
  private static final String KETTLE_AEL_PDI_DAEMON_CONTEXT_REUSE = "KETTLE_AEL_PDI_DAEMON_CONTEXT_REUSE";
  private static final String Y_LWC = "y";
  private static final int MAX_TXT_MSG_BUF_SIZE = 500000;
  private static final int MAX_BIN_MSG_BUF_SIZE = 500000;

  private static final String URL_TEMPLATE = "%s://%s:%s/execution";
  private static final String PRFX_WS = "ws";
  private static final String PRFX_WS_SSL = "wss";
  private final MessageEventService messageEventService;
  private Session userSession = null;
  private String principal = null;
  private String keytab = null;
  private boolean reuseSparkContext = false;
  //only one stop message
  private AtomicBoolean alReadySendedStopMessage = new AtomicBoolean( false );

  public DaemonMessagesClientEndpoint( String host, int port, boolean ssl,
                                       MessageEventService messageEventService ) throws KettleException {
    try {
      setAuthProperties();

      String url = String.format( URL_TEMPLATE, ( ssl ? PRFX_WS_SSL : PRFX_WS ), host, port );
      URI uri = new URI( url );
      this.messageEventService = messageEventService;

      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
      container.connectToServer( this, ClientEndpointConfig.Builder.create()
        .encoders( Collections.singletonList( MessageEncoder.class ) )
        .decoders( Collections.singletonList( MessageDecoder.class ) )
        .configurator( new SessionConfigurator( uri, keytab, principal ) )
        .build(), uri );

    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  //TODO: this is temporary for testing purpose we should get this values from the shim properties
  private void setAuthProperties() {
    Variables variables = new Variables();
    variables.initializeVariablesFrom( null );

    this.principal = variables.getVariable( KETTLE_AEL_PDI_DAEMON_PRINCIPAL, null );
    this.keytab = variables.getVariable( KETTLE_AEL_PDI_DAEMON_KEYTAB, null );
    String reuse = variables.getVariable( KETTLE_AEL_PDI_DAEMON_CONTEXT_REUSE, Boolean.FALSE.toString() );
    this.reuseSparkContext =
      Boolean.TRUE.toString().equals( reuse.toLowerCase() ) || Y_LWC.equals( reuse.toLowerCase() );
  }

  /**
   * Callback hook for Connection open events.
   *
   * @param userSession the userSession which is opened.
   */
  @Override
  public void onOpen( Session userSession, EndpointConfig endpointConfig ) {
    this.userSession = userSession;
    this.userSession.setMaxTextMessageBufferSize( MAX_TXT_MSG_BUF_SIZE );
    this.userSession.setMaxBinaryMessageBufferSize( MAX_BIN_MSG_BUF_SIZE );

    userSession.addMessageHandler( new MessageHandler.Whole<Message>() {
      /**
       * Callback hook for Message Events. This method will be invoked when the server send a message.
       *
       * @param message The text message
       */
      @Override
      public void onMessage( Message message ) {
        try {
          messageEventService.fireEvent( message );
        } catch ( MessageEventFireEventException e ) {
          throw new RuntimeException( e );
        }
      }
    } );
  }

  /**
   * Callback hook for Connection close events.
   *
   * @param userSession the userSession which is getting closed.
   * @param reason      the reason for connection close
   */
  @Override
  public void onClose( Session userSession, CloseReason reason ) {
    this.userSession = null;
  }

  /**
   * Callback hook for Connection close events.
   *
   * @param userSession the userSession which is getting closed.
   * @param thr         throwable
   */
  @Override
  public void onError( Session userSession, Throwable thr ) {
    throw new RuntimeException( thr );
  }

  /**
   * Send a execution request message.
   */
  public void sendMessage( ExecutionRequest request ) throws KettleException {
    sessionValid();
    try {
      request.setReuseSparkContext( reuseSparkContext );
      this.userSession.getBasicRemote().sendObject( request );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  /**
   * Send a stop message to server as result of user request.
   */
  public void sendMessage( StopMessage stopMessage ) throws KettleException {
    sessionValid();
    try {
      if ( !alReadySendedStopMessage.getAndSet( true ) ) {
        this.userSession.getBasicRemote().sendObject( stopMessage );
      }
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  /**
   * Close the session informing the reason.
   */
  public void close( String message ) throws KettleException {
    sessionValid();
    try {
      if ( this.userSession != null && this.userSession.isOpen() ) {
        this.userSession.close( new CloseReason( CloseReason.CloseCodes.NORMAL_CLOSURE, message ) );
      }
    } catch ( IOException e ) {
      throw new KettleException( e );
    }
  }

  /**
   * Validates if the session is open.
   */
  public void sessionValid() throws KettleException {
    if ( this.userSession == null || !this.userSession.isOpen() ) {
      throw new KettleException( BaseMessages.getString( PKG, EXCEPTION_SESSION_CLOSED ) );
    }
  }
}
