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

package org.pentaho.di.trans.steps.xmlinputsax;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.zip.ZipInputStream;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.w3c.dom.Node;

/**
 * @author Matt
 * @since 22-jan-2005
 */
public class XMLInputSaxData extends BaseStepData implements StepDataInterface {
  public String thisline, nextline, lastline;

  public Object[] previousRow;

  public int nr_repeats;

  public NumberFormat nf;

  public DecimalFormat df;

  public DecimalFormatSymbols dfs;

  public SimpleDateFormat daf;

  public DateFormatSymbols dafs;

  public RowMetaInterface outputRowMeta;

  public String[] files;

  public boolean last_file;

  public String filename;

  public int filenr;

  public FileInputStream fr;

  public ZipInputStream zi;

  public BufferedInputStream is;

  public XMLInputSaxDataRetriever document;

  public Node section;

  public String itemElement;

  public int itemCount;

  public int itemPosition;

  public long rownr;

  public RowMetaInterface convertRowMeta;

  public XMLInputSaxData() {
    super();

    thisline = null;
    nextline = null;
    nf = NumberFormat.getInstance();
    df = (DecimalFormat) nf;
    dfs = new DecimalFormatSymbols();
    daf = new SimpleDateFormat();
    dafs = new DateFormatSymbols();

    nr_repeats = 0;
    previousRow = null;
    filenr = 0;

    fr = null;
    zi = null;
    is = null;
  }

}
