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


package org.pentaho.di.trans.steps.rowgenerator;

import java.util.Date;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class RowGeneratorData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;
  public Object[] outputRowData;

  public long rowLimit;
  public long rowsWritten;
  public Date rowDate;
  public Date prevDate;
  public long delay;

  public RowGeneratorData() {
    super();
  }
}
