/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core.row;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.pentaho.di.compatibility.Row;
import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

public class RowMeta implements RowMetaInterface
{
	public static final String XML_META_TAG = "row-meta"; //$NON-NLS-1$
	public static final String XML_DATA_TAG = "row-data"; //$NON-NLS-1$
    
    private List<ValueMetaInterface> valueMetaList;
	private List<Integer>	valuesThatNeedRealClone;

    public RowMeta()
    {
        valueMetaList = new ArrayList<ValueMetaInterface>();
    }
    
    public RowMeta clone()
    {
        RowMeta rowMeta = new RowMeta();
        for (int i=0;i<size();i++)
        {
            ValueMetaInterface valueMeta = getValueMeta(i);
            rowMeta.addValueMeta( valueMeta.clone() );
        }
        return rowMeta;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (int i=0;i<size();i++)
        {
            if (i>0) buffer.append(", ");
            buffer.append( "[" );
            buffer.append(getValueMeta(i).toString());
            buffer.append( "]" );
        }
        return buffer.toString();
    }
    /**
     * @return the list of value metadata 
     */
    public List<ValueMetaInterface> getValueMetaList()
    {
        return valueMetaList;
    }

    /**
     * @param valueMetaList the list of valueMeta to set
     */
    public void setValueMetaList(List<ValueMetaInterface> valueMetaList)
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

    public boolean exists(ValueMetaInterface meta)
    {
        return searchValueMeta(meta.getName())!=null;
    }
    
    /**
     * Add a metadata value.
     * If a value with the same name already exists, it gets renamed.
     * 
     * @param meta The metadata value to add
     */
    public void addValueMeta(ValueMetaInterface meta)
    {
        if (!exists(meta))
        {
            valueMetaList.add(meta);
        }
        else
        {
            valueMetaList.add(renameValueMetaIfInRow(meta));
        }
    }
    
    /**
     * Add a metadata value on a certain location in the row.
     * If a value with the same name already exists, it gets renamed.
     * Remember to change the data row according to this.
     *
     * @param index The index where the metadata value needs to be put in the row
     * @param meta The metadata value to add to the row
     */
    public void addValueMeta(int index, ValueMetaInterface meta)
    {
        if (!exists(meta))
        {
            valueMetaList.add(index, meta);
        }
        else
        {
            valueMetaList.add(index, renameValueMetaIfInRow(meta));
        }
    }

    /**
     * Get the value metadata on the specified index.
     * @param index The index to get the value metadata from
     * @return The value metadata specified by the index.
     */
    public ValueMetaInterface getValueMeta(int index)
    {
        return valueMetaList.get(index);
    }
    
    /**
     * Replaces a value meta entry in the row metadata with another one 
     * @param index The index in the row to replace at
     * @param valueMeta the metadata to replace with
     */
    public void setValueMeta(int index, ValueMetaInterface valueMeta)
    {
        valueMetaList.set(index, valueMeta);
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
    	if( dataRow == null ) {
    		return null;
    	}
        ValueMetaInterface meta = valueMetaList.get(index);
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
    	if( dataRow == null ) {
    		return null;
    	}
        ValueMetaInterface meta = valueMetaList.get(index);
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
    	if( dataRow == null ) {
    		return null;
    	}
        ValueMetaInterface meta = valueMetaList.get(index);
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
    	if( dataRow == null ) {
    		return null;
    	}
        ValueMetaInterface meta = valueMetaList.get(index);
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
    	if( dataRow == null ) {
    		return null;
    	}
        ValueMetaInterface meta = valueMetaList.get(index);
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
    	if( dataRow == null ) {
    		return null;
    	}
        ValueMetaInterface meta = valueMetaList.get(index);
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
    	if( dataRow == null ) {
    		return null;
    	}
        ValueMetaInterface meta = valueMetaList.get(index);
        return meta.getBinary(dataRow[index]);
    }

    /**
     * Determines whether a value in a row is null.  
     * A value is null when the object is null or when it's an empty String 
     * 
     * @param dataRow The row of data 
     * @param index the index to reference
     * @return true if the value on the index is null.
     * @throws KettleValueException in case there is a conversion error (only thrown in case of lazy conversion)
     */
    public boolean isNull(Object[] dataRow, int index) throws KettleValueException
    {
    	if( dataRow == null ) {
    		// I guess so...
    		return true;
    	}
        return getValueMeta(index).isNull(dataRow[index]);
    }
    
