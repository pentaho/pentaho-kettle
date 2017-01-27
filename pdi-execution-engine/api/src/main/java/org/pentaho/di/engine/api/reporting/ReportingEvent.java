package org.pentaho.di.engine.api.reporting;

import org.pentaho.di.engine.api.events.PDIEvent;
import org.pentaho.di.engine.api.model.LogicalModelElement;

import java.io.Serializable;

/**
 * Created by hudak on 1/11/17.
 */
public interface ReportingEvent<S extends LogicalModelElement, D extends Serializable> extends PDIEvent {
  S getSource();

  D getData();
}
