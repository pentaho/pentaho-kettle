package org.pentaho.di.engine.api.reporting;

import org.pentaho.di.engine.api.model.LogicalModelElement;

/**
 * Created by nbaker on 1/17/17.
 */
public class StatusEvent<S extends LogicalModelElement> implements ReportingEvent<S, Status> {
  private final S source;
  private final Status status;

  public StatusEvent( S source, Status status ) {
    this.source = source;
    this.status = status;
  }

  @Override public S getSource() {
    return source;
  }

  @Override public Status getData() {
    return status;
  }
}
