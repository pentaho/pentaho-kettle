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

package org.pentaho.di.trans.steps.databaselookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.DatabaseMetaLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.NonZeroIntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;


public class DatabaseLookupMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<DatabaseLookupMeta> testMetaClass = DatabaseLookupMeta.class;

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( true );
    List<String> attributes =
        Arrays.asList( "schemaName", "tablename", "databaseMeta", "orderByClause", "cached",
            "cacheSize", "loadingAllDataInCache", "failingOnMultipleResults", "eatingRowOnLookupFailure",
            "streamKeyField1", "streamKeyField2", "keyCondition", "tableKeyField", "returnValueField",
            "returnValueNewName", "returnValueDefault", "returnValueDefaultType" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "streamKeyField1", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "streamKeyField2", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "keyCondition", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "tableKeyField", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "returnValueField", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "returnValueNewName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "returnValueDefault", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "returnValueDefaultType",
         new PrimitiveIntArrayLoadSaveValidator( new NonZeroIntLoadSaveValidator( 7 ), 5 ) );

    attrValidatorMap.put( "databaseMeta", new DatabaseMetaLoadSaveValidator() );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof DatabaseLookupMeta ) {
      ( (DatabaseLookupMeta) someMeta ).allocate( 5, 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  private DatabaseLookupMeta databaseLookupMeta = new DatabaseLookupMeta();

  @Test
  public void getFieldWithValueUsedTwice() throws KettleStepException {

    databaseLookupMeta.setReturnValueField( new String[] { "match", "match", "mismatch" } );
    databaseLookupMeta.setReturnValueNewName( new String[] { "v1", "v2", "v3" } );

    ValueMetaInterface v1 = new ValueMetaString( "match" );
    ValueMetaInterface v2 = new ValueMetaString( "match1" );
    RowMetaInterface[] info = new RowMetaInterface[1];
    info[0] = new RowMeta();
    info[0].setValueMetaList( Arrays.asList( v1, v2 ) );

    ValueMetaInterface r1 = new ValueMetaString( "value" );
    RowMetaInterface row = new RowMeta();
    row.setValueMetaList( new ArrayList<ValueMetaInterface>( Arrays.asList( r1 ) ) );

    databaseLookupMeta.getFields( row, "", info, null, null, null, null );

    List<ValueMetaInterface> expectedRow = Arrays.asList( new ValueMetaInterface[] { new ValueMetaString( "value" ),
      new ValueMetaString( "v1" ), new ValueMetaString( "v2" ), } );
    assertEquals( 3, row.getValueMetaList().size() );
    for ( int i = 0; i < 3; i++ ) {
      assertEquals( expectedRow.get( i ).getName(), row.getValueMetaList().get( i ).getName() );
    }
  }

  @Test
  public void testProvidesModelerMeta() throws Exception {

    DatabaseLookupMeta databaseLookupMeta = new DatabaseLookupMeta();
    databaseLookupMeta.setReturnValueField( new String[] { "f1", "f2", "f3" } );
    databaseLookupMeta.setReturnValueNewName( new String[] { "s4", "s5", "s6" } );

    DatabaseLookupData databaseLookupData = new DatabaseLookupData();
    databaseLookupData.returnMeta = Mockito.mock( RowMeta.class );
    assertEquals( databaseLookupData.returnMeta, databaseLookupMeta.getRowMeta( databaseLookupData ) );
    assertEquals( 3, databaseLookupMeta.getDatabaseFields().size() );
    assertEquals( "f1", databaseLookupMeta.getDatabaseFields().get( 0 ) );
    assertEquals( "f2", databaseLookupMeta.getDatabaseFields().get( 1 ) );
    assertEquals( "f3", databaseLookupMeta.getDatabaseFields().get( 2 ) );
    assertEquals( 3, databaseLookupMeta.getStreamFields().size() );
    assertEquals( "s4", databaseLookupMeta.getStreamFields().get( 0 ) );
    assertEquals( "s5", databaseLookupMeta.getStreamFields().get( 1 ) );
    assertEquals( "s6", databaseLookupMeta.getStreamFields().get( 2 ) );
  }

  @Test
  public void cloneTest() throws Exception {
    DatabaseLookupMeta meta = new DatabaseLookupMeta();
    meta.allocate( 2, 2 );
    meta.setStreamKeyField1( new String[] { "aa", "bb" } );
    meta.setTableKeyField( new String[] { "cc", "dd" } );
    meta.setKeyCondition( new String[] { "ee", "ff" } );
    meta.setStreamKeyField2( new String[] { "gg", "hh" } );
    meta.setReturnValueField( new String[] { "ii", "jj" } );
    meta.setReturnValueNewName( new String[] { "kk", "ll" } );
    meta.setReturnValueDefault( new String[] { "mm", "nn" } );
    meta.setReturnValueDefaultType( new int[] { 10, 50 } );
    meta.setOrderByClause( "FOO DESC" );
    DatabaseLookupMeta aClone = (DatabaseLookupMeta) meta.clone();
    assertFalse( aClone == meta );
    assertTrue( Arrays.equals( meta.getStreamKeyField1(), aClone.getStreamKeyField1() ) );
    assertTrue( Arrays.equals( meta.getTableKeyField(), aClone.getTableKeyField() ) );
    assertTrue( Arrays.equals( meta.getKeyCondition(), aClone.getKeyCondition() ) );
    assertTrue( Arrays.equals( meta.getStreamKeyField2(), aClone.getStreamKeyField2() ) );
    assertTrue( Arrays.equals( meta.getReturnValueField(), aClone.getReturnValueField() ) );
    assertTrue( Arrays.equals( meta.getReturnValueNewName(), aClone.getReturnValueNewName() ) );
    assertTrue( Arrays.equals( meta.getReturnValueDefault(), aClone.getReturnValueDefault() ) );
    assertTrue( Arrays.equals( meta.getReturnValueDefaultType(), aClone.getReturnValueDefaultType() ) );
    assertEquals( meta.getOrderByClause(), aClone.getOrderByClause() );
    assertEquals( meta.getXML(), aClone.getXML() );
  }
}
