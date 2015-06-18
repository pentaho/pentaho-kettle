/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.calculator;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for calculator step
 *
 * @author Pavel Sakun
 * @see Calculator
 */
public class CalculatorUnitTest {
  private StepMockHelper<CalculatorMeta, CalculatorData> smh;

  @BeforeClass
  public static void init() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Before
  public void setUp() {
    smh =
      new StepMockHelper<CalculatorMeta, CalculatorData>( "Calculator", CalculatorMeta.class,
        CalculatorData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      smh.logChannelInterface );
    when( smh.trans.isRunning() ).thenReturn( true );
  }

  @Test
  public void testReturnDigitsOnly() throws KettleException {
    RowMeta inputRowMeta = new RowMeta();
    ValueMetaString nameMeta = new ValueMetaString( "Name" );
    inputRowMeta.addValueMeta( nameMeta );
    ValueMetaString valueMeta = new ValueMetaString( "Value" );
    inputRowMeta.addValueMeta( valueMeta );

    RowSet inputRowSet = smh.getMockInputRowSet( new Object[][] { { "name1", "qwe123asd456zxc" }, { "name2", null } } );
    inputRowSet.setRowMeta( inputRowMeta );

    Calculator calculator = new Calculator( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    calculator.getInputRowSets().add( inputRowSet );
    calculator.setInputRowMeta( inputRowMeta );
    calculator.init( smh.initStepMetaInterface, smh.initStepDataInterface );

    CalculatorMeta meta = new CalculatorMeta();
    meta.setCalculation( new CalculatorMetaFunction[] {
      new CalculatorMetaFunction( "digits", CalculatorMetaFunction.CALC_GET_ONLY_DIGITS, "Value", null, null,
        ValueMetaInterface.TYPE_STRING, 0, 0, false, "", "", "", "" ) } );

    // Verify output
    try {
      calculator.addRowListener( new RowAdapter() {
        @Override public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
          assertEquals( "123456", row[ 2 ] );
        }
      } );
      calculator.processRow( meta, new CalculatorData() );
    } catch ( KettleException ke ) {
      ke.printStackTrace();
      fail();
    }
  }

  @Test
  public void calculatorShouldClearDataInstance() throws Exception {
    RowMeta inputRowMeta = new RowMeta();
    ValueMetaInteger valueMeta = new ValueMetaInteger( "Value" );
    inputRowMeta.addValueMeta( valueMeta );

    RowSet inputRowSet = smh.getMockInputRowSet( new Object[] { -1L } );
    inputRowSet.setRowMeta( inputRowMeta );

    Calculator calculator = new Calculator( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    calculator.getInputRowSets().add( inputRowSet );
    calculator.setInputRowMeta( inputRowMeta );
    calculator.init( smh.initStepMetaInterface, smh.initStepDataInterface );

    CalculatorMeta meta = new CalculatorMeta();
    meta.setCalculation( new CalculatorMetaFunction[] {
      new CalculatorMetaFunction( "test", CalculatorMetaFunction.CALC_ABS, "Value", null, null,
        ValueMetaInterface.TYPE_STRING, 0, 0, false, "", "", "", "" ) } );

    CalculatorData data = new CalculatorData();
    data = spy( data );

    calculator.processRow( meta, data );
    verify( data ).getValueMetaFor( eq( valueMeta.getType() ), anyString() );

    calculator.processRow( meta, data );
    verify( data ).clearValuesMetaMapping();
  }
}
