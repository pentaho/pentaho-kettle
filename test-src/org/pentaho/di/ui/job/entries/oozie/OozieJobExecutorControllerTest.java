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

import org.eclipse.jface.dialogs.MessageDialog;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.job.JobEntryMode;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.PropertyEntry;
import org.pentaho.di.job.entries.oozie.OozieJobExecutorConfig;
import org.pentaho.di.job.entries.oozie.OozieJobExecutorJobEntry;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.impl.XulFragmentContainer;
import org.pentaho.ui.xul.util.AbstractModelList;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

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
  public void before() throws XulException {
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
    assertEquals(controller.getModeToggleLabel(), "Quick Setup");

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

  @Test
  public void testSyncModel_quickSetupMode() throws Exception {
    assertEquals(0, controller.getAdvancedArguments().size());

    // set the props file, sync the model... should have equal amounts of elements
    OozieJobExecutorConfig config = getGoodConfig();
    controller.setConfig(config);
    controller.setJobEntryMode(JobEntryMode.QUICK_SETUP);
    assertEquals(0, controller.getAdvancedArguments().size());
    controller.syncModel();
    assertEquals(0, controller.getAdvancedArguments().size());
  }
  
  @Test
  public void testSyncModel_advancedMode() throws Exception {
    assertEquals(0, controller.getAdvancedArguments().size());

    // set the props file, sync the model... should have equal amounts of elements
    OozieJobExecutorConfig config = getGoodConfig();
    controller.setConfig(config);
    controller.setJobEntryMode(JobEntryMode.ADVANCED_LIST);
    Properties props = OozieJobExecutorJobEntry.getProperties(config, new Variables());

    assertFalse(props.size() == controller.getAdvancedArguments().size());
    controller.syncModel();
    assertEquals(props.size(), controller.getAdvancedArguments().size());
  }

  @Test
  public void testSyncModel_advanced_addedProp() throws Exception {
    OozieJobExecutorConfig config = getGoodConfig();
    controller.setConfig(config);
    Properties props = OozieJobExecutorJobEntry.getProperties(config, new Variables());
    controller.setJobEntryMode(JobEntryMode.ADVANCED_LIST);

    controller.syncModel();

    controller.addNewProperty();
    controller.syncModel();
    assertTrue(controller.isAdvancedArgumentsChanged());
    assertEquals(props.size() + 1, controller.getAdvancedArguments().size());
  }


  // ignoring for now, remove depends on the tree having selected items...
  @Ignore
  @Test
  public void testSyncModel_advanced_removedProp() throws Exception {
    OozieJobExecutorConfig config = getGoodConfig();

    controller.setConfig(config);
    Properties props = OozieJobExecutorJobEntry.getProperties(config, new Variables());
    controller.syncModel();
    controller.setJobEntryMode(JobEntryMode.ADVANCED_LIST);

    controller.variablesTree.setSelectedRows(new int[] {0});
    controller.removeProperty();
    controller.syncModel();
    assertTrue(controller.isAdvancedArgumentsChanged());
    assertEquals(props.size() - 1, controller.getAdvancedArguments().size());
  }

  @Test
  public void testSyncModel_advanced_editProp() throws Exception {
    OozieJobExecutorConfig config = getGoodConfig();
    controller.setConfig(config);
    Properties props = OozieJobExecutorJobEntry.getProperties(config, new Variables());
    controller.setJobEntryMode(JobEntryMode.ADVANCED_LIST);

    controller.syncModel();

    String key = controller.getAdvancedArguments().get(0).getKey();
    AbstractModelList<PropertyEntry> advanced = controller.getAdvancedArguments();
    advanced.get(0).setValue("new value");
    controller.setAdvancedArguments(advanced);
    controller.syncModel();
    assertEquals(props.size(), controller.getAdvancedArguments().size());
    Properties updatedProps = OozieJobExecutorJobEntry.getProperties(controller.getConfig(), new Variables());
    assertEquals("new value", updatedProps.get(key));
  }


  @Test
  public void testToggleMode() throws Exception {
    // get into advanced mode
    TestOozieJobExecutorController ctr = new TestOozieJobExecutorController();

    OozieJobExecutorConfig config = getGoodConfig();
    ctr.setConfig(config);
    Properties props = OozieJobExecutorJobEntry.getProperties(config, new Variables());
    ctr.syncModel();

    ctr.setJobEntryMode(JobEntryMode.ADVANCED_LIST);

    ctr.syncModel();

    String key = ctr.getAdvancedArguments().get(0).getKey();
    AbstractModelList<PropertyEntry> advanced = ctr.getAdvancedArguments();
    advanced.get(0).setValue("new value");
    ctr.setAdvancedArguments(advanced);
    ctr.syncModel();
    assertEquals(props.size(), ctr.getAdvancedArguments().size());
    Properties updatedProps = OozieJobExecutorJobEntry.getProperties(ctr.getConfig(), new Variables());
    assertEquals("new value", updatedProps.get(key));
    assertEquals(props.size(), ctr.getConfig().getWorkflowProperties().size());

    // make sure if set to QUICK_SETUP that we clear out any custom props from advanced mode
    ctr.toggleMode();
    assertEquals(0, ctr.getConfig().getWorkflowProperties().size());

  }

  @Test
  public void testShouldUseAdvancedProperties_basicMode() throws Exception {
    OozieJobExecutorConfig config = getGoodConfig();
    controller.setConfig(config);
    assertFalse(controller.shouldUseAdvancedProperties());
  }

  private OozieJobExecutorConfig getGoodConfig() {
    OozieJobExecutorConfig config = new OozieJobExecutorConfig();
    config.setOozieUrl("http://localhost:11000/oozie");             // don't worry if it isn't running, we fake out our test connection to it anyway
    config.setOozieWorkflowConfig("test-src/job.properties");
    config.setJobEntryName("name");
    return config;
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

    @Override
    protected boolean showConfirmationDialog(String title, String message) {
      return true;
    }

    @Override
    public void toggleMode() {
      JobEntryMode mode = (jobEntryMode == JobEntryMode.ADVANCED_LIST ? JobEntryMode.QUICK_SETUP : JobEntryMode.ADVANCED_LIST);
      this.setJobEntryMode(mode);
      this.syncModel();
    }
  }

  public class TestOozieJobExecutorJobEntry extends OozieJobExecutorJobEntry {
    @Override
    public List<String> getValidationWarnings(OozieJobExecutorConfig config) {
      return new ArrayList<String>();
    }
  }

}
