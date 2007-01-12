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

package be.ibridge.kettle.job.entry.ftp;

import java.util.ArrayList;

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

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.widget.LabelText;
import be.ibridge.kettle.core.widget.LabelTextVar;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.dialog.JobDialog;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * This dialog allows you to edit the SQL job entry settings. (select the connection and the sql script to be executed)
 *  
 * @author Matt
 * @since  19-06-2003
 */
public class JobEntryFTPDialog extends Dialog implements JobEntryDialogInterface
{
	private LabelText    wName;
    private FormData     fdName;

	private LabelTextVar wServerName;
	private FormData     fdServerName;
	
	private LabelTextVar wUserName;
	private FormData     fdUserName;
	
	private LabelTextVar wPassword;
	private FormData     fdPassword;
	
	private LabelTextVar wFtpDirectory;
	private FormData     fdFtpDirectory;
	
	private LabelTextVar wTargetDirectory;
	private FormData     fdTargetDirectory;
	
	private LabelTextVar wWildcard;
	private FormData     fdWildcard;
	
	private Label        wlBinaryMode;
	private Button       wBinaryMode;
	private FormData     fdlBinaryMode, fdBinaryMode;
	
	private LabelTextVar wTimeout;
	private FormData     fdTimeout;
	
	private Label        wlRemove;
	private Button       wRemove;
	private FormData     fdlRemove, fdRemove;

    private Label        wlOnlyNew;
    private Button       wOnlyNew;
    private FormData     fdlOnlyNew, fdOnlyNew;

    private Label        wlActive;
    private Button       wActive;
    private FormData     fdlActive, fdActive;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntryFTP     jobEntry;
	private Shell       	shell;
	private Props       	props;

	private SelectionAdapter lsDef;

	private boolean changed;
	
	public JobEntryFTPDialog(Shell parent, JobEntryFTP jobEntry, JobMeta jobMeta)
	{
		super(parent, SWT.NONE);
		props=Props.getInstance();
		this.jobEntry=jobEntry;

		if (this.jobEntry.getName() == null) this.jobEntry.setName("FTP Files");
	}

