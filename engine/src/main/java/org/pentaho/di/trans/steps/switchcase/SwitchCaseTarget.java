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

package org.pentaho.di.trans.steps.switchcase;

import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.trans.step.StepMeta;

/**
 * Utility class that contains the case value, the target step name and the resolved target step
 *
 * @author matt
 *
 */
public class SwitchCaseTarget implements Cloneable {
  /** The value to switch over */
  @Injection( name = "CASE_VALUE" )
  public String caseValue;

  /** The case target step name (only used during serialization) */
  @Injection( name = "CASE_TARGET_STEP_NAME" )
  public String caseTargetStepname;

  /** The case target step */
  public StepMeta caseTargetStep;

  public SwitchCaseTarget() {
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

}
