 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar HASSAN.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/


package org.pentaho.di.ui.job.entries.checkdbconnection;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.MessageBox; 

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.checkdbconnection.JobEntryCheckDbConnections;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit the check database connection job entry settings.
 * 
 * @author Samatar
 * @since 12-10-2007
 */

public class JobEntryCheckDbConnectionsDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryCheckDbConnections.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryCheckDbConnections jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;

    private boolean changed;
	private Label wlFields;
	private TableView wFields;
	private FormData fdlFields, fdFields;
	
	private Button       wbdSourceFileFolder; // Delete
	private FormData fdbdSourceFileFolder;
	

	private Button       wbgetConnections; // Get connections
	private FormData fdbgetConnections;
	
    private String[]     connections;

    public JobEntryCheckDbConnectionsDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryCheckDbConnections) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(BaseMessages.getString(PKG, "JobCheckDbConnections.Name.Default"));
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
        shell.setText(BaseMessages.getString(PKG, "JobCheckDbConnections.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Filename line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(BaseMessages.getString(PKG, "JobCheckDbConnections.Name.Label"));
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
		

		wlFields = new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "JobCheckDbConnections.Fields.Label"));
		props.setLook(wlFields);
		fdlFields = new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		//fdlFields.right= new FormAttachment(middle, -margin);
		fdlFields.top = new FormAttachment(wName,2*margin);
		wlFields.setLayoutData(fdlFields);

		// Buttons to the right of the screen...
		wbdSourceFileFolder=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbdSourceFileFolder);
		wbdSourceFileFolder.setText(BaseMessages.getString(PKG, "JobCheckDbConnections.DeleteEntry"));
		wbdSourceFileFolder.setToolTipText(BaseMessages.getString(PKG, "JobCheckDbConnections.DeleteSourceFileButton.Label"));
		fdbdSourceFileFolder=new FormData();
		fdbdSourceFileFolder.right = new FormAttachment(100, -margin);
		fdbdSourceFileFolder.top  = new FormAttachment (wlFields, 50);
		wbdSourceFileFolder.setLayoutData(fdbdSourceFileFolder);
		
		
		// Buttons to the right of the screen...
		wbgetConnections=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbgetConnections);
		wbgetConnections.setText(BaseMessages.getString(PKG, "JobCheckDbConnections.GetConnections"));
		wbgetConnections.setToolTipText(BaseMessages.getString(PKG, "JobCheckDbConnections.GetConnections.Tooltip"));
		fdbgetConnections=new FormData();
		fdbgetConnections.right = new FormAttachment(100, -margin);
		fdbgetConnections.top  = new FormAttachment (wlFields, 20);
		wbgetConnections.setLayoutData(fdbgetConnections);

		addDatabases();
		
		int rows = jobEntry.connections == null
		? 1
		: (jobEntry.connections.length == 0
		? 0
		: jobEntry.connections.length);
		
		final int FieldsRows = rows;

		ColumnInfo[] colinf=new ColumnInfo[]
			{
				new ColumnInfo(BaseMessages.getString(PKG, "JobCheckDbConnections.Fields.Argument.Label"),  ColumnInfo.COLUMN_TYPE_CCOMBO, connections, false),
				new ColumnInfo(BaseMessages.getString(PKG, "JobCheckDbConnections.Fields.WaitFor.Label"),  ColumnInfo.COLUMN_TYPE_TEXT, false),
				new ColumnInfo(BaseMessages.getString(PKG, "JobCheckDbConnections.Fields.WaitForTime.Label"),  ColumnInfo.COLUMN_TYPE_CCOMBO, JobEntryCheckDbConnections.unitTimeDesc, false),
			};

		colinf[0].setToolTip(BaseMessages.getString(PKG, "JobCheckDbConnections.Fields.Column"));
		colinf[1].setUsingVariables(true);
		colinf[1].setToolTip(BaseMessages.getString(PKG, "JobCheckDbConnections.WaitFor.ToolTip"));

		wFields = new TableView(jobMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf,
			FieldsRows, lsMod, props);

		fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(wbgetConnections, -margin);
		fdFields.bottom = new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        FormData fd = new FormData();
        fd.right = new FormAttachment(50, -10);
        fd.bottom = new FormAttachment(100, 0);
        fd.width = 100;
        wOK.setLayoutData(fd);

        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
        fd = new FormData();
        fd.left = new FormAttachment(50, 10);
        fd.bottom = new FormAttachment(100, 0);
        fd.width = 100;
        wCancel.setLayoutData(fd);
        
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
     // Delete files from the list of files...
		wbdSourceFileFolder.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				int idx[] = wFields.getSelectionIndices();
				wFields.remove(idx);
				wFields.removeEmptyRows();
				wFields.setRowNums();
			}
		});
		
		 // get connections...
		wbgetConnections.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				getDatabases();
			}
		});
        

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
        props.setDialogSize(shell, "JobCheckDbConnectionsDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }
    public void addDatabases() {
    	connections = new String[jobMeta.nrDatabases()];
        for (int i = 0; i < jobMeta.nrDatabases(); i++) {
          DatabaseMeta ci = jobMeta.getDatabase(i);
        	  connections[i]=ci.getName();
        }
      }
    public void getDatabases() 
    {
        wFields.removeAll();
        for (int i = 0; i < jobMeta.nrDatabases(); i++) {
			DatabaseMeta ci = jobMeta.getDatabase(i);
			if (ci != null){
				wFields.add(new String[]{ci.getName(), "0", JobEntryCheckDbConnections.unitTimeDesc[0]});
			}
		}
        wFields.removeEmptyRows();
		wFields.setRowNums();
		wFields.optWidth(true); 
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
        
        if (jobEntry.connections != null)
		{
			for (int i = 0; i < jobEntry.connections.length; i++)
			{
				TableItem ti = wFields.table.getItem(i);
				if (jobEntry.connections[i] != null)
				{
					ti.setText(1, jobEntry.connections[i].getName());
					ti.setText(2, ""+Const.toInt(jobEntry.waitfors[i],0));
					ti.setText(3,JobEntryCheckDbConnections.getWaitTimeDesc(jobEntry.waittimes[i]));
				}
			}
			wFields.setRowNums();
			wFields.optWidth(true);
		}
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
        
        int nritems = wFields.nrNonEmpty();

		jobEntry.connections = new DatabaseMeta[nritems];
		jobEntry.waitfors = new String[nritems];
		jobEntry.waittimes = new int[nritems];

		for (int i = 0; i < nritems; i++)
		{
			String arg = wFields.getNonEmpty(i).getText(1);
			DatabaseMeta dbMeta=jobMeta.findDatabase(arg);
			if (dbMeta != null)
			{
				jobEntry.connections[i] = dbMeta;
				jobEntry.waitfors[i]=""+Const.toInt(wFields.getNonEmpty(i).getText(2),0);
				jobEntry.waittimes[i]=JobEntryCheckDbConnections.getWaitTimeByDesc(wFields.getNonEmpty(i).getText(3));
			}
		}
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
