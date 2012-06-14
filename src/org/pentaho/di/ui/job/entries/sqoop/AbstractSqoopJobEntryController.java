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

import org.eclipse.swt.SWT;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.sqoop.AbstractSqoopJobEntry;
import org.pentaho.di.job.entries.sqoop.ArgumentWrapper;
import org.pentaho.di.job.entries.sqoop.SqoopConfig;
import org.pentaho.di.job.entries.sqoop.SqoopUtils;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.job.AbstractJobEntryController;
import org.pentaho.di.ui.job.JobEntryMode;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.util.AbstractModelList;

import java.util.Collection;
import java.util.List;

import static org.pentaho.di.job.entries.sqoop.SqoopConfig.*;

/**
 * Base functionality to support a Sqoop job entry controller that provides most of the common functionality to back a XUL-based dialog.
 *
 * @param <S> Type of Sqoop configuration object this controller depends upon. Must match the configuration object the
 *            job entry expects.
 */
public abstract class AbstractSqoopJobEntryController<S extends SqoopConfig> extends AbstractJobEntryController<S, AbstractSqoopJobEntry<S>> {

  public static final String SELECTED_DATABASE_CONNECTION = "selectedDatabaseConnection";
  public static final String MODE_TOGGLE_LABEL = "modeToggleLabel";
  private final String[] MODE_I18N_STRINGS = new String[]{"Sqoop.JobEntry.AdvancedOptions.Button.Text", "Sqoop.JobEntry.QuickSetup.Button.Text"};
  public static final String VALUE = "value";
  protected DatabaseItem NO_DATABASE = new DatabaseItem("@@none@@", "Choose Available"); // This is overwritten in init() with the i18n string
  protected DatabaseItem USE_ADVANCED_OPTIONS = new DatabaseItem("@@advanced@@", "Use Advanced Options"); // This is overwritten in init() with the i18n string

  protected AbstractModelList<ArgumentWrapper> advancedArguments;
  private AbstractModelList<DatabaseItem> databaseConnections;
  private DatabaseItem selectedDatabaseConnection;
  private DatabaseDialog databaseDialog;

  // Flag to indicate we shouldn't handle any events. Useful for preventing unwanted synchronization during initialization
  // or other user-driven events.
  protected boolean suppressEventHandling = false;


  /**
   * The text for the Quick Setup/Advanced Options mode toggle (label).
   */
  private String modeToggleLabel;
  private AdvancedButton selectedAdvancedButton = AdvancedButton.LIST;

  protected enum AdvancedButton {
    LIST(0, Mode.ADVANCED_LIST),
    COMMAND_LINE(1, Mode.ADVANCED_COMMAND_LINE);

    private int deckIndex;
    private Mode mode;

    private AdvancedButton(int deckIndex, Mode mode) {
      this.deckIndex = deckIndex;
      this.mode = mode;
    }

    public int getDeckIndex() {
      return deckIndex;
    }

    public Mode getMode() {
      return mode;
    }
  }

  /**
   * Creates a new Sqoop job entry controller.
   *
   * @param jobMeta         Meta data for the job
   * @param container       Container with dialog for which we will control
   * @param jobEntry        Job entry the dialog is being created for
   * @param bindingFactory  Binding factory to generate bindings
   */
  public AbstractSqoopJobEntryController(JobMeta jobMeta, XulDomContainer container, AbstractSqoopJobEntry<S> jobEntry, BindingFactory bindingFactory) {
    super(jobMeta, container, jobEntry, bindingFactory);
    this.config = (S) jobEntry.getJobConfig().clone();
    this.advancedArguments = new AbstractModelList<ArgumentWrapper>();
    this.databaseConnections = new AbstractModelList<DatabaseItem>();
  }

  /**
   * @return the element id of the XUL dialog element in the XUL document
   */
  public abstract String getDialogElementId();


