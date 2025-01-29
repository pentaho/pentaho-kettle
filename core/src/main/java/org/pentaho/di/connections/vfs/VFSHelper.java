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


package org.pentaho.di.connections.vfs;

import org.apache.commons.vfs2.FileSystemOptions;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.configuration.KettleGenericFileSystemConfigBuilder;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.util.function.Supplier;

/**
 * Created by bmorrise on 2/13/19.
 */
public class VFSHelper {

  /**
   * @deprecated, use the version with the Bowl
   */
  @Deprecated
  public static FileSystemOptions getOpts( String file, String connection, VariableSpace space ) {
    try {
      return getOpts( DefaultBowl.getInstance(), file, connection, space );
    } catch ( KettleException ex ) {
      // deprecated behavior was to ignore failures and return nulls.
      return null;
    }
  }

  public static FileSystemOptions getOpts( Bowl bowl, String file, String connection, VariableSpace space )
    throws KettleException {
    if ( connection != null ) {
      ConnectionManager connectionManager = bowl.getManager( ConnectionManager.class );
      VFSConnectionDetails vfsConnectionDetails =
        (VFSConnectionDetails) connectionManager.getConnectionDetails( file, connection );
      VFSConnectionProvider<VFSConnectionDetails> vfsConnectionProvider =
        (VFSConnectionProvider<VFSConnectionDetails>) connectionManager.getConnectionProvider( file );
      if ( vfsConnectionDetails != null && vfsConnectionProvider != null ) {
        vfsConnectionDetails.setSpace( space );
        FileSystemOptions opts = vfsConnectionProvider.getOpts( vfsConnectionDetails );

        KettleGenericFileSystemConfigBuilder.getInstance().setBowl( opts, bowl );

        return opts;
      }
    }
    return null;
  }
}
