package org.pentaho.pdi.core.row;

import java.util.ArrayList;
import java.util.List;

import be.ibridge.kettle.core.exception.KettleValueException;

public class RowMeta implements RowMetaInterface
{
    private List valueMetaList;

    public RowMeta()
    {
        valueMetaList = new ArrayList();
    }

    /**
     * @return the valueMeta
     */
    public List getValueMeta()
    {
        return valueMetaList;
    }

    /**
     * @param valueMeta the valueMeta to set
     */
    public void setValueMeta(List valueMeta)
    {
        this.valueMetaList = valueMeta;
    }

    /**
     * Add a metadata value, extends the array if needed.
     * 
     * @param meta The metadata value to add
     */
    public void addMetaValue(ValueMetaInterface meta)
    {
        valueMetaList.add(meta);
    }
    
    /**
     * Get the value metadata on the specified index.
     * @param index The index to get the value metadata from
     * @return The value metadata specified by the index.
     */
    public ValueMetaInterface getValueMeta(int index)
    {
        return (ValueMetaInterface) valueMetaList.get(index);
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
        ValueMetaInterface meta = (ValueMetaInterface) valueMetaList.get(index);
        
        // We need to convert the stored data type to String
        // We use a method in the ValueMetaInterface to convert to String
        return meta.convertToString(dataRow[index]);
    }
    
    
    /**
     * @return a cloned Object[] object.
     * @throws KettleValueException in case something is not quite right with the expected data
     */
    public Object[] cloneRow(Object[] objects) throws KettleValueException
    {
        Object[] newObjects = new Object[objects.length];
        for (int i=0;i<objects.length;i++)
        {
            if (objects[i]==null)
            {
                newObjects[i] = null; 
            }
            else
            {
                ValueMetaInterface valueMeta = getValueMeta(i);
                newObjects[i] = valueMeta.cloneValueData(objects[i]);
            }
        }
        return newObjects;
    }

}
