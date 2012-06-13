/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.job.entries.sqoop;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.sqoop.AbstractSqoopJobEntry;
import org.pentaho.di.job.entries.sqoop.PersistentPropertyChangeListener;
import org.pentaho.di.job.entries.sqoop.SqoopConfig;
import org.pentaho.di.job.entries.sqoop.SqoopConfig.Mode;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.impl.XulFragmentContainer;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AbstractSqoopJobEntryControllerTest {

  private class TestSqoopJobEntryController extends AbstractSqoopJobEntryController<SqoopConfig> {
    private XulDeck modeDeck;
    private XulDeck advancedModeDeck;
    private XulButton advancedListButton;
    private XulButton advancedCommandLineButton;

    private List<Object[]> shownErrors = new ArrayList<Object[]>();

    TestSqoopJobEntryController() {
      this(null, null, null, null);
    }

    TestSqoopJobEntryController(XulDeck modeDeck, XulDeck advancedModeDeck, XulButton advancedListButton, XulButton advancedCommandLineButton) {
      super(new JobMeta(), new XulFragmentContainer(null), new AbstractSqoopJobEntry() {
        @Override
        protected SqoopConfig buildSqoopConfig() {
          return new SqoopConfig() {
          };
        }

        @Override
        protected String getToolName() {
          return null;
        }
      }, new DefaultBindingFactory());
      this.modeDeck = modeDeck;
      this.advancedModeDeck = advancedModeDeck;
      this.advancedListButton = advancedListButton;
      this.advancedCommandLineButton = advancedCommandLineButton;
      syncModel();
    }

    @Override
    public String getDialogElementId() {
      return null;
    }

    @Override
    protected void editDatabaseMeta(DatabaseMeta database, boolean isNew) {
      // do nothing in tests
    }

    @Override
    protected void setDatabaseInteractionButtonsDisabled(boolean b) {
      // do nothing in tests
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
    protected XulDeck getModeDeck() {
      return modeDeck;
    }

    @Override
    protected XulDeck getAdvancedModeDeck() {
      return advancedModeDeck;
    }

    @Override
    public XulButton getAdvancedListButton() {
      return advancedListButton;
    }

    @Override
    public XulButton getAdvancedCommandLineButton() {
      return advancedCommandLineButton;
    }
  }

  @BeforeClass
  public static void setup() throws KettleException {
    KettleEnvironment.init();
  }

  @Test
  public void populateDatabases() throws XulException, InvocationTargetException {
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController();
    DatabaseItem test = new DatabaseItem("test");
    controller.populateDatabases();
    assertEquals(1, controller.getDatabaseConnections().size());
    assertEquals(controller.NO_DATABASE, controller.getDatabaseConnections().get(0));

    controller.getJobMeta().addDatabase(new DatabaseMeta(test.getName(), "MYSQL", null, null, null, null, null, null));
    controller.populateDatabases();
    assertEquals(2, controller.getDatabaseConnections().size());
    assertEquals(controller.NO_DATABASE, controller.getDatabaseConnections().get(0));
    assertEquals(test, controller.getDatabaseConnections().get(1));

    // Setting a selected database should remove the NO_DATABASE option
    controller.setSelectedDatabaseConnection(test);
    assertEquals(1, controller.getDatabaseConnections().size());
    assertEquals(test, controller.getDatabaseConnections().get(0));
  }

  @Test
  public void getSelectedDatabaseConnection() throws InvocationTargetException {
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController();

    DatabaseItem test = new DatabaseItem("test");
    controller.getJobMeta().addDatabase(new DatabaseMeta(test.getName(), "MYSQL", null, null, null, null, null, null));
    controller.setSelectedDatabaseConnection(test);

    assertEquals(test, controller.getSelectedDatabaseConnection());
    assertEquals(test.getName(), controller.getConfig().getDatabase());
  }

  /**
   * Make sure that setting the selected database to and from {@link AbstractSqoopJobEntryController#USE_ADVANCED_OPTIONS}
   * will properly set the {@code database, connect, username, and password} fields according to advanced settings or not.
   * @throws KettleDatabaseException
   */
  @Test
  public void setSelectedDatabaseConnection() throws KettleDatabaseException {
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController();

    String connect = "jdbc:bogus://bogus";
    String username = "username";
    String password = "password";
    controller.getConfig().setConnect(connect);
    controller.getConfig().setUsername(username);
    controller.getConfig().setPassword(password);

    String connectAdvanced = "jdbc:advanced://bogus";
    String usernameAdvanced = "advanced_user";
    String passwordAdvanced = "super password!";
    controller.getConfig().setConnectFromAdvanced(connectAdvanced);
    controller.getConfig().setUsernameFromAdvanced(usernameAdvanced);
    controller.getConfig().setPasswordFromAdvanced(passwordAdvanced);

    DatabaseItem test = new DatabaseItem("test");
    DatabaseMeta database = new DatabaseMeta(test.getName(), "MYSQL", null, null, null, null, null, null);
    controller.getJobMeta().addDatabase(database);
    controller.setSelectedDatabaseConnection(test);

    assertEquals(test, controller.getSelectedDatabaseConnection());
    assertEquals(test.getName(), controller.getConfig().getDatabase());
    assertEquals(database.getURL(), controller.getConfig().getConnect());
    assertEquals(database.getUsername(), controller.getConfig().getUsername());
    assertEquals(database.getPassword(), controller.getConfig().getPassword());

    assertEquals(connectAdvanced, controller.getConfig().getConnectFromAdvanced());
    assertEquals(usernameAdvanced, controller.getConfig().getUsernameFromAdvanced());
    assertEquals(passwordAdvanced, controller.getConfig().getPasswordFromAdvanced());

    controller.setSelectedDatabaseConnection(controller.USE_ADVANCED_OPTIONS);
    assertEquals(controller.USE_ADVANCED_OPTIONS, controller.getSelectedDatabaseConnection());
    assertNull(controller.getConfig().getDatabase());
    assertEquals(connectAdvanced, controller.getConfig().getConnect());
    assertEquals(usernameAdvanced, controller.getConfig().getUsername());
    assertEquals(passwordAdvanced, controller.getConfig().getPassword());
    assertEquals(connectAdvanced, controller.getConfig().getConnectFromAdvanced());
    assertEquals(usernameAdvanced, controller.getConfig().getUsernameFromAdvanced());
    assertEquals(passwordAdvanced, controller.getConfig().getPasswordFromAdvanced());

    controller.setSelectedDatabaseConnection(test);
    assertEquals(test, controller.getSelectedDatabaseConnection());
    assertEquals(test.getName(), controller.getConfig().getDatabase());
    assertEquals(database.getURL(), controller.getConfig().getConnect());
    assertEquals(database.getUsername(), controller.getConfig().getUsername());
    assertEquals(database.getPassword(), controller.getConfig().getPassword());

    assertEquals(connectAdvanced, controller.getConfig().getConnectFromAdvanced());
    assertEquals(usernameAdvanced, controller.getConfig().getUsernameFromAdvanced());
    assertEquals(passwordAdvanced, controller.getConfig().getPasswordFromAdvanced());
  }

  @Test
  public void updateDatabaseItemsList() {
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController();

    DatabaseItem test = new DatabaseItem("test");

    controller.getJobMeta().addDatabase(new DatabaseMeta(test.getName(), "MYSQL", null, null, null, null, null, null));
    controller.setSelectedDatabaseConnection(test);
    controller.populateDatabases();

    assertEquals(1, controller.getDatabaseConnections().size());
    assertEquals(test, controller.getDatabaseConnections().get(0));

    controller.getConfig().setConnectFromAdvanced("testing");
    controller.updateDatabaseItemsList();

    assertEquals(2, controller.getDatabaseConnections().size());
    assertEquals(controller.USE_ADVANCED_OPTIONS, controller.getDatabaseConnections().get(0));
    assertEquals(test, controller.getDatabaseConnections().get(1));

    controller.getConfig().setConnectFromAdvanced(null);
    controller.updateDatabaseItemsList();

    assertEquals(1, controller.getDatabaseConnections().size());
    assertEquals(test, controller.getDatabaseConnections().get(0));
  }

  @Test
  public void setConnectChanged() {
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController();

    String database = "testing";
    String connect = "jdbc:bogus://bogus";

    controller.getConfig().setDatabase(database);
    controller.getConfig().setConnect(connect);

    controller.setConnectChanged("test");

    assertNull(controller.getConfig().getDatabase());
    assertEquals(connect, controller.getConfig().getConnect());
    assertEquals(connect, controller.getConfig().getConnectFromAdvanced());
  }

  @Test
  public void setConnectChanged_ignoring_change_events() {
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController();
    String database = "testing";
    String connect = "jdbc:bogus://bogus";
    controller.getConfig().setDatabase(database);
    controller.getConfig().setConnect(connect);

    controller.suppressEventHandling = true;
    controller.setConnectChanged(connect);

    assertEquals(database, controller.getConfig().getDatabase());
    assertEquals(connect, controller.getConfig().getConnect());
    assertNull(connect, controller.getConfig().getConnectFromAdvanced());
  }

  @Test
  public void setUsernameChanged() {
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController();
    String database = "testing";
    String username = "username";
    controller.getConfig().setDatabase(database);
    controller.getConfig().setUsername(username);
    controller.setUsernameChanged("testing");

    assertNull(controller.getConfig().getDatabase());
    assertEquals(username, controller.getConfig().getUsername());
    assertEquals(username, controller.getConfig().getUsernameFromAdvanced());
  }

  @Test
  public void setUsernameChanged_ignoring_change_events() {
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController();
    String database = "testing";
    String username = "username";
    controller.getConfig().setDatabase(database);
    controller.getConfig().setUsername(username);

    controller.suppressEventHandling = true;
    controller.setUsernameChanged("testing");

    assertEquals(database, controller.getConfig().getDatabase());
    assertEquals(username, controller.getConfig().getUsername());
    assertNull(username, controller.getConfig().getUsernameFromAdvanced());
  }
  @Test
  public void setPasswordChanged() {
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController();
    String database = "testing";
    String password = "password";
    controller.getConfig().setDatabase(database);
    controller.getConfig().setPassword(password);
    controller.setPasswordChanged("testing");

    assertNull(controller.getConfig().getDatabase());
    assertEquals(password, controller.getConfig().getPassword());
    assertEquals(password, controller.getConfig().getPasswordFromAdvanced());
  }

  @Test
  public void setPasswordChanged_ignoring_change_events() {
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController();
    String database = "testing";
    String password = "password";
    controller.getConfig().setDatabase(database);
    controller.getConfig().setPassword(password);

    controller.suppressEventHandling = true;
    controller.setPasswordChanged("testing");

    assertEquals(database, controller.getConfig().getDatabase());
    assertEquals(password, controller.getConfig().getPassword());
    assertNull(password, controller.getConfig().getPasswordFromAdvanced());
  }

  @Test
  public void setModeToggleLabel() {
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController();
    PersistentPropertyChangeListener l = new PersistentPropertyChangeListener();
    controller.addPropertyChangeListener(l);

    String label = "test label";
    controller.setModeToggleLabel(label);

    assertEquals(label, controller.getModeToggleLabel());
    assertEquals(1, l.getReceivedEvents().size());
    PropertyChangeEvent event = l.getReceivedEvents().get(0);
    assertEquals("modeToggleLabel", event.getPropertyName());
    assertNull(event.getOldValue());
    assertEquals(label, event.getNewValue());
  }

  @Test
  public void createDatabaseItem() {
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController();
    String databaseName = "test";
    DatabaseItem item = controller.createDatabaseItem(databaseName);
    assertEquals("test", item.getName());

    item = controller.createDatabaseItem(null);
    assertEquals(controller.NO_DATABASE, item);

    controller.getConfig().setConnect("jdbc:bogus://bogus");
    item = controller.createDatabaseItem(null);
    assertEquals(controller.USE_ADVANCED_OPTIONS, item);
  }

  @Test
  public void updateUiMode() {
    XulDeck modeDeck = new MockXulDeck();
    XulDeck advancedModeDeck = new MockXulDeck();
    XulButton advancedListButton = new MockXulButton();
    XulButton advancedCommandLineButton = new MockXulButton();
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController(modeDeck, advancedModeDeck, advancedListButton, advancedCommandLineButton);
    modeDeck.setSelectedIndex(0);
    advancedModeDeck.setSelectedIndex(1);

    assertNull(controller.getConfig().getMode());

    controller.getConfig().setConnect("jdbc:mysql://from/config");
    controller.getConfig().setCommandLine("sqoop import --connect jdbc:mysql://from/cli --table test");

    Mode oldMode = Mode.ADVANCED_LIST;
    Mode newMode = Mode.QUICK_SETUP;

    controller.updateUiMode(oldMode, newMode);

    assertEquals(SqoopConfig.Mode.QUICK_SETUP, controller.getConfig().getModeAsEnum());

    // Command line should not be synced when changing from advanced list to quick setup
    assertEquals("jdbc:mysql://from/config", controller.getConfig().getConnect());

    modeDeck.setSelectedIndex(1);
    oldMode = Mode.QUICK_SETUP;
    newMode = Mode.ADVANCED_COMMAND_LINE;
    controller.updateUiMode(oldMode, newMode);

    // Command line should be synced when changing from anything to command line
    assertEquals("--connect jdbc:mysql://from/config", controller.getConfig().getCommandLine()) ;

    assertEquals(SqoopConfig.Mode.ADVANCED_COMMAND_LINE, controller.getConfig().getModeAsEnum());

    // Change the command line string as if the user updated it
    controller.getConfig().setCommandLine("sqoop import --connect jdbc:mysql://from/cli --table test");

    advancedModeDeck.setSelectedIndex(0);
    oldMode = Mode.ADVANCED_COMMAND_LINE;
    newMode = Mode.ADVANCED_LIST;
    controller.updateUiMode(oldMode, newMode);

    // Command line should be synced when changing from command line to advanced list
    assertEquals("jdbc:mysql://from/cli", controller.getConfig().getConnect());
    assertEquals("test", controller.getConfig().getTable());

    assertEquals(SqoopConfig.Mode.ADVANCED_LIST, controller.getConfig().getModeAsEnum());
  }

  @Test
  public void setUiMode_exception_parsing_command_line() {
    XulDeck modeDeck = new MockXulDeck();
    XulDeck advancedModeDeck = new MockXulDeck();
    XulButton advancedListButton = new MockXulButton();
    XulButton advancedCommandLineButton = new MockXulButton();
    TestSqoopJobEntryController controller = new TestSqoopJobEntryController(modeDeck, advancedModeDeck, advancedListButton, advancedCommandLineButton);

    controller.setUiMode(Mode.QUICK_SETUP);

    controller.getConfig().setCommandLine("--table test -P --connect jdbc:mysql://bogus/db");

    controller.updateUiMode(Mode.ADVANCED_COMMAND_LINE, Mode.ADVANCED_LIST);

    assertEquals(1, controller.shownErrors.size());
    assertEquals(BaseMessages.getString(AbstractSqoopJobEntry.class, "ErrorConfiguringFromCommandLine"), controller.shownErrors.get(0)[1]);
    assertEquals(KettleException.class, controller.shownErrors.get(0)[2].getClass());
  }

  @Test
  public void toggleMode() {
    XulDeck modeDeck = new MockXulDeck();
    XulDeck advancedModeDeck = new MockXulDeck();
    XulButton advancedListButton = new MockXulButton();
    XulButton advancedCommandLineButton = new MockXulButton();
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController(modeDeck, advancedModeDeck, advancedListButton, advancedCommandLineButton);
    modeDeck.setSelectedIndex(0);

    assertFalse(advancedListButton.isSelected());
    assertFalse(advancedCommandLineButton.isSelected());

    controller.toggleMode();
    assertEquals(1, modeDeck.getSelectedIndex());
    assertTrue(advancedListButton.isSelected());
    assertFalse(advancedCommandLineButton.isSelected());
  }

  @Test
  public void setUiMode() {
    XulDeck modeDeck = new MockXulDeck();
    XulDeck advancedModeDeck = new MockXulDeck();
    XulButton advancedListButton = new MockXulButton();
    XulButton advancedCommandLineButton = new MockXulButton();
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController(modeDeck, advancedModeDeck, advancedListButton, advancedCommandLineButton);

    assertEquals(-1, modeDeck.getSelectedIndex());
    assertEquals(-1, advancedModeDeck.getSelectedIndex());

    assertNull(controller.getModeToggleLabel());

    controller.setUiMode(SqoopConfig.Mode.QUICK_SETUP);
    assertEquals(0, modeDeck.getSelectedIndex());
    assertEquals(-1, advancedModeDeck.getSelectedIndex());
    assertEquals(BaseMessages.getString(AbstractSqoopJobEntry.class, "Sqoop.JobEntry.AdvancedOptions.Button.Text"), controller.getModeToggleLabel());

    controller.setUiMode(SqoopConfig.Mode.ADVANCED_LIST);
    assertEquals(1, modeDeck.getSelectedIndex());
    assertEquals(0, advancedModeDeck.getSelectedIndex());
    assertEquals(BaseMessages.getString(AbstractSqoopJobEntry.class, "Sqoop.JobEntry.QuickSetup.Button.Text"), controller.getModeToggleLabel());

    controller.setUiMode(SqoopConfig.Mode.ADVANCED_COMMAND_LINE);
    assertEquals(1, modeDeck.getSelectedIndex());
    assertEquals(1, advancedModeDeck.getSelectedIndex());
    assertEquals(BaseMessages.getString(AbstractSqoopJobEntry.class, "Sqoop.JobEntry.QuickSetup.Button.Text"), controller.getModeToggleLabel());
  }

  @Test
  public void setSelectedAdvancedButton() {
    XulDeck modeDeck = new MockXulDeck();
    XulDeck advancedModeDeck = new MockXulDeck();
    XulButton advancedListButton = new MockXulButton();
    XulButton advancedCommandLineButton = new MockXulButton();
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController(modeDeck, advancedModeDeck, advancedListButton, advancedCommandLineButton);

    assertNull(controller.getConfig().getMode());
    assertEquals(-1, advancedModeDeck.getSelectedIndex());

    assertFalse(advancedListButton.isSelected());
    assertFalse(advancedCommandLineButton.isSelected());

    controller.setSelectedAdvancedButton(AbstractSqoopJobEntryController.AdvancedButton.COMMAND_LINE);

    assertEquals(Mode.ADVANCED_COMMAND_LINE, controller.getConfig().getModeAsEnum());
    assertEquals(1, advancedModeDeck.getSelectedIndex());
    assertFalse(advancedListButton.isSelected());
    assertTrue(advancedCommandLineButton.isSelected());

    controller.setSelectedAdvancedButton(AbstractSqoopJobEntryController.AdvancedButton.LIST);

    assertEquals(Mode.ADVANCED_LIST, controller.getConfig().getModeAsEnum());
    assertEquals(0, advancedModeDeck.getSelectedIndex());
    assertTrue(advancedListButton.isSelected());
    assertFalse(advancedCommandLineButton.isSelected());
  }

  @Test
  public void syncCommandLineToConfig() throws XulException, InvocationTargetException {
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController();

    controller.getConfig().setCommandLine("--table test --connect jdbc:fake://bogus/db");
    controller.getConfig().setNamenodeHost("testing");
    controller.getConfig().setNamenodePort("54310");
    controller.getConfig().setJobtrackerHost("testing");
    controller.getConfig().setJobtrackerPort("testing");

    PersistentPropertyChangeListener l = new PersistentPropertyChangeListener();
    controller.getConfig().addPropertyChangeListener(l);

    controller.syncCommandLineToConfig();

    assertEquals("test", controller.getConfig().getTable());
    assertEquals("jdbc:fake://bogus/db", controller.getConfig().getConnect());

    List<PropertyChangeEvent> receivedEventsWithChanges = l.getReceivedEventsWithChanges();
    assertEquals(2, receivedEventsWithChanges.size());
    PropertyChangeEvent evt = receivedEventsWithChanges.get(0);
    assertEquals("connect", evt.getPropertyName());
    assertEquals("jdbc:fake://bogus/db", evt.getNewValue());
    evt = receivedEventsWithChanges.get(1);
    assertEquals("table", evt.getPropertyName());
    assertEquals("test", evt.getNewValue());

    // Make sure connection information is not reset
    assertEquals("testing", controller.getConfig().getNamenodeHost());
  }
}
