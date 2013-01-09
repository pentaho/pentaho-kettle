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

package org.pentaho.di.ui.job.entries.folderscompare;

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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.folderscompare.JobEntryFoldersCompare;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


/**
 * This dialog allows you to edit the Folders compare job entry settings.
 *
 * @author Samatar Hassan
 * @since  25-11-2007
 */
public class JobEntryFoldersCompareDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryFoldersCompare.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

   private static final String[] FILETYPES = new String[] {
           BaseMessages.getString(PKG, "JobFoldersCompare.Filetype.All") };

	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlFilename1;
	private Button       wbFilename1,wbDirectory1;
	private TextVar      wFilename1;
	private FormData     fdlFilename1, fdbFilename1, fdFilename1,fdbDirectory1;

	private Label        wlFilename2;
	private Button       wbFilename2,wbDirectory2;
	private TextVar      wFilename2;
	private FormData     fdlFilename2, fdbFilename2, fdFilename2,fdbDirectory2;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntryFoldersCompare jobEntry;
	private Shell       	shell;

	private SelectionAdapter lsDef;

	private boolean changed;
	
	private Group wSettings;
	private FormData fdSettings;
	
	private Label        wlIncludeSubfolders;
	private Button       wIncludeSubfolders;
	private FormData     fdlIncludeSubfolders, fdIncludeSubfolders;
	
	private Label        wlCompareFileContent;
	private Button       wCompareFileContent;
	private FormData     fdlCompareFileContent, fdCompareFileContent;
	
	private Label wlCompareOnly;
	private CCombo wCompareOnly;
	private FormData fdlCompareOnly, fdCompareOnly;
	
	private Label wlWildcard;
	private TextVar wWildcard;
	private FormData fdlWildcard, fdWildcard;
	
	private Label        wlCompareFileSize;
	private Button       wCompareFileSize;
	private FormData     fdlCompareFileSize, fdCompareFileSize;

	 public JobEntryFoldersCompareDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryFoldersCompare) jobEntryInt;
		if (this.jobEntry.getName() == null)
			this.jobEntry.setName(BaseMessages.getString(PKG, "JobFoldersCompare.Name.Default"));
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
		shell.setText(BaseMessages.getString(PKG, "JobFoldersCompare.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Name line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(BaseMessages.getString(PKG, "JobFoldersCompare.Name.Label"));
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
		wSettings.setText(BaseMessages.getString(PKG, "JobFoldersCompare.Settings.Label"));

		FormLayout groupLayout = new FormLayout();
		groupLayout.marginWidth = 10;
		groupLayout.marginHeight = 10;
		wSettings.setLayout(groupLayout);
		
		wlIncludeSubfolders = new Label(wSettings, SWT.RIGHT);
		wlIncludeSubfolders.setText(BaseMessages.getString(PKG, "JobFoldersCompare.IncludeSubfolders.Label"));
		props.setLook(wlIncludeSubfolders);
		fdlIncludeSubfolders = new FormData();
		fdlIncludeSubfolders.left = new FormAttachment(0, 0);
		fdlIncludeSubfolders.top = new FormAttachment(wName, margin);
		fdlIncludeSubfolders.right = new FormAttachment(middle, -margin);
		wlIncludeSubfolders.setLayoutData(fdlIncludeSubfolders);
		wIncludeSubfolders = new Button(wSettings, SWT.CHECK);
		props.setLook(wIncludeSubfolders);
		wIncludeSubfolders.setToolTipText(BaseMessages.getString(PKG, "JobFoldersCompare.IncludeSubfolders.Tooltip"));
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
			}
		});
		
		
		//Compare Only?
		wlCompareOnly = new Label(wSettings, SWT.RIGHT);
		wlCompareOnly.setText(BaseMessages.getString(PKG, "JobFoldersCompare.CompareOnly.Label"));
		props.setLook(wlCompareOnly);
		fdlCompareOnly = new FormData();
		fdlCompareOnly.left = new FormAttachment(0, 0);
		fdlCompareOnly.right = new FormAttachment(middle, 0);
		fdlCompareOnly.top = new FormAttachment(wIncludeSubfolders, margin);
		wlCompareOnly.setLayoutData(fdlCompareOnly);
		wCompareOnly = new CCombo(wSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wCompareOnly.add(BaseMessages.getString(PKG, "JobFoldersCompare.All_CompareOnly.Label"));
		wCompareOnly.add(BaseMessages.getString(PKG, "JobFoldersCompare.Files_CompareOnly.Label"));
		wCompareOnly.add(BaseMessages.getString(PKG, "JobFoldersCompare.Folders_CompareOnly.Label"));
		wCompareOnly.add(BaseMessages.getString(PKG, "JobFoldersCompare.Specify_CompareOnly.Label"));

		wCompareOnly.select(0); // +1: starts at -1

		props.setLook(wCompareOnly);
		fdCompareOnly= new FormData();
		fdCompareOnly.left = new FormAttachment(middle, 0);
		fdCompareOnly.top = new FormAttachment(wIncludeSubfolders, margin);
		fdCompareOnly.right = new FormAttachment(100, -margin);
		wCompareOnly.setLayoutData(fdCompareOnly);

		wCompareOnly.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				SpecifyCompareOnlyActivate();
				
			}
		});
		
		// Wildcard
		wlWildcard = new Label(wSettings, SWT.RIGHT);
		wlWildcard.setText(BaseMessages.getString(PKG, "JobFoldersCompare.Wildcard.Label"));
		props.setLook(wlWildcard);
		fdlWildcard = new FormData();
		fdlWildcard.left = new FormAttachment(0, 0);
		fdlWildcard.top = new FormAttachment(wCompareOnly, margin);
		fdlWildcard.right = new FormAttachment(middle, -margin);
		wlWildcard.setLayoutData(fdlWildcard);
		wWildcard = new TextVar(jobMeta,wSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER, 
				BaseMessages.getString(PKG, "JobFoldersCompare.Wildcard.Tooltip"));
		props.setLook(wWildcard);
		wWildcard.addModifyListener(lsMod);
		fdWildcard = new FormData();
		fdWildcard.left = new FormAttachment(middle, 0);
		fdWildcard.top = new FormAttachment(wCompareOnly, margin);
		fdWildcard.right = new FormAttachment(100, -margin);
		wWildcard.setLayoutData(fdWildcard);
		
		wlCompareFileSize = new Label(wSettings, SWT.RIGHT);
		wlCompareFileSize.setText(BaseMessages.getString(PKG, "JobFoldersCompare.CompareFileSize.Label"));
		props.setLook(wlCompareFileSize);
		fdlCompareFileSize = new FormData();
		fdlCompareFileSize.left = new FormAttachment(0, 0);
		fdlCompareFileSize.top = new FormAttachment(wWildcard, margin);
		fdlCompareFileSize.right = new FormAttachment(middle, -margin);
		wlCompareFileSize.setLayoutData(fdlCompareFileSize);
		wCompareFileSize = new Button(wSettings, SWT.CHECK);
		props.setLook(wCompareFileSize);
		wCompareFileSize.setToolTipText(BaseMessages.getString(PKG, "JobFoldersCompare.CompareFileSize.Tooltip"));
		fdCompareFileSize = new FormData();
		fdCompareFileSize.left = new FormAttachment(middle, 0);
		fdCompareFileSize.top = new FormAttachment(wWildcard, margin);
		fdCompareFileSize.right = new FormAttachment(100, 0);
		wCompareFileSize.setLayoutData(fdCompareFileSize);
		wCompareFileSize.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		
		wlCompareFileContent = new Label(wSettings, SWT.RIGHT);
		wlCompareFileContent.setText(BaseMessages.getString(PKG, "JobFoldersCompare.CompareFileContent.Label"));
		props.setLook(wlCompareFileContent);
		fdlCompareFileContent = new FormData();
		fdlCompareFileContent.left = new FormAttachment(0, 0);
		fdlCompareFileContent.top = new FormAttachment(wCompareFileSize, margin);
		fdlCompareFileContent.right = new FormAttachment(middle, -margin);
		wlCompareFileContent.setLayoutData(fdlCompareFileContent);
		wCompareFileContent = new Button(wSettings, SWT.CHECK);
		props.setLook(wCompareFileContent);
		wCompareFileContent.setToolTipText(BaseMessages.getString(PKG, "JobFoldersCompare.CompareFileContent.Tooltip"));
		fdCompareFileContent = new FormData();
		fdCompareFileContent.left = new FormAttachment(middle, 0);
		fdCompareFileContent.top = new FormAttachment(wCompareFileSize, margin);
		fdCompareFileContent.right = new FormAttachment(100, 0);
		wCompareFileContent.setLayoutData(fdCompareFileContent);
		wCompareFileContent.addSelectionListener(new SelectionAdapter()
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
		
		
		
		
		// Filename 1 line
		wlFilename1=new Label(shell, SWT.RIGHT);
		wlFilename1.setText(BaseMessages.getString(PKG, "JobFoldersCompare.Filename1.Label"));
 		props.setLook(wlFilename1);
		fdlFilename1=new FormData();
		fdlFilename1.left = new FormAttachment(0, 0);
		fdlFilename1.top  = new FormAttachment(wSettings, 2*margin);
		fdlFilename1.right= new FormAttachment(middle, -margin);
		wlFilename1.setLayoutData(fdlFilename1);
		
		// Browse folders button ...
		wbDirectory1=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbDirectory1);
		wbDirectory1.setText(BaseMessages.getString(PKG, "JobFoldersCompare.FolderBrowse.Label"));
		fdbDirectory1=new FormData();
		fdbDirectory1.right= new FormAttachment(100, -margin);
		fdbDirectory1.top  = new FormAttachment(wSettings, 2*margin);
		wbDirectory1.setLayoutData(fdbDirectory1);
		
		wbDirectory1.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wFilename1.getText()!=null)
					{
						ddialog.setFilterPath(jobMeta.environmentSubstitute(wFilename1.getText()) );
					}
					
					 // Calling open() will open and run the dialog.
			        // It will return the selected directory, or
			        // null if user cancels
			        String dir = ddialog.open();
			        if (dir != null) {
			          // Set the text box to the new selection
			        	wFilename1.setText(dir);
			        }
					
				}
			}
		);
		
		// Browse files ..
		wbFilename1=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFilename1);
		wbFilename1.setText(BaseMessages.getString(PKG, "JobFoldersCompare.FileBrowse.Label"));
		fdbFilename1=new FormData();
		fdbFilename1.right= new FormAttachment(wbDirectory1, -margin);
		fdbFilename1.top  = new FormAttachment(wSettings, 2*margin);
		wbFilename1.setLayoutData(fdbFilename1);
		
		wFilename1=new TextVar(jobMeta,shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename1);
		wFilename1.addModifyListener(lsMod);
		fdFilename1=new FormData();
		fdFilename1.left = new FormAttachment(middle, 0);
		fdFilename1.top  = new FormAttachment(wSettings, 2*margin);
		fdFilename1.right= new FormAttachment(wbFilename1, -margin);
		wFilename1.setLayoutData(fdFilename1);

		// Whenever something changes, set the tooltip to the expanded version:
		wFilename1.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wFilename1.setToolTipText(jobMeta.environmentSubstitute( wFilename1.getText() ) );
				}
			}
		);

		wbFilename1.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] {"*"});
					if (wFilename1.getText()!=null)
					{
						dialog.setFileName(jobMeta.environmentSubstitute(wFilename1.getText()) );
					}
					dialog.setFilterNames(FILETYPES);
					if (dialog.open()!=null)
					{
						wFilename1.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
					}
				}
			}
		);
		

				

		// Filename 2 line
		wlFilename2=new Label(shell, SWT.RIGHT);
		wlFilename2.setText(BaseMessages.getString(PKG, "JobFoldersCompare.Filename2.Label"));
 		props.setLook(wlFilename2);
		fdlFilename2=new FormData();
		fdlFilename2.left = new FormAttachment(0, 0);
		fdlFilename2.top  = new FormAttachment(wFilename1, margin);
		fdlFilename2.right= new FormAttachment(middle, -margin);
		wlFilename2.setLayoutData(fdlFilename2);
		
		// Browse folders button ...
		wbDirectory2=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbDirectory2);
		wbDirectory2.setText(BaseMessages.getString(PKG, "JobFoldersCompare.FolderBrowse.Label"));
		fdbDirectory2=new FormData();
		fdbDirectory2.right= new FormAttachment(100, -margin);
		fdbDirectory2.top  = new FormAttachment(wFilename1, margin);
		wbDirectory2.setLayoutData(fdbDirectory2);
		
		wbDirectory2.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wFilename2.getText()!=null)
					{
						ddialog.setFilterPath(jobMeta.environmentSubstitute(wFilename2.getText()) );
					}
					
					 // Calling open() will open and run the dialog.
			        // It will return the selected directory, or
			        // null if user cancels
			        String dir = ddialog.open();
			        if (dir != null) {
			          // Set the text box to the new selection
			        	wFilename2.setText(dir);
			        }
					
				}
			}
		);
		
		// Browse files...
		wbFilename2=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFilename2);
		wbFilename2.setText(BaseMessages.getString(PKG, "JobFoldersCompare.FileBrowse.Label"));
		fdbFilename2=new FormData();
		fdbFilename2.right= new FormAttachment(wbDirectory2, -margin);
		fdbFilename2.top  = new FormAttachment(wFilename1, margin);
		wbFilename2.setLayoutData(fdbFilename2);
		
		wFilename2=new TextVar(jobMeta,shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename2);
		wFilename2.addModifyListener(lsMod);
		fdFilename2=new FormData();
		fdFilename2.left = new FormAttachment(middle, 0);
		fdFilename2.top  = new FormAttachment(wFilename1, margin);
		fdFilename2.right= new FormAttachment(wbFilename2, -margin);
		wFilename2.setLayoutData(fdFilename2);

		// Whenever something changes, set the tooltip to the expanded version:
		wFilename2.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wFilename2.setToolTipText(jobMeta.environmentSubstitute( wFilename2.getText() ) );
				}
			}
		);

		wbFilename2.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] {"*"});
					if (wFilename2.getText()!=null)
					{
						dialog.setFileName(jobMeta.environmentSubstitute(wFilename2.getText()) );
					}
					dialog.setFilterNames(FILETYPES);
					if (dialog.open()!=null)
					{
						wFilename2.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
					}
				}
			}
		);

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wFilename2);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wName.addSelectionListener( lsDef );
		wFilename1.addSelectionListener( lsDef );
		wFilename2.addSelectionListener( lsDef );

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();
		SpecifyCompareOnlyActivate();

		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
	}
	private void SpecifyCompareOnlyActivate()
	{
		wWildcard.setEnabled(wCompareOnly.getSelectionIndex()==3);
		wlWildcard.setEnabled(wCompareOnly.getSelectionIndex()==3);
		
		wCompareFileContent.setEnabled(wCompareOnly.getSelectionIndex()!=2);
		wlCompareFileContent.setEnabled(wCompareOnly.getSelectionIndex()!=2);
		
		wCompareFileSize.setEnabled(wCompareOnly.getSelectionIndex()!=2);
		wlCompareFileSize.setEnabled(wCompareOnly.getSelectionIndex()!=2);
		
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
		
		
		if (jobEntry.getCompareOnly()!= null)
		{
			if(jobEntry.getCompareOnly().equals("only_files"))
				wCompareOnly.select(1);
			else if(jobEntry.getCompareOnly().equals("only_folders"))
				wCompareOnly.select(2);
			else if(jobEntry.getCompareOnly().equals("specify"))
				wCompareOnly.select(3);
			else
				wCompareOnly.select(0);
		}
		else
			wCompareOnly.select(0);
			
		if (jobEntry.getWildcard()!= null) wWildcard.setText( jobEntry.getWildcard() );
		if (jobEntry.getFilename1()!= null) wFilename1.setText( jobEntry.getFilename1() );
		if (jobEntry.getFilename2()!= null) wFilename2.setText( jobEntry.getFilename2() );
		
		wIncludeSubfolders.setSelection(jobEntry.isIncludeSubfolders());
		wCompareFileContent.setSelection(jobEntry.isCompareFileContent());
		wCompareFileSize.setSelection(jobEntry.isCompareFileSize());
		
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
		jobEntry.setIncludeSubfolders(wIncludeSubfolders.getSelection());
		jobEntry.setCompareFileContent(wCompareFileContent.getSelection());
		jobEntry.setCompareFileSize(wCompareFileSize.getSelection());

		
		if(wCompareOnly.getSelectionIndex()==1)
			jobEntry.setCompareOnly("only_files");
		else if(wCompareOnly.getSelectionIndex()==2)
			jobEntry.setCompareOnly("only_folders");
		else if(wCompareOnly.getSelectionIndex()==3)
			jobEntry.setCompareOnly("specify");
		else
			jobEntry.setCompareOnly("all");

		
		jobEntry.setName(wName.getText());
		jobEntry.setWildcard(wWildcard.getText());
		jobEntry.setFilename1(wFilename1.getText());
		jobEntry.setFilename2(wFilename2.getText());
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