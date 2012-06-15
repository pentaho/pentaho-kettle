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
import org.apache.oozie.client.WorkflowAction;
import org.apache.oozie.client.WorkflowJob;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.w3c.dom.Document;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * User: RFellows
 * Date: 6/5/12
 */
public class OozieJobExecutorJobEntryTest {

  @Test
  public void testLoadXml() throws Exception {

    OozieJobExecutorJobEntry jobEntry = new OozieJobExecutorJobEntry();
    OozieJobExecutorConfig jobConfig = new OozieJobExecutorConfig();

    jobConfig.setOozieWorkflow("hdfs://localhost:9000/user/test-user/oozie/workflow.xml");
    jobConfig.setOozieWorkflowConfig("file:///User/test-user/oozie/job.properties");
    jobConfig.setOozieUrl("http://localhost:11000/oozie");

    jobEntry.setJobConfig(jobConfig);

    JobEntryCopy jec = new JobEntryCopy(jobEntry);
    jec.setLocation(0, 0);
    String xml = jec.getXML();

    Document d = XMLHandler.loadXMLString(xml);

    OozieJobExecutorJobEntry jobEntry2 = new OozieJobExecutorJobEntry();
    jobEntry2.loadXML(d.getDocumentElement(), null, null, null);

    OozieJobExecutorConfig jobConfig2 = jobEntry2.getJobConfig();
    assertEquals(jobConfig.getOozieWorkflow(), jobConfig2.getOozieWorkflow());
    assertEquals(jobConfig.getOozieWorkflowConfig(), jobConfig2.getOozieWorkflowConfig());
    assertEquals(jobConfig.getOozieUrl(), jobConfig2.getOozieUrl());
  }

  @Test
  public void testGetValidationWarnings_emptyConfig() throws Exception {
    OozieJobExecutorConfig config = new OozieJobExecutorConfig();

    OozieJobExecutorJobEntry je = new OozieJobExecutorJobEntry();
    List<String> warnings = je.getValidationWarnings(config);

    assertEquals(3, warnings.size());
  }

  @Test
  public void testGetValidationWarnings_invalidOozieUrl() throws Exception {
    OozieJobExecutorConfig config = new OozieJobExecutorConfig();

    OozieJobExecutorJobEntry je = new OozieJobExecutorJobEntry();

    config.setOozieUrl("bad url");
    config.setOozieWorkflowConfig("file:///test/job.properties");
    config.setJobEntryName("name");

    List<String> warnings = je.getValidationWarnings(config);

    assertEquals(1, warnings.size());
    assertEquals(BaseMessages.getString(OozieJobExecutorJobEntry.class, "ValidationMessages.Invalid.Oozie.URL"), warnings.get(0));
  }

  @Test
  public void testGetValidationWarnings_incompatibleVersions() throws Exception {
    OozieJobExecutorConfig config = new OozieJobExecutorConfig();

    OozieClient client = getBadConfigTestOozieClient();
    OozieJobExecutorJobEntry je = new TestOozieJobExecutorJobEntry(client); // just use this to force an error condition

    config.setOozieUrl("http://localhost/oozie");
    config.setOozieWorkflowConfig("file:///test/job.properties");
    config.setJobEntryName("name");

    List<String> warnings = je.getValidationWarnings(config);

    assertEquals(1, warnings.size());
    assertEquals(BaseMessages.getString(OozieJobExecutorJobEntry.class, "ValidationMessages.Incompatible.Oozie.Versions", client.getClientBuildVersion()), warnings.get(0));
  }

  @Test
  public void testGetValidationWarnings_everythingIsGood() throws Exception {
    OozieJobExecutorJobEntry je = new TestOozieJobExecutorJobEntry(getSucceedingTestOozieClient());

    OozieJobExecutorConfig config = new OozieJobExecutorConfig();
    config.setOozieUrl("http://localhost:11000/oozie");             // don't worry if it isn't running, we fake out our test connection to it anyway
    config.setOozieWorkflowConfig("file:///test/job.properties");
    config.setJobEntryName("name");

    List<String> warnings = je.getValidationWarnings(config);

    assertEquals(0, warnings.size());
  }

  @Test
  public void execute_blocking() throws KettleException {
    final long waitTime = 200;

    OozieJobExecutorConfig config = new OozieJobExecutorConfig();
    config.setOozieUrl("http://localhost:11000/oozie");             // don't worry if it isn't running, we fake out our test connection to it anyway
    config.setOozieWorkflowConfig("test-src/job.properties");
    config.setJobEntryName("name");
    config.setBlockingPollingInterval(""+waitTime);

    TestOozieJobExecutorJobEntry je = new TestOozieJobExecutorJobEntry(getSucceedingTestOozieClient());

    je.setParentJob(new Job("test", null, null));
    je.setJobConfig(config);

    Result result = new Result();
    long start = System.currentTimeMillis();
    je.execute(result, 0);
    long end = System.currentTimeMillis();
    assertTrue("Total runtime should be >= the wait time if we are blocking", (end - start) >= waitTime);

    Assert.assertEquals(0, result.getNrErrors());
    assertTrue(result.getResult());
  }


