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

package org.pentaho.di.ui.job.entries.zipfile;

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
import org.pentaho.di.job.entries.zipfile.JobEntryZipFile;
import org.pentaho.di.job.entries.zipfile.Messages;


/**
 * This dialog allows you to edit the Create File job entry settings.
 *
 * @author Samatar Hassan
 * @since  27-02-2007
 */
public class JobEntryZipFileDialog extends JobEntryDialog implements JobEntryDialogInterface
{
   private static final String[] FILETYPES = new String[] {
			Messages.getString("JobZipFiles.Filetype.Zip"),
			Messages.getString("JobZipFiles.Filetype.All")};
	
	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlZipFilename;
	private Button       wbZipFilename,wbSourceDirectory,wbMovetoDirectory;
	private TextVar      wZipFilename;
	private FormData     fdlZipFilename, fdbZipFilename, fdZipFilename,fdbSourceDirectory,fdbMovetoDirectory;
	
 
	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntryZipFile jobEntry;
	private Shell       	shell;

	private Label wlSourceDirectory;
	private TextVar wSourceDirectory;
	private FormData fdlSourceDirectory, fdSourceDirectory;

	private Label wlMovetoDirectory;
	private TextVar wMovetoDirectory;
	private FormData fdlMovetoDirectory, fdMovetoDirectory;

	private Label wlWildcard;
	private TextVar wWildcard;
	private FormData fdlWildcard, fdWildcard;

	private Label wlWildcardExclude;
	private TextVar wWildcardExclude;
	private FormData fdlWildcardExclude, fdWildcardExclude;

	private Label wlCompressionRate;
	private  CCombo wCompressionRate;
	private FormData fdlCompressionRate, fdCompressionRate;

	private Label wlIfFileExists;
	private  CCombo wIfFileExists;
	private FormData fdlIfFileExists, fdIfFileExists;

	private Label wlAfterZip;
	private CCombo wAfterZip;
	private FormData fdlAfterZip, fdAfterZip;

	private SelectionAdapter lsDef;
	
	private Group wFileResult;
    private FormData fdFileResult;
    
	//  Add File to result
	private Label        wlAddFileToResult;
	private Button       wAddFileToResult;
	private FormData     fdlAddFileToResult, fdAddFileToResult;
	
	private boolean changed;