	public JobEntryInterface open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        JobDialog.setShellImage(shell, jobEntry);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				jobEntry.setChanged();
			}
		};
		changed = jobEntry.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Get files by FTP");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Job entry name line
        wName=new LabelText(shell, "Name of the job entry", "The unique name of job entry");
        wName.addModifyListener(lsMod);
        fdName=new FormData();
        fdName.top  = new FormAttachment(0, 0);
        fdName.left = new FormAttachment(0, 0);
        fdName.right= new FormAttachment(100, 0);
        wName.setLayoutData(fdName);

		// ServerName line
		wServerName=new LabelTextVar(shell, "FTP-server name (IP)", "The FTP server name or IP address");
 		props.setLook(wServerName);
		wServerName.addModifyListener(lsMod);
		fdServerName=new FormData();
		fdServerName.left = new FormAttachment(0, 0);
		fdServerName.top  = new FormAttachment(wName, margin);
		fdServerName.right= new FormAttachment(100, 0);
		wServerName.setLayoutData(fdServerName);

		// UserName line
		wUserName=new LabelTextVar(shell, "Username", "Enter the FTP server username");
 		props.setLook(wUserName);
		wUserName.addModifyListener(lsMod);
		fdUserName=new FormData();
		fdUserName.left = new FormAttachment(0, 0);
		fdUserName.top  = new FormAttachment(wServerName, margin);
		fdUserName.right= new FormAttachment(100, 0);
		wUserName.setLayoutData(fdUserName);

		// Password line
		wPassword=new LabelTextVar(shell, "Password", "The FTP server password");
 		props.setLook(wPassword);
        wPassword.setEchoChar('*');
		wPassword.addModifyListener(lsMod);
		fdPassword=new FormData();
		fdPassword.left = new FormAttachment(0, 0);
		fdPassword.top  = new FormAttachment(wUserName, margin);
		fdPassword.right= new FormAttachment(100, 0);
		wPassword.setLayoutData(fdPassword);

        // OK, if the password contains a variable, we don't want to have the password hidden...
        wPassword.getTextWidget().addModifyListener(new ModifyListener()
            {
                public void modifyText(ModifyEvent e)
                {
                    checkPasswordVisible();
                }
            }
        );

        
		// FtpDirectory line
		wFtpDirectory=new LabelTextVar(shell, "Remote directory", "The directory on the FTP server");
 		props.setLook(wFtpDirectory);
		wFtpDirectory.addModifyListener(lsMod);
		fdFtpDirectory=new FormData();
		fdFtpDirectory.left = new FormAttachment(0, 0);
		fdFtpDirectory.top  = new FormAttachment(wPassword, margin);
		fdFtpDirectory.right= new FormAttachment(100, 0);
		wFtpDirectory.setLayoutData(fdFtpDirectory);

		// TargetDirectory line
		wTargetDirectory=new LabelTextVar(shell, "Target directory", "The target directory on the local server");
 		props.setLook(wTargetDirectory);
		wTargetDirectory.addModifyListener(lsMod);
		fdTargetDirectory=new FormData();
		fdTargetDirectory.left = new FormAttachment(0, 0);
		fdTargetDirectory.top  = new FormAttachment(wFtpDirectory, margin);
		fdTargetDirectory.right= new FormAttachment(100, 0);
		wTargetDirectory.setLayoutData(fdTargetDirectory);

		// Wildcard line
		wWildcard=new LabelTextVar(shell, "Wildcard (regular expression)", "Enter a regular expression to specify the filenames to retrieve.\nFor example .*\\.txt$ : get all text files.");
 		props.setLook(wWildcard);
		wWildcard.addModifyListener(lsMod);
		fdWildcard=new FormData();
		fdWildcard.left = new FormAttachment(0, 0);
		fdWildcard.top  = new FormAttachment(wTargetDirectory, margin);
		fdWildcard.right= new FormAttachment(100, 0);
		wWildcard.setLayoutData(fdWildcard);

		// Binary mode selection...
		wlBinaryMode=new Label(shell, SWT.RIGHT);
		wlBinaryMode.setText("Use binary mode? ");
 		props.setLook(wlBinaryMode);
		fdlBinaryMode=new FormData();
		fdlBinaryMode.left = new FormAttachment(0, 0);
		fdlBinaryMode.top  = new FormAttachment(wWildcard, margin);
		fdlBinaryMode.right= new FormAttachment(middle, 0);
		wlBinaryMode.setLayoutData(fdlBinaryMode);
		wBinaryMode=new Button(shell, SWT.CHECK);
 		props.setLook(wBinaryMode);
		fdBinaryMode=new FormData();
		fdBinaryMode.left = new FormAttachment(middle, margin);
		fdBinaryMode.top  = new FormAttachment(wWildcard, margin);
		fdBinaryMode.right= new FormAttachment(100, 0);
		wBinaryMode.setLayoutData(fdBinaryMode);

		// Timeout line
		wTimeout=new LabelTextVar(shell, "Timeout", "The timeout in seconds");
 		props.setLook(wTimeout);
		wTimeout.addModifyListener(lsMod);
		fdTimeout=new FormData();
		fdTimeout.left = new FormAttachment(0, 0);
		fdTimeout.top  = new FormAttachment(wlBinaryMode, margin);
		fdTimeout.right= new FormAttachment(100, 0);
		wTimeout.setLayoutData(fdTimeout);
		
		// Remove files after retrieval...
		wlRemove=new Label(shell, SWT.RIGHT);
		wlRemove.setText("Remove files after retrieval? ");
 		props.setLook(wlRemove);
		fdlRemove=new FormData();
		fdlRemove.left = new FormAttachment(0, 0);
		fdlRemove.top  = new FormAttachment(wTimeout, margin);
		fdlRemove.right= new FormAttachment(middle, 0);
		wlRemove.setLayoutData(fdlRemove);
		wRemove=new Button(shell, SWT.CHECK);
 		props.setLook(wRemove);
		fdRemove=new FormData();
		fdRemove.left = new FormAttachment(middle, margin);
		fdRemove.top  = new FormAttachment(wTimeout, margin);
		fdRemove.right= new FormAttachment(100, 0);
		wRemove.setLayoutData(fdRemove);

        // OnlyNew files after retrieval...
        wlOnlyNew=new Label(shell, SWT.RIGHT);
        wlOnlyNew.setText("Don't overwrite files");
        props.setLook(wlOnlyNew);
        fdlOnlyNew=new FormData();
        fdlOnlyNew.left = new FormAttachment(0, 0);
        fdlOnlyNew.top  = new FormAttachment(wRemove, margin);
        fdlOnlyNew.right= new FormAttachment(middle, 0);
        wlOnlyNew.setLayoutData(fdlOnlyNew);
        wOnlyNew=new Button(shell, SWT.CHECK);
        props.setLook(wOnlyNew);
        fdOnlyNew=new FormData();
        fdOnlyNew.left = new FormAttachment(middle, margin);
        fdOnlyNew.top  = new FormAttachment(wRemove, margin);
        fdOnlyNew.right= new FormAttachment(100, 0);
        wOnlyNew.setLayoutData(fdOnlyNew);

        // active connection?
        wlActive=new Label(shell, SWT.RIGHT);
        wlActive.setText("Use active FTP connection");
        props.setLook(wlActive);
        fdlActive=new FormData();
        fdlActive.left = new FormAttachment(0, 0);
        fdlActive.top  = new FormAttachment(wOnlyNew, margin);
        fdlActive.right= new FormAttachment(middle, 0);
        wlActive.setLayoutData(fdlActive);
        wActive=new Button(shell, SWT.CHECK);
        props.setLook(wActive);
        fdActive=new FormData();
        fdActive.left = new FormAttachment(middle, margin);
        fdActive.top  = new FormAttachment(wOnlyNew, margin);
        fdActive.right= new FormAttachment(100, 0);
        wActive.setLayoutData(fdActive);

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wActive);

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
        wFtpDirectory.addSelectionListener( lsDef );
        wTargetDirectory.addSelectionListener( lsDef );
        wFtpDirectory.addSelectionListener( lsDef );
        wWildcard.addSelectionListener( lsDef );
        wTimeout.addSelectionListener( lsDef );

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();

		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
	}
    
    public void checkPasswordVisible()
    {
        String password = wPassword.getText();
        java.util.List list = new ArrayList();
        StringUtil.getUsedVariables(password, list, true);
        if (list.size()==0)
        {
            wPassword.setEchoChar('*');
        }
        else
        {
            wPassword.setEchoChar('\0'); // Show it all...
        }
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
		if (jobEntry.getName()    != null) wName.setText( jobEntry.getName() );
		wName.getTextWidget().selectAll();

		wServerName.setText(Const.NVL(jobEntry.getServerName(), ""));
		wUserName.setText(Const.NVL(jobEntry.getUserName(), ""));
		wPassword.setText(Const.NVL(jobEntry.getPassword(), ""));
		wFtpDirectory.setText(Const.NVL(jobEntry.getFtpDirectory(), ""));
		wTargetDirectory.setText(Const.NVL(jobEntry.getTargetDirectory(), ""));
		wWildcard.setText(Const.NVL(jobEntry.getWildcard(), ""));
		wBinaryMode.setSelection(jobEntry.isBinaryMode());
		wTimeout.setText(""+jobEntry.getTimeout());
		wRemove.setSelection(jobEntry.getRemove());
        wOnlyNew.setSelection(jobEntry.isOnlyGettingNewFiles());
        wActive.setSelection(jobEntry.isActiveConnection());
	}

	private void cancel()
	{
		jobEntry.setChanged(changed);
		jobEntry=null;
		dispose();
	}

	private void ok()
	{
		jobEntry.setName(wName.getText());
		jobEntry.setServerName(wServerName.getText());
		jobEntry.setUserName(wUserName.getText());
		jobEntry.setPassword(wPassword.getText());
		jobEntry.setFtpDirectory(wFtpDirectory.getText());
		jobEntry.setTargetDirectory(wTargetDirectory.getText());
		jobEntry.setWildcard(wWildcard.getText());
		jobEntry.setBinaryMode(wBinaryMode.getSelection());
		jobEntry.setTimeout(Const.toInt(wTimeout.getText(), 10000));
		jobEntry.setRemove(wRemove.getSelection());
        jobEntry.setOnlyGettingNewFiles(wOnlyNew.getSelection());
        jobEntry.setActiveConnection(wActive.getSelection());

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
