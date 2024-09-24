/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.www;

import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.CarteServlet;
import org.pentaho.di.core.util.ExecutorUtil;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;

@CarteServlet( id = "StopCarteServlet", name = "StopCarteServlet" )
public class StopCarteServlet extends BaseHttpServlet implements CartePluginInterface {

  private static Class<?> PKG = StopCarteServlet.class;

  private static final long serialVersionUID = -5459379367791045161L;
  public static final String CONTEXT_PATH = "/kettle/stopCarte";
  public static final String REQUEST_ACCEPTED = "request_accepted";
  private final DelayedExecutor delayedExecutor;

  public StopCarteServlet() {
    this( new DelayedExecutor() );
  }

  public StopCarteServlet( DelayedExecutor delayedExecutor ) {
    this.delayedExecutor = delayedExecutor;
  }

  @Override
  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  @Override
  public void doGet( HttpServletRequest request, HttpServletResponse response ) throws IOException {
    if ( isJettyMode() && !request.getContextPath().startsWith( CONTEXT_PATH ) ) {
      return;
    }

    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "StopCarteServlet.shutdownRequest" ) );
    }

    response.setStatus( HttpServletResponse.SC_OK );
    boolean useXML = "Y".equalsIgnoreCase( request.getParameter( "xml" ) );

    if ( useXML ) {
      response.setContentType( "text/xml" );
      response.setCharacterEncoding( Const.XML_ENCODING );
    } else {
      response.setContentType( "text/html" );
    }

    PrintStream out = new PrintStream( response.getOutputStream() );
    final Carte carte = CarteSingleton.getCarte();
    if ( useXML ) {
      out.print( XMLHandler.getXMLHeader( Const.XML_ENCODING ) );
      out.print( XMLHandler.addTagValue( REQUEST_ACCEPTED, carte != null ) );
      out.flush();
    } else {
      out.println( "<HTML>" );
      out.println(
          "<HEAD><TITLE>" + BaseMessages.getString( PKG, "StopCarteServlet.shutdownRequest" ) + "</TITLE></HEAD>" );
      out.println( "<BODY>" );
      out.println( "<H1>" + BaseMessages.getString( PKG, "StopCarteServlet.status.label" ) +  "</H1>" );
      out.println( "<p>" );
      if ( carte != null ) {
        out.println( BaseMessages.getString( PKG, "StopCarteServlet.shutdownRequest.status.ok" ) );
      } else {
        out.println( BaseMessages.getString( PKG, "StopCarteServlet.shutdownRequest.status.notFound" ) );
      }
      out.println( "</p>" );
      out.println( "</BODY>" );
      out.println( "</HTML>" );
      out.flush();
    }
    if ( carte != null ) {
      delayedExecutor.execute( new Runnable() {
        @Override
        public void run() {
          carte.getWebServer().stopServer();
          exitJVM( 0 );
        }
      }, 1000 );
    }
  }

  @Override
  public String toString() {
    return BaseMessages.getString( PKG, "StopCarteServlet.description" );
  }

  @Override
  public String getContextPath() {
    return CONTEXT_PATH;
  }

  public static class DelayedExecutor {
    public void execute( final Runnable runnable, final long delay ) {
      ExecutorUtil.getExecutor().execute( new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep( delay );
          } catch ( InterruptedException e ) {
            // Ignore
          }
          runnable.run();
        }
      } );
    }
  }

  private static final void exitJVM( int status ) {
    System.exit( status );
  }
}

