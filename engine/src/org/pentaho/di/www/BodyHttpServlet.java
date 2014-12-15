package org.pentaho.di.www;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.PackageMessages;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class BodyHttpServlet extends BaseHttpServlet implements CartePluginInterface {

  private static final long serialVersionUID = 6576714217004890327L;
  private static final Encoder ENCODER = ESAPI.encoder();

  private final PackageMessages messages;

  public BodyHttpServlet() {
    messages = new PackageMessages( this.getClass() );
  }

  public void doGet( HttpServletRequest request, HttpServletResponse response ) throws IOException {
    if ( isJettyMode() && !request.getContextPath().startsWith( getContextPath() ) ) {
      return;
    }

    if ( log.isDebug() ) {
      logDebug( messages.getString( "Log.Execute" ) );
    }

    boolean useXML = "Y".equalsIgnoreCase( request.getParameter( "xml" ) );
    PrintWriter out = new PrintWriter( response.getOutputStream() );

    try {

      if ( useXML ) {
        startXml( response, out );
      } else {
        beginHtml( response, out );
      }

      generateBody( request, response, useXML );

    } catch ( Exception e ) {
      String st = ExceptionUtils.getFullStackTrace( e );
      if ( useXML ) {
        out.println( new WebResult( WebResult.STRING_ERROR, st ).getXML() );
      } else {
        out.println( "<p><pre>" );
        out.println( ENCODER.encodeForHTML( st ) );
        out.println( "</pre>" );
      }
    } finally {
      if ( !useXML ) {
        endHtml( out );
      }
      out.flush();
      out.close();
    }
  }

  protected void beginHtml( HttpServletResponse response, PrintWriter out ) throws IOException {
    response.setContentType( "text/html;charset=UTF-8" );
    out.println( "<HTML>" );
    out.println( "<HEAD>" );
    out.println( "<TITLE>" );
    out.println( ENCODER.encodeForHTML( getTitle() ) );
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

  abstract void generateBody( HttpServletRequest request, HttpServletResponse response, boolean useXML )
    throws Exception;

  @Override
  public String getService() {
    return getContextPath() + " (" + getTitle() + ")";
  }

  private String getTitle() {
    return messages.getString( "Title" );
  }

}
