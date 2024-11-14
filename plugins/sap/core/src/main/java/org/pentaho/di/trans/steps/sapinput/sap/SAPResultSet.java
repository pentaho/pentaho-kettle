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


package org.pentaho.di.trans.steps.sapinput.sap;

import java.util.Collection;
import java.util.Vector;

public class SAPResultSet {

  private Collection<SAPRow> rows = new Vector<SAPRow>();

  @Override
  public String toString() {
    return "SAPResultSet [rows=" + rows + "]";
  }

  public Collection<SAPRow> getRows() {
    return rows;
  }

  public void setRows( Collection<SAPRow> rows ) {
    this.rows = rows;
  }

  public void addRow( SAPRow row ) {
    this.rows.add( row );
  }
}
