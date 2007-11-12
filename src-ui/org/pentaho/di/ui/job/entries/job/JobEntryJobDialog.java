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

package org.pentaho.di.ui.job.entries.job;

import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.VFS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.job.Messages;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.repository.dialog.SelectObjectDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

/**
 * This dialog allows you to edit the job job entry (JobEntryJob)
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class JobEntryJobDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static final String[] FILE_FILTERNAMES = new String[] {
			Messages.getString("JobJob.Fileformat.Kettle"), Messages.getString("JobJob.Fileformat.XML"),
			Messages.getString("JobJob.Fileformat.All") };

	private LogWriter log;

	private Label wlName;
	private Text wName;
	private FormData fdlName, fdName;

	private Label wlJobname;
	private Button wbJobname;
	private TextVar wJobname;
	private FormData fdlJobname, fdbJobname, fdJobname;

	private Label wlDirectory;
	private Text wDirectory;
	private FormData fdlDirectory, fdDirectory;

	private Label wlFilename;
	private Button wbFilename;
	private TextVar wFilename;

	private FormData fdlFilename, fdbFilename, fdFilename;
	private Group wLogging;
	private FormData fdLogging;

	private Label wlSetLogfile;
	private Button wSetLogfile;
	private FormData fdlSetLogfile, fdSetLogfile;

	private Label wlLogfile;
	private TextVar wLogfile;
	private FormData fdlLogfile, fdLogfile;

	private Label wlLogext;
	private TextVar wLogext;
	private FormData fdlLogext, fdLogext;

	private Label wlAddDate;
	private Button wAddDate;
	private FormData fdlAddDate, fdAddDate;

	private Label wlAddTime;
	private Button wAddTime;
	private FormData fdlAddTime, fdAddTime;

	private Label wlLoglevel;
	private CCombo wLoglevel;
	private FormData fdlLoglevel, fdLoglevel;

	private Label wlPrevious;
	private Button wPrevious;
	private FormData fdlPrevious, fdPrevious;

	private Label wlEveryRow;
	private Button wEveryRow;
	private FormData fdlEveryRow, fdEveryRow;

	private Label wlFields;
	private TableView wFields;
	private FormData fdlFields, fdFields;

	private Label wlSlaveServer;
	private CCombo wSlaveServer;
	private FormData fdlSlaveServer, fdSlaveServer;
	
	private Button wOK, wCancel;

	private Listener lsOK, lsCancel;

	private Shell shell;

	private SelectionAdapter lsDef;

	private JobEntryJob jobEntry;

	private boolean backupChanged;

	private Display display;

	public JobEntryJobDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
	{
		super(parent, jobEntryInt, rep, jobMeta);
		jobEntry = (JobEntryJob) jobEntryInt;
		this.log = LogWriter.getInstance();
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

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("JobJob.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Name line
		wlName = new Label(shell, SWT.RIGHT);
		wlName.setText(Messages.getString("JobJob.Name.Label"));
		props.setLook(wlName);
		fdlName = new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.top = new FormAttachment(0, 0);
		fdlName.right = new FormAttachment(middle, 0);
		wlName.setLayoutData(fdlName);

		wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName = new FormData();
		fdName.top = new FormAttachment(0, 0);
		fdName.left = new FormAttachment(middle, 0);
		fdName.right = new FormAttachment(100, 0);
		wName.setLayoutData(fdName);

		// Jobname line
		wlJobname = new Label(shell, SWT.RIGHT);
		wlJobname.setText(Messages.getString("JobJob.InternalName.Label"));
		props.setLook(wlJobname);
		fdlJobname = new FormData();
		fdlJobname.top = new FormAttachment(wName, margin * 2);
		fdlJobname.left = new FormAttachment(0, 0);
		fdlJobname.right = new FormAttachment(middle, 0);
		wlJobname.setLayoutData(fdlJobname);

		wbJobname = new Button(shell, SWT.PUSH | SWT.CENTER);
		props.setLook(wbJobname);
		wbJobname.setText("...");
		fdbJobname = new FormData();
		fdbJobname.top = new FormAttachment(wName, margin * 2);
		fdbJobname.right = new FormAttachment(100, 0);
		wbJobname.setLayoutData(fdbJobname);
		wbJobname.setEnabled(rep != null);

		wJobname = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wJobname);
		wJobname.setToolTipText(Messages.getString("JobJob.InternalName.Tooltip"));
		wJobname.addModifyListener(lsMod);
		fdJobname = new FormData();
		fdJobname.top = new FormAttachment(wName, margin * 2);
		fdJobname.left = new FormAttachment(middle, 0);
		fdJobname.right = new FormAttachment(wbJobname, -margin);
		wJobname.setLayoutData(fdJobname);

		// Directory line
		wlDirectory = new Label(shell, SWT.RIGHT);
		wlDirectory.setText(Messages.getString("JobJob.Repository.Label"));
		props.setLook(wlDirectory);
		fdlDirectory = new FormData();
		fdlDirectory.top = new FormAttachment(wJobname, margin * 2);
		fdlDirectory.left = new FormAttachment(0, 0);
		fdlDirectory.right = new FormAttachment(middle, 0);
		wlDirectory.setLayoutData(fdlDirectory);

		wDirectory = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wDirectory);
		wDirectory.setToolTipText(Messages.getString("JobJob.Repository.Tooltip"));
		wDirectory.addModifyListener(lsMod);
		fdDirectory = new FormData();
		fdDirectory.top = new FormAttachment(wJobname, margin * 2);
		fdDirectory.left = new FormAttachment(middle, 0);
		fdDirectory.right = new FormAttachment(100, 0);
		wDirectory.setLayoutData(fdDirectory);
		wDirectory.setEditable(false);

		// Filename line
		wlFilename = new Label(shell, SWT.RIGHT);
		wlFilename.setText(Messages.getString("JobJob.Filename.Label"));
		props.setLook(wlFilename);
		fdlFilename = new FormData();
		fdlFilename.top = new FormAttachment(wDirectory, margin);
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.right = new FormAttachment(middle, 0);
		wlFilename.setLayoutData(fdlFilename);

		wbFilename = new Button(shell, SWT.PUSH | SWT.CENTER);
		props.setLook(wbFilename);
		wbFilename.setText("...");
		fdbFilename = new FormData();
		fdbFilename.top = new FormAttachment(wDirectory, margin);
		fdbFilename.right = new FormAttachment(100, 0);
		wbFilename.setLayoutData(fdbFilename);

		wFilename = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename = new FormData();
		fdFilename.top = new FormAttachment(wDirectory, margin);
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right = new FormAttachment(wbFilename, -margin);
		wFilename.setLayoutData(fdFilename);

		// logging grouping?
		// ////////////////////////
		// START OF LOGGING GROUP///
		// /
		wLogging = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wLogging);
		wLogging.setText(Messages.getString("JobJob.LogSettings.Group.Label"));

		FormLayout groupLayout = new FormLayout();
		groupLayout.marginWidth = 10;
		groupLayout.marginHeight = 10;

		wLogging.setLayout(groupLayout);

		// Set the logfile?
		wlSetLogfile = new Label(wLogging, SWT.RIGHT);
		wlSetLogfile.setText(Messages.getString("JobJob.Specify.Logfile.Label"));
		props.setLook(wlSetLogfile);
		fdlSetLogfile = new FormData();
		fdlSetLogfile.left = new FormAttachment(0, 0);
		fdlSetLogfile.top = new FormAttachment(0, margin);
		fdlSetLogfile.right = new FormAttachment(middle, -margin);
		wlSetLogfile.setLayoutData(fdlSetLogfile);
		wSetLogfile = new Button(wLogging, SWT.CHECK);
		props.setLook(wSetLogfile);
		fdSetLogfile = new FormData();
		fdSetLogfile.left = new FormAttachment(middle, 0);
		fdSetLogfile.top = new FormAttachment(0, margin);
		fdSetLogfile.right = new FormAttachment(100, 0);
		wSetLogfile.setLayoutData(fdSetLogfile);
		wSetLogfile.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				setActive();
			}
		});

		// Set the logfile path + base-name
		wlLogfile = new Label(wLogging, SWT.RIGHT);
		wlLogfile.setText(Messages.getString("JobJob.NameOfLogfile.Label"));
		props.setLook(wlLogfile);
		fdlLogfile = new FormData();
		fdlLogfile.left = new FormAttachment(0, 0);
		fdlLogfile.top = new FormAttachment(wlSetLogfile, margin);
		fdlLogfile.right = new FormAttachment(middle, 0);
		wlLogfile.setLayoutData(fdlLogfile);
		wLogfile = new TextVar(jobMeta, wLogging, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wLogfile.setText("");
		props.setLook(wLogfile);
		fdLogfile = new FormData();
		fdLogfile.left = new FormAttachment(middle, 0);
		fdLogfile.top = new FormAttachment(wlSetLogfile, margin);
		fdLogfile.right = new FormAttachment(100, 0);
		wLogfile.setLayoutData(fdLogfile);

		// Set the logfile filename extention
		wlLogext = new Label(wLogging, SWT.RIGHT);
		wlLogext.setText(Messages.getString("JobJob.LogfileExtension.Label"));
		props.setLook(wlLogext);
		fdlLogext = new FormData();
		fdlLogext.left = new FormAttachment(0, 0);
		fdlLogext.top = new FormAttachment(wLogfile, margin);
		fdlLogext.right = new FormAttachment(middle, 0);
		wlLogext.setLayoutData(fdlLogext);
		wLogext = new TextVar(jobMeta, wLogging, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wLogext.setText("");
		props.setLook(wLogext);
		fdLogext = new FormData();
		fdLogext.left = new FormAttachment(middle, 0);
		fdLogext.top = new FormAttachment(wLogfile, margin);
		fdLogext.right = new FormAttachment(100, 0);
		wLogext.setLayoutData(fdLogext);

		// Add date to logfile name?
		wlAddDate = new Label(wLogging, SWT.RIGHT);
		wlAddDate.setText(Messages.getString("JobJob.Logfile.IncludeDate.Label"));
		props.setLook(wlAddDate);
		fdlAddDate = new FormData();
		fdlAddDate.left = new FormAttachment(0, 0);
		fdlAddDate.top = new FormAttachment(wLogext, margin);
		fdlAddDate.right = new FormAttachment(middle, -margin);
		wlAddDate.setLayoutData(fdlAddDate);
		wAddDate = new Button(wLogging, SWT.CHECK);
		props.setLook(wAddDate);
		fdAddDate = new FormData();
		fdAddDate.left = new FormAttachment(middle, 0);
		fdAddDate.top = new FormAttachment(wLogext, margin);
		fdAddDate.right = new FormAttachment(100, 0);
		wAddDate.setLayoutData(fdAddDate);

		// Add time to logfile name?
		wlAddTime = new Label(wLogging, SWT.RIGHT);
		wlAddTime.setText(Messages.getString("JobJob.Logfile.IncludeTime.Label"));
		props.setLook(wlAddTime);
		fdlAddTime = new FormData();
		fdlAddTime.left = new FormAttachment(0, 0);
		fdlAddTime.top = new FormAttachment(wlAddDate, margin);
		fdlAddTime.right = new FormAttachment(middle, -margin);
		wlAddTime.setLayoutData(fdlAddTime);
		wAddTime = new Button(wLogging, SWT.CHECK);
		props.setLook(wAddTime);
		fdAddTime = new FormData();
		fdAddTime.left = new FormAttachment(middle, 0);
		fdAddTime.top = new FormAttachment(wlAddDate, margin);
		fdAddTime.right = new FormAttachment(100, 0);
		wAddTime.setLayoutData(fdAddTime);

		wlLoglevel = new Label(wLogging, SWT.RIGHT);
		wlLoglevel.setText(Messages.getString("JobJob.Loglevel.Label"));
		props.setLook(wlLoglevel);
		fdlLoglevel = new FormData();
		fdlLoglevel.left = new FormAttachment(0, 0);
		fdlLoglevel.right = new FormAttachment(middle, -margin);
		fdlLoglevel.top = new FormAttachment(wlAddTime, margin);
		wlLoglevel.setLayoutData(fdlLoglevel);
		wLoglevel = new CCombo(wLogging, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		for (int i = 0; i < LogWriter.log_level_desc_long.length; i++)
			wLoglevel.add(LogWriter.log_level_desc_long[i]);
		wLoglevel.select(jobEntry.loglevel);

		props.setLook(wLoglevel);
		fdLoglevel = new FormData();
		fdLoglevel.left = new FormAttachment(middle, 0);
		fdLoglevel.top = new FormAttachment(wlAddTime, margin);
		fdLoglevel.right = new FormAttachment(100, 0);
		wLoglevel.setLayoutData(fdLoglevel);

		fdLogging = new FormData();
		fdLogging.left = new FormAttachment(0, margin);
		fdLogging.top = new FormAttachment(wbFilename, margin);
		fdLogging.right = new FormAttachment(100, -margin);
		wLogging.setLayoutData(fdLogging);
		// ///////////////////////////////////////////////////////////
		// / END OF LOGGING GROUP
		// ///////////////////////////////////////////////////////////

		wlPrevious = new Label(shell, SWT.RIGHT);
		wlPrevious.setText(Messages.getString("JobJob.Previous.Label"));
		props.setLook(wlPrevious);
		fdlPrevious = new FormData();
		fdlPrevious.left = new FormAttachment(0, 0);
		fdlPrevious.top = new FormAttachment(wLogging, margin * 3);
		fdlPrevious.right = new FormAttachment(middle, -margin);
		wlPrevious.setLayoutData(fdlPrevious);
		wPrevious = new Button(shell, SWT.CHECK);
		props.setLook(wPrevious);
		wPrevious.setSelection(jobEntry.argFromPrevious);
		wPrevious.setToolTipText(Messages.getString("JobJob.Previous.Tooltip"));
		fdPrevious = new FormData();
		fdPrevious.left = new FormAttachment(middle, 0);
		fdPrevious.top = new FormAttachment(wLogging, margin * 3);
		fdPrevious.right = new FormAttachment(100, 0);
		wPrevious.setLayoutData(fdPrevious);
		wPrevious.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				wlFields.setEnabled(!wPrevious.getSelection());
				wFields.setEnabled(!wPrevious.getSelection());
			}
		});

		wlEveryRow = new Label(shell, SWT.RIGHT);
		wlEveryRow.setText(Messages.getString("JobJob.ExecForEveryInputRow.Label"));
		props.setLook(wlEveryRow);
		fdlEveryRow = new FormData();
		fdlEveryRow.left = new FormAttachment(0, 0);
		fdlEveryRow.top = new FormAttachment(wPrevious, margin * 3);
		fdlEveryRow.right = new FormAttachment(middle, -margin);
		wlEveryRow.setLayoutData(fdlEveryRow);
		wEveryRow = new Button(shell, SWT.CHECK);
		props.setLook(wEveryRow);
		wEveryRow.setSelection(jobEntry.execPerRow);
		wEveryRow.setToolTipText(Messages.getString("JobJob.ExecForEveryInputRow.Tooltip"));
		fdEveryRow = new FormData();
		fdEveryRow.left = new FormAttachment(middle, 0);
		fdEveryRow.top = new FormAttachment(wPrevious, margin * 3);
		fdEveryRow.right = new FormAttachment(100, 0);
		wEveryRow.setLayoutData(fdEveryRow);
		wEveryRow.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.execPerRow = !jobEntry.execPerRow;
				jobEntry.setChanged();
			}
		});
		
		// The remote slave server
		wlSlaveServer = new Label(shell, SWT.RIGHT);
		wlSlaveServer.setText(Messages.getString("JobJob.SlaveServer.Label"));
		wlSlaveServer.setToolTipText(Messages.getString("JobJob.SlaveServer.ToolTip"));
		props.setLook(wlSlaveServer);
		fdlSlaveServer = new FormData();
		fdlSlaveServer.left = new FormAttachment(0, 0);
		fdlSlaveServer.right = new FormAttachment(middle, -margin);
		fdlSlaveServer.top = new FormAttachment(wEveryRow, margin);
		wlSlaveServer.setLayoutData(fdlSlaveServer);
		wSlaveServer = new CCombo(shell, SWT.SINGLE | SWT.BORDER);
		wSlaveServer.setItems(SlaveServer.getSlaveServerNames(jobMeta.getSlaveServers()));
		wSlaveServer.setToolTipText(Messages.getString("JobJob.SlaveServer.ToolTip"));
		props.setLook(wSlaveServer);
		fdSlaveServer = new FormData();
		fdSlaveServer.left = new FormAttachment(middle, 0);
		fdSlaveServer.top = new FormAttachment(wEveryRow, margin);
		fdSlaveServer.right = new FormAttachment(100, 0);
		wSlaveServer.setLayoutData(fdSlaveServer);
		
		wlFields = new Label(shell, SWT.NONE);
		wlFields.setText(Messages.getString("JobJob.Fields.Label"));
		props.setLook(wlFields);
		fdlFields = new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top = new FormAttachment(wSlaveServer, margin);
		wlFields.setLayoutData(fdlFields);

		final int FieldsCols = 1;
		int rows = jobEntry.arguments == null ? 1 : (jobEntry.arguments.length == 0 ? 0
				: jobEntry.arguments.length);
		final int FieldsRows = rows;

		ColumnInfo[] colinf = new ColumnInfo[FieldsCols];
		colinf[0] = new ColumnInfo(Messages.getString("JobJob.Fields.Argument.Label"),
				ColumnInfo.COLUMN_TYPE_TEXT, false);
		colinf[0].setUsingVariables(true);

		wFields = new TableView(jobMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf,
				FieldsRows, lsMod, props);

		fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);

		wlFields.setEnabled(!wPrevious.getSelection());
		wFields.setEnabled(!wPrevious.getSelection());

		// Some buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wFields);

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
		wFilename.addSelectionListener(lsDef);

		wbJobname.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				if (rep != null)
				{
					SelectObjectDialog sod = new SelectObjectDialog(shell, rep, false, true);
					String jobname = sod.open();
					if (jobname != null)
					{
						wJobname.setText(jobname);
						wDirectory.setText(sod.getDirectory().getPath());
						// Copy it to the job entry name too...
						wName.setText(wJobname.getText());
					}
				}
			}
		});

		wbFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileObject fileName = null;

				try
				{
					String curFile = wFilename.getText();

					if (curFile.trim().length() > 0)
						fileName = VFS.getManager().resolveFile(
								jobMeta.environmentSubstitute(wFilename.getText()));
					else
						fileName = VFS.getManager().resolveFile(Const.USER_HOME_DIRECTORY);

				} catch (IOException ex)
				{
					try
					{
						fileName = VFS.getManager().resolveFile(Const.USER_HOME_DIRECTORY);
					} catch (IOException iex)
					{
						// this should not happen
						throw new RuntimeException(iex);
					}
				}

				try
				{
					try
					{
						VfsFileChooserDialog dialog = new VfsFileChooserDialog(fileName.getParent(), fileName);
						FileObject lroot = dialog.open(shell, null, new String[] {
								"*.kjb;*.xml", "*.xml", "*" }, //$NON-NLS-1$
								FILE_FILTERNAMES, VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE); //$NON-NLS-1$

						if (lroot == null)
						{
							return;
						}
						String selected = lroot.getURL().toString();

						wFilename.setText(lroot != null ? selected : Const.EMPTY_STRING);

						JobMeta job = new JobMeta(log, wFilename.getText(), rep, SpoonFactory
								.getInstance());
						if (job.getName() != null)
							wName.setText(job.getName());
						else
							wName.setText(selected);
					} catch (IOException ex)
					{
						throw new KettleXMLException(ex.getMessage());
					}
				} catch (KettleXMLException xe)
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
					mb.setText(Messages.getString("JobJob.ErrorReadingJob.Text"));
					mb.setMessage(Messages.getString("JobJob.ErrorReadingJob.Message", wFilename.getText(),
							xe.getMessage()));
					mb.open();
				}
			}
		});

		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter()
		{
			public void shellClosed(ShellEvent e)
			{
				cancel();
			}
		});

		getData();
		setActive();

		BaseStepDialog.setSize(shell);

		shell.open();
		props.setDialogSize(shell, "JobJobDialogSize");
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

	public void setActive()
	{
		wlLogfile.setEnabled(wSetLogfile.getSelection());
		wLogfile.setEnabled(wSetLogfile.getSelection());

		wlLogext.setEnabled(wSetLogfile.getSelection());
		wLogext.setEnabled(wSetLogfile.getSelection());

		wlAddDate.setEnabled(wSetLogfile.getSelection());
		wAddDate.setEnabled(wSetLogfile.getSelection());

		wlAddTime.setEnabled(wSetLogfile.getSelection());
		wAddTime.setEnabled(wSetLogfile.getSelection());

		wlLoglevel.setEnabled(wSetLogfile.getSelection());
		wLoglevel.setEnabled(wSetLogfile.getSelection());
	}

	public void getData()
	{
		if (jobEntry.getDirectory() != null)
			wDirectory.setText(jobEntry.getDirectory().getPath());
		if (jobEntry.getName() != null)
			wName.setText(jobEntry.getName());
		if (jobEntry.getJobName() != null)
			wJobname.setText(jobEntry.getJobName());
		if (jobEntry.getFilename() != null)
			wFilename.setText(jobEntry.getFilename());
		if (jobEntry.arguments != null)
		{
			for (int i = 0; i < jobEntry.arguments.length; i++)
			{
				TableItem ti = wFields.table.getItem(i);
				if (jobEntry.arguments[i] != null)
					ti.setText(1, jobEntry.arguments[i]);
			}
			wFields.setRowNums();
			wFields.optWidth(true);
		}
		wPrevious.setSelection(jobEntry.argFromPrevious);
		wSetLogfile.setSelection(jobEntry.setLogfile);
		if (jobEntry.logfile != null)
			wLogfile.setText(jobEntry.logfile);
		if (jobEntry.logext != null)
			wLogext.setText(jobEntry.logext);
		wAddDate.setSelection(jobEntry.addDate);
		wAddTime.setSelection(jobEntry.addTime);
		
		if (jobEntry.getRemoteSlaveServer()!=null)
		{
			wSlaveServer.setText(jobEntry.getRemoteSlaveServer().getName());
		}

		wLoglevel.select(jobEntry.loglevel);
	}

	private void cancel()
	{
		jobEntry.setChanged(backupChanged);

		jobEntry = null;
		dispose();
	}

	private void ok()
	{
		jobEntry.setJobName(wJobname.getText());
		jobEntry.setFileName(wFilename.getText());
		jobEntry.setName(wName.getText());
		if (rep != null)
			jobEntry.setDirectory(rep.getDirectoryTree().findDirectory(wDirectory.getText()));

		int nritems = wFields.nrNonEmpty();
		int nr = 0;
		for (int i = 0; i < nritems; i++)
		{
			String arg = wFields.getNonEmpty(i).getText(1);
			if (arg != null && arg.length() != 0)
				nr++;
		}
		jobEntry.arguments = new String[nr];
		nr = 0;
		for (int i = 0; i < nritems; i++)
		{
			String arg = wFields.getNonEmpty(i).getText(1);
			if (arg != null && arg.length() != 0)
			{
				jobEntry.arguments[nr] = arg;
				nr++;
			}
		}

		jobEntry.setLogfile = wSetLogfile.getSelection();
		jobEntry.addDate = wAddDate.getSelection();
		jobEntry.addTime = wAddTime.getSelection();
		jobEntry.logfile = wLogfile.getText();
		jobEntry.logext = wLogext.getText();
		jobEntry.loglevel = wLoglevel.getSelectionIndex();
		jobEntry.argFromPrevious = wPrevious.getSelection();
		jobEntry.execPerRow = wEveryRow.getSelection();
		
		jobEntry.setRemoteSlaveServer( SlaveServer.findSlaveServer(jobMeta.getSlaveServers(), wSlaveServer.getText()) );
		
		dispose();
	}
}