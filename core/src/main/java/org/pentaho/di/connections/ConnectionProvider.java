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

package org.pentaho.di.connections;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;

import java.util.List;

/**
 * Created by bmorrise on 2/12/19.
 */
public interface ConnectionProvider<T extends ConnectionDetails> {
  String getName();

  String getKey();

  Class<T> getClassType();

  /**
   * @deprecated use getNames( ConnectionManager )
   */
  @Deprecated
  default List<String> getNames() {
    throw new UnsupportedOperationException( "Deprecated method" );
  }

  /**
   * @deprecated use getNames( ConnectionManager )
   */
  @Deprecated
  default List<T> getConnectionDetails() {
    throw new UnsupportedOperationException( "Deprecated method" );
  }

  // Subclasses should implement this to work with Bowls.
  default List<String> getNames( ConnectionManager connectionManager ) {
    return getNames();
  }

  // Subclasses should implement this to work with Bowls.
  default List<T> getConnectionDetails( ConnectionManager connectionManager ) {
    return getConnectionDetails();
  }

  boolean test( T connectionDetails ) throws KettleException;

  T prepare( T connectionDetails ) throws KettleException;
}
