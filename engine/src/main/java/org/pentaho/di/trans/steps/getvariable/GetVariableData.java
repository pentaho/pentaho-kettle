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


package org.pentaho.di.trans.steps.getvariable;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class GetVariableData extends BaseStepData implements StepDataInterface {
  public boolean readsRows;
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface inputRowMeta;
  public Object[] extraData;
  public RowMetaInterface conversionMeta;

  public GetVariableData() {
    super();
  }
}
