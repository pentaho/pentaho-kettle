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

import com.google.cloud.storage.StorageOptions;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import java.util.Collection;

/**
 * Created by bmorrise on 8/28/17.
 */
public class GoogleCloudStorageFileSystem extends AbstractFileSystem implements FileSystem {

  public GoogleCloudStorageFileSystem( FileName rootName, FileSystemOptions fileSystemOptions ) {
    super( rootName, null, fileSystemOptions );
  }

  @Override
  protected FileObject createFile( AbstractFileName abstractFileName ) throws Exception {
    return new GoogleCloudStorageFileObject( abstractFileName, this, StorageOptions.getDefaultInstance().getService() );
  }

  @Override protected void addCapabilities( Collection<Capability> collection ) {
    collection.addAll( GoogleCloudStorageFileProvider.capabilities );
  }
}
