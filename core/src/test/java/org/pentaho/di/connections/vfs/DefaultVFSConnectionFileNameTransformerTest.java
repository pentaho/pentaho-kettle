/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.vfs;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.VfsComponentContext;
import org.apache.commons.vfs2.provider.url.UrlFileNameParser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.provider.ConnectionFileName;
import org.pentaho.di.connections.vfs.provider.ConnectionFileNameParser;
import org.pentaho.di.connections.vfs.provider.ConnectionFileNameUtils;
import org.pentaho.di.connections.vfs.provider.ConnectionFileProvider;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.IKettleVFS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultVFSConnectionFileNameTransformerTest {
  private static final String CONNECTION_NAME1 = "connection-name1";
  private static final String CONNECTION_TYPE1 = "scheme1";

  @Mock
  private ConnectionManager manager;

  @Mock
  private VFSConnectionDetails details1;

  @Mock
  private Bowl bowl;

  @Mock
  private IKettleVFS kettleVFS;

  @Mock
  private VFSConnectionManagerHelper vfsConnectionManagerHelper;

  @Mock
  private VfsComponentContext context;

  @Mock
  private FileSystemManager fileSystemManager;

  private DefaultVFSConnectionFileNameTransformer<VFSConnectionDetails> transformer;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.openMocks( this );

    when( details1.getName() ).thenReturn( CONNECTION_NAME1 );
    when( details1.getType() ).thenReturn( CONNECTION_TYPE1 );

    // ---

    when( manager.getBowl() ).thenReturn( bowl );

    // ---

    when( context.getFileSystemManager() ).thenReturn( fileSystemManager );

    when( fileSystemManager.getSchemes() ).thenReturn( new String[] {
      ConnectionFileProvider.SCHEME,
      CONNECTION_TYPE1
    } );

    // ---

    when( vfsConnectionManagerHelper.getConnectionRootFileName( any() ) )
      .thenCallRealMethod();

    when( vfsConnectionManagerHelper.usesBuckets( any() ) )
      .thenCallRealMethod();

    // ---

    when( kettleVFS.resolveURI( anyString() ) )
      .then( (Answer<FileName>) invocationOnMock -> {
        String uri = invocationOnMock.getArgument( 0 );
        if ( isPvfsUri( uri ) ) {
          return new ConnectionFileNameParser().parseUri( uri );
        }

        return new UrlFileNameParser().parseUri( context, null, uri );
      } );

    // ---

    transformer = spy( new DefaultVFSConnectionFileNameTransformer<>(
      manager,
      vfsConnectionManagerHelper,
      ConnectionFileNameUtils.getInstance() ) );

    when( transformer.getKettleVFS( bowl ) ).thenReturn( kettleVFS );
  }

  // region toProviderFileName( ConnectionFileName, VFSConnectionDetails )
  @Test
  public void testToProviderFileNameHandlesTheConnectionRoot() throws Exception {

    ConnectionFileName pvfsFileName = mockPvfsFileNameWithPath( "/" );

    FileName providerFileName = transformer.toProviderFileName( pvfsFileName, details1 );

    assertEquals( "scheme1:///", providerFileName.getURI() );
    assertFalse( providerFileName.isFile() );

    // Should do provider uri normalization.
    verify( kettleVFS, times( 1 ) ).resolveURI( any() );
  }

  @Test
  public void testToProviderFileNameHandlesTwoLevelPaths() throws Exception {

    ConnectionFileName pvfsFileName = mockPvfsFileNameWithPath( "/rest/path" );

    FileName providerFileName = transformer.toProviderFileName( pvfsFileName, details1 );

    assertEquals( "scheme1://rest/path", providerFileName.getURI() );
    assertTrue( providerFileName.isFile() );

    // Should do provider uri normalization.
    verify( kettleVFS, times( 1 ) ).resolveURI( any() );
  }

  @Test
  public void testToProviderFileNameHandlesFolders() throws Exception {

    ConnectionFileName pvfsFileName = mockPvfsFileNameWithPath( "/rest/path/" );

    FileName providerFileName = transformer.toProviderFileName( pvfsFileName, details1 );

    assertEquals( "scheme1://rest/path", providerFileName.getURI() );
    assertFalse( providerFileName.isFile() );

    // Should do provider uri normalization.
    verify( kettleVFS, times( 1 ) ).resolveURI( any() );
  }

  @Test
  public void testToProviderFileNameHandlesConnectionsWithDomain() throws Exception {

    mockDetailsWithDomain( details1, "my-domain:8080" );

    ConnectionFileName pvfsFileName = mockPvfsFileNameWithPath( "/rest/path" );

    FileName providerFileName = transformer.toProviderFileName( pvfsFileName, details1 );

    assertEquals( "scheme1://my-domain:8080/rest/path", providerFileName.getURI() );

    // Should do provider uri normalization.
    verify( kettleVFS, times( 1 ) ).resolveURI( any() );

    // ---

    // Change domain to have leading and trailing slashes
    mockDetailsWithDomain( details1, "/my-domain:8080/" );

    providerFileName = transformer.toProviderFileName( pvfsFileName, details1 );

    assertEquals( "scheme1://my-domain:8080/rest/path", providerFileName.getURI() );

    // Should do provider uri normalization.
    verify( kettleVFS, times( 2 ) ).resolveURI( any() );
  }

  @Test
  public void testToProviderFileNameHandlesConnectionsWithRootPath() throws Exception {

    ConnectionFileName pvfsFileName = mockPvfsFileNameWithPath( "/rest/path" );

    mockDetailsWithRootPath( details1, "my/root/path" );

    FileName providerFileName = transformer.toProviderFileName( pvfsFileName, details1 );

    assertEquals( "scheme1://my/root/path/rest/path", providerFileName.getURI() );

    // Should do provider uri normalization.
    verify( kettleVFS, times( 1 ) ).resolveURI( any() );

    // ---

    mockDetailsWithRootPath( details1, "/my/root/path/" );

    providerFileName = transformer.toProviderFileName( pvfsFileName, details1 );

    assertEquals( "scheme1://my/root/path/rest/path", providerFileName.getURI() );

    // Should do provider uri normalization.
    verify( kettleVFS, times( 2 ) ).resolveURI( any() );
  }

  @Test
  public void testToProviderFileNameHandlesConnectionsWithDomainAndRootPath() throws Exception {
    mockDetailsWithDomain( details1, "my-domain:8080" );
    mockDetailsWithRootPath( details1, "my/root/path" );

    ConnectionFileName pvfsFileName = mockPvfsFileNameWithPath( "/rest/path" );

    FileName providerFileName = transformer.toProviderFileName( pvfsFileName, details1 );

    assertEquals( "scheme1://my-domain:8080/my/root/path/rest/path", providerFileName.getURI() );

    // Should do provider uri normalization.
    verify( kettleVFS, times( 1 ) ).resolveURI( any() );
  }
  // endregion

  // region toPvfsFileName( FileName, VFSConnectionDetails )

  // Note that, currently, there's no connection type which has no domain, no buckets and no required root path. And
  // although the default transformer code likely supports, it, would need a special file name parser that would handle
  // degenerate URLs (such as "scheme://", to represent the connection's root provider URI prefix) to demonstrate it
  // here.

  @Test
  public void testToPvfsFileNameHandlesConnectionsWithDomain() throws Exception {
    // Example: HCP

    mockDetailsWithDomain( details1, "my-domain:8080" );

    String connectionRootProviderUriPrefix = "scheme1://my-domain:8080";
    String restPath = "/rest/path";

    FileName providerFileName = mockFileNameWithUri( FileName.class, connectionRootProviderUriPrefix + restPath );

    ConnectionFileName pvfsFileName = transformer.toPvfsFileName( providerFileName, details1 );

    assertEquals( "pvfs://connection-name1" + restPath, pvfsFileName.getURI() );

    // Should do connection root provider uri normalization.
    verify( kettleVFS, times( 1 ) ).resolveURI( connectionRootProviderUriPrefix );
  }

  @Test
  public void testToPvfsFileNameHandlesConnectionsWithBuckets() throws Exception {
    // Example: S3

    when( details1.hasBuckets() ).thenReturn( true );

    String connectionRootProviderUriPrefix = "scheme1://";
    String restPath = "/rest/path";

    FileName providerFileName = mockFileNameWithUri( FileName.class, connectionRootProviderUriPrefix + restPath );

    ConnectionFileName pvfsFileName = transformer.toPvfsFileName( providerFileName, details1 );

    assertEquals( "pvfs://connection-name1" + restPath, pvfsFileName.getURI() );

    // Should NOT do connection root provider uri normalization.
    verify( kettleVFS, never() ).resolveURI( connectionRootProviderUriPrefix );
  }

  @Test
  public void testToPvfsFileNameHandlesConnectionsWithDomainAndBuckets() throws Exception {
    // Example: SMB

    mockDetailsWithDomain( details1, "my-domain:8080" );
    when( details1.hasBuckets() ).thenReturn( true );

    String connectionRootProviderUriPrefix = "scheme1://my-domain:8080";
    String restPath = "/rest/path";

    FileName providerFileName = mockFileNameWithUri( FileName.class, connectionRootProviderUriPrefix + restPath );

    ConnectionFileName pvfsFileName = transformer.toPvfsFileName( providerFileName, details1 );

    assertEquals( "pvfs://connection-name1" + restPath, pvfsFileName.getURI() );

    // Should do connection root provider uri normalization.
    verify( kettleVFS, times( 1 ) ).resolveURI( connectionRootProviderUriPrefix );
  }

  // Same combinations but with RootPath.

  @Test
  public void testToPvfsFileNameHandlesConnectionsWithRootPath() throws Exception {
    // Local (always with root path)

    mockDetailsWithRootPath( details1, "my/root/path" );

    String connectionRootProviderUriPrefix = "scheme1://my/root/path";
    String restPath = "/rest/path";

    FileName providerFileName = mockFileNameWithUri( FileName.class, connectionRootProviderUriPrefix + restPath );

    ConnectionFileName pvfsFileName = transformer.toPvfsFileName( providerFileName, details1 );

    assertEquals( "pvfs://connection-name1" + restPath, pvfsFileName.getURI() );

    // Should do connection root provider uri normalization.
    verify( kettleVFS, times( 1 ) ).resolveURI( connectionRootProviderUriPrefix );
  }

  @Test
  public void testToPvfsFileNameHandlesConnectionsWithDomainAndRootPath() throws Exception {
    // Example: HCP with root path

    mockDetailsWithDomain( details1, "my-domain:8080" );
    mockDetailsWithRootPath( details1, "my/root/path" );

    String connectionRootProviderUriPrefix = "scheme1://my-domain:8080/my/root/path";
    String restPath = "/rest/path";

    FileName providerFileName = mockFileNameWithUri( FileName.class, connectionRootProviderUriPrefix + restPath );

    ConnectionFileName pvfsFileName = transformer.toPvfsFileName( providerFileName, details1 );

    assertEquals( "pvfs://connection-name1" + restPath, pvfsFileName.getURI() );

    // Should do connection root provider uri normalization.
    verify( kettleVFS, times( 1 ) ).resolveURI( connectionRootProviderUriPrefix );
  }

  @Test
  public void testToPvfsFileNameHandlesConnectionsWithBucketsAndRootPath() throws Exception {
    // Example: S3 with root path

    when( details1.hasBuckets() ).thenReturn( true );
    mockDetailsWithRootPath( details1, "my/root/path" );

    String connectionRootProviderUriPrefix = "scheme1://my/root/path";
    String restPath = "/rest/path";

    FileName providerFileName = mockFileNameWithUri( FileName.class, connectionRootProviderUriPrefix + restPath );

    ConnectionFileName pvfsFileName = transformer.toPvfsFileName( providerFileName, details1 );

    assertEquals( "pvfs://connection-name1" + restPath, pvfsFileName.getURI() );

    // Should do connection root provider uri normalization.
    verify( kettleVFS, times( 1 ) ).resolveURI( connectionRootProviderUriPrefix );
  }

  @Test
  public void testToPvfsFileNameHandlesConnectionsWithDomainAndBucketsAndRootPath() throws Exception {
    // Example: SMB with root path

    mockDetailsWithDomain( details1, "my-domain:8080" );
    when( details1.hasBuckets() ).thenReturn( true );
    mockDetailsWithRootPath( details1, "my/root/path" );

    String connectionRootProviderUriPrefix = "scheme1://my-domain:8080/my/root/path";
    String restPath = "/rest/path";

    FileName providerFileName = mockFileNameWithUri( FileName.class, connectionRootProviderUriPrefix + restPath );

    ConnectionFileName pvfsFileName = transformer.toPvfsFileName( providerFileName, details1 );

    assertEquals( "pvfs://connection-name1" + restPath, pvfsFileName.getURI() );

    // Should do connection root provider uri normalization.
    verify( kettleVFS, times( 1 ) ).resolveURI( connectionRootProviderUriPrefix );
  }

  @Test
  public void testToPvfsFileNameHandlesTheConnectionRoot() throws Exception {
    // Example: SMB with root path

    mockDetailsWithDomain( details1, "my-domain:8080" );
    when( details1.hasBuckets() ).thenReturn( true );
    mockDetailsWithRootPath( details1, "my/root/path" );

    String connectionRootProviderUriPrefix = "scheme1://my-domain:8080/my/root/path";

    FileName providerFileName = mockFileNameWithUri( FileName.class, connectionRootProviderUriPrefix );

    ConnectionFileName pvfsFileName = transformer.toPvfsFileName( providerFileName, details1 );

    assertEquals( "pvfs://connection-name1/", pvfsFileName.getURI() );

    // Should do connection root provider uri normalization.
    verify( kettleVFS, times( 1 ) ).resolveURI( connectionRootProviderUriPrefix );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testToPvfsFileNameThrowsIfProviderUriIsNotDescendantOfConnectionRootUri() throws Exception {

    mockDetailsWithDomain( details1, "my-domain:8080" );
    mockDetailsWithRootPath( details1, "my/root/path" );

    // Note the `another-domain` which is different from the `my-domain` of the connection.
    String connectionRootProviderUriPrefix = "scheme1://another-domain:8080/my/root/path";
    String restPath = "/rest/path";

    FileName providerFileName = mockFileNameWithUri( FileName.class, connectionRootProviderUriPrefix + restPath );

    transformer.toPvfsFileName( providerFileName, details1 );
  }
  // endregion

  // region Helpers
  boolean isPvfsUri( String uri ) {
    return uri.startsWith( ConnectionFileProvider.ROOT_URI );
  }

  Class<? extends FileName> getFileNameClassOfUri( String uri ) {
    return isPvfsUri( uri ) ? ConnectionFileName.class : FileName.class;
  }

  <T extends FileName> T mockFileNameWithUri( Class<T> fileNameClass, String uri ) {

    boolean isFolder = isPathFolder( uri );

    uri = removeUriPathTrailingSeparator( uri );

    return mockFileNameWithUri( fileNameClass, uri, isFolder );
  }

  <T extends FileName> T mockFileNameWithUri( Class<T> fileNameClass, String uri, boolean isFolder ) {
    T fileName = mock( fileNameClass );

    when( fileName.getURI() ).thenReturn( uri );

    mockFileNameIsFolder( fileName, isFolder );

    return fileName;
  }

  ConnectionFileName mockPvfsFileNameWithPath( String path ) {
    ConnectionFileName fileName = mock( ConnectionFileName.class );

    boolean isFolder = isPathFolder( path );

    mockFileNameIsFolder( fileName, isFolder );

    if ( isFolder ) {
      path = removeUriPathTrailingSeparator( path );
    }

    when( fileName.getPath() ).thenReturn( path );

    return fileName;
  }

  boolean isPathFolder( String path ) {
    return path.endsWith( FileName.SEPARATOR );
  }

  void mockFileNameIsFolder( FileName fileName, boolean isFolder ) {
    when( fileName.getType() ).thenReturn( isFolder ? FileType.FOLDER : FileType.FILE );
    try {
      when( fileName.isFile() ).thenReturn( !isFolder );
    } catch ( FileSystemException e ) {
      // Never happens in tests.
    }
  }

  String removeUriPathTrailingSeparator( String uri ) {
    if ( isPathFolder( uri ) ) {
      // Remove the path's or the URI's _path_ trailing separator.
      int schemeStart = uri.indexOf( "://" );
      if ( schemeStart < 0 || ( schemeStart + 2 < uri.length() - 1 ) ) {
        uri = uri.substring( 0, uri.length() - 1 );
      }
    }

    return uri;
  }

  void mockDetailsWithRootPath( VFSConnectionDetails details, String resolvedRootPath ) throws KettleException {
    when( details.isRootPathSupported() ).thenReturn( true );
    when( vfsConnectionManagerHelper.getResolvedRootPath( details ) ).thenReturn( resolvedRootPath );
  }

  void mockDetailsWithDomain( VFSConnectionDetails details, String domain ) {
    when( details.getDomain() ).thenReturn( domain );
  }
  // endregion
}
