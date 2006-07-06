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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.ResultFile;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.widget.LabelText;
import be.ibridge.kettle.core.widget.LabelTextVar;
import be.ibridge.kettle.core.widget.TextVar;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.textfileinput.VariableButtonListenerFactory;


/**
 * Dialog that allows you to edit a JobEntryMail object.
 * 
 * @author Matt
 * @since  19-06-2003
 */
public class JobEntryMailDialog extends Dialog implements JobEntryDialogInterface
{
	private LabelText    wName;
	private FormData     fdName;

	private LabelTextVar wDestination;
	private FormData     fdDestination;

	private LabelTextVar wServer;
	private FormData     fdServer;

    private Label        wlUseAuth;
    private Button       wUseAuth;
    private FormData     fdlUseAuth, fdUseAuth;

    private LabelTextVar wAuthUser;
    private FormData     fdAuthUser;

    private LabelTextVar wAuthPass;
    private FormData     fdAuthPass;

	private LabelTextVar wReply;
	private FormData     fdReply;

	private LabelTextVar wSubject;
	private FormData     fdSubject;

	private Label        wlAddDate;
	private Button       wAddDate;
	private FormData     fdlAddDate, fdAddDate;
	
	private Label        wlIncludeFiles;
	private Button       wIncludeFiles;
	private FormData     fdlIncludeFiles, fdIncludeFiles;

	private Label        wlTypes;
	private List         wTypes;
	private FormData     fdlTypes, fdTypes;

	private Label        wlZipFiles;
	private Button       wZipFiles;
	private FormData     fdlZipFiles, fdZipFiles;

	private LabelTextVar  wZipFilename;
	private FormData     fdZipFilename;
	
	private LabelTextVar wPerson;
	private FormData     fdPerson;

	private LabelTextVar wPhone;
	private FormData     fdPhone;

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
	
	public JobEntryMailDialog(Shell parent, JobEntryMail jm)
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
		shell.setText("Job mail details");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Name line
		wName=new LabelText(shell, "Name of mail job entry", "Name of mail job entry");
 		wName.addModifyListener(lsMod);
		fdName=new FormData();
		fdName.top  = new FormAttachment(0, 0);
		fdName.left = new FormAttachment(0, 0);
		fdName.right= new FormAttachment(100, 0);
		wName.setLayoutData(fdName);

		// Destination line
		wDestination=new LabelTextVar(shell, "Destination address", "Destination address");
		wDestination.addModifyListener(lsMod);
		fdDestination=new FormData();
		fdDestination.left = new FormAttachment(0, 0);
		fdDestination.top  = new FormAttachment(wName, margin);
		fdDestination.right= new FormAttachment(100, 0);
		wDestination.setLayoutData(fdDestination);

