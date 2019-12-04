/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.endpoints;

import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.FromTo;
import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileNotFoundException;
import org.pentaho.di.plugins.fileopensave.controllers.FileController;
import org.pentaho.di.plugins.fileopensave.controllers.RepositoryBrowserController;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by bmorrise on 5/12/17.
 */
public class FileBrowserEndpoint {

  private final RepositoryBrowserController repositoryBrowserController;
  private final FileController fileController;

  public FileBrowserEndpoint( RepositoryBrowserController repositoryBrowserController,
                              FileController fileController ) {
    this.repositoryBrowserController = repositoryBrowserController;
    this.fileController = fileController;
  }

  @GET
  @Path( "/loadDirectoryTree{filter : (/filter)?}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response loadDirectoryTree( @PathParam( "filter" ) String filter,
                                     @QueryParam( "connectionTypes" ) String connectionTypes ) {
    List<String> connectionTypeList = new ArrayList<>();
    if ( connectionTypes != null ) {
      Collections.addAll( connectionTypeList, connectionTypes.split( "," ) );
    }
    List<Tree> trees = fileController.load( filter, connectionTypeList );
    return Response.ok( trees ).build();
  }

  @POST
  @Path( "/getFiles" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response getFiles( @QueryParam( "filters" ) String filters,
                            @QueryParam( "useCache" ) Boolean useCache,
                            File file ) {
    useCache = useCache != null ? useCache : true;
    if ( !useCache ) {
      fileController.clearCache( file );
    }
    try {
      return Response.ok( fileController.getFiles( file, filters, useCache ) ).build();
    } catch ( FileException e ) {
      if ( e instanceof FileNotFoundException ) {
        return Response.status( Response.Status.NOT_FOUND ).build();
      }
    }
    return Response.status( Response.Status.NO_CONTENT ).build();
  }

  @POST
  @Path( "/getFile" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response getFile( File file ) {
    File result = fileController.getFile( file );
    if ( result == null ) {
      return Response.status( Response.Status.NOT_FOUND ).build();
    }
    return Response.ok( result ).build();
  }

  @POST
  @Path( "/delete" )
  @Produces( { MediaType.APPLICATION_JSON } )
  @Consumes( { MediaType.APPLICATION_JSON } )
  public Response deleteFiles( List<File> files ) {
    return Response.ok( fileController.delete( files ) ).build();
  }

  @PUT
  @Path( "/add" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response addFolder( File folder ) {
    return Response.ok( fileController.add( folder ) ).build();
  }

  @POST
  @Path( "/move" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response move( @QueryParam( "overwrite" ) Boolean overwrite,
                        @QueryParam( "path" ) String path,
                        FromTo fromTo ) {
    overwrite = overwrite != null ? overwrite : false;
    return Response.ok( fileController.moveFile( fromTo.getFrom(), fromTo.getTo(), path, overwrite ) ).build();
  }

  @POST
  @Path( "/rename" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response rename( @QueryParam( "overwrite" ) Boolean overwrite,
                          @QueryParam( "newPath" ) String newPath,
                          File file ) {
    overwrite = overwrite != null ? overwrite : false;
    return Response.ok( fileController.rename( file, newPath, overwrite ) ).build();
  }

  @POST
  @Path( "/copy" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response copy( @QueryParam( "overwrite" ) Boolean overwrite,
                        @QueryParam( "path" ) String path,
                        FromTo fromTo ) {
    overwrite = overwrite != null ? overwrite : false;
    return Response.ok( fileController.copyFile( fromTo.getFrom(), fromTo.getTo(), path, overwrite ) ).build();
  }

  @POST
  @Path( "/fileExists" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response fileExists( @QueryParam( "newPath" ) String newPath, File destDir ) {
    return Response.ok( fileController.fileExists( destDir, newPath ) ).build();
  }

  @POST
  @Path( "/getNewName" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response getNewName( @QueryParam( "newPath" ) String newPath, File destDir ) {
    return Response.ok( fileController.getNewName( destDir, newPath ) ).build();
  }

  @POST
  @Path( "/clearCache" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response clearCache( File file ) {
    return Response.ok( fileController.clearCache( file ) ).build();
  }

  /**
   * OLD ENDPOINTS
   **/
  @GET
  @Path( "/getActiveFileName" )
  public Response getActiveFileName() {
    String name = repositoryBrowserController.getActiveFileName();
    return Response.ok( Collections.singletonMap( "fileName", name ) ).build();
  }

  @GET
  @Path( "/loadRecent/{repo}/{id}" )
  public Response loadRecent( @PathParam( "repo" ) String repo, @PathParam( "id" ) String id ) {
    if ( repositoryBrowserController.openRecentFile( repo, id ) ) {
      return Response.ok().build();
    }
    return Response.status( Response.Status.NOT_FOUND ).build();
  }

  @GET
  @Path( "/saveFile/{path}/{name}/{fileName}/{override}" )
  public Response saveFile( @PathParam( "path" ) String path, @PathParam( "name" ) String name,
                            @PathParam( "fileName" ) String fileName,
                            @PathParam( "override" ) String override ) {
    boolean overwrite = override != null && override.toLowerCase().equals( "true" );
    if ( repositoryBrowserController.saveFile( path, name, fileName, overwrite ) ) {
      return Response.ok().build();
    }
    return Response.noContent().build();
  }

  @GET
  @Path( "/saveFile/{path}/{name}/{override}" )
  public Response saveFile( @PathParam( "path" ) String path, @PathParam( "name" ) String name,
                            @PathParam( "override" ) String override ) {
    boolean overwrite = override != null && override.toLowerCase().equals( "true" );
    if ( repositoryBrowserController.saveFile( path, name, "", overwrite ) ) {
      return Response.ok().build();
    }
    return Response.noContent().build();
  }

  @GET
  @Path( "/checkForSecurityOrDupeIssues/{path}/{name}/{fileName}/{override}" )
  public Response checkForSecurityOrDupeIssues( @PathParam( "path" ) String path, @PathParam( "name" ) String name,
                                                @PathParam( "fileName" ) String fileName,
                                                @PathParam( "override" ) String override ) {
    boolean overwrite = override != null && override.toLowerCase().equals( "true" );
    if ( repositoryBrowserController.checkForSecurityOrDupeIssues( path, name, fileName, overwrite ) ) {
      return Response.ok().build();
    }
    return Response.noContent().build();
  }

  @GET
  @Path( "/checkForSecurityOrDupeIssues/{path}/{name}/{override}" )
  public Response checkForSecurityOrDupeIssues( @PathParam( "path" ) String path, @PathParam( "name" ) String name,
                                                @PathParam( "override" ) String override ) {
    boolean overwrite = override != null && override.toLowerCase().equals( "true" );
    if ( repositoryBrowserController.checkForSecurityOrDupeIssues( path, name, "", overwrite ) ) {
      return Response.ok().build();
    }
    return Response.noContent().build();
  }

  @GET
  @Path( "/search/{path}/{filter}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response search( @PathParam( "path" ) String path, @PathParam( "filter" ) String filter ) {
    return Response.ok( repositoryBrowserController.search( path, filter ) ).build();
  }

  @GET
  @Path( "/updateRecentFiles/{oldPath}/{newPath}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response updateRecentFiles( @PathParam( "oldPath" ) String oldPath, @PathParam( "newPath" ) String newPath ) {
    return Response.ok( repositoryBrowserController.updateRecentFiles( oldPath, newPath ) ).build();
  }

  @GET
  @Path( "/recentSearches" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response recentSearches() {
    return Response.ok( repositoryBrowserController.getRecentSearches() ).build();
  }

  @GET
  @Path( "/storeRecentSearch/{recentSearch}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response storeRecentSearch( @PathParam( "recentSearch" ) String recentSearch ) {
    return Response.ok( repositoryBrowserController.storeRecentSearch( recentSearch ) ).build();
  }

  @GET
  @Path( "/currentRepo" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public Response getCurrentRepo() {
    return Response.ok( repositoryBrowserController.getCurrentRepo() ).build();
  }
}
