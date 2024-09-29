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

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.database.AthenaDatabaseMeta.AuthType;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.row.value.ValueMetaString;

import java.util.HashMap;
import java.util.Map;

public class AthenaDatabaseMetaTest {

  @BeforeClass
  public static void setupOnce() throws Exception {
    // this will make the type discoverable for DatabaseMeta
    DatabasePluginType.getInstance().searchPlugins();
  }

  @Test
  public void testDbMeta() throws Exception {
    DatabaseMeta meta = new DatabaseMeta( "dbMeta", "Athena", "Native", "host", "db", "443", null, null );
    assertEquals( AthenaDatabaseMeta.class, meta.getDatabaseInterface().getClass() );
    assertEquals( "com.amazon.athena.jdbc.AthenaDriver", meta.getDriverClass() );
    assertEquals( "athena", meta.getDatabaseInterface().getXulOverlayFile() );
    assertEquals( "useSchemaNameForTableList", false, meta.useSchemaNameForTableList() );
  }

  @Test
  public void testFieldQuoting() {
    DatabaseMeta meta = getDBMeta();
    assertEquals( "reserved words quoted", "`from`", meta.quoteField( "from" ) );
    assertEquals( "regular fields not quoted", "something", meta.quoteField( "something" ) );
  }

  private DatabaseMeta getDBMeta() {
    return new DatabaseMeta( "dbMeta", "Athena", "Native", "host", "db", "443", null, null );
  }

  @Test
  public void testUrl() throws Exception {
    AthenaDatabaseMeta dbMeta = new AthenaDatabaseMeta();

    dbMeta.setWorkGroup( "testworkgroup" );
    dbMeta.setRegion( "testregion" );
    dbMeta.setCatalog( "testcatalog" );
    dbMeta.setOutputLocation( "testlocation" );

    dbMeta.setAuthType( AuthType.ProfileCredentials );
    dbMeta.setProfileName( "atokenvalue" );

    String url = dbMeta.getURL( "hostn", "444", "testdatabase" );
    String[] urlParts = url.split( ";" );
    Map<String, String> urlParams = new HashMap<>( urlParts.length - 1 );
    assertEquals( "jdbc:athena://", urlParts[0] );
    for ( int i = 1; i < urlParts.length; i++ ) {
      String[] pair = urlParts[i].split( "=" );
      assertEquals( 2, pair.length );
      urlParams.put( pair[0], pair[1] );
    }
    assertEquals( "testworkgroup", urlParams.get( "WorkGroup" ) );
    assertEquals( "testregion", urlParams.get( "Region" ) );
    assertEquals( "testcatalog", urlParams.get( "Catalog" ) );
    assertEquals( "testdatabase", urlParams.get( "Database" ) );
    assertEquals( "testlocation", urlParams.get( "OutputLocation" ) );
    assertEquals( "atokenvalue", urlParams.get( "ProfileName" ) );
    assertEquals( "ProfileCredentials", urlParams.get( "CredentialsProvider" ) );
  }

  @Test
  public void testQuerySchema() {
    AthenaDatabaseMeta db = new AthenaDatabaseMeta();
    assertEquals( "SELECT * FROM thetable LIMIT 0", db.getSQLQueryFields( "thetable" ) );
  }

  @Test( expected = KettleDatabaseException.class )
  public void testNoPath() throws Exception {
    DatabaseMeta dbMeta = getDBMeta();
    dbMeta.getURL();
  }

  @Test
  public void testStringFieldDef() throws Exception {
    AthenaDatabaseMeta dbricks = new AthenaDatabaseMeta();
    String fieldDef = dbricks.getFieldDefinition( new ValueMetaString( "name" ), null, null, false, false, false );
    assertEquals( "VARCHAR()", fieldDef );
  }

}
