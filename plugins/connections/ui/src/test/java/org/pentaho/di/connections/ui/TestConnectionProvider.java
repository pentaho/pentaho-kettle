/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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
