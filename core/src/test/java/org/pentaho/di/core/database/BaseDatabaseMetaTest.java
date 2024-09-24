/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;
import org.pentaho.di.repository.LongObjectId;

public class BaseDatabaseMetaTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();
  BaseDatabaseMeta nativeMeta, jndiMeta;

  @Before
  public void setupOnce() throws Exception {
    nativeMeta = new ConcreteBaseDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
    jndiMeta = new ConcreteBaseDatabaseMeta();
    KettleClientEnvironment.init();
  }

  @Test
  public void testShowIsTreatedAsAResultsQuery() throws Exception {
    List<SqlScriptStatement> sqlScriptStatements =
      new H2DatabaseMeta().getSqlScriptStatements( "show annotations from service" );
    assertTrue( sqlScriptStatements.get( 0 ).isQuery() );
  }

  @Test
  public void testDefaultSettings() throws Exception {
    // Note - this method should only use native.
    // The jndi meta is used for mutations of the meta, and it would
    // not be threadsafe in a multi-threaded testing environment
    // (each test run in its own thread).
    assertEquals( -1, nativeMeta.getDefaultDatabasePort() );
    assertTrue( nativeMeta.supportsSetCharacterStream() );
    assertTrue( nativeMeta.supportsAutoInc() );
    assertEquals( "", nativeMeta.getLimitClause( 5 ) );
    assertEquals( 0, nativeMeta.getNotFoundTK( true ) );
    assertEquals( "", nativeMeta.getSQLNextSequenceValue( "FOO" ) );
    assertEquals( "", nativeMeta.getSQLCurrentSequenceValue( "FOO" ) );
    assertEquals( "", nativeMeta.getSQLSequenceExists( "FOO" ) );
    assertTrue( nativeMeta.isFetchSizeSupported() );
    assertFalse( nativeMeta.needsPlaceHolder() );
    assertTrue( nativeMeta.supportsSchemas() );
    assertTrue( nativeMeta.supportsCatalogs() );
    assertTrue( nativeMeta.supportsEmptyTransactions() );
    assertEquals( "SUM", nativeMeta.getFunctionSum() );
    assertEquals( "AVG", nativeMeta.getFunctionAverage() );
    assertEquals( "MIN", nativeMeta.getFunctionMinimum() );
    assertEquals( "MAX", nativeMeta.getFunctionMaximum() );
    assertEquals( "COUNT", nativeMeta.getFunctionCount() );
    assertEquals( "\"", nativeMeta.getStartQuote() );
    assertEquals( "\"", nativeMeta.getEndQuote() );
    assertEquals( "FOO.BAR", nativeMeta.getSchemaTableCombination( "FOO", "BAR" ) );
    assertEquals( DatabaseMeta.CLOB_LENGTH, nativeMeta.getMaxTextFieldLength() );
    assertEquals( DatabaseMeta.CLOB_LENGTH, nativeMeta.getMaxVARCHARLength() );
    assertTrue( nativeMeta.supportsTransactions() );
    assertFalse( nativeMeta.supportsSequences() );
    assertTrue( nativeMeta.supportsBitmapIndex() );
    assertTrue( nativeMeta.supportsSetLong() );
    assertArrayEquals( new String[] {}, nativeMeta.getReservedWords() );
    assertTrue( nativeMeta.quoteReservedWords() );
    assertFalse( nativeMeta.supportsRepository() );
    assertArrayEquals( new String[] { "TABLE" }, nativeMeta.getTableTypes() );
    assertArrayEquals( new String[] { "VIEW" }, nativeMeta.getViewTypes() );
    assertArrayEquals( new String[] { "SYNONYM" }, nativeMeta.getSynonymTypes() );
    assertFalse( nativeMeta.useSchemaNameForTableList() );
    assertTrue( nativeMeta.supportsViews() );
    assertFalse( nativeMeta.supportsSynonyms() );
    assertNull( nativeMeta.getSQLListOfProcedures() );
    assertNull( nativeMeta.getSQLListOfSequences() );
    assertTrue( nativeMeta.supportsFloatRoundingOnUpdate() );
    assertNull( nativeMeta.getSQLLockTables( new String[] { "FOO" } ) );
    assertNull( nativeMeta.getSQLUnlockTables( new String[] { "FOO" } ) );
    assertTrue( nativeMeta.supportsTimeStampToDateConversion() );
    assertTrue( nativeMeta.supportsBatchUpdates() );
    assertFalse( nativeMeta.supportsBooleanDataType() );
    assertFalse( nativeMeta.supportsTimestampDataType() );
    assertTrue( nativeMeta.preserveReservedCase() );
    assertTrue( nativeMeta.isDefaultingToUppercase() );
    Map<String, String> emptyMap = new HashMap<String, String>();
    assertEquals( emptyMap, nativeMeta.getExtraOptions() );
    assertEquals( ";", nativeMeta.getExtraOptionSeparator() );
    assertEquals( "=", nativeMeta.getExtraOptionValueSeparator() );
    assertEquals( ";", nativeMeta.getExtraOptionIndicator() );
    assertTrue( nativeMeta.supportsOptionsInURL() );
    assertNull( nativeMeta.getExtraOptionsHelpText() );
    assertTrue( nativeMeta.supportsGetBlob() );
    assertNull( nativeMeta.getConnectSQL() );
    assertTrue( nativeMeta.supportsSetMaxRows() );
    assertFalse( nativeMeta.isUsingConnectionPool() );
    assertEquals( ConnectionPoolUtil.defaultMaximumNrOfConnections, nativeMeta.getMaximumPoolSize() );
    assertEquals( ConnectionPoolUtil.defaultInitialNrOfConnections, nativeMeta.getInitialPoolSize() );
    assertFalse( nativeMeta.isPartitioned() );
    assertArrayEquals( new PartitionDatabaseMeta[0], nativeMeta.getPartitioningInformation() );
    Properties emptyProps = new Properties();
    assertEquals( emptyProps, nativeMeta.getConnectionPoolingProperties() );
    assertTrue( nativeMeta.needsToLockAllTables() );
    assertTrue( nativeMeta.isStreamingResults() );
    assertFalse( nativeMeta.isQuoteAllFields() );
    assertFalse( nativeMeta.isForcingIdentifiersToLowerCase() );
    assertFalse( nativeMeta.isForcingIdentifiersToUpperCase() );
    assertFalse( nativeMeta.isUsingDoubleDecimalAsSchemaTableSeparator() );
    assertTrue( nativeMeta.isRequiringTransactionsOnQueries() );
    assertEquals( "org.pentaho.di.core.database.DatabaseFactory", nativeMeta.getDatabaseFactoryName() );
    assertNull( nativeMeta.getPreferredSchemaName() );
    assertFalse( nativeMeta.supportsSequenceNoMaxValueOption() );
    assertFalse( nativeMeta.requiresCreateTablePrimaryKeyAppend() );
    assertFalse( nativeMeta.requiresCastToVariousForIsNull() );
    assertFalse( nativeMeta.isDisplaySizeTwiceThePrecision() );
    assertTrue( nativeMeta.supportsPreparedStatementMetadataRetrieval() );
    assertFalse( nativeMeta.supportsResultSetMetadataRetrievalOnly() );
    assertFalse( nativeMeta.isSystemTable( "FOO" ) );
    assertTrue( nativeMeta.supportsNewLinesInSQL() );
    assertNull( nativeMeta.getSQLListOfSchemas() );
    assertEquals( 0, nativeMeta.getMaxColumnsInIndex() );
    assertTrue( nativeMeta.supportsErrorHandlingOnBatchUpdates() );
    assertTrue( nativeMeta.isExplorable() );
    assertNull( nativeMeta.getXulOverlayFile() );
    assertTrue( nativeMeta.onlySpaces( "   \t   \n  \r   " ) );
    assertFalse( nativeMeta.isMySQLVariant() );
    assertTrue( nativeMeta.canTest() );
    assertTrue( nativeMeta.requiresName() );
    assertTrue( nativeMeta.releaseSavepoint() );
    Variables v = new Variables();
    v.setVariable( "FOOVARIABLE", "FOOVALUE" );
    DatabaseMeta dm = new DatabaseMeta();
    dm.setDatabaseInterface( nativeMeta );
    assertEquals( "", nativeMeta.getDataTablespaceDDL( v, dm ) );
    assertEquals( "", nativeMeta.getIndexTablespaceDDL( v, dm ) );
    assertFalse( nativeMeta.useSafePoints() );
    assertTrue( nativeMeta.supportsErrorHandling() );
    assertEquals( "'DATA'", nativeMeta.getSQLValue( new ValueMetaString( "FOO" ), "DATA", null ) );
    assertEquals( "'15'", nativeMeta.getSQLValue( new ValueMetaString( "FOO" ), "15", null ) );
    assertEquals( "_", nativeMeta.getFieldnameProtector() );
    assertEquals( "_1ABC_123", nativeMeta.getSafeFieldname( "1ABC 123" ) );
    BaseDatabaseMeta tmpSC = new ConcreteBaseDatabaseMeta( ) {
      @Override
      public String[] getReservedWords() {
        return new String[] { "SELECT" };
      }
    };
    assertEquals( "SELECT_", tmpSC.getSafeFieldname( "SELECT" ) );
    assertEquals( "NOMAXVALUE", nativeMeta.getSequenceNoMaxValueOption() );
    assertTrue( nativeMeta.supportsAutoGeneratedKeys() );
    assertNull( nativeMeta.customizeValueFromSQLType( new ValueMetaString( "FOO" ), null, 0 ) );
    assertTrue( nativeMeta.fullExceptionLog( new RuntimeException( "xxxx" ) ) );
  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testDeprecatedItems() throws Exception {
    assertEquals( "'2016-08-11'", nativeMeta.getSQLValue( new ValueMetaDate( "FOO" ), new Date( 116, 7, 11 ), "YYYY-MM-dd" ) );
    assertEquals( "\"FOO\".\"BAR\"", nativeMeta.getBackwardsCompatibleSchemaTableCombination( "FOO", "BAR" ) );
    assertEquals( "\"null\".\"BAR\"", nativeMeta.getBackwardsCompatibleSchemaTableCombination( null, "BAR" ) ); // not sure this is right ...
    assertEquals( "FOO\".\"BAR\"", nativeMeta.getBackwardsCompatibleSchemaTableCombination( "FOO\"", "BAR" ) );
    assertEquals( "FOO\".BAR\"", nativeMeta.getBackwardsCompatibleSchemaTableCombination( "FOO\"", "BAR\"" ) );
    assertEquals( "\"FOO\"", nativeMeta.getBackwardsCompatibleTable( "FOO" ) );
    assertEquals( "\"null\"", nativeMeta.getBackwardsCompatibleTable( null ) ); // not sure this should happen but it does
    assertEquals( "FOO\"", nativeMeta.getBackwardsCompatibleTable( "FOO\"" ) );
    assertEquals( "\"FOO", nativeMeta.getBackwardsCompatibleTable( "\"FOO" ) );

  }

  @Test
  public void testDefaultSQLStatements() {
    // Note - this method should use only native meta.
    // Use of the jndi meta here could create a race condition
    // when test cases are run by multiple threads
    String lineSep = System.getProperty( "line.separator" );
    String expected = "ALTER TABLE FOO DROP BAR" + lineSep;
    assertEquals( "insert into \"FOO\".\"BAR\"(KEYFIELD, VERSIONFIELD) values (0, 1)",
        nativeMeta.getSQLInsertAutoIncUnknownDimensionRow( "\"FOO\".\"BAR\"", "KEYFIELD", "VERSIONFIELD" ) );
    assertEquals( "select count(*) FROM FOO", nativeMeta.getSelectCountStatement( "FOO" ) );
    assertEquals( "COL9", nativeMeta.generateColumnAlias( 9, "FOO" ) );
    assertEquals( "[SELECT 1, INSERT INTO FOO VALUES(BAR), DELETE FROM BAR]", nativeMeta.parseStatements( "SELECT 1;INSERT INTO FOO VALUES(BAR);DELETE FROM BAR" ).toString() );
    assertEquals( "CREATE TABLE ", nativeMeta.getCreateTableStatement() );
    assertEquals( "DROP TABLE IF EXISTS FOO", nativeMeta.getDropTableIfExistsStatement( "FOO" ) );
  }

  @Test
  public void testGettersSetters() {
    // Note - this method should *ONLY* use the jndi meta and not native one.
    // This is the only method in this test class that mutates the meta.
    jndiMeta.setUsername( "FOO" );
    assertEquals( "FOO", jndiMeta.getUsername() );
    jndiMeta.setPassword( "BAR" );
    assertEquals( "BAR", jndiMeta.getPassword() );
    jndiMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_JNDI );
    assertEquals( "", jndiMeta.getUsername() );
    assertEquals( "", jndiMeta.getPassword() );
    assertFalse( jndiMeta.isChanged() );
    jndiMeta.setChanged( true );
    assertTrue( jndiMeta.isChanged() );
    jndiMeta.setName( "FOO" );
    assertEquals( "FOO", jndiMeta.getName() );
    assertEquals( "FOO", jndiMeta.getDisplayName() );
    jndiMeta.setName( null );
    assertNull( jndiMeta.getName() );
    assertEquals( "FOO", jndiMeta.getDisplayName() );
    jndiMeta.setDisplayName( null );
    assertNull( jndiMeta.getDisplayName() );
    jndiMeta.setDatabaseName( "FOO" );
    assertEquals( "FOO", jndiMeta.getDatabaseName() );
    assertEquals( "-1", jndiMeta.getDatabasePortNumberString() );
    jndiMeta.setDatabasePortNumberString( "9876" );
    assertEquals( "9876", jndiMeta.getDatabasePortNumberString() );
    jndiMeta.setDatabasePortNumberString( null );
    assertEquals( "9876", jndiMeta.getDatabasePortNumberString() ); // not sure I agree with this behavior
    jndiMeta.setHostname( "FOO" );
    assertEquals( "FOO", jndiMeta.getHostname() );
    LongObjectId id = new LongObjectId( 9876 );
    jndiMeta.setObjectId( id );
    assertEquals( id, jndiMeta.getObjectId() );
    jndiMeta.setServername( "FOO" );
    assertEquals( "FOO", jndiMeta.getServername() );
    jndiMeta.setDataTablespace( "FOO" );
    assertEquals( "FOO", jndiMeta.getDataTablespace() );
    jndiMeta.setIndexTablespace( "FOO" );
    assertEquals( "FOO", jndiMeta.getIndexTablespace() );
    Properties attrs = jndiMeta.getAttributes();
    Properties testAttrs = new Properties();
    testAttrs.setProperty( "FOO", "BAR" );
    jndiMeta.setAttributes( testAttrs );
    assertEquals( testAttrs, jndiMeta.getAttributes() );
    jndiMeta.setAttributes( attrs ); // reset attributes back to what they were...
    jndiMeta.setSupportsBooleanDataType( true );
    assertTrue( jndiMeta.supportsBooleanDataType() );
    jndiMeta.setSupportsTimestampDataType( true );
    assertTrue( jndiMeta.supportsTimestampDataType() );
    jndiMeta.setPreserveReservedCase( false );
    assertFalse( jndiMeta.preserveReservedCase() );
    jndiMeta.addExtraOption( "JNDI", "FOO", "BAR" );
    Map<String, String> expectedOptionsMap = new HashMap<String, String>();
    expectedOptionsMap.put( "JNDI.FOO", "BAR" );
    assertEquals( expectedOptionsMap, jndiMeta.getExtraOptions() );
    jndiMeta.setConnectSQL( "SELECT COUNT(*) FROM FOO" );
    assertEquals( "SELECT COUNT(*) FROM FOO", jndiMeta.getConnectSQL() );
    jndiMeta.setUsingConnectionPool( true );
    assertTrue( jndiMeta.isUsingConnectionPool() );
    jndiMeta.setMaximumPoolSize( 15 );
    assertEquals( 15, jndiMeta.getMaximumPoolSize() );
    jndiMeta.setInitialPoolSize( 5 );
    assertEquals( 5, jndiMeta.getInitialPoolSize() );
    jndiMeta.setPartitioned( true );
    assertTrue( jndiMeta.isPartitioned() );
    PartitionDatabaseMeta[] clusterInfo = new PartitionDatabaseMeta[1];
    PartitionDatabaseMeta aClusterDef = new PartitionDatabaseMeta( "FOO", "BAR", "WIBBLE", "NATTIE" );
    aClusterDef.setUsername( "FOOUSER" );
    aClusterDef.setPassword( "BARPASSWORD" );
    clusterInfo[0] = aClusterDef;
    jndiMeta.setPartitioningInformation( clusterInfo );
    PartitionDatabaseMeta[] gotPartitions = jndiMeta.getPartitioningInformation();
    // MB: Can't use arrayEquals because the PartitionDatabaseMeta doesn't have a toString. :(
    // assertArrayEquals( clusterInfo, gotPartitions );
    assertTrue( gotPartitions != null );
    if ( gotPartitions != null ) {
      assertEquals( 1, gotPartitions.length );
      PartitionDatabaseMeta compareWith = gotPartitions[0];
      // MB: Can't use x.equals(y) because PartitionDatabaseMeta doesn't override equals... :(
      assertEquals( aClusterDef.getClass(), compareWith.getClass() );
      assertEquals( aClusterDef.getDatabaseName(), compareWith.getDatabaseName() );
      assertEquals( aClusterDef.getHostname(), compareWith.getHostname() );
      assertEquals( aClusterDef.getPartitionId(), compareWith.getPartitionId() );
      assertEquals( aClusterDef.getPassword(), compareWith.getPassword() );
      assertEquals( aClusterDef.getPort(), compareWith.getPort() );
      assertEquals( aClusterDef.getUsername(), compareWith.getUsername() );
    }
    Properties poolProperties = new Properties();
    poolProperties.put( "FOO", "BAR" );
    poolProperties.put( "BAR", "FOO" );
    poolProperties.put( "ZZZZZZZZZZZZZZ", "Z.Z.Z.Z.Z.Z.Z.Z.a.a.a.a.a.a.a.a.a" );
    poolProperties.put( "TOM", "JANE" );
    poolProperties.put( "AAAAAAAAAAAAA", "BBBBB.BBB.BBBBBBB.BBBBBBBB.BBBBBBBBBBBBBB" );
    jndiMeta.setConnectionPoolingProperties( poolProperties );
    Properties compareWithProps = jndiMeta.getConnectionPoolingProperties();
    assertEquals( poolProperties, compareWithProps );
    jndiMeta.setStreamingResults( false );
    assertFalse( jndiMeta.isStreamingResults() );
    jndiMeta.setQuoteAllFields( true );
    jndiMeta.setForcingIdentifiersToLowerCase( true );
    jndiMeta.setForcingIdentifiersToUpperCase( true );
    assertTrue( jndiMeta.isQuoteAllFields() );
    assertTrue( jndiMeta.isForcingIdentifiersToLowerCase() );
    assertTrue( jndiMeta.isForcingIdentifiersToUpperCase() );
    jndiMeta.setUsingDoubleDecimalAsSchemaTableSeparator( true );
    assertTrue( jndiMeta.isUsingDoubleDecimalAsSchemaTableSeparator() );
    jndiMeta.setPreferredSchemaName( "FOO" );
    assertEquals( "FOO", jndiMeta.getPreferredSchemaName() );
  }

  private int rowCnt = 0;

  @Test
  public void testCheckIndexExists() throws Exception {
    Database db = Mockito.mock(  Database.class );
    ResultSet rs = Mockito.mock( ResultSet.class );
    DatabaseMetaData dmd = Mockito.mock( DatabaseMetaData.class );
    DatabaseMeta dm = Mockito.mock( DatabaseMeta.class );
    Mockito.when( dm.getQuotedSchemaTableCombination( "", "FOO" ) ).thenReturn( "FOO" );
    Mockito.when( rs.next() ).thenAnswer( new Answer<Boolean>() {
      public Boolean answer( InvocationOnMock invocation ) throws Throwable {
        rowCnt++;
        return new Boolean( rowCnt < 3 );
      }
    } );
    Mockito.when( db.getDatabaseMetaData() ).thenReturn( dmd );
    Mockito.when( dmd.getIndexInfo( null, null, "FOO", false, true ) ).thenReturn( rs );
    Mockito.when( rs.getString( "COLUMN_NAME" ) ).thenAnswer( new Answer<String>() {
      @Override
      public String answer( InvocationOnMock invocation ) throws Throwable {
        if ( rowCnt == 1 ) {
          return "ROW1COL2";
        } else if ( rowCnt == 2 ) {
          return "ROW2COL2";
        } else {
          return null;
        }
      }
    } );
    Mockito.when(  db.getDatabaseMeta() ).thenReturn( dm );
  }

}
