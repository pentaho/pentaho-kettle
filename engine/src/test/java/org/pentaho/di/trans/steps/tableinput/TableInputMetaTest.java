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

package org.pentaho.di.trans.steps.tableinput;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

/**
 * User: Dzmitry Stsiapanau Date: 2/4/14 Time: 5:47 PM
 */
public class TableInputMetaTest {
  LoadSaveTester loadSaveTester;
  Class<TableInputMeta> testMetaClass = TableInputMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  public class TableInputMetaHandler extends TableInputMeta {
    public Database database = mock( Database.class );

    @Override
    protected Database getDatabase() {
      return database;
    }
  }

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
      Arrays.asList( "databaseMeta", "sQL", "rowLimit", "executeEachInputRow", "variableReplacementActive",
        "lazyConversionActive", "cachedRowMetaActive" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
      new LoadSaveTester( testMetaClass, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testGetFields() throws Exception {
    TableInputMetaHandler meta = new TableInputMetaHandler();
    meta.setLazyConversionActive( true );
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    meta.setDatabaseMeta( dbMeta );
    Database mockDB = meta.getDatabase();
    when( mockDB.getQueryFields( anyString(), anyBoolean() ) ).thenReturn( createMockFields() );

    RowMetaInterface expectedRowMeta = new RowMeta();
    ValueMetaInterface valueMeta = new ValueMetaString( "field1" );
    valueMeta.setStorageMetadata( new ValueMetaString( "field1" ) );
    valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    expectedRowMeta.addValueMeta( valueMeta );

    VariableSpace space = mock( VariableSpace.class );
    RowMetaInterface rowMetaInterface = new RowMeta();
    meta.getFields( rowMetaInterface, "TABLE_INPUT_META", null, null, space, null, null );

    verify( mockDB).getQueryFields( any(), anyBoolean() ) ;
  }

  private RowMetaInterface createMockFields() {
    RowMetaInterface rowMetaInterface = new RowMeta();
    ValueMetaInterface valueMeta = new ValueMetaString( "field1" );
    rowMetaInterface.addValueMeta( valueMeta );
    return rowMetaInterface;
  }
}
