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

package org.pentaho.di.trans.step;

import org.pentaho.di.trans.TransMeta;

public interface StepMetaChangeListenerInterface {
  /**
   * This method is called when a step was changed
   *
   * @param transMeta
   *          TransMeta which include this steps
   *
   * @param oldMeta
   *          the previous meta, which changed
   *
   * @param newMeta
   *          the updated meta with new variables values
   */
  public void onStepChange( TransMeta transMeta, StepMeta oldMeta, StepMeta newMeta );

}
