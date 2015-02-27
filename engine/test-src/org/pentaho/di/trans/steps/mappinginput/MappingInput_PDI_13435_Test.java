/* ******************************************************************************
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

package org.pentaho.di.trans.steps.mappinginput;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.util.Collections;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Andrey Khayrutdinov
 */
public class MappingInput_PDI_13435_Test {
  private MappingInput input;
  private MappingInputData data;

  @Before
  public void setUp() throws Exception {
    StepMockHelper<MappingInputMeta, MappingInputData> stepMockHelper =
      new StepMockHelper<MappingInputMeta, MappingInputData>( "MappingInput", MappingInputMeta.class,
        MappingInputData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      stepMockHelper.logChannelInterface );

    data = new MappingInputData();
    data.linked = true;
    data.valueRenames = Collections.singletonList( new MappingValueRename( "source", "target" ) );

    input = new MappingInput( stepMockHelper.stepMeta, data,
      0, stepMockHelper.transMeta, stepMockHelper.trans );
    input = spy( input );
    doReturn( new Object[0] ).when( input ).getRow();
    doNothing().when( input ).putRow( any( RowMetaInterface.class ), any( Object[].class ) );
  }

  @Test
  public void renamesInputRowMeta() throws Exception {
    RowMeta inputRowMeta = new RowMeta(  );
    inputRowMeta.addValueMeta( new ValueMetaString( "source" ) );
    input.setInputRowMeta( inputRowMeta );

    input.processRow( new MappingInputMeta(), data );

    assertRowMetasContainField( "target", data.outputRowMeta, inputRowMeta );
  }

  @Test
  public void doesNotRenameInputRowMetaIfFoundRenamed() throws Exception {
    RowMeta inputRowMeta = new RowMeta(  );
    inputRowMeta.addValueMeta( new ValueMetaString( "target" ) );
    input.setInputRowMeta( inputRowMeta );

    input.processRow( new MappingInputMeta(), data );

    assertRowMetasContainField( "target", data.outputRowMeta, inputRowMeta );
  }

  @Test(expected = KettleException.class)
  public void failsWhenFoundNothingToRename() throws Exception {
    RowMeta inputRowMeta = new RowMeta(  );
    inputRowMeta.addValueMeta( new ValueMetaString( "s1" ) );
    inputRowMeta.addValueMeta( new ValueMetaString( "s2" ) );
    input.setInputRowMeta( inputRowMeta );

    input.processRow( new MappingInputMeta(), data );
  }

  private static void assertRowMetasContainField(String field, RowMetaInterface... metas) {
    for ( RowMetaInterface meta : metas ) {
      assertTrue( asList( meta.getFieldNames() ).contains( field ) );
    }
  }
}
