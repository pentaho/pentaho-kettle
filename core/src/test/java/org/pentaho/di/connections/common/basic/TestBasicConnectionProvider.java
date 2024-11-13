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


package org.pentaho.di.connections.common.basic;

import org.pentaho.di.connections.vfs.BaseVFSConnectionProvider;
import org.pentaho.di.connections.vfs.VFSRoot;

import java.util.List;

public class TestBasicConnectionProvider extends BaseVFSConnectionProvider<TestBasicConnectionDetails> {
  public static final String NAME = "Test";
  public static final String SCHEME = "test4";

  @Override public String getName() {
    return NAME;
  }

  @Override public String getKey() {
    return SCHEME;
  }

  @Override public List<VFSRoot> getLocations( TestBasicConnectionDetails vfsConnectionDetails ) {
    return null;
  }

  @Override public String getProtocol( TestBasicConnectionDetails vfsConnectionDetails ) {
    return SCHEME;
  }

  @Override public Class<TestBasicConnectionDetails> getClassType() {
    return TestBasicConnectionDetails.class;
  }

  @Override public boolean test( TestBasicConnectionDetails connectionDetails ) {
    return true;
  }
}
