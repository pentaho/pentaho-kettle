package org.pentaho.di.core.logging;

import org.pentaho.di.core.metrics.MetricsSnapshotInterface;

/**
 * Just a small wrapper class to allow us to pass a few extra details along with
 * a metrics snapshot (like the batch id)
 * 
 * @author matt
 * 
 */
public class LoggingMetric {
  private long                     batchId;
  private MetricsSnapshotInterface snapshot;

  /**
   * @param batchId
   * @param snapshot
   */
  public LoggingMetric(long batchId, MetricsSnapshotInterface snapshot) {
    this.batchId = batchId;
    this.snapshot = snapshot;
  }

  /**
   * @return the batchId
   */
  public long getBatchId() {
    return batchId;
  }

  /**
   * @param batchId
   *          the batchId to set
   */
  public void setBatchId(long batchId) {
    this.batchId = batchId;
  }

  /**
   * @return the snapshot
   */
  public MetricsSnapshotInterface getSnapshot() {
    return snapshot;
  }

  /**
   * @param snapshot
   *          the snapshot to set
   */
  public void setSnapshot(MetricsSnapshotInterface snapshot) {
    this.snapshot = snapshot;
  }

}
