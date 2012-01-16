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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleValueException;
import org.w3c.dom.Node;


public interface ValueMetaInterface extends Cloneable
{
    /** Value type indicating that the value has no type set */
    public static final int TYPE_NONE        = 0;
    
    /** Value type indicating that the value contains a floating point double precision number. */
    public static final int TYPE_NUMBER      = 1;
    
    /** Value type indicating that the value contains a text String. */
    public static final int TYPE_STRING      = 2;
    
    /** Value type indicating that the value contains a Date. */
    public static final int TYPE_DATE        = 3;
    
    /** Value type indicating that the value contains a boolean. */
    public static final int TYPE_BOOLEAN     = 4;
    
    /** Value type indicating that the value contains a long integer. */
    public static final int TYPE_INTEGER     = 5;
    
    /** Value type indicating that the value contains a floating point precision number with arbitrary precision. */
    public static final int TYPE_BIGNUMBER   = 6;
    
    /** Value type indicating that the value contains an Object. */
    public static final int TYPE_SERIALIZABLE= 7;
    
    /** Value type indicating that the value contains binary data: BLOB, CLOB, ... */
    public static final int TYPE_BINARY      = 8;

    public static final String[] typeCodes = new String[] { "-", "Number", "String", "Date", "Boolean", "Integer", "BigNumber", "Serializable", "Binary", }; 

    
    
    /** The storage type is the same as the indicated value type */
    public static final int STORAGE_TYPE_NORMAL        =  0; 
    
    /** The storage type is binary: read from text but not yet converted to the requested target data type, for lazy conversions. */
    public static final int STORAGE_TYPE_BINARY_STRING =  1; 

    /** The storage type is indexed.  This means that the value is a simple integer index referencing the values in getIndex() */
    public static final int STORAGE_TYPE_INDEXED       =  2; 
    
    public static final String[] storageTypeCodes = new String[] { "normal", "binary-string", "indexed", };
    
    
    /** Indicating that the rows are not sorted on this key */
    public static final int SORT_TYPE_NOT_SORTED = 0;
    
    /** Indicating that the rows are not sorted ascending on this key */
    public static final int SORT_TYPE_ASCENDING = 1;

    /** Indicating that the rows are sorted descending on this key */
    public static final int SORT_TYPE_DESCENDING = 2;
    
    public static final String[] sortTypeCodes = new String[] { "none", "ascending", "descending", };
    
    
    /** Indicating that the string content should NOT be trimmed if conversion is to occur to another data type */
    public static final int TRIM_TYPE_NONE  = 0;
    
    /** Indicating that the string content should be LEFT trimmed if conversion is to occur to another data type */
    public static final int TRIM_TYPE_LEFT  = 1;
    
    /** Indicating that the string content should be RIGHT trimmed if conversion is to occur to another data type */
    public static final int TRIM_TYPE_RIGHT = 2;

    /** Indicating that the string content should be LEFT AND RIGHT trimmed if conversion is to occur to another data type */
    public static final int TRIM_TYPE_BOTH  = 3;

    
    /** Default integer length for hardcoded metadata integers */
    public static final int DEFAULT_INTEGER_LENGTH = 10;

    public String   getName();
    public void     setName(String name);
    
    public int      getLength();
    public void     setLength(int length);
    
    public int      getPrecision();
    public void     setPrecision(int precision);
    
    public void     setLength(int length, int precision);
    
    public String   getOrigin();
    public void     setOrigin(String origin);
    
    public String   getComments();
    public void     setComments(String comments);
    
    public int      getType();
    public void     setType(int type);

    public int      getStorageType();
    public void     setStorageType(int storageType);
    
    public int      getTrimType();
    public void     setTrimType(int trimType);

    public Object[] getIndex();
    public void     setIndex(Object[] index);
    
    public boolean isStorageNormal();
    public boolean  isStorageIndexed();
    public boolean isStorageBinaryString();
    
    public String   getConversionMask();
    public void     setConversionMask(String conversionMask);
    
    public String getDecimalSymbol();
    public void setDecimalSymbol(String decimalSymbol);

    public String getGroupingSymbol();
    public void setGroupingSymbol(String groupingSymbol);
    
    public String getCurrencySymbol();
    public void setCurrencySymbol(String currencySymbol);
    
    public SimpleDateFormat getDateFormat();
    public DecimalFormat getDecimalFormat();
    public DecimalFormat getDecimalFormat(boolean useBigDecimal);

    public String   getStringEncoding();
    public void     setStringEncoding(String stringEncoding);
    
