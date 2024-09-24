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
