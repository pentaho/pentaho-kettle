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

package org.pentaho.di.trans.steps.delay;

import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 27-06-2008
 *
 */
public class DelayData extends BaseStepData implements StepDataInterface {
  public int Multiple;
  public int timeout;

  public DelayData() {
    super();
    Multiple = 1000;
    timeout = 0;
  }

}
