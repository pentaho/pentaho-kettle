/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.ui.endpoints;

import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.ui.tree.ConnectionFolderProvider;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.osgi.metastore.locator.api.MetastoreLocator;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.function.Supplier;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class ConnectionEndpoints {

  private static Class<?> PKG = ConnectionEndpoints.class;
  public static final String ERROR_401 = "401";
  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;

  private ConnectionManager connectionManager;

  public ConnectionEndpoints( MetastoreLocator metastoreLocator ) {
    this.connectionManager = ConnectionManager.getInstance();
    this.connectionManager.setMetastoreSupplier( metastoreLocator::getMetastore );
  }

  @GET
  @Path( "/types" )
  @Produces( { APPLICATION_JSON } )
  public Response getTypes() {
    return Response.ok( connectionManager.getItems() ).build();
  }

  @GET
  @Path( "/connection/{scheme}" )
  @Produces( { APPLICATION_JSON } )
  public Response getFields( @PathParam( "scheme" ) String scheme ) {
    return Response.ok( connectionManager.createConnectionDetails( scheme ) ).build();
  }

  @GET
  @Path( "/connection" )
  @Consumes( { APPLICATION_JSON } )
  public Response getConnection( @QueryParam( "name" ) String name ) {
    return Response.ok( connectionManager.getConnectionDetails( name ) ).build();
  }

  @GET
  @Path( "/connection/exists" )
  @Consumes( { APPLICATION_JSON } )
  public Response getConnectionExists( @QueryParam( "name" ) String name ) {
    return Response.ok( String.valueOf( connectionManager.exists( name ) ) ).build();
  }

  @PUT
  @Path( "/connection" )
  @Consumes( { APPLICATION_JSON } )
  public Response createConnection( ConnectionDetails connectionDetails ) {
    boolean saved = connectionManager.save( connectionDetails );
    if ( saved ) {
      spoonSupplier.get().getShell().getDisplay().asyncExec( () -> spoonSupplier.get().refreshTree(
        ConnectionFolderProvider.STRING_VFS_CONNECTIONS ) );
      return Response.ok().build();
    } else {
      return Response.serverError().build();
    }
  }

  @POST
  @Path( "/test" )
  @Consumes( { APPLICATION_JSON } )
  public Response testConnection( ConnectionDetails connectionDetails ) {
    boolean valid = connectionManager.test( connectionDetails );
    if ( valid ) {
      return Response.ok().build();
    } else {
      return Response.status( Response.Status.BAD_REQUEST ).build();
    }
  }
}
