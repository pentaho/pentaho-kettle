/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.widgets;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServiceHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Synchronizer;


/**
 * Instances of this class provide synchronization support for displays in
 * high-availability cluster environments.
 * <p>
 * If transparent session failover in conjunction with <code>(a)syncExec()</code>
 * is required, then this synchronizer should be used. Call <code>setSynchronizer()</code> right
 * after the display was constructed.
 * </p>
 *
 * @see Synchronizer
 * @see org.eclipse.swt.widgets.Display#setSynchronizer
 * @see org.eclipse.swt.widgets.Display#syncExec(Runnable)
 * @see org.eclipse.swt.widgets.Display#asyncExec(Runnable)
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ClusteredSynchronizer extends Synchronizer {
  private final String requestUrl;
  private final String cookies;

  public ClusteredSynchronizer( Display display ) {
    super( display );
    requestUrl = AsyncExecServiceHandler.createRequestUrl( RWT.getRequest() );
    cookies = extractRequestCookies( RWT.getRequest() );
    AsyncExecServiceHandler.register();
  }

  @Override
  protected void runnableAdded( Runnable runnable ) {
    notifyAsyncExecServiceHandler();
  }

  static String extractRequestCookies( HttpServletRequest request ) {
    String result = "";
    Cookie[] requestCookies = request.getCookies();
    if( requestCookies != null ) {
      for( Cookie requestCookie : requestCookies ) {
        if( result.length() > 0 ) {
          result += "; ";
        }
        result += requestCookie.getName() + "=" + requestCookie.getValue();
      }
    }
    return result;
  }

  private void notifyAsyncExecServiceHandler() {
    try {
      sendAsyncExecServiceHandlerRequest();
    } catch( IOException ioe ) {
      throw new RuntimeException( ioe );
    }
  }

  private void sendAsyncExecServiceHandlerRequest() throws IOException {
    HttpURLConnection connection = createConnection();
    connection.connect();
    int responseCode = connection.getResponseCode();
    if( responseCode != HttpURLConnection.HTTP_OK ) {
      String msg = "AsyncExec service request returned response code " + responseCode;
      throw new IOException( msg );
    }
  }

  private HttpURLConnection createConnection() throws IOException {
    URL url = new URL( requestUrl );
    HttpURLConnection result = ( HttpURLConnection )url.openConnection();
    if( cookies.length() > 0 ) {
      result.setRequestProperty( "Cookie", cookies );
    }
    return result;
  }

  static class AsyncExecServiceHandler implements ServiceHandler {
    static final String ID = "asyncExecServiceHandler";

    static void register() {
      AsyncExecServiceHandler serviceHandler = new AsyncExecServiceHandler();
      RWT.getServiceManager().registerServiceHandler( ID, serviceHandler );
    }

    static String createRequestUrl( HttpServletRequest request ) {
      StringBuilder buffer = new StringBuilder();
      buffer.append( "http://127.0.0.1:" );
      buffer.append( request.getServerPort() );
      buffer.append( RWT.getServiceManager().getServiceHandlerUrl( ID ) );
      return buffer.toString();
    }

    @Override
    public void service( HttpServletRequest request, HttpServletResponse response ) {
      // do nothing
    }
  }

}
