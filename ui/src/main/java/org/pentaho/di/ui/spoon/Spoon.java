//CHECKSTYLE:FileLength:OFF
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.window.ApplicationWindow;
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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
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
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.AddUndoPositionInterface;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.JndiUtil;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.ObjectUsageCount;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.XmlExportHelper;
import org.pentaho.di.core.changed.ChangedFlagInterface;
import org.pentaho.di.core.changed.PDIObserver;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleAuthException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleRowException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
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
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.FileLoggingEventListener;
import org.pentaho.di.core.logging.JobEntryLogTable;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.MetricsLogTable;
import org.pentaho.di.core.logging.PerformanceLogTable;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.logging.StepLogTable;
import org.pentaho.di.core.logging.TransLogTable;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.LifecyclePluginType;
import org.pentaho.di.core.plugins.PartitionerPluginType;
import org.pentaho.di.core.plugins.PluginFolder;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.plugins.PluginTypeListener;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.reflection.StringSearchResult;
import org.pentaho.di.core.row.RowBuffer;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.undo.TransAction;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.vfs.KettleVfsDelegatingResolver;
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
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.pan.CommandLineOption;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.pkg.JarfileGenerator;
import org.pentaho.di.repository.KettleRepositoryLostException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryCapabilities;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObject;
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
import org.pentaho.di.trans.step.RowDistributionInterface;
import org.pentaho.di.trans.step.RowDistributionPluginType;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.FileDialogOperation;
import org.pentaho.di.ui.core.PrintSpool;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.auth.AuthProviderDialog;
import org.pentaho.di.ui.core.database.wizard.CreateDatabaseWizard;
import org.pentaho.di.ui.core.dialog.AboutDialog;
import org.pentaho.di.ui.core.dialog.BrowserEnvironmentWarningDialog;
import org.pentaho.di.ui.core.dialog.CheckResultDialog;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.EnterOptionsDialog;
import org.pentaho.di.ui.core.dialog.EnterSearchDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.EnterStringsDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.KettlePropertiesFileDialog;
import org.pentaho.di.ui.core.dialog.PopupOverwritePrompter;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.dialog.ShowBrowserDialog;
import org.pentaho.di.ui.core.dialog.ShowMessageDialog;
import org.pentaho.di.ui.core.dialog.SimpleMessageDialog;
import org.pentaho.di.ui.core.dialog.Splash;
import org.pentaho.di.ui.core.dialog.SubjectDataBrowserDialog;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.ProviderFilterType;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.OsHelper;
import org.pentaho.di.ui.core.widget.tree.TreeToolbar;
import org.pentaho.di.ui.imp.ImportRulesDialog;
import org.pentaho.di.ui.job.dialog.JobDialogPluginType;
import org.pentaho.di.ui.job.dialog.JobLoadProgressDialog;
import org.pentaho.di.ui.repository.ILoginCallback;
import org.pentaho.di.ui.repository.RepositoriesDialog;
import org.pentaho.di.ui.repository.RepositorySecurityUI;
import org.pentaho.di.ui.repository.dialog.RepositoryDialogInterface;
import org.pentaho.di.ui.repository.dialog.RepositoryExportProgressDialog;
import org.pentaho.di.ui.repository.dialog.RepositoryImportProgressDialog;
import org.pentaho.di.ui.repository.dialog.RepositoryRevisionBrowserDialogInterface;
import org.pentaho.di.ui.repository.dialog.SelectDirectoryDialog;
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
import org.pentaho.di.ui.spoon.dialog.MetaStoreExplorerDialog;
import org.pentaho.di.ui.spoon.dialog.SaveProgressDialog;
import org.pentaho.di.ui.spoon.job.JobGraph;
import org.pentaho.di.ui.spoon.partition.PartitionMethodSelector;
import org.pentaho.di.ui.spoon.partition.PartitionSettings;
import org.pentaho.di.ui.spoon.partition.processor.MethodProcessor;
import org.pentaho.di.ui.spoon.partition.processor.MethodProcessorFactory;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.di.ui.spoon.tree.TreeManager;
import org.pentaho.di.ui.spoon.tree.extension.TreePaneExtension;
import org.pentaho.di.ui.spoon.tree.extension.TreePaneManager;
import org.pentaho.di.ui.spoon.tree.provider.ClustersFolderProvider;
import org.pentaho.di.ui.spoon.tree.provider.DBConnectionFolderProvider;
import org.pentaho.di.ui.spoon.tree.provider.HopsFolderProvider;
import org.pentaho.di.ui.spoon.tree.provider.JobEntriesFolderProvider;
import org.pentaho.di.ui.spoon.tree.provider.PartitionsFolderProvider;
import org.pentaho.di.ui.spoon.tree.provider.SlavesFolderProvider;
import org.pentaho.di.ui.spoon.tree.provider.StepsFolderProvider;
import org.pentaho.di.ui.spoon.wizards.CopyTableWizardPage1;
import org.pentaho.di.ui.spoon.wizards.CopyTableWizardPage2;
import org.pentaho.di.ui.trans.dialog.TransDialogPluginType;
import org.pentaho.di.ui.trans.dialog.TransHopDialog;
import org.pentaho.di.ui.trans.dialog.TransLoadProgressDialog;
import org.pentaho.di.ui.util.EngineMetaUtils;
import org.pentaho.di.ui.util.EnvironmentUtils;
import org.pentaho.di.ui.util.HelpUtils;
import org.pentaho.di.ui.util.ThreadGuiResources;
import org.pentaho.di.ui.xul.KettleWaitBox;
import org.pentaho.di.ui.xul.KettleXulLoader;
import org.pentaho.di.version.BuildVersion;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulEventSource;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.WaitBoxRunnable;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.components.XulMenuseparator;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.containers.XulMenupopup;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.jface.tags.ApplicationWindowLocal;
import org.pentaho.ui.xul.jface.tags.JfaceMenuitem;
import org.pentaho.ui.xul.jface.tags.JfaceMenupopup;
import org.pentaho.ui.xul.swt.tags.SwtDeck;
import org.pentaho.vfs.ui.VfsFileChooserDialog;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabListener;
import org.pentaho.xul.swt.tab.TabSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * This class handles the main window of the Spoon graphical transformation editor.
 *
 * @author Matt
 * @since 16-may-2003, i18n at 07-Feb-2006, redesign 01-Dec-2006
 */
public class Spoon extends ApplicationWindow implements AddUndoPositionInterface, TabListener, SpoonInterface,
  OverwritePrompter, PDIObserver, LifeEventHandler, XulEventSource, XulEventHandler, PartitionSchemasProvider {
  private static final String userHomeDir = System.getProperty( "user.home" );
  /**
   * Separate VFS connection name variable is no longer needed.
   * @deprecated
   * The connection name is in the URI since full {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME } paths are being used.
   */
  @Deprecated
  public static final String CONNECTION = "connection";
  private static final String XML_EXTENSION = "xml";
  public static final String SPOON_DIALOG_PROMPT_OVERWRITE_FILE = "Spoon.Dialog.PromptOverwriteFile.";
  public static final String MESSAGE = ".Message";
  public static final String SPOON_DIALOG_PROMPT_OVERWRITE_FILE_TITLE = "Spoon.Dialog.PromptOverwriteFile.Title";
  private static Class<?> PKG = Spoon.class;

  public static final LoggingObjectInterface loggingObject = new SimpleLoggingObject( "Spoon", LoggingObjectType.SPOON,
      null );

  public static final String STRING_TRANSFORMATIONS = BaseMessages.getString( PKG, "Spoon.STRING_TRANSFORMATIONS" );

  public static final String STRING_JOBS = BaseMessages.getString( PKG, "Spoon.STRING_JOBS" );

  public static final String STRING_BUILDING_BLOCKS = BaseMessages.getString( PKG, "Spoon.STRING_BUILDING_BLOCKS" );

  public static final String STRING_ELEMENTS = BaseMessages.getString( PKG, "Spoon.STRING_ELEMENTS" );

  public static final String STRING_CONNECTIONS = BaseMessages.getString( PKG, "Spoon.STRING_CONNECTIONS" );

  public static final String STRING_STEPS = BaseMessages.getString( PKG, "Spoon.STRING_STEPS" );

  public static final String STRING_JOB_ENTRIES = BaseMessages.getString( PKG, "Spoon.STRING_JOB_ENTRIES" );

  public static final String STRING_HOPS = BaseMessages.getString( PKG, "Spoon.STRING_HOPS" );

  public static final String STRING_PARTITIONS = BaseMessages.getString( PKG, "Spoon.STRING_PARTITIONS" );

  public static final String STRING_SLAVES = BaseMessages.getString( PKG, "Spoon.STRING_SLAVES" );

  public static final String STRING_CLUSTERS = BaseMessages.getString( PKG, "Spoon.STRING_CLUSTERS" );

  public static final String STRING_TRANS_BASE = BaseMessages.getString( PKG, "Spoon.STRING_BASE" );

  public static final String STRING_HISTORY = BaseMessages.getString( PKG, "Spoon.STRING_HISTORY" );

  public static final String STRING_TRANS_NO_NAME = BaseMessages.getString( PKG, "Spoon.STRING_TRANS_NO_NAME" );

  public static final String STRING_JOB_NO_NAME = BaseMessages.getString( PKG, "Spoon.STRING_JOB_NO_NAME" );

  public static final String STRING_TRANSFORMATION = BaseMessages.getString( PKG, "Spoon.STRING_TRANSFORMATION" );

  public static final String STRING_JOB = BaseMessages.getString( PKG, "Spoon.STRING_JOB" );

  private static final String SYNC_TRANS = "sync_trans_name_to_file_name";

  public static final String APP_NAME = BaseMessages.getString( PKG, "Spoon.Application.Name" );

  private static final String STRING_SPOON_MAIN_TREE = BaseMessages.getString( PKG, "Spoon.MainTree.Label" );

  private static final String STRING_SPOON_CORE_OBJECTS_TREE = BaseMessages
      .getString( PKG, "Spoon.CoreObjectsTree.Label" );

  public static final String XML_TAG_TRANSFORMATION_STEPS = "transformation-steps";

  public static final String XML_TAG_JOB_JOB_ENTRIES = "job-jobentries";

  private static final String XML_TAG_STEPS = "steps";

  public static final int MESSAGE_DIALOG_WITH_TOGGLE_YES_BUTTON_ID = 256;

  public static final int MESSAGE_DIALOG_WITH_TOGGLE_NO_BUTTON_ID = 257;

  public static final int MESSAGE_DIALOG_WITH_TOGGLE_CUSTOM_DISTRIBUTION_BUTTON_ID = 258;

  private static Spoon staticSpoon;

  private static LogChannelInterface log;

  private Display display;

  private Shell shell;

  private static Splash splash;

  private static FileLoggingEventListener fileLoggingEventListener;

  private boolean destroy;

  private boolean hasFatalError = false;

  private SashForm sashform;

  public TabSet tabfolder;

  private Composite viewTreeComposite;
  private Composite designTreeComposite;
  private TreeToolbar viewTreeToolbar;
  private TreeToolbar designTreeToolbar;

  // THE HANDLERS
  public SpoonDelegates delegates = new SpoonDelegates( this );

  private SharedObjectSyncUtil sharedObjectSyncUtil = new SharedObjectSyncUtil( this );

  public RowMetaAndData variables = new RowMetaAndData( new RowMeta() );

  /**
   * These are the arguments that were given at Spoon launch time...
   */
  private String[] arguments;

  private boolean stopped;

  private Cursor cursor_hourglass, cursor_hand;

  public PropsUI props;

  // should never be null
  private Bowl bowl = DefaultBowl.getInstance();

  public Repository rep;
  private String repositoryName;

  // private RepositorySecurityManager securityManager;

  public RepositoryCapabilities capabilities;

  // Save the last directory saved to for new files
  // TODO: Save the last saved position to the defaultSaveLocation
  private RepositoryDirectoryInterface defaultSaveLocation = null;

  // Associate the defaultSaveLocation with a given repository; We should clear this out on a repo change
  private Repository defaultSaveLocationRepository = null;

  private CTabItem view, design;

  private CTabFolder tabFolder;

  private org.eclipse.swt.widgets.Menu fileMenus;

  private static final String APP_TITLE = APP_NAME;

  private static final String STRING_WELCOME_TAB_NAME = BaseMessages.getString( PKG, "Spoon.Title.STRING_WELCOME" );

  private static final String STRING_DOCUMENT_TAB_NAME = BaseMessages.getString( PKG, "Spoon.Documentation" );

  // "docs/English/welcome/index.html";
  private static final String FILE_WELCOME_PAGE = Const
      .safeAppendDirectory( BasePropertyHandler.getProperty( "documentationDirBase", "docs/" ),
          BaseMessages.getString( PKG, "Spoon.Title.STRING_DOCUMENT_WELCOME" ) );

  private static final String FILE_WELCOME_PAGE_NO_OSGI = Const
          .safeAppendDirectory( BasePropertyHandler.getProperty( "documentationDirBase", "docs/" ),
                  BaseMessages.getString( PKG, "Spoon.Title.STRING_DOCUMENT_WELCOME_NO_OSGI" ) );

  private static final String SYSTEM_FOLDER = Const
          .safeAppendDirectory( BasePropertyHandler.getProperty( "systemDirBase", "system/" ), "" );

  public static final String DOCUMENTATION_URL = Const
      .getDocUrl( BasePropertyHandler.getProperty( "documentationUrl" ) );

  private static final String UNDO_MENU_ITEM = "edit-undo";

  private static final String REDO_MENU_ITEM = "edit-redo";

  // "Undo : not available \tCTRL-Z"
  private static final String UNDO_UNAVAILABLE = BaseMessages.getString( PKG, "Spoon.Menu.Undo.NotAvailable" );

  // "Redo : not available \tCTRL-Y"
  private static final String REDO_UNAVAILABLE = BaseMessages.getString( PKG, "Spoon.Menu.Redo.NotAvailable" );

  private static Boolean unsupportedBrowserEnvironment;

  private static Boolean webkitUnavailable;

  private static String availableBrowser;

  public static final String REFRESH_SELECTION_EXTENSION = "REFRESH_SELECTION_EXTENSION";

  public static final String EDIT_SELECTION_EXTENSION = "EDIT_SELECTION_EXTENSION";

  private static final int MISSING_RECENT_DLG_WIDTH = 465;

  private Composite tabComp;

  private Tree selectionTree;

  private Tree coreObjectsTree;

  private TransExecutionConfiguration transExecutionConfiguration;

  private TransExecutionConfiguration transPreviewExecutionConfiguration;

  private TransExecutionConfiguration transDebugExecutionConfiguration;

  private JobExecutionConfiguration jobExecutionConfiguration;

  // private Menu spoonMenu; // Connections,

  private int coreObjectsState = STATE_CORE_OBJECTS_NONE;

  protected Map<String, FileListener> fileExtensionMap = new HashMap<>();

  private List<Object[]> menuListeners = new ArrayList<>();

  // loads the lifecycle listeners
  private LifecycleSupport lifecycleSupport = new LifecycleSupport();

  private Composite mainComposite;

  private Map<String, String> coreStepToolTipMap;

  private Map<String, String> coreJobToolTipMap;

  private DefaultToolTip toolTip;

  public Map<String, SharedObjects> sharedObjectsFileMap;

  /**
   * We can use this to set a default filter path in the open and save dialogs
   */
  public String lastDirOpened;

  private List<FileListener> fileListeners = new ArrayList<>();

  private XulDomContainer mainSpoonContainer;

  // Menu controllers to modify the main spoon menu
  private List<ISpoonMenuController> menuControllers = new ArrayList<>();

  private XulToolbar mainToolbar;

  private SwtDeck deck;

  public static final String XUL_FILE_MAIN = "ui/spoon.xul";

  private Map<String, XulComponent> menuMap = new HashMap<>();

  private RepositoriesDialog loginDialog;

  private VfsFileChooserDialog vfsFileChooserDialog;

  // the id of the perspective to start in, if any
  protected String startupPerspective = null;

  private CommandLineOption[] commandLineOptions;

  /* package for testing */ Supplier<IMetaStore> metaStoreSupplier = MetaStoreConst.getDefaultMetastoreSupplier();

  private static PrintStream originalSystemOut = System.out;
  private static PrintStream originalSystemErr = System.err;

  private TreeManager selectionTreeManager;

  public Text selectionFilter;

  private TreePaneManager viewTabPanes = new TreePaneManager();

  // licensing related property so that Revenera can properly identify execution is triggered by spoon
  private static final String EXECUTION_TYPE_PROP = "system-property.pentaho.execution.type";

  /**
   * This is the main procedure for Spoon.
   *
   * @param a
   *     Arguments are available in the "Get System Info" step.
   */
  public static void main( String[] a ) throws KettleException {
    boolean doConsoleRedirect = !Boolean.getBoolean( "Spoon.Console.Redirect.Disabled" );
    if ( doConsoleRedirect ) {
      try {
        Path parent = Paths.get( System.getProperty( "user.dir" ) + File.separator + "logs" );
        Files.createDirectories( parent );
        Files.deleteIfExists( Paths.get( parent.toString(), "spoon.log" ) );
        Path path = Files.createFile( Paths.get( parent.toString(), "spoon.log" ) );
        System.setProperty( "LOG_PATH", path.toString() );
        final FileOutputStream fos = new FileOutputStream( path.toFile() );
        System.setOut( new PrintStream( new TeeOutputStream( originalSystemOut, fos ) ) );
        System.setErr( new PrintStream( new TeeOutputStream( originalSystemErr, fos ) ) );
        KettleLogStore.OriginalSystemOut = System.out;
        KettleLogStore.OriginalSystemErr = System.err;
      } catch ( Throwable ignored ) {
        // ignored
      }
    }

    ExecutorService executor = Executors.newCachedThreadPool();
    Future<KettleException> pluginRegistryFuture = executor.submit( new Callable<KettleException>() {

      @Override
      public KettleException call() throws Exception {
        registerUIPluginObjectTypes();

        KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.SPOON );
        try {
          KettleEnvironment.init();
        } catch ( KettleException e ) {
          return e;
        }

        return null;
      }
    } );
    try {
      OsHelper.setAppName();
      // Bootstrap Kettle
      //
      Display display;
      if ( System.getProperties().containsKey( "SLEAK" ) ) {
        DeviceData data = new DeviceData();
        data.tracking = true;
        display = new Display( data );
        Sleak sleak = new Sleak();
        Shell sleakShell = new Shell( display );
        sleakShell.setText( "S-Leak" );
        org.eclipse.swt.graphics.Point size = sleakShell.getSize();
        sleakShell.setSize( size.x / 2, size.y / 2 );
        sleak.create( sleakShell );
        sleakShell.open();
      } else {
        display = new Display();
      }

      // Note: this needs to be done before the look and feel is set
      OsHelper.initOsHandlers( display );

      UIManager.setLookAndFeel( new MetalLookAndFeel() );

      // The core plugin types don't know about UI classes. Add them in now
      // before the PluginRegistry is inited.
      splash = new Splash( display );

      List<String> args = new ArrayList<>( Arrays.asList( a ) );

      CommandLineOption[] commandLineOptions = getCommandLineArgs( args );

      KettleException registryException = pluginRegistryFuture.get();
      if ( registryException != null ) {
        throw registryException;
      }

      PropsUI.init( display, Props.TYPE_PROPERTIES_SPOON );

      KettleLogStore
          .init( PropsUI.getInstance().getMaxNrLinesInLog(), PropsUI.getInstance().getMaxLogLineTimeoutMinutes() );

      initLogging( commandLineOptions );
      // remember...

      // setting property to designer so that Revenera can properly identify execution is triggered by spoon
      System.setProperty( EXECUTION_TYPE_PROP, "designer" );

      staticSpoon = new Spoon();
      staticSpoon.commandLineOptions = commandLineOptions;
      // pull the startup perspective id from the command line options and hand it to Spoon
      String pId;
      StringBuilder perspectiveIdBuff = Spoon.getCommandLineOption( commandLineOptions, "perspective" ).getArgument();
      pId = perspectiveIdBuff.toString();
      if ( !Utils.isEmpty( pId ) ) {
        Spoon.staticSpoon.startupPerspective = pId;
      }
      SpoonFactory.setSpoonInstance( staticSpoon );
      staticSpoon.setDestroy( true );
      GUIFactory.setThreadDialogs( new ThreadGuiResources() );

      staticSpoon.setArguments( args.toArray( new String[ args.size() ] ) );
      staticSpoon.start();

    } catch ( Throwable t ) {
      // avoid calls to Messages i18n method getString() in this block
      // We do this to (hopefully) also catch Out of Memory Exceptions
      //
      t.printStackTrace();
      if ( staticSpoon != null ) {
        log.logError( "Fatal error : " + Const.NVL( t.toString(), Const.NVL( t.getMessage(), "Unknown error" ) ) );
        log.logError( Const.getStackTracker( t ) );
      }
    }

    // Kill all remaining things in this VM!
    System.exit( 0 );
  }

  private static void initLogging( CommandLineOption[] options ) throws KettleException {
    StringBuilder optionLogFile = getCommandLineOption( options, "logfile" ).getArgument();
    StringBuilder optionLogLevel = getCommandLineOption( options, "level" ).getArgument();

    // Set default Locale:
    Locale.setDefault( LanguageChoice.getInstance().getDefaultLocale() );

    if ( !Utils.isEmpty( optionLogFile ) ) {
      fileLoggingEventListener = new FileLoggingEventListener( optionLogFile.toString(), true );
      if ( log.isBasic() ) {
        String filename = fileLoggingEventListener.getFilename();
        log.logBasic( BaseMessages.getString( PKG, "Spoon.Log.LoggingToFile" ) + filename );
      }
      KettleLogStore.getAppender().addLoggingEventListener( fileLoggingEventListener );
    } else {
      fileLoggingEventListener = null;
    }

    if ( !Utils.isEmpty( optionLogLevel ) ) {
      log.setLogLevel( LogLevel.getLogLevelForCode( optionLogLevel.toString() ) );
      if ( log.isBasic() ) {
        // "Logging is at level : "
        log.logBasic( BaseMessages.getString( PKG, "Spoon.Log.LoggingAtLevel" ) + log.getLogLevel().getDescription() );
      }
    }
  }

  public Spoon() {
    this( null );
  }

  public Spoon( Repository rep ) {
    super( null );
    this.addMenuBar();
    log = new LogChannel( APP_NAME );
    SpoonFactory.setSpoonInstance( this );

    props = PropsUI.getInstance();
    sharedObjectsFileMap = new Hashtable<>();
    // sharedObjectSyncUtil = new SharedObjectSyncUtil( delegates, sharedObjectsFileMap );
    setRepository( rep );

    Thread uiThread = Thread.currentThread();

    display = Display.findDisplay( uiThread );

    staticSpoon = this;

    try {
      JndiUtil.initJNDI();
    } catch ( Exception e ) {
      new ErrorDialog( shell, "Unable to init simple JNDI", "Unable to init simple JNDI", e );
    }

  }

  /**
   * The core plugin types don't know about UI classes. This method adds those in before initialization.
   *
   * TODO: create a SpoonLifecycle listener that can notify interested parties of a pre-initialization state so this can
   * happen in those listeners.
   */
  private static void registerUIPluginObjectTypes() {
    RepositoryPluginType repositoryPluginType = RepositoryPluginType.getInstance();
    repositoryPluginType.addObjectType( RepositoryRevisionBrowserDialogInterface.class, "version-browser-classname" );
    repositoryPluginType.addObjectType( RepositoryDialogInterface.class, "dialog-classname" );

    PluginRegistry.addPluginType( SpoonPluginType.getInstance() );

    SpoonPluginType.getInstance().getPluginFolders().add( new PluginFolder( "plugins/repositories", false, true ) );

    LifecyclePluginType.getInstance().getPluginFolders().add( new PluginFolder( "plugins/spoon", false, true ) );
    LifecyclePluginType.getInstance().getPluginFolders().add( new PluginFolder( "plugins/repositories", false, true ) );

    PluginRegistry.addPluginType( JobDialogPluginType.getInstance() );
    PluginRegistry.addPluginType( TransDialogPluginType.getInstance() );
  }

  public void init( TransMeta ti ) {
    FormLayout layout = new FormLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    shell.setLayout( layout );

    addFileListener( new TransFileListener() );

    addFileListener( new JobFileListener() );

    // INIT Data structure
    if ( ti != null ) {
      delegates.trans.addTransformation( ti );
    }

    // Load settings in the props
    loadSettings();

    transExecutionConfiguration = new TransExecutionConfiguration();
    transExecutionConfiguration.setGatheringMetrics( true );
    transPreviewExecutionConfiguration = new TransExecutionConfiguration();
    transPreviewExecutionConfiguration.setGatheringMetrics( true );
    transDebugExecutionConfiguration = new TransExecutionConfiguration();
    transDebugExecutionConfiguration.setGatheringMetrics( true );

    jobExecutionConfiguration = new JobExecutionConfiguration();
    jobExecutionConfiguration.setGatheringMetrics( true );

    // Clean out every time we start, auto-loading etc, is not a good idea
    // If they are needed that often, set them in the kettle.properties file
    //
    variables = new RowMetaAndData( new RowMeta() );

    // props.setLook(shell);
    shell.setImage( GUIResource.getInstance().getImageSpoon() );

    cursor_hourglass = new Cursor( display, SWT.CURSOR_WAIT );
    cursor_hand = new Cursor( display, SWT.CURSOR_HAND );

    Composite sashComposite = null;
    MainSpoonPerspective mainPerspective = null;
    try {
      KettleXulLoader xulLoader = new KettleXulLoader();
      xulLoader.setIconsSize( 16, 16 );
      xulLoader.setOuterContext( shell );
      xulLoader.setSettingsManager( XulSpoonSettingsManager.getInstance() );

      ApplicationWindowLocal.setApplicationWindow( this );

      mainSpoonContainer = xulLoader.loadXul( XUL_FILE_MAIN, new XulSpoonResourceBundle() );

      BindingFactory bf = new DefaultBindingFactory();
      bf.setDocument( mainSpoonContainer.getDocumentRoot() );
      mainSpoonContainer.addEventHandler( this );
      /* menuBar = (XulMenubar) */
      mainSpoonContainer.getDocumentRoot().getElementById( "spoon-menubar" );
      mainToolbar = (XulToolbar) mainSpoonContainer.getDocumentRoot().getElementById( "main-toolbar" );
      props.setLook( (Control) mainToolbar.getManagedObject(), Props.WIDGET_STYLE_TOOLBAR );

      /* canvas = (XulVbox) */
      mainSpoonContainer.getDocumentRoot().getElementById( "trans-job-canvas" );
      deck = (SwtDeck) mainSpoonContainer.getDocumentRoot().getElementById( "canvas-deck" );

      sashComposite = new Composite( shell, SWT.None );

      mainPerspective = new MainSpoonPerspective( sashComposite, tabfolder );
      if ( startupPerspective == null ) {
        startupPerspective = mainPerspective.getId();
      }

      SpoonPerspectiveManager.getInstance().setStartupPerspective( startupPerspective );
      SpoonPerspectiveManager.getInstance().addPerspective( mainPerspective );

      SpoonPluginManager.getInstance().applyPluginsForContainer( "spoon", mainSpoonContainer );

      SpoonPerspectiveManager.getInstance().setDeck( deck );
      SpoonPerspectiveManager.getInstance().setXulDoc( mainSpoonContainer );
      SpoonPerspectiveManager.getInstance().initialize();
    } catch ( Exception e ) {
      LogChannel.GENERAL.logError( "Error initializing transformation", e );
    }
    // addBar();

    // Set the shell size, based upon previous time...
    WindowProperty windowProperty = props.getScreen( APP_TITLE );
    if ( windowProperty != null ) {
      windowProperty.setShell( shell );
    } else {
      shell.pack();
      shell.setMaximized( true ); // Default = maximized!
    }

    layout = new FormLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;

    GridData data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.grabExcessVerticalSpace = true;
    data.verticalAlignment = SWT.FILL;
    data.horizontalAlignment = SWT.FILL;
    sashComposite.setLayoutData( data );

    sashComposite.setLayout( layout );

    sashform = new SashForm( sashComposite, SWT.HORIZONTAL );

    FormData fdSash = new FormData();
    fdSash.left = new FormAttachment( 0, 0 );
    // fdSash.top = new FormAttachment((org.eclipse.swt.widgets.ToolBar)
    // toolbar.getNativeObject(), 0);
    fdSash.top = new FormAttachment( 0, 0 );
    fdSash.bottom = new FormAttachment( 100, 0 );
    fdSash.right = new FormAttachment( 100, 0 );
    sashform.setLayoutData( fdSash );

    createPopupMenus();
    addTree();
    addTabs();
    mainPerspective.setTabset( this.tabfolder );
    ( (Composite) deck.getManagedObject() ).layout( true, true );

    SpoonPluginManager.getInstance().notifyLifecycleListeners( SpoonLifeCycleEvent.STARTUP );

    // Add a browser widget
    if ( props.showWelcomePageOnStartup() ) {
      showWelcomePage();
    }

    // Allow data to be copied or moved to the drop target
    int operations = DND.DROP_COPY | DND.DROP_DEFAULT;
    DropTarget target = new DropTarget( shell, operations );

    // Receive data in File format
    final FileTransfer fileTransfer = FileTransfer.getInstance();
    Transfer[] types = new Transfer[] { fileTransfer };
    target.setTransfer( types );

    target.addDropListener( new DropTargetListener() {
      @Override
      public void dragEnter( DropTargetEvent event ) {
        if ( event.detail == DND.DROP_DEFAULT ) {
          if ( ( event.operations & DND.DROP_COPY ) != 0 ) {
            event.detail = DND.DROP_COPY;
          } else {
            event.detail = DND.DROP_NONE;
          }
        }
      }

      @Override
      public void dragOver( DropTargetEvent event ) {
        event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
      }

      @Override
      public void dragOperationChanged( DropTargetEvent event ) {
        if ( event.detail == DND.DROP_DEFAULT ) {
          if ( ( event.operations & DND.DROP_COPY ) != 0 ) {
            event.detail = DND.DROP_COPY;
          } else {
            event.detail = DND.DROP_NONE;
          }
        }
      }

      @Override
      public void dragLeave( DropTargetEvent event ) {
      }

      @Override
      public void dropAccept( DropTargetEvent event ) {
      }

      @Override
      public void drop( DropTargetEvent event ) {
        if ( fileTransfer.isSupportedType( event.currentDataType ) ) {
          String[] files = (String[]) event.data;
          for ( String file : files ) {
            openFile( file, false );
          }
        }
      }
    } );

    // listen for steps being added or removed
    PluginRegistry.getInstance().addPluginListener( StepPluginType.class, new PluginTypeListener() {
      @Override
      public void pluginAdded( Object serviceObject ) {
        previousShowTrans = false; // hack to get the tree to reload
        Display.getDefault().asyncExec( new Runnable() {
          @Override
          public void run() {
            refreshCoreObjects();
          }
        } );
      }

      @Override
      public void pluginRemoved( Object serviceObject ) {
        previousShowTrans = false; // hack to get the tree to reload
        Display.getDefault().asyncExec( new Runnable() {
          @Override
          public void run() {
            refreshCoreObjects();
          }
        } );
      }

      @Override
      public void pluginChanged( Object serviceObject ) {
      }
    } );
  }

  public XulDomContainer getMainSpoonContainer() {
    return mainSpoonContainer;
  }

  public void loadPerspective( String id ) {
    List<SpoonPerspective> perspectives = SpoonPerspectiveManager.getInstance().getPerspectives();
    for ( int pos = 0; pos < perspectives.size(); pos++ ) {
      SpoonPerspective perspective = perspectives.get( pos );
      if ( perspective.getId().equals( id ) ) {
        loadPerspective( pos );
        return;
      }
    }
  }

  public void loadPerspective( int pos ) {
    try {
      SpoonPerspectiveManager.getInstance().activatePerspective(
          SpoonPerspectiveManager.getInstance().getPerspectives().get( pos ).getClass() );
    } catch ( KettleException e ) {
      log.logError( "Error loading perspective", e );
    }
  }

  public static Spoon getInstance() {
    return staticSpoon;
  }

  public VfsFileChooserDialog getVfsFileChooserDialog( FileObject rootFile, FileObject initialFile ) {
    if ( vfsFileChooserDialog == null ) {
      vfsFileChooserDialog = new VfsFileChooserDialog( shell, new KettleVfsDelegatingResolver(), rootFile, initialFile );
    }
    vfsFileChooserDialog.setRootFile( rootFile );
    vfsFileChooserDialog.setInitialFile( initialFile );
    return vfsFileChooserDialog;
  }

  public boolean closeFile() {
    return closeFile( false );
  }

  public boolean closeFile( boolean force ) {
    boolean closed = true;
    EngineMetaInterface meta = getActiveMeta();
    if ( meta != null ) {
      // If a transformation or job is the current active tab, close it
      closed = tabCloseSelected( force );
    }

    return closed;
  }

  public boolean closeAllFiles() {
    return closeAllFiles( false );
  }

  public boolean closeAllFiles( boolean force ) {
    int numTabs = delegates.tabs.getTabs().size();
    for ( int i = numTabs - 1; i >= 0; i-- ) {
      tabfolder.setSelected( i );
      if ( !closeFile( force ) ) {
        return false; // A single cancel aborts the rest of the operation
      }
    }

    return true;
  }

  public boolean closeAllJobsAndTransformations() {
    return closeAllJobsAndTransformations( false );
  }

  /**
   * Prompt user to close all open Jobs & Transformations if they have execute permissions.
   * If they don't have execute permission then warn user if they really want to disconnect
   * from repository.  If yes, close all tabs.
   *
   * @return If user agrees with closing of tabs then return true so we can disconnect from the repo.
   */
  public boolean closeAllJobsAndTransformations( boolean force ) {
    // Check to see if there are any open jobs/trans.  If there are not any then we don't need to close anything.
    // Keep in mind that the 'Welcome' tab can be active.
    final List<TransMeta> transList = delegates.trans.getTransformationList();
    final List<JobMeta> jobList = delegates.jobs.getJobList();
    if ( ( transList.size() == 0 ) && ( jobList.size() == 0 ) ) {
      return true;
    }

    boolean createPerms = !RepositorySecurityUI
        .verifyOperations( shell, rep, false, RepositoryOperation.MODIFY_TRANSFORMATION,
            RepositoryOperation.MODIFY_JOB );
    boolean executePerms = !RepositorySecurityUI
        .verifyOperations( shell, rep, false, RepositoryOperation.EXECUTE_TRANSFORMATION,
            RepositoryOperation.EXECUTE_JOB );
    boolean readPerms = !RepositorySecurityUI
        .verifyOperations( shell, rep, false, RepositoryOperation.READ_TRANSFORMATION, RepositoryOperation.READ_JOB );

    // Check to see if display of warning dialog has been disabled
    String warningTitle = BaseMessages.getString( PKG, "Spoon.Dialog.WarnToCloseAllForce.Disconnect.Title" );
    String warningText = BaseMessages.getString( PKG, "Spoon.Dialog.WarnToCloseAllForce.Disconnect.Message" );
    int buttons = SWT.OK;
    if ( readPerms && createPerms && executePerms ) {
      warningTitle = BaseMessages.getString( PKG, "Spoon.Dialog.WarnToCloseAllOption.Disconnect.Title" );
      warningText = BaseMessages.getString( PKG, "Spoon.Dialog.WarnToCloseAllOption.Disconnect.Message" );
      buttons = SWT.YES | SWT.NO;
    }

    MessageBox mb = new MessageBox( Spoon.getInstance().getShell(), buttons | SWT.ICON_WARNING );
    mb.setMessage( warningText );
    mb.setText( warningTitle );

    final int isCloseAllFiles = mb.open();
    if ( ( isCloseAllFiles == SWT.YES ) || ( isCloseAllFiles == SWT.OK ) ) {
      // Yes - User specified that they want to close all.
      return closeAllFiles( force );
    } else if ( ( isCloseAllFiles == SWT.NO ) && ( executePerms ) ) {
      // No - don't close tabs
      // if user has execute permissions mark tabs for save
      markTabsChanged( force );
      // Return true so we can disconnect from repo
      return true;
    } else {
      // Cancel - don't close tabs and don't disconnect from repo
      return false;
    }
  }

  /*
   Fix for PDI-18593 Hadoop cluster items not refreshed when switching from local<->repo.
   Updates the tree on repository connection or disconnection without closing active Jobs or Transformations.
   */
  public void updateTreeForActiveAbstractMetas() {
    for ( TabMapEntry entry : delegates.tabs.getTabs() ) {
      Object managedObject = entry.getObject().getManagedObject();
      if ( managedObject instanceof AbstractMeta ) {
        if ( managedObject instanceof TransMeta ) {
          selectionTreeManager.create( (AbstractMeta) managedObject, STRING_TRANSFORMATIONS, props.isOnlyActiveFileShownInTree() );
        }
        if ( managedObject instanceof JobMeta ) {
          selectionTreeManager.create( (AbstractMeta) managedObject, STRING_JOBS, props.isOnlyActiveFileShownInTree() );
        }
        selectionTreeManager.show( (AbstractMeta) managedObject );
        refreshTree( (AbstractMeta) managedObject );
      }
    }
  }


  /**
   * PDI-18997 - clear specific repository directory when connect and or disconnect from server
   */
  public void clearRepositoryDirectory() {
    for ( TabMapEntry entry : delegates.tabs.getTabs() ) {
      Object managedObject = entry.getObject().getManagedObject();
      if ( managedObject instanceof AbstractMeta ) {
        AbstractMeta abstractMeta = (AbstractMeta) managedObject;
        abstractMeta.setRepositoryDirectory( new RepositoryDirectory() );
        abstractMeta.setFilename( null );
      }
    }
  }

  /**
   * Search the transformation meta-data.
   *
   */
  public void searchMetaData() {
    TransMeta[] transMetas = getLoadedTransformations();
    JobMeta[] jobMetas = getLoadedJobs();
    if ( ( transMetas == null || transMetas.length == 0 ) && ( jobMetas == null || jobMetas.length == 0 ) ) {
      return;
    }

    EnterSearchDialog esd = new EnterSearchDialog( shell );
    if ( !esd.open() ) {
      return;
    }

    List<Object[]> rows = new ArrayList<>();

    for ( TransMeta transMeta : transMetas ) {
      String filter = esd.getFilterString();
      if ( filter != null ) {
        filter = filter.toUpperCase();
      }

      List<StringSearchResult> stringList =
        transMeta.getStringList( esd.isSearchingSteps(), esd.isSearchingDatabases(), esd.isSearchingNotes() );
      for ( StringSearchResult result : stringList ) {
        boolean add = Utils.isEmpty( filter );
        if ( filter != null && result.getString().toUpperCase().contains( filter ) ) {
          add = true;
        }
        if ( filter != null && result.getFieldName().toUpperCase().contains( filter ) ) {
          add = true;
        }
        if ( filter != null && result.getParentObject().toString().toUpperCase().contains( filter ) ) {
          add = true;
        }
        if ( filter != null && result.getGrandParentObject().toString().toUpperCase().contains( filter ) ) {
          add = true;
        }

        if ( add ) {
          rows.add( result.toRow() );
        }
      }
    }

    for ( JobMeta jobMeta : jobMetas ) {
      String filter = esd.getFilterString();
      if ( filter != null ) {
        filter = filter.toUpperCase();
      }

      List<StringSearchResult> stringList =
        jobMeta.getStringList( esd.isSearchingSteps(), esd.isSearchingDatabases(), esd.isSearchingNotes() );
      for ( StringSearchResult result : stringList ) {
        boolean add = Utils.isEmpty( filter );
        if ( filter != null && result.getString().toUpperCase().contains( filter ) ) {
          add = true;
        }
        if ( filter != null && result.getFieldName().toUpperCase().contains( filter ) ) {
          add = true;
        }
        if ( filter != null && result.getParentObject().toString().toUpperCase().contains( filter ) ) {
          add = true;
        }
        if ( filter != null && result.getGrandParentObject().toString().toUpperCase().contains( filter ) ) {
          add = true;
        }

        if ( add ) {
          rows.add( result.toRow() );
        }
      }
    }

    if ( rows.size() != 0 ) {
      PreviewRowsDialog prd =
        new PreviewRowsDialog( shell, Variables.getADefaultVariableSpace(), SWT.NONE, BaseMessages.getString(
          PKG, "Spoon.StringSearchResult.Subtitle" ), StringSearchResult.getResultRowMeta(), rows );
      String title = BaseMessages.getString( PKG, "Spoon.StringSearchResult.Title" );
      String message = BaseMessages.getString( PKG, "Spoon.StringSearchResult.Message" );
      prd.setTitleMessage( title, message );
      prd.open();
    } else {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
      mb.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.NothingFound.Message" ) );
      mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.NothingFound.Title" ) ); // Sorry!
      mb.open();
    }
  }

  public void showArguments() {

    RowMetaAndData allArgs = new RowMetaAndData();

    for ( int ii = 0; ii < arguments.length; ++ii ) {
      allArgs.addValue( new ValueMetaString(
        Props.STRING_ARGUMENT_NAME_PREFIX + ( 1 + ii ) ), arguments[ii] );
    }

    // Now ask the use for more info on these!
    EnterStringsDialog esd = new EnterStringsDialog( shell, SWT.NONE, allArgs );
    esd.setTitle( BaseMessages.getString( PKG, "Spoon.Dialog.ShowArguments.Title" ) );
    esd.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.ShowArguments.Message" ) );
    esd.setReadOnly( true );
    esd.setShellImage( GUIResource.getInstance().getImageLogoSmall() );
    esd.open();
  }

  private void fillVariables( RowMetaAndData vars ) {
    TransMeta[] transMetas = getLoadedTransformations();
    JobMeta[] jobMetas = getLoadedJobs();
    if ( ( transMetas == null || transMetas.length == 0 ) && ( jobMetas == null || jobMetas.length == 0 ) ) {
      return;
    }

    Properties sp = new Properties();
    sp.putAll( System.getProperties() );

    VariableSpace space = Variables.getADefaultVariableSpace();
    String[] keys = space.listVariables();
    for ( String key : keys ) {
      sp.put( key, space.getVariable( key ) );
    }

    for ( TransMeta transMeta : transMetas ) {
      List<String> list = transMeta.getUsedVariables();
      for ( String varName : list ) {
        String varValue = sp.getProperty( varName, "" );
        if ( vars.getRowMeta().indexOfValue( varName ) < 0 && !varName.startsWith( Const.INTERNAL_VARIABLE_PREFIX ) ) {
          vars.addValue( new ValueMetaString( varName ), varValue );
        }
      }
    }

    for ( JobMeta jobMeta : jobMetas ) {
      List<String> list = jobMeta.getUsedVariables();
      for ( String varName : list ) {
        String varValue = sp.getProperty( varName, "" );
        if ( vars.getRowMeta().indexOfValue( varName ) < 0 && !varName.startsWith( Const.INTERNAL_VARIABLE_PREFIX ) ) {
          vars.addValue( new ValueMetaString( varName ), varValue );
        }
      }
    }
  }

  public void setVariables() {
    fillVariables( variables );

    // Now ask the use for more info on these!
    EnterStringsDialog esd = new EnterStringsDialog( shell, SWT.NONE, variables );
    esd.setTitle( BaseMessages.getString( PKG, "Spoon.Dialog.SetVariables.Title" ) );
    esd.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.SetVariables.Message" ) );
    esd.setReadOnly( false );
    esd.setShellImage( GUIResource.getInstance().getImageVariable() );
    if ( esd.open() != null ) {
      applyVariables();
    }
  }

  public void applyVariables() {
    for ( int i = 0; i < variables.size(); i++ ) {
      try {
        String name = variables.getValueMeta( i ).getName();
        String value = variables.getString( i, "" );

        applyVariableToAllLoadedObjects( name, value );
      } catch ( KettleValueException e ) {
        // Just eat the exception. getString() should never give an
        // exception.
        log.logDebug( "Unexpected exception occurred : " + e.getMessage() );
      }
    }
  }

  public void applyVariableToAllLoadedObjects( String name, String value ) {
    // We want to insert the variables into all loaded jobs and
    // transformations
    //
    for ( TransMeta transMeta : getLoadedTransformations() ) {
      transMeta.setVariable( name, Const.NVL( value, "" ) );
    }
    for ( JobMeta jobMeta : getLoadedJobs() ) {
      jobMeta.setVariable( name, Const.NVL( value, "" ) );
    }

    // Not only that, we also want to set the variables in the
    // execution configurations...
    //
    transExecutionConfiguration.getVariables().put( name, value );
    jobExecutionConfiguration.getVariables().put( name, value );
    transDebugExecutionConfiguration.getVariables().put( name, value );
  }

  public void showVariables() {
    fillVariables( variables );

    // Now ask the use for more info on these!
    EnterStringsDialog esd = new EnterStringsDialog( shell, SWT.NONE, variables );
    esd.setTitle( BaseMessages.getString( PKG, "Spoon.Dialog.ShowVariables.Title" ) );
    esd.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.ShowVariables.Message" ) );
    esd.setReadOnly( true );
    esd.setShellImage( GUIResource.getInstance().getImageVariable() );
    esd.open();
  }

  public void openSpoon() {
    shell = getShell();
    shell.setText( APP_TITLE );
    mainComposite.setRedraw( true );
    mainComposite.setVisible( false );
    mainComposite.setVisible( true );
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
   *          True to stop this application.
   */
  public void setStopped( boolean stopped ) {
    this.stopped = stopped;
  }

  /**
   * @param destroy
   *          Whether or not to destroy the display.
   */
  public void setDestroy( boolean destroy ) {
    this.destroy = destroy;
  }

  /**
   * @return Returns whether or not we should destroy the display.
   */
  public boolean doDestroy() {
    return destroy;
  }

  /**
   * @param arguments
   *          The arguments to set.
   */
  public void setArguments( String[] arguments ) {
    this.arguments = arguments;
  }

  /**
   * @return Returns the arguments.
   */
  public String[] getArguments() {
    return arguments;
  }

  public synchronized void dispose() {
    setStopped( true );
    cursor_hand.dispose();
    cursor_hourglass.dispose();

    if ( destroy && ( display != null ) && !display.isDisposed() ) {
      try {
        display.dispose();
      } catch ( SWTException e ) {
        // dispose errors
      } catch ( NullPointerException  e ) {
        // fixes NPE on Mac OS
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
    undoAction( getActiveUndoInterface() );
  }

  public void redoAction() {
    redoAction( getActiveUndoInterface() );
  }

  /**
   * It's called copySteps, but the job entries also arrive at this location
   */
  public void copySteps() {
    TransMeta transMeta = getActiveTransformation();
    if ( transMeta != null ) {
      copySelected( transMeta, transMeta.getSelectedSteps(), transMeta.getSelectedNotes() );
    }
    JobMeta jobMeta = getActiveJob();
    if ( jobMeta != null ) {
      copyJobentries();
    }
  }

  public void copyJobentries() {
    JobMeta jobMeta = getActiveJob();
    if ( jobMeta != null ) {
      if ( RepositorySecurityUI.verifyOperations( shell, rep,
          RepositoryOperation.MODIFY_JOB, RepositoryOperation.EXECUTE_JOB ) ) {
        return;
      }

      delegates.jobs.copyJobEntries( jobMeta.getSelectedEntries() );
    }
  }

  public void copy() {
    TransMeta transMeta = getActiveTransformation();
    JobMeta jobMeta = getActiveJob();
    boolean transActive = transMeta != null;
    boolean jobActive = jobMeta != null;
    Control focusControl = getDisplay().getFocusControl();

    if ( focusControl instanceof StyledText ) {
      copyLogSelectedText( (StyledText) focusControl );
    } else {
      if ( transActive ) {
        if ( transMeta.getSelectedSteps().size() > 0 ) {
          copySteps();
        } else {
          copyTransformation();
        }
      } else if ( jobActive ) {
        if ( jobMeta.getSelectedEntries().size() > 0 ) {
          copyJobentries();
        } else {
          copyJob();
        }
      }
    }
  }

  public void copyFile() {
    TransMeta transMeta = getActiveTransformation();
    JobMeta jobMeta = getActiveJob();
    boolean transActive = transMeta != null;
    boolean jobActive = jobMeta != null;

    if ( transActive ) {
      copyTransformation();
    } else if ( jobActive ) {
      copyJob();
    }
  }

  public void cut() {
    TransMeta transMeta = getActiveTransformation();
    JobMeta jobMeta = getActiveJob();
    boolean transActive = transMeta != null;
    boolean jobActive = jobMeta != null;

    if ( transActive ) {
      List<StepMeta> stepMetas = transMeta.getSelectedSteps();
      if ( stepMetas != null && stepMetas.size() > 0 ) {
        copySteps();
        delSteps( transMeta, stepMetas.toArray( new StepMeta[stepMetas.size()] ) );
      }
    } else if ( jobActive ) {
      List<JobEntryCopy> jobEntryCopies = jobMeta.getSelectedEntries();
      if ( jobEntryCopies != null && jobEntryCopies.size() > 0 ) {
        copyJobentries();
        deleteJobEntryCopies( jobMeta, jobEntryCopies.toArray( new JobEntryCopy[jobEntryCopies.size()] ) );
      }
    }
  }

  public void removeMenuItem( String itemid, boolean removeTrailingSeparators ) {
    XulMenuitem item = (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById( itemid );
    if ( item != null ) {
      XulComponent menu = item.getParent();
      item.getParent().removeChild( item );

      if ( removeTrailingSeparators ) {
        List<XulComponent> children = menu.getChildNodes();

        if ( children.size() > 0 ) {
          XulComponent lastMenuItem = children.get( children.size() - 1 );

          if ( lastMenuItem instanceof XulMenuseparator ) {
            menu.removeChild( lastMenuItem );
            // above call should work, but doesn't for some reason, removing separator by force
            // the menu separators seem to not be modeled as individual objects in XUL
            try {
              Menu swtm = (Menu) menu.getManagedObject();
              swtm.getItems()[swtm.getItemCount() - 1].dispose();
            } catch ( Throwable t ) {
              LogChannel.GENERAL.logError( "Error removing XUL menu item", t );
            }
          }
        }

      }

    } else {
      log.logError( "Could not find menu item with id " + itemid + " to remove from Spoon menu" );
    }
  }

  public void disableMenuItem( String itemId ) {
    XulMenuitem item = (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById( itemId );
    item.setDisabled( true );
  }

  public void enableMenuItem( String itemId ) {
    XulMenuitem item = (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById( itemId );
    item.setDisabled( false );
  }

  public void createPopupMenus() {

    try {
      menuMap.put( "trans-class", mainSpoonContainer.getDocumentRoot().getElementById( "trans-class" ) );
      menuMap.put( "trans-class-new", mainSpoonContainer.getDocumentRoot().getElementById( "trans-class-new" ) );
      menuMap.put( "job-class", mainSpoonContainer.getDocumentRoot().getElementById( "job-class" ) );
      menuMap.put( "trans-hop-class", mainSpoonContainer.getDocumentRoot().getElementById( "trans-hop-class" ) );
      menuMap.put( "database-class", mainSpoonContainer.getDocumentRoot().getElementById( "database-class" ) );
      menuMap.put( "partition-schema-class", mainSpoonContainer.getDocumentRoot().getElementById(
        "partition-schema-class" ) );
      menuMap.put( "cluster-schema-class", mainSpoonContainer.getDocumentRoot().getElementById(
        "cluster-schema-class" ) );
      menuMap.put( "slave-cluster-class", mainSpoonContainer.getDocumentRoot().getElementById(
        "slave-cluster-class" ) );
      menuMap.put( "trans-inst", mainSpoonContainer.getDocumentRoot().getElementById( "trans-inst" ) );
      menuMap.put( "job-inst", mainSpoonContainer.getDocumentRoot().getElementById( "job-inst" ) );
      menuMap.put( "step-plugin", mainSpoonContainer.getDocumentRoot().getElementById( "step-plugin" ) );
      menuMap.put( "database-inst", mainSpoonContainer.getDocumentRoot().getElementById( "database-inst" ) );
      menuMap.put( "named-conf-inst", mainSpoonContainer.getDocumentRoot().getElementById( "named-conf-inst" ) );
      menuMap.put( "step-inst", mainSpoonContainer.getDocumentRoot().getElementById( "step-inst" ) );
      menuMap.put( "job-entry-copy-inst", mainSpoonContainer.getDocumentRoot().getElementById(
        "job-entry-copy-inst" ) );
      menuMap.put( "trans-hop-inst", mainSpoonContainer.getDocumentRoot().getElementById( "trans-hop-inst" ) );
      menuMap.put( "partition-schema-inst", mainSpoonContainer.getDocumentRoot().getElementById(
        "partition-schema-inst" ) );
      menuMap.put( "cluster-schema-inst", mainSpoonContainer.getDocumentRoot().getElementById(
        "cluster-schema-inst" ) );
      menuMap
        .put( "slave-server-inst", mainSpoonContainer.getDocumentRoot().getElementById( "slave-server-inst" ) );
    } catch ( Throwable t ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "Spoon.Exception.ErrorReadingXULFile.Title" ), BaseMessages
          .getString( PKG, "Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_MAIN ), new Exception( t ) );
    }

    addMenuLast();
  }

  public void executeTransformation() {
    executeTransformation(
      getActiveTransformation(), true, false, false, false, false, transExecutionConfiguration.getReplayDate(),
      false, transExecutionConfiguration.getLogLevel() );
  }

  public void previewTransformation() {
    executeTransformation(
      getActiveTransformation(), true, false, false, true, false, transDebugExecutionConfiguration
        .getReplayDate(), true, transDebugExecutionConfiguration.getLogLevel() );
  }

  public void debugTransformation() {
    executeTransformation(
      getActiveTransformation(), true, false, false, false, true, transPreviewExecutionConfiguration
        .getReplayDate(), true, transPreviewExecutionConfiguration.getLogLevel() );
  }

  public void checkTrans() {
    checkTrans( getActiveTransformation() );
  }

  public void analyseImpact() {
    analyseImpact( getActiveTransformation() );
  }

  public void showLastImpactAnalyses() {
    showLastImpactAnalyses( getActiveTransformation() );
  }

  public void showLastTransPreview() {
    TransGraph transGraph = getActiveTransGraph();
    if ( transGraph != null ) {
      transGraph.showLastPreviewResults();
    }
  }

  public void showExecutionResults() {
    TransGraph transGraph = getActiveTransGraph();
    if ( transGraph != null ) {
      transGraph.showExecutionResults();
      enableMenus();
    } else {
      JobGraph jobGraph = getActiveJobGraph();
      if ( jobGraph != null ) {
        jobGraph.showExecutionResults();
        enableMenus();
      }
    }
  }

  public boolean isExecutionResultsPaneVisible() {
    TransGraph transGraph = getActiveTransGraph();
    return ( transGraph != null ) && ( transGraph.isExecutionResultsPaneVisible() );
  }

  public void copyLogSelectedText( StyledText text ) {
    toClipboard( text.getSelectionText() );
  }

  public void copyTransformation() {
    copyTransformation( getActiveTransformation() );
  }

  public void copyTransformationImage() {
    copyTransformationImage( getActiveTransformation() );
  }

  public boolean editTransformationProperties() {
    return TransGraph.editProperties( getActiveTransformation(), this, rep, true );
  }

  public boolean editProperties() {
    if ( getActiveTransformation() != null ) {
      return editTransformationProperties();
    } else if ( getActiveJob() != null ) {
      return editJobProperties( "job-settings" );
    }
    // no properties were edited, so no cancel was clicked
    return true;
  }

  public void executeJob() {
    executeJob( getActiveJob(), true, false, null, false, null, 0 );
  }

  public void copyJob() {
    copyJob( getActiveJob() );
  }

  public void showWelcomePage() {
    try {
      LocationListener listener = new LocationListener() {
        @Override
        public void changing( LocationEvent event ) {
          if ( event.location.endsWith( ".pdf" ) ) {
            Program.launch( event.location );
            event.doit = false;
          } else if ( event.location.contains( "samples/transformations" )
            || event.location.contains( "samples/jobs" ) || event.location.contains( "samples/mapping" ) ) {
            try {
              FileObject fileObject = KettleVFS.getFileObject( event.location );
              if ( fileObject.exists() ) {
                if ( event.location.endsWith( ".ktr" ) || event.location.endsWith( ".kjb" ) ) {
                  openFile( event.location, false );
                } else {
                  lastDirOpened = KettleVFS.getFilename( fileObject );
                  openFile( true );
                }
                event.doit = false;
              }
            } catch ( Exception e ) {
              log.logError( "Error handling samples location: " + event.location, e );
            }
          }
        }

        @Override
        public void changed( LocationEvent event ) {
          // System.out.println("Changed to: " + event.location);
        }
      };

      // create callback functions for welcome
      Runnable openFileFunction = new Runnable() {
        public void run() {
          try {
            Spoon.this.openFileNew();
          } catch (Exception e) {
            log.logError( Const.getStackTracker( e ) );
            throw new RuntimeException(e);
          }
        }
      };
      Runnable newTransFunction = new Runnable() {
        public void run() {
          Spoon.this.newTransFile();
        }
      };
      Runnable newJobFunction = new Runnable() {
        public void run() {
          Spoon.this.newJobFile();
        }
      };

      HashMap<String, Runnable> functions = new HashMap<String, Runnable>();
      functions.put( "openFileFunction", openFileFunction );
      functions.put( "newTransFunction", newTransFunction );
      functions.put( "newJobFunction", newJobFunction );

      // see if we are in webstart mode
      String webstartRoot = System.getProperty( "spoon.webstartroot" );
      String fileName = FILE_WELCOME_PAGE_NO_OSGI;
      File osgiFolder = new File( SYSTEM_FOLDER );
      if( osgiFolder.exists() )
        fileName = FILE_WELCOME_PAGE;
      if ( webstartRoot != null ) {
        URL url = new URL( webstartRoot + '/' + fileName );
        // ./docs/English/tips/index.htm
        addSpoonBrowser( STRING_WELCOME_TAB_NAME, url.toString(), true, listener, functions, false );
      } else {
        // see if we can find the welcome file on the file system
        File file = new File( fileName );
        if ( file.exists() ) {
          // ./docs/English/tips/index.htm
          addSpoonBrowser( STRING_WELCOME_TAB_NAME, file.toURI().toURL().toString(), true, listener, functions, false );
        }
      }
    } catch ( MalformedURLException e1 ) {
      log.logError( Const.getStackTracker( e1 ) );
    }
  }

  public void showDocumentMap() {
    try {
      URL url = new URL( DOCUMENTATION_URL );
      HelpUtils.openHelpDialog( shell, STRING_DOCUMENT_TAB_NAME, url.toString() );
    } catch ( MalformedURLException e1 ) {
      log.logError( Const.getStackTracker( e1 ) );
    }
  }

  public void addMenuLast() {
    org.pentaho.ui.xul.dom.Document doc = mainSpoonContainer.getDocumentRoot();
    JfaceMenupopup recentFilesPopup = (JfaceMenupopup) doc.getElementById( "file-open-recent-popup" );

    recentFilesPopup.removeChildren();

    // Previously loaded files...
    List<LastUsedFile> lastUsedFiles = props.getLastUsedFiles();
    for ( int i = 0; i < lastUsedFiles.size(); i++ ) {
      final LastUsedFile lastUsedFile = lastUsedFiles.get( i );

      char chr = (char) ( '1' + i );
      String accessKey = "ctrl-" + chr;
      String accessText = "CTRL-" + chr;
      String text = lastUsedFile.toString();
      String id = "last-file-" + i;

      if ( i > 8 ) {
        accessKey = null;
        accessText = null;
      }

      final String lastFileId = Integer.toString( i );

      Action action = new Action( "open-last-file-" + ( i + 1 ), Action.AS_PUSH_BUTTON ) {
        @Override
        public void run() {
          lastFileSelect( lastFileId );
        }
      };

      // shorten the filename if necessary
      int targetLength = 40;
      if ( text.length() > targetLength ) {
        int lastSep = text.replace( '\\', '/' ).lastIndexOf( '/' );
        if ( lastSep != -1 ) {
          String fileName = "..." + text.substring( lastSep );
          if ( fileName.length() < targetLength ) {
            // add the start of the file path
            int leadSize = targetLength - fileName.length();
            text = text.substring( 0, leadSize ) + fileName;
          } else {
            text = fileName;
          }
        }
      }

      JfaceMenuitem miFileLast = new JfaceMenuitem( null, recentFilesPopup, mainSpoonContainer, text, 0, action );

      miFileLast.setLabel( text );
      miFileLast.setId( id );
      if ( accessText != null && accessKey != null ) {
        miFileLast.setAcceltext( accessText );
        miFileLast.setAccesskey( accessKey );
      }

      if ( lastUsedFile.isTransformation() ) {
        miFileLast.setImage( GUIResource.getInstance().getImageTransGraph() );
      } else if ( lastUsedFile.isJob() ) {
        miFileLast.setImage( GUIResource.getInstance().getImageJobGraph() );
      }
      miFileLast.setCommand( "spoon.lastFileSelect('" + i + "')" );
    }
  }

  public void lastRepoFileSelect( String repo, String id ) {
    int idx = Integer.parseInt( id );
    List<LastUsedFile> lastUsedFiles = props.getLastUsedRepoFiles().getOrDefault( repo, Collections.emptyList() );
    lastFileSelect( lastUsedFiles.get( idx ) );
  }

  public void lastFileSelect( String id ) {
    int idx = Integer.parseInt( id );
    List<LastUsedFile> lastUsedFiles = props.getLastUsedFiles();
    lastFileSelect( lastUsedFiles.get( idx ) );
  }

  public void lastFileSelect( final LastUsedFile lastUsedFile ) {

    // If the file comes from a repository and it's not the same as
    // the one we're connected to, ask for a username/password!
    //
    if ( lastUsedFile.isSourceRepository()
      && ( rep == null || !rep.getName().equalsIgnoreCase( lastUsedFile.getRepositoryName() ) ) ) {
      // Allow plugins to handle how we connect to a repository
      try {
        ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.OpenRecent.id, lastUsedFile );
      } catch ( KettleException ke ) {
        log.logError( "Failed to call extension point", ke );
      }
    } else if ( !lastUsedFile.isSourceRepository() ) {
      // This file must have been on the file system.
      openFile( lastUsedFile.getFilename(), false );
    } else {
      // read from a repository...
      //
      try {
        String connection = getLastUsedConnection( lastUsedFile );
        Variables variables = new Variables();
        if ( connection != null ) {
          variables.setVariable( CONNECTION, connection );
        }
        loadLastUsedFile( lastUsedFile, rep == null ? null : rep.getName() );
        addMenuLast();
      } catch ( KettleException ke ) {
        // "Error loading transformation", "I was unable to load this
        // transformation from the
        // XML file because of an error"
        new ErrorDialog( loginDialog.getShell(),
          BaseMessages.getString( PKG, "Spoon.Dialog.LoadTransformationError.Title" ),
          BaseMessages.getString( PKG, "Spoon.Dialog.LoadTransformationError.Message" ), ke );
      }
    }
  }

  /** Add a pane to the view tab */
  public void registerViewTabPane( TreePaneExtension extension ) throws KettleException {
    viewTabPanes.addPane( extension );
  }

  private Composite addViewTree( Composite parent ) {
    Composite viewComposite = new Composite( parent, SWT.NONE );
    viewComposite.setLayout( new FormLayout()  );
    viewComposite.setBackground( GUIResource.getInstance().getColorDemoGray() );

    viewTreeToolbar = new TreeToolbar( viewComposite, SWT.NONE );
    FormData fdTreeToolbar = new FormData();
    fdTreeToolbar.left = new FormAttachment( 0 );
    fdTreeToolbar.right = new FormAttachment( 100 );
    viewTreeToolbar.setLayoutData( fdTreeToolbar );

    viewTreeToolbar.setSearchTooltip( BaseMessages.getString( PKG, "Spoon.SelectionFilter.Tooltip" ) );
    viewTreeToolbar.setSearchPlaceholder( BaseMessages.getString( PKG, "Spoon.SelectionFilter.Placeholder" ) );
    viewTreeToolbar.addSearchModifyListener( modifyEvent -> {
      selectionTreeManager.setFilter( viewTreeToolbar.getSearchText() );
      refreshTree();
      viewTreeToolbar.setFocus();
      if ( Utils.isEmpty( viewTreeToolbar.getSearchText() ) ) {
        tidyBranches( selectionTree.getItems(), false );
      }
    } );

    viewTreeToolbar.addExpandAllListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent selectionEvent ) {
        tidyBranches( selectionTree.getItems(), true );
      }
    } );

    viewTreeToolbar.addCollapseAllListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent selectionEvent ) {
        tidyBranches( selectionTree.getItems(), false );
      }
    } );

    viewTreeComposite = new Composite( viewComposite, SWT.NONE );
    viewTreeComposite.setLayout( new FillLayout() );

    FormData fdViewTreeComposite = new FormData();
    fdViewTreeComposite.left = new FormAttachment( 0 );
    fdViewTreeComposite.top = new FormAttachment( viewTreeToolbar );
    fdViewTreeComposite.right = new FormAttachment( 100 );
    fdViewTreeComposite.bottom = new FormAttachment( 100 );
    viewTreeComposite.setLayoutData( fdViewTreeComposite );

    FormData fdViewComposite = new FormData();
    fdViewComposite.left = new FormAttachment( 0 );
    fdViewComposite.top = new FormAttachment( 0 );
    fdViewComposite.right = new FormAttachment( 100 );
    fdViewComposite.bottom = new FormAttachment( 100 );
    viewComposite.setLayoutData( fdViewComposite );

    return viewComposite;
  }

  private void addViewTab( CTabFolder tabFolder ) {
    try {
      registerViewTabPane( ( Composite viewComposite, TreePaneExtension.ExpandController ignored ) -> {
        viewComposite.setLayout( new FormLayout() );
        addViewTree( viewComposite );
        return true;
      } );
    } catch ( KettleException e ) {
      // should never happen here
      log.logError( e.getLocalizedMessage(), e );
    }

    SashForm mainViewComposite = new SashForm( tabFolder, SWT.VERTICAL | SWT.BORDER );
    // build view along with any other registered extensions
    viewTabPanes.buildPanes( mainViewComposite );

    view = new CTabItem( tabFolder, SWT.NONE );
    view.setControl( mainViewComposite );
    view.setText( STRING_SPOON_MAIN_TREE );
    view.setImage( GUIResource.getInstance().getImageExploreSolutionSmall() );
  }

  private void addDesignTab( CTabFolder tabFolder ) {
    Composite designComposite = new Composite( tabFolder, SWT.NONE );
    designComposite.setLayout( new FormLayout()  );
    designComposite.setBackground( GUIResource.getInstance().getColorDemoGray() );

    designTreeToolbar = new TreeToolbar( designComposite, SWT.NONE );
    FormData fdTreeToolbar = new FormData();
    fdTreeToolbar.left = new FormAttachment( 0 );
    fdTreeToolbar.right = new FormAttachment( 100 );
    designTreeToolbar.setLayoutData( fdTreeToolbar );

    designTreeToolbar.setSearchTooltip( BaseMessages.getString( PKG, "Spoon.SelectionFilter.Tooltip" ) );
    designTreeToolbar.setSearchPlaceholder( BaseMessages.getString( PKG, "Spoon.SelectionFilter.Placeholder" ) );
    designTreeToolbar.addSearchModifyListener( modifyEvent -> {
      if ( coreObjectsTree != null && !coreObjectsTree.isDisposed() ) {
        previousShowTrans = false;
        previousShowJob = false;
        refreshCoreObjects();
        if ( !Utils.isEmpty( designTreeToolbar.getSearchText() ) ) {
          tidyBranches( coreObjectsTree.getItems(), true ); // expand all
        } else { // no filter: collapse all
          tidyBranches( coreObjectsTree.getItems(), false );
        }
      }
    } );

    designTreeToolbar.addExpandAllListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent selectionEvent ) {
        tidyBranches( coreObjectsTree.getItems(), true );
      }
    } );

    designTreeToolbar.addCollapseAllListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent selectionEvent ) {
        tidyBranches( coreObjectsTree.getItems(), false );
      }
    } );

    design = new CTabItem( tabFolder, SWT.NONE );
    design.setText( STRING_SPOON_CORE_OBJECTS_TREE );
    design.setControl( designComposite );
    design.setImage( GUIResource.getInstance().getImageEditSmall() );

    designTreeComposite = new Composite( designComposite, SWT.NONE );
    designTreeComposite.setLayout( new FillLayout() );

    FormData fdDesignTreeComposite = new FormData();
    fdDesignTreeComposite.left = new FormAttachment( 0 );
    fdDesignTreeComposite.top = new FormAttachment( designTreeToolbar );
    fdDesignTreeComposite.right = new FormAttachment( 100 );
    fdDesignTreeComposite.bottom = new FormAttachment( 100 );
    designTreeComposite.setLayoutData( fdDesignTreeComposite );

    FormData fdDesignComposite = new FormData();
    fdDesignComposite.left = new FormAttachment( 0 );
    fdDesignComposite.top = new FormAttachment( 0 );
    fdDesignComposite.right = new FormAttachment( 100 );
    fdDesignComposite.bottom = new FormAttachment( 100 );
    designComposite.setLayoutData( fdDesignComposite );
  }

  public void clearSearchFilter() {
    viewTreeToolbar.clear();
    designTreeToolbar.clear();
  }

  private void addTree() {
    mainComposite = new Composite( sashform, SWT.BORDER );
    mainComposite.setLayout( new FormLayout() );
    props.setLook( mainComposite, Props.WIDGET_STYLE_TOOLBAR );

    tabFolder = new CTabFolder( mainComposite, SWT.HORIZONTAL );
    props.setLook( tabFolder, Props.WIDGET_STYLE_TAB );

    FormData fdTab = new FormData();
    fdTab.left = new FormAttachment( 0 );
    fdTab.top = new FormAttachment( mainComposite, 0 );
    fdTab.right = new FormAttachment( 100 );
    fdTab.bottom = new FormAttachment( 100 );
    tabFolder.setLayoutData( fdTab );

    addViewTab( tabFolder );
    addDesignTab( tabFolder );

    tabFolder.setSelection( view );

    coreStepToolTipMap = new Hashtable<>();
    coreJobToolTipMap = new Hashtable<>();

    addDefaultKeyListeners( tabFolder );
    addDefaultKeyListeners( mainComposite );
  }

  public void addDefaultKeyListeners( Control control ) {
    control.addKeyListener( new KeyAdapter() {
      @Override
      public void keyPressed( KeyEvent e ) {
        // CTRL-W or CTRL-F4 : close tab
        //
        if ( ( e.keyCode == 'w' && ( e.stateMask & SWT.CONTROL ) != 0 )
          || ( e.keyCode == SWT.F4 && ( e.stateMask & SWT.CONTROL ) != 0 ) ) {
          closeFile();
        }

        // CTRL-F5 : metastore explorer
        //
        if ( e.keyCode == SWT.F5 && ( e.stateMask & SWT.CONTROL ) != 0 ) {
          new MetaStoreExplorerDialog( shell, metaStoreSupplier.get() ).open();
        }
      }
    } );
  }

  public boolean setViewMode() {
    tabFolder.setSelection( view );
    refreshTree();
    return false;
  }

  public boolean setDesignMode() {
    tabFolder.setSelection( design );
    refreshCoreObjects();
    return false;
  }

  private void tidyBranches( TreeItem[] items, boolean expand ) {
    for ( TreeItem item : items ) {
      item.setExpanded( expand );
      tidyBranches( item.getItems(), expand );
    }
  }

  public void addCoreObjectsTree() {

    if ( designTreeComposite == null ) {
      return;
    }

    // Now create a new expand bar inside that item
    // We're going to put the core object in there
    //
    coreObjectsTree = new Tree( designTreeComposite, SWT.V_SCROLL | SWT.SINGLE );
    props.setLook( coreObjectsTree );

    coreObjectsTree.addSelectionListener( new SelectionAdapter() {

      @Override
      public void widgetSelected( SelectionEvent event ) {
        // expand the selected tree item, collapse the rest
        //
        if ( props.getAutoCollapseCoreObjectsTree() ) {
          TreeItem[] selection = coreObjectsTree.getSelection();
          if ( selection.length == 1 ) {
            // expand if clicked on the the top level entry only...
            //
            TreeItem top = selection[0];
            while ( top.getParentItem() != null ) {
              top = top.getParentItem();
            }
            if ( top == selection[0] ) {
              boolean expanded = top.getExpanded();
              for ( TreeItem item : coreObjectsTree.getItems() ) {
                item.setExpanded( false );
              }
              top.setExpanded( !expanded );
            }
          }
        }
      }
    } );

    coreObjectsTree.addTreeListener( new TreeAdapter() {
      @Override
      public void treeExpanded( TreeEvent treeEvent ) {
        if ( props.getAutoCollapseCoreObjectsTree() ) {
          TreeItem treeItem = (TreeItem) treeEvent.item;
          /*
           * Trick for WSWT on Windows systems: a SelectionEvent is called after the TreeEvent if setSelection() is not
           * used here. Otherwise the first item in the list is selected as default and collapsed again but wrong, see
           * PDI-1480
           */
          coreObjectsTree.setSelection( treeItem );
          // expand the selected tree item, collapse the rest
          //
          for ( TreeItem item : coreObjectsTree.getItems() ) {
            if ( item != treeItem ) {
              item.setExpanded( false );
            } else {
              treeItem.setExpanded( true );
            }
          }
        }
      }
    } );

    coreObjectsTree.addMouseMoveListener( new MouseMoveListener() {

      @Override
      public void mouseMove( MouseEvent move ) {
        // don't show tooltips in the tree if the option is not set
        if ( !getProperties().showToolTips() ) {
          return;
        }

        toolTip.hide();
        TreeItem item = searchMouseOverTreeItem( coreObjectsTree.getItems(), move.x, move.y );
        if ( item != null ) {
          String name = item.getText();
          String tip = coreStepToolTipMap.get( name );
          if ( tip != null ) {
            PluginInterface plugin = PluginRegistry.getInstance().findPluginWithName( StepPluginType.class, name );
            if ( plugin != null ) {
              Image image =
                  GUIResource.getInstance().getImagesSteps().get( plugin.getIds()[0] ).getAsBitmapForSize( display,
                      ConstUI.ICON_SIZE, ConstUI.ICON_SIZE );
              if ( image == null ) {
                toolTip.hide();
              }
              toolTip.setImage( image );
              toolTip.setText( name + Const.CR + Const.CR + tip );
              toolTip.setBackgroundColor( GUIResource.getInstance().getColor( 255, 254, 225 ) );
              toolTip.setForegroundColor( GUIResource.getInstance().getColor( 0, 0, 0 ) );
              toolTip.show( new org.eclipse.swt.graphics.Point( move.x + 10, move.y + 10 ) );
            }
          }
          tip = coreJobToolTipMap.get( name );
          if ( tip != null ) {
            PluginInterface plugin =
              PluginRegistry.getInstance().findPluginWithName( JobEntryPluginType.class, name );
            if ( plugin != null ) {
              Image image =
                  GUIResource.getInstance().getImagesJobentries().get( plugin.getIds()[0] ).getAsBitmapForSize(
                      display, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE );
              toolTip.setImage( image );
              toolTip.setText( name + Const.CR + Const.CR + tip );
              toolTip.setBackgroundColor( GUIResource.getInstance().getColor( 255, 254, 225 ) );
              toolTip.setForegroundColor( GUIResource.getInstance().getColor( 0, 0, 0 ) );
              toolTip.show( new org.eclipse.swt.graphics.Point( move.x + 10, move.y + 10 ) );
            }
          }
        }
      }
    } );

    addDragSourceToTree( coreObjectsTree );
    addDefaultKeyListeners( coreObjectsTree );
    coreObjectsTree.addMouseListener( new MouseAdapter() {
      @Override
      public void mouseDoubleClick( MouseEvent event ) {
        boolean shift = ( event.stateMask & SWT.SHIFT ) != 0;
        doubleClickedInTree( coreObjectsTree, shift );
      }
    } );

    toolTip = new DefaultToolTip( viewTreeComposite, ToolTip.RECREATE, true );
    toolTip.setRespectMonitorBounds( true );
    toolTip.setRespectDisplayBounds( true );
    toolTip.setPopupDelay( 350 );
    toolTip.setHideDelay( 5000 );
    toolTip.setShift( new org.eclipse.swt.graphics.Point( ConstUI.TOOLTIP_OFFSET, ConstUI.TOOLTIP_OFFSET ) );
  }

  protected TreeItem searchMouseOverTreeItem( TreeItem[] treeItems, int x, int y ) {
    for ( TreeItem treeItem : treeItems ) {
      if ( treeItem.getBounds().contains( x, y ) ) {
        return treeItem;
      }
      if ( treeItem.getItemCount() > 0 ) {
        treeItem = searchMouseOverTreeItem( treeItem.getItems(), x, y );
        if ( treeItem != null ) {
          return treeItem;
        }
      }
    }
    return null;
  }

  private boolean previousShowTrans;

  private boolean previousShowJob;

  public boolean showTrans;

  public boolean showJob;

  private void addStepItem( final TreeItem categoryItem, final PluginInterface step ) {
    final Image stepImage =
      GUIResource.getInstance().getImagesStepsSmall().get( step.getIds()[ 0 ] );
    String pluginName = step.getName();
    String pluginDescription = step.getDescription();
    if ( filterMatch( pluginName ) || filterMatch( pluginDescription ) ) {
      createTreeItem( categoryItem, pluginName, stepImage, step.getIds()[ 0 ] );
      coreStepToolTipMap.put( pluginName, pluginDescription );
    }
  }

  private void addLineToBuilder( final StringBuilder builder, final Object...strings ) {
    if ( builder != null && strings.length > 0 ) {
      for ( final Object string : strings ) {
        builder.append( string );
      }
      builder.append( System.getProperty( "line.separator" ) );
    }
  }

  public void refreshCoreObjects() {
    if ( shell.isDisposed() ) {
      return;
    }

    if ( coreObjectsTree == null || coreObjectsTree.isDisposed() ) {
      addCoreObjectsTree();
    }

    showTrans = getActiveTransformation() != null;
    showJob = getActiveJob() != null;

    if ( showTrans == previousShowTrans && showJob == previousShowJob ) {
      return;
    }

    // First remove all the entries that where present...
    //
    TreeItem[] expandItems = coreObjectsTree.getItems();
    for ( TreeItem item : expandItems ) {
      item.dispose();
    }

    if ( showTrans ) {
      // Fill the base components...
      //
      // ////////////////////////////////////////////////////////////////////////////////////////////////
      // TRANSFORMATIONS
      // ////////////////////////////////////////////////////////////////////////////////////////////////

      // for convenience in debugging and validation, keep track of the step/category hierarchy and the step count for
      // each category
      final StringBuilder stepTree = new StringBuilder();
      addLineToBuilder( stepTree, LanguageChoice.getInstance().getDefaultLocale() );
      final StringBuilder stepCountPerCategory = new StringBuilder();
      addLineToBuilder( stepCountPerCategory, LanguageChoice.getInstance().getDefaultLocale() );

      PluginRegistry registry = PluginRegistry.getInstance();

      final List<PluginInterface> baseSteps = registry.getPlugins( StepPluginType.class );
      final List<String> baseCategories = registry.getCategories( StepPluginType.class );
      addLineToBuilder( stepCountPerCategory, "> total steps: ", baseSteps.size() );
      addLineToBuilder( stepCountPerCategory, "> total categories: ", baseCategories.size() );

      for ( String baseCategory : baseCategories ) {
        TreeItem item = new TreeItem( coreObjectsTree, SWT.NONE );
        item.setText( baseCategory );
        item.setImage( GUIResource.getInstance().getImageFolder() );
        addLineToBuilder( stepTree, "- ", baseCategory );

        List<PluginInterface> sortedCat = baseSteps.stream()
          .filter( baseStep -> baseStep.getCategory().equalsIgnoreCase( baseCategory ) )
          .sorted( Comparator.comparing( PluginInterface::getName ) )
          .collect( Collectors.toList() );
        addLineToBuilder( stepCountPerCategory, "- ", baseCategory, ": ", sortedCat.size() );

        for ( PluginInterface p : sortedCat ) {
          addLineToBuilder( stepTree, "--- ", p.getName() );
          addStepItem( item, p );
          // remove any step that was added to the 'sortedCat' list from 'baseSteps', that way, once all steps have been
          // sorted into their respective categories, all we have left in 'baseSteps' is mis-categorized steps, which
          // can be placed in the "Other" category
          baseSteps.remove( p );
        }
      }

      // whatever is left in the 'baseSteps' list is steps that are mis-categorized
      if ( baseSteps.size() > 0 ) {
        TreeItem item = new TreeItem( coreObjectsTree, SWT.NONE );
        final String otherCat = BaseMessages.getString( PKG, "Spoon.Other" );
        item.setText( otherCat );
        item.setImage( GUIResource.getInstance().getImageFolder() );
        addLineToBuilder( stepTree, "- ", otherCat );
        addLineToBuilder( stepCountPerCategory, "- ", otherCat, ": ", baseSteps.size() );
        for ( PluginInterface uncategorizedStep : baseSteps ) {
          addStepItem( item, uncategorizedStep );
          addLineToBuilder( stepTree, "--- ", uncategorizedStep.getName() );
        }
      }

      // Add History Items...
      TreeItem item = new TreeItem( coreObjectsTree, SWT.NONE );
      item.setText( BaseMessages.getString( PKG, "Spoon.History" ) );
      item.setImage( GUIResource.getInstance().getImageFolder() );

      List<ObjectUsageCount> pluginHistory = props.getPluginHistory();

      // The top 10 at most, the rest is not interesting anyway
      //
      for ( int i = 0; i < pluginHistory.size() && i < 10; i++ ) {
        ObjectUsageCount usage = pluginHistory.get( i );
        PluginInterface stepPlugin =
          PluginRegistry.getInstance().findPluginWithId( StepPluginType.class, usage.getObjectName() );
        if ( stepPlugin != null ) {
          final Image stepImage =
              GUIResource.getInstance().getImagesSteps().get( stepPlugin.getIds()[0] ).getAsBitmapForSize( display, ConstUI.MEDIUM_ICON_SIZE, ConstUI.MEDIUM_ICON_SIZE );
          String pluginName = Const.NVL( stepPlugin.getName(), "" );
          String pluginDescription = Const.NVL( stepPlugin.getDescription(), "" );

          if ( !filterMatch( pluginName ) && !filterMatch( pluginDescription ) ) {
            continue;
          }

          TreeItem stepItem = createTreeItem( item, pluginName, stepImage );
          stepItem.addListener( SWT.Selection, new Listener() {

            @Override
            public void handleEvent( Event event ) {
              System.out.println( "Tree item Listener fired" );
            }
          } );

          coreStepToolTipMap.put( stepPlugin.getDescription(), pluginDescription + " (" + usage.getNrUses() + ")" );
        }
      }
      log.logDebug( stepCountPerCategory.toString() );
      log.logDebug( stepTree.toString() );
    }

    if ( showJob ) {
      // Fill the base components...
      //
      // ////////////////////////////////////////////////////////////////////////////////////////////////
      // JOBS
      // ////////////////////////////////////////////////////////////////////////////////////////////////

      PluginRegistry registry = PluginRegistry.getInstance();
      List<PluginInterface> baseJobEntries = registry.getPlugins( JobEntryPluginType.class );
      List<String> baseCategories = registry.getCategories( JobEntryPluginType.class );

      TreeItem generalItem = null;

      for ( String baseCategory : baseCategories ) {
        TreeItem item = new TreeItem( coreObjectsTree, SWT.NONE );
        item.setText( baseCategory );
        item.setImage( GUIResource.getInstance().getImageFolder() );

        if ( baseCategory.equalsIgnoreCase( JobEntryPluginType.GENERAL_CATEGORY ) ) {
          generalItem = item;
        }

        for ( int j = 0; j < baseJobEntries.size(); j++ ) {
          if ( !baseJobEntries.get( j ).getIds()[ 0 ].equals( JobMeta.STRING_SPECIAL ) ) {
            if ( baseJobEntries.get( j ).getCategory().equalsIgnoreCase( baseCategory ) ) {
              final Image jobEntryImage =
                  GUIResource.getInstance().getImagesJobentriesSmall().get( baseJobEntries.get( j ).getIds()[0] );
              String pluginName = Const.NVL( baseJobEntries.get( j ).getName(), "" );
              String pluginDescription = Const.NVL( baseJobEntries.get( j ).getDescription(), "" );

              if ( !filterMatch( pluginName ) && !filterMatch( pluginDescription ) ) {
                continue;
              }

              TreeItem stepItem = createTreeItem( item, pluginName, jobEntryImage );
              stepItem.addListener( SWT.Selection, new Listener() {

                @Override
                public void handleEvent( Event arg0 ) {
                  System.out.println( "Tree item Listener fired" );
                }
              } );
              // if (isPlugin)
              // stepItem.setFont(GUIResource.getInstance().getFontBold());

              coreJobToolTipMap.put( pluginName, pluginDescription );
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

      String[] specialText = new String[] { startEntry.getName(), dummyEntry.getName(), };
      String[] specialTooltip = new String[] { startEntry.getDescription(), dummyEntry.getDescription(), };
      Image[] specialImage =
        new Image[] {
          GUIResource.getInstance().getImageStartMedium(), GUIResource.getInstance().getImageDummyMedium() };

      int pos = 0;
      for ( int i = 0; i < specialText.length; i++ ) {
        if ( !filterMatch( specialText[i] ) && !filterMatch( specialTooltip[i] ) ) {
          continue;
        }

        TreeItem specialItem = new TreeItem( generalItem, SWT.NONE, pos );
        specialItem.setImage( specialImage[i] );
        specialItem.setText( specialText[i] );
        specialItem.addListener( SWT.Selection, new Listener() {

          @Override
          public void handleEvent( Event arg0 ) {
            System.out.println( "Tree item Listener fired" );
          }

        } );

        coreJobToolTipMap.put( specialText[i], specialTooltip[i] );
        pos++;
      }
    }

    designTreeComposite.layout( true, true );

    previousShowTrans = showTrans;
    previousShowJob = showJob;
  }

  protected void shareObject( SharedObjectInterface sharedObject ) {
    sharedObject.setShared( true );
    EngineMetaInterface meta = getActiveMeta();
    try {
      if ( meta != null ) {
        SharedObjects sharedObjects = null;
        if ( meta instanceof TransMeta ) {
          sharedObjects = ( (TransMeta) meta ).getSharedObjects();
        }
        if ( meta instanceof JobMeta ) {
          sharedObjects = ( (JobMeta) meta ).getSharedObjects();
        }
        if ( sharedObjects != null ) {
          sharedObjects.storeObject( sharedObject );
          sharedObjects.saveToFile();
        }
      }
    } catch ( Exception e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "Spoon.Dialog.ErrorWritingSharedObjects.Title" ), BaseMessages
          .getString( PKG, "Spoon.Dialog.ErrorWritingSharedObjects.Message" ), e );
    }
    refreshTree( selectionTreeManager.getNameByType( sharedObject.getClass() ) );
  }

  protected void unShareObject( SharedObjectInterface sharedObject ) {
    MessageBox mb = new MessageBox( shell, SWT.YES | SWT.NO | SWT.ICON_WARNING );
    // "Are you sure you want to stop sharing?"
    mb.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.StopSharing.Message" ) );
    mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.StopSharing.Title" ) ); // Warning!
    int answer = mb.open();
    if ( answer == SWT.YES ) {
      sharedObject.setShared( false );
      EngineMetaInterface meta = getActiveMeta();
      try {
        if ( meta != null ) {
          SharedObjects sharedObjects = null;
          if ( meta instanceof TransMeta ) {
            sharedObjects = ( (TransMeta) meta ).getSharedObjects();
          }
          if ( meta instanceof JobMeta ) {
            sharedObjects = ( (JobMeta) meta ).getSharedObjects();
          }
          if ( sharedObjects != null ) {
            sharedObjects.removeObject( sharedObject );
            sharedObjects.saveToFile();
          }
        }
      } catch ( Exception e ) {
        new ErrorDialog(
          shell, BaseMessages.getString( PKG, "Spoon.Dialog.ErrorWritingSharedObjects.Title" ), BaseMessages
            .getString( PKG, "Spoon.Dialog.ErrorWritingSharedObjects.Message" ), e );
      }
      refreshTree( selectionTreeManager.getNameByType( sharedObject.getClass() ) );
    }
  }

  /**
   * @return The object that is selected in the tree or null if we couldn't figure it out. (titles etc. == null)
   */
  public TreeSelection[] getTreeObjects( final Tree tree ) {
    return delegates.tree.getTreeObjects( tree, selectionTree, coreObjectsTree );
  }

  private void addDragSourceToTree( final Tree tree ) {
    delegates.tree.addDragSourceToTree( tree, selectionTree, coreObjectsTree );
  }

  public void hideToolTips() {
    if ( toolTip != null ) {
      toolTip.hide();
    }
  }

  /**
   * If you click in the tree, you might want to show the corresponding window.
   */
  public void showSelection() {
    TreeSelection[] objects = getTreeObjects( selectionTree );
    if ( objects.length != 1 ) {
      return; // not yet supported, we can do this later when the OSX bug
      // goes away
    }

    TreeSelection object = objects[0];

    final Object selection = object.getSelection();
    final Object parent = object.getParent();

    TransMeta transMeta = null;
    if ( selection instanceof TransMeta ) {
      transMeta = (TransMeta) selection;
    }
    if ( parent instanceof TransMeta ) {
      transMeta = (TransMeta) parent;
    }

    if ( transMeta != null ) {

      TabMapEntry entry = delegates.tabs.findTabMapEntry( transMeta );
      if ( entry != null ) {
        int current = tabfolder.getSelectedIndex();
        int desired = tabfolder.indexOf( entry.getTabItem() );
        if ( current != desired ) {
          tabfolder.setSelected( desired );
        }
        transMeta.setInternalKettleVariables();
        if ( getCoreObjectsState() != STATE_CORE_OBJECTS_SPOON ) {
          // Switch the core objects in the lower left corner to the
          // spoon trans types
          refreshCoreObjects();
        }
      }
    }

    JobMeta jobMeta = null;
    if ( selection instanceof JobMeta ) {
      jobMeta = (JobMeta) selection;
    }
    if ( parent instanceof JobMeta ) {
      jobMeta = (JobMeta) parent;
    }
    if ( jobMeta != null ) {

      TabMapEntry entry = delegates.tabs.findTabMapEntry( jobMeta );
      if ( entry != null ) {
        int current = tabfolder.getSelectedIndex();
        int desired = tabfolder.indexOf( entry.getTabItem() );
        if ( current != desired ) {
          tabfolder.setSelected( desired );
        }
        jobMeta.setInternalKettleVariables();
        if ( getCoreObjectsState() != STATE_CORE_OBJECTS_CHEF ) {
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
    newHop( (TransMeta) selectionObjectParent );
  }

  public void sortHops() {
    ( (TransMeta) selectionObjectParent ).sortHops();
    refreshTree();
  }

  public void newDatabasePartitioningSchema() {
    TransMeta transMeta = getActiveTransformation();
    if ( transMeta != null ) {
      newPartitioningSchema( transMeta );
    }
  }

  public void newClusteringSchema() {
    TransMeta transMeta = getActiveTransformation();
    if ( transMeta != null ) {
      newClusteringSchema( transMeta );
    }
  }

  public void newSlaveServer() {
    HasSlaveServersInterface hasSlaveServersInterface = getActiveHasSlaveServersInterface();
    if ( Objects.isNull( hasSlaveServersInterface ) ) {
      return;
    }
    newSlaveServer( hasSlaveServersInterface );
  }

  public void editTransformationPropertiesPopup() {
    TransGraph.editProperties( (TransMeta) selectionObject, this, rep, true );
  }

  public void addTransLog() {
    TransGraph activeTransGraph = getActiveTransGraph();
    if ( activeTransGraph != null ) {
      activeTransGraph.transLogDelegate.addTransLog();
      activeTransGraph.transGridDelegate.addTransGrid();
    }
  }

  public void addTransHistory() {
    TransGraph activeTransGraph = getActiveTransGraph();
    if ( activeTransGraph != null ) {
      activeTransGraph.transHistoryDelegate.addTransHistory();
    }
  }

  public boolean editJobProperties( String id ) {
    if ( "job-settings".equals( id ) ) {
      return JobGraph.editProperties( getActiveJob(), this, rep, true );
    } else if ( "job-inst-settings".equals( id ) ) {
      return JobGraph.editProperties( (JobMeta) selectionObject, this, rep, true );
    }
    return false;
  }

  public void editJobPropertiesPopup() {
    JobGraph.editProperties( (JobMeta) selectionObject, this, rep, true );
  }

  public void addJobLog() {
    JobGraph activeJobGraph = getActiveJobGraph();
    if ( activeJobGraph != null ) {
      activeJobGraph.jobLogDelegate.addJobLog();
      activeJobGraph.jobGridDelegate.addJobGrid();
    }
  }

  public void addJobHistory() {
    addJobHistory( (JobMeta) selectionObject, true );
  }

  public void newStep() {
    newStep( getActiveTransformation() );
  }

  public void editConnection() {

    if ( RepositorySecurityUI.verifyOperations( shell, rep, RepositoryOperation.MODIFY_DATABASE ) ) {
      return;
    }

    final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
    delegates.db.editConnection( databaseMeta );
  }

  public void dupeConnection() {
    final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
    final HasDatabasesInterface hasDatabasesInterface = (HasDatabasesInterface) selectionObjectParent;
    delegates.db.dupeConnection( hasDatabasesInterface, databaseMeta );
  }

  public void clipConnection() {
    final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
    delegates.db.clipConnection( databaseMeta );
  }

  public void delConnection() {
    if ( RepositorySecurityUI.verifyOperations( shell, rep, RepositoryOperation.DELETE_DATABASE ) ) {
      return;
    }

    final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
    MessageBox mb = new MessageBox( shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION );
    mb.setMessage( BaseMessages.getString(
      PKG, "Spoon.ExploreDB.DeleteConnectionAsk.Message", databaseMeta.getName() ) );
    mb.setText( BaseMessages.getString( PKG, "Spoon.ExploreDB.DeleteConnectionAsk.Title" ) );
    int response = mb.open();

    if ( response != SWT.YES ) {
      return;
    }

    final HasDatabasesInterface hasDatabasesInterface = (HasDatabasesInterface) selectionObjectParent;
    delegates.db.delConnection( hasDatabasesInterface, databaseMeta );
  }

  public void sqlConnection() {
    final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
    delegates.db.sqlConnection( databaseMeta );
  }

  public void clearDBCache( String id ) {
    if ( "database-class-clear-cache".equals( id ) ) {
      delegates.db.clearDBCache( null );
    }
    if ( "database-inst-clear-cache".equals( id ) ) {
      final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
      delegates.db.clearDBCache( databaseMeta );
    }
  }

  public void exploreDatabase() {

    if ( RepositorySecurityUI.verifyOperations( shell, rep, RepositoryOperation.EXPLORE_DATABASE ) ) {
      return;
    }

    // Show a minimal window to allow you to quickly select the database
    // connection to explore
    //
    List<DatabaseMeta> databases = new ArrayList<>();

    // First load the connections from the loaded file
    //
    HasDatabasesInterface databasesInterface = getActiveHasDatabasesInterface();
    if ( databasesInterface != null ) {
      databases.addAll( databasesInterface.getDatabases() );
    }

    // Overwrite the information with the connections from the repository
    //
    if ( rep != null ) {
      try {
        List<DatabaseMeta> list = rep.readDatabases();
        for ( DatabaseMeta databaseMeta : list ) {
          int index = databases.indexOf( databaseMeta );
          if ( index < 0 ) {
            databases.add( databaseMeta );
          } else {
            databases.set( index, databaseMeta );
          }
        }
      } catch ( KettleException e ) {
        log.logError( "Unexpected repository error", e.getMessage() );
      }
    }

    if ( databases.size() == 0 ) {
      return;
    }

    // OK, get a list of all the database names...
    //
    String[] databaseNames = new String[databases.size()];
    for ( int i = 0; i < databases.size(); i++ ) {
      databaseNames[i] = databases.get( i ).getName();
    }

    // show the shell...
    //
    EnterSelectionDialog dialog = new EnterSelectionDialog( shell, databaseNames,
      BaseMessages.getString( PKG, "Spoon.ExploreDB.SelectDB.Title" ),
      BaseMessages.getString( PKG, "Spoon.ExploreDB.SelectDB.Message" ) );
    String name = dialog.open();
    if ( name != null ) {
      selectionObject = DatabaseMeta.findDatabase( databases, name );
      exploreDB();
    }
  }

  public void exploreDB() {
    final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
    delegates.db.exploreDB( databaseMeta, true );
  }

  public void editStep() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final StepMeta stepMeta = (StepMeta) selectionObject;
    delegates.steps.editStep( transMeta, stepMeta );
    sharedObjectSyncUtil.synchronizeSteps( stepMeta );
  }

  public void dupeStep() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final StepMeta stepMeta = (StepMeta) selectionObject;
    delegates.steps.dupeStep( transMeta, stepMeta );
  }

  public void delStep() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final StepMeta stepMeta = (StepMeta) selectionObject;
    delegates.steps.delStep( transMeta, stepMeta );
  }

  public void helpStep() {
    final StepMeta stepMeta = (StepMeta) selectionObject;
    PluginInterface stepPlugin =
      PluginRegistry.getInstance().findPluginWithId( StepPluginType.class, stepMeta.getStepID() );
    HelpUtils.openHelpDialog( shell, stepPlugin );
  }

  public void shareObject( String id ) {
    if ( "database-inst-share".equals( id ) ) {
      final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
      if ( databaseMeta.isShared() ) {
        unShareObject( databaseMeta );
      } else {
        shareObject( databaseMeta );
      }
    }
    if ( "step-inst-share".equals( id ) ) {
      final StepMeta stepMeta = (StepMeta) selectionObject;
      shareObject( stepMeta );
    }
    if ( "partition-schema-inst-share".equals( id ) ) {
      final PartitionSchema partitionSchema = (PartitionSchema) selectionObject;
      shareObject( partitionSchema );
    }
    if ( "cluster-schema-inst-share".equals( id ) ) {
      final ClusterSchema clusterSchema = (ClusterSchema) selectionObject;
      shareObject( clusterSchema );
    }
    if ( "slave-server-inst-share".equals( id ) ) {
      final SlaveServer slaveServer = (SlaveServer) selectionObject;
      shareObject( slaveServer );
    }
    sharedObjectSyncUtil.reloadSharedObjects();
  }

  public void editJobEntry() {
    final JobMeta jobMeta = (JobMeta) selectionObjectParent;
    final JobEntryCopy jobEntry = (JobEntryCopy) selectionObject;
    editJobEntry( jobMeta, jobEntry );
  }

  public void dupeJobEntry() {
    final JobMeta jobMeta = (JobMeta) selectionObjectParent;
    final JobEntryCopy jobEntry = (JobEntryCopy) selectionObject;
    delegates.jobs.dupeJobEntry( jobMeta, jobEntry );
  }

  public void deleteJobEntryCopies() {
    final JobMeta jobMeta = (JobMeta) selectionObjectParent;
    final JobEntryCopy jobEntry = (JobEntryCopy) selectionObject;
    deleteJobEntryCopies( jobMeta, jobEntry );
  }

  public void helpJobEntry() {
    final JobEntryCopy jobEntry = (JobEntryCopy) selectionObject;
    String jobName = jobEntry.getName();
    PluginInterface jobEntryPlugin =
      PluginRegistry.getInstance().findPluginWithName( JobEntryPluginType.class, jobName );
    HelpUtils.openHelpDialog( shell, jobEntryPlugin );
  }

  public void editHop() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final TransHopMeta transHopMeta = (TransHopMeta) selectionObject;
    editHop( transMeta, transHopMeta );
  }

  public void delHop() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final TransHopMeta transHopMeta = (TransHopMeta) selectionObject;
    delHop( transMeta, transHopMeta );
  }

  public void editPartitionSchema() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final PartitionSchema partitionSchema = (PartitionSchema) selectionObject;
    editPartitionSchema( transMeta, partitionSchema );
  }

  public void delPartitionSchema() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final PartitionSchema partitionSchema = (PartitionSchema) selectionObject;
    delPartitionSchema( transMeta, partitionSchema );
  }

  public void editClusterSchema() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final ClusterSchema clusterSchema = (ClusterSchema) selectionObject;
    delegates.clusters.editClusterSchema( transMeta, clusterSchema );
  }

  public void delClusterSchema() {
    final TransMeta transMeta = (TransMeta) selectionObjectParent;
    final ClusterSchema clusterSchema = (ClusterSchema) selectionObject;
    delegates.clusters.delClusterSchema( transMeta, clusterSchema );
  }

  public void monitorClusterSchema() throws KettleException {
    final ClusterSchema clusterSchema = (ClusterSchema) selectionObject;
    monitorClusterSchema( clusterSchema );
  }

  public void editSlaveServer() {
    final SlaveServer slaveServer = (SlaveServer) selectionObject;
    editSlaveServer( slaveServer );
  }

  public void delSlaveServer() {
    final HasSlaveServersInterface hasSlaveServersInterface = (HasSlaveServersInterface) selectionObjectParent;
    final SlaveServer slaveServer = (SlaveServer) selectionObject;
    delSlaveServer( hasSlaveServersInterface, slaveServer );
  }

  public void addSpoonSlave() {
    final SlaveServer slaveServer = (SlaveServer) selectionObject;
    addSpoonSlave( slaveServer );
  }

  private synchronized void setMenu( Tree tree ) {
    TreeSelection[] objects = getTreeObjects( tree );
    if ( objects.length != 1 ) {
      return; // not yet supported, we can do this later when the OSX bug
      // goes away
    }

    TreeSelection object = objects[0];

    selectionObject = object.getSelection();
    Object selection = selectionObject;
    selectionObjectParent = object.getParent();

    // Not clicked on a real object: returns a class
    XulMenupopup spoonMenu = null;
    if ( selection instanceof Class<?> ) {
      if ( selection.equals( TransMeta.class ) ) {
        // New
        spoonMenu = (XulMenupopup) menuMap.get( "trans-class" );
      } else if ( selection.equals( JobMeta.class ) ) {
        // New
        spoonMenu = (XulMenupopup) menuMap.get( "job-class" );
      } else if ( selection.equals( TransHopMeta.class ) ) {
        // New
        spoonMenu = (XulMenupopup) menuMap.get( "trans-hop-class" );
      } else if ( selection.equals( DatabaseMeta.class ) ) {
        spoonMenu = (XulMenupopup) menuMap.get( "database-class" );
      } else if ( selection.equals( PartitionSchema.class ) ) {
        // New
        spoonMenu = (XulMenupopup) menuMap.get( "partition-schema-class" );
      } else if ( selection.equals( ClusterSchema.class ) ) {
        spoonMenu = (XulMenupopup) menuMap.get( "cluster-schema-class" );
      } else if ( selection.equals( SlaveServer.class ) ) {
        spoonMenu = (XulMenupopup) menuMap.get( "slave-cluster-class" );
      } else {
        spoonMenu = null;
      }
    } else {

      if ( selection instanceof TransMeta ) {
        spoonMenu = (XulMenupopup) menuMap.get( "trans-inst" );
      } else if ( selection instanceof JobMeta ) {
        spoonMenu = (XulMenupopup) menuMap.get( "job-inst" );
      } else if ( selection instanceof PluginInterface ) {
        spoonMenu = (XulMenupopup) menuMap.get( "step-plugin" );
      } else if ( selection instanceof DatabaseMeta ) {
        spoonMenu = (XulMenupopup) menuMap.get( "database-inst" );
        // disable for now if the connection is an SAP ERP type of database...
        //
        XulMenuitem item =
          (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById( "database-inst-explore" );
        if ( item != null ) {
          final DatabaseMeta databaseMeta = (DatabaseMeta) selection;
          item.setDisabled( !databaseMeta.isExplorable() );
        }
        item = (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById( "database-inst-clear-cache" );
        if ( item != null ) {
          final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
          item.setLabel( BaseMessages.getString( PKG, "Spoon.Menu.Popup.CONNECTIONS.ClearDBCache" )
            + databaseMeta.getName() ); // Clear
        }

        item = (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById( "database-inst-share" );
        if ( item != null ) {
          final DatabaseMeta databaseMeta = (DatabaseMeta) selection;
          if ( databaseMeta.isShared() ) {
            item.setLabel( BaseMessages.getString( PKG, "Spoon.Menu.Popup.CONNECTIONS.UnShare" ) );
          } else {
            item.setLabel( BaseMessages.getString( PKG, "Spoon.Menu.Popup.CONNECTIONS.Share" ) );
          }
        }
      } else if ( selection instanceof StepMeta ) {
        spoonMenu = (XulMenupopup) menuMap.get( "step-inst" );
      } else if ( selection instanceof JobEntryCopy ) {
        spoonMenu = (XulMenupopup) menuMap.get( "job-entry-copy-inst" );
      } else if ( selection instanceof TransHopMeta ) {
        spoonMenu = (XulMenupopup) menuMap.get( "trans-hop-inst" );
      } else if ( selection instanceof PartitionSchema ) {
        spoonMenu = (XulMenupopup) menuMap.get( "partition-schema-inst" );
      } else if ( selection instanceof ClusterSchema ) {
        spoonMenu = (XulMenupopup) menuMap.get( "cluster-schema-inst" );
      } else if ( selection instanceof SlaveServer ) {
        spoonMenu = (XulMenupopup) menuMap.get( "slave-server-inst" );
      }

    }
    if ( spoonMenu != null ) {
      ConstUI.displayMenu( spoonMenu, tree );
    } else {
      tree.setMenu( null );
    }

    createPopUpMenuExtension();
  }

  /**
   * Reaction to double click
   *
   */
  private void doubleClickedInTree( Tree tree ) {
    doubleClickedInTree( tree, false );
  }

  /**
   * Reaction to double click
   *
   */
  private void doubleClickedInTree( Tree tree, boolean shift ) {
    TreeSelection[] objects = getTreeObjects( tree );
    if ( objects.length != 1 ) {
      return; // not yet supported, we can do this later when the OSX bug
      // goes away
    }

    TreeSelection object = objects[0];

    final Object selection = object.getSelection();
    final Object parent = object.getParent();

    if ( selection instanceof Class<?> ) {
      if ( selection.equals( TransMeta.class ) ) {
        newTransFile();
      }
      if ( selection.equals( JobMeta.class ) ) {
        newJobFile();
      }
      if ( selection.equals( TransHopMeta.class ) ) {
        newHop( (TransMeta) parent );
      }
      if ( selection.equals( DatabaseMeta.class ) ) {
        delegates.db.newConnection();
      }
      if ( selection.equals( PartitionSchema.class ) ) {
        newPartitioningSchema( (TransMeta) parent );
      }
      if ( selection.equals( ClusterSchema.class ) ) {
        newClusteringSchema( (TransMeta) parent );
      }
      if ( selection.equals( SlaveServer.class ) ) {
        newSlaveServer( (HasSlaveServersInterface) parent );
      }
    } else {
      if ( selection instanceof TransMeta ) {
        TransGraph.editProperties( (TransMeta) selection, this, rep, true );
      }
      if ( selection instanceof JobMeta ) {
        JobGraph.editProperties( (JobMeta) selection, this, rep, true );
      }
      if ( selection instanceof PluginInterface ) {
        PluginInterface plugin = (PluginInterface) selection;
        if ( plugin.getPluginType().equals( StepPluginType.class ) ) {
          TransGraph transGraph = getActiveTransGraph();
          if ( transGraph != null ) {
            transGraph.addStepToChain( plugin, shift );
          }
        }
        if ( plugin.getPluginType().equals( JobEntryPluginType.class ) ) {
          JobGraph jobGraph = getActiveJobGraph();
          if ( jobGraph != null ) {
            jobGraph.addJobEntryToChain( object.getItemText(), shift );
          }
        }
      }
      if ( selection instanceof DatabaseMeta ) {
        DatabaseMeta database = (DatabaseMeta) selection;
        delegates.db.editConnection( database );
      }
      if ( selection instanceof StepMeta ) {
        StepMeta step = (StepMeta) selection;
        delegates.steps.editStep( (TransMeta) parent, step );
        sharedObjectSyncUtil.synchronizeSteps( step );
      }
      if ( selection instanceof JobEntryCopy ) {
        editJobEntry( (JobMeta) parent, (JobEntryCopy) selection );
      }
      if ( selection instanceof TransHopMeta ) {
        editHop( (TransMeta) parent, (TransHopMeta) selection );
      }
      if ( selection instanceof PartitionSchema ) {
        editPartitionSchema( (TransMeta) parent, (PartitionSchema) selection );
      }
      if ( selection instanceof ClusterSchema ) {
        delegates.clusters.editClusterSchema( (TransMeta) parent, (ClusterSchema) selection );
      }
      if ( selection instanceof SlaveServer ) {
        editSlaveServer( (SlaveServer) selection );
      }

      editSelectionTreeExtension( selection );
    }
  }

  protected void monitorClusterSchema( ClusterSchema clusterSchema ) throws KettleException {
    for ( int i = 0; i < clusterSchema.getSlaveServers().size(); i++ ) {
      SlaveServer slaveServer = clusterSchema.getSlaveServers().get( i );
      addSpoonSlave( slaveServer );
    }
  }

  private AbstractMeta getActiveAbstractMeta() {
    AbstractMeta abstractMeta = getActiveTransformation();
    if ( abstractMeta == null ) {
      abstractMeta = getActiveJob();
    }
    return abstractMeta;
  }

  private void addTabs() {

    if ( tabComp != null ) {
      tabComp.dispose();
    }

    tabComp = new Composite( sashform, SWT.BORDER );
    props.setLook( tabComp );
    tabComp.setLayout( new FillLayout() );

    tabfolder = new TabSet( tabComp );
    tabfolder.setChangedFont( GUIResource.getInstance().getFontBold() );
    final CTabFolder cTabFolder = tabfolder.getSwtTabset();
    props.setLook( cTabFolder, Props.WIDGET_STYLE_TAB );
    cTabFolder.addMenuDetectListener( new MenuDetectListener() {
      @Override
      public void menuDetected( MenuDetectEvent event ) {
        org.eclipse.swt.graphics.Point real = new org.eclipse.swt.graphics.Point( event.x, event.y );
        org.eclipse.swt.graphics.Point point = display.map( null, cTabFolder, real );
        final CTabItem item = cTabFolder.getItem( point );
        if ( item != null ) {
          Menu menu = new Menu( cTabFolder );
          MenuItem closeItem = new MenuItem( menu, SWT.NONE );
          closeItem.setText( BaseMessages.getString( PKG, "Spoon.Tab.Close" ) );
          closeItem.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent event ) {
              int index = tabfolder.getSwtTabset().indexOf( item );
              if ( index >= 0 ) {
                TabMapEntry entry = delegates.tabs.getTabs().get( index );
                tabClose( entry.getTabItem() );
              }
            }
          } );

          MenuItem closeAllItems = new MenuItem( menu, SWT.NONE );
          closeAllItems.setText( BaseMessages.getString( PKG, "Spoon.Tab.CloseAll" ) );
          closeAllItems.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent event ) {
              for ( TabMapEntry entry : delegates.tabs.getTabs() ) {
                tabClose( entry.getTabItem() );
              }
            }
          } );

          MenuItem closeOtherItems = new MenuItem( menu, SWT.NONE );
          closeOtherItems.setText( BaseMessages.getString( PKG, "Spoon.Tab.CloseOthers" ) );
          closeOtherItems.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent event ) {
              int index = tabfolder.getSwtTabset().indexOf( item );
              if ( index >= 0 ) {
                TabMapEntry entry = delegates.tabs.getTabs().get( index );
                for ( TabMapEntry closeEntry : delegates.tabs.getTabs() ) {
                  if ( !closeEntry.equals( entry ) ) {
                    tabClose( closeEntry.getTabItem() );
                  }
                }
              }
            }
          } );

          menu.setLocation( real );
          menu.setVisible( true );

        }
      }
    } );

    int[] weights = props.getSashWeights();
    sashform.setWeights( weights );
    sashform.setVisible( true );

    // Set a minimum width on the sash so that the view and design buttons
    // on the left panel are always visible.
    //
    Control[] comps = sashform.getChildren();
    for ( Control comp : comps ) {

      if ( comp instanceof Sash ) {
        int limit = 10;

        final int SASH_LIMIT = Const.isOSX() ? 150 : limit;
        final Sash sash = (Sash) comp;

        sash.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent event ) {
            Rectangle rect = sash.getParent().getClientArea();
            event.x = Math.min( Math.max( event.x, SASH_LIMIT ), rect.width - SASH_LIMIT );
            if ( event.detail != SWT.DRAG ) {
              sash.setBounds( event.x, event.y, event.width, event.height );
              sashform.layout();
            }
          }
        } );
      }
    }

    tabfolder.addListener( this ); // methods: tabDeselected, tabClose,
    // tabSelected

  }

  @Override
  public void tabDeselected( TabItem item ) {
    if ( !ExpandedContentManager.isVisible() ) {
      item.setSashWeights( sashform.getWeights() );
    }
  }

  public boolean tabCloseSelected() {
    return tabCloseSelected( false );
  }

  public boolean tabCloseSelected( boolean force ) {
    // this gets called on by the file-close menu item

    String activePerspectiveId = SpoonPerspectiveManager.getInstance().getActivePerspective().getId();
    boolean etlPerspective = activePerspectiveId.equals( MainSpoonPerspective.ID );

    if ( etlPerspective || EngineMetaUtils.isJobOrTransformation( getActiveMeta() ) ) {
      return tabClose( tabfolder.getSelected(), force );
    }

    // hack to make the plugins see file-close commands
    // this should be resolved properly when resolving PDI-6054
    // maybe by extending the SpoonPerspectiveInterface to register event handlers from Spoon?
    try {
      SpoonPerspective activePerspective = SpoonPerspectiveManager.getInstance().getActivePerspective();
      Class<? extends SpoonPerspective> cls = activePerspective.getClass();
      Method m = cls.getMethod( "onFileClose" );
      return (Boolean) m.invoke( activePerspective );
    } catch ( Exception e ) {
      // ignore any errors resulting from the hack
      // e.printStackTrace();
    }

    return false;

  }

  @Override
  public boolean tabClose( TabItem item ) {
    return tabClose( item, false );
  }

  public boolean tabClose( TabItem item, boolean force ) {
    try {
      try {
        return delegates.tabs.tabClose( item, force );
      } catch ( KettleRepositoryLostException e ) {
        handleRepositoryLost( e );
        return delegates.tabs.tabClose( item, force );
      }
    } catch ( Exception e ) {
      new ErrorDialog( shell, "Error", "Unexpected error closing tab!", e );
    }
    return false;
  }

  public TabSet getTabSet() {
    return tabfolder;
  }

  @Override
  public void tabSelected( TabItem item ) {
    sashform.setWeights( item.getSashWeights() );
    delegates.tabs.tabSelected( item );
    if ( ExpandedContentManager.isVisible() ) {
      sashform.setWeights( new int[] {0, 1000} );
    }
    enableMenus();
  }

  public String getRepositoryName() {
    return repositoryName;
  }

  public String getUsername() {
    if ( rep == null || rep.getUserInfo() == null ) {
      return "";
    }

    return rep.getUserInfo().getLogin();
  }

  public void pasteXML( TransMeta transMeta, String clipcontent, Point loc ) {
    if ( RepositorySecurityUI.verifyOperations( shell, rep,
        RepositoryOperation.MODIFY_TRANSFORMATION, RepositoryOperation.EXECUTE_TRANSFORMATION ) ) {
      return;
    }
    try {
      Document doc = XMLHandler.loadXMLString( clipcontent );
      Node transNode = XMLHandler.getSubNode( doc, Spoon.XML_TAG_TRANSFORMATION_STEPS );
      // De-select all, re-select pasted steps...
      transMeta.unselectAll();

      Node stepsNode = XMLHandler.getSubNode( transNode, "steps" );
      int nr = XMLHandler.countNodes( stepsNode, "step" );
      if ( getLog().isDebug() ) {
        // "I found "+nr+" steps to paste on location: "
        getLog().logDebug( BaseMessages.getString( PKG, "Spoon.Log.FoundSteps", "" + nr ) + loc );
      }
      StepMeta[] steps = new StepMeta[nr];
      ArrayList<String> stepOldNames = new ArrayList<>( nr );

      // Point min = new Point(loc.x, loc.y);
      Point min = new Point( 99999999, 99999999 );

      // Load the steps...
      for ( int i = 0; i < nr; i++ ) {
        Node stepNode = XMLHandler.getSubNodeByNr( stepsNode, "step", i );
        steps[i] = new StepMeta( stepNode, transMeta.getDatabases(), metaStoreSupplier.get() );

        if ( loc != null ) {
          Point p = steps[i].getLocation();

          if ( min.x > p.x ) {
            min.x = p.x;
          }
          if ( min.y > p.y ) {
            min.y = p.y;
          }
        }
      }

      // Load the hops...
      Node hopsNode = XMLHandler.getSubNode( transNode, "order" );
      nr = XMLHandler.countNodes( hopsNode, "hop" );
      if ( getLog().isDebug() ) {
        // "I found "+nr+" hops to paste."
        getLog().logDebug( BaseMessages.getString( PKG, "Spoon.Log.FoundHops", "" + nr ) );
      }
      TransHopMeta[] hops = new TransHopMeta[nr];

      for ( int i = 0; i < nr; i++ ) {
        Node hopNode = XMLHandler.getSubNodeByNr( hopsNode, "hop", i );
        hops[i] = new TransHopMeta( hopNode,  Arrays.asList( steps ) );
      }

      // This is the offset:
      Point offset = new Point( loc.x - min.x, loc.y - min.y );

      // Undo/redo object positions...
      int[] position = new int[steps.length];

      for ( int i = 0; i < steps.length; i++ ) {
        Point p = steps[i].getLocation();
        String name = steps[i].getName();

        steps[i].setLocation( p.x + offset.x, p.y + offset.y );
        steps[i].setDraw( true );

        // Check the name, find alternative...
        stepOldNames.add( name );
        steps[i].setName( transMeta.getAlternativeStepname( name ) );
        transMeta.addStep( steps[i] );
        position[i] = transMeta.indexOfStep( steps[i] );
        steps[i].setSelected( true );
      }

      // Add the hops too...
      for ( TransHopMeta hop : hops ) {
        transMeta.addTransHop( hop );
      }

      // Load the notes...
      Node notesNode = XMLHandler.getSubNode( transNode, "notepads" );
      nr = XMLHandler.countNodes( notesNode, "notepad" );
      if ( getLog().isDebug() ) {
        // "I found "+nr+" notepads to paste."
        getLog().logDebug( BaseMessages.getString( PKG, "Spoon.Log.FoundNotepads", "" + nr ) );
      }
      NotePadMeta[] notes = new NotePadMeta[nr];

      for ( int i = 0; i < notes.length; i++ ) {
        Node noteNode = XMLHandler.getSubNodeByNr( notesNode, "notepad", i );
        notes[i] = new NotePadMeta( noteNode );
        Point p = notes[i].getLocation();
        notes[i].setLocation( p.x + offset.x, p.y + offset.y );
        transMeta.addNote( notes[i] );
        notes[i].setSelected( true );
      }

      // Set the source and target steps ...
      for ( StepMeta step : steps ) {
        StepMetaInterface smi = step.getStepMetaInterface();
        smi.searchInfoAndTargetSteps( transMeta.getSteps() );
      }

      // Set the error handling hops
      Node errorHandlingNode = XMLHandler.getSubNode( transNode, TransMeta.XML_TAG_STEP_ERROR_HANDLING );
      int nrErrorHandlers = XMLHandler.countNodes( errorHandlingNode, StepErrorMeta.XML_ERROR_TAG );
      for ( int i = 0; i < nrErrorHandlers; i++ ) {
        Node stepErrorMetaNode = XMLHandler.getSubNodeByNr( errorHandlingNode, StepErrorMeta.XML_ERROR_TAG, i );
        StepErrorMeta stepErrorMeta =
          new StepErrorMeta( transMeta.getParentVariableSpace(), stepErrorMetaNode, transMeta.getSteps() );

        // Handle pasting multiple times, need to update source and target step names
        int srcStepPos = stepOldNames.indexOf( stepErrorMeta.getSourceStep().getName() );
        int tgtStepPos = stepOldNames.indexOf( stepErrorMeta.getTargetStep().getName() );
        StepMeta sourceStep = transMeta.findStep( steps[srcStepPos].getName() );
        if ( sourceStep != null ) {
          sourceStep.setStepErrorMeta( stepErrorMeta );
        }
        sourceStep.setStepErrorMeta( null );
        if ( tgtStepPos >= 0 ) {
          sourceStep.setStepErrorMeta( stepErrorMeta );
          StepMeta targetStep = transMeta.findStep( steps[tgtStepPos].getName() );
          stepErrorMeta.setSourceStep( sourceStep );
          stepErrorMeta.setTargetStep( targetStep );
        }
      }

      // Save undo information too...
      addUndoNew( transMeta, steps, position, false );

      int[] hopPos = new int[hops.length];
      for ( int i = 0; i < hops.length; i++ ) {
        hopPos[i] = transMeta.indexOfTransHop( hops[i] );
      }
      addUndoNew( transMeta, hops, hopPos, true );

      int[] notePos = new int[notes.length];
      for ( int i = 0; i < notes.length; i++ ) {
        notePos[i] = transMeta.indexOfNote( notes[i] );
      }
      addUndoNew( transMeta, notes, notePos, true );

      if ( transMeta.haveStepsChanged() ) {
        refreshTree();
        refreshGraph();
      }
    } catch ( KettleException e ) {
      // "Error pasting steps...",
      // "I was unable to paste steps to this transformation"
      new ErrorDialog( shell, BaseMessages.getString( PKG, "Spoon.Dialog.UnablePasteSteps.Title" ), BaseMessages
        .getString( PKG, "Spoon.Dialog.UnablePasteSteps.Message" ), e );
    }
  }

  public void copySelected( TransMeta transMeta, List<StepMeta> steps, List<NotePadMeta> notes ) {
    if ( steps == null || steps.size() == 0 ) {
      return;
    }

    if ( RepositorySecurityUI.verifyOperations( shell, rep,
        RepositoryOperation.MODIFY_TRANSFORMATION, RepositoryOperation.EXECUTE_TRANSFORMATION ) ) {
      return;
    }

    StringBuilder xml = new StringBuilder( 5000 ).append( XMLHandler.getXMLHeader() );
    try {
      xml.append( XMLHandler.openTag( Spoon.XML_TAG_TRANSFORMATION_STEPS ) ).append( Const.CR );

      xml.append( XMLHandler.openTag( Spoon.XML_TAG_STEPS ) ).append( Const.CR );
      for ( StepMeta step : steps ) {
        xml.append( step.getXML() );
      }
      xml.append( XMLHandler.closeTag( Spoon.XML_TAG_STEPS ) ).append( Const.CR );

      // Also check for the hops in between the selected steps...
      xml.append( XMLHandler.openTag( TransMeta.XML_TAG_ORDER ) ).append( Const.CR );
      for ( StepMeta step1 : steps ) {
        for ( StepMeta step2 : steps ) {
          if ( step1 != step2 ) {
            TransHopMeta hop = transMeta.findTransHop( step1, step2, true );
            if ( hop != null ) {
              // Ok, we found one...
              xml.append( hop.getXML() ).append( Const.CR );
            }
          }
        }
      }
      xml.append( XMLHandler.closeTag( TransMeta.XML_TAG_ORDER ) ).append( Const.CR );

      xml.append( XMLHandler.openTag( TransMeta.XML_TAG_NOTEPADS ) ).append( Const.CR );
      if ( notes != null ) {
        for ( NotePadMeta note : notes ) {
          xml.append( note.getXML() );
        }
      }
      xml.append( XMLHandler.closeTag( TransMeta.XML_TAG_NOTEPADS ) ).append( Const.CR );

      xml.append( XMLHandler.openTag( TransMeta.XML_TAG_STEP_ERROR_HANDLING ) ).append( Const.CR );
      for ( StepMeta step : steps ) {
        if ( step.getStepErrorMeta() != null ) {
          xml.append( step.getStepErrorMeta().getXML() ).append( Const.CR );
        }
      }
      xml.append( XMLHandler.closeTag( TransMeta.XML_TAG_STEP_ERROR_HANDLING ) ).append( Const.CR );

      xml.append( XMLHandler.closeTag( Spoon.XML_TAG_TRANSFORMATION_STEPS ) ).append( Const.CR );

      toClipboard( xml.toString() );
    } catch ( Exception ex ) {
      new ErrorDialog( getShell(), "Error", "Error encoding to XML", ex );
    }
  }

  public void editHop( TransMeta transMeta, TransHopMeta transHopMeta ) {
    // Backup situation BEFORE edit:
    String name = transHopMeta.toString();
    TransHopMeta before = (TransHopMeta) transHopMeta.clone();

    TransHopDialog hd = new TransHopDialog( shell, SWT.NONE, transHopMeta, transMeta );
    if ( hd.open() != null ) {
      // Backup situation for redo/undo:
      TransHopMeta after = (TransHopMeta) transHopMeta.clone();
      addUndoChange( transMeta, new TransHopMeta[] { before }, new TransHopMeta[] { after }, new int[] { transMeta
        .indexOfTransHop( transHopMeta ) } );

      String newName = transHopMeta.toString();
      if ( !name.equalsIgnoreCase( newName ) ) {
        refreshTree();
        refreshGraph(); // color, nr of copies...
      }
    }
    setShellText();
  }

  public void delHop( TransMeta transMeta, TransHopMeta transHopMeta ) {
    int index = transMeta.indexOfTransHop( transHopMeta );
    addUndoDelete( transMeta, new Object[] { (TransHopMeta) transHopMeta.clone() }, new int[] { index } );
    transMeta.removeTransHop( index );

    StepMeta fromStepMeta = transHopMeta.getFromStep();
    StepMeta beforeFrom = (StepMeta) fromStepMeta.clone();
    int indexFrom = transMeta.indexOfStep( fromStepMeta );

    StepMeta toStepMeta = transHopMeta.getToStep();
    StepMeta beforeTo = (StepMeta) toStepMeta.clone();
    int indexTo = transMeta.indexOfStep( toStepMeta );

    boolean stepFromNeedAddUndoChange = fromStepMeta.getStepMetaInterface()
      .cleanAfterHopFromRemove( transHopMeta.getToStep() );
    boolean stepToNeedAddUndoChange = toStepMeta.getStepMetaInterface().cleanAfterHopToRemove( fromStepMeta );

    // If this is an error handling hop, disable it
    //
    if ( transHopMeta.getFromStep().isDoingErrorHandling() ) {
      StepErrorMeta stepErrorMeta = fromStepMeta.getStepErrorMeta();

      // We can only disable error handling if the target of the hop is the same as the target of the error handling.
      //
      if ( stepErrorMeta.getTargetStep() != null
        && stepErrorMeta.getTargetStep().equals( transHopMeta.getToStep() ) ) {

        // Only if the target step is where the error handling is going to...
        //
        stepErrorMeta.setEnabled( false );
        stepFromNeedAddUndoChange = true;
      }
    }

    if ( stepFromNeedAddUndoChange ) {
      addUndoChange( transMeta, new Object[] { beforeFrom }, new Object[] { fromStepMeta }, new int[] { indexFrom } );
    }

    if ( stepToNeedAddUndoChange ) {
      addUndoChange( transMeta, new Object[] { beforeTo }, new Object[] { toStepMeta }, new int[] { indexTo } );
    }

    refreshTree();
    refreshGraph();
  }

  public void newHop( TransMeta transMeta, StepMeta fr, StepMeta to ) {
    TransHopMeta hi = new TransHopMeta( fr, to );

    TransHopDialog hd = new TransHopDialog( shell, SWT.NONE, hi, transMeta );
    if ( hd.open() != null ) {
      newHop( transMeta, hi );
    }
  }

  public void newHop( TransMeta transMeta, TransHopMeta transHopMeta ) {
    if ( checkIfHopAlreadyExists( transMeta, transHopMeta ) ) {
      transMeta.addTransHop( transHopMeta );
      int idx = transMeta.indexOfTransHop( transHopMeta );

      if ( !performNewTransHopChecks( transMeta, transHopMeta ) ) {
        // Some error occurred: loops, existing hop, etc.
        // Remove it again...
        //
        transMeta.removeTransHop( idx );
      } else {
        addUndoNew( transMeta, new TransHopMeta[] { transHopMeta }, new int[] { transMeta
          .indexOfTransHop( transHopMeta ) } );
      }

      // Just to make sure
      transHopMeta.getFromStep().drawStep();
      transHopMeta.getToStep().drawStep();

      refreshTree();
      refreshGraph();
    }
  }

  /**
   * @param transMeta transformation's meta
   * @param newHop hop to be checked
   * @return true when the hop was added, false if there was an error
   */
  public boolean checkIfHopAlreadyExists( TransMeta transMeta, TransHopMeta newHop ) {
    boolean ok = true;
    if ( transMeta.findTransHop( newHop.getFromStep(), newHop.getToStep() ) != null ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.HopExists.Message" ) ); // "This hop already exists!"
      mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.HopExists.Title" ) ); // Error!
      mb.open();
      ok = false;
    }

    return ok;
  }

  /**
   * @param transMeta transformation's meta
   * @param newHop hop to be checked
   * @return true when the hop was added, false if there was an error
   */
  public boolean performNewTransHopChecks( TransMeta transMeta, TransHopMeta newHop ) {
    boolean ok = true;

    if ( transMeta.hasLoop( newHop.getToStep() ) ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "TransGraph.Dialog.HopCausesLoop.Message" ) );
      mb.setText( BaseMessages.getString( PKG, "TransGraph.Dialog.HopCausesLoop.Title" ) );
      mb.open();
      ok = false;
    }

    if ( ok ) { // only do the following checks, e.g. checkRowMixingStatically
      // when not looping, otherwise we get a loop with
      // StackOverflow there ;-)
      try {
        if ( !newHop.getToStep().getStepMetaInterface().excludeFromRowLayoutVerification() ) {
          transMeta.checkRowMixingStatically( newHop.getToStep(), null );
        }
      } catch ( KettleRowException re ) {
        // Show warning about mixing rows with conflicting layouts...
        new ErrorDialog(
          shell, BaseMessages.getString( PKG, "TransGraph.Dialog.HopCausesRowMixing.Title" ), BaseMessages
            .getString( PKG, "TransGraph.Dialog.HopCausesRowMixing.Message" ), re );
      }

      verifyCopyDistribute( transMeta, newHop.getFromStep() );
    }

    return ok;
  }

  public void verifyCopyDistribute( TransMeta transMeta, StepMeta fr ) {
    List<StepMeta> nextSteps = transMeta.findNextSteps( fr );
    int nrNextSteps = nextSteps.size();

    // don't show it for 3 or more hops, by then you should have had the
    // message
    if ( nrNextSteps == 2 ) {
      boolean distributes = fr.getStepMetaInterface().excludeFromCopyDistributeVerification();
      boolean customDistribution = false;

      if ( props.showCopyOrDistributeWarning()
        && !fr.getStepMetaInterface().excludeFromCopyDistributeVerification() ) {
        MessageDialogWithToggle md =
          new MessageDialogWithToggle(
            shell, BaseMessages.getString( PKG, "System.Warning" ), null, BaseMessages.getString(
              PKG, "Spoon.Dialog.CopyOrDistribute.Message", fr.getName(), Integer.toString( nrNextSteps ) ),
            MessageDialog.WARNING, getRowDistributionLabels(), 0, BaseMessages.getString(
              PKG, "Spoon.Message.Warning.NotShowWarning" ), !props.showCopyOrDistributeWarning() );
        MessageDialogWithToggle.setDefaultImage( GUIResource.getInstance().getImageSpoon() );
        int idx = md.open();
        props.setShowCopyOrDistributeWarning( !md.getToggleState() );
        props.saveProps();

        distributes = idx == Spoon.MESSAGE_DIALOG_WITH_TOGGLE_YES_BUTTON_ID;
        customDistribution = idx == Spoon.MESSAGE_DIALOG_WITH_TOGGLE_CUSTOM_DISTRIBUTION_BUTTON_ID;
      }

      if ( distributes ) {
        fr.setDistributes( true );
        fr.setRowDistribution( null );
      } else if ( customDistribution ) {

        RowDistributionInterface rowDistribution = getActiveTransGraph().askUserForCustomDistributionMethod();

        fr.setDistributes( true );
        fr.setRowDistribution( rowDistribution );
      } else {
        fr.setDistributes( false );
        fr.setDistributes( false );
      }

      refreshTree();
      refreshGraph();
    }
  }

  private String[] getRowDistributionLabels() {
    ArrayList<String> labels = new ArrayList<>();
    labels.add( BaseMessages.getString( PKG, "Spoon.Dialog.CopyOrDistribute.Distribute" ) );
    labels.add( BaseMessages.getString( PKG, "Spoon.Dialog.CopyOrDistribute.Copy" ) );
    if ( PluginRegistry.getInstance().getPlugins( RowDistributionPluginType.class ).size() > 0 ) {
      labels.add( BaseMessages.getString( PKG, "Spoon.Dialog.CopyOrDistribute.CustomRowDistribution" ) );
    }
    return labels.toArray( new String[labels.size()] );
  }

  public void newHop( TransMeta transMeta ) {
    newHop( transMeta, null, null );
  }

  public void openRepository() {
    // Check to tabs are dirty and warn user that they must save tabs prior to connecting.  Don't connect!
    if ( Spoon.getInstance().isTabsChanged() ) {
      MessageBox mb = new MessageBox( Spoon.getInstance().getShell(), SWT.OK );
      mb.setMessage(  BaseMessages.getString( PKG, "Spoon.Dialog.WarnToSaveAllPriorToConnect.Message" ) );
      mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.WarnToCloseAllForce.Disconnect.Title" ) );
      mb.open();

      // Don't connect, user will need to save all their dirty tabs.
      return;
    }

    loginDialog = new RepositoriesDialog( shell, null, new ILoginCallback() {

      @Override
      public void onSuccess( Repository repository ) {
        // Close previous repository...
        if ( rep != null ) {
          rep.disconnect();
          SpoonPluginManager.getInstance().notifyLifecycleListeners( SpoonLifeCycleEvent.REPOSITORY_DISCONNECTED );
        }
        setRepository( repository );

        loadSessionInformation( repository, true );

        refreshTree();
        setShellText();
        SpoonPluginManager.getInstance().notifyLifecycleListeners( SpoonLifeCycleEvent.REPOSITORY_CONNECTED );
      }

      @Override
      public void onError( Throwable t ) {
        closeRepository();
        onLoginError( t );
      }

      @Override
      public void onCancel() {

      }
    } );
    loginDialog.show();
  }

  private void loadSessionInformation( Repository repository, boolean saveOldDatabases ) {

    JobMeta[] jobMetas = getLoadedJobs();
    for ( JobMeta jobMeta : jobMetas ) {
      for ( int i = 0; i < jobMeta.nrDatabases(); i++ ) {
        jobMeta.getDatabase( i ).setObjectId( null );
      }

      // Set for the existing job the ID at -1!
      jobMeta.setObjectId( null );

      // Keep track of the old databases for now.
      List<DatabaseMeta> oldDatabases = jobMeta.getDatabases();

      // In order to re-match the databases on name (not content), we
      // need to load the databases from the new repository.
      // NOTE: for purposes such as DEVELOP - TEST - PRODUCTION
      // cycles.

      // first clear the list of databases and slave servers
      jobMeta.setDatabases( new ArrayList<DatabaseMeta>() );
      jobMeta.setSlaveServers( new ArrayList<SlaveServer>() );

      // Read them from the new repository.
      try {
        SharedObjects sharedObjects =
          repository != null ? repository.readJobMetaSharedObjects( jobMeta ) : jobMeta.readSharedObjects();
        sharedObjectsFileMap.put( sharedObjects.getFilename(), sharedObjects );

      } catch ( KettleException e ) {
        new ErrorDialog(
          shell, BaseMessages.getString( PKG, "Spoon.Dialog.ErrorReadingSharedObjects.Title" ), BaseMessages
          .getString( PKG, "Spoon.Dialog.ErrorReadingSharedObjects.Message", makeTabName( jobMeta, true ) ),
          e
        );
      }

      // Then we need to re-match the databases at save time...
      for ( DatabaseMeta oldDatabase : oldDatabases ) {
        DatabaseMeta newDatabase = DatabaseMeta.findDatabase( jobMeta.getDatabases(), oldDatabase.getName() );

        // If it exists, change the settings...
        if ( newDatabase != null ) {
          //
          // A database connection with the same name exists in
          // the new repository.
          // Change the old connections to reflect the settings in
          // the new repository
          //
          oldDatabase.setDatabaseInterface( newDatabase.getDatabaseInterface() );
        } else {
          if ( saveOldDatabases ) {
            //
            // The old database is not present in the new
            // repository: simply add it to the list.
            // When the job gets saved, it will be added
            // to the repository.
            //
            jobMeta.addDatabase( oldDatabase );
          }
        }
      }

      if ( repository != null ) {
        try {
          // For the existing job, change the directory too:
          // Try to find the same directory in the new repository...
          RepositoryDirectoryInterface rdi =
            repository.findDirectory( jobMeta.getRepositoryDirectory().getPath() );
          if ( rdi != null && !rdi.getPath().equals( "/" ) ) {
            jobMeta.setRepositoryDirectory( rdi );
          } else {
            // the root is the default!
            jobMeta.setRepositoryDirectory( repository.loadRepositoryDirectoryTree() );
          }
        } catch ( KettleException ke ) {
          rep = null;
          new ErrorDialog(
            shell, BaseMessages.getString( PKG, "Spoon.Dialog.ErrorConnectingRepository.Title" ), BaseMessages
            .getString( PKG, "Spoon.Dialog.ErrorConnectingRepository.Message", Const.CR ), ke
          );
        }
      }
    }

    TransMeta[] transMetas = getLoadedTransformations();
    for ( TransMeta transMeta : transMetas ) {
      for ( int i = 0; i < transMeta.nrDatabases(); i++ ) {
        transMeta.getDatabase( i ).setObjectId( null );
      }

      // Set for the existing transformation the ID at -1!
      transMeta.setObjectId( null );

      // Keep track of the old databases for now.
      List<DatabaseMeta> oldDatabases = transMeta.getDatabases();

      // In order to re-match the databases on name (not content), we
      // need to load the databases from the new repository.
      // NOTE: for purposes such as DEVELOP - TEST - PRODUCTION
      // cycles.

      // first clear the list of databases, partition schemas, slave
      // servers, clusters
      transMeta.setDatabases( new ArrayList<DatabaseMeta>() );
      transMeta.setPartitionSchemas( new ArrayList<PartitionSchema>() );
      transMeta.setSlaveServers( new ArrayList<SlaveServer>() );
      transMeta.setClusterSchemas( new ArrayList<ClusterSchema>() );

      // Read them from the new repository.
      try {
        SharedObjects sharedObjects =
          repository != null ? repository.readTransSharedObjects( transMeta ) : transMeta.readSharedObjects();
        sharedObjectsFileMap.put( sharedObjects.getFilename(), sharedObjects );
      } catch ( KettleException e ) {
        new ErrorDialog(
          shell, BaseMessages.getString( PKG, "Spoon.Dialog.ErrorReadingSharedObjects.Title" ),
          BaseMessages.getString( PKG, "Spoon.Dialog.ErrorReadingSharedObjects.Message", makeTabName(
            transMeta, true ) ), e
        );
      }

      // Then we need to re-match the databases at save time...
      for ( DatabaseMeta oldDatabase : oldDatabases ) {
        DatabaseMeta newDatabase = DatabaseMeta.findDatabase( transMeta.getDatabases(), oldDatabase.getName() );

        // If it exists, change the settings...
        if ( newDatabase != null ) {
          //
          // A database connection with the same name exists in
          // the new repository.
          // Change the old connections to reflect the settings in
          // the new repository
          //
          oldDatabase.setDatabaseInterface( newDatabase.getDatabaseInterface() );
        } else {
          if ( saveOldDatabases ) {
            //
            // The old database is not present in the new
            // repository: simply add it to the list.
            // When the transformation gets saved, it will be added
            // to the repository.
            //
            transMeta.addDatabase( oldDatabase );
          }
        }
      }

      if ( repository != null ) {
        try {
          // For the existing transformation, change the directory too:
          // Try to find the same directory in the new repository...
          RepositoryDirectoryInterface rdi =
            repository.findDirectory( transMeta.getRepositoryDirectory().getPath() );
          if ( rdi != null && !rdi.getPath().equals( "/" ) ) {
            transMeta.setRepositoryDirectory( rdi );
          } else {
            // the root is the default!
            transMeta.setRepositoryDirectory( repository.loadRepositoryDirectoryTree() );
          }
        } catch ( KettleException ke ) {
          rep = null;
          new ErrorDialog(
            shell, BaseMessages.getString( PKG, "Spoon.Dialog.ErrorConnectingRepository.Title" ), BaseMessages
            .getString( PKG, "Spoon.Dialog.ErrorConnectingRepository.Message", Const.CR ), ke
          );
        }
      }
    }
  }

  public void clearSharedObjectCache() throws KettleException {
    if ( rep != null ) {
      rep.clearSharedObjectCache();
      TransMeta transMeta = getActiveTransformation();
      if ( transMeta != null ) {
        rep.readTransSharedObjects( transMeta );
      }
      JobMeta jobMeta = getActiveJob();
      if ( jobMeta != null ) {
        rep.readJobMetaSharedObjects( jobMeta );
      }
    }
  }

  public void exploreRepository() {
    if ( rep != null ) {
      final RepositoryExplorerCallback cb = new RepositoryExplorerCallback() {

        @Override
        public boolean open( UIRepositoryContent element, String revision ) throws Exception {
          String objName = element.getName();
          if ( objName != null ) {
            RepositoryObjectType objectType = element.getRepositoryElementType();
            RepositoryDirectory repDir = element.getRepositoryDirectory();
            if ( element.getObjectId() != null ) { // new way
              loadObjectFromRepository( element.getObjectId(), objectType, revision );
            } else { // old way
              loadObjectFromRepository( objName, objectType, repDir, revision );
            }
          }
          return false; // do not close explorer
        }

        @Override
        public boolean error( String message ) throws Exception {
          closeRepository();
          return true;
        }
      };

      try {
        final KettleWaitBox box = (KettleWaitBox) this.mainSpoonContainer.getDocumentRoot().createElement( "iconwaitbox" );
        box.setIndeterminate( true );
        box.setCanCancel( false );
        box.setIcon( "ui/images/kettle_logo_small.svg" );
        box.setTitle( BaseMessages.getString(
          RepositoryDialogInterface.class, "RepositoryExplorerDialog.Connection.Wait.Title" ) );
        box.setMessage( BaseMessages.getString(
          RepositoryDialogInterface.class, "RepositoryExplorerDialog.Explorer.Wait.Message" ) );
        box.setDialogParent( shell );
        box.setRunnable( new WaitBoxRunnable( box ) {
          @Override
          public void run() {

            shell.getDisplay().syncExec( new Runnable() {
              @Override
              public void run() {
                RepositoryExplorer explorer;
                try {
                  try {
                    explorer =
                        new RepositoryExplorer( shell, rep, cb, Variables.getADefaultVariableSpace(),
                            sharedObjectSyncUtil );
                  } catch ( final KettleRepositoryLostException krle ) {
                    handleRepositoryLost( krle );
                    closeRepository();
                    return;
                  } finally {
                    box.stop();
                  }

                  if ( explorer.isInitialized() ) {
                    explorer.show();
                  } else {
                    return;
                  }

                  explorer.dispose();

                } catch ( final Throwable e ) {
                  shell.getDisplay().asyncExec( new Runnable() {
                    @Override
                    public void run() {
                      new ErrorDialog( shell, BaseMessages.getString( PKG, "Spoon.Error" ), e.getMessage(), e );
                    }
                  } );
                }
              }
            } );
          }

          @Override
          public void cancel() {
          }

        } );
        box.start();
      } catch ( Throwable e ) {
        new ErrorDialog( shell, BaseMessages.getString( PKG, "Spoon.Error" ), e.getMessage(), e );
      }

    }
  }

  public void loadObjectFromRepository(
      ObjectId objectId, RepositoryObjectType objectType, String revision ) throws Exception {
    // Try to open the selected transformation.
    if ( objectType.equals( RepositoryObjectType.TRANSFORMATION ) ) {
      try {
        TransLoadProgressDialog progressDialog = new TransLoadProgressDialog( shell, rep, objectId, revision );
        TransMeta transMeta = progressDialog.open();
        transMeta.clearChanged();
        if ( transMeta != null ) {
          if ( log.isDetailed() ) {
            log.logDetailed( BaseMessages.getString(
              PKG, "Spoon.Log.LoadToTransformation", transMeta.getName(), transMeta
                .getRepositoryDirectory().getName() ) );
          }
          props.addLastFile( LastUsedFile.FILE_TYPE_TRANSFORMATION, transMeta.getName(), transMeta
            .getRepositoryDirectory().getPath(), true, rep.getName(), getUsername(), null, null );
          addMenuLast();
          addTransGraph( transMeta );
        }
        refreshTree();
        refreshGraph();
      } catch ( Exception e ) {
        if ( KettleRepositoryLostException.lookupStackStrace( e ) == null ) {
          new ErrorDialog( ( (Spoon) SpoonFactory.getInstance() ).getShell(), BaseMessages.getString(
            Spoon.class, "Spoon.Dialog.ErrorOpeningById.Message", objectId ), e.getMessage(), e );
        } else {
          throw e;
        }
      }
    } else if ( objectType.equals( RepositoryObjectType.JOB ) ) {
      try {
        JobLoadProgressDialog progressDialog = new JobLoadProgressDialog( shell, rep, objectId, revision );
        JobMeta jobMeta = progressDialog.open();
        jobMeta.clearChanged();
        if ( jobMeta != null ) {
          props.addLastFile( LastUsedFile.FILE_TYPE_JOB, jobMeta.getName(), jobMeta
            .getRepositoryDirectory().getPath(), true, rep.getName(), getUsername(), null, null );
          saveSettings();
          addMenuLast();
          addJobGraph( jobMeta );
        }
        refreshTree();
        refreshGraph();
      } catch ( Exception e ) {
        if ( KettleRepositoryLostException.lookupStackStrace( e ) == null ) {
          new ErrorDialog( ( (Spoon) SpoonFactory.getInstance() ).getShell(), BaseMessages.getString(
            Spoon.class, "Spoon.Dialog.ErrorOpeningById.Message", objectId ), e.getMessage(), e );
        } else {
          throw e;
        }
      }
    }
  }

  public void loadObjectFromRepository( String objName, RepositoryObjectType objectType,
    RepositoryDirectoryInterface repDir, String versionLabel ) throws Exception {
    // Try to open the selected transformation.
    if ( objectType.equals( RepositoryObjectType.TRANSFORMATION ) ) {
      try {
        TransLoadProgressDialog progressDialog =
          new TransLoadProgressDialog( shell, rep, objName, repDir, versionLabel );
        TransMeta transMeta = progressDialog.open();
        transMeta.clearChanged();
        if ( transMeta != null ) {
          if ( log.isDetailed() ) {
            log.logDetailed( BaseMessages.getString( PKG, "Spoon.Log.LoadToTransformation", objName, repDir
              .getName() ) );
          }
          props.addLastFile( LastUsedFile.FILE_TYPE_TRANSFORMATION, objName, repDir.getPath(), true, rep.getName(),
            getUsername(), null, null );
          addMenuLast();
          addTransGraph( transMeta );
        }
        refreshTree();
        refreshGraph();
      } catch ( Exception e ) {
        if ( KettleRepositoryLostException.lookupStackStrace( e ) == null ) {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
          mb.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.ErrorOpening.Message" )
            + objName + Const.CR + e.getMessage() ); // "Error opening : "
          mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.ErrorOpening.Title" ) );
          mb.open();
        } else {
          throw e;
        }
      }
    } else if ( objectType.equals( RepositoryObjectType.JOB ) ) {
      // Try to open the selected job.
      try {
        JobLoadProgressDialog progressDialog =
          new JobLoadProgressDialog( shell, rep, objName, repDir, versionLabel );
        JobMeta jobMeta = progressDialog.open();
        jobMeta.clearChanged();
        if ( jobMeta != null ) {
          props.addLastFile( LastUsedFile.FILE_TYPE_JOB, objName, repDir.getPath(), true, rep.getName(), getUsername(),
            null, null );
          saveSettings();
          addMenuLast();
          addJobGraph( jobMeta );
        }
        refreshTree();
        refreshGraph();
      } catch ( Exception e ) {
        if ( KettleRepositoryLostException.lookupStackStrace( e ) == null ) {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
          mb.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.ErrorOpening.Message" )
            + objName + Const.CR + e.getMessage() ); // "Error opening : "
          mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.ErrorOpening.Title" ) );
          mb.open();
        } else {
          throw e;
        }
      }
    }
  }

  public void closeRepository() {
    if ( rep != null ) {

      // Prompt and close all tabs as user disconnected from the repo
      boolean shouldDisconnect = Spoon.getInstance().closeAllJobsAndTransformations( true );
      if ( shouldDisconnect ) {
        loadSessionInformation( null, false );

        if ( rep != null ) {
          rep.disconnect();
        }

        setRepository( null );
        setShellText();
        SpoonPluginManager.getInstance().notifyLifecycleListeners( SpoonLifeCycleEvent.REPOSITORY_DISCONNECTED );
        enableMenus();
        updateTreeForActiveAbstractMetas();
        clearRepositoryDirectory();
      }
    }
  }

  public void openFile() {
    openFile( false );
  }

  public void openFile( boolean importfile ) {
    try {
      SpoonPerspective activePerspective = SpoonPerspectiveManager.getInstance().getActivePerspective();

      // In case the perspective wants to handle open/save itself, let it...
      //
      if ( !importfile ) {
        if ( activePerspective instanceof SpoonPerspectiveOpenSaveInterface ) {
          ( (SpoonPerspectiveOpenSaveInterface) activePerspective ).open();
          return;
        }
      }

      if ( rep == null || importfile ) { // Load from XML

        FileDialog dialog = new FileDialog( shell, SWT.OPEN );

        LinkedHashSet<String> extensions = new LinkedHashSet<>();
        LinkedHashSet<String> extensionNames = new LinkedHashSet<>();
        StringBuilder allExtensions = new StringBuilder();
        for ( FileListener l : fileListeners ) {
          for ( String ext : l.getSupportedExtensions() ) {
            extensions.add( "*." + ext );
            allExtensions.append( "*." ).append( ext ).append( ";" );
          }
          Collections.addAll( extensionNames, l.getFileTypeDisplayNames( Locale.getDefault() ) );
        }
        extensions.add( "*" );
        extensionNames.add( BaseMessages.getString( PKG, "Spoon.Dialog.OpenFile.AllFiles" ) );

        String[] exts = new String[extensions.size() + 1];
        exts[0] = allExtensions.toString();
        System.arraycopy( extensions.toArray( new String[extensions.size()] ), 0, exts, 1, extensions.size() );

        String[] extNames = new String[extensionNames.size() + 1];
        extNames[0] = BaseMessages.getString( PKG, "Spoon.Dialog.OpenFile.AllTypes" );
        System.arraycopy( extensionNames.toArray( new String[extensionNames.size()] ), 0, extNames, 1, extensionNames
            .size() );

        dialog.setFilterExtensions( exts );

        setFilterPath( dialog );
        String filename = dialog.open();
        if ( filename != null ) {

          if ( importfile ) {
            if ( activePerspective instanceof SpoonPerspectiveOpenSaveInterface ) {
              ( (SpoonPerspectiveOpenSaveInterface) activePerspective ).importFile( filename );
              return;
            }
          }

          lastDirOpened = dialog.getFilterPath();
          openFile( filename, importfile );
        }
      } else {
        try {
          FileDialogOperation fileDialogOperation =
            new FileDialogOperation( FileDialogOperation.OPEN, FileDialogOperation.ORIGIN_SPOON );
          fileDialogOperation.setProviderFilter( evaluateFileBrowserProviderFilter() );
          ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.SpoonOpenSaveNew.id,
            fileDialogOperation );
          if ( fileDialogOperation.getRepositoryObject() != null ) {
            RepositoryObject repositoryObject = (RepositoryObject) fileDialogOperation.getRepositoryObject();
            loadObjectFromRepository( repositoryObject.getObjectId(), repositoryObject.getObjectType(), null );
          }
        } catch ( Exception e ) {
         // Ignore
        }
      }
    } catch ( KettleRepositoryLostException krle ) {
      new ErrorDialog(
          getShell(),
          BaseMessages.getString( PKG, "Spoon.Error" ),
          krle.getPrefaceMessage(),
          krle );
      this.closeRepository();
    }
  }

  private void setFilterPath( FileDialog dialog ) {
    if ( !Utils.isEmpty( lastDirOpened ) ) {
      if ( new File( lastDirOpened ).exists() ) {
        dialog.setFilterPath( lastDirOpened );
      }
    }
  }

  private String lastFileOpened = null;

  public String getLastFileOpened() {
    if ( lastFileOpened == null ) {
      lastFileOpened = System.getProperty( "org.pentaho.di.defaultVFSPath", "" );
    }
    return lastFileOpened;
  }

  public void setLastFileOpened( String inLastFileOpened ) {
    lastFileOpened = inLastFileOpened;
  }

  public void displayCmdLine() {
    String cmdFile = getCmdLine();

    if ( Utils.isEmpty( cmdFile ) ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
      mb.setMessage( BaseMessages.getString( PKG, "ExportCmdLine.JobOrTransformationMissing.Message" ) );
      mb.setText( BaseMessages.getString( PKG, "ExportCmdLine.JobOrTransformationMissing.Title" ) );
      mb.open();
    } else {
      ShowBrowserDialog sbd =
        new ShowBrowserDialog( shell, BaseMessages.getString( PKG, "ExportCmdLine.CommandLine.Title" ), cmdFile );
      sbd.open();
    }
  }

  public void createCmdLineFile() {
    String cmdFile = getCmdLine();

    if ( Utils.isEmpty( cmdFile ) ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
      mb.setMessage( BaseMessages.getString( PKG, "ExportCmdLine.JobOrTransformationMissing.Message" ) );
      mb.setText( BaseMessages.getString( PKG, "ExportCmdLine.JobOrTransformationMissing.Title" ) );
      mb.open();
    } else {
      boolean export = true;

      FileDialog dialog = new FileDialog( shell, SWT.SAVE );
      dialog.setFilterExtensions( new String[] { "*.bat", ".sh", "*.*" } );
      dialog.setFilterNames( new String[] {
        BaseMessages.getString( PKG, "ExportCmdLine.BatFiles" ),
        BaseMessages.getString( PKG, "ExportCmdLineShFiles" ),
        BaseMessages.getString( PKG, "ExportCmdLine.AllFiles" ) } );
      String filename = dialog.open();

      if ( filename != null ) {
        // See if the file already exists...
        int id = SWT.YES;
        try {
          FileObject f = KettleVFS.getFileObject( filename );
          if ( f.exists() ) {
            MessageBox mb = new MessageBox( shell, SWT.NO | SWT.YES | SWT.ICON_WARNING );
            mb.setMessage( BaseMessages.getString( PKG, "ExportCmdLineShFiles.FileExistsReplace", filename ) );
            mb.setText( BaseMessages.getString( PKG, "ExportCmdLineShFiles.ConfirmOverwrite" ) );
            id = mb.open();
          }
        } catch ( Exception e ) {
          // Ignore errors
        }
        if ( id == SWT.NO ) {
          export = false;
        }

        if ( export ) {
          java.io.FileWriter out = null;
          try {
            out = new java.io.FileWriter( filename );
            out.write( cmdFile );
            out.flush();
          } catch ( Exception e ) {
            new ErrorDialog(
              shell, BaseMessages.getString( PKG, "ExportCmdLineShFiles.ErrorWritingFile.Title" ), BaseMessages
                .getString( PKG, "ExportCmdLineShFiles.ErrorWritingFile.Message", filename ), e );
          } finally {
            if ( out != null ) {
              try {
                out.close();
              } catch ( Exception e ) {
                // Ignore errors
              }
            }
          }

          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
          mb.setMessage( BaseMessages.getString( PKG, "ExportCmdLineShFiles.CmdExported.Message", filename ) );
          mb.setText( BaseMessages.getString( PKG, "ExportCmdLineShFiles.CmdExported.Title" ) );
          mb.open();
        }
      }
    }
  }

  private String getCmdLine() {
    TransMeta transMeta = getActiveTransformation();
    JobMeta jobMeta = getActiveJob();
    String cmdFile = "";

    if ( rep != null && ( jobMeta != null || transMeta != null ) ) {
      if ( jobMeta != null ) {
        if ( jobMeta.getName() != null ) {
          if ( Const.isWindows() ) {
            cmdFile =
              "kitchen "
                + "/rep:\"" + rep.getName() + "\"" + " /user:\""
                + ( rep.getUserInfo() != null ? rep.getUserInfo().getLogin() : "" ) + "\"" + " /pass:\""
                + Encr.encryptPasswordIfNotUsingVariables( rep.getUserInfo().getPassword() ) + "\""
                + " /job:\"" + jobMeta.getName() + '"' + " /dir:\""
                + jobMeta.getRepositoryDirectory().getPath() + "\"" + " /level:Basic";
          } else {
            cmdFile =
              "sh kitchen.sh "
                + "-rep='"
                + rep.getName()
                + "'"
                + " -user='"
                + ( rep.getUserInfo() != null ? rep.getUserInfo().getLogin() : "" )
                + "'"
                + " -pass='"
                + Encr.encryptPasswordIfNotUsingVariables( rep.getUserInfo() != null ? rep
                  .getUserInfo().getPassword() : "" ) + "'" + " -job='" + jobMeta.getName() + "'"
                + " -dir='" + jobMeta.getRepositoryDirectory().getPath() + "'" + " -level=Basic";
          }
        }
      } else {
        if ( transMeta.getName() != null ) {
          if ( Const.isWindows() ) {
            cmdFile =
              "pan "
                + "/rep:\""
                + rep.getName()
                + "\""
                + " /user:\""
                + ( rep.getUserInfo() != null ? rep.getUserInfo().getLogin() : "" )
                + "\""
                + " /pass:\""
                + Encr.encryptPasswordIfNotUsingVariables( rep.getUserInfo() != null ? rep
                  .getUserInfo().getPassword() : "" ) + "\"" + " /trans:\"" + transMeta.getName() + "\""
                + " /dir:\"" + transMeta.getRepositoryDirectory().getPath() + "\"" + " /level:Basic";
          } else {
            cmdFile =
              "sh pan.sh "
                + "-rep='"
                + rep.getName()
                + "'"
                + " -user='"
                + ( rep.getUserInfo() != null ? rep.getUserInfo().getLogin() : "" )
                + "'"
                + " -pass='"
                + Encr.encryptPasswordIfNotUsingVariables( rep.getUserInfo() != null ? rep
                  .getUserInfo().getPassword() : "" ) + "'" + " -trans='" + transMeta.getName() + "'"
                + " -dir='" + transMeta.getRepositoryDirectory().getPath() + "'" + " -level=Basic";
          }
        }
      }
    } else if ( rep == null && ( jobMeta != null || transMeta != null ) ) {
      if ( jobMeta != null ) {
        if ( jobMeta.getFilename() != null ) {
          if ( Const.isWindows() ) {
            cmdFile = "kitchen " + "/file:\"" + jobMeta.getFilename() + "\"" + " /level:Basic";
          } else {
            cmdFile = "sh kitchen.sh " + "-file='" + jobMeta.getFilename() + "'" + " -level=Basic";
          }
        }
      } else {
        if ( transMeta.getFilename() != null ) {
          if ( Const.isWindows() ) {
            cmdFile = "pan " + "/file:\"" + transMeta.getFilename() + "\"" + " /level:Basic";
          } else {
            cmdFile = "sh pan.sh " + "-file:'" + transMeta.getFilename() + "'" + " -level=Basic";
          }
        }
      }
    }
    return cmdFile;

  }

  public void openFileVFSFile() {
    FileObject initialFile;
    FileObject rootFile;
    try {
      initialFile = KettleVFS.getFileObject( getLastFileOpened() );
      rootFile = initialFile.getFileSystem().getRoot();
    } catch ( Exception e ) {
      String message = Const.getStackTracker( e );
      new ErrorDialog( shell, BaseMessages.getString( PKG, "Spoon.Error" ), message, e );

      return;
    }

    FileObject selectedFile =
      getVfsFileChooserDialog( rootFile, initialFile ).open(
        shell, null, Const.STRING_TRANS_AND_JOB_FILTER_EXT, Const.getTransformationAndJobFilterNames(),
        VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE );
    if ( selectedFile != null ) {
      setLastFileOpened( selectedFile.getName().getFriendlyURI() );
      openFile( selectedFile.getName().getFriendlyURI(), rep != null );
    }
  }

  /**
   * Separate VFS connection name variable is no longer needed.
   * @deprecated
   * The connection name is in the URI since full {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME } paths are being used.
   */
  @Deprecated
  private String lastFileOpenedConnection;
  private String lastFileOpenedProvider;

  public void openFileVFSFileNew() {
    try {
      openFileNew();
    } catch ( Exception ignored ) {
      // Show an error message
    }
  }

  /**
   * Used when importing content from an xml file. We only expect to import the content from local
   * or from a vfs store
   * @throws Exception
   */
  public void importFileFromXML() throws Exception {
    openFileNew(ProviderFilterType.LOCAL.toString() + "," +  ProviderFilterType.VFS
        , FilterType.XML + "," + FilterType.KETTLE_TRANS
            + "," + FilterType.KETTLE_JOB + "," + FilterType.ALL , true, FileDialogOperation.IMPORT );
  }

  private String evaluateFileBrowserProviderFilter()  {
    String providerFilter = ProviderFilterType.ALL_PROVIDERS.toString();
    // Check if we are connected to the repository, then only give option to either load from
    // repository or from recent list

    if ( Spoon.getInstance() != null && Spoon.getInstance().rep != null ) {
      providerFilter = ProviderFilterType.REPOSITORY + "," + ProviderFilterType.RECENTS;
    }
    return providerFilter;
  }

  public void openFileNew() throws Exception {
    openFileNew( evaluateFileBrowserProviderFilter(), FilterType.KETTLE_FILES.toString()  + "," + FilterType.ALL.toString(), false
		, FileDialogOperation.OPEN );
  }

  public void openFileNew( String providerFilter, String fileFilterType, boolean importFile, String command ) throws Exception {
    FileDialogOperation fileDialogOperation =
            getFileDialogOperation( command, FileDialogOperation.ORIGIN_SPOON );
    fileDialogOperation.setProviderFilter( providerFilter );
    if ( !Utils.isEmpty( lastFileOpened ) ) {
      // Test for Windows vs Linux/Remote parent path
      int parentIndex = lastFileOpened.lastIndexOf( '\\' );
      if ( parentIndex == -1 ) {
        parentIndex = lastFileOpened.lastIndexOf( '/' );
      }

      if ( parentIndex > 0 ) {
        // We are able to find the index of forward or backward slash set the folder to be the path before slash
        String folder = lastFileOpened.substring( 0, parentIndex );
        fileDialogOperation.setPath( folder );
        fileDialogOperation.setConnection( null );
        fileDialogOperation.setProvider( lastFileOpenedProvider );
      } else {
        // We were unable to find the folder path from the last file opened. We will set the file open dialog to
        // default to user's home
        defaultFileDialogOperationToUserHome( fileDialogOperation );
      }
    } else if ( getActiveMeta() != null ) {
      // There is an opened file, lets set that as the file open browser location
      setFileOperatioPathForNonRepositoryFile( fileDialogOperation, getActiveMeta(), false );
    } else {
      // Unable to find last open file so setting the file open browser to user's home
      defaultFileDialogOperationToUserHome( fileDialogOperation );
    }
    try {
      fileDialogOperation.setFilter( fileFilterType );
      fileDialogOperation.setDefaultFilter( FilterType.KETTLE_FILES.toString()  + "," + FilterType.ALL.toString() );
      ExtensionPointHandler.callExtensionPoint( getLog(), KettleExtensionPoint.SpoonOpenSaveNew.id,
              fileDialogOperation );
      String path = fileDialogOperation.getPath();
      if ( fileDialogOperation.getRepositoryObject() != null ) {
        RepositoryObject repositoryObject = (RepositoryObject) fileDialogOperation.getRepositoryObject();
        loadObjectFromRepository( repositoryObject.getObjectId(), repositoryObject.getObjectType(), null );
        lastFileOpened = repositoryObject.getRepositoryDirectory().getPath() + "/" + repositoryObject.getName();
        lastFileOpenedProvider = fileDialogOperation.getProvider();
      } else if ( path != null ) {
        Variables variables = new Variables();
        variables.setVariable( CONNECTION, fileDialogOperation.getConnection() );
        openFile( path, variables, importFile );
        lastFileOpened = path;
        props.setLastUsedLocalFile( path );
        lastFileOpenedConnection = null;
        lastFileOpenedProvider = fileDialogOperation.getProvider();
      }
    } catch ( KettleException e ) {
      // Ignore for now
    }
  }

  private String deriveFileFilterFromFileType( String fileType ) {
    if (fileType != null) {
      if (fileType.equalsIgnoreCase(FileDialogOperation.TRANSFORMATION)
              || fileType.equalsIgnoreCase(LastUsedFile.FILE_TYPE_TRANSFORMATION)) {
        return FilterType.KETTLE_TRANS.toString();
      } else if (fileType.equalsIgnoreCase(FileDialogOperation.JOB)
              || fileType.equalsIgnoreCase(LastUsedFile.FILE_TYPE_JOB)) {
        return FilterType.KETTLE_JOB.toString();
      }
    }
    return fileType;
  }

  public boolean saveAsNew() {
    return saveAsNew( null, false, FileDialogOperation.SAVE_AS );
  }

  public boolean saveAsNew( EngineMetaInterface inputMeta, boolean export, String command) {
    EngineMetaInterface meta = inputMeta;
    if ( meta == null ) {
      meta = getActiveMeta();
    }
    String fileType = meta.getFileType().equals( LastUsedFile.FILE_TYPE_TRANSFORMATION )
            ? FileDialogOperation.TRANSFORMATION : FileDialogOperation.JOB;

    return saveAsNew( meta, export, evaluateFileBrowserProviderFilter(), fileType, command );
  }

  private void setFileOperatioPathForNonRepositoryFile(FileDialogOperation fileDialogOperation
          , EngineMetaInterface meta, boolean export) {
    // Check if user is exporting a file
    if (export && !Utils.isEmpty( lastFileOpenedProvider ) && lastFileOpenedProvider
        .equalsIgnoreCase(ProviderFilterType.REPOSITORY.toString())) {
      // Sine the last opened path is from repository and user can only export to local or vfs, set the
      // path to the user's home folder
      fileDialogOperation.setPath(userHomeDir);
      fileDialogOperation.setProvider(ProviderFilterType.LOCAL.toString());
    } else {
      if ( !StringUtils.isEmpty(meta.getFilename() ) ) {
        // The file exist and the user has invoked as SaveAs operation. Set the path to the folder the current file exist
        String pathSplitter = meta.getFilename().contains("/") ? "/" : "\\";
        fileDialogOperation
                .setPath(meta.getFilename().substring(0, meta.getFilename().lastIndexOf(pathSplitter)));
      } else if ( !StringUtils.isEmpty( meta.getName() ) ) {
        // This is the first time user is saving this file.
        if ( !Utils.isEmpty( lastFileOpenedProvider )  && lastFileOpenedProvider
            .equalsIgnoreCase( ProviderFilterType.REPOSITORY.toString() ) && rep == null ) {
          // User has not opened any file but the lastOpenProvier was repository and use is not connected to the
          // repository so set the session to the user's home folder
          defaultFileDialogOperationToUserHome( fileDialogOperation );
        } else if ( !Utils.isEmpty( lastFileOpened ) ) {
          //User has opened a file previously, set the save folder be the last file opened folder
          int parentIndex = lastFileOpened.lastIndexOf('\\');
          if (parentIndex == -1) {
            parentIndex = lastFileOpened.lastIndexOf('/');
          }
          String folder = lastFileOpened.substring(0, parentIndex);
          fileDialogOperation.setPath( folder );
          fileDialogOperation.setProvider( lastFileOpenedProvider );
        } else {
          //User has not opened any file so set the session to the user's home folder
          defaultFileDialogOperationToUserHome( fileDialogOperation );
        }
      }
    }
  }

  private void defaultFileDialogOperationToUserHome( FileDialogOperation fileDialogOperation ) {
    fileDialogOperation.setPath(userHomeDir);
    fileDialogOperation.setProvider(ProviderFilterType.LOCAL.toString());
  }
  public boolean saveAsNew( EngineMetaInterface meta, boolean export, String providerFilter
          , String fileFilterType, String command ) {
    FileDialogOperation fileDialogOperation =
            getFileDialogOperation( command, FileDialogOperation.ORIGIN_SPOON );
    fileDialogOperation.setFileType( fileFilterType );
    fileDialogOperation.setFilter( deriveFileFilterFromFileType( fileFilterType ) );
    fileDialogOperation.setFilename( meta.getName() );
    fileDialogOperation.setProviderFilter( providerFilter );
    if ( !export && rep != null && meta.getRepositoryDirectory() != null ) {
      setFileOperatioPathForRepositoryFile( fileDialogOperation, meta );
    } else {
      setFileOperatioPathForNonRepositoryFile(fileDialogOperation, meta, export );
    }
    if ( meta instanceof VariableSpace ) {
      fileDialogOperation.setConnection( null );
    }
    boolean saved = false;
    try {
      ExtensionPointHandler.callExtensionPoint( getLog(), KettleExtensionPoint.SpoonOpenSaveNew.id,
              fileDialogOperation );
      if ( meta instanceof VariableSpace && fileDialogOperation.getConnection() != null ) {
        ( (VariableSpace) meta ).setVariable( CONNECTION, fileDialogOperation.getConnection() );
      }
      if ( !export && fileDialogOperation.getRepositoryObject() != null ) {
        saved = performRepoSave( meta, fileFilterType, fileDialogOperation );
      } else if ( fileDialogOperation.getPath() != null && fileDialogOperation.getFilename() != null ) {
        String filename = fileDialogOperation.getPath() + File.separator + fileDialogOperation.getFilename();
        lastFileOpened = filename;
        props.setLastUsedLocalFile( filename );
        lastFileOpenedConnection = null;
        lastFileOpenedProvider = fileDialogOperation.getProvider();
        if ( lastFileOpenedConnection != null && meta instanceof VariableSpace ) {
          ( (VariableSpace) meta ).setVariable( CONNECTION, lastFileOpenedConnection );
        }
        saved = saveXMLFile( meta, filename, export );
      }
    } catch ( KettleException e ) {
      return false;
    }
    if ( saved ) {
      delegates.tabs.renameTabs();
    }

    return saved;
  }

  private void setFileOperatioPathForRepositoryFile( FileDialogOperation fileDialogOperation, EngineMetaInterface meta ) {

    List<LastUsedFile> lastUsedFileList = getLastUsedRepoFiles();

    if ( !Utils.isEmpty( lastUsedFileList ) ) {
      fileDialogOperation.setPath( lastUsedFileList.get( 0 ).getDirectory() );
    } else {
      fileDialogOperation.setPath( meta.getRepositoryDirectory().getPath() );
    }
  }

  private List<LastUsedFile> getLastUsedRepoFiles() {
    String repoAndUser = getRepoAndUser();
    return props.getLastUsedRepoFiles().getOrDefault( repoAndUser, Collections.emptyList() );
  }

  private String getRepoAndUser() {
    String username = getRepository().getUserInfo() != null ? getRepository().getUserInfo().getLogin() : "";
    return getRepository().getName() + ":" + username;
  }

  private boolean performRepoSave( EngineMetaInterface meta, String fileType, FileDialogOperation fileDialogOperation ) throws KettleException {
    boolean saved = false;
    RepositoryObject repositoryObject = (RepositoryObject) fileDialogOperation.getRepositoryObject();
     if ( canRepoSaveOrOverwrite( meta, fileType, repositoryObject ) ) {
      final String originalName = meta.getName();
      final ObjectId originalObjectId = meta.getObjectId();
      final String originalFilename = meta.getFilename();
      meta.setObjectId( null );
      meta.setFilename( null );
      meta.setRepositoryDirectory( repositoryObject.getRepositoryDirectory() );
      meta.setName( repositoryObject.getName() );
      saved = saveToRepositoryConfirmed( meta );
      if ( !saved ) {
        // if the object wasn't successfully saved, set the name and directory back to their original values
        meta.setRepositoryDirectory( repositoryObject.getRepositoryDirectory() );
        meta.setName( originalName );
        meta.setObjectId( originalObjectId );
        meta.setFilename( originalFilename );
      }
    }
    return saved;
  }

  private boolean canRepoSaveOrOverwrite( EngineMetaInterface meta, String fileType, RepositoryObject repositoryObject ) throws KettleException {
    if ( rep.exists( repositoryObject.getName(), repositoryObject.getRepositoryDirectory(), StringUtils.equals( FileDialogOperation.TRANSFORMATION, fileType ) ? RepositoryObjectType.TRANSFORMATION : RepositoryObjectType.JOB ) ) {
      MessageBox mb = new MessageBox( shell, SWT.NO | SWT.YES | SWT.ICON_WARNING );
      // "This file already exists.  Do you want to overwrite it?"
      mb.setMessage( BaseMessages.getString( PKG, SPOON_DIALOG_PROMPT_OVERWRITE_FILE + meta.getFileType()
              + MESSAGE, Const.createName( repositoryObject.getName() ) ) );
      // "This file already exists!"
      mb.setText( BaseMessages.getString( PKG, SPOON_DIALOG_PROMPT_OVERWRITE_FILE_TITLE ) );
      if ( mb.open() == SWT.YES ) {
        // user wants to overwrite the file
        return true;
      } else {
        // user does wants to overwrite the file
        return false;
      }
    }
    // the file does not exist
    return true;
  }

  public void addFileListener( FileListener listener ) {
    this.fileListeners.add( listener );
    for ( String s : listener.getSupportedExtensions() ) {
      if ( !fileExtensionMap.containsKey( s ) ) {
        fileExtensionMap.put( s, listener );
      }
    }
  }

  protected static String getFileType( final String fineName ) {
    final String fileExt = fineName == null ? null : FilenameUtils.getExtension( fineName );
    String fileType = BaseMessages.getString( PKG, "System.FileType.File" );
    if ( "ktr".equals( fileExt ) ) {
      fileType = BaseMessages.getString( PKG, "System.FileType.Transformation" );
    } else if ( "kjb".equals( fileExt ) ) {
      fileType = BaseMessages.getString( PKG, "System.FileType.Job" );
    }
    return fileType;
  }

  public void openFile( String filename, boolean importFile ) {
    openFile( filename, null, importFile );
  }

  public void openFile( String filename, VariableSpace variableSpace, boolean importFile ) {
    // Open the XML and see what's in there.
    // We expect a single <transformation> or <job> root at this time...

    // does the file exist? If not, show an error dialog
    boolean fileExists = false;
    FileObject file = null;
    try {
      file = KettleVFS.getFileObject( filename, variableSpace );
      fileExists = file.exists();
    } catch ( final KettleFileException | FileSystemException e ) {
      // nothing to do, null fileObject will be handled below
    }
    if ( StringUtils.isBlank( filename ) || !fileExists ) {
      final Dialog dlg = new SimpleMessageDialog( shell,
        BaseMessages.getString( PKG, "Spoon.Dialog.MissingRecentFile.Title" ),
        BaseMessages.getString( PKG, "Spoon.Dialog.MissingRecentFile.Message", getFileType( filename ).toLowerCase() ),
        MessageDialog.ERROR, BaseMessages.getString( PKG, "System.Button.Close" ),
        MISSING_RECENT_DLG_WIDTH, SimpleMessageDialog.BUTTON_WIDTH );
      dlg.open();
      return;
    }

    boolean loaded = false;
    FileListener listener = null;
    Node root = null;
    // match by extension first
    int idx = filename.lastIndexOf( '.' );
    if ( idx != -1 ) {
      for ( FileListener li : fileListeners ) {
        if ( li.accepts( filename ) ) {
          listener = li;
          break;
        }
      }
    }

    // Attempt to find a root XML node name. Fails gracefully for non-XML file
    // types.
    try {
      Document document = XMLHandler.loadXMLFile( file );
      root = document.getDocumentElement();
    } catch ( KettleXMLException e ) {
      if ( log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString( PKG, "Spoon.File.Xml.Parse.Error" ) );
      }
    }

    // otherwise try by looking at the root node if we were able to parse file
    // as XML
    if ( listener == null && root != null ) {
      for ( FileListener li : fileListeners ) {
        if ( li.acceptsXml( root.getNodeName() ) ) {
          listener = li;
          break;
        }
      }
    }

    // You got to have a file name!
    //
    if ( !Utils.isEmpty( filename ) ) {
      if ( listener != null ) {
        try {
          String connection = variableSpace != null ? variableSpace.getVariable( CONNECTION ) : null;
          if ( listener instanceof ConnectionListener ) {
            loaded = ( (ConnectionListener) listener ).open( root, filename, connection, importFile );
          } else {
            loaded = listener.open( root, filename, importFile );
          }
        } catch ( KettleMissingPluginsException e ) {
          log.logError( e.getMessage(), e );
        }
      }
      if ( !loaded ) {
        // Give error back
        hideSplash();
        MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
        mb.setMessage( BaseMessages.getString( PKG, "Spoon.UnknownFileType.Message", filename ) );
        mb.setText( BaseMessages.getString( PKG, "Spoon.UnknownFileType.Title" ) );
        mb.open();
      } else {
        applyVariables(); // set variables in the newly loaded
        // transformation(s) and job(s).
      }
    }
  }

  /**
   * The method which can open the marketplace.
   */
  private Method marketplaceMethod = null;

  /**
   * Set the method which can open the marketplace.
   */
  public void setMarketMethod( Method m ) {
    marketplaceMethod = m;
  }

  /**
   * If available, this method will open the marketplace.
   */
  public void openMarketplace() {
    try {
      if ( marketplaceMethod != null ) {
        marketplaceMethod.invoke( marketplaceMethod.getDeclaringClass().newInstance() );
      }
    } catch ( Exception ex ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "Spoon.ErrorShowingMarketplaceDialog.Title" ), BaseMessages
          .getString( PKG, "Spoon.ErrorShowingMarketplaceDialog.Message" ), ex );
    }
  }

  /**
   * Shows a dialog listing the missing plugins, asking if you want to go into the marketplace
   *
   * @param missingPluginsException
   *          The missing plugins exception
   */
  public void handleMissingPluginsExceptionWithMarketplace( KettleMissingPluginsException missingPluginsException ) {
    hideSplash();
    MessageBox box = new MessageBox( shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO );
    box.setText( BaseMessages.getString( PKG, "Spoon.MissingPluginsFoundDialog.Title" ) );
    box.setMessage( BaseMessages.getString(
      PKG, "Spoon.MissingPluginsFoundDialog.Message", Const.CR, missingPluginsException.getPluginsMessage() ) );
    int answer = box.open();
    if ( ( answer & SWT.YES ) != 0 ) {
      openMarketplace();
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
    XulToolbarbutton button = (XulToolbarbutton) this.mainToolbar.getElementById( "file-new" );
    Object object = button.getManagedObject();
    if ( object instanceof ToolItem ) {
      // OK, let's determine the location of this widget...
      //
      ToolItem item = (ToolItem) object;
      Rectangle bounds = item.getBounds();
      org.eclipse.swt.graphics.Point p =
        item.getParent().toDisplay( new org.eclipse.swt.graphics.Point( bounds.x, bounds.y ) );

      fileMenus.setLocation( p.x, p.y + bounds.height );
      fileMenus.setVisible( true );
    }
  }

  public void newTransFile() {
    TransMeta transMeta = new TransMeta();
    transMeta.addObserver( this );

    // Set the variables that were previously defined in this session on the
    // transformation metadata too.
    //
    setTransMetaVariables( transMeta );

    // Pass repository information
    //
    transMeta.setRepository( rep );
    transMeta.setMetaStore( metaStoreSupplier.get() );

    try {
      SharedObjects sharedObjects =
        rep != null ? rep.readTransSharedObjects( transMeta ) : transMeta.readSharedObjects();
      sharedObjectsFileMap.put( sharedObjects.getFilename(), sharedObjects );
      if ( rep == null ) {
        transMeta.setSharedObjects( sharedObjects );
      }
      transMeta.importFromMetaStore();
      transMeta.clearChanged();
    } catch ( Exception e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "Spoon.Exception.ErrorReadingSharedObjects.Title" ), BaseMessages
          .getString( PKG, "Spoon.Exception.ErrorReadingSharedObjects.Message" ), e );
    }

    // Set the location of the new transMeta to that of the default location or the last saved location
    transMeta.setRepositoryDirectory( getDefaultSaveLocation( transMeta ) );

    int nr = 1;
    transMeta.setName( STRING_TRANSFORMATION + " " + nr );

    // See if a transformation with the same name isn't already loaded...
    //
    while ( findTransformation( delegates.tabs.makeTabName( transMeta, false ) ) != null ) {
      nr++;
      transMeta.setName( STRING_TRANSFORMATION + " " + nr ); // rename
    }
    addTransGraph( transMeta );
    applyVariables();

    // switch to design mode...
    //
    setDesignMode();
    refreshTree( transMeta );
    loadPerspective( MainSpoonPerspective.ID );

    try {
      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.TransformationCreateNew.id, transMeta );
    } catch ( KettleException e ) {
      log.logError( "Failed to call extension point", e );
    }
  }

  public void newJobFile() {
    try {
      JobMeta jobMeta = new JobMeta();
      jobMeta.addObserver( this );

      // Set the variables that were previously defined in this session on
      // the transformation metadata too.
      //
      setJobMetaVariables( jobMeta );

      // Pass repository information
      //
      jobMeta.setRepository( rep );
      jobMeta.setMetaStore( metaStoreSupplier.get() );

      try {
        // TODO: MAKE LIKE TRANS
        SharedObjects sharedObjects =
          rep != null ? rep.readJobMetaSharedObjects( jobMeta ) : jobMeta.readSharedObjects();
        sharedObjectsFileMap.put( sharedObjects.getFilename(), sharedObjects );
        if ( rep == null ) {
          jobMeta.setSharedObjects( sharedObjects );
        }
        jobMeta.importFromMetaStore();
      } catch ( Exception e ) {
        new ErrorDialog(
          shell, BaseMessages.getString( PKG, "Spoon.Dialog.ErrorReadingSharedObjects.Title" ), BaseMessages
            .getString( PKG, "Spoon.Dialog.ErrorReadingSharedObjects.Message", delegates.tabs.makeTabName(
              jobMeta, true ) ), e );
      }

      // Set the location of the new jobMeta to that of the default location or the last saved location
      jobMeta.setRepositoryDirectory( getDefaultSaveLocation( jobMeta ) );

      int nr = 1;
      jobMeta.setName( STRING_JOB + " " + nr );

      // See if a transformation with the same name isn't already
      // loaded...
      while ( findJob( delegates.tabs.makeTabName( jobMeta, false ) ) != null ) {
        nr++;
        jobMeta.setName( STRING_JOB + " " + nr ); // rename
      }

      jobMeta.clearChanged();

      addJobGraph( jobMeta );
      applyVariables();

      // switch to design mode...
      //
      setDesignMode();
      refreshTree( jobMeta );
      loadPerspective( MainSpoonPerspective.ID );
    } catch ( Exception e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "Spoon.Exception.ErrorCreatingNewJob.Title" ), BaseMessages
          .getString( PKG, "Spoon.Exception.ErrorCreatingNewJob.Message" ), e );
    }
  }

  /**
   * Set previously defined variables (set variables dialog) on the specified transformation
   *
   * @param transMeta transformation's meta
   */
  public void setTransMetaVariables( TransMeta transMeta ) {
    for ( int i = 0; i < variables.size(); i++ ) {
      try {
        String name = variables.getValueMeta( i ).getName();
        String value = variables.getString( i, "" );

        transMeta.setVariable( name, Const.NVL( value, "" ) );
      } catch ( Exception e ) {
        // Ignore the exception, it should never happen on a getString()
        // anyway.
      }
    }

    // Also set the parameters
    //
    setParametersAsVariablesInUI( transMeta, transMeta );
  }

  /**
   * Set previously defined variables (set variables dialog) on the specified job
   *
   * @param jobMeta job's meta
   */
  public void setJobMetaVariables( JobMeta jobMeta ) {
    for ( int i = 0; i < variables.size(); i++ ) {
      try {
        String name = variables.getValueMeta( i ).getName();
        String value = variables.getString( i, "" );

        jobMeta.setVariable( name, Const.NVL( value, "" ) );
      } catch ( Exception e ) {
        // Ignore the exception, it should never happen on a getString()
        // anyway.
      }
    }

    // Also set the parameters
    //
    setParametersAsVariablesInUI( jobMeta, jobMeta );
  }

  /**
   * @deprecated is this ever used?
   * functionality seems covered in loadSessionInformation
   */
  public void loadRepositoryObjects( TransMeta transMeta ) {
    // Load common database info from active repository...
    if ( rep != null ) {
      try {
        SharedObjects sharedObjects = rep.readTransSharedObjects( transMeta );
        sharedObjectsFileMap.put( sharedObjects.getFilename(), sharedObjects );
      } catch ( Exception e ) {
        new ErrorDialog(
          shell, BaseMessages.getString( PKG, "Spoon.Error.UnableToLoadSharedObjects.Title" ), BaseMessages
            .getString( PKG, "Spoon.Error.UnableToLoadSharedObjects.Message" ), e );
      }

    }
  }

  public boolean promptForSave() throws KettleException {
    List<TabMapEntry> list = delegates.tabs.getTabs();

    for ( TabMapEntry mapEntry : list ) {
      TabItemInterface itemInterface = mapEntry.getObject();

      if ( !itemInterface.canBeClosed() ) {
        // Show the tab
        tabfolder.setSelected( mapEntry.getTabItem() );

        // Unsaved work that needs to changes to be applied?
        //
        int reply = itemInterface.showChangedWarning();
        if ( reply == SWT.YES ) {
          itemInterface.applyChanges();
        } else if ( reply == SWT.CANCEL ) {
          return false;
        }
      }
    }
    return true;
  }

  public boolean quitFile( boolean canCancel ) throws KettleException {
    if ( log.isDetailed() ) {
      log.logDetailed( BaseMessages.getString( PKG, "Spoon.Log.QuitApplication" ) ); // "Quit application."
    }

    boolean exit = true;

    saveSettings();

    if ( props.showExitWarning() && canCancel ) {
      // Display message: are you sure you want to exit?
      //
      MessageDialogWithToggle md =
        new MessageDialogWithToggle( shell,
          BaseMessages.getString( PKG, "System.Warning" ), // "Warning!"
          null,
          BaseMessages.getString( PKG, "Spoon.Message.Warning.PromptExit" ),
          MessageDialog.WARNING, new String[] {
            // "Yes",
            BaseMessages.getString( PKG, "Spoon.Message.Warning.Yes" ),
            // "No"
            BaseMessages.getString( PKG, "Spoon.Message.Warning.No" )
          }, 1,
          // "Please, don't show this warning anymore."
          BaseMessages.getString( PKG, "Spoon.Message.Warning.NotShowWarning" ),
          !props.showExitWarning() );
      MessageDialogWithToggle.setDefaultImage( GUIResource.getInstance().getImageSpoon() );
      int idx = md.open();
      props.setExitWarningShown( !md.getToggleState() );
      props.saveProps();
      if ( ( idx & 0xFF ) == 1 ) {
        return false; // No selected: don't exit!
      }
    }

    // Check all tabs to see if we can close them...
    //
    List<TabMapEntry> list = delegates.tabs.getTabs();

    for ( TabMapEntry mapEntry : list ) {
      TabItemInterface itemInterface = mapEntry.getObject();

      if ( !itemInterface.canBeClosed() ) {
        // Show the tab
        tabfolder.setSelected( mapEntry.getTabItem() );

        // Unsaved work that needs to changes to be applied?
        //
        int reply = itemInterface.showChangedWarning();
        if ( reply == SWT.YES ) {
          // If there is a fatal error, give the user opportunity to rename file w/out overwriting existing file
          if ( hasFatalError ) {
            saveFileAs( itemInterface.getMeta() );
          } else {
            exit = itemInterface.applyChanges();
          }
        } else {
          if ( reply == SWT.CANCEL ) {
            return false;
          } else { // SWT.NO
            exit = true;
          }
        }
      }
    }

    if ( exit || !canCancel ) {
      // we have asked about it all and we're still here. Now close
      // all the tabs, stop the running transformations
      for ( TabMapEntry mapEntry : list ) {
        if ( !mapEntry.getObject().canBeClosed() ) {
          // Unsaved transformation?
          //
          if ( mapEntry.getObject() instanceof TransGraph ) {
            TransMeta transMeta = (TransMeta) mapEntry.getObject().getManagedObject();
            if ( transMeta.hasChanged() ) {
              delegates.tabs.removeTab( mapEntry );
            }
          }
          // A running transformation?
          //
          if ( mapEntry.getObject() instanceof TransGraph ) {
            TransGraph transGraph = (TransGraph) mapEntry.getObject();
            if ( transGraph.isRunning() ) {
              transGraph.stop();
              delegates.tabs.removeTab( mapEntry );
            }
          }
        }
      }
    }

    // and now we call the listeners

    try {
      lifecycleSupport.onExit( this );
    } catch ( LifecycleException e ) {
      MessageBox box = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
      box.setMessage( e.getMessage() );
      box.open();
    }

    if ( exit ) {
      // on windows [...].swt.ole.win32.OleClientSite.OnInPlaceDeactivate can
      // cause the focus to move to an already disposed tab, resulting in a NPE
      // so we first move the focus to somewhere else
      if ( this.designTreeToolbar != null && !this.designTreeToolbar.isDisposed() ) {
        this.designTreeToolbar.forceFocus();
      }

      close();
    }

    return exit;
  }

  public boolean saveFile() {
    return saveFile( false );
  }

  public boolean saveFile( boolean export ) {
    try {
      EngineMetaInterface meta = getActiveMeta();
      if ( meta != null ) {
        if ( AbstractMeta.class.isAssignableFrom( meta.getClass() ) && ( (AbstractMeta) meta ).hasMissingPlugins() ) {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
          mb.setMessage( BaseMessages.getString( PKG, "Spoon.ErrorDialog.MissingPlugin.Error" ) );
          mb.setText( BaseMessages.getString( PKG, "Spoon.ErrorDialog.MissingPlugin.Title" ) );
          mb.open();
          return false;
        }
        if ( meta != null ) {
          return saveToFile( meta, export );
        }
      }
    } catch ( Exception e ) {
      KettleRepositoryLostException krle = KettleRepositoryLostException.lookupStackStrace( e );
      if ( krle != null ) {
        new ErrorDialog(
            shell,
            BaseMessages.getString( PKG, "Spoon.File.Save.Fail.Title" ),
            krle.getPrefaceMessage(),
            krle );
        closeRepository();
      } else {
        new ErrorDialog( shell, BaseMessages.getString( PKG, "Spoon.File.Save.Fail.Title" ), BaseMessages.getString(
            PKG, "Spoon.File.Save.Fail.Message" ), e );
      }
    }
    return false;
  }

  public boolean saveToFile( EngineMetaInterface meta ) throws KettleException {
    return saveToFile( meta, false );
  }

  public boolean saveToFile( EngineMetaInterface meta, boolean export ) throws KettleException {
    if ( meta == null ) {
      return false;
    }

    boolean saved = false;

    ( (AbstractMeta) meta ).setRepository( rep );
    ( (AbstractMeta) meta ).setMetaStore( metaStoreSupplier.get() );

    if ( getLog().isDetailed() ) {
      // "Save to file or repository...
      getLog().logDetailed( BaseMessages.getString( PKG, "Spoon.Log.SaveToFileOrRepository" ) );
    }

    SpoonPerspective activePerspective = SpoonPerspectiveManager.getInstance().getActivePerspective();

    // In case the perspective wants to handle open/save itself, let it...
    //
    if ( activePerspective instanceof SpoonPerspectiveOpenSaveInterface ) {
      return ( (SpoonPerspectiveOpenSaveInterface) activePerspective ).save( meta );
    }

    String activePerspectiveId = activePerspective.getId();
    boolean etlPerspective = activePerspectiveId.equals( MainSpoonPerspective.ID );
    if ( rep != null && etlPerspective ) {
      if ( meta.getObjectId() == null ) {
        meta.setFilename( null );
      }
      saved = saveToRepository( meta );
    } else {
      if ( meta.getFilename() != null ) {
        saved = save( meta, meta.getFilename(), export );
      } else {
        if ( meta.canSave() ) {
          saved = saveAsNew(meta, export, FileDialogOperation.SAVE_AS);
        }
      }
    }

    meta.saveSharedObjects(); // throws Exception in case anything goes wrong

    try {
      if ( props.useDBCache() && meta instanceof TransMeta ) {
        ( (TransMeta) meta ).getDbCache().saveCache();
      }
    } catch ( KettleException e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingDatabaseCache.Title" ),
        // "An error occurred saving the database cache to disk"
        BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingDatabaseCache.Message" ), e );
    }

    // rename the tab only if the meta was successfully saved
    if ( saved ) {
      delegates.tabs.renameTabs(); // filename or name of transformation might have changed.
    }
    refreshTree();

    // Update menu status for the newly saved object
    enableMenus();

    return saved;
  }

  public boolean saveToRepository( EngineMetaInterface meta ) throws KettleException {
    return saveToRepository( meta, meta.getObjectId() == null );
  }

  @VisibleForTesting
  FileDialogOperation getFileDialogOperation( String command, String origin ) {
    return new FileDialogOperation( command, origin );
  }

  public boolean saveToRepository( EngineMetaInterface meta, boolean ask_name ) throws KettleException {
    if ( getLog().isDetailed() ) {
      // "Save to repository..."
      //
      getLog().logDetailed( BaseMessages.getString( PKG, "Spoon.Log.SaveToRepository" ) );
    }
    if ( rep != null ) {

      // If the repository directory is root then get the default save directory
      if ( meta.getRepositoryDirectory() == null || meta.getRepositoryDirectory().isRoot() ) {
        meta.setRepositoryDirectory( rep.getDefaultSaveDirectory( meta ) );
      }

      if ( ask_name ) {
        try {
          String fileType = meta.getFileType().equals( LastUsedFile.FILE_TYPE_TRANSFORMATION )
            ? FileDialogOperation.TRANSFORMATION : FileDialogOperation.JOB;
          FileDialogOperation fileDialogOperation = getFileDialogOperation( FileDialogOperation.SAVE,
            FileDialogOperation.ORIGIN_SPOON );
          fileDialogOperation.setFileType( fileType );
          fileDialogOperation.setFilter( deriveFileFilterFromFileType( fileType ) );
          setFileOperatioPathForRepositoryFile( fileDialogOperation, meta );
          //Set the filename so it can be used as the default filename in the save dialog
          String fileName = ( meta.getFilename() == null || meta.getFilename().length() == 0 )
                  ? meta.getName() : meta.getFilename();
          fileDialogOperation.setFilename( fileName );
          fileDialogOperation.setProviderFilter( evaluateFileBrowserProviderFilter() );
          ExtensionPointHandler.callExtensionPoint( getLog(), KettleExtensionPoint.SpoonOpenSaveNew.id,
            fileDialogOperation );
          if ( fileDialogOperation.getRepositoryObject() != null ) {
            RepositoryObject repositoryObject = (RepositoryObject) fileDialogOperation.getRepositoryObject();
            final RepositoryDirectoryInterface oldDir = meta.getRepositoryDirectory();
            final String oldName = meta.getName();
            meta.setRepositoryDirectory( repositoryObject.getRepositoryDirectory() );
            meta.setName( repositoryObject.getName() );
            final boolean saved = saveToRepositoryConfirmed( meta );
            // rename the tab only if the meta object was successfully saved
            if ( saved ) {
              delegates.tabs.renameTabs();
            } else {
              // if the object wasn't successfully saved, set the name and directory back to their original values
              meta.setRepositoryDirectory( oldDir );
              meta.setName( oldName );
            }
            return saved;
          }
        } catch ( KettleException ke ) {
          //Ignore
        }
      } else {
        return saveToRepositoryConfirmed( meta );
      }

    } else {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      // "There is no repository connection available."
      mb.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.NoRepositoryConnection.Message" ) );
      // "No repository available."
      mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.NoRepositoryConnection.Title" ) );
      mb.open();
    }
    return false;
  }

  public boolean saveToRepositoryCheckSecurity( EngineMetaInterface meta ) {
    // Verify repository security first...
    //
    if ( meta.getFileType().equals( LastUsedFile.FILE_TYPE_TRANSFORMATION ) ) {
      if ( RepositorySecurityUI.verifyOperations( shell, rep, RepositoryOperation.MODIFY_TRANSFORMATION ) ) {
        return false;
      }
    }
    if ( meta.getFileType().equals( LastUsedFile.FILE_TYPE_JOB ) ) {
      if ( RepositorySecurityUI.verifyOperations( shell, rep, RepositoryOperation.MODIFY_JOB ) ) {
        return false;
      }
    }
    return true;
  }

  public boolean saveToRepositoryConfirmed( EngineMetaInterface meta ) throws KettleException {
    if ( !Utils.isEmpty( meta.getName() ) && rep != null ) {

      ObjectId existingId = null;
      if ( meta instanceof TransMeta ) {
        existingId = rep.getTransformationID( meta.getName(), meta.getRepositoryDirectory() );
      }
      if ( meta instanceof JobMeta ) {
        existingId = rep.getJobId( meta.getName(), meta.getRepositoryDirectory() );
      }

      boolean saved = false;

      if ( meta.getObjectId() == null ) {
        meta.setObjectId( existingId );
      }

      try {
        shell.setCursor( cursor_hourglass );

        // Keep info on who & when this transformation was
        // created and or modified...
        if ( meta.getCreatedDate() == null ) {
          meta.setCreatedDate( new Date() );
          if ( capabilities.supportsUsers() ) {
            meta.setCreatedUser( rep.getUserInfo().getLogin() );
          }
        }

        // Keep info on who & when this transformation was
        // changed...
        meta.setModifiedDate( new Date() );
        if ( capabilities.supportsUsers() ) {
          meta.setModifiedUser( rep.getUserInfo().getLogin() );
        }

        boolean versioningEnabled = true;
        boolean versionCommentsEnabled = true;
        String fullPath = getJobTransfFullPath( meta );
        RepositorySecurityProvider repositorySecurityProvider =
          rep != null && rep.getSecurityProvider() != null ? rep.getSecurityProvider() : null;
        if ( repositorySecurityProvider != null && fullPath != null ) {
          versioningEnabled = repositorySecurityProvider.isVersioningEnabled( fullPath );
          versionCommentsEnabled = repositorySecurityProvider.allowsVersionComments( fullPath );
        }

        // Finally before saving, ask for a version comment (if
        // applicable)
        //
        String versionComment = null;
        boolean versionOk;
        if ( !versioningEnabled || !versionCommentsEnabled ) {
          versionOk = true;
          versionComment = "";
        } else {
          versionOk = false;
        }
        while ( !versionOk ) {
          versionComment = RepositorySecurityUI.getVersionComment( shell, rep, meta.getName(), fullPath, false );

          // if the version comment is null, the user hit cancel, exit.
          if ( rep != null && rep.getSecurityProvider() != null
            && rep.getSecurityProvider().allowsVersionComments( fullPath ) && versionComment == null ) {
            return false;
          }

          if ( Utils.isEmpty( versionComment ) && rep.getSecurityProvider().isVersioningEnabled( fullPath )
            && rep.getSecurityProvider().isVersionCommentMandatory() ) {
            if ( !RepositorySecurityUI.showVersionCommentMandatoryDialog( shell ) ) {
              return false; // no, I don't want to enter a
              // version comment and yes,
              // it's mandatory.
            }
          } else {
            versionOk = true;
          }
        }

        if ( versionOk ) {
          SaveProgressDialog spd = new SaveProgressDialog( shell, rep, meta, versionComment );
          if ( spd.open() ) {
            //Now save the filename to meta
            meta.setFilename( fullPath );
            saved = true;
            if ( !props.getSaveConfirmation() ) {
              MessageDialogWithToggle md =
                new MessageDialogWithToggle(
                  shell, BaseMessages.getString( PKG, "Spoon.Message.Warning.SaveOK" ), null, BaseMessages
                  .getString( PKG, "Spoon.Message.Warning.TransformationWasStored" ),
                  MessageDialog.QUESTION, new String[] {
                  BaseMessages.getString( PKG, "Spoon.Message.Warning.OK" ) },
                  0,
                  BaseMessages.getString( PKG, "Spoon.Message.Warning.NotShowThisMessage" ),
                  props.getSaveConfirmation() );
              MessageDialogWithToggle.setDefaultImage( GUIResource.getInstance().getImageSpoon() );
              md.open();
              props.setSaveConfirmation( md.getToggleState() );
            }

            // Handle last opened files...
            props.addLastFile(
              meta.getFileType(), meta.getName(), meta.getRepositoryDirectory().getPath(), true,
              getRepositoryName(), getUsername(), null, null );
            saveSettings();
            addMenuLast();

            setShellText();
          }
        }
      } finally {
        shell.setCursor( null );
      }

      return saved;
    } else {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      // "There is no repository connection available."
      mb.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.NoRepositoryConnection.Message" ) );
      // "No repository available."
      mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.NoRepositoryConnection.Title" ) );
      mb.open();
    }
    return false;
  }

  public boolean saveJobRepository( JobMeta jobMeta ) throws KettleException {
    return saveToRepository( jobMeta, false );
  }

  public boolean saveJobRepository( JobMeta jobMeta, boolean ask_name ) throws KettleException {
    return saveToRepository( jobMeta, ask_name );
  }

  public boolean saveFileAs() throws KettleException {
    try {
      EngineMetaInterface meta = getActiveMeta();
      if ( meta != null && AbstractMeta.class.isAssignableFrom( meta.getClass() ) ) {
        if ( ( (AbstractMeta) meta ).hasMissingPlugins() ) {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
          mb.setMessage( BaseMessages.getString( PKG, "Spoon.ErrorDialog.MissingPlugin.Error" ) );
          mb.setText( BaseMessages.getString( PKG, "Spoon.ErrorDialog.MissingPlugin.Title" ) );
          mb.open();
          return false;
        }
      }
      if ( meta != null ) {
        if ( meta.canSave() ) {
          return saveFileAs( meta );
        }
      }
    } catch ( Exception e ) {
      KettleRepositoryLostException krle = KettleRepositoryLostException.lookupStackStrace( e );
      if ( krle != null ) {
        new ErrorDialog( shell,
            BaseMessages.getString( PKG, "Spoon.File.Save.Fail.Title" ),
            krle.getPrefaceMessage(),
            krle );
        closeRepository();
      } else {
        new ErrorDialog( shell,
            BaseMessages.getString( PKG, "Spoon.File.Save.Fail.Title" ),
            BaseMessages.getString( PKG, "Spoon.File.Save.Fail.Message" ), e );
      }
    }

    return false;
  }

  public boolean saveFileAs( EngineMetaInterface meta ) throws KettleException {
    boolean saved;

    if ( getLog().isBasic() ) {
      getLog().logBasic( BaseMessages.getString( PKG, "Spoon.Log.SaveAs" ) ); // "Save as..."
    }

    ( (AbstractMeta) meta ).setRepository( rep );
    ( (AbstractMeta) meta ).setMetaStore( metaStoreSupplier.get() );

    String activePerspectiveId = SpoonPerspectiveManager.getInstance().getActivePerspective().getId();
    boolean etlPerspective = activePerspectiveId.equals( MainSpoonPerspective.ID );
    if ( rep != null && etlPerspective ) {
      meta.setObjectId( null );
      meta.setFilename( null );
      saved = saveToRepository( meta, true );

    } else {
      saved = saveXMLFile( meta, false );
    }

    // rename the tab only if the meta was successfully saved
    if ( saved ) {
      delegates.tabs.renameTabs(); // filename or name of transformation might have changed.
    }
    refreshTree();
    if ( saved && ( meta instanceof TransMeta || meta instanceof JobMeta ) ) {
      TabMapEntry tabEntry = delegates.tabs.findTabMapEntry( meta );
      TabItem tabItem = tabEntry.getTabItem();
      if ( meta.getFileType().equals( LastUsedFile.FILE_TYPE_TRANSFORMATION ) ) {
        tabItem.setImage( GUIResource.getInstance().getImageTransGraph() );
      } else if ( meta.getFileType().equals( LastUsedFile.FILE_TYPE_JOB ) ) {
        tabItem.setImage( GUIResource.getInstance().getImageJobGraph() );
      }
    }

    // Update menu status for the newly saved object
    enableMenus();
    return saved;
  }

  public boolean exportXMLFile() {
    return saveXMLFile( true, FilterType.XML + "," + FilterType.ALL, FileDialogOperation.EXPORT );
  }

  /**
   * Export this job or transformation including all depending resources to a single zip file.
   */
  public void exportAllXMLFile() {

    ResourceExportInterface resourceExportInterface = getActiveTransformation();
    if ( resourceExportInterface == null ) {
      resourceExportInterface = getActiveJob();
    }
    if ( resourceExportInterface == null ) {
      return; // nothing to do here, prevent an NPE
    }

    try {
      String zipFilename = null;
      FileDialogOperation fileDialogOperation =
              getFileDialogOperation(FileDialogOperation.EXPORT_ALL, FileDialogOperation.ORIGIN_SPOON);
      fileDialogOperation.setFileType(FilterType.ZIP.toString());
      fileDialogOperation.setFilter(FilterType.ZIP.toString());
      fileDialogOperation.setProviderFilter(ProviderFilterType.LOCAL + "," + ProviderFilterType.VFS);
      setFileOperatioPathForNonRepositoryFile(fileDialogOperation, (EngineMetaInterface) resourceExportInterface, true);
      ExtensionPointHandler.callExtensionPoint(getLog(), KettleExtensionPoint.SpoonOpenSaveNew.id,
              fileDialogOperation);

      if (fileDialogOperation.getPath() != null && fileDialogOperation.getFilename() != null) {
        zipFilename = fileDialogOperation.getPath() + File.separator + fileDialogOperation.getFilename();
        lastFileOpened = zipFilename;
        lastFileOpenedConnection = null;
        lastFileOpenedProvider = fileDialogOperation.getProvider();
        FileObject zipFileObject = KettleVFS.getFileObject(zipFilename);
        if (zipFileObject.exists()) {
          MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL);
          box.setMessage(BaseMessages.getString(PKG, "Spoon.ExportResourceZipFileExists.Message", zipFilename));
          box.setText(BaseMessages.getString(PKG, "Spoon.ExportResourceZipFileExists.Title"));
          int answer = box.open();
          if (answer == SWT.CANCEL) {
            return;
          }
          if (answer == SWT.NO) {
            zipFilename = null;
          }
        }
        if (!Utils.isEmpty(zipFilename)) {
          // Export the resources linked to the currently loaded file...
          TopLevelResource topLevelResource =
                  ResourceUtil.serializeResourceExportInterface(
                          zipFilename, resourceExportInterface, (VariableSpace) resourceExportInterface, rep, metaStoreSupplier.get() );
          String message =
                  ResourceUtil.getExplanation(zipFilename, topLevelResource.getResourceName(), resourceExportInterface);

          EnterTextDialog enterTextDialog =
                  new EnterTextDialog(
                          shell, BaseMessages.getString(PKG, "Spoon.Dialog.ResourceSerialized"), BaseMessages.getString(
                          PKG, "Spoon.Dialog.ResourceSerializedSuccesfully"), message);
          enterTextDialog.setReadOnly();
          enterTextDialog.open();
        }
      }
    } catch (KettleException e) {
        new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Error"), BaseMessages.getString(
                PKG, "Spoon.ErrorExportingFile"), e);
    } catch (FileSystemException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Export this job or transformation including all depending resources to a single ZIP file containing a file
   * repository.
   */
  public void exportAllFileRepository() {

    ResourceExportInterface resourceExportInterface = getActiveTransformation();
    if ( resourceExportInterface == null ) {
      resourceExportInterface = getActiveJob();
    }
    if ( resourceExportInterface == null ) {
      return; // nothing to do here, prevent an NPE
    }

    // Ask the user for a zip file to export to:
    //
    try {
      String zipFilename = null;
      while ( Utils.isEmpty( zipFilename ) ) {
        FileDialog dialog = new FileDialog( shell, SWT.SAVE );
        dialog.setText( BaseMessages.getString( PKG, "Spoon.ExportResourceSelectZipFile" ) );
        dialog.setFilterExtensions( new String[] { "*.zip;*.ZIP", "*" } );
        dialog.setFilterNames( new String[] {
          BaseMessages.getString( PKG, "System.FileType.ZIPFiles" ),
          BaseMessages.getString( PKG, "System.FileType.AllFiles" ), } );
        setFilterPath( dialog );
        if ( dialog.open() != null ) {
          lastDirOpened = dialog.getFilterPath();
          zipFilename = dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName();
          FileObject zipFileObject = KettleVFS.getFileObject( zipFilename );
          if ( zipFileObject.exists() ) {
            MessageBox box = new MessageBox( shell, SWT.YES | SWT.NO | SWT.CANCEL );
            box
              .setMessage( BaseMessages
                .getString( PKG, "Spoon.ExportResourceZipFileExists.Message", zipFilename ) );
            box.setText( BaseMessages.getString( PKG, "Spoon.ExportResourceZipFileExists.Title" ) );
            int answer = box.open();
            if ( answer == SWT.CANCEL ) {
              return;
            }
            if ( answer == SWT.NO ) {
              zipFilename = null;
            }
          }
        } else {
          return;
        }
      }

      // Export the resources linked to the currently loaded file...
      //
      TopLevelResource topLevelResource =
        ResourceUtil.serializeResourceExportInterface(
          zipFilename, resourceExportInterface, (VariableSpace) resourceExportInterface, rep, metaStoreSupplier.get() );
      String message =
        ResourceUtil.getExplanation( zipFilename, topLevelResource.getResourceName(), resourceExportInterface );

      /*
       * // Add the ZIP file as a repository to the repository list... // RepositoriesMeta repositoriesMeta = new
       * RepositoriesMeta(); repositoriesMeta.readData();
       *
       * KettleFileRepositoryMeta fileRepositoryMeta = new KettleFileRepositoryMeta(
       * KettleFileRepositoryMeta.REPOSITORY_TYPE_ID, "Export " + baseFileName, "Export to file : " + zipFilename,
       * "zip://" + zipFilename + "!"); fileRepositoryMeta.setReadOnly(true); // A ZIP file is read-only int nr = 2;
       * String baseName = fileRepositoryMeta.getName(); while
       * (repositoriesMeta.findRepository(fileRepositoryMeta.getName()) != null) { fileRepositoryMeta.setName(baseName +
       * " " + nr); nr++; }
       *
       * repositoriesMeta.addRepository(fileRepositoryMeta); repositoriesMeta.writeData();
       */

      // Show some information concerning all this work...
      //
      EnterTextDialog enterTextDialog =
        new EnterTextDialog(
          shell, BaseMessages.getString( PKG, "Spoon.Dialog.ResourceSerialized" ), BaseMessages.getString(
            PKG, "Spoon.Dialog.ResourceSerializedSuccesfully" ), message );
      enterTextDialog.setReadOnly();
      enterTextDialog.open();
    } catch ( Exception e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "Spoon.Error" ), BaseMessages.getString(
        PKG, "Spoon.ErrorExportingFile" ), e );
    }
  }

  public void exportRepositoryAll() {
    exportRepositoryDirectory( null );
  }

  /**
   * @param directoryToExport
   *          set to null to export the complete repository
   * @return false if we want to stop processing. true if we need to continue.
   */
  public boolean exportRepositoryDirectory( RepositoryDirectory directoryToExport ) {

    FileDialog dialog = this.getExportFileDialog();
    if ( dialog.open() == null ) {
      return false;
    }

    String filename = dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName();
    log.logBasic( BaseMessages.getString( PKG, "Spoon.Log.Exporting" ), BaseMessages.getString(
      PKG, "Spoon.Log.ExportObjectsToFile", filename ) );

    // check if file is exists
    MessageBox box = RepositoryExportProgressDialog.checkIsFileIsAcceptable( shell, log, filename );
    int answer = ( box == null ) ? SWT.OK : box.open();
    if ( answer != SWT.OK ) {
      // seems user don't want to overwrite file...
      return false;
    }

    //ok, let's show one more modal dialog, users like modal dialogs.
    //They feel that their opinion are important to us.
    box =
      new MessageBox( shell, SWT.ICON_QUESTION
        | SWT.APPLICATION_MODAL | SWT.SHEET | SWT.YES | SWT.NO | SWT.CANCEL );
    box.setText( BaseMessages.getString( PKG, "Spoon.QuestionApplyImportRulesToExport.Title" ) );
    box.setMessage( BaseMessages.getString( PKG, "Spoon.QuestionApplyImportRulesToExport.Message" ) );
    answer = box.open();
    if ( answer == SWT.CANCEL ) {
      return false;
    }

    // Get the import rules
    //
    ImportRules importRules = new ImportRules();
    if ( answer == SWT.YES ) {
      ImportRulesDialog importRulesDialog = new ImportRulesDialog( shell, importRules );
      if ( !importRulesDialog.open() ) {
        return false;
      }
    }

    RepositoryExportProgressDialog repd =
      new RepositoryExportProgressDialog( shell, rep, directoryToExport, filename, importRules );
    repd.open();

    return true;
  }

  /**
   * local method to be able to use Spoon localization messages.
   * @return
   */
  public FileDialog getExportFileDialog() {
    FileDialog dialog = new FileDialog( shell, SWT.SAVE | SWT.SINGLE );
    dialog.setText( BaseMessages.getString( PKG, "Spoon.SelectAnXMLFileToExportTo.Message" ) );
    return dialog;
  }


  public void importDirectoryToRepository() {
    FileDialog dialog = new FileDialog( shell, SWT.OPEN | SWT.MULTI );
    dialog.setText( BaseMessages.getString( PKG, "Spoon.SelectAnXMLFileToImportFrom.Message" ) );
    if ( dialog.open() == null ) {
      return;
    }

    // Ask for a set of import rules
    //
    MessageBox box =
      new MessageBox( shell, SWT.ICON_QUESTION
        | SWT.APPLICATION_MODAL | SWT.SHEET | SWT.YES | SWT.NO | SWT.CANCEL );
    box.setText( BaseMessages.getString( PKG, "Spoon.QuestionApplyImportRules.Title" ) );
    box.setMessage( BaseMessages.getString( PKG, "Spoon.QuestionApplyImportRules.Message" ) );
    int answer = box.open();
    if ( answer == SWT.CANCEL ) {
      return;
    }

    // Get the import rules
    //
    ImportRules importRules = new ImportRules();
    if ( answer == SWT.YES ) {
      ImportRulesDialog importRulesDialog = new ImportRulesDialog( shell, importRules );
      if ( !importRulesDialog.open() ) {
        return;
      }
    }

    // Ask for a destination in the repository...
    //
    SelectDirectoryDialog sdd = new SelectDirectoryDialog( shell, SWT.NONE, rep );
    RepositoryDirectoryInterface baseDirectory = sdd.open();
    if ( baseDirectory == null ) {
      return;
    }

    // Finally before importing, ask for a version comment (if applicable)
    //
    String fullPath = baseDirectory.getPath() + "/foo.ktr";
    String versionComment = null;
    boolean versionOk = false;
    while ( !versionOk ) {
      versionComment =
        RepositorySecurityUI.getVersionComment( shell, rep, "Import of files into ["
          + baseDirectory.getPath() + "]", fullPath, true );
      // if the version comment is null, the user hit cancel, exit.
      if ( versionComment == null ) {
        return;
      }

      if ( Utils.isEmpty( versionComment ) && rep.getSecurityProvider().isVersionCommentMandatory( ) ) {
        if ( !RepositorySecurityUI.showVersionCommentMandatoryDialog( shell ) ) {
          versionOk = true;
        }
      } else {
        versionOk = true;
      }
    }

    String[] filenames = dialog.getFileNames();
    if ( filenames.length > 0 ) {
      RepositoryImportProgressDialog ripd =
        new RepositoryImportProgressDialog(
          shell, SWT.NONE, rep, dialog.getFilterPath(), filenames, baseDirectory, versionComment, importRules );
      ripd.open();

      refreshTree();
    }
  }

  public boolean saveXMLFile( boolean export, String fileFilterType, String command ) {
    TransMeta transMeta = getActiveTransformation();
    if ( transMeta != null ) {
      return saveTransAsXmlFile( transMeta, export, fileFilterType + "," + FilterType.KETTLE_TRANS, command );
    }

    JobMeta jobMeta = getActiveJob();
    if ( jobMeta != null ) {
      return saveJobAsXmlFile( jobMeta, export, fileFilterType + "," + FilterType.KETTLE_JOB, command );
    }

    return false;
  }

  private boolean saveTransAsXmlFile( TransMeta transMeta, boolean export, String fileFilterType, String command ) {
    TransLogTable origTransLogTable = transMeta.getTransLogTable();
    StepLogTable origStepLogTable = transMeta.getStepLogTable();
    PerformanceLogTable origPerformanceLogTable = transMeta.getPerformanceLogTable();
    ChannelLogTable origChannelLogTable = transMeta.getChannelLogTable();
    MetricsLogTable origMetricsLogTable = transMeta.getMetricsLogTable();

    try {
      XmlExportHelper.swapTables( transMeta );
      return saveAsNew( transMeta, export, ProviderFilterType.LOCAL + "," + ProviderFilterType.VFS
              , fileFilterType, command );
    } finally {
      transMeta.setTransLogTable( origTransLogTable );
      transMeta.setStepLogTable( origStepLogTable );
      transMeta.setPerformanceLogTable( origPerformanceLogTable );
      transMeta.setChannelLogTable( origChannelLogTable );
      transMeta.setMetricsLogTable( origMetricsLogTable );
    }
  }


  private boolean saveJobAsXmlFile( JobMeta jobMeta, boolean export, String fileFilterType, String command ) {
    JobLogTable origJobLogTable = jobMeta.getJobLogTable();
    JobEntryLogTable originEntryLogTable = jobMeta.getJobEntryLogTable();
    ChannelLogTable originChannelLogTable = jobMeta.getChannelLogTable();
    List<LogTableInterface> originExtraLogTables = jobMeta.getExtraLogTables();

    try {
      XmlExportHelper.swapTables( jobMeta );
      return saveAsNew( jobMeta, export , ProviderFilterType.LOCAL + "," + ProviderFilterType.VFS
              , fileFilterType, command );
    } finally {
      jobMeta.setJobLogTable( origJobLogTable );
      jobMeta.setJobEntryLogTable( originEntryLogTable );
      jobMeta.setChannelLogTable( originChannelLogTable );
      jobMeta.setExtraLogTables( originExtraLogTables );
    }
  }

  public boolean saveXMLFile( EngineMetaInterface meta, boolean export ) {
    FileDialog dialog = new FileDialog( shell, SWT.SAVE );
    dialog.setFilterExtensions( meta.getFilterExtensions() );
    dialog.setFilterNames( meta.getFilterNames() );
    setFilterPath( dialog );
    String filename = dialog.open();
    if ( filename != null ) {
      lastDirOpened = dialog.getFilterPath();
      return saveXMLFile( meta, filename, export );
    }
    return false;
  }

  public boolean saveXMLFile( EngineMetaInterface meta, String filename, boolean export ) {
    if ( log.isBasic() ) {
      log.logBasic( "Save file as..." );
    }
    boolean saved = false;
    String beforeFilename = meta.getFilename();
    String beforeName = meta.getName();
    if ( filename != null ) {
      String[] extensions = meta.getFilterExtensions();
      // Is the filename ending on .ktr, .xml?
      boolean ending = false;
      for ( int i = 0; i < extensions.length - 1; i++ ) {
        String[] parts = extensions[i].split( ";" );
        for ( String part : parts ) {
          if ( filename.toLowerCase().endsWith( part.substring( 1 ).toLowerCase() ) ) {
            ending = true;
          }
        }
      }
      if ( filename.endsWith( meta.getDefaultExtension() ) ) {
        ending = true;
      }
      if ( !ending ) {
        if ( !meta.getDefaultExtension().startsWith( "." ) && !filename.endsWith( "." ) ) {
          filename += ".";
        }
        if ( export ) {
          filename += XML_EXTENSION;
        } else {
          filename += meta.getDefaultExtension();
        }
      }
      // See if the file already exists...
      int id = SWT.YES;
      try {
        Variables variables = new Variables();
        if ( meta instanceof VariableSpace ) {
          variables.setVariable( CONNECTION, ( (VariableSpace) meta ).getVariable( CONNECTION ) );
        }
        FileObject f = KettleVFS.getFileObject( filename, variables );
        if ( f.exists() ) {
          MessageBox mb = new MessageBox( shell, SWT.NO | SWT.YES | SWT.ICON_WARNING );
          // "This file already exists.  Do you want to overwrite it?"
          mb.setMessage( BaseMessages.getString( PKG, SPOON_DIALOG_PROMPT_OVERWRITE_FILE + meta.getFileType()
            + MESSAGE, Const.createName( filename ) ) );
          // "This file already exists!"
          mb.setText( BaseMessages.getString( PKG, SPOON_DIALOG_PROMPT_OVERWRITE_FILE_TITLE ) );
          id = mb.open();
        }
      } catch ( Exception e ) {
        // TODO do we want to show an error dialog here? My first guess
        // is not, but we might.
      }
      if ( id == SWT.YES ) {
        if ( !export && !Utils.isEmpty( beforeFilename ) && !beforeFilename.equals( filename ) ) {
          meta.setName( Const.createName( filename ) );
          meta.setFilename( filename );
          // If the user hits cancel here, don't save anything
          //
          if ( !editProperties() ) {
            // Revert the changes!
            //
            meta.setFilename( beforeFilename );
            meta.setName( beforeName );
            return saved;
          }
        }

        saved = save( meta, filename, export );
        if ( !saved ) {
          meta.setFilename( beforeFilename );
          meta.setName( beforeName );
        }
      }
    }
    return saved;
  }

  public boolean saveXMLFileToVfs() {
    TransMeta transMeta = getActiveTransformation();
    if ( transMeta != null ) {
      return saveXMLFileToVfs( transMeta );
    }

    JobMeta jobMeta = getActiveJob();
    if ( jobMeta != null ) {
      return saveXMLFileToVfs( jobMeta );
    }

    return false;
  }

  public boolean saveXMLFileToVfs( EngineMetaInterface meta ) {
    if ( log.isBasic() ) {
      log.logBasic( "Save file as..." );
    }

    FileObject rootFile;
    FileObject initialFile;
    try {
      initialFile = KettleVFS.getFileObject( getLastFileOpened() );
      rootFile = KettleVFS.getFileObject( getLastFileOpened() ).getFileSystem().getRoot();
    } catch ( Exception e ) {
      MessageBox messageDialog = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
      messageDialog.setText( "Error" );
      messageDialog.setMessage( e.getMessage() );
      messageDialog.open();
      return false;
    }

    String filename = null;
    FileObject selectedFile =
      getVfsFileChooserDialog( rootFile, initialFile ).open(
          shell, "Untitled", Const.STRING_TRANS_AND_JOB_FILTER_EXT, Const.getTransformationAndJobFilterNames(),
          VfsFileChooserDialog.VFS_DIALOG_SAVEAS );
    if ( selectedFile != null ) {
      filename = selectedFile.getName().getFriendlyURI();
    }

    String[] extensions = meta.getFilterExtensions();
    if ( filename != null ) {
      // Is the filename ending on .ktr, .xml?
      boolean ending = false;
      for ( int i = 0; i < extensions.length - 1; i++ ) {
        if ( filename.endsWith( extensions[i].substring( 1 ) ) ) {
          ending = true;
        }
      }
      if ( filename.endsWith( meta.getDefaultExtension() ) ) {
        ending = true;
      }
      if ( !ending ) {
        filename += '.' + meta.getDefaultExtension();
      }
      // See if the file already exists...
      int id = SWT.YES;
      try {
        FileObject f = KettleVFS.getFileObject( filename );
        if ( f.exists() ) {
          MessageBox mb = new MessageBox( shell, SWT.NO | SWT.YES | SWT.ICON_WARNING );
          // "This file already exists.  Do you want to overwrite it?"
          mb.setMessage( BaseMessages.getString( PKG, SPOON_DIALOG_PROMPT_OVERWRITE_FILE + meta.getFileType()
            + MESSAGE, Const.createName( filename ) ) );
          mb.setText( BaseMessages.getString( PKG, SPOON_DIALOG_PROMPT_OVERWRITE_FILE_TITLE ) );
          id = mb.open();
        }
      } catch ( Exception e ) {
        // TODO do we want to show an error dialog here? My first guess
        // is not, but we might.
      }
      if ( id == SWT.YES ) {
        save( meta, filename, false );
      }
    }
    return false;
  }

  public boolean save( EngineMetaInterface meta, String filename, boolean export ) {
    boolean saved = false;

    // the only file types that are subject to ascii-only rule are those that are not trans and not job
    boolean isNotTransOrJob =
      !LastUsedFile.FILE_TYPE_TRANSFORMATION.equals( meta.getFileType() )
        && !LastUsedFile.FILE_TYPE_JOB.equals( meta.getFileType() );

    if ( isNotTransOrJob ) {
      Pattern pattern = Pattern.compile( "\\p{ASCII}+" );
      Matcher matcher = pattern.matcher( filename );
      if ( !matcher.matches() ) {
        /*
         * Temporary fix for AGILEBI-405 Don't allow saving of files that contain special characters until AGILEBI-394
         * is resolved. AGILEBI-394 Naming an analyzer report with spanish accents gives error when publishing.
         */
        MessageBox box = new MessageBox( staticSpoon.shell, SWT.ICON_ERROR | SWT.OK );
        box.setMessage( "Special characters are not allowed in the filename. Please use ASCII characters only" );
        box.setText( BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingConnection.Title" ) );
        box.open();
        return false;
      }
    }

    FileListener listener = null;
    // match by extension first - this results into PDI-15323
    // So to fix PDI-15323, we let the fileListener for the default extension for this meta
    // handle the request, commenting out the code that finds the listener by extension type
    /*
    int idx = filename.lastIndexOf( '.' );
    if ( idx != -1 ) {
      String extension = filename.substring( idx + 1 );
      listener = fileExtensionMap.get( extension );
    }
    */
    if ( listener == null ) {
      String xt = meta.getDefaultExtension();
      listener = fileExtensionMap.get( xt );
    }

    if ( listener != null ) {
      String sync = BasePropertyHandler.getProperty( SYNC_TRANS );
      if ( Boolean.parseBoolean( sync ) ) {
        listener.syncMetaName( meta, Const.createName( filename ) );
      }
      saved = listener.save( meta, filename, export );
      if ( Boolean.parseBoolean( sync ) && saved ) {
        // rename the tab only if the meta was successfully saved
        delegates.tabs.renameTabs();
      }
    }

    return saved;
  }

  public boolean saveMeta( EngineMetaInterface meta, String filename ) {
    return saveMeta( meta, filename, false );
  }

  public boolean saveMeta( EngineMetaInterface meta, String filename, boolean export ) {
    if ( !export ) {
      meta.setFilename( filename );
    }
    if ( Utils.isEmpty( meta.getName() )
      || delegates.jobs.isDefaultJobName( meta.getName() )
      || delegates.trans.isDefaultTransformationName( meta.getName() ) ) {
      meta.nameFromFilename();
    }

    boolean saved = false;
    try {
      String xml = XMLHandler.getXMLHeader() + meta.getXML();

      VariableSpace variableSpace = meta instanceof VariableSpace ? (VariableSpace) meta : null;
      DataOutputStream dos = new DataOutputStream( KettleVFS.getOutputStream( filename, variableSpace, false ) );
      dos.write( xml.getBytes( Const.XML_ENCODING ) );
      dos.close();

      saved = true;

      // Handle last opened files...
      props.addLastFile( meta.getFileType(), filename, null, false, null );
      saveSettings();
      addMenuLast();

      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "Spoon.Log.FileWritten" ) + " [" + filename + "]" ); // "File
      }
      // written
      // to
      if ( !export ) {
        meta.setFilename( filename );
        meta.clearChanged();
        setShellText();
      }
    } catch ( Exception e ) {
      if ( log.isDebug() ) {
        // "Error opening file for writing! --> "
        log.logDebug( BaseMessages.getString( PKG, "Spoon.Log.ErrorOpeningFileForWriting" ) + e.toString() );
      }
      new ErrorDialog( shell, BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingFile.Title" ),
        BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingFile.Message" )
          + Const.CR + e.toString(), e );
    }
    return saved;
  }

  public void helpAbout() {
    try {
      AboutDialog aboutDialog = new AboutDialog( getShell() );
      aboutDialog.open();
    } catch ( KettleException e ) {
      log.logError( "Error opening about dialog", e );
    }
  }

  /**
   * Show a plugin browser
   */
  public void showPluginInfo() {
    try {
      // First we collect information concerning all the plugin types...
      //
      Map<String, RowMetaInterface> metaMap = new HashMap<>();
      Map<String, List<Object[]>> dataMap = new HashMap<>();

      PluginRegistry registry = PluginRegistry.getInstance();
      List<Class<? extends PluginTypeInterface>> pluginTypeClasses = registry.getPluginTypes();
      for ( Class<? extends PluginTypeInterface> pluginTypeClass : pluginTypeClasses ) {
        PluginTypeInterface pluginTypeInterface = registry.getPluginType( pluginTypeClass );
        if ( pluginTypeInterface.isFragment() ) {
          continue;
        }

        String subject = pluginTypeInterface.getName();
        RowBuffer pluginInformation = registry.getPluginInformation( pluginTypeClass );
        metaMap.put( subject, pluginInformation.getRowMeta() );
        dataMap.put( subject, pluginInformation.getBuffer() );
      }

      // Now push it all to a subject data browser...
      //
      SubjectDataBrowserDialog dialog =
        new SubjectDataBrowserDialog( shell, metaMap, dataMap, "Plugin browser", "Plugin type" );
      dialog.open();

    } catch ( Exception e ) {
      new ErrorDialog( shell, "Error", "Error listing plugins", e );
    }

  }

  public void editUnselectAll() {
    TransMeta transMeta = getActiveTransformation();
    if ( transMeta != null ) {
      transMeta.unselectAll();
      getActiveTransGraph().redraw();
    }

    JobMeta jobMeta = getActiveJob();
    if ( jobMeta != null ) {
      jobMeta.unselectAll();
      getActiveJobGraph().redraw();
    }
  }

  public void editSelectAll() {
    TransMeta transMeta = getActiveTransformation();
    if ( transMeta != null ) {
      transMeta.selectAll();
      getActiveTransGraph().redraw();
    }

    JobMeta jobMeta = getActiveJob();
    if ( jobMeta != null ) {
      jobMeta.selectAll();
      getActiveJobGraph().redraw();
    }
  }

  public void editOptions() {
    EnterOptionsDialog eod = new EnterOptionsDialog( shell );
    if ( eod.open() != null ) {
      props.saveProps();
      loadSettings();
      changeLooks();

      MessageBox mb = new MessageBox( shell, SWT.ICON_INFORMATION );
      mb.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.PleaseRestartApplication.Message" ) );
      mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.PleaseRestartApplication.Title" ) );
      mb.open();
    }
  }



  public void editKettlePropertiesFile() {
    KettlePropertiesFileDialog dialog = new KettlePropertiesFileDialog( shell, SWT.NONE );
    Map<String, String> newProperties = dialog.open();
    if ( newProperties != null ) {
      for ( String name : newProperties.keySet() ) {
        String value = newProperties.get( name );
        applyVariableToAllLoadedObjects( name, value );

        // Also set as a JVM property
        //
        System.setProperty( name, value );
      }
    }
  }

  /**
   * Matches if the filter is non-empty
   *
   * @param string string to match
   * @return true in case string matches filter
   */
  @VisibleForTesting boolean filterMatch( String string ) {
    if ( Utils.isEmpty( string ) ) {
      return true;
    }

    String filter = designTreeToolbar.getSearchText();
    if ( Utils.isEmpty( filter ) ) {
      return true;
    }

    return string.toUpperCase().contains( filter.toUpperCase() );
  }

  public TreeManager getTreeManager() {
    return selectionTreeManager;
  }

  private void createSelectionTree() {
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Now set up the transformation/job tree
    //
    selectionTree = new Tree( viewTreeComposite, SWT.SINGLE );
    selectionTreeManager = new TreeManager( selectionTree );
    selectionTreeManager.addRoot( STRING_TRANSFORMATIONS, Arrays.asList( new DBConnectionFolderProvider(), new
            StepsFolderProvider(), new HopsFolderProvider(), new PartitionsFolderProvider(), new SlavesFolderProvider(), new
            ClustersFolderProvider() ) );
    selectionTreeManager.addRoot( STRING_JOBS, Arrays.asList( new DBConnectionFolderProvider(), new
            JobEntriesFolderProvider(), new SlavesFolderProvider() ) );

    props.setLook( selectionTree );
    selectionTree.setLayout( new FillLayout() );
    addDefaultKeyListeners( selectionTree );

    selectionTree.addMenuDetectListener( e -> setMenu( selectionTree ) );

    selectionTree.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        showSelection();
      }

      @Override
      public void widgetDefaultSelected( SelectionEvent e ) {
        doubleClickedInTree( selectionTree );
      }
    } );

    // Set a listener on the tree
    addDragSourceToTree( selectionTree );
  }

  public void refreshTree( AbstractMeta abstractMeta ) {
    selectionTreeManager.remove( abstractMeta );
    refreshTree();
  }

  public void refreshTree( String folderName ) {
    selectionTreeManager.update( folderName );
    refreshTree();
  }

  /**
   * Refresh the object selection tree (on the left of the screen)
   */
  public void refreshTree() {

    if ( selectionTree == null ) {
      createSelectionTree();
    }

    selectionTreeManager.clear();
    TransMeta activeTransMeta = getActiveTransformation();
    JobMeta activeJobMeta = getActiveJob();
    boolean showAll = activeTransMeta == null && activeJobMeta == null;
    boolean showTrans = !props.isOnlyActiveFileShownInTree() || showAll || ( props.isOnlyActiveFileShownInTree()
            && activeTransMeta != null );
    boolean showJobs = !props.isOnlyActiveFileShownInTree() || showAll || ( props.isOnlyActiveFileShownInTree()
            && activeJobMeta != null );

    selectionTreeManager.showRoot( STRING_TRANSFORMATIONS, showTrans || showAll );
    selectionTreeManager.showRoot( STRING_JOBS, showJobs || showAll );

    if ( showTrans ) {
      for ( TabMapEntry entry : delegates.tabs.getTabs() ) {
        Object managedObject = entry.getObject().getManagedObject();
        if ( managedObject instanceof TransMeta ) {
          showMetaTree( activeTransMeta, (TransMeta) managedObject, STRING_TRANSFORMATIONS, showAll );
        }
      }
    }

    if ( showJobs ) {
      for ( TabMapEntry entry : delegates.tabs.getTabs() ) {
        Object managedObject = entry.getObject().getManagedObject();
        if ( managedObject instanceof JobMeta ) {
          showMetaTree( activeJobMeta, (JobMeta) managedObject, STRING_JOBS, showAll );
        }
      }
    }

    selectionTreeManager.render();
    selectionTree.setFocus();
    selectionTree.layout();
    viewTreeComposite.layout( true, true );
    setShellText();
  }

  private void showMetaTree( AbstractMeta activeMeta, AbstractMeta meta, String type, boolean showAll ) {
    if ( !props.isOnlyActiveFileShownInTree() || showAll || ( activeMeta != null && activeMeta.equals( meta ) ) ) {
      if ( !selectionTreeManager.hasNode( meta ) ) {
        selectionTreeManager.create( meta, type, props.isOnlyActiveFileShownInTree() );
      } else {
        selectionTreeManager.checkUpdate( meta, type );
        selectionTreeManager.reset( meta );
      }
      if ( activeMeta != null ) {
        selectionTreeManager.show( meta );
      }
    }
  }

  @VisibleForTesting TreeItem createTreeItem( TreeItem parent, String text, Image image ) {
    return createTreeItem( parent, text, image, null );
  }

  @VisibleForTesting TreeItem createTreeItem( TreeItem parent, String text, Image image, String id ) {
    TreeItem item = new TreeItem( parent, SWT.NONE );
    item.setText( text );
    item.setImage( image );

    if ( id != null ) {
      item.setData( "StepId", id );
    }
    return item;
  }

  public void handleRepositoryLost( KettleRepositoryLostException e ) {
    setRepository( null );
    warnRepositoryLost( e );
    SpoonPluginManager.getInstance().notifyLifecycleListeners( SpoonLifeCycleEvent.REPOSITORY_DISCONNECTED );
    setShellText();
    enableMenus();
  }

  private void warnRepositoryLost( KettleRepositoryLostException e ) {
    MessageBox box = new MessageBox( shell, SWT.OK | SWT.ICON_WARNING );
    box.setText( BaseMessages.getString( PKG, "System.Warning" ) );
    box.setMessage( e.getPrefaceMessage() );
    box.open();
  }

  @Override public List<String> getPartitionSchemasNames( TransMeta transMeta ) throws KettleException {
    return Arrays.asList( pickupPartitionSchemaNames( transMeta ) );
  }

  private String[] pickupPartitionSchemaNames( TransMeta transMeta ) throws KettleException {
    return ( rep == null ) ? transMeta.getPartitionSchemasNames() : rep.getPartitionSchemaNames( false );
  }


  @Override public List<PartitionSchema> getPartitionSchemas( TransMeta transMeta ) throws KettleException {
    return pickupPartitionSchemas( transMeta );
  }

  private List<PartitionSchema> pickupPartitionSchemas( TransMeta transMeta ) throws KettleException {
    if ( rep != null ) {
      ObjectId[] ids = rep.getPartitionSchemaIDs( false );
      List<PartitionSchema> result = new ArrayList<>( ids.length );
      for ( ObjectId id : ids ) {
        PartitionSchema schema = rep.loadPartitionSchema( id, null );
        result.add( schema );
      }
      return result;
    }

    return transMeta.getPartitionSchemas();
  }

  @VisibleForTesting void refreshSelectionTreeExtension( TreeItem tiRootName, AbstractMeta meta, GUIResource guiResource ) {
    try {
      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.SpoonViewTreeExtension.id,
          new SelectionTreeExtension( tiRootName, meta, guiResource, REFRESH_SELECTION_EXTENSION ) );
    } catch ( Exception e ) {
      log.logError( "Error handling menu right click on job entry through extension point", e );
    }
  }

  @VisibleForTesting void editSelectionTreeExtension( Object selection ) {
    try {
      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.SpoonViewTreeExtension.id,
          new SelectionTreeExtension( selection, EDIT_SELECTION_EXTENSION ) );
    } catch ( Exception e ) {
      log.logError( "Error handling menu right click on job entry through extension point", e );
    }
  }


  @VisibleForTesting void createPopUpMenuExtension() {
    try {
      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.SpoonPopupMenuExtension.id, selectionTree );
    } catch ( Exception e ) {
      log.logError( "Error handling menu right click on job entry through extension point", e );
    }
  }

  public String getActiveTabText() {
    if ( tabfolder.getSelected() == null ) {
      return null;
    }
    return tabfolder.getSelected().getText();
  }

  public void refreshGraph() {
    if ( shell.isDisposed() ) {
      return;
    }

    TabItem tabItem = tabfolder.getSelected();
    if ( tabItem == null ) {
      return;
    }

    TabMapEntry tabMapEntry = delegates.tabs.getTab( tabItem );
    if ( tabMapEntry != null ) {
      if ( tabMapEntry.getObject() instanceof TransGraph ) {
        TransGraph transGraph = (TransGraph) tabMapEntry.getObject();
        transGraph.redraw();
      }
      if ( tabMapEntry.getObject() instanceof JobGraph ) {
        JobGraph jobGraph = (JobGraph) tabMapEntry.getObject();
        jobGraph.redraw();
      }
    }

    setShellText();
  }

  public StepMeta newStep( TransMeta transMeta ) {
    return newStep( transMeta, true, true );
  }

  public StepMeta newStep( TransMeta transMeta, boolean openit, boolean rename ) {
    if ( transMeta == null ) {
      return null;
    }
    TreeItem[] ti = selectionTree.getSelection();
    StepMeta inf = null;

    if ( ti.length == 1 ) {
      String stepType = ti[0].getText();
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "Spoon.Log.NewStep" ) + stepType ); // "New step: "
      }

      inf = newStep( transMeta, stepType, stepType, openit, rename );
    }

    return inf;
  }

  /**
   * Allocate new step, optionally open and rename it.
   *
   * @param name
   *          Name of the new step
   * @param description
   *          Description of the type of step
   * @param openit
   *          Open the dialog for this step?
   * @param rename
   *          Rename this step?
   *
   * @return The newly created StepMeta object.
   *
   */
  public StepMeta newStep( TransMeta transMeta, String name, String description, boolean openit, boolean rename ) {
    return newStep( transMeta, null, name, description, openit, rename );
  }

    /**
     * Allocate new step, optionally open and rename it.
     *
     * @param id
     *          Id of the new step
     * @param name
     *          Name of the new step
     * @param description
     *          Description of the type of step
     * @param openit
     *          Open the dialog for this step?
     * @param rename
     *          Rename this step?
     *
     * @return The newly created StepMeta object.
     *
     */
  public StepMeta newStep( TransMeta transMeta, String id, String name, String description, boolean openit, boolean rename ) {
    StepMeta inf = null;

    // See if we need to rename the step to avoid doubles!
    if ( rename && transMeta.findStep( name ) != null ) {
      int i = 2;
      String newName = name + " " + i;
      while ( transMeta.findStep( newName ) != null ) {
        i++;
        newName = name + " " + i;
      }
      name = newName;
    }

    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface stepPlugin = id != null ? registry.findPluginWithId( StepPluginType.class, id )
      : registry.findPluginWithName( StepPluginType.class, description );

    try {
      if ( stepPlugin != null ) {
        StepMetaInterface info = (StepMetaInterface) registry.loadClass( stepPlugin );

        info.setDefault();

        if ( openit ) {
          StepDialogInterface dialog = this.getStepEntryDialog( info, transMeta, name );
          if ( dialog != null ) {
            name = dialog.open();
          }
        }
        inf = new StepMeta( stepPlugin.getIds()[0], name, info );

        if ( name != null ) {
          // OK pressed in the dialog: we have a step-name
          String newName = name;
          StepMeta stepMeta = transMeta.findStep( newName );
          int nr = 2;
          while ( stepMeta != null ) {
            newName = name + " " + nr;
            stepMeta = transMeta.findStep( newName );
            nr++;
          }
          if ( nr > 2 ) {
            inf.setName( newName );
            MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
            // "This stepName already exists.  Spoon changed the stepName to ["+newName+"]"
            mb.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.ChangeStepname.Message", newName ) );
            mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.ChangeStepname.Title" ) );
            mb.open();
          }
          inf.setLocation( 20, 20 ); // default location at (20,20)
          transMeta.addStep( inf );
          addUndoNew( transMeta, new StepMeta[] { inf }, new int[] { transMeta.indexOfStep( inf ) } );

          // Also store it in the pluginHistory list...
          props.increasePluginHistory( stepPlugin.getIds()[0] );

          // stepHistoryChanged = true;

          refreshTree();
        } else {
          return null; // Cancel pressed in dialog.
        }
        setShellText();
      }
    } catch ( KettleException e ) {
      String filename = stepPlugin.getErrorHelpFile();
      if ( !Utils.isEmpty( filename ) ) {
        // OK, in stead of a normal error message, we give back the
        // content of the error help file... (HTML)
        FileInputStream fis = null;
        try {
          StringBuilder content = new StringBuilder();

          fis = new FileInputStream( new File( filename ) );
          int ch = fis.read();
          while ( ch >= 0 ) {
            content.append( (char) ch );
            ch = fis.read();
          }

          ShowBrowserDialog sbd =
            new ShowBrowserDialog(
              // "Error help text"
              shell, BaseMessages.getString( PKG, "Spoon.Dialog.ErrorHelpText.Title" ), content.toString() );
          sbd.open();
        } catch ( Exception ex ) {
          new ErrorDialog( shell,
            // "Error showing help text"
            BaseMessages.getString( PKG, "Spoon.Dialog.ErrorShowingHelpText.Title" ), BaseMessages.getString(
              PKG, "Spoon.Dialog.ErrorShowingHelpText.Message" ), ex );
        } finally {
          if ( fis != null ) {
            try {
              fis.close();
            } catch ( Exception ex ) {
              log.logError( "Error closing plugin help file", ex );
            }
          }
        }
      } else {
        new ErrorDialog( shell,
          // "Error creating step"
          // "I was unable to create a new step"
          BaseMessages.getString( PKG, "Spoon.Dialog.UnableCreateNewStep.Title" ), BaseMessages.getString(
            PKG, "Spoon.Dialog.UnableCreateNewStep.Message" ), e );
      }
      return null;
    } catch ( Throwable e ) {
      if ( !shell.isDisposed() ) {
        new ErrorDialog( shell,
          // "Error creating step"
          BaseMessages.getString( PKG, "Spoon.Dialog.ErrorCreatingStep.Title" ), BaseMessages.getString(
            PKG, "Spoon.Dialog.UnableCreateNewStep.Message" ), e );
      }
      return null;
    }

    return inf;
  }

  private static String getJobTransfFullPath( EngineMetaInterface jobTransMeta ) {
    String fullPath = null;
    if ( jobTransMeta != null ) {
      fullPath =
          jobTransMeta.getRepositoryDirectory() + "/" + jobTransMeta.getName() + jobTransMeta.getRepositoryElementType()
              .getExtension();
    }
    return fullPath;
  }

  static boolean isVersionEnabled( Repository rep, EngineMetaInterface jobTransMeta ) {
    //It is not necessary to check VersioningEnabled on the server every time (see PDI-16684)
    if ( jobTransMeta.getVersioningEnabled() == null ) {
      boolean versioningEnabled = checkIsVersioningEnabledOnServer( rep, jobTransMeta );
      jobTransMeta.setVersioningEnabled( versioningEnabled );
      return versioningEnabled;
    }
    return jobTransMeta.getVersioningEnabled();
  }

  private static boolean checkIsVersioningEnabledOnServer( Repository rep, EngineMetaInterface jobTransMeta ) {
    boolean versioningEnabled = true;

    String fullPath = getJobTransfFullPath( jobTransMeta );
    RepositorySecurityProvider
      repositorySecurityProvider =
      rep != null && rep.getSecurityProvider() != null ? rep.getSecurityProvider() : null;
    if ( repositorySecurityProvider != null && fullPath != null ) {
      versioningEnabled = repositorySecurityProvider.isVersioningEnabled( fullPath );
    }

    return versioningEnabled;
  }

  public void setShellText() {
    if ( shell.isDisposed() ) {
      return;
    }

    String filename = null;
    String name = null;
    String version = null;
    ChangedFlagInterface changed = null;
    boolean versioningEnabled = true;

    AbstractMeta meta = getActiveJob() != null ? getActiveJob() : getActiveTransformation();
    if ( meta != null ) {
      changed = meta;
      filename = meta.getFilename();
      name = meta.getName();
      version = meta.getObjectRevision() == null ? null : meta.getObjectRevision().getName();
      try {
        versioningEnabled = isVersionEnabled( rep, meta );
      } catch ( KettleRepositoryLostException krle ) {
        handleRepositoryLost( krle );
      }
    }

    String text = "";

    if ( rep != null ) {
      text += APP_TITLE + " - [" + getRepositoryName() + "] ";
    } else {
      text += APP_TITLE + " - ";
    }

    if ( Utils.isEmpty( name ) ) {
      if ( !Utils.isEmpty( filename ) ) {
        text += filename;
      } else {
        String tab = getActiveTabText();
        if ( !Utils.isEmpty( tab ) ) {
          text += tab;
        } else {
          text += BaseMessages.getString( PKG, "Spoon.Various.NoName" ); // "[no name]"
        }
      }
    } else {
      text += name;
    }

    if ( versioningEnabled && !Utils.isEmpty( version ) ) {
      text += " v" + version;
    }

    if ( changed != null && changed.hasChanged() ) {
      text += " " + BaseMessages.getString( PKG, "Spoon.Various.Changed" );
    }

    shell.setText( text );

    markTabsChanged( false );
  }

  public void enableMenus() {
    boolean disableTransMenu = getActiveTransformation() == null;
    boolean disableJobMenu = getActiveJob() == null;
    boolean disableMetaMenu = getActiveMeta() == null;
    boolean isRepositoryRunning = rep != null;
    boolean disablePreviewButton = true;
    String activePerspectiveId = null;
    SpoonPerspectiveManager manager = SpoonPerspectiveManager.getInstance();
    if ( manager != null && manager.getActivePerspective() != null ) {
      activePerspectiveId = manager.getActivePerspective().getId();
    }
    boolean etlPerspective = false;
    if ( activePerspectiveId != null && activePerspectiveId.length() > 0 ) {
      etlPerspective = activePerspectiveId.equals( MainSpoonPerspective.ID );
    }

    TransGraph transGraph = getActiveTransGraph();
    if ( transGraph != null ) {
      disablePreviewButton = !( transGraph.isRunning() && !transGraph.isHalting() );
    }
    boolean disableSave = true;
    boolean disableDatabaseExplore = true;
    TabItemInterface currentTab = getActiveTabitem();
    if ( currentTab != null && currentTab.canHandleSave() ) {
      disableSave = !currentTab.hasContentChanged();
    }
    EngineMetaInterface meta = getActiveMeta();
    if ( meta != null ) {
      disableSave = !meta.canSave();
      disableDatabaseExplore = false;
    }

    org.pentaho.ui.xul.dom.Document doc;
    if ( mainSpoonContainer != null ) {
      doc = mainSpoonContainer.getDocumentRoot();
      if ( doc != null ) {
        if ( etlPerspective ) {
          doc.getElementById( "file" ).setVisible( etlPerspective );
          doc.getElementById( "edit" ).setVisible( etlPerspective );
          doc.getElementById( "view" ).setVisible( etlPerspective );
          doc.getElementById( "action" ).setVisible( etlPerspective );
          doc.getElementById( "tools" ).setVisible( etlPerspective );
          doc.getElementById( "help" ).setVisible( etlPerspective );
          doc.getElementById( "help-welcome" ).setVisible( etlPerspective );
          doc.getElementById( "help-plugins" ).setVisible( true );
        }
        // Only enable certain menu-items if we need to.
        disableMenuItem( doc, "file-new-database", disableTransMenu && disableJobMenu );
        disableMenuItem( doc, "menubar-new-database", disableTransMenu && disableJobMenu );
        disableMenuItem( doc, "menubar-new-slave", disableTransMenu && disableJobMenu );
        disableMenuItem( doc, "file-save", disableTransMenu && disableJobMenu && disableMetaMenu || disableSave );
        disableMenuItem( doc, "toolbar-file-save", disableTransMenu
          && disableJobMenu && disableMetaMenu || disableSave );
        disableMenuItem( doc, "file-save-as", disableTransMenu && disableJobMenu && disableMetaMenu || disableSave );
        disableMenuItem( doc, "toolbar-file-save-as", disableTransMenu
          && disableJobMenu && disableMetaMenu || disableSave );
        disableMenuItem( doc, "file-close", disableTransMenu && disableJobMenu && disableMetaMenu );
        disableMenuItem( doc, "file-print", disableTransMenu && disableJobMenu );
        disableMenuItem( doc, "file-export-to-xml", disableTransMenu && disableJobMenu );
        disableMenuItem( doc, "file-export-all-to-xml", disableTransMenu && disableJobMenu );

        // Disable the undo and redo menus if there is no active transformation
        // or active job
        // DO NOT ENABLE them otherwise ... leave that to the undo/redo settings
        //
        disableMenuItem( doc, UNDO_MENU_ITEM, disableTransMenu && disableJobMenu );
        disableMenuItem( doc, REDO_MENU_ITEM, disableTransMenu && disableJobMenu );

        disableMenuItem( doc, "edit-clear-selection", disableTransMenu && disableJobMenu );
        disableMenuItem( doc, "edit-select-all", disableTransMenu && disableJobMenu );
        updateSettingsMenu( doc, disableTransMenu, disableJobMenu );
        disableMenuItem( doc, "edit-settings", disableTransMenu && disableJobMenu && disableMetaMenu );

        // View Menu
        ( (XulMenuitem) doc.getElementById( "view-results" ) ).setSelected( isExecutionResultsPaneVisible() );
        disableMenuItem( doc, "view-results", transGraph == null && disableJobMenu );
        disableMenuItem( doc, "view-zoom-in", disableTransMenu && disableJobMenu );
        disableMenuItem( doc, "view-zoom-out", disableTransMenu && disableJobMenu );
        disableMenuItem( doc, "view-zoom-100pct", disableTransMenu && disableJobMenu );

        // Transformations
        disableMenuItem( doc, "process-run", disableTransMenu && disablePreviewButton && disableJobMenu );
        disableMenuItem( doc, "process-run-options", disableTransMenu && disablePreviewButton && disableJobMenu );
        disableMenuItem( doc, "trans-replay", disableTransMenu && disablePreviewButton );
        disableMenuItem( doc, "trans-preview", disableTransMenu && disablePreviewButton );
        disableMenuItem( doc, "trans-debug", disableTransMenu && disablePreviewButton );
        disableMenuItem( doc, "trans-verify", disableTransMenu );
        disableMenuItem( doc, "trans-impact", disableTransMenu );
        disableMenuItem( doc, "trans-get-sql", disableTransMenu );
        disableMenuItem( doc, "trans-last-impact", disableTransMenu );

        // Tools
        disableMenuItem( doc, "repository-explore", !isRepositoryRunning );
        disableMenuItem( doc, "tools-dabase-explore", !isRepositoryRunning && disableDatabaseExplore );
        disableMenuItem( doc, "repository-clear-shared-object-cache", !isRepositoryRunning );
        disableMenuItem( doc, "toolbar-expore-repository", !isRepositoryRunning );
        disableMenuItem( doc, "repository-export-all", !isRepositoryRunning );
        disableMenuItem( doc, "repository-import-directory", !isRepositoryRunning );
        disableMenuItem( doc, "trans-last-preview", !isRepositoryRunning || disableTransMenu );

        // Wizard
        disableMenuItem( doc, "wizard-connection", disableTransMenu && disableJobMenu );
        disableMenuItem( doc, "wizard-copy-table", disableTransMenu && disableJobMenu );
        disableMenuItem( doc, "wizard-copy-tables", isRepositoryRunning && disableTransMenu && disableJobMenu );

        disableMenuItem( doc, "database-inst-dependancy", !isRepositoryRunning );

        SpoonPluginManager.getInstance().notifyLifecycleListeners( SpoonLifeCycleEvent.MENUS_REFRESHED );

        MenuManager menuManager = getMenuBarManager();
        menuManager.updateAll( true );

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
  private void updateSettingsMenu( org.pentaho.ui.xul.dom.Document doc, boolean disableTransMenu,
    boolean disableJobMenu ) {
    XulMenuitem settingsItem = (XulMenuitem) doc.getElementById( "edit-settings" );
    if ( settingsItem != null ) {
      if ( disableTransMenu && !disableJobMenu ) {
        settingsItem.setAcceltext( "CTRL-J" );
        settingsItem.setAccesskey( "ctrl-j" );
      } else if ( !disableTransMenu && disableJobMenu ) {
        settingsItem.setAcceltext( "CTRL-T" );
        settingsItem.setAccesskey( "ctrl-t" );
      } else {
        settingsItem.setAcceltext( "" );
        settingsItem.setAccesskey( "" );
      }
    }
  }

  public void addSpoonMenuController( ISpoonMenuController menuController ) {
    if ( menuControllers != null ) {
      menuControllers.add( menuController );
    }
  }

  public boolean removeSpoonMenuController( ISpoonMenuController menuController ) {
    if ( menuControllers != null ) {
      return menuControllers.remove( menuController );
    }
    return false;
  }

  public ISpoonMenuController removeSpoonMenuController( String menuControllerName ) {
    ISpoonMenuController result = null;

    if ( menuControllers != null ) {
      for ( ISpoonMenuController menuController : menuControllers ) {
        if ( menuController.getName().equals( menuControllerName ) ) {
          result = menuController;
          menuControllers.remove( result );
          break;
        }
      }
    }

    return result;
  }

  private void disableMenuItem( org.pentaho.ui.xul.dom.Document doc, String itemId, boolean disable ) {
    XulComponent menuItem = doc.getElementById( itemId );
    if ( menuItem != null ) {
      menuItem.setDisabled( disable );
    } else {
      log.logError( "Non-Fatal error : Menu Item with id = " + itemId + " does not exist! Check 'menubar.xul'" );
    }
  }

  private void markTabsChanged( boolean force ) {

    for ( TabMapEntry entry : delegates.tabs.getTabs() ) {
      if ( entry.getTabItem().isDisposed() ) {
        continue;
      }

      boolean changed = force || entry.getObject().hasContentChanged();
      if ( changed ) {
        // Call extension point to alert plugins that a transformation or job has changed
        Object tabObject = entry.getObject().getManagedObject();
        String changedId = null;
        if ( tabObject instanceof TransMeta ) {
          changedId = KettleExtensionPoint.TransChanged.id;
        } else if ( tabObject instanceof JobMeta ) {
          changedId = KettleExtensionPoint.JobChanged.id;
        } else {
          changed = false;
        }

        if ( changedId != null ) {
          try {
            if ( force ) {
              ( (AbstractMeta) tabObject ).setChanged();
            }
            ExtensionPointHandler.callExtensionPoint( log, changedId, tabObject );
          } catch ( KettleException e ) {
            // fails gracefully
          }
        }
      }
      entry.getTabItem().setChanged( changed );
    }
  }

  /**
   * Check to see if any jobs or transformations are dirty
   * @return true if any of the open jobs or trans are marked dirty
   */
  public boolean isTabsChanged() {
    for ( TabMapEntry entry : delegates.tabs.getTabs() ) {
      if ( entry.getTabItem().isDisposed() ) {
        continue;
      }

      if ( entry.getObject().hasContentChanged() ) {
        return true;
      }
    }

    return false;
  }

  public void printFile() {
    TransMeta transMeta = getActiveTransformation();
    if ( transMeta != null ) {
      printTransFile( transMeta );
    }

    JobMeta jobMeta = getActiveJob();
    if ( jobMeta != null ) {
      printJobFile( jobMeta );
    }
  }

  private void printTransFile( TransMeta transMeta ) {
    TransGraph transGraph = getActiveTransGraph();
    if ( transGraph == null ) {
      return;
    }

    PrintSpool ps = new PrintSpool();
    Printer printer = ps.getPrinter( shell );

    // Create an image of the screen
    Point max = transMeta.getMaximum();

    Image img = transGraph.getTransformationImage( printer, max.x, max.y, 1.0f );

    ps.printImage( shell, img );

    img.dispose();
    ps.dispose();
  }

  private void printJobFile( JobMeta jobMeta ) {
    JobGraph jobGraph = getActiveJobGraph();
    if ( jobGraph == null ) {
      return;
    }

    PrintSpool ps = new PrintSpool();
    Printer printer = ps.getPrinter( shell );

    // Create an image of the screen
    Point max = jobMeta.getMaximum();

    Image img = jobGraph.getJobImage( printer, max.x, max.y, 1.0f );

    ps.printImage( shell, img );

    img.dispose();
    ps.dispose();
  }

  public TransGraph getActiveTransGraph() {
    if ( tabfolder != null ) {
      if ( tabfolder.getSelected() == null ) {
        return null;
      }
    } else {
      return null;
    }
    if ( delegates != null && delegates.tabs != null ) {
      TabMapEntry mapEntry = delegates.tabs.getTab( tabfolder.getSelected() );
      if ( mapEntry != null ) {
        if ( mapEntry.getObject() instanceof TransGraph ) {
          return (TransGraph) mapEntry.getObject();
        }
      }
    }
    return null;
  }

  public JobGraph getActiveJobGraph() {
    if ( delegates != null && delegates.tabs != null && tabfolder != null ) {
      TabMapEntry mapEntry = delegates.tabs.getTab( tabfolder.getSelected() );
      if ( mapEntry != null && mapEntry.getObject() instanceof JobGraph ) {
        return (JobGraph) mapEntry.getObject();
      }
    }
    return null;
  }

  public EngineMetaInterface getActiveMeta() {
    SpoonPerspectiveManager manager = SpoonPerspectiveManager.getInstance();
    if ( manager != null && manager.getActivePerspective() != null ) {
      return manager.getActivePerspective().getActiveMeta();
    }
    return null;
  }

  public TabItemInterface getActiveTabitem() {

    if ( tabfolder == null ) {
      return null;
    }
    TabItem tabItem = tabfolder.getSelected();
    if ( tabItem == null ) {
      return null;
    }
    if ( delegates != null && delegates.tabs != null ) {
      TabMapEntry mapEntry = delegates.tabs.getTab( tabItem );
      if ( mapEntry != null ) {
        return mapEntry.getObject();
      } else {
        return null;
      }
    }
    return null;
  }

  /**
   * @return The active TransMeta object by looking at the selected TransGraph, TransLog, TransHist If nothing valueable
   *         is selected, we return null
   */
  public TransMeta getActiveTransformation() {
    EngineMetaInterface meta = getActiveMeta();
    if ( meta instanceof TransMeta ) {
      return (TransMeta) meta;
    }
    return null;
  }

  /**
   * @return The active JobMeta object by looking at the selected JobGraph, JobLog, JobHist If nothing valueable is
   *         selected, we return null
   */
  public JobMeta getActiveJob() {
    EngineMetaInterface meta = getActiveMeta();
    if ( meta instanceof JobMeta ) {
      return (JobMeta) meta;
    }
    return null;
  }

  public UndoInterface getActiveUndoInterface() {
    return (UndoInterface) this.getActiveMeta();
  }

  public TransMeta findTransformation( String tabItemText ) {
    if ( delegates != null && delegates.trans != null ) {
      return delegates.trans.getTransformation( tabItemText );
    } else {
      return null;
    }
  }

  public JobMeta findJob( String tabItemText ) {
    if ( delegates != null && delegates.jobs != null ) {
      return delegates.jobs.getJob( tabItemText );
    } else {
      return null;
    }

  }

  public TransMeta[] getLoadedTransformations() {
    if ( delegates != null && delegates.trans != null ) {
      List<TransMeta> list = delegates.trans.getTransformationList();
      return list.toArray( new TransMeta[list.size()] );
    } else {
      return null;
    }
  }

  public JobMeta[] getLoadedJobs() {
    if ( delegates != null && delegates.jobs != null ) {
      List<JobMeta> list = delegates.jobs.getJobList();
      return list.toArray( new JobMeta[list.size()] );
    } else {
      return null;
    }
  }

  public void saveSettings() {
    if ( shell.isDisposed() ) {
      // we cannot save the settings, it's too late
      return;
    }
    WindowProperty windowProperty = new WindowProperty( shell );
    windowProperty.setName( APP_TITLE );
    props.setScreen( windowProperty );

    props.setLogLevel( DefaultLogLevel.getLogLevel().getCode() );
    if ( sashform.getWeights()[0] != 0 ) {
      props.setSashWeights( sashform.getWeights() );
    }

    // Also save the open files...
    // Go over the list of tabs, then add the info to the list
    // of open tab files in PropsUI
    //
    props.getOpenTabFiles().clear();

    for ( TabMapEntry entry : delegates.tabs.getTabs() ) {
      String fileType = null;
      String filename = null;
      String directory = null;
      String connection = null;
      int openType = 0;
      if ( entry.getObjectType() == ObjectType.TRANSFORMATION_GRAPH ) {
        fileType = LastUsedFile.FILE_TYPE_TRANSFORMATION;
        TransMeta transMeta = (TransMeta) entry.getObject().getManagedObject();
        filename = rep != null ? transMeta.getName() : transMeta.getFilename();
        directory = transMeta.getRepositoryDirectory().toString();
        openType = LastUsedFile.OPENED_ITEM_TYPE_MASK_GRAPH;
        entry.setObjectName( transMeta.getName() );
        connection = transMeta.getVariable( CONNECTION );
      } else if ( entry.getObjectType() == ObjectType.JOB_GRAPH ) {
        fileType = LastUsedFile.FILE_TYPE_JOB;
        JobMeta jobMeta = (JobMeta) entry.getObject().getManagedObject();
        filename = rep != null ? jobMeta.getName() : jobMeta.getFilename();
        directory = jobMeta.getRepositoryDirectory().toString();
        openType = LastUsedFile.OPENED_ITEM_TYPE_MASK_GRAPH;
        entry.setObjectName( jobMeta.getName() );
        connection = jobMeta.getVariable( CONNECTION );
      }

      if ( fileType != null ) {
        props.addOpenTabFile( fileType, filename, directory, rep != null, rep != null ? rep.getName() : null, openType,
          connection );
      }
    }

    props.saveProps();
  }

  public void loadSettings() {
    LogLevel logLevel = LogLevel.getLogLevelForCode( props.getLogLevel() );
    DefaultLogLevel.setLogLevel( logLevel );
    log.setLogLevel( logLevel );
    KettleLogStore.getAppender().setMaxNrLines( props.getMaxNrLinesInLog() );

    // transMeta.setMaxUndo(props.getMaxUndo());
    DBCache.getInstance().setActive( props.useDBCache() );
  }

  public void changeLooks() {
    if ( !selectionTree.isDisposed() ) {
      props.setLook( selectionTree );
    }
    props.setLook( tabfolder.getSwtTabset(), Props.WIDGET_STYLE_TAB );

    refreshTree();
    refreshGraph();
  }

  public void undoAction( UndoInterface undoInterface ) {
    if ( undoInterface == null ) {
      return;
    }

    TransAction ta = undoInterface.previousUndo();
    if ( ta == null ) {
      return;
    }

    setUndoMenu( undoInterface ); // something changed: change the menu

    if ( undoInterface instanceof TransMeta ) {
      delegates.trans.undoTransformationAction( (TransMeta) undoInterface, ta );
      if ( ta.getType() == TransAction.TYPE_ACTION_DELETE_STEP ) {
        setUndoMenu( undoInterface ); // something changed: change the menu
        handleSelectedStepOnUndo( (TransMeta) undoInterface );
        ta = undoInterface.viewPreviousUndo();
        if ( ta != null && ta.getType() == TransAction.TYPE_ACTION_DELETE_HOP ) {
          ta = undoInterface.previousUndo();
          delegates.trans.undoTransformationAction( (TransMeta) undoInterface, ta );
        }
      }

    }
    if ( undoInterface instanceof JobMeta ) {
      delegates.jobs.undoJobAction( (JobMeta) undoInterface, ta );
      if ( ta.getType() == TransAction.TYPE_ACTION_DELETE_JOB_ENTRY ) {
        setUndoMenu( undoInterface ); // something changed: change the menu
        ta = undoInterface.viewPreviousUndo();
        if ( ta != null && ta.getType() == TransAction.TYPE_ACTION_DELETE_JOB_HOP ) {
          ta = undoInterface.previousUndo();
          delegates.jobs.undoJobAction( (JobMeta) undoInterface, ta );
        }
      }
    }

    // Put what we undo in focus
    if ( undoInterface instanceof TransMeta ) {
      TransGraph transGraph = delegates.trans.findTransGraphOfTransformation( (TransMeta) undoInterface );
      transGraph.forceFocus();
    }
    if ( undoInterface instanceof JobMeta ) {
      JobGraph jobGraph = delegates.jobs.findJobGraphOfJob( (JobMeta) undoInterface );
      jobGraph.forceFocus();
    }
  }

  public void redoAction( UndoInterface undoInterface ) {
    if ( undoInterface == null ) {
      return;
    }

    TransAction ta = undoInterface.nextUndo();
    if ( ta == null ) {
      return;
    }

    setUndoMenu( undoInterface ); // something changed: change the menu

    if ( undoInterface instanceof TransMeta ) {
      delegates.trans.redoTransformationAction( (TransMeta) undoInterface, ta );
      if ( ta.getType() == TransAction.TYPE_ACTION_DELETE_HOP ) {
        setUndoMenu( undoInterface ); // something changed: change the menu
        ta = undoInterface.viewNextUndo();
        if ( ta != null && ta.getType() == TransAction.TYPE_ACTION_DELETE_STEP ) {
          ta = undoInterface.nextUndo();
          delegates.trans.redoTransformationAction( (TransMeta) undoInterface, ta );
        }
      }

    }
    if ( undoInterface instanceof JobMeta ) {
      delegates.jobs.redoJobAction( (JobMeta) undoInterface, ta );
      if ( ta.getType() == TransAction.TYPE_ACTION_DELETE_JOB_HOP ) {
        setUndoMenu( undoInterface ); // something changed: change the menu
        ta = undoInterface.viewNextUndo();
        if ( ta != null && ta.getType() == TransAction.TYPE_ACTION_DELETE_JOB_ENTRY ) {
          ta = undoInterface.nextUndo();
          delegates.jobs.redoJobAction( (JobMeta) undoInterface, ta );
        }
      }
    }


    // Put what we redo in focus
    if ( undoInterface instanceof TransMeta ) {
      TransGraph transGraph = delegates.trans.findTransGraphOfTransformation( (TransMeta) undoInterface );
      transGraph.forceFocus();
    }
    if ( undoInterface instanceof JobMeta ) {
      JobGraph jobGraph = delegates.jobs.findJobGraphOfJob( (JobMeta) undoInterface );
      jobGraph.forceFocus();
    }
  }

  /**
   * Sets the text and enabled settings for the undo and redo menu items
   *
   * @param undoInterface
   *          the object which holds the undo/redo information
   */
  public void setUndoMenu( UndoInterface undoInterface ) {
    if ( shell.isDisposed() ) {
      return;
    }

    TransAction prev = undoInterface != null ? undoInterface.viewThisUndo() : null;
    TransAction next = undoInterface != null ? undoInterface.viewNextUndo() : null;

    // Set the menubar text and enabled flags
    XulMenuitem item = (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById( UNDO_MENU_ITEM );
    item.setLabel( prev == null ? UNDO_UNAVAILABLE : BaseMessages.getString(
      PKG, "Spoon.Menu.Undo.Available", prev.toString() ) );
    item.setDisabled( prev == null );
    item = (XulMenuitem) mainSpoonContainer.getDocumentRoot().getElementById( REDO_MENU_ITEM );
    item.setLabel( next == null ? REDO_UNAVAILABLE : BaseMessages.getString(
      PKG, "Spoon.Menu.Redo.Available", next.toString() ) );
    item.setDisabled( next == null );
  }

  public void addUndoNew( UndoInterface undoInterface, Object[] obj, int[] position ) {
    addUndoNew( undoInterface, obj, position, false );
  }

  public void addUndoNew( UndoInterface undoInterface, Object[] obj, int[] position, boolean nextAlso ) {
    undoInterface.addUndo( obj, null, position, null, null, TransMeta.TYPE_UNDO_NEW, nextAlso );
    setUndoMenu( undoInterface );
  }

  // Undo delete object
  public void addUndoDelete( UndoInterface undoInterface, Object[] obj, int[] position ) {
    addUndoDelete( undoInterface, obj, position, false );
  }

  // Undo delete object
  public void addUndoDelete( UndoInterface undoInterface, Object[] obj, int[] position, boolean nextAlso ) {
    undoInterface.addUndo( obj, null, position, null, null, TransMeta.TYPE_UNDO_DELETE, nextAlso );
    setUndoMenu( undoInterface );
  }

  // Change of step, connection, hop or note...
  @Override
  public void addUndoPosition( UndoInterface undoInterface, Object[] obj, int[] pos, Point[] prev, Point[] curr ) {
    // It's better to store the indexes of the objects, not the objects
    // itself!
    undoInterface.addUndo( obj, null, pos, prev, curr, JobMeta.TYPE_UNDO_POSITION, false );
    setUndoMenu( undoInterface );
  }

  // Change of step, connection, hop or note...
  public void addUndoChange( UndoInterface undoInterface, Object[] from, Object[] to, int[] pos ) {
    addUndoChange( undoInterface, from, to, pos, false );
  }

  // Change of step, connection, hop or note...
  public void addUndoChange( UndoInterface undoInterface, Object[] from, Object[] to, int[] pos, boolean nextAlso ) {
    undoInterface.addUndo( from, to, pos, null, null, JobMeta.TYPE_UNDO_CHANGE, nextAlso );
    setUndoMenu( undoInterface );
  }

  private void handleSelectedStepOnUndo( TransMeta transMeta ) {
    if ( transMeta.getSelectedSteps().size() == 1 ) {
      getActiveTransGraph().setCurrentStep( transMeta.getSelectedSteps().get( 0 ) );
    }
  }

  /**
   * Checks *all* the steps in the transformation, puts the result in remarks list
   */
  public void checkTrans( TransMeta transMeta ) {
    checkTrans( transMeta, false );
  }

  /**
   * Check the steps in a transformation
   *
   * @param only_selected
   *          True: Check only the selected steps...
   */
  public void checkTrans( TransMeta transMeta, boolean only_selected ) {
    if ( transMeta == null ) {
      return;
    }
    TransGraph transGraph = delegates.trans.findTransGraphOfTransformation( transMeta );
    if ( transGraph == null ) {
      return;
    }

    CheckTransProgressDialog ctpd =
      new CheckTransProgressDialog( shell, transMeta, transGraph.getRemarks(), only_selected );
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
    if ( transMeta == null ) {
      return;
    }
    TransGraph transGraph = delegates.trans.findTransGraphOfTransformation( transMeta );
    if ( transGraph == null ) {
      return;
    }

    CheckResultDialog crd = new CheckResultDialog( transMeta, shell, SWT.NONE, transGraph.getRemarks() );
    String stepName = crd.open();
    if ( stepName != null ) {
      // Go to the indicated step!
      StepMeta stepMeta = transMeta.findStep( stepName );
      if ( stepMeta != null ) {
        delegates.steps.editStep( transMeta, stepMeta );
      }
    }
  }

  public void analyseImpact( TransMeta transMeta ) {
    if ( transMeta == null ) {
      return;
    }
    TransGraph transGraph = delegates.trans.findTransGraphOfTransformation( transMeta );
    if ( transGraph == null ) {
      return;
    }

    AnalyseImpactProgressDialog aipd = new AnalyseImpactProgressDialog( shell, transMeta, transGraph.getImpact() );
    transGraph.setImpactFinished( aipd.open() );
    if ( transGraph.isImpactFinished() ) {
      showLastImpactAnalyses( transMeta );
    }
  }

  public void showLastImpactAnalyses( TransMeta transMeta ) {
    if ( transMeta == null ) {
      return;
    }
    TransGraph transGraph = delegates.trans.findTransGraphOfTransformation( transMeta );
    if ( transGraph == null ) {
      return;
    }

    List<Object[]> rows = new ArrayList<>();
    RowMetaInterface rowMeta = null;
    for ( int i = 0; i < transGraph.getImpact().size(); i++ ) {
      DatabaseImpact ii = transGraph.getImpact().get( i );
      RowMetaAndData row = ii.getRow();
      rowMeta = row.getRowMeta();
      rows.add( row.getData() );
    }

    if ( rows.size() > 0 ) {
      // Display all the rows...
      PreviewRowsDialog prd =
        new PreviewRowsDialog( shell, Variables.getADefaultVariableSpace(), SWT.NONE, "-", rowMeta, rows );
      prd.setTitleMessage(
        // "Impact analyses"
        // "Result of analyses:"
        BaseMessages.getString( PKG, "Spoon.Dialog.ImpactAnalyses.Title" ), BaseMessages.getString(
          PKG, "Spoon.Dialog.ImpactAnalyses.Message" ) );
      prd.open();
    } else {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
      if ( transGraph.isImpactFinished() ) {
        // "As far as I can tell, this transformation has no impact on any database."
        mb.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.TransformationNoImpactOnDatabase.Message" ) );
      } else {
        // "Please run the impact analyses first on this transformation."
        mb.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.RunImpactAnalysesFirst.Message" ) );
      }
      mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.ImpactAnalyses.Title" ) ); // Impact
      mb.open();
    }
  }

  public void toClipboard( String clipText ) {
    try {
      GUIResource.getInstance().toClipboard( clipText );
    } catch ( Throwable e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "Spoon.Dialog.ExceptionCopyToClipboard.Title" ), BaseMessages
          .getString( PKG, "Spoon.Dialog.ExceptionCopyToClipboard.Message" ), e );
    }
  }

  public String fromClipboard() {
    try {
      return GUIResource.getInstance().fromClipboard();
    } catch ( Throwable e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "Spoon.Dialog.ExceptionPasteFromClipboard.Title" ), BaseMessages
          .getString( PKG, "Spoon.Dialog.ExceptionPasteFromClipboard.Message" ), e );
      return null;
    }
  }

  /**
   * Paste transformation from the clipboard...
   *
   */
  public void pasteTransformation() {

    if ( RepositorySecurityUI.verifyOperations( shell, rep,
        RepositoryOperation.MODIFY_TRANSFORMATION, RepositoryOperation.EXECUTE_TRANSFORMATION ) ) {
      return;
    }

    if ( log.isDetailed() ) {
      // "Paste transformation from the clipboard!"
      log.logDetailed( BaseMessages.getString( PKG, "Spoon.Log.PasteTransformationFromClipboard" ) );
    }
    String xml = fromClipboard();
    try {
      Document doc = XMLHandler.loadXMLString( xml );

      TransMeta transMeta = new TransMeta( XMLHandler.getSubNode( doc, TransMeta.XML_TAG ), rep );
      setTransMetaVariables( transMeta );
      addTransGraph( transMeta ); // create a new tab
      sharedObjectsFileMap.put( transMeta.getSharedObjects().getFilename(), transMeta.getSharedObjects() );
      refreshGraph();
      refreshTree();
    } catch ( KettleException e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "Spoon.Dialog.ErrorPastingTransformation.Title" ), BaseMessages
          .getString( PKG, "Spoon.Dialog.ErrorPastingTransformation.Message" ), e );
    }
  }

  /**
   * Paste job from the clipboard...
   *
   */
  public void pasteJob() {

    if ( RepositorySecurityUI.verifyOperations( shell, rep,
        RepositoryOperation.MODIFY_JOB, RepositoryOperation.EXECUTE_JOB ) ) {
      return;
    }

    String xml = fromClipboard();
    try {
      Document doc = XMLHandler.loadXMLString( xml );
      JobMeta jobMeta = new JobMeta( XMLHandler.getSubNode( doc, JobMeta.XML_TAG ), rep, this );
      addJobGraph( jobMeta ); // create a new tab
      refreshGraph();
      refreshTree();
    } catch ( KettleException e ) {
      new ErrorDialog( shell,
        // Error pasting transformation
        // "An error occurred pasting a transformation from the clipboard"
        BaseMessages.getString( PKG, "Spoon.Dialog.ErrorPastingJob.Title" ), BaseMessages.getString(
          PKG, "Spoon.Dialog.ErrorPastingJob.Message" ), e );
    }
  }

  public void copyTransformation( TransMeta transMeta ) {
    if ( transMeta == null ) {
      return;
    }
    try {
      if ( RepositorySecurityUI.verifyOperations(
          shell, rep, RepositoryOperation.MODIFY_TRANSFORMATION, RepositoryOperation.EXECUTE_TRANSFORMATION ) ) {
        return;
      }

      toClipboard( XMLHandler.getXMLHeader() + transMeta.getXML() );
    } catch ( Exception ex ) {
      new ErrorDialog( getShell(), "Error", "Error encoding to XML", ex );
    }
  }

  public void copyJob( JobMeta jobMeta ) {
    if ( jobMeta == null ) {
      return;
    }
    if ( RepositorySecurityUI.verifyOperations(
        shell, rep, RepositoryOperation.MODIFY_JOB, RepositoryOperation.EXECUTE_JOB ) ) {
      return;
    }

    toClipboard( XMLHandler.getXMLHeader() + jobMeta.getXML() );
  }

  public void copyTransformationImage( TransMeta transMeta ) {
    TransGraph transGraph = delegates.trans.findTransGraphOfTransformation( transMeta );
    if ( transGraph == null ) {
      return;
    }

    Clipboard clipboard = GUIResource.getInstance().getNewClipboard();

    Point area = transMeta.getMaximum();
    Image image = transGraph.getTransformationImage( Display.getCurrent(), area.x, area.y, 1.0f );
    clipboard.setContents(
      new Object[] { image.getImageData() }, new Transfer[] { ImageTransfer.getInstance() } );
  }

  /**
   * @return Either a TransMeta or JobMeta object
   */
  public HasDatabasesInterface getActiveHasDatabasesInterface() {
    return getActiveTransformationOrJob();
  }

  public HasSlaveServersInterface getActiveHasSlaveServersInterface() {
    return getActiveTransformationOrJob();
  }

  private AbstractMeta getActiveTransformationOrJob() {
    TransMeta transMeta = getActiveTransformation();
    if ( transMeta != null ) {
      return transMeta;
    }
    return getActiveJob();
  }

  /**
   * Shows a wizard that creates a new database connection...
   *
   */
  public void createDatabaseWizard() {
    HasDatabasesInterface hasDatabasesInterface = getActiveHasDatabasesInterface();
    if ( hasDatabasesInterface == null ) {
      return; // nowhere to put the new database
    }

    CreateDatabaseWizard cdw = new CreateDatabaseWizard();
    DatabaseMeta newDBInfo = cdw.createAndRunDatabaseWizard( shell, props, hasDatabasesInterface.getDatabases() );
    if ( newDBInfo != null ) { // finished
      hasDatabasesInterface.addDatabase( newDBInfo );
      refreshTree( DBConnectionFolderProvider.STRING_CONNECTIONS );
      refreshGraph();
    }
  }

  public List<DatabaseMeta> getActiveDatabases() {
    Map<String, DatabaseMeta> map = new Hashtable<>();

    HasDatabasesInterface hasDatabasesInterface = getActiveHasDatabasesInterface();
    if ( hasDatabasesInterface != null ) {
      for ( int i = 0; i < hasDatabasesInterface.nrDatabases(); i++ ) {
        map.put( hasDatabasesInterface.getDatabase( i ).getName(), hasDatabasesInterface.getDatabase( i ) );
      }
    }
    if ( rep != null ) {
      try {
        List<DatabaseMeta> repDBs = rep.readDatabases();
        for ( DatabaseMeta databaseMeta : repDBs ) {
          map.put( databaseMeta.getName(), databaseMeta );
        }
      } catch ( Exception e ) {
        log.logError( "Unexpected error reading databases from the repository: " + e.toString() );
        log.logError( Const.getStackTracker( e ) );
      }
    }

    List<DatabaseMeta> databases = new ArrayList<>();
    databases.addAll( map.values() );

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
    if ( databases.size() == 0 ) {
      return; // Nothing to do here
    }

    final CopyTableWizardPage1 page1 = new CopyTableWizardPage1( "1", databases );
    final CopyTableWizardPage2 page2 = new CopyTableWizardPage2( "2" );

    Wizard wizard = new Wizard() {
      @Override
      public boolean performFinish() {
        return delegates.db.copyTable( page1.getSourceDatabase(), page1.getTargetDatabase(), page2.getSelection() );
      }

      /**
       * @see org.eclipse.jface.wizard.Wizard#canFinish()
       */
      @Override
      public boolean canFinish() {
        return page2.canFinish();
      }
    };

    wizard.addPage( page1 );
    wizard.addPage( page2 );

    WizardDialog wd = new WizardDialog( shell, wizard );
    WizardDialog.setDefaultImage( GUIResource.getInstance().getImageWizard() );
    wd.setMinimumPageSize( 700, 400 );
    wd.updateSize();
    wd.open();
  }

  @Override
  public String toString() {
    return APP_NAME;
  }

  public void handleStartOptions( CommandLineOption[] options ) {

    // note that at this point the rep object is populated by previous calls

    StringBuilder optionRepname = getCommandLineOption( options, "rep" ).getArgument();
    StringBuilder optionFilename = getCommandLineOption( options, "file" ).getArgument();
    StringBuilder optionDirname = getCommandLineOption( options, "dir" ).getArgument();
    StringBuilder optionTransname = getCommandLineOption( options, "trans" ).getArgument();
    StringBuilder optionJobname = getCommandLineOption( options, "job" ).getArgument();
    // StringBuilder optionUsername = getCommandLineOption(options,
    // "user").getArgument();
    // StringBuilder optionPassword = getCommandLineOption(options,
    // "pass").getArgument();

    try {
      // Read kettle transformation specified on command-line?
      if ( !Utils.isEmpty( optionRepname ) || !Utils.isEmpty( optionFilename ) ) {
        if ( !Utils.isEmpty( optionRepname ) ) {
          if ( rep != null ) {

            if ( Utils.isEmpty( optionDirname ) ) {
              optionDirname = new StringBuilder( RepositoryDirectory.DIRECTORY_SEPARATOR );
            }

            // Options /file, /job and /trans are mutually
            // exclusive
            int t =
              ( Utils.isEmpty( optionFilename ) ? 0 : 1 )
                + ( Utils.isEmpty( optionJobname ) ? 0 : 1 ) + ( Utils.isEmpty( optionTransname ) ? 0 : 1 );
            if ( t > 1 ) {
              // "More then one mutually exclusive options /file, /job and /trans are specified."
              log.logError( BaseMessages.getString( PKG, "Spoon.Log.MutuallyExcusive" ) );
            } else if ( t == 1 ) {
              if ( !Utils.isEmpty( optionFilename ) ) {
                openFile( optionFilename.toString(), false );
              } else {
                // OK, if we have a specified job or
                // transformation, try to load it...
                // If not, keep the repository logged
                // in.
                RepositoryDirectoryInterface rdi = rep.findDirectory( optionDirname.toString() );
                if ( rdi == null ) {
                  log.logError( BaseMessages.getString( PKG, "Spoon.Log.UnableFindDirectory", optionDirname
                    .toString() ) ); // "Can't find directory ["+dirname+"] in the repository."
                } else {
                  if ( !Utils.isEmpty( optionTransname ) ) {
                    TransMeta transMeta =
                      rep.loadTransformation( optionTransname.toString(), rdi, null, true, null ); // reads
                    // last
                    // version
                    transMeta.clearChanged();
                    transMeta.setInternalKettleVariables();
                    addTransGraph( transMeta );
                  } else {
                    // Try to load a specified job
                    // if any
                    JobMeta jobMeta = rep.loadJob( optionJobname.toString(), rdi, null, null ); // reads
                    // last
                    // version
                    jobMeta.clearChanged();
                    jobMeta.setInternalKettleVariables();
                    addJobGraph( jobMeta );
                  }
                }
              }
            }
          } else {
            // "No repositories defined on this system."
            log.logError( BaseMessages.getString( PKG, "Spoon.Log.NoRepositoriesDefined" ) );
          }
        } else if ( !Utils.isEmpty( optionFilename ) ) {
          openFile( optionFilename.toString(), false );
        }
      }
    } catch ( KettleException ke ) {
      hideSplash();
      log.logError( BaseMessages.getString( PKG, "Spoon.Log.ErrorOccurred" ) + Const.CR + ke.getMessage() );
      log.logError( Const.getStackTracker( ke ) );
      // do not just eat the exception
      new ErrorDialog( shell, BaseMessages.getString( PKG, "Spoon.Log.ErrorOccurred" ), BaseMessages.getString(
        PKG, "Spoon.Log.ErrorOccurred" )
        + Const.CR + ke.getMessage(), ke );
      rep = null;
    }
  }

  private void loadLastUsedFiles() {
    if ( props.openLastFile() ) {
      if ( log.isDetailed() ) {
        // "Trying to open the last file used."
        log.logDetailed( BaseMessages.getString( PKG, "Spoon.Log.TryingOpenLastUsedFile" ) );
      }

      List<LastUsedFile> lastUsedFiles = props.getOpenTabFiles();
      for ( LastUsedFile lastUsedFile : lastUsedFiles ) {
        try {
          if ( !lastUsedFile.isSourceRepository()
            || lastUsedFile.isSourceRepository() && rep != null
            && rep.getName().equals( lastUsedFile.getRepositoryName() ) ) {
            loadLastUsedFileAtStartup( lastUsedFile, rep == null ? null : rep.getName() );
          }
        } catch ( Exception e ) {
          hideSplash();
          new ErrorDialog(
            shell, BaseMessages.getString( PKG, "Spoon.LoadLastUsedFile.Exception.Title" ), BaseMessages
              .getString( PKG, "Spoon.LoadLastUsedFile.Exception.Message", lastUsedFile.toString() ), e );
        }
      }
    }
  }

  public void start( CommandLineOption[] options ) throws KettleException {

    // Read the start option parameters
    //
    handleStartOptions( options );

    // Enable menus based on whether user was able to login or not
    //
    enableMenus();

    // enable perspective switching
    SpoonPerspectiveManager.getInstance().setForcePerspective( false );

    if ( splash != null ) {
      splash.dispose();
      splash = null;
    }

    // If we are a MILESTONE or RELEASE_CANDIDATE
    if ( !ValueMetaString.convertStringToBoolean( System.getProperty( "KETTLE_HIDE_DEVELOPMENT_VERSION_WARNING", "N" ) )
      && Const.RELEASE.equals( Const.ReleaseType.MILESTONE ) ) {

      // display the same warning message
      MessageBox dialog = new MessageBox( shell, SWT.ICON_WARNING );
      dialog.setText( BaseMessages.getString( PKG, "Spoon.Warning.DevelopmentRelease.Title" ) );
      dialog.setMessage( BaseMessages.getString(
        PKG, "Spoon.Warning.DevelopmentRelease.Message", Const.CR, BuildVersion.getInstance().getVersion() ) );
      dialog.open();
    }
  }

  private void waitForDispose() {

    boolean retryAfterError; // Enable the user to retry and
    // continue after fatal error
    do {
      retryAfterError = false; // reset to false after error otherwise
      // it will loop forever after
      // closing Spoon
      try {

        while ( getShell() != null && !getShell().isDisposed() ) {
          if ( !readAndDispatch() ) {
            sleep();
          }
        }
      } catch ( Throwable e ) {
        // "An unexpected error occurred in Spoon: probable cause: please close all windows before stopping Spoon! "
        log.logError( BaseMessages.getString( PKG, "Spoon.Log.UnexpectedErrorOccurred" )
          + Const.CR + e.getMessage() );
        log.logError( Const.getStackTracker( e ) );
        try {
          new ErrorDialog( shell, BaseMessages.getString( PKG, "Spoon.Log.UnexpectedErrorOccurred" ), BaseMessages
            .getString( PKG, "Spoon.Log.UnexpectedErrorOccurred" )
            + Const.CR + e.getMessage(), e );

          ShowMessageDialog showMessageDialog = new ShowMessageDialog( shell, SWT.ERROR | SWT.IGNORE | SWT.SAVE,
            BaseMessages.getString( PKG, "Spoon.FatalError.Title" ),
            BaseMessages.getString( PKG, "Spoon.FatalError.Message1" ) + Const.CR + Const.CR
              + BaseMessages.getString( PKG, "Spoon.FatalError.Message2" ) + Const.CR, false );
          showMessageDialog.setType( Const.SHOW_FATAL_ERROR ); // Adjusts spacing within dialog
          showMessageDialog.setCentered( true );

          if ( SWT.SAVE == showMessageDialog.open() ) { // save changed files and quit spoon
            hasFatalError = true;
            quitFile( true );
          } else { // continue working with spoon
            retryAfterError = true;
          }
        } catch ( Throwable e1 ) {
          // When the opening of a dialog crashed, we can not do
          // anything more here
        }
      }
    } while ( retryAfterError );
    if ( !display.isDisposed() ) {
      display.update();
    }
    dispose();
    if ( log.isBasic() ) {
      log.logBasic( APP_NAME + " " + BaseMessages.getString( PKG, "Spoon.Log.AppHasEnded" ) ); // " has ended."
    }

    // Close the logfile
    if ( fileLoggingEventListener != null ) {
      try {
        fileLoggingEventListener.close();
      } catch ( Exception e ) {
        LogChannel.GENERAL.logError( "Error closing logging file", e );
      }
      KettleLogStore.getAppender().removeLoggingEventListener( fileLoggingEventListener );
    }
  }

  // public Splash splash;

  // public CommandLineOption options[];

  public static CommandLineOption getCommandLineOption( CommandLineOption[] options, String opt ) {
    for ( CommandLineOption option : options ) {
      if ( option.getOption().equals( opt ) ) {
        return option;
      }
    }
    return null;
  }

  public static CommandLineOption[] getCommandLineArgs( List<String> args ) {

    CommandLineOption[] clOptions =
      new CommandLineOption[] {
        new CommandLineOption( "rep", "Repository name", new StringBuilder() ),
        new CommandLineOption( "user", "Repository username", new StringBuilder() ),
        new CommandLineOption( "pass", "Repository password", new StringBuilder() ),
        new CommandLineOption( "job", "The name of the job to launch", new StringBuilder() ),
        new CommandLineOption( "trans", "The name of the transformation to launch", new StringBuilder() ),
        new CommandLineOption( "dir", "The directory (don't forget the leading /)", new StringBuilder() ),
        new CommandLineOption( "file", "The filename (Transformation in XML) to launch", new StringBuilder() ),
        new CommandLineOption(
          "level", "The logging level (Basic, Detailed, Debug, Rowlevel, Error, Nothing)",
          new StringBuilder() ),
        new CommandLineOption( "logfile", "The logging file to write to", new StringBuilder() ),
        new CommandLineOption(
          "log", "The logging file to write to (deprecated)", new StringBuilder(), false, true ),
        new CommandLineOption( "perspective", "The perspective to start in", new StringBuilder(), false, true ) };

    // start with the default logger until we find out otherwise
    //
    log = new LogChannel( APP_NAME );

    // Parse the options...
    if ( !CommandLineOption.parseArguments( args, clOptions, log ) ) {
      log.logError( "Command line option not understood" );
      System.exit( 8 );
    }

    String kettleRepname = Const.getEnvironmentVariable( "KETTLE_REPOSITORY", null );
    String kettleUsername = Const.getEnvironmentVariable( "KETTLE_USER", null );
    String kettlePassword = Const.getEnvironmentVariable( "KETTLE_PASSWORD", null );

    if ( !Utils.isEmpty( kettleRepname ) ) {
      clOptions[0].setArgument( new StringBuilder( kettleRepname ) );
    }
    if ( !Utils.isEmpty( kettleUsername ) ) {
      clOptions[1].setArgument( new StringBuilder( kettleUsername ) );
    }
    if ( !Utils.isEmpty( kettlePassword ) ) {
      clOptions[2].setArgument( new StringBuilder( kettlePassword ) );
    }

    return clOptions;
  }

  /**
   * Loads the {@link LastUsedFile} residing the the {@link Repository} with {@code repoName} without "tracking it" -
   * meaning that it is not explicitly added to the "recent" file collection, since it is expected to already exist
   * in this collections.
   *
   * @param lastUsedFile   the {@link LastUsedFile} being loaded
   * @param repositoryName the name of the {@link Repository} containing the {@link LastUsedFile}
   * @throws KettleException
   */
  public void loadLastUsedFileAtStartup( LastUsedFile lastUsedFile, String repositoryName ) throws KettleException {
    // when loading tabs at startup, we do not need to track it, as it should already be added to the appropriate
    // lastUsedFile collections
    loadLastUsedFile( lastUsedFile, repositoryName, false, true );
  }

  /**
   * Loads the {@link LastUsedFile} residing the the {@link Repository} with {@code repoName}, and "tracks" it by
   * adding it to the "recent" files collection.
   *
   * @param lastUsedFile   the {@link LastUsedFile} being loaded
   * @param repositoryName the name of the {@link Repository} containing the {@link LastUsedFile}
   * @throws KettleException
   */
  public void loadLastUsedFile( LastUsedFile lastUsedFile, String repositoryName ) throws KettleException {
    loadLastUsedFile( lastUsedFile, repositoryName, true, false );
  }

  /**
   * Returns true if the {@link LastUsedFile} at the given {@code index} within the {@link
   * PropsUI#getLastUsedRepoFiles()} collection exists within the given {@code repo} {@link Repository},
   * and false otherwise.
   *
   * @param repo     the {@link Repository} containing the file
   * @param indexStr the String index of the file within the last used repo file collection
   * @return true if the {@link LastUsedFile} at the given {@code index} within the {@link
   * PropsUI#getLastUsedRepoFiles()} collection exists within the given {@code repo} {@link Repository},
   * and false otherwise
   * @throws KettleException
   */
  public boolean recentRepoFileExists( String repo, String indexStr ) {
    final LastUsedFile lastUsedFile = getLastUsedRepoFile( repo, indexStr );
    // this should never happen when used on a repo file, but we check just to be safe
    if ( lastUsedFile == null ) {
      return false;
    }
    // again, should never happen, since the file being selected is a valid repo file in this repo
    if ( !lastUsedFile.isSourceRepository() || !rep.getName().equalsIgnoreCase( lastUsedFile.getRepositoryName() ) ) {
      return false;
    }
    try {
      final RepositoryDirectoryInterface rdi = rep.findDirectory( lastUsedFile.getDirectory() );
      if ( rdi == null ) {
        return false;
      }
      final RepositoryObjectType fileType = lastUsedFile.isJob() ? RepositoryObjectType.JOB
        : RepositoryObjectType.TRANSFORMATION;
      return rep.exists( lastUsedFile.getFilename(), rdi, fileType );
    } catch ( final KettleException ke ) {
      // log
      return false;
    }
  }


  public LastUsedFile getLastUsedRepoFile( String repo, String indexStr ) {
    // this should never happen when used on a repo file, but we check just to be safe
    if ( rep == null ) {
      return null;
    }
    final int idx = Integer.parseInt( indexStr );
    final List<LastUsedFile> lastUsedFiles = props.getLastUsedRepoFiles().getOrDefault( repo, Collections.emptyList() );
    return lastUsedFiles.get( idx );
  }

  private void loadLastUsedFile(
      LastUsedFile lastUsedFile, String repositoryName, boolean trackIt, boolean isStartup ) throws KettleException {
    boolean useRepository = repositoryName != null;
    // Perhaps we need to connect to the repository?
    //
    if ( lastUsedFile.isSourceRepository() ) {
      if ( !Utils.isEmpty( lastUsedFile.getRepositoryName() ) ) {
        if ( useRepository && !lastUsedFile.getRepositoryName().equalsIgnoreCase( repositoryName ) ) {
          // We just asked...
          useRepository = false;
        }
      }
    }

    if ( useRepository && lastUsedFile.isSourceRepository() ) {
      if ( rep != null ) { // load from this repository...
        if ( rep.getName().equalsIgnoreCase( lastUsedFile.getRepositoryName() ) ) {
          RepositoryDirectoryInterface rdi = rep.findDirectory( lastUsedFile.getDirectory() );
          if ( rdi != null ) {
            // does the file exist in the repo?
            final RepositoryObjectType fileType = lastUsedFile.isJob() ? RepositoryObjectType.JOB
              : RepositoryObjectType.TRANSFORMATION;
            if ( !rep.exists( lastUsedFile.getFilename(), rdi, fileType ) ) {
              // open an warning dialog only if this file was explicitly selected to be opened; on startup, simply skip
              // opening this file
              if ( isStartup ) {
                if ( log.isDetailed() ) {
                  log.logDetailed( BaseMessages.getString( PKG, "Spoon.log.openingMissingFile" ) );
                }
              } else {
                final Dialog dlg = new SimpleMessageDialog( shell,
                  BaseMessages.getString( PKG, "Spoon.Dialog.MissingRecentFile.Title" ),
                  BaseMessages.getString( PKG, "Spoon.Dialog.MissingRecentFile.Message",
                    lastUsedFile.getLongFileType().toLowerCase() ), MessageDialog.ERROR,
                  BaseMessages.getString( PKG, "System.Button.Close" ),
                  MISSING_RECENT_DLG_WIDTH, SimpleMessageDialog.BUTTON_WIDTH );
                dlg.open();
              }
            } else {
              // Are we loading a transformation or a job?
              if ( lastUsedFile.isTransformation() ) {
                if ( log.isDetailed() ) {
                  // "Auto loading transformation ["+lastfiles[0]+"] from repository directory ["+lastdirs[0]+"]"
                  log.logDetailed( BaseMessages.getString( PKG, "Spoon.Log.AutoLoadingTransformation", lastUsedFile
                    .getFilename(), lastUsedFile.getDirectory() ) );
                }
                TransLoadProgressDialog tlpd =
                  new TransLoadProgressDialog( shell, rep, lastUsedFile.getFilename(), rdi, null );
                TransMeta transMeta = tlpd.open();
                if ( transMeta != null ) {
                  if ( trackIt ) {
                    props.addLastFile( LastUsedFile.FILE_TYPE_TRANSFORMATION, lastUsedFile.getFilename(), rdi
                      .getPath(), true, rep.getName(), getUsername(), null, null );
                  }
                  // transMeta.setFilename(lastUsedFile.getFilename());
                  transMeta.clearChanged();
                  addTransGraph( transMeta );
                  refreshTree();
                }
              } else if ( lastUsedFile.isJob() ) {
                JobLoadProgressDialog progressDialog =
                  new JobLoadProgressDialog( shell, rep, lastUsedFile.getFilename(), rdi, null );
                JobMeta jobMeta = progressDialog.open();
                if ( jobMeta != null ) {
                  if ( trackIt ) {
                    props.addLastFile(
                      LastUsedFile.FILE_TYPE_JOB, lastUsedFile.getFilename(), rdi.getPath(), true, rep
                        .getName(), getUsername(), null, null );
                  }
                  jobMeta.clearChanged();
                  addJobGraph( jobMeta );
                }
              }
              refreshTree();
            }
          }
        }
      }
    }

    // open files stored locally, not in the repository
    if ( !lastUsedFile.isSourceRepository() && !Utils.isEmpty( lastUsedFile.getFilename() ) ) {
      String connection = getLastUsedConnection( lastUsedFile );
      Variables variables = null;
      if ( connection != null ) {
        variables = new Variables();
        variables.setVariable( CONNECTION, connection );
      }
      if ( lastUsedFile.isTransformation() ) {
        openFile( lastUsedFile.getFilename(), variables, rep != null );
      }
      if ( lastUsedFile.isJob() ) {
        openFile( lastUsedFile.getFilename(), variables, false );
      }
      refreshTree();
    }
  }

  private String getLastUsedConnection( LastUsedFile lastUsedFile ) {
    if ( !Utils.isEmpty( lastUsedFile.getConnection() ) && !lastUsedFile.getConnection().equals( "null" ) ) {
      return lastUsedFile.getConnection();
    }
    return null;
  }

  /**
   * Create a new SelectValues step in between this step and the previous. If the previous fields are not there, no
   * mapping can be made, same with the required fields.
   *
   * @param stepMeta
   *          The target step to map against.
   */
  // retry of required fields acquisition
  public void generateFieldMapping( TransMeta transMeta, StepMeta stepMeta ) {
    try {
      if ( stepMeta != null ) {
        StepMetaInterface smi = stepMeta.getStepMetaInterface();
        RowMetaInterface targetFields = smi.getRequiredFields( transMeta );
        RowMetaInterface sourceFields = transMeta.getPrevStepFields( stepMeta );

        // Build the mapping: let the user decide!!
        String[] source = sourceFields.getFieldNames();
        for ( int i = 0; i < source.length; i++ ) {
          ValueMetaInterface v = sourceFields.getValueMeta( i );
          source[i] += EnterMappingDialog.STRING_ORIGIN_SEPARATOR + v.getOrigin() + ")";
        }
        String[] target = targetFields.getFieldNames();

        EnterMappingDialog dialog = new EnterMappingDialog( shell, source, target );
        List<SourceToTargetMapping> mappings = dialog.open();
        if ( mappings != null ) {
          // OK, so we now know which field maps where.
          // This allows us to generate the mapping using a
          // SelectValues Step...
          SelectValuesMeta svm = new SelectValuesMeta();
          svm.allocate( mappings.size(), 0, 0 );

          //CHECKSTYLE:Indentation:OFF
          for ( int i = 0; i < mappings.size(); i++ ) {
            SourceToTargetMapping mapping = mappings.get( i );
            svm.getSelectFields()[i].setName( sourceFields.getValueMeta( mapping.getSourcePosition() ).getName() );
            svm.getSelectFields()[i].setRename( target[mapping.getTargetPosition()] );
            svm.getSelectFields()[i].setLength( -1 );
            svm.getSelectFields()[i].setPrecision( -1 );
          }
          // a new comment. Sincerely yours CO ;)
          // Now that we have the meta-data, create a new step info object
          String stepName = stepMeta.getName() + " Mapping";
          stepName = transMeta.getAlternativeStepname( stepName ); // if
          // it's already there, rename it.

          StepMeta newStep = new StepMeta( "SelectValues", stepName, svm );
          newStep.setLocation( stepMeta.getLocation().x + 20, stepMeta.getLocation().y + 20 );
          newStep.setDraw( true );

          transMeta.addStep( newStep );
          addUndoNew( transMeta, new StepMeta[] { newStep }, new int[] { transMeta.indexOfStep( newStep ) } );

          // Redraw stuff...
          refreshTree();
          refreshGraph();
        }
      } else {
        throw new KettleException( "There is no target to do a field mapping against!" );
      }
    } catch ( KettleException e ) {
      new ErrorDialog(
        shell, "Error creating mapping",
        "There was an error when Kettle tried to generate a field mapping against the target step", e );
    }
  }

  public boolean isDefinedSchemaExist( String[] schemaNames ) {
    // Before we start, check if there are any partition schemas defined...
    if ( ( schemaNames == null ) || ( schemaNames.length == 0 ) ) {
      MessageBox box = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
      box.setText( "Create a partition schema" );
      box.setMessage( "You first need to create one or more partition schemas in "
        + "the transformation settings dialog before you can select one!" );
      box.open();
      return false;
    }
    return true;
  }

  public void editPartitioning( TransMeta transMeta, StepMeta stepMeta ) {
    String[] schemaNames;
    try {
      schemaNames = pickupPartitionSchemaNames( transMeta );
    } catch ( KettleException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "Spoon.ErrorDialog.Title" ),
        BaseMessages.getString( PKG, "Spoon.ErrorDialog.ErrorFetchingFromRepo.PartitioningSchemas" ),
        e
      );
      return;
    }
    try {
      /*Check if Partition schema has already defined*/
      if ( isDefinedSchemaExist( schemaNames ) ) {

        /*Prepare settings for Method selection*/
        PluginRegistry registry = PluginRegistry.getInstance();
        List<PluginInterface> plugins = registry.getPlugins( PartitionerPluginType.class );
        int exactSize = StepPartitioningMeta.methodDescriptions.length + plugins.size();
        PartitionSettings settings = new PartitionSettings( exactSize, transMeta, stepMeta, this );
        settings.fillOptionsAndCodesByPlugins( plugins );

        /*Method selection*/
        PartitionMethodSelector methodSelector = new PartitionMethodSelector();
        String partitionMethodDescription =
          methodSelector.askForPartitionMethod( shell, settings );
        if ( !StringUtil.isEmpty( partitionMethodDescription ) ) {
          String method = settings.getMethodByMethodDescription( partitionMethodDescription );
          int methodType = StepPartitioningMeta.getMethodType( method );

          settings.updateMethodType( methodType );
          settings.updateMethod( method );

          /*Schema selection*/
          MethodProcessor methodProcessor = MethodProcessorFactory.create( methodType );
          methodProcessor.schemaSelection( settings, shell, delegates );
        }
        addUndoChange( settings.getTransMeta(), new StepMeta[] { settings.getBefore() },
          new StepMeta[] { settings.getAfter() }, new int[] { settings.getTransMeta()
            .indexOfStep( settings.getStepMeta() ) }
        );
        refreshGraph();
      }
    } catch ( Exception e ) {
      new ErrorDialog(
        shell, "Error",
        "There was an unexpected error while editing the partitioning method specifics:", e );
    }
  }

  /**
   * Select a clustering schema for this step.
   *
   * @param stepMeta
   *          The step to set the clustering schema for.
   */
  public void editClustering( TransMeta transMeta, StepMeta stepMeta ) {
    editClustering( transMeta, Collections.singletonList( stepMeta ) );
  }

  /**
   * Select a clustering schema for this step.
   *
   * @param stepMetas The steps (at least one!) to set the clustering schema for.
   */
  public void editClustering( TransMeta transMeta, List<StepMeta> stepMetas ) {
    String[] clusterSchemaNames = transMeta.getClusterSchemaNames();

    StepMeta stepMeta = stepMetas.get( 0 );
    int idx = -1;
    if ( stepMeta.getClusterSchema() != null ) {
      idx = transMeta.getClusterSchemas().indexOf( stepMeta.getClusterSchema() );
    }

    EnterSelectionDialog dialog = new EnterSelectionDialog(
      shell,
      clusterSchemaNames,
      BaseMessages.getString( PKG, "Spoon.Dialog.SelectClusteringSchema.Title" ),
      BaseMessages.getString( PKG, "Spoon.Dialog.SelectClusteringSchema.Message" )
    );
    String schemaName = dialog.open( idx );

    if ( schemaName == null ) {
      for ( StepMeta step : stepMetas ) {
        step.setClusterSchema( null );
      }
    } else {
      ClusterSchema clusterSchema = transMeta.findClusterSchema( schemaName );
      for ( StepMeta step : stepMetas ) {
        step.setClusterSchema( clusterSchema );
      }
    }

    transMeta.setChanged();
    refreshTree();
    refreshGraph();
  }

  public void createKettleArchive( TransMeta transMeta ) {
    if ( transMeta == null ) {
      return;
    }
    JarfileGenerator.generateJarFile( transMeta );
  }

  /**
   * This creates a new partitioning schema, edits it and adds it to the transformation metadata if its name is not a
   * duplicate of any of existing
   */
  public void newPartitioningSchema( TransMeta transMeta ) {
    delegates.partitions.newPartitioningSchema( transMeta );
  }

  private void editPartitionSchema( TransMeta transMeta, PartitionSchema partitionSchema ) {
    delegates.partitions.editPartitionSchema( transMeta, partitionSchema );
  }

  private void delPartitionSchema( TransMeta transMeta, PartitionSchema partitionSchema ) {
    delegates.partitions.delPartitionSchema( transMeta, partitionSchema );
  }

  /**
   * This creates a new clustering schema, edits it and adds it to the transformation metadata if its name is not a
   * duplicate of any of existing
   */
  public void newClusteringSchema( TransMeta transMeta ) {
    delegates.clusters.newClusteringSchema( transMeta );
  }

  /**
   * This creates a slave server, edits it and adds it to the transformation metadata
   *
   */
  public void newSlaveServer( HasSlaveServersInterface hasSlaveServersInterface ) {
    delegates.slaves.newSlaveServer( hasSlaveServersInterface );
  }

  public void delSlaveServer( HasSlaveServersInterface hasSlaveServersInterface, SlaveServer slaveServer ) {
    try {
      delegates.slaves.delSlaveServer( hasSlaveServersInterface, slaveServer );
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "Spoon.Dialog.ErrorDeletingSlave.Title" ), BaseMessages
        .getString( PKG, "Spoon.Dialog.ErrorDeletingSlave.Message" ), e );
    }
  }

  protected void editSlaveServer( SlaveServer slaveServer ) {
     List<SlaveServer> existingServers = getActiveAbstractMeta().getSlaveServers();
    // List<SlaveServer> existingServers = pickupSlaveServers( getActiveAbstractMeta() );
    delegates.slaves.edit( slaveServer, existingServers );
  }

  /**
   * Sends transformation to slave server
   *
   * @param executionConfiguration
   */
  public void sendTransformationXMLToSlaveServer( TransMeta transMeta,
    TransExecutionConfiguration executionConfiguration ) {
    try {
      Trans.sendToSlaveServer( transMeta, executionConfiguration, rep, metaStoreSupplier.get() );
    } catch ( Exception e ) {
      new ErrorDialog( shell, "Error", "Error sending transformation to server", e );
    }
  }

  public void runFile() {
    executeFile( true, false, false, false, false, null, false, false );
  }

  public void runOptionsFile() {
    executeFile( true, false, false, false, false, null, false, true );
  }

  public void replayTransformation() {
    TransExecutionConfiguration tc = this.getTransExecutionConfiguration();
    executeFile(
      tc.isExecutingLocally(), tc.isExecutingRemotely(), tc.isExecutingClustered(), false, false, new Date(),
      false, false );
  }

  public void previewFile() {
    executeFile( true, false, false, true, false, null, true, false );
  }

  public void debugFile() {
    executeFile( true, false, false, false, true, null, true, false );
  }

  public void executeFile( boolean local, boolean remote, boolean cluster, boolean preview, boolean debug,
      Date replayDate, boolean safe, boolean show ) {

    TransMeta transMeta = getActiveTransformation();
    if ( transMeta != null ) {
      transMeta.setShowDialog( show || transMeta.isAlwaysShowRunOptions() );
      executeTransformation( transMeta, local, remote, cluster, preview, debug, replayDate, safe,
          transExecutionConfiguration.getLogLevel() );
    }

    JobMeta jobMeta = getActiveJob();
    if ( jobMeta != null ) {
      jobMeta.setShowDialog( show || jobMeta.isAlwaysShowRunOptions() );
      executeJob( jobMeta, local, remote, replayDate, safe, null, 0 );
    }

  }

  public void executeTransformation( final TransMeta transMeta, final boolean local, final boolean remote,
    final boolean cluster, final boolean preview, final boolean debug, final Date replayDate,
    final boolean safe, final LogLevel logLevel ) {

    if ( RepositorySecurityUI.verifyOperations( shell, rep, RepositoryOperation.EXECUTE_TRANSFORMATION ) ) {
      return;
    }

    Thread thread = new Thread() {
      @Override
      public void run() {
        getDisplay().asyncExec( new Runnable() {
          @Override
          public void run() {
            try {
              delegates.trans.executeTransformation(
                transMeta, local, remote, cluster, preview, debug, replayDate, safe, logLevel );
            } catch ( Exception e ) {
              new ErrorDialog(
                shell, "Execute transformation", "There was an error during transformation execution", e );
            }
          }
        } );
      }
    };
    thread.start();
  }

  public void executeJob( JobMeta jobMeta, boolean local, boolean remote, Date replayDate, boolean safe,
    String startCopyName, int startCopyNr ) {

    if ( RepositorySecurityUI.verifyOperations( shell, rep, RepositoryOperation.EXECUTE_JOB ) ) {
      return;
    }

    try {
      delegates.jobs.executeJob( jobMeta, local, remote, replayDate, safe, startCopyName, startCopyNr );
    } catch ( Exception e ) {
      new ErrorDialog( shell, "Execute job", "There was an error during job execution", e );
    }

  }

  public void addSpoonSlave( SlaveServer slaveServer ) {
    delegates.slaves.addSpoonSlave( slaveServer );
  }

  public void addJobHistory( JobMeta jobMeta, boolean select ) {
    JobGraph activeJobGraph = getActiveJobGraph();
    if ( activeJobGraph != null ) {
      activeJobGraph.jobHistoryDelegate.addJobHistory();
    }

    // delegates.jobs.addJobHistory(jobMeta, select);
  }

  public void paste() {
    String clipContent = fromClipboard();
    if ( clipContent != null ) {
      // Load the XML
      //
      try {
        Document document = XMLHandler.loadXMLString( clipContent );

        boolean transformation = XMLHandler.getSubNode( document, TransMeta.XML_TAG ) != null;
        boolean job = XMLHandler.getSubNode( document, JobMeta.XML_TAG ) != null;
        boolean steps = XMLHandler.getSubNode( document, Spoon.XML_TAG_TRANSFORMATION_STEPS ) != null;
        boolean jobEntries = XMLHandler.getSubNode( document, Spoon.XML_TAG_JOB_JOB_ENTRIES ) != null;

        if ( transformation ) {
          pasteTransformation();
        } else if ( job ) {
          pasteJob();
        } else if ( steps ) {
          TransGraph transGraph = getActiveTransGraph();
          if ( transGraph != null && transGraph.getLastMove() != null ) {
            pasteXML( transGraph.getManagedObject(), clipContent, transGraph.getLastMove() );
          }
        } else if ( jobEntries ) {
          JobGraph jobGraph = getActiveJobGraph();
          if ( jobGraph != null && jobGraph.getLastMove() != null ) {
            pasteXML( jobGraph.getManagedObject(), clipContent, jobGraph.getLastMove() );
          }

        }
      } catch ( KettleXMLException e ) {
        log.logError( "Unable to paste", e );
      }
    }

  }

  public JobEntryCopy newJobEntry( JobMeta jobMeta, String typeDesc, boolean openit ) {
    return delegates.jobs.newJobEntry( jobMeta, typeDesc, openit );
  }

  public JobEntryDialogInterface getJobEntryDialog( JobEntryInterface jei, JobMeta jobMeta ) {

    return delegates.jobs.getJobEntryDialog( jei, jobMeta );
  }

  public StepDialogInterface getStepEntryDialog( StepMetaInterface stepMeta, TransMeta transMeta, String stepName ) {
    try {
      return delegates.steps.getStepDialog( stepMeta, transMeta, stepName );
    } catch ( Throwable t ) {
      log.logError( "Could not create dialog for " + stepMeta.getDialogClassName(), t );
    }
    return null;
  }

  public void editJobEntry( JobMeta jobMeta, JobEntryCopy je ) {
    delegates.jobs.editJobEntry( jobMeta, je );
  }

  public void deleteJobEntryCopies( JobMeta jobMeta, JobEntryCopy[] jobEntry ) {
    delegates.jobs.deleteJobEntryCopies( jobMeta, jobEntry );
  }

  public void deleteJobEntryCopies( JobMeta jobMeta, JobEntryCopy jobEntry ) {
    delegates.jobs.deleteJobEntryCopies( jobMeta, jobEntry );
  }

  public void pasteXML( JobMeta jobMeta, String clipContent, Point loc ) {
    if ( RepositorySecurityUI.verifyOperations( shell, rep,
        RepositoryOperation.MODIFY_JOB, RepositoryOperation.EXECUTE_JOB ) ) {
      return;
    }
    delegates.jobs.pasteXML( jobMeta, clipContent, loc );
  }

  public void newJobHop( JobMeta jobMeta, JobEntryCopy fr, JobEntryCopy to ) {
    delegates.jobs.newJobHop( jobMeta, fr, to );
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

  public JobMeta ripDB( final List<DatabaseMeta> databases, final String jobName,
    final RepositoryDirectory repdir, final String directory, final DatabaseMeta sourceDbInfo,
    final DatabaseMeta targetDbInfo, final String[] tables ) {
    return delegates.jobs.ripDB( databases, jobName, repdir, directory, sourceDbInfo, targetDbInfo, tables );
  }

  /**
   * Set the core object state.
   *
   * @param state state to set
   */
  public void setCoreObjectsState( int state ) {
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

  public void setRepository( Repository rep ) {
    this.rep = rep;
    if ( rep != null ) {
      this.repositoryName = rep.getName();
      List<LastUsedFile> lastUsedFiles = getLastUsedRepoFiles();
      if ( !Utils.isEmpty( lastUsedFiles ) ) {
        String path = lastUsedFiles.get( 0 ).getDirectory() + "/" + lastUsedFiles.get( 0 ).getFilename();
        lastFileOpened = path;
        lastFileOpenedProvider = ProviderFilterType.REPOSITORY.toString();
      }
    } else {
      this.repositoryName = null;
      lastFileOpened = props.getLastUsedLocalFile();
      lastFileOpenedProvider = ProviderFilterType.LOCAL.toString();
    }
      if ( rep != null ) {
        this.capabilities = rep.getRepositoryMeta().getRepositoryCapabilities();
      }

    ConnectionManager.getInstance().reset();
    // Registering the UI Support classes
    UISupportRegistery.getInstance().registerUISupport(
      RepositorySecurityProvider.class, BaseRepositoryExplorerUISupport.class );
    UISupportRegistery
      .getInstance().registerUISupport( RepositorySecurityManager.class, ManageUserUISupport.class );
    if ( rep != null ) {
      SpoonPluginManager.getInstance().notifyLifecycleListeners( SpoonLifeCycleEvent.REPOSITORY_CHANGED );
    }
    delegates.update( this );
    enableMenus();
  }

  public void setRepositoryName( String repoName ) {
    this.repositoryName = repoName;
  }

  public void addMenuListener( String id, Object listener, String methodName ) {
    menuListeners.add( new Object[] { id, listener, methodName } );
  }

  @Override
  public void addTransGraph( TransMeta transMeta ) {
    delegates.trans.addTransGraph( transMeta );
  }

  @Override
  public void addJobGraph( JobMeta jobMeta ) {
    delegates.jobs.addJobGraph( jobMeta );
  }

  public boolean addSpoonBrowser( String name, String urlString, boolean isURL, LocationListener locationListener, Map<String, Runnable> functions, boolean showControls ) {
    return delegates.tabs.addSpoonBrowser( name, urlString, isURL, locationListener, functions, showControls );
  }

  public boolean addSpoonBrowser( String name, String urlString, LocationListener locationListener, boolean showControls ) {
    return delegates.tabs.addSpoonBrowser( name, urlString, locationListener, showControls );
  }

  public boolean addSpoonBrowser( String name, String urlString, LocationListener locationListener ) {
    return delegates.tabs.addSpoonBrowser( name, urlString, locationListener, true );
  }

  public boolean addSpoonBrowser( String name, String urlString, boolean showControls ) {
    return delegates.tabs.addSpoonBrowser( name, urlString, null, showControls );
  }

  @Override
  public boolean addSpoonBrowser( String name, String urlString ) {
    return delegates.tabs.addSpoonBrowser( name, urlString, null, true );
  }

  public TransExecutionConfiguration getTransExecutionConfiguration() {
    return transExecutionConfiguration;
  }

  public void editStepErrorHandling( TransMeta transMeta, StepMeta stepMeta ) {
    delegates.steps.editStepErrorHandling( transMeta, stepMeta );
  }

  public String editStep( TransMeta transMeta, StepMeta stepMeta ) {
    String stepname = delegates.steps.editStep( transMeta, stepMeta );
    sharedObjectSyncUtil.synchronizeSteps( stepMeta );
    return stepname;
  }

  public void dupeStep( TransMeta transMeta, StepMeta stepMeta ) {
    delegates.steps.dupeStep( transMeta, stepMeta );
  }

  public void delSteps( TransMeta transformation, StepMeta[] steps ) {
    delegates.steps.delSteps( transformation, steps );
  }

  public void delStep( TransMeta transMeta, StepMeta stepMeta ) {
    delegates.steps.delStep( transMeta, stepMeta );
  }

  public String makeTabName( EngineMetaInterface transMeta, boolean showingLocation ) {
    return delegates.tabs.makeTabName( transMeta, showingLocation );
  }

  public void newConnection() {
    delegates.db.newConnection();
  }

  public void getSQL() {
    delegates.db.getSQL();
  }

  @Override
  public boolean overwritePrompt( String message, String rememberText, String rememberPropertyName ) {
    return new PopupOverwritePrompter( shell, props ).overwritePrompt( message, rememberText, rememberPropertyName );
  }

  @Override
  public Object[] messageDialogWithToggle( String dialogTitle, Object image, String message, int dialogImageType,
      String[] buttonLabels, int defaultIndex, String toggleMessage, boolean toggleState ) {
    return GUIResource.getInstance().messageDialogWithToggle(
        shell, dialogTitle, (Image) image, message, dialogImageType, buttonLabels, defaultIndex, toggleMessage,
        toggleState );
  }

  @Override
  public boolean messageBox( final String message, final String text, final boolean allowCancel, final int type ) {

    final StringBuilder answer = new StringBuilder( "N" );

    display.syncExec( new Runnable() {

      @Override
      public void run() {

        int flags = SWT.OK;
        if ( allowCancel ) {
          flags |= SWT.CANCEL;
        }

        switch ( type ) {
          case Const.INFO:
            flags |= SWT.ICON_INFORMATION;
            break;
          case Const.ERROR:
            flags |= SWT.ICON_ERROR;
            break;
          case Const.WARNING:
            flags |= SWT.ICON_WARNING;
            break;
          default:
            break;
        }

        MessageBox mb = new MessageBox( shell, flags );
        // Set the Body Message
        mb.setMessage( message );
        // Set the title Message
        mb.setText( text );
        if ( mb.open() == SWT.OK ) {
          answer.setCharAt( 0, 'Y' );
        }
      }
    } );

    return "Y".equalsIgnoreCase( answer.toString() );
  }

  /**
   * @return the previewExecutionConfiguration
   */
  public TransExecutionConfiguration getTransPreviewExecutionConfiguration() {
    return transPreviewExecutionConfiguration;
  }

  /**
   * @param previewExecutionConfiguration
   *          the previewExecutionConfiguration to set
   */
  public void setTransPreviewExecutionConfiguration( TransExecutionConfiguration previewExecutionConfiguration ) {
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
   *          the debugExecutionConfiguration to set
   */
  public void setTransDebugExecutionConfiguration( TransExecutionConfiguration debugExecutionConfiguration ) {
    this.transDebugExecutionConfiguration = debugExecutionConfiguration;
  }

  /**
   * @param executionConfiguration
   *          the executionConfiguration to set
   */
  public void setTransExecutionConfiguration( TransExecutionConfiguration executionConfiguration ) {
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
   *          the jobExecutionConfiguration to set
   */
  public void setJobExecutionConfiguration( JobExecutionConfiguration jobExecutionConfiguration ) {
    this.jobExecutionConfiguration = jobExecutionConfiguration;
  }

  @Override
  public void update( ChangedFlagInterface o, Object arg ) {
    try {
      Method m = getClass().getMethod( arg.toString() );

      if ( m != null ) {
        m.invoke( this );
      }
    } catch ( Exception e ) {
      // ignore... let the other notifiers try to do something
      System.out.println( "Unable to update: " + e.getLocalizedMessage() );
    }
  }

  @Override
  public void consume( final LifeEventInfo info ) {

    if ( info.hasHint( LifeEventInfo.Hint.DISPLAY_BROWSER ) ) {
      display.asyncExec( new Runnable() {
        @Override
        public void run() {
          delegates.tabs.addSpoonBrowser( info.getName(), info.getMessage(), false, null );
        }
      } );

    } else {
      MessageBox box =
        new MessageBox( shell, ( info.getState() != LifeEventInfo.State.SUCCESS
          ? SWT.ICON_ERROR : SWT.ICON_INFORMATION )
          | SWT.OK );
      box.setText( info.getName() );
      box.setMessage( info.getMessage() );
      box.open();
    }

  }

  public void setLog() {
    LogSettingsDialog lsd = new LogSettingsDialog( shell, SWT.NONE, props );
    lsd.open();
    log.setLogLevel( DefaultLogLevel.getLogLevel() );
  }

  /**
   * @return the display
   */
  public Display getDisplay() {
    return display;
  }

  public void zoomIn() {
    TransGraph transGraph = getActiveTransGraph();
    if ( transGraph != null ) {
      transGraph.zoomIn();
    }
    JobGraph jobGraph = getActiveJobGraph();
    if ( jobGraph != null ) {
      jobGraph.zoomIn();
    }
  }

  public void zoomOut() {
    TransGraph transGraph = getActiveTransGraph();
    if ( transGraph != null ) {
      transGraph.zoomOut();
    }
    JobGraph jobGraph = getActiveJobGraph();
    if ( jobGraph != null ) {
      jobGraph.zoomOut();
    }
  }

  public void zoom100Percent() {
    TransGraph transGraph = getActiveTransGraph();
    if ( transGraph != null ) {
      transGraph.zoom100Percent();
    }
    JobGraph jobGraph = getActiveJobGraph();
    if ( jobGraph != null ) {
      jobGraph.zoom100Percent();
    }
  }

  public void setParametersAsVariablesInUI( NamedParams namedParameters, VariableSpace space ) {
    for ( String param : namedParameters.listParameters() ) {
      try {
        space.setVariable( param, Const.NVL( namedParameters.getParameterValue( param ), Const.NVL(
          namedParameters.getParameterDefault( param ), Const.NVL( space.getVariable( param ), "" ) ) ) );
      } catch ( Exception e ) {
        // ignore this
      }
    }
  }

  public void browseVersionHistory() {
    if ( rep == null ) {
      return;
    }
    TransGraph transGraph = getActiveTransGraph();
    if ( transGraph != null ) {
      transGraph.browseVersionHistory();
    }

    JobGraph jobGraph = getActiveJobGraph();
    if ( jobGraph != null ) {
      jobGraph.browseVersionHistory();
    }

  }

  public Trans findActiveTrans( Job job, JobEntryCopy jobEntryCopy ) {
    JobEntryTrans jobEntryTrans = job.getActiveJobEntryTransformations().get( jobEntryCopy );
    if ( jobEntryTrans == null ) {
      return null;
    }
    return jobEntryTrans.getTrans();
  }

  public Job findActiveJob( Job job, JobEntryCopy jobEntryCopy ) {
    JobEntryJob jobEntryJob = job.getActiveJobEntryJobs().get( jobEntryCopy );
    if ( jobEntryJob == null ) {
      return null;
    }
    return jobEntryJob.getJob();
  }

  public Object getSelectionObject() {
    return selectionObject;
  }

  public RepositoryDirectoryInterface getDefaultSaveLocation( RepositoryElementInterface repositoryElement ) {
    try {
      if ( getRepository() != defaultSaveLocationRepository ) {
        // The repository has changed, reset the defaultSaveLocation
        defaultSaveLocation = null;
        defaultSaveLocationRepository = null;
      }

      if ( defaultSaveLocation == null ) {
        if ( getRepository() != null ) {
          defaultSaveLocation = getRepository().getDefaultSaveDirectory( repositoryElement );
          defaultSaveLocationRepository = getRepository();
        } else {
          defaultSaveLocation = new RepositoryDirectory();
        }
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }

    return defaultSaveLocation;
  }

  /* ========================= XulEventSource Methods ========================== */

  protected PropertyChangeSupport changeSupport = new PropertyChangeSupport( this );

  @Override
  public void addPropertyChangeListener( PropertyChangeListener listener ) {
    changeSupport.addPropertyChangeListener( listener );
  }

  public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener ) {
    changeSupport.addPropertyChangeListener( propertyName, listener );
  }

  @Override
  public void removePropertyChangeListener( PropertyChangeListener listener ) {
    changeSupport.removePropertyChangeListener( listener );
  }

  protected void firePropertyChange( String attr, Object previousVal, Object newVal ) {
    if ( previousVal == null && newVal == null ) {
      return;
    }
    changeSupport.firePropertyChange( attr, previousVal, newVal );
  }

  /*
   * ========================= End XulEventSource Methods ==========================
   */

  /*
   * ========================= Start XulEventHandler Methods ==========================
   */

  @Override
  public Object getData() {
    return null;
  }

  @Override
  public String getName() {
    return "spoon";
  }

  @Override
  public XulDomContainer getXulDomContainer() {
    return getMainSpoonContainer();
  }

  @Override
  public void setData( Object arg0 ) {
  }

  @Override
  public void setName( String arg0 ) {
  }

  @Override
  public void setXulDomContainer( XulDomContainer arg0 ) {
  }

  public RepositorySecurityManager getSecurityManager() {
    return rep.getSecurityManager();
  }

  public void displayDbDependancies() {
    TreeItem[] selection = selectionTree.getSelection();
    if ( selection == null || selection.length != 1 ) {
      return;
    }
    // Clear all dependencies for select connection
    TreeItem parent = selection[0];
    if ( parent != null ) {
      int nrChilds = parent.getItemCount();
      if ( nrChilds > 0 ) {
        for ( int i = 0; i < nrChilds; i++ ) {
          parent.getItem( i ).dispose();
        }
      }
    }
    if ( rep == null ) {
      return;
    }

    try {

      final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
      String[] jobList = rep.getJobsUsingDatabase( databaseMeta.getObjectId() );
      String[] transList = rep.getTransformationsUsingDatabase( databaseMeta.getObjectId() );
      if ( jobList.length == 0 && transList.length == 0 ) {
        MessageBox box = new MessageBox( shell, SWT.ICON_INFORMATION | SWT.OK );
        box.setText( "Connection dependencies" );
        box.setMessage( "This connection is not used by a job nor a transformation." );
        box.open();
      } else {
        for ( String aJobList : jobList ) {
          if ( aJobList != null ) {
            createTreeItem( parent, aJobList, GUIResource.getInstance().getImageJobGraph() );
          }
        }

        for ( String aTransList : transList ) {
          if ( aTransList != null ) {
            createTreeItem( parent, aTransList, GUIResource.getInstance().getImageTransGraph() );
          }
        }
        parent.setExpanded( true );
      }
    } catch ( Exception e ) {
      new ErrorDialog( shell, "Error", "Error getting dependencies! :", e );
    }
  }

  public void fireMenuControlers() {
    if ( !Display.getDefault().getThread().equals( Thread.currentThread() ) ) {
      display.syncExec( new Runnable() {
        @Override
        public void run() {
          fireMenuControlers();
        }
      } );
      return;
    }
    org.pentaho.ui.xul.dom.Document doc;
    if ( mainSpoonContainer != null ) {
      doc = mainSpoonContainer.getDocumentRoot();
      for ( ISpoonMenuController menuController : menuControllers ) {
        menuController.updateMenu( doc );
      }
    }
  }

  public void hideSplash() {
    if ( splash != null ) {
      splash.hide();
    }
  }

  private void checkEnvironment() {
    if ( EnvironmentUtils.getInstance().isBrowserEnvironmentCheckDisabled() ) {
      webkitUnavailable = null;
      unsupportedBrowserEnvironment = null;
      availableBrowser = "";
      return;
    }
    webkitUnavailable = EnvironmentUtils.getInstance().isWebkitUnavailable();
    unsupportedBrowserEnvironment = EnvironmentUtils.getInstance().isUnsupportedBrowserEnvironment();
    availableBrowser = EnvironmentUtils.getInstance().getBrowserName();
    if ( webkitUnavailable ) {
      ( new BrowserEnvironmentWarningDialog( shell ) ).showWarningDialog(
          BrowserEnvironmentWarningDialog.EnvironmentCase.UBUNTU );
      return;
    }
    if ( unsupportedBrowserEnvironment ) {
      if ( availableBrowser.contains( EnvironmentUtils.WINDOWS_BROWSER ) ) {
        ( new BrowserEnvironmentWarningDialog( shell ) ).showWarningDialog(
            BrowserEnvironmentWarningDialog.EnvironmentCase.WINDOWS );
      } else if ( availableBrowser.contains( EnvironmentUtils.MAC_BROWSER ) ) {
        ( new BrowserEnvironmentWarningDialog( shell ) ).showWarningDialog(
            BrowserEnvironmentWarningDialog.EnvironmentCase.MAC_OS_X );
      }
    }
  }

  /**
   * Hides or shows the main toolbar
   *
   * @param visible
   */
  public void setMainToolbarVisible( boolean visible ) {
    mainToolbar.setVisible( visible );
  }

  public void setMenuBarVisible( boolean visible ) {
    mainSpoonContainer.getDocumentRoot().getElementById( "edit" ).setVisible( visible );
    mainSpoonContainer.getDocumentRoot().getElementById( "file" ).setVisible( visible );
    mainSpoonContainer.getDocumentRoot().getElementById( "view" ).setVisible( visible );
    mainSpoonContainer.getDocumentRoot().getElementById( "action" ).setVisible( visible );
    mainSpoonContainer.getDocumentRoot().getElementById( "tools" ).setVisible( visible );
    mainSpoonContainer.getDocumentRoot().getElementById( "help" ).setVisible( visible );

    MenuManager menuManager = getMenuBarManager();
    menuManager.getMenu().setVisible( visible );
    menuManager.updateAll( true );
  }

  @Override
  protected Control createContents( Composite parent ) {

    shell = getShell();

    init( null );

    openSpoon();

    // listeners
    //
    try {
      lifecycleSupport.onStart( this );
    } catch ( LifecycleException e ) {
      // if severe, we have to quit
      MessageBox box = new MessageBox( shell, ( e.isSevere() ? SWT.ICON_ERROR : SWT.ICON_WARNING ) | SWT.OK );
      box.setMessage( e.getMessage() );
      box.open();
    }

    try {
      start( commandLineOptions );
    } catch ( KettleException e ) {
      MessageBox box = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
      box.setMessage( e.getMessage() );
      box.open();
    }
    getMenuBarManager().updateAll( true );

    return parent;
  }

  public void start() {
    // We store the UI thread for the getDisplay() method
    setBlockOnOpen( false );
    try {
      open();
      checkEnvironment();
      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.SpoonStart.id, commandLineOptions );
      // Load the last loaded files
      lastFileOpened = props.getLastUsedLocalFile();
      loadLastUsedFiles();
      waitForDispose();
      // runEventLoop2(getShell());
    } catch ( Throwable e ) {
      LogChannel.GENERAL.logError( "Error starting Spoon shell", e );
    }
    System.out.println( "stopping" );
  }

  public String getStartupPerspective() {
    return startupPerspective;
  }

  public IMetaStore getMetaStore() {
    return metaStoreSupplier == null ? null : metaStoreSupplier.get();
  }

  public Supplier<IMetaStore> getMetaStoreSupplier() {
    return metaStoreSupplier;
  }

  public void setMetaStoreSupplier( Supplier<IMetaStore> metaStoreSupplier ) {
    this.metaStoreSupplier = metaStoreSupplier;
  }

  public Bowl getBowl() {
    return bowl;
  }

  public void setBowl( Bowl bowl ) {
    this.bowl = Objects.requireNonNull( bowl );
  }

  private void onLoginError( Throwable t ) {
    if ( t instanceof KettleAuthException ) {
      ShowMessageDialog dialog =
        new ShowMessageDialog( loginDialog.getShell(), SWT.OK | SWT.ICON_ERROR, BaseMessages.getString(
          PKG, "Spoon.Dialog.LoginFailed.Title" ), t.getLocalizedMessage() );
      dialog.open();
    } else {
      new ErrorDialog(
        loginDialog.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.LoginFailed.Title" ), BaseMessages
        .getString( PKG, "Spoon.Dialog.LoginFailed.Message", t ), t );
    }
  }

  @Override
  protected void handleShellCloseEvent() {
    try {
      if ( quitFile( true ) ) {
        SpoonPluginManager.getInstance().notifyLifecycleListeners( SpoonLifeCycleEvent.SHUTDOWN );
        super.handleShellCloseEvent();
      }
    } catch ( KettleRepositoryLostException e ) {
      handleRepositoryLost( e );
    } catch ( Exception e ) {
      LogChannel.GENERAL.logError( "Error closing Spoon", e );
    }
  }

  public void showAuthenticationOptions() {
    AuthProviderDialog authProviderDialog = new AuthProviderDialog( shell );
    authProviderDialog.show();
  }

  public void createExpandedContent( String url ) {
    disableCutCopyPaste();
    ExpandedContentManager.createExpandedContent( url );
    ExpandedContentManager.showExpandedContent();
  }

  public void hideExpandedContent() {
    enableCutCopyPaste();
    ExpandedContentManager.hideExpandedContent();
  }

  public void closeExpandedContent() {
    enableCutCopyPaste();
    ExpandedContentManager.closeExpandedContent();
  }

  public void showExpandedContent() {
    disableCutCopyPaste();
    ExpandedContentManager.showExpandedContent();
  }

  public Composite getDesignParent() {
    return sashform;
  }

  public static Boolean isUnsupportedBrowserEnvironment() {
    return unsupportedBrowserEnvironment;
  }

  public static Boolean isWebkitUnavailable() {
    return webkitUnavailable;
  }

  public static String getAvailableBrowser() {
    return availableBrowser;
  }

  public void disableCutCopyPaste() {
    disableMenuItem( "edit-cut-steps" );
    disableMenuItem( "edit-copy-steps" );
    disableMenuItem( "edit-paste-steps" );
  }

  public void enableCutCopyPaste() {
    enableMenuItem( "edit-cut-steps" );
    enableMenuItem( "edit-copy-steps" );
    enableMenuItem( "edit-paste-steps" );
  }
}
