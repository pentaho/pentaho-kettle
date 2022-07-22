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

/**
 * Author = Shailesh Ahuja
 */

package org.pentaho.di.trans.steps.excelinput.staxpoi;

import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KCellType;

public class StaxPoiCell implements KCell {

  private KCellType type;
  private Object value;
  private int row;

  public StaxPoiCell( String value, int row ) {
    this.value = value;
    this.row = row;
    type = KCellType.STRING_FORMULA;
  }

  public StaxPoiCell( Object value, KCellType type, int row ) {
    this.value = value;
    this.type = type;
    this.row = row;
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
    return value == null ? null : value.toString();
  }

  @Override
  public int getRow() {
    return row;
  }

}
