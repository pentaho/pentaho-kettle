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

package org.pentaho.di.trans.steps.excelinput.jxl;

import jxl.BooleanCell;
import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.LabelCell;
import jxl.NumberCell;

import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KCellType;

public class XLSCell implements KCell {

  private Cell cell;

  public XLSCell( Cell cell ) {
    this.cell = cell;
  }

  public KCellType getType() {
    CellType type = cell.getType();
    if ( type.equals( CellType.BOOLEAN ) ) {
      return KCellType.BOOLEAN;
    } else if ( type.equals( CellType.BOOLEAN_FORMULA ) ) {
      return KCellType.BOOLEAN_FORMULA;
    } else if ( type.equals( CellType.DATE ) ) {
      return KCellType.DATE;
    } else if ( type.equals( CellType.DATE_FORMULA ) ) {
      return KCellType.DATE_FORMULA;
    } else if ( type.equals( CellType.LABEL ) ) {
      return KCellType.LABEL;
    } else if ( type.equals( CellType.STRING_FORMULA ) ) {
      return KCellType.STRING_FORMULA;
    } else if ( type.equals( CellType.EMPTY ) ) {
      return KCellType.EMPTY;
    } else if ( type.equals( CellType.NUMBER ) ) {
      return KCellType.NUMBER;
    } else if ( type.equals( CellType.NUMBER_FORMULA ) ) {
      return KCellType.NUMBER_FORMULA;
    }
    return null;
  }

  public Object getValue() {
    switch ( getType() ) {
      case BOOLEAN_FORMULA:
      case BOOLEAN:
        return Boolean.valueOf( ( (BooleanCell) cell ).getValue() );
      case DATE_FORMULA:
      case DATE:
        return ( (DateCell) cell ).getDate();
      case NUMBER_FORMULA:
      case NUMBER:
        return Double.valueOf( ( (NumberCell) cell ).getValue() );
      case STRING_FORMULA:
      case LABEL:
        return ( (LabelCell) cell ).getString();
      case EMPTY:
      default:
        return null;
    }
  }

  public String getContents() {
    return cell.getContents();
  }

  public int getRow() {
    return cell.getRow();
  }
}
