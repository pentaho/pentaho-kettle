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

package org.pentaho.di.trans.steps.salesforceinsert;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.steps.salesforce.SalesforceStepData;

import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;

/*
 * @author Samatar
 * @since 10-06-2007
 */
public class SalesforceInsertData extends SalesforceStepData {
  public RowMetaInterface inputRowMeta;
  public RowMetaInterface outputRowMeta;

  public int nrfields;
  public int[] fieldnrs;

  public SaveResult[] saveResult;

  public SObject[] sfBuffer;
  public Object[][] outputBuffer;
  public int iBufferPos;

  public String realSalesforceFieldName;

  public SalesforceInsertData() {
    super();

    nrfields = 0;

    saveResult = null;
    realSalesforceFieldName = null;
    iBufferPos = 0;
  }
}