	/**
	 * @return true if the String encoding used (storage) is single byte encoded.
	 */
	public boolean isSingleByteEncoding();

    /**
     * Determine if an object is null.
     * This is the case if data==null or if it's an empty string.
     * @param data the object to test
     * @return true if the object is considered null.
     * @throws KettleValueException in case there is a conversion error (only thrown in case of lazy conversion)
     */
    public boolean isNull(Object data) throws KettleValueException;
    
    /**
     * @return the caseInsensitive
     */
    public boolean isCaseInsensitive();
    
    /**
     * @param caseInsensitive the caseInsensitive to set
     */
    public void setCaseInsensitive(boolean caseInsensitive);
    
    /**
     * @return the sortedDescending
     */
    public boolean isSortedDescending();

    /**
     * @param sortedDescending the sortedDescending to set
     */
    public void setSortedDescending(boolean sortedDescending);
    
    /**
     * @return true if output padding is enabled (padding to specified length)
     */
    public boolean isOutputPaddingEnabled();
    
    /**
     * @param outputPaddingEnabled Set to true if output padding is to be enabled (padding to specified length)
     */
    public void setOutputPaddingEnabled(boolean outputPaddingEnabled);
    
    /**
     * @return true if this is a large text field (CLOB, TEXT) with arbitrary length.
     */
    public boolean isLargeTextField();
    
    /**
     * @param largeTextField Set to true if this is to be a large text field (CLOB, TEXT) with arbitrary length.
     */
    public void setLargeTextField(boolean largeTextField);
    
    /**
     * @return true if the the date formatting (parsing) is to be lenient
     */
    public boolean isDateFormatLenient();
    
    /**
     * @param dateFormatLenient true if the the date formatting (parsing) is to be set to lenient
     */
    public void setDateFormatLenient(boolean dateFormatLenient);
    
    /**
     * @return the date format locale
     */
    public Locale getDateFormatLocale();
    
    /**
     * @param dateFormatLocale the date format locale to set
     */
    public void setDateFormatLocale(Locale dateFormatLocale);

    /**
     *  store original JDBC RecordSetMetaData for later use
     *	@see java.sql.ResultSetMetaData
     */

    public int    getOriginalColumnType(); 
    public void	  setOriginalColumnType(int originalColumnType);

    public String getOriginalColumnTypeName(); 
    public void	  setOriginalColumnTypeName(String originalColumnTypeName);
   
    public int    getOriginalPrecision(); 
    public void	  setOriginalPrecision(int originalPrecision);

    public int    getOriginalScale(); 
    public void	  setOriginalScale(int originalScale);

    public boolean isOriginalAutoIncrement(); 
    public void	  setOriginalAutoIncrement(boolean originalAutoIncrement);

    public int    isOriginalNullable(); 
    public void	  setOriginalNullable(int originalNullable);

    public boolean isOriginalSigned(); 
    public void	  setOriginalSigned(boolean originalSigned);
    
    /* Conversion methods */
    
    public Object cloneValueData(Object object) throws KettleValueException;

    /** Convert the supplied data to a String compatible with version 2.5. */
    public String getCompatibleString(Object object) throws KettleValueException;

    /** Convert the supplied data to a String */
    public String getString(Object object) throws KettleValueException;

    /** convert the supplied data to a binary string representation (for writing text) */
    public byte[] getBinaryString(Object object) throws KettleValueException;
    
    /** Convert the supplied data to a Number */
    public Double getNumber(Object object) throws KettleValueException;

    /** Convert the supplied data to a BigNumber */
    public BigDecimal getBigNumber(Object object) throws KettleValueException;

    /** Convert the supplied data to an Integer*/
    public Long getInteger(Object object) throws KettleValueException;

    /** Convert the supplied data to a Date */
    public Date getDate(Object object) throws KettleValueException;

    /** Convert the supplied data to a Boolean */
    public Boolean getBoolean(Object object) throws KettleValueException;

    /** Convert the supplied data to binary data */
    public byte[] getBinary(Object object) throws KettleValueException;

    /**
     * @return a copy of this value meta object
     */
    public ValueMetaInterface clone();
    
    /**
     * Checks wheter or not the value is a String.
     * @return true if the value is a String.
     */
    public boolean isString();

    /**
     * Checks whether or not this value is a Date
     * @return true if the value is a Date
     */
    public boolean isDate();

    /**
     * Checks whether or not the value is a Big Number
     * @return true is this value is a big number
     */
    public boolean isBigNumber();

    /**
     * Checks whether or not the value is a Number
     * @return true is this value is a number
     */
    public boolean isNumber();

