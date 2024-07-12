/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.rest;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import java.io.IOException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

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
    HttpServer server = JdkHttpServerFactory.create( param );
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
