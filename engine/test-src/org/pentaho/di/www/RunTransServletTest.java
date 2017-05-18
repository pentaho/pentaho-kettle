/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2017 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.www;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;


@RunWith( MockitoJUnitRunner.class )
public class RunTransServletTest {

  @Mock
  TransMeta transMeta;
  @Mock
  Trans trans;

  List<StepMetaDataCombi> stepList;
  ByteArrayOutputStream outData;
  PrintWriter out;
  String lineSeparator = System.getProperty( "line.separator" );

  String transId = "testId";
  String expectedOutputIfNoServletOutputSteps = "<webresult>" + lineSeparator
    + "  <result>OK</result>" + lineSeparator
    + "  <message>Transformation started</message>" + lineSeparator
    + "  <id>testId</id>" + lineSeparator
    + "</webresult>" + lineSeparator + lineSeparator;


  RunTransServlet runTransServlet;

  @Before
  public void setup() throws Exception {
    runTransServlet = new RunTransServlet();
    outData = new ByteArrayOutputStream();
    out = new PrintWriter( outData );
    stepList = new ArrayList<>();
    for ( int i = 0; i < 5; i++ ) {
      StepMetaDataCombi stepMetaDataCombi = new StepMetaDataCombi();
      StepMetaInterface stepMeta = mock( StepMetaInterface.class );
      when( stepMeta.passDataToServletOutput() ).thenReturn( false );
      stepMetaDataCombi.meta = stepMeta;
      stepList.add( stepMetaDataCombi );
    }
    when( trans.getSteps() ).thenReturn( stepList );
    when( trans.getContainerObjectId() ).thenReturn( transId );
  }

  @Test
  public void testFinishProcessingTransWithoutServletOutputSteps() throws Exception {
    runTransServlet.finishProcessing( trans, out );
    assertEquals( expectedOutputIfNoServletOutputSteps, outData.toString() );
  }

  @Test
  public void testFinishProcessingTransWithServletOutputSteps() throws Exception {
    StepMetaDataCombi stepMetaDataCombi = new StepMetaDataCombi();
    StepMetaInterface stepMeta = mock( StepMetaInterface.class );
    when( stepMeta.passDataToServletOutput() ).thenReturn( true );
    stepMetaDataCombi.meta = stepMeta;
    stepList.add( stepMetaDataCombi );
    doAnswer( new Answer<Void>() {
      @Override
      public Void answer( InvocationOnMock invocation ) throws Throwable {
        Thread.currentThread().sleep( 2000 );
        return null;
      }
    } ).when( trans ).waitUntilFinished();
    runTransServlet.finishProcessing( trans, out );
    assertTrue( outData.toString().isEmpty() );
  }


}
