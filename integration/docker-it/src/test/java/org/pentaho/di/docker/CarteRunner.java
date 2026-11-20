/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.di.docker;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;

/**
 * Runs the KTR through the Carte REST API: addTrans, startTrans, then transStatus until the run ends.
 */
final class CarteRunner implements KtrRunner {

  private static final Duration RUN_TIMEOUT = Duration.ofMinutes( 10 );
  private static final Duration POLL_INTERVAL = Duration.ofSeconds( 1 );
  private static final Set<String> RUNNING_STATES = Set.of(
    "Waiting", "Preparing executing", "Initializing", "Running", "Halting", "Paused" );

  private final String baseUrl;
  private final String authorization;
  private final HttpClient http = HttpClient.newHttpClient();

  CarteRunner( String baseUrl, String user, String password ) {
    this.baseUrl = baseUrl;
    this.authorization = "Basic " + Base64.getEncoder()
      .encodeToString( ( user + ":" + password ).getBytes( StandardCharsets.UTF_8 ) );
  }

  /**
   * Waits until the Carte endpoint answers. It returns false when the timeout ends first.
   */
  boolean awaitReady( Duration timeout ) throws Exception {
    long deadline = System.nanoTime() + timeout.toNanos();
    while ( System.nanoTime() < deadline ) {
      try {
        if ( send( "GET", "status/?xml=Y", null ).contains( "<serverstatus>" ) ) {
          return true;
        }
      } catch ( java.io.IOException ignored ) {
        // The endpoint is not up yet.
      }
      Thread.sleep( POLL_INTERVAL.toMillis() );
    }
    return false;
  }


  @Override
  public ExecutionResult run( KtrCase ktr, byte[] xml ) throws Exception {
    String name = URLEncoder.encode( transformationName( xml ), StandardCharsets.UTF_8 );
    String added = send( "POST", "addTrans/?name=" + name + "&xml=Y", wrap( xml ) );
    if ( !added.contains( "<result>OK</result>" ) ) {
      throw new IllegalStateException( "Carte addTrans failed\n" + added );
    }
    String started = send( "GET", "startTrans/?name=" + name + "&xml=Y", null );
    if ( !started.contains( "<result>OK</result>" ) ) {
      return new ExecutionResult( false, "carte startTrans failed\n" + started );
    }
    try {
      return waitForEnd( name );
    } finally {
      send( "GET", "removeTrans/?name=" + name + "&xml=Y", null );
    }
  }

  private ExecutionResult waitForEnd( String name ) throws Exception {
    long deadline = System.nanoTime() + RUN_TIMEOUT.toNanos();
    while ( true ) {
      String statusXml = send( "GET", "transStatus/?name=" + name + "&xml=Y", null );
      Document status = parse( statusXml );
      String state = text( status, "status_desc" );
      if ( !RUNNING_STATES.contains( state ) ) {
        String errors = text( status, "nr_errors" );
        boolean success = "Finished".equals( state ) && "0".equals( errors );
        return new ExecutionResult( success, "carte status " + state + ", nr_errors " + errors + "\n" + statusXml );
      }
      if ( System.nanoTime() > deadline ) {
        return new ExecutionResult( false, "carte status " + state + " after " + RUN_TIMEOUT + "\n" + statusXml );
      }
      Thread.sleep( POLL_INTERVAL.toMillis() );
    }
  }

  private String send( String method, String path, String body ) throws Exception {
    HttpRequest.Builder request = HttpRequest.newBuilder( URI.create( baseUrl + path ) )
      .header( "Authorization", authorization )
      .timeout( Duration.ofMinutes( 2 ) );
    if ( body == null ) {
      request.GET();
    } else {
      request.header( "Content-Type", "text/xml" ).POST( HttpRequest.BodyPublishers.ofString( body ) );
    }
    HttpResponse<String> response = http.send( request.build(), HttpResponse.BodyHandlers.ofString() );
    return response.body();
  }

  /**
   * Carte needs the transformation inside a transformation_configuration element.
   */
  private static String wrap( byte[] xml ) {
    String ktr = new String( xml, StandardCharsets.UTF_8 ).replaceFirst( "^\\s*<\\?xml[^>]*\\?>", "" );
    return "<transformation_configuration>" + ktr
      + "<execution_configuration><log_level>Basic</log_level></execution_configuration>"
      + "</transformation_configuration>";
  }

  /**
   * Carte registers the transformation under the name in its info element, not under the name in the request.
   */
  private static String transformationName( byte[] xml ) throws Exception {
    return text( parse( new String( xml, StandardCharsets.UTF_8 ) ), "name" );
  }

  private static Document parse( String xml ) throws Exception {
    return DocumentBuilderFactory.newInstance().newDocumentBuilder()
      .parse( new ByteArrayInputStream( xml.getBytes( StandardCharsets.UTF_8 ) ) );
  }

  private static String text( Document doc, String tag ) {
    var nodes = doc.getElementsByTagName( tag );
    return nodes.getLength() == 0 ? "" : nodes.item( 0 ).getTextContent().trim();
  }
}
