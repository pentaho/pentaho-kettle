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

package org.pentaho.di.trans.steps.httppost;

import com.google.common.io.ByteStreams;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: Dzmitry Stsiapanau Date: 12/2/13 Time: 4:35 PM
 */
public class HTTPPOSTIT {
  class HTTPPOSTHandler extends HTTPPOST {

    Object[] row = new Object[] { "anyData" };
    Object[] outputRow;
    boolean override;

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
    protected InputStreamReader openStream( String encoding, HttpResponse httpResponse ) throws Exception {
      if ( override ) {
        InputStreamReader mockInputStreamReader = Mockito.mock( InputStreamReader.class );
        when( mockInputStreamReader.read() ).thenReturn( -1 );
        return mockInputStreamReader;
      } else {
        return super.openStream( encoding, httpResponse );
      }
    }

    @Override
    protected Header[] searchForHeaders( HttpResponse response ) {
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
  }

  @After
  public void tearDown() throws Exception {
    httpServer.stop( 5 );

  }

  @Test
  public void test204Answer() throws Exception {
    startHttpServer( get204AnswerHandler() );
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
    startHttpServer( get204AnswerHandler() );
    HTTPPOSTData data = new HTTPPOSTData();
    int[] index = { 0, 1, 3 };
    RowMeta meta = new RowMeta();
    meta.addValueMeta( new ValueMetaString( "fieldName" ) );
    meta.addValueMeta( new ValueMetaInteger( "codeFieldName" ) );
    meta.addValueMeta( new ValueMetaInteger( "responseTimeFieldName" ) );
    meta.addValueMeta( new ValueMetaString( "headerFieldName" ) );
    Object[] expectedRow =
      new Object[] { "", 402L, 0L, "{\"host\":\"localhost\"}" };
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
    when( stepMockHelper.processRowsStepMetaInterface.getEncoding() ).thenReturn( "UTF-8" );
    when( stepMockHelper.processRowsStepMetaInterface.getResponseTimeFieldName() ).thenReturn(
      "ResponseTimeFieldName" );
    when( stepMockHelper.processRowsStepMetaInterface.getResponseHeaderFieldName() ).thenReturn(
      "ResponseHeaderFieldName" );
    HTTPPOST.init( stepMockHelper.processRowsStepMetaInterface, data );
    Assert.assertTrue( HTTPPOST.processRow( stepMockHelper.processRowsStepMetaInterface, data ) );
    Object[] out = ( (HTTPPOSTHandler) HTTPPOST ).getOutputRow();
    Assert.assertTrue( meta.equals( out, expectedRow, index ) );
  }

