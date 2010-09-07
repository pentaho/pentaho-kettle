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
  
  public XLSCell(Cell cell) {
    this.cell = cell;
  }
  
  public KCellType getType() {
    CellType type = cell.getType();
    if (type.equals(CellType.BOOLEAN)) {
      return KCellType.BOOLEAN;
    } else if (type.equals(CellType.BOOLEAN_FORMULA)) {
      return KCellType.BOOLEAN_FORMULA;
    } else if (type.equals(CellType.DATE)) {
      return KCellType.DATE;
    } else if (type.equals(CellType.DATE_FORMULA)) {
      return KCellType.DATE_FORMULA;
    } else if (type.equals(CellType.LABEL)) {
      return KCellType.LABEL;
    } else if (type.equals(CellType.STRING_FORMULA)) {
      return KCellType.STRING_FORMULA;
    } else if (type.equals(CellType.EMPTY)) {
      return KCellType.EMPTY;
    } else if (type.equals(CellType.NUMBER)) {
      return KCellType.NUMBER;
    } else if (type.equals(CellType.NUMBER_FORMULA)) {
      return KCellType.NUMBER_FORMULA;
    }
    return null;
  }
  
  public Object getValue() {
    switch(getType()) {
    case BOOLEAN_FORMULA: 
    case BOOLEAN: return Boolean.valueOf( ((BooleanCell)cell).getValue() );
    case DATE_FORMULA:
    case DATE: return ((DateCell)cell).getDate();
    case NUMBER_FORMULA:
    case NUMBER: return Double.valueOf( ((NumberCell)cell).getValue() );
    case STRING_FORMULA:
    case LABEL: return ((LabelCell)cell).getString();
    case EMPTY:
    default: return null;
    }
  }
  
  public String getContents() {
    return cell.getContents();
  }
  
  public int getRow() {
    return cell.getRow();
  }
}
