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


package org.pentaho.di.trans.steps.excelinput;

import java.util.Date;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.playlist.FilePlayList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandler;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class ExcelInputData extends BaseStepData implements StepDataInterface {
  /**
   * The previous row in case we want to repeat values...
   */
  public Object[] previousRow;

  /**
   * The maximum length of all filenames...
   */
  public int maxfilelength;

  /**
   * The maximum length of all sheets...
   */
  public int maxsheetlength;

  /**
   * The Excel files to read
   */
  public FileInputList files;

  /**
   * The file number that's being handled...
   */
  public int filenr;

  public String filename;

  public FileObject file;

  /**
   * The openFile that's being processed...
   */
  public KWorkbook workbook;

  /**
   * The sheet number that's being processed...
   */
  public int sheetnr;

  /**
   * The sheet that's being processed...
   */
  public KSheet sheet;

  /**
   * The row where we left off the previous time...
   */
  public int rownr;

  /**
   * The column where we left off previous time...
   */
  public int colnr;

  /**
   * The error handler when processing of a row fails.
   */
  public FileErrorHandler errorHandler;

  public FilePlayList filePlayList;

  public RowMetaInterface outputRowMeta;

  ValueMetaInterface valueMetaString;
  ValueMetaInterface valueMetaNumber;
  ValueMetaInterface valueMetaDate;
  ValueMetaInterface valueMetaBoolean;

  public RowMetaInterface conversionRowMeta;

  public String[] sheetNames;
  public int[] startColumn;
  public int[] startRow;

  public int defaultStartColumn;
  public int defaultStartRow;

  public String shortFilename;
  public String path;
  public String extension;
  public boolean hidden;
  public Date lastModificationDateTime;
  public String uriName;
  public String rootUriName;
  public long size;

  public ExcelInputData() {
    super();
    workbook = null;
    filenr = 0;
    sheetnr = 0;
    rownr = -1;
    colnr = -1;

    valueMetaString = new ValueMetaString( "v" );
    valueMetaNumber = new ValueMetaNumber( "v" );
    valueMetaDate = new ValueMetaDate( "v" );
    valueMetaBoolean = new ValueMetaBoolean( "v" );
  }
}
