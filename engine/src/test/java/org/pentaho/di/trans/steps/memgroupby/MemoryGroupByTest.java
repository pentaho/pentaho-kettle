/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowHandler;
import org.pentaho.di.trans.step.StepMeta;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith( PowerMockRunner.class )
@PrepareForTest( PluginRegistry.class )
public class MemoryGroupByTest {

  @Mock private StepMeta mockStepMeta;
  @Mock private MemoryGroupByData mockStepDataInterface;
  @Mock private TransMeta mockTransMeta;
  @Mock private Trans mockTrans;
  @Mock private RowHandler mockRowHandler;

  @Mock private PluginInterface numberPlugin;
  @Mock private PluginInterface sringPlugin;
  @Mock private PluginInterface integerPlugin;

  @Mock private PluginRegistry pluginRegistry;

  private int currentInputRow;

  @Before
  public void before() throws Exception {

    mockStatic( PluginRegistry.class );
    when( PluginRegistry.getInstance() ).thenReturn( pluginRegistry );
    when( pluginRegistry.getPlugin( ValueMetaPluginType.class, "1" ) ).thenReturn( numberPlugin );
    when( pluginRegistry.getPlugin( ValueMetaPluginType.class, "2" ) ).thenReturn( sringPlugin );
    when( pluginRegistry.getPlugin( ValueMetaPluginType.class, "5" ) ).thenReturn( integerPlugin );

    when( pluginRegistry.loadClass( numberPlugin, ValueMetaInterface.class ) ).thenReturn( new ValueMetaNumber() );
    when( pluginRegistry.loadClass( sringPlugin, ValueMetaInterface.class ) ).thenReturn( new ValueMetaString() );
    when( pluginRegistry.loadClass( integerPlugin, ValueMetaInterface.class ) ).thenReturn( new ValueMetaInteger() );
  }

  @Test
  public void testCalculations() throws Exception {

    final RowMeta inputRowMeta = new RowMeta() {
      @Override
      public RowMeta clone() {
        final RowMeta inputRowMetaClone = new RowMeta();
        inputRowMetaClone.addValueMeta( new ValueMetaString( "NAME" ) );
        inputRowMetaClone.addValueMeta( new ValueMetaNumber( "VALUE" ) );
        return inputRowMetaClone;
      }
    };
    inputRowMeta.addValueMeta( new ValueMetaString( "NAME" ) );
    inputRowMeta.addValueMeta( new ValueMetaNumber( "VALUE" ) );

    final RowMetaAndData[] inputRows = new RowMetaAndData[] {
      new RowMetaAndData( inputRowMeta, "Foo", 2255.84 ),
      new RowMetaAndData( inputRowMeta, "Foo", 7381.16 ),
      new RowMetaAndData( inputRowMeta, "Foo", 4516.22 )
    };

    MemoryGroupByMeta groupByMeta = new MemoryGroupByMeta();
    groupByMeta.setValueField( new String[] { "10", "10", "10" } ); // 10th percentile, a value for each row
    groupByMeta.setSubjectField( new String[] {
      "VALUE", "VALUE", "VALUE", "NAME", "NAME", "VALUE", "NAME", "VALUE", "VALUE" } );
    groupByMeta.setGroupField( new String[] { "NAME" } );
    groupByMeta.setAggregateField( new String[] { "Sum", "Average (Mean)", "Percentile",
      "Number of Distinct Values (N)", "Number of Values (N)", "Standard deviation",
      "Concatenate strings separated by ,", "Minimum", "Maximum" } );
    groupByMeta.setAggregateType( new int[] { MemoryGroupByMeta.TYPE_GROUP_SUM, MemoryGroupByMeta.TYPE_GROUP_AVERAGE,
      MemoryGroupByMeta.TYPE_GROUP_PERCENTILE, MemoryGroupByMeta.TYPE_GROUP_COUNT_DISTINCT,
      MemoryGroupByMeta.TYPE_GROUP_COUNT_ALL, MemoryGroupByMeta.TYPE_GROUP_STANDARD_DEVIATION,
      MemoryGroupByMeta.TYPE_GROUP_CONCAT_COMMA, MemoryGroupByMeta.TYPE_GROUP_MIN,
      MemoryGroupByMeta.TYPE_GROUP_MAX } );
    groupByMeta.setParentStepMeta( mockStepMeta );

    final String stepName = "Group by";
    when( mockStepMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockStepMeta.getName() ).thenReturn( stepName );
    when( mockTransMeta.findStep( stepName ) ).thenReturn( mockStepMeta );
    try {
      when( mockRowHandler.getRow() ).thenAnswer( ( InvocationOnMock invocation ) -> {
        Object[] result = null;
        if ( currentInputRow < inputRows.length ) {
          result = inputRows[ currentInputRow ].getData().clone();
          currentInputRow++;
        }
        return result;
      } );
    } catch ( KettleException e ) {
      fail( e.getMessage() );
    }

    final MemoryGroupBy groupBy = new MemoryGroupBy( mockStepMeta, mockStepDataInterface, 0, mockTransMeta, mockTrans );
    groupBy.setRowHandler( mockRowHandler );
    groupBy.setInputRowMeta( inputRowMeta );
    groupBy.setLogLevel( LogLevel.ERROR );

    final Object[] groupData = new Object[] { "Foo" };
    mockStepDataInterface.map = new HashMap<MemoryGroupByData.HashEntry, Aggregate>();
    when( mockStepDataInterface.getHashEntry( groupData ) ).thenReturn(
      mockStepDataInterface.new HashEntry( groupData ) );

    int rowsProcessed = -1;
    boolean result = true;
    while ( result ) {
      rowsProcessed++;
      result = groupBy.processRow( groupByMeta, mockStepDataInterface );
    }

    assertEquals( inputRows.length, rowsProcessed );

    for ( MemoryGroupByData.HashEntry entry : mockStepDataInterface.map.keySet() ) {
      Aggregate aggregate = mockStepDataInterface.map.get( entry );
      final Object[] results = groupBy.getAggregateResult( aggregate );
      assertNotNull( results );
      assertEquals( groupByMeta.getSubjectField().length, results.length );

      final NumberFormat formatter = new DecimalFormat( "#.##" );
      assertEquals( "14153.22", formatter.format( results[ 0 ] ) ); // sum
      assertEquals( "4717.74", formatter.format( results[ 1 ] ) ); // average
      assertEquals( "2255.84", formatter.format( results[ 2 ] ) ); // 10th percentile (nearest rank)
      assertEquals( 1L, results[ 3 ] ); // distinct count
      assertEquals( 3L, results[ 4 ] ); // total count
      assertEquals( "2568.6", formatter.format( results[ 5 ] ) ); // standard deviation
      assertEquals( "Foo, Foo, Foo", results[ 6 ] ); // concat, comma
      assertEquals( "2255.84", formatter.format( results[ 7 ] ) ); // min
      assertEquals( "7381.16", formatter.format( results[ 8 ] ) ); // max
    }
  }
}
