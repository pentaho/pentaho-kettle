/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 * Copyright (C) 2017 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.spoon;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.utils.URIUtils;
import org.mitre.dsmiley.httpproxy.ProxyServlet;

public class WebSpoonProxyServlet extends ProxyServlet {

  private static final String OSGI_SERVICE_PORT = "OSGI_SERVICE_PORT";

  @Override
  protected void initTarget() throws ServletException {
    //test it's valid
    try {
      targetUriObj = new URI( "http://localhost:" + getOsgiServicePort() );
    } catch (Exception e) {
      throw new ServletException("Trying to process targetUri init parameter: "+e,e);
    }
    targetHost = URIUtils.extractHost(targetUriObj);
  }

  @Override
  protected String rewriteUrlFromRequest(HttpServletRequest servletRequest) {
    servletRequest.setAttribute( ATTR_TARGET_URI, "" );
    // doubleEncodedUrl is double encoded like %252Fhome%252Fadmin
    String doubleEncodedUrl = super.rewriteUrlFromRequest( servletRequest );
    // decode once to make it like %2Fhome%2Fadmin
    String encodedUrl = URLDecoder.decode( doubleEncodedUrl );
    return encodedUrl;
  }

  /**
   * Get undecoded version of servletRequest.getPathInfo()
   */
  @Override
  protected String rewritePathInfoFromRequest(HttpServletRequest servletRequest) {
    String temp = servletRequest.getContextPath().concat( servletRequest.getServletPath() );
    return servletRequest.getRequestURI().substring( temp.length() );
  }

  private static Integer getOsgiServicePort() {
    // if no service port is specified try getting it from
    try {
      Class serverPortRegistry = Class.forName( "org.pentaho.platform.settings.ServerPortRegistry" );
      Method getPort = serverPortRegistry.getMethod( "getPort", String.class);
      Object osgiServicePort = getPort.invoke( null, OSGI_SERVICE_PORT );

      Class serverPort = Class.forName( "org.pentaho.platform.settings.ServerPort" );
      Method getAssignedPort = serverPort.getMethod( "getAssignedPort" );
      return (Integer) getAssignedPort.invoke( osgiServicePort );
    } catch ( Exception e ) {
      return null;
    }
  }
}
