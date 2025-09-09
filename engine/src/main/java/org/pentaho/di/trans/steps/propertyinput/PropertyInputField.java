/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.propertyinput;

import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Describes an Property field
 *
 * @author Samatar Hassan
 * @since 24-03-2008
 */
public class PropertyInputField implements Cloneable {
  private static Class<?> PKG = PropertyInputMeta.class; // for i18n purposes, needed by Translator2!!

  public static final int TYPE_TRIM_NONE = 0;
  public static final int TYPE_TRIM_LEFT = 1;
  public static final int TYPE_TRIM_RIGHT = 2;
  public static final int TYPE_TRIM_BOTH = 3;

  public static final String[] trimTypeCode = { "none", "left", "right", "both" };

  public static final String[] trimTypeDesc = {
    BaseMessages.getString( PKG, "PropertyInputField.TrimType.None" ),
    BaseMessages.getString( PKG, "PropertyInputField.TrimType.Left" ),
    BaseMessages.getString( PKG, "PropertyInputField.TrimType.Right" ),
    BaseMessages.getString( PKG, "PropertyInputField.TrimType.Both" ) };

  public static final int COLUMN_KEY = 0;
  public static final int COLUMN_VALUE = 1;

  public static final String[] ColumnCode = { "key", "value" };

  public static final String[] ColumnDesc = {
    BaseMessages.getString( PKG, "PropertyInputField.Column.Key" ),
    BaseMessages.getString( PKG, "PropertyInputField.Column.Value" ) };

  private String name;
  private int column;
  private int type;
  private int length;
  private String format;
  private int trimType;
  private int precision;
  private String currencySymbol;
  private String decimalSymbol;
  private String groupSymbol;
  private boolean repeat;

  private String[] samples;

  public PropertyInputField( String fieldName ) {
    this.name = fieldName;
    this.column = COLUMN_KEY;
    this.length = -1;
    this.type = ValueMetaInterface.TYPE_STRING;
    this.format = "";
    this.trimType = TYPE_TRIM_NONE;
    this.groupSymbol = "";
    this.decimalSymbol = "";
    this.currencySymbol = "";
    this.precision = -1;
    this.repeat = false;
  }
  @Override
  public String toString() {
    return String.format( "PropertyInputField: (%s, %d, %d, %d, %s, %d, %d, %s, %s, %s, %b)",
        name, column, type, length, format, trimType, precision,
        currencySymbol, decimalSymbol, groupSymbol, repeat );
  }

  public PropertyInputField() {
    this( null );
  }

  public static int getTrimTypeByCode( String tt ) {
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

  public String getColumnDesc() {
    return getColumnDesc( column );
  }

  public static String getColumnDesc( int i ) {
    if ( i < 0 || i >= ColumnDesc.length ) {
      return ColumnDesc[0];
    }
    return ColumnDesc[i];
  }

  public static int getTrimTypeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < trimTypeDesc.length; i++ ) {
      if ( trimTypeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public String getColumnCode() {
    return getColumnCode( column );
  }

  public static String getColumnCode( int i ) {
    if ( i < 0 || i >= ColumnCode.length ) {
      return ColumnCode[0];
    }
    return ColumnCode[i];
  }

  public static int getColumnByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < ColumnCode.length; i++ ) {
      if ( ColumnCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static String getTrimTypeCode( int i ) {
    if ( i < 0 || i >= trimTypeCode.length ) {
      return trimTypeCode[0];
    }
    return trimTypeCode[i];
  }

  public static String getTrimTypeDesc( int i ) {
    if ( i < 0 || i >= trimTypeDesc.length ) {
      return trimTypeDesc[0];
    }
    return trimTypeDesc[i];
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  public int getLength() {
    return length;
  }

  public void setLength( int length ) {
    this.length = length;
  }

  public String getName() {
    return name;
  }

  public void setName( String fieldName ) {
    this.name = fieldName;
  }

  public int getType() {
    return type;
  }

  public String getTypeDesc() {
    return ValueMetaFactory.getValueMetaName( type );
  }

  public void setType( int type ) {
    this.type = type;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat( String format ) {
    this.format = format;
  }

  public void setSamples( String[] samples ) {
    this.samples = samples;
  }

  public String[] getSamples() {
    return samples;
  }

  public int getTrimType() {
    return trimType;
  }

  public String getTrimTypeCode() {
    return getTrimTypeCode( trimType );
  }

  public String getTrimTypeDesc() {
    return getTrimTypeDesc( trimType );
  }

  public void setTrimType( int trimType ) {
    this.trimType = trimType;
  }

  public String getGroupSymbol() {
    return groupSymbol;
  }

  public void setGroupSymbol( String groupSymbol ) {
    this.groupSymbol = groupSymbol;
  }

  public String getDecimalSymbol() {
    return decimalSymbol;
  }

  public void setDecimalSymbol( String decimalSymbol ) {
    this.decimalSymbol = decimalSymbol;
  }

  public String getCurrencySymbol() {
    return currencySymbol;
  }

  public void setCurrencySymbol( String currencySymbol ) {
    this.currencySymbol = currencySymbol;
  }

  public int getPrecision() {
    return precision;
  }

  public void setPrecision( int precision ) {
    this.precision = precision;
  }

  public boolean isRepeated() {
    return repeat;
  }

  public void setRepeated( boolean repeat ) {
    this.repeat = repeat;
  }

  public void flipRepeated() {
    repeat = !repeat;
  }

  public static int getColumnByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < ColumnDesc.length; i++ ) {
      if ( ColumnDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public void setColumn( int column ) {
    this.column = column;
  }

}
