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
}