  /**
   * Create the necessary XUL {@link Binding}s to support the dialog's desired functionality.
   *
   * @param config         Configuration object to bind to
   * @param container      Container with components to bind to
   * @param bindingFactory Binding factory to create bindings with
   * @param bindings       Collection to add created bindings to. This collection will be initialized via {@link org.pentaho.ui.xul.binding.Binding#fireSourceChanged()} upon return.
   */
  @Override
  protected void createBindings(S config, XulDomContainer container, BindingFactory bindingFactory, Collection<Binding> bindings) {
    bindingFactory.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bindings.add(bindingFactory.createBinding(config, JOB_ENTRY_NAME, JOB_ENTRY_NAME, VALUE));
    bindings.add(bindingFactory.createBinding(config, NAMENODE_HOST, NAMENODE_HOST, VALUE));
    bindings.add(bindingFactory.createBinding(config, NAMENODE_PORT, NAMENODE_PORT, VALUE));
    bindings.add(bindingFactory.createBinding(config, JOBTRACKER_HOST, JOBTRACKER_HOST, VALUE));
    bindings.add(bindingFactory.createBinding(config, JOBTRACKER_PORT, JOBTRACKER_PORT, VALUE));
    // TODO Determine if separate schema field is required, this has to be provided as part of the --table argument anyway.
//    bindings.add(bindingFactory.createBinding(config, SCHEMA, SCHEMA, VALUE));
    bindings.add(bindingFactory.createBinding(config, TABLE, TABLE, VALUE));

    bindings.add(bindingFactory.createBinding(config, COMMAND_LINE, COMMAND_LINE, VALUE));

    bindingFactory.setBindingType(Binding.Type.ONE_WAY);
    bindings.add(bindingFactory.createBinding(this, "modeToggleLabel", getModeToggleLabelElementId(), VALUE));
    bindings.add(bindingFactory.createBinding(databaseConnections, "children", "connection", "elements"));

    XulTree variablesTree = (XulTree) container.getDocumentRoot().getElementById("advanced-table");
    bindings.add(bindingFactory.createBinding(advancedArguments, "children", variablesTree, "elements"));

    // Create database/connection sync so that we're notified any time the connect argument is updated
    bindingFactory.createBinding(config, "connect", this, "connectChanged");
    bindingFactory.createBinding(config, "username", this, "usernameChanged");
    bindingFactory.createBinding(config, "password", this, "passwordChanged");

    bindingFactory.setBindingType(Binding.Type.BI_DIRECTIONAL);
    // Specifically create this binding after the databaseConnections binding so the list is populated before we attempt to select an item
    bindings.add(bindingFactory.createBinding(this, SELECTED_DATABASE_CONNECTION, "connection", "selectedItem"));
  }

  /**
   * @return element id for the deck of dialog modes (quick setup, advanced)
   */
  protected String getModeDeckElementId() {
    return "modeDeck";
  }

  @Override
  protected void beforeInit() {
    NO_DATABASE = new DatabaseItem("@@none@@", BaseMessages.getString(AbstractSqoopJobEntry.class, "DatabaseName.ChooseAvailable"));
    USE_ADVANCED_OPTIONS = new DatabaseItem("@@advanced@@", BaseMessages.getString(AbstractSqoopJobEntry.class, "DatabaseName.UseAdvancedOptions"));
    // Suppress event handling while we're initializing to prevent unwanted value changes
    suppressEventHandling = true;
    populateDatabases();
    setModeToggleLabel(BaseMessages.getString(AbstractSqoopJobEntry.class, MODE_I18N_STRINGS[0]));
//    customizeModeToggleLabel(getModeToggleLabelElementId());
  }

  @Override
  protected void afterInit() {
    setUiMode(getConfig().getModeAsEnum());

    suppressEventHandling = false;

    // Manually set the current database, if it is valid, to sync the UI buttons since we suppressed their event handling while initializing bindings
    setSelectedDatabaseConnection(createDatabaseItem(getConfig().getDatabase()));
  }

  /**
   * @return element id for the deck of advanced dialog modes (general, list, command line)
   */
  protected String getAdvancedModeDeckElementId() {
    return "advancedModeDeck";
  }

  /**
   * Synchronize the model values from the configuration object to our internal model objects.
   */
  protected void syncModel() {
    advancedArguments.clear();
    advancedArguments.addAll(config.getAdvancedArgumentsList());
  }

  /**
   * Populate the list of databases from the {@link JobMeta}.
   */
  protected void populateDatabases() {
    databaseConnections.clear();
    updateDatabaseItemsList();
    for (DatabaseMeta dbMeta : jobMeta.getDatabases()) {
      if (jobEntry.isDatabaseSupported(dbMeta.getDatabaseInterface().getClass())) {
        databaseConnections.add(new DatabaseItem(dbMeta.getName()));
      }
    }
  }

  /**
   * This is used to be notified when the connect string changes so we can remove the selection from the database dropdown.
   */
  public void setConnectChanged(String connect) {
    // If the connect string changes unselect the database
    if (!suppressEventHandling) {
      if (connect != null) {
        config.copyConnectionInfoToAdvanced();
        setSelectedDatabaseConnection(USE_ADVANCED_OPTIONS);
      } else {
        setSelectedDatabaseConnection(NO_DATABASE);
      }
    }
  }

