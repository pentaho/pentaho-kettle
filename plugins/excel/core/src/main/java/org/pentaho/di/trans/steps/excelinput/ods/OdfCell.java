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

package org.pentaho.di.trans.steps.excelinput.ods;

import java.sql.Date;
import java.util.TimeZone;

import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KCellType;
import org.pentaho.di.core.util.Utils;

public class OdfCell implements KCell {

  public static String TYPE_BOOLEAN = "boolean";
  public static String TYPE_CURRENCY = "currency";
  public static String TYPE_DATE = "date";
  public static String TYPE_FLOAT = "float";
  public static String TYPE_PERCENTAGE = "percentage";
  public static String TYPE_STRING = "string";
  public static String TYPE_TIME = "time";

  private OdfTableCell cell;

  public OdfCell( OdfTableCell cell ) {
    this.cell = cell;
  }

  public KCellType getType() {

    String type = cell.getValueType();
    if ( Utils.isEmpty( type ) ) {
      return KCellType.EMPTY;
    }

    if ( TYPE_BOOLEAN.equals( type ) ) {
      if ( Utils.isEmpty( cell.getFormula() ) ) {
        return KCellType.BOOLEAN;
      } else {
        return KCellType.BOOLEAN_FORMULA;
      }
    } else if ( TYPE_CURRENCY.equals( type ) || TYPE_FLOAT.equals( type ) || TYPE_PERCENTAGE.equals( type ) ) {
      if ( Utils.isEmpty( cell.getFormula() ) ) {
        return KCellType.NUMBER;
      } else {
        return KCellType.NUMBER_FORMULA;
      }
    } else if ( TYPE_DATE.equals( type ) || TYPE_TIME.equals( type ) ) { // Validate!
      if ( Utils.isEmpty( cell.getFormula() ) ) {
        return KCellType.DATE;
      } else {
        return KCellType.DATE_FORMULA;
      }
    } else if ( TYPE_STRING.equals( type ) ) {
      if ( Utils.isEmpty( cell.getFormula() ) ) {
        return KCellType.LABEL;
      } else {
        return KCellType.STRING_FORMULA;
      }
    }

    // TODO: check what to do with a formula! Is the result cached or not with this format?

    return null; // unknown type!
  }

  public Object getValue() {
    try {
      switch ( getType() ) {
        case BOOLEAN_FORMULA:
        case BOOLEAN:
          return Boolean.valueOf( cell.getBooleanValue() );
        case DATE_FORMULA:
        case DATE:
          // Timezone conversion needed since POI doesn't support this apparently
          //
          long time = cell.getDateValue().getTime().getTime();
          long tzOffset = TimeZone.getDefault().getOffset( time );

          return new Date( time + tzOffset );
        case NUMBER_FORMULA:
        case NUMBER:
          return Double.valueOf( cell.getDoubleValue() );
        case STRING_FORMULA:
        case LABEL:
          return cell.getStringValue();
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
    return cell.getRowIndex();
  }
}
