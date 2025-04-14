package com.pentaho.di.repovfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import com.pentaho.di.repovfs.cfg.JCRSolutionConfig;
import com.pentaho.di.repovfs.repo.BasicAuthentication;
import com.pentaho.di.repovfs.repo.RepositoryClient;

import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import javax.ws.rs.HttpMethod;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

public class RepositoryClientUploadDeleteTest {
    private static ClientAndServer server;

  @BeforeAll
  static void setUpOnce() throws Exception {
    server = ClientAndServer.startClientAndServer( 0 );
    ServerTestUtil.setUpBasicBrowse( server );
  }

  @AfterAll
  static void tearDownOnce() throws Exception {
    server.stop();
  }


  private RepositoryClient createRepositoryClient() {
    int port = server.getPort();
    String url = String.format("http://localhost:%d/pentaho", port );
    return new RepositoryClient( new JCRSolutionConfig(), url, new BasicAuthentication( "a", "p") );
  }


  @Test
  void testUploadFile() throws Exception {
    final String path = "/pentaho/api/repo/files/public%3Aprojecting%3Afile.ext";
    boolean[] isItThere = new boolean[] { false };

    server.when( request().withMethod( HttpMethod.PUT ).withPath( path ) )
      .respond( req -> {
        byte[] bytesReceived = req.getBodyAsRawBytes();
        if ( !isItThere[ 0 ] && Arrays.equals( bytesReceived, new byte[] { 1, 2, 3 } ) ) {
          isItThere[0] = true;
          return response().withStatusCode( 200 );
        } else {
          return response().withStatusCode( 500 );
        }
      } );

    RepositoryClient client = createRepositoryClient();
    try ( InputStream bytes = new ByteArrayInputStream( new byte[] { 1, 2, 3 } ) ) {
      client.writeData( new String[] { "public", "projecting", "file.ext" }, bytes );
    }
    assertTrue( isItThere[0] );
  }


  @Test
  void testDeleteFile() throws Exception {
    final String fileId = "xpto-wombat-002";

    boolean[] isItThere = new boolean[] { true };

    server.when( request().withMethod( HttpMethod.PUT ).withPath( "/pentaho/api/repo/files/delete" ) )
      .respond( req -> {
        String receivedId = req.getBodyAsString();
        if ( receivedId.equals( fileId ) && isItThere[ 0 ] ) {
          isItThere[ 0 ] = false;
          return response().withStatusCode( 200 );
        } else {
          return response().withStatusCode( 500 );
        }
      } );

    RepositoryClient client = createRepositoryClient();
    RepositoryFileDto file = new RepositoryFileDto();
    file.setId( fileId );
    file.setName( "delete.me" );
    file.setPath( "/public/projecting/something/delete.me" );
    client.delete( file );
    assertFalse( isItThere[ 0 ] );
  }
}