    /**
     * @return a cloned Object[] object.
     * @throws KettleValueException in case something is not quite right with the expected data
     */
    public Object[] cloneRow(Object[] objects) throws KettleValueException {
		if (valuesThatNeedRealClone == null) {
			valuesThatNeedRealClone = new ArrayList<Integer>();
			for (int i = 0; i < size(); i++) {
				ValueMetaInterface valueMeta = getValueMeta(i);
				if (valueMeta.requiresRealClone()) {
					valuesThatNeedRealClone.add(i);
				}
			}
		}
		Object[] newObjects = objects.clone();
		for (Integer i : valuesThatNeedRealClone) {
			ValueMetaInterface valueMeta = getValueMeta(i);
			newObjects[i] = valueMeta.cloneValueData(objects[i]);
		}
		return newObjects;
	} 

    
    public String getString(Object[] dataRow, String valueName, String defaultValue) throws KettleValueException
    {
        int index = indexOfValue(valueName);
        if (index<0) return defaultValue;
        return getString(dataRow, index);
    }

    public Long getInteger(Object[] dataRow, String valueName, Long defaultValue) throws KettleValueException
    {
        int index = indexOfValue(valueName);
        if (index<0) return defaultValue;
        return getInteger(dataRow, index);
    }

    public Date getDate(Object[] dataRow, String valueName, Date defaultValue) throws KettleValueException
    {
        int index = indexOfValue(valueName);
        if (index<0) return defaultValue;
        return getDate(dataRow, index);
    }

    /**
     * Searches the index of a value meta with a given name
     * @param valueName the name of the value metadata to look for
     * @return the index or -1 in case we didn't find the value
     */
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

    public void addRowMeta(RowMetaInterface rowMeta)
    {
        for (int i=0;i<rowMeta.size();i++)
        {
            addValueMeta(rowMeta.getValueMeta(i));
        }
    }
    
    /**
     * Merge the values of row r to this Row.
     * The values that are not yet in the row are added unchanged.
     * The values that are in the row are renamed to name_2, name_3, etc.
     *
     * @param r The row to be merged with this row
     */
    public void mergeRowMeta(RowMetaInterface r)
    {
        for (int x=0;x<r.size();x++)
        {
            ValueMetaInterface field = r.getValueMeta(x);
            if (searchValueMeta(field.getName())==null)
            {
                addValueMeta(field); // Not in list yet: add
            }
            else
            {
                // We want to rename the field to Name[2], Name[3], ...
                // 
                addValueMeta(renameValueMetaIfInRow(field));
            }
        }
    }
    
    private ValueMetaInterface renameValueMetaIfInRow(ValueMetaInterface valueMeta)
    {
        // We want to rename the field to Name[2], Name[3], ...
        // 
        int index = 1;
        String name = valueMeta.getName()+"_"+index;
        while (searchValueMeta(name)!=null)
        {
            index++;
            name = valueMeta.getName()+"_"+index;
        }

        // Create a copy of the valueMeta object to make sure we don't rename any other value meta objects.
        // It's only being renamed because of the addition to THIS row metadata object, not another.
        //
        ValueMetaInterface copy = valueMeta.clone();
        
        // OK, this is the new name to pick
        //
        copy.setName(name);
        
        return copy;
    }

    /**
     * Get an array of the names of all the Values in the Row.
     * @return an array of Strings: the names of all the Values in the Row.
     */
    public String[] getFieldNames()
    {
        String retval[] = new String[size()];

        for (int i=0;i<size();i++)
        {
            retval[i]=getValueMeta(i).getName();
        }

        return retval;
    }

    /**
     * Write ONLY the specified data to the outputStream
     * @throws KettleFileException  in case things go awry
     */
    public void writeData(DataOutputStream outputStream, Object[] data) throws KettleFileException
    {
        // Write all values in the row
        for (int i=0;i<size();i++) getValueMeta(i).writeData(outputStream, data[i]);
        
        // If there are 0 values in the row, we write a marker flag to be able to detect an EOF on the other end (sockets etc)
        // 
        if (size()==0) {
    		try {
				outputStream.writeBoolean(true);
			} catch (IOException e) {
				throw new KettleFileException("Error writing marker flag", e);
			}
        }
    }

