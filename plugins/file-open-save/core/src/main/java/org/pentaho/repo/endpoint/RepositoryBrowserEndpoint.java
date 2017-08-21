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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.exception.KettleObjectExistsException;
import org.pentaho.di.core.exception.KettleTransException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.repo.controller.RepositoryBrowserController;
import org.pentaho.repo.model.RepositoryDirectory;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
  @Path( "/loadDirectoryTree{filter : (/filter)?}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response loadDirectoryTree( @PathParam( "filter" ) String filter ) {
    List<RepositoryDirectory> repositoryDirectories =
      Utils.isEmpty( filter ) ? repositoryBrowserController.loadDirectoryTree() :
        repositoryBrowserController.loadDirectoryTree( filter );
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
  @Path( "/rename/{id}/{path}/{newName}/{type}/{oldName}" )
  public Response rename( @PathParam( "id" ) String id, @PathParam( "path" ) String path,
                          @PathParam( "newName" ) String newName, @PathParam( "type" ) String type,
                          @PathParam( "oldName" ) String oldName ) {

    try {
      ObjectId objectId = repositoryBrowserController.rename( id, path, newName, type, oldName );
      if ( objectId != null ) {
        return Response.ok( objectId ).build();
      }
    } catch ( KettleObjectExistsException koee ) {
      return Response.status( Response.Status.CONFLICT ).build();
    } catch ( KettleTransException | KettleJobException ktje ) {
      return Response.status( Response.Status.NOT_ACCEPTABLE ).build();
    } catch ( KettleException ke ) {
      return Response.notModified().build();
    }

    return Response.notModified().build();
  }

  @POST
  @Path( "/create/{parent}/{name}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response rename( @PathParam( "parent" ) String parent, @PathParam( "name" ) String name ) {
    RepositoryDirectory repositoryDirectory = repositoryBrowserController.create( parent, name );
    if ( repositoryDirectory != null ) {
      return Response.ok( repositoryDirectory ).build();
    }

    return Response.status( Response.Status.UNAUTHORIZED ).build();
  }

  @DELETE
  @Path( "/remove/{id}/{name}/{path}/{type}" )
  public Response delete( @PathParam( "id" ) String id, @PathParam( "name" ) String name,
                          @PathParam( "path" ) String path, @PathParam( "type" ) String type ) {
    try {
      if ( repositoryBrowserController.remove( id, name, path, type ) ) {
        return Response.ok().build();
      }
    } catch ( KettleException ke ) {
      return Response.status( Response.Status.NOT_ACCEPTABLE ).build();
    }
    return Response.status( Response.Status.NOT_MODIFIED ).build();
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
