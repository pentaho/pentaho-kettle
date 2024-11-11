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

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrey Khayrutdinov
 */
public class DatabasesCollector {
  private final AbstractMeta meta;
  private final Repository repository;

  private List<String> dbNames;
  private Map<String, DatabaseMeta> names2metas;

  public DatabasesCollector( AbstractMeta meta, Repository repository ) {
    this.meta = meta;
    this.repository = repository;
  }

  public void collectDatabases() throws KettleException {
    List<DatabaseMeta> dbsFromMeta = meta.getDatabases();
    names2metas = new HashMap<String, DatabaseMeta>( dbsFromMeta.size() );
    for ( DatabaseMeta db : dbsFromMeta ) {
      names2metas.put( db.getName(), db );
    }

    if ( repository != null ) {
      List<DatabaseMeta> dbsFromRepo = repository.readDatabases();
      for ( DatabaseMeta db : dbsFromRepo ) {
        if ( !names2metas.containsKey( db.getName() ) ) {
          names2metas.put( db.getName(), db );
        }
      }
    }

    dbNames = new ArrayList<String>( names2metas.keySet() );
    Collections.sort( dbNames, String.CASE_INSENSITIVE_ORDER );
  }

  public List<String> getDatabaseNames() {
    if ( dbNames == null ) {
      throw exception();
    }
    return Collections.unmodifiableList( dbNames );
  }

  public DatabaseMeta getMetaFor( String dbName ) {
    if ( names2metas == null ) {
      throw exception();
    }
    return names2metas.get( dbName );
  }

  private static IllegalStateException exception() {
    return new IllegalStateException( "Call collectDatabases() first" );
  }
}
