package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.engine.api.events.DataEvent;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.events.PDIEventSink;
import org.pentaho.di.engine.api.events.PDIEventSource;
import org.pentaho.di.engine.api.reporting.Metrics;

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
public interface IExecutableOperation extends PDIEventSource<DataEvent>, PDIEventSink<DataEvent> {

  void start();

  Operation getParent();

  Metrics getMetrics();

}