  public void setUsernameChanged(String username) {
    if (!suppressEventHandling) {
      config.copyConnectionInfoToAdvanced();
      setSelectedDatabaseConnection(USE_ADVANCED_OPTIONS);
    }
  }

  public void setPasswordChanged(String password) {
    if (!suppressEventHandling) {
      config.copyConnectionInfoToAdvanced();
      setSelectedDatabaseConnection(USE_ADVANCED_OPTIONS);
    }
  }

  /**
   * @return the list of database connections that back the connections list
   */
  public AbstractModelList<DatabaseItem> getDatabaseConnections() {
    return databaseConnections;
  }

  /**
   * @return the selected database from the configuration object
   */
  public DatabaseItem getSelectedDatabaseConnection() {
    return selectedDatabaseConnection;
  }

  /**
   * Creates a {@link DatabaseItem} based on the name of a database and the existence of a connection string in the configuration.
   *
   * @param database Name of database
   * @return A database item whose name is {@code database}. If {@code database} is null, {@link #NO_DATABASE} is returned iff. {@link org.pentaho.di.job.entries.sqoop.SqoopConfig#getConnect() getConfig().getConnect()} is null; {@link #USE_ADVANCED_OPTIONS} otherwise.
   */
  protected DatabaseItem createDatabaseItem(String database) {
    return database == null
      ? (getConfig().getConnect() != null ? USE_ADVANCED_OPTIONS : NO_DATABASE)
      : new DatabaseItem(database);
  }

  /**
   * Sets the selected database connection. This database will be verified to exist and the appropriate settings within
   * the model will be set.
   *
   * @param selectedDatabaseConnection Database item to select
   */
  public void setSelectedDatabaseConnection(DatabaseItem selectedDatabaseConnection) {
    DatabaseItem old = this.selectedDatabaseConnection;
    this.selectedDatabaseConnection = selectedDatabaseConnection;
    DatabaseMeta databaseMeta = this.selectedDatabaseConnection == null ? null : jobMeta.findDatabase(this.selectedDatabaseConnection.getName());
    boolean validDatabaseSelected = databaseMeta != null;
    setDatabaseInteractionButtonsDisabled(!validDatabaseSelected);
    updateDatabaseItemsList();
    // If the selected database changes update the config
    if (!suppressEventHandling && (old == null && this.selectedDatabaseConnection != null || !old.equals(this.selectedDatabaseConnection))) {
      if (validDatabaseSelected) {
        try {
          getConfig().setConnectionInfo(databaseMeta.getName(), databaseMeta.getURL(), databaseMeta.getUsername(), databaseMeta.getPassword());
        } catch (KettleDatabaseException ex) {
          jobEntry.logError(BaseMessages.getString(AbstractSqoopJobEntry.class, "ErrorConfiguringDatabaseConnection"), ex);
        }
      } else {
        getConfig().copyConnectionInfoFromAdvanced();
      }
    }
    firePropertyChange("selectedDatabaseConnection", old, this.selectedDatabaseConnection);
  }

  /**
   * Make sure we have a "Use Advanced Option" in the list of database connections if we don't have a valid database selected
   * but we have an advanced connect string.
   */
  protected void updateDatabaseItemsList() {
    if (this.selectedDatabaseConnection == null || NO_DATABASE.equals(this.selectedDatabaseConnection)) {
      if (!databaseConnections.contains(NO_DATABASE)) {
        databaseConnections.add(0, NO_DATABASE);
      }
    } else {
      if (databaseConnections.contains(NO_DATABASE)) {
        databaseConnections.remove(NO_DATABASE);
      }
    }
    if (getConfig().getConnectFromAdvanced() != null ||
      getConfig().getUsernameFromAdvanced() != null ||
      getConfig().getPasswordFromAdvanced() != null) {
      if (!databaseConnections.contains(USE_ADVANCED_OPTIONS)) {
        databaseConnections.add(0, USE_ADVANCED_OPTIONS);
      }
    } else {
      if (databaseConnections.contains(USE_ADVANCED_OPTIONS)) {
        databaseConnections.remove(USE_ADVANCED_OPTIONS);
      }
    }
  }

