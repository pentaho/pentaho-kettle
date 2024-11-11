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


package org.pentaho.di.trans.steps.insertupdate;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.BooleanLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.DatabaseMetaLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveBooleanArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class InsertUpdateMetaTest {
  LoadSaveTester loadSaveTester;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private StepMeta stepMeta;
  private InsertUpdate upd;
  private InsertUpdateData ud;
  private InsertUpdateMeta umi;
  private StepMockHelper<InsertUpdateMeta, InsertUpdateData> mockHelper;

  @BeforeClass
  public static void initEnvironment() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "delete1" );

    Map<String, String> vars = new HashMap<String, String>();
    vars.put( "max.sz", "10" );
    transMeta.injectVariables( vars );

    umi = new InsertUpdateMeta();
    ud = new InsertUpdateData();

    PluginRegistry plugReg = PluginRegistry.getInstance();
    String deletePid = plugReg.getPluginId( StepPluginType.class, umi );

    stepMeta = new StepMeta( deletePid, "delete", umi );
    Trans trans = new Trans( transMeta );
    transMeta.addStep( stepMeta );
    upd = new InsertUpdate( stepMeta, ud, 1, transMeta, trans );
    upd.copyVariablesFrom( transMeta );

    mockHelper =
      new StepMockHelper<>( "insertUpdate", InsertUpdateMeta.class, InsertUpdateData.class );
    Mockito.when(
        mockHelper.logChannelInterfaceFactory.create( Mockito.any(), Mockito.any( LoggingObjectInterface.class ) ) )
      .thenReturn( mockHelper.logChannelInterface );
    Mockito.when( mockHelper.stepMeta.getStepMetaInterface() ).thenReturn( new InsertUpdateMeta() );
  }

  @After
  public void cleanUp() {
    mockHelper.cleanUp();
  }

  @Test
  public void testCommitCountFixed() {
    umi.setCommitSize( "100" );
    Assert.assertTrue( umi.getCommitSize( upd ) == 100 );
  }

  @Test
  public void testCommitCountVar() {
    umi.setCommitSize( "${max.sz}" );
    Assert.assertTrue( umi.getCommitSize( upd ) == 10 );
  }

  @Test
  public void testProvidesModeler() throws Exception {
    InsertUpdateMeta insertUpdateMeta = new InsertUpdateMeta();
    InsertUpdateMeta.UpdateField[] updateFields = new InsertUpdateMeta.UpdateField[ 3 ];
    updateFields[ 0 ] = new InsertUpdateMeta.UpdateField();
    updateFields[ 1 ] = new InsertUpdateMeta.UpdateField();
    updateFields[ 2 ] = new InsertUpdateMeta.UpdateField();

    updateFields[ 0 ].setUpdateLookup( "f1" );
    updateFields[ 1 ].setUpdateLookup( "f2" );
    updateFields[ 2 ].setUpdateLookup( "f3" );

    updateFields[ 0 ].setUpdateStream( "s4" );
    updateFields[ 1 ].setUpdateStream( "s5" );
    updateFields[ 2 ].setUpdateStream( "s6" );

    insertUpdateMeta.setUpdateFields( updateFields );

    InsertUpdateData tableOutputData = new InsertUpdateData();
    tableOutputData.insertRowMeta = Mockito.mock( RowMeta.class );
    Assert.assertEquals( tableOutputData.insertRowMeta, insertUpdateMeta.getRowMeta( tableOutputData ) );
    Assert.assertEquals( 3, insertUpdateMeta.getDatabaseFields().size() );
    Assert.assertEquals( "f1", insertUpdateMeta.getDatabaseFields().get( 0 ) );
    Assert.assertEquals( "f2", insertUpdateMeta.getDatabaseFields().get( 1 ) );
    Assert.assertEquals( "f3", insertUpdateMeta.getDatabaseFields().get( 2 ) );
    Assert.assertEquals( 3, insertUpdateMeta.getStreamFields().size() );
    Assert.assertEquals( "s4", insertUpdateMeta.getStreamFields().get( 0 ) );
    Assert.assertEquals( "s5", insertUpdateMeta.getStreamFields().get( 1 ) );
    Assert.assertEquals( "s6", insertUpdateMeta.getStreamFields().get( 2 ) );
  }

  @Test
  public void testCommitCountMissedVar() {
    umi.setCommitSize( "missed-var" );
    try {
      umi.getCommitSize( upd );
      Assert.fail();
    } catch ( Exception ex ) {
    }
  }

  @Before
  public void setUpLoadSave() throws Exception {
    List<String> attributes =
      Arrays.asList( "schemaName", "tableName", "databaseMeta", "keyFields", "updateFields", "commitSize",
        "updateBypassed" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "schemaName", "getSchemaName" );
        put( "tableName", "getTableName" );
        put( "databaseMeta", "getDatabaseMeta" );
        put( "keyStream", "getKeyStream" );
        put( "keyLookup", "getKeyLookup" );
        put( "keyCondition", "getKeyCondition" );
        put( "keyStream2", "getKeyStream2" );
        put( "updateLookup", "getUpdateLookup" );
        put( "updateStream", "getUpdateStream" );
        put( "update", "getUpdate" );
        put( "commitSize", "getCommitSizeVar" );
        put( "updateBypassed", "isUpdateBypassed" );
      }
    };

    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "schemaName", "setSchemaName" );
        put( "tableName", "setTableName" );
        put( "databaseMeta", "setDatabaseMeta" );
        put( "keyStream", "setKeyStream" );
        put( "keyLookup", "setKeyLookup" );
        put( "keyCondition", "setKeyCondition" );
        put( "keyStream2", "setKeyStream2" );
        put( "updateLookup", "setUpdateLookup" );
        put( "updateStream", "setUpdateStream" );
        put( "update", "setUpdate" );
        put( "commitSize", "setCommitSize" );
        put( "updateBypassed", "setUpdateBypassed" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "keyFields",
      new ArrayLoadSaveValidator<InsertUpdateMeta.KeyField>( new InsertFieldLoadSaveValidator(), 4 ) );
    attrValidatorMap.put( "updateFields",
      new ArrayLoadSaveValidator<InsertUpdateMeta.UpdateField>( new UpdateFieldLoadSaveValidator(), 3 ) );
    attrValidatorMap.put( "databaseMeta", new DatabaseMetaLoadSaveValidator() );
    attrValidatorMap.put( "update", new ArrayLoadSaveValidator<Boolean>( new BooleanLoadSaveValidator(), 5 ) );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    typeValidatorMap.put( boolean[].class.getCanonicalName(),
      new PrimitiveBooleanArrayLoadSaveValidator( new BooleanLoadSaveValidator(), 3 ) );

    loadSaveTester = new LoadSaveTester( InsertUpdateMeta.class, attributes, getterMap, setterMap, attrValidatorMap,
      typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testErrorProcessRow() throws KettleException {
    Mockito.when(
        mockHelper.logChannelInterfaceFactory.create( Mockito.any(), Mockito.any( LoggingObjectInterface.class ) ) )
      .thenReturn(
        mockHelper.logChannelInterface );
    Mockito.when( mockHelper.stepMeta.getStepMetaInterface() ).thenReturn( new InsertUpdateMeta() );

    InsertUpdate insertUpdateStep =
      new InsertUpdate( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );
    insertUpdateStep = Mockito.spy( insertUpdateStep );

    Mockito.doReturn( new Object[] {} ).when( insertUpdateStep ).getRow();
    insertUpdateStep.first = false;
    mockHelper.processRowsStepDataInterface.lookupParameterRowMeta = Mockito.mock( RowMetaInterface.class );
    mockHelper.processRowsStepDataInterface.keynrs = new int[] {};
    mockHelper.processRowsStepDataInterface.db = Mockito.mock( Database.class );
    mockHelper.processRowsStepDataInterface.valuenrs = new int[] {};
    Mockito.doThrow( new KettleStepException( "Test exception" ) ).when( insertUpdateStep )
      .putRow( Mockito.any(), Mockito.any() );

    boolean result =
      insertUpdateStep.processRow( mockHelper.processRowsStepMetaInterface, mockHelper.processRowsStepDataInterface );
    Assert.assertFalse( result );
  }

  //PDI-16349
  @Test
  public void keyStream2ProcessRow() throws KettleException {
    InsertUpdate insertUpdateStep =
      new InsertUpdate( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );
    insertUpdateStep.setInputRowMeta( Mockito.mock( RowMetaInterface.class ) );
    insertUpdateStep = Mockito.spy( insertUpdateStep );

    InsertUpdateMeta.KeyField[] keyFields = new InsertUpdateMeta.KeyField[ 1 ];
    keyFields[ 0 ] = new InsertUpdateMeta.KeyField();
    InsertUpdateMeta insertUpdateMeta = new InsertUpdateMeta();
    keyFields[ 0 ].setKeyStream( "test_field" );
    keyFields[ 0 ].setKeyCondition( "test_condition" );
    keyFields[ 0 ].setKeyStream2( "" );
    keyFields[ 0 ].setKeyLookup( "" );
    insertUpdateMeta.setKeyFields( keyFields );
    insertUpdateMeta.setUpdateBypassed( true );
    insertUpdateMeta.setDatabaseMeta( Mockito.mock( DatabaseMeta.class ) );
    Database database = Mockito.mock( Database.class );
    mockHelper.processRowsStepDataInterface.db = database;
    Mockito.doReturn( Mockito.mock( Connection.class ) ).when( database ).getConnection();
    Mockito.doNothing().when( insertUpdateStep ).lookupValues( Mockito.any(), Mockito.any() );
    Mockito.doNothing().when( insertUpdateStep ).putRow( Mockito.any(), Mockito.any() );
    Mockito.doReturn( new Object[] {} ).when( insertUpdateStep ).getRow();
    insertUpdateStep.first = true;

    insertUpdateMeta.afterInjectionSynchronization();
    //run without a exception
    insertUpdateStep.processRow( insertUpdateMeta, mockHelper.processRowsStepDataInterface );

    /**
     * Original test for PDI-16349 solution tested the length of the String[] from keyStream and keyStream2.
     * Since we converted these properties to an array of KeyField objects, the equivalent test would be to check the
     * value of keyStream2 is not null.
     */
    Assert.assertNotNull( insertUpdateMeta.getKeyFields()[ 0 ].getKeyStream() );
    Assert.assertNotNull( insertUpdateMeta.getKeyFields()[ 0 ].getKeyStream2() );
  }

  public class InsertFieldLoadSaveValidator implements FieldLoadSaveValidator<InsertUpdateMeta.KeyField> {

    @Override public InsertUpdateMeta.KeyField getTestObject() {
      InsertUpdateMeta.KeyField rtn = new InsertUpdateMeta.KeyField();
      rtn.setKeyStream( UUID.randomUUID().toString() );
      rtn.setKeyStream2( UUID.randomUUID().toString() );
      rtn.setKeyCondition( UUID.randomUUID().toString() );
      rtn.setKeyLookup( UUID.randomUUID().toString() );
      return rtn;
    }

    @Override public boolean validateTestObject( InsertUpdateMeta.KeyField testObject, Object actual ) {
      if ( !( actual instanceof InsertUpdateMeta.KeyField ) ) {
        return false;
      }
      InsertUpdateMeta.KeyField another = (InsertUpdateMeta.KeyField) actual;
      return new EqualsBuilder()
        .append( testObject.getKeyCondition(), another.getKeyCondition() )
        .append( testObject.getKeyLookup(), another.getKeyLookup() )
        .append( testObject.getKeyStream(), another.getKeyStream() )
        .append( testObject.getKeyStream2(), another.getKeyStream2() )
        .isEquals();
    }
  }

  public class UpdateFieldLoadSaveValidator implements FieldLoadSaveValidator<InsertUpdateMeta.UpdateField> {

    @Override public InsertUpdateMeta.UpdateField getTestObject() {
      InsertUpdateMeta.UpdateField rtn = new InsertUpdateMeta.UpdateField();
      rtn.setUpdate( true );
      rtn.setUpdateLookup( UUID.randomUUID().toString() );
      rtn.setUpdateStream( UUID.randomUUID().toString() );
      return rtn;
    }

    @Override public boolean validateTestObject( InsertUpdateMeta.UpdateField testObject, Object actual ) {
      if ( !( actual instanceof InsertUpdateMeta.UpdateField ) ) {
        return false;
      }
      InsertUpdateMeta.UpdateField another = (InsertUpdateMeta.UpdateField) actual;
      return new EqualsBuilder()
        .append( testObject.getUpdate(), another.getUpdate() )
        .append( testObject.getUpdateLookup(), another.getUpdateLookup() )
        .append( testObject.getUpdateStream(), another.getUpdateStream() )
        .isEquals();
    }
  }
}