    /**
     * Write ONLY the specified metadata to the outputStream
     * @throws KettleFileException  in case things go awry
     */
    public void writeMeta(DataOutputStream outputStream) throws KettleFileException
    {
        // First handle the number of fields in a row
        try
        {
            outputStream.writeInt(size());
        }
        catch (IOException e)
        {
            throw new KettleFileException("Unable to write nr of metadata values", e);
        }

        // Write all values in the row
        for (int i=0;i<size();i++) getValueMeta(i).writeMeta(outputStream);

    }
    
    public RowMeta(DataInputStream inputStream) throws KettleFileException, KettleEOFException, SocketTimeoutException
    {
        this();
        
        int nr;
        try
        {
            nr = inputStream.readInt();
        }
        catch(SocketTimeoutException e)
        {
        	throw e;
        }
        catch (EOFException e) 
        {
        	throw new KettleEOFException("End of file while reading the number of metadata values in the row metadata", e);
        }
        catch (IOException e)
        {
            throw new KettleFileException("Unable to read nr of metadata values: "+e.toString(), e);
        }
        for (int i=0;i<nr;i++)
        {
            addValueMeta( new ValueMeta(inputStream) );
        }
    }

	public Object[] readData(DataInputStream inputStream) throws KettleFileException, KettleEOFException, SocketTimeoutException
    {
        Object[] data = new Object[size()];
        for (int i=0;i<size();i++)
        {
            data[i] = getValueMeta(i).readData(inputStream);
        }
        if (size()==0) 
        {
        	try 
        	{
				inputStream.readBoolean();
			}
        	catch(EOFException e)
	        {
	        	throw new KettleEOFException(e);
	        }
	        catch(SocketTimeoutException e)
	        {
	        	throw e;
	        }
	        catch(IOException e)
	        {
	            throw new KettleFileException(toString()+" : Unable to read the marker flag data from input stream", e);
	        }

        }
        return data;
    }

    public void clear()
    {
        valueMetaList.clear();
    }

    public void removeValueMeta(String valueName) throws KettleValueException
    {
        int index = indexOfValue(valueName);
        if (index<0) throw new KettleValueException("Unable to find value metadata with name '"+valueName+"', so I can't delete it.");
        valueMetaList.remove(index);
    }

    public void removeValueMeta(int index)
    {
        valueMetaList.remove(index);
    }
    
    /**
     * @return a string with a description of all the metadata values of the complete row of metadata
     */
    public String toStringMeta()
    {
        StringBuffer buffer = new StringBuffer();
        for (int i=0;i<size();i++)
        {
            if (i>0) buffer.append(", ");
            buffer.append( "[" );
            buffer.append(getValueMeta(i).toStringMeta());
            buffer.append( "]" );
        }
        return buffer.toString();
    }
    
    /**
     * Get the string representation of the data in a row of data
     * @param row the row of data to convert to string
     * @return the row of data in string form
     * @throws KettleValueException in case of a conversion error
     */
    public String getString(Object[] row) throws KettleValueException
    {
        StringBuffer buffer = new StringBuffer();
        for (int i=0;i<size();i++)
        {
            if (i>0) buffer.append(", ");
            buffer.append( "[" );
            buffer.append( getString(row, i) );
            buffer.append( "]" );
        }
        return buffer.toString();
    }

    /**
     * Get an array of strings showing the name of the values in the row
     * padded to a maximum length, followed by the types of the values.
     *
     * @param maxlen The length to which the name will be padded.
     * @return an array of strings: the names and the types of the fieldnames in the row.
     */
    public String[] getFieldNamesAndTypes(int maxlen)
    {
        String retval[] = new String[size()];

        for (int i=0;i<size();i++)
        {
            ValueMetaInterface v = getValueMeta(i);
            retval[i]= Const.rightPad(v.getName(), maxlen)+"   ("+v.getTypeDesc()+")";
        }

        return retval;
    }
    
