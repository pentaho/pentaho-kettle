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

package org.pentaho.di.trans.steps.excelinput.jxl;

import jxl.Cell;
import jxl.Sheet;

import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KSheet;

public class XLSSheet implements KSheet {
  private Sheet sheet;

  public XLSSheet( Sheet sheet ) {
    this.sheet = sheet;
  }

  public String getName() {
    return sheet.getName();
  }

  public KCell[] getRow( int rownr ) {
    Cell[] cells = sheet.getRow( rownr );
    XLSCell[] xlsCells = new XLSCell[cells.length];
    for ( int i = 0; i < cells.length; i++ ) {
      if ( cells[i] != null ) {
        xlsCells[i] = new XLSCell( cells[i] );
      }
    }
    return xlsCells;
  }

  public int getRows() {
    return sheet.getRows();
  }

  public KCell getCell( int colnr, int rownr ) {
    Cell cell = sheet.getCell( colnr, rownr );
    if ( cell == null ) {
      return null;
    }
    return new XLSCell( cell );
  }
}
