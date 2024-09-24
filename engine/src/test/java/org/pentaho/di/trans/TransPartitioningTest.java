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

package org.pentaho.di.trans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleTransException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;

/**
 * <p>This test verify transformation step initializations and row sets distributions based
 * on different steps execution. In this tests uses one step as a producer and one step as a consumer.
 * Examines different situations when step runs in multiple copies or partitioned. One of
 * the possible issues of incorrect rowsets initialization described in PDI-12140.</p>
 * So next combinations is examined:
 * <ol>
 * <li>1 - 2x - when one step copy is hoped to step running in 2 copies
 * <li>2x - 2x - when step running in 2 copies hops to step running in 2 copies
 * <li>2x - 1 - when step running in 2 copies hops to step running in 1 copy
 * <li>1 - cl1 - when step running in one copy hops to step running partitioned
 * <li>cl1-cl1 - when step running partitioned hops to step running partitioned (swim lanes case)
 * <li>cl1-cl2 - when step running partitioned by one partitioner hops to step partitioned by another partitioner
 * <li>x2-cl1 - when step running in 2 copies hops to partitioned step
 * 
 */
public class TransPartitioningTest {

  /**
   * This is convenient names for testing steps in transformation.
   * 
   * The trick is if we use numeric names for steps we can use NavigableSet to find next or previous when mocking
   * appropriate TransMeta methods (comparable strings).
   */
  private final String ONE = "1";
  private final String TWO = "2";
  private final String S10 = "1.0";
  private final String S11 = "1.1";
  private final String S20 = "2.0";
  private final String S21 = "2.1";
  private final String PID1 = "a";
  private final String PID2 = "b";
  private final String SP10 = "1.a";
  private final String SP11 = "1.b";
  private final String SP20 = "2.a";
  private final String SP21 = "2.b";

  @Mock
  LogChannelInterface log;

  Trans trans;

  /**
   * Step meta is sorted according StepMeta name so using numbers of step names we can easy build step chain mock.
   */
  private final NavigableSet<StepMeta> chain = new TreeSet<StepMeta>();

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks( this );
    trans = new Trans() {
      @Override
      public void calculateBatchIdAndDateRange() throws KettleTransException {
        // avoid NPE if called
      }

      @Override
      public void beginProcessing() throws KettleTransException {
        // avoid NPE if called
      }
    };
    // prepare TransMeta complete mock:
    TransMeta meta = Mockito.mock( TransMeta.class );
    Mockito.when( meta.getName() ).thenReturn( "junit meta" );
    Mockito.when( meta.getTransformationType() ).thenReturn( TransformationType.Normal );
    Mockito.when( meta.getSizeRowset() ).thenReturn( 13 );

    Mockito.when( meta.getTransHopSteps( Mockito.anyBoolean() ) ).thenAnswer( new Answer<List<StepMeta>>() {
      @Override
      public List<StepMeta> answer( InvocationOnMock invocation ) throws Throwable {
        return ( new ArrayList<StepMeta>( chain ) );
      }
    } );
    Mockito.when( meta.findNextSteps( Mockito.any( StepMeta.class ) ) ).then( new Answer<List<StepMeta>>() {
      @Override
      public List<StepMeta> answer( InvocationOnMock invocation ) throws Throwable {
        Object obj = invocation.getArguments()[0];
        StepMeta findFor = StepMeta.class.cast( obj );
        List<StepMeta> ret = new ArrayList<StepMeta>();
        StepMeta nextStep = chain.higher( findFor );
        if ( nextStep != null ) {
          ret.add( nextStep );
        }
        return ret;
      }
    } );
    Mockito.when( meta.findPreviousSteps( Mockito.any( StepMeta.class ), Mockito.anyBoolean() ) ).thenAnswer(
        new Answer<List<StepMeta>>() {
          @Override
          public List<StepMeta> answer( InvocationOnMock invocation ) throws Throwable {
            Object obj = invocation.getArguments()[0];
            StepMeta findFor = StepMeta.class.cast( obj );
            List<StepMeta> ret = new ArrayList<StepMeta>();
            StepMeta prevStep = chain.lower( findFor );
            if ( prevStep != null ) {
              ret.add( prevStep );
            }
            return ret;
          }
        } );
    Mockito.when( meta.findStep( Mockito.anyString() ) ).thenAnswer( new Answer<StepMeta>() {
      @Override
      public StepMeta answer( InvocationOnMock invocation ) throws Throwable {
        Object obj = invocation.getArguments()[0];
        String findFor = String.class.cast( obj );
        for ( StepMeta item : chain ) {
          if ( item.getName().equals( findFor ) ) {
            return item;
          }
        }
        return null;
      }
    } );

