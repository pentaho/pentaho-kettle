package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.engine.api.IData;
import org.pentaho.di.engine.api.IDataEvent;
import org.pentaho.di.engine.api.IExecutableOperation;
import org.pentaho.di.engine.api.IPDIEventSource;

public class KettleDataEvent implements IDataEvent {

  final private IData data;
  private final RowMetaInterface rowMeta;
  private final IExecutableOperation operation;
  private final STATE state;

  private KettleDataEvent(
    IExecutableOperation operation, RowMetaInterface rowMeta, Object[] values, STATE state ) {
    this.operation = operation;
    data = () -> values;
    this.rowMeta = rowMeta;
    this.state = state;
  }

  public static KettleDataEvent active( IExecutableOperation operation, RowMetaInterface rowMeta, Object[] values ) {
    return new KettleDataEvent( operation, rowMeta, values, STATE.ACTIVE );
  }

  public static KettleDataEvent complete( IExecutableOperation operation ) {
    return new KettleDataEvent( operation, null, null, STATE.COMPLETE );
  }

  /**
   * An event without content.  Corresponds to a call to .onNext to a source operation
   */
  public static KettleDataEvent empty() {
    return new KettleDataEvent( null, null, null, STATE.EMPTY );
  }

  @Override public TYPE getType() {
    return null;
  }

  @Override public STATE getState() {
    return state;
  }

  @Override public IData getData() {
    return data;
  }

  @Override public IPDIEventSource getEventSource() {
    return operation;
  }

  public RowMetaInterface getRowMeta() {
    return rowMeta;
  }
}
