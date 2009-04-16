package org.pentaho.di.trans.steps.infobrightoutput;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.infobright.etl.model.BrighthouseRecord;
import com.infobright.etl.model.ValueConverterException;

/**
 * @author geoffrey.falk@infobright.com
 */
public class KettleRecordPopulator {

  private KettleValueConverter[] conv = null;
  
  public void populate(BrighthouseRecord record, Object[] row, RowMetaInterface rowMeta)
      throws KettleValueException {

    // assume row metadata is same for all rows
    if (conv == null) {
      init(rowMeta);
    }
    
    for (int colidx = 0; colidx < record.size(); colidx++) {
      Object value = row[colidx];
      try {
        record.setData(colidx, value, conv[colidx]);
      } catch (ValueConverterException e) {
        Throwable cause = e.getCause();
        if (cause instanceof KettleValueException) {
          throw (KettleValueException) cause;
        } else if (cause instanceof RuntimeException) {
          throw (RuntimeException) cause;
        } else if (cause instanceof Error) {
          throw (Error) cause;
        } else {
          throw e;
        }
      }
    }
  }

  private void init(RowMetaInterface rowMeta) {
    int size = rowMeta.size();
    conv = new KettleValueConverter[size];
    for (int colidx = 0; colidx < size; colidx++) {
      ValueMetaInterface meta = rowMeta.getValueMeta(colidx);
      conv[colidx] = new KettleValueConverter(meta);
    }
  }
}
