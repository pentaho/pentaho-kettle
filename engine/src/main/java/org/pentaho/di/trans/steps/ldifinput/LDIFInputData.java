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

package org.pentaho.di.trans.steps.ldifinput;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipInputStream;

import netscape.ldap.LDAPAttribute;
import netscape.ldap.util.LDIF;
import netscape.ldap.util.LDIFRecord;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Samatar Hassan
 * @since 24-May-2005
 */
public class LDIFInputData extends BaseStepData implements StepDataInterface {
  public String thisline, nextline, lastline;
  // public Row previousRow;
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
  public ZipInputStream zi;
  public BufferedInputStream is;
  public Document document;
  public Node section;
  public String itemElement;
  public int itemCount;
  public int itemPosition;
  public long rownr;
  public RowMetaInterface outputRowMeta;
  public Object[] previousRow;
  public RowMetaInterface inputRowMeta;
  public RowMetaInterface convertRowMeta;
  public int nrInputFields;
  public LDAPAttribute[] attributes_LDIF;
  public LDIFRecord recordLDIF;
  public LDIF InputLDIF;
  public String multiValueSeparator;
  public int totalpreviousfields;
  public Object[] readrow;
  public int indexOfFilenameField;

  public String filename;
  public String shortFilename;
  public String path;
  public String extension;
  public boolean hidden;
  public Date lastModificationDateTime;
  public String uriName;
  public String rootUriName;
  public long size;

  public LDIFInputData() {
    super();
    nrInputFields = -1;
    thisline = null;
    nextline = null;
    nf = NumberFormat.getInstance();
    df = (DecimalFormat) nf;
    dfs = new DecimalFormatSymbols();
    daf = new SimpleDateFormat();
    dafs = new DateFormatSymbols();

    nr_repeats = 0;
    filenr = 0;

    fr = null;
    zi = null;
    is = null;
    InputLDIF = null;
    recordLDIF = null;
    multiValueSeparator = ",";
    totalpreviousfields = 0;
    readrow = null;
    indexOfFilenameField = -1;
  }
}
