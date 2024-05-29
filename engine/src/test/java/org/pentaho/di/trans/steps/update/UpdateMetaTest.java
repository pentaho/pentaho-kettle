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

package org.pentaho.di.trans.steps.update;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.DatabaseMetaLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.metastore.api.IMetaStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class UpdateMetaTest implements InitializerInterface<StepMetaInterface> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private Update upd;
  private UpdateMeta umi;
  LoadSaveTester loadSaveTester;
  Class<UpdateMeta> testMetaClass = UpdateMeta.class;
  private StepMockHelper<UpdateMeta, UpdateData> mockHelper;

  public static final String databaseXML =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<connection>" + "<name>lookup</name>" + "<server>127.0.0.1</server>" + "<type>H2</type>"
        + "<access>Native</access>" + "<database>mem:db</database>" + "<port></port>" + "<username>sa</username>"
        + "<password></password>" + "</connection>";


  @Before
  public void setUp() throws KettleException {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "delete1" );

    Map<String, String> vars = new HashMap<>();
    vars.put( "max.sz", "10" );
    transMeta.injectVariables( vars );

    umi = new UpdateMeta();
    UpdateData ud = new UpdateData();

    PluginRegistry plugReg = PluginRegistry.getInstance();
    String deletePid = plugReg.getPluginId( StepPluginType.class, umi );

    StepMeta stepMeta = new StepMeta( deletePid, "delete", umi );
    Trans trans = new Trans( transMeta );
    transMeta.addStep( stepMeta );
    mockHelper = new StepMockHelper<>( "Update", UpdateMeta.class, UpdateData.class );
    Mockito.when( mockHelper.logChannelInterfaceFactory.create( Mockito.any(), Mockito.any( LoggingObjectInterface.class ) ) ).thenReturn( mockHelper.logChannelInterface );

    upd = new Update( stepMeta, ud, 1, transMeta, trans );
    upd.copyVariablesFrom( transMeta );

    List<String> attributes =
        Arrays.asList( "schemaName", "tableName", "commitSize", "errorIgnored", "ignoreFlagField",
            "skipLookup", "useBatchUpdate", "keyStream", "keyLookup", "keyCondition", "keyStream2",
            "updateLookup", "updateStream", "databaseMeta" );

    Map<String, String> getterMap = new HashMap<>() {
      {
        put( "schemaName", "getSchemaName" );
        put( "tableName", "getTableName" );
        put( "commitSize", "getCommitSizeVar" );
        put( "errorIgnored", "isErrorIgnored" );
        put( "ignoreFlagField", "getIgnoreFlagField" );
        put( "skipLookup", "isSkipLookup" );
        put( "useBatchUpdate", "useBatchUpdate" );
        put( "keyStream", "getKeyStream" );
        put( "keyLookup", "getKeyLookup" );
        put( "keyCondition", "getKeyCondition" );
        put( "keyStream2", "getKeyStream2" );
        put( "updateLookup", "getUpdateLookup" );
        put( "updateStream", "getUpdateStream" );
        put( "databaseMeta", "getDatabaseMeta" );
      }
    };
    Map<String, String> setterMap = new HashMap<>() {
      {
        put( "schemaName", "setSchemaName" );
        put( "tableName", "setTableName" );
        put( "commitSize", "setCommitSize" );
        put( "errorIgnored", "setErrorIgnored" );
        put( "ignoreFlagField", "setIgnoreFlagField" );
        put( "skipLookup", "setSkipLookup" );
        put( "useBatchUpdate", "setUseBatchUpdate" );
        put( "keyStream", "setKeyStream" );
        put( "keyLookup", "setKeyLookup" );
        put( "keyCondition", "setKeyCondition" );
        put( "keyStream2", "setKeyStream2" );
        put( "updateLookup", "setUpdateLookup" );
        put( "updateStream", "setUpdateStream" );
        put( "databaseMeta", "setDatabaseMeta" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<>( new StringLoadSaveValidator(), 5 );


    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<>();
    attrValidatorMap.put( "keyStream", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "keyLookup", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "keyCondition", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "keyStream2", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "updateLookup", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "updateStream", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "databaseMeta", new DatabaseMetaLoadSaveValidator() );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  @After
  public void cleanUp() {
    mockHelper.cleanUp();
  }

  @Test
  public void testCommitCountFixed() {
    umi.setCommitSize( "100" );
    assertEquals( 100, umi.getCommitSize( upd ) );
  }

  @Test
  public void testCommitCountVar() {
    umi.setCommitSize( "${max.sz}" );
    assertEquals( 10, umi.getCommitSize( upd ) );
  }

  @Test
  public void testCommitCountMissedVar() {
    umi.setCommitSize( "missed-var" );
    try {
      umi.getCommitSize( upd );
      fail();
    } catch ( Exception ignored ) {
    }
  }

  @Test
  public void testUseDefaultSchemaName() throws Exception {
    String schemaName = "";
    String tableName = "tableName";
    String schemaTable = "default.tableName";

    DatabaseMeta databaseMeta = spy( new DatabaseMeta( databaseXML ) );
    doReturn( "someValue" ).when( databaseMeta )
      .getFieldDefinition( any( ValueMetaInterface.class ), anyString(), anyString(), anyBoolean() );
    doReturn( schemaTable ).when( databaseMeta ).getQuotedSchemaTableCombination( schemaName, tableName );

    ValueMetaInterface valueMeta = mock( ValueMetaInterface.class );
    when( valueMeta.clone() ).thenReturn( mock( ValueMetaInterface.class ) );
    RowMetaInterface rowMetaInterface = mock( RowMetaInterface.class );
    when( rowMetaInterface.size() ).thenReturn( 1 );
    when( rowMetaInterface.searchValueMeta( anyString() ) ).thenReturn( valueMeta );

    UpdateMeta updateMeta = new UpdateMeta();
    updateMeta.setDatabaseMeta( databaseMeta );
    updateMeta.setTableName( tableName );
    updateMeta.setSchemaName( schemaName );
    updateMeta.setKeyLookup( new String[] { "KeyLookup1", "KeyLookup2" } );
    updateMeta.setKeyStream( new String[] { "KeyStream1", "KeyStream2" } );
    updateMeta.setUpdateLookup( new String[] { "updateLookup1", "updateLookup2" } );
    updateMeta.setUpdateStream( new String[] { "UpdateStream1", "UpdateStream2" } );

    SQLStatement sqlStatement =
        updateMeta.getSQLStatements( new TransMeta(), mock( StepMeta.class ), rowMetaInterface,
            mock( Repository.class ), mock( IMetaStore.class ) );
    String sql = sqlStatement.getSQL();

    assertEquals( 2, StringUtils.countMatches( sql, schemaTable ) );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof UpdateMeta ) {
      ( (UpdateMeta) someMeta ).allocate( 5, 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testPDI16559() throws Exception {
    UpdateMeta update = new UpdateMeta();
    update.setKeyStream( new String[] { "field1", "field2", "field3", "field4", "field5" } );
    update.setKeyLookup( new String[] { "lkup1", "lkup2" } );
    update.setKeyCondition( new String[] { "cond1", "cond2", "cond3" } );
    update.setKeyStream2( new String[] { "str21", "str22", "str23", "str24" } );

    update.setUpdateLookup( new String[] { "updlkup1", "updlkup2", "updlkup3", "updlkup4" } );
    update.setUpdateStream( new String[] { "updlkup1", "updlkup2" } );

    try {
      String badXml = update.getXML();
      fail( "Before calling afterInjectionSynchronization, should have thrown an ArrayIndexOOB" );
    } catch ( Exception expected ) {
      // Do Nothing
    }
    update.afterInjectionSynchronization();
    //run without a exception
    String ktrXml = update.getXML();

    int targetSz = update.getKeyStream().length;

    assertEquals( targetSz, update.getKeyLookup().length );
    assertEquals( targetSz, update.getKeyCondition().length );
    assertEquals( targetSz, update.getKeyStream2().length );

    targetSz = update.getUpdateLookup().length;
    assertEquals( targetSz, update.getUpdateStream().length );

  }

  @Test
  public void testReadRepAllocatesSizeProperly() throws Exception {
    Repository rep = mock( Repository.class );
    ObjectId objectId = () -> "testId";
    when( rep.countNrStepAttributes( objectId, "key_name" ) ).thenReturn( 2 );
    when( rep.countNrStepAttributes( objectId, "key_field" ) ).thenReturn( 2 );
    when( rep.countNrStepAttributes( objectId, "key_condition" ) ).thenReturn( 0 );
    when( rep.countNrStepAttributes( objectId, "key_name2" ) ).thenReturn( 0 );

    when( rep.countNrStepAttributes( objectId, "value_name" ) ).thenReturn( 3 );
    when( rep.countNrStepAttributes( objectId, "value_rename" ) ).thenReturn( 2 );

    UpdateMeta updateMeta = spy( UpdateMeta.class );

    updateMeta.readRep( rep, null, objectId, null );

    verify( rep ).countNrStepAttributes( objectId, "key_name" );
    verify( rep ).countNrStepAttributes( objectId, "key_field" );
    verify( rep ).countNrStepAttributes( objectId, "key_condition" );
    verify( rep ).countNrStepAttributes( objectId, "key_name2" );

    verify( rep ).countNrStepAttributes( objectId, "value_name" );
    verify( rep ).countNrStepAttributes( objectId, "value_rename" );

    verify( updateMeta ).allocate( 2, 3 );
  }
}
