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

package org.pentaho.di.job;

import org.junit.Test;
import org.pentaho.di.job.entries.helper.PersistentPropertyChangeListener;

import static junit.framework.Assert.*;

/**
 * User: RFellows
 * Date: 6/5/12
 */
public class BlockableJobConfigTest {

  @Test
  public void testAddPropertyChangeListener() throws Exception {
    BlockableJobConfig config = new BlockableJobConfig();

    PersistentPropertyChangeListener l = new PersistentPropertyChangeListener();

    // make sure it is capturing property change events
    config.addPropertyChangeListener(l);
    config.setJobEntryName("jobName1");
    assertEquals(1, l.getReceivedEvents().size());
    assertEquals(config.getJobEntryName(), l.getReceivedEvents().get(0).getNewValue());

    // remove the listener & verify that it isn't receiving events anymore
    config.removePropertyChangeListener(l);
    config.setJobEntryName("jobName2");
    assertEquals(1, l.getReceivedEvents().size()); // still 1, from the previous call
  }

  @Test
  public void testAddPropertyChangeListener_propertyName() throws Exception {
    BlockableJobConfig config = new BlockableJobConfig();

    PersistentPropertyChangeListener l = new PersistentPropertyChangeListener();

    // dummy property name, should not indicate any captured prop change
    config.addPropertyChangeListener("dummy", l);
    config.setJobEntryName("jobName0");
    assertEquals(0, l.getReceivedEvents().size());
    config.removePropertyChangeListener("dummy", l);

    // make sure it is capturing property change events
    config.addPropertyChangeListener(BlockableJobConfig.JOB_ENTRY_NAME, l);
    config.setJobEntryName("jobName1");
    assertEquals(1, l.getReceivedEvents().size());
    assertEquals(config.getJobEntryName(), l.getReceivedEvents().get(0).getNewValue());

    // remove the listener & verify that it isn't receiving events anymore
    config.removePropertyChangeListener(BlockableJobConfig.JOB_ENTRY_NAME, l);
    config.setJobEntryName("jobName2");
    assertEquals(1, l.getReceivedEvents().size()); // still 1, from the previous call
  }

  @Test
  public void testGetterAndSetter() throws Exception {
    BlockableJobConfig config = new BlockableJobConfig();
    assertNull(config.getJobEntryName());

    config.setJobEntryName("jobName");
    assertEquals("jobName", config.getJobEntryName());
  }

  @Test
  public void testClone() throws Exception {
    BlockableJobConfig configOrig = new BlockableJobConfig();
    configOrig.setJobEntryName("Test");
    BlockableJobConfig configCloned = (BlockableJobConfig) configOrig.clone();

    assertNotSame(configOrig, configCloned);
    assertEquals(configOrig, configCloned);

    configOrig.setJobEntryName("New Name");
    assertFalse(configOrig.getJobEntryName().equals(configCloned.getJobEntryName()));

  }
}
