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

package org.pentaho.di.trans.steps.memgroupby;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.di.trans.steps.memgroupby.MemoryGroupByMeta.TYPE_GROUP_COUNT_ANY;
import static org.pentaho.di.trans.steps.memgroupby.MemoryGroupByMeta.TYPE_GROUP_MAX;
import static org.pentaho.di.trans.steps.memgroupby.MemoryGroupByMeta.TYPE_GROUP_MIN;

/**
 * @author Luis Martins
 */
public class MemoryGroupByMetaGetFieldsTest {

  private MemoryGroupByMeta memoryGroupByMeta;
  private RowMetaInterface rowMeta;

  private RowMetaInterface[] mockInfo;
  private StepMeta mockNextStep;
  private VariableSpace mockSpace;
  private IMetaStore mockIMetaStore;

  @Before
  public void setup() throws KettlePluginException {
    mockSpace = mock( VariableSpace.class );
    doReturn("N" ).when( mockSpace ).getVariable( any(), anyString() );

    rowMeta = spy( new RowMeta() );
    memoryGroupByMeta = spy( new MemoryGroupByMeta() );
  }

  @After
  public void cleanup() {
  }

  @Test
  public void getFieldsWithSubject_WithFormat() {
    try ( MockedStatic<ValueMetaFactory> valueMetaFactoryMockedStatic = mockStatic( ValueMetaFactory.class ) ) {
      valueMetaFactoryMockedStatic.when( () -> ValueMetaFactory.createValueMeta( anyInt() ) ).thenCallRealMethod();
      valueMetaFactoryMockedStatic.when( () -> ValueMetaFactory.createValueMeta( anyString(), anyInt() ) ).thenCallRealMethod();
      valueMetaFactoryMockedStatic.when( () -> ValueMetaFactory.createValueMeta( eq( "maxDate" ), eq( 3 ), eq( -1 ), eq( -1 ) ) ).thenReturn( new ValueMetaDate( "maxDate" ) );
      valueMetaFactoryMockedStatic.when( () -> ValueMetaFactory.createValueMeta( eq( "minDate" ), eq( 3 ), eq( -1 ), eq( -1 ) ) ).thenReturn( new ValueMetaDate( "minDate" ) );
      valueMetaFactoryMockedStatic.when( () -> ValueMetaFactory.createValueMeta( eq( "countDate" ), eq( 5 ), eq( -1 ), eq( -1 ) ) ).thenReturn( new ValueMetaDate( "countDate" ) );
      valueMetaFactoryMockedStatic.when( () -> ValueMetaFactory.getValueMetaName( eq( 3 ) ) ).thenReturn( "Date" );
      valueMetaFactoryMockedStatic.when( () -> ValueMetaFactory.getValueMetaName( eq( 5 ) ) ).thenReturn( "Integer" );
      ValueMetaDate valueMeta = new ValueMetaDate();
      valueMeta.setConversionMask( "yyyy-MM-dd" );
      valueMeta.setName( "date" );

      doReturn( valueMeta ).when( rowMeta ).searchValueMeta( "date" );

      memoryGroupByMeta.setSubjectField( new String[] { "date" } );
      memoryGroupByMeta.setGroupField( new String[] {} );
      memoryGroupByMeta.setAggregateField( new String[] { "maxDate" } );
      memoryGroupByMeta.setAggregateType( new int[] { TYPE_GROUP_MAX } );

      memoryGroupByMeta.getFields( rowMeta, "Memory Group by", mockInfo, mockNextStep, mockSpace, null,
        mockIMetaStore );

      verify( rowMeta, times( 1 ) ).clear();
      verify( rowMeta, times( 1 ) ).addRowMeta( any() );
      assertEquals( "yyyy-MM-dd", rowMeta.searchValueMeta( "maxDate" ).getConversionMask() );
    }
  }

