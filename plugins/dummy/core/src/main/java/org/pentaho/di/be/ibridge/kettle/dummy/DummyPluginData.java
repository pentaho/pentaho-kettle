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



package org.pentaho.di.be.ibridge.kettle.dummy;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * 
 * 
 * @author Matt
 * @since  24-mrt-2005
 */
public class DummyPluginData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;

  public DummyPluginData() {
    super();
  }
}
