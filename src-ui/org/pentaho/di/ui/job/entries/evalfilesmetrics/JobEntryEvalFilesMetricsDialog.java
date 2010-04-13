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
 * Created on 19-02-2010
 *
 */

package org.pentaho.di.ui.job.entries.evalfilesmetrics;

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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.evalfilesmetrics.JobEntryEvalFilesMetrics;
import org.pentaho.di.job.entries.simpleeval.JobEntrySimpleEval;
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
 * This dialog allows you to edit the eval files metrics job entry settings.
 *
 * @author Samatar Hassan
 * @since  26-02-2010
 */

public class JobEntryEvalFilesMetricsDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryEvalFilesMetrics.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static final String[] FILETYPES = new String[] {BaseMessages.getString(PKG, "JobEvalFilesMetrics.Filetype.All")};
	
	private Label        wlName;
	private Text         wName;
	private FormData     fdlName, fdName;

	private Label        wlSourceFileFolder;
	private Button       wbSourceFileFolder,
						 wbSourceDirectory;
	
	private TextVar      wSourceFileFolder;
	private FormData     fdlSourceFileFolder, fdbSourceFileFolder, 
						 fdSourceFileFolder,fdbSourceDirectory;

	private Button       wOK, wCancel;
	private Listener     lsOK, lsCancel;


	private JobEntryEvalFilesMetrics jobEntry;
	private Shell         	    shell;

	private SelectionAdapter lsDef;

	private boolean changed;

	private Label wlFields;

	private TableView wFields;

	private FormData fdlFields, fdFields;

	private Group wSettings;
	private FormData fdSettings;

	
	private Label wlWildcard;
	private TextVar wWildcard;
	private FormData fdlWildcard, fdWildcard;
	
	private Label wlResultFilenamesWildcard;
	private TextVar wResultFilenamesWildcard;
	private FormData fdlResultFilenamesWildcard, fdResultFilenamesWildcard;
	
	private Label wlResultFieldFile;
	private TextVar wResultFieldFile;
	private FormData fdlResultFieldFile, fdResultFieldFile;
	
	private Label wlResultFieldWildcard;
	private TextVar wResultFieldWildcard;
	private FormData fdlResultFieldWildcard, fdResultFieldWildcard;
	
	private Label wlResultFieldIncludeSubFolders;
	private TextVar wResultFieldIncludeSubFolders;
	private FormData fdlResultFieldIncludeSubFolders, fdResultFieldIncludeSubFolders;

	private Button       wbdSourceFileFolder; // Delete
	private Button       wbeSourceFileFolder; // Edit
	private Button       wbaSourceFileFolder; // Add or change
	
	
	private CTabFolder   wTabFolder;
	private Composite    wGeneralComp,wAdvancedComp;	
	private CTabItem     wGeneralTab,wAdvancedTab;
	private FormData	 fdGeneralComp,fdAdvancedComp;
	private FormData     fdTabFolder;
    
    
	private Group wSuccessOn;
    private FormData fdSuccessOn;
    
	private Label wlSuccessNumberCondition;
	private CCombo wSuccessNumberCondition;
	private FormData fdlSuccessNumberCondition, fdSuccessNumberCondition;
	
	private Label wlScale;
	private CCombo wScale;
	private FormData fdlScale, fdScale;
	
	private Label wlSourceFiles;
	private CCombo wSourceFiles;
	private FormData fdlSourceFiles, fdSourceFiles;
	
	private Label wlEvaluationType;
	private CCombo wEvaluationType;
	private FormData fdlEvaluationType, fdEvaluationType;
	
	
	private Label wlCompareValue;
	private TextVar wCompareValue;
	private FormData fdlCompareValue, fdCompareValue;
	
	private Label wlMinValue;
	private TextVar wMinValue;
	private FormData fdlMinValue, fdMinValue;
	
	
	private Label wlMaxValue;
	private TextVar wMaxValue;
	private FormData fdlMaxValue, fdMaxValue;
	 
	

	private FormData fdbeSourceFileFolder, fdbaSourceFileFolder, fdbdSourceFileFolder;

	public JobEntryEvalFilesMetricsDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
	 {
	     super(parent, jobEntryInt, rep, jobMeta);
	      jobEntry = (JobEntryEvalFilesMetrics) jobEntryInt;
	      if (this.jobEntry.getName() == null)
	          this.jobEntry.setName(BaseMessages.getString(PKG, "JobEvalFilesMetrics.Name.Default"));
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
		shell.setText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.Name.Label"));
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
		wGeneralTab.setText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.Tab.General.Label"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
		

		// SETTINGS grouping?
		// ////////////////////////
		// START OF SETTINGS GROUP
		// 

		wSettings = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wSettings);
		wSettings.setText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.Settings.Label"));

		FormLayout groupLayout = new FormLayout();
		groupLayout.marginWidth = 10;
		groupLayout.marginHeight = 10;
		wSettings.setLayout(groupLayout);
		
	    //SourceFiles
	  	wlSourceFiles = new Label(wSettings, SWT.RIGHT);
	  	wlSourceFiles.setText(BaseMessages.getString(PKG, "JobEvalFilesMetricsDialog.SourceFiles.Label"));
	  	props.setLook(wlSourceFiles);
	  	fdlSourceFiles = new FormData();
	  	fdlSourceFiles.left = new FormAttachment(0, 0);
	  	fdlSourceFiles.right = new FormAttachment(middle, -margin);
	  	fdlSourceFiles.top = new FormAttachment(wName, margin);
	  	wlSourceFiles.setLayoutData(fdlSourceFiles);
	  	
	  	wSourceFiles = new CCombo(wSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wSourceFiles.setItems(JobEntryEvalFilesMetrics.SourceFilesDesc);
	  	wSourceFiles.select(0); // +1: starts at -1
	  	
		props.setLook(wSourceFiles);
		fdSourceFiles= new FormData();
		fdSourceFiles.left = new FormAttachment(middle, 0);
		fdSourceFiles.top = new FormAttachment(wName, margin);
		fdSourceFiles.right = new FormAttachment(100, 0);
		wSourceFiles.setLayoutData(fdSourceFiles);
		wSourceFiles.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
				RefreshSourceFiles();
			}
		});
		
		
		// ResultFilenamesWildcard 
		wlResultFilenamesWildcard = new Label(wSettings, SWT.RIGHT);
		wlResultFilenamesWildcard .setText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.ResultFilenamesWildcard.Label"));
		props.setLook(wlResultFilenamesWildcard );
		fdlResultFilenamesWildcard = new FormData();
		fdlResultFilenamesWildcard .left = new FormAttachment(0, 0);
		fdlResultFilenamesWildcard .top = new FormAttachment(wSourceFiles, margin);
		fdlResultFilenamesWildcard .right = new FormAttachment(middle, -margin);
		wlResultFilenamesWildcard .setLayoutData(fdlResultFilenamesWildcard );
		
		wResultFilenamesWildcard = new TextVar(jobMeta, wSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wResultFilenamesWildcard .setToolTipText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.ResultFilenamesWildcard.Tooltip"));
		props.setLook(wResultFilenamesWildcard );
		wResultFilenamesWildcard .addModifyListener(lsMod);
		fdResultFilenamesWildcard = new FormData();
		fdResultFilenamesWildcard .left = new FormAttachment(middle, 0);
		fdResultFilenamesWildcard .top = new FormAttachment(wSourceFiles, margin);
		fdResultFilenamesWildcard .right= new FormAttachment(100, -margin);
		wResultFilenamesWildcard .setLayoutData(fdResultFilenamesWildcard );
		
		// ResultFieldFile 
		wlResultFieldFile = new Label(wSettings, SWT.RIGHT);
		wlResultFieldFile .setText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.ResultFieldFile.Label"));
		props.setLook(wlResultFieldFile );
		fdlResultFieldFile = new FormData();
		fdlResultFieldFile .left = new FormAttachment(0, 0);
		fdlResultFieldFile .top = new FormAttachment(wResultFilenamesWildcard, margin);
		fdlResultFieldFile .right = new FormAttachment(middle, -margin);
		wlResultFieldFile .setLayoutData(fdlResultFieldFile );
		
		wResultFieldFile = new TextVar(jobMeta, wSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wResultFieldFile .setToolTipText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.ResultFieldFile.Tooltip"));
		props.setLook(wResultFieldFile );
		wResultFieldFile .addModifyListener(lsMod);
		fdResultFieldFile = new FormData();
		fdResultFieldFile .left = new FormAttachment(middle, 0);
		fdResultFieldFile .top = new FormAttachment(wResultFilenamesWildcard, margin);
		fdResultFieldFile .right= new FormAttachment(100, -margin);
		wResultFieldFile .setLayoutData(fdResultFieldFile );
		
		// ResultFieldWildcard 
		wlResultFieldWildcard = new Label(wSettings, SWT.RIGHT);
		wlResultFieldWildcard .setText(BaseMessages.getString(PKG, "JobEvalWildcardsMetrics.ResultFieldWildcard.Label"));
		props.setLook(wlResultFieldWildcard );
		fdlResultFieldWildcard = new FormData();
		fdlResultFieldWildcard .left = new FormAttachment(0, 0);
		fdlResultFieldWildcard .top = new FormAttachment(wResultFieldFile, margin);
		fdlResultFieldWildcard .right = new FormAttachment(middle, -margin);
		wlResultFieldWildcard .setLayoutData(fdlResultFieldWildcard );
		
		wResultFieldWildcard = new TextVar(jobMeta, wSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wResultFieldWildcard .setToolTipText(BaseMessages.getString(PKG, "JobEvalWildcardsMetrics.ResultFieldWildcard.Tooltip"));
		props.setLook(wResultFieldWildcard );
		wResultFieldWildcard .addModifyListener(lsMod);
		fdResultFieldWildcard = new FormData();
		fdResultFieldWildcard .left = new FormAttachment(middle, 0);
		fdResultFieldWildcard .top = new FormAttachment(wResultFieldFile, margin);
		fdResultFieldWildcard .right= new FormAttachment(100, -margin);
		wResultFieldWildcard .setLayoutData(fdResultFieldWildcard );
		
		// ResultFieldIncludeSubFolders 
		wlResultFieldIncludeSubFolders = new Label(wSettings, SWT.RIGHT);
		wlResultFieldIncludeSubFolders .setText(BaseMessages.getString(PKG, "JobEvalIncludeSubFolderssMetrics.ResultFieldIncludeSubFolders.Label"));
		props.setLook(wlResultFieldIncludeSubFolders );
		fdlResultFieldIncludeSubFolders = new FormData();
		fdlResultFieldIncludeSubFolders .left = new FormAttachment(0, 0);
		fdlResultFieldIncludeSubFolders .top = new FormAttachment(wResultFieldWildcard, margin);
		fdlResultFieldIncludeSubFolders .right = new FormAttachment(middle, -margin);
		wlResultFieldIncludeSubFolders .setLayoutData(fdlResultFieldIncludeSubFolders );
		
		wResultFieldIncludeSubFolders = new TextVar(jobMeta, wSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wResultFieldIncludeSubFolders .setToolTipText(BaseMessages.getString(PKG, "JobEvalIncludeSubFolderssMetrics.ResultFieldIncludeSubFolders.Tooltip"));
		props.setLook(wResultFieldIncludeSubFolders );
		wResultFieldIncludeSubFolders .addModifyListener(lsMod);
		fdResultFieldIncludeSubFolders = new FormData();
		fdResultFieldIncludeSubFolders .left = new FormAttachment(middle, 0);
		fdResultFieldIncludeSubFolders .top = new FormAttachment(wResultFieldWildcard, margin);
		fdResultFieldIncludeSubFolders .right= new FormAttachment(100, -margin);
		wResultFieldIncludeSubFolders .setLayoutData(fdResultFieldIncludeSubFolders );
		
		
		
	    //EvaluationType
	  	wlEvaluationType = new Label(wSettings, SWT.RIGHT);
	  	wlEvaluationType.setText(BaseMessages.getString(PKG, "JobEvalFilesMetricsDialog.EvaluationType.Label"));
	  	props.setLook(wlEvaluationType);
	  	fdlEvaluationType = new FormData();
	  	fdlEvaluationType.left = new FormAttachment(0, 0);
	  	fdlEvaluationType.right = new FormAttachment(middle, -margin);
	  	fdlEvaluationType.top = new FormAttachment(wResultFieldIncludeSubFolders , margin);
	  	wlEvaluationType.setLayoutData(fdlEvaluationType);
	  	
	  	wEvaluationType = new CCombo(wSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wEvaluationType.setItems(JobEntryEvalFilesMetrics.EvaluationTypeDesc);
	  	wEvaluationType.select(0); // +1: starts at -1
	  	
		props.setLook(wEvaluationType);
		fdEvaluationType= new FormData();
		fdEvaluationType.left = new FormAttachment(middle, 0);
		fdEvaluationType.top = new FormAttachment(wResultFieldIncludeSubFolders , margin);
		fdEvaluationType.right = new FormAttachment(100, 0);
		wEvaluationType.setLayoutData(fdEvaluationType);
		wEvaluationType.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				RefreshSize();
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

		// SourceFileFolder line
		wlSourceFileFolder=new Label(wGeneralComp, SWT.RIGHT);
		wlSourceFileFolder.setText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.SourceFileFolder.Label"));
		props.setLook(wlSourceFileFolder);
		fdlSourceFileFolder=new FormData();
		fdlSourceFileFolder.left = new FormAttachment(0, 0);
		fdlSourceFileFolder.top  = new FormAttachment(wSettings, 2*margin);
		fdlSourceFileFolder.right= new FormAttachment(middle, -margin);
		wlSourceFileFolder.setLayoutData(fdlSourceFileFolder);

		// Browse Source folders button ...
		wbSourceDirectory=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
		props.setLook(wbSourceDirectory);
		wbSourceDirectory.setText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.BrowseFolders.Label"));
		fdbSourceDirectory=new FormData();
		fdbSourceDirectory.right= new FormAttachment(100, 0);
		fdbSourceDirectory.top  = new FormAttachment(wSettings, margin);
		wbSourceDirectory.setLayoutData(fdbSourceDirectory);
		
		wbSourceDirectory.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wSourceFileFolder.getText()!=null)
					{
						ddialog.setFilterPath(jobMeta.environmentSubstitute(wSourceFileFolder.getText()) );
					}
					
					 // Calling open() will open and run the dialog.
			        // It will return the selected directory, or
			        // null if user cancels
			        String dir = ddialog.open();
			        if (dir != null) {
			          // Set the text box to the new selection
			        	wSourceFileFolder.setText(dir);
			        }
					
				}
			}
		);
		
		// Browse Source files button ...
		wbSourceFileFolder=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
		props.setLook(wbSourceFileFolder);
		wbSourceFileFolder.setText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.BrowseFiles.Label"));
		fdbSourceFileFolder=new FormData();
		fdbSourceFileFolder.right= new FormAttachment(wbSourceDirectory, -margin);
		fdbSourceFileFolder.top  = new FormAttachment(wSettings, margin);
		wbSourceFileFolder.setLayoutData(fdbSourceFileFolder);
		
		// Browse Destination file add button ...
		wbaSourceFileFolder=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
		props.setLook(wbaSourceFileFolder);
		wbaSourceFileFolder.setText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.FilenameAdd.Button"));
		fdbaSourceFileFolder=new FormData();
		fdbaSourceFileFolder.right= new FormAttachment(wbSourceFileFolder, -margin);
		fdbaSourceFileFolder.top  = new FormAttachment(wSettings, margin);
		wbaSourceFileFolder.setLayoutData(fdbaSourceFileFolder);

		wSourceFileFolder=new TextVar(jobMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wSourceFileFolder.setToolTipText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.SourceFileFolder.Tooltip"));
		
		props.setLook(wSourceFileFolder);
		wSourceFileFolder.addModifyListener(lsMod);
		fdSourceFileFolder=new FormData();
		fdSourceFileFolder.left = new FormAttachment(middle, 0);
		fdSourceFileFolder.top  = new FormAttachment(wSettings, 2*margin);
		fdSourceFileFolder.right= new FormAttachment(wbSourceFileFolder, -55);
		wSourceFileFolder.setLayoutData(fdSourceFileFolder);

		// Whenever something changes, set the tooltip to the expanded version:
		wSourceFileFolder.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				wSourceFileFolder.setToolTipText(jobMeta.environmentSubstitute(wSourceFileFolder.getText() ) );
			}
		}
			);

		wbSourceFileFolder.addSelectionListener
			(
			new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterExtensions(new String[] {"*"});
				if (wSourceFileFolder.getText()!=null)
				{
					dialog.setFileName(jobMeta.environmentSubstitute(wSourceFileFolder.getText()) );
				}
				dialog.setFilterNames(FILETYPES);
				if (dialog.open()!=null)
				{
					wSourceFileFolder.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
				}
			}
		}
			);
		
		// Buttons to the right of the screen...
		wbdSourceFileFolder=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
		props.setLook(wbdSourceFileFolder);
		wbdSourceFileFolder.setText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.FilenameDelete.Button"));
		wbdSourceFileFolder.setToolTipText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.FilenameDelete.Tooltip"));
		fdbdSourceFileFolder=new FormData();
		fdbdSourceFileFolder.right = new FormAttachment(100, 0);
		fdbdSourceFileFolder.top  = new FormAttachment (wSourceFileFolder, 40);
		wbdSourceFileFolder.setLayoutData(fdbdSourceFileFolder);

		wbeSourceFileFolder=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
		props.setLook(wbeSourceFileFolder);
		wbeSourceFileFolder.setText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.FilenameEdit.Button"));
		fdbeSourceFileFolder=new FormData();
		fdbeSourceFileFolder.right = new FormAttachment(100, 0);
		fdbeSourceFileFolder.left = new FormAttachment(wbdSourceFileFolder, 0, SWT.LEFT);
		fdbeSourceFileFolder.top  = new FormAttachment (wbdSourceFileFolder, margin);
		wbeSourceFileFolder.setLayoutData(fdbeSourceFileFolder);
		
		
		
		// Wildcard
		wlWildcard = new Label(wGeneralComp, SWT.RIGHT);
		wlWildcard.setText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.Wildcard.Label"));
		props.setLook(wlWildcard);
		fdlWildcard = new FormData();
		fdlWildcard.left = new FormAttachment(0, 0);
		fdlWildcard.top = new FormAttachment(wSourceFileFolder, margin);
		fdlWildcard.right = new FormAttachment(middle, -margin);
		wlWildcard.setLayoutData(fdlWildcard);
		
		wWildcard = new TextVar(jobMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wWildcard.setToolTipText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.Wildcard.Tooltip"));
		props.setLook(wWildcard);
		wWildcard.addModifyListener(lsMod);
		fdWildcard = new FormData();
		fdWildcard.left = new FormAttachment(middle, 0);
		fdWildcard.top = new FormAttachment(wSourceFileFolder, margin);
		fdWildcard.right= new FormAttachment(wbSourceFileFolder, -55);
		wWildcard.setLayoutData(fdWildcard);

		wlFields = new Label(wGeneralComp, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.Fields.Label"));
		props.setLook(wlFields);
		fdlFields = new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.right= new FormAttachment(middle, -margin);
		fdlFields.top = new FormAttachment(wWildcard,margin);
		wlFields.setLayoutData(fdlFields);

		int rows = jobEntry.source_filefolder == null
			? 1
			: (jobEntry.source_filefolder.length == 0
			? 0
			: jobEntry.source_filefolder.length);
		final int FieldsRows = rows;

		ColumnInfo[] colinf=new ColumnInfo[]
			{
				new ColumnInfo(BaseMessages.getString(PKG, "JobEvalFilesMetrics.Fields.SourceFileFolder.Label"),  ColumnInfo.COLUMN_TYPE_TEXT,    false),
				new ColumnInfo(BaseMessages.getString(PKG, "JobEvalFilesMetrics.Fields.Wildcard.Label"), ColumnInfo.COLUMN_TYPE_TEXT,    false ),
				new ColumnInfo(BaseMessages.getString(PKG, "JobEvalFilesMetrics.Fields.IncludeSubDirs.Label"), ColumnInfo.COLUMN_TYPE_CCOMBO,  JobEntryEvalFilesMetrics.IncludeSubFoldersDesc )
			};

		colinf[0].setUsingVariables(true);
		colinf[0].setToolTip(BaseMessages.getString(PKG, "JobEvalFilesMetrics.Fields.SourceFileFolder.Tooltip"));
		colinf[1].setUsingVariables(true);
		colinf[1].setToolTip(BaseMessages.getString(PKG, "JobEvalFilesMetrics.Fields.Wildcard.Tooltip"));

		wFields = new TableView(jobMeta, wGeneralComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf,	FieldsRows, lsMod, props);

		fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(wbeSourceFileFolder, -margin);
		fdFields.bottom = new FormAttachment(100, -margin);
		wFields.setLayoutData(fdFields);

		//RefreshArgFromPrevious();

		// Add the file to the list of files...
		SelectionAdapter selA = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				wFields.add(new String[] { wSourceFileFolder.getText(), wWildcard.getText() } );
				wSourceFileFolder.setText("");

				wWildcard.setText("");
				wFields.removeEmptyRows();
				wFields.setRowNums();
				wFields.optWidth(true);
			}
		};
		wbaSourceFileFolder.addSelectionListener(selA);
		wSourceFileFolder.addSelectionListener(selA);

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

		// Edit the selected file & remove from the list...
		wbeSourceFileFolder.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				int idx = wFields.getSelectionIndex();
				if (idx>=0)
				{
					String string[] = wFields.getItem(idx);
					wSourceFileFolder.setText(string[0]);
					wWildcard.setText(string[1]);
					wFields.remove(idx);
				}
				wFields.removeEmptyRows();
				wFields.setRowNums();
			}
		});
		
		
		

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
		

 		//////////////////////////////////////
		// START OF ADVANCED  TAB   ///
		/////////////////////////////////////
		
		
		
		wAdvancedTab=new CTabItem(wTabFolder, SWT.NONE);
		wAdvancedTab.setText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.Tab.Advanced.Label"));

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		wAdvancedComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wAdvancedComp);
 		wAdvancedComp.setLayout(contentLayout);
 		
 		
 	   
 		
		 // SuccessOngrouping?
	     // ////////////////////////
	     // START OF SUCCESS ON GROUP///
	     // /
	    wSuccessOn= new Group(wAdvancedComp, SWT.SHADOW_NONE);
	    props.setLook(wSuccessOn);
	    wSuccessOn.setText(BaseMessages.getString(PKG, "JobEvalFilesMetrics.SuccessOn.Group.Label"));

	    FormLayout successongroupLayout = new FormLayout();
	    successongroupLayout.marginWidth = 10;
	    successongroupLayout.marginHeight = 10;

	    wSuccessOn.setLayout(successongroupLayout);
	    
	    
		
	    //Scale
	  	wlScale = new Label(wSuccessOn, SWT.RIGHT);
	  	wlScale.setText(BaseMessages.getString(PKG, "JobEvalFilesMetricsDialog.Scale.Label"));
	  	props.setLook(wlScale);
	  	fdlScale = new FormData();
	  	fdlScale.left = new FormAttachment(0, 0);
	  	fdlScale.right = new FormAttachment(middle, -margin);
	  	fdlScale.top = new FormAttachment(0, margin);
	  	wlScale.setLayoutData(fdlScale);
	  	
	  	wScale = new CCombo(wSuccessOn, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wScale.setItems(JobEntryEvalFilesMetrics.scaleDesc);
	  	wScale.select(0); // +1: starts at -1
	  	
		props.setLook(wScale);
		fdScale= new FormData();
		fdScale.left = new FormAttachment(middle, 0);
		fdScale.top = new FormAttachment(0, margin);
		fdScale.right = new FormAttachment(100, 0);
		wScale.setLayoutData(fdScale);
		wScale.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
	    
	    //Success number Condition
	  	wlSuccessNumberCondition = new Label(wSuccessOn, SWT.RIGHT);
	  	wlSuccessNumberCondition.setText(BaseMessages.getString(PKG, "JobEvalFilesMetricsDialog.SuccessCondition.Label"));
	  	props.setLook(wlSuccessNumberCondition);
	  	fdlSuccessNumberCondition = new FormData();
	  	fdlSuccessNumberCondition.left = new FormAttachment(0, 0);
	  	fdlSuccessNumberCondition.right = new FormAttachment(middle, -margin);
	  	fdlSuccessNumberCondition.top = new FormAttachment(wScale, margin);
	  	wlSuccessNumberCondition.setLayoutData(fdlSuccessNumberCondition);
	  	
	  	wSuccessNumberCondition = new CCombo(wSuccessOn, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wSuccessNumberCondition.setItems(JobEntrySimpleEval.successNumberConditionDesc);
	  	wSuccessNumberCondition.select(0); // +1: starts at -1
	  	
		props.setLook(wSuccessNumberCondition);
		fdSuccessNumberCondition= new FormData();
		fdSuccessNumberCondition.left = new FormAttachment(middle, 0);
		fdSuccessNumberCondition.top = new FormAttachment(wScale, margin);
		fdSuccessNumberCondition.right = new FormAttachment(100, 0);
		wSuccessNumberCondition.setLayoutData(fdSuccessNumberCondition);
		wSuccessNumberCondition.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				refresh();
				jobEntry.setChanged();
			}
		});
		
		

		// Compare with value
		wlCompareValue= new Label(wSuccessOn, SWT.RIGHT);
		wlCompareValue.setText(BaseMessages.getString(PKG, "JobEvalFilesMetricsDialog.CompareValue.Label"));
		props.setLook(wlCompareValue);
		fdlCompareValue= new FormData();
		fdlCompareValue.left = new FormAttachment(0, 0);
		fdlCompareValue.top = new FormAttachment(wSuccessNumberCondition, margin);
		fdlCompareValue.right = new FormAttachment(middle, -margin);
		wlCompareValue.setLayoutData(fdlCompareValue);
		
		wCompareValue= new TextVar(jobMeta,wSuccessOn, SWT.SINGLE | SWT.LEFT | SWT.BORDER, 
				BaseMessages.getString(PKG, "JobEvalFilesMetricsDialog.CompareValue.Tooltip"));
		props.setLook(wCompareValue);
		wCompareValue.addModifyListener(lsMod);
		fdCompareValue= new FormData();
		fdCompareValue.left = new FormAttachment(middle, 0);
		fdCompareValue.top = new FormAttachment(wSuccessNumberCondition, margin);
		fdCompareValue.right = new FormAttachment(100, -margin);
		wCompareValue.setLayoutData(fdCompareValue);
		
		// Min value
		wlMinValue= new Label(wSuccessOn, SWT.RIGHT);
		wlMinValue.setText(BaseMessages.getString(PKG, "JobEvalFilesMetricsDialog.MinValue.Label"));
		props.setLook(wlMinValue);
		fdlMinValue= new FormData();
		fdlMinValue.left = new FormAttachment(0, 0);
		fdlMinValue.top = new FormAttachment(wSuccessNumberCondition, margin);
		fdlMinValue.right = new FormAttachment(middle, -margin);
		wlMinValue.setLayoutData(fdlMinValue);
		
		wMinValue= new TextVar(jobMeta,wSuccessOn, SWT.SINGLE | SWT.LEFT | SWT.BORDER, 
				BaseMessages.getString(PKG, "JobEvalFilesMetricsDialog.MinValue.Tooltip"));
		props.setLook(wMinValue);
		wMinValue.addModifyListener(lsMod);
		fdMinValue= new FormData();
		fdMinValue.left = new FormAttachment(middle, 0);
		fdMinValue.top = new FormAttachment(wSuccessNumberCondition, margin);
		fdMinValue.right = new FormAttachment(100, -margin);
		wMinValue.setLayoutData(fdMinValue);
		
		// Maximum value
		wlMaxValue= new Label(wSuccessOn, SWT.RIGHT);
		wlMaxValue.setText(BaseMessages.getString(PKG, "JobEvalFilesMetricsDialog.MaxValue.Label"));
		props.setLook(wlMaxValue);
		fdlMaxValue= new FormData();
		fdlMaxValue.left = new FormAttachment(0, 0);
		fdlMaxValue.top = new FormAttachment(wMinValue, margin);
		fdlMaxValue.right = new FormAttachment(middle, -margin);
		wlMaxValue.setLayoutData(fdlMaxValue);
		
		wMaxValue= new TextVar(jobMeta, wSuccessOn, SWT.SINGLE | SWT.LEFT | SWT.BORDER, BaseMessages.getString(PKG, "JobEvalFilesMetricsDialog.MaxValue.Tooltip"));
		props.setLook(wMaxValue);
		wMaxValue.addModifyListener(lsMod);
		fdMaxValue= new FormData();
		fdMaxValue.left = new FormAttachment(middle, 0);
		fdMaxValue.top = new FormAttachment(wMinValue, margin);
		fdMaxValue.right = new FormAttachment(100, -margin);
		wMaxValue.setLayoutData(fdMaxValue);		

	
	    fdSuccessOn= new FormData();
	    fdSuccessOn.left = new FormAttachment(0, margin);
	    fdSuccessOn.top = new FormAttachment(0, margin);
	    fdSuccessOn.right = new FormAttachment(100, -margin);
	    wSuccessOn.setLayoutData(fdSuccessOn);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Success ON GROUP
	     // ///////////////////////////////////////////////////////////


 		
 		
	    fdAdvancedComp = new FormData();
		fdAdvancedComp.left  = new FormAttachment(0, 0);
 		fdAdvancedComp.top   = new FormAttachment(0, 0);
 		fdAdvancedComp.right = new FormAttachment(100, 0);
 		fdAdvancedComp.bottom= new FormAttachment(100, 0);
 		wAdvancedComp.setLayoutData(wAdvancedComp);

 		wAdvancedComp.layout();
		wAdvancedTab.setControl(wAdvancedComp);


		/////////////////////////////////////////////////////////////
		/// END OF ADVANCED TAB
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

		wName.addSelectionListener( lsDef );
		wSourceFileFolder.addSelectionListener( lsDef );

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();
		refresh();
		RefreshSize();
		RefreshSourceFiles();
		wTabFolder.setSelection(0);
		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
	}

	private void RefreshSourceFiles()
	{
		boolean useStaticFiles=(JobEntryEvalFilesMetrics.getSourceFilesByDesc(wSourceFiles.getText())==JobEntryEvalFilesMetrics.SOURCE_FILES_FILES);
		wlFields.setEnabled(useStaticFiles);
		wFields.setEnabled(useStaticFiles);
		wbdSourceFileFolder.setEnabled(useStaticFiles);
		wbeSourceFileFolder.setEnabled(useStaticFiles);
		wbSourceFileFolder.setEnabled(useStaticFiles);
		wbaSourceFileFolder.setEnabled(useStaticFiles);		
		wlSourceFileFolder.setEnabled(useStaticFiles);
		wSourceFileFolder.setEnabled(useStaticFiles);
		
		wlWildcard.setEnabled(useStaticFiles);
		wWildcard.setEnabled(useStaticFiles);	
		wbSourceDirectory.setEnabled(useStaticFiles);
		
		boolean setResultWildcard=(JobEntryEvalFilesMetrics.getSourceFilesByDesc(wSourceFiles.getText())==JobEntryEvalFilesMetrics.SOURCE_FILES_FILENAMES_RESULT);
		wlResultFilenamesWildcard .setEnabled(setResultWildcard);
		wResultFilenamesWildcard .setEnabled(setResultWildcard);
		
		boolean setResultFields=(JobEntryEvalFilesMetrics.getSourceFilesByDesc(wSourceFiles.getText())==JobEntryEvalFilesMetrics.SOURCE_FILES_PREVIOUS_RESULT);
		wlResultFieldIncludeSubFolders.setEnabled(setResultFields);
		wResultFieldIncludeSubFolders.setEnabled(setResultFields);
		wlResultFieldFile.setEnabled(setResultFields);
		wResultFieldFile.setEnabled(setResultFields);
		wlResultFieldWildcard.setEnabled(setResultFields);
		wResultFieldWildcard.setEnabled(setResultFields);
	}

	private void RefreshSize()
	{
		boolean useSize=(JobEntryEvalFilesMetrics.getEvaluationTypeByDesc(wEvaluationType.getText())==JobEntryEvalFilesMetrics.EVALUATE_TYPE_SIZE);
		wlScale.setVisible(useSize);
		wScale.setVisible(useSize);	
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
		if (jobEntry.getName()!= null) wName.setText( jobEntry.getName() );
		wName.selectAll();
		
		if (jobEntry.source_filefolder != null)
		{
			for (int i = 0; i < jobEntry.source_filefolder.length; i++)
			{
				TableItem ti = wFields.table.getItem(i);
				if (jobEntry.source_filefolder[i] != null)
					ti.setText(1, jobEntry.source_filefolder[i]);

				if (jobEntry.wildcard[i] != null)
					ti.setText(2, jobEntry.wildcard[i]);
				
				if (jobEntry.includeSubFolders[i] != null)
					ti.setText(3, JobEntryEvalFilesMetrics.getIncludeSubFoldersDesc(jobEntry.includeSubFolders[i]));
			}
			wFields.setRowNums();
			wFields.optWidth(true);
		}
		if (jobEntry.getResultFilenamesWildcard()!= null) wResultFilenamesWildcard.setText( jobEntry.getResultFilenamesWildcard() );
		if (jobEntry.getResultFieldFile()!= null) wResultFieldFile.setText( jobEntry.getResultFieldFile());
		if (jobEntry.getResultFieldWildcard()!= null) wResultFieldWildcard.setText( jobEntry.getResultFieldWildcard());
		if (jobEntry.getResultFieldIncludeSubfolders()!= null) wResultFieldIncludeSubFolders.setText( jobEntry.getResultFieldIncludeSubfolders());
		wSourceFiles.setText(JobEntryEvalFilesMetrics.getSourceFilesDesc(jobEntry.sourceFiles));
		wEvaluationType.setText(JobEntryEvalFilesMetrics.getEvaluationTypeDesc(jobEntry.evaluationType));
		wScale.setText(JobEntryEvalFilesMetrics.getScaleDesc(jobEntry.scale));
		wSuccessNumberCondition.setText(JobEntrySimpleEval.getSuccessNumberConditionDesc(jobEntry.successnumbercondition));
		if (jobEntry.getCompareValue()!= null) wCompareValue.setText( jobEntry.getCompareValue() );
		if (jobEntry.getMinValue()!= null) wMinValue.setText( jobEntry.getMinValue() );
		if (jobEntry.getMaxValue()!= null) wMaxValue.setText( jobEntry.getMaxValue() );
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
			mb.setMessage("Veuillez svp donner un nom à cette entrée tâche!");
			mb.setText("Entrée tâche non nommée");
			mb.open(); 
			return;
        }
		jobEntry.setName(wName.getText());
		jobEntry.setResultFilenamesWildcard(wResultFilenamesWildcard .getText());
		jobEntry.setResultFieldFile(wResultFieldFile.getText());
		jobEntry.setResultFieldWildcard(wResultFieldWildcard.getText());
		jobEntry.setResultFieldIncludeSubfolders(wResultFieldIncludeSubFolders.getText());
		jobEntry.sourceFiles=  JobEntryEvalFilesMetrics.getSourceFilesByDesc(wSourceFiles.getText());
		jobEntry.evaluationType=  JobEntryEvalFilesMetrics.getEvaluationTypeByDesc(wEvaluationType.getText());
		jobEntry.scale=  JobEntryEvalFilesMetrics.getScaleByDesc(wScale.getText());
		jobEntry.successnumbercondition=  JobEntrySimpleEval.getSuccessNumberConditionByDesc(wSuccessNumberCondition.getText());
		jobEntry.setCompareValue(wCompareValue.getText());
		jobEntry.setMinValue(wMinValue.getText());
		jobEntry.setMaxValue(wMaxValue.getText());
		int nritems = wFields.nrNonEmpty();
		int nr = 0;
		for (int i = 0; i < nritems; i++)
		{
			String arg = wFields.getNonEmpty(i).getText(1);
			if (arg != null && arg.length() != 0)
				nr++;
		}
		jobEntry.source_filefolder = new String[nr];
		jobEntry.wildcard = new String[nr];
		jobEntry.includeSubFolders = new String[nr];
		nr = 0;
		for (int i = 0; i < nritems; i++)
		{
			String source = wFields.getNonEmpty(i).getText(1);
			String wild = wFields.getNonEmpty(i).getText(2);
			String includeSubFolders = wFields.getNonEmpty(i).getText(3);
			if (source != null && source.length() != 0)
			{
				jobEntry.source_filefolder[nr] = source;
				jobEntry.wildcard[nr] = wild;
				jobEntry.includeSubFolders[nr]=JobEntryEvalFilesMetrics.getIncludeSubFolders(includeSubFolders);
				nr++;
			}
		}
		dispose();
	}
	private void refresh()
	{
		 boolean compareValue= (JobEntrySimpleEval.getSuccessNumberConditionByDesc(wSuccessNumberCondition.getText())
				 !=JobEntrySimpleEval.SUCCESS_NUMBER_CONDITION_BETWEEN);
		 wlCompareValue.setVisible(compareValue);
		 wCompareValue.setVisible(compareValue);
		 wlMinValue.setVisible(!compareValue);
		 wMinValue.setVisible(!compareValue);
		 wlMaxValue.setVisible(!compareValue);
		 wMaxValue.setVisible(!compareValue);
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