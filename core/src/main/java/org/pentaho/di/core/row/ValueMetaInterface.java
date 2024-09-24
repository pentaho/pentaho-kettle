/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.gui.PrimitiveGCInterface;
import org.w3c.dom.Node;

/**
 * ValueMetaInterface objects are used to determine the characteristics of the row fields. They are typically obtained
 * from a RowMetaInterface object, which is acquired by a call to getInputRowMeta(). The getType() method returns one of
 * the static constants declared by ValueMetaInterface to indicate the PDI field type. Each field type maps to a
 * corresponding native Java type for the actual value.
 * <p>
 * <b>PDI Field Type / Java Mapping</b>
 * <p>
 * <Table border="1">
 * <tr>
 * <th>PDI data type</th>
 * <th>Type constant</th>
 * <th>Java data type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>String</td>
 * <td>TYPE_STRING</td>
 * <td>java.lang.String</td>
 * <td>A variable (unlimited) length text encoded in UTF-8 (Unicode)</td>
 * </tr>
 * <tr>
 * <td>Integer</td>
 * <td>TYPE_INTEGER</td>
 * <td>java.lang.Long</td>
 * <td>A signed long (64-bit) integer</td>
 * </tr>
 * <tr>
 * <td>Number</td>
 * <td>TYPE_NUMBER</td>
 * <td>java.lang.Double</td>
 * <td>A double precision floating point value</td>
 * </tr>
 * <tr>
 * <td>Big Number</td>
 * <td>TYPE_BIGNUMBER</td>
 * <td>java.math.BigDecimal</td>
 * <td>An arbitrary (unlimited) precision number</td>
 * </tr>
 * <tr>
 * <td>Date</td>
 * <td>TYPE_DATE</td>
 * <td>java.util.Date</td>
 * <td>A date-time value with millisecond precision</td>
 * </tr>
 * <tr>
 * <td>Boolean</td>
 * <td>TYPE_BOOLEAN</td>
 * <td>java.lang.Boolean</td>
 * <td>A boolean value (true or false)</td>
 * </tr>
 * <tr>
 * <td>Binary</td>
 * <td>TYPE_BINARY</td>
 * <td>java.lang.byte[</td>
 * <td>An array of bytes that contain any type of binary data.</td>
 * </tr>
 * </Table>
 * <p>
 * <b>Storage Types</b>
 * <p>
 * In addition to the data type of a field, the storage type (getStorageType()/setStorageType()) is used to interpret
 * the actual field value in a row array.
 * <p>
 * <Table border="1">
 * <tr>
 * <th>Type constant</th>
 * <th>Actual field data type</th>
 * <th>Interpretation</th>
 * <tr>
 * <tr>
 * <td>STORAGE_TYPE_NORMAL</td>
 * <td>As listed above</td>
 * <td>The value in the row array is of the type listed in the data type table above and represents the field&#39;s
 * value directly.
 * <tr>
 * <td>STORAGE_TYPE_BINARY_STRING</td>
 * <td>java.lang.byte[]</td>
 * <td>The field has been created using the &ldquo;Lazy conversion&rdquo; feature. This means it is a non-altered
 * sequence of bytes as read from an external medium (usually a file).
 * <tr>
 * <td>STORAGE_TYPE_INDEXED</td>
 * <td>java.lang.Integer</td>
 * <td>The row value is an integer index into a fixed array of possible values. The ValueMetaInterface object maintains
 * the set of possible values in getIndex()/setIndex().
 * </Table>
 */
public interface ValueMetaInterface extends Cloneable {
  /** Value type indicating that the value has no type set */
  int TYPE_NONE = 0;

  /** Value type indicating that the value contains a floating point double precision number. */
  int TYPE_NUMBER = 1;

  /** Value type indicating that the value contains a text String. */
  int TYPE_STRING = 2;

  /** Value type indicating that the value contains a Date. */
  int TYPE_DATE = 3;

  /** Value type indicating that the value contains a boolean. */
  int TYPE_BOOLEAN = 4;

  /** Value type indicating that the value contains a long integer. */
  int TYPE_INTEGER = 5;

  /** Value type indicating that the value contains a floating point precision number with arbitrary precision. */
  int TYPE_BIGNUMBER = 6;

  /** Value type indicating that the value contains an Object. */
  int TYPE_SERIALIZABLE = 7;

  /** Value type indicating that the value contains binary data: BLOB, CLOB, ... */
  int TYPE_BINARY = 8;

