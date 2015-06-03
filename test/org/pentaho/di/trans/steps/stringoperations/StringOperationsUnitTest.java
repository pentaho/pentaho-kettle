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

package org.pentaho.di.trans.steps.stringoperations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;

public class StringOperationsUnitTest {


  private static final String INPUT_FIELD_NAME = "inputString";
  private static final String STEP_NAME = "My StringOperations Step";

  private static List<RowMetaAndData> getInputData() {
    RowMeta rm = new RowMeta();
    rm.addValueMeta( new ValueMetaString( INPUT_FIELD_NAME ) );
    return Arrays.<RowMetaAndData>asList( new RowMetaAndData( rm, " joHn dOe " ) );
  }

  @BeforeClass
  public static void before() throws KettleException {
    KettleEnvironment.init();
  }

  @Test
  public void testUpperLower() throws KettleException {
    StringOperationsMeta stepMeta = new StringOperationsMeta();
    stepMeta.allocate( 3 );
    stepMeta.setFieldInStream( new String[]{ INPUT_FIELD_NAME, INPUT_FIELD_NAME, INPUT_FIELD_NAME } );
    stepMeta.setFieldOutStream( new String[]{ "Normal", "Upper", "Lower" } );
    stepMeta.setLowerUpper( new int[]{ StringOperationsMeta.LOWER_UPPER_NONE, StringOperationsMeta.LOWER_UPPER_UPPER,
      StringOperationsMeta.LOWER_UPPER_LOWER } );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, stepMeta, STEP_NAME );
    List<RowMetaAndData> ret =
      TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, STEP_NAME,
        TransTestFactory.DUMMY_STEPNAME, getInputData() );

    assertNotNull( ret );
    assertEquals( 1, ret.size() );
    assertEquals( 4, ret.get( 0 ).getRowMeta().size() );
    assertEquals( INPUT_FIELD_NAME, ret.get( 0 ).getRowMeta().getValueMeta( 0 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, ret.get( 0 ).getRowMeta().getValueMeta( 0 ).getType() );
    assertEquals( " joHn dOe ", ret.get( 0 ).getString( 0, "default" ) );
    assertEquals( "Normal", ret.get( 0 ).getRowMeta().getValueMeta( 1 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, ret.get( 0 ).getRowMeta().getValueMeta( 1 ).getType() );
    assertEquals( " joHn dOe ", ret.get( 0 ).getString( 1, "default" ) );
    assertEquals( "Upper", ret.get( 0 ).getRowMeta().getValueMeta( 2 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, ret.get( 0 ).getRowMeta().getValueMeta( 2 ).getType() );
    assertEquals( " JOHN DOE ", ret.get( 0 ).getString( 2, "default" ) );
    assertEquals( "Lower", ret.get( 0 ).getRowMeta().getValueMeta( 3 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, ret.get( 0 ).getRowMeta().getValueMeta( 3 ).getType() );
    assertEquals( " john doe ", ret.get( 0 ).getString( 3, "default" ) );
  }

  @Test
  public void testReverseString() throws KettleException {
    StringOperationsMeta stepMeta = new StringOperationsMeta();
    stepMeta.allocate( 2 );
    stepMeta.setFieldInStream( new String[]{ INPUT_FIELD_NAME, INPUT_FIELD_NAME } );
    stepMeta.setFieldOutStream( new String[]{ "Not Reversed", "Reversed" } );
    stepMeta.setReverseString( new int[]{ StringOperationsMeta.REVERSE_NO, StringOperationsMeta.REVERSE_YES } );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, stepMeta, STEP_NAME );
    List<RowMetaAndData> ret =
      TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, STEP_NAME,
        TransTestFactory.DUMMY_STEPNAME, getInputData() );

    assertNotNull( ret );
    assertEquals( 1, ret.size() );
    assertEquals( 3, ret.get( 0 ).getRowMeta().size() );
    assertEquals( INPUT_FIELD_NAME, ret.get( 0 ).getRowMeta().getValueMeta( 0 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, ret.get( 0 ).getRowMeta().getValueMeta( 0 ).getType() );
    assertEquals( " joHn dOe ", ret.get( 0 ).getString( 0, "default" ) );
    assertEquals( "Not Reversed", ret.get( 0 ).getRowMeta().getValueMeta( 1 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, ret.get( 0 ).getRowMeta().getValueMeta( 1 ).getType() );
    assertEquals( " joHn dOe ", ret.get( 0 ).getString( 1, "default" ) );
    assertEquals( "Reversed", ret.get( 0 ).getRowMeta().getValueMeta( 2 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, ret.get( 0 ).getRowMeta().getValueMeta( 2 ).getType() );
    assertEquals( " eOd nHoj ", ret.get( 0 ).getString( 2, "default" ) );
  }

  @Test
  public void testReverseStringMultipleOptions() throws KettleException {
    StringOperationsMeta stepMeta = new StringOperationsMeta();
    stepMeta.allocate( 2 );
    stepMeta.setFieldInStream( new String[]{ INPUT_FIELD_NAME, INPUT_FIELD_NAME } );
    stepMeta.setFieldOutStream( new String[]{ "Reverse_InitCap_LeftTrim", "Reverse_NoInitCap_RightTrim" } );
    stepMeta.setReverseString( new int[]{ StringOperationsMeta.REVERSE_YES, StringOperationsMeta.REVERSE_YES } );
    stepMeta.setInitCap( new int[]{ StringOperationsMeta.INIT_CAP_YES, StringOperationsMeta.INIT_CAP_NO } );
    stepMeta.setTrimType( new int[]{ StringOperationsMeta.TRIM_LEFT, StringOperationsMeta.TRIM_RIGHT } );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, stepMeta, STEP_NAME );
    List<RowMetaAndData> ret =
      TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, STEP_NAME,
        TransTestFactory.DUMMY_STEPNAME, getInputData() );

    assertNotNull( ret );
    assertEquals( 1, ret.size() );
    assertEquals( 3, ret.get( 0 ).getRowMeta().size() );
    assertEquals( INPUT_FIELD_NAME, ret.get( 0 ).getRowMeta().getValueMeta( 0 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, ret.get( 0 ).getRowMeta().getValueMeta( 0 ).getType() );
    assertEquals( " joHn dOe ", ret.get( 0 ).getString( 0, "default" ) );
    assertEquals( "Reverse_InitCap_LeftTrim", ret.get( 0 ).getRowMeta().getValueMeta( 1 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, ret.get( 0 ).getRowMeta().getValueMeta( 1 ).getType() );
    assertEquals( "Eod Nhoj ", ret.get( 0 ).getString( 1, "default" ) );
    assertEquals( "Reverse_NoInitCap_RightTrim", ret.get( 0 ).getRowMeta().getValueMeta( 2 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, ret.get( 0 ).getRowMeta().getValueMeta( 2 ).getType() );
    assertEquals( " eOd nHoj", ret.get( 0 ).getString( 2, "default" ) );
  }
}
