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
package org.eclipse.rap.rwt.internal.engine;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.protocol.ClientMessageConst;
import org.eclipse.rap.rwt.internal.service.UISessionImpl;
import org.eclipse.rap.rwt.service.UISession;


public class RWTClusterSupport implements Filter {

  @Override
  public void init( FilterConfig filterConfig ) {
  }

  @Override
  public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
    throws IOException, ServletException
  {
    beforeService( request );
    chain.doFilter( request, response );
    afterService( request );
  }

  @Override
  public void destroy() {
  }

  private static void beforeService( ServletRequest request ) {
    HttpSession httpSession = getHttpSession( request );
    if( httpSession != null ) {
      beforeService( httpSession, getConnectionId( request ) );
    }
  }

  private static void beforeService( HttpSession httpSession, String connectionId ) {
    UISessionImpl uiSession = UISessionImpl.getInstanceFromSession( httpSession, connectionId );
    if( uiSession != null ) {
      uiSession.setHttpSession( httpSession );
      attachApplicationContext( uiSession );
      PostDeserialization.runProcessors( uiSession );
    }
  }

  private static void attachApplicationContext( UISession uiSession ) {
    ServletContext servletContext = uiSession.getHttpSession().getServletContext();
    ApplicationContextImpl applicationContext = ApplicationContextImpl.getFrom( servletContext );
    ( ( UISessionImpl )uiSession ).setApplicationContext( applicationContext );
  }

  private static void afterService( ServletRequest request ) {
    HttpSession httpSession = getHttpSession( request );
    if( httpSession != null ) {
      afterService( httpSession, getConnectionId( request ) );
    }
  }

  private static void afterService( HttpSession httpSession, String connectionId ) {
    markSessionChanged( httpSession, connectionId );
  }

  private static void markSessionChanged( HttpSession httpSession, String connectionId ) {
    // If a session attribute changes, the servlet engine must be told to replicate the change.
    // Unfortunately the Servlet specs do not specify how this should be done.
    // The most common way is to call HttpSession.setAttribute() to flag the object as changed.
    // See http://wiki.eclipse.org/RAP/RWT_Cluster#Serializable_Session_Data
    // See also: J2EE clustering, Part 2, section Session-storage guidelines
    // http://java.sun.com/developer/technicalArticles/J2EE/clustering/
    UISessionImpl uiSession = UISessionImpl.getInstanceFromSession( httpSession, connectionId );
    if( uiSession != null ) {
      uiSession.attachToHttpSession();
    }
  }

  private static HttpSession getHttpSession( ServletRequest request ) {
    return ( ( HttpServletRequest )request ).getSession( false );
  }

  private static String getConnectionId( ServletRequest request ) {
    return request.getParameter( ClientMessageConst.CONNECTION_ID );
  }

}
