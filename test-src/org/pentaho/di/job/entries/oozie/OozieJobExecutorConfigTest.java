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
import org.pentaho.di.job.entries.helper.PersistentPropertyChangeListener;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * User: RFellows
 * Date: 6/5/12
 */
public class OozieJobExecutorConfigTest {

  @Test
  public void testAddPropertyChangeListener() throws Exception {
    OozieJobExecutorConfig config = new OozieJobExecutorConfig();

    PersistentPropertyChangeListener l = new PersistentPropertyChangeListener();

    // make sure it is capturing property change events
    config.addPropertyChangeListener(l);
    config.setOozieWorkflow("workflow1.xml");
    assertEquals(1, l.getReceivedEvents().size());
    assertEquals(config.getOozieWorkflow(), l.getReceivedEvents().get(0).getNewValue());

    // remove the listener & verify that it isn't receiving events anymore
    config.removePropertyChangeListener(l);
    config.setOozieWorkflow("workflow2.xml");
    assertEquals(1, l.getReceivedEvents().size()); // still 1, from the previous call
  }

  @Test
  public void testAddPropertyChangeListener_propertyName() throws Exception {
    OozieJobExecutorConfig config = new OozieJobExecutorConfig();

    PersistentPropertyChangeListener l = new PersistentPropertyChangeListener();

    // dummy property name, should not indicate any captured prop change
    config.addPropertyChangeListener("dummy", l);
    config.setOozieWorkflowConfig("job0.properties");
    assertEquals(0, l.getReceivedEvents().size());
    config.removePropertyChangeListener("dummy", l);

    // make sure it is capturing property change events
    config.addPropertyChangeListener(OozieJobExecutorConfig.OOZIE_WORKFLOW_CONFIG, l);
    config.setOozieWorkflowConfig("job1.properties");
    assertEquals(1, l.getReceivedEvents().size());
    assertEquals(config.getOozieWorkflowConfig(), l.getReceivedEvents().get(0).getNewValue());

    // remove the listener & verify that it isn't receiving events anymore
    config.removePropertyChangeListener(OozieJobExecutorConfig.OOZIE_WORKFLOW_CONFIG, l);
    config.setOozieWorkflowConfig("job2.properties");
    assertEquals(1, l.getReceivedEvents().size()); // still 1, from the previous call
  }

  @Test
  public void testGettersAndSetters() throws Exception {
    OozieJobExecutorConfig config = new OozieJobExecutorConfig();

    // everything should be null initially
    assertNull(config.getOozieUrl());
    assertNull(config.getOozieWorkflow());
    assertNull(config.getOozieWorkflowConfig());

    config.setOozieUrl("http://localhost:11000");
    assertEquals("http://localhost:11000", config.getOozieUrl());

    config.setOozieWorkflow("hdfs://localhsot:9000/user/test-user/workflowFolder");
    assertEquals("hdfs://localhsot:9000/user/test-user/workflowFolder", config.getOozieWorkflow());

    config.setOozieWorkflowConfig("job.properties");
    assertEquals("job.properties", config.getOozieWorkflowConfig());

  }
}
