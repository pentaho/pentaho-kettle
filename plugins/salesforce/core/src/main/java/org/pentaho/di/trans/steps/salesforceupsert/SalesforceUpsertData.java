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

package org.pentaho.di.trans.steps.salesforceupsert;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.steps.salesforce.SalesforceStepData;

import com.sforce.soap.partner.UpsertResult;
import com.sforce.soap.partner.sobject.SObject;

import java.util.HashMap;
import java.util.Map;

/*
 * @author Samatar
 * @since 10-06-2007
 */
public class SalesforceUpsertData extends SalesforceStepData {
  public RowMetaInterface inputRowMeta;
  public RowMetaInterface outputRowMeta;

  public int nrfields;
  public int[] fieldnrs;

  public UpsertResult[] upsertResult;

  public SObject[] sfBuffer;
  public Object[][] outputBuffer;
  public int iBufferPos;

  public String realSalesforceFieldName;
  public Map<String, String> dataTypeMap;
  public boolean mapData;

  public SalesforceUpsertData() {
    super();

    nrfields = 0;

    upsertResult = null;
    realSalesforceFieldName = null;
    iBufferPos = 0;
    dataTypeMap = new HashMap<>();
  }
}
