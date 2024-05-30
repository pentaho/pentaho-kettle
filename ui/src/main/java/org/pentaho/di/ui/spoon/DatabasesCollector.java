/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.DatabaseConnectionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrey Khayrutdinov
 */
public class DatabasesCollector {
  private DatabaseConnectionManager dbManager;
  private final Repository repository;
  private AbstractMeta meta;
  private List<String> dbNames;
  private Map<String, DatabaseMeta> dbMetaMap;

  public DatabasesCollector( DatabaseConnectionManager dbManager, Repository repository ) {
    this( dbManager, null, repository );
  }

  public DatabasesCollector( DatabaseConnectionManager dbManager, AbstractMeta meta, Repository repository ) {
    this.dbManager = dbManager;
    this.meta = meta;
    this.repository = repository;
  }

  public void collectDatabases() throws KettleException {
    dbMetaMap = new HashMap<String, DatabaseMeta>();
    List<DatabaseMeta> dbMetaList;

    // SharedObjects
    if ( dbManager != null ) {
      dbMetaList = dbManager.getDatabases();
      addToMetaMap( dbMetaList );
    }

    // local databases
    if ( meta != null ) {
      dbMetaList = meta.getLocalDbMetas();
      addToMetaMap( dbMetaList );
    }

    //Repository
    if ( repository != null ) {
      List<DatabaseMeta> dbsFromRepo = repository.readDatabases();
      for ( DatabaseMeta db : dbsFromRepo ) {
        if ( !dbMetaMap.containsKey( db.getName() ) ) {
          dbMetaMap.put( db.getName(), db );
        }
      }
    }

    dbNames = new ArrayList<String>( dbMetaMap.keySet() );
    Collections.sort( dbNames, String.CASE_INSENSITIVE_ORDER );
  }

  public List<String> getDatabaseNames() throws KettleException {
    collectDatabases();
    if ( dbNames == null ) {
      throw exception();
    }
    return Collections.unmodifiableList( dbNames );
  }

  public DatabaseMeta getMetaFor( String dbName ) {
    if ( dbMetaMap == null ) {
      throw exception();
    }
    return dbMetaMap.get( dbName );
  }

  private static IllegalStateException exception() {
    return new IllegalStateException( "Call collectDatabases() first" );
  }

  private void addToMetaMap( List<DatabaseMeta> metaList ) {
    for ( DatabaseMeta db : metaList ) {
      dbMetaMap.put( db.getName(), db );
    }
  }
}
