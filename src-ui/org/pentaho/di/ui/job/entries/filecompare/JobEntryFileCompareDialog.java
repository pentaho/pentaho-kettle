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

package org.pentaho.di.ui.job.entries.filecompare;

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
import org.pentaho.di.job.entries.filecompare.JobEntryFileCompare;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit the File compare job entry settings.
 *
 * @author Sven Boden
 * @since  01-02-2007
 */
public class JobEntryFileCompareDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryFileCompare.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

   private static final String[] FILETYPES = new String[] {
           BaseMessages.getString(PKG, "JobFileCompare.Filetype.All") };

	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlFilename1;
	private Button       wbFilename1;
	private TextVar      wFilename1;
	private FormData     fdlFilename1, fdbFilename1, fdFilename1;

	private Label        wlFilename2;
	private Button       wbFilename2;
	private TextVar      wFilename2;
	private FormData     fdlFilename2, fdbFilename2, fdFilename2;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntryFileCompare jobEntry;
	private Shell       	shell;
    private Label        wlAddFilenameResult;
    private Button       wAddFilenameResult;
    private FormData     fdlAddFilenameResult, fdAddFilenameResult; 

	private SelectionAdapter lsDef;
	
	private boolean changed;

    public JobEntryFileCompareDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryFileCompare) jobEntryInt;
		if (this.jobEntry.getName() == null)
			this.jobEntry.setName(BaseMessages.getString(PKG, "JobFileCompare.Name.Default"));
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
		shell.setText(BaseMessages.getString(PKG, "JobFileCompare.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Name line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(BaseMessages.getString(PKG, "JobFileCompare.Name.Label"));
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

		// Filename 1 line
		wlFilename1=new Label(shell, SWT.RIGHT);
		wlFilename1.setText(BaseMessages.getString(PKG, "JobFileCompare.Filename1.Label"));
 		props.setLook(wlFilename1);
		fdlFilename1=new FormData();
		fdlFilename1.left = new FormAttachment(0, 0);
		fdlFilename1.top  = new FormAttachment(wName, margin);
		fdlFilename1.right= new FormAttachment(middle, -margin);
		wlFilename1.setLayoutData(fdlFilename1);
		wbFilename1=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFilename1);
		wbFilename1.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbFilename1=new FormData();
		fdbFilename1.right= new FormAttachment(100, 0);
		fdbFilename1.top  = new FormAttachment(wName, 0);
		wbFilename1.setLayoutData(fdbFilename1);
		wFilename1=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename1);
		wFilename1.addModifyListener(lsMod);
		fdFilename1=new FormData();
		fdFilename1.left = new FormAttachment(middle, 0);
		fdFilename1.top  = new FormAttachment(wName, margin);
		fdFilename1.right= new FormAttachment(wbFilename1, -margin);
		wFilename1.setLayoutData(fdFilename1);

		// Whenever something changes, set the tooltip to the expanded version:
		wFilename1.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wFilename1.setToolTipText(jobMeta.environmentSubstitute( wFilename1.getText() ) );
				}
			}
		);

		wbFilename1.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] {"*"});
					if (wFilename1.getText()!=null)
					{
						dialog.setFileName(jobMeta.environmentSubstitute(wFilename1.getText()) );
					}
					dialog.setFilterNames(FILETYPES);
					if (dialog.open()!=null)
					{
						wFilename1.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
					}
				}
			}
		);

		// Filename 2 line
		wlFilename2=new Label(shell, SWT.RIGHT);
		wlFilename2.setText(BaseMessages.getString(PKG, "JobFileCompare.Filename2.Label"));
 		props.setLook(wlFilename2);
		fdlFilename2=new FormData();
		fdlFilename2.left = new FormAttachment(0, 0);
		fdlFilename2.top  = new FormAttachment(wFilename1, margin);
		fdlFilename2.right= new FormAttachment(middle, -margin);
		wlFilename2.setLayoutData(fdlFilename2);
		wbFilename2=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFilename2);
		wbFilename2.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbFilename2=new FormData();
		fdbFilename2.right= new FormAttachment(100, 0);
		fdbFilename2.top  = new FormAttachment(wFilename1, 0);
		wbFilename2.setLayoutData(fdbFilename2);
		wFilename2=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename2);
		wFilename2.addModifyListener(lsMod);
		fdFilename2=new FormData();
		fdFilename2.left = new FormAttachment(middle, 0);
		fdFilename2.top  = new FormAttachment(wFilename1, margin);
		fdFilename2.right= new FormAttachment(wbFilename2, -margin);
		wFilename2.setLayoutData(fdFilename2);

		// Whenever something changes, set the tooltip to the expanded version:
		wFilename2.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wFilename2.setToolTipText(jobMeta.environmentSubstitute( wFilename2.getText() ) );
				}
			}
		);

		wbFilename2.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] {"*"});
					if (wFilename2.getText()!=null)
					{
						dialog.setFileName(jobMeta.environmentSubstitute(wFilename2.getText()) );
					}
					dialog.setFilterNames(FILETYPES);
					if (dialog.open()!=null)
					{
						wFilename2.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
					}
				}
			}
		);
		// Add filename to result filenames		
        wlAddFilenameResult = new Label(shell, SWT.RIGHT);
        wlAddFilenameResult.setText(BaseMessages.getString(PKG, "JobFileCompare.AddFilenameResult.Label"));
        props.setLook(wlAddFilenameResult);
        fdlAddFilenameResult = new FormData();
        fdlAddFilenameResult.left = new FormAttachment(0, 0);
        fdlAddFilenameResult.top = new FormAttachment(wbFilename2, margin);
        fdlAddFilenameResult.right = new FormAttachment(middle, -margin);
        wlAddFilenameResult.setLayoutData(fdlAddFilenameResult);
        wAddFilenameResult = new Button(shell, SWT.CHECK);
        props.setLook(wAddFilenameResult);
        wAddFilenameResult.setToolTipText(BaseMessages.getString(PKG, "JobFileCompare.AddFilenameResult.Tooltip"));
        fdAddFilenameResult = new FormData();
        fdAddFilenameResult.left = new FormAttachment(middle, 0);
        fdAddFilenameResult.top = new FormAttachment(wbFilename2, margin);
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
		wFilename1.addSelectionListener( lsDef );
		wFilename2.addSelectionListener( lsDef );

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
		if (jobEntry.getFilename1()!= null) wFilename1.setText( jobEntry.getFilename1() );
		if (jobEntry.getFilename2()!= null) wFilename2.setText( jobEntry.getFilename2() );
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
		jobEntry.setFilename1(wFilename1.getText());
		jobEntry.setFilename2(wFilename2.getText());
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