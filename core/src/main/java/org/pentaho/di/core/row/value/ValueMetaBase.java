//CHECKSTYLE:FileLength:OFF
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.row.value;

import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.GreenplumDatabaseMeta;
import org.pentaho.di.core.database.NetezzaDatabaseMeta;
import org.pentaho.di.core.database.OracleDatabaseMeta;
import org.pentaho.di.core.database.PostgreSQLDatabaseMeta;
import org.pentaho.di.core.database.SQLiteDatabaseMeta;
import org.pentaho.di.core.database.TeradataDatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.gui.PrimitiveGCInterface;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.ValueDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ValueMetaBase implements ValueMetaInterface {

  // region Default Numeric Types Parse Format
  public static final String DEFAULT_INTEGER_PARSE_MASK = "####0";
  public static final String DEFAULT_NUMBER_PARSE_MASK = "####0.0#########";
  public static final String DEFAULT_BIGNUMBER_PARSE_MASK = "######0.0###################";

  public static final String DEFAULT_DATE_PARSE_MASK = "yyyy/MM/dd HH:mm:ss.SSS";
  public static final String DEFAULT_TIMESTAMP_PARSE_MASK = "yyyy/MM/dd HH:mm:ss.SSSSSSSSS";
  // endregion

  // region Default Types Format
  public static final String DEFAULT_INTEGER_FORMAT_MASK = Const.NVL(
          EnvUtil.getSystemProperty( Const.KETTLE_DEFAULT_INTEGER_FORMAT ),
          "####0;-####0" );

  public static final String DEFAULT_NUMBER_FORMAT_MASK = Const.NVL(
          EnvUtil.getSystemProperty( Const.KETTLE_DEFAULT_NUMBER_FORMAT ),
          "####0.0#########;-####0.0#########" );

  public static final String DEFAULT_BIG_NUMBER_FORMAT_MASK = Const.NVL(
          EnvUtil.getSystemProperty( Const.KETTLE_DEFAULT_BIGNUMBER_FORMAT ),
          "######0.0###################;-######0.0###################" );

  public static final String DEFAULT_DATE_FORMAT_MASK = Const.NVL( EnvUtil
          .getSystemProperty( Const.KETTLE_DEFAULT_DATE_FORMAT ), "yyyy/MM/dd HH:mm:ss.SSS" );

  public static final String DEFAULT_TIMESTAMP_FORMAT_MASK = Const.NVL( EnvUtil
          .getSystemProperty( Const.KETTLE_DEFAULT_TIMESTAMP_FORMAT ), "yyyy/MM/dd HH:mm:ss.SSSSSSSSS" );
  // endregion

  // region ValueMetaBase Attributes
  protected static Class<?> PKG = Const.class; // for i18n purposes, needed by Translator2

  public static final String XML_META_TAG = "value-meta";
  public static final String XML_DATA_TAG = "value-data";

  public static final String COMPATIBLE_DATE_FORMAT_PATTERN = "yyyy/MM/dd HH:mm:ss.SSS";

  public static final boolean EMPTY_STRING_AND_NULL_ARE_DIFFERENT = convertStringToBoolean(
          Const.NVL( System.getProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "N" ), "N" ) );

  protected String name;
  protected int length;
  protected int precision;
  protected int type;
  protected int trimType;
  protected int storageType;

  protected String origin;
  protected String comments;
  protected Object[] index;
  protected String conversionMask;
  protected String stringEncoding;
  protected String decimalSymbol;
  protected String groupingSymbol;
  protected String currencySymbol;
  protected int collatorStrength;
  protected boolean caseInsensitive;
  protected boolean collatorDisabled;
  protected boolean sortedDescending;
  protected boolean outputPaddingEnabled;
  protected boolean largeTextField;
  protected Locale collatorLocale;
  protected Collator collator;
  protected Locale dateFormatLocale;
  protected TimeZone dateFormatTimeZone;
  protected boolean dateFormatLenient;
  protected boolean lenientStringToNumber;
  protected boolean ignoreTimezone;

  protected SimpleDateFormat dateFormat;
  protected boolean dateFormatChanged;

  protected DecimalFormat decimalFormat;
  protected boolean decimalFormatChanged;

  protected ValueMetaInterface storageMetadata;
  protected boolean identicalFormat;

  protected ValueMetaInterface conversionMetadata;

  boolean singleByteEncoding;

  protected long numberOfBinaryStringConversions;

  protected boolean bigNumberFormatting;

  // get & store original result set meta data for later use
  // @see java.sql.ResultSetMetaData
  //
  protected int originalColumnType;
  protected String originalColumnTypeName;
  protected int originalPrecision;
  protected int originalScale;
  protected boolean originalAutoIncrement;
  protected int originalNullable;
  protected boolean originalSigned;

  private static final LogChannelInterface log = KettleLogStore.getLogChannelInterfaceFactory().create( "ValueMetaBase" );

  /**
   * The trim type codes
   */
  public static final String[] trimTypeCode = { "none", "left", "right", "both" };

  /**
   * The trim description
   */
  public static final String[] trimTypeDesc =
  { BaseMessages.getString( PKG, "ValueMeta.TrimType.None" ),
        BaseMessages.getString( PKG, "ValueMeta.TrimType.Left" ),
        BaseMessages.getString( PKG, "ValueMeta.TrimType.Right" ),
        BaseMessages.getString( PKG, "ValueMeta.TrimType.Both" ) };
  // endregion

  public ValueMetaBase() {
    this( null, ValueMetaInterface.TYPE_NONE, -1, -1 );
  }

  public ValueMetaBase( String name ) {
    this( name, ValueMetaInterface.TYPE_NONE, -1, -1 );
  }

  public ValueMetaBase( String name, int type ) {
    this( name, type, -1, -1 );
  }

  public ValueMetaBase( String name, int type, int length, int precision ) {
    this.name = name;
    this.type = type;
    this.length = length;
    this.precision = precision;
    this.storageType = STORAGE_TYPE_NORMAL;
    this.sortedDescending = false;
    this.outputPaddingEnabled = false;
    this.decimalSymbol = "" + Const.DEFAULT_DECIMAL_SEPARATOR;
    this.groupingSymbol = "" + Const.DEFAULT_GROUPING_SEPARATOR;
    this.dateFormatLocale = Locale.getDefault();
    this.collatorDisabled = true;
    this.collatorLocale = Locale.getDefault();
    this.collator = Collator.getInstance( this.collatorLocale );
    this.collatorStrength = 0;
    this.dateFormatTimeZone = TimeZone.getDefault();
    this.identicalFormat = true;
    this.bigNumberFormatting = true;
    this.lenientStringToNumber =
      convertStringToBoolean( Const.NVL( System.getProperty( Const.KETTLE_LENIENT_STRING_TO_NUMBER_CONVERSION, "N" ),
        "N" ) );
    this.ignoreTimezone =
      convertStringToBoolean( Const.NVL( System.getProperty( Const.KETTLE_COMPATIBILITY_DB_IGNORE_TIMEZONE, "N" ),
        "N" ) );

    determineSingleByteEncoding();
    setDefaultConversionMask();
  }

  public ValueMetaBase( Node node ) throws KettleException {
    this();

    type = getType( XMLHandler.getTagValue( node, "type" ) );
    storageType = getStorageType( XMLHandler.getTagValue( node, "storagetype" ) );

    switch ( storageType ) {
      case STORAGE_TYPE_INDEXED:
        Node indexNode = XMLHandler.getSubNode( node, "index" );
        int nrIndexes = XMLHandler.countNodes( indexNode, "value" );
        index = new Object[nrIndexes];

        for ( int i = 0; i < index.length; i++ ) {
          Node valueNode = XMLHandler.getSubNodeByNr( indexNode, "value", i );
          String valueString = XMLHandler.getNodeValue( valueNode );
          if ( Utils.isEmpty( valueString ) ) {
            index[i] = null;
          } else {
            switch ( type ) {
              case TYPE_STRING:
                index[i] = valueString;
                break;
              case TYPE_NUMBER:
                index[i] = Double.parseDouble( valueString );
                break;
              case TYPE_INTEGER:
                index[i] = Long.parseLong( valueString );
                break;
              case TYPE_DATE:
                index[i] = XMLHandler.stringToDate( valueString );
                break;
              case TYPE_BIGNUMBER:
                index[i] = new BigDecimal( valueString );
                break;
              case TYPE_BOOLEAN:
                index[i] = Boolean.valueOf( "Y".equalsIgnoreCase( valueString ) );
                break;
              case TYPE_BINARY:
                index[i] = XMLHandler.stringToBinary( valueString );
                break;
              default:
                throw new KettleException( toString()
                  + " : Unable to de-serialize indexe storage type from XML for data type " + getType() );
            }
          }
        }
        break;

      case STORAGE_TYPE_BINARY_STRING:
        // Load the storage meta data...
        //
        Node storageMetaNode = XMLHandler.getSubNode( node, "storage-meta" );
        Node storageValueMetaNode = XMLHandler.getSubNode( storageMetaNode, XML_META_TAG );
        if ( storageValueMetaNode != null ) {
          storageMetadata = new ValueMetaBase( storageValueMetaNode );
        }
        break;

      default:
        break;
    }

    name = XMLHandler.getTagValue( node, "name" );
    length = Integer.parseInt( XMLHandler.getTagValue( node, "length" ) );
    precision = Integer.parseInt( XMLHandler.getTagValue( node, "precision" ) );
    origin = XMLHandler.getTagValue( node, "origin" );
    comments = XMLHandler.getTagValue( node, "comments" );
    conversionMask = XMLHandler.getTagValue( node, "conversion_Mask" );
    decimalSymbol = XMLHandler.getTagValue( node, "decimal_symbol" );
    groupingSymbol = XMLHandler.getTagValue( node, "grouping_symbol" );
    currencySymbol = XMLHandler.getTagValue( node, "currency_symbol" );
    trimType = getTrimTypeByCode( XMLHandler.getTagValue( node, "trim_type" ) );
    caseInsensitive = "Y".equalsIgnoreCase( XMLHandler.getTagValue( node, "case_insensitive" ) );
    collatorDisabled = "Y".equalsIgnoreCase( XMLHandler.getTagValue( node, "collator_disabled" ) );
    if ( XMLHandler.getTagValue( node, "collator_strength" ) != null ) {
      collatorStrength = Integer.parseInt( XMLHandler.getTagValue( node, "collator_strength" ) );
    }
    sortedDescending = "Y".equalsIgnoreCase( XMLHandler.getTagValue( node, "sort_descending" ) );
    outputPaddingEnabled = "Y".equalsIgnoreCase( XMLHandler.getTagValue( node, "output_padding" ) );
    dateFormatLenient = "Y".equalsIgnoreCase( XMLHandler.getTagValue( node, "date_format_lenient" ) );
    String dateFormatLocaleString = XMLHandler.getTagValue( node, "date_format_locale" );
    if ( !Utils.isEmpty( dateFormatLocaleString ) ) {
      dateFormatLocale = EnvUtil.createLocale( dateFormatLocaleString );
    }
    String dateTimeZoneString = XMLHandler.getTagValue( node, "date_format_timezone" );
    if ( !Utils.isEmpty( dateTimeZoneString ) ) {
      dateFormatTimeZone = EnvUtil.createTimeZone( dateTimeZoneString );
    } else {
      dateFormatTimeZone = TimeZone.getDefault();
    }
    lenientStringToNumber = "Y".equalsIgnoreCase( XMLHandler.getTagValue( node, "lenient_string_to_number" ) );
  }

  /**
   * Create a new Value meta object.
   *
   * @param inputStream
   * @throws KettleFileException
   * @throws KettleEOFException
   * @deprecated in favor of a combination of {@link ValueMetaFactory}.createValueMeta() and the loadMetaData() method.
   */
  @Deprecated
  public ValueMetaBase( DataInputStream inputStream ) throws KettleFileException, KettleEOFException {
    this();
    try {
      type = inputStream.readInt();
    } catch ( EOFException e ) {
      throw new KettleEOFException( e );
    } catch ( IOException e ) {
      throw new KettleFileException( toString() + " : Unable to read value metadata from input stream", e );
    }

    readMetaData( inputStream );
  }

  /**
   * @deprecated use {@link #ValueMetaBase(String, int)} and {@link #setStorageType(int)} instead
   *
   * @param name
   * @param type
   * @param storageType
   */
  @Deprecated
  public ValueMetaBase( String name, int type, int storageType ) {
    this( name, type, -1, -1 );
    this.storageType = storageType;
    setDefaultConversionMask();
  }

  public static final String[] SINGLE_BYTE_ENCODINGS = new String[] { "ISO8859_1", "Cp1252", "ASCII", "Cp037", "Cp273",
    "Cp277", "Cp278", "Cp280", "Cp284", "Cp285", "Cp297", "Cp420", "Cp424", "Cp437", "Cp500", "Cp737", "Cp775",
    "Cp850", "Cp852", "Cp855", "Cp856", "Cp857", "Cp858", "Cp860", "Cp861", "Cp862", "Cp863", "Cp865", "Cp866",
    "Cp869", "Cp870", "Cp871", "Cp875", "Cp918", "Cp921", "Cp922", "Cp1140", "Cp1141", "Cp1142", "Cp1143", "Cp1144",
    "Cp1145", "Cp1146", "Cp1147", "Cp1148", "Cp1149", "Cp1250", "Cp1251", "Cp1253", "Cp1254", "Cp1255", "Cp1257",
    "ISO8859_2", "ISO8859_3", "ISO8859_5", "ISO8859_5", "ISO8859_6", "ISO8859_7", "ISO8859_8", "ISO8859_9",
    "ISO8859_13", "ISO8859_15", "ISO8859_15_FDIS", "MacCentralEurope", "MacCroatian", "MacCyrillic", "MacDingbat",
    "MacGreek", "MacHebrew", "MacIceland", "MacRoman", "MacRomania", "MacSymbol", "MacTurkish", "MacUkraine", };

  protected void setDefaultConversionMask() {
    // Set some sensible default mask on the numbers
    //
    switch ( getType() ) {
      case TYPE_INTEGER:
        setConversionMask( DEFAULT_INTEGER_FORMAT_MASK );
        break;
      case TYPE_NUMBER:
        setConversionMask( DEFAULT_NUMBER_FORMAT_MASK );
        break;
      case TYPE_BIGNUMBER:
        setConversionMask( DEFAULT_BIG_NUMBER_FORMAT_MASK );

        setGroupingSymbol( null );
        setDecimalSymbol( "." ); // For backward compatibility reasons!
        break;
      default:
        // does nothing
    }
  }

  protected void determineSingleByteEncoding() {
    singleByteEncoding = false;

    Charset cs;
    if ( Utils.isEmpty( stringEncoding ) ) {
      cs = Charset.defaultCharset();
    } else {
      cs = Charset.forName( stringEncoding );
    }

    // See if the default character set for input is single byte encoded.
    //
    for ( String charSetEncoding : SINGLE_BYTE_ENCODINGS ) {
      if ( cs.toString().equalsIgnoreCase( charSetEncoding ) ) {
        singleByteEncoding = true;
      }
    }
  }

  @Override
  public ValueMetaBase clone() {
    try {
      ValueMetaBase valueMeta = (ValueMetaBase) super.clone();
      valueMeta.dateFormat = null;
      valueMeta.decimalFormat = null;
      if ( dateFormatLocale != null ) {
        valueMeta.dateFormatLocale = (Locale) dateFormatLocale.clone();
      }
      if ( dateFormatTimeZone != null ) {
        valueMeta.dateFormatTimeZone = (TimeZone) dateFormatTimeZone.clone();
      }
      if ( storageMetadata != null ) {
        valueMeta.storageMetadata = storageMetadata.clone();
      }
      if ( conversionMetadata != null ) {
        valueMeta.conversionMetadata = conversionMetadata.clone();
      }

      valueMeta.compareStorageAndActualFormat();

      return valueMeta;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  /**
   * @return the comments
   */
  @Override
  public String getComments() {
    return comments;
  }

  /**
   * @param comments
   *          the comments to set
   */
  @Override
  public void setComments( String comments ) {
    this.comments = comments;
  }

  /**
   * @return the index
   */
  @Override
  public Object[] getIndex() {
    return index;
  }

  /**
   * @param index
   *          the index to set
   */
  @Override
  public void setIndex( Object[] index ) {
    this.index = index;
  }

  /**
   * @return the length
   */
  @Override
  public int getLength() {
    return length;
  }

  /**
   * @param length
   *          the length to set
   */
  @Override
  public void setLength( int length ) {
    this.length = length;
  }

  /**
   * @param length
   *          the length to set
   */
  @Override
  public void setLength( int length, int precision ) {
    this.length = length;
    this.precision = precision;
  }

  /**
   *
   * @return
   */
  boolean isLengthInvalidOrZero() {
    return this.length < 1;
  }

  /**
   * @return the name
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  @Override
  public void setName( String name ) {
    this.name = name;
  }

  /**
   * @return the origin
   */
  @Override
  public String getOrigin() {
    return origin;
  }

  /**
   * @param origin
   *          the origin to set
   */
  @Override
  public void setOrigin( String origin ) {
    this.origin = origin;
  }

  /**
   * @return the precision
   */
  @Override
  public int getPrecision() {
    // For backward compatibility we need to tweak a bit...
    //
    if ( isInteger() || isBinary() ) {
      return 0;
    }
    if ( isString() || isBoolean() ) {
      return -1;
    }

    return precision;
  }

  /**
   * @param precision
   *          the precision to set
   */
  @Override
  public void setPrecision( int precision ) {
    this.precision = precision;
  }

  /**
   * @return the storageType
   */
  @Override
  public int getStorageType() {
    return storageType;
  }

  /**
   * @param storageType
   *          the storageType to set
   */
  @Override
  public void setStorageType( int storageType ) {
    this.storageType = storageType;
  }

  @Override
  public boolean isStorageNormal() {
    return storageType == STORAGE_TYPE_NORMAL;
  }

  @Override
  public boolean isStorageIndexed() {
    return storageType == STORAGE_TYPE_INDEXED;
  }

  @Override
  public boolean isStorageBinaryString() {
    return storageType == STORAGE_TYPE_BINARY_STRING;
  }

  /**
   * @return the type
   */
  @Override
  public int getType() {
    return type;
  }

  /**
   * @param type
   *          the type to set
   * @deprecated
   */
  @Override
  @Deprecated
  public void setType( int type ) {
    this.type = type;
  }

  /**
   * @return the conversionMask
   */
  @Override
  @Deprecated
  public String getConversionMask() {
    return conversionMask;
  }

  /**
   * @param conversionMask
   *          the conversionMask to set
   */
  @Override
  public void setConversionMask( String conversionMask ) {
    this.conversionMask = conversionMask;
    dateFormatChanged = true;
    decimalFormatChanged = true;
    compareStorageAndActualFormat();
  }

  /**
   * @return the encoding
   */
  @Override
  public String getStringEncoding() {
    return stringEncoding;
  }

  /**
   * @param encoding
   *          the encoding to set
   */
  @Override
  public void setStringEncoding( String encoding ) {
    this.stringEncoding = encoding;
    determineSingleByteEncoding();
    compareStorageAndActualFormat();
  }

  /**
   * @return the decimalSymbol
   */
  @Override
  public String getDecimalSymbol() {
    return decimalSymbol;
  }

  /**
   * @param decimalSymbol
   *          the decimalSymbol to set
   */
  @Override
  public void setDecimalSymbol( String decimalSymbol ) {
    this.decimalSymbol = decimalSymbol;
    decimalFormatChanged = true;
    compareStorageAndActualFormat();
  }

  /**
   * @return the groupingSymbol
   */
  @Override
  public String getGroupingSymbol() {
    return groupingSymbol;
  }

  /**
   * @param groupingSymbol
   *          the groupingSymbol to set
   */
  @Override
  public void setGroupingSymbol( String groupingSymbol ) {
    this.groupingSymbol = groupingSymbol;
    decimalFormatChanged = true;
    compareStorageAndActualFormat();
  }

  /**
   * @return the currencySymbol
   */
  @Override
  public String getCurrencySymbol() {
    return currencySymbol;
  }

  /**
   * @param currencySymbol
   *          the currencySymbol to set
   */
  @Override
  public void setCurrencySymbol( String currencySymbol ) {
    this.currencySymbol = currencySymbol;
    decimalFormatChanged = true;
  }

  /**
   * @return the caseInsensitive
   */
  @Override
  public boolean isCaseInsensitive() {
    return caseInsensitive;
  }

  /**
   * @param caseInsensitive
   *          the caseInsensitive to set
   */
  @Override
  public void setCaseInsensitive( boolean caseInsensitive ) {
    this.caseInsensitive = caseInsensitive;
  }

   /**
   * @return the collatorDisabled
   */
  @Override
  public boolean isCollatorDisabled() {
    return collatorDisabled;
  }

  /**
   * @param collatorDisabled
   *          the collatorDisabled to set
   */
  @Override
  public void setCollatorDisabled( boolean collatorDisabled ) {
    this.collatorDisabled = collatorDisabled;
  }

  @Override
  public Locale getCollatorLocale() {
    return this.collatorLocale;
  }

  /**
   * @ sets the collator Locale
   */
  @Override
  public void setCollatorLocale( Locale locale ) {
    // Update the collator only if required
    if ( collatorLocale == null || !collatorLocale.equals( locale ) ) {
      this.collatorLocale = locale;
      this.collator = Collator.getInstance( locale );
    }
  }

  /**
   * @get the collatorStrength
   */
  @Override
  public int getCollatorStrength() {
    return collatorStrength;
  }

  /**
   * @param collatorStrength
   *          the collatorStrength to set
   */
  @Override
  public void setCollatorStrength( int collatorStrength ) throws IllegalArgumentException {
    try {
      if ( collator != null ) {
        this.collator.setStrength( collatorStrength );
        this.collatorStrength = collatorStrength;
      }
    } catch ( IllegalArgumentException e ) {
      throw new IllegalArgumentException( " : Collator strength must be an int between 0 and 3. " );
    }
  }

  /**
   * @return the sortedDescending
   */
  @Override
  public boolean isSortedDescending() {
    return sortedDescending;
  }

  /**
   * @param sortedDescending
   *          the sortedDescending to set
   */
  @Override
  public void setSortedDescending( boolean sortedDescending ) {
    this.sortedDescending = sortedDescending;
  }

  /**
   * @return true if output padding is enabled (padding to specified length)
   */
  @Override
  public boolean isOutputPaddingEnabled() {
    return outputPaddingEnabled;
  }

  /**
   * @param outputPaddingEnabled
   *          Set to true if output padding is to be enabled (padding to specified length)
   */
  @Override
  public void setOutputPaddingEnabled( boolean outputPaddingEnabled ) {
    this.outputPaddingEnabled = outputPaddingEnabled;
  }

  /**
   * @return true if this is a large text field (CLOB, TEXT) with arbitrary length.
   */
  @Override
  public boolean isLargeTextField() {
    return largeTextField;
  }

  /**
   * @param largeTextField
   *          Set to true if this is to be a large text field (CLOB, TEXT) with arbitrary length.
   */
  @Override
  public void setLargeTextField( boolean largeTextField ) {
    this.largeTextField = largeTextField;
  }

  /**
   * @return the dateFormatLenient
   */
  @Override
  public boolean isDateFormatLenient() {
    return dateFormatLenient;
  }

  /**
   * @param dateFormatLenient
   *          the dateFormatLenient to set
   */
  @Override
  public void setDateFormatLenient( boolean dateFormatLenient ) {
    this.dateFormatLenient = dateFormatLenient;
    dateFormatChanged = true;
  }

  /**
   * @return the dateFormatLocale
   */
  @Override
  public Locale getDateFormatLocale() {
    return dateFormatLocale;
  }

  /**
   * @param dateFormatLocale
   *          the dateFormatLocale to set
   */
  @Override
  public void setDateFormatLocale( Locale dateFormatLocale ) {
    this.dateFormatLocale = dateFormatLocale;
    dateFormatChanged = true;
  }

  // DATE + STRING

  protected synchronized String convertDateToString( Date date ) {
    if ( date == null ) {
      return null;
    }

    return getDateFormat().format( date );
  }

  protected static SimpleDateFormat compatibleDateFormat = new SimpleDateFormat( COMPATIBLE_DATE_FORMAT_PATTERN );

  protected synchronized String convertDateToCompatibleString( Date date ) {
    if ( date == null ) {
      return null;
    }
    return compatibleDateFormat.format( date );
  }

  protected synchronized Date convertStringToDate( String string ) throws KettleValueException {
    string = Const.trimToType( string, getTrimType() ); // see if trimming needs
    // to be performed before
    // conversion

    if ( Utils.isEmpty( string ) ) {
      return null;
    }

    try {
      ParsePosition pp = new ParsePosition( 0 );
      Date result = getDateFormat( TYPE_DATE ).parse( string, pp );
      if ( pp.getErrorIndex() >= 0 ) {
        // error happen
        throw new ParseException( string, pp.getErrorIndex() );
      }
      // some chars can be after pp.getIndex(). That means, not full value was parsed. For example, for value
      // "25-03-1918 11:54" and format "dd-MM-yyyy", value will be "25-03-1918 00:00" without any exception.
      // If there are only spaces after pp.getIndex() - that means full values was parsed
      return result;
    } catch ( ParseException e ) {
      String dateFormat = ( getDateFormat() != null ) ? getDateFormat().toPattern() : "null";
      throw new KettleValueException( toString() + " : couldn't convert string [" + string
          + "] to a date using format [" + dateFormat + "] on offset location " + e.getErrorOffset(), e );
    }
  }

  // DATE + NUMBER

  protected Double convertDateToNumber( Date date ) {
    return new Double( date.getTime() );
  }

  protected Date convertNumberToDate( Double number ) {
    return new Date( number.longValue() );
  }

  // DATE + INTEGER

  protected Long convertDateToInteger( Date date ) {
    return new Long( date.getTime() );
  }

  protected Date convertIntegerToDate( Long number ) {
    return new Date( number.longValue() );
  }

  // DATE + BIGNUMBER

  protected BigDecimal convertDateToBigNumber( Date date ) {
    return new BigDecimal( date.getTime() );
  }

  protected Date convertBigNumberToDate( BigDecimal number ) {
    return new Date( number.longValue() );
  }

  protected synchronized String convertNumberToString( Double number ) throws KettleValueException {
    if ( number == null ) {
      if ( !outputPaddingEnabled || length < 1 ) {
        return null;
      } else {
        // Return strings padded to the specified length...
        // This is done for backward compatibility with 2.5.x
        // We just optimized this a bit...
        //
        String[] emptyPaddedStrings = Const.getEmptyPaddedStrings();
        if ( length < emptyPaddedStrings.length ) {
          return emptyPaddedStrings[length];
        } else {
          return Const.rightPad( "", length );
        }
      }
    }

    try {
      return getDecimalFormat( false ).format( number );
    } catch ( Exception e ) {
      throw new KettleValueException( toString() + " : couldn't convert Number to String ", e );
    }
  }

  protected synchronized String convertNumberToCompatibleString( Double number ) throws KettleValueException {
    if ( number == null ) {
      return null;
    }
    return Double.toString( number );
  }

  protected synchronized Double convertStringToNumber( String string ) throws KettleValueException {
    string = Const.trimToType( string, getTrimType() ); // see if trimming needs
    // to be performed before
    // conversion

    if ( Utils.isEmpty( string ) ) {
      return null;
    }

    try {
      DecimalFormat format = getDecimalFormat( false );
      Number number;
      if ( lenientStringToNumber ) {
        number = format.parse( string );
      } else {
        ParsePosition parsePosition = new ParsePosition( 0 );
        number = format.parse( string, parsePosition );

        if ( parsePosition.getIndex() < string.length() ) {
          throw new KettleValueException( toString()
              + " : couldn't convert String to number : non-numeric character found at position "
              + ( parsePosition.getIndex() + 1 ) + " for value [" + string + "]" );
        }

      }

      return new Double( number.doubleValue() );
    } catch ( Exception e ) {
      throw new KettleValueException( toString() + " : couldn't convert String to number ", e );
    }
  }

  @Override
  public synchronized SimpleDateFormat getDateFormat() {
    return getDateFormat( getType() );
  }

  private synchronized SimpleDateFormat getDateFormat( int valueMetaType ) {
    // If we have a Date that is represented as a String
    // In that case we can set the format of the original Date on the String
    // value metadata in the form of a conversion metadata object.
    // That way, we can always convert from Date to String and back without a
    // problem, no matter how complex the format was.
    // As such, we should return the date SimpleDateFormat of the conversion
    // metadata.
    //
    if ( conversionMetadata != null ) {
      return conversionMetadata.getDateFormat();
    }

    if ( dateFormat == null || dateFormatChanged ) {
      // This may not become static as the class is not thread-safe!
      dateFormat = new SimpleDateFormat();

      String mask = this.getMask( valueMetaType );

      // Do we have a locale?
      //
      if ( dateFormatLocale == null || dateFormatLocale.equals( Locale.getDefault() ) ) {
        dateFormat = new SimpleDateFormat( mask );
      } else {
        dateFormat = new SimpleDateFormat( mask, dateFormatLocale );
      }

      // Do we have a time zone?
      //
      if ( dateFormatTimeZone != null ) {
        dateFormat.setTimeZone( dateFormatTimeZone );
      }

      // Set the conversion leniency as well
      //
      dateFormat.setLenient( dateFormatLenient );

      dateFormatChanged = false;
    }

    return dateFormat;
  }

  @Override
  public synchronized DecimalFormat getDecimalFormat() {
    return getDecimalFormat( false );
  }

  @Override
  public synchronized DecimalFormat getDecimalFormat( boolean useBigDecimal ) {
    // If we have an Integer that is represented as a String
    // In that case we can set the format of the original Integer on the String
    // value metadata in the form of a conversion metadata object.
    // That way, we can always convert from Integer to String and back without a
    // problem, no matter how complex the format was.
    // As such, we should return the decimal format of the conversion metadata.
    //
    if ( conversionMetadata != null ) {
      return conversionMetadata.getDecimalFormat( useBigDecimal );
    }

    // Calculate the decimal format as few times as possible.
    // That is because creating or changing a DecimalFormat object is very CPU
    // hungry.
    //
    if ( decimalFormat == null || decimalFormatChanged ) {
      decimalFormat = (DecimalFormat) NumberFormat.getInstance();
      decimalFormat.setParseBigDecimal( useBigDecimal );
      DecimalFormatSymbols decimalFormatSymbols = decimalFormat.getDecimalFormatSymbols();

      if ( !Utils.isEmpty( currencySymbol ) ) {
        decimalFormatSymbols.setCurrencySymbol( currencySymbol );
      }
      if ( !Utils.isEmpty( groupingSymbol ) ) {
        decimalFormatSymbols.setGroupingSeparator( groupingSymbol.charAt( 0 ) );
      }
      if ( !Utils.isEmpty( decimalSymbol ) ) {
        decimalFormatSymbols.setDecimalSeparator( decimalSymbol.charAt( 0 ) );
      }
      decimalFormat.setDecimalFormatSymbols( decimalFormatSymbols );

      String decimalPattern = getMask( getType() );
      if ( !Utils.isEmpty( decimalPattern ) ) {
        decimalFormat.applyPattern( decimalPattern );
      }

      decimalFormatChanged = false;
    }

    return decimalFormat;
  }

  @Override
  public String getFormatMask() {
    return getMask( getType() );
  }

  String getMask( int type ) {
    if ( !Utils.isEmpty( this.conversionMask ) ) {
      return this.conversionMask;
    }

    boolean fromString = isString();
    switch ( type ) {
      case TYPE_INTEGER:
        return fromString ? DEFAULT_INTEGER_PARSE_MASK : getIntegerFormatMask();
      case TYPE_NUMBER:
        return fromString ? DEFAULT_NUMBER_PARSE_MASK : getNumberFormatMask();
      case TYPE_BIGNUMBER:
        return fromString ? DEFAULT_BIGNUMBER_PARSE_MASK : getBigNumberFormatMask();

      case TYPE_DATE:
        return fromString ? DEFAULT_DATE_PARSE_MASK : getDateFormatMask();
      case TYPE_TIMESTAMP:
        return fromString ? DEFAULT_TIMESTAMP_PARSE_MASK : getTimestampFormatMask();
    }

    return null;
  }

  String getNumberFormatMask() {
    String numberMask = this.conversionMask;

    if ( Utils.isEmpty( numberMask ) ) {
      if ( this.isLengthInvalidOrZero() ) {
        numberMask = DEFAULT_NUMBER_FORMAT_MASK;
      } else {
        numberMask = this.buildNumberPattern();
      }
    }

    return numberMask;
  }

  String getIntegerFormatMask() {
    String integerMask = this.conversionMask;

    if ( Utils.isEmpty( integerMask ) ) {
      if ( this.isLengthInvalidOrZero() ) {
        integerMask = DEFAULT_INTEGER_FORMAT_MASK;
        // as
        // before
        // version
        // 3.0
      } else {
        StringBuilder integerPattern = new StringBuilder();

        // First the format for positive integers...
        //
        integerPattern.append( " " );
        for ( int i = 0; i < getLength(); i++ ) {
          integerPattern.append( '0' ); // all zeroes.
        }
        integerPattern.append( ";" );

        // Then the format for the negative numbers...
        //
        integerPattern.append( "-" );
        for ( int i = 0; i < getLength(); i++ ) {
          integerPattern.append( '0' ); // all zeroes.
        }

        integerMask = integerPattern.toString();

      }
    }

    return integerMask;
  }

  String getBigNumberFormatMask() {
    String bigNumberMask = this.conversionMask;

    if ( Utils.isEmpty( bigNumberMask ) ) {
      if ( this.isLengthInvalidOrZero() ) {
        bigNumberMask = DEFAULT_BIG_NUMBER_FORMAT_MASK;
      } else {
        bigNumberMask = this.buildNumberPattern();
      }
    }

    return bigNumberMask;
  }

  String getDateFormatMask() {
    String mask = this.conversionMask;
    if ( Utils.isEmpty( mask ) ) {
      mask = DEFAULT_DATE_FORMAT_MASK;
    }

    return mask;
  }

  String getTimestampFormatMask() {
    String mask = conversionMask;
    if ( Utils.isEmpty( mask ) ) {
      mask = DEFAULT_TIMESTAMP_FORMAT_MASK;
    }

    return mask;
  }

  private String buildNumberPattern() {
    StringBuilder numberPattern = new StringBuilder();

    // First do the format for positive numbers...
    //
    numberPattern.append( ' ' ); // to compensate for minus sign.
    if ( precision < 0 ) {
      // Default: two decimals
      for ( int i = 0; i < length; i++ ) {
        numberPattern.append( '0' );
      }
      numberPattern.append( ".00" ); // for the .00
    } else {
      // Floating point format 00001234,56 --> (12,2)
      for ( int i = 0; i <= length; i++ ) {
        numberPattern.append( '0' ); // all zeroes.
      }
      int pos = length - precision + 1;
      if ( pos >= 0 && pos < numberPattern.length() ) {
        numberPattern.setCharAt( length - precision + 1, '.' ); // one
        // 'comma'
      }
    }

    // Now do the format for negative numbers...
    //
    StringBuilder negativePattern = new StringBuilder( numberPattern );
    negativePattern.setCharAt( 0, '-' );

    numberPattern.append( ";" );
    numberPattern.append( negativePattern );

    // Return the pattern...
    //
    return numberPattern.toString();
  }

  protected synchronized String convertIntegerToString( Long integer ) throws KettleValueException {
    if ( integer == null ) {
      if ( !outputPaddingEnabled || length < 1 ) {
        return null;
      } else {
        // Return strings padded to the specified length...
        // This is done for backward compatibility with 2.5.x
        // We just optimized this a bit...
        //
        String[] emptyPaddedStrings = Const.getEmptyPaddedStrings();
        if ( length < emptyPaddedStrings.length ) {
          return emptyPaddedStrings[length];
        } else {
          return Const.rightPad( "", length );
        }
      }
    }

    try {
      return getDecimalFormat( false ).format( integer );
    } catch ( Exception e ) {
      throw new KettleValueException( toString() + " : couldn't convert Long to String ", e );
    }
  }

  protected synchronized String convertIntegerToCompatibleString( Long integer ) throws KettleValueException {
    if ( integer == null ) {
      return null;
    }
    return Long.toString( integer );
  }

  protected synchronized Long convertStringToInteger( String string ) throws KettleValueException {
    string = Const.trimToType( string, getTrimType() ); // see if trimming needs
    // to be performed before
    // conversion

    if ( Utils.isEmpty( string ) ) {
      return null;
    }

    try {
      Number number;
      if ( lenientStringToNumber ) {
        number = new Long( getDecimalFormat( false ).parse( string ).longValue() );
      } else {
        ParsePosition parsePosition = new ParsePosition( 0 );
        number = getDecimalFormat( false ).parse( string, parsePosition );

        if ( parsePosition.getIndex() < string.length() ) {
          throw new KettleValueException( toString()
              + " : couldn't convert String to number : non-numeric character found at position "
              + ( parsePosition.getIndex() + 1 ) + " for value [" + string + "]" );
        }

      }
      return new Long( number.longValue() );
    } catch ( Exception e ) {
      throw new KettleValueException( toString() + " : couldn't convert String to Integer", e );
    }
  }

  protected synchronized String convertBigNumberToString( BigDecimal number ) throws KettleValueException {
    if ( number == null ) {
      return null;
    }

    try {
      return getDecimalFormat( bigNumberFormatting ).format( number );
    } catch ( Exception e ) {
      throw new KettleValueException( toString() + " : couldn't convert BigNumber to String ", e );
    }
  }

  protected synchronized BigDecimal convertStringToBigNumber( String string ) throws KettleValueException {
    string = Const.trimToType( string, getTrimType() ); // see if trimming needs
    // to be performed before
    // conversion

    if ( Utils.isEmpty( string ) ) {
      return null;
    }

    try {
      DecimalFormat format = getDecimalFormat( bigNumberFormatting );
      Number number;
      if ( lenientStringToNumber ) {
        number = format.parse( string );
      } else {
        ParsePosition parsePosition = new ParsePosition( 0 );
        number = format.parse( string, parsePosition );

        if ( parsePosition.getIndex() < string.length() ) {
          throw new KettleValueException( toString()
              + " : couldn't convert String to number : non-numeric character found at position "
              + ( parsePosition.getIndex() + 1 ) + " for value [" + string + "]" );
        }
      }
      return (BigDecimal) number;
    } catch ( Exception e ) {
      // We added this workaround for PDI-1824
      //
      try {
        return new BigDecimal( string );
      } catch ( NumberFormatException ex ) {
        throw new KettleValueException( toString() + " : couldn't convert string value '" + string
            + "' to a big number.", ex );
      }
    }
  }

  // BOOLEAN + STRING

  protected String convertBooleanToString( Boolean bool ) {
    if ( bool == null ) {
      return null;
    }
    if ( length >= 3 ) {
      return bool.booleanValue() ? "true" : "false";
    } else {
      return bool.booleanValue() ? "Y" : "N";
    }
  }

  public static Boolean convertStringToBoolean( String string ) {
    if ( Utils.isEmpty( string ) ) {
      return null;
    }
    return Boolean.valueOf( "Y".equalsIgnoreCase( string ) || "TRUE".equalsIgnoreCase( string )
        || "YES".equalsIgnoreCase( string ) || "1".equals( string ) );
  }

  // BOOLEAN + NUMBER

  protected Double convertBooleanToNumber( Boolean bool ) {
    if ( bool == null ) {
      return null;
    }
    return new Double( bool.booleanValue() ? 1.0 : 0.0 );
  }

  protected Boolean convertNumberToBoolean( Double number ) {
    if ( number == null ) {
      return null;
    }
    return Boolean.valueOf( number.intValue() != 0 );
  }

  // BOOLEAN + INTEGER

  protected Long convertBooleanToInteger( Boolean bool ) {
    if ( bool == null ) {
      return null;
    }
    return Long.valueOf( bool.booleanValue() ? 1L : 0L );
  }

  protected Boolean convertIntegerToBoolean( Long number ) {
    if ( number == null ) {
      return null;
    }
    return Boolean.valueOf( number.longValue() != 0 );
  }

  // BOOLEAN + BIGNUMBER

  protected BigDecimal convertBooleanToBigNumber( Boolean bool ) {
    if ( bool == null ) {
      return null;
    }
    return bool.booleanValue() ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  protected Boolean convertBigNumberToBoolean( BigDecimal number ) {
    if ( number == null ) {
      return null;
    }
    return Boolean.valueOf( number.signum() != 0 );
  }

  /**
   * Converts a byte[] stored in a binary string storage type into a String;
   *
   * @param binary
   *          the binary string
   * @return the String in the correct encoding.
   * @throws KettleValueException
   */
  protected String convertBinaryStringToString( byte[] binary ) throws KettleValueException {
    //noinspection deprecation
    return convertBinaryStringToString( binary, EMPTY_STRING_AND_NULL_ARE_DIFFERENT );
  }

  /*
   * Do not use this method directly! It is for tests!
   */
  @Deprecated
  String convertBinaryStringToString( byte[] binary, boolean emptyStringDiffersFromNull ) throws KettleValueException {
    // OK, so we have an internal representation of the original object, read
    // from file.
    // Before we release it back, we have to see if we don't have to do a
    // String-<type>-String
    // conversion with different masks.
    // This obviously only applies to numeric data and dates.
    // We verify if this is true or false in advance for performance reasons
    //
    // if (binary==null || binary.length==0) return null;
    if ( binary == null || binary.length == 0 ) {
      return ( emptyStringDiffersFromNull && binary != null ) ? "" : null;
    }

    String encoding;
    if ( identicalFormat ) {
      encoding = getStringEncoding();
    } else {
      encoding = storageMetadata.getStringEncoding();
    }

    if ( Utils.isEmpty( encoding ) ) {
      return new String( binary );
    } else {
      try {
        return new String( binary, encoding );
      } catch ( UnsupportedEncodingException e ) {
        throw new KettleValueException( toString()
            + " : couldn't convert binary value to String with specified string encoding [" + stringEncoding + "]", e );
      }
    }
  }

  /**
   * Converts the specified data object to the normal storage type.
   *
   * @param object
   *          the data object to convert
   * @return the data in a normal storage type
   * @throws KettleValueException
   *           In case there is a data conversion error.
   */
  @Override
  public Object convertToNormalStorageType( Object object ) throws KettleValueException {
    if ( object == null ) {
      return null;
    }

    switch ( storageType ) {
      case STORAGE_TYPE_NORMAL:
        return object;
      case STORAGE_TYPE_BINARY_STRING:
        return convertBinaryStringToNativeType( (byte[]) object );
      case STORAGE_TYPE_INDEXED:
        return index[(Integer) object];
      default:
        throw new KettleValueException( toStringMeta() + " : Unknown storage type [" + storageType
            + "] while converting to normal storage type" );
    }
  }

  /**
   * Converts the specified data object to the binary string storage type.
   *
   * @param object
   *          the data object to convert
   * @return the data in a binary string storage type
   * @throws KettleValueException
   *           In case there is a data conversion error.
   */
  @Override
  public Object convertToBinaryStringStorageType( Object object ) throws KettleValueException {
    if ( object == null ) {
      return null;
    }

    switch ( storageType ) {
      case STORAGE_TYPE_NORMAL:
        return convertNormalStorageTypeToBinaryString( object );
      case STORAGE_TYPE_BINARY_STRING:
        return object;
      case STORAGE_TYPE_INDEXED:
        return convertNormalStorageTypeToBinaryString( index[(Integer) object] );
      default:
        throw new KettleValueException( toStringMeta() + " : Unknown storage type [" + storageType
            + "] while converting to normal storage type" );
    }
  }

  /**
   * Convert the binary data to the actual data type.<br>
   * - byte[] --> Long (Integer) - byte[] --> Double (Number) - byte[] --> BigDecimal (BigNumber) - byte[] --> Date
   * (Date) - byte[] --> Boolean (Boolean) - byte[] --> byte[] (Binary)
   *
   * @param binary
   * @return
   * @throws KettleValueException
   */
  @Override
  public Object convertBinaryStringToNativeType( byte[] binary ) throws KettleValueException {
    if ( binary == null ) {
      return null;
    }

    numberOfBinaryStringConversions++;

    // OK, so we have an internal representation of the original object, read
    // from file.
    // First we decode it in the correct encoding
    //
    String string = convertBinaryStringToString( binary );

    // In this method we always must convert the data.
    // We use the storageMetadata object to convert the binary string object.
    //
    // --> Convert from the String format to the current data type...
    //
    return convertData( storageMetadata, string );
  }

  @Override
  public Object convertNormalStorageTypeToBinaryString( Object object ) throws KettleValueException {
    if ( object == null ) {
      return null;
    }

    String string = getString( object );

    return convertStringToBinaryString( string );
  }

  protected byte[] convertStringToBinaryString( String string ) throws KettleValueException {
    if ( string == null ) {
      return null;
    }

    if ( Utils.isEmpty( stringEncoding ) ) {
      return string.getBytes();
    } else {
      try {
        return string.getBytes( stringEncoding );
      } catch ( UnsupportedEncodingException e ) {
        throw new KettleValueException( toString()
            + " : couldn't convert String to Binary with specified string encoding [" + stringEncoding + "]", e );
      }
    }
  }

  /**
   * Clones the data. Normally, we don't have to do anything here, but just for arguments and safety, we do a little
   * extra work in case of binary blobs and Date objects. We should write a programmers manual later on to specify in
   * all clarity that "we always overwrite/replace values in the Object[] data rows, we never modify them" .
   *
   * @return a cloned data object if needed
   */
  @Override
  public Object cloneValueData( Object object ) throws KettleValueException {
    if ( object == null ) {
      return null;
    }

    if ( storageType == STORAGE_TYPE_NORMAL ) {
      switch ( getType() ) {
        case ValueMetaInterface.TYPE_STRING:
        case ValueMetaInterface.TYPE_NUMBER:
        case ValueMetaInterface.TYPE_INTEGER:
        case ValueMetaInterface.TYPE_BOOLEAN:
        case ValueMetaInterface.TYPE_BIGNUMBER: // primitive data types: we can only
          // overwrite these, not change them
          return object;

        case ValueMetaInterface.TYPE_DATE:
          return new Date( ( (Date) object ).getTime() ); // just to make sure: very
          // inexpensive too.

        case ValueMetaInterface.TYPE_BINARY:
          byte[] origin = (byte[]) object;
          byte[] target = new byte[origin.length];
          System.arraycopy( origin, 0, target, 0, origin.length );
          return target;

        case ValueMetaInterface.TYPE_SERIALIZABLE:
          // Let's not create a copy but simply return the same value.
          //
          return object;

        default:
          throw new KettleValueException( toString() + ": unable to make copy of value type: " + getType() );
      }
    } else {

      return object;

    }
  }

  @Override
  public String getCompatibleString( Object object ) throws KettleValueException {
    try {
      String string;

      switch ( type ) {
        case TYPE_DATE:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              string = convertDateToCompatibleString( (Date) object );
              break;
            case STORAGE_TYPE_BINARY_STRING:
              string = convertDateToCompatibleString( (Date) convertBinaryStringToNativeType( (byte[]) object ) );
              break;
            case STORAGE_TYPE_INDEXED:
              if ( object == null ) {
                string = null;
              } else {
                string = convertDateToCompatibleString( (Date) index[( (Integer) object ).intValue()] );
              }
              break;
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
          break;

        case TYPE_NUMBER:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              string = convertNumberToCompatibleString( (Double) object );
              break;
            case STORAGE_TYPE_BINARY_STRING:
              string = convertNumberToCompatibleString( (Double) convertBinaryStringToNativeType( (byte[]) object ) );
              break;
            case STORAGE_TYPE_INDEXED:
              string =
                  object == null ? null : convertNumberToCompatibleString( (Double) index[( (Integer) object )
                      .intValue()] );
              break;
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
          break;

        case TYPE_INTEGER:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              string = convertIntegerToCompatibleString( (Long) object );
              break;
            case STORAGE_TYPE_BINARY_STRING:
              try {
                string = convertIntegerToCompatibleString( (Long) convertBinaryStringToNativeType( (byte[]) object ) );
              } catch ( ClassCastException e ) {
                string = convertIntegerToCompatibleString( (Long) object );
              }
              break;
            case STORAGE_TYPE_INDEXED:
              string =
                  object == null ? null : convertIntegerToCompatibleString( (Long) index[( (Integer) object )
                      .intValue()] );
              break;
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
          break;

        default:
          return getString( object );
      }

      return string;
    } catch ( ClassCastException e ) {
      throw new KettleValueException( toString() + " : There was a data type error: the data type of "
          + object.getClass().getName() + " object [" + object + "] does not correspond to value meta ["
          + toStringMeta() + "]" );
    }
  }

  @Override
  public String getString( Object object ) throws KettleValueException {
    try {
      String string;

      switch ( type ) {
        case TYPE_STRING:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              string = object == null ? null : object.toString();
              break;
            case STORAGE_TYPE_BINARY_STRING:
              string = (String) convertBinaryStringToNativeType( (byte[]) object );
              break;
            case STORAGE_TYPE_INDEXED:
              string = object == null ? null : (String) index[( (Integer) object ).intValue()];
              break;
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
          if ( string != null ) {
            string = trim( string );
          }
          break;

        case TYPE_DATE:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              string = convertDateToString( (Date) object );
              break;
            case STORAGE_TYPE_BINARY_STRING:
              string = convertDateToString( (Date) convertBinaryStringToNativeType( (byte[]) object ) );
              break;
            case STORAGE_TYPE_INDEXED:
              string = object == null ? null : convertDateToString( (Date) index[( (Integer) object ).intValue()] );
              break;
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
          break;

        case TYPE_NUMBER:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              string = convertNumberToString( (Double) object );
              break;
            case STORAGE_TYPE_BINARY_STRING:
              string = convertNumberToString( (Double) convertBinaryStringToNativeType( (byte[]) object ) );
              break;
            case STORAGE_TYPE_INDEXED:
              string = object == null ? null : convertNumberToString( (Double) index[( (Integer) object ).intValue()] );
              break;
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
          break;

        case TYPE_INTEGER:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              string = convertIntegerToString( (Long) object );
              break;
            case STORAGE_TYPE_BINARY_STRING:
              string = convertIntegerToString( (Long) convertBinaryStringToNativeType( (byte[]) object ) );
              break;
            case STORAGE_TYPE_INDEXED:
              string = object == null ? null : convertIntegerToString( (Long) index[( (Integer) object ).intValue()] );
              break;
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
          break;

        case TYPE_BIGNUMBER:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              string = convertBigNumberToString( (BigDecimal) object );
              break;
            case STORAGE_TYPE_BINARY_STRING:
              string = convertBigNumberToString( (BigDecimal) convertBinaryStringToNativeType( (byte[]) object ) );
              break;
            case STORAGE_TYPE_INDEXED:
              string =
                  object == null ? null
                      : convertBigNumberToString( (BigDecimal) index[( (Integer) object ).intValue()] );
              break;
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
          break;

        case TYPE_BOOLEAN:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              string = convertBooleanToString( (Boolean) object );
              break;
            case STORAGE_TYPE_BINARY_STRING:
              string = convertBooleanToString( (Boolean) convertBinaryStringToNativeType( (byte[]) object ) );
              break;
            case STORAGE_TYPE_INDEXED:
              string =
                  object == null ? null : convertBooleanToString( (Boolean) index[( (Integer) object ).intValue()] );
              break;
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
          break;

        case TYPE_BINARY:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              string = convertBinaryStringToString( (byte[]) object );
              break;
            case STORAGE_TYPE_BINARY_STRING:
              string = convertBinaryStringToString( (byte[]) object );
              break;
            case STORAGE_TYPE_INDEXED:
              string =
                  object == null ? null : convertBinaryStringToString( (byte[]) index[( (Integer) object ).intValue()] );
              break;
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
          break;

        case TYPE_SERIALIZABLE:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              string = object == null ? null : object.toString();
              break; // just go for the default toString()
            case STORAGE_TYPE_BINARY_STRING:
              string = convertBinaryStringToString( (byte[]) object );
              break;
            case STORAGE_TYPE_INDEXED:
              string = object == null ? null : index[( (Integer) object ).intValue()].toString();
              break; // just go for the default toString()
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
          break;

        default:
          throw new KettleValueException( toString() + " : Unknown type " + type + " specified." );
      }

      if ( isOutputPaddingEnabled() && getLength() > 0 ) {
        string = ValueDataUtil.rightPad( string, getLength() );
      }

      return string;
    } catch ( ClassCastException e ) {
      throw new KettleValueException( toString() + " : There was a data type error: the data type of "
          + object.getClass().getName() + " object [" + object + "] does not correspond to value meta ["
          + toStringMeta() + "]" );
    }
  }

  protected String trim( String string ) {
    switch ( getTrimType() ) {
      case TRIM_TYPE_NONE:
        break;
      case TRIM_TYPE_RIGHT:
        string = Const.rtrim( string );
        break;
      case TRIM_TYPE_LEFT:
        string = Const.ltrim( string );
        break;
      case TRIM_TYPE_BOTH:
        string = Const.trim( string );
        break;
      default:
        break;
    }
    return string;
  }

  @Override
  public Double getNumber( Object object ) throws KettleValueException {
    try {
      if ( isNull( object ) ) {
        return null;
      }
      switch ( type ) {
        case TYPE_NUMBER:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return (Double) object;
            case STORAGE_TYPE_BINARY_STRING:
              return (Double) convertBinaryStringToNativeType( (byte[]) object );
            case STORAGE_TYPE_INDEXED:
              return (Double) index[( (Integer) object ).intValue()];
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_STRING:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return convertStringToNumber( (String) object );
            case STORAGE_TYPE_BINARY_STRING:
              return convertStringToNumber( (String) convertBinaryStringToNativeType( (byte[]) object ) );
            case STORAGE_TYPE_INDEXED:
              return convertStringToNumber( (String) index[( (Integer) object ).intValue()] );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_DATE:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return convertDateToNumber( (Date) object );
            case STORAGE_TYPE_BINARY_STRING:
              return convertDateToNumber( (Date) convertBinaryStringToNativeType( (byte[]) object ) );
            case STORAGE_TYPE_INDEXED:
              return new Double( ( (Date) index[( (Integer) object ).intValue()] ).getTime() );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_INTEGER:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return new Double( ( (Long) object ).doubleValue() );
            case STORAGE_TYPE_BINARY_STRING:
              return new Double( ( (Long) convertBinaryStringToNativeType( (byte[]) object ) ).doubleValue() );
            case STORAGE_TYPE_INDEXED:
              return new Double( ( (Long) index[( (Integer) object ).intValue()] ).doubleValue() );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_BIGNUMBER:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return new Double( ( (BigDecimal) object ).doubleValue() );
            case STORAGE_TYPE_BINARY_STRING:
              return new Double( ( (BigDecimal) convertBinaryStringToNativeType( (byte[]) object ) ).doubleValue() );
            case STORAGE_TYPE_INDEXED:
              return new Double( ( (BigDecimal) index[( (Integer) object ).intValue()] ).doubleValue() );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_BOOLEAN:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return convertBooleanToNumber( (Boolean) object );
            case STORAGE_TYPE_BINARY_STRING:
              return convertBooleanToNumber( (Boolean) convertBinaryStringToNativeType( (byte[]) object ) );
            case STORAGE_TYPE_INDEXED:
              return convertBooleanToNumber( (Boolean) index[( (Integer) object ).intValue()] );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_BINARY:
          throw new KettleValueException( toString() + " : I don't know how to convert binary values to numbers." );
        case TYPE_SERIALIZABLE:
          throw new KettleValueException( toString() + " : I don't know how to convert serializable values to numbers." );
        default:
          throw new KettleValueException( toString() + " : Unknown type " + type + " specified." );
      }
    } catch ( Exception e ) {
      throw new KettleValueException( "Unexpected conversion error while converting value [" + toString()
          + "] to a Number", e );
    }
  }

  @Override
  public Long getInteger( Object object ) throws KettleValueException {
    try {
      if ( isNull( object ) ) {
        return null;
      }
      switch ( type ) {
        case TYPE_INTEGER:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return (Long) object;
            case STORAGE_TYPE_BINARY_STRING:
              return (Long) convertBinaryStringToNativeType( (byte[]) object );
            case STORAGE_TYPE_INDEXED:
              return (Long) index[( (Integer) object ).intValue()];
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_STRING:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return convertStringToInteger( (String) object );
            case STORAGE_TYPE_BINARY_STRING:
              return convertStringToInteger( (String) convertBinaryStringToNativeType( (byte[]) object ) );
            case STORAGE_TYPE_INDEXED:
              return convertStringToInteger( (String) index[( (Integer) object ).intValue()] );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_NUMBER:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return new Long( Math.round( ( (Double) object ).doubleValue() ) );
            case STORAGE_TYPE_BINARY_STRING:
              return new Long( Math.round( ( (Double) convertBinaryStringToNativeType( (byte[]) object ) )
                  .doubleValue() ) );
            case STORAGE_TYPE_INDEXED:
              return new Long( Math.round( ( (Double) index[( (Integer) object ).intValue()] ).doubleValue() ) );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_DATE:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return convertDateToInteger( (Date) object );
            case STORAGE_TYPE_BINARY_STRING:
              return new Long( ( (Date) convertBinaryStringToNativeType( (byte[]) object ) ).getTime() );
            case STORAGE_TYPE_INDEXED:
              return convertDateToInteger( (Date) index[( (Integer) object ).intValue()] );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_BIGNUMBER:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return new Long( ( (BigDecimal) object ).longValue() );
            case STORAGE_TYPE_BINARY_STRING:
              return new Long( ( (BigDecimal) convertBinaryStringToNativeType( (byte[]) object ) ).longValue() );
            case STORAGE_TYPE_INDEXED:
              return new Long( ( (BigDecimal) index[( (Integer) object ).intValue()] ).longValue() );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_BOOLEAN:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return convertBooleanToInteger( (Boolean) object );
            case STORAGE_TYPE_BINARY_STRING:
              return convertBooleanToInteger( (Boolean) convertBinaryStringToNativeType( (byte[]) object ) );
            case STORAGE_TYPE_INDEXED:
              return convertBooleanToInteger( (Boolean) index[( (Integer) object ).intValue()] );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_BINARY:
          throw new KettleValueException( toString() + " : I don't know how to convert binary values to integers." );
        case TYPE_SERIALIZABLE:
          throw new KettleValueException( toString()
              + " : I don't know how to convert serializable values to integers." );
        default:
          throw new KettleValueException( toString() + " : Unknown type " + type + " specified." );
      }
    } catch ( Exception e ) {
      throw new KettleValueException( "Unexpected conversion error while converting value [" + toString()
          + "] to an Integer", e );
    }
  }

  @Override
  public BigDecimal getBigNumber( Object object ) throws KettleValueException {
    try {
      if ( isNull( object ) ) {
        return null;
      }
      switch ( type ) {
        case TYPE_BIGNUMBER:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return (BigDecimal) object;
            case STORAGE_TYPE_BINARY_STRING:
              return (BigDecimal) convertBinaryStringToNativeType( (byte[]) object );
            case STORAGE_TYPE_INDEXED:
              return (BigDecimal) index[( (Integer) object ).intValue()];
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_STRING:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return convertStringToBigNumber( (String) object );
            case STORAGE_TYPE_BINARY_STRING:
              return convertStringToBigNumber( (String) convertBinaryStringToNativeType( (byte[]) object ) );
            case STORAGE_TYPE_INDEXED:
              return convertStringToBigNumber( (String) index[( (Integer) object ).intValue()] );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_INTEGER:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return BigDecimal.valueOf( ( (Long) object ).longValue() );
            case STORAGE_TYPE_BINARY_STRING:
              return BigDecimal.valueOf( ( (Long) convertBinaryStringToNativeType( (byte[]) object ) ).longValue() );
            case STORAGE_TYPE_INDEXED:
              return BigDecimal.valueOf( ( (Long) index[( (Integer) object ).intValue()] ).longValue() );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_NUMBER:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return BigDecimal.valueOf( ( (Double) object ).doubleValue() );
            case STORAGE_TYPE_BINARY_STRING:
              return BigDecimal.valueOf( ( (Double) convertBinaryStringToNativeType( (byte[]) object ) ).doubleValue() );
            case STORAGE_TYPE_INDEXED:
              return BigDecimal.valueOf( ( (Double) index[( (Integer) object ).intValue()] ).doubleValue() );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_DATE:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return convertDateToBigNumber( (Date) object );
            case STORAGE_TYPE_BINARY_STRING:
              return convertDateToBigNumber( (Date) convertBinaryStringToNativeType( (byte[]) object ) );
            case STORAGE_TYPE_INDEXED:
              return convertDateToBigNumber( (Date) index[( (Integer) object ).intValue()] );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_BOOLEAN:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return convertBooleanToBigNumber( (Boolean) object );
            case STORAGE_TYPE_BINARY_STRING:
              return convertBooleanToBigNumber( (Boolean) convertBinaryStringToNativeType( (byte[]) object ) );
            case STORAGE_TYPE_INDEXED:
              return convertBooleanToBigNumber( (Boolean) index[( (Integer) object ).intValue()] );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_BINARY:
          throw new KettleValueException( toString() + " : I don't know how to convert binary values to BigDecimals." );
        case TYPE_SERIALIZABLE:
          throw new KettleValueException( toString() + " : I don't know how to convert serializable values to BigDecimals." );
        default:
          throw new KettleValueException( toString() + " : Unknown type " + type + " specified." );
      }
    } catch ( Exception e ) {
      throw new KettleValueException( "Unexpected conversion error while converting value [" + toString()
          + "] to a BigNumber", e );
    }
  }

  @Override
  public Boolean getBoolean( Object object ) throws KettleValueException {
    if ( object == null ) {
      return null;
    }
    switch ( type ) {
      case TYPE_BOOLEAN:
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            return (Boolean) object;
          case STORAGE_TYPE_BINARY_STRING:
            return (Boolean) convertBinaryStringToNativeType( (byte[]) object );
          case STORAGE_TYPE_INDEXED:
            return (Boolean) index[( (Integer) object ).intValue()];
          default:
            throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
        }
      case TYPE_STRING:
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            return convertStringToBoolean( trim( (String) object ) );
          case STORAGE_TYPE_BINARY_STRING:
            return convertStringToBoolean( trim( (String) convertBinaryStringToNativeType( (byte[]) object ) ) );
          case STORAGE_TYPE_INDEXED:
            return convertStringToBoolean( trim( (String) index[( (Integer) object ).intValue()] ) );
          default:
            throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
        }
      case TYPE_INTEGER:
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            return convertIntegerToBoolean( (Long) object );
          case STORAGE_TYPE_BINARY_STRING:
            return convertIntegerToBoolean( (Long) convertBinaryStringToNativeType( (byte[]) object ) );
          case STORAGE_TYPE_INDEXED:
            return convertIntegerToBoolean( (Long) index[( (Integer) object ).intValue()] );
          default:
            throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
        }
      case TYPE_NUMBER:
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            return convertNumberToBoolean( (Double) object );
          case STORAGE_TYPE_BINARY_STRING:
            return convertNumberToBoolean( (Double) convertBinaryStringToNativeType( (byte[]) object ) );
          case STORAGE_TYPE_INDEXED:
            return convertNumberToBoolean( (Double) index[( (Integer) object ).intValue()] );
          default:
            throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
        }
      case TYPE_BIGNUMBER:
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            return convertBigNumberToBoolean( (BigDecimal) object );
          case STORAGE_TYPE_BINARY_STRING:
            return convertBigNumberToBoolean( (BigDecimal) convertBinaryStringToNativeType( (byte[]) object ) );
          case STORAGE_TYPE_INDEXED:
            return convertBigNumberToBoolean( (BigDecimal) index[( (Integer) object ).intValue()] );
          default:
            throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
        }
      case TYPE_DATE:
        throw new KettleValueException( toString() + " : I don't know how to convert date values to booleans." );
      case TYPE_BINARY:
        throw new KettleValueException( toString() + " : I don't know how to convert binary values to booleans." );
      case TYPE_SERIALIZABLE:
        throw new KettleValueException( toString() + " : I don't know how to convert serializable values to booleans." );
      default:
        throw new KettleValueException( toString() + " : Unknown type " + type + " specified." );
    }
  }

  @Override
  public Date getDate( Object object ) throws KettleValueException {
    if ( isNull( object ) ) {
      return null;
    }
    switch ( type ) {
      case TYPE_DATE:
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            return (Date) object;
          case STORAGE_TYPE_BINARY_STRING:
            return (Date) convertBinaryStringToNativeType( (byte[]) object );
          case STORAGE_TYPE_INDEXED:
            return (Date) index[( (Integer) object ).intValue()];
          default:
            throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
        }
      case TYPE_STRING:
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            return convertStringToDate( (String) object );
          case STORAGE_TYPE_BINARY_STRING:
            return convertStringToDate( (String) convertBinaryStringToNativeType( (byte[]) object ) );
          case STORAGE_TYPE_INDEXED:
            return convertStringToDate( (String) index[( (Integer) object ).intValue()] );
          default:
            throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
        }
      case TYPE_NUMBER:
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            return convertNumberToDate( (Double) object );
          case STORAGE_TYPE_BINARY_STRING:
            return convertNumberToDate( (Double) convertBinaryStringToNativeType( (byte[]) object ) );
          case STORAGE_TYPE_INDEXED:
            return convertNumberToDate( (Double) index[( (Integer) object ).intValue()] );
          default:
            throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
        }
      case TYPE_INTEGER:
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            return convertIntegerToDate( (Long) object );
          case STORAGE_TYPE_BINARY_STRING:
            return convertIntegerToDate( (Long) convertBinaryStringToNativeType( (byte[]) object ) );
          case STORAGE_TYPE_INDEXED:
            return convertIntegerToDate( (Long) index[( (Integer) object ).intValue()] );
          default:
            throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
        }
      case TYPE_BIGNUMBER:
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            return convertBigNumberToDate( (BigDecimal) object );
          case STORAGE_TYPE_BINARY_STRING:
            return convertBigNumberToDate( (BigDecimal) convertBinaryStringToNativeType( (byte[]) object ) );
          case STORAGE_TYPE_INDEXED:
            return convertBigNumberToDate( (BigDecimal) index[( (Integer) object ).intValue()] );
          default:
            throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
        }
      case TYPE_BOOLEAN:
        throw new KettleValueException( toString() + " : I don't know how to convert a boolean to a date." );
      case TYPE_BINARY:
        throw new KettleValueException( toString() + " : I don't know how to convert a binary value to date." );
      case TYPE_SERIALIZABLE:
        throw new KettleValueException( toString() + " : I don't know how to convert a serializable value to date." );

      default:
        throw new KettleValueException( toString() + " : Unknown type " + type + " specified." );
    }
  }

  @Override
  public byte[] getBinary( Object object ) throws KettleValueException {
    if ( object == null ) {
      return null;
    }
    switch ( type ) {
      case TYPE_BINARY:
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            return (byte[]) object;
          case STORAGE_TYPE_BINARY_STRING:
            return (byte[]) object;
          case STORAGE_TYPE_INDEXED:
            return (byte[]) index[( (Integer) object ).intValue()];
          default:
            throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
        }
      case TYPE_DATE:
        throw new KettleValueException( toString() + " : I don't know how to convert a date to binary." );
      case TYPE_STRING:
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            return convertStringToBinaryString( (String) object );
          case STORAGE_TYPE_BINARY_STRING:
            return (byte[]) object;
          case STORAGE_TYPE_INDEXED:
            return convertStringToBinaryString( (String) index[( (Integer) object ).intValue()] );
          default:
            throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
        }
      case TYPE_NUMBER:
        throw new KettleValueException( toString() + " : I don't know how to convert a number to binary." );
      case TYPE_INTEGER:
        throw new KettleValueException( toString() + " : I don't know how to convert an integer to binary." );
      case TYPE_BIGNUMBER:
        throw new KettleValueException( toString() + " : I don't know how to convert a bignumber to binary." );
      case TYPE_BOOLEAN:
        throw new KettleValueException( toString() + " : I don't know how to convert a boolean to binary." );
      case TYPE_SERIALIZABLE:
        throw new KettleValueException( toString() + " : I don't know how to convert a serializable to binary." );

      default:
        throw new KettleValueException( toString() + " : Unknown type " + type + " specified." );
    }
  }

  @Override
  public byte[] getBinaryString( Object object ) throws KettleValueException {
    // If the input is a binary string, we should return the exact same binary
    // object IF
    // and only IF the formatting options for the storage metadata and this
    // object are the same.
    //
    if ( isStorageBinaryString() && identicalFormat ) {
      return (byte[]) object; // shortcut it directly for better performance.
    }

    try {
      if ( object == null ) {
        return null;
      }

      switch ( type ) {
        case TYPE_STRING:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return convertStringToBinaryString( (String) object );
            case STORAGE_TYPE_BINARY_STRING:
              return convertStringToBinaryString( (String) convertBinaryStringToNativeType( (byte[]) object ) );
            case STORAGE_TYPE_INDEXED:
              return convertStringToBinaryString( (String) index[( (Integer) object ).intValue()] );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }

        case TYPE_DATE:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return convertStringToBinaryString( convertDateToString( (Date) object ) );
            case STORAGE_TYPE_BINARY_STRING:
              String string = convertDateToString( (Date) convertBinaryStringToNativeType( (byte[]) object ) );
              return convertStringToBinaryString( string );
            case STORAGE_TYPE_INDEXED:
              return convertStringToBinaryString( convertDateToString( (Date) index[( (Integer) object ).intValue()] ) );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }

        case TYPE_NUMBER:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return convertStringToBinaryString( convertNumberToString( (Double) object ) );
            case STORAGE_TYPE_BINARY_STRING:
              String string = convertNumberToString( (Double) convertBinaryStringToNativeType( (byte[]) object ) );
              return convertStringToBinaryString( string );
            case STORAGE_TYPE_INDEXED:
              return convertStringToBinaryString( convertNumberToString( (Double) index[( (Integer) object ).intValue()] ) );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }

        case TYPE_INTEGER:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return convertStringToBinaryString( convertIntegerToString( (Long) object ) );
            case STORAGE_TYPE_BINARY_STRING:
              String string = convertIntegerToString( (Long) convertBinaryStringToNativeType( (byte[]) object ) );
              return convertStringToBinaryString( string );
            case STORAGE_TYPE_INDEXED:
              return convertStringToBinaryString( convertIntegerToString( (Long) index[( (Integer) object ).intValue()] ) );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }

        case TYPE_BIGNUMBER:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return convertStringToBinaryString( convertBigNumberToString( (BigDecimal) object ) );
            case STORAGE_TYPE_BINARY_STRING:
              String string =
                  convertBigNumberToString( (BigDecimal) convertBinaryStringToNativeType( (byte[]) object ) );
              return convertStringToBinaryString( string );
            case STORAGE_TYPE_INDEXED:
              return convertStringToBinaryString( convertBigNumberToString( (BigDecimal) index[( (Integer) object )
                  .intValue()] ) );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }

        case TYPE_BOOLEAN:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return convertStringToBinaryString( convertBooleanToString( (Boolean) object ) );
            case STORAGE_TYPE_BINARY_STRING:
              String string = convertBooleanToString( (Boolean) convertBinaryStringToNativeType( (byte[]) object ) );
              return convertStringToBinaryString( string );
            case STORAGE_TYPE_INDEXED:
              return convertStringToBinaryString( convertBooleanToString( (Boolean) index[( (Integer) object )
                  .intValue()] ) );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }

        case TYPE_BINARY:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return (byte[]) object;
            case STORAGE_TYPE_BINARY_STRING:
              return (byte[]) object;
            case STORAGE_TYPE_INDEXED:
              return (byte[]) index[( (Integer) object ).intValue()];
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }

        case TYPE_SERIALIZABLE:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return convertStringToBinaryString( object.toString() );
            case STORAGE_TYPE_BINARY_STRING:
              return (byte[]) object;
            case STORAGE_TYPE_INDEXED:
              return convertStringToBinaryString( index[( (Integer) object ).intValue()].toString() );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }

        default:
          throw new KettleValueException( toString() + " : Unknown type " + type + " specified." );
      }
    } catch ( ClassCastException e ) {
      throw new KettleValueException( toString() + " : There was a data type error: the data type of "
          + object.getClass().getName() + " object [" + object + "] does not correspond to value meta ["
          + toStringMeta() + "]" );
    }
  }

  /**
   * Checks whether or not the value is a String.
   *
   * @return true if the value is a String.
   */
  @Override
  public boolean isString() {
    return type == TYPE_STRING;
  }

  /**
   * Checks whether or not this value is a Date
   *
   * @return true if the value is a Date
   */
  @Override
  public boolean isDate() {
    return type == TYPE_DATE || type == TYPE_TIMESTAMP;
  }

  /**
   * Checks whether or not the value is a Big Number
   *
   * @return true is this value is a big number
   */
  @Override
  public boolean isBigNumber() {
    return type == TYPE_BIGNUMBER;
  }

  /**
   * Checks whether or not the value is a Number
   *
   * @return true is this value is a number
   */
  @Override
  public boolean isNumber() {
    return type == TYPE_NUMBER;
  }

  /**
   * Checks whether or not this value is a boolean
   *
   * @return true if this value has type boolean.
   */
  @Override
  public boolean isBoolean() {
    return type == TYPE_BOOLEAN;
  }

  /**
   * Checks whether or not this value is of type Serializable
   *
   * @return true if this value has type Serializable
   */
  @Override
  public boolean isSerializableType() {
    return type == TYPE_SERIALIZABLE;
  }

  /**
   * Checks whether or not this value is of type Binary
   *
   * @return true if this value has type Binary
   */
  @Override
  public boolean isBinary() {
    return type == TYPE_BINARY;
  }

  /**
   * Checks whether or not this value is an Integer
   *
   * @return true if this value is an integer
   */
  @Override
  public boolean isInteger() {
    return type == TYPE_INTEGER;
  }

  /**
   * Checks whether or not this Value is Numeric A Value is numeric if it is either of type Number or Integer
   *
   * @return true if the value is either of type Number or Integer
   */
  @Override
  public boolean isNumeric() {
    return isInteger() || isNumber() || isBigNumber();
  }

  /**
   * Checks whether or not the specified type is either Integer or Number
   *
   * @param t
   *          the type to check
   * @return true if the type is Integer or Number
   */
  public static final boolean isNumeric( int t ) {
    return t == TYPE_INTEGER || t == TYPE_NUMBER || t == TYPE_BIGNUMBER;
  }

  public boolean isSortedAscending() {
    return !isSortedDescending();
  }

  /**
   * Return the type of a value in a textual form: "String", "Number", "Integer", "Boolean", "Date", ...
   *
   * @return A String describing the type of value.
   */
  @Override
  public String getTypeDesc() {
    return getTypeDesc( type );
  }

  /**
   * Return the storage type of a value in a textual form: "normal", "binary-string", "indexes"
   *
   * @return A String describing the storage type of the value metadata
   */
  public String getStorageTypeDesc() {
    return storageTypeCodes[storageType];
  }

  @Override
  public String toString() {
    return name + " " + toStringMeta();
  }

  /**
   * a String text representation of this Value, optionally padded to the specified length
   *
   * @return a String text representation of this Value, optionally padded to the specified length
   */
  @Override
  public String toStringMeta() {
    // We (Sven Boden) did explicit performance testing for this
    // part. The original version used Strings instead of StringBuilders,
    // performance between the 2 does not differ that much. A few milliseconds
    // on 100000 iterations in the advantage of StringBuilders. The
    // lessened creation of objects may be worth it in the long run.
    StringBuilder retval = new StringBuilder( getTypeDesc() );

    switch ( getType() ) {
      case TYPE_STRING:
        if ( getLength() > 0 ) {
          retval.append( '(' ).append( getLength() ).append( ')' );
        }
        break;
      case TYPE_NUMBER:
      case TYPE_BIGNUMBER:
        if ( getLength() > 0 ) {
          retval.append( '(' ).append( getLength() );
          if ( getPrecision() > 0 ) {
            retval.append( ", " ).append( getPrecision() );
          }
          retval.append( ')' );
        }
        break;
      case TYPE_INTEGER:
        if ( getLength() > 0 ) {
          retval.append( '(' ).append( getLength() ).append( ')' );
        }
        break;
      default:
        break;
    }

    if ( !isStorageNormal() ) {
      retval.append( "<" ).append( getStorageTypeDesc() ).append( ">" );
    }

    return retval.toString();
  }

  @Override
  public void writeData( DataOutputStream outputStream, Object object ) throws KettleFileException {
    try {
      // Is the value NULL?
      outputStream.writeBoolean( object == null );

      if ( object != null ) {
        // otherwise there is no point
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            // Handle Content -- only when not NULL
            switch ( getType() ) {
              case TYPE_STRING:
                writeString( outputStream, (String) object );
                break;
              case TYPE_NUMBER:
                writeNumber( outputStream, (Double) object );
                break;
              case TYPE_INTEGER:
                writeInteger( outputStream, (Long) object );
                break;
              case TYPE_DATE:
                writeDate( outputStream, (Date) object );
                break;
              case TYPE_BIGNUMBER:
                writeBigNumber( outputStream, (BigDecimal) object );
                break;
              case TYPE_BOOLEAN:
                writeBoolean( outputStream, (Boolean) object );
                break;
              case TYPE_BINARY:
                writeBinary( outputStream, (byte[]) object );
                break;
              case TYPE_INET:
                writeBinary( outputStream, ( (InetAddress) object ).getAddress() );
                break;
              default:
                throw new KettleFileException( toString() + " : Unable to serialize data type " + getType() );
            }
            break;

          case STORAGE_TYPE_BINARY_STRING:
            // Handle binary string content -- only when not NULL
            // In this case, we opt not to convert anything at all for speed.
            // That way, we can save on CPU power.
            // Since the streams can be compressed, volume shouldn't be an issue
            // at all.
            //
            writeBinaryString( outputStream, (byte[]) object );
            break;

          case STORAGE_TYPE_INDEXED:
            writeInteger( outputStream, (Integer) object ); // just an index
            break;

          default:
            throw new KettleFileException( toString() + " : Unknown storage type " + getStorageType() );
        }
      }
    } catch ( ClassCastException e ) {
      throw new RuntimeException( toString() + " : There was a data type error: the data type of "
          + object.getClass().getName() + " object [" + object + "] does not correspond to value meta ["
          + toStringMeta() + "]" );
    } catch ( IOException e ) {
      throw new KettleFileException( toString() + " : Unable to write value data to output stream", e );
    }

  }

  @Override
  public Object readData( DataInputStream inputStream ) throws KettleFileException, KettleEOFException,
    SocketTimeoutException {
    try {
      // Is the value NULL?
      if ( inputStream.readBoolean() ) {
        return null; // done
      }

      switch ( storageType ) {
        case STORAGE_TYPE_NORMAL:
          // Handle Content -- only when not NULL
          switch ( getType() ) {
            case TYPE_STRING:
              return readString( inputStream );
            case TYPE_NUMBER:
              return readNumber( inputStream );
            case TYPE_INTEGER:
              return readInteger( inputStream );
            case TYPE_DATE:
              return readDate( inputStream );
            case TYPE_BIGNUMBER:
              return readBigNumber( inputStream );
            case TYPE_BOOLEAN:
              return readBoolean( inputStream );
            case TYPE_BINARY:
              return readBinary( inputStream );
            case TYPE_INET:
              return InetAddress.getByAddress( readBinary( inputStream ) );
            default:
              throw new KettleFileException( toString() + " : Unable to de-serialize data of type " + getType() );
          }

        case STORAGE_TYPE_BINARY_STRING:
          return readBinaryString( inputStream );

        case STORAGE_TYPE_INDEXED:
          return readSmallInteger( inputStream ); // just an index: 4-bytes should
          // be enough.

        default:
          throw new KettleFileException( toString() + " : Unknown storage type " + getStorageType() );
      }
    } catch ( EOFException e ) {
      throw new KettleEOFException( e );
    } catch ( SocketTimeoutException e ) {
      throw e;
    } catch ( IOException e ) {
      throw new KettleFileException( toString() + " : Unable to read value data from input stream", e );
    }
  }

  protected void writeString( DataOutputStream outputStream, String string ) throws IOException {
    // Write the length and then the bytes
    if ( string == null ) {
      outputStream.writeInt( -1 );
    } else {
      byte[] chars = string.getBytes( Const.XML_ENCODING );
      outputStream.writeInt( chars.length );
      outputStream.write( chars );
    }
  }

  protected void writeBinaryString( DataOutputStream outputStream, byte[] binaryString ) throws IOException {
    // Write the length and then the bytes
    if ( binaryString == null ) {
      outputStream.writeInt( -1 );
    } else {
      outputStream.writeInt( binaryString.length );
      outputStream.write( binaryString );
    }
  }

  protected String readString( DataInputStream inputStream ) throws IOException {
    // Read the length and then the bytes
    int length = inputStream.readInt();
    if ( length < 0 ) {
      return null;
    }

    byte[] chars = new byte[length];
    inputStream.readFully( chars );

    String string = new String( chars, Const.XML_ENCODING );
    // System.out.println("Read string("+getName()+"), length "+length+": "+string);
    return string;
  }

  protected byte[] readBinaryString( DataInputStream inputStream ) throws IOException {
    // Read the length and then the bytes
    int length = inputStream.readInt();
    if ( length < 0 ) {
      return null;
    }

    byte[] chars = new byte[length];
    inputStream.readFully( chars );

    return chars;
  }

  protected void writeBigNumber( DataOutputStream outputStream, BigDecimal number ) throws IOException {
    String string = number.toString();
    writeString( outputStream, string );
  }

  protected BigDecimal readBigNumber( DataInputStream inputStream ) throws IOException {
    String string = readString( inputStream );
    // System.out.println("Read big number("+getName()+") ["+string+"]");
    return new BigDecimal( string );
  }

  protected void writeDate( DataOutputStream outputStream, Date date ) throws IOException {
    outputStream.writeLong( date.getTime() );
  }

  protected Date readDate( DataInputStream inputStream ) throws IOException {
    long time = inputStream.readLong();
    // System.out.println("Read Date("+getName()+") ["+new Date(time)+"]");
    return new Date( time );
  }

  protected void writeBoolean( DataOutputStream outputStream, Boolean bool ) throws IOException {
    outputStream.writeBoolean( bool.booleanValue() );
  }

  protected Boolean readBoolean( DataInputStream inputStream ) throws IOException {
    Boolean bool = Boolean.valueOf( inputStream.readBoolean() );
    // System.out.println("Read boolean("+getName()+") ["+bool+"]");
    return bool;
  }

  protected void writeNumber( DataOutputStream outputStream, Double number ) throws IOException {
    outputStream.writeDouble( number.doubleValue() );
  }

  protected Double readNumber( DataInputStream inputStream ) throws IOException {
    Double d = new Double( inputStream.readDouble() );
    // System.out.println("Read number("+getName()+") ["+d+"]");
    return d;
  }

  protected void writeInteger( DataOutputStream outputStream, Long number ) throws IOException {
    outputStream.writeLong( number.longValue() );
  }

  protected Long readInteger( DataInputStream inputStream ) throws IOException {
    Long l = new Long( inputStream.readLong() );
    // System.out.println("Read integer("+getName()+") ["+l+"]");
    return l;
  }

  protected void writeInteger( DataOutputStream outputStream, Integer number ) throws IOException {
    outputStream.writeInt( number.intValue() );
  }

  protected Integer readSmallInteger( DataInputStream inputStream ) throws IOException {
    Integer i = Integer.valueOf( inputStream.readInt() );
    // System.out.println("Read index integer("+getName()+") ["+i+"]");
    return i;
  }

  protected void writeBinary( DataOutputStream outputStream, byte[] binary ) throws IOException {
    outputStream.writeInt( binary.length );
    outputStream.write( binary );
  }

  protected byte[] readBinary( DataInputStream inputStream ) throws IOException {
    int size = inputStream.readInt();
    byte[] buffer = new byte[size];
    inputStream.readFully( buffer );

    // System.out.println("Read binary("+getName()+") with size="+size);

    return buffer;
  }

  @Override
  public void writeMeta( DataOutputStream outputStream ) throws KettleFileException {
    try {
      int type = getType();

      // Handle type
      outputStream.writeInt( type );

      // Handle storage type
      outputStream.writeInt( storageType );

      switch ( storageType ) {
        case STORAGE_TYPE_INDEXED:
          // Save the indexed strings...
          if ( index == null ) {
            outputStream.writeInt( -1 ); // null
          } else {
            outputStream.writeInt( index.length );
            for ( int i = 0; i < index.length; i++ ) {
              try {
                switch ( type ) {
                  case TYPE_STRING:
                    writeString( outputStream, (String) index[i] );
                    break;
                  case TYPE_NUMBER:
                    writeNumber( outputStream, (Double) index[i] );
                    break;
                  case TYPE_INTEGER:
                    writeInteger( outputStream, (Long) index[i] );
                    break;
                  case TYPE_DATE:
                    writeDate( outputStream, (Date) index[i] );
                    break;
                  case TYPE_BIGNUMBER:
                    writeBigNumber( outputStream, (BigDecimal) index[i] );
                    break;
                  case TYPE_BOOLEAN:
                    writeBoolean( outputStream, (Boolean) index[i] );
                    break;
                  case TYPE_BINARY:
                    writeBinary( outputStream, (byte[]) index[i] );
                    break;
                  default:
                    throw new KettleFileException( toString()
                        + " : Unable to serialize indexe storage type for data type " + getType() );
                }
              } catch ( ClassCastException e ) {
                throw new RuntimeException( toString() + " : There was a data type error: the data type of "
                    + index[i].getClass().getName() + " object [" + index[i] + "] does not correspond to value meta ["
                    + toStringMeta() + "]" );
              }
            }
          }
          break;

        case STORAGE_TYPE_BINARY_STRING:
          // Save the storage meta data...
          //
          outputStream.writeBoolean( storageMetadata != null );

          if ( storageMetadata != null ) {
            storageMetadata.writeMeta( outputStream );
          }
          break;

        default:
          break;
      }

      // Handle name-length
      writeString( outputStream, name );

      // length & precision
      outputStream.writeInt( getLength() );
      outputStream.writeInt( getPrecision() );

      // Origin
      writeString( outputStream, origin );

      // Comments
      writeString( outputStream, comments );

      // formatting Mask, decimal, grouping, currency
      writeString( outputStream, conversionMask );
      writeString( outputStream, decimalSymbol );
      writeString( outputStream, groupingSymbol );
      writeString( outputStream, currencySymbol );
      outputStream.writeInt( trimType );

      // Case sensitivity of compare
      outputStream.writeBoolean( caseInsensitive );

      // Collator Locale
      writeString( outputStream, collatorLocale.toLanguageTag() );

      // Collator Disabled of compare
      outputStream.writeBoolean( collatorDisabled );

      // Collator strength of compare
      outputStream.writeInt( collatorStrength );

      // Sorting information
      outputStream.writeBoolean( sortedDescending );

      // Padding information
      outputStream.writeBoolean( outputPaddingEnabled );

      // date format lenient?
      outputStream.writeBoolean( dateFormatLenient );

      // date format locale?
      writeString( outputStream, dateFormatLocale != null ? dateFormatLocale.toString() : null );

      // date time zone?
      writeString( outputStream, dateFormatTimeZone != null ? dateFormatTimeZone.getID() : null );

      // string to number conversion lenient?
      outputStream.writeBoolean( lenientStringToNumber );
    } catch ( IOException e ) {
      throw new KettleFileException( toString() + " : Unable to write value metadata to output stream", e );
    }
  }

  /**
   * Load the attributes of this particular value meta object from the input stream. Loading the type is not handled
   * here, this should be read from the stream previously!
   *
   * @param inputStream
   *          the input stream to read from
   * @throws KettleFileException
   *           In case there was a IO problem
   * @throws KettleEOFException
   *           If we reached the end of the stream
   */
  @Override
  public void readMetaData( DataInputStream inputStream ) throws KettleFileException, KettleEOFException {

    // Loading the type is not handled here, this should be read from the stream previously!
    //
    try {

      // Handle storage type
      storageType = inputStream.readInt();

      // Read the data in the index
      switch ( storageType ) {
        case STORAGE_TYPE_INDEXED:
          int indexSize = inputStream.readInt();
          if ( indexSize < 0 ) {
            index = null;
          } else {
            index = new Object[indexSize];
            for ( int i = 0; i < indexSize; i++ ) {
              switch ( type ) {
                case TYPE_STRING:
                  index[i] = readString( inputStream );
                  break;
                case TYPE_NUMBER:
                  index[i] = readNumber( inputStream );
                  break;
                case TYPE_INTEGER:
                  index[i] = readInteger( inputStream );
                  break;
                case TYPE_DATE:
                  index[i] = readDate( inputStream );
                  break;
                case TYPE_BIGNUMBER:
                  index[i] = readBigNumber( inputStream );
                  break;
                case TYPE_BOOLEAN:
                  index[i] = readBoolean( inputStream );
                  break;
                case TYPE_BINARY:
                  index[i] = readBinary( inputStream );
                  break;
                default:
                  throw new KettleFileException( toString()
                      + " : Unable to de-serialize indexed storage type for data type " + getType() );
              }
            }
          }
          break;

        case STORAGE_TYPE_BINARY_STRING:
          // In case we do have storage metadata defined, we read that back in as
          // well..
          if ( inputStream.readBoolean() ) {
            storageMetadata = new ValueMetaBase( inputStream );
          }
          break;

        default:
          break;
      }

      // name
      name = readString( inputStream );

      // length & precision
      length = inputStream.readInt();
      precision = inputStream.readInt();

      // Origin
      origin = readString( inputStream );

      // Comments
      comments = readString( inputStream );

      // formatting Mask, decimal, grouping, currency

      conversionMask = readString( inputStream );
      decimalSymbol = readString( inputStream );
      groupingSymbol = readString( inputStream );
      currencySymbol = readString( inputStream );
      trimType = inputStream.readInt();

      // Case sensitivity
      caseInsensitive = inputStream.readBoolean();

      // Collator locale
      setCollatorLocale( Locale.forLanguageTag( readString( inputStream ) ) );

      // Collator disabled
      collatorDisabled = inputStream.readBoolean();

      // Collator strength
      collatorStrength = inputStream.readInt();

      // Sorting type
      sortedDescending = inputStream.readBoolean();

      // Output padding?
      outputPaddingEnabled = inputStream.readBoolean();

      // is date parsing lenient?
      //
      dateFormatLenient = inputStream.readBoolean();

      // What is the date format locale?
      //
      String strDateFormatLocale = readString( inputStream );
      if ( Utils.isEmpty( strDateFormatLocale ) ) {
        dateFormatLocale = null;
      } else {
        dateFormatLocale = EnvUtil.createLocale( strDateFormatLocale );
      }

      // What is the time zone to use for date parsing?
      //
      String strTimeZone = readString( inputStream );
      if ( Utils.isEmpty( strTimeZone ) ) {
        dateFormatTimeZone = TimeZone.getDefault();
      } else {
        dateFormatTimeZone = EnvUtil.createTimeZone( strTimeZone );
      }

      // is string to number parsing lenient?
      lenientStringToNumber = inputStream.readBoolean();
    } catch ( EOFException e ) {
      throw new KettleEOFException( e );
    } catch ( IOException e ) {
      throw new KettleFileException( toString() + " : Unable to read value metadata from input stream", e );
    }

  }

  @Override
  public String getMetaXML() throws IOException {
    StringBuilder xml = new StringBuilder();

    xml.append( XMLHandler.openTag( XML_META_TAG ) );

    xml.append( XMLHandler.addTagValue( "type", getTypeDesc() ) );
    xml.append( XMLHandler.addTagValue( "storagetype", getStorageTypeCode( getStorageType() ) ) );

    switch ( storageType ) {
      case STORAGE_TYPE_INDEXED:
        xml.append( XMLHandler.openTag( "index" ) );

        // Save the indexed strings...
        //
        if ( index != null ) {
          for ( int i = 0; i < index.length; i++ ) {
            try {
              switch ( type ) {
                case TYPE_STRING:
                  xml.append( XMLHandler.addTagValue( "value", (String) index[i] ) );
                  break;
                case TYPE_NUMBER:
                  xml.append( XMLHandler.addTagValue( "value", (Double) index[i] ) );
                  break;
                case TYPE_INTEGER:
                  xml.append( XMLHandler.addTagValue( "value", (Long) index[i] ) );
                  break;
                case TYPE_DATE:
                  xml.append( XMLHandler.addTagValue( "value", (Date) index[i] ) );
                  break;
                case TYPE_BIGNUMBER:
                  xml.append( XMLHandler.addTagValue( "value", (BigDecimal) index[i] ) );
                  break;
                case TYPE_BOOLEAN:
                  xml.append( XMLHandler.addTagValue( "value", (Boolean) index[i] ) );
                  break;
                case TYPE_BINARY:
                  xml.append( XMLHandler.addTagValue( "value", (byte[]) index[i] ) );
                  break;
                default:
                  throw new IOException( toString() + " : Unable to serialize index storage type to XML for data type "
                      + getType() );
              }
            } catch ( ClassCastException e ) {
              throw new RuntimeException( toString() + " : There was a data type error: the data type of "
                  + index[i].getClass().getName() + " object [" + index[i] + "] does not correspond to value meta ["
                  + toStringMeta() + "]" );
            }
          }
        }
        xml.append( XMLHandler.closeTag( "index" ) );
        break;

      case STORAGE_TYPE_BINARY_STRING:
        // Save the storage meta data...
        //
        if ( storageMetadata != null ) {
          xml.append( XMLHandler.openTag( "storage-meta" ) );
          xml.append( storageMetadata.getMetaXML() );
          xml.append( XMLHandler.closeTag( "storage-meta" ) );
        }
        break;

      default:
        break;
    }

    xml.append( XMLHandler.addTagValue( "name", name ) );
    xml.append( XMLHandler.addTagValue( "length", length ) );
    xml.append( XMLHandler.addTagValue( "precision", precision ) );
    xml.append( XMLHandler.addTagValue( "origin", origin ) );
    xml.append( XMLHandler.addTagValue( "comments", comments ) );
    xml.append( XMLHandler.addTagValue( "conversion_Mask", conversionMask ) );
    xml.append( XMLHandler.addTagValue( "decimal_symbol", decimalSymbol ) );
    xml.append( XMLHandler.addTagValue( "grouping_symbol", groupingSymbol ) );
    xml.append( XMLHandler.addTagValue( "currency_symbol", currencySymbol ) );
    xml.append( XMLHandler.addTagValue( "trim_type", getTrimTypeCode( trimType ) ) );
    xml.append( XMLHandler.addTagValue( "case_insensitive", caseInsensitive ) );
    xml.append( XMLHandler.addTagValue( "collator_disabled", collatorDisabled ) );
    xml.append( XMLHandler.addTagValue( "collator_strength", collatorStrength ) );
    xml.append( XMLHandler.addTagValue( "sort_descending", sortedDescending ) );
    xml.append( XMLHandler.addTagValue( "output_padding", outputPaddingEnabled ) );
    xml.append( XMLHandler.addTagValue( "date_format_lenient", dateFormatLenient ) );
    xml.append( XMLHandler.addTagValue( "date_format_locale", dateFormatLocale != null ? dateFormatLocale.toString()
        : null ) );
    xml.append( XMLHandler.addTagValue( "date_format_timezone", dateFormatTimeZone != null ? dateFormatTimeZone.getID()
        : null ) );
    xml.append( XMLHandler.addTagValue( "lenient_string_to_number", lenientStringToNumber ) );

    xml.append( XMLHandler.closeTag( XML_META_TAG ) );

    return xml.toString();
  }

  @Override
  public String getDataXML( Object object ) throws IOException {
    StringBuilder xml = new StringBuilder();

    String string;

    if ( object != null ) {
      try {
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            // Handle Content -- only when not NULL
            //
            switch ( getType() ) {
              case TYPE_STRING:
                string = (String) object;
                break;
              case TYPE_NUMBER:
                string = Double.toString( (Double) object );
                break;
              case TYPE_INTEGER:
                string = Long.toString( (Long) object );
                break;
              case TYPE_DATE:
                string = XMLHandler.date2string( (Date) object );
                break;
              case TYPE_BIGNUMBER:
                string = ( (BigDecimal) object ).toString();
                break;
              case TYPE_BOOLEAN:
                string = Boolean.toString( (Boolean) object );
                break;
              case TYPE_BINARY:
                string = XMLHandler.encodeBinaryData( (byte[]) object );
                break;
              case TYPE_TIMESTAMP:
                string = XMLHandler.timestamp2string( (Timestamp)  object );
                break;
              case TYPE_INET:
                string = ( (InetAddress) object ).toString();
                break;
              default:
                throw new IOException( toString() + " : Unable to serialize data type to XML " + getType() );
            }

            break;

          case STORAGE_TYPE_BINARY_STRING:
            // Handle binary string content -- only when not NULL
            // In this case, we opt not to convert anything at all for speed.
            // That way, we can save on CPU power.
            // Since the streams can be compressed, volume shouldn't be an issue
            // at all.
            //
            string = XMLHandler.addTagValue( "binary-string", (byte[]) object );
            xml.append( XMLHandler.openTag( XML_DATA_TAG ) ).append( string ).append( XMLHandler.closeTag( XML_DATA_TAG ) );
            return xml.toString();

          case STORAGE_TYPE_INDEXED:
            // Just an index
            string = XMLHandler.addTagValue( "index-value", (Integer) object );
            break;

          default:
            throw new IOException( toString() + " : Unknown storage type " + getStorageType() );
        }
      } catch ( ClassCastException e ) {
        throw new RuntimeException( toString() + " : There was a data type error: the data type of "
            + object.getClass().getName() + " object [" + object + "] does not correspond to value meta ["
            + toStringMeta() + "]", e );
      } catch ( Exception e ) {
        throw new RuntimeException( toString() + " : there was a value XML encoding error", e );
      }
    } else {
      // If the object is null: give an empty string
      //
      string = "";
    }
    xml.append( XMLHandler.addTagValue( XML_DATA_TAG, string ) );

    return xml.toString();
  }

  /**
   * Convert a data XML node to an Object that corresponds to the metadata. This is basically String to Object
   * conversion that is being done.
   *
   * @param node
   *          the node to retrieve the data value from
   * @return the converted data value
   * @throws IOException
   *           thrown in case there is a problem with the XML to object conversion
   */
  @Override
  public Object getValue( Node node ) throws KettleException {

    switch ( storageType ) {
      case STORAGE_TYPE_NORMAL:
        String valueString = XMLHandler.getNodeValue( node );
        if ( Utils.isEmpty( valueString ) ) {
          return null;
        }

        // Handle Content -- only when not NULL
        //
        switch ( getType() ) {
          case TYPE_STRING:
            return valueString;
          case TYPE_NUMBER:
            return Double.parseDouble( valueString );
          case TYPE_INTEGER:
            return Long.parseLong( valueString );
          case TYPE_DATE:
            return XMLHandler.stringToDate( valueString );
          case TYPE_TIMESTAMP:
            return XMLHandler.stringToTimestamp( valueString );
          case TYPE_BIGNUMBER:
            return new BigDecimal( valueString );
          case TYPE_BOOLEAN:
            return "Y".equalsIgnoreCase( valueString );
          case TYPE_BINARY:
            return XMLHandler.stringToBinary( XMLHandler.getTagValue( node, "binary-value" ) );
          default:
            throw new KettleException( toString() + " : Unable to de-serialize '" + valueString
                + "' from XML for data type " + getType() );
        }

      case STORAGE_TYPE_BINARY_STRING:
        // Handle binary string content -- only when not NULL
        // In this case, we opt not to convert anything at all for speed.
        // That way, we can save on CPU power.
        // Since the streams can be compressed, volume shouldn't be an issue at
        // all.
        //
        String binaryString = XMLHandler.getTagValue( node, "binary-string" );
        if ( Utils.isEmpty( binaryString ) ) {
          return null;
        }

        return XMLHandler.stringToBinary( binaryString );

      case STORAGE_TYPE_INDEXED:
        String indexString = XMLHandler.getTagValue( node, "index-value" );
        if ( Utils.isEmpty( indexString ) ) {
          return null;
        }

        return Integer.parseInt( indexString );

      default:
        throw new KettleException( toString() + " : Unknown storage type " + getStorageType() );
    }

  }

  /**
   * get an array of String describing the possible types a Value can have.
   *
   * @return an array of String describing the possible types a Value can have.
   */
  public static final String[] getTypes() {

    return ValueMetaFactory.getValueMetaNames();

    /*
     * String retval[] = new String[typeCodes.length - 1]; System.arraycopy(typeCodes, 1, retval, 0, typeCodes.length -
     * 1); return retval;
     */
  }

  /**
   * Get an array of String describing the possible types a Value can have.
   *
   * @return an array of String describing the possible types a Value can have.
   */
  public static final String[] getAllTypes() {

    return ValueMetaFactory.getAllValueMetaNames();

    /*
     * String retval[] = new String[typeCodes.length]; System.arraycopy(typeCodes, 0, retval, 0, typeCodes.length);
     * return retval;
     */
  }

  /**
   * TODO: change Desc to Code all over the place. Make sure we can localise this stuff later on.
   *
   * @param type
   *          the type
   * @return the description (code) of the type
   */
  public static final String getTypeDesc( int type ) {

    return ValueMetaFactory.getValueMetaName( type );

    // return typeCodes[type];
  }

  /**
   * Convert the String description of a type to an integer type.
   *
   * @param desc
   *          The description of the type to convert
   * @return The integer type of the given String. (ValueMetaInterface.TYPE_...)
   */
  public static final int getType( String desc ) {

    return ValueMetaFactory.getIdForValueMeta( desc );

    /*
     * for (int i = 1; i < typeCodes.length; i++) { if (typeCodes[i].equalsIgnoreCase(desc)) { return i; } }
     *
     * return TYPE_NONE;
     */
  }

  /**
   * Convert the String description of a storage type to an integer type.
   *
   * @param desc
   *          The description of the storage type to convert
   * @return The integer storage type of the given String. (ValueMetaInterface.STORAGE_TYPE_...) or -1 if the storage
   *         type code not be found.
   */
  public static final int getStorageType( String desc ) {
    for ( int i = 0; i < storageTypeCodes.length; i++ ) {
      if ( storageTypeCodes[i].equalsIgnoreCase( desc ) ) {
        return i;
      }
    }

    return -1;
  }

  public static final String getStorageTypeCode( int storageType ) {
    if ( storageType >= STORAGE_TYPE_NORMAL && storageType <= STORAGE_TYPE_INDEXED ) {
      return storageTypeCodes[storageType];
    }
    return null;
  }

  /**
   * Determine if an object is null. This is the case if data==null or if it's an empty string.
   *
   * @param data
   *          the object to test
   * @return true if the object is considered null.
   * @throws KettleValueException
   *           in case there is a conversion error (only thrown in case of lazy conversion)
   */
  @Override
  public boolean isNull( Object data ) throws KettleValueException {
    //noinspection deprecation
    return isNull( data, EMPTY_STRING_AND_NULL_ARE_DIFFERENT );
  }

  /*
   * Do not use this method directly! It is for tests!
   */
  @Deprecated
  boolean isNull( Object data, boolean emptyStringDiffersFromNull ) throws KettleValueException {
    try {
      Object value = data;

      if ( isStorageBinaryString() ) {
        if ( value == null || !emptyStringDiffersFromNull && ( (byte[]) value ).length == 0 ) {
          return true; // shortcut
        }
        value = convertBinaryStringToNativeType( (byte[]) data );
      }

      // Re-check for null, even for lazy conversion.
      // A value (5 spaces for example) can be null after trim and conversion
      //
      if ( value == null ) {
        return true;
      }

      if ( emptyStringDiffersFromNull ) {
        return false;
      }

      // If it's a string and the string is empty, it's a null value as well
      //
      if ( isString() ) {
        if ( value.toString().length() == 0 ) {
          return true;
        }
      }

      // We tried everything else so we assume this value is not null.
      //
      return false;
    } catch ( ClassCastException e ) {
      throw new RuntimeException( "Unable to verify if [" + toString() + "] is null or not because of an error:"
          + e.toString(), e );
    }
  }

  /*
   * Compare 2 binary strings, one byte at a time.<br> This algorithm is very fast but most likely wrong as well.<br>
   *
   * @param one The first binary string to compare with
   *
   * @param two the second binary string to compare to
   *
   * @return -1 if <i>one</i> is smaller than <i>two</i>, 0 is both byte arrays are identical and 1 if <i>one</i> is
   * larger than <i>two</i> protected int compareBinaryStrings(byte[] one, byte[] two) {
   *
   * for (int i=0;i<one.length;i++) { if (i>=two.length) return 1; // larger if (one[i]>two[i]) return 1; // larger if
   * (one[i]<two[i]) return -1; // smaller } if (one.length>two.length) return 1; // larger if (one.length>two.length)
   * return -11; // smaller return 0; }
   */

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
  @Override
  public int compare( Object data1, Object data2 ) throws KettleValueException {
    boolean n1 = isNull( data1 );
    boolean n2 = isNull( data2 );

    if ( n1 && !n2 ) {
      if ( isSortedDescending() ) {
        // BACKLOG-14028
        return 1;
      } else {
        return -1;
      }
    }
    if ( !n1 && n2 ) {
      if ( isSortedDescending() ) {
        return -1;
      } else {
        return 1;
      }
    }
    if ( n1 && n2 ) {
      return 0;
    }

    int cmp = 0;
    switch ( getType() ) {
      case TYPE_STRING:
        // if (isStorageBinaryString() && identicalFormat &&
        // storageMetadata.isSingleByteEncoding()) return
        // compareBinaryStrings((byte[])data1, (byte[])data2); TODO
        String one = getString( data1 );
        String two = getString( data2 );

        if ( collatorDisabled ) {
          if ( caseInsensitive ) {
            cmp = one.compareToIgnoreCase( two );
          } else {
            cmp = one.compareTo( two );
          }
        } else {
          cmp = collator.compare( one, two );
        }
        break;

      case TYPE_INTEGER:
        // if (isStorageBinaryString() && identicalFormat) return
        // compareBinaryStrings((byte[])data1, (byte[])data2); TODO
        cmp = getInteger( data1 ).compareTo( getInteger( data2 ) );
        break;

      case TYPE_NUMBER:
        cmp = Double.compare( getNumber( data1 ).doubleValue(), getNumber( data2 ).doubleValue() );
        break;

      case TYPE_DATE:
        cmp = Long.valueOf( getDate( data1 ).getTime() ).compareTo( Long.valueOf( getDate( data2 ).getTime() ) );
        break;

      case TYPE_BIGNUMBER:
        cmp = getBigNumber( data1 ).compareTo( getBigNumber( data2 ) );
        break;

      case TYPE_BOOLEAN:
        if ( getBoolean( data1 ).booleanValue() == getBoolean( data2 ).booleanValue() ) {
          cmp = 0; // true == true, false == false
        } else if ( getBoolean( data1 ).booleanValue() && !getBoolean( data2 ).booleanValue() ) {
          cmp = 1; // true > false
        } else {
          cmp = -1; // false < true
        }
        break;

      case TYPE_BINARY:
        byte[] b1 = (byte[]) data1;
        byte[] b2 = (byte[]) data2;

        int length = b1.length < b2.length ? b1.length : b2.length;

        cmp = b1.length - b2.length;
        if ( cmp == 0 ) {
          for ( int i = 0; i < length; i++ ) {
            cmp = b1[ i ] - b2[ i ];
            if ( cmp != 0 ) {
              cmp = cmp < 0 ? -1 : 1;
              break;
            }
          }
        }

        break;
      default:
        throw new KettleValueException( toString() + " : Comparing values can not be done with data type : "
            + getType() );
    }

    if ( isSortedDescending() ) {
      return -cmp;
    } else {
      return cmp;
    }
  }

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
  @Override
  public int compare( Object data1, ValueMetaInterface meta2, Object data2 ) throws KettleValueException {
    if ( meta2 == null ) {
      throw new KettleValueException( toStringMeta()
          + " : Second meta data (meta2) is null, please check one of the previous steps." );
    }

    try {
      // Before we can compare data1 to data2 we need to make sure they have the
      // same data type etc.
      //
      if ( getType() == meta2.getType() ) {
        if ( getStorageType() == meta2.getStorageType() ) {
          return compare( data1, data2 );
        }

        // Convert the storage type to compare the data.
        //
        switch ( getStorageType() ) {
          case STORAGE_TYPE_NORMAL:
            return compare( data1, meta2.convertToNormalStorageType( data2 ) );
          case STORAGE_TYPE_BINARY_STRING:
            if ( storageMetadata != null && storageMetadata.getConversionMask() != null ) {
              // BACKLOG-18754 - if there is a storage conversion mask, we should use
              // it as the mask for meta2 (meta2 can have specific storage type and type, so
              // it can't be used directly to convert data2 to binary string)
              ValueMetaInterface meta2StorageMask = meta2.clone();
              meta2StorageMask.setConversionMask( storageMetadata.getConversionMask() );
              return compare( data1, meta2StorageMask.convertToBinaryStringStorageType( data2 ) );
            } else {
              return compare( data1, meta2.convertToBinaryStringStorageType( data2 ) );
            }
          case STORAGE_TYPE_INDEXED:
            switch ( meta2.getStorageType() ) {
              case STORAGE_TYPE_INDEXED:
                return compare( data1, data2 ); // not accessible, just to make sure.
              case STORAGE_TYPE_NORMAL:
                return -meta2.compare( data2, convertToNormalStorageType( data1 ) );
              case STORAGE_TYPE_BINARY_STRING:
                return -meta2.compare( data2, convertToBinaryStringStorageType( data1 ) );
              default:
                throw new KettleValueException( meta2.toStringMeta() + " : Unknown storage type : "
                    + meta2.getStorageType() );

            }
          default:
            throw new KettleValueException( toStringMeta() + " : Unknown storage type : " + getStorageType() );
        }
      } else if ( ValueMetaInterface.TYPE_INTEGER == getType() && ValueMetaInterface.TYPE_NUMBER == meta2.getType() ) {
        // BACKLOG-18738
        // compare Double to Integer
        return -meta2.compare( data2, meta2.convertData( this, data1 ) );
      }

      // If the data types are not the same, the first one is the driver...
      // The second data type is converted to the first one.
      //
      return compare( data1, convertData( meta2, data2 ) );
    } catch ( Exception e ) {
      throw new KettleValueException(
          toStringMeta() + " : Unable to compare with value [" + meta2.toStringMeta() + "]", e );
    }
  }

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
  @Override
  public Object convertData( ValueMetaInterface meta2, Object data2 ) throws KettleValueException {
    switch ( getType() ) {
      case TYPE_STRING:
        return meta2.getString( data2 );
      case TYPE_NUMBER:
        return meta2.getNumber( data2 );
      case TYPE_INTEGER:
        return meta2.getInteger( data2 );
      case TYPE_DATE:
        return meta2.getDate( data2 );
      case TYPE_BIGNUMBER:
        return meta2.getBigNumber( data2 );
      case TYPE_BOOLEAN:
        return meta2.getBoolean( data2 );
      case TYPE_BINARY:
        return meta2.getBinary( data2 );
      default:
        throw new KettleValueException( toString() + " : I can't convert the specified value to data type : "
            + getType() );
    }
  }

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
  @Override
  public Object convertDataCompatible( ValueMetaInterface meta2, Object data2 ) throws KettleValueException {
    switch ( getType() ) {
      case TYPE_STRING:
        return meta2.getCompatibleString( data2 );
      case TYPE_NUMBER:
        return meta2.getNumber( data2 );
      case TYPE_INTEGER:
        return meta2.getInteger( data2 );
      case TYPE_DATE:
        return meta2.getDate( data2 );
      case TYPE_BIGNUMBER:
        return meta2.getBigNumber( data2 );
      case TYPE_BOOLEAN:
        return meta2.getBoolean( data2 );
      case TYPE_BINARY:
        return meta2.getBinary( data2 );
      default:
        throw new KettleValueException( toString() + " : I can't convert the specified value to data type : "
            + getType() );
    }
  }

  /**
   * Convert an object to the data type specified in the conversion metadata
   *
   * @param data
   *          The data
   * @return The data converted to the storage data type
   * @throws KettleValueException
   *           in case there is a conversion error.
   */
  @Override
  public Object convertDataUsingConversionMetaData( Object data ) throws KettleValueException {
    if ( conversionMetadata == null ) {
      throw new KettleValueException(
          "API coding error: please specify the conversion metadata before attempting to convert value " + name );
    }

    // Suppose we have an Integer 123, length 5
    // The string variation of this is " 00123"
    // To convert this back to an Integer we use the storage metadata
    // Specifically, in method convertStringToInteger() we consult the
    // storageMetaData to get the correct conversion mask
    // That way we're always sure that a conversion works both ways.
    //

    switch ( conversionMetadata.getType() ) {
      case TYPE_STRING:
        return getString( data );
      case TYPE_INTEGER:
        return getInteger( data );
      case TYPE_NUMBER:
        return getNumber( data );
      case TYPE_DATE:
        return getDate( data );
      case TYPE_BIGNUMBER:
        return getBigNumber( data );
      case TYPE_BOOLEAN:
        return getBoolean( data );
      case TYPE_BINARY:
        return getBinary( data );
      default:
        throw new KettleValueException( toString() + " : I can't convert the specified value to data type : "
            + conversionMetadata.getType() );
    }
  }

  /**
   * Convert the specified string to the data type specified in this object.
   *
   * @param pol
   *          the string to be converted
   * @param convertMeta
   *          the metadata of the object (only string type) to be converted
   * @param nullIf
   *          set the object to null if pos equals nullif (IgnoreCase)
   * @param ifNull
   *          set the object to ifNull when pol is empty or null
   * @param trim_type
   *          the trim type to be used (ValueMetaInterface.TRIM_TYPE_XXX)
   * @return the object in the data type of this value metadata object
   * @throws KettleValueException
   *           in case there is a data conversion error
   */
  @Override
  public Object convertDataFromString( String pol, ValueMetaInterface convertMeta, String nullIf, String ifNull,
      int trim_type ) throws KettleValueException {
    if ( convertMeta == null ) {
      throw new KettleValueException( "API coding error: convertMeta input parameter should not be equals to null" );
    }
    // null handling and conversion of value to null
    //
    String null_value = nullIf;
    int inValueType = convertMeta.getType();
    int outValueType = getType();

    if ( null_value == null ) {
      switch ( inValueType ) {
        case ValueMetaInterface.TYPE_BOOLEAN:
          null_value = Const.NULL_BOOLEAN;
          break;
        case ValueMetaInterface.TYPE_STRING:
          null_value = Const.NULL_STRING;
          break;
        case ValueMetaInterface.TYPE_BIGNUMBER:
          null_value = Const.NULL_BIGNUMBER;
          break;
        case ValueMetaInterface.TYPE_NUMBER:
          null_value = Const.NULL_NUMBER;
          break;
        case ValueMetaInterface.TYPE_INTEGER:
          null_value = Const.NULL_INTEGER;
          break;
        case ValueMetaInterface.TYPE_DATE:
          null_value = Const.NULL_DATE;
          break;
        case ValueMetaInterface.TYPE_BINARY:
          null_value = Const.NULL_BINARY;
          break;
        default:
          null_value = Const.NULL_NONE;
          break;
      }
    }

    // See if we need to convert a null value into a String
    // For example, we might want to convert null into "Empty".
    //
    if ( !Utils.isEmpty( ifNull ) ) {
      // Note that you can't pull the pad method up here as a nullComp variable
      // because you could get an NPE since you haven't checked isEmpty(pol)
      // yet!
      if ( Utils.isEmpty( pol )
          || pol.equalsIgnoreCase( Const.rightPad( new StringBuilder( null_value ), pol.length() ) ) ) {
        pol = ifNull;
      }
    }

    // See if the polled value is empty
    // In that case, we have a null value on our hands...
    boolean isStringValue = outValueType == Value.VALUE_TYPE_STRING;
    Object emptyValue = isStringValue ? Const.NULL_STRING : null;

    Boolean isEmptyAndNullDiffer = convertStringToBoolean(
        Const.NVL( System.getProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "N" ), "N" ) );

    if ( pol == null && isStringValue && isEmptyAndNullDiffer ) {
      pol = Const.NULL_STRING;
    }

    if ( pol == null ) {
      return null;
    } else if ( Utils.isEmpty( pol ) && !isStringValue ) {
      return null;
    } else {
      // if the null_value is specified, we try to match with that.
      //
      if ( !Utils.isEmpty( null_value ) ) {
        if ( null_value.length() <= pol.length() ) {
          // If the polled value is equal to the spaces right-padded null_value,
          // we have a match
          //
          if ( pol.equalsIgnoreCase( Const.rightPad( new StringBuilder( null_value ), pol.length() ) ) ) {
            return emptyValue;
          }
        }
      } else {
        // Verify if there are only spaces in the polled value...
        // We consider that empty as well...
        //
        if ( Const.onlySpaces( pol ) ) {
          return emptyValue;
        }
      }
    }

    // Trimming
    StringBuilder strpol;
    switch ( trim_type ) {
      case ValueMetaInterface.TRIM_TYPE_LEFT:
        strpol = new StringBuilder( pol );
        while ( strpol.length() > 0 && strpol.charAt( 0 ) == ' ' ) {
          strpol.deleteCharAt( 0 );
        }
        pol = strpol.toString();

        break;
      case ValueMetaInterface.TRIM_TYPE_RIGHT:
        strpol = new StringBuilder( pol );
        while ( strpol.length() > 0 && strpol.charAt( strpol.length() - 1 ) == ' ' ) {
          strpol.deleteCharAt( strpol.length() - 1 );
        }
        pol = strpol.toString();

        break;
      case ValueMetaInterface.TRIM_TYPE_BOTH:
        strpol = new StringBuilder( pol );
        while ( strpol.length() > 0 && strpol.charAt( 0 ) == ' ' ) {
          strpol.deleteCharAt( 0 );
        }
        while ( strpol.length() > 0 && strpol.charAt( strpol.length() - 1 ) == ' ' ) {
          strpol.deleteCharAt( strpol.length() - 1 );
        }
        pol = strpol.toString();
        break;
      default:
        break;
    }

    // On with the regular program...
    // Simply call the ValueMeta routines to do the conversion
    // We need to do some effort here: copy all
    //
    return convertData( convertMeta, pol );
  }

  /**
   * Calculate the hashcode of the specified data object
   *
   * @param object
   *          the data value to calculate a hashcode for
   * @return the calculated hashcode
   * @throws KettleValueException
   */
  @Override
  public int hashCode( Object object ) throws KettleValueException {
    int hash = 0;

    if ( isNull( object ) ) {
      switch ( getType() ) {
        case TYPE_BOOLEAN:
          hash ^= 1;
          break;
        case TYPE_DATE:
          hash ^= 2;
          break;
        case TYPE_NUMBER:
          hash ^= 4;
          break;
        case TYPE_STRING:
          hash ^= 8;
          break;
        case TYPE_INTEGER:
          hash ^= 16;
          break;
        case TYPE_BIGNUMBER:
          hash ^= 32;
          break;
        case TYPE_NONE:
          break;
        default:
          break;
      }
    } else {
      switch ( getType() ) {
        case TYPE_BOOLEAN:
          hash ^= getBoolean( object ).hashCode();
          break;
        case TYPE_DATE:
          hash ^= getDate( object ).hashCode();
          break;
        case TYPE_INTEGER:
          hash ^= getInteger( object ).hashCode();
          break;
        case TYPE_NUMBER:
          hash ^= getNumber( object ).hashCode();
          break;
        case TYPE_STRING:
          hash ^= getString( object ).hashCode();
          break;
        case TYPE_BIGNUMBER:
          hash ^= getBigNumber( object ).hashCode();
          break;
        case TYPE_NONE:
          break;
        default:
          break;
      }
    }

    return hash;
  }

  /**
   * Create an old-style value for backward compatibility reasons
   *
   * @param data
   *          the data to store in the value
   * @return a newly created Value object
   * @throws KettleValueException
   *           case there is a data conversion problem
   */
  @Override
  public Value createOriginalValue( Object data ) throws KettleValueException {
    Value value = new Value( name, type );
    value.setLength( length, precision );

    if ( isNull( data ) ) {
      value.setNull();
    } else {
      switch ( value.getType() ) {
        case TYPE_STRING:
          value.setValue( getString( data ) );
          break;
        case TYPE_NUMBER:
          value.setValue( getNumber( data ).doubleValue() );
          break;
        case TYPE_INTEGER:
          value.setValue( getInteger( data ).longValue() );
          break;
        case TYPE_DATE:
          value.setValue( getDate( data ) );
          break;
        case TYPE_BOOLEAN:
          value.setValue( getBoolean( data ).booleanValue() );
          break;
        case TYPE_BIGNUMBER:
          value.setValue( getBigNumber( data ) );
          break;
        case TYPE_BINARY:
          value.setValue( getBinary( data ) );
          break;
        default:
          throw new KettleValueException( toString() + " : We can't convert data type " + getTypeDesc()
              + " to an original (V2) Value" );
      }
    }
    return value;
  }

  /**
   * Extracts the primitive data from an old style Value object
   *
   * @param value
   *          the old style Value object
   * @return the value's data, NOT the meta data.
   * @throws KettleValueException
   *           case there is a data conversion problem
   */
  @Override
  public Object getValueData( Value value ) throws KettleValueException {
    if ( value == null || value.isNull() ) {
      return null;
    }

    // So far the old types and the new types map to the same thing.
    // For compatibility we just ask the old-style value to convert to the new
    // one.
    // In the old transformation this would happen sooner or later anyway.
    // It doesn't throw exceptions or complain either (unfortunately).
    //

    switch ( getType() ) {
      case ValueMetaInterface.TYPE_STRING:
        return value.getString();
      case ValueMetaInterface.TYPE_NUMBER:
        return value.getNumber();
      case ValueMetaInterface.TYPE_INTEGER:
        return value.getInteger();
      case ValueMetaInterface.TYPE_DATE:
        return value.getDate();
      case ValueMetaInterface.TYPE_BOOLEAN:
        return value.getBoolean();
      case ValueMetaInterface.TYPE_BIGNUMBER:
        return value.getBigNumber();
      case ValueMetaInterface.TYPE_BINARY:
        return value.getBytes();
      default:
        throw new KettleValueException( toString() + " : We can't convert original data type " + value.getTypeDesc()
            + " to a primitive data type" );
    }
  }

  /**
   * @return the storageMetadata
   */
  @Override
  public ValueMetaInterface getStorageMetadata() {
    return storageMetadata;
  }

  /**
   * @param storageMetadata
   *          the storageMetadata to set
   */
  @Override
  public void setStorageMetadata( ValueMetaInterface storageMetadata ) {
    this.storageMetadata = storageMetadata;
    compareStorageAndActualFormat();
  }

  protected void compareStorageAndActualFormat() {

    if ( storageMetadata == null ) {
      identicalFormat = true;
    } else {

      // If a trim type is set, we need to at least try to trim the strings.
      // In that case, we have to set the identical format off.
      //
      if ( trimType != TRIM_TYPE_NONE ) {
        identicalFormat = false;
      } else {

        // If there is a string encoding set and it's the same encoding in the
        // binary string, then we don't have to convert
        // If there are no encodings set, then we're certain we don't have to
        // convert as well.
        //
        if ( getStringEncoding() != null && getStringEncoding().equals( storageMetadata.getStringEncoding() )
            || getStringEncoding() == null && storageMetadata.getStringEncoding() == null ) {

          // However, perhaps the conversion mask changed since we read the
          // binary string?
          // The output can be different from the input. If the mask is
          // different, we need to do conversions.
          // Otherwise, we can just ignore it...
          //
          if ( isDate() ) {
            if ( ( getConversionMask() != null && getConversionMask().equals( storageMetadata.getConversionMask() ) )
                || ( getConversionMask() == null && storageMetadata.getConversionMask() == null ) ) {
              identicalFormat = true;
            } else {
              identicalFormat = false;
            }
          } else if ( isNumeric() ) {
            // Check the lengths first
            //
            if ( getLength() != storageMetadata.getLength() ) {
              identicalFormat = false;
            } else if ( getPrecision() != storageMetadata.getPrecision() ) {
              identicalFormat = false;
            } else if ( ( getConversionMask() != null
                && getConversionMask().equals( storageMetadata.getConversionMask() ) || ( getConversionMask() == null && storageMetadata
                .getConversionMask() == null ) ) ) {
              // For the same reasons as above, if the conversion mask, the
              // decimal or the grouping symbol changes
              // we need to convert from the binary strings to the target data
              // type and then back to a string in the required format.
              //
              if ( ( getGroupingSymbol() != null && getGroupingSymbol().equals( storageMetadata.getGroupingSymbol() ) )
                  || ( getConversionMask() == null && storageMetadata.getConversionMask() == null ) ) {
                if ( ( getDecimalFormat( false ) != null && getDecimalFormat( false ).equals(
                    storageMetadata.getDecimalFormat( false ) ) )
                    || ( getDecimalFormat( false ) == null && storageMetadata.getDecimalFormat( false ) == null ) ) {
                  identicalFormat = true;
                } else {
                  identicalFormat = false;
                }
              } else {
                identicalFormat = false;
              }
            } else {
              identicalFormat = false;
            }
          }
        }
      }
    }
  }

  /**
   * @return the trimType
   */
  @Override
  public int getTrimType() {
    return trimType;
  }

  /**
   * @param trimType
   *          the trimType to set
   */
  @Override
  public void setTrimType( int trimType ) {
    this.trimType = trimType;
  }

  public static final int getTrimTypeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < trimTypeCode.length; i++ ) {
      if ( trimTypeCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static final int getTrimTypeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < trimTypeDesc.length; i++ ) {
      if ( trimTypeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getTrimTypeByCode( tt );
  }

  public static final String getTrimTypeCode( int i ) {
    if ( i < 0 || i >= trimTypeCode.length ) {
      return trimTypeCode[0];
    }
    return trimTypeCode[i];
  }

  public static final String getTrimTypeDesc( int i ) {
    if ( i < 0 || i >= trimTypeDesc.length ) {
      return trimTypeDesc[0];
    }
    return trimTypeDesc[i];
  }

  /**
   * @return the conversionMetadata
   */
  @Override
  public ValueMetaInterface getConversionMetadata() {
    return conversionMetadata;
  }

  /**
   * @param conversionMetadata
   *          the conversionMetadata to set
   */
  @Override
  public void setConversionMetadata( ValueMetaInterface conversionMetadata ) {
    this.conversionMetadata = conversionMetadata;
  }

  /**
   * @return true if the String encoding used (storage) is single byte encoded.
   */
  @Override
  public boolean isSingleByteEncoding() {
    return singleByteEncoding;
  }

  /**
   * @return the number of binary string to native data type conversions done with this object conversions
   */
  @Override
  public long getNumberOfBinaryStringConversions() {
    return numberOfBinaryStringConversions;
  }

  /**
   * @param numberOfBinaryStringConversions
   *          the number of binary string to native data type done with this object conversions to set
   */
  @Override
  public void setNumberOfBinaryStringConversions( long numberOfBinaryStringConversions ) {
    this.numberOfBinaryStringConversions = numberOfBinaryStringConversions;
  }

  /*
   * Original JDBC RecordSetMetaData
   *
   * @see java.sql.ResultSetMetaData#isAutoIncrement()
   */
  @Override
  public boolean isOriginalAutoIncrement() {
    return originalAutoIncrement;
  }

  /*
   * Original JDBC RecordSetMetaData
   *
   * @see java.sql.ResultSetMetaData#setAutoIncrement(boolean)
   */
  @Override
  public void setOriginalAutoIncrement( boolean originalAutoIncrement ) {
    this.originalAutoIncrement = originalAutoIncrement;
  }

  /*
   * Original JDBC RecordSetMetaData
   *
   * @see java.sql.ResultSetMetaData#getColumnType()
   */
  @Override
  public int getOriginalColumnType() {
    return originalColumnType;
  }

  /*
   * Original JDBC RecordSetMetaData
   *
   * @see java.sql.ResultSetMetaData#setColumnType(int)
   */
  @Override
  public void setOriginalColumnType( int originalColumnType ) {
    this.originalColumnType = originalColumnType;
  }

  /*
   * Original JDBC RecordSetMetaData
   *
   * @see java.sql.ResultSetMetaData#getColumnTypeName()
   */
  @Override
  public String getOriginalColumnTypeName() {
    return originalColumnTypeName;
  }

  /*
   * Original JDBC RecordSetMetaData
   *
   * @see java.sql.ResultSetMetaData#setColumnTypeName(java.lang.String)
   */
  @Override
  public void setOriginalColumnTypeName( String originalColumnTypeName ) {
    this.originalColumnTypeName = originalColumnTypeName;

  }

  /*
   * Original JDBC RecordSetMetaData
   *
   * @see java.sql.ResultSetMetaData#isNullable()
   */
  @Override
  public int isOriginalNullable() {
    return originalNullable;
  }

  /*
   * Original JDBC RecordSetMetaData
   *
   * @see java.sql.ResultSetMetaData#setNullable(int)
   */
  @Override
  public void setOriginalNullable( int originalNullable ) {
    this.originalNullable = originalNullable;

  }

  /*
   * Original JDBC RecordSetMetaData
   *
   * @see java.sql.ResultSetMetaData#getPrecision()
   */
  @Override
  public int getOriginalPrecision() {
    return originalPrecision;
  }

  /*
   * Original JDBC RecordSetMetaData
   *
   * @see java.sql.ResultSetMetaData#setPrecision(int)
   */
  @Override
  public void setOriginalPrecision( int originalPrecision ) {
    this.originalPrecision = originalPrecision;
  }

  /*
   * Original JDBC RecordSetMetaData
   *
   * @see java.sql.ResultSetMetaData#getScale()
   */
  @Override
  public int getOriginalScale() {
    return originalScale;
  }

  /*
   * Original JDBC RecordSetMetaData
   *
   * @see java.sql.ResultSetMetaData#setScale(int)
   */
  @Override
  public void setOriginalScale( int originalScale ) {
    this.originalScale = originalScale;

  }

  /*
   * Original JDBC RecordSetMetaData
   *
   * @see java.sql.ResultSetMetaData#isSigned()
   */
  @Override
  public boolean isOriginalSigned() {
    return originalSigned;
  }

  /*
   * Original JDBC RecordSetMetaData
   *
   * @see java.sql.ResultSetMetaData#setOriginalSigned(boolean)
   */
  @Override
  public void setOriginalSigned( boolean originalSigned ) {
    this.originalSigned = originalSigned;
  }

  /**
   * @return the bigNumberFormatting flag : true if BigNumbers of formatted as well
   */
  public boolean isBigNumberFormatting() {
    return bigNumberFormatting;
  }

  /**
   * @param bigNumberFormatting
   *          the bigNumberFormatting flag to set : true if BigNumbers of formatted as well
   */
  public void setBigNumberFormatting( boolean bigNumberFormatting ) {
    this.bigNumberFormatting = bigNumberFormatting;
  }

  /**
   * @return The available trim type codes (NOT localized, use for persistence)
   */
  public static String[] getTrimTypeCodes() {
    return trimTypeCode;
  }

  /**
   * @return The available trim type descriptions (localized)
   */
  public static String[] getTrimTypeDescriptions() {
    return trimTypeDesc;
  }

  @Override
  public boolean requiresRealClone() {
    return type == TYPE_BINARY || type == TYPE_SERIALIZABLE;
  }

  /**
   * @return the lenientStringToNumber
   */
  @Override
  public boolean isLenientStringToNumber() {
    return lenientStringToNumber;
  }

  /**
   * @param lenientStringToNumber
   *          the lenientStringToNumber to set
   */
  @Override
  public void setLenientStringToNumber( boolean lenientStringToNumber ) {
    this.lenientStringToNumber = lenientStringToNumber;
  }

  /**
   * @return the date format time zone
   */
  @Override
  public TimeZone getDateFormatTimeZone() {
    return dateFormatTimeZone;
  }

  /**
   * @param dateFormatTimeZone
   *          the date format time zone to set
   */
  @Override
  public void setDateFormatTimeZone( TimeZone dateFormatTimeZone ) {
    this.dateFormatTimeZone = dateFormatTimeZone;
    dateFormatChanged = true;
  }

  @Override
  public void drawValue( PrimitiveGCInterface gc, Object value ) throws KettleValueException {
    // Just draw the string by default.
    //
    gc.drawText( getString( value ), 0, 0 );
  }

  @SuppressWarnings( "fallthrough" )
  @Override
  public ValueMetaInterface getValueFromSQLType( DatabaseMeta databaseMeta, String name, java.sql.ResultSetMetaData rm,
      int index, boolean ignoreLength, boolean lazyConversion ) throws KettleDatabaseException {
    try {
      int length = -1;
      int precision = -1;
      int valtype = ValueMetaInterface.TYPE_NONE;
      boolean isClob = false;

      int type = rm.getColumnType( index );
      boolean signed = false;
      try {
        signed = rm.isSigned( index );
      } catch ( Exception ignored ) {
        // This JDBC Driver doesn't support the isSigned method
        // nothing more we can do here by catch the exception.
      }
      switch ( type ) {
        case java.sql.Types.CHAR:
        case java.sql.Types.VARCHAR:
        case java.sql.Types.NVARCHAR:
        case java.sql.Types.LONGVARCHAR: // Character Large Object
          valtype = ValueMetaInterface.TYPE_STRING;
          if ( !ignoreLength ) {
            length = rm.getColumnDisplaySize( index );
          }
          break;

        case java.sql.Types.CLOB:
        case java.sql.Types.NCLOB:
          valtype = ValueMetaInterface.TYPE_STRING;
          length = DatabaseMeta.CLOB_LENGTH;
          isClob = true;
          break;

        case java.sql.Types.BIGINT:
          // verify Unsigned BIGINT overflow!
          //
          if ( signed ) {
            valtype = ValueMetaInterface.TYPE_INTEGER;
            precision = 0; // Max 9.223.372.036.854.775.807
            length = 15;
          } else {
            valtype = ValueMetaInterface.TYPE_BIGNUMBER;
            precision = 0; // Max 18.446.744.073.709.551.615
            length = 16;
          }
          break;

        case java.sql.Types.INTEGER:
          valtype = ValueMetaInterface.TYPE_INTEGER;
          precision = 0; // Max 2.147.483.647
          length = 9;
          break;

        case java.sql.Types.SMALLINT:
          valtype = ValueMetaInterface.TYPE_INTEGER;
          precision = 0; // Max 32.767
          length = 4;
          break;

        case java.sql.Types.TINYINT:
          valtype = ValueMetaInterface.TYPE_INTEGER;
          precision = 0; // Max 127
          length = 2;
          break;

        case java.sql.Types.DECIMAL:
        case java.sql.Types.DOUBLE:
        case java.sql.Types.FLOAT:
        case java.sql.Types.REAL:
        case java.sql.Types.NUMERIC:
          valtype = ValueMetaInterface.TYPE_NUMBER;
          length = rm.getPrecision( index );
          precision = rm.getScale( index );
          if ( length >= 126 ) {
            length = -1;
          }
          if ( precision >= 126 ) {
            precision = -1;
          }

          if ( type == java.sql.Types.DOUBLE || type == java.sql.Types.FLOAT || type == java.sql.Types.REAL ) {
            if ( precision == 0 ) {
              precision = -1; // precision is obviously incorrect if the type if
              // Double/Float/Real
            }

            // If we're dealing with PostgreSQL and double precision types
            if ( databaseMeta.getDatabaseInterface() instanceof PostgreSQLDatabaseMeta && type == java.sql.Types.DOUBLE
                && precision >= 16 && length >= 16 ) {
              precision = -1;
              length = -1;
            }

            // MySQL: max resolution is double precision floating point (double)
            // The (12,31) that is given back is not correct
            if ( databaseMeta.getDatabaseInterface().isMySQLVariant() ) {
              if ( precision >= length ) {
                precision = -1;
                length = -1;
              }
            }

            // if the length or precision needs a BIGNUMBER
            if ( length > 15 || precision > 15 ) {
              valtype = ValueMetaInterface.TYPE_BIGNUMBER;
            }
          } else {
            if ( precision == 0 ) {
              if ( length <= 18 && length > 0 ) { // Among others Oracle is affected
                // here.
                valtype = ValueMetaInterface.TYPE_INTEGER; // Long can hold up to 18
                // significant digits
              } else if ( length > 18 ) {
                valtype = ValueMetaInterface.TYPE_BIGNUMBER;
              }
            } else { // we have a precision: keep NUMBER or change to BIGNUMBER?
              if ( length > 15 || precision > 15 ) {
                valtype = ValueMetaInterface.TYPE_BIGNUMBER;
              }
            }
          }

          if ( databaseMeta.getDatabaseInterface() instanceof PostgreSQLDatabaseMeta
              || databaseMeta.getDatabaseInterface() instanceof GreenplumDatabaseMeta ) {
            // undefined size => arbitrary precision
            if ( type == java.sql.Types.NUMERIC && length == 0 && precision == 0 ) {
              valtype = ValueMetaInterface.TYPE_BIGNUMBER;
              length = -1;
              precision = -1;
            }
          }

          if ( databaseMeta.getDatabaseInterface() instanceof OracleDatabaseMeta ) {
            if ( precision == 0 && length == 38 ) {
              valtype = ( (OracleDatabaseMeta) databaseMeta.getDatabaseInterface() )
                .strictBigNumberInterpretation() ? TYPE_BIGNUMBER : TYPE_INTEGER;
            }
            if ( precision <= 0 && length <= 0 ) {
              // undefined size: BIGNUMBER,
              // precision on Oracle can be 38, too
              // big for a Number type
              valtype = ValueMetaInterface.TYPE_BIGNUMBER;
              length = -1;
              precision = -1;
            }
          }

          break;

        case java.sql.Types.TIMESTAMP:
          if ( databaseMeta.supportsTimestampDataType() ) {
            valtype = ValueMetaInterface.TYPE_TIMESTAMP;
            length = rm.getScale( index );
          }
          break;

        case java.sql.Types.DATE:
          if ( databaseMeta.getDatabaseInterface() instanceof TeradataDatabaseMeta ) {
            precision = 1;
          }
        case java.sql.Types.TIME:
          valtype = ValueMetaInterface.TYPE_DATE;
          //
          if ( databaseMeta.getDatabaseInterface().isMySQLVariant() ) {
            String property = databaseMeta.getConnectionProperties().getProperty( "yearIsDateType" );
            if ( property != null && property.equalsIgnoreCase( "false" )
                && rm.getColumnTypeName( index ).equalsIgnoreCase( "YEAR" ) ) {
              valtype = ValueMetaInterface.TYPE_INTEGER;
              precision = 0;
              length = 4;
              break;
            }
          }
          break;

        case java.sql.Types.BOOLEAN:
        case java.sql.Types.BIT:
          valtype = ValueMetaInterface.TYPE_BOOLEAN;
          break;

        case java.sql.Types.BINARY:
        case java.sql.Types.BLOB:
        case java.sql.Types.VARBINARY:
        case java.sql.Types.LONGVARBINARY:
          valtype = ValueMetaInterface.TYPE_BINARY;

          if ( databaseMeta.isDisplaySizeTwiceThePrecision()
              && ( 2 * rm.getPrecision( index ) ) == rm.getColumnDisplaySize( index ) ) {
            // set the length for "CHAR(X) FOR BIT DATA"
            length = rm.getPrecision( index );
          } else if ( ( databaseMeta.getDatabaseInterface() instanceof OracleDatabaseMeta )
              && ( type == java.sql.Types.VARBINARY || type == java.sql.Types.LONGVARBINARY ) ) {
            // set the length for Oracle "RAW" or "LONGRAW" data types
            valtype = ValueMetaInterface.TYPE_STRING;
            length = rm.getColumnDisplaySize( index );
          } else if ( databaseMeta.isMySQLVariant()
              && ( type == java.sql.Types.VARBINARY || type == java.sql.Types.LONGVARBINARY ) ) {
            // PDI-6677 - don't call 'length = rm.getColumnDisplaySize(index);'
            length = -1; // keep the length to -1, e.g. for string functions (e.g.
            // CONCAT see PDI-4812)
          } else if ( databaseMeta.getDatabaseInterface() instanceof SQLiteDatabaseMeta ) {
            valtype = ValueMetaInterface.TYPE_STRING;
          } else {
            length = -1;
          }
          precision = -1;
          break;

        default:
          valtype = ValueMetaInterface.TYPE_STRING;
          precision = rm.getScale( index );
          break;
      }

      ValueMetaInterface v = ValueMetaFactory.createValueMeta( name, valtype );
      v.setLength( length );
      v.setPrecision( precision );
      v.setLargeTextField( isClob );

      getOriginalColumnMetadata( v, rm, index, ignoreLength );

      // See if we need to enable lazy conversion...
      //
      if ( lazyConversion && valtype == ValueMetaInterface.TYPE_STRING ) {
        v.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
        // TODO set some encoding to go with this.

        // Also set the storage metadata. a copy of the parent, set to String too.
        //
        try {
          ValueMetaInterface storageMetaData = ValueMetaFactory.cloneValueMeta( v, ValueMetaInterface.TYPE_STRING );
          storageMetaData.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
          v.setStorageMetadata( storageMetaData );
        } catch ( Exception e ) {
          throw new SQLException( e );
        }
      }

      ValueMetaInterface newV = null;
      try {
        newV = databaseMeta.getDatabaseInterface().customizeValueFromSQLType( v, rm, index );
      } catch ( SQLException e ) {
        throw new SQLException( e );
      }
      return newV == null ? v : newV;
    } catch ( Exception e ) {
      throw new KettleDatabaseException( "Error determining value metadata from SQL resultset metadata", e );
    }
  }

  protected void getOriginalColumnMetadata( ValueMetaInterface v, ResultSetMetaData rm, int index, boolean ignoreLength )
    throws SQLException {
    // Grab the comment as a description to the field as well.
    String comments = rm.getColumnLabel( index );
    v.setComments( comments );

    // get & store more result set meta data for later use
    int originalColumnType = rm.getColumnType( index );
    v.setOriginalColumnType( originalColumnType );

    String originalColumnTypeName = rm.getColumnTypeName( index );
    v.setOriginalColumnTypeName( originalColumnTypeName );

    int originalPrecision = -1;
    if ( !ignoreLength ) {
      // Throws exception on MySQL
      originalPrecision = rm.getPrecision( index );
    }
    v.setOriginalPrecision( originalPrecision );

    int originalScale = rm.getScale( index );
    v.setOriginalScale( originalScale );

    // DISABLED FOR PERFORMANCE REASONS : PDI-1788
    //
    // boolean originalAutoIncrement=rm.isAutoIncrement(index); DISABLED FOR
    // PERFORMANCE REASONS : PDI-1788
    // v.setOriginalAutoIncrement(originalAutoIncrement);

    // int originalNullable=rm.isNullable(index); DISABLED FOR PERFORMANCE
    // REASONS : PDI-1788
    // v.setOriginalNullable(originalNullable);
    //

    boolean originalSigned = false;
    try {
      originalSigned = rm.isSigned( index );
    } catch ( Exception ignored ) {
      // This JDBC Driver doesn't support the isSigned method.
      // Nothing more we can do here.
    }
    v.setOriginalSigned( originalSigned );
  }

  /**
   * Get a value from a result set column based on the current value metadata
   *
   * @param databaseInterface
   *          the database metadata to use
   * @param resultSet
   *          The JDBC result set to read from
   * @param index
   *          The column index (1-based)
   * @return The Kettle native data type based on the value metadata
   * @throws KettleDatabaseException
   *           in case something goes wrong.
   */
  @Override
  public Object getValueFromResultSet( DatabaseInterface databaseInterface, ResultSet resultSet, int index )
    throws KettleDatabaseException {
    try {
      Object data = null;

      switch ( getType() ) {
        case ValueMetaInterface.TYPE_BOOLEAN:
          data = Boolean.valueOf( resultSet.getBoolean( index + 1 ) );
          break;
        case ValueMetaInterface.TYPE_NUMBER:
          data = new Double( resultSet.getDouble( index + 1 ) );
          break;
        case ValueMetaInterface.TYPE_BIGNUMBER:
          data = resultSet.getBigDecimal( index + 1 );
          break;
        case ValueMetaInterface.TYPE_INTEGER:
          data = Long.valueOf( resultSet.getLong( index + 1 ) );
          break;
        case ValueMetaInterface.TYPE_STRING:
          if ( isStorageBinaryString() ) {
            data = resultSet.getBytes( index + 1 );
          } else {
            data = resultSet.getString( index + 1 );
          }
          break;
        case ValueMetaInterface.TYPE_BINARY:
          if ( databaseInterface.supportsGetBlob() ) {
            Blob blob = resultSet.getBlob( index + 1 );
            if ( blob != null ) {
              data = blob.getBytes( 1L, (int) blob.length() );
            } else {
              data = null;
            }
          } else {
            data = resultSet.getBytes( index + 1 );
          }
          break;

        case ValueMetaInterface.TYPE_DATE:
          if ( getPrecision() != 1 && databaseInterface.supportsTimeStampToDateConversion() ) {
            data = resultSet.getTimestamp( index + 1 );
            break; // Timestamp extends java.util.Date
          } else if ( databaseInterface instanceof NetezzaDatabaseMeta ) {
            // PDI-10877 workaround for IBM netezza jdbc 'special' implementation
            data = getNetezzaDateValueWorkaround( databaseInterface, resultSet, index + 1 );
            break;
          } else {
            data = resultSet.getDate( index + 1 );
            break;
          }
        default:
          break;
      }
      if ( resultSet.wasNull() ) {
        data = null;
      }
      return data;
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Unable to get value '" + toStringMeta() + "' from database resultset, index "
          + index, e );
    }

  }

  private Object getNetezzaDateValueWorkaround( DatabaseInterface databaseInterface, ResultSet resultSet, int index )
    throws SQLException, KettleDatabaseException {
    Object data = null;
    int type = resultSet.getMetaData().getColumnType( index );
    switch ( type ) {
      case Types.TIME: {
        data = resultSet.getTime( index );
        break;
      }
      default: {
        data = resultSet.getDate( index );
      }
    }
    return data;
  }

  @Override
  public void setPreparedStatementValue( DatabaseMeta databaseMeta, PreparedStatement preparedStatement, int index,
      Object data ) throws KettleDatabaseException {
    try {
      switch ( getType() ) {
        case ValueMetaInterface.TYPE_NUMBER:
          if ( !isNull( data ) ) {
            double num = getNumber( data ).doubleValue();
            if ( databaseMeta.supportsFloatRoundingOnUpdate() && getPrecision() >= 0 ) {
              num = Const.round( num, getPrecision() );
            }
            preparedStatement.setDouble( index, num );
          } else {
            preparedStatement.setNull( index, java.sql.Types.DOUBLE );
          }
          break;
        case ValueMetaInterface.TYPE_INTEGER:
          if ( !isNull( data ) ) {
            if ( databaseMeta.supportsSetLong() ) {
              preparedStatement.setLong( index, getInteger( data ).longValue() );
            } else {
              double d = getNumber( data ).doubleValue();
              if ( databaseMeta.supportsFloatRoundingOnUpdate() && getPrecision() >= 0 ) {
                preparedStatement.setDouble( index, d );
              } else {
                preparedStatement.setDouble( index, Const.round( d, getPrecision() ) );
              }
            }
          } else {
            preparedStatement.setNull( index, java.sql.Types.INTEGER );
          }
          break;
        case ValueMetaInterface.TYPE_STRING:
          if ( getLength() < databaseMeta.getMaxTextFieldLength() ) {
            if ( !isNull( data ) ) {
              preparedStatement.setString( index, getString( data ) );
            } else {
              preparedStatement.setNull( index, java.sql.Types.VARCHAR );
            }
          } else {
            if ( !isNull( data ) ) {
              String string = getString( data );

              int maxlen = databaseMeta.getMaxTextFieldLength();
              int len = string.length();

              // Take the last maxlen characters of the string...
              int begin = Math.max( len - maxlen, 0 );
              if ( begin > 0 ) {
                // Truncate if logging result if it exceeds database maximum string field length
                log.logMinimal( String.format( "Truncating %d symbols of original message in '%s' field", begin, getName() ) );
                string = string.substring( begin );
              }

              if ( databaseMeta.supportsSetCharacterStream() ) {
                StringReader sr = new StringReader( string );
                preparedStatement.setCharacterStream( index, sr, string.length() );
              } else {
                preparedStatement.setString( index, string );
              }
            } else {
              preparedStatement.setNull( index, java.sql.Types.VARCHAR );
            }
          }
          break;
        case ValueMetaInterface.TYPE_DATE:
          if ( !isNull( data ) ) {
            // Environment variable to disable timezone setting for the database updates
            // When it is set, timezone will not be taken into account and the value will be converted
            // into the local java timezone
            if ( getPrecision() == 1 || !databaseMeta.supportsTimeStampToDateConversion() ) {
              // Convert to DATE!
              long dat = getInteger( data ).longValue(); // converts using Date.getTime()
              java.sql.Date ddate = new java.sql.Date( dat );
              if ( ignoreTimezone || this.getDateFormatTimeZone() == null ) {
                preparedStatement.setDate( index, ddate );
              } else {
                preparedStatement.setDate( index, ddate, Calendar.getInstance( this.getDateFormatTimeZone() ) );
              }
            } else {
              if ( data instanceof java.sql.Timestamp ) {
                // Preserve ns precision!
                //
                if ( ignoreTimezone || this.getDateFormatTimeZone() == null ) {
                  preparedStatement.setTimestamp( index, (java.sql.Timestamp) data );
                } else {
                  preparedStatement.setTimestamp( index, (java.sql.Timestamp) data, Calendar.getInstance( this
                      .getDateFormatTimeZone() ) );
                }
              } else {
                long dat = getInteger( data ).longValue(); // converts using Date.getTime()
                java.sql.Timestamp sdate = new java.sql.Timestamp( dat );
                if ( ignoreTimezone || this.getDateFormatTimeZone() == null ) {
                  preparedStatement.setTimestamp( index, sdate );
                } else {
                  preparedStatement.setTimestamp( index, sdate, Calendar.getInstance( this.getDateFormatTimeZone() ) );
                }
              }
            }
          } else {
            if ( getPrecision() == 1 || !databaseMeta.supportsTimeStampToDateConversion() ) {
              preparedStatement.setNull( index, java.sql.Types.DATE );
            } else {
              preparedStatement.setNull( index, java.sql.Types.TIMESTAMP );
            }
          }
          break;
        case ValueMetaInterface.TYPE_BOOLEAN:
          if ( databaseMeta.supportsBooleanDataType() ) {
            if ( !isNull( data ) ) {
              preparedStatement.setBoolean( index, getBoolean( data ).booleanValue() );
            } else {
              preparedStatement.setNull( index, java.sql.Types.BOOLEAN );
            }
          } else {
            if ( !isNull( data ) ) {
              preparedStatement.setString( index, getBoolean( data ).booleanValue() ? "Y" : "N" );
            } else {
              preparedStatement.setNull( index, java.sql.Types.CHAR );
            }
          }
          break;
        case ValueMetaInterface.TYPE_BIGNUMBER:
          if ( !isNull( data ) ) {
            preparedStatement.setBigDecimal( index, getBigNumber( data ) );
          } else {
            preparedStatement.setNull( index, java.sql.Types.DECIMAL );
          }
          break;
        case ValueMetaInterface.TYPE_BINARY:
          if ( !isNull( data ) ) {
            preparedStatement.setBytes( index, getBinary( data ) );
          } else {
            preparedStatement.setNull( index, java.sql.Types.BINARY );
          }
          break;
        default:
          // placeholder
          preparedStatement.setNull( index, java.sql.Types.VARCHAR );
          break;
      }
    } catch ( Exception e ) {
      throw new KettleDatabaseException( "Error setting value #" + index + " [" + toStringMeta()
          + "] on prepared statement", e );
    }
  }

  @Override
  public Object getNativeDataType( Object object ) throws KettleValueException {
    switch ( getStorageType() ) {
      case STORAGE_TYPE_BINARY_STRING:
        return convertBinaryStringToNativeType( (byte[]) object );
      case STORAGE_TYPE_INDEXED:
        return index[(Integer) object];
      case STORAGE_TYPE_NORMAL:
      default:
        return object;
    }
  }

  @Override
  public String getDatabaseColumnTypeDefinition( DatabaseInterface databaseInterface, String tk, String pk,
      boolean use_autoinc, boolean add_fieldname, boolean add_cr ) {
    return null; // No default suggestions...
  }

  protected int getQuotesBeforeSymbol( String df, String symbols ) {
    int quotes = 0;
    int stopPos = df.indexOf( symbols );
    if ( stopPos > 0 ) {
      int curPos = -1;
      do {
        curPos = df.indexOf( "'", curPos + 1 );
        if ( curPos >= 0 && curPos < stopPos ) {
          quotes++;
        }
      } while ( curPos >= 0 && curPos < stopPos );
    }
    return quotes;
  }

  @Override
  public Class<?> getNativeDataTypeClass() throws KettleValueException {
    // Not implemented for base class
    throw new KettleValueException( getTypeDesc() + " does not implement this method" );
  }
}
