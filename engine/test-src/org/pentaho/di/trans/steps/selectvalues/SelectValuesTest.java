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

package org.pentaho.di.trans.steps.selectvalues;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleConversionException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Andrey Khayrutdinov
 */
public class SelectValuesTest {

  private static final String SELECTED_FIELD = "field";

  private final Object[] inputRow = new Object[] { "a string" };

  private SelectValues step;

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    StepMockHelper<SelectValuesMeta, StepDataInterface> helper =
      StepMockUtil.getStepMockHelper( SelectValuesMeta.class, "SelectValuesTest" );
    when( helper.stepMeta.isDoingErrorHandling() ).thenReturn( true );

    step = new SelectValues( helper.stepMeta, helper.stepDataInterface, 1, helper.transMeta, helper.trans );
    step = spy( step );
    doReturn( inputRow ).when( step ).getRow();
    doNothing().when( step )
      .putError( any( RowMetaInterface.class ), any( Object[].class ), anyLong(), anyString(), anyString(),
        anyString() );

    RowMeta inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta( new ValueMetaString( SELECTED_FIELD ) );
    step.setInputRowMeta( inputRowMeta );
  }

  @Test
  public void errorRowSetObtainsFieldName() throws Exception {
    SelectValuesMeta stepMeta = new SelectValuesMeta();
    stepMeta.allocate( 1, 0, 1 );
    stepMeta.getSelectName()[ 0 ] = SELECTED_FIELD;
    stepMeta.getMeta()[ 0 ] =
      new SelectMetadataChange( stepMeta, SELECTED_FIELD, null, ValueMetaInterface.TYPE_INTEGER, -2, -2,
        ValueMetaInterface.STORAGE_TYPE_NORMAL, null, false, null, null, false, null, null, null );

    SelectValuesData stepData = new SelectValuesData();
    stepData.select = true;
    stepData.metadata = true;
    stepData.firstselect = true;
    stepData.firstmetadata = true;

    step.processRow( stepMeta, stepData );

    verify( step )
      .putError( any( RowMetaInterface.class ), any( Object[].class ), anyLong(), anyString(), eq( SELECTED_FIELD ),
        anyString() );


    // additionally ensure conversion error causes KettleConversionError
    boolean properException = false;
    try {
      step.metadataValues( step.getInputRowMeta(), inputRow );
    } catch ( KettleConversionException e ) {
      properException = true;
    }
    assertTrue( properException );
  }
}