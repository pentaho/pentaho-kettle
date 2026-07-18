/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.trans.steps.webserviceavailable;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 03-02-2010
 *
 */
public class WebServiceAvailableData extends BaseStepData implements StepDataInterface {
  public int indexOfURL;
  public int connectTimeOut;
  public int readTimeOut;

  public RowMetaInterface previousRowMeta;
  public RowMetaInterface outputRowMeta;
  public int NrPrevFields;

  public WebServiceAvailableData() {
    super();
    indexOfURL = -1;
    connectTimeOut = 0;
    readTimeOut = 0;
  }

}
