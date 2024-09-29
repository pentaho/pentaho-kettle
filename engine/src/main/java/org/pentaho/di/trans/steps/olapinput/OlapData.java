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


package org.pentaho.di.trans.steps.olapinput;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Paul Stoellberger
 * @since 11-MAR-2010
 */
public class OlapData extends BaseStepData implements StepDataInterface {
  public OlapHelper olapHelper;
  public RowMetaInterface outputRowMeta;
  public OlapInputMeta meta;
  public int rowNumber;

  public OlapData() {
    super();
  }
}
