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



package org.pentaho.di.trans.steps.webservices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class WebServiceData extends BaseStepData implements StepDataInterface {
  public String realUrl;

  public RowMetaInterface outputRowMeta;

  public Map<String, Integer> indexMap;

  public List<Object[]> argumentRows;

  public WebServiceData() {
    argumentRows = new ArrayList<Object[]>();
  }

}
