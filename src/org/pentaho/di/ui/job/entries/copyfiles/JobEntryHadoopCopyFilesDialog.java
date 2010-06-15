package org.pentaho.di.ui.job.entries.copyfiles;

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

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.copyfiles.JobEntryCopyFiles;
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
 * This dialog allows you to edit the Copy Files job entry settings.
 *
 * @author Samatar Hassan
 * @since  06-05-2007
 */

public class JobEntryHadoopCopyFilesDialog extends JobEntryDialog implements JobEntryDialogInterface
{
    private static Class<?> PKG = JobEntryCopyFiles.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private static final String[] FILETYPES = new String[] 
        {
            BaseMessages.getString(PKG, "JobCopyFiles.Filetype.All") };
    
    private Label        wlName;
    private Text         wName;
    private FormData     fdlName, fdName;

    private Label        wlSourceFileFolder;
    private Button       wbSourceFileFolder,wbDestinationFileFolder,wbSourceDirectory,wbDestinationDirectory;
    private TextVar      wSourceFileFolder;
    private FormData     fdlSourceFileFolder, fdbSourceFileFolder, fdSourceFileFolder,fdbDestinationFileFolder,fdbSourceDirectory,fdbDestinationDirectory;
    
    private Label        wlCopyEmptyFolders;
    private Button       wCopyEmptyFolders;
    private FormData     fdlCopyEmptyFolders, fdCopyEmptyFolders;

    private Label        wlOverwriteFiles;
    private Button       wOverwriteFiles;
    private FormData     fdlOverwriteFiles, fdOverwriteFiles;

    private Label        wlIncludeSubfolders;
    private Button       wIncludeSubfolders;
    private FormData     fdlIncludeSubfolders, fdIncludeSubfolders;
    
    private Label        wlRemoveSourceFiles;
    private Button       wRemoveSourceFiles;
    private FormData     fdlRemoveSourceFiles, fdRemoveSourceFiles;
    
    

    private Button       wOK, wCancel;
    private Listener     lsOK, lsCancel;


    private JobEntryCopyFiles jobEntry;
    private Shell               shell;

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

    private Label wlDestinationFileFolder;
    private TextVar wDestinationFileFolder;
    private FormData fdlDestinationFileFolder, fdDestinationFileFolder;
    
    private Label wlWildcard;
    private TextVar wWildcard;
    private FormData fdlWildcard, fdWildcard;

    private Button       wbdSourceFileFolder; // Delete
    private Button       wbeSourceFileFolder; // Edit
    private Button       wbaSourceFileFolder; // Add or change
    
    
    private CTabFolder   wTabFolder;
    private Composite    wGeneralComp,wResultfilesComp; 
    private CTabItem     wGeneralTab,wResultfilesTab;
    private FormData     fdGeneralComp,fdResultfilesComp;
    private FormData     fdTabFolder;
    
    //  Add File to result
    
    private Group wFileResult;
    private FormData fdFileResult;
    
    
    private Label        wlAddFileToResult;
    private Button       wAddFileToResult;
    private FormData     fdlAddFileToResult, fdAddFileToResult;
    
    private Label        wlCreateDestinationFolder;
    private Button       wCreateDestinationFolder;
    private FormData     fdlCreateDestinationFolder, fdCreateDestinationFolder;
    
    private Label        wlDestinationIsAFile;
    private Button       wDestinationIsAFile;
    private FormData     fdlDestinationIsAFile, fdDestinationIsAFile;
    
    

    private FormData fdbeSourceFileFolder, fdbaSourceFileFolder, fdbdSourceFileFolder;

   public JobEntryHadoopCopyFilesDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryCopyFiles) jobEntryInt;

        if (this.jobEntry.getName() == null) 
            this.jobEntry.setName(BaseMessages.getString(PKG, "JobHadoopCopyFiles.Name.Default"));
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
        shell.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Filename line
        wlName=new Label(shell, SWT.RIGHT);
        wlName.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.Name.Label"));
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
        wGeneralTab.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.Tab.General.Label"));
        
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
        wSettings.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.Settings.Label"));

        FormLayout groupLayout = new FormLayout();
        groupLayout.marginWidth = 10;
        groupLayout.marginHeight = 10;
        wSettings.setLayout(groupLayout);
        
        wlIncludeSubfolders = new Label(wSettings, SWT.RIGHT);
        wlIncludeSubfolders.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.IncludeSubfolders.Label"));
        props.setLook(wlIncludeSubfolders);
        fdlIncludeSubfolders = new FormData();
        fdlIncludeSubfolders.left = new FormAttachment(0, 0);
        fdlIncludeSubfolders.top = new FormAttachment(wName, margin);
        fdlIncludeSubfolders.right = new FormAttachment(middle, -margin);
        wlIncludeSubfolders.setLayoutData(fdlIncludeSubfolders);
        wIncludeSubfolders = new Button(wSettings, SWT.CHECK);
        props.setLook(wIncludeSubfolders);
        wIncludeSubfolders.setToolTipText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.IncludeSubfolders.Tooltip"));
        fdIncludeSubfolders = new FormData();
        fdIncludeSubfolders.left = new FormAttachment(middle, 0);
        fdIncludeSubfolders.top = new FormAttachment(wName, margin);
        fdIncludeSubfolders.right = new FormAttachment(100, 0);
        wIncludeSubfolders.setLayoutData(fdIncludeSubfolders);
        wIncludeSubfolders.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
                CheckIncludeSubFolders();
            }
        });
        
        // Destination is a file?
        wlDestinationIsAFile = new Label(wSettings, SWT.RIGHT);
        wlDestinationIsAFile.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.DestinationIsAFile.Label"));
        props.setLook(wlDestinationIsAFile);
        fdlDestinationIsAFile = new FormData();
        fdlDestinationIsAFile.left = new FormAttachment(0, 0);
        fdlDestinationIsAFile.top = new FormAttachment(wIncludeSubfolders, margin);
        fdlDestinationIsAFile.right = new FormAttachment(middle, -margin);
        wlDestinationIsAFile.setLayoutData(fdlDestinationIsAFile);
        wDestinationIsAFile = new Button(wSettings, SWT.CHECK);
        props.setLook(wDestinationIsAFile);
        wDestinationIsAFile.setToolTipText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.DestinationIsAFile.Tooltip"));
        fdDestinationIsAFile = new FormData();
        fdDestinationIsAFile.left = new FormAttachment(middle, 0);
        fdDestinationIsAFile.top = new FormAttachment(wIncludeSubfolders, margin);
        fdDestinationIsAFile.right = new FormAttachment(100, 0);
        wDestinationIsAFile.setLayoutData(fdDestinationIsAFile);
        wDestinationIsAFile.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });
        
        // Copy empty folders
        wlCopyEmptyFolders = new Label(wSettings, SWT.RIGHT);
        wlCopyEmptyFolders.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.CopyEmptyFolders.Label"));
        props.setLook(wlCopyEmptyFolders);
        fdlCopyEmptyFolders = new FormData();
        fdlCopyEmptyFolders.left = new FormAttachment(0, 0);
        fdlCopyEmptyFolders.top = new FormAttachment(wDestinationIsAFile, margin);
        fdlCopyEmptyFolders.right = new FormAttachment(middle, -margin);
        wlCopyEmptyFolders.setLayoutData(fdlCopyEmptyFolders);
        wCopyEmptyFolders = new Button(wSettings, SWT.CHECK);
        props.setLook(wCopyEmptyFolders);
        wCopyEmptyFolders.setToolTipText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.CopyEmptyFolders.Tooltip"));
        fdCopyEmptyFolders = new FormData();
        fdCopyEmptyFolders.left = new FormAttachment(middle, 0);
        fdCopyEmptyFolders.top = new FormAttachment(wDestinationIsAFile, margin);
        fdCopyEmptyFolders.right = new FormAttachment(100, 0);
        wCopyEmptyFolders.setLayoutData(fdCopyEmptyFolders);
        wCopyEmptyFolders.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });
        
        // Create destination folder/parent folder
        wlCreateDestinationFolder = new Label(wSettings, SWT.RIGHT);
        wlCreateDestinationFolder.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.CreateDestinationFolder.Label"));
        props.setLook(wlCreateDestinationFolder);
        fdlCreateDestinationFolder = new FormData();
        fdlCreateDestinationFolder.left = new FormAttachment(0, 0);
        fdlCreateDestinationFolder.top = new FormAttachment(wCopyEmptyFolders, margin);
        fdlCreateDestinationFolder.right = new FormAttachment(middle, -margin);
        wlCreateDestinationFolder.setLayoutData(fdlCreateDestinationFolder);
        wCreateDestinationFolder = new Button(wSettings, SWT.CHECK);
        props.setLook(wCreateDestinationFolder);
        wCreateDestinationFolder.setToolTipText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.CreateDestinationFolder.Tooltip"));
        fdCreateDestinationFolder = new FormData();
        fdCreateDestinationFolder.left = new FormAttachment(middle, 0);
        fdCreateDestinationFolder.top = new FormAttachment(wCopyEmptyFolders, margin);
        fdCreateDestinationFolder.right = new FormAttachment(100, 0);
        wCreateDestinationFolder.setLayoutData(fdCreateDestinationFolder);
        wCreateDestinationFolder.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });
        
        // OverwriteFiles Option 
        wlOverwriteFiles = new Label(wSettings, SWT.RIGHT);
        wlOverwriteFiles.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.OverwriteFiles.Label"));
        props.setLook(wlOverwriteFiles);
        fdlOverwriteFiles = new FormData();
        fdlOverwriteFiles.left = new FormAttachment(0, 0);
        fdlOverwriteFiles.top = new FormAttachment(wCreateDestinationFolder, margin);
        fdlOverwriteFiles.right = new FormAttachment(middle, -margin);
        wlOverwriteFiles.setLayoutData(fdlOverwriteFiles);
        wOverwriteFiles = new Button(wSettings, SWT.CHECK);
        props.setLook(wOverwriteFiles);
        wOverwriteFiles.setToolTipText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.OverwriteFiles.Tooltip"));
        fdOverwriteFiles = new FormData();
        fdOverwriteFiles.left = new FormAttachment(middle, 0);
        fdOverwriteFiles.top = new FormAttachment(wCreateDestinationFolder, margin);
        fdOverwriteFiles.right = new FormAttachment(100, 0);
        wOverwriteFiles.setLayoutData(fdOverwriteFiles);
        wOverwriteFiles.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });


    
        // Remove source files option
        wlRemoveSourceFiles = new Label(wSettings, SWT.RIGHT);
        wlRemoveSourceFiles.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.RemoveSourceFiles.Label"));
        props.setLook(wlRemoveSourceFiles);
        fdlRemoveSourceFiles = new FormData();
        fdlRemoveSourceFiles.left = new FormAttachment(0, 0);
        fdlRemoveSourceFiles.top = new FormAttachment(wOverwriteFiles, margin);
        fdlRemoveSourceFiles.right = new FormAttachment(middle, -margin);
        wlRemoveSourceFiles.setLayoutData(fdlRemoveSourceFiles);
        wRemoveSourceFiles = new Button(wSettings, SWT.CHECK);
        props.setLook(wRemoveSourceFiles);
        wRemoveSourceFiles.setToolTipText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.RemoveSourceFiles.Tooltip"));
        fdRemoveSourceFiles = new FormData();
        fdRemoveSourceFiles.left = new FormAttachment(middle, 0);
        fdRemoveSourceFiles.top = new FormAttachment(wOverwriteFiles, margin);
        fdRemoveSourceFiles.right = new FormAttachment(100, 0);
        wRemoveSourceFiles.setLayoutData(fdRemoveSourceFiles);
        wRemoveSourceFiles.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });
        
        wlPrevious = new Label(wSettings, SWT.RIGHT);
        wlPrevious.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.Previous.Label"));
        props.setLook(wlPrevious);
        fdlPrevious = new FormData();
        fdlPrevious.left = new FormAttachment(0, 0);
        fdlPrevious.top = new FormAttachment(wRemoveSourceFiles, margin );
        fdlPrevious.right = new FormAttachment(middle, -margin);
        wlPrevious.setLayoutData(fdlPrevious);
        wPrevious = new Button(wSettings, SWT.CHECK);
        props.setLook(wPrevious);
        wPrevious.setSelection(jobEntry.arg_from_previous);
        wPrevious.setToolTipText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.Previous.Tooltip"));
        fdPrevious = new FormData();
        fdPrevious.left = new FormAttachment(middle, 0);
        fdPrevious.top = new FormAttachment(wRemoveSourceFiles, margin );
        fdPrevious.right = new FormAttachment(100, 0);
        wPrevious.setLayoutData(fdPrevious);
        wPrevious.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {

                RefreshArgFromPrevious();               
                
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
        wlSourceFileFolder.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.SourceFileFolder.Label"));
        props.setLook(wlSourceFileFolder);
        fdlSourceFileFolder=new FormData();
        fdlSourceFileFolder.left = new FormAttachment(0, 0);
        fdlSourceFileFolder.top  = new FormAttachment(wSettings, 2*margin);
        fdlSourceFileFolder.right= new FormAttachment(middle, -margin);
        wlSourceFileFolder.setLayoutData(fdlSourceFileFolder);

        // Browse Source folders button ...
        wbSourceDirectory=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
        props.setLook(wbSourceDirectory);
        wbSourceDirectory.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.BrowseFolders.Label"));
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
        wbSourceFileFolder.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.BrowseFiles.Label"));
        fdbSourceFileFolder=new FormData();
        fdbSourceFileFolder.right= new FormAttachment(wbSourceDirectory, -margin);
        fdbSourceFileFolder.top  = new FormAttachment(wSettings, margin);
        wbSourceFileFolder.setLayoutData(fdbSourceFileFolder);
        
        // Browse Source file add button ...
        wbaSourceFileFolder=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
        props.setLook(wbaSourceFileFolder);
        wbaSourceFileFolder.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.FilenameAdd.Button"));
        fdbaSourceFileFolder=new FormData();
        fdbaSourceFileFolder.right= new FormAttachment(wbSourceFileFolder, -margin);
        fdbaSourceFileFolder.top  = new FormAttachment(wSettings, margin);
        wbaSourceFileFolder.setLayoutData(fdbaSourceFileFolder);

        wSourceFileFolder=new TextVar(jobMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wSourceFileFolder.setToolTipText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.SourceFileFolder.Tooltip"));
        
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
        
        // Destination
        wlDestinationFileFolder = new Label(wGeneralComp, SWT.RIGHT);
        wlDestinationFileFolder.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.DestinationFileFolder.Label"));
        props.setLook(wlDestinationFileFolder);
        fdlDestinationFileFolder = new FormData();
        fdlDestinationFileFolder.left = new FormAttachment(0, 0);
        fdlDestinationFileFolder.top = new FormAttachment(wSourceFileFolder, margin);
        fdlDestinationFileFolder.right = new FormAttachment(middle, -margin);
        wlDestinationFileFolder.setLayoutData(fdlDestinationFileFolder);
        
        
        
        // Browse Destination folders button ...
        wbDestinationDirectory=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
        props.setLook(wbDestinationDirectory);
        wbDestinationDirectory.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.BrowseFolders.Label"));
        fdbDestinationDirectory=new FormData();
        fdbDestinationDirectory.right= new FormAttachment(100, 0);
        fdbDestinationDirectory.top  = new FormAttachment(wSourceFileFolder, margin);
        wbDestinationDirectory.setLayoutData(fdbDestinationDirectory);
        
        
        wbDestinationDirectory.addSelectionListener
        (
            new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
                    if (wDestinationFileFolder.getText()!=null)
                    {
                        ddialog.setFilterPath(jobMeta.environmentSubstitute(wDestinationFileFolder.getText()) );
                    }
                    
                     // Calling open() will open and run the dialog.
                    // It will return the selected directory, or
                    // null if user cancels
                    String dir = ddialog.open();
                    if (dir != null) {
                      // Set the text box to the new selection
                        wDestinationFileFolder.setText(dir);
                    }
                    
                }
            }
        );

        
        
        
        
        // Browse Destination file browse button ...
        wbDestinationFileFolder=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
        props.setLook(wbDestinationFileFolder);
        wbDestinationFileFolder.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.BrowseFiles.Label"));
        fdbDestinationFileFolder=new FormData();
        fdbDestinationFileFolder.right= new FormAttachment(wbDestinationDirectory, -margin);
        fdbDestinationFileFolder.top  = new FormAttachment(wSourceFileFolder, margin);
        wbDestinationFileFolder.setLayoutData(fdbDestinationFileFolder);
        
                
        
        wDestinationFileFolder = new TextVar(jobMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER); 
        wDestinationFileFolder.setToolTipText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.DestinationFileFolder.Tooltip"));
        props.setLook(wDestinationFileFolder);
        wDestinationFileFolder.addModifyListener(lsMod);
        fdDestinationFileFolder = new FormData();
        fdDestinationFileFolder.left = new FormAttachment(middle, 0);
        fdDestinationFileFolder.top = new FormAttachment(wSourceFileFolder, margin);
        fdDestinationFileFolder.right= new FormAttachment(wbSourceFileFolder, -55);
        wDestinationFileFolder.setLayoutData(fdDestinationFileFolder);
        
        wbDestinationFileFolder.addSelectionListener
            (
            new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterExtensions(new String[] {"*"});
                if (wDestinationFileFolder.getText()!=null)
                {
                    dialog.setFileName(jobMeta.environmentSubstitute(wDestinationFileFolder.getText()) );
                }
                dialog.setFilterNames(FILETYPES);
                if (dialog.open()!=null)
                {
                    wDestinationFileFolder.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
                }
            }
        }
            );

        // Buttons to the right of the screen...
        wbdSourceFileFolder=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
        props.setLook(wbdSourceFileFolder);
        wbdSourceFileFolder.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.FilenameDelete.Button"));
        wbdSourceFileFolder.setToolTipText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.FilenameDelete.Tooltip"));
        fdbdSourceFileFolder=new FormData();
        fdbdSourceFileFolder.right = new FormAttachment(100, 0);
        fdbdSourceFileFolder.top  = new FormAttachment (wDestinationFileFolder, 40);
        wbdSourceFileFolder.setLayoutData(fdbdSourceFileFolder);

        wbeSourceFileFolder=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
        props.setLook(wbeSourceFileFolder);
        wbeSourceFileFolder.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.FilenameEdit.Button"));
        wbeSourceFileFolder.setToolTipText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.FilenameEdit.Tooltip"));
        fdbeSourceFileFolder=new FormData();
        fdbeSourceFileFolder.right = new FormAttachment(100, 0);
        fdbeSourceFileFolder.left = new FormAttachment(wbdSourceFileFolder, 0, SWT.LEFT);
        fdbeSourceFileFolder.top  = new FormAttachment (wbdSourceFileFolder, margin);
        wbeSourceFileFolder.setLayoutData(fdbeSourceFileFolder);
        
        
        
        // Wildcard
        wlWildcard = new Label(wGeneralComp, SWT.RIGHT);
        wlWildcard.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.Wildcard.Label"));
        props.setLook(wlWildcard);
        fdlWildcard = new FormData();
        fdlWildcard.left = new FormAttachment(0, 0);
        fdlWildcard.top = new FormAttachment(wDestinationFileFolder, margin);
        fdlWildcard.right = new FormAttachment(middle, -margin);
        wlWildcard.setLayoutData(fdlWildcard);
        
        wWildcard = new TextVar(jobMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wWildcard.setToolTipText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.Wildcard.Tooltip"));
        props.setLook(wWildcard);
        wWildcard.addModifyListener(lsMod);
        fdWildcard = new FormData();
        fdWildcard.left = new FormAttachment(middle, 0);
        fdWildcard.top = new FormAttachment(wDestinationFileFolder, margin);
        fdWildcard.right= new FormAttachment(wbSourceFileFolder, -55);
        wWildcard.setLayoutData(fdWildcard);

        wlFields = new Label(wGeneralComp, SWT.NONE);
        wlFields.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.Fields.Label"));
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
                new ColumnInfo(BaseMessages.getString(PKG, "JobHadoopCopyFiles.Fields.SourceFileFolder.Label"),  ColumnInfo.COLUMN_TYPE_TEXT,    false),
                new ColumnInfo(BaseMessages.getString(PKG, "JobHadoopCopyFiles.Fields.DestinationFileFolder.Label"),  ColumnInfo.COLUMN_TYPE_TEXT,    false),
                new ColumnInfo(BaseMessages.getString(PKG, "JobHadoopCopyFiles.Fields.Wildcard.Label"), ColumnInfo.COLUMN_TYPE_TEXT,    false ),
            };

        colinf[0].setUsingVariables(true);
        colinf[0].setToolTip(BaseMessages.getString(PKG, "JobHadoopCopyFiles.Fields.SourceFileFolder.Tooltip"));
        colinf[1].setUsingVariables(true);
        colinf[1].setToolTip(BaseMessages.getString(PKG, "JobHadoopCopyFiles.Fields.DestinationFileFolder.Tooltip"));
        colinf[2].setUsingVariables(true);
        colinf[2].setToolTip(BaseMessages.getString(PKG, "JobHadoopCopyFiles.Fields.Wildcard.Tooltip"));

        wFields = new TableView(jobMeta, wGeneralComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props);

        fdFields = new FormData();
        fdFields.left = new FormAttachment(0, 0);
        fdFields.top = new FormAttachment(wlFields, margin);
        fdFields.right = new FormAttachment(100, -75);
        fdFields.bottom = new FormAttachment(100, -margin);
        wFields.setLayoutData(fdFields);

        RefreshArgFromPrevious();

        // Add the file to the list of files...
        SelectionAdapter selA = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                wFields.add(new String[] { wSourceFileFolder.getText(), wDestinationFileFolder.getText(), wWildcard.getText() } );
                wSourceFileFolder.setText("");
                wDestinationFileFolder.setText("");
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
                    wDestinationFileFolder.setText(string[1]);
                    wWildcard.setText(string[2]);
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
        // START OF RESULT FILES  TAB   ///
        /////////////////////////////////////

        wResultfilesTab=new CTabItem(wTabFolder, SWT.NONE);
        wResultfilesTab.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.Tab.AddResultFilesName.Label"));

        FormLayout contentLayout = new FormLayout ();
        contentLayout.marginWidth  = 3;
        contentLayout.marginHeight = 3;
        
        wResultfilesComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wResultfilesComp);
        wResultfilesComp.setLayout(contentLayout);
        
        
         // fileresult grouping?
         // ////////////////////////
         // START OF LOGGING GROUP///
         // /
        wFileResult = new Group(wResultfilesComp, SWT.SHADOW_NONE);
        props.setLook(wFileResult);
        wFileResult.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.FileResult.Group.Label"));

        FormLayout fileresultgroupLayout = new FormLayout();
        fileresultgroupLayout.marginWidth = 10;
        fileresultgroupLayout.marginHeight = 10;

        wFileResult.setLayout(fileresultgroupLayout);
          
          
        //Add file to result
        wlAddFileToResult = new Label(wFileResult, SWT.RIGHT);
        wlAddFileToResult.setText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.AddFileToResult.Label"));
        props.setLook(wlAddFileToResult);
        fdlAddFileToResult = new FormData();
        fdlAddFileToResult.left = new FormAttachment(0, 0);
        fdlAddFileToResult.top = new FormAttachment(0, margin);
        fdlAddFileToResult.right = new FormAttachment(middle, -margin);
        wlAddFileToResult.setLayoutData(fdlAddFileToResult);
        wAddFileToResult = new Button(wFileResult, SWT.CHECK);
        props.setLook(wAddFileToResult);
        wAddFileToResult.setToolTipText(BaseMessages.getString(PKG, "JobHadoopCopyFiles.AddFileToResult.Tooltip"));
        fdAddFileToResult = new FormData();
        fdAddFileToResult.left = new FormAttachment(middle, 0);
        fdAddFileToResult.top = new FormAttachment(0, margin);
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
         fdFileResult.top = new FormAttachment(0, margin);
         fdFileResult.right = new FormAttachment(100, -margin);
         wFileResult.setLayoutData(fdFileResult);
         // ///////////////////////////////////////////////////////////
         // / END OF FilesRsult GROUP
         // ///////////////////////////////////////////////////////////
        
        
        fdResultfilesComp = new FormData();
        fdResultfilesComp.left  = new FormAttachment(0, 0);
        fdResultfilesComp.top   = new FormAttachment(0, 0);
        fdResultfilesComp.right = new FormAttachment(100, 0);
        fdResultfilesComp.bottom= new FormAttachment(100, 0);
        wResultfilesComp.setLayoutData(wResultfilesComp);

        wResultfilesComp.layout();
        wResultfilesTab.setControl(wResultfilesComp);


        /////////////////////////////////////////////////////////////
        /// END OF RESULT FILES TAB
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
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

        getData();
        CheckIncludeSubFolders();
        wTabFolder.setSelection(0);
        BaseStepDialog.setSize(shell);

        shell.open();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
        }
        return jobEntry;
    }

    private void RefreshArgFromPrevious()
    {

        wlFields.setEnabled(!wPrevious.getSelection());
        wFields.setEnabled(!wPrevious.getSelection());
        wbdSourceFileFolder.setEnabled(!wPrevious.getSelection());
        wbeSourceFileFolder.setEnabled(!wPrevious.getSelection());
        wbSourceFileFolder.setEnabled(!wPrevious.getSelection());
        wbaSourceFileFolder.setEnabled(!wPrevious.getSelection());      
        wbDestinationFileFolder.setEnabled(!wPrevious.getSelection());
        wlDestinationFileFolder.setEnabled(!wPrevious.getSelection());
        wDestinationFileFolder.setEnabled(!wPrevious.getSelection());
        wlSourceFileFolder.setEnabled(!wPrevious.getSelection());
        wSourceFileFolder.setEnabled(!wPrevious.getSelection());
        
        wlWildcard.setEnabled(!wPrevious.getSelection());
        wWildcard.setEnabled(!wPrevious.getSelection());    
        wbSourceDirectory.setEnabled(!wPrevious.getSelection());
        wbDestinationDirectory.setEnabled(!wPrevious.getSelection());
        
        
    }

    public void dispose()
    {
        WindowProperty winprop = new WindowProperty(shell);
        props.setScreen(winprop);
        shell.dispose();
    }
    
    private void CheckIncludeSubFolders()
    {
        wlCopyEmptyFolders.setEnabled(wIncludeSubfolders.getSelection());
        wCopyEmptyFolders.setEnabled(wIncludeSubfolders.getSelection());
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    public void getData()
    {
        if (jobEntry.getName()    != null) wName.setText( jobEntry.getName() );
        wName.selectAll();
        wCopyEmptyFolders.setSelection(jobEntry.copy_empty_folders);
        
        if (jobEntry.source_filefolder != null)
        {
            for (int i = 0; i < jobEntry.source_filefolder.length; i++)
            {
                TableItem ti = wFields.table.getItem(i);
                if (jobEntry.source_filefolder[i] != null)
                    ti.setText(1, jobEntry.source_filefolder[i]);
                if (jobEntry.destination_filefolder[i] != null)
                    ti.setText(2, jobEntry.destination_filefolder[i]);
                if (jobEntry.wildcard[i] != null)
                    ti.setText(3, jobEntry.wildcard[i]);
            }
            wFields.setRowNums();
            wFields.optWidth(true);
        }
        wPrevious.setSelection(jobEntry.arg_from_previous);
        wOverwriteFiles.setSelection(jobEntry.overwrite_files);
        wIncludeSubfolders.setSelection(jobEntry.include_subfolders);
        wRemoveSourceFiles.setSelection(jobEntry.remove_source_files);
        wDestinationIsAFile.setSelection(jobEntry.destination_is_a_file);
        wCreateDestinationFolder.setSelection(jobEntry.create_destination_folder);
            
        
        wAddFileToResult.setSelection(jobEntry.add_result_filesname);
        

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
        jobEntry.setCopyEmptyFolders(wCopyEmptyFolders.getSelection());
        jobEntry.setoverwrite_files(wOverwriteFiles.getSelection());
        jobEntry.setIncludeSubfolders(wIncludeSubfolders.getSelection());
        jobEntry.setArgFromPrevious(wPrevious.getSelection());
        jobEntry.setRemoveSourceFiles(wRemoveSourceFiles.getSelection());
        jobEntry.setAddresultfilesname(wAddFileToResult.getSelection());
        jobEntry.setDestinationIsAFile(wDestinationIsAFile.getSelection());
        jobEntry.setCreateDestinationFolder(wCreateDestinationFolder.getSelection());

        int nritems = wFields.nrNonEmpty();
        int nr = 0;
        for (int i = 0; i < nritems; i++)
        {
            String arg = wFields.getNonEmpty(i).getText(1);
            if (arg != null && arg.length() != 0)
                nr++;
        }
        jobEntry.source_filefolder = new String[nr];
        jobEntry.destination_filefolder = new String[nr];
        jobEntry.wildcard = new String[nr];
        nr = 0;
        for (int i = 0; i < nritems; i++)
        {
            String source = wFields.getNonEmpty(i).getText(1);
            String dest = wFields.getNonEmpty(i).getText(2);
            String wild = wFields.getNonEmpty(i).getText(3);
            if (source != null && source.length() != 0)
            {
                jobEntry.source_filefolder[nr] = source;
                jobEntry.destination_filefolder[nr] = dest;
                jobEntry.wildcard[nr] = wild;
                nr++;
            }
        }
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