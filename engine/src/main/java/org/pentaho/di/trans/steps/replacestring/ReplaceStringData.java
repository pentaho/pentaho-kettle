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


package org.pentaho.di.trans.steps.replacestring;

import java.util.regex.Pattern;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar Hassan
 * @since 28 September 2008
 */
public class ReplaceStringData extends BaseStepData implements StepDataInterface {

  public int[] inStreamNrs;

  public String[] outStreamNrs;

  public boolean[] useRegEx;

  public String[] replaceString;

  public String[] replaceByString;

  public boolean[] setEmptyString;

  public int[] replaceFieldIndex;

  public boolean[] wholeWord;

  public boolean[] caseSensitive;

  public boolean[] isUnicode;

  public String realChangeField;

  public String[] valueChange;

  public String finalvalueChange;

  public RowMetaInterface outputRowMeta;

  public int inputFieldsNr;

  public Pattern[] patterns;

  public int numFields;

  /**
   * Default constructor.
   */
  public ReplaceStringData() {
    super();
    realChangeField = null;
    valueChange = null;
    finalvalueChange = null;
    inputFieldsNr = 0;
  }
}
