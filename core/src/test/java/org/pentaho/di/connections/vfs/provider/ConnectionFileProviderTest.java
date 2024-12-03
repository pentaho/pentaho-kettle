/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.connections.vfs.provider;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.FileNameParser;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.common.basic.TestBasicConnectionDetails;
import org.pentaho.di.connections.common.basic.TestBasicConnectionProvider;
import org.pentaho.di.connections.common.basic.TestBasicFileProvider;
import org.pentaho.di.connections.common.bucket.TestConnectionWithBucketsDetails;
import org.pentaho.di.connections.common.bucket.TestConnectionWithBucketsProvider;
import org.pentaho.di.connections.common.bucket.TestFileWithBucketsProvider;
import org.pentaho.di.connections.common.domain.TestConnectionWithDomainDetails;
import org.pentaho.di.connections.common.domain.TestConnectionWithDomainProvider;
import org.pentaho.di.connections.common.domain.TestFileWithDomainProvider;
import org.pentaho.di.connections.common.domainbuckets.TestConnectionWithDomainAndBucketsDetails;
import org.pentaho.di.connections.common.domainbuckets.TestConnectionWithDomainAndBucketsProvider;
import org.pentaho.di.connections.common.domainbuckets.TestFileWithDomainAndBucketsProvider;
import org.pentaho.di.core.bowl.BaseBowl;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.vfs.KettleVFSFileSystemException;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ConnectionFileProviderTest {
  private Bowl bowl;
  private ConnectionManager connectionManager;

  @Before
  public void setup() throws Exception {
    bowl = createTestBowl();
    connectionManager = bowl.getConnectionManager();

    // Connection Types

    connectionManager.addConnectionProvider(
      TestConnectionWithBucketsProvider.SCHEME,
      new TestConnectionWithBucketsProvider() );

    connectionManager.addConnectionProvider(
      TestConnectionWithDomainProvider.SCHEME,
      new TestConnectionWithDomainProvider() );

    connectionManager.addConnectionProvider(
      TestConnectionWithDomainAndBucketsProvider.SCHEME,
      new TestConnectionWithDomainAndBucketsProvider() );

    connectionManager.addConnectionProvider(
      TestBasicConnectionProvider.SCHEME,
      new TestBasicConnectionProvider() );

    // Apache VFS File Providers

    DefaultFileSystemManager fsm = (DefaultFileSystemManager) KettleVFS.getInstance().getFileSystemManager();

    if ( !fsm.hasProvider( ConnectionFileProvider.SCHEME ) ) {
      fsm.addProvider( ConnectionFileProvider.SCHEME, new ConnectionFileProvider() );
    }

    if ( !fsm.hasProvider( TestFileWithBucketsProvider.SCHEME ) ) {
      fsm.addProvider( TestFileWithBucketsProvider.SCHEME, new TestFileWithBucketsProvider() );
    }

    if ( !fsm.hasProvider( TestFileWithDomainProvider.SCHEME ) ) {
      fsm.addProvider( TestFileWithDomainProvider.SCHEME, new TestFileWithDomainProvider() );
    }

    if ( !fsm.hasProvider( TestFileWithDomainAndBucketsProvider.SCHEME ) ) {
      fsm.addProvider( TestFileWithDomainAndBucketsProvider.SCHEME, new TestFileWithDomainAndBucketsProvider() );
    }

    if ( !fsm.hasProvider( TestBasicFileProvider.SCHEME ) ) {
      fsm.addProvider( TestBasicFileProvider.SCHEME, new TestBasicFileProvider() );
    }

    // Connection Instances

    ConnectionDetails details;

    details = new TestConnectionWithBucketsDetails();
    details.setName( "Connection With Buckets" );
    connectionManager.save( details );

    details = new TestConnectionWithBucketsDetails();
    details.setName( "Connection Variable Substitution With Buckets" );
    connectionManager.save( details );

    details = new TestConnectionWithDomainDetails();
    details.setName( "Connection With Domain" );
    connectionManager.save( details );

    details = new TestConnectionWithDomainDetails();
    details.setName( "Connection Variable Substitution With Domain" );
    connectionManager.save( details );

    details = new TestConnectionWithDomainAndBucketsDetails();
    details.setName( "Connection With Domain And Buckets" );
    connectionManager.save( details );

    details = new TestConnectionWithDomainAndBucketsDetails();
    details.setName( "Connection Variable Substitution With Domain And Buckets" );
    connectionManager.save( details );

    details = new TestBasicConnectionDetails();
    details.setName( "Basic Connection" );
    connectionManager.save( details );

    details = new TestBasicConnectionDetails();
    details.setName( "Basic Connection Variable Substitution" );
    connectionManager.save( details );
  }

  @NonNull
  private Bowl createTestBowl() {
    MemoryMetaStore memoryMetaStore = new MemoryMetaStore();

    return new BaseBowl() {
      @Override
      public IMetaStore getMetastore() {
        return memoryMetaStore;
      }

      @Override
      public IMetaStore getExplicitMetastore() {
        return memoryMetaStore;
      }
    };
  }

  @NonNull
  private IKettleVFS getKettleVFS() {
    return KettleVFS.getInstance( bowl );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testSetFileNameParserThrowsIfIncorrectType() {
    try ( ConnectionFileProvider provider = new ConnectionFileProvider() ) {
      provider.setFileNameParser( mock( FileNameParser.class ) );
    }
  }

  // region PVFS Root File
  @Test
  public void testGetRootFile() throws Exception {
    ConnectionFileObject fileObject =
      (ConnectionFileObject) getKettleVFS().getFileObject( ConnectionFileProvider.ROOT_URI );

    assertTrue( fileObject.exists() );
    assertNull( fileObject.getResolvedFileObject() );
    assertEquals( ConnectionFileProvider.ROOT_URI, fileObject.getPublicURIString() );
    assertNull( fileObject.getParent() );

    assertTrue( fileObject.isAttached() );
    assertTrue( fileObject.isFolder() );
    assertFalse( fileObject.isHidden() );
    assertTrue( fileObject.isReadable() );
    assertFalse( fileObject.isWriteable() );
    assertFalse( fileObject.isExecutable() );
    assertFalse( fileObject.canRenameTo( mock( FileObject.class ) ) );
  }

  @Test( expected = KettleVFSFileSystemException.class )
  public void testRootFileThrowsOnMutationOperation() throws Exception {
    ConnectionFileObject fileObject =
      (ConnectionFileObject) getKettleVFS().getFileObject( ConnectionFileProvider.ROOT_URI );

    fileObject.delete();
  }
  // endregion

  // region Undefined Connection Root File
  @Test
  public void testGetUndefinedConnectionRootFile() throws Exception {
    ConnectionFileObject fileObject =
      (ConnectionFileObject) getKettleVFS().getFileObject( "pvfs://undefined-connection" );

    assertFalse( fileObject.exists() );
    assertNull( fileObject.getResolvedFileObject() );
    assertEquals( "pvfs://undefined-connection/", fileObject.getPublicURIString() );

    assertTrue( fileObject.isAttached() );
    assertFalse( fileObject.isHidden() );
    assertFalse( fileObject.isReadable() );
    assertFalse( fileObject.isWriteable() );
    assertFalse( fileObject.isExecutable() );
    assertFalse( fileObject.canRenameTo( mock( FileObject.class ) ) );
  }

  @Test( expected = KettleVFSFileSystemException.class )
  public void testUndefinedConnectionRootFileThrowsOnMutationOperation() throws Exception {
    ConnectionFileObject fileObject =
      (ConnectionFileObject) getKettleVFS().getFileObject( "pvfs://undefined-connection" );

    fileObject.delete();
  }
  // endregion

  // region Connection With Buckets Root File
  @Test
  public void testGetConnectionWithBucketsRootFile() throws Exception {
    ConnectionFileObject fileObject =
      (ConnectionFileObject) getKettleVFS().getFileObject( "pvfs://Connection With Buckets" );

    assertTrue( fileObject.exists() );
    assertNull( fileObject.getResolvedFileObject() );
    assertEquals( "pvfs://Connection With Buckets/", fileObject.getPublicURIString() );

    assertTrue( fileObject.isAttached() );
    assertTrue( fileObject.isFolder() );
    assertFalse( fileObject.isHidden() );
    assertTrue( fileObject.isReadable() );
    assertFalse( fileObject.isWriteable() );
    assertFalse( fileObject.isExecutable() );
    assertFalse( fileObject.canRenameTo( mock( FileObject.class ) ) );
  }

  @Test( expected = KettleVFSFileSystemException.class )
  public void testConnectionWithBucketsRootFileThrowsOnMutationOperation() throws Exception {
    ConnectionFileObject fileObject =
      (ConnectionFileObject) getKettleVFS().getFileObject( "pvfs://Connection With Buckets" );

    fileObject.delete();
  }

  @Test
  public void testGetConnectionWithDomainAndBucketsRootFile() throws Exception {
    ConnectionFileObject fileObject =
      (ConnectionFileObject) getKettleVFS().getFileObject( "pvfs://Connection With Domain And Buckets" );

    assertTrue( fileObject.exists() );
    assertNull( fileObject.getResolvedFileObject() );
    assertEquals( "pvfs://Connection With Domain And Buckets/", fileObject.getPublicURIString() );
  }

  @Test( expected = KettleVFSFileSystemException.class )
  public void testConnectionWithDomainAndBucketsRootFileThrowsOnMutationOperation() throws Exception {
    ConnectionFileObject fileObject =
      (ConnectionFileObject) getKettleVFS().getFileObject( "pvfs://Connection With Domain And Buckets" );

    fileObject.delete();
  }
  // endregion

  // region Resolved Files
  @Test
  public void testGetBasicConnectionRootFile() throws Exception {
    String pvfsUri = "pvfs://Basic Connection";
    ConnectionFileObject fileObject = (ConnectionFileObject) getKettleVFS().getFileObject( pvfsUri );
    assertTrue( fileObject.exists() );
    assertNotNull( fileObject.getResolvedFileObject() );

    assertEquals( "pvfs://Basic Connection/", fileObject.getPublicURIString() );
    assertEquals( "test4:///", fileObject.getResolvedFileObject().getPublicURIString() );
  }

  @Test
  public void testGetFileOfBasicConnection() throws Exception {
    String pvfsUri = "pvfs://Basic Connection/path/to/file.txt";
    ConnectionFileObject fileObject = (ConnectionFileObject) getKettleVFS().getFileObject( pvfsUri );
    assertTrue( fileObject.exists() );
    assertNotNull( fileObject.getResolvedFileObject() );

    assertEquals( pvfsUri, fileObject.getPublicURIString() );
    assertEquals( "pvfs://Basic Connection/path/to", fileObject.getParent().getPublicURIString() );
    assertEquals( "test4:///path/to/file.txt", fileObject.getResolvedFileObject().getPublicURIString() );
  }
  
  @Test
  public void testGetFileOfBasicConnectionVariableSubstitution() throws Exception {

    TestBasicConnectionDetails connectionDetails = (TestBasicConnectionDetails) connectionManager
      .getConnectionDetails( "Basic Connection Variable Substitution" );
    connectionDetails.setRootPath( "${my_var_root_path}" );

    Variables myVariableSpace = new Variables();
    myVariableSpace.setVariable( "my_var_root_path",  "/some/magical/dir" );

    String pvfsUri = "pvfs://Basic Connection Variable Substitution/path/to/file.txt";
    ConnectionFileObject fileObject = (ConnectionFileObject) getKettleVFS().getFileObject( pvfsUri, myVariableSpace );
    assertTrue( fileObject.exists() );
    assertNotNull( fileObject.getResolvedFileObject() );

    assertEquals( pvfsUri, fileObject.getPublicURIString() );
    assertEquals( "pvfs://Basic Connection Variable Substitution/path/to", fileObject.getParent().getPublicURIString() );
    assertEquals( "test4:///some/magical/dir/path/to/file.txt", fileObject.getResolvedFileObject().getPublicURIString() );
  }

  @Test
  public void testGetFileOfConnectionWithBuckets() throws Exception {
    String pvfsUri = "pvfs://Connection With Buckets/bucket/path/to/file.txt";
    ConnectionFileObject fileObject = (ConnectionFileObject) getKettleVFS().getFileObject( pvfsUri );
    assertTrue( fileObject.exists() );
    assertNotNull( fileObject.getResolvedFileObject() );

    assertEquals( pvfsUri, fileObject.getPublicURIString() );
    assertEquals( "pvfs://Connection With Buckets/bucket/path/to", fileObject.getParent().getPublicURIString() );
    assertEquals( "test://bucket/path/to/file.txt", fileObject.getResolvedFileObject().getPublicURIString() );
  }

  @Test
  public void testGetFileOfConnectionVariableSubstitutionWithBuckets() throws Exception {

    TestConnectionWithBucketsDetails connectionDetails = (TestConnectionWithBucketsDetails)
      connectionManager.getConnectionDetails( "Connection Variable Substitution With Buckets" );
    connectionDetails.setRootPath( "${my_var_root_path}" );

    Variables myVariableSpace = new Variables();
    myVariableSpace.setVariable( "my_var_root_path",  "/some/magical/dir" );

    String pvfsUri = "pvfs://Connection Variable Substitution With Buckets/bucket/path/to/file.txt";
    ConnectionFileObject fileObject = (ConnectionFileObject) getKettleVFS().getFileObject( pvfsUri, myVariableSpace );
    assertTrue( fileObject.exists() );
    assertNotNull( fileObject.getResolvedFileObject() );

    assertEquals( pvfsUri, fileObject.getPublicURIString() );
    assertEquals( "pvfs://Connection Variable Substitution With Buckets/bucket/path/to", fileObject.getParent().getPublicURIString() );
    assertEquals( "test://some/magical/dir/bucket/path/to/file.txt", fileObject.getResolvedFileObject().getPublicURIString() );
  }

  @Test
  public void testGetFileOfConnectionWithDomain() throws Exception {
    String pvfsUri = "pvfs://Connection With Domain/path/to/file.txt";
    ConnectionFileObject fileObject = (ConnectionFileObject) getKettleVFS().getFileObject( pvfsUri );
    assertTrue( fileObject.exists() );
    assertNotNull( fileObject.getResolvedFileObject() );

    assertEquals( pvfsUri, fileObject.getPublicURIString() );
    assertEquals( "pvfs://Connection With Domain/path/to", fileObject.getParent().getPublicURIString() );
    assertEquals( "test2://example.com/path/to/file.txt", fileObject.getResolvedFileObject().getPublicURIString() );
  }

  @Test
  public void testGetFileOfConnectionVariableSubstitutionWithDomain() throws Exception {

    TestConnectionWithDomainDetails connectionDetails = (TestConnectionWithDomainDetails)
      connectionManager.getConnectionDetails( "Connection Variable Substitution With Domain" );
    connectionDetails.setRootPath( "${my_var_root_path}" );

    Variables myVariableSpace = new Variables();
    myVariableSpace.setVariable( "my_var_root_path",  "/some/magical/dir" );

    String pvfsUri = "pvfs://Connection Variable Substitution With Domain/path/to/file.txt";
    ConnectionFileObject fileObject = (ConnectionFileObject) getKettleVFS().getFileObject( pvfsUri, myVariableSpace );
    assertTrue( fileObject.exists() );
    assertNotNull( fileObject.getResolvedFileObject() );

    assertEquals( pvfsUri, fileObject.getPublicURIString() );
    assertEquals( "pvfs://Connection Variable Substitution With Domain/path/to", fileObject.getParent().getPublicURIString() );
    assertEquals( "test2://example.com/some/magical/dir/path/to/file.txt", fileObject.getResolvedFileObject().getPublicURIString() );
  }

  @Test
  public void testGetFileOfConnectionWithDomainAndBuckets() throws Exception {
    String pvfsUri = "pvfs://Connection With Domain And Buckets/bucket/path/to/file.txt";
    ConnectionFileObject fileObject = (ConnectionFileObject) getKettleVFS().getFileObject( pvfsUri );
    assertTrue( fileObject.exists() );
    assertNotNull( fileObject.getResolvedFileObject() );

    assertEquals( pvfsUri, fileObject.getPublicURIString() );
    assertEquals( "pvfs://Connection With Domain And Buckets/bucket/path/to",
      fileObject.getParent().getPublicURIString() );
    assertEquals( "test3://example.com/bucket/path/to/file.txt",
      fileObject.getResolvedFileObject().getPublicURIString() );
  }

  @Test
  public void testGetFileOfConnectionConnectionVariableSubstitutionBucketsWithDomainAndBuckets() throws Exception {

    TestConnectionWithDomainAndBucketsDetails connectionDetails = (TestConnectionWithDomainAndBucketsDetails)
      connectionManager.getConnectionDetails( "Connection Variable Substitution With Domain And Buckets" );
    connectionDetails.setRootPath( "${my_var_root_path}" );

    Variables myVariableSpace = new Variables();
    myVariableSpace.setVariable( "my_var_root_path",  "/some/magical/dir" );

    String pvfsUri = "pvfs://Connection Variable Substitution With Domain And Buckets/bucket/path/to/file.txt";
    ConnectionFileObject fileObject = (ConnectionFileObject) getKettleVFS().getFileObject( pvfsUri, myVariableSpace );
    assertTrue( fileObject.exists() );
    assertNotNull( fileObject.getResolvedFileObject() );

    assertEquals( pvfsUri, fileObject.getPublicURIString() );
    assertEquals( "pvfs://Connection Variable Substitution With Domain And Buckets/bucket/path/to",
      fileObject.getParent().getPublicURIString() );
    assertEquals( "test3://example.com/some/magical/dir/bucket/path/to/file.txt",
      fileObject.getResolvedFileObject().getPublicURIString() );
  }

  @Test
  public void testGetFileOfUndefinedConnection() throws Exception {
    String pvfsUri = "pvfs://Fake Item/path/to/file.txt";
    FileObject fileObject = getKettleVFS().getFileObject( pvfsUri );
    assertFalse( fileObject.exists() );
    assertEquals( pvfsUri, fileObject.getPublicURIString() );
  }
  // endregion

  @Test
  public void testGetChildren() throws Exception {
    ConnectionFileObject fileObject =
      (ConnectionFileObject) getKettleVFS().getFileObject( "pvfs://Connection With Buckets/bucket/path/to/directory" );
    assertNotNull( fileObject.getResolvedFileObject() );

    assertEquals( "test://bucket/path/to/directory", fileObject.getResolvedFileObject().getPublicURIString() );
    FileObject[] children = fileObject.getChildren();
    for ( FileObject child : children ) {
      assertTrue( child.getPublicURIString().startsWith( "pvfs://Connection With Buckets/bucket" ) );
    }
  }

  @Test
  public void testGetFileWithDomain() throws Exception {
    ConnectionFileObject fileObject =
      (ConnectionFileObject) getKettleVFS().getFileObject( "pvfs://Connection With Domain/path/to/directory" );
    assertNotNull( fileObject.getResolvedFileObject() );

    assertEquals( "test2://example.com/path/to/directory", fileObject.getResolvedFileObject().getPublicURIString() );
  }
}
