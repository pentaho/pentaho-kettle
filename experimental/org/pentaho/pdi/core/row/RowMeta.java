package org.pentaho.pdi.core.row;

import java.util.ArrayList;
import java.util.List;

import be.ibridge.kettle.core.exception.KettleValueException;

public class RowMeta implements RowMetaInterface
{
    private List valueMeta;

    public RowMeta()
    {
        valueMeta = new ArrayList();
    }

    /**
     * @return the valueMeta
     */
    public List getValueMeta()
    {
        return valueMeta;
    }

    /**
     * @param valueMeta the valueMeta to set
     */
    public void setValueMeta(List valueMeta)
    {
        this.valueMeta = valueMeta;
    }

    /**
     * Add a metadata value, extends the array if needed.
     * 
     * @param meta The metadata value to add
     */
    public void addMetaValue(ValueMetaInterface meta)
    {
        valueMeta.add(meta);
    }
    
    /**
     * Get a String value from a row of data.  Convert data if this needed. 
     * 
     * @param rowRow the row of data
     * @param index the index
     * @return The string found on that position in the row
     * @throws KettleValueException in case there was a problem converting the data.
     */
    public String getString(Object[] dataRow, int index) throws KettleValueException
    {
        ValueMetaInterface meta = (ValueMetaInterface) valueMeta.get(index);
        
        // If the metadata says it's a String, we can simply cast it and go with that.
        if (meta.getType()==ValueMetaInterface.TYPE_STRING) 
        {
            return (String)dataRow[index];
        }
        else
        {
            // We need to convert the stored data type to String
            // We use a method in the ValueMetaInterface to convert to String
            return meta.convertToString(dataRow[index]);
        }
    }
}
