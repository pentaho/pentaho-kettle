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
 * Created on 19-jun-2003
 *
 */

package be.ibridge.kettle.job.entry.sftp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * This dialog allows you to edit the SQL job entry settings. (select the connection and the sql script to be executed)
 *  
 * @author Matt
 * @since  19-06-2003
 */
public class JobEntrySFTPDialog extends Dialog implements JobEntryDialogInterface
{
	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlServerName;
	private Text         wServerName;
	private FormData     fdlServerName, fdServerName;
	
	private Label        wlServerPort;
	private Text         wServerPort;
	private FormData     fdlServerPort, fdServerPort;
	
	private Label        wlUserName;
	private Text         wUserName;
	private FormData     fdlUserName, fdUserName;
	
	private Label        wlPassword;
	private Text         wPassword;
	private FormData     fdlPassword, fdPassword;
	
	private Label        wlScpDirectory;
	private Text         wScpDirectory;
	private FormData     fdlScpDirectory, fdScpDirectory;
	
	private Label        wlTargetDirectory;
	private Text         wTargetDirectory;
	private FormData     fdlTargetDirectory, fdTargetDirectory;
	
	private Label        wlWildcard;
	private Text         wWildcard;
	private FormData     fdlWildcard, fdWildcard;
	
	private Label        wlRemove;
	private Button       wRemove;
	private FormData     fdlRemove, fdRemove;
	
	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntrySFTP     jobentry;
	private Shell       	shell;
	private Props       	props;

	private SelectionAdapter lsDef;

	private boolean changed;
	
	public JobEntrySFTPDialog(Shell parent, JobEntrySFTP je, Repository rep, JobMeta ji)
	{
			super(parent, SWT.NONE);
			props=Props.getInstance();
			jobentry=je;
	
			if (jobentry.getName() == null) jobentry.setName("FTP Files");
	}

