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
