/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.repo.endpoints;

import org.json.simple.JSONObject;
import org.pentaho.di.core.exception.KettleAuthException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.repo.model.ErrorModel;
import org.pentaho.di.ui.repo.model.LoginModel;
import org.pentaho.di.ui.repo.model.RepositoryModel;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Created by bmorrise on 10/20/16.
 */
public class RepositoryEndpoint {

  private static Class<?> PKG = RepositoryEndpoint.class;
  public static final String ERROR_401 = "401";

  private RepositoryConnectController controller;

  public RepositoryEndpoint( RepositoryConnectController controller ) {
    this.controller = controller;
  }

  @GET
  @Path( "/help" )
  public Response help() {
    return Response.ok( controller.help() ).build();
  }

  @GET
  @Path( "/user" )
  public Response user() {
    return Response.ok( controller.getCurrentUser() ).build();
  }


  @GET
  @Path( "/connection/create" )
  public Response createConnection() {
    return Response.ok( controller.createConnection() ).build();
  }

  @POST
  @Path( "/connection/edit" )
  public Response editConnection( String database ) {
    return Response.ok( controller.editDatabaseConnection( database ) ).build();
  }

  @POST
  @Path( "/connection/delete" )
  public Response deleteConnection( String database ) {
    return Response.ok( controller.deleteDatabaseConnection( database ) ).build();
  }

  @POST
  @Path( "/login" )
  @Consumes( { APPLICATION_JSON } )
  public Response login( LoginModel loginModel ) {
    try {
      if ( controller.isRelogin() ) {
        controller
          .reconnectToRepository( loginModel.getRepositoryName(), loginModel.getUsername(), loginModel.getPassword() );
      } else {
        controller
          .connectToRepository( loginModel.getRepositoryName(), loginModel.getUsername(), loginModel.getPassword() );
      }
      return Response.ok().build();
    } catch ( Exception e ) {
      if ( e.getMessage().contains( ERROR_401 ) || e instanceof KettleAuthException ) {
        return Response.serverError()
          .entity( new ErrorModel( BaseMessages.getString( PKG, "RepositoryConnection.Error.InvalidCredentials" ) ) )
          .build();
      } else {
        return Response.serverError()
          .entity( new ErrorModel( BaseMessages.getString( PKG, "RepositoryConnection.Error.InvalidServer" ) ) )
          .build();
      }
    }
  }

  @POST
  @Path( "/add" )
  @Consumes( { APPLICATION_JSON } )
  public Response add( RepositoryModel model ) {
    if ( controller.createRepository( model.getId(), controller.modelToMap( model ) ) != null ) {
      return Response.ok().build();
    } else {
      return Response.serverError()
        .entity( new ErrorModel( BaseMessages.getString( PKG, "RepositoryConnection.Error.InvalidServer" ) ) )
        .build();
    }
  }

  @POST
  @Path( "/update" )
  @Consumes( { APPLICATION_JSON } )
  public Response update( RepositoryModel model ) {
    if ( controller.updateRepository( model.getId(), controller.modelToMap( model ) ) ) {
      return Response.ok().build();
    } else {
      return Response.serverError()
        .entity( new ErrorModel( BaseMessages.getString( PKG, "RepositoryConnection.Error.InvalidServer" ) ) )
        .build();
    }
  }

  @GET
  @Path( "/list" )
  @Produces( { APPLICATION_JSON } )
  public Response repositories() {
    return Response.ok( controller.getRepositories() ).build();
  }

  @GET
  @Path( "/find/{repo : .+}" )
  @Produces( { APPLICATION_JSON } )
  public Response repository( @PathParam( "repo" ) String repo ) {
    return Response.ok( controller.getRepository( repo ) ).build();
  }

  @POST
  @Path( "/default/set" )
  @Consumes( { APPLICATION_JSON } )
  public Response setDefault( RepositoryModel model ) {
    return Response.ok( controller.setDefaultRepository( model.getDisplayName() ) ).build();
  }

  @POST
  @Path( "/default/clear" )
  @Consumes( { APPLICATION_JSON } )
  public Response setDefault() {
    return Response.ok( controller.clearDefaultRepository() ).build();
  }

  @POST
  @Path( "/duplicate" )
  @Consumes( { APPLICATION_JSON } )
  public Response duplicate( RepositoryModel model ) {
    return Response.ok( controller.checkDuplicate( model.getDisplayName() ) ).build();
  }

  @POST
  @Path( "/remove" )
  @Consumes( { APPLICATION_JSON } )
  public Response delete( RepositoryModel model ) {
    return Response.ok( controller.deleteRepository( model.getDisplayName() ) ).build();
  }

  @GET
  @Path( "/types" )
  @Consumes( { APPLICATION_JSON } )
  public Response types() {
    return Response.ok( controller.getPlugins() ).build();
  }

  @GET
  @Path( "/databases" )
  @Produces( { APPLICATION_JSON } )
  public Response databases() {
    return Response.ok( controller.getDatabases() ).build();
  }

  @GET
  @Path( "/browse" )
  @Produces( { APPLICATION_JSON } )
  public Response browse() {
    String path = "/";
    try {
      path = controller.browse();
    } catch ( Exception e ) {
      // Do nothing
    }
    JSONObject jsonObject = new JSONObject();
    jsonObject.put( "path", path );
    return Response.ok( jsonObject.toJSONString() ).build();
  }
}
