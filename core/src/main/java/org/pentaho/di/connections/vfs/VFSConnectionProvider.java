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

package org.pentaho.di.connections.vfs;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.ConnectionProvider;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;

import java.util.List;

/**
 * Created by bmorrise on 2/3/19.
 */
public interface VFSConnectionProvider<T extends VFSConnectionDetails> extends ConnectionProvider<T> {
  FileSystemOptions getOpts( T vfsConnectionDetails );

  List<VFSRoot> getLocations( T vfsConnectionDetails );

  String getProtocol( T vfsConnectionDetails );

  String sanitizeName( String string );

  /**
   * If the provided ConnectionDetails are the correct type, return a FileObject for the provided path. This method and
   * the returning FileObject must not depend on a ConnectionManager or MetaStore.
   *
   * @param connectionDetails for the connection
   * @param path              path relative to the connection
   * @return FileSystem or null if the provided connection details are not the matching type
   */
  default FileObject getDirectFile( T connectionDetails, String path ) throws KettleFileException {
    String pvfsUrl = connectionDetails.getType() + "://" + path;
    // use an empty Variables to prevent other "connection" values from causing StackOverflowErrors
    return KettleVFS.getFileObject( pvfsUrl, new Variables(), getOpts( connectionDetails ) );
  }

  /**
   * Gets a file name transformer for transforming file names in the context of a given connection manager.
   * @param connectionManager The connection manager.
   * @return The file name transformer.
   */
  @NonNull
  default VFSConnectionFileNameTransformer<T> getFileNameTransformer( @NonNull ConnectionManager connectionManager ) {
    return new DefaultVFSConnectionFileNameTransformer<>( connectionManager );
  }
}
