/*******************************************************************************
 * Copyright (c) 2002, 2016 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    Frank Appel - replaced singletons and static fields (Bug 337787)
 *    EclipseSource - ongoing implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.service;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.CONNECTION_ID;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.rap.rwt.internal.RWTProperties;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.service.ServiceHandler;
import org.eclipse.rap.rwt.service.ServiceManager;
import org.eclipse.rap.rwt.service.UISession;


public class ServiceManagerImpl implements ServiceManager {

  public static final String REQUEST_PARAM = "servicehandler";

  private final ServiceHandler defaultHandler;
  private final ServiceHandlerRegistry customHandlers;

  public ServiceManagerImpl( ServiceHandler defaultServieHandler ) {
    defaultHandler = defaultServieHandler;
    customHandlers = new ServiceHandlerRegistry();
  }

  public ServiceHandler getServiceHandler( String customId ) {
    return customHandlers.get( customId );
  }

  @Override
  public void registerServiceHandler( String id, ServiceHandler handler ) {
    ParamCheck.notNullOrEmpty( id, "id" );
    ParamCheck.notNull( handler, "handler" );
    if( !customHandlers.put( id, handler ) ) {
      String message = "A service handler is already registered with this id: " + id;
      throw new IllegalArgumentException( message );
    }
  }

  @Override
  public void unregisterServiceHandler( String id ) {
    ParamCheck.notNullOrEmpty( id, "id" );
    customHandlers.remove( id );
  }

  @Override
  public String getServiceHandlerUrl( String id ) {
    ParamCheck.notNull( id, "id" );
    HttpServletRequest request = ContextProvider.getRequest();
    StringBuilder url = new StringBuilder();
    String baseUrl = RWTProperties.getServiceHandlerBaseUrl();
    if( baseUrl != null ) {
      url.append( baseUrl );
    }
    url.append( request.getRequestURI() )
      .append( '?' )
      .append( REQUEST_PARAM )
      .append( '=' )
      .append( encodeParameter( id ) );
    String connectionId = getConnectionId();
    if( connectionId != null ) {
      url.append( '&' )
        .append( CONNECTION_ID )
        .append( '=' )
        .append( connectionId );
    }
    return ContextProvider.getResponse().encodeURL( url.toString() );
  }

  public void clear() {
    customHandlers.clear();
  }

  public ServiceHandler getHandler() {
    ServiceHandler result;
    String customId = getCustomHandlerId();
    if( customId != null && customId.length() > 0 ) {
      result = getCustomHandlerChecked( customId );
    } else {
      result = defaultHandler;
    }
    return result;
  }

  private ServiceHandler getCustomHandlerChecked( String customId ) {
    ServiceHandler customHandler = customHandlers.get( customId );
    if( customHandler == null ) {
      throw new IllegalArgumentException( "No service handler registered with id " + customId );
    }
    return customHandler;
  }

  private static String getCustomHandlerId() {
    return ContextProvider.getRequest().getParameter( REQUEST_PARAM );
  }

  private static String getConnectionId() {
    UISession uiSession = ContextProvider.getUISession();
    return uiSession == null ? null : ( ( UISessionImpl )uiSession ).getConnectionId();
  }

  private static String encodeParameter( String id ) {
    try {
      // TODO [rst] Encode parameters according to URI spec (RFC 2396). URLEncoder is meant for form
      //            encoding which is not the same, but better than nothing for the moment.
      // See http://stackoverflow.com/questions/444112/how-do-i-encode-uri-parameter-values
      return URLEncoder.encode( id, "UTF-8" ).replace( "+", "%20" );
    } catch( UnsupportedEncodingException exception ) {
      throw new RuntimeException( exception );
    }
  }

}
