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

package org.pentaho.di.trans.steps.stringoperations;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Apply certain operations too string.
 *
 * @author Samatar Hassan
 * @since 02 April 2009
 */
public class StringOperationsData extends BaseStepData implements StepDataInterface {

  public int[] inStreamNrs; // string infields

  public String[] outStreamNrs;

  /** Runtime trim operators */
  public String[] trimOperators;

  /** Runtime trim operators */
  public String[] lowerUpperOperators;

  public String[] padType;

  public String[] padChar;

  public int[] padLen;

  public String[] initCap;

  public String[] maskHTML;

  public String[] digits;

  public String[] removeSpecialCharacters;

  public RowMetaInterface outputRowMeta;

  public int inputFieldsNr;

  public int nrFieldsInStream;

  /**
   * Default constructor.
   */
  public StringOperationsData() {
    super();
    this.inputFieldsNr = 0;
    this.nrFieldsInStream = 0;
  }
}
