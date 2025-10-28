package org.pentaho.di.repovfs;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import org.pentaho.di.repovfs.vfs.JCRSolutionFileProvider;
import org.pentaho.di.repovfs.vfs.JCRSolutionFileSystem;
import org.pentaho.di.repovfs.vfs.JCRSolutionFileSystem.ConfigBuilder;

import jakarta.ws.rs.HttpMethod;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Parameter;

class RepositoryClientMoveTest {
  private static DefaultFileSystemManager fsm;
  private static ClientAndServer server;
  private static FileSystemOptions opts;

  private static String baseUrl;

  @BeforeAll
  static void setUpOnce() throws Exception {
    server = ClientAndServer.startClientAndServer( 0 );
    ServerTestUtil.setUpBasicBrowse( server );

    fsm = (DefaultFileSystemManager) VFS.getManager();
    fsm.removeProvider( JCRSolutionFileProvider.SCHEME );
    fsm.addProvider( JCRSolutionFileProvider.SCHEME, new JCRSolutionFileProvider() );

    opts = new FileSystemOptions();
    ConfigBuilder cfg = JCRSolutionFileSystem.createConfigBuilder();
    cfg.setUser( opts, "user" );
    cfg.setPassword( opts, "pass" );
    cfg.setUrl( opts, "http://localhost:" + server.getPort() + "/pentaho" );

    baseUrl = String.format( "%s:http://localhost:%d/pentaho!", JCRSolutionFileProvider.SCHEME, server.getPort() );
  }

  @AfterAll
  static void tearDownOnce() {
    server.stop();
  }

  @Test
  void testRenameFile() throws Exception {
    boolean[] called = new boolean[] { false };
    final String renameCall = "/pentaho/api/repo/files/:public:projecting:something/rename";
    server.when( request().withMethod( HttpMethod.PUT ).withPath( renameCall )
      .withQueryStringParameter( new Parameter( "newName", "anything" ) ) )
      .respond( req -> {
        called[ 0 ] = true;
        return response().withStatusCode( 200 );
      } );

    FileObject orig = fsm.resolveFile( baseUrl + "/public/projecting/something", opts );
    assertTrue( orig.exists() );
    FileObject target = fsm.resolveFile( baseUrl + "/public/projecting/anything", opts );
    assertFalse( target.exists() );

    orig.moveTo( target );
    assertTrue( called[ 0 ], "Did not call rename on repo" );
  }

  @Test
  void testMoveFile() throws Exception {
    boolean[] called = new boolean[] { false };
    final String moveCall = "/pentaho/api/repo/files/:public/move";
    final String somethingId = "37c81de7-a430-4efd-a4ee-d95404867237";
    server.when( request().withMethod( HttpMethod.PUT ).withPath( moveCall )
      .withBody( somethingId ) )
      .respond( req -> {
        called[ 0 ] = true;
        return response().withStatusCode( 200 );
      } );

    FileObject orig = fsm.resolveFile( baseUrl + "/public/projecting/something", opts );
    assertTrue( orig.exists() );
    FileObject target = fsm.resolveFile( baseUrl + "/public/something", opts );
    assertFalse( target.exists() );

    orig.moveTo( target );
    assertTrue( called[ 0 ], "Did not call move on repo" );
  }
}
