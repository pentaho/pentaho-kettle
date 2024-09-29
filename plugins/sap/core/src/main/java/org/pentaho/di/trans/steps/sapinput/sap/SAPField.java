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


package org.pentaho.di.trans.steps.sapinput.sap;

public class SAPField {

  private String name;
  private String table;
  private String type;
  private String typePentaho;
  private String typeSAP;
  private Object value;
  private String description;
  private String defaultvalue;

  public SAPField( String name, String table, String type ) {
    super();
    this.name = name;
    this.table = table;
    this.type = type;
  }

  public SAPField( String name, String table, String type, Object value ) {
    super();
    this.name = name;
    this.table = table;
    this.type = type;
    this.value = value;
  }

  @Override
  public String toString() {
    return "SAPField [name="
      + name + ", table=" + table + ", type=" + type + ", typePentaho=" + typePentaho + ", typeSAP=" + typeSAP
      + ", value=" + value + ", defaultvalue=" + defaultvalue + ", description=" + description + "]";
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public String getTable() {
    return table;
  }

  public void setTable( String table ) {
    this.table = table;
  }

  public Object getValue() {
    return value;
  }

  public void setValue( Object value ) {
    this.value = value;
  }

  public String getTypePentaho() {
    return typePentaho;
  }

  public void setTypePentaho( String typepentaho ) {
    this.typePentaho = typepentaho;
  }

  public String getTypeSAP() {
    return typeSAP;
  }

  public void setTypeSAP( String typesap ) {
    this.typeSAP = typesap;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getDefaultvalue() {
    return defaultvalue;
  }

  public void setDefaultvalue( String defaultvalue ) {
    this.defaultvalue = defaultvalue;
  }

}
