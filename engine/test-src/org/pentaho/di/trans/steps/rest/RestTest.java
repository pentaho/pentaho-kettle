/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.rest;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

/**
 * User: Dzmitry Stsiapanau Date: 11/29/13 Time: 3:42 PM
 */
public class RestTest {

  public static final String HTTP_LOCALHOST_9998 = "http://localhost:9998/";

  private class RestHandler extends Rest {

    Object[] row = new Object[] { "anyData" };
    Object[] outputRow;

    public RestHandler( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
        Trans trans ) {
      super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    }

    @SuppressWarnings( "unused" )
    public void setRow( Object[] row ) {
      this.row = row;
    }

    /**
     * In case of getRow, we receive data from previous steps through the input rowset. In case we split the stream, we
     * have to copy the data to the alternate splits: rowsets 1 through n.
     */
    @Override
    public Object[] getRow() throws KettleException {
      return row;
    }

    /**
     * putRow is used to copy a row, to the alternate rowset(s) This should get priority over everything else!
     * (synchronized) If distribute is true, a row is copied only once to the output rowsets, otherwise copies are sent
     * to each rowset!
     * 
     * @param row
     *          The row to put to the destination rowset(s).
     * @throws org.pentaho.di.core.exception.KettleStepException
     * 
     */
    @Override
    public void putRow( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
      outputRow = row;
    }

    public Object[] getOutputRow() {
      return outputRow;
    }

  }

  private StepMockHelper<RestMeta, RestData> stepMockHelper;
  private HttpServer server;

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    stepMockHelper = new StepMockHelper<RestMeta, RestData>( "REST CLIENT TEST", RestMeta.class, RestData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    verify( stepMockHelper.trans, never() ).stopAll();
    server = HttpServerFactory.create( HTTP_LOCALHOST_9998 );
    server.start();
  }

  @After
  public void tearDown() throws Exception {
    server.stop( 0 );
  }

  @Test
  public void testNoContent() throws Exception {
    RestData data = new RestData();
    Object[] expectedRow = new Object[] { "", 204L, null, null, null, null, null, null, null, null, null, null };
    Rest rest = new RestHandler( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    rest.setInputRowMeta( inputRowMeta );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    when( stepMockHelper.processRowsStepMetaInterface.getUrl() ).thenReturn(
        HTTP_LOCALHOST_9998 + "restTest/restNoContentAnswer" );
    when( stepMockHelper.processRowsStepMetaInterface.getMethod() ).thenReturn( RestMeta.HTTP_METHOD_GET );
    rest.init( stepMockHelper.processRowsStepMetaInterface, data );
    data.resultFieldName = "ResultFieldName";
    data.resultCodeFieldName = "ResultCodeFieldName";
    assertTrue( rest.processRow( stepMockHelper.processRowsStepMetaInterface, data ) );
    System.out.println( Arrays.toString( expectedRow ) );
    Object[] out = ( (RestHandler) rest ).getOutputRow();
    System.out.println( Arrays.toString( out ) );
    assertTrue( Arrays.equals( expectedRow, out ) );
  }

  @Test
  public void testLoadSaveRoundTrip() throws KettleException {
    List<String> attributes =
        Arrays.asList( "applicationType", "method", "url", "urlInField", "dynamicMethod", "methodFieldName",
            "urlField", "bodyField", "httpLogin", "httpPassword", "proxyHost", "proxyPort", "preemptive",
            "trustStoreFile", "trustStorePassword", "headerField", "headerName", "parameterField", "parameterName",
            "matrixParameterField", "matrixParameterName", "fieldName", "resultCodeFieldName", "responseTimeFieldName" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
        new HashMap<String, FieldLoadSaveValidator<?>>();

    // Arrays need to be consistent length
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 );
    fieldLoadSaveValidatorAttributeMap.put( "headerField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "headerName", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "parameterField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "parameterName", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "matrixParameterField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "matrixParameterName", stringArrayLoadSaveValidator );

    LoadSaveTester loadSaveTester =
        new LoadSaveTester( RestMeta.class, attributes, new HashMap<String, String>(), new HashMap<String, String>(),
            fieldLoadSaveValidatorAttributeMap, new HashMap<String, FieldLoadSaveValidator<?>>() );

    loadSaveTester.testRepoRoundTrip();
    loadSaveTester.testXmlRoundTrip();
  }
}
