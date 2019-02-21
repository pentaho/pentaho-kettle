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

package org.pentaho.di.trans.steps.xmloutput;

import com.google.common.base.Enums;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.row.ValueMeta;

/**
 * Describes a single field in an XML output file
 * 
 * @author Matt
 * @since 14-jan-2006
 * 
 */
public class XMLField implements Cloneable {

  public enum ContentType {
    Element, Attribute;

    /**
     * [PDI-15575] Ensuring that this enum can return with some default value. Necessary for when being used on the
     * GUI side if a user leaves the field empty, it will enforce a default value. This allows the object to be saved,
     * loaded, and cloned as necessary.
     * @param contentType
     * @return ContentType
     */
    public static ContentType getIfPresent( String contentType ) {
      return Enums.getIfPresent( ContentType.class, contentType ).or( Element );
    }
  }

  @Injection( name = "OUTPUT_FIELDNAME", group = "OUTPUT_FIELDS" )
  private String fieldName;

  @Injection( name = "OUTPUT_ELEMENTNAME", group = "OUTPUT_FIELDS" )
  private String elementName;

  private int type;

  @Injection( name = "OUTPUT_FORMAT", group = "OUTPUT_FIELDS" )
  private String format;

  @Injection( name = "OUTPUT_LENGTH", group = "OUTPUT_FIELDS" )
  private int length;

  @Injection( name = "OUTPUT_PRECISION", group = "OUTPUT_FIELDS" )
  private int precision;

  @Injection( name = "OUTPUT_CURRENCY", group = "OUTPUT_FIELDS" )
  private String currencySymbol;

  @Injection( name = "OUTPUT_DECIMAL", group = "OUTPUT_FIELDS" )
  private String decimalSymbol;

  @Injection( name = "OUTPUT_GROUP", group = "OUTPUT_FIELDS" )
  private String groupingSymbol;

  @Injection( name = "OUTPUT_NULL", group = "OUTPUT_FIELDS" )
  private String nullString;

  @Injection( name = "OUTPUT_CONTENT_TYPE", group = "OUTPUT_FIELDS" )
  private ContentType contentType;

  public XMLField( ContentType contentType, String fieldName, String elementName, int type, String format, int length,
      int precision, String currencySymbol, String decimalSymbol, String groupSymbol, String nullString ) {
    this.contentType = contentType;
    this.fieldName = fieldName;
    this.elementName = elementName;
    this.type = type;
    this.format = format;
    this.length = length;
    this.precision = precision;
    this.currencySymbol = currencySymbol;
    this.decimalSymbol = decimalSymbol;
    this.groupingSymbol = groupSymbol;
    this.nullString = nullString;
  }

  public XMLField() {
    contentType = ContentType.Element;
  }

  public int compare( Object obj ) {
    XMLField field = (XMLField) obj;

    return fieldName.compareTo( field.getFieldName() );
  }

  public boolean equal( Object obj ) {
    XMLField field = (XMLField) obj;

    return fieldName.equals( field.getFieldName() );
  }

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

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName( String fieldname ) {
    this.fieldName = fieldname;
  }

  public int getType() {
    return type;
  }

  public String getTypeDesc() {
    return ValueMeta.getTypeDesc( type );
  }

  public void setType( int type ) {
    this.type = type;
  }

  @Injection( name = "OUTPUT_TYPE", group = "OUTPUT_FIELDS" )
  public void setType( String typeDesc ) {
    this.type = ValueMeta.getType( typeDesc );
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

  public String toString() {
    return fieldName + ":" + getTypeDesc() + ":" + elementName;
  }

  /**
   * @return Returns the elementName.
   */
  public String getElementName() {
    return elementName;
  }

  /**
   * @param elementName
   *          The elementName to set.
   */
  public void setElementName( String elementName ) {
    this.elementName = elementName;
  }

  /**
   * @return the contentType
   */
  public ContentType getContentType() {
    return contentType;
  }

  /**
   * @param contentType
   *          the contentType to set
   */
  public void setContentType( ContentType contentType ) {
    this.contentType = contentType;
  }
}
