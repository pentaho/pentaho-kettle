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

package org.pentaho.di.trans.steps.dimensionlookup;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

public class DimensionLookupIT {
  private StepMeta mockStepMeta;
  private DimensionLookupMeta mockDimensionLookupMeta;
  private DimensionLookupData mockDimensionLookupData;
  private DatabaseMeta mockDatabaseMeta;
  private RowMetaInterface mockOutputRowMeta;
  private TransMeta mockTransMeta;
  private Trans mockTrans;
  private Connection mockConnection;

  private DimensionLookup dimensionLookup;

  @Before
  public void setup() {
    mockStepMeta = mock( StepMeta.class );
    mockDimensionLookupMeta = mock( DimensionLookupMeta.class );
    mockDimensionLookupData = mock( DimensionLookupData.class );
    mockDatabaseMeta = mock( DatabaseMeta.class );
    mockOutputRowMeta = mock( RowMetaInterface.class );
    mockTransMeta = mock( TransMeta.class );
    mockTrans = mock( Trans.class );
    mockConnection = mock( Connection.class );

    mockDimensionLookupData.outputRowMeta = mockOutputRowMeta;

    String stepName = "testName";
    when( mockStepMeta.getName() ).thenReturn( stepName );
    when( mockTransMeta.findStep( stepName ) ).thenReturn( mockStepMeta );
    when( mockTrans.getLogLevel() ).thenReturn( LogLevel.ROWLEVEL );

    dimensionLookup = new DimensionLookup( mockStepMeta, mockDimensionLookupData, 0, mockTransMeta, mockTrans );
    dimensionLookup.init( mockDimensionLookupMeta, mockDimensionLookupData );
  }

  public void prepareMocksForInsertTest() {
    mockDimensionLookupData.schemaTable = "testSchemaTable";
    ValueMetaInterface mockKeyValueMeta = mock( ValueMetaInterface.class );
    when( mockDimensionLookupMeta.getDatabaseMeta() ).thenReturn( mockDatabaseMeta );
    when( mockDatabaseMeta.quoteField( anyString() ) ).thenAnswer( new Answer<String>() {
      public String answer( InvocationOnMock invocation ) throws Throwable {
        return "\"" + invocation.getArguments()[0] + "\"";
      }
    } );
    String keyField = "testKeyField";
    when( mockDimensionLookupMeta.getKeyField() ).thenReturn( keyField );
    when( mockDimensionLookupMeta.getVersionField() ).thenReturn( "testVersionField" );
    when( mockDimensionLookupMeta.getDateFrom() ).thenReturn( "1900-01-01" );
    when( mockDimensionLookupMeta.getDateTo() ).thenReturn( "1901-01-01" );
    when( mockDimensionLookupMeta.getKeyLookup() ).thenReturn( new String[] {} );
    when( mockDimensionLookupMeta.getFieldLookup() ).thenReturn( new String[] {} );
    when( mockDimensionLookupMeta.getFieldUpdate() ).thenReturn( new int[] {} );
    mockDimensionLookupData.keynrs = new int[] {};
    mockDimensionLookupData.fieldnrs = new int[] {};
    Database mockDatabase = mock( Database.class );
    when( mockDatabase.getConnection() ).thenReturn( mockConnection );
    mockDimensionLookupData.db = mockDatabase;
    when( mockKeyValueMeta.getName() ).thenReturn( "testKey" );
    when( mockOutputRowMeta.getValueMeta( 0 ) ).thenReturn( mockKeyValueMeta );
  }

  @Test
  public void testDimInsertPreparesStatementWithReturnKeysForNullTechnicalKey() throws KettleException,
    SQLException {
    RowMetaInterface mockMetaInterface = mock( RowMetaInterface.class );
    Object[] row = new Object[0];
    Long technicalKey = null;
    boolean newEntry = false;
    Long versionNr = 2L;
    Date dateFrom = new Date();
    Date dateTo = new Date();
    prepareMocksForInsertTest();
    dimensionLookup.dimInsert( mockMetaInterface, row, technicalKey, newEntry, versionNr, dateFrom, dateTo );
    // Insert statement with keys
    verify( mockConnection, times( 1 ) ).prepareStatement( anyString(), eq( Statement.RETURN_GENERATED_KEYS ) );
    // Update statement without
    verify( mockConnection, times( 1 ) ).prepareStatement( anyString() );
  }

  @Test
  public void testDimInsertPreparesStatementWithReturnKeysForNotNullTechnicalKey() throws KettleException,
    SQLException {
    RowMetaInterface mockMetaInterface = mock( RowMetaInterface.class );
    Object[] row = new Object[0];
    Long technicalKey = 1L;
    boolean newEntry = false;
    Long versionNr = 2L;
    Date dateFrom = new Date();
    Date dateTo = new Date();
    prepareMocksForInsertTest();
    dimensionLookup.dimInsert( mockMetaInterface, row, technicalKey, newEntry, versionNr, dateFrom, dateTo );
    // Neither insert nor update should have keys
    verify( mockConnection, times( 2 ) ).prepareStatement( anyString() );
  }
}
