/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.groupby;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class GroupByNewAggregateTest {

  static StepMockHelper<GroupByMeta, GroupByData> mockHelper;

  GroupBy step;
  GroupByData data;

  static List<Integer> strings;
  static List<Integer> statistics;

  static int def = -113;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    mockHelper = new StepMockHelper<>( "Group By", GroupByMeta.class, GroupByData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );

    // In this step we will distinct String aggregations from numeric ones
    strings = new ArrayList<>();
    strings.add( GroupByMeta.TYPE_GROUP_CONCAT_COMMA );
    strings.add( GroupByMeta.TYPE_GROUP_CONCAT_STRING );

    // Statistics will be initialized with collections...
    statistics = new ArrayList<>();
    statistics.add( GroupByMeta.TYPE_GROUP_MEDIAN );
    statistics.add( GroupByMeta.TYPE_GROUP_PERCENTILE );
  }

  @AfterClass
  public static void cleanUp() {
    mockHelper.cleanUp();
  }

  @Before
  public void setUp() throws Exception {
    data = new GroupByData();

    data.subjectnrs = new int[18];
    int[] arr = new int[18];
    String[] arrF = new String[18];
    data.previousSums = new Object[18];
    data.previousAvgCount = new long[18];
    data.previousAvgSum = new Object[18];
    for ( int i = 0; i < arr.length; i++ ) {
      // set aggregation types (hardcoded integer values from 1 to 18)
      arr[i] = i + 1;
      data.subjectnrs[i] = i;
    }
    Arrays.fill( arrF, "x" );
    Arrays.fill( data.previousSums, 11 );
    Arrays.fill( data.previousAvgCount, 12 );
    Arrays.fill( data.previousAvgSum, 13 );

    GroupByMeta meta = new GroupByMeta();
    meta.setAggregateType( arr );
    meta.setAggregateField( arrF );

    ValueMetaInterface vmi = new ValueMetaInteger();
    when( mockHelper.stepMeta.getStepMetaInterface() ).thenReturn( meta );
    RowMetaInterface rmi = Mockito.mock( RowMetaInterface.class );
    data.inputRowMeta = rmi;
    when( rmi.getValueMeta( Mockito.anyInt() ) ).thenReturn( vmi );
    data.aggMeta = rmi;
    data.agg = new Object[] { def };
    data.counts = new long[] { 1 };
    data.previousSums = new Object[] { 18 };

    step = new GroupBy( mockHelper.stepMeta, data, 0, mockHelper.transMeta, mockHelper.trans );
  }

  /**
   * PDI-6960 - Group by and Memory group by ignores NULL values ("sum" aggregation)
   * <p>
   * We do not assign 0 instead of null.
   */
  @Test
  public void newAggregateInitializationTest() {
    Object[] r = new Object[18];
    Arrays.fill( r, null );
    step.newAggregate( r );

    Object[] agg = data.agg;

    Assert.assertEquals( "All possible aggregation cases considered", 18, agg.length );

    // all aggregations types is int values, filled in ascending order in perconditions
    for ( int i = 0; i < agg.length; i++ ) {
      int type = i + 1;
      if ( strings.contains( type ) ) {
        Assert.assertTrue( "This is appendable type, type=" + type, agg[i] instanceof Appendable );
      } else if ( statistics.contains( type ) ) {
        Assert.assertTrue( "This is collection, type=" + type, agg[i] instanceof Collection );
      } else {
        Assert.assertNull( "Aggregation initialized with null, type=" + type, agg[i] );
      }
    }
  }
}
