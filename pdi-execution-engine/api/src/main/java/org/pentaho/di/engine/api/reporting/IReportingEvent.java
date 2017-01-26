package org.pentaho.di.engine.api.reporting;

import org.pentaho.di.engine.api.events.IPDIEvent;
import org.pentaho.di.engine.api.model.ILogicalModelElement;

import java.io.Serializable;

/**
 * Created by hudak on 1/11/17.
 */
public interface IReportingEvent<S extends ILogicalModelElement, D extends Serializable> extends IPDIEvent {
  S getSource();

  D getData();
}
