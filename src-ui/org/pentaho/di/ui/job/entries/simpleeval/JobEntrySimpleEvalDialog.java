/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is samatar Hassan
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.ui.job.entries.simpleeval;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.widgets.MessageBox; 
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog; 
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.job.entries.simpleeval.JobEntrySimpleEval;
import org.pentaho.di.job.entries.simpleeval.Messages;


/**
 * This dialog allows you to edit the XML valid job entry settings.
 *
 * @author Samatar Hassan
 * @since  01-01-2000
 */

public class JobEntrySimpleEvalDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	
	private Label        wlName;
	private Text         wName;
	private FormData     fdlName, fdName;

	private Button       wOK, wCancel;
	private Listener     lsOK, lsCancel;

	private JobEntrySimpleEval jobEntry;
	private Shell         	    shell;

	private SelectionAdapter lsDef;

	private boolean changed;
	
	private CTabFolder   wTabFolder;
	private Composite    wGeneralComp;	
	private CTabItem     wGeneralTab;
	private FormData	 fdGeneralComp;
	private FormData     fdTabFolder;
    
    
	private Group wSuccessOn,wSource;
    private FormData fdSuccessOn,fdSource;

	private Label wlSuccessCondition,wlValueType,wlFieldType,wlMask;
	private CCombo wSuccessCondition,wValueType,wFieldType;
	private ComboVar wMask;
	private FormData fdlSuccessCondition, fdSuccessCondition,fdlValueType,fdValueType,
			fdFieldType,fdlFieldType,fdMask,fdlMask;
	
	private Label wlSuccessNumberCondition;
	private CCombo wSuccessNumberCondition;
	private FormData fdlSuccessNumberCondition, fdSuccessNumberCondition;
	
	private Label wlCompareValue;
	private TextVar wCompareValue;
	private FormData fdlCompareValue, fdCompareValue;
	
	
	
	private Label wlMinValue;
	private TextVar wMinValue;
	private FormData fdlMinValue, fdMinValue;
	
	
	private Label wlMaxValue;
	private TextVar wMaxValue;
	private FormData fdlMaxValue, fdMaxValue;
	 
	private Label wlVariableName;
	private TextVar wVariableName;
	private FormData fdlVariableName, fdVariableName;
	
	private Label wlFieldName;
	private TextVar wFieldName;
	private FormData fdlFieldName, fdFieldName;
    
	public JobEntrySimpleEvalDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntrySimpleEval) jobEntryInt;
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
		shell.setText(Messages.getString("JobSimpleEval.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(Messages.getString("JobSimpleEval.Name.Label"));
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
		  
        wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
 		
 		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(Messages.getString("JobSimpleEval.Tab.General.Label"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
		

		
		 // SuccessOngrouping?
	     // ////////////////////////
	     // START OF SUCCESS ON GROUP///
	     // /
	    wSource= new Group(wGeneralComp, SWT.SHADOW_NONE);
	    props.setLook(wSource);
	    wSource.setText(Messages.getString("JobSimpleEval.Source.Group.Label"));
	    FormLayout sourcegroupLayout = new FormLayout();
	    sourcegroupLayout.marginWidth = 10;
	    sourcegroupLayout.marginHeight = 10;
	    wSource.setLayout(sourcegroupLayout);
	    

	    //Evaluate value (variable ou field from previous result entry)?
	  	wlValueType = new Label(wSource, SWT.RIGHT);
	  	wlValueType.setText(Messages.getString("JobSimpleEval.ValueType.Label"));
	  	props.setLook(wlValueType);
	  	fdlValueType = new FormData();
	  	fdlValueType.left = new FormAttachment(0, -margin);
	  	fdlValueType.right = new FormAttachment(middle, -margin);
	  	fdlValueType.top = new FormAttachment(0, margin);
	  	wlValueType.setLayoutData(fdlValueType);
	  	wValueType = new CCombo(wSource, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wValueType.setItems(JobEntrySimpleEval.valueTypeDesc);
	  	
	  	props.setLook(wValueType);
		fdValueType= new FormData();
		fdValueType.left = new FormAttachment(middle, 0);
		fdValueType.top = new FormAttachment(0, margin);
		fdValueType.right = new FormAttachment(100, 0);
		wValueType.setLayoutData(fdValueType);
		wValueType.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				
				displayCorrectValueType();				
				
			}
		});


		// Name of the field to evaluate
		wlFieldName= new Label(wSource, SWT.RIGHT);
		wlFieldName.setText(Messages.getString("JobSimpleEval.FieldName.Label"));
		props.setLook(wlFieldName);
		fdlFieldName= new FormData();
		fdlFieldName.left = new FormAttachment(0, 0);
		fdlFieldName.top = new FormAttachment(wValueType, margin);
		fdlFieldName.right = new FormAttachment(middle, -margin);
		wlFieldName.setLayoutData(fdlFieldName);
		
		wFieldName= new TextVar(jobMeta ,wSource, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages
			.getString("JobSimpleEval.FieldName.Tooltip"));
		props.setLook(wFieldName);
		wFieldName.addModifyListener(lsMod);
		fdFieldName= new FormData();
		fdFieldName.left = new FormAttachment(middle, 0);
		fdFieldName.top = new FormAttachment(wValueType, margin);
		fdFieldName.right = new FormAttachment(100, -margin);
		wFieldName.setLayoutData(fdFieldName);
		
		// Name of the variable to evaluate
		wlVariableName= new Label(wSource, SWT.RIGHT);
		wlVariableName.setText(Messages.getString("JobSimpleEval.Variable.Label"));
		props.setLook(wlVariableName);
		fdlVariableName= new FormData();
		fdlVariableName.left = new FormAttachment(0, 0);
		fdlVariableName.top = new FormAttachment(wValueType, margin);
		fdlVariableName.right = new FormAttachment(middle, -margin);
		wlVariableName.setLayoutData(fdlVariableName);
		
		wVariableName= new TextVar(jobMeta, wSource, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages
			.getString("JobSimpleEval.Variable.Tooltip"));
		props.setLook(wVariableName);
		wVariableName.addModifyListener(lsMod);
		fdVariableName= new FormData();
		fdVariableName.left = new FormAttachment(middle, 0);
		fdVariableName.top = new FormAttachment(wValueType, margin);
		fdVariableName.right = new FormAttachment(100, -margin);
		wVariableName.setLayoutData(fdVariableName);
		

	    //Field type
	  	wlFieldType = new Label(wSource, SWT.RIGHT);
	  	wlFieldType.setText(Messages.getString("JobSimpleEval.FieldType.Label"));
	  	props.setLook(wlFieldType);
	  	fdlFieldType = new FormData();
	  	fdlFieldType.left = new FormAttachment(0, 0);
	  	fdlFieldType.right = new FormAttachment(middle, -margin);
	  	fdlFieldType.top = new FormAttachment(wVariableName, margin);
	  	wlFieldType.setLayoutData(fdlFieldType);
	  	wFieldType = new CCombo(wSource, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wFieldType.setItems(JobEntrySimpleEval.fieldTypeDesc);
	  	
		props.setLook(wFieldType);
		fdFieldType= new FormData();
		fdFieldType.left = new FormAttachment(middle, 0);
		fdFieldType.top = new FormAttachment(wVariableName, margin);
		fdFieldType.right = new FormAttachment(100, 0);
		wFieldType.setLayoutData(fdFieldType);
		wFieldType.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				
				activeMask();				
				
			}
		});

	    //Mask
	  	wlMask = new Label(wSource, SWT.RIGHT);
	  	wlMask.setText(Messages.getString("JobSimpleEval.Mask.Label"));
	  	props.setLook(wlMask);
	  	fdlMask = new FormData();
	  	fdlMask.left = new FormAttachment(0, 0);
	  	fdlMask.right = new FormAttachment(middle, -margin);
	  	fdlMask.top = new FormAttachment(wFieldType, margin);
	  	wlMask.setLayoutData(fdlMask);

	  	wMask=new ComboVar(jobMeta, wSource, SWT.BORDER | SWT.READ_ONLY);
	  	wMask.setItems(Const.getDateFormats());
	  	wMask.setEditable(true);
		props.setLook(wMask);
		fdMask= new FormData();
		fdMask.left = new FormAttachment(middle, 0);
		fdMask.top = new FormAttachment(wFieldType, margin);
		fdMask.right = new FormAttachment(100, 0);
		wMask.setLayoutData(fdMask);
		wMask.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				
				
			}
		});
		
	    fdSource= new FormData();
	    fdSource.left = new FormAttachment(0, margin);
	    fdSource.top = new FormAttachment(wName, margin);
	    fdSource.right = new FormAttachment(100, -margin);
	    wSource.setLayoutData(fdSource);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Success ON GROUP
	     // ///////////////////////////////////////////////////////////
		
	
		 // SuccessOngrouping?
	     // ////////////////////////
	     // START OF SUCCESS ON GROUP///
	     // /
	    wSuccessOn= new Group(wGeneralComp, SWT.SHADOW_NONE);
	    props.setLook(wSuccessOn);
	    wSuccessOn.setText(Messages.getString("JobSimpleEval.SuccessOn.Group.Label"));

	    FormLayout successongroupLayout = new FormLayout();
	    successongroupLayout.marginWidth = 10;
	    successongroupLayout.marginHeight = 10;

	    wSuccessOn.setLayout(successongroupLayout);
		

	    //Success Condition
	  	wlSuccessCondition = new Label(wSuccessOn, SWT.RIGHT);
	  	wlSuccessCondition.setText(Messages.getString("JobSimpleEval.SuccessCondition.Label"));
	  	props.setLook(wlSuccessCondition);
	  	fdlSuccessCondition = new FormData();
	  	fdlSuccessCondition.left = new FormAttachment(0, 0);
	  	fdlSuccessCondition.right = new FormAttachment(middle, 0);
	  	fdlSuccessCondition.top = new FormAttachment(wVariableName, margin);
	  	wlSuccessCondition.setLayoutData(fdlSuccessCondition);
	  	
	  	wSuccessCondition = new CCombo(wSuccessOn, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wSuccessCondition.add(Messages.getString("JobSimpleEval.SuccessWhenEqual.Label"));
	  	wSuccessCondition.add(Messages.getString("JobSimpleEval.SuccessWhenDifferent.Label"));
	  	wSuccessCondition.add(Messages.getString("JobSimpleEval.SuccessWhenContains.Label"));
	  	wSuccessCondition.add(Messages.getString("JobSimpleEval.SuccessWhenNotContains.Label"));
	  	wSuccessCondition.add(Messages.getString("JobSimpleEval.SuccessWhenStartWith.Label"));
	  	wSuccessCondition.add(Messages.getString("JobSimpleEval.SuccessWhenNotStartWith.Label"));
	  	wSuccessCondition.add(Messages.getString("JobSimpleEval.SuccessWhenEndWith.Label"));
	  	wSuccessCondition.add(Messages.getString("JobSimpleEval.SuccessWhenNotEndWith.Label"));
	  	wSuccessCondition.add(Messages.getString("JobSimpleEval.SuccessWhenRegExp.Label"));
	  	wSuccessCondition.select(0); // +1: starts at -1
	  	
		props.setLook(wSuccessCondition);
		fdSuccessCondition= new FormData();
		fdSuccessCondition.left = new FormAttachment(middle, 0);
		fdSuccessCondition.top = new FormAttachment(wVariableName, margin);
		fdSuccessCondition.right = new FormAttachment(100, 0);
		wSuccessCondition.setLayoutData(fdSuccessCondition);
		wSuccessCondition.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				
			}
		});
		
	    //Success number(date) Condition
	  	wlSuccessNumberCondition = new Label(wSuccessOn, SWT.RIGHT);
	  	wlSuccessNumberCondition.setText(Messages.getString("JobSimpleEval.SuccessNumberCondition.Label"));
	  	props.setLook(wlSuccessNumberCondition);
	  	fdlSuccessNumberCondition = new FormData();
	  	fdlSuccessNumberCondition.left = new FormAttachment(0, 0);
	  	fdlSuccessNumberCondition.right = new FormAttachment(middle, -margin);
	  	fdlSuccessNumberCondition.top = new FormAttachment(wVariableName, margin);
	  	wlSuccessNumberCondition.setLayoutData(fdlSuccessNumberCondition);
	  	
	  	wSuccessNumberCondition = new CCombo(wSuccessOn, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wSuccessNumberCondition.setItems(JobEntrySimpleEval.successNumberConditionDesc);
	  	wSuccessNumberCondition.select(0); // +1: starts at -1
	  	
		props.setLook(wSuccessNumberCondition);
		fdSuccessNumberCondition= new FormData();
		fdSuccessNumberCondition.left = new FormAttachment(middle, 0);
		fdSuccessNumberCondition.top = new FormAttachment(wVariableName, margin);
		fdSuccessNumberCondition.right = new FormAttachment(100, 0);
		wSuccessNumberCondition.setLayoutData(fdSuccessNumberCondition);
		wSuccessNumberCondition.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				activeSuccessCondition();
				
			}
		});

		

		// Compare with value
		wlCompareValue= new Label(wSuccessOn, SWT.RIGHT);
		wlCompareValue.setText(Messages.getString("JobSimpleEval.CompareValue.Label"));
		props.setLook(wlCompareValue);
		fdlCompareValue= new FormData();
		fdlCompareValue.left = new FormAttachment(0, 0);
		fdlCompareValue.top = new FormAttachment(wSuccessNumberCondition, margin);
		fdlCompareValue.right = new FormAttachment(middle, -margin);
		wlCompareValue.setLayoutData(fdlCompareValue);
		
		wCompareValue= new TextVar(jobMeta ,wSuccessOn, SWT.SINGLE | SWT.LEFT | SWT.BORDER, 
				Messages.getString("JobSimpleEval.CompareValue.Tooltip"));
		props.setLook(wCompareValue);
		wCompareValue.addModifyListener(lsMod);
		fdCompareValue= new FormData();
		fdCompareValue.left = new FormAttachment(middle, 0);
		fdCompareValue.top = new FormAttachment(wSuccessNumberCondition, margin);
		fdCompareValue.right = new FormAttachment(100, -margin);
		wCompareValue.setLayoutData(fdCompareValue);
		
		// Min value
		wlMinValue= new Label(wSuccessOn, SWT.RIGHT);
		wlMinValue.setText(Messages.getString("JobSimpleEval.MinValue.Label"));
		props.setLook(wlMinValue);
		fdlMinValue= new FormData();
		fdlMinValue.left = new FormAttachment(0, 0);
		fdlMinValue.top = new FormAttachment(wSuccessNumberCondition, margin);
		fdlMinValue.right = new FormAttachment(middle, -margin);
		wlMinValue.setLayoutData(fdlMinValue);
		
		wMinValue= new TextVar(jobMeta, wSuccessOn, SWT.SINGLE | SWT.LEFT | SWT.BORDER, 
				Messages.getString("JobSimpleEval.MinValue.Tooltip"));
		props.setLook(wMinValue);
		wMinValue.addModifyListener(lsMod);
		fdMinValue= new FormData();
		fdMinValue.left = new FormAttachment(middle, 0);
		fdMinValue.top = new FormAttachment(wSuccessNumberCondition, margin);
		fdMinValue.right = new FormAttachment(100, -margin);
		wMinValue.setLayoutData(fdMinValue);
		
		// Maximum value
		wlMaxValue= new Label(wSuccessOn, SWT.RIGHT);
		wlMaxValue.setText(Messages.getString("JobSimpleEval.MaxValue.Label"));
		props.setLook(wlMaxValue);
		fdlMaxValue= new FormData();
		fdlMaxValue.left = new FormAttachment(0, 0);
		fdlMaxValue.top = new FormAttachment(wMinValue, margin);
		fdlMaxValue.right = new FormAttachment(middle, -margin);
		wlMaxValue.setLayoutData(fdlMaxValue);
		
		wMaxValue= new TextVar(jobMeta, wSuccessOn, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages
			.getString("JobSimpleEval.MaxValue.Tooltip"));
		props.setLook(wMaxValue);
		wMaxValue.addModifyListener(lsMod);
		fdMaxValue= new FormData();
		fdMaxValue.left = new FormAttachment(middle, 0);
		fdMaxValue.top = new FormAttachment(wMinValue, margin);
		fdMaxValue.right = new FormAttachment(100, -margin);
		wMaxValue.setLayoutData(fdMaxValue);
		
	
	    fdSuccessOn= new FormData();
	    fdSuccessOn.left = new FormAttachment(0, margin);
	    fdSuccessOn.top = new FormAttachment(wSource, margin);
	    fdSuccessOn.right = new FormAttachment(100, -margin);
	    wSuccessOn.setLayoutData(fdSuccessOn);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Success ON GROUP
	     // ///////////////////////////////////////////////////////////


		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(0, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(100, 0);
		wGeneralComp.setLayoutData(fdGeneralComp);
		
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
 		props.setLook(wGeneralComp);
 		
 		
 		
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////
 		
 		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wName, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
		
		

		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wTabFolder);
		

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
		displayCorrectValueType();
		activeMask();


		wTabFolder.setSelection(0);
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
	private void activeSuccessCondition()
	{
		if(wFieldType.getSelectionIndex()==0)
		{
			wlCompareValue.setVisible(true);
			wCompareValue.setVisible(true);
			wlMinValue.setVisible(false);
			wMinValue.setVisible(false);
			wlMaxValue.setVisible(false);	
			wMaxValue.setVisible(false);
		}else
		{
			if(wSuccessNumberCondition.getSelectionIndex()==6)
			{
				wlCompareValue.setVisible(false);
				wCompareValue.setVisible(false);
				wlMinValue.setVisible(true);
				wMinValue.setVisible(true);
				wlMaxValue.setVisible(true);	
				wMaxValue.setVisible(true);
			}else
			{
				wlCompareValue.setVisible(true);
				wCompareValue.setVisible(true);
				wlMinValue.setVisible(false);
				wMinValue.setVisible(false);
				wlMaxValue.setVisible(false);	
				wMaxValue.setVisible(false);
			}
		}
	}
	private void activeMask()
	{

		if(!Const.isEmpty(wFieldType.getText()))
		{
			wlMask.setVisible(wFieldType.getSelectionIndex()==2);
			wMask.setVisible(wFieldType.getSelectionIndex()==2);
			if(wFieldType.getSelectionIndex()==0)
			{
				wlSuccessCondition.setVisible(true);
				wSuccessCondition.setVisible(true);
				wlSuccessNumberCondition.setVisible(false);
				wSuccessNumberCondition.setVisible(false);
			}
			else
			{				
				wlSuccessCondition.setVisible(false);
				wSuccessCondition.setVisible(false);
				wlSuccessNumberCondition.setVisible(true);
				wSuccessNumberCondition.setVisible(true);
			}
		}else
		{
			wlMask.setVisible(false);
			wMask.setVisible(false);
			
			wlSuccessCondition.setVisible(false);
			wSuccessCondition.setVisible(false);
			wlSuccessNumberCondition.setVisible(false);
			wSuccessNumberCondition.setVisible(false);
		}
		activeSuccessCondition();
	}
	

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		if (jobEntry.getName()    != null) wName.setText( jobEntry.getName() );
		wName.selectAll();
		wValueType.setText(JobEntrySimpleEval.getValueTypeDesc(jobEntry.valuetype));
		if (jobEntry.getFieldName()    != null) wFieldName.setText( jobEntry.getFieldName() );
		if (jobEntry.getVariableName() != null) wVariableName.setText( jobEntry.getVariableName() );
		
		wFieldType.setText(JobEntrySimpleEval.getFieldTypeDesc(jobEntry.fieldtype));
		if (jobEntry.getMask()!= null) wMask.setText( jobEntry.getMask() );
		if (jobEntry.getCompareValue()!= null) wCompareValue.setText( jobEntry.getCompareValue() );
		if (jobEntry.getMinValue()!= null) wMinValue.setText( jobEntry.getMinValue() );
		if (jobEntry.getMaxValue()!= null) wMaxValue.setText( jobEntry.getMaxValue() );
		wSuccessCondition.setText(JobEntrySimpleEval.getSuccessConditionDesc(jobEntry.successcondition));
		wSuccessNumberCondition.setText(JobEntrySimpleEval.getSuccessNumberConditionDesc(jobEntry.successnumbercondition));
		
	}
	private void displayCorrectValueType()
	{
		wlFieldName.setVisible(wValueType.getSelectionIndex()==0);
		wFieldName.setVisible(wValueType.getSelectionIndex()==0);
		wlVariableName.setVisible(wValueType.getSelectionIndex()!=0);
		wVariableName.setVisible(wValueType.getSelectionIndex()!=0);
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
			mb.setMessage(Messages.getString("System.StepJobEntryNameMissing.Title"));
			mb.setText(Messages.getString("System.JobEntryNameMissing.Msg"));
			mb.open(); 
			return;
        }
		jobEntry.setName(wName.getText());
		
		jobEntry.valuetype=  JobEntrySimpleEval.getValueTypeByDesc(wValueType.getText());
		jobEntry.setFieldName(wFieldName.getText());
		jobEntry.setVariableName(wVariableName.getText());
		
		jobEntry.fieldtype=  JobEntrySimpleEval.getFieldTypeByDesc(wFieldType.getText());
		jobEntry.setMask(wMask.getText());
		jobEntry.setCompareValue(wCompareValue.getText());
		jobEntry.setMinValue(wMinValue.getText());
		jobEntry.setMaxValue(wMaxValue.getText());
		jobEntry.successcondition=  JobEntrySimpleEval.getSuccessConditionByDesc(wSuccessCondition.getText());
		jobEntry.successnumbercondition=  JobEntrySimpleEval.getSuccessNumberConditionByDesc(wSuccessNumberCondition.getText());
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