  /**
   * Set the enabled state for all buttons that require a valid database to be selected.
   *
   * @param b {@code true} if the buttons should be disabled
   */
  protected void setDatabaseInteractionButtonsDisabled(boolean b) {
    document.getElementById(getEditConnectionButtonId()).setDisabled(b);
    document.getElementById(getBrowseTableButtonId()).setDisabled(b);
//    document.getElementById(getBrowseSchemaButtonId()).setDisabled(b);
  }

  /**
   * @return the text for the "modeToggleLabel" label
   */
  public String getModeToggleLabel() {
    return modeToggleLabel;
  }

  /**
   * Set the label text for the mode toggle label element
   * @param modeToggleLabel
   */
  public void setModeToggleLabel(String modeToggleLabel) {
    String old = this.modeToggleLabel;
    this.modeToggleLabel = modeToggleLabel;
    firePropertyChange(MODE_TOGGLE_LABEL, old, this.modeToggleLabel);
  }

  @Override
  protected void setModeToggleLabel(JobEntryMode mode) {
    setModeToggleLabel(BaseMessages.getString(AbstractSqoopJobEntry.class, MODE_I18N_STRINGS[mode.ordinal()]));
  }

  protected DatabaseDialog getDatabaseDialog() {
    if (databaseDialog == null) {
      databaseDialog = new DatabaseDialog(getShell());
    }
    return databaseDialog;
  }

  public void editConnection() {
    DatabaseMeta current = jobMeta.findDatabase(config.getDatabase());
    if (current == null) {
      return; // nothing to edit, this should not be possible through the UI
    }
    editDatabaseMeta(current, false);
  }

  public void newConnection() {
    editDatabaseMeta(new DatabaseMeta(), true);
  }

  /**
   * Open the Database Connection Dialog to edit
   *
   * @param database Database meta to edit
   * @param isNew    Is this database meta new? If so and the user chooses to save the database connection we will make sure to save this into the job meta.
   */
  protected void editDatabaseMeta(DatabaseMeta database, boolean isNew) {
    database.shareVariablesWith(jobMeta);
    getDatabaseDialog().setDatabaseMeta(database);
    if (getDatabaseDialog().open() != null) {
      if (isNew) {
        jobMeta.addDatabase(getDatabaseDialog().getDatabaseMeta());
      }
      populateDatabases();
      setSelectedDatabaseConnection(createDatabaseItem(getDatabaseDialog().getDatabaseMeta().getName()));
    }
  }

  /**
   * @return the simple name for this controller. This controller can be referenced by this name in the XUL document.
   */
  @Override
  public String getName() {
    return "controller";
  }

  /**
   * @return the edit connection button's id. By default this is {@code "editConnectionButton"}
   */
  public String getEditConnectionButtonId() {
    return "editConnectionButton";
  }

  /**
   * @return the browse table button's id. By default this is {@code "browseTableButton"}
   */
  public String getBrowseTableButtonId() {
    return "browseTableButton";
  }

  public String getBrowseSchemaButtonId() {
    return "browseSchemaButton";
  }

  /**
   * @return the id of the element responsible for toggling between "Quick Setup" and "Advanced Options" modes
   */
  public String getModeToggleLabelElementId() {
    return "mode-toggle-label";
  }

  /**
   * @return the advanced list button's id. By default this is {@code "advanced-list-button"}
   */
  public String getAdvancedListButtonElementId() {
    return "advanced-list-button";
  }

  /**
   * @return the advanced command line button's id. By default this is {@code "advanced-command-line-button"}
   */
  public String getAdvancedCommandLineButtonElementId() {
    return "advanced-command-line-button";
  }

  /**
   * @return the current configuration object. This configuration may be discarded if the dialog is canceled.
   */
  @Override
  public S getConfig() {
    return config;
  }

  /**
   * Test the configuration settings and show a dialog with the feedback.
   */
  public void testSettings() {
    List<String> warnings = jobEntry.getValidationWarnings(getConfig());
    if (!warnings.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (String warning : warnings) {
        sb.append(warning).append("\n");
      }
      showErrorDialog(
        BaseMessages.getString(AbstractSqoopJobEntry.class, "ValidationError.Dialog.Title"),
        sb.toString());
      return;
    }
    // TODO implement
    showErrorDialog("Error", "Not Implemented");
  }

  /**
   * Toggles between Quick Setup and Advanced Options mode. This assumes there exists a deck by id {@link #getModeDeckElementId()}
   * and it contains two panels.
   */
  public void toggleMode() {
    XulDeck deck = getModeDeck();
    setUiMode(deck.getSelectedIndex() == 1 ? Mode.QUICK_SETUP : selectedAdvancedButton.getMode());
  }

