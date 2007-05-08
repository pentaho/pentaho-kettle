package org.pentaho.pdi.core.row;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.ibridge.kettle.core.exception.KettleValueException;

public class RowMeta implements RowMetaInterface
{
    private List valueMetaList;

    public RowMeta()
    {
        valueMetaList = new ArrayList();
    }
    
    public Object clone()
    {
        RowMeta rowMeta = new RowMeta();
        for (int i=0;i<size();i++)
        {
            ValueMetaInterface valueMeta = getValueMeta(i);
            rowMeta.addMetaValue( (ValueMetaInterface)valueMeta.clone() );
        }
        return rowMeta;
    }

    /**
     * @return the list of value Metadata 
     */
    public List getValueMetaList()
    {
        return valueMetaList;
    }

    /**
     * @param valueMetaList the list of valueMeta to set
     */
    public void setValueMeta(List valueMetaList)
    {
        this.valueMetaList = valueMetaList;
    }

    /**
     * @return the number of values in the row
     */
    public int size()
    {
        return valueMetaList.size();
    }

    /**
     * @return true if there are no elements in the row metadata
     */
    public boolean isEmpty()
    {
        return size()==0;
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
        return meta.getString(dataRow[index]);
    }
    
    /**
     * Get an Integer value from a row of data.  Convert data if this needed. 
     * 
     * @param rowRow the row of data
     * @param index the index
     * @return The integer found on that position in the row
     * @throws KettleValueException in case there was a problem converting the data.
     */
    public Long getInteger(Object[] dataRow, int index) throws KettleValueException
    {
        ValueMetaInterface meta = (ValueMetaInterface) valueMetaList.get(index);
        return meta.getInteger(dataRow[index]);
    }

    /**
     * Get a Number value from a row of data.  Convert data if this needed. 
     * 
     * @param rowRow the row of data
     * @param index the index
     * @return The number found on that position in the row
     * @throws KettleValueException in case there was a problem converting the data.
     */
    public Double getNumber(Object[] dataRow, int index) throws KettleValueException
    {
        ValueMetaInterface meta = (ValueMetaInterface) valueMetaList.get(index);
        return meta.getNumber(dataRow[index]);
    }

    /**
     * Get a Date value from a row of data.  Convert data if this needed. 
     * 
     * @param rowRow the row of data
     * @param index the index
     * @return The date found on that position in the row
     * @throws KettleValueException in case there was a problem converting the data.
     */
    public Date getDate(Object[] dataRow, int index) throws KettleValueException
    {
        ValueMetaInterface meta = (ValueMetaInterface) valueMetaList.get(index);
        return meta.getDate(dataRow[index]);
    }

    /**
     * Get a BigNumber value from a row of data.  Convert data if this needed. 
     * 
     * @param rowRow the row of data
     * @param index the index
     * @return The bignumber found on that position in the row
     * @throws KettleValueException in case there was a problem converting the data.
     */
    public BigDecimal getBigNumber(Object[] dataRow, int index) throws KettleValueException
    {
        ValueMetaInterface meta = (ValueMetaInterface) valueMetaList.get(index);
        return meta.getBigNumber(dataRow[index]);
    }

    /**
     * Get a Boolean value from a row of data.  Convert data if this needed. 
     * 
     * @param rowRow the row of data
     * @param index the index
     * @return The boolean found on that position in the row
     * @throws KettleValueException in case there was a problem converting the data.
     */
    public Boolean getBoolean(Object[] dataRow, int index) throws KettleValueException
    {
        ValueMetaInterface meta = (ValueMetaInterface) valueMetaList.get(index);
        return meta.getBoolean(dataRow[index]);
    }
    
    /**
     * Get a Binary value from a row of data.  Convert data if this needed. 
     * 
     * @param rowRow the row of data
     * @param index the index
     * @return The binary found on that position in the row
     * @throws KettleValueException in case there was a problem converting the data.
     */
    public byte[] getBinary(Object[] dataRow, int index) throws KettleValueException
    {
        ValueMetaInterface meta = (ValueMetaInterface) valueMetaList.get(index);
        return meta.getBinary(dataRow[index]);
    }

    /**
     * Determines whether a value in a row is null.  A value is null when the object is null.
     * As such, you can just as good write dataRow[index]==null in your code.
     * 
     * @param dataRow The row of data 
     * @param index the index to reference
     * @return true if the value on the index is null.
     */
    public boolean isNull(Object[] dataRow, int index)
    {
        return dataRow[index] == null;
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

    
    public String getString(Object[] dataRow, String valueName, String defaultValue) throws KettleValueException
    {
        int index = indexOfValue(valueName);
        if (index<1) return defaultValue;
        return getString(dataRow, index);
    }

    public Long getInteger(Object[] dataRow, String valueName, Long defaultValue) throws KettleValueException
    {
        int index = indexOfValue(valueName);
        if (index<1) return defaultValue;
        return getInteger(dataRow, index);
    }

    public int indexOfValue(String valueName)
    {
        for (int i=0;i<valueMetaList.size();i++)
        {
            if (getValueMeta(i).getName().equalsIgnoreCase(valueName)) return i;
        }
        return -1;
    }
    
    /**
     * Searches for a value with a certain name in the value meta list 
     * @param valueName The value name to search for
     * @return The value metadata or null if nothing was found
     */
    public ValueMetaInterface searchValueMeta(String valueName)
    {
        for (int i=0;i<valueMetaList.size();i++)
        {
            ValueMetaInterface valueMeta = getValueMeta(i);
            if (valueMeta.getName().equalsIgnoreCase(valueName)) return valueMeta;
        }
        return null;
    }

}
