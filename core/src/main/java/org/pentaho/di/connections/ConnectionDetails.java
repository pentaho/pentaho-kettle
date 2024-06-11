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