    /**
     * Checks whether or not this value is a boolean
     * @return true if this value has type boolean.
     */
    public boolean isBoolean();

    /**
     * Checks whether or not this value is of type Serializable
     * @return true if this value has type Serializable
     */
    public boolean isSerializableType();

    /**
     * Checks whether or not this value is of type Binary
     * @return true if this value has type Binary
     */
    public boolean isBinary(); 
    
    /**
     * Checks whether or not this value is an Integer
     * @return true if this value is an integer
     */
    public boolean isInteger();

    /**
     * Checks whether or not this Value is Numeric
     * A Value is numeric if it is either of type Number or Integer
     * @return true if the value is either of type Number or Integer
     */
    public boolean isNumeric();
    
    /**
     * Return the type of a value in a textual form: "String", "Number", "Integer", "Boolean", "Date", ...
     * @return A String describing the type of value.
     */
    public String getTypeDesc();
    
    /**
     * a String text representation of this Value, optionally padded to the specified length
     * @return a String text representation of this Value, optionally padded to the specified length
     */
    public String toStringMeta();
    
    /**
     * Write the content of this class (metadata) to the specified output stream.
     * @param outputStream the outputstream to write to
     * @throws KettleFileException in case a I/O error occurs
     */
    public void writeMeta(DataOutputStream outputStream) throws KettleFileException;

    /**
     * Serialize the content of the specified data object to the outputStream.  No metadata is written.
     * @param outputStream the outputstream to write to
     * @param object the data object to serialize
     * @throws KettleFileException in case a I/O error occurs
     */
    public void writeData(DataOutputStream outputStream, Object object) throws KettleFileException;

    /**
     * De-serialize data from an inputstream.  No metadata is read or changed.
     * @param inputStream the input stream to read from 
     * @return a new data object
     * @throws KettleFileException in case a I/O error occurs
     * @throws KettleEOFException When we have read all the data there is to read
     * @throws SocketTimeoutException In case there is a timeout (when set on a socket) during reading
     */
    public Object readData(DataInputStream inputStream) throws KettleFileException, KettleEOFException, SocketTimeoutException;
    
    /**
     * Compare 2 values of the same data type
     * @param data1 the first value
     * @param data2 the second value
     * @return 0 if the values are equal, -1 if data1 is smaller than data2 and +1 if it's larger.
     * @throws KettleValueException In case we get conversion errors
     */
    public int compare(Object data1, Object data2) throws KettleValueException;

    /**
     * Compare 2 values of the same data type
     * @param data1 the first value
     * @param meta2 the second value's metadata
     * @param data2 the second value
     * @return 0 if the values are equal, -1 if data1 is smaller than data2 and +1 if it's larger.
     * @throws KettleValueException In case we get conversion errors
     */
    public int compare(Object data1, ValueMetaInterface meta2, Object data2) throws KettleValueException;
    
    /**
     * Convert the specified data to the data type specified in this object.
     * @param meta2 the metadata of the object to be converted
     * @param data2 the data of the object to be converted
     * @return the object in the data type of this value metadata object
     * @throws KettleValueException in case there is a data conversion error
     */
    public Object convertData(ValueMetaInterface meta2, Object data2) throws KettleValueException;

    /**
     * Convert the specified data to the data type specified in this object.
     * For String conversion, be compatible with version 2.5.2.
     * 
     * @param meta2 the metadata of the object to be converted
     * @param data2 the data of the object to be converted
     * @return the object in the data type of this value metadata object
     * @throws KettleValueException in case there is a data conversion error
     */
    public Object convertDataCompatible(ValueMetaInterface meta2, Object data2) throws KettleValueException;

    /**
     * Convert an object to the data type specified in the conversion metadata
     * @param data The data
     * @return The data converted to the conversion data type
     * @throws KettleValueException in case there is a conversion error.
     */
    public Object convertDataUsingConversionMetaData(Object data) throws KettleValueException;
    /**
     * Convert the specified string to the data type specified in this object.
     * @param pol the string to be converted
     * @param convertMeta the metadata of the object (only string type) to be converted
     * @param nullif set the object to null if pos equals nullif (IgnoreCase)
     * @param ifNull set the object to ifNull when pol is empty or null
     * @param trim_type the trim type to be used (ValueMetaInterface.TRIM_TYPE_XXX)
     * @return the object in the data type of this value metadata object
     * @throws KettleValueException in case there is a data conversion error
     */
    public Object convertDataFromString(String pol, ValueMetaInterface convertMeta, String nullif, String ifNull, int trim_type) throws KettleValueException;
    
