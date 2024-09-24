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

package org.pentaho.di.connections.ui;

import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.ConnectionProvider;

import java.util.List;

public class TestConnectionProvider implements ConnectionProvider<TestConnectionDetails> {

  private ConnectionManager connectionManager;

  public TestConnectionProvider( ConnectionManager connectionManager ) {
    this.connectionManager = connectionManager;
  }

  public static final String NAME = "Test";
  public static final String SCHEME = "test";

  @Override public String getName() {
    return NAME;
  }

  @Override public String getKey() {
    return SCHEME;
  }

  @Override public Class<TestConnectionDetails> getClassType() {
    return TestConnectionDetails.class;
  }

  @Override public List<String> getNames() {
    return getNames( connectionManager );
  }

  @Override public List<String> getNames( ConnectionManager connectionManager ) {
    return connectionManager.getNamesByType( getClass() );
  }

  @Override public List<TestConnectionDetails> getConnectionDetails() {
    return getConnectionDetails( connectionManager );
  }

  @SuppressWarnings( "unchecked" )
  @Override public List<TestConnectionDetails> getConnectionDetails( ConnectionManager connectionManager ) {
    return (List<TestConnectionDetails>) connectionManager.getConnectionDetailsByScheme( getKey() );
  }

  @Override public boolean test( TestConnectionDetails connectionDetails ) {
    return true;
  }

  @Override public TestConnectionDetails prepare( TestConnectionDetails connectionDetails ) {
    return connectionDetails;
  }
}
