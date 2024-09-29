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


package org.pentaho.di.trans.steps;

import static org.mockito.Mockito.mock;

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.BaseStepData;

public class PDI_11948_StepsTestsParent<T extends BaseStep, E extends BaseStepData> {
  protected T stepMock;
  protected Trans transMock;
  protected E stepDataMock;

  public void init() throws Exception {
    transMock = mock( Trans.class );
  }
}
