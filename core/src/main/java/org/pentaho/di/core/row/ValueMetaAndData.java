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

import java.math.BigDecimal;
import java.util.Date;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaSerializable;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

public class ValueMetaAndData {
  public static final String XML_TAG = "value";

  private ValueMetaInterface valueMeta;
  private Object valueData;

  public ValueMetaAndData() {
  }

  /**
   * @param valueMeta
   * @param valueData
   */
  public ValueMetaAndData( ValueMetaInterface valueMeta, Object valueData ) {
    this.valueMeta = valueMeta;
    this.valueData = valueData;
  }

  public ValueMetaAndData( String valueName, Object valueData ) throws KettleValueException {
    this.valueData = valueData;
    if ( valueData instanceof String ) {
      this.valueMeta = new ValueMetaString( valueName );
    } else if ( valueData instanceof Double ) {
      this.valueMeta = new ValueMetaNumber( valueName );
    } else if ( valueData instanceof Long ) {
      this.valueMeta = new ValueMetaInteger( valueName );
    } else if ( valueData instanceof Date ) {
      this.valueMeta = new ValueMetaDate( valueName, ValueMetaInterface.TYPE_DATE );
    } else if ( valueData instanceof BigDecimal ) {
      this.valueMeta = new ValueMetaBigNumber( valueName );
    } else if ( valueData instanceof Boolean ) {
      this.valueMeta = new ValueMetaBoolean( valueName );
    } else if ( valueData instanceof byte[] ) {
      this.valueMeta = new ValueMetaBinary( valueName );
    } else {
      this.valueMeta = new ValueMetaSerializable( valueName );
    }
  }

  @Override
  public Object clone() {
    ValueMetaAndData vmad = new ValueMetaAndData();
    try {
      vmad.valueData = valueMeta.cloneValueData( valueData );
    } catch ( KettleValueException e ) {
      vmad.valueData = null; // TODO: should we really do this? Is it safe?
    }
    vmad.valueMeta = valueMeta.clone();

    return vmad;
  }

  public static final String VALUE_REPOSITORY_NUMBER_CONVERSION_MASK = "#.#";
  public static final String VALUE_REPOSITORY_INTEGER_CONVERSION_MASK = "#";
  public static final String VALUE_REPOSITORY_DATE_CONVERSION_MASK = "yyyy/MM/dd HH:mm:ss.SSS";
  public static final String VALUE_REPOSITORY_DECIMAL_SYMBOL = ".";
  public static final String VALUE_REPOSITORY_GROUPING_SYMBOL = ",";

  @Override
  public String toString() {
    try {
      return valueMeta.getString( valueData );
    } catch ( KettleValueException e ) {
      return "<![" + e.getMessage() + "]!>";
    }
  }

  /**
   * Produce the XML representation of this value.
   *
   * @return a String containing the XML to represent this Value.
   * @throws KettleValueException
   *           in case there is a data conversion error, only throws in case of lazy conversion
   */
  public String getXML() throws KettleValueException {
    ValueMetaInterface meta = valueMeta.clone();
    meta.setDecimalSymbol( "." );
    meta.setGroupingSymbol( null );
    meta.setCurrencySymbol( null );

    StringBuilder retval = new StringBuilder( 128 );
    retval.append( "<" + XML_TAG + ">" );
    retval.append( XMLHandler.addTagValue( "name", meta.getName(), false ) );
    retval.append( XMLHandler.addTagValue( "type", meta.getTypeDesc(), false ) );
    try {
      retval.append( XMLHandler.addTagValue( "text", meta.getCompatibleString( valueData ), false ) );
    } catch ( KettleValueException e ) {
      // LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
      retval.append( XMLHandler.addTagValue( "text", "", false ) );
    }
    retval.append( XMLHandler.addTagValue( "length", meta.getLength(), false ) );
    retval.append( XMLHandler.addTagValue( "precision", meta.getPrecision(), false ) );
    retval.append( XMLHandler.addTagValue( "isnull", meta.isNull( valueData ), false ) );
    retval.append( XMLHandler.addTagValue( "mask", meta.getConversionMask(), false ) );
    retval.append( "</" + XML_TAG + ">" );

    return retval.toString();
  }

  /**
   * Construct a new Value and read the data from XML
   *
   * @param valnode
   *          The XML Node to read from.
   */
  public ValueMetaAndData( Node valnode ) {
    this();
    loadXML( valnode );
  }

  /**
   * Read the data for this Value from an XML Node
   *
   * @param valnode
   *          The XML Node to read from
   * @return true if all went well, false if something went wrong.
   */
  public boolean loadXML( Node valnode ) {
    valueMeta = null;

    try {
      String valname = XMLHandler.getTagValue( valnode, "name" );
      int valtype = ValueMetaBase.getType( XMLHandler.getTagValue( valnode, "type" ) );
      String text = XMLHandler.getTagValue( valnode, "text" );
      boolean isnull = "Y".equalsIgnoreCase( XMLHandler.getTagValue( valnode, "isnull" ) );
      int len = Const.toInt( XMLHandler.getTagValue( valnode, "length" ), -1 );
      int prec = Const.toInt( XMLHandler.getTagValue( valnode, "precision" ), -1 );
      String mask = XMLHandler.getTagValue( valnode, "mask" );

      valueMeta = new ValueMeta( valname, valtype );
      valueData = text;
      valueMeta.setLength( len );
      valueMeta.setPrecision( prec );
      if ( mask != null ) {
        valueMeta.setConversionMask( mask );
      }

      if ( valtype != ValueMetaInterface.TYPE_STRING ) {
        ValueMetaInterface originMeta = new ValueMetaString( valname );
        if ( valueMeta.isNumeric() ) {
          originMeta.setDecimalSymbol( "." );
          originMeta.setGroupingSymbol( null );
          originMeta.setCurrencySymbol( null );
        }
        if ( valtype == ValueMetaInterface.TYPE_DATE ) {
          originMeta.setConversionMask( ValueMetaBase.COMPATIBLE_DATE_FORMAT_PATTERN );
        }
        valueData = Const.trim( text );
        valueData = valueMeta.convertData( originMeta, valueData );
      }

      if ( isnull ) {
        valueData = null;
      }
    } catch ( Exception e ) {
      valueData = null;
      return false;
    }

    return true;
  }

  public String toStringMeta() {
    return valueMeta.toStringMeta();
  }

  /**
   * @return the valueData
   */
  public Object getValueData() {
    return valueData;
  }

  /**
   * @param valueData
   *          the valueData to set
   */
  public void setValueData( Object valueData ) {
    this.valueData = valueData;
  }

  /**
   * @return the valueMeta
   */
  public ValueMetaInterface getValueMeta() {
    return valueMeta;
  }

  /**
   * @param valueMeta
   *          the valueMeta to set
   */
  public void setValueMeta( ValueMetaInterface valueMeta ) {
    this.valueMeta = valueMeta;
  }

}
