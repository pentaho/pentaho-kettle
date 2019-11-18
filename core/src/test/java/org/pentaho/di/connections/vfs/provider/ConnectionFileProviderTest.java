/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.vfs.provider;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.common.bucket.TestConnectionDetails;
import org.pentaho.di.connections.common.bucket.TestConnectionProvider;
import org.pentaho.di.connections.common.bucket.TestFileProvider;
import org.pentaho.di.connections.common.domain.TestConnectionWithDomainDetails;
import org.pentaho.di.connections.common.domain.TestConnectionWithDomainProvider;
import org.pentaho.di.connections.common.domain.TestFileWithDomainProvider;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

public class ConnectionFileProviderTest {

  // Provider type "test" which are bucket based
  private static String CONNECTION_NAME = "Connection Name";
  public static final String PVFS_FILE_PATH = "pvfs://Connection Name/bucket/path/to/file.txt";
  public static final String RESOLVED_FILE_PATH = "test://bucket/path/to/file.txt";
  public static final String PVFS_PARENT_FILE_PATH = "pvfs://Connection Name/bucket/path/to";
  public static final String PVFS_DIRECTORY_PATH = "pvfs://Connection Name/bucket/path/to/directory";
  public static final String RESOLVED_DIRECTORY_PATH = "test://bucket/path/to/directory";
  public static final String PVFS_PREFIX = "pvfs://Connection Name/bucket";

  // Provider type "test2" which are domain based
  private static String CONNECTION_NAME_2 = "Connection Name 2";
  public static final String PVFS_FILE_PATH_2 = "pvfs://Connection Name 2/path/to/file.txt";
  public static final String RESOLVED_FILE_PATH_2 = "test2://example.com/path/to/file.txt";
  public static final String PVFS_PARENT_FILE_PATH_2 = "pvfs://Connection Name 2/path/to";
  public static final String PVFS_DIRECTORY_PATH_2 = "pvfs://Connection Name 2/path/to/directory";
  public static final String RESOLVED_DIRECTORY_PATH_2 = "test2://example.com/path/to/directory";

  // Does not exist
  public static final String PVFS_FILE_PATH_1 = "pvfs://Fake Item/path/to/file.txt";

  private ConnectionManager connectionManager;
  private MemoryMetaStore memoryMetaStore = new MemoryMetaStore();

  @Before
  public void setup() throws Exception {
    connectionManager = ConnectionManager.getInstance();
    connectionManager.setMetastoreSupplier( () -> memoryMetaStore );
    addOne();

    DefaultFileSystemManager fsm = (DefaultFileSystemManager) KettleVFS.getInstance().getFileSystemManager();
    if ( !fsm.hasProvider( ConnectionFileProvider.SCHEME ) ) {
      fsm.addProvider( ConnectionFileProvider.SCHEME, new ConnectionFileProvider() );
    }
    if ( !fsm.hasProvider( TestFileProvider.SCHEME ) ) {
      fsm.addProvider( TestFileProvider.SCHEME, new TestFileProvider() );
    }
    if ( !fsm.hasProvider( TestFileWithDomainProvider.SCHEME ) ) {
      fsm.addProvider( TestFileWithDomainProvider.SCHEME, new TestFileWithDomainProvider() );
    }
  }

  private void addProvider() {
    TestConnectionProvider testConnectionProvider = new TestConnectionProvider( connectionManager );
    connectionManager.addConnectionProvider( TestConnectionProvider.SCHEME, testConnectionProvider );

    TestConnectionWithDomainProvider testConnectionWithDomainProvider =
      new TestConnectionWithDomainProvider( connectionManager );
    connectionManager
      .addConnectionProvider( TestConnectionWithDomainProvider.SCHEME, testConnectionWithDomainProvider );
  }

  private void addOne() {
    addProvider();
    TestConnectionDetails testConnectionDetails = new TestConnectionDetails();
    testConnectionDetails.setName( CONNECTION_NAME );
    connectionManager.save( testConnectionDetails );
  }

  private void addConnectionWithDomain() {
    addProvider();
    TestConnectionWithDomainDetails testConnectionDetails = new TestConnectionWithDomainDetails();
    testConnectionDetails.setName( CONNECTION_NAME_2 );
    connectionManager.save( testConnectionDetails );
  }

  @Test
  public void testGetFile() throws Exception {
    ConnectionFileObject fileObject = (ConnectionFileObject) KettleVFS.getFileObject( PVFS_FILE_PATH );
    Assert.assertTrue( fileObject.exists() );
    Assert.assertEquals( PVFS_FILE_PATH, fileObject.getPublicURIString() );
    Assert.assertEquals( PVFS_PARENT_FILE_PATH, fileObject.getParent().getPublicURIString() );
    Assert.assertEquals( RESOLVED_FILE_PATH, fileObject.getResolvedFileObject().getPublicURIString() );
  }

  @Test
  public void testGetFileNotFound() throws Exception {
    FileObject fileObject = KettleVFS.getFileObject( PVFS_FILE_PATH_1 );
    Assert.assertFalse( fileObject.exists() );
    Assert.assertEquals( PVFS_FILE_PATH_1, fileObject.getPublicURIString() );
  }

  @Test
  public void testGetChildren() throws Exception {
    ConnectionFileObject fileObject = (ConnectionFileObject) KettleVFS.getFileObject( PVFS_DIRECTORY_PATH );
    Assert.assertEquals( RESOLVED_DIRECTORY_PATH, fileObject.getResolvedFileObject().getPublicURIString() );
    FileObject[] children = fileObject.getChildren();
    for ( FileObject child : children ) {
      Assert.assertTrue( child.getPublicURIString().startsWith( PVFS_PREFIX ) );
    }
  }

  @Test
  public void testGetFileWithDomain() throws Exception {
    addConnectionWithDomain();

    ConnectionFileObject fileObject = (ConnectionFileObject) KettleVFS.getFileObject( PVFS_DIRECTORY_PATH_2 );
    Assert.assertEquals( RESOLVED_DIRECTORY_PATH_2, fileObject.getResolvedFileObject().getPublicURIString() );
  }


}
