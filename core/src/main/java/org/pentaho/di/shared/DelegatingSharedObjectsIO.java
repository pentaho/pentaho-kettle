/*!
 * Copyright 2024 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.di.shared;

import org.pentaho.di.core.exception.KettleException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Node;


/**
 * A Read-only wrapper around multiple other SharedObjectsIO implementations that combines them by precedence order.
 *
 */
public class DelegatingSharedObjectsIO implements SharedObjectsIO {

  private final List<SharedObjectsIO> stores;

  /**
   * Construct a new instance with a prioritized list of SharedObjectsIO instances.
   * <p>
   * The earlier a store exists in this list, the higher the priority it is.
   *
   * @param stores prioritized list of SharedObjectsIO instances
   */
  public DelegatingSharedObjectsIO( SharedObjectsIO... stores ) {
    this.stores = new ArrayList<SharedObjectsIO>( Arrays.asList( stores ) );
  }

  @Override
  public Map<String, Node> getSharedObjects( String type ) throws KettleException {
    Map<String, Node> retMap = new HashMap<>();
    for ( SharedObjectsIO store : stores ) {
      Map<String, Node> storeMap = store.getSharedObjects( type );
      for ( Map.Entry<String,Node> entry : storeMap.entrySet() ) {
        retMap.putIfAbsent( entry.getKey(), entry.getValue() );
      }
    }
    return retMap;
  }

  @Override
  public Node getSharedObject( String type, String name ) throws KettleException {
    for ( SharedObjectsIO store : stores ) {
      Node node = store.getSharedObject( type, name );
      if ( node != null ) {
        return node;
      }
    }
    return null;
  }

  @Override
  public void delete( String type, String name ) throws KettleException {
    throw new UnsupportedOperationException( "Read-only data structure" );
  }

  @Override
  public void clear( String type ) throws KettleException {
    throw new UnsupportedOperationException( "Read-only data structure" );
  }

  @Override
  public void saveSharedObject( String type, String name, Node node ) throws KettleException {
    throw new UnsupportedOperationException( "Read-only data structure" );
  }
}
