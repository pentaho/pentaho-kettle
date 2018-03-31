/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