    trans.setLog( log );
    trans.setTransMeta( meta );
  }

  /**
   * This checks transformation initialization when using one to many copies
   * 
   * @throws KettleException
   */
  @Test
  public void testOneToManyCopies() throws KettleException {
    prepareStepMetas_1_x2();

    trans.prepareExecution( new String[] {} );
    List<RowSet> rowsets = trans.getRowsets();
    assertTrue( !rowsets.isEmpty() );
    assertEquals( "We have 2 rowsets finally", 2, rowsets.size() );
    assertEquals( "We have 3 steps: one producer and 2 copies of consumer", 3, trans.getSteps().size() );

    // Ok, examine initialized steps now.
    StepInterface stepOne = getStepByName( S10 );
    assertTrue( "1 step have no input row sets", stepOne.getInputRowSets().isEmpty() );
    assertEquals( "1 step have 2 output rowsets", 2, stepOne.getOutputRowSets().size() );

    StepInterface stepTwo0 = getStepByName( S20 );
    Assert.assertEquals( "2.0 step have 12 input row sets", 1, stepTwo0.getInputRowSets().size() );
    Assert.assertTrue( "2.0 step have no output row sets", stepTwo0.getOutputRowSets().isEmpty() );

    StepInterface stepTwo1 = getStepByName( S21 );
    Assert.assertEquals( "2.1 step have 1 input row sets", 1, stepTwo1.getInputRowSets().size() );
    Assert.assertTrue( "2.1 step have no output row sets", stepTwo1.getOutputRowSets().isEmpty() );
  }

  /**
   * This checks transformation initialization when using many to many copies.
   * 
   * @throws KettleException
   */
  @Test
  public void testManyToManyCopies() throws KettleException {
    prepareStepMetas_x2_x2();

    trans.prepareExecution( new String[] {} );
    List<RowSet> rowsets = trans.getRowsets();
    assertTrue( !rowsets.isEmpty() );
    assertEquals( "We have 2 rowsets finally", 2, rowsets.size() );
    assertEquals( "We have 4 steps: 2 copies of producer and 2 copies of consumer", 4, trans.getSteps().size() );

    // Ok, examine initialized steps now.
    StepInterface stepOne0 = getStepByName( S10 );
    assertTrue( "1 step have no input row sets", stepOne0.getInputRowSets().isEmpty() );
    assertEquals( "1 step have 1 output rowsets", 1, stepOne0.getOutputRowSets().size() );

    StepInterface stepOne1 = getStepByName( S11 );
    assertTrue( "1 step have no input row sets", stepOne1.getInputRowSets().isEmpty() );
    assertEquals( "1 step have 1 output rowsets", 1, stepOne1.getOutputRowSets().size() );

    StepInterface stepTwo0 = getStepByName( S20 );
    Assert.assertEquals( "2.0 step have 1 input row sets", 1, stepTwo0.getInputRowSets().size() );
    Assert.assertTrue( "2.0 step have no output row sets", stepTwo0.getOutputRowSets().isEmpty() );

    StepInterface stepTwo1 = getStepByName( S21 );
    Assert.assertEquals( "2.1 step have 1 input row sets", 1, stepTwo1.getInputRowSets().size() );
    Assert.assertTrue( "2.1 step have no output row sets", stepTwo1.getOutputRowSets().isEmpty() );
  }

  /**
   * This checks transformation initialization when using many copies to one next step
   * 
   * @throws KettleException
   */
  @Test
  public void testManyToOneCopies() throws KettleException {
    prepareStepMetas_x2_1();

    trans.prepareExecution( new String[] {} );
    List<RowSet> rowsets = trans.getRowsets();
    assertTrue( !rowsets.isEmpty() );
    assertEquals( "We have 2 rowsets finally", 2, rowsets.size() );
    assertEquals( "We have 4 steps: 2 copies of producer and 2 copies of consumer", 3, trans.getSteps().size() );

    // Ok, examine initialized steps now.
    StepInterface stepOne0 = getStepByName( S10 );
    assertTrue( "1 step have no input row sets", stepOne0.getInputRowSets().isEmpty() );
    assertEquals( "1 step have 1 output rowsets", 1, stepOne0.getOutputRowSets().size() );

    StepInterface stepOne1 = getStepByName( S11 );
    assertTrue( "1 step have no input row sets", stepOne1.getInputRowSets().isEmpty() );
    assertEquals( "1 step have 1 output rowsets", 1, stepOne1.getOutputRowSets().size() );

    StepInterface stepTwo0 = getStepByName( S20 );
    Assert.assertEquals( "2.0 step have 2 input row sets", 2, stepTwo0.getInputRowSets().size() );
    Assert.assertTrue( "2.0 step have no output row sets", stepTwo0.getOutputRowSets().isEmpty() );
  }

  /**
   * Test one to one partitioning step transformation organization.
   * 
   * @throws KettleException
   */
  @Test
  public void testOneToPartitioningSchema() throws KettleException {
    prepareStepMetas_1_cl1();

    trans.prepareExecution( new String[] {} );
    List<RowSet> rowsets = trans.getRowsets();
    assertTrue( !rowsets.isEmpty() );
    assertEquals( "We have 2 rowsets finally", 2, rowsets.size() );
    assertEquals( "We have 3 steps: 1 producer and 2 copies of consumer since it is partitioned", 3, trans.getSteps()
        .size() );

    // Ok, examine initialized steps now.
    StepInterface stepOne0 = getStepByName( S10 );
    assertTrue( "1 step have no input row sets", stepOne0.getInputRowSets().isEmpty() );
    assertEquals( "1 step have 2 output rowsets", 2, stepOne0.getOutputRowSets().size() );

    StepInterface stepTwo0 = getStepByName( SP20 );
    assertEquals( "2.0 step have one input row sets", 1, stepTwo0.getInputRowSets().size() );
    assertTrue( "2.0 step have no output rowsets", stepTwo0.getOutputRowSets().isEmpty() );

    StepInterface stepTwo1 = getStepByName( SP21 );
    Assert.assertEquals( "2.1 step have 1 input row sets", 1, stepTwo1.getInputRowSets().size() );
    Assert.assertTrue( "2.1 step have no output row sets", stepTwo1.getOutputRowSets().isEmpty() );
  }

  /**
   * Test 'Swim lines partitioning'
   * 
   * @throws KettleException
   */
  @Test
  public void testSwimLanesPartitioning() throws KettleException {
    prepareStepMetas_cl1_cl1();

    trans.prepareExecution( new String[] {} );
    List<RowSet> rowsets = trans.getRowsets();
    assertTrue( !rowsets.isEmpty() );
    assertEquals( "We have 2 rowsets finally", 2, rowsets.size() );
    assertEquals( "We have 3 steps: 1 producer and 2 copies of consumer since it is partitioned", 4, trans.getSteps()
        .size() );

    // Ok, examine initialized steps now.
    StepInterface stepOne0 = getStepByName( SP10 );
    assertTrue( "1.0 step have no input row sets", stepOne0.getInputRowSets().isEmpty() );
    assertEquals( "1.0 step have 1 output rowsets", 1, stepOne0.getOutputRowSets().size() );

    StepInterface stepOne1 = getStepByName( SP11 );
    assertTrue( "1.1 step have no input row sets", stepOne1.getInputRowSets().isEmpty() );
    assertEquals( "1.1 step have 1 output rowsets", 1, stepOne1.getOutputRowSets().size() );

    StepInterface stepTwo0 = getStepByName( SP20 );
    assertEquals( "2.0 step have 2 input row sets", 1, stepTwo0.getInputRowSets().size() );
    assertTrue( "2.0 step have no output rowsets", stepTwo0.getOutputRowSets().isEmpty() );

    StepInterface stepTwo2 = getStepByName( SP21 );
    assertTrue( "2.2 step have no output row sets", stepTwo2.getOutputRowSets().isEmpty() );
    assertEquals( "2.2 step have 2 output rowsets", 1, stepTwo2.getInputRowSets().size() );
  }

  /**
   * This is PDI-12140 case. 2 steps with same partitions ID's count but different partitioner. This is not a swim lines
   * cases and we need repartitioning here.
   * 
   * @throws KettleException
   */
  @Test
  public void testDifferentPartitioningFlow() throws KettleException {
    prepareStepMetas_cl1_cl2();

    trans.prepareExecution( new String[] {} );
    List<RowSet> rowsets = trans.getRowsets();
    assertTrue( !rowsets.isEmpty() );
    assertEquals( "We have 4 rowsets finally since repartitioning happens", 4, rowsets.size() );
    assertEquals( "We have 4 steps: 2 producer copies and 2 copies of consumer since they both partitioned", 4, trans
        .getSteps().size() );

    // Ok, examine initialized steps now.
    StepInterface stepOne0 = getStepByName( SP10 );
    assertTrue( "1.0 step have no input row sets", stepOne0.getInputRowSets().isEmpty() );
    assertEquals( "1.0 step have 2 output rowsets", 2, stepOne0.getOutputRowSets().size() );

    StepInterface stepOne1 = getStepByName( SP11 );
    assertTrue( "1.1 step have no input row sets", stepOne1.getInputRowSets().isEmpty() );
    assertEquals( "1.1 step have 2 output rowsets", 2, stepOne1.getOutputRowSets().size() );

    StepInterface stepTwo0 = getStepByName( SP20 );
    assertTrue( "2.0 step have no output row sets", stepTwo0.getOutputRowSets().isEmpty() );
    assertEquals( "2.0 step have 1 input rowsets", 2, stepTwo0.getInputRowSets().size() );

    StepInterface stepTwo2 = getStepByName( SP21 );
    assertTrue( "2.1 step have no output row sets", stepTwo2.getOutputRowSets().isEmpty() );
    assertEquals( "2.2 step have 2 input rowsets", 2, stepTwo2.getInputRowSets().size() );
  }

  /**
   * This is a case when step running in many copies meets partitioning one.
   * 
   * @throws KettleException
   */
  @Test
  public void testManyCopiesToPartitioningFlow() throws KettleException {
    prepareStepMetas_x2_cl1();

    trans.prepareExecution( new String[] {} );
    List<RowSet> rowsets = trans.getRowsets();
    assertTrue( !rowsets.isEmpty() );
    assertEquals( "We have 4 rowsets finally since repartitioning happens", 4, rowsets.size() );
    assertEquals( "We have 4 steps: 2 producer copies and 2 copies of consumer since consumer is partitioned", 4, trans
        .getSteps().size() );

    // Ok, examine initialized steps now.
    StepInterface stepOne0 = getStepByName( S10 );
    assertTrue( "1.0 step have no input row sets", stepOne0.getInputRowSets().isEmpty() );
    assertEquals( "1.0 step have 2 output rowsets", 2, stepOne0.getOutputRowSets().size() );

    StepInterface stepOne1 = getStepByName( S11 );
    assertTrue( "1.1 step have no input row sets", stepOne1.getInputRowSets().isEmpty() );
    assertEquals( "1.1 step have 2 output rowsets", 2, stepOne1.getOutputRowSets().size() );

    StepInterface stepTwo0 = getStepByName( SP20 );
    assertTrue( "2.0 step have no output row sets", stepTwo0.getOutputRowSets().isEmpty() );
    assertEquals( "2.0 step have 2 input rowsets", 2, stepTwo0.getInputRowSets().size() );

    StepInterface stepTwo2 = getStepByName( SP21 );
    assertTrue( "2.1 step have no output row sets", stepTwo2.getOutputRowSets().isEmpty() );
    assertEquals( "2.2 step have 2 input rowsets", 2, stepTwo2.getInputRowSets().size() );
  }

  private StepInterface getStepByName( String name ) {
    List<StepMetaDataCombi> combiList = trans.getSteps();
    for ( StepMetaDataCombi item : combiList ) {
      if ( item.step.toString().equals( name ) ) {
        return item.step;
      }
    }
    fail( "Test error, can't find step with name: " + name );
    // and this will never happens.
    return null;
  }

  /**
   * one 'regular step' to 'step running in 2 copies'
   */
  private void prepareStepMetas_1_x2() {
    StepMeta dummy1 = new StepMeta( ONE, null );
    StepMeta dummy2 = new StepMeta( TWO, null );
    dummy2.setCopies( 2 );
    chain.add( dummy1 );
    chain.add( dummy2 );

    for ( StepMeta item : chain ) {
      item.setStepMetaInterface( new DummyTransMeta() );
    }
  }

  /**
   * one 'step running in 2 copies' to 'step running in 2 copies'
   */
  private void prepareStepMetas_x2_x2() {
    StepMeta dummy1 = new StepMeta( ONE, null );
    StepMeta dummy2 = new StepMeta( TWO, null );
    dummy1.setCopies( 2 );
    dummy2.setCopies( 2 );
    chain.add( dummy1 );
    chain.add( dummy2 );

    for ( StepMeta item : chain ) {
      item.setStepMetaInterface( new DummyTransMeta() );
    }
  }

  /**
   * many steps copies to one
   */
  private void prepareStepMetas_x2_1() {
    StepMeta dummy1 = new StepMeta( ONE, null );
    StepMeta dummy2 = new StepMeta( TWO, null );
    dummy1.setCopies( 2 );
    chain.add( dummy1 );
    chain.add( dummy2 );

    for ( StepMeta item : chain ) {
      item.setStepMetaInterface( new DummyTransMeta() );
    }
  }

  /**
   * This is a case when we have 1 step to 1 clustered step distribution.
   * 
   * @throws KettlePluginException
   */
  private void prepareStepMetas_1_cl1() throws KettlePluginException {
    StepMeta dummy1 = new StepMeta( ONE, null );
    StepMeta dummy2 = new StepMeta( TWO, null );

    PartitionSchema schema = new PartitionSchema( "p1", Arrays.asList( new String[] { PID1, PID2 } ) );
    StepPartitioningMeta partMeta = new StepPartitioningMeta( "Mirror to all partitions", schema );
    dummy2.setStepPartitioningMeta( partMeta );

    chain.add( dummy1 );
    chain.add( dummy2 );
    for ( StepMeta item : chain ) {
      item.setStepMetaInterface( new DummyTransMeta() );
    }
  }

  /**
   * This case simulates when we do have 2 step partitioned with one same partitioner We want to get a 'swim-lanes'
   * transformation
   * 
   * @throws KettlePluginException
   */
  private void prepareStepMetas_cl1_cl1() throws KettlePluginException {
    StepMeta dummy1 = new StepMeta( ONE, null );
    StepMeta dummy2 = new StepMeta( TWO, null );

    PartitionSchema schema = new PartitionSchema( "p1", Arrays.asList( new String[] { PID1, PID2 } ) );
    // for delayed binding StepPartitioning meta does not achieve
    // schema name when using in constructor so we have to set it
    // explicitly. See equals implementation for StepPartitioningMeta.
    StepPartitioningMeta partMeta = new StepPartitioningMeta( "Mirror to all partitions", schema );
    // that is what I am talking about:
    partMeta.setPartitionSchemaName( schema.getName() );

    dummy1.setStepPartitioningMeta( partMeta );
    dummy2.setStepPartitioningMeta( partMeta );

    chain.add( dummy1 );
    chain.add( dummy2 );
    for ( StepMeta item : chain ) {
      item.setStepMetaInterface( new DummyTransMeta() );
    }
  }

  /**
   * This is a case when we have 2 steps, but partitioned differently
   * 
   * @throws KettlePluginException
   */
  private void prepareStepMetas_cl1_cl2() throws KettlePluginException {
    StepMeta dummy1 = new StepMeta( ONE, null );
    StepMeta dummy2 = new StepMeta( TWO, null );

    PartitionSchema schema1 = new PartitionSchema( "p1", Arrays.asList( new String[] { PID1, PID2 } ) );
    PartitionSchema schema2 = new PartitionSchema( "p2", Arrays.asList( new String[] { PID1, PID2 } ) );

    StepPartitioningMeta partMeta1 = new StepPartitioningMeta( "Mirror to all partitions", schema1 );
    StepPartitioningMeta partMeta2 = new StepPartitioningMeta( "Mirror to all partitions", schema2 );
    partMeta1.setPartitionSchemaName( schema1.getName() );
    partMeta2.setPartitionSchemaName( schema2.getName() );

    dummy1.setStepPartitioningMeta( partMeta1 );
    dummy2.setStepPartitioningMeta( partMeta2 );

    chain.add( dummy1 );
    chain.add( dummy2 );
    for ( StepMeta item : chain ) {
      item.setStepMetaInterface( new DummyTransMeta() );
    }
  }

  /**
   * This is a case when first step running 2 copies and next is partitioned one.
   * 
   * @throws KettlePluginException
   */
  private void prepareStepMetas_x2_cl1() throws KettlePluginException {
    StepMeta dummy1 = new StepMeta( ONE, null );
    StepMeta dummy2 = new StepMeta( TWO, null );

    PartitionSchema schema1 = new PartitionSchema( "p1", Arrays.asList( new String[] { PID1, PID2 } ) );
    StepPartitioningMeta partMeta1 = new StepPartitioningMeta( "Mirror to all partitions", schema1 );

    dummy2.setStepPartitioningMeta( partMeta1 );
    dummy1.setCopies( 2 );

    chain.add( dummy1 );
    chain.add( dummy2 );
    for ( StepMeta item : chain ) {
      item.setStepMetaInterface( new DummyTransMeta() );
    }
  }
}
