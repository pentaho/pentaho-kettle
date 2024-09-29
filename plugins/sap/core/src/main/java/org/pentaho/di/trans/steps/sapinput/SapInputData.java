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


package org.pentaho.di.trans.steps.sapinput;

import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.sapinput.sap.SAPConnection;
import org.pentaho.di.trans.steps.sapinput.sap.SAPField;

/**
 * @author Matt
 * @since 20-jan-2005
 */
public class SapInputData extends BaseStepData implements StepDataInterface {

  public SAPConnection sapConnection;

  public List<Integer> parameterIndexes;

  public List<SAPField> output;

  public RowMetaInterface outputRowMeta;

  public SapInputData() {
    super();
  }

}
