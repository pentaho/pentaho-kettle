 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

 

package be.ibridge.kettle.chef;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.chef.wizards.RipDatabaseWizardPage1;
import be.ibridge.kettle.chef.wizards.RipDatabaseWizardPage2;
import be.ibridge.kettle.chef.wizards.RipDatabaseWizardPage3;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.GUIResource;
import be.ibridge.kettle.core.LocalVariables;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.NotePadMeta;
import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.PrintSpool;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.TransAction;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.DatabaseDialog;
import be.ibridge.kettle.core.dialog.DatabaseExplorerDialog;
import be.ibridge.kettle.core.dialog.EnterOptionsDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.dialog.SQLEditor;
import be.ibridge.kettle.core.dialog.SQLStatementsDialog;
import be.ibridge.kettle.core.dialog.Splash;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.EnvUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.wizards.createdatabase.CreateDatabaseWizard;
import be.ibridge.kettle.job.JobEntryLoader;
import be.ibridge.kettle.job.JobHopMeta;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.JobPlugin;
import be.ibridge.kettle.job.dialog.JobDialog;
import be.ibridge.kettle.job.dialog.JobLoadProgressDialog;
import be.ibridge.kettle.job.dialog.JobSaveProgressDialog;
import be.ibridge.kettle.job.entry.JobEntryCopy;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.job.entry.sql.JobEntrySQL;
import be.ibridge.kettle.job.entry.trans.JobEntryTrans;
import be.ibridge.kettle.pan.CommandLineOption;
import be.ibridge.kettle.repository.PermissionMeta;
import be.ibridge.kettle.repository.RepositoriesMeta;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.repository.RepositoryMeta;
import be.ibridge.kettle.repository.UserInfo;
import be.ibridge.kettle.repository.dialog.RepositoriesDialog;
import be.ibridge.kettle.repository.dialog.RepositoryExplorerDialog;
import be.ibridge.kettle.repository.dialog.SelectObjectDialog;
import be.ibridge.kettle.repository.dialog.UserDialog;
import be.ibridge.kettle.spoon.dialog.GetJobSQLProgressDialog;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.TransHopMeta;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.tableinput.TableInputMeta;
import be.ibridge.kettle.trans.step.tableoutput.TableOutputMeta;


/**
 * Chef is an editor for Kettle jobs.
 * 
 * @author Matt
 * @since 16-05-2003
 *
 */
public class Chef
{
    public static final String APP_NAME = Messages.getString("Chef.Application.Name"); //$NON-NLS-1$

	private LogWriter log;
	public  Display disp;
	private Shell shell;
	private boolean destroy;
	public  Props props;

	public  Repository rep;
	
	public JobMeta jobMeta;
	private ChefGraph chefgraph;

	private ChefLog    cheflog;
	private SashForm   sashform;
	private CTabFolder tabfolder;
	private CTabItem   tiTabsGraph;
	private CTabItem   tiTabsList;

	// public  Font variable_font, fixed_font, graph_font, grid_font;

	private ToolBar  tBar;
	
	private Menu     msFile;
	private MenuItem miFileSep3;
	private MenuItem miEditUndo, miEditRedo;
	
	// public  WidgetContainer widgets;
	private Listener lsNew, lsEdit, lsDupe, lsDel, lsSQL, lsCache, lsExpl;
	private SelectionAdapter lsEditDef, lsNewDef, lsEditSel;

	public static final String STRING_CONNECTIONS       = Messages.getString("Chef.Tree.Connections"); //$NON-NLS-1$
    public static final String STRING_JOBENTRIES        = Messages.getString("Chef.Tree.JobEntries"); //$NON-NLS-1$
    public static final String STRING_BASE_JOBENTRIES   = Messages.getString("Chef.Tree.BaseJobEntryTypes"); //$NON-NLS-1$
    public static final String STRING_PLUGIN_JOBENTRIES = Messages.getString("Chef.Tree.PluginJobEntries"); //$NON-NLS-1$
    
	public static final String STRING_SPECIAL      = JobEntryInterface.typeDesc[JobEntryInterface.TYPE_JOBENTRY_SPECIAL];
	
	private static final String APPL_TITLE      = Messages.getString("Chef.Application.Title"); //$NON-NLS-1$

	private static final String STRING_DEFAULT_EXT    = ".kjb"; //$NON-NLS-1$
	private static final String STRING_FILTER_EXT  [] = { "*.kjb;*.xml", "*.xml", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String STRING_FILTER_NAMES[] = { Messages.getString("Chef.FileType.KettleJobs"), Messages.getString("Chef.FileType.XMLJobs"), Messages.getString("Chef.FileType.AllJobs") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	private Tree tMain;
	private TreeItem tiSection[];
		
	public  KeyAdapter defKeys;
	
	public Chef(LogWriter log, Display d, Repository rep)
	{
		this.log=log;
		this.rep = rep;
		
		if (d!=null)
		{
			disp=d;
			destroy=false; 
		}
		else
		{
			disp=new Display();
			destroy=true;
		} 
		shell=new Shell(disp);
		shell.setText(APPL_TITLE);
		FormLayout layout = new FormLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		shell.setLayout (layout);
		
		// INIT Data structure
		jobMeta = new JobMeta(log);
		
		if (!Props.isInitialized()) 
		{
			Props.init(disp, Props.TYPE_PROPERTIES_CHEF);
		}
		props=Props.getInstance();

		// Load settings in the props
		loadSettings();
		
		defKeys = new KeyAdapter() 
			{
				public void keyPressed(KeyEvent e) 
				{
					if (e.keyCode == SWT.F5)
					{
						refreshGraph();
						refreshTree();
					}
					
					
					// ESC --> Unselect All steps
					if (e.keyCode == SWT.ESC)   { jobMeta.unselectAll(); refreshGraph(); };
					// F3 --> create database connection wizard
					if (e.keyCode == SWT.F3)    { createDatabaseWizard(); }
					// F5 --> refresh
					if (e.keyCode == SWT.F5)    { refreshGraph(); refreshTree(true); }
					// F9 --> run
					if (e.keyCode == SWT.F9)    { tabfolder.setSelection(1); cheflog.startstop(); }
					// F10 --> ripDB wizard
					if (e.keyCode == SWT.F10)    { ripDBWizard(); }
					// CTRL-A --> Select All steps
					if ((int)e.character ==  1) { jobMeta.selectAll(); refreshGraph(); };
					// CTRL-D --> Disconnect from repository
					if ((int)e.character ==  4) { closeRepository(); };
					// CTRL-E --> Explore the repository
					if ((int)e.character ==  5) { exploreRepository(); };
					// CTRL-I --> Import file from XML
					if ((int)e.character ==  9) { openFile(true); };
					// CTRL-J --> Job Dialog : edit job settings
					if ((int)e.character == 10) { setJob(); };
					// CTRL-N --> new
					if ((int)e.character == 14) { newFile();         } 
					// CTRL-O --> open
					if ((int)e.character == 15) { openFile(false);    } 
					// CTRL-P --> print
					if ((int)e.character == 16) { printFile();   } 
					// CTRL-R --> Connect to repository
					if ((int)e.character == 18) { openRepository(); };
					// CTRL-S --> save
					if ((int)e.character == 19) { saveFile();    } 
					// CTRL-Y --> save
					if ((int)e.character == 25) { redoAction();  } 
					// CTRL-Z --> save
					if ((int)e.character == 26) { undoAction();  } 

				}
			};

		
		addBar();

		FormData fdBar = new FormData();
		fdBar.left = new FormAttachment(0, 0);
		fdBar.top = new FormAttachment(0, 0);
		tBar.setLayoutData(fdBar);

		sashform = new SashForm(shell, SWT.HORIZONTAL);

		FormData fdSash = new FormData();
		fdSash.left = new FormAttachment(0, 0);
		fdSash.top = new FormAttachment(tBar, 0);
		fdSash.bottom = new FormAttachment(100, 0);
		fdSash.right  = new FormAttachment(100, 0);
		sashform.setLayoutData(fdSash);

		addMenu();
		addTree();
		addTabs();
		
		refreshTree();

		shell.setImage(chefgraph.images[JobEntryInterface.TYPE_JOBENTRY_JOB]);
		
		// In case someone dares to press the [X] in the corner ;-)
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { e.doit=quitFile(); } } );
		sashform.setWeights(new int[] {25,75});
		sashform.setVisible(true);
		
		shell.layout();

		// Set the shell size, based upon previous time...
		WindowProperty winprop = props.getScreen(APPL_TITLE);
		if (winprop!=null) winprop.setShell(shell); else shell.pack();
	}
	
	/**
	 * @param destroy The destroy to set.
	 */
	public void setDestroy(boolean destroy)
	{
		this.destroy = destroy;
	}
	
	/**
	 * @return Returns the destroy.
	 */
	public boolean isDestroy()
	{
		return destroy;
	}
	
	public void open()
	{
		shell.open();
		
		// Shared database entries to load from repository?
		loadRepositoryObjects();
		
		// Perhaps the job contains elements at startup?
		if (jobMeta.nrJobEntries()>0 || jobMeta.nrDatabases()>0 || jobMeta.nrJobHops()>0)
		{
			refreshTree(true);  // Do a complete refresh then...
		}
		
		/* WANTED: Build tips for Chef too...
		 * 
		if (props.showTips()) 
		{
			TipsDialog tip = new TipsDialog(shell, job.props);
			tip.open();
		}
		*/
	}
	
	public boolean readAndDispatch ()
	{
		return disp.readAndDispatch();
	}
	
	public void dispose()
	{
		if (destroy) disp.dispose();
	}

	public boolean isDisposed()
	{
		return disp.isDisposed();
	}

	public void sleep()
	{
		disp.sleep();
	}
	
