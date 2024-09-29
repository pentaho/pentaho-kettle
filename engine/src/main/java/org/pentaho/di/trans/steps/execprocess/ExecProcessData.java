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


package org.pentaho.di.trans.steps.execprocess;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 03-11-2008
 *
 */
public class ExecProcessData extends BaseStepData implements StepDataInterface {
  public int indexOfProcess;
  public int[] indexOfArguments;
  public RowMetaInterface previousRowMeta;
  public RowMetaInterface outputRowMeta;
  public int NrPrevFields;
  public Runtime runtime;

  public ExecProcessData() {
    super();
    indexOfProcess = -1;
  }

}
