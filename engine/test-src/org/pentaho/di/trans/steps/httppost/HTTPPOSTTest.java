package org.pentaho.di.trans.steps.httppost;

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
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertTrue;

/**
 * User: Dzmitry Stsiapanau Date: 12/2/13 Time: 4:35 PM
 */
public class HTTPPOSTTest {
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
    httpServer.stop( 0 );

  }

  @Test
  public void test204Answer() throws Exception {
    HTTPPOSTData data = new HTTPPOSTData();
    Object[] expectedRow = new Object[] { "", 204L, null, null, null, null, null, null, null, null, null, null };
    HTTPPOST HTTPPOST =
        new HTTPPOSTHandler( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    HTTPPOST.setInputRowMeta( inputRowMeta );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    when( stepMockHelper.processRowsStepMetaInterface.getUrl() ).thenReturn( HTTP_LOCALHOST_9998 );
    when( stepMockHelper.processRowsStepMetaInterface.getQueryField() ).thenReturn( new String[] {} );
    when( stepMockHelper.processRowsStepMetaInterface.getArgumentField() ).thenReturn( new String[] {} );
    when( stepMockHelper.processRowsStepMetaInterface.getResultCodeFieldName() ).thenReturn( "ResultCodeFieldName" );
    when( stepMockHelper.processRowsStepMetaInterface.getFieldName() ).thenReturn( "ResultFieldName" );
    HTTPPOST.init( stepMockHelper.processRowsStepMetaInterface, data );
    assertTrue( HTTPPOST.processRow( stepMockHelper.processRowsStepMetaInterface, data ) );
    System.out.println( Arrays.toString( expectedRow ) );
    Object[] out = ( (HTTPPOSTHandler) HTTPPOST ).getOutputRow();
    System.out.println( Arrays.toString( out ) );
    assertTrue( Arrays.equals( expectedRow, out ) );
  }

  private void startHttp204Answer() throws IOException {
    httpServer = HttpServer.create( new InetSocketAddress( HTTPPOSTTest.host, HTTPPOSTTest.port ), 10 );
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
