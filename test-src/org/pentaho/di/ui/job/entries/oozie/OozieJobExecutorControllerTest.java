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
import org.pentaho.di.job.JobEntryMode;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.oozie.OozieJobExecutorConfig;
import org.pentaho.di.job.entries.oozie.OozieJobExecutorJobEntry;
import org.pentaho.di.job.entries.oozie.OozieJobExecutorJobEntryTest;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.impl.XulFragmentContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * User: RFellows
 * Date: 6/4/12
 */
public class OozieJobExecutorControllerTest {

  OozieJobExecutorConfig jobConfig = null;
  OozieJobExecutorJobEntryController controller = null;

  @BeforeClass
  public static void init() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void before() {
    jobConfig = new OozieJobExecutorConfig();
    jobConfig.setOozieWorkflow("hdfs://localhost:9000/user/" + System.getProperty("user.name") + "/examples/apps/map-reduce");

    controller = new OozieJobExecutorJobEntryController(new JobMeta(),
            new XulFragmentContainer(null),
            new OozieJobExecutorJobEntry(),
            new DefaultBindingFactory()
        );
  }

  @Test
  public void testSetModeToggleLabel_JobEntryMode() throws Exception {
    assertEquals(controller.getModeToggleLabel(), null);

    controller.setModeToggleLabel(JobEntryMode.QUICK_SETUP);
    assertEquals(controller.getModeToggleLabel(), "Basic Options");

    controller.setModeToggleLabel(JobEntryMode.ADVANCED_LIST);
    assertEquals(controller.getModeToggleLabel(), "Advanced Options");

  }

  @Test(expected = RuntimeException.class)
  public void testSetModeToggleLabel_UnsupportedJobEntryMode() {
    controller.setModeToggleLabel(JobEntryMode.ADVANCED_COMMAND_LINE);
    fail("JobEntryMode.ADVANCED_COMMAND_LINE is not supported, should have gotten a RuntimeException");
  }

  @Test
  public void testTestSettings_ErrorsFound() throws Exception {
    TestOozieJobExecutorController ctr = new TestOozieJobExecutorController();
    ctr.setConfig(new OozieJobExecutorConfig());
    ctr.testSettings();
    assertTrue(ctr.getShownErrors().size() > 0);
  }

  @Test
  public void testTestSettings_NoErrors() throws Exception {
    TestOozieJobExecutorController ctr = new TestOozieJobExecutorController();

    // the dummy test job entry will return no errors when getValidationMessages is called
    ctr.setJobEntry(new TestOozieJobExecutorJobEntry());

    OozieJobExecutorConfig config = new OozieJobExecutorConfig();
    ctr.setConfig(config);
    ctr.testSettings();
    assertTrue(ctr.wasInfoShown());
  }



  // stub classes
  class TestOozieJobExecutorController extends OozieJobExecutorJobEntryController {
    private XulDeck modeDeck;

    private List<Object[]> shownErrors = new ArrayList<Object[]>();
    private boolean infoShown = false;

    TestOozieJobExecutorController() {
      this(null);
    }

    public TestOozieJobExecutorController(XulDeck modeDeck) {
      super(new JobMeta(),
          new XulFragmentContainer(null),
          new OozieJobExecutorJobEntry(),
          new DefaultBindingFactory());

      this.modeDeck = modeDeck;
      syncModel();
    }

    @Override
    protected void showErrorDialog(String title, String message) {
      shownErrors.add(new Object[]{title, message, null});
    }

    @Override
    protected void showErrorDialog(String title, String message, Throwable t) {
      shownErrors.add(new Object[]{title, message, t});
    }

    @Override
    protected void showInfoDialog(String title, String message) {
      infoShown = true;
    }

    public List<Object[]> getShownErrors() {
      return shownErrors;
    }

    public boolean wasInfoShown() {
      return infoShown;
    }

    public void setJobEntry(OozieJobExecutorJobEntry je) {
      jobEntry = je;
    }

    //    @Override
//    protected XulDeck getModeDeck() {
//      return modeDeck;
//    }
  }

  public class TestOozieJobExecutorJobEntry extends OozieJobExecutorJobEntry {
    @Override
    public List<String> getValidationWarnings(OozieJobExecutorConfig config) {
      return new ArrayList<String>();
    }
  }

}
