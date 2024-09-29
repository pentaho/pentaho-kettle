/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
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

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: Dzmitry Stsiapanau Date: 12/2/13 Time: 1:24 PM
 */
public class HTTPIT {

  private class HTTPHandler extends HTTP {

    Object[] row;
    Object[] outputRow;
    boolean override = false;

    public HTTPHandler( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                        Trans trans, boolean override ) {
      super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
      this.row = new Object[] { "anyData" };
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
     * @param row The row to put to the destination rowset(s).
     * @throws org.pentaho.di.core.exception.KettleStepException
     */
    @Override
    public void putRow( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
      outputRow = row;
    }

    public Object[] getOutputRow() {
      return outputRow;
    }


    @Override
    protected int requestStatusCode( HttpResponse httpResponse ) {
      if ( override ) {
        return 402;
      } else {
        return super.requestStatusCode( httpResponse );
      }
    }

    @Override
    protected Header[] searchForHeaders( CloseableHttpResponse response ) {
      Header[] headers = { new BasicHeader( "host", host ) };
      if ( override ) {
        return headers;
      } else {
        return super.searchForHeaders( response );
      }
    }

  }


  public static final String host = "localhost";
  public static final int port = 9998;
  public static final String HTTP_LOCALHOST_9998 = "http://localhost:9998/";

  @InjectMocks
  private StepMockHelper<HTTPMeta, HTTPData> stepMockHelper;
  private HttpServer httpServer;

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    stepMockHelper = new StepMockHelper<HTTPMeta, HTTPData>( "HTTP CLIENT TEST", HTTPMeta.class, HTTPData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    verify( stepMockHelper.trans, never() ).stopAll();
  }

  @After
  public void tearDown() throws Exception {
    httpServer.stop( 5 );

  }


  @Test
  public void test204Answer() throws Exception {
    startHttpServer( get204AnswerHandler() );
    HTTPData data = new HTTPData();
    int[] index = { 0, 1 };
    RowMeta meta = new RowMeta();
    meta.addValueMeta( new ValueMetaString( "fieldName" ) );
    meta.addValueMeta( new ValueMetaInteger( "codeFieldName" ) );
    Object[] expectedRow = new Object[] { "", 204L };
    HTTP http =
      new HTTPHandler( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans, false );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    http.setInputRowMeta( inputRowMeta );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    when( stepMockHelper.processRowsStepMetaInterface.getUrl() ).thenReturn( HTTP_LOCALHOST_9998 );
    when( stepMockHelper.processRowsStepMetaInterface.getHeaderField() ).thenReturn( new String[] {} );
    when( stepMockHelper.processRowsStepMetaInterface.getArgumentField() ).thenReturn( new String[] {} );
    when( stepMockHelper.processRowsStepMetaInterface.getResultCodeFieldName() ).thenReturn( "ResultCodeFieldName" );
    when( stepMockHelper.processRowsStepMetaInterface.getFieldName() ).thenReturn( "ResultFieldName" );
    http.init( stepMockHelper.processRowsStepMetaInterface, data );
    Assert.assertTrue( http.processRow( stepMockHelper.processRowsStepMetaInterface, data ) );
    Object[] out = ( (HTTPHandler) http ).getOutputRow();
    Assert.assertTrue( meta.equals( out, expectedRow, index ) );
  }

