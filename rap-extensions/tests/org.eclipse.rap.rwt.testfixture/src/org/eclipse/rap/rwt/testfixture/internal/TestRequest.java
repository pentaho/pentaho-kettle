/*******************************************************************************
 * Copyright (c) 2009, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 *    Frank Appel - replaced singletons and static fields (Bug 337787)
 ******************************************************************************/
package org.eclipse.rap.rwt.testfixture.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ReadListener;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;


/**
 * <p>
 * <strong>IMPORTANT:</strong> This class is <em>not</em> part the public RAP
 * API. It may change or disappear without further notice. Use this class at
 * your own risk.
 * </p>
 */
public final class TestRequest implements HttpServletRequest {

  private static final String DEFAULT_SCHEME = "http";
  public static final String DEFAULT_REQUEST_URI = "/fooapp/rap";
  public static final String DEFAULT_CONTEX_PATH = "/fooapp";
  public static final String DEFAULT_SERVER_NAME = "fooserver";
  public static final String DEFAULT_SERVLET_PATH = "/rap";
  public static final int PORT = 8080;

  private final StringBuffer requestURL;
  private HttpSession session;
  private String scheme;
  private String serverName;
  private String contextPath;
  private String requestURI;
  private String servletPath;
  private String pathInfo;
  private final Map<String,String[]> parameters;
  private final Map<String,String> headers;
  private final Map<String,Object> attributes;
  private final Collection<Cookie> cookies;
  private String contentType;
  private String body;
  private String method;
  private Locale[] locales;

  public TestRequest() {
    requestURL = new StringBuffer();
    scheme = DEFAULT_SCHEME;
    serverName = DEFAULT_SERVER_NAME;
    contextPath = DEFAULT_CONTEX_PATH;
    requestURI = DEFAULT_REQUEST_URI;
    servletPath = DEFAULT_SERVLET_PATH;
    parameters = new HashMap<String,String[]>();
    headers = new HashMap<String, String>();
    attributes = new HashMap<String,Object>();
    cookies = new LinkedList<Cookie>();
  }

  public String getAuthType() {
    return null;
  }

  public void addCookie( Cookie cookie ) {
    cookies.add( cookie );
  }

  public Cookie[] getCookies() {
    return cookies.toArray( new Cookie[ cookies.size() ] );
  }

  public long getDateHeader( String arg0 ) {
    return 0;
  }

  public String getHeader( String arg0 ) {
    return headers.get( arg0 );
  }

  public void setHeader( String arg0, String arg1) {
    headers.put( arg0, arg1 );
  }

  public Enumeration<String> getHeaders( String arg0 ) {
    return null;
  }

  public Enumeration<String> getHeaderNames() {
    return new Enumeration<String>() {
      private final Iterator iterator = headers.keySet().iterator();
      public boolean hasMoreElements() {
        return iterator.hasNext();
      }
      public String nextElement() {
        return ( String )iterator.next();
      }
    };
  }

