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


package org.pentaho.di.core.database;

import static org.junit.Assert.*;

import org.junit.Test;

public class ReleaseSavePointTest {

  DatabaseInterface[] noSupport = new DatabaseInterface[] {
    new Exasol4DatabaseMeta(),
    new InfiniDbDatabaseMeta(),
    new InfobrightDatabaseMeta(),
    new MySQLDatabaseMeta(),
    new MariaDBDatabaseMeta(),
    new OracleDatabaseMeta()
  };

  DatabaseInterface[] support = new DatabaseInterface[] {
    new AS400DatabaseMeta(),
    new DB2DatabaseMeta(),
    new DbaseDatabaseMeta(),
    new DerbyDatabaseMeta(),
    new ExtenDBDatabaseMeta(),
    new FirebirdDatabaseMeta(),
    new GenericDatabaseMeta(),
    new GreenplumDatabaseMeta(),
    new GuptaDatabaseMeta(),
    new H2DatabaseMeta(),
    new HypersonicDatabaseMeta(),
    new InformixDatabaseMeta(),
    new IngresDatabaseMeta(),
    new InterbaseDatabaseMeta(),
    new KingbaseESDatabaseMeta(),
    new LucidDBDatabaseMeta(),
    new MondrianNativeDatabaseMeta(),
    new MSAccessDatabaseMeta(),
    new MSSQLServerDatabaseMeta(),
    new MSSQLServerNativeDatabaseMeta(),
    new MonetDBDatabaseMeta(),
    new NeoviewDatabaseMeta(),
    new NetezzaDatabaseMeta(),
    new OracleRDBDatabaseMeta(),
    new PostgreSQLDatabaseMeta(),
    new RemedyActionRequestSystemDatabaseMeta(),
    new SAPDBDatabaseMeta(),
    new SQLiteDatabaseMeta(),
    new SybaseDatabaseMeta(),
    new SybaseIQDatabaseMeta(),
    new TeradataDatabaseMeta(),
    new UniVerseDatabaseMeta(),
    new VerticaDatabaseMeta()
  };

  @Test
  public void testReleaseSavePointBooleans() {
    try {
      for ( DatabaseInterface db : support ) {
        assertTrue( db.releaseSavepoint() );
      }

      for ( DatabaseInterface db : noSupport ) {
        assertFalse( db.releaseSavepoint() );
      }
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }
}
