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

package org.pentaho.di.trans.steps.clonerow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.util.Collections;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

/**
 * @author Andrey Khayrutdinov
 */
public class CloneRowTest {

  private StepMockHelper<CloneRowMeta, CloneRowData> stepMockHelper;

  @Before
  public void setup() {
    stepMockHelper =
      new StepMockHelper<CloneRowMeta, CloneRowData>( "Test CloneRow", CloneRowMeta.class, CloneRowData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void tearDown() {
    stepMockHelper.cleanUp();
  }

  @Test( expected = KettleException.class )
  public void nullNrCloneField() throws Exception {
    CloneRow step =
      new CloneRow( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
    step.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );

    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    when( inputRowMeta.getInteger( any( Object[].class ), anyInt() ) ).thenReturn( null );

    RowSet inputRowSet = stepMockHelper.getMockInputRowSet( new Integer[]{ null } );
    when( inputRowSet.getRowMeta() ).thenReturn( inputRowMeta );
    step.setInputRowSets( Collections.singletonList( inputRowSet ) );

    when( stepMockHelper.processRowsStepMetaInterface.isNrCloneInField() ).thenReturn( true );
    when( stepMockHelper.processRowsStepMetaInterface.getNrCloneField() ).thenReturn( "field" );

    step.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface );
  }
}
