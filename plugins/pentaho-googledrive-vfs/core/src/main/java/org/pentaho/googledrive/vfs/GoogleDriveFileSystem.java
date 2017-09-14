/*!
* Copyright 2010 - 2017 Pentaho Corporation.  All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.pentaho.googledrive.vfs;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import java.util.Collection;

public class GoogleDriveFileSystem extends AbstractFileSystem implements FileSystem {

  protected GoogleDriveFileSystem( final FileName rootName, final FileSystemOptions fileSystemOptions ) {
    super( rootName, null, fileSystemOptions );
  }

  protected FileObject createFile( AbstractFileName abstractFileName ) throws Exception {
    return new GoogleDriveFileObject( abstractFileName, this );
  }

  protected void addCapabilities( Collection<Capability> caps ) {
    caps.addAll( GoogleDriveFileProvider.capabilities );
  }

  protected void clearFileFromCache( FileName name ) {
    super.removeFileFromCache( name );
  }
}
