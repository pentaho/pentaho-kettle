/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

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
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

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

  @Test
  public void testRound() throws KettleException {
    assertRound( 1.0, 1.2 );
    assertRound( 2.0, 1.5 );
    assertRound( 2.0, 1.7 );
    assertRound( 2.0, 2.2 );
    assertRound( 3.0, 2.5 );
    assertRound( 3.0, 2.7 );
    assertRound( -1.0, -1.2 );
    assertRound( -1.0, -1.5 );
    assertRound( -2.0, -1.7 );
    assertRound( -2.0, -2.2 );
    assertRound( -2.0, -2.5 );
    assertRound( -3.0, -2.7 );
  }

  public void assertRound( final double expectedResult, final double value ) throws KettleException {
    RowMeta inputRowMeta = new RowMeta();
    ValueMetaNumber valueMeta = new ValueMetaNumber( "Value" );
    inputRowMeta.addValueMeta( valueMeta );

    ;
    RowSet inputRowSet = smh.getMockInputRowSet( new Object[] { value } );
    inputRowSet.setRowMeta( inputRowMeta );

    Calculator calculator = new Calculator( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    calculator.getInputRowSets().add( inputRowSet );
    calculator.setInputRowMeta( inputRowMeta );
    calculator.init( smh.initStepMetaInterface, smh.initStepDataInterface );

    CalculatorMeta meta = new CalculatorMeta();
    meta.setCalculation( new CalculatorMetaFunction[] { new CalculatorMetaFunction( "test",
        CalculatorMetaFunction.CALC_ROUND_1, "Value", null, null, ValueMetaInterface.TYPE_NUMBER, 2, 0, false, "", "",
        "", "" ) } );

    // Verify output
    try {
      calculator.addRowListener( new RowAdapter() {
        @Override
        public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
          assertEquals( expectedResult, row[1] );
        }
      } );
      calculator.processRow( meta, new CalculatorData() );
    } catch ( KettleException ke ) {
      ke.printStackTrace();
      fail();
    }

  }

  @Test
  public void testRound2() throws KettleException {
    assertRound2( 1.0, 1.2, 0 );
    assertRound2( 2.0, 1.5, 0 );
    assertRound2( 2.0, 1.7, 0 );
    assertRound2( 2.0, 2.2, 0 );
    assertRound2( 3.0, 2.5, 0 );
    assertRound2( 3.0, 2.7, 0 );
    assertRound2( -1.0, -1.2, 0 );
    assertRound2( -1.0, -1.5, 0 );
    assertRound2( -2.0, -1.7, 0 );
    assertRound2( -2.0, -2.2, 0 );
    assertRound2( -2.0, -2.5, 0 );
    assertRound2( -3.0, -2.7, 0 );
  }

  public void assertRound2( final double expectedResult, final double value, final long precision )
    throws KettleException {
    RowMeta inputRowMeta = new RowMeta();
    ValueMetaNumber valueMeta = new ValueMetaNumber( "Value" );
    ValueMetaInteger precisionMeta = new ValueMetaInteger( "Precision" );
    inputRowMeta.addValueMeta( valueMeta );
    inputRowMeta.addValueMeta( precisionMeta );

    RowSet inputRowSet = smh.getMockInputRowSet( new Object[] { value, precision } );
    inputRowSet.setRowMeta( inputRowMeta );

    Calculator calculator = new Calculator( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    calculator.getInputRowSets().add( inputRowSet );
    calculator.setInputRowMeta( inputRowMeta );
    calculator.init( smh.initStepMetaInterface, smh.initStepDataInterface );

    CalculatorMeta meta = new CalculatorMeta();
    meta.setCalculation( new CalculatorMetaFunction[] { new CalculatorMetaFunction( "test",
        CalculatorMetaFunction.CALC_ROUND_2, "Value", "Precision", null, ValueMetaInterface.TYPE_NUMBER, 2, 0, false,
        "", "", "", "" ) } );

    // Verify output
    try {
      calculator.addRowListener( new RowAdapter() {
        @Override
        public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
          assertEquals( expectedResult, row[2] );
        }
      } );
      calculator.processRow( meta, new CalculatorData() );
    } catch ( KettleException ke ) {
      ke.printStackTrace();
      fail();
    }

  }

  @Test
  public void testRoundStd() throws KettleException {
    assertRoundStd( 1.0, 1.2 );
    assertRoundStd( 2.0, 1.5 );
    assertRoundStd( 2.0, 1.7 );
    assertRoundStd( 2.0, 2.2 );
    assertRoundStd( 3.0, 2.5 );
    assertRoundStd( 3.0, 2.7 );
    assertRoundStd( -1.0, -1.2 );
    assertRoundStd( -2.0, -1.5 );
    assertRoundStd( -2.0, -1.7 );
    assertRoundStd( -2.0, -2.2 );
    assertRoundStd( -3.0, -2.5 );
    assertRoundStd( -3.0, -2.7 );
  }

  public void assertRoundStd( final double expectedResult, final double value ) throws KettleException {
    RowMeta inputRowMeta = new RowMeta();
    ValueMetaNumber valueMeta = new ValueMetaNumber( "Value" );
    inputRowMeta.addValueMeta( valueMeta );

    RowSet inputRowSet = smh.getMockInputRowSet( new Object[] { value } );
    inputRowSet.setRowMeta( inputRowMeta );

    Calculator calculator = new Calculator( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    calculator.getInputRowSets().add( inputRowSet );
    calculator.setInputRowMeta( inputRowMeta );
    calculator.init( smh.initStepMetaInterface, smh.initStepDataInterface );

    CalculatorMeta meta = new CalculatorMeta();
    meta.setCalculation( new CalculatorMetaFunction[] { new CalculatorMetaFunction( "test",
        CalculatorMetaFunction.CALC_ROUND_STD_1, "Value", null, null, ValueMetaInterface.TYPE_NUMBER, 2, 0, false, "",
        "", "", "" ) } );

    // Verify output
    try {
      calculator.addRowListener( new RowAdapter() {
        @Override
        public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
          assertEquals( expectedResult, row[1] );
        }
      } );
      calculator.processRow( meta, new CalculatorData() );
    } catch ( KettleException ke ) {
      ke.printStackTrace();
      fail();
    }

  }

  @Test
  public void testRoundStd2() throws KettleException {
    assertRoundStd2( 1.0, 1.2, 0 );
    assertRoundStd2( 2.0, 1.5, 0 );
    assertRoundStd2( 2.0, 1.7, 0 );
    assertRoundStd2( 2.0, 2.2, 0 );
    assertRoundStd2( 3.0, 2.5, 0 );
    assertRoundStd2( 3.0, 2.7, 0 );
    assertRoundStd2( -1.0, -1.2, 0 );
    assertRoundStd2( -2.0, -1.5, 0 );
    assertRoundStd2( -2.0, -1.7, 0 );
    assertRoundStd2( -2.0, -2.2, 0 );
    assertRoundStd2( -3.0, -2.5, 0 );
    assertRoundStd2( -3.0, -2.7, 0 );
  }

  public void assertRoundStd2( final double expectedResult, final double value, final long precision )
    throws KettleException {
    RowMeta inputRowMeta = new RowMeta();
    ValueMetaNumber valueMeta = new ValueMetaNumber( "Value" );
    ValueMetaInteger precisionMeta = new ValueMetaInteger( "Precision" );
    inputRowMeta.addValueMeta( valueMeta );
    inputRowMeta.addValueMeta( precisionMeta );

    RowSet inputRowSet = smh.getMockInputRowSet( new Object[] { value, precision } );
    inputRowSet.setRowMeta( inputRowMeta );

    Calculator calculator = new Calculator( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    calculator.getInputRowSets().add( inputRowSet );
    calculator.setInputRowMeta( inputRowMeta );
    calculator.init( smh.initStepMetaInterface, smh.initStepDataInterface );

    CalculatorMeta meta = new CalculatorMeta();
    meta.setCalculation( new CalculatorMetaFunction[] { new CalculatorMetaFunction( "test",
        CalculatorMetaFunction.CALC_ROUND_STD_2, "Value", "Precision", null, ValueMetaInterface.TYPE_NUMBER, 2, 0,
        false, "", "", "", "" ) } );

    // Verify output
    try {
      calculator.addRowListener( new RowAdapter() {
        @Override
        public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
          assertEquals( expectedResult, row[2] );
        }
      } );
      calculator.processRow( meta, new CalculatorData() );
    } catch ( KettleException ke ) {
      ke.printStackTrace();
      fail();
    }

  }

  @Test
  public void testRoundCustom1() throws KettleException {
    assertRoundCustom1( 2.0, 1.2, BigDecimal.ROUND_UP );
    assertRoundCustom1( 1.0, 1.2, BigDecimal.ROUND_DOWN );
    assertRoundCustom1( 2.0, 1.2, BigDecimal.ROUND_CEILING );
    assertRoundCustom1( 1.0, 1.2, BigDecimal.ROUND_FLOOR );
    assertRoundCustom1( 1.0, 1.2, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom1( 1.0, 1.2, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom1( 1.0, 1.2, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom1( 2.0, 1.5, BigDecimal.ROUND_UP );
    assertRoundCustom1( 1.0, 1.5, BigDecimal.ROUND_DOWN );
    assertRoundCustom1( 2.0, 1.5, BigDecimal.ROUND_CEILING );
    assertRoundCustom1( 1.0, 1.5, BigDecimal.ROUND_FLOOR );
    assertRoundCustom1( 2.0, 1.5, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom1( 1.0, 1.5, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom1( 2.0, 1.5, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom1( 2.0, 1.7, BigDecimal.ROUND_UP );
    assertRoundCustom1( 1.0, 1.7, BigDecimal.ROUND_DOWN );
    assertRoundCustom1( 2.0, 1.7, BigDecimal.ROUND_CEILING );
    assertRoundCustom1( 1.0, 1.7, BigDecimal.ROUND_FLOOR );
    assertRoundCustom1( 2.0, 1.7, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom1( 2.0, 1.7, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom1( 2.0, 1.7, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom1( 3.0, 2.2, BigDecimal.ROUND_UP );
    assertRoundCustom1( 2.0, 2.2, BigDecimal.ROUND_DOWN );
    assertRoundCustom1( 3.0, 2.2, BigDecimal.ROUND_CEILING );
    assertRoundCustom1( 2.0, 2.2, BigDecimal.ROUND_FLOOR );
    assertRoundCustom1( 2.0, 2.2, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom1( 2.0, 2.2, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom1( 2.0, 2.2, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom1( 3.0, 2.5, BigDecimal.ROUND_UP );
    assertRoundCustom1( 2.0, 2.5, BigDecimal.ROUND_DOWN );
    assertRoundCustom1( 3.0, 2.5, BigDecimal.ROUND_CEILING );
    assertRoundCustom1( 2.0, 2.5, BigDecimal.ROUND_FLOOR );
    assertRoundCustom1( 3.0, 2.5, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom1( 2.0, 2.5, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom1( 2.0, 2.5, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom1( 3.0, 2.7, BigDecimal.ROUND_UP );
    assertRoundCustom1( 2.0, 2.7, BigDecimal.ROUND_DOWN );
    assertRoundCustom1( 3.0, 2.7, BigDecimal.ROUND_CEILING );
    assertRoundCustom1( 2.0, 2.7, BigDecimal.ROUND_FLOOR );
    assertRoundCustom1( 3.0, 2.7, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom1( 3.0, 2.7, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom1( 3.0, 2.7, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom1( -2.0, -1.2, BigDecimal.ROUND_UP );
    assertRoundCustom1( -1.0, -1.2, BigDecimal.ROUND_DOWN );
    assertRoundCustom1( -1.0, -1.2, BigDecimal.ROUND_CEILING );
    assertRoundCustom1( -2.0, -1.2, BigDecimal.ROUND_FLOOR );
    assertRoundCustom1( -1.0, -1.2, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom1( -1.0, -1.2, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom1( -1.0, -1.2, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom1( -2.0, -1.5, BigDecimal.ROUND_UP );
    assertRoundCustom1( -1.0, -1.5, BigDecimal.ROUND_DOWN );
    assertRoundCustom1( -1.0, -1.5, BigDecimal.ROUND_CEILING );
    assertRoundCustom1( -2.0, -1.5, BigDecimal.ROUND_FLOOR );
    assertRoundCustom1( -2.0, -1.5, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom1( -1.0, -1.5, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom1( -2.0, -1.5, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom1( -2.0, -1.7, BigDecimal.ROUND_UP );
    assertRoundCustom1( -1.0, -1.7, BigDecimal.ROUND_DOWN );
    assertRoundCustom1( -1.0, -1.7, BigDecimal.ROUND_CEILING );
    assertRoundCustom1( -2.0, -1.7, BigDecimal.ROUND_FLOOR );
    assertRoundCustom1( -2.0, -1.7, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom1( -2.0, -1.7, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom1( -2.0, -1.7, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom1( -3.0, -2.2, BigDecimal.ROUND_UP );
    assertRoundCustom1( -2.0, -2.2, BigDecimal.ROUND_DOWN );
    assertRoundCustom1( -2.0, -2.2, BigDecimal.ROUND_CEILING );
    assertRoundCustom1( -3.0, -2.2, BigDecimal.ROUND_FLOOR );
    assertRoundCustom1( -2.0, -2.2, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom1( -2.0, -2.2, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom1( -2.0, -2.2, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom1( -3.0, -2.5, BigDecimal.ROUND_UP );
    assertRoundCustom1( -2.0, -2.5, BigDecimal.ROUND_DOWN );
    assertRoundCustom1( -2.0, -2.5, BigDecimal.ROUND_CEILING );
    assertRoundCustom1( -3.0, -2.5, BigDecimal.ROUND_FLOOR );
    assertRoundCustom1( -3.0, -2.5, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom1( -2.0, -2.5, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom1( -2.0, -2.5, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom1( -3.0, -2.7, BigDecimal.ROUND_UP );
    assertRoundCustom1( -2.0, -2.7, BigDecimal.ROUND_DOWN );
    assertRoundCustom1( -2.0, -2.7, BigDecimal.ROUND_CEILING );
    assertRoundCustom1( -3.0, -2.7, BigDecimal.ROUND_FLOOR );
    assertRoundCustom1( -3.0, -2.7, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom1( -3.0, -2.7, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom1( -3.0, -2.7, BigDecimal.ROUND_HALF_EVEN );
  }

  public void assertRoundCustom1( final double expectedResult, final double value, final long roundingMode )
    throws KettleException {
    RowMeta inputRowMeta = new RowMeta();
    ValueMetaNumber valueMeta = new ValueMetaNumber( "Value" );
    ValueMetaInteger roundingModeMeta = new ValueMetaInteger( "RoundingMode" );
    inputRowMeta.addValueMeta( valueMeta );
    inputRowMeta.addValueMeta( roundingModeMeta );

    RowSet inputRowSet = smh.getMockInputRowSet( new Object[] { value, roundingMode } );
    inputRowSet.setRowMeta( inputRowMeta );

    Calculator calculator = new Calculator( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    calculator.getInputRowSets().add( inputRowSet );
    calculator.setInputRowMeta( inputRowMeta );
    calculator.init( smh.initStepMetaInterface, smh.initStepDataInterface );

    CalculatorMeta meta = new CalculatorMeta();
    meta.setCalculation( new CalculatorMetaFunction[] { new CalculatorMetaFunction( "test",
        CalculatorMetaFunction.CALC_ROUND_CUSTOM_1, "Value", "RoundingMode", null, ValueMetaInterface.TYPE_NUMBER, 2,
        0, false, "", "", "", "" ) } );

    // Verify output
    try {
      calculator.addRowListener( new RowAdapter() {
        @Override
        public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
          assertEquals( expectedResult, row[2] );
        }
      } );
      calculator.processRow( meta, new CalculatorData() );
    } catch ( KettleException ke ) {
      ke.printStackTrace();
      fail();
    }

  }

  @Test
  public void testRoundCustom2() throws KettleException {
    assertRoundCustom2( 2.0, 1.2, 0, BigDecimal.ROUND_UP );
    assertRoundCustom2( 1.0, 1.2, 0, BigDecimal.ROUND_DOWN );
    assertRoundCustom2( 2.0, 1.2, 0, BigDecimal.ROUND_CEILING );
    assertRoundCustom2( 1.0, 1.2, 0, BigDecimal.ROUND_FLOOR );
    assertRoundCustom2( 1.0, 1.2, 0, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom2( 1.0, 1.2, 0, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom2( 1.0, 1.2, 0, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom2( 2.0, 1.5, 0, BigDecimal.ROUND_UP );
    assertRoundCustom2( 1.0, 1.5, 0, BigDecimal.ROUND_DOWN );
    assertRoundCustom2( 2.0, 1.5, 0, BigDecimal.ROUND_CEILING );
    assertRoundCustom2( 1.0, 1.5, 0, BigDecimal.ROUND_FLOOR );
    assertRoundCustom2( 2.0, 1.5, 0, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom2( 1.0, 1.5, 0, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom2( 2.0, 1.5, 0, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom2( 2.0, 1.7, 0, BigDecimal.ROUND_UP );
    assertRoundCustom2( 1.0, 1.7, 0, BigDecimal.ROUND_DOWN );
    assertRoundCustom2( 2.0, 1.7, 0, BigDecimal.ROUND_CEILING );
    assertRoundCustom2( 1.0, 1.7, 0, BigDecimal.ROUND_FLOOR );
    assertRoundCustom2( 2.0, 1.7, 0, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom2( 2.0, 1.7, 0, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom2( 2.0, 1.7, 0, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom2( 3.0, 2.2, 0, BigDecimal.ROUND_UP );
    assertRoundCustom2( 2.0, 2.2, 0, BigDecimal.ROUND_DOWN );
    assertRoundCustom2( 3.0, 2.2, 0, BigDecimal.ROUND_CEILING );
    assertRoundCustom2( 2.0, 2.2, 0, BigDecimal.ROUND_FLOOR );
    assertRoundCustom2( 2.0, 2.2, 0, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom2( 2.0, 2.2, 0, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom2( 2.0, 2.2, 0, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom2( 3.0, 2.5, 0, BigDecimal.ROUND_UP );
    assertRoundCustom2( 2.0, 2.5, 0, BigDecimal.ROUND_DOWN );
    assertRoundCustom2( 3.0, 2.5, 0, BigDecimal.ROUND_CEILING );
    assertRoundCustom2( 2.0, 2.5, 0, BigDecimal.ROUND_FLOOR );
    assertRoundCustom2( 3.0, 2.5, 0, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom2( 2.0, 2.5, 0, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom2( 2.0, 2.5, 0, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom2( 3.0, 2.7, 0, BigDecimal.ROUND_UP );
    assertRoundCustom2( 2.0, 2.7, 0, BigDecimal.ROUND_DOWN );
    assertRoundCustom2( 3.0, 2.7, 0, BigDecimal.ROUND_CEILING );
    assertRoundCustom2( 2.0, 2.7, 0, BigDecimal.ROUND_FLOOR );
    assertRoundCustom2( 3.0, 2.7, 0, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom2( 3.0, 2.7, 0, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom2( 3.0, 2.7, 0, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom2( -2.0, -1.2, 0, BigDecimal.ROUND_UP );
    assertRoundCustom2( -1.0, -1.2, 0, BigDecimal.ROUND_DOWN );
    assertRoundCustom2( -1.0, -1.2, 0, BigDecimal.ROUND_CEILING );
    assertRoundCustom2( -2.0, -1.2, 0, BigDecimal.ROUND_FLOOR );
    assertRoundCustom2( -1.0, -1.2, 0, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom2( -1.0, -1.2, 0, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom2( -1.0, -1.2, 0, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom2( -2.0, -1.5, 0, BigDecimal.ROUND_UP );
    assertRoundCustom2( -1.0, -1.5, 0, BigDecimal.ROUND_DOWN );
    assertRoundCustom2( -1.0, -1.5, 0, BigDecimal.ROUND_CEILING );
    assertRoundCustom2( -2.0, -1.5, 0, BigDecimal.ROUND_FLOOR );
    assertRoundCustom2( -2.0, -1.5, 0, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom2( -1.0, -1.5, 0, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom2( -2.0, -1.5, 0, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom2( -2.0, -1.7, 0, BigDecimal.ROUND_UP );
    assertRoundCustom2( -1.0, -1.7, 0, BigDecimal.ROUND_DOWN );
    assertRoundCustom2( -1.0, -1.7, 0, BigDecimal.ROUND_CEILING );
    assertRoundCustom2( -2.0, -1.7, 0, BigDecimal.ROUND_FLOOR );
    assertRoundCustom2( -2.0, -1.7, 0, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom2( -2.0, -1.7, 0, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom2( -2.0, -1.7, 0, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom2( -3.0, -2.2, 0, BigDecimal.ROUND_UP );
    assertRoundCustom2( -2.0, -2.2, 0, BigDecimal.ROUND_DOWN );
    assertRoundCustom2( -2.0, -2.2, 0, BigDecimal.ROUND_CEILING );
    assertRoundCustom2( -3.0, -2.2, 0, BigDecimal.ROUND_FLOOR );
    assertRoundCustom2( -2.0, -2.2, 0, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom2( -2.0, -2.2, 0, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom2( -2.0, -2.2, 0, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom2( -3.0, -2.5, 0, BigDecimal.ROUND_UP );
    assertRoundCustom2( -2.0, -2.5, 0, BigDecimal.ROUND_DOWN );
    assertRoundCustom2( -2.0, -2.5, 0, BigDecimal.ROUND_CEILING );
    assertRoundCustom2( -3.0, -2.5, 0, BigDecimal.ROUND_FLOOR );
    assertRoundCustom2( -3.0, -2.5, 0, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom2( -2.0, -2.5, 0, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom2( -2.0, -2.5, 0, BigDecimal.ROUND_HALF_EVEN );

    assertRoundCustom2( -3.0, -2.7, 0, BigDecimal.ROUND_UP );
    assertRoundCustom2( -2.0, -2.7, 0, BigDecimal.ROUND_DOWN );
    assertRoundCustom2( -2.0, -2.7, 0, BigDecimal.ROUND_CEILING );
    assertRoundCustom2( -3.0, -2.7, 0, BigDecimal.ROUND_FLOOR );
    assertRoundCustom2( -3.0, -2.7, 0, BigDecimal.ROUND_HALF_UP );
    assertRoundCustom2( -3.0, -2.7, 0, BigDecimal.ROUND_HALF_DOWN );
    assertRoundCustom2( -3.0, -2.7, 0, BigDecimal.ROUND_HALF_EVEN );
  }

  public void assertRoundCustom2( final double expectedResult, final double value, final long precision,
      final long roundingMode ) throws KettleException {
    RowMeta inputRowMeta = new RowMeta();
    ValueMetaNumber valueMeta = new ValueMetaNumber( "Value" );
    ValueMetaInteger precisionMeta = new ValueMetaInteger( "Precision" );
    ValueMetaInteger roundingModeMeta = new ValueMetaInteger( "RoundingMode" );
    inputRowMeta.addValueMeta( valueMeta );
    inputRowMeta.addValueMeta( precisionMeta );
    inputRowMeta.addValueMeta( roundingModeMeta );

    RowSet inputRowSet = smh.getMockInputRowSet( new Object[] { value, precision, roundingMode } );
    inputRowSet.setRowMeta( inputRowMeta );

    Calculator calculator = new Calculator( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    calculator.getInputRowSets().add( inputRowSet );
    calculator.setInputRowMeta( inputRowMeta );
    calculator.init( smh.initStepMetaInterface, smh.initStepDataInterface );

    CalculatorMeta meta = new CalculatorMeta();
    meta.setCalculation( new CalculatorMetaFunction[] { new CalculatorMetaFunction( "test",
        CalculatorMetaFunction.CALC_ROUND_CUSTOM_2, "Value", "Precision", "RoundingMode",
        ValueMetaInterface.TYPE_NUMBER, 2, 0, false, "", "", "", "" ) } );

    // Verify output
    try {
      calculator.addRowListener( new RowAdapter() {
        @Override
        public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
          assertEquals( expectedResult, row[3] );
        }
      } );
      calculator.processRow( meta, new CalculatorData() );
    } catch ( KettleException ke ) {
      ke.printStackTrace();
      fail();
    }
  }

}
