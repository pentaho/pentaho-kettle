/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.repository.kdr;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryDatabaseDelegate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * @author Andrey Khayrutdinov
 */
public class KettleDatabaseRepository_DatabaseNames_Test {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  private KettleDatabaseRepository repository;
  private KettleDatabaseRepositoryDatabaseDelegate databaseDelegate;

  @Before
  public void setUp() throws Exception {
    repository = spy( new KettleDatabaseRepository() );
    databaseDelegate = spy( new KettleDatabaseRepositoryDatabaseDelegate( repository ) );
    repository.databaseDelegate = databaseDelegate;
  }


  @Test
  public void getDatabaseId_ExactMatch() throws Exception {
    final String name = UUID.randomUUID().toString();
    final ObjectId expectedId = new StringObjectId( "expected" );
    doReturn( expectedId ).when( databaseDelegate ).getDatabaseID( name );

    ObjectId id = repository.getDatabaseID( name );
    assertEquals( expectedId, id );
  }

  @Test
  public void getDatabaseId_InsensitiveMatch() throws Exception {
    final String name = "databaseWithCamelCase";
    final String lookupName = name.toLowerCase();
    assertNotSame( lookupName, name );

    final ObjectId expected = new StringObjectId( "expected" );
    doReturn( expected ).when( databaseDelegate ).getDatabaseID( name );
    doReturn( null ).when( databaseDelegate ).getDatabaseID( lookupName );

    DatabaseMeta db = new DatabaseMeta();
    db.setName( name );
    db.setObjectId( expected );
    List<DatabaseMeta> dbs = Collections.singletonList( db );
    doReturn( dbs ).when( repository ).getDatabases();

    ObjectId id = repository.getDatabaseID( lookupName );
    assertEquals( expected, id );
  }

  @Test
  public void getDatabaseId_ReturnsExactMatch_PriorToCaseInsensitiveMatch() throws Exception {
    final String exact = "databaseExactMatch";
    final String similar = exact.toLowerCase();
    assertNotSame( similar, exact );

    final ObjectId exactId = new StringObjectId( "exactId" );
    doReturn( exactId ).when( databaseDelegate ).getDatabaseID( exact );
    final ObjectId similarId = new StringObjectId( "similarId" );
    doReturn( similarId ).when( databaseDelegate ).getDatabaseID( similar );

    DatabaseMeta db = new DatabaseMeta();
    db.setName( exact );
    DatabaseMeta another = new DatabaseMeta();
    db.setName( similar );
    List<DatabaseMeta> dbs = Arrays.asList( another, db );
    doReturn( dbs ).when( repository ).getDatabases();

    ObjectId id = this.repository.getDatabaseID( exact );
    assertEquals( exactId, id );
  }

}
