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

package org.pentaho.di.connections.vfs;

import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class BaseVFSConnectionProvider<T extends VFSConnectionDetails> implements VFSConnectionProvider<T> {

  private Supplier<ConnectionManager> connectionManagerSupplier = ConnectionManager::getInstance;

  @Override
  public List<String> getNames() {
    return connectionManagerSupplier.get().getNamesByType( getClass() );
  }

  @Override
  public List<T> getConnectionDetails() {
    return getConnectionDetails( connectionManagerSupplier.get() );
  }

  @Override
  public List<String> getNames( ConnectionManager connectionManager ) {
    return connectionManager.getNamesByType( getClass() );
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public List<T> getConnectionDetails( ConnectionManager connectionManager ) {
    return (List<T>) connectionManager.getConnectionDetailsByScheme( getKey() );
  }

  @Override public T prepare( T connectionDetails ) throws KettleException {
    return connectionDetails;
  }

  @Override public String sanitizeName( String string ) {
    return string;
  }

  // Utility method to perform variable substitution on values
  protected String getVar( String value, VariableSpace variableSpace ) {
    if ( variableSpace != null ) {
      return variableSpace.environmentSubstitute( value );
    }
    return value;
  }

  // Utility method to derive a boolean checkbox setting that may use variables instead
  protected static boolean getBooleanValueOfVariable( VariableSpace space, String variableName, String defaultValue ) {
    if ( !Utils.isEmpty( variableName ) ) {
      String value = space.environmentSubstitute( variableName );
      if ( !Utils.isEmpty( value ) ) {
        Boolean b = ValueMetaBase.convertStringToBoolean( value );
        return b != null && b;
      }
    }
    return Objects.equals( Boolean.TRUE, ValueMetaBase.convertStringToBoolean( defaultValue ) );
  }

  protected VariableSpace getSpace( ConnectionDetails connectionDetails ) {
    return connectionDetails.getSpace() == null ? Variables.getADefaultVariableSpace() : connectionDetails.getSpace();
  }
}
