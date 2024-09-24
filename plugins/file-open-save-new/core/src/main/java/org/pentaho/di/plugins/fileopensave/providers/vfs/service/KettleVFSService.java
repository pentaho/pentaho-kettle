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

package org.pentaho.di.plugins.fileopensave.providers.vfs.service;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;

/**
 * Simple wrapper class around {@link IKettleVFS}
 */
public class KettleVFSService {

  protected IKettleVFS iKettleVFS;

  public KettleVFSService() {
    this( DefaultBowl.getInstance() );
  }

  public KettleVFSService( Bowl bowl ) {
    this( KettleVFS.getInstance( bowl ) );
  }

  public KettleVFSService( IKettleVFS iKettleVFS ) {
    this.iKettleVFS = iKettleVFS;
  }

  /**
   * Wrapper around {@link IKettleVFS#getFileObject(String, VariableSpace)}
   * @param vfsPath - file object where <code>vfsFile.getPath()</code> returns a URI
   *  with the prefix or scheme equal to {@value  org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME}
   * @param space
   * @return
   * @throws FileException
   */
  public FileObject getFileObject( String vfsPath, VariableSpace space ) throws FileException {
    try {
      return iKettleVFS.getFileObject( vfsPath, space );
    } catch ( KettleFileException kfe ) {
      throw new FileException( "error calling IKettleVFS.getFileObject", kfe );
    }
  }
}
