/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2017 by Hitachi Vantara : http://www.pentaho.com
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


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepMetaInterface;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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


  @Test
  public void testRunTransServletCheckParameter() throws Exception {
    HttpServletRequest request = Mockito.mock( HttpServletRequest.class );
    HttpServletResponse response = Mockito.mock( HttpServletResponse.class );
    Mockito.when( request.getParameter( "trans" ) ).thenReturn( "home/test.rtr" );

    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    Mockito.when( request.getContextPath() ).thenReturn( RunTransServlet.CONTEXT_PATH );
    Mockito.when( response.getWriter() ).thenReturn( printWriter );

    TransformationMap mockTransformationMap = Mockito.mock( TransformationMap.class );
    SlaveServerConfig slaveServerConfig = Mockito.mock( SlaveServerConfig.class );
    Mockito.when( mockTransformationMap.getSlaveServerConfig() ).thenReturn( slaveServerConfig );

    Repository repository = Mockito.mock( Repository.class );
    Mockito.when( slaveServerConfig.getRepository() ).thenReturn( repository );
    RepositoryDirectoryInterface repositoryDirectoryInterface = Mockito.mock( RepositoryDirectoryInterface.class );
    Mockito.when( repository.loadRepositoryDirectoryTree() ).thenReturn( repositoryDirectoryInterface );
    Mockito.when( repositoryDirectoryInterface.findDirectory( Mockito.anyString() ) )
      .thenReturn( repositoryDirectoryInterface );

    TransMeta transMeta = Mockito.mock( TransMeta.class );
    Mockito.when( repository.loadTransformation( Mockito.any(), Mockito.any() ) ).thenReturn(
      transMeta );

    String testParameter = "testParameter";
    Mockito.when( transMeta.listVariables() ).thenReturn( new String[] { testParameter } );
    Mockito.when( transMeta.getVariable( Mockito.anyString() ) ).thenReturn( "default value" );

    Mockito.when( transMeta.listParameters() ).thenReturn( new String[] { testParameter } );
    Mockito.when( request.getParameterNames() ).thenReturn( new StringTokenizer( testParameter ) );

    String testValue = "testValue";
    Mockito.when( request.getParameterValues( testParameter ) ).thenReturn( new String[] { testValue } );

    RunTransServlet runTransServlet = Mockito.mock( RunTransServlet.class );
    Mockito.doCallRealMethod().when( runTransServlet ).doGet( Mockito.anyObject(), Mockito.anyObject() );

    Trans trans =
      new Trans( transMeta, new SimpleLoggingObject( RunTransServlet.CONTEXT_PATH, LoggingObjectType.CARTE, null ) );
    Mockito.when( runTransServlet.createTrans( Mockito.anyObject(), Mockito.anyObject() ) ).thenReturn( trans );
    Mockito.when( transMeta.getParameterValue( Mockito.eq( testParameter ) ) ).thenReturn( testValue );

    runTransServlet.log = new LogChannel( "RunTransServlet" );
    runTransServlet.transformationMap = mockTransformationMap;


    runTransServlet.doGet( request, response );
    Assert.assertEquals( testValue, trans.getParameterValue( testParameter ) );
  }
}
