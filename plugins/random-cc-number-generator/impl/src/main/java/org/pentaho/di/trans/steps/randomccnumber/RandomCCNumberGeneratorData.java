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


package org.pentaho.di.trans.steps.randomccnumber;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Generate random credit card number.
 *
 * @author Samatar
 * @since 01-4-2010
 */
public class RandomCCNumberGeneratorData extends BaseStepData implements StepDataInterface {

  public int[] cardTypes;
  public int[] cardSize;
  public int[] cardLen;
  public RowMetaInterface outputRowMeta;

  public boolean addCardTypeOutput;
  public boolean addCardLengthOutput;

  public RandomCCNumberGeneratorData() {
    super();
    addCardTypeOutput = false;
    addCardLengthOutput = false;
  }
}
