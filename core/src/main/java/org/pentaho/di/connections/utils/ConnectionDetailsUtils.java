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

package org.pentaho.di.connections.utils;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import static org.pentaho.metastore.util.PentahoDefaults.NAMESPACE;

public class ConnectionDetailsUtils {

  private ConnectionDetailsUtils() {
    // Static clas
  }

  /**
   * Clones a given connection details instance.
   * <p>
   * Cloning is done by according to the meta store metadata from present meta store annotations in the connection
   * details type. Essentially, behaving as a save followed by a load from an in-memory meta-store.
   * As such any information not stored in the meta store is not preserved in the cloned instance.
   * <p>
   * The given connection details must have a name set or the operation fails.
   *
   * @param instance The connection details instance to clone.
   * @return The cloned connection details.
   */
  public static <T extends ConnectionDetails> T cloneMeta( T instance ) throws MetaStoreException {
    // Use a temporary in-memory meta store to save to and then load from.
    IMetaStore metaStore = new MemoryMetaStore();

    @SuppressWarnings( "unchecked" )
    MetaStoreFactory<T> metaStoreFactory =
      new MetaStoreFactory<>( (Class<T>) instance.getClass(), metaStore, NAMESPACE );

    String originalName = instance.getName();

    // Meta-store requires a name to save and load.
    boolean hasBlankName = StringUtils.isBlank( originalName );
    if ( hasBlankName ) {
      instance.setName( "NAME" );
    }

    metaStoreFactory.saveElement( instance );

    T clone = metaStoreFactory.loadElement( instance.getName() );

    if ( hasBlankName ) {
      // Restore previous blank state.
      instance.setName( originalName );
      clone.setName( originalName );
    }

    return clone;
  }
}
