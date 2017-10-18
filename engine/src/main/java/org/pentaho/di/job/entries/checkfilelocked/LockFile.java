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

package org.pentaho.di.job.entries.checkfilelocked;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;

public class LockFile {

  /** name of file to check **/
  private String filename;
  /** lock indicator **/
  private boolean locked;

  /**
   * Checks if a file is locked In order to check is a file is locked we will use a dummy renaming exercise
   *
   * @param filename
   * @throws KettleException
   */
  public LockFile( String filename ) throws KettleException {
    setFilename( filename );
    setLocked( false );

    // In order to check is a file is locked
    // we will use a dummy renaming exercise
    FileObject file = null;
    FileObject dummyfile = null;

    try {

      file = KettleVFS.getFileObject( filename );
      if ( file.exists() ) {
        dummyfile = KettleVFS.getFileObject( filename );
        // move file to itself!
        file.moveTo( dummyfile );
      }
    } catch ( Exception e ) {
      // We got an exception
      // The is locked by another process
      setLocked( true );
    } finally {
      if ( file != null ) {
        try {
          file.close();
        } catch ( Exception e ) { /* Ignore */
        }
      }
      if ( dummyfile != null ) {
        try {
          file.close();
        } catch ( Exception e ) { /* Ignore */
        }
      }
    }

  }

  /**
   * Returns filename
   *
   * @return filename
   */
  public String getFilename() {
    return this.filename;
  }

  /**
   * Set filename
   *
   * @param filename
   */
  private void setFilename( String filename ) {
    this.filename = filename;
  }

  /**
   * Returns lock indicator
   *
   * @return TRUE is file is locked
   */
  public boolean isLocked() {
    return this.locked;
  }

  /**
   * Set lock indicator
   *
   * @param lock
   */
  private void setLocked( boolean lock ) {
    this.locked = lock;
  }
}
