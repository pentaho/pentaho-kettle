package org.pentaho.di.repovfs;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.pentaho.di.repovfs.vfs.JCRSolutionFileProvider;
import org.pentaho.di.repovfs.vfs.JCRSolutionFileSystem;
import org.pentaho.di.repovfs.vfs.JCRSolutionFileSystem.ConfigBuilder;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

/**
 * Integration tests dealing with the VFS layer downwards (no PDI)
 */
public class VFSBrowseTest {

  private static DefaultFileSystemManager fsm;
  private static ClientAndServer server;
  private static FileSystemOptions opts;

  private static String baseUrl;

  @BeforeAll
  static void init() throws Exception {
    // assumes http resolver is registered
    fsm = (DefaultFileSystemManager) VFS.getManager();
    server = ClientAndServer.startClientAndServer( 0 );
    ServerTestUtil.setUpBasicBrowse( server );
    fsm.removeProvider( JCRSolutionFileProvider.SCHEME );
    fsm.addProvider( JCRSolutionFileProvider.SCHEME, new JCRSolutionFileProvider() );

    opts = new FileSystemOptions();
    ConfigBuilder cfg = JCRSolutionFileSystem.createConfigBuilder();
    cfg.setUser( opts, "user" );
    cfg.setPassword( opts, "pass" );
    cfg.setUrl( opts, "http://localhost:" + server.getPort() + "/pentaho" );

    baseUrl = String.format( "%s:http://localhost:%d/pentaho!", JCRSolutionFileProvider.SCHEME, server.getPort() );
  }

  @Test
  void testResolveBrowse() throws Exception {
    FileObject pub = fsm.resolveFile( baseUrl + "/public", opts );
    assertTrue( pub.exists() );

    FileObject folder = pub.getChild( "projecting" );
    assertTrue( folder.exists() );
    assertTrue( folder.isFolder() );
  }

  @Test
  void testResolveBrowseNotThere() throws Exception {
    FileObject pub = fsm.resolveFile( baseUrl + "/public", opts );
    assertTrue( pub.exists() );

    FileObject notThere = pub.getChild( "nope" );
    assertNull( notThere );
  }

  @Test
  void testResolveFile() throws Exception {
    FileObject folder = fsm.resolveFile( baseUrl + "/public/projecting/something/", opts );
    FileObject file = folder.getChild( "results.xls" );
    assertTrue( file.exists() );
    assertTrue( file.isFile() );
  }

  @Test
  void testResolveFileNotThere() throws Exception {
    FileObject notThere = fsm.resolveFile( baseUrl + "/public/nope/not_there.txt", opts );
    assertFalse( notThere.exists() );
  }

  @Test
  void testBrowseTreeDtoToKtrVfs() throws Exception {
    FileObject curr = fsm.resolveFile( baseUrl + "/", opts );
    curr = curr.getChild( "public" );
    assertTrue( curr.isFolder() );
    curr = curr.getChild( "projecting" );
    assertTrue( curr.isFolder() );
    curr = curr.getChild( "something" );
    assertTrue( curr.isFolder() );
    curr = curr.getChild( "dum.ktr" );
    assertFalse( curr.isFolder() );
  }

}
