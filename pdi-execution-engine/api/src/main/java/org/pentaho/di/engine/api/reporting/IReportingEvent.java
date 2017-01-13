package org.pentaho.di.engine.api.reporting;

import org.pentaho.di.engine.api.IPDIEvent;

import java.io.Serializable;

/**
 * Created by hudak on 1/11/17.
 */
public interface IReportingEvent<S extends IReportingEventSource, D extends Serializable> extends IPDIEvent {
  S getSource();

  D getData();
}
