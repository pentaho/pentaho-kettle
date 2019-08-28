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

package org.pentaho.di.connections.vfs.provider;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.URLFileName;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;

import java.util.Collection;
import java.util.function.Supplier;

public class ConnectionFileSystem extends AbstractFileSystem implements FileSystem {

  public static final String CONNECTION = "connection";
  private Supplier<ConnectionManager> connectionManager = ConnectionManager::getInstance;

  public ConnectionFileSystem( FileName rootName, FileSystemOptions fileSystemOptions ) {
    super( rootName, null, fileSystemOptions );
  }

  @Override
  protected FileObject createFile( AbstractFileName abstractFileName ) throws Exception {
    String connectionName = ( (URLFileName) abstractFileName ).getHostName();
    ConnectionDetails connectionDetails = connectionManager.get().getConnectionDetails( connectionName );

    if ( connectionDetails != null ) {
      String url = connectionDetails.getType() + ":/" + abstractFileName.getPath();
      Variables variables = new Variables();
      variables.setVariable( CONNECTION, connectionName );
      return KettleVFS.getFileObject( url, variables );
    }

    return KettleVFS.getFileObject( abstractFileName.getPath() );
  }

  @Override protected void addCapabilities( Collection<Capability> collection ) {
    collection.addAll( ConnectionFileProvider.capabilities );
  }

}
