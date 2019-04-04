/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.getfields.endpoint;

import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.getfields.types.json.JsonSampler;
import org.pentaho.getfields.types.json.node.Node;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by bmorrise on 8/17/18.
 */
public class GetFieldsEndpoint {

  private static final String JSON = "json";

  @GET
  @Path( "/sample/{path}/{type}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response loadFile( @PathParam( "path" ) String path, @PathParam( "type" ) String type ) {
    switch ( type ) {
      case JSON:
        return loadJson( path );
    }

    return null;
  }

  private Response loadJson( String path ) {
    JsonSampler jsonSampler = new JsonSampler();
    Node node = null;
    try {
      node = jsonSampler.sample( path );
    } catch ( KettleFileException e ) { // Cannot find file OR no permissions
      return Response.status( Response.Status.NOT_FOUND ).build();
    } catch ( IOException e ) { // Invalid JSON structure
      return Response.status( Response.Status.NOT_ACCEPTABLE ).build();
    }

    return Response.ok( node ).build();
  }

}
