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


package org.pentaho.di.trans.steps.rest;

import com.sun.net.httpserver.HttpServer;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * User: Dzmitry Stsiapanau Date: 11/29/13 Time: 3:36 PM
 */

@Path( "/restTest" )
public class RestServices {

  @GET
  @Path( "/restNoContentAnswer" )
  public void getNoContentAnswer( String someStr ) {
  }

  public static void main( String[] args ) throws IOException {
    String param = args.length > 0 ? args[0] : RestIT.HTTP_LOCALHOST_9998;
    HttpServer server = HttpServerFactory.create( param );
    server.start();

    System.out.println( "Server running" );
    System.out.println( "Visit: " + param );
    System.out.println( "Hit return to stop..." );
    System.in.read();
    System.out.println( "Stopping server" );
    server.stop( 0 );
    System.out.println( "Server stopped" );
  }

}
