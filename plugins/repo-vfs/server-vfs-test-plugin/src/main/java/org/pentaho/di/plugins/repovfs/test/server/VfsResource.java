package org.pentaho.di.plugins.repovfs.test.server;

import org.pentaho.di.plugins.repovfs.test.server.service.FileInfo;
import org.pentaho.di.plugins.repovfs.test.server.service.VfsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path( "/vfs-test-plugin/api/v0/vfs" )
public class VfsResource {

  public record PathRequest( String path ) {}

  public record SrcDestRequest( String source, String destination ) {};

  public record WriteRequest( String path, String content, boolean append ) {};

  public record PathResponse( String path, String message ) {}

  public record SrcDestResponse( String source, String destination, String message ) {
    public SrcDestResponse( SrcDestRequest req, String msg ) {
      this( req.source, req.destination, msg );
    }
  }

  public record ReadResponse( String path, String content ) {};

  public record ErrorResponse( String error ) {};


  VfsService vfsService = new VfsService();


  @GET
  @Produces( MediaType.TEXT_PLAIN )
  @Path( "ping" )
  public Response ping() {
    return Response.ok().entity( "v0.2" ).build();
  }

  @GET
  @Path( "list" )
  @Consumes( MediaType.APPLICATION_JSON )
  @Produces( MediaType.APPLICATION_JSON )
  public Response listFiles( PathRequest request ) {
    if ( request.path == null ) {
      return missingArg( "path" );
    }
    try {
      List<FileInfo> files = vfsService.listFiles( request.path );
      return Response.ok( files ).build();
    } catch ( Exception e ) {
      return serverError( e );
    }
  }


  @GET
  @Path( "read" )
  @Consumes( MediaType.APPLICATION_JSON )
  @Produces( MediaType.APPLICATION_JSON )
  public Response readFile( PathRequest req ) {
    try {
      String content = vfsService.readFileAsBase64( req.path );
      var response = new ReadResponse( req.path, content );
      return Response.ok( response ).build();
    } catch ( Exception e ) {
      return serverError( e );
    }
  }

  @POST
  @Path( "write" )
  @Consumes( MediaType.APPLICATION_JSON )
  @Produces( MediaType.APPLICATION_JSON )
  public Response writeFile( WriteRequest req ) {
    try {
      vfsService.writeFileBase64( req.path, req.content, req.append );
      var response = new PathResponse( req.path, "File written" );
      return Response.ok( response ).build();
    } catch ( Exception e ) {
      return serverError( e );
    }
  }

  @POST
  @Path( "copy" )
  @Consumes( MediaType.APPLICATION_JSON )
  @Produces( MediaType.APPLICATION_JSON )
  public Response copyFile( SrcDestRequest req ) {
    try {
      vfsService.copy( req.source, req.destination );
      var response = new SrcDestResponse( req, "File copied" );
      return Response.ok( response ).build();
    } catch ( Exception e ) {
      return serverError( e );
    }
  }

  @POST
  @Path( "move" )
  @Consumes( MediaType.APPLICATION_JSON )
  @Produces( MediaType.APPLICATION_JSON )
  public Response moveFile( SrcDestRequest req ) {
    try {
      vfsService.move( req.source, req.destination );
      var response = new SrcDestResponse( req, "File moved" );
      return Response.ok( response ).build();
    } catch ( Exception e ) {
      return serverError( e );
    }
  }

  @POST
  @Path( "delete" )
  @Consumes( MediaType.APPLICATION_JSON )
  @Produces( MediaType.APPLICATION_JSON )
  public Response deleteFile( PathRequest req ) {
    try {
      boolean deleted = vfsService.delete( req.path );
      var msg = deleted ? "File deleted" : "File not found";
      var response = new PathResponse( req.path, msg );
      return Response.ok( response ).build();
    } catch ( Exception e ) {
      return serverError( e );
    }
  }

  @POST
  @Path( "mkdir" )
  @Consumes( MediaType.APPLICATION_JSON )
  @Produces( MediaType.APPLICATION_JSON )
  public Response createDirectory( PathRequest req ) {
    try {
      vfsService.createDirectory( req.path );
      var response = new PathResponse( req.path, "Directory created" );
      return Response.ok( response ).build();
    } catch ( Exception e ) {
      return serverError( e );
    }
  }

  @POST
  @Path( "create" )
  @Consumes( MediaType.APPLICATION_JSON )
  @Produces( MediaType.APPLICATION_JSON )
  public Response createEmptyFile( PathRequest req ) {
    try {
      vfsService.createEmptyFile( req.path );
      return Response.ok( new PathResponse( req.path, "file created" ) ).build();
    } catch ( Exception e ) {
      return serverError( e );
    }
  }

  @GET
  @Path( "info" )
  @Consumes( MediaType.APPLICATION_JSON )
  @Produces( MediaType.APPLICATION_JSON )
  public Response getFileInfo( PathRequest req ) {
    try {
      return vfsService.getFileInfo( req.path )
        .map( fileInfo -> Response.ok( fileInfo ).build() )
        .orElseGet( () -> fileNotFound( req.path ) );
    } catch ( Exception e ) {
      return serverError( e );
    }
  }

  @GET
  @Path( "exists" )
  @Consumes( MediaType.APPLICATION_JSON )
  @Produces( MediaType.APPLICATION_JSON )
  public Response fileExists( PathRequest request ) {
    try {

      if ( request.path == null ) {
        return missingArg( "path" );
      }
      boolean exists = vfsService.exists( request.path );

      Map<String, Object> response = new HashMap<>();
      response.put( "exists", exists );
      response.put( "path", request.path );
      return Response.ok( response ).build();
    } catch ( Exception e ) {
      return serverError( e );
    }
  }

  private Response fileNotFound( String path ) {
    return Response.status( Response.Status.NOT_FOUND )
      .entity( new ErrorResponse( "File not found: " + path ) )
      .build();
  }

  private Response serverError( Exception e ) {
    return Response.status( Response.Status.INTERNAL_SERVER_ERROR )
        .entity( new ErrorResponse( e.getMessage() ) )
        .build();
  }

  private Response missingArg( String arg ) {
    return Response.status( Response.Status.BAD_REQUEST )
      .entity( new ErrorResponse( String.format( "%s field is required in JSON body", arg ) ) )
      .build();
  }

}
