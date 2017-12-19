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

package org.pentaho.di.trans.steps.transexecutor;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TransExecutorMetaTest {

  LoadSaveTester loadSaveTester;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    PluginRegistry.addPluginType( ValueMetaPluginType.getInstance() );
    PluginRegistry.init( true );
  }

  @Before
  public void setUp() throws Exception {

    List<String> attributes =
      Arrays.asList( "fileName", "transName", "directoryPath", "groupSize", "groupField", "groupTime",
        "executionTimeField", "executionFilesRetrievedField", "executionLogTextField",
        "executionLogChannelIdField", "executionResultField", "executionNrErrorsField", "executionLinesReadField",
        "executionLinesWrittenField", "executionLinesInputField", "executionLinesOutputField",
        "executionLinesRejectedField", "executionLinesUpdatedField", "executionLinesDeletedField",
        "executionExitStatusField", "outputRowsField", "outputRowsType", "outputRowsLength",
        "outputRowsPrecision" );

    // executionResultTargetStepMeta -? (see for switch case meta)
    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();
    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 1 );

    // don't want the random value given by the default getTestObject...
    IntLoadSaveValidator intValidator = spy( new IntLoadSaveValidator() );
    doReturn( ValueMetaInterface.TYPE_INTEGER ).when( intValidator ).getTestObject();

    FieldLoadSaveValidator<int[]> intArrayLoadSaveValidator =
      new PrimitiveIntArrayLoadSaveValidator( intValidator, 1 );

    attrValidatorMap.put( "outputRowsField", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "outputRowsType", intArrayLoadSaveValidator );
    attrValidatorMap.put( "outputRowsLength", intArrayLoadSaveValidator );
    attrValidatorMap.put( "outputRowsPrecision", intArrayLoadSaveValidator );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    typeValidatorMap.put( int[].class.getCanonicalName(), new PrimitiveIntArrayLoadSaveValidator(
      new IntLoadSaveValidator(), 1 ) );
    loadSaveTester =
      new LoadSaveTester( TransExecutorMeta.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );

  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }


  @Test
  public void firstStreamIsExecutionStatistics() throws Exception {
    StreamInterface stream = mockStream();
    StepIOMetaInterface stepIo = mockStepIo( stream, 0 );

    TransExecutorMeta meta = new TransExecutorMeta();
    meta = spy( meta );

    when( meta.getStepIOMeta() ).thenReturn( stepIo );
    doCallRealMethod().when( meta ).handleStreamSelection( any( StreamInterface.class ) );

    meta.handleStreamSelection( stream );

    assertEquals( stream.getStepMeta(), meta.getExecutionResultTargetStepMeta() );
  }

  @Test
  public void secondStreamIsInternalTransformationsOutput() throws Exception {
    StreamInterface stream = mockStream();
    StepIOMetaInterface stepIo = mockStepIo( stream, 1 );

    TransExecutorMeta meta = new TransExecutorMeta();
    meta = spy( meta );

    when( meta.getStepIOMeta() ).thenReturn( stepIo );
    doCallRealMethod().when( meta ).handleStreamSelection( any( StreamInterface.class ) );

    meta.handleStreamSelection( stream );

    assertEquals( stream.getStepMeta(), meta.getOutputRowsSourceStepMeta() );
  }

  @Test
  public void thirdStreamIsExecutionResultFiles() throws Exception {
    StreamInterface stream = mockStream();
    StepIOMetaInterface stepIo = mockStepIo( stream, 2 );

    TransExecutorMeta meta = new TransExecutorMeta();
    meta = spy( meta );

    when( meta.getStepIOMeta() ).thenReturn( stepIo );
    doCallRealMethod().when( meta ).handleStreamSelection( any( StreamInterface.class ) );

    meta.handleStreamSelection( stream );

    assertEquals( stream.getStepMeta(), meta.getResultFilesTargetStepMeta() );
  }

  @Test
  public void forthStreamIsExecutorsInput() throws Exception {
    StreamInterface stream = mockStream();
    StepIOMetaInterface stepIo = mockStepIo( stream, 3 );

    TransExecutorMeta meta = new TransExecutorMeta();
    meta = spy( meta );

    when( meta.getStepIOMeta() ).thenReturn( stepIo );
    doCallRealMethod().when( meta ).handleStreamSelection( any( StreamInterface.class ) );

    meta.handleStreamSelection( stream );

    assertEquals( stream.getStepMeta(), meta.getExecutorsOutputStepMeta() );
  }

  @Test
  public void testPrepareExecutionResultsFields() throws Exception {
    TransExecutorMeta meta = new TransExecutorMeta();
    meta = spy( meta );

    RowMetaInterface row = mock( RowMetaInterface.class );
    StepMeta nextStep = mock( StepMeta.class );

    meta.setExecutionResultTargetStepMeta( nextStep );
    meta.setExecutionTimeField( "time" );

    StepMeta parent = mock( StepMeta.class );
    doReturn( parent ).when( meta ).getParentStepMeta();
    when( parent.getName() ).thenReturn( "parent step" );

    meta.prepareExecutionResultsFields( row, nextStep );

    // make sure we get the name of the parent step meta... used for the origin step
    verify( parent ).getName();
    ArgumentCaptor<ValueMetaInterface> argumentCaptor = ArgumentCaptor.forClass( ValueMetaInterface.class );
    verify( row ).addValueMeta( argumentCaptor.capture() );
    assertEquals( "parent step", argumentCaptor.getValue().getOrigin() );
  }

  @Test
  public void testPrepareExecutionResultsFileFields() throws Exception {
    TransExecutorMeta meta = new TransExecutorMeta();
    meta = spy( meta );

    RowMetaInterface row = mock( RowMetaInterface.class );
    StepMeta nextStep = mock( StepMeta.class );

    meta.setResultFilesTargetStepMeta( nextStep );
    meta.setResultFilesFileNameField( "file_name" );

    StepMeta parent = mock( StepMeta.class );
    doReturn( parent ).when( meta ).getParentStepMeta();
    when( parent.getName() ).thenReturn( "parent step" );

    meta.prepareExecutionResultsFileFields( row, nextStep );

    // make sure we get the name of the parent step meta... used for the origin step
    verify( parent ).getName();
    ArgumentCaptor<ValueMetaInterface> argumentCaptor = ArgumentCaptor.forClass( ValueMetaInterface.class );
    verify( row ).addValueMeta( argumentCaptor.capture() );
    assertEquals( "parent step", argumentCaptor.getValue().getOrigin() );
  }

  @Test
  public void testPrepareResultsRowsFields() throws Exception {
    TransExecutorMeta meta = new TransExecutorMeta();
    String[] outputFieldNames = new String[]{ "one", "two" };
    int[] outputFieldTypes = new int[]{ 0, 1 };
    int[] outputFieldLength = new int[]{ 4, 8 };
    int[] outputFieldPrecision = new int[]{ 2, 4 };

    meta.setOutputRowsField( outputFieldNames );
    meta.setOutputRowsType( outputFieldTypes );
    meta.setOutputRowsLength( outputFieldLength );
    meta.setOutputRowsPrecision( outputFieldPrecision );
    meta = spy( meta );

    RowMetaInterface row = mock( RowMetaInterface.class );

    StepMeta parent = mock( StepMeta.class );
    doReturn( parent ).when( meta ).getParentStepMeta();
    when( parent.getName() ).thenReturn( "parent step" );

    meta.prepareResultsRowsFields( row );

    // make sure we get the name of the parent step meta... used for the origin step
    verify( parent, times( outputFieldNames.length ) ).getName();
    ArgumentCaptor<ValueMetaInterface> argumentCaptor = ArgumentCaptor.forClass( ValueMetaInterface.class );
    verify( row, times( outputFieldNames.length ) ).addValueMeta( argumentCaptor.capture() );
    assertEquals( "parent step", argumentCaptor.getValue().getOrigin() );
  }

  @Test
  public void testGetFields() throws Exception {
    TransExecutorMeta meta = new TransExecutorMeta();
    meta = spy( meta );

    StepMeta nextStep = mock( StepMeta.class );

    // Test null
    meta.getFields( null, null, null, nextStep, null, null, null );
    verify( meta, never() ).addFieldToRow( any( RowMetaInterface.class ), anyString(), anyInt() );

    RowMetaInterface rowMeta = mock( RowMetaInterface.class );
    meta.getFields( rowMeta, null, null, nextStep, null, null, null );
    verify( rowMeta, never() ).clear();

    StepMeta executionResultTargetStepMeta = mock( StepMeta.class );
    meta.setExecutionResultTargetStepMeta( executionResultTargetStepMeta );
    meta.getFields( rowMeta, null, null, nextStep, null, null, null );
    verify( rowMeta, atMost( 1 ) ).clear();
    meta.setExecutionResultTargetStepMeta( null );

    StepMeta resultFilesTargetStepMeta = mock( StepMeta.class );
    meta.setResultFilesTargetStepMeta( resultFilesTargetStepMeta );
    meta.getFields( rowMeta, null, null, nextStep, null, null, null );
    verify( rowMeta, atMost( 1 ) ).clear();
    meta.setResultFilesTargetStepMeta( null );

    StepMeta outputRowsSourceStepMeta = mock( StepMeta.class );
    meta.setOutputRowsSourceStepMeta( outputRowsSourceStepMeta );
    meta.getFields( rowMeta, null, null, nextStep, null, null, null );
    verify( rowMeta, atMost( 1 ) ).clear();
    meta.setOutputRowsSourceStepMeta( null );
  }

  @Test
  public void testClone() throws Exception {
    TransExecutorMeta meta = new TransExecutorMeta();
    meta.setOutputRowsField( new String[] { "field1", "field2" } );
    meta.setOutputRowsLength( new int[] { 5, 5 } );
    meta.setOutputRowsPrecision( new int[] { 5, 5 } );
    meta.setOutputRowsType( new int[] { 0, 0 } );

    TransExecutorMeta cloned = (TransExecutorMeta) meta.clone();
    assertFalse( cloned.getOutputRowsField() == meta.getOutputRowsField() );
    assertTrue( Arrays.equals( cloned.getOutputRowsField(), meta.getOutputRowsField() ) );
    assertFalse( cloned.getOutputRowsLength() == meta.getOutputRowsLength() );
    assertTrue( Arrays.equals( cloned.getOutputRowsLength(), meta.getOutputRowsLength() ) );
    assertFalse( cloned.getOutputRowsPrecision() == meta.getOutputRowsPrecision() );
    assertTrue( Arrays.equals( cloned.getOutputRowsPrecision(), meta.getOutputRowsPrecision() ) );
    assertFalse( cloned.getOutputRowsType() == meta.getOutputRowsType() );
    assertTrue( Arrays.equals( cloned.getOutputRowsType(), meta.getOutputRowsType() ) );
  }

  @SuppressWarnings( "unchecked" )
  private static StepIOMetaInterface mockStepIo( StreamInterface stream, int desiredIndex ) {
    List<StreamInterface> list = mock( List.class );
    when( list.indexOf( stream ) ).thenReturn( desiredIndex );
    when( list.get( eq( desiredIndex ) ) ).thenReturn( stream );

    StepIOMetaInterface stepIo = mock( StepIOMetaInterface.class );
    when( stepIo.getTargetStreams() ).thenReturn( list );
    return stepIo;
  }

  private static StreamInterface mockStream() {
    StepMeta stepMeta = mock( StepMeta.class );
    StreamInterface stream = mock( StreamInterface.class );
    when( stream.getStepMeta() ).thenReturn( stepMeta );
    return stream;
  }


  @Test
  public void testRemoveHopFrom() throws Exception {
    TransExecutorMeta transExecutorMeta = new TransExecutorMeta();
    transExecutorMeta.setExecutionResultTargetStepMeta( new StepMeta() );
    transExecutorMeta.setOutputRowsSourceStepMeta( new StepMeta() );
    transExecutorMeta.setResultFilesTargetStepMeta( new StepMeta() );
    transExecutorMeta.setExecutorsOutputStepMeta( new StepMeta() );

    transExecutorMeta.cleanAfterHopFromRemove();

    assertNull( transExecutorMeta.getExecutionResultTargetStepMeta() );
    assertNull( transExecutorMeta.getOutputRowsSourceStepMeta() );
    assertNull( transExecutorMeta.getResultFilesTargetStepMeta() );
    assertNull( transExecutorMeta.getExecutorsOutputStepMeta() );
  }

}
