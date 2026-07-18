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



package org.pentaho.di.job;

import org.pentaho.di.core.Result;
import org.pentaho.di.job.entry.JobEntryCopy;

public class JobExecutionExtension {

  public Job job;
  public Result result;
  public JobEntryCopy jobEntryCopy;
  public boolean executeEntry;

  public JobExecutionExtension( Job job, Result result, JobEntryCopy jobEntryCopy, boolean executeEntry ) {
    super();
    this.job = job;
    this.result = result;
    this.jobEntryCopy = jobEntryCopy;
    this.executeEntry = executeEntry;
  }
}
