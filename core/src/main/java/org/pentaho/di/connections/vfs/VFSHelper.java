/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

/**
 * Created by bmorrise on 2/13/19.
 */
public class VFSHelper {
  public static FileSystemOptions getOpts( String file, String connection ) {
    if ( connection != null ) {
      VFSConnectionDetails vfsConnectionDetails =
        (VFSConnectionDetails) ConnectionManager.getInstance().getConnectionDetails( file, connection );
      VFSConnectionProvider<VFSConnectionDetails> vfsConnectionProvider =
        (VFSConnectionProvider<VFSConnectionDetails>) ConnectionManager.getInstance().getConnectionProvider( file );
      if ( vfsConnectionDetails != null && vfsConnectionProvider != null ) {
        return vfsConnectionProvider.getOpts( vfsConnectionDetails );
      }
    }
    return null;
  }
}
