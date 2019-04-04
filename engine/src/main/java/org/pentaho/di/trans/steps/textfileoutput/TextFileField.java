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

package org.pentaho.di.trans.steps.textfileoutput;

import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;

/**
 * Describes a single field in a text file
 *
 * @author Matt
 * @since 11-05-2005
 *
 */
public class TextFileField implements Cloneable {
  @Injection( name = "OUTPUT_FIELDNAME", group = "OUTPUT_FIELDS" )
  private String name;

  private int type;

  @Injection( name = "OUTPUT_FORMAT", group = "OUTPUT_FIELDS" )
  private String format;

  @Injection( name = "OUTPUT_LENGTH", group = "OUTPUT_FIELDS" )
  private int length = -1;

  @Injection( name = "OUTPUT_PRECISION", group = "OUTPUT_FIELDS" )
  private int precision = -1;

  @Injection( name = "OUTPUT_CURRENCY", group = "OUTPUT_FIELDS" )
  private String currencySymbol;

  @Injection( name = "OUTPUT_DECIMAL", group = "OUTPUT_FIELDS" )
  private String decimalSymbol;

  @Injection( name = "OUTPUT_GROUP", group = "OUTPUT_FIELDS" )
  private String groupingSymbol;

  @Injection( name = "OUTPUT_NULL", group = "OUTPUT_FIELDS" )
  private String nullString;

  private int trimType;

  public TextFileField( String name, int type, String format, int length, int precision, String currencySymbol,
    String decimalSymbol, String groupSymbol, String nullString ) {
    this.name = name;
    this.type = type;
    this.format = format;
    this.length = length;
    this.precision = precision;
    this.currencySymbol = currencySymbol;
    this.decimalSymbol = decimalSymbol;
    this.groupingSymbol = groupSymbol;
    this.nullString = nullString;
  }

  public TextFileField() {
  }

  public int compare( Object obj ) {
    TextFileField field = (TextFileField) obj;

    return name.compareTo( field.getName() );
  }

  public boolean equal( Object obj ) {
    TextFileField field = (TextFileField) obj;

    return name.equals( field.getName() );
  }

  @Override
  public Object clone() {
    try {
      Object retval = super.clone();
      return retval;
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

  public void setName( String fieldname ) {
    this.name = fieldname;
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

  @Injection( name = "OUTPUT_TYPE", group = "OUTPUT_FIELDS" )
  public void setType( String typeDesc ) {
    this.type = ValueMetaFactory.getIdForValueMeta( typeDesc );
  }

  public String getFormat() {
    return format;
  }

  public void setFormat( String format ) {
    this.format = format;
  }

  public String getGroupingSymbol() {
    return groupingSymbol;
  }

  public void setGroupingSymbol( String group_symbol ) {
    this.groupingSymbol = group_symbol;
  }

  public String getDecimalSymbol() {
    return decimalSymbol;
  }

  public void setDecimalSymbol( String decimal_symbol ) {
    this.decimalSymbol = decimal_symbol;
  }

  public String getCurrencySymbol() {
    return currencySymbol;
  }

  public void setCurrencySymbol( String currency_symbol ) {
    this.currencySymbol = currency_symbol;
  }

  public int getPrecision() {
    return precision;
  }

  public void setPrecision( int precision ) {
    this.precision = precision;
  }

  public String getNullString() {
    return nullString;
  }

  public void setNullString( String null_string ) {
    this.nullString = null_string;
  }

  @Override
  public String toString() {
    return name + ":" + getTypeDesc();
  }

  public int getTrimType() {
    return trimType;
  }

  public void setTrimType( int trimType ) {
    this.trimType = trimType;
  }

  @Injection( name = "OUTPUT_TRIM", group = "OUTPUT_FIELDS" )
  public void setTrimTypeByDesc( String value ) {
    this.trimType = ValueMetaString.getTrimTypeByDesc( value );
  }

  public String getTrimTypeCode() {
    return ValueMetaString.getTrimTypeCode( trimType );
  }

  public String getTrimTypeDesc() {
    return ValueMetaString.getTrimTypeDesc( trimType );
  }
}
