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


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.GUIResource;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.dialog.EnterStringDialog;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;


/**
 * This dialog represents an explorer type of interface on a given database connection.
 * It shows the tables defined in the visible schemas or catalogs on that connection.
 * The interface also allows you to get all kinds of information on those tables.
 * 
 * @author Matt
 * @since 18-05-2003
 *
 */
public class SelectDirectoryDialog extends Dialog 
{
	private LogWriter log;
	private Props props;
	private Repository rep;
		
	private Shell     shell;
	private Tree      wTree;
	private TreeItem  tiTree;
	 
	private Button    wOK;
	private Button    wRefresh;
	private Button    wCancel;
	
	private RepositoryDirectory selection;
	
	private Color dircolor;
	
    /**
     * @deprecated
     */
	public SelectDirectoryDialog(Shell parent, Props props, int style, LogWriter log, Repository rep)
	{
		super(parent, style);
		this.props          = props;
		this.log            = log;
		this.rep            = rep;
		
		selection = null;
	}
    
    public SelectDirectoryDialog(Shell parent, int style, Repository rep)
    {
        super(parent, style);
        this.props          = Props.getInstance();
        this.log            = LogWriter.getInstance();
        this.rep            = rep;
        
        selection = null;
    }
	
	public RepositoryDirectory open() 
	{
		dircolor = GUIResource.getInstance().getColorDirectory();
		
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
		shell.setText("Directory Selection dialog");
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		shell.setLayout (formLayout);
 		
 		// Tree
 		wTree = new Tree(shell, SWT.SINGLE | SWT.BORDER );
 		props.setLook( 		wTree);
 				
		if (!getData()) return null;
 		
 		// Buttons
		wOK = new Button(shell, SWT.PUSH); 
		wOK.setText("  &OK  ");

		wRefresh = new Button(shell, SWT.PUSH); 
		wRefresh.setText("  &Refresh  ");
		
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");
		
		FormData fdTree      = new FormData(); 
		FormData fdOK        = new FormData();
		FormData fdRefresh   = new FormData();
		FormData fdCancel    = new FormData();

		int margin =  10;

		fdTree.left   = new FormAttachment(0, 0); // To the right of the label
		fdTree.top    = new FormAttachment(0, 0);
		fdTree.right  = new FormAttachment(100, 0);
		fdTree.bottom = new FormAttachment(100, -50);
		wTree.setLayoutData(fdTree);

		fdOK.left    = new FormAttachment(wTree, 0, SWT.CENTER); 
		fdOK.bottom  = new FormAttachment(100, -margin);
		wOK.setLayoutData(fdOK);

		fdRefresh.left    = new FormAttachment(wOK, 10); 
		fdRefresh.bottom  = new FormAttachment(100, -margin);
		wRefresh.setLayoutData(fdRefresh);

		fdCancel.left = new FormAttachment(wRefresh, 10); 
		fdCancel.bottom  = new FormAttachment(100, -margin);
		wCancel.setLayoutData(fdCancel);
	
		// Add listeners
		wCancel.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					log.logDebug("SelectDirectoryDialog", "CANCEL SelectDirectoryDialog");
					dispose();
				}
			}
		);

		// Add listeners
		wOK.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					handleOK();
				}
			}
		);
		
		wTree.addSelectionListener(new SelectionAdapter()
			{
				public void widgetDefaultSelected(SelectionEvent arg0)
				{
					handleOK();
				}
			}
		);
		
		wRefresh.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					getData();
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
			}
		);

		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();

		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) 
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return selection;
	}
	
	private boolean getData()
	{
		// Clear the tree top entry
		if (tiTree!=null && !tiTree.isDisposed()) tiTree.dispose();

		tiTree = new TreeItem(wTree, SWT.NONE);
		rep.getDirectoryTree().getDirectoryTree(tiTree, dircolor);
		tiTree.setExpanded(true);
		
		return true;
	}
	
	public void setTreeMenu()
	{
		Menu mTree = null;
		
		TreeItem ti[]=wTree.getSelection();  // use SWT.SINGLE in wTree!!!!
		if (ti.length==1)
		{
			mTree = new Menu(wTree);
			
			/*
			 * NEW Sub-directory
			 */
			MenuItem miNew = new MenuItem(mTree, SWT.CASCADE);
			miNew.setText("New sub-directory");
			miNew.addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					if (!rep.getUserInfo().isReadonly())
					{
						TreeItem ti = wTree.getSelection()[0];
						String str[] = Const.getTreeStrings(ti);
						//
						// In which directory do we want create a subdirectory?
						//
						RepositoryDirectory dir = rep.getDirectoryTree().findDirectory(str);
						if (dir!=null)
						{
							//
							// What's the name of the new directory?
							//
							EnterStringDialog etd = new EnterStringDialog(shell, props, "New directory", "Enter the directory name", "New directory");
							String newdir = etd.open();
							if (newdir!=null)
							{
								RepositoryDirectory subdir = new RepositoryDirectory(dir, newdir);
								if (subdir.addToRep(rep))
								{
									dir.addSubdirectory(subdir);
									TreeItem tiNew = new TreeItem(ti, SWT.NONE);
									tiNew.setText(newdir);
									wTree.setSelection(new TreeItem[] { tiNew });
								}
								else
								{
									MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
									mb.setMessage("Unable to create new directory in the repository!");
									mb.setText("ERROR");
									mb.open();
								}
							}
						}
						else
						{
							MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
							mb.setMessage("Couldn't locate this directory!");
							mb.setText("ERROR");
							mb.open();
						}
					}
					else
					{
						MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
						mb.setMessage("User ["+rep.getUserInfo().getLogin()+"] doesn't have the permissions to create directories!");
						mb.setText("ERROR");
						mb.open();
					}
				}
			});
			
			/*
			 * RENAME directory
			 */
			MenuItem miRen = new MenuItem(mTree, SWT.CASCADE);
			miRen.setText("Rename directory\tF2");
			MenuItem miDel = new MenuItem(mTree, SWT.CASCADE);
			miDel.setText("Delete directory\tDEL");
		}
		wTree.setMenu(mTree);
	}


	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public void handleOK()
	{
		TreeItem ti[]=wTree.getSelection();
		if (ti.length==1)
		{
			String tree[] = Const.getTreeStrings(ti[0]);
			selection = rep.getDirectoryTree().findDirectory(tree);
			dispose();
		}
	}
		
	public String toString()
	{
		return this.getClass().getName();
	}

}
