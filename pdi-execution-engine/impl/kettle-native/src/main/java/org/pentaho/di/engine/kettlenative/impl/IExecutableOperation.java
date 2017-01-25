package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.engine.api.events.IDataEvent;
import org.pentaho.di.engine.api.model.IOperation;
import org.pentaho.di.engine.api.events.IPDIEventSink;
import org.pentaho.di.engine.api.events.IPDIEventSource;
import org.pentaho.di.engine.api.reporting.Metrics;

import java.util.List;

/**
 * IExecutableOperation is the materialized version of
 * an IOperation.  It represents the mapping of the
 * structural specification of the op to it's concrete
 * executable form, as applicable to the IEngine in which
 * it is running.
 * <p>
 * An IExecutableOperation encapsulates a function capable of transforming input
 * tuples received "From" parent operations and published
 * "To" child ops.
 */
public interface IExecutableOperation extends IPDIEventSource<IDataEvent>, IPDIEventSink<IDataEvent> {

  void start();

  IOperation getParent();

  Metrics getMetrics();

}
