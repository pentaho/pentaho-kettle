/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.excelinput;

import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;

/**
 * Describes a single field in an excel file
 *
 * @author Matt
 * @since 12-04-2006
 *
 */
public class ExcelInputField implements Cloneable {
  @Injection( name = "NAME", group = "FIELDS" )
  private String name;
  private int type;
  @Injection( name = "LENGTH", group = "FIELDS" )
  private int length = -1;
  @Injection( name = "PRECISION", group = "FIELDS" )
  private int precision = -1;
  private int trimtype;
  @Injection( name = "FORMAT", group = "FIELDS" )
  private String format;
  @Injection( name = "CURRENCY", group = "FIELDS" )
  private String currencySymbol;
  @Injection( name = "DECIMAL", group = "FIELDS" )
  private String decimalSymbol;
  @Injection( name = "GROUP", group = "FIELDS" )
  private String groupSymbol;
  @Injection( name = "REPEAT", group = "FIELDS" )
  private boolean repeat;

  public ExcelInputField( String fieldname, int position, int length ) {
    this.name = fieldname;
    this.length = length;
    this.type = ValueMetaInterface.TYPE_STRING;
    this.format = "";
    this.trimtype = ExcelInputMeta.TYPE_TRIM_NONE;
    this.groupSymbol = "";
    this.decimalSymbol = "";
    this.currencySymbol = "";
    this.precision = -1;
    this.repeat = false;
  }

  public ExcelInputField() {
    this( null, -1, -1 );
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

  @Injection( name = "TYPE", group = "FIELDS" )
  public void setType( String typeDesc ) {
    this.type = ValueMetaFactory.getIdForValueMeta( typeDesc );
  }

  public String getFormat() {
    return format;
  }

  public void setFormat( String format ) {
    this.format = format;
  }

  public int getTrimType() {
    return trimtype;
  }

  public String getTrimTypeCode() {
    return ExcelInputMeta.getTrimTypeCode( trimtype );
  }

  public String getTrimTypeDesc() {
    return ExcelInputMeta.getTrimTypeDesc( trimtype );
  }

  public void setTrimType( int trimtype ) {
    this.trimtype = trimtype;
  }

  @Injection( name = "TRIM_TYPE", group = "FIELDS" )
  public void setTrimType( String trimType ) {
    this.trimtype = ValueMetaBase.getTrimTypeByCode( trimType );
  }

  public String getGroupSymbol() {
    return groupSymbol;
  }

  public void setGroupSymbol( String group_symbol ) {
    this.groupSymbol = group_symbol;
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

  public boolean isRepeated() {
    return repeat;
  }

  public void setRepeated( boolean repeat ) {
    this.repeat = repeat;
  }

  public void flipRepeated() {
    repeat = !repeat;
  }

  @Override
  public String toString() {
    return name + ":" + getTypeDesc() + "(" + length + "," + precision + ")";
  }
}
