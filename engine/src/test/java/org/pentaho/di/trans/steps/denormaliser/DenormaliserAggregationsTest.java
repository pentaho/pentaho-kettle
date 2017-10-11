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

package org.pentaho.di.trans.steps.denormaliser;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class DenormaliserAggregationsTest {

  static final String JUNIT = "JUNIT";

  static StepMockHelper<DenormaliserMeta, DenormaliserData> mockHelper;
  Denormaliser step;
  DenormaliserData data = new DenormaliserData();
  DenormaliserMeta meta = new DenormaliserMeta();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    mockHelper =
        new StepMockHelper<DenormaliserMeta, DenormaliserData>( "Denormaliser", DenormaliserMeta.class,
            DenormaliserData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );
  }

  @Before
  public void setUp() throws Exception {
    Mockito.when( mockHelper.stepMeta.getStepMetaInterface() ).thenReturn( meta );
    step = new Denormaliser( mockHelper.stepMeta, data, 0, mockHelper.transMeta, mockHelper.trans );
  }

  /**
   * PDI-11597 100+null=100 , null+100=100
   *
   * @throws KettleValueException
   */
  @Test
  public void testDenormalizeSum100PlusNull() throws KettleValueException {
    // prevTargetData
    Long sto = new Long( 100 );
    data.targetResult = new Object[] { sto };

    step.deNormalise( testSumPreconditions( "SUM" ), new Object[] { JUNIT, null } );

    Assert.assertEquals( "100 + null = 100 ", sto, data.targetResult[0] );
  }

  @Test
  public void testDenormalizeSumNullPlus100() throws KettleValueException {
    // prevTargetData
    Long sto = new Long( 100 );
    data.targetResult = new Object[] { null };

    step.deNormalise( testSumPreconditions( "SUM" ), new Object[] { JUNIT, sto } );

    Assert.assertEquals( "null + 100 = 100 ", sto, data.targetResult[0] );
  }

  /**
   * PDI-9662 respect of new variable for null comparsion
   *
   * @throws KettleValueException
   */
  @Test
  public void testDenormalizeMinValueY() throws KettleValueException {
    step.setMinNullIsValued( true );

    Long trinadzat = new Long( -13 );
    data.targetResult = new Object[] { trinadzat };

    step.deNormalise( testSumPreconditions( "MIN" ), new Object[] { JUNIT, null } );

    Assert.assertNull( "Null now is new minimal", data.targetResult[0] );
  }

  /**
   * PDI-9662 respect of new variable for null comparsion
   *
   * @throws KettleValueException
   */
  @Test
  public void testDenormalizeMinValueN() throws KettleValueException {
    step.setVariable( Const.KETTLE_AGGREGATION_MIN_NULL_IS_VALUED, "N" );

    Long sto = new Long( 100 );
    data.targetResult = new Object[] { sto };

    step.deNormalise( testSumPreconditions( "MIN" ), new Object[] { JUNIT, null } );

    Assert.assertEquals( "Null is ignored", sto, data.targetResult[0] );
  }

  /**
   * This is extracted common part for sum tests
   *
   * @return
   */
  RowMetaInterface testSumPreconditions( String agg ) {

    // create rmi for one string and 2 integers
    RowMetaInterface rmi = new RowMeta();
    List<ValueMetaInterface> list = new ArrayList<ValueMetaInterface>();
    list.add( new ValueMetaString( "a" ) );
    list.add( new ValueMetaInteger( "b" ) );
    list.add( new ValueMetaInteger( "d" ) );
    rmi.setValueMetaList( list );

    // denormalizer key field will be String 'Junit'
    data.keyValue = new HashMap<String, List<Integer>>();
    List<Integer> listInt = new ArrayList<Integer>();
    listInt.add( 0 );
    data.keyValue.put( JUNIT, listInt );

    // we will calculate sum for second field ( like ["JUNIT", 1] )
    data.fieldNameIndex = new int[] { 1 };
    data.inputRowMeta = rmi;
    data.outputRowMeta = rmi;
    data.removeNrs = new int[] { -1 };

    // we do create internal instance of output field wiht sum aggregation
    DenormaliserTargetField tField = new DenormaliserTargetField();
    tField.setTargetAggregationType( agg );
    DenormaliserTargetField[] pivotField = new DenormaliserTargetField[] { tField };
    meta.setDenormaliserTargetField( pivotField );

    // return row meta interface to pass into denormalize method
    return rmi;
  }

  /**
   * PDI-9662 respect to KETTLE_AGGREGATION_ALL_NULLS_ARE_ZERO variable
   *
   * @throws KettleValueException
   */
  @Test
  public void testBuildResultWithNullsY() throws KettleValueException {
    step.setAllNullsAreZero( true );

    Object[] rowData = new Object[10];
    data.targetResult = new Object[1];
    // this removal of input rows?
    RowMetaInterface rmi = testSumPreconditions( "-" );
    data.removeNrs = new int[]{ 0 };
    Object[] outputRowData = step.buildResult( rmi, rowData );

    Assert.assertEquals( "Output row: nulls are zeros", new Long( 0 ), outputRowData[2] );
  }

  @Test
  public void testBuildResultWithNullsN() throws KettleValueException {
    step.setAllNullsAreZero( false );

    Object[] rowData = new Object[10];
    data.targetResult = new Object[1];
    Object[] outputRowData = step.buildResult( testSumPreconditions( "-" ), rowData );

    Assert.assertNull( "Output row: nulls are nulls", outputRowData[3] );
  }

  /**
   * PDI-16017. Method newGroup should not initialize result by default for MIN
   * (in addition PDI-16015 without converting)
   *
   * @throws Exception
   */
  @Test
  public void testNewGroup() throws Exception {
    DenormaliserTargetField field1 = new DenormaliserTargetField();
    field1.setTargetAggregationType( "MIN" );

    DenormaliserTargetField field2 = new DenormaliserTargetField();
    field2.setTargetAggregationType( "MIN" );

    DenormaliserTargetField field3 = new DenormaliserTargetField();
    field3.setTargetAggregationType( "MIN" );

    DenormaliserTargetField[] pivotField = new DenormaliserTargetField[] { field1, field2, field3 };
    meta.setDenormaliserTargetField( pivotField );
    data.counters = new long[3];
    data.sum = new Object[3];

    Method newGroupMethod = step.getClass().getDeclaredMethod( "newGroup" );
    newGroupMethod.setAccessible( true );
    newGroupMethod.invoke( step );

    Assert.assertEquals( 3, data.targetResult.length );

    for ( Object result : data.targetResult ) {
      Assert.assertNull( result );
    }
  }

}
