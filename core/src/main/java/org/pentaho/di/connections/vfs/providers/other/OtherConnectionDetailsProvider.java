/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.vfs.providers.other;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.vfs.BaseVFSConnectionProvider;
import org.pentaho.di.connections.vfs.VFSRoot;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;

import java.util.Collections;
import java.util.List;

/**
 * Created by bmorrise on 2/3/19.
 */
public class OtherConnectionDetailsProvider extends BaseVFSConnectionProvider<OtherConnectionDetails> {

  public static final String NAME = "Other";
  public static final String SCHEME = "other";

  @Override public Class<OtherConnectionDetails> getClassType() {
    return OtherConnectionDetails.class;
  }

  @Override public FileSystemOptions getOpts( OtherConnectionDetails otherConnectionDetails ) {
    if ( otherConnectionDetails == null ) {
      return null;
    }

    VariableSpace space = getSpace( otherConnectionDetails );
    StaticUserAuthenticator auth =
      new StaticUserAuthenticator( getVar( otherConnectionDetails.getHost(), space ),
        getVar( otherConnectionDetails.getUsername(), space ),
        getVar( otherConnectionDetails.getPassword(), space ) );
    FileSystemOptions opts = new FileSystemOptions();
    try {
      DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator( opts, auth );
    } catch ( FileSystemException fse ) {
      // Ignore and return default options
    }
    return opts;
  }

  @Override public List<VFSRoot> getLocations( OtherConnectionDetails vfsConnectionDetails ) {
    VariableSpace space = getSpace( vfsConnectionDetails );
    String host = getVar( vfsConnectionDetails.getHost(), space );
    String port = getVar( vfsConnectionDetails.getPort(), space );

    String location = host + ( port != null && !port.equals( "" ) ? ":" + port : "" );
    return Collections.singletonList( new VFSRoot( location, null ) );
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getKey() {
    return SCHEME;
  }

  @Override public String getProtocol( OtherConnectionDetails otherConnectionDetails ) {
    return otherConnectionDetails.getProtocol();
  }

  @Override public boolean test( OtherConnectionDetails connectionDetails ) {
    return true;
  }
}

