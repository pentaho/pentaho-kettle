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


package org.pentaho.di.trans.steps.injector;

import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Data class to allow a java program to inject rows of data into a transformation. This step can be used as a starting
 * point in such a "headless" transformation.
 *
 * @since 22-jun-2006
 */
public class InjectorData extends BaseStepData implements StepDataInterface {
  /**
   * Default constructor.
   */
  public InjectorData() {
    super();
  }
}
