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


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepMetaInterfaceExtended;
import org.pentaho.di.trans.steps.insertupdate.InsertUpdate;
import org.pentaho.di.trans.steps.insertupdate.InsertUpdateData;
import org.pentaho.di.trans.steps.insertupdate.InsertUpdateMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


@RunWith( MockitoJUnitRunner.class )
public class RunTransServletTest {

  @Mock
  TransMeta transMeta;
  @Mock
  Trans trans;
  @Mock( extraInterfaces = StepMetaInterfaceExtended.class )
  StepMetaInterface stepMeta;

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
      stepMetaDataCombi.meta = Mockito.mock( StepMetaInterface.class );
      stepList.add( stepMetaDataCombi );
    }
    Mockito.when( trans.getSteps() ).thenReturn( stepList );
    Mockito.when( trans.getContainerObjectId() ).thenReturn( transId );
  }

  @Test
  public void testFinishProcessingTransWithoutServletOutputSteps() throws Exception {
    runTransServlet.finishProcessing( trans, out );
    Assert.assertEquals( expectedOutputIfNoServletOutputSteps, outData.toString() );
  }

  @Test
  public void testFinishProcessingTransWithServletOutputSteps() throws Exception {
    StepMetaDataCombi stepMetaDataCombi = new StepMetaDataCombi();
    Mockito.when( ( (StepMetaInterfaceExtended) stepMeta ).passDataToServletOutput() ).thenReturn( true );
    stepMetaDataCombi.meta = stepMeta;
    stepList.add( stepMetaDataCombi );
    Mockito.doAnswer( new Answer<Void>() {
      @Override
      public Void answer( InvocationOnMock invocation ) throws Throwable {
        Thread.currentThread().sleep( 2000 );
        return null;
      }
    } ).when( trans ).waitUntilFinished();
    runTransServlet.finishProcessing( trans, out );
    Assert.assertTrue( outData.toString().isEmpty() );
  }

  @Test
  public void testErrorProcessRow() throws KettleException {
    StepMockHelper<InsertUpdateMeta, InsertUpdateData> mockHelper =
            new StepMockHelper<>( "insertUpdate", InsertUpdateMeta.class, InsertUpdateData.class );
    Mockito.when( mockHelper.logChannelInterfaceFactory.create( Mockito.any(), Mockito.any( LoggingObjectInterface.class ) ) )
            .thenReturn( mockHelper.logChannelInterface );
    Mockito.when( mockHelper.stepMeta.getStepMetaInterface() ).thenReturn( new InsertUpdateMeta() );

    InsertUpdate insertUpdateStep =
            new InsertUpdate( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );
    insertUpdateStep = Mockito.spy( insertUpdateStep );

    Mockito.doReturn( new Object[] {} ).when( insertUpdateStep ).getRow();
    insertUpdateStep.first = false;
    mockHelper.processRowsStepDataInterface.lookupParameterRowMeta = Mockito.mock( RowMetaInterface.class );
    mockHelper.processRowsStepDataInterface.keynrs = new int[] {};
    mockHelper.processRowsStepDataInterface.db = Mockito.mock( Database.class );
    mockHelper.processRowsStepDataInterface.valuenrs = new int[] {};
    Mockito.doThrow( new KettleStepException( "Test exception" ) ).when( insertUpdateStep ).
            putRow( (RowMetaInterface) Mockito.any(), (Object[]) Mockito.any() );

    boolean result =
            insertUpdateStep.processRow( mockHelper.processRowsStepMetaInterface, mockHelper.processRowsStepDataInterface );
    Assert.assertFalse( result );
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
    Mockito.when( repository.loadTransformation( (ObjectId) Mockito.any(), (String) Mockito.any() ) ).thenReturn(
      transMeta );

    final String testParameter = "testParameter";
    Mockito.when( transMeta.listVariables() ).thenReturn( new String[] { testParameter } );
    Mockito.when( transMeta.getVariable( Mockito.anyString() ) ).thenReturn( "default value" );

    Mockito.when( transMeta.listParameters() ).thenReturn( new String[] { testParameter } );
    Mockito.when( request.getParameterNames() ).thenReturn( new Enumeration() {
      Set<String> set = new HashSet<>();
      Iterator<String> iterator;
      {
        set.add( testParameter );
        iterator = set.iterator();
      }

      @Override
      public boolean hasMoreElements() {
        return iterator.hasNext();
      }

      @Override
      public Object nextElement() {
        return iterator.next();
      }
    } );

    String testValue = "testValue";
    Mockito.when( request.getParameterValues( testParameter ) ).thenReturn( new String[] { testValue } );

    RunTransServlet runTransServlet = Mockito.mock( RunTransServlet.class );
    Mockito.doCallRealMethod().when( runTransServlet ).doGet( (HttpServletRequest) Mockito.anyObject(), (HttpServletResponse) Mockito.anyObject() );

    Trans trans =
      new Trans( transMeta, new SimpleLoggingObject( RunTransServlet.CONTEXT_PATH, LoggingObjectType.CARTE, null ) );
    Mockito.when( runTransServlet.createTrans( (TransMeta) Mockito.any(), (SimpleLoggingObject) Mockito.any() ) ).thenReturn( trans );
    Mockito.when( transMeta.getParameterValue( Mockito.eq( testParameter ) ) ).thenReturn( testValue );

    runTransServlet.log = new LogChannel( "RunTransServlet" );
    runTransServlet.transformationMap = mockTransformationMap;


    runTransServlet.doGet( request, response );
    Assert.assertEquals( testValue, trans.getParameterValue( testParameter ) );
  }
}
