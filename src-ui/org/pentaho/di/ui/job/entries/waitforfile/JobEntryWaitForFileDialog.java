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

package org.pentaho.di.ui.job.entries.waitforfile;

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
import org.pentaho.di.job.entries.waitforfile.JobEntryWaitForFile;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit the Wait For File job entry settings.
 *
 * @author Sven Boden
 * @since  28-01-2007
 */
public class JobEntryWaitForFileDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryWaitForFile.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private static final String[] FILETYPES = new String[] {
           BaseMessages.getString(PKG, "JobWaitForFile.Filetype.All") };
	
	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlFilename;
	private Button       wbFilename;
	private TextVar      wFilename;
	private FormData     fdlFilename, fdbFilename, fdFilename;

    private Label        wlMaximumTimeout;
    private TextVar      wMaximumTimeout;
    private FormData     fdlMaximumTimeout, fdMaximumTimeout;

    private Label        wlCheckCycleTime;
    private TextVar      wCheckCycleTime;
    private FormData     fdlCheckCycleTime, fdCheckCycleTime;    
    
    private Label        wlSuccesOnTimeout;
    private Button       wSuccesOnTimeout;
    private FormData     fdlSuccesOnTimeout, fdSuccesOnTimeout;

    private Label        wlFileSizeCheck;
    private Button       wFileSizeCheck;
    private FormData     fdlFileSizeCheck, fdFileSizeCheck;   
    
    private Label        wlAddFilenameResult;
    private Button       wAddFilenameResult;
    private FormData     fdlAddFilenameResult, fdAddFilenameResult;   
    
	private Button       wOK, wCancel;
	private Listener     lsOK, lsCancel;

	private JobEntryWaitForFile jobEntry;
	private Shell       	shell;
	private SelectionAdapter lsDef;
	
	private boolean changed;

    public JobEntryWaitForFileDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryWaitForFile) jobEntryInt;
        if (this.jobEntry.getName() == null) 
			this.jobEntry.setName(BaseMessages.getString(PKG, "JobWaitForFile.Name.Default"));
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
		shell.setText(BaseMessages.getString(PKG, "JobWaitForFile.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(BaseMessages.getString(PKG, "JobWaitForFile.Name.Label"));
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
		wlFilename.setText(BaseMessages.getString(PKG, "JobWaitForFile.Filename.Label"));
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
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
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

        // Maximum timeout
        wlMaximumTimeout = new Label(shell, SWT.RIGHT);
        wlMaximumTimeout.setText(BaseMessages.getString(PKG, "JobWaitForFile.MaximumTimeout.Label"));
        props.setLook(wlMaximumTimeout);
        fdlMaximumTimeout = new FormData();
        fdlMaximumTimeout.left = new FormAttachment(0, 0);
        fdlMaximumTimeout.top = new FormAttachment(wFilename, margin);
        fdlMaximumTimeout.right = new FormAttachment(middle, -margin);
        wlMaximumTimeout.setLayoutData(fdlMaximumTimeout);
        wMaximumTimeout = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wMaximumTimeout);
        wMaximumTimeout.setToolTipText(BaseMessages.getString(PKG, "JobWaitForFile.MaximumTimeout.Tooltip"));
        wMaximumTimeout.addModifyListener(lsMod);
        fdMaximumTimeout = new FormData();
        fdMaximumTimeout.left = new FormAttachment(middle, 0);
        fdMaximumTimeout.top = new FormAttachment(wFilename, margin);
        fdMaximumTimeout.right = new FormAttachment(100, 0);
        wMaximumTimeout.setLayoutData(fdMaximumTimeout);

        // Cycle time
        wlCheckCycleTime = new Label(shell, SWT.RIGHT);
        wlCheckCycleTime.setText(BaseMessages.getString(PKG, "JobWaitForFile.CheckCycleTime.Label"));
        props.setLook(wlCheckCycleTime);
        fdlCheckCycleTime = new FormData();
        fdlCheckCycleTime.left = new FormAttachment(0, 0);
        fdlCheckCycleTime.top = new FormAttachment(wMaximumTimeout, margin);
        fdlCheckCycleTime.right = new FormAttachment(middle, -margin);
        wlCheckCycleTime.setLayoutData(fdlCheckCycleTime);
        wCheckCycleTime = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wCheckCycleTime);
        wCheckCycleTime.setToolTipText(BaseMessages.getString(PKG, "JobWaitForFile.CheckCycleTime.Tooltip"));
        wCheckCycleTime.addModifyListener(lsMod);
        fdCheckCycleTime = new FormData();
        fdCheckCycleTime.left = new FormAttachment(middle, 0);
        fdCheckCycleTime.top = new FormAttachment(wMaximumTimeout, margin);
        fdCheckCycleTime.right = new FormAttachment(100, 0);
        wCheckCycleTime.setLayoutData(fdCheckCycleTime);
	        
        // Success on timeout		
        wlSuccesOnTimeout = new Label(shell, SWT.RIGHT);
        wlSuccesOnTimeout.setText(BaseMessages.getString(PKG, "JobWaitForFile.SuccessOnTimeout.Label"));
        props.setLook(wlSuccesOnTimeout);
        fdlSuccesOnTimeout = new FormData();
        fdlSuccesOnTimeout.left = new FormAttachment(0, 0);
        fdlSuccesOnTimeout.top = new FormAttachment(wCheckCycleTime, margin);
        fdlSuccesOnTimeout.right = new FormAttachment(middle, -margin);
        wlSuccesOnTimeout.setLayoutData(fdlSuccesOnTimeout);
        wSuccesOnTimeout = new Button(shell, SWT.CHECK);
        props.setLook(wSuccesOnTimeout);
        wSuccesOnTimeout.setToolTipText(BaseMessages.getString(PKG, "JobWaitForFile.SuccessOnTimeout.Tooltip"));
        fdSuccesOnTimeout = new FormData();
        fdSuccesOnTimeout.left = new FormAttachment(middle, 0);
        fdSuccesOnTimeout.top = new FormAttachment(wCheckCycleTime, margin);
        fdSuccesOnTimeout.right = new FormAttachment(100, 0);
        wSuccesOnTimeout.setLayoutData(fdSuccesOnTimeout);
        wSuccesOnTimeout.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });

        // Check file size		
        wlFileSizeCheck = new Label(shell, SWT.RIGHT);
        wlFileSizeCheck.setText(BaseMessages.getString(PKG, "JobWaitForFile.FileSizeCheck.Label"));
        props.setLook(wlFileSizeCheck);
        fdlFileSizeCheck = new FormData();
        fdlFileSizeCheck.left = new FormAttachment(0, 0);
        fdlFileSizeCheck.top = new FormAttachment(wSuccesOnTimeout, margin);
        fdlFileSizeCheck.right = new FormAttachment(middle, -margin);
        wlFileSizeCheck.setLayoutData(fdlFileSizeCheck);
        wFileSizeCheck = new Button(shell, SWT.CHECK);
        props.setLook(wFileSizeCheck);
        wFileSizeCheck.setToolTipText(BaseMessages.getString(PKG, "JobWaitForFile.FileSizeCheck.Tooltip"));
        fdFileSizeCheck = new FormData();
        fdFileSizeCheck.left = new FormAttachment(middle, 0);
        fdFileSizeCheck.top = new FormAttachment(wSuccesOnTimeout, margin);
        fdFileSizeCheck.right = new FormAttachment(100, 0);
        wFileSizeCheck.setLayoutData(fdFileSizeCheck);
        wFileSizeCheck.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });        
        // Add filename to result filenames		
        wlAddFilenameResult = new Label(shell, SWT.RIGHT);
        wlAddFilenameResult.setText(BaseMessages.getString(PKG, "JobWaitForFile.AddFilenameResult.Label"));
        props.setLook(wlAddFilenameResult);
        fdlAddFilenameResult = new FormData();
        fdlAddFilenameResult.left = new FormAttachment(0, 0);
        fdlAddFilenameResult.top = new FormAttachment(wFileSizeCheck, margin);
        fdlAddFilenameResult.right = new FormAttachment(middle, -margin);
        wlAddFilenameResult.setLayoutData(fdlAddFilenameResult);
        wAddFilenameResult = new Button(shell, SWT.CHECK);
        props.setLook(wAddFilenameResult);
        wAddFilenameResult.setToolTipText(BaseMessages.getString(PKG, "JobWaitForFile.AddFilenameResult.Tooltip"));
        fdAddFilenameResult = new FormData();
        fdAddFilenameResult.left = new FormAttachment(middle, 0);
        fdAddFilenameResult.top = new FormAttachment(wFileSizeCheck, margin);
        fdAddFilenameResult.right = new FormAttachment(100, 0);
        wAddFilenameResult.setLayoutData(fdAddFilenameResult);
        wAddFilenameResult.addSelectionListener(new SelectionAdapter()
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

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wAddFilenameResult);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wName.addSelectionListener( lsDef );
		wFilename.addSelectionListener( lsDef );
		wMaximumTimeout.addSelectionListener(lsDef);
		wCheckCycleTime.addSelectionListener(lsDef);

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
		if (jobEntry.getName() != null) 
			wName.setText( jobEntry.getName() );
		wName.selectAll();

		wFilename.setText(Const.NVL(jobEntry.getFilename(), ""));
		wMaximumTimeout.setText(Const.NVL(jobEntry.getMaximumTimeout(), ""));
		wCheckCycleTime.setText(Const.NVL(jobEntry.getCheckCycleTime(), ""));
		wSuccesOnTimeout.setSelection(jobEntry.isSuccessOnTimeout());		
		wFileSizeCheck.setSelection(jobEntry.isFileSizeCheck());
		wAddFilenameResult.setSelection(jobEntry.isAddFilenameToResult());
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
		jobEntry.setMaximumTimeout(wMaximumTimeout.getText());
		jobEntry.setCheckCycleTime(wCheckCycleTime.getText());
		jobEntry.setSuccessOnTimeout(wSuccesOnTimeout.getSelection());		
		jobEntry.setFileSizeCheck(wFileSizeCheck.getSelection());
		jobEntry.setAddFilenameToResult(wAddFilenameResult.getSelection());
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