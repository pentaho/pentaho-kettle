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

package org.pentaho.di.ui.job.entries.xslt;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.DirectoryDialog;
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
import org.pentaho.di.job.entries.xslt.JobEntryXSLT;
import org.pentaho.di.job.entries.xslt.Messages;

/**
 * This dialog allows you to edit the XSLT job entry settings.
 *
 * @author Samatar Hassan
 * @since  02-03-2007
 */
public class JobEntryXSLTDialog extends JobEntryDialog implements JobEntryDialogInterface
{
   private static final String[] FILETYPES_XML = new String[] {
           Messages.getString("JobEntryXSLT.Filetype.Xml"),
		   Messages.getString("JobEntryXSLT.Filetype.All") };

	private static final String[] FILETYPES_XSL = new String[] 
		{
			Messages.getString("JobEntryXSLT.Filetype.Xsl"),
			Messages.getString("JobEntryXSLT.Filetype.Xslt"),
			Messages.getString("JobEntryXSLT.Filetype.All")};


	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlxmlFilename;
	private Button       wbxmlFilename;
	private TextVar      wxmlFilename;
	private FormData     fdlxmlFilename, fdbxmlFilename, fdxmlFilename;

	private Label        wlxslFilename;
	private Button       wbxslFilename;
	private TextVar      wxslFilename;
	private FormData     fdlxslFilename, fdbxslFilename, fdxslFilename;
	
	private Button wbOutputDirectory;
	private FormData     fdbOutputDirectory;

	private Label wlOutputFilename;
	private TextVar wOutputFilename;
	private FormData fdlOutputFilename, fdOutputFilename;

	private Label wlIfFileExists;
	private  CCombo wIfFileExists;
	private FormData fdlIfFileExists, fdIfFileExists;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntryXSLT jobEntry;
	private Shell       	shell;

	private SelectionAdapter lsDef;
	
	private boolean changed;
	
	//  Add File to result
    
	private Group wFileResult;
    private FormData fdFileResult;
    
    private Label        wlXSLTFactory;
    private CCombo       wXSLTFactory;
    private FormData     fdlXSLTFactory, fdXSLTFactory;
    
    
	private Label        wlAddFileToResult;
	private Button       wAddFileToResult;
	private FormData     fdlAddFileToResult, fdAddFileToResult;

