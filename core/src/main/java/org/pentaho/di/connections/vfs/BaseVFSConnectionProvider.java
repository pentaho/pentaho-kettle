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


package org.pentaho.di.connections.vfs;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.vfs2.FileSystemOptions;
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

  @NonNull
  private Supplier<ConnectionManager> connectionManagerSupplier = ConnectionManager::getInstance;

  /**
   * This method was added solely to support unit testing of deprecated behavior.
   *
   * @param connectionManagerSupplier A supplier of connection manager.
   * @deprecated
   */
  @Deprecated( forRemoval = true )
  protected void setConnectionManagerSupplier( @NonNull Supplier<ConnectionManager> connectionManagerSupplier ) {
    this.connectionManagerSupplier = Objects.requireNonNull( connectionManagerSupplier );
  }

  @Override
  public List<String> getNames() {
    return getNames( connectionManagerSupplier.get() );
  }

  @Override
  public List<T> getConnectionDetails() {
    return getConnectionDetails( connectionManagerSupplier.get() );
  }

  @Override
  public List<String> getNames( @NonNull ConnectionManager connectionManager ) {
    return connectionManager.getNamesByType( getClass() );
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public List<T> getConnectionDetails( @NonNull ConnectionManager connectionManager ) {
    return (List<T>) connectionManager.getConnectionDetailsByScheme( getKey() );
  }

  @Override
  public T prepare( T connectionDetails ) throws KettleException {
    return connectionDetails;
  }

  @Override
  public String sanitizeName( String string ) {
    return string;
  }

  @Override
  public FileSystemOptions getOpts( T connectionDetails ) {
    return new FileSystemOptions();
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

  @NonNull
  protected VariableSpace getSpace( @NonNull T connectionDetails ) {
    return connectionDetails.getSpace() == null ? Variables.getADefaultVariableSpace() : connectionDetails.getSpace();
  }
}