    /**
     * Compare 2 rows with each other using certain values in the rows and
     * also considering the specified ascending clauses of the value metadata.

     * @param rowData1 The first row of data
     * @param rowData2 The second row of data
     * @param fieldnrs the fields to compare on (in that order)
     * @return 0 if the rows are considered equal, -1 is data1 is smaller, 1 if data2 is smaller.
     * @throws KettleValueException
     */
    public int compare(Object[] rowData1, Object[] rowData2, int fieldnrs[]) throws KettleValueException
    {
        for (int i=0;i<fieldnrs.length;i++)
        {
            ValueMetaInterface valueMeta = getValueMeta(fieldnrs[i]);
            
            int cmp = valueMeta.compare(rowData1[fieldnrs[i]], rowData2[fieldnrs[i]]);
            if (cmp!=0) return cmp;
        }

        return 0;
    }

    /**
     * Compare 2 rows with each other for equality using certain values in the rows and
     * also considering the case sensitivity flag.

     * @param rowData1 The first row of data
     * @param rowData2 The second row of data
     * @param fieldnrs the fields to compare on (in that order)
     * @return true if the rows are considered equal, false if they are not.
     * @throws KettleValueException
     */
    public boolean equals(Object[] rowData1, Object[] rowData2, int[] fieldnrs) throws KettleValueException
    {
        for (int i=0;i<fieldnrs.length;i++)
        {
            ValueMetaInterface valueMeta = getValueMeta(fieldnrs[i]);
            
            int cmp = valueMeta.compare(rowData1[fieldnrs[i]], rowData2[fieldnrs[i]]);
            if (cmp!=0) return false;
        }

        return true;
    }
    
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
    public int compare(Object[] rowData1, Object[] rowData2, int fieldnrs1[], int fieldnrs2[]) throws KettleValueException
    {
    	int len = (fieldnrs1.length < fieldnrs2.length) ? fieldnrs1.length : fieldnrs2.length;
        for (int i=0;i<len;i++)
        {
            ValueMetaInterface valueMeta = getValueMeta(fieldnrs1[i]);
            
            int cmp = valueMeta.compare(rowData1[fieldnrs1[i]], rowData2[fieldnrs2[i]]);
            if (cmp!=0) return cmp;
        }

        return 0;
    }
    
    
    /**
     * Compare 2 rows with each other using certain values in the rows and
     * also considering the specified ascending clauses of the value metadata.

     * @param rowData1 The first row of data
     * @param rowMeta2 the metadata of the second row of data 
     * @param rowData2 The second row of data
	 * @param fieldnrs1 The indexes of the values to compare in the first row
     * @param fieldnrs2 The indexes of the values to compare with in the second row
     * @return 0 if the rows are considered equal, -1 is data1 is smaller, 1 if data2 is smaller.
     * @throws KettleValueException
     */
    public int compare(Object[] rowData1, RowMetaInterface rowMeta2, Object[] rowData2, int fieldnrs1[], int fieldnrs2[]) throws KettleValueException
    {
    	int len = (fieldnrs1.length < fieldnrs2.length) ? fieldnrs1.length : fieldnrs2.length;
        for (int i=0;i<len;i++)
        {
            ValueMetaInterface valueMeta1 = getValueMeta(fieldnrs1[i]);
            ValueMetaInterface valueMeta2 = rowMeta2.getValueMeta(fieldnrs2[i]);
            
            int cmp = valueMeta1.compare(rowData1[fieldnrs1[i]], valueMeta2, rowData2[fieldnrs2[i]]);
            if (cmp!=0) return cmp;
        }

        return 0;
    }

    
    /**
     * Compare 2 rows with each other using all values in the rows and
     * also considering the specified ascending clauses of the value metadata.

     * @param rowData1 The first row of data
     * @param rowData2 The second row of data
     * @return 0 if the rows are considered equal, -1 is data1 is smaller, 1 if data2 is smaller.
     * @throws KettleValueException
     */
    public int compare(Object[] rowData1, Object[] rowData2) throws KettleValueException
    {
        for (int i=0;i<size();i++)
        {
            ValueMetaInterface valueMeta = getValueMeta(i);
            
            int cmp = valueMeta.compare(rowData1[i], rowData2[i]);
            if (cmp!=0) return cmp;
        }

        return 0;
    }
    
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
    public int oldXORHashCode(Object[] rowData) throws KettleValueException
    {
        int hash = 0;

        for (int i=0;i<size();i++)
        {
            ValueMetaInterface valueMeta = getValueMeta(i);
            hash^=valueMeta.hashCode(rowData[i]);
        }
        
        return hash;
    }
    
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
    public int hashCode(Object[] rowData) throws KettleValueException
    {
        return Arrays.hashCode(rowData);
    }

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
    public int convertedValuesHashCode(Object[] rowData) throws KettleValueException
    {
        if (rowData == null) return 0;
        
        int result = 1;
        for (int i = 0; i < rowData.length; i++)
        {
            result = 31 * result + getValueMeta(i).hashCode();
        }
        return result;
    }
    
