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

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.cluster.SlaveServerManagementInterface;
import org.pentaho.di.cluster.SlaveServerManager;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This Manager that does not cache anything. Complete passthrough to the provided SharedObjectsIO instance.
 *
*/
public class PassthroughSlaveServerManager implements SlaveServerManagementInterface {

  private final SharedObjectsIO sharedObjectsIO;
  public PassthroughSlaveServerManager(SharedObjectsIO sharedObjectsIO ) {
    this.sharedObjectsIO = sharedObjectsIO;
  }

  @Override
  public void add( SlaveServer slaveServer ) throws KettleException {
    //Node node = SlaveServerManager.toNode( sharedObjectInterface );
    Node node = slaveServer.toNode();
    sharedObjectsIO.saveSharedObject(  SlaveServerManager.SLAVESERVER_TYPE, slaveServer.getName(), node );
  }

  @Override
  public SlaveServer get( String name) throws KettleException {
    Node node = sharedObjectsIO.getSharedObject( SlaveServerManager.SLAVESERVER_TYPE, name );
    if ( node == null ) {
      return null;
    }

    return new SlaveServer( node );
  }

  @Override
  public List<SlaveServer> getAll( ) throws KettleException {
    Map<String, Node> nodeMap = sharedObjectsIO.getSharedObjects( SlaveServerManager.SLAVESERVER_TYPE );
    List<SlaveServer> result = new ArrayList<>( nodeMap.size() );

    for ( Node node : nodeMap.values() ) {
      result.add( new SlaveServer( node ) );
    }
    return result;
  }

  @Override
  public void clear( ) throws KettleException {
    sharedObjectsIO.clear( SlaveServerManager.SLAVESERVER_TYPE );
  }

  @Override
  public void remove( SlaveServer slaveServer ) throws KettleException {
    remove( slaveServer.getName() );
  }
  @Override
  public void remove( String sharedObjectName) throws KettleException {
    sharedObjectsIO.delete( SlaveServerManager.SLAVESERVER_TYPE, sharedObjectName );
  }
}
