/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 **                                                                   **
 **                                                                   **
 **********************************************************************/


package org.pentaho.di.ui.job.entries.unzip;

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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog; 
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.job.entries.unzip.JobEntryUnZip;
import org.pentaho.di.job.entries.unzip.Messages;


/**
 * This dialog allows you to edit the Unzip job entry settings.
 *
 * @author Samatar Hassan
 * @since  25-09-2007
 */
public class JobEntryUnZipDialog extends JobEntryDialog implements JobEntryDialogInterface
{

   private static final String[] FILETYPES = new String[] {
			Messages.getString("JobUnZip.Filetype.Zip"),
			Messages.getString("JobUnZip.Filetype.All")};
	
	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlZipFilename;
	private Button       wbZipFilename;
	private TextVar      wZipFilename;
	private FormData     fdlZipFilename, fdbZipFilename, fdZipFilename;
	
 
	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntryUnZip jobEntry;
	private Shell       	shell;

	private Label wlTargetDirectory;
	private TextVar wTargetDirectory;
	private FormData fdlTargetDirectory, fdTargetDirectory;

	private Label wlMovetoDirectory;
	private TextVar wMovetoDirectory;
	private FormData fdlMovetoDirectory, fdMovetoDirectory;

	private Label wlWildcard;
	private TextVar wWildcard;
	private FormData fdlWildcard, fdWildcard;

	private Label wlWildcardExclude;
	private TextVar wWildcardExclude;
	private FormData fdlWildcardExclude, fdWildcardExclude;

	private Label wlAfterUnZip;
	private CCombo wAfterUnZip;
	private FormData fdlAfterUnZip, fdAfterUnZip;

	private SelectionAdapter lsDef;
	
	private Group wFileResult;
    private FormData fdFileResult;
    
	//  Add File to result
	private Label        wlAddFileToResult;
	private Button       wAddFileToResult;
	private FormData     fdlAddFileToResult, fdAddFileToResult;
	
    private Button wbTargetDirectory;
    private FormData fdbTargetDirectory;
    
    private Button wbMovetoDirectory;
    private FormData fdbMovetoDirectory;

	private boolean changed;

