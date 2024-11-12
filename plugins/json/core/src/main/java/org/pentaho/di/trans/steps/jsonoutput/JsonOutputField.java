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


package org.pentaho.di.trans.steps.jsonoutput;

/**
 * Describes a single field in an Json output file
 *
 * @author Samatar
 * @since 14-june-2010
 *
 */
public class JsonOutputField implements Cloneable {
  private String fieldName;
  private String elementName;

  public JsonOutputField( String fieldName, String elementName, int type, String format, int length,
    int precision, String currencySymbol, String decimalSymbol, String groupSymbol, String nullString,
    boolean attribute, String attributeParentName ) {
    this.fieldName = fieldName;
    this.elementName = elementName;
  }

  public JsonOutputField() {
  }

  public int compare( Object obj ) {
    JsonOutputField field = (JsonOutputField) obj;

    return fieldName.compareTo( field.getFieldName() );
  }

  public boolean equal( Object obj ) {
    JsonOutputField field = (JsonOutputField) obj;

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

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName( String fieldname ) {
    this.fieldName = fieldname;
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
}
