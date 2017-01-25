package org.pentaho.di.engine.kettlenative.impl;

import com.google.common.collect.ImmutableList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.engine.api.model.IRow;
import org.pentaho.di.engine.api.events.IDataEvent;
import org.pentaho.di.engine.api.events.IPDIEventSource;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class KettleDataEvent implements IDataEvent, Serializable {

  final private List<IRow> rows;
  private  final RowMetaInterface rowMeta;
  private final IExecutableOperation operation;
  private final STATE state;

  private KettleDataEvent(
    IExecutableOperation operation, RowMetaInterface rowMeta, Object[] values, STATE state ) {
    this.operation = operation;
    rows = state == STATE.ACTIVE ? ImmutableList.of( new KettleRow( rowMeta, values ) ) : Collections.emptyList();
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

  @Override public List<IRow> getRows() {
    return rows;
  }

  @Override public IPDIEventSource<IDataEvent> getEventSource() {
    return operation;
  }

  public RowMetaInterface getRowMeta() {
    return rowMeta;
  }
}
