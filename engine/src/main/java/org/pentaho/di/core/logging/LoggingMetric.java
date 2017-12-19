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

package org.pentaho.di.core.logging;

import org.pentaho.di.core.metrics.MetricsSnapshotInterface;

/**
 * Just a small wrapper class to allow us to pass a few extra details along with a metrics snapshot (like the batch id)
 *
 * @author matt
 *
 */
public class LoggingMetric {
  private long batchId;
  private MetricsSnapshotInterface snapshot;

  /**
   * @param batchId
   * @param snapshot
   */
  public LoggingMetric( long batchId, MetricsSnapshotInterface snapshot ) {
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
  public void setBatchId( long batchId ) {
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
  public void setSnapshot( MetricsSnapshotInterface snapshot ) {
    this.snapshot = snapshot;
  }

}
