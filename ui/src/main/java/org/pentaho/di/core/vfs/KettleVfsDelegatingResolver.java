/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.vfs.ui.VfsResolver;

/**
 * @author Andrey Khayrutdinov
 */
public class KettleVfsDelegatingResolver implements VfsResolver {

  @Override
  public FileObject resolveFile( String vfsUrl ) {
    try {
      return KettleVFS.getFileObject( vfsUrl );
    } catch ( KettleFileException e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public FileObject resolveFile( String vfsUrl, FileSystemOptions fsOptions ) {
    try {
      return KettleVFS.getFileObject( vfsUrl, fsOptions );
    } catch ( KettleFileException e ) {
      throw new RuntimeException( e );
    }
  }
}
