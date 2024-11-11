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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.owasp.encoder.Encode;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.PackageMessages;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public abstract class BodyHttpServlet extends BaseHttpServlet implements CartePluginInterface {

  private static final long serialVersionUID = 6576714217004890327L;
  private final PackageMessages messages;

  public BodyHttpServlet() {
    messages = new PackageMessages( this.getClass() );
  }

  protected boolean useXML( HttpServletRequest request ) {
    return "Y".equalsIgnoreCase( request.getParameter( "xml" ) );
  }

  public void doGet( HttpServletRequest request, HttpServletResponse response ) throws IOException {
    if ( isJettyMode() && !request.getContextPath().startsWith( getContextPath() ) ) {
      return;
    }

    if ( log.isDebug() ) {
      logDebug( messages.getString( "Log.Execute" ) );
    }

    boolean useXML = useXML( request );
    PrintWriter out = new PrintWriter( response.getOutputStream() );

    try {

      if ( useXML ) {
        startXml( response, out );
      } else {
        beginHtml( response, out );
      }

      WebResult result = generateBody( request, response, useXML );
      if ( result != null ) {
        out.println( result.getXML() );
      }

    } catch ( Exception e ) {
      String st = ExceptionUtils.getFullStackTrace( e );
      if ( useXML ) {
        out.println( new WebResult( WebResult.STRING_ERROR, st ).getXML() );
      } else {
        out.println( "<p><pre>" );
        out.println( Encode.forHtml( st ) );
        out.println( "</pre>" );
      }
    } finally {
      if ( !useXML ) {
        endHtml( out );
      }
      out.flush();
      IOUtils.closeQuietly( out );
    }
  }

  protected void beginHtml( HttpServletResponse response, PrintWriter out ) throws IOException {
    response.setContentType( "text/html;charset=UTF-8" );
    out.println( "<HTML>" );
    out.println( "<HEAD>" );
    out.println( "<TITLE>" );
    out.println( Encode.forHtml( getTitle() ) );
    out.println( "</TITLE>" );
    out.println( "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" );
    out.println( "</HEAD>" );
    out.println( "<BODY>" );
  }

  protected void endHtml( PrintWriter out ) {
    out.println( "<p>" );
    out.println( "</BODY>" );
    out.println( "</HTML>" );
  }

  protected void startXml( HttpServletResponse response, PrintWriter out ) throws IOException {
    response.setContentType( "text/xml" );
    response.setCharacterEncoding( Const.XML_ENCODING );
    out.print( XMLHandler.getXMLHeader( Const.XML_ENCODING ) );
  }

  abstract WebResult generateBody( HttpServletRequest request, HttpServletResponse response, boolean useXML )
    throws Exception;

  @Override
  public String getService() {
    return getContextPath() + " (" + getTitle() + ")";
  }

  private String getTitle() {
    return messages.getString( "Title" );
  }
}
