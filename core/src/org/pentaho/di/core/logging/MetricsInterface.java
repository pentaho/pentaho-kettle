package org.pentaho.di.core.logging;

import org.pentaho.di.core.metrics.MetricsSnapshotType;

public interface MetricsInterface {
  public String getCode();
  public String getDescription();
  public MetricsSnapshotType getType();
}
