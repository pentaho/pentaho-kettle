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