  public int getIntHeader( String arg0 ) {
    return 0;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod( String method ) {
    this.method = method;
  }

  public String getPathInfo() {
    return pathInfo;
  }

  public void setPathInfo( String pathInfo ) {
    this.pathInfo = pathInfo;
  }

  public String getPathTranslated() {
    return null;
  }

  public String getContextPath() {
    return contextPath;
  }

  public void setContextPath( String contextPath ) {
    this.contextPath = contextPath;
  }

  public String getQueryString() {
    return null;
  }

  public String getRemoteUser() {
    return null;
  }

  public boolean isUserInRole( String arg0 ) {
    return false;
  }

  public Principal getUserPrincipal() {
    return null;
  }

  public String getRequestedSessionId() {
    return null;
  }

  public String getRequestURI() {
    return requestURI;
  }

  public void setRequestURI( String requestURI ) {
    this.requestURI = requestURI;
  }

  public StringBuffer getRequestURL() {
    return requestURL;
  }

  public String getServletPath() {
    return servletPath;
  }

  public void setServletPath( String servletPath ) {
    this.servletPath = servletPath;
  }

  public HttpSession getSession( boolean arg0 ) {
    return session;
  }

  public HttpSession getSession() {
    return session;
  }

  public boolean isRequestedSessionIdValid() {
    return false;
  }

  public boolean isRequestedSessionIdFromCookie() {
    return false;
  }

  public boolean isRequestedSessionIdFromURL() {
    return false;
  }

  public boolean isRequestedSessionIdFromUrl() {
    return false;
  }

  public Object getAttribute( String arg0 ) {
    return attributes.get( arg0 );
  }

  public Enumeration<String> getAttributeNames() {
    return null;
  }

  public String getCharacterEncoding() {
    return null;
  }

  public void setCharacterEncoding( String arg0 )
    throws UnsupportedEncodingException
  {
  }

  public int getContentLength() {
    return body != null ? body.length() : 0;
  }

  public long getContentLengthLong() {
    return getContentLength();
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType( String contentType ) {
    this.contentType = contentType;
  }

  public ServletInputStream getInputStream() throws IOException {
    final StringReader reader = new StringReader( body );
    return new ServletInputStream() {
      @Override
      public int read() throws IOException {
        return reader.read();
      }
      @Override
      public boolean isFinished() {
        return false;
      }
      @Override
      public boolean isReady() {
        return true;
      }
      @Override
      public void setReadListener( ReadListener readListener ) {
      }
    };
  }

  public String getParameter( String arg0 ) {
    String[] value = parameters.get( arg0 );
    String result = null;
    if( value != null ) {
      result = value[ 0 ];
    }
    return result;
  }

  public Enumeration<String> getParameterNames() {
    return new Enumeration<String>() {
      private final Iterator iterator = parameters.keySet().iterator();
      public boolean hasMoreElements() {
        return iterator.hasNext();
      }

      public String nextElement() {
        return ( String )iterator.next();
      }
    };
  }

  public String[] getParameterValues( String arg0 ) {
    return parameters.get( arg0 );
  }

  public void setParameter( String key, String value ) {
    if( value == null ) {
      parameters.remove( key );
    } else {
      parameters.put( key, new String[] { value } );
    }
  }

  public void addParameter( String key, String value ) {
    if( parameters.containsKey( key ) ) {
      String[] values = parameters.get( key );
      String[] newValues = new String[ values.length + 1 ];
      System.arraycopy( values, 0, newValues, 0, values.length );
      newValues[ values.length ] = value;
      parameters.put( key, newValues );
    } else {
      setParameter( key, value );
    }
  }

  public Map<String,String[]> getParameterMap() {
    return parameters;
  }

  public String getProtocol() {
    return null;
  }

  public String getScheme() {
    return scheme;
  }

  public void setScheme( String scheme ) {
    this.scheme = scheme;
  }

  public String getServerName() {
    return serverName;
  }

  public void setServerName( String serverName ) {
    this.serverName = serverName;
  }

  public int getServerPort() {
    return PORT;
  }

  public BufferedReader getReader() throws IOException {
    return new BufferedReader( new StringReader( body != null ? body : "" ) );
  }

  public void setBody( String body ) {
    this.body = body;
  }

  public String getBody() {
    return body;
  }

  public String getRemoteAddr() {
    return null;
  }

  public String getRemoteHost() {
    return null;
  }

  public void setAttribute( String arg0, Object arg1 ) {
    attributes.put( arg0, arg1 );
  }

  public void removeAttribute( String arg0 ) {
  }

  public Locale getLocale() {
    return locales == null || locales.length == 0 ? Locale.getDefault() : locales[ 0 ] ;
  }

  public Enumeration<Locale> getLocales() {
    Locale[] returnedLocales = locales;
    if( locales == null || locales.length == 0 ) {
      returnedLocales = new Locale[]{ Locale.getDefault() };
    }
    final Iterator<Locale> iterator = Arrays.asList( returnedLocales ).iterator();
    return new Enumeration<Locale>() {

      public Locale nextElement() {
        return iterator.next();
      }

      public boolean hasMoreElements() {
        return iterator.hasNext();
      }
    };
  }

  public void setLocales( Locale... locales ) {
    this.locales = locales;
  }

  public boolean isSecure() {
    return false;
  }

  public RequestDispatcher getRequestDispatcher( String arg0 ) {
    return null;
  }

  public String getRealPath( String arg0 ) {
    return null;
  }

  public void setSession( HttpSession session ) {
    this.session = session;
  }

  public String getLocalAddr() {
    throw new UnsupportedOperationException();
  }

  public String getLocalName() {
    throw new UnsupportedOperationException();
  }

  public int getLocalPort() {
    throw new UnsupportedOperationException();
  }

  public int getRemotePort() {
    throw new UnsupportedOperationException();
  }

  public ServletContext getServletContext() {
    return null;
  }

  public AsyncContext startAsync() throws IllegalStateException {
    return null;
  }

  public AsyncContext startAsync( ServletRequest servletRequest, ServletResponse servletResponse )
    throws IllegalStateException
  {
    return null;
  }

  public boolean isAsyncStarted() {
    return false;
  }

  public boolean isAsyncSupported() {
    return false;
  }

  public AsyncContext getAsyncContext() {
    return null;
  }

  public DispatcherType getDispatcherType() {
    return null;
  }

  public boolean authenticate( HttpServletResponse response ) throws IOException, ServletException {
    return false;
  }

  public void login( String username, String password ) throws ServletException {
  }

  public void logout() throws ServletException {
  }

  public Collection<Part> getParts() throws IOException, ServletException {
    return null;
  }

  public Part getPart( String name ) throws IOException, ServletException {
    return null;
  }

  public String changeSessionId() {
    return null;
  }

  public <T extends HttpUpgradeHandler> T upgrade( Class<T> handlerClass )
    throws IOException, ServletException
  {
    return null;
  }

}
