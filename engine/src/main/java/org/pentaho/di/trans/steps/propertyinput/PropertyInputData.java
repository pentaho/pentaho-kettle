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

package org.pentaho.di.trans.steps.propertyinput;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.vfs2.FileObject;
import org.ini4j.Profile.Section;
import org.ini4j.Wini;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar Hassan
 * @since 24-Mars-2008
 */
public class PropertyInputData extends BaseStepData implements StepDataInterface {
  public String thisline;
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface convertRowMeta;
  public Object[] previousRow;
  public int nr_repeats;

  public NumberFormat nf;
  public DecimalFormat df;
  public DecimalFormatSymbols dfs;
  public SimpleDateFormat daf;
  public DateFormatSymbols dafs;

  public FileInputList files;
  public boolean last_file;
  public FileObject file;
  public int filenr;

  public FileInputStream fr;
  public BufferedInputStream is;
  public long rownr;
  public Map<String, Object> rw;
  public RowMetaInterface inputRowMeta;
  public int totalpreviousfields;
  public int indexOfFilenameField;
  public Object[] readrow;

  // Properties files
  public Properties pro;
  public Iterator<Object> it;

  // INI files
  public Section iniSection;
  public Wini wini;
  public Iterator<String> itSection;
  public String realEncoding;
  public String realSection;
  public Iterator<String> iniIt;

  public boolean propfiles;

  public String filename;
  public String shortFilename;
  public String path;
  public String extension;
  public boolean hidden;
  public Date lastModificationDateTime;
  public String uriName;
  public String rootUriName;
  public long size;

  public PropertyInputData() {
    super();
    previousRow = null;
    thisline = null;
    nf = NumberFormat.getInstance();
    df = (DecimalFormat) nf;
    dfs = new DecimalFormatSymbols();
    daf = new SimpleDateFormat();
    dafs = new DateFormatSymbols();

    nr_repeats = 0;
    previousRow = null;
    filenr = 0;

    fr = null;
    is = null;
    rw = null;
    totalpreviousfields = 0;
    indexOfFilenameField = -1;
    readrow = null;

    pro = null;
    it = null;
    iniSection = null;
    wini = null;
    itSection = null;
    realEncoding = null;
    realSection = null;
    propfiles = true;
    iniIt = null;
  }
}
