/*
 * ******************************************************************************
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */

package org.pentaho.di.job.entries.oozie;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.AbstractJobEntry;
import org.pentaho.di.job.entry.JobEntryInterface;

/**
 * User: RFellows
 * Date: 6/4/12
 */
@JobEntry(id = "OozieJobExecutor",
    name = "Oozie.JobExecutor.PluginName",
    description = "Oozie.JobExecutor.PluginDescription",
    categoryDescription = "BigData.Category.Description",
    image = "oozie_job_executor.jpg",
    i18nPackageName = "org.pentaho.di.job.entries.oozie",
    version = "1"
)
public class OozieJobExecutorJobEntry extends AbstractJobEntry<OozieJobExecutorConfig> implements Cloneable, JobEntryInterface {


  @Override
  protected OozieJobExecutorConfig createJobConfig() {
    return new OozieJobExecutorConfig();
  }

  @Override
  protected boolean isValid(OozieJobExecutorConfig config) {
    return true; // TODO, implement this
  }

  @Override
  protected Runnable getExecutionRunnable(Result jobResult) {
    return new Runnable() {
      @Override
      public void run() {
        // run the oozie job here
      }
    };
  }

  @Override
  protected void handleUncaughtThreadException(Thread t, Throwable e, Result jobResult) {
    // TODO, log the error
  }
}
