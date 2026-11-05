package org.pentaho.s3.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.jupiter.api.Test;
import org.pentaho.di.core.vfs.configuration.KettleGenericFileSystemConfigBuilder;
import org.pentaho.s3.vfs.support.TestBowl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class S3FileSystemCacheTest {

  @Test
  void collidingAccessKeysHaveSeparateFileSystems() throws Exception {
    // Aa and BB will yield the same hash, exploiting an issue with FileSystemOptions#compareTo
    FileSystemOptions first = options( "Aa" );
    FileSystemOptions colliding = options( "BB" );
    FileSystemOptions different = options( "CC" );
    assertNotEquals( accessKey( first ), accessKey( colliding ) );
    assertEquals( accessKey( first ).hashCode(), accessKey( colliding ).hashCode() );
    assertEquals( 0, first.compareTo( colliding ), "precondition: VFS treats the options as equal" );

    try ( DefaultFileSystemManager manager = new DefaultFileSystemManager() ) {
      manager.addProvider( "s3", new S3FileProvider() );
      manager.init();
      // Resolve only: no metadata access, SDK client creation, or network requests are needed.
      try ( FileObject a = manager.resolveFile( "s3://bucket/object.bin", first );
            FileObject b = manager.resolveFile( "s3://bucket/object.bin", colliding );
            FileObject c = manager.resolveFile( "s3://bucket/object.bin", different );
            FileObject again = manager.resolveFile( "s3://bucket/other.bin", options( "Aa" ) ) ) {
        assertNotSame( a.getFileSystem(), b.getFileSystem(), "colliding keys must be isolated" );
        assertEquals( "BB", accessKey( b.getFileSystem().getFileSystemOptions() ),
          "the second resolution must retain its own credentials" );
        assertNotSame( a.getFileSystem(), c.getFileSystem(), "non-colliding keys must be isolated" );
        assertSame( a.getFileSystem(), again.getFileSystem(), "same options reuse the filesystem" );
      }
    }
  }

  @Test
  void otherCollidingOptionsHaveSeparateFileSystems() throws Exception {
    for ( String option : new String[] { "name", "secret", "token", "region", "credentialsFile", "profileName",
      "endpoint", "signatureVersion", "pathStyleAccess", "defaultS3Config", "connectionType", "trustFile",
      "trustPassword", "trustAll", "keyFile", "keyPassword" } ) {
      assertCollidingOptionIsIsolated( option );
    }
  }

  @Test
  void combinedValueHashCollisionDoesNotShareFileSystem() throws Exception {
    FileSystemOptions first = options( "a" );
    FileSystemOptions second = options( "b" );
    new S3FileSystemConfigBuilder( first ).setSecretKey( "bA" );
    new S3FileSystemConfigBuilder( second ).setSecretKey( "aA" );
    assertNotEquals( accessKey( first ).hashCode(), accessKey( second ).hashCode() );
    assertEquals( 0, first.compareTo( second ), "precondition: combined value hashes collide" );

    try ( DefaultFileSystemManager manager = new DefaultFileSystemManager() ) {
      manager.addProvider( "s3", new S3FileProvider() );
      manager.init();
      try ( FileObject a = manager.resolveFile( "s3://bucket/object.bin", first );
            FileObject b = manager.resolveFile( "s3://bucket/object.bin", second ) ) {
        assertNotSame( a.getFileSystem(), b.getFileSystem() );
      }
    }
  }

  @Test
  void vfsOptionsOutsideS3AreStillCompared() throws Exception {
    FileSystemOptions first = options( "same-key" );
    FileSystemOptions second = options( "same-key" );
    FileSystemOptions equivalent = options( "same-key" );
    var builder = new ExtraOptionsBuilder();
    builder.setValue( first, "first" );
    builder.setValue( second, "second" );
    builder.setValue( equivalent, "first" );
    assertNotEquals( 0, first.compareTo( second ) );

    try ( DefaultFileSystemManager manager = new DefaultFileSystemManager() ) {
      manager.addProvider( "s3", new S3FileProvider() );
      manager.init();
      try ( FileObject a = manager.resolveFile( "s3://bucket/object.bin", first );
            FileObject b = manager.resolveFile( "s3://bucket/object.bin", second );
            FileObject again = manager.resolveFile( "s3://bucket/other.bin", equivalent ) ) {
        assertNotSame( a.getFileSystem(), b.getFileSystem() );
        assertSame( a.getFileSystem(), again.getFileSystem() );
      }
    }
  }

  @Test
  void vfsDistinguishesAbsentAndExplicitDefaultOptions() throws Exception {
    FileSystemOptions absent = new FileSystemOptions();
    FileSystemOptions explicit = new FileSystemOptions();
    new S3FileSystemConfigBuilder( explicit ).setUseDefaults( true );
    assertNotEquals( 0, absent.compareTo( explicit ) );

    try ( DefaultFileSystemManager manager = new DefaultFileSystemManager() ) {
      manager.addProvider( "s3", new S3FileProvider() );
      manager.init();
      try ( FileObject a = manager.resolveFile( "s3://bucket/object.bin", absent );
            FileObject b = manager.resolveFile( "s3://bucket/object.bin", explicit );
            FileObject again = manager.resolveFile( "s3://bucket/other.bin", (FileSystemOptions) null ) ) {
        assertNotSame( a.getFileSystem(), b.getFileSystem() );
        assertSame( a.getFileSystem(), again.getFileSystem() );
      }
    }
  }

  private static void assertCollidingOptionIsIsolated( String option ) throws Exception {
    FileSystemOptions first = options( "same-access-key" );
    FileSystemOptions second = options( "same-access-key" );
    setOption( first, option, "Aa" );
    setOption( second, option, "BB" );
    assertEquals( 0, first.compareTo( second ), "precondition: VFS treats the options as equal" );
    try ( DefaultFileSystemManager manager = new DefaultFileSystemManager() ) {
      manager.addProvider( "s3", new S3FileProvider() );
      manager.init();
      try ( FileObject a = manager.resolveFile( "s3://bucket/object.bin", first );
            FileObject b = manager.resolveFile( "s3://bucket/object.bin", second ) ) {
        assertNotSame( a.getFileSystem(), b.getFileSystem(), option );
      }
    }
  }

  @Test
  void bowlsRemainIsolatedAndReuseTheirOwnFileSystems() throws Exception {
    var firstBowl = new TestBowl();
    var secondBowl = new TestBowl();
    FileSystemOptions first = options( "same-key" );
    FileSystemOptions second = options( "same-key" );
    FileSystemOptions equivalent = options( "same-key" );
    var builder = KettleGenericFileSystemConfigBuilder.getInstance();
    builder.setBowl( first, firstBowl );
    builder.setBowl( second, secondBowl );
    builder.setBowl( equivalent, firstBowl );
    try ( DefaultFileSystemManager manager = new DefaultFileSystemManager() ) {
      manager.addProvider( "s3", new S3FileProvider() );
      manager.init();
      try ( FileObject a = manager.resolveFile( "s3://bucket/object.bin", first );
            FileObject b = manager.resolveFile( "s3://bucket/object.bin", second );
            FileObject again = manager.resolveFile( "s3://bucket/other.bin", equivalent ) ) {
        assertNotSame( a.getFileSystem(), b.getFileSystem() );
        assertSame( a.getFileSystem(), again.getFileSystem() );
      }
    }
  }

  @Test
  void callerMutationDoesNotChangeCachedCredentials() throws Exception {
    FileSystemOptions mutable = options( "Aa" );
    try ( DefaultFileSystemManager manager = new DefaultFileSystemManager() ) {
      manager.addProvider( "s3", new S3FileProvider() );
      manager.init();
      try ( FileObject a = manager.resolveFile( "s3://bucket/object.bin", mutable ) ) {
        new S3FileSystemConfigBuilder( mutable ).setAccessKey( "BB" );
        try ( FileObject b = manager.resolveFile( "s3://bucket/object.bin", mutable );
              FileObject original = manager.resolveFile( "s3://bucket/other.bin", options( "Aa" ) ) ) {
          assertNotSame( a.getFileSystem(), b.getFileSystem() );
          assertEquals( "Aa", accessKey( a.getFileSystem().getFileSystemOptions() ) );
          assertEquals( "BB", accessKey( b.getFileSystem().getFileSystemOptions() ) );
          assertSame( a.getFileSystem(), original.getFileSystem() );
        }
      }
    }
  }

  @Test
  void closingOneFileSystemEvictsOnlyItsOwnEntry() throws Exception {
    try ( DefaultFileSystemManager manager = new DefaultFileSystemManager() ) {
      manager.addProvider( "s3", new S3FileProvider() );
      manager.init();
      var first = manager.resolveFile( "s3://bucket/object.bin", options( "Aa" ) ).getFileSystem();
      var second = manager.resolveFile( "s3://bucket/object.bin", options( "BB" ) ).getFileSystem();
      manager.closeFileSystem( first );
      try ( FileObject replacement = manager.resolveFile( "s3://bucket/object.bin", options( "Aa" ) );
            FileObject retained = manager.resolveFile( "s3://bucket/object.bin", options( "BB" ) ) ) {
        assertNotSame( first, replacement.getFileSystem() );
        assertNotSame( second, replacement.getFileSystem() );
        assertSame( second, retained.getFileSystem() );
      }
    }
  }

  private static void setOption( FileSystemOptions options, String option, String value ) {
    var builder = new S3FileSystemConfigBuilder( options );
    switch ( option ) {
      case "name" -> builder.setName( value );
      case "secret" -> builder.setSecretKey( value );
      case "token" -> builder.setSessionToken( value );
      case "region" -> builder.setRegion( value );
      case "credentialsFile" -> builder.setCredentialsFile( value );
      case "profileName" -> builder.setProfileName( value );
      case "endpoint" -> builder.setEndpoint( value );
      case "signatureVersion" -> builder.setSignatureVersion( value );
      case "pathStyleAccess" -> builder.setPathStyleAccess( value );
      case "defaultS3Config" -> builder.setDefaultS3Config( value );
      case "connectionType" -> builder.setConnectionType( value );
      case "trustFile" -> builder.setTrustStoreFilePath( value );
      case "trustPassword" -> builder.setTrustStorePassword( value );
      case "trustAll" -> builder.setTrustAll( value );
      case "keyFile" -> builder.setKeyStoreFilePath( value );
      case "keyPassword" -> builder.setKeyStorePassword( value );
      default -> throw new IllegalArgumentException( option );
    }
  }

  private static FileSystemOptions options( String accessKey ) {
    S3FileSystemConfigBuilder builder = new S3FileSystemConfigBuilder( new FileSystemOptions() );
    builder.setAccessKey( accessKey );
    builder.setSecretKey( "same-secret" );
    builder.setUseDefaults( false );
    return builder.getFileSystemOptions();
  }

  private static String accessKey( FileSystemOptions options ) {
    return new S3FileSystemConfigBuilder( options ).getAccessKey();
  }

  private static class ExtraOptionsBuilder extends FileSystemConfigBuilder {
    void setValue( FileSystemOptions options, String value ) {
      setParam( options, "extra", value );
    }

    @Override
    protected Class<? extends FileSystem> getConfigClass() {
      return FileSystem.class;
    }
  }
}