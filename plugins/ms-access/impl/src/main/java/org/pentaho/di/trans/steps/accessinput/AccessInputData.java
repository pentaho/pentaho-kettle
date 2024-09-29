/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.steps.accessinput;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

/**
 * @author Samatar Hassan
 * @since 24-May-2005
 */
public class AccessInputData extends BaseStepData implements StepDataInterface {
  public String thisline;
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface convertRowMeta;
  public Object[] previousRow;
  public int nr_repeats;

  public SimpleDateFormat daf;

  public FileInputList files;
  public boolean last_file;
  public FileObject file;
  public int filenr;

  public FileInputStream fr;
  public BufferedInputStream is;
  public long rownr;
  public Database d;
  public Table t;
  public Map<String, Object> rw;
  public RowMetaInterface inputRowMeta;
  public int totalpreviousfields;
  public int indexOfFilenameField;
  public Object[] readrow;

  public String filename;
  public String shortFilename;
  public String path;
  public String extension;
  public boolean hidden;
  public Date lastModificationDateTime;
  public String uriName;
  public String rootUriName;
  public long size;
  public String tableName;
  public boolean isTableSystem;

  public int nrFields;

  /**
  *
  */
  public AccessInputData() {
    super();
    isTableSystem = false;
    tableName = null;
    previousRow = null;
    thisline = null;
    daf = new SimpleDateFormat();
    nr_repeats = 0;
    previousRow = null;
    filenr = 0;
    fr = null;
    is = null;
    d = null;
    t = null;
    rw = null;
    rownr = 1L;
    totalpreviousfields = 0;
    indexOfFilenameField = -1;
    readrow = null;
  }
}
