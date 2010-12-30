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

package org.pentaho.di.ui.job.entries.setvariables;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.setvariables.JobEntrySetVariables;
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
 * This dialog allows you to edit the Set variables job entry settings.
 *
 * @author Samatar Hassan
 * @since  06-05-2007
 */

public class JobEntrySetVariablesDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntrySetVariables.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;
	
	private Label        wlVarSubs;
	private Button       wVarSubs;
	private FormData     fdlVarSubs, fdVarSubs;

	private Button       wOK, wCancel;
	private Listener     lsOK, lsCancel;

	private JobEntrySetVariables jobEntry;
	private Shell         	    shell;

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;
	
	private SelectionAdapter lsDef;
	
	private boolean changed;

	private Group wSettings;
	private FormData fdSettings;

    public JobEntrySetVariablesDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntrySetVariables) jobEntryInt;

		if (this.jobEntry.getName() == null) 
			this.jobEntry.setName(BaseMessages.getString(PKG, "JobEntrySetVariables.Name.Default"));
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
		shell.setText(BaseMessages.getString(PKG, "JobEntrySetVariables.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(BaseMessages.getString(PKG, "JobEntrySetVariables.Name.Label"));
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

		// SETTINGS grouping?
		// ////////////////////////
		// START OF SETTINGS GROUP
		// 

		wSettings = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wSettings);
		wSettings.setText(BaseMessages.getString(PKG, "JobEntrySetVariables.Settings.Label"));

		FormLayout groupLayout = new FormLayout();
		groupLayout.marginWidth = 10;
		groupLayout.marginHeight = 10;
		wSettings.setLayout(groupLayout);

		wlVarSubs = new Label(wSettings, SWT.RIGHT);
		wlVarSubs.setText(BaseMessages.getString(PKG, "JobEntrySetVariables.VarsReplace.Label"));
		props.setLook(wlVarSubs);
		fdlVarSubs = new FormData();
		fdlVarSubs.left = new FormAttachment(0, 0);
		fdlVarSubs.top = new FormAttachment(wName, margin);
		fdlVarSubs.right = new FormAttachment(middle, -margin);
		wlVarSubs.setLayoutData(fdlVarSubs);
		wVarSubs = new Button(wSettings, SWT.CHECK);
		props.setLook(wVarSubs);
		wVarSubs.setToolTipText(BaseMessages.getString(PKG, "JobEntrySetVariables.VarsReplace.Tooltip"));
		fdVarSubs = new FormData();
		fdVarSubs.left = new FormAttachment(middle, 0);
		fdVarSubs.top = new FormAttachment(wName, margin);
		fdVarSubs.right = new FormAttachment(100, 0);
		wVarSubs.setLayoutData(fdVarSubs);
		wVarSubs.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		
		fdSettings = new FormData();
		fdSettings.left = new FormAttachment(0, margin);
		fdSettings.top = new FormAttachment(wName, margin);
		fdSettings.right = new FormAttachment(100, -margin);
		wSettings.setLayoutData(fdSettings);
		
		// ///////////////////////////////////////////////////////////
		// / END OF SETTINGS GROUP
		// ///////////////////////////////////////////////////////////
		
		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "SetVariableDialog.Variables.Label")); //$NON-NLS-1$
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wSettings, margin);
		wlFields.setLayoutData(fdlFields);
		

		int rows = jobEntry.variableName == null
		? 1
		: (jobEntry.variableName.length == 0
		? 0
		: jobEntry.variableName.length);
		final int FieldsRows = rows;
		
		ColumnInfo[] colinf=
            {
		        new ColumnInfo(BaseMessages.getString(PKG, "SetVariableDialog.Fields.Column.VariableName"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
		        new ColumnInfo(BaseMessages.getString(PKG, "SetVariableDialog.Fields.Column.Value"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
                new ColumnInfo(BaseMessages.getString(PKG, "SetVariableDialog.Fields.Column.VariableType"), ColumnInfo.COLUMN_TYPE_CCOMBO, JobEntrySetVariables.getVariableTypeDescriptions(), false), //$NON-NLS-1$
            };
		colinf[0].setUsingVariables(true);
		colinf[1].setUsingVariables(true);
		
		wFields=new TableView(jobMeta,shell, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  FieldsRows,  
							  lsMod,
							  props
							  );

		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);
		
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wFields);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wName.addSelectionListener( lsDef );

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
		if (jobEntry.getName() != null) wName.setText( jobEntry.getName() );
		wName.selectAll();
		
		wVarSubs.setSelection(jobEntry.isReplaceVars());
			
		if (jobEntry.variableName != null)
		{
			for (int i = 0; i < jobEntry.variableName.length; i++)
			{
				TableItem ti = wFields.table.getItem(i);
				if (jobEntry.variableName[i] != null)
					ti.setText(1, jobEntry.variableName[i]);
				if (jobEntry.getVariableValue()[i] != null)
					ti.setText(2, jobEntry.getVariableValue()[i]);

				ti.setText(3, JobEntrySetVariables.getVariableTypeDescription(jobEntry.getVariableType()[i]));
				
			}
			wFields.setRowNums();
			wFields.optWidth(true);
		}
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
		jobEntry.setReplaceVars(wVarSubs.getSelection());
		
		int nritems = wFields.nrNonEmpty();
		int nr = 0;
		for (int i = 0; i < nritems; i++)
		{
			String arg = wFields.getNonEmpty(i).getText(1);
			if (arg != null && arg.length() != 0)
				nr++;
		}
		jobEntry.variableName = new String[nr];
		jobEntry.variableValue = new String[nr];
		jobEntry.variableType = new int[nr];

		
		nr = 0;
		for (int i = 0; i < nritems; i++)
		{
			String varname = wFields.getNonEmpty(i).getText(1);
			String varvalue = wFields.getNonEmpty(i).getText(2);
		    String vartype = wFields.getNonEmpty(i).getText(3);
		
			if (varname != null && varname.length() != 0)
			{
				jobEntry.variableName[nr] = varname;
				jobEntry.variableValue[nr] = varvalue;
				jobEntry.variableType[nr]= JobEntrySetVariables.getVariableType(vartype);
				nr++;
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