package org.pentaho.di.repovfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import org.pentaho.di.repovfs.cfg.JCRSolutionConfig;
import org.pentaho.di.repovfs.repo.BasicAuthentication;
import org.pentaho.di.repovfs.repo.RepositoryClient;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileTreeDto;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.mockserver.model.Parameter;

public class RepositoryClientBrowseTest {

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
  void testBrowseRoot() throws Exception {
    RepositoryClient client = createRepositoryClient();
    RepositoryFileTreeDto root = client.getRoot();

    Set<String> rootChildren = root.getChildren().stream().map( child -> child.getFile().getName() ).collect( Collectors.toSet() );
    assertEquals( 2, rootChildren.size() );
    assertTrue( rootChildren.contains( "home" ) );
    assertTrue( rootChildren.contains( "public" ) );
  }

  @Test
  void testLookupProjecting() throws Exception {
    RepositoryClient client = createRepositoryClient();
    RepositoryFileTreeDto something = client.fetchChildTree( new String[] { "public", "projecting", "something" } ).get();
    assertEquals( "something", something.getFile().getName() );
    assertTrue( something.getFile().isFolder() );
  }

  @Test
  void testLookupFail() throws Exception {
    RepositoryClient client = createRepositoryClient();
    assertTrue( client.fetchChildTree( new String[] { "public", "projecting", "something_else" } ).isEmpty() );
    assertTrue( client.fetchChildTree( new String[] { "nowhere", "to", "be", "found" } ).isEmpty() );
  }

  @Test
  void testBasicAuth() throws Exception {
    String rootResp = ServerTestUtil.readFileAsString( "/browse/root_children_resp.xml" );
    server.when( request().withPath( "/hexaho/api/repo/files/:/tree" ).withQueryStringParameters( new Parameter( "depth",
      "1" ) ).withHeader( "Authorization", "Basic dXNlcjoxMjM0" ) )
      .respond( response().withBody( rootResp ).withContentType( MediaType.XML_UTF_8 ) );

    String url = String.format("http://localhost:%d/hexaho", server.getPort() );
    RepositoryClient client = new RepositoryClient( new JCRSolutionConfig(), url, new BasicAuthentication( "user", "1234" ) );
    assertNotNull( client.getRoot() );
  }
}
