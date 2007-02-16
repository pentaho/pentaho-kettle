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
import be.ibridge.kettle.job.dialog.JobDialog;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.textfileinput.VariableButtonListenerFactory;

/**
 * Dialog that allows you to edit a JobEntryMail object.
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class JobEntryMailDialog extends Dialog implements JobEntryDialogInterface
{
    private LabelText wName;

    private FormData fdName;

    private LabelTextVar wDestination;

	private LabelTextVar wDestinationCc;

	private LabelTextVar wDestinationBCc;

    private FormData fdDestination;

	private FormData fdDestinationCc;

	private FormData fdDestinationBCc;

    private LabelTextVar wServer;

    private FormData fdServer;

    private LabelTextVar wPort;

    private FormData fdPort;

    private Label wlUseAuth;

    private Button wUseAuth;

    private FormData fdlUseAuth, fdUseAuth;

    private Label wlUseSecAuth;

    private Button wUseSecAuth;

    private FormData fdlUseSecAuth, fdUseSecAuth;

    private LabelTextVar wAuthUser;

    private FormData fdAuthUser;

    private LabelTextVar wAuthPass;

    private FormData fdAuthPass;

    private LabelTextVar wReply;

    private FormData fdReply;

    private LabelTextVar wSubject;

    private FormData fdSubject;

    private Label wlAddDate;

    private Button wAddDate;

    private FormData fdlAddDate, fdAddDate;

    private Label wlIncludeFiles;

    private Button wIncludeFiles;

    private FormData fdlIncludeFiles, fdIncludeFiles;

    private Label wlTypes;

    private List wTypes;

    private FormData fdlTypes, fdTypes;

    private Label wlZipFiles;

    private Button wZipFiles;

    private FormData fdlZipFiles, fdZipFiles;

    private LabelTextVar wZipFilename;

    private FormData fdZipFilename;

    private LabelTextVar wPerson;

    private FormData fdPerson;

    private LabelTextVar wPhone;

    private FormData fdPhone;

    private Label wlComment;

    private Text wComment;

    private FormData fdlComment, fdComment;

    private Label wlOnlyComment;

    private Button wOnlyComment;

    private FormData fdlOnlyComment, fdOnlyComment;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private Shell shell;

    private SelectionAdapter lsDef;

    private JobEntryMail jobEntry;

    private boolean backupDate, backupChanged;

    private Props props;

    private Display display;

    public JobEntryMailDialog(Shell parent, JobEntryMail jobEntry)
    {
        super(parent, SWT.NONE);
        props = Props.getInstance();
        this.jobEntry = jobEntry;
    }

    public JobEntryInterface open()
    {
        Shell parent = getParent();
        display = parent.getDisplay();

        shell = new Shell(parent, props.getJobsDialogStyle());
        props.setLook(shell);
        JobDialog.setShellImage(shell, jobEntry);

        ModifyListener lsMod = new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                jobEntry.setChanged();
            }
        };
        backupChanged = jobEntry.hasChanged();
        backupDate = jobEntry.getIncludeDate();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(Messages.getString("JobMail.Header"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Name line
        wName = new LabelText(shell, Messages.getString("JobMail.NameOfEntry.Label"), Messages
            .getString("JobMail.NameOfEntry.Tooltip"));
        wName.addModifyListener(lsMod);
        fdName = new FormData();
        fdName.top = new FormAttachment(0, 0);
        fdName.left = new FormAttachment(0, 0);
        fdName.right = new FormAttachment(100, 0);
        wName.setLayoutData(fdName);

        // Destination line
        wDestination = new LabelTextVar(shell, Messages
            .getString("JobMail.DestinationAddress.Label"), Messages
            .getString("JobMail.DestinationAddress.Tooltip"));
        wDestination.addModifyListener(lsMod);
        fdDestination = new FormData();
        fdDestination.left = new FormAttachment(0, 0);
        fdDestination.top = new FormAttachment(wName, margin);
        fdDestination.right = new FormAttachment(100, 0);
        wDestination.setLayoutData(fdDestination);


		// Destination Cc
		wDestinationCc = new LabelTextVar(shell, Messages
			.getString("JobMail.DestinationAddressCc.Label"), Messages
			.getString("JobMail.DestinationAddressCc.Tooltip"));
		wDestinationCc.addModifyListener(lsMod);
		fdDestinationCc = new FormData();
		fdDestinationCc.left = new FormAttachment(0, 0);
		fdDestinationCc.top = new FormAttachment(wDestination, margin);
		fdDestinationCc.right = new FormAttachment(100, 0);
		wDestinationCc.setLayoutData(fdDestinationCc);

		// Destination BCc
		wDestinationBCc = new LabelTextVar(shell, Messages
			.getString("JobMail.DestinationAddressBCc.Label"), Messages
			.getString("JobMail.DestinationAddressBCc.Tooltip"));
		wDestinationBCc.addModifyListener(lsMod);
		fdDestinationBCc = new FormData();
		fdDestinationBCc.left = new FormAttachment(0, 0);
		fdDestinationBCc.top = new FormAttachment(wDestinationCc, margin);
		fdDestinationBCc.right = new FormAttachment(100, 0);
		wDestinationBCc.setLayoutData(fdDestinationBCc);


        // Server line
        wServer = new LabelTextVar(shell, Messages.getString("JobMail.SMTPServer.Label"), Messages
            .getString("JobMail.SMTPServer.Tooltip"));
        wServer.addModifyListener(lsMod);
        fdServer = new FormData();
        fdServer.left = new FormAttachment(0, 0);
        fdServer.top = new FormAttachment(wDestinationBCc, margin);
        fdServer.right = new FormAttachment(100, 0);
        wServer.setLayoutData(fdServer);

        // Port line
        wPort = new LabelTextVar(shell, Messages.getString("JobMail.Port.Label"), Messages
            .getString("JobMail.Port.Tooltip"));
        wPort.addModifyListener(lsMod);
        fdPort = new FormData();
        fdPort.left = new FormAttachment(0, 0);
        fdPort.top = new FormAttachment(wServer, margin);
        fdPort.right = new FormAttachment(100, 0);
        wPort.setLayoutData(fdPort);

        // Include Files?
        wlUseAuth = new Label(shell, SWT.RIGHT);
        wlUseAuth.setText(Messages.getString("JobMail.UseAuthentication.Label"));
        props.setLook(wlUseAuth);
        fdlUseAuth = new FormData();
        fdlUseAuth.left = new FormAttachment(0, 0);
        fdlUseAuth.top = new FormAttachment(wPort, margin);
        fdlUseAuth.right = new FormAttachment(middle, -margin);
        wlUseAuth.setLayoutData(fdlUseAuth);
        wUseAuth = new Button(shell, SWT.CHECK);
        props.setLook(wUseAuth);
        fdUseAuth = new FormData();
        fdUseAuth.left = new FormAttachment(middle, margin);
        fdUseAuth.top = new FormAttachment(wPort, margin);
        fdUseAuth.right = new FormAttachment(100, 0);
        wUseAuth.setLayoutData(fdUseAuth);
        wUseAuth.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
                setFlags();
            }
        });

        // AuthUser line
        wAuthUser = new LabelTextVar(shell, Messages.getString("JobMail.AuthenticationUser.Label"),
            Messages.getString("JobMail.AuthenticationUser.Tooltip"));
        wAuthUser.addModifyListener(lsMod);
        fdAuthUser = new FormData();
        fdAuthUser.left = new FormAttachment(0, 0);
        fdAuthUser.top = new FormAttachment(wUseAuth, margin);
        fdAuthUser.right = new FormAttachment(100, 0);
        wAuthUser.setLayoutData(fdAuthUser);

        // AuthPass line
        wAuthPass = new LabelTextVar(shell, Messages
            .getString("JobMail.AuthenticationPassword.Label"), Messages
            .getString("JobMail.AuthenticationPassword.Tooltip"));
        wAuthPass.setEchoChar('*');
        wAuthPass.addModifyListener(lsMod);
        fdAuthPass = new FormData();
        fdAuthPass.left = new FormAttachment(0, 0);
        fdAuthPass.top = new FormAttachment(wAuthUser, margin);
        fdAuthPass.right = new FormAttachment(100, 0);
        wAuthPass.setLayoutData(fdAuthPass);

        // Use authentication?
        wlUseSecAuth = new Label(shell, SWT.RIGHT);
        wlUseSecAuth.setText(Messages.getString("JobMail.UseSecAuthentication.Label"));
        props.setLook(wlUseSecAuth);
        fdlUseSecAuth = new FormData();
        fdlUseSecAuth.left = new FormAttachment(0, 0);
        fdlUseSecAuth.top = new FormAttachment(wAuthPass, margin);
        fdlUseSecAuth.right = new FormAttachment(middle, -margin);
        wlUseSecAuth.setLayoutData(fdlUseSecAuth);
        wUseSecAuth = new Button(shell, SWT.CHECK);
        props.setLook(wUseSecAuth);
        fdUseSecAuth = new FormData();
        fdUseSecAuth.left = new FormAttachment(middle, margin);
        fdUseSecAuth.top = new FormAttachment(wAuthPass, margin);
        fdUseSecAuth.right = new FormAttachment(100, 0);
        wUseSecAuth.setLayoutData(fdUseSecAuth);
        wUseSecAuth.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
                setFlags();
            }
        });

        // Reply line
        wReply = new LabelTextVar(shell, Messages.getString("JobMail.ReplyAddress.Label"), Messages
            .getString("JobMail.ReplyAddress.Tooltip"));
        wReply.addModifyListener(lsMod);
        fdReply = new FormData();
        fdReply.left = new FormAttachment(0, 0);
        fdReply.top = new FormAttachment(wUseSecAuth, margin);
        fdReply.right = new FormAttachment(100, 0);
        wReply.setLayoutData(fdReply);

        // Subject line
        wSubject = new LabelTextVar(shell, Messages.getString("JobMail.Subject.Label"), Messages
            .getString("JobMail.Subject.Tooltip"));
        wSubject.addModifyListener(lsMod);
        fdSubject = new FormData();
        fdSubject.left = new FormAttachment(0, 0);
        fdSubject.top = new FormAttachment(wReply, margin);
        fdSubject.right = new FormAttachment(100, 0);
        wSubject.setLayoutData(fdSubject);

        // Add date to logfile name?
        wlAddDate = new Label(shell, SWT.RIGHT);
        wlAddDate.setText(Messages.getString("JobMail.IncludeDate.Label"));
        props.setLook(wlAddDate);
        fdlAddDate = new FormData();
        fdlAddDate.left = new FormAttachment(0, 0);
        fdlAddDate.top = new FormAttachment(wSubject, margin);
        fdlAddDate.right = new FormAttachment(middle, -margin);
        wlAddDate.setLayoutData(fdlAddDate);
        wAddDate = new Button(shell, SWT.CHECK);
        props.setLook(wAddDate);
        fdAddDate = new FormData();
        fdAddDate.left = new FormAttachment(middle, margin);
        fdAddDate.top = new FormAttachment(wSubject, margin);
        fdAddDate.right = new FormAttachment(100, 0);
        wAddDate.setLayoutData(fdAddDate);
        wAddDate.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });

        // Include Files?
        wlIncludeFiles = new Label(shell, SWT.RIGHT);
        wlIncludeFiles.setText(Messages.getString("JobMail.AttachFiles.Label"));
        props.setLook(wlIncludeFiles);
        fdlIncludeFiles = new FormData();
        fdlIncludeFiles.left = new FormAttachment(0, 0);
        fdlIncludeFiles.top = new FormAttachment(wAddDate, margin);
        fdlIncludeFiles.right = new FormAttachment(middle, -margin);
        wlIncludeFiles.setLayoutData(fdlIncludeFiles);
        wIncludeFiles = new Button(shell, SWT.CHECK);
        props.setLook(wIncludeFiles);
        fdIncludeFiles = new FormData();
        fdIncludeFiles.left = new FormAttachment(middle, margin);
        fdIncludeFiles.top = new FormAttachment(wAddDate, margin);
        fdIncludeFiles.right = new FormAttachment(100, 0);
        wIncludeFiles.setLayoutData(fdIncludeFiles);
        wIncludeFiles.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
                setFlags();
            }
        });

        // Include Files?
        wlTypes = new Label(shell, SWT.RIGHT);
        wlTypes.setText(Messages.getString("JobMail.SelectFileTypes.Label"));
        props.setLook(wlTypes);
        fdlTypes = new FormData();
        fdlTypes.left = new FormAttachment(0, 0);
        fdlTypes.top = new FormAttachment(wIncludeFiles, margin);
        fdlTypes.right = new FormAttachment(middle, -margin);
        wlTypes.setLayoutData(fdlTypes);
        wTypes = new List(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        props.setLook(wTypes);
        fdTypes = new FormData();
        fdTypes.left = new FormAttachment(middle, margin);
        fdTypes.top = new FormAttachment(wIncludeFiles, margin);
        fdTypes.bottom = new FormAttachment(wIncludeFiles, margin + 150);
        fdTypes.right = new FormAttachment(100, 0);
        wTypes.setLayoutData(fdTypes);
        for (int i = 0; i < ResultFile.getAllTypeDesc().length; i++)
        {
            wTypes.add(ResultFile.getAllTypeDesc()[i]);
        }

        // Zip Files?
        wlZipFiles = new Label(shell, SWT.RIGHT);
        wlZipFiles.setText(Messages.getString("JobMail.ZipFiles.Label"));
        props.setLook(wlZipFiles);
        fdlZipFiles = new FormData();
        fdlZipFiles.left = new FormAttachment(0, 0);
        fdlZipFiles.top = new FormAttachment(wTypes, margin);
        fdlZipFiles.right = new FormAttachment(middle, -margin);
        wlZipFiles.setLayoutData(fdlZipFiles);
        wZipFiles = new Button(shell, SWT.CHECK);
        props.setLook(wZipFiles);
        fdZipFiles = new FormData();
        fdZipFiles.left = new FormAttachment(middle, margin);
        fdZipFiles.top = new FormAttachment(wTypes, margin);
        fdZipFiles.right = new FormAttachment(100, 0);
        wZipFiles.setLayoutData(fdZipFiles);
        wZipFiles.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
                setFlags();
            }
        });

        // ZipFilename line
        wZipFilename = new LabelTextVar(shell, Messages.getString("JobMail.ZipFilename.Label"),
            Messages.getString("JobMail.ZipFilename.Tooltip"));
        wZipFilename.addModifyListener(lsMod);
        fdZipFilename = new FormData();
        fdZipFilename.left = new FormAttachment(0, 0);
        fdZipFilename.top = new FormAttachment(wZipFiles, margin);
        fdZipFilename.right = new FormAttachment(100, 0);
        wZipFilename.setLayoutData(fdZipFilename);

        // ZipFilename line
        wPerson = new LabelTextVar(shell, Messages.getString("JobMail.ContactPerson.Label"),
            Messages.getString("JobMail.ContactPerson.Tooltip"));
        wPerson.addModifyListener(lsMod);
        fdPerson = new FormData();
        fdPerson.left = new FormAttachment(0, 0);
        fdPerson.top = new FormAttachment(wZipFilename, margin);
        fdPerson.right = new FormAttachment(100, 0);
        wPerson.setLayoutData(fdPerson);

        // Phone line
        wPhone = new LabelTextVar(shell, Messages.getString("JobMail.ContactPhone.Label"), Messages
            .getString("JobMail.ContactPhone.Tooltip"));
        wPhone.addModifyListener(lsMod);
        fdPhone = new FormData();
        fdPhone.left = new FormAttachment(0, 0);
        fdPhone.top = new FormAttachment(wPerson, margin);
        fdPhone.right = new FormAttachment(100, 0);
        wPhone.setLayoutData(fdPhone);

        // Some buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);

        // Only send the comment in the mail body
        wlOnlyComment = new Label(shell, SWT.RIGHT);
        wlOnlyComment.setText(Messages.getString("JobMail.OnlyCommentInBody.Label"));
        props.setLook(wlOnlyComment);
        fdlOnlyComment = new FormData();
        fdlOnlyComment.left = new FormAttachment(0, 0);
        fdlOnlyComment.bottom = new FormAttachment(wOK, -margin * 2);
        fdlOnlyComment.right = new FormAttachment(middle, -margin);
        wlOnlyComment.setLayoutData(fdlOnlyComment);
        wOnlyComment = new Button(shell, SWT.CHECK);
        props.setLook(wOnlyComment);
        fdOnlyComment = new FormData();
        fdOnlyComment.left = new FormAttachment(middle, margin);
        fdOnlyComment.bottom = new FormAttachment(wOK, -margin * 2);
        fdOnlyComment.right = new FormAttachment(100, 0);
        wOnlyComment.setLayoutData(fdOnlyComment);
        wOnlyComment.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });

        // Comment line
        wlComment = new Label(shell, SWT.RIGHT);
        wlComment.setText(Messages.getString("JobMail.Comment.Label"));
        props.setLook(wlComment);
        fdlComment = new FormData();
        fdlComment.left = new FormAttachment(0, 0);
        fdlComment.top = new FormAttachment(wPhone, margin);
        fdlComment.right = new FormAttachment(middle, margin);
        wlComment.setLayoutData(fdlComment);

        wComment = new Text(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        props.setLook(wComment);
        wComment.addModifyListener(lsMod);
        fdComment = new FormData();
        fdComment.left = new FormAttachment(middle, margin);
        fdComment.top = new FormAttachment(wPhone, margin);
        fdComment.right = new FormAttachment(100, 0);
        fdComment.bottom = new FormAttachment(wOnlyComment, -margin);
        wComment.setLayoutData(fdComment);
        SelectionAdapter lsVar = VariableButtonListenerFactory.getSelectionAdapter(shell, wComment);
        wComment.addKeyListener(TextVar.getControlSpaceKeyListener(wComment, lsVar));

        // Add listeners
        lsCancel = new Listener()
        {
            public void handleEvent(Event e)
            {
                cancel();
            }
        };
        lsOK = new Listener()
        {
            public void handleEvent(Event e)
            {
                ok();
            }
        };

        wOK.addListener(SWT.Selection, lsOK);
        wCancel.addListener(SWT.Selection, lsCancel);

        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };
        wName.addSelectionListener(lsDef);
        wServer.addSelectionListener(lsDef);
        wSubject.addSelectionListener(lsDef);
        wDestination.addSelectionListener(lsDef);
		wDestinationCc.addSelectionListener(lsDef);
		wDestinationBCc.addSelectionListener(lsDef);
        wReply.addSelectionListener(lsDef);
        wPerson.addSelectionListener(lsDef);
        wPhone.addSelectionListener(lsDef);
        wZipFilename.addSelectionListener(lsDef);

        // Detect [X] or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        // BaseStepDialog.setTraverseOrder(new Control[] {wName, wDestination, wServer, wUseAuth,
        // wAuthUser, wAuthPass, wReply,
        // wSubject, wAddDate, wIncludeFiles, wTypes, wZipFiles, wZipFilename, wPerson, wPhone,
        // wComment, wOK, wCancel });

        getData();

        BaseStepDialog.setSize(shell);

        shell.open();
        props.setDialogSize(shell, "JobMailDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
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
        wUseSecAuth.setEnabled(wUseAuth.getSelection());
    }

    public void dispose()
    {
        WindowProperty winprop = new WindowProperty(shell);
        props.setScreen(winprop);
        shell.dispose();
    }

    public void getData()
    {
        if (jobEntry.getName() != null)
            wName.setText(jobEntry.getName());
        if (jobEntry.getDestination() != null)
            wDestination.setText(jobEntry.getDestination());
		if (jobEntry.getDestinationCc() != null)
			wDestinationCc.setText(jobEntry.getDestinationCc());
		if (jobEntry.getDestinationBCc() != null)
			wDestinationBCc.setText(jobEntry.getDestinationBCc());
        if (jobEntry.getServer() != null)
            wServer.setText(jobEntry.getServer());
        if (jobEntry.getPort() != null)
            wPort.setText(jobEntry.getPort());
        if (jobEntry.getReplyAddress() != null)
            wReply.setText(jobEntry.getReplyAddress());
        if (jobEntry.getSubject() != null)
            wSubject.setText(jobEntry.getSubject());
        if (jobEntry.getContactPerson() != null)
            wPerson.setText(jobEntry.getContactPerson());
        if (jobEntry.getContactPhone() != null)
            wPhone.setText(jobEntry.getContactPhone());
        if (jobEntry.getComment() != null)
            wComment.setText(jobEntry.getComment());

        wAddDate.setSelection(jobEntry.getIncludeDate());
        wIncludeFiles.setSelection(jobEntry.isIncludingFiles());

        if (jobEntry.getFileType() != null)
        {
            int types[] = jobEntry.getFileType();
            wTypes.setSelection(types);
        }

        wZipFiles.setSelection(jobEntry.isZipFiles());
        if (jobEntry.getZipFilename() != null)
            wZipFilename.setText(jobEntry.getZipFilename());

        wUseAuth.setSelection(jobEntry.isUsingAuthentication());
        wUseSecAuth.setSelection(jobEntry.isUsingSecureAuthentication());
        if (jobEntry.getAuthenticationUser() != null)
            wAuthUser.setText(jobEntry.getAuthenticationUser());
        if (jobEntry.getAuthenticationPassword() != null)
            wAuthPass.setText(jobEntry.getAuthenticationPassword());

        wOnlyComment.setSelection(jobEntry.isOnlySendComment());

        setFlags();
    }

    private void cancel()
    {
        jobEntry.setChanged(backupChanged);
        jobEntry.setIncludeDate(backupDate);

        jobEntry = null;
        dispose();
    }

    private void ok()
    {
        jobEntry.setName(wName.getText());
        jobEntry.setDestination(wDestination.getText());
		jobEntry.setDestinationCc(wDestinationCc.getText());
		jobEntry.setDestinationBCc(wDestinationBCc.getText());
        jobEntry.setServer(wServer.getText());
        jobEntry.setPort(wPort.getText());
        jobEntry.setReplyAddress(wReply.getText());
        jobEntry.setSubject(wSubject.getText());
        jobEntry.setContactPerson(wPerson.getText());
        jobEntry.setContactPhone(wPhone.getText());
        jobEntry.setComment(wComment.getText());

        jobEntry.setIncludeDate(wAddDate.getSelection());
        jobEntry.setIncludingFiles(wIncludeFiles.getSelection());
        jobEntry.setFileType(wTypes.getSelectionIndices());
        jobEntry.setZipFilename(wZipFilename.getText());
        jobEntry.setZipFiles(wZipFiles.getSelection());
        jobEntry.setAuthenticationUser(wAuthUser.getText());
        jobEntry.setAuthenticationPassword(wAuthPass.getText());
        jobEntry.setUsingAuthentication(wUseAuth.getSelection());
        jobEntry.setUsingSecureAuthentication(wUseSecAuth.getSelection());
        jobEntry.setOnlySendComment(wOnlyComment.getSelection());

        dispose();
    }
}
