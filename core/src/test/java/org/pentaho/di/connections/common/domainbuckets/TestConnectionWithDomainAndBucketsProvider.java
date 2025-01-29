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


package org.pentaho.di.connections.common.domainbuckets;

import org.pentaho.di.connections.vfs.BaseVFSConnectionProvider;
import org.pentaho.di.connections.vfs.VFSRoot;

import java.util.List;

public class TestConnectionWithDomainAndBucketsProvider
  extends BaseVFSConnectionProvider<TestConnectionWithDomainAndBucketsDetails> {

  public static final String NAME = "Test3";
  public static final String SCHEME = "test3";

  @Override public String getName() {
    return NAME;
  }

  @Override public String getKey() {
    return SCHEME;
  }

  @Override public List<VFSRoot> getLocations( TestConnectionWithDomainAndBucketsDetails vfsConnectionDetails ) {
    return null;
  }

  @Override public String getProtocol( TestConnectionWithDomainAndBucketsDetails vfsConnectionDetails ) {
    return null;
  }

  @Override public Class<TestConnectionWithDomainAndBucketsDetails> getClassType() {
    return TestConnectionWithDomainAndBucketsDetails.class;
  }

  @Override public boolean test( TestConnectionWithDomainAndBucketsDetails connectionDetails ) {
    return true;
  }
}
