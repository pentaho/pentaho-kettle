/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020-2022 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class ConnectionEndpoints {

  private static Class<?> PKG = ConnectionEndpoints.class;

  private ConnectionManager connectionManager;

  public static final String HELP_URL =
    Const.getDocUrl( BaseMessages.getString( PKG, "ConnectionDialog.help.dialog.Help" ) );

  public ConnectionEndpoints() { // TODO wiring in spring beans.xml to only have one constructor ConnectionEndpoints( ConnectionManager connectionManager )
    this( ConnectionManager.getInstance() );
    this.connectionManager.setMetastoreSupplier( MetaStoreConst.getDefaultMetastoreSupplier() );
  }

  public ConnectionEndpoints( ConnectionManager connectionManager ) {
    this.connectionManager = connectionManager;
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
  public Response createConnection( ConnectionDetails connectionDetails, @QueryParam( "name" ) String name ) {
    boolean saved = connectionManager.save( connectionDetails );
    if ( saved ) {
      if ( !connectionDetails.getName().equals( name ) ) {
        connectionManager.delete( name );
      }
      /**
       * FIXME removing dependency on Spoon.java/SWT - SCENARIO NEWCONNECTION
       * TODO properly implement a javascript callback similar to:
       *    https://github.com/pentaho/pentaho-kettle/blob/9.3.0.4/plugins/connections/ui/src/main/javascript/app/components/intro/intro.component.js#L176
       * and then in the client ( Spoon or PUC) provide the js function such as:
       *    https://github.com/pentaho/pentaho-kettle/blob/9.3.0.4/plugins/connections/ui/src/main/java/org/pentaho/di/connections/ui/dialog/ConnectionDialog.java#L84-L89
       * and return name of newly created connection or no name and just success
       */
//      getSpoon().getShell().getDisplay().asyncExec( () -> getSpoon().refreshTree(
//        ConnectionFolderProvider.STRING_VFS_CONNECTIONS ) );
//      EngineMetaInterface engineMetaInterface = getSpoon().getActiveMeta();
//      if ( engineMetaInterface instanceof AbstractMeta ) {
//        ( (AbstractMeta) engineMetaInterface ).setChanged();
//      }
      return Response.ok().build();
    } else {
      return Response.serverError().build();
    }
  }

  @POST
  @Path( "/test" )
  @Consumes( { APPLICATION_JSON } )
  public Response testConnection( ConnectionDetails connectionDetails ) {
    VariableSpace space = Variables.getADefaultVariableSpace();
    connectionDetails.setSpace( space );
    boolean valid = false;
    try {
      valid = connectionManager.test( connectionDetails );
    } catch ( KettleException e ) {
      // NOTE: do nothing
    }

    if ( valid ) {
      return Response.ok().build();
    } else {
      return Response.status( Response.Status.BAD_REQUEST ).build();
    }
  }

  @GET
  @Path( "/help" )
  public Response help() {
    /**
     * FIXME removing dependency on Spoon.java/SWT - SCENARIO HELP
     * TODO properly implement a javascript callback similar to:
     *    https://github.com/pentaho/pentaho-kettle/blob/9.3.0.4/plugins/connections/ui/src/main/javascript/app/components/intro/intro.component.js#L176
     * and then in the client ( Spoon or PUC) provide the js function such as:
     *    https://github.com/pentaho/pentaho-kettle/blob/9.3.0.4/plugins/connections/ui/src/main/java/org/pentaho/di/connections/ui/dialog/ConnectionDialog.java#L84-L89
     * and return just String or JSON of HELP_URL or value of "ConnectionDialog.help.dialog.Help" in this function
     */
//    spoonSupplier.get().getShell().getDisplay().asyncExec( () ->
//      HelpUtils.openHelpDialog( spoonSupplier.get().getDisplay().getActiveShell(),
//        BaseMessages.getString( PKG, "ConnectionDialog.help.dialog.Title" ),
//        HELP_URL, BaseMessages.getString( PKG, "ConnectionDialog.help.dialog.Header" ) ) );
    return Response.ok().build();
  }

}
