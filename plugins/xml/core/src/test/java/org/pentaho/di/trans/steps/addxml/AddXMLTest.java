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
package org.pentaho.di.trans.steps.addxml;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.www.SocketRepository;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AddXMLTest {

  private StepMockHelper<AddXMLMeta, AddXMLData> stepMockHelper;

  @Before
  public void setup() {
    XMLField field = mock( XMLField.class );
    when( field.getElementName() ).thenReturn( "ADDXML_TEST" );
    when( field.isAttribute() ).thenReturn( true );

    stepMockHelper = new StepMockHelper<AddXMLMeta, AddXMLData>( "ADDXML_TEST", AddXMLMeta.class, AddXMLData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    SocketRepository socketRepository = mock( SocketRepository.class );
    when( stepMockHelper.trans.getSocketRepository() ).thenReturn( socketRepository );
    when( stepMockHelper.initStepMetaInterface.getOutputFields() ).thenReturn( new XMLField[] { field } );
    when( stepMockHelper.initStepMetaInterface.getRootNode() ).thenReturn( "ADDXML_TEST" );
  }

  @After
  public void tearDown() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testProcessRow() throws KettleException {
    AddXML addXML =
        new AddXML( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
            stepMockHelper.trans );
    addXML.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    addXML.setInputRowSets( asList( createSourceRowSet( "ADDXML_TEST" ) ) );

    assertTrue( addXML.processRow( stepMockHelper.initStepMetaInterface, stepMockHelper.processRowsStepDataInterface ) );
    assertTrue( addXML.getErrors() == 0 );
    assertTrue( addXML.getLinesWritten() > 0 );
  }

  private RowSet createSourceRowSet( String source ) {
    RowSet sourceRowSet = stepMockHelper.getMockInputRowSet( new String[] { source } );
    RowMetaInterface sourceRowMeta = mock( RowMetaInterface.class );
    when( sourceRowMeta.getFieldNames() ).thenReturn( new String[] { source } );
    when( sourceRowSet.getRowMeta() ).thenReturn( sourceRowMeta );

    return sourceRowSet;
  }

}
