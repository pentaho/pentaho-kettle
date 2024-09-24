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

package org.pentaho.di.trans.steps.salesforceinput;

import java.util.GregorianCalendar;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.steps.salesforce.SalesforceStepData;

/*
 * @author Samatar
 * @since 10-06-2007
 */
public class SalesforceInputData extends SalesforceStepData {
  public int nr_repeats;
  public long rownr;
  public Object[] previousRow;
  public RowMetaInterface inputRowMeta;
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface convertRowMeta;
  public int recordcount;
  public int nrfields;
  public boolean limitReached;
  public long limit;
  // available before we call query more if needed
  public int nrRecords;
  // We use this variable to query more
  // we initialize it each time we call query more
  public int recordIndex;
  public GregorianCalendar startCal;
  public GregorianCalendar endCal;
  public boolean finishedRecord;

  public SalesforceInputData() {
    super();

    nr_repeats = 0;
    nrfields = 0;
    recordcount = 0;
    limitReached = false;
    limit = 0;
    nrRecords = 0;
    recordIndex = 0;
    rownr = 0;

    startCal = null;
    endCal = null;
    finishedRecord = false;
  }
}
