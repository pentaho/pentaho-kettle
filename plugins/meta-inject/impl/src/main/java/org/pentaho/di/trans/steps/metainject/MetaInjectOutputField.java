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

package org.pentaho.di.trans.steps.metainject;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionTypeConverter;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;

public class MetaInjectOutputField {

  @Injection( name = "SOURCE_OUTPUT_NAME", group = "SOURCE_OUTPUT_FIELDS" )
  private String name;

  @Injection( name = "SOURCE_OUTPUT_TYPE", group = "SOURCE_OUTPUT_FIELDS", converter = DataTypeConverter.class )
  private int type;

  @Injection( name = "SOURCE_OUTPUT_LENGTH", group = "SOURCE_OUTPUT_FIELDS" )
  private int length;

  @Injection( name = "SOURCE_OUTPUT_PRECISION", group = "SOURCE_OUTPUT_FIELDS" )
  private int precision;

  public MetaInjectOutputField() {
  }

  public MetaInjectOutputField( String name, int type, int length, int precision ) {
    super();
    this.name = name;
    this.type = type;
    this.length = length;
    this.precision = precision;
  }

  public String getTypeDescription() {
    return ValueMetaFactory.getValueMetaName( type );
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public int getType() {
    return type;
  }

  public void setType( int type ) {
    this.type = type;
  }

  public int getLength() {
    return length;
  }

  public void setLength( int length ) {
    this.length = length;
  }

  public int getPrecision() {
    return precision;
  }

  public void setPrecision( int precision ) {
    this.precision = precision;
  }

  public ValueMetaInterface createValueMeta() throws KettlePluginException {
    return ValueMetaFactory.createValueMeta( name, type, length, precision );
  }

  public static class DataTypeConverter extends InjectionTypeConverter {
    @Override
    public int string2intPrimitive( String v ) throws KettleValueException {
      return ValueMetaBase.getType( v );
    }
  }
}
