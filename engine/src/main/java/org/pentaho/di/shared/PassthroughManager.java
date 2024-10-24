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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * This Manager that does not cache anything. Complete passthrough to the provided SharedObjectsIO instance.
 *
*/
public abstract class PassthroughManager<T extends SharedObjectInterface<T>> implements SharedObjectsManagementInterface<T> {

  private final SharedObjectsIO sharedObjectsIO;
  private final String type;
  private Class<T> clazz;

  public PassthroughManager( SharedObjectsIO sharedObjectsIO, Class<T> clazz, String type ) {
    this.sharedObjectsIO = sharedObjectsIO;
    this.type = type;
  }

  /**
   * This method is used to create concrete SharedObjectInterface implementation class. This will be implemented by
   * subclasses.
   * @param node
   * @return
   * @throws KettleException
   */
  protected abstract T createSharedObjectUsingNode( Node node ) throws KettleException;


  @Override
  public void add( T object ) throws KettleException {
    Node node = object.toNode();
    sharedObjectsIO.saveSharedObject( type, object.getName(), node );
  }

  @Override
  public T get( String name) throws KettleException {
    Node node = sharedObjectsIO.getSharedObject( type, name );
    if ( node == null ) {
      return null;
    }

    return createSharedObjectUsingNode( node );
  }

  @Override
  public List<T> getAll( ) throws KettleException {
    Map<String, Node> nodeMap = sharedObjectsIO.getSharedObjects( type );
    List<T> result = new ArrayList<>( nodeMap.size() );

    for ( Node node : nodeMap.values() ) {
      result.add( createSharedObjectUsingNode( node ) );
    }
    return result;
  }

  @Override
  public void clear( ) throws KettleException {
    sharedObjectsIO.clear( type );
  }

  @Override
  public void remove( T object ) throws KettleException {
    remove( object.getName() );
  }

  @Override
  public void remove( String name) throws KettleException {
    sharedObjectsIO.delete( type, name );
  }


}
