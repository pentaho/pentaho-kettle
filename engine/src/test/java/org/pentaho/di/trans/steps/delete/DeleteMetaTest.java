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

package org.pentaho.di.trans.steps.delete;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
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
import org.pentaho.metastore.api.IMetaStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.test.util.InternalState.getInternalState;


public class DeleteMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<DeleteMeta> testMetaClass = DeleteMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    PluginRegistry.init( false );
    List<String> attributes =
            Arrays.asList( "schemaName", "tableName", "commitSize", "databaseMeta", "keyFields" );

    Map<String, String> getterMap = new HashMap<>();
    Map<String, String> setterMap = new HashMap<>();
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<>( new StringLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<>();
    attrValidatorMap.put( "databaseMeta", new DatabaseMetaLoadSaveValidator() );
    attrValidatorMap.put ("keyFields", new ArrayLoadSaveValidator<>( new DeleteFieldLoadSaveValidator(), 5 ));
    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<>();

    loadSaveTester =
            new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
                    getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof DeleteMeta ) {
      ( (DeleteMeta) someMeta ).allocate( 1 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }


  private StepMeta stepMeta;
  private Delete del;
  private DeleteData dd;
  private DeleteMeta dmi;


  @BeforeClass
  public static void initEnvironment() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "delete1" );

    Map<String, String> vars = new HashMap<>();
    vars.put( "max.sz", "10" );
    transMeta.injectVariables( vars );

    dmi = new DeleteMeta();
    dd = new DeleteData();

    PluginRegistry plugReg = PluginRegistry.getInstance();
    String deletePid = plugReg.getPluginId( StepPluginType.class, dmi );

    stepMeta = new StepMeta( deletePid, "delete", dmi );
    Trans trans = new Trans( transMeta );
    transMeta.addStep( stepMeta );
    del = new Delete( stepMeta, dd, 1, transMeta, trans );
    del.copyVariablesFrom( transMeta );
  }

  @Test
  public void testCommitCountFixed() {
    dmi.setCommitSize( "100" );
    assertEquals( 100, dmi.getCommitSize( del ) );
  }

  @Test
  public void testCommitCountVar() {
    dmi.setCommitSize( "${max.sz}" );
    assertEquals( 10, dmi.getCommitSize( del ) );
  }

  @Test
  public void testCommitCountMissedVar() {
    dmi.setCommitSize( "missed-var" );
    try {
      dmi.getCommitSize( del );
      fail();
    } catch ( Exception ignored ) {
    }
  }

  @Test
  public void testReadRepToLoadKeys() throws KettleException {
    DeleteMeta deleteMeta = new DeleteMeta();
    Repository rep = mock( Repository.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    ObjectId idStep = mock( ObjectId.class );
    List<DatabaseMeta> databases = new ArrayList<>();

    String keyNameValue = UUID.randomUUID().toString();
    String keyFieldValue = UUID.randomUUID().toString();
    String keyConditionValue = UUID.randomUUID().toString();
    String keyName2Value = UUID.randomUUID().toString();

    when( rep.countNrStepAttributes( idStep, "key_field" ) ).thenReturn( 1 );
    when( rep.getStepAttributeString( idStep, 0, "key_name" ) ).thenReturn( keyNameValue );
    when( rep.getStepAttributeString( idStep, 0, "key_field" ) ).thenReturn( keyFieldValue );
    when( rep.getStepAttributeString( idStep, 0, "key_condition" ) ).thenReturn( keyConditionValue );
    when( rep.getStepAttributeString( idStep, 0, "key_name2" ) ).thenReturn( keyName2Value );

    deleteMeta.readRep( rep, metaStore, idStep, databases );

    assertEquals( 1, ( (DeleteMeta.KeyFields[])
            getInternalState( deleteMeta, "keyFields" ) ).length );
    assertEquals( keyNameValue, ( (DeleteMeta.KeyFields[])
            getInternalState( deleteMeta, "keyFields" ) )[0].getKeyStream() );
    assertEquals( keyFieldValue, ( (DeleteMeta.KeyFields[])
            getInternalState( deleteMeta, "keyFields" ) )[0].getKeyLookup() );
    assertEquals( keyConditionValue, ( (DeleteMeta.KeyFields[])
            getInternalState( deleteMeta, "keyFields" ) )[0].getKeyCondition() );
    assertEquals( keyName2Value, ( (DeleteMeta.KeyFields[])
            getInternalState( deleteMeta, "keyFields" ) )[0].getKeyStream2() );
  }

  public class DeleteFieldLoadSaveValidator implements FieldLoadSaveValidator<DeleteMeta.KeyFields>{

    @Override public DeleteMeta.KeyFields getTestObject() {
      DeleteMeta.KeyFields rtn = new DeleteMeta.KeyFields();
      rtn.setKeyStream( UUID.randomUUID().toString() );
      rtn.setKeyStream2( UUID.randomUUID().toString() );
      rtn.setKeyCondition( UUID.randomUUID().toString() );
      rtn.setKeyLookup( UUID.randomUUID().toString() );
      return rtn;
    }

    @Override public boolean validateTestObject( DeleteMeta.KeyFields testObject, Object actual ) {
      if( !( actual instanceof DeleteMeta.KeyFields)) {
        return false;
      }
      DeleteMeta.KeyFields another = (DeleteMeta.KeyFields) actual;
      return new EqualsBuilder()
              .append( testObject.getKeyCondition(), another.getKeyCondition() )
              .append( testObject.getKeyLookup(), another.getKeyLookup() )
              .append( testObject.getKeyStream(), another.getKeyStream() )
              .append( testObject.getKeyStream2(), another.getKeyStream2() )
              .isEquals();
    }
  }
}
