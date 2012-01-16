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

package org.pentaho.di.ui.job.entries.xslt;

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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.xslt.JobEntryXSLT;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.steps.xslt.XsltMeta;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit the XSLT job entry settings.
 *
 * @author Samatar Hassan
 * @since  02-03-2007
 */
public class JobEntryXSLTDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryXSLT.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

   private static final String[] FILETYPES_XML = new String[] {
           BaseMessages.getString(PKG, "JobEntryXSLT.Filetype.Xml"),
		   BaseMessages.getString(PKG, "JobEntryXSLT.Filetype.All") };

	private static final String[] FILETYPES_XSL = new String[] 
		{
			BaseMessages.getString(PKG, "JobEntryXSLT.Filetype.Xsl"),
			BaseMessages.getString(PKG, "JobEntryXSLT.Filetype.Xslt"),
			BaseMessages.getString(PKG, "JobEntryXSLT.Filetype.All")};


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
	
    private Button wbMovetoDirectory;
    private FormData fdbMovetoDirectory;  

	private Label wlIfFileExists;
	private  CCombo wIfFileExists;
	private FormData fdlIfFileExists, fdIfFileExists;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntryXSLT jobEntry;
	private Shell       	shell;

	private SelectionAdapter lsDef;
	
	private boolean changed;
	
	private Group wFileResult;
    private FormData fdFileResult;
    
	private Group wFiles;
    private FormData fdFiles;
    
    private Label        wlXSLTFactory;
    private CCombo       wXSLTFactory;
    private FormData     fdlXSLTFactory, fdXSLTFactory;
    
	private Label wlPrevious;
	private Button wPrevious;
	private FormData fdlPrevious, fdPrevious;
    
	private Label        wlAddFileToResult;
	private Button       wAddFileToResult;
	private FormData     fdlAddFileToResult, fdAddFileToResult;
	
	private CTabFolder   wTabFolder;
	private Composite    wAdvancedComp,wGeneralComp;	
	private CTabItem     wAdvancedTab,wGeneralTab;
	private FormData	 fdAdvancedComp,fdGeneralComp;
	private FormData     fdTabFolder;
	
	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;
	
	private ColumnInfo[] colinf;
    
	
	private Label wlOutputProperties;
	private FormData fdlOutputProperties;
	
	private TableView wOutputProperties;
	private FormData fdOutputProperties;
	
    public JobEntryXSLTDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryXSLT) jobEntryInt;
        if (this.jobEntry.getName() == null)
			this.jobEntry.setName(BaseMessages.getString(PKG, "JobEntryXSLT.Name.Default"));
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
		shell.setText(BaseMessages.getString(PKG, "JobEntryXSLT.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Name line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(BaseMessages.getString(PKG, "JobEntryXSLT.Name.Label"));
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
		wGeneralTab.setText(BaseMessages.getString(PKG, "JobEntryXSLT.Tab.General.Label"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
		

		 // Files grouping?
	     // ////////////////////////
	     // START OF LOGGING GROUP///
	     // /
	    wFiles = new Group(wGeneralComp, SWT.SHADOW_NONE);
	    props.setLook(wFiles);
	    wFiles.setText(BaseMessages.getString(PKG, "JobEntryXSLT.Files.Group.Label"));

	    FormLayout groupLayout = new FormLayout();
	    groupLayout.marginWidth = 10;
	    groupLayout.marginHeight = 10;

	    wFiles.setLayout(groupLayout);

	    wlPrevious = new Label(wFiles, SWT.RIGHT);
		wlPrevious.setText(BaseMessages.getString(PKG, "JobEntryXSLT.Previous.Label"));
		props.setLook(wlPrevious);
		fdlPrevious = new FormData();
		fdlPrevious.left = new FormAttachment(0, 0);
		fdlPrevious.top = new FormAttachment(wName, margin );
		fdlPrevious.right = new FormAttachment(middle, -margin);
		wlPrevious.setLayoutData(fdlPrevious);
		wPrevious = new Button(wFiles, SWT.CHECK);
		props.setLook(wPrevious);
		wPrevious.setToolTipText(BaseMessages.getString(PKG, "JobEntryXSLT.Previous.ToolTip"));
		fdPrevious = new FormData();
		fdPrevious.left = new FormAttachment(middle, 0);
		fdPrevious.top = new FormAttachment(wName, margin );
		fdPrevious.right = new FormAttachment(100, 0);
		wPrevious.setLayoutData(fdPrevious);
		wPrevious.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{

				RefreshArgFromPrevious();				
				
			}
		});

		// Filename 1 line
		wlxmlFilename=new Label(wFiles, SWT.RIGHT);
		wlxmlFilename.setText(BaseMessages.getString(PKG, "JobEntryXSLT.xmlFilename.Label"));
 		props.setLook(wlxmlFilename);
		fdlxmlFilename=new FormData();
		fdlxmlFilename.left = new FormAttachment(0, 0);
		fdlxmlFilename.top  = new FormAttachment(wPrevious, margin);
		fdlxmlFilename.right= new FormAttachment(middle, -margin);
		wlxmlFilename.setLayoutData(fdlxmlFilename);
		wbxmlFilename=new Button(wFiles, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbxmlFilename);
		wbxmlFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbxmlFilename=new FormData();
		fdbxmlFilename.right= new FormAttachment(100, 0);
		fdbxmlFilename.top  = new FormAttachment(wPrevious, 0);
		wbxmlFilename.setLayoutData(fdbxmlFilename);
		wxmlFilename=new TextVar(jobMeta, wFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wxmlFilename);
		wxmlFilename.addModifyListener(lsMod);
		fdxmlFilename=new FormData();
		fdxmlFilename.left = new FormAttachment(middle, 0);
		fdxmlFilename.top  = new FormAttachment(wPrevious, margin);
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
		wlxslFilename=new Label(wFiles, SWT.RIGHT);
		wlxslFilename.setText(BaseMessages.getString(PKG, "JobEntryXSLT.xslFilename.Label"));
 		props.setLook(wlxslFilename);
		fdlxslFilename=new FormData();
		fdlxslFilename.left = new FormAttachment(0, 0);
		fdlxslFilename.top  = new FormAttachment(wxmlFilename, margin);
		fdlxslFilename.right= new FormAttachment(middle, -margin);
		wlxslFilename.setLayoutData(fdlxslFilename);
		wbxslFilename=new Button(wFiles, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbxslFilename);
		wbxslFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbxslFilename=new FormData();
		fdbxslFilename.right= new FormAttachment(100, 0);
		fdbxslFilename.top  = new FormAttachment(wxmlFilename, 0);
		wbxslFilename.setLayoutData(fdbxslFilename);
		wxslFilename=new TextVar(jobMeta, wFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
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

		
		// Browse Source folders button ...
		wbOutputDirectory=new Button(wFiles, SWT.PUSH| SWT.CENTER);
		props.setLook(wbOutputDirectory);
		wbOutputDirectory.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
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
		wlOutputFilename = new Label(wFiles, SWT.RIGHT);
		wlOutputFilename.setText(BaseMessages.getString(PKG, "JobEntryXSLT.OutputFilename.Label"));
		props.setLook(wlOutputFilename);
		fdlOutputFilename = new FormData();
		fdlOutputFilename.left = new FormAttachment(0, 0);
		fdlOutputFilename.top = new FormAttachment(wxslFilename, margin);
		fdlOutputFilename.right = new FormAttachment(middle, -margin);
		wlOutputFilename.setLayoutData(fdlOutputFilename);
		
        // Browse folders button ...
		wbMovetoDirectory=new Button(wFiles, SWT.PUSH| SWT.CENTER);
		props.setLook(wbMovetoDirectory);
		wbMovetoDirectory.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbMovetoDirectory=new FormData();
		fdbMovetoDirectory.right= new FormAttachment(100, 0);
		fdbMovetoDirectory.top  = new FormAttachment(wxslFilename, margin);
	 	wbMovetoDirectory.setLayoutData(fdbMovetoDirectory);
	    wbMovetoDirectory.addSelectionListener
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
			
		wOutputFilename = new TextVar(jobMeta, wFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wOutputFilename);
		wOutputFilename.addModifyListener(lsMod);
		fdOutputFilename = new FormData();
		fdOutputFilename.left = new FormAttachment(middle, 0);
		fdOutputFilename.top = new FormAttachment(wxslFilename, margin);
		fdOutputFilename.right = new FormAttachment(wbMovetoDirectory, -margin);
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




	     fdFiles = new FormData();
	     fdFiles.left = new FormAttachment(0, margin);
	     fdFiles.top = new FormAttachment(wName, margin);
	     fdFiles.right = new FormAttachment(100, -margin);
	     wFiles.setLayoutData(fdFiles);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Files GROUP
	     // ///////////////////////////////////////////////////////////

		 // fileresult grouping?
	     // ////////////////////////
	     // START OF FILE RESULT GROUP///
	     // /
	    wFileResult = new Group(wGeneralComp, SWT.SHADOW_NONE);
	    props.setLook(wFileResult);
	    wFileResult.setText(BaseMessages.getString(PKG, "JobEntryXSLT.FileResult.Group.Settings.Label"));

	    FormLayout groupFilesResultLayout = new FormLayout();
	    groupFilesResultLayout.marginWidth = 10;
	    groupFilesResultLayout.marginHeight = 10;

	    wFileResult.setLayout(groupFilesResultLayout);
	      
		
		
		
		 // XSLTFactory
       wlXSLTFactory=new Label(wFileResult, SWT.RIGHT);
       wlXSLTFactory.setText(BaseMessages.getString(PKG, "JobEntryXSLT.XSLTFactory.Label"));
       props.setLook(wlXSLTFactory);
       fdlXSLTFactory=new FormData();
       fdlXSLTFactory.left = new FormAttachment(0, 0);
       fdlXSLTFactory.top  = new FormAttachment(wFiles, margin);
       fdlXSLTFactory.right= new FormAttachment(middle, -margin);
       wlXSLTFactory.setLayoutData(fdlXSLTFactory);
       wXSLTFactory=new CCombo(wFileResult, SWT.BORDER | SWT.READ_ONLY);
       wXSLTFactory.setEditable(true);
       props.setLook(wXSLTFactory);
       wXSLTFactory.addModifyListener(lsMod);
       fdXSLTFactory=new FormData();
       fdXSLTFactory.left = new FormAttachment(middle, 0);
       fdXSLTFactory.top  = new FormAttachment(wFiles,margin);
       fdXSLTFactory.right= new FormAttachment(100, 0);
       wXSLTFactory.setLayoutData(fdXSLTFactory);
       wXSLTFactory.add("JAXP");
       wXSLTFactory.add("SAXON");
		

		//IF File Exists
		wlIfFileExists = new Label(wFileResult, SWT.RIGHT);
		wlIfFileExists.setText(BaseMessages.getString(PKG, "JobEntryXSLT.IfFileExists.Label"));
		props.setLook(wlIfFileExists);
		fdlIfFileExists = new FormData();
		fdlIfFileExists.left = new FormAttachment(0, 0);
		fdlIfFileExists.right = new FormAttachment(middle, -margin);
		fdlIfFileExists.top = new FormAttachment(wXSLTFactory, margin);
		wlIfFileExists.setLayoutData(fdlIfFileExists);
		wIfFileExists = new CCombo(wFileResult, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wIfFileExists.add(BaseMessages.getString(PKG, "JobEntryXSLT.Create_NewFile_IfFileExists.Label"));
		wIfFileExists.add(BaseMessages.getString(PKG, "JobEntryXSLT.Do_Nothing_IfFileExists.Label"));
		wIfFileExists.add(BaseMessages.getString(PKG, "JobEntryXSLT.Fail_IfFileExists.Label"));
		wIfFileExists.select(1); // +1: starts at -1

		props.setLook(wIfFileExists);
		fdIfFileExists= new FormData();
		fdIfFileExists.left = new FormAttachment(middle, 0);
		fdIfFileExists.top = new FormAttachment(wXSLTFactory, margin);
		fdIfFileExists.right = new FormAttachment(100, 0);
		wIfFileExists.setLayoutData(fdIfFileExists);

		fdIfFileExists = new FormData();
		fdIfFileExists.left = new FormAttachment(middle, 0);
		fdIfFileExists.top = new FormAttachment(wXSLTFactory, margin);
		fdIfFileExists.right = new FormAttachment(100, 0);
		wIfFileExists.setLayoutData(fdIfFileExists);

		
	  	//Add file to result
		wlAddFileToResult = new Label(wFileResult, SWT.RIGHT);
		wlAddFileToResult.setText(BaseMessages.getString(PKG, "JobEntryXSLT.AddFileToResult.Label"));
		props.setLook(wlAddFileToResult);
		fdlAddFileToResult = new FormData();
		fdlAddFileToResult.left = new FormAttachment(0, 0);
		fdlAddFileToResult.top = new FormAttachment(wIfFileExists, margin);
		fdlAddFileToResult.right = new FormAttachment(middle, -margin);
		wlAddFileToResult.setLayoutData(fdlAddFileToResult);
		wAddFileToResult = new Button(wFileResult, SWT.CHECK);
		props.setLook(wAddFileToResult);
		wAddFileToResult.setToolTipText(BaseMessages.getString(PKG, "JobEntryXSLT.AddFileToResult.Tooltip"));
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
	     fdFileResult.top = new FormAttachment(wFiles, margin);
	     fdFileResult.right = new FormAttachment(100, -margin);
	     wFileResult.setLayoutData(fdFileResult);
	     // ///////////////////////////////////////////////////////////
	     // / END OF FileResult GROUP
	     // ///////////////////////////////////////////////////////////


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
		wAdvancedTab.setText(BaseMessages.getString(PKG, "JobEntryXSLT.Tab.Advanced.Label"));
		
		wAdvancedComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wAdvancedComp);

		FormLayout advancedLayout = new FormLayout();
		advancedLayout.marginWidth  = 3;
		advancedLayout.marginHeight = 3;
		wAdvancedComp.setLayout(advancedLayout);

 		
		
		 // Output properties
		 wlOutputProperties=new Label(wAdvancedComp, SWT.NONE);
	     wlOutputProperties.setText(BaseMessages.getString(PKG, "XsltDialog.OutputProperties.Label")); //$NON-NLS-1$
	     props.setLook(wlOutputProperties);
	     fdlOutputProperties=new FormData();
	     fdlOutputProperties.left = new FormAttachment(0, 0);
	     fdlOutputProperties.top  = new FormAttachment(0, margin);
	     wlOutputProperties.setLayoutData(fdlOutputProperties);

	    final int OutputPropertiesRows=0;//input.getParameterField().length;
		
		 colinf=new ColumnInfo[] { 
		  new ColumnInfo(BaseMessages.getString(PKG, "XsltDialog.ColumnInfo.OutputProperties.Name"),      ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
		  new ColumnInfo(BaseMessages.getString(PKG, "XsltDialog.ColumnInfo.OutputProperties.Value"),  ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
	       };
		 colinf[0].setComboValues(XsltMeta.outputProperties);
		 colinf[1].setUsingVariables(true);
		
		 wOutputProperties=new TableView(jobMeta, wAdvancedComp, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  OutputPropertiesRows,  
							  lsMod,
							  props
							  );
		fdOutputProperties=new FormData();
		fdOutputProperties.left  = new FormAttachment(0, 0);
		fdOutputProperties.top   = new FormAttachment(wlOutputProperties, margin);
		fdOutputProperties.right = new FormAttachment(100, -margin);
		fdOutputProperties.bottom= new FormAttachment(wlOutputProperties, 200);
		wOutputProperties.setLayoutData(fdOutputProperties);
		
		// Parameters
		
		 wlFields=new Label(wAdvancedComp, SWT.NONE);
	     wlFields.setText(BaseMessages.getString(PKG, "XsltDialog.Parameters.Label")); //$NON-NLS-1$
	     props.setLook(wlFields);
	     fdlFields=new FormData();
	     fdlFields.left = new FormAttachment(0, 0);
	     fdlFields.top  = new FormAttachment(wOutputProperties, 2*margin);
	     wlFields.setLayoutData(fdlFields);
	       
		
			
		final int FieldsRows=0;//input.getParameterField().length;
		
		 colinf=new ColumnInfo[] { 
		  new ColumnInfo(BaseMessages.getString(PKG, "XsltDialog.ColumnInfo.Name"),   ColumnInfo.COLUMN_TYPE_TEXT,   false),
		  new ColumnInfo(BaseMessages.getString(PKG, "XsltDialog.ColumnInfo.Parameter"),  ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
	       };
		colinf[1].setUsingVariables(true);
		colinf[0].setUsingVariables(true);
		
		wFields=new TableView(jobMeta, wAdvancedComp, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  FieldsRows,  
							  lsMod,
							  props
							  );
		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, -margin);
		fdFields.bottom= new FormAttachment(100, -margin);
		wFields.setLayoutData(fdFields);
		
 		
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

		wName.addSelectionListener( lsDef );
		wxmlFilename.addSelectionListener( lsDef );
		wxslFilename.addSelectionListener( lsDef );


		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();
		RefreshArgFromPrevious();
		BaseStepDialog.setSize(shell);
	    wTabFolder.setSelection(0);
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
	}
	private void RefreshArgFromPrevious()
	{
		wlxmlFilename.setEnabled(!wPrevious.getSelection());
		wxmlFilename.setEnabled(!wPrevious.getSelection());
		wbxmlFilename.setEnabled(!wPrevious.getSelection());
		wlxslFilename.setEnabled(!wPrevious.getSelection());
		wxslFilename.setEnabled(!wPrevious.getSelection());
		wbxslFilename.setEnabled(!wPrevious.getSelection());
		wlOutputFilename.setEnabled(!wPrevious.getSelection());
		wOutputFilename.setEnabled(!wPrevious.getSelection());		
		wbMovetoDirectory.setEnabled(!wPrevious.getSelection());		
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
		wPrevious.setSelection(jobEntry.isFilenamesFromPrevious());
		if (jobEntry.getXSLTFactory()!= null) 
		{
			wXSLTFactory.setText(jobEntry.getXSLTFactory());
		}
		else
		{
			wXSLTFactory.setText("JAXP");
		}
		if (jobEntry.getParameterName()!=null) {
			for (int i=0;i<jobEntry.getParameterName().length;i++)
			{
				TableItem item = wFields.table.getItem(i);
				item.setText(1, Const.NVL(jobEntry.getParameterField()[i], ""));
				item.setText(2, Const.NVL(jobEntry.getParameterName()[i], ""));
			}
		}
		if (jobEntry.getOutputPropertyName()!=null) {
			for (int i=0;i<jobEntry.getOutputPropertyName().length;i++)
			{
				TableItem item = wOutputProperties.table.getItem(i);
				item.setText(1, Const.NVL(jobEntry.getOutputPropertyName()[i], ""));
				item.setText(2, Const.NVL(jobEntry.getOutputPropertyValue()[i], ""));
			}
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
	   if(Const.isEmpty(wName.getText())) 
         {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setText(BaseMessages.getString(PKG, "System.StepJobEntryNameMissing.Title"));
			mb.setMessage(BaseMessages.getString(PKG, "System.JobEntryNameMissing.Msg"));
			mb.open(); 
			return;
         }
		jobEntry.setName(wName.getText());
		jobEntry.setxmlFilename(wxmlFilename.getText());
		jobEntry.setxslFilename(wxslFilename.getText());
		jobEntry.setoutputFilename(wOutputFilename.getText());
		jobEntry.iffileexists = wIfFileExists.getSelectionIndex();
		jobEntry.setFilenamesFromPrevious(wPrevious.getSelection());
		jobEntry.setAddFileToResult(wAddFileToResult.getSelection());
		jobEntry.setXSLTFactory(wXSLTFactory.getText());

		int nrparams = wFields.nrNonEmpty();
		int nroutputprops = wOutputProperties.nrNonEmpty();
		jobEntry.allocate(nrparams, nroutputprops);

		for (int i=0;i<nrparams;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			jobEntry.getParameterField()[i]       = item.getText(1);
			jobEntry.getParameterName()[i]    = item.getText(2);
		}
		for (int i=0;i<nroutputprops;i++)
		{
			TableItem item = wOutputProperties.getNonEmpty(i);
			jobEntry.getOutputPropertyName()[i]       = item.getText(1);
			jobEntry.getOutputPropertyValue()[i]    = item.getText(2);
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