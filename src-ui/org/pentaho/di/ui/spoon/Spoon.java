/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.ui.spoon;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.vfs.FileObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
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
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.reflection.StringSearchResult;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.undo.TransAction;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.job.JobEntryCategory;
import org.pentaho.di.job.JobEntryLoader;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.JobPlugin;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.laf.BasePropertyHandler;
import org.pentaho.di.pan.CommandLineOption;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.pkg.JarfileGenerator;
import org.pentaho.di.repository.PermissionMeta;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.resource.ResourceExportInterface;
import org.pentaho.di.resource.ResourceUtil;
import org.pentaho.di.resource.TopLevelResource;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.HasSlaveServersInterface;
import org.pentaho.di.trans.Partitioner;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.StepPlugin;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.debug.TransDebugMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDialogInterface;
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
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.dialog.ShowBrowserDialog;
import org.pentaho.di.ui.core.dialog.Splash;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.gui.XulHelper;
import org.pentaho.di.ui.core.widget.TreeMemory;
import org.pentaho.di.ui.job.dialog.JobLoadProgressDialog;
import org.pentaho.di.ui.job.dialog.JobSaveProgressDialog;
import org.pentaho.di.ui.partition.dialog.PartitionSchemaDialog;
import org.pentaho.di.ui.repository.dialog.RepositoriesDialog;
import org.pentaho.di.ui.repository.dialog.RepositoryExplorerDialog;
import org.pentaho.di.ui.repository.dialog.SelectObjectDialog;
import org.pentaho.di.ui.repository.dialog.UserDialog;
import org.pentaho.di.ui.repository.dialog.RepositoryExplorerDialog.RepositoryExplorerCallback;
import org.pentaho.di.ui.repository.dialog.RepositoryExplorerDialog.RepositoryObjectReference;
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
import org.pentaho.vfs.ui.VfsFileChooserDialog;
import org.pentaho.xul.menu.XulMenu;
import org.pentaho.xul.menu.XulMenuBar;
import org.pentaho.xul.menu.XulMenuItem;
import org.pentaho.xul.menu.XulPopupMenu;
import org.pentaho.xul.swt.menu.Menu;
import org.pentaho.xul.swt.menu.MenuChoice;
import org.pentaho.xul.swt.menu.PopupMenu;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabListener;
import org.pentaho.xul.swt.tab.TabSet;
import org.pentaho.xul.toolbar.XulToolbar;
import org.pentaho.xul.toolbar.XulToolbarButton;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class handles the main window of the Spoon graphical transformation
 * editor.
 * 
 * @author Matt
 * @since 16-may-2003, i18n at 07-Feb-2006, redesign 01-Dec-2006
 */
public class Spoon implements AddUndoPositionInterface, TabListener, SpoonInterface, OverwritePrompter, PDIObserver, LifeEventHandler {
	public static final String				STRING_TRANSFORMATIONS	= Messages.getString("Spoon.STRING_TRANSFORMATIONS");	// Transformations
	public static final String				STRING_JOBS				= Messages.getString("Spoon.STRING_JOBS");				// Jobs
	public static final String				STRING_BUILDING_BLOCKS	= Messages.getString("Spoon.STRING_BUILDING_BLOCKS");	// Building blocks
	public static final String				STRING_ELEMENTS			= Messages.getString("Spoon.STRING_ELEMENTS");			// Model elements
	public static final String				STRING_CONNECTIONS		= Messages.getString("Spoon.STRING_CONNECTIONS");		// Connections
	public static final String				STRING_STEPS			= Messages.getString("Spoon.STRING_STEPS");				// Steps
	public static final String				STRING_JOB_ENTRIES		= Messages.getString("Spoon.STRING_JOB_ENTRIES");		// Job entries
	public static final String				STRING_HOPS				= Messages.getString("Spoon.STRING_HOPS");				// Hops
	public static final String				STRING_PARTITIONS		= Messages.getString("Spoon.STRING_PARTITIONS");		// Partition schemas
	public static final String				STRING_SLAVES			= Messages.getString("Spoon.STRING_SLAVES");			// Slave servers
	public static final String				STRING_CLUSTERS			= Messages.getString("Spoon.STRING_CLUSTERS");			// Cluster Schemas
	public static final String				STRING_TRANS_BASE		= Messages.getString("Spoon.STRING_BASE");				// Base step types
	public static final String				STRING_JOB_BASE			= Messages.getString("Spoon.STRING_JOBENTRY_BASE");		// Base job entry types
	public static final String				STRING_HISTORY			= Messages.getString("Spoon.STRING_HISTORY");			// Step creation history
	public static final String				STRING_TRANS_NO_NAME	= Messages.getString("Spoon.STRING_TRANS_NO_NAME");		// <unnamed transformation>
	public static final String				STRING_JOB_NO_NAME		= Messages.getString("Spoon.STRING_JOB_NO_NAME");		// <unnamed job>
	public static final String				STRING_TRANSFORMATION	= Messages.getString("Spoon.STRING_TRANSFORMATION");	// Transformation
	public static final String				STRING_JOB				= Messages.getString("Spoon.STRING_JOB");				// Job

	private static final String				SYNC_TRANS				= "sync_trans_name_to_file_name";
	
	public static final String				APP_NAME				= Messages.getString("Spoon.Application.Name");			// "Spoon";

	private static Spoon					staticSpoon;

	private LogWriter						log;
	private Display							display;
	private Shell							shell;
	private boolean							destroy;

	private SashForm						sashform;
	public TabSet							tabfolder;

	public boolean							shift;
	public boolean							control;

	// THE HANDLERS
	public SpoonDelegates					delegates				= new SpoonDelegates(this);

	public RowMetaAndData					variables				= new RowMetaAndData(new RowMeta(), new Object[] {});

	/**
	 * These are the arguments that were given at Spoon launch time...
	 */
	private String[]						arguments;

	private boolean							stopped;

	private Cursor							cursor_hourglass, cursor_hand;

	public PropsUI							props;

	public Repository						rep;

	/**
	 * This contains a map with all the unnamed transformation (just a filename)
	 */

	private XulMenuBar						menuBar;

	private ToolItem						view, design, expandAll, collapseAll;

	private Label							selectionLabel;
	private Text                            selectionFilter;

	private org.eclipse.swt.widgets.Menu	fileMenus;

	private static final String				APPL_TITLE				= APP_NAME;

	private static final String				STRING_WELCOME_TAB_NAME	= Messages.getString("Spoon.Title.STRING_WELCOME");

	private static final String				FILE_WELCOME_PAGE		= Const.safeAppendDirectory(BasePropertyHandler.getProperty("documentationDirBase", "docs/"), Messages.getString("Spoon.Title.STRING_DOCUMENT_WELCOME"));	// "docs/English/welcome/kettle_document_map.html";

	private static final String				UNDO_MENUITEM			= "edit-undo";				//$NON-NLS-1$
	private static final String				REDO_MENUITEM			= "edit-redo";				//$NON-NLS-1$
	private static final String				UNDO_UNAVAILABLE		= Messages.getString("Spoon.Menu.Undo.NotAvailable");	//"Undo : not available \tCTRL-Z" //$NON-NLS-1$
	private static final String				REDO_UNAVAILABLE		= Messages.getString("Spoon.Menu.Redo.NotAvailable");	//"Redo : not available \tCTRL-Y" //$NON-NLS-1$S

	public KeyAdapter						defKeys;

	private Composite						tabComp;

	private Tree							selectionTree;
	private Tree							coreObjectsTree;

	private TransExecutionConfiguration		transExecutionConfiguration;
	private TransExecutionConfiguration		transPreviewExecutionConfiguration;
	private TransExecutionConfiguration		transDebugExecutionConfiguration;

	private JobExecutionConfiguration		jobExecutionConfiguration;

	private Menu							spoonMenu;																																											// Connections,

	private int								coreObjectsState		= STATE_CORE_OBJECTS_NONE;

	protected Map<String, FileListener>		fileExtensionMap		= new HashMap<String, FileListener>();
	protected Map<String, FileListener>		fileNodeMap				= new HashMap<String, FileListener>();

	private List<Object[]>					menuListeners			= new ArrayList<Object[]>();

	private Map<String, Menu>				menuMap					= new HashMap<String, Menu>();

	// loads the lifecycle listeners
	private LifecycleSupport				lcsup					= new LifecycleSupport();
	private Composite						mainComposite;

	private boolean							viewSelected;
	private boolean							designSelected;

	private Composite						variableComposite;

	private Map<String, String>				coreStepToolTipMap;
	private Map<String, String>				coreJobToolTipMap;

	private DefaultToolTip					toolTip;


	public Map<String, SharedObjects>		sharedObjectsFileMap;
	
	/** We can use this to set a default filter path in the open and save dialogs */
	public String                           lastDirOpened;

