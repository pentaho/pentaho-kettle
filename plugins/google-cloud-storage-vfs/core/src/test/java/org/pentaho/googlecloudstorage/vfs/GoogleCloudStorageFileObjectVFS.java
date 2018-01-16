/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.googlecloudstorage.vfs;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.common.collect.ImmutableList;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.URLFileName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by bmorrise on 1/15/18.
 */
@RunWith( MockitoJUnitRunner.class )
public class GoogleCloudStorageFileObjectVFS {

  @Mock
  private Storage storage;

  @Mock
  private Blob blob;

  @Mock
  private Bucket bucket;

  @Mock
  private Page page;

  private GoogleCloudStorageFileObject fileObject;

  private GoogleCloudStorageFileObject folderObject;

  private GoogleCloudStorageFileSystem fileSystem;

  @Before
  public void setup() {
    AbstractFileName rootFileName = new URLFileName( "gs", "mock-bucket", -1, -1, "", "", "/", FileType
      .FILE, "" );

    fileSystem = new GoogleCloudStorageFileSystem( rootFileName, null );
    URLFileName testFileName = new URLFileName( "gs", "mock-bucket", -1, -1, "", "", "/file/path/file.csv", FileType
      .FILE, "" );
    fileObject = new GoogleCloudStorageFileObject( testFileName, fileSystem, storage );

    doReturn( bucket ).when( storage ).get( testFileName.getHostName() );
    doReturn( true ).when( bucket ).exists();
    doReturn( blob ).when( bucket ).get( anyString() );
    doReturn( "/file/path/file.csv" ).when( blob ).getName();

    Blob blob1 = mock( Blob.class );
    doReturn( "/file/" ).when( blob1 ).getName();
    Blob blob2 = mock( Blob.class );
    doReturn( "/file/path/" ).when( blob2 ).getName();
    Blob blob3 = mock( Blob.class );
    doReturn( "/file/path/file2.csv" ).when( blob3 ).getName();

    URLFileName testFolderName = new URLFileName( "gs", "mock-bucket", -1, -1, "", "", "/file/path/", FileType
      .FILE, "" );
    folderObject = new GoogleCloudStorageFileObject( testFolderName, fileSystem, storage );
    doReturn( ImmutableList.of( blob1, blob2, blob, blob3 ) ).when( page ).iterateAll();
    doReturn( page ).when( bucket ).list();
  }

  @Test
  public void testDoGetType() throws Exception {
    FileType fileType = fileObject.getType();
    assertEquals( fileType, FileType.FILE );

    verify( bucket.get( anyString() ) );
    verify( blob.getName() );
  }

  @Test
  public void testDoListChildren() throws Exception {
    String[] children = folderObject.doListChildren();
    assertTrue( Arrays.asList( children ).contains( "file.csv" ) );
    assertTrue( Arrays.asList( children ).contains( "file2.csv" ) );
    assertEquals( 2, children.length );
  }
}
