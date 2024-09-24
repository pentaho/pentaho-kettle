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

package org.pentaho.di.trans.steps.getslavesequence;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class GetSlaveSequenceData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;
  public SlaveServer slaveServer;
  public long value;
  public long startValue;
  public long increment;
  public String sequenceName;

  public GetSlaveSequenceData() {
    super();
  }
}
