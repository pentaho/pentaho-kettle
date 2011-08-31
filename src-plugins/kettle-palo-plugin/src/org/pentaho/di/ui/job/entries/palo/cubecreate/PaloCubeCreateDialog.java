/*
 *   This file is part of PaloKettlePlugin.
 *
 *   PaloKettlePlugin is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PaloKettlePlugin is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with PaloKettlePlugin.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   Copyright 2011 De Bortoli Wines Pty Limited (Australia)
 */

package org.pentaho.di.ui.job.entries.palo.cubecreate;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;

import org.pentaho.di.palo.core.PaloHelper;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.palo.cubecreate.PaloCubeCreate;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to define the palo cube you want to create
 * 
 * @author Pieter van der Merwe
 * @since 03-08-2011
 */

public class PaloCubeCreateDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = PaloCubeCreate.class;
	
	private Text textStepName;
	private Label labelStepName;

	private CCombo addConnectionLine;

	private Label labelCubeName;
	private Text textCubeName;
	
	private ColumnInfo[] colinf; 
	private TableView tableViewFields;
	
	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private PaloCubeCreate     jobEntry;
	private Shell       	shell;
	private PropsUI       	props;

	private ColumnInfo comboDropDown; 
	
	private SelectionAdapter lsDef;

	private boolean changed;
	private JobMeta jobMeta;

	public PaloCubeCreateDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
	{
		super(parent, jobEntryInt, rep, jobMeta);
		props=PropsUI.getInstance();
		this.jobEntry=(PaloCubeCreate) jobEntryInt;

		if (this.jobEntry.getName() == null) this.jobEntry.setName(jobEntryInt.getName());
		
		this.jobMeta = jobMeta;
	}

	public JobEntryInterface open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
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
		shell.setText(BaseMessages.getString(PKG,"PaloCubeCreateDialog.PaloCubeCreate")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		labelStepName=new Label(shell, SWT.RIGHT);
		labelStepName.setText(BaseMessages.getString(PKG,"PaloCubeCreateDialog.StepName")); //$NON-NLS-1$
		props.setLook( labelStepName );
		FormData fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right= new FormAttachment(middle, -margin);
		fd.top  = new FormAttachment(0, margin);
		labelStepName.setLayoutData(fd);

		textStepName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		textStepName.setText(jobEntry.getName());
		props.setLook( textStepName );
		textStepName.addModifyListener(lsMod);
		fd=new FormData();
		fd.left = new FormAttachment(middle, 0);
		fd.top  = new FormAttachment(0, margin);
		fd.right= new FormAttachment(100, 0);
		textStepName.setLayoutData(fd);

		addConnectionLine = addConnectionLine(shell, textStepName, Const.MIDDLE_PCT, margin);
		
		addConnectionLine.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                doSelectConnection(false);
            }
        });
		
		props.setLook(addConnectionLine);

		// Get cube name to delete
		labelCubeName = new Label(shell, SWT.RIGHT);
		labelCubeName.setText(BaseMessages.getString(PKG,"PaloCubeCreateDialog.CubeName")); //$NON-NLS-1$
		props.setLook(labelCubeName);

		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right= new FormAttachment(middle, -margin);
		fd.top  = new FormAttachment(addConnectionLine, margin);
		labelCubeName.setLayoutData(fd);

		textCubeName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		textCubeName.addModifyListener(lsMod);
		props.setLook(textCubeName);
		
		fd = new FormData();
		fd.left = new FormAttachment(middle, 0);
		fd.right= new FormAttachment(100, 0);
		fd.top  = new FormAttachment(addConnectionLine, margin);
		textCubeName.setLayoutData(fd);
		
		colinf=new ColumnInfo[] {
                new ColumnInfo(getLocalizedColumn(0),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {}, false)
                };
        
        tableViewFields = new TableView(null, shell,  
                                                  SWT.NONE | SWT.BORDER, 
                                                  colinf, 
                                                  10, true, 
                                                  lsMod,
                                                  props
                                                  );
        
        tableViewFields.setSize(477, 105);
        tableViewFields.setBounds(5, 250, 477, 105);
        tableViewFields.setReadonly(false);
        tableViewFields.table.removeAll();
        tableViewFields.optWidth(true);
        
        fd=new FormData();
        fd.left  = new FormAttachment(0, margin);
        fd.top   = new FormAttachment(textCubeName, 3*margin);
        fd.right = new FormAttachment(100, 0);
        fd.bottom= new FormAttachment(100, -50);
        tableViewFields.setLayoutData(fd);
        
        tableViewFields.table.addFocusListener(new FocusListener() {
			
			public void focusLost(FocusEvent arg0) {
			}
			
			public void focusGained(FocusEvent arg0) {
				doBuildDimensionList();
			}
		});        

        
        props.setLook(tableViewFields);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG,"System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG,"System.Button.Cancel")); //$NON-NLS-1$

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel}, margin, tableViewFields);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		textStepName.addSelectionListener( lsDef );

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
	
	private String getLocalizedColumn(int columnIndex) {
        switch(columnIndex) {
            case 0:
                return BaseMessages.getString(PKG,"PaloCellCreateDialog.ColumnDimension");
            default:
                return "";
        }
    }

	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}


	private void doBuildDimensionList() {
		if(comboDropDown == null && addConnectionLine.getText() != null) {
			DatabaseMeta dbMeta = DatabaseMeta.findDatabase(jobMeta.getDatabases(), addConnectionLine.getText());
			if (dbMeta != null) {
				PaloHelper helper = new PaloHelper(dbMeta);
				try{
					helper.connect();
					List<String> dimensionNames = helper.getDimensionsNames();
					comboDropDown = new ColumnInfo("Field",  ColumnInfo.COLUMN_TYPE_CCOMBO, helper.getDimensionsNames().toArray(new String[dimensionNames.size()]), true);
					tableViewFields.setColumnInfo(0, comboDropDown);
				}
				catch (Exception ex) {
					new ErrorDialog(shell, BaseMessages.getString(PKG,"PaloCellOutputDialog.RetreiveCubesErrorTitle") , BaseMessages.getString(PKG,"PaloCellOutputDialog.RetreiveCubesError") , ex);
				}
				finally
				{
					helper.disconnect();
				}

			}
		}
	}

	private void doSelectConnection(boolean clearCurrentData ) {
		comboDropDown = null;
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (jobEntry.getName()    != null) 
			textStepName.setText( jobEntry.getName() );
		textStepName.selectAll();

		int index = addConnectionLine.indexOf(jobEntry.getDatabaseMeta() != null ? jobEntry.getDatabaseMeta().getName() : "");
		if (index >=0) 
			addConnectionLine.select(index);

		if (jobEntry.getCubeName() != null)
			textCubeName.setText(jobEntry.getCubeName());
		
		tableViewFields.table.removeAll();

        if(jobEntry.getDimensionNames() != null 
        		&& jobEntry.getDimensionNames().size() > 0) 
            for (String dimensionName : jobEntry.getDimensionNames())
                tableViewFields.add(dimensionName);
        
        if (tableViewFields.table.getItemCount() == 0)
        	tableViewFields.add("");
        
		tableViewFields.setRowNums();
		
	}

	private void cancel()
	{
		jobEntry.setChanged(changed);
		jobEntry=null;
		dispose();
	}

	private void ok()
	{
		tableViewFields.removeEmptyRows();
		List <String> dimensionNames = new ArrayList<String>();
		
		for (int i = 0; i < tableViewFields.table.getItemCount(); i++) 
        	dimensionNames.add(tableViewFields.table.getItem(i).getText(1));
        

		jobEntry.setName(textStepName.getText());
		jobEntry.setDatabaseMeta(DatabaseMeta.findDatabase(jobMeta.getDatabases(), addConnectionLine.getText()));
		jobEntry.setCubeName(textCubeName.getText());
		jobEntry.setDimensionNames(dimensionNames);
		
		dispose();
	}

	public String toString()
	{
		return this.getClass().getName();
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