	/**
	 * This is the main procedure for Spoon.
	 * 
	 * @param a
	 *            Arguments are available in the "Get System Info" step.
	 */
	public static void main(String[] a) throws KettleException {
		try {
			// Do some initialization of environment variables
			EnvUtil.environmentInit();

			List<String> args = new ArrayList<String>(java.util.Arrays.asList(a));

			Display display = new Display();

			Splash splash = new Splash(display);

			CommandLineOption[] commandLineOptions = getCommandLineArgs(args);

			initLogging(commandLineOptions);
			initPlugins();

			PropsUI.init(display, Props.TYPE_PROPERTIES_SPOON); // things to //
			// remember...

			staticSpoon = new Spoon(display);
			staticSpoon.init(null);
			SpoonFactory.setSpoonInstance(staticSpoon);
			staticSpoon.setDestroy(true);
			GUIFactory.setThreadDialogs(new ThreadGuiResources());

			if (LogWriter.getInstance().isBasic()) {
				LogWriter.getInstance().logBasic(APP_NAME, Messages.getString("Spoon.Log.MainWindowCreated"));// Main window is created.
			}

			// listeners
			//
			try {
				staticSpoon.lcsup.onStart(staticSpoon);
			} catch (LifecycleException e) {
				// if severe, we have to quit
				MessageBox box = new MessageBox(staticSpoon.shell, (e.isSevere() ? SWT.ICON_ERROR : SWT.ICON_WARNING) | SWT.OK);
				box.setMessage(e.getMessage());
				box.open();
			}

			staticSpoon.setArguments(args.toArray(new String[args.size()]));
			staticSpoon.start(splash, commandLineOptions);
		} catch (Throwable t) {
			// avoid calls to Messages i18n method getString() in this block
			// We do this to (hopefully) also catch Out of Memory Exceptions
			//
			LogWriter.getInstance().logError(APP_NAME, "Fatal error : " + Const.NVL(t.toString(), Const.NVL(t.getMessage(), "Unknown error"))); //$NON-NLS-1$ //$NON-NLS-2$
			LogWriter.getInstance().logError(APP_NAME, Const.getStackTracker(t));
			// inform the user with a dialog when possible
			new ErrorDialog(staticSpoon.shell, Messages.getString("Spoon.Dialog.FatalError"), "Fatal error : " + Const.NVL(t.toString(), Const.NVL(t.getMessage(), "Unknown error")), t); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		// Kill all remaining things in this VM!
		System.exit(0);
	}

	private static void initPlugins() throws KettleException {
		/* Load the plugins etc. */
		try {
			StepLoader.init();
		} catch (KettleException e) {
			throw new KettleException(Messages.getString("Spoon.Log.ErrorLoadingAndHaltSystem"), e);
		}

		/* Load the plugins etc. we need to load jobentry */
		try {
			JobEntryLoader.init();
		} catch (KettleException e) {
			throw new KettleException("Error loading job entries & plugins... halting Spoon!", e);
		}
	}

	private static void initLogging(CommandLineOption[] options) throws KettleException {
		StringBuffer optionLogfile = getCommandLineOption(options, "logfile").getArgument();
		StringBuffer optionLoglevel = getCommandLineOption(options, "level").getArgument();

		// Set default Locale:
		Locale.setDefault(Const.DEFAULT_LOCALE);

		LogWriter.setConsoleAppenderDebug();
		LogWriter log;
		if (Const.isEmpty(optionLogfile)) {
			log = LogWriter.getInstance(Const.SPOON_LOG_FILE, false, LogWriter.LOG_LEVEL_BASIC);
		} else {
			log = LogWriter.getInstance(optionLogfile.toString(), true, LogWriter.LOG_LEVEL_BASIC);
		}

		if (log.getRealFilename() != null) {
			if (log.isBasic())
				log.logBasic(APP_NAME, Messages.getString("Spoon.Log.LoggingToFile") + log.getRealFilename());// "Logging goes to "
		}

		if (!Const.isEmpty(optionLoglevel)) {
			log.setLogLevel(optionLoglevel.toString());
			if (log.isBasic())
				log.logBasic(APP_NAME, Messages.getString("Spoon.Log.LoggingAtLevel") + log.getLogLevelDesc());// "Logging is at level : "
		}
	}

	public Spoon(Display d) {
		this(d, null);
	}

	public Spoon(Repository rep) {
		this(null, rep);
	}

	public Spoon(Display d, Repository rep) {
		this.log = LogWriter.getInstance();
		this.rep = rep;

		if (d != null) {
			display = d;
			destroy = false;
		} else {
			display = new Display();
			destroy = true;
		}

		props = PropsUI.getInstance();

		shell = new Shell(display);
		shell.setText(APPL_TITLE);
		staticSpoon = this;

		JndiUtil.initJNDI();

		SpoonFactory.setSpoonInstance(this);
	}

	public void init(TransMeta ti) {
		FormLayout layout = new FormLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		shell.setLayout(layout);

		addFileListener(new TransFileListener(), Const.STRING_TRANS_DEFAULT_EXT, TransMeta.XML_TAG);

		addFileListener(new JobFileListener(), Const.STRING_JOB_DEFAULT_EXT, JobMeta.XML_TAG);

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

		sharedObjectsFileMap = new Hashtable<String, SharedObjects>();

		// props.setLook(shell);

		shell.setImage(GUIResource.getInstance().getImageSpoon());

		cursor_hourglass = new Cursor(display, SWT.CURSOR_WAIT);
		cursor_hand = new Cursor(display, SWT.CURSOR_HAND);

		// widgets = new WidgetContainer();

		defKeys = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {

				boolean ctrl = ((e.stateMask & SWT.CONTROL) != 0);
				boolean alt = ((e.stateMask & SWT.ALT) != 0);

				String key = null;

				switch (e.keyCode) {
				case SWT.ESC:
					key = "esc";break; //$NON-NLS-1$
				case SWT.F1:
					key = "f1";break; //$NON-NLS-1$
				case SWT.F2:
					key = "f2";break; //$NON-NLS-1$
				case SWT.F3:
					key = "f3";break; //$NON-NLS-1$
				case SWT.F4:
					key = "f4";break; //$NON-NLS-1$
				case SWT.F5:
					key = "f5";break; //$NON-NLS-1$
				case SWT.F6:
					key = "f6";break; //$NON-NLS-1$
				case SWT.F7:
					key = "f7";break; //$NON-NLS-1$
				case SWT.F8:
					key = "f8";break; //$NON-NLS-1$
				case SWT.F9:
					key = "f9";break; //$NON-NLS-1$
				case SWT.F10:
					key = "f10";break; //$NON-NLS-1$
				case SWT.F11:
					key = "f12";break; //$NON-NLS-1$
				case SWT.F12:
					key = "f12";break; //$NON-NLS-1$
				case SWT.ARROW_UP:
					key = "up";break; //$NON-NLS-1$
				case SWT.ARROW_DOWN:
					key = "down";break; //$NON-NLS-1$
				case SWT.ARROW_LEFT:
					key = "left";break; //$NON-NLS-1$
				case SWT.ARROW_RIGHT:
					key = "right";break; //$NON-NLS-1$
				case SWT.HOME:
					key = "home";break; //$NON-NLS-1$
				case SWT.PAGE_UP:
					key = "pageup";break; //$NON-NLS-1$
				case SWT.PAGE_DOWN:
					key = "pagedown";break; //$NON-NLS-1$
				default:
					;
				}
				if (key == null && ctrl) {
					// get the character
					if (e.character >= '0' && e.character <= '9') {
						char c = e.character;
						key = new String(new char[] { c });
					} else {
						char c = (char) ('a' + (e.character - 1));
						key = new String(new char[] { c });
					}
				} else if (key == null) {
					char c = e.character;
					key = new String(new char[] { c });
				}

				menuBar.handleAccessKey(key, alt, ctrl);
			}
		};

		// addBar();

		initFileMenu();

		sashform = new SashForm(shell, SWT.HORIZONTAL);

		FormData fdSash = new FormData();
		fdSash.left = new FormAttachment(0, 0);
		// fdSash.top = new FormAttachment((org.eclipse.swt.widgets.ToolBar)
		// toolbar.getNativeObject(), 0);
		fdSash.top = new FormAttachment(0, 0);
		fdSash.bottom = new FormAttachment(100, 0);
		fdSash.right = new FormAttachment(100, 0);
		sashform.setLayoutData(fdSash);

		// Set the shell size, based upon previous time...
		WindowProperty winprop = props.getScreen(APPL_TITLE);
		if (winprop != null)
			winprop.setShell(shell);
		else {
			shell.pack();
			shell.setMaximized(true); // Default = maximized!
		}

		addMenu();
		addTree();
		// addCoreObjectsExpandBar();
		addTabs();

		// sashform.layout(true, true);
		// sashform.redraw();
		// cButtons.layout(true, true);
		// cButtons.redraw();

		// In case someone dares to press the [X] in the corner ;-)
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				e.doit = quitFile();
			}
		});

		shell.addKeyListener(defKeys);

		// Add a browser widget
		if (props.showWelcomePageOnStartup()) {
			showWelcomePage();
		}
	}

	private void initFileMenu() {
		fileMenus = new org.eclipse.swt.widgets.Menu(shell, SWT.NONE);

		// Add the new file toolbar items dropdowns
		//
		MenuItem miNewTrans = new MenuItem(fileMenus, SWT.CASCADE);
		miNewTrans.setText(Messages.getString("Spoon.Menu.File.NewTrans")); //$NON-NLS-1$
		miNewTrans.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				newTransFile();
			}
		});
		miNewTrans.setImage(GUIResource.getInstance().getImageTransGraph());

		MenuItem miNewJob = new MenuItem(fileMenus, SWT.CASCADE);
		miNewJob.setText(Messages.getString("Spoon.Menu.File.NewJob")); //$NON-NLS-1$
		miNewJob.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				newJobFile();
			}
		});
		miNewJob.setImage(GUIResource.getInstance().getImageJobGraph());

		new MenuItem(fileMenus, SWT.SEPARATOR);

		MenuItem miNewDB = new MenuItem(fileMenus, SWT.CASCADE);
		miNewDB.setText(Messages.getString("Spoon.Menu.File.NewDB")); //$NON-NLS-1$
		miNewDB.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				newConnection();
			}
		});
		miNewDB.setImage(GUIResource.getInstance().getImageConnection());

		MenuItem miNewSlave = new MenuItem(fileMenus, SWT.CASCADE);
		miNewSlave.setText(Messages.getString("Spoon.Menu.File.NewSlave")); //$NON-NLS-1$
		miNewSlave.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				newSlaveServer();
			}
		});
		miNewSlave.setImage(GUIResource.getInstance().getImageSlave());
	}

	public Shell getShell() {
		return shell;
	}

	public static Spoon getInstance() {
		return staticSpoon;
	}

	public XulMenuBar getMenuBar() {
		return menuBar;
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

	public void closeSpoonBrowser() {
		delegates.tabs.removeTab(STRING_WELCOME_TAB_NAME, TabMapEntry.OBJECT_TYPE_BROWSER);
		TabItem browserTab = delegates.tabs.findTabItem(STRING_WELCOME_TAB_NAME, TabMapEntry.OBJECT_TYPE_BROWSER);
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

			List<StringSearchResult> stringList = transMeta.getStringList(esd.isSearchingSteps(), esd.isSearchingDatabases(), esd.isSearchingNotes());
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

			List<StringSearchResult> stringList = jobMeta.getStringList(esd.isSearchingSteps(), esd.isSearchingDatabases(), esd.isSearchingNotes());
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
			PreviewRowsDialog prd = new PreviewRowsDialog(shell, Variables.getADefaultVariableSpace(), SWT.NONE, Messages.getString("Spoon.StringSearchResult.Subtitle"), StringSearchResult.getResultRowMeta(), rows);
			String title = Messages.getString("Spoon.StringSearchResult.Title");
			String message = Messages.getString("Spoon.StringSearchResult.Message");
			prd.setTitleMessage(title, message);
			prd.open();
		} else {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
			mb.setMessage(Messages.getString("Spoon.Dialog.NothingFound.Message")); // Nothing found that matches your criteria
			mb.setText(Messages.getString("Spoon.Dialog.NothingFound.Title")); // Sorry!
			mb.open();
		}
	}

	public void showArguments() {

		RowMetaAndData allArgs = new RowMetaAndData();

		for (int ii = 0; ii < arguments.length; ++ii) {
			allArgs.addValue(new ValueMeta(Props.STRING_ARGUMENT_NAME_PREFIX + (1 + ii), ValueMetaInterface.TYPE_STRING), arguments[ii]);
		}

		// Now ask the use for more info on these!
		EnterStringsDialog esd = new EnterStringsDialog(shell, SWT.NONE, allArgs);
		esd.setTitle(Messages.getString("Spoon.Dialog.ShowArguments.Title"));
		esd.setMessage(Messages.getString("Spoon.Dialog.ShowArguments.Message"));
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
		esd.setTitle(Messages.getString("Spoon.Dialog.SetVariables.Title"));
		esd.setMessage(Messages.getString("Spoon.Dialog.SetVariables.Message"));
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
			} catch (KettleValueException e) {
				// Just eat the exception. getString() should never give an
				// exception.
			}
		}
	}

	public void showVariables() {
		fillVariables(variables);

		// Now ask the use for more info on these!
		EnterStringsDialog esd = new EnterStringsDialog(shell, SWT.NONE, variables);
		esd.setTitle(Messages.getString("Spoon.Dialog.ShowVariables.Title"));
		esd.setMessage(Messages.getString("Spoon.Dialog.ShowVariables.Message"));
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

		if (destroy && !display.isDisposed())
			display.dispose();
	}

	public boolean isDisposed() {
		return display.isDisposed();
	}

	public void sleep() {
		display.sleep();
	}

	public void addMenuListeners() {
		try {
			// first get the XML document
			URL url = XulHelper.getAndValidate(XUL_FILE_MENU_PROPERTIES);
			Properties props = new Properties();
			props.load(url.openStream());
			String ids[] = menuBar.getMenuItemIds();
			for (int i = 0; i < ids.length; i++) {
				String methodName = (String) props.get(ids[i]);
				if (methodName != null) {
					menuBar.addMenuListener(ids[i], this, methodName);
					// toolbar.addMenuListener(ids[i], this, methodName);

				}
			}
			for (String id : menuMap.keySet()) {
				PopupMenu menu = (PopupMenu) menuMap.get(id);
				ids = menu.getMenuItemIds();
				for (int i = 0; i < ids.length; i++) {
					String methodName = (String) props.get(ids[i]);
					if (methodName != null) {
						menu.addMenuListener(ids[i], this, methodName);
					}
				}
				for (int i = 0; i < menuListeners.size(); i++) {
					Object info[] = menuListeners.get(i);
					menu.addMenuListener((String) info[0], info[1], (String) info[2]);
				}
			}

			// now apply any overrides
			for (int i = 0; i < menuListeners.size(); i++) {
				Object info[] = menuListeners.get(i);
				menuBar.addMenuListener((String) info[0], info[1], (String) info[2]); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (Throwable t) {
			t.printStackTrace();
			new ErrorDialog(shell, Messages.getString("Spoon.Exception.ErrorReadingXULFile.Title"), Messages.getString("Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_MENU_PROPERTIES), new Exception(t));
		}
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

	public void addMenu() {

		if (menuBar != null && !menuBar.isDisposed()) {
			menuBar.dispose();
		}

		try {
			menuBar = XulHelper.createMenuBar(XUL_FILE_MENUBAR, shell, new XulMessages());
			List<String> ids = new ArrayList<String>();
			ids.add("trans-class");
			ids.add("trans-class-new");
			ids.add("job-class");
			ids.add("trans-hop-class");
			ids.add("database-class");
			ids.add("partition-schema-class");
			ids.add("cluster-schema-class");
			ids.add("slave-cluster-class");
			ids.add("trans-inst");
			ids.add("job-inst");
			ids.add("step-plugin");
			ids.add("database-inst");
			ids.add("step-inst");
			ids.add("job-entry-copy-inst");
			ids.add("trans-hop-inst");
			ids.add("partition-schema-inst");
			ids.add("cluster-schema-inst");
			ids.add("slave-server-inst");

			this.menuMap = XulHelper.createPopupMenus(XUL_FILE_MENUS, shell, new XulMessages(), ids);// createMenuBarFromXul();
		} catch (Throwable t) {
			t.printStackTrace();
			new ErrorDialog(shell, Messages.getString("Spoon.Exception.ErrorReadingXULFile.Title"), Messages.getString("Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_MENUS), new Exception(t));
		}

		addMenuListeners();
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

	public void copyTransformation() {
		copyTransformation(getActiveTransformation());
	}

	public void copyTransformationImage() {
		copyTransformationImage(getActiveTransformation());
	}

	public void editTransformationProperties() {
		TransGraph.editProperties(getActiveTransformation(), this, rep, true);
	}

	public void executeJob() {
		executeJob(getActiveJob(), true, false, null, false);
	}

	public void copyJob() {
		copyJob(getActiveJob());
	}

	public void showTips() {
		new TipsDialog(shell).open();
	}

	public void showWelcomePage() {
		try {
			// see if we are in webstart mode
			String webstartRoot = System.getProperty("spoon.webstartroot");
			if (webstartRoot != null) {
				URL url = new URL(webstartRoot + '/' + FILE_WELCOME_PAGE);
				addSpoonBrowser(STRING_WELCOME_TAB_NAME, url.toString()); // ./docs/English/tips/index.htm
			} else {
				// see if we can find the welcome file on the file system
				File file = new File(FILE_WELCOME_PAGE);
				if (file.exists()) {
					addSpoonBrowser(STRING_WELCOME_TAB_NAME, file.toURI().toURL().toString()); // ./docs/English/tips/index.htm
				}
			}
		} catch (MalformedURLException e1) {
			log.logError(toString(), Const.getStackTracker(e1));
		}
	}

	public void addMenuLast() {

		XulMenuItem sep = menuBar.getSeparatorById("file-last-separator"); //$NON-NLS-1$
		XulMenu msFile = null;
		if (sep != null) {
			msFile = sep.getMenu(); //$NON-NLS-1$
		}
		if (msFile == null || sep == null) {
			// The menu system has been altered and we can't display the last
			// used files
			return;
		}
		int idx = msFile.indexOf(sep);
		int max = msFile.getItemCount();
		// Remove everything until end...
		for (int i = max - 1; i > idx; i--) {
			XulMenuItem mi = msFile.getItem(i);
			msFile.remove(mi);
			mi.dispose();
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

			MenuChoice miFileLast = new MenuChoice(msFile, text, id, accessText, accessKey, MenuChoice.TYPE_PLAIN, null);

			if (lastUsedFile.isTransformation()) {
				miFileLast.setImage(GUIResource.getInstance().getImageTransGraph());
			} else if (lastUsedFile.isJob()) {
				miFileLast.setImage(GUIResource.getInstance().getImageJobGraph());
			}

			menuBar.addMenuListener(id, this, "lastFileSelect"); //$NON-NLS-1$
		}

	}

	public void lastFileSelect(String id) {

		int idx = Integer.parseInt(id.substring("last-file-".length()));
		List<LastUsedFile> lastUsedFiles = props.getLastUsedFiles();
		final LastUsedFile lastUsedFile = (LastUsedFile) lastUsedFiles.get(idx);

		// If the file comes from a repository and it's not the same as
		// the one we're connected to, ask for a username/password!
		//
		boolean cancelled = false;
		if (lastUsedFile.isSourceRepository() && (rep == null || !rep.getRepositoryInfo().getName().equalsIgnoreCase(lastUsedFile.getRepositoryName()))) {
			// Ask for a username password to get the required repository access
			//
			int perms[] = new int[] { PermissionMeta.TYPE_PERMISSION_TRANSFORMATION };
			RepositoriesDialog rd = new RepositoriesDialog(display, perms, Messages.getString("Spoon.Application.Name")); // RepositoriesDialog.ToolName="Spoon"
			rd.setRepositoryName(lastUsedFile.getRepositoryName());
			if (rd.open()) {
				// Close the previous connection...
				if (rep != null)
					rep.disconnect();
				rep = new Repository(log, rd.getRepository(), rd.getUser());
				try {
					rep.connect(APP_NAME);
				} catch (KettleException ke) {
					rep = null;
					new ErrorDialog(shell, Messages.getString("Spoon.Dialog.UnableConnectRepository.Title"), Messages.getString("Spoon.Dialog.UnableConnectRepository.Message"), ke); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else {
				cancelled = true;
			}
		}

		if (!cancelled) {
			try {
				RepositoryMeta meta = (rep == null ? null : rep.getRepositoryInfo());
				loadLastUsedFile(lastUsedFile, meta);
				addMenuLast();
				refreshHistory();
			} catch (KettleException ke) {
				// "Error loading transformation", "I was unable to load this
				// transformation from the
				// XML file because of an error"
				new ErrorDialog(shell, Messages.getString("Spoon.Dialog.LoadTransformationError.Title"), Messages.getString("Spoon.Dialog.LoadTransformationError.Message"), ke);
			}
		}

	}

	private static final String	STRING_SPOON_MAIN_TREE			= Messages.getString("Spoon.MainTree.Label");
	private static final String	STRING_SPOON_CORE_OBJECTS_TREE	= Messages.getString("Spoon.CoreObjectsTree.Label");

	public static final String	XML_TAG_TRANSFORMATION_STEPS	= "transformation-steps";
	public static final String	XML_TAG_JOB_JOB_ENTRIES			= "job-jobentries";

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

		ToolBar tb = new ToolBar(mainComposite, SWT.HORIZONTAL | SWT.FLAT);
		tb.setBackground(GUIResource.getInstance().getColorCreamPentaho());
		view = new ToolItem(tb, SWT.CHECK);
		view.setImage(GUIResource.getInstance().getImageViewPanel());
		view.setText(STRING_SPOON_MAIN_TREE);
		design = new ToolItem(tb, SWT.CHECK);
		design.setImage(GUIResource.getInstance().getImageDesignPanel());
		design.setText(STRING_SPOON_CORE_OBJECTS_TREE);
		design.setEnabled(false);

		FormData fdTreeButton = new FormData();
		fdTreeButton.left = new FormAttachment(0, 0);
		fdTreeButton.top = new FormAttachment(sep0, 0);
		fdTreeButton.right = new FormAttachment(100, 0);
		tb.setLayoutData(fdTreeButton);
		lastControl = tb;

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
		selectionFilter.setToolTipText(Messages.getString("Spoon.SelectionFilter.Tooltip"));
		FormData fdSelectionFilter = new FormData();
		fdSelectionFilter.top = new FormAttachment(lastControl, -(GUIResource.getInstance().getImageExpandAll().getBounds().height+5));
		fdSelectionFilter.right = new FormAttachment(95, -55);
		selectionFilter.setLayoutData(fdSelectionFilter);

		selectionFilter.addModifyListener(new ModifyListener() { public void modifyText(ModifyEvent arg0) {
			if (coreObjectsTree!=null && !coreObjectsTree.isDisposed()) {
				previousShowTrans=false;
				previousShowJob=false;
				refreshCoreObjects(); 
				tidyBranches(coreObjectsTree.getItems(), true); // expand all
			}
			if (selectionTree!=null && !selectionTree.isDisposed()) {
				refreshTree();
				tidyBranches(selectionTree.getItems(), true); // expand all
				selectionFilter.setFocus();
			}
		} });  
			
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

		view.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				setViewMode();
			}
		});

		design.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				setDesignMode();
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

	}

	public boolean setViewMode() {
		if (viewSelected)
			return true;
		disposeVariableComposite(true, false, false, false);
		refreshTree();
		return false;
	}

	public boolean setDesignMode() {
		if (designSelected)
			return true;
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
		view.setSelection(viewSelected);
		designSelected = core;
		design.setSelection(designSelected);

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

		selectionLabel.setText(tree ? Messages.getString("Spoon.Explorer") : Messages.getString("Spoon.Steps"));
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
					 * Trick for WSWT on Windows systems: a SelectionEvent is
					 * called after the TreeEvent if setSelection() is not used
					 * here. Otherwise the first item in the list is selected as
					 * default and collapsed again but wrong, see PDI-1480
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
				toolTip.hide();
				TreeItem item = searchMouseOverTreeItem(coreObjectsTree.getItems(), move.x, move.y);
				if (item != null) {
					String name = item.getText();
					String tip = coreStepToolTipMap.get(name);
					if (tip != null) {
						StepPlugin plugin = StepLoader.getInstance().findStepPluginWithDescription(name);
						Image image = GUIResource.getInstance().getImagesSteps().get(plugin.getID()[0]);
						toolTip.setImage(image);
						toolTip.setText(name + Const.CR + Const.CR + tip);
						toolTip.show(new org.eclipse.swt.graphics.Point(move.x + 10, move.y + 10));
					}
					tip = coreJobToolTipMap.get(name);
					if (tip != null) {
						JobPlugin plugin = JobEntryLoader.getInstance().findJobEntriesWithDescription(name);
						if (plugin != null) {
							Image image = GUIResource.getInstance().getImagesJobentries().get(plugin.getID());
							toolTip.setImage(image);
							toolTip.setText(name + Const.CR + Const.CR + tip);
							toolTip.show(new org.eclipse.swt.graphics.Point(move.x + 10, move.y + 10));
						}
					}
				}
			}
		});

		addDragSourceToTree(coreObjectsTree);

		toolTip = new DefaultToolTip(variableComposite, ToolTip.RECREATE, true);
		toolTip.setRespectMonitorBounds(true);
		toolTip.setRespectDisplayBounds(true);
		toolTip.setPopupDelay(350);
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

	private boolean	previousShowTrans;
	private boolean	previousShowJob;
	public boolean	showTrans;
	public boolean	showJob;

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

			final String locale = LanguageChoice.getInstance().getDefaultLocale().toString().toLowerCase();

			StepLoader steploader = StepLoader.getInstance();
			StepPlugin basesteps[] = steploader.getStepsWithType(StepPlugin.TYPE_ALL);

			final String basecat[] = steploader.getCategories(StepPlugin.TYPE_ALL, locale);

			// Sort these base steps by category and then by step name in the given locale
			//
			Arrays.sort(basesteps, new Comparator<StepPlugin>() {

				public int compare(StepPlugin one, StepPlugin two) {
					int idxOne = Const.indexOfString(one.getCategory(locale), basecat);
					int idxTwo = Const.indexOfString(two.getCategory(locale), basecat);
					if (idxOne == idxTwo) {
						String nameOne = one.getDescription(locale);
						String nameTwo = two.getDescription(locale);
						return nameOne.compareTo(nameTwo);
					} else {
						return idxOne - idxTwo;
					}
				}
			});

			for (int i = 0; i < basecat.length; i++) {
				TreeItem item = new TreeItem(coreObjectsTree, SWT.NONE);
				item.setText(basecat[i]);
				item.setImage(GUIResource.getInstance().getImageArrow());

				for (int j = 0; j < basesteps.length; j++) {
					if (basesteps[j].getCategory(locale).equalsIgnoreCase(basecat[i])) {
						final Image stepimg = (Image) GUIResource.getInstance().getImagesStepsSmall().get(basesteps[j].getID()[0]);
						String pluginName = basesteps[j].getDescription(locale);
						String pluginDescription = basesteps[j].getTooltip(locale);
						boolean isPlugin = basesteps[j].isPlugin();
						
						if (!filterMatch(pluginName) && !filterMatch(pluginDescription)) continue;

						TreeItem stepItem = new TreeItem(item, SWT.NONE);
						stepItem.setImage(stepimg);
						stepItem.setText(pluginName);
						stepItem.addListener(SWT.Selection, new Listener() {

							public void handleEvent(Event arg0) {
								System.out.println("Tree item Listener fired");
							}
						});
						if (isPlugin)
							stepItem.setFont(GUIResource.getInstance().getFontBold());

						coreStepToolTipMap.put(pluginName, pluginDescription);
						// addExpandBarItemLine(composite, stepimg, pluginName,
						// pluginDescription, isPlugin, basesteps[j]);
					}
				}
			}

			// Add History Items...
			TreeItem item = new TreeItem(coreObjectsTree, SWT.NONE);
			item.setText(Messages.getString("Spoon.History"));
			item.setImage(GUIResource.getInstance().getImageArrow());

			List<ObjectUsageCount> pluginHistory = props.getPluginHistory();

			for (int i = 0; i < pluginHistory.size() && i < 10; i++) // top 10 maximum, the rest is not interesting anyway --  for GUI performance reasons
			{
				ObjectUsageCount usage = pluginHistory.get(i);
				StepPlugin stepPlugin = StepLoader.getInstance().findStepPluginWithID(usage.getObjectName());
				if (stepPlugin != null) {
					final Image stepimg = GUIResource.getInstance().getImagesSteps().get(stepPlugin.getID()[0]);
					String pluginName = Const.NVL(stepPlugin.getDescription(locale), "");
					String pluginDescription = Const.NVL(stepPlugin.getTooltip(locale), "");
					boolean isPlugin = stepPlugin.isPlugin();
					
					if (!filterMatch(pluginName) && !filterMatch(pluginDescription)) continue;

					TreeItem stepItem = new TreeItem(item, SWT.NONE);
					stepItem.setImage(stepimg);
					stepItem.setText(pluginName);
					stepItem.addListener(SWT.Selection, new Listener() {

						public void handleEvent(Event arg0) {
							System.out.println("Tree item Listener fired");
						}
					});
					if (isPlugin)
						stepItem.setFont(GUIResource.getInstance().getFontBold());

					coreStepToolTipMap.put(stepPlugin.getDescription(locale), pluginDescription + " (" + usage.getNrUses() + ")");

				}
			}
		}

		if (showJob) {
			// Fill the base components...
			//
			// ////////////////////////////////////////////////////////////////////////////////////////////////
			// JOBS
			// ////////////////////////////////////////////////////////////////////////////////////////////////

			final String locale = LanguageChoice.getInstance().getDefaultLocale().toString().toLowerCase();

			JobEntryLoader jobEntryLoader = JobEntryLoader.getInstance();
			JobPlugin baseJobEntries[] = jobEntryLoader.getJobEntriesWithType(JobPlugin.TYPE_ALL);

			final String baseCategories[] = jobEntryLoader.getCategories(JobPlugin.TYPE_ALL, locale);

			// Sort these base steps by category and then by step name in the given locale
			//
			Arrays.sort(baseJobEntries, new Comparator<JobPlugin>() {

				public int compare(JobPlugin one, JobPlugin two) {
					int idxOne = Const.indexOfString(one.getCategory(locale), baseCategories);
					int idxTwo = Const.indexOfString(two.getCategory(locale), baseCategories);
					if (idxOne == idxTwo) {
						String nameOne = one.getDescription(locale);
						String nameTwo = two.getDescription(locale);
						return nameOne.compareTo(nameTwo);
					} else {
						return idxOne - idxTwo;
					}
				}
			});

			TreeItem generalItem = null;

			for (int i = 0; i < baseCategories.length; i++) {
				TreeItem item = new TreeItem(coreObjectsTree, SWT.NONE);
				item.setText(baseCategories[i]);
				item.setImage(GUIResource.getInstance().getImageArrow());

				if (baseCategories[i].equalsIgnoreCase(JobEntryCategory.GENERAL.getName())) {
					generalItem = item;
				}

				for (int j = 0; j < baseJobEntries.length; j++) {
					if (!baseJobEntries[j].getID().equals("SPECIAL")) {

						if (baseJobEntries[j].getCategory(locale).equalsIgnoreCase(baseCategories[i])) {
							final Image jobEntryImage = (Image) GUIResource.getInstance().getImagesJobentriesSmall().get(baseJobEntries[j].getID());
							String pluginName = Const.NVL(baseJobEntries[j].getDescription(locale), "");
							String pluginDescription = Const.NVL(baseJobEntries[j].getTooltip(locale), "");
							boolean isPlugin = baseJobEntries[j].isPlugin();

							if (!filterMatch(pluginName) && !filterMatch(pluginDescription)) continue;

							TreeItem stepItem = new TreeItem(item, SWT.NONE);
							stepItem.setImage(jobEntryImage);
							stepItem.setText(pluginName);
							stepItem.addListener(SWT.Selection, new Listener() {

								public void handleEvent(Event arg0) {
									System.out.println("Tree item Listener fired");
								}
							});
							if (isPlugin)
								stepItem.setFont(GUIResource.getInstance().getFontBold());

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
			Image specialImage[] = new Image[] { GUIResource.getInstance().getImageStartSmall(), GUIResource.getInstance().getImageDummySmall() };

			for (int i = 0; i < specialText.length; i++) {
				TreeItem specialItem = new TreeItem(generalItem, SWT.NONE, i);
				specialItem.setImage(specialImage[i]);
				specialItem.setText(specialText[i]);
				specialItem.addListener(SWT.Selection, new Listener() {

					public void handleEvent(Event arg0) {
						System.out.println("Tree item Listener fired");
					}

				});

				coreJobToolTipMap.put(specialText[i], specialTooltip[i]);
			}
		}

		variableComposite.layout(true, true);

		previousShowTrans = showTrans;
		previousShowJob = showJob;
	}

	protected void shareObject(SharedObjectInterface sharedObjectInterface) {
		sharedObjectInterface.setShared(true);
		refreshTree();
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
	 * If you click in the tree, you might want to show the corresponding
	 * window.
	 */
	public void showSelection() {
		TreeSelection[] objects = getTreeObjects(selectionTree);
		if (objects.length != 1)
			return; // not yet supported, we can do this later when the OSX bug goes away

		TreeSelection object = objects[0];

		final Object selection = object.getSelection();
		final Object parent = object.getParent();

		TransMeta transMeta = null;
		if (selection instanceof TransMeta)
			transMeta = (TransMeta) selection;
		if (parent instanceof TransMeta)
			transMeta = (TransMeta) parent;
		if (transMeta != null) {
			// Search the corresponding TransGraph tab
			TabItem tabItem = delegates.tabs.findTabItem(makeTransGraphTabName(transMeta), TabMapEntry.OBJECT_TYPE_TRANSFORMATION_GRAPH);
			if (tabItem != null) {
				int current = tabfolder.getSelectedIndex();
				int desired = tabfolder.indexOf(tabItem);
				if (current != desired)
					tabfolder.setSelected(desired);
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
			// Search the corresponding TransGraph tab
			TabItem tabItem = delegates.tabs.findTabItem(delegates.tabs.makeJobGraphTabName(jobMeta), TabMapEntry.OBJECT_TYPE_JOB_GRAPH);
			if (tabItem != null) {
				int current = tabfolder.getSelectedIndex();
				int desired = tabfolder.indexOf(tabItem);
				if (current != desired)
					tabfolder.setSelected(desired);
				jobMeta.setInternalKettleVariables();
				if (getCoreObjectsState() != STATE_CORE_OBJECTS_CHEF) {
					// Switch the core objects in the lower left corner to the spoon job types
					//
					refreshCoreObjects();
				}
			}
		}
	}

	private Object	selectionObjectParent	= null;
	private Object	selectionObject			= null;

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

	public Map<String, Menu> getMenuMap() {
		return menuMap;
	}

	public void editJobProperties(String id) {
		if ("job-settings".equals(id)) {
			JobGraph.editProperties(getActiveJob(), this, rep, true);
		} else if ("job-inst-settings".equals(id)) {
			JobGraph.editProperties((JobMeta) selectionObject, this, rep, true);
		}
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
		final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
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
		// Show a minimal window to allow you to quickly select the database connection to explore
		//
		List<DatabaseMeta> databases = new ArrayList<DatabaseMeta>();

		HasDatabasesInterface databasesInterface = getActiveHasDatabasesInterface();
		if (databasesInterface != null) {
			databases.addAll(databasesInterface.getDatabases());
		}

		if (rep != null) {
			try {
				databases.addAll(rep.readDatabases());
			} catch (KettleException e) {
				log.logError(toString(), "Unexpected repository error", e);
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
		EnterSelectionDialog dialog = new EnterSelectionDialog(shell, databaseNames, Messages.getString("Spoon.ExploreDB.SelectDB.Title"), Messages.getString("Spoon.ExploreDB.SelectDB.Message"));
		String name = dialog.open();
		if (name != null) {
			selectionObject = DatabaseMeta.findDatabase(databases, name);
			exploreDB();
		}
	}

	public void exploreDB() {
		final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
		delegates.db.exploreDB(databaseMeta);
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
			shareObject(databaseMeta);
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
		// final Object grandParent = object.getGrandParent();

		// Not clicked on a real object: returns a class
		if (selection instanceof Class<?>) {
			if (selection.equals(TransMeta.class)) {
				// New
				spoonMenu = (Menu) menuMap.get("trans-class");
			} else if (selection.equals(JobMeta.class)) {
				// New
				spoonMenu = (Menu) menuMap.get("job-class");
			} else if (selection.equals(TransHopMeta.class)) {
				// New
				spoonMenu = (Menu) menuMap.get("trans-hop-class");
			} else if (selection.equals(DatabaseMeta.class)) {
				spoonMenu = (Menu) menuMap.get("database-class");
			} else if (selection.equals(PartitionSchema.class)) {
				// New
				spoonMenu = (Menu) menuMap.get("partition-schema-class");
			} else if (selection.equals(ClusterSchema.class)) {
				spoonMenu = (Menu) menuMap.get("cluster-schema-class");
			} else if (selection.equals(SlaveServer.class)) {
				spoonMenu = (Menu) menuMap.get("slave-cluster-class");
			} else
				spoonMenu = null;
		} else {

			if (selection instanceof TransMeta) {
				spoonMenu = (Menu) menuMap.get("trans-inst");
			} else if (selection instanceof JobMeta) {
				spoonMenu = (Menu) menuMap.get("job-inst");
			} else if (selection instanceof StepPlugin) {
				spoonMenu = (Menu) menuMap.get("step-plugin");
			}

			else if (selection instanceof DatabaseMeta) {
				spoonMenu = (PopupMenu) menuMap.get("database-inst");
				// disable for now if the connection is an SAP R/3 type of database...
				//
				XulMenuItem item = ((XulPopupMenu) spoonMenu).getMenuItemById("database-inst-explore");
				if (item != null) {
					final DatabaseMeta databaseMeta = (DatabaseMeta) selection;
					if (databaseMeta.getDatabaseType() == DatabaseMeta.TYPE_DATABASE_SAPR3)
						item.setEnabled(false);
				}
				item = ((XulPopupMenu) spoonMenu).getMenuItemById("database-inst-clear-cache");
				if (item != null) {
					final DatabaseMeta databaseMeta = (DatabaseMeta) selectionObject;
					item.setText(Messages.getString("Spoon.Menu.Popup.CONNECTIONS.ClearDBCache") + databaseMeta.getName());// Clear
																															// DB
																															// Cache
					// of
				}

			} else if (selection instanceof StepMeta) {
				spoonMenu = (Menu) menuMap.get("step-inst");
			} else if (selection instanceof JobEntryCopy) {
				spoonMenu = (Menu) menuMap.get("job-entry-copy-inst");
			} else if (selection instanceof TransHopMeta) {
				spoonMenu = (Menu) menuMap.get("trans-hop-inst");
			} else if (selection instanceof PartitionSchema) {
				spoonMenu = (Menu) menuMap.get("partition-schema-inst");
			} else if (selection instanceof ClusterSchema) {
				spoonMenu = (Menu) menuMap.get("cluster-schema-inst");
			} else if (selection instanceof SlaveServer) {
				spoonMenu = (Menu) menuMap.get("slave-server-inst");
			}

		}
		if (spoonMenu != null) {
			ConstUI.displayMenu(spoonMenu.getSwtMenu(), tree);
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
			return; // not yet supported, we can do this later when the OSX bug goes away
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
			if (selection instanceof StepPlugin)
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

		tabfolder.addKeyListener(defKeys);

		sashform.addKeyListener(defKeys);

		int weights[] = props.getSashWeights();
		sashform.setWeights(weights);
		sashform.setVisible(true);

		// Set a minimum width on the sash so that the view and design buttons on the left panel are always visible.
		//
		Control[] comps = sashform.getChildren();
		for (int i = 0; i < comps.length; i++) {

			if (comps[i] instanceof Sash) {
				int limit = 10;
				for (ToolItem item : view.getParent().getItems()) {
					limit += item.getWidth();
				}

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

		tabfolder.addListener(this); // methods: tabDeselected, tabClose, tabSelected

	}

	public void tabDeselected(TabItem item) {

	}

	public boolean tabClose(TabItem item) {
		return delegates.tabs.tabClose(item);
	}

	public TabSet getTabSet() {
		return tabfolder;
	}

	public void tabSelected(TabItem item) {
		delegates.tabs.tabSelected(item);
	}

	public String getRepositoryName() {
		if (rep == null)
			return null;
		return rep.getRepositoryInfo().getName();
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
				log.logDebug(toString(), Messages.getString("Spoon.Log.FoundSteps", "" + nr) + loc);// "I found "+nr+" steps to paste on location: "
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
				log.logDebug(toString(), Messages.getString("Spoon.Log.FoundHops", "" + nr));// "I found "+nr+" hops to paste."
			TransHopMeta hops[] = new TransHopMeta[nr];

			ArrayList<StepMeta> alSteps = new ArrayList<StepMeta>();
			for (int i = 0; i < steps.length; i++)
				alSteps.add(steps[i]);

			for (int i = 0; i < nr; i++) {
				Node hopnode = XMLHandler.getSubNodeByNr(hopsnode, "hop", i);
				hops[i] = new TransHopMeta(hopnode, alSteps);
			}

			// What's the difference between loc and min?
			// This is the offset:
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
				log.logDebug(toString(), Messages.getString("Spoon.Log.FoundNotepads", "" + nr));// "I found "+nr+" notepads to paste."
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
			new ErrorDialog(shell, Messages.getString("Spoon.Dialog.UnablePasteSteps.Title"), Messages.getString("Spoon.Dialog.UnablePasteSteps.Message"), e);// "Error pasting steps...",
			// "I was unable to paste steps to this transformation"
		}
	}

	public void copySelected(TransMeta transMeta, StepMeta stepMeta[], NotePadMeta notePadMeta[]) {
		if (stepMeta == null || stepMeta.length == 0)
			return;

		String xml = XMLHandler.getXMLHeader();
		try {
			xml += XMLHandler.openTag(Spoon.XML_TAG_TRANSFORMATION_STEPS) + Const.CR;
			xml += " <steps>" + Const.CR;

			for (int i = 0; i < stepMeta.length; i++) {
				xml += stepMeta[i].getXML();
			}

			xml += "    </steps>" + Const.CR;

			// 
			// Also check for the hops in between the selected steps...
			//

			xml += "<order>" + Const.CR;
			if (stepMeta != null)
				for (int i = 0; i < stepMeta.length; i++) {
					for (int j = 0; j < stepMeta.length; j++) {
						if (i != j) {
							TransHopMeta hop = transMeta.findTransHop(stepMeta[i], stepMeta[j], true);
							if (hop != null) // Ok, we found one...
							{
								xml += hop.getXML() + Const.CR;
							}
						}
					}
				}
			xml += "  </order>" + Const.CR;

			xml += "  <notepads>" + Const.CR;
			if (notePadMeta != null)
				for (int i = 0; i < notePadMeta.length; i++) {
					xml += notePadMeta[i].getXML();
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
			addUndoChange(transMeta, new TransHopMeta[] { before }, new TransHopMeta[] { after }, new int[] { transMeta.indexOfTransHop(transHopMeta) });

			String newname = transHopMeta.toString();
			if (!name.equalsIgnoreCase(newname)) {
				refreshTree();
				refreshGraph(); // color, nr of copies...
			}
		}
		setShellText();
	}

	public void delHop(TransMeta transMeta, TransHopMeta transHopMeta) {
		int i = transMeta.indexOfTransHop(transHopMeta);
		addUndoDelete(transMeta, new Object[] { (TransHopMeta) transHopMeta.clone() }, new int[] { transMeta.indexOfTransHop(transHopMeta) });
		transMeta.removeTransHop(i);
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
				addUndoNew(transMeta, new TransHopMeta[] { transHopMeta }, new int[] { transMeta.indexOfTransHop(transHopMeta) });
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
			mb.setMessage(Messages.getString("Spoon.Dialog.HopExists.Message"));// "This hop already exists!"
			mb.setText(Messages.getString("Spoon.Dialog.HopExists.Title"));// Error!
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
			mb.setMessage(Messages.getString("TransGraph.Dialog.HopCausesLoop.Message")); //$NON-NLS-1$
			mb.setText(Messages.getString("TransGraph.Dialog.HopCausesLoop.Title")); //$NON-NLS-1$
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
				new ErrorDialog(shell, Messages.getString("TransGraph.Dialog.HopCausesRowMixing.Title"), Messages.getString("TransGraph.Dialog.HopCausesRowMixing.Message"), re);
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
			boolean distributes = false;

			if (props.showCopyOrDistributeWarning()) {
				MessageDialogWithToggle md = new MessageDialogWithToggle(shell, Messages.getString("System.Warning"),// "Warning!"
						null, Messages.getString("Spoon.Dialog.CopyOrDistribute.Message", fr.getName(), Integer.toString(nrNextSteps)), MessageDialog.WARNING, new String[] { Messages.getString("Spoon.Dialog.CopyOrDistribute.Copy"),
								Messages.getString("Spoon.Dialog.CopyOrDistribute.Distribute") },// "Copy
						// Distribute
						0, Messages.getString("Spoon.Message.Warning.NotShowWarning"),// "Please, don't show this warning anymore."
						!props.showCopyOrDistributeWarning());
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
		int perms[] = new int[] { PermissionMeta.TYPE_PERMISSION_TRANSFORMATION };
		RepositoriesDialog rd = new RepositoriesDialog(display, perms, APP_NAME);
		rd.getShell().setImage(GUIResource.getInstance().getImageSpoon());
		if (rd.open()) {
			// Close previous repository...

			if (rep != null) {
				rep.disconnect();
			}

			rep = new Repository(log, rd.getRepository(), rd.getUser());
			try {
				rep.connect(APP_NAME);
			} catch (KettleException ke) {
				rep = null;
				new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorConnectingRepository.Title"), Messages.getString("Spoon.Dialog.ErrorConnectingRepository.Message", Const.CR), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}

			TransMeta transMetas[] = getLoadedTransformations();
			for (int t = 0; t < transMetas.length; t++) {
				TransMeta transMeta = transMetas[t];

				for (int i = 0; i < transMeta.nrDatabases(); i++) {
					transMeta.getDatabase(i).setID(-1L);
				}

				// Set for the existing transformation the ID at -1!
				transMeta.setID(-1L);

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
					SharedObjects sharedObjects = transMeta.readSharedObjects(rep);
					sharedObjectsFileMap.put(sharedObjects.getFilename(), sharedObjects);
				} catch (KettleException e) {
					new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorReadingSharedObjects.Title"), Messages.getString("Spoon.Dialog.ErrorReadingSharedObjects.Message", makeTransGraphTabName(transMeta)), e);
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

				// For the existing transformation, change the directory too:
				// Try to find the same directory in the new repository...
				RepositoryDirectory redi = rep.getDirectoryTree().findDirectory(transMeta.getDirectory().getPath());
				if (redi != null) {
					transMeta.setDirectory(redi);
				} else {
					transMeta.setDirectory(rep.getDirectoryTree()); // the root
					// is the
					// default!
				}
			}

			refreshTree();
			setShellText();
		} else {
			// Not cancelled? --> Clear repository...
			if (!rd.isCancelled()) {
				closeRepository();
			}
		}
	}

	public void exploreRepository() {
		if (rep != null) {
			RepositoryExplorerCallback cb = new RepositoryExplorerDialog.RepositoryExplorerCallback() {

				public boolean open(RepositoryObjectReference objectToOpen) {
					String objname = objectToOpen.getName();
					if (objname != null) {
						String object_type = objectToOpen.getType();
						RepositoryDirectory repdir = objectToOpen.getDirectory();

						// Try to open the selected transformation.
						if (object_type.equals(RepositoryExplorerDialog.STRING_TRANSFORMATIONS)) {
							try {
								TransLoadProgressDialog progressDialog = new TransLoadProgressDialog(shell, rep, objname, repdir);
								TransMeta transMeta = progressDialog.open();
								transMeta.clearChanged();
								transMeta.setFilename(objname);
								addTransGraph(transMeta);
								refreshTree();
								refreshGraph();
							} catch (Exception e) {
								MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
								mb.setMessage(Messages.getString("Spoon.Dialog.ErrorOpening.Message") + objname + Const.CR + e.getMessage());// "Error opening : "
								mb.setText(Messages.getString("Spoon.Dialog.ErrorOpening.Title"));
								mb.open();
							}
						} else
						// Try to open the selected job.
						if (object_type.equals(RepositoryExplorerDialog.STRING_JOBS)) {
							try {
								JobLoadProgressDialog progressDialog = new JobLoadProgressDialog(shell, rep, objname, repdir);
								JobMeta jobMeta = progressDialog.open();
								jobMeta.clearChanged();
								jobMeta.setFilename(objname);
								jobMeta.setArguments(arguments);
								delegates.jobs.addJobGraph(jobMeta);
								refreshTree();
								refreshGraph();
							} catch (Exception e) {
								MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
								mb.setMessage(Messages.getString("Spoon.Dialog.ErrorOpening.Message") + objname + Const.CR + e.getMessage());// "Error opening : "
								mb.setText(Messages.getString("Spoon.Dialog.ErrorOpening.Title"));
								mb.open();
							}
						}
					}
					return false; // do not close explorer
				}
			};

			RepositoryExplorerDialog erd = new RepositoryExplorerDialog(shell, SWT.NONE, rep, rep.getUserInfo(), cb, Variables.getADefaultVariableSpace());
			erd.open();

		}
	}

	public void editRepositoryUser() {
		if (rep != null) {
			UserInfo userinfo = rep.getUserInfo();
			UserDialog ud = new UserDialog(shell, SWT.NONE, rep, userinfo);
			UserInfo ui = ud.open();
			if (!userinfo.isReadonly()) {
				if (ui != null) {
					try {
						ui.saveRep(rep);
					} catch (KettleException e) {
						MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
						mb.setMessage(Messages.getString("Spoon.Dialog.UnableChangeUser.Message") + Const.CR + e.getMessage());// Sorry,
																																// I
						// was
						// unable
						// to
						// change
						// this
						// user
						// in
						// the
						// repository:
						mb.setText(Messages.getString("Spoon.Dialog.UnableChangeUser.Title"));// "Edit user"
						mb.open();
					}
				}
			} else {
				MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
				mb.setMessage(Messages.getString("Spoon.Dialog.NotAllowedChangeUser.Message"));// "Sorry, you are not allowed to change this user."
				mb.setText(Messages.getString("Spoon.Dialog.NotAllowedChangeUser.Title"));
				mb.open();
			}
		}
	}

	public void closeRepository() {
		if (rep != null)
			rep.disconnect();
		rep = null;
		setShellText();
	}

	public void openFile() {
		openFile(false);
	}

	public void importFile() {
		openFile(true);
	}

	public void openFile(boolean importfile) {
		if (rep == null || importfile) // Load from XML
		{
			FileDialog dialog = new FileDialog(shell, SWT.OPEN);
			dialog.setFilterExtensions(Const.STRING_TRANS_AND_JOB_FILTER_EXT);
			dialog.setFilterNames(Const.getTransformationAndJobFilterNames());
			setFilterPath(dialog);
			String fname = dialog.open();
			if (fname != null) {
				lastDirOpened = dialog.getFilterPath();
				openFile(fname, importfile);
			}
		} else {
			SelectObjectDialog sod = new SelectObjectDialog(shell, rep);
			if (sod.open() != null) {
				String type = sod.getObjectType();
				String name = sod.getObjectName();
				RepositoryDirectory repdir = sod.getDirectory();

				// Load a transformation
				if (RepositoryObject.STRING_OBJECT_TYPE_TRANSFORMATION.equals(type)) {
					TransLoadProgressDialog tlpd = new TransLoadProgressDialog(shell, rep, name, repdir);
					TransMeta transMeta = tlpd.open();
					sharedObjectsFileMap.put(transMeta.getSharedObjects().getFilename(), transMeta.getSharedObjects());
					setTransMetaVariables(transMeta);
					
					if (transMeta != null) {
						if (log.isDetailed())
							log.logDetailed(toString(), Messages.getString("Spoon.Log.LoadToTransformation", name, repdir.getDirectoryName()));// "Transformation ["+transname+"] in directory ["+repdir+"] loaded from the repository."
						props.addLastFile(LastUsedFile.FILE_TYPE_TRANSFORMATION, name, repdir.getPath(), true, rep.getName());
						addMenuLast();
						transMeta.clearChanged();
						transMeta.setFilename(name);
						addTransGraph(transMeta);
					}
					refreshGraph();
					refreshTree();
					refreshHistory();
				} else
				// Load a job
				if (RepositoryObject.STRING_OBJECT_TYPE_JOB.equals(type)) {
					JobLoadProgressDialog jlpd = new JobLoadProgressDialog(shell, rep, name, repdir);
					JobMeta jobMeta = jlpd.open();
					sharedObjectsFileMap.put(jobMeta.getSharedObjects().getFilename(), jobMeta.getSharedObjects());
					setJobMetaVariables(jobMeta);
					if (jobMeta != null) {
						props.addLastFile(LastUsedFile.FILE_TYPE_JOB, name, repdir.getPath(), true, rep.getName());
						saveSettings();
						addMenuLast();
						delegates.jobs.addJobGraph(jobMeta);
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

	private String	lastFileOpened	= null;

	public String getLastFileOpened() {
		if (lastFileOpened == null) {
			lastFileOpened = System.getProperty("org.pentaho.di.defaultVFSPath", "");
		}
		return lastFileOpened;
	}

	public void setLastFileOpened(String inLastFileOpened) {
		lastFileOpened = inLastFileOpened;
	}

	// private String lastVfsUsername="";
	// private String lastVfsPassword="";

	public void openFileVFSFile() {
		FileObject initialFile = null;
		FileObject rootFile = null;
		try {
			initialFile = KettleVFS.getFileObject(getLastFileOpened());
			rootFile = initialFile.getFileSystem().getRoot();
		} catch (IOException e) {
			e.printStackTrace();
			String message = e.getMessage();
			if (e.getCause() != null) {
				message = e.getCause().getMessage();
			}
			MessageBox messageDialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			messageDialog.setText(Messages.getString("Spoon.Error"));
			messageDialog.setMessage(message);
			messageDialog.open();

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

		VfsFileChooserDialog vfsFileChooser = new VfsFileChooserDialog(rootFile, initialFile);
		FileObject selectedFile = vfsFileChooser.open(shell, null, Const.STRING_TRANS_AND_JOB_FILTER_EXT, Const.getTransformationAndJobFilterNames(), VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE);
		if (selectedFile != null) {
			setLastFileOpened(selectedFile.getName().getFriendlyURI());
			openFile(selectedFile.getName().getFriendlyURI(), false);
		}
	}

	public void addFileListener(FileListener listener, String extension, String rootNodeName) {
		fileExtensionMap.put(extension, listener);
		fileNodeMap.put(rootNodeName, listener);
	}

	public void openFile(String fname, boolean importfile) {
		// Open the XML and see what's in there.
		// We expect a single <transformation> or <job> root at this time...
		try {

			boolean loaded = false;
			FileListener listener = null;
			// match by extension first
			int idx = fname.lastIndexOf('.');
			if (idx != -1) {
				String extension = fname.substring(idx + 1);
				listener = fileExtensionMap.get(extension);
			}
			// otherwise try by looking at the root node
			Document document = XMLHandler.loadXMLFile(fname);
			Node root = document.getDocumentElement();
			if (listener == null) {
				listener = fileNodeMap.get(root.getNodeName());
			}

			// You got to have a file name!
			//
			if (!Const.isEmpty(fname)) {
				
				if (listener != null) {
					loaded = listener.open(root, fname, importfile);
				}
				if (!loaded) {
					// Give error back
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
					mb.setMessage(Messages.getString("Spoon.UnknownFileType.Message", fname));
					mb.setText(Messages.getString("Spoon.UnknownFileType.Title"));
					mb.open();
				} else {
					applyVariables(); // set variables in the newly loaded
					// transformation(s) and job(s).
				}
			}
		} catch (KettleException e) {
			new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorOpening.Title"), Messages.getString("Spoon.Dialog.ErrorOpening.Message") + fname, e);
		}
	}

	public PropsUI getProperties() {
		return props;
	}

	/*
	 * public void newFileDropDown() { newFileDropDown(toolbar); }
	 */

	public void newFileDropDown(XulToolbar usedToolBar) {
		if (usedToolBar == null) {
			System.out.println("Blocked new file drop down attempt");
			return; // call it a day
		}

		// Drop down a list below the "New" icon (new.png)
		// First problem: where is that icon?
		//
		XulToolbarButton button = usedToolBar.getButtonById("file-new");
		Object object = button.getNativeObject();
		if (object instanceof ToolItem) {
			// OK, let's determine the location of this widget...
			//
			ToolItem item = (ToolItem) object;
			Rectangle bounds = item.getBounds();
			org.eclipse.swt.graphics.Point p = item.getParent().toDisplay(new org.eclipse.swt.graphics.Point(bounds.x, bounds.y));

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
			SharedObjects sharedObjects = transMeta.readSharedObjects(rep);
			sharedObjectsFileMap.put(sharedObjects.getFilename(), sharedObjects);

			transMeta.clearChanged();
		} catch (Exception e) {
			new ErrorDialog(shell, Messages.getString("Spoon.Exception.ErrorReadingSharedObjects.Title"), Messages.getString("Spoon.Exception.ErrorReadingSharedObjects.Message"), e);
		}
		int nr = 1;
		transMeta.setName(STRING_TRANSFORMATION + " " + nr);

		// See if a transformation with the same name isn't already loaded...
		while (findTransformation(delegates.tabs.makeTransGraphTabName(transMeta)) != null) {
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
	}

	public void newJobFile() {
		try {
			JobMeta jobMeta = new JobMeta(log);
			jobMeta.addObserver(this);

			// Set the variables that were previously defined in this session on
			// the transformation metadata too.
			//
			setJobMetaVariables(jobMeta);

			try {
				SharedObjects sharedObjects = jobMeta.readSharedObjects(rep);
				sharedObjectsFileMap.put(sharedObjects.getFilename(), sharedObjects);
			} catch (KettleException e) {
				new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorReadingSharedObjects.Title"), Messages.getString("Spoon.Dialog.ErrorReadingSharedObjects.Message", delegates.tabs.makeJobGraphTabName(jobMeta)), e);
			}

			int nr = 1;
			jobMeta.setName(STRING_JOB + " " + nr);

			// See if a transformation with the same name isn't already
			// loaded...
			while (findJob(delegates.tabs.makeJobGraphTabName(jobMeta)) != null) {
				nr++;
				jobMeta.setName(STRING_JOB + " " + nr); // rename
			}

			delegates.jobs.addJobGraph(jobMeta);
			applyVariables();

			// switch to design mode...
			//
			if (setDesignMode()) {
				// No refresh done yet, do so
				refreshTree();
			}
		} catch (Exception e) {
			new ErrorDialog(shell, Messages.getString("Spoon.Exception.ErrorCreatingNewJob.Title"), Messages.getString("Spoon.Exception.ErrorCreatingNewJob.Message"), e);
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
				SharedObjects sharedObjects = transMeta.readSharedObjects(rep);
				sharedObjectsFileMap.put(sharedObjects.getFilename(), sharedObjects);
			} catch (Exception e) {
				new ErrorDialog(shell, Messages.getString("Spoon.Error.UnableToLoadSharedObjects.Title"), Messages.getString("Spoon.Error.UnableToLoadSharedObjects.Message"), e);
			}

		}
	}

	public boolean quitFile() {
		if (log.isDetailed())
			log.logDetailed(toString(), Messages.getString("Spoon.Log.QuitApplication"));// "Quit application."

		boolean exit = true;

		saveSettings();

		if (props.showExitWarning()) {
			MessageDialogWithToggle md = new MessageDialogWithToggle(shell, Messages.getString("System.Warning"),// "Warning!"
					null, Messages.getString("Spoon.Message.Warning.PromptExit"), // Are
					// you
					// sure
					// you
					// want
					// to
					// exit?
					MessageDialog.WARNING, new String[] { Messages.getString("Spoon.Message.Warning.Yes"), Messages.getString("Spoon.Message.Warning.No") },// "Yes",
					// "No"
					1, Messages.getString("Spoon.Message.Warning.NotShowWarning"),// "Please, don't show this warning anymore."
					!props.showExitWarning());
			MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
			int idx = md.open();
			props.setExitWarningShown(!md.getToggleState());
			props.saveProps();
			if ((idx & 0xFF) == 1)
				return false; // No selected: don't exit!
		}

		// Check all tabs to see if we can close them...
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
							delegates.tabs.removeTab(mapEntry.getTabItem());
						}
					}
					// A running transformation?
					//
					if (mapEntry.getObject() instanceof TransGraph) {
						TransGraph transGraph = (TransGraph) mapEntry.getObject();
						if (transGraph.isRunning()) {
							transGraph.stop();
							delegates.tabs.removeTab(mapEntry.getTabItem());
						}
					}
				}
			}
		}

		// and now we call the listeners

		try {
			lcsup.onExit(this);
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
		TransMeta transMeta = getActiveTransformation();
		if (transMeta != null)
			return saveToFile(transMeta);

		JobMeta jobMeta = getActiveJob();
		if (jobMeta != null)
			return saveToFile(jobMeta);

		return false;
	}

	public boolean saveToFile(EngineMetaInterface meta) {
		if (meta == null)
			return false;

		boolean saved = false;

		if (log.isDetailed())
			log.logDetailed(toString(), Messages.getString("Spoon.Log.SaveToFileOrRepository"));// "Save to file or repository..."

		if (rep != null) {
			saved = saveToRepository(meta);
		} else {
			if (meta.getFilename() != null) {
				saved = save(meta, meta.getFilename(), false);
			} else {
				saved = saveFileAs(meta);
			}
		}

		if (saved) // all was OK
		{
			saved = meta.saveSharedObjects();
		}

		try {
			if (props.useDBCache() && meta instanceof TransMeta)
				((TransMeta) meta).getDbCache().saveCache(log);
		} catch (KettleException e) {
			new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorSavingDatabaseCache.Title"), Messages.getString("Spoon.Dialog.ErrorSavingDatabaseCache.Message"), e);// "An error occured saving the database cache to disk"
		}

		delegates.tabs.renameTabs(); // filename or name of transformation might
		// have changed.
		refreshTree();

		return saved;
	}

	public boolean saveToRepository(EngineMetaInterface meta) {
		return saveToRepository(meta, false);
	}

	public boolean saveToRepository(EngineMetaInterface meta, boolean ask_name) {
		if (log.isDetailed())
			log.logDetailed(toString(), Messages.getString("Spoon.Log.SaveToRepository"));// "Save to repository..."
		if (rep != null) {
			boolean answer = true;
			boolean ask = ask_name;
			while (answer && (ask || Const.isEmpty(meta.getName()))) {
				if (!ask) {
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					mb.setMessage(Messages.getString("Spoon.Dialog.PromptTransformationName.Message"));// "Please give this transformation a name before saving it in the database."
					mb.setText(Messages.getString("Spoon.Dialog.PromptTransformationName.Title"));// "Transformation has no name."
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
				if (!rep.getUserInfo().isReadonly()) {
					int response = SWT.YES;
					if (meta.showReplaceWarning(rep)) {
						MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
						mb.setMessage(Messages.getString("Spoon.Dialog.PromptOverwriteTransformation.Message", meta.getName(), Const.CR));// "There already is a transformation called ["+transMeta.getName()+"] in the repository."+Const.CR+"Do you want to overwrite the transformation?"
						mb.setText(Messages.getString("Spoon.Dialog.PromptOverwriteTransformation.Title"));// "Overwrite?"
						response = mb.open();
					}

					boolean saved = false;
					if (response == SWT.YES) {
						shell.setCursor(cursor_hourglass);

						// Keep info on who & when this transformation was
						// created...
						if (meta.getCreatedUser() == null || meta.getCreatedUser().equals("-")) {
							meta.setCreatedDate(new Date());
							meta.setCreatedUser(rep.getUserInfo().getLogin());
						} else {

							meta.setCreatedDate(meta.getCreatedDate());
							meta.setCreatedUser(meta.getCreatedUser());
						}

						// Keep info on who & when this transformation was
						// changed...
						meta.setModifiedDate(new Date());
						meta.setModifiedUser(rep.getUserInfo().getLogin());

						SaveProgressDialog tspd = new SaveProgressDialog(shell, rep, meta);
						if (tspd.open()) {
							saved = true;
							if (!props.getSaveConfirmation()) {
								MessageDialogWithToggle md = new MessageDialogWithToggle(shell, Messages.getString("Spoon.Message.Warning.SaveOK"), // "Save OK!"
										null, Messages.getString("Spoon.Message.Warning.TransformationWasStored"),// "This transformation was stored in repository"
										MessageDialog.QUESTION, new String[] { Messages.getString("Spoon.Message.Warning.OK") },// "OK!"
										0, Messages.getString("Spoon.Message.Warning.NotShowThisMessage"),// "Don't show this message again."
										props.getSaveConfirmation());
								MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
								md.open();
								props.setSaveConfirmation(md.getToggleState());
							}

							// Handle last opened files...
							props.addLastFile(meta.getFileType(), meta.getName(), meta.getDirectory().getPath(), true, getRepositoryName());
							saveSettings();
							addMenuLast();

							setShellText();
						}
						shell.setCursor(null);
					}
					return saved;
				} else {
					MessageBox mb = new MessageBox(shell, SWT.CLOSE | SWT.ICON_ERROR);
					mb.setMessage(Messages.getString("Spoon.Dialog.OnlyreadRepository.Message"));// "Sorry, the user you're logged on with, can only read from the repository"
					mb.setText(Messages.getString("Spoon.Dialog.OnlyreadRepository.Title"));// "Transformation not saved!"
					mb.open();
				}
			}
		} else {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage(Messages.getString("Spoon.Dialog.NoRepositoryConnection.Message"));// "There is no repository connection available."
			mb.setText(Messages.getString("Spoon.Dialog.NoRepositoryConnection.Title"));// "No repository available."
			mb.open();
		}
		return false;
	}

	public boolean saveJobRepository(JobMeta jobMeta) {
		return saveToRepository(jobMeta, false);
	}

	public boolean saveJobRepository(JobMeta jobMeta, boolean ask_name) {
		if (log.isDetailed())
			log.logDetailed(toString(), "Save to repository..."); //$NON-NLS-1$
		if (rep != null) {
			boolean answer = true;
			boolean ask = ask_name;
			while (answer && (ask || jobMeta.getName() == null || jobMeta.getName().length() == 0)) {
				if (!ask) {
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					mb.setMessage(Messages.getString("Spoon.Dialog.GiveJobANameBeforeSaving.Message")); //$NON-NLS-1$
					mb.setText(Messages.getString("Spoon.Dialog.GiveJobANameBeforeSaving.Title")); //$NON-NLS-1$
					mb.open();
				}
				ask = false;
				answer = JobGraph.editProperties(jobMeta, this, rep, false);
			}

			if (answer && jobMeta.getName() != null && jobMeta.getName().length() > 0) {
				if (!rep.getUserInfo().isReadonly()) {
					boolean saved = false;
					int response = SWT.YES;
					if (jobMeta.showReplaceWarning(rep)) {
						MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
						mb.setMessage("'" + jobMeta.getName() + "'" + Const.CR + Const.CR + Messages.getString("Spoon.Dialog.FileExistsOverWrite.Message")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						mb.setText(Messages.getString("Spoon.Dialog.FileExistsOverWrite.Title")); //$NON-NLS-1$
						response = mb.open();
					}

					if (response == SWT.YES) {

						// Keep info on who & when this job was created...
						if (jobMeta.getCreatedUser() == null || jobMeta.getCreatedUser().equals("-")) {
							jobMeta.setCreatedDate(new Date());
							jobMeta.setCreatedUser(rep.getUserInfo().getLogin());
						} else {

							jobMeta.setCreatedDate(jobMeta.getCreatedDate());
							jobMeta.setCreatedUser(jobMeta.getCreatedUser());
						}

						// Keep info on who & when this job was changed...
						jobMeta.setModifiedDate(new Date());
						jobMeta.setModifiedUser(rep.getUserInfo().getLogin());

						JobSaveProgressDialog jspd = new JobSaveProgressDialog(shell, rep, jobMeta);
						if (jspd.open()) {
							if (!props.getSaveConfirmation()) {
								MessageDialogWithToggle md = new MessageDialogWithToggle(shell, Messages.getString("Spoon.Dialog.JobWasStoredInTheRepository.Title"), //$NON-NLS-1$
										null, Messages.getString("Spoon.Dialog.JobWasStoredInTheRepository.Message"), //$NON-NLS-1$
										MessageDialog.QUESTION, new String[] { Messages.getString("System.Button.OK") }, //$NON-NLS-1$
										0, Messages.getString("Spoon.Dialog.JobWasStoredInTheRepository.Toggle"), //$NON-NLS-1$
										props.getSaveConfirmation());
								MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
								md.open();
								props.setSaveConfirmation(md.getToggleState());
							}

							// Handle last opened files...
							props.addLastFile(LastUsedFile.FILE_TYPE_JOB, jobMeta.getName(), jobMeta.getDirectory().getPath(), true, rep.getName());
							saveSettings();
							addMenuLast();

							setShellText();

							saved = true;
						}
					}
					return saved;
				} else {
					MessageBox mb = new MessageBox(shell, SWT.CLOSE | SWT.ICON_ERROR);
					mb.setMessage(Messages.getString("Spoon.Dialog.UserCanOnlyReadFromTheRepositoryJobNotSaved.Message")); //$NON-NLS-1$
					mb.setText(Messages.getString("Spoon.Dialog.UserCanOnlyReadFromTheRepositoryJobNotSaved.Title")); //$NON-NLS-1$
					mb.open();
				}
			}
		} else {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage(Messages.getString("Spoon.Dialog.NoRepositoryConnectionAvailable.Message")); //$NON-NLS-1$
			mb.setText(Messages.getString("Spoon.Dialog.NoRepositoryConnectionAvailable.Title")); //$NON-NLS-1$
			mb.open();
		}
		return false;
	}

	public boolean saveFileAs() {
		TransMeta transMeta = getActiveTransformation();
		if (transMeta != null)
			return saveFileAs(transMeta);

		JobMeta jobMeta = getActiveJob();
		if (jobMeta != null)
			return saveFileAs(jobMeta);

		return false;
	}

	public boolean saveFileAs(EngineMetaInterface meta) {
		boolean saved = false;

		if (log.isBasic())
			log.logBasic(toString(), Messages.getString("Spoon.Log.SaveAs"));// "Save as..."

		if (rep != null) {
			meta.setID(-1L);
			saved = saveToRepository(meta, true);

		} else {
			saved = saveXMLFile(meta, false);

		}

		delegates.tabs.renameTabs(); // filename or name of transformation might
		// have changed.
		refreshTree();

		return saved;
	}

	public boolean exportXMLFile() {
		return saveXMLFile(true);
	}
	
	/**
	 * Export this job or transformation including all depending resources to a single zip file.
	 */
	public void exportAllXMLFile() {

		ResourceExportInterface resourceExportInterface = getActiveTransformation();
		if (resourceExportInterface==null) resourceExportInterface=getActiveJob();
		if (resourceExportInterface==null) return; // nothing to do here, prevent an NPE
		
		// ((VariableSpace)resourceExportInterface).getVariable("Internal.Transformation.Filename.Directory");
		
		// Ask the user for a zip file to export to:
		//
		try {
			String zipFilename = null;
			while (Const.isEmpty(zipFilename)) {
				FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				dialog.setText(Messages.getString("Spoon.ExportResourceSelectZipFile"));
				dialog.setFilterExtensions(new String[] {"*.zip;*.ZIP", "*"});
				dialog.setFilterNames(new String[] { Messages.getString("System.FileType.ZIPFiles"), Messages.getString("System.FileType.AllFiles"), });
				setFilterPath(dialog);
				if (dialog.open()!=null)
				{
					lastDirOpened = dialog.getFilterPath();
					zipFilename = dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName();
					FileObject zipFileObject = KettleVFS.getFileObject(zipFilename);
					if (zipFileObject.exists()) {
						MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL);
						box.setMessage(Messages.getString("Spoon.ExportResourceZipFileExists.Message", zipFilename));
						box.setText(Messages.getString("Spoon.ExportResourceZipFileExists.Title"));
						int answer = box.open();
						if (answer==SWT.CANCEL) return;
						if (answer==SWT.NO) zipFilename = null;
					}
				} else {
					return;
				}
			}
			
			// Export the resources linked to the currently loaded file...
			//
			TopLevelResource topLevelResource = ResourceUtil.serializeResourceExportInterface(zipFilename, resourceExportInterface, (VariableSpace)resourceExportInterface, rep);
			String message = ResourceUtil.getExplanation(zipFilename, topLevelResource.getResourceName(), resourceExportInterface);
							
			EnterTextDialog enterTextDialog = new EnterTextDialog(shell, "Resource serialized", "This resource was serialized succesfully!", message);
			enterTextDialog.setReadOnly();
			enterTextDialog.open();
		} catch(Exception e) {
			new ErrorDialog(shell, "Error", "Error exporting current file", e);
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
			log.logBasic(toString(), "Save file as..."); //$NON-NLS-1$
		boolean saved = false;

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
					mb.setMessage(Messages.getString("Spoon.Dialog.PromptOverwriteFile.Message"));// "This file already exists.  Do you want to overwrite it?"
					mb.setText(Messages.getString("Spoon.Dialog.PromptOverwriteFile.Title"));// "This file already exists!"
					id = mb.open();
				}
			} catch (Exception e) {
				// TODO do we want to show an error dialog here? My first guess
				// is not, but we might.
			}
			if (id == SWT.YES) {
				save(meta, fname, export);
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
			log.logBasic(toString(), "Save file as..."); //$NON-NLS-1$
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
		VfsFileChooserDialog vfsFileChooser = new VfsFileChooserDialog(rootFile, initialFile);
		FileObject selectedFile = vfsFileChooser.open(shell, "Untitled", Const.STRING_TRANS_AND_JOB_FILTER_EXT, Const.getTransformationAndJobFilterNames(), VfsFileChooserDialog.VFS_DIALOG_SAVEAS);
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
					mb.setMessage(Messages.getString("Spoon.Dialog.PromptOverwriteFile.Message"));// "This file already exists.  Do you want to overwrite it?"
					mb.setText(Messages.getString("Spoon.Dialog.PromptOverwriteFile.Title"));// "This file already exists!"
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
		if (Const.isEmpty(meta.getName()) || delegates.jobs.isDefaultJobName(meta.getName())) {
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
				log.logDebug(toString(), Messages.getString("Spoon.Log.FileWritten") + " [" + fname + "]"); // "File
																											// written
																											// to
			meta.setFilename(fname);
			meta.clearChanged();
			setShellText();
		} catch (Exception e) {
			if (log.isDebug())
				log.logDebug(toString(), Messages.getString("Spoon.Log.ErrorOpeningFileForWriting") + e.toString());// "Error opening file for writing! --> "
			new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorSavingFile.Title"), Messages.getString("Spoon.Dialog.ErrorSavingFile.Message") + Const.CR + e.toString(), e);
		}
		return saved;
	}

	public void helpAbout() {
		MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION | SWT.CENTER);
		String mess = Messages.getString("System.ProductInfo") + Const.VERSION + Const.CR + Const.CR + Const.CR;// Kettle
																												// -
																												// Spoon
																												// version
		mess += Messages.getString("System.CompanyInfo") + Const.CR;
		mess += "         " + Messages.getString("System.ProductWebsiteUrl") + Const.CR; // (c)
																							// 2001-2004
																							// i-Bridge
																							// bvba
																							// www.kettle.be
		mess += Const.CR;
		mess += Const.CR;
		mess += Const.CR;
		mess += "         Build version : " + BuildVersion.getInstance().getVersion() + Const.CR;
		mess += "         Build date    : " + BuildVersion.getInstance().getBuildDate() + Const.CR;

		mb.setMessage(mess);
		mb.setText(APP_NAME);
		mb.open();
	}

	/**
	 * Show a dialog containing information on the different step plugins.
	 */
	public void helpShowStepPlugins() {
		List<Object[]> pluginInformation = StepLoader.getInstance().getPluginInformation();
		RowMetaInterface pluginInformationRowMeta = StepPlugin.getPluginInformationRowMeta();

		PreviewRowsDialog dialog = new PreviewRowsDialog(shell, null, SWT.NONE, null, pluginInformationRowMeta, pluginInformation);
		dialog.setTitleMessage(Messages.getString("Spoon.Dialog.StepPluginList.Title"), Messages.getString("Spoon.Dialog.StepPluginList.Message"));
		dialog.open();
	}

	/**
	 * Show a dialog containing information on the different job entry plugins.
	 */
	public void helpShowJobEntryPlugins() {
		List<Object[]> pluginInformation = JobEntryLoader.getInstance().getPluginInformation();
		RowMetaInterface pluginInformationRowMeta = StepPlugin.getPluginInformationRowMeta();

		PreviewRowsDialog dialog = new PreviewRowsDialog(shell, null, SWT.NONE, null, pluginInformationRowMeta, pluginInformation);
		dialog.setTitleMessage(Messages.getString("Spoon.Dialog.StepPluginList.Title"), Messages.getString("Spoon.Dialog.StepPluginList.Message"));
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
			mb.setMessage(Messages.getString("Spoon.Dialog.PleaseRestartApplication.Message"));
			mb.setText(Messages.getString("Spoon.Dialog.PleaseRestartApplication.Title"));
			mb.open();
		}
	}
	
	/**
	 * Matches if the filter is non-empty 
	 * @param string
	 * @return
	 */
	private boolean filterMatch(String string) {
		String filter = selectionFilter.getText();
		if (Const.isEmpty(string)) return true;
		if (Const.isEmpty(filter)) return true;
		
		try {
			if (string.matches(filter)) return true;
		} catch(Exception e) {
			log.logError(toString(), "Not a valid pattern ["+filter+"] : "+e.getMessage());
		}

		if (string.toUpperCase().contains(filter.toUpperCase())) return true;

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
			selectionTree = new Tree(variableComposite, SWT.SINGLE );
			props.setLook(selectionTree);
			selectionTree.setLayout(new FillLayout());

			/*
			 * ExpandItem treeItem = new ExpandItem(mainExpandBar, SWT.NONE);
			 * treeItem.setControl(selectionTree);
			 * treeItem.setHeight(shell.getBounds().height);
			 * setHeaderImage(treeItem,
			 * GUIResource.getInstance().getImageLogoSmall(),
			 * STRING_SPOON_MAIN_TREE, 0, true);
			 */

			// Add a tree memory as well...
			TreeMemory.addTreeListener(selectionTree, STRING_SPOON_MAIN_TREE);

			selectionTree.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent event) {
					if (event.button == 3) {
						setMenu(selectionTree);
					}
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

			// Keyboard shortcuts!
			selectionTree.addKeyListener(defKeys);

			// Set a listener on the tree
			addDragSourceToTree(selectionTree);
		}

		GUIResource guiResource = GUIResource.getInstance();
		TransMeta activeTransMeta = getActiveTransformation();
		JobMeta activeJobMeta = getActiveJob();
		boolean showAll = activeTransMeta == null && activeJobMeta == null;

		// get a list of transformations from the transformation map
		List<TransMeta> transformations = delegates.trans.getTransformationList();
		Collections.sort(transformations);
		TransMeta[] transMetas = transformations.toArray(new TransMeta[transformations.size()]);

		// get a list of jobs from the job map
		List<JobMeta> jobs = delegates.jobs.getJobList();
		Collections.sort(jobs);
		JobMeta[] jobMetas = jobs.toArray(new JobMeta[jobs.size()]);

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

			for (int t = 0; t < transMetas.length; t++) {
				TransMeta transMeta = transMetas[t];
				
				if (!props.isOnlyActiveFileShownInTree() || showAll || (activeTransMeta != null && activeTransMeta.equals(transMeta))) {
					
					// Add a tree item with the name of transformation
					//
					String name = delegates.tabs.makeTransGraphTabName(transMeta);
					if (Const.isEmpty(name)) {
						name = STRING_TRANS_NO_NAME;
					}
					
					TreeItem tiTransName = new TreeItem(tiTrans, SWT.NONE);
					tiTransName.setText(name);
					tiTransName.setImage(guiResource.getImageTransGraph());

					// Set expanded if this is the only transformation shown.
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
					for (int i=0;i<dbNames.length;i++) dbNames[i]=transMeta.getDatabase(i).getName();
					Arrays.sort(dbNames, new Comparator<String>() { public int compare(String o1, String o2) { return o1.compareToIgnoreCase(o2); } });

					// Draw the connections themselves below it.
					for (int i = 0; i < dbNames.length ; i++) {
						DatabaseMeta databaseMeta = transMeta.findDatabase(dbNames[i]);
						
						if (!filterMatch(dbNames[i])) continue;
						
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
						StepPlugin stepPlugin = StepLoader.getInstance().findStepPluginWithID(stepMeta.getStepID());
						
						if (!filterMatch(stepMeta.getName()) && !filterMatch(stepMeta.getDescription())) continue;
						
						TreeItem tiStep = new TreeItem(tiStepTitle, SWT.NONE);
						tiStep.setText(stepMeta.getName());
						if (stepMeta.isShared())
							tiStep.setFont(guiResource.getFontBold());
						if (!stepMeta.isDrawn())
							tiStep.setForeground(guiResource.getColorDarkGray());
						Image stepIcon = guiResource.getImagesStepsSmall().get(stepPlugin.getID()[0]);
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
						
						if (!filterMatch(hopMeta.toString())) continue;
						
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
						if (!filterMatch(partitionSchema.getName())) continue;
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
					Arrays.sort(slaveNames, new Comparator<String>() { public int compare(String o1, String o2) { return o1.compareToIgnoreCase(o2); } });

					for (int i = 0; i < slaveNames.length ; i++) {
						SlaveServer slaveServer = transMeta.findSlaveServer(slaveNames[i]);
						if (!filterMatch(slaveServer.getName())) continue;
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
						if (!filterMatch(clusterSchema.getName())) continue;
						TreeItem tiCluster = new TreeItem(tiClusterTitle, SWT.NONE);
						tiCluster.setText(clusterSchema.toString());
						tiCluster.setImage(guiResource.getImageCluster());
						if (clusterSchema.isShared())
							tiCluster.setFont(guiResource.getFontBold());
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
			for (int t = 0; t < jobMetas.length; t++) {
				JobMeta jobMeta = jobMetas[t];

				if (!props.isOnlyActiveFileShownInTree() || showAll || (activeJobMeta != null && activeJobMeta.equals(jobMeta))) {
					// Add a tree item with the name of job
					//
					String name = delegates.tabs.makeJobGraphTabName(jobMeta);
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
					for (int i=0;i<dbNames.length;i++) dbNames[i]=jobMeta.getDatabase(i).getName();
					Arrays.sort(dbNames, new Comparator<String>() { public int compare(String o1, String o2) { return o1.compareToIgnoreCase(o2); } });

					// Draw the connections themselves below it.
					for (int i = 0; i < dbNames.length; i++) {
						DatabaseMeta databaseMeta = jobMeta.findDatabase(dbNames[i]);
						if (!filterMatch(databaseMeta.getName())) continue;
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

						if (!filterMatch(jobEntry.getName()) && !filterMatch(jobEntry.getDescription())) continue;

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
							String key = jobEntry.getEntry().getPluginID();
							if (key == null)
								key = jobEntry.getEntry().getJobEntryType().name();
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
					Arrays.sort(slaveNames, new Comparator<String>() { public int compare(String o1, String o2) { return o1.compareToIgnoreCase(o2); } });

					for (int i = 0; i < slaveNames.length ; i++) {
						SlaveServer slaveServer = jobMeta.findSlaveServer(slaveNames[i]);
						if (!filterMatch(slaveServer.getName())) continue;
						TreeItem tiSlave = new TreeItem(tiSlaveTitle, SWT.NONE);
						tiSlave.setText(slaveServer.getName());
						tiSlave.setImage(guiResource.getImageSlave());
						if (slaveServer.isShared())
							tiSlave.setFont(guiResource.getFontBold());
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
		if (tabMapEntry.getObject() instanceof TransGraph) {
			TransGraph transGraph = (TransGraph) tabMapEntry.getObject();
			transGraph.redraw();
		}
		if (tabMapEntry.getObject() instanceof JobGraph) {
			JobGraph jobGraph = (JobGraph) tabMapEntry.getObject();
			jobGraph.redraw();
		}

		setShellText();
	}

	public void refreshHistory() {
		final TransGraph transGraph = getActiveTransGraph();
		if (transGraph != null) {
			transGraph.transHistoryDelegate.markRefreshNeeded();
		}
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
				log.logDebug(toString(), Messages.getString("Spoon.Log.NewStep") + steptype);// "New step: "

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

		StepLoader steploader = StepLoader.getInstance();
		StepPlugin stepPlugin = null;

		String locale = LanguageChoice.getInstance().getDefaultLocale().toString().toLowerCase();

		try {
			stepPlugin = steploader.findStepPluginWithDescription(description, locale);
			if (stepPlugin != null) {
				StepMetaInterface info = BaseStep.getStepInfo(stepPlugin, steploader);

				info.setDefault();

				if (openit) {
					StepDialogInterface dialog = this.getStepEntryDialog(info, transMeta, name);
					if (dialog != null) {
						name = dialog.open();
					}
				}
				inf = new StepMeta(stepPlugin.getID()[0], name, info);

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
						mb.setMessage(Messages.getString("Spoon.Dialog.ChangeStepname.Message", newname));// "This stepname already exists.  Spoon changed the stepname to ["+newname+"]"
						mb.setText(Messages.getString("Spoon.Dialog.ChangeStepname.Title"));// "Info!"
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
					props.increasePluginHistory(stepPlugin.getID()[0]);
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

					ShowBrowserDialog sbd = new ShowBrowserDialog(shell, Messages.getString("Spoon.Dialog.ErrorHelpText.Title"), content.toString());// "Error help text"
					sbd.open();
				} catch (Exception ex) {
					new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorShowingHelpText.Title"), Messages.getString("Spoon.Dialog.ErrorShowingHelpText.Message"), ex);// "Error showing help text"
				}
			} else {
				new ErrorDialog(shell, Messages.getString("Spoon.Dialog.UnableCreateNewStep.Title"), Messages.getString("Spoon.Dialog.UnableCreateNewStep.Message"), e);// "Error creating step"
				// "I was unable to create a new step"
			}
			return null;
		} catch (Throwable e) {
			if (!shell.isDisposed())
				new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorCreatingStep.Title"), Messages.getString("Spoon.Dialog.UnableCreateNewStep.Message"), e);// "Error creating step"
			return null;
		}

		return inf;
	}

	/*
	 * private void setTreeImages() {
	 * 
	 * TreeItem tiBaseCat[]=tiTransBase.getItems(); for (int
	 * x=0;x<tiBaseCat.length;x++) {
	 * tiBaseCat[x].setImage(GUIResource.getInstance().getImageBol());
	 * 
	 * TreeItem ti[] = tiBaseCat[x].getItems(); for (int i=0;i<ti.length;i++) {
	 * TreeItem stepitem = ti[i]; String description = stepitem.getText();
	 * 
	 * StepLoader steploader = StepLoader.getInstance(); StepPlugin sp =
	 * steploader.findStepPluginWithDescription(description); if (sp!=null) {
	 * Image stepimg =
	 * (Image)GUIResource.getInstance().getImagesStepsSmall().get
	 * (sp.getID()[0]); if (stepimg!=null) { stepitem.setImage(stepimg); } } } }
	 * }
	 */

	public void setShellText() {
		if (shell.isDisposed())
			return;

		String fname = null;
		String name = null;
		ChangedFlagInterface changed = null;

		TransMeta transMeta = getActiveTransformation();
		if (transMeta != null) {
			changed = transMeta;
			fname = transMeta.getFilename();
			name = transMeta.getName();
		}
		JobMeta jobMeta = getActiveJob();
		if (jobMeta != null) {
			changed = jobMeta;
			fname = jobMeta.getFilename();
			name = jobMeta.getName();
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
					text += Messages.getString("Spoon.Various.NoName");// "[no name]"
				}
			}
		} else {
			text += name;
		}

		if (changed != null && changed.hasChanged()) {
			text += " " + Messages.getString("Spoon.Various.Changed");
		}

		shell.setText(text);

		enableMenus();
		markTabsChanged();
	}

	public void enableMenus() {
		boolean enableTransMenu = getActiveTransformation() != null;
		boolean enableJobMenu = getActiveJob() != null;
		boolean enableRepositoryMenu = rep != null;

		boolean enableLastPreviewMenu = false;
		boolean disablePreviewButton = false;

		TransGraph transGraph = getActiveTransGraph();
		if (transGraph != null) {
			TransDebugMeta lastTransDebugMeta = transGraph.getLastTransDebugMeta();
			enableLastPreviewMenu = !(lastTransDebugMeta == null || lastTransDebugMeta.getStepDebugMetaMap().isEmpty());

			disablePreviewButton = transGraph.isRunning() && !transGraph.isHalting();
		}

		design.setEnabled(enableTransMenu || enableJobMenu);

		// Only enable certain menu-items if we need to.
		menuBar.setEnableById("file-save", enableTransMenu || enableJobMenu);
		menuBar.setEnableById("file-save-as", enableTransMenu || enableJobMenu);
		menuBar.setEnableById("file-close", enableTransMenu || enableJobMenu);
		menuBar.setEnableById("file-print", enableTransMenu || enableJobMenu);

		// Disable the undo and redo menus if there is no active transformation
		// or active job
		// DO NOT ENABLE them otherwise ... leave that to the undo/redo settings
		//
		if (!enableTransMenu && !enableJobMenu) {
			menuBar.setEnableById(UNDO_MENUITEM, false);
			menuBar.setEnableById(REDO_MENUITEM, false);
		}

		menuBar.setEnableById("edit-clear-selection", enableTransMenu);
		menuBar.setEnableById("edit-select-all", enableTransMenu);
		menuBar.setEnableById("edit-copy", enableTransMenu);
		menuBar.setEnableById("edit-paste", enableTransMenu);

		// Transformations
		menuBar.setEnableById("trans-run", enableTransMenu && !disablePreviewButton);
		menuBar.setEnableById("trans-replay", enableTransMenu && !disablePreviewButton);
		menuBar.setEnableById("trans-preview", enableTransMenu && !disablePreviewButton);
		menuBar.setEnableById("trans-debug", enableTransMenu && !disablePreviewButton);
		menuBar.setEnableById("trans-verify", enableTransMenu);
		menuBar.setEnableById("trans-impact", enableTransMenu);
		menuBar.setEnableById("trans-get-sql", enableTransMenu);
		menuBar.setEnableById("trans-last-impact", enableTransMenu);
		menuBar.setEnableById("trans-last-check", enableTransMenu);
		menuBar.setEnableById("trans-last-preview", enableTransMenu);
		menuBar.setEnableById("trans-copy", enableTransMenu);
		// miTransPaste.setEnabled(enableTransMenu);
		menuBar.setEnableById("trans-copy-image", enableTransMenu);
		menuBar.setEnableById("trans-settings", enableTransMenu);

		// Jobs
		menuBar.setEnableById("job-run", enableJobMenu);
		menuBar.setEnableById("job-copy", enableJobMenu);
		menuBar.setEnableById("job-settings", enableJobMenu);

		menuBar.setEnableById("wizard-connection", enableTransMenu || enableJobMenu);
		menuBar.setEnableById("wizard-copy-table", enableTransMenu || enableJobMenu);
		menuBar.setEnableById("wizard-copy-tables", enableRepositoryMenu || enableTransMenu || enableJobMenu);

		menuBar.setEnableById("repository-disconnect", enableRepositoryMenu);
		menuBar.setEnableById("repository-explore", enableRepositoryMenu);
		menuBar.setEnableById("repository-edit-user", enableRepositoryMenu);

		menuBar.setEnableById("trans-last-preview", enableLastPreviewMenu);

		// What steps & plugins to show?
		refreshCoreObjects();
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

		Image img = transGraph.getTransformationImage(printer, max.x, max.y, false, 1.0f);

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

		PaletteData pal = ps.getPaletteData();

		ImageData imd = new ImageData(max.x, max.y, printer.getDepth(), pal);
		Image img = new Image(printer, imd);

		GC img_gc = new GC(img);

		// Clear the background first, fill with background color...
		img_gc.setForeground(GUIResource.getInstance().getColorBackground());
		img_gc.fillRectangle(0, 0, max.x, max.y);

		// Draw the transformation...
		jobGraph.drawJob(printer, img_gc, false);

		ps.printImage(shell, img);

		img_gc.dispose();
		img.dispose();
		ps.dispose();
	}

	public TransGraph getActiveTransGraph() {
		if (tabfolder.getSelected() == null)
			return null;

		TabMapEntry mapEntry = delegates.tabs.getTab(tabfolder.getSelected());
		if (mapEntry != null) {
			if (mapEntry.getObject() instanceof TransGraph) {
				return (TransGraph) mapEntry.getObject();
			}
		}
		return null;
	}

	public JobGraph getActiveJobGraph() {
		TabMapEntry mapEntry = delegates.tabs.getTab(tabfolder.getSelected());
		if (mapEntry.getObject() instanceof JobGraph)
			return (JobGraph) mapEntry.getObject();
		return null;
	}

	public EngineMetaInterface getActiveMeta() {
		if (tabfolder == null)
			return null;
		TabItem tabItem = tabfolder.getSelected();
		if (tabItem == null)
			return null;

		// What transformation is in the active tab?
		// TransLog, TransGraph & TransHist contain the same transformation
		//
		TabMapEntry mapEntry = delegates.tabs.getTab(tabfolder.getSelected());
		EngineMetaInterface meta = null;
		if (mapEntry != null) {
			if (mapEntry.getObject() instanceof TransGraph)
				meta = (mapEntry.getObject()).getMeta();
			if (mapEntry.getObject() instanceof JobGraph)
				meta = (mapEntry.getObject()).getMeta();
		}

		return meta;
	}

	/**
	 * @return The active TransMeta object by looking at the selected
	 *         TransGraph, TransLog, TransHist If nothing valueable is selected,
	 *         we return null
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
		return delegates.trans.getTransformation(tabItemText);
	}

	public JobMeta findJob(String tabItemText) {
		return delegates.jobs.getJob(tabItemText);
	}

	public TransMeta[] getLoadedTransformations() {
		List<TransMeta> list = delegates.trans.getTransformationList();
		return list.toArray(new TransMeta[list.size()]);
	}

	public JobMeta[] getLoadedJobs() {
		List<JobMeta> list = delegates.jobs.getJobList();
		return list.toArray(new JobMeta[list.size()]);
	}

	public void saveSettings() {
		WindowProperty winprop = new WindowProperty(shell);
		winprop.setName(APPL_TITLE);
		props.setScreen(winprop);

		props.setLogLevel(log.getLogLevelDesc());
		props.setLogFilter(log.getFilter());
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
			if (entry.getObjectType() == TabMapEntry.OBJECT_TYPE_TRANSFORMATION_GRAPH) {
				fileType = LastUsedFile.FILE_TYPE_TRANSFORMATION;
				TransMeta transMeta = (TransMeta) entry.getObject().getManagedObject();
				filename = rep != null ? transMeta.getName() : transMeta.getFilename();
				directory = transMeta.getDirectory().toString();
				openType = LastUsedFile.OPENED_ITEM_TYPE_MASK_GRAPH;
				if (delegates.tabs.findTabItem(delegates.tabs.makeLogTabName(transMeta), TabMapEntry.OBJECT_TYPE_TRANSFORMATION_LOG) != null) {
					openType |= LastUsedFile.OPENED_ITEM_TYPE_MASK_LOG;
				}
				if (delegates.tabs.findTabItem(delegates.tabs.makeHistoryTabName(transMeta), TabMapEntry.OBJECT_TYPE_TRANSFORMATION_HISTORY) != null) {
					openType |= LastUsedFile.OPENED_ITEM_TYPE_MASK_HISTORY;
				}
			} else if (entry.getObjectType() == TabMapEntry.OBJECT_TYPE_JOB_GRAPH) {
				fileType = LastUsedFile.FILE_TYPE_JOB;
				JobMeta jobMeta = (JobMeta) entry.getObject().getManagedObject();
				filename = rep != null ? jobMeta.getName() : jobMeta.getFilename();
				directory = jobMeta.getDirectory().toString();
				openType = LastUsedFile.OPENED_ITEM_TYPE_MASK_GRAPH;
				if (delegates.tabs.findTabItem(delegates.tabs.makeJobLogTabName(jobMeta), TabMapEntry.OBJECT_TYPE_JOB_LOG) != null) {
					openType |= LastUsedFile.OPENED_ITEM_TYPE_MASK_LOG;
				}
				if (delegates.tabs.findTabItem(delegates.tabs.makeJobHistoryTabName(jobMeta), TabMapEntry.OBJECT_TYPE_JOB_HISTORY) != null) {
					openType |= LastUsedFile.OPENED_ITEM_TYPE_MASK_HISTORY;
				}
			}

			if (fileType != null) {
				props.addOpenTabFile(fileType, filename, directory, rep != null, rep != null ? rep.getName() : null, openType);
			}
		}

		props.saveProps();
	}

	public void loadSettings() {
		log.setLogLevel(props.getLogLevel());
		log.setFilter(props.getLogFilter());

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

		// Set the menubar text
		menuBar.setTextById(UNDO_MENUITEM, prev == null ? UNDO_UNAVAILABLE : Messages.getString("Spoon.Menu.Undo.Available", prev.toString())); //$NON-NLS-1$
		menuBar.setTextById(REDO_MENUITEM, next == null ? REDO_UNAVAILABLE : Messages.getString("Spoon.Menu.Redo.Available", next.toString())); //$NON-NLS-1$

		// Set the enabled flags
		menuBar.setEnableById(UNDO_MENUITEM, prev != null);
		menuBar.setEnableById(REDO_MENUITEM, next != null);
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

		CheckTransProgressDialog ctpd = new CheckTransProgressDialog(shell, transMeta, transGraph.getRemarks(), only_selected);
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
			PreviewRowsDialog prd = new PreviewRowsDialog(shell, Variables.getADefaultVariableSpace(), SWT.NONE, "-", rowMeta, rows);
			prd.setTitleMessage(Messages.getString("Spoon.Dialog.ImpactAnalyses.Title"), Messages.getString("Spoon.Dialog.ImpactAnalyses.Message"));// "Impact analyses"
			// "Result of analyses:"
			prd.open();
		} else {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
			if (transGraph.isImpactFinished()) {
				mb.setMessage(Messages.getString("Spoon.Dialog.TransformationNoImpactOnDatabase.Message"));// "As far as I can tell, this transformation has no impact on any database."
			} else {
				mb.setMessage(Messages.getString("Spoon.Dialog.RunImpactAnalysesFirst.Message"));// "Please run the impact analyses first on this transformation."
			}
			mb.setText(Messages.getString("Spoon.Dialog.ImpactAnalyses.Title"));// Impact
			mb.open();
		}
	}

	public void toClipboard(String cliptext) {
		try {
			GUIResource.getInstance().toClipboard(cliptext);
		} catch (Throwable e) {
			new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ExceptionCopyToClipboard.Title"), Messages.getString("Spoon.Dialog.ExceptionCopyToClipboard.Message"), e);
		}
	}

	public String fromClipboard() {
		try {
			return GUIResource.getInstance().fromClipboard();
		} catch (Throwable e) {
			new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ExceptionPasteFromClipboard.Title"), Messages.getString("Spoon.Dialog.ExceptionPasteFromClipboard.Message"), e);
			return null;
		}
	}

	/**
	 * Paste transformation from the clipboard...
	 * 
	 */
	public void pasteTransformation() {
		if (log.isDetailed())
			log.logDetailed(toString(), Messages.getString("Spoon.Log.PasteTransformationFromClipboard"));// "Paste transformation from the clipboard!"
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
			new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorPastingTransformation.Title"), Messages.getString("Spoon.Dialog.ErrorPastingTransformation.Message"), e);// Error
																																													// pasting
																																													// transformation
			// "An error occurred pasting a transformation from the clipboard"
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
			JobMeta jobMeta = new JobMeta(log, XMLHandler.getSubNode(doc, JobMeta.XML_TAG), rep, this);
			delegates.jobs.addJobGraph(jobMeta); // create a new tab
			refreshGraph();
			refreshTree();
		} catch (KettleException e) {
			new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorPastingJob.Title"), Messages.getString("Spoon.Dialog.ErrorPastingJob.Message"), e);// Error
			// pasting
			// transformation
			// "An error occurred pasting a transformation from the clipboard"
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
		Image image = transGraph.getTransformationImage(Display.getCurrent(), area.x, area.y, false, 1.0f);
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
				List<DatabaseMeta> repDBs = rep.getDatabases();
				for (int i = 0; i < repDBs.size(); i++) {
					DatabaseMeta databaseMeta = (DatabaseMeta) repDBs.get(i);
					map.put(databaseMeta.getName(), databaseMeta);
				}
			} catch (Exception e) {
				log.logError(toString(), "Unexpected error reading databases from the repository: " + e.toString());
				log.logError(toString(), Const.getStackTracker(e));
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

	// Added this method to avoid code pasting... SEMINOLE-69
	private boolean openRepositoryDialog(RepositoriesDialog rd, RepositoryMeta repositoryMeta, UserInfo userinfo) {
		if (rd.open()) {
			repositoryMeta = rd.getRepository();
			userinfo = rd.getUser();
			if (!userinfo.useTransformations()) {
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				mb.setMessage(Messages.getString("Spoon.Dialog.RepositoryUserCannotWork.Message"));// "Sorry, this repository user can't work with transformations from the repository."
				mb.setText(Messages.getString("Spoon.Dialog.RepositoryUserCannotWork.Title"));// "Error!"
				mb.open();

				userinfo = null;
				repositoryMeta = null;

				return false;
			} else {
				String repName = repositoryMeta.getName();
				RepositoriesMeta repsinfo = new RepositoriesMeta(log);
				if (repsinfo.readData()) {
					repositoryMeta = repsinfo.findRepository(repName);
					if (repositoryMeta != null) {
						// Define and connect to the repository...
						setRepository(new Repository(log, repositoryMeta, userinfo));
						return true;
					} else {
						log.logError(toString(), Messages.getString("Spoon.Log.NoRepositoryRrovided"));// "No repository provided, can't load transformation."
					}
				} else {
					log.logError(toString(), Messages.getString("Spoon.Log.NoRepositoriesDefined"));// "No repositories defined on this system."
				}

				return false;

			}
		} else {
			// Exit point: user pressed CANCEL!
			if (rd.isCancelled()) {
				return false;
			}
		}

		return true;
	}

	public boolean selectRep(Splash splash, CommandLineOption[] options) {
		RepositoryMeta repositoryMeta = null;
		UserInfo userinfo = null;

		StringBuffer optionRepname = getCommandLineOption(options, "rep").getArgument();
		StringBuffer optionFilename = getCommandLineOption(options, "file").getArgument();
		int perms[] = new int[] { PermissionMeta.TYPE_PERMISSION_TRANSFORMATION, PermissionMeta.TYPE_PERMISSION_JOB };

		if (Const.isEmpty(optionRepname) && Const.isEmpty(optionFilename) && props.showRepositoriesDialogAtStartup()) {
			if (log.isBasic())
				log.logBasic(APP_NAME, Messages.getString("Spoon.Log.AskingForRepository"));// "Asking for repository"

			splash.hide();
			RepositoriesDialog rd = new RepositoriesDialog(display, perms, Messages.getString("Spoon.Application.Name"));// "Spoon"

			return openRepositoryDialog(rd, repositoryMeta, userinfo);
		} else if (!Const.isEmpty(optionRepname) && Const.isEmpty(optionFilename)) {
			RepositoriesMeta repsinfo = new RepositoriesMeta(log);
			if (repsinfo.readData()) {
				repositoryMeta = repsinfo.findRepository(optionRepname.toString());
				if (repositoryMeta != null) {
					// Define and connect to the repository...
					setRepository(new Repository(log, repositoryMeta, userinfo));
				} else {
					String msg = Messages.getString("Spoon.Log.NoRepositoriesDefined");
					log.logError(toString(), msg);// "No repositories defined on this system."
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
					mb.setMessage(Messages.getString("Spoon.Error.Repository.NotFound", optionRepname.toString()));
					mb.setText(Messages.getString("Spoon.Error.Repository.NotFound.Title"));
					mb.open();
					RepositoriesDialog rd = new RepositoriesDialog(display, perms, Messages.getString("Spoon.Application.Name"));// "Spoon"

					return openRepositoryDialog(rd, repositoryMeta, userinfo);
				}
			}
		}
		return true;
	}

	public void handleStartOptions(CommandLineOption[] options) {

		StringBuffer optionRepname = getCommandLineOption(options, "rep").getArgument();
		StringBuffer optionFilename = getCommandLineOption(options, "file").getArgument();
		StringBuffer optionDirname = getCommandLineOption(options, "dir").getArgument();
		StringBuffer optionTransname = getCommandLineOption(options, "trans").getArgument();
		StringBuffer optionJobname = getCommandLineOption(options, "job").getArgument();
		StringBuffer optionUsername = getCommandLineOption(options, "user").getArgument();
		StringBuffer optionPassword = getCommandLineOption(options, "pass").getArgument();

		try {
			// Read kettle transformation specified on command-line?
			if (!Const.isEmpty(optionRepname) || !Const.isEmpty(optionFilename)) {
				if (!Const.isEmpty(optionRepname)) {
					if (rep != null) {
						if (rep.connect(Messages.getString("Spoon.Application.Name")))// "Spoon"
						{
							if (Const.isEmpty(optionDirname))
								optionDirname = new StringBuffer(RepositoryDirectory.DIRECTORY_SEPARATOR);

							// Check username, password
							rep.userinfo = new UserInfo(rep, optionUsername.toString(), optionPassword.toString());

							if (rep.userinfo.getID() > 0) {
								// Options /file, /job and /trans are mutually
								// exclusive
								int t = (Const.isEmpty(optionFilename) ? 0 : 1) + (Const.isEmpty(optionJobname) ? 0 : 1) + (Const.isEmpty(optionTransname) ? 0 : 1);
								if (t > 1) {
									log.logError(toString(), Messages.getString("Spoon.Log.MutuallyExcusive")); // "More then one mutually exclusive options /file, /job and /trans are specified."
								} else if (t == 1) {
									if (!Const.isEmpty(optionFilename)) {
										openFile(optionFilename.toString(), false);
									} else {
										// OK, if we have a specified job or
										// transformation, try to load it...
										// If not, keep the repository logged
										// in.
										RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(optionDirname.toString());
										if (repdir == null) {
											log.logError(toString(), Messages.getString("Spoon.Log.UnableFindDirectory", optionDirname.toString())); // "Can't find directory ["+dirname+"] in the repository."
										} else {
											if (!Const.isEmpty(optionTransname)) {
												TransMeta transMeta = new TransMeta(rep, optionTransname.toString(), repdir);
												transMeta.setFilename(optionRepname.toString());
												transMeta.clearChanged();
												transMeta.setInternalKettleVariables();
												addTransGraph(transMeta);
											} else {
												// Try to load a specified job
												// if any
												JobMeta jobMeta = new JobMeta(log, rep, optionJobname.toString(), repdir);
												jobMeta.setFilename(optionRepname.toString());
												jobMeta.clearChanged();
												jobMeta.setInternalKettleVariables();
												delegates.jobs.addJobGraph(jobMeta);
											}
										}
									}
								}
							} else {
								log.logError(toString(), Messages.getString("Spoon.Log.UnableVerifyUser"));// "Can't verify username and password."
								rep.disconnect();
								rep = null;
							}
						} else {
							log.logError(toString(), Messages.getString("Spoon.Log.UnableConnectToRepository"));// "Can't connect to the repository."
						}
					} else {
						log.logError(toString(), Messages.getString("Spoon.Log.NoRepositoriesDefined"));// "No repositories defined on this system."
					}
				} else if (!Const.isEmpty(optionFilename)) {
					openFile(optionFilename.toString(), false);
				}
			} else // Normal operations, nothing on the commandline...
			{
				// Can we connect to the repository?
				if (rep != null && rep.userinfo != null) {
					if (!rep.connect(Messages.getString("Spoon.Application.Name"))) // "Spoon"
					{
						setRepository(null);
					}
				}

				if (props.openLastFile()) {
					if (log.isDetailed())
						log.logDetailed(toString(), Messages.getString("Spoon.Log.TryingOpenLastUsedFile"));// "Trying to open the last file used."

					List<LastUsedFile> lastUsedFiles = props.getOpenTabFiles();
					for (LastUsedFile lastUsedFile : lastUsedFiles) {
						RepositoryMeta repInfo = (rep == null) ? null : rep.getRepositoryInfo();
						loadLastUsedFile(lastUsedFile, repInfo, false);
					}
				}
			}
		} catch (KettleException ke) {
			log.logError(toString(), Messages.getString("Spoon.Log.ErrorOccurred") + Const.CR + ke.getMessage());// "An error occurred: "
			log.logError(toString(), Const.getStackTracker(ke));
			// do not just eat the exception
			new ErrorDialog(shell, Messages.getString("Spoon.Log.ErrorOccurred"), Messages.getString("Spoon.Log.ErrorOccurred") + Const.CR + ke.getMessage(), ke);
			rep = null;
		}

	}

	public void start(Splash splash, CommandLineOption[] options) {

		boolean stop = !selectRep(splash, options);
		if (stop) {
			splash.dispose();
			stop = quitFile();
		}

		if (!stop) {
			handleStartOptions(options);
			open();

			if (props.showTips()) {
				TipsDialog tip = new TipsDialog(shell);
				tip.open();
			}

			if (splash != null) {
				splash.dispose();
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
					log.logError(toString(), Messages.getString("Spoon.Log.UnexpectedErrorOccurred") + Const.CR + e.getMessage());// "An unexpected error occurred in Spoon: probable cause: please close all windows before stopping Spoon! "
					log.logError(toString(), Const.getStackTracker(e));
					try {
						new ErrorDialog(shell, Messages.getString("Spoon.Log.UnexpectedErrorOccurred"), Messages.getString("Spoon.Log.UnexpectedErrorOccurred") + Const.CR + e.getMessage(), e);
						// Retry dialog
						MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION | SWT.NO | SWT.YES);
						mb.setText(Messages.getString("Spoon.Log.UnexpectedErrorRetry.Titel"));
						mb.setMessage(Messages.getString("Spoon.Log.UnexpectedErrorRetry.Message"));
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
				log.logBasic(toString(), APP_NAME + " " + Messages.getString("Spoon.Log.AppHasEnded"));// " has ended."

			// Close the logfile
			log.close();
		}

	}

	// public Splash splash;

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

		CommandLineOption[] clOptions = new CommandLineOption[] { new CommandLineOption("rep", "Repository name", new StringBuffer()), new CommandLineOption("user", "Repository username", new StringBuffer()),
				new CommandLineOption("pass", "Repository password", new StringBuffer()), new CommandLineOption("job", "The name of the job to launch", new StringBuffer()),
				new CommandLineOption("trans", "The name of the transformation to launch", new StringBuffer()), new CommandLineOption("dir", "The directory (don't forget the leading /)", new StringBuffer()),
				new CommandLineOption("file", "The filename (Transformation in XML) to launch", new StringBuffer()), new CommandLineOption("level", "The logging level (Basic, Detailed, Debug, Rowlevel, Error, Nothing)", new StringBuffer()),
				new CommandLineOption("logfile", "The logging file to write to", new StringBuffer()), new CommandLineOption("log", "The logging file to write to (deprecated)", new StringBuffer(), false, true), };

		LogWriter log;
		LogWriter.setConsoleAppenderDebug();
		// start with the default logger until we find out otherwise
		log = LogWriter.getInstance(LogWriter.LOG_LEVEL_BASIC);

		// Parse the options...
		if (!CommandLineOption.parseArguments(args, clOptions, log)) {
			log.logError("Spoon", "Command line option not understood");
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

	private void loadLastUsedFile(LastUsedFile lastUsedFile, RepositoryMeta repositoryMeta) throws KettleException {
		loadLastUsedFile(lastUsedFile, repositoryMeta, true);
	}

	private void loadLastUsedFile(LastUsedFile lastUsedFile, RepositoryMeta repositoryMeta, boolean trackIt) throws KettleException {
		boolean useRepository = repositoryMeta != null;

		// Perhaps we need to connect to the repository?
		// 
		if (lastUsedFile.isSourceRepository()) {
			if (!Const.isEmpty(lastUsedFile.getRepositoryName())) {
				if (useRepository && !lastUsedFile.getRepositoryName().equalsIgnoreCase(repositoryMeta.getName())) {
					// We just asked...
					useRepository = false;
				}
			}
		}

		if (useRepository && lastUsedFile.isSourceRepository()) {
			if (rep != null) // load from this repository...
			{
				if (rep.getName().equalsIgnoreCase(lastUsedFile.getRepositoryName())) {
					RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(lastUsedFile.getDirectory());
					if (repdir != null) {
						// Are we loading a transformation or a job?
						if (lastUsedFile.isTransformation()) {
							if (log.isDetailed())
								log.logDetailed(toString(), Messages.getString("Spoon.Log.AutoLoadingTransformation", lastUsedFile.getFilename(), lastUsedFile.getDirectory()));// "Auto loading transformation ["+lastfiles[0]+"] from repository directory ["+lastdirs[0]+"]"
							TransLoadProgressDialog tlpd = new TransLoadProgressDialog(shell, rep, lastUsedFile.getFilename(), repdir);
							TransMeta transMeta = tlpd.open();
							if (transMeta != null) {
								if (trackIt)
									props.addLastFile(LastUsedFile.FILE_TYPE_TRANSFORMATION, lastUsedFile.getFilename(), repdir.getPath(), true, rep.getName());
								transMeta.setFilename(lastUsedFile.getFilename());
								transMeta.clearChanged();
								addTransGraph(transMeta);
								refreshTree();
							}
						} else if (lastUsedFile.isJob()) {
							JobLoadProgressDialog progressDialog = new JobLoadProgressDialog(shell, rep, lastUsedFile.getFilename(), repdir);
							JobMeta jobMeta = progressDialog.open();
							if (trackIt)
								props.addLastFile(LastUsedFile.FILE_TYPE_JOB, lastUsedFile.getFilename(), repdir.getPath(), true, rep.getName());
							if (jobMeta != null) {
								jobMeta.clearChanged();
								delegates.jobs.addJobGraph(jobMeta);
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
				 * TransMeta transMeta = new
				 * TransMeta(lastUsedFile.getFilename());
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
				 * JobMeta jobMeta = new JobMeta(log,
				 * lastUsedFile.getFilename(), rep);
				 * jobMeta.setFilename(lastUsedFile.getFilename());
				 * jobMeta.clearChanged();
				 * props.addLastFile(LastUsedFile.FILE_TYPE_JOB,
				 * lastUsedFile.getFilename(), null, false, null);
				 * addJobGraph(jobMeta);
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
	@SuppressWarnings("deprecation") // retry of required fields acquisition
	public void generateFieldMapping(TransMeta transMeta, StepMeta stepMeta) {
		try {
			if (stepMeta != null) {
				StepMetaInterface smi = stepMeta.getStepMetaInterface();
				RowMetaInterface targetFields = smi.getRequiredFields(transMeta);
				if (targetFields.isEmpty()) smi.getRequiredFields(); // retry, get rid of this method in 4.x
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
					stepName = transMeta.getAlternativeStepname(stepName); // if it's already there, rename it.

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
			new ErrorDialog(shell, "Error creating mapping", "There was an error when Kettle tried to generate a field mapping against the target step", e);
		}
	}

	public void editPartitioning(TransMeta transMeta, StepMeta stepMeta) {
		StepPartitioningMeta stepPartitioningMeta = stepMeta.getStepPartitioningMeta();
		if (stepPartitioningMeta == null)
			stepPartitioningMeta = new StepPartitioningMeta();

		StepMeta before = (StepMeta) stepMeta.clone();

		Set<Entry<String, Partitioner>> plugins = StepLoader.getPartitionerList().entrySet();
		String options[] = new String[StepPartitioningMeta.methodDescriptions.length + plugins.size()];
		String codes[] = new String[StepPartitioningMeta.methodDescriptions.length + plugins.size()];
		System.arraycopy(StepPartitioningMeta.methodDescriptions, 0, options, 0, StepPartitioningMeta.methodDescriptions.length);
		System.arraycopy(StepPartitioningMeta.methodCodes, 0, codes, 0, StepPartitioningMeta.methodCodes.length);

		Iterator<Entry<String, Partitioner>> it = plugins.iterator();
		int idx = 0;
		while (it.hasNext()) {
			Partitioner entry = it.next().getValue();
			options[StepPartitioningMeta.methodDescriptions.length + idx] = entry.getDescription();
			codes[StepPartitioningMeta.methodCodes.length + idx] = entry.getId();
			idx++;
		}

		for (int i = 0; i < codes.length; i++) {
			if (codes[i].equals(stepPartitioningMeta.getMethod())) {
				idx = i;
				break;
			}
		}

		EnterSelectionDialog dialog = new EnterSelectionDialog(shell, options, "Partioning method", "Select the partitioning method");
		String methodDescription = dialog.open(idx);
		if (methodDescription != null) {
			String method = StepPartitioningMeta.methodCodes[StepPartitioningMeta.PARTITIONING_METHOD_NONE];
			for (int i = 0; i < options.length; i++) {
				if (options[i].equals(methodDescription)) {
					method = codes[i];
				}
			}

			int methodType = StepPartitioningMeta.getMethodType(method);
			stepPartitioningMeta.setMethodType(methodType);
			stepPartitioningMeta.setMethod(method);
			switch (methodType) {
			case StepPartitioningMeta.PARTITIONING_METHOD_NONE:
				break;
			case StepPartitioningMeta.PARTITIONING_METHOD_MIRROR:
			case StepPartitioningMeta.PARTITIONING_METHOD_SPECIAL:

				// Ask for a Partitioning Schema
				String schemaNames[] = transMeta.getPartitionSchemasNames();
				if (schemaNames.length == 0) {
					MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					box.setText("Create a partition schema");
					box.setMessage("You first need to create one or more partition schemas in the transformation settings dialog before you can select one!");
					box.open();
				} else {
					// Set the partitioning schema too.
					PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();
					idx = -1;
					if (partitionSchema != null) {
						idx = Const.indexOfString(partitionSchema.getName(), schemaNames);
					}
					EnterSelectionDialog askSchema = new EnterSelectionDialog(shell, schemaNames, "Select a partition schema", "Select the partition schema to use:");
					String schemaName = askSchema.open(idx);
					if (schemaName != null) {
						idx = Const.indexOfString(schemaName, schemaNames);
						stepPartitioningMeta.setPartitionSchema(transMeta.getPartitionSchemas().get(idx));
					}
				}

				if (methodType == StepPartitioningMeta.PARTITIONING_METHOD_SPECIAL) {
					// ask for a fieldname

					StepDialogInterface partitionerDialog = null;
					try {
						partitionerDialog = delegates.steps.getPartitionerDialog(stepMeta.getStepMetaInterface(), stepPartitioningMeta, transMeta);
						/* String result = */partitionerDialog.open();
					} catch (Exception e) {
						e.printStackTrace();
					}

					// EnterStringDialog stringDialog = new
					// EnterStringDialog(shell,
					// Const.NVL(stepPartitioningMeta.getFieldName(), ""),
					// "Fieldname", "Enter a field name to partition on");
					// String fieldName = stringDialog.open();
					// stepPartitioningMeta.setFieldName(fieldName);
				}
				break;
			}
			StepMeta after = (StepMeta) stepMeta.clone();
			addUndoChange(transMeta, new StepMeta[] { before }, new StepMeta[] { after }, new int[] { transMeta.indexOfStep(stepMeta) });

			refreshGraph();
		}
	}

	/**
	 * Select a clustering schema for this step.
	 * 
	 * @param stepMeta
	 *            The step to set the clustering schema for.
	 */
	public void editClustering(TransMeta transMeta, StepMeta stepMeta) {
		editClustering(transMeta, new StepMeta[] { stepMeta, });
	}

	/**
	 * Select a clustering schema for this step.
	 * 
	 * @param stepMeta
	 *            The steps (at least one!) to set the clustering schema for.
	 */
	public void editClustering(TransMeta transMeta, StepMeta[] stepMetas) {
		StepMeta stepMeta = stepMetas[0];
		int idx = -1;
		if (stepMeta.getClusterSchema() != null) {
			idx = transMeta.getClusterSchemas().indexOf(stepMeta.getClusterSchema());
		}
		String[] clusterSchemaNames = transMeta.getClusterSchemaNames();
		EnterSelectionDialog dialog = new EnterSelectionDialog(shell, clusterSchemaNames, "Cluster schema", "Select the cluster schema to use (cancel=clear)");
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

		PartitionSchemaDialog dialog = new PartitionSchemaDialog(shell, partitionSchema, transMeta.getDatabases(), transMeta);
		if (dialog.open()) {
			transMeta.getPartitionSchemas().add(partitionSchema);
			refreshTree();
		}
	}

	private void editPartitionSchema(TransMeta transMeta, PartitionSchema partitionSchema) {
		PartitionSchemaDialog dialog = new PartitionSchemaDialog(shell, partitionSchema, transMeta.getDatabases(), transMeta);
		if (dialog.open()) {
			refreshTree();
		}
	}

	private void delPartitionSchema(TransMeta transMeta, PartitionSchema partitionSchema) {
		try {
			if (rep != null && partitionSchema.getId() > 0) {
				// remove the partition schema from the repository too...
				rep.delPartitionSchema(partitionSchema.getId());
			}

			int idx = transMeta.getPartitionSchemas().indexOf(partitionSchema);
			transMeta.getPartitionSchemas().remove(idx);
			refreshTree();
		} catch (KettleException e) {
			new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorDeletingClusterSchema.Title"), Messages.getString("Spoon.Dialog.ErrorDeletingClusterSchema.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
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
			if (rep != null && clusterSchema.getId() > 0) {
				// remove the partition schema from the repository too...
				rep.delClusterSchema(clusterSchema.getId());
			}

			int idx = transMeta.getClusterSchemas().indexOf(clusterSchema);
			transMeta.getClusterSchemas().remove(idx);
			refreshTree();
		} catch (KettleException e) {
			new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorDeletingPartitionSchema.Title"), Messages.getString("Spoon.Dialog.ErrorDeletingPartitionSchema.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
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
			new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorDeletingSlave.Title"), Messages.getString("Spoon.Dialog.ErrorDeletingSlave.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
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
		executeFile(tc.isExecutingLocally(), tc.isExecutingRemotely(), tc.isExecutingClustered(), false, false, new Date(), false);
	}

	public void previewFile() {
		executeFile(true, false, false, true, false, null, true);
	}

	public void debugFile() {
		executeFile(true, false, false, false, true, null, true);
	}

	public void executeFile(boolean local, boolean remote, boolean cluster, boolean preview, boolean debug, Date replayDate, boolean safe) {
		TransMeta transMeta = getActiveTransformation();
		if (transMeta != null)
			executeTransformation(transMeta, local, remote, cluster, preview, debug, replayDate, safe);

		JobMeta jobMeta = getActiveJob();
		if (jobMeta != null)
			executeJob(jobMeta, local, remote, replayDate, safe);

	}

	public void executeTransformation(final TransMeta transMeta, final boolean local, final boolean remote, final boolean cluster, final boolean preview, final boolean debug, final Date replayDate, final boolean safe) {
		new Thread() {
			public void run() {
				getDisplay().asyncExec(new Runnable() { public void run() { 
						try {
							delegates.trans.executeTransformation(transMeta, local, remote, cluster, preview, debug, replayDate, safe);
						} catch (Exception e) {
							new ErrorDialog(shell, "Execute transformation", "There was an error during transformation execution", e);
						}
					}
				});
			}	
		}.start();
	}

	public void executeJob(JobMeta jobMeta, boolean local, boolean remote, Date replayDate, boolean safe) {
		// delegates.jobs.addJobLog(jobMeta);
		// JobLog jobLog = getActiveJobLog();
		// jobLog.startJob(replayDate);

		try {
			delegates.jobs.executeJob(jobMeta, local, remote, replayDate, safe);
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

	public void pasteSteps() {
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
						pasteXML(transGraph.getManagedObject(), clipcontent, transGraph.getLastMove());
					}
				} else if (jobEntries) {
					JobGraph jobGraph = getActiveJobGraph();
					if (jobGraph != null && jobGraph.getLastMove() != null) {
						pasteXML(jobGraph.getManagedObject(), clipcontent, jobGraph.getLastMove());
					}

				}
			} catch (KettleXMLException e) {
				log.logError(toString(), "Unable to paste", e);
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
			log.logError(toString(), "Could not create dialog for " + stepMeta.getDialogClassName(), t);
		}
		return null;
	}

	public void editJobEntry(JobMeta jobMeta, JobEntryCopy je) {
		delegates.jobs.editJobEntry(jobMeta, je);
	}

	public JobEntryTrans newJobEntry(JobMeta jobMeta, JobEntryType type) {
		return delegates.jobs.newJobEntry(jobMeta, type);
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

	public JobMeta ripDB(final List<DatabaseMeta> databases, final String jobName, final RepositoryDirectory repdir, final String directory, final DatabaseMeta sourceDbInfo, final DatabaseMeta targetDbInfo, final String[] tables) {
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

	public LogWriter getLog() {
		return log;
	}

	public Repository getRepository() {
		return rep;
	}

	public void setRepository(Repository rep) {
		this.rep = rep;
	}

	public void addMenuListener(String id, Object listener, String methodName) {
		menuListeners.add(new Object[] { id, listener, methodName });
	}

	public void addTransGraph(TransMeta transMeta) {
		delegates.trans.addTransGraph(transMeta);
	}

	public boolean addSpoonBrowser(String name, String urlString) {
		return delegates.tabs.addSpoonBrowser(name, urlString);
	}

	public TransExecutionConfiguration getTransExecutionConfiguration() {
		return transExecutionConfiguration;
	}

	public Object[] messageDialogWithToggle(String dialogTitle, Object image, String message, int dialogImageType, String[] buttonLabels, int defaultIndex, String toggleMessage, boolean toggleState) {
		return GUIResource.getInstance().messageDialogWithToggle(shell, dialogTitle, (Image) image, message, dialogImageType, buttonLabels, defaultIndex, toggleMessage, toggleState);
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

	public String makeTransGraphTabName(TransMeta transMeta) {
		return delegates.tabs.makeTransGraphTabName(transMeta);
	}

	public void newConnection() {
		delegates.db.newConnection();
	}

	public void getSQL() {
		delegates.db.getSQL();
	}

	public boolean overwritePrompt(String message, String rememberText, String rememberPropertyName) {
		Object res[] = messageDialogWithToggle("Warning", null, message, Const.WARNING, new String[] { Messages.getString("System.Button.Yes"), //$NON-NLS-1$ 
				Messages.getString("System.Button.No") },//$NON-NLS-1$
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
					delegates.tabs.addSpoonBrowser(info.getName(), info.getMessage(), false);
				}
			});

		} else {
			MessageBox box = new MessageBox(shell, (info.getState() != LifeEventInfo.State.SUCCESS ? SWT.ICON_ERROR : SWT.ICON_INFORMATION) | SWT.OK);
			box.setText(info.getName());
			box.setMessage(info.getMessage());
			box.open();
		}

	}

	public void setLog() {
		LogSettingsDialog lsd = new LogSettingsDialog(shell, SWT.NONE, log, props);
		lsd.open();
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
	    		  space.setVariable(param, Const.NVL(namedParameters.getParameterValue(param), Const.NVL(namedParameters.getParameterDefault(param), Const.NVL(space.getVariable(param), ""))));
	    	  }
	    	  catch(Exception e) {
	    		  // ignore this
	    	  }
	      }
	}

}