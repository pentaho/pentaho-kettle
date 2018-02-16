/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

/**
 * Unit tests for calculator step
 *
 * @author Pavel Sakun
 * @see Calculator
 */
public class CalculatorBackwardCompatibilityUnitTest {
  private StepMockHelper<CalculatorMeta, CalculatorData> smh;

  private static final String SYS_PROPERTY_ROUND_2_MODE = "ROUND_2_MODE";
  private static final int OBSOLETE_ROUND_2_MODE = BigDecimal.ROUND_HALF_EVEN;
  private static final int DEFAULT_ROUND_2_MODE = Const.ROUND_HALF_CEILING;

  /**
   * Get value of private static field ValueDataUtil.ROUND_2_MODE.
   *
   * @return
   */
  private static int getRound2Mode() {
    int value = -1;
    try {
      Class<ValueDataUtil> cls = ValueDataUtil.class;
      Field f = cls.getDeclaredField( SYS_PROPERTY_ROUND_2_MODE );
      f.setAccessible( true );
      value = (Integer) f.get( null );
      f.setAccessible( false );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
    return value;
  }

  /**
   * Set new value of value of private static field ValueDataUtil.ROUND_2_MODE.
   *
   * @param newValue
   */
  private static void setRound2Mode( int newValue ) {
    try {
      Class<ValueDataUtil> cls = ValueDataUtil.class;
      Field f = cls.getDeclaredField( SYS_PROPERTY_ROUND_2_MODE );
      f.setAccessible( true );
      f.set( null, newValue );
      f.setAccessible( false );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  @BeforeClass
  public static void init() throws KettleException {
    assertEquals( DEFAULT_ROUND_2_MODE, getRound2Mode() );
    setRound2Mode( OBSOLETE_ROUND_2_MODE );
    assertEquals( OBSOLETE_ROUND_2_MODE, getRound2Mode() );

    KettleEnvironment.init( false );
  }

  @AfterClass
  public static void restore() throws Exception {
    setRound2Mode( DEFAULT_ROUND_2_MODE );
    assertEquals( DEFAULT_ROUND_2_MODE, getRound2Mode() );
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

    RowSet inputRowSet = smh.getMockInputRowSet( new Object[] { value } );
    inputRowSet.setRowMeta( inputRowMeta );

    Calculator calculator = new Calculator( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    calculator.addRowSetToInputRowSets( inputRowSet );
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
    assertRound2( 2.0, 2.5, 0 );
    assertRound2( 3.0, 2.7, 0 );
    assertRound2( -1.0, -1.2, 0 );
    assertRound2( -2.0, -1.5, 0 );
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
    calculator.addRowSetToInputRowSets( inputRowSet );
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
    calculator.addRowSetToInputRowSets( inputRowSet );
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
    calculator.addRowSetToInputRowSets( inputRowSet );
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

}
