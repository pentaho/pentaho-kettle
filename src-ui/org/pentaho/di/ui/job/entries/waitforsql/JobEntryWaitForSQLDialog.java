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

/*
 * Created on 27-10-2008
 *
 */

package org.pentaho.di.ui.job.entries.waitforsql;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.waitforsql.JobEntryWaitForSQL;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.steps.tableinput.SQLValuesHighlight;

/**
 * This dialog allows you to edit the Wait for SQL job entry settings.
 * 
 * @author Samatar
 * @since 27-10-2008
 */
public class JobEntryWaitForSQLDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryWaitForSQL.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Button wbTable,wbSQLTable;
	
    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;


    private CCombo wConnection;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel,lsbSQLTable;

    private JobEntryWaitForSQL jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;

    private boolean changed;
	
    private Label wlUseSubs;

    private Button wUseSubs;

    private FormData fdlUseSubs, fdUseSubs;
    
    private Label wlAddRowsToResult;

    private Button wAddRowsToResult;

    private FormData fdlAddRowsToResult, fdAddRowsToResult;
    
    private Label wlcustomSQL;

    private Button wcustomSQL;

    private FormData fdlcustomSQL, fdcustomSQL;
    
    private FormData fdlSQL, fdSQL;
    
    private Label wlSQL;

    private StyledTextComp wSQL;

    private Label wlPosition;

    private FormData fdlPosition;
	
	private Group wSuccessGroup;
    private FormData fdSuccessGroup;

	// Schema name
	private Label wlSchemaname;
	private TextVar wSchemaname;
	private FormData fdlSchemaname, fdSchemaname;

	private Label wlTablename;
	private TextVar wTablename;
	private FormData fdlTablename, fdTablename;
	
	private Group wCustomGroup;
    private FormData fdCustomGroup;
    
	private Label wlSuccessCondition;
	private CCombo wSuccessCondition;
	private FormData fdlSuccessCondition, fdSuccessCondition;
	
	
	private Label wlRowsCountValue;
	private TextVar wRowsCountValue;
	private FormData fdlRowsCountValue, fdRowsCountValue;
	
    private Label        wlMaximumTimeout;
    private TextVar      wMaximumTimeout;
    private FormData     fdlMaximumTimeout, fdMaximumTimeout;

    private Label        wlCheckCycleTime;
    private TextVar      wCheckCycleTime;
    private FormData     fdlCheckCycleTime, fdCheckCycleTime;    
    
    private Label        wlSuccesOnTimeout;
    private Button       wSuccesOnTimeout;
    private FormData     fdlSuccesOnTimeout, fdSuccesOnTimeout;
    
    private Label wlClearResultList;
    private Button wClearResultList;
    private FormData fdlClearResultList, fdClearResultList;

	private SQLValuesHighlight lineStyler = new SQLValuesHighlight();
	 
    public JobEntryWaitForSQLDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryWaitForSQL) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(BaseMessages.getString(PKG, "JobEntryWaitForSQL.Name.Default"));
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
        shell.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;
        
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


        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);

        
        // Filename line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.Name.Label"));
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
		// Schema name line
		wlSchemaname = new Label(shell, SWT.RIGHT);
		wlSchemaname.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.Schemaname.Label"));
		props.setLook(wlSchemaname);
		fdlSchemaname = new FormData();
		fdlSchemaname.left = new FormAttachment(0, 0);
		fdlSchemaname.right = new FormAttachment(middle, 0);
		fdlSchemaname.top = new FormAttachment(wConnection, margin);
		wlSchemaname.setLayoutData(fdlSchemaname);

		wSchemaname = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSchemaname);
		wSchemaname.setToolTipText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.Schemaname.Tooltip"));
		wSchemaname.addModifyListener(lsMod);
		fdSchemaname = new FormData();
		fdSchemaname.left = new FormAttachment(middle, 0);
		fdSchemaname.top = new FormAttachment(wConnection, margin);
		fdSchemaname.right = new FormAttachment(100, 0);
		wSchemaname.setLayoutData(fdSchemaname);

		// Table name line
		wlTablename = new Label(shell, SWT.RIGHT);
		wlTablename.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.Tablename.Label"));
		props.setLook(wlTablename);
		fdlTablename = new FormData();
		fdlTablename.left = new FormAttachment(0, 0);
		fdlTablename.right = new FormAttachment(middle, 0);
		fdlTablename.top = new FormAttachment(wSchemaname, margin);
		wlTablename.setLayoutData(fdlTablename);

		wbTable=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbTable);
		wbTable.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		FormData fdbTable = new FormData();
		fdbTable.right= new FormAttachment(100, 0);
		fdbTable.top  = new FormAttachment(wSchemaname, margin/2);
		wbTable.setLayoutData(fdbTable);
		wbTable.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getTableName(); } } );

		wTablename = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wTablename);
		wTablename.setToolTipText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.Tablename.Tooltip"));
		wTablename.addModifyListener(lsMod);
		fdTablename = new FormData();
		fdTablename.left = new FormAttachment(middle, 0);
		fdTablename.top = new FormAttachment(wSchemaname, margin);
		fdTablename.right = new FormAttachment(wbTable, -margin);
		wTablename.setLayoutData(fdTablename);
		
	    // ////////////////////////
	    // START OF Success GROUP///
	    // ///////////////////////////////
	    wSuccessGroup = new Group(shell, SWT.SHADOW_NONE);
	    props.setLook(wSuccessGroup);
	    wSuccessGroup.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.SuccessGroup.Group.Label"));

	    FormLayout SuccessGroupLayout = new FormLayout();
	    SuccessGroupLayout .marginWidth = 10;
	    SuccessGroupLayout .marginHeight = 10;
	    wSuccessGroup.setLayout(SuccessGroupLayout );   


	    //Success Condition
	  	wlSuccessCondition = new Label(wSuccessGroup, SWT.RIGHT);
	  	wlSuccessCondition.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.SuccessCondition.Label"));
	  	props.setLook(wlSuccessCondition);
	  	fdlSuccessCondition = new FormData();
	  	fdlSuccessCondition.left = new FormAttachment(0, -margin);
	  	fdlSuccessCondition.right = new FormAttachment(middle, -2*margin);
	  	fdlSuccessCondition.top = new FormAttachment(0, margin);
	  	wlSuccessCondition.setLayoutData(fdlSuccessCondition);
	  	wSuccessCondition = new CCombo(wSuccessGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wSuccessCondition.setItems(JobEntryWaitForSQL.successConditionsDesc);
	  	wSuccessCondition.select(0); // +1: starts at -1
	  	
		props.setLook(wSuccessCondition);
		fdSuccessCondition= new FormData();
		fdSuccessCondition.left = new FormAttachment(middle, -margin);
		fdSuccessCondition.top = new FormAttachment(0, margin);
		fdSuccessCondition.right = new FormAttachment(100, 0);
		wSuccessCondition.setLayoutData(fdSuccessCondition);
		wSuccessCondition.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				//activeSuccessCondition();
				
			}
		});

		// Success when number of errors less than
		wlRowsCountValue= new Label(wSuccessGroup, SWT.RIGHT);
		wlRowsCountValue.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.RowsCountValue.Label"));
		props.setLook(wlRowsCountValue);
		fdlRowsCountValue= new FormData();
		fdlRowsCountValue.left = new FormAttachment(0, -margin);
		fdlRowsCountValue.top = new FormAttachment(wSuccessCondition, margin);
		fdlRowsCountValue.right = new FormAttachment(middle, -2*margin);
		wlRowsCountValue.setLayoutData(fdlRowsCountValue);
		
		
		wRowsCountValue= new TextVar(jobMeta, wSuccessGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER, 
				BaseMessages.getString(PKG, "JobEntryWaitForSQL.RowsCountValue.Tooltip"));
		props.setLook(wRowsCountValue);
		wRowsCountValue.addModifyListener(lsMod);
		fdRowsCountValue= new FormData();
		fdRowsCountValue.left = new FormAttachment(middle, -margin);
		fdRowsCountValue.top = new FormAttachment(wSuccessCondition, margin);
		fdRowsCountValue.right = new FormAttachment(100, 0);
		wRowsCountValue.setLayoutData(fdRowsCountValue);
		
		 // Maximum timeout
        wlMaximumTimeout = new Label(wSuccessGroup, SWT.RIGHT);
        wlMaximumTimeout.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.MaximumTimeout.Label"));
        props.setLook(wlMaximumTimeout);
        fdlMaximumTimeout = new FormData();
        fdlMaximumTimeout.left = new FormAttachment(0, -margin);
        fdlMaximumTimeout.top = new FormAttachment(wRowsCountValue, margin);
        fdlMaximumTimeout.right = new FormAttachment(middle, -2*margin);
        wlMaximumTimeout.setLayoutData(fdlMaximumTimeout);
        wMaximumTimeout = new TextVar(jobMeta, wSuccessGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wMaximumTimeout);
        wMaximumTimeout.setToolTipText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.MaximumTimeout.Tooltip"));
        wMaximumTimeout.addModifyListener(lsMod);
        fdMaximumTimeout = new FormData();
        fdMaximumTimeout.left = new FormAttachment(middle, -margin);
        fdMaximumTimeout.top = new FormAttachment(wRowsCountValue, margin);
        fdMaximumTimeout.right = new FormAttachment(100, 0);
        wMaximumTimeout.setLayoutData(fdMaximumTimeout);

        // Cycle time
        wlCheckCycleTime = new Label(wSuccessGroup, SWT.RIGHT);
        wlCheckCycleTime.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.CheckCycleTime.Label"));
        props.setLook(wlCheckCycleTime);
        fdlCheckCycleTime = new FormData();
        fdlCheckCycleTime.left = new FormAttachment(0, -margin);
        fdlCheckCycleTime.top = new FormAttachment(wMaximumTimeout, margin);
        fdlCheckCycleTime.right = new FormAttachment(middle, -2*margin);
        wlCheckCycleTime.setLayoutData(fdlCheckCycleTime);
        wCheckCycleTime = new TextVar(jobMeta, wSuccessGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wCheckCycleTime);
        wCheckCycleTime.setToolTipText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.CheckCycleTime.Tooltip"));
        wCheckCycleTime.addModifyListener(lsMod);
        fdCheckCycleTime = new FormData();
        fdCheckCycleTime.left = new FormAttachment(middle, -margin);
        fdCheckCycleTime.top = new FormAttachment(wMaximumTimeout, margin);
        fdCheckCycleTime.right = new FormAttachment(100, 0);
        wCheckCycleTime.setLayoutData(fdCheckCycleTime);
	        
        // Success on timeout		
        wlSuccesOnTimeout = new Label(wSuccessGroup, SWT.RIGHT);
        wlSuccesOnTimeout.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.SuccessOnTimeout.Label"));
        props.setLook(wlSuccesOnTimeout);
        fdlSuccesOnTimeout = new FormData();
        fdlSuccesOnTimeout.left = new FormAttachment(0, -margin);
        fdlSuccesOnTimeout.top = new FormAttachment(wCheckCycleTime, margin);
        fdlSuccesOnTimeout.right = new FormAttachment(middle, -2*margin);
        wlSuccesOnTimeout.setLayoutData(fdlSuccesOnTimeout);
        wSuccesOnTimeout = new Button(wSuccessGroup, SWT.CHECK);
        props.setLook(wSuccesOnTimeout);
        wSuccesOnTimeout.setToolTipText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.SuccessOnTimeout.Tooltip"));
        fdSuccesOnTimeout = new FormData();
        fdSuccesOnTimeout.left = new FormAttachment(middle, -margin);
        fdSuccesOnTimeout.top = new FormAttachment(wCheckCycleTime, margin);
        fdSuccesOnTimeout.right = new FormAttachment(100, -margin);
        wSuccesOnTimeout.setLayoutData(fdSuccesOnTimeout);
        wSuccesOnTimeout.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });

	    
	    
	    
	     fdSuccessGroup = new FormData();
	     fdSuccessGroup .left = new FormAttachment(0, margin);
	     fdSuccessGroup .top = new FormAttachment(wbTable, margin);
	     fdSuccessGroup .right = new FormAttachment(100, -margin);
	     wSuccessGroup.setLayoutData(fdSuccessGroup );
	     // ///////////////////////////////////////////////////////////
	     // / END OF SuccessGroup GROUP
	     // ///////////////////////////////////////////////////////////

	
	    
		
		
	    // ////////////////////////
	    // START OF Custom GROUP///
	    // ///////////////////////////////
	    wCustomGroup = new Group(shell, SWT.SHADOW_NONE);
	    props.setLook(wCustomGroup);
	    wCustomGroup.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.CustomGroup.Group.Label"));

	    FormLayout CustomGroupLayout = new FormLayout();
	    CustomGroupLayout .marginWidth = 10;
	    CustomGroupLayout .marginHeight = 10;
	    wCustomGroup.setLayout(CustomGroupLayout );
	    
	    

		
		  // custom SQL?
        wlcustomSQL= new Label(wCustomGroup, SWT.RIGHT);
        wlcustomSQL.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.customSQL.Label"));
        props.setLook(wlcustomSQL);
        fdlcustomSQL= new FormData();
        fdlcustomSQL.left = new FormAttachment(0, -margin);
        fdlcustomSQL.top = new FormAttachment(wSuccessGroup, margin);
        fdlcustomSQL.right = new FormAttachment(middle, -2*margin);
        wlcustomSQL.setLayoutData(fdlcustomSQL);
        wcustomSQL= new Button(wCustomGroup, SWT.CHECK);
        props.setLook(wcustomSQL);
        wcustomSQL.setToolTipText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.customSQL.Tooltip"));
        fdcustomSQL= new FormData();
        fdcustomSQL.left = new FormAttachment(middle, -margin);
        fdcustomSQL.top = new FormAttachment(wSuccessGroup, margin);
        fdcustomSQL.right = new FormAttachment(100, 0);
        wcustomSQL.setLayoutData(fdcustomSQL);
        wcustomSQL.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				
				setCustomerSQL();				
				jobEntry.setChanged();
			}
		}); 
        // use Variable substitution?
        wlUseSubs = new Label(wCustomGroup, SWT.RIGHT);
        wlUseSubs.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.UseVariableSubst.Label"));
        props.setLook(wlUseSubs);
        fdlUseSubs = new FormData();
        fdlUseSubs.left = new FormAttachment(0, -margin);
        fdlUseSubs.top = new FormAttachment(wcustomSQL, margin);
        fdlUseSubs.right = new FormAttachment(middle, -2*margin);
        wlUseSubs.setLayoutData(fdlUseSubs);
        wUseSubs = new Button(wCustomGroup, SWT.CHECK);
        props.setLook(wUseSubs);
        wUseSubs.setToolTipText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.UseVariableSubst.Tooltip"));
        fdUseSubs = new FormData();
        fdUseSubs.left = new FormAttachment(middle, -margin);
        fdUseSubs.top = new FormAttachment(wcustomSQL, margin);
        fdUseSubs.right = new FormAttachment(100, 0);
        wUseSubs.setLayoutData(fdUseSubs);
        wUseSubs.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });
        
        // clear result rows ?
        wlClearResultList = new Label(wCustomGroup, SWT.RIGHT);
        wlClearResultList.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.ClearResultList.Label"));
        props.setLook(wlClearResultList);
        fdlClearResultList = new FormData();
        fdlClearResultList.left = new FormAttachment(0, -margin);
        fdlClearResultList.top = new FormAttachment(wUseSubs, margin);
        fdlClearResultList.right = new FormAttachment(middle, -2*margin);
        wlClearResultList.setLayoutData(fdlClearResultList);
        wClearResultList = new Button(wCustomGroup, SWT.CHECK);
        props.setLook(wClearResultList);
        wClearResultList.setToolTipText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.ClearResultList.Tooltip"));
        fdClearResultList = new FormData();
        fdClearResultList.left = new FormAttachment(middle, -margin);
        fdClearResultList.top = new FormAttachment(wUseSubs, margin);
        fdClearResultList.right = new FormAttachment(100, 0);
        wClearResultList.setLayoutData(fdClearResultList);
        wClearResultList.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });
        
    		
        // add rows to result?
        wlAddRowsToResult = new Label(wCustomGroup, SWT.RIGHT);
        wlAddRowsToResult.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.AddRowsToResult.Label"));
        props.setLook(wlAddRowsToResult);
        fdlAddRowsToResult = new FormData();
        fdlAddRowsToResult.left = new FormAttachment(0, -margin);
        fdlAddRowsToResult.top = new FormAttachment(wClearResultList, margin);
        fdlAddRowsToResult.right = new FormAttachment(middle, -2*margin);
        wlAddRowsToResult.setLayoutData(fdlAddRowsToResult);
        wAddRowsToResult = new Button(wCustomGroup, SWT.CHECK);
        props.setLook(wAddRowsToResult);
        wAddRowsToResult.setToolTipText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.AddRowsToResult.Tooltip"));
        fdAddRowsToResult = new FormData();
        fdAddRowsToResult.left = new FormAttachment(middle, -margin);
        fdAddRowsToResult.top = new FormAttachment(wClearResultList, margin);
        fdAddRowsToResult.right = new FormAttachment(100, 0);
        wAddRowsToResult.setLayoutData(fdAddRowsToResult);
        wAddRowsToResult.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });

        wlPosition = new Label(wCustomGroup, SWT.NONE);
        props.setLook(wlPosition);
        fdlPosition = new FormData();
        fdlPosition.left = new FormAttachment(0, 0);
        fdlPosition.right= new FormAttachment(100, 0);
        //fdlPosition.top= new FormAttachment(wSQL , 0);
        fdlPosition.bottom = new FormAttachment(100, -margin);
        wlPosition.setLayoutData(fdlPosition);
        
        // Script line
        wlSQL = new Label(wCustomGroup, SWT.NONE);
        wlSQL.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.Script.Label"));
        props.setLook(wlSQL);
        fdlSQL = new FormData();
        fdlSQL.left = new FormAttachment(0, 0);
        fdlSQL.top = new FormAttachment(wAddRowsToResult, margin);
        wlSQL.setLayoutData(fdlSQL);
        
    	
		wbSQLTable=new Button(wCustomGroup, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbSQLTable);
		wbSQLTable.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.GetSQLAndSelectStatement")); //$NON-NLS-1$
		FormData fdbSQLTable=new FormData();
		fdbSQLTable.right = new FormAttachment(100, 0);
		fdbSQLTable.top   = new FormAttachment(wAddRowsToResult, margin);
		wbSQLTable.setLayoutData(fdbSQLTable);

        wSQL=new StyledTextComp(wCustomGroup, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
        props.setLook(wSQL, Props.WIDGET_STYLE_FIXED);
        wSQL.addModifyListener(lsMod);
        fdSQL = new FormData();
        fdSQL.left = new FormAttachment(0, 0);
        fdSQL.top = new FormAttachment(wbSQLTable, margin);
        fdSQL.right = new FormAttachment(100, -5);
        fdSQL.bottom = new FormAttachment(wlPosition, -margin);
        wSQL.setLayoutData(fdSQL);
        
        wSQL.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent arg0)
            {
                setPosition();
            }

        }
    );
        

    	wSQL.addKeyListener(new KeyAdapter(){
    		public void keyPressed(KeyEvent e) { setPosition(); }
    		public void keyReleased(KeyEvent e) { setPosition(); }
    		} 
    	);
    	wSQL.addFocusListener(new FocusAdapter(){
    		public void focusGained(FocusEvent e) { setPosition(); }
    		public void focusLost(FocusEvent e) { setPosition(); }
    		}
    	);
    	wSQL.addMouseListener(new MouseAdapter(){
    		public void mouseDoubleClick(MouseEvent e) { setPosition(); }
    		public void mouseDown(MouseEvent e) { setPosition(); }
    		public void mouseUp(MouseEvent e) { setPosition(); }
    		}
    	);
    	wSQL.addModifyListener(lsMod);
    	
    	
    		// Text Higlighting
    		lineStyler = new SQLValuesHighlight();
    		wSQL.addLineStyleListener(lineStyler);
    		

        
	     fdCustomGroup = new FormData();
	     fdCustomGroup .left = new FormAttachment(0, margin);
	     fdCustomGroup .top = new FormAttachment(wSuccessGroup, margin);
	     fdCustomGroup .right = new FormAttachment(100, -margin);
	     fdCustomGroup .bottom=new FormAttachment(wOK, -margin);
	     wCustomGroup.setLayoutData(fdCustomGroup );
	     // ///////////////////////////////////////////////////////////
	     // / END OF CustomGroup GROUP
	     // ///////////////////////////////////////////////////////////

	
     
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
        lsbSQLTable   = new Listener() { public void handleEvent(Event e) { getSQL();  } };
        
        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);

        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };
        
        wbSQLTable.addListener  (SWT.Selection, lsbSQLTable);
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
        setCustomerSQL();
        BaseStepDialog.setSize(shell);

        shell.open();
        props.setDialogSize(shell, "JobEntryWaitForSQLDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }
    private void getSQL()
	{
		DatabaseMeta inf = jobMeta.findDatabase(wConnection.getText());
		if (inf!=null)
		{	
			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, jobMeta.getDatabases());
			if (std.open())
			{
				String sql = "SELECT *"+Const.CR+"FROM "+inf.getQuotedSchemaTableCombination(std.getSchemaName(), std.getTableName())+Const.CR; //$NON-NLS-1$ //$NON-NLS-2$
				wSQL.setText(sql);

				MessageBox yn = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
				yn.setMessage(BaseMessages.getString(PKG, "JobEntryWaitForSQL.IncludeFieldNamesInSQL")); //$NON-NLS-1$
				yn.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.DialogCaptionQuestion")); //$NON-NLS-1$
				int id = yn.open();
				switch(id)
				{
				case SWT.CANCEL: break;
				case SWT.NO:     wSQL.setText(sql); break;
				case SWT.YES:
					Database db = new Database(loggingObject, inf);
					try
					{
						db.connect();
						RowMetaInterface fields = db.getQueryFields(sql, false);
						if (fields!=null)
						{
							sql = "SELECT"+Const.CR; //$NON-NLS-1$
							for (int i=0;i<fields.size();i++)
							{
								ValueMetaInterface field=fields.getValueMeta(i);
								if (i==0) sql+="  "; else sql+=", "; //$NON-NLS-1$ //$NON-NLS-2$
								sql+=inf.quoteField(field.getName())+Const.CR;
							}
							sql+="FROM "+inf.getQuotedSchemaTableCombination(std.getSchemaName(), std.getTableName())+Const.CR; //$NON-NLS-1$
							wSQL.setText(sql);
						}
						else
						{
							MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
							mb.setMessage(BaseMessages.getString(PKG, "JobEntryWaitForSQL.ERROR_CouldNotRetrieveFields")+Const.CR+BaseMessages.getString(PKG, "JobEntryWaitForSQL.PerhapsNoPermissions")); //$NON-NLS-1$ //$NON-NLS-2$
							mb.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.DialogCaptionError2")); //$NON-NLS-1$
							mb.open();
						}
					}
					catch(KettleException e)
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.DialogCaptionError3")); //$NON-NLS-1$
						mb.setMessage(BaseMessages.getString(PKG, "JobEntryWaitForSQL.AnErrorOccurred")+Const.CR+e.getMessage()); //$NON-NLS-1$
						mb.open(); 
					}
					finally
					{
						db.disconnect();
					}
					break;
				}
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "JobEntryWaitForSQL.ConnectionNoLongerAvailable")); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.DialogCaptionError4")); //$NON-NLS-1$
			mb.open();
		}
					
	}
	
    public void setPosition(){
		
		String scr = wSQL.getText();
		int linenr = wSQL.getLineAtOffset(wSQL.getCaretOffset())+1;
		int posnr  = wSQL.getCaretOffset();
				
		// Go back from position to last CR: how many positions?
		int colnr=0;
		while (posnr>0 && scr.charAt(posnr-1)!='\n' && scr.charAt(posnr-1)!='\r')
		{
			posnr--;
			colnr++;
		}
		wlPosition.setText(BaseMessages.getString(PKG, "JobEntryWaitForSQL.Position.Label",""+linenr,""+colnr));

	}
    private void setCustomerSQL()
    {
    	wlClearResultList.setEnabled(wcustomSQL.getSelection());
    	wClearResultList.setEnabled(wcustomSQL.getSelection());
    	wlSQL.setEnabled(wcustomSQL.getSelection());
    	wSQL.setEnabled(wcustomSQL.getSelection());
    	wlAddRowsToResult.setEnabled(wcustomSQL.getSelection());
    	wAddRowsToResult.setEnabled(wcustomSQL.getSelection());
    	wlUseSubs.setEnabled(wcustomSQL.getSelection());
    	wbSQLTable.setEnabled(wcustomSQL.getSelection());
    	wUseSubs.setEnabled(wcustomSQL.getSelection());
    	wbTable.setEnabled(!wcustomSQL.getSelection());
    	wTablename.setEnabled(!wcustomSQL.getSelection());
    	wlTablename.setEnabled(!wcustomSQL.getSelection());
    	wlSchemaname.setEnabled(!wcustomSQL.getSelection());
    	wSchemaname.setEnabled(!wcustomSQL.getSelection());
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
        
        if(jobEntry.schemaname!=null)  wSchemaname.setText(jobEntry.schemaname);
        if(jobEntry.tablename!=null)  wTablename.setText(jobEntry.tablename);
      
        wSuccessCondition.setText(JobEntryWaitForSQL.getSuccessConditionDesc(jobEntry.successCondition));
        if(jobEntry.rowsCountValue!=null)  wRowsCountValue.setText(jobEntry.rowsCountValue);
        else   wRowsCountValue.setText("0");

        wcustomSQL.setSelection(jobEntry.iscustomSQL); 
        wUseSubs.setSelection(jobEntry.isUseVars);  
        wAddRowsToResult.setSelection(jobEntry.isAddRowsResult);  
        wClearResultList.setSelection(jobEntry.isClearResultList); 
        if(jobEntry.customSQL!=null)  wSQL.setText(jobEntry.customSQL);
		wMaximumTimeout.setText(Const.NVL(jobEntry.getMaximumTimeout(), ""));
		wCheckCycleTime.setText(Const.NVL(jobEntry.getCheckCycleTime(), ""));
		wSuccesOnTimeout.setSelection(jobEntry.isSuccessOnTimeout());
		
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
			mb.setMessage("Please give this job entry a name.");
			mb.setText("Enter the name of the job entry");
			mb.open(); 
			return;
        }
        jobEntry.setName(wName.getText());
        jobEntry.setDatabase(jobMeta.findDatabase(wConnection.getText()));

        jobEntry.schemaname=  wSchemaname.getText();
        jobEntry.tablename=  wTablename.getText();
        jobEntry.successCondition=  JobEntryWaitForSQL.getSuccessConditionByDesc(wSuccessCondition.getText());
        jobEntry.rowsCountValue=  wRowsCountValue.getText();
        jobEntry.iscustomSQL=wcustomSQL.getSelection();
        jobEntry.isUseVars=wUseSubs.getSelection();
        jobEntry.isAddRowsResult=wAddRowsToResult.getSelection();
        jobEntry.isClearResultList=wClearResultList.getSelection();
        jobEntry.customSQL=  wSQL.getText();
		jobEntry.setMaximumTimeout(wMaximumTimeout.getText());
		jobEntry.setCheckCycleTime(wCheckCycleTime.getText());
		jobEntry.setSuccessOnTimeout(wSuccesOnTimeout.getSelection());	
		
        dispose();
    }

    private void getTableName()
	{
    	// New class: SelectTableDialog
		int connr = wConnection.getSelectionIndex();
		if (connr>=0)
		{
			DatabaseMeta inf = jobMeta.getDatabase(connr);
                        
			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, jobMeta.getDatabases());
			std.setSelectedSchemaAndTable(wSchemaname.getText(), wTablename.getText());
			if (std.open())
			{
				wTablename.setText(Const.NVL(std.getTableName(), ""));
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "JobEntryWaitForSQL.ConnectionError2.DialogMessage"));
			mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
			mb.open(); 
		}    
	}
		
}