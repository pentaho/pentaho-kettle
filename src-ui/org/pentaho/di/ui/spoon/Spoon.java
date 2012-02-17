/*******************************************************************************
 *
 * Pentaho Data Integration
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

package org.pentaho.di.ui.spoon;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs.FileObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.AddUndoPositionInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.JndiUtil;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.ObjectUsageCount;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.changed.ChangedFlagInterface;
import org.pentaho.di.core.changed.PDIObserver;
import org.pentaho.di.core.clipboard.ImageDataTransfer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleAuthException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleRowException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.gui.GUIFactory;
import org.pentaho.di.core.gui.OverwritePrompter;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.gui.SpoonInterface;
import org.pentaho.di.core.gui.UndoInterface;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifeEventInfo;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleSupport;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.Log4jFileAppender;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.LifecyclePluginType;
import org.pentaho.di.core.plugins.PartitionerPluginType;
import org.pentaho.di.core.plugins.PluginFolder;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.reflection.StringSearchResult;
import org.pentaho.di.core.row.RowBuffer;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.undo.TransAction;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.imp.ImportRules;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.laf.BasePropertyHandler;
import org.pentaho.di.pan.CommandLineOption;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.pkg.JarfileGenerator;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryCapabilities;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.resource.ResourceExportInterface;
import org.pentaho.di.resource.ResourceUtil;
import org.pentaho.di.resource.TopLevelResource;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.HasSlaveServersInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.di.ui.cluster.dialog.ClusterSchemaDialog;
import org.pentaho.di.ui.cluster.dialog.SlaveServerDialog;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.PrintSpool;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.wizard.CreateDatabaseWizard;
import org.pentaho.di.ui.core.dialog.CheckResultDialog;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.EnterOptionsDialog;
import org.pentaho.di.ui.core.dialog.EnterSearchDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.EnterStringsDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.KettlePropertiesFileDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.dialog.ShowBrowserDialog;
import org.pentaho.di.ui.core.dialog.ShowMessageDialog;
import org.pentaho.di.ui.core.dialog.Splash;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TreeMemory;
import org.pentaho.di.ui.imp.ImportRulesDialog;
import org.pentaho.di.ui.job.dialog.JobLoadProgressDialog;
import org.pentaho.di.ui.partition.dialog.PartitionSchemaDialog;
import org.pentaho.di.ui.repository.ILoginCallback;
import org.pentaho.di.ui.repository.RepositoriesDialog;
import org.pentaho.di.ui.repository.RepositorySecurityUI;
import org.pentaho.di.ui.repository.dialog.RepositoryDialogInterface;
import org.pentaho.di.ui.repository.dialog.RepositoryExportProgressDialog;
import org.pentaho.di.ui.repository.dialog.RepositoryImportProgressDialog;
import org.pentaho.di.ui.repository.dialog.RepositoryRevisionBrowserDialogInterface;
import org.pentaho.di.ui.repository.dialog.SelectDirectoryDialog;
import org.pentaho.di.ui.repository.dialog.SelectObjectDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorerCallback;
import org.pentaho.di.ui.repository.repositoryexplorer.UISupportRegistery;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryContent;
import org.pentaho.di.ui.repository.repositoryexplorer.uisupport.BaseRepositoryExplorerUISupport;
import org.pentaho.di.ui.repository.repositoryexplorer.uisupport.ManageUserUISupport;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener.SpoonLifeCycleEvent;
import org.pentaho.di.ui.spoon.TabMapEntry.ObjectType;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegates;
import org.pentaho.di.ui.spoon.dialog.AnalyseImpactProgressDialog;
import org.pentaho.di.ui.spoon.dialog.CheckTransProgressDialog;
import org.pentaho.di.ui.spoon.dialog.LogSettingsDialog;
import org.pentaho.di.ui.spoon.dialog.SaveProgressDialog;
import org.pentaho.di.ui.spoon.dialog.TipsDialog;
import org.pentaho.di.ui.spoon.job.JobGraph;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.di.ui.spoon.wizards.CopyTableWizardPage1;
import org.pentaho.di.ui.spoon.wizards.CopyTableWizardPage2;
import org.pentaho.di.ui.trans.dialog.TransHopDialog;
import org.pentaho.di.ui.trans.dialog.TransLoadProgressDialog;
import org.pentaho.di.ui.util.ThreadGuiResources;
import org.pentaho.di.version.BuildVersion;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulEventSource;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.WaitBoxRunnable;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.components.XulWaitBox;
import org.pentaho.ui.xul.containers.XulMenu;
import org.pentaho.ui.xul.containers.XulMenupopup;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.tags.SwtDeck;
import org.pentaho.ui.xul.swt.tags.SwtMenupopup;
import org.pentaho.ui.xul.swt.tags.SwtToolbarbutton;
import org.pentaho.vfs.ui.VfsFileChooserDialog;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabListener;
import org.pentaho.xul.swt.tab.TabSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class handles the main window of the Spoon graphical transformation
 * editor.
 * 
 * @author Matt
 * @since 16-may-2003, i18n at 07-Feb-2006, redesign 01-Dec-2006
 */