    /**
     * Converts the specified data object to the normal storage type.
     * @param object the data object to convert
     * @return the data in a normal storage type
     * @throws KettleValueException In case there is a data conversion error.
     */
    public Object convertToNormalStorageType(Object object) throws KettleValueException;
    
    /**
     * Convert the given binary data to the actual data type.<br> 
     * - byte[] --> Long (Integer)<br>
     * - byte[] --> Double (Number)<br>
     * - byte[] --> BigDecimal (BigNumber)<br>
     * - byte[] --> Date (Date)<br>
     * - byte[] --> Boolean (Boolean)<br>
     * - byte[] --> byte[] (Binary)<br>
     * <br>
     * @param binary the binary data read from file or database
     * @return the native data type after conversion
     * @throws KettleValueException in case there is a data conversion error
     */
    public Object convertBinaryStringToNativeType(byte[] binary) throws KettleValueException;

    /**
     * Convert a normal storage type to a binary string object. (for comparison reasons)
     * @param object The object expressed in a normal storage type 
     * @return a binary string
     * @throws KettleValueException in case there is a data conversion error
     */
    public Object convertNormalStorageTypeToBinaryString(Object object) throws KettleValueException;
    
    /**
     * Converts the specified data object to the binary string storage type.
     * @param object the data object to convert
     * @return the data in a binary string storage type
     * @throws KettleValueException In case there is a data conversion error.
     */
    public Object convertToBinaryStringStorageType(Object object) throws KettleValueException;
    
    /**
     * Calculate the hashcode of the specified data object
     * @param object the data value to calculate a hashcode for 
     * @return the calculated hashcode
     * @throws KettleValueException in case there is a data conversion error
     */
    public int hashCode(Object object) throws KettleValueException;
    
    /**
     * Create an old-style value for backward compatibility reasons
     * @param data the data to store in the value
     * @return a newly created Value object
     * @throws KettleValueException in case there is a data conversion problem 
     */
    public Value createOriginalValue(Object data) throws KettleValueException;
    
    /**
     * Extracts the primitive data from an old style Value object 
     * @param value the old style Value object 
     * @return the value's data, NOT the meta data.
     * @throws KettleValueException  case there is a data conversion problem
     */
    public Object getValueData(Value value) throws KettleValueException;
    
	/**
	 * @return the storage Meta data that is needed for internal conversion from BinaryString or String to the specified type.
	 *         This storage Meta data object survives cloning and should travel through the transformation unchanged as long as the data type remains the same.
	 */
	public ValueMetaInterface getStorageMetadata();
	
	/**
	 * @param storageMetadata the storage Meta data that is needed for internal conversion from BinaryString or String to the specified type.
	 *         This storage Meta data object survives cloning and should travel through the transformation unchanged as long as the data type remains the same.
	 */
	public void setStorageMetadata(ValueMetaInterface storageMetadata);
	
	/**
	 * This conversion metadata can be attached to a String object to see where it came from and with which mask it was generated, the encoding, the local languages used, padding, etc.
	 * @return The conversion metadata
	 */
	public ValueMetaInterface getConversionMetadata();

	/**
	 * 	Attach conversion metadata to a String object to see where it came from and with which mask it was generated, the encoding, the local languages used, padding, etc.

	 * @param conversionMetadata the conversionMetadata to set 
	 */
	public void setConversionMetadata(ValueMetaInterface conversionMetadata);
	
	/**
	 * @return an XML representation of the row metadata
	 * @throws IOException Thrown in case there is an (Base64/GZip) decoding problem
	 */
	public String getMetaXML() throws IOException;
	
	/**
	 * @param value The data to serialize as XML
	 * @return an xML representation of the row data
	 * @throws IOException Thrown in case there is an (Base64/GZip) decoding problem
	 */
	public String getDataXML(Object value) throws IOException;
	
    /**
     * Convert a data XML node to an Object that corresponds to the metadata.
     * This is basically String to Object conversion that is being done.
     * @param node the node to retrieve the data value from
     * @return the converted data value
     * @throws KettleException thrown in case there is a problem with the XML to object conversion 
     */
	public Object getValue(Node node) throws KettleException;
	
	/**
	 * @return the number of binary string to native data type conversions done with this object conversions
	 */
	public long getNumberOfBinaryStringConversions();

	/**
	 * @param numberOfBinaryStringConversions the number of binary string to native data type done with this object conversions to set
	 */
	public void setNumberOfBinaryStringConversions(long numberOfBinaryStringConversions);
	
	/**
	 * @return true if the data type requires a real copy. Usually a binary or Serializable object
	 */
	public boolean requiresRealClone();
	
}
