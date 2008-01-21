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

package org.pentaho.di.ui.job.entries.mssqlbulkload;

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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog; 
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.job.entries.mssqlbulkload.JobEntryMssqlBulkLoad;
import org.pentaho.di.job.entries.mssqlbulkload.Messages;

/**
 * Dialog class for the MySqlBulkLoader.
 * 
 * @author Samatar Hassan
 * @since  Jan-2007
 */
public class JobEntryMssqlBulkLoadDialog extends JobEntryDialog implements JobEntryDialogInterface
{

	private static final String[] FILETYPES = new String[] { Messages.getString("JobMssqlBulkLoad.Filetype.Text"), Messages.getString("JobMssqlBulkLoad.Filetype.All") };

	private Label wlName;
	private Text wName;
	private FormData fdlName, fdName;

	private CCombo wConnection;

	// Schema name
	private Label wlSchemaname;
	private TextVar wSchemaname;
	private FormData fdlSchemaname, fdSchemaname;

	private Label wlTablename;
	private TextVar wTablename;
	private FormData fdlTablename, fdTablename;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;
	private JobEntryMssqlBulkLoad jobEntry;
	private Shell shell;
	private SelectionAdapter lsDef;
	private boolean changed;

	// File
	private Label wlFilename;
	private Button wbFilename;
	private TextVar wFilename;
	private FormData fdlFilename, fdbFilename, fdFilename;


	// Separator
	private Label        wlSeparator;
	private TextVar         wSeparator;
	private FormData     fdlSeparator,  fdSeparator;

	//Line terminated
	private Label wlLineterminated;
	private TextVar wLineterminated;
	private FormData fdlLineterminated, fdLineterminated;


	//List Columns
	private Label wlOrderBy;
	private TextVar wOrderBy;
	private FormData fdlOrderBy, fdOrderBy;

	//Take First lines
	private Label wlTakelines;
	private TextVar wTakelines;
	private FormData fdlTakelines, fdTakelines;
	
	//Specific Codepage
	private Label wlSpecificCodePage;
	private TextVar wSpecificCodePage;
	private FormData fdlSpecificCodePage, fdSpecificCodePage;


	private Button wbTable;
	private Button wbOrderBy;
	

	//  Add File to result
	private Group wFileResult;
    private FormData fdFileResult;
    
    
	private Label        wlAddFileToResult;
	private Button       wAddFileToResult;
	private FormData     fdlAddFileToResult, fdAddFileToResult;
	
	// Fire Triggers
	private Label        wlFireTriggers;
	private Button       wFireTriggers;
	private FormData     fdlFireTriggers, fdFireTriggers;
	
	// Check Constaints
	private Label        wlCheckConstraints;
	private Button       wCheckConstraints;
	private FormData     fdlCheckConstraints, fdCheckConstraints;
	
	// Keep nulls
	private Label        wlKeepNulls;
	private Button       wKeepNulls;
	private FormData     fdlKeepNulls, fdKeepNulls;
	
	// Tablock
	private Label        wlTablock;
	private Button       wTablock;
	private FormData     fdlTablock, fdTablock;
	
	
	// Data file type
	private Label wlDataFiletype;
	private  CCombo wDataFiletype;
	private FormData fdlDataFiletype, fdDataFiletype;
	
	// Format file

	private Label wlFortmatFilename;
	private Button wbFortmatFilename;
	private TextVar wFortmatFilename;
	private FormData fdlFortmatFilename, fdbFortmatFilename, fdFortmatFilename;
	
	// Order Direction
	private Label wlOrderDirection;
	private  CCombo wOrderDirection;
	private FormData fdlOrderDirection, fdOrderDirection;
	
	// CodePage
	private Label wlCodePage;
	private  CCombo wCodePage;
	private FormData fdlCodePage, fdCodePage;



    public JobEntryMssqlBulkLoadDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryMssqlBulkLoad) jobEntryInt;
        if (this.jobEntry.getName() == null)
			this.jobEntry.setName(Messages.getString("JobMssqlBulkLoad.Name.Default"));
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
		shell.setText(Messages.getString("JobMssqlBulkLoad.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlName = new Label(shell, SWT.RIGHT);
		wlName.setText(Messages.getString("JobMssqlBulkLoad.Name.Label"));
		props.setLook(wlName);
		fdlName = new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.right = new FormAttachment(middle, 0);
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
			
		// Connection line
		wConnection = addConnectionLine(shell, wName, middle, margin);
		if (jobEntry.getDatabase()==null && jobMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);
		
		// Schema name line
		wlSchemaname = new Label(shell, SWT.RIGHT);
		wlSchemaname.setText(Messages.getString("JobMssqlBulkLoad.Schemaname.Label"));
		props.setLook(wlSchemaname);
		fdlSchemaname = new FormData();
		fdlSchemaname.left = new FormAttachment(0, 0);
		fdlSchemaname.right = new FormAttachment(middle, 0);
		fdlSchemaname.top = new FormAttachment(wConnection, margin);
		wlSchemaname.setLayoutData(fdlSchemaname);

		wSchemaname = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSchemaname);
		wSchemaname.setToolTipText(Messages.getString("JobMssqlBulkLoad.Schemaname.Tooltip"));
		wSchemaname.addModifyListener(lsMod);
		fdSchemaname = new FormData();
		fdSchemaname.left = new FormAttachment(middle, 0);
		fdSchemaname.top = new FormAttachment(wConnection, margin);
		fdSchemaname.right = new FormAttachment(100, 0);
		wSchemaname.setLayoutData(fdSchemaname);

		// Table name line
		wlTablename = new Label(shell, SWT.RIGHT);
		wlTablename.setText(Messages.getString("JobMssqlBulkLoad.Tablename.Label"));
		props.setLook(wlTablename);
		fdlTablename = new FormData();
		fdlTablename.left = new FormAttachment(0, 0);
		fdlTablename.right = new FormAttachment(middle, 0);
		fdlTablename.top = new FormAttachment(wSchemaname, margin);
		wlTablename.setLayoutData(fdlTablename);

		wbTable=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbTable);
		wbTable.setText(Messages.getString("System.Button.Browse"));
		FormData fdbTable = new FormData();
		fdbTable.right= new FormAttachment(100, 0);
		fdbTable.top  = new FormAttachment(wSchemaname, margin/2);
		wbTable.setLayoutData(fdbTable);
		wbTable.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getTableName(); } } );

		wTablename = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wTablename);
		wTablename.addModifyListener(lsMod);
		fdTablename = new FormData();
		fdTablename.left = new FormAttachment(middle, 0);
		fdTablename.top = new FormAttachment(wSchemaname, margin);
		fdTablename.right = new FormAttachment(wbTable, -margin);
		wTablename.setLayoutData(fdTablename);

		// Filename line
		wlFilename = new Label(shell, SWT.RIGHT);
		wlFilename.setText(Messages.getString("JobMssqlBulkLoad.Filename.Label"));
		props.setLook(wlFilename);
		fdlFilename = new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top = new FormAttachment(wTablename, margin);
		fdlFilename.right = new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbFilename = new Button(shell, SWT.PUSH | SWT.CENTER);
		props.setLook(wbFilename);
		wbFilename.setText(Messages.getString("System.Button.Browse"));
		fdbFilename = new FormData();
		fdbFilename.right = new FormAttachment(100, 0);
		fdbFilename.top = new FormAttachment(wTablename, 0);
		wbFilename.setLayoutData(fdbFilename);

		wFilename = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename = new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.top = new FormAttachment(wTablename, margin);
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
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.txt", "*.csv", "*" });
				if (wFilename.getText() != null)
				{
					dialog.setFileName(jobMeta.environmentSubstitute(wFilename.getText()));
				}
				dialog.setFilterNames(FILETYPES);
				if (dialog.open() != null)
				{
					wFilename.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
						+ dialog.getFileName());
				}
			}
		});

		

		// Separator
		wlSeparator = new Label(shell, SWT.RIGHT);
		wlSeparator.setText(Messages.getString("JobMssqlBulkLoad.Separator.Label"));
		props.setLook(wlSeparator);
		fdlSeparator = new FormData();
		fdlSeparator.left = new FormAttachment(0, 0);
		fdlSeparator.right = new FormAttachment(middle, 0);
		fdlSeparator.top = new FormAttachment(wFilename, margin);
		wlSeparator.setLayoutData(fdlSeparator);

		wSeparator = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSeparator);
		wSeparator.addModifyListener(lsMod);
		fdSeparator = new FormData();
		fdSeparator.left = new FormAttachment(middle, 0);
		fdSeparator.top = new FormAttachment(wFilename, margin);
		fdSeparator.right = new FormAttachment(100, 0);
		wSeparator.setLayoutData(fdSeparator);

		// Line terminated
		wlLineterminated = new Label(shell, SWT.RIGHT);
		wlLineterminated.setText(Messages.getString("JobMssqlBulkLoad.Lineterminated.Label"));
		props.setLook(wlLineterminated);
		fdlLineterminated = new FormData();
		fdlLineterminated.left = new FormAttachment(0, 0);
		fdlLineterminated.right = new FormAttachment(middle, 0);
		fdlLineterminated.top = new FormAttachment(wSeparator, margin);
		wlLineterminated.setLayoutData(fdlLineterminated);

		wLineterminated = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wLineterminated);
		wLineterminated.addModifyListener(lsMod);
		fdLineterminated = new FormData();
		fdLineterminated.left = new FormAttachment(middle, 0);
		fdLineterminated.top = new FormAttachment(wSeparator, margin);
		fdLineterminated.right = new FormAttachment(100, 0);
		wLineterminated.setLayoutData(fdLineterminated);
		
		
		// Data file type
		wlDataFiletype = new Label(shell, SWT.RIGHT);
		wlDataFiletype.setText(Messages.getString("JobMysqlBulkLoad.DataFiletype.Label"));
		props.setLook(wlDataFiletype);
		fdlDataFiletype = new FormData();
		fdlDataFiletype.left = new FormAttachment(0, 0);
		fdlDataFiletype.right = new FormAttachment(middle, 0);
		fdlDataFiletype.top = new FormAttachment(wLineterminated, margin);
		wlDataFiletype.setLayoutData(fdlDataFiletype);
		wDataFiletype = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
					wDataFiletype.add("char");
					wDataFiletype.add("native");
					wDataFiletype.add("widechar");
					wDataFiletype.select(0); // +1: starts at -1

		props.setLook(wDataFiletype);
		fdDataFiletype= new FormData();
		fdDataFiletype.left = new FormAttachment(middle, 0);
		fdDataFiletype.top = new FormAttachment(wLineterminated, margin);
		fdDataFiletype.right = new FormAttachment(100, 0);
		wDataFiletype.setLayoutData(fdDataFiletype);
		
		// CodePage
		wlCodePage = new Label(shell, SWT.RIGHT);
		wlCodePage.setText(Messages.getString("JobMysqlBulkLoad.CodePage.Label"));
		props.setLook(wlCodePage);
		fdlCodePage = new FormData();
		fdlCodePage.left = new FormAttachment(0, 0);
		fdlCodePage.right = new FormAttachment(middle, 0);
		fdlCodePage.top = new FormAttachment(wDataFiletype, margin);
		wlCodePage.setLayoutData(fdlCodePage);
		wCodePage = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
					wCodePage.add("ACP");
					wCodePage.add("OEM");
					wCodePage.add("RAW");
					wCodePage.add(Messages.getString("JobMssqlBulkLoad.CodePage.Specific"));
					wCodePage.select(0); // +1: starts at -1

		props.setLook(wCodePage);
		fdCodePage= new FormData();
		fdCodePage.left = new FormAttachment(middle, 0);
		fdCodePage.top = new FormAttachment(wDataFiletype, margin);
		fdCodePage.right = new FormAttachment(100, 0);
		wCodePage.setLayoutData(fdCodePage);

		// Specific CodePage
		wlSpecificCodePage = new Label(shell, SWT.RIGHT);
		wlSpecificCodePage.setText(Messages.getString("JobMssqlBulkLoad.SpecificCodePage.Label"));
		props.setLook(wlSpecificCodePage);
		fdlSpecificCodePage = new FormData();
		fdlSpecificCodePage.left = new FormAttachment(0, 0);
		fdlSpecificCodePage.right = new FormAttachment(middle, 0);
		fdlSpecificCodePage.top = new FormAttachment(wCodePage, margin);
		wlSpecificCodePage.setLayoutData(fdlSpecificCodePage);

		wSpecificCodePage = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSpecificCodePage);
		wSpecificCodePage.addModifyListener(lsMod);
		fdSpecificCodePage = new FormData();
		fdSpecificCodePage.left = new FormAttachment(middle, 0);
		fdSpecificCodePage.top = new FormAttachment(wCodePage, margin);
		fdSpecificCodePage.right = new FormAttachment(100, 0);
		wSpecificCodePage.setLayoutData(fdSpecificCodePage);

		// FortmatFilename line
		wlFortmatFilename = new Label(shell, SWT.RIGHT);
		wlFortmatFilename.setText(Messages.getString("JobMssqlBulkLoad.FortmatFilename.Label"));
		props.setLook(wlFortmatFilename);
		fdlFortmatFilename = new FormData();
		fdlFortmatFilename.left = new FormAttachment(0, 0);
		fdlFortmatFilename.top = new FormAttachment(wSpecificCodePage, margin);
		fdlFortmatFilename.right = new FormAttachment(middle, -margin);
		wlFortmatFilename.setLayoutData(fdlFortmatFilename);

		wbFortmatFilename = new Button(shell, SWT.PUSH | SWT.CENTER);
		props.setLook(wbFortmatFilename);
		wbFortmatFilename.setText(Messages.getString("System.Button.Browse"));
		fdbFortmatFilename = new FormData();
		fdbFortmatFilename.right = new FormAttachment(100, 0);
		fdbFortmatFilename.top = new FormAttachment(wSpecificCodePage, 0);
		wbFortmatFilename.setLayoutData(fdbFortmatFilename);

		wFortmatFilename = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFortmatFilename);
		wFortmatFilename.addModifyListener(lsMod);
		fdFortmatFilename = new FormData();
		fdFortmatFilename.left = new FormAttachment(middle, 0);
		fdFortmatFilename.top = new FormAttachment(wSpecificCodePage, margin);
		fdFortmatFilename.right = new FormAttachment(wbFortmatFilename, -margin);
		wFortmatFilename.setLayoutData(fdFortmatFilename);

		// Whenever something changes, set the tooltip to the expanded version:
		wFortmatFilename.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				wFortmatFilename.setToolTipText(jobMeta.environmentSubstitute(wFortmatFilename.getText()));
			}
		});

		wbFortmatFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.txt", "*.csv", "*" });
				if (wFortmatFilename.getText() != null)
				{
					dialog.setFileName(jobMeta.environmentSubstitute(wFortmatFilename.getText()));
				}
				dialog.setFilterNames(FILETYPES);
				if (dialog.open() != null)
				{
					wFortmatFilename.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
						+ dialog.getFileName());
				}
			}
		});

		
		
		
		//Fire Triggers?
		wlFireTriggers = new Label(shell, SWT.RIGHT);
		wlFireTriggers.setText(Messages.getString("JobMssqlBulkLoad.FireTriggers.Label"));
		props.setLook(wlFireTriggers);
		fdlFireTriggers = new FormData();
		fdlFireTriggers.left = new FormAttachment(0, 0);
		fdlFireTriggers.top = new FormAttachment(wFortmatFilename, margin);
		fdlFireTriggers.right = new FormAttachment(middle, -margin);
		wlFireTriggers.setLayoutData(fdlFireTriggers);
		wFireTriggers = new Button(shell, SWT.CHECK);
		props.setLook(wFireTriggers);
		wFireTriggers.setToolTipText(Messages.getString("JobMssqlBulkLoad.FireTriggers.Tooltip"));
		fdFireTriggers = new FormData();
		fdFireTriggers.left = new FormAttachment(middle, 0);
		fdFireTriggers.top = new FormAttachment(wFortmatFilename, margin);
		fdFireTriggers.right = new FormAttachment(100, 0);
		wFireTriggers.setLayoutData(fdFireTriggers);
		wFireTriggers.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		

		// CHECK CONSTRAINTS
		wlCheckConstraints = new Label(shell, SWT.RIGHT);
		wlCheckConstraints.setText(Messages.getString("JobMssqlBulkLoad.CheckConstraints.Label"));
		props.setLook(wlCheckConstraints);
		fdlCheckConstraints = new FormData();
		fdlCheckConstraints.left = new FormAttachment(0, 0);
		fdlCheckConstraints.top = new FormAttachment(wFireTriggers, margin);
		fdlCheckConstraints.right = new FormAttachment(middle, -margin);
		wlCheckConstraints.setLayoutData(fdlCheckConstraints);
		wCheckConstraints = new Button(shell, SWT.CHECK);
		props.setLook(wCheckConstraints);
		wCheckConstraints.setToolTipText(Messages.getString("JobMssqlBulkLoad.CheckConstraints.Tooltip"));
		fdCheckConstraints = new FormData();
		fdCheckConstraints.left = new FormAttachment(middle, 0);
		fdCheckConstraints.top = new FormAttachment(wFireTriggers, margin);
		fdCheckConstraints.right = new FormAttachment(100, 0);
		wCheckConstraints.setLayoutData(fdCheckConstraints);
		wCheckConstraints.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		// Keep Nulls
		wlKeepNulls = new Label(shell, SWT.RIGHT);
		wlKeepNulls.setText(Messages.getString("JobMssqlBulkLoad.KeepNulls.Label"));
		props.setLook(wlKeepNulls);
		fdlKeepNulls = new FormData();
		fdlKeepNulls.left = new FormAttachment(0, 0);
		fdlKeepNulls.top = new FormAttachment(wCheckConstraints, margin);
		fdlKeepNulls.right = new FormAttachment(middle, -margin);
		wlKeepNulls.setLayoutData(fdlKeepNulls);
		wKeepNulls = new Button(shell, SWT.CHECK);
		props.setLook(wKeepNulls);
		wKeepNulls.setToolTipText(Messages.getString("JobMssqlBulkLoad.KeepNulls.Tooltip"));
		fdKeepNulls = new FormData();
		fdKeepNulls.left = new FormAttachment(middle, 0);
		fdKeepNulls.top = new FormAttachment(wCheckConstraints, margin);
		fdKeepNulls.right = new FormAttachment(100, 0);
		wKeepNulls.setLayoutData(fdKeepNulls);
		wKeepNulls.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		// TABBLOCK
		wlTablock = new Label(shell, SWT.RIGHT);
		wlTablock.setText(Messages.getString("JobMssqlBulkLoad.Tablock.Label"));
		props.setLook(wlTablock);
		fdlTablock = new FormData();
		fdlTablock.left = new FormAttachment(0, 0);
		fdlTablock.top = new FormAttachment(wKeepNulls, margin);
		fdlTablock.right = new FormAttachment(middle, -margin);
		wlTablock.setLayoutData(fdlTablock);
		wTablock = new Button(shell, SWT.CHECK);
		props.setLook(wTablock);
		wTablock.setToolTipText(Messages.getString("JobMssqlBulkLoad.Tablock.Tooltip"));
		fdTablock = new FormData();
		fdTablock.left = new FormAttachment(middle, 0);
		fdTablock.top = new FormAttachment(wKeepNulls, margin);
		fdTablock.right = new FormAttachment(100, 0);
		wTablock.setLayoutData(fdTablock);
		wTablock.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		
		// Nbr of lines to take
		wlTakelines = new Label(shell, SWT.RIGHT);
		wlTakelines.setText(Messages.getString("JobMssqlBulkLoad.Takelines.Label"));
		props.setLook(wlTakelines);
		fdlTakelines = new FormData();
		fdlTakelines.left = new FormAttachment(0, 0);
		fdlTakelines.right = new FormAttachment(middle, 0);
		fdlTakelines.top = new FormAttachment(wTablock, margin);
		wlTakelines.setLayoutData(fdlTakelines);

		wTakelines = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wTakelines);
		wTakelines.addModifyListener(lsMod);
		fdTakelines = new FormData();
		fdTakelines.left = new FormAttachment(middle, 0);
		fdTakelines.top = new FormAttachment(wTablock, margin);
		fdTakelines.right = new FormAttachment(100, 0);
		wTakelines.setLayoutData(fdTakelines);
		
		
		// Specifies how the data in the data file is sorted
		wlOrderBy = new Label(shell, SWT.RIGHT);
		wlOrderBy.setText(Messages.getString("JobMssqlBulkLoad.OrderBy.Label"));
		props.setLook(wlOrderBy);
		fdlOrderBy = new FormData();
		fdlOrderBy.left = new FormAttachment(0, 0);
		fdlOrderBy.right = new FormAttachment(middle, 0);
		fdlOrderBy.top = new FormAttachment(wTakelines, margin);
		wlOrderBy.setLayoutData(fdlOrderBy);
		
		wbOrderBy=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbOrderBy);
		wbOrderBy.setText(Messages.getString("System.Button.Edit"));
		FormData fdbListattribut = new FormData();
		fdbListattribut.right= new FormAttachment(100, 0);
		fdbListattribut.top  = new FormAttachment(wTakelines, margin);
		wbOrderBy.setLayoutData(fdbListattribut);
		wbOrderBy.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getListColumns(); } } );

		wOrderBy = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wOrderBy);
		wOrderBy.setToolTipText(Messages.getString("JobMssqlBulkLoad.OrderBy.Tooltip"));
		wOrderBy.addModifyListener(lsMod);
		fdOrderBy = new FormData();
		fdOrderBy.left = new FormAttachment(middle, 0);
		fdOrderBy.top = new FormAttachment(wTakelines, margin);
		fdOrderBy.right = new FormAttachment(wbOrderBy, -margin);
		wOrderBy.setLayoutData(fdOrderBy);
		
		
		// Order Direction
		wlOrderDirection = new Label(shell, SWT.RIGHT);
		wlOrderDirection.setText(Messages.getString("JobMysqlBulkLoad.OrderDirection.Label"));
		props.setLook(wlOrderDirection);
		fdlOrderDirection = new FormData();
		fdlOrderDirection.left = new FormAttachment(0, 0);
		fdlOrderDirection.right = new FormAttachment(middle, 0);
		fdlOrderDirection.top = new FormAttachment(wOrderBy, margin);
		wlOrderDirection.setLayoutData(fdlOrderDirection);
		wOrderDirection = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
					wOrderDirection.add(Messages.getString("JobMysqlBulkLoad.OrderDirectionAsc.Label"));
					wOrderDirection.add(Messages.getString("JobMysqlBulkLoad.OrderDirectionDesc.Label"));
					wOrderDirection.select(0); // +1: starts at -1

		props.setLook(wOrderDirection);
		fdOrderDirection= new FormData();
		fdOrderDirection.left = new FormAttachment(middle, 0);
		fdOrderDirection.top = new FormAttachment(wOrderBy, margin);
		fdOrderDirection.right = new FormAttachment(100, 0);
		wOrderDirection.setLayoutData(fdOrderDirection);
		
		
		
		 // fileresult grouping?
	     // ////////////////////////
	     // START OF FILE RESULT GROUP///
	     // /
	    wFileResult = new Group(shell, SWT.SHADOW_NONE);
	    props.setLook(wFileResult);
	    wFileResult.setText(Messages.getString("JobMssqlBulkLoad.FileResult.Group.Label"));

	    FormLayout groupLayout = new FormLayout();
	    groupLayout.marginWidth = 10;
	    groupLayout.marginHeight = 10;

	    wFileResult.setLayout(groupLayout);
	      
	      
	  	//Add file to result
		wlAddFileToResult = new Label(wFileResult, SWT.RIGHT);
		wlAddFileToResult.setText(Messages.getString("JobMssqlBulkLoad.AddFileToResult.Label"));
		props.setLook(wlAddFileToResult);
		fdlAddFileToResult = new FormData();
		fdlAddFileToResult.left = new FormAttachment(0, 0);
		fdlAddFileToResult.top = new FormAttachment(wOrderDirection, margin);
		fdlAddFileToResult.right = new FormAttachment(middle, -margin);
		wlAddFileToResult.setLayoutData(fdlAddFileToResult);
		wAddFileToResult = new Button(wFileResult, SWT.CHECK);
		props.setLook(wAddFileToResult);
		wAddFileToResult.setToolTipText(Messages.getString("JobMssqlBulkLoad.AddFileToResult.Tooltip"));
		fdAddFileToResult = new FormData();
		fdAddFileToResult.left = new FormAttachment(middle, 0);
		fdAddFileToResult.top = new FormAttachment(wOrderDirection, margin);
		fdAddFileToResult.right = new FormAttachment(100, 0);
		wAddFileToResult.setLayoutData(fdAddFileToResult);
		wAddFileToResult.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
	      
	      
	     fdFileResult = new FormData();
	     fdFileResult.left = new FormAttachment(0, margin);
	     fdFileResult.top = new FormAttachment(wOrderDirection, margin);
	     fdFileResult.right = new FormAttachment(100, -margin);
	     wFileResult.setLayoutData(fdFileResult);
	     // ///////////////////////////////////////////////////////////
	     // / END OF FilesRsult GROUP
	     // ///////////////////////////////////////////////////////////

		

		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));

		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));
		
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wFileResult);
		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wName.addSelectionListener(lsDef);
		wTablename.addSelectionListener(lsDef);
	
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		
		getData();

		BaseStepDialog.setSize(shell);

		shell.open();
		props.setDialogSize(shell, "JobMssqlBulkLoadDialogSize");
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
		// System.out.println("evaluates: "+jobentry.evaluates());

		if (jobEntry.getName() != null)
			wName.setText(jobEntry.getName());
		if (jobEntry.getSchemaname() != null)
			wSchemaname.setText(jobEntry.getSchemaname());
		if (jobEntry.getTablename() != null)
			wTablename.setText(jobEntry.getTablename());
		if (jobEntry.getFilename() != null)
			wFilename.setText(jobEntry.getFilename());
		if (jobEntry.getSeparator() != null)
			wSeparator.setText(jobEntry.getSeparator());
		
		if (jobEntry.getLineterminated() != null)
			wLineterminated.setText(jobEntry.getLineterminated());			


	
		if (jobEntry.getTakelines() != null)
		{

			wTakelines.setText(jobEntry.getTakelines());
		}
		else
			wTakelines.setText("0");
		
		if (jobEntry.getOrderBy() != null)
			wOrderBy.setText(jobEntry.getOrderBy());
		     
		
		if (jobEntry.getDatabase() != null)
		{
			wConnection.setText(jobEntry.getDatabase().getName());
		}
		
		wAddFileToResult.setSelection(jobEntry.isAddFileToResult());
		
		wName.selectAll();
	}

	private void cancel()
	{
		jobEntry.setChanged(changed);
		jobEntry = null;
		dispose();
	}

	private void ok()
	{
		jobEntry.setName(wName.getText());
		jobEntry.setDatabase(jobMeta.findDatabase(wConnection.getText()));
		jobEntry.setSchemaname(wSchemaname.getText());
		jobEntry.setTablename(wTablename.getText());
		jobEntry.setFilename(wFilename.getText());
		jobEntry.setSeparator(wSeparator.getText());
		jobEntry.setLineterminated(wLineterminated.getText());
		jobEntry.setTakelines(wTakelines.getText());
		jobEntry.setOrderBy(wOrderBy.getText());
		
		jobEntry.setAddFileToResult(wAddFileToResult.getSelection());
		
		dispose();
	}

	public String toString()
	{
		return this.getClass().getName();
	}
	
	private void getTableName()
	{
		// New class: SelectTableDialog
		int connr = wConnection.getSelectionIndex();
		if (connr>=0)
		{
			DatabaseMeta inf = jobMeta.getDatabase(connr);
                        
			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, jobMeta.getDatabases());
			std.setSelectedSchema(wSchemaname.getText());
			std.setSelectedTable(wTablename.getText());
			std.setSplitSchemaAndTable(true);
			if (std.open() != null)
			{
				wTablename.setText(Const.NVL(std.getTableName(), ""));
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("JobMssqlBulkLoad.ConnectionError2.DialogMessage"));
			mb.setText(Messages.getString("System.Dialog.Error.Title"));
			mb.open(); 
		}                    
	}

	/**
	 * Get a list of columns, comma separated, allow the user to select from it.
	 */
	private void getListColumns()
	{
		if (!Const.isEmpty(wTablename.getText()))
		{
			DatabaseMeta databaseMeta = jobMeta.findDatabase(wConnection.getText());
			if (databaseMeta!=null)
			{
				Database database = new Database(databaseMeta);
				database.shareVariablesWith(jobMeta);
				try
				{
					database.connect();
					String schemaTable = databaseMeta.getQuotedSchemaTableCombination(wSchemaname.getText(), wTablename.getText());
					RowMetaInterface row = database.getTableFields(schemaTable);
					String available[] = row.getFieldNames();
                    
					String source[] = wOrderBy.getText().split(",");
					for (int i=0;i<source.length;i++) source[i] = Const.trim(source[i]);
					int idxSource[] = Const.indexsOfStrings(source, available);
					EnterSelectionDialog dialog = new EnterSelectionDialog(shell, available, Messages.getString("JobMssqlBulkLoad.SelectColumns.Title"), Messages.getString("JobMssqlBulkLoad.SelectColumns.Message"));
					dialog.setMulti(true);
					dialog.setSelectedNrs(idxSource);
					if (dialog.open()!=null)
					{
						String columns="";
						int idx[] = dialog.getSelectionIndeces();
						for (int i=0;i<idx.length;i++)
						{
							if (i>0) columns+=", ";
							columns+=available[idx[i]];
						}
						wOrderBy.setText(columns);
					}
				}
				catch(KettleDatabaseException e)
				{
					new ErrorDialog(shell, Messages.getString("System.Dialog.Error.Title"), Messages.getString("JobMssqlBulkLoad.ConnectionError2.DialogMessage"), e);
				}
				finally
				{
					database.disconnect();
				}
			}
		}
	}	
}