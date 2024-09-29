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
