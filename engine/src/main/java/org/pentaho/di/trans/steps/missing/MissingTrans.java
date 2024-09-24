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

package org.pentaho.di.trans.steps.missing;

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;

public class MissingTrans extends DummyTransMeta {

  private String stepName;
  private String missingPluginId;

  public MissingTrans( String stepName, String missingPluginId ) {
    this.stepName = stepName;
    this.missingPluginId = missingPluginId;
  }

  public String getStepName() {
    return stepName;
  }

  public void setStepName( String stepName ) {
    this.stepName = stepName;
  }

  public String getMissingPluginId() {
    return missingPluginId;
  }

  public void setMissingPluginId( String missingPluginId ) {
    this.missingPluginId = missingPluginId;
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
      Trans trans ) {
    return new MissingTransStep( stepMeta, stepDataInterface, cnr, tr, trans );
  }
}
