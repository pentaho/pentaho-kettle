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

package org.pentaho.di.trans.steps.creditcardvalidator;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class CreditCardValidatorData extends BaseStepData implements StepDataInterface {
  public int indexOfField;
  public String realResultFieldname;
  public String realCardTypeFieldname;
  public String realNotValidMsgFieldname;
  public RowMetaInterface outputRowMeta;
  public int NrPrevFields;
  public RowMetaInterface previousRowMeta;

  public CreditCardValidatorData() {
    super();
    indexOfField = -1;
    realResultFieldname = null;
    realCardTypeFieldname = null;
    realNotValidMsgFieldname = null;
  }

}
