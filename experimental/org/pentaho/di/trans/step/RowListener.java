package org.pentaho.di.trans.step;

import org.pentaho.di.core.row.RowMetaInterface;


public interface RowListener
{
    public void rowReadEvent(RowMetaInterface rowMeta, Object[] row);
    public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row);
    public void errorRowWrittenEvent(RowMetaInterface rowMeta, Object[] row);
}
