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



package org.pentaho.di.trans.steps.uniquerowsbyhashset;

import java.util.HashSet;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class UniqueRowsByHashSetData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface inputRowMeta;
  public boolean storeValues;
  public int[] fieldnrs;
  public String compareFields;
  public String realErrorDescription;
  boolean sendDuplicateRows;

  public HashSet<RowKey> seen = new HashSet<RowKey>();

  public UniqueRowsByHashSetData() {
    super();
  }

  public void clearHashSet() {
    sendDuplicateRows = false;
    compareFields = null;
    realErrorDescription = null;
  }
}
