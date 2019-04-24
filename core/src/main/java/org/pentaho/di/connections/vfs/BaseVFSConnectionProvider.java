/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.vfs;

import org.pentaho.di.connections.ConnectionManager;

import java.util.List;
import java.util.function.Supplier;

public abstract class BaseVFSConnectionProvider<T extends VFSConnectionDetails> implements VFSConnectionProvider<T> {

  private Supplier<ConnectionManager> connectionManagerSupplier = ConnectionManager::getInstance;

  @Override public List<String> getNames() {
    return connectionManagerSupplier.get().getNamesByType( getClass() );
  }

  @SuppressWarnings( "unchecked" )
  @Override public List<T> getConnectionDetails() {
    return (List<T>) connectionManagerSupplier.get().getConnectionDetailsByScheme( getKey() );
  }

  @Override public T prepare( T connectionDetails ) {
    return connectionDetails;
  }

  @Override public String sanitizeName( String string ) {
    return string;
  }
}
