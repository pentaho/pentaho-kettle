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

package org.pentaho.di.trans.steps.blockingstep;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Test;
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

public class BlockingStep_PDI_11344_Test {

  private static RowMetaInterface createRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    {
      new ValueMetaString( "field1" ), new ValueMetaInteger( "field2" ),
      new ValueMetaNumber( "field3" ), new ValueMetaDate( "field4" ),
      new ValueMetaBoolean( "field5" ),
      new ValueMetaBigNumber( "field6" ),
      new ValueMetaBigNumber( "field7" ) };

    for ( ValueMetaInterface aValuesMeta : valuesMeta ) {
      rm.addValueMeta( aValuesMeta );
    }

    return rm;
  }

  @Test
  public void outputRowMetaIsCreateOnce() throws Exception {
    StepMockHelper<BlockingStepMeta, BlockingStepData> mockHelper =
      new StepMockHelper<BlockingStepMeta, BlockingStepData>( "BlockingStep", BlockingStepMeta.class,
        BlockingStepData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );

    BlockingStep step =
      new BlockingStep( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta,
        mockHelper.trans );
    step = spy( step );

    BlockingStepData data = new BlockingStepData();
    step.init( mockHelper.processRowsStepMetaInterface, data );
    step.setInputRowMeta( createRowMetaInterface() );

    doReturn( new Object[ 0 ] ).when( step ).getRow();
    step.processRow( mockHelper.processRowsStepMetaInterface, data );

    RowMetaInterface outputRowMeta = data.outputRowMeta;
    assertNotNull( outputRowMeta );

    doReturn( new Object[ 0 ] ).when( step ).getRow();
    step.processRow( mockHelper.processRowsStepMetaInterface, data );
    assertTrue( data.outputRowMeta == outputRowMeta );
  }
}
