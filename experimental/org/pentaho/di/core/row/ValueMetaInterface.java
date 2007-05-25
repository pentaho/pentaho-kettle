package org.pentaho.di.core.row;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;

import be.ibridge.kettle.core.exception.KettleFileException;
import be.ibridge.kettle.core.exception.KettleValueException;
import be.ibridge.kettle.core.value.Value;

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
    public static final int STORAGE_TYPE_NORMAL = 0; 
    
    /** The storage type is indexed.  This means that the value is a simple integer index referencing the values in getIndex() */
    public static final int STORAGE_TYPE_INDEXED = 2; 
    
    
    
    /** Indicating that the rows are not sorted on this key */
    public static final int SORT_TYPE_NOT_SORTED = 0;
    
    /** Indicating that the rows are not sorted ascending on this key */
    public static final int SORT_TYPE_ASCENDING = 1;

    /** Indicating that the rows are sorted descending on this key */
    public static final int SORT_TYPE_DESCENDING = 2;
    
    
    

    public String   getName();
    public void     setName(String name);
    
    public int      getLength();
    public void     setLength(int length);
    
    public int      getPrecision();
    public void     setPrecision(int precision);
    
    public String   getOrigin();
    public void     setOrigin(String origin);
    
    public String   getComments();
    public void     setComments(String comments);
    
    public int      getType();
    public void     setType(int type);

    public int      getStorageType();
    public void     setStorageType(int storageType);
    
    public Object[] getIndex();
    public void     setIndex(Object[] index);
    
    public boolean  isIndexed();
    
    public String   getConversionMask();
    public void     setConversionMask(String conversionMask);
    
    public String getDecimalSymbol();
    public void setDecimalSymbol(String decimalSymbol);

    public String getGroupingSymbol();
    public void setGroupingSymbol(String groupingSymbol);
    
    public String getCurrencySymbol();
    public void setCurrencySymbol(String currencySymbol);
    
    public DecimalFormat getDecimalFormat();

    public String   getStringEncoding();
    public void     setStringEncoding(String stringEncoding);
    
    /**
     * Determine if an object is null.
     * This is the case if data==null or if it's an empty string.
     * @param data the object to test
     * @return true if the object is considered null.
     */
    public boolean isNull(Object data);
    
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
    
    
    /* Conversion methods */
    
    public Object cloneValueData(Object object) throws KettleValueException;
    
    /** Convert the supplied data to a String */
    public String getString(Object object) throws KettleValueException;

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
    public Object clone();
    
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
     */
    public Object readData(DataInputStream inputStream) throws KettleFileException;
    
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
}
