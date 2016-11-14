/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

import org.pentaho.di.core.exception.KettleAuthException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.repo.RepositoryConnectController;
import org.pentaho.di.ui.repo.model.ErrorModel;
import org.pentaho.di.ui.repo.model.LoginModel;
import org.pentaho.di.ui.repo.model.RepositoryModel;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Created by bmorrise on 10/20/16.
 */
public class RepositoryEndpoint {

  private static Class<?> PKG = RepositoryConnectController.class;
  public static final String ERROR_401 = "401";

  private RepositoryConnectController controller;

  public RepositoryEndpoint( RepositoryConnectController controller ) {
    this.controller = controller;
  }

  @POST
  @Path( "/login" )
  @Consumes( { APPLICATION_JSON } )
  public Response login( LoginModel loginModel ) {
    try {
      if ( controller.isRelogin() ) {
        controller.reconnectToRepository( loginModel.getUsername(), loginModel.getPassword() );
      } else {
        controller.connectToRepository( loginModel.getUsername(), loginModel.getPassword() );
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
    if ( controller.createRepository( model.getId(), controller.modelToMap( model ) ) ) {
      return Response.ok().build();
    } else {
      return Response.serverError()
        .entity( new ErrorModel( BaseMessages.getString( PKG, "RepositoryConnection.Error.InvalidServer" ) ) )
        .build();
    }
  }

}