	public void addMenu()
	{
		Menu mBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(mBar);

		/*
		 * File menu
		 * 
		 */		
		MenuItem mFile = new MenuItem(mBar, SWT.CASCADE); mFile.setText(Messages.getString("Chef.Menu.File")); //$NON-NLS-1$
		  msFile = new Menu(shell, SWT.DROP_DOWN);
		  mFile.setMenu(msFile);
		  MenuItem miFileNew    = new MenuItem(msFile, SWT.CASCADE); miFileNew.setText(Messages.getString("Chef.Menu.File.New")); //$NON-NLS-1$
		  MenuItem miFileOpen   = new MenuItem(msFile, SWT.CASCADE); miFileOpen.setText(Messages.getString("Chef.Menu.File.Open")); //$NON-NLS-1$
		  MenuItem miFileSave   = new MenuItem(msFile, SWT.CASCADE); miFileSave.setText(Messages.getString("Chef.Menu.File.Save")); //$NON-NLS-1$
		  MenuItem miFileSaveAs = new MenuItem(msFile, SWT.CASCADE); miFileSaveAs.setText(Messages.getString("Chef.Menu.File.SaveAs")); //$NON-NLS-1$
		  new MenuItem(msFile, SWT.SEPARATOR);
		  MenuItem miFilePrint  = new MenuItem(msFile, SWT.CASCADE); miFilePrint.setText(Messages.getString("Chef.Menu.File.Print")); //$NON-NLS-1$
		  new MenuItem(msFile, SWT.SEPARATOR);
		  MenuItem miFileQuit   = new MenuItem(msFile, SWT.CASCADE); miFileQuit.setText(Messages.getString("Chef.Menu.File.Quit")); //$NON-NLS-1$
 		  miFileSep3 = new MenuItem(msFile, SWT.SEPARATOR);
		  addMenuLast();

		Listener lsFileOpen       = new Listener() { public void handleEvent(Event e) { openFile(false);  } };
		Listener lsFileNew        = new Listener() { public void handleEvent(Event e) { newFile();        } };
		Listener lsFileSave       = new Listener() { public void handleEvent(Event e) { saveFile();       } };
		Listener lsFileSaveAs     = new Listener() { public void handleEvent(Event e) { saveFileAs();     } };
		Listener lsFilePrint      = new Listener() { public void handleEvent(Event e) { printFile();      } };
		Listener lsFileQuit       = new Listener() { public void handleEvent(Event e) { quitFile();       } };

		miFileOpen      .addListener (SWT.Selection, lsFileOpen   );
		miFileNew       .addListener (SWT.Selection, lsFileNew    );
		miFileSave      .addListener (SWT.Selection, lsFileSave   );
		miFileSaveAs    .addListener (SWT.Selection, lsFileSaveAs );
		miFilePrint     .addListener (SWT.Selection, lsFilePrint  );
		miFileQuit      .addListener (SWT.Selection, lsFileQuit   );

		/*
		 * Edit menu
		 * 
		 */		

		MenuItem mEdit = new MenuItem(mBar, SWT.CASCADE); mEdit.setText(Messages.getString("Chef.Menu.Edit")); //$NON-NLS-1$
		Menu msEdit = new Menu(shell, SWT.DROP_DOWN);
		mEdit.setMenu(msEdit);
		miEditUndo         = new MenuItem(msEdit, SWT.CASCADE);
		miEditRedo         = new MenuItem(msEdit, SWT.CASCADE);
		setUndoMenu();
		new MenuItem(msEdit, SWT.SEPARATOR);
		MenuItem miEditUnselectAll  = new MenuItem(msEdit, SWT.CASCADE); miEditUnselectAll.setText(Messages.getString("Chef.Menu.Edit.ClearSelection")); //$NON-NLS-1$
		MenuItem miEditSelectAll    = new MenuItem(msEdit, SWT.CASCADE); miEditSelectAll.setText(Messages.getString("Chef.Menu.Edit.SelectAllSteps")); //$NON-NLS-1$
		new MenuItem(msEdit, SWT.SEPARATOR);
		MenuItem miEditOptions      = new MenuItem(msEdit, SWT.CASCADE); miEditOptions.setText(Messages.getString("Chef.Menu.Edit.Options")); //$NON-NLS-1$

		Listener lsEditUndo        = new Listener() { public void handleEvent(Event e) { undoAction(); } };
		Listener lsEditRedo        = new Listener() { public void handleEvent(Event e) { redoAction(); } };
		Listener lsEditUnselectAll = new Listener() { public void handleEvent(Event e) { editUnselectAll(); } };
		Listener lsEditSelectAll   = new Listener() { public void handleEvent(Event e) { editSelectAll();   } };
		Listener lsEditOptions     = new Listener() { public void handleEvent(Event e) { editOptions();     } };

	    miEditUndo       .addListener(SWT.Selection, lsEditUndo);
	    miEditRedo       .addListener(SWT.Selection, lsEditRedo);
	    miEditUnselectAll.addListener(SWT.Selection, lsEditUnselectAll);
	    miEditSelectAll  .addListener(SWT.Selection, lsEditSelectAll);
	    miEditOptions    .addListener(SWT.Selection, lsEditOptions);

		// main Repository menu...
	    MenuItem mRep = new MenuItem(mBar, SWT.CASCADE); mRep.setText(Messages.getString("Chef.Menu.Repository")); //$NON-NLS-1$
		  Menu msRep = new Menu(shell, SWT.DROP_DOWN);
		  mRep.setMenu(msRep);
		  MenuItem miRepConnect    = new MenuItem(msRep, SWT.CASCADE); miRepConnect.setText(Messages.getString("Chef.Menu.Repository.Connect")); //$NON-NLS-1$
		  MenuItem miRepDisconnect = new MenuItem(msRep, SWT.CASCADE); miRepDisconnect.setText(Messages.getString("Chef.Menu.Repository.Disconnect")); //$NON-NLS-1$
		  MenuItem miRepExplore    = new MenuItem(msRep, SWT.CASCADE); miRepExplore.setText(Messages.getString("Chef.Menu.Repository.Explore")); //$NON-NLS-1$
		  new MenuItem(msRep, SWT.SEPARATOR);
		  MenuItem miRepUser       = new MenuItem(msRep, SWT.CASCADE); miRepUser.setText(Messages.getString("Chef.Menu.Repository.EditUser")); //$NON-NLS-1$
		
		  Listener lsRepConnect     = new Listener() { public void handleEvent(Event e) { openRepository();    } };
		  Listener lsRepDisconnect  = new Listener() { public void handleEvent(Event e) { closeRepository();   } };
		  Listener lsRepExplore     = new Listener() { public void handleEvent(Event e) { exploreRepository(); } };
		  Listener lsRepUser        = new Listener() { public void handleEvent(Event e) { editRepositoryUser();} };
		
		miRepConnect    .addListener (SWT.Selection, lsRepConnect   );
		miRepDisconnect .addListener (SWT.Selection, lsRepDisconnect);
		miRepExplore    .addListener (SWT.Selection, lsRepExplore   );
		miRepUser       .addListener (SWT.Selection, lsRepUser      );

		/*
		 * Job menu
		 * 
		 */		

		MenuItem mJob = new MenuItem(mBar, SWT.CASCADE); 
		mJob.setText(Messages.getString("Chef.Menu.Job")); //$NON-NLS-1$
		Menu msJob = new Menu(shell, SWT.DROP_DOWN);
		mJob.setMenu(msJob);
		MenuItem miJobRun           = new MenuItem(msJob, SWT.CASCADE);   miJobRun.setText(Messages.getString("Chef.Menu.Job.Run")); //$NON-NLS-1$
		new MenuItem(msJob, SWT.SEPARATOR);
		MenuItem miJobCopy = new MenuItem(msJob, SWT.CASCADE); 		      miJobCopy.setText(Messages.getString("Chef.Menu.Job.CopyToClipboard")); //$NON-NLS-1$
		new MenuItem(msJob, SWT.SEPARATOR);
		MenuItem miJobInfo          = new MenuItem(msJob, SWT.CASCADE);   miJobInfo.setText(Messages.getString("Chef.Menu.Job.Settings")); //$NON-NLS-1$
		
		Listener lsJobInfo        = new Listener() { public void handleEvent(Event e) { setJob();  } };
		miJobInfo.addListener (SWT.Selection, lsJobInfo );
		Listener lsJobCopy        = new Listener() { public void handleEvent(Event e) { toClipboard(XMLHandler.getXMLHeader() + jobMeta.getXML()); } };
		miJobCopy.addListener(SWT.Selection, lsJobCopy );
		
		
		// Wizard menu
		MenuItem mWizard = new MenuItem(mBar, SWT.CASCADE); mWizard.setText(Messages.getString("Chef.Menu.Wizard")); //$NON-NLS-1$
		  Menu msWizard = new Menu(shell, SWT.DROP_DOWN );
		  mWizard.setMenu(msWizard);

		  MenuItem miWizardNewConnection = new MenuItem(msWizard, SWT.CASCADE); 
		  miWizardNewConnection.setText(Messages.getString("Chef.Menu.Wizard.CreateDatabaseConnection")); //$NON-NLS-1$
		  Listener lsWizardNewConnection= new Listener() { public void handleEvent(Event e) { createDatabaseWizard();  } };
		  miWizardNewConnection.addListener(SWT.Selection, lsWizardNewConnection);

		  MenuItem miWizardRipDatabase = new MenuItem(msWizard, SWT.CASCADE); 
		  miWizardRipDatabase.setText(Messages.getString("Chef.Menu.Wizard.CopyTables")); //$NON-NLS-1$
		  Listener lsWizardRipDatabase= new Listener() { public void handleEvent(Event e) { ripDBWizard();  } };
		  miWizardRipDatabase.addListener(SWT.Selection, lsWizardRipDatabase);

		/*
		 * Help menu
		 * 
		 */		

		MenuItem mHelp = new MenuItem(mBar, SWT.CASCADE); mHelp.setText(Messages.getString("Chef.Menu.Help")); //$NON-NLS-1$
		  Menu msHelp = new Menu(shell, SWT.DROP_DOWN);
		  mHelp.setMenu(msHelp);
		  MenuItem miHelpAbout       = new MenuItem(msHelp, SWT.CASCADE); miHelpAbout.setText(Messages.getString("Chef.Menu.Help.About")); //$NON-NLS-1$
		
		  Listener lsHelpAbout		 = new Listener() { public void handleEvent(Event e) { helpAbout();      } };
		  miHelpAbout     .addListener (SWT.Selection, lsHelpAbout  );
	}
	
	private void addMenuLast()
	{
		int idx = msFile.indexOf(miFileSep3);
		int max = msFile.getItemCount();
		
		// Remove everything until end... 
		for (int i=max-1;i>idx;i--)
		{
			MenuItem mi = msFile.getItem(i);
			mi.dispose();
		}
		
		// Previously loaded files...
		String  lf[] = props.getLastFiles();
		String  ld[] = props.getLastDirs();
		boolean lt[] = props.getLastTypes();
		String  lr[] = props.getLastRepositories();
		
		for (int i=0;i<lf.length;i++)
		{
		  MenuItem miFileLast = new MenuItem(msFile, SWT.CASCADE);
		  char chr  = (char)('1'+i );
		  int accel =  SWT.CTRL | chr;
		  String repository = ( lr[i]!=null && lr[i].length()>0 ) ? ( "["+lr[i]+"] " ) : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		  String filename = RepositoryDirectory.DIRECTORY_SEPARATOR + lf[i];
		  if (!lt[i]) filename = lf[i];
		  
		  if (!ld[i].equals(RepositoryDirectory.DIRECTORY_SEPARATOR))
		  {
		  	filename=ld[i]+filename;
		  }
		  if (i<9)
		  {
		  	miFileLast.setAccelerator(accel);
			miFileLast.setText("&"+chr+"  "+repository+filename+ "\tCTRL-"+chr); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		  }
		  else
		  {
		  	miFileLast.setText("   "+repository+filename); //$NON-NLS-1$
		  }
		  
		  final String  fn = lf[i];   // filename
		  final String  fd = ld[i];   // Repository directory ...
		  final boolean ft = lt[i];   // type: true=repository, false=file
		  final String  fr = lr[i];   // repository name
		  
		  Listener lsFileLast = new Listener() 
			  { 
				  public void handleEvent(Event e) 
				  {
				      if (showChangedWarning())
				      {
				      	// If the file comes from a repository and it's not the same as 
				      	// the one we're connected to, ask for a username/password!
				      	// 
				      	boolean noRepository=false;
				      	if (ft && (rep==null || !rep.getRepositoryInfo().getName().equalsIgnoreCase(fr) ))
				      	{
				      		int perms[] = new int[] { PermissionMeta.TYPE_PERMISSION_JOB };
				      		RepositoriesDialog rd = new RepositoriesDialog(disp, SWT.NONE, perms, APP_NAME);
				      		rd.setRepositoryName(fr);
				      		if (rd.open())
				      		{
				      			//	Close the previous connection...
				      			if (rep!=null) rep.disconnect();
				      			rep = new Repository(log, rd.getRepository(), rd.getUser());
				      			if (!rep.connect(APP_NAME))
				      			{
				      				rep=null;
				      				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
									mb.setMessage(Messages.getString("Chef.Dialog.UnableToConnectToRepository.Message")); //$NON-NLS-1$
									mb.setText(Messages.getString("Chef.Dialog.UnableToConnectToRepository.Title")); //$NON-NLS-1$
									mb.open();
				      			}
				      		}
				      		else
				      		{
				      			noRepository=true;
				      		}
				      	}

				      	if (ft)
				      	{
				      		if (!noRepository && rep!=null && rep.getRepositoryInfo().getName().equalsIgnoreCase(fr))
				      		{
			      				// OK, we're connected to the new repository...
			      				// Load the job...
				      			RepositoryDirectory fdRepdir = rep.getDirectoryTree().findDirectory(fd);
			      				try
								{
			      					if (fdRepdir!=null)
			      					{
			      						jobMeta = new JobMeta(log, rep, fn, fdRepdir);
			      					}
			      					else
			      					{
			      						throw new KettleException(Messages.getString("Chef.Exception.RepositoryDirectoryDoesNotExist")+fd); //$NON-NLS-1$
			      					}
								}
			      				catch(KettleException ke)
								{
									jobMeta.clear();
									new ErrorDialog(shell, props, Messages.getString("Chef.ErrorDialog.ErrorLoadingJob.Title"), Messages.getString("Chef.ErrorDialog.ErrorLoadingJob.Message"), ke); //$NON-NLS-1$ //$NON-NLS-2$
								}
				      		}
				      		else
				      		{
								jobMeta.clear();
			      				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
								mb.setMessage(Messages.getString("Chef.Dialog.CanNotLoadJobConnectToRepositoryFirst.Message")); //$NON-NLS-1$
								mb.setText(Messages.getString("Chef.Dialog.CanNotLoadJobConnectToRepositoryFirst.Title")); //$NON-NLS-1$
								mb.open();
				      		}
				      	}
				      	else
				      		// Load from XML!
				      	{
				      		try
							{
				      			jobMeta = new JobMeta(log, fn, rep);
							}
				      		catch(KettleException ke)
							{
								jobMeta.clear();
								new ErrorDialog(shell, props, Messages.getString("Chef.ErrorDialog.UnableToLoadJobFromXML.Title"), Messages.getString("Chef.ErrorDialog.UnableToLoadJobFromXML.Message"), ke); //$NON-NLS-1$ //$NON-NLS-2$
							}
				      	}
				      	
				      	refreshTree(true);
						refreshGraph();

					  }
				  } 
			  };
			  
		  miFileLast.addListener(SWT.Selection, lsFileLast);
		}
	}

