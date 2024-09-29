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


package org.pentaho.di.trans.steps.ifnull;

import java.util.HashMap;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 16-06-2008
 *
 */
public class IfNullData extends BaseStepData implements StepDataInterface {

  public RowMetaInterface outputRowMeta;
  public RowMetaInterface convertRowMeta;

  public int[] fieldnrs;
  public int fieldnr;
  public String realReplaceByValue;
  public String realconversionMask;
  public boolean realSetEmptyString;

  public HashMap<String, Integer> ListTypes;
  public String[] defaultValues;
  public String[] defaultMasks;
  public boolean[] setEmptyString;

  public IfNullData() {
    super();
    ListTypes = new HashMap<String, Integer>();
  }

}
