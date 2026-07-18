/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.trans.step;

import org.pentaho.di.trans.Trans;

/**
 * This listener informs the audience of the various states of a step.
 *
 * @author matt
 *
 */
public interface StepListener {

  /**
   * This method is called when a step goes from being idle to being active.
   *
   * @param trans
   * @param stepMeta
   * @param step
   */
  public void stepActive( Trans trans, StepMeta stepMeta, StepInterface step );

  /**
   * This method is called when a step completes all work and is finished.
   *
   * @param trans
   * @param stepMeta
   * @param step
   */
  public void stepFinished( Trans trans, StepMeta stepMeta, StepInterface step );
}
