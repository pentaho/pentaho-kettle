/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.excelinput.poi;

import java.sql.Date;
import java.util.TimeZone;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KCellType;

public class PoiCell implements KCell {

  private Cell cell;

  public PoiCell( Cell cell ) {
    this.cell = cell;
  }

  public KCellType getType() {
    CellType type = cell.getCellType();
    if ( type == CellType.BOOLEAN ) {
      return KCellType.BOOLEAN;
    } else if ( type == CellType.NUMERIC ) {
      if ( DateUtil.isCellDateFormatted( cell ) ) {
        return KCellType.DATE;
      } else {
        return KCellType.NUMBER;
      }
    } else if ( type == CellType.STRING ) {
      return KCellType.LABEL;
    } else if ( type == CellType.BLANK || type == CellType.ERROR ) {
      return KCellType.EMPTY;
    } else if ( type == CellType.FORMULA ) {
      CellType cachedFormulaResultType = cell.getCachedFormulaResultType();
      if ( CellType.ERROR.equals( cachedFormulaResultType ) ) {
        return KCellType.EMPTY;
      } else if ( CellType.BOOLEAN.equals( cachedFormulaResultType ) ) {
        return KCellType.BOOLEAN_FORMULA;
      } else if ( CellType.STRING.equals( cachedFormulaResultType ) ) {
        return KCellType.STRING_FORMULA;
      } else if ( CellType.NUMERIC.equals( cachedFormulaResultType ) ) {
        if ( DateUtil.isCellDateFormatted( cell ) ) {
          return KCellType.DATE_FORMULA;
        } else {
          return KCellType.NUMBER_FORMULA;
        }
      }
    }
    return null;
  }

  public Object getValue() {
    try {
      switch ( getType() ) {
        case BOOLEAN_FORMULA:
        case BOOLEAN:
          return Boolean.valueOf( cell.getBooleanCellValue() );
        case DATE_FORMULA:
        case DATE:
          // Timezone conversion needed since POI doesn't support this apparently
          //
          long time = cell.getDateCellValue().getTime();
          long tzOffset = TimeZone.getDefault().getOffset( time );

          return new Date( time + tzOffset );
        case NUMBER_FORMULA:
        case NUMBER:
          return Double.valueOf( cell.getNumericCellValue() );
        case STRING_FORMULA:
        case LABEL:
          return cell.getStringCellValue();
        case EMPTY:
        default:
          return null;
      }
    } catch ( Exception e ) {
      throw new RuntimeException( "Unable to get value of cell ("
        + cell.getColumnIndex() + ", " + cell.getRowIndex() + ")", e );
    }
  }

  public String getContents() {
    try {
      Object value = getValue();
      if ( value == null ) {
        return null;
      }
      return value.toString();
    } catch ( Exception e ) {
      throw new RuntimeException( "Unable to get string content of cell ("
        + cell.getColumnIndex() + ", " + cell.getRowIndex() + ")", e );
    }
  }

  public int getRow() {
    Row row = cell.getRow();
    return row.getRowNum();
  }
}
