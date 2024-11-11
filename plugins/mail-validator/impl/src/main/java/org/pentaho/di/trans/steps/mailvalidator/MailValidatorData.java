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


package org.pentaho.di.trans.steps.mailvalidator;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class MailValidatorData extends BaseStepData implements StepDataInterface {
  public int indexOfeMailField;
  public String realResultFieldName;
  public String realResultErrorsFieldName;
  public int indexOfdefaultSMTPField;
  public int timeout;
  public String realemailSender;
  public String realdefaultSMTPServer;
  public String msgValidMail;
  public String msgNotValidMail;
  public RowMetaInterface previousRowMeta;
  public RowMetaInterface outputRowMeta;
  public int NrPrevFields;

  public MailValidatorData() {
    super();
    indexOfeMailField = -1;
    indexOfdefaultSMTPField = -1;
    timeout = 0;
    realemailSender = null;
    realdefaultSMTPServer = null;
    msgNotValidMail = null;
    msgValidMail = null;
  }

}
