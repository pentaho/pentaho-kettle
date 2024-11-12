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


package org.pentaho.di.trans.steps.userdefinedjavaclass;

import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.trans.step.StepMeta;

public class TargetStepDefinition extends StepDefinition {
  @Injection( name = "TARGET_TAG", group = "TARGET_STEPS" )
  public String tag = super.tag;
  @Injection( name = "TARGET_STEP_NAME", group = "TARGET_STEPS" )
  public String stepName = super.stepName;
  public StepMeta stepMeta = super.stepMeta;
  @Injection( name = "TARGET_DESCRIPTION", group = "TARGET_STEPS" )
  public String description = super.description;

  public TargetStepDefinition() {
    super();
  }

  public TargetStepDefinition( String tag, String stepName, StepMeta stepMeta, String description ) {
    super( tag, stepName, stepMeta, description );
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
