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

package org.pentaho.di.trans.steps.exceloutput;

import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Map;

import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 7-sep-2006
 */
public class ExcelOutputData extends BaseStepData implements StepDataInterface {
  public int splitnr;

  public RowMetaInterface previousMeta;
  public RowMetaInterface outputMeta;
  public int[] fieldnrs;

  public WritableWorkbook workbook;

  public WritableSheet sheet;

  public int templateColumns; // initial number of columns in the template

  public WritableCellFormat writableCellFormat;

  public Map<String, WritableCellFormat> formats;

  public int positionX;

  public int positionY;

  public WritableFont headerFont;

  public OutputStream outputStream;

  public FileObject file;

  public boolean oneFileOpened;

  public String realSheetname;

  int[] fieldsWidth;

  public boolean headerWrote;

  public int Headerrowheight;

  public String realHeaderImage;

  public Colour rowFontBackgoundColour;

  public WritableCellFormat headerCellFormat;

  public WritableImage headerImage;

  public double headerImageHeight;
  public double headerImageWidth;
  public WritableFont writableFont;

  public String realFilename;

  public WorkbookSettings ws;

  public ExcelOutputData() {
    super();

    formats = new Hashtable<String, WritableCellFormat>();
    oneFileOpened = false;
    file = null;
    realSheetname = null;
    headerWrote = false;
  }

}
