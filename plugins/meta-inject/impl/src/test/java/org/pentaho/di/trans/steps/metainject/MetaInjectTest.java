/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.metainject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.metastore.api.IMetaStore;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.anyString;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( { MetaInject.class } )
public class MetaInjectTest {

  private static final String INJECTOR_STEP_NAME = "TEST_STEP_FOR_INJECTION";

  private static final String TEST_VALUE = "TEST_VALUE";

  private static final String TEST_VARIABLE = "TEST_VARIABLE";

  private static final String TEST_PARAMETER = "TEST_PARAMETER";

  private static final String TEST_TARGET_STEP_NAME = "TEST_TARGET_STEP_NAME";

  private static final String TEST_SOURCE_STEP_NAME = "TEST_SOURCE_STEP_NAME";

  private static final String TEST_ATTR_VALUE = "TEST_ATTR_VALUE";

  private static final String TEST_FIELD = "TEST_FIELD";

  private static final String UNAVAILABLE_STEP = "UNAVAILABLE_STEP";

  private static final TargetStepAttribute UNAVAILABLE_TARGET_STEP =
      new TargetStepAttribute( UNAVAILABLE_STEP, TEST_ATTR_VALUE, false );

  private static final SourceStepField UNAVAILABLE_SOURCE_STEP = new SourceStepField( UNAVAILABLE_STEP, TEST_FIELD );

  private MetaInject metaInject;
  private Repository repository;

  private MetaInjectMeta meta;

  private MetaInjectData data;

  private TransMeta transMeta;

  private Trans trans;

  private StepMetaInjectionInterface metaInjectionInterface;
  private IMetaStore metaStore;

  @Before
  public void before() throws Exception {
    repository = PowerMockito.mock( Repository.class );
    transMeta = PowerMockito.spy( new TransMeta() );
    meta = new MetaInjectMeta();
    data = new MetaInjectData();
    data.transMeta = transMeta;
    metaInject = StepMockUtil.getStep( MetaInject.class, MetaInjectMeta.class, "MetaInjectTest" );
    metaInject = PowerMockito.spy( metaInject );
    metaInject.init( meta, data );
    metaStore = mock( IMetaStore.class );
    metaInject.setMetaStore( metaStore );
    doReturn( transMeta ).when( metaInject ).getTransMeta();

    TransMeta internalTransMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );
    trans = new Trans();
    trans.setLog( LogChannel.GENERAL );
    trans = PowerMockito.spy( trans );
    doReturn( trans ).when( metaInject ).getTrans();
    doReturn( INJECTOR_STEP_NAME ).when( stepMeta ).getName();
    doReturn( Collections.singletonList( stepMeta ) ).when( internalTransMeta ).getUsedSteps();
    StepMetaInterface stepMetaInterface = mock( StepMetaInterface.class );
    doReturn( stepMetaInterface ).when( stepMeta ).getStepMetaInterface();
    metaInjectionInterface = mock( StepMetaInjectionInterface.class );
    doReturn( metaInjectionInterface ).when( stepMetaInterface ).getStepMetaInjectionInterface();

