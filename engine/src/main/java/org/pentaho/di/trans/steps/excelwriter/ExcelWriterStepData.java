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

package org.pentaho.di.trans.steps.excelwriter;

import org.apache.commons.vfs2.FileObject;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class ExcelWriterStepData extends BaseStepData implements StepDataInterface {

  public RowMetaInterface outputRowMeta;
  public int splitnr;
  public int datalines;
  public String realSheetname;
  public String realTemplateSheetName;
  public boolean firstFileOpened;
  public FileObject file;
  public int posX;
  public int posY;
  public Sheet sheet;
  public Workbook wb;
  public int[] fieldnrs;
  public RowMetaInterface inputRowMeta;
  public int[] commentfieldnrs;
  public int[] commentauthorfieldnrs;
  public int startingCol = 0;
  public int startingRow = 0;
  public boolean shiftExistingCells = false;
  public boolean createNewFile = false;
  public boolean createNewSheet = false;
  public String realTemplateFileName;
  public String realStartingCell;
  public String realPassword;
  public String realProtectedBy;
  public int[] linkfieldnrs;
  private CellStyle[] cellStyleCache;
  private CellStyle[] cellLinkStyleCache;

  public ExcelWriterStepData() {
    super();
  }

  public void clearStyleCache( int nrFields ) {
    cellStyleCache = new CellStyle[nrFields];
    cellLinkStyleCache = new CellStyle[nrFields];
  }

  public void cacheStyle( int fieldNr, CellStyle style ) {
    cellStyleCache[fieldNr] = style;
  }

  public void cacheLinkStyle( int fieldNr, CellStyle style ) {
    cellLinkStyleCache[fieldNr] = style;
  }

  public CellStyle getCachedStyle( int fieldNr ) {
    return cellStyleCache[fieldNr];
  }

  public CellStyle getCachedLinkStyle( int fieldNr ) {
    return cellLinkStyleCache[fieldNr];
  }

}