	private void addBar()
	{
		tBar = new ToolBar(shell, SWT.HORIZONTAL | SWT.FLAT );
		//tBar.setSize(200, 20);
		final ToolItem tiFileNew = new ToolItem(tBar, SWT.PUSH);
		final Image imFileNew = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"new.png"));  //$NON-NLS-1$
		tiFileNew.setImage(imFileNew);
		tiFileNew.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newFile(); }});
		tiFileNew.setToolTipText(Messages.getString("Chef.ToolBarButton.NewFile.ToolTip")); //$NON-NLS-1$

		final ToolItem tiFileOpen = new ToolItem(tBar, SWT.PUSH);
		final Image imFileOpen = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"open.png"));  //$NON-NLS-1$
		tiFileOpen.setImage(imFileOpen);
		tiFileOpen.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { openFile(false); }});
		tiFileOpen.setToolTipText(Messages.getString("Chef.ToolBarButton.OpenFile.ToolTip")); //$NON-NLS-1$

		final ToolItem tiFileSave = new ToolItem(tBar, SWT.PUSH);
		final Image imFileSave = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"save.png"));  //$NON-NLS-1$
		tiFileSave.setImage(imFileSave);
		tiFileSave.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { saveFile(); }});
		tiFileSave.setToolTipText(Messages.getString("Chef.ToolBarButton.SaveCurrentFile.ToolTip")); //$NON-NLS-1$

		final ToolItem tiFileSaveAs = new ToolItem(tBar, SWT.PUSH);
		final Image imFileSaveAs = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"saveas.png"));  //$NON-NLS-1$
		tiFileSaveAs.setImage(imFileSaveAs);
		tiFileSaveAs.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { saveFileAs(); }});
		tiFileSaveAs.setToolTipText(Messages.getString("Chef.ToolBarButton.SaveFileAs.ToolTip")); //$NON-NLS-1$

		new ToolItem(tBar, SWT.SEPARATOR);
		final ToolItem tiFilePrint = new ToolItem(tBar, SWT.PUSH);
		final Image imFilePrint = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"print.png"));  //$NON-NLS-1$
		tiFilePrint.setImage(imFilePrint);
		tiFilePrint.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { printFile(); }});
		tiFilePrint.setToolTipText(Messages.getString("Chef.ToolBarButton.Print.ToolTip")); //$NON-NLS-1$

		new ToolItem(tBar, SWT.SEPARATOR);
		final ToolItem tiFileRun = new ToolItem(tBar, SWT.PUSH);
		final Image imFileRun = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"run.png"));  //$NON-NLS-1$
		tiFileRun.setImage(imFileRun);
		tiFileRun.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { tabfolder.setSelection(1); cheflog.startstop(); }});
		tiFileRun.setToolTipText(Messages.getString("Chef.ToolBarButton.RunThisJob.ToolTip")); //$NON-NLS-1$

        new ToolItem(tBar, SWT.SEPARATOR);
        final ToolItem tiSQL = new ToolItem(tBar, SWT.PUSH);
        final Image imSQL = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"SQLbutton.png"));  //$NON-NLS-1$
        tiSQL.setImage(imSQL);
        tiSQL.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getSQL(); }});
        tiSQL.setToolTipText(Messages.getString("Chef.ToolBarButton.GenerateSQL.ToolTip")); //$NON-NLS-1$

		tBar.addDisposeListener(new DisposeListener() 
			{
				public void widgetDisposed(DisposeEvent e) 
				{
					imFileNew.dispose();
					imFileOpen.dispose();
					imFileSave.dispose();
					imFileSaveAs.dispose();
				}
			}
		);
		tBar.addKeyListener(defKeys);
		tBar.pack();
	}
	
    /**
     * Get & show the SQL required to run the loaded job entry...
     *
     */
    public void getSQL()
    {
        GetJobSQLProgressDialog pspd = new GetJobSQLProgressDialog(shell, jobMeta, rep);
        ArrayList stats = pspd.open();
        if (stats!=null) // null means error, but we already displayed the error
        {
            if (stats.size()>0)
            {
                SQLStatementsDialog ssd = new SQLStatementsDialog(shell, SWT.NONE, stats);
                ssd.open();
            }
            else
            {
                MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
                mb.setMessage(Messages.getString("Chef.Dialog.NoSQLNeeded.Message")); //$NON-NLS-1$
                mb.setText(Messages.getString("Chef.Dialog.NoSQLNeeded.Title")); //$NON-NLS-1$
                mb.open();
            }
        }
    }
    

    private void addTree()
	{
		SashForm leftsplit = new SashForm(sashform, SWT.VERTICAL);
		leftsplit.setLayout(new FillLayout());
		
		// CSH: Connections, Steps and Transformations
		Composite cCSH = new Composite(leftsplit, SWT.NONE);
		cCSH.setLayout(new FillLayout());
			
		leftsplit.setWeights(new int[] {100});
		
		// Now set up the main CSH tree
		tMain = new Tree(cCSH, SWT.MULTI | SWT.BORDER);
		
        // Add the connections subtree
        //
        TreeItem tiConnections = new TreeItem(tMain, SWT.NONE);
        tiConnections.setText(STRING_CONNECTIONS);

        // Job-entries
        // 
        TreeItem tiEntries = new TreeItem(tMain, SWT.NONE);
        tiEntries.setText(STRING_JOBENTRIES);

        // Job entry base type
        // 
        TreeItem tiBaseEntries = new TreeItem(tMain, SWT.NONE);
        tiBaseEntries.setText(STRING_BASE_JOBENTRIES);
        
        // Set the entry types on it using the JobEntryLoader
        JobEntryLoader jobEntryLoader = JobEntryLoader.getInstance();
        JobPlugin baseJobEntries[] = jobEntryLoader.getJobEntriesWithType(JobPlugin.TYPE_NATIVE);
        for (int i=0;i<baseJobEntries.length;i++)
        {
            JobPlugin plugin = baseJobEntries[i];
            if (!plugin.getID().equals("SPECIAL")) //$NON-NLS-1$
            {
               TreeItem tiBase = new TreeItem(tiBaseEntries, SWT.NONE);
               tiBase.setText(baseJobEntries[i].getDescription());
            }
        }

        // Job entry base type
        // 
        TreeItem tiPluginEntries = new TreeItem(tMain, SWT.NONE);
        tiPluginEntries.setText(STRING_PLUGIN_JOBENTRIES);
        
        // Set the entry types on it using the JobEntryLoader
        JobPlugin pluginJobEntries[] = jobEntryLoader.getJobEntriesWithType(JobPlugin.TYPE_PLUGIN);
        for (int i=0;i<pluginJobEntries.length;i++)
        {
           TreeItem tiPlugin = new TreeItem(tiPluginEntries, SWT.NONE);
           tiPlugin.setText(pluginJobEntries[i].getDescription());
        }
        
        /*
		tiSection = new TreeItem[JobEntryInterface.type_desc.length];
        tiSection[0] = new TreeItem(tMain, SWT.NONE);
		tiSection[0].setText(STRING_CONNECTIONS);
		for (int i=1;i<JobEntryInterface.type_desc.length;i++)
		{
			tiSection[i] = new TreeItem(tMain, SWT.NONE);
			tiSection[i].setText(JobEntryInterface.type_desc_long[i]);
		}
        */
		
		props.setLook( tMain );

		// Popup-menu selection
		lsNew    = new Listener() { public void handleEvent(Event e) { newSelected();  } };  
		lsEdit   = new Listener() { public void handleEvent(Event e) { editSelected(); } };
		lsDupe   = new Listener() { public void handleEvent(Event e) { dupeSelected(); } };
		lsDel    = new Listener() { public void handleEvent(Event e) { delSelected();  } };
		lsSQL    = new Listener() { public void handleEvent(Event e) { sqlSelected();  } };
		lsCache  = new Listener() { public void handleEvent(Event e) { clearDBCache(); } };
		lsExpl   = new Listener() { public void handleEvent(Event e) { exploreDB();    } };

		// Default selection (double-click, enter)
		lsEditDef = new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e){ editSelected(); } };
		lsNewDef  = new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e){ newSelected();  } };
		lsEditSel = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { setMenu(e); } };
		
		// Add all the listeners... 
		tMain.addSelectionListener(lsEditDef); // double click somewhere in the tree...
		tMain.addSelectionListener(lsNewDef); // double click somewhere in the tree...
		tMain.addSelectionListener(lsEditSel);
		
		// Drag & Drop for steps
		Transfer[] ttypes = new Transfer[] {TextTransfer.getInstance() };
		
		DragSource ddSource = new DragSource(tMain, DND.DROP_MOVE | DND.DROP_COPY);
		ddSource.setTransfer(ttypes);
		ddSource.addDragListener(new DragSourceListener() 
			{
				public void dragStart(DragSourceEvent event){ }
	
				public void dragSetData(DragSourceEvent event) 
				{
					TreeItem ti[] = tMain.getSelection();
					String data = new String();
					for (int i=0;i<ti.length;i++) data+=ti[i].getText()+Const.CR;
					event.data = data;
				}
	
				public void dragFinished(DragSourceEvent event) {}
			}
		);
		
		// Keyboard shortcuts!
		tMain.addKeyListener( defKeys );
	}
	
	private void setMenu(SelectionEvent e)
	{
		TreeItem ti = (TreeItem)e.item;
		log.logDebug(toString(), Messages.getString("Chef.Log.ClickedOn")+ti.getText()); //$NON-NLS-1$
		
		Menu mCSH = new Menu(shell, SWT.POP_UP);

		// Find the level we clicked on: Top level (only NEW in the menu) or below (edit, insert, ...)
		TreeItem parent = ti.getParentItem();
		if (parent==null) // Top level
		{
			MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText(Messages.getString("Chef.TreeMenu.New")); //$NON-NLS-1$
			miNew.addListener( SWT.Selection, lsNew );
		}
		else
		{
			String section = parent.getText();
			if (section.equalsIgnoreCase(STRING_CONNECTIONS))
			{
				MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText(Messages.getString("Chef.TreeMenu.Connection.New")); //$NON-NLS-1$
				MenuItem miEdit = new MenuItem(mCSH, SWT.PUSH); miEdit.setText(Messages.getString("Chef.TreeMenu.Connection.Edit")); //$NON-NLS-1$
				MenuItem miDupe = new MenuItem(mCSH, SWT.PUSH); miDupe.setText(Messages.getString("Chef.TreeMenu.Connection.Duplicate")); //$NON-NLS-1$
				MenuItem miDel  = new MenuItem(mCSH, SWT.PUSH); miDel.setText(Messages.getString("Chef.TreeMenu.Connection.Delete")); //$NON-NLS-1$
				new MenuItem(mCSH, SWT.SEPARATOR);
				MenuItem miSQL  = new MenuItem(mCSH, SWT.PUSH); miSQL.setText(Messages.getString("Chef.TreeMenu.Connection.SQLEditor")); //$NON-NLS-1$
				MenuItem miCache= new MenuItem(mCSH, SWT.PUSH); miCache.setText(Messages.getString("Chef.TreeMenu.Connection.ClearDBCache")+ti.getText()); //$NON-NLS-1$
				new MenuItem(mCSH, SWT.SEPARATOR);
				MenuItem miExpl = new MenuItem(mCSH, SWT.PUSH); miExpl.setText(Messages.getString("Chef.TreeMenu.Connection.Explore")); //$NON-NLS-1$
				miNew.addListener( SWT.Selection, lsNew );   
				miEdit.addListener(SWT.Selection, lsEdit );
				miDupe.addListener(SWT.Selection, lsDupe );
				miDel.addListener(SWT.Selection, lsDel );
				miSQL.addListener(SWT.Selection, lsSQL );
				miCache.addListener(SWT.Selection, lsCache);
				miExpl.addListener(SWT.Selection, lsExpl);
			}
			else
			if (!ti.getText().equalsIgnoreCase(STRING_SPECIAL) && !section.equalsIgnoreCase(STRING_SPECIAL))
			{
				MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText(Messages.getString("Chef.TreeMenu.JobEntry.New")); //$NON-NLS-1$
				MenuItem miEdit = new MenuItem(mCSH, SWT.PUSH); miEdit.setText(Messages.getString("Chef.TreeMenu.JobEntry.Edit")); //$NON-NLS-1$
				MenuItem miDupe = new MenuItem(mCSH, SWT.PUSH); miDupe.setText(Messages.getString("Chef.TreeMenu.JobEntry.Duplicate")); //$NON-NLS-1$
				MenuItem miDel  = new MenuItem(mCSH, SWT.PUSH); miDel.setText(Messages.getString("Chef.TreeMenu.JobEntry.Delete")); //$NON-NLS-1$
				miNew.addListener( SWT.Selection, lsNew );   
				miEdit.addListener(SWT.Selection, lsEdit );
				miDupe.addListener(SWT.Selection, lsDupe );
				miDel.addListener(SWT.Selection, lsDel );
			}
		}
		tMain.setMenu(mCSH);
	}
	
	private void addTabs()
	{
		Composite child = new Composite(sashform, SWT.BORDER );
		child.setLayout(new FillLayout());
		
		tabfolder= new CTabFolder(child, SWT.BORDER);
		props.setLook(tabfolder, Props.WIDGET_STYLE_TAB);

		tiTabsGraph = new CTabItem(tabfolder, SWT.NONE); tiTabsGraph.setText(Messages.getString("Chef.Tab.GraphicalView.Text")); //$NON-NLS-1$
		tiTabsGraph.setToolTipText(Messages.getString("Chef.Tab.GraphicalView.ToolTip")); //$NON-NLS-1$
		tiTabsList  = new CTabItem(tabfolder, SWT.NULL); tiTabsList.setText(Messages.getString("Chef.Tab.LogView.Text")); //$NON-NLS-1$
		tiTabsList.setToolTipText(Messages.getString("Chef.Tab.LogView.ToolTip")); //$NON-NLS-1$
		
		chefgraph = new ChefGraph(tabfolder, SWT.V_SCROLL | SWT.H_SCROLL | SWT.NO_BACKGROUND, this);
		cheflog   = new ChefLog(tabfolder, SWT.NONE, this);
				
		tiTabsGraph.setControl(chefgraph);
		tiTabsList.setControl(cheflog);
		
		tabfolder.setSelection(0);
				
		sashform.addKeyListener(defKeys);
	}

	public String getRepositoryName()
	{
		return rep==null?"":rep.getName(); //$NON-NLS-1$
	}
		
	public void newSelected() // Double click in tree
	{
		// Determine what menu we selected from...
		TreeItem ti[] = tMain.getSelection();
					
		if (ti.length>=1)
		{
			String name = ti[0].getText();
			TreeItem parent = ti[0].getParentItem();
			if (parent == null)  // Double click on parent: new entry!
			{
				if (name.equalsIgnoreCase(STRING_CONNECTIONS))
				{
					newConnection();
				}
				else
				{
					newChefGraphEntry(name, true);
				}
			}
			else  // Double-click on entry: edit it!
			{
				// This is handled separately in editSelected through listener lsDef.
			}
		}
	}
	
	public void editSelected()
	{
		// Determine what menu we selected from...
		
		TreeItem ti[] = tMain.getSelection();					
		if (ti.length==1)
		{
			String name = ti[0].getText();
			TreeItem parent = ti[0].getParentItem();
			if (parent != null)
			{
				if (parent.getText().equalsIgnoreCase(STRING_CONNECTIONS))
				{
					editConnection(name);
				}
				else
				{
					JobEntryCopy getjge = jobMeta.findJobEntry(name, 0, true);
					if (getjge!=null)
					{
						editChefGraphEntry(getjge);
					}
				}
			}
		}
	}

	public void dupeSelected()
	{
		// Determine what menu we selected from...

		TreeItem ti[] = tMain.getSelection();
        
		// Then call editConnection or editStep or editTrans
		if (ti.length==1)
		{
			String name = ti[0].getText();
			TreeItem parent = ti[0].getParentItem();
			if (parent != null)
			{
				dupeChefGraphEntry(name);
			} 
		}
	}
	
	public void delSelected()
	{
		// Determine what menu we selected from...
		int i;
		
		TreeItem ti[] = tMain.getSelection();
		String name[] = new String[ti.length];
		TreeItem parent[] = new TreeItem[ti.length];
		
		for (i=0;i<ti.length;i++) 
		{
			name[i] = ti[i].getText();
			parent[i] = ti[i].getParentItem();
		} 
		
		// Then call editConnection or editStep or editTrans
		for (i=name.length-1;i>=0;i--)
		{
			log.logDebug(toString(), Messages.getString("Chef.Log.TryingToDelete")+i+"/"+(ti.length-1)+" : "+name[i]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (parent[i] != null)
			{
                String type = parent[i].getText();
                if (type.equalsIgnoreCase(STRING_CONNECTIONS))
                {
                    delConnection(name[i]);
                }
                if (type.equalsIgnoreCase(STRING_JOBENTRIES))
                {
                    deleteChefGraphEntry(name[i]);
                }
			} 
		}
	}

	public void sqlSelected()
	{
		// Determine what menu we selected from...
		int i;
		
		TreeItem ti[] = tMain.getSelection();
		for (i=0;i<ti.length;i++) 
		{
			String name     = ti[i].getText();
			TreeItem parent = ti[i].getParentItem();
			String type     = parent.getText();
			if (type.equalsIgnoreCase(STRING_CONNECTIONS))
			{
				DatabaseMeta ci = jobMeta.findDatabase(name);
				SQLEditor sql = new SQLEditor(shell, SWT.NONE, ci, jobMeta.dbcache, ""); //$NON-NLS-1$
				sql.open();
			}
			
		} 
	}

	public void editConnection(String name)
	{
		DatabaseMeta db = jobMeta.findDatabase(name);
		if (db!=null)
		{
			DatabaseMeta before = (DatabaseMeta)db.clone();
			
			DatabaseDialog con = new DatabaseDialog(shell, SWT.NONE, log, db, props);
            con.setDatabases(jobMeta.getDatabases());
			String newname = con.open(); 
			if (newname != null)  // null: CANCEL
			{
				// Store undo/redo information
				DatabaseMeta after = (DatabaseMeta)db.clone();
				addUndoChange(new DatabaseMeta[] { before }, new DatabaseMeta[] { after }, new int[] { jobMeta.indexOfDatabase(db) } );
				
				saveConnection(db);
				
				// It's saved, remove the changed flag
				db.setChanged(false);
				
				if (!name.equalsIgnoreCase(newname)) refreshTree();
			}
		}
		setShellText();
	}

	public void dupeConnection(String name)
	{
		int i, pos=0;
		DatabaseMeta db = null, look=null;
						
		for (i=0;i<jobMeta.nrDatabases() && db==null;i++)
		{
			look = jobMeta.getDatabase(i);
			if (look.getName().equalsIgnoreCase(name))
			{
				db=look;
				pos=i;
			}
		}
		if (db!=null)
		{
			DatabaseMeta newdb = (DatabaseMeta)db.clone();
			String dupename = Messages.getString("Chef.JobEntryName.Duplicate.Prefix")+name;  //$NON-NLS-1$
			newdb.setName(dupename);
			jobMeta.addDatabase(pos+1, newdb);
			refreshTree();

			DatabaseDialog con = new DatabaseDialog(shell, SWT.NONE, log, newdb, props);
			String newname = con.open(); 
			if (newname != null)  // null: CANCEL
			{
				jobMeta.removeDatabase(pos+1);
				jobMeta.addDatabase(pos+1, newdb);
				
				if (!newname.equalsIgnoreCase(dupename)) refreshTree();
			}
			else
			{
				addUndoNew(new DatabaseMeta[] { (DatabaseMeta)db.clone() }, new int[] { jobMeta.indexOfDatabase(db) });
				
				saveConnection(db);				
			}
		}
	}

       /**
     * Delete a database connection
     * @param name The name of the database connection.
     */
    public void delConnection(String name)
    {
        DatabaseMeta db = jobMeta.findDatabase(name);
        int pos = jobMeta.indexOfDatabase(db);                
        if (db!=null)
        {
            boolean worked=false;
            
            // delete from repository?
            if (rep!=null)
            {
                if (!rep.getUserInfo().isReadonly())
                {
                    try
                    {
                        long id_database = rep.getDatabaseID(db.getName());
                        rep.delDatabase(id_database);
            
                        worked=true;
                    }
                    catch(KettleDatabaseException dbe)
                    {
                        
                        new ErrorDialog(shell, props, Messages.getString("Chef.ErrorDialog.ErrorDeletingConnection.Title"), Messages.getString("Chef.ErrorDialog.ErrorDeletingConnection.Message")+db+Messages.getString("Chef.ErrorDialog.ErrorDeletingConnection.Message2"), dbe); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                }
                else
                {
                    new ErrorDialog(shell, props, Messages.getString("Chef.ErrorDialog.DeleteConnectionUserIsReadOnly.Title"),  Messages.getString("Chef.ErrorDialog.DeleteConnectionUserIsReadOnly.Message")+db+Messages.getString("Chef.ErrorDialog.DeleteConnectionUserIsReadOnly.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }

            if (rep==null || worked)
            {
                addUndoDelete(new DatabaseMeta[] { (DatabaseMeta)db.clone() }, new int[] { pos });
                jobMeta.removeDatabase(pos);
            }

            refreshTree();
        }
        setShellText();
    }

		
	public void newJobHop(JobEntryCopy fr, JobEntryCopy to)
	{
		log.logBasic(toString(), Messages.getString("Chef.Log.NewJobHop")+fr.getName()+", "+to.getName()+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JobHopMeta hi = new JobHopMeta(fr, to);
		jobMeta.addJobHop(hi);
		addUndoNew(new JobHopMeta[] {hi}, new int[] { jobMeta.indexOfJobHop(hi)} );
		refreshGraph();
		refreshTree();
	}
	
	public boolean showChangedWarning()
	{
		boolean answer = true;
		if (jobMeta.hasChanged())
		{
			MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_WARNING );
			mb.setMessage(Messages.getString("Chef.Dialog.OpenNewFileHasChanged.Message")); //$NON-NLS-1$
			mb.setText(Messages.getString("Chef.Dialog.OpenNewFileHasChanged.Title")); //$NON-NLS-1$
			answer = mb.open()==SWT.YES;
		}
		return answer;
	}

	public void openRepository()
	{
		int perms[] = new int[] { PermissionMeta.TYPE_PERMISSION_TRANSFORMATION };
		RepositoriesDialog rd = new RepositoriesDialog(disp, SWT.NONE, perms, APP_NAME);
		rd.getShell().setImage(GUIResource.getInstance().getImageSpoon());
		if (rd.open())
		{
			// Close previous repository...
			
			if (rep!=null) rep.disconnect();
			
			rep = new Repository(log, rd.getRepository(), rd.getUser());
			if (!rep.connect(Messages.getString("Chef.AppName.RepositoryConnect"))) //$NON-NLS-1$
			{
				rep=null;
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage(Messages.getString("Chef.Dialog.ErrorConnectingToTheRepository.Message1")+Const.CR+Messages.getString("Chef.Dialog.ErrorConnectingToTheRepository.Message2")); //$NON-NLS-1$ //$NON-NLS-2$
				mb.setText(Messages.getString("Chef.Dialog.ErrorConnectingToTheRepository.Title")); //$NON-NLS-1$
				mb.open();
			}
			
            // Set for the existing databases, the ID's at -1!
            for (int i=0;i<jobMeta.nrDatabases();i++) 
            {
                jobMeta.getDatabase(i).setID(-1L);
            }
            // Set for the existing transformation the ID at -1!
            jobMeta.setID(-1L);
            
            ArrayList oldDatabases = jobMeta.getDatabases(); // Save the list.
            
            // In order to re-match the databases on name (not content), we need to load the databases from the new repository.
            // NOTE: for purposes such as DEVELOP - TEST - PRODUCTION sycles.
            loadRepositoryObjects();
            
            // Then we need to re-match the databases at save time...
            for (int i=0;i<oldDatabases.size();i++)
            {
                DatabaseMeta oldDatabase = (DatabaseMeta) oldDatabases.get(i);
                DatabaseMeta newDatabase = Const.findDatabase(jobMeta.getDatabases(), oldDatabase.getName());
                
                // If it exists, change the settings...
                if (newDatabase!=null)
                {
                    // A database connection with the same name exists in the new repository.
                    // Change the old connections to reflect the settings in the new repository 
                    oldDatabase.setDatabaseInterface(newDatabase.getDatabaseInterface());
                }
                else
                {
                    // The old database is not present in the new repository: simply add it to the list.
                    // When the transformation gets saved, it will be added to the repository.
                    jobMeta.addDatabase(oldDatabase);
                }
            }
            
            // For the existing transformation, change the directory too:
            // Try to find the same directory in the new repository...
            RepositoryDirectory redi = rep.getDirectoryTree().findDirectory(jobMeta.getDirectory().getPath());
            if (redi!=null)
            {
                jobMeta.setDirectory(redi);
            }
            else
            {
                jobMeta.setDirectory(rep.getDirectoryTree()); // the root is the default!
            }

            refreshTree();
			
			setShellText();
		}
		else
		{
			// Not cancelled? --> Clear repository...
			if (!rd.isCancelled())
			{
				closeRepository();
			}
		}
	}
	
	public void readDatabases()
	{
		jobMeta.readDatabases(rep);
	}


	public void exploreRepository()
	{
		RepositoryExplorerDialog erd = new RepositoryExplorerDialog(shell, SWT.NONE, rep, rep.getUserInfo());
		erd.open();
	}
	
	public void closeRepository()
	{
		if (showChangedWarning() && rep!=null)
		{
			rep.disconnect();
			rep = null;
		}
		setShellText();
	}
	
	public void editRepositoryUser()
	{
		if (rep!=null)
		{
			UserInfo userinfo = rep.getUserInfo();
			UserDialog ud = new UserDialog(shell, SWT.NONE, log, props, rep, userinfo);
			UserInfo ui = ud.open();
			if (!userinfo.isReadonly())
			{
				if (ui!=null)
				{
					try
					{
						ui.saveRep(rep);
					}
					catch(KettleException e)
					{
						MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
						mb.setMessage(Messages.getString("Chef.Dialog.UnableToChangeUser.Message")+Const.CR+e.getMessage()); //$NON-NLS-1$
						mb.setText(Messages.getString("Chef.Dialog.UnableToChangeUser.Title")); //$NON-NLS-1$
						mb.open();
					}
			 	}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
				mb.setMessage(Messages.getString("Chef.Dialog.NotAllowedToChangeUser.Message")); //$NON-NLS-1$
				mb.setText(Messages.getString("Chef.Dialog.NotAllowedToChangeUser.Title")); //$NON-NLS-1$
				mb.open();
			}		
		}
	}

	public void clearDBCache()
	{
		// Determine what menu we selected from...
	
		TreeItem ti[] = tMain.getSelection();
		// Then call editConnection or editStep or editTrans
		if (ti.length==1)
		{
			String name = ti[0].getText();
			TreeItem parent = ti[0].getParentItem();
			if (parent != null)
			{
				String type = parent.getText();
				if (type.equalsIgnoreCase(STRING_CONNECTIONS)) 
				{
					jobMeta.dbcache.clear(name);
				} 
			}
			else
			{
				if (name.equalsIgnoreCase(STRING_CONNECTIONS)) jobMeta.dbcache.clear(null);
			}
		}
	}

	public void exploreDB()
	{
		// Determine what menu we selected from...
		TreeItem ti[] = tMain.getSelection();
				
		// Then call editConnection or editStep or editTrans
		if (ti.length==1)
		{
			String name = ti[0].getText();
			TreeItem parent = ti[0].getParentItem();
			if (parent != null)
			{
				String type = parent.getText();
				if (type.equalsIgnoreCase(STRING_CONNECTIONS)) 
				{
					DatabaseMeta dbinfo = jobMeta.findDatabase(name);
					if (dbinfo!=null)
					{
						DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, props, SWT.NONE, dbinfo, jobMeta.databases, true );
						std.open();
					}
					else
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setMessage(Messages.getString("Chef.Dialog.UnableToFindConnection.Message")); //$NON-NLS-1$
						mb.setText(Messages.getString("Chef.Dialog.UnableToFindConnection.Title")); //$NON-NLS-1$
						mb.open();
					}
				} 
			}
			else
			{
				if (name.equalsIgnoreCase(STRING_CONNECTIONS)) jobMeta.dbcache.clear(null);
			}
		}
	}


	public void openFile(boolean importfile)
	{
        if (showChangedWarning())
        {
            if (rep==null || importfile)  // Load from XML
            {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                // dialog.setFilterPath("C:\\Projects\\kettle\\source\\");
                dialog.setFilterExtensions(Const.STRING_JOB_FILTER_EXT);
                dialog.setFilterNames(Const.STRING_JOB_FILTER_NAMES);
                String fname = dialog.open();
                if (fname!=null)
                {
                    try
                    {
                        jobMeta = new JobMeta(log, fname, rep);
                        props.addLastFile(Props.TYPE_PROPERTIES_CHEF, fname, null, false, null);
                        addMenuLast();
                    }
                    catch(KettleXMLException xe)
                    {
                        jobMeta.clear();
                        MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
                        mb.setMessage(Messages.getString("Chef.Dialog.ErrorOpeningJob.Message")+fname+Const.CR+xe.getMessage()); //$NON-NLS-1$
                        mb.setText(Messages.getString("Chef.Dialog.ErrorOpeningJob.Title")); //$NON-NLS-1$
                        mb.open();
                    }

                    refreshGraph();
                    refreshTree(true);
                    setUndoMenu();
                }
            }
            else // Read a job from the repository!
            {
                // Refresh the directory tree for now...
                SelectObjectDialog sod = new SelectObjectDialog(shell, props, rep, false, true, false);
                String fname  = sod.open();
                RepositoryDirectory repdir = sod.getDirectory();
                if (fname!=null && repdir!=null)
                {
                    JobLoadProgressDialog jlpd = new JobLoadProgressDialog(log, props, shell, rep, fname, repdir);
                    JobMeta jobInfo = jlpd.open();
                    if (jobInfo!=null)
                    {
                        jobMeta = jobInfo;
                        props.addLastFile(Props.TYPE_PROPERTIES_CHEF, fname, repdir.getPath(), true, rep.getName());
                        addMenuLast();
                    }
                    refreshGraph();
                    refreshTree(true);
                    setUndoMenu();
                }
            }
        }
    }
        
	public void newFile()
	{
		// AYS: Y/N??
		MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_WARNING );
		mb.setMessage(Messages.getString("Chef.Dialog.ConfirmNewFile.Message")); //$NON-NLS-1$
		mb.setText(Messages.getString("Chef.Dialog.ConfirmNewFile.Title")); //$NON-NLS-1$
		int answer = mb.open();
		
		if (answer == SWT.YES)
		{ 
			jobMeta.clear();
			loadRepositoryObjects();    // Add databases if connected to repository
			setFilename(null);
			refreshTree();
			refreshGraph();
            setUndoMenu();
		}
	}
	
	public void loadDatabases()
	{
		// Load common database info from active repository...
		if (rep!=null)
		{
			jobMeta.readDatabases(rep);
		}
	}

    public boolean quitFile()
    {
        boolean exit        = true;
        boolean showWarning = true;
        
        log.logDetailed(toString(), "Quit application."); //$NON-NLS-1$
        saveSettings();
        if (jobMeta.hasChanged())
        {
            MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_WARNING );
            mb.setMessage(Messages.getString("Chef.Dialog.FileChangedSaveFirst.Message")); //$NON-NLS-1$
            mb.setText(Messages.getString("Chef.Dialog.FileChangedSaveFirst.Title")); //$NON-NLS-1$
            int answer = mb.open();
        
            switch(answer)
            {
            case SWT.YES: saveFile(); exit=true; showWarning=false; break;
            case SWT.NO:  exit=true; showWarning=false; break;
            case SWT.CANCEL: 
                exit=false;
                showWarning=false;
                break;
            }
        }
        
        // System.out.println("exit="+exit+", showWarning="+showWarning+", running="+spoonlog.isRunning()+", showExitWarning="+props.showExitWarning());
        
        // Show warning on exit when spoon is still running
        // Show warning on exit when a warning needs to be displayed, but only if we didn't ask to save before. (could have pressed cancel then!)
        // 
        if ( (exit && cheflog.isRunning() ) ||
             (exit && showWarning && props.showExitWarning() )
           )
        {
            String message = Messages.getString("Chef.Dialog.ExitApplicationAreYouSure.Message");  //$NON-NLS-1$
            if (cheflog.isRunning()) message = Messages.getString("Chef.Dialog.ExitApplicationAreYouSure.MessageRunning"); //$NON-NLS-1$
            
            MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
                    Messages.getString("Chef.Dialog.ExitApplicationAreYouSure.Title"),  //$NON-NLS-1$
                    null,
                    message,
                    MessageDialog.WARNING,
                    new String[] { Messages.getString("System.Button.Yes"), Messages.getString("System.Button.No") }, //$NON-NLS-1$ //$NON-NLS-2$
                    1,
                    Messages.getString("Chef.Dialog.ExitApplicationAreYouSure.Toggle"), //$NON-NLS-1$
                    !props.showExitWarning()
               );
               int idx = md.open();
               props.setExitWarningShown(!md.getToggleState());
               props.saveProps();
               
               if (idx==1) exit=false; // No selected: don't exit!
               else exit=true;
        }            

        if (exit) dispose();
            
        return exit;
    }
	
	public void loadRepositoryObjects()
	{
		// Load common database info from active repository...
		if (rep!=null)
		{
			jobMeta.readDatabases(rep);
		}
	}
	

	public void saveFile()
	{
		log.logDetailed(toString(), "Save file..."); //$NON-NLS-1$
		if (rep!=null)
		{
			saveRepository();
		}
		else
		{
			if (jobMeta.getFilename()!=null)
			{
				save(jobMeta.getFilename());
			}
			else
			{
				saveFileAs();
			}
		}
	}

	public void saveRepository()
	{
		saveRepository(false);
	}

	public void saveRepository(boolean ask_name)
	{
		log.logDetailed(toString(), "Save to repository..."); //$NON-NLS-1$
		if (rep!=null)
		{
			boolean answer = true;
			boolean ask    = ask_name;
			while (answer && ( ask || jobMeta.getName()==null || jobMeta.getName().length()==0 ) )
			{
				if (!ask)
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					mb.setMessage(Messages.getString("Chef.Dialog.GiveJobANameBeforeSaving.Message")); //$NON-NLS-1$
					mb.setText(Messages.getString("Chef.Dialog.GiveJobANameBeforeSaving.Title")); //$NON-NLS-1$
					mb.open();
				}
				ask=false;
				answer = setJob();
			}
			
			if (answer && jobMeta.getName()!=null && jobMeta.getName().length()>0)
			{
				if (!rep.getUserInfo().isReadonly())
				{
					int response = SWT.YES;
					if (jobMeta.showReplaceWarning(rep))
					{
						MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
						mb.setMessage(Messages.getString("Chef.Dialog.JobExistsOverwrite.Message1")+jobMeta.getName()+Messages.getString("Chef.Dialog.JobExistsOverwrite.Message2")+Const.CR+Messages.getString("Chef.Dialog.JobExistsOverwrite.Message3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						mb.setText(Messages.getString("Chef.Dialog.JobExistsOverwrite.Title")); //$NON-NLS-1$
						response = mb.open();
					}
					
					if (response == SWT.YES)
					{
						// Keep info on who & when this transformation was changed...
						jobMeta.modified_date = new Value("MODIFIED_DATE", Value.VALUE_TYPE_DATE); 				 //$NON-NLS-1$
						jobMeta.modified_date.sysdate();
						jobMeta.modified_user = rep.getUserInfo().getLogin();

						JobSaveProgressDialog jspd = new JobSaveProgressDialog(log, props, shell, rep, jobMeta);
						if (jspd.open())
						{
							if (!props.getSaveConfirmation())
							{
								MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
																						 Messages.getString("Chef.Dialog.JobWasStoredInTheRepository.Title"),  //$NON-NLS-1$
																						 null,
																						 Messages.getString("Chef.Dialog.JobWasStoredInTheRepository.Message"), //$NON-NLS-1$
																						 MessageDialog.QUESTION,
																						 new String[] { Messages.getString("System.Button.OK") }, //$NON-NLS-1$
																						 0,
																						 Messages.getString("Chef.Dialog.JobWasStoredInTheRepository.Toggle"), //$NON-NLS-1$
																						 props.getSaveConfirmation()
																						 );
								md.open();
								props.setSaveConfirmation(md.getToggleState());
							}
	
							// Handle last opened files...
							props.addLastFile(Props.TYPE_PROPERTIES_CHEF, jobMeta.getName(), jobMeta.getDirectory().getPath(), true, rep.getName());
							saveSettings();
							addMenuLast();
	
							setShellText();
						}
					}
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.CLOSE | SWT.ICON_ERROR);
					mb.setMessage(Messages.getString("Chef.Dialog.UserCanOnlyReadFromTheRepositoryJobNotSaved.Message")); //$NON-NLS-1$
					mb.setText(Messages.getString("Chef.Dialog.UserCanOnlyReadFromTheRepositoryJobNotSaved.Title")); //$NON-NLS-1$
					mb.open();
				}
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage(Messages.getString("Chef.Dialog.NoRepositoryConnectionAvailable.Message")); //$NON-NLS-1$
			mb.setText(Messages.getString("Chef.Dialog.NoRepositoryConnectionAvailable.Title")); //$NON-NLS-1$
			mb.open();
		}
	}

	public void saveFileAs()
	{
		log.logBasic(toString(), "Save file as..."); //$NON-NLS-1$

		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		//dialog.setFilterPath("C:\\Projects\\kettle\\source\\");
		dialog.setFilterExtensions(STRING_FILTER_EXT);
		dialog.setFilterNames     (STRING_FILTER_NAMES);
		String fname = dialog.open();
		if (fname!=null) 
		{
			// Is the filename ending on .ktr, .xml?
			boolean ending=false;
			for (int i=0;i<STRING_FILTER_EXT.length-1;i++)
			{
				if (fname.endsWith(STRING_FILTER_EXT[i].substring(1))) ending=true;
			}
			if (fname.endsWith(STRING_DEFAULT_EXT)) ending=true;
			if (!ending)
			{
				fname+=STRING_DEFAULT_EXT;
			}
			// See if the file already exists...
			File f = new File(fname);
			int id = SWT.YES;
			if (f.exists())
			{
				MessageBox mb = new MessageBox(shell, SWT.NO | SWT.YES | SWT.ICON_WARNING);
				mb.setMessage(Messages.getString("Chef.Dialog.FileExistsOverWrite.Message")); //$NON-NLS-1$
				mb.setText(Messages.getString("Chef.Dialog.FileExistsOverWrite.Title")); //$NON-NLS-1$
				id = mb.open();
			}
			if (id==SWT.YES)
			{
				save(fname);
			}
		} 
	}
	
	private void save(String fname)
	{
		String xml = XMLHandler.getXMLHeader() + jobMeta.getXML();
		try
		{
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(fname)));
            dos.write(xml.getBytes(Const.XML_ENCODING));
            dos.close();

            // Handle last opened files...
			props.addLastFile(Props.TYPE_PROPERTIES_CHEF, fname, RepositoryDirectory.DIRECTORY_SEPARATOR, false, ""); //$NON-NLS-1$
			saveSettings();
			addMenuLast();

			log.logDebug(toString(), "File written to ["+fname+"]"); //$NON-NLS-1$ //$NON-NLS-2$
			jobMeta.setFilename( fname );
			jobMeta.clearChanged();
			setShellText();
		}
		catch(Exception e)
		{
			log.logDebug(toString(), "Error opening file for writing! --> "+e.toString()); //$NON-NLS-1$
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage(Messages.getString("Chef.Dialog.ErrorSavingFile.Message")+Const.CR+e.toString()); //$NON-NLS-1$
			mb.setText(Messages.getString("Chef.Dialog.ErrorSavingFile.Title")); //$NON-NLS-1$
			mb.open();
		}
	}
	
	public void helpAbout()
	{
		MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION | SWT.CENTER);
		String mess = Messages.getString("Chef.Dialog.About.KettleChefVersion")+Const.VERSION+Const.CR+Const.CR+Const.CR; //$NON-NLS-1$
		mess+=Messages.getString("Chef.Dialog.About.Company")+Const.CR+Messages.getString("Chef.Dialog.About.WebSite")+Const.CR; //$NON-NLS-1$ //$NON-NLS-2$
		        
        mb.setMessage(mess);
		mb.setText(Messages.getString("Chef.Application.Name")); //$NON-NLS-1$
		mb.open();
	}
	
	public void editUnselectAll()
	{
		jobMeta.unselectAll(); 
		chefgraph.redraw();
	}
	
	public void editSelectAll()
	{
		jobMeta.selectAll(); 
		chefgraph.redraw();
	}
	
	public void editOptions()
	{
		EnterOptionsDialog eod = new EnterOptionsDialog(shell, props);
		if (eod.open()!=null)
		{
			props.saveProps();
			loadSettings();
			changeLooks();
		} 
	}

    /**
     * @deprecated
     * @param section
     * @param item
     */
	public void addToTree(String section, String item)
	{
		addToTree(section, item, 0);
	}

    /**
     * @deprecated
     * @param section
     * @param item
     * @param steptype
     */
	public void addToTree(String section, String item, int steptype)
	{
		
		TreeItem ti=null;
		for (int i=1;i<tiSection.length;i++)
		{
			if (tiSection[i].getText().equalsIgnoreCase(section)) ti=tiSection[i];
		}
		
		if (ti!=null && item!=null)
		{
			TreeItem ni = new TreeItem(ti, SWT.NONE);
			ni.setText(item);
			setTreeImage(ni, section, steptype);
		}
		setShellText();
	}

	public void setTreeImage(TreeItem ni, String section)
	{
		setTreeImage(ni, section, 0);
	}
	
	public void setTreeImage(TreeItem ni, String section, int steptype)
	{
	}

	public void refreshTree()
	{
		refreshTree(false);
	}

	public void refreshTree(boolean complete)
	{
		log.logDetailed(toString(), "refreshTree() called"); //$NON-NLS-1$
	
        // Get some TreeItems...
        //
        TreeItem tiJobEntries = null;
        TreeItem tiConnections = null;
        
        TreeItem ti[] = tMain.getItems();
        for (int i=0;i<ti.length;i++)
        {
            if (ti[i].getText().equalsIgnoreCase(STRING_JOBENTRIES)) tiJobEntries=ti[i];
            if (ti[i].getText().equalsIgnoreCase(STRING_CONNECTIONS)) tiConnections=ti[i];
        }

        if (tiConnections!=null)
        {
            TreeItem entries[] = tiConnections.getItems();
            for (int i=0;i<entries.length;i++) entries[i].dispose();
        }

        if (tiJobEntries!=null)
        {
            TreeItem entries[] = tiJobEntries.getItems();
            for (int i=0;i<entries.length;i++) entries[i].dispose();
        }
		
		for (int i=0;i<jobMeta.nrDatabases();i++)
		{
			DatabaseMeta dbinfo = jobMeta.getDatabase(i);
			TreeItem item = new TreeItem(tiConnections, SWT.NONE);
			item.setText(dbinfo.getName());
		}
		
		for (int i=0;i<jobMeta.nrJobEntries();i++)
		{
			JobEntryCopy je = jobMeta.getJobEntry(i);
			TreeItem item = new TreeItem(tiJobEntries, SWT.NONE);
			if (je.getName()!=null) item.setText(je.getName());
		}

		tMain.setFocus();
		setShellText();
	}
	
	public void refreshGraph()
	{
		chefgraph.redraw();
		setShellText();
	}
	
	public void newConnection()
	{
		DatabaseMeta db = new DatabaseMeta(); 
		DatabaseDialog con = new DatabaseDialog(shell, SWT.APPLICATION_MODAL, log, db, props);
		String con_name = con.open(); 
		if (con_name!=null)
		{
			jobMeta.addDatabase(db);
			addUndoNew(new DatabaseMeta[] { (DatabaseMeta)db.clone() }, new int[] { jobMeta.indexOfDatabase(db) });
			saveConnection(db);
			refreshTree();
		}
	}

	public void saveConnection(DatabaseMeta db)
	{
		// Also add to repository?
		if (rep!=null)
		{
			if (!rep.getUserInfo().isReadonly())
			{
				try
				{
					db.saveRep(rep);
					log.logDetailed(toString(), "Saved database connection ["+db+"] to the repository."); //$NON-NLS-1$ //$NON-NLS-2$
					
                    // Put a commit behind it!
                    rep.commit();
				}
				catch(KettleException ke)
				{
					new ErrorDialog(shell, props, Messages.getString("Chef.ErrorDialog.ErrorSavingConnection.Title"), Messages.getString("Chef.ErrorDialog.ErrorSavingConnection.Message1")+db+Messages.getString("Chef.ErrorDialog.ErrorSavingConnection.Message2"), ke);  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
			else
			{
				new ErrorDialog(shell, props, Messages.getString("Chef.ErrorDialog.ReadOnlyUserErrorSavingConnection.Title"), Messages.getString("Chef.ErrorDialog.ReadOnlyUserErrorSavingConnection.Message"), new KettleException(Messages.getString("Chef.ErrorDialog.ReadOnlyUserErrorSavingConnection.Exception")));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
	}


	public JobEntryCopy newChefGraphEntry(String type_desc, boolean openit)
	{
        JobEntryLoader jobLoader = JobEntryLoader.getInstance();
        JobPlugin jobPlugin = null; 
        
		try
		{
            jobPlugin = jobLoader.findJobEntriesWithDescription(type_desc);

			if (jobPlugin!=null)
			{
				// System.out.println("new job entry of type: "+type+" ["+type_desc+"]");
				
				// Determine name & number for this entry.
				String basename = type_desc;
				int nr = jobMeta.generateJobEntryNameNr(basename);
				String entry_name = basename+" "+nr; //$NON-NLS-1$
				
				// Generate the appropriate class...
                JobEntryInterface jei = jobLoader.getJobEntryClass(jobPlugin); 
				jei.setName(entry_name);
		
				if (openit)
				{
	                JobEntryDialogInterface d = jei.getDialog(shell,jei,jobMeta,entry_name,rep);
					if (d.open()!=null)
					{
						JobEntryCopy jge = new JobEntryCopy(log);
						jge.setEntry(jei);
						jge.setLocation(50,50);
						jge.setNr(0);
						jobMeta.addJobEntry(jge);
						addUndoNew(new JobEntryCopy[] { jge }, new int[] { jobMeta.indexOfJobEntry(jge) });
						refreshGraph();
						refreshTree();
						return jge;
					}
					else
					{
						return null;
					}
				}
				else
				{
					JobEntryCopy jge = new JobEntryCopy(log);
					jge.setEntry(jei);
					jge.setLocation(50,50);
					jge.setNr(0);
					jobMeta.addJobEntry(jge);
					addUndoNew(new JobEntryCopy[] { jge }, new int[] { jobMeta.indexOfJobEntry(jge) });
					refreshGraph();
					refreshTree();
					return jge;
				}
			}
			else
			{
				return null;
			}
		}
		catch(Throwable e)
		{
			new ErrorDialog(shell, props, Messages.getString("Chef.ErrorDialog.UnexpectedErrorCreatingNewChefGraphEntry.Title"), Messages.getString("Chef.ErrorDialog.UnexpectedErrorCreatingNewChefGraphEntry.Message"),new Exception(e));  //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}
		
	public void editChefGraphEntry(JobEntryCopy je)
	{
        try
        {
    		log.logBasic(toString(), "edit job graph entry: "+je.getName()); //$NON-NLS-1$
    		
    		JobEntryCopy before =(JobEntryCopy)je.clone_deep();
    		boolean entry_changed=false;
    		
    		JobEntryInterface jei = je.getEntry();
    		
    		JobEntryDialogInterface d = jei.getDialog(shell,jei,jobMeta,je.getName(),rep); 
    		if (d!=null)
    		{
    			if (d.open()!=null)
    			{
    				entry_changed=true;
    			}
    	
    			if (entry_changed)
    			{
    				addUndoChange(new JobEntryCopy[] { before }, new JobEntryCopy[] { je }, new int[] { jobMeta.indexOfJobEntry(je) } );
    				refreshGraph();
    				refreshTree();
    			}
    		}
    		else
    		{
    			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
    			mb.setMessage(Messages.getString("Chef.Dialog.JobEntryCanNotBeChanged.Message")); //$NON-NLS-1$
    			mb.setText(Messages.getString("Chef.Dialog.JobEntryCanNotBeChanged.Title")); //$NON-NLS-1$
    			mb.open();
    		}

        }
        catch(Exception e)
        {
            new ErrorDialog(shell, props, Messages.getString("Chef.ErrorDialog.ErrorEditingJobEntry.Title"), Messages.getString("Chef.ErrorDialog.ErrorEditingJobEntry.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
	}

	
	public JobEntryTrans newJobEntry(int type)
	{
		JobEntryTrans je = new JobEntryTrans();
		je.setType(type);
		String basename = JobEntryTrans.typeDesc[type]; 
		int nr = jobMeta.generateJobEntryNameNr(basename);
		je.setName(basename+" "+nr); //$NON-NLS-1$

		setShellText();
		
		return je;
	}

	public void deleteChefGraphEntry(String name)
	{
		// First delete all the hops using entry with name:
		JobHopMeta hi[] = jobMeta.getAllJobHopsUsing(name);
		if (hi.length>0)
		{
			int hix[] = new int[hi.length];
			for (int i=0;i<hi.length;i++) hix[i] = jobMeta.indexOfJobHop(hi[i]);
			
			addUndoDelete(hi, hix);
			for (int i=hix.length-1;i>=0;i--) jobMeta.removeJobHop(hix[i]);
		}
		// Then delete all the entries with name:
		JobEntryCopy je[] = jobMeta.getAllChefGraphEntries(name);
		int jex[] = new int[je.length];
		for (int i=0;i<je.length;i++) jex[i] = jobMeta.indexOfJobEntry(je[i]);

		addUndoDelete(je, jex);
		for (int i=jex.length-1;i>=0;i--) jobMeta.removeJobEntry(jex[i]);
		
		refreshGraph();
		refreshTree();
	}

	public void dupeChefGraphEntry(String name)
	{
		JobEntryCopy jge = jobMeta.findJobEntry(name, 0, true);
		if (jge!=null)
		{
			JobEntryCopy dupejge = (JobEntryCopy)jge.clone();
			dupejge.setNr( jobMeta.findUnusedNr(dupejge.getName()) );
			if (dupejge.isDrawn())
			{
				Point p = jge.getLocation();
				dupejge.setLocation(p.x+10, p.y+10);
			}
			jobMeta.addJobEntry(dupejge);
			refreshGraph();
			refreshTree();
		}
		setShellText();
	}
	
	
	public void copyJobEntries(JobEntryCopy jec[])
	{
		if (jec==null || jec.length==0) return;
		
		String xml = XMLHandler.getXMLHeader();
		xml+="<jobentries>"+Const.CR; //$NON-NLS-1$

		for (int i=0;i<jec.length;i++)
		{
			xml+=jec[i].getXML();
		}
		
		xml+="    </jobentries>"+Const.CR; //$NON-NLS-1$
		
		toClipboard(xml);
	}

	public void pasteXML(String clipcontent, Point loc)
	{
		try
		{
			Document doc = XMLHandler.loadXMLString(clipcontent);

			// De-select all, re-select pasted steps...
			jobMeta.unselectAll();
			
			Node entriesnode = XMLHandler.getSubNode(doc, "jobentries"); //$NON-NLS-1$
			int nr = XMLHandler.countNodes(entriesnode, "entry"); //$NON-NLS-1$
			log.logDebug(toString(), "I found "+nr+" job entries to paste on location: "+loc); //$NON-NLS-1$ //$NON-NLS-2$
			JobEntryCopy entries[] = new JobEntryCopy[nr];
			
			//Point min = new Point(loc.x, loc.y);
			Point min = new Point(99999999,99999999);
			
			for (int i=0;i<nr;i++)
			{
				Node entrynode = XMLHandler.getSubNodeByNr(entriesnode, "entry", i); //$NON-NLS-1$
				entries[i] = new JobEntryCopy(entrynode, jobMeta.getDatabases(), rep);

				String name = jobMeta.getAlternativeJobentryName(entries[i].getName());
				entries[i].setName(name);

				if (loc!=null)
				{
					Point p = entries[i].getLocation();
					
					if (min.x > p.x) min.x = p.x;
					if (min.y > p.y) min.y = p.y;
				}
			}
			
			// What's the difference between loc and min?
			// This is the offset:
			Point offset = new Point(loc.x-min.x, loc.y-min.y);
			
			// Undo/redo object positions...
			int position[] = new int[entries.length];
			
			for (int i=0;i<entries.length;i++)
			{
				Point p = entries[i].getLocation();
				String name = entries[i].getName();
				
				entries[i].setLocation(p.x+offset.x, p.y+offset.y);
				
				// Check the name, find alternative...
				entries[i].setName( jobMeta.getAlternativeJobentryName(name) );
				jobMeta.addJobEntry(entries[i]);
				position[i] = jobMeta.indexOfJobEntry(entries[i]);
			}
			
			// Save undo information too...
			addUndoNew(entries, position);

			if (jobMeta.hasChanged())
			{
				refreshTree();
				refreshGraph();
			}
		}
		catch(KettleException e)
		{
		    new ErrorDialog(shell, props, Messages.getString("Chef.ErrorDialog.ErrorPasingJobEntries.Title"), Messages.getString("Chef.ErrorDialog.ErrorPasingJobEntries.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	

	public void toClipboard(String cliptext)
	{
	    props.toClipboard(cliptext);
	}
	
	public String fromClipboard()
	{
	    return props.fromClipboard();
	}
	
	public void setShellText()
	{
		String fname = jobMeta.getFilename();
		if (shell.isDisposed()) return;

		if (rep!=null)
		{
			String repository = "["+getRepositoryName()+"]"; //$NON-NLS-1$ //$NON-NLS-2$
			String transname  = jobMeta.getName();
			if (transname==null) transname=Messages.getString("Chef.ShellText.NoJobName"); //$NON-NLS-1$
			shell.setText(APPL_TITLE+" - "+repository+"   "+transname+(jobMeta.hasChanged()?Messages.getString("Chef.ShellText.Changed"):"")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		else
		{
			String repository = Messages.getString("Chef.ShellText.NoRepository"); //$NON-NLS-1$
			if (fname!=null)
			{
				shell.setText(APPL_TITLE+" - "+repository+Messages.getString("Chef.ShellText.File")+fname+(jobMeta.hasChanged()?Messages.getString("Chef.ShellText.Changed2"):"")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			else
			{
				shell.setText(APPL_TITLE+" - "+repository+"   "+(jobMeta.hasChanged()?Messages.getString("Chef.ShellText.Changed3"):"")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
		}
	}

	private void setFilename(String fname)
	{
		jobMeta.setFilename( fname );
		setShellText();
	}
	
	private void printFile()
	{
		PrintSpool ps = new PrintSpool();
		Printer printer = ps.getPrinter(shell);
		
		// Create an image of the screen
		Point max = jobMeta.getMaximum();
		
		//Image img_screen = new Image(trans, max.x, max.y);
		//img_screen.dispose();
		
		PaletteData pal = ps.getPaletteData();		
		
		ImageData imd = new ImageData(max.x, max.y, printer.getDepth(), pal);
		Image img = new Image(printer, imd);
		
		GC img_gc = new GC(img);
		
		// Clear the background first, fill with background color...
		img_gc.setForeground(GUIResource.getInstance().getColorBackground());
		img_gc.fillRectangle(0,0,max.x, max.y);
		
		// Draw the transformation...
		chefgraph.drawJob(img_gc);
		
		//ShowImageDialog sid = new ShowImageDialog(shell, jobMeta.props, img);
		//sid.open();
		
		ps.printImage(shell, props, img);
		
		img_gc.dispose();
		img.dispose();
		ps.dispose();
	}
	
	private boolean setJob()
	{
		JobDialog jd = new JobDialog(shell, SWT.NONE, jobMeta, rep);
		JobMeta ji = jd.open();
		setShellText();
		return ji!=null;
	}
	
	
	public boolean ripDB(	String jobName, 
							RepositoryDirectory repositoryDirectory, 
							DatabaseMeta srcDbInfo, 
							DatabaseMeta tgtDbInfo, 
							String[] tablesToRip
					 	)
	{
		final String[] tables = tablesToRip;
		final DatabaseMeta sourceDbInfo = srcDbInfo;
		final DatabaseMeta targetDbInfo = tgtDbInfo;
		final RepositoryDirectory repdir = repositoryDirectory;
		final String jobname = jobName;
		//
		// Create a new job...
		//
		jobMeta=new JobMeta(log);
		jobMeta.readDatabases(rep);
		setFilename(null);
		jobMeta.setName(jobname);
		jobMeta.setDirectory( repdir );
		refreshTree();
		refreshGraph();
		
		final Point location = new Point(50, 50);
		
		// The start entry...
		final JobEntryCopy start = jobMeta.findStart();
		start.setLocation(new Point(location.x, location.y));
		start.setDrawn();

		// Create a dialog with a progress indicator!
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
                // This is running in a new process: copy some KettleVariables info
                LocalVariables.getInstance().createKettleVariables(Thread.currentThread(), shell.getDisplay().getSyncThread(), true);

				monitor.beginTask(Messages.getString("Chef.RipDB.Monitor.BuildingNewJob"), tables.length); //$NON-NLS-1$
				monitor.worked(0);
				JobEntryCopy previous = start;
				
				// Loop over the table-names...
				for (int i=0;i<tables.length && !monitor.isCanceled();i++)
				{
					monitor.setTaskName(Messages.getString("Chef.RipDB.Monitor.ProcessingTable")+tables[i]+"]..."); //$NON-NLS-1$ //$NON-NLS-2$
					//
					// Create the new transformation...
					//
					String transname = Messages.getString("Chef.RipDB.Monitor.Transname1")+sourceDbInfo+"].["+tables[i]+Messages.getString("Chef.RipDB.Monitor.Transname2")+targetDbInfo+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					
					TransMeta ti = new TransMeta((String)null, transname, null);
					
					ti.setDirectory( repdir );
					
					//
					// Add a note
					//
					String note = Messages.getString("Chef.RipDB.Monitor.Note1")+tables[i]+Messages.getString("Chef.RipDB.Monitor.Note2")+sourceDbInfo+"]"+Const.CR; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					note+=Messages.getString("Chef.RipDB.Monitor.Note3")+tables[i]+Messages.getString("Chef.RipDB.Monitor.Note4")+targetDbInfo+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					NotePadMeta ni = new NotePadMeta(note, 150, 10, -1, -1);
					ti.addNote(ni);
					
					//
					// Add the TableInputMeta step...
					// 
					String fromstepname = Messages.getString("Chef.RipDB.Monitor.FromStep.Name")+tables[i]+"]"; //$NON-NLS-1$ //$NON-NLS-2$
					TableInputMeta tii = new TableInputMeta();
					tii.setDatabaseMeta( sourceDbInfo );
					tii.setSQL( "SELECT * FROM "+tables[i] ); //$NON-NLS-1$
					
					String fromstepid = StepLoader.getInstance().getStepPluginID(tii);
					StepMeta fromstep = new StepMeta(log, fromstepid, fromstepname, (StepMetaInterface)tii );
					fromstep.setLocation(150,100);
					fromstep.setDraw(true);
					fromstep.setDescription(Messages.getString("Chef.RipDB.Monitor.FromStep.Description")+tables[i]+Messages.getString("Chef.RipDB.Monitor.FromStep.Description2")+sourceDbInfo+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					ti.addStep(fromstep);
					
					//
					// Add the TableOutputMeta step...
					//
					String tostepname = Messages.getString("Chef.RipDB.Monitor.ToStep.Name")+tables[i]+"]"; //$NON-NLS-1$ //$NON-NLS-2$
					TableOutputMeta toi = new TableOutputMeta();
					toi.setDatabase( targetDbInfo );
					toi.setTablename( tables[i] );
					toi.setCommitSize( 100 );
					toi.setTruncateTable( true );
					
					String tostepid = StepLoader.getInstance().getStepPluginID(toi);
					StepMeta tostep = new StepMeta(log, tostepid, tostepname, (StepMetaInterface)toi );
					tostep.setLocation(500,100);
					tostep.setDraw(true);
					tostep.setDescription(Messages.getString("Chef.RipDB.Monitor.ToStep.Description1")+tables[i]+Messages.getString("Chef.RipDB.Monitor.ToStep.Description2")+targetDbInfo+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					ti.addStep(tostep);
					
					//
					// Add a hop between the two steps...
					//
					TransHopMeta hi = new TransHopMeta(fromstep, tostep);
					ti.addTransHop(hi);
					
					//
					// Now we generate the SQL needed to run for this transformation.
					//
					// First set the limit to 1 to speed things up!
					String tmpSql = tii.getSQL();
					tii.setSQL( tii.getSQL()+sourceDbInfo.getLimitClause(1) );
					String sql = ""; //$NON-NLS-1$
					try
					{
						sql = ti.getSQLStatementsString();
					}
					catch(KettleStepException kse)
					{
						throw new InvocationTargetException(kse, Messages.getString("Chef.RipDB.Exception.ErrorGettingSQLFromTransformation")+ti+"] : "+kse.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
					}
					// remove the limit
					tii.setSQL( tmpSql );
					
					//
					// Now, save the transformation...
					//
					try
					{
						ti.saveRep(rep);
					}
					catch(KettleException dbe)
					{
						throw new InvocationTargetException(dbe, Messages.getString("Chef.RipDB.Exception.UnableToSaveTransformationToRepository")); //$NON-NLS-1$
					}
					
					// We can now continue with the population of the job...
					////////////////////////////////////////////////////////////////////////
					
					location.x=250;
					if (i>0) location.y += 100;

					//
					// We can continue defining the job.
					//
					// First the SQL, but only if needed!
					// If the table exists & has the correct format, nothing is done 
					//
					if (sql!=null && sql.length()>0)
					{
						String jesqlname = Messages.getString("Chef.RipDB.JobEntrySQL.Name")+tables[i]+"]"; //$NON-NLS-1$ //$NON-NLS-2$
						JobEntrySQL jesql = new JobEntrySQL(jesqlname);
						jesql.setDatabase(targetDbInfo);
						jesql.setSQL(sql);
						jesql.setDescription(Messages.getString("Chef.RipDB.JobEntrySQL.Description")+targetDbInfo+"].["+tables[i]+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						
						JobEntryCopy jecsql = new JobEntryCopy(log);
						jecsql.setEntry(jesql);
						jecsql.setLocation(new Point(location.x, location.y));
						jecsql.setDrawn();
						jobMeta.addJobEntry(jecsql);
						
						// Add the hop too...
						JobHopMeta jhi = new JobHopMeta(previous, jecsql);
						jobMeta.addJobHop(jhi);
						previous = jecsql;
					}
					
					//
					// Add the jobentry for the transformation too...
					//
					String jetransname = Messages.getString("Chef.RipDB.JobEntryTrans.Name")+tables[i]+"]"; //$NON-NLS-1$ //$NON-NLS-2$
					JobEntryTrans jetrans = new JobEntryTrans(jetransname);
					jetrans.setTransname(ti.getName());
					jetrans.setDirectory(ti.getDirectory());
					
					JobEntryCopy jectrans = new JobEntryCopy(log, jetrans);
					jectrans.setDescription(Messages.getString("Chef.RipDB.JobEntryTrans.Description1")+Const.CR+Messages.getString("Chef.RipDB.JobEntryTrans.Description2")+sourceDbInfo+"].["+tables[i]+"]"+Const.CR+Messages.getString("Chef.RipDB.JobEntryTrans.Description3")+targetDbInfo+"].["+tables[i]+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
					jectrans.setDrawn();
					location.x+=400;
					jectrans.setLocation(new Point(location.x, location.y));
					jobMeta.addJobEntry(jectrans);
					
					// Add a hop between the last 2 job entries.
					JobHopMeta jhi2 = new JobHopMeta(previous, jectrans);
					jobMeta.addJobHop(jhi2);
					previous = jectrans;
					
					monitor.worked( 1 );
				}
				
				monitor.worked(100);
				monitor.done();
			}
		};
		
		try
		{
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
			pmd.run(false, true, op);
		}
		catch (InvocationTargetException e)
		{
			new ErrorDialog(shell, props, Messages.getString("Chef.ErrorDialog.RipDB.ErrorRippingTheDatabase.Title"), Messages.getString("Chef.ErrorDialog.RipDB.ErrorRippingTheDatabase.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, props, Messages.getString("Chef.ErrorDialog.RipDB.ErrorRippingTheDatabase.Title"), Messages.getString("Chef.ErrorDialog.RipDB.ErrorRippingTheDatabase.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		finally
		{
			refreshGraph();
			refreshTree();
		}
		return true;
	}
	
	/**
	 * Create a job that extracts tables & data from a database.<p><p>
	 * 
	 * 0) Select the database to rip<p>
	 * 1) Select the tables in the database to rip<p>
	 * 2) Select the database to dump to<p>
	 * 3) Select the repository directory in which it will end up<p>
	 * 4) Select a name for the new job<p>
	 * 5) Create an empty job with the selected name.<p>
	 * 6) Create 1 transformation for every selected table<p>
	 * 7) add every created transformation to the job & evaluate<p>
	 * 
	 */
	private void ripDBWizard()
	{
		final RipDatabaseWizardPage1 page1 = new RipDatabaseWizardPage1("1", jobMeta.databases); //$NON-NLS-1$
		page1.createControl(shell);
		final RipDatabaseWizardPage2 page2 = new RipDatabaseWizardPage2 ("2"); //$NON-NLS-1$
		page2.createControl(shell);
		final RipDatabaseWizardPage3 page3 = new RipDatabaseWizardPage3 ("3", rep); //$NON-NLS-1$
		page3.createControl(shell);

		Wizard wizard = new Wizard() 
		{
			public boolean performFinish() 
			{
				return ripDB(page3.getJobname(), page3.getDirectory(),
					  page1.getSourceDatabase(), page1.getTargetDatabase(),
					  page2.getSelection()
					  );
			}
			
			/**
			 * @see org.eclipse.jface.wizard.Wizard#canFinish()
			 */
			public boolean canFinish()
			{
				return page3.canFinish();
			}
		};
				
		wizard.addPage(page1);
		wizard.addPage(page2);
		wizard.addPage(page3);
				
		WizardDialog wd = new WizardDialog(shell, wizard);
		wd.setMinimumPageSize(700,400);
		wd.open();
	}

	/**
	 * Shows a wizard that creates a new database connection...
	 *
	 */
    private void createDatabaseWizard()
    {
    	CreateDatabaseWizard cdw=new CreateDatabaseWizard();
    	DatabaseMeta newDBInfo=cdw.createAndRunDatabaseWizard(shell, props, jobMeta.getDatabases());
    	if(newDBInfo!=null){ //finished
    		jobMeta.addDatabase(newDBInfo);
    		refreshTree(true);
    		refreshGraph();
    	}
    }

	
	public void saveSettings()
	{
		WindowProperty winprop = new WindowProperty(shell);
		winprop.setName(APPL_TITLE);

		props.setScreen(winprop);
		props.setScreen(new WindowProperty(shell));
		props.setLogLevel(log.getLogLevelDesc());
		props.setSashWeights(sashform.getWeights());
		props.saveProps();
	}


	public void loadSettings()
	{
		log.setLogLevel(props.getLogLevel());
        log.setFilter(props.getLogFilter());
        
		jobMeta.setMaxUndo(props.getMaxUndo());
	}

	public void changeLooks()
	{
		props.setLook( tMain );
		
		chefgraph.newProps();

		refreshTree();
		refreshGraph();
	}

	public void undoAction()
	{
		chefgraph.forceFocus();
		
		TransAction ta = jobMeta.previousUndo();
		if (ta==null) return;
		setUndoMenu(); // something changed: change the menu
		switch(ta.getType())
		{
			//
			// NEW
			//

			// We created a new entry : undo this...
			case TransAction.TYPE_ACTION_NEW_JOB_ENTRY:
				// Delete the entry at correct location:
				{
					int idx[] = ta.getCurrentIndex();
					for (int i=idx.length-1;i>=0;i--) jobMeta.removeJobEntry(idx[i]);
					refreshTree();
					refreshGraph();
				}
				break;
	
			// We created a new note : undo this...
			case TransAction.TYPE_ACTION_NEW_NOTE:
				// Delete the note at correct location:
				{
					int idx[] = ta.getCurrentIndex();
					for (int i=idx.length-1;i>=0;i--) jobMeta.removeNote(idx[i]);
					refreshTree();
					refreshGraph();
				}
				break;
	
			// We created a new hop : undo this...
			case TransAction.TYPE_ACTION_NEW_JOB_HOP:
				// Delete the hop at correct location:
				{
					int idx[] = ta.getCurrentIndex();
					for (int i=idx.length-1;i>=0;i--) jobMeta.removeJobHop(idx[i]);
					refreshTree();
					refreshGraph();
				}
				break;

			//
			// DELETE
			//

			// We delete an entry : undo this...
			case TransAction.TYPE_ACTION_DELETE_STEP:
				// un-Delete the entry at correct location: re-insert
				{
					JobEntryCopy ce[] = (JobEntryCopy[])ta.getCurrent();
					int idx[] = ta.getCurrentIndex();
					for (int i=0;i<ce.length;i++) jobMeta.addJobEntry(idx[i], ce[i]);
					refreshTree();
					refreshGraph();
				}
				break;
	
			// We delete new note : undo this...
			case TransAction.TYPE_ACTION_DELETE_NOTE:
				// re-insert the note at correct location:
				{
					NotePadMeta ni[] = (NotePadMeta[])ta.getCurrent();
					int idx[] = ta.getCurrentIndex();
					for (int i=0;i<idx.length;i++) jobMeta.addNote(idx[i], ni[i]);
					refreshTree();
					refreshGraph();
				}
				break;
	
			// We deleted a new hop : undo this...
			case TransAction.TYPE_ACTION_DELETE_JOB_HOP:
				// re-insert the hop at correct location:
				{
					JobHopMeta hi[] = (JobHopMeta[])ta.getCurrent();
					int idx[] = ta.getCurrentIndex();
					for (int i=0;i<hi.length;i++)
					{
						jobMeta.addJobHop(idx[i], hi[i]);
					}
					refreshTree();
					refreshGraph();
				}
				break;


			//
			// CHANGE
			//

			// We changed a job entry: undo this...
			case TransAction.TYPE_ACTION_CHANGE_JOB_ENTRY:
				// Delete the current job entry, insert previous version.
				{
					JobEntryCopy prev[] = (JobEntryCopy[])ta.getPrevious();
					int idx[] = ta.getCurrentIndex();
					
					for (int i=0;i<idx.length;i++)
					{
						jobMeta.removeJobEntry(idx[i]);
						jobMeta.addJobEntry(idx[i], prev[i]);
					}
					refreshTree();
					refreshGraph();
				}
				break;
	
			// We changed a note : undo this...
			case TransAction.TYPE_ACTION_CHANGE_NOTE:
				// Delete & re-insert
				{
					NotePadMeta prev[] = (NotePadMeta[])ta.getPrevious();
					int idx[] = ta.getCurrentIndex();
					for (int i=0;i<idx.length;i++)
					{
						jobMeta.removeNote(idx[i]);
						jobMeta.addNote(idx[i], prev[i]);
					}
					refreshTree();
					refreshGraph();
				}
				break;
	
			// We changed a hop : undo this...
			case TransAction.TYPE_ACTION_CHANGE_JOB_HOP:
				// Delete & re-insert
				{
					JobHopMeta prev[] = (JobHopMeta[])ta.getPrevious();
					int idx[] = ta.getCurrentIndex();
					for (int i=0;i<idx.length;i++)
					{
						jobMeta.removeJobHop(idx[i]);
						jobMeta.addJobHop(idx[i], prev[i]);
					}
					refreshTree();
					refreshGraph();
				}
				break;

			//
			// POSITION
			//
				
			// The position of a step has changed: undo this...
			case TransAction.TYPE_ACTION_POSITION_JOB_ENTRY:
				// Find the location of the step:
				{
					int  idx[] = ta.getCurrentIndex();
					Point  p[] = ta.getPreviousLocation();
					for (int i = 0; i < p.length; i++) 
					{
						JobEntryCopy entry = jobMeta.getJobEntry(idx[i]);
						entry.setLocation(p[i]);
					}
					refreshGraph();
				}
				break;
	
			// The position of a note has changed: undo this...
			case TransAction.TYPE_ACTION_POSITION_NOTE:
				int idx[] = ta.getCurrentIndex();
				Point prev[] = ta.getPreviousLocation();
				for (int i=0;i<idx.length;i++)
				{
					NotePadMeta npi = jobMeta.getNote(idx[i]);
					npi.setLocation(prev[i]);
				}
				refreshGraph();
				break;
			default: break;
		}
	}
	
	public void redoAction()
	{
		chefgraph.forceFocus();

		TransAction ta = jobMeta.nextUndo();
		if (ta==null) return;
		setUndoMenu(); // something changed: change the menu
		switch(ta.getType())
		{
		//
		// NEW
		//
		case TransAction.TYPE_ACTION_NEW_JOB_ENTRY:
			// re-delete the entry at correct location:
			{
				JobEntryCopy si[] = (JobEntryCopy[])ta.getCurrent();
				int idx[] = ta.getCurrentIndex();
				for (int i=0;i<idx.length;i++) jobMeta.addJobEntry(idx[i], si[i]);
				refreshTree();
				refreshGraph();
			}
			break;

		case TransAction.TYPE_ACTION_NEW_NOTE:
			// re-insert the note at correct location:
			{
				NotePadMeta ni[] = (NotePadMeta[])ta.getCurrent();
				int idx[] = ta.getCurrentIndex();
				for (int i=0;i<idx.length;i++) jobMeta.addNote(idx[i], ni[i]);
				refreshTree();
				refreshGraph();
			}
			break;

		case TransAction.TYPE_ACTION_NEW_JOB_HOP:
			// re-insert the hop at correct location:
			{
				JobHopMeta hi[] = (JobHopMeta[])ta.getCurrent();
				int idx[] = ta.getCurrentIndex();
				for (int i=0;i<idx.length;i++) jobMeta.addJobHop(idx[i], hi[i]);
				refreshTree();
				refreshGraph();
			}
			break;
		
		//	
		// DELETE
		//
		case TransAction.TYPE_ACTION_DELETE_JOB_ENTRY:
			// re-remove the entry at correct location:
			{
				int idx[] = ta.getCurrentIndex();
				for (int i=idx.length-1;i>=0;i--) jobMeta.removeJobEntry(idx[i]);
				refreshTree();
				refreshGraph();
			}
			break;

		case TransAction.TYPE_ACTION_DELETE_NOTE:
			// re-remove the note at correct location:
			{
				int idx[] = ta.getCurrentIndex();
				for (int i=idx.length-1;i>=0;i--) jobMeta.removeNote(idx[i]);
				refreshTree();
				refreshGraph();
			}
			break;

		case TransAction.TYPE_ACTION_DELETE_JOB_HOP:
			// re-remove the hop at correct location:
			{
				int idx[] = ta.getCurrentIndex();
				for (int i=idx.length-1;i>=0;i--) jobMeta.removeJobHop(idx[i]);
				refreshTree();
				refreshGraph();
			}
			break;

		//
		// CHANGE
		//

		// We changed a step : undo this...
		case TransAction.TYPE_ACTION_CHANGE_JOB_ENTRY:
			// Delete the current step, insert previous version.
			{
				JobEntryCopy ce[] = (JobEntryCopy[])ta.getCurrent();
				int idx[] = ta.getCurrentIndex();
				
				for (int i=0;i<idx.length;i++)
				{
					jobMeta.removeJobEntry(idx[i]);
					jobMeta.addJobEntry(idx[i], ce[i]);
				}
				refreshTree();
				refreshGraph();
			}
			break;

		// We changed a note : undo this...
		case TransAction.TYPE_ACTION_CHANGE_NOTE:
			// Delete & re-insert
			{
				NotePadMeta ni[] = (NotePadMeta[])ta.getCurrent();
				int idx[] = ta.getCurrentIndex();
				
				for (int i=0;i<idx.length;i++)
				{
					jobMeta.removeNote(idx[i]);
					jobMeta.addNote(idx[i], ni[i]);
				}
				refreshTree();
				refreshGraph();
			}
			break;

		// We changed a hop : undo this...
		case TransAction.TYPE_ACTION_CHANGE_JOB_HOP:
			// Delete & re-insert
			{
				JobHopMeta hi[] = (JobHopMeta[])ta.getCurrent();
				int idx[] = ta.getCurrentIndex();

				for (int i=0;i<idx.length;i++)
				{
					jobMeta.removeJobHop(idx[i]);
					jobMeta.addJobHop(idx[i], hi[i]);
				}
				refreshTree();
				refreshGraph();
			}
			break;

		//
		// CHANGE POSITION
		//
		case TransAction.TYPE_ACTION_POSITION_JOB_ENTRY:
			{
				// Find the location of the step:
				int idx[] = ta.getCurrentIndex();
				Point p[] = ta.getCurrentLocation();
				for (int i = 0; i < p.length; i++) 
				{
					JobEntryCopy entry = jobMeta.getJobEntry(idx[i]);
					entry.setLocation(p[i]);
				}
				refreshGraph();
			}
			break;
		case TransAction.TYPE_ACTION_POSITION_NOTE:
			{
				int idx[] = ta.getCurrentIndex();
				Point curr[] = ta.getCurrentLocation();
				for (int i=0;i<idx.length;i++)
				{
					NotePadMeta npi = jobMeta.getNote(idx[i]);
					npi.setLocation(curr[i]);
				}
				refreshGraph();
			}
			break;
		default: break;
		}
	}
	
	public void setUndoMenu()
	{
		TransAction prev = jobMeta.viewThisUndo();
		TransAction next = jobMeta.viewNextUndo();
		
		if (prev!=null) 
		{
			miEditUndo.setEnabled(true);
			miEditUndo.setText(Messages.getString("Chef.Menu.Edit.Undo")+prev.toString()+" \tCTRL-Z"); //$NON-NLS-1$ //$NON-NLS-2$
		} 
		else            
		{
			miEditUndo.setEnabled(false);
			miEditUndo.setText(Messages.getString("Chef.Menu.Edit.UndoNotAvailable")); //$NON-NLS-1$
		} 

		if (next!=null) 
		{
			miEditRedo.setEnabled(true);
			miEditRedo.setText(Messages.getString("Chef.Menu.Edit.Redo")+next.toString()+" \tCTRL-Y"); //$NON-NLS-1$ //$NON-NLS-2$
		} 
		else            
		{
			miEditRedo.setEnabled(false);
			miEditRedo.setText(Messages.getString("Chef.Menu.Edit.RedoNotAvailable"));			 //$NON-NLS-1$
		} 

	}


	public void addUndoNew(Object obj[], int position[])
	{
		// New step?
		jobMeta.addUndo(obj, null, position, null, null, JobMeta.TYPE_UNDO_NEW);
		setUndoMenu();
	}	

	// Undo delete object
	public void addUndoDelete(Object obj[], int position[])
	{
		jobMeta.addUndo(obj, null, position, null, null, JobMeta.TYPE_UNDO_DELETE);
		setUndoMenu();
	}	

	// Change of step, connection, hop or note...
	public void addUndoPosition(Object obj[], int pos[], Point prev[], Point curr[])
	{
		// It's better to store the indexes of the objects, not the objects itself!
		jobMeta.addUndo(obj, null, pos, prev, curr, JobMeta.TYPE_UNDO_POSITION);
		setUndoMenu();
	}

	// Change of step, connection, hop or note...
	public void addUndoChange(Object from[], Object to[], int[] pos)
	{
		jobMeta.addUndo(from, to, pos, null, null, JobMeta.TYPE_UNDO_CHANGE);
		setUndoMenu();
	}
	
	public ChefGraph getChefGraph()
	{
		return chefgraph;
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
	
	public static void main (String [] a) throws KettleException
	{
		EnvUtil.environmentInit();
	    ArrayList args = new ArrayList();
	    for (int i=0;i<a.length;i++) args.add(a[i]);

		Display display = new Display();
		Splash splash = new Splash(display);
		
		// System.out.println("Welcome to Chef!");
		StringBuffer optionRepname, optionUsername, optionPassword, optionJobname, optionFilename, optionDirname, optionLogfile;

		CommandLineOption options[] = new CommandLineOption[] 
            {
			    new CommandLineOption("rep", Messages.getString("Chef.CommandLine.RepositoryName.Description"), optionRepname=new StringBuffer()), //$NON-NLS-1$ //$NON-NLS-2$
			    new CommandLineOption("user", Messages.getString("Chef.CommandLine.Username.Description"), optionUsername=new StringBuffer()), //$NON-NLS-1$ //$NON-NLS-2$
			    new CommandLineOption("pass", Messages.getString("Chef.CommandLine.Password.Description"), optionPassword=new StringBuffer()), //$NON-NLS-1$ //$NON-NLS-2$
			    new CommandLineOption("job", Messages.getString("Chef.CommandLine.JobName.Description"), optionJobname=new StringBuffer()), //$NON-NLS-1$ //$NON-NLS-2$
			    new CommandLineOption("dir", Messages.getString("Chef.CommandLine.RepositoryDirectory.Description"), optionDirname=new StringBuffer()), //$NON-NLS-1$ //$NON-NLS-2$
			    new CommandLineOption("file", Messages.getString("Chef.CommandLine.Filename.Description"), optionFilename=new StringBuffer()), //$NON-NLS-1$ //$NON-NLS-2$
			    new CommandLineOption("logfile", Messages.getString("Chef.CommandLine.LogFile.Description"), optionLogfile=new StringBuffer()), //$NON-NLS-1$ //$NON-NLS-2$
			    new CommandLineOption("log", Messages.getString("Chef.CommandLine.LogFileDeprecated.Description"), optionLogfile=new StringBuffer(), false, true), //$NON-NLS-1$ //$NON-NLS-2$
            };

		// Parse the options...
		CommandLineOption.parseArguments(args, options);

        String kettleRepname  = Const.getEnvironmentVariable("KETTLE_REPOSITORY", null); //$NON-NLS-1$
        String kettleUsername = Const.getEnvironmentVariable("KETTLE_USER", null); //$NON-NLS-1$
        String kettlePassword = Const.getEnvironmentVariable("KETTLE_PASSWORD", null); //$NON-NLS-1$
        
        if (kettleRepname !=null && kettleRepname .length()>0) optionRepname  = new StringBuffer(kettleRepname);
        if (kettleUsername!=null && kettleUsername.length()>0) optionUsername = new StringBuffer(kettleUsername);
        if (kettlePassword!=null && kettlePassword.length()>0) optionPassword = new StringBuffer(kettlePassword);

		Locale.setDefault(Const.DEFAULT_LOCALE);
				
        LogWriter log;
        if (Const.isEmpty(optionLogfile))
        {
            log=LogWriter.getInstance(Const.SPOON_LOG_FILE, false, LogWriter.LOG_LEVEL_BASIC);
        }
        else
        {
            log=LogWriter.getInstance( optionLogfile.toString(), true, LogWriter.LOG_LEVEL_BASIC );
        }
        
        if (log.getRealFilename()!=null) log.logBasic(APP_NAME, Messages.getString("Chef.Log.LoggingGoesTo")+log.getRealFilename()); //$NON-NLS-1$
        		
		/* Load the plugins etc.*/
		StepLoader stloader = StepLoader.getInstance();
		if (!stloader.read())
		{
			log.logError(APP_NAME, Messages.getString("Chef.Log.Error.LoadingSteps")); //$NON-NLS-1$
			return;
		}
        
        /* Load the plugins etc.*/
        JobEntryLoader jeloader = JobEntryLoader.getInstance();
        if (!jeloader.read())
        {
            log.logError(APP_NAME, Messages.getString("Chef.Log.Error.LoadingJobEntries")); //$NON-NLS-1$
            return;
        }


		final Chef win = new Chef(log, display,  null);
		win.setDestroy(true);
		
		log.logDetailed(APP_NAME, "Main window is created."); //$NON-NLS-1$
		
		RepositoryMeta repinfo = null;
		UserInfo userinfo = null;
		
		if (Const.isEmpty(optionRepname) && Const.isEmpty(optionFilename) && win.props.showRepositoriesDialogAtStartup())
		{		
            log.logDetailed(APP_NAME, "Asking for repository"); //$NON-NLS-1$

			int perms[] = new int[] { PermissionMeta.TYPE_PERMISSION_JOB };
			splash.hide();
			RepositoriesDialog rd = new RepositoriesDialog(win.disp, SWT.NONE, perms, Messages.getString("Chef.Application.Name")); //$NON-NLS-1$
			if (rd.open())
			{
				repinfo = rd.getRepository();
				userinfo = rd.getUser();
				if (!userinfo.useJobs())
				{
					MessageBox mb = new MessageBox(win.shell, SWT.OK | SWT.ICON_ERROR );
					mb.setMessage(Messages.getString("Chef.Message.UserHasNoRightsToWorkWithJobs.Message")); //$NON-NLS-1$
					mb.setText(Messages.getString("Chef.Message.UserHasNoRightsToWorkWithJobs.Title")); //$NON-NLS-1$
					mb.open();
					
					userinfo = null;
					repinfo  = null;
				}
			}
			else
			{
				// Exit point: user pressed CANCEL!
				if (rd.isCancelled()) 
				{
					splash.dispose();
					win.quitFile();
					return;
				}
			}
		}
		

		try
		{
			// Read kettle job specified on command-line?
			if (!Const.isEmpty(optionRepname) || !Const.isEmpty(optionFilename))
			{
				if (!Const.isEmpty(optionRepname))
				{
					RepositoriesMeta repsinfo = new RepositoriesMeta(log);
					if (repsinfo.readData())
					{
						repinfo = repsinfo.findRepository(optionRepname.toString());
						if (repinfo!=null)
						{
							// Define and connect to the repository...
							win.rep = new Repository(log, repinfo, userinfo);
							if (win.rep.connect(Messages.getString("Chef.Application.Name"))) //$NON-NLS-1$
							{
								if (Const.isEmpty(optionDirname)) optionDirname=new StringBuffer(RepositoryDirectory.DIRECTORY_SEPARATOR);
	
								// Check username, password
								win.rep.userinfo = new UserInfo(win.rep, optionUsername.toString(), optionPassword.toString());
								
								if (win.rep.getUserInfo().getID()>0)
								{
									RepositoryDirectory repdir = win.rep.getDirectoryTree().findDirectory(optionDirname.toString());
									
									win.jobMeta = new JobMeta(log, win.rep, optionJobname.toString(), repdir);
									win.setFilename(optionFilename.toString());
									win.jobMeta.clearChanged();
								}
								else
								{
                                    log.logError(APP_NAME, Messages.getString("Chef.Log.Error.VerifyingUsernamePassword")); //$NON-NLS-1$
									win.rep=null;
								}
							}
							else
							{
                                log.logError(APP_NAME, Messages.getString("Chef.Log.Error.ConnectingToRepository")); //$NON-NLS-1$
							}
						}
						else
						{
                            log.logError(APP_NAME, Messages.getString("Chef.Log.Error.NoRepositoryProvidedCanNotLoadJob")); //$NON-NLS-1$
						}
					}
					else
					{
                        log.logError(APP_NAME, Messages.getString("Chef.Log.Error.NoRepositoriesOnThisSystem")); //$NON-NLS-1$
					}
				}
				else
				if (!Const.isEmpty(optionFilename))
				{
					win.jobMeta = new JobMeta(log, optionFilename.toString(), win.rep);
					win.jobMeta.clearChanged();
				}
			} // Nothing on commandline...
			else
			{
				// Can we connect to the repository?
				if (repinfo!=null && userinfo!=null)
				{
					win.rep = new Repository(log, repinfo, userinfo);
					if (!win.rep.connect(Messages.getString("Chef.Application.Name"))) //$NON-NLS-1$
					{
						win.rep = null;
					}
				}
	
				if (win.props.openLastFile())
				{
					String lastfiles[]  = win.props.getLastFiles();
					String lastdirs[]   = win.props.getLastDirs();
					boolean lasttypes[] = win.props.getLastTypes();
					String lastrepos[]  = win.props.getLastRepositories();
			
					if (lastfiles.length>0)
					{
						boolean use_repository = win.rep!=null;
	
						if (use_repository || !lasttypes[0])
						{
							if (win.rep!=null) // load from repository...
							{
								if (win.rep.getName().equalsIgnoreCase(lastrepos[0]))
								{
									RepositoryDirectory repdir = win.rep.getDirectoryTree().findDirectory(lastdirs[0]);
									if (repdir!=null)
									{
                                        log.logDetailed(APP_NAME, Messages.getString("Chef.Log.AutoLoading")+lastfiles[0]+Messages.getString("Chef.Log.AutoLoading2")+repdir.getPath()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
										// win.jobinfo = new JobInfo(log, win.rep, lastfiles[0], repdir);
										JobLoadProgressDialog jlpd = new JobLoadProgressDialog(log, win.props, win.shell, win.rep, lastfiles[0], repdir);
										JobMeta jobInfo = jlpd.open();
										if (jobInfo!=null)
										{
											win.jobMeta = jobInfo;
											win.setFilename(lastfiles[0]);
										}
									}
								}
							}
							else // Load from XML?
							{
								win.jobMeta = new JobMeta(log, lastfiles[0], win.rep);
							}
							win.setFilename(lastfiles[0]);
						}
						win.jobMeta.clearChanged();
					}
				}
			}

		}
		catch(KettleException ke)
		{
            log.logError(APP_NAME, Messages.getString("Chef.Log.Error.ErrorLoadingJob")+ke.getMessage()); //$NON-NLS-1$
		}

		win.open ();
		splash.dispose();
		
		while (!win.isDisposed ()) 
		{
			if (!win.readAndDispatch ()) win.sleep ();
		}
		win.dispose();
		
        log.logBasic(APP_NAME, APP_NAME+Messages.getString("Chef.Log.ApplicationHasEnded")); //$NON-NLS-1$

		// Close the logfile...
		log.close();
        
        // Kill all remaining things in this VM!
       System.exit(0);
	}

    /**
     * @return Returns the jobMeta.
     */
    public JobMeta getJobMeta()
    {
        return jobMeta;
    }

    /**
     * @param jobMeta The jobMeta to set.
     */
    public void setJobMeta(JobMeta jobMeta)
    {
        this.jobMeta = jobMeta;
    }
}
