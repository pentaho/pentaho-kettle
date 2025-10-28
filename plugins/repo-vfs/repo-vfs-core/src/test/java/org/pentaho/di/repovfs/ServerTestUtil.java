package org.pentaho.di.repovfs;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import static org.pentaho.di.repovfs.util.RepoTestFileTreeDto.createFileTreeDto;
import static org.pentaho.di.repovfs.util.RepoTestFileTreeDto.createFileXml;

import org.pentaho.platform.util.RepositoryPathEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.TimeToLive;
import org.mockserver.matchers.Times;
import org.mockserver.model.MediaType;
import org.mockserver.model.Parameter;

public class ServerTestUtil {

   public static void setUpBasicBrowse( ClientAndServer server ) throws Exception {
    String rootResp = readFileAsString( "/browse/root_children_resp.xml" );
    String publicResp = readFileAsString( "/browse/browse_public_resp.xml" );
    String publicProjectingResp = readFileAsString( "/browse/browse_public_projecting_resp.xml" );
    String publicProjectingSomethingResp = readFileAsString( "/browse/browse_public_projecting_something_resp.xml" );

    // low-priority catch-all for missed tree calls
    server.when( request().withPath( "/pentaho/api/repo/files/.*/tree" ),
      Times.unlimited(), TimeToLive.unlimited(), -10 )
      .respond( response().withStatusCode( 204 ) );

    server.when( request().withPath( "/pentaho/api/repo/files/tree" ).withQueryStringParameters( new Parameter( "depth",
      "1" ) ) )
      .respond( response().withBody( rootResp ).withContentType( MediaType.XML_UTF_8 ) );
    server.when( request().withPath( "/pentaho/api/repo/files/:/tree" ).withQueryStringParameters( new Parameter( "depth",
      "1" ) ) )
      .respond( response().withBody( rootResp ).withContentType( MediaType.XML_UTF_8 ) );

    server.when( request().withPath( "/pentaho/api/repo/files/:public/tree" ).withQueryStringParameters( new Parameter( "depth",
      "1" ) ) )
      .respond( response().withBody( publicResp ).withContentType( MediaType.XML_UTF_8 ) );

    server.when( request().withPath( "/pentaho/api/repo/files/:public:projecting/tree" ).withQueryStringParameters( new Parameter( "depth",
      "1" ) ) )
      .respond( response().withBody( publicProjectingResp ).withContentType( MediaType.XML_UTF_8 ) );

    server.when( request().withPath( "/pentaho/api/repo/files/:public:projecting:something/tree" ).withQueryStringParameters( new Parameter( "depth",
      "1" ) ) )
      .respond( response().withBody( publicProjectingSomethingResp ).withContentType( MediaType.XML_UTF_8 ) );

    String resultsXlsPath = "/public/projecting/something/results.xls";
    var resultsXlsTree = createFileTreeDto(
      createFileXml( "8bce2b16-b4c4-4606-9b25-b5549c102323", "results.xls", resultsXlsPath, false ) );
    addBrowseEntry( server, resultsXlsPath, resultsXlsTree );

  }

  public static String readFileAsString( String path ) throws IOException {
    try ( InputStream is = RepositoryClientBrowseTest.class.getResourceAsStream( path ) ) {
      return IOUtils.toString( is, StandardCharsets.UTF_8 );
    }
  }

  public static void addBrowseEntry( ClientAndServer server, String path, Document responseXml ) {
    addBrowseEntry(server, path, responseXml.asXML() );
  }

  public static void addBrowseEntry( ClientAndServer server, String path, String responseXml ) {
    String serverPath = String.format( "/pentaho/api/repo/files/%s/tree", RepositoryPathEncoder.encodeRepositoryPath(
      path ) );
    server.when( request().withPath( serverPath ).withQueryStringParameters( new Parameter( "depth", "1" ) ) )
      .respond( response().withBody( responseXml ).withContentType( MediaType.XML_UTF_8 ) );
  }
}
