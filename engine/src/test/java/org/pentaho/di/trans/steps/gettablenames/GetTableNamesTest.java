/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.gettablenames;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.test.util.InternalState.setInternalState;

public class GetTableNamesTest {
  private StepMockHelper<GetTableNamesMeta, GetTableNamesData> mockHelper;
  private GetTableNames getTableNamesSpy;
  private Database database;
  private GetTableNamesMeta getTableNamesMeta;
  private GetTableNamesData getTableNamesData;

  @Before
  public void setUp() throws Exception {
    mockHelper =
      new StepMockHelper<>( "Get Table Names", GetTableNamesMeta.class, GetTableNamesData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );

    getTableNamesSpy = Mockito.spy( new GetTableNames( mockHelper.stepMeta, mockHelper.stepDataInterface, 0,
      mockHelper.transMeta, mockHelper.trans ) );
    database = mock( Database.class );
    getTableNamesMeta = mock( GetTableNamesMeta.class );
    getTableNamesData = mock( GetTableNamesData.class );
  }

  @After
  public void tearDown() throws Exception {
    mockHelper.cleanUp();
  }

  @Test
  public void processIncludeTableIncludeSchemaTest() throws KettleException {
    prepareIncludeTableTest( true );
    getTableNamesSpy.processIncludeTable( new Object[] { "", "", "", "" } );
    //Regardless of include schema is true or false calls to isSystemTable and getTableFieldsMeta should be done
    //with the table name without the schema concatenated
    for ( String table : getTableNamesWithoutSchema() ) {
      verify( database ).isSystemTable( table );
      verify( database ).getTableFieldsMeta( "schema", table );
    }
    //getTablenames without including schema, must be called only once, because it is always needed
    //to call isSystemTable and getTableFieldsMeta without schema.
    //Since includeSchema in meta is set, then a call to getTablename including schema is also done.
    verify( database, times( 1 ) ).getTablenames( "schema", false );
    verify( database, times( 1 ) ).getTablenames( "schema", true );
  }

  @Test
  public void processIncludeTableDontIncludeSchemaTest() throws KettleException {
    prepareIncludeTableTest( false );
    getTableNamesSpy.processIncludeTable( new Object[] { "", "", "", "" } );
    //Regardless of include schema is true or false calls to isSystemTable and getTableFieldsMeta should be done
    //with the table name without the schema concatenated
    for ( String table : getTableNamesWithoutSchema() ) {
      verify( database ).isSystemTable( table );
      verify( database ).getTableFieldsMeta( "schema", table );
    }
    //getTablenames without including schema, must be called 2 times, one because includeSchema in meta is false,
    //the other because it is always needed to call isSystemTable and getTableFieldsMeta without schema.
    //No calls, with include schema are done.
    verify( database, times( 2 ) ).getTablenames( "schema", false );
    verify( database, times( 0 ) ).getTablenames( "schema", true );
  }

  @Test
  public void processIncludeViewIncludesSchemaTest() throws KettleException {
    prepareIncludeViewTest( true );
    getTableNamesSpy.processIncludeView( new Object[] { "", "", "", "" } );
    //Regardless of include schema is true or false calls to isSystemTable should be done
    //with the table name without the schema concatenated
    for ( String table : getTableNamesWithoutSchema() ) {
      verify( database ).isSystemTable( table );
    }
    //getViews without including schema, must be called only once, because it is always needed
    //to call isSystemTable without schema.
    //Since includeSchema in meta is set, then a call to getViews including schema is also done.
    verify( database, times( 1 ) ).getViews( "schema", false );
    verify( database, times( 1 ) ).getViews( "schema", true );
  }

  @Test
  public void processIncludeViewDontIncludeSchemaTest() throws KettleException {
    prepareIncludeViewTest( false );
    getTableNamesSpy.processIncludeView( new Object[] { "", "", "", "" } );
    //Regardless of include schema is true or false calls to isSystemTable should be done
    //with the table name without the schema concatenated
    for ( String table : getTableNamesWithoutSchema() ) {
      verify( database ).isSystemTable( table );
    }
    //getViews without including schema, must be called 2 times, one because includeSchema in meta is false,
    //the other because it is always needed to call isSystemTable without schema.
    //No calls, with include schema are done.
    verify( database, times( 2 ) ).getViews( "schema", false );
    verify( database, times( 0 ) ).getViews( "schema", true );
  }

  private void prepareIncludeViewTest( boolean includeSchema ) throws KettleException {
    setInternalState( getTableNamesSpy, "meta", getTableNamesMeta );
    setInternalState( getTableNamesSpy, "data", getTableNamesData );
    setInternalState( getTableNamesData, "db", database );
    setInternalState( getTableNamesData, "realSchemaName", "schema" );
    setInternalState( getTableNamesData, "realIsSystemObjectFieldName", "Y" );

    when( getTableNamesMeta.isIncludeView() ).thenReturn( true );
    when( getTableNamesMeta.isAddSchemaInOut() ).thenReturn( includeSchema );
    when( database.getViews( "schema", true ) ).thenReturn( getTableNamesWithSchema() );
    when( database.getViews( "schema", false ) ).thenReturn( getTableNamesWithoutSchema() );
  }

  private void prepareIncludeTableTest( boolean includeSchema ) throws KettleException {
    setInternalState( getTableNamesSpy, "meta", getTableNamesMeta );
    setInternalState( getTableNamesSpy, "data", getTableNamesData );
    setInternalState( getTableNamesData, "db", database );
    setInternalState( getTableNamesData, "realSchemaName", "schema" );
    setInternalState( getTableNamesData, "realIsSystemObjectFieldName", "Y" );

    when( getTableNamesMeta.isIncludeTable() ).thenReturn( true );
    when( getTableNamesMeta.isAddSchemaInOut() ).thenReturn( includeSchema );
    when( database.getTablenames( "schema", true ) ).thenReturn( getTableNamesWithSchema() );
    when( database.getTablenames( "schema", false ) ).thenReturn( getTableNamesWithoutSchema() );
    when( database.getCreateTableStatement( anyString(), any(), anyString(), anyBoolean(), anyString(), anyBoolean() ) ).thenReturn( "" );
  }

  private String[] getTableNamesWithoutSchema() {
    return new String[] { "table1", "table2", "table3", "table4" };
  }

  private String[] getTableNamesWithSchema() {
    return new String[] { "schema.table1", "schema.table2", "schema.table3", "schema.table4" };
  }
}