    public JobEntryXSLTDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryXSLT) jobEntryInt;
        if (this.jobEntry.getName() == null)
			this.jobEntry.setName(Messages.getString("JobEntryXSLT.Name.Default"));
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
		shell.setText(Messages.getString("JobEntryXSLT.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Name line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(Messages.getString("JobEntryXSLT.Name.Label"));
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

		// Filename 1 line
		wlxmlFilename=new Label(shell, SWT.RIGHT);
		wlxmlFilename.setText(Messages.getString("JobEntryXSLT.xmlFilename.Label"));
 		props.setLook(wlxmlFilename);
		fdlxmlFilename=new FormData();
		fdlxmlFilename.left = new FormAttachment(0, 0);
		fdlxmlFilename.top  = new FormAttachment(wName, margin);
		fdlxmlFilename.right= new FormAttachment(middle, -margin);
		wlxmlFilename.setLayoutData(fdlxmlFilename);
		wbxmlFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbxmlFilename);
		wbxmlFilename.setText(Messages.getString("System.Button.Browse"));
		fdbxmlFilename=new FormData();
		fdbxmlFilename.right= new FormAttachment(100, 0);
		fdbxmlFilename.top  = new FormAttachment(wName, 0);
		wbxmlFilename.setLayoutData(fdbxmlFilename);
		wxmlFilename=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wxmlFilename);
		wxmlFilename.addModifyListener(lsMod);
		fdxmlFilename=new FormData();
		fdxmlFilename.left = new FormAttachment(middle, 0);
		fdxmlFilename.top  = new FormAttachment(wName, margin);
		fdxmlFilename.right= new FormAttachment(wbxmlFilename, -margin);
		wxmlFilename.setLayoutData(fdxmlFilename);

		// Whenever something changes, set the tooltip to the expanded version:
		wxmlFilename.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wxmlFilename.setToolTipText(jobMeta.environmentSubstitute( wxmlFilename.getText() ) );
				}
			}
		);

		wbxmlFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] {"*.xml;*.XML", "*"});
					if (wxmlFilename.getText()!=null)
					{
						dialog.setFileName(jobMeta.environmentSubstitute(wxmlFilename.getText()) );
					}
					dialog.setFilterNames(FILETYPES_XML);
					if (dialog.open()!=null)
					{
						wxmlFilename.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
					}
				}
			}
		);

		// Filename 2 line
		wlxslFilename=new Label(shell, SWT.RIGHT);
		wlxslFilename.setText(Messages.getString("JobEntryXSLT.xslFilename.Label"));
 		props.setLook(wlxslFilename);
		fdlxslFilename=new FormData();
		fdlxslFilename.left = new FormAttachment(0, 0);
		fdlxslFilename.top  = new FormAttachment(wxmlFilename, margin);
		fdlxslFilename.right= new FormAttachment(middle, -margin);
		wlxslFilename.setLayoutData(fdlxslFilename);
		wbxslFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbxslFilename);
		wbxslFilename.setText(Messages.getString("System.Button.Browse"));
		fdbxslFilename=new FormData();
		fdbxslFilename.right= new FormAttachment(100, 0);
		fdbxslFilename.top  = new FormAttachment(wxmlFilename, 0);
		wbxslFilename.setLayoutData(fdbxslFilename);
		wxslFilename=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wxslFilename);
		wxslFilename.addModifyListener(lsMod);
		fdxslFilename=new FormData();
		fdxslFilename.left = new FormAttachment(middle, 0);
		fdxslFilename.top  = new FormAttachment(wxmlFilename, margin);
		fdxslFilename.right= new FormAttachment(wbxslFilename, -margin);
		wxslFilename.setLayoutData(fdxslFilename);

		// Whenever something changes, set the tooltip to the expanded version:
		wxslFilename.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wxslFilename.setToolTipText(jobMeta.environmentSubstitute( wxslFilename.getText() ) );
				}
			}
		);

		wbxslFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] {"*.xsl;*.XSL", "*.xslt;*.XSLT", "*"});
					if (wxslFilename.getText()!=null)
					{
						dialog.setFileName(jobMeta.environmentSubstitute(wxslFilename.getText()) );
					}
					dialog.setFilterNames(FILETYPES_XSL);
					if (dialog.open()!=null)
					{
						wxslFilename.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
					}
				}
			}
		);
	
		
		
		 // XSLTFactory
        wlXSLTFactory=new Label(shell, SWT.RIGHT);
        wlXSLTFactory.setText(Messages.getString("JobEntryXSLT.XSLTFactory.Label"));
        props.setLook(wlXSLTFactory);
        fdlXSLTFactory=new FormData();
        fdlXSLTFactory.left = new FormAttachment(0, 0);
        fdlXSLTFactory.top  = new FormAttachment(wxslFilename, margin);
        fdlXSLTFactory.right= new FormAttachment(middle, -margin);
        wlXSLTFactory.setLayoutData(fdlXSLTFactory);
        wXSLTFactory=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
        wXSLTFactory.setEditable(true);
        props.setLook(wXSLTFactory);
        wXSLTFactory.addModifyListener(lsMod);
        fdXSLTFactory=new FormData();
        fdXSLTFactory.left = new FormAttachment(middle, 0);
        fdXSLTFactory.top  = new FormAttachment(wxslFilename,margin);
        fdXSLTFactory.right= new FormAttachment(100, 0);
        wXSLTFactory.setLayoutData(fdXSLTFactory);
        wXSLTFactory.add("JAXP");
        wXSLTFactory.add("SAXON");
		
		
		// Browse Source folders button ...
		wbOutputDirectory=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbOutputDirectory);
		wbOutputDirectory.setText(Messages.getString("System.Button.Browse"));
		fdbOutputDirectory=new FormData();
		fdbOutputDirectory.right= new FormAttachment(100, 0);
		fdbOutputDirectory.top  = new FormAttachment(wXSLTFactory, margin);
		wbOutputDirectory.setLayoutData(fdbOutputDirectory);
		
		wbOutputDirectory.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wOutputFilename.getText()!=null)
					{
						ddialog.setFilterPath(jobMeta.environmentSubstitute(wOutputFilename.getText()) );
					}
					
					 // Calling open() will open and run the dialog.
			        // It will return the selected directory, or
			        // null if user cancels
			        String dir = ddialog.open();
			        if (dir != null) {
			          // Set the text box to the new selection
			        	wOutputFilename.setText(dir);
			        }
					
				}
			}
		);
		
		
		
		// OutputFilename
		wlOutputFilename = new Label(shell, SWT.RIGHT);
		wlOutputFilename.setText(Messages.getString("JobEntryXSLT.OutputFilename.Label"));
		props.setLook(wlOutputFilename);
		fdlOutputFilename = new FormData();
		fdlOutputFilename.left = new FormAttachment(0, 0);
		fdlOutputFilename.top = new FormAttachment(wXSLTFactory, margin);
		fdlOutputFilename.right = new FormAttachment(middle, -margin);
		wlOutputFilename.setLayoutData(fdlOutputFilename);
		wOutputFilename = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wOutputFilename);
		wOutputFilename.addModifyListener(lsMod);
		fdOutputFilename = new FormData();
		fdOutputFilename.left = new FormAttachment(middle, 0);
		fdOutputFilename.top = new FormAttachment(wXSLTFactory, margin);
		fdOutputFilename.right = new FormAttachment(wbOutputDirectory, -margin);
		wOutputFilename.setLayoutData(fdOutputFilename);



		// Whenever something changes, set the tooltip to the expanded version:
		wOutputFilename.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				wOutputFilename.setToolTipText(jobMeta.environmentSubstitute( wOutputFilename.getText() ) );
			}
		}
			);

		

		//IF File Exists
		wlIfFileExists = new Label(shell, SWT.RIGHT);
		wlIfFileExists.setText(Messages.getString("JobEntryXSLT.IfFileExists.Label"));
		props.setLook(wlIfFileExists);
		fdlIfFileExists = new FormData();
		fdlIfFileExists.left = new FormAttachment(0, 0);
		fdlIfFileExists.right = new FormAttachment(middle, 0);
		fdlIfFileExists.top = new FormAttachment(wOutputFilename, 2*margin);
		wlIfFileExists.setLayoutData(fdlIfFileExists);
		wIfFileExists = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wIfFileExists.add(Messages.getString("JobEntryXSLT.Create_NewFile_IfFileExists.Label"));
		wIfFileExists.add(Messages.getString("JobEntryXSLT.Do_Nothing_IfFileExists.Label"));
		wIfFileExists.add(Messages.getString("JobEntryXSLT.Fail_IfFileExists.Label"));
		wIfFileExists.select(1); // +1: starts at -1

		props.setLook(wIfFileExists);
		fdIfFileExists= new FormData();
		fdIfFileExists.left = new FormAttachment(middle, 0);
		fdIfFileExists.top = new FormAttachment(wOutputFilename, 2*margin);
		fdIfFileExists.right = new FormAttachment(100, 0);
		wIfFileExists.setLayoutData(fdIfFileExists);

		fdIfFileExists = new FormData();
		fdIfFileExists.left = new FormAttachment(middle, 0);
		fdIfFileExists.top = new FormAttachment(wOutputFilename, 2*margin);
		fdIfFileExists.right = new FormAttachment(100, 0);
		wIfFileExists.setLayoutData(fdIfFileExists);


		 // fileresult grouping?
	     // ////////////////////////
	     // START OF LOGGING GROUP///
	     // /
	    wFileResult = new Group(shell, SWT.SHADOW_NONE);
	    props.setLook(wFileResult);
	    wFileResult.setText(Messages.getString("JobEntryXSLT.FileResult.Group.Label"));

	    FormLayout groupLayout = new FormLayout();
	    groupLayout.marginWidth = 10;
	    groupLayout.marginHeight = 10;

	    wFileResult.setLayout(groupLayout);
	      
	      
	  	//Add file to result
		wlAddFileToResult = new Label(wFileResult, SWT.RIGHT);
		wlAddFileToResult.setText(Messages.getString("JobEntryXSLT.AddFileToResult.Label"));
		props.setLook(wlAddFileToResult);
		fdlAddFileToResult = new FormData();
		fdlAddFileToResult.left = new FormAttachment(0, 0);
		fdlAddFileToResult.top = new FormAttachment(wIfFileExists, margin);
		fdlAddFileToResult.right = new FormAttachment(middle, -margin);
		wlAddFileToResult.setLayoutData(fdlAddFileToResult);
		wAddFileToResult = new Button(wFileResult, SWT.CHECK);
		props.setLook(wAddFileToResult);
		wAddFileToResult.setToolTipText(Messages.getString("JobEntryXSLT.AddFileToResult.Tooltip"));
		fdAddFileToResult = new FormData();
		fdAddFileToResult.left = new FormAttachment(middle, 0);
		fdAddFileToResult.top = new FormAttachment(wIfFileExists, margin);
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
	     fdFileResult.top = new FormAttachment(wIfFileExists, margin);
	     fdFileResult.right = new FormAttachment(100, -margin);
	     wFileResult.setLayoutData(fdFileResult);
	     // ///////////////////////////////////////////////////////////
	     // / END OF FileResult GROUP
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
		wxmlFilename.addSelectionListener( lsDef );
		wxslFilename.addSelectionListener( lsDef );


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
		if (jobEntry.getxmlFilename()!= null) wxmlFilename.setText( jobEntry.getxmlFilename() );
		if (jobEntry.getxslFilename()!= null) wxslFilename.setText( jobEntry.getxslFilename() );
		if (jobEntry.getoutputFilename()!= null) wOutputFilename.setText( jobEntry.getoutputFilename() );
		
		if (jobEntry.iffileexists>=0) 
		{
			wIfFileExists.select(jobEntry.iffileexists );
		}
		else
		{
			wIfFileExists.select(2); // NOTHING
		}
		
		wAddFileToResult.setSelection(jobEntry.isAddFileToResult());
		if (jobEntry.getXSLTFactory()!= null) 
		{
			wXSLTFactory.setText(jobEntry.getXSLTFactory());
		}
		else
		{
			wXSLTFactory.setText("JAXP");
		}

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
		jobEntry.setxmlFilename(wxmlFilename.getText());
		jobEntry.setxslFilename(wxslFilename.getText());
		jobEntry.setoutputFilename(wOutputFilename.getText());
		jobEntry.iffileexists = wIfFileExists.getSelectionIndex();
		
		jobEntry.setAddFileToResult(wAddFileToResult.getSelection());
		jobEntry.setXSLTFactory(wXSLTFactory.getText());

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