    public JobEntryZipFileDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryZipFile) jobEntryInt;
        if (this.jobEntry.getName() == null) 
			this.jobEntry.setName(Messages.getString("JobZipFiles.Name.Default"));
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
		shell.setText(Messages.getString("JobZipFiles.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// ZipFilename line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(Messages.getString("JobZipFiles.Name.Label"));
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

		// TargetDirectory line
		wlSourceDirectory = new Label(shell, SWT.RIGHT);
		wlSourceDirectory.setText(Messages.getString("JobZipFiles.SourceDir.Label"));
		props.setLook(wlSourceDirectory);
		fdlSourceDirectory = new FormData();
		fdlSourceDirectory.left = new FormAttachment(0, 0);
		fdlSourceDirectory.top = new FormAttachment(wName, margin);
		fdlSourceDirectory.right = new FormAttachment(middle, -margin);
		wlSourceDirectory.setLayoutData(fdlSourceDirectory);

		
		wbSourceDirectory=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbSourceDirectory);
		wbSourceDirectory.setText(Messages.getString("System.Button.Browse"));
		fdbSourceDirectory=new FormData();
		fdbSourceDirectory.right= new FormAttachment(100, 0);
		fdbSourceDirectory.top  = new FormAttachment(wName, 0);
		wbSourceDirectory.setLayoutData(fdbSourceDirectory);
		
		
		wSourceDirectory=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSourceDirectory);
		wSourceDirectory.addModifyListener(lsMod);
		fdSourceDirectory=new FormData();
		fdSourceDirectory.left = new FormAttachment(middle, 0);
		fdSourceDirectory.top  = new FormAttachment(wName, margin);
		fdSourceDirectory.right= new FormAttachment(wbSourceDirectory, -margin);
		wSourceDirectory.setLayoutData(fdSourceDirectory);

		// Whenever something changes, set the tooltip to the expanded version:
		wSourceDirectory.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wSourceDirectory.setToolTipText(jobMeta.environmentSubstitute( wSourceDirectory.getText() ) );
				}
			}
		);

		wbSourceDirectory.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wSourceDirectory.getText()!=null)
					{
						ddialog.setFilterPath(jobMeta.environmentSubstitute(wSourceDirectory.getText()) );
					}
					
					 // Calling open() will open and run the dialog.
			        // It will return the selected directory, or
			        // null if user cancels
			        String dir = ddialog.open();
			        if (dir != null) {
			          // Set the text box to the new selection
			        	wSourceDirectory.setText(dir);
			        }
					
				}
			}
		);

		
		
		// Wildcard line
		wlWildcard = new Label(shell, SWT.RIGHT);
		wlWildcard.setText(Messages.getString("JobZipFiles.Wildcard.Label"));
		props.setLook(wlWildcard);
		fdlWildcard = new FormData();
		fdlWildcard.left = new FormAttachment(0, 0);
		fdlWildcard.top = new FormAttachment(wSourceDirectory, margin);
		fdlWildcard.right = new FormAttachment(middle, -margin);
		wlWildcard.setLayoutData(fdlWildcard);
		wWildcard = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages.getString("JobZipFiles.Wildcard.Tooltip"));
		props.setLook(wWildcard);
		wWildcard.addModifyListener(lsMod);
		fdWildcard = new FormData();
		fdWildcard.left = new FormAttachment(middle, 0);
		fdWildcard.top = new FormAttachment(wSourceDirectory, margin);
		fdWildcard.right = new FormAttachment(100, 0);
		wWildcard.setLayoutData(fdWildcard);
		
		// Wildcard to exclude
		wlWildcardExclude = new Label(shell, SWT.RIGHT);
		wlWildcardExclude.setText(Messages.getString("JobZipFiles.WildcardExclude.Label"));
		props.setLook(wlWildcardExclude);
		fdlWildcardExclude = new FormData();
		fdlWildcardExclude.left = new FormAttachment(0, 0);
		fdlWildcardExclude.top = new FormAttachment(wWildcard, margin);
		fdlWildcardExclude.right = new FormAttachment(middle, -margin);
		wlWildcardExclude.setLayoutData(fdlWildcardExclude);
		wWildcardExclude = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages.getString("JobZipFiles.WildcardExclude.Tooltip"));
		props.setLook(wWildcardExclude);
		wWildcardExclude.addModifyListener(lsMod);
		fdWildcardExclude = new FormData();
		fdWildcardExclude.left = new FormAttachment(middle, 0);
		fdWildcardExclude.top = new FormAttachment(wWildcard, margin);
		fdWildcardExclude.right = new FormAttachment(100, 0);
		wWildcardExclude.setLayoutData(fdWildcardExclude);

		// ZipFilename line
		wlZipFilename=new Label(shell, SWT.RIGHT);
		wlZipFilename.setText(Messages.getString("JobZipFiles.ZipFilename.Label"));
		props.setLook(wlZipFilename);
		fdlZipFilename=new FormData();
		fdlZipFilename.left = new FormAttachment(0, 0);
		fdlZipFilename.top  = new FormAttachment(wWildcardExclude, margin);
		fdlZipFilename.right= new FormAttachment(middle, -margin);
		wlZipFilename.setLayoutData(fdlZipFilename);
		wbZipFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbZipFilename);
		wbZipFilename.setText(Messages.getString("System.Button.Browse"));
		fdbZipFilename=new FormData();
		fdbZipFilename.right= new FormAttachment(100, 0);
		fdbZipFilename.top  = new FormAttachment(wWildcardExclude, 0);
		wbZipFilename.setLayoutData(fdbZipFilename);
		wZipFilename=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wZipFilename);
		wZipFilename.addModifyListener(lsMod);
		fdZipFilename=new FormData();
		fdZipFilename.left = new FormAttachment(middle, 0);
		fdZipFilename.top  = new FormAttachment(wWildcardExclude, margin);
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

		//Compression Rate
		wlCompressionRate = new Label(shell, SWT.RIGHT);
		wlCompressionRate.setText(Messages.getString("JobZipFiles.CompressionRate.Label"));
		props.setLook(wlCompressionRate);
		fdlCompressionRate = new FormData();
		fdlCompressionRate.left = new FormAttachment(0, 0);
		fdlCompressionRate.right = new FormAttachment(middle, 0);
		fdlCompressionRate.top = new FormAttachment(wZipFilename, margin);
		wlCompressionRate.setLayoutData(fdlCompressionRate);
		wCompressionRate = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wCompressionRate.add(Messages.getString("JobZipFiles.NO_COMP_CompressionRate.Label"));
		wCompressionRate.add(Messages.getString("JobZipFiles.DEF_COMP_CompressionRate.Label"));
		wCompressionRate.add(Messages.getString("JobZipFiles.BEST_COMP_CompressionRate.Label"));
		wCompressionRate.add(Messages.getString("JobZipFiles.BEST_SPEED_CompressionRate.Label"));
		wCompressionRate.select(1); // +1: starts at -1

		props.setLook(wCompressionRate);
		fdCompressionRate= new FormData();
		fdCompressionRate.left = new FormAttachment(middle, 0);
		fdCompressionRate.top = new FormAttachment(wZipFilename, margin);
		fdCompressionRate.right = new FormAttachment(100, 0);
		wCompressionRate.setLayoutData(fdCompressionRate);

		fdCompressionRate = new FormData();
		fdCompressionRate.left = new FormAttachment(middle, 0);
		fdCompressionRate.top = new FormAttachment(wZipFilename, margin);
		fdCompressionRate.right = new FormAttachment(100, 0);
		wCompressionRate.setLayoutData(fdCompressionRate);
	
		//IF File Exists
		wlIfFileExists = new Label(shell, SWT.RIGHT);
		wlIfFileExists.setText(Messages.getString("JobZipFiles.IfZipFileExists.Label"));
		props.setLook(wlIfFileExists);
		fdlIfFileExists = new FormData();
		fdlIfFileExists.left = new FormAttachment(0, 0);
		fdlIfFileExists.right = new FormAttachment(middle, 0);
		fdlIfFileExists.top = new FormAttachment(wCompressionRate, margin);
		wlIfFileExists.setLayoutData(fdlIfFileExists);
		wIfFileExists = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wIfFileExists.add(Messages.getString("JobZipFiles.Create_NewFile_IfFileExists.Label"));
		wIfFileExists.add(Messages.getString("JobZipFiles.Append_File_IfFileExists.Label"));
		wIfFileExists.add(Messages.getString("JobZipFiles.Do_Nothing_IfFileExists.Label"));

		wIfFileExists.add(Messages.getString("JobZipFiles.Fail_IfFileExists.Label"));
		wIfFileExists.select(3); // +1: starts at -1

		props.setLook(wIfFileExists);
		fdIfFileExists= new FormData();
		fdIfFileExists.left = new FormAttachment(middle, 0);
		fdIfFileExists.top = new FormAttachment(wCompressionRate, margin);
		fdIfFileExists.right = new FormAttachment(100, 0);
		wIfFileExists.setLayoutData(fdIfFileExists);

		fdIfFileExists = new FormData();
		fdIfFileExists.left = new FormAttachment(middle, 0);
		fdIfFileExists.top = new FormAttachment(wCompressionRate, margin);
		fdIfFileExists.right = new FormAttachment(100, 0);
		wIfFileExists.setLayoutData(fdIfFileExists);

		//After Zipping
		wlAfterZip = new Label(shell, SWT.RIGHT);
		wlAfterZip.setText(Messages.getString("JobZipFiles.AfterZip.Label"));
		props.setLook(wlAfterZip);
		fdlAfterZip = new FormData();
		fdlAfterZip.left = new FormAttachment(0, 0);
		fdlAfterZip.right = new FormAttachment(middle, 0);
		fdlAfterZip.top = new FormAttachment(wIfFileExists, margin);
		wlAfterZip.setLayoutData(fdlAfterZip);
		wAfterZip = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wAfterZip.add(Messages.getString("JobZipFiles.Do_Nothing_AfterZip.Label"));
		wAfterZip.add(Messages.getString("JobZipFiles.Delete_Files_AfterZip.Label"));
		wAfterZip.add(Messages.getString("JobZipFiles.Move_Files_AfterZip.Label"));

		wAfterZip.select(0); // +1: starts at -1

		props.setLook(wAfterZip);
		fdAfterZip= new FormData();
		fdAfterZip.left = new FormAttachment(middle, 0);
		fdAfterZip.top = new FormAttachment(wIfFileExists, margin);
		fdAfterZip.right = new FormAttachment(100, 0);
		wAfterZip.setLayoutData(fdAfterZip);

		fdAfterZip = new FormData();
		fdAfterZip.left = new FormAttachment(middle, 0);
		fdAfterZip.top = new FormAttachment(wIfFileExists, margin);
		fdAfterZip.right = new FormAttachment(100, 0);
		wAfterZip.setLayoutData(fdAfterZip);

		wAfterZip.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				AfterZipActivate();
				
			}
		});

		// moveTo Directory
		wlMovetoDirectory = new Label(shell, SWT.RIGHT);
		wlMovetoDirectory.setText(Messages.getString("JobZipFiles.MovetoDirectory.Label"));
		props.setLook(wlMovetoDirectory);
		fdlMovetoDirectory = new FormData();
		fdlMovetoDirectory.left = new FormAttachment(0, 0);
		fdlMovetoDirectory.top = new FormAttachment(wAfterZip, margin);
		fdlMovetoDirectory.right = new FormAttachment(middle, -margin);
		wlMovetoDirectory.setLayoutData(fdlMovetoDirectory);
		
		
		wbMovetoDirectory=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbMovetoDirectory);
		wbMovetoDirectory.setText(Messages.getString("System.Button.Browse"));
		fdbMovetoDirectory=new FormData();
		fdbMovetoDirectory.right= new FormAttachment(100, 0);
		fdbMovetoDirectory.top  = new FormAttachment(wAfterZip, 0);
		wbMovetoDirectory.setLayoutData(fdbMovetoDirectory);
		
		
		wMovetoDirectory = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages.getString("JobZipFiles.MovetoDirectory.Tooltip"));
		props.setLook(wMovetoDirectory);
		wMovetoDirectory.addModifyListener(lsMod);
		fdMovetoDirectory=new FormData();
		fdMovetoDirectory.left = new FormAttachment(middle, 0);
		fdMovetoDirectory.top  = new FormAttachment(wAfterZip, margin);
		fdMovetoDirectory.right= new FormAttachment(wbMovetoDirectory, -margin);
		wMovetoDirectory.setLayoutData(fdMovetoDirectory);

		// Whenever something changes, set the tooltip to the expanded version:
		wMovetoDirectory.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wMovetoDirectory.setToolTipText(jobMeta.environmentSubstitute( wMovetoDirectory.getText() ) );
				}
			}
		);

		wbMovetoDirectory.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog mdialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wMovetoDirectory.getText()!=null)
					{
						mdialog.setFilterPath(jobMeta.environmentSubstitute(wMovetoDirectory.getText()) );
					}
					
					 // Calling open() will open and run the dialog.
			        // It will return the selected directory, or
			        // null if user cancels
			        String dir = mdialog.open();
			        if (dir != null) {
			          // Set the text box to the new selection
			        	wMovetoDirectory.setText(dir);
			        }
					
				}
			}
		);

		
		
		
	 // fileresult grouping?
      // ////////////////////////
      // START OF LOGGING GROUP///
      // /
      wFileResult = new Group(shell, SWT.SHADOW_NONE);
      props.setLook(wFileResult);
      wFileResult.setText(Messages.getString("JobZipFiles.FileResult.Group.Label"));

      FormLayout groupLayout = new FormLayout();
      groupLayout.marginWidth = 10;
      groupLayout.marginHeight = 10;

      wFileResult.setLayout(groupLayout);
      
      
  	//Add file to result
		wlAddFileToResult = new Label(wFileResult, SWT.RIGHT);
		wlAddFileToResult.setText(Messages.getString("JobZipFiles.AddFileToResult.Label"));
		props.setLook(wlAddFileToResult);
		fdlAddFileToResult = new FormData();
		fdlAddFileToResult.left = new FormAttachment(0, 0);
		fdlAddFileToResult.top = new FormAttachment(wMovetoDirectory, margin);
		fdlAddFileToResult.right = new FormAttachment(middle, -margin);
		wlAddFileToResult.setLayoutData(fdlAddFileToResult);
		wAddFileToResult = new Button(wFileResult, SWT.CHECK);
		props.setLook(wAddFileToResult);
		wAddFileToResult.setToolTipText(Messages.getString("JobZipFiles.AddFileToResult.Tooltip"));
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

		wName.addSelectionListener( lsDef );
		wZipFilename.addSelectionListener( lsDef );

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();

		AfterZipActivate();

		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
	}

	public void AfterZipActivate()
	{

		jobEntry.setChanged();
		if (wAfterZip.getSelectionIndex()==2)
		{
			wlMovetoDirectory.setEnabled(true);
			wMovetoDirectory.setEnabled(true);
			wbMovetoDirectory.setEnabled(true);
		}
		else
		{
			wlMovetoDirectory.setEnabled(false);
			wMovetoDirectory.setEnabled(false);
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

		if (jobEntry.compressionrate>=0) 
		{
			wCompressionRate.select(jobEntry.compressionrate );
		}
		else
		{
			wCompressionRate.select(1); // DEFAULT
		}

		if (jobEntry.ifzipfileexists>=0) 
		{
			wIfFileExists.select(jobEntry.ifzipfileexists );
		}
		else
		{
			wIfFileExists.select(2); // NOTHING
		}

		if (jobEntry.getWildcard()!= null) wWildcard.setText( jobEntry.getWildcard() );
		if (jobEntry.getWildcardExclude()!= null) wWildcardExclude.setText( jobEntry.getWildcardExclude() );
		if (jobEntry.getSourceDirectory()!= null) wSourceDirectory.setText( jobEntry.getSourceDirectory() );
		if (jobEntry.getMoveToDirectory()!= null) wMovetoDirectory.setText( jobEntry.getMoveToDirectory() );

		if (jobEntry.afterzip>=0)
		{
			wAfterZip.select(jobEntry.afterzip );
		}

		else
		{
			wAfterZip.select(0 ); // NOTHING
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

		jobEntry.compressionrate = wCompressionRate.getSelectionIndex();
		jobEntry.ifzipfileexists = wIfFileExists.getSelectionIndex();

		jobEntry.setWildcard(wWildcard.getText());
		jobEntry.setWildcardExclude(wWildcardExclude.getText());
		jobEntry.setSourceDirectory(wSourceDirectory.getText());

		jobEntry.setMoveToDirectory(wMovetoDirectory.getText());
		
		
		jobEntry.afterzip = wAfterZip.getSelectionIndex();
		
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