  /**
   * Toggles between Quick Setup and Advanced Options mode. This assumes there exists a deck by id {@link #getModeDeckElementId()}
   * and it contains two panels.
   *
   * @param quickMode Should quick mode be visible/selected?
   */
  protected void toggleQuickMode(boolean quickMode) {
    XulDeck deck = getModeDeck();
    deck.setSelectedIndex(quickMode ? 0 : 1);

    // Swap the label on the button
    setModeToggleLabel(BaseMessages.getString(AbstractSqoopJobEntry.class, MODE_I18N_STRINGS[deck.getSelectedIndex()]));

    // We toggle to and from quick setup in this method so either the old or the new is always Mode.QUICK_SETUP.
    // Whichever is not is the mode for the currently selected advanced button
    Mode oldMode = deck.getSelectedIndex() == 0 ? selectedAdvancedButton.getMode() : Mode.QUICK_SETUP;
    Mode newMode = Mode.QUICK_SETUP == oldMode ? selectedAdvancedButton.getMode() : Mode.QUICK_SETUP;
    updateUiMode(oldMode, newMode);
  }

  protected void setUiMode(Mode mode) {
    switch(mode) {
      case QUICK_SETUP:
        toggleQuickMode(true);
        break;
      case ADVANCED_LIST:
        setSelectedAdvancedButton(AdvancedButton.LIST);
        toggleQuickMode(false);
        break;
      case ADVANCED_COMMAND_LINE:
        setSelectedAdvancedButton(AdvancedButton.COMMAND_LINE);
        toggleQuickMode(false);
        break;
      default:
        throw new RuntimeException("unsupported mode: " + mode);
    }
  }

  /**
   * Update the UI Mode and configure the underlying {@link SqoopConfig} object.
   *
   * @param oldMode Old mode
   * @param newMode New mode
   */
  protected void updateUiMode(Mode oldMode, Mode newMode) {
    if (suppressEventHandling) {
      return;
    }
    if (Mode.ADVANCED_COMMAND_LINE.equals(oldMode)) {
      if (!syncCommandLineToConfig()) {
        // Flip back to the advanced command line view
        // Suppress event handling so we don't re-enter updateUiMode and copy the properties back on top of the command line
        suppressEventHandling = true;
        try {
          setUiMode(Mode.ADVANCED_COMMAND_LINE);
        } finally {
          suppressEventHandling = false;
        }
        return;
      }
    } else if(Mode.ADVANCED_COMMAND_LINE.equals(newMode)) {
      // Sync config properties -> command line
      getConfig().setCommandLine(SqoopUtils.generateCommandLineString(getConfig(), getJobEntry()));
    }

    if (Mode.ADVANCED_LIST.equals(newMode)) {
      // Synchronize the model when we switch to the advanced list to make sure it's fresh
      syncModel();
    }

    getConfig().setMode(getMode().name());
  }

  /**
   * @return the current UI mode based off the current state of the components
   */
  private Mode getMode() {
    XulDeck modeDeck = getModeDeck();
    XulDeck advancedModeDeck = getAdvancedModeDeck();
    if (modeDeck.getSelectedIndex() == 0) {
      return Mode.QUICK_SETUP;
    } else {
      for(AdvancedButton b : AdvancedButton.values()) {
        if(b.getDeckIndex() == advancedModeDeck.getSelectedIndex()) {
          return b.getMode();
        }
      }
    }
    throw new RuntimeException("unknown UI mode");
  }

  /**
   * Configure the current config object from the command line string. This will invoke {@link #showErrorDialog(String, String, Throwable)}
   * if an exception occurs.
   *
   * @return {@code true} if the command line could be parsed and the config object updated successfully.
   */
  protected boolean syncCommandLineToConfig() {
    try {
      // Sync command line -> config properties
      SqoopUtils.configureFromCommandLine(getConfig(), getConfig().getCommandLine(), getJobEntry());
      return true;
    } catch (Exception ex) {
      showErrorDialog(BaseMessages.getString(AbstractSqoopJobEntry.class, "Dialog.Error"),
        BaseMessages.getString(AbstractSqoopJobEntry.class, "ErrorConfiguringFromCommandLine"),
        ex);
    }
    return false;
  }

