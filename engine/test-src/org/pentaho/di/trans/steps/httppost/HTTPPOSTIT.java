/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.io.ByteStreams;
import com.sun.net.httpserver.HttpExchange;
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
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Assert;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.BooleanLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveBooleanArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * User: Dzmitry Stsiapanau Date: 12/2/13 Time: 4:35 PM
 */
public class HTTPPOSTIT {
  private class HTTPPOSTHandler extends HTTPPOST {

    Object[] row = new Object[] { "anyData" };
    Object[] outputRow;

    public HTTPPOSTHandler( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
      super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
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
    Mockito.when( stepMockHelper.logChannelInterfaceFactory.create( Mockito.any(), Mockito.any( LoggingObjectInterface.class ) ) ).thenReturn(
      stepMockHelper.logChannelInterface );
    Mockito.when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    Mockito.verify( stepMockHelper.trans, Mockito.never() ).stopAll();
  }

  @After
  public void tearDown() throws Exception {
    if ( httpServer != null ) {
      httpServer.stop( 5 );
    }
  }

  @Test
  public void test204Answer() throws Exception {
    startHttpServer( get204AnswerHandler() );
    HTTPPOSTData data = new HTTPPOSTData();
    Object[] expectedRow = new Object[] { "", 204L, null, null, null, null, null, null, null, null, null, null };
    HTTPPOST HTTPPOST =
      new HTTPPOSTHandler( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans );
    RowMetaInterface inputRowMeta = Mockito.mock( RowMetaInterface.class );
    HTTPPOST.setInputRowMeta( inputRowMeta );
    Mockito.when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getUrl() ).thenReturn( HTTP_LOCALHOST_9998 );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getQueryField() ).thenReturn( new String[] {} );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getArgumentField() ).thenReturn( new String[] {} );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getResultCodeFieldName() ).thenReturn( "ResultCodeFieldName" );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getFieldName() ).thenReturn( "ResultFieldName" );
    HTTPPOST.init( stepMockHelper.processRowsStepMetaInterface, data );
    Assert.assertTrue( HTTPPOST.processRow( stepMockHelper.processRowsStepMetaInterface, data ) );
    System.out.println( Arrays.toString( expectedRow ) );
    Object[] out = ( (HTTPPOSTHandler) HTTPPOST ).getOutputRow();
    System.out.println( Arrays.toString( out ) );
    Assert.assertTrue( Arrays.equals( expectedRow, out ) );
  }

  private void startHttpServer( HttpHandler httpHandler ) throws IOException {
    httpServer = HttpServer.create( new InetSocketAddress( HTTPPOSTIT.host, HTTPPOSTIT.port ), 10 );
    httpServer.createContext( "/", httpHandler );
    httpServer.start();
  }

  @Test
  public void testLoadSaveRoundTrip() throws KettleException {
    List<String> attributes =
        Arrays.asList( "postAFile", "encoding", "url", "urlInField", "urlField", "requestEntity", "httpLogin",
            "httpPassword", "proxyHost", "proxyPort", "socketTimeout", "connectionTimeout",
            "closeIdleConnectionsTime", "argumentField", "argumentParameter", "argumentHeader", "queryField",
            "queryParameter", "fieldName", "resultCodeFieldName", "responseTimeFieldName" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
        new HashMap<String, FieldLoadSaveValidator<?>>();

    //Arrays need to be consistent length
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 );
    FieldLoadSaveValidator<boolean[]> booleanArrayLoadSaveValidator =
        new PrimitiveBooleanArrayLoadSaveValidator( new BooleanLoadSaveValidator(), 25 );
    fieldLoadSaveValidatorAttributeMap.put( "argumentField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "argumentParameter", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "argumentHeader", booleanArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "queryField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "queryParameter", stringArrayLoadSaveValidator );

    LoadSaveTester loadSaveTester =
        new LoadSaveTester( HTTPPOSTMeta.class, attributes, new HashMap<String, String>(),
            new HashMap<String, String>(), fieldLoadSaveValidatorAttributeMap,
            new HashMap<String, FieldLoadSaveValidator<?>>() );

    loadSaveTester.testRepoRoundTrip();
    loadSaveTester.testXmlRoundTrip();
  }

  public void testServerReturnsCorrectlyEncodedParams( String testString, String testCharset ) throws Exception {
    AtomicBoolean testStatus = new AtomicBoolean();
    startHttpServer( getEncodingCheckingHandler( testString, testCharset, testStatus ) );
    HTTPPOSTData data = new HTTPPOSTData();
    RowMeta meta = new RowMeta();
    meta.addValueMeta( new ValueMetaString( "fieldName" ) );
    HTTPPOSTHandler httpPost = new HTTPPOSTHandler(
      stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans );
    RowMetaInterface inputRowMeta = Mockito.mock( RowMetaInterface.class );
    httpPost.setInputRowMeta( inputRowMeta );
    httpPost.row = new Object[] { testString };
    Mockito.when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    Mockito.when( inputRowMeta.getString( httpPost.row, 0 ) ).thenReturn( testString );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getUrl() ).thenReturn( HTTP_LOCALHOST_9998 );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getQueryField() ).thenReturn( new String[] {} );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getArgumentField() ).thenReturn( new String[] { "testBodyField" } );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getArgumentParameter() ).thenReturn( new String[] { "testBodyParam" } );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getArgumentHeader() ).thenReturn( new boolean[] { false } );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getFieldName() ).thenReturn( "ResultFieldName" );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getEncoding() ).thenReturn( testCharset );
    httpPost.init( stepMockHelper.processRowsStepMetaInterface, data );
    Assert.assertTrue( httpPost.processRow( stepMockHelper.processRowsStepMetaInterface, data ) );
    Assert.assertTrue( testStatus.get(), "Test failed" );
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

  private HttpHandler get204AnswerHandler() {
    return new HttpHandler() {
      @Override
      public void handle( HttpExchange httpExchange ) throws IOException {
        httpExchange.sendResponseHeaders( 204, 0 );
        httpExchange.close();
      }
    };
  }

  private HttpHandler getEncodingCheckingHandler( final String expectedResultString, final String expectedEncoding, final AtomicBoolean testStatus ) {
    return new HttpHandler() {
      @Override
      public void handle( HttpExchange httpExchange ) throws IOException {
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
      }
    };
  }

  private void checkEncoding( String expectedResult, String encoding, InputStream inputStream ) throws Exception {
    byte[] receivedBytes = ByteStreams.toByteArray( inputStream );
    String urlEncodedString = new String( receivedBytes, "US-ASCII" );
    String finalString = URLDecoder.decode( urlEncodedString, encoding );
    expectedResult = "testBodyParam=" + expectedResult;
    Assert.assertTrue( expectedResult.equals( finalString ), "The final received string is not the same" );
  }
}
