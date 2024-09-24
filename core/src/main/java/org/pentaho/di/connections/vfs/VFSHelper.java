/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.vfs;

import org.apache.commons.vfs2.FileSystemOptions;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
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
    } catch ( MetaStoreException ex ) {
      // deprecated behavior was to ignore failures and return nulls.
      return null;
    }
  }

  public static FileSystemOptions getOpts( Bowl bowl, String file, String connection, VariableSpace space )
    throws MetaStoreException {
    if ( connection != null ) {
      VFSConnectionDetails vfsConnectionDetails =
        (VFSConnectionDetails) bowl.getConnectionManager().getConnectionDetails( file, connection );
      VFSConnectionProvider<VFSConnectionDetails> vfsConnectionProvider =
        (VFSConnectionProvider<VFSConnectionDetails>) bowl.getConnectionManager().getConnectionProvider( file );
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
