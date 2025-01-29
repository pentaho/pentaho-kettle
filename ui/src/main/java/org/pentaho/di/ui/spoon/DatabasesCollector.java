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


package org.pentaho.di.ui.spoon;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.DatabaseManagementInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrey Khayrutdinov
 */
public class DatabasesCollector {
  private DatabaseManagementInterface dbManager;
  private final Repository repository;
  private List<String> dbNames;
  private Map<String, DatabaseMeta> dbMetaMap;

  public DatabasesCollector( DatabaseManagementInterface dbManager, Repository repository ) {
    this.dbManager = dbManager;
    this.repository = repository;
  }

  public void collectDatabases() throws KettleException {
    dbMetaMap = new HashMap<String, DatabaseMeta>();
    List<DatabaseMeta> dbMetaList;

    if ( dbManager != null ) {
      dbMetaList = dbManager.getAll();
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
