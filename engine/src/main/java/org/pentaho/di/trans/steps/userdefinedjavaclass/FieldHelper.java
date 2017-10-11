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

package org.pentaho.di.trans.steps.userdefinedjavaclass;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.regex.Pattern;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

public class FieldHelper {
  private int index = -1;
  private ValueMetaInterface meta;

  public FieldHelper( RowMetaInterface rowMeta, String fieldName ) {
    this.meta = rowMeta.searchValueMeta( fieldName );
    this.index = rowMeta.indexOfValue( fieldName );
    if ( this.index == -1 ) {
      throw new IllegalArgumentException( String.format(
        "FieldHelper could not be initialized. The field named '%s' not found in RowMeta: %s", fieldName,
        rowMeta.toStringMeta() ) );
    }
  }

  public Object getObject( Object[] dataRow ) {
    return dataRow[index];
  }

  public BigDecimal getBigNumber( Object[] dataRow ) throws KettleValueException {
    return meta.getBigNumber( dataRow[index] );
  }

  public byte[] getBinary( Object[] dataRow ) throws KettleValueException {
    return meta.getBinary( dataRow[index] );
  }

  public Boolean getBoolean( Object[] dataRow ) throws KettleValueException {
    return meta.getBoolean( dataRow[index] );
  }

  public Date getDate( Object[] dataRow ) throws KettleValueException {
    return meta.getDate( dataRow[index] );
  }

  public Long getInteger( Object[] dataRow ) throws KettleValueException {
    return meta.getInteger( dataRow[index] );
  }

  public Double getNumber( Object[] dataRow ) throws KettleValueException {
    return meta.getNumber( dataRow[index] );
  }

  public Serializable getSerializable( Object[] dataRow ) throws KettleValueException {
    return (Serializable) dataRow[index];
  }

  public String getString( Object[] dataRow ) throws KettleValueException {
    return meta.getString( dataRow[index] );
  }

  public ValueMetaInterface getValueMeta() {
    return meta;
  }

  public int indexOfValue() {
    return index;
  }

  public void setValue( Object[] dataRow, Object value ) {
    dataRow[index] = value;
  }

  private static final Pattern validJavaIdentifier = Pattern.compile( "^[\\w&&\\D]\\w*" );

  public static String getAccessor( boolean isIn, String fieldName ) {
    StringBuilder sb = new StringBuilder( "get(Fields." );
    sb.append( isIn ? "In" : "Out" );
    sb.append( String.format( ", \"%s\")", fieldName.replace( "\\", "\\\\" ).replace( "\"", "\\\"" ) ) );
    return sb.toString();
  }

  public static String getGetSignature( String accessor, ValueMetaInterface v ) {
    StringBuilder sb = new StringBuilder();

    switch ( v.getType() ) {
      case ValueMetaInterface.TYPE_BIGNUMBER:
        sb.append( "BigDecimal " );
        break;
      case ValueMetaInterface.TYPE_BINARY:
        sb.append( "byte[] " );
        break;
      case ValueMetaInterface.TYPE_BOOLEAN:
        sb.append( "Boolean " );
        break;
      case ValueMetaInterface.TYPE_DATE:
        sb.append( "Date " );
        break;
      case ValueMetaInterface.TYPE_INTEGER:
        sb.append( "Long " );
        break;
      case ValueMetaInterface.TYPE_NUMBER:
        sb.append( "Double " );
        break;
      case ValueMetaInterface.TYPE_STRING:
        sb.append( "String " );
        break;
      case ValueMetaInterface.TYPE_SERIALIZABLE:
      default:
        sb.append( "Object " );
        break;
    }

    if ( validJavaIdentifier.matcher( v.getName() ).matches() ) {
      sb.append( v.getName() );
    } else {
      sb.append( "value" );
    }
    String typeDesc = v.getTypeDesc();
    sb
      .append( " = " ).append( accessor ).append( ".get" ).append( "-".equals( typeDesc ) ? "Object" : typeDesc )
      .append( "(r);" );

    return sb.toString();
  }
}
