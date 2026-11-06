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


package org.pentaho.di.trans.steps.userdefinedjavaclass;

import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.trans.step.StepMeta;

public class InfoStepDefinition extends StepDefinition {
  @Injection( name = "INFO_TAG", group = "INFO_STEPS" )
  public String tag = super.tag;
  @Injection( name = "INFO_STEP_NAME", group = "INFO_STEPS" )
  public String stepName = super.stepName;
  public StepMeta stepMeta = super.stepMeta;
  @Injection( name = "INFO_DESCRIPTION", group = "INFO_STEPS" )
  public String description = super.description;

  public InfoStepDefinition() {
    super();
  }

  public InfoStepDefinition( String tag, String stepName, StepMeta stepMeta, String description ) {
    super( tag, stepName, stepMeta, description );
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