    doReturn( internalTransMeta ).when( metaInject ).loadTransformationMeta();
  }

  @Test
  public void injectMetaFromMultipleInputSteps() throws KettleException {
    Map<TargetStepAttribute, SourceStepField> targetSourceMapping =
        new LinkedHashMap<TargetStepAttribute, SourceStepField>();
    targetSourceMapping.put( new TargetStepAttribute( INJECTOR_STEP_NAME, "DATA_TYPE", true ), new SourceStepField(
        "TYPE_INPUT", "col_type" ) );
    targetSourceMapping.put( new TargetStepAttribute( INJECTOR_STEP_NAME, "NAME", true ), new SourceStepField(
        "NAME_INPUT", "col_name" ) );
    meta.setTargetSourceMapping( targetSourceMapping );

    doReturn( new String[] { "NAME_INPUT", "TYPE_INPUT" } ).when( transMeta ).getPrevStepNames( any( StepMeta.class ) );

    RowSet nameInputRowSet = mock( RowSet.class );
    RowMeta nameRowMeta = new RowMeta();
    nameRowMeta.addValueMeta( new ValueMetaString( "col_name" ) );
    doReturn( nameRowMeta ).when( nameInputRowSet ).getRowMeta();
    doReturn( nameInputRowSet ).when( metaInject ).findInputRowSet( "NAME_INPUT" );

    RowSet typeInputRowSet = mock( RowSet.class );
    RowMeta typeRowMeta = new RowMeta();
    typeRowMeta.addValueMeta( new ValueMetaString( "col_type" ) );
    doReturn( typeRowMeta ).when( typeInputRowSet ).getRowMeta();
    doReturn( typeInputRowSet ).when( metaInject ).findInputRowSet( "TYPE_INPUT" );

    doReturn( new Object[] { "FIRST_NAME" } ).doReturn( null ).when( metaInject ).getRowFrom( nameInputRowSet );
    doReturn( new Object[] { "String" } ).doReturn( null ).when( metaInject ).getRowFrom( typeInputRowSet );

    List<StepInjectionMetaEntry> injectionMetaEntryList = new ArrayList<StepInjectionMetaEntry>();
    StepInjectionMetaEntry fields = new StepInjectionMetaEntry( "FIELDS", ValueMetaInterface.TYPE_NONE, "" );
    StepInjectionMetaEntry fieldEntry = new StepInjectionMetaEntry( "FIELD", ValueMetaInterface.TYPE_NONE, "" );
    fields.getDetails().add( fieldEntry );
    StepInjectionMetaEntry nameEntry = new StepInjectionMetaEntry( "NAME", ValueMetaInterface.TYPE_STRING, "" );
    fieldEntry.getDetails().add( nameEntry );
    StepInjectionMetaEntry dataEntry = new StepInjectionMetaEntry( "DATA_TYPE", ValueMetaInterface.TYPE_STRING, "" );
    fieldEntry.getDetails().add( dataEntry );
    injectionMetaEntryList.add( fields );
    doReturn( injectionMetaEntryList ).when( metaInjectionInterface ).getStepInjectionMetadataEntries();

    meta.setNoExecution( true );
    assertTrue( metaInject.init( meta, data ) );

    metaInject.processRow( meta, data );

    StepInjectionMetaEntry expectedNameEntry =
        new StepInjectionMetaEntry( "NAME", "FIRST_NAME", ValueMetaInterface.TYPE_STRING, "" );
    StepInjectionMetaEntry expectedDataEntry =
        new StepInjectionMetaEntry( "DATA_TYPE", "String", ValueMetaInterface.TYPE_STRING, "" );
    verify( metaInject, atLeastOnce() ).setEntryValueIfFieldExists( refEq( expectedNameEntry ), any(
        RowMetaAndData.class ), any( SourceStepField.class ) );
    verify( metaInject, atLeastOnce() ).setEntryValueIfFieldExists( refEq( expectedDataEntry ), any(
        RowMetaAndData.class ), any( SourceStepField.class ) );
  }

  @Test
  public void testMetastoreIsSet() throws Exception {
    doReturn( new String[] { } ).when( transMeta ).getPrevStepNames( any( StepMeta.class ) );
    data.stepInjectionMetasMap = new HashMap<>();
    data.stepInjectionMap = new HashMap<>();

    data.transMeta = new TransMeta();

    meta.setNoExecution( false );
    doReturn( LogLevel.ERROR ).when( metaInject ).getLogLevel();
    // don't need to actually run anything to verify this. force it to "stopped"
    doReturn( true ).when( metaInject ).isStopped();
    doNothing().when( metaInject ).waitUntilFinished( any( Trans.class ) );
    // make sure the injected tranformation doesn't have a metastore first
    assertNull( data.transMeta.getMetaStore() );

    metaInject.processRow( meta, data );

    // now it should be set
    assertEquals( metaStore, data.transMeta.getMetaStore() );
  }

  @Test
  public void testTransWaitsForListenersToFinish() throws Exception {
    doReturn( new String[] { } ).when( transMeta ).getPrevStepNames( any( StepMeta.class ) );
    data.stepInjectionMetasMap = new HashMap<>();
    data.stepInjectionMap = new HashMap<>();
    data.transMeta = new TransMeta();
    meta.setNoExecution( false );
    Trans injectTrans = mock( Trans.class );
    doReturn( injectTrans ).when( metaInject ).createInjectTrans();
    when( injectTrans.isFinished() ).thenReturn( true );
    Result result = mock( Result.class );
    when( injectTrans.getResult() ).thenReturn( result );
    metaInject.processRow( meta, data );
    verify( injectTrans ).waitUntilFinished();
  }

  @Test
  public void transVariablesPassedToChildTransformation() throws KettleException {
    doReturn( new String[] { TEST_VARIABLE } ).when( metaInject ).listVariables();
    doReturn( TEST_VALUE ).when( metaInject ).getVariable( TEST_VARIABLE );

    TransMeta transMeta = new TransMeta();
    doReturn( transMeta ).when( metaInject ).getTransMeta();
    TransMeta internalTransMeta = new TransMeta();
    doReturn( internalTransMeta ).when( metaInject ).loadTransformationMeta();

    assertTrue( metaInject.init( meta, data ) );

    assertEquals( TEST_VALUE, internalTransMeta.getVariable( TEST_VARIABLE ) );
  }

  @Test
  public void transParametersPassedToChildTransformation() throws KettleException {
    Trans trans = new Trans();
    trans.addParameterDefinition( TEST_PARAMETER, "TEST_DEF_VALUE", "" );
    trans.setParameterValue( TEST_PARAMETER, TEST_VALUE );

    doReturn( trans ).when( metaInject ).getTrans();
    TransMeta internalTransMeta = new TransMeta();
    doReturn( internalTransMeta ).when( metaInject ).loadTransformationMeta();

    assertTrue( metaInject.init( meta, data ) );

    assertEquals( TEST_VALUE, internalTransMeta.getParameterValue( TEST_PARAMETER ) );
  }

  @Test
  public void getUnavailableSourceSteps() {
    TargetStepAttribute targetStep = new TargetStepAttribute( TEST_TARGET_STEP_NAME, TEST_ATTR_VALUE, false );
    SourceStepField unavailableSourceStep = new SourceStepField( UNAVAILABLE_STEP, TEST_FIELD );
    Map<TargetStepAttribute, SourceStepField> targetMap = Collections.singletonMap( targetStep, unavailableSourceStep );
    TransMeta sourceTransMeta = mock( TransMeta.class );
    doReturn( new String[0] ).when( sourceTransMeta ).getPrevStepNames( any( StepMeta.class ) );

    Set<SourceStepField> actualSet =
        MetaInject.getUnavailableSourceSteps( targetMap, sourceTransMeta, mock( StepMeta.class ) );
    assertTrue( actualSet.contains( unavailableSourceStep ) );
  }

  @Test
  public void getUnavailableTargetSteps() {
    TargetStepAttribute unavailableTargetStep = new TargetStepAttribute( UNAVAILABLE_STEP, TEST_ATTR_VALUE, false );
    SourceStepField sourceStep = new SourceStepField( TEST_SOURCE_STEP_NAME, TEST_FIELD );
    Map<TargetStepAttribute, SourceStepField> targetMap = Collections.singletonMap( unavailableTargetStep, sourceStep );
    TransMeta injectedTransMeta = mock( TransMeta.class );
    doReturn( Collections.emptyList() ).when( injectedTransMeta ).getUsedSteps();

    Set<TargetStepAttribute> actualSet = MetaInject.getUnavailableTargetSteps( targetMap, injectedTransMeta );
    assertTrue( actualSet.contains( unavailableTargetStep ) );
  }

  @Test
  public void removeUnavailableStepsFromMapping_unavailable_source_step() {
    TargetStepAttribute unavailableTargetStep = new TargetStepAttribute( UNAVAILABLE_STEP, TEST_ATTR_VALUE, false );
    SourceStepField unavailableSourceStep = new SourceStepField( UNAVAILABLE_STEP, TEST_FIELD );
    Map<TargetStepAttribute, SourceStepField> targetMap = new HashMap<TargetStepAttribute, SourceStepField>();
    targetMap.put( unavailableTargetStep, unavailableSourceStep );

    Set<SourceStepField> unavailableSourceSteps = Collections.singleton( UNAVAILABLE_SOURCE_STEP );
    MetaInject.removeUnavailableStepsFromMapping( targetMap, unavailableSourceSteps, Collections
        .<TargetStepAttribute>emptySet() );
    assertTrue( targetMap.isEmpty() );
  }

  @Test
  public void removeUnavailableStepsFromMapping_unavailable_target_step() {
    TargetStepAttribute unavailableTargetStep = new TargetStepAttribute( UNAVAILABLE_STEP, TEST_ATTR_VALUE, false );
    SourceStepField unavailableSourceStep = new SourceStepField( UNAVAILABLE_STEP, TEST_FIELD );
    Map<TargetStepAttribute, SourceStepField> targetMap = new HashMap<TargetStepAttribute, SourceStepField>();
    targetMap.put( unavailableTargetStep, unavailableSourceStep );

    Set<TargetStepAttribute> unavailableTargetSteps = Collections.singleton( UNAVAILABLE_TARGET_STEP );
    MetaInject.removeUnavailableStepsFromMapping( targetMap, Collections.<SourceStepField>emptySet(),
        unavailableTargetSteps );
    assertTrue( targetMap.isEmpty() );
  }

  @Test
  public void removeUnavailableStepsFromMapping_unavailable_source_target_step() {
    TargetStepAttribute unavailableTargetStep = new TargetStepAttribute( UNAVAILABLE_STEP, TEST_ATTR_VALUE, false );
    SourceStepField unavailableSourceStep = new SourceStepField( UNAVAILABLE_STEP, TEST_FIELD );
    Map<TargetStepAttribute, SourceStepField> targetMap = new HashMap<TargetStepAttribute, SourceStepField>();
    targetMap.put( unavailableTargetStep, unavailableSourceStep );

    Set<TargetStepAttribute> unavailableTargetSteps = Collections.singleton( UNAVAILABLE_TARGET_STEP );
    Set<SourceStepField> unavailableSourceSteps = Collections.singleton( UNAVAILABLE_SOURCE_STEP );
    MetaInject.removeUnavailableStepsFromMapping( targetMap, unavailableSourceSteps, unavailableTargetSteps );
    assertTrue( targetMap.isEmpty() );
  }

  @Test
  public void setEntryValue_string() throws KettleValueException {
    StepInjectionMetaEntry entry = mock( StepInjectionMetaEntry.class );
    doReturn( ValueMetaInterface.TYPE_STRING ).when( entry ).getValueType();
    RowMetaAndData row = createRowMetaAndData( new ValueMetaString( TEST_FIELD ), TEST_VALUE );
    SourceStepField sourceField = new SourceStepField( TEST_SOURCE_STEP_NAME, TEST_FIELD );

    MetaInject.setEntryValue( entry, row, sourceField );

    verify( entry ).setValue( TEST_VALUE );
  }

  @Test
  public void setEntryValue_boolean() throws KettleValueException {
    StepInjectionMetaEntry entry = mock( StepInjectionMetaEntry.class );
    doReturn( ValueMetaInterface.TYPE_BOOLEAN ).when( entry ).getValueType();
    RowMetaAndData row = createRowMetaAndData( new ValueMetaBoolean( TEST_FIELD ), true );
    SourceStepField sourceField = new SourceStepField( TEST_SOURCE_STEP_NAME, TEST_FIELD );

    MetaInject.setEntryValue( entry, row, sourceField );

    verify( entry ).setValue( true );
  }

  @Test
  public void setEntryValue_integer() throws KettleValueException {
    StepInjectionMetaEntry entry = mock( StepInjectionMetaEntry.class );
    doReturn( ValueMetaInterface.TYPE_INTEGER ).when( entry ).getValueType();
    RowMetaAndData row = createRowMetaAndData( new ValueMetaInteger( TEST_FIELD ), new Long( 1 ) );
    SourceStepField sourceField = new SourceStepField( TEST_SOURCE_STEP_NAME, TEST_FIELD );

    MetaInject.setEntryValue( entry, row, sourceField );

    verify( entry ).setValue( 1L );
  }

  @Test
  public void setEntryValue_number() throws KettleValueException {
    StepInjectionMetaEntry entry = mock( StepInjectionMetaEntry.class );
    doReturn( ValueMetaInterface.TYPE_NUMBER ).when( entry ).getValueType();
    RowMetaAndData row = createRowMetaAndData( new ValueMetaNumber( TEST_FIELD ), new Double( 1 ) );
    SourceStepField sourceField = new SourceStepField( TEST_SOURCE_STEP_NAME, TEST_FIELD );

    MetaInject.setEntryValue( entry, row, sourceField );

    verify( entry ).setValue( 1.0D );
  }

  @Test
  public void setEntryValue_date() throws KettleValueException {
    StepInjectionMetaEntry entry = mock( StepInjectionMetaEntry.class );
    doReturn( ValueMetaInterface.TYPE_DATE ).when( entry ).getValueType();
    RowMetaAndData row = createRowMetaAndData( new ValueMetaDate( TEST_FIELD ), null );
    SourceStepField sourceField = new SourceStepField( TEST_SOURCE_STEP_NAME, TEST_FIELD );

    MetaInject.setEntryValue( entry, row, sourceField );

    verify( entry ).setValue( null );
  }

  @Test
  public void setEntryValue_bignumber() throws KettleValueException {
    StepInjectionMetaEntry entry = mock( StepInjectionMetaEntry.class );
    doReturn( ValueMetaInterface.TYPE_BIGNUMBER ).when( entry ).getValueType();
    RowMetaAndData row = createRowMetaAndData( new ValueMetaBigNumber( TEST_FIELD ), new BigDecimal( 1 ) );
    SourceStepField sourceField = new SourceStepField( TEST_SOURCE_STEP_NAME, TEST_FIELD );

    MetaInject.setEntryValue( entry, row, sourceField );

    verify( entry ).setValue( new BigDecimal( 1 ) );
  }

  @Test
  public void convertToUpperCaseSet_null_array() {
    Set<String> actualResult = MetaInject.convertToUpperCaseSet( null );
    assertNotNull( actualResult );
    assertTrue( actualResult.isEmpty() );
  }

  @Test
  public void convertToUpperCaseSet() {
    String[] input = new String[] { "Test_Step", "test_step1" };
    Set<String> actualResult = MetaInject.convertToUpperCaseSet( input );
    Set<String> expectedResult = new HashSet<>();
    expectedResult.add( "TEST_STEP" );
    expectedResult.add( "TEST_STEP1" );
    assertEquals( expectedResult, actualResult );
  }

  @Test
  public void testGetUnavailableTargetKeys() throws Exception {
    final String targetStepName = "injectable step name";
    TargetStepAttribute unavailableTargetAttr = new TargetStepAttribute( targetStepName, "NOT_THERE", false );
    TargetStepAttribute availableTargetAttr = new TargetStepAttribute( targetStepName, "THERE", false );
    SourceStepField sourceStep = new SourceStepField( TEST_SOURCE_STEP_NAME, TEST_FIELD );

    Map<TargetStepAttribute, SourceStepField> targetMap = new HashMap<>( 2 );
    targetMap.put( unavailableTargetAttr, sourceStep );
    targetMap.put( availableTargetAttr, sourceStep );

    StepMetaInterface smi = new InjectableTestStepMeta();
    TransMeta transMeta = mockSingleStepTransMeta( targetStepName, smi );
    Set<TargetStepAttribute> unavailable =
        MetaInject.getUnavailableTargetKeys( targetMap, transMeta, Collections.<TargetStepAttribute>emptySet() );
    assertEquals( 1, unavailable.size() );
    assertTrue( unavailable.contains( unavailableTargetAttr ) );
  }

  @Test
  public void testStepChangeListener() throws Exception {
    MetaInjectMeta mim = new MetaInjectMeta();
    StepMeta sm = new StepMeta( "testStep", mim );
    try {
      transMeta.addOrReplaceStep( sm );
    } catch ( Exception ex ) {
      fail();
    }
  }

  private TransMeta mockSingleStepTransMeta( final String targetStepName, StepMetaInterface smi ) {
    StepMeta stepMeta = mock( StepMeta.class );
    when( stepMeta.getStepMetaInterface() ).thenReturn( smi );
    when( stepMeta.getName() ).thenReturn( targetStepName );
    TransMeta transMeta = mock( TransMeta.class );
    when( transMeta.getUsedSteps() ).thenReturn( Collections.singletonList( stepMeta ) );
    return transMeta;
  }

  @InjectionSupported( localizationPrefix = "", groups = "groups" )
  private static class InjectableTestStepMeta extends BaseStepMeta implements StepMetaInterface {

    @Injection( name = "THERE" )
    private String there;

    @Override
    public void setDefault() {
    }

    @Override
    public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
        TransMeta transMeta, Trans trans ) {
      return null;
    }

    @Override
    public StepDataInterface getStepData() {
      return null;
    }
  }

  private static RowMetaAndData createRowMetaAndData( ValueMetaInterface valueMeta, Object data ) {
    RowMetaAndData row = new RowMetaAndData();
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( valueMeta );
    row.setRowMeta( rowMeta );
    row.setData( new Object[] { data } );
    return row;
  }

  @Test
  public void testWriteInjectedKtrNoRepo() throws Exception {
    PowerMockito.doNothing().when( metaInject, "writeInjectedKtrToRepo", "/home/admin/injected_trans.ktr" );
    PowerMockito.doNothing().when( metaInject, "writeInjectedKtrToFs", "/home/admin/injected_trans.ktr" );
    metaInject.setRepository( null );
    Whitebox.<String>invokeMethod( metaInject, "writeInjectedKtr", "/home/admin/injected_trans.ktr" );
    PowerMockito.verifyPrivate( metaInject, times( 0 ) ).invoke( "writeInjectedKtrToRepo",
      "/home/admin/injected_trans.ktr" );
    PowerMockito.verifyPrivate( metaInject, times( 1 ) ).invoke( "writeInjectedKtrToFs", "/home/admin/injected_trans"
      + ".ktr" );
  }

  @Test
  public void testWriteInjectedKtrWithRepo() throws Exception {
    PowerMockito.doNothing().when( metaInject, "writeInjectedKtrToRepo", "/home/admin/injected_trans.ktr" );
    PowerMockito.doNothing().when( metaInject, "writeInjectedKtrToFs", "/home/admin/injected_trans.ktr" );
    metaInject.setRepository( repository );
    Whitebox.<String>invokeMethod( metaInject, "writeInjectedKtr", "/home/admin/injected_trans.ktr" );
    PowerMockito.verifyPrivate( metaInject, times( 1 ) ).invoke( "writeInjectedKtrToRepo",
      "/home/admin/injected_trans.ktr" );
    PowerMockito.verifyPrivate( metaInject, times( 0 ) ).invoke( "writeInjectedKtrToFs",
      "/home/admin/injected_trans.ktr" );
  }

  @Test
  public void testWriteInjectedKtrToRepoSameDir() throws Exception {
    RepositoryDirectory rootDir = PowerMockito.spy( new RepositoryDirectory( null, "/" ) );
    RepositoryDirectory adminDir = PowerMockito.spy( new RepositoryDirectory( new RepositoryDirectory(
      new RepositoryDirectory( null, "/" ), "home" ), "admin" ) );
    TransMeta cloneMeta = PowerMockito.spy( (TransMeta) data.transMeta.realClone( false ) );
    PowerMockito.doReturn( cloneMeta ).when( data.transMeta ).realClone( false );

    PowerMockito.doReturn( adminDir ).when( repository ).createRepositoryDirectory( rootDir, "home/admin" );
    PowerMockito.doReturn( adminDir ).when( data.transMeta ).getRepositoryDirectory();
    PowerMockito.whenNew( RepositoryDirectory.class ).withArguments( null, "/" ).thenReturn( rootDir );

    metaInject.setRepository( repository );
    Whitebox.<String>invokeMethod( metaInject, "writeInjectedKtrToRepo", "/home/admin/injected_trans.ktr" );
    verify( repository, times( 1 ) ).findDirectory( "home/admin" );
    verify( repository, times( 1 ) ).createRepositoryDirectory( rootDir, "home/admin" );
    verify( cloneMeta, times( 1 ) ).setRepositoryDirectory( adminDir );
    verify( cloneMeta, times( 1 ) ).setObjectId( any( ObjectId.class ) );
    verify( repository, times( 1 ) ).save( cloneMeta, null, null, true );
  }

  @Test
  public void testWriteInjectedKtrToRepoDifferentDir() throws Exception {
    RepositoryDirectory rootDir = PowerMockito.spy( new RepositoryDirectory( null, "/" ) );
    RepositoryDirectory adminDir = PowerMockito.spy( new RepositoryDirectory( new RepositoryDirectory(
      new RepositoryDirectory( null, "/" ), "home" ), "admin" ) );
    TransMeta cloneMeta = PowerMockito.spy( (TransMeta) data.transMeta.realClone( false ) );
    PowerMockito.doReturn( cloneMeta ).when( data.transMeta ).realClone( false );

    PowerMockito.doReturn( adminDir ).when( repository ).createRepositoryDirectory( rootDir,
      "/home/admin" );
    PowerMockito.doReturn( adminDir ).when( data.transMeta ).getRepositoryDirectory();
    PowerMockito.whenNew( RepositoryDirectory.class ).withArguments( null, "/" ).thenReturn( rootDir );

    metaInject.setRepository( repository );
    Whitebox.<String>invokeMethod( metaInject, "writeInjectedKtrToRepo", "injected_trans" );
    verify( repository, times( 0 ) ).findDirectory( anyString() );
    verify( repository, times( 0 ) ).createRepositoryDirectory( any(), any() );
    verify( cloneMeta, times( 1 ) ).setRepositoryDirectory( adminDir );
    verify( cloneMeta, times( 1 ) ).setObjectId( any( ObjectId.class ) );
    verify( repository, times( 1 ) ).save( cloneMeta, null, null, true );
  }

  @Test
  public void writeInjectedKtrKeepsDataTest() throws Exception {
    String filepath = "filepath";
    metaInject.writeInjectedKtr( filepath );
    //Make sure realClone( false ) is called and no other, so that the resulting ktr keeps all the info
    verify( data.transMeta, times( 1 ) ).realClone( false );
    verify( data.transMeta, times( 0 ) ).realClone( true );
    verify( data.transMeta, times( 0 ) ).clone();

    //Delete temporary file created by the test
    new File( filepath ).delete();
  }

  @Test
  public void writeInjectedKtrShouldWriteToRepoTest() throws Exception {
    String filepath = "filepath";
    metaInject.setRepository( repository );
    metaInject.writeInjectedKtr( "filepath" );

    verify( metaInject, times( 1 ) ).writeInjectedKtrToRepo( filepath );
    verify( metaInject, times( 0 ) ).writeInjectedKtrToFs( filepath );
  }

  @Test
  public void writeInjectedKtrShouldWriteToFileSystemTest() throws Exception {
    String filepath = "filepath";
    metaInject.writeInjectedKtr( "filepath" );

    verify( metaInject, times( 0 ) ).writeInjectedKtrToRepo( filepath );
    verify( metaInject, times( 1 ) ).writeInjectedKtrToFs( filepath );

    //Delete temporary file created by the test
    new File( filepath ).delete();
  }

  @Test
  public void writeInjectedKtrShouldWriteToFileSystemCompatibilityFlagTest() throws Exception {
    String filepath = "filepath";
    metaInject.setRepository( repository );
    metaInject.setVariable( Const.KETTLE_COMPATIBILITY_MDI_INJECTED_FILE_ALWAYS_IN_FILESYSTEM, "Y" );
    metaInject.writeInjectedKtr( "filepath" );

    verify( metaInject, times( 0 ) ).writeInjectedKtrToRepo( filepath );
    verify( metaInject, times( 1 ) ).writeInjectedKtrToFs( filepath );

    //Delete temporary file created by the test
    new File( filepath ).delete();
  }

  @Test
  public void getRepositoryNotNullTest() {
    metaInject.setRepository( repository );
    //If repository is set in the base step (Local Execution) TransMeta will not be required to get the repository
    metaInject.getRepository();
    verify( metaInject, times( 0 ) ).getTransMeta();
  }

  @Test
  public void getRepositoryNullTest() {
    metaInject.getRepository();
    //If repository is not set in the base step (Remote Executions/Scheduling) Need to get the repository from TransMeta
    verify( metaInject, times( 1 ) ).getTransMeta();
  }
}
