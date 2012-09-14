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

import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;
import org.apache.oozie.client.WorkflowJob;
import org.apache.oozie.util.PropertiesUtils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.AbstractJobEntry;
import org.pentaho.di.job.JobEntryMode;
import org.pentaho.di.job.JobEntryUtils;
import org.pentaho.di.job.PropertyEntry;
import org.pentaho.di.job.entry.JobEntryInterface;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * User: RFellows
 * Date: 6/4/12
 */
@JobEntry(id = "OozieJobExecutor",
    name = "Oozie.JobExecutor.PluginName",
    description = "Oozie.JobExecutor.PluginDescription",
    categoryDescription = "BigData.Category.Description",
    image = "oozie-job-executor.png",
    i18nPackageName = "org.pentaho.di.job.entries.oozie",
    version = "1"
)
public class OozieJobExecutorJobEntry extends AbstractJobEntry<OozieJobExecutorConfig> implements Cloneable, JobEntryInterface {

  public static final String HTTP_ERROR_CODE_404 = "HTTP error code: 404";
  public static final String USER_NAME = "user.name";
  private OozieClient oozieClient = null;

  @Override
  protected OozieJobExecutorConfig createJobConfig() {
    return new OozieJobExecutorConfig();
  }

  /**
   * Validates the current configuration of the step.
   * <p/>
   * <strong>To be valid in Quick Setup mode:</strong>
   * <ul>
   * <li>Name is required</li>
   * <li>Oozie URL is required and must be a valid oozie location</li>
   * <li>Workflow Properties file path is required and must be a valid job properties file</li>
   * </ul>
   * @param config Configuration to validate
   * @return
   */
  @Override
  public List<String> getValidationWarnings(OozieJobExecutorConfig config) {
    List<String> messages = new ArrayList<String>();

    // verify there is a job name
    if(StringUtil.isEmpty(config.getJobEntryName())) {
      messages.add(BaseMessages.getString(OozieJobExecutorJobEntry.class, "ValidationMessages.Missing.JobName"));
    }

    if(StringUtil.isEmpty(config.getOozieUrl())) {
      messages.add(BaseMessages.getString(OozieJobExecutorJobEntry.class, "ValidationMessages.Missing.Oozie.URL"));
    } else {
      try {
        // oozie url is valid and client & ws versions are compatible
        oozieClient = getOozieClient(config);
        oozieClient.getProtocolUrl();
        oozieClient.validateWSVersion();
      } catch (OozieClientException e) {
        if(e.getErrorCode().equals(HTTP_ERROR_CODE_404) ||
            (e.getCause() != null && (
                e.getCause() instanceof MalformedURLException ||
                e.getCause() instanceof ConnectException)
           )) {
          messages.add(BaseMessages.getString(OozieJobExecutorJobEntry.class, "ValidationMessages.Invalid.Oozie.URL"));
        } else {
          messages.add(BaseMessages.getString(OozieJobExecutorJobEntry.class, "ValidationMessages.Incompatible.Oozie.Versions", oozieClient.getClientBuildVersion()));
        }
      }
    }

    // path to oozie workflow properties file
    if(config.getModeAsEnum() == JobEntryMode.QUICK_SETUP && StringUtil.isEmpty(config.getOozieWorkflowConfig())) {
      messages.add(BaseMessages.getString(OozieJobExecutorJobEntry.class, "ValidationMessages.Missing.Workflow.Properties"));
    } else {
      // make sure the path to the properties file is valid
      try {
        Properties props = getProperties(config);

        // make sure it has at minimum a workflow definition (need app path)
        if(props.containsKey(OozieClient.APP_PATH) ||
           props.containsKey(OozieClient.COORDINATOR_APP_PATH) ||
           props.containsKey(OozieClient.BUNDLE_APP_PATH)) {
        } else {
          messages.add(BaseMessages.getString(OozieJobExecutorJobEntry.class,
              "ValidationMessages.App.Path.Property.Missing"));
        }

      } catch (KettleFileException e) {
        // can't find the file specified as the Workflow Properties definition
        messages.add(BaseMessages.getString(OozieJobExecutorJobEntry.class,
            "ValidationMessages.Workflow.Properties.FileNotFound"));
      } catch (IOException e) {
        // something went wrong with the reading of the properties file
        messages.add(BaseMessages.getString(OozieJobExecutorJobEntry.class,
            "ValidationMessages.Workflow.Properties.ReadError"));
      }
    }

    boolean pollingIntervalValid = false;
    try {
      long pollingInterval = JobEntryUtils.asLong(config.getBlockingPollingInterval(), variables);
      pollingIntervalValid = pollingInterval > 0;
    } catch (Exception ex) {
      // ignore, polling interval is not valid
    }
    if (!pollingIntervalValid) {
      messages.add(BaseMessages.getString(OozieJobExecutorJobEntry.class,
          "ValidationMessages.Invalid.PollingInterval"));
    } 

    return messages;
  }

