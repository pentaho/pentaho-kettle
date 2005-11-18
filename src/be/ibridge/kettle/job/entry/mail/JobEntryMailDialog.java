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

package be.ibridge.kettle.job.entry.mail;
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
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * Dialog that allows you to edit a JobEntryMail object.
 * 
 * @author Matt
 * @since  19-06-2003
 */
public class JobEntryMailDialog extends Dialog implements JobEntryDialogInterface
{
	private Label        wlName;
	private Text         wName;
	private FormData     fdlName, fdName;

	private Label        wlDestination;
	private Text         wDestination;
	private FormData     fdlDestination, fdDestination;

	private Label        wlServer;
	private Text         wServer;
	private FormData     fdlServer, fdServer;

	private Label        wlReply;
	private Text         wReply;
	private FormData     fdlReply, fdReply;

	private Label        wlSubject;
	private Text         wSubject;
	private FormData     fdlSubject, fdSubject;

	private Label        wlAddDate;
	private Button       wAddDate;
	private FormData     fdlAddDate, fdAddDate;

	private Label        wlPerson;
	private Text         wPerson;
	private FormData     fdlPerson, fdPerson;

	private Label        wlPhone;
	private Text         wPhone;
	private FormData     fdlPhone, fdPhone;

	private Label        wlComment;
	private Text         wComment;
	private FormData     fdlComment, fdComment;



	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private Shell  shell;
	private SelectionAdapter lsDef;
	
	private JobEntryMail  jobmail;
	private boolean  backupDate, backupChanged;
	private Props    props;
	private Display  display;
	
	public JobEntryMailDialog(Shell parent, JobEntryMail jm, Repository rep)
	{
		super(parent, SWT.NONE);
		props=Props.getInstance();
		jobmail=jm;
	}

