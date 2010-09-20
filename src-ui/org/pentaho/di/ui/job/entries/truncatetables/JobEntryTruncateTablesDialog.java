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


package org.pentaho.di.ui.job.entries.truncatetables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox; 
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.core.Const;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog; 
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.job.entries.truncatetables.JobEntryTruncateTables;
import org.pentaho.di.job.entries.truncatetables.Messages;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.ui.core.dialog.ErrorDialog;

/**
 * This dialog allows you to edit the Truncate Tables job entry settings. (select the connection and
 * the table to be truncated) 
 * 
 * @author Samatar
 * @since 22-07-2008
 */
public class JobEntryTruncateTablesDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private Button wbTable;
	
    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;

    private CCombo wConnection;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryTruncateTables jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;

    private boolean changed;
	
	private Label wlFields;
	private TableView wFields;
	private FormData fdlFields, fdFields;
	
	private Button   wbdTablename; 
	private FormData fdbdTablename;
	
	private Label wlPrevious;
	private Button wPrevious;
	private FormData fdlPrevious, fdPrevious;

    public JobEntryTruncateTablesDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryTruncateTables) jobEntryInt;
        if (this.jobEntry.getName() == null)
			this.jobEntry.setName(Messages.getString("JobTruncateTables.Name.Default"));
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
        shell.setText(Messages.getString("JobTruncateTables.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Filename line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(Messages.getString("JobTruncateTables.Name.Label"));
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
		
		// Connection line
		wConnection = addConnectionLine(shell, wName, middle, margin);
		if (jobEntry.getDatabase()==null && jobMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);
		
		wlPrevious = new Label(shell, SWT.RIGHT);
		wlPrevious.setText(Messages.getString("JobTruncateTables.Previous.Label"));
		props.setLook(wlPrevious);
		fdlPrevious = new FormData();
		fdlPrevious.left = new FormAttachment(0, 0);
		fdlPrevious.top = new FormAttachment(wConnection, margin );
		fdlPrevious.right = new FormAttachment(middle, -margin);
		wlPrevious.setLayoutData(fdlPrevious);
		wPrevious = new Button(shell, SWT.CHECK);
		props.setLook(wPrevious);
		wPrevious.setToolTipText(Messages.getString("JobTruncateTables.Previous.Tooltip"));
		fdPrevious = new FormData();
		fdPrevious.left = new FormAttachment(middle, 0);
		fdPrevious.top = new FormAttachment(wConnection, margin );
		fdPrevious.right = new FormAttachment(100, 0);
		wPrevious.setLayoutData(fdPrevious);
		wPrevious.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				
				setPrevious();				
				jobEntry.setChanged();
			}
		});
		
        
		wbTable=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbTable);
		wbTable.setText(Messages.getString("JobTruncateTables.GetTablenamesList.Auto"));
		FormData fdbTable = new FormData();
		fdbTable.left= new FormAttachment(0, margin);
		fdbTable.right= new FormAttachment(100, -margin);
		fdbTable.top  = new FormAttachment(wPrevious, 2*margin);
		wbTable.setLayoutData(fdbTable);
		wbTable.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getTableName(); } } );

		// Buttons to the right of the screen...
		wbdTablename=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbdTablename);
		wbdTablename.setText(Messages.getString("JobTruncateTables.TableDelete.Button"));
		wbdTablename.setToolTipText(Messages.getString("JobTruncateTables.TableDelete.Tooltip"));
		fdbdTablename=new FormData();
		fdbdTablename.right = new FormAttachment(100, 0);
		fdbdTablename.top  = new FormAttachment (wbTable, 2*middle);
		wbdTablename.setLayoutData(fdbdTablename);
        
        wlFields = new Label(shell, SWT.NONE);
		wlFields.setText(Messages.getString("JobTruncateTables.Fields.Label"));
		props.setLook(wlFields);
		fdlFields = new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.right= new FormAttachment(middle, -margin);
		fdlFields.top = new FormAttachment(wbTable,2*margin);
		wlFields.setLayoutData(fdlFields);

		int rows =jobEntry.arguments == null
			? 1
			: (jobEntry.arguments.length == 0
			? 0
			: jobEntry.arguments.length);
		final int FieldsRows = rows;

		ColumnInfo[] colinf=new ColumnInfo[]
			{
				new ColumnInfo(Messages.getString("JobTruncateTables.Fields.Table.Label"),  ColumnInfo.COLUMN_TYPE_TEXT,    false),
				new ColumnInfo(Messages.getString("JobTruncateTables.Fields.Schema.Label"), ColumnInfo.COLUMN_TYPE_TEXT,    false ),
			};

		colinf[0].setUsingVariables(true);
		colinf[0].setToolTip(Messages.getString("JobTruncateTables.Fields.Table.Tooltip"));
		colinf[1].setUsingVariables(true);
		colinf[1].setToolTip(Messages.getString("JobTruncateTables.Fields.Schema.Tooltip"));

		wFields = new TableView(jobMeta,shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf,
			FieldsRows, lsMod, props);

		fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(wbdTablename, -margin);
		fdFields.bottom = new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);
		
		// Delete files from the list of files...
		wbdTablename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				int idx[] = wFields.getSelectionIndices();
				wFields.remove(idx);
				wFields.removeEmptyRows();
				wFields.setRowNums();
			}
		});

		

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        FormData fd = new FormData();
        fd.right = new FormAttachment(50, -10);
        fd.bottom = new FormAttachment(100, 0);
        fd.width = 100;
        wOK.setLayoutData(fd);

        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));
        fd = new FormData();
        fd.left = new FormAttachment(50, 10);
        fd.bottom = new FormAttachment(100, 0);
        fd.width = 100;
        wCancel.setLayoutData(fd);

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
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wFields);
        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };

        wName.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        getData();
        setPrevious();
        BaseStepDialog.setSize(shell);

        shell.open();
        props.setDialogSize(shell, "JobTruncateTablesDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }
    private void setPrevious()
    {
    	wlFields.setEnabled(!wPrevious.getSelection());
    	wFields.setEnabled(!wPrevious.getSelection());
    	wbdTablename.setEnabled(!wPrevious.getSelection());
    	wbTable.setEnabled(!wPrevious.getSelection());
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
        if (jobEntry.getName() != null) wName.setText(jobEntry.getName());
        if (jobEntry.getDatabase() != null)
            wConnection.setText(jobEntry.getDatabase().getName());
        if (jobEntry.arguments != null)
		{
			for (int i = 0; i < jobEntry.arguments.length; i++)
			{
				//TableItem ti = new TableItem(wFields.table, SWT.NONE);
				TableItem ti = wFields.table.getItem(i);
				if (jobEntry.arguments[i] != null) ti.setText(1, jobEntry.arguments[i]);
				if (jobEntry.schemaname[i] != null) ti.setText(2, jobEntry.schemaname[i]);
			}

		    wFields.removeEmptyRows();
		    wFields.setRowNums();
		    wFields.optWidth(true);
		}
        wPrevious.setSelection(jobEntry.argFromPrevious);        
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
 	   if(Const.isEmpty(wName.getText())) 
       {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setText(Messages.getString("System.StepJobEntryNameMissing.Title"));
			mb.setMessage(Messages.getString("System.JobEntryNameMissing.Msg"));
			mb.open(); 
			return;
       }
        jobEntry.setName(wName.getText());
        jobEntry.setDatabase(jobMeta.findDatabase(wConnection.getText()));
        jobEntry.argFromPrevious=wPrevious.getSelection();

        int nritems = wFields.nrNonEmpty();
		int nr = 0;
		for (int i = 0; i < nritems; i++)
		{
			String arg = wFields.getNonEmpty(i).getText(1);
			if (arg != null && arg.length() != 0) nr++;
		}
		jobEntry.arguments = new String[nr];
		jobEntry.schemaname = new String[nr];
		nr = 0;
		for (int i = 0; i < nritems; i++)
		{
			String arg = wFields.getNonEmpty(i).getText(1);
			String wild = wFields.getNonEmpty(i).getText(2);
			if (arg != null && arg.length() != 0)
			{
				jobEntry.arguments[nr] = arg;
				jobEntry.schemaname[nr] = wild;
				nr++;
			}
		}
		
        dispose();
    }

    public String toString()
    {
        return this.getClass().getName();
    }
    private void getTableName()
	{
		DatabaseMeta databaseMeta = jobMeta.findDatabase(wConnection.getText());
		if (databaseMeta!=null)
		{
			Database database = new Database(databaseMeta);
			database.shareVariablesWith(jobMeta);
			try
			{
				database.connect();
				String Tablenames[]=database.getTablenames();
				EnterSelectionDialog dialog = new EnterSelectionDialog(shell, Tablenames, Messages.getString("JobTruncateTables.SelectTables.Title"), Messages.getString("JobTruncateTables.SelectTables.Message"));
				dialog.setMulti(true);

				if (dialog.open()!=null)
				{
					int idx[] = dialog.getSelectionIndeces();
					for (int i=0;i<idx.length;i++)
					{
						TableItem tableItem = new TableItem(wFields.table, SWT.NONE);
						tableItem.setText(1, Tablenames[idx[i]]);
					}
				}
			}
			catch(KettleDatabaseException e)
			{
				new ErrorDialog(shell, Messages.getString("System.Dialog.Error.Title"), Messages.getString("JobEntryTruncateTables.ConnectionError.DialogMessage"), e);
			}
			finally
			{
				if(database!=null) database.disconnect();
			}
		    wFields.removeEmptyRows();
		    wFields.setRowNums();
		    wFields.optWidth(true);
		 
		}
		
}
}