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

import org.junit.Test;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.w3c.dom.Document;

import static junit.framework.Assert.assertEquals;

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
}
