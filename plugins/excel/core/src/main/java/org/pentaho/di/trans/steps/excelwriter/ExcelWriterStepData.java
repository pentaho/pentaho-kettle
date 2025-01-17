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


package org.pentaho.di.trans.steps.excelwriter;

import org.apache.commons.vfs2.FileObject;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import java.util.Optional;

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
  /** the inner template, if streaming */
  public Optional<Sheet> innerSheet = Optional.empty();
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
