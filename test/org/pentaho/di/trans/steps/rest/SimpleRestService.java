package org.pentaho.di.trans.steps.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * A simple rest service
 * @author vladimir.dolzhenko@gmail.com
 */
@Path("/simple")
public class SimpleRestService {
  @GET
  @Path("/join")
  @Produces( MediaType.TEXT_PLAIN )
  public String join(@MatrixParam( "limit" ) String limit, @QueryParam( "name" ) String name){
    return name + ":" + limit;
  }
}
