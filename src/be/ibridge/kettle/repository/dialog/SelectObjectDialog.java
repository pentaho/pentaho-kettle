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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.GUIResource;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;


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
	private FormData fdOK, fdCancel;
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
		
	public SelectObjectDialog(Shell parent, Props props, Repository rep, boolean trans, boolean job, boolean schema)
	{
		super(parent, SWT.NONE);
		
		this.props          = props;
		this.rep            = rep;
		this.trans          = trans;
		this.job            = job;
		this.schema         = schema;
		
		shellText = "Select repository object";
		lineText = (trans?"Select the transformation:":
			                (job?"Select the job:":
			                	  (schema?"Select the schema:":
			                	  	      "Select the object"
			                	  )
			                )
					);
		objectName = null;
		objectDirectory = null;
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
		
 		props.setLook(wTree);
		fdTree=new FormData();
		fdTree.left = new FormAttachment(0, 0);
		fdTree.right= new FormAttachment(100, 0);
		fdTree.top  = new FormAttachment(wlTree, margin);
		fdTree.bottom= new FormAttachment(100, -30);
		wTree.setLayoutData(fdTree);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		fdOK=new FormData();
		wOK.setText("  &OK  ");
		fdOK.left       = new FormAttachment(33, 0);
		fdOK.bottom     = new FormAttachment(100, 0);

		wOK.setLayoutData(fdOK);
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		wOK.addListener    (SWT.Selection, lsOK     );
		
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");
		fdCancel=new FormData();
		fdCancel.left   = new FormAttachment(66, 0);
		fdCancel.bottom = new FormAttachment(100, 0);
		wCancel.setLayoutData(fdCancel);
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		wCancel.addListener(SWT.Selection, lsCancel );

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

		getData();
		
		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();

		shell.open();
		while (!shell.isDisposed())
		{
				if (!shell.getDisplay().readAndDispatch()) shell.getDisplay().sleep();
		}
		return objectName;
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public void getData()
	{
		Control children[] = wTree.getChildren();
		for (int i=0;i<children.length;i++) children[i].dispose();
		
		TreeItem ti = new TreeItem(wTree, SWT.NONE);
		ti.setExpanded(true);
		
        try
        {
            rep.getDirectoryTree().getTreeWithNames(ti, rep, dircolor, trans, job, schema);
        }
        catch(KettleDatabaseException e)
        {
            new ErrorDialog(shell, props, "Error constructing directory tree", "There was a database error while constructing the repository directory tree", e);
        }
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
						mb.setMessage("Sorry, I coudln't find the directory for this object.");
						mb.setText("Error!");
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
