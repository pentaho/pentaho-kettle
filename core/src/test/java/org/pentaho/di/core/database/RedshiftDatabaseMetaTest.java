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

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for RedshiftDatabaseMeta
 */
public class RedshiftDatabaseMetaTest {

  private RedshiftDatabaseMeta dbMeta;

  @Before
  public void setUp() throws Exception {
    dbMeta = new RedshiftDatabaseMeta();
    dbMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testExtraOption() {
    Map<String, String> opts = dbMeta.getExtraOptions();
    assertNotNull( opts );
    assertEquals( "true", opts.get( "REDSHIFT.tcpKeepAlive" ) );
  }

  @Test
  public void testGetDefaultDatabasePort() throws Exception {
    assertEquals( 5439, dbMeta.getDefaultDatabasePort() );
    dbMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_JNDI );
    assertEquals( -1, dbMeta.getDefaultDatabasePort() );
  }

  @Test
  public void testGetDriverClass() throws Exception {
    assertEquals( "com.amazon.redshift.jdbc4.Driver", dbMeta.getDriverClass() );
    dbMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_ODBC );
    assertEquals( "sun.jdbc.odbc.JdbcOdbcDriver", dbMeta.getDriverClass() );
  }

  @Test
  public void testGetURL() throws Exception {
    assertEquals( "jdbc:redshift://:/", dbMeta.getURL( "", "", "" ) );
    assertEquals( "jdbc:redshift://rs.pentaho.com:4444/myDB",
      dbMeta.getURL( "rs.pentaho.com", "4444", "myDB" ) );
    dbMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_ODBC );
    assertEquals( "jdbc:odbc:myDB", dbMeta.getURL( null, "Not Null", "myDB" ) );
  }

  @Test
  public void testGetExtraOptionsHelpText() throws Exception {
    assertEquals( "http://docs.aws.amazon.com/redshift/latest/mgmt/configure-jdbc-connection.html",
      dbMeta.getExtraOptionsHelpText() );
  }

  @Test
  public void testIsFetchSizeSupported() throws Exception {
    assertFalse( dbMeta.isFetchSizeSupported() );
  }

  @Test
  public void testSupportsSetMaxRows() throws Exception {
    assertFalse( dbMeta.supportsSetMaxRows() );
  }

  @Test
  public void testGetUsedLibraries() throws Exception {
    String[] libs = dbMeta.getUsedLibraries();
    assertNotNull( libs );
    assertEquals( 1, libs.length );
    assertEquals( "RedshiftJDBC4_1.0.10.1010.jar", libs[0] );
  }
}