  public void setSelectedAdvancedButton(AdvancedButton button) {
    AdvancedButton old = selectedAdvancedButton;
    selectedAdvancedButton = button;
    switch(button) {
      case LIST:
        XulButton advancedList = getAdvancedListButton();
        advancedList.setSelected(true);
        getAdvancedCommandLineButton().setSelected(false);
        break;
      case COMMAND_LINE:
        getAdvancedListButton().setSelected(false);
        getAdvancedCommandLineButton().setSelected(true);
        break;
      default:
        throw new RuntimeException("Unknown button type: " + button);
    }
    toggleAdvancedMode(button);
    updateUiMode(old == null ? null : old.getMode(), button.getMode());
  }

  /**
   * Toggle the selected deck for advanced mode.
   *
   * @param button Button that was selected
   */
  protected void toggleAdvancedMode(AdvancedButton button) {
    getAdvancedModeDeck().setSelectedIndex(button.getDeckIndex());
  }

  /**
   * @return The button to select the advanced list mode
   */
  public XulButton getAdvancedListButton() {
    return getButton(getAdvancedListButtonElementId());
  }

  /**
   * @return The button to select the advanced command line mode
   */
  public XulButton getAdvancedCommandLineButton() {
    return getButton(getAdvancedCommandLineButtonElementId());
  }

  /**
   * @return the deck that shows either the Quick Setup or Advanced Mode UI
   */
  protected XulDeck getModeDeck() {
    return (XulDeck) getXulDomContainer().getDocumentRoot().getElementById(getModeDeckElementId());
  }

  /**
   * @return the deck that contains Advanced Mode panels
   */
  protected XulDeck getAdvancedModeDeck() {
    return (XulDeck) getXulDomContainer().getDocumentRoot().getElementById(getAdvancedModeDeckElementId());
  }


  /**
   * Gets a {@link XulButton} from the current {@link XulDomContainer}
   *
   * @param elementId Element Id of the button to look up
   * @return The button with element id {@code elementId} or {@code null} if not found
   */
  protected XulButton getButton(String elementId) {
    return (XulButton) getXulDomContainer().getDocumentRoot().getElementById(elementId);
  }

  /**
   * Callback for clicking the advanced list button
   */
  public void advancedListButtonClicked() {
    setSelectedAdvancedButton(AdvancedButton.LIST);
  }

  /**
   * Callback for clicking the advanced command line button
   */
  public void advancedCommandLineButtonClicked() {
    setSelectedAdvancedButton(AdvancedButton.COMMAND_LINE);
  }

  /**
   * Show the schema browse dialog if schemas can be detected and exist for the give database. Set the selected schema
   * to {@link SqoopConfig#setSchema(String) getConfig().setSchema(schema)}.
   */
  public void browseSchema() {
    DatabaseMeta databaseMeta = jobMeta.findDatabase(getConfig().getDatabase());
    Database database = new Database(jobMeta.getParent(), databaseMeta);
    try {
      database.connect();
      String schemas[] = database.getSchemas();

      if (null != schemas && schemas.length > 0) {
        schemas = Const.sortStrings(schemas);
        EnterSelectionDialog dialog = new EnterSelectionDialog(getShell(), schemas,
          BaseMessages.getString(AbstractSqoopJobEntry.class, "AvailableSchemas.Title"),
          BaseMessages.getString(AbstractSqoopJobEntry.class, "AvailableSchemas.Message"));
        String schema = dialog.open();
        if (schema != null) {
          getConfig().setSchema(schema);
        }
      } else {
        showErrorDialog(BaseMessages.getString(AbstractSqoopJobEntry.class, "Dialog.Error"), BaseMessages.getString(AbstractSqoopJobEntry.class, "NoSchema.Error"));
      }
    } catch (Exception e) {
      showErrorDialog(BaseMessages.getString(BaseMessages.getString(AbstractSqoopJobEntry.class, "System.Dialog.Error.Title")),
        BaseMessages.getString(BaseMessages.getString(AbstractSqoopJobEntry.class, "ErrorRetrievingSchemas")), e);
    } finally {
      database.disconnect();
    }
  }

  /**
   * Show the Database Explorer Dialog for the database information provided. The provided schema and table
   * will be selected if already configured. Any new selection will be saved in the current configuration.
   */
  public void browseTable() {
    DatabaseMeta databaseMeta = jobMeta.findDatabase(getConfig().getDatabase());
    DatabaseExplorerDialog std = new DatabaseExplorerDialog(getShell(), SWT.NONE, databaseMeta, jobMeta.getDatabases());
    std.setSelectedSchemaAndTable(getConfig().getSchema(), getConfig().getTable());
    if (std.open()) {
      getConfig().setSchema(std.getSchemaName());
      getConfig().setTable(std.getTableName());
    }
  }

}
