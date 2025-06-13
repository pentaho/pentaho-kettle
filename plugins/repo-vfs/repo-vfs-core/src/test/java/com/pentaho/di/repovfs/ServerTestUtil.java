package org.pentaho.di.repovfs;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.mockserver.model.Parameter;

public class ServerTestUtil {

   public static void setUpBasicBrowse( ClientAndServer server ) throws Exception {
    String rootResp = readFileAsString( "/browse/root_children_resp.xml" );
    String publicResp = readFileAsString( "/browse/browse_public_resp.xml" );
    String publicProjectingResp = readFileAsString( "/browse/browse_public_projecting_resp.xml" );
    String publicProjectingSomethingResp = readFileAsString( "/browse/browse_public_projecting_something_resp.xml" );

    server.when( request().withPath( "/pentaho/api/repo/files/tree" ).withQueryStringParameters( new Parameter( "depth",
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
  }

  public static String readFileAsString( String path ) throws IOException {
    try ( InputStream is = RepositoryClientBrowseTest.class.getResourceAsStream( path ) ) {
      return IOUtils.toString( is, StandardCharsets.UTF_8 );
    }
  }
}
