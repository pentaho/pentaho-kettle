/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
import java.util.Locale;
import java.util.TimeZone;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Node;

/**
 * Convenience class for backward compatibility.
 *
 *
 */
@Deprecated
public class ValueMeta extends ValueMetaBase {
  private static Class<?> PKG = Const.class;

  public static final String DEFAULT_DATE_FORMAT_MASK = "yyyy/MM/dd HH:mm:ss.SSS";

  public static final String DEFAULT_TIMESTAMP_FORMAT_MASK  = "yyyy/MM/dd HH:mm:ss.SSSSSSSSS";

  public static final String XML_META_TAG = "value-meta";
  public static final String XML_DATA_TAG = "value-data";

  public static final boolean EMPTY_STRING_AND_NULL_ARE_DIFFERENT = convertStringToBoolean( Const.NVL( System
    .getProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "N" ), "N" ) );

  /**
   * The trim type codes
   */
  public static final String[] trimTypeCode = { "none", "left", "right", "both" };

  /**
   * The trim description
   */
  public static final String[] trimTypeDesc = {
    BaseMessages.getString( PKG, "ValueMeta.TrimType.None" ),
    BaseMessages.getString( PKG, "ValueMeta.TrimType.Left" ),
    BaseMessages.getString( PKG, "ValueMeta.TrimType.Right" ),
    BaseMessages.getString( PKG, "ValueMeta.TrimType.Both" ) };

  public static final String[] SINGLE_BYTE_ENCODINGS = new String[] {
    "ISO8859_1", "Cp1252", "ASCII", "Cp037", "Cp273", "Cp277", "Cp278", "Cp280", "Cp284", "Cp285", "Cp297",
    "Cp420", "Cp424", "Cp437", "Cp500", "Cp737", "Cp775", "Cp850", "Cp852", "Cp855", "Cp856", "Cp857", "Cp858",
    "Cp860", "Cp861", "Cp862", "Cp863", "Cp865", "Cp866", "Cp869", "Cp870", "Cp871", "Cp875", "Cp918", "Cp921",
    "Cp922", "Cp1140", "Cp1141", "Cp1142", "Cp1143", "Cp1144", "Cp1145", "Cp1146", "Cp1147", "Cp1148", "Cp1149",
    "Cp1250", "Cp1251", "Cp1253", "Cp1254", "Cp1255", "Cp1257", "ISO8859_2", "ISO8859_3", "ISO8859_5",
    "ISO8859_5", "ISO8859_6", "ISO8859_7", "ISO8859_8", "ISO8859_9", "ISO8859_13", "ISO8859_15",
    "ISO8859_15_FDIS", "MacCentralEurope", "MacCroatian", "MacCyrillic", "MacDingbat", "MacGreek", "MacHebrew",
    "MacIceland", "MacRoman", "MacRomania", "MacSymbol", "MacTurkish", "MacUkraine", };

  private ValueMetaInterface nativeType; // Used only for getNativeDataTypeClass(), not a "deep" clone of this object

  public ValueMeta() {
    this( null, ValueMetaInterface.TYPE_NONE, -1, -1 );
  }

  @Deprecated
  public ValueMeta( String name ) {
    this( name, ValueMetaInterface.TYPE_NONE, -1, -1 );
  }

  public ValueMeta( String name, int type ) {
    this( name, type, -1, -1 );
  }

  @Deprecated
  public ValueMeta( String name, int type, int storageType ) {
    this( name, type, -1, -1 );
    setStorageType( storageType );
  }

  public ValueMeta( String name, int type, int length, int precision ) {
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
    this.dateFormatTimeZone = TimeZone.getDefault();
    this.identicalFormat = true;
    this.bigNumberFormatting = true;
    this.lenientStringToNumber =
      convertStringToBoolean( Const.NVL( System.getProperty(
        Const.KETTLE_LENIENT_STRING_TO_NUMBER_CONVERSION, "N" ), "N" ) );

    super.determineSingleByteEncoding();
    setDefaultConversionMask();
  }

  /**
   * @param inputStream
   * @throws KettleFileException
   * @throws KettleEOFException
   * @deprecated
   */
  @Deprecated
  public ValueMeta( DataInputStream inputStream ) throws KettleFileException, KettleEOFException {
    super( inputStream );
  }

  /**
   * @param node
   * @throws KettleException
   * @deprecated
   */
  @Deprecated
  public ValueMeta( Node node ) throws KettleException {
    super( node );
  }

  /**
   * @deprecated
   */
  @Override
  @Deprecated
  public void setType( int type ) {
    super.setType( type );
  }

  @Override
  public Class<?> getNativeDataTypeClass() throws KettleValueException {
    if ( nativeType == null ) {
      try {
        nativeType = ValueMetaFactory.createValueMeta( getType() );
      } catch ( KettlePluginException e ) {
        throw new KettleValueException( e );
      }
    }
    return nativeType.getNativeDataTypeClass();
  }
}
