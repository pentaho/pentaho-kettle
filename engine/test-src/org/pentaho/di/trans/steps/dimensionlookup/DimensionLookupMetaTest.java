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

package org.pentaho.di.trans.steps.dimensionlookup;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStore;


public class DimensionLookupMetaTest {

  public static final String databaseXML =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
      + "<connection>" + "<name>lookup</name>" + "<server>127.0.0.1</server>" + "<type>H2</type>"
      + "<access>Native</access>" + "<database>mem:db</database>" + "<port></port>" + "<username>sa</username>"
      + "<password></password>" + "</connection>";


  @Before
  public void setUp() throws Exception {
    LogChannelInterfaceFactory logChannelInterfaceFactory = mock( LogChannelInterfaceFactory.class );
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    KettleLogStore.setLogChannelInterfaceFactory( logChannelInterfaceFactory );
    when( logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        logChannelInterface );
  }

  @Test
  public void testGetFields() throws Exception {

    RowMeta extraFields = new RowMeta();
    extraFields.addValueMeta( new ValueMetaString( "field1" ) );

    DatabaseMeta dbMeta = mock( DatabaseMeta.class );

    DimensionLookupMeta meta = spy( new DimensionLookupMeta() );
    meta.setUpdate( false );
    meta.setKeyField( null );
    meta.setFieldLookup( new String[] { "field1" } );
    meta.setFieldStream( new String[] { "" } );
    meta.setDatabaseMeta( dbMeta );
    doReturn( extraFields ).when( meta ).getDatabaseTableFields( (Database) anyObject(), anyString(), anyString() );
    doReturn( mock( LogChannelInterface.class ) ).when( meta ).getLog();

    RowMeta row = new RowMeta();
    try {
      meta.getFields( row, "DimensionLookupMetaTest", new RowMeta[] { row }, null, null, null, null );
    } catch ( Throwable e ) {
      Assert.assertTrue( e.getMessage().contains(
          BaseMessages.getString( DimensionLookupMeta.class, "DimensionLookupMeta.Error.NoTechnicalKeySpecified" ) ) );
    }
  }

  @Test
  public void testUseDefaultSchemaName() throws Exception {
    KettleEnvironment.init();

    String schemaName = "";
    String tableName = "tableName";
    String schemaTable = "default.tableName";
    String keyField = "keyField";

    DatabaseMeta databaseMeta = spy( new DatabaseMeta( databaseXML ) {
      public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean use_autoinc ) {
        return "someValue";
      }
    } );
    when( databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName ) ).thenReturn( schemaTable );

    DimensionLookupMeta dlm = new DimensionLookupMeta();
    dlm.setUpdate( true );
    dlm.setDatabaseMeta( databaseMeta );
    dlm.setTableName( tableName );
    dlm.setSchemaName( schemaName );
    dlm.setKeyLookup( new String[] { "keyLookup1", "keyLookup2" } );
    dlm.setKeyStream( new String[] { "keyStream1", "keyStream2" } );
    dlm.setFieldLookup( new String[] { "fieldLookup1", "fieldLookup2" } );
    dlm.setFieldStream( new String[] { "FieldStream1", "FieldStream2" } );
    dlm.setFieldUpdate( new int[] { 1, 2 } );
    dlm.setKeyField( keyField );

    StepMeta stepMeta = mock( StepMeta.class );

    RowMetaInterface rowMetaInterface = mock( RowMetaInterface.class );
    when( rowMetaInterface.size() ).thenReturn( 1 );

    Repository repository = mock( Repository.class );
    IMetaStore metaStore = mock( IMetaStore.class );

    SQLStatement sqlStatement =
        dlm.getSQLStatements( new TransMeta(), stepMeta, rowMetaInterface, repository, metaStore );

    String sql = sqlStatement.getSQL();
    assertEquals( 3, StringUtils.countMatches( sql, schemaTable ) );
  }

  @Test
  public void testProvidesModelerMeta() throws Exception {

    final RowMeta rowMeta = Mockito.mock( RowMeta.class );
    final DimensionLookupMeta dimensionLookupMeta = new DimensionLookupMeta() {
      @Override Database createDatabaseObject() {
        return mock( Database.class );
      }

      @Override protected RowMetaInterface getDatabaseTableFields( Database db, String schemaName, String tableName )
        throws KettleDatabaseException {
        assertEquals( "aSchema", schemaName );
        assertEquals( "aDimTable", tableName );
        return rowMeta;
      }
    };
    dimensionLookupMeta.setFieldLookup( new String[] { "f1", "f2", "f3" } );
    dimensionLookupMeta.setKeyLookup( new String[] {"k1"} );
    dimensionLookupMeta.setFieldStream( new String[] { "s4", "s5", "s6" } );
    dimensionLookupMeta.setKeyStream( new String[] {"ks1"} );
    dimensionLookupMeta.setSchemaName( "aSchema" );
    dimensionLookupMeta.setTableName( "aDimTable" );

    final DimensionLookupData dimensionLookupData = new DimensionLookupData();
    assertEquals( rowMeta, dimensionLookupMeta.getRowMeta( dimensionLookupData ) );
    assertEquals( 4, dimensionLookupMeta.getDatabaseFields().size() );
    assertEquals( "f1", dimensionLookupMeta.getDatabaseFields().get( 0 ) );
    assertEquals( "f2", dimensionLookupMeta.getDatabaseFields().get( 1 ) );
    assertEquals( "f3", dimensionLookupMeta.getDatabaseFields().get( 2 ) );
    assertEquals( "k1", dimensionLookupMeta.getDatabaseFields().get( 3 ) );
    assertEquals( 4, dimensionLookupMeta.getStreamFields().size() );
    assertEquals( "s4", dimensionLookupMeta.getStreamFields().get( 0 ) );
    assertEquals( "s5", dimensionLookupMeta.getStreamFields().get( 1 ) );
    assertEquals( "s6", dimensionLookupMeta.getStreamFields().get( 2 ) );
    assertEquals( "ks1", dimensionLookupMeta.getStreamFields().get( 3 ) );
  }
}