	public JobEntryInterface open()
	{
		Shell parent = getParent();
		display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
 		props.setLook(shell);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				jobmail.setChanged();
			}
		};
		backupChanged = jobmail.hasChanged();
		backupDate    = jobmail.getIncludeDate();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Job mail details:");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Name line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText("Name of mail job entry: ");
 		props.setLook(wlName);
		fdlName=new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.top  = new FormAttachment(0, 0);
		fdlName.right= new FormAttachment(middle, 0);
		wlName.setLayoutData(fdlName);

		wName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName=new FormData();
		fdName.top  = new FormAttachment(0, 0);
		fdName.left = new FormAttachment(middle, 0);
		fdName.right= new FormAttachment(100, 0);
		wName.setLayoutData(fdName);

		// Destination line
		wlDestination=new Label(shell, SWT.RIGHT);
		wlDestination.setText("Destination address: ");
 		props.setLook(wlDestination);
		fdlDestination=new FormData();
		fdlDestination.left = new FormAttachment(0, 0);
		fdlDestination.top  = new FormAttachment(wName, margin);
		fdlDestination.right= new FormAttachment(middle, 0);
		wlDestination.setLayoutData(fdlDestination);

		wDestination=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDestination);
		wDestination.addModifyListener(lsMod);
		fdDestination=new FormData();
		fdDestination.left = new FormAttachment(middle, 0);
		fdDestination.top  = new FormAttachment(wName, margin);
		fdDestination.right= new FormAttachment(100, 0);
		wDestination.setLayoutData(fdDestination);

		// Server line
		wlServer=new Label(shell, SWT.RIGHT);
		wlServer.setText("SMTP Server: ");
 		props.setLook(wlServer);
		fdlServer=new FormData();
		fdlServer.left = new FormAttachment(0, 0);
		fdlServer.top  = new FormAttachment(wDestination, margin);
		fdlServer.right= new FormAttachment(middle, 0);
		wlServer.setLayoutData(fdlServer);

		wServer=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wServer);
		wServer.addModifyListener(lsMod);
		fdServer=new FormData();
		fdServer.left = new FormAttachment(middle, 0);
		fdServer.top  = new FormAttachment(wDestination, margin);
		fdServer.right= new FormAttachment(100, 0);
		wServer.setLayoutData(fdServer);

		// Reply line
		wlReply=new Label(shell, SWT.RIGHT);
		wlReply.setText("Reply address: ");
 		props.setLook(wlReply);
		fdlReply=new FormData();
		fdlReply.left = new FormAttachment(0, 0);
		fdlReply.top  = new FormAttachment(wServer, margin);
		fdlReply.right= new FormAttachment(middle, 0);
		wlReply.setLayoutData(fdlReply);

		wReply=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wReply);
		wReply.addModifyListener(lsMod);
		fdReply=new FormData();
		fdReply.left = new FormAttachment(middle, 0);
		fdReply.top  = new FormAttachment(wServer, margin);
		fdReply.right= new FormAttachment(100, 0);
		wReply.setLayoutData(fdReply);

		// Subject line
		wlSubject=new Label(shell, SWT.RIGHT);
		wlSubject.setText("Subject: ");
 		props.setLook(wlSubject);
		fdlSubject=new FormData();
		fdlSubject.left = new FormAttachment(0, 0);
		fdlSubject.top  = new FormAttachment(wReply, margin);
		fdlSubject.right= new FormAttachment(middle, 0);
		wlSubject.setLayoutData(fdlSubject);

		wSubject=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wSubject);
		wSubject.addModifyListener(lsMod);
		fdSubject=new FormData();
		fdSubject.left = new FormAttachment(middle, 0);
		fdSubject.top  = new FormAttachment(wReply, margin);
		fdSubject.right= new FormAttachment(100, 0);
		wSubject.setLayoutData(fdSubject);

		// Add date to logfile name?
		wlAddDate=new Label(shell, SWT.RIGHT);
		wlAddDate.setText("Include date in message?");
 		props.setLook(wlAddDate);
		fdlAddDate=new FormData();
		fdlAddDate.left = new FormAttachment(0, 0);
		fdlAddDate.top  = new FormAttachment(wSubject, margin);
		fdlAddDate.right= new FormAttachment(middle, -margin);
		wlAddDate.setLayoutData(fdlAddDate);
		wAddDate=new Button(shell, SWT.CHECK);
 		props.setLook(wAddDate);
		fdAddDate=new FormData();
		fdAddDate.left = new FormAttachment(middle, 0);
		fdAddDate.top  = new FormAttachment(wSubject, margin);
		fdAddDate.right= new FormAttachment(100, 0);
		wAddDate.setLayoutData(fdAddDate);
		wAddDate.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobmail.setIncludeDate(jobmail.getIncludeDate());
					jobmail.setChanged();
				}
			}
		);

		// Person line
		wlPerson=new Label(shell, SWT.RIGHT);
		wlPerson.setText("Contact person: ");
 		props.setLook(wlPerson);
		fdlPerson=new FormData();
		fdlPerson.left = new FormAttachment(0, 0);
		fdlPerson.top  = new FormAttachment(wAddDate, margin);
		fdlPerson.right= new FormAttachment(middle, 0);
		wlPerson.setLayoutData(fdlPerson);

		wPerson=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wPerson);
		wPerson.addModifyListener(lsMod);
		fdPerson=new FormData();
		fdPerson.left = new FormAttachment(middle, 0);
		fdPerson.top  = new FormAttachment(wAddDate, margin);
		fdPerson.right= new FormAttachment(100, 0);
		wPerson.setLayoutData(fdPerson);

		// Phone line
		wlPhone=new Label(shell, SWT.RIGHT);
		wlPhone.setText("Contact Phone: ");
 		props.setLook(wlPhone);
		fdlPhone=new FormData();
		fdlPhone.left = new FormAttachment(0, 0);
		fdlPhone.top  = new FormAttachment(wPerson, margin);
		fdlPhone.right= new FormAttachment(middle, 0);
		wlPhone.setLayoutData(fdlPhone);

		wPhone=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wPhone);
		wPhone.addModifyListener(lsMod);
		fdPhone=new FormData();
		fdPhone.left = new FormAttachment(middle, 0);
		fdPhone.top  = new FormAttachment(wPerson, margin);
		fdPhone.right= new FormAttachment(100, 0);
		wPhone.setLayoutData(fdPhone);

		// Comment line
		wlComment=new Label(shell, SWT.RIGHT);
		wlComment.setText("Comment: ");
 		props.setLook(wlComment);
		fdlComment=new FormData();
		fdlComment.left = new FormAttachment(0, 0);
		fdlComment.top  = new FormAttachment(wPhone, margin);
		fdlComment.right= new FormAttachment(middle, 0);
		wlComment.setLayoutData(fdlComment);

		wComment=new Text(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
 		props.setLook(wComment);
		wComment.addModifyListener(lsMod);
		fdComment=new FormData();
		fdComment.left   = new FormAttachment(middle, 0);
		fdComment.top    = new FormAttachment(wPhone, margin);
		fdComment.right  = new FormAttachment(100, 0);
		fdComment.bottom = new FormAttachment(100, -50);
		wComment.setLayoutData(fdComment);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wOK.addListener    (SWT.Selection, lsOK     );
		wCancel.addListener(SWT.Selection, lsCancel );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wName.addSelectionListener(lsDef);
		wServer.addSelectionListener(lsDef);
		wSubject.addSelectionListener(lsDef);
		wDestination.addSelectionListener(lsDef);
		wReply.addSelectionListener(lsDef);
		wPerson.addSelectionListener(lsDef);
		wPhone.addSelectionListener(lsDef);
		wComment.addSelectionListener(lsDef);

		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();
		
		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobmail;
	}

	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}
		
	public void getData()
	{
		if (jobmail.getName()!=null)     wName.setText(jobmail.getName());
		if (jobmail.getDestination()!=null) wDestination.setText(jobmail.getDestination());
		if (jobmail.getServer()!=null)      wServer.setText(jobmail.getServer());
		if (jobmail.getReplyAddress()!=null) wReply.setText(jobmail.getReplyAddress());
		if (jobmail.getSubject()!=null) wSubject.setText(jobmail.getSubject());
		if (jobmail.getContactPerson()!=null) wPerson.setText(jobmail.getContactPerson());
		if (jobmail.getContactPhone()!=null) wPhone.setText(jobmail.getContactPhone());
		if (jobmail.getComment()!=null) wComment.setText(jobmail.getComment());
		wAddDate.setSelection(jobmail.getIncludeDate());
	}
	
	private void cancel()
	{
		jobmail.setChanged(backupChanged);
		jobmail.setIncludeDate(backupDate);
		
		jobmail=null;
		dispose();
	}
	
	private void ok()
	{
		jobmail.setName( wName.getText() );
		jobmail.setDestination( wDestination.getText() );
		jobmail.setServer( wServer.getText() );
		jobmail.setReplyAddress( wReply.getText() );
		jobmail.setSubject( wSubject.getText() );
		jobmail.setContactPerson( wPerson.getText() );
		jobmail.setContactPhone( wPhone.getText() );
		jobmail.setComment( wComment.getText() );

		jobmail.setIncludeDate( wAddDate.getSelection() );

		dispose();
	}
}
