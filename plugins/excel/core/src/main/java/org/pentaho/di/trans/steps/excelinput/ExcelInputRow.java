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


package org.pentaho.di.trans.steps.excelinput;

import org.pentaho.di.core.spreadsheet.KCell;

/**
 * Represent 1 row in a an Excel sheet.
 */
public class ExcelInputRow {

  public final String sheetName;
  public final int rownr;
  public final KCell[] cells;

  public ExcelInputRow( String sheetName, int rownr, KCell[] cells ) {
    this.sheetName = sheetName;
    this.rownr = rownr;
    this.cells = cells;
  }
}
