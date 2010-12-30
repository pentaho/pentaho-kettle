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
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.zipfile.JobEntryZipFile;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


/**
 * This dialog allows you to edit the Zip File job entry settings.
 *
 * @author Samatar Hassan
 * @since  27-02-2007
 */
public class JobEntryZipFileDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryZipFile.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private static final String[] FILETYPES = new String[] {
			BaseMessages.getString(PKG, "JobZipFiles.Filetype.Zip"),
			BaseMessages.getString(PKG, "JobZipFiles.Filetype.All")};
	
	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;
    
    private Label wlCreateParentFolder;
    private FormData fdlCreateParentFolder, fdCreateParentFolder;
    private Button wCreateParentFolder;

	private Label        wlZipFilename;
	private Button       wbZipFilename;
	private TextVar      wZipFilename;
	private FormData     fdlZipFilename, fdbZipFilename, fdZipFilename;
	
 
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

    private Label wlIncludeSubfolders;
    private Button wIncludeSubfolders;
    private FormData fdlIncludeSubfolders, fdIncludeSubfolders;

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
	
	private Group wFileResult,wSourceFiles, wZipFile, wSettings;
    private FormData fdFileResult,fdSourceFiles,fdZipFile, fdSettings;
    
	//  Add File to result
	private Label        wlAddFileToResult;
	private Button       wAddFileToResult;
	private FormData     fdlAddFileToResult, fdAddFileToResult;
	
    private Button wbSourceDirectory,wbSourceFile;
    private FormData fdbSourceDirectory,fdbSourceFile;
    
    private Button wbMovetoDirectory;
    private FormData fdbMovetoDirectory;   
    
	//  Result from previous?
	private Label        wlgetFromPrevious;
	private Button       wgetFromPrevious;
	private FormData     fdlgetFromPrevious, fdgetFromPrevious;
	

	private Label        wlAddDate;
	private Button       wAddDate;
	private FormData     fdlAddDate, fdAddDate;

	private Label        wlAddTime;
	private Button       wAddTime;
	private FormData     fdlAddTime, fdAddTime;

	private Button       wbShowFiles;
	private FormData     fdbShowFiles;
	
	private Label        wlSpecifyFormat;
	private Button       wSpecifyFormat;
	private FormData     fdlSpecifyFormat, fdSpecifyFormat;

  	private Label        wlDateTimeFormat;
	private CCombo       wDateTimeFormat;
	private FormData     fdlDateTimeFormat, fdDateTimeFormat; 
	
	private CTabFolder   wTabFolder;
	private Composite    wAdvancedComp,wGeneralComp;	
	private CTabItem     wAdvancedTab,wGeneralTab;
	private FormData	 fdAdvancedComp,fdGeneralComp;
	private FormData     fdTabFolder;
	
	private Label        wlcreateMoveToDirectory;
	private Button       wcreateMoveToDirectory;
	private FormData     fdlcreateMoveToDirectory, fdcreateMoveToDirectory;
	
	private boolean changed;
    public JobEntryZipFileDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryZipFile) jobEntryInt;
        if (this.jobEntry.getName() == null) 
			this.jobEntry.setName(BaseMessages.getString(PKG, "JobZipFiles.Name.Default"));
 
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
		shell.setText(BaseMessages.getString(PKG, "JobZipFiles.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// ZipFilename line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(BaseMessages.getString(PKG, "JobZipFiles.Name.Label"));
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
		wGeneralTab.setText(BaseMessages.getString(PKG, "JobZipFiles.Tab.General.Label"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
		
		
		  // SourceFile grouping?
        // ////////////////////////
        // START OF SourceFile GROUP///
        // /
        wSourceFiles = new Group(wGeneralComp, SWT.SHADOW_NONE);
        props.setLook(wSourceFiles);
        wSourceFiles.setText(BaseMessages.getString(PKG, "JobZipFiles.SourceFiles.Group.Label"));

        FormLayout groupLayout = new FormLayout();
        groupLayout.marginWidth = 10;
        groupLayout.marginHeight = 10;

        wSourceFiles.setLayout(groupLayout);

      //Get Result from previous?
		wlgetFromPrevious = new Label(wSourceFiles, SWT.RIGHT);
		wlgetFromPrevious.setText(BaseMessages.getString(PKG, "JobZipFiles.getFromPrevious.Label"));
		props.setLook(wlgetFromPrevious);
		fdlgetFromPrevious = new FormData();
		fdlgetFromPrevious.left = new FormAttachment(0, 0);
		fdlgetFromPrevious.top = new FormAttachment(wName, margin);
		fdlgetFromPrevious.right = new FormAttachment(middle, -margin);
		wlgetFromPrevious.setLayoutData(fdlgetFromPrevious);
		wgetFromPrevious = new Button(wSourceFiles, SWT.CHECK);
		props.setLook(wgetFromPrevious);
		wgetFromPrevious.setToolTipText(BaseMessages.getString(PKG, "JobZipFiles.getFromPrevious.Tooltip"));
		fdgetFromPrevious = new FormData();
		fdgetFromPrevious.left = new FormAttachment(middle, 0);
		fdgetFromPrevious.top = new FormAttachment(wName, margin);
		fdgetFromPrevious.right = new FormAttachment(100, 0);
		wgetFromPrevious.setLayoutData(fdgetFromPrevious);
		wgetFromPrevious.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
				setGetFromPrevious();
			}
		});
		
		
		

		// TargetDirectory line
		wlSourceDirectory = new Label(wSourceFiles, SWT.RIGHT);
		wlSourceDirectory.setText(BaseMessages.getString(PKG, "JobZipFiles.SourceDir.Label"));
		props.setLook(wlSourceDirectory);
		fdlSourceDirectory = new FormData();
		fdlSourceDirectory.left = new FormAttachment(0, 0);
		fdlSourceDirectory.top = new FormAttachment(wgetFromPrevious, margin);
		fdlSourceDirectory.right = new FormAttachment(middle, -margin);
		wlSourceDirectory.setLayoutData(fdlSourceDirectory);
		
        
        // Browse folders button ...
		wbSourceDirectory=new Button(wSourceFiles, SWT.PUSH| SWT.CENTER);
		props.setLook(wbSourceDirectory);
		wbSourceDirectory.setText(BaseMessages.getString(PKG, "JobZipFiles.BrowseFolders.Label"));
		fdbSourceDirectory=new FormData();
		fdbSourceDirectory.right= new FormAttachment(100, 0);
		fdbSourceDirectory.top  = new FormAttachment(wgetFromPrevious, margin);
		wbSourceDirectory.setLayoutData(fdbSourceDirectory);
		
		// Browse Destination file browse button ...
		wbSourceFile=new Button(wSourceFiles, SWT.PUSH| SWT.CENTER);
		props.setLook(wbSourceFile);
		wbSourceFile.setText(BaseMessages.getString(PKG, "JobZipFiles.BrowseFiles.Label"));
		fdbSourceFile=new FormData();
		fdbSourceFile.right= new FormAttachment(wbSourceDirectory, -margin);
		fdbSourceFile.top  = new FormAttachment(wgetFromPrevious, margin);
		wbSourceFile.setLayoutData(fdbSourceFile);
				
		
		wSourceDirectory = new TextVar(jobMeta,wSourceFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER, 
				BaseMessages.getString(PKG, "JobZipFiles.SourceDir.Tooltip"));
		props.setLook(wSourceDirectory);
		wSourceDirectory.addModifyListener(lsMod);
		fdSourceDirectory = new FormData();
		fdSourceDirectory.left = new FormAttachment(middle, 0);
		fdSourceDirectory.top = new FormAttachment(wgetFromPrevious, margin);
		fdSourceDirectory.right = new FormAttachment(wbSourceFile, -margin);
		wSourceDirectory.setLayoutData(fdSourceDirectory);
		
		// Wildcard line
		wlWildcard = new Label(wSourceFiles, SWT.RIGHT);
		wlWildcard.setText(BaseMessages.getString(PKG, "JobZipFiles.Wildcard.Label"));
		props.setLook(wlWildcard);
		fdlWildcard = new FormData();
		fdlWildcard.left = new FormAttachment(0, 0);
		fdlWildcard.top = new FormAttachment(wSourceDirectory, margin);
		fdlWildcard.right = new FormAttachment(middle, -margin);
		wlWildcard.setLayoutData(fdlWildcard);
		wWildcard = new TextVar(jobMeta,wSourceFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER, 
				BaseMessages.getString(PKG, "JobZipFiles.Wildcard.Tooltip"));
		props.setLook(wWildcard);
		wWildcard.addModifyListener(lsMod);
		fdWildcard = new FormData();
		fdWildcard.left = new FormAttachment(middle, 0);
		fdWildcard.top = new FormAttachment(wSourceDirectory, margin);
		fdWildcard.right = new FormAttachment(100, 0);
		wWildcard.setLayoutData(fdWildcard);
		
		// Wildcard to exclude
		wlWildcardExclude = new Label(wSourceFiles, SWT.RIGHT);
		wlWildcardExclude.setText(BaseMessages.getString(PKG, "JobZipFiles.WildcardExclude.Label"));
		props.setLook(wlWildcardExclude);
		fdlWildcardExclude = new FormData();
		fdlWildcardExclude.left = new FormAttachment(0, 0);
		fdlWildcardExclude.top = new FormAttachment(wWildcard, margin);
		fdlWildcardExclude.right = new FormAttachment(middle, -margin);
		wlWildcardExclude.setLayoutData(fdlWildcardExclude);
		wWildcardExclude = new TextVar(jobMeta,wSourceFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER, 
				BaseMessages.getString(PKG, "JobZipFiles.WildcardExclude.Tooltip"));
		props.setLook(wWildcardExclude);
		wWildcardExclude.addModifyListener(lsMod);
		fdWildcardExclude = new FormData();
		fdWildcardExclude.left = new FormAttachment(middle, 0);
		fdWildcardExclude.top = new FormAttachment(wWildcard, margin);
		fdWildcardExclude.right = new FormAttachment(100, 0);
		wWildcardExclude.setLayoutData(fdWildcardExclude);

	    // Include sub-folders?
		//
        wlIncludeSubfolders = new Label(wSourceFiles, SWT.RIGHT);
        wlIncludeSubfolders.setText(BaseMessages.getString(PKG, "JobZipFiles.IncludeSubfolders.Label"));
        props.setLook(wlIncludeSubfolders);
        fdlIncludeSubfolders = new FormData();
        fdlIncludeSubfolders.left = new FormAttachment(0, 0);
        fdlIncludeSubfolders.top = new FormAttachment(wWildcardExclude, margin);
        fdlIncludeSubfolders.right = new FormAttachment(middle, -margin);
        wlIncludeSubfolders.setLayoutData(fdlIncludeSubfolders);
        wIncludeSubfolders = new Button(wSourceFiles, SWT.CHECK);
        props.setLook(wIncludeSubfolders);
        wIncludeSubfolders.setToolTipText(BaseMessages.getString(PKG, "JobZipFiles.IncludeSubfolders.Tooltip"));
        fdIncludeSubfolders = new FormData();
        fdIncludeSubfolders.left = new FormAttachment(middle, 0);
        fdIncludeSubfolders.top = new FormAttachment(wWildcardExclude, margin);
        fdIncludeSubfolders.right = new FormAttachment(100, 0);
        wIncludeSubfolders.setLayoutData(fdIncludeSubfolders);
        wIncludeSubfolders.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });

        fdSourceFiles = new FormData();
        fdSourceFiles.left = new FormAttachment(0, margin);
        fdSourceFiles.top = new FormAttachment(wName, margin);
        fdSourceFiles.right = new FormAttachment(100, -margin);
        wSourceFiles.setLayoutData(fdSourceFiles);
        // ///////////////////////////////////////////////////////////
        // / END OF SourceFile GROUP
        // ///////////////////////////////////////////////////////////

		  // ZipFile grouping?
        // ////////////////////////
        // START OF ZipFile GROUP///
        // /
        wZipFile = new Group(wGeneralComp, SWT.SHADOW_NONE);
        props.setLook(wZipFile);
        wZipFile.setText(BaseMessages.getString(PKG, "JobZipFiles.ZipFile.Group.Label"));

        FormLayout groupLayoutzipfile = new FormLayout();
        groupLayoutzipfile.marginWidth = 10;
        groupLayoutzipfile.marginHeight = 10;

        wZipFile.setLayout(groupLayoutzipfile);
        

		// ZipFilename line
		wlZipFilename=new Label(wZipFile, SWT.RIGHT);
		wlZipFilename.setText(BaseMessages.getString(PKG, "JobZipFiles.ZipFilename.Label"));
		props.setLook(wlZipFilename);
		fdlZipFilename=new FormData();
		fdlZipFilename.left = new FormAttachment(0, 0);
		fdlZipFilename.top  = new FormAttachment(wSourceFiles, margin);
		fdlZipFilename.right= new FormAttachment(middle, -margin);
		wlZipFilename.setLayoutData(fdlZipFilename);
		wbZipFilename=new Button(wZipFile, SWT.PUSH| SWT.CENTER);
		props.setLook(wbZipFilename);
		wbZipFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbZipFilename=new FormData();
		fdbZipFilename.right= new FormAttachment(100, 0);
		fdbZipFilename.top  = new FormAttachment(wSourceFiles, 0);
		wbZipFilename.setLayoutData(fdbZipFilename);
		wZipFilename=new TextVar(jobMeta,wZipFile, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wZipFilename);
		wZipFilename.addModifyListener(lsMod);
		fdZipFilename=new FormData();
		fdZipFilename.left = new FormAttachment(middle, 0);
		fdZipFilename.top  = new FormAttachment(wSourceFiles, margin);
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
					FileDialog dialog = new FileDialog(shell, SWT.SAVE);
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
		
		// Create Parent Folder
		wlCreateParentFolder=new Label(wZipFile, SWT.RIGHT);
		wlCreateParentFolder.setText(BaseMessages.getString(PKG, "JobZipFiles.CreateParentFolder.Label"));
 		props.setLook(wlCreateParentFolder);
		fdlCreateParentFolder=new FormData();
		fdlCreateParentFolder.left = new FormAttachment(0, 0);
		fdlCreateParentFolder.top  = new FormAttachment(wZipFilename, margin);
		fdlCreateParentFolder.right= new FormAttachment(middle, -margin);
		wlCreateParentFolder.setLayoutData(fdlCreateParentFolder);
		wCreateParentFolder=new Button(wZipFile, SWT.CHECK );
		wCreateParentFolder.setToolTipText(BaseMessages.getString(PKG, "JobZipFiles.CreateParentFolder.Tooltip"));
 		props.setLook(wCreateParentFolder);
		fdCreateParentFolder=new FormData();
		fdCreateParentFolder.left = new FormAttachment(middle, 0);
		fdCreateParentFolder.top  = new FormAttachment(wZipFilename, margin);
		fdCreateParentFolder.right= new FormAttachment(100, 0);
		wCreateParentFolder.setLayoutData(fdCreateParentFolder);
		wCreateParentFolder.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobEntry.setChanged();
				}
			}
		);

		// Create multi-part file?
		wlAddDate=new Label(wZipFile, SWT.RIGHT);
		wlAddDate.setText(BaseMessages.getString(PKG, "JobZipFiles.AddDate.Label"));
 		props.setLook(wlAddDate);
		fdlAddDate=new FormData();
		fdlAddDate.left = new FormAttachment(0, 0);
		fdlAddDate.top  = new FormAttachment(wCreateParentFolder, margin);
		fdlAddDate.right= new FormAttachment(middle, -margin);
		wlAddDate.setLayoutData(fdlAddDate);
		wAddDate=new Button(wZipFile, SWT.CHECK);
 		props.setLook(wAddDate);
 		wAddDate.setToolTipText(BaseMessages.getString(PKG, "JobZipFiles.AddDate.Tooltip"));
		fdAddDate=new FormData();
		fdAddDate.left = new FormAttachment(middle, 0);
		fdAddDate.top  = new FormAttachment(wCreateParentFolder, margin);
		fdAddDate.right= new FormAttachment(100, 0);
		wAddDate.setLayoutData(fdAddDate);
		wAddDate.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobEntry.setChanged();
				}
			}
		);
		// Create multi-part file?
		wlAddTime=new Label(wZipFile, SWT.RIGHT);
		wlAddTime.setText(BaseMessages.getString(PKG, "JobZipFiles.AddTime.Label"));
 		props.setLook(wlAddTime);
		fdlAddTime=new FormData();
		fdlAddTime.left = new FormAttachment(0, 0);
		fdlAddTime.top  = new FormAttachment(wAddDate, margin);
		fdlAddTime.right= new FormAttachment(middle, -margin);
		wlAddTime.setLayoutData(fdlAddTime);
		wAddTime=new Button(wZipFile, SWT.CHECK);
 		props.setLook(wAddTime);
 		wAddTime.setToolTipText(BaseMessages.getString(PKG, "JobZipFiles.AddTime.Tooltip"));
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
				}
			}
		);
		
		// Specify date time format?
		wlSpecifyFormat=new Label(wZipFile, SWT.RIGHT);
		wlSpecifyFormat.setText(BaseMessages.getString(PKG, "JobZipFiles.SpecifyFormat.Label"));
		props.setLook(wlSpecifyFormat);
		fdlSpecifyFormat=new FormData();
		fdlSpecifyFormat.left = new FormAttachment(0, 0);
		fdlSpecifyFormat.top  = new FormAttachment(wAddTime, margin);
		fdlSpecifyFormat.right= new FormAttachment(middle, -margin);
		wlSpecifyFormat.setLayoutData(fdlSpecifyFormat);
		wSpecifyFormat=new Button(wZipFile, SWT.CHECK);
		props.setLook(wSpecifyFormat);
		wSpecifyFormat.setToolTipText(BaseMessages.getString(PKG, "JobZipFiles.SpecifyFormat.Tooltip"));
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
				}
			}
		);

		
		//	Prepare a list of possible DateTimeFormats...
		String dats[] = Const.getDateFormats();
		
 		// DateTimeFormat
		wlDateTimeFormat=new Label(wZipFile, SWT.RIGHT);
        wlDateTimeFormat.setText(BaseMessages.getString(PKG, "JobZipFiles.DateTimeFormat.Label"));
        props.setLook(wlDateTimeFormat);
        fdlDateTimeFormat=new FormData();
        fdlDateTimeFormat.left = new FormAttachment(0, 0);
        fdlDateTimeFormat.top  = new FormAttachment(wSpecifyFormat, margin);
        fdlDateTimeFormat.right= new FormAttachment(middle, -margin);
        wlDateTimeFormat.setLayoutData(fdlDateTimeFormat);
        wDateTimeFormat=new CCombo(wZipFile, SWT.BORDER | SWT.READ_ONLY);
        wDateTimeFormat.setEditable(true);
        props.setLook(wDateTimeFormat);
        wDateTimeFormat.addModifyListener(lsMod);
        fdDateTimeFormat=new FormData();
        fdDateTimeFormat.left = new FormAttachment(middle, 0);
        fdDateTimeFormat.top  = new FormAttachment(wSpecifyFormat, margin);
        fdDateTimeFormat.right= new FormAttachment(100, 0);
        wDateTimeFormat.setLayoutData(fdDateTimeFormat);
        for (int x=0;x<dats.length;x++) wDateTimeFormat.add(dats[x]);
        

		wbShowFiles=new Button(wZipFile, SWT.PUSH| SWT.CENTER);
		props.setLook(wbShowFiles);
		wbShowFiles.setText(BaseMessages.getString(PKG, "JobZipFiles.ShowFile.Button"));
		fdbShowFiles=new FormData();
		fdbShowFiles.left = new FormAttachment(middle, 0);
		fdbShowFiles.top  = new FormAttachment(wDateTimeFormat, margin*2);
		wbShowFiles.setLayoutData(fdbShowFiles);
		wbShowFiles.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				if(!Const.isEmpty(wZipFilename.getText()))
				{
					JobEntryZipFile jobEntry = new JobEntryZipFile();
					String filename[] = new String[1];
					filename[0]=jobEntry.getFullFilename(wZipFilename.getText(),
							wAddDate.getSelection(),wAddTime.getSelection(), 
							wSpecifyFormat.getSelection(),wDateTimeFormat.getText());
					if (filename!=null && filename.length>0)
					{
						EnterSelectionDialog esd = new EnterSelectionDialog(shell, filename, BaseMessages.getString(PKG, "JobZipFiles.SelectOutputFiles.DialogTitle"), BaseMessages.getString(PKG, "JobZipFiles.SelectOutputFiles.DialogMessage"));
						esd.setViewOnly();
						esd.open();
					}
					else
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setMessage(BaseMessages.getString(PKG, "JobZipFiles.NoFilesFound.DialogMessage"));
						mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
						mb.open(); 
					}
				}
			}
		}
			);
		
		

        fdZipFile = new FormData();
        fdZipFile.left = new FormAttachment(0, margin);
        fdZipFile.top = new FormAttachment(wSourceFiles, margin);
        fdZipFile.right = new FormAttachment(100, -margin);
        wZipFile.setLayoutData(fdZipFile);
        
        /////////////////////////////////////////////////////////////
        // END OF ZipFile GROUP
        /////////////////////////////////////////////////////////////
        
		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(0, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(500, -margin);
		wGeneralComp.setLayoutData(fdGeneralComp);
		
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
 		props.setLook(wGeneralComp);
 		
 		
 		
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////
        
        
        
        //////////////////////////
		// START OF ADVANCED TAB   ///
		//////////////////////////
		
		wAdvancedTab=new CTabItem(wTabFolder, SWT.NONE);
		wAdvancedTab.setText(BaseMessages.getString(PKG, "JobZipFiles.Tab.Advanced.Label"));
		
		wAdvancedComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wAdvancedComp);

		FormLayout advancedLayout = new FormLayout();
		advancedLayout.marginWidth  = 3;
		advancedLayout.marginHeight = 3;
		wAdvancedComp.setLayout(advancedLayout);

        //////////////////////////////
        // START OF Settings GROUP
        //
        wSettings = new Group(wAdvancedComp, SWT.SHADOW_NONE);
        props.setLook(wSettings);
        wSettings.setText(BaseMessages.getString(PKG, "JobZipFiles.Advanced.Group.Label"));
        FormLayout groupLayoutSettings = new FormLayout();
        groupLayoutSettings.marginWidth = 10;
        groupLayoutSettings.marginHeight = 10;
        wSettings.setLayout(groupLayoutSettings);

        // Compression Rate
		wlCompressionRate = new Label(wSettings, SWT.RIGHT);
		wlCompressionRate.setText(BaseMessages.getString(PKG, "JobZipFiles.CompressionRate.Label"));
		props.setLook(wlCompressionRate);
		fdlCompressionRate = new FormData();
		fdlCompressionRate.left = new FormAttachment(0, -margin);
		fdlCompressionRate.right = new FormAttachment(middle, -margin);
		fdlCompressionRate.top = new FormAttachment(wZipFile, margin);
		wlCompressionRate.setLayoutData(fdlCompressionRate);
		wCompressionRate = new CCombo(wSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wCompressionRate.add(BaseMessages.getString(PKG, "JobZipFiles.NO_COMP_CompressionRate.Label"));
		wCompressionRate.add(BaseMessages.getString(PKG, "JobZipFiles.DEF_COMP_CompressionRate.Label"));
		wCompressionRate.add(BaseMessages.getString(PKG, "JobZipFiles.BEST_COMP_CompressionRate.Label"));
		wCompressionRate.add(BaseMessages.getString(PKG, "JobZipFiles.BEST_SPEED_CompressionRate.Label"));
		wCompressionRate.select(1); // +1: starts at -1

		props.setLook(wCompressionRate);
		fdCompressionRate= new FormData();
		fdCompressionRate.left = new FormAttachment(middle, 0);
		fdCompressionRate.top = new FormAttachment(wZipFile, margin);
		fdCompressionRate.right = new FormAttachment(100, 0);
		wCompressionRate.setLayoutData(fdCompressionRate);
	
		// If File Exists
		wlIfFileExists = new Label(wSettings, SWT.RIGHT);
		wlIfFileExists.setText(BaseMessages.getString(PKG, "JobZipFiles.IfZipFileExists.Label"));
		props.setLook(wlIfFileExists);
		fdlIfFileExists = new FormData();
		fdlIfFileExists.left = new FormAttachment(0, -margin);
		fdlIfFileExists.right = new FormAttachment(middle, -margin);
		fdlIfFileExists.top = new FormAttachment(wCompressionRate, margin);
		wlIfFileExists.setLayoutData(fdlIfFileExists);
		wIfFileExists = new CCombo(wSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wIfFileExists.add(BaseMessages.getString(PKG, "JobZipFiles.Create_NewFile_IfFileExists.Label"));
		wIfFileExists.add(BaseMessages.getString(PKG, "JobZipFiles.Append_File_IfFileExists.Label"));
		wIfFileExists.add(BaseMessages.getString(PKG, "JobZipFiles.Do_Nothing_IfFileExists.Label"));

		wIfFileExists.add(BaseMessages.getString(PKG, "JobZipFiles.Fail_IfFileExists.Label"));
		wIfFileExists.select(3); // +1: starts at -1

		props.setLook(wIfFileExists);
		fdIfFileExists= new FormData();
		fdIfFileExists.left = new FormAttachment(middle, 0);
		fdIfFileExists.top = new FormAttachment(wCompressionRate, margin);
		fdIfFileExists.right = new FormAttachment(100, 0);
		wIfFileExists.setLayoutData(fdIfFileExists);

		// After Zipping
		wlAfterZip = new Label(wSettings, SWT.RIGHT);
		wlAfterZip.setText(BaseMessages.getString(PKG, "JobZipFiles.AfterZip.Label"));
		props.setLook(wlAfterZip);
		fdlAfterZip = new FormData();
		fdlAfterZip.left = new FormAttachment(0, -margin);
		fdlAfterZip.right = new FormAttachment(middle, -margin);
		fdlAfterZip.top = new FormAttachment(wIfFileExists, margin);
		wlAfterZip.setLayoutData(fdlAfterZip);
		wAfterZip = new CCombo(wSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wAfterZip.add(BaseMessages.getString(PKG, "JobZipFiles.Do_Nothing_AfterZip.Label"));
		wAfterZip.add(BaseMessages.getString(PKG, "JobZipFiles.Delete_Files_AfterZip.Label"));
		wAfterZip.add(BaseMessages.getString(PKG, "JobZipFiles.Move_Files_AfterZip.Label"));

		wAfterZip.select(0); // +1: starts at -1

		props.setLook(wAfterZip);
		fdAfterZip= new FormData();
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
		wlMovetoDirectory = new Label(wSettings, SWT.RIGHT);
		wlMovetoDirectory.setText(BaseMessages.getString(PKG, "JobZipFiles.MovetoDirectory.Label"));
		props.setLook(wlMovetoDirectory);
		fdlMovetoDirectory = new FormData();
		fdlMovetoDirectory.left = new FormAttachment(0, 0);
		fdlMovetoDirectory.top = new FormAttachment(wAfterZip, margin);
		fdlMovetoDirectory.right = new FormAttachment(middle, -margin);
		wlMovetoDirectory.setLayoutData(fdlMovetoDirectory);
		
        // Browse folders button ...
		wbMovetoDirectory=new Button(wSettings, SWT.PUSH| SWT.CENTER);
		props.setLook(wbMovetoDirectory);
		wbMovetoDirectory.setText(BaseMessages.getString(PKG, "JobZipFiles.BrowseFolders.Label"));
		fdbMovetoDirectory=new FormData();
		fdbMovetoDirectory.right= new FormAttachment(100, 0);
		fdbMovetoDirectory.top  = new FormAttachment(wAfterZip, margin);
		wbMovetoDirectory.setLayoutData(fdbMovetoDirectory);
		
		wMovetoDirectory = new TextVar(jobMeta,wSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER, 
				BaseMessages.getString(PKG, "JobZipFiles.MovetoDirectory.Tooltip"));
		props.setLook(wMovetoDirectory);
		wMovetoDirectory.addModifyListener(lsMod);
		fdMovetoDirectory = new FormData();
		fdMovetoDirectory.left = new FormAttachment(middle, 0);
		fdMovetoDirectory.top = new FormAttachment(wAfterZip, margin);
		fdMovetoDirectory.right = new FormAttachment(wbMovetoDirectory, -margin);
		wMovetoDirectory.setLayoutData(fdMovetoDirectory);
		
		//create moveto folder
		wlcreateMoveToDirectory = new Label(wSettings, SWT.RIGHT);
		wlcreateMoveToDirectory.setText(BaseMessages.getString(PKG, "JobZipFiles.createMoveToDirectory.Label"));
		props.setLook(wlcreateMoveToDirectory);
		fdlcreateMoveToDirectory = new FormData();
		fdlcreateMoveToDirectory.left = new FormAttachment(0, 0);
		fdlcreateMoveToDirectory.top = new FormAttachment(wMovetoDirectory, margin);
		fdlcreateMoveToDirectory.right = new FormAttachment(middle, -margin);
		wlcreateMoveToDirectory.setLayoutData(fdlcreateMoveToDirectory);
		wcreateMoveToDirectory = new Button(wSettings, SWT.CHECK);
		props.setLook(wcreateMoveToDirectory);
		wcreateMoveToDirectory.setToolTipText(BaseMessages.getString(PKG, "JobZipFiles.createMoveToDirectory.Tooltip"));
		fdcreateMoveToDirectory = new FormData();
		fdcreateMoveToDirectory.left = new FormAttachment(middle, 0);
		fdcreateMoveToDirectory.top = new FormAttachment(wMovetoDirectory, margin);
		fdcreateMoveToDirectory.right = new FormAttachment(100, 0);
		wcreateMoveToDirectory.setLayoutData(fdcreateMoveToDirectory);
		wcreateMoveToDirectory.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
	    
		
        fdSettings = new FormData();
        fdSettings.left = new FormAttachment(0, margin);
        fdSettings.top = new FormAttachment(wZipFile, margin);
        fdSettings.right = new FormAttachment(100, -margin);
        wSettings.setLayoutData(fdSettings);
        // ///////////////////////////////////////////////////////////
        // / END OF Settings GROUP
        // ///////////////////////////////////////////////////////////


		
		
		  // fileresult grouping?
        // ////////////////////////
        // START OF LOGGING GROUP///
        // /
        wFileResult = new Group(wAdvancedComp, SWT.SHADOW_NONE);
        props.setLook(wFileResult);
        wFileResult.setText(BaseMessages.getString(PKG, "JobZipFiles.FileResult.Group.Label"));

        FormLayout groupLayoutresult = new FormLayout();
        groupLayoutresult.marginWidth = 10;
        groupLayoutresult.marginHeight = 10;

        wFileResult.setLayout(groupLayoutresult);
        
        
    	//Add file to result
		wlAddFileToResult = new Label(wFileResult, SWT.RIGHT);
		wlAddFileToResult.setText(BaseMessages.getString(PKG, "JobZipFiles.AddFileToResult.Label"));
		props.setLook(wlAddFileToResult);
		fdlAddFileToResult = new FormData();
		fdlAddFileToResult.left = new FormAttachment(0, 0);
		fdlAddFileToResult.top = new FormAttachment(wSettings, margin);
		fdlAddFileToResult.right = new FormAttachment(middle, -margin);
		wlAddFileToResult.setLayoutData(fdlAddFileToResult);
		wAddFileToResult = new Button(wFileResult, SWT.CHECK);
		props.setLook(wAddFileToResult);
		wAddFileToResult.setToolTipText(BaseMessages.getString(PKG, "JobZipFiles.AddFileToResult.Tooltip"));
		fdAddFileToResult = new FormData();
		fdAddFileToResult.left = new FormAttachment(middle, 0);
		fdAddFileToResult.top = new FormAttachment(wSettings, margin);
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
        fdFileResult.top = new FormAttachment(wSettings, margin);
        fdFileResult.right = new FormAttachment(100, -margin);
        wFileResult.setLayoutData(fdFileResult);
        // ///////////////////////////////////////////////////////////
        // / END OF FILE RESULT GROUP
        // ///////////////////////////////////////////////////////////

		fdAdvancedComp=new FormData();
		fdAdvancedComp.left  = new FormAttachment(0, 0);
		fdAdvancedComp.top   = new FormAttachment(0, 0);
		fdAdvancedComp.right = new FormAttachment(100, 0);
		fdAdvancedComp.bottom= new FormAttachment(500, -margin);
		wAdvancedComp.setLayoutData(fdAdvancedComp);
		
		wAdvancedComp.layout();
		wAdvancedTab.setControl(wAdvancedComp);
 		props.setLook(wAdvancedComp);
 		 		
		/////////////////////////////////////////////////////////////
		/// END OF Advanced TAB
		/////////////////////////////////////////////////////////////

		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wName, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

        
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
        
        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wTabFolder);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		
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
			
		  
			wbSourceFile.addSelectionListener(new SelectionAdapter()
			{
			public void widgetSelected(SelectionEvent e)
			{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] {"*"});
					if (wSourceDirectory.getText()!=null)
					{
						dialog.setFileName(jobMeta.environmentSubstitute(wSourceDirectory.getText()) );
					}
					dialog.setFilterNames(FILETYPES);
					if (dialog.open()!=null)
					{
						wSourceDirectory.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
					}
				}
			}
			);
			
		wName.addSelectionListener( lsDef );
		wZipFilename.addSelectionListener( lsDef );

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();
		setGetFromPrevious();
		AfterZipActivate();
		setDateTimeFormat();

        wTabFolder.setSelection(0);
		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
	}
	public void setGetFromPrevious()
	{
		wlSourceDirectory.setEnabled(!wgetFromPrevious.getSelection());
		wSourceDirectory.setEnabled(!wgetFromPrevious.getSelection());
		wWildcard.setEnabled(!wgetFromPrevious.getSelection());
		wlWildcard.setEnabled(!wgetFromPrevious.getSelection());
		wWildcardExclude.setEnabled(!wgetFromPrevious.getSelection());
		wlWildcardExclude.setEnabled(!wgetFromPrevious.getSelection());
		wbSourceDirectory.setEnabled(!wgetFromPrevious.getSelection());
		wlZipFilename.setEnabled(!wgetFromPrevious.getSelection());
		wZipFilename.setEnabled(!wgetFromPrevious.getSelection());
		wbZipFilename.setEnabled(!wgetFromPrevious.getSelection());
		wbSourceFile.setEnabled(!wgetFromPrevious.getSelection());
		wlAddDate.setEnabled(!wgetFromPrevious.getSelection());
		wAddDate.setEnabled(!wgetFromPrevious.getSelection());
		wlAddTime.setEnabled(!wgetFromPrevious.getSelection());
		wAddTime.setEnabled(!wgetFromPrevious.getSelection());
		wbShowFiles.setEnabled(!wgetFromPrevious.getSelection());
		
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
	public void AfterZipActivate()
	{

		jobEntry.setChanged();
		if (wAfterZip.getSelectionIndex()==2)
		{
			wMovetoDirectory.setEnabled(true);
			wlMovetoDirectory.setEnabled(true);
			wbMovetoDirectory.setEnabled(true);
			wlcreateMoveToDirectory.setEnabled(true);
			wcreateMoveToDirectory.setEnabled(true);
		}
		else
		{
			wMovetoDirectory.setEnabled(false);
			wlMovetoDirectory.setEnabled(false);
			wbMovetoDirectory.setEnabled(false);
			wlcreateMoveToDirectory.setEnabled(false);
			wcreateMoveToDirectory.setEnabled(true);
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

		wWildcard.setText( Const.NVL(jobEntry.getWildcard(), "") );
		wWildcardExclude.setText( Const.NVL(jobEntry.getWildcardExclude(), "") );
		wSourceDirectory.setText( Const.NVL(jobEntry.getSourceDirectory(), "") );
		wMovetoDirectory.setText( Const.NVL(jobEntry.getMoveToDirectory(), "") );
		if (jobEntry.afterzip>=0)
		{
			wAfterZip.select(jobEntry.afterzip );
		}
		else
		{
			wAfterZip.select(0 ); // NOTHING
		}
		
		wAddFileToResult.setSelection(jobEntry.isAddFileToResult());
		wgetFromPrevious.setSelection(jobEntry.getDatafromprevious());
		wCreateParentFolder.setSelection(jobEntry.getcreateparentfolder());
		wAddDate.setSelection(jobEntry.isDateInFilename());
		wAddTime.setSelection(jobEntry.isTimeInFilename());
		
		wDateTimeFormat.setText( Const.NVL(jobEntry.getDateTimeFormat(), "") );
		wSpecifyFormat.setSelection(jobEntry.isSpecifyFormat());
		wcreateMoveToDirectory.setSelection(jobEntry.isCreateMoveToDirectory());
		wIncludeSubfolders.setSelection(jobEntry.isIncludingSubFolders());
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
		jobEntry.setZipFilename(wZipFilename.getText());

		jobEntry.compressionrate = wCompressionRate.getSelectionIndex();
		jobEntry.ifzipfileexists = wIfFileExists.getSelectionIndex();

		jobEntry.setWildcard(wWildcard.getText());
		jobEntry.setWildcardExclude(wWildcardExclude.getText());
		jobEntry.setSourceDirectory(wSourceDirectory.getText());

		jobEntry.setMoveToDirectory(wMovetoDirectory.getText());
		
		
		jobEntry.afterzip = wAfterZip.getSelectionIndex();
		
		jobEntry.setAddFileToResult(wAddFileToResult.getSelection());
		jobEntry.setDatafromprevious(wgetFromPrevious.getSelection());
		jobEntry.setcreateparentfolder(wCreateParentFolder.getSelection());
		jobEntry.setDateInFilename( wAddDate.getSelection() );
		jobEntry.setTimeInFilename( wAddTime.getSelection() );
		jobEntry.setSpecifyFormat(wSpecifyFormat.getSelection());
		jobEntry.setDateTimeFormat(wDateTimeFormat.getText());
		jobEntry.setCreateMoveToDirectory(wcreateMoveToDirectory.getSelection());
		jobEntry.setIncludingSubFolders(wIncludeSubfolders.getSelection());
		
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