    public JobEntryUnZipDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryUnZip) jobEntryInt;
        if (this.jobEntry.getName() == null) 
			this.jobEntry.setName(Messages.getString("JobUnZip.Name.Default"));
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
		shell.setText(Messages.getString("JobUnZip.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// ZipFilename line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(Messages.getString("JobUnZip.Name.Label"));
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
		
		
		// ZipFilename line
		wlZipFilename=new Label(shell, SWT.RIGHT);
		wlZipFilename.setText(Messages.getString("JobUnZip.ZipFilename.Label"));
		props.setLook(wlZipFilename);
		fdlZipFilename=new FormData();
		fdlZipFilename.left = new FormAttachment(0, 0);
		fdlZipFilename.top  = new FormAttachment(wName, margin);
		fdlZipFilename.right= new FormAttachment(middle, -margin);
		wlZipFilename.setLayoutData(fdlZipFilename);
		wbZipFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbZipFilename);
		wbZipFilename.setText(Messages.getString("System.Button.Browse"));
		fdbZipFilename=new FormData();
		fdbZipFilename.right= new FormAttachment(100, 0);
		fdbZipFilename.top  = new FormAttachment(wName, 0);
		wbZipFilename.setLayoutData(fdbZipFilename);
		wZipFilename=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wZipFilename);
		wZipFilename.addModifyListener(lsMod);
		fdZipFilename=new FormData();
		fdZipFilename.left = new FormAttachment(middle, 0);
		fdZipFilename.top  = new FormAttachment(wName, margin);
		fdZipFilename.right= new FormAttachment(wbZipFilename, -margin);
		wZipFilename.setLayoutData(fdZipFilename);

		// Whenever something changes, set the tooltip to the expanded version:
		wZipFilename.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wZipFilename.setToolTipText(jobMeta.environmentSubstitute( wZipFilename.getText() ) );
				}
			}
		);

		wbZipFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					//dialog.setFilterExtensions(new String[] {"*"});
					dialog.setFilterExtensions(new String[] {"*.zip;*.ZIP", "*"});
					if (wZipFilename.getText()!=null)
					{
						dialog.setFileName(jobMeta.environmentSubstitute(wZipFilename.getText()) );
					}
					dialog.setFilterNames(FILETYPES);
					if (dialog.open()!=null)
					{
						wZipFilename.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
					}
				}
			}
		);

		

		// TargetDirectory line
		wlTargetDirectory = new Label(shell, SWT.RIGHT);
		wlTargetDirectory.setText(Messages.getString("JobUnZip.TargetDir.Label"));
		props.setLook(wlTargetDirectory);
		fdlTargetDirectory = new FormData();
		fdlTargetDirectory.left = new FormAttachment(0, 0);
		fdlTargetDirectory.top = new FormAttachment(wZipFilename, margin);
		fdlTargetDirectory.right = new FormAttachment(middle, -margin);
		wlTargetDirectory.setLayoutData(fdlTargetDirectory);
		
        
        // Browse folders button ...
		wbTargetDirectory=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbTargetDirectory);
		wbTargetDirectory.setText(Messages.getString("JobUnZip.BrowseFolders.Label"));
		fdbTargetDirectory=new FormData();
		fdbTargetDirectory.right= new FormAttachment(100, 0);
		fdbTargetDirectory.top  = new FormAttachment(wZipFilename, margin);
		wbTargetDirectory.setLayoutData(fdbTargetDirectory);
		
		wTargetDirectory = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages.getString("JobUnZip.TargetDir.Tooltip"));
		props.setLook(wTargetDirectory);
		wTargetDirectory.addModifyListener(lsMod);
		fdTargetDirectory = new FormData();
		fdTargetDirectory.left = new FormAttachment(middle, 0);
		fdTargetDirectory.top = new FormAttachment(wZipFilename, margin);
		fdTargetDirectory.right = new FormAttachment(wbTargetDirectory, -margin);
		wTargetDirectory.setLayoutData(fdTargetDirectory);
		
		// Wildcard line
		wlWildcard = new Label(shell, SWT.RIGHT);
		wlWildcard.setText(Messages.getString("JobUnZip.Wildcard.Label"));
		props.setLook(wlWildcard);
		fdlWildcard = new FormData();
		fdlWildcard.left = new FormAttachment(0, 0);
		fdlWildcard.top = new FormAttachment(wTargetDirectory, margin);
		fdlWildcard.right = new FormAttachment(middle, -margin);
		wlWildcard.setLayoutData(fdlWildcard);
		wWildcard = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages.getString("JobUnZip.Wildcard.Tooltip"));
		props.setLook(wWildcard);
		wWildcard.addModifyListener(lsMod);
		fdWildcard = new FormData();
		fdWildcard.left = new FormAttachment(middle, 0);
		fdWildcard.top = new FormAttachment(wTargetDirectory, margin);
		fdWildcard.right = new FormAttachment(100, 0);
		wWildcard.setLayoutData(fdWildcard);
		
		// Wildcard to exclude
		wlWildcardExclude = new Label(shell, SWT.RIGHT);
		wlWildcardExclude.setText(Messages.getString("JobUnZip.WildcardExclude.Label"));
		props.setLook(wlWildcardExclude);
		fdlWildcardExclude = new FormData();
		fdlWildcardExclude.left = new FormAttachment(0, 0);
		fdlWildcardExclude.top = new FormAttachment(wWildcard, margin);
		fdlWildcardExclude.right = new FormAttachment(middle, -margin);
		wlWildcardExclude.setLayoutData(fdlWildcardExclude);
		wWildcardExclude = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages.getString("JobUnZip.WildcardExclude.Tooltip"));
		props.setLook(wWildcardExclude);
		wWildcardExclude.addModifyListener(lsMod);
		fdWildcardExclude = new FormData();
		fdWildcardExclude.left = new FormAttachment(middle, 0);
		fdWildcardExclude.top = new FormAttachment(wWildcard, margin);
		fdWildcardExclude.right = new FormAttachment(100, 0);
		wWildcardExclude.setLayoutData(fdWildcardExclude);

		

		//After Zipping
		wlAfterUnZip = new Label(shell, SWT.RIGHT);
		wlAfterUnZip.setText(Messages.getString("JobUnZip.AfterUnZip.Label"));
		props.setLook(wlAfterUnZip);
		fdlAfterUnZip = new FormData();
		fdlAfterUnZip.left = new FormAttachment(0, 0);
		fdlAfterUnZip.right = new FormAttachment(middle, 0);
		fdlAfterUnZip.top = new FormAttachment(wWildcardExclude, margin);
		wlAfterUnZip.setLayoutData(fdlAfterUnZip);
		wAfterUnZip = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wAfterUnZip.add(Messages.getString("JobUnZip.Do_Nothing_AfterUnZip.Label"));
		wAfterUnZip.add(Messages.getString("JobUnZip.Delete_Files_AfterUnZip.Label"));
		wAfterUnZip.add(Messages.getString("JobUnZip.Move_Files_AfterUnZip.Label"));

		wAfterUnZip.select(0); // +1: starts at -1

		props.setLook(wAfterUnZip);
		fdAfterUnZip= new FormData();
		fdAfterUnZip.left = new FormAttachment(middle, 0);
		fdAfterUnZip.top = new FormAttachment(wWildcardExclude, margin);
		fdAfterUnZip.right = new FormAttachment(100, 0);
		wAfterUnZip.setLayoutData(fdAfterUnZip);


		wAfterUnZip.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				AfterUnZipActivate();
				
			}
		});

		// moveTo Directory
		wlMovetoDirectory = new Label(shell, SWT.RIGHT);
		wlMovetoDirectory.setText(Messages.getString("JobUnZip.MovetoDirectory.Label"));
		props.setLook(wlMovetoDirectory);
		fdlMovetoDirectory = new FormData();
		fdlMovetoDirectory.left = new FormAttachment(0, 0);
		fdlMovetoDirectory.top = new FormAttachment(wAfterUnZip, margin);
		fdlMovetoDirectory.right = new FormAttachment(middle, -margin);
		wlMovetoDirectory.setLayoutData(fdlMovetoDirectory);
		wMovetoDirectory = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages.getString("JobUnZip.MovetoDirectory.Tooltip"));
		props.setLook(wMovetoDirectory);
		
	    // Browse folders button ...
		wbMovetoDirectory=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbMovetoDirectory);
		wbMovetoDirectory.setText(Messages.getString("JobUnZip.BrowseFolders.Label"));
		fdbMovetoDirectory=new FormData();
		fdbMovetoDirectory.right= new FormAttachment(100, 0);
		fdbMovetoDirectory.top  = new FormAttachment(wAfterUnZip, margin);
		wbMovetoDirectory.setLayoutData(fdbMovetoDirectory);
		
		
		wMovetoDirectory.addModifyListener(lsMod);
		fdMovetoDirectory = new FormData();
		fdMovetoDirectory.left = new FormAttachment(middle, 0);
		fdMovetoDirectory.top = new FormAttachment(wAfterUnZip, margin);
		fdMovetoDirectory.right = new FormAttachment(wbMovetoDirectory, -margin);
		wMovetoDirectory.setLayoutData(fdMovetoDirectory);
		
		
		  // file result grouping?
        // ////////////////////////
        // START OF LOGGING GROUP///
        // /
        wFileResult = new Group(shell, SWT.SHADOW_NONE);
        props.setLook(wFileResult);
        wFileResult.setText(Messages.getString("JobUnZip.FileResult.Group.Label"));

        FormLayout groupLayout = new FormLayout();
        groupLayout.marginWidth = 10;
        groupLayout.marginHeight = 10;

        wFileResult.setLayout(groupLayout);
        
        
    	//Add file to result
		wlAddFileToResult = new Label(wFileResult, SWT.RIGHT);
		wlAddFileToResult.setText(Messages.getString("JobUnZip.AddFileToResult.Label"));
		props.setLook(wlAddFileToResult);
		fdlAddFileToResult = new FormData();
		fdlAddFileToResult.left = new FormAttachment(0, 0);
		fdlAddFileToResult.top = new FormAttachment(wMovetoDirectory, margin);
		fdlAddFileToResult.right = new FormAttachment(middle, -margin);
		wlAddFileToResult.setLayoutData(fdlAddFileToResult);
		wAddFileToResult = new Button(wFileResult, SWT.CHECK);
		props.setLook(wAddFileToResult);
		wAddFileToResult.setToolTipText(Messages.getString("JobUnZip.AddFileToResult.Tooltip"));
		fdAddFileToResult = new FormData();
		fdAddFileToResult.left = new FormAttachment(middle, 0);
		fdAddFileToResult.top = new FormAttachment(wMovetoDirectory, margin);
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
        fdFileResult.top = new FormAttachment(wMovetoDirectory, margin);
        fdFileResult.right = new FormAttachment(100, -margin);
        wFileResult.setLayoutData(fdFileResult);
        // ///////////////////////////////////////////////////////////
        // / END OF LOGGING GROUP
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

		
		wbTargetDirectory.addSelectionListener
			(
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
						if (wTargetDirectory.getText()!=null)
						{
							ddialog.setFilterPath(jobMeta.environmentSubstitute(wTargetDirectory.getText()) );
						}
						
						 // Calling open() will open and run the dialog.
				        // It will return the selected directory, or
				        // null if user cancels
				        String dir = ddialog.open();
				        if (dir != null) {
				          // Set the text box to the new selection
				        	wTargetDirectory.setText(dir);
				        }
						
					}
				}
			);
			
		
		wbMovetoDirectory.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wMovetoDirectory.getText()!=null)
					{
						ddialog.setFilterPath(jobMeta.environmentSubstitute(wMovetoDirectory.getText()) );
					}
					
					 // Calling open() will open and run the dialog.
			        // It will return the selected directory, or
			        // null if user cancels
			        String dir = ddialog.open();
			        if (dir != null) {
			          // Set the text box to the new selection
			        	wMovetoDirectory.setText(dir);
			        }
					
				}
			}
		);
		
		wName.addSelectionListener( lsDef );
		wZipFilename.addSelectionListener( lsDef );

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();

		AfterUnZipActivate();

		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
	}

	public void AfterUnZipActivate()
	{

		jobEntry.setChanged();
		if (wAfterUnZip.getSelectionIndex()==2)
		{
			wMovetoDirectory.setEnabled(true);
			wlMovetoDirectory.setEnabled(true);
			wbMovetoDirectory.setEnabled(true);
		}
		else
		{
			wMovetoDirectory.setEnabled(false);
			wlMovetoDirectory.setEnabled(false);
			wbMovetoDirectory.setEnabled(false);
		}
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
		if (jobEntry.getZipFilename()!= null) wZipFilename.setText( jobEntry.getZipFilename() );


		if (jobEntry.getWildcard()!= null) wWildcard.setText( jobEntry.getWildcard() );
		if (jobEntry.getWildcardExclude()!= null) wWildcardExclude.setText( jobEntry.getWildcardExclude() );
		if (jobEntry.getSourceDirectory()!= null) wTargetDirectory.setText( jobEntry.getSourceDirectory() );
		if (jobEntry.getMoveToDirectory()!= null) wMovetoDirectory.setText( jobEntry.getMoveToDirectory() );

		if (jobEntry.afterunzip>=0)
		{
			wAfterUnZip.select(jobEntry.afterunzip );
		}

		else
		{
			wAfterUnZip.select(0 ); // NOTHING
		}
		
		wAddFileToResult.setSelection(jobEntry.isAddFileToResult());
		
	}

	private void cancel()
	{
		jobEntry.setChanged(changed);
		jobEntry=null;
		dispose();
	}

	private void ok()
	{
		jobEntry.setName(wName.getText());
		jobEntry.setZipFilename(wZipFilename.getText());


		jobEntry.setWildcard(wWildcard.getText());
		jobEntry.setWildcardExclude(wWildcardExclude.getText());
		jobEntry.setSourceDirectory(wTargetDirectory.getText());

		jobEntry.setMoveToDirectory(wMovetoDirectory.getText());
		
		
		jobEntry.afterunzip = wAfterUnZip.getSelectionIndex();
		
		jobEntry.setAddFileToResult(wAddFileToResult.getSelection());
	
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