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


package org.pentaho.di.connections.common.domain;

import org.pentaho.di.connections.vfs.BaseVFSConnectionProvider;
import org.pentaho.di.connections.vfs.VFSRoot;

import java.util.List;

public class TestConnectionWithDomainProvider extends BaseVFSConnectionProvider<TestConnectionWithDomainDetails> {

  public static final String NAME = "Test2";
  public static final String SCHEME = "test2";

  @Override public String getName() {
    return NAME;
  }

  @Override public String getKey() {
    return SCHEME;
  }

  @Override public List<VFSRoot> getLocations( TestConnectionWithDomainDetails vfsConnectionDetails ) {
    return null;
  }

  @Override public String getProtocol( TestConnectionWithDomainDetails vfsConnectionDetails ) {
    return null;
  }

  @Override public Class<TestConnectionWithDomainDetails> getClassType() {
    return TestConnectionWithDomainDetails.class;
  }

  @Override public boolean test( TestConnectionWithDomainDetails connectionDetails ) {
    return true;
  }
}
