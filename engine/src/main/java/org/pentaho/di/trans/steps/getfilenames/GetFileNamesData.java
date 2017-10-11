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

package org.pentaho.di.trans.steps.getfilenames;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.playlist.FilePlayList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandler;

/**
 * @author Matt
 * @since 22-jan-2005
 */
public class GetFileNamesData extends BaseStepData implements StepDataInterface {
  public List<String> lineBuffer;

  public Object[] previous_row;

  public int nr_repeats;

  public int nrLinesOnPage;

  public NumberFormat nf;

  public DecimalFormat df;

  public DecimalFormatSymbols dfs;

  public SimpleDateFormat daf;

  public RowMetaInterface outputRowMeta;

  public DateFormatSymbols dafs;

  public FileInputList files;

  public boolean isLastFile;

  public String filename;

  public int filenr;

  public int filessize;

  public FileInputStream fr;

  public ZipInputStream zi;

  public InputStreamReader isr;

  public boolean doneReading;

  public int headerLinesRead;

  public int footerLinesRead;

  public int pageLinesRead;

  public boolean doneWithHeader;

  public FileErrorHandler dataErrorLineHandler;

  public FilePlayList filePlayList;

  public FileObject file;

  public long rownr;

  public int totalpreviousfields;

  public int indexOfFilenameField;

  public int indexOfWildcardField;
  public int indexOfExcludeWildcardField;

  public RowMetaInterface inputRowMeta;

  public Object[] readrow;

  public int nrStepFields;

  public GetFileNamesData() {
    super();

    lineBuffer = new ArrayList<String>();
    nf = NumberFormat.getInstance();
    df = (DecimalFormat) nf;
    dfs = new DecimalFormatSymbols();
    daf = new SimpleDateFormat();
    dafs = new DateFormatSymbols();

    nr_repeats = 0;
    previous_row = null;
    filenr = 0;
    filessize = 0;

    nrLinesOnPage = 0;

    fr = null;
    zi = null;
    file = null;
    totalpreviousfields = 0;
    indexOfFilenameField = -1;
    indexOfWildcardField = -1;
    readrow = null;
    nrStepFields = 0;
    indexOfExcludeWildcardField = -1;
  }

  public void setDateFormatLenient( boolean lenient ) {
    daf.setLenient( lenient );
  }

}