  @Test
  public void testDuplicateNamesInHeader() throws Exception {
    startHttpServer( getDuplicateHeadersHandler() );
    HTTPPOSTData data = new HTTPPOSTData();
    RowMeta meta = new RowMeta();
    meta.addValueMeta( new ValueMetaString( "headerFieldName" ) );
    HTTPPOST HTTPPOST = new HTTPPOSTHandler(
      stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans, false );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    HTTPPOST.setInputRowMeta( inputRowMeta );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    when( stepMockHelper.processRowsStepMetaInterface.getUrl() ).thenReturn( HTTP_LOCALHOST_9998 );
    when( stepMockHelper.processRowsStepMetaInterface.getQueryField() ).thenReturn( new String[] {} );
    when( stepMockHelper.processRowsStepMetaInterface.getArgumentField() ).thenReturn( new String[] {} );
    when( stepMockHelper.processRowsStepMetaInterface.getEncoding() ).thenReturn( "UTF-8" );
    when( stepMockHelper.processRowsStepMetaInterface.getResponseHeaderFieldName() ).thenReturn(
      "ResponseHeaderFieldName" );
    HTTPPOST.init( stepMockHelper.processRowsStepMetaInterface, data );
    Assert.assertTrue( HTTPPOST.processRow( stepMockHelper.processRowsStepMetaInterface, data ) );
    Object[] out = ( (HTTPPOSTHandler) HTTPPOST ).getOutputRow();
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

  @Test
  public void testUTF8() throws Exception {
    testServerReturnsCorrectlyEncodedParams( "test string \uD842\uDFB7 øó 測試", "UTF-8" );
  }

  @Test
  public void testUTF16() throws Exception {
    testServerReturnsCorrectlyEncodedParams( "test string \uD842\uDFB7 øó 測試", "UTF-16" );
  }

  @Test
  public void testUTF32() throws Exception {
    testServerReturnsCorrectlyEncodedParams( "test string \uD842\uDFB7 øó 測試", "UTF-32" );
  }

  public void testServerReturnsCorrectlyEncodedParams( String testString, String testCharset ) throws Exception {
    AtomicBoolean testStatus = new AtomicBoolean();
    startHttpServer( getEncodingCheckingHandler( testString, testCharset, testStatus ) );
    HTTPPOSTData data = new HTTPPOSTData();
    RowMeta meta = new RowMeta();
    meta.addValueMeta( new ValueMetaString( "fieldName" ) );
    HTTPPOSTHandler httpPost = new HTTPPOSTHandler(
      stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans, false );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    httpPost.setInputRowMeta( inputRowMeta );
    httpPost.row = new Object[] { testString };
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    when( inputRowMeta.getString( httpPost.row, 0 ) ).thenReturn( testString );
    when( stepMockHelper.processRowsStepMetaInterface.getUrl() ).thenReturn( HTTP_LOCALHOST_9998 );
    when( stepMockHelper.processRowsStepMetaInterface.getQueryField() ).thenReturn( new String[] {} );
    when( stepMockHelper.processRowsStepMetaInterface.getArgumentField() )
      .thenReturn( new String[] { "testBodyField" } );
    when( stepMockHelper.processRowsStepMetaInterface.getArgumentParameter() )
      .thenReturn( new String[] { "testBodyParam" } );
    when( stepMockHelper.processRowsStepMetaInterface.getArgumentHeader() ).thenReturn( new boolean[] { false } );
    when( stepMockHelper.processRowsStepMetaInterface.getFieldName() ).thenReturn( "ResultFieldName" );
    when( stepMockHelper.processRowsStepMetaInterface.getEncoding() ).thenReturn( testCharset );
    httpPost.init( stepMockHelper.processRowsStepMetaInterface, data );
    Assert.assertTrue( httpPost.processRow( stepMockHelper.processRowsStepMetaInterface, data ) );
    Assert.assertTrue( testStatus.get(), "Test failed" );
  }


  private void startHttpServer( HttpHandler httpHandler ) throws IOException {
    httpServer = HttpServer.create( new InetSocketAddress( HTTPPOSTIT.host, HTTPPOSTIT.port ), 10 );
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

  private HttpHandler getEncodingCheckingHandler( String expectedResultString, String expectedEncoding,
                                                  AtomicBoolean testStatus ) {
    return httpExchange -> {
      try {
        checkEncoding( expectedResultString, expectedEncoding, httpExchange.getRequestBody() );
        testStatus.set( true );
      } catch ( Throwable e ) {
        e.printStackTrace();
        testStatus.set( false );
      } finally {
        httpExchange.sendResponseHeaders( 200, 0 );
        httpExchange.close();
      }
    };
  }

  private void checkEncoding( String expectedResult, String encoding, InputStream inputStream ) throws Exception {
    byte[] receivedBytes = ByteStreams.toByteArray( inputStream );
    String urlEncodedString = new String( receivedBytes, "US-ASCII" );
    String finalString = URLDecoder.decode( urlEncodedString, encoding );
    expectedResult = "testBodyParam=" + expectedResult;
    assertEquals( "The final received string is not the same", expectedResult, finalString );
  }

}
