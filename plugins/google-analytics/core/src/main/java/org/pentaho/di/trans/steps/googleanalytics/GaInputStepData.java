/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.googleanalytics;

import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.GaData;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


public class GaInputStepData extends BaseStepData implements StepDataInterface {

  public RowMetaInterface outputRowMeta;

  // meta info for a string conversion
  public ValueMetaInterface[] conversionMeta;

  // holds currently processed feed
  public Analytics.Data.Ga.Get query;
  public GaData feed;
  public int entryIndex;

  public GaInputStepData() {
    super();
  }
}
