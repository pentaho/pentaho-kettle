/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Test;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.DatabaseConnectionManager;
import org.pentaho.di.shared.DatabaseManagementInterface;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class DatabasesCollectorTest {

  @Test
  public void repositoryIsNull() throws Exception {
    DatabasesCollector collector = new DatabasesCollector( prepareDbManager( mockDb( "mysql" ) ), null );
    collector.collectDatabases();

    assertEquals( collector.getDatabaseNames().size(), 1 );
    assertNotNull( collector.getMetaFor( "mysql" ) );
  }

  @Test
  public void repositoryDuplicates() throws Exception {
    DatabaseConnectionManager mgr = prepareDbManager( mockDb( "mysql" ) );
    Repository repository = mockRepository( mockDb( "mysql" ) );

    DatabasesCollector collector = new DatabasesCollector( mgr, repository );
    collector.collectDatabases();

    assertEquals( collector.getDatabaseNames().size(), 1 );
    assertNotNull( collector.getMetaFor( "mysql" ) );
  }

  @Test
  public void repositoryContainsUnique() throws Exception {
    DatabaseConnectionManager mgr = prepareDbManager( mockDb( "mysql" ), mockDb( "oracle" ) );
    Repository repository = mockRepository( mockDb( "h2" ) );

    DatabasesCollector collector = new DatabasesCollector( mgr, repository );
    collector.collectDatabases();

    assertEquals( collector.getDatabaseNames().size(), 3 );
  }

  @Test
  public void collectDatabasesRepIsNullTest() throws Exception {
    DatabaseConnectionManager mgr = prepareDbManager( mockDb( "mysql" ), mockDb( "oracle" ) );
    DatabasesCollector collector = new DatabasesCollector( mgr, null );
    collector.collectDatabases();
    assertEquals( collector.getDatabaseNames().size(), 2 );

    AbstractMeta meta = prepareMeta( mockDb( "postgres" ) );
    collector = new DatabasesCollector( meta.getDatabaseManagementInterface(), null );
    collector.collectDatabases();
    assertEquals( collector.getDatabaseNames().size(), 1 );

  }

  private static AbstractMeta prepareMeta( DatabaseMeta... metas ) throws Exception {
    if ( metas == null ) {
      metas = new DatabaseMeta[ 0 ];
    }

    AbstractMeta meta = mock( AbstractMeta.class );
    List<DatabaseMeta> dbs = asList( metas );

    DatabaseManagementInterface dbMgr = mock( DatabaseManagementInterface.class );
    when( dbMgr.getAll() ).thenReturn( dbs );
    when( meta.getDatabaseManagementInterface() ).thenReturn( dbMgr );
    return meta;
  }

  private static DatabaseConnectionManager prepareDbManager( DatabaseMeta... metas ) throws KettleException {
    if ( metas == null ) {
      metas = new DatabaseMeta[ 0 ];
    }

    DatabaseConnectionManager mgr = mock( DatabaseConnectionManager.class );
    List<DatabaseMeta> dbs = asList( metas );
    when( mgr.getAll() ).thenReturn( dbs );
    return mgr;
  }

  private static Repository mockRepository( DatabaseMeta... metas ) throws Exception {
    if ( metas == null ) {
      metas = new DatabaseMeta[ 0 ];
    }

    Repository repo = mock( Repository.class );
    when( repo.readDatabases() ).thenReturn( asList( metas ) );
    return repo;
  }

  private static DatabaseMeta mockDb( String name ) {
    DatabaseMeta mock = mock( DatabaseMeta.class );
    when( mock.getName() ).thenReturn( name );
    when( mock.getDisplayName() ).thenReturn( name );
    return mock;
  }
}