  /** Value type indicating that the value contains a date-time with nanosecond precision */
  int TYPE_TIMESTAMP = 9;

  /** Value type indicating that the value contains an Internet address */
  int TYPE_INET = 10;

  /** The Constant typeCodes. */
  String[] typeCodes = new String[] {
    "-", "Number", "String", "Date", "Boolean", "Integer", "BigNumber", "Serializable", "Binary", "Timestamp",
    "Internet Address", };

  /** The storage type is the same as the indicated value type */
  int STORAGE_TYPE_NORMAL = 0;

  /**
   * The storage type is binary: read from text but not yet converted to the requested target data type, for lazy
   * conversions.
   */
  int STORAGE_TYPE_BINARY_STRING = 1;

  /**
   * The storage type is indexed. This means that the value is a simple integer index referencing the values in
   * getIndex()
   */
  int STORAGE_TYPE_INDEXED = 2;

  /** The Constant storageTypeCodes. */
  String[] storageTypeCodes = new String[] { "normal", "binary-string", "indexed", };

  /** Indicating that the rows are not sorted on this key */
  int SORT_TYPE_NOT_SORTED = 0;

  /** Indicating that the rows are not sorted ascending on this key */
  int SORT_TYPE_ASCENDING = 1;

  /** Indicating that the rows are sorted descending on this key */
  int SORT_TYPE_DESCENDING = 2;

  /** The Constant sortTypeCodes. */
  String[] sortTypeCodes = new String[] { "none", "ascending", "descending", };

  /** Indicating that the string content should NOT be trimmed if conversion is to occur to another data type */
  int TRIM_TYPE_NONE = 0;

  /** Indicating that the string content should be LEFT trimmed if conversion is to occur to another data type */
  int TRIM_TYPE_LEFT = 1;

  /** Indicating that the string content should be RIGHT trimmed if conversion is to occur to another data type */
  int TRIM_TYPE_RIGHT = 2;

  /**
   * Indicating that the string content should be LEFT AND RIGHT trimmed if conversion is to occur to another data type
   */
  int TRIM_TYPE_BOTH = 3;

  /** Default integer length for hardcoded metadata integers */
  int DEFAULT_INTEGER_LENGTH = 10;

  static String getTypeDescription( int type ) {
    try {
      return typeCodes[ type ];
    } catch ( Exception e ) {
      return "unknown/illegal";
    }
  }

