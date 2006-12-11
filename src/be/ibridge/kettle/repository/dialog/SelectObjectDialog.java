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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.GUIResource;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * Allows the user to make a selection of an Object in the repository
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class SelectObjectDialog extends Dialog
{
	private Label        wlTree;
	private Tree         wTree;
    private FormData     fdlTree, fdTree;
		
	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private Shell  shell;
	private SelectionAdapter lsDef;
	
	private String shellText;
	private String lineText;
	private Props props;
	
	private boolean trans, job, schema;
	private Color dircolor;
	private Repository rep;

	private String 			    objectName;
	private RepositoryDirectory objectDirectory;
    private TreeColumn nameColumn;
    private TreeColumn userColumn;
    private TreeColumn changedColumn;
    private int sortColumn;
    private boolean ascending;
		
	public SelectObjectDialog(Shell parent, Props props, Repository rep, boolean trans, boolean job, boolean schema)
	{
		super(parent, SWT.NONE);
		
		this.props          = props;
		this.rep            = rep;
		this.trans          = trans;
		this.job            = job;
		this.schema         = schema;
		
		shellText = Messages.getString("SelectObjectDialog.Dialog.Main.Title"); //$NON-NLS-1$
		lineText = (trans?Messages.getString("SelectObjectDialog.Dialog.Trans.Title"): //$NON-NLS-1$
			                (job?Messages.getString("SelectObjectDialog.Dialog.Job.Title"): //$NON-NLS-1$
			                	  (schema?Messages.getString("SelectObjectDialog.Dialog.Schema.Title"): //$NON-NLS-1$
			                	  	      Messages.getString("SelectObjectDialog.Dialog.Object.Title") //$NON-NLS-1$
			                	  )
			                )
					);
		objectName = null;
		objectDirectory = null;
        
        sortColumn = 0;
        ascending = true;
	}
	
	public String open()
	{
		Shell parent = getParent();
        dircolor = GUIResource.getInstance().getColorDirectory();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.MIN | SWT.MAX );
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(shellText);
		
		int margin = Const.MARGIN;

		// From step line
		wlTree=new Label(shell, SWT.NONE);
		wlTree.setText(lineText);
 		props.setLook(wlTree);
		fdlTree=new FormData();
		fdlTree.left = new FormAttachment(0, 0);
		fdlTree.top  = new FormAttachment(0, margin);
		wlTree.setLayoutData(fdlTree);
		wTree=new Tree(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
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
        
        
        props.setLook(wTree);
		fdTree=new FormData();
		fdTree.left = new FormAttachment(0, 0);
		fdTree.right= new FormAttachment(100, 0);
		fdTree.top  = new FormAttachment(wlTree, margin);
		fdTree.bottom= new FormAttachment(100, -30);
		wTree.setLayoutData(fdTree);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		wOK.addListener    (SWT.Selection, lsOK     );
		
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		wCancel.addListener(SWT.Selection, lsCancel );

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel}, margin, null);
		// Add listeners
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wTree.addSelectionListener(lsDef);
		wTree.addKeyListener(new KeyAdapter() 
			{
				public void keyPressed(KeyEvent e) 
				{
					if (e.character == SWT.CR) ok();
				}
			}
		);
		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

        try
        {
            rep.refreshRepositoryDirectoryTree();
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("SelectObjectDialog.Dialog.ErrorRefreshingDirectoryTree.Title"), 
                    Messages.getString("SelectObjectDialog.Dialog.ErrorRefreshingDirectoryTree.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        getData();
		
		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!shell.getDisplay().readAndDispatch()) shell.getDisplay().sleep();
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

    
    private void refreshTree()
    {
        try
        {
            wTree.removeAll();
            
            TreeItem ti = new TreeItem(wTree, SWT.NONE);
            ti.setExpanded(true);
            
            rep.getDirectoryTree().getTreeWithNames(ti, rep, dircolor, trans, job, schema, sortColumn, ascending);
        }
        catch(KettleDatabaseException e)
        {
            new ErrorDialog(shell, Messages.getString("SelectObjectDialog.Dialog.UnexpectedError.Title"), Messages.getString("SelectObjectDialog.Dialog.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public void getData()
	{
		setSort(1);
	}
	
	private void cancel()
	{
		objectName=null;
		dispose();
	}
	
	private void ok()
	{
		// Something has to be selected!
		if (wTree.getSelectionCount()>0)
		{
			TreeItem ti = wTree.getSelection()[0];
			
			// No directory!
			if (!ti.getForeground().equals(dircolor))
			{
				int level = Const.getTreeLevel(ti);
				if (level>0)
				{
					String path[] = Const.getTreeStrings(ti.getParentItem());
					objectName = ti.getText();
					objectDirectory = rep.getDirectoryTree().findDirectory(path);
					
					if (objectDirectory!=null)
					{
						dispose();
					}
					else
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setMessage(Messages.getString("SelectObjectDialog.Dialog.DirectoryNotFound.Message")); //$NON-NLS-1$
						mb.setText(Messages.getString("SelectObjectDialog.Dialog.DirectoryNotFound.Title")); //$NON-NLS-1$
						mb.open();
					}
				}
			}
		}
	}
	
	public RepositoryDirectory getDirectory()
	{
		return objectDirectory;
	}
}
