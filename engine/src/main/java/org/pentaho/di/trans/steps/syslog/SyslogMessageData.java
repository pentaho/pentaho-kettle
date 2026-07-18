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



package org.pentaho.di.trans.steps.syslog;

import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.productivity.java.syslog4j.SyslogIF;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class SyslogMessageData extends BaseStepData implements StepDataInterface {
  public int indexOfMessageFieldName;
  public SyslogIF syslog;
  public String datePattern;

  public SyslogMessageData() {
    super();
    indexOfMessageFieldName = -1;
    syslog = null;
    datePattern = null;
  }

}
