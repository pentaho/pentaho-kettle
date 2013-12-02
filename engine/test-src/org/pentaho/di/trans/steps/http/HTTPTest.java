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

package org.pentaho.di.trans.steps.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.pentaho.di.core.util.Assert.assertTrue;

/**
 * User: Dzmitry Stsiapanau Date: 12/2/13 Time: 1:24 PM
 */
public class HTTPTest {

  private class HTTPHandler extends HTTP {

    Object[] row = new Object[] { "anyData" };
    Object[] outputRow;

    public HTTPHandler( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
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
  private StepMockHelper<HTTPMeta, HTTPData> stepMockHelper;
  private HttpServer httpServer;

  @Before
  public void setUp() throws Exception {
    stepMockHelper = new StepMockHelper<HTTPMeta, HTTPData>( "HTTP CLIENT TEST", HTTPMeta.class, HTTPData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    verify( stepMockHelper.trans, never() ).stopAll();
    startHttp204Answer();
  }

  @After
  public void tearDown() throws Exception {
    httpServer.stop( 0 );

  }

  @Test
  public void test204Answer() throws Exception {
    HTTPData data = new HTTPData();
    Object[] expectedRow = new Object[] { "", 204L, null, null, null, null, null, null, null, null, null, null };
    HTTP http = new HTTPHandler( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    http.setInputRowMeta( inputRowMeta );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    when( stepMockHelper.processRowsStepMetaInterface.getUrl() ).thenReturn( HTTP_LOCALHOST_9998 );
    when( stepMockHelper.processRowsStepMetaInterface.getHeaderField() ).thenReturn( new String[] {} );
    when( stepMockHelper.processRowsStepMetaInterface.getArgumentField() ).thenReturn( new String[] {} );
    when( stepMockHelper.processRowsStepMetaInterface.getResultCodeFieldName() ).thenReturn( "ResultCodeFieldName" );
    when( stepMockHelper.processRowsStepMetaInterface.getFieldName() ).thenReturn( "ResultFieldName" );
    http.init( stepMockHelper.processRowsStepMetaInterface, data );
    assertTrue( http.processRow( stepMockHelper.processRowsStepMetaInterface, data ) );
    System.out.println( Arrays.toString( expectedRow ) );
    Object[] out = ( (HTTPHandler) http ).getOutputRow();
    System.out.println( Arrays.toString( out ) );
    assertTrue( Arrays.equals( expectedRow, out ) );
  }

  private void startHttp204Answer() throws IOException {
    httpServer = HttpServer.create( new InetSocketAddress( HTTPTest.host, HTTPTest.port ), 10 );
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
