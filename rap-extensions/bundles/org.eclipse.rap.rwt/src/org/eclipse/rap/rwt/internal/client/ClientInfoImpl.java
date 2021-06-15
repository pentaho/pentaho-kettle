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
package org.eclipse.rap.rwt.internal.client;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.ClientInfo;
import org.eclipse.rap.rwt.internal.remote.ConnectionImpl;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.rap.rwt.remote.RemoteObject;


public class ClientInfoImpl implements ClientInfo {

  private Integer timezoneOffset;
  private Locale[] locales;

  public ClientInfoImpl() {
    initialize();
  }

  private void initialize() {
    ConnectionImpl connection = ( ConnectionImpl )RWT.getUISession().getConnection();
    RemoteObject remoteObject = connection.createServiceObject( "rwt.client.ClientInfo" );
    remoteObject.setHandler( new InfoOperationHandler() );
    HttpServletRequest request = ContextProvider.getRequest();
    if( request.getHeader( "Accept-Language" ) != null ) {
      Enumeration<Locale> locales = request.getLocales();
      this.locales = Collections.list( locales ).toArray( new Locale[ 1 ] );
    }
  }

  @Override
  public int getTimezoneOffset() {
    if( timezoneOffset == null ) {
      throw new IllegalStateException( "timezoneOffset is not set" );
    }
    return timezoneOffset.intValue();
  }

  @Override
  public Locale getLocale() {
    return locales == null ? null : locales[ 0 ];
  }

  @Override
  public Locale[] getLocales() {
    return locales == null ? new Locale[ 0 ] : locales.clone();
  }

  private final class InfoOperationHandler extends AbstractOperationHandler {
    @Override
    public void handleSet( JsonObject properties ) {
      JsonValue value = properties.get( "timezoneOffset" );
      if( value != null ) {
        timezoneOffset = Integer.valueOf( value.asInt() );
      }
    }
  }

}
