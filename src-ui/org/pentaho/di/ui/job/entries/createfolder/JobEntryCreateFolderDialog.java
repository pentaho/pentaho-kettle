 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 **                                                                   **
 **                                                                   **
 **********************************************************************/


package org.pentaho.di.ui.job.entries.createfolder;

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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.createfolder.JobEntryCreateFolder;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


/**
 * This dialog allows you to edit the Create Folder job entry settings.
 *
 * @author Sven/Samatar
 * @since  17-10-2007
 */
public class JobEntryCreateFolderDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryCreateFolder.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlFoldername;
	private Button       wbFoldername;
	private TextVar      wFoldername;
	private FormData     fdlFoldername, fdbFoldername, fdFoldername;
	
    private Label        wlAbortExists;
    private Button       wAbortExists;
    private FormData     fdlAbortExists, fdAbortExists;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntryCreateFolder jobEntry;
	private Shell       	shell;

	private SelectionAdapter lsDef;

	private boolean changed;

	public JobEntryCreateFolderDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {		
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryCreateFolder) jobEntryInt;
		if (this.jobEntry.getName() == null) 
			this.jobEntry.setName(BaseMessages.getString(PKG, "JobCreateFolder.Name.Default"));
	}

	public JobEntryInterface open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

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
		changed = jobEntry.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "JobCreateFolder.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Foldername line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(BaseMessages.getString(PKG, "JobCreateFolder.Name.Label"));
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

		// Foldername line
		wlFoldername=new Label(shell, SWT.RIGHT);
		wlFoldername.setText(BaseMessages.getString(PKG, "JobCreateFolder.Foldername.Label"));
 		props.setLook(wlFoldername);
		fdlFoldername=new FormData();
		fdlFoldername.left = new FormAttachment(0, 0);
		fdlFoldername.top  = new FormAttachment(wName, margin);
		fdlFoldername.right= new FormAttachment(middle, -margin);
		wlFoldername.setLayoutData(fdlFoldername);

		wbFoldername=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFoldername);
		wbFoldername.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbFoldername=new FormData();
		fdbFoldername.right= new FormAttachment(100, 0);
		fdbFoldername.top  = new FormAttachment(wName, 0);
		wbFoldername.setLayoutData(fdbFoldername);

		wFoldername=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFoldername);
		wFoldername.addModifyListener(lsMod);
		fdFoldername=new FormData();
		fdFoldername.left = new FormAttachment(middle, 0);
		fdFoldername.top  = new FormAttachment(wName, margin);
		fdFoldername.right= new FormAttachment(wbFoldername, -margin);
		wFoldername.setLayoutData(fdFoldername);

		// Whenever something changes, set the tooltip to the expanded version:
		wFoldername.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wFoldername.setToolTipText(jobMeta.environmentSubstitute( wFoldername.getText() ) );
				}
			}
		);

		wbFoldername.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wFoldername.getText()!=null)
					{
						dialog.setFilterPath(jobMeta.environmentSubstitute(wFoldername.getText()) );
					}
				
					String dir=dialog.open();
					if(dir!=null)
					{
						wFoldername.setText(dir);
					}
				}
			}
		);

        wlAbortExists = new Label(shell, SWT.RIGHT);
        wlAbortExists.setText(BaseMessages.getString(PKG, "JobCreateFolder.FailIfExists.Label"));
        props.setLook(wlAbortExists);
        fdlAbortExists = new FormData();
        fdlAbortExists.left = new FormAttachment(0, 0);
        fdlAbortExists.top = new FormAttachment(wFoldername, margin);
        fdlAbortExists.right = new FormAttachment(middle, -margin);
        wlAbortExists.setLayoutData(fdlAbortExists);
        wAbortExists = new Button(shell, SWT.CHECK);
        props.setLook(wAbortExists);
        wAbortExists.setToolTipText(BaseMessages.getString(PKG, "JobCreateFolder.FailIfExists.Tooltip"));
        fdAbortExists = new FormData();
        fdAbortExists.left = new FormAttachment(middle, 0);
        fdAbortExists.top = new FormAttachment(wFoldername, margin);
        fdAbortExists.right = new FormAttachment(100, 0);
        wAbortExists.setLayoutData(fdAbortExists);
        wAbortExists.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });
		
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
        
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wAbortExists);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wName.addSelectionListener( lsDef );
		wFoldername.addSelectionListener( lsDef );

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
		wName.selectAll();
		if (jobEntry.getFoldername()!= null) wFoldername.setText( jobEntry.getFoldername() );
		wAbortExists.setSelection(jobEntry.isFailOfFolderExists());
	}

	private void cancel()
	{
		jobEntry.setChanged(changed);
		jobEntry=null;
		dispose();
	}

	private void ok()
	{
	    if(Const.isEmpty(wName.getText())) 
        {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setText(BaseMessages.getString(PKG, "System.StepJobEntryNameMissing.Title"));
			mb.setMessage(BaseMessages.getString(PKG, "System.JobEntryNameMissing.Msg"));
			mb.open(); 
			return;
        }
		jobEntry.setName(wName.getText());
		jobEntry.setFoldername(wFoldername.getText());
		jobEntry.setFailOfFolderExists(wAbortExists.getSelection());
		dispose();
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