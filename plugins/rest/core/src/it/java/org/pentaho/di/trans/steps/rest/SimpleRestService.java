/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.trans.steps.rest;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * A simple rest service
 * 
 * @author vladimir.dolzhenko@gmail.com
 */
@Path( "/simple" )
public class SimpleRestService {
  @POST
  @Path( "/join" )
  @Produces( MediaType.TEXT_PLAIN )
  public String join( @MatrixParam( "limit" ) String limit, @QueryParam( "name" ) String name ) {
    return name + ":" + limit;
  }
}
