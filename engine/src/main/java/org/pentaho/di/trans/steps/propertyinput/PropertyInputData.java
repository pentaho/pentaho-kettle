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


package org.pentaho.di.trans.steps.propertyinput;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;

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

/**
 * @author Samatar Hassan
 * @since 24-Mars-2008
 */
@SuppressWarnings( { "java:S1104", "java:S116" } ) // Public fields and naming convention are intentional for data classes in this framework
public class PropertyInputData extends BaseStepData {
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

  // Are we handling Property files (true) or INI files (false)?
  public boolean propfiles;

  // Properties files
  public Properties pro;
  public Iterator<Object> propIt;

  // INI files
  public INIConfiguration iniConf;
  public SubnodeConfiguration iniSection;
  public Iterator<String> iniIt;
  public Iterator<String> iniSectionIt;

  public String realEncoding;
  public String realSection;
  public String currentSection;

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
    filenr = 0;

    fr = null;
    is = null;
    rw = null;
    totalpreviousfields = 0;
    indexOfFilenameField = -1;
    readrow = null;

    pro = null;
    propIt = null;
    iniSection = null;
    currentSection = null;
    iniConf = null;
    iniSectionIt = null;
    realEncoding = null;
    realSection = null;
    propfiles = true;
    iniIt = null;
  }
}
