/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.httppost;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Assert;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * User: Dzmitry Stsiapanau Date: 12/2/13 Time: 4:35 PM
 */
public class HTTPPOSTIT {
  private class HTTPPOSTHandler extends HTTPPOST {

    Object[] row = new Object[] { "anyData" };
    Object[] outputRow;
    boolean  override;

    public HTTPPOSTHandler( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans, boolean override ) {
      super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
      this.override = override;
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


    @Override
    protected int requestStatusCode( PostMethod post, HostConfiguration hostConfiguration, HttpClient httpPostClient )
            throws IOException {
      if ( override ) {
        return 402;
      } else {
        return super.requestStatusCode( post, hostConfiguration, httpPostClient );
      }

    }

    @Override
    protected InputStreamReader openStream( String encoding, PostMethod post ) throws Exception {
      if ( override ) {
        InputStreamReader mockInputStreamReader = Mockito.mock( InputStreamReader.class );
        when( mockInputStreamReader.read() ).thenReturn( -1 );
        return mockInputStreamReader;
      } else {
        return super.openStream( encoding, post );
      }
    }

    @Override
    protected Header[] searchForHeaders( PostMethod post ) {
      Header[] headers = { new Header( "host", host ) };
      if ( override ) {
        return headers;
      } else {
        return super.searchForHeaders( post );
      }
    }
  }

  public static final String host = "localhost";
  public static final int port = 9998;
  public static final String HTTP_LOCALHOST_9998 = "http://localhost:9998/";
  @InjectMocks
  private StepMockHelper<HTTPPOSTMeta, HTTPPOSTData> stepMockHelper;
  private HttpServer httpServer;

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    stepMockHelper =
      new StepMockHelper<HTTPPOSTMeta, HTTPPOSTData>( "HTTPPOST CLIENT TEST",
        HTTPPOSTMeta.class, HTTPPOSTData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    verify( stepMockHelper.trans, never() ).stopAll();
    startHttp204Answer();
  }

  @After
  public void tearDown() throws Exception {
    httpServer.stop( 5 );

  }

  @Test
  public void test204Answer() throws Exception {
    HTTPPOSTData data = new HTTPPOSTData();
    int[] index = { 0, 1 };
    RowMeta meta = new RowMeta();
    meta.addValueMeta( new ValueMetaString( "fieldName" ) );
    meta.addValueMeta( new ValueMetaInteger( "codeFieldName" ) );
    Object[] expectedRow = new Object[] { "", 204L };
    HTTPPOST HTTPPOST = new HTTPPOSTHandler(
            stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans, false );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    HTTPPOST.setInputRowMeta( inputRowMeta );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    when( stepMockHelper.processRowsStepMetaInterface.getUrl() ).thenReturn( HTTP_LOCALHOST_9998 );
    when( stepMockHelper.processRowsStepMetaInterface.getQueryField() ).thenReturn( new String[] {} );
    when( stepMockHelper.processRowsStepMetaInterface.getArgumentField() ).thenReturn( new String[] {} );
    when( stepMockHelper.processRowsStepMetaInterface.getResultCodeFieldName() ).thenReturn( "ResultCodeFieldName" );
    when( stepMockHelper.processRowsStepMetaInterface.getFieldName() ).thenReturn( "ResultFieldName" );
    HTTPPOST.init( stepMockHelper.processRowsStepMetaInterface, data );
    Assert.assertTrue( HTTPPOST.processRow( stepMockHelper.processRowsStepMetaInterface, data ) );
    Object[] out = ( (HTTPPOSTHandler) HTTPPOST ).getOutputRow();
    Assert.assertTrue( meta.equals( out, expectedRow, index ) );
  }

  @Test
  public void testResponseHeader() throws Exception {
    HTTPPOSTData data = new HTTPPOSTData();
    int[] index = { 0, 1, 2 };
    RowMeta meta = new RowMeta();
    meta.addValueMeta( new ValueMetaString( "fieldName" ) );
    meta.addValueMeta( new ValueMetaInteger( "codeFieldName" ) );
    meta.addValueMeta( new ValueMetaString( "headerFieldName" ) );
    Object[] expectedRow =
            new Object[] { "", 402L, "{\"host\":\"localhost\"}" };
    HTTPPOST HTTPPOST = new HTTPPOSTHandler(
            stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans, true );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    HTTPPOST.setInputRowMeta( inputRowMeta );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    when( stepMockHelper.processRowsStepMetaInterface.getUrl() ).thenReturn( HTTP_LOCALHOST_9998 );
    when( stepMockHelper.processRowsStepMetaInterface.getQueryField() ).thenReturn( new String[] {} );
    when( stepMockHelper.processRowsStepMetaInterface.getArgumentField() ).thenReturn( new String[] {} );
    when( stepMockHelper.processRowsStepMetaInterface.getResultCodeFieldName() ).thenReturn( "ResultCodeFieldName" );
    when( stepMockHelper.processRowsStepMetaInterface.getFieldName() ).thenReturn( "ResultFieldName" );
    when( stepMockHelper.processRowsStepMetaInterface.getEncoding() ).thenReturn( "UTF8" );
    when( stepMockHelper.processRowsStepMetaInterface.getResponseHeaderFieldName() ).thenReturn(
            "ResponseHeaderFieldName" );
    HTTPPOST.init( stepMockHelper.processRowsStepMetaInterface, data );
    Assert.assertTrue( HTTPPOST.processRow( stepMockHelper.processRowsStepMetaInterface, data ) );
    Object[] out = ( (HTTPPOSTHandler) HTTPPOST ).getOutputRow();
    Assert.assertTrue( meta.equals( out, expectedRow, index ) );
  }


  private void startHttp204Answer() throws IOException {
    httpServer = HttpServer.create( new InetSocketAddress( HTTPPOSTIT.host, HTTPPOSTIT.port ), 10 );
    httpServer.createContext( "/", new HttpHandler() {
      @Override
      public void handle( HttpExchange httpExchange ) throws IOException {
        httpExchange.sendResponseHeaders( 204, 0 );
        httpExchange.close();
      }
    } );
    httpServer.start();
  }

}
