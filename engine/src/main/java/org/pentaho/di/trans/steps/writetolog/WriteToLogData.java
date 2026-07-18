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



package org.pentaho.di.trans.steps.writetolog;

import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 16-06-2008
 *
 */
public class WriteToLogData extends BaseStepData implements StepDataInterface {

  public int[] fieldnrs;
  public int fieldnr;
  public LogLevel loglevel;
  public String logmessage;

  public WriteToLogData() {
    super();

  }

}
