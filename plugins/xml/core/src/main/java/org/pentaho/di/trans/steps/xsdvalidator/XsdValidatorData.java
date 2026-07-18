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



package org.pentaho.di.trans.steps.xsdvalidator;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 14-08-2007
 * 
 */
public class XsdValidatorData extends BaseStepData implements StepDataInterface {
  public int xmlindex;
  public int xsdindex;
  public RowMetaInterface outputRowMeta;

  public XsdValidatorData() {
    super();
    xmlindex = -1;
    xsdindex = -1;

  }

}
