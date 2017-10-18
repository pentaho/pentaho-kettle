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

package org.pentaho.di.trans.steps.memgroupby;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.TreeBasedTable;

/**
 * @author nhudak
 */

@RunWith( org.mockito.runners.MockitoJUnitRunner.class )
public class MemoryGroupByAggregationTest {

  private Variables variables;
  private Map<String, Integer> aggregates;

  public static final String STEP_NAME = "testStep";
  private static final ImmutableMap<String, Integer> default_aggregates;

  static {
    default_aggregates = ImmutableMap.<String, Integer>builder()
      .put( "min", MemoryGroupByMeta.TYPE_GROUP_MIN )
      .put( "max", MemoryGroupByMeta.TYPE_GROUP_MAX )
      .put( "sum", MemoryGroupByMeta.TYPE_GROUP_SUM )
      .put( "ave", MemoryGroupByMeta.TYPE_GROUP_AVERAGE )
      .put( "count", MemoryGroupByMeta.TYPE_GROUP_COUNT_ALL )
      .put( "count_any", MemoryGroupByMeta.TYPE_GROUP_COUNT_ANY )
      .put( "count_distinct", MemoryGroupByMeta.TYPE_GROUP_COUNT_DISTINCT )
      .build();
  }

  private RowMeta rowMeta;
  private TreeBasedTable<Integer, Integer, Optional<Object>> data;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    rowMeta = new RowMeta();
    data = TreeBasedTable.create();
    variables = new Variables();
    aggregates = Maps.newHashMap( default_aggregates );
  }

  @Test
  public void testDefault() throws Exception {
    addColumn( new ValueMetaInteger( "intg" ), 0L, 1L, 1L, 10L );
    addColumn( new ValueMetaInteger( "nul" ) );
    addColumn( new ValueMetaInteger( "mix1" ), -1L, 2L );
    addColumn( new ValueMetaInteger( "mix2" ), null, 7L );
    addColumn( new ValueMetaNumber( "mix3" ), -1.0, 2.5 );
    addColumn( new ValueMetaDate( "date1" ), new Date( 1L ), new Date( 2L ) );

    RowMetaAndData output = runStep();

    assertThat( output.getInteger( "intg_min" ), is( 0L ) );
    assertThat( output.getInteger( "intg_max" ), is( 10L ) );
    assertThat( output.getInteger( "intg_sum" ), is( 12L ) );
    assertThat( output.getInteger( "intg_ave" ), is( 3L ) );
    assertThat( output.getInteger( "intg_count" ), is( 4L ) );
    assertThat( output.getInteger( "intg_count_any" ), is( 4L ) );
    assertThat( output.getInteger( "intg_count_distinct" ), is( 3L ) );

    assertThat( output.getInteger( "nul_min" ), nullValue() );
    assertThat( output.getInteger( "nul_max" ), nullValue() );
    assertThat( output.getInteger( "nul_sum" ), nullValue() );
    assertThat( output.getInteger( "nul_ave" ), nullValue() );
    assertThat( output.getInteger( "nul_count" ), is( 0L ) );
    assertThat( output.getInteger( "nul_count_any" ), is( 4L ) );
    assertThat( output.getInteger( "nul_count_distinct" ), is( 0L ) );

    assertThat( output.getInteger( "mix1_max" ), is( 2L ) );
    assertThat( output.getInteger( "mix1_min" ), is( -1L ) );
    assertThat( output.getInteger( "mix1_sum" ), is( 1L ) );
    assertThat( output.getInteger( "mix1_ave" ), is( 0L ) );
    assertThat( output.getInteger( "mix1_count" ), is( 2L ) );
    assertThat( output.getInteger( "mix1_count_any" ), is( 4L ) );
    assertThat( output.getInteger( "mix1_count_distinct" ), is( 2L ) );

    assertThat( output.getInteger( "mix2_max" ), is( 7L ) );
    assertThat( output.getInteger( "mix2_min" ), is( 7L ) );
    assertThat( output.getInteger( "mix2_sum" ), is( 7L ) );
    assertThat( output.getNumber( "mix2_ave", Double.NaN ), is( 7.0 ) );
    assertThat( output.getInteger( "mix2_count" ), is( 1L ) );
    assertThat( output.getInteger( "mix2_count_any" ), is( 4L ) );
    assertThat( output.getInteger( "mix2_count_distinct" ), is( 1L ) );

    assertThat( output.getNumber( "mix3_max", Double.NaN ), is( 2.5 ) );
    assertThat( output.getNumber( "mix3_min", Double.NaN ), is( -1.0 ) );
    assertThat( output.getNumber( "mix3_sum", Double.NaN ), is( 1.5 ) );
    assertThat( output.getNumber( "mix3_ave", Double.NaN ), is( 0.75 ) );
    assertThat( output.getInteger( "mix3_count" ), is( 2L ) );
    assertThat( output.getInteger( "mix3_count_any" ), is( 4L ) );
    assertThat( output.getInteger( "mix3_count_distinct" ), is( 2L ) );

    assertThat( output.getNumber( "date1_min", Double.NaN ), is( 1.0 ) );
    assertThat( output.getNumber( "date1_max", Double.NaN ), is( 2.0 ) );
    assertThat( output.getNumber( "date1_sum", Double.NaN ), is( 3.0 ) );
    assertThat( output.getNumber( "date1_ave", Double.NaN ), is( 1.5 ) );
    assertThat( output.getInteger( "date1_count" ), is( 2L ) );
    assertThat( output.getInteger( "date1_count_any" ), is( 4L ) );
    assertThat( output.getInteger( "date1_count_distinct" ), is( 2L ) );
  }

  @Test
  public void testCompatibility() throws KettleException {
    variables.setVariable( Const.KETTLE_COMPATIBILITY_MEMORY_GROUP_BY_SUM_AVERAGE_RETURN_NUMBER_TYPE, "Y" );

    addColumn( new ValueMetaInteger( "intg" ), 0L, 1L, 1L, 10L );
    addColumn( new ValueMetaInteger( "nul" ) );
    addColumn( new ValueMetaInteger( "mix1" ), -1L, 2L );
    addColumn( new ValueMetaInteger( "mix2" ), null, 7L );
    addColumn( new ValueMetaNumber( "mix3" ), -1.0, 2.5 );

    RowMetaAndData output = runStep();

    assertThat( output.getInteger( "intg_min" ), is( 0L ) );
    assertThat( output.getInteger( "intg_max" ), is( 10L ) );
    assertThat( output.getInteger( "intg_sum" ), is( 12L ) );
    assertThat( output.getInteger( "intg_ave" ), is( 3L ) );
    assertThat( output.getInteger( "intg_count" ), is( 4L ) );
    assertThat( output.getInteger( "intg_count_any" ), is( 4L ) );
    assertThat( output.getInteger( "intg_count_distinct" ), is( 3L ) );

    assertThat( output.getInteger( "nul_min" ), nullValue() );
    assertThat( output.getInteger( "nul_max" ), nullValue() );
    assertThat( output.getInteger( "nul_sum" ), nullValue() );
    assertThat( output.getInteger( "nul_ave" ), nullValue() );
    assertThat( output.getInteger( "nul_count" ), is( 0L ) );
    assertThat( output.getInteger( "nul_count_any" ), is( 4L ) );
    assertThat( output.getInteger( "nul_count_distinct" ), is( 0L ) );

    assertThat( output.getInteger( "mix1_max" ), is( 2L ) );
    assertThat( output.getInteger( "mix1_min" ), is( -1L ) );
    assertThat( output.getInteger( "mix1_sum" ), is( 1L ) );
    assertThat( output.getNumber( "mix1_ave", Double.NaN ), is( 0.5 ) );
    assertThat( output.getInteger( "mix1_count" ), is( 2L ) );
    assertThat( output.getInteger( "mix1_count_any" ), is( 4L ) );
    assertThat( output.getInteger( "mix1_count_distinct" ), is( 2L ) );

    assertThat( output.getInteger( "mix2_max" ), is( 7L ) );
    assertThat( output.getInteger( "mix2_min" ), is( 7L ) );
    assertThat( output.getInteger( "mix2_sum" ), is( 7L ) );
    assertThat( output.getNumber( "mix2_ave", Double.NaN ), is( 7.0 ) );
    assertThat( output.getInteger( "mix2_count" ), is( 1L ) );
    assertThat( output.getInteger( "mix2_count_any" ), is( 4L ) );
    assertThat( output.getInteger( "mix2_count_distinct" ), is( 1L ) );

    assertThat( output.getNumber( "mix3_max", Double.NaN ), is( 2.5 ) );
    assertThat( output.getNumber( "mix3_min", Double.NaN ), is( -1.0 ) );
    assertThat( output.getNumber( "mix3_sum", Double.NaN ), is( 1.5 ) );
    assertThat( output.getNumber( "mix3_ave", Double.NaN ), is( 0.75 ) );
    assertThat( output.getInteger( "mix3_count" ), is( 2L ) );
    assertThat( output.getInteger( "mix3_count_any" ), is( 4L ) );
    assertThat( output.getInteger( "mix3_count_distinct" ), is( 2L ) );
  }

  @Test
  public void testNullMin() throws Exception {
    variables.setVariable( Const.KETTLE_AGGREGATION_MIN_NULL_IS_VALUED, "Y" );

    addColumn( new ValueMetaInteger( "intg" ), null, 0L, 1L, -1L );
    addColumn( new ValueMetaString( "str" ), "A", null, "B", null );

    aggregates = Maps.toMap( ImmutableList.of( "min", "max" ), Functions.forMap( default_aggregates ) );

    RowMetaAndData output = runStep();

    assertThat( output.getInteger( "intg_min" ), nullValue() );
    assertThat( output.getInteger( "intg_max" ), is( 1L ) );

    assertThat( output.getString( "str_min", null ), nullValue() );
    assertThat( output.getString( "str_max", "invalid" ), is( "B" ) );
  }

  @Test
  public void testNullsAreZeroCompatible() throws Exception {
    variables.setVariable( Const.KETTLE_AGGREGATION_ALL_NULLS_ARE_ZERO, "Y" );
    variables.setVariable( Const.KETTLE_COMPATIBILITY_MEMORY_GROUP_BY_SUM_AVERAGE_RETURN_NUMBER_TYPE, "Y" );

    addColumn( new ValueMetaInteger( "nul" ) );
    addColumn( new ValueMetaInteger( "both" ), -2L, 0L, null, 10L );

    RowMetaAndData output = runStep();

    assertThat( output.getInteger( "nul_min" ), is( 0L ) );
    assertThat( output.getInteger( "nul_max" ), is( 0L ) );
    assertThat( output.getInteger( "nul_sum" ), is( 0L ) );
    assertThat( output.getInteger( "nul_ave" ), is( 0L ) );
    assertThat( output.getInteger( "nul_count" ), is( 0L ) );
    assertThat( output.getInteger( "nul_count_any" ), is( 4L ) );
    assertThat( output.getInteger( "nul_count_distinct" ), is( 0L ) );

    assertThat( output.getInteger( "both_max" ), is( 10L ) );
    assertThat( output.getInteger( "both_min" ), is( -2L ) );
    assertThat( output.getInteger( "both_sum" ), is( 8L ) );
    assertThat( output.getInteger( "both_ave" ), is( 3L ) );
    assertThat( output.getInteger( "both_count" ), is( 3L ) );
    assertThat( output.getInteger( "both_count_any" ), is( 4L ) );
    assertThat( output.getInteger( "both_count_distinct" ), is( 3L ) );
  }

  @Test
  public void testNullsAreZeroDefault() throws Exception {
    variables.setVariable( Const.KETTLE_AGGREGATION_ALL_NULLS_ARE_ZERO, "Y" );

    addColumn( new ValueMetaInteger( "nul" ) );
    addColumn( new ValueMetaInteger( "both" ), -2L, 0L, null, 10L );
    addColumn( new ValueMetaNumber( "both_num" ), -2.0, 0.0, null, 10.0 );

    RowMetaAndData output = runStep();

    assertThat( output.getInteger( "nul_min" ), is( 0L ) );
    assertThat( output.getInteger( "nul_max" ), is( 0L ) );
    assertThat( output.getInteger( "nul_sum" ), is( 0L ) );
    assertThat( output.getInteger( "nul_ave" ), is( 0L ) );
    assertThat( output.getInteger( "nul_count" ), is( 0L ) );
    assertThat( output.getInteger( "nul_count_any" ), is( 4L ) );
    assertThat( output.getInteger( "nul_count_distinct" ), is( 0L ) );

    assertThat( output.getInteger( "both_max" ), is( 10L ) );
    assertThat( output.getInteger( "both_min" ), is( -2L ) );
    assertThat( output.getInteger( "both_sum" ), is( 8L ) );
    assertThat( output.getInteger( "both_ave" ), is( 2L ) );
    assertThat( output.getInteger( "both_count" ), is( 3L ) );
    assertThat( output.getInteger( "both_count_any" ), is( 4L ) );
    assertThat( output.getInteger( "both_count_distinct" ), is( 3L ) );

    assertThat( output.getNumber( "both_num_max", Double.NaN ), is( 10.0 ) );
    assertThat( output.getNumber( "both_num_min", Double.NaN ), is( -2.0 ) );
    assertThat( output.getNumber( "both_num_sum", Double.NaN ), is( 8.0 ) );
    assertEquals( 2.666666, output.getNumber( "both_num_ave", Double.NaN ), 0.000001 /* delta */ );
    assertThat( output.getInteger( "both_num_count" ), is( 3L ) );
    assertThat( output.getInteger( "both_num_count_any" ), is( 4L ) );
    assertThat( output.getInteger( "both_num_count_distinct" ), is( 3L ) );
  }

  @Test
  public void testSQLCompatible() throws Exception {
    addColumn( new ValueMetaInteger( "value" ), null, -2L, null, 0L, null, 10L, null, null, 0L, null );

    RowMetaAndData output = runStep();

    assertThat( output.getInteger( "value_max" ), is( 10L ) );
    assertThat( output.getInteger( "value_min" ), is( -2L ) );
    assertThat( output.getInteger( "value_sum" ), is( 8L ) );
    assertThat( output.getInteger( "value_ave" ), is( 2L ) );
    assertThat( output.getInteger( "value_count" ), is( 4L ) );
    assertThat( output.getInteger( "value_count_any" ), is( 10L ) );
    assertThat( output.getInteger( "value_count_distinct" ), is( 3L ) );
  }

  private RowMetaAndData runStep() throws KettleException {
    // Allocate meta
    List<String> aggKeys = ImmutableList.copyOf( aggregates.keySet() );
    MemoryGroupByMeta meta = new MemoryGroupByMeta();
    meta.allocate( 0, rowMeta.size() * aggKeys.size() );
    for ( int i = 0; i < rowMeta.size(); i++ ) {
      String name = rowMeta.getValueMeta( i ).getName();
      for ( int j = 0; j < aggKeys.size(); j++ ) {
        String aggKey = aggKeys.get( j );
        int index = i * aggKeys.size() + j;

        meta.getAggregateField()[index] = name + "_" + aggKey;
        meta.getSubjectField()[index] = name;
        meta.getAggregateType()[index] = aggregates.get( aggKey );
      }
    }

    MemoryGroupByData data = new MemoryGroupByData();
    data.map = Maps.newHashMap();

    // Add to trans
    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = new StepMeta( STEP_NAME, meta );
    when( transMeta.findStep( STEP_NAME ) ).thenReturn( stepMeta );

    // Spy on step, regrettable but we need to easily inject rows
    MemoryGroupBy step = spy( new MemoryGroupBy( stepMeta, data, 0, transMeta, mock( Trans.class ) ) );
    step.copyVariablesFrom( variables );
    doNothing().when( step ).putRow( (RowMetaInterface) any(), (Object[]) any() );
    doNothing().when( step ).setOutputDone();

    // Process rows
    doReturn( rowMeta ).when( step ).getInputRowMeta();
    for ( Object[] row : getRows() ) {
      doReturn( row ).when( step ).getRow();
      assertThat( step.processRow( meta, data ), is( true ) );
    }
    verify( step, never() ).putRow( (RowMetaInterface) any(), (Object[]) any() );

    // Mark stop
    doReturn( null ).when( step ).getRow();
    assertThat( step.processRow( meta, data ), is( false ) );
    verify( step ).setOutputDone();

    // Collect output
    ArgumentCaptor<RowMetaInterface> rowMetaCaptor = ArgumentCaptor.forClass( RowMetaInterface.class );
    ArgumentCaptor<Object[]> rowCaptor = ArgumentCaptor.forClass( Object[].class );
    verify( step ).putRow( rowMetaCaptor.capture(), rowCaptor.capture() );

    return new RowMetaAndData( rowMetaCaptor.getValue(), rowCaptor.getValue() );
  }

  private void addColumn( ValueMetaInterface meta, Object... values ) {
    int column = rowMeta.size();

    rowMeta.addValueMeta( meta );
    for ( int row = 0; row < values.length; row++ ) {
      data.put( row, column, Optional.fromNullable( values[row] ) );
    }
  }

  private Iterable<Object[]> getRows() {
    if ( data.isEmpty() ) {
      return ImmutableSet.of();
    }

    Range<Integer> rows = Range.closed( 0, data.rowMap().lastKey() );

    return FluentIterable.from( ContiguousSet.create( rows, DiscreteDomain.integers() ) )
      .transform( Functions.forMap( data.rowMap(), ImmutableMap.<Integer, Optional<Object>>of() ) )
      .transform( new Function<Map<Integer, Optional<Object>>, Object[]>() {
        @Override public Object[] apply( Map<Integer, Optional<Object>> input ) {
          Object[] row = new Object[rowMeta.size()];
          for ( Map.Entry<Integer, Optional<Object>> entry : input.entrySet() ) {
            row[entry.getKey()] = entry.getValue().orNull();
          }
          return row;
        }
      } );
  }
}
