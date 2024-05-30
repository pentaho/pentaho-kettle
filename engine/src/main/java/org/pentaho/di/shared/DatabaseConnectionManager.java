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

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.ManagerFactory;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class uses the SharedObjectIO to retrieve and save shared objects. This is used by the UI.
 */
public class DatabaseConnectionManager {

  private SharedObjectsIO sharedObjectIO;
  private Map<String, DatabaseMeta> dbMetas = new HashMap<>();

  private Bowl bowl;

  public static DatabaseConnectionManager getInstance( Bowl bowl ) throws KettleXMLException {
    return new DatabaseConnectionManager( bowl );
  }
  private DatabaseConnectionManager( Bowl bowl ) throws KettleXMLException {
    this.bowl = bowl;
    this.sharedObjectIO = bowl.getSharedObjectsIO();
  }


  public List<DatabaseMeta> getDatabases() throws KettleXMLException {
    Map<String, Node> nodeMap = sharedObjectIO.getSharedObjects( String.valueOf( SharedObjectsIO.SharedObjectType.CONNECTION ) );
    populateDbMetaMap( nodeMap );

    return new ArrayList<>( dbMetas.values() );

  }

  private void populateDbMetaMap( Map<String, Node> nodesMap ) throws KettleXMLException {
    Map<String, DatabaseMeta> metaMap = new HashMap<>();
    for ( String name : nodesMap.keySet() ) {
      DatabaseMeta dbMeta = new DatabaseMeta( nodesMap.get( name ) );
      if ( !metaMap.containsKey( name ) ) {
        metaMap.put( name, dbMeta );
      }
    }
    this.dbMetas = metaMap;

  }

  /**
   * Factory for the DatabaseConnectionManager. This factory class is registered with BowlFactory registry
   * during the initialization in KettleEnvironment
   */
  public static class DbConnectionManagerFactory implements ManagerFactory<DatabaseConnectionManager> {
    public DatabaseConnectionManager apply( Bowl bowl ) throws KettleException {
      return DatabaseConnectionManager.getInstance( bowl );
    }
  }

}
