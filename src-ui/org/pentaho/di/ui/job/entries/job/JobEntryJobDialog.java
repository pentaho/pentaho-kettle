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
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Composite;
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
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.job.Messages;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
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
	private TextVar wDirectory;
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
	
	private Label wlPrevToParams;
	private Button wPrevToParams;
	private FormData fdlPrevToParams, fdPrevToParams;	

	private Label wlEveryRow;
	private Button wEveryRow;
	private FormData fdlEveryRow, fdEveryRow;

	private TableView wFields;
	private TableView wParameters;

	private Label wlSlaveServer;
	private ComboVar wSlaveServer;
	private FormData fdlSlaveServer, fdSlaveServer;
	
	private Label wlWaitingToFinish;
	private Button wWaitingToFinish;
	private FormData fdlWaitingToFinish, fdWaitingToFinish;

	private Label wlFollowingAbortRemotely;
	private Button wFollowingAbortRemotely;
	private FormData fdlFollowingAbortRemotely, fdFollowingAbortRemotely;

	private Label wlPassParams;
	private Button wPassParams;
	private FormData fdlPassParams, fdPassParams;

	private Button wOK, wCancel;

	private Listener lsOK, lsCancel;

	private Shell shell;

	private SelectionAdapter lsDef;

	private JobEntryJob jobEntry;

	private boolean backupChanged;
	
    private Label wlAppendLogfile;

    private Button wAppendLogfile;

    private FormData fdlAppendLogfile, fdAppendLogfile;

	private Display display;	
	
    public JobEntryJobDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
	{
		super(parent, jobEntryInt, rep, jobMeta);
		jobEntry = (JobEntryJob) jobEntryInt;
		this.log = LogWriter.getInstance();
	}

	public JobEntryInterface open()
	{
		CTabFolder   wTabFolder;
		FormData     fdTabFolder;
		
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
		fdlName.right = new FormAttachment(middle, -margin);
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
		fdlJobname.right = new FormAttachment(middle, -margin);
		wlJobname.setLayoutData(fdlJobname);

		wbJobname = new Button(shell, SWT.PUSH | SWT.CENTER);
		//props.setLook(wbJobname);
		//wbJobname.setText("...");
		wbJobname.setImage(GUIResource.getInstance().getImageJobGraph());
		wbJobname.setToolTipText(Messages.getString("JobJob.SelectJobRep.Tooltip"));
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
		fdlDirectory.right = new FormAttachment(middle, -margin);
		wlDirectory.setLayoutData(fdlDirectory);

		wDirectory = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wDirectory);
		wDirectory.setToolTipText(Messages.getString("JobJob.Repository.Tooltip"));
		wDirectory.addModifyListener(lsMod);
		fdDirectory = new FormData();
		fdDirectory.top = new FormAttachment(wJobname, margin * 2);
		fdDirectory.left = new FormAttachment(middle, 0);
		fdDirectory.right = new FormAttachment(100, 0);
		wDirectory.setLayoutData(fdDirectory);

		// Filename line
		wlFilename = new Label(shell, SWT.RIGHT);
		wlFilename.setText(Messages.getString("JobJob.Filename.Label"));
		props.setLook(wlFilename);
		fdlFilename = new FormData();
		fdlFilename.top = new FormAttachment(wDirectory, margin);
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.right = new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbFilename = new Button(shell, SWT.PUSH | SWT.CENTER);
		props.setLook(wbFilename);
		//wbFilename.setText("...");
		wbFilename.setImage(GUIResource.getInstance().getImageJobGraph());
		wbFilename.setToolTipText(Messages.getString("JobJob.SelectFilename.Tooltip"));
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
		
		 // Append logfile?
        wlAppendLogfile = new Label(wLogging, SWT.RIGHT);
        wlAppendLogfile.setText(Messages.getString("JobJob.Append.Logfile.Label"));
        props.setLook(wlAppendLogfile);
        fdlAppendLogfile = new FormData();
        fdlAppendLogfile.left = new FormAttachment(0, 0);
        fdlAppendLogfile.top = new FormAttachment(wSetLogfile, margin);
        fdlAppendLogfile.right = new FormAttachment(middle, -margin);
        wlAppendLogfile.setLayoutData(fdlAppendLogfile);
        wAppendLogfile = new Button(wLogging, SWT.CHECK);
        wAppendLogfile.setToolTipText(Messages.getString("JobJob.Append.Logfile.Tooltip"));
        props.setLook(wAppendLogfile);
        fdAppendLogfile = new FormData();
        fdAppendLogfile.left = new FormAttachment(middle, 0);
        fdAppendLogfile.top = new FormAttachment(wSetLogfile, margin);
        fdAppendLogfile.right = new FormAttachment(100, 0);
        wAppendLogfile.setLayoutData(fdAppendLogfile);
        wAppendLogfile.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            }
        });

		// Set the logfile path + base-name
		wlLogfile = new Label(wLogging, SWT.RIGHT);
		wlLogfile.setText(Messages.getString("JobJob.NameOfLogfile.Label"));
		props.setLook(wlLogfile);
		fdlLogfile = new FormData();
		fdlLogfile.left = new FormAttachment(0, 0);
		fdlLogfile.top = new FormAttachment(wAppendLogfile, margin);
		fdlLogfile.right = new FormAttachment(middle, -margin);
		wlLogfile.setLayoutData(fdlLogfile);
		wLogfile = new TextVar(jobMeta, wLogging, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wLogfile.setText("");
		props.setLook(wLogfile);
		fdLogfile = new FormData();
		fdLogfile.left = new FormAttachment(middle, 0);
		fdLogfile.top = new FormAttachment(wAppendLogfile, margin);
		fdLogfile.right = new FormAttachment(100, 0);
		wLogfile.setLayoutData(fdLogfile);

		// Set the logfile filename extention
		wlLogext = new Label(wLogging, SWT.RIGHT);
		wlLogext.setText(Messages.getString("JobJob.LogfileExtension.Label"));
		props.setLook(wlLogext);
		fdlLogext = new FormData();
		fdlLogext.left = new FormAttachment(0, 0);
		fdlLogext.top = new FormAttachment(wLogfile, margin);
		fdlLogext.right = new FormAttachment(middle, -margin);
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
		fdlAddTime.top = new FormAttachment(wAddDate, margin);
		fdlAddTime.right = new FormAttachment(middle, -margin);
		wlAddTime.setLayoutData(fdlAddTime);
		wAddTime = new Button(wLogging, SWT.CHECK);
		props.setLook(wAddTime);
		fdAddTime = new FormData();
		fdAddTime.left = new FormAttachment(middle, 0);
		fdAddTime.top = new FormAttachment(wAddDate, margin);
		fdAddTime.right = new FormAttachment(100, 0);
		wAddTime.setLayoutData(fdAddTime);

		wlLoglevel = new Label(wLogging, SWT.RIGHT);
		wlLoglevel.setText(Messages.getString("JobJob.Loglevel.Label"));
		props.setLook(wlLoglevel);
		fdlLoglevel = new FormData();
		fdlLoglevel.left = new FormAttachment(0, 0);
		fdlLoglevel.right = new FormAttachment(middle, -margin);
		fdlLoglevel.top = new FormAttachment(wAddTime, margin);
		wlLoglevel.setLayoutData(fdlLoglevel);
		wLoglevel = new CCombo(wLogging, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		for (int i = 0; i < LogWriter.log_level_desc_long.length; i++)
			wLoglevel.add(LogWriter.log_level_desc_long[i]);
		wLoglevel.select(jobEntry.loglevel);

		props.setLook(wLoglevel);
		fdLoglevel = new FormData();
		fdLoglevel.left = new FormAttachment(middle, 0);
		fdLoglevel.top = new FormAttachment(wAddTime, margin);
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
				jobEntry.setChanged();
			}
		});

		wlPrevToParams = new Label(shell, SWT.RIGHT);
		wlPrevToParams.setText(Messages.getString("JobJob.PrevToParams.Label"));
		props.setLook(wlPrevToParams);
		fdlPrevToParams = new FormData();
		fdlPrevToParams.left = new FormAttachment(0, 0);
		fdlPrevToParams.top = new FormAttachment(wPrevious, margin * 3);
		fdlPrevToParams.right = new FormAttachment(middle, -margin);
		wlPrevToParams.setLayoutData(fdlPrevToParams);
		wPrevToParams = new Button(shell, SWT.CHECK);
		props.setLook(wPrevToParams);
		wPrevToParams.setSelection(jobEntry.paramsFromPrevious);
		wPrevToParams.setToolTipText(Messages.getString("JobJob.PrevToParams.Tooltip"));
		fdPrevToParams = new FormData();
		fdPrevToParams.left = new FormAttachment(middle, 0);
		fdPrevToParams.top = new FormAttachment(wPrevious, margin * 3);
		fdPrevToParams.right = new FormAttachment(100, 0);
		wPrevToParams.setLayoutData(fdPrevToParams);
		wPrevToParams.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		
		wlEveryRow = new Label(shell, SWT.RIGHT);
		wlEveryRow.setText(Messages.getString("JobJob.ExecForEveryInputRow.Label"));
		props.setLook(wlEveryRow);
		fdlEveryRow = new FormData();
		fdlEveryRow.left = new FormAttachment(0, 0);
		fdlEveryRow.top = new FormAttachment(wPrevToParams, margin * 3);
		fdlEveryRow.right = new FormAttachment(middle, -margin);
		wlEveryRow.setLayoutData(fdlEveryRow);
		wEveryRow = new Button(shell, SWT.CHECK);
		props.setLook(wEveryRow);
		wEveryRow.setSelection(jobEntry.execPerRow);
		wEveryRow.setToolTipText(Messages.getString("JobJob.ExecForEveryInputRow.Tooltip"));
		fdEveryRow = new FormData();
		fdEveryRow.left = new FormAttachment(middle, 0);
		fdEveryRow.top = new FormAttachment(wPrevToParams, margin * 3);
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
		wSlaveServer = new ComboVar(jobMeta, shell, SWT.SINGLE | SWT.BORDER);
		wSlaveServer.setItems(SlaveServer.getSlaveServerNames(jobMeta.getSlaveServers()));
		wSlaveServer.setToolTipText(Messages.getString("JobJob.SlaveServer.ToolTip"));
		props.setLook(wSlaveServer);
		fdSlaveServer = new FormData();
		fdSlaveServer.left = new FormAttachment(middle, 0);
		fdSlaveServer.top = new FormAttachment(wEveryRow, margin);
		fdSlaveServer.right = new FormAttachment(100, 0);
		wSlaveServer.setLayoutData(fdSlaveServer);
		wSlaveServer.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { setActive(); } });

		// Wait for the remote transformation to finish?
		//
		wlWaitingToFinish = new Label(shell, SWT.RIGHT);
		wlWaitingToFinish.setText(Messages.getString("JobJob.WaitToFinish.Label"));
		props.setLook(wlWaitingToFinish);
		fdlWaitingToFinish = new FormData();
		fdlWaitingToFinish.left = new FormAttachment(0, 0);
		fdlWaitingToFinish.top = new FormAttachment(wSlaveServer, margin);
		fdlWaitingToFinish.right = new FormAttachment(middle, -margin);
		wlWaitingToFinish.setLayoutData(fdlWaitingToFinish);
		wWaitingToFinish = new Button(shell, SWT.CHECK);
		props.setLook(wWaitingToFinish);
		fdWaitingToFinish = new FormData();
		fdWaitingToFinish.left = new FormAttachment(middle, 0);
		fdWaitingToFinish.top = new FormAttachment(wSlaveServer, margin);
		fdWaitingToFinish.right = new FormAttachment(100, 0);
		wWaitingToFinish.setLayoutData(fdWaitingToFinish);
		wWaitingToFinish.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { setActive(); } });

		// Follow a local abort remotely?
		//
		wlFollowingAbortRemotely = new Label(shell, SWT.RIGHT);
		wlFollowingAbortRemotely.setText(Messages.getString("JobJob.AbortRemote.Label"));
		props.setLook(wlFollowingAbortRemotely);
		fdlFollowingAbortRemotely = new FormData();
		fdlFollowingAbortRemotely.left = new FormAttachment(0, 0);
		fdlFollowingAbortRemotely.top = new FormAttachment(wWaitingToFinish, margin);
		fdlFollowingAbortRemotely.right = new FormAttachment(middle, -margin);
		wlFollowingAbortRemotely.setLayoutData(fdlFollowingAbortRemotely);
		wFollowingAbortRemotely = new Button(shell, SWT.CHECK);
		props.setLook(wFollowingAbortRemotely);
		fdFollowingAbortRemotely = new FormData();
		fdFollowingAbortRemotely.left = new FormAttachment(middle, 0);
		fdFollowingAbortRemotely.top = new FormAttachment(wWaitingToFinish, margin);
		fdFollowingAbortRemotely.right = new FormAttachment(100, 0);
		wFollowingAbortRemotely.setLayoutData(fdFollowingAbortRemotely);

		wTabFolder = new CTabFolder(shell, SWT.BORDER);
		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
	
		// The Argument tab
		CTabItem wFieldTab = new CTabItem(wTabFolder, SWT.NONE);
		wFieldTab.setText(Messages.getString("JobJob.Fields.Argument.Label")); //$NON-NLS-1$
        
        FormLayout fieldLayout = new FormLayout();
        fieldLayout.marginWidth  = Const.MARGIN;
        fieldLayout.marginHeight = Const.MARGIN;
        
		Composite wFieldComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wFieldComp);
		wFieldComp.setLayout(fieldLayout);
				
		final int FieldsCols = 1;
		int rows = jobEntry.arguments == null ? 1 : (jobEntry.arguments.length == 0 ? 0
				: jobEntry.arguments.length);
		final int FieldsRows = rows;

		ColumnInfo[] colinf = new ColumnInfo[FieldsCols];
		colinf[0] = new ColumnInfo(Messages.getString("JobJob.Fields.Argument.Label"),
				ColumnInfo.COLUMN_TYPE_TEXT, false);
		colinf[0].setUsingVariables(true);

		wFields = new TableView(jobMeta, wFieldComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf,
				FieldsRows, lsMod, props);
		
        FormData fdFields = new FormData();
        fdFields.left  = new FormAttachment(0, 0);
        fdFields.top   = new FormAttachment(0, margin);
        fdFields.right = new FormAttachment(100, 0);
        fdFields.bottom= new FormAttachment(100, 0);
        wFields.setLayoutData(fdFields);

        FormData fdFieldsComp = new FormData();
        fdFieldsComp.left  = new FormAttachment(0, 0);
        fdFieldsComp.top   = new FormAttachment(0, 0);
        fdFieldsComp.right = new FormAttachment(100, 0);
        fdFieldsComp.bottom= new FormAttachment(100, 0);
        wFieldComp.setLayoutData(fdFieldsComp);
        
        wFieldComp.layout();
        wFieldTab.setControl(wFieldComp);		

		// The parameters tab
		CTabItem wParametersTab = new CTabItem(wTabFolder, SWT.NONE);
		wParametersTab.setText(Messages.getString("JobJob.Fields.Parameters.Label")); //$NON-NLS-1$
        
        fieldLayout = new FormLayout();
        fieldLayout.marginWidth  = Const.MARGIN;
        fieldLayout.marginHeight = Const.MARGIN;
        
		Composite wParameterComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wParameterComp);
		wParameterComp.setLayout(fieldLayout);
		
		// Pass all parameters down 
		//
		wlPassParams = new Label(wParameterComp, SWT.RIGHT);
		wlPassParams.setText(Messages.getString("JobJob.PassAllParameters.Label"));
		props.setLook(wlPassParams);
		fdlPassParams = new FormData();
		fdlPassParams.left = new FormAttachment(0, 0);
		fdlPassParams.top = new FormAttachment(0, 0);
		fdlPassParams.right = new FormAttachment(middle, -margin);
		wlPassParams.setLayoutData(fdlPassParams);
		wPassParams = new Button(wParameterComp, SWT.CHECK);
		props.setLook(wPassParams);
		fdPassParams = new FormData();
		fdPassParams.left = new FormAttachment(middle, 0);
		fdPassParams.top = new FormAttachment(0, 0);
		fdPassParams.right = new FormAttachment(100, 0);
		wPassParams.setLayoutData(fdPassParams);
		
		final int ParameterCols = 3;
		final int parameterRows = jobEntry.parameters.length;

	    colinf = new ColumnInfo[ParameterCols];
		colinf[0] = new ColumnInfo(Messages.getString("JobJob.Parameters.Parameter.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false);
		colinf[1] = new ColumnInfo(Messages.getString("JobJob.Parameters.ColumnName.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false);
		colinf[2] = new ColumnInfo(Messages.getString("JobJob.Parameters.Value.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false);		
		colinf[2].setUsingVariables(true);

		wParameters = new TableView(jobMeta, wParameterComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf,
				parameterRows, lsMod, props);
				
        FormData fdParameters = new FormData();
        fdParameters.left  = new FormAttachment(0, 0);
        fdParameters.top   = new FormAttachment(wPassParams, margin);
        fdParameters.right = new FormAttachment(100, 0);
        fdParameters.bottom= new FormAttachment(100, 0);
        wParameters.setLayoutData(fdParameters);

        FormData fdParametersComp = new FormData();
        fdParametersComp.left  = new FormAttachment(0, 0);
        fdParametersComp.top   = new FormAttachment(0, 0);
        fdParametersComp.right = new FormAttachment(100, 0);
        fdParametersComp.bottom= new FormAttachment(100, 0);
        wParameterComp.setLayoutData(fdParametersComp);
        
        wParameterComp.layout();
        wParametersTab.setControl(wParameterComp);		        
        
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wFollowingAbortRemotely, 0);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
			
		wTabFolder.setSelection(0);
		
		// Some buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wTabFolder);

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

						JobMeta jobMeta = new JobMeta(log, wFilename.getText(), rep, SpoonFactory.getInstance());
						if (jobMeta.getName() != null) {
							wName.setText(jobMeta.getName());
						} else {
							wName.setText(selected);
						}
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
		
		wlAppendLogfile.setEnabled(wSetLogfile.getSelection());
		wAppendLogfile.setEnabled(wSetLogfile.getSelection());
		
		wlWaitingToFinish.setEnabled( !Const.isEmpty(wSlaveServer.getText()));
		wWaitingToFinish.setEnabled( !Const.isEmpty(wSlaveServer.getText()));

		wlFollowingAbortRemotely.setEnabled( wWaitingToFinish.getSelection() && !Const.isEmpty(wSlaveServer.getText()));
		wFollowingAbortRemotely.setEnabled( wWaitingToFinish.getSelection() && !Const.isEmpty(wSlaveServer.getText()));
	}

	public void getData()
	{
		if (jobEntry.getDirectory() != null)
			wDirectory.setText(jobEntry.getDirectory());
		if (jobEntry.getName() != null)
			wName.setText(jobEntry.getName());
		if (jobEntry.getJobName() != null)
			wJobname.setText(jobEntry.getJobName());
		if (jobEntry.getFilename() != null)
			wFilename.setText(jobEntry.getFilename());
		
		// Arguments
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

		// Parameters
		if (jobEntry.parameters != null)
		{
			for (int i = 0; i < jobEntry.parameters.length; i++)
			{
				TableItem ti = wParameters.table.getItem(i);
				if (! Const.isEmpty(jobEntry.parameters[i]) )  {
					ti.setText(1, Const.NVL(jobEntry.parameters[i], ""));
					ti.setText(2, Const.NVL(jobEntry.parameterFieldNames[i], ""));
					ti.setText(3, Const.NVL(jobEntry.parameterValues[i], ""));
				}
			}
			wParameters.setRowNums();
			wParameters.optWidth(true);
		}	
		
		wPassParams.setSelection(jobEntry.isPassingAllParameters());
		
		wPrevious.setSelection(jobEntry.argFromPrevious);
		wPrevToParams.setSelection(jobEntry.paramsFromPrevious);
		wSetLogfile.setSelection(jobEntry.setLogfile);
		if (jobEntry.logfile != null)
			wLogfile.setText(jobEntry.logfile);
		if (jobEntry.logext != null)
			wLogext.setText(jobEntry.logext);
		wAddDate.setSelection(jobEntry.addDate);
		wAddTime.setSelection(jobEntry.addTime);
		
		if (jobEntry.getRemoteSlaveServerName()!=null)
		{
			wSlaveServer.setText(jobEntry.getRemoteSlaveServerName());
		}

		wLoglevel.select(jobEntry.loglevel);
		wAppendLogfile.setSelection(jobEntry.setAppendLogfile);
		wWaitingToFinish.setSelection(jobEntry.isWaitingToFinish());
		wFollowingAbortRemotely.setSelection(jobEntry.isFollowingAbortRemotely());
	}

	private void cancel()
	{
		jobEntry.setChanged(backupChanged);

		jobEntry = null;
		dispose();
	}

	private void ok()
	{
	   if(Const.isEmpty(wName.getText())) 
         {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setText(Messages.getString("System.StepJobEntryNameMissing.Title"));
			mb.setMessage(Messages.getString("System.JobEntryNameMissing.Msg"));
			mb.open(); 
			return;
         }
		jobEntry.setJobName(wJobname.getText());
		jobEntry.setFileName(wFilename.getText());
		jobEntry.setName(wName.getText());
		if (rep != null)  {
			jobEntry.setDirectory(wDirectory.getText());
		}

		// Do the arguments
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

		// Do the parameters
		nritems = wParameters.nrNonEmpty();
		nr = 0;
		for (int i = 0; i < nritems; i++)
		{
			String param = wParameters.getNonEmpty(i).getText(1);
			if (param != null && param.length() != 0)
				nr++;
		}
		jobEntry.parameters          = new String[nr];
		jobEntry.parameterFieldNames = new String[nr];
		jobEntry.parameterValues     = new String[nr];
		nr = 0;
		for (int i = 0; i < nritems; i++)
		{
			String param = wParameters.getNonEmpty(i).getText(1);
			String fieldName = wParameters.getNonEmpty(i).getText(2);
			String value = wParameters.getNonEmpty(i).getText(3);
			
			jobEntry.parameters[nr] = param;
			
			if ( ! Const.isEmpty(Const.trim(fieldName)) )  {
				jobEntry.parameterFieldNames[nr] = fieldName;				
			}
			else  {
				jobEntry.parameterFieldNames[nr] = "";
			}

			if ( ! Const.isEmpty(Const.trim(value)) )  {
				jobEntry.parameterValues[nr] = value;				
			}
			else  {
				jobEntry.parameterValues[nr] = "";
			}			
			
			nr++;
		}		
		jobEntry.setPassingAllParameters(wPassParams.getSelection());

		jobEntry.setLogfile = wSetLogfile.getSelection();
		jobEntry.addDate = wAddDate.getSelection();
		jobEntry.addTime = wAddTime.getSelection();
		jobEntry.logfile = wLogfile.getText();
		jobEntry.logext = wLogext.getText();
		jobEntry.loglevel = wLoglevel.getSelectionIndex();
		jobEntry.argFromPrevious = wPrevious.getSelection();
		jobEntry.paramsFromPrevious = wPrevToParams.getSelection();
		jobEntry.execPerRow = wEveryRow.getSelection();
		
		jobEntry.setRemoteSlaveServerName( wSlaveServer.getText() );
		jobEntry.setAppendLogfile = wAppendLogfile.getSelection();
		jobEntry.setWaitingToFinish( wWaitingToFinish.getSelection() );
		jobEntry.setFollowingAbortRemotely( wFollowingAbortRemotely.getSelection() );
		dispose();
	}
}