    /**
     * Serialize a row of data to byte[]
     * @param metadata the metadata to use
     * @param row the row of data
     * @return a serialized form of the data as a byte array
     */
    public static final byte[] extractData(RowMetaInterface metadata, Object[] row)
    {
        try
        {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            metadata.writeData(dataOutputStream, row);
            dataOutputStream.close();
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        }
        catch(Exception e)
        {
            throw new RuntimeException("Error serializing row to byte array", e);
        }
    }
    
    /**
     * Create a row of data bases on a serialized format (byte[]) 
     * @param data the serialized data
     * @param metadata the metadata to use
     * @return a new row of data
     */
    public static final Object[] getRow(RowMetaInterface metadata, byte[] data)
    {
        try
        {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
            return metadata.readData(dataInputStream);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Error de-serializing row of data from byte array", e);
        }
    }

	public static Row createOriginalRow(RowMetaInterface rowMeta, Object[] rowData) throws KettleValueException {
		Row row = new Row();
		
		for (int i=0;i<rowMeta.size();i++) {
			ValueMetaInterface valueMeta = rowMeta.getValueMeta(i);
			Object valueData = rowData[i];
			
			Value value = valueMeta.createOriginalValue(valueData);
			row.addValue(value);
		}
		
		return row;
	}
	
	/**
	 * @return an XML representation of the row metadata
	 * @throws IOException Thrown in case there is an (Base64/GZip) encoding problem
	 */
	public String getMetaXML() throws IOException
	{
		StringBuffer xml= new StringBuffer();
		
        xml.append("<").append(XML_META_TAG).append(">"); //$NON-NLS-1$ //$NON-NLS-2$
        
        for (int i=0;i<size();i++)
		{
			xml.append(getValueMeta(i).getMetaXML());
		}
        
		xml.append("</").append(XML_META_TAG).append(">"); //$NON-NLS-1$ //$NON-NLS-2$

		return xml.toString();
	}
	
	/**
	 * Create a new row metadata object from XML
	 * @param node the XML node to deserialize from
	 * @throws IOException Thrown in case there is an (Base64/GZip) decoding problem
	 */
    public RowMeta(Node node) throws KettleException {
    	this();
    	
		int nrValues = XMLHandler.countNodes(node, ValueMeta.XML_META_TAG); 
		for (int i=0;i<nrValues;i++)
		{
			addValueMeta( new ValueMeta(XMLHandler.getSubNodeByNr(node, ValueMeta.XML_META_TAG, i)) );
		}
	}
	
	/**
	 * @param rowData the row of data to serialize as XML
	 * @return an XML representation of the row data
	 * @throws IOException Thrown in case there is an (Base64/GZip) encoding problem
	 */
	public String getDataXML(Object[] rowData) throws IOException
	{
		StringBuffer xml= new StringBuffer();
		
        xml.append("<").append(XML_DATA_TAG).append(">"); //$NON-NLS-1$ //$NON-NLS-2$
        
        for (int i=0;i<size();i++)
		{
			xml.append(getValueMeta(i).getDataXML(rowData[i]));
		}
        
		xml.append("</").append(XML_DATA_TAG).append(">"); //$NON-NLS-1$ //$NON-NLS-2$

		return xml.toString();
	}

	/**
	 * Convert an XML node into binary data using the row metadata supplied. 
	 * @param node The data row node 
	 * @throws IOException Thrown in case there is an (Base64/GZip) decoding problem
	 * @return a row of data, converted from XML
	 */
	public Object[] getRow(Node node) throws KettleException 
	{
		Object[] rowData = RowDataUtil.allocateRowData(size());

		for (int i=0;i<size();i++)
		{
			Node valueDataNode = XMLHandler.getSubNodeByNr(node, ValueMeta.XML_DATA_TAG, i);
			rowData[i] = getValueMeta(i).getValue(valueDataNode);
		}
		return rowData;
	}

}