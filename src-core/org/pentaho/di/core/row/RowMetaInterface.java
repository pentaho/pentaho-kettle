/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.row;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleValueException;
import org.w3c.dom.Node;

public interface RowMetaInterface extends Cloneable
{
    /**
     * @return the list of value Metadata 
     */
    public List<ValueMetaInterface> getValueMetaList();

    /**
     * @param valueMetaList the list of valueMeta to set
     */
    public void setValueMetaList(List<ValueMetaInterface> valueMetaList);

    /**
     * Check if a value is already present in this row with the same name
     * @param meta the value to check for existence
     * @return true if a value with the same name already exists in the row
     */
    public boolean exists(ValueMetaInterface meta);
    
    /**
     * Add a metadata value, extends the array if needed.
     * If a value with the same name already exists, it gets renamed.
     * 
     * @param meta The metadata value to add
     */
    public void addValueMeta(ValueMetaInterface meta);

    /**
     * Add a metadata value on a certain location in the row.
     * If a value with the same name already exists, it gets renamed.
     * Remember to change the data row according to this.
     *
     * @param index The index where the metadata value needs to be put in the row
     * @param meta The metadata value to add to the row
     */
    public void addValueMeta(int index, ValueMetaInterface meta);
    
    /**
     * Get the value metadata on the specified index.
     * @param index The index to get the value metadata from
     * @return The value metadata specified by the index.
     */
    public ValueMetaInterface getValueMeta(int index);
    
    /**
     * Replaces a value meta entry in the row metadata with another one 
     * @param index The index in the row to replace at
     * @param valueMeta the metadata to replace with
     */
    public void setValueMeta(int index, ValueMetaInterface valueMeta);

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
    public Object[] cloneRow(Object[] objects, Object[] cloneTo) throws KettleValueException;

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
     * @throws KettleValueException in case there is a conversion error (only thrown in case of lazy conversion)
     */
    public boolean isNull(Object[] dataRow, int index) throws KettleValueException;
    
    /**
     * @return a copy of this RowMetaInterface object
     */
    public RowMetaInterface clone();
    
    public String getString(Object[] dataRow, String valueName, String defaultValue) throws KettleValueException;
    public Long getInteger(Object[] dataRow, String valueName, Long defaultValue) throws KettleValueException;
    public Date getDate(Object[] dataRow, String valueName, Date defaultValue) throws KettleValueException;

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
     * Merge the values of row r to this Row.
     * The values that are not yet in the row are added unchanged.
     * The values that are in the row are renamed to name[2], name[3], etc.
     *
     * @param r The row to be merged with this row
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
     * @throws SocketTimeoutException In case there is a timeout during reading.
     */
    public Object[] readData(DataInputStream inputStream) throws KettleFileException, SocketTimeoutException;

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
    
    /**
     * Get an array of strings showing the name of the values in the row
     * padded to a maximum length, followed by the types of the values.
     *
     * @param maxlen The length to which the name will be padded.
     * @return an array of strings: the names and the types of the fieldnames in the row.
     */
    public String[] getFieldNamesAndTypes(int maxlen);
    
    /**
     * Compare 2 rows with each other using certain values in the rows and
     * also considering the specified ascending clauses of the value metadata.

     * @param rowData1 The first row of data
     * @param rowData2 The second row of data
     * @param fieldnrs the fields to compare on (in that order)
     * @return 0 if the rows are considered equal, -1 is data1 is smaller, 1 if data2 is smaller.
     * @throws KettleValueException
     */
    public int compare(Object[] rowData1, Object[] rowData2, int fieldnrs[]) throws KettleValueException;

    /**
     * Compare 2 rows with each other for equality using certain values in the rows and
     * also considering the case sensitivity flag.

     * @param rowData1 The first row of data
     * @param rowData2 The second row of data
     * @param fieldnrs the fields to compare on (in that order)
     * @return true if the rows are considered equal, false if they are not.
     * @throws KettleValueException
     */
    public boolean equals(Object[] rowData1, Object[] rowData2, int fieldnrs[]) throws KettleValueException;

