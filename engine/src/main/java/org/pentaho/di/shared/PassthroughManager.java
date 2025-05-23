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


package org.pentaho.di.shared;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryElementInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * This Manager that does not cache anything. Complete passthrough to the provided SharedObjectsIO instance.
 *
*/
public abstract class PassthroughManager<T extends SharedObjectInterface<T> & RepositoryElementInterface>
  implements SharedObjectsManagementInterface<T> {

  private final SharedObjectsIO sharedObjectsIO;
  private final String type;

  protected PassthroughManager( SharedObjectsIO sharedObjectsIO, String type ) {
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
