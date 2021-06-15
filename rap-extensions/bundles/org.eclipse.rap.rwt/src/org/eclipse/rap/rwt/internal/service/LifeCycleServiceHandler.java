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
package org.eclipse.rap.rwt.internal.service;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_PRECONDITION_FAILED;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.REQUEST_COUNTER;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.SHUTDOWN;
import static org.eclipse.rap.rwt.internal.service.ContextProvider.getUISession;
import static org.eclipse.rap.rwt.internal.util.HTTP.CHARSET_UTF_8;
import static org.eclipse.rap.rwt.internal.util.HTTP.CONTENT_TYPE_JSON;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.lifecycle.RequestCounter;
import org.eclipse.rap.rwt.internal.protocol.ClientMessage;
import org.eclipse.rap.rwt.internal.protocol.ProtocolMessageWriter;
import org.eclipse.rap.rwt.internal.protocol.RequestMessage;
import org.eclipse.rap.rwt.internal.protocol.ResponseMessage;
import org.eclipse.rap.rwt.internal.remote.MessageChainReference;
import org.eclipse.rap.rwt.service.ServiceHandler;
import org.eclipse.rap.rwt.service.UISession;


public class LifeCycleServiceHandler implements ServiceHandler {

  private static final String PROP_ERROR = "error";
  private static final String ATTR_LAST_RESPONSE_MESSAGE
    = LifeCycleServiceHandler.class.getName() + "#lastResponseMessage";

  private final MessageChainReference messageChainReference;

  public LifeCycleServiceHandler( MessageChainReference messageChainReference ) {
    this.messageChainReference = messageChainReference;
  }

  @Override
  public void service( HttpServletRequest request, HttpServletResponse response )
    throws IOException
  {
    UISessionImpl uiSession = ( UISessionImpl )getUISession();
    if( uiSession == null ) {
      setJsonResponseHeaders( response );
      writeSessionTimeoutError( response );
    } else {
      // Do not use uiSession itself as a lock
      // see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=372946
      synchronized( uiSession.getRequestLock() ) {
        synchronizedService( request, response );
      }
    }
  }

  void synchronizedService( HttpServletRequest request, HttpServletResponse response )
    throws IOException
  {
    try {
      processUIRequest( request, response );
    } catch( IOException exception ) {
      shutdownUISession();
      throw exception;
    } catch( RuntimeException exception ) {
      shutdownUISession();
      throw exception;
    }
  }

  private void processUIRequest( HttpServletRequest request, HttpServletResponse response )
    throws IOException
  {
    RequestMessage requestMessage = readRequestMessage( request );
    setJsonResponseHeaders( response );
    if( isSessionShutdown( requestMessage ) ) {
      shutdownUISession();
      writeEmptyMessage( response );
    } else if( !isRequestCounterValid( requestMessage ) ) {
      if( isDuplicateRequest( requestMessage ) ) {
        writeBufferedResponse( response );
      } else {
        writeInvalidRequestCounterError( response );
      }
    } else {
      ResponseMessage responseMessage = processMessage( requestMessage );
      writeResponseMessage( responseMessage, response );
      RequestCounter.getInstance().nextRequestId();
    }
  }

  private static RequestMessage readRequestMessage( HttpServletRequest request ) {
    try {
      return new ClientMessage( JsonObject.readFrom( getReader( request ) ) );
    } catch( IOException ioe ) {
      throw new IllegalStateException( "Unable to read the json message", ioe );
    }
  }

  /*
   * Workaround for bug in certain servlet containers where the reader is sometimes empty.
   * 411616: Application crash with very long messages
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=411616
   */
  private static Reader getReader( HttpServletRequest request ) throws IOException {
    String encoding = request.getCharacterEncoding();
    if( encoding == null ) {
      encoding = CHARSET_UTF_8;
    }
    return new InputStreamReader( request.getInputStream(), encoding );
  }

  private ResponseMessage processMessage( RequestMessage requestMessage ) {
    return messageChainReference.get().handleMessage( requestMessage );
  }

  static boolean isRequestCounterValid( RequestMessage requestMessage ) {
    int expectedRequestId = RequestCounter.getInstance().currentRequestId();
    JsonValue sentRequestId = requestMessage.getHead().get( REQUEST_COUNTER );
    if( sentRequestId == null ) {
      return false;
    }
    return sentRequestId.asInt() == expectedRequestId;
  }

  private static boolean isDuplicateRequest( RequestMessage requestMessage ) {
    int currentRequestId = RequestCounter.getInstance().currentRequestId();
    JsonValue sentRequestId = requestMessage.getHead().get( REQUEST_COUNTER );
    return sentRequestId != null && sentRequestId.asInt() == currentRequestId - 1;
  }

  private static void shutdownUISession() {
    UISessionImpl uiSession = ( UISessionImpl )getUISession();
    uiSession.shutdown();
  }

  private static void writeInvalidRequestCounterError( HttpServletResponse response )
    throws IOException
  {
    writeError( response, SC_PRECONDITION_FAILED, "invalid request counter" );
  }

  private static void writeSessionTimeoutError( HttpServletResponse response ) throws IOException {
    writeError( response, SC_FORBIDDEN, "session timeout" );
  }

  private static void writeError( HttpServletResponse response,
                                  int statusCode,
                                  String errorType ) throws IOException
  {
    response.setStatus( statusCode );
    ProtocolMessageWriter writer = new ProtocolMessageWriter();
    writer.appendHead( PROP_ERROR, JsonValue.valueOf( errorType ) );
    writer.createMessage().toJson().writeTo( response.getWriter() );
  }

  private static boolean isSessionShutdown( RequestMessage requestMessage ) {
    return JsonValue.TRUE.equals( requestMessage.getHead().get( SHUTDOWN ) );
  }

  private static void setJsonResponseHeaders( ServletResponse response ) {
    response.setContentType( CONTENT_TYPE_JSON );
    response.setCharacterEncoding( CHARSET_UTF_8 );
  }

  private static void writeEmptyMessage( ServletResponse response ) throws IOException {
    new ProtocolMessageWriter().createMessage().toJson().writeTo( response.getWriter() );
  }

  private static void writeResponseMessage( ResponseMessage responseMessage,
                                            ServletResponse response )
    throws IOException
  {
    bufferMessage( responseMessage );
    responseMessage.toJson().writeTo( response.getWriter() );
  }

  private static void writeBufferedResponse( HttpServletResponse response ) throws IOException {
    getBufferedMessage().toJson().writeTo( response.getWriter() );
  }

  private static void bufferMessage( ResponseMessage responseMessage ) {
    UISession uiSession = getUISession();
    if( uiSession != null ) {
      uiSession.setAttribute( ATTR_LAST_RESPONSE_MESSAGE, responseMessage );
    }
  }

  private static ResponseMessage getBufferedMessage() {
    return ( ResponseMessage )getUISession().getAttribute( ATTR_LAST_RESPONSE_MESSAGE );
  }

}
