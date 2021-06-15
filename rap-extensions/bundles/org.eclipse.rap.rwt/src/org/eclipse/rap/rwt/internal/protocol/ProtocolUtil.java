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
package org.eclipse.rap.rwt.internal.protocol;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.REQUEST_COUNTER;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.Operation.CallOperation;
import org.eclipse.rap.rwt.internal.protocol.Operation.NotifyOperation;
import org.eclipse.rap.rwt.internal.protocol.Operation.SetOperation;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceStore;
import org.eclipse.rap.rwt.internal.util.SharedInstanceBuffer;
import org.eclipse.rap.rwt.internal.util.SharedInstanceBuffer.InstanceCreator;
import org.eclipse.rap.rwt.remote.OperationHandler;


public final class ProtocolUtil {

  private static final Pattern FONT_NAME_FILTER_PATTERN = Pattern.compile( "\"|\\\\" );
  private static final String CLIENT_MESSAGE = ProtocolUtil.class.getName() + "#clientMessage";

  //////////////////////////////////////////////////////////////////////////////
  // TODO [fappel]: Experimental - profiler seems to indicate that buffering
  //                improves performance - still under investigation.
  private final static SharedInstanceBuffer<String,String[]> parsedFonts
    = new SharedInstanceBuffer<>();
  private final static InstanceCreator<String, String[]> parsedFontsCreator
    = new InstanceCreator<String, String[]>() {
    @Override
    public String[] createInstance( String name ) {
      return parseFontNameInternal( name );
    }
  };
  //////////////////////////////////////////////////////////////////////////////

  private ProtocolUtil() {
    // prevent instantiation
  }

  public static ClientMessage getClientMessage() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    return ( ClientMessage )serviceStore.getAttribute( CLIENT_MESSAGE );
  }

  public static void setClientMessage( ClientMessage clientMessage ) {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    serviceStore.setAttribute( CLIENT_MESSAGE, clientMessage );
  }

  public static void handleOperation( OperationHandler handler, Operation operation ) {
    if( operation instanceof SetOperation ) {
      SetOperation setOperation = ( SetOperation )operation;
      handler.handleSet( setOperation.getProperties() );
    } else if( operation instanceof CallOperation ) {
      CallOperation callOperation = ( CallOperation )operation;
      handler.handleCall( callOperation.getMethodName(), callOperation.getParameters() );
    } else if( operation instanceof NotifyOperation ) {
      NotifyOperation notifyOperation = ( NotifyOperation )operation;
      handler.handleNotify( notifyOperation.getEventName(), notifyOperation.getProperties() );
    }
  }

  public static JsonValue readPropertyValue( String target, String property ) {
    SetOperation operation =  getClientMessage().getLastSetOperationFor( target, property );
    return operation != null ? operation.getProperties().get( property ) : null;
  }

  public static JsonValue readEventPropertyValue( String target, String eventName, String property ) {
    NotifyOperation operation = getClientMessage().getLastNotifyOperationFor( target, eventName );
    return operation != null ? operation.getProperties().get( property ) : null;
  }

  public static boolean wasEventSent( String target, String eventName ) {
    ClientMessage message = getClientMessage();
    NotifyOperation operation =  message.getLastNotifyOperationFor( target, eventName );
    return operation != null;
  }

  public static String[] parseFontName( final String name ) {
    return parsedFonts.get( name, parsedFontsCreator );
  }

  public static boolean isInitialRequest( RequestMessage message ) {
    return JsonValue.valueOf( 0 ).equals( message.getHead().get( REQUEST_COUNTER ) );
  }

  private static String[] parseFontNameInternal( String name ) {
    String[] result = name.split( "," );
    for( int i = 0; i < result.length; i++ ) {
      result[ i ] = result[ i ].trim();
      Matcher matcher = FONT_NAME_FILTER_PATTERN.matcher( result[ i ] );
      result[ i ] = matcher.replaceAll( "" );
    }
    return result;
  }

}
