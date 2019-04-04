/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
