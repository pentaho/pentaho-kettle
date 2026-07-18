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



package org.pentaho.di.trans.step;

public class StepMetaDataCombi {
  public StepMeta stepMeta;
  public String stepname;
  public int copy;

  public StepInterface step;
  public StepMetaInterface meta;
  public StepDataInterface data;

  public String toString() {
    return step.toString();
  }
}
