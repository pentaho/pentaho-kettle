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
 
/*
 * Created on 16-may-2003
 *
 */

package be.ibridge.kettle.menu;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
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
import org.eclipse.swt.graphics.Color;
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

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.GUIResource;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.NotePadMeta;
import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.PrintSpool;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.TransAction;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.DatabaseDialog;
import be.ibridge.kettle.core.dialog.DatabaseExplorerDialog;
import be.ibridge.kettle.core.dialog.EnterOptionsDialog;
import be.ibridge.kettle.core.dialog.SQLEditor;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.schema.RelationshipMeta;
import be.ibridge.kettle.schema.SchemaMeta;
import be.ibridge.kettle.schema.TableField;
import be.ibridge.kettle.schema.TableMeta;
import be.ibridge.kettle.schema.WhereCondition;
import be.ibridge.kettle.schema.dialog.RelationshipDialog;
import be.ibridge.kettle.schema.dialog.SelectFieldDialog;
import be.ibridge.kettle.schema.dialog.TableDialog;
import be.ibridge.kettle.spoon.Spoon;
import be.ibridge.kettle.spoon.dialog.TipsDialog;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.TransHopMeta;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.cubeoutput.CubeOutputMeta;
import be.ibridge.kettle.trans.step.dummytrans.DummyTransMeta;
import be.ibridge.kettle.trans.step.selectvalues.SelectValuesMeta;
import be.ibridge.kettle.trans.step.tableinput.TableInputMeta;


public class PMenu
{
	protected StepLoader steploader;
	private LogWriter  log;
	private Display    disp;
	private Shell      shell;
	private boolean    destroy;
	private PMenuGraph pmenugraph;
	private PMenuLog   pmenulog;
	private SashForm   sashform;
	private CTabFolder tabfolder;
	public  Image      menu_image;
	public  Repository rep;
	
	public  boolean    demo_mode;
	public  int        licence_nr;
	
	public  SchemaMeta     schema;

	private ToolBar    tBar;
	
	private Menu       mBar;
	private MenuItem   mFile;
	private Menu       msFile;
	private MenuItem   miFileOpen, miFileMerge, miFileNew, miFileSave, miFileSaveAs, miFilePrint, miFileSep3, miFileQuit;
	private Listener   lsFileOpen, lsFileMerge, lsFileNew, lsFileSave, lsFileSaveAs, lsFilePrint, lsFileQuit;
	
	private MenuItem mEdit;
	private Menu     msEdit;
	private MenuItem miEditUndo, miEditRedo, miEditSelectAll, miEditUnselectAll, miEditOptions;
	private Listener lsEditUndo, lsEditRedo, lsEditSelectAll, lsEditUnselectAll, lsEditOptions;

	private MenuItem mTrans;
	private Menu     msTrans;
	private MenuItem miTransCheck;
	private Listener lsTransCheck;

	private MenuItem mHelp;
	private Menu     msHelp;
	private MenuItem miHelpAbout;
	private Listener lsHelpAbout;
	
	private Listener lsNew, lsEdit, lsDupe, lsDel, lsSQL, lsCache, lsImport, lsExpl;
	private SelectionAdapter lsEditDef, lsEditSel;
	
	public static final String STRING_CONNECTIONS   = "Connections";
	public static final String STRING_TABLES        = "Tables";
	public static final String STRING_RELATIONSHIPS = "Relationships";
	public static final String STRING_GROUPS        = "Groups";

	private static final String MENU_TITLE         = "Menu : Prepare your plate";
	
	private static final String STRING_DEFAULT_EXT    = ".ksc";
	private static final String STRING_FILTER_EXT[]   = new String[] { "*.ksc;*.xml", "*.xml", "*.*" };
	private static final String STRING_FILTER_NAMES[] = new String[] { "Kettle Schema", "XML Files", "All files" };
	 	
	private Tree tCSH, tGroup;
	private TreeItem tiConn;
	private TreeItem tiStep;
	private TreeItem tiTrns;

	public  KeyAdapter defKeys;
	public  KeyAdapter modKeys;

	/* Remember the size and position of the different windows...
	 *  0 : Main window
	 *  1 : TableDialog
	 *  2 : SelectFieldsDialog
	 *  
	 */
	public PMenu(LogWriter log, StepLoader steploader)
	{
		this(log, null, steploader);
	}
	
