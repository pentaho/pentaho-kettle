package org.pentaho.di.trans;

import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;

/**
 * Tests for RowProducer
 *
 */
public class RowProducerTest extends TestCase {

  /**
   * Verify that putting a row into a RowProducer does 
   * not block if the underlying rowSet is not full.
   */
  public void testPutRow_not_full() {
    final int attempts = 1;
    MockBlockingRowSet rs = new MockBlockingRowSet(attempts);
    RowProducer rp = new RowProducer(null, rs);

    rp.putRow(null, null);

    assertEquals("Total attempts to putRow() exceeded expected amount", attempts, rs.getTotalAttempts()); //$NON-NLS-1$
  }

  /**
   * Verify that putting a row into a RowProducer blocks until the row  
   * is successfully added instead of returning immediately.
   */
  public void testPutRow_full() {
    final int attempts = 10;
    MockBlockingRowSet rs = new MockBlockingRowSet(attempts);
    RowProducer rp = new RowProducer(null, rs);

    rp.putRow(null, null);

    assertEquals("Total attempts to putRow() exceeded expected amount", attempts, rs.getTotalAttempts()); //$NON-NLS-1$
  }

  class MockBlockingRowSet implements RowSet {

    // The number of calls to putRowWait() that it requires to actually put a row.
    private final int reqdAttempts;

    // Number of times putRowWait() has been called.
    private int totalAttempts;

    /**
     * Create a blocking row set that requires {@code attempts} calls to 
     * {@link #putRowWait(RowMetaInterface, Object[], long, TimeUnit)} before actually adding the row. 
     * 
     * @param attempts Number of calls required to actually put a row.
     */
    public MockBlockingRowSet(int attempts) {
      this.reqdAttempts = attempts;
      totalAttempts = 0;
    }

    public int getTotalAttempts() {
      return totalAttempts;
    }

    public boolean putRow(RowMetaInterface rowMeta, Object[] rowData) {
      throw new UnsupportedOperationException();
    }

    public boolean putRowWait(RowMetaInterface rowMeta, Object[] rowData, long time, TimeUnit tu) {
      totalAttempts++;
      if (totalAttempts % reqdAttempts == 0) {
        return true;
      }
      try {
        Thread.sleep(10); // Simulate overhead of blocking
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
      return false;
    }

    public Object[] getRow() {
      throw new UnsupportedOperationException();
    }

    public Object[] getRowImmediate() {
      throw new UnsupportedOperationException();
    }

    public Object[] getRowWait(long timeout, TimeUnit tu) {
      throw new UnsupportedOperationException();
    }

    public void setDone() {
      throw new UnsupportedOperationException();
    }

    public boolean isDone() {
      throw new UnsupportedOperationException();
    }

    public String getOriginStepName() {
      throw new UnsupportedOperationException();
    }

    public int getOriginStepCopy() {
      throw new UnsupportedOperationException();
    }

    public String getDestinationStepName() {
      throw new UnsupportedOperationException();
    }

    public int getDestinationStepCopy() {
      throw new UnsupportedOperationException();
    }

    public String getName() {
      throw new UnsupportedOperationException();
    }

    public int size() {
      throw new UnsupportedOperationException();
    }

    public void setThreadNameFromToCopy(String from, int from_copy, String to, int to_copy) {
      throw new UnsupportedOperationException();
    }

    public RowMetaInterface getRowMeta() {
      throw new UnsupportedOperationException();
    }

    public void setRowMeta(RowMetaInterface rowMeta) {
      throw new UnsupportedOperationException();
    }

    public String getRemoteSlaveServerName() {
      throw new UnsupportedOperationException();
    }

    public void setRemoteSlaveServerName(String remoteSlaveServerName) {
      throw new UnsupportedOperationException();
    }

    public boolean isBlocking() {
      return true;
    }
  }
}
