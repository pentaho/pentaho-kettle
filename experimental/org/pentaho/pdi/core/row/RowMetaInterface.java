package org.pentaho.pdi.core.row;

import java.util.List;

import be.ibridge.kettle.core.exception.KettleValueException;

public interface RowMetaInterface
{
    public List getValueMeta();
    public void setValueMeta(List valueMeta);
    
    /**
     * Add a metadata value, extends the array if needed.
     * 
     * @param meta The metadata value to add
     */
    public void addMetaValue(ValueMetaInterface meta);
    
    /**
     * Get a String value from a row of data.  Convert data if this needed. 
     * 
     * @param rowRow the row of data
     * @param index the index
     * @return The string found on that position in the row
     * @throws KettleValueException in case there was a problem converting the data.
     */
    public String getString(Object[] dataRow, int index) throws KettleValueException;

}
