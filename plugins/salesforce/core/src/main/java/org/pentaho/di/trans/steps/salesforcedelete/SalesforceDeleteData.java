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


package org.pentaho.di.trans.steps.salesforcedelete;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.steps.salesforce.SalesforceStepData;

import com.sforce.soap.partner.DeleteResult;

/*
 * @author Samatar
 * @since 10-06-2007
 */
public class SalesforceDeleteData extends SalesforceStepData {
  public RowMetaInterface inputRowMeta;
  public RowMetaInterface outputRowMeta;

  public DeleteResult[] deleteResult;

  public String[] deleteId;
  public Object[][] outputBuffer;
  public int iBufferPos;

  public int indexOfKeyField;

  public SalesforceDeleteData() {
    super();

    deleteResult = null;
    iBufferPos = 0;
    indexOfKeyField = -1;
  }
}
