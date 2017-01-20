package org.pentaho.di.engine.api.reporting;

import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Created by nbaker on 1/18/17.
 */
public interface IMaterializedModelElement extends IModelElement  {

  <D extends Serializable> Optional<Publisher> getPublisher( Class<D> type );

  <D extends Serializable> List<Serializable> getEventTypes();

  ILogicalModelElement getLogicalElement();

  /**
   * Called right before the transformation will be executed.
   */
  void init();
}
