package org.pentaho.di.trans.steps.simplemapping;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;

public interface PutRowInterface {
  public void putRow(RowMetaInterface rowMeta, Object[] rowData) throws KettleStepException;
}
