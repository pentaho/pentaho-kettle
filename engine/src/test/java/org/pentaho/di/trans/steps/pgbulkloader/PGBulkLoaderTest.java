/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.pgbulkloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class PGBulkLoaderTest {
  private StepMockHelper<PGBulkLoaderMeta, PGBulkLoaderData> stepMockHelper;
  private PGBulkLoader pgBulkLoader;

  private static final String CONNECTION_NAME = "PSQLConnect";
  private static final String CONNECTION_DB_NAME = "test1181";
  private static final String CONNECTION_DB_HOST = "localhost";
  private static final String CONNECTION_DB_PORT = "5093";
  private static final String CONNECTION_DB_USERNAME = "postgres";
  private static final String CONNECTION_DB_PASSWORD = "password";
  private static final String DB_NAME_OVVERRIDE = "test1181_2";
  private static final String DB_NAME_EMPTY = "";

  private static final String PG_TEST_CONNECTION =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<connection> <name>" + CONNECTION_NAME + "</name><server>" + CONNECTION_DB_HOST + "</server><type>POSTGRESQL</type><access>Native</access><database>" + CONNECTION_DB_NAME + "</database>"
          + "  <port>" + CONNECTION_DB_PORT + "</port><username>" + CONNECTION_DB_USERNAME + "</username><password>Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde</password></connection>";

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    stepMockHelper = new StepMockHelper<>( "PostgreSQL Bulk Loader", PGBulkLoaderMeta.class, PGBulkLoaderData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    pgBulkLoader = new PGBulkLoader( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta, stepMockHelper.trans );
  }

  @After
  public void tearDown() throws Exception {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testCreateCommandLine() throws Exception {
    PGBulkLoaderMeta meta = mock( PGBulkLoaderMeta.class );
    doReturn( new DatabaseMeta() ).when( meta ).getDatabaseMeta();
    doReturn( new String[0] ).when( meta ).getFieldStream();
    PGBulkLoaderData data = mock( PGBulkLoaderData.class );

    PGBulkLoader spy = spy( pgBulkLoader );
    doReturn( new Object[0] ).when( spy ).getRow();
    doReturn( "" ).when( spy ).getCopyCommand();
    doNothing().when( spy ).connect();
    doNothing().when( spy ).processTruncate();
    spy.processRow( meta, data );
    verify( spy ).processTruncate();
  }

  @Test
  public void testDBNameOverridden_IfDbNameOverrideSetUp() throws Exception {
    // Db Name Override is set up
    PGBulkLoaderMeta pgBulkLoaderMock = getPgBulkLoaderMock( DB_NAME_OVVERRIDE );
    Database database = pgBulkLoader.getDatabase( pgBulkLoader, pgBulkLoaderMock );
    assertNotNull( database );
    // Verify DB name is overridden
    assertEquals( DB_NAME_OVVERRIDE, database.getDatabaseMeta().getDatabaseName() );
    // Check additionally other connection information
    assertEquals( CONNECTION_NAME, database.getDatabaseMeta().getName() );
    assertEquals( CONNECTION_DB_HOST, database.getDatabaseMeta().getHostname() );
    assertEquals( CONNECTION_DB_PORT, database.getDatabaseMeta().getDatabasePortNumberString() );
    assertEquals( CONNECTION_DB_USERNAME, database.getDatabaseMeta().getUsername() );
    assertEquals( CONNECTION_DB_PASSWORD, database.getDatabaseMeta().getPassword() );
  }

  @Test
  public void testDBNameNOTOverridden_IfDbNameOverrideEmpty() throws Exception {
    // Db Name Override is empty
    PGBulkLoaderMeta pgBulkLoaderMock = getPgBulkLoaderMock( DB_NAME_EMPTY );
    Database database = pgBulkLoader.getDatabase( pgBulkLoader, pgBulkLoaderMock );
    assertNotNull( database );
    // Verify DB name is NOT overridden
    assertEquals( CONNECTION_DB_NAME, database.getDatabaseMeta().getDatabaseName() );
    // Check additionally other connection information
    assertEquals( CONNECTION_NAME, database.getDatabaseMeta().getName() );
    assertEquals( CONNECTION_DB_HOST, database.getDatabaseMeta().getHostname() );
    assertEquals( CONNECTION_DB_PORT, database.getDatabaseMeta().getDatabasePortNumberString() );
    assertEquals( CONNECTION_DB_USERNAME, database.getDatabaseMeta().getUsername() );
    assertEquals( CONNECTION_DB_PASSWORD, database.getDatabaseMeta().getPassword() );
  }

  @Test
  public void testDBNameNOTOverridden_IfDbNameOverrideNull() throws Exception {
    // Db Name Override is null
    PGBulkLoaderMeta pgBulkLoaderMock = getPgBulkLoaderMock( null );
    Database database = pgBulkLoader.getDatabase( pgBulkLoader, pgBulkLoaderMock );
    assertNotNull( database );
    // Verify DB name is NOT overridden
    assertEquals( CONNECTION_DB_NAME, database.getDatabaseMeta().getDatabaseName() );
    // Check additionally other connection information
    assertEquals( CONNECTION_NAME, database.getDatabaseMeta().getName() );
    assertEquals( CONNECTION_DB_HOST, database.getDatabaseMeta().getHostname() );
    assertEquals( CONNECTION_DB_PORT, database.getDatabaseMeta().getDatabasePortNumberString() );
    assertEquals( CONNECTION_DB_USERNAME, database.getDatabaseMeta().getUsername() );
    assertEquals( CONNECTION_DB_PASSWORD, database.getDatabaseMeta().getPassword() );
  }

  private static PGBulkLoaderMeta getPgBulkLoaderMock( String DbNameOverride ) throws KettleXMLException {
    PGBulkLoaderMeta pgBulkLoaderMetaMock = mock( PGBulkLoaderMeta.class );
    when( pgBulkLoaderMetaMock.getDbNameOverride() ).thenReturn( DbNameOverride );
    DatabaseMeta databaseMeta = getDatabaseMetaSpy();
    when( pgBulkLoaderMetaMock.getDatabaseMeta() ).thenReturn( databaseMeta );
    return pgBulkLoaderMetaMock;
  }

  private static DatabaseMeta getDatabaseMetaSpy() throws KettleXMLException {
    DatabaseMeta databaseMeta = spy( new DatabaseMeta( PG_TEST_CONNECTION ) );
    return databaseMeta;
  }
}
