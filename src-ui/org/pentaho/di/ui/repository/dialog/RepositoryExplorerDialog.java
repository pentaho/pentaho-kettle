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

 

package org.pentaho.di.ui.repository.dialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.swt.widgets.ToolBar;
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
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.ProfileMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryUtil;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.cluster.dialog.ClusterSchemaDialog;
import org.pentaho.di.ui.cluster.dialog.SlaveServerDialog;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.dialog.EnterStringDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.DoubleClickInterface;
import org.pentaho.di.ui.core.widget.TreeItemAccelerator;
import org.pentaho.di.ui.core.widget.TreeMemory;
import org.pentaho.di.ui.partition.dialog.PartitionSchemaDialog;
import org.pentaho.di.ui.repository.RepositoryDirectoryUI;
import org.pentaho.di.ui.spoon.job.JobGraph;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.w3c.dom.Document;


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
    public interface RepositoryExplorerCallback {
    	/**
    	 * request that specified object be opened in 'Spoon' display
    	 * @param object
    	 * @return boolean indicating if repository explorer dialog should close
    	 */
    	boolean open(RepositoryObjectReference object);
    }
    
    /**
     * capture a reference to an object in the repository
     * @author jgoldman
     *
     */
    public class RepositoryObjectReference {
    	private String              type;   // Type of object
    	private RepositoryDirectory directory;    // The directory to which it belongs.
    	private String              name;   // name of object being referenced	
    	
    	public RepositoryObjectReference(String type, RepositoryDirectory dir, String name) {
    		this.type = type;
    		this.directory = dir;
    		this.name = name;
    	}
    	
		public RepositoryDirectory getDirectory()
		{
			return directory;
		}
		public String getName()
		{
			return name;
		}
		public String getType()
		{
			return type;
		}
    }
    
	private static final String STRING_DATABASES       = Messages.getString("RepositoryExplorerDialog.Tree.String.Connections"); //$NON-NLS-1$
    private static final String STRING_PARTITIONS      = Messages.getString("RepositoryExplorerDialog.Tree.String.Partitions"); //$NON-NLS-1$
    private static final String STRING_SLAVES          = Messages.getString("RepositoryExplorerDialog.Tree.String.Slaves"); //$NON-NLS-1$
    private static final String STRING_CLUSTERS        = Messages.getString("RepositoryExplorerDialog.Tree.String.Clusters"); //$NON-NLS-1$
	public  static final String STRING_TRANSFORMATIONS = Messages.getString("RepositoryExplorerDialog.Tree.String.Transformations"); //$NON-NLS-1$
	public  static final String STRING_JOBS            = Messages.getString("RepositoryExplorerDialog.Tree.String.Jobs"); //$NON-NLS-1$
	private static final String STRING_USERS           = Messages.getString("RepositoryExplorerDialog.Tree.String.Users"); //$NON-NLS-1$
	private static final String STRING_PROFILES        = Messages.getString("RepositoryExplorerDialog.Tree.String.Profiles"); //$NON-NLS-1$
	
	private static final int    ITEM_CATEGORY_NONE                        =  0;
	private static final int    ITEM_CATEGORY_ROOT                        =  1;
	private static final int    ITEM_CATEGORY_DATABASES_ROOT              =  2;
	private static final int    ITEM_CATEGORY_DATABASE                    =  3;
	private static final int    ITEM_CATEGORY_TRANSFORMATIONS_ROOT        =  4;
	private static final int    ITEM_CATEGORY_TRANSFORMATION              =  5;
	private static final int    ITEM_CATEGORY_TRANSFORMATION_DIRECTORY    =  6;
	private static final int    ITEM_CATEGORY_JOBS_ROOT                   =  7;
	private static final int    ITEM_CATEGORY_JOB                         =  8;
	private static final int    ITEM_CATEGORY_JOB_DIRECTORY               =  9;
	private static final int    ITEM_CATEGORY_USERS_ROOT                  = 10;
	private static final int    ITEM_CATEGORY_USER                        = 11;
	private static final int    ITEM_CATEGORY_PROFILES_ROOT               = 12;
	private static final int    ITEM_CATEGORY_PROFILE                     = 13;
    private static final int    ITEM_CATEGORY_PARTITIONS_ROOT             = 14;
    private static final int    ITEM_CATEGORY_PARTITION                   = 15;
    private static final int    ITEM_CATEGORY_SLAVES_ROOT                 = 16;
    private static final int    ITEM_CATEGORY_SLAVE                       = 17;
    private static final int    ITEM_CATEGORY_CLUSTERS_ROOT               = 18;
    private static final int    ITEM_CATEGORY_CLUSTER                     = 19;
    
	private Shell     shell;
	private Tree      wTree;
	private Button    wCommit;
	private Button    wRollback;
	private boolean   changedInDialog;

	private LogWriter log;
	private PropsUI props;
	private Repository     rep;
	private UserInfo       userinfo;
	
	private String debug;
    
    private int sortColumn;
    private boolean ascending;
    private TreeColumn nameColumn;
    private TreeColumn typeColumn;
    private TreeColumn userColumn;
    private TreeColumn changedColumn;
    private TreeColumn descriptionColumn;
    
    private RepositoryExplorerCallback callback;
    
    private RepositoryObjectReference lastOpened;
	private VariableSpace variableSpace;
	
	private ToolItem expandAll, collapseAll;
    
    private FormData     fdexpandAll;
    
	private RepositoryExplorerDialog(Shell par, int style, Repository rep, UserInfo ui, VariableSpace variableSpace)
	{
		super(par, style);
		this.props=PropsUI.getInstance();
		this.log=LogWriter.getInstance();
		this.rep=rep;
		this.userinfo=ui;
		this.variableSpace = variableSpace;

        sortColumn = 0;
        ascending = false;
	}
    
	public RepositoryExplorerDialog(Shell par, int style, Repository rep, UserInfo ui, RepositoryExplorerCallback callback, VariableSpace variableSpace)
	{
		this(par, style, rep, ui, variableSpace);
		this.callback = callback;
	}
	
    private static final String STRING_REPOSITORY_EXPLORER_TREE_NAME = "Repository Exporer Tree Name";

	public RepositoryObjectReference open() 
	{
        debug="opening repository explorer"; //$NON-NLS-1$
        
        try
        {
            debug="open new independent shell"; //$NON-NLS-1$
            Shell parent = getParent();
    		Display display = parent.getDisplay();
    		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
    		props.setLook(shell);
			shell.setImage(GUIResource.getInstance().getImageFolderConnections());
    		shell.setText(Messages.getString("RepositoryExplorerDialog.Title")+rep.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
    		
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
            miFileExport.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { exportAll(null); } });
            
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
            miFileClose.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { commit(); } });
            
            ToolBar treeTb = new ToolBar(shell, SWT.HORIZONTAL | SWT.FLAT);
            expandAll = new ToolItem(treeTb,SWT.PUSH);
            expandAll.setImage(GUIResource.getInstance().getImageExpandAll());
            collapseAll = new ToolItem(treeTb,SWT.PUSH);
            collapseAll.setImage(GUIResource.getInstance().getImageCollapseAll());
    		fdexpandAll=new FormData();
    		fdexpandAll.right = new FormAttachment(100, -20);
    		fdexpandAll.top  = new FormAttachment(0, 0);
    		treeTb.setLayoutData(fdexpandAll);
            
     		// Tree
     		wTree = new Tree(shell, SWT.MULTI | SWT.BORDER /*| (multiple?SWT.CHECK:SWT.NONE)*/);
            wTree.setHeaderVisible(true);
     		props.setLook(wTree);
    
            // Add some columns to it as well...
            nameColumn = new TreeColumn(wTree, SWT.LEFT);
            nameColumn.setText(Messages.getString("RepositoryExplorerDialog.Column.Name")); //$NON-NLS-1$
            nameColumn.setWidth(350);
            nameColumn.setAlignment(10);
            nameColumn.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { setSort(0); } });
            
            // No sorting on the type column just yet.
            typeColumn = new TreeColumn(wTree, SWT.LEFT);
            typeColumn.setText(Messages.getString("RepositoryExplorerDialog.Column.Type")); //$NON-NLS-1$
            typeColumn.setWidth(100);
            typeColumn.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { setSort(1); } });
            
            userColumn = new TreeColumn(wTree, SWT.LEFT);
            userColumn.setText(Messages.getString("RepositoryExplorerDialog.Column.User")); //$NON-NLS-1$
            userColumn.setWidth(100);
            userColumn.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { setSort(2); } });

            changedColumn = new TreeColumn(wTree, SWT.LEFT);
            changedColumn.setText(Messages.getString("RepositoryExplorerDialog.Column.Changed")); //$NON-NLS-1$
            changedColumn.setWidth(120);
            changedColumn.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { setSort(3); } });

            descriptionColumn = new TreeColumn(wTree, SWT.LEFT);
            descriptionColumn.setText(Messages.getString("RepositoryExplorerDialog.Column.Description")); //$NON-NLS-1$
            descriptionColumn.setWidth(120);
            descriptionColumn.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { setSort(4); } });

            // Add a memory to the tree.
            TreeMemory.addTreeListener(wTree,STRING_REPOSITORY_EXPLORER_TREE_NAME);
            
     		// Buttons
    		wCommit = new Button(shell, SWT.PUSH); 
    		wCommit.setText(Messages.getString("RepositoryExplorerDialog.Button.Commit")); //$NON-NLS-1$
    		wRollback = new Button(shell, SWT.PUSH); 
    		wRollback.setText(Messages.getString("RepositoryExplorerDialog.Button.Rollback")); //$NON-NLS-1$
    				
    		FormData fdTree      = new FormData(); 
    		int margin =  10;
    
    		fdTree.left   = new FormAttachment(0, 0); // To the right of the label
    		fdTree.top    = new FormAttachment(0, 0);
    		fdTree.right  = new FormAttachment(100, 0);
    		fdTree.bottom = new FormAttachment(100, -50);
    		wTree.setLayoutData(fdTree);
    
    		BaseStepDialog.positionBottomButtons(shell, new Button[] { wCommit, wRollback }, margin, null );
    
    		// Add listeners
    		wCommit.addListener(SWT.Selection, new Listener ()
    			{
    				public void handleEvent (Event e) 
    				{
    					commit();
    				}
    			}
    		);
    		// Add listeners
    		wRollback.addListener(SWT.Selection, new Listener ()
    			{
    				public void handleEvent (Event e) 
    				{
    					rollback();
    				}
    			}
    		);
    		
    		wTree.addMouseListener(new MouseAdapter()
    			{
    				public void mouseDown(MouseEvent e)
    				{
    					if (e.button == 3) // right click!
    					{
    						setTreeMenu();
    					}
    				}
    				
    				public void mouseDoubleClick(MouseEvent e) {
    					if (e.button == 1) // left double click!
    					{
    						doDoubleClick();
    					}
    				}

    			}
    		);
    		
    		wTree.addKeyListener(new KeyAdapter() 
    			{
    				public void keyPressed(KeyEvent e) 
    				{
    					// F2 --> rename...
    					if (e.keyCode == SWT.F2)    { if (!userinfo.isReadonly()) renameInTree(); }
    					// F5 --> refresh...
    					if (e.keyCode == SWT.F5)    { refreshTree(); }
    				}
    			}
    		);
    		
    		expandAll.addSelectionListener(new SelectionAdapter() {
    		      public void widgetSelected(SelectionEvent event) {
    		    	expandAllItems(wTree.getItems(),true);
    		      }});
    		
    		collapseAll.addSelectionListener(new SelectionAdapter() {
  		      public void widgetSelected(SelectionEvent event) {
  		    	expandAllItems(wTree.getItems(),false);
  		      }});

    		
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
                                        mb.setMessage(Messages.getString("RepositoryExplorerDialog.Trans.Move.UnableToMove.Message")); //$NON-NLS-1$
                                        mb.setText(Messages.getString("RepositoryExplorerDialog.Trans.Move.UnableToMove.Title")); //$NON-NLS-1$
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
                                        mb.setMessage(Messages.getString("RepositoryExplorerDialog.Job.Move.UnableToMove.Message")); //$NON-NLS-1$
                                        mb.setText(Messages.getString("RepositoryExplorerDialog.Job.Move.UnableToMove.Title")); //$NON-NLS-1$
                                        mb.open();
                                    }
        						}
        					}
                            else
                            {
                                MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK );
                                mb.setMessage(Messages.getString("RepositoryExplorerDialog.Trans.Move.SorryOneItemAtATime.Message")); //$NON-NLS-1$
                                mb.setText(Messages.getString("RepositoryExplorerDialog.Trans.Move.SorryOneItemAtATime.Title")); //$NON-NLS-1$
                                mb.open();
                            }
        				}
                    }
                    catch(Throwable e)
                    {
                        new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Drop.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Drop.UnexpectedError.Message1")+debug+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Drop.UnexpectedError.Message2"), e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    }
    			}
    
    			public void dropAccept(DropTargetEvent event) 
    			{
                    debug="drop accept"; //$NON-NLS-1$
    			}
    		});
    
    
    		// Detect X or ALT-F4 or something that kills this window...
    		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { checkRollback(e); } } );
            
            debug="set screen size and position"; //$NON-NLS-1$
    
    		BaseStepDialog.setSize(shell, 400, 480, true);
    
    		setSort(0); // refreshes too.
    
    		shell.open();
    		while (!shell.isDisposed()) 
    		{
    			if (!display.readAndDispatch()) display.sleep();
    		}
        }
        catch(Throwable e)
        {
            new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Main.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Main.UnexpectedError.Message1")+debug+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Main.UnexpectedError.Message2")+Const.CR+Messages.getString("RepositoryExplorerDialog.Main.UnexpectedError.Message3"), e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }
		return lastOpened;

	}
	private void expandAllItems(TreeItem[] treeitems,boolean expand)
	{
	  for (TreeItem item : treeitems) { 
		    item.setExpanded(expand);
		    if(item.getItemCount()>0)
		    	expandAllItems(item.getItems(),expand);
	    }
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
        
        if (sortColumn>=0 && sortColumn<5)
        {
            TreeColumn column = wTree.getColumn(sortColumn);
            wTree.setSortColumn(column);
            wTree.setSortDirection(ascending?SWT.UP:SWT.DOWN);
        }
        refreshTree();
    }

    public RepositoryDirectory getDirectory(TreeItem ti)
	{
		RepositoryDirectory repdir = null;
		
		int level = ConstUI.getTreeLevel(ti);
		String path[] = ConstUI.getTreeStrings(ti);
		
		if (level>1)
		{
			int cat = getItemCategory(ti);
			switch(cat)
			{
			case ITEM_CATEGORY_JOB:
			case ITEM_CATEGORY_TRANSFORMATION:
				{
					// The first 3 levels of text[] don't belong to the path to this transformation!
					String realpath[] = new String[level-2];
					for (int i=0;i<realpath.length;i++) realpath[i] = path[i+2];
					
					repdir = rep.getDirectoryTree().findDirectory(realpath);
				}
				break;
			case ITEM_CATEGORY_JOB_DIRECTORY:
			case ITEM_CATEGORY_TRANSFORMATION_DIRECTORY:
				{
					// The first 3 levels of text[] don't belong to the path to this transformation!
					String realpath[] = new String[level-1];
					for (int i=0;i<realpath.length;i++) realpath[i] = path[i+2];
					
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
	
	public void doDoubleClick()
	{
		final TreeItem tisel[]=wTree.getSelection();
		if (tisel.length==1 || sameCategory(tisel))
		{
			final TreeItem ti = tisel[0];
			final int level = ConstUI.getTreeLevel(ti);
		
			int cat = getItemCategory(ti);
			if ((level >= 2) &&
					((cat == ITEM_CATEGORY_JOB_DIRECTORY) || (cat == ITEM_CATEGORY_TRANSFORMATION_DIRECTORY) || 
							(cat == ITEM_CATEGORY_JOB) || (cat == ITEM_CATEGORY_TRANSFORMATION)))
			{
				String realpath[];
				if ((cat == ITEM_CATEGORY_JOB_DIRECTORY) || (cat == ITEM_CATEGORY_TRANSFORMATION_DIRECTORY))
				{
					// The first levels of path[] don't belong to the path to this directory!
					realpath = new String[level - 1];
				}
				else
				{
					// The first 3 levels of path[] don't belong to the path to this transformation or Job!
					realpath = new String[level - 2];
				}	
				
				final String path[] = ConstUI.getTreeStrings(ti);
				for (int i = 0; i < realpath.length; i++)
				{
					realpath[i] = path[i + 2];
				}
				// Find the directory in the directory tree...
				final RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(realpath);

				switch (cat) {
				case ITEM_CATEGORY_JOB_DIRECTORY:
				case ITEM_CATEGORY_TRANSFORMATION_DIRECTORY: {
					if (!userinfo.isReadonly())	createDirectory(ti, repdir);
					break;
				}
				case ITEM_CATEGORY_TRANSFORMATION: {
					openTransformation(ti.getText(), repdir);
					break;
				}
				case ITEM_CATEGORY_JOB: {
					openJob(ti.getText(), repdir);
					break;
				}
				default:
				}
				
			}
		}
	}
	
	public void setTreeMenu()
	{
		Menu mTree = new Menu(shell, SWT.POP_UP);

		final TreeItem tisel[]=wTree.getSelection();
		if (tisel.length==1 || sameCategory(tisel))
		{
			final TreeItem ti = tisel[0];
			final int level = ConstUI.getTreeLevel(ti);
			final String path[] = ConstUI.getTreeStrings(ti);
			final String item = ti.getText();
		
			int cat = getItemCategory(ti);
			
			switch(cat)
			{
			case ITEM_CATEGORY_ROOT :
				{
					// Export all
					MenuItem miExp  = new MenuItem(mTree, SWT.PUSH); 
					miExp.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Objects.ExportAll")); //$NON-NLS-1$
					SelectionAdapter lsExp = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { exportAll(rep.getDirectoryTree()); } };
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
					SelectionAdapter lsTrans = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { exportTransformations(rep.getDirectoryTree()); } };
					miTrans.addSelectionListener( lsTrans );

					// Export jobs
					MenuItem miJobs  = new MenuItem(mTree, SWT.PUSH); 
					miJobs.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Objects.ExportJob")); //$NON-NLS-1$
					SelectionAdapter lsJobs = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { exportJobs(rep.getDirectoryTree()); } };
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

            case ITEM_CATEGORY_SLAVES_ROOT :
                {
                    // New slave
                    MenuItem miNew  = new MenuItem(mTree, SWT.PUSH); 
                    miNew.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Slave.New")); //$NON-NLS-1$
                    SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newSlaveServer(); } };
                    miNew.addSelectionListener( lsNew );
                    miNew.setEnabled(!userinfo.isReadonly());
                }
                break;
				
            case ITEM_CATEGORY_SLAVE :
                {
                    // New slave
                    MenuItem miNew  = new MenuItem(mTree, SWT.PUSH); 
                    miNew.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Slave.New")); //$NON-NLS-1$
                    SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newSlaveServer(); } };
                    miNew.addSelectionListener( lsNew );
                    miNew.setEnabled(!userinfo.isReadonly());
                    // Edit slave
                    MenuItem miEdit  = new MenuItem(mTree, SWT.PUSH); 
                    miEdit.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Slave.Edit")); //$NON-NLS-1$
                    SelectionAdapter lsEdit = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editSlaveServer(item); } };
                    miEdit.addSelectionListener( lsEdit );
                    miEdit.setEnabled(!userinfo.isReadonly());
                    // Delete slave
                    MenuItem miDel  = new MenuItem(mTree, SWT.PUSH); 
                    miDel.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Slave.Delete")); //$NON-NLS-1$
                    SelectionAdapter lsDel = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { delSlaveServer(item); } };
                    miDel.addSelectionListener( lsDel );
                    miDel.setEnabled(!userinfo.isReadonly());
                }
                break;


            case ITEM_CATEGORY_PARTITIONS_ROOT :
                {
                    // New partition schema
                    MenuItem miNew  = new MenuItem(mTree, SWT.PUSH); 
                    miNew.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.PartitionSchema.New")); //$NON-NLS-1$
                    SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newPartitionSchema(); } };
                    miNew.addSelectionListener( lsNew );
                    miNew.setEnabled(!userinfo.isReadonly());
                }
                break;

            case ITEM_CATEGORY_PARTITION :
                {
                    // New partition schema
                    MenuItem miNew  = new MenuItem(mTree, SWT.PUSH); 
                    miNew.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.PartitionSchema.New")); //$NON-NLS-1$
                    SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newPartitionSchema(); } };
                    miNew.addSelectionListener( lsNew );
                    miNew.setEnabled(!userinfo.isReadonly());
                    // Edit partition schema
                    MenuItem miEdit  = new MenuItem(mTree, SWT.PUSH); 
                    miEdit.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.PartitionSchema.Edit")); //$NON-NLS-1$
                    SelectionAdapter lsEdit = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editPartitionSchema(item); } };
                    miEdit.addSelectionListener( lsEdit );
                    miEdit.setEnabled(!userinfo.isReadonly());
                    // Delete partition schema
                    MenuItem miDel  = new MenuItem(mTree, SWT.PUSH); 
                    miDel.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.PartitionSchema.Delete")); //$NON-NLS-1$
                    SelectionAdapter lsDel = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { delPartitionSchema(item); } };
                    miDel.addSelectionListener( lsDel );
                    miDel.setEnabled(!userinfo.isReadonly());
                }
                break;

            case ITEM_CATEGORY_CLUSTERS_ROOT :
                {
                    // New cluster
                    MenuItem miNew  = new MenuItem(mTree, SWT.PUSH); 
                    miNew.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Cluster.New")); //$NON-NLS-1$
                    SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newCluster(); } };
                    miNew.addSelectionListener( lsNew );
                    miNew.setEnabled(!userinfo.isReadonly());
                }
                break;

            case ITEM_CATEGORY_CLUSTER:
                {
                    // New cluster
                    MenuItem miNew  = new MenuItem(mTree, SWT.PUSH); 
                    miNew.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Cluster.New")); //$NON-NLS-1$
                    SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newCluster(); } };
                    miNew.addSelectionListener( lsNew );
                    miNew.setEnabled(!userinfo.isReadonly());
                    // Edit cluster
                    MenuItem miEdit  = new MenuItem(mTree, SWT.PUSH); 
                    miEdit.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Cluster.Edit")); //$NON-NLS-1$
                    SelectionAdapter lsEdit = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editCluster(item); } };
                    miEdit.addSelectionListener( lsEdit );
                    miEdit.setEnabled(!userinfo.isReadonly());
                    // Delete cluster
                    MenuItem miDel  = new MenuItem(mTree, SWT.PUSH); 
                    miDel.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Cluster.Delete")); //$NON-NLS-1$
                    SelectionAdapter lsDel = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { delCluster(item); } };
                    miDel.addSelectionListener( lsDel );
                    miDel.setEnabled(!userinfo.isReadonly());
                }
                break;
			case ITEM_CATEGORY_TRANSFORMATIONS_ROOT        :
				break;
			case ITEM_CATEGORY_TRANSFORMATION              :
				if (level>=2)
				{
					// The first 1 levels of path[] don't belong to the path to this transformation!
					String realpath[] = new String[level-2];
					for (int i=0;i<realpath.length;i++) realpath[i] = path[i+2];
					
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
			case ITEM_CATEGORY_TRANSFORMATION_DIRECTORY    :
				if (level>=2)
				{
					// The first levels of path[] don't belong to the path to this directory!
					String realpath[] = new String[level-1];
					for (int i=0;i<realpath.length;i++) realpath[i] = path[i+2];
					
					// Find the directory in the directory tree...
					final RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(realpath);

					// Export xforms and jobs from directory
					MenuItem miExp  = new MenuItem(mTree, SWT.PUSH); 
					miExp.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Objects.ExportAll")); //$NON-NLS-1$
					SelectionAdapter lsExp = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { exportAll(repdir); } };
					miExp.addSelectionListener( lsExp );
					miExp.setEnabled(!userinfo.isReadonly());
					
					if (cat == ITEM_CATEGORY_TRANSFORMATION_DIRECTORY)
					{
					// Export transMeta
					MenuItem miTrans  = new MenuItem(mTree, SWT.PUSH); 
					miTrans.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Objects.ExportTrans")); //$NON-NLS-1$
					SelectionAdapter lsTrans = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { exportTransformations(repdir); } };
					miTrans.addSelectionListener( lsTrans );
					}
					
					if (cat == ITEM_CATEGORY_JOB_DIRECTORY)
					{
					// Export jobs
					MenuItem miJobs  = new MenuItem(mTree, SWT.PUSH); 
					miJobs.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Objects.ExportJob")); //$NON-NLS-1$
					SelectionAdapter lsJobs = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { exportJobs(repdir); } };
					miJobs.addSelectionListener( lsJobs );
					}
					
					// create directory...
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

					if (level>2) // Can't rename or delete root directory...
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
					String realpath[] = new String[level-2];
					for (int i=0;i<realpath.length;i++) realpath[i] = path[i+2];
					
					// Find the directory in the directory tree...
					final RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(realpath);
	
                    // Open job...
                    MenuItem miOpen  = new MenuItem(mTree, SWT.PUSH); 
                    miOpen.setText(Messages.getString("RepositoryExplorerDialog.PopupMenu.Jobs.Open")); //$NON-NLS-1$
                    miOpen.addSelectionListener( 
                        new SelectionAdapter() 
                        { 
                            public void widgetSelected(SelectionEvent e) 
                            { 
                                openJob(item, repdir);
                            }
                        }
                    );
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
		
		ConstUI.displayMenu(mTree, wTree);
	}
	
	public void renameInTree()
	{
		TreeItem ti[]=wTree.getSelection();
		if (ti.length==1)
		{
			// Get the parent.
			final TreeItem item = ti[0];
			
			int level = ConstUI.getTreeLevel(item);
			String text[] = ConstUI.getTreeStrings(item);
			
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
					String path[] = new String[level-2];
					for (int i=0;i<path.length;i++) path[i] = text[i+2];
					
					// Find the directory in the directory tree...
					RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(path);
	
					if (repdir!=null) renameTransformation(name, repdir); 
				}
				break;
			case ITEM_CATEGORY_JOB :
				{
					final String name = item.getText();
	
					// The first 3 levels of text[] don't belong to the path to this transformation!
					String path[] = new String[level-2];
					for (int i=0;i<path.length;i++) path[i] = text[i+2];
					
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

	public void rollback()
	{
        rep.rollback();

        props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public void checkRollback(ShellEvent e)
	{
		if (changedInDialog)
		{
			int save = JobGraph.showChangedWarning(shell, "repository");
			if (save == SWT.CANCEL)
			{
				e.doit = false;
			}
			else if (save == SWT.YES)
			{
				commit();
			}
			else
			{
				rollback();
			}
		}
		else
		{
			rollback();
		}
	}
	
	public void commit()
	{
	    try
	    {
	        rep.commit();
	    }
	    catch(KettleException e)
	    {
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.PopupMenu.Dialog.ErrorCommitingChanges.Title"), Messages.getString("RepositoryExplorerDialog.PopupMenu.Dialog.ErrorCommitingChanges.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
	    }
        
        props.setScreen(new WindowProperty(shell));
        shell.dispose();
	}
	
	public void refreshTree()
	{
		try
		{
			wTree.removeAll();
			
			// Load the directory tree:
			rep.setDirectoryTree( new RepositoryDirectory(rep) );
	
			TreeItem tiTree = new TreeItem(wTree, SWT.NONE); 
			tiTree.setImage(GUIResource.getInstance().getImageFolderConnections());
			tiTree.setText(rep.getName()==null?"-":rep.getName()); //$NON-NLS-1$
			
			// The Databases...				
			TreeItem tiParent = new TreeItem(tiTree, SWT.NONE); 
			tiParent.setImage(GUIResource.getInstance().getImageBol());
			tiParent.setText(STRING_DATABASES);
            if (!userinfo.isReadonly()) TreeItemAccelerator.addDoubleClick(tiParent, new DoubleClickInterface() { public void action(TreeItem treeItem) { newDatabase(); } });
	
			String names[] = rep.getDatabaseNames();			
			for (int i=0;i<names.length;i++)
			{
				TreeItem newDB = new TreeItem(tiParent, SWT.NONE);
				newDB.setImage(GUIResource.getInstance().getImageConnection());
				newDB.setText(Const.NVL(names[i], ""));
                if (!userinfo.isReadonly()) TreeItemAccelerator.addDoubleClick(newDB, new DoubleClickInterface() { public void action(TreeItem treeItem) { editDatabase(treeItem.getText()); } });
			}
	
            // The partition schemas...             
            tiParent = new TreeItem(tiTree, SWT.NONE); 
            tiParent.setImage(GUIResource.getInstance().getImageBol());
            tiParent.setText(STRING_PARTITIONS);
            if (!userinfo.isReadonly()) TreeItemAccelerator.addDoubleClick(tiParent, 
            		new DoubleClickInterface() { public void action(TreeItem treeItem) { newPartitionSchema(); } });            
    
            names = rep.getPartitionSchemaNames();          
            for (int i=0;i<names.length;i++)
            {
                TreeItem newItem = new TreeItem(tiParent, SWT.NONE);
                newItem.setImage(GUIResource.getInstance().getImageFolderConnections());
                newItem.setText(Const.NVL(names[i], ""));
                if (!userinfo.isReadonly()) TreeItemAccelerator.addDoubleClick(newItem, 
                		new DoubleClickInterface() { public void action(TreeItem treeItem) { editPartitionSchema(treeItem.getText()); } });                            
            }
            
            // The slaves...         
            tiParent = new TreeItem(tiTree, SWT.NONE); 
            tiParent.setImage(GUIResource.getInstance().getImageBol());
            tiParent.setText(STRING_SLAVES);
            if (!userinfo.isReadonly()) TreeItemAccelerator.addDoubleClick(tiParent, new DoubleClickInterface() { public void action(TreeItem treeItem) { newSlaveServer(); } });

            names = rep.getSlaveNames();          
            for (int i=0;i<names.length;i++)
            {
                TreeItem newItem = new TreeItem(tiParent, SWT.NONE);
                newItem.setImage(GUIResource.getInstance().getImageSlave());
                newItem.setText(Const.NVL(names[i], ""));
                if (!userinfo.isReadonly()) TreeItemAccelerator.addDoubleClick(newItem, new DoubleClickInterface() { public void action(TreeItem treeItem) { editSlaveServer(treeItem.getText()); } });
            }
            
            // The clusters ...
            tiParent = new TreeItem(tiTree, SWT.NONE); 
            tiParent.setImage(GUIResource.getInstance().getImageBol());
            tiParent.setText(STRING_CLUSTERS);
            if (!userinfo.isReadonly()) TreeItemAccelerator.addDoubleClick(tiParent, 
            		new DoubleClickInterface() { public void action(TreeItem treeItem) { newCluster(); } });            
    
            names = rep.getClusterNames();          
            for (int i=0;i<names.length;i++)
            {
                TreeItem newItem = new TreeItem(tiParent, SWT.NONE);
                newItem.setImage(GUIResource.getInstance().getImageCluster());
                newItem.setText(Const.NVL(names[i], ""));
                if (!userinfo.isReadonly()) TreeItemAccelerator.addDoubleClick(newItem, 
                		new DoubleClickInterface() { public void action(TreeItem treeItem) { editCluster(treeItem.getText()); } });                            
            }
    
			// The transformations...				
			if (userinfo.useTransformations())
			{
				TreeItem tiTrans = new TreeItem(tiTree, SWT.NONE); 
				tiTrans.setImage(GUIResource.getInstance().getImageTransGraph());
				tiTrans.setText(STRING_TRANSFORMATIONS);
				
				TreeItem newCat = new TreeItem(tiTrans, SWT.NONE);
				newCat.setImage(GUIResource.getInstance().getImageLogoSmall());
	    		Color dircolor = GUIResource.getInstance().getColorDirectory();
				RepositoryDirectoryUI.getTreeWithNames(newCat, rep, dircolor, sortColumn, ascending, true, false, rep.getDirectoryTree());
			}
			
			// The Jobs...				
			if (userinfo.useJobs())
			{
				TreeItem tiJob = new TreeItem(tiTree, SWT.NONE); 
				tiJob.setImage(GUIResource.getInstance().getImageJobGraph());
				tiJob.setText(STRING_JOBS);
	
				TreeItem newJob = new TreeItem(tiJob, SWT.NONE);
				newJob.setImage(GUIResource.getInstance().getImageLogoSmall());
	    		Color dircolor = GUIResource.getInstance().getColorDirectory();
				RepositoryDirectoryUI.getTreeWithNames(newJob, rep, dircolor, sortColumn, ascending, false, true, rep.getDirectoryTree());
			}
	
			//
			// Add the users or only yourself
			//
			TreeItem tiUser = new TreeItem(tiTree, SWT.NONE);
			tiUser.setImage(GUIResource.getInstance().getImageBol());
			tiUser.setText(STRING_USERS);
            if (!userinfo.isReadonly()) TreeItemAccelerator.addDoubleClick(tiUser, new DoubleClickInterface() { public void action(TreeItem treeItem) { newUser(); } });
			
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
					    newUser.setImage(GUIResource.getInstance().getImageUser());
					    newUser.setText(users[i]);
                        if (!userinfo.isReadonly()) TreeItemAccelerator.addDoubleClick(newUser, new DoubleClickInterface() { public void action(TreeItem treeItem) { editUser(treeItem.getText()); } });
					}
				}
			}
	
			//
			// Add the profiles if you're admin...
			//
			if (userinfo.isAdministrator())
			{
				TreeItem tiProf = new TreeItem(tiTree, SWT.NONE);
				tiProf.setImage(GUIResource.getInstance().getImageBol());
				tiProf.setText(STRING_PROFILES);
                TreeItemAccelerator.addDoubleClick(tiProf, new DoubleClickInterface() { public void action(TreeItem treeItem) { newProfile(); } });

				String prof[] = rep.getProfiles();
				for (int i=0;i<prof.length;i++)
				{
					TreeItem newProf = new TreeItem(tiProf, SWT.NONE);
					newProf.setImage(GUIResource.getInstance().getImageProfil());
					newProf.setText(prof[i]);
                    TreeItemAccelerator.addDoubleClick(newProf, new DoubleClickInterface() { public void action(TreeItem treeItem) { editProfile(treeItem.getText()); } });
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
		lastOpened = new RepositoryObjectReference(STRING_TRANSFORMATIONS, repdir, name);
		if (callback != null) {
			if (callback.open(lastOpened))			{
				commit();
			}
		}
		else {
			commit();
		}
	}

	public void openJob(String name, RepositoryDirectory repdir)
	{
		lastOpened = new RepositoryObjectReference(STRING_JOBS, repdir, name);
		if (callback != null) {
			if (callback.open(lastOpened)) {
				commit();
			}
		}
		else {
			commit();
		}
	}

	public boolean delSelectedTransformations()
	{
		boolean retval=false;
		TreeItem tiSel[] = wTree.getSelection();
		boolean done  = false;
		boolean error = false;

		MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
		mb.setMessage(Messages.getString("RepositoryExplorerDialog.Trans.Delete.Confirm.Message1")+(tiSel.length>1?Messages.getString("RepositoryExplorerDialog.Trans.Delete.Confirm.Message2")+tiSel.length+Messages.getString("RepositoryExplorerDialog.Trans.Delete.Confirm.Message3"):Messages.getString("RepositoryExplorerDialog.Trans.Delete.Confirm.Message4"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		mb.setText(Messages.getString("RepositoryExplorerDialog.Trans.Delete.Confirm.Title")); //$NON-NLS-1$
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
					catch(KettleException dbe)
					{
						new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Trans.Delete.ErrorRemoving.Title"), Messages.getString("RepositoryExplorerDialog.Trans.Delete.ErrorRemoving.Message")+name+"]", dbe); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						error=true;
					}
				}
				else
				{
					mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.Trans.Delete.ErrorFinding.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Trans.Delete.ErrorFinding.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					mb.setText(Messages.getString("RepositoryExplorerDialog.Trans.Delete.ErrorFinding.Title")); //$NON-NLS-1$
					mb.open();
					error=true;
				}
		
				if (!error)
				{
					retval=true;
				}
				if (done) ti.dispose();
			}
			catch(KettleException dbe)
			{
				new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Trans.Delete.ErrorDeleting.Title"), Messages.getString("RepositoryExplorerDialog.Trans.Delete.ErrorDeleting.Message"), dbe); //$NON-NLS-1$ //$NON-NLS-2$
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
                        // Focus is lost: apply changes.
                        String newname = text.getText();
                        if (renameTransformation(fname, newname, frepdir)) 
                        {
                            ti.setText(newname);
                        }
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
				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Trans.Rename.ErrorFinding.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Trans.Rename.ErrorFinding.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				mb.setText(Messages.getString("RepositoryExplorerDialog.Trans.Rename.ErrorFinding.Title")); //$NON-NLS-1$
				mb.open();
			}
		}
		catch(KettleException dbe)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Trans.Rename.ErrorRenaming.Title"), Messages.getString("RepositoryExplorerDialog.Trans.Rename.ErrorRenaming.Message")+name+"]!", dbe); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
                    long existingTransID = rep.getTransformationID(transname, repdir.getID());
                    if (existingTransID == -1) {
	                    rep.moveTransformation(transname, fromdir.getID(), repdir.getID());
	    				changedInDialog = true;
	    				retval=true;
                    }
                    else
                    {
                    	MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Trans.Move.ErrorDuplicate.Message", transname)+Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
        				mb.setText(Messages.getString("RepositoryExplorerDialog.Trans.Move.ErrorDuplicate.Title")); //$NON-NLS-1$
        				mb.open();
                    }
    			}
    			else
    			{
    				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
    				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Trans.Move.ErrorMoving.Message")+dirname+"]"+Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
    				mb.setText(Messages.getString("RepositoryExplorerDialog.Trans.Move.ErrorMoving.Title")); //$NON-NLS-1$
    				mb.open();
    			}
            }
		}
		catch(Exception dbe)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Trans.Move.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Trans.Move.UnexpectedError.Message"), dbe); //$NON-NLS-1$ //$NON-NLS-2$
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
                    long existingjobID = rep.getJobID(jobname, repdir.getID());
                    if (existingjobID == -1) {
                    	rep.moveJob(jobname, fromdir.getID(), repdir.getID());
                    	changedInDialog = true;
                    	retval=true;
                    }
                    else 
                    {
                    	MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Job.Move.ErrorDuplicate.Message", jobname)+Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
        				mb.setText(Messages.getString("RepositoryExplorerDialog.Job.Move.ErrorDuplicate.Title")); //$NON-NLS-1$
        				mb.open();	
                    }
    			}
    			else
    			{
    				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
    				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Job.Move.ErrorMoving.Message")+dirname+"]"+Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
    				mb.setText(Messages.getString("RepositoryExplorerDialog.Job.Move.ErrorMoving.Title")); //$NON-NLS-1$
    				mb.open();
    			}
            }
		}
		catch(Exception dbe)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Job.Move.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Trans.Move.UnexpectedError.Message"), dbe); //$NON-NLS-1$ //$NON-NLS-2$
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
                        // Focus is lost: apply changes
                        String newname = text.getText();
                        if (renameJob(fname, newname, frepdir)) 
                        {
                            ti.setText(newname);
                        }
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
    				changedInDialog = true;
					retval=true;
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Job.Move.ErrorFinding.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Job.Move.ErrorFinding.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				mb.setText(Messages.getString("RepositoryExplorerDialog.Job.Move.ErrorFinding.Title")); //$NON-NLS-1$
				mb.open();
			}
		}
		catch(KettleException dbe)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Job.Move.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Job.Move.UnexpectedError.Message")+name+"]", dbe); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Job.Delete.Confirmation.Message")); //$NON-NLS-1$
				mb.setText("["+name+"]"); //$NON-NLS-1$ //$NON-NLS-2$
				int answer = mb.open();
				
				if (answer==SWT.YES)
				{
					// System.out.println("OK, Deleting transformation ["+name+"] with ID = "+id);
					rep.delAllFromJob(id);
    				changedInDialog = true;
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Job.Delete.ErrorFinding.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Job.Delete.ErrorFinding.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				mb.setText(Messages.getString("RepositoryExplorerDialog.Job.Delete.ErrorFinding.Title")); //$NON-NLS-1$
				mb.open();
				error=true;
			}
		}
		catch(KettleException dbe)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Job.Delete.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Job.Delete.UnexpectedError.Message")+name+"]", dbe); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
                    // Focus is lost: apply changes
                    String newname = text.getText();
                    if (renameJob(name, repdir, newname)) 
                    {
                        ti.setText(newname);
                    }
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
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.Job.Rename.ErrorFinding.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Job.Rename.ErrorFinding.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					mb.setText(Messages.getString("RepositoryExplorerDialog.Job.Rename.ErrorFinding.Title")); //$NON-NLS-1$
					mb.open();
				}								
			}
		}
		catch(KettleException dbe)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Job.Rename.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Job.Rename.UnexpectedError.Message")+name+"]", dbe); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		return retval;
	}
    
	public void editUser(String login)
	{
		try
		{
			UserInfo uinfo = new UserInfo(rep, login); // Get UserInfo from repository...
			UserDialog ud = new UserDialog(shell, SWT.NONE, rep, uinfo);
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
				mb.setMessage(Messages.getString("RepositoryExplorerDialog.User.Edit.NotAllowed.Message")); //$NON-NLS-1$
				mb.setText(Messages.getString("RepositoryExplorerDialog.User.Edit.NotAllowed.Title")); //$NON-NLS-1$
				mb.open();
			}
			if(ui!=null && !login.equalsIgnoreCase(ui.getLogin())) refreshTree();
			
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.User.Edit.UnexpectedError.Message.Title"), Messages.getString("RepositoryExplorerDialog.User.Edit.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void newUser()
	{
		UserDialog ud = new UserDialog(shell, SWT.NONE, rep, new UserInfo());
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
				mb.setMessage(Messages.getString("RepositoryExplorerDialog.User.New.AlreadyExists.Message")); //$NON-NLS-1$
				mb.setText(Messages.getString("RepositoryExplorerDialog.User.New.AlreadyExists.Title")); //$NON-NLS-1$
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
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.User.Delete.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.User.Delete.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
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
                        // Focus is lost: apply changes
                        String newname = text.getText();
                        if (renameUser(name, newname)) 
                        {
                            ti.setText(newname);
                        }
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
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.User.Rename.ErrorFinding.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.User.Rename.ErrorFinding.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					mb.setText(Messages.getString("RepositoryExplorerDialog.User.Rename.ErrorFinding.Title")); //$NON-NLS-1$
					mb.open();
				}
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.User.Rename.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.User.Rename.UnexpectedError.Message")+name+"]", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
                        // Focus is lost: apply changes
                        String newname = text.getText();
                        if (renameProfile(name, newname)) 
                        {
                            ti.setText(newname);
                        }
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
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.Profile.Rename.ErrorFinding.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Profile.Rename.ErrorFinding.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					mb.setText(Messages.getString("RepositoryExplorerDialog.Profile.Rename.ErrorFinding.Title")); //$NON-NLS-1$
					mb.open();
				}
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Profile.Rename.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Profile.Rename.UnexpectedError.Message")+name+"]", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		return retval;
	}


	public void editDatabase(String databasename)
	{
		try
		{
			long idDatabase = rep.getDatabaseID(databasename);
			DatabaseMeta databaseMeta = RepositoryUtil.loadDatabaseMeta(rep, idDatabase);

			DatabaseDialog dd = new DatabaseDialog(shell, databaseMeta);
			String name = dd.open();
			if (name!=null)
			{
				if (!userinfo.isReadonly())
				{
                    rep.lockRepository();
                    rep.insertLogEntry("Updating database connection '"+databaseMeta.getName()+"'");
                    RepositoryUtil.saveDatabaseMeta(databaseMeta,rep);
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.Connection.Edit.NotAllowed.Message")); //$NON-NLS-1$
					mb.setText(Messages.getString("RepositoryExplorerDialog.Connection.Edit.NotAllowed.Title")); //$NON-NLS-1$
					mb.open();
				}
				if(!databasename.equalsIgnoreCase(name)) refreshTree();
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Connection.Edit.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Connection.Edit.UnexpectedError.Message")+databasename+"]", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
        finally
        {
            try
            {
                rep.unlockRepository();
            }
            catch(KettleException e)
            {
                new ErrorDialog(shell, "Error", "Unexpected error unlocking the repository database", e);
            }
        }
	}

	public void newDatabase()
	{
		try
		{
			DatabaseMeta databaseMeta = new DatabaseMeta();
			databaseMeta.initializeVariablesFrom(null);
			DatabaseDialog dd = new DatabaseDialog(shell, databaseMeta);
			String name = dd.open();
			if (name!=null)
			{
				// See if this user already exists...
				long idDatabase = rep.getDatabaseID(name);
				if (idDatabase<=0)
				{
                    rep.lockRepository();
                    rep.insertLogEntry("Creating new database '"+databaseMeta.getName()+"'");
                    RepositoryUtil.saveDatabaseMeta(databaseMeta,rep);
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.Connection.Create.AlreadyExists.Message")); //$NON-NLS-1$
					mb.setText(Messages.getString("RepositoryExplorerDialog.Connection.Create.AlreadyExists.Title")); //$NON-NLS-1$
					mb.open();
				}
					// Refresh tree...
				refreshTree();
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Connection.Create.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Connection.Create.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
        finally
        {
            try
            {
                rep.unlockRepository();
            }
            catch(KettleException e)
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
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Connection.Delete.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Connection.Delete.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
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
						// Focus is lost: apply changes
                        String newname = text.getText();
                        if (renameDatabase(name, newname)) 
                        {
                            ti.setText(newname);
                        }
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
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.Connection.Rename.ErrorFinding.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Connection.Rename.ErrorFinding.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					mb.setText(Messages.getString("RepositoryExplorerDialog.Connection.Rename.ErrorFinding.Title")); //$NON-NLS-1$
					mb.open();
				}
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Connection.Rename.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Connection.Rename.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
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
					// Focus is lost: apply changes
                    String newname = text.getText();
                    if (renameDirectory(repdir, name, newname)) 
                    {
                        ti.setText(newname);
                    }
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
    				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Directory.Rename.UnexpectedError.Message1")+name+"]"+Const.CR+Messages.getString("RepositoryExplorerDialog.Directory.Rename.UnexpectedError.Message2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    				mb.setText(Messages.getString("RepositoryExplorerDialog.Directory.Rename.UnexpectedError.Title")); //$NON-NLS-1$
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
            new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Directory.Rename.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Directory.Rename.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
		return retval;
	}

	public void editProfile(String profilename)
	{
		try
		{
			long idProfile = rep.getProfileID(profilename);
			ProfileMeta profinfo = new ProfileMeta(rep, idProfile);
			
			// System.out.println("editProfile, nrPermissions = "+profinfo.nrPermissions());
	
			ProfileDialog pd = new ProfileDialog(shell, SWT.NONE, profinfo);
			String name = pd.open();
			if (name!=null)
			{
				profinfo.saveRep(rep);
			}
				
			if(!profilename.equalsIgnoreCase(name)) refreshTree();
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Profile.Edit.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Profile.Edit.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void newProfile()
	{
		try
		{
			ProfileMeta profinfo = new ProfileMeta();
			ProfileDialog pd = new ProfileDialog(shell, SWT.NONE, profinfo);
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
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.Profile.Create.AlreadyExists.Message")); //$NON-NLS-1$
					mb.setText(Messages.getString("RepositoryExplorerDialog.Profile.Create.AlreadyExists.Title")); //$NON-NLS-1$
					mb.open();
				}
					
				// Refresh tree...
				refreshTree();
			} 
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Profile.Create.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Profile.Create.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}

	public void delProfile(String profilename)
	{
		try
		{
			long idProfile = rep.getProfileID(profilename);
			if (idProfile>0)
			{
			    rep.delProfilePermissions(idProfile);
			    rep.delProfile(idProfile);
			}
	
			refreshTree();
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Profile.Delete.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Profile.Delete.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public void createDirectory(TreeItem ti, RepositoryDirectory repdir)
	{
		EnterStringDialog esd = new EnterStringDialog(shell, Messages.getString("RepositoryExplorerDialog.Directory.Create.AskName.Default"), Messages.getString("RepositoryExplorerDialog.Directory.Create.AskName.Title"), Messages.getString("RepositoryExplorerDialog.Directory.Create.AskName.Message")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
					mb.setMessage(Messages.getString("RepositoryExplorerDialog.Directory.Create.UnexpectedError.Message1")+newdir+Messages.getString("RepositoryExplorerDialog.Directory.Create.UnexpectedError.Message2")+repdir.getPath()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					mb.setText(Messages.getString("RepositoryExplorerDialog.Directory.Create.UnexpectedError.Title")); //$NON-NLS-1$
					mb.open();
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				mb.setMessage(Messages.getString("RepositoryExplorerDialog.Directory.Create.AlreadyExists.Message1")+newdir+Messages.getString("RepositoryExplorerDialog.Directory.Create.AlreadyExists.Message2")+repdir.getPath()+Messages.getString("RepositoryExplorerDialog.Directory.Create.AlreadyExists.Message3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				mb.setText(Messages.getString("RepositoryExplorerDialog.Directory.Create.AlreadyExists.Title")); //$NON-NLS-1$
				mb.open();
			}
		}
	}
	
	public void delDirectory(TreeItem ti, RepositoryDirectory repdir)
	{
		MessageBox rmb = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
		rmb.setMessage(Messages.getString("RepositoryExplorerDialog.Dialog.delDirectory.Message")); //$NON-NLS-1$
		rmb.setText(Messages.getString("RepositoryExplorerDialog.Dialog.delDirectory.Title")); //$NON-NLS-1$
		int answer = rmb.open();
		
		if (answer == SWT.YES)
		{
			try
			{
				repdir.delFromRep(rep);
				refreshTree();
			}
			catch(KettleException e)
			{
				new ErrorDialog(shell,
						Messages.getString("RepositoryExplorerDialog.Directory.Delete.ErrorRemoving.Title"), //$NON-NLS-1$
						Messages.getString("RepositoryExplorerDialog.Directory.Delete.ErrorRemoving.Message1"), //$NON-NLS-1$
						e
				); 
			}
		}
	}
	
	public void exportTransformations(RepositoryDirectory root)
	{
		try
		{
			DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
			if (dialog.open()!=null)
			{
				String directory = dialog.getFilterPath();
				
				long dirids[] = ((null == root)? rep.getDirectoryTree() : root).getDirectoryIDs();
				for (int d=0;d<dirids.length;d++)
				{
					RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(dirids[d]);				
					String trans[] = rep.getTransformationNames(dirids[d]);
				
					// See if the directory exists...
					File dir = new File(directory+repdir.getPath());
					if (!dir.exists()) 
					{
						dir.mkdir();
						log.logBasic("Exporting transformation", "Created directory ["+dir.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
					
					for (int i=0;i<trans.length;i++)
					{
						TransMeta ti = new TransMeta(rep, trans[i], repdir);
						if(log.isBasic()) log.logBasic("Exporting transformation", "["+trans[i]+"] in directory ["+repdir.getPath()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

						String xml = XMLHandler.getXMLHeader() + ti.getXML();
							
						String filename = directory+repdir.getPath()+Const.FILE_SEPARATOR+fixFileName(trans[i])+".ktr"; //$NON-NLS-1$						
						File f = new File(filename);
						try
						{
							FileOutputStream fos = new FileOutputStream(f);
							fos.write(xml.getBytes(Const.XML_ENCODING));
							fos.close();
						}
						catch(IOException e)
						{
							throw new RuntimeException("Exporting transformation: Couldn't create file ["+filename+"]",e); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.ExportTrans.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.ExportTrans.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
	
	private String fixFileName(String filename) {
		filename = filename.replace('/', '_'); // do something with illegal file name chars
		if (!("/".equals(Const.FILE_SEPARATOR)))
		{
			filename = Const.replace(filename, Const.FILE_SEPARATOR, "_");
		}
		return filename;
	}

	public void exportJobs(RepositoryDirectory root)
	{
		try
		{
			DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
			if (dialog.open()!=null)
			{
				String directory = dialog.getFilterPath();
	
				long dirids[] = ((null == root)? rep.getDirectoryTree() : root).getDirectoryIDs();
				for (int d=0;d<dirids.length;d++)
				{
					RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(dirids[d]);				
					String jobs[] = rep.getJobNames(dirids[d]);

					// See if the directory exists...
					File dir = new File(directory+repdir.getPath());
					if (!dir.exists()) 
					{
						dir.mkdir();
						log.logBasic("Exporting Jobs", "Created directory ["+dir.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$							
					}
				
					for (int i=0;i<jobs.length;i++)
					{
						JobMeta ji = new JobMeta(log, rep, jobs[i], repdir);
						log.logBasic("Exporting Jobs", "["+jobs[i]+"] in directory ["+repdir.getPath()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

						String xml = XMLHandler.getXMLHeader() + ji.getXML();
						
						String filename = directory+repdir.getPath()+Const.FILE_SEPARATOR+fixFileName(jobs[i])+".kjb"; //$NON-NLS-1$
						File f = new File(filename);
						try
						{
							FileOutputStream fos = new FileOutputStream(f);
							fos.write(xml.getBytes(Const.XML_ENCODING));
							fos.close();
						}
						catch(IOException e)
						{
							throw new RuntimeException("Exporting jobs: Couldn't create file ["+filename+"]",e); //$NON-NLS-1$ //$NON-NLS-2$							
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.ExportJobs.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.ExportJobs.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public void exportAll(RepositoryDirectory dir)
	{
		FileDialog dialog = new FileDialog(shell, SWT.SAVE | SWT.SINGLE);
		if (dialog.open()!=null)
		{
			String filename = dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName();
			if(log.isBasic()) log.logBasic("Exporting All", "Export objects to file ["+filename+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
			RepositoryExportProgressDialog repd = new RepositoryExportProgressDialog(shell, rep, dir, filename);
			repd.open();
		}
	}

	public void importAll()
	{
		FileDialog dialog = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
		if (dialog.open()!=null)
		{
			String[] filenames = dialog.getFileNames();
			if (filenames.length > 0)
			{
			SelectDirectoryDialog sdd = new SelectDirectoryDialog(shell, SWT.NONE, rep);
			RepositoryDirectory baseDirectory = sdd.open();
			if (baseDirectory!=null)
			{
						RepositoryImportProgressDialog ripd = new RepositoryImportProgressDialog(shell, SWT.NONE, rep, dialog.getFilterPath(), filenames, baseDirectory);
				ripd.open();
				
				refreshTree();
				}
			}
		}
	}
	
	private int getItemCategory(TreeItem ti)
	{
		int cat = ITEM_CATEGORY_NONE;
		
		int level = ConstUI.getTreeLevel(ti);
		String path[] = ConstUI.getTreeStrings(ti);
		
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
		}
		else
		if (level>=2)
		{
			     if (parent.equals(STRING_USERS)) cat = ITEM_CATEGORY_USER;
			else if (parent.equals(STRING_PROFILES)) cat = ITEM_CATEGORY_PROFILE;
            else if (parent.equals(STRING_DATABASES)) cat = ITEM_CATEGORY_DATABASE;
            else if (parent.equals(STRING_PARTITIONS)) cat = ITEM_CATEGORY_PARTITION;
            else if (parent.equals(STRING_SLAVES)) cat = ITEM_CATEGORY_SLAVE;
            else if (parent.equals(STRING_CLUSTERS)) cat = ITEM_CATEGORY_CLUSTER;
                 
			final Color dircolor = GUIResource.getInstance().getColorDirectory();
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
		}
		
		return cat;
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}

    public void newSlaveServer()
    {
        try
        {
            SlaveServer slaveServer = new SlaveServer();
            SlaveServerDialog dd = new SlaveServerDialog(shell, slaveServer);
            if (dd.open())
            {
                // See if this slave server already exists...
                long idSlave = rep.getSlaveID(slaveServer.getName());
                if (idSlave<=0)
                {
                    rep.lockRepository();
                    rep.insertLogEntry("Creating new slave server '"+slaveServer.getName()+"'");
                    slaveServer.saveRep(rep);
                }
                else
                {
                    MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                    mb.setMessage(Messages.getString("RepositoryExplorerDialog.Slave.Create.AlreadyExists.Message")); //$NON-NLS-1$
                    mb.setText(Messages.getString("RepositoryExplorerDialog.Slave.Create.AlreadyExists.Title")); //$NON-NLS-1$
                    mb.open();
                }
                    // Refresh tree...
                refreshTree();
            }
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Connection.Create.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Connection.Create.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        finally
        {
            try
            {
                rep.unlockRepository();
            }
            catch(KettleException e)
            {
                new ErrorDialog(shell, "Error", "Unexpected error unlocking the repository database", e);
            }
        }
    }
    
    public void editSlaveServer(String slaveName)
    {
        try
        {
            long id = rep.getSlaveID(slaveName);
            SlaveServer slaveServer = new SlaveServer(rep, id);

            SlaveServerDialog dd = new SlaveServerDialog(shell, slaveServer);
            if (dd.open())
            {
                rep.lockRepository();
                rep.insertLogEntry("Updating slave server '"+slaveServer.getName()+"'");
                slaveServer.saveRep(rep);
                if(!slaveName.equalsIgnoreCase(slaveServer.getName())) refreshTree();
            }
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Slave.Edit.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Slave.Edit.UnexpectedError.Message")+slaveName+"]", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        finally
        {
            try
            {
                rep.unlockRepository();
            }
            catch(KettleException e)
            {
                new ErrorDialog(shell, "Error", "Unexpected error unlocking the repository database", e);
            }
        }
    }
    
    public void delSlaveServer(String slaveName)
    {
        try
        {
            long id = rep.getSlaveID(slaveName);
            if (id>0)
            {
                rep.delSlave(id);
            }
    
            refreshTree();
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Slave.Delete.UnexpectedError.Title"), Messages.getString("RepositoryExplorerDialog.Slave.Delete.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public void newPartitionSchema()
    {
        try
        {
            PartitionSchema partitionSchema = new PartitionSchema();
            PartitionSchemaDialog dd = new PartitionSchemaDialog(shell, partitionSchema, rep.getDatabases(), variableSpace);
            if (dd.open())
            {
                // See if this slave server already exists...
                long idPartitionSchema = rep.getPartitionSchemaID(partitionSchema.getName());
                if (idPartitionSchema<=0)
                {
                    rep.lockRepository();
                    rep.insertLogEntry("Creating new partition schema '"+partitionSchema.getName()+"'");
                    partitionSchema.saveRep(rep);
                }
                else
                {
                    MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                    mb.setMessage(Messages.getString("RepositoryExplorerDialog.PartitionSchema.Create.AlreadyExists.Message")); //$NON-NLS-1$
                    mb.setText(Messages.getString("RepositoryExplorerDialog.PartitionSchema.Create.AlreadyExists.Title")); //$NON-NLS-1$
                    mb.open();
                }
                    // Refresh tree...
                refreshTree();
            }
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell,Messages.getString("RepositoryExplorerDialog.PartitionSchema.Create.UnexpectedError.Title"), 
                    Messages.getString("RepositoryExplorerDialog.PartitionSchema.Create.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        finally
        {
            try
            {
                rep.unlockRepository();
            }
            catch(KettleException e)
            {
                new ErrorDialog(shell, "Error", "Unexpected error unlocking the repository database", e);
            }
        }
    }
    
    public void editPartitionSchema(String partitionSchemaName)
    {
        try
        {
            long id = rep.getPartitionSchemaID(partitionSchemaName);
            PartitionSchema partitionSchema = new PartitionSchema(rep, id);

            PartitionSchemaDialog dd = new PartitionSchemaDialog(shell, partitionSchema, rep.getDatabases(), variableSpace);
            if (dd.open())
            {
                rep.lockRepository();
                rep.insertLogEntry("Updating partition schema '"+partitionSchema.getName()+"'");
                partitionSchema.saveRep(rep);
                if(!partitionSchemaName.equalsIgnoreCase(partitionSchema.getName())) refreshTree();
            }
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.PartitionSchema.Edit.UnexpectedError.Title"), 
                    Messages.getString("RepositoryExplorerDialog.PartitionSchema.Edit.UnexpectedError.Message")+partitionSchemaName+"]", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        finally
        {
            try
            {
                rep.unlockRepository();
            }
            catch(KettleException e)
            {
                new ErrorDialog(shell, "Error", "Unexpected error unlocking the repository database", e);
            }
        }
    }
    
    public void delPartitionSchema(String partitionSchemaName)
    {
        try
        {
            long id = rep.getPartitionSchemaID(partitionSchemaName);
            if (id>0)
            {
                rep.delPartitionSchema(id);
            }
    
            refreshTree();
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.PartitionSchema.Delete.UnexpectedError.Title"), 
                    Messages.getString("RepositoryExplorerDialog.PartitionSchema.Delete.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public void newCluster()
    {
        try
        {
            ClusterSchema cluster = new ClusterSchema();
            ClusterSchemaDialog dd = new ClusterSchemaDialog(shell, cluster, rep.getSlaveServers());
            if (dd.open())
            {
                // See if this slave server already exists...
                long idCluster = rep.getClusterID(cluster.getName());
                if (idCluster<=0)
                {
                    rep.lockRepository();
                    rep.insertLogEntry("Creating new cluster '"+cluster.getName()+"'");
                    cluster.saveRep(rep);
                }
                else
                {
                    MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                    mb.setMessage(Messages.getString("RepositoryExplorerDialog.Cluster.Create.AlreadyExists.Message")); //$NON-NLS-1$
                    mb.setText(Messages.getString("RepositoryExplorerDialog.Cluster.Create.AlreadyExists.Title")); //$NON-NLS-1$
                    mb.open();
                }
                    // Refresh tree...
                refreshTree();
            }
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Cluster.Create.UnexpectedError.Title"), 
                    Messages.getString("RepositoryExplorerDialog.Cluster.Create.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        finally
        {
            try
            {
                rep.unlockRepository();
            }
            catch(KettleException e)
            {
                new ErrorDialog(shell, "Error", "Unexpected error unlocking the repository database", e);
            }
        }
    }
    
    public void editCluster(String clusterName)
    {
        try
        {
            long id = rep.getClusterID(clusterName);
            ClusterSchema cluster = new ClusterSchema(rep, id, rep.getSlaveServers());

            ClusterSchemaDialog dd = new ClusterSchemaDialog(shell, cluster, rep.getSlaveServers());
            if (dd.open())
            {
                rep.lockRepository();
                rep.insertLogEntry("Updating cluster '"+cluster.getName()+"'");
                cluster.saveRep(rep);
                if(!clusterName.equalsIgnoreCase(cluster.getName())) refreshTree();
            }
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Cluster.Edit.UnexpectedError.Title"), 
                    Messages.getString("RepositoryExplorerDialog.Cluster.Edit.UnexpectedError.Message")+clusterName+"]", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        finally
        {
            try
            {
                rep.unlockRepository();
            }
            catch(KettleException e)
            {
                new ErrorDialog(shell, "Error", "Unexpected error unlocking the repository database", e);
            }
        }
    }
    
    public void delCluster(String clusterName)
    {
        try
        {
            long id = rep.getClusterID(clusterName);
            if (id>0)
            {
                rep.delClusterSchema(id);
            }
    
            refreshTree();
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("RepositoryExplorerDialog.Cluster.Delete.UnexpectedError.Title"), 
                    Messages.getString("RepositoryExplorerDialog.Cluster.Delete.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}
