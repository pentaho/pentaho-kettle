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
