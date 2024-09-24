/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.mailinput;

import java.util.Iterator;

import javax.mail.Message;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.job.entries.getpop.MailConnection;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class MailInputData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface inputRowMeta;
  public int totalpreviousfields;

  public RowMetaInterface outputRowMeta;
  public MailConnection mailConn;
  public int messagesCount;
  public long rownr;
  public String folder;
  public String[] folders;
  public int folderenr;
  public boolean usePOP;
  public int indexOfFolderField;
  public Object[] readrow;
  public int rowlimit;
  public int nrFields;
  public Iterator<Message> folderIterator;

  public Integer start;
  public Integer end;

  public MailInputData() {
    super();
    mailConn = null;
    messagesCount = 0;
    folder = null;
    folderenr = 0;
    usePOP = true;
    indexOfFolderField = -1;
    readrow = null;
    totalpreviousfields = 0;
    rowlimit = 0;
  }

}
