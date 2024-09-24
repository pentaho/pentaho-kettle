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

package org.pentaho.di.trans.steps.mappinginput;

import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class MappingInputData extends BaseStepData implements StepDataInterface {

  public boolean finished;
  public StepInterface[] sourceSteps;
  public boolean linked;
  public RowMetaInterface outputRowMeta;
  public List<MappingValueRename> valueRenames;
  public int[] fieldNrs;

  public MappingInputData() {
    super();
    linked = false;
  }

}