  @Test
  public void execute_blocking_FAIL() throws KettleException {
    final long waitTime = 1000;

    OozieJobExecutorConfig config = new OozieJobExecutorConfig();
    config.setOozieUrl("http://localhost:11000/oozie");             // don't worry if it isn't running, we fake out our test connection to it anyway
    config.setOozieWorkflowConfig("test-src/job.properties");
    config.setJobEntryName("name");
    config.setBlockingPollingInterval(""+waitTime);

    TestOozieJobExecutorJobEntry je = new TestOozieJobExecutorJobEntry(getFailingTestOozieClient());

    je.setParentJob(new Job("test", null, null));
    je.setJobConfig(config);

    Result result = new Result();
    long start = System.currentTimeMillis();
    je.execute(result, 0);
    long end = System.currentTimeMillis();
    assertTrue("Total runtime should be >= the wait time if we are blocking", (end - start) >= waitTime);

    Assert.assertEquals(1, result.getNrErrors());
    assertFalse(result.getResult());
  }

  private TestOozieClient getFailingTestOozieClient() {
    // return status = FAILED
    // isValidWS = true
    // isValidProtocol = true
    return new TestOozieClient(WorkflowJob.Status.FAILED, true, true);
  }
  private TestOozieClient getSucceedingTestOozieClient() {
    // return status = SUCCEEDED
    // isValidWS = true
    // isValidProtocol = true
    return new TestOozieClient(WorkflowJob.Status.SUCCEEDED, true, true);
  }
  private TestOozieClient getBadConfigTestOozieClient() {
    // return status = SUCCEEDED
    // isValidWS = false
    // isValidProtocol = false
    return new TestOozieClient(WorkflowJob.Status.SUCCEEDED, false, false);
  }


  ////////////////////////////////////////////////////////////
  // Stub classes to help in testing.
  // Oozie doesn't provide much in the way of interfaces,
  // so this is our best solution
  ////////////////////////////////////////////////////////////
  class TestOozieClient extends OozieClient {
    TestWorkflowJob wj = null;
    WorkflowJob.Status returnStatus = null;
    boolean isValidWS = true;
    boolean isValidProtocol = true;

    TestOozieClient(WorkflowJob.Status returnStatus, boolean isValidWS, boolean isValidProtocol) {
      this.returnStatus = returnStatus;
      this.isValidWS = isValidWS;
      this.isValidProtocol = isValidProtocol;
    }

    @Override
    public synchronized void validateWSVersion() throws OozieClientException {
      if (isValidWS) return;
      throw new OozieClientException("Error", new Exception("Not compatible"));
    }
    @Override
    public String getProtocolUrl() throws OozieClientException {
      if(isValidProtocol) return "HTTP";
      return null;
    }

    @Override
    public String run(Properties conf) throws OozieClientException {
      wj = new TestWorkflowJob(WorkflowJob.Status.RUNNING);
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          // block for a second
          try {
            Thread.sleep(1000);
            wj.setStatus(returnStatus);
          } catch (InterruptedException e) {

          }
        }
      });
      t.start();
      return "test-job-id";
    }
    @Override
    public String getJobLog(String jobId) throws OozieClientException {
      return "nothing to log";
    }
    @Override
    public WorkflowJob getJobInfo(String jobId) throws OozieClientException {
      return wj;
    }
  }

  class TestOozieJobExecutorJobEntry extends OozieJobExecutorJobEntry {
    private OozieClient client = null;

    TestOozieJobExecutorJobEntry(OozieClient client) {
      this.client = client;
    }

    @Override
    public OozieClient getOozieClient() {
      return client;
    }

    @Override
    public OozieClient getOozieClient(OozieJobExecutorConfig config) {
      return client;
    }
  }

  class TestWorkflowJob implements WorkflowJob {
    private Status status;

    TestWorkflowJob(Status status) {
      this.status = status;
    }

    public void setStatus(Status status) {
      this.status = status;
    }

    @Override
    public String getAppPath() {return null;}
    @Override
    public String getAppName() {return null;}
    @Override
    public String getId() {return null;}
    @Override
    public String getConf() {return null;}
    @Override
    public Status getStatus() {return status;}
    @Override
    public Date getLastModifiedTime() {return null;}
    @Override
    public Date getCreatedTime() {return null;}
    @Override
    public Date getStartTime() {return null;}
    @Override
    public Date getEndTime() {return null;}
    @Override
    public String getUser() {return null;}
    @Override
    public String getGroup() {return null;}
    @Override
    public int getRun() {return 0;}
    @Override
    public String getConsoleUrl() {return null;}
    @Override
    public String getParentId() {return null;}
    @Override
    public List<WorkflowAction> getActions() {return null;}
  }

}
