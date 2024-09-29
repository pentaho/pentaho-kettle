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
package org.pentaho.di.trans.steps.denormaliser;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class DenormalizerNullIfTest {
  private static final String KEY_VALUE = "keyValue";
  private static final String DATE_FORMAT = "MM/dd/yyyy";
  static StepMockHelper<DenormaliserMeta, DenormaliserData> mockHelper;
  Denormaliser step;
  DenormaliserData data = new DenormaliserData();
  DenormaliserMeta meta = new DenormaliserMeta();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    mockHelper =
      new StepMockHelper<>( "Denormaliser", DenormaliserMeta.class,
        DenormaliserData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );
  }

  @AfterClass
  public static void cleanUp() {
    mockHelper.cleanUp();
  }

  @Before
  public void setUp() throws Exception {
    Mockito.when( mockHelper.stepMeta.getStepMetaInterface() ).thenReturn( meta );
    step = new Denormaliser( mockHelper.stepMeta, data, 0, mockHelper.transMeta, mockHelper.trans );
  }

  @Test
  public void testNullIfStringPositive() throws KettleValueException {
    step.deNormalise( getRmi( "AAA", new ValueMetaString( "target" ), new ValueMetaString( "value" ) ),
      new Object[] { KEY_VALUE, "target1", "AAA" } );
    Assert.assertNull( data.targetResult[ 0 ] );
  }

  @Test
  public void testNullIfStringNegative() throws KettleValueException {
    step.deNormalise( getRmi( "AAA", new ValueMetaString( "target" ), new ValueMetaString( "value" ) ),
      new Object[] { KEY_VALUE, "target1", "XXX" } );
    Assert.assertEquals( "XXX", data.targetResult[ 0 ] );
  }

  @Test
  public void testNullIfBooleanPositive() throws KettleValueException {
    step.deNormalise( getRmi( "true", new ValueMetaBoolean( "target", 5, 0 ), new ValueMetaBoolean( "value", 5, 0 ) ),
      new Object[] { KEY_VALUE, "target1", true } );
    Assert.assertNull( data.targetResult[ 0 ] );
  }

  @Test
  public void testNullIfBooleanNegative() throws KettleValueException {
    step.deNormalise( getRmi( "true", new ValueMetaBoolean( "target", 5, 0 ), new ValueMetaBoolean( "value", 5, 0 ) ),
      new Object[] { KEY_VALUE, "target1", false } );
    Assert.assertEquals( false, data.targetResult[ 0 ] );
  }

  @Test
  public void testNullIfBooleanAltPositive() throws KettleValueException {
    step.deNormalise( getRmi( "y", new ValueMetaBoolean( "target", 5, 0 ), new ValueMetaBoolean( "value", 1, 0 ) ),
      new Object[] { KEY_VALUE, "target1", true } );
    Assert.assertNull( data.targetResult[ 0 ] );
  }

  @Test
  public void testNullIfBooleanAltNegative() throws KettleValueException {
    step.deNormalise( getRmi( "y", new ValueMetaBoolean( "target", 5, 0 ), new ValueMetaBoolean( "value", 1, 0 ) ),
      new Object[] { KEY_VALUE, "target1", false } );
    Assert.assertEquals( false, data.targetResult[ 0 ] );
  }

  @Test
  public void testNullIfNumberPositive() throws KettleValueException {
    step.deNormalise( getRmi( "1111.22", new ValueMetaNumber( "target", 6, 2 ), new ValueMetaNumber( "value", 6, 2 ) ),
      new Object[] { KEY_VALUE, "target1", 1111.22 } );
    Assert.assertNull( data.targetResult[ 0 ] );
  }

  @Test
  public void testNullIfNumberNegative() throws KettleValueException {
    step.deNormalise( getRmi( "1111.22", new ValueMetaNumber( "target", 6, 2 ), new ValueMetaNumber( "value", 6, 2 ) ),
      new Object[] { KEY_VALUE, "target1", 3333.22 } );
    Assert.assertEquals( 3333.22, data.targetResult[ 0 ] );
  }

  @Test
  public void testNullIfDatePositive() throws Exception {
    ValueMetaInterface vmiTarget = new ValueMetaDate( "target" );
    vmiTarget.setConversionMask( DATE_FORMAT );
    ValueMetaInterface vmiValue = new ValueMetaDate( "value" );
    vmiValue.setConversionMask( DATE_FORMAT );
    SimpleDateFormat fmt = new SimpleDateFormat( DATE_FORMAT );
    Date date = fmt.parse( "01/01/2012" );
    step.deNormalise( getRmi( "01/01/2012", vmiTarget, vmiValue ), new Object[] { KEY_VALUE, "target1", date } );
    Assert.assertNull( data.targetResult[ 0 ] );
  }

  @Test
  public void testNullIfDateNegative() throws Exception {
    ValueMetaInterface vmiTarget = new ValueMetaDate( "target" );
    vmiTarget.setConversionMask( DATE_FORMAT );
    ValueMetaInterface vmiValue = new ValueMetaDate( "value" );
    vmiValue.setConversionMask( DATE_FORMAT );
    SimpleDateFormat fmt = new SimpleDateFormat( DATE_FORMAT );
    Date date = fmt.parse( "03/01/2012" );
    step.deNormalise( getRmi( "01/01/2012", vmiTarget, vmiValue ), new Object[] { KEY_VALUE, "target1", date } );
    Assert.assertEquals( date, data.targetResult[ 0 ] );
  }

  @Test
  public void testNullIfIntegerPositive() throws KettleValueException {
    step.deNormalise( getRmi( "111", new ValueMetaInteger( "target" ), new ValueMetaInteger( "value" ) ),
      new Object[] { KEY_VALUE, "target1", 111L } );
    Assert.assertNull( data.targetResult[ 0 ] );
  }

  @Test
  public void testNullIfIntegerNegative() throws KettleValueException {
    step.deNormalise( getRmi( "111", new ValueMetaInteger( "target" ), new ValueMetaInteger( "value" ) ),
      new Object[] { KEY_VALUE, "target1", 333L } );
    Assert.assertEquals( 333L, data.targetResult[ 0 ] );
  }

  @Test
  public void testNullIfBigNumberPositive() throws KettleValueException {
    step.deNormalise( getRmi( "12345.6789", new ValueMetaBigNumber( "target" ), new ValueMetaBigNumber( "value" ) ),
      new Object[] { KEY_VALUE, "target1", new BigDecimal( "12345.6789" ) } );
    Assert.assertNull( data.targetResult[ 0 ] );
  }

  @Test
  public void testNullIfBigNumberNegative() throws KettleValueException {
    step.deNormalise( getRmi( "12345.6789", new ValueMetaBigNumber( "target" ), new ValueMetaBigNumber( "value" ) ),
      new Object[] { KEY_VALUE, "target1", new BigDecimal( "9912345.6789" ) } );
    Assert.assertEquals( new BigDecimal( "9912345.6789" ), data.targetResult[ 0 ] );
  }
  
  RowMetaInterface getRmi( String nullString, ValueMetaInterface targetVmi, ValueMetaInterface valueVmi ) {

    // create rmi for one string and 2 integers
    RowMetaInterface rmi = new RowMeta();
    List<ValueMetaInterface> list = new ArrayList<>();
    list.add( new ValueMetaString( "key" ) );
    list.add( targetVmi );
    list.add( valueVmi );
    rmi.setValueMetaList( list );

    // denormalizer key field will be String 'keyValue'
    data.keyValue = new HashMap<>();
    List<Integer> listInt = new ArrayList<>();
    listInt.add( 0 );
    data.keyValue.put( KEY_VALUE, listInt );

    data.fieldNameIndex = new int[] { 2 }; //Index of the value being checked for "null if"
    data.targetResult = new Object[ 2 ];
    data.inputRowMeta = rmi;
    data.outputRowMeta = rmi;
    data.removeNrs = new int[] { 1, 2 };

    DenormaliserTargetField tField = new DenormaliserTargetField();
    tField.setTargetNullString( nullString );
    DenormaliserTargetField[] pivotField = new DenormaliserTargetField[] { tField };
    meta.setDenormaliserTargetField( pivotField );

    // return row meta interface to pass into denormalize method
    return rmi;
  }
}