    /**
     * Compare 2 rows with each other using certain values in the rows and
     * also considering the specified ascending clauses of the value metadata.

     * @param rowData1 The first row of data
     * @param rowData2 The second row of data
	 * @param fieldnrs1 The indexes of the values to compare in the first row
     * @param fieldnrs2 The indexes of the values to compare with in the second row
     * @return 0 if the rows are considered equal, -1 is data1 is smaller, 1 if data2 is smaller.
     * @throws KettleValueException
     */
    public int compare(Object[] rowData1, Object[] rowData2, int fieldnrs1[], int fieldnrs2[]) throws KettleValueException;

    /**
     * Compare 2 rows with each other using certain values in the rows and
     * also considering the specified ascending clauses of the value metadata.

     * @param rowData1 The first row of data
     * @param rowMeta2 the metadat of the second row of data
     * @param rowData2 The second row of data
	 * @param fieldnrs1 The indexes of the values to compare in the first row
     * @param fieldnrs2 The indexes of the values to compare with in the second row
     * @return 0 if the rows are considered equal, -1 is data1 is smaller, 1 if data2 is smaller.
     * @throws KettleValueException
     */
    public int compare(Object[] rowData1, RowMetaInterface rowMeta2, Object[] rowData2, int fieldnrs1[], int fieldnrs2[]) throws KettleValueException;

    /**
     * Compare 2 rows with each other using all values in the rows and
     * also considering the specified ascending clauses of the value metadata.

     * @param rowData1 The first row of data
     * @param rowData2 The second row of data
     * @return 0 if the rows are considered equal, -1 is data1 is smaller, 1 if data2 is smaller.
     * @throws KettleValueException
     */
    public int compare(Object[] rowData1, Object[] rowData2) throws KettleValueException;
 
    /**
     * Calculate a hashCode of the content (not the index) of the data specified
     * NOTE: This method uses a simple XOR of the individual hashCodes which can
     * result in a lot of collisions for similar types of data (e.g. [A,B] == [B,A] 
     * and is not suitable for normal use.  It is kept to provide backward
     * compatibility with CombinationLookup.lookupValues()
     * @param rowData The data to calculate a hashCode with
     * @return the calculated hashCode
     * @throws KettleValueException in case there is a data conversion error
     * @deprecated
     */
    public int oldXORHashCode(Object[] rowData) throws KettleValueException;
    
    /**
     * Calculates a simple hashCode of all the native data objects in
     * the supplied row.  This method will return a better distribution
     * of values for rows of numbers or rows with the same values in
     * different positions.
     * NOTE: This method performs against the native values, not the values
     * returned by ValueMeta.  This means that if you have two rows with
     * different primitive values ['2008-01-01:12:30'] and ['2008-01-01:00:00']
     * that use a format object to change the value (as Date yyyy-MM-dd), the
     * hashCodes will be different resulting in the two rows not being considered
     * equal via the hashCode even though compare() or equals() might consider them to be.
     * @param rowData The data to calculate a hashCode with
     * @return the calculated hashCode
     * @throws KettleValueException in case there is a data conversion error
     */
    public int hashCode(Object[] rowData) throws KettleValueException;

    /**
     * Calculates a hashcode of the converted value of all objects in the supplied
     * row.  This method returns distinct values for nulls of different data types
     * and will return the same hashCode for different native values that have a
     * ValueMeta converting them into the same value 
     * (e.g. ['2008-01-01:12:30'] and ['2008-01-01:00:00'] as Date yyyy-MM-dd)
     * @param rowData The data to calculate a hashCode with
     * @return the calculated hashCode
     * @throws KettleValueException in case there is a data conversion error
     */
    public int convertedValuesHashCode(Object[] rowData) throws KettleValueException;
    
    /**
     * @return a string with a description of all the metadata values of the complete row of metadata
     */
    public String toStringMeta();
    
	/**
	 * @return an XML representation of the row metadata
	 * @throws IOException Thrown in case there is an (Base64/GZip) encoding problem
	 */
	public String getMetaXML() throws IOException;

	/**
	 * @param rowData the row of data to serialize as XML
	 * @return an XML representation of the row data
	 * @throws IOException Thrown in case there is an (Base64/GZip) encoding problem
	 */
	public String getDataXML(Object[] rowData) throws IOException;
	
	/**
	 * Convert an XML node into binary data using the row metadata supplied. 
	 * @param rowMeta The row metadata to reference
	 * @param node The data row node 
	 * @return a row of data de-serialized from XML
	 * @throws KettleException Thrown in case there is an (Base64/GZip) decoding problem 
	 */
	public Object[] getRow(Node node) throws KettleException; 

}
