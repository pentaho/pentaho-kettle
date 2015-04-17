/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SequenceMetaTests {

  @Test
  public void testSupport() {

    // According to our Meta, Oracle, PostGres,
    // Greenplum and Vertica support sequences
    DatabaseInterface[] support = new DatabaseInterface[] {
      new OracleDatabaseMeta(),
      new OracleRDBDatabaseMeta(),
      new VerticaDatabaseMeta(),
      new PostgreSQLDatabaseMeta(),
      new GreenplumDatabaseMeta(),
      new AS400DatabaseMeta(),
      new DB2DatabaseMeta(),
      new HypersonicDatabaseMeta(),
      new KingbaseESDatabaseMeta(),
      new NetezzaDatabaseMeta()
    };

    // the rest of the database metas say they don't support sequences
    DatabaseInterface[] doNotSupport = new DatabaseInterface[] {
      new MySQLDatabaseMeta(),
      new InfiniDbDatabaseMeta(),
      new InfobrightDatabaseMeta(),
      new DbaseDatabaseMeta(),
      new DerbyDatabaseMeta(),
      new ExtenDBDatabaseMeta(),
      new FirebirdDatabaseMeta(),
      new GenericDatabaseMeta(),
      new GuptaDatabaseMeta(),
      new H2DatabaseMeta(),
      new InformixDatabaseMeta(),
      new IngresDatabaseMeta(),
      new InterbaseDatabaseMeta(),
      new LucidDBDatabaseMeta(),
      new MonetDBDatabaseMeta(),
      new MSAccessDatabaseMeta(),
      new MSSQLServerDatabaseMeta(),
      new MSSQLServerNativeDatabaseMeta(),
      new NeoviewDatabaseMeta(),
      new RemedyActionRequestSystemDatabaseMeta(),
      new SAPDBDatabaseMeta(),
      new SAPR3DatabaseMeta(),
      new SQLiteDatabaseMeta(),
      new SybaseDatabaseMeta(),
      new SybaseIQDatabaseMeta(),
      new TeradataDatabaseMeta(),
      new UniVerseDatabaseMeta()
    };

    for ( DatabaseInterface db : support ) {
      assertSupports( db, true );
    }

    for ( DatabaseInterface db : doNotSupport ) {
      assertSupports( db, false );
    }
  }

  private static void assertSupports( DatabaseInterface db, boolean expected ) {
    if (expected) {
      assertTrue( db.getClass().getSimpleName(), db.supportsSequences() );
    } else {
      assertFalse( db.getClass().getSimpleName(), db.supportsSequences() );
    }
  }

  @Test
  public void testSQL() {

    DatabaseInterface databaseInterface;
    final String sequenceName = "sequence_name";

    databaseInterface = new OracleDatabaseMeta();
    assertEquals( "SELECT sequence_name.nextval FROM dual", databaseInterface
      .getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "SELECT sequence_name.currval FROM DUAL", databaseInterface
      .getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new OracleRDBDatabaseMeta();
    assertEquals( "SELECT sequence_name.nextval FROM dual", databaseInterface
      .getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "SELECT sequence_name.currval FROM DUAL", databaseInterface
      .getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new VerticaDatabaseMeta();
    assertEquals( "SELECT nextval('sequence_name')", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "SELECT currval('sequence_name')", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new PostgreSQLDatabaseMeta();
    assertEquals( "SELECT nextval('sequence_name')", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "SELECT currval('sequence_name')", databaseInterface
      .getSQLCurrentSequenceValue( sequenceName ) );
    assertEquals( "SELECT relname AS sequence_name FROM pg_catalog.pg_statio_all_sequences", databaseInterface
      .getSQLListOfSequences() );
    assertEquals( "SELECT relname AS sequence_name FROM pg_catalog.pg_statio_all_sequences WHERE relname = 'sequence_name'",
      databaseInterface.getSQLSequenceExists( sequenceName ) );

    databaseInterface = new GreenplumDatabaseMeta();
    assertEquals( "SELECT nextval('sequence_name')", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "SELECT currval('sequence_name')", databaseInterface
      .getSQLCurrentSequenceValue( sequenceName ) );
    assertEquals( "SELECT relname AS sequence_name FROM pg_catalog.pg_statio_all_sequences", databaseInterface
      .getSQLListOfSequences() );
    assertEquals( "SELECT relname AS sequence_name FROM pg_catalog.pg_statio_all_sequences WHERE relname = 'sequence_name'",
      databaseInterface.getSQLSequenceExists( sequenceName ) );

    databaseInterface = new AS400DatabaseMeta();
    assertEquals( "SELECT NEXT VALUE FOR sequence_name FROM SYSIBM.SYSDUMMY1", databaseInterface
      .getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "SELECT PREVIOUS VALUE FOR sequence_name FROM SYSIBM.SYSDUMMY1", databaseInterface
      .getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new DB2DatabaseMeta();
    assertEquals( "SELECT NEXT VALUE FOR sequence_name FROM SYSIBM.SYSDUMMY1", databaseInterface
      .getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "SELECT PREVIOUS VALUE FOR sequence_name FROM SYSIBM.SYSDUMMY1", databaseInterface
      .getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new HypersonicDatabaseMeta();
    assertEquals( "SELECT NEXT VALUE FOR sequence_name "
      + "FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_NAME = 'sequence_name'",
      databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "SELECT sequence_name.currval "
      + "FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_NAME = 'sequence_name'",
      databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new KingbaseESDatabaseMeta();
    assertEquals( "SELECT nextval('sequence_name')", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "SELECT currval('sequence_name')", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new NetezzaDatabaseMeta();
    assertEquals( "select next value for sequence_name", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "select last_value from sequence_name", databaseInterface
      .getSQLCurrentSequenceValue( sequenceName ) );

    // the rest of the database metas say they don't support sequences

    databaseInterface = new MySQLDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new InfiniDbDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new InfobrightDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new DbaseDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new DerbyDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new ExtenDBDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new FirebirdDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new GenericDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new GuptaDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new H2DatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new InformixDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new IngresDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new InterbaseDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new LucidDBDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new MonetDBDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new MSAccessDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new MSSQLServerDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new MSSQLServerNativeDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new NeoviewDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new RemedyActionRequestSystemDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new SAPDBDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new SAPR3DatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new SQLiteDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new SybaseDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new SybaseIQDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new TeradataDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new UniVerseDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );
  }
}
