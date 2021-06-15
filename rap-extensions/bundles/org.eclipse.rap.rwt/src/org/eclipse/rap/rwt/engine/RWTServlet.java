/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing implementation
 *    Frank Appel - replaced singletons and static fields (Bug 337787)
 ******************************************************************************/
package org.eclipse.rap.rwt.engine;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.CONNECTION_ID;
import static org.eclipse.rap.rwt.internal.util.HTTP.CONTENT_TYPE_JSON;
import static org.eclipse.rap.rwt.internal.util.HTTP.METHOD_POST;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;
import org.eclipse.rap.rwt.internal.service.ServiceManagerImpl;
import org.eclipse.rap.rwt.internal.service.ServiceStore;
import org.eclipse.rap.rwt.internal.service.StartupJson;
import org.eclipse.rap.rwt.internal.service.UISessionBuilder;
import org.eclipse.rap.rwt.internal.service.UISessionImpl;
import org.eclipse.rap.rwt.internal.util.HTTP;
import org.eclipse.rap.rwt.service.ServiceHandler;


/**
 * The RWT servlet. This servlet receives all requests to a RAP application.
 * <p>
 * Usually, you only have to register this servlet manually in a traditional web
 * application, i.e. without OSGi.
 * </p>
 * <p>
 * In a traditional web application (without OSGi), this servlet must be
 * registered in the application's deployment descriptor like shown below. Note
 * that the RWT servlet has to be registered for every entrypoint of the
 * application.
 * </p>
 *
 * <pre>
 * &lt;context-param&gt;
 *   &lt;param-name&gt;org.eclipse.rap.applicationConfiguration&lt;/param-name&gt;
 *   &lt;param-value&gt;com.example.HelloWorldConfiguration&lt;/param-value&gt;
 * &lt;/context-param&gt;
 *
 * &lt;listener&gt;
 *   &lt;listener-class&gt;org.eclipse.rap.rwt.engine.RWTServletContextListener&lt;/listener-class&gt;
 * &lt;/listener&gt;
 *
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;rwtServlet&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;org.eclipse.rap.rwt.engine.RWTServlet&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 *
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;rwtServlet&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;/example&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 *
 * <pre>
 *
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RWTServlet extends HttpServlet {

  private ApplicationContextImpl applicationContext;

  @Override
  public String getServletInfo() {
    return "RWT Servlet";
  }

  @Override
  public void init() throws ServletException {
    ServletContext servletContext = getServletContext();
    applicationContext = ApplicationContextImpl.getFrom( servletContext );
  }

  @Override
  public void doGet( HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException
  {
    handleRequest( request, response );
  }

  @Override
  public void doPost( HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException
  {
    handleRequest( request, response );
  }

  private void handleRequest( HttpServletRequest request, HttpServletResponse response )
    throws IOException, ServletException
  {
    if( !applicationContext.allowsRequests() ) {
      response.sendError( SC_SERVICE_UNAVAILABLE );
    } else if( request.getPathInfo() == null ) {
      // /context/servlet: no extra path info after servlet name
      handleValidRequest( request, response );
    } else if( "/".equals( request.getPathInfo() ) && "".equals( request.getServletPath() ) ) {
      // /context/: root servlet, in this case path info "/" is ok
      handleValidRequest( request, response );
    } else {
      response.sendError( SC_NOT_FOUND );
    }
  }

  private void handleValidRequest( HttpServletRequest request, HttpServletResponse response )
    throws IOException, ServletException
  {
    ServiceContext context = new ServiceContext( request, response, applicationContext );
    context.setServiceStore( new ServiceStore() );
    ContextProvider.setContext( context );
    try {
      prepareUISession( context );
      if( isUIRequest( request ) || isServiceHandlerRequest( request ) ) {
        getServiceHandler().service( request, response );
      } else {
        sendStartupContent( request, response );
      }
    } finally {
      ContextProvider.disposeContext();
    }
  }

  private ServiceHandler getServiceHandler() {
    return applicationContext.getServiceManager().getHandler();
  }

  private static boolean isUIRequest( HttpServletRequest request ) {
    return    METHOD_POST.equals( request.getMethod() )
           && isContentTypeValid( request )
           && !isServiceHandlerRequest( request );
  }

  private static boolean isServiceHandlerRequest( HttpServletRequest request ) {
    return request.getParameter( ServiceManagerImpl.REQUEST_PARAM ) != null;
  }

  private static boolean isContentTypeValid( ServletRequest request ) {
    String contentType = request.getContentType();
    return contentType != null && contentType.startsWith( CONTENT_TYPE_JSON );
  }

  private void sendStartupContent( HttpServletRequest request, HttpServletResponse response )
    throws IOException
  {
    String accept = request.getHeader( HTTP.HEADER_ACCEPT );
    if( accept != null && accept.contains( HTTP.CONTENT_TYPE_JSON ) ) {
      StartupJson.send( response );
    } else {
      applicationContext.getStartupPage().send( response );
    }
  }

  private static void prepareUISession( ServiceContext context ) {
    HttpServletRequest request = context.getRequest();
    HttpSession httpSession = request.getSession( true );
    String connectionId = request.getParameter( CONNECTION_ID );
    if( connectionId != null ) {
      context.setUISession( UISessionImpl.getInstanceFromSession( httpSession, connectionId ) );
    } else if( isUIRequest( request ) ) {
      context.setUISession( new UISessionBuilder( context ).buildUISession() );
    }
  }

}
