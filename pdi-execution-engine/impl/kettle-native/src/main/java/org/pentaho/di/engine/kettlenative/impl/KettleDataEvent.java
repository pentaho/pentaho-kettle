package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.engine.api.IData;
import org.pentaho.di.engine.api.IDataEvent;
import org.pentaho.di.engine.api.IPDIEventSource;

public class KettleDataEvent implements IDataEvent {

  final private IData data;
  private final RowMetaInterface rowMeta;

  public KettleDataEvent( RowMetaInterface rowMeta, Object[] values ) {
    data = () -> values;
    this.rowMeta = rowMeta;
  }

  @Override public TYPE getType() {
    return null;
  }

  @Override public IData getData() {
    return data;
  }

  @Override public IPDIEventSource getEventSource() {
    return null;
  }

  public RowMetaInterface getRowMeta() {
    return rowMeta;
  }
}
