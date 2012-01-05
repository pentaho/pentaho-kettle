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

package org.pentaho.di.ui.job.entries.copymoveresultfilenames;

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
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.copymoveresultfilenames.JobEntryCopyMoveResultFilenames;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


/**
 * This dialog allows you to edit the Copy/Move result filenames job entry settings.
 *
 * @author Samatar
 * @since  26-02-2008
 */
public class JobEntryCopyMoveResultFilenamesDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryCopyMoveResultFilenames.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	
    private Label        wlSpecifyWildcard;
    private Button       wSpecifyWildcard;
    private FormData     fdlSpecifyWildcard, fdSpecifyWildcard;
    
	private Label        wlWildcard;
	private TextVar      wWildcard;
	private FormData     fdlWildcard, fdWildcard;    
	
	private Label        wlWildcardExclude;
	private TextVar      wWildcardExclude;
	private FormData     fdlWildcardExclude, fdWildcardExclude; 

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;
	
	private Label wlAction;
	private CCombo wAction;
	private FormData fdlAction, fdAction;

	private JobEntryCopyMoveResultFilenames jobEntry;
	private Shell       	shell;

	private SelectionAdapter lsDef;

	private boolean changed;
	
	private Label        wlFoldername;
	private Button       wbFoldername;
	private TextVar      wFoldername;
	private FormData     fdlFoldername, fdbFoldername, fdFoldername;
	
	private Group wLimitTo;
	private FormData fdLimitTo;

	private Label        wlAddDate;
	private Button       wAddDate;
	private FormData     fdlAddDate, fdAddDate;

	private Label        wlAddTime;
	private Button       wAddTime;
	private FormData     fdlAddTime, fdAddTime;
	
	private Label        wlSpecifyFormat;
	private Button       wSpecifyFormat;
	private FormData     fdlSpecifyFormat, fdSpecifyFormat;

  	private Label        wlDateTimeFormat;
	private CCombo      wDateTimeFormat;
	private FormData     fdlDateTimeFormat, fdDateTimeFormat; 
	
	private Group wSuccessOn;
    private FormData fdSuccessOn;
    
    private Label wlSuccessCondition;
	private CCombo wSuccessCondition;
	private FormData fdlSuccessCondition, fdSuccessCondition;
	
	
	private Label        wlAddDateBeforeExtension;
	private Button       wAddDateBeforeExtension;
	private FormData     fdlAddDateBeforeExtension, fdAddDateBeforeExtension;
	
	private Label wlNrErrorsLessThan;
	private TextVar wNrErrorsLessThan;
	private FormData fdlNrErrorsLessThan, fdNrErrorsLessThan;
	
	private Label        wlOverwriteFile;
	private Button       wOverwriteFile;
	private FormData     fdlOverwriteFile, fdOverwriteFile;
	
	private Label        wlCreateDestinationFolder;
	private Button       wCreateDestinationFolder;
	private FormData     fdlCreateDestinationFolder, fdCreateDestinationFolder;
	
	private Label        wlRemovedSourceFilename;
	private Button       wRemovedSourceFilename;
	private FormData     fdlRemovedSourceFilename, fdRemovedSourceFilename;
	
	private Label        wlAddDestinationFilename;
	private Button       wAddDestinationFilename;
	private FormData     fdlAddDestinationFilename, fdAddDestinationFilename;
	
	
	 public JobEntryCopyMoveResultFilenamesDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
	 {	
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryCopyMoveResultFilenames) jobEntryInt;

		if (this.jobEntry.getName() == null) 
			this.jobEntry.setName(BaseMessages.getString(PKG, "JobEntryDeleteResultFilenames.Name.Default"));
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
		shell.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Name line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.Name.Label"));
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
		
		//Copy or Move
	  	wlAction = new Label(shell, SWT.RIGHT);
	  	wlAction.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.Action.Label"));
	  	props.setLook(wlAction);
	  	fdlAction = new FormData();
	  	fdlAction.left = new FormAttachment(0,0);
	  	fdlAction.right = new FormAttachment(middle, -margin);
	  	fdlAction.top = new FormAttachment(wName, 2*margin);
	  	wlAction.setLayoutData(fdlAction);
	  	wAction = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wAction.add(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.Copy.Label"));
	  	wAction.add(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.Move.Label"));
	  	wAction.select(0); // +1: starts at -1
	  	
		props.setLook(wAction);
		fdAction= new FormData();
		fdAction.left = new FormAttachment(middle, 0);
		fdAction.top = new FormAttachment(wName, 2*margin);
		fdAction.right = new FormAttachment(100, 0);
		wAction.setLayoutData(fdAction);
		
		
		// Foldername line
		wlFoldername=new Label(shell, SWT.RIGHT);
		wlFoldername.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.Foldername.Label"));
 		props.setLook(wlFoldername);
		fdlFoldername=new FormData();
		fdlFoldername.left = new FormAttachment(0, 0);
		fdlFoldername.top  = new FormAttachment(wAction, margin);
		fdlFoldername.right= new FormAttachment(middle, -margin);
		wlFoldername.setLayoutData(fdlFoldername);

		wbFoldername=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFoldername);
		wbFoldername.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbFoldername=new FormData();
		fdbFoldername.right= new FormAttachment(100, 0);
		fdbFoldername.top  = new FormAttachment(wAction, 0);
		wbFoldername.setLayoutData(fdbFoldername);

		wFoldername=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFoldername);
		wFoldername.addModifyListener(lsMod);
		fdFoldername=new FormData();
		fdFoldername.left = new FormAttachment(middle, 0);
		fdFoldername.top  = new FormAttachment(wAction, margin);
		fdFoldername.right= new FormAttachment(wbFoldername, -margin);
		wFoldername.setLayoutData(fdFoldername);

		// Whenever something changes, set the tooltip to the expanded version:
		wFoldername.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wFoldername.setToolTipText(jobMeta.environmentSubstitute( wFoldername.getText() ) );
				}
			}
		);

		wbFoldername.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wFoldername.getText()!=null)
					{
						dialog.setFilterPath(jobMeta.environmentSubstitute(wFoldername.getText()) );
					}
				
					String dir=dialog.open();
					if(dir!=null)
					{
						wFoldername.setText(dir);
					}
				}
			}
		);
		
		// Create destination folder
		wlCreateDestinationFolder = new Label(shell, SWT.RIGHT);
		wlCreateDestinationFolder.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.CreateDestinationFolder.Label"));
		props.setLook(wlCreateDestinationFolder);
		fdlCreateDestinationFolder = new FormData();
		fdlCreateDestinationFolder.left = new FormAttachment(0, 0);
		fdlCreateDestinationFolder.top = new FormAttachment(wFoldername, margin);
		fdlCreateDestinationFolder.right = new FormAttachment(middle, -margin);
		wlCreateDestinationFolder.setLayoutData(fdlCreateDestinationFolder);
		wCreateDestinationFolder = new Button(shell, SWT.CHECK);
		props.setLook(wCreateDestinationFolder);
		wCreateDestinationFolder.setToolTipText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.CreateDestinationFolder.Tooltip"));
		fdCreateDestinationFolder = new FormData();
		fdCreateDestinationFolder.left = new FormAttachment(middle, 0);
		fdCreateDestinationFolder.top = new FormAttachment(wFoldername, margin);
		fdCreateDestinationFolder.right = new FormAttachment(100, 0);
		wCreateDestinationFolder.setLayoutData(fdCreateDestinationFolder);
		wCreateDestinationFolder.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		// Overwrite files
		wlOverwriteFile = new Label(shell, SWT.RIGHT);
		wlOverwriteFile.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.OverwriteFile.Label"));
		props.setLook(wlOverwriteFile);
		fdlOverwriteFile = new FormData();
		fdlOverwriteFile.left = new FormAttachment(0, 0);
		fdlOverwriteFile.top = new FormAttachment(wCreateDestinationFolder, margin);
		fdlOverwriteFile.right = new FormAttachment(middle, -margin);
		wlOverwriteFile.setLayoutData(fdlOverwriteFile);
		wOverwriteFile = new Button(shell, SWT.CHECK);
		props.setLook(wOverwriteFile);
		wOverwriteFile.setToolTipText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.OverwriteFile.Tooltip"));
		fdOverwriteFile = new FormData();
		fdOverwriteFile.left = new FormAttachment(middle, 0);
		fdOverwriteFile.top = new FormAttachment(wCreateDestinationFolder, margin);
		fdOverwriteFile.right = new FormAttachment(100, 0);
		wOverwriteFile.setLayoutData(fdOverwriteFile);
		wOverwriteFile.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		// Remove source filename from result filenames
		wlRemovedSourceFilename = new Label(shell, SWT.RIGHT);
		wlRemovedSourceFilename.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.RemovedSourceFilename.Label"));
		props.setLook(wlRemovedSourceFilename);
		fdlRemovedSourceFilename = new FormData();
		fdlRemovedSourceFilename.left = new FormAttachment(0, 0);
		fdlRemovedSourceFilename.top = new FormAttachment(wOverwriteFile, margin);
		fdlRemovedSourceFilename.right = new FormAttachment(middle, -margin);
		wlRemovedSourceFilename.setLayoutData(fdlRemovedSourceFilename);
		wRemovedSourceFilename = new Button(shell, SWT.CHECK);
		props.setLook(wRemovedSourceFilename);
		wRemovedSourceFilename.setToolTipText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.RemovedSourceFilename.Tooltip"));
		fdRemovedSourceFilename = new FormData();
		fdRemovedSourceFilename.left = new FormAttachment(middle, 0);
		fdRemovedSourceFilename.top = new FormAttachment(wOverwriteFile, margin);
		fdRemovedSourceFilename.right = new FormAttachment(100, 0);
		wRemovedSourceFilename.setLayoutData(fdRemovedSourceFilename);
		wRemovedSourceFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		// Add destination filename to result filenames
		wlAddDestinationFilename = new Label(shell, SWT.RIGHT);
		wlAddDestinationFilename.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.AddDestinationFilename.Label"));
		props.setLook(wlAddDestinationFilename);
		fdlAddDestinationFilename = new FormData();
		fdlAddDestinationFilename.left = new FormAttachment(0, 0);
		fdlAddDestinationFilename.top = new FormAttachment(wRemovedSourceFilename, margin);
		fdlAddDestinationFilename.right = new FormAttachment(middle, -margin);
		wlAddDestinationFilename.setLayoutData(fdlAddDestinationFilename);
		wAddDestinationFilename = new Button(shell, SWT.CHECK);
		props.setLook(wAddDestinationFilename);
		wAddDestinationFilename.setToolTipText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.AddDestinationFilename.Tooltip"));
		fdAddDestinationFilename = new FormData();
		fdAddDestinationFilename.left = new FormAttachment(middle, 0);
		fdAddDestinationFilename.top = new FormAttachment(wRemovedSourceFilename, margin);
		fdAddDestinationFilename.right = new FormAttachment(100, 0);
		wAddDestinationFilename.setLayoutData(fdAddDestinationFilename);
		wAddDestinationFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		
		// Create multi-part file?
		wlAddDate=new Label(shell, SWT.RIGHT);
		wlAddDate.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.AddDate.Label"));
 		props.setLook(wlAddDate);
		fdlAddDate=new FormData();
		fdlAddDate.left = new FormAttachment(0, 0);
		fdlAddDate.top  = new FormAttachment(wAddDestinationFilename, margin);
		fdlAddDate.right= new FormAttachment(middle, -margin);
		wlAddDate.setLayoutData(fdlAddDate);
		wAddDate=new Button(shell, SWT.CHECK);
 		props.setLook(wAddDate);
 		wAddDate.setToolTipText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.AddDate.Tooltip"));
		fdAddDate=new FormData();
		fdAddDate.left = new FormAttachment(middle, 0);
		fdAddDate.top  = new FormAttachment(wAddDestinationFilename, margin);
		fdAddDate.right= new FormAttachment(100, 0);
		wAddDate.setLayoutData(fdAddDate);
		wAddDate.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobEntry.setChanged();
					setAddDateBeforeExtension();
				}
			}
		);
		// Create multi-part file?
		wlAddTime=new Label(shell, SWT.RIGHT);
		wlAddTime.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.AddTime.Label"));
 		props.setLook(wlAddTime);
		fdlAddTime=new FormData();
		fdlAddTime.left = new FormAttachment(0, 0);
		fdlAddTime.top  = new FormAttachment(wAddDate, margin);
		fdlAddTime.right= new FormAttachment(middle, -margin);
		wlAddTime.setLayoutData(fdlAddTime);
		wAddTime=new Button(shell, SWT.CHECK);
 		props.setLook(wAddTime);
 		wAddTime.setToolTipText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.AddTime.Tooltip"));
		fdAddTime=new FormData();
		fdAddTime.left = new FormAttachment(middle, 0);
		fdAddTime.top  = new FormAttachment(wAddDate, margin);
		fdAddTime.right= new FormAttachment(100, 0);
		wAddTime.setLayoutData(fdAddTime);
		wAddTime.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobEntry.setChanged();
					setAddDateBeforeExtension();
				}
			}
		);

		// Specify date time format?
		wlSpecifyFormat=new Label(shell, SWT.RIGHT);
		wlSpecifyFormat.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.SpecifyFormat.Label"));
		props.setLook(wlSpecifyFormat);
		fdlSpecifyFormat=new FormData();
		fdlSpecifyFormat.left = new FormAttachment(0, 0);
		fdlSpecifyFormat.top  = new FormAttachment(wAddTime, margin);
		fdlSpecifyFormat.right= new FormAttachment(middle, -margin);
		wlSpecifyFormat.setLayoutData(fdlSpecifyFormat);
		wSpecifyFormat=new Button(shell, SWT.CHECK);
		props.setLook(wSpecifyFormat);
		wSpecifyFormat.setToolTipText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.SpecifyFormat.Tooltip"));
	    fdSpecifyFormat=new FormData();
		fdSpecifyFormat.left = new FormAttachment(middle, 0);
		fdSpecifyFormat.top  = new FormAttachment(wAddTime, margin);
		fdSpecifyFormat.right= new FormAttachment(100, 0);
		wSpecifyFormat.setLayoutData(fdSpecifyFormat);
		wSpecifyFormat.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobEntry.setChanged();
					setDateTimeFormat();
					setAddDateBeforeExtension();
				}
			}
		);

		
		//	Prepare a list of possible DateTimeFormats...
		String dats[] = Const.getDateFormats();
		
 		// DateTimeFormat
		wlDateTimeFormat=new Label(shell, SWT.RIGHT);
        wlDateTimeFormat.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.DateTimeFormat.Label"));
        props.setLook(wlDateTimeFormat);
        fdlDateTimeFormat=new FormData();
        fdlDateTimeFormat.left = new FormAttachment(0, 0);
        fdlDateTimeFormat.top  = new FormAttachment(wSpecifyFormat, margin);
        fdlDateTimeFormat.right= new FormAttachment(middle, -margin);
        wlDateTimeFormat.setLayoutData(fdlDateTimeFormat);
        wDateTimeFormat=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
        wDateTimeFormat.setEditable(true);
        props.setLook(wDateTimeFormat);
        wDateTimeFormat.addModifyListener(lsMod);
        fdDateTimeFormat=new FormData();
        fdDateTimeFormat.left = new FormAttachment(middle, 0);
        fdDateTimeFormat.top  = new FormAttachment(wSpecifyFormat, margin);
        fdDateTimeFormat.right= new FormAttachment(100, 0);
        wDateTimeFormat.setLayoutData(fdDateTimeFormat);
        for (int x=0;x<dats.length;x++) wDateTimeFormat.add(dats[x]);
        
        

        // Add Date befor extension?
        wlAddDateBeforeExtension = new Label(shell, SWT.RIGHT);
        wlAddDateBeforeExtension.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.AddDateBeforeExtension.Label"));
        props.setLook(wlAddDateBeforeExtension);
        fdlAddDateBeforeExtension = new FormData();
        fdlAddDateBeforeExtension.left = new FormAttachment(0, 0);
        fdlAddDateBeforeExtension.top = new FormAttachment(wDateTimeFormat, margin);
        fdlAddDateBeforeExtension.right = new FormAttachment(middle, -margin);
        wlAddDateBeforeExtension.setLayoutData(fdlAddDateBeforeExtension);
        wAddDateBeforeExtension = new Button(shell, SWT.CHECK);
        props.setLook(wAddDateBeforeExtension);
        wAddDateBeforeExtension.setToolTipText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.AddDateBeforeExtension.Tooltip"));
        fdAddDateBeforeExtension = new FormData();
        fdAddDateBeforeExtension.left = new FormAttachment(middle, 0);
        fdAddDateBeforeExtension.top = new FormAttachment(wDateTimeFormat, margin);
        fdAddDateBeforeExtension.right = new FormAttachment(100, 0);
        wAddDateBeforeExtension.setLayoutData(fdAddDateBeforeExtension);
        wAddDateBeforeExtension.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
                CheckLimit();
            }
        });
        
        

		// LimitTo grouping?
		// ////////////////////////
		// START OF LimitTo GROUP
		// 

		wLimitTo = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wLimitTo);
		wLimitTo.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.Group.LimitTo.Label"));

		FormLayout groupLayout = new FormLayout();
		groupLayout.marginWidth = 10;
		groupLayout.marginHeight = 10;
		wLimitTo.setLayout(groupLayout);

		
        // Specify wildcard?
        wlSpecifyWildcard = new Label(wLimitTo, SWT.RIGHT);
        wlSpecifyWildcard.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.SpecifyWildcard.Label"));
        props.setLook(wlSpecifyWildcard);
        fdlSpecifyWildcard = new FormData();
        fdlSpecifyWildcard.left = new FormAttachment(0, 0);
        fdlSpecifyWildcard.top = new FormAttachment(wAddDateBeforeExtension, margin);
        fdlSpecifyWildcard.right = new FormAttachment(middle, -margin);
        wlSpecifyWildcard.setLayoutData(fdlSpecifyWildcard);
        wSpecifyWildcard = new Button(wLimitTo, SWT.CHECK);
        props.setLook(wSpecifyWildcard);
        wSpecifyWildcard.setToolTipText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.SpecifyWildcard.Tooltip"));
        fdSpecifyWildcard = new FormData();
        fdSpecifyWildcard.left = new FormAttachment(middle, 0);
        fdSpecifyWildcard.top = new FormAttachment(wAddDateBeforeExtension, margin);
        fdSpecifyWildcard.right = new FormAttachment(100, 0);
        wSpecifyWildcard.setLayoutData(fdSpecifyWildcard);
        wSpecifyWildcard.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
                CheckLimit();
            }
        });
        

        
		// Wildcard line
		wlWildcard=new Label(wLimitTo, SWT.RIGHT);
		wlWildcard.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.Wildcard.Label"));
 		props.setLook(wlWildcard);
		fdlWildcard=new FormData();
		fdlWildcard.left = new FormAttachment(0, 0);
		fdlWildcard.top  = new FormAttachment(wSpecifyWildcard, margin);
		fdlWildcard.right= new FormAttachment(middle, -margin);
		wlWildcard.setLayoutData(fdlWildcard);
		wWildcard=new TextVar(jobMeta,wLimitTo, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wWildcard.setToolTipText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.Wildcard.Tooltip"));
 		props.setLook(wWildcard);
		wWildcard.addModifyListener(lsMod);
		fdWildcard=new FormData();
		fdWildcard.left = new FormAttachment(middle, 0);
		fdWildcard.top  = new FormAttachment(wSpecifyWildcard, margin);
		fdWildcard.right= new FormAttachment(100, -margin);
		wWildcard.setLayoutData(fdWildcard);
		
		
		// Whenever something changes, set the tooltip to the expanded version:
		wWildcard.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wWildcard.setToolTipText(jobMeta.environmentSubstitute( wWildcard.getText() ) );
				}
			}
		);

		// wWildcardExclude
		wlWildcardExclude=new Label(wLimitTo, SWT.RIGHT);
		wlWildcardExclude.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.WildcardExclude.Label"));
 		props.setLook(wlWildcardExclude);
		fdlWildcardExclude=new FormData();
		fdlWildcardExclude.left = new FormAttachment(0, 0);
		fdlWildcardExclude.top  = new FormAttachment(wWildcard, margin);
		fdlWildcardExclude.right= new FormAttachment(middle, -margin);
		wlWildcardExclude.setLayoutData(fdlWildcardExclude);
		wWildcardExclude=new TextVar(jobMeta,wLimitTo, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wWildcardExclude.setToolTipText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.WildcardExclude.Tooltip"));
 		props.setLook(wWildcardExclude);
		wWildcardExclude.addModifyListener(lsMod);
		fdWildcardExclude=new FormData();
		fdWildcardExclude.left = new FormAttachment(middle, 0);
		fdWildcardExclude.top  = new FormAttachment(wWildcard, margin);
		fdWildcardExclude.right= new FormAttachment(100, -margin);
		wWildcardExclude.setLayoutData(fdWildcardExclude);
		
		
		// Whenever something changes, set the tooltip to the expanded version:
		wWildcardExclude.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wWildcardExclude.setToolTipText(jobMeta.environmentSubstitute( wWildcardExclude.getText() ) );
				}
			}
		);

		

		fdLimitTo = new FormData();
		fdLimitTo.left = new FormAttachment(0, margin);
		fdLimitTo.top = new FormAttachment(wAddDateBeforeExtension, margin);
		fdLimitTo.right = new FormAttachment(100, -margin);
		wLimitTo.setLayoutData(fdLimitTo);
		
		// ///////////////////////////////////////////////////////////
		// / END OF LimitTo GROUP
		// ///////////////////////////////////////////////////////////
		
		
		 // SuccessOngrouping?
	     // ////////////////////////
	     // START OF SUCCESS ON GROUP///
	     // /
	    wSuccessOn= new Group(shell, SWT.SHADOW_NONE);
	    props.setLook(wSuccessOn);
	    wSuccessOn.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.SuccessOn.Group.Label"));

	    FormLayout successongroupLayout = new FormLayout();
	    successongroupLayout.marginWidth = 10;
	    successongroupLayout.marginHeight = 10;

	    wSuccessOn.setLayout(successongroupLayout);
	    

	    //Success Condition
	  	wlSuccessCondition = new Label(wSuccessOn, SWT.RIGHT);
	  	wlSuccessCondition.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.SuccessCondition.Label"));
	  	props.setLook(wlSuccessCondition);
	  	fdlSuccessCondition = new FormData();
	  	fdlSuccessCondition.left = new FormAttachment(0, 0);
	  	fdlSuccessCondition.right = new FormAttachment(middle, 0);
	  	fdlSuccessCondition.top = new FormAttachment(wLimitTo, 2*margin);
	  	wlSuccessCondition.setLayoutData(fdlSuccessCondition);
	  	wSuccessCondition = new CCombo(wSuccessOn, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wSuccessCondition.add(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.SuccessWhenAllWorksFine.Label"));
	  	wSuccessCondition.add(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.SuccessWhenAtLeat.Label"));
	  	wSuccessCondition.add(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.SuccessWhenErrorsLessThan.Label"));
	  	wSuccessCondition.select(0); // +1: starts at -1
	  	
		props.setLook(wSuccessCondition);
		fdSuccessCondition= new FormData();
		fdSuccessCondition.left = new FormAttachment(middle, 0);
		fdSuccessCondition.top = new FormAttachment(wLimitTo, 2*margin);
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
		wlNrErrorsLessThan.setText(BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.NrErrorsLessThan.Label"));
		props.setLook(wlNrErrorsLessThan);
		fdlNrErrorsLessThan= new FormData();
		fdlNrErrorsLessThan.left = new FormAttachment(0, 0);
		fdlNrErrorsLessThan.top = new FormAttachment(wSuccessCondition, margin);
		fdlNrErrorsLessThan.right = new FormAttachment(middle, -margin);
		wlNrErrorsLessThan.setLayoutData(fdlNrErrorsLessThan);
		
		
		wNrErrorsLessThan= new TextVar(jobMeta,wSuccessOn, SWT.SINGLE | SWT.LEFT | SWT.BORDER, BaseMessages.getString(PKG, "JobEntryCopyMoveResultFilenames.NrErrorsLessThan.Tooltip"));
		props.setLook(wNrErrorsLessThan);
		wNrErrorsLessThan.addModifyListener(lsMod);
		fdNrErrorsLessThan= new FormData();
		fdNrErrorsLessThan.left = new FormAttachment(middle, 0);
		fdNrErrorsLessThan.top = new FormAttachment(wSuccessCondition, margin);
		fdNrErrorsLessThan.right = new FormAttachment(100, -margin);
		wNrErrorsLessThan.setLayoutData(fdNrErrorsLessThan);
		
		
	    fdSuccessOn= new FormData();
	    fdSuccessOn.left = new FormAttachment(0, margin);
	    fdSuccessOn.top = new FormAttachment(wLimitTo, margin);
	    fdSuccessOn.right = new FormAttachment(100, -margin);
	    wSuccessOn.setLayoutData(fdSuccessOn);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Success ON GROUP
	     // ///////////////////////////////////////////////////////////

		
		
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
        
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wSuccessOn);

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
		CheckLimit();
		setDateTimeFormat();
		activeSuccessCondition();
		setAddDateBeforeExtension();

		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
	}
	private void setAddDateBeforeExtension()
	{
		wlAddDateBeforeExtension.setEnabled(wAddDate.getSelection()||wAddTime.getSelection()||wSpecifyFormat.getSelection() );
		wAddDateBeforeExtension.setEnabled(wAddDate.getSelection()||wAddTime.getSelection()||wSpecifyFormat.getSelection() );
		if(!wAddDate.getSelection()&& !wAddTime.getSelection()&& !wSpecifyFormat.getSelection())
			wAddDateBeforeExtension.setSelection(false);
	}
	private void activeSuccessCondition()
	{
		wlNrErrorsLessThan.setEnabled(wSuccessCondition.getSelectionIndex()!=0);
		wNrErrorsLessThan.setEnabled(wSuccessCondition.getSelectionIndex()!=0);	
	}
	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}
	private void CheckLimit()
	{
		wlWildcard.setEnabled(wSpecifyWildcard.getSelection());
		wWildcard.setEnabled(wSpecifyWildcard.getSelection());
		wlWildcardExclude.setEnabled(wSpecifyWildcard.getSelection());
		wWildcardExclude.setEnabled(wSpecifyWildcard.getSelection());
	}
	
	private void setDateTimeFormat()
	{
		if(wSpecifyFormat.getSelection())
		{
			wAddDate.setSelection(false);	
			wAddTime.setSelection(false);
		}
		
		wDateTimeFormat.setEnabled(wSpecifyFormat.getSelection());
		wlDateTimeFormat.setEnabled(wSpecifyFormat.getSelection());
		wAddDate.setEnabled(!wSpecifyFormat.getSelection());
		wlAddDate.setEnabled(!wSpecifyFormat.getSelection());
		wAddTime.setEnabled(!wSpecifyFormat.getSelection());
		wlAddTime.setEnabled(!wSpecifyFormat.getSelection());
		
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		if (jobEntry.getName()!= null) wName.setText( jobEntry.getName() );
		wName.selectAll();
		wSpecifyWildcard.setSelection(jobEntry.isSpecifyWildcard());
		if (jobEntry.getWildcard()!= null) wWildcard.setText( jobEntry.getWildcard() );
		if (jobEntry.getWildcardExclude()!= null) wWildcardExclude.setText( jobEntry.getWildcardExclude() );
		

		if (jobEntry.getDestinationFolder()!= null) wFoldername.setText( jobEntry.getDestinationFolder() );

		if (jobEntry.getNrErrorsLessThan()!= null) 
			wNrErrorsLessThan.setText( jobEntry.getNrErrorsLessThan() );
		else
			wNrErrorsLessThan.setText("10");
		

		if(jobEntry.getSuccessCondition()!=null)
		{
			if(jobEntry.getSuccessCondition().equals(jobEntry.SUCCESS_IF_AT_LEAST_X_FILES_UN_ZIPPED))
				wSuccessCondition.select(1);
			else if(jobEntry.getSuccessCondition().equals(jobEntry.SUCCESS_IF_ERRORS_LESS))
				wSuccessCondition.select(2);
			else
				wSuccessCondition.select(0);	
		}else wSuccessCondition.select(0);
		
		
		
		if(jobEntry.getAction()!=null)
		{
			if(jobEntry.getAction().equals("move"))
				wAction.select(1);
			else
				wAction.select(0);	
		}else wAction.select(0);
		
		if (jobEntry.getDateTimeFormat()!= null) wDateTimeFormat.setText( jobEntry.getDateTimeFormat() );
		
		wAddDate.setSelection(jobEntry.isAddDate());
		wAddTime.setSelection(jobEntry.isAddTime());
		wSpecifyFormat.setSelection(jobEntry.isSpecifyFormat());
		wAddDateBeforeExtension.setSelection(jobEntry.isAddDateBeforeExtension());
		wOverwriteFile.setSelection(jobEntry.isOverwriteFile());
		wCreateDestinationFolder.setSelection(jobEntry.isCreateDestinationFolder());
		wRemovedSourceFilename.setSelection(jobEntry.isRemovedSourceFilename());
		wAddDestinationFilename.setSelection(jobEntry.isAddDestinationFilename());
		
		
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
		jobEntry.setSpecifyWildcard(wSpecifyWildcard.getSelection());
		jobEntry.setWildcard(wWildcard.getText());
		jobEntry.setWildcardExclude(wWildcardExclude.getText());

		jobEntry.setDestinationFolder(wFoldername.getText());
		jobEntry.setNrErrorsLessThan(wNrErrorsLessThan.getText());
		

		if(wSuccessCondition.getSelectionIndex()==1)
			jobEntry.setSuccessCondition(jobEntry.SUCCESS_IF_AT_LEAST_X_FILES_UN_ZIPPED);
		else if(wSuccessCondition.getSelectionIndex()==2)
			jobEntry.setSuccessCondition(jobEntry.SUCCESS_IF_ERRORS_LESS);
		else
			jobEntry.setSuccessCondition(jobEntry.SUCCESS_IF_NO_ERRORS);
		
		if(wAction.getSelectionIndex()==1)
			jobEntry.setAction("move");
		else
			jobEntry.setAction("copy");	
		
		jobEntry.setAddDate(wAddDate.getSelection());
		jobEntry.setAddTime(wAddTime.getSelection());
		jobEntry.setSpecifyFormat(wSpecifyFormat.getSelection());
		jobEntry.setDateTimeFormat(wDateTimeFormat.getText());
		jobEntry.setAddDateBeforeExtension(wAddDateBeforeExtension.getSelection());
		jobEntry.setOverwriteFile(wOverwriteFile.getSelection());
		
		jobEntry.setCreateDestinationFolder(wCreateDestinationFolder.getSelection());
		jobEntry.setRemovedSourceFilename(wRemovedSourceFilename.getSelection());
		jobEntry.setAddDestinationFilename(wAddDestinationFilename.getSelection());
		
		
		
		
		
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