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


package org.pentaho.di.connections;

import org.pentaho.di.connections.utils.ConnectionDetailsUtils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.util.Collections;
import java.util.Map;

/**
 * Created by bmorrise on 2/13/19.
 */
public interface ConnectionDetails {
  String getName();

  void setName( String name );

  String getType();

  String getDescription();

  /**
   * Gets props associated with this ConnectionDetails.
   * Allows implementors to expose connection properties without
   * requiring clients to have the implementation as a dependency.
   */
  default Map<String, String> getProperties() {
    return Collections.emptyMap();
  }

  default Object openDialog( Object wTabFolder, Object propsUI ) {
    //noop if not defined
    return null;
  }

  default void closeDialog() {
    //noop if not defined
  }

  default void setDescription( String description ) {
    //remove the default when done implementing
  }

  VariableSpace getSpace();

  void setSpace( VariableSpace space );

  /**
   * Clones the connection details instance.
   * <p>
   * The default implementation delegates cloning to {@link ConnectionDetailsUtils#cloneMeta(ConnectionDetails)}.
   *
   * @return The cloned connection details.
   */
  default ConnectionDetails cloneDetails() throws MetaStoreException {
    return ConnectionDetailsUtils.cloneMeta( this );
  }
}
