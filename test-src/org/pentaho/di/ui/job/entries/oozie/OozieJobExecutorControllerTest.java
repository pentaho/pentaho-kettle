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

package org.pentaho.di.ui.job.entries.oozie;

import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.WorkflowJob;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.entries.oozie.OozieJobExecutorConfig;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * User: RFellows
 * Date: 6/4/12
 */
public class OozieJobExecutorControllerTest {

  OozieJobExecutorConfig jobConfig = null;

  @BeforeClass
  public static void init() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void before() {
    jobConfig = new OozieJobExecutorConfig();

    jobConfig.setOozieWorkflow("hdfs://localhost:9000/user/" + System.getProperty("user.name") + "/examples/apps/map-reduce");

  }

  @Ignore
  @Test
  public void testOozieClient() throws Exception {
    OozieClient oozieClient = new OozieClient("http://localhost:11000/oozie");

    Properties config = oozieClient.createConfiguration();

    // get the workflow?
    // parse for props
    // set props values
    config.setProperty("nameNode", "hdfs://localhost:9000");
    config.setProperty("jobTracker", "localhost:9001");
    config.setProperty("queueName", "default");
    config.setProperty("examplesRoot", "examples");

//    config.setProperty(OozieClient.APP_PATH, jobConfig.getOozieWorkflow());
    config.setProperty(OozieClient.APP_PATH, "${nameNode}/user/${user.name}/${examplesRoot}/apps/map-reduce");

    config.setProperty("outputDir", "map-reduce");

    // run the job
    String jobId = oozieClient.run(config);
    System.out.println("Started the workflow --> " + jobId);

    // wait for it to finish
    while(oozieClient.getJobInfo(jobId).getStatus() == WorkflowJob.Status.RUNNING) {
      System.out.println("Still running...");
      Thread.sleep(10 * 1000);
    }

    // log results
    oozieClient.getJobLog(jobId, null, null, System.out);

    assertEquals("Job was not successful", WorkflowJob.Status.SUCCEEDED, oozieClient.getJobInfo(jobId).getStatus());

  }
}