  public Properties getPropertiesFromFile(OozieJobExecutorConfig config) throws IOException, KettleFileException {
    return getPropertiesFromFile(config, getVariableSpace());
  }
  public static Properties getPropertiesFromFile(OozieJobExecutorConfig config, VariableSpace variableSpace) throws IOException, KettleFileException {
    InputStreamReader reader = new InputStreamReader(KettleVFS.getInputStream(variableSpace.environmentSubstitute(config.getOozieWorkflowConfig())));

    Properties jobProps = new Properties(); //PropertiesUtils.readProperties(reader, 100*1024);
    jobProps.load(reader);
    return jobProps;
  }

  public Properties getProperties(OozieJobExecutorConfig config) throws KettleFileException, IOException {
    return getProperties(config, getVariableSpace());
  }

  public static Properties getProperties(OozieJobExecutorConfig config, VariableSpace variableSpace) throws KettleFileException, IOException {
    Properties jobProps = null;
    if(config.getModeAsEnum() == JobEntryMode.ADVANCED_LIST
        && config.getWorkflowProperties() != null) {
      jobProps = new Properties();
      for (PropertyEntry propertyEntry : config.getWorkflowProperties()) {
        if(propertyEntry.getKey() != null) {
          String value = propertyEntry.getValue() == null ? "" : propertyEntry.getValue();
          jobProps.setProperty(propertyEntry.getKey(), variableSpace.environmentSubstitute(value));
        }
      }
    } else {
      jobProps = getPropertiesFromFile(config, variableSpace);
    }
    return jobProps;
  }

  @Override
  protected Runnable getExecutionRunnable(final Result jobResult) {
    return new Runnable() {
      @Override
      public void run() {

        OozieClient oozieClient = getOozieClient();
        try {
          oozieClient.validateWSVersion();
        } catch (OozieClientException e) {

          setJobResultFailed(jobResult);

          if(e.getErrorCode().equals(HTTP_ERROR_CODE_404) ||
              (e.getCause() != null && (
                  e.getCause() instanceof MalformedURLException ||
                      e.getCause() instanceof ConnectException)
              )) {
            logError(BaseMessages.getString(OozieJobExecutorJobEntry.class, "ValidationMessages.Invalid.Oozie.URL"), e);
          } else {
            logError(BaseMessages.getString(OozieJobExecutorJobEntry.class, "ValidationMessages.Incompatible.Oozie.Versions", oozieClient.getClientBuildVersion()), e);
          }
        }

        String jobId = null;

        try {
          Properties jobProps = getProperties(jobConfig);

          // make sure we supply the current user name
          if(!jobProps.containsKey(USER_NAME)) {
            jobProps.setProperty(USER_NAME, getVariableSpace().environmentSubstitute("${" + USER_NAME + "}"));
          }

          jobId = oozieClient.run(jobProps);
          if (JobEntryUtils.asBoolean(getJobConfig().getBlockingExecution(), variables)) {
            while(oozieClient.getJobInfo(jobId).getStatus().equals(WorkflowJob.Status.RUNNING)) {
              // System.out.println("Still running " + jobId + "...");
              long interval = JobEntryUtils.asLong(jobConfig.getBlockingPollingInterval(), variables);
              Thread.sleep(interval);
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
          setJobResultFailed(jobResult);
          logError(BaseMessages.getString(OozieJobExecutorJobEntry.class, "Oozie.JobExecutor.ERROR.File.Resolution"), e);
        } catch (IOException e) {
          setJobResultFailed(jobResult);
          logError(BaseMessages.getString(OozieJobExecutorJobEntry.class, "Oozie.JobExecutor.ERROR.Props.Loading"), e);
        } catch (OozieClientException e) {
          setJobResultFailed(jobResult);
          logError(BaseMessages.getString(OozieJobExecutorJobEntry.class, "Oozie.JobExecutor.ERROR.OozieClient"), e);
        } catch (InterruptedException e) {
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


  public OozieClient getOozieClient() {
    return new OozieClient(getVariableSpace().environmentSubstitute(jobConfig.getOozieUrl()));
  }

  public OozieClient getOozieClient(OozieJobExecutorConfig config) {
    return new OozieClient(getVariableSpace().environmentSubstitute(config.getOozieUrl()));
  }

}
