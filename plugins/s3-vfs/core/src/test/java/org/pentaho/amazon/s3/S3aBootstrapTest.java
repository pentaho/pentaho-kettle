package org.pentaho.amazon.s3;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.ram.RamFileProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** S3A compatibility bootstrap coverage using real VFS managers. */
class S3aBootstrapTest {
  @Test
  void registersBothSchemesAndKeepsTheirFileSystemsSeparate() throws Exception {
    try ( var manager = new DefaultFileSystemManager() ) {
      manager.init();
      S3FileSystemBootstrap.registerFileProvider( manager );
      try ( FileObject s3 = manager.resolveFile( "s3://bucket/key" );
            FileObject s3a = manager.resolveFile( "s3a://bucket/key" ) ) {
        assertEquals( "s3", s3.getName().getScheme() );
        assertEquals( "s3a", s3a.getName().getScheme() );
        assertNotSame( s3.getFileSystem(), s3a.getFileSystem() );
        S3FileSystemBootstrap.registerFileProvider( manager );
        try ( FileObject again = manager.resolveFile( "s3a://bucket/key" ) ) {
          assertSame( s3a.getFileSystem(), again.getFileSystem() );
        }
      }
      assertTrue( manager.hasProvider( "s3" ) );
      assertTrue( manager.hasProvider( "s3a" ) );
      assertFalse( manager.hasProvider( "s3n" ) );
    }
  }

  @Test
  void preservesEitherPreRegisteredScheme() throws Exception {
    for ( String existing : new String[] { "s3", "s3a" } ) {
      try ( var manager = new DefaultFileSystemManager() ) {
        manager.addProvider( existing, new RamFileProvider() );
        manager.init();
        try ( FileObject before = manager.resolveFile( existing + ":/sentinel" ) ) {
          S3FileSystemBootstrap.registerFileProvider( manager );
          S3FileSystemBootstrap.registerFileProvider( manager );
          try ( FileObject after = manager.resolveFile( existing + ":/sentinel" ) ) {
            assertSame( before.getFileSystem(), after.getFileSystem() );
          }
        }
        String added = existing.equals( "s3" ) ? "s3a" : "s3";
        try ( FileObject file = manager.resolveFile( added + "://bucket/key" ) ) {
          assertEquals( added, file.getName().getScheme() );
        }
      }
    }
  }
}