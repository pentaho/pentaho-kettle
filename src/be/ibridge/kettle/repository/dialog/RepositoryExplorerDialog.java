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

 

package be.ibridge.kettle.repository.dialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.w3c.dom.Document;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.GUIResource;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.DatabaseDialog;
import be.ibridge.kettle.core.dialog.EnterStringDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.widget.TreeMemory;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.repository.ProfileMeta;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.repository.UserInfo;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * This dialog displays the content of the repository in a Windows explorer like fashion.
 * The user can manipulate the objects in the repository as well as administer users, profiles, etc.
 * 
 * @author Matt
 * @since 18-mei-2003
 *
 */

public class RepositoryExplorerDialog extends Dialog 
{
	private static final String STRING_DATABASES       = Messages.getString("RepositoryExplorerDialog.Tree.String.Connections"); //$NON-NLS-1$
    private static final String STRING_PARTITIONS      = Messages.getString("RepositoryExplorerDialog.Tree.String.Partitions"); //$NON-NLS-1$
    private static final String STRING_SLAVES          = Messages.getString("RepositoryExplorerDialog.Tree.String.Slaves"); //$NON-NLS-1$
    private static final String STRING_CLUSTERS        = Messages.getString("RepositoryExplorerDialog.Tree.String.Clusters"); //$NON-NLS-1$
	public  static final String STRING_TRANSFORMATIONS = Messages.getString("RepositoryExplorerDialog.Tree.String.Transformations"); //$NON-NLS-1$
	public  static final String STRING_JOBS            = Messages.getString("RepositoryExplorerDialog.Tree.String.Jobs"); //$NON-NLS-1$
	public  static final String STRING_SCHEMAS         = Messages.getString("RepositoryExplorerDialog.Tree.String.Schemas"); //$NON-NLS-1$
	private static final String STRING_USERS           = Messages.getString("RepositoryExplorerDialog.Tree.String.Users"); //$NON-NLS-1$
	private static final String STRING_PROFILES        = Messages.getString("RepositoryExplorerDialog.Tree.String.Profiles"); //$NON-NLS-1$
	
	private static final int    ITEM_CATEGORY_NONE                        =  0;
	private static final int    ITEM_CATEGORY_ROOT                        =  1;
	private static final int    ITEM_CATEGORY_OBJECTS                     =  2;
	private static final int    ITEM_CATEGORY_DATABASES_ROOT              =  3;
	private static final int    ITEM_CATEGORY_DATABASE                    =  4;
	private static final int    ITEM_CATEGORY_TRANSFORMATIONS_ROOT        =  5;
	private static final int    ITEM_CATEGORY_TRANSFORMATION              =  6;
	private static final int    ITEM_CATEGORY_TRANSFORMATION_DIRECTORY    =  7;
	private static final int    ITEM_CATEGORY_JOBS_ROOT                   =  8;
	private static final int    ITEM_CATEGORY_JOB                         =  9;
	private static final int    ITEM_CATEGORY_JOB_DIRECTORY               = 10;
	private static final int    ITEM_CATEGORY_SCHEMAS_ROOT                = 11;
	private static final int    ITEM_CATEGORY_SCHEMA                      = 12;
	private static final int    ITEM_CATEGORY_SCHEMA_DIRECTORY            = 13;
	private static final int    ITEM_CATEGORY_USERS_ROOT                  = 14;
	private static final int    ITEM_CATEGORY_USER                        = 15;
	private static final int    ITEM_CATEGORY_PROFILES_ROOT               = 16;
	private static final int    ITEM_CATEGORY_PROFILE                     = 17;
    private static final int    ITEM_CATEGORY_PARTITIONS_ROOT             = 18;
    private static final int    ITEM_CATEGORY_PARTITION                   = 19;
    private static final int    ITEM_CATEGORY_SLAVES_ROOT                 = 20;
    private static final int    ITEM_CATEGORY_SLAVE                       = 21;
    private static final int    ITEM_CATEGORY_CLUSTERS_ROOT               = 22;
    private static final int    ITEM_CATEGORY_CLUSTER                     = 23;
	
	private Shell     shell;
	private Tree      wTree;
	private Button    wOK;
	private Button    wCancel;

	private LogWriter log;
	private Props props;
	private Repository     rep;
	private UserInfo       userinfo;

	private String              objectName;   // Return this object to do something with it...
	private RepositoryDirectory objectDir;    // The directory to which it belongs. 
	private String              objectType;   // Type of return object
	
	private Color dircolor;
	
	private String debug;
    
    private int sortColumn;
    private boolean ascending;
    private TreeColumn nameColumn;
    private TreeColumn userColumn;
    private TreeColumn changedColumn;
    
    /** @deprecated */
    public RepositoryExplorerDialog(Shell par, Props pr, int style, LogWriter l, Repository rep, UserInfo ui)
    {
        this(par, style, rep, ui);
    }

	public RepositoryExplorerDialog(Shell par, int style, Repository rep, UserInfo ui)
	{
		super(par, style);
		this.props=Props.getInstance();
		this.log=LogWriter.getInstance();
		this.rep=rep;
		this.userinfo=ui;

		objectName = null;
        sortColumn = 1;
        ascending = true;
	}
    
    private static final String STRING_REPOSITORY_EXPLORER_TREE_NAME = "Repository Exporer Tree Name";

	public String open() 
	{
        debug="opening repository explorer"; //$NON-NLS-1$
        
        try
        {
    		dircolor = GUIResource.getInstance().getColorDirectory();
    		
            debug="open new shell"; //$NON-NLS-1$
            Shell parent = getParent();
    		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
    		props.setLook(shell);
    		shell.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Title")+rep.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
    		
    		FormLayout formLayout = new FormLayout ();
    		formLayout.marginWidth  = Const.FORM_MARGIN;
    		formLayout.marginHeight = Const.FORM_MARGIN;
    		
    		shell.setLayout (formLayout);
     		
            // Add a menu on top!
            Menu mBar = new Menu(shell, SWT.BAR);
            shell.setMenuBar(mBar);
            
            ////////////////////////////////////////////////////
            // FILE
            MenuItem mFile = new MenuItem(mBar, SWT.CASCADE); 
            mFile.setText(Messages.getString("RepositoryExplorerDialog.Menu.File")); //$NON-NLS-1$
            Menu msFile = new Menu(shell, SWT.DROP_DOWN);
            mFile.setMenu(msFile);
            
            // File export ALL
            //
            MenuItem miFileExport = new MenuItem(msFile, SWT.CASCADE); 
            miFileExport.setText(Messages.getString("RepositoryExplorerDialog.Menu.FileExportAll")); //$NON-NLS-1$
            miFileExport.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { exportAll(); } });
            