	public JobEntryInterface open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				jobentry.setChanged();
			}
		};
		changed = jobentry.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Get files by SCP");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText("Job entry name ");
 		props.setLook(wlName);
		fdlName=new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.right= new FormAttachment(middle, -margin);
		fdlName.top  = new FormAttachment(0, margin);
		wlName.setLayoutData(fdlName);
		wName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName=new FormData();
		fdName.left = new FormAttachment(middle, 0);
		fdName.top  = new FormAttachment(0, margin);
		fdName.right= new FormAttachment(100, 0);
		wName.setLayoutData(fdName);

		// ServerName line
		wlServerName=new Label(shell, SWT.RIGHT);
		wlServerName.setText("SFTP-server name (IP)");
 		props.setLook(wlServerName);
		fdlServerName=new FormData();
		fdlServerName.left = new FormAttachment(0, 0);
		fdlServerName.top  = new FormAttachment(wName, margin);
		fdlServerName.right= new FormAttachment(middle, -margin);
		wlServerName.setLayoutData(fdlServerName);
		wServerName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wServerName);
		wServerName.addModifyListener(lsMod);
		fdServerName=new FormData();
		fdServerName.left = new FormAttachment(middle, 0);
		fdServerName.top  = new FormAttachment(wName, margin);
		fdServerName.right= new FormAttachment(100, 0);
		wServerName.setLayoutData(fdServerName);
		
		// ServerPort line
		wlServerPort=new Label(shell, SWT.RIGHT);
		wlServerPort.setText("SFTP-server port (usually 22)");
 		props.setLook(wlServerPort);
		fdlServerPort=new FormData();
		fdlServerPort.left = new FormAttachment(0, 0);
		fdlServerPort.top  = new FormAttachment(wServerName, margin);
		fdlServerPort.right= new FormAttachment(middle, -margin);
		wlServerPort.setLayoutData(fdlServerPort);
		wServerPort=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wServerPort);
		wServerPort.addModifyListener(lsMod);
		fdServerPort=new FormData();
		fdServerPort.left = new FormAttachment(middle, 0);
		fdServerPort.top  = new FormAttachment(wServerName, margin);
		fdServerPort.right= new FormAttachment(100, 0);
		wServerPort.setLayoutData(fdServerPort);

		// UserName line
		wlUserName=new Label(shell, SWT.RIGHT);
		wlUserName.setText("Username");
 		props.setLook(wlUserName);
		fdlUserName=new FormData();
		fdlUserName.left = new FormAttachment(0, 0);
		fdlUserName.top  = new FormAttachment(wServerPort, margin);
		fdlUserName.right= new FormAttachment(middle, -margin);
		wlUserName.setLayoutData(fdlUserName);
		wUserName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wUserName);
		wUserName.addModifyListener(lsMod);
		fdUserName=new FormData();
		fdUserName.left = new FormAttachment(middle, 0);
		fdUserName.top  = new FormAttachment(wServerPort, margin);
		fdUserName.right= new FormAttachment(100, 0);
		wUserName.setLayoutData(fdUserName);

		// Password line
		wlPassword=new Label(shell, SWT.RIGHT);
		wlPassword.setText("Password");
 		props.setLook(wlPassword);
		fdlPassword=new FormData();
		fdlPassword.left = new FormAttachment(0, 0);
		fdlPassword.top  = new FormAttachment(wUserName, margin);
		fdlPassword.right= new FormAttachment(middle, -margin);
		wlPassword.setLayoutData(fdlPassword);
		wPassword=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wPassword);
        wPassword.setEchoChar('*');
        wPassword.addModifyListener(lsMod);
		fdPassword=new FormData();
		fdPassword.left = new FormAttachment(middle, 0);
		fdPassword.top  = new FormAttachment(wUserName, margin);
		fdPassword.right= new FormAttachment(100, 0);
		wPassword.setLayoutData(fdPassword);

		// FtpDirectory line
		wlScpDirectory=new Label(shell, SWT.RIGHT);
		wlScpDirectory.setText("Remote directory");
 		props.setLook(wlScpDirectory);
		fdlScpDirectory=new FormData();
		fdlScpDirectory.left = new FormAttachment(0, 0);
		fdlScpDirectory.top  = new FormAttachment(wPassword, margin);
		fdlScpDirectory.right= new FormAttachment(middle, -margin);
		wlScpDirectory.setLayoutData(fdlScpDirectory);
		wScpDirectory=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wScpDirectory);
		wScpDirectory.addModifyListener(lsMod);
		fdScpDirectory=new FormData();
		fdScpDirectory.left = new FormAttachment(middle, 0);
		fdScpDirectory.top  = new FormAttachment(wPassword, margin);
		fdScpDirectory.right= new FormAttachment(100, 0);
		wScpDirectory.setLayoutData(fdScpDirectory);

		// TargetDirectory line
		wlTargetDirectory=new Label(shell, SWT.RIGHT);
		wlTargetDirectory.setText("Target directory");
 		props.setLook(wlTargetDirectory);
		fdlTargetDirectory=new FormData();
		fdlTargetDirectory.left = new FormAttachment(0, 0);
		fdlTargetDirectory.top  = new FormAttachment(wScpDirectory, margin);
		fdlTargetDirectory.right= new FormAttachment(middle, -margin);
		wlTargetDirectory.setLayoutData(fdlTargetDirectory);
		wTargetDirectory=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTargetDirectory);
		wTargetDirectory.addModifyListener(lsMod);
		fdTargetDirectory=new FormData();
		fdTargetDirectory.left = new FormAttachment(middle, 0);
		fdTargetDirectory.top  = new FormAttachment(wScpDirectory, margin);
		fdTargetDirectory.right= new FormAttachment(100, 0);
		wTargetDirectory.setLayoutData(fdTargetDirectory);

		// Wildcard line
		wlWildcard=new Label(shell, SWT.RIGHT);
		wlWildcard.setText("Wildcard (regular expression)");
 		props.setLook(wlWildcard);
		fdlWildcard=new FormData();
		fdlWildcard.left = new FormAttachment(0, 0);
		fdlWildcard.top  = new FormAttachment(wTargetDirectory, margin);
		fdlWildcard.right= new FormAttachment(middle, -margin);
		wlWildcard.setLayoutData(fdlWildcard);
		wWildcard=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wWildcard);
		wWildcard.addModifyListener(lsMod);
		fdWildcard=new FormData();
		fdWildcard.left = new FormAttachment(middle, 0);
		fdWildcard.top  = new FormAttachment(wTargetDirectory, margin);
		fdWildcard.right= new FormAttachment(100, 0);
		wWildcard.setLayoutData(fdWildcard);

		// Remove files after retreival...
		wlRemove=new Label(shell, SWT.RIGHT);
		wlRemove.setText("Remove files after retreival? ");
 		props.setLook(wlRemove);
		fdlRemove=new FormData();
		fdlRemove.left = new FormAttachment(0, 0);
		fdlRemove.top  = new FormAttachment(wWildcard, margin);
		fdlRemove.right= new FormAttachment(middle, -margin);
		wlRemove.setLayoutData(fdlRemove);
		wRemove=new Button(shell, SWT.CHECK);
 		props.setLook(wRemove);
		fdRemove=new FormData();
		fdRemove.left = new FormAttachment(middle, 0);
		fdRemove.top  = new FormAttachment(wWildcard, margin);
		fdRemove.right= new FormAttachment(100, 0);
		wRemove.setLayoutData(fdRemove);

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wRemove);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wName.addSelectionListener( lsDef );
        wServerName.addSelectionListener( lsDef );
        wUserName.addSelectionListener( lsDef );
        wPassword.addSelectionListener( lsDef );
        wScpDirectory.addSelectionListener( lsDef );
        wTargetDirectory.addSelectionListener( lsDef );
        wWildcard.addSelectionListener( lsDef );
        			
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
				
		getData();
		
		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobentry;
	}

	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (jobentry.getName()    != null) wName.setText( jobentry.getName() );
		wName.selectAll();

		wServerName.setText(Const.NVL(jobentry.getServerName(), ""));
		wServerPort.setText(""+jobentry.getServerPort());
		wUserName.setText(Const.NVL(jobentry.getUserName(), ""));
		wPassword.setText(Const.NVL(jobentry.getPassword(), ""));
		wScpDirectory.setText(Const.NVL(jobentry.getScpDirectory(), ""));
		wTargetDirectory.setText(Const.NVL(jobentry.getTargetDirectory(), ""));
		wWildcard.setText(Const.NVL(jobentry.getWildcard(), ""));
		wRemove.setSelection(jobentry.getRemove());
	}
	
	private void cancel()
	{
		jobentry.setChanged(changed);
		jobentry=null;
		dispose();
	}
	
	private void ok()
	{
		jobentry.setName(wName.getText());
		jobentry.setServerName(wServerName.getText());
		jobentry.setServerPort(Const.toInt(wServerPort.getText(), 22));
		jobentry.setUserName(wUserName.getText());
		jobentry.setPassword(wPassword.getText());
		jobentry.setScpDirectory(wScpDirectory.getText());
		jobentry.setTargetDirectory(wTargetDirectory.getText());
		jobentry.setWildcard(wWildcard.getText());
		jobentry.setRemove(wRemove.getSelection());

		dispose();
	}

	public String toString()
	{
		return this.getClass().getName();
	}
	
	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return false;
	}

}
