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

  /**
   * Called by {@link ConnectionManager#save(ConnectionDetails)} after a connection has been successfully saved to the
   * default meta store. Unlike {@link #prepare(ConnectionDetails)}, which also runs when testing or using a connection,
   * this is only called when the connection is actually saved.
   *
   * @param connectionManager The connection manager that saved the connection.
   * @param connectionDetails A copy of the saved connection details, as reloaded from the meta store.
   */
  default void afterSave( ConnectionManager connectionManager, T connectionDetails ) {
  }
}
