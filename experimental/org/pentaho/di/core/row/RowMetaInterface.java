package org.pentaho.di.core.row;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import be.ibridge.kettle.core.exception.KettleFileException;
import be.ibridge.kettle.core.exception.KettleValueException;

public interface RowMetaInterface extends Cloneable
{
    /**
     * @return the list of value Metadata 
     */
    public List getValueMetaList();

    /**
     * @param valueMetaList the list of valueMeta to set
     */
    public void setValueMeta(List valueMetaList);

    
    /**
     * Add a metadata value, extends the array if needed.
     * 
     * @param meta The metadata value to add
     */
    public void addValueMeta(ValueMetaInterface meta);
    
    /**
     * Get the value metadata on the specified index.
     * @param index The index to get the value metadata from
     * @return The value metadata specified by the index.
     */
    public ValueMetaInterface getValueMeta(int index);

    /**
     * Get a String value from a row of data.  Convert data if this needed. 
     * 
     * @param rowRow the row of data
     * @param index the index
     * @return The string found on that position in the row
     * @throws KettleValueException in case there was a problem converting the data.
     */
    public String getString(Object[] dataRow, int index) throws KettleValueException;
    
    /**
     * Get an Integer value from a row of data.  Convert data if this needed. 
     * 
     * @param rowRow the row of data
     * @param index the index
     * @return The integer found on that position in the row
     * @throws KettleValueException in case there was a problem converting the data.
     */
    public Long getInteger(Object[] dataRow, int index) throws KettleValueException;

    /**
     * Get a Number value from a row of data.  Convert data if this needed. 
     * 
     * @param rowRow the row of data
     * @param index the index
     * @return The number found on that position in the row
     * @throws KettleValueException in case there was a problem converting the data.
     */
    public Double getNumber(Object[] dataRow, int index) throws KettleValueException;

    /**
     * Get a Date value from a row of data.  Convert data if this needed. 
     * 
     * @param rowRow the row of data
     * @param index the index
     * @return The date found on that position in the row
     * @throws KettleValueException in case there was a problem converting the data.
     */
    public Date getDate(Object[] dataRow, int index) throws KettleValueException;

    /**
     * Get a BigNumber value from a row of data.  Convert data if this needed. 
     * 
     * @param rowRow the row of data
     * @param index the index
     * @return The bignumber found on that position in the row
     * @throws KettleValueException in case there was a problem converting the data.
     */
    public BigDecimal getBigNumber(Object[] dataRow, int index) throws KettleValueException;

    /**
     * Get a Boolean value from a row of data.  Convert data if this needed. 
     * 
     * @param rowRow the row of data
     * @param index the index
     * @return The boolean found on that position in the row
     * @throws KettleValueException in case there was a problem converting the data.
     */
    public Boolean getBoolean(Object[] dataRow, int index) throws KettleValueException;
    
    /**
     * Get a Binary value from a row of data.  Convert data if this needed. 
     * 
     * @param rowRow the row of data
     * @param index the index
     * @return The binary found on that position in the row
     * @throws KettleValueException in case there was a problem converting the data.
     */
    public byte[] getBinary(Object[] dataRow, int index) throws KettleValueException;
    
    /**
     * @return a cloned Object[] object.
     * @throws KettleValueException in case something is not quite right with the expected data
     */
    public Object[] cloneRow(Object[] objects) throws KettleValueException;

    /**
     * @return the size of the metadata row
     */
    public int size();
    
    /**
     * @return true if there are no elements in the row metadata
     */
    public boolean isEmpty();
    
    /**
     * Determines whether a value in a row is null.  A value is null when the object is null.
     * As such, you can just as good write dataRow[index]==null in your code.
     * 
     * @param dataRow The row of data 
     * @param index the index to reference
     * @return true if the value on the index is null.
     */
    public boolean isNull(Object[] dataRow, int index);
    
    /**
     * @return a copy of this RowMetaInterface object
     */
    public Object clone();
    
    public String getString(Object[] dataRow, String valueName, String defaultValue) throws KettleValueException;
    public Long getInteger(Object[] dataRow, String valueName, Long defaultValue) throws KettleValueException;

    /**
     * Searches for a value with a certain name in the value meta list 
     * @param valueName The value name to search for
     * @return The value metadata or null if nothing was found
     */
    public ValueMetaInterface searchValueMeta(String valueName);
    
    /**
     * Searches the index of a value meta with a given name
     * @param valueName the name of the value metadata to look for
     * @return the index or -1 in case we didn't find the value
     */
    public int indexOfValue(String valueName);
    
    /**
     * Add a number of fields from another row (append to the end)
     * @param rowMeta The row of metadata values to add
     */
    public void addRowMeta(RowMetaInterface rowMeta);
    
    /**
     * Merge the values of row r to this Row metadata.
     * Merge means: only the values that are not yet in the row are added
     * (comparing on the value name).
     *
     * @param r The row metadata to be merged with this row metadata
     */
    public void mergeRowMeta(RowMetaInterface r);

    /**
     * Get an array of the names of all the Values in the Row.
     * @return an array of Strings: the names of all the Values in the Row.
     */
    public String[] getFieldNames();

    /**
     * Write a serialized version of this class (Row Metadata) to the specified outputStream
     * @param outputStream the outputstream to write to
     * @param data the data to write after the metadata
     * @throws KettleFileException in case a I/O error occurs
     */
    public void writeMeta(DataOutputStream outputStream) throws KettleFileException;
    
    /**
     * Write a serialized version of the supplied data to the outputStream (based on the metadata but not the metadata itself)
     * @param outputStream the outputstream to write to
     * @param data the data to write after the metadata
     * @throws KettleFileException in case a I/O error occurs
     */
    public void writeData(DataOutputStream outputStream, Object[] data) throws KettleFileException;

    /**
     * De-serialize a row of data (no metadata is read) from an input stream
     * @param inputStream the inputstream to read from
     * @return a new row of data
     * @throws KettleFileException in case a I/O error occurs
     */
    public Object[] readData(DataInputStream inputStream) throws KettleFileException;

    /**
     * Clear the row metadata
     */
    public void clear();

    /**
     * Remove a value with a certain name from the row metadata
     * @param string the name of the value metadata to remove
     * @throws KettleValueException in case the value couldn't be found in the row metadata
     */
    public void removeValueMeta(String string) throws KettleValueException;

    /**
     * Remove a value metadata object on a certain index in the row
     * @param index the index to remove the value metadata from
     */
    public void removeValueMeta(int index);

    /**
     * Get the string representation of the data in a row of data
     * @param row the row of data to convert to string
     * @return the row of data in string form
     * @throws KettleValueException in case of a conversion error
     */
    public String getString(Object[] row) throws KettleValueException;
}
