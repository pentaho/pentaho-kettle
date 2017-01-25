package org.pentaho.di.engine.api.model;

import org.pentaho.di.engine.api.reporting.IReportingEvent;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.List;

/**
 * Created by nbaker on 1/18/17.
 */
public interface IMaterializedModelElement extends IModelElement  {

  <D extends Serializable> List<Publisher<? extends IReportingEvent>> getPublisher( Class<D> type );

  <D extends Serializable> List<Serializable> getEventTypes();

  ILogicalModelElement getLogicalElement();

  /**
   * Called right before the transformation will be executed.
   */
  void init();
}
