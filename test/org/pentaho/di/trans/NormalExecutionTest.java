/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans;

import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepInterface;

public class NormalExecutionTest extends TestCase {
  
  private boolean initCalled = false;
  
  protected synchronized void setUp() throws Exception {
    if (initCalled) { return; }
    KettleEnvironment.init();
    initCalled = true;
    CentralLogStore.getAppender().setMaxNrLines(100);
  }
  
  /**
   * Mock {@link RowListener} that totals the number of events.  Can be stopped with {@code close()}.
   */
  private class CountingRowListener extends RowAdapter {
    private Long error;

    private Long written;

    private Long read;

    private Long ignoredError;

    private Long ignoredWritten;

    private Long ignoredRead;

    private AtomicBoolean listening;

    /**
     * Creates a new counting row listener that is listening.
     */
    public CountingRowListener() {
      ignoredError = ignoredWritten = ignoredRead = error = written = read = 0L;
      listening = new AtomicBoolean(true);
    }

    public void setListening(final boolean b) {
      listening.set(b);
    }

    public boolean isListening() {
      return listening.get();
    }

    /**
     * @return The number of {@code errorRowWrittenEvent()} calls while {@code listening == true}
     */
    public Long getError() {
      return error;
    }

    /**
     * @return The number of {@code rowWrittenEvent()} calls while {@code listening == true}
     */
    public Long getWritten() {
      return written;
    }

    /**
     * @return The number of {@code rowReadEvent()} calls while {@code listening == true}
     */
    public Long getRead() {
      return read;
    }

    /**
     * @return The number of {@code errorRowWrittenEvent()} calls while {@code listening == false}
     */
    public Long getIgnoredError() {
      return ignoredError;
    }

    /**
     * @return The number of {@code rowWrittenEvent()} calls while {@code listening == false}
     */
    public Long getIgnoredWritten() {
      return ignoredWritten;
    }

    /**
     * @return The number of {@code rowReadEvent()} calls while {@code listening == false}
     */
    public Long getIgnoredRead() {
      return ignoredRead;
    }

    @Override
    public void errorRowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
      if (!isListening()) {
        ignoredError++;
      } else {
        error++;
      }
    }

    @Override
    public void rowReadEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
      if (!isListening()) {
        ignoredRead++;
      } else {
        read++;
      }
    }

    @Override
    public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
      if (!isListening()) {
        ignoredWritten++;
      } else {
        written++;
      }
    }
  }

  /**
   * Tests that all rows are written out to {@link RowListener}s before {@link Trans#waitUntilFinished} returns.
   */
  public void testWaitUntilFinished() throws Exception {
    // The number of rows we'll pump through our RowProducer.
    final Long ROWS = 10L;
    final int ITERATIONS = 100000;
    
    // Load transformation
    TransMeta transMeta = new TransMeta("testfiles/NormalExecutionTest - WaitUntilFinished.ktr"); //$NON-NLS-1$
    transMeta.setSizeRowset(5);

    for (int t=0;t<ITERATIONS;t++) {
      Trans trans = new Trans(transMeta);
      trans.setLogLevel(LogLevel.NOTHING);
  
      trans.prepareExecution(null);
  
      StepInterface injector = trans.findRunThread("Injector"); //$NON-NLS-1$
      StepInterface output = trans.findRunThread("Output"); //$NON-NLS-1$
  
      // Get the RowMeta for the injector step (it will be an Integer named 'a' of length 1)
      RowMeta injectorRowMeta = new RowMeta();
      ((BaseStepMeta) injector.getStepMeta().getStepMetaInterface()).getFields(injectorRowMeta, null, null, null, null);
  
      RowProducer producer = trans.addRowProducer(injector.getStepname(), 0);
  
      // Our counting row listener will record how many rows we receive
      CountingRowListener countingListener = new CountingRowListener();
      output.addRowListener(countingListener);
  
      // Feed input to transformation
      Object[] row = new Object[1];
      trans.startThreads();
      for (Integer i = 0; i < ROWS; i++) {
        row[0] = i;
        producer.putRow(injectorRowMeta, row);
      }
      producer.finished();
  
      trans.waitUntilFinished();  // This should block until everything is complete
      countingListener.setListening(false);
      
      assertTrue(trans.isFinished());
      
      // Make sure we collect all output so we can report how long it actually took
      long start = System.currentTimeMillis();
      while (countingListener.getWritten() + countingListener.getIgnoredWritten() != ROWS) {
        Thread.sleep(0, 10);
      }
      long end = System.currentTimeMillis();
      
      System.out.println("Run report for RowListener on last step in transformation, iteration #"+(t+1)+" :\n"); //$NON-NLS-1$
      System.out.println("Rows read             : " + countingListener.getRead()); //$NON-NLS-1$
      System.out.println("Rows written          : " + countingListener.getWritten()); //$NON-NLS-1$
      System.out.println("Rows error            : " + countingListener.getError()); //$NON-NLS-1$
      System.out.println("Rows ignored (read)   : " + countingListener.getIgnoredRead()); //$NON-NLS-1$
      System.out.println("Rows ignored (written): " + countingListener.getIgnoredWritten()); //$NON-NLS-1$
      System.out.println("Rows ignored (error)  : " + countingListener.getIgnoredError()); //$NON-NLS-1$
      System.out.println("Had to wait " + (end - start) + "ms for all data to be received by the row listener."); //$NON-NLS-1$ //$NON-NLS-2$
      
      assertEquals("Incorrect number of read rows received", ROWS, countingListener.getRead()); //$NON-NLS-1$
      assertEquals("Incorrect number of written rows received", ROWS, countingListener.getWritten()); //$NON-NLS-1$
      assertEquals("Incorrect number of error rows received", new Long(0), countingListener.getError()); //$NON-NLS-1$
    }
  }
  
  public void testStartThreads_only_one_TransListener() throws Exception {
    TransMeta transMeta = new TransMeta("testfiles/NormalExecutionTest - WaitUntilFinished.ktr"); //$NON-NLS-1$

    Trans trans = new Trans(transMeta);
    trans.setLogLevel(LogLevel.NOTHING);

    trans.prepareExecution(null);
    trans.startThreads();
    trans.waitUntilFinished();
    
    // Record original number of trans listeners
    int numTransListeners = trans.getTransListeners().size();
    
    trans.prepareExecution(null);
    trans.startThreads();
    trans.waitUntilFinished();
    
    assertEquals("TransListeners on Trans are growing", numTransListeners, trans.getTransListeners().size()); //$NON-NLS-1$
  }
}
