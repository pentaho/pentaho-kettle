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
