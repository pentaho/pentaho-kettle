/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.job.entries.deletefolders;

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
import org.eclipse.swt.widgets.DirectoryDialog;
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
import org.pentaho.di.job.entries.deletefolders.JobEntryDeleteFolders;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit the Delete Folders job entry settings.
 *
 * @author Samatar Hassan
 * @since  13-05-2008
 */
public class JobEntryDeleteFoldersDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryDeleteFolders.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlFilename;
	private Button       wbDirectory;
	private TextVar      wFilename;
	private FormData     fdlFilename, fdFilename,fdbDirectory;

	private Button       wOK, wCancel;
	private Listener     lsOK, lsCancel;

	private JobEntryDeleteFolders jobEntry;
	private Shell         	    shell;

	private SelectionAdapter lsDef;
	
	private boolean changed;

	private Label wlPrevious;
	private Button wPrevious;
	private FormData fdlPrevious, fdPrevious;

	private Label wlFields;
	private TableView wFields;
	private FormData fdlFields, fdFields;

	private Group wSettings;
	private FormData fdSettings;

	private Button   wbdFilename; // Delete
	private Button   wbeFilename; // Edit
	private Button   wbaFilename; // Add or change
	private FormData fdbeFilename, fdbaFilename, fdbdFilename;
	
	private Group wSuccessOn;
    private FormData fdSuccessOn;
    
	private Label wlSuccessCondition;
	private CCombo wSuccessCondition;
	private FormData fdlSuccessCondition, fdSuccessCondition;
	
	private Label wlNrErrorsLessThan;
	private TextVar wLimitFolders;
	private FormData fdlNrErrorsLessThan, fdNrErrorsLessThan;

    public JobEntryDeleteFoldersDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryDeleteFolders) jobEntryInt;

		if (this.jobEntry.getName() == null) 
			this.jobEntry.setName(BaseMessages.getString(PKG, "JobDeleteFolders.Name.Default"));
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
		shell.setText(BaseMessages.getString(PKG, "JobDeleteFolders.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(BaseMessages.getString(PKG, "JobDeleteFolders.Name.Label"));
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
		wSettings.setText(BaseMessages.getString(PKG, "JobDeleteFolders.Settings.Label"));

		FormLayout groupLayout = new FormLayout();
		groupLayout.marginWidth = 10;
		groupLayout.marginHeight = 10;
		wSettings.setLayout(groupLayout);
	
		wlPrevious = new Label(wSettings, SWT.RIGHT);
		wlPrevious.setText(BaseMessages.getString(PKG, "JobDeleteFolders.Previous.Label"));
		props.setLook(wlPrevious);
		fdlPrevious = new FormData();
		fdlPrevious.left = new FormAttachment(0, 0);
		fdlPrevious.top = new FormAttachment(wName, margin );
		fdlPrevious.right = new FormAttachment(middle, -margin);
		wlPrevious.setLayoutData(fdlPrevious);
		wPrevious = new Button(wSettings, SWT.CHECK);
		props.setLook(wPrevious);
		wPrevious.setSelection(jobEntry.argFromPrevious);
		wPrevious.setToolTipText(BaseMessages.getString(PKG, "JobDeleteFolders.Previous.Tooltip"));
		fdPrevious = new FormData();
		fdPrevious.left = new FormAttachment(middle, 0);
		fdPrevious.top = new FormAttachment(wName, margin );
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
		fdSettings = new FormData();
		fdSettings.left = new FormAttachment(0, margin);
		fdSettings.top = new FormAttachment(wName, margin);
		fdSettings.right = new FormAttachment(100, -margin);
		wSettings.setLayoutData(fdSettings);
		
		// ///////////////////////////////////////////////////////////
		// / END OF SETTINGS GROUP
		// ///////////////////////////////////////////////////////////

		 // SuccessOngrouping?
	     // ////////////////////////
	     // START OF SUCCESS ON GROUP///
	     // /
	    wSuccessOn= new Group(shell, SWT.SHADOW_NONE);
	    props.setLook(wSuccessOn);
	    wSuccessOn.setText(BaseMessages.getString(PKG, "JobDeleteFolders.SuccessOn.Group.Label"));

	    FormLayout successongroupLayout = new FormLayout();
	    successongroupLayout.marginWidth = 10;
	    successongroupLayout.marginHeight = 10;

	    wSuccessOn.setLayout(successongroupLayout);
	    

	    //Success Condition
	  	wlSuccessCondition = new Label(wSuccessOn, SWT.RIGHT);
	  	wlSuccessCondition.setText(BaseMessages.getString(PKG, "JobDeleteFolders.SuccessCondition.Label"));
	  	props.setLook(wlSuccessCondition);
	  	fdlSuccessCondition = new FormData();
	  	fdlSuccessCondition.left = new FormAttachment(0, 0);
	  	fdlSuccessCondition.right = new FormAttachment(middle, -margin);
	  	fdlSuccessCondition.top = new FormAttachment(wSettings, margin);
	  	wlSuccessCondition.setLayoutData(fdlSuccessCondition);
	  	wSuccessCondition = new CCombo(wSuccessOn, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wSuccessCondition.add(BaseMessages.getString(PKG, "JobDeleteFolders.SuccessWhenAllWorksFine.Label"));
	  	wSuccessCondition.add(BaseMessages.getString(PKG, "JobDeleteFolders.SuccessWhenAtLeat.Label"));
	  	wSuccessCondition.add(BaseMessages.getString(PKG, "JobDeleteFolders.SuccessWhenErrorsLessThan.Label"));
	  	
	  	wSuccessCondition.select(0); // +1: starts at -1
	  	
		props.setLook(wSuccessCondition);
		fdSuccessCondition= new FormData();
		fdSuccessCondition.left = new FormAttachment(middle, 0);
		fdSuccessCondition.top = new FormAttachment(wSettings, margin);
		fdSuccessCondition.right = new FormAttachment(100, 0);
		wSuccessCondition.setLayoutData(fdSuccessCondition);
		wSuccessCondition.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				activeSuccessCondition();
				
			}
		});

		// Success when number of errors less than
		wlNrErrorsLessThan= new Label(wSuccessOn, SWT.RIGHT);
		wlNrErrorsLessThan.setText(BaseMessages.getString(PKG, "JobDeleteFolders.LimitFolders.Label"));
		props.setLook(wlNrErrorsLessThan);
		fdlNrErrorsLessThan= new FormData();
		fdlNrErrorsLessThan.left = new FormAttachment(0, 0);
		fdlNrErrorsLessThan.top = new FormAttachment(wSuccessCondition, margin);
		fdlNrErrorsLessThan.right = new FormAttachment(middle, -margin);
		wlNrErrorsLessThan.setLayoutData(fdlNrErrorsLessThan);
		
		
		wLimitFolders= new TextVar(jobMeta,wSuccessOn, SWT.SINGLE | SWT.LEFT | SWT.BORDER, BaseMessages.getString(PKG, "JobDeleteFolders.LimitFolders.Tooltip"));
		props.setLook(wLimitFolders);
		wLimitFolders.addModifyListener(lsMod);
		fdNrErrorsLessThan= new FormData();
		fdNrErrorsLessThan.left = new FormAttachment(middle, 0);
		fdNrErrorsLessThan.top = new FormAttachment(wSuccessCondition, margin);
		fdNrErrorsLessThan.right = new FormAttachment(100, -margin);
		wLimitFolders.setLayoutData(fdNrErrorsLessThan);
		
	    fdSuccessOn= new FormData();
	    fdSuccessOn.left = new FormAttachment(0, margin);
	    fdSuccessOn.top = new FormAttachment(wSettings, margin);
	    fdSuccessOn.right = new FormAttachment(100, -margin);
	    wSuccessOn.setLayoutData(fdSuccessOn);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Success ON GROUP
	     // ///////////////////////////////////////////////////////////

		
		// Filename line
		wlFilename=new Label(shell, SWT.RIGHT);
		wlFilename.setText(BaseMessages.getString(PKG, "JobDeleteFolders.Filename.Label"));
		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(wSuccessOn, 2*margin);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);
		
		
		
		// Browse Source folders button ...
		wbDirectory=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbDirectory);
		wbDirectory.setText(BaseMessages.getString(PKG, "JobDeleteFolders.BrowseFolders.Label"));
		fdbDirectory=new FormData();
		fdbDirectory.right= new FormAttachment(100, -margin);
		fdbDirectory.top  = new FormAttachment(wSuccessOn, margin);
		wbDirectory.setLayoutData(fdbDirectory);
		
		wbDirectory.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wFilename.getText()!=null)
					{
						ddialog.setFilterPath(jobMeta.environmentSubstitute(wFilename.getText()) );
					}
					
					 // Calling open() will open and run the dialog.
			        // It will return the selected directory, or
			        // null if user cancels
			        String dir = ddialog.open();
			        if (dir != null) {
			          // Set the text box to the new selection
			        	wFilename.setText(dir);
			        }
					
				}
			}
		);
				

		wbaFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbaFilename);
		wbaFilename.setText(BaseMessages.getString(PKG, "JobDeleteFolders.FilenameAdd.Button"));
		fdbaFilename=new FormData();
		fdbaFilename.right= new FormAttachment(wbDirectory, -margin);
		fdbaFilename.top  = new FormAttachment(wSuccessOn, margin);
		wbaFilename.setLayoutData(fdbaFilename);

		wFilename=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.top  = new FormAttachment(wSuccessOn, 2*margin);
		fdFilename.right= new FormAttachment(wbDirectory, -55);
		wFilename.setLayoutData(fdFilename);

		// Whenever something changes, set the tooltip to the expanded version:
		wFilename.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				wFilename.setToolTipText(jobMeta.environmentSubstitute( wFilename.getText() ) );
			}
		}
		);

		// Buttons to the right of the screen...
		wbdFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbdFilename);
		wbdFilename.setText(BaseMessages.getString(PKG, "JobDeleteFolders.FilenameDelete.Button"));
		wbdFilename.setToolTipText(BaseMessages.getString(PKG, "JobDeleteFolders.FilenameDelete.Tooltip"));
		fdbdFilename=new FormData();
		fdbdFilename.right = new FormAttachment(100, 0);
		fdbdFilename.top  = new FormAttachment (wFilename, 40);
		wbdFilename.setLayoutData(fdbdFilename);

		wbeFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbeFilename);
		wbeFilename.setText(BaseMessages.getString(PKG, "JobDeleteFolders.FilenameEdit.Button"));
		wbeFilename.setToolTipText(BaseMessages.getString(PKG, "JobDeleteFolders.FilenameEdit.Tooltip"));
		fdbeFilename=new FormData();
		fdbeFilename.right = new FormAttachment(100, 0);
		fdbeFilename.left = new FormAttachment(wbdFilename, 0, SWT.LEFT);
		fdbeFilename.top  = new FormAttachment (wbdFilename, margin);
		wbeFilename.setLayoutData(fdbeFilename);

		wlFields = new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "JobDeleteFolders.Fields.Label"));
		props.setLook(wlFields);
		fdlFields = new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.right= new FormAttachment(middle, -margin);
		fdlFields.top = new FormAttachment(wFilename,margin);
		wlFields.setLayoutData(fdlFields);

		int rows = jobEntry.arguments == null
			? 1
			: (jobEntry.arguments.length == 0
			? 0
			: jobEntry.arguments.length);
		final int FieldsRows = rows;

		ColumnInfo[] colinf=new ColumnInfo[]
			{
				new ColumnInfo(BaseMessages.getString(PKG, "JobDeleteFolders.Fields.Argument.Label"),  ColumnInfo.COLUMN_TYPE_TEXT,    false),
			};

		colinf[0].setUsingVariables(true);
		colinf[0].setToolTip(BaseMessages.getString(PKG, "JobDeleteFolders.Fields.Column"));

		wFields = new TableView(jobMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf,
			FieldsRows, lsMod, props);

		fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, -75);
		fdFields.bottom = new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);

		wlFields.setEnabled(!jobEntry.argFromPrevious);
		wFields.setEnabled(!jobEntry.argFromPrevious);

		// Add the file to the list of files...
		SelectionAdapter selA = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				wFields.add(new String[] { wFilename.getText()} );
				wFilename.setText("");
				wFields.removeEmptyRows();
				wFields.setRowNums();
				wFields.optWidth(true);
			}
		};
		wbaFilename.addSelectionListener(selA);
		wFilename.addSelectionListener(selA);

		// Delete files from the list of files...
		wbdFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				int idx[] = wFields.getSelectionIndices();
				wFields.remove(idx);
				wFields.removeEmptyRows();
				wFields.setRowNums();
			}
		});

		// Edit the selected file & remove from the list...
		wbeFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				int idx = wFields.getSelectionIndex();
				if (idx>=0)
				{
					String string[] = wFields.getItem(idx);
					wFilename.setText(string[0]);
					wFields.remove(idx);
				}
				wFields.removeEmptyRows();
				wFields.setRowNums();
			}
		});

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
		wFilename.addSelectionListener( lsDef );

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();
		setPrevious();
		activeSuccessCondition();

		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
	}
	private void activeSuccessCondition()
	{
		wlNrErrorsLessThan.setEnabled(wSuccessCondition.getSelectionIndex()!=0);
		wLimitFolders.setEnabled(wSuccessCondition.getSelectionIndex()!=0);	
	}
	private void setPrevious()
	{
		wlFields.setEnabled(!wPrevious.getSelection());
		wFields.setEnabled(!wPrevious.getSelection());
		
		wFilename.setEnabled(!wPrevious.getSelection());
		wlFilename.setEnabled(!wPrevious.getSelection());

		wbdFilename.setEnabled(!wPrevious.getSelection());
		wbeFilename.setEnabled(!wPrevious.getSelection());
		wbaFilename.setEnabled(!wPrevious.getSelection());
		wbDirectory.setEnabled(!wPrevious.getSelection());
		
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
		if (jobEntry.getName()    != null) wName.setText( jobEntry.getName() );
		wName.selectAll();
		
		if (jobEntry.arguments != null)
		{
			for (int i = 0; i < jobEntry.arguments.length; i++)
			{
				TableItem ti = wFields.table.getItem(i);
				if (jobEntry.arguments[i] != null);
					ti.setText(1, jobEntry.arguments[i]);
			}
			wFields.setRowNums();
			wFields.optWidth(true);
		}
		wPrevious.setSelection(jobEntry.argFromPrevious);		

		if (jobEntry.getLimitFolders()!= null) 
			wLimitFolders.setText( jobEntry.getLimitFolders() );
		else
			wLimitFolders.setText("10");
		
		if(jobEntry.getSuccessCondition()!=null)
		{
			if(jobEntry.getSuccessCondition().equals(jobEntry.SUCCESS_IF_AT_LEAST_X_FOLDERS_DELETED))
				wSuccessCondition.select(1);
			else if(jobEntry.getSuccessCondition().equals(jobEntry.SUCCESS_IF_ERRORS_LESS))
				wSuccessCondition.select(2);
			else
				wSuccessCondition.select(0);	
		}else wSuccessCondition.select(0);
		
		
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
		jobEntry.setPrevious(wPrevious.getSelection());
		jobEntry.setLimitFolders(wLimitFolders.getText());
		
		if(wSuccessCondition.getSelectionIndex()==1)
			jobEntry.setSuccessCondition(jobEntry.SUCCESS_IF_AT_LEAST_X_FOLDERS_DELETED);
		else if(wSuccessCondition.getSelectionIndex()==2)
			jobEntry.setSuccessCondition(jobEntry.SUCCESS_IF_ERRORS_LESS);
		else
			jobEntry.setSuccessCondition(jobEntry.SUCCESS_IF_NO_ERRORS);	
		
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