	public PMenu(LogWriter log, Display d, StepLoader steploader)
	{
		this.log=log;
		this.steploader = steploader;
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
		shell.setText(MENU_TITLE);
		FormLayout layout = new FormLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		shell.setLayout (layout);
		
		// INIT Data structure
		schema = new SchemaMeta(log);
		
		if (!Props.isInitialized()) Props.init(disp, Props.TYPE_PROPERTIES_SPOON);  // things to remember...
		schema.props=Props.getInstance();
				
		// Load settings in the props
		loadSettings();
		
		// shell.setFont(GUIResource.getInstance().getFontDefault());
		menu_image = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"spoon32.png"));
		shell.setImage(menu_image);
		
		defKeys = new KeyAdapter() 
			{
				public void keyPressed(KeyEvent e) 
				{
					// ESC --> Unselect All steps
					if (e.keyCode == SWT.ESC)   { schema.unselectAll(); refreshGraph(); };
					
					// F5 --> refresh
					if (e.keyCode == SWT.F5)    { refreshGraph(); refreshTree(true); }
					// CTRL-A --> Select All steps
					if ((int)e.character ==  1) { schema.selectAll(); refreshGraph(); };
					// CTRL-N --> new
					if ((int)e.character == 14) { newFile();    pmenugraph.control=false; } 
					// CTRL-O --> open
					if ((int)e.character == 15) { openFile();   pmenugraph.control=false; } 
					// CTRL-P --> print
					if ((int)e.character == 16) { printFile();  pmenugraph.control=false; } 
					// CTRL-S --> save
					if ((int)e.character == 19) { saveFile();   pmenugraph.control=false; } 
					// CTRL-V --> verify
					if ((int)e.character == 22) { buildSQL(); pmenugraph.control=false; } 
					// CTRL-Y --> redo action
					if ((int)e.character == 25) { redoAction(); pmenugraph.control=false; } 
					// CTRL-Z --> undo action
					if ((int)e.character == 26) { undoAction(); pmenugraph.control=false; } 
				}
			};
		modKeys = new KeyAdapter() 
			{
				public void keyPressed(KeyEvent e) 
				{
					if (e.keyCode == SWT.SHIFT  ) pmenugraph.shift=true;
					if (e.keyCode == SWT.CONTROL) pmenugraph.control=true;					
				}

				public void keyReleased(KeyEvent e) 
				{
					if (e.keyCode == SWT.SHIFT)   pmenugraph.shift=false;
					if (e.keyCode == SWT.CONTROL) pmenugraph.control=false;
				}
			};

		
		addBar();

		FormData fdBar = new FormData();
		fdBar.left = new FormAttachment(0, 0);
		fdBar.top = new FormAttachment(0, 0);
		tBar.setLayoutData(fdBar);

		sashform = new SashForm(shell, SWT.HORIZONTAL);
		// sashform.setFont(GUIResource.getInstance().getFontDefault());

		FormData fdSash = new FormData();
		fdSash.left = new FormAttachment(0, 0);
		fdSash.top = new FormAttachment(tBar, 0);
		fdSash.bottom = new FormAttachment(100, 0);
		fdSash.right  = new FormAttachment(100, 0);
		sashform.setLayoutData(fdSash);

		addMenu();
		addTree();
		addTabs();
		
		setTreeImages();
		
		// In case someone dares to press the [X] in the corner ;-)
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { e.doit=quitFile(); } } );
		int weights[] = schema.props.getSashWeights();
		sashform.setWeights(weights);
		sashform.setVisible(true);
		
		// Set the shell size, based upon previous time...
		WindowProperty winprop = schema.props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();

		shell.layout();
	}
	
	public void open()
	{		
		shell.open();
		
		// Perhaps the transformation contains elements at startup?
		if (schema.nrTables()>0 || schema.nrConnections()>0)
		{
			refreshTree(true);  // Do a complete refresh then...
		}
		
		if (schema.props.showTips()) 
		{
			TipsDialog tip = new TipsDialog(shell, schema.props);
			tip.open();
		}
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
		mBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(mBar);
		
		// main File menu...
		mFile = new MenuItem(mBar, SWT.CASCADE); mFile.setText("&File");
		  msFile = new Menu(shell, SWT.DROP_DOWN);
		  mFile.setMenu(msFile);
		  miFileNew    = new MenuItem(msFile, SWT.CASCADE); miFileNew.setText("&New \tCTRL-N");
		  miFileOpen   = new MenuItem(msFile, SWT.CASCADE); miFileOpen.setText("&Open \tCTRL-O");
		  miFileMerge  = new MenuItem(msFile, SWT.CASCADE); miFileMerge.setText("&Merge");
		  miFileSave   = new MenuItem(msFile, SWT.CASCADE); miFileSave.setText("&Save \tCTRL-S");
		  miFileSaveAs = new MenuItem(msFile, SWT.CASCADE); miFileSaveAs.setText("Save &as...");
		  new MenuItem(msFile, SWT.SEPARATOR);
		  miFilePrint  = new MenuItem(msFile, SWT.CASCADE); miFilePrint.setText("&Print \tCTRL-P");
		  new MenuItem(msFile, SWT.SEPARATOR);
		  miFileQuit   = new MenuItem(msFile, SWT.CASCADE); miFileQuit.setText("&Quit");
		  miFileSep3 = new MenuItem(msFile, SWT.SEPARATOR);
		  addMenuLast();

		
		lsFileOpen       = new Listener() { public void handleEvent(Event e) { openFile();       } };
		lsFileMerge      = new Listener() { public void handleEvent(Event e) { mergeFile();      } };
		lsFileNew        = new Listener() { public void handleEvent(Event e) { newFile();        } };
		lsFileSave       = new Listener() { public void handleEvent(Event e) { saveFile();       } };
		lsFileSaveAs     = new Listener() { public void handleEvent(Event e) { saveFileAs();     } };
		lsFilePrint      = new Listener() { public void handleEvent(Event e) { printFile();      } };
		lsFileQuit       = new Listener() { public void handleEvent(Event e) { quitFile();       } };
		
		miFileOpen      .addListener (SWT.Selection, lsFileOpen   );
		miFileMerge     .addListener (SWT.Selection, lsFileMerge  );
		miFileNew       .addListener (SWT.Selection, lsFileNew    );
		miFileSave      .addListener (SWT.Selection, lsFileSave   );
		miFileSaveAs    .addListener (SWT.Selection, lsFileSaveAs );
		miFilePrint     .addListener (SWT.Selection, lsFilePrint  );
		miFileQuit      .addListener (SWT.Selection, lsFileQuit   );

		// main Edit menu...
		mEdit = new MenuItem(mBar, SWT.CASCADE); mEdit.setText("&Edit");
		  msEdit = new Menu(shell, SWT.DROP_DOWN);
		  mEdit.setMenu(msEdit);
		  miEditUndo         = new MenuItem(msEdit, SWT.CASCADE);
		  miEditRedo         = new MenuItem(msEdit, SWT.CASCADE);
		  setUndoMenu();
		  new MenuItem(msEdit, SWT.SEPARATOR);
		  miEditUnselectAll  = new MenuItem(msEdit, SWT.CASCADE); miEditUnselectAll.setText("&Clear selection \tESC");
		  miEditSelectAll    = new MenuItem(msEdit, SWT.CASCADE); miEditSelectAll.setText("&Select all steps \tCTRL-A");
		  new MenuItem(msEdit, SWT.SEPARATOR);
		  miEditOptions      = new MenuItem(msEdit, SWT.CASCADE); miEditOptions.setText("&Refresh \tF5");
		  new MenuItem(msEdit, SWT.SEPARATOR);
		  miEditOptions      = new MenuItem(msEdit, SWT.CASCADE); miEditOptions.setText("&Options...");
		
		lsEditUndo        = new Listener() { public void handleEvent(Event e) { undoAction(); } };
		lsEditRedo        = new Listener() { public void handleEvent(Event e) { redoAction(); } };
		lsEditUnselectAll = new Listener() { public void handleEvent(Event e) { editUnselectAll(); } };
		lsEditSelectAll   = new Listener() { public void handleEvent(Event e) { editSelectAll();   } };
		lsEditOptions     = new Listener() { public void handleEvent(Event e) { editOptions();     } };

		miEditUndo       .addListener(SWT.Selection, lsEditUndo);
		miEditRedo       .addListener(SWT.Selection, lsEditRedo);
		miEditUnselectAll.addListener(SWT.Selection, lsEditUnselectAll);
		miEditSelectAll  .addListener(SWT.Selection, lsEditSelectAll);
		miEditOptions    .addListener(SWT.Selection, lsEditOptions);

		// main Transformation menu...
		mTrans = new MenuItem(mBar, SWT.CASCADE); mTrans.setText("&Model");
		  msTrans = new Menu(shell, SWT.DROP_DOWN );
		  mTrans.setMenu(msTrans);
		  miTransCheck   = new MenuItem(msTrans, SWT.CASCADE); miTransCheck  .setText("&Verify \tCTRL-V");

		lsTransCheck     = new Listener() { public void handleEvent(Event e) { buildSQL();   } };
		miTransCheck  .addListener(SWT.Selection, lsTransCheck);

		// main Help menu...
		mHelp = new MenuItem(mBar, SWT.CASCADE); mHelp.setText("&Help");
		  msHelp = new Menu(shell, SWT.DROP_DOWN );
		  mHelp.setMenu(msHelp);
		  miHelpAbout       = new MenuItem(msHelp, SWT.CASCADE); miHelpAbout.setText("&About");
		lsHelpAbout = new Listener() { public void handleEvent(Event e) { helpAbout();      } };
		miHelpAbout.addListener (SWT.Selection, lsHelpAbout  );
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
		String lf[] = schema.props.getLastFiles();
		boolean lt[] = schema.props.getLastTypes();
		
		for (int i=0;i<lf.length;i++)
		{
		  MenuItem miFileLast = new MenuItem(msFile, SWT.CASCADE);
		  char chr  = (char)('1'+i );
		  int accel =  SWT.CTRL | chr;
		  miFileLast.setText("&"+chr+"  "+lf[i]+" ("+(lt[i]?"Repository":"File")+") \tCTRL-"+chr);
		  miFileLast.setAccelerator(accel);
		  final String fn = lf[i];
		  final boolean ft = lt[i];
		  
		  Listener lsFileLast = new Listener() 
			  { 
				  public void handleEvent(Event e) 
				  {
				      if (showChangedWarning())
				      {
						if (readData(fn, ft))
						{
							schema.clearChanged();
							setFilename(fn);
							pmenugraph.control=false;
						}
						else
						{
							MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
							mb.setMessage("Error opening file : "+fn+Const.CR+"See the log for more information.");
							mb.setText("Error!");
							mb.open();
						}
					  }
				  } 
			  };
		  miFileLast.addListener(SWT.Selection, lsFileLast);
		}
	}

	private void addBar()
	{
		tBar = new ToolBar(shell, SWT.HORIZONTAL | SWT.FLAT );
		// tBar.setFont(GUIResource.getInstance().getFontDefault());
		
		//tBar.setSize(200, 20);
		final ToolItem tiFileNew = new ToolItem(tBar, SWT.PUSH);
		final Image imFileNew = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"new.png")); 
		tiFileNew.setImage(imFileNew);
		tiFileNew.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newFile(); }});
		tiFileNew.setToolTipText("New file, clear all settings");

		final ToolItem tiFileOpen = new ToolItem(tBar, SWT.PUSH);
		final Image imFileOpen = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"open.png")); 
		tiFileOpen.setImage(imFileOpen);
		tiFileOpen.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { openFile(); }});
		tiFileOpen.setToolTipText("Open file");

		final ToolItem tiFileMerge = new ToolItem(tBar, SWT.PUSH);
		final Image imFileMerge = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"merge.png")); 
		tiFileMerge.setImage(imFileMerge);
		tiFileMerge.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { mergeFile(); }});
		tiFileMerge.setToolTipText("Merge with other file");

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
		final ToolItem tiFileSQL = new ToolItem(tBar, SWT.PUSH);
		final Image imFileSQL = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"SQLbutton.png")); 
		tiFileSQL.setImage(imFileSQL);
		tiFileSQL.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { buildSQL(); }});
		tiFileSQL.setToolTipText("Verify this transformation");

		tBar.addDisposeListener(new DisposeListener() 
			{
				public void widgetDisposed(DisposeEvent e) 
				{
					imFileNew.dispose();
					imFileOpen.dispose();
					imFileMerge.dispose();
					imFileSave.dispose();
					imFileSaveAs.dispose();
				}
			}
		);
		tBar.addKeyListener(defKeys);
		tBar.addKeyListener(modKeys);
		tBar.pack();
	}
	
	private void addTree()
	{
		SashForm leftsplit = new SashForm(sashform,SWT.VERTICAL);
		leftsplit.setLayout(new FillLayout());

		// CSH: Connections, Steps and Transformations
		Composite cCSH = new Composite(leftsplit, SWT.NONE);
		cCSH.setLayout(new FillLayout());
					
		// Now set up the main CSH tree
		tCSH = new Tree(cCSH, SWT.MULTI | SWT.BORDER);
		// tCSH.setFont(GUIResource.getInstance().getFontDefault());
		tiConn = new TreeItem(tCSH, SWT.NONE); tiConn.setText(STRING_CONNECTIONS);
		tiStep = new TreeItem(tCSH, SWT.NONE); tiStep.setText(STRING_TABLES);
		tiTrns = new TreeItem(tCSH, SWT.NONE); tiTrns.setText(STRING_RELATIONSHIPS);
		
		tiConn.setExpanded(true);
		tiStep.setExpanded(false);

		tCSH.setBackground(GUIResource.getInstance().getColorBackground());

		// Popup-menu selection
		lsNew    = new Listener() { public void handleEvent(Event e) { newSelected();  } };  
		lsEdit   = new Listener() { public void handleEvent(Event e) { editSelected(); } };
		lsDupe   = new Listener() { public void handleEvent(Event e) { dupeSelected(); } };
		lsDel    = new Listener() { public void handleEvent(Event e) { delSelected();  } };
		lsSQL    = new Listener() { public void handleEvent(Event e) { sqlSelected();  } };
		lsCache  = new Listener() { public void handleEvent(Event e) { clearDBCache(); } };
		lsImport = new Listener() { public void handleEvent(Event e) { importTables(); } };
		lsExpl   = new Listener() { public void handleEvent(Event e) { exploreDB();    } };

		// Default selection (double-click, enter)
		lsEditDef = new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e){ editSelected(); } };
		//lsNewDef  = new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e){ newSelected();  } };
		lsEditSel = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { setMenu(e); } };
		
		// Add all the listeners... 
		tCSH.addSelectionListener(lsEditDef); // double click somewhere in the tree...
		//tCSH.addSelectionListener(lsNewDef); // double click somewhere in the tree...
		tCSH.addSelectionListener(lsEditSel);
		
		// Drag & Drop for steps
		Transfer[] ttypes = new Transfer[] {TextTransfer.getInstance() };
		
		DragSource ddSource = new DragSource(tCSH, DND.DROP_MOVE | DND.DROP_COPY);
		ddSource.setTransfer(ttypes);
		ddSource.addDragListener(new DragSourceListener() 
			{
				public void dragStart(DragSourceEvent event){ }
	
				public void dragSetData(DragSourceEvent event) 
				{
					TreeItem ti[] = tCSH.getSelection();
					String data = new String();
					for (int i=0;i<ti.length;i++) 
					{
                        data+=ti[i].getText()+Const.CR;
					} 
					event.data = data;
				}
	
				public void dragFinished(DragSourceEvent event) {}
			}
		);
		DropTarget dtCSH = new DropTarget(tCSH, DND.DROP_MOVE | DND.DROP_COPY);
		dtCSH.setTransfer(ttypes);
		dtCSH.addDropListener(new DropTargetListener() 
		{
			public void dragEnter(DropTargetEvent event) { }
			public void dragLeave(DropTargetEvent event) { }
			public void dragOperationChanged(DropTargetEvent event) { }
			public void dragOver(DropTargetEvent event) { }
			public void drop(DropTargetEvent event) 
			{
				if (event.data == null) { // no data to copy, indicate failure in event.detail
					event.detail = DND.DROP_NONE;
					return;
				}
				StringTokenizer strtok = new StringTokenizer((String)event.data, Const.CR);
				while (strtok.hasMoreTokens())
				{
					String   source = strtok.nextToken();
					TreeItem target = (TreeItem)event.item;
					TreeItem parent = target.getParentItem();
	
					if (STRING_TABLES.equalsIgnoreCase(parent.getText()))
					{
						TableMeta fr = schema.findTable(source);
						TableMeta to = schema.findTable(target.getText());
						
						int idx_fr = schema.indexOfTable(fr);
						int idx_to = schema.indexOfTable(to);
	
						log.logBasic("DROP", "idx_fr = "+idx_fr+", idx_to="+idx_to);
						
						if (idx_fr==idx_to) return;
						if (idx_fr >idx_to)
						{
							// 1: remove at location idx_fr
							schema.removeTable(idx_fr);
							// 2: add at location idx_to: before!
							schema.addTable(idx_to, fr);
						}
						else
						{
							// 1: remove at location idx_fr
							schema.removeTable(idx_fr);
							// 2: add at location idx_to-1: before!
							schema.addTable(idx_to-1, fr);
						}
						refreshTree();
					}
				}
			}

			public void dropAccept(DropTargetEvent event) 
			{
			}
		});

		Composite cGroup = new Composite(leftsplit, SWT.NONE);
		cGroup.setLayout(new FillLayout());
		
		// Now set up the main CSH tree
		tGroup = new Tree(cGroup, SWT.MULTI | SWT.BORDER);
		// tGroup.setFont(GUIResource.getInstance().getFontDefault());
		TreeItem tiGroup = new TreeItem(tGroup, SWT.NONE); 
		tiGroup.setText(STRING_GROUPS);
			
		leftsplit.setWeights(new int[] {50, 50});

		DropTarget dtGroup = new DropTarget(tGroup, DND.DROP_MOVE | DND.DROP_COPY);
		dtGroup.setTransfer(ttypes);
		dtGroup.addDropListener(new DropTargetListener() 
		{
			public void dragEnter(DropTargetEvent event) { }
			public void dragLeave(DropTargetEvent event) { }
			public void dragOperationChanged(DropTargetEvent event) { }
			public void dragOver(DropTargetEvent event) { }
			public void drop(DropTargetEvent event) 
			{
				if (event.data == null)  // no data to copy, indicate failure in event.detail 
				{
					event.detail = DND.DROP_NONE;
					return;
				}
		
//				TreeItem ti = (TreeItem)event.item;
//				
//              int level = Const.getTreeLevel(ti);
//				String str[] = Const.getTreeStrings(ti);
//		
//				System.out.println("tree :");
//				  
//				for (int i=0;i<str.length;i++)
//				{
//				     System.out.println("Level #"+i+" --> "+str[i]);
//				}
				
			}

			public void dropAccept(DropTargetEvent event) 
			{
			}
		});

		// Set the menu in the group tree
		tGroup.addSelectionListener( new SelectionAdapter() 
			{ 
				public void widgetDefaultSelected(SelectionEvent e)
				{
					TreeItem ti[] = tGroup.getSelection();
					for (int i=0;i<ti.length;i++) ti[i].setExpanded(!ti[i].getExpanded());
				}

				public void widgetSelected(SelectionEvent e) 
				{ 
					setGroupMenu(e); 
				} 
			}
		);
		
		// Keyboard shortcuts!
		tCSH.addKeyListener(defKeys);
		tCSH.addKeyListener(modKeys);

	}
	
	private void setMenu(SelectionEvent e)
	{
		TreeItem ti = (TreeItem)e.item;
		Tree root = ti.getParent();
		log.logDebug(toString(), "Clicked on  "+ti.getText());
		TreeItem sel[] = root.getSelection();

		Menu mCSH = new Menu(shell, SWT.POP_UP);

		// Find the level we clicked on: Top level (only NEW in the menu) or below (edit, insert, ...)
		TreeItem parent = ti.getParentItem();
		if (parent==null) // Top level
		{
			MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText("New");
			miNew.addListener( SWT.Selection, lsNew );
			if (ti.getText().equalsIgnoreCase(STRING_CONNECTIONS))
			{
				MenuItem miCache  = new MenuItem(mCSH, SWT.PUSH); miCache.setText("Clear complete DB Cache");
				miCache.addListener( SWT.Selection, lsCache );
			}
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
				MenuItem miImp  = new MenuItem(mCSH, SWT.PUSH); miImp.setText("Import...");
				MenuItem miSQL  = new MenuItem(mCSH, SWT.PUSH); miSQL.setText("SQL Editor");
				MenuItem miCache= new MenuItem(mCSH, SWT.PUSH); miCache.setText("Clear DB Cache of "+ti.getText());
				new MenuItem(mCSH, SWT.SEPARATOR);
				MenuItem miExpl = new MenuItem(mCSH, SWT.PUSH); miExpl.setText("Explore...");
				miNew.addListener( SWT.Selection, lsNew );   
				miEdit.addListener(SWT.Selection, lsEdit );
				miDupe.addListener(SWT.Selection, lsDupe );
				miDel.addListener(SWT.Selection, lsDel );
				miSQL.addListener(SWT.Selection, lsSQL );
				miCache.addListener(SWT.Selection, lsCache);
				miImp.addListener(SWT.Selection, lsImport);
				miExpl.addListener(SWT.Selection, lsExpl);
			}
			if (section.equalsIgnoreCase(STRING_TABLES))
			{
				if (sel.length==2)
				{
					MenuItem miNewHop = new MenuItem(mCSH, SWT.PUSH); miNewHop.setText("New Hop");
					miNewHop.addListener(SWT.Selection, lsNew);
				}
				MenuItem miEdit   = new MenuItem(mCSH, SWT.PUSH); miEdit.setText("Edit");
				MenuItem miDupe   = new MenuItem(mCSH, SWT.PUSH); miDupe.setText("Duplicate");
				MenuItem miDel    = new MenuItem(mCSH, SWT.PUSH); miDel.setText("Delete");
				miEdit.addListener(SWT.Selection, lsEdit );
				miDupe.addListener(SWT.Selection, lsDupe );
				miDel.addListener(SWT.Selection, lsDel );
			}
			if (section.equalsIgnoreCase(STRING_RELATIONSHIPS))
			{
				MenuItem miEdit = new MenuItem(mCSH, SWT.PUSH); miEdit.setText("Edit");
				MenuItem miDel  = new MenuItem(mCSH, SWT.PUSH); miDel.setText("Delete");
				miEdit.addListener( SWT.Selection, lsEdit );
				miDel.addListener ( SWT.Selection, lsDel  );
			}
		}
		tCSH.setMenu(mCSH);
	}
	
	private int nr=0;
	
	private void setGroupMenu(SelectionEvent e)
	{
		final TreeItem ti = (TreeItem)e.item;
		log.logDebug(toString(), "Clicked on  "+ti.getText());
		
		Menu mGroup = new Menu(shell, SWT.POP_UP);

		// Find the level we clicked on: Top level (only NEW in the menu) or below (edit, insert, ...)
        /*
         * int level = Const.getTreeLevel(ti);
         * String str[] = Const.getTreeStrings(ti);
         * 
		 * System.out.println("tree :");
		 * for (int i=0;i<str.length;i++)
		 * {
		 * 	System.out.println("Level #"+i+" --> "+str[i]);
		 * }
		 */
		// TreeItem parent = ti.getParentItem();

		MenuItem miNew  = new MenuItem(mGroup, SWT.PUSH); miNew.setText("New");
		miNew.addSelectionListener( new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					TreeItem item = new TreeItem(ti, SWT.NONE);
					item.setText("Test "+(nr++));
				}
			}
		);
		/*
		if (ti.getText().equalsIgnoreCase(STRING_CONNECTIONS))
		{
			MenuItem miCache  = new MenuItem(mGroup, SWT.PUSH); miCache.setText("Clear complete DB Cache");
			miCache.addListener( SWT.Selection, lsCache );
		}
		*/
		tGroup.setMenu(mGroup);
	}
	
	
	private void addTabs()
	{
		Composite child = new Composite(sashform, SWT.BORDER );
		child.setLayout(new FillLayout());
		
		tabfolder= new CTabFolder(child, SWT.BORDER);
		// tabfolder.setFont(GUIResource.getInstance().getFontDefault());
		// tabfolder.setBackground(GUIResource.getInstance().getColorBackground());
		tabfolder.setSimple(false);
		// tabfolder.setSelectionBackground(GUIResource.getInstance().getColorTab());
		
       CTabItem   tiTabsGraph = new CTabItem(tabfolder, SWT.NONE); tiTabsGraph.setText("Graphical view");
       tiTabsGraph.setToolTipText("Displays the schema graphically.");
		CTabItem   tiTabsList  = new CTabItem(tabfolder, SWT.NULL); tiTabsList.setText("Log view");
		tiTabsList.setToolTipText("Displays the log.");

		pmenugraph = new PMenuGraph(tabfolder, SWT.V_SCROLL | SWT.H_SCROLL | SWT.NO_BACKGROUND, schema, this);
		pmenulog   = new PMenuLog(tabfolder, SWT.NONE, schema, null);
		
        tiTabsGraph.setControl(pmenugraph);
		tiTabsList.setControl(pmenulog);
		
		tabfolder.setSelection(0);
		
		sashform.addKeyListener(defKeys);
		sashform.addKeyListener(modKeys);

	}
	
	private boolean readData(String fname, boolean repository)
	{
		System.out.println("readData("+fname+")");
		
		schema.props.addLastFile(Props.TYPE_PROPERTIES_CHEF, fname, Const.FILE_SEPARATOR, false, "");
		saveSettings();
		addMenuLast();
		if (repository)
		{
			return false;
		}
		else
		{
			return readData(fname, false, false);
		}
	}
	
	private boolean readData(String fname, boolean merge, boolean repository)
	{
		boolean retval = schema.loadXML(fname, merge);
		refreshTree();
		refreshGraph();
		
		return retval;
	}
	
	public void newSelected()
	{
		log.logDebug(toString(), "New Selected");
		// Determine what menu we selected from...
		
		TreeItem ti[] = tCSH.getSelection();
					
		// Then call newConnection or newTrans
		if (ti.length>=1)
		{
			String name = ti[0].getText();
			TreeItem parent = ti[0].getParentItem();
			if (parent == null)
			{
				log.logDebug(toString(), "Element has no parent");
				if (name.equalsIgnoreCase(STRING_CONNECTIONS)) newConnection();
				if (name.equalsIgnoreCase(STRING_RELATIONSHIPS      )) newRelationship();
				if (name.equalsIgnoreCase(STRING_TABLES     )) 
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
					mb.setMessage("Please import tables via the connections.");
					mb.setText("Tip!");
					mb.open();
				} 
				//refreshTree();
			}
			else
			{
				String section = parent.getText();
				log.logDebug(toString(), "Element has parent: "+section);
				if (section.equalsIgnoreCase(STRING_CONNECTIONS)) newConnection();
				if (section.equalsIgnoreCase(STRING_TABLES      )) 
				{
					log.logDebug(toString(), "New hop!");
					String from = ti[0].getText();
					String to;
					if (ti.length>1) to=ti[1].getText(); else to=""; 
					newRelationship(from, to);
				} 
				
			}
		}
	}
	
	public void editSelected()
	{
		// Determine what menu we selected from...

		TreeItem ti[] = tCSH.getSelection();
					
		// Then call editConnection or editStep or editTrans
		if (ti.length==1)
		{
			String name = ti[0].getText();
			TreeItem parent = ti[0].getParentItem();
			if (parent != null)
			{
				log.logDebug(toString(), "(EDIT) Element has parent.");
				String type = parent.getText();
				if (type.equalsIgnoreCase(STRING_CONNECTIONS)) editConnection(name);
				if (type.equalsIgnoreCase(STRING_TABLES      )) editTable(name);
				if (type.equalsIgnoreCase(STRING_RELATIONSHIPS       )) editRelationship(name);
			}
			else
			{
				log.logDebug(toString(), "Element has no parent");
				if (name.equalsIgnoreCase(STRING_CONNECTIONS)) newConnection();
				if (name.equalsIgnoreCase(STRING_RELATIONSHIPS       )) newRelationship();
			}
		}
	}

	public void dupeSelected()
	{
		// Determine what menu we selected from...

		TreeItem ti[] = tCSH.getSelection();
		
        // Then call editConnection or editStep or editTrans
		if (ti.length==1)
		{
			String name = ti[0].getText();
			TreeItem parent = ti[0].getParentItem();
			if (parent != null)
			{
				log.logDebug(toString(), "(DUPE) Element has parent.");
				String type = parent.getText();
				if (type.equalsIgnoreCase(STRING_CONNECTIONS)) dupeConnection(name);
				if (type.equalsIgnoreCase(STRING_TABLES      )) dupeTable(name);
			} 
		}
	}
	
	public void delSelected()
	{
		// Determine what menu we selected from...
		int i;
		
		TreeItem ti[] = tCSH.getSelection();
		
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
				String type = parent[i].getText();
				log.logDebug(toString(), "(DELETE) Element has parent: "+type);
				if (type.equalsIgnoreCase(STRING_CONNECTIONS)) delConnection(name[i]);
				if (type.equalsIgnoreCase(STRING_TABLES      )) delTable(name[i]);
				if (type.equalsIgnoreCase(STRING_RELATIONSHIPS       )) delRelations(name[i]);
			} 
		}
	}

	public void sqlSelected()
	{
		// Determine what menu we selected from...
		TreeItem ti[] = tCSH.getSelection();
		
		for (int i=0;i<ti.length;i++) 
		{
			String name     = ti[i].getText();
			TreeItem parent = ti[i].getParentItem();
			String type     = parent.getText();
			if (type.equalsIgnoreCase(STRING_CONNECTIONS))
			{
				DatabaseMeta ci = schema.findConnection(name);
				SQLEditor sql = new SQLEditor(shell, SWT.NONE, ci, schema.dbcache, "");
				sql.open();
			}
			
		} 
	}
	
	public void editConnection(String name)
	{
		DatabaseMeta db = schema.findConnection(name);
		if (db!=null)
		{
			DatabaseMeta before = (DatabaseMeta)db.clone();
			
			DatabaseDialog con = new DatabaseDialog(shell, SWT.NONE, log, db, schema.props);
			String newname = con.open(); 
			if (newname != null)  // null: CANCEL
			{
				// Store undo/redo information
				DatabaseMeta after = (DatabaseMeta)db.clone();
				addUndoChange(new DatabaseMeta[] { before }, new DatabaseMeta[] { after }, new int[] { schema.indexOfConnection(db) } );
			}
		}
		setShellText();
	}

	public void dupeConnection(String name)
	{
		int i, pos=0;
		DatabaseMeta db = null, look=null;
						
		for (i=0;i<schema.nrConnections() && db==null;i++)
		{
			look = schema.getConnection(i);
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
			schema.addConnection(pos+1, newdb);
			refreshTree();

			DatabaseDialog con = new DatabaseDialog(shell, SWT.NONE, log, newdb, schema.props);
			String newname = con.open(); 
			if (newname != null)  // null: CANCEL
			{
				schema.removeConnection(pos+1);
				schema.addConnection(pos+1, newdb);
				
				if (!newname.equalsIgnoreCase(dupename)) refreshTree();
			}
			else
			{
				addUndoNew(new DatabaseMeta[] { (DatabaseMeta)db.clone() }, new int[] { schema.indexOfConnection(db) });
			}
		}
	}

	public void delConnection(String name)
	{
		int i, pos=0;
		DatabaseMeta db = null, look=null;
						
		for (i=0;i<schema.nrConnections() && db==null;i++)
		{
			look = schema.getConnection(i);
			if (look.getName().equalsIgnoreCase(name))
			{
				db=look;
				pos=i;
			}
		}
		if (db!=null)
		{
			addUndoDelete(new DatabaseMeta[] { (DatabaseMeta)db.clone() }, new int[] { schema.indexOfConnection(db) });
			schema.removeConnection(pos);
			refreshTree();
		}
		setShellText();
	}
	
	public void editTable(String name)
	{
		log.logDebug(toString(), "Edit table: "+name);
		editTableInfo(schema.findTable(name));
	}
	
	public void editTableInfo(TableMeta tableinfo)
	{
		if (tableinfo!=null)
		{
			String tablename=null;
			
			// Before we do anything, let's store the situation the way it was...
			TableMeta before = (TableMeta)tableinfo.clone();

			TableDialog td = new TableDialog(shell, SWT.NONE, log, tableinfo, schema);
			td.open();
					
			if (tablename!=null)
			{
				// OK, so the table has changed...
				//
				// First, backup the situation for undo/redo
				TableMeta after = (TableMeta)tableinfo.clone();
				addUndoChange(new TableMeta[] { before }, new TableMeta[] { after }, new int[] { schema.indexOfTable(tableinfo) }  );
	
				// Then, store the size of the 
				// See if the new name the user enter, doesn't collide with another step. 
				// If so, change the stepname and warn the user!
				//
				String newname=tablename;
				TableMeta si = schema.findTable(newname, tableinfo);
				int nr=2;
				while (si!=null)
				{
					newname = tablename+" "+nr;
					si = schema.findTable(newname);
					nr++;
				}
				if (nr>2)
				{
					tablename=newname;
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
					mb.setMessage("This tablename already exists.  Menu changed the tablename to ["+tablename+"]");
					mb.setText("Tip!");
					mb.open();
				}
				tableinfo.setName(tablename);
				
				refreshGraph();
				refreshTree();  // Perhaps new connections were created in the step dialog.				
			}
			setShellText();
		}
	}

	public void dupeTable(String name)
	{
		log.logDebug(toString(), "Duplicate table: "+name);
		
		TableMeta tinfo = null, tableinfo = null, look=null;
		
		for (int i=0;i<schema.nrTables() && tableinfo==null;i++)
		{
			look = schema.getTable(i);
			if (look.getName().equalsIgnoreCase(name))
			{
				tableinfo=look;
			}
		}
		if (tableinfo!=null)
		{
			tinfo = (TableMeta)tableinfo.clone();
			if (tinfo!=null)
			{
				String newname = tableinfo.getName()+" (copy)";
				int nr=2;
				while (schema.findTable(newname)!=null)
				{
					newname = tableinfo.getName()+" (copy "+nr+")";
					nr++;
				}
				tinfo.setName(newname);
				// Don't select this new step!
				tinfo.setSelected(false);
				Point loc = tinfo.getLocation();
				tinfo.setLocation(loc.x+20, loc.y+20);
				schema.addTable(tinfo);
				addUndoNew(new TableMeta[] { (TableMeta)tinfo.clone() }, new int[] { schema.indexOfTable(tinfo) });
				refreshTree();
				refreshGraph();
			}
		}
	}


	public void delTable(String name)
	{
		log.logDebug(toString(), "Delete table: "+name);
		
		int pos=0;
		TableMeta tableinfo = null, look=null;
					
		for (int i=0;i<schema.nrTables() && tableinfo==null;i++)
		{
			look = schema.getTable(i);
			if (look.getName().equalsIgnoreCase(name))
			{
				tableinfo=look;
				pos=i;
			}
		}
		if (tableinfo!=null)
		{
			schema.removeTable(pos);
			addUndoDelete(new TableMeta[] { (TableMeta)tableinfo.clone() }, new int[] { pos });
			for (int i=schema.nrRelationships()-1;i>=0;i--)
			{
				RelationshipMeta ri = schema.getRelationship(i);
				if (   ri.getTableFrom().getName().equalsIgnoreCase(tableinfo.getName())
				    || ri.getTableTo().getName().equalsIgnoreCase(tableinfo.getName())
				   )
				{
					addUndoDelete(new RelationshipMeta[] { (RelationshipMeta)ri.clone() }, new int[] { schema.indexOfRelationship(ri) });
				    schema.removeRelationship(i);
					refreshTree();
				}
			}
			refreshTree();
			refreshGraph();
		}
		else
		{
			log.logDebug(toString(), "Couldn't find step ["+name+"] to delete...");
		}
	}	

	public void editRelationship(String name)
	{
		RelationshipMeta ri = schema.findRelationship(name);
		if (ri!=null)
		{
			// Backup situation BEFORE edit:
			RelationshipMeta before = (RelationshipMeta)ri.clone();
			
			RelationshipDialog rd = new RelationshipDialog(shell, SWT.NONE, log, ri, schema);
			if (rd.open()!=null)
			{
				// Backup situation for redo/undo:
				RelationshipMeta after = (RelationshipMeta)ri.clone();
				addUndoChange(new RelationshipMeta[] { before }, new RelationshipMeta[] { after }, new int[] { schema.indexOfRelationship(ri) } );
			
				String newname = ri.toString();
				if (!name.equalsIgnoreCase(newname)) 
				{
					refreshTree(); 
					refreshGraph(); // color, nr of copies...
				}
			}
		}
		setShellText();
	}

	public void delRelations(String name)
	{
		int i,n;
		
		n=schema.nrRelationships();
		
		for (i=0;i<n;i++)
		{
			RelationshipMeta hi = schema.getRelationship(i);
			if (hi.toString().equalsIgnoreCase(name))
			{
				addUndoDelete(new RelationshipMeta[] { (RelationshipMeta)hi.clone() }, new int[] { schema.indexOfRelationship(hi) });
				schema.removeRelationship(i);
				refreshTree();
				refreshGraph();
				return;
			}
		}
		setShellText();
	}

	public void newRelationship(String fr, String to)
	{
		TableMeta tabfrom = schema.findTable(fr);
		TableMeta tabto   = schema.findTable(to);
		
		RelationshipMeta hi = new RelationshipMeta(tabfrom, tabto, -1, -1);
		
		RelationshipDialog hd = new RelationshipDialog(shell, SWT.NONE, log, hi, schema);
		if (hd.open()!=null)
		{
			boolean error=false;

			if (schema.findRelationship(hi.getTableFrom().getName(), hi.getTableTo().getName())!=null)
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage("This relationship already exists!");
				mb.setText("Error!");
				mb.open();
				error=true;
			}
			
			if (!error)
			{
				schema.addRelationship(hi);
				addUndoNew(new RelationshipMeta[] { (RelationshipMeta)hi.clone() }, new int[] { schema.indexOfRelationship(hi) });
				TableMeta sifr = schema.findTable(fr);
				TableMeta sito = schema.findTable(to);
				sifr.draw();
				sito.draw();
				refreshTree();
				refreshGraph();
			}
		}
	}

	public void newRelationship()
	{
		newRelationship("", "");
	}
	
	public void newConnection()
	{
		DatabaseMeta db = new DatabaseMeta(); 
		DatabaseDialog con = new DatabaseDialog(shell, SWT.APPLICATION_MODAL, log, db, schema.props);
		String con_name = con.open(); 
		if (con_name!=null)
		{
			schema.addConnection(db);
			addUndoNew(new DatabaseMeta[] { (DatabaseMeta)db.clone() }, new int[] { schema.indexOfConnection(db) });
			refreshTree();
		}
	}

	public void mergeFile()
	{
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setFilterPath("C:\\Projects\\kettle\\source\\");
		dialog.setFilterExtensions(STRING_FILTER_EXT);
		dialog.setFilterNames(STRING_FILTER_NAMES);
		dialog.setText("Merge...");
		String filen = dialog.open();
		if (filen!=null) 
		{
			readData(filen, true);
		} 
	}
	
	public boolean showChangedWarning()
	{
		boolean answer = true;
		if (schema.hasChanged())
		{
			MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_WARNING );
			mb.setMessage("This model has changed.  Are you sure you want open a new file?");
			mb.setText("Warning!");
			answer = mb.open()==SWT.YES;
		}
		return answer;
	}

	public void openFile()
	{
		System.out.println("Open file!!!!");
		if (showChangedWarning())
		{ 
			FileDialog dialog = new FileDialog(shell, SWT.OPEN);
			dialog.setFilterPath("C:\\Projects\\kettle\\source\\");
			dialog.setFilterExtensions(STRING_FILTER_EXT);
			dialog.setFilterNames(STRING_FILTER_NAMES);
			String fname = dialog.open();
			if (fname!=null) 
			{
				if (readData(fname, false))
				{
					schema.clearChanged();
					setFilename(fname);
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
					mb.setMessage("Error opening file : "+fname+Const.CR+"See the log for more information.");
					mb.setText("Error!");
					mb.open();
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
			schema.clear();
			setFilename(null);
			refreshTree(true);
			refreshGraph();
		}
	}
	
	public boolean quitFile()
	{
		boolean retval=true;
		
		log.logDetailed(toString(), "Quit application.");
		saveSettings();
		if (schema.hasChanged())
		{
			MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_WARNING );
			mb.setMessage("File has changed!  Do you want to save first?");
			mb.setText("Warning!");
			int answer = mb.open();
		
			switch(answer)
			{
			case SWT.YES: saveFile(); dispose(); break;
			case SWT.NO: dispose(); break;
			case SWT.CANCEL: retval=false; break;
			}
		}
		else
		{
			dispose();
		}
		return retval;
	}
	
	public void saveFile()
	{
		log.logDetailed(toString(), "Save file...");
		if (schema.filename!=null)
		{
			save(schema.filename);
		}
		else
		{
			saveFileAs();
		}
	}

	public void saveFileAs()
	{
		log.logBasic(toString(), "Save file as...");

		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setFilterPath("C:\\Projects\\kettle\\source\\");
		dialog.setFilterExtensions(STRING_FILTER_EXT);
		dialog.setFilterNames(STRING_FILTER_NAMES);
		String fname = dialog.open();
		if (fname!=null) 
		{
			// Is the filename ending on .ktr, .xml?
			boolean ending=false;
			for (int i=0;i<STRING_FILTER_EXT.length-1;i++)
			{
				if (fname.endsWith(STRING_FILTER_EXT[i].substring(1))) 
				{
					ending=true;
				} 
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
				setFilename(fname);
			}
		} 
	}
	
	private void save(String fname)
	{
		String xml = schema.getXML();
		try
		{
			FileWriter fw = new FileWriter(fname);
			fw.write(xml);
			fw.close();
			// Handle last opened files...
			schema.props.addLastFile(Props.TYPE_PROPERTIES_MENU, fname, Const.FILE_SEPARATOR, false, "");
			saveSettings();
			addMenuLast();
			
			schema.clearChanged();
			setShellText();
			log.logDebug(toString(), "File written to ["+fname+"]");
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
		String mess = "Kettle - Spoon version "+Const.VERSION+Const.CR+Const.CR+Const.CR;
		mess+="(c) 2001-2004 i-Bridge bvba"+Const.CR+"         www.ibridge.be"+Const.CR;
		
        mb.setMessage(mess);
		mb.setText("Menu");
		mb.open();
	}

	public void editUnselectAll()
	{
		schema.unselectAll(); 
		pmenugraph.redraw();
	}
	
	public void editSelectAll()
	{
		schema.selectAll(); 
		pmenugraph.redraw();
	}
	
	public void editOptions()
	{
		EnterOptionsDialog eod = new EnterOptionsDialog(shell, schema.props);
		if (eod.open()!=null)
		{
			schema.props.saveProps();
			loadSettings();
			changeLooks();
		} 
	}
	
	public int getTreePosition(TreeItem ti, String item)
	{
		if (ti!=null)
		{
			TreeItem items[] = ti.getItems();
			for (int x=0;x<items.length;x++)
			{
				if (items[x].getText().equalsIgnoreCase(item))
				{
					return x;
				}
			}
		}
		return -1;
	}

	public void refreshTree()
	{
		refreshTree(false);
	}

	public void refreshTree(boolean complete)
	{
		if (!schema.hasChanged() && !complete) return;  // Nothing changed: nothing to do!
		
		int idx;
		TreeItem ti[];
		
		// Refresh the connections...
		//
		if (schema.haveConnectionsChanged() || complete)
		{
			ti = tiConn.getItems();

			// In complete refresh: delete all items first
			if (complete)
			{
				for (int i=0;i<ti.length;i++) ti[i].dispose();
				ti = tiConn.getItems();
			}
			
			// First delete no longer used items...
			for (int i=0;i<ti.length;i++)
			{
				String str = ti[i].getText();
				DatabaseMeta inf = schema.findConnection(str);
				if (inf!=null) idx = schema.indexOfConnection(inf); else idx=-1;
				if (idx<0 || idx>i) ti[i].dispose();
			}
			ti = tiConn.getItems();
			
			// Insert missing items in tree...
			int j=0;
			for (int i=0;i<schema.nrConnections();i++)
			{
				DatabaseMeta inf = schema.getConnection(i);
				String con_name = inf.getName();
				String ti_name = "";
				if (j<ti.length) ti_name = ti[j].getText();
				if (!con_name.equalsIgnoreCase(ti_name))
				{
					// insert at position j in tree
					TreeItem newitem = new TreeItem(tiConn, j);
					newitem.setText(inf.getName());
					newitem.setForeground(GUIResource.getInstance().getColorBlack());
					newitem.setImage(GUIResource.getInstance().getImageConnection());
					j++;
					ti = tiConn.getItems();
				}
				else
				{
					j++;
				}
			}
			// tiConn.setExpanded(true);
		}

		//ni.setImage(gv.hop_image);
		//ni.setImage(gv.step_images_small[steptype]);
		
		// Refresh the Steps...
		//
		if (schema.haveTablesChanged() || complete)
		{
			ti = tiStep.getItems();

			// In complete refresh: delete all items first
			if (complete)
			{
				for (int i=0;i<ti.length;i++) ti[i].dispose();
				ti = tiStep.getItems();
			}

			// First delete no longer used items...
			log.logDebug(toString(), "check steps");
			for (int i=0;i<ti.length;i++)
			{
				String str = ti[i].getText();
				log.logDebug(toString(), "  check step tree item #"+i+" : ["+str+"]");
				TableMeta inf = schema.findTable(str);
				if (inf!=null) idx = schema.indexOfTable(inf); else idx=-1;
				if (idx<0 || idx>i) 
				{
					log.logDebug(toString(), "     remove tree item ["+str+"]");
					ti[i].dispose();
				}
			}
			ti = tiStep.getItems();
			
			// Insert missing items in tree...
			int j=0;
			for (int i=0;i<schema.nrTables();i++)
			{
				TableMeta inf = schema.getTable(i);
				String step_name = inf.getName();
				String ti_name = "";
				if (j<ti.length) ti_name = ti[j].getText();
				if (!step_name.equalsIgnoreCase(ti_name))
				{
					// insert at position j in tree
					TreeItem newitem = new TreeItem(tiStep, j);
					newitem.setText(inf.getName());
					newitem.setImage(GUIResource.getInstance().getImageBol());
					j++;
					ti = tiStep.getItems();
				}
				else
				{
					j++;
				}
			}
			
			// See if the colors are still OK!
			for (int i=0;i<ti.length;i++)
			{
				Color col = ti[i].getForeground();
				Color newcol;
				if (schema.isTableUsedInRelationships(ti[i].getText()))
                {
                    newcol=GUIResource.getInstance().getColorBlack();
                }
                else
                {
                    newcol=GUIResource.getInstance().getColorGray();
                }
                
				if (!newcol.equals(col)) ti[i].setForeground(newcol);
			}

			tiStep.setExpanded(true);
		}
		
		// Refresh the Hops...
		//
		if (schema.haveRelationsipsChanged() || complete)
		{
			ti = tiTrns.getItems();

			// In complete refresh: delete all items first
			if (complete)
			{
				for (int i=0;i<ti.length;i++) ti[i].dispose();
				ti = tiTrns.getItems();
			}

			// First delete no longer used items...
			for (int i=0;i<ti.length;i++)
			{
				String str = ti[i].getText();
				RelationshipMeta inf = schema.findRelationship(str);
				if (inf!=null) idx = schema.indexOfRelationship(inf); else idx=-1;
				if (idx<0 || idx>i) ti[i].dispose();
			}
			ti = tiTrns.getItems();
			
			// Insert missing items in tree...
			int j=0;
			for (int i=0;i<schema.nrRelationships();i++)
			{
				RelationshipMeta inf = schema.getRelationship(i);
				String trans_name = inf.toString();
				String ti_name = "";
				if (j<ti.length) ti_name = ti[j].getText();
				if (!trans_name.equalsIgnoreCase(ti_name))
				{
					// insert at position j in tree
					TreeItem newitem = new TreeItem(tiTrns, j);
					newitem.setText(inf.toString());
					newitem.setForeground(GUIResource.getInstance().getColorBlack());
					newitem.setImage(GUIResource.getInstance().getImageHop());
					j++;
					ti = tiTrns.getItems();
				}
				else
				{
					j++;
				}
			}
			// tiTrns.setExpanded(false);
		}

		tCSH.setFocus();
		setShellText();
	}
	
	public void refreshGraph()
	{
		pmenugraph.redraw();
		setShellText();
	}

	private void setTreeImages()
	{
		tiConn.setImage(GUIResource.getInstance().getImageConnection());
		tiTrns.setImage(GUIResource.getInstance().getImageHop());
		tiStep.setImage(GUIResource.getInstance().getImageBol());
	}
	
	public DatabaseMeta getConnection(String name)
	{
		int i;
		
		for (i=0;i<schema.nrConnections();i++)
		{
			DatabaseMeta ci = schema.getConnection(i);
			if (ci.getName().equalsIgnoreCase(name))
			{
				return ci;
			}
		}
		return null;
	}

	public void setShellText()
	{
		String fname = schema.filename;
		if (shell.isDisposed()) return;
		if (fname!=null)
		{
			shell.setText(MENU_TITLE+" - "+fname+(schema.hasChanged()?" (changed)":""));
		}
		else
		{
			shell.setText(MENU_TITLE+(schema.hasChanged()?" (changed)":""));
		}
	}
	
	public void setFilename(String fname)
	{
		schema.filename = fname;
		setShellText();
	}
	
	private void printFile()
	{
		PrintSpool ps = new PrintSpool();
		Printer printer = ps.getPrinter(shell);
		
		// Create an image of the screen
		Point max = schema.getMaximum();
		
		//Image img_screen = new Image(trans, max.x, max.y);
		//img_screen.dispose();
		
		PaletteData pal = ps.getPaletteData();		
		
		ImageData imd = new ImageData(max.x, max.y, printer.getDepth(), pal);
		Image img = new Image(printer, imd);
		
		GC img_gc = new GC(img);
		
		// Clear the background first, fill with background color...
		Color bg = new Color(printer, schema.props.getBackgroundRGB());
		img_gc.setForeground(bg);
		img_gc.fillRectangle(0,0,max.x, max.y);
		bg.dispose();
		
		// Draw the transformation...
		pmenugraph.drawTrans(img_gc);
		
		//ShowImageDialog sid = new ShowImageDialog(shell, transMeta.props, img);
		//sid.open();
		
		ps.printImage(shell, schema.props, img);
		
		img_gc.dispose();
		img.dispose();
		ps.dispose();
	}
	
	public void saveSettings()
	{
		WindowProperty winprop = new WindowProperty(shell);
		schema.props.setScreen(winprop);
		schema.props.setLogLevel(log.getLogLevelDesc());
		schema.props.setSashWeights(sashform.getWeights());
		schema.props.saveProps();
	}

	public void loadSettings()
	{
		log.setLogLevel(schema.props.getLogLevel());
		
        GUIResource.getInstance().reload();
		
		schema.setMaxUndo(schema.props.getMaxUndo());
		
		schema.dbcache.setActive(schema.props.useDBCache());
	}
	
	public void changeLooks()
	{
		// tCSH.setFont(GUIResource.getInstance().getFontDefault());
		tCSH.setBackground(GUIResource.getInstance().getColorBlack());

		// tabfolder.setFont(GUIResource.getInstance().getFontDefault());
		
		pmenugraph.newProps();

		refreshTree();
		refreshGraph();
	}
	
	public void undoAction()
	{
		pmenugraph.forceFocus();
		
		TransAction ta = schema.previousUndo();
		if (ta==null) return;
		setUndoMenu(); // something changed: change the menu
		switch(ta.getType())
		{
			//
			// NEW
			//

			// We created a new step : undo this...
			case TransAction.TYPE_ACTION_NEW_TABLE:
				// Delete the step at correct location:
				{
					int idx = ta.getCurrentIndex()[0];
					schema.removeTable(idx);
					refreshTree();
					refreshGraph();
				}
				break;
	
			// We created a new connection : undo this...
			case TransAction.TYPE_ACTION_NEW_CONNECTION:
				// Delete the connection at correct location:
				{
					int idx = ta.getCurrentIndex()[0];
					schema.removeConnection(idx);
					refreshTree();
					refreshGraph();
				}
				break;
	
			// We created a new note : undo this...
			case TransAction.TYPE_ACTION_NEW_NOTE:
				// Delete the note at correct location:
				{
					int idx = ta.getCurrentIndex()[0];
					schema.removeNote(idx);
					refreshTree();
					refreshGraph();
				}
				break;
	
			// We created a new hop : undo this...
			case TransAction.TYPE_ACTION_NEW_RELATIONSHIP:
				// Delete the hop at correct location:
				{
					int idx = ta.getCurrentIndex()[0];
					schema.removeRelationship(idx);
					refreshTree();
					refreshGraph();
				}
				break;

			//
			// DELETE
			//

			// We delete a step : undo this...
			case TransAction.TYPE_ACTION_DELETE_TABLE:
				// un-Delete the step at correct location: re-insert
				{
					TableMeta ti = (TableMeta)ta.getCurrent()[0];
					int idx = ta.getCurrentIndex()[0];
					schema.addTable(idx, ti);
					refreshTree();
					refreshGraph();
				}
				break;
	
			// We deleted a connection : undo this...
			case TransAction.TYPE_ACTION_DELETE_CONNECTION:
				// re-insert the connection at correct location:
				{
					DatabaseMeta ci = (DatabaseMeta)ta.getCurrent()[0];
					int idx = ta.getCurrentIndex()[0];
					schema.addConnection(idx, ci);
					refreshTree();
					refreshGraph();
				}
				break;
	
			// We delete new note : undo this...
			case TransAction.TYPE_ACTION_DELETE_NOTE:
				// re-insert the note at correct location:
				{
					NotePadMeta ni = (NotePadMeta)ta.getCurrent()[0];
					int idx = ta.getCurrentIndex()[0];
					schema.addNote(idx, ni);
					refreshTree();
					refreshGraph();
				}
				break;
	
			// We deleted a new hop : undo this...
			case TransAction.TYPE_ACTION_DELETE_RELATIONSHIP:
				// re-insert the hop at correct location:
				{
					RelationshipMeta ri = (RelationshipMeta)ta.getCurrent()[0];
					int idx = ta.getCurrentIndex()[0];
					schema.addRelationship(idx, ri);
					refreshTree();
					refreshGraph();
				}
				break;


			//
			// CHANGE
			//

			// We changed a step : undo this...
			case TransAction.TYPE_ACTION_CHANGE_TABLE:
				// Delete the current step, insert previous version.
				{
					TableMeta prev = (TableMeta)ta.getPrevious()[0];
					int idx = ta.getCurrentIndex()[0];

					schema.removeTable(idx);
					schema.addTable(idx, prev);
					refreshTree();
					refreshGraph();
				}
				break;
	
			// We changed a connection : undo this...
			case TransAction.TYPE_ACTION_CHANGE_CONNECTION:
				// Delete & re-insert
				{
					DatabaseMeta prev = (DatabaseMeta)ta.getPrevious()[0];
					int idx = ta.getCurrentIndex()[0];

					schema.removeConnection(idx);
					schema.addConnection(idx, prev);
					refreshTree();
					refreshGraph();
				}
				break;
	
			// We changed a note : undo this...
			case TransAction.TYPE_ACTION_CHANGE_NOTE:
				// Delete & re-insert
				{
					int idx = ta.getCurrentIndex()[0];
					schema.removeNote(idx);
					NotePadMeta prev = (NotePadMeta)ta.getPrevious()[0];
					schema.addNote(idx, prev);
					refreshTree();
					refreshGraph();
				}
				break;
	
			// We changed a hop : undo this...
			case TransAction.TYPE_ACTION_CHANGE_RELATIONSHIP:
				// Delete & re-insert
				{
				    RelationshipMeta prev = (RelationshipMeta)ta.getPrevious()[0];
					int idx = ta.getCurrentIndex()[0];

					schema.removeRelationship(idx);
					schema.addRelationship(idx, prev);
					refreshTree();
					refreshGraph();
				}
				break;

			//
			// POSITION
			//
				
			// The position of a step has changed: undo this...
			case TransAction.TYPE_ACTION_POSITION_TABLE:
				// Find the location of the step:
				{
					int  idx[] = ta.getCurrentIndex();
					Point  p[] = ta.getPreviousLocation();
					for (int i = 0; i < p.length; i++) 
					{
						TableMeta tableinfo = schema.getTable(idx[i]);
						tableinfo.setLocation(p[i]);
					}
					refreshGraph();
				}
				break;
	
			// The position of a note has changed: undo this...
			case TransAction.TYPE_ACTION_POSITION_NOTE:
				int idx = ta.getCurrentIndex()[0];
				NotePadMeta npi = schema.getNote(idx);
				Point prev = ta.getPreviousLocation()[0];
				npi.setLocation(prev);
				refreshGraph();
				break;
			default: break;
		}
	}
	
	public void redoAction()
	{
		pmenugraph.forceFocus();

		TransAction ta = schema.nextUndo();
		if (ta==null) return;
		setUndoMenu(); // something changed: change the menu
		switch(ta.getType())
		{
		//
		// NEW
		//
		case TransAction.TYPE_ACTION_NEW_TABLE:
			// re-delete the step at correct location:
			{
				TableMeta ti = (TableMeta)ta.getCurrent()[0];
				int idx = ta.getCurrentIndex()[0];
				schema.addTable(idx, ti);
				refreshTree();
				refreshGraph();
			}
			break;

		case TransAction.TYPE_ACTION_NEW_CONNECTION:
			// re-insert the connection at correct location:
			{
				DatabaseMeta ci = (DatabaseMeta)ta.getCurrent()[0];
				int idx = ta.getCurrentIndex()[0];
				schema.addConnection(idx, ci);
				refreshTree();
				refreshGraph();
			}
			break;

		case TransAction.TYPE_ACTION_NEW_NOTE:
			// re-insert the note at correct location:
			{
				NotePadMeta ni = (NotePadMeta)ta.getCurrent()[0];
				int idx = ta.getCurrentIndex()[0];
				schema.addNote(idx, ni);
				refreshTree();
				refreshGraph();
			}
			break;

		case TransAction.TYPE_ACTION_NEW_RELATIONSHIP:
			// re-insert the hop at correct location:
			{
				RelationshipMeta ri = (RelationshipMeta)ta.getCurrent()[0];
				int idx = ta.getCurrentIndex()[0];
				schema.addRelationship(idx, ri);
				refreshTree();
				refreshGraph();
			}
			break;
		
		//	
		// DELETE
		//
		case TransAction.TYPE_ACTION_DELETE_TABLE:
			// re-remove the step at correct location:
			{
				int idx = ta.getCurrentIndex()[0];
				schema.removeTable(idx);
				refreshTree();
				refreshGraph();
			}
			break;

		case TransAction.TYPE_ACTION_DELETE_CONNECTION:
			// re-remove the connection at correct location:
			{
				int idx = ta.getCurrentIndex()[0];
				schema.removeConnection(idx);
				refreshTree();
				refreshGraph();
			}
			break;

		case TransAction.TYPE_ACTION_DELETE_NOTE:
			// re-remove the note at correct location:
			{
				int idx = ta.getCurrentIndex()[0];
				schema.removeNote(idx);
				refreshTree();
				refreshGraph();
			}
			break;

		case TransAction.TYPE_ACTION_DELETE_RELATIONSHIP:
			// re-remove the hop at correct location:
			{
				int idx = ta.getCurrentIndex()[0];
				schema.removeRelationship(idx);
				refreshTree();
				refreshGraph();
			}
			break;

		//
		// CHANGE
		//

		// We changed a step : undo this...
		case TransAction.TYPE_ACTION_CHANGE_TABLE:
			// Delete the current step, insert previous version.
			{
				TableMeta ri   = (TableMeta)ta.getCurrent()[0];
				int idx = ta.getCurrentIndex()[0];
				
				schema.removeTable(idx);
				schema.addTable(idx, ri);
				refreshTree();
				refreshGraph();
			}
			break;

		// We changed a connection : undo this...
		case TransAction.TYPE_ACTION_CHANGE_CONNECTION:
			// Delete & re-insert
			{
				DatabaseMeta ci = (DatabaseMeta)ta.getCurrent()[0];
				int idx = ta.getCurrentIndex()[0];

				schema.removeConnection(idx);
				schema.addConnection(idx, ci);
				refreshTree();
				refreshGraph();
			}
			break;

		// We changed a note : undo this...
		case TransAction.TYPE_ACTION_CHANGE_NOTE:
			// Delete & re-insert
			{
				NotePadMeta ni = (NotePadMeta)ta.getCurrent()[0];
				int idx = ta.getCurrentIndex()[0];

				schema.removeNote(idx);
				schema.addNote(idx, ni);
				refreshTree();
				refreshGraph();
			}
			break;

		// We changed a hop : undo this...
		case TransAction.TYPE_ACTION_CHANGE_RELATIONSHIP:
			// Delete & re-insert
			{
				RelationshipMeta ri   = (RelationshipMeta)ta.getCurrent()[0];
				int idx = ta.getCurrentIndex()[0];

				schema.removeRelationship(idx);
				schema.addRelationship(idx, ri);
				refreshTree();
				refreshGraph();
			}
			break;

		//
		// CHANGE POSITION
		//
		case TransAction.TYPE_ACTION_POSITION_TABLE:
			{
				// Find the location of the step:
				int idx[] = ta.getCurrentIndex();
				Point    p[]  = ta.getCurrentLocation();
				for (int i = 0; i < p.length; i++) 
				{
					TableMeta tableinfo = schema.getTable(idx[i]);
					tableinfo.setLocation(p[i]);
				}
				refreshGraph();
			}
			break;
		case TransAction.TYPE_ACTION_POSITION_NOTE:
			{
				int idx = ta.getCurrentIndex()[0];
				NotePadMeta npi = schema.getNote(idx);
				Point curr = ta.getCurrentLocation()[0];
				npi.setLocation(curr);
				refreshGraph();
			}
			break;
		default: break;
		}
	}
	
	public void setUndoMenu()
	{
		TransAction prev = schema.viewPreviousUndo();
		TransAction next = schema.viewNextUndo();
		
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
		schema.addUndo(obj, null, position, null, null, TransMeta.TYPE_UNDO_NEW);
		setUndoMenu();
	}	

	// Undo delete object
	public void addUndoDelete(Object obj[], int position[])
	{
		schema.addUndo(obj, null, position, null, null, TransMeta.TYPE_UNDO_DELETE);
		setUndoMenu();
	}	

	// Change of step, connection, hop or note...
	public void addUndoPosition(Object obj[], int pos[], Point prev[], Point curr[])
	{
		// It's better to store the indexes of the objects, not the objects itself!
		schema.addUndo(obj, null, pos, prev, curr, TransMeta.TYPE_UNDO_POSITION);
		setUndoMenu();
	}

	// Change of step, connection, hop or note...
	public void addUndoChange(Object from[], Object to[], int[] pos)
	{
		schema.addUndo(from, to, pos, null, null, TransMeta.TYPE_UNDO_CHANGE);
		setUndoMenu();
	}
	
	
	public void buildSQL()
	{
		SelectFieldDialog sfd = new SelectFieldDialog(shell, SWT.NONE, log, schema);
		if (sfd.open())
		{
			TableField f[] = sfd.fields;
			WhereCondition c[] = sfd.conditions;
			
			System.out.println("f.length = "+f.length+", c.length = "+c.length);
			
			String sql = schema.getSQL(f, c);
			if (sql!=null)
			{
				//EnterTextDialog etd = new EnterTextDialog(shell, schema.props, "SQL", "SQL:", sql, true);
				//if (etd.open()!=null)
				{
					schema.removeAllSelFields();
					for (int i=0;i<f.length;i++)
					{
						schema.addSelField(f[i]);
					}
					schema.removeAllSelConditions();
					for (int i=0;i<c.length;i++)
					{
						schema.addSelCondition(c[i]);
					}

					LogWriter newlog = LogWriter.getInstance(Const.MENU_LOG_FILE, false, LogWriter.LOG_LEVEL_BASIC);
					
					// build new transformation from this SQL!
					TransMeta trans = new TransMeta();
					
					// What's the connection?
					DatabaseMeta dbinfo = f[0].getTable().getDatabase();
					
					// First, add the connection!
					trans.addDatabase(dbinfo);
					
					// Then, add the TableInput step:					
					TableInputMeta tii = new TableInputMeta();
					tii.setDatabaseMeta( dbinfo );
					tii.setSQL( sql );
					tii.setChanged();
					StepMeta stii = new StepMeta(newlog, "TableInput", "Read from database", tii);
					stii.setLocation(50,50);
					stii.setDraw(true);
					trans.addStep(stii);
					
					// Then, rename the fields to their correct names
					SelectValuesMeta svi = new SelectValuesMeta();
					svi.allocate(f.length, 0, 0);
					for (int i=0;i<f.length;i++)
					{
						svi.getSelectName()[i] = f[i].getRenameAsField(dbinfo, i);
						svi.getSelectRename()[i] = f[i].getName();
						svi.getSelectLength()[i] = -2;
						svi.getSelectPrecision()[i] = -2;
					}
					
					StepMeta ssvi = new StepMeta(newlog, "SelectValues", "Rename values", svi);
					ssvi.setLocation(200,50);
					ssvi.setDraw(true);
					trans.addStep(ssvi);
					
					TransHopMeta hop = new TransHopMeta(stii, ssvi);
					trans.addTransHop(hop);
					
					// Also, add a final DummyTrans
					DummyTransMeta dti = new DummyTransMeta();
					StepMeta sdti = new StepMeta(newlog, "Dummy", "Dummy", dti);
					sdti.setLocation(350, 50);
					sdti.setDraw(true);
					sdti.setTerminator();
					trans.addStep(sdti);
					
					TransHopMeta hop2 = new TransHopMeta(ssvi, sdti);
					trans.addTransHop(hop2);
					
					// Put it into a cube file:
					CubeOutputMeta coi = new CubeOutputMeta();
					coi.setFilename( schema.getCubeFile() );
					StepMeta scoi = new StepMeta(newlog, "CubeOutput", "Write to cube", coi);
					scoi.setLocation(500, 50);
					scoi.setDraw(true);
					trans.addStep(scoi);
					
					TransHopMeta hop3 = new TransHopMeta(sdti, scoi);
					trans.addTransHop(hop3);
					
					trans.setName("Transformation generated by Kettle");

					/*
					Trans trun = new Trans(newlog, transMeta);
					trun.execute();
					trun.waitUntilFinished();
					
					RunThread rt = trun.findRunThread(sdti.getName());
					if (rt!=null)
					{
						ArrayList sniffer = rt.terminator_rows;
						 
						System.out.println("Size sniffer = "+sniffer.size());
						for (int i=0;i<sniffer.size();i++)
						{
							Row r = (Row)sniffer.get(i);
							System.out.println("#"+i+" : "+r);
						}
					}
					*/
					
					trans.clearChanged();  // Testing purpose only!
					
					// Launch spoon!
					Spoon sp = new Spoon(newlog, disp, trans, rep);
					sp.open();
					
					/*
					// Test FreeChart!
					DefaultPieDataset data = new DefaultPieDataset();
					data.setValue("Category 1", 43.2);
					data.setValue("Category 2", 27.9);
					data.setValue("Category 3", 79.5);
					
					PiePlot plot =  new PiePlot(data);
					plot.setBackgroundAlpha((float)0.5);
					plot.setForegroundAlpha((float)0.9);
					plot.setCircular(true);
					
					JFreeChart chart = new JFreeChart(plot); 
					
					chart.setAntiAlias(true);
					
					int ask_width = 800;
					int ask_height = 600;
					
					BufferedImage awt = chart.createBufferedImage(ask_width, ask_height);
					
					Image img = ImageConverter.convertBufferedImage(awt, trans);
					int width = img.getBounds().width;
					int height = img.getBounds().height;
					
					ShowImageDialog sid = new ShowImageDialog(shell, schema.props, img, width, height);
					sid.open();
								
					img.dispose();
					*/
				} 
			}
		}
	}
	
	public void clearDBCache()
	{
		// Determine what menu we selected from...

		TreeItem ti[] = tCSH.getSelection();
				
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
					schema.dbcache.clear(name);
				} 
			}
			else
			{
				if (name.equalsIgnoreCase(STRING_CONNECTIONS)) schema.dbcache.clear(null);
			}
		}
	}

	public void importTables()
	{
		// Determine what menu we selected from...

		TreeItem ti[] = tCSH.getSelection();
				
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
					DatabaseMeta dbinfo = schema.findConnection(name);
					DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, schema.props, SWT.NONE, dbinfo, schema.databases, false);
					String tablename = (String)std.open();
					if (tablename!=null)
					{
						ArrayList fields = new ArrayList();
						
						String shortname = tablename;
						int idx_last_dot = tablename.lastIndexOf(".");
						if (idx_last_dot>=0)
						{
							shortname=tablename.substring(idx_last_dot+1);
						}
						
						if (schema.findTable(shortname)!=null)
						{
							// find a new name for the table: add " 2", " 3", " 4", ... to name:
							int copy = 2;
							String newname = shortname+" "+copy;
							while (schema.findTable(newname)!=null)
							{
								copy++;
								newname = shortname+" "+copy;
							}
							shortname=newname;
						}
						
						TableMeta tableinfo = new TableMeta(shortname, tablename, dbinfo, fields, new ArrayList());
						Database db = new Database(dbinfo);
						try
						{
							db.connect();
							
							// System.out.println("Connected to database : "+dbinfo.toString());
							
							Row row = db.getTableFields(tablename);
							
							if (row!=null && row.size()>0)
							{
								// System.out.println("Found "+row.size()+" fields to retrieve.");
								
								for (int i=0;i<row.size();i++)
								{
									Value v = row.getValue(i);
									String dbname = /*tableinfo.getDBName()+"."+*/ v.getName();
									TableField f = new TableField(v.getName(), dbname, TableField.TYPE_FIELD_DIMENSION, TableField.TYPE_AGGREGATION_NONE, tableinfo);
									fields.add(f);
								}
							}
						}
						catch(KettleDatabaseException dbe)
						{
							MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
							mb.setMessage("Error reading fields of table : "+tablename+Const.CR+dbe.getMessage());
							mb.setText("Error!");
							mb.open();
						}
						finally
						{
							db.disconnect();
						}

						if (shortname.startsWith("D_")) tableinfo.setType(TableMeta.TYPE_TABLE_DIMENSION);
						if (shortname.startsWith("F_")) tableinfo.setType(TableMeta.TYPE_TABLE_FACT);
						

						schema.addTable(tableinfo);
						refreshTree();
					}
				} 
			}
			else
			{
				if (name.equalsIgnoreCase(STRING_CONNECTIONS)) schema.dbcache.clear(null);
			}
		}
	}

	public void exploreDB()
	{
		// Determine what menu we selected from...

		TreeItem ti[] = tCSH.getSelection();
				
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
					DatabaseMeta dbinfo = schema.findConnection(name);
					if (dbinfo!=null)
					{
						DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, schema.props, SWT.NONE, dbinfo, schema.databases, true );
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
				if (name.equalsIgnoreCase(STRING_CONNECTIONS)) schema.dbcache.clear(null);
			}
		}
	}
	
	
	public String toString()
	{
		return this.getClass().getName();
	}
	
	
	
	public static void main (String [] args) 
	{
		// Set default Locale:
		Locale.setDefault(Const.DEFAULT_LOCALE);
				
		LogWriter log=LogWriter.getInstance(Const.MENU_LOG_FILE, false, LogWriter.LOG_LEVEL_BASIC);

		/* Load the plugins etc.*/
		StepLoader stloader = StepLoader.getInstance();
		if (!stloader.read())
		{
			log.logError("Menu", "Error loading steps... halting Menu!");
			return;
		}
        
		final PMenu win = new PMenu(log, stloader);
		
		// Check licence info!
		win.demo_mode=false;
		
		// Read kettle transformation specified on command-line?
		if (args.length==1)
		{
			if (win.schema.loadXML(args[0], false))
			{
				win.setFilename(args[0]);
				win.schema.clearChanged();
			}
		}
		else
		{
			if (win.schema.props.openLastFile())
			{
				String lastfiles[] = win.schema.props.getLastFiles();
				if (lastfiles.length>0)
				{
					if (win.schema.loadXML(lastfiles[0], false))
					{
						win.setFilename(lastfiles[0]);
						win.schema.clearChanged();
					}
				}
			}
		}
		
		win.open ();
		while (!win.isDisposed ()) 
		{
			if (!win.readAndDispatch ()) win.sleep ();
		}
		win.dispose();

		// Close the logfile...
		log.close();
	}
}