            // File import ALL
            //
            MenuItem miFileImport = new MenuItem(msFile, SWT.CASCADE); 
            miFileImport.setText(Messages.getString("RepositoryExplorerDialog.Menu.FileImportAll")); //$NON-NLS-1$
            miFileImport.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { importAll(); } });

            new MenuItem(msFile, SWT.SEPARATOR);
            
            // File close
            //
            MenuItem miFileClose= new MenuItem(msFile, SWT.CASCADE); 
            miFileClose.setText(Messages.getString("RepositoryExplorerDialog.Menu.FileClose")); //$NON-NLS-1$
            miFileClose.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { handleOK(); } });
            
            
     		// Tree
     		wTree = new Tree(shell, SWT.MULTI | SWT.BORDER /*| (multiple?SWT.CHECK:SWT.NONE)*/);
            wTree.setHeaderVisible(true);
     		props.setLook(wTree);
    
            // Add some columns to it as well...
            nameColumn = new TreeColumn(wTree, SWT.LEFT);
            nameColumn.setText(Messages.getString("RepositoryExplorerDialog.Column.Name")); //$NON-NLS-1$
            nameColumn.setWidth(350);
            nameColumn.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { setSort(1); } });
            
            userColumn = new TreeColumn(wTree, SWT.LEFT);
            userColumn.setText(Messages.getString("RepositoryExplorerDialog.Column.User")); //$NON-NLS-1$
            userColumn.setWidth(100);
            userColumn.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { setSort(2); } });

            changedColumn = new TreeColumn(wTree, SWT.LEFT);
            changedColumn.setText(Messages.getString("RepositoryExplorerDialog.Column.Changed")); //$NON-NLS-1$
            changedColumn.setWidth(100);
            changedColumn.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { setSort(3); } });
            
            // Add a memory to the tree.
            TreeMemory.addTreeListener(wTree,STRING_REPOSITORY_EXPLORER_TREE_NAME);
            
     		// Buttons
    		wOK = new Button(shell, SWT.PUSH); 
    		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
    		wCancel = new Button(shell, SWT.PUSH); 
    		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$
    				
    		FormData fdTree      = new FormData(); 
    		int margin =  10;
    
    		fdTree.left   = new FormAttachment(0, 0); // To the right of the label
    		fdTree.top    = new FormAttachment(0, 0);
    		fdTree.right  = new FormAttachment(100, 0);
    		fdTree.bottom = new FormAttachment(100, -50);
    		wTree.setLayoutData(fdTree);
    
    		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null );
    
    		// Add listeners
    		wOK.addListener(SWT.Selection, new Listener ()
    			{
    				public void handleEvent (Event e) 
    				{
    					handleOK();
    				}
    			}
    		);
    		// Add listeners
    		wCancel.addListener(SWT.Selection, new Listener ()
    			{
    				public void handleEvent (Event e) 
    				{
    					dispose();
    				}
    			}
    		);
    		SelectionAdapter selAdapter=new SelectionAdapter()
    			{
    				public void widgetDefaultSelected(SelectionEvent e)
    				{
    					doubleClick();	
    				}
    			};
    		wTree.addSelectionListener(selAdapter);
    		
    		wTree.addMouseListener(new MouseAdapter()
    			{
    				public void mouseDown(MouseEvent e)
    				{
    					if (e.button == 3) // right click!
    					{
    						setTreeMenu();
    					}
    				}
    			}
    		);
    		
    		wTree.addKeyListener(new KeyAdapter() 
    			{
    				public void keyPressed(KeyEvent e) 
    				{
    					// F2 --> rename...
    					if (e.keyCode == SWT.F2)    { renameInTree(); }
    					// F5 --> refresh...
    					if (e.keyCode == SWT.F5)    { refreshTree(); }
    				}
    			}
    		);
    		
    		// Drag & Drop
    		Transfer[] ttypes = new Transfer[] {TextTransfer.getInstance() };
    		
    		DragSource ddSource = new DragSource( wTree, DND.DROP_MOVE );
    		
    		ddSource.setTransfer(ttypes);
    		ddSource.addDragListener(new DragSourceListener() 
    			{
    				public void dragStart(DragSourceEvent event)
    				{ 
                        debug="drag start"; //$NON-NLS-1$
    					event.doit=true;
    				}
    	
    				public void dragSetData(DragSourceEvent event) 
    				{
                        debug="drag set data"; //$NON-NLS-1$

    					event.data = ""; //$NON-NLS-1$
    					event.doit = false;
    					
    					TreeItem ti[] = wTree.getSelection();
    					if (ti.length>=1)
    					{
    						int cat = getItemCategory(ti[0]);
    						//
    						// Drag around a transformation...
    						//
    						if (cat==ITEM_CATEGORY_TRANSFORMATION)
    						{
                                debug="drag set: drag around transformation"; //$NON-NLS-1$
    							RepositoryDirectory repdir = getDirectory(ti[0]);
    							if (repdir!=null)
    							{
    								//
    								// Pass info as a piece of XML
    								//
    								String xml=XMLHandler.getXMLHeader();
    								xml+="<dragdrop>"+Const.CR; //$NON-NLS-1$
    								xml+="  "+XMLHandler.addTagValue("directory", repdir.getPath()); //$NON-NLS-1$ //$NON-NLS-2$
    								xml+="  "+XMLHandler.addTagValue("transformation", ti[0].getText()); //$NON-NLS-1$ //$NON-NLS-2$
    								xml+="</dragdrop>"+Const.CR; //$NON-NLS-1$
    								
    								event.data = xml;
    								event.doit = true;
    							}
    						}
                            else
    						if (cat==ITEM_CATEGORY_JOB)
    						{
                                debug="drag set: drag around job"; //$NON-NLS-1$
    							RepositoryDirectory repdir = getDirectory(ti[0]);
    							if (repdir!=null)
    							{
    								//
    								// Pass info as a piece of XML
    								//
    								String xml=XMLHandler.getXMLHeader();
    								xml+="<dragdrop>"+Const.CR; //$NON-NLS-1$
    								xml+="  "+XMLHandler.addTagValue("directory", repdir.getPath()); //$NON-NLS-1$ //$NON-NLS-2$
    								xml+="  "+XMLHandler.addTagValue("job", ti[0].getText()); //$NON-NLS-1$ //$NON-NLS-2$
    								xml+="</dragdrop>"+Const.CR; //$NON-NLS-1$
    								
    								event.data = xml;
    								event.doit = true;
    							}
    						}
                            else
                            {
                                debug="do nothing"; //$NON-NLS-1$
                                String xml=XMLHandler.getXMLHeader();
                                xml+="<dragdrop>"+Const.CR; //$NON-NLS-1$
                                xml+="</dragdrop>"+Const.CR; //$NON-NLS-1$
                                event.data=xml;
                                event.doit=true;
                            }
    					}
    				}
    	
    				public void dragFinished(DragSourceEvent event) {}
    			}
    		);
    		
    		DropTarget ddTarget = new DropTarget(wTree, DND.DROP_MOVE );
    		ddTarget.setTransfer(ttypes);
    		ddTarget.addDropListener(new DropTargetListener() 
    		{
    			public void dragEnter(DropTargetEvent event) 
    			{ 
    			}
    			
    			public void dragLeave(DropTargetEvent event) 
    			{ 
                    debug="drag leave"; //$NON-NLS-1$
    			}
    			
    			public void dragOperationChanged(DropTargetEvent event) 
    			{
    			}
    			
    			public void dragOver(DropTargetEvent event) 
    			{ 
                    debug="drag over";    				 //$NON-NLS-1$
    			}
    			public void drop(DropTargetEvent event) 
    			{
                    try
                    {
                        debug="Drop item in tree"; //$NON-NLS-1$
                        
        				if (event.data == null)  // no data to copy, indicate failure in event.detail 
        				{
        					event.detail = DND.DROP_NONE;
        					return;
        				}
        					
        				// event.feedback = DND.FEEDBACK_NONE;
        
        				TreeItem ti = (TreeItem)event.item;
        				if (ti!=null)
        				{
                            debug="Get category"; //$NON-NLS-1$
        					int category = getItemCategory(ti);

        					if (category==ITEM_CATEGORY_TRANSFORMATION_DIRECTORY || category==ITEM_CATEGORY_TRANSFORMATION)
        					{
                                debug="Get directory"; //$NON-NLS-1$
        						RepositoryDirectory repdir = getDirectory(ti);
        						if (repdir!=null)
        						{
        							event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
        							
        							if (moveTransformation((String)event.data, repdir))
        							{
        								refreshTree();
        							}
                                    else
                                    {
                                        MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK );
                                        mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Move.UnableToMove.Message")); //$NON-NLS-1$
                                        mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Move.UnableToMove.Title")); //$NON-NLS-1$
                                        mb.open();
                                    }
        						}
        					}
                            else
        					if (category==ITEM_CATEGORY_JOB_DIRECTORY || category==ITEM_CATEGORY_JOB)
        					{
                                debug="Get directory"; //$NON-NLS-1$
        						RepositoryDirectory repdir = getDirectory(ti);
        						if (repdir!=null)
        						{
        							event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
        							
        							if (moveJob((String)event.data, repdir))
        							{
        								refreshTree();
        							}
                                    else
                                    {
                                        MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK );
                                        mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Job.Move.UnableToMove.Message")); //$NON-NLS-1$
                                        mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Job.Move.UnableToMove.Title")); //$NON-NLS-1$
                                        mb.open();
                                    }
        						}
        					}
                            else
                            {
                                MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK );
                                mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Move.SorryOneItemAtATime.Message")); //$NON-NLS-1$
                                mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Move.SorryOneItemAtATime.Title")); //$NON-NLS-1$
                                mb.open();
                            }
        				}
                    }
                    catch(Throwable e)
                    {
                        new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Drop.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Drop.UnexpectedError.Message1")+debug+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Dialog.Drop.UnexpectedError.Message2"), new Exception(e)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    }
    			}
    
    			public void dropAccept(DropTargetEvent event) 
    			{
                    debug="drop accept"; //$NON-NLS-1$
    			}
    		});
    
    
    		// Detect X or ALT-F4 or something that kills this window...
    		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { dispose(); } } );
            
            debug="set screen size and position"; //$NON-NLS-1$
    
    		BaseStepDialog.setSize(shell, 400, 480, true);
    
    		setSort(1); // refreshes too.
    
    		shell.open();
    		Display display = parent.getDisplay();
    		while (!shell.isDisposed()) 
    		{
    			if (!display.readAndDispatch()) display.sleep();
    		}
        }
        catch(Throwable e)
        {
            new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Main.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Main.UnexpectedError.Message1")+debug+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Dialog.Main.UnexpectedError.Message2")+Const.CR+Messages.getString("RepositoryExplorerDialog.Dialog.Main.UnexpectedError.Message3"), new Exception(e)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }
		return objectName;

	}
	
	protected void setSort(int i)
    {
        if (sortColumn==i)
        {
            ascending=!ascending;
        }
        else
        {
            sortColumn=i;
            ascending=true;
        }
        
        if (sortColumn>0 && sortColumn<4)
        {
            TreeColumn column = wTree.getColumn(sortColumn-1);
            wTree.setSortColumn(column);
            wTree.setSortDirection(ascending?SWT.UP:SWT.DOWN);
        }
        refreshTree();
    }

    public RepositoryDirectory getDirectory(TreeItem ti)
	{
		RepositoryDirectory repdir = null;
		
		int level = Const.getTreeLevel(ti);
		String path[] = Const.getTreeStrings(ti);
		
		if (level>2)
		{
			int cat = getItemCategory(ti);
			switch(cat)
			{
			case ITEM_CATEGORY_JOB:
			case ITEM_CATEGORY_SCHEMA:
			case ITEM_CATEGORY_TRANSFORMATION:
				{
					// The first 3 levels of text[] don't belong to the path to this transformation!
					String realpath[] = new String[level-3];
					for (int i=0;i<realpath.length;i++) realpath[i] = path[i+3];
					
					repdir = rep.getDirectoryTree().findDirectory(realpath);
				}
				break;
			case ITEM_CATEGORY_JOB_DIRECTORY:
			case ITEM_CATEGORY_SCHEMA_DIRECTORY:
			case ITEM_CATEGORY_TRANSFORMATION_DIRECTORY:
				{
					// The first 3 levels of text[] don't belong to the path to this transformation!
					String realpath[] = new String[level-2];
					for (int i=0;i<realpath.length;i++) realpath[i] = path[i+3];
					
					repdir = rep.getDirectoryTree().findDirectory(realpath);
				}
				break;
			default: break;
			}
		}
		return repdir;
	}
	
	public boolean sameCategory(TreeItem[] tisel)
	{
		if (tisel.length==0) return false;
		
		int cat = getItemCategory(tisel[0]);
		for (int i=1;i<tisel.length;i++)
		{
			if (cat!=getItemCategory(tisel[i])) return false;
		}
		
		return true;
	}
	
	public void setTreeMenu()
	{
		Menu mTree = new Menu(shell, SWT.POP_UP);

		final TreeItem tisel[]=wTree.getSelection();
		if (tisel.length==1 || sameCategory(tisel))
		{
			final TreeItem ti = tisel[0];
			final int level = Const.getTreeLevel(ti);
			final String path[] = Const.getTreeStrings(ti);
			final String item = ti.getText();
		
			int cat = getItemCategory(ti);
			
			switch(cat)
			{
			case ITEM_CATEGORY_OBJECTS                     :
				{
					// Export all
					MenuItem miExp  = new MenuItem(mTree, SWT.PUSH); 
					miExp.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Objects.ExportAll")); //$NON-NLS-1$
					SelectionAdapter lsExp = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { exportAll(); } };
					miExp.addSelectionListener( lsExp );

					// Import all
					MenuItem miImp  = new MenuItem(mTree, SWT.PUSH); 
					miImp.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Objects.ImportAll")); //$NON-NLS-1$
					SelectionAdapter lsImp = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { importAll(); } };
					miImp.addSelectionListener( lsImp );
					miImp.setEnabled(!userinfo.isReadonly());

					// Export transMeta
					MenuItem miTrans  = new MenuItem(mTree, SWT.PUSH); 
					miTrans.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Objects.ExportTrans")); //$NON-NLS-1$
					SelectionAdapter lsTrans = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { exportTransformations(); } };
					miTrans.addSelectionListener( lsTrans );

					// Export jobs
					MenuItem miJobs  = new MenuItem(mTree, SWT.PUSH); 
					miJobs.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Objects.ExportJob")); //$NON-NLS-1$
					SelectionAdapter lsJobs = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { exportJobs(); } };
					miJobs.addSelectionListener( lsJobs );
				}
				break;
				
			case ITEM_CATEGORY_DATABASES_ROOT              :
				{
					// New database
					MenuItem miNew  = new MenuItem(mTree, SWT.PUSH); 
					miNew.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.ConnectionsRoot.New")); //$NON-NLS-1$
					SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newDatabase(); } };
					miNew.addSelectionListener( lsNew );
					miNew.setEnabled(!userinfo.isReadonly());
				}
				break;
				
			case ITEM_CATEGORY_DATABASE                    :
				{
					// New database
					MenuItem miNew  = new MenuItem(mTree, SWT.PUSH); 
					miNew.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Connections.New")); //$NON-NLS-1$
					SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newDatabase(); } };
					miNew.addSelectionListener( lsNew );
					miNew.setEnabled(!userinfo.isReadonly());
					// Edit database info
					MenuItem miEdit  = new MenuItem(mTree, SWT.PUSH); 
					miEdit.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Connections.Edit")); //$NON-NLS-1$
					SelectionAdapter lsEdit = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editDatabase(item); } };
					miEdit.addSelectionListener( lsEdit );
					miEdit.setEnabled(!userinfo.isReadonly());
					// Delete database info
					MenuItem miDel  = new MenuItem(mTree, SWT.PUSH); 
					miDel.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Connections.Delete")); //$NON-NLS-1$
					SelectionAdapter lsDel = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { delDatabase(item); } };
					miDel.addSelectionListener( lsDel );
					miDel.setEnabled(!userinfo.isReadonly());
				}
				break;
				
			case ITEM_CATEGORY_TRANSFORMATIONS_ROOT        :
				break;
			case ITEM_CATEGORY_TRANSFORMATION              :
				if (level>=3)
				{
					// The first 3 levels of text[] don't belong to the path to this transformation!
					String realpath[] = new String[level-3];
					for (int i=0;i<realpath.length;i++) realpath[i] = path[i+3];
					
					// Find the directory in the directory tree...
					final RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(realpath);

					// Open transformation...
					MenuItem miOpen  = new MenuItem(mTree, SWT.PUSH); 
					miOpen.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Transformations.Open")); //$NON-NLS-1$
					miOpen.addSelectionListener( 
						new SelectionAdapter() 
						{ 
							public void widgetSelected(SelectionEvent e) 
							{ 
								openTransformation(item, repdir);
							}
						}
					);
					// Delete transformation
					MenuItem miDel  = new MenuItem(mTree, SWT.PUSH); 
					miDel.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Transformations.Delete")); //$NON-NLS-1$
					miDel.addSelectionListener( 
						new SelectionAdapter() 
						{ 
							public void widgetSelected(SelectionEvent e) 
							{ 
								delSelectedTransformations(); 
							}
						}
					);
					// Rename transformation
					miDel.setEnabled(!userinfo.isReadonly());
					MenuItem miRen  = new MenuItem(mTree, SWT.PUSH); 
					miRen.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Transformations.Rename")); //$NON-NLS-1$
					miRen.addSelectionListener( 
						new SelectionAdapter() 
						{ 
							public void widgetSelected(SelectionEvent e) 
							{ 
								renameTransformation(item, repdir); 
							}
						}
					);
					miRen.setEnabled(!userinfo.isReadonly());
				}
				break;
				
			case ITEM_CATEGORY_JOB_DIRECTORY               :
			case ITEM_CATEGORY_SCHEMA_DIRECTORY            :
			case ITEM_CATEGORY_TRANSFORMATION_DIRECTORY    :
				if (level>=3)
				{
					// The first 2 levels of text[] don't belong to the path to this transformation!
					String realpath[] = new String[level-2];
					for (int i=0;i<realpath.length;i++) realpath[i] = path[i+3];
					
					// Find the directory in the directory tree...
					final RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(realpath);

					// Open transformation...
					MenuItem miCreate  = new MenuItem(mTree, SWT.PUSH); 
					miCreate.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.TransDirectory.Create")); //$NON-NLS-1$
					miCreate.addSelectionListener( 
						new SelectionAdapter() 
						{ 
							public void widgetSelected(SelectionEvent e) 
							{ 
								createDirectory(ti, repdir);
							}
						}
					);
					if (level>3) // Can't rename or delete root directory...
					{
						// Rename directory
						MenuItem miRename  = new MenuItem(mTree, SWT.PUSH); 
						miRename.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.TransDirectory.Rename")); //$NON-NLS-1$
						miRename.addSelectionListener( 
							new SelectionAdapter() 
							{ 
								public void widgetSelected(SelectionEvent e) 
								{ 
									renameDirectory(ti, repdir);
								}
							}
						);
						// Delete directory
						MenuItem miDelete  = new MenuItem(mTree, SWT.PUSH); 
						miDelete.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.TransDirectory.Delete")); //$NON-NLS-1$
						miDelete.addSelectionListener( 
							new SelectionAdapter() 
							{ 
								public void widgetSelected(SelectionEvent e) 
								{ 
									delDirectory(ti, repdir);
								}
							}
						);
					}
				}
				break;
				
			case ITEM_CATEGORY_JOBS_ROOT                   :
				break;
				
			case ITEM_CATEGORY_JOB                         :
				{
					// The first 3 levels of text[] don't belong to the path to this transformation!
					String realpath[] = new String[level-3];
					for (int i=0;i<realpath.length;i++) realpath[i] = path[i+3];
					
					// Find the directory in the directory tree...
					final RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(realpath);
	

					// Delete job
					MenuItem miDel  = new MenuItem(mTree, SWT.PUSH); 
					miDel.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Jobs.Delete")); //$NON-NLS-1$
					miDel.addSelectionListener( 
						new SelectionAdapter() 
						{ 
							public void widgetSelected(SelectionEvent e) 
							{ 
								if (delJob(item, repdir)) ti.dispose();
							}
						}
					);
					// Rename job
					miDel.setEnabled(!userinfo.isReadonly());
					MenuItem miRen  = new MenuItem(mTree, SWT.PUSH); 
					miRen.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Jobs.Rename")); //$NON-NLS-1$
					miRen.addSelectionListener( 
						new SelectionAdapter() 
						{ 
							public void widgetSelected(SelectionEvent e) 
							{ 
								renameJob(ti, item, repdir); 
							}
						}
					);
					miRen.setEnabled(!userinfo.isReadonly());
				}
				break;
				
			case ITEM_CATEGORY_SCHEMAS_ROOT                :
				break;
				
			case ITEM_CATEGORY_SCHEMA                      :
				break;
				
			case ITEM_CATEGORY_USERS_ROOT                  :
				{
					mTree = new Menu(shell, SWT.POP_UP);
					// New user
					MenuItem miNew  = new MenuItem(mTree, SWT.PUSH); 
					miNew.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.UsersRoot.New")); //$NON-NLS-1$
					SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newUser(); } };
					miNew.addSelectionListener( lsNew );
					miNew.setEnabled(!userinfo.isReadonly());
				}
				break;
				
			case ITEM_CATEGORY_USER                        :
				{
					// New user
					MenuItem miNew  = new MenuItem(mTree, SWT.PUSH); 
					miNew.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Users.New")); //$NON-NLS-1$
					SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newUser(); } };
					miNew.addSelectionListener( lsNew );
					miNew.setEnabled(!userinfo.isReadonly());
					// Edit user info
					MenuItem miEdit  = new MenuItem(mTree, SWT.PUSH); 
					miEdit.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Users.Edit")); //$NON-NLS-1$
					SelectionAdapter lsEdit = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editUser(item); } };
					miEdit.addSelectionListener( lsEdit );
					miEdit.setEnabled(!userinfo.isReadonly());
					// Rename user
					MenuItem miRen = new MenuItem(mTree, SWT.PUSH); 
					miRen.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Users.Rename")); //$NON-NLS-1$
					SelectionAdapter lsRen = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { renameUser(); } };
					miRen.addSelectionListener( lsRen );
					miRen.setEnabled(!userinfo.isReadonly());
					// Delete user info
					MenuItem miDel  = new MenuItem(mTree, SWT.PUSH); 
					miDel.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Users.Delete")); //$NON-NLS-1$
					SelectionAdapter lsDel = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { delUser(item); } };
					miDel.addSelectionListener( lsDel );
					miDel.setEnabled(!userinfo.isReadonly());
				}
				break;
				
			case ITEM_CATEGORY_PROFILES_ROOT               :
				{
					MenuItem miNew  = new MenuItem(mTree, SWT.PUSH); 
					miNew.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.ProfilesRoot.New")); //$NON-NLS-1$
					SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newProfile(); } };
					miNew.addSelectionListener( lsNew );
					miNew.setEnabled(!userinfo.isReadonly());
				}
				break;
				
			case ITEM_CATEGORY_PROFILE                     :
				{
					// New profile
					MenuItem miNew  = new MenuItem(mTree, SWT.PUSH); 
					miNew.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Profiles.New")); //$NON-NLS-1$
					SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newProfile(); } };
					miNew.addSelectionListener( lsNew );
					miNew.setEnabled(!userinfo.isReadonly());
					// Edit profile info
					MenuItem miEdit  = new MenuItem(mTree, SWT.PUSH); 
					miEdit.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.ProfilesRoot.Edit")); //$NON-NLS-1$
					SelectionAdapter lsEdit = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editProfile(item); } };
					miEdit.addSelectionListener( lsEdit );
					miEdit.setEnabled(!userinfo.isReadonly());
					// Rename profile
					MenuItem miRen = new MenuItem(mTree, SWT.PUSH); 
					miRen.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.ProfilesRoot.Rename")); //$NON-NLS-1$
					SelectionAdapter lsRen = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { renameProfile(); } };
					miRen.addSelectionListener( lsRen );
					miRen.setEnabled(!userinfo.isReadonly());
					// Delete profile info
					MenuItem miDel  = new MenuItem(mTree, SWT.PUSH); 
					miDel.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.ProfilesRoot.Delete")); //$NON-NLS-1$
					SelectionAdapter lsDel = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { delProfile(item); } };
					miDel.addSelectionListener( lsDel );
					miDel.setEnabled(!userinfo.isReadonly());
				}
				break;
				
			default: 
				mTree = null;
			}
		}
		
		wTree.setMenu(mTree);
	}
	
	public void renameInTree()
	{
		TreeItem ti[]=wTree.getSelection();
		if (ti.length==1)
		{
			// Get the parent.
			final TreeItem item = ti[0];
			
			int level = Const.getTreeLevel(item);
			String text[] = Const.getTreeStrings(item);
			
			int cat = getItemCategory(item);
			
			switch(cat)
			{
			case ITEM_CATEGORY_DATABASE : 
				renameDatabase(); 
                break;
			case ITEM_CATEGORY_TRANSFORMATION :
				{
					final String name = item.getText();
	
					// The first 3 levels of text[] don't belong to the path to this transformation!
					String path[] = new String[level-3];
					for (int i=0;i<path.length;i++) path[i] = text[i+3];
					
					// Find the directory in the directory tree...
					RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(path);
	
					if (repdir!=null) renameTransformation(name, repdir); 
				}
				break;
			case ITEM_CATEGORY_JOB :
				{
					final String name = item.getText();
	
					// The first 3 levels of text[] don't belong to the path to this transformation!
					String path[] = new String[level-3];
					for (int i=0;i<path.length;i++) path[i] = text[i+3];
					
					// Find the directory in the directory tree...
					RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(path);
	
					if (repdir!=null) renameJob(name, repdir); 
				}
				break;
			case ITEM_CATEGORY_USER:
				renameUser();
				break;
				
			case ITEM_CATEGORY_PROFILE:
				renameProfile();
				break;
			
			default: break;
			}
		}
	}

	public void dispose()
	{
        rep.rollback();

        props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public void handleOK()
	{
	    try
	    {
	        rep.commit();
	    }
	    catch(KettleException e)
	    {
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.PopupMenu.Dialog.ErrorCommitingChanges.Title"), Messages.getString("RepositoryExplorerDialog.PopupMenu.Dialog.ErrorCommitingChanges.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
	    }
		dispose();
	}
	
	public void refreshTree()
	{
		try
		{
			wTree.removeAll();
			
			// Load the directory tree:
			rep.setDirectoryTree( new RepositoryDirectory(rep) );
	
			TreeItem tiTree = new TreeItem(wTree, SWT.NONE); 
			tiTree.setText(rep.getName()==null?"-":rep.getName()); //$NON-NLS-1$
			
			// The Databases...				
			TreeItem tiParent = new TreeItem(tiTree, SWT.NONE); 
			tiParent.setText(STRING_DATABASES);
	
			String names[] = rep.getDatabaseNames();			
			for (int i=0;i<names.length;i++)
			{
				TreeItem newDB = new TreeItem(tiParent, SWT.NONE);
				newDB.setText(Const.NVL(names[i], ""));
			}
	
            // The partition schemas...             
            tiParent = new TreeItem(tiTree, SWT.NONE); 
            tiParent.setText(STRING_PARTITIONS);
    
            names = rep.getPartitionSchemaNames();          
            for (int i=0;i<names.length;i++)
            {
                TreeItem newItem = new TreeItem(tiParent, SWT.NONE);
                newItem.setText(Const.NVL(names[i], ""));
            }
            
            // The slaves...         
            tiParent = new TreeItem(tiTree, SWT.NONE); 
            tiParent.setText(STRING_SLAVES);
    
            names = rep.getSlaveNames();          
            for (int i=0;i<names.length;i++)
            {
                TreeItem newItem = new TreeItem(tiParent, SWT.NONE);
                newItem.setText(Const.NVL(names[i], ""));
            }
            
            // The clusters ...
            tiParent = new TreeItem(tiTree, SWT.NONE); 
            tiParent.setText(STRING_CLUSTERS);
    
            names = rep.getClusterNames();          
            for (int i=0;i<names.length;i++)
            {
                TreeItem newItem = new TreeItem(tiParent, SWT.NONE);
                newItem.setText(Const.NVL(names[i], ""));
            }
    
			// The transformations...				
			if (userinfo.useTransformations())
			{
				TreeItem tiCat = new TreeItem(tiTree, SWT.NONE); 
				tiCat.setText(STRING_TRANSFORMATIONS);
				
				TreeItem newCat = new TreeItem(tiCat, SWT.NONE);
                
				rep.getDirectoryTree().getTreeWithNames(newCat, rep, dircolor, sortColumn, ascending);
			}
			
			// The Jobs...				
			if (userinfo.useJobs())
			{
				TreeItem tiJob = new TreeItem(tiTree, SWT.NONE); 
				tiJob.setText(STRING_JOBS);
	
				TreeItem newJob = new TreeItem(tiJob, SWT.NONE);
				rep.getDirectoryTree().getTreeWithNames(newJob, rep, dircolor, sortColumn, ascending);
			}
	
			//
			// Add the users or only yourself
			//
			TreeItem tiUser = new TreeItem(tiTree, SWT.NONE);
			tiUser.setText(STRING_USERS);
			
			String users[] = rep.getUserLogins();
			for (int i=0;i<users.length;i++)
			{
				if (userinfo.isAdministrator() || userinfo.getLogin().equalsIgnoreCase(users[i]))
				{
					if ( users[i] != null )
					{
						// If users[i] is null TreeWidget will throw exceptions.
						// The solution is to verify on saving a user.
					    TreeItem newUser = new TreeItem(tiUser, SWT.NONE);
					    newUser.setText(users[i]);
					}
				}
			}
	
			//
			// Add the profiles if you're admin...
			//
			if (userinfo.isAdministrator())
			{
				TreeItem tiProf = new TreeItem(tiTree, SWT.NONE);
				tiProf.setText(STRING_PROFILES);
				
				String prof[] = rep.getProfiles();
				for (int i=0;i<prof.length;i++)
				{
					TreeItem newProf = new TreeItem(tiProf, SWT.NONE);
					newProf.setText(prof[i]);
				}
			}
            
            // Set the expanded flags based on the TreeMemory
			TreeMemory.setExpandedFromMemory(wTree, STRING_REPOSITORY_EXPLORER_TREE_NAME);
            
            // Always expand the top level entry...
            tiTree.setExpanded(true);
		}
		catch(KettleException dbe)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.PopupMenu.Dialog.ErrorRefreshingTree.Title"), Messages.getString("RepositoryExplorerDialog.PopupMenu.Dialog.ErrorRefreshingTree.Message"), dbe); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public void openTransformation(String name, RepositoryDirectory repdir)
	{
		objectName =  name;
		objectDir  =  repdir;
		objectType =  STRING_TRANSFORMATIONS;
		handleOK();
	}
	
	public boolean delSelectedTransformations()
	{
		boolean retval=false;
		TreeItem tiSel[] = wTree.getSelection();
		boolean done  = false;
		boolean error = false;

		MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
		mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Delete.Confirm.Message1")+(tiSel.length>1?Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Delete.Confirm.Message2")+tiSel.length+Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Delete.Confirm.Message3"):Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Delete.Confirm.Message4"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Delete.Confirm.Title")); //$NON-NLS-1$
		int answer = mb.open();
		
		if (answer!=SWT.YES)
		{
			return false;
		}
		
		for (int i=0;i<tiSel.length;i++)
		{
			TreeItem ti = tiSel[i];
			String name = ti.getText();
			done = false;
			
			RepositoryDirectory repdir = getDirectory(ti);
			
			try
			{
				long id = rep.getTransformationID(ti.getText(), repdir.getID());
		
				// System.out.println("Deleting transformation ["+name+"] with ID = "+id);
		
				if (id>0)
				{
					// System.out.println("OK, Deleting transformation ["+name+"] with ID = "+id);
					
					try
					{
						rep.delAllFromTrans(id);
						done=true;
					}
					catch(KettleDatabaseException dbe)
					{
						new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Delete.ErrorRemoving.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Delete.ErrorRemoving.Message")+name+"]", dbe); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						error=true;
					}
				}
				else
				{
					mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Delete.ErrorFinding.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Delete.ErrorFinding.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Delete.ErrorFinding.Title")); //$NON-NLS-1$
					mb.open();
					error=true;
				}
		
				if (!error)
				{
					retval=true;
				}
				if (done) ti.dispose();
			}
			catch(KettleDatabaseException dbe)
			{
				new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Delete.ErrorDeleting.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Delete.ErrorDeleting.Message"), dbe); //$NON-NLS-1$ //$NON-NLS-2$
				error=true;
			}
		}
		
		return retval;
	}

	public boolean renameTransformation(String name, RepositoryDirectory repdir)
	{
		boolean retval=false;
		final TreeItem ti = wTree.getSelection()[0];
		
		if (ti.getItemCount()==0)
		{
			final String fname = name;
			final RepositoryDirectory frepdir = repdir;
			
			TreeEditor editor = new TreeEditor(wTree);
			editor.setItem(ti);
			final Text text = new Text(wTree, SWT.NONE);
			props.setLook(text);
			text.setText(name);
			text.addFocusListener(new FocusAdapter() 
				{
					public void focusLost(FocusEvent arg0) 
					{
                        // Focus is lost: don't change anything UNLESS you hit enter!
                        text.dispose();
					}
				}
			);
			text.addKeyListener(new KeyAdapter() 
				{
					public void keyPressed(KeyEvent e) 
					{
						// ESC --> Don't change tree item...
						if (e.keyCode   == SWT.ESC)   
						{ 
							text.dispose(); 
						};
						// ENTER --> Save changes...
						if (e.character == SWT.CR )
						{
							String newname = text.getText();
							if (renameTransformation(fname, newname, frepdir)) 
							{
								ti.setText(newname);
							}
							text.dispose();
						}
					}
				}
			);

			editor.horizontalAlignment = SWT.LEFT;
			editor.grabHorizontal = true;
			editor.grabVertical   = true;
			editor.minimumWidth   =   50;

			text.selectAll();
			text.setFocus();
			
			editor.layout();
			editor.setEditor(text);
		}
		return retval;
	}
	
	public boolean renameTransformation(String name, String newname, RepositoryDirectory repdir)
	{
		boolean retval=false;
		
		try
		{
            if (Const.isEmpty(newname))
            {
                throw new KettleException(Messages.getString("RepositoryExplorerDialog.Exception.NameCanNotBeEmpty"));
            }
			if (!name.equals(newname))
			{
				long id = rep.getTransformationID(name, repdir.getID());
				if (id>0)
				{
					// System.out.println("Renaming transformation ["+name+"] with ID = "+id);
					rep.renameTransformation(id, newname);
					retval=true;
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Rename.ErrorFinding.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Rename.ErrorFinding.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Rename.ErrorFinding.Title")); //$NON-NLS-1$
				mb.open();
			}
		}
		catch(KettleException dbe)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Rename.ErrorRenaming.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Rename.ErrorRenaming.Message")+name+"]!", dbe); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		return retval;
	}
	
	public boolean moveTransformation(String xml, RepositoryDirectory repdir)
	{
        debug = "Move transformation"; //$NON-NLS-1$
        
		boolean retval=false;
		
		try
		{
            debug = "parse xml"; //$NON-NLS-1$
			Document doc = XMLHandler.loadXMLString(xml);
			
			String dirname   = XMLHandler.getTagValue(doc, "dragdrop", "directory"); //$NON-NLS-1$ //$NON-NLS-2$
			String transname = XMLHandler.getTagValue(doc, "dragdrop", "transformation"); //$NON-NLS-1$ //$NON-NLS-2$
            
            if (dirname!=null && transname!=null)
            {
                debug = "dirname="+dirname+", transname="+transname; //$NON-NLS-1$ //$NON-NLS-2$
    
    			// OK, find this transformation...
    			RepositoryDirectory fromdir = rep.getDirectoryTree().findDirectory(dirname);
    			if (fromdir!=null)
    			{
                    debug = "fromdir found: move transformation!"; //$NON-NLS-1$
    				rep.moveTransformation(transname, fromdir.getID(), repdir.getID());
    				retval=true;
    			}
    			else
    			{
    				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
    				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Move.ErrorMoving.Message")+dirname+"]"+Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
    				mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Move.ErrorMoving.Title")); //$NON-NLS-1$
    				mb.open();
    			}
            }
		}
		catch(Exception dbe)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Move.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Move.UnexpectedError.Message"), dbe); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return retval;
	}
	
	public boolean moveJob(String xml, RepositoryDirectory repdir)
	{
        debug = "Move Job"; //$NON-NLS-1$
        
		boolean retval=false;
		
		try
		{
            debug = "parse xml"; //$NON-NLS-1$
			Document doc = XMLHandler.loadXMLString(xml);
			
			String dirname = XMLHandler.getTagValue(doc, "dragdrop", "directory"); //$NON-NLS-1$ //$NON-NLS-2$
			String jobname = XMLHandler.getTagValue(doc, "dragdrop", "job"); //$NON-NLS-1$ //$NON-NLS-2$
            
            if (dirname!=null && jobname!=null)
            {
                debug = "dirname="+dirname+", jobname="+jobname; //$NON-NLS-1$ //$NON-NLS-2$
    
    			// OK, find this transformation...
    			RepositoryDirectory fromdir = rep.getDirectoryTree().findDirectory(dirname);
    			if (fromdir!=null)
    			{
                    debug = "fromdir found: move job!"; //$NON-NLS-1$
    				rep.moveJob(jobname, fromdir.getID(), repdir.getID());
    				retval=true;
    			}
    			else
    			{
    				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
    				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Job.Move.ErrorMoving.Message")+dirname+"]"+Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
    				mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Job.Move.ErrorMoving.Title")); //$NON-NLS-1$
    				mb.open();
    			}
            }
		}
		catch(Exception dbe)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Job.Move.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Trans.Move.UnexpectedError.Message"), dbe); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return retval;
	}


	public boolean renameJob(String name, RepositoryDirectory repdir)
	{
		boolean retval=false;
		
        final TreeItem ti = wTree.getSelection()[0];
        
		if (ti.getItemCount()==0)
		{
			final String fname = name;
			final RepositoryDirectory frepdir = repdir;
			
			TreeEditor editor = new TreeEditor(wTree);
			editor.setItem(ti);
			final Text text = new Text(wTree, SWT.NONE);
			props.setLook(text);
			text.setText(name);
			text.addFocusListener(new FocusAdapter() 
				{
					public void focusLost(FocusEvent arg0) 
					{
                        // Focus is lost: don't change anything UNLESS you hit enter!
                        text.dispose();
					}
				}
			);
			text.addKeyListener(new KeyAdapter() 
				{
					public void keyPressed(KeyEvent e) 
					{
						// ESC --> Don't change tree item...
						if (e.keyCode   == SWT.ESC)   
						{ 
							text.dispose(); 
						};
						// ENTER --> Save changes...
						if (e.character == SWT.CR )
						{
							String newname = text.getText();
							if (renameJob(fname, newname, frepdir)) 
							{
								ti.setText(newname);
							}
							text.dispose();
						}
					}
				}
			);

			editor.horizontalAlignment = SWT.LEFT;
			editor.grabHorizontal = true;
			editor.grabVertical   = true;
			editor.minimumWidth   =   50;

			text.selectAll();
			text.setFocus();
			
			editor.layout();
			editor.setEditor(text);
		}
		return retval;
	}
	
	public boolean renameJob(String name, String newname, RepositoryDirectory repdir)
	{
		boolean retval=false;
		
		try
		{
            if (Const.isEmpty(newname))
            {
                throw new KettleException(Messages.getString("RepositoryExplorerDialog.Exception.NameCanNotBeEmpty"));
            }
			if (!name.equals(newname))
			{
				long id = rep.getJobID(name, repdir.getID());
				if (id>0)
				{
					System.out.println("Renaming job ["+name+"] with ID = "+id);
					rep.renameJob(id, newname);
					retval=true;
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Job.Move.ErrorFinding.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Dialog.Job.Move.ErrorFinding.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Job.Move.ErrorFinding.Title")); //$NON-NLS-1$
				mb.open();
			}
		}
		catch(KettleException dbe)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Job.Move.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Job.Move.UnexpectedError.Message")+name+"]", dbe); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		return retval;
	}
	
	public boolean delJob(String name, RepositoryDirectory repdir)
	{
		boolean error = false;
		
		try
		{
			long id = rep.getJobID(name, repdir.getID());
	
			// System.out.println("Deleting transformation ["+name+"] with ID = "+id);
	
			if (id>0)
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Job.Delete.Confirmation.Message")); //$NON-NLS-1$
				mb.setText("["+name+"]"); //$NON-NLS-1$ //$NON-NLS-2$
				int answer = mb.open();
				
				if (answer==SWT.YES)
				{
					// System.out.println("OK, Deleting transformation ["+name+"] with ID = "+id);
					rep.delAllFromJob(id);
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Job.Delete.ErrorFinding.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Dialog.Job.Delete.ErrorFinding.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Job.Delete.ErrorFinding.Title")); //$NON-NLS-1$
				mb.open();
				error=true;
			}
		}
		catch(KettleDatabaseException dbe)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Job.Delete.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Job.Delete.UnexpectedError.Message")+name+"]", dbe); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			error=true;
		}
			
		return !error;
	}

	public void renameJob(TreeItem treeitem, String jobname, RepositoryDirectory repositorydir)
	{
		final TreeItem ti = treeitem;
		final String name = jobname;
		final RepositoryDirectory repdir = repositorydir;
		
		TreeEditor editor = new TreeEditor(wTree);
		editor.setItem(ti);
		final Text text = new Text(wTree, SWT.NONE);
 		props.setLook(text);
		text.setText(name);
		text.addFocusListener(new FocusAdapter() 
			{
				public void focusLost(FocusEvent arg0) 
				{
                    // Focus is lost: don't change anything UNLESS you hit enter!
                    text.dispose();
				}
			}
		);
		text.addKeyListener(new KeyAdapter() 
			{
				public void keyPressed(KeyEvent e) 
				{
					// ESC --> Don't change tree item...
					if (e.keyCode   == SWT.ESC)   
					{ 
						text.dispose(); 
					};
					// ENTER --> Save changes...
					if (e.character == SWT.CR )
					{
						String newname = text.getText();
						if (renameJob(name, repdir, newname)) 
						{
							ti.setText(newname);
						}
						text.dispose();
					}
				}
			}
		);

		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.grabVertical   = true;
		editor.minimumWidth   =   50;

		text.selectAll();
		text.setFocus();
		
		editor.layout();
		editor.setEditor(text);
	}
	
	public boolean renameJob(String name, RepositoryDirectory repdir, String newname)
	{
		boolean retval=false;
		
		try
		{
            if (Const.isEmpty(newname))
            {
                throw new KettleException(Messages.getString("RepositoryExplorerDialog.Exception.NameCanNotBeEmpty"));
            }
			if (!name.equals(newname))
			{
				long id = rep.getJobID(name, repdir.getID());
				if (id>0)
				{
					// System.out.println("Renaming transformation ["+name+"] with ID = "+id);
					rep.renameJob(id, newname);
					retval=true;
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Job.Rename.ErrorFinding.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Dialog.Job.Rename.ErrorFinding.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Job.Rename.ErrorFinding.Title")); //$NON-NLS-1$
					mb.open();
				}								
			}
		}
		catch(KettleException dbe)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Job.Rename.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Job.Rename.UnexpectedError.Message")+name+"]", dbe); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		return retval;
	}
    
	public void editUser(String login)
	{
		try
		{
			UserInfo uinfo = new UserInfo(rep, login); // Get UserInfo from repository...
			UserDialog ud = new UserDialog(shell, SWT.NONE, log, props, rep, uinfo);
			UserInfo ui = ud.open();
			if (!userinfo.isReadonly())
			{
				if (ui!=null)
				{
					ui.saveRep(rep);
			 	}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.User.Edit.NotAllowed.Message")); //$NON-NLS-1$
				mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.User.Edit.NotAllowed.Title")); //$NON-NLS-1$
				mb.open();
			}
			if(ui!=null && !login.equalsIgnoreCase(ui.getLogin())) refreshTree();
			
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.User.Edit.UnexpectedError.Message.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.User.Edit.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void newUser()
	{
		UserDialog ud = new UserDialog(shell, SWT.NONE, log, props, rep, new UserInfo());
		UserInfo ui = ud.open();
		if (ui!=null)
		{
/***************************
            Removed by sboden as the user dialog already saves the id on pressing ok.
            Related defect #4228 on javaforge.
				
   	     	// See if this user already exists...
		    long uid = rep.getUserID(ui.getLogin());
			if (uid<=0)
			{
				ui.saveRep(rep);
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.User.New.AlreadyExists.Message")); //$NON-NLS-1$
				mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.User.New.AlreadyExists.Title")); //$NON-NLS-1$
				mb.open();
			}
****************************/					
			// Refresh tree...
			refreshTree();
		}
	}

	public void delUser(String login)
	{
		try
		{
			long isUser = rep.getUserID(login);
			if (isUser>0)
			{
				rep.delUser(isUser);
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.User.Delete.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.User.Delete.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}

		refreshTree();
	}

	public boolean renameUser()
	{
		boolean retval=false;
		final TreeItem ti = wTree.getSelection()[0];
		
		if (ti.getItemCount()==0)
		{
			final String name = ti.getText();
			TreeEditor editor = new TreeEditor(wTree);
			editor.setItem(ti);
			final Text text = new Text(wTree, SWT.NONE);
			props.setLook(text);
			text.setText(name);
			text.addFocusListener(new FocusAdapter() 
				{
					public void focusLost(FocusEvent arg0) 
					{
                        // Focus is lost: don't change anything UNLESS you hit enter!
                        text.dispose();
					}
				}
			);
			text.addKeyListener(new KeyAdapter() 
				{
					public void keyPressed(KeyEvent e) 
					{
						// ESC --> Don't change tree item...
						if (e.keyCode   == SWT.ESC)   
						{ 
							text.dispose(); 
						};
						// ENTER --> Save changes...
						if (e.character == SWT.CR )
						{
							String newname = text.getText();
							if (renameUser(name, newname)) 
							{
								ti.setText(newname);
							}
							text.dispose();
						}
					}
				}
			);

			editor.horizontalAlignment = SWT.LEFT;
			editor.grabHorizontal = true;
			editor.grabVertical   = true;
			editor.minimumWidth   =   50;

			text.selectAll();
			text.setFocus();
			
			editor.layout();
			editor.setEditor(text);
		}
		return retval;
	}
	
	
	public boolean renameUser(String name, String newname)
	{
		boolean retval=false;
		
		try
		{
            if (Const.isEmpty(newname))
            {
                throw new KettleException(Messages.getString("RepositoryExplorerDialog.Exception.NameCanNotBeEmpty"));
            }
			if (!name.equals(newname))
			{
				long id = rep.getUserID(name);
				if (id>0)
				{
					// System.out.println("Renaming user ["+name+"] with ID = "+id);
					rep.renameUser(id, newname);
					retval=true;
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.User.Rename.ErrorFinding.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Dialog.User.Rename.ErrorFinding.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.User.Rename.ErrorFinding.Title")); //$NON-NLS-1$
					mb.open();
				}
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.User.Rename.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.User.Rename.UnexpectedError.Message")+name+"]", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		return retval;
	}
	
	public boolean renameProfile()
	{
		boolean retval=false;
		final TreeItem ti = wTree.getSelection()[0];
		
		if (ti.getItemCount()==0)
		{
			final String name = ti.getText();
			TreeEditor editor = new TreeEditor(wTree);
			editor.setItem(ti);
			final Text text = new Text(wTree, SWT.NONE);
			props.setLook(text);
			text.setText(name);
			text.addFocusListener(new FocusAdapter() 
				{
					public void focusLost(FocusEvent arg0) 
					{
                        // Focus is lost: don't change anything UNLESS you hit enter!
                        text.dispose();
					}
				}
			);
			text.addKeyListener(new KeyAdapter() 
				{
					public void keyPressed(KeyEvent e) 
					{
						// ESC --> Don't change tree item...
						if (e.keyCode   == SWT.ESC)   
						{ 
							text.dispose(); 
						};
						// ENTER --> Save changes...
						if (e.character == SWT.CR )
						{
							String newname = text.getText();
							if (renameProfile(name, newname)) 
							{
								ti.setText(newname);
							}
							text.dispose();
						}
					}
				}
			);

			editor.horizontalAlignment = SWT.LEFT;
			editor.grabHorizontal = true;
			editor.grabVertical   = true;
			editor.minimumWidth   =   50;

			text.selectAll();
			text.setFocus();
			
			editor.layout();
			editor.setEditor(text);
		}
		return retval;
	}
	
	

	
	public boolean renameProfile(String name, String newname)
	{
		boolean retval=false;
		
		try
		{
            if (Const.isEmpty(newname))
            {
                throw new KettleException(Messages.getString("RepositoryExplorerDialog.Exception.NameCanNotBeEmpty"));
            }
			if (!name.equals(newname))
			{
				long id = rep.getProfileID(name);
				if (id>0)
				{
					rep.renameProfile(id, newname);
					retval=true;
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Profile.Rename.ErrorFinding.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Dialog.Profile.Rename.ErrorFinding.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Profile.Rename.ErrorFinding.Title")); //$NON-NLS-1$
					mb.open();
				}
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Profile.Rename.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Profile.Rename.UnexpectedError.Message")+name+"]", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		return retval;
	}


	public void editDatabase(String databasename)
	{
		try
		{
			long idDatabase = rep.getDatabaseID(databasename);
			DatabaseMeta dbinfo = new DatabaseMeta(rep, idDatabase);

			DatabaseDialog dd = new DatabaseDialog(shell, SWT.NONE, log, dbinfo, props);
			String name = dd.open();
			if (name!=null)
			{
				if (!userinfo.isReadonly())
				{
                    rep.lockRepository();
                    rep.insertLogEntry("Updating database connection '"+dbinfo.getName()+"'");
                    dbinfo.saveRep(rep);
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Connection.Edit.NotAllowed.Message")); //$NON-NLS-1$
					mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Connection.Edit.NotAllowed.Title")); //$NON-NLS-1$
					mb.open();
				}
				if(!databasename.equalsIgnoreCase(name)) refreshTree();
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Connection.Edit.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Connection.Edit.UnexpectedError.Message")+databasename+"]", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
        finally
        {
            try
            {
                rep.unlockRepository();
            }
            catch(KettleDatabaseException e)
            {
                new ErrorDialog(shell, "Error", "Unexpected error unlocking the repository database", e);
            }
        }
	}

	public void newDatabase()
	{
		try
		{
			DatabaseMeta dbinfo = new DatabaseMeta();
			DatabaseDialog dd = new DatabaseDialog(shell, SWT.NONE, log, dbinfo, props);
			String name = dd.open();
			if (name!=null)
			{
				// See if this user already exists...
				long idDatabase = rep.getDatabaseID(name);
				if (idDatabase<=0)
				{
                    rep.lockRepository();
                    rep.insertLogEntry("Creating new database '"+dbinfo.getName()+"'");
					dbinfo.saveRep(rep);
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Connection.Create.AlreadyExists.Message")); //$NON-NLS-1$
					mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Connection.Create.AlreadyExists.Title")); //$NON-NLS-1$
					mb.open();
				}
					// Refresh tree...
				refreshTree();
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Connection.Create.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Connection.Create.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
        finally
        {
            try
            {
                rep.unlockRepository();
            }
            catch(KettleDatabaseException e)
            {
                new ErrorDialog(shell, "Error", "Unexpected error unlocking the repository database", e);
            }
        }
	}

	public void delDatabase(String databasename)
	{
		try
		{
			long idDatabase = rep.getDatabaseID(databasename);
			if (idDatabase>0)
			{
				rep.delDatabase(idDatabase);
			}
	
			refreshTree();
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Connection.Delete.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Connection.Delete.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public boolean renameDatabase()
	{
		boolean retval=false;
		final TreeItem ti = wTree.getSelection()[0];
		
		if (ti.getItemCount()==0)
		{
			final String name = ti.getText();
			TreeEditor editor = new TreeEditor(wTree);
			editor.setItem(ti);
			final Text text = new Text(wTree, SWT.NONE);
			props.setLook(text);
			text.setText(name);
			text.addFocusListener(new FocusAdapter() 
				{
					public void focusLost(FocusEvent arg0) 
					{
						// Focus is lost: don't change anything UNLESS you hit enter!
                        text.dispose();
					}
				}
			);
			text.addKeyListener(new KeyAdapter() 
				{
					public void keyPressed(KeyEvent e) 
					{
						// ESC --> Don't change tree item...
						if (e.keyCode   == SWT.ESC)   
						{ 
							text.dispose(); 
						};
						// ENTER --> Save changes...
						if (e.character == SWT.CR )
						{
                            if (ti.getText().equals(name))  // Only if the name wasn't changed already.
                            {
    							String newname = text.getText();
    							if (renameDatabase(name, newname)) 
    							{
    								ti.setText(newname);
    							}
    							text.dispose();
                            }
						}
					}
				}
			);

			editor.horizontalAlignment = SWT.LEFT;
			editor.grabHorizontal = true;
			editor.grabVertical   = true;
			editor.minimumWidth   =   50;

			text.selectAll();
			text.setFocus();
			
			editor.layout();
			editor.setEditor(text);
		}
		return retval;
	}
	
	public boolean renameDatabase(String name, String newname)
	{
		boolean retval=false;
		
		try
		{
            if (Const.isEmpty(newname))
            {
                throw new KettleException(Messages.getString("RepositoryExplorerDialog.Exception.NameCanNotBeEmpty"));
            }
			if (!name.equals(newname))
			{
				long id = rep.getDatabaseID(name);
				if (id>0)
				{
					// System.out.println("Renaming transformation ["+name+"] with ID = "+id);
					rep.renameDatabase(id, newname);
					retval=true;
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Connection.Rename.ErrorFinding.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Dialog.Connection.Rename.ErrorFinding.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Connection.Rename.ErrorFinding.Title")); //$NON-NLS-1$
					mb.open();
				}
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Connection.Rename.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Connection.Rename.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return retval;
	}

	public void renameDirectory(TreeItem treeitem, RepositoryDirectory rd)
	{
		final TreeItem ti = treeitem;
		final RepositoryDirectory repdir = rd;
		
		final String name = ti.getText();
		TreeEditor editor = new TreeEditor(wTree);
		editor.setItem(ti);
		final Text text = new Text(wTree, SWT.NONE);
 		props.setLook(text);
		text.setText(name);
		text.addFocusListener(new FocusAdapter() 
			{
				public void focusLost(FocusEvent arg0) 
				{
					// Focus is lost: don't change anything UNLESS you hit enter!
					text.dispose();
				}
			}
		);
		text.addKeyListener(new KeyAdapter() 
			{
				public void keyPressed(KeyEvent e) 
				{
					// ESC --> Don't change tree item...
					if (e.keyCode   == SWT.ESC)   
					{ 
						text.dispose(); 
					};
					// ENTER --> Save changes...
					if (e.character == SWT.CR )
					{
						String newname = text.getText();
						if (renameDirectory(repdir, name, newname)) 
						{
							ti.setText(newname);
						}
						text.dispose();
					}
				}
			}
		);

		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.grabVertical   = true;
		editor.minimumWidth   =   50;

		text.selectAll();
		text.setFocus();
		
		editor.layout();
		editor.setEditor(text);
	}
	
	
	public boolean renameDirectory(RepositoryDirectory repdir, String name, String newname)
	{
		boolean retval=false;

        try
        {
            if (Const.isEmpty(newname))
            {
                throw new KettleException(Messages.getString("RepositoryExplorerDialog.Exception.NameCanNotBeEmpty"));
            }
    		if (!name.equals(newname))
    		{
    			repdir.setDirectoryName(newname);
    			if (!repdir.renameInRep(rep))
    			{
    				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
    				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Directory.Rename.UnexpectedError.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Dialog.Directory.Rename.UnexpectedError.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    				mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Directory.Rename.UnexpectedError.Title")); //$NON-NLS-1$
    				mb.open();
    			}
    			else
    			{
    				retval=true;
    			}
    		}
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Directory.Rename.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Directory.Rename.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
		return retval;
	}

	
	// Open or edit the first entry in the list...
	public void doubleClick()
	{
		TreeItem tis[] = wTree.getSelection();
		if (tis.length>0)
		{
			TreeItem ti = tis[0];
			TreeItem parent = ti.getParentItem();
			
			int level = Const.getTreeLevel(ti);
			
			if (level==0)
			{
				Const.flipExpanded(ti);
			}
			else
			if (level==1 && ti.getText().equalsIgnoreCase(STRING_USERS))
			{
				if (!userinfo.isReadonly()) newUser();
			}
			else
			if (level==1 && ti.getText().equalsIgnoreCase(STRING_PROFILES))
			{
				if (!userinfo.isReadonly()) newProfile();
			}
			else
			if (level==1 && ti.getText().equalsIgnoreCase(STRING_DATABASES))
			{
				if (!userinfo.isReadonly()) newDatabase();
			}
			else
			if (level==1 && parent.getText().equalsIgnoreCase(STRING_USERS))
			{
				editUser(ti.getText());
			}
			else
			if (level==1 && parent.getText().equalsIgnoreCase(STRING_PROFILES))
			{
				editProfile(ti.getText());
			}
			else
			if (level==2 && parent.getText().equalsIgnoreCase(STRING_DATABASES))
			{
				editDatabase(ti.getText());
			}
		}
		
	}

	public void editProfile(String profilename)
	{
		try
		{
			long idProfile = rep.getProfileID(profilename);
			ProfileMeta profinfo = new ProfileMeta(rep, idProfile);
			
			// System.out.println("editProfile, nrPermissions = "+profinfo.nrPermissions());
	
			ProfileDialog pd = new ProfileDialog(shell, SWT.NONE, log, profinfo, props);
			String name = pd.open();
			if (name!=null)
			{
				profinfo.saveRep(rep);
			}
				
			if(!profilename.equalsIgnoreCase(name)) refreshTree();
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Profile.Edit.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Profile.Edit.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void newProfile()
	{
		try
		{
			ProfileMeta profinfo = new ProfileMeta();
			ProfileDialog pd = new ProfileDialog(shell, SWT.NONE, log, profinfo, props);
			String name = pd.open();
			if (name!=null)
			{
				// See if this user already exists...
				long idProfile = rep.getProfileID(name);
				if (idProfile<=0)
				{
					profinfo.saveRep(rep);
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Profile.Create.AlreadyExists.Message")); //$NON-NLS-1$
					mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Profile.Create.AlreadyExists.Title")); //$NON-NLS-1$
					mb.open();
				}
					
				// Refresh tree...
				refreshTree();
			} 
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Profile.Create.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Profile.Create.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}

	public void delProfile(String profilename)
	{
		try
		{
			long idProfile = rep.getProfileID(profilename);
			if (idProfile>0)
			{
				rep.delProfile(idProfile);
			}
	
			refreshTree();
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.Profile.Delete.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Profile.Delete.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public void createDirectory(TreeItem ti, RepositoryDirectory repdir)
	{
		EnterStringDialog esd = new EnterStringDialog(shell, props, Messages.getString("RepositoryExplorerDialog.Dialog.Directory.Create.AskName.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.Directory.Create.AskName.Default"), Messages.getString("RepositoryExplorerDialog.Dialog.Directory.Create.AskName.Message")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String newdir = esd.open();
		if (newdir!=null)
		{
			RepositoryDirectory rd = new RepositoryDirectory(repdir, newdir);
			String path[] = rd.getPathArray();
			
			RepositoryDirectory exists = rep.getDirectoryTree().findDirectory( path );
			if (exists==null)
			{
				if (rd.addToRep(rep))
				{
					refreshTree();
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Directory.Create.UnexpectedError.Message1")+newdir+Messages.getString("RepositoryExplorerDialog.Dialog.Directory.Create.UnexpectedError.Message2")+repdir.getPath()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Directory.Create.UnexpectedError.Title")); //$NON-NLS-1$
					mb.open();
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.Directory.Create.AlreadyExists.Message1")+newdir+Messages.getString("RepositoryExplorerDialog.Dialog.Directory.Create.AlreadyExists.Message2")+repdir.getPath()+Messages.getString("RepositoryExplorerDialog.Dialog.Directory.Create.AlreadyExists.Message3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				mb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.Directory.Create.AlreadyExists.Title")); //$NON-NLS-1$
				mb.open();
			}
		}
	}
	
	public void delDirectory(TreeItem ti, RepositoryDirectory repdir)
	{
		try
		{
			repdir.delFromRep(rep);
			refreshTree();
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell,
					Messages.getString("RepositoryExplorerDialog.Dialog.Directory.Delete.ErrorRemoving.Title"), //$NON-NLS-1$
					Messages.getString("RepositoryExplorerDialog.Dialog.Directory.Delete.ErrorRemoving.Message1"), //$NON-NLS-1$
					e
			); 
		}
	}
	
	public void exportTransformations()
	{
		try
		{
			DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
			if (dialog.open()!=null)
			{
				String directory = dialog.getFilterPath();
				
				long dirids[] = rep.getDirectoryTree().getDirectoryIDs();
				for (int d=0;d<dirids.length;d++)
				{
					RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(dirids[d]);				
					String trans[] = rep.getTransformationNames(dirids[d]);
				
					for (int i=0;i<trans.length;i++)
					{
						TransMeta ti = new TransMeta(rep, trans[i], repdir);
						System.out.println("Loading/Exporting transformation ["+trans[i]+"] in directory ["+repdir.getPath()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

						String xml = XMLHandler.getXMLHeader() + ti.getXML();
							
						// See if the directory exists...
						File dir = new File(directory+repdir.getPath());
						if (!dir.exists()) 
						{
							dir.mkdir();
							System.out.println("Created directory ["+dir.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
						}
						
						String filename = directory+repdir.getPath()+Const.FILE_SEPARATOR+trans[i]+".ktr"; //$NON-NLS-1$
						File f = new File(filename);
						try
						{
							FileOutputStream fos = new FileOutputStream(f);
							fos.write(xml.getBytes(Const.XML_ENCODING));
							fos.close();
						}
						catch(IOException e)
						{
							System.out.println("Couldn't create file ["+filename+"]"); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.ExportTrans.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.ExportTrans.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}

	public void exportJobs()
	{
		try
		{
			DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
			if (dialog.open()!=null)
			{
				String directory = dialog.getFilterPath();
	
				long dirids[] = rep.getDirectoryTree().getDirectoryIDs();
				for (int d=0;d<dirids.length;d++)
				{
					RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(dirids[d]);				
					String jobs[] = rep.getJobNames(dirids[d]);
				
					for (int i=0;i<jobs.length;i++)
					{
						JobMeta ji = new JobMeta(log, rep, jobs[i], repdir);
						System.out.println("Loading/Exporting job ["+jobs[i]+"] in directory ["+repdir.getPath()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

						String xml = XMLHandler.getXMLHeader() + ji.getXML();
						
						// See if the directory exists...
						File dir = new File(directory+repdir.getPath());
						if (!dir.exists()) 
						{
							dir.mkdir();
							System.out.println("Created directory ["+dir.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
						}
						
						String filename = directory+repdir.getPath()+Const.FILE_SEPARATOR+jobs[i]+".kjb"; //$NON-NLS-1$
						File f = new File(filename);
						try
						{
							FileOutputStream fos = new FileOutputStream(f);
							fos.write(xml.getBytes(Const.XML_ENCODING));
							fos.close();
						}
						catch(IOException e)
						{
							System.out.println("Couldn't create file ["+filename+"]"); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Dialog.ExportJobs.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Dialog.ExportJobs.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public void exportAll()
	{
		FileDialog dialog = new FileDialog(shell, SWT.SAVE | SWT.SINGLE);
		if (dialog.open()!=null)
		{
			String filename = dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName();
			System.out.println("Export objects to file ["+filename+"]"); //$NON-NLS-1$ //$NON-NLS-2$
			
			RepositoryExportProgressDialog repd = new RepositoryExportProgressDialog(shell, rep, filename);
			repd.open();
		}
	}

	public void importAll()
	{
		FileDialog dialog = new FileDialog(shell, SWT.OPEN | SWT.SINGLE);
		if (dialog.open()!=null)
		{
			final String filename = dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName();
			System.out.println("Import objects from XML file ["+filename+"]"); //$NON-NLS-1$ //$NON-NLS-2$
			
			SelectDirectoryDialog sdd = new SelectDirectoryDialog(shell, SWT.NONE, rep);
			RepositoryDirectory baseDirectory = sdd.open();
			if (baseDirectory!=null)
			{
				RepositoryImportProgressDialog ripd = new RepositoryImportProgressDialog(shell, SWT.NONE, rep, filename, baseDirectory);
				ripd.open();
				
				refreshTree();
			}
		}
	}

	public String getObjectType()
	{
		return objectType;
	}
	
	public RepositoryDirectory getObjectDirectory()
	{
		return objectDir;
	}
	
	private int getItemCategory(TreeItem ti)
	{
		int cat = ITEM_CATEGORY_NONE;
		
		int level = Const.getTreeLevel(ti);
		String path[] = Const.getTreeStrings(ti);
		
		String item = ""; //$NON-NLS-1$
		String parent = ""; //$NON-NLS-1$
		
		if (ti!=null) 
		{
			item = ti.getText();
			if (ti.getParentItem()!=null)
			{
				parent = ti.getParentItem().getText();
			}
		}
		
		
		// Level 1:
		if (level==0)
		{
			cat = ITEM_CATEGORY_ROOT;
		}
		else
		if (level==1)
		{
			     if (item.equals(STRING_USERS))           cat = ITEM_CATEGORY_USERS_ROOT;
			else if (item.equals(STRING_PROFILES))        cat = ITEM_CATEGORY_PROFILES_ROOT;
            else if (item.equals(STRING_DATABASES))       cat = ITEM_CATEGORY_DATABASES_ROOT;
            else if (item.equals(STRING_PARTITIONS))      cat = ITEM_CATEGORY_PARTITIONS_ROOT;
            else if (item.equals(STRING_SLAVES))          cat = ITEM_CATEGORY_SLAVES_ROOT;
            else if (item.equals(STRING_CLUSTERS))        cat = ITEM_CATEGORY_CLUSTERS_ROOT;
            else if (item.equals(STRING_TRANSFORMATIONS)) cat = ITEM_CATEGORY_TRANSFORMATIONS_ROOT;
            else if (item.equals(STRING_JOBS))            cat = ITEM_CATEGORY_JOBS_ROOT;
            else if (item.equals(STRING_SCHEMAS))         cat = ITEM_CATEGORY_SCHEMAS_ROOT;
		}
		else
		if (level==2)
		{
			if (parent.equals(STRING_USERS)) cat = ITEM_CATEGORY_USER;
			else if (parent.equals(STRING_PROFILES)) cat = ITEM_CATEGORY_PROFILE;
            else if (parent.equals(STRING_DATABASES)) cat = ITEM_CATEGORY_DATABASE;
            else if (parent.equals(STRING_PARTITIONS)) cat = ITEM_CATEGORY_PARTITION;
            else if (parent.equals(STRING_SLAVES)) cat = ITEM_CATEGORY_SLAVE;
            else if (parent.equals(STRING_CLUSTERS)) cat = ITEM_CATEGORY_CLUSTER;
            if (path[1].equals(STRING_TRANSFORMATIONS))
            {
                if (ti.getForeground().equals(dircolor)) 
                     cat = ITEM_CATEGORY_TRANSFORMATION_DIRECTORY;
                else cat = ITEM_CATEGORY_TRANSFORMATION;
            }
            else
            if (path[1].equals(STRING_JOBS))
            {
                if (ti.getForeground().equals(dircolor)) 
                     cat = ITEM_CATEGORY_JOB_DIRECTORY;
                else cat = ITEM_CATEGORY_JOB;
            }
            else
            if (path[1].equals(STRING_SCHEMAS))
            {
                if (ti.getForeground().equals(dircolor)) 
                     cat = ITEM_CATEGORY_SCHEMA_DIRECTORY;
                else cat = ITEM_CATEGORY_SCHEMA;
            }
		}
		
		return cat;
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}

}
