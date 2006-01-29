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
import be.ibridge.kettle.core.dialog.Splash;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.wizards.createdatabase.CreateDatabaseWizardPage1;
import be.ibridge.kettle.core.wizards.createdatabase.CreateDatabaseWizardPage2;
import be.ibridge.kettle.core.wizards.createdatabase.CreateDatabaseWizardPageInformix;
import be.ibridge.kettle.core.wizards.createdatabase.CreateDatabaseWizardPageJDBC;
import be.ibridge.kettle.core.wizards.createdatabase.CreateDatabaseWizardPageOCI;
import be.ibridge.kettle.core.wizards.createdatabase.CreateDatabaseWizardPageODBC;
import be.ibridge.kettle.core.wizards.createdatabase.CreateDatabaseWizardPageOracle;
import be.ibridge.kettle.job.JobEntryLoader;
import be.ibridge.kettle.job.JobHopMeta;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.JobPlugin;
import be.ibridge.kettle.job.dialog.JobDialog;
import be.ibridge.kettle.job.dialog.JobLoadProgressDialog;
import be.ibridge.kettle.job.dialog.JobSaveProgressDialog;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryCopy;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.job.entry.sql.JobEntrySQL;
import be.ibridge.kettle.job.entry.trans.JobEntryTrans;
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
    public static final String APP_NAME = "Chef";

	private LogWriter log;
	public  Display disp;
	private Shell shell;
	private boolean destroy;
	public  Props props;

	public  Repository rep;
	
	public  boolean demo_mode;
	public  int     license_nr;
	
	public  JobMeta jobMeta;
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

	public static final String STRING_CONNECTIONS       = "Connections";
    public static final String STRING_JOBENTRIES        = "Job-entries";
    public static final String STRING_BASE_JOBENTRIES   = "Base job-entry types";
    public static final String STRING_PLUGIN_JOBENTRIES = "Plugin job-entry types";
    
	public static final String STRING_SPECIAL      = JobEntryInterface.type_desc_long[JobEntryInterface.TYPE_JOBENTRY_SPECIAL];
	
	private static final String APPL_TITLE      = "Chef : The Job Editor";

	private static final String STRING_DEFAULT_EXT    = ".kjb";
	private static final String STRING_FILTER_EXT  [] = { "*.kjb;*.xml", "*.xml", "*.*" };
	private static final String STRING_FILTER_NAMES[] = { "Kettle Jobs", "XML files", "All files" };
	
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
		MenuItem mFile = new MenuItem(mBar, SWT.CASCADE); mFile.setText("&File");
		  msFile = new Menu(shell, SWT.DROP_DOWN);
		  mFile.setMenu(msFile);
		  MenuItem miFileNew    = new MenuItem(msFile, SWT.CASCADE); miFileNew.setText("&New\tCTRL-N");
		  MenuItem miFileOpen   = new MenuItem(msFile, SWT.CASCADE); miFileOpen.setText("&Open\tCTRL-O");
		  MenuItem miFileSave   = new MenuItem(msFile, SWT.CASCADE); miFileSave.setText("&Save\tCTRL-S");
		  MenuItem miFileSaveAs = new MenuItem(msFile, SWT.CASCADE); miFileSaveAs.setText("Save &as...");
		  new MenuItem(msFile, SWT.SEPARATOR);
		  MenuItem miFilePrint  = new MenuItem(msFile, SWT.CASCADE); miFilePrint.setText("&Print\tCTRL-P");
		  new MenuItem(msFile, SWT.SEPARATOR);
		  MenuItem miFileQuit   = new MenuItem(msFile, SWT.CASCADE); miFileQuit.setText("&Quit");
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

		MenuItem mEdit = new MenuItem(mBar, SWT.CASCADE); mEdit.setText("&Edit");
		Menu msEdit = new Menu(shell, SWT.DROP_DOWN);
		mEdit.setMenu(msEdit);
		miEditUndo         = new MenuItem(msEdit, SWT.CASCADE);
		miEditRedo         = new MenuItem(msEdit, SWT.CASCADE);
		setUndoMenu();
		new MenuItem(msEdit, SWT.SEPARATOR);
		MenuItem miEditUnselectAll  = new MenuItem(msEdit, SWT.CASCADE); miEditUnselectAll.setText("&Clear selection\tESC");
		MenuItem miEditSelectAll    = new MenuItem(msEdit, SWT.CASCADE); miEditSelectAll.setText("&Select all steps\tCTRL-A");
		new MenuItem(msEdit, SWT.SEPARATOR);
		MenuItem miEditOptions      = new MenuItem(msEdit, SWT.CASCADE); miEditOptions.setText("&Options...");

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
	    MenuItem mRep = new MenuItem(mBar, SWT.CASCADE); mRep.setText("&Repository");
		  Menu msRep = new Menu(shell, SWT.DROP_DOWN);
		  mRep.setMenu(msRep);
		  MenuItem miRepConnect    = new MenuItem(msRep, SWT.CASCADE); miRepConnect.setText("&Connect to repository \tCTRL-R");
		  MenuItem miRepDisconnect = new MenuItem(msRep, SWT.CASCADE); miRepDisconnect.setText("&Disconnect repository \tCTRL-D");
		  MenuItem miRepExplore    = new MenuItem(msRep, SWT.CASCADE); miRepExplore.setText("&Explore repository \tCTRL-E");
		  new MenuItem(msRep, SWT.SEPARATOR);
		  MenuItem miRepUser       = new MenuItem(msRep, SWT.CASCADE); miRepUser.setText("&Edit current user\tCTRL-U");
		
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
		mJob.setText("&Job");
		Menu msJob = new Menu(shell, SWT.DROP_DOWN);
		mJob.setMenu(msJob);
		MenuItem miJobRun           = new MenuItem(msJob, SWT.CASCADE);   miJobRun.setText("&Run...\tF9");
		new MenuItem(msJob, SWT.SEPARATOR);
		MenuItem miJobCopy = new MenuItem(msJob, SWT.CASCADE); 		      miJobCopy.setText("&Copy job to clipboard");
		new MenuItem(msJob, SWT.SEPARATOR);
		MenuItem miJobInfo          = new MenuItem(msJob, SWT.CASCADE);   miJobInfo.setText("&Settings...\tCTRL-J");
		
		Listener lsJobInfo        = new Listener() { public void handleEvent(Event e) { setJob();  } };
		miJobInfo.addListener (SWT.Selection, lsJobInfo );
		Listener lsJobCopy        = new Listener() { public void handleEvent(Event e) { toClipboard(XMLHandler.getXMLHeader() + jobMeta.getXML()); } };
		miJobCopy.addListener(SWT.Selection, lsJobCopy );
		
		
		// Wizard menu
		MenuItem mWizard = new MenuItem(mBar, SWT.CASCADE); mWizard.setText("&Wizard");
		  Menu msWizard = new Menu(shell, SWT.DROP_DOWN );
		  mWizard.setMenu(msWizard);

		  MenuItem miWizardNewConnection = new MenuItem(msWizard, SWT.CASCADE); 
		  miWizardNewConnection.setText("&Create database connection wizard...\tF3");
		  Listener lsWizardNewConnection= new Listener() { public void handleEvent(Event e) { createDatabaseWizard();  } };
		  miWizardNewConnection.addListener(SWT.Selection, lsWizardNewConnection);

		  MenuItem miWizardRipDatabase = new MenuItem(msWizard, SWT.CASCADE); 
		  miWizardRipDatabase.setText("&Copy tables wizard...\tF10");
		  Listener lsWizardRipDatabase= new Listener() { public void handleEvent(Event e) { ripDBWizard();  } };
		  miWizardRipDatabase.addListener(SWT.Selection, lsWizardRipDatabase);

		/*
		 * Help menu
		 * 
		 */		

		MenuItem mHelp = new MenuItem(mBar, SWT.CASCADE); mHelp.setText("&Help");
		  Menu msHelp = new Menu(shell, SWT.DROP_DOWN);
		  mHelp.setMenu(msHelp);
		  MenuItem miHelpAbout       = new MenuItem(msHelp, SWT.CASCADE); miHelpAbout.setText("&About");
		
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
		  String repository = ( lr[i]!=null && lr[i].length()>0 ) ? ( "["+lr[i]+"] " ) : "";
		  String filename = RepositoryDirectory.DIRECTORY_SEPARATOR + lf[i];
		  if (!lt[i]) filename = lf[i];
		  
		  if (!ld[i].equals(RepositoryDirectory.DIRECTORY_SEPARATOR))
		  {
		  	filename=ld[i]+filename;
		  }
		  if (i<9)
		  {
		  	miFileLast.setAccelerator(accel);
			miFileLast.setText("&"+chr+"  "+repository+filename+ "\tCTRL-"+chr);
		  }
		  else
		  {
		  	miFileLast.setText("   "+repository+filename);
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
									mb.setMessage("I was unable to connect to this repository!");
									mb.setText("Error!");
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
			      						throw new KettleException("The directory specified doesn't exist: "+fd);
			      					}
								}
			      				catch(KettleException ke)
								{
									jobMeta.clear();
									new ErrorDialog(shell, props, "Error loading job", "I was unable to load this job from the repository!", ke);
								}
				      		}
				      		else
				      		{
								jobMeta.clear();
			      				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
								mb.setMessage("Can't load this job.  Please connect to the correct repository first.");
								mb.setText("Error!");
								mb.open();
				      		}
				      	}
				      	else
				      		// Load from XML!
				      	{
				      		try
							{
				      			jobMeta = new JobMeta(log, fn);
							}
				      		catch(KettleException ke)
							{
								jobMeta.clear();
								new ErrorDialog(shell, props, "Error loading job", "I was unable to load this job from the XML file!", ke);
							}
				      	}
				      	
				      	refreshTree();
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
		final Image imFileNew = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"new.png")); 
		tiFileNew.setImage(imFileNew);
		tiFileNew.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newFile(); }});
		tiFileNew.setToolTipText("New file, clear all settings");

		final ToolItem tiFileOpen = new ToolItem(tBar, SWT.PUSH);
		final Image imFileOpen = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"open.png")); 
		tiFileOpen.setImage(imFileOpen);
		tiFileOpen.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { openFile(false); }});
		tiFileOpen.setToolTipText("Open file");

		final ToolItem tiFileSave = new ToolItem(tBar, SWT.PUSH);
		final Image imFileSave = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"save.png")); 
		tiFileSave.setImage(imFileSave);
		tiFileSave.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { saveFile(); }});
		tiFileSave.setToolTipText("Save current file");

		final ToolItem tiFileSaveAs = new ToolItem(tBar, SWT.PUSH);
		final Image imFileSaveAs = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"saveas.png")); 
		tiFileSaveAs.setImage(imFileSaveAs);
		tiFileSaveAs.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { saveFileAs(); }});
		tiFileSaveAs.setToolTipText("Save file with different name");

		new ToolItem(tBar, SWT.SEPARATOR);
		final ToolItem tiFilePrint = new ToolItem(tBar, SWT.PUSH);
		final Image imFilePrint = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"print.png")); 
		tiFilePrint.setImage(imFilePrint);
		tiFilePrint.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { printFile(); }});
		tiFilePrint.setToolTipText("Print");

		new ToolItem(tBar, SWT.SEPARATOR);
		final ToolItem tiFileRun = new ToolItem(tBar, SWT.PUSH);
		final Image imFileRun = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"run.png")); 
		tiFileRun.setImage(imFileRun);
		tiFileRun.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { tabfolder.setSelection(1); cheflog.startstop(); }});
		tiFileRun.setToolTipText("Run this job");

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
            if (!plugin.getID().equals("SPECIAL"))
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
		log.logDebug(toString(), "Clicked on  "+ti.getText());
		
		Menu mCSH = new Menu(shell, SWT.POP_UP);

		// Find the level we clicked on: Top level (only NEW in the menu) or below (edit, insert, ...)
		TreeItem parent = ti.getParentItem();
		if (parent==null) // Top level
		{
			MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText("New");
			miNew.addListener( SWT.Selection, lsNew );
		}
		else
		{
			String section = parent.getText();
			if (section.equalsIgnoreCase(STRING_CONNECTIONS))
			{
				MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText("New");
				MenuItem miEdit = new MenuItem(mCSH, SWT.PUSH); miEdit.setText("Edit");
				MenuItem miDupe = new MenuItem(mCSH, SWT.PUSH); miDupe.setText("Duplicate");
				MenuItem miDel  = new MenuItem(mCSH, SWT.PUSH); miDel.setText("Delete");
				new MenuItem(mCSH, SWT.SEPARATOR);
				MenuItem miSQL  = new MenuItem(mCSH, SWT.PUSH); miSQL.setText("SQL Editor");
				MenuItem miCache= new MenuItem(mCSH, SWT.PUSH); miCache.setText("Clear DB Cache of "+ti.getText());
				new MenuItem(mCSH, SWT.SEPARATOR);
				MenuItem miExpl = new MenuItem(mCSH, SWT.PUSH); miExpl.setText("Explore");
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
				MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText("New");
				MenuItem miEdit = new MenuItem(mCSH, SWT.PUSH); miEdit.setText("Edit");
				MenuItem miDupe = new MenuItem(mCSH, SWT.PUSH); miDupe.setText("Duplicate");
				MenuItem miDel  = new MenuItem(mCSH, SWT.PUSH); miDel.setText("Delete");
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

		tiTabsGraph = new CTabItem(tabfolder, SWT.NONE); tiTabsGraph.setText("Graphical view");
		tiTabsGraph.setToolTipText("Displays the job graphically.");
		tiTabsList  = new CTabItem(tabfolder, SWT.NULL); tiTabsList.setText("Log view");
		tiTabsList.setToolTipText("Displays the log of the running job.");
		
		chefgraph = new ChefGraph(tabfolder, SWT.V_SCROLL | SWT.H_SCROLL | SWT.NO_BACKGROUND, this);
		cheflog   = new ChefLog(tabfolder, SWT.NONE, this);
				
		tiTabsGraph.setControl(chefgraph);
		tiTabsList.setControl(cheflog);
		
		tabfolder.setSelection(0);
				
		sashform.addKeyListener(defKeys);
	}

	public String getRepositoryName()
	{
		return rep==null?"":rep.getName();
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
					newChefGraphEntry(name);
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
					JobEntryCopy getjge = jobMeta.findJobEntry(name, 0);
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
			log.logDebug(toString(), "(DELETE) Trying to delete #"+i+"/"+(ti.length-1)+" : "+name[i]);
			if (parent[i] != null)
			{
				deleteChefGraphEntry(name[i]);
				//String type = parent[i].getText();
				//log.logBasic(toString(), "Delete JobEntry ["+name[i]+"] of type ["+parent[i].getText()+"]");
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
				SQLEditor sql = new SQLEditor(shell, SWT.NONE, ci, jobMeta.dbcache, "");
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
			String dupename = "(copy of) "+name; 
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

	public void delConnection(String name)
	{
		DatabaseMeta db = jobMeta.findDatabase(name);
		int pos = jobMeta.indexOfDatabase(db);
		if (db!=null)
		{
			addUndoDelete(new DatabaseMeta[] { (DatabaseMeta)db.clone() }, new int[] { pos });
			jobMeta.removeDatabase(pos);
			
			// Also add to repository?
			if (rep!=null)
			{
				if (!rep.getUserInfo().isReadonly())
				{
					try
					{
						long id_database = rep.getDatabaseID(db.getName());
						rep.delDatabase(id_database);
					}
					catch(KettleDatabaseException dbe)
					{
						new ErrorDialog(shell, props, "Can't delete", "Error deleting connection ["+db+"] from repository :"+dbe.getMessage());
					}
				}
				else
				{
					new ErrorDialog(shell, props, "Can't delete", "Error deleting connection ["+db+"] from repository: user is read-only!");
				}
			}
			
			refreshTree();
		}
		setShellText();
	}
		
	public void newJobHop(JobEntryCopy fr, JobEntryCopy to)
	{
		log.logBasic(toString(), "new JobHop("+fr.getName()+", "+to.getName()+")");
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
			mb.setMessage("This model has changed.  Are you sure you want open a new file?");
			mb.setText("Warning!");
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
			if (!rep.connect("Spoon"))
			{
				rep=null;
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage("An error occured connecting to the repository!"+Const.CR+"See the log for more information.");
				mb.setText("Error!");
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
						mb.setMessage("Sorry, I was unable to change this user in the repository: "+Const.CR+e.getMessage());
						mb.setText("Edit user");
						mb.open();
					}
			 	}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
				mb.setMessage("Sorry, you are not allowed to change this user.");
				mb.setText("Edit user");
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
						mb.setMessage("Couldn't find connection, please refresh the tree (F5)!");
						mb.setText("Error!");
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
                        jobMeta = new JobMeta(log, fname);
                        props.addLastFile(Props.TYPE_PROPERTIES_CHEF, fname, null, false, null);
                        addMenuLast();
                    }
                    catch(KettleXMLException xe)
                    {
                        jobMeta.clear();
                        MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
                        mb.setMessage("Error opening : "+fname+Const.CR+xe.getMessage());
                        mb.setText("Error!");
                        mb.open();
                    }

                    refreshGraph();
                    refreshTree(true);
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
                }
            }
        }
    }
        
	public void newFile()
	{
		// AYS: Y/N??
		MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_WARNING );
		mb.setMessage("Are you sure you want to clear all information?");
		mb.setText("Warning!");
		int answer = mb.open();
		
		if (answer == SWT.YES)
		{ 
			jobMeta.clear();
			loadRepositoryObjects();    // Add databases if connected to repository
			setFilename(null);
			refreshTree();
			refreshGraph();
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
        
        log.logDetailed(toString(), "Quit application.");
        saveSettings();
        if (jobMeta.hasChanged())
        {
            MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_WARNING );
            mb.setMessage("File has changed!  Do you want to save first?");
            mb.setText("Warning!");
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
            String message = "Are you sure you want to exit?"; 
            if (cheflog.isRunning()) message = "There is a running job.  Are you sure you want to exit?";
            
            MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
                    "Warning!", 
                    null,
                    message,
                    MessageDialog.WARNING,
                    new String[] { "Yes", "No" },
                    1,
                    "Please, don't show this warning anymore.",
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
		log.logDetailed(toString(), "Save file...");
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
		log.logDetailed(toString(), "Save to repository...");
		if (rep!=null)
		{
			boolean answer = true;
			boolean ask    = ask_name;
			while (answer && ( ask || jobMeta.getName()==null || jobMeta.getName().length()==0 ) )
			{
				if (!ask)
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					mb.setMessage("Please give this job a name before saving it in the database.");
					mb.setText("This job has no name.");
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
						mb.setMessage("There already is a job called ["+jobMeta.getName()+"] in the repository."+Const.CR+"Do you want to overwrite the job?");
						mb.setText("Overwrite?");
						response = mb.open();
					}
					
					if (response == SWT.YES)
					{
						// Keep info on who & when this transformation was changed...
						jobMeta.modified_date = new Value("MODIFIED_DATE", Value.VALUE_TYPE_DATE); 				
						jobMeta.modified_date.sysdate();
						jobMeta.modified_user = rep.getUserInfo().getLogin();

						JobSaveProgressDialog jspd = new JobSaveProgressDialog(log, props, shell, rep, jobMeta);
						if (jspd.open())
						{
							if (!props.getSaveConfirmation())
							{
								MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
																						 "Save OK!", 
																						 null,
																						 "This job was stored in repository",
																						 MessageDialog.QUESTION,
																						 new String[] { "OK!" },
																						 0,
																						 "Don't show this message again.",
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
					mb.setMessage("Sorry, the user you're logged on with, can only read from the repository");
					mb.setText("Job not saved!");
					mb.open();
				}
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage("There is no repository connection available.");
			mb.setText("No repository available.");
			mb.open();
		}
	}

	public void saveFileAs()
	{
		log.logBasic(toString(), "Save file as...");

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
				mb.setMessage("This file already exists.  Do you want to overwrite it?");
				mb.setText("This file already exists!");
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
			props.addLastFile(Props.TYPE_PROPERTIES_CHEF, fname, RepositoryDirectory.DIRECTORY_SEPARATOR, false, "");
			saveSettings();
			addMenuLast();

			log.logDebug(toString(), "File written to ["+fname+"]");
			jobMeta.setFilename( fname );
			jobMeta.clearChanged();
			setShellText();
		}
		catch(Exception e)
		{
			log.logDebug(toString(), "Error opening file for writing! --> "+e.toString());
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage("Error saving file:"+Const.CR+e.toString());
			mb.setText("ERROR");
			mb.open();
		}
	}
	
	public void helpAbout()
	{
		MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION | SWT.CENTER);
		String mess = "Kettle - Chef version "+Const.VERSION+Const.CR+Const.CR+Const.CR;
		mess+="(c) 2001-2004 i-Bridge bvba"+Const.CR+"         www.kettle.be"+Const.CR;
		        
        mb.setMessage(mess);
		mb.setText("Chef");
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
		log.logDetailed(toString(), "refreshTree() called");
	
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
					log.logDetailed(toString(), "Saved database connection ["+db+"] to the repository.");
				}
				catch(KettleException ke)
				{
					new ErrorDialog(shell, props, "Error saving connection", "Error saving connection ["+db+"] to repository!", ke); 
				}
			}
			else
			{
				new ErrorDialog(shell, props, "Error saving connection", "Can't save database connection!", new KettleException("This repository user is read-only!")); 
			}
		}
	}


	public JobEntryCopy newChefGraphEntry(String type_desc)
	{
		try
		{
			int type = JobEntryBase.getType(type_desc);
			
			if (type!=JobEntryInterface.TYPE_JOBENTRY_NONE)
			{
				// System.out.println("new job entry of type: "+type+" ["+type_desc+"]");
				
				// Determine name & number for this entry.
				String basename = type_desc;
				int nr = jobMeta.generateJobEntryNameNr(basename);
				String entry_name = basename+" "+nr;
				
				// Generate the appropriate class...
				JobEntryInterface jei = JobEntryBase.newJobEntryInterface(type);
				jei.setName(entry_name);
		
				JobEntryDialogInterface d = JobEntryBase.newJobEntryDialog(shell, jei, rep, jobMeta);               
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
				return null;
			}
		}
		catch(Throwable e)
		{
			new ErrorDialog(shell, props, "Severe error creating resources.", "Error creating new chefgraphentry",new Exception(e)); 
			return null;
		}
	}
		
	public void editChefGraphEntry(JobEntryCopy je)
	{
        try
        {
    		log.logBasic(toString(), "edit job graph entry: "+je.getName());
    		
    		JobEntryCopy before =(JobEntryCopy)je.clone_deep();
    		boolean entry_changed=false;
    		
    		JobEntryInterface jei = je.getEntry();
    		
    		JobEntryDialogInterface d = JobEntryBase.newJobEntryDialog(shell, jei, rep, jobMeta);
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
    			mb.setMessage("This job entry can't be changed!");
    			mb.setText("Sorry...");
    			mb.open();
    		}

        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, props, "Error editing job entry", "Error editing job entry", e);
        }
	}

	
	public JobEntryTrans newJobEntry(int type)
	{
		JobEntryTrans je = new JobEntryTrans();
		je.setType(type);
		String basename = JobEntryTrans.type_desc_long[type]; 
		int nr = jobMeta.generateJobEntryNameNr(basename);
		je.setName(basename+" "+nr);

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
		JobEntryCopy jge = jobMeta.findJobEntry(name, 0);
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
		xml+="<jobentries>"+Const.CR;

		for (int i=0;i<jec.length;i++)
		{
			xml+=jec[i].getXML();
		}
		
		xml+="    </jobentries>"+Const.CR;
		
		toClipboard(xml);
	}

	public void pasteSteps(String clipcontent, Point loc)
	{
		try
		{
			Document doc = XMLHandler.loadXMLString(clipcontent);

			// De-select all, re-select pasted steps...
			jobMeta.unselectAll();
			
			Node entriesnode = XMLHandler.getSubNode(doc, "jobentries");
			int nr = XMLHandler.countNodes(entriesnode, "entry");
			log.logDebug(toString(), "I found "+nr+" job entries to paste on location: "+loc);
			JobEntryCopy entries[] = new JobEntryCopy[nr];
			
			//Point min = new Point(loc.x, loc.y);
			Point min = new Point(99999999,99999999);
			
			for (int i=0;i<nr;i++)
			{
				Node entrynode = XMLHandler.getSubNodeByNr(entriesnode, "entry", i);
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
		    new ErrorDialog(shell, props, "Error pasting job entries...", "I was unable to paste job entries to this job", e);
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
			String repository = "["+getRepositoryName()+"]";
			String transname  = jobMeta.getName();
			if (transname==null) transname="[no name]";
			shell.setText(APPL_TITLE+" - "+repository+"   "+transname+(jobMeta.hasChanged()?" (changed)":""));
		}
		else
		{
			String repository = "[no repository]";
			if (fname!=null)
			{
				shell.setText(APPL_TITLE+" - "+repository+"   File: "+fname+(jobMeta.hasChanged()?" (changed)":""));
			}
			else
			{
				shell.setText(APPL_TITLE+" - "+repository+"   "+(jobMeta.hasChanged()?" (changed)":""));
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
				monitor.beginTask("Building new job...", tables.length);
				monitor.worked(0);
				JobEntryCopy previous = start;
				
				// Loop over the table-names...
				for (int i=0;i<tables.length && !monitor.isCanceled();i++)
				{
					monitor.setTaskName("Processing table ["+tables[i]+"]...");
					//
					// Create the new transformation...
					//
					String transname = "copy ["+sourceDbInfo+"].["+tables[i]+"] to ["+targetDbInfo+"]";
					
					TransMeta ti = new TransMeta((String)null, transname, null);
					
					ti.setDirectory( repdir );
					
					//
					// Add a note
					//
					String note = "Reads information from table ["+tables[i]+"] on database ["+sourceDbInfo+"]"+Const.CR;
					note+="After that, it writes the information to table ["+tables[i]+"] on database ["+targetDbInfo+"]";
					NotePadMeta ni = new NotePadMeta(note, 150, 10, -1, -1);
					ti.addNote(ni);
					
					//
					// Add the TableInputMeta step...
					// 
					String fromstepname = "read from ["+tables[i]+"]";
					TableInputMeta tii = new TableInputMeta();
					tii.setDatabaseMeta( sourceDbInfo );
					tii.setSQL( "SELECT * FROM "+tables[i] );
					
					String fromstepid = StepLoader.getInstance().getStepPluginID(tii);
					StepMeta fromstep = new StepMeta(log, fromstepid, fromstepname, (StepMetaInterface)tii );
					fromstep.setLocation(150,100);
					fromstep.setDraw(true);
					fromstep.setDescription("Reads information from table ["+tables[i]+"] on database ["+sourceDbInfo+"]");
					ti.addStep(fromstep);
					
					//
					// Add the TableOutputMeta step...
					//
					String tostepname = "write to ["+tables[i]+"]";
					TableOutputMeta toi = new TableOutputMeta();
					toi.setDatabase( targetDbInfo );
					toi.setTablename( tables[i] );
					toi.setCommitSize( 100 );
					toi.setTruncateTable( true );
					
					String tostepid = StepLoader.getInstance().getStepPluginID(toi);
					StepMeta tostep = new StepMeta(log, tostepid, tostepname, (StepMetaInterface)toi );
					tostep.setLocation(500,100);
					tostep.setDraw(true);
					tostep.setDescription("Write information to table ["+tables[i]+"] on database ["+targetDbInfo+"]");
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
					String sql = "";
					try
					{
						sql = ti.getSQLStatementsString();
					}
					catch(KettleStepException kse)
					{
						throw new InvocationTargetException(kse, "Error getting SQL from transformation ["+ti+"] : "+kse.getMessage());
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
						throw new InvocationTargetException(dbe, "Unable to save transformation to the repository");
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
						String jesqlname = "Create table ["+tables[i]+"]";
						JobEntrySQL jesql = new JobEntrySQL(jesqlname);
						jesql.setDatabase(targetDbInfo);
						jesql.setSQL(sql);
						jesql.setDescription("This executes the SQL to create table ["+targetDbInfo+"].["+tables[i]+"]");
						
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
					String jetransname = "Copy data to ["+tables[i]+"]";
					JobEntryTrans jetrans = new JobEntryTrans(jetransname);
					jetrans.setTransname(ti.getName());
					jetrans.setDirectory(ti.getDirectory());
					
					JobEntryCopy jectrans = new JobEntryCopy(log, jetrans);
					jectrans.setDescription("This job entry executes the transformation to copy data"+Const.CR+"from: ["+sourceDbInfo+"].["+tables[i]+"]"+Const.CR+"to:   ["+targetDbInfo+"].["+tables[i]+"]");
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
			new ErrorDialog(shell, props, "Error ripping database", "An error occured ripping the database!", e);
			return false;
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, props, "Error ripping database", "An error occured ripping the database!", e);
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
		final RipDatabaseWizardPage1 page1 = new RipDatabaseWizardPage1("1", jobMeta.databases);
		page1.createControl(shell);
		final RipDatabaseWizardPage2 page2 = new RipDatabaseWizardPage2 ("2");
		page2.createControl(shell);
		final RipDatabaseWizardPage3 page3 = new RipDatabaseWizardPage3 ("3", rep);
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
		final DatabaseMeta newDBInfo = new DatabaseMeta();
		
		final CreateDatabaseWizardPage1 page1 = new CreateDatabaseWizardPage1("1", props, newDBInfo, jobMeta.getDatabases());
		page1.createControl(shell);
		
		final  CreateDatabaseWizardPageInformix pageifx = new CreateDatabaseWizardPageInformix("ifx", props, newDBInfo);
		pageifx.createControl(shell);
		
		final  CreateDatabaseWizardPageJDBC pagejdbc = new CreateDatabaseWizardPageJDBC("jdbc", props, newDBInfo);
		pagejdbc.createControl(shell);
		
		final  CreateDatabaseWizardPageOCI pageoci = new CreateDatabaseWizardPageOCI("oci", props, newDBInfo);
		pageoci.createControl(shell);
		
		final CreateDatabaseWizardPageODBC pageodbc = new CreateDatabaseWizardPageODBC("odbc", props, newDBInfo);
		pageodbc.createControl(shell);
		
		final CreateDatabaseWizardPageOracle pageoracle = new CreateDatabaseWizardPageOracle("oracle", props, newDBInfo);

		final CreateDatabaseWizardPage2 page2 = new CreateDatabaseWizardPage2("2", props, newDBInfo);
		page2.createControl(shell);

		Wizard wizard = new Wizard() 
		{
			public boolean performFinish() 
			{
				jobMeta.addDatabase(newDBInfo);
				refreshTree(true);
				refreshGraph();
				return true;
			}
			
			/**
			 * @see org.eclipse.jface.wizard.Wizard#canFinish()
			 */
			public boolean canFinish()
			{
				return page2.canFinish();
			}
		};
				
		wizard.addPage(page1);
		wizard.addPage(pageoci);
		wizard.addPage(pageodbc);
		wizard.addPage(pagejdbc);
		wizard.addPage(pageoracle);
		wizard.addPage(pageifx);
		wizard.addPage(page2);
				
		WizardDialog wd = new WizardDialog(shell, wizard);
		wd.setMinimumPageSize(700,400);
		wd.open();
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
			miEditUndo.setText("Undo : "+prev.toString()+" \tCTRL-Z");
		} 
		else            
		{
			miEditUndo.setEnabled(false);
			miEditUndo.setText("Undo : not available \tCTRL-Z");
		} 

		if (next!=null) 
		{
			miEditRedo.setEnabled(true);
			miEditRedo.setText("Redo : "+next.toString()+" \tCTRL-Y");
		} 
		else            
		{
			miEditRedo.setEnabled(false);
			miEditRedo.setText("Redo : not available \tCTRL-Y");			
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
	
	public static void main (String [] a)
	{
	    ArrayList args = new ArrayList();
	    for (int i=0;i<a.length;i++) args.add(a[i]);

		Display display = new Display();
		Splash splash = new Splash(display);
		
		// System.out.println("Welcome to Chef!");
		String repname   = Const.getCommandlineOption(args, "rep");
		String username  = Const.getCommandlineOption(args, "user");
		String password  = Const.getCommandlineOption(args, "pass");
		String jobname   = Const.getCommandlineOption(args, "job");
		String filename  = Const.getCommandlineOption(args, "file");
		String dirname   = Const.getCommandlineOption(args, "dir");
        String logfile   = Const.getCommandlineOption(args, "log");

        String kettleRepname  = Const.getEnvironmentVariable("KETTLE_REPOSITORY", null);
        String kettleUsername = Const.getEnvironmentVariable("KETTLE_USER", null);
        String kettlePassword = Const.getEnvironmentVariable("KETTLE_PASSWORD", null);
        
        if (kettleRepname !=null && kettleRepname .length()>0) repname  = kettleRepname;
        if (kettleUsername!=null && kettleUsername.length()>0) username = kettleUsername;
        if (kettlePassword!=null && kettlePassword.length()>0) password = kettlePassword;

		// if (args.length==1 && filename==null) filename=args[1]; // try to load first argument...

		Locale.setDefault(Const.DEFAULT_LOCALE);
				
        LogWriter log;
        if (logfile==null)
        {
            log=LogWriter.getInstance(Const.SPOON_LOG_FILE, false, LogWriter.LOG_LEVEL_BASIC);
        }
        else
        {
            log=LogWriter.getInstance( logfile, true, LogWriter.LOG_LEVEL_BASIC );
        }
        
        if (log.getRealFilename()!=null) log.logBasic(APP_NAME, "Logging goes to "+log.getRealFilename());
        		
		/* Load the plugins etc.*/
		StepLoader stloader = StepLoader.getInstance();
		if (!stloader.read())
		{
			log.logError(APP_NAME, "Error loading steps... halting Chef!");
			return;
		}
        
        /* Load the plugins etc.*/
        JobEntryLoader jeloader = JobEntryLoader.getInstance();
        if (!jeloader.read())
        {
            log.logError(APP_NAME, "Error loading job entries & plugins... halting Chef!");
            return;
        }


		final Chef win = new Chef(log, display,  null);
		win.setDestroy(true);
		
		log.logDetailed(APP_NAME, "Main window is created.");
        
        // Check license info!
		win.demo_mode=false;
		
		RepositoryMeta repinfo = null;
		UserInfo userinfo = null;
		
		if (repname==null && filename==null && win.props.showRepositoriesDialogAtStartup())
		{		
            log.logDetailed(APP_NAME, "Asking for repository");

			int perms[] = new int[] { PermissionMeta.TYPE_PERMISSION_JOB };
			splash.hide();
			RepositoriesDialog rd = new RepositoriesDialog(win.disp, SWT.NONE, perms, "Chef");
			if (rd.open())
			{
				repinfo = rd.getRepository();
				userinfo = rd.getUser();
				if (!userinfo.useJobs())
				{
					MessageBox mb = new MessageBox(win.shell, SWT.OK | SWT.ICON_ERROR );
					mb.setMessage("Sorry, this repository user can't work with jobs from the repository.");
					mb.setText("Error!");
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
			if (repname!=null || filename!=null)
			{
				if (repname!=null)
				{
					RepositoriesMeta repsinfo = new RepositoriesMeta(log);
					if (repsinfo.readData())
					{
						repinfo = repsinfo.findRepository(repname);
						if (repinfo!=null)
						{
							// Define and connect to the repository...
							win.rep = new Repository(log, repinfo, userinfo);
							if (win.rep.connect("Chef"))
							{
								if (dirname==null) dirname=RepositoryDirectory.DIRECTORY_SEPARATOR;
	
                                // Check username, password
                                try
                                {
                                    win.rep.userinfo = new UserInfo(win.rep, username, Const.NVL(password, ""));
                                    
                                    if (jobname!=null && dirname!=null)
                                    {
                                        RepositoryDirectory repdir = win.rep.getDirectoryTree().findDirectory(dirname);
                                        if (repdir!=null)
                                        {
                                            win.jobMeta = new JobMeta(log, win.rep, jobname, repdir);
                                            win.setFilename(repname);
                                            win.jobMeta.clearChanged();
                                        }
                                        else
                                        {
                                            log.logError(APP_NAME, "Can't find directory ["+dirname+"] in the repository.");
                                        }
                                    }
                                }
                                catch(KettleException e)
                                {
                                    log.logError(APP_NAME, "Can't verify username and password.");
                                    win.rep.disconnect();
                                    win.rep=null;
                                    MessageBox mb = new MessageBox(win.shell, SWT.OK | SWT.ICON_ERROR);
                                    mb.setMessage("The supplied username or password is incorrect.");
                                    mb.setText("Sorry...");
                                    mb.open();
                                }
							}
							else
							{
                                log.logError(APP_NAME, "Can't connect to the repository.");
							}
						}
						else
						{
                            log.logError(APP_NAME, "No repository provided, can't load the job.");
						}
					}
					else
					{
                        log.logError(APP_NAME, "No repositories defined on this system.");
					}
				}
				else
				if (filename!=null)
				{
					win.jobMeta = new JobMeta(log, filename);
					win.jobMeta.clearChanged();
					}
			} // Nothing on commandline...
			else
			{
				// Can we connect to the repository?
				if (repinfo!=null && userinfo!=null)
				{
					win.rep = new Repository(log, repinfo, userinfo);
					if (!win.rep.connect("Spoon"))
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
                                        log.logDetailed(APP_NAME, "Auto loading job ["+lastfiles[0]+"] from repository directory ["+repdir.getPath()+"]");
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
								win.jobMeta = new JobMeta(log, lastfiles[0]);
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
            log.logError(APP_NAME, "Error loading job: "+ke.getMessage());
		}

		win.open ();
		splash.dispose();
		
		while (!win.isDisposed ()) 
		{
			if (!win.readAndDispatch ()) win.sleep ();
		}
		win.dispose();
		
        log.logBasic(APP_NAME, APP_NAME+" has ended.");

		// Close the logfile...
		log.close();
        
        // Kill all remaining things in this VM!
       System.exit(0);
	}
}
