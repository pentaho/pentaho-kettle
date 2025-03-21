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


package org.pentaho.di.trans.steps.salesforceinput;

public class FieldDTO {

  String name;
  String field;
  boolean idlookup;
  String type;
  String length;
  String precision;

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getField() {
    return field;
  }

  public void setField( String field ) {
    this.field = field;
  }

  public boolean isIdlookup() {
    return idlookup;
  }

  public void setIdlookup( boolean idlookup ) {
    this.idlookup = idlookup;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public String getLength() {
    return length;
  }

  public void setLength( String length ) {
    this.length = length;
  }

  public String getPrecision() {
    return precision;
  }

  public void setPrecision( String precision ) {
    this.precision = precision;
  }
}
