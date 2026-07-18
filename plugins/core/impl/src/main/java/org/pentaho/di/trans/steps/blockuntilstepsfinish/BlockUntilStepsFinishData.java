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



package org.pentaho.di.trans.steps.blockuntilstepsfinish;

import java.util.concurrent.ConcurrentHashMap;

import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;

/**
 * @author Samatar
 * @since 16-06-2008
 *
 */
public class BlockUntilStepsFinishData extends BaseStepData implements StepDataInterface {

  boolean continueLoop;
  public ConcurrentHashMap<Integer, StepInterface> stepInterfaces;

  public BlockUntilStepsFinishData() {
    super();
    continueLoop = true;
  }

}
