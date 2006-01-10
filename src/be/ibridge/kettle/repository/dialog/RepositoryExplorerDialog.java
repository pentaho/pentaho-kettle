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
	private static final String STRING_OBJECTS         = "Objects";
	private static final String STRING_DATABASES       = "Connections";
	public  static final String STRING_TRANSFORMATIONS = "Transformations";
	public  static final String STRING_JOBS            = "Jobs";
	public  static final String STRING_SCHEMAS         = "Schema's";
	private static final String STRING_USERS           = "Users";
	private static final String STRING_PROFILES        = "Profiles";
	
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
	}

	public String open() 
	{
        debug="opening repository explorer";
        
        try
        {
    		dircolor = GUIResource.getInstance().getColorDirectory();
    		
            debug="open new shell";
            Shell parent = getParent();
    		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
    		props.setLook(shell);
    		shell.setText("Repository explorer on ["+rep.getName()+"]");
    		
    		FormLayout formLayout = new FormLayout ();
    		formLayout.marginWidth  = Const.FORM_MARGIN;
    		formLayout.marginHeight = Const.FORM_MARGIN;
    		
    		shell.setLayout (formLayout);
     		
     		// Tree
     		wTree = new Tree(shell, SWT.MULTI | SWT.BORDER /*| (multiple?SWT.CHECK:SWT.NONE)*/);
     		props.setLook(wTree);
    
     		// Buttons
    		wOK = new Button(shell, SWT.PUSH); 
    		wOK.setText("  &OK  ");
    		wCancel = new Button(shell, SWT.PUSH); 
    		wCancel.setText("  &Cancel  ");
    				
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
                        debug="drag start";
    					event.doit=true;
    				}
    	
    				public void dragSetData(DragSourceEvent event) 
    				{
                        debug="drag set data";

    					event.data = "";
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
                                debug="drag set: drag around transformation";
    							RepositoryDirectory repdir = getDirectory(ti[0]);
    							if (repdir!=null)
    							{
    								//
    								// Pass info as a piece of XML
    								//
    								String xml=XMLHandler.getXMLHeader();
    								xml+="<dragdrop>"+Const.CR;
    								xml+="  "+XMLHandler.addTagValue("directory", repdir.getPath());
    								xml+="  "+XMLHandler.addTagValue("transformation", ti[0].getText());
    								xml+="</dragdrop>"+Const.CR;
    								
    								event.data = xml;
    								event.doit = true;
    							}
    						}
                            else
                            {
                                debug="do nothing";
                                String xml=XMLHandler.getXMLHeader();
                                xml+="<dragdrop>"+Const.CR;
                                xml+="</dragdrop>"+Const.CR;
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
                    debug="drag leave";
    			}
    			
    			public void dragOperationChanged(DropTargetEvent event) 
    			{
    			}
    			
    			public void dragOver(DropTargetEvent event) 
    			{ 
                    debug="drag over";    				
    			}
    			public void drop(DropTargetEvent event) 
    			{
                    try
                    {
                        debug="Drop item in tree";
                        
        				if (event.data == null)  // no data to copy, indicate failure in event.detail 
        				{
        					event.detail = DND.DROP_NONE;
        					return;
        				}
        					
        				// event.feedback = DND.FEEDBACK_NONE;
        
        				TreeItem ti = (TreeItem)event.item;
        				if (ti!=null)
        				{
                            debug="Get category";
        					int category = getItemCategory(ti);
        					if (category==ITEM_CATEGORY_TRANSFORMATION_DIRECTORY ||
        					    category==ITEM_CATEGORY_TRANSFORMATION
        					   )
        					{
                                debug="Get directory";
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
                                        mb.setMessage("I'm sorry, but I can't move the transformation.");
                                        mb.setText("Sorry...");
                                        mb.open();
                                    }
        						}
        					}
                            else
                            {
                                MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK );
                                mb.setMessage("I'm sorry, but I can't move this item at this time.");
                                mb.setText("Sorry...");
                                mb.open();
                            }
        				}
                    }
                    catch(Throwable e)
                    {
                        new ErrorDialog(shell, props, "Unexpected error", "Error in part ["+debug+"]"+Const.CR+"Please report this error to the Kettle developers!", new Exception(e));
                    }
    			}
    
    			public void dropAccept(DropTargetEvent event) 
    			{
                    debug="drop accept";
    			}
    		});
    
    
    		// Detect X or ALT-F4 or something that kills this window...
    		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { dispose(); } } );
            
            debug="set screen size and position";
    
    		WindowProperty winprop = props.getScreen(shell.getText());
    		if (winprop!=null) winprop.setShell(shell); 
    		else 
    		{
                debug="pack screen";
    		    shell.pack();
                debug="set default screen size ";
    		    shell.setSize(400, 480);
    		}
    
    		refreshTree();
    
    		shell.open();
    		Display display = parent.getDisplay();
    		while (!shell.isDisposed()) 
    		{
    			if (!display.readAndDispatch()) display.sleep();
    		}
        }
        catch(Throwable e)
        {
            new ErrorDialog(shell, props, "Unexpected error", "An unexpected error occurred in the repository explorer in part ["+debug+"]"+Const.CR+"Please contact support for a software update."+Const.CR+"We appoligize for the unconvenience.", new Exception(e));
        }
		return objectName;

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
					miExp.setText("&Export all objects to an XML file");
					SelectionAdapter lsExp = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { exportAll(); } };
					miExp.addSelectionListener( lsExp );

					// Import all
					MenuItem miImp  = new MenuItem(mTree, SWT.PUSH); 
					miImp.setText("&Import all objects from an XML file");
					SelectionAdapter lsImp = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { importAll(); } };
					miImp.addSelectionListener( lsImp );
					miImp.setEnabled(!userinfo.isReadonly());

					// Export transMeta
					MenuItem miTrans  = new MenuItem(mTree, SWT.PUSH); 
					miTrans.setText("&Export transformations");
					SelectionAdapter lsTrans = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { exportTransformations(); } };
					miTrans.addSelectionListener( lsTrans );

					// Export jobs
					MenuItem miJobs  = new MenuItem(mTree, SWT.PUSH); 
					miJobs.setText("&Export Jobs");
					SelectionAdapter lsJobs = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { exportJobs(); } };
					miJobs.addSelectionListener( lsJobs );
				}
				break;
				
			case ITEM_CATEGORY_DATABASES_ROOT              :
				{
					// New database
					MenuItem miNew  = new MenuItem(mTree, SWT.PUSH); 
					miNew.setText("&New database");
					SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newDatabase(); } };
					miNew.addSelectionListener( lsNew );
					miNew.setEnabled(!userinfo.isReadonly());
				}
				break;
				
			case ITEM_CATEGORY_DATABASE                    :
				{
					// New database
					MenuItem miNew  = new MenuItem(mTree, SWT.PUSH); 
					miNew.setText("&New database");
					SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newDatabase(); } };
					miNew.addSelectionListener( lsNew );
					miNew.setEnabled(!userinfo.isReadonly());
					// Edit database info
					MenuItem miEdit  = new MenuItem(mTree, SWT.PUSH); 
					miEdit.setText("&Edit database");
					SelectionAdapter lsEdit = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editDatabase(item); } };
					miEdit.addSelectionListener( lsEdit );
					miEdit.setEnabled(!userinfo.isReadonly());
					// Delete database info
					MenuItem miDel  = new MenuItem(mTree, SWT.PUSH); 
					miDel.setText("&Delete database");
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
					miOpen.setText("&Open transformation");
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
					miDel.setText("&Delete transformation");
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
					miRen.setText("&Rename transformation \tF2");
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
					miCreate.setText("&Create directory");
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
						miRename.setText("&Rename directory");
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
						miDelete.setText("&Delete directory");
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
					miDel.setText("&Delete job");
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
					miRen.setText("&Rename job \tF2");
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
					miNew.setText("&New user");
					SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newUser(); } };
					miNew.addSelectionListener( lsNew );
					miNew.setEnabled(!userinfo.isReadonly());
				}
				break;
				
			case ITEM_CATEGORY_USER                        :
				{
					// New user
					MenuItem miNew  = new MenuItem(mTree, SWT.PUSH); 
					miNew.setText("&New user");
					SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newUser(); } };
					miNew.addSelectionListener( lsNew );
					miNew.setEnabled(!userinfo.isReadonly());
					// Edit user info
					MenuItem miEdit  = new MenuItem(mTree, SWT.PUSH); 
					miEdit.setText("&Edit user");
					SelectionAdapter lsEdit = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editUser(item); } };
					miEdit.addSelectionListener( lsEdit );
					miEdit.setEnabled(!userinfo.isReadonly());
					// Rename user
					MenuItem miRen = new MenuItem(mTree, SWT.PUSH); 
					miRen.setText("&Rename user");
					SelectionAdapter lsRen = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { renameUser(); } };
					miRen.addSelectionListener( lsRen );
					miRen.setEnabled(!userinfo.isReadonly());
					// Delete user info
					MenuItem miDel  = new MenuItem(mTree, SWT.PUSH); 
					miDel.setText("&Delete user");
					SelectionAdapter lsDel = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { delUser(item); } };
					miDel.addSelectionListener( lsDel );
					miDel.setEnabled(!userinfo.isReadonly());
				}
				break;
				
			case ITEM_CATEGORY_PROFILES_ROOT               :
				{
					MenuItem miNew  = new MenuItem(mTree, SWT.PUSH); 
					miNew.setText("&New profile");
					SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newProfile(); } };
					miNew.addSelectionListener( lsNew );
					miNew.setEnabled(!userinfo.isReadonly());
				}
				break;
				
			case ITEM_CATEGORY_PROFILE                     :
				{
					// New profile
					MenuItem miNew  = new MenuItem(mTree, SWT.PUSH); 
					miNew.setText("&New profile");
					SelectionAdapter lsNew = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newProfile(); } };
					miNew.addSelectionListener( lsNew );
					miNew.setEnabled(!userinfo.isReadonly());
					// Edit profile info
					MenuItem miEdit  = new MenuItem(mTree, SWT.PUSH); 
					miEdit.setText("&Edit profile");
					SelectionAdapter lsEdit = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editProfile(item); } };
					miEdit.addSelectionListener( lsEdit );
					miEdit.setEnabled(!userinfo.isReadonly());
					// Rename profile
					MenuItem miRen = new MenuItem(mTree, SWT.PUSH); 
					miRen.setText("&Rename profile");
					SelectionAdapter lsRen = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { renameProfile(); } };
					miRen.addSelectionListener( lsRen );
					miRen.setEnabled(!userinfo.isReadonly());
					// Delete profile info
					MenuItem miDel  = new MenuItem(mTree, SWT.PUSH); 
					miDel.setText("&Delete profile");
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
				renameDatabase(); break;
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
			new ErrorDialog(shell, props, "Error", "An error occurred commiting the changes you made: ", e);
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
			tiTree.setText(rep.getName()==null?"-":rep.getName());
			
			TreeItem tiObj = new TreeItem(tiTree, SWT.NONE);
			tiObj.setText(STRING_OBJECTS);
	
			// The Databases...				
			TreeItem tiDB = new TreeItem(tiObj, SWT.NONE); 
			tiDB.setText(STRING_DATABASES);
	
			String dbs[] = rep.getDatabaseNames();			
	
			for (int i=0;i<dbs.length;i++)
			{
				TreeItem newDB = new TreeItem(tiDB, SWT.NONE);
				newDB.setText(dbs[i]);
			}
				
			tiDB.setExpanded(false);
			
			// The transformations...				
			if (userinfo.useTransformations())
			{
				TreeItem tiCat = new TreeItem(tiObj, SWT.NONE); 
				tiCat.setText(STRING_TRANSFORMATIONS);
				
				TreeItem newCat = new TreeItem(tiCat, SWT.NONE);
				rep.getDirectoryTree().getTreeWithNames(newCat, rep, dircolor, true, false, false);
				
				tiCat.setExpanded(true);
			}
			
			// The Jobs...				
			if (userinfo.useJobs())
			{
				TreeItem tiJob = new TreeItem(tiObj, SWT.NONE); 
				tiJob.setText(STRING_JOBS);
	
				TreeItem newJob = new TreeItem(tiJob, SWT.NONE);
				rep.getDirectoryTree().getTreeWithNames(newJob, rep, dircolor, false, true, false);
				
				tiJob.setExpanded(true);
	
			}
	
			// The schema's
			if (userinfo.useSchemas())
			{
				TreeItem tiSch = new TreeItem(tiObj, SWT.NONE); 
				tiSch.setText(STRING_SCHEMAS);
				
	/*				String schs[] = rep.getSchemaNames();			
		
					for (int i=0;i<jobs.length;i++)
					{
						TreeItem newJob = new TreeItem(tiJob, SWT.NONE);
						newJob.setText(jobs[i]);
					}
	*/
				tiSch.setExpanded(true);
			}
	
			tiObj.setExpanded(true);
	
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
					TreeItem newUser = new TreeItem(tiUser, SWT.NONE);
					newUser.setText(users[i]);
				}
			}
			tiUser.setExpanded(true);
	
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
				tiProf.setExpanded(true);
			}
	
			tiTree.setExpanded(true);
		}
		catch(KettleException dbe)
		{
			new ErrorDialog(shell, props, "Error", "An error occurred refreshing the repository tree: ", dbe);
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
		mb.setMessage("Are you sure you want to remove "+(tiSel.length>1?"these "+tiSel.length+" transformations?":"this transformation?"));
		mb.setText("Confirm delete...");
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
						new ErrorDialog(shell, props, "Error", "Kettle couldn't remove transformation ["+name+"]", dbe);
						error=true;
					}
				}
				else
				{
					mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					mb.setMessage("Sorry, couldn't find transformation ["+name+"]"+Const.CR+"It's probably already removed.");
					mb.setText("ERROR");
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
				new ErrorDialog(shell, props, "Error", "An error occurred deleting this transformation: ", dbe);
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
						// Focus is lost: connect to repository and change name.
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
				mb.setMessage("Sorry, couldn't find transformation ["+name+"]"+Const.CR+"It's probably already removed.");
				mb.setText("ERROR");
				mb.open();
			}
		}
		catch(KettleDatabaseException dbe)
		{
			new ErrorDialog(shell, props, "Error", "Kettle couldn't rename transformation ["+name+"]!", dbe);
		}
		
		return retval;
	}
	
    // TODO: add moveJob function too...
    //
	public boolean moveTransformation(String xml, RepositoryDirectory repdir)
	{
        debug = "Move transformation";
        
		boolean retval=false;
		
		try
		{
            debug = "parse xml";
			Document doc = XMLHandler.loadXMLString(xml);
			
			String dirname   = XMLHandler.getTagValue(doc, "dragdrop", "directory");
			String transname = XMLHandler.getTagValue(doc, "dragdrop", "transformation");
            
            if (dirname!=null && transname!=null)
            {
                debug = "dirname="+dirname+", transname="+transname;
    
    			// OK, find this transformation...
    			RepositoryDirectory fromdir = rep.getDirectoryTree().findDirectory(dirname);
    			if (fromdir!=null)
    			{
                    debug = "fromdir found: move transformation!";
    				rep.moveTransformation(transname, fromdir.getID(), repdir.getID());
    				retval=true;
    			}
    			else
    			{
    				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
    				mb.setMessage("Sorry, I couldn't find directory ["+dirname+"]"+Const.CR);
    				mb.setText("ERROR");
    				mb.open();
    			}
            }
		}
		catch(Exception dbe)
		{
			new ErrorDialog(shell, props, "Error", "Kettle couldn't move this transformation!", dbe);
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
						// Focus is lost: connect to repository and change name.
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
			if (!name.equals(newname))
			{
				long id = rep.getTransformationID(name, repdir.getID());
				if (id>0)
				{
					// System.out.println("Renaming job ["+name+"] with ID = "+id);
					rep.renameJob(id, newname);
					retval=true;
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				mb.setMessage("Sorry, couldn't find job ["+name+"]"+Const.CR+"It's probably already removed.");
				mb.setText("ERROR");
				mb.open();
			}
		}
		catch(KettleDatabaseException dbe)
		{
			new ErrorDialog(shell, props, "Error", "Kettle couldn't rename job ["+name+"]", dbe);
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
				mb.setMessage("Are you sure you want to remove this job?");
				mb.setText("["+name+"]");
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
				mb.setMessage("Sorry, couldn't find job ["+name+"]"+Const.CR+"It's probably already removed.");
				mb.setText("ERROR");
				mb.open();
				error=true;
			}
		}
		catch(KettleDatabaseException dbe)
		{
			new ErrorDialog(shell, props, "Error", "Kettle couldn't remove job ["+name+"]", dbe);
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
					// Focus is lost: connect to repository and change name.
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
					mb.setMessage("Sorry, couldn't find job ["+name+"]"+Const.CR+"It's probably already removed.");
					mb.setText("ERROR");
					mb.open();
				}								
			}
		}
		catch(KettleDatabaseException dbe)
		{
			new ErrorDialog(shell, props, "Error", "Kettle couldn't rename job ["+name+"]", dbe);
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
				mb.setMessage("Sorry, you are not allowed to change this user.");
				mb.setText("Edit user");
				mb.open();
			}
			if(ui!=null && !login.equalsIgnoreCase(ui.getLogin())) refreshTree();
			
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, props, "Error", "Sorry, I couldn't save user information to the repository.", e);
		}
	}

	public void newUser()
	{
		try
		{
			UserDialog ud = new UserDialog(shell, SWT.NONE, log, props, rep, new UserInfo());
			UserInfo ui = ud.open();
			if (ui!=null)
			{
				// See if this user already exists...
				long uid = rep.getUserID(ui.getLogin());
				if (uid<=0)
				{
					ui.saveRep(rep);
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					mb.setMessage("Sorry, this user already exists in the repository.");
					mb.setText("ERROR");
					mb.open();
				}
					
				// Refresh tree...
				refreshTree();
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, props, "Error", "Sorry, I couldn't save user information to the repository.", e);
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
			new ErrorDialog(shell, props, "Error", "Sorry, I couldn't delete the user information from the repository.", e);
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
						// Focus is lost: connect to repository and change name.
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
					mb.setMessage("Sorry, couldn't find user ["+name+"]"+Const.CR+"It's probably already removed or renamed.");
					mb.setText("ERROR");
					mb.open();
				}
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, props, "Error", "Sorry, I couldn't rename user ["+name+"]", e);
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
						// Focus is lost: connect to repository and change name.
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
					mb.setMessage("Sorry, couldn't find profile ["+name+"]"+Const.CR+"It's probably already removed or renamed.");
					mb.setText("ERROR");
					mb.open();
				}
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, props, "Error", "Sorry, I couldn't rename profile ["+name+"]", e);
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
					dbinfo.saveRep(rep);
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
					mb.setMessage("Sorry, you are not allowed to change this database connection.");
					mb.setText("Edit database");
					mb.open();
				}
				if(!databasename.equalsIgnoreCase(name)) refreshTree();
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, props, "Error", "Sorry, I couldn't change this database connection ["+databasename+"]", e);
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
					dbinfo.saveRep(rep);
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					mb.setMessage("Sorry, this database already exists in the repository.");
					mb.setText("ERROR");
					mb.open();
				}
					// Refresh tree...
				refreshTree();
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, props, "Error", "Sorry, I couldn't create the new database connection", e);
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
			new ErrorDialog(shell, props, "Error", "Sorry, I couldn't delete the database connection", e);
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
						// Focus is lost: connect to repository and change name.
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
							String newname = text.getText();
							if (renameDatabase(name, newname)) 
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
	
	public boolean renameDatabase(String name, String newname)
	{
		boolean retval=false;
		
		try
		{
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
					mb.setMessage("Sorry, couldn't find database connection ["+name+"]"+Const.CR+"It's probably already removed or renamed.");
					mb.setText("ERROR");
					mb.open();
				}
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, props, "Error", "Sorry, I couldn't rename the new database connection", e);
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
					// Focus is lost: connect to repository and change name.
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
		
		if (!name.equals(newname))
		{
			repdir.setDirectoryName(newname);
			if (!repdir.renameInRep(rep))
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				mb.setMessage("Kettle couldn't rename directory ["+name+"]"+Const.CR+"Please check the logs for more information.");
				mb.setText("ERROR");
				mb.open();
			}
			else
			{
				retval=true;
			}
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
			
			// System.out.print("Level="+level+", Tree: ");
			// for (int i=0;i<text.length;i++) System.out.print("/"+text[i]);		
			// System.out.println();
			
			if (level==0)
			{
				Const.flipExpanded(ti);
			}
			else
			if (level==1 && ti.getText().equalsIgnoreCase(STRING_OBJECTS))
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
			if (level==2 && ti.getText().equalsIgnoreCase(STRING_DATABASES))
			{
				if (!userinfo.isReadonly()) newDatabase();
			}
			else
			if (level==2 && parent.getText().equalsIgnoreCase(STRING_USERS))
			{
				editUser(ti.getText());
			}
			else
			if (level==2 && parent.getText().equalsIgnoreCase(STRING_PROFILES))
			{
				editProfile(ti.getText());
			}
			else
			if (level==3 && parent.getText().equalsIgnoreCase(STRING_DATABASES))
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
			new ErrorDialog(shell, props, "Error", "Sorry, I couldn't edit the profile.", e);
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
					mb.setMessage("Sorry, this database already exists in the repository.");
					mb.setText("ERROR");
					mb.open();
				}
					
				// Refresh tree...
				refreshTree();
			} 
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, props, "Error", "Sorry, I couldn't create a new profile", e);
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
			new ErrorDialog(shell, props, "Error", "Sorry, I couldn't delete the profile", e);
		}
	}
	
	public void createDirectory(TreeItem ti, RepositoryDirectory repdir)
	{
		EnterStringDialog esd = new EnterStringDialog(shell, props, "New directory", "New directory", "Specify the directory:");
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
					mb.setMessage("Sorry, I was unable to create directory ["+newdir+"] in directory ["+repdir.getPath()+"]");
					mb.setText("ERROR");
					mb.open();
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				mb.setMessage("Sorry, directory ["+newdir+"] in directory ["+repdir.getPath()+"] already exists!");
				mb.setText("ERROR");
				mb.open();
			}
		}
	}
	
	public void delDirectory(TreeItem ti, RepositoryDirectory repdir)
	{
		if (repdir.delFromRep(rep))
		{
			refreshTree();
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			mb.setMessage("Sorry, this directory could not be deleted. Perhaps there are still objects in it?"+Const.CR+"Please check the log for more information.");
			mb.setText("ERROR");
			mb.open();
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
						System.out.println("Loading/Exporting transformation ["+trans[i]+"] in directory ["+repdir.getPath()+"]");

						String xml = XMLHandler.getXMLHeader() + ti.getXML();
							
						// See if the directory exists...
						File dir = new File(directory+repdir.getPath());
						if (!dir.exists()) 
						{
							dir.mkdir();
							System.out.println("Created directory ["+dir.getName()+"]");
						}
						
						String filename = directory+repdir.getPath()+Const.FILE_SEPARATOR+trans[i]+".ktr";
						File f = new File(filename);
						try
						{
							FileOutputStream fos = new FileOutputStream(f);
							fos.write(xml.getBytes(Const.XML_ENCODING));
							fos.close();
						}
						catch(IOException e)
						{
							System.out.println("Couldn't create file ["+filename+"]");
						}
					}
				}
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, props, "Error", "Sorry, an error occurred exporting the transformations to XML:", e);
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
						System.out.println("Loading/Exporting job ["+jobs[i]+"] in directory ["+repdir.getPath()+"]");

						String xml = XMLHandler.getXMLHeader() + ji.getXML();
						
						// See if the directory exists...
						File dir = new File(directory+repdir.getPath());
						if (!dir.exists()) 
						{
							dir.mkdir();
							System.out.println("Created directory ["+dir.getName()+"]");
						}
						
						String filename = directory+repdir.getPath()+Const.FILE_SEPARATOR+jobs[i]+".kjb";
						File f = new File(filename);
						try
						{
							FileOutputStream fos = new FileOutputStream(f);
							fos.write(xml.getBytes(Const.XML_ENCODING));
							fos.close();
						}
						catch(IOException e)
						{
							System.out.println("Couldn't create file ["+filename+"]");
						}
					}
				}
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, props, "Error", "Sorry, an error occurred exporting the jobs to XML:", e);
		}
	}
	
	public void exportAll()
	{
		FileDialog dialog = new FileDialog(shell, SWT.SAVE | SWT.SINGLE);
		if (dialog.open()!=null)
		{
			String filename = dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName();
			System.out.println("Export objects to file ["+filename+"]");
			
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
			System.out.println("Import objects from XML file ["+filename+"]");
			
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
		
		String item = "";
		String parent = "";
		
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
			     if (item.equals(STRING_OBJECTS))  cat = ITEM_CATEGORY_OBJECTS;
			else if (item.equals(STRING_USERS))    cat = ITEM_CATEGORY_USERS_ROOT;
			else if (item.equals(STRING_PROFILES)) cat = ITEM_CATEGORY_PROFILES_ROOT;
		}
		else
		if (level==2)
		{
			if (parent.equals(STRING_OBJECTS))
			{
				     if (item.equals(STRING_DATABASES))       cat = ITEM_CATEGORY_DATABASES_ROOT;
				else if (item.equals(STRING_TRANSFORMATIONS)) cat = ITEM_CATEGORY_TRANSFORMATIONS_ROOT;
				else if (item.equals(STRING_JOBS))            cat = ITEM_CATEGORY_JOBS_ROOT;
				else if (item.equals(STRING_SCHEMAS))         cat = ITEM_CATEGORY_SCHEMAS_ROOT;
			}
			else 
			if (parent.equals(STRING_USERS)) cat = ITEM_CATEGORY_USER;
			else
			if (parent.equals(STRING_PROFILES)) cat = ITEM_CATEGORY_PROFILE;
		}
		else
		if (level>2)
		{
			if (parent.equals(STRING_DATABASES)) cat = ITEM_CATEGORY_DATABASE;
			else 
			if (path[2].equals(STRING_TRANSFORMATIONS))
			{
				if (ti.getForeground().equals(dircolor)) 
					 cat = ITEM_CATEGORY_TRANSFORMATION_DIRECTORY;
				else cat = ITEM_CATEGORY_TRANSFORMATION;
			}
			else
			if (path[2].equals(STRING_JOBS))
			{
				if (ti.getForeground().equals(dircolor)) 
					 cat = ITEM_CATEGORY_JOB_DIRECTORY;
				else cat = ITEM_CATEGORY_JOB;
			}
			else
			if (path[2].equals(STRING_SCHEMAS))
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
