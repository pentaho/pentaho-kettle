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

package org.pentaho.di.trans.steps.jsonoutput;

import java.io.Writer;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 22-jan-2005
 */
public class JsonOutputData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface inputRowMeta;
  public RowMetaInterface outputRowMeta;
  public int inputRowMetaSize;

  public int nrFields;

  public int[] fieldIndexes;
  public JSONObject jg;
  public JSONArray ja;
  public int nrRow;
  public boolean rowsAreSafe;
  public NumberFormat nf;
  public DecimalFormat df;
  public DecimalFormatSymbols dfs;

  public SimpleDateFormat daf;
  public DateFormatSymbols dafs;

  public DecimalFormat defaultDecimalFormat;
  public DecimalFormatSymbols defaultDecimalFormatSymbols;

  public SimpleDateFormat defaultDateFormat;
  public DateFormatSymbols defaultDateFormatSymbols;

  public boolean outputValue;
  public boolean writeToFile;

  public String realBlocName;
  public int splitnr;
  public Writer writer;
  public int nrRowsInBloc;

  /**
   *
   */
  public JsonOutputData() {
    super();
    this.ja = new JSONArray();
    this.nrRow = 0;
    this.outputValue = false;
    this.writeToFile = false;
    this.writer = null;
    this.nrRowsInBloc = 0;
  }

}
