package org.pentaho.di.engine.api.model;

import org.pentaho.di.engine.api.reporting.ReportingEvent;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.List;

/**
 * Created by nbaker on 1/18/17.
 */
public interface MaterializedModelElement extends ModelElement {

  <D extends Serializable> List<Publisher<? extends ReportingEvent>> getPublisher( Class<D> type );

  <D extends Serializable> List<Serializable> getEventTypes();

  LogicalModelElement getLogicalElement();

  /**
   * Called right before the transformation will be executed.
   */
  void init();
}
