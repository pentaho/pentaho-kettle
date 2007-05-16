package org.pentaho.di.core;

import org.pentaho.di.core.row.RowMetaInterface;

import be.ibridge.kettle.core.exception.KettleValueException;

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

    public int hashCode()
    {
        try
        {
            return rowMeta.hashCode(data);
        }
        catch(KettleValueException e)
        {
            throw new RuntimeException("Row metadata and data: unable to calculate hashcode because of a data conversion problem", e);
        }
    }
    
    public boolean equals(Object obj)
    {
        try
        {
            return rowMeta.compare(data, ((RowMetaAndData)obj).getData())==0;
        }
        catch (KettleValueException e)
        {
            throw new RuntimeException("Row metadata and data: unable to compare rows because of a data conversion problem", e);
        }
    }
}