public class Spoon implements AddUndoPositionInterface, TabListener, SpoonInterface, OverwritePrompter, PDIObserver,
    LifeEventHandler, XulEventSource, XulEventHandler {

  private static Class<?> PKG = Spoon.class;

  public static final LoggingObjectInterface loggingObject = new SimpleLoggingObject("Spoon", LoggingObjectType.SPOON,
      null);

  public static final String STRING_TRANSFORMATIONS = BaseMessages.getString(PKG, "Spoon.STRING_TRANSFORMATIONS");

  public static final String STRING_JOBS = BaseMessages.getString(PKG, "Spoon.STRING_JOBS");

  public static final String STRING_BUILDING_BLOCKS = BaseMessages.getString(PKG, "Spoon.STRING_BUILDING_BLOCKS");

  public static final String STRING_ELEMENTS = BaseMessages.getString(PKG, "Spoon.STRING_ELEMENTS");

  public static final String STRING_CONNECTIONS = BaseMessages.getString(PKG, "Spoon.STRING_CONNECTIONS");

  public static final String STRING_STEPS = BaseMessages.getString(PKG, "Spoon.STRING_STEPS");

  public static final String STRING_JOB_ENTRIES = BaseMessages.getString(PKG, "Spoon.STRING_JOB_ENTRIES");

  public static final String STRING_HOPS = BaseMessages.getString(PKG, "Spoon.STRING_HOPS");

  public static final String STRING_PARTITIONS = BaseMessages.getString(PKG, "Spoon.STRING_PARTITIONS");

  public static final String STRING_SLAVES = BaseMessages.getString(PKG, "Spoon.STRING_SLAVES");

  public static final String STRING_CLUSTERS = BaseMessages.getString(PKG, "Spoon.STRING_CLUSTERS");

  public static final String STRING_TRANS_BASE = BaseMessages.getString(PKG, "Spoon.STRING_BASE");

  public static final String STRING_JOB_BASE = BaseMessages.getString(PKG, "Spoon.STRING_JOBENTRY_BASE");

  public static final String STRING_HISTORY = BaseMessages.getString(PKG, "Spoon.STRING_HISTORY");

  public static final String STRING_TRANS_NO_NAME = BaseMessages.getString(PKG, "Spoon.STRING_TRANS_NO_NAME");

  public static final String STRING_JOB_NO_NAME = BaseMessages.getString(PKG, "Spoon.STRING_JOB_NO_NAME");

  public static final String STRING_TRANSFORMATION = BaseMessages.getString(PKG, "Spoon.STRING_TRANSFORMATION");

  public static final String STRING_JOB = BaseMessages.getString(PKG, "Spoon.STRING_JOB");

  private static final String SYNC_TRANS = "sync_trans_name_to_file_name";

  public static final String APP_NAME = BaseMessages.getString(PKG, "Spoon.Application.Name");

  private static Spoon staticSpoon;

  private static LogChannelInterface log;

  private Display display;

  private Shell shell;
  
  private static Splash splash;

  private boolean destroy;

  private SashForm sashform;

  public TabSet tabfolder;

  // THE HANDLERS
  public SpoonDelegates delegates = new SpoonDelegates(this);

  public RowMetaAndData variables = new RowMetaAndData(new RowMeta(), new Object[] {});

  /**
   * These are the arguments that were given at Spoon launch time...
   */
  private String[] arguments;

  private boolean stopped;

  private Cursor cursor_hourglass, cursor_hand;

  public PropsUI props;

  public Repository rep;

  // private RepositorySecurityManager securityManager;

  public RepositoryCapabilities capabilities;

  // Save the last directory saved to for new files
  // TODO: Save the last saved position to the defaulstSaveLocaton
  private RepositoryDirectoryInterface defaultSaveLocation = null;

  // Associate the defaultSaveLocation with a given repository; We should clear this out on a repo change
  private Repository defaultSaveLocationRepository = null;

  /**
   * This contains a map with all the unnamed transformation (just a filename)
   */

  private ToolItem expandAll, collapseAll;

  private CTabItem view, design;

  private Label selectionLabel;

  public Text selectionFilter;

  private org.eclipse.swt.widgets.Menu fileMenus;

  private static final String APPL_TITLE = APP_NAME;

  private static final String STRING_WELCOME_TAB_NAME = BaseMessages.getString(PKG, "Spoon.Title.STRING_WELCOME");

  private static final String FILE_WELCOME_PAGE = Const.safeAppendDirectory(BasePropertyHandler.getProperty(
      "documentationDirBase", "docs/"), BaseMessages.getString(PKG, "Spoon.Title.STRING_DOCUMENT_WELCOME")); // "docs/English/welcome/kettle_document_map.html";

  private static final String UNDO_MENUITEM = "edit-undo"; //$NON-NLS-1$

  private static final String REDO_MENUITEM = "edit-redo"; //$NON-NLS-1$

  private static final String UNDO_UNAVAILABLE = BaseMessages.getString(PKG, "Spoon.Menu.Undo.NotAvailable"); //"Undo : not available \tCTRL-Z" //$NON-NLS-1$

  private static final String REDO_UNAVAILABLE = BaseMessages.getString(PKG, "Spoon.Menu.Redo.NotAvailable"); //"Redo : not available \tCTRL-Y" //$NON-NLS-1$S

  private Composite tabComp;

  private Tree selectionTree;

  private Tree coreObjectsTree;

  private TransExecutionConfiguration transExecutionConfiguration;

  private TransExecutionConfiguration transPreviewExecutionConfiguration;

  private TransExecutionConfiguration transDebugExecutionConfiguration;

  private JobExecutionConfiguration jobExecutionConfiguration;

  // private Menu spoonMenu; // Connections,

  private int coreObjectsState = STATE_CORE_OBJECTS_NONE;

  protected Map<String, FileListener> fileExtensionMap = new HashMap<String, FileListener>();

  protected Map<String, FileListener> fileNodeMap = new HashMap<String, FileListener>();

  private List<Object[]> menuListeners = new ArrayList<Object[]>();

  // loads the lifecycle listeners
  private LifecycleSupport lifecycleSupport = new LifecycleSupport();

  private Composite mainComposite;

  private boolean viewSelected;

  private boolean designSelected;

  private Composite variableComposite;

  private Map<String, String> coreStepToolTipMap;

  private Map<String, String> coreJobToolTipMap;

  private DefaultToolTip toolTip;

  public Map<String, SharedObjects> sharedObjectsFileMap;

  /**
   * We can use this to set a default filter path in the open and save dialogs
   */
  public String lastDirOpened;

  private List<FileListener> fileListeners = new ArrayList<FileListener>();

  private SwtXulLoader xulLoader;

  private XulDomContainer mainSpoonContainer;

  private BindingFactory bf;

  // Menu controllers to modify the main spoon menu
  private List<ISpoonMenuController> menuControllers = new ArrayList<ISpoonMenuController>();

  private XulToolbar mainToolbar;

  private SwtDeck deck;

  public static final String XUL_FILE_MAIN = "ui/spoon.xul";

  private Map<String, XulComponent> menuMap = new HashMap<String, XulComponent>();

  private RepositoriesDialog loginDialog;

  private VfsFileChooserDialog vfsFileChooserDialog;

  /**
   * This is the main procedure for Spoon.
   * 
   * @param a
   *            Arguments are available in the "Get System Info" step.
   */
  public static void main(String[] a) throws KettleException {
    
    try {
      // Bootstrap Kettle
      //
      //  We start Sleak if the VM argument RUN_SLEAK was provided
      Display display = null;
      if (System.getProperties().containsKey("SLEAK")) {
         DeviceData data = new DeviceData();
         data.tracking = true;
         display = new Display(data);
         Sleak sleak = new Sleak();
         sleak.open();
      }
      else {
         display = new Display();
      }

      // The core plugin types don't know about UI classes. Add them in now
      // before the PluginRegistry inits.
      splash = new Splash(display);

      registerUIPluginObjectTypes();

      KettleEnvironment.init();

      List<String> args = new ArrayList<String>(java.util.Arrays.asList(a));

      CommandLineOption[] commandLineOptions = getCommandLineArgs(args);

      initLogging(commandLineOptions);

      PropsUI.init(display, Props.TYPE_PROPERTIES_SPOON);

      CentralLogStore.init(
          PropsUI.getInstance().getMaxNrLinesInLog(), 
          PropsUI.getInstance().getMaxLogLineTimeoutMinutes());

      // remember...

      staticSpoon = new Spoon(display);
      staticSpoon.init(null);
      SpoonFactory.setSpoonInstance(staticSpoon);
      staticSpoon.setDestroy(true);
      GUIFactory.setThreadDialogs(new ThreadGuiResources());

      // listeners
      //
      try {
        staticSpoon.lifecycleSupport.onStart(staticSpoon);
      } catch (LifecycleException e) {
        // if severe, we have to quit
        MessageBox box = new MessageBox(staticSpoon.shell, (e.isSevere() ? SWT.ICON_ERROR : SWT.ICON_WARNING) | SWT.OK);
        box.setMessage(e.getMessage());
        box.open();
      }

      staticSpoon.setArguments(args.toArray(new String[args.size()]));
      staticSpoon.start(commandLineOptions);
    } catch (Throwable t) {
      // avoid calls to Messages i18n method getString() in this block
      // We do this to (hopefully) also catch Out of Memory Exceptions
      //
      t.printStackTrace();
      if (staticSpoon != null) {
        log.logError("Fatal error : " + Const.NVL(t.toString(), Const.NVL(t.getMessage(), "Unknown error"))); //$NON-NLS-1$ //$NON-NLS-2$
        log.logError(Const.getStackTracker(t));
      }
    }

    // Kill all remaining things in this VM!
    System.exit(0);
  }

  private static void initLogging(CommandLineOption[] options) throws KettleException {
    StringBuffer optionLogfile = getCommandLineOption(options, "logfile").getArgument();
    StringBuffer optionLoglevel = getCommandLineOption(options, "level").getArgument();

    // Set default Locale:
    Locale.setDefault(Const.DEFAULT_LOCALE);

    Log4jFileAppender fileAppender;
    if (Const.isEmpty(optionLogfile)) {
      fileAppender = LogWriter.createFileAppender(Const.SPOON_LOG_FILE, false);
    } else {
      fileAppender = LogWriter.createFileAppender(optionLogfile.toString(), true);
    }
    LogWriter.getInstance().addAppender(fileAppender);

    if (log.isBasic()) {
      log.logBasic(BaseMessages.getString(PKG, "Spoon.Log.LoggingToFile") + fileAppender.getFile().toString());// "Logging goes to "
    }

    if (!Const.isEmpty(optionLoglevel)) {
      log.setLogLevel(LogLevel.getLogLevelForCode(optionLoglevel.toString()));
      if (log.isBasic())
        log.logBasic(BaseMessages.getString(PKG, "Spoon.Log.LoggingAtLevel") + log.getLogLevel().getDescription());// "Logging is at level : "
    }
  }

  public Spoon(Display d) {
    this(d, null);
  }

  public Spoon(Repository rep) {
    this(null, rep);
  }

  public Spoon(Display d, Repository rep) {
    log = new LogChannel(APP_NAME);
    SpoonFactory.setSpoonInstance(this);
    setRepository(rep);

    if (d != null) {
      display = d;
      destroy = false;
    } else {
      display = new Display();
      destroy = true;
    }

    props = PropsUI.getInstance();

    sharedObjectsFileMap = new Hashtable<String, SharedObjects>();

    shell = new Shell(display);
    shell.setText(APPL_TITLE);
    staticSpoon = this;

    try {
      JndiUtil.initJNDI();
    } catch (Exception e) {
      new ErrorDialog(shell, "Unable to init simple JNDI", "Unable to init simple JNDI", e);
    }

  }

  /**
   * The core plugin types don't know about UI classes. This method adds those
   * in before initialization.
   * 
   * TODO: create a SpoonLifecycle listener that can notify interested parties
   * of a pre-initialization state so this can happen in those listeners.
   */
  private static void registerUIPluginObjectTypes() {
    RepositoryPluginType.getInstance().addObjectType(RepositoryRevisionBrowserDialogInterface.class,
        "version-browser-classname");
    RepositoryPluginType.getInstance().addObjectType(RepositoryDialogInterface.class, "dialog-classname");

    PluginRegistry.addPluginType(SpoonPluginType.getInstance());

    SpoonPluginType.getInstance().getPluginFolders().add(new PluginFolder("plugins/repositories", false, true));

    LifecyclePluginType.getInstance().getPluginFolders().add(new PluginFolder("plugins/spoon", false, true));
    LifecyclePluginType.getInstance().getPluginFolders().add(new PluginFolder("plugins/repositories", false, true));

  }

  public void init(TransMeta ti) {
    FormLayout layout = new FormLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    shell.setLayout(layout);

    addFileListener(new TransFileListener());

    addFileListener(new JobFileListener());

    // INIT Data structure
    if (ti != null)
      delegates.trans.addTransformation(ti);

    // Load settings in the props
    loadSettings();

    transExecutionConfiguration = new TransExecutionConfiguration();
    transPreviewExecutionConfiguration = new TransExecutionConfiguration();
    transDebugExecutionConfiguration = new TransExecutionConfiguration();

    jobExecutionConfiguration = new JobExecutionConfiguration();

    // Clean out every time we start, auto-loading etc, is not a good idea
    // If they are needed that often, set them in the kettle.properties file
    //
    variables = new RowMetaAndData(new RowMeta(), new Object[] {});

    // props.setLook(shell);

    shell.setImage(GUIResource.getInstance().getImageSpoon());

    cursor_hourglass = new Cursor(display, SWT.CURSOR_WAIT);
    cursor_hand = new Cursor(display, SWT.CURSOR_HAND);

    Composite sashComposite = null;
    MainSpoonPerspective mainPerspective = null;
    try {
      xulLoader = new SwtXulLoader();
      xulLoader.setOuterContext(shell);
      xulLoader.setSettingsManager(XulSpoonSettingsManager.getInstance());

      mainSpoonContainer = xulLoader.loadXul(XUL_FILE_MAIN, new XulSpoonResourceBundle());

      bf = new DefaultBindingFactory();
      bf.setDocument(mainSpoonContainer.getDocumentRoot());
      mainSpoonContainer.addEventHandler(this);
      /* menuBar = (XulMenubar) */mainSpoonContainer.getDocumentRoot().getElementById("spoon-menubar");
      mainToolbar = (XulToolbar) mainSpoonContainer.getDocumentRoot().getElementById("main-toolbar");
      /* canvas = (XulVbox) */mainSpoonContainer.getDocumentRoot().getElementById("trans-job-canvas");
      deck = (SwtDeck) mainSpoonContainer.getDocumentRoot().getElementById("canvas-deck");

      final Composite tempSashComposite = new Composite(shell, SWT.None);
      sashComposite = tempSashComposite;

      mainPerspective = new MainSpoonPerspective(tempSashComposite, tabfolder);
      SpoonPerspectiveManager.getInstance().addPerspective(mainPerspective);

      SpoonPluginManager.getInstance().applyPluginsForContainer("spoon", mainSpoonContainer);

      SpoonPerspectiveManager.getInstance().setDeck(deck);
      SpoonPerspectiveManager.getInstance().setXulDoc(mainSpoonContainer);
      boolean firstBtn = true;
      int y = 0;
      for (SpoonPerspective per : SpoonPerspectiveManager.getInstance().getPerspectives()) {
        String name = per.getDisplayName(LanguageChoice.getInstance().getDefaultLocale());
        InputStream in = per.getPerspectiveIcon();

        final SwtToolbarbutton btn = (SwtToolbarbutton) mainSpoonContainer.getDocumentRoot().createElement(
            "toolbarbutton");
        btn.setType("toggle");
        btn.setLabel(name);
        btn.setTooltiptext(name);
        btn.setOnclick("spoon.loadPerspective(" + y + ")");
        mainToolbar.addChild(btn);
        if (firstBtn) {
          btn.setSelected(true);
          firstBtn = false;
        }
        if (in != null) {
          btn.setImageFromStream(in);
          try {
            in.close();
          } catch (IOException e1) {
          }
        }

        XulVbox box = deck.createVBoxCard();
        box.setId("perspective-" + per.getId());
        box.setFlex(1);
        deck.addChild(box);

        per.getUI().setParent((Composite) box.getManagedObject());
        per.getUI().layout();

        per.addPerspectiveListener(new SpoonPerspectiveListener() {
          public void onActivation() {
            btn.setSelected(true);
          }

          public void onDeactication() {
            btn.setSelected(false);
          }
        });
        y++;
      }
      deck.setSelectedIndex(0);

    } catch (IllegalArgumentException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (XulException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    // addBar();

    // Set the shell size, based upon previous time...
    WindowProperty winprop = props.getScreen(APPL_TITLE);
    if (winprop != null)
      winprop.setShell(shell);
    else {
      shell.pack();
      shell.setMaximized(true); // Default = maximized!
    }

    // In case someone dares to press the [X] in the corner ;-)
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        try {
          e.doit = quitFile();
        } catch (KettleException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        SpoonPluginManager.getInstance().notifyLifecycleListeners(SpoonLifeCycleEvent.SHUTDOWN);
      }
    });

    layout = new FormLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;

    GridData data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.grabExcessVerticalSpace = true;
    data.verticalAlignment = SWT.FILL;
    data.horizontalAlignment = SWT.FILL;
    sashComposite.setLayoutData(data);

    sashComposite.setLayout(layout);

    sashform = new SashForm(sashComposite, SWT.HORIZONTAL);

    FormData fdSash = new FormData();
    fdSash.left = new FormAttachment(0, 0);
    // fdSash.top = new FormAttachment((org.eclipse.swt.widgets.ToolBar)
    // toolbar.getNativeObject(), 0);
    fdSash.top = new FormAttachment(0, 0);
    fdSash.bottom = new FormAttachment(100, 0);
    fdSash.right = new FormAttachment(100, 0);
    sashform.setLayoutData(fdSash);

    createPopupMenus();
    addTree();
    addTabs();
    mainPerspective.setTabset(this.tabfolder);
    ((Composite) deck.getManagedObject()).layout(true, true);

    SpoonPluginManager.getInstance().notifyLifecycleListeners(SpoonLifeCycleEvent.STARTUP);

    // Add a browser widget
    if (props.showWelcomePageOnStartup()) {
      showWelcomePage();
    }

    // Allow data to be copied or moved to the drop target
    int operations = DND.DROP_COPY | DND.DROP_DEFAULT;
    DropTarget target = new DropTarget(shell, operations);

    // Receive data in File format
    final FileTransfer fileTransfer = FileTransfer.getInstance();
    Transfer[] types = new Transfer[] { fileTransfer };
    target.setTransfer(types);

    target.addDropListener(new DropTargetListener() {
      public void dragEnter(DropTargetEvent event) {
        if (event.detail == DND.DROP_DEFAULT) {
          if ((event.operations & DND.DROP_COPY) != 0) {
            event.detail = DND.DROP_COPY;
          } else {
            event.detail = DND.DROP_NONE;
          }
        }
      }

      public void dragOver(DropTargetEvent event) {
        event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
      }

      public void dragOperationChanged(DropTargetEvent event) {
        if (event.detail == DND.DROP_DEFAULT) {
          if ((event.operations & DND.DROP_COPY) != 0) {
            event.detail = DND.DROP_COPY;
          } else {
            event.detail = DND.DROP_NONE;
          }
        }
      }

      public void dragLeave(DropTargetEvent event) {
      }

      public void dropAccept(DropTargetEvent event) {
      }

      public void drop(DropTargetEvent event) {
        if (fileTransfer.isSupportedType(event.currentDataType)) {
          String[] files = (String[]) event.data;
          for (int i = 0; i < files.length; i++) {
            openFile(files[i], false);
          }
        }
      }
    });
  }

  public XulDomContainer getMainSpoonContainer() {
    return mainSpoonContainer;
  }

  public void loadPerspective(String id) {
    List<SpoonPerspective> perspectives = SpoonPerspectiveManager.getInstance().getPerspectives();
    for (int pos = 0; pos < perspectives.size(); pos++) {
      SpoonPerspective perspective = perspectives.get(pos);
      if (perspective.getId().equals(id)) {
        loadPerspective(pos);
        return;
      }
    }
  }

  public void loadPerspective(int pos) {
    try {
      SpoonPerspectiveManager.getInstance().activatePerspective(
          SpoonPerspectiveManager.getInstance().getPerspectives().get(pos).getClass());
    } catch (KettleException e) {
      log.logError("Error loading perspective", e);
    }
  }

  public Shell getShell() {
    return shell;
  }

  public static Spoon getInstance() {
    return staticSpoon;
  }

  public VfsFileChooserDialog getVfsFileChooserDialog(FileObject rootFile, FileObject initialFile) {
    if (vfsFileChooserDialog == null) {
      vfsFileChooserDialog = new VfsFileChooserDialog(shell, rootFile, initialFile);
    }
    vfsFileChooserDialog.setRootFile(rootFile);
    vfsFileChooserDialog.setInitialFile(initialFile);
    return vfsFileChooserDialog;
  }
  
  public void closeFile() {
    TransMeta transMeta = getActiveTransformation();
    if (transMeta != null) {
      // If a transformation is the current active tab, close it
      delegates.trans.closeTransformation(transMeta);
    } else {
      // Otherwise try to find the current open job and close it
      JobMeta jobMeta = getActiveJob();
      if (jobMeta != null)
        delegates.jobs.closeJob(jobMeta);
    }
  }

  public void closeAllFiles() {
    TransMeta transMeta = getActiveTransformation();
    JobMeta jobMeta = getActiveJob();
    while (jobMeta != null || transMeta != null) {
      closeFile();
      transMeta = getActiveTransformation();
      jobMeta = getActiveJob();
    }
  }

  public void closeSpoonBrowser() {
    TabMapEntry browserTab = delegates.tabs.findTabMapEntry(STRING_WELCOME_TAB_NAME, ObjectType.BROWSER);

    if (browserTab != null) {
      delegates.tabs.removeTab(browserTab);
    }
  }

  /**
   * Search the transformation meta-data.
   * 
   */
  public void searchMetaData() {
    TransMeta[] transMetas = getLoadedTransformations();
    JobMeta[] jobMetas = getLoadedJobs();
    if ((transMetas == null || transMetas.length == 0) && (jobMetas == null || jobMetas.length == 0))
      return;

    EnterSearchDialog esd = new EnterSearchDialog(shell);
    if (!esd.open()) {
      return;
    }

    List<Object[]> rows = new ArrayList<Object[]>();

    for (int t = 0; t < transMetas.length; t++) {
      TransMeta transMeta = transMetas[t];
      String filterString = esd.getFilterString();
      String filter = filterString;
      if (filter != null)
        filter = filter.toUpperCase();

      List<StringSearchResult> stringList = transMeta.getStringList(esd.isSearchingSteps(), esd.isSearchingDatabases(),
          esd.isSearchingNotes());
      for (int i = 0; i < stringList.size(); i++) {
        StringSearchResult result = (StringSearchResult) stringList.get(i);

        boolean add = Const.isEmpty(filter);
        if (filter != null && result.getString().toUpperCase().indexOf(filter) >= 0)
          add = true;
        if (filter != null && result.getFieldName().toUpperCase().indexOf(filter) >= 0)
          add = true;
        if (filter != null && result.getParentObject().toString().toUpperCase().indexOf(filter) >= 0)
          add = true;
        if (filter != null && result.getGrandParentObject().toString().toUpperCase().indexOf(filter) >= 0)
          add = true;

        if (add)
          rows.add(result.toRow());
      }
    }

    for (int t = 0; t < jobMetas.length; t++) {
      JobMeta jobMeta = jobMetas[t];
      String filterString = esd.getFilterString();
      String filter = filterString;
      if (filter != null)
        filter = filter.toUpperCase();

      List<StringSearchResult> stringList = jobMeta.getStringList(esd.isSearchingSteps(), esd.isSearchingDatabases(),
          esd.isSearchingNotes());
      for (StringSearchResult result : stringList) {
        boolean add = Const.isEmpty(filter);
        if (filter != null && result.getString().toUpperCase().indexOf(filter) >= 0)
          add = true;
        if (filter != null && result.getFieldName().toUpperCase().indexOf(filter) >= 0)
          add = true;
        if (filter != null && result.getParentObject().toString().toUpperCase().indexOf(filter) >= 0)
          add = true;
        if (filter != null && result.getGrandParentObject().toString().toUpperCase().indexOf(filter) >= 0)
          add = true;

        if (add)
          rows.add(result.toRow());
      }
    }

    if (rows.size() != 0) {
      PreviewRowsDialog prd = new PreviewRowsDialog(shell, Variables.getADefaultVariableSpace(), SWT.NONE, BaseMessages
          .getString(PKG, "Spoon.StringSearchResult.Subtitle"), StringSearchResult.getResultRowMeta(), rows);
      String title = BaseMessages.getString(PKG, "Spoon.StringSearchResult.Title");
      String message = BaseMessages.getString(PKG, "Spoon.StringSearchResult.Message");
      prd.setTitleMessage(title, message);
      prd.open();
    } else {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
      mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.NothingFound.Message"));
      mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.NothingFound.Title")); // Sorry!
      mb.open();
    }
  }

  public void showArguments() {

    RowMetaAndData allArgs = new RowMetaAndData();

    for (int ii = 0; ii < arguments.length; ++ii) {
      allArgs.addValue(new ValueMeta(Props.STRING_ARGUMENT_NAME_PREFIX + (1 + ii), ValueMetaInterface.TYPE_STRING),
          arguments[ii]);
    }

    // Now ask the use for more info on these!
    EnterStringsDialog esd = new EnterStringsDialog(shell, SWT.NONE, allArgs);
    esd.setTitle(BaseMessages.getString(PKG, "Spoon.Dialog.ShowArguments.Title"));
    esd.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.ShowArguments.Message"));
    esd.setReadOnly(true);
    esd.setShellImage(GUIResource.getInstance().getImageLogoSmall());
    esd.open();
  }

  private void fillVariables(RowMetaAndData vars) {
    TransMeta[] transMetas = getLoadedTransformations();
    JobMeta[] jobMetas = getLoadedJobs();
    if ((transMetas == null || transMetas.length == 0) && (jobMetas == null || jobMetas.length == 0))
      return;

    Properties sp = new Properties();
    sp.putAll(System.getProperties());

    VariableSpace space = Variables.getADefaultVariableSpace();
    String keys[] = space.listVariables();
    for (int i = 0; i < keys.length; i++) {
      sp.put(keys[i], space.getVariable(keys[i]));
    }

    for (int t = 0; t < transMetas.length; t++) {
      TransMeta transMeta = transMetas[t];

      List<String> list = transMeta.getUsedVariables();
      for (int i = 0; i < list.size(); i++) {
        String varName = list.get(i);
        String varValue = sp.getProperty(varName, "");
        if (vars.getRowMeta().indexOfValue(varName) < 0 && !varName.startsWith(Const.INTERNAL_VARIABLE_PREFIX)) {
          vars.addValue(new ValueMeta(varName, ValueMetaInterface.TYPE_STRING), varValue);
        }
      }
    }

    for (int t = 0; t < jobMetas.length; t++) {
      JobMeta jobMeta = jobMetas[t];

      List<String> list = jobMeta.getUsedVariables();
      for (int i = 0; i < list.size(); i++) {
        String varName = list.get(i);
        String varValue = sp.getProperty(varName, "");
        if (vars.getRowMeta().indexOfValue(varName) < 0 && !varName.startsWith(Const.INTERNAL_VARIABLE_PREFIX)) {
          vars.addValue(new ValueMeta(varName, ValueMetaInterface.TYPE_STRING), varValue);
        }
      }
    }
  }

  public void setVariables() {
    fillVariables(variables);

    // Now ask the use for more info on these!
    EnterStringsDialog esd = new EnterStringsDialog(shell, SWT.NONE, variables);
    esd.setTitle(BaseMessages.getString(PKG, "Spoon.Dialog.SetVariables.Title"));
    esd.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.SetVariables.Message"));
    esd.setReadOnly(false);
    esd.setShellImage(GUIResource.getInstance().getImageVariable());
    if (esd.open() != null) {
      applyVariables();
    }
  }

  public void applyVariables() {
    for (int i = 0; i < variables.size(); i++) {
      try {
        String name = variables.getValueMeta(i).getName();
        String value = variables.getString(i, "");

        applyVariableToAllLoadedObjects(name, value);
      } catch (KettleValueException e) {
        // Just eat the exception. getString() should never give an
        // exception.
      }
    }
  }

  public void applyVariableToAllLoadedObjects(String name, String value) {
    // We want to insert the variables into all loaded jobs and
    // transformations
    //
    for (TransMeta transMeta : getLoadedTransformations()) {
      transMeta.setVariable(name, Const.NVL(value, ""));
    }
    for (JobMeta jobMeta : getLoadedJobs()) {
      jobMeta.setVariable(name, Const.NVL(value, ""));
    }

    // Not only that, we also want to set the variables in the
    // execution configurations...
    //
    transExecutionConfiguration.getVariables().put(name, value);
    jobExecutionConfiguration.getVariables().put(name, value);
    transDebugExecutionConfiguration.getVariables().put(name, value);
  }

  public void showVariables() {
    fillVariables(variables);

    // Now ask the use for more info on these!
    EnterStringsDialog esd = new EnterStringsDialog(shell, SWT.NONE, variables);
    esd.setTitle(BaseMessages.getString(PKG, "Spoon.Dialog.ShowVariables.Title"));
    esd.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.ShowVariables.Message"));
    esd.setReadOnly(true);
    esd.setShellImage(GUIResource.getInstance().getImageVariable());
    esd.open();
  }

  public void open() {
    shell.open();
    mainComposite.setRedraw(true);
    mainComposite.setVisible(false);
    mainComposite.setVisible(true);
    mainComposite.redraw();

    // Perhaps the transformation contains elements at startup?
    refreshTree(); // Do a complete refresh then...

    setShellText();
  }

  public boolean readAndDispatch() {
    return display.readAndDispatch();
  }

  /**
   * @return check whether or not the application was stopped.
   */
  public boolean isStopped() {
    return stopped;
  }

  /**
   * @param stopped
   *            True to stop this application.
   */
  public void setStopped(boolean stopped) {
    this.stopped = stopped;
  }

  /**
   * @param destroy
   *            Whether or not to distroy the display.
   */
  public void setDestroy(boolean destroy) {
    this.destroy = destroy;
  }

  /**
   * @return Returns whether or not we should distroy the display.
   */
  public boolean doDestroy() {
    return destroy;
  }

  /**
   * @param arguments
   *            The arguments to set.
   */
  public void setArguments(String[] arguments) {
    this.arguments = arguments;
  }

  /**
   * @return Returns the arguments.
   */
  public String[] getArguments() {
    return arguments;
  }

  public synchronized void dispose() {
    setStopped(true);
    cursor_hand.dispose();
    cursor_hourglass.dispose();

    if (destroy && !display.isDisposed()){
      try{
        display.dispose();
      } catch(SWTException e){
        // dispose errors
      }
    }
  }

  public boolean isDisposed() {
    return display.isDisposed();
  }

  public void sleep() {
    display.sleep();
  }

  public void undoAction() {
    undoAction(getActiveUndoInterface());
  }

  public void redoAction() {
    redoAction(getActiveUndoInterface());
  }

  /**
   * It's called copySteps, but the job entries also arrive at this location
   */
  public void copySteps() {
    TransMeta transMeta = getActiveTransformation();
    if (transMeta != null) {
      copySelected(transMeta, transMeta.getSelectedSteps(), transMeta.getSelectedNotes());
    }
    JobMeta jobMeta = getActiveJob();
    if (jobMeta != null) {
      copyJobentries();
    }
  }

  public void copyJobentries() {
    JobMeta jobMeta = getActiveJob();
    if (jobMeta != null) {
      delegates.jobs.copyJobEntries(jobMeta, jobMeta.getSelectedEntries());
    }
  }

  public void copy() {
    TransMeta transMeta = getActiveTransformation();
    JobMeta jobMeta = getActiveJob();
    boolean transActive = transMeta != null;
    boolean jobActive = jobMeta != null;

    if (transActive) {
      if (transMeta.getSelectedSteps().size() > 0) {
        copySteps();
      } else {
        copyTransformation();
      }
    } else if (jobActive) {
      if (jobMeta.getSelectedEntries().size() > 0) {
        copyJobentries();
      } else {
        copyJob();
      }
    }
  }

  public void copyFile() {
    TransMeta transMeta = getActiveTransformation();
    JobMeta jobMeta = getActiveJob();
    boolean transActive = transMeta != null;
    boolean jobActive = jobMeta != null;

    if (transActive) {
      copyTransformation();
    } else if (jobActive) {
      copyJob();
    }
  }

  public void cut() {
    TransMeta transMeta = getActiveTransformation();
    JobMeta jobMeta = getActiveJob();
    boolean transActive = transMeta != null;
    boolean jobActive = jobMeta != null;

    if (transActive) {
      List<StepMeta> stepMetas = transMeta.getSelectedSteps();
      if (stepMetas != null && stepMetas.size() > 0) {
        copySteps();
        for (StepMeta stepMeta : stepMetas) {
          delStep(transMeta, stepMeta);
        }
      }
    } else if (jobActive) {
      List<JobEntryCopy> jobEntryCopies = jobMeta.getSelectedEntries();
      if (jobEntryCopies != null && jobEntryCopies.size() > 0) {
        copyJobentries();
        for (JobEntryCopy jobEntryCopy : jobEntryCopies) {
          deleteJobEntryCopies(jobMeta, jobEntryCopy);
        }
      }
    }
  }

  public void createPopupMenus() {

    try {
      menuMap.put("trans-class", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("trans-class")); //$NON-NLS-1$
      menuMap.put("trans-class-new", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("trans-class-new")); //$NON-NLS-1$
      menuMap.put("job-class", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("job-class")); //$NON-NLS-1$
      menuMap.put("trans-hop-class", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("trans-hop-class")); //$NON-NLS-1$
      menuMap.put("database-class", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("database-class")); //$NON-NLS-1$
      menuMap.put("partition-schema-class", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("partition-schema-class")); //$NON-NLS-1$
      menuMap.put("cluster-schema-class", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("cluster-schema-class")); //$NON-NLS-1$
      menuMap.put("slave-cluster-class", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("slave-cluster-class")); //$NON-NLS-1$
      menuMap.put("trans-inst", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("trans-inst")); //$NON-NLS-1$
      menuMap.put("job-inst", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("job-inst")); //$NON-NLS-1$
      menuMap.put("step-plugin", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("step-plugin")); //$NON-NLS-1$
      menuMap.put("database-inst", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("database-inst")); //$NON-NLS-1$
      menuMap.put("step-inst", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("step-inst")); //$NON-NLS-1$
      menuMap.put("job-entry-copy-inst", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("job-entry-copy-inst")); //$NON-NLS-1$
      menuMap.put("trans-hop-inst", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("trans-hop-inst")); //$NON-NLS-1$
      menuMap.put("partition-schema-inst", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("partition-schema-inst")); //$NON-NLS-1$
      menuMap.put("cluster-schema-inst", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("cluster-schema-inst")); //$NON-NLS-1$
      menuMap.put("slave-server-inst", mainSpoonContainer //$NON-NLS-1$
          .getDocumentRoot().getElementById("slave-server-inst")); //$NON-NLS-1$
    } catch (Throwable t) {
      t.printStackTrace();
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Title"), //$NON-NLS-1$
          BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_MAIN), //$NON-NLS-1$
          new Exception(t));
    }

    addMenuLast();
  }

  public void executeTransformation() {
    executeTransformation(getActiveTransformation(), true, false, false, false, false, null, false);
  }

  public void previewTransformation() {
    executeTransformation(getActiveTransformation(), true, false, false, true, false, null, true);
  }

  public void debugTransformation() {
    executeTransformation(getActiveTransformation(), true, false, false, false, true, null, true);
  }

  public void checkTrans() {
    checkTrans(getActiveTransformation());
  }

  public void analyseImpact() {
    analyseImpact(getActiveTransformation());
  }

  public void showLastImpactAnalyses() {
    showLastImpactAnalyses(getActiveTransformation());
  }

  public void showLastTransPreview() {
    TransGraph transGraph = getActiveTransGraph();
    if (transGraph != null) {
      transGraph.showLastPreviewResults();
    }
  }

  public void showExecutionResults() {
    TransGraph transGraph = getActiveTransGraph();
    if (transGraph != null) {
      transGraph.showExecutionResults();
      enableMenus();
    } else {
      JobGraph jobGraph = getActiveJobGraph();
      if (jobGraph != null) {
        jobGraph.showExecutionResults();
        enableMenus();
      }
    }
  }

  public boolean isExecutionResultsPaneVisible() {
    TransGraph transGraph = getActiveTransGraph();
    return (transGraph != null) && (transGraph.isExecutionResultsPaneVisible());
  }

  public void copyTransformation() {
    copyTransformation(getActiveTransformation());
  }

  public void copyTransformationImage() {
    copyTransformationImage(getActiveTransformation());
  }

  public boolean editTransformationProperties() {
    return TransGraph.editProperties(getActiveTransformation(), this, rep, true);
  }

  public boolean editProperties() {
    if (getActiveTransformation() != null) {
      return editTransformationProperties();
    } else if (getActiveJob() != null) {
      return editJobProperties("job-settings");
    }
    // no properties were edited, so no cancel was clicked
    return true;
  }

  public void executeJob() {
    executeJob(getActiveJob(), true, false, null, false, null, 0);
  }

  public void copyJob() {
    copyJob(getActiveJob());
  }

  public void showTips() {
    new TipsDialog(shell).open();
  }

  public void showWelcomePage() {
    try {
      LocationListener listener = new LocationListener() {
        public void changing(LocationEvent event) {

//          System.out.println("Changing to: " + event.location);

          // file:///home/matt/svn/kettle/trunk/docs/English/welcome/samples/transformations/
          //
          if (event.location.contains("samples/transformations") || event.location.contains("samples/jobs")
              || event.location.contains("samples/mapping")) {
            try {
              FileObject fileObject = KettleVFS.getFileObject(event.location);
              if (fileObject.exists()) {
                if (event.location.endsWith(".ktr") || event.location.endsWith(".kjb")) {
                  openFile(event.location, false);
                } else {
                  lastDirOpened = KettleVFS.getFilename(fileObject);
                  openFile(true);
                }
                event.doit = false;
              }
            } catch (Exception e) {
              log.logError("Error handling samples location: " + event.location, e);
            }
          }
        }

        public void changed(LocationEvent event) {
          // System.out.println("Changed to: " + event.location);
        }
      };

      // see if we are in webstart mode
      String webstartRoot = System.getProperty("spoon.webstartroot");
      if (webstartRoot != null) {
        URL url = new URL(webstartRoot + '/' + FILE_WELCOME_PAGE);
        addSpoonBrowser(STRING_WELCOME_TAB_NAME, url.toString(), listener); // ./docs/English/tips/index.htm
      } else {
        // see if we can find the welcome file on the file system
        File file = new File(FILE_WELCOME_PAGE);
        if (file.exists()) {
          addSpoonBrowser(STRING_WELCOME_TAB_NAME, file.toURI().toURL().toString(), listener); // ./docs/English/tips/index.htm
        }
      }
    } catch (MalformedURLException e1) {
      log.logError(Const.getStackTracker(e1));
    }
  }

  public void addMenuLast() {
    org.pentaho.ui.xul.dom.Document doc = (org.pentaho.ui.xul.dom.Document) mainSpoonContainer.getDocumentRoot();
    XulMenupopup recentFilesPopup = (XulMenupopup) doc.getElementById("file-open-recent-popup");

    int max = recentFilesPopup.getChildNodes().size();
    for (int i = max - 1; i >= 0; i--) {
      XulComponent mi = recentFilesPopup.getChildNodes().get(i);
      mi.getParent().removeChild(mi);
    }

    // Previously loaded files...
    List<LastUsedFile> lastUsedFiles = props.getLastUsedFiles();
    for (int i = 0; i < lastUsedFiles.size(); i++) {
      final LastUsedFile lastUsedFile = lastUsedFiles.get(i);

      char chr = (char) ('1' + i);
      String accessKey = "ctrl-" + chr; //$NON-NLS-1$
      String accessText = "CTRL-" + chr; //$NON-NLS-1$
      String text = lastUsedFile.toString();
      String id = "last-file-" + i; //$NON-NLS-1$

      if (i > 9) {
        accessKey = null;
        accessText = null;
      }

      XulMenuitem miFileLast = ((SwtMenupopup) recentFilesPopup).createNewMenuitem();

      // shorten the filename if necessary
      int targetLength = 40;
      if (text.length() > targetLength) {
        int lastSep = text.replace('\\', '/').lastIndexOf('/');
        if (lastSep != -1) {
          String fileName = "..." + text.substring(lastSep);
          if (fileName.length() < targetLength) {
            // add the start of the file path
            int leadSize = targetLength - fileName.length();
            text = text.substring(0, leadSize) + fileName;
          } else {
            text = fileName;
          }
        }
      }

      miFileLast.setLabel(text);
      miFileLast.setId(id);
      miFileLast.setAcceltext(accessText);
      miFileLast.setAccesskey(accessKey);

      if (lastUsedFile.isTransformation()) {
        MenuItem item = (MenuItem) miFileLast.getManagedObject();
        item.setImage(GUIResource.getInstance().getImageTransGraph());
      } else if (lastUsedFile.isJob()) {
        MenuItem item = (MenuItem) miFileLast.getManagedObject();
        item.setImage(GUIResource.getInstance().getImageJobGraph());
      }
      miFileLast.setCommand("spoon.lastFileSelect('" + i + "')");
    }
  }

  public void lastFileSelect(String id) {

    int idx = Integer.parseInt(id);
    List<LastUsedFile> lastUsedFiles = props.getLastUsedFiles();
    final LastUsedFile lastUsedFile = (LastUsedFile) lastUsedFiles.get(idx);

    // If the file comes from a repository and it's not the same as
    // the one we're connected to, ask for a username/password!
    //
    if (lastUsedFile.isSourceRepository()
        && (rep == null || !rep.getName().equalsIgnoreCase(lastUsedFile.getRepositoryName()))) {
      // Ask for a username password to get the required repository access
      //
      loginDialog = new RepositoriesDialog(shell, lastUsedFile.getRepositoryName(), new ILoginCallback() {

        public void onSuccess(Repository repository) {
          // Close the previous connection...
          if (rep != null) {
            rep.disconnect();
            SpoonPluginManager.getInstance().notifyLifecycleListeners(SpoonLifeCycleEvent.REPOSITORY_DISCONNECTED);
          }
          setRepository(repository);
          try {
            loadLastUsedFile(lastUsedFile, rep == null ? null : rep.getName());
            addMenuLast();
          } catch (KettleException ke) {
            // "Error loading transformation", "I was unable to load this
            // transformation from the
            // XML file because of an error"
            new ErrorDialog(loginDialog.getShell(), BaseMessages.getString(PKG,
                "Spoon.Dialog.LoadTransformationError.Title"), BaseMessages.getString(PKG,
                "Spoon.Dialog.LoadTransformationError.Message"), ke);
          }
        }

        public void onError(Throwable t) {
          new ErrorDialog(loginDialog.getShell(), BaseMessages.getString(PKG, "Spoon.Dialog.LoginFailed.Title"),
              BaseMessages.getString(PKG, "Spoon.Dialog.LoginFailed.Message"), t);
          
        }

        public void onCancel() {
        }
      });
      loginDialog.show();
    } else if (!lastUsedFile.isSourceRepository()) {
      // This file must have been on the file system.
      openFile(lastUsedFile.getFilename(), false);
    } else {
      // read from a repository...
      //
      try {
        loadLastUsedFile(lastUsedFile, rep == null ? null : rep.getName());
        addMenuLast();
      } catch (KettleException ke) {
        // "Error loading transformation", "I was unable to load this
        // transformation from the
        // XML file because of an error"
        new ErrorDialog(loginDialog.getShell(), BaseMessages.getString(PKG,
            "Spoon.Dialog.LoadTransformationError.Title"), BaseMessages.getString(PKG,
            "Spoon.Dialog.LoadTransformationError.Message"), ke);
      }
    }
  }

  private static final String STRING_SPOON_MAIN_TREE = BaseMessages.getString(PKG, "Spoon.MainTree.Label");

  private static final String STRING_SPOON_CORE_OBJECTS_TREE = BaseMessages.getString(PKG,
      "Spoon.CoreObjectsTree.Label");

  public static final String XML_TAG_TRANSFORMATION_STEPS = "transformation-steps";

  public static final String XML_TAG_JOB_JOB_ENTRIES = "job-jobentries";

  private void addTree() {
    // Color background = GUIResource.getInstance().getColorLightPentaho();
    mainComposite = new Composite(sashform, SWT.BORDER);
    mainComposite.setLayout(new FormLayout());

    // int mainMargin = 4;

    // TODO: add i18n keys
    //
    Label sep0 = new Label(mainComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
    sep0.setBackground(GUIResource.getInstance().getColorWhite());
    FormData fdSep0 = new FormData();
    fdSep0.left = new FormAttachment(0, 0);
    fdSep0.right = new FormAttachment(100, 0);
    fdSep0.top = new FormAttachment(0, 0);
    sep0.setLayoutData(fdSep0);
    Control lastControl = sep0;

    // empty panel to correct background color.
    Composite tabWrapper = new Composite(mainComposite, SWT.NONE);
    tabWrapper.setLayout(new FormLayout());
    tabWrapper.setBackground(GUIResource.getInstance().getColorWhite());

    FormData fdTabWrapper = new FormData();
    fdTabWrapper.left = new FormAttachment(0, 0);
    fdTabWrapper.top = new FormAttachment(sep0, 0);
    fdTabWrapper.right = new FormAttachment(100, 0);
    tabWrapper.setLayoutData(fdTabWrapper);

    CTabFolder tabFolder = new CTabFolder(tabWrapper, SWT.HORIZONTAL | SWT.FLAT);
    tabFolder.setSimple(false); // Set simple what!!?? Well it sets the style of
    // the tab folder to simple or stylish (curvy
    // borders)
    tabFolder.setBackground(GUIResource.getInstance().getColorWhite());
    tabFolder.setBorderVisible(false);
    tabFolder.setSelectionBackground(new Color[] { display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW),
        display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW), }, new int[] { 55, }, true);

    FormData fdTab = new FormData();
    fdTab.left = new FormAttachment(0, 0);
    fdTab.top = new FormAttachment(sep0, 0);
    fdTab.right = new FormAttachment(100, 0);
    fdTab.height = 0;
    tabFolder.setLayoutData(fdTab);

    view = new CTabItem(tabFolder, SWT.NONE);
    view.setControl(new Composite(tabFolder, SWT.NONE));
    view.setText(STRING_SPOON_MAIN_TREE);
    view.setImage(GUIResource.getInstance().getImageExploreSolutionSmall());

    design = new CTabItem(tabFolder, SWT.NONE);
    design.setText(STRING_SPOON_CORE_OBJECTS_TREE);
    design.setControl(new Composite(tabFolder, SWT.NONE));
    design.setImage(GUIResource.getInstance().getImageEditSmall());

    lastControl = tabWrapper;

    Label sep3 = new Label(mainComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
    sep3.setBackground(GUIResource.getInstance().getColorWhite());
    FormData fdSep3 = new FormData();
    fdSep3.left = new FormAttachment(0, 0);
    fdSep3.right = new FormAttachment(100, 0);
    fdSep3.top = new FormAttachment(lastControl, 0);
    sep3.setLayoutData(fdSep3);
    lastControl = sep3;

    selectionLabel = new Label(mainComposite, SWT.HORIZONTAL);
    selectionLabel.setFont(GUIResource.getInstance().getFontMedium());
    FormData fdsLabel = new FormData();
    fdsLabel.left = new FormAttachment(0, 0);
    fdsLabel.top = new FormAttachment(lastControl, 5);
    selectionLabel.setLayoutData(fdsLabel);
    lastControl = selectionLabel;

    ToolBar treeTb = new ToolBar(mainComposite, SWT.HORIZONTAL | SWT.FLAT | SWT.BORDER);
    expandAll = new ToolItem(treeTb, SWT.PUSH);
    expandAll.setImage(GUIResource.getInstance().getImageExpandAll());
    collapseAll = new ToolItem(treeTb, SWT.PUSH);
    collapseAll.setImage(GUIResource.getInstance().getImageCollapseAll());

    FormData fdTreeToolbar = new FormData();
    fdTreeToolbar.top = new FormAttachment(sep3, 0);
    fdTreeToolbar.right = new FormAttachment(95, 5);
    treeTb.setLayoutData(fdTreeToolbar);
    lastControl = treeTb;

    selectionFilter = new Text(mainComposite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    selectionFilter.setFont(GUIResource.getInstance().getFontSmall());
    selectionFilter.setToolTipText(BaseMessages.getString(PKG, "Spoon.SelectionFilter.Tooltip"));
    FormData fdSelectionFilter = new FormData();
    fdSelectionFilter.top = new FormAttachment(lastControl,
        -(GUIResource.getInstance().getImageExpandAll().getBounds().height + 5));
    fdSelectionFilter.right = new FormAttachment(95, -55);
    selectionFilter.setLayoutData(fdSelectionFilter);

    selectionFilter.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent arg0) {
        if (coreObjectsTree != null && !coreObjectsTree.isDisposed()) {
          previousShowTrans = false;
          previousShowJob = false;
          refreshCoreObjects();
          if(!Const.isEmpty(selectionFilter.getText())) {
        	  tidyBranches(coreObjectsTree.getItems(), true); // expand all
          } else { // no filter: collapse all
        	  tidyBranches(coreObjectsTree.getItems(), false);
          }
        }
        if (selectionTree != null && !selectionTree.isDisposed()) {
          refreshTree();
          if(!Const.isEmpty(selectionFilter.getText())) {
        	  tidyBranches(selectionTree.getItems(), true); // expand all
          } else { // no filter: collapse all
        	  tidyBranches(selectionTree.getItems(), false);
          }
          selectionFilter.setFocus();
        }
      }
    });

    lastControl = treeTb;

    expandAll.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        if (designSelected) {
          tidyBranches(coreObjectsTree.getItems(), true);
        }
        if (viewSelected) {
          tidyBranches(selectionTree.getItems(), true);
        }
      }
    });

    collapseAll.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        if (designSelected) {
          tidyBranches(coreObjectsTree.getItems(), false);
        }
        if (viewSelected) {
          tidyBranches(selectionTree.getItems(), false);
        }
      }
    });

    tabFolder.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent arg0) {
        if (arg0.item == view) {
          setViewMode();
        } else {
          setDesignMode();
        }
      }
    });

    Label sep4 = new Label(mainComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
    sep4.setBackground(GUIResource.getInstance().getColorWhite());
    FormData fdSep4 = new FormData();
    fdSep4.left = new FormAttachment(0, 0);
    fdSep4.right = new FormAttachment(100, 0);
    fdSep4.top = new FormAttachment(lastControl, 5);
    sep4.setLayoutData(fdSep4);
    lastControl = sep4;

    variableComposite = new Composite(mainComposite, SWT.NONE);
    variableComposite.setBackground(GUIResource.getInstance().getColorBackground());
    variableComposite.setLayout(new FillLayout());
    FormData fdVariableComposite = new FormData();
    fdVariableComposite.left = new FormAttachment(0, 0);
    fdVariableComposite.right = new FormAttachment(100, 0);
    fdVariableComposite.top = new FormAttachment(lastControl, 0);
    fdVariableComposite.bottom = new FormAttachment(100, 0);
    variableComposite.setLayoutData(fdVariableComposite);
    lastControl = variableComposite;

    disposeVariableComposite(true, false, false, false);

    coreStepToolTipMap = new Hashtable<String, String>();
    coreJobToolTipMap = new Hashtable<String, String>();

    addDefaultKeyListeners(tabFolder);
    addDefaultKeyListeners(mainComposite);
  }
  
  public void addDefaultKeyListeners(Control control) {
    control.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        // CTRL-W or CTRL-F4 : close tab
        //
        if ((e.keyCode=='w' && (e.stateMask & SWT.CONTROL) != 0 ) ||
            (e.keyCode==SWT.F4 && (e.stateMask & SWT.CONTROL) != 0 )
            )
        {
            closeFile();
        }
      }
    });
  }

  public boolean setViewMode() {
    if (viewSelected)
      return true;
    selectionFilter.setText(""); //reset filter when switched to view
    disposeVariableComposite(true, false, false, false);
    refreshTree();
    return false;
  }

  public boolean setDesignMode() {
    if (designSelected)
      return true;
    selectionFilter.setText(""); //reset filter when switched to design
    disposeVariableComposite(false, false, true, false);
    refreshCoreObjects();
    return false;
  }

  private void tidyBranches(TreeItem[] items, boolean expand) {
    for (TreeItem item : items) {
      item.setExpanded(expand);
      tidyBranches(item.getItems(), expand);
    }
  }

  public void disposeVariableComposite(boolean tree, boolean shared, boolean core, boolean history) {

    viewSelected = tree;
    view.getParent().setSelection(viewSelected ? view : design);
    designSelected = core;

    // historySelected = history;
    // sharedSelected = shared;

    for (Control control : variableComposite.getChildren()) {

      // PDI-1247 - these menus are coded for reuse, so make sure
      // they don't get disposed of here (alert: dirty design)
      if (control instanceof Tree) {
        ((Tree) control).setMenu(null);
      }
      control.dispose();
    }

    previousShowTrans = false;
    previousShowJob = false;

    // stepHistoryChanged=true;

    selectionLabel.setText(tree ? BaseMessages.getString(PKG, "Spoon.Explorer") : BaseMessages.getString(PKG,
        "Spoon.Steps"));
  }

  public void addCoreObjectsTree() {
    // Now create a new expand bar inside that item
    // We're going to put the core object in there
    //
    coreObjectsTree = new Tree(variableComposite, SWT.V_SCROLL | SWT.SINGLE);
    props.setLook(coreObjectsTree);

    coreObjectsTree.addSelectionListener(new SelectionAdapter() {

      public void widgetSelected(SelectionEvent event) {
        // expand the selected tree item, collapse the rest
        //
        if (props.getAutoCollapseCoreObjectsTree()) {
          TreeItem[] selection = coreObjectsTree.getSelection();
          if (selection.length == 1) {
            // expand if clicked on the the top level entry only...
            //
            TreeItem top = selection[0];
            while (top.getParentItem() != null) {
              top = top.getParentItem();
            }
            if (top == selection[0]) {
              boolean expanded = top.getExpanded();
              for (TreeItem item : coreObjectsTree.getItems()) {
                item.setExpanded(false);
              }
              top.setExpanded(!expanded);
            }
          }
        }
      }
    });

    coreObjectsTree.addTreeListener(new TreeAdapter() {
      public void treeExpanded(TreeEvent treeEvent) {
        if (props.getAutoCollapseCoreObjectsTree()) {
          TreeItem treeItem = (TreeItem) treeEvent.item;
          /*
           * Trick for WSWT on Windows systems: a SelectionEvent is called after
           * the TreeEvent if setSelection() is not used here. Otherwise the
           * first item in the list is selected as default and collapsed again
           * but wrong, see PDI-1480
           */
          coreObjectsTree.setSelection(treeItem);
          // expand the selected tree item, collapse the rest
          //
          for (TreeItem item : coreObjectsTree.getItems()) {
            if (item != treeItem)
              item.setExpanded(false);
            else
              treeItem.setExpanded(true);
          }
        }
      }
    });

    coreObjectsTree.addMouseMoveListener(new MouseMoveListener() {

      public void mouseMove(MouseEvent move) {
        // don't show tooltips in the tree if the option is not set
        if(!getProperties().showToolTips())
          return;
        
        toolTip.hide();
        TreeItem item = searchMouseOverTreeItem(coreObjectsTree.getItems(), move.x, move.y);
        if (item != null) {
          String name = item.getText();
          String tip = coreStepToolTipMap.get(name);
          if (tip != null) {
            PluginInterface plugin = PluginRegistry.getInstance().findPluginWithName(StepPluginType.class, name);
            if (plugin != null) {
              Image image = GUIResource.getInstance().getImagesSteps().get(plugin.getIds()[0]);
              if (image == null) {
                toolTip.hide();
              }
              toolTip.setImage(image);
              toolTip.setText(name + Const.CR + Const.CR + tip);
              toolTip.show(new org.eclipse.swt.graphics.Point(move.x + 10, move.y + 10));
            }
          }
          tip = coreJobToolTipMap.get(name);
          if (tip != null) {
            PluginInterface plugin = PluginRegistry.getInstance().findPluginWithName(JobEntryPluginType.class, name);
            if (plugin != null) {
              Image image = GUIResource.getInstance().getImagesJobentries().get(plugin.getIds()[0]);
              toolTip.setImage(image);
              toolTip.setText(name + Const.CR + Const.CR + tip);
              toolTip.show(new org.eclipse.swt.graphics.Point(move.x + 10, move.y + 10));
            }
          }
        }
      }
    });

    addDragSourceToTree(coreObjectsTree);
    addDefaultKeyListeners(coreObjectsTree);

    toolTip = new DefaultToolTip(variableComposite, ToolTip.RECREATE, true);
    toolTip.setRespectMonitorBounds(true);
    toolTip.setRespectDisplayBounds(true);
    toolTip.setPopupDelay(350);
    toolTip.setHideDelay(5000);
    toolTip.setShift(new org.eclipse.swt.graphics.Point(ConstUI.TOOLTIP_OFFSET, ConstUI.TOOLTIP_OFFSET));
  }

  protected TreeItem searchMouseOverTreeItem(TreeItem[] treeItems, int x, int y) {
    for (TreeItem treeItem : treeItems) {
      if (treeItem.getBounds().contains(x, y))
        return treeItem;
      if (treeItem.getItemCount() > 0) {
        treeItem = searchMouseOverTreeItem(treeItem.getItems(), x, y);
        if (treeItem != null)
          return treeItem;
      }
    }
    return null;
  }

  private boolean previousShowTrans;

  private boolean previousShowJob;

  public boolean showTrans;

  public boolean showJob;

  public void refreshCoreObjects() {
    if (shell.isDisposed())
      return;
    if (!designSelected)
      return;

    if (coreObjectsTree == null || coreObjectsTree.isDisposed()) {
      addCoreObjectsTree();
    }

    showTrans = getActiveTransformation() != null;
    showJob = getActiveJob() != null;

    if (showTrans == previousShowTrans && showJob == previousShowJob) {
      return;
    }

    // First remove all the entries that where present...
    //
    TreeItem[] expandItems = coreObjectsTree.getItems();
    for (int i = 0; i < expandItems.length; i++) {
      TreeItem item = expandItems[i];
      item.dispose();
    }

    if (showTrans) {
      // Fill the base components...
      //
      // ////////////////////////////////////////////////////////////////////////////////////////////////
      // TRANSFORMATIONS
      // ////////////////////////////////////////////////////////////////////////////////////////////////

      PluginRegistry registry = PluginRegistry.getInstance();

      final List<PluginInterface> basesteps = registry.getPlugins(StepPluginType.class);
      final List<String> basecat = registry.getCategories(StepPluginType.class);

      for (int i = 0; i < basecat.size(); i++) {
        TreeItem item = new TreeItem(coreObjectsTree, SWT.NONE);
        item.setText(basecat.get(i));
        item.setImage(GUIResource.getInstance().getImageArrow());

        for (int j = 0; j < basesteps.size(); j++) {
          if (basesteps.get(j).getCategory().equalsIgnoreCase(basecat.get(i))) {
            final Image stepimg = (Image) GUIResource.getInstance().getImagesStepsSmall().get(
                basesteps.get(j).getIds()[0]);
            String pluginName = basesteps.get(j).getName();
            String pluginDescription = basesteps.get(j).getDescription();

            if (!filterMatch(pluginName) && !filterMatch(pluginDescription))
              continue;

            TreeItem stepItem = new TreeItem(item, SWT.NONE);
            stepItem.setImage(stepimg);
            stepItem.setText(pluginName);
            stepItem.addListener(SWT.Selection, new Listener() {

              public void handleEvent(Event arg0) {
                // System.out.println("Tree item Listener fired");
              }
            });

            coreStepToolTipMap.put(pluginName, pluginDescription);
          }
        }
      }

      // Add History Items...
      TreeItem item = new TreeItem(coreObjectsTree, SWT.NONE);
      item.setText(BaseMessages.getString(PKG, "Spoon.History"));
      item.setImage(GUIResource.getInstance().getImageArrow());

      List<ObjectUsageCount> pluginHistory = props.getPluginHistory();

      // The top 10 at most, the rest is not interesting anyway
      // 
      for (int i = 0; i < pluginHistory.size() && i < 10; i++) {
        ObjectUsageCount usage = pluginHistory.get(i);
        PluginInterface stepPlugin = PluginRegistry.getInstance().findPluginWithId(StepPluginType.class,
            usage.getObjectName());
        if (stepPlugin != null) {
          final Image stepimg = GUIResource.getInstance().getImagesSteps().get(stepPlugin.getIds()[0]);
          String pluginName = Const.NVL(stepPlugin.getName(), "");
          String pluginDescription = Const.NVL(stepPlugin.getDescription(), "");

          if (!filterMatch(pluginName) && !filterMatch(pluginDescription))
            continue;

          TreeItem stepItem = new TreeItem(item, SWT.NONE);
          stepItem.setImage(stepimg);
          stepItem.setText(pluginName);
          stepItem.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event arg0) {
              // System.out.println("Tree item Listener fired");
            }
          });

          coreStepToolTipMap.put(stepPlugin.getDescription(), pluginDescription + " (" + usage.getNrUses() + ")");
        }
      }
    }

    if (showJob) {
      // Fill the base components...
      //
      // ////////////////////////////////////////////////////////////////////////////////////////////////
      // JOBS
      // ////////////////////////////////////////////////////////////////////////////////////////////////

      PluginRegistry registry = PluginRegistry.getInstance();
      List<PluginInterface> baseJobEntries = registry.getPlugins(JobEntryPluginType.class);
      List<String> baseCategories = registry.getCategories(JobEntryPluginType.class);

      TreeItem generalItem = null;

      for (int i = 0; i < baseCategories.size(); i++) {
        TreeItem item = new TreeItem(coreObjectsTree, SWT.NONE);
        item.setText(baseCategories.get(i));
        item.setImage(GUIResource.getInstance().getImageArrow());

        if (baseCategories.get(i).equalsIgnoreCase(JobEntryPluginType.GENERAL_CATEGORY)) {
          generalItem = item;
        }

        for (int j = 0; j < baseJobEntries.size(); j++) {
          if (!baseJobEntries.get(j).getIds()[0].equals("SPECIAL")) {
            if (baseJobEntries.get(j).getCategory().equalsIgnoreCase(baseCategories.get(i))) {
              final Image jobEntryImage = (Image) GUIResource.getInstance().getImagesJobentriesSmall().get(
                  baseJobEntries.get(j).getIds()[0]);
              String pluginName = Const.NVL(baseJobEntries.get(j).getName(), "");
              String pluginDescription = Const.NVL(baseJobEntries.get(j).getDescription(), "");

              if (!filterMatch(pluginName) && !filterMatch(pluginDescription))
                continue;

              TreeItem stepItem = new TreeItem(item, SWT.NONE);
              stepItem.setImage(jobEntryImage);
              stepItem.setText(pluginName);
              stepItem.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event arg0) {
                  // System.out.println("Tree item Listener fired");
                }
              });
              // if (isPlugin)
              // stepItem.setFont(GUIResource.getInstance().getFontBold());

              coreJobToolTipMap.put(pluginName, pluginDescription);
            }
          }
        }
      }

      // First add a few "Special entries: Start, Dummy, OK, ERROR
      // We add these to the top of the base category, we don't care about
      // the sort order here.
      //
      JobEntryCopy startEntry = JobMeta.createStartEntry();
      JobEntryCopy dummyEntry = JobMeta.createDummyEntry();

      String specialText[] = new String[] { startEntry.getName(), dummyEntry.getName(), };
      String specialTooltip[] = new String[] { startEntry.getDescription(), dummyEntry.getDescription(), };
      Image specialImage[] = new Image[] { GUIResource.getInstance().getImageStartSmall(),
          GUIResource.getInstance().getImageDummySmall() };

      for (int i = 0; i < specialText.length; i++) {
        TreeItem specialItem = new TreeItem(generalItem, SWT.NONE, i);
        specialItem.setImage(specialImage[i]);
        specialItem.setText(specialText[i]);
        specialItem.addListener(SWT.Selection, new Listener() {

          public void handleEvent(Event arg0) {
            // System.out.println("Tree item Listener fired");
          }

        });

        coreJobToolTipMap.put(specialText[i], specialTooltip[i]);
      }
    }

    variableComposite.layout(true, true);

    previousShowTrans = showTrans;
    previousShowJob = showJob;
  }

  protected void shareObject(SharedObjectInterface sharedObject) {
    sharedObject.setShared(true);
    EngineMetaInterface meta = getActiveMeta();
    try {
      if (meta!=null) {
        SharedObjects sharedObjects = null;
        if (meta instanceof TransMeta) sharedObjects=((TransMeta)meta).getSharedObjects();
        if (meta instanceof JobMeta) sharedObjects=((JobMeta)meta).getSharedObjects();
        if (sharedObjects!=null) {
          sharedObjects.storeObject(sharedObject);
          sharedObjects.saveToFile();
        }
      }
    } catch(Exception e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Dialog.ErrorWritingSharedObjects.Title"),
          BaseMessages.getString(PKG, "Spoon.Dialog.ErrorWritingSharedObjects.Message"), e);
    }
    refreshTree();
  }

  protected void unShareObject(SharedObjectInterface sharedObject) {
    MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_WARNING);
    mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.StopSharing.Message"));// "Are you sure you want to stop sharing?"
    mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.StopSharing.Title"));// Warning!
    int answer = mb.open();
    if (answer==SWT.YES) {
      sharedObject.setShared(false);
      EngineMetaInterface meta = getActiveMeta();
      try {
        if (meta!=null) {
          SharedObjects sharedObjects = null;
          if (meta instanceof TransMeta) sharedObjects=((TransMeta)meta).getSharedObjects();
          if (meta instanceof JobMeta) sharedObjects=((JobMeta)meta).getSharedObjects();
          if (sharedObjects!=null) {
            sharedObjects.removeObject(sharedObject);
            sharedObjects.saveToFile();
          }
        }
      } catch(Exception e) {
        new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Dialog.ErrorWritingSharedObjects.Title"),
            BaseMessages.getString(PKG, "Spoon.Dialog.ErrorWritingSharedObjects.Message"), e);
      }
      refreshTree();
    }
  }

  /**
   * @return The object that is selected in the tree or null if we couldn't
   *         figure it out. (titles etc. == null)
   */
  public TreeSelection[] getTreeObjects(final Tree tree) {
    return delegates.tree.getTreeObjects(tree, selectionTree, coreObjectsTree);
  }

  private void addDragSourceToTree(final Tree tree) {
    delegates.tree.addDragSourceToTree(tree, selectionTree, coreObjectsTree);
  }

  public void hideToolTips() {
    if (toolTip != null) {
      toolTip.hide();
    }
  }

  /**
   * If you click in the tree, you might want to show the corresponding window.
   */
  public void showSelection() {
    TreeSelection[] objects = getTreeObjects(selectionTree);
    if (objects.length != 1) {
      return; // not yet supported, we can do this later when the OSX bug
      // goes away
    }

    TreeSelection object = objects[0];

    final Object selection = object.getSelection();
    final Object parent = object.getParent();

    TransMeta transMeta = null;
    if (selection instanceof TransMeta) {
      transMeta = (TransMeta) selection;
    }
    if (parent instanceof TransMeta) {
      transMeta = (TransMeta) parent;
    }

    if (transMeta != null) {

      TabMapEntry entry = delegates.tabs.findTabMapEntry(transMeta);
      if (entry != null) {
        int current = tabfolder.getSelectedIndex();
        int desired = tabfolder.indexOf(entry.getTabItem());
        if (current != desired) {
          tabfolder.setSelected(desired);
        }
        transMeta.setInternalKettleVariables();
        if (getCoreObjectsState() != STATE_CORE_OBJECTS_SPOON) {
          // Switch the core objects in the lower left corner to the
          // spoon trans types
          refreshCoreObjects();
        }
      }
    }

    JobMeta jobMeta = null;
    if (selection instanceof JobMeta)
      jobMeta = (JobMeta) selection;
    if (parent instanceof JobMeta)
      jobMeta = (JobMeta) parent;
    if (jobMeta != null) {

      TabMapEntry entry = delegates.tabs.findTabMapEntry(transMeta);
      if (entry != null) {
        int current = tabfolder.getSelectedIndex();
        int desired = tabfolder.indexOf(entry.getTabItem());
        if (current != desired)
          tabfolder.setSelected(desired);
        jobMeta.setInternalKettleVariables();
        if (getCoreObjectsState() != STATE_CORE_OBJECTS_CHEF) {
          // Switch the core objects in the lower left corner to the
          // spoon job types
          //
          refreshCoreObjects();
        }
      }
    }
  }

  private Object selectionObjectParent = null;

  private Object selectionObject = null;

  public void newHop() {
    newHop((TransMeta) selectionObjectParent);
  }

  public void sortHops() {
    ((TransMeta) selectionObjectParent).sortHops();
    refreshTree();
  }

  public void newDatabasePartitioningSchema() {
    TransMeta transMeta = getActiveTransformation();
    if (transMeta != null) {
      newPartitioningSchema(transMeta);
    }
  }

  public void newClusteringSchema() {
    TransMeta transMeta = getActiveTransformation();
    if (transMeta != null) {
      newClusteringSchema(transMeta);
    }
  }

  public void newSlaveServer() {
    newSlaveServer((HasSlaveServersInterface) selectionObjectParent);
  }

  public void editTransformationPropertiesPopup() {
    TransGraph.editProperties((TransMeta) selectionObject, this, rep, true);
  }

  public void addTransLog() {
    TransGraph activeTransGraph = getActiveTransGraph();
    if (activeTransGraph != null) {
      activeTransGraph.transLogDelegate.addTransLog();
      activeTransGraph.transGridDelegate.addTransGrid();
    }
  }

  public void addTransHistory() {
    TransGraph activeTransGraph = getActiveTransGraph();
    if (activeTransGraph != null) {
      activeTransGraph.transHistoryDelegate.addTransHistory();
    }
  }

  public boolean editJobProperties(String id) {
    if ("job-settings".equals(id)) {
      return JobGraph.editProperties(getActiveJob(), this, rep, true);
    } else if ("job-inst-settings".equals(id)) {
      return JobGraph.editProperties((JobMeta) selectionObject, this, rep, true);
    }
    return false;
  }

  public void editJobPropertiesPopup() {
    JobGraph.editProperties((JobMeta) selectionObject, this, rep, true);
  }
  
  public void addJobLog() {
    JobGraph activeJobGraph = getActiveJobGraph();
    if (activeJobGraph != null) {
      activeJobGraph.jobLogDelegate.addJobLog();
      activeJobGraph.jobGridDelegate.addJobGrid();
    }
  }

  public void addJobHistory() {
    addJobHistory((JobMeta) selectionObject, true);
  }

  public void newStep() {
    newStep(getActiveTransformation());
  }

  public void editConnection() {

    if (RepositorySecurityUI.verifyOperations(shell, rep, RepositoryOperation.MODIFY_DATABASE))
      return;

    final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
    delegates.db.editConnection(databaseMeta);
  }

  public void dupeConnection() {
    final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
    final HasDatabasesInterface hasDatabasesInterface = (HasDatabasesInterface) selectionObjectParent;
    delegates.db.dupeConnection(hasDatabasesInterface, databaseMeta);
  }

  public void clipConnection() {
    final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
    delegates.db.clipConnection(databaseMeta);
  }

  public void delConnection() {
    if (RepositorySecurityUI.verifyOperations(shell, rep, RepositoryOperation.DELETE_DATABASE))
      return;

    final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
	MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO| SWT.ICON_QUESTION);
	mb.setMessage(BaseMessages.getString(PKG, "Spoon.ExploreDB.DeleteConnectionAsk.Message", databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	mb.setText(BaseMessages.getString(PKG, "Spoon.ExploreDB.DeleteConnectionAsk.Title")); //$NON-NLS-1$
	int response = mb.open();

	if (response != SWT.YES)return;
	
    final HasDatabasesInterface hasDatabasesInterface = (HasDatabasesInterface) selectionObjectParent;
    delegates.db.delConnection(hasDatabasesInterface, databaseMeta);
  }

  public void sqlConnection() {
    final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
    delegates.db.sqlConnection(databaseMeta);
  }

  public void clearDBCache(String id) {
    if ("database-class-clear-cache".equals(id)) {
      delegates.db.clearDBCache((DatabaseMeta) null);
    }
    if ("database-inst-clear-cache".equals(id)) {
      final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
      delegates.db.clearDBCache(databaseMeta);
    }
  }

  public void exploreDatabase() {

    if (RepositorySecurityUI.verifyOperations(shell, rep, RepositoryOperation.EXPLORE_DATABASE)) {
      return;
    }

    // Show a minimal window to allow you to quickly select the database
    // connection to explore
    //
    List<DatabaseMeta> databases = new ArrayList<DatabaseMeta>();

    // First load the connections from the loaded file
    //
    HasDatabasesInterface databasesInterface = getActiveHasDatabasesInterface();
    if (databasesInterface != null) {
      databases.addAll(databasesInterface.getDatabases());
    }

    // Overwrite the information with the connections from the repository
    //
    if (rep != null) {
      try {
        List<DatabaseMeta> list = rep.readDatabases();
        for (DatabaseMeta databaseMeta : list) {
          int index = databases.indexOf(databaseMeta);
          if (index < 0) {
            databases.add(databaseMeta);
          } else {
            databases.set(index, databaseMeta);
          }
        }
      } catch (KettleException e) {
        log.logError("Unexpected repository error", e.getMessage());
      }
    }

    if (databases.size() == 0)
      return;

    // OK, get a list of all the database names...
    // 
    String[] databaseNames = new String[databases.size()];
    for (int i = 0; i < databases.size(); i++)
      databaseNames[i] = databases.get(i).getName();

    // show the shell...
    //
    EnterSelectionDialog dialog = new EnterSelectionDialog(shell, databaseNames, BaseMessages.getString(PKG,
        "Spoon.ExploreDB.SelectDB.Title"), BaseMessages.getString(PKG, "Spoon.ExploreDB.SelectDB.Message"));
    String name = dialog.open();
    if (name != null) {
      selectionObject = DatabaseMeta.findDatabase(databases, name);
      exploreDB();
    }
  }

  public void exploreDB() {
    final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
    delegates.db.exploreDB(databaseMeta, true);
  }

  public void editStep() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final StepMeta stepMeta = (StepMeta) selectionObject;
    delegates.steps.editStep(transMeta, stepMeta);
  }

  public void dupeStep() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final StepMeta stepMeta = (StepMeta) selectionObject;
    delegates.steps.dupeStep(transMeta, stepMeta);
  }

  public void delStep() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final StepMeta stepMeta = (StepMeta) selectionObject;
    delegates.steps.delStep(transMeta, stepMeta);
  }

  public void shareObject(String id) {
    if ("database-inst-share".equals(id)) {
      final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
      if (databaseMeta.isShared()) {
        unShareObject(databaseMeta);
      } else {
        shareObject(databaseMeta);
      }
    }
    if ("step-inst-share".equals(id)) {
      final StepMeta stepMeta = (StepMeta) selectionObject;
      shareObject(stepMeta);
    }
    if ("partition-schema-inst-share".equals(id)) {
      final PartitionSchema partitionSchema = (PartitionSchema) selectionObject;
      shareObject(partitionSchema);
    }
    if ("cluster-schema-inst-share".equals(id)) {
      final ClusterSchema clusterSchema = (ClusterSchema) selectionObject;
      shareObject(clusterSchema);
    }
    if ("slave-server-inst-share".equals(id)) {
      final SlaveServer slaveServer = (SlaveServer) selectionObject;
      shareObject(slaveServer);
    }
  }

  public void editJobEntry() {
    final JobMeta jobMeta = (JobMeta) selectionObjectParent;
    final JobEntryCopy jobEntry = (JobEntryCopy) selectionObject;
    editJobEntry(jobMeta, jobEntry);
  }

  public void dupeJobEntry() {
    final JobMeta jobMeta = (JobMeta) selectionObjectParent;
    final JobEntryCopy jobEntry = (JobEntryCopy) selectionObject;
    delegates.jobs.dupeJobEntry(jobMeta, jobEntry);
  }

  public void deleteJobEntryCopies() {
    final JobMeta jobMeta = (JobMeta) selectionObjectParent;
    final JobEntryCopy jobEntry = (JobEntryCopy) selectionObject;
    deleteJobEntryCopies(jobMeta, jobEntry);
  }

  public void editHop() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final TransHopMeta transHopMeta = (TransHopMeta) selectionObject;
    editHop(transMeta, transHopMeta);
  }

  public void delHop() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final TransHopMeta transHopMeta = (TransHopMeta) selectionObject;
    delHop(transMeta, transHopMeta);
  }

  public void editPartitionSchema() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final PartitionSchema partitionSchema = (PartitionSchema) selectionObject;
    editPartitionSchema(transMeta, partitionSchema);
  }

  public void delPartitionSchema() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final PartitionSchema partitionSchema = (PartitionSchema) selectionObject;
    delPartitionSchema(transMeta, partitionSchema);
  }

  public void editClusterSchema() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final ClusterSchema clusterSchema = (ClusterSchema) selectionObject;
    editClusterSchema(transMeta, clusterSchema);
  }

  public void delClusterSchema() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final ClusterSchema clusterSchema = (ClusterSchema) selectionObject;
    delClusterSchema(transMeta, clusterSchema);
  }

  public void monitorClusterSchema() throws KettleException {
    final ClusterSchema clusterSchema = (ClusterSchema) selectionObject;
    monitorClusterSchema(clusterSchema);
  }

  public void editSlaveServer() {
    final SlaveServer slaveServer = (SlaveServer) selectionObject;
    editSlaveServer(slaveServer);
  }

  public void delSlaveServer() {
    final HasSlaveServersInterface hasSlaveServersInterface = (HasSlaveServersInterface) selectionObjectParent;
    final SlaveServer slaveServer = (SlaveServer) selectionObject;
    delSlaveServer(hasSlaveServersInterface, slaveServer);
  }

  public void addSpoonSlave() {
    final SlaveServer slaveServer = (SlaveServer) selectionObject;
    addSpoonSlave(slaveServer);
  }

  private synchronized void setMenu(Tree tree) {
    TreeSelection[] objects = getTreeObjects(tree);
    if (objects.length != 1)
      return; // not yet supported, we can do this later when the OSX bug
    // goes away

    TreeSelection object = objects[0];

    selectionObject = object.getSelection();
    Object selection = selectionObject;
    selectionObjectParent = object.getParent();

    // Not clicked on a real object: returns a class
    XulMenupopup spoonMenu = null;
    if (selection instanceof Class<?>) {
      if (selection.equals(TransMeta.class)) {
        // New
        spoonMenu = (XulMenupopup) menuMap.get("trans-class");
      } else if (selection.equals(JobMeta.class)) {
        // New
        spoonMenu = (XulMenupopup) menuMap.get("job-class");
      } else if (selection.equals(TransHopMeta.class)) {
        // New
        spoonMenu = (XulMenupopup) menuMap.get("trans-hop-class");
      } else if (selection.equals(DatabaseMeta.class)) {
        spoonMenu = (XulMenupopup) menuMap.get("database-class");
      } else if (selection.equals(PartitionSchema.class)) {
        // New
        spoonMenu = (XulMenupopup) menuMap.get("partition-schema-class");
      } else if (selection.equals(ClusterSchema.class)) {
        spoonMenu = (XulMenupopup) menuMap.get("cluster-schema-class");
      } else if (selection.equals(SlaveServer.class)) {
        spoonMenu = (XulMenupopup) menuMap.get("slave-cluster-class");
      } else
        spoonMenu = null;
    } else {

      if (selection instanceof TransMeta) {
        spoonMenu = (XulMenupopup) menuMap.get("trans-inst");
      } else if (selection instanceof JobMeta) {
        spoonMenu = (XulMenupopup) menuMap.get("job-inst");
      } else if (selection instanceof PluginInterface) {
        spoonMenu = (XulMenupopup) menuMap.get("step-plugin");
      } else if (selection instanceof DatabaseMeta) {
        spoonMenu = (XulMenupopup) menuMap.get("database-inst");
        // disable for now if the connection is an SAP ERP type of database...
        //
        XulMenuitem item = (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById("database-inst-explore");
        if (item != null) {
          final DatabaseMeta databaseMeta = (DatabaseMeta) selection;
          item.setDisabled(!databaseMeta.isExplorable());
        }
        item = (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById("database-inst-clear-cache");
        if (item != null) {
          final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
          item.setLabel(BaseMessages.getString(PKG, "Spoon.Menu.Popup.CONNECTIONS.ClearDBCache")
              + databaseMeta.getName());// Clear
        }

        item = (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById("database-inst-share");
        if (item!=null) {
          final DatabaseMeta databaseMeta = (DatabaseMeta) selection;
          if (databaseMeta.isShared()) {
            item.setLabel(BaseMessages.getString(PKG, "Spoon.Menu.Popup.CONNECTIONS.UnShare"));
          } else {
            item.setLabel(BaseMessages.getString(PKG, "Spoon.Menu.Popup.CONNECTIONS.Share"));
          }
        }
      } else if (selection instanceof StepMeta) {
        spoonMenu = (XulMenupopup) menuMap.get("step-inst");
      } else if (selection instanceof JobEntryCopy) {
        spoonMenu = (XulMenupopup) menuMap.get("job-entry-copy-inst");
      } else if (selection instanceof TransHopMeta) {
        spoonMenu = (XulMenupopup) menuMap.get("trans-hop-inst");
      } else if (selection instanceof PartitionSchema) {
        spoonMenu = (XulMenupopup) menuMap.get("partition-schema-inst");
      } else if (selection instanceof ClusterSchema) {
        spoonMenu = (XulMenupopup) menuMap.get("cluster-schema-inst");
      } else if (selection instanceof SlaveServer) {
        spoonMenu = (XulMenupopup) menuMap.get("slave-server-inst");
      }

    }
    if (spoonMenu != null) {
      ConstUI.displayMenu((org.eclipse.swt.widgets.Menu) spoonMenu.getManagedObject(), tree);
    } else
      tree.setMenu(null);
  }

  /**
   * Reaction to double click
   * 
   */
  private void doubleClickedInTree(Tree tree) {
    TreeSelection[] objects = getTreeObjects(tree);
    if (objects.length != 1) {
      return; // not yet supported, we can do this later when the OSX bug
      // goes away
    }

    TreeSelection object = objects[0];

    final Object selection = object.getSelection();
    final Object parent = object.getParent();

    if (selection instanceof Class<?>) {
      if (selection.equals(TransMeta.class))
        newTransFile();
      if (selection.equals(JobMeta.class))
        newJobFile();
      if (selection.equals(TransHopMeta.class))
        newHop((TransMeta) parent);
      if (selection.equals(DatabaseMeta.class))
        delegates.db.newConnection();
      if (selection.equals(PartitionSchema.class))
        newPartitioningSchema((TransMeta) parent);
      if (selection.equals(ClusterSchema.class))
        newClusteringSchema((TransMeta) parent);
      if (selection.equals(SlaveServer.class))
        newSlaveServer((HasSlaveServersInterface) parent);
    } else {
      if (selection instanceof TransMeta)
        TransGraph.editProperties((TransMeta) selection, this, rep, true);
      if (selection instanceof JobMeta)
        JobGraph.editProperties((JobMeta) selection, this, rep, true);
      if (selection instanceof PluginInterface)
        newStep(getActiveTransformation());
      if (selection instanceof DatabaseMeta)
        delegates.db.editConnection((DatabaseMeta) selection);
      if (selection instanceof StepMeta)
        delegates.steps.editStep((TransMeta) parent, (StepMeta) selection);
      if (selection instanceof JobEntryCopy)
        editJobEntry((JobMeta) parent, (JobEntryCopy) selection);
      if (selection instanceof TransHopMeta)
        editHop((TransMeta) parent, (TransHopMeta) selection);
      if (selection instanceof PartitionSchema)
        editPartitionSchema((TransMeta) parent, (PartitionSchema) selection);
      if (selection instanceof ClusterSchema)
        editClusterSchema((TransMeta) parent, (ClusterSchema) selection);
      if (selection instanceof SlaveServer)
        editSlaveServer((SlaveServer) selection);
    }
  }

  protected void monitorClusterSchema(ClusterSchema clusterSchema) throws KettleException {
    for (int i = 0; i < clusterSchema.getSlaveServers().size(); i++) {
      SlaveServer slaveServer = clusterSchema.getSlaveServers().get(i);
      addSpoonSlave(slaveServer);
    }
  }

  protected void editSlaveServer(SlaveServer slaveServer) {
    // slaveServer.getVariable("MASTER_HOST")
    SlaveServerDialog dialog = new SlaveServerDialog(shell, slaveServer);
    if (dialog.open()) {
      refreshTree();
      refreshGraph();
    }
  }

  private void addTabs() {

    if (tabComp != null) {
      tabComp.dispose();
    }

    tabComp = new Composite(sashform, SWT.BORDER);
    props.setLook(tabComp);
    tabComp.setLayout(new FillLayout());

    tabfolder = new TabSet(tabComp);
    tabfolder.setChangedFont(GUIResource.getInstance().getFontBold());
    tabfolder.setUnchangedFont(GUIResource.getInstance().getFontGraph());
    props.setLook(tabfolder.getSwtTabset(), Props.WIDGET_STYLE_TAB);

    int weights[] = props.getSashWeights();
    sashform.setWeights(weights);
    sashform.setVisible(true);

    // Set a minimum width on the sash so that the view and design buttons
    // on the left panel are always visible.
    //
    Control[] comps = sashform.getChildren();
    for (int i = 0; i < comps.length; i++) {

      if (comps[i] instanceof Sash) {
        int limit = 10;

        final int SASH_LIMIT = Const.isOSX() ? 150 : limit;
        final Sash sash = (Sash) comps[i];

        sash.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent event) {
            Rectangle rect = sash.getParent().getClientArea();
            event.x = Math.min(Math.max(event.x, SASH_LIMIT), rect.width - SASH_LIMIT);
            if (event.detail != SWT.DRAG) {
              sash.setBounds(event.x, event.y, event.width, event.height);
              sashform.layout();
            }
          }
        });
      }
    }

    tabfolder.addListener(this); // methods: tabDeselected, tabClose,
    // tabSelected

  }

  public void tabDeselected(TabItem item) {

  }

  public boolean tabClose(TabItem item) {
    try {
      return delegates.tabs.tabClose(item);
    } catch (Exception e) {
      new ErrorDialog(shell, "Error", "Unexpected error closing tab!", e);
      return false;
    }
  }

  public TabSet getTabSet() {
    return tabfolder;
  }

  public void tabSelected(TabItem item) {
    delegates.tabs.tabSelected(item);
    enableMenus();
  }

  public String getRepositoryName() {
    if (rep == null)
      return null;
    return rep.getName();
  }

  public void pasteXML(TransMeta transMeta, String clipcontent, Point loc) {
    try {
      Document doc = XMLHandler.loadXMLString(clipcontent);
      Node transnode = XMLHandler.getSubNode(doc, Spoon.XML_TAG_TRANSFORMATION_STEPS);
      // De-select all, re-select pasted steps...
      transMeta.unselectAll();

      Node stepsnode = XMLHandler.getSubNode(transnode, "steps");
      int nr = XMLHandler.countNodes(stepsnode, "step");
      if (log.isDebug())
        log.logDebug(BaseMessages.getString(PKG, "Spoon.Log.FoundSteps", "" + nr) + loc);// "I found "+nr+" steps to paste on location: "
      StepMeta steps[] = new StepMeta[nr];

      // Point min = new Point(loc.x, loc.y);
      Point min = new Point(99999999, 99999999);

      // Load the steps...
      for (int i = 0; i < nr; i++) {
        Node stepnode = XMLHandler.getSubNodeByNr(stepsnode, "step", i);
        steps[i] = new StepMeta(stepnode, transMeta.getDatabases(), transMeta.getCounters());

        if (loc != null) {
          Point p = steps[i].getLocation();

          if (min.x > p.x)
            min.x = p.x;
          if (min.y > p.y)
            min.y = p.y;
        }
      }

      // Load the hops...
      Node hopsnode = XMLHandler.getSubNode(transnode, "order");
      nr = XMLHandler.countNodes(hopsnode, "hop");
      if (log.isDebug())
        log.logDebug(BaseMessages.getString(PKG, "Spoon.Log.FoundHops", "" + nr));// "I found "+nr+" hops to paste."
      TransHopMeta hops[] = new TransHopMeta[nr];

      ArrayList<StepMeta> alSteps = new ArrayList<StepMeta>();
      for (int i = 0; i < steps.length; i++)
        alSteps.add(steps[i]);

      for (int i = 0; i < nr; i++) {
        Node hopnode = XMLHandler.getSubNodeByNr(hopsnode, "hop", i);
        hops[i] = new TransHopMeta(hopnode, alSteps);
      }

      // This is the offset:
      //
      Point offset = new Point(loc.x - min.x, loc.y - min.y);

      // Undo/redo object positions...
      int position[] = new int[steps.length];

      for (int i = 0; i < steps.length; i++) {
        Point p = steps[i].getLocation();
        String name = steps[i].getName();

        steps[i].setLocation(p.x + offset.x, p.y + offset.y);
        steps[i].setDraw(true);

        // Check the name, find alternative...
        steps[i].setName(transMeta.getAlternativeStepname(name));
        transMeta.addStep(steps[i]);
        position[i] = transMeta.indexOfStep(steps[i]);
        steps[i].setSelected(true);
      }

      // Add the hops too...
      for (int i = 0; i < hops.length; i++) {
        transMeta.addTransHop(hops[i]);
      }

      // Load the notes...
      Node notesnode = XMLHandler.getSubNode(transnode, "notepads");
      nr = XMLHandler.countNodes(notesnode, "notepad");
      if (log.isDebug())
        log.logDebug(BaseMessages.getString(PKG, "Spoon.Log.FoundNotepads", "" + nr));// "I found "+nr+" notepads to paste."
      NotePadMeta notes[] = new NotePadMeta[nr];

      for (int i = 0; i < notes.length; i++) {
        Node notenode = XMLHandler.getSubNodeByNr(notesnode, "notepad", i);
        notes[i] = new NotePadMeta(notenode);
        Point p = notes[i].getLocation();
        notes[i].setLocation(p.x + offset.x, p.y + offset.y);
        transMeta.addNote(notes[i]);
        notes[i].setSelected(true);
      }

      // Set the source and target steps ...
      for (int i = 0; i < steps.length; i++) {
        StepMetaInterface smi = steps[i].getStepMetaInterface();
        smi.searchInfoAndTargetSteps(transMeta.getSteps());
      }

      // Save undo information too...
      addUndoNew(transMeta, steps, position, false);

      int hoppos[] = new int[hops.length];
      for (int i = 0; i < hops.length; i++)
        hoppos[i] = transMeta.indexOfTransHop(hops[i]);
      addUndoNew(transMeta, hops, hoppos, true);

      int notepos[] = new int[notes.length];
      for (int i = 0; i < notes.length; i++)
        notepos[i] = transMeta.indexOfNote(notes[i]);
      addUndoNew(transMeta, notes, notepos, true);

      if (transMeta.haveStepsChanged()) {
        refreshTree();
        refreshGraph();
      }
    } catch (KettleException e) {
      // "Error pasting steps...",
      // "I was unable to paste steps to this transformation"
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Dialog.UnablePasteSteps.Title"), BaseMessages
          .getString(PKG, "Spoon.Dialog.UnablePasteSteps.Message"), e);
    }
  }

  public void copySelected(TransMeta transMeta, List<StepMeta> steps, List<NotePadMeta> notes) {
    if (steps == null || steps.size() == 0)
      return;

    String xml = XMLHandler.getXMLHeader();
    try {
      xml += XMLHandler.openTag(Spoon.XML_TAG_TRANSFORMATION_STEPS) + Const.CR;
      xml += " <steps>" + Const.CR;

      for (int i = 0; i < steps.size(); i++) {
        xml += steps.get(i).getXML();
      }

      xml += "    </steps>" + Const.CR;

      // 
      // Also check for the hops in between the selected steps...
      //

      xml += "<order>" + Const.CR;
      if (steps != null)
        for (int i = 0; i < steps.size(); i++) {
          for (int j = 0; j < steps.size(); j++) {
            if (i != j) {
              TransHopMeta hop = transMeta.findTransHop(steps.get(i), steps.get(j), true);
              if (hop != null) // Ok, we found one...
              {
                xml += hop.getXML() + Const.CR;
              }
            }
          }
        }
      xml += "  </order>" + Const.CR;

      xml += "  <notepads>" + Const.CR;
      if (notes != null)
        for (int i = 0; i < notes.size(); i++) {
          xml += notes.get(i).getXML();
        }
      xml += "   </notepads>" + Const.CR;

      xml += " " + XMLHandler.closeTag(Spoon.XML_TAG_TRANSFORMATION_STEPS) + Const.CR;

      toClipboard(xml);
    } catch (Exception ex) {
      new ErrorDialog(getShell(), "Error", "Error encoding to XML", ex);
    }
  }

  public void editHop(TransMeta transMeta, TransHopMeta transHopMeta) {
    // Backup situation BEFORE edit:
    String name = transHopMeta.toString();
    TransHopMeta before = (TransHopMeta) transHopMeta.clone();

    TransHopDialog hd = new TransHopDialog(shell, SWT.NONE, transHopMeta, transMeta);
    if (hd.open() != null) {
      // Backup situation for redo/undo:
      TransHopMeta after = (TransHopMeta) transHopMeta.clone();
      addUndoChange(transMeta, new TransHopMeta[] { before }, new TransHopMeta[] { after }, new int[] { transMeta
          .indexOfTransHop(transHopMeta) });

      String newname = transHopMeta.toString();
      if (!name.equalsIgnoreCase(newname)) {
        refreshTree();
        refreshGraph(); // color, nr of copies...
      }
    }
    setShellText();
  }

  public void delHop(TransMeta transMeta, TransHopMeta transHopMeta) {
    int index = transMeta.indexOfTransHop(transHopMeta);
    addUndoDelete(transMeta, new Object[] { (TransHopMeta) transHopMeta.clone() }, new int[] { index });
    transMeta.removeTransHop(index);
    
    // If this is an error handling hop, disable it
    // 
    if (transHopMeta.getFromStep().isDoingErrorHandling()) {
      StepErrorMeta stepErrorMeta = transHopMeta.getFromStep().getStepErrorMeta();

      // We can only disable error handling if the target of the hop is the same as the target of the error handling.
      //
      if (stepErrorMeta.getTargetStep()!=null && stepErrorMeta.getTargetStep().equals(transHopMeta.getToStep())) {
        StepMeta stepMeta = transHopMeta.getFromStep();
        // Only if the target step is where the error handling is going to...
        //
  
        StepMeta before = (StepMeta)stepMeta.clone();
        stepErrorMeta.setEnabled(false);
  
        index = transMeta.indexOfStep(stepMeta);
        addUndoChange(transMeta, new Object[] { before }, new Object[]{ stepMeta}, new int[] { index });
      }
    }
    
    refreshTree();
    refreshGraph();
  }

  public void newHop(TransMeta transMeta, StepMeta fr, StepMeta to) {
    TransHopMeta hi = new TransHopMeta(fr, to);

    TransHopDialog hd = new TransHopDialog(shell, SWT.NONE, hi, transMeta);
    if (hd.open() != null) {
      newHop(transMeta, hi);
    }
  }

  public void newHop(TransMeta transMeta, TransHopMeta transHopMeta) {
    if (checkIfHopAlreadyExists(transMeta, transHopMeta)) {
      transMeta.addTransHop(transHopMeta);
      int idx = transMeta.indexOfTransHop(transHopMeta);

      if (!performNewTransHopChecks(transMeta, transHopMeta)) {
        // Some error occurred: loops, existing hop, etc.
        // Remove it again...
        //
        transMeta.removeTransHop(idx);
      } else {
        addUndoNew(transMeta, new TransHopMeta[] { transHopMeta },
            new int[] { transMeta.indexOfTransHop(transHopMeta) });
      }

      // Just to make sure
      transHopMeta.getFromStep().drawStep();
      transHopMeta.getToStep().drawStep();

      refreshTree();
      refreshGraph();
    }
  }

  /**
   * @param transMeta
   * @param newHop
   * @return true when the hop was added, false if there was an error
   */
  public boolean checkIfHopAlreadyExists(TransMeta transMeta, TransHopMeta newHop) {
    boolean ok = true;
    if (transMeta.findTransHop(newHop.getFromStep(), newHop.getToStep()) != null) {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.HopExists.Message"));// "This hop already exists!"
      mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.HopExists.Title"));// Error!
      mb.open();
      ok = false;
    }

    return ok;
  }

  /**
   * @param transMeta
   * @param newHop
   * @return true when the hop was added, false if there was an error
   */
  public boolean performNewTransHopChecks(TransMeta transMeta, TransHopMeta newHop) {
    boolean ok = true;

    if (transMeta.hasLoop(newHop.getFromStep()) || transMeta.hasLoop(newHop.getToStep())) {
      MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING);
      mb.setMessage(BaseMessages.getString(PKG, "TransGraph.Dialog.HopCausesLoop.Message")); //$NON-NLS-1$
      mb.setText(BaseMessages.getString(PKG, "TransGraph.Dialog.HopCausesLoop.Title")); //$NON-NLS-1$
      mb.open();
      ok = false;
    }

    if (ok) { // only do the following checks, e.g. checkRowMixingStatically
      // when not looping, otherwise we get a loop with
      // StackOverflow there ;-)
      try {
        if (!newHop.getToStep().getStepMetaInterface().excludeFromRowLayoutVerification()) {
          transMeta.checkRowMixingStatically(newHop.getToStep(), null);
        }
      } catch (KettleRowException re) {
        // Show warning about mixing rows with conflicting layouts...
        new ErrorDialog(shell, BaseMessages.getString(PKG, "TransGraph.Dialog.HopCausesRowMixing.Title"), BaseMessages
            .getString(PKG, "TransGraph.Dialog.HopCausesRowMixing.Message"), re);
      }

      verifyCopyDistribute(transMeta, newHop.getFromStep());
    }

    return ok;
  }

  public void verifyCopyDistribute(TransMeta transMeta, StepMeta fr) {
    List<StepMeta> nextSteps = transMeta.findNextSteps(fr);
    int nrNextSteps = nextSteps.size();

    // don't show it for 3 or more hops, by then you should have had the
    // message
    if (nrNextSteps == 2) {
      boolean distributes = fr.getStepMetaInterface().excludeFromCopyDistributeVerification();

      if (props.showCopyOrDistributeWarning() && !fr.getStepMetaInterface().excludeFromCopyDistributeVerification()) {
        MessageDialogWithToggle md = new MessageDialogWithToggle(shell, BaseMessages.getString(PKG, "System.Warning"),
            null, BaseMessages.getString(PKG, "Spoon.Dialog.CopyOrDistribute.Message", fr.getName(), Integer
                .toString(nrNextSteps)), MessageDialog.WARNING, new String[] {
                BaseMessages.getString(PKG, "Spoon.Dialog.CopyOrDistribute.Copy"),
                BaseMessages.getString(PKG, "Spoon.Dialog.CopyOrDistribute.Distribute") }, 0, BaseMessages.getString(
                PKG, "Spoon.Message.Warning.NotShowWarning"), !props.showCopyOrDistributeWarning());
        MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
        int idx = md.open();
        props.setShowCopyOrDistributeWarning(!md.getToggleState());
        props.saveProps();

        distributes = (idx & 0xFF) == 1;
      }

      if (distributes) {
        fr.setDistributes(true);
      } else {
        fr.setDistributes(false);
      }

      refreshTree();
      refreshGraph();
    }
  }

  public void newHop(TransMeta transMeta) {
    newHop(transMeta, null, null);
  }

  public void openRepository() {
    loginDialog = new RepositoriesDialog(shell, null, new ILoginCallback() {

      public void onSuccess(Repository repository) {
        // Close previous repository...
        if (rep != null) {
          rep.disconnect();
          SpoonPluginManager.getInstance().notifyLifecycleListeners(SpoonLifeCycleEvent.REPOSITORY_DISCONNECTED);
        }
        setRepository(repository);

        JobMeta jobMetas[] = getLoadedJobs();
        for (int t = 0; t < jobMetas.length; t++) {
          JobMeta jobMeta = jobMetas[t];

          for (int i = 0; i < jobMeta.nrDatabases(); i++) {
            jobMeta.getDatabase(i).setObjectId(null);
          }

          // Set for the existing job the ID at -1!
          jobMeta.setObjectId(null);

          // Keep track of the old databases for now.
          List<DatabaseMeta> oldDatabases = jobMeta.getDatabases();

          // In order to re-match the databases on name (not content), we
          // need to load the databases from the new repository.
          // NOTE: for purposes such as DEVELOP - TEST - PRODUCTION
          // sycles.

          // first clear the list of databases and slave servers
          jobMeta.setDatabases(new ArrayList<DatabaseMeta>());
          jobMeta.setSlaveServers(new ArrayList<SlaveServer>());

          // Read them from the new repository.
          try {
            SharedObjects sharedObjects = rep.readJobMetaSharedObjects(jobMeta);
            sharedObjectsFileMap.put(sharedObjects.getFilename(), sharedObjects);
          } catch (KettleException e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Dialog.ErrorReadingSharedObjects.Title"),
                BaseMessages.getString(PKG, "Spoon.Dialog.ErrorReadingSharedObjects.Message", makeTabName(jobMeta,
                    true)), e);
          }

          // Then we need to re-match the databases at save time...
          for (int i = 0; i < oldDatabases.size(); i++) {
            DatabaseMeta oldDatabase = oldDatabases.get(i);
            DatabaseMeta newDatabase = DatabaseMeta.findDatabase(jobMeta.getDatabases(), oldDatabase.getName());

            // If it exists, change the settings...
            if (newDatabase != null) {
              // 
              // A database connection with the same name exists in
              // the new repository.
              // Change the old connections to reflect the settings in
              // the new repository
              //
              oldDatabase.setDatabaseInterface(newDatabase.getDatabaseInterface());
            } else {
              // 
              // The old database is not present in the new
              // repository: simply add it to the list.
              // When the job gets saved, it will be added
              // to the repository.
              //
              jobMeta.addDatabase(oldDatabase);
            }
          }

          try {
            // For the existing job, change the directory too:
            // Try to find the same directory in the new repository...
            RepositoryDirectoryInterface redi = rep.findDirectory(jobMeta.getRepositoryDirectory().getPath());
            if (redi != null) {
              jobMeta.setRepositoryDirectory(redi);
            } else {
              // the root is the default!
              jobMeta.setRepositoryDirectory(rep.loadRepositoryDirectoryTree()); 
            }
          } catch (KettleException ke) {
            rep = null;
            new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Dialog.ErrorConnectingRepository.Title"), //$NON-NLS-1$
                BaseMessages.getString(PKG, "Spoon.Dialog.ErrorConnectingRepository.Message", //$NON-NLS-1$
                    Const.CR), ke);
          }
        }
        
        TransMeta transMetas[] = getLoadedTransformations();
        for (int t = 0; t < transMetas.length; t++) {
          TransMeta transMeta = transMetas[t];

          for (int i = 0; i < transMeta.nrDatabases(); i++) {
            transMeta.getDatabase(i).setObjectId(null);
          }

          // Set for the existing transformation the ID at -1!
          transMeta.setObjectId(null);

          // Keep track of the old databases for now.
          List<DatabaseMeta> oldDatabases = transMeta.getDatabases();

          // In order to re-match the databases on name (not content), we
          // need to load the databases from the new repository.
          // NOTE: for purposes such as DEVELOP - TEST - PRODUCTION
          // sycles.

          // first clear the list of databases, partition schemas, slave
          // servers, clusters
          transMeta.setDatabases(new ArrayList<DatabaseMeta>());
          transMeta.setPartitionSchemas(new ArrayList<PartitionSchema>());
          transMeta.setSlaveServers(new ArrayList<SlaveServer>());
          transMeta.setClusterSchemas(new ArrayList<ClusterSchema>());

          // Read them from the new repository.
          try {
            SharedObjects sharedObjects = rep.readTransSharedObjects(transMeta);
            sharedObjectsFileMap.put(sharedObjects.getFilename(), sharedObjects);
          } catch (KettleException e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Dialog.ErrorReadingSharedObjects.Title"),
                BaseMessages.getString(PKG, "Spoon.Dialog.ErrorReadingSharedObjects.Message", makeTabName(transMeta,
                    true)), e);
          }

          // Then we need to re-match the databases at save time...
          for (int i = 0; i < oldDatabases.size(); i++) {
            DatabaseMeta oldDatabase = oldDatabases.get(i);
            DatabaseMeta newDatabase = DatabaseMeta.findDatabase(transMeta.getDatabases(), oldDatabase.getName());

            // If it exists, change the settings...
            if (newDatabase != null) {
              // 
              // A database connection with the same name exists in
              // the new repository.
              // Change the old connections to reflect the settings in
              // the new repository
              //
              oldDatabase.setDatabaseInterface(newDatabase.getDatabaseInterface());
            } else {
              // 
              // The old database is not present in the new
              // repository: simply add it to the list.
              // When the transformation gets saved, it will be added
              // to the repository.
              //
              transMeta.addDatabase(oldDatabase);
            }
          }

          try {
            // For the existing transformation, change the directory too:
            // Try to find the same directory in the new repository...
            RepositoryDirectoryInterface redi = rep.findDirectory(transMeta.getRepositoryDirectory().getPath());
            if (redi != null) {
              transMeta.setRepositoryDirectory(redi);
            } else {
              // the root is the default!
              transMeta.setRepositoryDirectory(rep.loadRepositoryDirectoryTree()); 
            }
          } catch (KettleException ke) {
            rep = null;
            new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Dialog.ErrorConnectingRepository.Title"), //$NON-NLS-1$
                BaseMessages.getString(PKG, "Spoon.Dialog.ErrorConnectingRepository.Message", //$NON-NLS-1$
                    Const.CR), ke);
          }
        }

        refreshTree();
        setShellText();
        SpoonPluginManager.getInstance().notifyLifecycleListeners(SpoonLifeCycleEvent.REPOSITORY_CONNECTED);
      }

      public void onError(Throwable t) {
        closeRepository();
        MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
        mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.LoginFailed.Message", t.getLocalizedMessage()));
        mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.LoginFailed.Title"));
        mb.open();

      }

      public void onCancel() {

      }
    });
    loginDialog.show();
  }

  /*
   * public void exploreRepository() { if (rep != null) {
   * RepositoryExplorerDialog.RepositoryExplorerCallback cb = new
   * RepositoryExplorerDialog.RepositoryExplorerCallback() {
   * 
   * public boolean open(RepositoryObjectReference objectToOpen) { String
   * objname = objectToOpen.getName(); if (objname != null) {
   * RepositoryObjectType objectType = objectToOpen.getType();
   * RepositoryDirectory repdir = objectToOpen.getDirectory(); String
   * versionLabel = objectToOpen.getVersionLabel();
   * 
   * loadObjectFromRepository(objname, objectType, repdir, versionLabel); }
   * return false; // do not close explorer } };
   * 
   * RepositoryExplorerDialog erd = new RepositoryExplorerDialog(shell,
   * SWT.NONE, rep, cb, Variables .getADefaultVariableSpace()); erd.open();
   * 
   * } }
   */

  public void clearSharedObjectCache() throws KettleException {
    if (rep != null) {
      rep.clearSharedObjectCache();
      TransMeta transMeta = getActiveTransformation();
      if (transMeta != null) {
        rep.readTransSharedObjects(transMeta);
      }
      JobMeta jobMeta = getActiveJob();
      if (jobMeta != null) {
        rep.readJobMetaSharedObjects(jobMeta);
      }
    }
  }
  
  public void exploreRepository() {
    if (rep != null) {
      final RepositoryExplorerCallback cb = new RepositoryExplorerCallback() {

        public boolean open(UIRepositoryContent element, String revision) {
          String objname = element.getName();
          if (objname != null) {
            RepositoryObjectType objectType = element.getRepositoryElementType();
            RepositoryDirectory repdir = element.getRepositoryDirectory();
            if (element.getObjectId() != null) { // new way
              loadObjectFromRepository(element.getObjectId(), objectType, revision);
            } else { // old way
              loadObjectFromRepository(objname, objectType, repdir, revision);
            }
          }
          return false; // do not close explorer
        }
      };

      
      try {
        final XulWaitBox box = (XulWaitBox) this.mainSpoonContainer.getDocumentRoot().createElement("waitbox");
        box.setIndeterminate(true);
        box.setCanCancel(false);
        box.setTitle(BaseMessages.getString(RepositoryDialogInterface.class, "RepositoryExplorerDialog.Connection.Wait.Title"));
        box.setMessage(BaseMessages.getString(RepositoryDialogInterface.class, "RepositoryExplorerDialog.Explorer.Wait.Message"));
        box.setDialogParent(shell);
        box.setRunnable(new WaitBoxRunnable(box){
          @Override
          public void run() {
              
              shell.getDisplay().syncExec(new Runnable(){
                public void run() {
                  try{
                    RepositoryExplorer explorer = new RepositoryExplorer(shell, rep, cb, Variables.getADefaultVariableSpace());
                    box.stop();
                    explorer.show();
                    explorer.dispose();
                    
                  } catch (final Throwable e) {
                    shell.getDisplay().asyncExec(new Runnable(){
                      public void run() {
                        new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Error"), e.getMessage(), e);  
                      }
                    });
                  }
                }
              });
          }

          @Override
          public void cancel() {
          }
          
        });
        box.start();
      } catch (Throwable e) {
        new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Error"), e.getMessage(), e);
      }
      
    }
  }


  private void loadObjectFromRepository(ObjectId objectId, RepositoryObjectType objectType, String revision) {
    // Try to open the selected transformation.
    if (objectType.equals(RepositoryObjectType.TRANSFORMATION)) {
      try {
        TransLoadProgressDialog progressDialog = new TransLoadProgressDialog(shell, rep, objectId, revision);
        TransMeta transMeta = progressDialog.open();
        transMeta.clearChanged();
        if (transMeta != null) {
          if (log.isDetailed())
            log.logDetailed(BaseMessages.getString(PKG, "Spoon.Log.LoadToTransformation", transMeta.getName(), transMeta.getRepositoryDirectory().getName()));
          props.addLastFile(LastUsedFile.FILE_TYPE_TRANSFORMATION, transMeta.getName(), transMeta.getRepositoryDirectory().getPath(), true, rep.getName());
          addMenuLast();
          addTransGraph(transMeta);
        }
        refreshTree();
        refreshGraph();
      } catch (Exception e) {
        new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), BaseMessages.getString(Spoon.class,
        "Spoon.Dialog.ErrorOpeningById.Message", objectId), e.getMessage(), e); //$NON-NLS-1$
      }
    } else
    // Try to open the selected job.
    if (objectType.equals(RepositoryObjectType.JOB)) {
      try {
        JobLoadProgressDialog progressDialog = new JobLoadProgressDialog(shell, rep, objectId, revision);
        JobMeta jobMeta = progressDialog.open();
        jobMeta.clearChanged();
        if (jobMeta != null) {
          props.addLastFile(LastUsedFile.FILE_TYPE_JOB, jobMeta.getName(), jobMeta.getRepositoryDirectory().getPath(), true, rep.getName());
          saveSettings();
          addMenuLast();
          jobMeta.setArguments(arguments);
          addJobGraph(jobMeta);
        }
        refreshTree();
        refreshGraph();
      } catch (Exception e) {
        new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), BaseMessages.getString(Spoon.class,
            "Spoon.Dialog.ErrorOpeningById.Message", objectId), e.getMessage(), e); //$NON-NLS-1$
      }
    }
  }
  
  public void loadObjectFromRepository(String objname, RepositoryObjectType objectType, RepositoryDirectoryInterface repdir,
      String versionLabel) {
    // Try to open the selected transformation.
    if (objectType.equals(RepositoryObjectType.TRANSFORMATION)) {
      try {
        TransLoadProgressDialog progressDialog = new TransLoadProgressDialog(shell, rep, objname, repdir, versionLabel);
        TransMeta transMeta = progressDialog.open();
        transMeta.clearChanged();
        if (transMeta != null) {
          if (log.isDetailed())
            log.logDetailed(BaseMessages.getString(PKG, "Spoon.Log.LoadToTransformation", objname, repdir.getName()));
          props.addLastFile(LastUsedFile.FILE_TYPE_TRANSFORMATION, objname, repdir.getPath(), true, rep.getName());
          addMenuLast();
          addTransGraph(transMeta);
        }
        refreshTree();
        refreshGraph();
      } catch (Exception e) {
        MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
        mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.ErrorOpening.Message") + objname + Const.CR
            + e.getMessage());// "Error opening : "
        mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.ErrorOpening.Title"));
        mb.open();
      }
    } else
    // Try to open the selected job.
    if (objectType.equals(RepositoryObjectType.JOB)) {
      try {
        JobLoadProgressDialog progressDialog = new JobLoadProgressDialog(shell, rep, objname, repdir, versionLabel);
        JobMeta jobMeta = progressDialog.open();
        jobMeta.clearChanged();
        if (jobMeta != null) {
          props.addLastFile(LastUsedFile.FILE_TYPE_JOB, objname, repdir.getPath(), true, rep.getName());
          saveSettings();
          addMenuLast();
          jobMeta.setArguments(arguments);
          addJobGraph(jobMeta);
        }
        refreshTree();
        refreshGraph();
      } catch (Exception e) {
        MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
        mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.ErrorOpening.Message") + objname + Const.CR
            + e.getMessage());// "Error opening : "
        mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.ErrorOpening.Title"));
        mb.open();
      }
    }
  }

  public void closeRepository() {
    if (rep != null)
      rep.disconnect();
    rep = null;
    setRepository(rep);
    setShellText();
    SpoonPluginManager.getInstance().notifyLifecycleListeners(SpoonLifeCycleEvent.REPOSITORY_DISCONNECTED);
  }

  public void openFile() {
    openFile(false);
  }

  public void importFile() {
    openFile(true);
  }

  public void openFile(boolean importfile) {
    String activePerspectiveId = SpoonPerspectiveManager.getInstance().getActivePerspective().getId();
    boolean etlPerspective = activePerspectiveId.equals(MainSpoonPerspective.ID);

    if (rep == null || importfile || !etlPerspective) // Load from XML
    {
      FileDialog dialog = new FileDialog(shell, SWT.OPEN);

      LinkedHashSet<String> extensions = new LinkedHashSet<String>();
      LinkedHashSet<String> extensionNames = new LinkedHashSet<String>();
      StringBuffer allExtensions = new StringBuffer();
      for (FileListener l : fileListeners) {
        for (String ext : l.getSupportedExtensions()) {
          extensions.add("*." + ext);
          allExtensions.append("*.").append(ext).append(";");
        }
        for (String name : l.getFileTypeDisplayNames(Locale.getDefault())) {
          extensionNames.add(name);
        }
      }
      extensions.add("*");
      extensionNames.add(BaseMessages.getString(PKG, "Spoon.Dialog.OpenFile.AllFiles"));

      String[] exts = new String[extensions.size() + 1];
      exts[0] = allExtensions.toString();
      System.arraycopy(extensions.toArray(new String[extensions.size()]), 0, exts, 1, extensions.size());

      String[] extNames = new String[extensionNames.size() + 1];
      extNames[0] = BaseMessages.getString(PKG, "Spoon.Dialog.OpenFile.AllTypes");
      System
          .arraycopy(extensionNames.toArray(new String[extensionNames.size()]), 0, extNames, 1, extensionNames.size());

      dialog.setFilterExtensions(exts);

      setFilterPath(dialog);
      String fname = dialog.open();
      if (fname != null) {
        lastDirOpened = dialog.getFilterPath();
        openFile(fname, importfile);
      }
    } else {
      SelectObjectDialog sod = new SelectObjectDialog(shell, rep);
      if (sod.open() != null) {
        RepositoryObjectType type = sod.getObjectType();
        String name = sod.getObjectName();
        RepositoryDirectoryInterface repdir = sod.getDirectory();

        // Load a transformation
        if (RepositoryObjectType.TRANSFORMATION.equals(type)) {
          TransLoadProgressDialog tlpd = new TransLoadProgressDialog(shell, rep, name, repdir, null); // Loads
          // the
          // last
          // version
          TransMeta transMeta = tlpd.open();
          sharedObjectsFileMap.put(transMeta.getSharedObjects().getFilename(), transMeta.getSharedObjects());
          setTransMetaVariables(transMeta);

          if (transMeta != null) {
            if (log.isDetailed())
              log.logDetailed(BaseMessages.getString(PKG, "Spoon.Log.LoadToTransformation", name, repdir.getName()));
            props.addLastFile(LastUsedFile.FILE_TYPE_TRANSFORMATION, name, repdir.getPath(), true, rep.getName());
            addMenuLast();
            transMeta.clearChanged();
            // transMeta.setFilename(name); // Don't do it, it's a
            // bad idea!
            addTransGraph(transMeta);
          }
          refreshGraph();
          refreshTree();
        } else
        // Load a job
        if (RepositoryObjectType.JOB.equals(type)) {
          JobLoadProgressDialog jlpd = new JobLoadProgressDialog(shell, rep, name, repdir, null); // Loads
          // the
          // last
          // version
          JobMeta jobMeta = jlpd.open();
          sharedObjectsFileMap.put(jobMeta.getSharedObjects().getFilename(), jobMeta.getSharedObjects());
          setJobMetaVariables(jobMeta);
          if (jobMeta != null) {
            props.addLastFile(LastUsedFile.FILE_TYPE_JOB, name, repdir.getPath(), true, rep.getName());
            saveSettings();
            addMenuLast();
            addJobGraph(jobMeta);
          }
          refreshGraph();
          refreshTree();
        }
      }
    }
  }

  private void setFilterPath(FileDialog dialog) {
    if (!Const.isEmpty(lastDirOpened)) {
      if (new File(lastDirOpened).exists()) {
        dialog.setFilterPath(lastDirOpened);
      }
    }
  }

  private String lastFileOpened = null;

  public String getLastFileOpened() {
    if (lastFileOpened == null) {
      lastFileOpened = System.getProperty("org.pentaho.di.defaultVFSPath", "");
    }
    return lastFileOpened;
  }

  public void setLastFileOpened(String inLastFileOpened) {
    lastFileOpened = inLastFileOpened;
  }

  public void displayCmdLine() {
    String cmdFile = getCmdLine();

    if (Const.isEmpty(cmdFile)) {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
      mb.setMessage(BaseMessages.getString(PKG, "ExportCmdLine.JobOrTransformationMissing.Message"));
      mb.setText(BaseMessages.getString(PKG, "ExportCmdLine.JobOrTransformationMissing.Title"));
      mb.open();
    } else {
      ShowBrowserDialog sbd = new ShowBrowserDialog(shell, BaseMessages.getString(PKG,
          "ExportCmdLine.CommandLine.Title"), cmdFile);
      sbd.open();
    }
  }

  public void createCmdLineFile() {
    String cmdFile = getCmdLine();

    if (Const.isEmpty(cmdFile)) {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
      mb.setMessage(BaseMessages.getString(PKG, "ExportCmdLine.JobOrTransformationMissing.Message"));
      mb.setText(BaseMessages.getString(PKG, "ExportCmdLine.JobOrTransformationMissing.Title"));
      mb.open();
    } else {
      boolean export = true;

      FileDialog dialog = new FileDialog(shell, SWT.SAVE);
      dialog.setFilterExtensions(new String[] { "*.bat", ".sh", "*.*" });
      dialog.setFilterNames(new String[] { BaseMessages.getString(PKG, "ExportCmdLine.BatFiles"),
          BaseMessages.getString(PKG, "ExportCmdLineShFiles"), BaseMessages.getString(PKG, "ExportCmdLine.AllFiles") });
      String fname = dialog.open();

      if (fname != null) {
        // See if the file already exists...
        int id = SWT.YES;
        try {
          FileObject f = KettleVFS.getFileObject(fname);
          if (f.exists()) {
            MessageBox mb = new MessageBox(shell, SWT.NO | SWT.YES | SWT.ICON_WARNING);
            mb.setMessage(BaseMessages.getString(PKG, "ExportCmdLineShFiles.FileExistsReplace", fname));
            mb.setText(BaseMessages.getString(PKG, "ExportCmdLineShFiles.ConfirmOverwrite"));
            id = mb.open();
          }
        } catch (Exception e) {
        }
        if (id == SWT.NO) {
          export = false;
        }

        if (export) {
          java.io.FileWriter out = null;
          try {
            out = new java.io.FileWriter(fname);
            out.write(cmdFile);
            out.flush();
          } catch (Exception e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "ExportCmdLineShFiles.ErrorWritingFile.Title"),
                BaseMessages.getString(PKG, "ExportCmdLineShFiles.ErrorWritingFile.Message", fname), e);
          } finally {
            if (out != null) {
              try {
                out.close();
              } catch (Exception e) {
              }
            }
          }

          MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
          mb.setMessage(BaseMessages.getString(PKG, "ExportCmdLineShFiles.CmdExported.Message", fname));
          mb.setText(BaseMessages.getString(PKG, "ExportCmdLineShFiles.CmdExported.Title"));
          mb.open();
        }
      }
    }
  }

  private String getCmdLine() {
    TransMeta transMeta = getActiveTransformation();
    JobMeta jobMeta = getActiveJob();
    String cmdFile = "";

    if (rep != null && (jobMeta != null || transMeta != null)) {
      if (jobMeta != null) {
        if (jobMeta.getName() != null) {
          if (Const.isWindows()) {
            cmdFile = "kitchen " + "/rep:\"" + rep.getName() + "\"" + " /user:\"" + rep.getUserInfo().getLogin() + "\""
                + " /pass:\"" + Encr.encryptPasswordIfNotUsingVariables(rep.getUserInfo().getPassword()) + "\""
                + " /job:\"" + jobMeta.getName() + '"' + " /dir:\"" + jobMeta.getRepositoryDirectory().getPath() + "\""
                + " /level:Basic";
          } else {
            cmdFile = "sh kitchen.sh " + "-rep='" + rep.getName() + "'" + " -user='" + rep.getUserInfo().getLogin()
                + "'" + " -pass='" + Encr.encryptPasswordIfNotUsingVariables(rep.getUserInfo().getPassword()) + "'"
                + " -job='" + jobMeta.getName() + "'" + " -dir='" + jobMeta.getRepositoryDirectory().getPath() + "'"
                + " -level=Basic";
          }
        }
      } else {
        if (transMeta.getName() != null) {
          if (Const.isWindows()) {
            cmdFile = "pan " + "/rep:\"" + rep.getName() + "\"" + " /user:\"" + rep.getUserInfo().getLogin() + "\""
                + " /pass:\"" + Encr.encryptPasswordIfNotUsingVariables(rep.getUserInfo().getPassword()) + "\""
                + " /trans:\"" + transMeta.getName() + "\"" + " /dir:\"" + transMeta.getRepositoryDirectory().getPath()
                + "\"" + " /level:Basic";
          } else {
            cmdFile = "sh pan.sh " + "-rep='" + rep.getName() + "'" + " -user='" + rep.getUserInfo().getLogin() + "'"
                + " -pass='" + Encr.encryptPasswordIfNotUsingVariables(rep.getUserInfo().getPassword()) + "'"
                + " -trans='" + transMeta.getName() + "'" + " -dir='" + transMeta.getRepositoryDirectory().getPath()
                + "'" + " -level=Basic";
          }
        }
      }
    } else if (rep == null && (jobMeta != null || transMeta != null)) {
      if (jobMeta != null) {
        if (jobMeta.getFilename() != null) {
          if (Const.isWindows()) {
            cmdFile = "kitchen " + "/file:\"" + jobMeta.getFilename() + "\"" + " /level:Basic";
          } else {
            cmdFile = "sh kitchen.sh " + "-file='" + jobMeta.getFilename() + "'" + " -level=Basic";
          }
        }
      } else {
        if (transMeta.getFilename() != null) {
          if (Const.isWindows()) {
            cmdFile = "pan " + "/file:\"" + transMeta.getFilename() + "\"" + " /level:Basic";
          } else {
            cmdFile = "sh pan.sh " + "-file:'" + transMeta.getFilename() + "'" + " -level=Basic";
          }
        }
      }
    }
    return cmdFile;

  }

  // private String lastVfsUsername="";
  // private String lastVfsPassword="";

  public void openFileVFSFile() {
    FileObject initialFile = null;
    FileObject rootFile = null;
    try {
      initialFile = KettleVFS.getFileObject(getLastFileOpened());
      rootFile = initialFile.getFileSystem().getRoot();
    } catch (Exception e) {
      String message = Const.getStackTracker(e);
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Error"), message, e);

      // bring up a dialog to prompt for userid/password and try again
      // lastVfsUsername = "";
      // lastVfsPassword = "";
      // if (lastFileOpened != null && lastFileOpened.indexOf("@") == -1)
      // {
      // lastFileOpened = lastFileOpened.substring(0,
      // lastFileOpened.indexOf("//")+2) + lastVfsUsername + ":" +
      // lastVfsPassword + "@" +
      // lastFileOpened.substring(lastFileOpened.indexOf("//")+2);
      // }
      // openFileVFSFile();
      return;
    }

    FileObject selectedFile = getVfsFileChooserDialog(rootFile, initialFile).open(shell, null, Const.STRING_TRANS_AND_JOB_FILTER_EXT, Const
        .getTransformationAndJobFilterNames(), VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE);
    if (selectedFile != null) {
      setLastFileOpened(selectedFile.getName().getFriendlyURI());
      openFile(selectedFile.getName().getFriendlyURI(), false);
    }
  }
  
  public void addFileListener(FileListener listener) {
    this.fileListeners.add(listener);
    for (String s : listener.getSupportedExtensions()) {
      if (fileExtensionMap.containsKey(s) == false) {
        fileExtensionMap.put(s, listener);
      }
    }
  }

  public void openFile(String fname, boolean importfile) {
    // Open the XML and see what's in there.
    // We expect a single <transformation> or <job> root at this time...

    boolean loaded = false;
    FileListener listener = null;
    Node root = null;
    // match by extension first
    int idx = fname.lastIndexOf('.');
    if (idx != -1) {
      for (FileListener li : fileListeners) {
        if (li.accepts(fname)) {
          listener = li;
          break;
        }
      }
    }

    // Attempt to find a root XML node name. Fails gracefully for non-XML file
    // types.
    try {
      Document document = XMLHandler.loadXMLFile(fname);
      root = document.getDocumentElement();
    } catch (KettleXMLException e) {
      if (log.isDetailed()) {
        log.logDetailed( BaseMessages.getString(PKG, "Spoon.File.Xml.Parse.Error"));
      }
    }

    // otherwise try by looking at the root node if we were able to parse file
    // as XML
    if (listener == null && root != null) {
      for (FileListener li : fileListeners) {
        if (li.acceptsXml(root.getNodeName())) {
          listener = li;
          break;
        }
      }
    }

    // You got to have a file name!
    //
    if (!Const.isEmpty(fname)) {

      if (listener != null) {
        loaded = listener.open(root, fname, importfile);
      }
      if (!loaded) {
        // Give error back
        hideSplash();
        MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
        mb.setMessage(BaseMessages.getString(PKG, "Spoon.UnknownFileType.Message", fname));
        mb.setText(BaseMessages.getString(PKG, "Spoon.UnknownFileType.Title"));
        mb.open();
      } else {
        applyVariables(); // set variables in the newly loaded
        // transformation(s) and job(s).
      }
    }
  }

  public PropsUI getProperties() {
    return props;
  }

  /*
   * public void newFileDropDown() { newFileDropDown(toolbar); }
   */

  public void newFileDropDown() {
    // Drop down a list below the "New" icon (new.png)
    // First problem: where is that icon?
    XulToolbarbutton button = (XulToolbarbutton) this.mainToolbar.getElementById("file-new"); // = usedToolBar.getButtonById("file-new");
    Object object = button.getManagedObject();
    if (object instanceof ToolItem) {
      // OK, let's determine the location of this widget...
      //
      ToolItem item = (ToolItem) object;
      Rectangle bounds = item.getBounds();
      org.eclipse.swt.graphics.Point p = item.getParent().toDisplay(
          new org.eclipse.swt.graphics.Point(bounds.x, bounds.y));

      fileMenus.setLocation(p.x, p.y + bounds.height);
      fileMenus.setVisible(true);
    }
  }

  public void newTransFile() {
    TransMeta transMeta = new TransMeta();
    transMeta.addObserver(this);

    // Set the variables that were previously defined in this session on the
    // transformation metadata too.
    //
    setTransMetaVariables(transMeta);

    try {
      SharedObjects sharedObjects = rep != null ? rep.readTransSharedObjects(transMeta) : transMeta.readSharedObjects();
      sharedObjectsFileMap.put(sharedObjects.getFilename(), sharedObjects);

      transMeta.clearChanged();
    } catch (Exception e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingSharedObjects.Title"),
          BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingSharedObjects.Message"), e);
    }

    // Set the location of the new transMeta to that of the default location or the last saved location
    transMeta.setRepositoryDirectory(getDefaultSaveLocation(transMeta));

    int nr = 1;
    transMeta.setName(STRING_TRANSFORMATION + " " + nr);

    // See if a transformation with the same name isn't already loaded...
    //
    while (findTransformation(delegates.tabs.makeTabName(transMeta, false)) != null) {
      nr++;
      transMeta.setName(STRING_TRANSFORMATION + " " + nr); // rename
    }
    addTransGraph(transMeta);
    applyVariables();

    // switch to design mode...
    //
    if (setDesignMode()) {
      // No refresh done yet, do so
      refreshTree();
    }
    loadPerspective(MainSpoonPerspective.ID);
  }

  public void newJobFile() {
    try {
      JobMeta jobMeta = new JobMeta();
      jobMeta.addObserver(this);

      // Set the variables that were previously defined in this session on
      // the transformation metadata too.
      //
      setJobMetaVariables(jobMeta);

      try {
        SharedObjects sharedObjects = rep != null ? rep.readJobMetaSharedObjects(jobMeta) : jobMeta.readSharedObjects();
        sharedObjectsFileMap.put(sharedObjects.getFilename(), sharedObjects);
      } catch (KettleException e) {
        new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Dialog.ErrorReadingSharedObjects.Title"),
            BaseMessages.getString(PKG, "Spoon.Dialog.ErrorReadingSharedObjects.Message", delegates.tabs.makeTabName(
                jobMeta, true)), e);
      }

      // Set the location of the new jobMeta to that of the default location or the last saved location
      jobMeta.setRepositoryDirectory(getDefaultSaveLocation(jobMeta));

      int nr = 1;
      jobMeta.setName(STRING_JOB + " " + nr);

      // See if a transformation with the same name isn't already
      // loaded...
      while (findJob(delegates.tabs.makeTabName(jobMeta, false)) != null) {
        nr++;
        jobMeta.setName(STRING_JOB + " " + nr); // rename
      }

      addJobGraph(jobMeta);
      applyVariables();

      // switch to design mode...
      //
      if (setDesignMode()) {
        // No refresh done yet, do so
        refreshTree();
      }
      loadPerspective(MainSpoonPerspective.ID);
    } catch (Exception e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Exception.ErrorCreatingNewJob.Title"), BaseMessages
          .getString(PKG, "Spoon.Exception.ErrorCreatingNewJob.Message"), e);
    }
  }

  /**
   * Set previously defined variables (set variables dialog) on the specified
   * transformation
   * 
   * @param transMeta
   */
  public void setTransMetaVariables(TransMeta transMeta) {
    for (int i = 0; i < variables.size(); i++) {
      try {
        String name = variables.getValueMeta(i).getName();
        String value = variables.getString(i, "");

        transMeta.setVariable(name, Const.NVL(value, ""));
      } catch (Exception e) {
        // Ignore the exception, it should never happen on a getString()
        // anyway.
      }
    }

    // Also set the parameters
    //
    setParametersAsVariablesInUI(transMeta, transMeta);
  }

  /**
   * Set previously defined variables (set variables dialog) on the specified
   * job
   * 
   * @param jobMeta
   */
  public void setJobMetaVariables(JobMeta jobMeta) {
    for (int i = 0; i < variables.size(); i++) {
      try {
        String name = variables.getValueMeta(i).getName();
        String value = variables.getString(i, "");

        jobMeta.setVariable(name, Const.NVL(value, ""));
      } catch (Exception e) {
        // Ignore the exception, it should never happen on a getString()
        // anyway.
      }
    }

    // Also set the parameters
    //
    setParametersAsVariablesInUI(jobMeta, jobMeta);
  }

  public void loadRepositoryObjects(TransMeta transMeta) {
    // Load common database info from active repository...
    if (rep != null) {
      try {
        SharedObjects sharedObjects = rep != null ? rep.readTransSharedObjects(transMeta) : transMeta
            .readSharedObjects();
        sharedObjectsFileMap.put(sharedObjects.getFilename(), sharedObjects);
      } catch (Exception e) {
        new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Error.UnableToLoadSharedObjects.Title"), BaseMessages
            .getString(PKG, "Spoon.Error.UnableToLoadSharedObjects.Message"), e);
      }

    }
  }

  public boolean quitFile() throws KettleException {
    if (log.isDetailed())
      log.logDetailed(BaseMessages.getString(PKG, "Spoon.Log.QuitApplication"));// "Quit application."

    boolean exit = true;

    saveSettings();

    if (props.showExitWarning()) {
      // Display message: are you sure you want to exit?
      //
      MessageDialogWithToggle md = new MessageDialogWithToggle(shell, BaseMessages.getString(PKG, "System.Warning"),// "Warning!"
          null, BaseMessages.getString(PKG, "Spoon.Message.Warning.PromptExit"),

          MessageDialog.WARNING, new String[] { BaseMessages.getString(PKG, "Spoon.Message.Warning.Yes"),
              BaseMessages.getString(PKG, "Spoon.Message.Warning.No") },// "Yes",
          // "No"
          1, BaseMessages.getString(PKG, "Spoon.Message.Warning.NotShowWarning"),// "Please, don't show this warning anymore."
          !props.showExitWarning());
      MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
      int idx = md.open();
      props.setExitWarningShown(!md.getToggleState());
      props.saveProps();
      if ((idx & 0xFF) == 1)
        return false; // No selected: don't exit!
    }

    // Check all tabs to see if we can close them...
    //
    List<TabMapEntry> list = delegates.tabs.getTabs();

    for (TabMapEntry mapEntry : list) {
      TabItemInterface itemInterface = mapEntry.getObject();

      if (!itemInterface.canBeClosed()) {
        // Show the tab
        tabfolder.setSelected(mapEntry.getTabItem());

        // Unsaved work that needs to changes to be applied?
        //
        int reply = itemInterface.showChangedWarning();
        if (reply == SWT.YES) {
          exit = itemInterface.applyChanges();
        } else {
          if (reply == SWT.CANCEL) {
            exit = false;
          } else // SWT.NO
          {
            exit = true;
          }
        }
      }
    }

    if (exit) // we have asked about it all and we're still here. Now close
    // all the tabs, stop the running transformations
    {
      for (TabMapEntry mapEntry : list) {
        if (!mapEntry.getObject().canBeClosed()) {
          // Unsaved transformation?
          //
          if (mapEntry.getObject() instanceof TransGraph) {
            TransMeta transMeta = (TransMeta) mapEntry.getObject().getManagedObject();
            if (transMeta.hasChanged()) {
              delegates.tabs.removeTab(mapEntry);
            }
          }
          // A running transformation?
          //
          if (mapEntry.getObject() instanceof TransGraph) {
            TransGraph transGraph = (TransGraph) mapEntry.getObject();
            if (transGraph.isRunning()) {
              transGraph.stop();
              delegates.tabs.removeTab(mapEntry);
            }
          }
        }
      }
    }

    // and now we call the listeners

    try {
      lifecycleSupport.onExit(this);
    } catch (LifecycleException e) {
      MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
      box.setMessage(e.getMessage());
      box.open();
    }

    if (exit)
      dispose();

    return exit;
  }

  public boolean saveFile() {
    try {
      EngineMetaInterface meta = getActiveMeta();
      if (meta != null) {
        return saveToFile(meta);
      }
    } catch(Exception e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.File.Save.Fail.Title"), BaseMessages.getString(PKG, "Spoon.File.Save.Fail.Message"), e);
    }
    return false;
  }

  public boolean saveToFile(EngineMetaInterface meta) throws KettleException {
    if (meta == null)
      return false;

    boolean saved = false;

    if (log.isDetailed())
      // "Save to file or repository...
      log.logDetailed(BaseMessages.getString(PKG, "Spoon.Log.SaveToFileOrRepository"));

    String activePerspectiveId = SpoonPerspectiveManager.getInstance().getActivePerspective().getId();
    boolean etlPerspective = activePerspectiveId.equals(MainSpoonPerspective.ID);
    if (rep != null && etlPerspective) {
      saved = saveToRepository(meta);
    } else {
      if (meta.getFilename() != null) {
        saved = save(meta, meta.getFilename(), false);
      } else {
        if (meta.canSave()) {
          saved = saveFileAs(meta);
        }
      }
    }

    meta.saveSharedObjects(); // throws Exception in case anything goes wrong

    try {
      if (props.useDBCache() && meta instanceof TransMeta)
        ((TransMeta) meta).getDbCache().saveCache();
    } catch (KettleException e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Dialog.ErrorSavingDatabaseCache.Title"), BaseMessages
          .getString(PKG, "Spoon.Dialog.ErrorSavingDatabaseCache.Message"), e);// "An error occured saving the database cache to disk"
    }

    delegates.tabs.renameTabs(); // filename or name of transformation might
    // have changed.
    refreshTree();

    // Update menu status for the newly saved object
    enableMenus();
    
    return saved;
  }

  public boolean saveToRepository(EngineMetaInterface meta) throws KettleException {
    return saveToRepository(meta, meta.getObjectId() == null);
  }

  public boolean saveToRepository(EngineMetaInterface meta, boolean ask_name) throws KettleException {
    
    // Verify repository security first...
    //
    if (meta.getFileType().equals(LastUsedFile.FILE_TYPE_TRANSFORMATION)) {
      if (RepositorySecurityUI.verifyOperations(shell, rep, RepositoryOperation.MODIFY_TRANSFORMATION)) {
        return false;
      }
    }
    if (meta.getFileType().equals(LastUsedFile.FILE_TYPE_JOB)) {
      if (RepositorySecurityUI.verifyOperations(shell, rep, RepositoryOperation.MODIFY_JOB)) {
        return false;
      }
    }

    if (log.isDetailed())
      // "Save to repository..."
      //
      log.logDetailed(BaseMessages.getString(PKG, "Spoon.Log.SaveToRepository"));
    if (rep != null) {
      boolean answer = true;
      boolean ask = ask_name;

      // If the repository directory is root then get the default save directory
      if(meta.getRepositoryDirectory() == null || meta.getRepositoryDirectory().isRoot()) {
        meta.setRepositoryDirectory(rep.getDefaultSaveDirectory(meta));
      }
      while (answer && (ask || Const.isEmpty(meta.getName()))) {
        if (!ask) {
          MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);

          // "Please give this transformation a name before saving it in the database."
          mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.PromptTransformationName.Message"));
          // "Transformation has no name."
          mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.PromptTransformationName.Title"));
          mb.open();
        }
        ask = false;
        if (meta instanceof TransMeta) {
          answer = TransGraph.editProperties((TransMeta) meta, this, rep, false);
        }
        if (meta instanceof JobMeta) {
          answer = JobGraph.editProperties((JobMeta) meta, this, rep, false);
        }
      }

      if (answer && !Const.isEmpty(meta.getName())) {

        int response = SWT.YES;

        ObjectId existingId = null;
        if (meta instanceof TransMeta) {
          existingId = rep.getTransformationID(meta.getName(), meta.getRepositoryDirectory());
        }
        if (meta instanceof JobMeta) {
          existingId = rep.getJobId(meta.getName(), meta.getRepositoryDirectory());
        }

        // If there is no object id (import from XML) and there is an existing object.
        //
        // or...
        //
        // If the transformation/job has an object id and it's different from the one in the repository.
        //
        if ((meta.getObjectId() == null && existingId != null) || existingId != null
            && !meta.getObjectId().equals(existingId)) {
          // In case we support revisions, we can simply overwrite
          // without a problem so we simply don't ask.
          // However, if we import from a file we should ask.
          //
          if (!rep.getRepositoryMeta().getRepositoryCapabilities().supportsRevisions() || meta.getObjectId() == null) {
            MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);

            // There already is a transformation called ... in the repository.
            // Do you want to overwrite the transformation?
            //
            mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.PromptOverwriteTransformation.Message", meta
                .getName(), Const.CR));
            mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.PromptOverwriteTransformation.Title"));
            response = mb.open();
          }
        }

        boolean saved = false;
        if (response == SWT.YES) {

          if (meta.getObjectId() == null) {
            meta.setObjectId(existingId);
          }

          try {
            shell.setCursor(cursor_hourglass);

            // Keep info on who & when this transformation was
            // created and or modified...
            if (meta.getCreatedDate() == null) {
              meta.setCreatedDate(new Date());
              if (capabilities.supportsUsers()) {
                meta.setCreatedUser(rep.getUserInfo().getLogin());
              }
            }

            // Keep info on who & when this transformation was
            // changed...
            meta.setModifiedDate(new Date());
            if (capabilities.supportsUsers()) {
              meta.setModifiedUser(rep.getUserInfo().getLogin());
            }

            // Finally before saving, ask for a version comment (if
            // applicable)
            //
            String versionComment = null;
            boolean versionOk = false;
            while (!versionOk) {
              versionComment = RepositorySecurityUI.getVersionComment(shell, rep, meta.getName());
              
              // if the version comment is null, the user hit cancel, exit.
              if (rep != null && rep.getSecurityProvider() != null && 
                  rep.getSecurityProvider().allowsVersionComments() && 
                  versionComment == null) 
              {
                return false;
              }
              
              if (Const.isEmpty(versionComment) && rep.getSecurityProvider().isVersionCommentMandatory()) {
                if (!RepositorySecurityUI.showVersionCommentMandatoryDialog(shell)) {
                  return false; // no, I don't want to enter a
                  // version comment and yes,
                  // it's mandatory.
                }
              } else {
                versionOk = true;
              }
            }

            if (versionOk) {
              SaveProgressDialog tspd = new SaveProgressDialog(shell, rep, meta, versionComment);
              if (tspd.open()) {
                saved = true;
                if (!props.getSaveConfirmation()) {
                  MessageDialogWithToggle md = new MessageDialogWithToggle(shell, BaseMessages.getString(PKG,
                      "Spoon.Message.Warning.SaveOK"), null, BaseMessages.getString(PKG,
                      "Spoon.Message.Warning.TransformationWasStored"), MessageDialog.QUESTION,
                      new String[] { BaseMessages.getString(PKG, "Spoon.Message.Warning.OK") }, 0, BaseMessages
                          .getString(PKG, "Spoon.Message.Warning.NotShowThisMessage"), props.getSaveConfirmation());
                  MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
                  md.open();
                  props.setSaveConfirmation(md.getToggleState());
                }

                // Handle last opened files...
                props.addLastFile(meta.getFileType(), meta.getName(), meta.getRepositoryDirectory().getPath(), true,
                    getRepositoryName());
                saveSettings();
                addMenuLast();

                setShellText();
              }
            }
          } finally {
            shell.setCursor(null);
          }
        }
        return saved;
      }
    } else {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      // "There is no repository connection available."
      mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.NoRepositoryConnection.Message"));
      // "No repository available."
      mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.NoRepositoryConnection.Title"));
      mb.open();
    }
    return false;
  }

  public boolean saveJobRepository(JobMeta jobMeta) throws KettleException {
    return saveToRepository(jobMeta, false);
  }

  public boolean saveJobRepository(JobMeta jobMeta, boolean ask_name) throws KettleException {
    return saveToRepository(jobMeta, ask_name);
  }

  public boolean saveFileAs() throws KettleException {
    try {
      EngineMetaInterface meta = getActiveMeta();
      if (meta != null) {
        if (meta.canSave()) {
          return saveFileAs(meta);
        }
      }
    } catch(Exception e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.File.Save.Fail.Title"), BaseMessages.getString(PKG, "Spoon.File.Save.Fail.Message"), e);
    }
    
    return false;
  }

  public boolean saveFileAs(EngineMetaInterface meta) throws KettleException {
    boolean saved = false;

    if (log.isBasic())
      log.logBasic(BaseMessages.getString(PKG, "Spoon.Log.SaveAs"));// "Save as..."

    String activePerspectiveId = SpoonPerspectiveManager.getInstance().getActivePerspective().getId();
    boolean etlPerspective = activePerspectiveId.equals(MainSpoonPerspective.ID);
    if (rep != null && etlPerspective) {
      meta.setObjectId(null);
      saved = saveToRepository(meta, true);

    } else {
      saved = saveXMLFile(meta, false);
    }

    delegates.tabs.renameTabs(); // filename or name of transformation might
    // have changed.
    refreshTree();
    if(saved && (meta instanceof TransMeta || meta instanceof JobMeta)) {
      TabMapEntry tabEntry = delegates.tabs.findTabMapEntry(meta);
      TabItem tabItem = tabEntry.getTabItem();
      if(meta.getFileType().equals(LastUsedFile.FILE_TYPE_TRANSFORMATION)) {
        tabItem.setImage(GUIResource.getInstance().getImageTransGraph());  
      } else if(meta.getFileType().equals(LastUsedFile.FILE_TYPE_JOB)) {
        tabItem.setImage(GUIResource.getInstance().getImageJobGraph());
      }
    }
    
    // Update menu status for the newly saved object
    enableMenus();
    return saved;
  }

  public boolean exportXMLFile() {
    return saveXMLFile(true);
  }

  /**
   * Export this job or transformation including all depending resources to a
   * single zip file.
   */
  public void exportAllXMLFile() {

    ResourceExportInterface resourceExportInterface = getActiveTransformation();
    if (resourceExportInterface == null)
      resourceExportInterface = getActiveJob();
    if (resourceExportInterface == null)
      return; // nothing to do here, prevent an NPE

    // ((VariableSpace)resourceExportInterface).getVariable("Internal.Transformation.Filename.Directory");

    // Ask the user for a zip file to export to:
    //
    try {
      String zipFilename = null;
      while (Const.isEmpty(zipFilename)) {
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setText(BaseMessages.getString(PKG, "Spoon.ExportResourceSelectZipFile"));
        dialog.setFilterExtensions(new String[] { "*.zip;*.ZIP", "*" });
        dialog.setFilterNames(new String[] { BaseMessages.getString(PKG, "System.FileType.ZIPFiles"),
            BaseMessages.getString(PKG, "System.FileType.AllFiles"), });
        setFilterPath(dialog);
        if (dialog.open() != null) {
          lastDirOpened = dialog.getFilterPath();
          zipFilename = dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName();
          FileObject zipFileObject = KettleVFS.getFileObject(zipFilename);
          if (zipFileObject.exists()) {
            MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL);
            box.setMessage(BaseMessages.getString(PKG, "Spoon.ExportResourceZipFileExists.Message", zipFilename));
            box.setText(BaseMessages.getString(PKG, "Spoon.ExportResourceZipFileExists.Title"));
            int answer = box.open();
            if (answer == SWT.CANCEL)
              return;
            if (answer == SWT.NO)
              zipFilename = null;
          }
        } else {
          return;
        }
      }

      // Export the resources linked to the currently loaded file...
      //
      TopLevelResource topLevelResource = ResourceUtil.serializeResourceExportInterface(zipFilename,
          resourceExportInterface, (VariableSpace) resourceExportInterface, rep);
      String message = ResourceUtil.getExplanation(zipFilename, topLevelResource.getResourceName(),
          resourceExportInterface);

      /*
       * // Add the ZIP file as a repository to the repository list... //
       * RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
       * repositoriesMeta.readData();
       * 
       * KettleFileRepositoryMeta fileRepositoryMeta = new
       * KettleFileRepositoryMeta( KettleFileRepositoryMeta.REPOSITORY_TYPE_ID,
       * "Export " + baseFileName, "Export to file : " + zipFilename, "zip://" +
       * zipFilename + "!"); fileRepositoryMeta.setReadOnly(true); // A ZIP file
       * is read-only int nr = 2; String baseName =
       * fileRepositoryMeta.getName(); while
       * (repositoriesMeta.findRepository(fileRepositoryMeta.getName()) != null)
       * { fileRepositoryMeta.setName(baseName + " " + nr); nr++; }
       * 
       * repositoriesMeta.addRepository(fileRepositoryMeta);
       * repositoriesMeta.writeData();
      */

      // Show some information concerning all this work...
      
      EnterTextDialog enterTextDialog = new EnterTextDialog(shell, BaseMessages.getString(PKG, "Spoon.Dialog.ResourceSerialized"), BaseMessages.getString(PKG, "Spoon.Dialog.ResourceSerializedSuccesfully"), message);
      enterTextDialog.setReadOnly();
      enterTextDialog.open();
    } catch (Exception e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Error"), BaseMessages.getString(PKG, "Spoon.ErrorExportingFile"), e); 
    }
  }

  /**
   * Export this job or transformation including all depending resources to a
   * single ZIP file containing a file repository.
   */
  public void exportAllFileRepository() {

    ResourceExportInterface resourceExportInterface = getActiveTransformation();
    if (resourceExportInterface == null)
      resourceExportInterface = getActiveJob();
    if (resourceExportInterface == null)
      return; // nothing to do here, prevent an NPE

    // Ask the user for a zip file to export to:
    //
    try {
      String zipFilename = null;
      while (Const.isEmpty(zipFilename)) {
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setText(BaseMessages.getString(PKG, "Spoon.ExportResourceSelectZipFile"));
        dialog.setFilterExtensions(new String[] { "*.zip;*.ZIP", "*" });
        dialog.setFilterNames(new String[] { BaseMessages.getString(PKG, "System.FileType.ZIPFiles"),
            BaseMessages.getString(PKG, "System.FileType.AllFiles"), });
        setFilterPath(dialog);
        if (dialog.open() != null) {
          lastDirOpened = dialog.getFilterPath();
          zipFilename = dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName();
          FileObject zipFileObject = KettleVFS.getFileObject(zipFilename);
          if (zipFileObject.exists()) {
            MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL);
            box.setMessage(BaseMessages.getString(PKG, "Spoon.ExportResourceZipFileExists.Message", zipFilename));
            box.setText(BaseMessages.getString(PKG, "Spoon.ExportResourceZipFileExists.Title"));
            int answer = box.open();
            if (answer == SWT.CANCEL)
              return;
            if (answer == SWT.NO)
              zipFilename = null;
          }
        } else {
          return;
        }
      }

      // Export the resources linked to the currently loaded file...
      //
      TopLevelResource topLevelResource = ResourceUtil.serializeResourceExportInterface(zipFilename,
          resourceExportInterface, (VariableSpace) resourceExportInterface, rep);
      String message = ResourceUtil.getExplanation(zipFilename, topLevelResource.getResourceName(),
          resourceExportInterface);

      /*
       * // Add the ZIP file as a repository to the repository list... //
       * RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
       * repositoriesMeta.readData();
       * 
       * KettleFileRepositoryMeta fileRepositoryMeta = new
       * KettleFileRepositoryMeta( KettleFileRepositoryMeta.REPOSITORY_TYPE_ID,
       * "Export " + baseFileName, "Export to file : " + zipFilename, "zip://" +
       * zipFilename + "!"); fileRepositoryMeta.setReadOnly(true); // A ZIP file
       * is read-only int nr = 2; String baseName =
       * fileRepositoryMeta.getName(); while
       * (repositoriesMeta.findRepository(fileRepositoryMeta.getName()) != null)
       * { fileRepositoryMeta.setName(baseName + " " + nr); nr++; }
       * 
       * repositoriesMeta.addRepository(fileRepositoryMeta);
       * repositoriesMeta.writeData();
       */

      // Show some information concerning all this work...
      //
      EnterTextDialog enterTextDialog = new EnterTextDialog(shell, BaseMessages.getString(PKG, "Spoon.Dialog.ResourceSerialized"), BaseMessages.getString(PKG, "Spoon.Dialog.ResourceSerializedSuccesfully"), message);
      enterTextDialog.setReadOnly();
      enterTextDialog.open();
    } catch (Exception e) {
	new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Error"), BaseMessages.getString(PKG, "Spoon.ErrorExportingFile"), e); 
    }
  }

  public void exportRepositoryAll() {
    exportRepositoryDirectory(null);
  }

  /**
   * @param directoryToExport set to null to export the complete repository
   * @return false if we want to stop processing. true if we need to continue.
   */
  public boolean exportRepositoryDirectory(RepositoryDirectory directoryToExport) {
    
    if (directoryToExport!=null) {
      MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.APPLICATION_MODAL | SWT.YES | SWT.NO | SWT.CANCEL);
      box.setText(BaseMessages.getString(PKG, "Spoon.QuestionExportDirectory.Title"));
      box.setMessage(BaseMessages.getString(PKG, "Spoon.QuestionExportFolder.Message", Const.CR, directoryToExport.getPath()));
      int answer = box.open();
      if (answer==SWT.NO) return true;
      if (answer==SWT.CANCEL) return false;
    }
    
    FileDialog dialog = new FileDialog(shell, SWT.SAVE | SWT.SINGLE);
    dialog.setText(BaseMessages.getString(PKG, "Spoon.SelectAnXMLFileToExportTo.Message"));
    if (dialog.open() == null) return false;
    
    String filename = dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName();
    log.logBasic(BaseMessages.getString(PKG, "Spoon.Log.Exporting"), BaseMessages.getString(PKG, "Spoon.Log.ExportObjectsToFile", filename));
    
    MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.APPLICATION_MODAL | SWT.YES | SWT.NO | SWT.CANCEL);
    box.setText(BaseMessages.getString(PKG, "Spoon.QuestionApplyImportRulesToExport.Title"));
    box.setMessage(BaseMessages.getString(PKG, "Spoon.QuestionApplyImportRulesToExport.Message"));
    int answer = box.open();
    if (answer==SWT.CANCEL) return false;
    
    // Get the import rules
    //
    ImportRules importRules = new ImportRules();
    if (answer==SWT.YES){
      ImportRulesDialog importRulesDialog = new ImportRulesDialog(shell, importRules);
      if (!importRulesDialog.open()) return false;
    }

    RepositoryExportProgressDialog repd = new RepositoryExportProgressDialog(shell, rep, directoryToExport, filename, importRules);
    repd.open();
    
    return true;
  }

  public void importDirectoryToRepository() {
    FileDialog dialog = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
    dialog.setText(BaseMessages.getString(PKG, "Spoon.SelectAnXMLFileToImportFrom.Message"));
    if (dialog.open() == null) return;
    
    // Ask for a set of import rules
    //
    MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.APPLICATION_MODAL | SWT.YES | SWT.NO | SWT.CANCEL);
    box.setText(BaseMessages.getString(PKG, "Spoon.QuestionApplyImportRules.Title"));
    box.setMessage(BaseMessages.getString(PKG, "Spoon.QuestionApplyImportRules.Message"));
    int answer = box.open();
    if (answer==SWT.CANCEL) return;
    
    // Get the import rules
    //
    ImportRules importRules = new ImportRules();
    if (answer==SWT.YES){
      ImportRulesDialog importRulesDialog = new ImportRulesDialog(shell, importRules);
      if (!importRulesDialog.open()) return;
    }

    // Ask for a destination in the repository...
    //
    SelectDirectoryDialog sdd = new SelectDirectoryDialog(shell, SWT.NONE, rep);
    RepositoryDirectoryInterface baseDirectory = sdd.open();
    if (baseDirectory == null) return;
    
    // Finally before importing, ask for a version comment (if applicable)
    //
    String versionComment = null;
    boolean versionOk = false;
    while (!versionOk) {
      versionComment = RepositorySecurityUI.getVersionComment(shell, rep, "Import of files into ["
          + baseDirectory.getPath() + "]");
      // if the version comment is null, the user hit cancel, exit.
      if (rep != null && rep.getSecurityProvider() != null && 
          rep.getSecurityProvider().allowsVersionComments() && 
          versionComment == null) 
      {
        return;
      }

      if (Const.isEmpty(versionComment) && rep.getSecurityProvider().isVersionCommentMandatory()) {
        if (!RepositorySecurityUI.showVersionCommentMandatoryDialog(shell)) {
          versionOk = true;
        }
      } else {
        versionOk = true;
      }
    }

    String[] filenames = dialog.getFileNames();
    if (filenames.length > 0) {
      RepositoryImportProgressDialog ripd = new RepositoryImportProgressDialog(shell, SWT.NONE, rep, 
          dialog.getFilterPath(), filenames, baseDirectory, versionComment, importRules);
      ripd.open();

      refreshTree();
    }
  }

  public boolean saveXMLFile(boolean export) {
    TransMeta transMeta = getActiveTransformation();
    if (transMeta != null)
      return saveXMLFile(transMeta, export);

    JobMeta jobMeta = getActiveJob();
    if (jobMeta != null)
      return saveXMLFile(jobMeta, export);

    return false;
  }

  public boolean saveXMLFile(EngineMetaInterface meta, boolean export) {
    if (log.isBasic())
      log.logBasic("Save file as..."); //$NON-NLS-1$
    boolean saved = false;
    String beforeFilename = meta.getFilename();
    String beforeName = meta.getName();
    
    FileDialog dialog = new FileDialog(shell, SWT.SAVE);
    String extensions[] = meta.getFilterExtensions();
    dialog.setFilterExtensions(extensions);
    dialog.setFilterNames(meta.getFilterNames());
    setFilterPath(dialog);
    String fname = dialog.open();
    if (fname != null) {
      lastDirOpened = dialog.getFilterPath();

      // Is the filename ending on .ktr, .xml?
      boolean ending = false;
      for (int i = 0; i < extensions.length - 1; i++) {
        String[] parts = extensions[i].split(";");
        for (int j = 0; j < parts.length; j++) {
          if (fname.toLowerCase().endsWith(parts[j].substring(1).toLowerCase())) {
            ending = true;
          }
        }
      }
      if (fname.endsWith(meta.getDefaultExtension()))
        ending = true;
      if (!ending) {
        if (!meta.getDefaultExtension().startsWith(".") && !fname.endsWith("."))
          fname += ".";
        fname += meta.getDefaultExtension();
      }
      // See if the file already exists...
      int id = SWT.YES;
      try {
        FileObject f = KettleVFS.getFileObject(fname);
        if (f.exists()) {
          MessageBox mb = new MessageBox(shell, SWT.NO | SWT.YES | SWT.ICON_WARNING);
          // "This file already exists.  Do you want to overwrite it?"
          mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.PromptOverwriteFile.Message"));
          // "This file already exists!"
          mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.PromptOverwriteFile.Title"));
          id = mb.open();
        }
      } catch (Exception e) {
        // TODO do we want to show an error dialog here? My first guess
        // is not, but we might.
      }
      if (id == SWT.YES) {
        if (!export && !Const.isEmpty(beforeFilename) && !beforeFilename.equals(fname)) {
          meta.setName(Const.createName(fname));
          meta.setFilename(fname);
          // If the user hits cancel here, don't save anything
          //
          if (!editProperties()) {
            // Revert the changes!
            //
            meta.setFilename(beforeFilename);
            meta.setName(beforeName);
            return saved;
          }
        }
        
        saved=save(meta, fname, export);
        if(!saved) {
          meta.setFilename(beforeFilename);
          meta.setName(beforeName);
        }
      }
    }
    return saved;
  }

  public boolean saveXMLFileToVfs() {
    TransMeta transMeta = getActiveTransformation();
    if (transMeta != null)
      return saveXMLFileToVfs((EngineMetaInterface) transMeta);

    JobMeta jobMeta = getActiveJob();
    if (jobMeta != null)
      return saveXMLFileToVfs((EngineMetaInterface) jobMeta);

    return false;
  }

  public boolean saveXMLFileToVfs(EngineMetaInterface meta) {
    if (log.isBasic())
      log.logBasic("Save file as..."); //$NON-NLS-1$
    boolean saved = false;

    FileObject rootFile = null;
    FileObject initialFile = null;
    try {
      initialFile = KettleVFS.getFileObject(getLastFileOpened());
      rootFile = KettleVFS.getFileObject(getLastFileOpened()).getFileSystem().getRoot();
    } catch (Exception e) {
      e.printStackTrace();
      MessageBox messageDialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
      messageDialog.setText("Error");
      messageDialog.setMessage(e.getMessage());
      messageDialog.open();
      return false;
    }

    String fname = null;
    FileObject selectedFile = getVfsFileChooserDialog(rootFile, initialFile).open(shell, "Untitled", Const.STRING_TRANS_AND_JOB_FILTER_EXT, Const
        .getTransformationAndJobFilterNames(), VfsFileChooserDialog.VFS_DIALOG_SAVEAS);
    if (selectedFile != null) {
      fname = selectedFile.getName().getFriendlyURI();
    }

    String extensions[] = meta.getFilterExtensions();
    if (fname != null) {
      // Is the filename ending on .ktr, .xml?
      boolean ending = false;
      for (int i = 0; i < extensions.length - 1; i++) {
        if (fname.endsWith(extensions[i].substring(1)))
          ending = true;
      }
      if (fname.endsWith(meta.getDefaultExtension()))
        ending = true;
      if (!ending) {
        fname += '.' + meta.getDefaultExtension();
      }
      // See if the file already exists...
      int id = SWT.YES;
      try {
        FileObject f = KettleVFS.getFileObject(fname);
        if (f.exists()) {
          MessageBox mb = new MessageBox(shell, SWT.NO | SWT.YES | SWT.ICON_WARNING);
          // "This file already exists.  Do you want to overwrite it?"
          mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.PromptOverwriteFile.Message"));
          mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.PromptOverwriteFile.Title"));
          id = mb.open();
        }
      } catch (Exception e) {
        // TODO do we want to show an error dialog here? My first guess
        // is not, but we might.
      }
      if (id == SWT.YES) {
        save(meta, fname, false);
      }
    }
    return saved;
  }

  public boolean save(EngineMetaInterface meta, String fname, boolean export) {
    boolean saved = false;

    // the only file types that are subject to ascii-only rule are those that are not trans and not job 
    boolean isNotTransOrJob = !LastUsedFile.FILE_TYPE_TRANSFORMATION.equals(meta.getFileType())
        && !LastUsedFile.FILE_TYPE_JOB.equals(meta.getFileType());

    if (isNotTransOrJob) {
      Pattern pattern = Pattern.compile("\\p{ASCII}+");
      Matcher matcher = pattern.matcher(fname);
      if (!matcher.matches()) {
        /*
         * Temporary fix for AGILEBI-405 Don't allow saving of files that contain special characters until AGILEBI-394 is resolved.
         * AGILEBI-394 Naming an analyzer report with spanish accents gives error when publishing.
         * */
        MessageBox box = new MessageBox(staticSpoon.shell, SWT.ICON_ERROR | SWT.OK);
        box.setMessage("Special characters are not allowed in the filename. Please use ASCII characters only");
        box.setText(BaseMessages.getString(PKG, "Spoon.Dialog.ErrorSavingConnection.Title"));
        box.open();
        return false;
      }
    }

    FileListener listener = null;
    // match by extension first
    int idx = fname.lastIndexOf('.');
    if (idx != -1) {
      String extension = fname.substring(idx + 1);
      listener = fileExtensionMap.get(extension);
    }
    if (listener == null) {
      String xt = meta.getDefaultExtension();
      listener = fileExtensionMap.get(xt);
    }

    if (listener != null) {
      String sync = BasePropertyHandler.getProperty(SYNC_TRANS);
      if (Boolean.parseBoolean(sync)) {
        listener.syncMetaName(meta, Const.createName(fname));
        delegates.tabs.renameTabs();
      }
      saved = listener.save(meta, fname, export);
    }

    return saved;
  }

  public boolean saveMeta(EngineMetaInterface meta, String fname) {
    meta.setFilename(fname);
    if (Const.isEmpty(meta.getName()) || delegates.jobs.isDefaultJobName(meta.getName()) || delegates.trans.isDefaultTransformationName(meta.getName())) {
      meta.nameFromFilename();
    }

    boolean saved = false;
    try {
      String xml = XMLHandler.getXMLHeader() + meta.getXML();

      DataOutputStream dos = new DataOutputStream(KettleVFS.getOutputStream(fname, false));
      dos.write(xml.getBytes(Const.XML_ENCODING));
      dos.close();

      saved = true;

      // Handle last opened files...
      props.addLastFile(meta.getFileType(), fname, null, false, null); //$NON-NLS-1$
      saveSettings();
      addMenuLast();

      if (log.isDebug())
        log.logDebug(BaseMessages.getString(PKG, "Spoon.Log.FileWritten") + " [" + fname + "]"); // "File
      // written
      // to
      meta.setFilename(fname);
      meta.clearChanged();
      setShellText();
    } catch (Exception e) {
      if (log.isDebug())
        log.logDebug(BaseMessages.getString(PKG, "Spoon.Log.ErrorOpeningFileForWriting") + e.toString());// "Error opening file for writing! --> "
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Dialog.ErrorSavingFile.Title"), BaseMessages.getString(
          PKG, "Spoon.Dialog.ErrorSavingFile.Message")
          + Const.CR + e.toString(), e);
    }
    return saved;
  }

  public void helpAbout() {
    MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION | SWT.CENTER);

    //  resolve the release text
    String releaseText = "";
    if (Const.RELEASE.equals(Const.ReleaseType.PREVIEW)) {
      releaseText = BaseMessages.getString(PKG, "Spoon.PreviewRelease.HelpAboutText");
    } else if (Const.RELEASE.equals(Const.ReleaseType.RELEASE_CANDIDATE)) {
        releaseText = BaseMessages.getString(PKG, "Spoon.Candidate.HelpAboutText");
    } else if (Const.RELEASE.equals(Const.ReleaseType.MILESTONE)) {
      releaseText = BaseMessages.getString(PKG, "Spoon.Milestone.HelpAboutText");
    } else if (Const.RELEASE.equals(Const.ReleaseType.GA)) {
      releaseText = BaseMessages.getString(PKG, "Spoon.GA.HelpAboutText");
    }

    //  build a message
    StringBuilder messageBuilder = new StringBuilder();

    messageBuilder.append(BaseMessages.getString(PKG, "System.ProductInfo"));
    messageBuilder.append(releaseText);
    messageBuilder.append(" - ");
    messageBuilder.append(Const.VERSION);
    messageBuilder.append(Const.CR);
    messageBuilder.append(Const.CR);
    messageBuilder.append(Const.CR);
    messageBuilder.append(BaseMessages.getString(PKG, "System.CompanyInfo"));
    messageBuilder.append(Const.CR);
    messageBuilder.append("         ");
    messageBuilder.append(BaseMessages.getString(PKG, "System.ProductWebsiteUrl"));
    messageBuilder.append(Const.CR);
    messageBuilder.append(Const.CR);
    messageBuilder.append(Const.CR);
    messageBuilder.append(Const.CR);
    messageBuilder.append("Build version : ");
    messageBuilder.append(BuildVersion.getInstance().getVersion());
    messageBuilder.append(Const.CR);
    messageBuilder.append("Build date    : ");
    messageBuilder.append(BuildVersion.getInstance().getBuildDate()); //  this should be the longest line of text
    messageBuilder.append("     "); //  so this is the right margin 
    messageBuilder.append(Const.CR);

    //  set the text in the message box
    mb.setMessage(messageBuilder.toString());
    mb.setText(APP_NAME);

    //  now open the message bx
    mb.open();
  }

  /**
   * Show a dialog containing information on the different step plugins.
   */
  public void helpShowStepPlugins() {
    RowBuffer rowBuffer = PluginRegistry.getInstance().getPluginInformation(StepPluginType.class);
    PreviewRowsDialog dialog = new PreviewRowsDialog(shell, null, SWT.NONE, null, rowBuffer.getRowMeta(), rowBuffer
        .getBuffer());
    dialog.setTitleMessage(BaseMessages.getString(PKG, "Spoon.Dialog.StepPluginList.Title"), BaseMessages.getString(
        PKG, "Spoon.Dialog.StepPluginList.Message"));
    dialog.open();
  }

  /**
   * Show a dialog containing information on the different job entry plugins.
   */
  public void helpShowJobEntryPlugins() {
    RowBuffer rowBuffer = PluginRegistry.getInstance().getPluginInformation(JobEntryPluginType.class);
    PreviewRowsDialog dialog = new PreviewRowsDialog(shell, null, SWT.NONE, null, rowBuffer.getRowMeta(), rowBuffer
        .getBuffer());
    dialog.setTitleMessage(BaseMessages.getString(PKG, "Spoon.Dialog.JobEntryPluginList.Title"), BaseMessages
        .getString(PKG, "Spoon.Dialog.JobEntryPluginList.Message"));
    dialog.open();
  }

  public void editUnselectAll() {
    TransMeta transMeta = getActiveTransformation();
    if (transMeta != null) {
      transMeta.unselectAll();
      getActiveTransGraph().redraw();
    }

    JobMeta jobMeta = getActiveJob();
    if (jobMeta != null) {
      jobMeta.unselectAll();
      getActiveJobGraph().redraw();
    }
  }

  public void editSelectAll() {
    TransMeta transMeta = getActiveTransformation();
    if (transMeta != null) {
      transMeta.selectAll();
      getActiveTransGraph().redraw();
    }

    JobMeta jobMeta = getActiveJob();
    if (jobMeta != null) {
      jobMeta.selectAll();
      getActiveJobGraph().redraw();
    }
  }

  public void editOptions() {
    EnterOptionsDialog eod = new EnterOptionsDialog(shell);
    if (eod.open() != null) {
      props.saveProps();
      loadSettings();
      changeLooks();

      MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION);
      mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.PleaseRestartApplication.Message"));
      mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.PleaseRestartApplication.Title"));
      mb.open();
    }
  }

  public void editKettlePropertiesFile() {
    KettlePropertiesFileDialog dialog = new KettlePropertiesFileDialog(shell, SWT.NONE);
    Map<String,String> newProperties = dialog.open();
    if (newProperties!=null) {
      for (String name : newProperties.keySet()) {
        String value = newProperties.get(name);
        applyVariableToAllLoadedObjects(name, value);
        
        // Also set as a JVM property
        //
        System.setProperty(name, value);
      }
    }
  }

  /**
   * Matches if the filter is non-empty
   * 
   * @param string
   * @return
   */
  private boolean filterMatch(String string) {
    String filter = selectionFilter.getText();
    if (Const.isEmpty(string))
      return true;
    if (Const.isEmpty(filter))
      return true;

    try {
      if (string.matches(filter))
        return true;
    } catch (Exception e) {
      log.logError("Not a valid pattern [" + filter + "] : " + e.getMessage());
    }

    if (string.toUpperCase().contains(filter.toUpperCase()))
      return true;

    return false;
  }

  /**
   * Refresh the object selection tree (on the left of the screen)
   */
  public void refreshTree() {
    if (shell.isDisposed())
      return;

    if (!viewSelected)
      return; // Nothing to see here, move along...

    if (selectionTree == null || selectionTree.isDisposed()) {
      // //////////////////////////////////////////////////////////////////////////////////////////////////
      //
      // Now set up the transformation/job tree
      //
      selectionTree = new Tree(variableComposite, SWT.SINGLE);
      props.setLook(selectionTree);
      selectionTree.setLayout(new FillLayout());
      addDefaultKeyListeners(selectionTree);

      /*
       * ExpandItem treeItem = new ExpandItem(mainExpandBar, SWT.NONE);
       * treeItem.setControl(selectionTree);
       * treeItem.setHeight(shell.getBounds().height); setHeaderImage(treeItem,
       * GUIResource.getInstance().getImageLogoSmall(), STRING_SPOON_MAIN_TREE,
       * 0, true);
       */

      // Add a tree memory as well...
      TreeMemory.addTreeListener(selectionTree, STRING_SPOON_MAIN_TREE);

      selectionTree.addMenuDetectListener(new MenuDetectListener() {
        public void menuDetected(MenuDetectEvent e) {
          setMenu(selectionTree);
        }
      });

      selectionTree.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          showSelection();
        }
      });
      selectionTree.addSelectionListener(new SelectionAdapter() {
        public void widgetDefaultSelected(SelectionEvent e) {
          doubleClickedInTree(selectionTree);
        }
      });

      // Set a listener on the tree
      addDragSourceToTree(selectionTree);
    }

    GUIResource guiResource = GUIResource.getInstance();
    TransMeta activeTransMeta = getActiveTransformation();
    JobMeta activeJobMeta = getActiveJob();
    boolean showAll = activeTransMeta == null && activeJobMeta == null;

    // get a list of transformations from the transformation map
    //

    /*
     * List<TransMeta> transformations =
     * delegates.trans.getTransformationList();
     * Collections.sort(transformations); TransMeta[] transMetas =
     * transformations.toArray(new TransMeta[transformations.size()]);
     * 
     * // get a list of jobs from the job map List<JobMeta> jobs =
     * delegates.jobs.getJobList(); Collections.sort(jobs); JobMeta[] jobMetas =
     * jobs.toArray(new JobMeta[jobs.size()]);
     */

    // Refresh the content of the tree for those transformations
    //
    // First remove the old ones.
    selectionTree.removeAll();

    // Now add the data back
    //
    if (!props.isOnlyActiveFileShownInTree() || showAll || activeTransMeta != null) {
      TreeItem tiTrans = new TreeItem(selectionTree, SWT.NONE);
      tiTrans.setText(STRING_TRANSFORMATIONS);
      tiTrans.setImage(GUIResource.getInstance().getImageBol());

      // Set expanded if this is the only transformation shown.
      if (props.isOnlyActiveFileShownInTree()) {
        TreeMemory.getInstance().storeExpanded(STRING_SPOON_MAIN_TREE, tiTrans, true);
      }

      for (TabMapEntry entry : delegates.tabs.getTabs()) {
        Object managedObject = entry.getObject().getManagedObject();
        if (managedObject instanceof TransMeta) {
          TransMeta transMeta = (TransMeta) managedObject;

          if (!props.isOnlyActiveFileShownInTree() || showAll
              || (activeTransMeta != null && activeTransMeta.equals(transMeta))) {

            // Add a tree item with the name of transformation
            //
            String name = delegates.tabs.makeTabName(transMeta, entry.isShowingLocation());
            if (Const.isEmpty(name)) {
              name = STRING_TRANS_NO_NAME;
            }

            TreeItem tiTransName = new TreeItem(tiTrans, SWT.NONE);
            tiTransName.setText(name);
            tiTransName.setImage(guiResource.getImageTransGraph());

            // Set expanded if this is the only transformation
            // shown.
            if (props.isOnlyActiveFileShownInTree()) {
              TreeMemory.getInstance().storeExpanded(STRING_SPOON_MAIN_TREE, tiTransName, true);
            }

            // /////////////////////////////////////////////////////
            //
            // Now add the database connections
            //
            TreeItem tiDbTitle = new TreeItem(tiTransName, SWT.NONE);
            tiDbTitle.setText(STRING_CONNECTIONS);
            tiDbTitle.setImage(guiResource.getImageBol());

            String[] dbNames = new String[transMeta.nrDatabases()];
            for (int i = 0; i < dbNames.length; i++)
              dbNames[i] = transMeta.getDatabase(i).getName();
            Arrays.sort(dbNames, new Comparator<String>() {
              public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
              }
            });

            // Draw the connections themselves below it.
            for (int i = 0; i < dbNames.length; i++) {
              DatabaseMeta databaseMeta = transMeta.findDatabase(dbNames[i]);

              if (!filterMatch(dbNames[i]))
                continue;

              TreeItem tiDb = new TreeItem(tiDbTitle, SWT.NONE);
              tiDb.setText(databaseMeta.getName());
              if (databaseMeta.isShared())
                tiDb.setFont(guiResource.getFontBold());
              tiDb.setImage(guiResource.getImageConnection());
            }

            // /////////////////////////////////////////////////////
            //
            // The steps
            //
            TreeItem tiStepTitle = new TreeItem(tiTransName, SWT.NONE);
            tiStepTitle.setText(STRING_STEPS);
            tiStepTitle.setImage(guiResource.getImageBol());

            // Put the steps below it.
            for (int i = 0; i < transMeta.nrSteps(); i++) {
              StepMeta stepMeta = transMeta.getStep(i);
              PluginInterface stepPlugin = PluginRegistry.getInstance().findPluginWithId(StepPluginType.class,
                  stepMeta.getStepID());

              if (!filterMatch(stepMeta.getName()) && !filterMatch(stepMeta.getName())) {
                continue;
              }

              TreeItem tiStep = new TreeItem(tiStepTitle, SWT.NONE);
              tiStep.setText(stepMeta.getName());
              if (stepMeta.isShared())
                tiStep.setFont(guiResource.getFontBold());
              if (!stepMeta.isDrawn())
                tiStep.setForeground(guiResource.getColorDarkGray());
              Image stepIcon = guiResource.getImagesStepsSmall().get(stepPlugin.getIds()[0]);
              if (stepIcon == null)
                stepIcon = guiResource.getImageBol();
              tiStep.setImage(stepIcon);
            }

            // /////////////////////////////////////////////////////
            //
            // The hops
            //
            TreeItem tiHopTitle = new TreeItem(tiTransName, SWT.NONE);
            tiHopTitle.setText(STRING_HOPS);
            tiHopTitle.setImage(guiResource.getImageBol());

            // Put the steps below it.
            for (int i = 0; i < transMeta.nrTransHops(); i++) {
              TransHopMeta hopMeta = transMeta.getTransHop(i);

              if (!filterMatch(hopMeta.toString()))
                continue;

              TreeItem tiHop = new TreeItem(tiHopTitle, SWT.NONE);
              tiHop.setText(hopMeta.toString());
              if (hopMeta.isEnabled())
                tiHop.setImage(guiResource.getImageHop());
              else
                tiHop.setImage(guiResource.getImageDisabledHop());
            }

            // /////////////////////////////////////////////////////
            //
            // The partitions
            //
            TreeItem tiPartitionTitle = new TreeItem(tiTransName, SWT.NONE);
            tiPartitionTitle.setText(STRING_PARTITIONS);
            tiPartitionTitle.setImage(guiResource.getImageBol());

            // Put the steps below it.
            for (int i = 0; i < transMeta.getPartitionSchemas().size(); i++) {
              PartitionSchema partitionSchema = transMeta.getPartitionSchemas().get(i);
              if (!filterMatch(partitionSchema.getName()))
                continue;
              TreeItem tiPartition = new TreeItem(tiPartitionTitle, SWT.NONE);
              tiPartition.setText(partitionSchema.getName());
              tiPartition.setImage(guiResource.getImageFolderConnections());
              if (partitionSchema.isShared())
                tiPartition.setFont(guiResource.getFontBold());
            }

            // /////////////////////////////////////////////////////
            //
            // The slaves
            //
            TreeItem tiSlaveTitle = new TreeItem(tiTransName, SWT.NONE);
            tiSlaveTitle.setText(STRING_SLAVES);
            tiSlaveTitle.setImage(guiResource.getImageBol());

            // Put the slaves below it.
            //
            String[] slaveNames = transMeta.getSlaveServerNames();
            Arrays.sort(slaveNames, new Comparator<String>() {
              public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
              }
            });

            for (int i = 0; i < slaveNames.length; i++) {
              SlaveServer slaveServer = transMeta.findSlaveServer(slaveNames[i]);
              if (!filterMatch(slaveServer.getName()))
                continue;
              TreeItem tiSlave = new TreeItem(tiSlaveTitle, SWT.NONE);
              tiSlave.setText(slaveServer.getName());
              tiSlave.setImage(guiResource.getImageSlave());
              if (slaveServer.isShared())
                tiSlave.setFont(guiResource.getFontBold());
            }

            // /////////////////////////////////////////////////////
            //
            // The clusters
            //
            TreeItem tiClusterTitle = new TreeItem(tiTransName, SWT.NONE);
            tiClusterTitle.setText(STRING_CLUSTERS);
            tiClusterTitle.setImage(guiResource.getImageBol());

            // Put the steps below it.
            for (int i = 0; i < transMeta.getClusterSchemas().size(); i++) {
              ClusterSchema clusterSchema = transMeta.getClusterSchemas().get(i);
              if (!filterMatch(clusterSchema.getName()))
                continue;
              TreeItem tiCluster = new TreeItem(tiClusterTitle, SWT.NONE);
              tiCluster.setText(clusterSchema.toString());
              tiCluster.setImage(guiResource.getImageCluster());
              if (clusterSchema.isShared())
                tiCluster.setFont(guiResource.getFontBold());
            }
          }
        }
      }
    }

    if (!props.isOnlyActiveFileShownInTree() || showAll || activeJobMeta != null) {
      TreeItem tiJobs = new TreeItem(selectionTree, SWT.NONE);
      tiJobs.setText(STRING_JOBS);
      tiJobs.setImage(GUIResource.getInstance().getImageBol());

      // Set expanded if this is the only job shown.
      if (props.isOnlyActiveFileShownInTree()) {
        tiJobs.setExpanded(true);
        TreeMemory.getInstance().storeExpanded(STRING_SPOON_MAIN_TREE, tiJobs, true);
      }

      // Now add the jobs
      //
      for (TabMapEntry entry : delegates.tabs.getTabs()) {
        Object managedObject = entry.getObject().getManagedObject();
        if (managedObject instanceof JobMeta) {
          JobMeta jobMeta = (JobMeta) managedObject;

          if (!props.isOnlyActiveFileShownInTree() || showAll
              || (activeJobMeta != null && activeJobMeta.equals(jobMeta))) {
            // Add a tree item with the name of job
            //
            String name = delegates.tabs.makeTabName(jobMeta, entry.isShowingLocation());
            if (Const.isEmpty(name)) {
              name = STRING_JOB_NO_NAME;
            }
            if (!filterMatch(name)) {
              continue;
            }

            TreeItem tiJobName = new TreeItem(tiJobs, SWT.NONE);
            tiJobName.setText(name);
            tiJobName.setImage(guiResource.getImageJobGraph());

            // Set expanded if this is the only job shown.
            if (props.isOnlyActiveFileShownInTree()) {
              TreeMemory.getInstance().storeExpanded(STRING_SPOON_MAIN_TREE, tiJobName, true);
            }

            // /////////////////////////////////////////////////////
            //
            // Now add the database connections
            //
            TreeItem tiDbTitle = new TreeItem(tiJobName, SWT.NONE);
            tiDbTitle.setText(STRING_CONNECTIONS);
            tiDbTitle.setImage(guiResource.getImageBol());

            String[] dbNames = new String[jobMeta.nrDatabases()];
            for (int i = 0; i < dbNames.length; i++)
              dbNames[i] = jobMeta.getDatabase(i).getName();
            Arrays.sort(dbNames, new Comparator<String>() {
              public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
              }
            });

            // Draw the connections themselves below it.
            for (int i = 0; i < dbNames.length; i++) {
              DatabaseMeta databaseMeta = jobMeta.findDatabase(dbNames[i]);
              if (!filterMatch(databaseMeta.getName()))
                continue;
              TreeItem tiDb = new TreeItem(tiDbTitle, SWT.NONE);
              tiDb.setText(databaseMeta.getName());
              if (databaseMeta.isShared())
                tiDb.setFont(guiResource.getFontBold());
              tiDb.setImage(guiResource.getImageConnection());
            }

            // /////////////////////////////////////////////////////
            //
            // The job entries
            //
            TreeItem tiJobEntriesTitle = new TreeItem(tiJobName, SWT.NONE);
            tiJobEntriesTitle.setText(STRING_JOB_ENTRIES);
            tiJobEntriesTitle.setImage(guiResource.getImageBol());

            // Put the job entries below it.
            //
            for (int i = 0; i < jobMeta.nrJobEntries(); i++) {
              JobEntryCopy jobEntry = jobMeta.getJobEntry(i);

              if (!filterMatch(jobEntry.getName()) && !filterMatch(jobEntry.getDescription()))
                continue;

              TreeItem tiJobEntry = ConstUI.findTreeItem(tiJobEntriesTitle, jobEntry.getName());
              if (tiJobEntry != null)
                continue; // only show it once

              tiJobEntry = new TreeItem(tiJobEntriesTitle, SWT.NONE);
              tiJobEntry.setText(jobEntry.getName());
              // if (jobEntry.isShared())
              // tiStep.setFont(guiResource.getFontBold()); TODO:
              // allow job entries to be shared as well...
              if (jobEntry.isStart()) {
                tiJobEntry.setImage(GUIResource.getInstance().getImageStart());
              } else if (jobEntry.isDummy()) {
                tiJobEntry.setImage(GUIResource.getInstance().getImageDummy());
              } else {
                String key = jobEntry.getEntry().getPluginId();
                Image image = GUIResource.getInstance().getImagesJobentriesSmall().get(key);
                tiJobEntry.setImage(image);
              }
            }

            // /////////////////////////////////////////////////////
            //
            // The slaves
            //
            TreeItem tiSlaveTitle = new TreeItem(tiJobName, SWT.NONE);
            tiSlaveTitle.setText(STRING_SLAVES);
            tiSlaveTitle.setImage(guiResource.getImageBol());

            // Put the slaves below it.
            //
            String[] slaveNames = jobMeta.getSlaveServerNames();
            Arrays.sort(slaveNames, new Comparator<String>() {
              public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
              }
            });

            for (int i = 0; i < slaveNames.length; i++) {
              SlaveServer slaveServer = jobMeta.findSlaveServer(slaveNames[i]);
              if (!filterMatch(slaveServer.getName()))
                continue;
              TreeItem tiSlave = new TreeItem(tiSlaveTitle, SWT.NONE);
              tiSlave.setText(slaveServer.getName());
              tiSlave.setImage(guiResource.getImageSlave());
              if (slaveServer.isShared())
                tiSlave.setFont(guiResource.getFontBold());
            }
          }
        }
      }
    }

    // Set the expanded state of the complete tree.
    TreeMemory.setExpandedFromMemory(selectionTree, STRING_SPOON_MAIN_TREE);

    // refreshCoreObjectsHistory();

    selectionTree.setFocus();
    selectionTree.layout();
    variableComposite.layout(true, true);
    setShellText();
  }

  public String getActiveTabText() {
    if (tabfolder.getSelected() == null)
      return null;
    return tabfolder.getSelected().getText();
  }

  public void refreshGraph() {
    if (shell.isDisposed())
      return;

    TabItem tabItem = tabfolder.getSelected();
    if (tabItem == null)
      return;

    TabMapEntry tabMapEntry = delegates.tabs.getTab(tabItem);
    if (tabMapEntry!=null) {
      if (tabMapEntry.getObject() instanceof TransGraph) {
        TransGraph transGraph = (TransGraph) tabMapEntry.getObject();
        transGraph.redraw();
      }
      if (tabMapEntry.getObject() instanceof JobGraph) {
        JobGraph jobGraph = (JobGraph) tabMapEntry.getObject();
        jobGraph.redraw();
      }
    }
  
    setShellText();
  }

  public StepMeta newStep(TransMeta transMeta) {
    return newStep(transMeta, true, true);
  }

  public StepMeta newStep(TransMeta transMeta, boolean openit, boolean rename) {
    if (transMeta == null)
      return null;
    TreeItem ti[] = selectionTree.getSelection();
    StepMeta inf = null;

    if (ti.length == 1) {
      String steptype = ti[0].getText();
      if (log.isDebug())
        log.logDebug(BaseMessages.getString(PKG, "Spoon.Log.NewStep") + steptype);// "New step: "

      inf = newStep(transMeta, steptype, steptype, openit, rename);
    }

    return inf;
  }

  /**
   * Allocate new step, optionally open and rename it.
   * 
   * @param name
   *            Name of the new step
   * @param description
   *            Description of the type of step
   * @param openit
   *            Open the dialog for this step?
   * @param rename
   *            Rename this step?
   * 
   * @return The newly created StepMeta object.
   * 
   */
  public StepMeta newStep(TransMeta transMeta, String name, String description, boolean openit, boolean rename) {
    StepMeta inf = null;

    // See if we need to rename the step to avoid doubles!
    if (rename && transMeta.findStep(name) != null) {
      int i = 2;
      String newname = name + " " + i;
      while (transMeta.findStep(newname) != null) {
        i++;
        newname = name + " " + i;
      }
      name = newname;
    }

    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface stepPlugin = null;

    try {
      stepPlugin = registry.findPluginWithName(StepPluginType.class, description);
      if (stepPlugin != null) {
        StepMetaInterface info = (StepMetaInterface) registry.loadClass(stepPlugin);

        info.setDefault();

        if (openit) {
          StepDialogInterface dialog = this.getStepEntryDialog(info, transMeta, name);
          if (dialog != null) {
            name = dialog.open();
          }
        }
        inf = new StepMeta(stepPlugin.getIds()[0], name, info);

        if (name != null) // OK pressed in the dialog: we have a
        // step-name
        {
          String newname = name;
          StepMeta stepMeta = transMeta.findStep(newname);
          int nr = 2;
          while (stepMeta != null) {
            newname = name + " " + nr;
            stepMeta = transMeta.findStep(newname);
            nr++;
          }
          if (nr > 2) {
            inf.setName(newname);
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
            // "This stepname already exists.  Spoon changed the stepname to ["+newname+"]"
            mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.ChangeStepname.Message", newname));
            mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.ChangeStepname.Title"));
            mb.open();
          }
          inf.setLocation(20, 20); // default location at (20,20)
          transMeta.addStep(inf);

          // Save for later:
          // if openit is false: we drag&drop it onto the canvas!
          if (openit) {
            addUndoNew(transMeta, new StepMeta[] { inf }, new int[] { transMeta.indexOfStep(inf) });
          }

          // Also store it in the pluginHistory list...
          props.increasePluginHistory(stepPlugin.getIds()[0]);
          // stepHistoryChanged = true;

          refreshTree();
        } else {
          return null; // Cancel pressed in dialog.
        }
        setShellText();
      }
    } catch (KettleException e) {
      String filename = stepPlugin.getErrorHelpFile();
      if (stepPlugin != null && !Const.isEmpty(filename)) {
        // OK, in stead of a normal error message, we give back the
        // content of the error help file... (HTML)
        try {
          StringBuffer content = new StringBuffer();

          FileInputStream fis = new FileInputStream(new File(filename));
          int ch = fis.read();
          while (ch >= 0) {
            content.append((char) ch);
            ch = fis.read();
          }

          ShowBrowserDialog sbd = new ShowBrowserDialog(shell, BaseMessages.getString(PKG,
              "Spoon.Dialog.ErrorHelpText.Title"), content.toString());// "Error help text"
          sbd.open();
        } catch (Exception ex) {
          new ErrorDialog(shell,
          // "Error showing help text"
              BaseMessages.getString(PKG, "Spoon.Dialog.ErrorShowingHelpText.Title"), BaseMessages.getString(PKG,
                  "Spoon.Dialog.ErrorShowingHelpText.Message"), ex);
        }
      } else {
        new ErrorDialog(shell,
        // "Error creating step"
            // "I was unable to create a new step"
            BaseMessages.getString(PKG, "Spoon.Dialog.UnableCreateNewStep.Title"), BaseMessages.getString(PKG,
                "Spoon.Dialog.UnableCreateNewStep.Message"), e);
      }
      return null;
    } catch (Throwable e) {
      if (!shell.isDisposed())
        new ErrorDialog(shell,
        // "Error creating step"
            BaseMessages.getString(PKG, "Spoon.Dialog.ErrorCreatingStep.Title"), BaseMessages.getString(PKG,
                "Spoon.Dialog.UnableCreateNewStep.Message"), e);
      return null;
    }

    return inf;
  }

  public void setShellText() {
    if (shell.isDisposed())
      return;

    String fname = null;
    String name = null;
    String version = null;
    ChangedFlagInterface changed = null;

    TransMeta transMeta = getActiveTransformation();
    if (transMeta != null) {
      changed = transMeta;
      fname = transMeta.getFilename();
      name = transMeta.getName();
      version = transMeta.getObjectRevision() == null ? null : transMeta.getObjectRevision().getName();
    }
    JobMeta jobMeta = getActiveJob();
    if (jobMeta != null) {
      changed = jobMeta;
      fname = jobMeta.getFilename();
      name = jobMeta.getName();
      version = jobMeta.getObjectRevision() == null ? null : jobMeta.getObjectRevision().getName();
    }

    String text = "";

    if (rep != null) {
      text += APPL_TITLE + " - [" + getRepositoryName() + "] ";
    } else {
      text += APPL_TITLE + " - ";
    }

    if (Const.isEmpty(name)) {
      if (!Const.isEmpty(fname)) {
        text += fname;
      } else {
        String tab = getActiveTabText();
        if (!Const.isEmpty(tab)) {
          text += tab;
        } else {
          text += BaseMessages.getString(PKG, "Spoon.Various.NoName");// "[no name]"
        }
      }
    } else {
      text += name;
    }

    if (!Const.isEmpty(version)) {
      text += " v" + version;
    }

    if (changed != null && changed.hasChanged()) {
      text += " " + BaseMessages.getString(PKG, "Spoon.Various.Changed");
    }

    shell.setText(text);

    markTabsChanged();
  }

  public void enableMenus() {
    boolean disableTransMenu = getActiveTransformation() == null;
    boolean disableJobMenu = getActiveJob() == null;
    boolean disableMetaMenu = getActiveMeta() == null;
    boolean isRepositoryRunning = rep != null;
    boolean disablePreviewButton = true;
    String activePerspectiveId = null; 
    SpoonPerspectiveManager manager = SpoonPerspectiveManager.getInstance();
    if(manager != null && manager.getActivePerspective() != null) {
      activePerspectiveId = manager.getActivePerspective().getId();
    }
    boolean etlPerspective = false;
    if(activePerspectiveId != null && activePerspectiveId.length() > 0) {
      etlPerspective = activePerspectiveId.equals(MainSpoonPerspective.ID);
    }

    TransGraph transGraph = getActiveTransGraph();
    if (transGraph != null) {
      disablePreviewButton = !(transGraph.isRunning() && !transGraph.isHalting());
    }
    boolean disableSave = true;
    TabItemInterface currentTab = getActiveTabitem();
    if (currentTab != null && currentTab.canHandleSave()) {
      disableSave = !currentTab.hasContentChanged();
    }
    EngineMetaInterface meta = getActiveMeta();
    if (meta != null) {
      disableSave = !meta.canSave();
    }


    org.pentaho.ui.xul.dom.Document doc = null;
    if(mainSpoonContainer != null) {
      doc = mainSpoonContainer.getDocumentRoot();
      if(doc != null) {
        XulMenu menu = (XulMenu) doc.getElementById("action");
        menu.setVisible(etlPerspective);
        // Only enable certain menu-items if we need to.
        disableMenuItem(doc, "file-new-database", disableTransMenu && disableJobMenu || !isRepositoryRunning);
        disableMenuItem(doc, "file-save", disableTransMenu && disableJobMenu && disableMetaMenu || disableSave);
        disableMenuItem(doc, "toolbar-file-save", disableTransMenu && disableJobMenu && disableMetaMenu || disableSave);
        disableMenuItem(doc, "file-save-as", disableTransMenu && disableJobMenu && disableMetaMenu || disableSave);
        disableMenuItem(doc, "toolbar-file-save-as", disableTransMenu && disableJobMenu && disableMetaMenu || disableSave);
        disableMenuItem(doc, "file-save-as-vfs", disableTransMenu && disableJobMenu && disableMetaMenu);
        disableMenuItem(doc, "file-close", disableTransMenu && disableJobMenu && disableMetaMenu); //    ((XulMenuitem) doc.getElementById("file-print")).setDisabled(disableTransMenu && disableJobMenu);
        disableMenuItem(doc, "file-print", disableTransMenu && disableJobMenu);
        disableMenuItem(doc, "file-export-to-xml", disableTransMenu && disableJobMenu);
        disableMenuItem(doc, "file-export-all-to-xml", disableTransMenu && disableJobMenu);

        // Disable the undo and redo menus if there is no active transformation
        // or active job
        // DO NOT ENABLE them otherwise ... leave that to the undo/redo settings
        //
        disableMenuItem(doc, UNDO_MENUITEM, disableTransMenu && disableJobMenu);
        disableMenuItem(doc, REDO_MENUITEM, disableTransMenu && disableJobMenu);

        disableMenuItem(doc, "edit-clear-selection", disableTransMenu && disableJobMenu);
        disableMenuItem(doc, "edit-select-all", disableTransMenu && disableJobMenu);
        updateSettingsMenu(doc, disableTransMenu, disableJobMenu);
        disableMenuItem(doc, "edit-settings" ,disableTransMenu && disableJobMenu && disableMetaMenu);

        // View Menu
        ((XulMenuitem) doc.getElementById("view-results")).setSelected(isExecutionResultsPaneVisible());
        disableMenuItem(doc, "view-results", transGraph == null && disableJobMenu);
        disableMenuItem(doc, "view-zoom-in", disableTransMenu && disableJobMenu);
        disableMenuItem(doc, "view-zoom-out", disableTransMenu && disableJobMenu);
        disableMenuItem(doc, "view-zoom-100pct", disableTransMenu && disableJobMenu);

        // Transformations
        disableMenuItem(doc, "process-run", disableTransMenu && disablePreviewButton && disableJobMenu);
        disableMenuItem(doc, "trans-replay", disableTransMenu && disablePreviewButton);
        disableMenuItem(doc, "trans-preview", disableTransMenu && disablePreviewButton);
        disableMenuItem(doc, "trans-debug", disableTransMenu && disablePreviewButton);
        disableMenuItem(doc, "trans-verify", disableTransMenu);
        disableMenuItem(doc, "trans-impact", disableTransMenu);
        disableMenuItem(doc, "trans-get-sql", disableTransMenu);
        disableMenuItem(doc, "trans-last-impact", disableTransMenu);

        // Tools
        disableMenuItem(doc, "repository-connect", isRepositoryRunning);
        disableMenuItem(doc, "repository-disconnect", !isRepositoryRunning);
        disableMenuItem(doc, "repository-explore", !isRepositoryRunning);
        disableMenuItem(doc, "repository-clear-shared-object-cache", !isRepositoryRunning);
        disableMenuItem(doc, "toolbar-expore-repository", !isRepositoryRunning);
        disableMenuItem(doc, "repository-export-all", !isRepositoryRunning);
        disableMenuItem(doc, "repository-import-directory", !isRepositoryRunning);
        disableMenuItem(doc, "trans-last-preview", !isRepositoryRunning || disableTransMenu);

        // Wizard
        disableMenuItem(doc, "wizard-connection", disableTransMenu && disableJobMenu);
        disableMenuItem(doc, "wizard-copy-table", disableTransMenu && disableJobMenu);
        disableMenuItem(doc, "wizard-copy-tables", isRepositoryRunning && disableTransMenu && disableJobMenu);

        disableMenuItem(doc, "database-inst-dependancy", !isRepositoryRunning);
        
        SpoonPluginManager.getInstance().notifyLifecycleListeners(SpoonLifeCycleEvent.MENUS_REFRESHED);

        // What steps & plugins to show?
        refreshCoreObjects();

        fireMenuControlers();
      }
    }
  }

  /**
   * @param doc
   * @param disableJobMenu 
   * @param disableTransMenu 
   */
  private void updateSettingsMenu(org.pentaho.ui.xul.dom.Document doc, boolean disableTransMenu, boolean disableJobMenu) {
    XulMenuitem settingsItem = (XulMenuitem) doc.getElementById("edit-settings");
    if (settingsItem != null) {
      if (disableTransMenu && !disableJobMenu) {
        settingsItem.setAcceltext("CTRL-J");
        settingsItem.setAccesskey("ctrl-j");
      } else if (!disableTransMenu && disableJobMenu) {
        settingsItem.setAcceltext("CTRL-T");
        settingsItem.setAccesskey("ctrl-t");        
      } else {
        settingsItem.setAcceltext("");
        settingsItem.setAccesskey("");
      }
    }
  }

  public void addSpoonMenuController(ISpoonMenuController menuController) {
    if (menuControllers != null) {
      menuControllers.add(menuController);
    }
  }

  public boolean removeSpoonMenuController(ISpoonMenuController menuController) {
    if (menuControllers != null) {
      return menuControllers.remove(menuController);
    }
    return false;
  }

  public ISpoonMenuController removeSpoonMenuController(String menuControllerName) {
    ISpoonMenuController result = null;

    if (menuControllers != null) {
      for (ISpoonMenuController menuController : menuControllers) {
        if (menuController.getName().equals(menuControllerName)) {
          result = menuController;
          menuControllers.remove(result);
          break;
        }
      }
    }

    return result;
  }

  private void disableMenuItem(org.pentaho.ui.xul.dom.Document doc, String itemId, boolean disable) {
    XulComponent menuItem = doc.getElementById(itemId);
    if (menuItem != null) {
      menuItem.setDisabled(disable);
    } else {
      log.logError("Non-Fatal error : Menu Item with id = " + itemId
          + " does not exist!  Check 'menubar.xul'");
    }
  }

  private void markTabsChanged() {
    for (TabMapEntry entry : delegates.tabs.getTabs()) {
      if (entry.getTabItem().isDisposed())
        continue;

      boolean changed = entry.getObject().hasContentChanged();
      entry.getTabItem().setChanged(changed);
    }
  }

  public void printFile() {
    TransMeta transMeta = getActiveTransformation();
    if (transMeta != null) {
      printTransFile(transMeta);
    }

    JobMeta jobMeta = getActiveJob();
    if (jobMeta != null) {
      printJobFile(jobMeta);
    }
  }

  private void printTransFile(TransMeta transMeta) {
    TransGraph transGraph = getActiveTransGraph();
    if (transGraph == null)
      return;

    PrintSpool ps = new PrintSpool();
    Printer printer = ps.getPrinter(shell);

    // Create an image of the screen
    Point max = transMeta.getMaximum();

    Image img = transGraph.getTransformationImage(printer, max.x, max.y, 1.0f);

    ps.printImage(shell, img);

    img.dispose();
    ps.dispose();
  }

  private void printJobFile(JobMeta jobMeta) {
    JobGraph jobGraph = getActiveJobGraph();
    if (jobGraph == null)
      return;

    PrintSpool ps = new PrintSpool();
    Printer printer = ps.getPrinter(shell);

    // Create an image of the screen
    Point max = jobMeta.getMaximum();

    Image img = jobGraph.getJobImage(printer, max.x, max.y, 1.0f);

    ps.printImage(shell, img);

    img.dispose();
    ps.dispose();
  }

  public TransGraph getActiveTransGraph() {
    if(tabfolder != null) {
      if (tabfolder.getSelected() == null)
        return null;      
    } else {
      return null;
    }
    if(delegates != null && delegates.tabs != null) {
      TabMapEntry mapEntry = delegates.tabs.getTab(tabfolder.getSelected());
      if (mapEntry != null) {
        if (mapEntry.getObject() instanceof TransGraph) {
          return (TransGraph) mapEntry.getObject();
        }
      }
    }
    return null;
  }

  public JobGraph getActiveJobGraph() {
    if(delegates != null && delegates.tabs != null && tabfolder != null) {
      TabMapEntry mapEntry = delegates.tabs.getTab(tabfolder.getSelected());
      if (mapEntry.getObject() instanceof JobGraph)
        return (JobGraph) mapEntry.getObject();
    }
    return null;
  }

  public EngineMetaInterface getActiveMeta() {
    SpoonPerspectiveManager manager = SpoonPerspectiveManager.getInstance();
    if(manager != null &&  manager.getActivePerspective() != null) {
      return manager.getActivePerspective().getActiveMeta();
    }
    return null;
  }

  public TabItemInterface getActiveTabitem() {

    if (tabfolder == null)
      return null;
    TabItem tabItem = tabfolder.getSelected();
    if (tabItem == null)
      return null;
    if(delegates != null && delegates.tabs != null) {
      TabMapEntry mapEntry = delegates.tabs.getTab(tabItem);
      if (mapEntry!=null) {
        return mapEntry.getObject();
      } else {
        return null;
      }
    }
    return null;
  }

  /**
   * @return The active TransMeta object by looking at the selected TransGraph,
   *         TransLog, TransHist If nothing valueable is selected, we return
   *         null
   */
  public TransMeta getActiveTransformation() {
    EngineMetaInterface meta = getActiveMeta();
    if (meta instanceof TransMeta) {
      return (TransMeta) meta;
    }
    return null;
  }

  /**
   * @return The active JobMeta object by looking at the selected JobGraph,
   *         JobLog, JobHist If nothing valueable is selected, we return null
   */
  public JobMeta getActiveJob() {
    EngineMetaInterface meta = getActiveMeta();
    if (meta instanceof JobMeta) {
      return (JobMeta) meta;
    }
    return null;
  }

  public UndoInterface getActiveUndoInterface() {
    return (UndoInterface) this.getActiveMeta();
  }

  public TransMeta findTransformation(String tabItemText) {
    if(delegates != null && delegates.trans != null) {
      return delegates.trans.getTransformation(tabItemText);
    } else {
      return null;
    }
  }

  public JobMeta findJob(String tabItemText) {
    if(delegates != null && delegates.jobs != null) {
      return delegates.jobs.getJob(tabItemText);  
    } else {
      return null;
    }
    
  }

  public TransMeta[] getLoadedTransformations() {
    if(delegates != null && delegates.trans != null) {
      List<TransMeta> list = delegates.trans.getTransformationList();
      return list.toArray(new TransMeta[list.size()]);
    } else {
      return null;
    }
  }

  public JobMeta[] getLoadedJobs() {
    if(delegates != null && delegates.jobs != null) {
      List<JobMeta> list = delegates.jobs.getJobList();
      return list.toArray(new JobMeta[list.size()]);
    } else {
      return null;
    }
  }

  public void saveSettings() {
    WindowProperty winprop = new WindowProperty(shell);
    winprop.setName(APPL_TITLE);
    props.setScreen(winprop);

    props.setLogLevel(DefaultLogLevel.getLogLevel().getCode());
    props.setLogFilter(LogWriter.getInstance().getFilter());
    props.setSashWeights(sashform.getWeights());

    // Also save the open files...
    // Go over the list of tabs, then add the info to the list
    // of open tab files in PropsUI
    //
    props.getOpenTabFiles().clear();

    for (TabMapEntry entry : delegates.tabs.getTabs()) {
      String fileType = null;
      String filename = null;
      String directory = null;
      int openType = 0;
      if (entry.getObjectType() == ObjectType.TRANSFORMATION_GRAPH) {
        fileType = LastUsedFile.FILE_TYPE_TRANSFORMATION;
        TransMeta transMeta = (TransMeta) entry.getObject().getManagedObject();
        filename = rep != null ? transMeta.getName() : transMeta.getFilename();
        directory = transMeta.getRepositoryDirectory().toString();
        openType = LastUsedFile.OPENED_ITEM_TYPE_MASK_GRAPH;
      } else if (entry.getObjectType() == ObjectType.JOB_GRAPH) {
        fileType = LastUsedFile.FILE_TYPE_JOB;
        JobMeta jobMeta = (JobMeta) entry.getObject().getManagedObject();
        filename = rep != null ? jobMeta.getName() : jobMeta.getFilename();
        directory = jobMeta.getRepositoryDirectory().toString();
        openType = LastUsedFile.OPENED_ITEM_TYPE_MASK_GRAPH;
      }

      if (fileType != null) {
        props.addOpenTabFile(fileType, filename, directory, rep != null, rep != null ? rep.getName() : null, openType);
      }
    }

    props.saveProps();
  }

  public void loadSettings() {
    LogLevel logLevel = LogLevel.getLogLevelForCode(props.getLogLevel());
    DefaultLogLevel.setLogLevel(logLevel);
    log.setLogLevel(logLevel);
    LogWriter.getInstance().setFilter(props.getLogFilter());
    CentralLogStore.getAppender().setMaxNrLines(props.getMaxNrLinesInLog());

    // transMeta.setMaxUndo(props.getMaxUndo());
    DBCache.getInstance().setActive(props.useDBCache());
  }

  public void changeLooks() {
    if (!selectionTree.isDisposed())
      props.setLook(selectionTree);
    props.setLook(tabfolder.getSwtTabset(), Props.WIDGET_STYLE_TAB);

    refreshTree();
    refreshGraph();
  }

  public void undoAction(UndoInterface undoInterface) {
    if (undoInterface == null)
      return;

    TransAction ta = undoInterface.previousUndo();
    if (ta == null)
      return;

    setUndoMenu(undoInterface); // something changed: change the menu

    if (undoInterface instanceof TransMeta)
      delegates.trans.undoTransformationAction((TransMeta) undoInterface, ta);
    if (undoInterface instanceof JobMeta)
      delegates.jobs.undoJobAction((JobMeta) undoInterface, ta);

    // Put what we undo in focus
    if (undoInterface instanceof TransMeta) {
      TransGraph transGraph = delegates.trans.findTransGraphOfTransformation((TransMeta) undoInterface);
      transGraph.forceFocus();
    }
    if (undoInterface instanceof JobMeta) {
      JobGraph jobGraph = delegates.jobs.findJobGraphOfJob((JobMeta) undoInterface);
      jobGraph.forceFocus();
    }
  }

  public void redoAction(UndoInterface undoInterface) {
    if (undoInterface == null)
      return;

    TransAction ta = undoInterface.nextUndo();
    if (ta == null)
      return;

    setUndoMenu(undoInterface); // something changed: change the menu

    if (undoInterface instanceof TransMeta)
      delegates.trans.redoTransformationAction((TransMeta) undoInterface, ta);
    if (undoInterface instanceof JobMeta)
      delegates.jobs.redoJobAction((JobMeta) undoInterface, ta);

    // Put what we redo in focus
    if (undoInterface instanceof TransMeta) {
      TransGraph transGraph = delegates.trans.findTransGraphOfTransformation((TransMeta) undoInterface);
      transGraph.forceFocus();
    }
    if (undoInterface instanceof JobMeta) {
      JobGraph jobGraph = delegates.jobs.findJobGraphOfJob((JobMeta) undoInterface);
      jobGraph.forceFocus();
    }
  }

  /**
   * Sets the text and enabled settings for the undo and redo menu items
   * 
   * @param undoInterface
   *            the object which holds the undo/redo information
   */
  public void setUndoMenu(UndoInterface undoInterface) {
    if (shell.isDisposed())
      return;

    TransAction prev = undoInterface != null ? undoInterface.viewThisUndo() : null;
    TransAction next = undoInterface != null ? undoInterface.viewNextUndo() : null;

    // Set the menubar text and enabled flags
    XulMenuitem item = (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById(UNDO_MENUITEM);
    item.setLabel(prev == null ? UNDO_UNAVAILABLE : BaseMessages.getString(PKG, "Spoon.Menu.Undo.Available", prev
        .toString()));
    item.setDisabled(prev == null);
    item = (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById(REDO_MENUITEM);
    item.setLabel(next == null ? REDO_UNAVAILABLE : BaseMessages.getString(PKG, "Spoon.Menu.Redo.Available", next
        .toString()));
    item.setDisabled(next == null);
    //    menuBar.setTextById(UNDO_MENUITEM, prev == null ? UNDO_UNAVAILABLE : Messages.getString("Spoon.Menu.Undo.Available", prev.toString())); //$NON-NLS-1$
    //    menuBar.setTextById(REDO_MENUITEM, next == null ? REDO_UNAVAILABLE : Messages.getString("Spoon.Menu.Redo.Available", next.toString())); //$NON-NLS-1$
    //
    //    // Set the enabled flags
    //    menuBar.setEnableById(UNDO_MENUITEM, prev != null);
    //    menuBar.setEnableById(REDO_MENUITEM, next != null);
  }

  public void addUndoNew(UndoInterface undoInterface, Object obj[], int position[]) {
    addUndoNew(undoInterface, obj, position, false);
  }

  public void addUndoNew(UndoInterface undoInterface, Object obj[], int position[], boolean nextAlso) {
    undoInterface.addUndo(obj, null, position, null, null, TransMeta.TYPE_UNDO_NEW, nextAlso);
    setUndoMenu(undoInterface);
  }

  // Undo delete object
  public void addUndoDelete(UndoInterface undoInterface, Object obj[], int position[]) {
    addUndoDelete(undoInterface, obj, position, false);
  }

  // Undo delete object
  public void addUndoDelete(UndoInterface undoInterface, Object obj[], int position[], boolean nextAlso) {
    undoInterface.addUndo(obj, null, position, null, null, TransMeta.TYPE_UNDO_DELETE, nextAlso);
    setUndoMenu(undoInterface);
  }

  // Change of step, connection, hop or note...
  public void addUndoPosition(UndoInterface undoInterface, Object obj[], int pos[], Point prev[], Point curr[]) {
    // It's better to store the indexes of the objects, not the objects
    // itself!
    undoInterface.addUndo(obj, null, pos, prev, curr, JobMeta.TYPE_UNDO_POSITION, false);
    setUndoMenu(undoInterface);
  }

  // Change of step, connection, hop or note...
  public void addUndoChange(UndoInterface undoInterface, Object from[], Object to[], int[] pos) {
    addUndoChange(undoInterface, from, to, pos, false);
  }

  // Change of step, connection, hop or note...
  public void addUndoChange(UndoInterface undoInterface, Object from[], Object to[], int[] pos, boolean nextAlso) {
    undoInterface.addUndo(from, to, pos, null, null, JobMeta.TYPE_UNDO_CHANGE, nextAlso);
    setUndoMenu(undoInterface);
  }

  /**
   * Checks *all* the steps in the transformation, puts the result in remarks
   * list
   */
  public void checkTrans(TransMeta transMeta) {
    checkTrans(transMeta, false);
  }

  /**
   * Check the steps in a transformation
   * 
   * @param only_selected
   *            True: Check only the selected steps...
   */
  public void checkTrans(TransMeta transMeta, boolean only_selected) {
    if (transMeta == null)
      return;
    TransGraph transGraph = delegates.trans.findTransGraphOfTransformation(transMeta);
    if (transGraph == null)
      return;

    CheckTransProgressDialog ctpd = new CheckTransProgressDialog(shell, transMeta, transGraph.getRemarks(),
        only_selected);
    ctpd.open(); // manages the remarks arraylist...
    showLastTransCheck();
  }

  /**
   * Show the remarks of the last transformation check that was run.
   * 
   * @see #checkTrans()
   */
  public void showLastTransCheck() {
    TransMeta transMeta = getActiveTransformation();
    if (transMeta == null)
      return;
    TransGraph transGraph = delegates.trans.findTransGraphOfTransformation(transMeta);
    if (transGraph == null)
      return;

    CheckResultDialog crd = new CheckResultDialog(transMeta, shell, SWT.NONE, transGraph.getRemarks());
    String stepname = crd.open();
    if (stepname != null) {
      // Go to the indicated step!
      StepMeta stepMeta = transMeta.findStep(stepname);
      if (stepMeta != null) {
        delegates.steps.editStep(transMeta, stepMeta);
      }
    }
  }

  public void analyseImpact(TransMeta transMeta) {
    if (transMeta == null)
      return;
    TransGraph transGraph = delegates.trans.findTransGraphOfTransformation(transMeta);
    if (transGraph == null)
      return;

    AnalyseImpactProgressDialog aipd = new AnalyseImpactProgressDialog(shell, transMeta, transGraph.getImpact());
    transGraph.setImpactFinished(aipd.open());
    if (transGraph.isImpactFinished())
      showLastImpactAnalyses(transMeta);
  }

  public void showLastImpactAnalyses(TransMeta transMeta) {
    if (transMeta == null)
      return;
    TransGraph transGraph = delegates.trans.findTransGraphOfTransformation(transMeta);
    if (transGraph == null)
      return;

    List<Object[]> rows = new ArrayList<Object[]>();
    RowMetaInterface rowMeta = null;
    for (int i = 0; i < transGraph.getImpact().size(); i++) {
      DatabaseImpact ii = (DatabaseImpact) transGraph.getImpact().get(i);
      RowMetaAndData row = ii.getRow();
      rowMeta = row.getRowMeta();
      rows.add(row.getData());
    }

    if (rows.size() > 0) {
      // Display all the rows...
      PreviewRowsDialog prd = new PreviewRowsDialog(shell, Variables.getADefaultVariableSpace(), SWT.NONE, "-",
          rowMeta, rows);
      prd.setTitleMessage(
      // "Impact analyses"
          // "Result of analyses:"
          BaseMessages.getString(PKG, "Spoon.Dialog.ImpactAnalyses.Title"), BaseMessages.getString(PKG,
              "Spoon.Dialog.ImpactAnalyses.Message"));
      prd.open();
    } else {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
      if (transGraph.isImpactFinished()) {
        // "As far as I can tell, this transformation has no impact on any database."
        mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.TransformationNoImpactOnDatabase.Message"));
      } else {
        // "Please run the impact analyses first on this transformation."
        mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.RunImpactAnalysesFirst.Message"));
      }
      mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.ImpactAnalyses.Title"));// Impact
      mb.open();
    }
  }

  public void toClipboard(String cliptext) {
    try {
      GUIResource.getInstance().toClipboard(cliptext);
    } catch (Throwable e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Dialog.ExceptionCopyToClipboard.Title"), BaseMessages
          .getString(PKG, "Spoon.Dialog.ExceptionCopyToClipboard.Message"), e);
    }
  }

  public String fromClipboard() {
    try {
      return GUIResource.getInstance().fromClipboard();
    } catch (Throwable e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Dialog.ExceptionPasteFromClipboard.Title"),
          BaseMessages.getString(PKG, "Spoon.Dialog.ExceptionPasteFromClipboard.Message"), e);
      return null;
    }
  }

  /**
   * Paste transformation from the clipboard...
   * 
   */
  public void pasteTransformation() {
    if (log.isDetailed())
      log.logDetailed(BaseMessages.getString(PKG, "Spoon.Log.PasteTransformationFromClipboard"));// "Paste transformation from the clipboard!"
    String xml = fromClipboard();
    try {
      Document doc = XMLHandler.loadXMLString(xml);
      
      TransMeta transMeta = new TransMeta(XMLHandler.getSubNode(doc, TransMeta.XML_TAG), rep);
      setTransMetaVariables(transMeta);
      addTransGraph(transMeta); // create a new tab
      sharedObjectsFileMap.put(transMeta.getSharedObjects().getFilename(), transMeta.getSharedObjects());
      refreshGraph();
      refreshTree();
    } catch (KettleException e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Dialog.ErrorPastingTransformation.Title"), BaseMessages
          .getString(PKG, "Spoon.Dialog.ErrorPastingTransformation.Message"), e);
    }
  }

  /**
   * Paste job from the clipboard...
   * 
   */
  public void pasteJob() {
    String xml = fromClipboard();
    try {
      Document doc = XMLHandler.loadXMLString(xml);
      JobMeta jobMeta = new JobMeta(XMLHandler.getSubNode(doc, JobMeta.XML_TAG), rep, this);
      addJobGraph(jobMeta); // create a new tab
      refreshGraph();
      refreshTree();
    } catch (KettleException e) {
      new ErrorDialog(shell,
      // Error pasting transformation
          // "An error occurred pasting a transformation from the clipboard"
          BaseMessages.getString(PKG, "Spoon.Dialog.ErrorPastingJob.Title"), BaseMessages.getString(PKG,
              "Spoon.Dialog.ErrorPastingJob.Message"), e);
    }
  }

  public void copyTransformation(TransMeta transMeta) {
    if (transMeta == null)
      return;
    try {
      toClipboard(XMLHandler.getXMLHeader() + transMeta.getXML());
    } catch (Exception ex) {
      new ErrorDialog(getShell(), "Error", "Error encoding to XML", ex);
    }
  }

  public void copyJob(JobMeta jobMeta) {
    if (jobMeta == null)
      return;
    toClipboard(XMLHandler.getXMLHeader() + jobMeta.getXML());
  }

  public void copyTransformationImage(TransMeta transMeta) {
    TransGraph transGraph = delegates.trans.findTransGraphOfTransformation(transMeta);
    if (transGraph == null)
      return;

    Clipboard clipboard = GUIResource.getInstance().getNewClipboard();

    Point area = transMeta.getMaximum();
    Image image = transGraph.getTransformationImage(Display.getCurrent(), area.x, area.y, 1.0f);
    clipboard.setContents(new Object[] { image.getImageData() }, new Transfer[] { ImageDataTransfer.getInstance() });
  }

  /**
   * @return Either a TransMeta or JobMeta object
   */
  public HasDatabasesInterface getActiveHasDatabasesInterface() {
    TransMeta transMeta = getActiveTransformation();
    if (transMeta != null)
      return transMeta;
    return getActiveJob();
  }

  /**
   * Shows a wizard that creates a new database connection...
   * 
   */
  public void createDatabaseWizard() {
    HasDatabasesInterface hasDatabasesInterface = getActiveHasDatabasesInterface();
    if (hasDatabasesInterface == null)
      return; // nowhere to put the new database

    CreateDatabaseWizard cdw = new CreateDatabaseWizard();
    DatabaseMeta newDBInfo = cdw.createAndRunDatabaseWizard(shell, props, hasDatabasesInterface.getDatabases());
    if (newDBInfo != null) { // finished
      hasDatabasesInterface.addDatabase(newDBInfo);
      refreshTree();
      refreshGraph();
    }
  }

  public List<DatabaseMeta> getActiveDatabases() {
    Map<String, DatabaseMeta> map = new Hashtable<String, DatabaseMeta>();

    HasDatabasesInterface hasDatabasesInterface = getActiveHasDatabasesInterface();
    if (hasDatabasesInterface != null) {
      for (int i = 0; i < hasDatabasesInterface.nrDatabases(); i++) {
        map.put(hasDatabasesInterface.getDatabase(i).getName(), hasDatabasesInterface.getDatabase(i));
      }
    }
    if (rep != null) {
      try {
        List<DatabaseMeta> repDBs = rep.readDatabases();
        for (int i = 0; i < repDBs.size(); i++) {
          DatabaseMeta databaseMeta = (DatabaseMeta) repDBs.get(i);
          map.put(databaseMeta.getName(), databaseMeta);
        }
      } catch (Exception e) {
        log.logError("Unexpected error reading databases from the repository: " + e.toString());
        log.logError(Const.getStackTracker(e));
      }
    }

    List<DatabaseMeta> databases = new ArrayList<DatabaseMeta>();
    databases.addAll(map.values());

    return databases;
  }

  /**
   * Create a transformation that extracts tables & data from a database.
   * <p>
   * <p>
   * 
   * 0) Select the database to rip
   * <p>
   * 1) Select the table in the database to copy
   * <p>
   * 2) Select the database to dump to
   * <p>
   * 3) Select the repository directory in which it will end up
   * <p>
   * 4) Select a name for the new transformation
   * <p>
   * 6) Create 1 transformation for the selected table
   * <p>
   */
  public void copyTableWizard() {
    List<DatabaseMeta> databases = getActiveDatabases();
    if (databases.size() == 0)
      return; // Nothing to do here

    final CopyTableWizardPage1 page1 = new CopyTableWizardPage1("1", databases);
    page1.createControl(shell);
    final CopyTableWizardPage2 page2 = new CopyTableWizardPage2("2");
    page2.createControl(shell);

    Wizard wizard = new Wizard() {
      public boolean performFinish() {
        return delegates.db.copyTable(page1.getSourceDatabase(), page1.getTargetDatabase(), page2.getSelection());
      }

      /**
       * @see org.eclipse.jface.wizard.Wizard#canFinish()
       */
      public boolean canFinish() {
        return page2.canFinish();
      }
    };

    wizard.addPage(page1);
    wizard.addPage(page2);

    WizardDialog wd = new WizardDialog(shell, wizard);
    WizardDialog.setDefaultImage(GUIResource.getInstance().getImageWizard());
    wd.setMinimumPageSize(700, 400);
    wd.updateSize();
    wd.open();
  }

  public String toString() {
    return APP_NAME;
  }

  public void selectRep(CommandLineOption[] options) {
    RepositoryMeta repositoryMeta = null;
    // UserInfo userinfo = null;
    
    StringBuffer optionRepname = getCommandLineOption(options, "rep").getArgument();
    StringBuffer optionFilename = getCommandLineOption(options, "file").getArgument();
    StringBuffer optionUsername = getCommandLineOption(options, "user").getArgument();
    StringBuffer optionPassword = getCommandLineOption(options, "pass").getArgument();

    if (Const.isEmpty(optionRepname) && Const.isEmpty(optionFilename) && props.showRepositoriesDialogAtStartup()) {
      if (log.isBasic()) {
        // "Asking for repository"
        log.logBasic(BaseMessages.getString(PKG, "Spoon.Log.AskingForRepository"));
      }

      loginDialog = new RepositoriesDialog(shell, null, new ILoginCallback() {

        public void onSuccess(Repository repository) {
          setRepository(repository);
          SpoonPluginManager.getInstance().notifyLifecycleListeners(SpoonLifeCycleEvent.REPOSITORY_CONNECTED);
        }

        public void onError(Throwable t) {
          if (t instanceof KettleAuthException) {
            ShowMessageDialog dialog = new ShowMessageDialog(loginDialog.getShell(), SWT.OK | SWT.ICON_ERROR, BaseMessages.getString(PKG,
                "Spoon.Dialog.LoginFailed.Title"), t.getLocalizedMessage());
            dialog.open();
          } else {
             new ErrorDialog(loginDialog.getShell(), BaseMessages.getString(PKG, "Spoon.Dialog.LoginFailed.Title"), BaseMessages.getString(PKG, "Spoon.Dialog.LoginFailed.Message", t), t);
          }
        }

        public void onCancel() {
          // do nothing
        }
      });
      hideSplash();
      loginDialog.show();
      showSplash();
    } 
    else if (!Const.isEmpty(optionRepname) && Const.isEmpty(optionFilename)) {
      RepositoriesMeta repsinfo = new RepositoriesMeta();
      try {
        repsinfo.readData();
        repositoryMeta = repsinfo.findRepository(optionRepname.toString());
        if (repositoryMeta != null && !Const.isEmpty(optionUsername) && !Const.isEmpty(optionPassword)) {
          // Define and connect to the repository...
          Repository repo = PluginRegistry.getInstance().loadClass(RepositoryPluginType.class, repositoryMeta,
              Repository.class);
          repo.init(repositoryMeta);
          repo.connect(optionUsername != null ? optionUsername.toString() : null,
              optionPassword != null ? optionPassword.toString() : null);
          setRepository(repo);
        } else {
          if (!Const.isEmpty(optionUsername) && !Const.isEmpty(optionPassword)) {
          String msg = BaseMessages.getString(PKG, "Spoon.Log.NoRepositoriesDefined");
          log.logError(msg);// "No repositories defined on this system."
          MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
          mb.setMessage(BaseMessages.getString(PKG, "Spoon.Error.Repository.NotFound", optionRepname.toString()));
          mb.setText(BaseMessages.getString(PKG, "Spoon.Error.Repository.NotFound.Title"));
          mb.open();
          }
          
          loginDialog = new RepositoriesDialog(shell, null, new ILoginCallback() {

            public void onSuccess(Repository repository) {
              setRepository(repository);
              SpoonPluginManager.getInstance().notifyLifecycleListeners(SpoonLifeCycleEvent.REPOSITORY_CONNECTED);
            }

            public void onError(Throwable t) {
              MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
              mb.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.LoginFailed.Message", t.getLocalizedMessage()));
              mb.setText(BaseMessages.getString(PKG, "Spoon.Dialog.LoginFailed.Title"));
              mb.open();
            }

            public void onCancel() {
              // TODO Auto-generated method stub

            }
          });
          hideSplash();
          loginDialog.show();
          showSplash();
        }
      } catch (Exception e) {
        hideSplash();
        // Eat the exception but log it...
        log.logError("Error reading repositories xml file", e);
      }
    }
  }

  public void handleStartOptions(CommandLineOption[] options) {

    // note that at this point the rep object is populated by previous calls 

    StringBuffer optionRepname = getCommandLineOption(options, "rep").getArgument();
    StringBuffer optionFilename = getCommandLineOption(options, "file").getArgument();
    StringBuffer optionDirname = getCommandLineOption(options, "dir").getArgument();
    StringBuffer optionTransname = getCommandLineOption(options, "trans").getArgument();
    StringBuffer optionJobname = getCommandLineOption(options, "job").getArgument();
    // StringBuffer optionUsername = getCommandLineOption(options,
    // "user").getArgument();
    // StringBuffer optionPassword = getCommandLineOption(options,
    // "pass").getArgument();

    try {
      // Read kettle transformation specified on command-line?
      if (!Const.isEmpty(optionRepname) || !Const.isEmpty(optionFilename)) {
        if (!Const.isEmpty(optionRepname)) {
          if (rep != null) {

            if (Const.isEmpty(optionDirname)) {
              optionDirname = new StringBuffer(RepositoryDirectory.DIRECTORY_SEPARATOR);
            }

            // Options /file, /job and /trans are mutually
            // exclusive
            int t = (Const.isEmpty(optionFilename) ? 0 : 1) + (Const.isEmpty(optionJobname) ? 0 : 1)
                + (Const.isEmpty(optionTransname) ? 0 : 1);
            if (t > 1) {
              // "More then one mutually exclusive options /file, /job and /trans are specified."
              log.logError(BaseMessages.getString(PKG, "Spoon.Log.MutuallyExcusive"));
            } else if (t == 1) {
              if (!Const.isEmpty(optionFilename)) {
                openFile(optionFilename.toString(), false);
              } else {
                // OK, if we have a specified job or
                // transformation, try to load it...
                // If not, keep the repository logged
                // in.
                RepositoryDirectoryInterface repdir = rep.findDirectory(optionDirname.toString());
                if (repdir == null) {
                  log.logError(BaseMessages.getString(PKG, "Spoon.Log.UnableFindDirectory", optionDirname.toString())); // "Can't find directory ["+dirname+"] in the repository."
                } else {
                  if (!Const.isEmpty(optionTransname)) {
                    TransMeta transMeta = rep.loadTransformation(optionTransname.toString(), repdir, null, true, null); // reads
                    // last
                    // version
                    transMeta.clearChanged();
                    transMeta.setInternalKettleVariables();
                    addTransGraph(transMeta);
                  } else {
                    // Try to load a specified job
                    // if any
                    JobMeta jobMeta = rep.loadJob(optionJobname.toString(), repdir, null, null); // reads
                    // last
                    // version
                    jobMeta.clearChanged();
                    jobMeta.setInternalKettleVariables();
                    addJobGraph(jobMeta);
                  }
                }
              }
            }
          } else {
            // "No repositories defined on this system."
            log.logError(BaseMessages.getString(PKG, "Spoon.Log.NoRepositoriesDefined"));
          }
        } else if (!Const.isEmpty(optionFilename)) {
          openFile(optionFilename.toString(), false);
        }
      }
    } catch (KettleException ke) {
      hideSplash();
      log.logError(BaseMessages.getString(PKG, "Spoon.Log.ErrorOccurred") + Const.CR + ke.getMessage());// "An error occurred: "
      log.logError(Const.getStackTracker(ke));
      // do not just eat the exception
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Log.ErrorOccurred"), BaseMessages.getString(PKG,
          "Spoon.Log.ErrorOccurred")
          + Const.CR + ke.getMessage(), ke);
      rep = null;
    }
  }
  
  private void loadLastUsedFiles() {
    if (props.openLastFile()) {
      if (log.isDetailed())
        // "Trying to open the last file used."
        log.logDetailed(BaseMessages.getString(PKG, "Spoon.Log.TryingOpenLastUsedFile"));

      List<LastUsedFile> lastUsedFiles = props.getOpenTabFiles();
      for (LastUsedFile lastUsedFile : lastUsedFiles) {
        try {
          if (!lastUsedFile.isSourceRepository() || 
              lastUsedFile.isSourceRepository() && rep!=null && rep.getName().equals(lastUsedFile.getRepositoryName())) {
            loadLastUsedFile(lastUsedFile, rep == null ? null : rep.getName(), false);
          }
        } catch(Exception e) {
          hideSplash();
          new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.LoadLastUsedFile.Exception.Title"), 
              BaseMessages.getString(PKG, "Spoon.LoadLastUsedFile.Exception.Message", lastUsedFile.toString()), e);
        }
      }
    }
  }

  public void start(CommandLineOption[] options) throws KettleException {

    // Show the repository connection dialog
    //
    selectRep(options);
     
    // Read the start option parameters
    //
    handleStartOptions(options);
    
    // Open the spoon application
    //
    open();
    
    // Load the last loaded files
    //
    loadLastUsedFiles();
    
    // Enable menus based on whether user was able to login or not
    //
    enableMenus();

    if (props.showTips()) {
      TipsDialog tip = new TipsDialog(shell);
      
      hideSplash();
      tip.open();
    }
    if (splash != null) {
      splash.dispose();
    }

    //  If we are a MILESTONE or RELEASE_CANDIDATE    
    if (Const.RELEASE.equals(Const.ReleaseType.MILESTONE)) { // || Const.RELEASE.equals(Const.ReleaseType.RELEASE_CANDIDATE)) {

      //  display the same warning message
      MessageBox dialog = new MessageBox(shell, SWT.ICON_WARNING);
      dialog.setText(BaseMessages.getString(PKG, "Spoon.Warning.DevelopmentRelease.Title"));
      dialog.setMessage(BaseMessages
          .getString(PKG, "Spoon.Warning.DevelopmentRelease.Message", Const.CR, Const.VERSION));
      dialog.open();
    }

    boolean retryAfterError = false; // Enable the user to retry and
    // continue after fatal error
    do {
      retryAfterError = false; // reset to false after error otherwise
      // it will loop forever after
      // closing Spoon
      try {

        while (!isDisposed()) {
          if (!readAndDispatch())
            sleep();
        }
      } catch (Throwable e) {
        log.logError(BaseMessages.getString(PKG, "Spoon.Log.UnexpectedErrorOccurred") + Const.CR + e.getMessage());// "An unexpected error occurred in Spoon: probable cause: please close all windows before stopping Spoon! "
        log.logError(Const.getStackTracker(e));
        try {
          new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Log.UnexpectedErrorOccurred"), BaseMessages
              .getString(PKG, "Spoon.Log.UnexpectedErrorOccurred")
              + Const.CR + e.getMessage(), e);
          // Retry dialog
          MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION | SWT.NO | SWT.YES);
          mb.setText(BaseMessages.getString(PKG, "Spoon.Log.UnexpectedErrorRetry.Titel"));
          mb.setMessage(BaseMessages.getString(PKG, "Spoon.Log.UnexpectedErrorRetry.Message"));
          if (mb.open() == SWT.YES)
            retryAfterError = true;
        } catch (Throwable e1) {
          // When the opening of a dialog crashed, we can not do
          // anyting more here
        }
      }
    } while (retryAfterError);
    dispose();
    if (log.isBasic())
      log.logBasic(APP_NAME + " " + BaseMessages.getString(PKG, "Spoon.Log.AppHasEnded"));// " has ended."

    // Close the logfile
    LogWriter.getInstance().close();
  }

  // public CommandLineOption options[];

  public static CommandLineOption getCommandLineOption(CommandLineOption[] options, String opt) {
    for (int i = 0; i < options.length; i++) {
      if (options[i].getOption().equals(opt)) {
        return options[i];
      }
    }
    return null;
  }

  public static CommandLineOption[] getCommandLineArgs(List<String> args) {

    CommandLineOption[] clOptions = new CommandLineOption[] {
        new CommandLineOption("rep", "Repository name", new StringBuffer()),
        new CommandLineOption("user", "Repository username", new StringBuffer()),
        new CommandLineOption("pass", "Repository password", new StringBuffer()),
        new CommandLineOption("job", "The name of the job to launch", new StringBuffer()),
        new CommandLineOption("trans", "The name of the transformation to launch", new StringBuffer()),
        new CommandLineOption("dir", "The directory (don't forget the leading /)", new StringBuffer()),
        new CommandLineOption("file", "The filename (Transformation in XML) to launch", new StringBuffer()),
        new CommandLineOption("level", "The logging level (Basic, Detailed, Debug, Rowlevel, Error, Nothing)",
            new StringBuffer()), new CommandLineOption("logfile", "The logging file to write to", new StringBuffer()),
        new CommandLineOption("log", "The logging file to write to (deprecated)", new StringBuffer(), false, true), };

    // start with the default logger until we find out otherwise
    //
    log = new LogChannel(APP_NAME);

    // Parse the options...
    if (!CommandLineOption.parseArguments(args, clOptions, log)) {
      log.logError("Command line option not understood");
      System.exit(8);
    }

    String kettleRepname = Const.getEnvironmentVariable("KETTLE_REPOSITORY", null);
    String kettleUsername = Const.getEnvironmentVariable("KETTLE_USER", null);
    String kettlePassword = Const.getEnvironmentVariable("KETTLE_PASSWORD", null);

    if (!Const.isEmpty(kettleRepname))
      clOptions[0].setArgument(new StringBuffer(kettleRepname));
    if (!Const.isEmpty(kettleUsername))
      clOptions[1].setArgument(new StringBuffer(kettleUsername));
    if (!Const.isEmpty(kettlePassword))
      clOptions[2].setArgument(new StringBuffer(kettlePassword));

    return clOptions;
  }

  private void loadLastUsedFile(LastUsedFile lastUsedFile, String repositoryName) throws KettleException {
    loadLastUsedFile(lastUsedFile, repositoryName, true);
  }

  private void loadLastUsedFile(LastUsedFile lastUsedFile, String repositoryName, boolean trackIt)
      throws KettleException {
    boolean useRepository = repositoryName != null;
    // Perhaps we need to connect to the repository?
    // 
    if (lastUsedFile.isSourceRepository()) {
      if (!Const.isEmpty(lastUsedFile.getRepositoryName())) {
        if (useRepository && !lastUsedFile.getRepositoryName().equalsIgnoreCase(repositoryName)) {
          // We just asked...
          useRepository = false;
        }
      }
    }

    if (useRepository && lastUsedFile.isSourceRepository()) {
      if (rep != null) // load from this repository...
      {
        if (rep.getName().equalsIgnoreCase(lastUsedFile.getRepositoryName())) {
          RepositoryDirectoryInterface repdir = rep.findDirectory(lastUsedFile.getDirectory());
          if (repdir != null) {
            // Are we loading a transformation or a job?
            if (lastUsedFile.isTransformation()) {
              if (log.isDetailed())
                log.logDetailed(BaseMessages.getString(PKG, "Spoon.Log.AutoLoadingTransformation", lastUsedFile
                    .getFilename(), lastUsedFile.getDirectory()));// "Auto loading transformation ["+lastfiles[0]+"] from repository directory ["+lastdirs[0]+"]"
              TransLoadProgressDialog tlpd = new TransLoadProgressDialog(shell, rep, lastUsedFile.getFilename(),
                  repdir, null);
              TransMeta transMeta = tlpd.open();
              if (transMeta != null) {
                if (trackIt)
                  props.addLastFile(LastUsedFile.FILE_TYPE_TRANSFORMATION, lastUsedFile.getFilename(),
                      repdir.getPath(), true, rep.getName());
                // transMeta.setFilename(lastUsedFile.getFilename());
                transMeta.clearChanged();
                addTransGraph(transMeta);
                refreshTree();
              }
            } else if (lastUsedFile.isJob()) {
              JobLoadProgressDialog progressDialog = new JobLoadProgressDialog(shell, rep, lastUsedFile.getFilename(),
                  repdir, null);
              JobMeta jobMeta = progressDialog.open();
              if (jobMeta != null) {
                if (trackIt)
                  props.addLastFile(LastUsedFile.FILE_TYPE_JOB, lastUsedFile.getFilename(), repdir.getPath(), true, rep
                      .getName());
                jobMeta.clearChanged();
                addJobGraph(jobMeta);
              }
            }
            refreshTree();
          }
        }
      }
    }

    if (!lastUsedFile.isSourceRepository() && !Const.isEmpty(lastUsedFile.getFilename())) {
      if (lastUsedFile.isTransformation()) {
        openFile(lastUsedFile.getFilename(), false);
        /*
         *TransMeta transMeta = null;
         *try {
         * transMeta = DomainObjectRegistry.getInstance().constructTransMeta(new Class[] {String.class}, new Object[]{lastUsedFile.getFilename()}); 
         *} catch(DomainObjectCreationException doce) {
         * new TransMeta(lastUsedFile.getFilename());
         *}
         * transMeta.setFilename(lastUsedFile.getFilename());
         * transMeta.clearChanged();
         * props.addLastFile(LastUsedFile.FILE_TYPE_TRANSFORMATION,
         * lastUsedFile.getFilename(), null, false, null);
         * addTransGraph(transMeta);
         */
      }
      if (lastUsedFile.isJob()) {
        openFile(lastUsedFile.getFilename(), false);
        /*
         * JobMeta jobMeta = new JobMeta(log, lastUsedFile.getFilename(), rep);
         * jobMeta.setFilename(lastUsedFile.getFilename());
         * jobMeta.clearChanged(); props.addLastFile(LastUsedFile.FILE_TYPE_JOB,
         * lastUsedFile.getFilename(), null, false, null); addJobGraph(jobMeta);
         */
      }
      refreshTree();
    }
  }

  /**
   * Create a new SelectValues step in between this step and the previous. If
   * the previous fields are not there, no mapping can be made, same with the
   * required fields.
   * 
   * @param stepMeta
   *            The target step to map against.
   */
  // retry of required fields acquisition
  public void generateFieldMapping(TransMeta transMeta, StepMeta stepMeta) {
    try {
      if (stepMeta != null) {
        StepMetaInterface smi = stepMeta.getStepMetaInterface();
        RowMetaInterface targetFields = smi.getRequiredFields(transMeta);
        RowMetaInterface sourceFields = transMeta.getPrevStepFields(stepMeta);

        // Build the mapping: let the user decide!!
        String[] source = sourceFields.getFieldNames();
        for (int i = 0; i < source.length; i++) {
          ValueMetaInterface v = sourceFields.getValueMeta(i);
          source[i] += EnterMappingDialog.STRING_ORIGIN_SEPARATOR + v.getOrigin() + ")";
        }
        String[] target = targetFields.getFieldNames();

        EnterMappingDialog dialog = new EnterMappingDialog(shell, source, target);
        List<SourceToTargetMapping> mappings = dialog.open();
        if (mappings != null) {
          // OK, so we now know which field maps where.
          // This allows us to generate the mapping using a
          // SelectValues Step...
          SelectValuesMeta svm = new SelectValuesMeta();
          svm.allocate(mappings.size(), 0, 0);

          for (int i = 0; i < mappings.size(); i++) {
            SourceToTargetMapping mapping = mappings.get(i);
            svm.getSelectName()[i] = sourceFields.getValueMeta(mapping.getSourcePosition()).getName();
            svm.getSelectRename()[i] = target[mapping.getTargetPosition()];
            svm.getSelectLength()[i] = -1;
            svm.getSelectPrecision()[i] = -1;
          }
          // a new comment
          // Now that we have the meta-data, create a new step info
          // object

          String stepName = stepMeta.getName() + " Mapping";
          stepName = transMeta.getAlternativeStepname(stepName); // if
          // it's
          // already
          // there,
          // rename
          // it.

          StepMeta newStep = new StepMeta("SelectValues", stepName, svm);
          newStep.setLocation(stepMeta.getLocation().x + 20, stepMeta.getLocation().y + 20);
          newStep.setDraw(true);

          transMeta.addStep(newStep);
          addUndoNew(transMeta, new StepMeta[] { newStep }, new int[] { transMeta.indexOfStep(newStep) });

          // Redraw stuff...
          refreshTree();
          refreshGraph();
        }
      } else {
        throw new KettleException("There is no target to do a field mapping against!");
      }
    } catch (KettleException e) {
      new ErrorDialog(shell, "Error creating mapping",
          "There was an error when Kettle tried to generate a field mapping against the target step", e);
    }
  }

  public void editPartitioning(TransMeta transMeta, StepMeta stepMeta) {
    
    // Before we start, check if there are any partition schemas defined...
    //
    String schemaNames[] = transMeta.getPartitionSchemasNames();
    if (schemaNames.length == 0) {
      MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
      box.setText("Create a partition schema");
      box.setMessage("You first need to create one or more partition schemas in the transformation settings dialog before you can select one!");
      box.open();
      return;
    }
    
    StepPartitioningMeta stepPartitioningMeta = stepMeta.getStepPartitioningMeta();
    if (stepPartitioningMeta == null)
      stepPartitioningMeta = new StepPartitioningMeta();

    StepMeta before = (StepMeta) stepMeta.clone();

    PluginRegistry registry = PluginRegistry.getInstance();
    List<PluginInterface> plugins = registry.getPlugins(PartitionerPluginType.class);

    String options[] = new String[StepPartitioningMeta.methodDescriptions.length + plugins.size()];
    String codes[] = new String[StepPartitioningMeta.methodDescriptions.length + plugins.size()];
    System.arraycopy(StepPartitioningMeta.methodDescriptions, 0, options, 0,
        StepPartitioningMeta.methodDescriptions.length);
    System.arraycopy(StepPartitioningMeta.methodCodes, 0, codes, 0, StepPartitioningMeta.methodCodes.length);

    Iterator<PluginInterface> it = plugins.iterator();
    int idx = 0;
    while (it.hasNext()) {
      PluginInterface entry = it.next();
      options[StepPartitioningMeta.methodDescriptions.length + idx] = entry.getDescription();
      codes[StepPartitioningMeta.methodCodes.length + idx] = entry.getIds()[0];
      idx++;
    }

    for (int i = 0; i < codes.length; i++) {
      if (codes[i].equals(stepPartitioningMeta.getMethod())) {
        idx = i;
        break;
      }
    }

    EnterSelectionDialog dialog = new EnterSelectionDialog(shell, options, "Partioning method",
        "Select the partitioning method");
    String methodDescription = dialog.open(idx);
    if (methodDescription != null) {
      String method = StepPartitioningMeta.methodCodes[StepPartitioningMeta.PARTITIONING_METHOD_NONE];
      for (int i = 0; i < options.length; i++) {
        if (options[i].equals(methodDescription)) {
          method = codes[i];
        }
      }

      try {
        int methodType = StepPartitioningMeta.getMethodType(method);
        stepPartitioningMeta.setMethodType(methodType);
        stepPartitioningMeta.setMethod(method);
        switch (methodType) {
          case StepPartitioningMeta.PARTITIONING_METHOD_NONE:
            break;
          case StepPartitioningMeta.PARTITIONING_METHOD_MIRROR:
          case StepPartitioningMeta.PARTITIONING_METHOD_SPECIAL:


              // Set the partitioning schema too.
              PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();
              idx = -1;
              if (partitionSchema != null) {
                idx = Const.indexOfString(partitionSchema.getName(), schemaNames);
              }
              EnterSelectionDialog askSchema = new EnterSelectionDialog(shell, schemaNames,
                  "Select a partition schema", "Select the partition schema to use:");
              String schemaName = askSchema.open(idx);
              if (schemaName != null) {
                idx = Const.indexOfString(schemaName, schemaNames);
                stepPartitioningMeta.setPartitionSchema(transMeta.getPartitionSchemas().get(idx));
              }
            

            if (methodType == StepPartitioningMeta.PARTITIONING_METHOD_SPECIAL) {
              // ask for a fieldname

              StepDialogInterface partitionerDialog = null;
              try {
                partitionerDialog = delegates.steps.getPartitionerDialog(stepMeta, stepPartitioningMeta, transMeta);
                partitionerDialog.open();
              } catch (Exception e) {
                new ErrorDialog(shell, "Error",
                    "There was an unexpected error while editing the partitioning method specifics:", e);
              }
            }
            break;
        }
        StepMeta after = (StepMeta) stepMeta.clone();
        addUndoChange(transMeta, new StepMeta[] { before }, new StepMeta[] { after }, new int[] { transMeta
            .indexOfStep(stepMeta) });

        refreshGraph();
      } catch (Exception e) {
        new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.ErrorEditingStepPartitioning.Title"), BaseMessages
            .getString(PKG, "Spoon.ErrorEditingStepPartitioning.Message"), e);
      }
    }
  }

  /**
   * Select a clustering schema for this step.
   * 
   * @param stepMeta
   *            The step to set the clustering schema for.
   */
  public void editClustering(TransMeta transMeta, StepMeta stepMeta) {
    List<StepMeta> stepMetas = new ArrayList<StepMeta>();
    stepMetas.add(stepMeta);
    editClustering(transMeta, stepMetas);
  }

  /**
   * Select a clustering schema for this step.
   * 
   * @param stepMeta
   *            The steps (at least one!) to set the clustering schema for.
   */
  public void editClustering(TransMeta transMeta, List<StepMeta> stepMetas) {
    StepMeta stepMeta = stepMetas.get(0);
    int idx = -1;
    if (stepMeta.getClusterSchema() != null) {
      idx = transMeta.getClusterSchemas().indexOf(stepMeta.getClusterSchema());
    }
    String[] clusterSchemaNames = transMeta.getClusterSchemaNames();
    EnterSelectionDialog dialog = new EnterSelectionDialog(shell, clusterSchemaNames, "Cluster schema",
        "Select the cluster schema to use (cancel=clear)");
    String schemaName = dialog.open(idx);
    if (schemaName == null) {
      for (StepMeta step : stepMetas) {
        step.setClusterSchema(null);
      }
    } else {
      ClusterSchema clusterSchema = transMeta.findClusterSchema(schemaName);
      for (StepMeta step : stepMetas) {
        step.setClusterSchema(clusterSchema);
      }
    }

    refreshTree();
    refreshGraph();
  }

  public void createKettleArchive(TransMeta transMeta) {
    if (transMeta == null)
      return;
    JarfileGenerator.generateJarFile(transMeta);
  }

  /**
   * This creates a new partitioning schema, edits it and adds it to the
   * transformation metadata
   * 
   */
  public void newPartitioningSchema(TransMeta transMeta) {
    PartitionSchema partitionSchema = new PartitionSchema();

    PartitionSchemaDialog dialog = new PartitionSchemaDialog(shell, partitionSchema, transMeta.getDatabases(),
        transMeta);
    if (dialog.open()) {
      transMeta.getPartitionSchemas().add(partitionSchema);
      
      if (rep!=null) {
        try {
          if (!rep.getSecurityProvider().isReadOnly()) {
            rep.save(partitionSchema, Const.VERSION_COMMENT_INITIAL_VERSION, null);
          } else {
            throw new KettleException(BaseMessages.getString(PKG, "Spoon.Dialog.Exception.ReadOnlyRepositoryUser"));
          }
        } catch (KettleException e) {
          new ErrorDialog(getShell(), BaseMessages.getString(PKG, "Spoon.Dialog.ErrorSavingPartition.Title"), BaseMessages.getString(PKG, "Spoon.Dialog.ErrorSavingPartition.Message", partitionSchema.getName()), e);
        }
      }
      
      refreshTree();
    }
  }

  private void editPartitionSchema(TransMeta transMeta, PartitionSchema partitionSchema) {
    PartitionSchemaDialog dialog = new PartitionSchemaDialog(shell, partitionSchema, transMeta.getDatabases(),
        transMeta);
    if (dialog.open()) {
      refreshTree();
    }
  }

  private void delPartitionSchema(TransMeta transMeta, PartitionSchema partitionSchema) {
    try {
      if (rep != null && partitionSchema.getObjectId() != null) {
        // remove the partition schema from the repository too...
        rep.deletePartitionSchema(partitionSchema.getObjectId());
      }

      int idx = transMeta.getPartitionSchemas().indexOf(partitionSchema);
      transMeta.getPartitionSchemas().remove(idx);
      refreshTree();
    } catch (KettleException e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Dialog.ErrorDeletingClusterSchema.Title"), BaseMessages
          .getString(PKG, "Spoon.Dialog.ErrorDeletingClusterSchema.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /**
   * This creates a new clustering schema, edits it and adds it to the
   * transformation metadata
   * 
   */
  public void newClusteringSchema(TransMeta transMeta) {
    ClusterSchema clusterSchema = new ClusterSchema();

    ClusterSchemaDialog dialog = new ClusterSchemaDialog(shell, clusterSchema, transMeta.getSlaveServers());
    if (dialog.open()) {
      transMeta.getClusterSchemas().add(clusterSchema);
      
      if (rep!=null) {
        try {
          if (!rep.getSecurityProvider().isReadOnly()) {
            rep.save(clusterSchema, Const.VERSION_COMMENT_INITIAL_VERSION, null);
          } else {
            throw new KettleException(BaseMessages.getString(PKG, "Spoon.Dialog.Exception.ReadOnlyRepositoryUser"));
          }
        } catch (KettleException e) {
          new ErrorDialog(getShell(), BaseMessages.getString(PKG, "Spoon.Dialog.ErrorSavingCluster.Title"), BaseMessages.getString(PKG, "Spoon.Dialog.ErrorSavingCluster.Message", clusterSchema.getName()), e);
        }
      }
      
      refreshTree();
    }
  }

  private void editClusterSchema(TransMeta transMeta, ClusterSchema clusterSchema) {
    ClusterSchemaDialog dialog = new ClusterSchemaDialog(shell, clusterSchema, transMeta.getSlaveServers());
    if (dialog.open()) {
      refreshTree();
    }
  }

  private void delClusterSchema(TransMeta transMeta, ClusterSchema clusterSchema) {
    try {
      if (rep != null && clusterSchema.getObjectId() != null) {
        // remove the partition schema from the repository too...
        rep.deleteClusterSchema(clusterSchema.getObjectId());
      }

      int idx = transMeta.getClusterSchemas().indexOf(clusterSchema);
      transMeta.getClusterSchemas().remove(idx);
      refreshTree();
    } catch (KettleException e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Dialog.ErrorDeletingPartitionSchema.Title"),
          BaseMessages.getString(PKG, "Spoon.Dialog.ErrorDeletingPartitionSchema.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /**
   * This creates a slave server, edits it and adds it to the transformation
   * metadata
   * 
   */
  public void newSlaveServer(HasSlaveServersInterface hasSlaveServersInterface) {
    delegates.slaves.newSlaveServer(hasSlaveServersInterface);
  }

  public void delSlaveServer(HasSlaveServersInterface hasSlaveServersInterface, SlaveServer slaveServer) {
    try {
      delegates.slaves.delSlaveServer(hasSlaveServersInterface, slaveServer);
    } catch (KettleException e) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(PKG, "Spoon.Dialog.ErrorDeletingSlave.Title"), BaseMessages.getString(PKG, "Spoon.Dialog.ErrorDeletingSlave.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /**
   * Sends transformation to slave server
   * 
   * @param executionConfiguration
   */
  public void sendTransformationXMLToSlaveServer(TransMeta transMeta, TransExecutionConfiguration executionConfiguration) {
    try {
      Trans.sendToSlaveServer(transMeta, executionConfiguration, rep);
    } catch (Exception e) {
      new ErrorDialog(shell, "Error", "Error sending transformation to server", e);
    }
  }

  public void runFile() {
    executeFile(true, false, false, false, false, null, false);
  }

  public void replayTransformation() {
    TransExecutionConfiguration tc = this.getTransExecutionConfiguration();
    executeFile(tc.isExecutingLocally(), tc.isExecutingRemotely(), tc.isExecutingClustered(), false, false, new Date(),
        false);
  }

  public void previewFile() {
    executeFile(true, false, false, true, false, null, true);
  }

  public void debugFile() {
    executeFile(true, false, false, false, true, null, true);
  }

  public void executeFile(boolean local, boolean remote, boolean cluster, boolean preview, boolean debug,
      Date replayDate, boolean safe) {

    TransMeta transMeta = getActiveTransformation();
    if (transMeta != null)
      executeTransformation(transMeta, local, remote, cluster, preview, debug, replayDate, safe);

    JobMeta jobMeta = getActiveJob();
    if (jobMeta != null)
      executeJob(jobMeta, local, remote, replayDate, safe, null, 0);

  }

  public void executeTransformation(final TransMeta transMeta, final boolean local, final boolean remote,
      final boolean cluster, final boolean preview, final boolean debug, final Date replayDate, final boolean safe) {
    new Thread() {
      public void run() {
        getDisplay().asyncExec(new Runnable() {
          public void run() {
            try {
              delegates.trans
                  .executeTransformation(transMeta, local, remote, cluster, preview, debug, replayDate, safe);
            } catch (Exception e) {
              new ErrorDialog(shell, "Execute transformation", "There was an error during transformation execution", e);
            }
          }
        });
      }
    }.start();
  }

  public void executeJob(JobMeta jobMeta, boolean local, boolean remote, Date replayDate, boolean safe, String startCopyName, int startCopyNr) {
    // delegates.jobs.addJobLog(jobMeta);
    // JobLog jobLog = getActiveJobLog();
    // jobLog.startJob(replayDate);

    try {
      delegates.jobs.executeJob(jobMeta, local, remote, replayDate, safe, startCopyName, startCopyNr);
    } catch (Exception e) {
      new ErrorDialog(shell, "Execute job", "There was an error during job execution", e);
    }

  }

  public void addSpoonSlave(SlaveServer slaveServer) {
    delegates.slaves.addSpoonSlave(slaveServer);
  }

  public void addJobHistory(JobMeta jobMeta, boolean select) {
    JobGraph activeJobGraph = getActiveJobGraph();
    if (activeJobGraph != null) {
      activeJobGraph.jobHistoryDelegate.addJobHistory();
    }

    // delegates.jobs.addJobHistory(jobMeta, select);
  }

  public void paste() {
    String clipcontent = fromClipboard();
    if (clipcontent != null) {
      // Load the XML
      //
      try {
        Document document = XMLHandler.loadXMLString(clipcontent);

        boolean transformation = XMLHandler.getSubNode(document, TransMeta.XML_TAG) != null;
        boolean job = XMLHandler.getSubNode(document, JobMeta.XML_TAG) != null;
        boolean steps = XMLHandler.getSubNode(document, Spoon.XML_TAG_TRANSFORMATION_STEPS) != null;
        boolean jobEntries = XMLHandler.getSubNode(document, Spoon.XML_TAG_JOB_JOB_ENTRIES) != null;

        if (transformation) {
          pasteTransformation();
        } else if (job) {
          pasteJob();
        } else if (steps) {
          TransGraph transGraph = getActiveTransGraph();
          if (transGraph != null && transGraph.getLastMove() != null) {
            pasteXML(transGraph.getManagedObject(), clipcontent, transGraph.screen2real(transGraph.getLastMove().x, transGraph.getLastMove().y));
          }
        } else if (jobEntries) {
          JobGraph jobGraph = getActiveJobGraph();
          if (jobGraph != null && jobGraph.getLastMove() != null) {
            pasteXML(jobGraph.getManagedObject(), clipcontent, jobGraph.getLastMove());
          }

        }
      } catch (KettleXMLException e) {
        log.logError("Unable to paste", e);
      }
    }

  }

  public JobEntryCopy newJobEntry(JobMeta jobMeta, String typeDesc, boolean openit) {
    return delegates.jobs.newJobEntry(jobMeta, typeDesc, openit);
  }

  public JobEntryDialogInterface getJobEntryDialog(JobEntryInterface jei, JobMeta jobMeta) {

    return delegates.jobs.getJobEntryDialog(jei, jobMeta);
  }

  public StepDialogInterface getStepEntryDialog(StepMetaInterface stepMeta, TransMeta transMeta, String stepName) {
    try {
      return delegates.steps.getStepEntryDialog(stepMeta, transMeta, stepName);
    } catch (Throwable t) {
      log.logError("Could not create dialog for " + stepMeta.getDialogClassName(), t);
    }
    return null;
  }

  public void editJobEntry(JobMeta jobMeta, JobEntryCopy je) {
    delegates.jobs.editJobEntry(jobMeta, je);
  }

  public void deleteJobEntryCopies(JobMeta jobMeta, JobEntryCopy jobEntry) {
    delegates.jobs.deleteJobEntryCopies(jobMeta, jobEntry);
  }

  public void pasteXML(JobMeta jobMeta, String clipcontent, Point loc) {
    delegates.jobs.pasteXML(jobMeta, clipcontent, loc);
  }

  public void newJobHop(JobMeta jobMeta, JobEntryCopy fr, JobEntryCopy to) {
    delegates.jobs.newJobHop(jobMeta, fr, to);
  }

  /**
   * Create a job that extracts tables & data from a database.
   * <p>
   * <p>
   * 
   * 0) Select the database to rip
   * <p>
   * 1) Select the tables in the database to rip
   * <p>
   * 2) Select the database to dump to
   * <p>
   * 3) Select the repository directory in which it will end up
   * <p>
   * 4) Select a name for the new job
   * <p>
   * 5) Create an empty job with the selected name.
   * <p>
   * 6) Create 1 transformation for every selected table
   * <p>
   * 7) add every created transformation to the job & evaluate
   * <p>
   * 
   */
  public void ripDBWizard() {
    delegates.jobs.ripDBWizard();
  }

  public JobMeta ripDB(final List<DatabaseMeta> databases, final String jobName, final RepositoryDirectory repdir,
      final String directory, final DatabaseMeta sourceDbInfo, final DatabaseMeta targetDbInfo, final String[] tables) {
    return delegates.jobs.ripDB(databases, jobName, repdir, directory, sourceDbInfo, targetDbInfo, tables);
  }

  /**
   * Set the core object state.
   * 
   * @param state
   */
  public void setCoreObjectsState(int state) {
    coreObjectsState = state;
  }

  /**
   * Get the core object state.
   * 
   * @return state.
   */
  public int getCoreObjectsState() {
    return coreObjectsState;
  }

  public LogChannelInterface getLog() {
    return log;
  }

  public Repository getRepository() {
    return rep;
  }

  public void setRepository(Repository rep) {
    this.rep = rep;
    if (rep != null) {
      this.capabilities = rep.getRepositoryMeta().getRepositoryCapabilities();
    }
    // Registering the UI Support classes
    UISupportRegistery.getInstance().registerUISupport(RepositorySecurityProvider.class,
        BaseRepositoryExplorerUISupport.class);
    UISupportRegistery.getInstance().registerUISupport(RepositorySecurityManager.class, ManageUserUISupport.class);
    if (rep != null) {
      SpoonPluginManager.getInstance().notifyLifecycleListeners(SpoonLifeCycleEvent.REPOSITORY_CHANGED);
    }
    delegates.update(this);
    enableMenus();
  }

  public void addMenuListener(String id, Object listener, String methodName) {
    menuListeners.add(new Object[] { id, listener, methodName });
  }

  public void addTransGraph(TransMeta transMeta) {
    delegates.trans.addTransGraph(transMeta);
  }

  public void addJobGraph(JobMeta jobMeta) {
    delegates.jobs.addJobGraph(jobMeta);
  }

  public boolean addSpoonBrowser(String name, String urlString, LocationListener locationListener) {
    return delegates.tabs.addSpoonBrowser(name, urlString, locationListener);
  }

  public boolean addSpoonBrowser(String name, String urlString) {
    return delegates.tabs.addSpoonBrowser(name, urlString, null);
  }

  public TransExecutionConfiguration getTransExecutionConfiguration() {
    return transExecutionConfiguration;
  }

  public Object[] messageDialogWithToggle(String dialogTitle, Object image, String message, int dialogImageType,
      String[] buttonLabels, int defaultIndex, String toggleMessage, boolean toggleState) {
    return GUIResource.getInstance().messageDialogWithToggle(shell, dialogTitle, (Image) image, message,
        dialogImageType, buttonLabels, defaultIndex, toggleMessage, toggleState);
  }

  public void editStepErrorHandling(TransMeta transMeta, StepMeta stepMeta) {
    delegates.steps.editStepErrorHandling(transMeta, stepMeta);
  }

  public String editStep(TransMeta transMeta, StepMeta stepMeta) {
    return delegates.steps.editStep(transMeta, stepMeta);
  }

  public void dupeStep(TransMeta transMeta, StepMeta stepMeta) {
    delegates.steps.dupeStep(transMeta, stepMeta);
  }

  public void delStep(TransMeta transMeta, StepMeta stepMeta) {
    delegates.steps.delStep(transMeta, stepMeta);
  }

  public String makeTabName(EngineMetaInterface transMeta, boolean showingLocation) {
    return delegates.tabs.makeTabName(transMeta, showingLocation);
  }

  public void newConnection() {
    delegates.db.newConnection();
  }

  public void getSQL() {
    delegates.db.getSQL();
  }

  public boolean overwritePrompt(String message, String rememberText, String rememberPropertyName) {
    Object res[] = messageDialogWithToggle(
        "Warning", null, message, Const.WARNING, new String[] { BaseMessages.getString(PKG, "System.Button.Yes"), //$NON-NLS-1$ 
            BaseMessages.getString(PKG, "System.Button.No") },//$NON-NLS-1$
        1, rememberText, !"Y".equalsIgnoreCase(props.getProperty(rememberPropertyName)));
    int idx = ((Integer) res[0]).intValue();
    boolean overwrite = ((idx & 0xFF) == 0);
    boolean toggleState = ((Boolean) res[1]).booleanValue();
    props.setProperty(rememberPropertyName, (!toggleState) ? "Y" : "N");
    return overwrite;

  }

  public boolean messageBox(final String message, final String text, final boolean allowCancel, final int type) {

    final StringBuffer answer = new StringBuffer("N");

    display.syncExec(new Runnable() {

      public void run() {
        int flags = SWT.OK;
        if (allowCancel) {
          flags |= SWT.CANCEL;
        }
        switch (type) {
          case Const.INFO:
            flags |= SWT.ICON_INFORMATION;
            break;
          case Const.ERROR:
            flags |= SWT.ICON_ERROR;
            break;
          case Const.WARNING:
            flags |= SWT.ICON_WARNING;
            break;
        }

        Shell shell = new Shell(display);
        MessageBox mb = new MessageBox(shell, flags);
        // Set the Body Message
        mb.setMessage(message);
        // Set the title Message
        mb.setText(text);
        if (mb.open() == SWT.OK) {
          answer.setCharAt(0, 'Y');
        }
      }
    });

    return "Y".equalsIgnoreCase(answer.toString());
  }

  /**
   * @return the previewExecutionConfiguration
   */
  public TransExecutionConfiguration getTransPreviewExecutionConfiguration() {
    return transPreviewExecutionConfiguration;
  }

  /**
   * @param previewExecutionConfiguration
   *            the previewExecutionConfiguration to set
   */
  public void setTransPreviewExecutionConfiguration(TransExecutionConfiguration previewExecutionConfiguration) {
    this.transPreviewExecutionConfiguration = previewExecutionConfiguration;
  }

  /**
   * @return the debugExecutionConfiguration
   */
  public TransExecutionConfiguration getTransDebugExecutionConfiguration() {
    return transDebugExecutionConfiguration;
  }

  /**
   * @param debugExecutionConfiguration
   *            the debugExecutionConfiguration to set
   */
  public void setTransDebugExecutionConfiguration(TransExecutionConfiguration debugExecutionConfiguration) {
    this.transDebugExecutionConfiguration = debugExecutionConfiguration;
  }

  /**
   * @param executionConfiguration
   *            the executionConfiguration to set
   */
  public void setTransExecutionConfiguration(TransExecutionConfiguration executionConfiguration) {
    this.transExecutionConfiguration = executionConfiguration;
  }

  /**
   * @return the jobExecutionConfiguration
   */
  public JobExecutionConfiguration getJobExecutionConfiguration() {
    return jobExecutionConfiguration;
  }

  /**
   * @param jobExecutionConfiguration
   *            the jobExecutionConfiguration to set
   */
  public void setJobExecutionConfiguration(JobExecutionConfiguration jobExecutionConfiguration) {
    this.jobExecutionConfiguration = jobExecutionConfiguration;
  }

  /*
   * public XulToolbar getToolbar() { return toolbar; }
   */

  public void update(ChangedFlagInterface o, Object arg) {
    try {
      Method m = getClass().getMethod(arg.toString());

      if (m != null)
        m.invoke(this);
    } catch (Exception e) {
      // ignore... let the other notifiers try to do something
      System.out.println("Unable to update: " + e.getLocalizedMessage());
    }
  }

  public void consume(final LifeEventInfo info) {
    // if (PropsUI.getInstance().isListenerDisabled(info.getName()))
    // return;

    if (info.hasHint(LifeEventInfo.Hint.DISPLAY_BROWSER)) {
      display.asyncExec(new Runnable() {
        public void run() {
          delegates.tabs.addSpoonBrowser(info.getName(), info.getMessage(), false, null);
        }
      });

    } else {
      MessageBox box = new MessageBox(shell, (info.getState() != LifeEventInfo.State.SUCCESS ? SWT.ICON_ERROR
          : SWT.ICON_INFORMATION)
          | SWT.OK);
      box.setText(info.getName());
      box.setMessage(info.getMessage());
      box.open();
    }

  }

  public void setLog() {
    LogSettingsDialog lsd = new LogSettingsDialog(shell, SWT.NONE, props);
    lsd.open();
    log.setLogLevel(DefaultLogLevel.getLogLevel());
  }

  /**
   * @return the display
   */
  public Display getDisplay() {
    return display;
  }

  public void zoomIn() {
    TransGraph transGraph = getActiveTransGraph();
    if (transGraph != null)
      transGraph.zoomIn();
    JobGraph jobGraph = getActiveJobGraph();
    if (jobGraph != null)
      jobGraph.zoomIn();
  }

  public void zoomOut() {
    TransGraph transGraph = getActiveTransGraph();
    if (transGraph != null)
      transGraph.zoomOut();
    JobGraph jobGraph = getActiveJobGraph();
    if (jobGraph != null)
      jobGraph.zoomOut();
  }

  public void zoom100Percent() {
    TransGraph transGraph = getActiveTransGraph();
    if (transGraph != null)
      transGraph.zoom100Percent();
    JobGraph jobGraph = getActiveJobGraph();
    if (jobGraph != null)
      jobGraph.zoom100Percent();
  }

  public void setParametersAsVariablesInUI(NamedParams namedParameters, VariableSpace space) {
    for (String param : namedParameters.listParameters()) {
      try {
        space.setVariable(param, Const.NVL(namedParameters.getParameterValue(param), Const.NVL(namedParameters
            .getParameterDefault(param), Const.NVL(space.getVariable(param), ""))));
      } catch (Exception e) {
        // ignore this
      }
    }
  }

  public void browseVersionHistory() {
    if (rep == null)
      return;
    TransGraph transGraph = getActiveTransGraph();
    if (transGraph != null) {
      transGraph.browseVersionHistory();
    }

    JobGraph jobGraph = getActiveJobGraph();
    if (jobGraph != null) {
      jobGraph.browseVersionHistory();
    }

  }

  public Trans findActiveTrans(Job job, JobEntryCopy jobEntryCopy) {
    JobEntryTrans jobEntryTrans = job.getActiveJobEntryTransformations().get(jobEntryCopy);
    if (jobEntryTrans == null)
      return null;
    return jobEntryTrans.getTrans();
  }

  public Job findActiveJob(Job job, JobEntryCopy jobEntryCopy) {
    JobEntryJob jobEntryJob = job.getActiveJobEntryJobs().get(jobEntryCopy);
    if (jobEntryJob == null)
      return null;
    return jobEntryJob.getJob();
  }

  public Object getSelectionObject() {
    return selectionObject;
  }

  public RepositoryDirectoryInterface getDefaultSaveLocation(RepositoryElementInterface repositoryElement) {
    try {
      if (getRepository() != defaultSaveLocationRepository) {
        // The repository has changed, reset the defaultSaveLocation
        defaultSaveLocation = null;
        defaultSaveLocationRepository = null;
      }

      if (defaultSaveLocation == null) {
        if (getRepository() != null) {
          defaultSaveLocation = getRepository().getDefaultSaveDirectory(repositoryElement);
          defaultSaveLocationRepository = getRepository();
        } else {
          defaultSaveLocation = new RepositoryDirectory();
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return defaultSaveLocation;
  }

  /* ========================= XulEventSource Methods ========================== */

  protected PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    changeSupport.addPropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    changeSupport.addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    changeSupport.removePropertyChangeListener(listener);
  }

  protected void firePropertyChange(String attr, Object previousVal, Object newVal) {
    if (previousVal == null && newVal == null) {
      return;
    }
    changeSupport.firePropertyChange(attr, previousVal, newVal);
  }

  /*
   * ========================= End XulEventSource Methods
   * ==========================
   */

  /*
   * ========================= Start XulEventHandler Methods
   * ==========================
   */

  public Object getData() {
    return null;
  }

  public String getName() {
    return "spoon";
  }

  public XulDomContainer getXulDomContainer() {
    return getMainSpoonContainer();
  }

  public void setData(Object arg0) {
  }

  public void setName(String arg0) {
  }

  public void setXulDomContainer(XulDomContainer arg0) {
  }

  public RepositorySecurityManager getSecurityManager() {
    return rep.getSecurityManager();
  }
  public void displayDbDependancies() {
		  TreeItem[] selection = selectionTree.getSelection();
		  if(selection==null || selection.length!=1) return;
		  // Clear all dependancies for select connection
		  TreeItem parent=selection[0];
			if(parent!=null) {
				int nrChilds=parent.getItemCount();
				if(nrChilds>0) {
					for(int i=0; i<nrChilds; i++) {
						parent.getItem(i).dispose();
					}
				}
			}
		 if(rep==null) {
			 return;
		 }
		
		try {
		
		    final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
			String jobList[] = rep.getJobsUsingDatabase(databaseMeta.getObjectId());
			String transList[] = rep.getTransformationsUsingDatabase(databaseMeta.getObjectId());
			if (jobList.length == 0 && transList.length == 0) {
				MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION	| SWT.OK);
				box.setText("Connection dependancies");
				box.setMessage("This connection is not used by a job nor a transformation.");
				box.open();
			} else {
				for (int i = 0; i < jobList.length; i++) {
					if(jobList[i]!=null) {
						TreeItem tidep = new TreeItem(parent, SWT.NONE);
						tidep.setImage(GUIResource.getInstance().getImageJobGraph());
						tidep.setText(jobList[i]);
					}
				}
			

				for (int i = 0; i < transList.length; i++) {
					if(transList[i]!=null) {
						TreeItem tidep = new TreeItem(parent, SWT.NONE);
						tidep.setImage(GUIResource.getInstance().getImageTransGraph());
						tidep.setText(transList[i]);
					}
				}
				parent.setExpanded(true);
			}
		} catch (Exception e) {
			new ErrorDialog(shell,"Error","Error getting dependancies! :",e);
		}
	}
  
    public void fireMenuControlers() { 
    	if (!Display.getDefault().getThread().equals(Thread.currentThread()) ) {
            display.syncExec(new Runnable() {
                public void run() {
                	fireMenuControlers();
                }
            });
            return;
    	}
        org.pentaho.ui.xul.dom.Document doc = null;
        if(mainSpoonContainer != null) {
          doc = mainSpoonContainer.getDocumentRoot();    	
	      for (ISpoonMenuController menuController : menuControllers) {
	        menuController.updateMenu(doc);
	      }
        }
    }
    
    public void hideSplash() {
       if (splash!=null) {
          splash.hide();
       }
    }
    
    private void showSplash() {
       if (splash != null) {
          splash.show();
       }
    }
}