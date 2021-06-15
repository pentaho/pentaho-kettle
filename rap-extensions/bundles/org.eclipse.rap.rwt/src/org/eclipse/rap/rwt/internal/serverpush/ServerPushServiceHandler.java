/*******************************************************************************
 * Copyright (c) 2007, 2018 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.serverpush;

import static org.eclipse.rap.rwt.internal.service.ContextProvider.getUISession;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rap.rwt.service.ServiceHandler;


public class ServerPushServiceHandler implements ServiceHandler {

  public final static String HANDLER_ID = "org.eclipse.rap.serverpush";

  @Override
  public void service( HttpServletRequest request, HttpServletResponse response )
    throws IOException
  {
    setResponseHeaders( response );
    if( getUISession() != null ) {
      ServerPushManager.getInstance().processRequest( response );
    }
  }

  private static void setResponseHeaders( HttpServletResponse response ) {
    // Ensures that the response is not cached
    // 410157: [ServerPush] ServerPush requests always return immediately in IE
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=410157
    response.setHeader( "Cache-Control", "no-cache, no-store, must-revalidate" );
    response.setHeader( "Pragma", "no-cache" );
    response.setHeader( "Content-Type", "application/octet-stream" );
    response.setDateHeader( "Expires", 0 );
  }

}