		// Server line
		wServer=new LabelTextVar(shell, "SMTP Server", "SMTP Server");
		wServer.addModifyListener(lsMod);
		fdServer=new FormData();
		fdServer.left = new FormAttachment(0, 0);
		fdServer.top  = new FormAttachment(wDestination, margin);
		fdServer.right= new FormAttachment(100, 0);
		wServer.setLayoutData(fdServer);

        
        // Include Files?
        wlUseAuth=new Label(shell, SWT.RIGHT);
        wlUseAuth.setText("Use authentication?");
        props.setLook(wlUseAuth);
        fdlUseAuth=new FormData();
        fdlUseAuth.left = new FormAttachment(0, 0);
        fdlUseAuth.top  = new FormAttachment(wServer, margin);
        fdlUseAuth.right= new FormAttachment(middle, -margin);
        wlUseAuth.setLayoutData(fdlUseAuth);
        wUseAuth=new Button(shell, SWT.CHECK);
        props.setLook(wUseAuth);
        fdUseAuth=new FormData();
        fdUseAuth.left = new FormAttachment(middle, margin);
        fdUseAuth.top  = new FormAttachment(wServer, margin);
        fdUseAuth.right= new FormAttachment(100, 0);
        wUseAuth.setLayoutData(fdUseAuth);
        wUseAuth.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent e) 
                {
                    jobmail.setUsingAuthentication(!jobmail.isUsingAuthentication());
                    jobmail.setChanged();
                    setFlags();
                }
            }
        );

        // AuthUser line
        wAuthUser=new LabelTextVar(shell, "Authentication user", "Authentication user");
        wAuthUser.addModifyListener(lsMod);
        fdAuthUser=new FormData();
        fdAuthUser.left = new FormAttachment(0, 0);
        fdAuthUser.top  = new FormAttachment(wUseAuth, margin);
        fdAuthUser.right= new FormAttachment(100, 0);
        wAuthUser.setLayoutData(fdAuthUser);

        // AuthPass line
        wAuthPass=new LabelTextVar(shell, "Authentication password", "Authentication password");
        wAuthPass.setEchoChar('*');
        wAuthPass.addModifyListener(lsMod);
        fdAuthPass=new FormData();
        fdAuthPass.left = new FormAttachment(0, 0);
        fdAuthPass.top  = new FormAttachment(wAuthUser, margin);
        fdAuthPass.right= new FormAttachment(100, 0);
        wAuthPass.setLayoutData(fdAuthPass);

		// Reply line
		wReply=new LabelTextVar(shell, "Reply address", "Reply address");
		wReply.addModifyListener(lsMod);
		fdReply=new FormData();
		fdReply.left = new FormAttachment(0, 0);
		fdReply.top  = new FormAttachment(wAuthPass, margin);
		fdReply.right= new FormAttachment(100, 0);
		wReply.setLayoutData(fdReply);

		// Subject line
		wSubject=new LabelTextVar(shell, "Subject", "Subject");
		wSubject.addModifyListener(lsMod);
		fdSubject=new FormData();
		fdSubject.left = new FormAttachment(0, 0);
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
		fdAddDate.left = new FormAttachment(middle, margin);
		fdAddDate.top  = new FormAttachment(wSubject, margin);
		fdAddDate.right= new FormAttachment(100, 0);
		wAddDate.setLayoutData(fdAddDate);
		wAddDate.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobmail.setIncludeDate(!jobmail.getIncludeDate());
					jobmail.setChanged();
				}
			}
		);
		
		// Include Files?
		wlIncludeFiles=new Label(shell, SWT.RIGHT);
		wlIncludeFiles.setText("Attach files to message?");
 		props.setLook(wlIncludeFiles);
		fdlIncludeFiles=new FormData();
		fdlIncludeFiles.left = new FormAttachment(0, 0);
		fdlIncludeFiles.top  = new FormAttachment(wAddDate, margin);
		fdlIncludeFiles.right= new FormAttachment(middle, -margin);
		wlIncludeFiles.setLayoutData(fdlIncludeFiles);
		wIncludeFiles=new Button(shell, SWT.CHECK);
 		props.setLook(wIncludeFiles);
		fdIncludeFiles=new FormData();
		fdIncludeFiles.left = new FormAttachment(middle, margin);
		fdIncludeFiles.top  = new FormAttachment(wAddDate, margin);
		fdIncludeFiles.right= new FormAttachment(100, 0);
		wIncludeFiles.setLayoutData(fdIncludeFiles);
		wIncludeFiles.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobmail.setIncludingFiles(!jobmail.isIncludingFiles());
					jobmail.setChanged();
					setFlags();
				}
			}
		);

		// Include Files?
		wlTypes=new Label(shell, SWT.RIGHT);
		wlTypes.setText("Select the result file types to attach");
 		props.setLook(wlTypes);
		fdlTypes=new FormData();
		fdlTypes.left = new FormAttachment(0, 0);
		fdlTypes.top  = new FormAttachment(wIncludeFiles, margin);
		fdlTypes.right= new FormAttachment(middle, -margin);
		wlTypes.setLayoutData(fdlTypes);
		wTypes=new List(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
 		props.setLook(wTypes);
		fdTypes=new FormData();
		fdTypes.left   = new FormAttachment(middle, margin);
		fdTypes.top    = new FormAttachment(wIncludeFiles, margin);
		fdTypes.bottom = new FormAttachment(wIncludeFiles, margin+150);
		fdTypes.right  = new FormAttachment(100, 0);
		wTypes.setLayoutData(fdTypes);
		for (int i=0;i<ResultFile.getAllTypeDesc().length;i++)
		{
			wTypes.add(ResultFile.getAllTypeDesc()[i]);
		}

		// Zip Files?
		wlZipFiles=new Label(shell, SWT.RIGHT);
		wlZipFiles.setText("Zip files into a single archive?");
 		props.setLook(wlZipFiles);
		fdlZipFiles=new FormData();
		fdlZipFiles.left = new FormAttachment(0, 0);
		fdlZipFiles.top  = new FormAttachment(wTypes, margin);
		fdlZipFiles.right= new FormAttachment(middle, -margin);
		wlZipFiles.setLayoutData(fdlZipFiles);
		wZipFiles=new Button(shell, SWT.CHECK);
 		props.setLook(wZipFiles);
		fdZipFiles=new FormData();
		fdZipFiles.left = new FormAttachment(middle, margin);
		fdZipFiles.top  = new FormAttachment(wTypes, margin);
		fdZipFiles.right= new FormAttachment(100, 0);
		wZipFiles.setLayoutData(fdZipFiles);
		wZipFiles.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobmail.setZipFiles(!jobmail.isZipFiles());
					jobmail.setChanged();
					setFlags();
				}
			}
		);
		
		// ZipFilename line
		wZipFilename=new LabelTextVar(shell, "The zip filename", "The zip filename");
 		wZipFilename.addModifyListener(lsMod);
		fdZipFilename=new FormData();
		fdZipFilename.left = new FormAttachment(0, 0);
		fdZipFilename.top  = new FormAttachment(wZipFiles, margin);
		fdZipFilename.right= new FormAttachment(100, 0);
		wZipFilename.setLayoutData(fdZipFilename);

		// ZipFilename line
		wPerson=new LabelTextVar(shell, "Contact person", "Contact person");
		wPerson.addModifyListener(lsMod);
		fdPerson=new FormData();
		fdPerson.left = new FormAttachment(0, 0);
		fdPerson.top  = new FormAttachment(wZipFilename, margin);
		fdPerson.right= new FormAttachment(100, 0);
		wPerson.setLayoutData(fdPerson);

		// Phone line
		wPhone=new LabelTextVar(shell, "Contact Phone", "Contact Phone");
		wPhone.addModifyListener(lsMod);
		fdPhone=new FormData();
		fdPhone.left = new FormAttachment(0, 0);
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
        fdlComment.right= new FormAttachment(middle, margin);
        wlComment.setLayoutData(fdlComment);

        wComment=new Text(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        props.setLook(wComment);
        wComment.addModifyListener(lsMod);
        fdComment=new FormData();
        fdComment.left   = new FormAttachment(middle, margin);
        fdComment.top    = new FormAttachment(wPhone, margin);
        fdComment.right  = new FormAttachment(100, 0);
        fdComment.bottom = new FormAttachment(100, -50);
        wComment.setLayoutData(fdComment);
        SelectionAdapter lsVar = VariableButtonListenerFactory.getSelectionAdapter(shell, wComment);
        wComment.addKeyListener(TextVar.getControlSpaceKeyListener(wComment, lsVar));
        
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wComment);

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
		wZipFilename.addSelectionListener(lsDef);

		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

        BaseStepDialog.setTraverseOrder(new Control[] {wName, wDestination, wServer, wUseAuth, wAuthUser, wAuthPass, wReply, 
                wSubject, wAddDate, wIncludeFiles, wTypes, wZipFiles, wZipFilename, wPerson, wPhone, wComment, wOK, wCancel });
                
		getData();
		
		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobmail;
	}

    protected void setFlags()
	{
		wlTypes.setEnabled(wIncludeFiles.getSelection());
		wTypes.setEnabled(wIncludeFiles.getSelection());
		wlZipFiles.setEnabled(wIncludeFiles.getSelection());
		wZipFiles.setEnabled(wIncludeFiles.getSelection());
		wZipFilename.setEnabled(wIncludeFiles.getSelection() && wZipFiles.getSelection());
        
        wAuthUser.setEnabled(wUseAuth.getSelection());
        wAuthPass.setEnabled(wUseAuth.getSelection());
	}

	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}
		
	public void getData()
	{
		if (jobmail.getName()!=null)          wName.setText(jobmail.getName());
		if (jobmail.getDestination()!=null)   wDestination.setText(jobmail.getDestination());
		if (jobmail.getServer()!=null)        wServer.setText(jobmail.getServer());
		if (jobmail.getReplyAddress()!=null)  wReply.setText(jobmail.getReplyAddress());
		if (jobmail.getSubject()!=null)       wSubject.setText(jobmail.getSubject());
		if (jobmail.getContactPerson()!=null) wPerson.setText(jobmail.getContactPerson());
		if (jobmail.getContactPhone()!=null)  wPhone.setText(jobmail.getContactPhone());
		if (jobmail.getComment()!=null)       wComment.setText(jobmail.getComment());
        
		wAddDate.setSelection(jobmail.getIncludeDate());
		wIncludeFiles.setSelection(jobmail.isIncludingFiles());
		
		if (jobmail.getFileType()!=null)
		{
			int types[] = jobmail.getFileType();
			wTypes.setSelection(types);
		}
		
		wZipFiles.setSelection(jobmail.isZipFiles());
		if (jobmail.getZipFilename()!=null) wZipFilename.setText(jobmail.getZipFilename());
        
        wUseAuth.setSelection(jobmail.isUsingAuthentication());
        if (jobmail.getAuthenticationUser()!=null)     wAuthUser.setText( jobmail.getAuthenticationUser() );
        if (jobmail.getAuthenticationPassword()!=null) wAuthPass.setText( jobmail.getAuthenticationPassword() );
        
		setFlags();
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
		jobmail.setIncludingFiles( wIncludeFiles.getSelection() );
		jobmail.setFileType(wTypes.getSelectionIndices());
		jobmail.setZipFilename( wZipFilename.getText());
        jobmail.setAuthenticationUser( wAuthUser.getText() );
        jobmail.setAuthenticationPassword( wAuthPass.getText() );
		dispose();
	}
}
