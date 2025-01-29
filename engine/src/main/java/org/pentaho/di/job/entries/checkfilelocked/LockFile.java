/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.job.entries.checkfilelocked;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.IKettleVFS;
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
  public LockFile( Bowl bowl, String filename ) throws KettleException {
    setFilename( filename );
    setLocked( false );

    // In order to check is a file is locked
    // we will use a dummy renaming exercise
    FileObject file = null;
    FileObject dummyfile = null;

    try {
      IKettleVFS vfs = KettleVFS.getInstance( bowl );
      file = vfs.getFileObject( filename );
      if ( file.exists() ) {
        dummyfile = vfs.getFileObject( filename );
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
