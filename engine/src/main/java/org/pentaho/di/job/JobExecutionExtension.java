/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
