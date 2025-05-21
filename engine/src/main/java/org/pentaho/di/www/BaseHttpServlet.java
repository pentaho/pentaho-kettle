/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.www;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

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

}
