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


package org.pentaho.di.connections.vfs.providers.other;

import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.pentaho.di.connections.vfs.BaseVFSConnectionProvider;
import org.pentaho.di.connections.vfs.VFSRoot;
import org.pentaho.di.core.variables.VariableSpace;

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

    DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator( opts, auth );

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

