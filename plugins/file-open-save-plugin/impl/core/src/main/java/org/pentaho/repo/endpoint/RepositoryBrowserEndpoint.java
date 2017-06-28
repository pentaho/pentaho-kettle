/*
 * Copyright 2017 Pentaho Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */

package org.pentaho.repo.endpoint;

import org.pentaho.repo.controller.RepositoryBrowserController;
import org.pentaho.repo.model.RepositoryDirectory;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;

/**
 * Created by bmorrise on 5/12/17.
 */
public class RepositoryBrowserEndpoint {

  private RepositoryBrowserController repositoryBrowserController;

  public RepositoryBrowserEndpoint( RepositoryBrowserController repositoryBrowserController ) {
    this.repositoryBrowserController = repositoryBrowserController;
  }

  @GET
  @Path( "/loadDirectoryTree" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response loadDirectoryTree() {
    List<RepositoryDirectory> repositoryDirectories = repositoryBrowserController.loadDirectoryTree();
    if ( repositoryDirectories != null ) {
      return Response.ok( repositoryDirectories ).build();
    }

    return Response.noContent().build();
  }

  @GET
  @Path( "/loadFile/{id}/{type}" )
  public Response loadFile( @PathParam( "id" ) String id, @PathParam( "type" ) String type ) {
    if ( repositoryBrowserController.loadFile( id, type ) ) {
      return Response.ok().build();
    }

    return Response.noContent().build();
  }

  @GET
  @Path( "/loadFiles/{id}" )
  public Response loadFile( @PathParam( "id" ) String id ) {
    return Response.ok( repositoryBrowserController.loadFiles( id ) ).build();
  }

  @GET
  @Path( "/getActiveFileName" )
  public Response getActiveFileName() {
    String name = repositoryBrowserController.getActiveFileName();
    return Response.ok( Collections.singletonMap( "fileName", name ) ).build();
  }

  @GET
  @Path( "/loadRecent/{repo}/{id}" )
  public Response loadRecent( @PathParam( "repo" ) String repo, @PathParam( "id" ) String id ) {
    repositoryBrowserController.openRecentFile( repo, id );

    return Response.ok().build();
  }

  @GET
  @Path( "/saveFile/{path}/{name}" )
  public Response saveFile( @PathParam( "path" ) String path, @PathParam( "name" ) String name ) {
    if ( repositoryBrowserController.saveFile( path, name ) ) {
      return Response.ok().build();
    }

    return Response.noContent().build();
  }

  @POST
  @Path( "/rename/{id}/{path}/{name}/{type}" )
  public Response rename( @PathParam( "id" ) String id, @PathParam( "path" ) String path,
                          @PathParam( "name" ) String name, @PathParam( "type" ) String type ) {
    return Response.ok( repositoryBrowserController.rename( id, path, name, type ) ).build();
  }

  @POST
  @Path( "/create/{parent}/{name}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response rename( @PathParam( "parent" ) String parent, @PathParam( "name" ) String name ) {
    RepositoryDirectory repositoryDirectory = repositoryBrowserController.create( parent, name );
    return Response.ok( repositoryDirectory ).build();
  }

  @DELETE
  @Path( "/remove/{id}/{type}" )
  public Response delete( @PathParam( "id" ) String id, @PathParam( "type" ) String type ) {
    if ( repositoryBrowserController.remove( id, type ) ) {
      return Response.ok().build();
    }

    return Response.noContent().build();
  }

  @GET
  @Path( "/recentFiles" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response recentFiles() {
    return Response.ok( repositoryBrowserController.getRecentFiles() ).build();
  }

  @GET
  @Path( "/recentSearches" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response recentSearches() { return Response.ok( repositoryBrowserController.getRecentSearches() ).build(); }

  @GET
  @Path( "/storeRecentSearch/{recentSearch}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response storeRecentSearch( @PathParam( "recentSearch" ) String recentSearch ) {
    return Response.ok( repositoryBrowserController.storeRecentSearch( recentSearch ) ).build();
  }
}
