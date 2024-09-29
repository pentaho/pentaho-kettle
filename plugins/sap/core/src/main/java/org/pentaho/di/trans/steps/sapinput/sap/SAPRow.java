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

import java.util.Collection;
import java.util.Vector;

public class SAPRow {

  private Collection<SAPField> fields = new Vector<SAPField>();

  public SAPRow() {
    super();
  }

  public SAPRow( Collection<SAPField> fields ) {
    super();
    this.fields = fields;
  }

  @Override
  public String toString() {
    return "SAPRow [field=" + fields + "]";
  }

  public Collection<SAPField> getFields() {
    return fields;
  }

  public void setField( Collection<SAPField> fields ) {
    this.fields = fields;
  }

  public void addField( SAPField field ) {
    this.fields.add( field );
  }

}
