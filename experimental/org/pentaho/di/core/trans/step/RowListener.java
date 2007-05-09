package org.pentaho.di.core.trans.step;


public interface RowListener
{
    public void rowReadEvent(Object[] row);
    public void rowWrittenEvent(Object[] row);
    public void errorRowWrittenEvent(Object[] row);
}
