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


package org.pentaho.di.trans.steps.exceloutput;

import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.row.value.ValueMetaFactory;

/**
 * Describes a single field in an excel file
 *
 * TODO: allow the width of a column to be set --> data.sheet.setColumnView(column, width);
 * TODO: allow the default font to be set
 * TODO: allow an aggregation formula on one of the columns --> SUM(A2:A151)
 *
 * @author Matt
 * @since 7-09-2006
 *
 */
public class ExcelField implements Cloneable {
  @Injection( name = "NAME", group = "FIELDS" )
  private String name;
  private int type;
  @Injection( name = "FORMAT", group = "FIELDS" )
  private String format;

  public ExcelField( String name, int type, String format ) {
    this.name = name;
    this.type = type;
    this.format = format;
  }

  public ExcelField() {
  }

  public int compare( Object obj ) {
    ExcelField field = (ExcelField) obj;

    return name.compareTo( field.getName() );
  }

  public boolean equal( Object obj ) {
    ExcelField field = (ExcelField) obj;

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

  @Override
  public String toString() {
    return name + ":" + getTypeDesc();
  }
}
