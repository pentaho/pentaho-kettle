package org.pentaho.di.core;

import org.pentaho.di.core.row.RowMetaInterface;

public class RowMetaAndData
{
    private RowMetaInterface rowMeta;

    private Object[]         data;

    public RowMetaAndData()
    {
    }
    
    /**
     * @param rowMeta
     * @param data
     */
    public RowMetaAndData(RowMetaInterface rowMeta, Object[] data)
    {
        this.rowMeta = rowMeta;
        this.data = data;
    }

    /**
     * @return the data
     */
    public Object[] getData()
    {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Object[] data)
    {
        this.data = data;
    }

    /**
     * @return the rowMeta
     */
    public RowMetaInterface getRowMeta()
    {
        return rowMeta;
    }

    /**
     * @param rowMeta the rowMeta to set
     */
    public void setRowMeta(RowMetaInterface rowMeta)
    {
        this.rowMeta = rowMeta;
    }

}
