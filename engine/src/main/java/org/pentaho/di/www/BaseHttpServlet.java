/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.www;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;


public class BaseHttpServlet extends HttpServlet {

  protected static final long serialVersionUID = -1348342810327662788L;

  protected TransformationMap transformationMap;
  protected JobMap jobMap;
  protected SocketRepository socketRepository;
  protected List<SlaveServerDetection> detections;

  private boolean jettyMode = false;

  protected LogChannelInterface log = new LogChannel( "Servlet" );

  public String convertContextPath( String contextPath ) {
    if ( jettyMode ) {
      return contextPath;
    }
    return contextPath.substring( contextPath.lastIndexOf( "/" ) + 1 );
  }

  public BaseHttpServlet() {
  }

  public BaseHttpServlet( TransformationMap transformationMap ) {
    this.transformationMap = transformationMap;
    this.jettyMode = true;
  }

  public BaseHttpServlet( JobMap jobMap ) {
    this.jobMap = jobMap;
    this.jettyMode = true;
  }

  public BaseHttpServlet( TransformationMap transformationMap, JobMap jobMap ) {
    this.transformationMap = transformationMap;
    this.jobMap = jobMap;
    this.jettyMode = true;
  }

  public BaseHttpServlet( TransformationMap transformationMap, SocketRepository socketRepository ) {
    this.transformationMap = transformationMap;
    this.socketRepository = socketRepository;
    this.jettyMode = true;
  }

  public BaseHttpServlet( JobMap jobMap, SocketRepository socketRepository ) {
    this.jobMap = jobMap;
    this.socketRepository = socketRepository;
    this.jettyMode = true;
  }

  public BaseHttpServlet( List<SlaveServerDetection> detections ) {
    this( detections, true );
  }

  public BaseHttpServlet( List<SlaveServerDetection> detections, boolean isJetty ) {
    this.detections = detections;
    this.jettyMode = isJetty;
  }

  protected boolean useXML( HttpServletRequest request ) {
    return "Y".equalsIgnoreCase( request.getParameter( "xml" ) );
  }
  

  /**
   * Sets contentType and encoding headers on the response. Also can optionally print the <?xml ...> declaration line.
   * @param isXml A boolean indicating if the header should be written for xml or html
   * @param response The HttpServlet Response to set the content type and character encoding
   * @param outputWriter When a non-null value is passed and isXml is true, the <?xml> declaration is written to the Writer
   * @param defaultEncoding A default encoding that should be used if no others are provided.
   * @return  Returns the encoding that was used
   * @throws IOException
   */
  protected String contentTypeAndHeader(boolean isXml, HttpServletResponse response, Writer outputWriter, String defaultEncoding  ) throws IOException {
    if( isXml ) {
      String xmlEncoding = Const.NVL( defaultEncoding, Const.XML_ENCODING );
      response.setContentType( MediaType.APPLICATION_XML );
      response.setCharacterEncoding( xmlEncoding );
      if( outputWriter != null ) {
        outputWriter.append( XMLHandler.getXMLHeader( xmlEncoding ) );
      }
      return xmlEncoding;
    } else {
      String encoding = System.getProperty( "KETTLE_DEFAULT_SERVLET_ENCODING", defaultEncoding );
      if ( encoding != null && !Utils.isEmpty( encoding.trim() ) ) {
        response.setCharacterEncoding( encoding );
        response.setContentType( "text/html; charset=" + encoding );
      } else {
        response.setContentType( MediaType.TEXT_HTML );
      }
      return encoding;
    }
  }
  
  protected void doPut( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
    IOException {
    doGet( request, response );
  }

  protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
    IOException {
    doGet( request, response );
  }

  protected void doDelete( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
    doGet( req, resp );
  }

  public TransformationMap getTransformationMap() {
    if ( transformationMap == null ) {
      return CarteSingleton.getInstance().getTransformationMap();
    }
    return transformationMap;
  }

  public JobMap getJobMap() {
    if ( jobMap == null ) {
      return CarteSingleton.getInstance().getJobMap();
    }
    return jobMap;
  }

  public SocketRepository getSocketRepository() {
    if ( socketRepository == null ) {
      return CarteSingleton.getInstance().getSocketRepository();
    }
    return socketRepository;
  }

  public List<SlaveServerDetection> getDetections() {
    return detections;
  }

  public boolean isJettyMode() {
    return jettyMode;
  }

  public void setJettyMode( boolean jettyMode ) {
    this.jettyMode = jettyMode;
  }

  public void logMinimal( String s ) {
    log.logMinimal( s );
  }

  public void logBasic( String s ) {
    log.logBasic( s );
  }

  public void logError( String s ) {
    log.logError( s );
  }

  public void logError( String s, Throwable e ) {
    log.logError( s, e );
  }

  public void logBasic( String s, Object... arguments ) {
    log.logBasic( s, arguments );
  }

  public void logDetailed( String s, Object... arguments ) {
    log.logDetailed( s, arguments );
  }

  public void logError( String s, Object... arguments ) {
    log.logError( s, arguments );
  }

  public void logDetailed( String s ) {
    log.logDetailed( s );
  }

  public void logDebug( String s ) {
    log.logDebug( s );
  }

  public void logRowlevel( String s ) {
    log.logRowlevel( s );
  }

  public void setup( TransformationMap transformationMap, JobMap jobMap, SocketRepository socketRepository,
    List<SlaveServerDetection> detections ) {
    this.transformationMap = transformationMap;
    this.jobMap = jobMap;
    this.socketRepository = socketRepository;
    this.detections = detections;
  }

  /**
   * Read the request parameters from the request and add it to the VariableSpace
   * @param request Input HttpServletRequest
   * @return VariableSpace Return the populated VariableSpace
   */
  protected VariableSpace getPopulatedVariableSpaceFromRequest( HttpServletRequest request, VariableSpace variableSpace ) {
    Enumeration<?> parameterNames = request.getParameterNames();
    while ( parameterNames.hasMoreElements() ) {
      String parameter = (String) parameterNames.nextElement();
      String[] values = request.getParameterValues( parameter );
      variableSpace.setVariable( parameter, values != null && values.length > 0 ? values[0] : "" );
    }
    return variableSpace;
  }

  protected void clearBowlCache( Repository repository ) {
    clearBowlCache( repository == null ? DefaultBowl.getInstance() : repository.getBowl() );
  }

  protected void clearBowlCache( Bowl bowl ) {
    if ( bowl != null ) {
      bowl.clearCache();
    }
  }

}
