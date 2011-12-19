/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

/*
 * Created on 19-jun-2003
 *
 */

package org.pentaho.di.ui.job.entries.fileexists;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.fileexists.JobEntryFileExists;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

/**
 * This dialog allows you to edit the SQL job entry settings. (select the
 * connection and the sql script to be executed)
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class JobEntryFileExistsDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryFileExists.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static final String[] EXTENSIONS = new String[] { "*.txt", "*.csv", "*" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private static final String[] FILETYPES = new String[] {
			BaseMessages.getString(PKG, "JobFileExists.Filetype.Text"), //$NON-NLS-1$
			BaseMessages.getString(PKG, "JobFileExists.Filetype.CSV"), //$NON-NLS-1$
			BaseMessages.getString(PKG, "JobFileExists.Filetype.All") }; //$NON-NLS-1$

	private Label wlName;

	private Text wName;

	private FormData fdlName, fdName;

	private Label wlFilename;

	private Button wbFilename;

	private TextVar wFilename;

	private FormData fdlFilename, fdbFilename, fdFilename;

	private Button wOK, wCancel;

	private Listener lsOK, lsCancel;

	private JobEntryFileExists jobEntry;

	private Shell shell;

	private SelectionAdapter lsDef;

	private boolean changed;

	public JobEntryFileExistsDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep,
			JobMeta jobMeta)
	{
		super(parent, jobEntryInt, rep, jobMeta);
		jobEntry = (JobEntryFileExists) jobEntryInt;
		if (this.jobEntry.getName() == null)
			this.jobEntry.setName(BaseMessages.getString(PKG, "JobFileExists.Name.Default")); //$NON-NLS-1$
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

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "JobFileExists.Title")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlName = new Label(shell, SWT.RIGHT);
		wlName.setText(BaseMessages.getString(PKG, "JobFileExists.Name.Label")); //$NON-NLS-1$
		props.setLook(wlName);
		fdlName = new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.right = new FormAttachment(middle, -margin);
		fdlName.top = new FormAttachment(0, margin);
		wlName.setLayoutData(fdlName);
		wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName = new FormData();
		fdName.left = new FormAttachment(middle, 0);
		fdName.top = new FormAttachment(0, margin);
		fdName.right = new FormAttachment(100, 0);
		wName.setLayoutData(fdName);

		// Filename line
		wlFilename = new Label(shell, SWT.RIGHT);
		wlFilename.setText(BaseMessages.getString(PKG, "JobFileExists.Filename.Label")); //$NON-NLS-1$
		props.setLook(wlFilename);
		fdlFilename = new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top = new FormAttachment(wName, margin);
		fdlFilename.right = new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbFilename = new Button(shell, SWT.PUSH | SWT.CENTER);
		props.setLook(wbFilename);
		wbFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse")); //$NON-NLS-1$
		fdbFilename = new FormData();
		fdbFilename.right = new FormAttachment(100, 0);
		fdbFilename.top = new FormAttachment(wName, 0);
		// fdbFilename.height = 22;
		wbFilename.setLayoutData(fdbFilename);

		wFilename = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename = new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.top = new FormAttachment(wName, margin);
		fdFilename.right = new FormAttachment(wbFilename, -margin);
		wFilename.setLayoutData(fdFilename);

		// Whenever something changes, set the tooltip to the expanded version:
		wFilename.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				wFilename.setToolTipText(jobMeta.environmentSubstitute(wFilename.getText()));
			}
		});

		wbFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					FileObject fileName = null;

					try
					{
						String curFile = wFilename.getText();
						
						if (curFile.trim().length()>0)
							fileName = KettleVFS.getInstance().getFileSystemManager().resolveFile(
									jobMeta.environmentSubstitute(wFilename.getText()));
						else
							fileName = KettleVFS.getInstance().getFileSystemManager().resolveFile(Const.getUserHomeDirectory());
							
					} catch (FileSystemException ex)
					{
						fileName = KettleVFS.getInstance().getFileSystemManager().resolveFile(Const.getUserHomeDirectory());
					}

         VfsFileChooserDialog vfsFileChooser = Spoon.getInstance().getVfsFileChooserDialog(fileName.getParent(), fileName);

					FileObject selected = vfsFileChooser.open(shell, null,
							EXTENSIONS, FILETYPES,
							VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE);
					wFilename.setText(selected != null ? selected.getURL().toString() : Const.EMPTY_STRING);
				} catch (FileSystemException ex)
				{
					ex.printStackTrace();
				}
			}
		});

		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		FormData fd = new FormData();
		fd.right = new FormAttachment(50, -10);
		fd.bottom = new FormAttachment(100, 0);
		fd.width = 100;
		wOK.setLayoutData(fd);

		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$
		fd = new FormData();
		fd.left = new FormAttachment(50, 10);
		fd.bottom = new FormAttachment(100, 0);
		fd.width = 100;
		wCancel.setLayoutData(fd);
		
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wFilename);
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

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);

		lsDef = new SelectionAdapter()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
				ok();
			}
		};

		wName.addSelectionListener(lsDef);
		wFilename.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter()
		{
			public void shellClosed(ShellEvent e)
			{
				cancel();
			}
		});

		getData();

		BaseStepDialog.setSize(shell);

		shell.open();
		props.setDialogSize(shell, "JobFileExistsDialogSize"); //$NON-NLS-1$
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
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
			wName.setText(jobEntry.getName());
		wName.selectAll();
		if (jobEntry.getFilename() != null)
			wFilename.setText(jobEntry.getFilename());
	}

	private void cancel()
	{
		jobEntry.setChanged(changed);
		jobEntry = null;
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
