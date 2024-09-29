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


package org.pentaho.di.trans.steps.xmloutput;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.zip.ZipOutputStream;

import javax.xml.stream.XMLStreamWriter;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 22-jan-2005
 */
public class XMLOutputData extends BaseStepData implements StepDataInterface {
  public int splitnr;

  public Object[] headerrow;

  public int[] fieldnrs;

  public NumberFormat nf;

  public DecimalFormat df;

  public RowMetaInterface formatRowMeta;

  public RowMetaInterface outputRowMeta;

  public DecimalFormatSymbols dfs;

  public SimpleDateFormat daf;

  public DateFormatSymbols dafs;

  public ZipOutputStream zip;

  public XMLStreamWriter writer;

  public DecimalFormat defaultDecimalFormat;

  public DecimalFormatSymbols defaultDecimalFormatSymbols;

  public SimpleDateFormat defaultDateFormat;

  public DateFormatSymbols defaultDateFormatSymbols;

  public boolean OpenedNewFile;

  /**
   *
   */
  public XMLOutputData() {
    super();

    nf = NumberFormat.getInstance();
    df = (DecimalFormat) nf;
    dfs = new DecimalFormatSymbols();

    defaultDecimalFormat = (DecimalFormat) NumberFormat.getInstance();
    defaultDecimalFormatSymbols = new DecimalFormatSymbols();

    daf = new SimpleDateFormat();
    dafs = new DateFormatSymbols();

    defaultDateFormat = new SimpleDateFormat();
    defaultDateFormatSymbols = new DateFormatSymbols();

    OpenedNewFile = false;

  }

}
