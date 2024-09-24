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
package org.pentaho.di.trans.steps.aggregaterows;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.www.SocketRepository;

public class AggregateRowsTest {

  private StepMockHelper<AggregateRowsMeta, AggregateRowsData> stepMockHelper;

  @Before
  public void setup() {
    String[] fieldNames = new String[] {"TEST"};
    stepMockHelper = new StepMockHelper<AggregateRowsMeta, AggregateRowsData>( "TEST", AggregateRowsMeta.class, AggregateRowsData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    when( stepMockHelper.trans.getSocketRepository() ).thenReturn( mock( SocketRepository.class ) );
    when( stepMockHelper.initStepMetaInterface.getFieldName() ).thenReturn( fieldNames );
    int[] types = new int[stepMockHelper.initStepMetaInterface.getFieldName().length];
    when( stepMockHelper.initStepMetaInterface.getAggregateType() ).thenReturn( types );
  }

  @After
  public void tearDown() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testProcessRow() throws KettleException {
    AggregateRows aggregateRows =  new AggregateRows( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta, stepMockHelper.trans );
    aggregateRows.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    aggregateRows.setInputRowSets( new ArrayList<RowSet>( Arrays.asList( createSourceRowSet( "TEST" ) ) ) );

    int fieldSize = stepMockHelper.initStepMetaInterface.getFieldName().length;
    AggregateRowsData data = new AggregateRowsData();
    data.fieldnrs = new int[ fieldSize ];
    data.counts = new long[ fieldSize ];
    data.values = new Object[ fieldSize ];

    assertTrue( aggregateRows.processRow( stepMockHelper.initStepMetaInterface,  data ) );
    assertTrue( aggregateRows.getErrors() == 0 );
    assertTrue( aggregateRows.getLinesRead() > 0 );

    RowMetaInterface outputRowMeta = mock( RowMetaInterface.class );
    when( outputRowMeta.size() ).thenReturn( fieldSize );
    data.outputRowMeta = outputRowMeta;

    assertFalse( aggregateRows.processRow( stepMockHelper.initStepMetaInterface,  data ) );
    assertTrue( aggregateRows.getLinesWritten() > 0 );
  }

  private RowSet createSourceRowSet( String source ) throws KettleValueException {
    ValueMetaInterface interface1 = mock( ValueMetaInterface.class );
    when( interface1.isNull( any() ) ).thenReturn( false );

    RowMetaInterface sourceRowMeta = mock( RowMetaInterface.class );
    when( sourceRowMeta.getFieldNames() ).thenReturn( new String[] { source } );
    when( sourceRowMeta.indexOfValue( anyString() ) ).thenReturn( 0 );
    when( sourceRowMeta.getValueMeta( anyInt() ) ).thenReturn( interface1 );

    RowSet sourceRowSet = stepMockHelper.getMockInputRowSet( new String[] { source } );
    when( sourceRowSet.getRowMeta() ).thenReturn( sourceRowMeta );
    return sourceRowSet;
  }
}
