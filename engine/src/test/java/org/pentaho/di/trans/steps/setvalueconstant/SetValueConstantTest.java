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

package org.pentaho.di.trans.steps.setvalueconstant;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

/**
 * Tests for "Set field value to a constant" step
 *
 * @author Pavel Sakun
 * @see SetValueConstant
 */
public class SetValueConstantTest {
  private StepMockHelper<SetValueConstantMeta, SetValueConstantData> smh;

  @Before
  public void setUp() {
    smh =
        new StepMockHelper<SetValueConstantMeta, SetValueConstantData>( "SetValueConstant", SetValueConstantMeta.class,
            SetValueConstantData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        smh.logChannelInterface );
  }

  @Test
  public void testUpdateField() throws Exception {
    SetValueConstant step = new SetValueConstant( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );

    ValueMetaInterface valueMeta = new ValueMetaString( "Field1" );
    valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );

    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( valueMeta );

    doReturn( new String[] { null } ).when( smh.initStepMetaInterface ).getReplaceMask();
    doReturn( rowMeta ).when( smh.initStepDataInterface ).getConvertRowMeta();
    doReturn( rowMeta ).when( smh.initStepDataInterface ).getOutputRowMeta();
    doReturn( 1 ).when( smh.initStepDataInterface ).getFieldnr();
    doReturn( new int[] { 0 } ).when( smh.initStepDataInterface ).getFieldnrs();
    doReturn( new String[] { "foo" } ).when( smh.initStepDataInterface ).getRealReplaceByValues();

    step.init( smh.initStepMetaInterface, smh.initStepDataInterface );

    Method m = SetValueConstant.class.getDeclaredMethod( "updateField", Object[].class );
    m.setAccessible( true );

    Object[] row = new Object[] { null };
    m.invoke( step, new Object[] { row } );

    Assert.assertEquals( "foo", valueMeta.getString( row[0] ) );
  }
}
