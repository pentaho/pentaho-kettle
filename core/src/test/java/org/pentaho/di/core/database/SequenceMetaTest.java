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

package org.pentaho.di.core.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pentaho.di.core.util.Utils;

public class SequenceMetaTest {

  @Test
  public void testSupport() {

    DatabaseInterface[] support = new DatabaseInterface[] {
      new AS400DatabaseMeta(),
      new DB2DatabaseMeta(),
      new GreenplumDatabaseMeta(),
      new HypersonicDatabaseMeta(),
      new KingbaseESDatabaseMeta(),
      new MonetDBDatabaseMeta(),
      new MSSQLServerDatabaseMeta(),
      new MSSQLServerNativeDatabaseMeta(),
      new NetezzaDatabaseMeta(),
      new OracleDatabaseMeta(),
      new OracleRDBDatabaseMeta(),
      new PostgreSQLDatabaseMeta(),
      new RedshiftDatabaseMeta(),
      new VerticaDatabaseMeta(),
      new Vertica5DatabaseMeta(),
    };

    DatabaseInterface[] doNotSupport = new DatabaseInterface[] {
      new CacheDatabaseMeta(),
      new Exasol4DatabaseMeta(),
      new ExtenDBDatabaseMeta(),
      new ExtenDBDatabaseMeta(),
      new FirebirdDatabaseMeta(),
      new GenericDatabaseMeta(),
      new GuptaDatabaseMeta(),
      new H2DatabaseMeta(),
      new InfiniDbDatabaseMeta(),
      new InfobrightDatabaseMeta(),
      new InformixDatabaseMeta(),
      new IngresDatabaseMeta(),
      new InterbaseDatabaseMeta(),
      new LucidDBDatabaseMeta(),
      new MondrianNativeDatabaseMeta(),
      new MySQLDatabaseMeta(),
      new MariaDBDatabaseMeta(),
      new NeoviewDatabaseMeta(),
      new RemedyActionRequestSystemDatabaseMeta(),
      new SAPDBDatabaseMeta(),
      new SQLiteDatabaseMeta(),
      new SybaseDatabaseMeta(),
      new SybaseIQDatabaseMeta(),
      new TeradataDatabaseMeta(),
      new UniVerseDatabaseMeta(),
      new VectorWiseDatabaseMeta()
    };

    for ( DatabaseInterface db : support ) {
      assertSupports( db, true );
    }

    for ( DatabaseInterface db : doNotSupport ) {
      assertSupports( db, false );
    }
  }

  public static void assertSupports( DatabaseInterface db, boolean expected ) {
    String dbType = db.getClass().getSimpleName();
    if ( expected ) {
      assertTrue( dbType, db.supportsSequences() );
      assertFalse( dbType + ": List of Sequences", Utils.isEmpty( db.getSQLListOfSequences() ) );
      assertFalse( dbType + ": Sequence Exists", Utils.isEmpty( db.getSQLSequenceExists( "testSeq" ) ) );
      assertFalse( dbType + ": Current Value", Utils.isEmpty( db.getSQLCurrentSequenceValue( "testSeq" ) ) );
      assertFalse( dbType + ": Next Value", Utils.isEmpty( db.getSQLNextSequenceValue( "testSeq" ) ) );
    } else {
      assertFalse( db.getClass().getSimpleName(), db.supportsSequences() );
      assertTrue( dbType + ": List of Sequences", Utils.isEmpty( db.getSQLListOfSequences() ) );
      assertTrue( dbType + ": Sequence Exists", Utils.isEmpty( db.getSQLSequenceExists( "testSeq" ) ) );
      assertTrue( dbType + ": Current Value", Utils.isEmpty( db.getSQLCurrentSequenceValue( "testSeq" ) ) );
      assertTrue( dbType + ": Next Value", Utils.isEmpty( db.getSQLNextSequenceValue( "testSeq" ) ) );
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

    databaseInterface = new MonetDBDatabaseMeta();
    assertEquals( "SELECT next_value_for( 'sys', 'sequence_name' )", databaseInterface
      .getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "SELECT get_value_for( 'sys', 'sequence_name' )", databaseInterface
      .getSQLCurrentSequenceValue( sequenceName ) );
    assertEquals( "SELECT name FROM sys.sequences", databaseInterface.getSQLListOfSequences() );
    assertEquals( "SELECT * FROM sys.sequences WHERE name = 'sequence_name'", databaseInterface
      .getSQLSequenceExists( sequenceName ) );

    databaseInterface = new MSSQLServerDatabaseMeta();
    assertEquals( "SELECT NEXT VALUE FOR sequence_name",
      databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "SELECT current_value FROM sys.sequences WHERE name = 'sequence_name'",
      databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );
    assertEquals( "SELECT name FROM sys.sequences", databaseInterface.getSQLListOfSequences() );
    assertEquals( "SELECT 1 FROM sys.sequences WHERE name = 'sequence_name'",
      databaseInterface.getSQLSequenceExists( sequenceName ) );

    databaseInterface = new MSSQLServerNativeDatabaseMeta();
    assertEquals( "SELECT NEXT VALUE FOR sequence_name",
      databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "SELECT current_value FROM sys.sequences WHERE name = 'sequence_name'",
      databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );
    assertEquals( "SELECT name FROM sys.sequences", databaseInterface.getSQLListOfSequences() );
    assertEquals( "SELECT 1 FROM sys.sequences WHERE name = 'sequence_name'",
      databaseInterface.getSQLSequenceExists( sequenceName ) );

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

    databaseInterface = new NeoviewDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new RemedyActionRequestSystemDatabaseMeta();
    assertEquals( "", databaseInterface.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", databaseInterface.getSQLCurrentSequenceValue( sequenceName ) );

    databaseInterface = new SAPDBDatabaseMeta();
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