  @Test
  public void testResponseHeader() throws Exception {
    startHttpServer( get204AnswerHandler() );
    HTTPData data = new HTTPData();
    int[] index = { 0, 1, 3 };
    RowMeta meta = new RowMeta();
    meta.addValueMeta( new ValueMetaString( "fieldName" ) );
    meta.addValueMeta( new ValueMetaInteger( "codeFieldName" ) );
    meta.addValueMeta( new ValueMetaInteger( "responseTimeFieldName" ) );
    meta.addValueMeta( new ValueMetaString( "headerFieldName" ) );
    Object[] expectedRow =
      new Object[] { "", 402L, 0L, "{\"host\":\"localhost\"}" };
    HTTP http =
      new HTTPHandler( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans, true );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    http.setInputRowMeta( inputRowMeta );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    when( stepMockHelper.processRowsStepMetaInterface.getUrl() ).thenReturn( HTTP_LOCALHOST_9998 );
    when( stepMockHelper.processRowsStepMetaInterface.getHeaderField() ).thenReturn( new String[] {} );
    when( stepMockHelper.processRowsStepMetaInterface.getArgumentField() ).thenReturn( new String[] {} );
    when( stepMockHelper.processRowsStepMetaInterface.getResultCodeFieldName() ).thenReturn( "ResultCodeFieldName" );
    when( stepMockHelper.processRowsStepMetaInterface.getFieldName() ).thenReturn( "ResultFieldName" );
    when( stepMockHelper.processRowsStepMetaInterface.getEncoding() ).thenReturn( "UTF8" );
    when( stepMockHelper.processRowsStepMetaInterface.getResponseTimeFieldName() ).thenReturn(
      "ResponseTimeFieldName" );
    when( stepMockHelper.processRowsStepMetaInterface.getResponseHeaderFieldName() ).thenReturn(
      "ResponseHeaderFieldName" );
    http.init( stepMockHelper.processRowsStepMetaInterface, data );
    Assert.assertTrue( http.processRow( stepMockHelper.processRowsStepMetaInterface, data ) );
    Object[] out = ( (HTTPHandler) http ).getOutputRow();
    Assert.assertTrue( meta.equals( out, expectedRow, index ) );

  }


  @Test
  public void testDuplicateNamesInHeader() throws Exception {
    startHttpServer( getDuplicateHeadersHandler() );
    HTTPData data = new HTTPData();
    RowMeta meta = new RowMeta();
    meta.addValueMeta( new ValueMetaString( "headerFieldName" ) );
    HTTP http =
      new HTTPHandler( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans, false );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    http.setInputRowMeta( inputRowMeta );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    when( stepMockHelper.processRowsStepMetaInterface.getUrl() ).thenReturn( HTTP_LOCALHOST_9998 );
    when( stepMockHelper.processRowsStepMetaInterface.getHeaderField() ).thenReturn( new String[] {} );
    when( stepMockHelper.processRowsStepMetaInterface.getArgumentField() ).thenReturn( new String[] {} );
    when( stepMockHelper.processRowsStepMetaInterface.getEncoding() ).thenReturn( "UTF8" );
    when( stepMockHelper.processRowsStepMetaInterface.getResponseHeaderFieldName() ).thenReturn(
      "ResponseHeaderFieldName" );
    http.init( stepMockHelper.processRowsStepMetaInterface, data );
    Assert.assertTrue( http.processRow( stepMockHelper.processRowsStepMetaInterface, data ) );
    Object[] out = ( (HTTPHandler) http ).getOutputRow();
    Assert.assertTrue( out.length == 1 );
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse( (String) out[ 0 ] );
    Object userAgent = json.get( "User-agent" );
    Assert.assertTrue( "HTTPTool/1.0".equals( userAgent ) );
    Object cookies = json.get( "Set-cookie" );
    Assert.assertTrue( cookies instanceof JSONArray );
    for ( int i = 0; i < 3; i++ ) {
      String cookie = ( (String) ( (JSONArray) cookies ).get( i ) );
      Assert.assertTrue( cookie.startsWith( "cookie" + i ) );
    }
  }

  private void startHttpServer( HttpHandler httpHandler ) throws IOException {
    httpServer = HttpServer.create( new InetSocketAddress( HTTPIT.host, HTTPIT.port ), 10 );
    httpServer.createContext( "/", httpHandler );
    httpServer.start();
  }

  private HttpHandler get204AnswerHandler() {
    return httpExchange -> {
      httpExchange.sendResponseHeaders( 204, 0 );
      httpExchange.close();
    };
  }

  private HttpHandler getDuplicateHeadersHandler() {
    return httpExchange -> {
      Headers headers = httpExchange.getResponseHeaders();
      headers.add( "User-agent", "HTTPTool/1.0" );
      headers.add( "Set-cookie", "cookie0=value0; Max-Age=3600" );
      headers.add( "Set-cookie", "cookie1=value1; HttpOnly" );
      headers.add( "Set-cookie", "cookie2=value2; Secure" );
      httpExchange.sendResponseHeaders( 200, 0 );
      httpExchange.close();
    };
  }


  // LoadSave Test is a unit test of the meta, not an integration test. Moved to new class.
  // MB 5/2016
}
