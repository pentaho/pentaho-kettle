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

package org.pentaho.di.trans.steps.userdefinedjavaclass;

import org.pentaho.di.trans.step.StepMeta;

public class StepDefinition implements Cloneable {
  public String tag;
  public String stepName;
  public StepMeta stepMeta;
  public String description;

  public StepDefinition() {
    this.tag = "";
    this.stepName = "";
    this.stepMeta = null;
    this.description = "";
  }

  public StepDefinition( String tag, String stepName, StepMeta stepMeta, String description ) {
    this.tag = tag;
    this.stepName = stepName;
    this.stepMeta = stepMeta;
    this.description = description;
  }

  public Object clone() throws CloneNotSupportedException {
    StepDefinition retval;
    retval = (StepDefinition) super.clone();
    if ( stepMeta != null ) {
      retval.stepMeta = (StepMeta) stepMeta.clone();
    }
    return retval;
  }
}
