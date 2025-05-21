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

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.di.core.logging.LogChannelInterface;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BaseCartePluginTest {

  HttpServletRequest req = mock( HttpServletRequest.class );
  HttpServletResponse resp = mock( HttpServletResponse.class );
  LogChannelInterface log = mock( LogChannelInterface.class );
  CarteRequestHandler.WriterResponse writerResponse = mock( CarteRequestHandler.WriterResponse.class );
  CarteRequestHandler.OutputStreamResponse outputStreamResponse = mock( CarteRequestHandler.OutputStreamResponse.class );
  PrintWriter printWriter = mock( PrintWriter.class );
  jakarta.servlet.ServletOutputStream outputStream = mock( jakarta.servlet.ServletOutputStream.class );

  ArgumentCaptor<CarteRequestHandler.CarteRequest> carteReqCaptor = ArgumentCaptor.forClass( CarteRequestHandler.CarteRequest.class );

  BaseCartePlugin baseCartePlugin;

  @Before
  public void before() {
    baseCartePlugin = spy( new BaseCartePlugin() {
      @Override
      public void handleRequest( CarteRequest request ) {
      }

      @Override
      public String getContextPath() {
        return null;
      }
    } );
    baseCartePlugin.log = log;
  }

  @Test
  @SuppressWarnings( "deprecation" )
  public void testDoGet() throws Exception {
    baseCartePlugin.doGet( req, resp );
    // doGet should delegate to .service
    verify( baseCartePlugin ).service( req, resp );
  }

  @Test
  public void testService() throws Exception {
    when( req.getContextPath() ).thenReturn( "/Path" );
    when( baseCartePlugin.getContextPath() ).thenReturn( "/Path" );
    when( log.isDebug() ).thenReturn( true );

    baseCartePlugin.service( req, resp );

    verify( log ).logDebug( baseCartePlugin.getService() );
    verify( baseCartePlugin ).handleRequest( carteReqCaptor.capture() );

    CarteRequestHandler.CarteRequest carteRequest = carteReqCaptor.getValue();

    testCarteRequest( carteRequest );
    testCarteResponse( carteRequest.respond( 200 ) );
  }

  private void testCarteResponse( CarteRequestHandler.CarteResponse response ) throws IOException {
    when( resp.getWriter() ).thenReturn( printWriter );
    when( resp.getOutputStream() ).thenReturn( outputStream );

    response.with( "text/xml", writerResponse );

    verify( resp ).setContentType( "text/xml" );
    verify( writerResponse ).write( printWriter );

    response.with( "text/sgml", outputStreamResponse );

    verify( resp ).setContentType( "text/sgml" );
    verify( outputStreamResponse ).write( outputStream );

    response.withMessage( "Message" );
    verify( resp ).setContentType( "text/plain" );
    verify( printWriter ).println( "Message" );
  }

  private void testCarteRequest( CarteRequestHandler.CarteRequest carteRequest ) {
    when( req.getMethod() ).thenReturn( "POST" );
    when( req.getHeader( "Connection" ) ).thenReturn( "Keep-Alive" );
    when( req.getParameter( "param1" ) ).thenReturn( "val1" );
    when( req.getParameterNames() ).thenReturn( Collections.enumeration(
      Arrays.asList( "name1", "name2" ) ) );
    when( req.getParameterValues( any( String.class ) ) )
      .thenReturn( new String[] { "val" } );
    when( req.getHeaderNames() ).thenReturn( Collections.enumeration(
      Arrays.asList( "name1", "name2" ) ) );
    when( req.getHeaders( "name1" ) ).thenReturn(
      Collections.enumeration( ImmutableList.of( "val" ) ) );
    when( req.getHeaders( "name2" ) ).thenReturn(
      Collections.enumeration( ImmutableList.of( "val" ) ) );

    assertThat( carteRequest.getMethod(), is( "POST" ) );
    assertThat( carteRequest.getHeader( "Connection" ), is( "Keep-Alive" ) );
    assertThat( carteRequest.getParameter( "param1" ), is( "val1" ) );

    checkMappedVals( carteRequest.getParameters() );
    checkMappedVals( carteRequest.getHeaders() );
  }

  private void checkMappedVals( Map<String, Collection<String>> map ) {
    assertThat( map.size(), is( 2 ) );
    Collection<String> name1Params = map.get( "name1" );
    Collection<String> name2Params = map.get( "name2" );
    assertThat( name1Params.contains( "val" ), is( true ) );
    assertThat( name2Params.contains( "val" ), is( true ) );
    assertThat( name1Params.size() == 1 && name2Params.size() == 1, is( true ) );
  }

  @Test
  public void testGetService() {
    when( baseCartePlugin.getContextPath() )
      .thenReturn( "/Path" );
    assertThat( baseCartePlugin.getService().startsWith( "/Path" ), is( true ) );
  }
}
