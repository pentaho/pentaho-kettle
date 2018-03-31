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

package org.pentaho.di.trans.steps.prioritizestreams;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

@RunWith( Parameterized.class )
public class PrioritizeStreamsExecutionIT {

  private String old_timeout_get;

  private static StepMockHelper<PrioritizeStreamsMeta, StepDataInterface> stepMockHelper;
  private static PrioritizeStreamsMeta meta;
  private TestData code;

  private static ExecutorService service = Executors.newCachedThreadPool();

  public PrioritizeStreamsExecutionIT( TestData code ) {
    this.code = code;
  }

  @BeforeClass
  public static void setup() {
    stepMockHelper =
        new StepMockHelper<PrioritizeStreamsMeta, StepDataInterface>( "Priority Streams Test",
            PrioritizeStreamsMeta.class, StepDataInterface.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    meta = mock( PrioritizeStreamsMeta.class );
  }

  @AfterClass
  public static void tearDown() {
    stepMockHelper.cleanUp();
    service.shutdown();
  }

  @Before
  public void before() {
    old_timeout_get = System.getProperty( Const.KETTLE_ROWSET_GET_TIMEOUT );
    // 1 sec
    System.setProperty( Const.KETTLE_ROWSET_GET_TIMEOUT, "1000" );
  }

  @After
  public void after() {
    if ( old_timeout_get != null ) {
      System.setProperty( Const.KETTLE_ROWSET_GET_TIMEOUT, old_timeout_get );
    }
  }

  enum TestData {
    ABC( "aaabbbccc" ), BAC( "bbbaaaccc" ), BCA( "bbbcccaaa" ), CAB( "cccaaabbb" ), CBA( "cccbbbaaa" );

    private String res;

    TestData( String res ) {
      this.res = res;
    }

    public String toString() {
      return res;
    }
  }

  @Parameterized.Parameters
  public static Collection<Object[]> primeNumbers() {
    TestData[] td = TestData.values();
    Object[][] ret = new Object[td.length][1];
    for ( int i = 0; i < ret.length; i++ ) {
      ret[i] = new Object[] { td[i] };
    }
    return Arrays.asList( ret );
  }

  /**
   * Test that priority streams step does respect input streams priority and input streams delay with their put row
   * method.
   *
   * Please pay attention, this test is based on concurrent execution environment,
   * so put input rowset delay should correlate with KETTLE_ROWSET_GET_TIMEOUT value.
   *
   * @throws KettleException
   */
  @Test
  public void testProcessRow() throws KettleException {
    PrioritizeStreamsData data = getTestData( code );
    PrioritizeStreamsInner ps = new PrioritizeStreamsInner( stepMockHelper );
    ps.first = false;
    for ( int i = 0; i < 9; i++ ) {
      ps.processRow( meta, data );
    }

    Assert.assertEquals( "Output stream collect all rows: " + ps.toString(), 9, ps.output.size() );
    Assert.assertEquals( "Stream output respect priority", code.toString(), ps.toString() );
  }

  private PrioritizeStreamsData getTestData( TestData code ) {
    PrioritizeStreamsData retData = new PrioritizeStreamsData();

    RowSet in1 = new BlockingRowSet( 4 );
    RowSet in2 = new BlockingRowSet( 4 );
    RowSet in3 = new BlockingRowSet( 4 );
    Object[][] data1 = { { "a" }, { "a" }, { "a" } };
    Object[][] data2 = { { "b" }, { "b" }, { "b" } };
    Object[][] data3 = { { "c" }, { "c" }, { "c" } };
    RowMetaInterface rmi = new RowMeta();

    Runnable stream1 = this.getInputProduser( in1, rmi, data1 );
    Runnable stream2 = this.getInputProduser( in2, rmi, data2 );
    Runnable stream3 = this.getInputProduser( in3, rmi, data3 );

    retData.outputRowMeta = rmi;

    switch ( code ) {
      case ABC: {
        retData.rowSets = new RowSet[] { in1, in2, in3 };
        break;
      }
      case BAC: {
        retData.rowSets = new RowSet[] { in2, in1, in3 };
        break;
      }
      case BCA: {
        retData.rowSets = new RowSet[] { in2, in3, in1 };
        break;
      }
      case CAB: {
        retData.rowSets = new RowSet[] { in3, in1, in2 };
        break;
      }
      case CBA: {
        retData.rowSets = new RowSet[] { in3, in2, in1 };
        break;
      }
      default: {
        Assert.fail( "This test data does not supported: " + code.toString() );
      }
    }

    retData.currentRowSet = retData.rowSets[0];

    retData.stepnr = 0;
    retData.stepnrs = 3;

    service.execute( stream1 );
    service.execute( stream2 );
    service.execute( stream3 );

    return retData;
  }

  private Runnable getInputProduser( final RowSet in, final RowMetaInterface rmi, final Object[][] data ) {
    return new InputProducer( in, rmi, data );
  }

  /**
   * This class simulates working delay before actual data will be available for
   * input stream. Pay attention to KETTLE_ROWSET_GET_TIMEOUT value. Default
   * value can be too small - and empty (even not started to fill) rowset will
   * be treated as empty (returns 'null' for offer method call on blocking queue).
   *
   */
  private class InputProducer implements Runnable {

    private Queue<Object[]> data;
    private RowSet rs;
    private RowMetaInterface rmi;

    private boolean first = true;

    InputProducer( RowSet in, RowMetaInterface rmi, Object[][] data ) {
      this.data = new LinkedList<Object[]>();
      this.data.addAll( Arrays.asList( data ) );
      this.rs = in;
      this.rmi = rmi;
    }

    @Override
    public void run() {
      while ( data.peek() != null ) {
        // first row always came with delay
        if ( first ) {
          first = false;
          try {
            Thread.sleep( 50 );
          } catch ( InterruptedException e ) {
            // this will never happens
            e.printStackTrace();
          }
        }
        Object[] putTo = data.poll();
        rs.putRowWait( rmi, putTo, 20, TimeUnit.SECONDS );
      }
      rs.setDone();
    }
  }

  private class PrioritizeStreamsInner extends PrioritizeStreams {

    List<Object[]> output = new ArrayList<Object[]>();

    public PrioritizeStreamsInner( StepMockHelper<PrioritizeStreamsMeta, StepDataInterface> stepMockHelper ) {
      super( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
          stepMockHelper.trans );
    }

    @Override
    public Object[] getRowFrom( RowSet rs ) {
      return rs.getRow();
    }

    @Override
    public void putRow( RowMetaInterface rmi, Object[] row ) {
      output.add( row );
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      Iterator<Object[]> it = output.iterator();
      while ( it.hasNext() ) {
        String val = String.class.cast( it.next()[0] );
        sb.append( val );
      }
      return sb.toString();
    }
  }
}
