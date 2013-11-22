/**
 * Author = Shailesh Ahuja
 */

package org.pentaho.di.trans.steps.excelinput.staxpoi;

import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KCellType;

public class StaxPoiCell implements KCell {

  private KCellType type;
  private String value;
  private int row;

  // only string type supported yet, everything else is cast to String
  public StaxPoiCell( String value, int row ) {
    this.value = value;
    this.row = row;
    type = KCellType.STRING_FORMULA;
  }

  @Override
  public KCellType getType() {
    return type;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public String getContents() {
    return value;
  }

  @Override
  public int getRow() {
    return row;
  }

}
