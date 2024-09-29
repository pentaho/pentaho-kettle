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


package org.pentaho.di.connections.common.bucket;

import org.pentaho.di.connections.vfs.BaseVFSConnectionProvider;
import org.pentaho.di.connections.vfs.VFSRoot;

import java.util.List;

public class TestConnectionWithBucketsProvider extends BaseVFSConnectionProvider<TestConnectionWithBucketsDetails> {
  public static final String NAME = "Test";
  public static final String SCHEME = "test";

  @Override public String getName() {
    return NAME;
  }

  @Override public String getKey() {
    return SCHEME;
  }

  @Override public List<VFSRoot> getLocations( TestConnectionWithBucketsDetails vfsConnectionDetails ) {
    return null;
  }

  @Override public String getProtocol( TestConnectionWithBucketsDetails vfsConnectionDetails ) {
    return SCHEME;
  }

  @Override public Class<TestConnectionWithBucketsDetails> getClassType() {
    return TestConnectionWithBucketsDetails.class;
  }

  @Override public boolean test( TestConnectionWithBucketsDetails connectionDetails ) {
    return true;
  }
}