  @Test
  public void getFieldsWithSubject_NoFormat() {
    try ( MockedStatic<ValueMetaFactory> valueMetaFactoryMockedStatic = mockStatic( ValueMetaFactory.class ) ) {
      valueMetaFactoryMockedStatic.when( () -> ValueMetaFactory.createValueMeta( anyInt() ) ).thenCallRealMethod();
      valueMetaFactoryMockedStatic.when( () -> ValueMetaFactory.createValueMeta( anyString(), anyInt() ) )
        .thenCallRealMethod();
      valueMetaFactoryMockedStatic.when(
          () -> ValueMetaFactory.createValueMeta( eq( "maxDate" ), eq( 3 ), eq( -1 ), eq( -1 ) ) )
        .thenReturn( new ValueMetaDate( "maxDate" ) );
      valueMetaFactoryMockedStatic.when(
          () -> ValueMetaFactory.createValueMeta( eq( "minDate" ), eq( 3 ), eq( -1 ), eq( -1 ) ) )
        .thenReturn( new ValueMetaDate( "minDate" ) );
      valueMetaFactoryMockedStatic.when(
          () -> ValueMetaFactory.createValueMeta( eq( "countDate" ), eq( 5 ), eq( -1 ), eq( -1 ) ) )
        .thenReturn( new ValueMetaDate( "countDate" ) );
      valueMetaFactoryMockedStatic.when( () -> ValueMetaFactory.getValueMetaName( eq( 3 ) ) ).thenReturn( "Date" );
      valueMetaFactoryMockedStatic.when( () -> ValueMetaFactory.getValueMetaName( eq( 5 ) ) ).thenReturn( "Integer" );
      ValueMetaDate valueMeta = new ValueMetaDate();
      valueMeta.setName( "date" );

      doReturn( valueMeta ).when( rowMeta ).searchValueMeta( "date" );

      memoryGroupByMeta.setSubjectField( new String[] { "date" } );
      memoryGroupByMeta.setGroupField( new String[] {} );
      memoryGroupByMeta.setAggregateField( new String[] { "minDate" } );
      memoryGroupByMeta.setAggregateType( new int[] { TYPE_GROUP_MIN } );

      memoryGroupByMeta.getFields( rowMeta, "Group by", mockInfo, mockNextStep, mockSpace, null, mockIMetaStore );

      verify( rowMeta, times( 1 ) ).clear();
      verify( rowMeta, times( 1 ) ).addRowMeta( any() );
      assertNull( rowMeta.searchValueMeta( "minDate" ).getConversionMask() );
    }
  }

  @Test
  public void getFieldsWithoutSubject() {
    try ( MockedStatic<ValueMetaFactory> valueMetaFactoryMockedStatic = mockStatic( ValueMetaFactory.class ) ) {
      valueMetaFactoryMockedStatic.when( () -> ValueMetaFactory.createValueMeta( anyInt() ) ).thenCallRealMethod();
      valueMetaFactoryMockedStatic.when( () -> ValueMetaFactory.createValueMeta( anyString(), anyInt() ) )
        .thenCallRealMethod();
      valueMetaFactoryMockedStatic.when(
          () -> ValueMetaFactory.createValueMeta( eq( "maxDate" ), eq( 3 ), eq( -1 ), eq( -1 ) ) )
        .thenReturn( new ValueMetaDate( "maxDate" ) );
      valueMetaFactoryMockedStatic.when(
          () -> ValueMetaFactory.createValueMeta( eq( "minDate" ), eq( 3 ), eq( -1 ), eq( -1 ) ) )
        .thenReturn( new ValueMetaDate( "minDate" ) );
      valueMetaFactoryMockedStatic.when(
          () -> ValueMetaFactory.createValueMeta( eq( "countDate" ), eq( 5 ), eq( -1 ), eq( -1 ) ) )
        .thenReturn( new ValueMetaDate( "countDate" ) );
      valueMetaFactoryMockedStatic.when( () -> ValueMetaFactory.getValueMetaName( eq( 3 ) ) ).thenReturn( "Date" );
      valueMetaFactoryMockedStatic.when( () -> ValueMetaFactory.getValueMetaName( eq( 5 ) ) ).thenReturn( "Integer" );
      ValueMetaDate valueMeta = new ValueMetaDate();
      valueMeta.setName( "date" );

      doReturn( valueMeta ).when( rowMeta ).searchValueMeta( "date" );

      memoryGroupByMeta.setSubjectField( new String[] { null } );
      memoryGroupByMeta.setGroupField( new String[] { "date" } );
      memoryGroupByMeta.setAggregateField( new String[] { "countDate" } );
      memoryGroupByMeta.setAggregateType( new int[] { TYPE_GROUP_COUNT_ANY } );

      memoryGroupByMeta.getFields( rowMeta, "Group by", mockInfo, mockNextStep, mockSpace, null, mockIMetaStore );

      verify( rowMeta, times( 1 ) ).clear();
      verify( rowMeta, times( 1 ) ).addRowMeta( any() );
      assertNotNull( rowMeta.searchValueMeta( "countDate" ) );
    }
  }
}