  static int getTypeCode( String desc ) {
    return Arrays.asList( typeCodes ).indexOf( desc );
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  String getName();

  /**
   * Sets the name.
   *
   * @param name
   *          the new name
   */
  void setName( String name );

  /**
   * Gets the length.
   *
   * @return the length
   */
  int getLength();

  /**
   * Sets the length.
   *
   * @param length
   *          the new length
   */
  void setLength( int length );

  /**
   * Gets the precision.
   *
   * @return the precision
   */
  int getPrecision();

  /**
   * Sets the precision.
   *
   * @param precision
   *          the new precision
   */
  void setPrecision( int precision );

  /**
   * Sets the length.
   *
   * @param length
   *          the length
   * @param precision
   *          the precision
   */
  void setLength( int length, int precision );

  /**
   * Gets the origin.
   *
   * @return the origin
   */
  String getOrigin();

  /**
   * Sets the origin.
   *
   * @param origin
   *          the new origin
   */
  void setOrigin( String origin );

  /**
   * Gets the comments.
   *
   * @return the comments
   */
  String getComments();

  /**
   * Sets the comments for the object implementing the interface.
   *
   * @param comments
   *          the new comments
   */
  void setComments( String comments );

  /**
   * Gets the type.
   *
   * @return the type
   */
  int getType();

  /**
   * Sets the type.
   *
   * @param type
   *          the new type
   *
   * @deprecated This method is deprecated. The same code is still used underneath.
   */
  @Deprecated
  void setType( int type );

  /**
   * Gets the storage type.
   *
   * @return the storage type
   */
  int getStorageType();

  /**
   * Sets the storage type.
   *
   * @param storageType
   *          the new storage type
   */
  void setStorageType( int storageType );

  /**
   * Gets the trim type.
   *
   * @return the trim type
   */
  int getTrimType();

  /**
   * Sets the trim type.
   *
   * @param trimType
   *          the new trim type
   */
  void setTrimType( int trimType );

  /**
   * Gets the index.
   *
   * @return the index
   */
  Object[] getIndex();

  /**
   * Sets the index.
   *
   * @param index
   *          the new index
   */
  void setIndex( Object[] index );

  /**
   * Checks if is storage normal.
   *
   * @return true, if is storage normal
   */
  boolean isStorageNormal();

  /**
   * Checks if is storage indexed.
   *
   * @return true, if is storage indexed
   */
  boolean isStorageIndexed();

  /**
   * Checks if is storage binary string.
   *
   * @return true, if is storage binary string
   */
  boolean isStorageBinaryString();

  /**
   * Gets the conversion mask.
   *
   * @return the conversion mask
   */
  String getConversionMask();

  /**
   * Sets the conversion mask.
   *
   * @param conversionMask
   *          the new conversion mask
   */
  void setConversionMask( String conversionMask );

  /**
   * Gets a formatting mask using this value's meta information.
   *
   * @return the format mask.
   */
  String getFormatMask();

  /**
   * Gets the decimal symbol.
   *
   * @return the decimal symbol
   */
  String getDecimalSymbol();

  /**
   * Sets the decimal symbol.
   *
   * @param decimalSymbol
   *          the new decimal symbol
   */
  void setDecimalSymbol( String decimalSymbol );

  /**
   * Gets the grouping symbol.
   *
   * @return the grouping symbol
   */
  String getGroupingSymbol();

  /**
   * Sets the grouping symbol.
   *
   * @param groupingSymbol
   *          the new grouping symbol
   */
  void setGroupingSymbol( String groupingSymbol );

  /**
   * Gets the currency symbol.
   *
   * @return the currency symbol
   */
  String getCurrencySymbol();

  /**
   * Sets the currency symbol.
   *
   * @param currencySymbol
   *          the new currency symbol
   */
  void setCurrencySymbol( String currencySymbol );

  /**
   * Gets the date format.
   *
   * @return the date format
   */
  SimpleDateFormat getDateFormat();

  /**
   * Gets the decimal format.
   *
   * @return the decimal format
   */
  DecimalFormat getDecimalFormat();

  /**
   * Gets the decimal format.
   *
   * @param useBigDecimal
   *          the use big decimal
   * @return the decimal format
   */
  DecimalFormat getDecimalFormat( boolean useBigDecimal );

  /**
   * Gets the string encoding.
   *
   * @return the string encoding
   */
  String getStringEncoding();

  /**
   * Sets the string encoding.
   *
   * @param stringEncoding
   *          the new string encoding
   */
  void setStringEncoding( String stringEncoding );

  /**
   * @return true if the String encoding used (storage) is single byte encoded.
   */
  boolean isSingleByteEncoding();

  /**
   * Determine if an object is null. This is the case if data==null or if it's an empty string.
   *
   * @param data
   *          the object to test
   * @return true if the object is considered null.
   * @throws KettleValueException
   *           in case there is a conversion error (only thrown in case of lazy conversion)
   */
  boolean isNull( Object data ) throws KettleValueException;

  /**
   * Returns a true of the value object is case insensitive, false if it is case sensitive,
   *
   * @return the caseInsensitive
   */
  boolean isCaseInsensitive();

  /**
   * Sets whether or not the value object is case sensitive. This information is useful if the value is involved in
   * string comparisons.
   *
   * @param caseInsensitive
   *          the caseInsensitive to set
   */
  void setCaseInsensitive( boolean caseInsensitive );

  /**
   * Returns a true of the value object is case insensitive, false if it is case sensitive,
   *
   * @return the collatorDisabled
   */
  boolean isCollatorDisabled();

  /**
   * Sets whether or not the value object should be sorted according to a locale using a collator
   *
   * @param collatorDisabled
   *          the collatorDisabled to set
   */
  void setCollatorDisabled( boolean collatorDisabled );

  /**
   * Get the current Locale of the collator
   * @return the current Collator Locale
   */
  Locale getCollatorLocale();

  /**
   * Sets the Locale of the collator
   */
  void setCollatorLocale( Locale locale );

  /**
   * Returns the strength of the collator.
   *
   * @return the collatorStrength
   */
  int getCollatorStrength();

 /**
   * Sets the strength of the collator.
   *
   * @param collatorStrength
   *          the collatorStrength to set
   */
  void setCollatorStrength( int collatorStrength ) throws IllegalArgumentException;

  /**
   * Returns whether or not the value should be sorted in descending order.
   *
   * @return the sortedDescending
   */
  boolean isSortedDescending();

  /**
   * Sets whether or not the value should be set in a descending order.
   *
   * @param sortedDescending
   *          the sortedDescending to set
   */
  void setSortedDescending( boolean sortedDescending );

  /**
   * Returns true if output padding is enabled (padding to specified length).
   *
   * @return true if output padding is enabled (padding to specified length)
   */
  boolean isOutputPaddingEnabled();

  /**
   * Set to true if output padding is to be enabled (padding to specified length).
   *
   * @param outputPaddingEnabled
   *          Set to true if output padding is to be enabled (padding to specified length)
   */
  void setOutputPaddingEnabled( boolean outputPaddingEnabled );

  /**
   * Returns true if this is a large text field (CLOB, TEXT) with arbitrary length.
   *
   * @return true if this is a large text field (CLOB, TEXT) with arbitrary length.
   */
  boolean isLargeTextField();

  /**
   * Set to true if this is to be a large text field (CLOB, TEXT) with arbitrary length.
   *
   * @param largeTextField
   *          Set to true if this is to be a large text field (CLOB, TEXT) with arbitrary length.
   */
  void setLargeTextField( boolean largeTextField );

  /**
   * Returns true of the date format is lenient, false if it is strict. <br/>
   * See also {@link #setDateFormatLenient(boolean)}
   *
   * @return true if the date formatting (parsing) is to be lenient.
   *
   */
  boolean isDateFormatLenient();

  /**
   * Set to true if the date formatting (parsing) is to be set to lenient. Being lenient means that the "date format" is
   * tolerant to some formatting errors. For example, a month specified as "15" will be interpreted as "March": <br/>
   *
   * <pre>
   * 15 - (December = 12) = 3 = March.
   * </pre>
   *
   * Set to false for stricter formatting validation.
   *
   * @param dateFormatLenient
   *          true if the date formatting (parsing) is to be set to lenient.
   */
  void setDateFormatLenient( boolean dateFormatLenient );

  /**
   * Returns the locale from the date format.
   *
   * @return the date format locale
   */
  Locale getDateFormatLocale();

  /**
   * Sets the locale of the date format.
   *
   * @param dateFormatLocale
   *          the date format locale to set
   */
  void setDateFormatLocale( Locale dateFormatLocale );

  /**
   * @return the date format time zone
   */
  TimeZone getDateFormatTimeZone();

  /**
   * @param dateFormatTimeZone
   *          the date format time zone to set
   */
  void setDateFormatTimeZone( TimeZone dateFormatTimeZone );

  /**
   * store original JDBC RecordSetMetaData for later use
   *
   * @see java.sql.ResultSetMetaData
   */

  int getOriginalColumnType();

  /**
   * Sets the original column type.
   *
   * @param originalColumnType
   *          the new original column type
   */
  void setOriginalColumnType( int originalColumnType );

  /**
   * Gets the original column type name.
   *
   * @return the original column type name
   */
  String getOriginalColumnTypeName();

  /**
   * Sets the original column type name.
   *
   * @param originalColumnTypeName
   *          the new original column type name
   */
  void setOriginalColumnTypeName( String originalColumnTypeName );

  /**
   * Gets the original precision.
   *
   * @return the original precision
   */
  int getOriginalPrecision();

  /**
   * Sets the original precision.
   *
   * @param originalPrecision
   *          the new original precision
   */
  void setOriginalPrecision( int originalPrecision );

  /**
   * Gets the original scale.
   *
   * @return the original scale
   */
  int getOriginalScale();

  /**
   * Gets the original nullable.
   *
   * @return the original nullable
   */
  int getOriginalNullable();

  /**
   * Gets the original signed.
   *
   * @return the original signed
   */
  boolean getOriginalSigned();

  /**
   * Sets the original scale.
   *
   * @param originalScale
   *          the new original scale
   */
  void setOriginalScale( int originalScale );

  /**
   * Checks if is original auto increment.
   *
   * @return true, if is original auto increment
   */
  boolean isOriginalAutoIncrement();

  /**
   * Sets the original auto increment.
   *
   * @param originalAutoIncrement
   *          the new original auto increment
   */
  void setOriginalAutoIncrement( boolean originalAutoIncrement );

  /**
   * Checks if is original nullable.
   *
   * @return the int
   */
  int isOriginalNullable();

  /**
   * Sets the original nullable.
   *
   * @param originalNullable
   *          the new original nullable
   */
  void setOriginalNullable( int originalNullable );

  /**
   * Checks if is original signed.
   *
   * @return true, if is original signed
   */
  boolean isOriginalSigned();

  /**
   * Sets the original signed.
   *
   * @param originalSigned
   *          the new original signed
   */
  void setOriginalSigned( boolean originalSigned );

  /* Conversion methods */

  /**
   * Clone value data.
   *
   * @param object
   *          the object
   * @return the object
   * @throws KettleValueException
   *           the kettle value exception
   */
  Object cloneValueData( Object object ) throws KettleValueException;

  /** Convert the supplied data to a String compatible with version 2.5. */
  String getCompatibleString( Object object ) throws KettleValueException;

  /** Convert the supplied data to a String */
  String getString( Object object ) throws KettleValueException;

  /** convert the supplied data to a binary string representation (for writing text) */
  byte[] getBinaryString( Object object ) throws KettleValueException;

  /** Convert the supplied data to a Number */
  Double getNumber( Object object ) throws KettleValueException;

  /** Convert the supplied data to a BigNumber */
  BigDecimal getBigNumber( Object object ) throws KettleValueException;

  /** Convert the supplied data to an Integer */
  Long getInteger( Object object ) throws KettleValueException;

  /** Convert the supplied data to a Date */
  Date getDate( Object object ) throws KettleValueException;

  /** Convert the supplied data to a Boolean */
  Boolean getBoolean( Object object ) throws KettleValueException;

  /** Convert the supplied data to binary data */
  byte[] getBinary( Object object ) throws KettleValueException;

  /**
   * @return a copy of this value meta object
   */
  ValueMetaInterface clone();

  /**
   * Checks whether or not the value is a String.
   *
   * @return true if the value is a String.
   */
  boolean isString();

  /**
   * Checks whether or not this value is a Date
   *
   * @return true if the value is a Date
   */
  boolean isDate();

  /**
   * Checks whether or not the value is a Big Number
   *
   * @return true is this value is a big number
   */
  boolean isBigNumber();

  /**
   * Checks whether or not the value is a Number
   *
   * @return true is this value is a number
   */
  boolean isNumber();

  /**
   * Checks whether or not this value is a boolean
   *
   * @return true if this value has type boolean.
   */
  boolean isBoolean();

  /**
   * Checks whether or not this value is of type Serializable
   *
   * @return true if this value has type Serializable
   */
  boolean isSerializableType();

  /**
   * Checks whether or not this value is of type Binary
   *
   * @return true if this value has type Binary
   */
  boolean isBinary();

  /**
   * Checks whether or not this value is an Integer
   *
   * @return true if this value is an integer
   */
  boolean isInteger();

  /**
   * Checks whether or not this Value is Numeric A Value is numeric if it is either of type Number or Integer
   *
   * @return true if the value is either of type Number or Integer
   */
  boolean isNumeric();

  /**
   * Return the type of a value in a textual form: "String", "Number", "Integer", "Boolean", "Date", ...
   *
   * @return A String describing the type of value.
   */
  String getTypeDesc();

  /**
   * a String text representation of this Value, optionally padded to the specified length
   *
   * @return a String text representation of this Value, optionally padded to the specified length
   */
  String toStringMeta();

  /**
   * Write the content of this class (metadata) to the specified output stream.
   *
   * @param outputStream
   *          the outputstream to write to
   * @throws KettleFileException
   *           in case a I/O error occurs
   */
  void writeMeta( DataOutputStream outputStream ) throws KettleFileException;

  /**
   * Serialize the content of the specified data object to the outputStream. No metadata is written.
   *
   * @param outputStream
   *          the outputstream to write to
   * @param object
   *          the data object to serialize
   * @throws KettleFileException
   *           in case a I/O error occurs
   */
  void writeData( DataOutputStream outputStream, Object object ) throws KettleFileException;

  /**
   * De-serialize data from an inputstream. No metadata is read or changed.
   *
   * @param inputStream
   *          the input stream to read from
   * @return a new data object
   * @throws KettleFileException
   *           in case a I/O error occurs
   * @throws KettleEOFException
   *           When we have read all the data there is to read
   * @throws SocketTimeoutException
   *           In case there is a timeout (when set on a socket) during reading
   */
  Object readData( DataInputStream inputStream ) throws KettleFileException, KettleEOFException,
    SocketTimeoutException;

  /**
   * Read the attributes of this particular value meta object from the specified input stream. Loading the type is not
   * handled here, this should be read from the stream previously!
   *
   * @param inputStream
   *          the input stream to read from
   * @throws KettleFileException
   *           In case there was a IO problem
   * @throws KettleEOFException
   *           If we reached the end of the stream
   */
  void readMetaData( DataInputStream inputStream ) throws KettleFileException, KettleEOFException;

  /**
   * Compare 2 values of the same data type
   *
   * @param data1
   *          the first value
   * @param data2
   *          the second value
   * @return 0 if the values are equal, -1 if data1 is smaller than data2 and +1 if it's larger.
   * @throws KettleValueException
   *           In case we get conversion errors
   */
  int compare( Object data1, Object data2 ) throws KettleValueException;

  /**
   * Compare 2 values of the same data type
   *
   * @param data1
   *          the first value
   * @param meta2
   *          the second value's metadata
   * @param data2
   *          the second value
   * @return 0 if the values are equal, -1 if data1 is smaller than data2 and +1 if it's larger.
   * @throws KettleValueException
   *           In case we get conversion errors
   */
  int compare( Object data1, ValueMetaInterface meta2, Object data2 ) throws KettleValueException;

  /**
   * Convert the specified data to the data type specified in this object.
   *
   * @param meta2
   *          the metadata of the object to be converted
   * @param data2
   *          the data of the object to be converted
   * @return the object in the data type of this value metadata object
   * @throws KettleValueException
   *           in case there is a data conversion error
   */
  Object convertData( ValueMetaInterface meta2, Object data2 ) throws KettleValueException;

  /**
   * Convert the specified data to the data type specified in this object. For String conversion, be compatible with
   * version 2.5.2.
   *
   * @param meta2
   *          the metadata of the object to be converted
   * @param data2
   *          the data of the object to be converted
   * @return the object in the data type of this value metadata object
   * @throws KettleValueException
   *           in case there is a data conversion error
   */
  Object convertDataCompatible( ValueMetaInterface meta2, Object data2 ) throws KettleValueException;

  /**
   * Convert an object to the data type specified in the conversion metadata
   *
   * @param data
   *          The data
   * @return The data converted to the conversion data type
   * @throws KettleValueException
   *           in case there is a conversion error.
   */
  Object convertDataUsingConversionMetaData( Object data ) throws KettleValueException;

  /**
   * Convert the specified string to the data type specified in this object.
   *
   * @param pol
   *          the string to be converted
   * @param convertMeta
   *          the metadata of the object (only string type) to be converted
   * @param nullif
   *          set the object to null if pos equals nullif (IgnoreCase)
   * @param ifNull
   *          set the object to ifNull when pol is empty or null
   * @param trim_type
   *          the trim type to be used (ValueMetaInterface.TRIM_TYPE_XXX)
   * @return the object in the data type of this value metadata object
   * @throws KettleValueException
   *           in case there is a data conversion error
   */
  Object convertDataFromString( String pol, ValueMetaInterface convertMeta, String nullif, String ifNull,
    int trim_type ) throws KettleValueException;

  /**
   * Converts the specified data object to the normal storage type.
   *
   * @param object
   *          the data object to convert
   * @return the data in a normal storage type
   * @throws KettleValueException
   *           In case there is a data conversion error.
   */
  Object convertToNormalStorageType( Object object ) throws KettleValueException;

  /**
   * Convert the given binary data to the actual data type.<br>
   * - byte[] --> Long (Integer)<br>
   * - byte[] --> Double (Number)<br>
   * - byte[] --> BigDecimal (BigNumber)<br>
   * - byte[] --> Date (Date)<br>
   * - byte[] --> Boolean (Boolean)<br>
   * - byte[] --> byte[] (Binary)<br>
   * <br>
   *
   * @param binary
   *          the binary data read from file or database
   * @return the native data type after conversion
   * @throws KettleValueException
   *           in case there is a data conversion error
   */
  Object convertBinaryStringToNativeType( byte[] binary ) throws KettleValueException;

  /**
   * Convert a normal storage type to a binary string object. (for comparison reasons)
   *
   * @param object
   *          The object expressed in a normal storage type
   * @return a binary string
   * @throws KettleValueException
   *           in case there is a data conversion error
   */
  Object convertNormalStorageTypeToBinaryString( Object object ) throws KettleValueException;

  /**
   * Converts the specified data object to the binary string storage type.
   *
   * @param object
   *          the data object to convert
   * @return the data in a binary string storage type
   * @throws KettleValueException
   *           In case there is a data conversion error.
   */
  Object convertToBinaryStringStorageType( Object object ) throws KettleValueException;

  /**
   * Calculate the hashcode of the specified data object
   *
   * @param object
   *          the data value to calculate a hashcode for
   * @return the calculated hashcode
   * @throws KettleValueException
   *           in case there is a data conversion error
   */
  int hashCode( Object object ) throws KettleValueException;

  /**
   * Create an old-style value for backward compatibility reasons
   *
   * @param data
   *          the data to store in the value
   * @return a newly created Value object
   * @throws KettleValueException
   *           in case there is a data conversion problem
   */
  Value createOriginalValue( Object data ) throws KettleValueException;

  /**
   * Extracts the primitive data from an old style Value object
   *
   * @param value
   *          the old style Value object
   * @return the value's data, NOT the meta data.
   * @throws KettleValueException
   *           case there is a data conversion problem
   */
  Object getValueData( Value value ) throws KettleValueException;

  /**
   * Returns the storage Meta data that is needed for internal conversion from BinaryString or String to the specified
   * type. This storage Meta data object survives cloning and should travel through the transformation unchanged as long
   * as the data type remains the same.
   *
   * @return the storage Meta data that is needed for internal conversion from BinaryString or String to the specified
   *         type.
   */
  ValueMetaInterface getStorageMetadata();

  /**
   * Sets the storage meta data.
   *
   * @param storageMetadata
   *          the storage Meta data that is needed for internal conversion from BinaryString or String to the specified
   *          type. This storage Meta data object survives cloning and should travel through the transformation
   *          unchanged as long as the data type remains the same.
   */
  void setStorageMetadata( ValueMetaInterface storageMetadata );

  /**
   * This conversion metadata can be attached to a String object to see where it came from and with which mask it was
   * generated, the encoding, the local languages used, padding, etc.
   *
   * @return The conversion metadata
   */
  ValueMetaInterface getConversionMetadata();

  /**
   * Attach conversion metadata to a String object to see where it came from and with which mask it was generated, the
   * encoding, the local languages used, padding, etc.
   *
   * @param conversionMetadata
   *          the conversionMetadata to set
   */
  void setConversionMetadata( ValueMetaInterface conversionMetadata );

  /**
   * Returns an XML representation of the row metadata.
   *
   * @return an XML representation of the row metadata
   * @throws IOException
   *           Thrown in case there is an (Base64/GZip) decoding problem
   */
  String getMetaXML() throws IOException;

  /**
   * Returns an XML representation of the row data.
   *
   * @param value
   *          The data to serialize as XML
   * @return an XML representation of the row data
   * @throws IOException
   *           Thrown in case there is an (Base64/GZip) decoding problem
   */
  String getDataXML( Object value ) throws IOException;

  /**
   * Convert a data XML node to an Object that corresponds to the metadata. This is basically String to Object
   * conversion that is being done.
   *
   * @param node
   *          the node to retrieve the data value from
   * @return the converted data value
   * @throws KettleException
   *           thrown in case there is a problem with the XML to object conversion
   */
  Object getValue( Node node ) throws KettleException;

  /**
   * Returns the number of binary string to native data type conversions done with this object conversions
   *
   * @return the number of binary string to native data type conversions done with this object conversions
   */
  long getNumberOfBinaryStringConversions();

  /**
   * Returns the number of binary string to native data type done with this object conversions to set.
   *
   * @param numberOfBinaryStringConversions
   *          the number of binary string to native data type done with this object conversions to set
   */
  void setNumberOfBinaryStringConversions( long numberOfBinaryStringConversions );

  /**
   * Returns true if the data type requires a real copy. Usually a binary or Serializable object.
   *
   * @return boolean
   */
  boolean requiresRealClone();

  /**
   * @return true if string to number conversion is occurring in a lenient fashion, parsing numbers successfully until a
   *         non-numeric character is found.
   */
  boolean isLenientStringToNumber();

  /**
   * @param lenientStringToNumber
   *          Set to if string to number conversion is to occur in a lenient fashion, parsing numbers successfully until
   *          a non-numeric character is found.
   */
  void setLenientStringToNumber( boolean lenientStringToNumber );

  /**
   * This method draws the value using the supplied graphical context.
   *
   * @param gc
   *          The graphical context to draw on.
   */
  void drawValue( PrimitiveGCInterface gc, Object value ) throws KettleValueException;

  /**
   * Investigate JDBC result set metadata at the specified index. If this value metadata is interested in handling this
   * SQL type, it should return the value meta. Otherwise it should return null.
   *
   * @param databaseMeta
   *          the database metadata to reference capabilities and so on.
   * @param name
   *          The name of the new value
   * @param rm
   *          The result metadata to investigate
   * @param index
   *          The index to look at (1-based)
   * @param ignoreLength
   *          Don't look at the length
   * @param lazyConversion
   *          use lazy conversion
   * @return The value metadata if this value should handle the SQL type at the specified index.
   * @throws KettleDatabaseException
   *           In case something went wrong.
   */
  ValueMetaInterface getValueFromSQLType( DatabaseMeta databaseMeta, String name, ResultSetMetaData rm,
    int index, boolean ignoreLength, boolean lazyConversion ) throws KettleDatabaseException;

  /**
   * This is a similar method to getValueFromSQLType, but it uses a
   * ResultSet from a call to DatabaseMetaData#getColumns(String, String, String, String)
   * The ResultSet must be positioned correctly on the row to read.
   *
   * <p>Note that the ValueMeta returned by this RowMeta may not contain
   * actual values. This is a lightweight call using only JDBC metadata and does
   * not make use of SQL statements.
   *
   * @param databaseMeta
   *          the database metadata to reference capabilities and so on.
   * @param rs
   *          A ResultSet from getColumns, positioned correctly on a column to read.
   */
  ValueMetaInterface getMetadataPreview( DatabaseMeta databaseMeta, ResultSet rs )
    throws KettleDatabaseException;

  /**
   * Get a value from a result set column based on the current value metadata
   *
   * @param databaseInterface
   *          the database metadata to use
   * @param resultSet
   *          The JDBC result set to read from
   * @param index
   *          The column index (0-based)
   * @return The Kettle native data type based on the value metadata
   * @throws KettleDatabaseException
   *           in case something goes wrong.
   */
  Object getValueFromResultSet( DatabaseInterface databaseInterface, ResultSet resultSet, int index ) throws KettleDatabaseException;

  /**
   * Set a value on a JDBC prepared statement on the specified position
   *
   * @param databaseMeta
   *          the database metadata to reference
   * @param preparedStatement
   *          The prepared statement
   * @param index
   *          the column index (1-based)
   * @param data
   *          the value to set
   * @throws KettleDatabaseException
   *           in case something goes wrong
   */
  void setPreparedStatementValue( DatabaseMeta databaseMeta, PreparedStatement preparedStatement,
    int index, Object data ) throws KettleDatabaseException;

  /**
   * This method gives you the native Java data type corresponding to the value meta-data. Conversions from other
   * storage types and other operations are done automatically according to the specified value meta-data.
   *
   * @param object
   *          The input data
   * @return The native data type
   * @throws KettleValueException
   *           in case there is an unexpected data conversion error.
   */
  Object getNativeDataType( Object object ) throws KettleValueException;

  /**
   * Return the Java class that represents the "native" storage type of this ValueMeta
   *
   * @return A Java class
   */
  Class<?> getNativeDataTypeClass() throws KettleValueException;

  /**
   * Ask for suggestions as to how this plugin data type should be represented in the specified database interface
   *
   * @param databaseInterface
   *          The database type/dialect to get the column type definition for
   * @param tk
   *          Is this a technical key field?
   * @param pk
   *          Is this a primary key field?
   * @param use_autoinc
   *          Use auto-increment?
   * @param add_fieldname
   *          add the fieldname to the column type definition?
   * @param add_cr
   *          add a cariage return to the string?
   * @return The field type definition
   */
  String getDatabaseColumnTypeDefinition( DatabaseInterface databaseInterface, String tk, String pk,
    boolean use_autoinc, boolean add_fieldname, boolean add_cr );

  /**
   * Is Ignore Whitespace
   * Only applicable for TYPE_STRING comparisons
   *
   * @return true if whitespace should be ignored during string comparison
   */
  boolean isIgnoreWhitespace();

  /**
   * Set Ignore Whitespace
   * Only applicable for TYPE_STRING comparisons
   *
   * @param ignoreWhitespace true if whitespace should be ignored during string comparison
   */
  void setIgnoreWhitespace( boolean ignoreWhitespace );

}
