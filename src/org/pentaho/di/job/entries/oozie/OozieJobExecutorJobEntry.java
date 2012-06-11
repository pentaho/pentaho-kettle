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

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;
import org.apache.oozie.client.WorkflowJob;
import org.apache.oozie.util.PropertiesUtils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.AbstractJobEntry;
import org.pentaho.di.job.JobEntryUtils;
import org.pentaho.di.job.entry.JobEntryInterface;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

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
  protected Runnable getExecutionRunnable(final Result jobResult) {
    return new Runnable() {
      @Override
      public void run() {

        OozieClient oozieClient = new OozieClient(jobConfig.getOozieUrl());
        try {
          oozieClient.validateWSVersion();
        } catch (OozieClientException e) {
          setJobResultFailed(jobResult);
          logError(BaseMessages.getString(OozieJobExecutorJobEntry.class, "Oozie.JobExecutor.ERROR.InvalidWSVersion", oozieClient.getClientBuildVersion()), e);
        }

        String jobId = null;

        try {
          InputStreamReader reader = new InputStreamReader(KettleVFS.getInputStream(jobConfig.getOozieWorkflowConfig()));
          Properties jobProps = PropertiesUtils.readProperties(reader, 1024);

          // make sure we supply the current user name
          if(!jobProps.containsKey("user.name")) {
            jobProps.setProperty("user.name", variables.environmentSubstitute("${user.name}"));
          }

          jobId = oozieClient.run(jobProps);
          if (JobEntryUtils.asBoolean(getJobConfig().getBlockingExecution(), variables)) {
            while(oozieClient.getJobInfo(jobId).getStatus().equals(WorkflowJob.Status.RUNNING)) {
              System.out.println("Still running...");
              Thread.sleep(10 * 1000);
            }
            String logDetail = oozieClient.getJobLog(jobId);
            if(oozieClient.getJobInfo(jobId).getStatus().equals(WorkflowJob.Status.SUCCEEDED)) {
              jobResult.setResult(true);
              logDetailed(logDetail);
            } else {
              // it failed
              setJobResultFailed(jobResult);
              logError(logDetail);
            }
          }

        } catch (KettleFileException e) {
          e.printStackTrace();
          setJobResultFailed(jobResult);
          logError(BaseMessages.getString(OozieJobExecutorJobEntry.class, "Oozie.JobExecutor.ERROR.File.Resolution"), e);
        } catch (IOException e) {
          e.printStackTrace();
          setJobResultFailed(jobResult);
          logError(BaseMessages.getString(OozieJobExecutorJobEntry.class, "Oozie.JobExecutor.ERROR.Props.Loading"), e);
        } catch (OozieClientException e) {
          logError(BaseMessages.getString(OozieJobExecutorJobEntry.class, "Oozie.JobExecutor.ERROR.OozieClient"), e);
        } catch (InterruptedException e) {
          e.printStackTrace();
          setJobResultFailed(jobResult);
          logError(BaseMessages.getString(OozieJobExecutorJobEntry.class, "Oozie.JobExecutor.ERROR.Threading"), e);
        }

      }
    };
  }

  @Override
  protected void handleUncaughtThreadException(Thread t, Throwable e, Result jobResult) {
    logError(BaseMessages.getString(OozieJobExecutorJobEntry.class, "Oozie.JobExecutor.ERROR.Generic"), e);
    setJobResultFailed(jobResult);
  }
}
