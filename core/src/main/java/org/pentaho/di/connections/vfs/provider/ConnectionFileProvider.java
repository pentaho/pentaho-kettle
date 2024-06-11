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

package org.pentaho.di.connections.vfs.provider;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.FileNameParser;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.VFSConnectionManagerHelper;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.vfs.KettleVFSFileSystemException;
import org.pentaho.di.core.vfs.configuration.KettleGenericFileSystemConfigBuilder;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ConnectionFileProvider extends AbstractOriginatingFileProvider {

  public static final String SCHEME = "pvfs";
  public static final String ROOT_URI = SCHEME + "://";
  public static final String SCHEME_NAME = "Connection File System";

  protected static final Collection<Capability>
    capabilities = Collections.unmodifiableCollection( Arrays.asList( Capability.CREATE, Capability.DELETE,
    Capability.RENAME, Capability.GET_TYPE, Capability.LIST_CHILDREN, Capability.READ_CONTENT, Capability.URI,
    Capability.WRITE_CONTENT, Capability.GET_LAST_MODIFIED, Capability.RANDOM_ACCESS_READ ) );

  public ConnectionFileProvider() {
    setFileNameParser( ConnectionFileNameParser.getInstance() );
  }

  @Override
  protected FileSystem doCreateFileSystem( @NonNull FileName rootName, @NonNull FileSystemOptions fileSystemOptions )
    throws FileSystemException {

    Bowl bowl = getBowl( fileSystemOptions );
    ConnectionManager manager = getDefaultManager( bowl );
    VFSConnectionManagerHelper vfsConnectionManagerHelper = getVfsConnectionManagerHelper();
    return new ConnectionFileSystem( rootName, fileSystemOptions, manager, vfsConnectionManagerHelper );
  }

  @NonNull
  private VFSConnectionManagerHelper getVfsConnectionManagerHelper() {
    return new VFSConnectionManagerHelper( ConnectionFileNameUtils.getInstance() );
  }

  @Override
  protected ConnectionFileNameParser getFileNameParser() {
    return (ConnectionFileNameParser) super.getFileNameParser();
  }

  @Override
  protected void setFileNameParser( FileNameParser parser ) {
    if ( !( parser instanceof ConnectionFileNameParser ) ) {
      throw new IllegalArgumentException( "Argument 'parser' is not an instance of 'ConnectionFileNameParser'." );
    }

    super.setFileNameParser( parser );
  }

  // The bowl is stored in the FileSystemOptions.
  @NonNull
  protected Bowl getBowl( @NonNull FileSystemOptions fileSystemOptions ) {
    return KettleGenericFileSystemConfigBuilder.getInstance().getBowl( fileSystemOptions );
  }

  @NonNull
  protected ConnectionManager getDefaultManager( @NonNull Bowl bowl ) throws FileSystemException {
    try {
      return bowl.getConnectionManager();
    } catch ( MetaStoreException e ) {
      throw new KettleVFSFileSystemException( "ConnectionFileProvider.FailedLoadConnectionManager", e );
    }
  }

  @Override
  public Collection<Capability> getCapabilities() {
    return capabilities;
  }
}
