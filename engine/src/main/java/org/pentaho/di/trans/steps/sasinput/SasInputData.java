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


package org.pentaho.di.trans.steps.sasinput;

import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Provides data for the XBaseInput step.
 *
 * @author Matt
 * @since 09-OCT-2011
 * @version 4.3
 */
public class SasInputData extends BaseStepData implements StepDataInterface {
  public SasInputHelper helper;
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface fileLayout;
  public List<Integer> fieldIndexes;

  public SasInputData() {
    super();

    helper = null;
  }

}
