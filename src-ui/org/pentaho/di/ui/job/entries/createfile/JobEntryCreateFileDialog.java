/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 **                                                                   **
 **                                                                   **
 **********************************************************************/

package org.pentaho.di.ui.job.entries.createfile;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.createfile.JobEntryCreateFile;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit the Create File job entry settings.
 *
 * @author Sven Boden
 * @since  28-01-2007
 */
public class JobEntryCreateFileDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryCreateFile.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private static final String[] FILETYPES = new String[] {
           BaseMessages.getString(PKG, "JobCreateFile.Filetype.All") };
	
	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlFilename;
	private Button       wbFilename;
	private TextVar      wFilename;
	private FormData     fdlFilename, fdbFilename, fdFilename;
	
    private Label        wlAbortExists;
    private Button       wAbortExists;
    private FormData     fdlAbortExists, fdAbortExists;
    
    private Label wlAddFilenameToResult;
    private Button wAddFilenameToResult;
    private FormData fdlAddFilenameToResult, fdAddFilenameToResult;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntryCreateFile jobEntry;
	private Shell       	shell;

	private SelectionAdapter lsDef;

	private boolean changed;

    public JobEntryCreateFileDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryCreateFile) jobEntryInt;
		if (this.jobEntry.getName() == null) 
			this.jobEntry.setName(BaseMessages.getString(PKG, "JobCreateFile.Name.Default"));
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
		shell.setText(BaseMessages.getString(PKG, "JobCreateFile.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(BaseMessages.getString(PKG, "JobCreateFile.Name.Label"));
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

		// Filename line
		wlFilename=new Label(shell, SWT.RIGHT);
		wlFilename.setText(BaseMessages.getString(PKG, "JobCreateFile.Filename.Label"));
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(wName, margin);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFilename);
		wbFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(wName, 0);
		wbFilename.setLayoutData(fdbFilename);

		wFilename=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.top  = new FormAttachment(wName, margin);
		fdFilename.right= new FormAttachment(wbFilename, -margin);
		wFilename.setLayoutData(fdFilename);

		// Whenever something changes, set the tooltip to the expanded version:
		wFilename.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wFilename.setToolTipText(jobMeta.environmentSubstitute( wFilename.getText() ) );
				}
			}
		);

		wbFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					FileDialog dialog = new FileDialog(shell, SWT.SAVE);
					dialog.setFilterExtensions(new String[] {"*"});
					if (wFilename.getText()!=null)
					{
						dialog.setFileName(jobMeta.environmentSubstitute(wFilename.getText()) );
					}
					dialog.setFilterNames(FILETYPES);
					if (dialog.open()!=null)
					{
						wFilename.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
					}
				}
			}
		);

        wlAbortExists = new Label(shell, SWT.RIGHT);
        wlAbortExists.setText(BaseMessages.getString(PKG, "JobCreateFile.FailIfExists.Label"));
        props.setLook(wlAbortExists);
        fdlAbortExists = new FormData();
        fdlAbortExists.left = new FormAttachment(0, 0);
        fdlAbortExists.top = new FormAttachment(wFilename, margin);
        fdlAbortExists.right = new FormAttachment(middle, -margin);
        wlAbortExists.setLayoutData(fdlAbortExists);
        wAbortExists = new Button(shell, SWT.CHECK);
        props.setLook(wAbortExists);
        wAbortExists.setToolTipText(BaseMessages.getString(PKG, "JobCreateFile.FailIfExists.Tooltip"));
        fdAbortExists = new FormData();
        fdAbortExists.left = new FormAttachment(middle, 0);
        fdAbortExists.top = new FormAttachment(wFilename, margin);
        fdAbortExists.right = new FormAttachment(100, 0);
        wAbortExists.setLayoutData(fdAbortExists);
        wAbortExists.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });
        
        // Add filenames to result filenames...
        wlAddFilenameToResult = new Label(shell, SWT.RIGHT);
        wlAddFilenameToResult.setText(BaseMessages.getString(PKG, "JobCreateFile.AddFilenameToResult.Label"));
        props.setLook(wlAddFilenameToResult);
        fdlAddFilenameToResult = new FormData();
        fdlAddFilenameToResult.left = new FormAttachment(0, 0);
        fdlAddFilenameToResult.top = new FormAttachment(wAbortExists, margin);
        fdlAddFilenameToResult.right = new FormAttachment(middle, -margin);
        wlAddFilenameToResult.setLayoutData(fdlAddFilenameToResult);
        wAddFilenameToResult = new Button(shell, SWT.CHECK);
        wAddFilenameToResult.setToolTipText(BaseMessages.getString(PKG, "JobCreateFile.AddFilenameToResult.Tooltip"));
        props.setLook(wAddFilenameToResult);
        fdAddFilenameToResult = new FormData();
        fdAddFilenameToResult.left = new FormAttachment(middle, 0);
        fdAddFilenameToResult.top = new FormAttachment(wAbortExists, margin);
        fdAddFilenameToResult.right = new FormAttachment(100, 0);
        wAddFilenameToResult.setLayoutData(fdAddFilenameToResult);
        
		
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
        
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wAddFilenameToResult);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wName.addSelectionListener( lsDef );
		wFilename.addSelectionListener( lsDef );

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
		if (jobEntry.getFilename()!= null) wFilename.setText( jobEntry.getFilename() );
		wAbortExists.setSelection(jobEntry.isFailIfFileExists());
		wAddFilenameToResult.setSelection(jobEntry.isAddFilenameToResult());
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
		jobEntry.setFilename(wFilename.getText());
		jobEntry.setFailIfFileExists(wAbortExists.getSelection());
		jobEntry.setAddFilenameToResult(wAddFilenameToResult.getSelection());
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