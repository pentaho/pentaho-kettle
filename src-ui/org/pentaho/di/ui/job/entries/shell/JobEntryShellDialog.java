/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.ui.job.entries.shell;

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
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.shell.JobEntryShell;
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
 * Dialog that allows you to enter the settings for a Shell job entry.
 * 
 * @author Matt
 * @since 19-06-2003
 * 
 */
public class JobEntryShellDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryShell.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private static final String[] FILEFORMATS = new String[] { BaseMessages.getString(PKG, "JobShell.Fileformat.Scripts"), BaseMessages.getString(PKG, "JobShell.Fileformat.All") };

    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;

    private Label wlFilename;

    private Button wbFilename;

    private TextVar wFilename;

    private FormData fdlFilename, fdbFilename, fdFilename;

    private Label wlWorkDirectory;

    private TextVar wWorkDirectory;

    private FormData fdlWorkDirectory, fdWorkDirectory;    
    
    private Group wLogging;

    private FormData fdLogging;

    private Label wlSetLogfile;

    private Button wSetLogfile;

    private FormData fdlSetLogfile, fdSetLogfile;

    private Label wlLogfile;

    private TextVar wLogfile;

    private FormData fdlLogfile, fdLogfile;

    private Label wlLogext;

    private TextVar wLogext;

    private FormData fdlLogext, fdLogext;

    private Label wlAddDate;

    private Button wAddDate;

    private FormData fdlAddDate, fdAddDate;

    private Label wlAddTime;

    private Button wAddTime;

    private FormData fdlAddTime, fdAddTime;

    private Label wlLoglevel;

    private CCombo wLoglevel;

    private FormData fdlLoglevel, fdLoglevel;

    private Label wlPrevious;

    private Button wPrevious;

    private FormData fdlPrevious, fdPrevious;

    private Label wlEveryRow;

    private Button wEveryRow;

    private FormData fdlEveryRow, fdEveryRow;

    private Label wlFields;

    private TableView wFields;

    private FormData fdlFields, fdFields;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private Shell shell;

    private SelectionAdapter lsDef;

    private JobEntryShell jobEntry;

    private boolean backupChanged, backupLogfile, backupDate, backupTime;
    
    private Label wlAppendLogfile;

    private Button wAppendLogfile,wInsertScript;

    private FormData fdlAppendLogfile, fdAppendLogfile;

    private Display display;
    
	private CTabFolder   wTabFolder;
	
	private Composite    wGeneralComp,wScriptComp;	
	
	private CTabItem     wGeneralTab,wScriptTab;
	
	private FormData     fdTabFolder,fdGeneralComp,fdScriptComp;

    private Label wlScript,wlInsertScript;

    private Text wScript;

    private FormData  fdScript,fdInsertScript,fdlInsertScript;

    public JobEntryShellDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryShell) jobEntryInt;
    }

    public JobEntryInterface open()
    {
        Shell parent = getParent();
        display = parent.getDisplay();

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
        backupChanged = jobEntry.hasChanged();
        backupLogfile = jobEntry.setLogfile;
        backupDate = jobEntry.addDate;
        backupTime = jobEntry.addTime;

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "JobShell.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Name line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(BaseMessages.getString(PKG, "JobShell.Name.Label"));
        props.setLook(wlName);
        fdlName = new FormData();
        fdlName.left = new FormAttachment(0, 0);
        fdlName.top = new FormAttachment(0, 0);
        fdlName.right = new FormAttachment(middle, 0);
        wlName.setLayoutData(fdlName);

        wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wName);
        wName.addModifyListener(lsMod);
        fdName = new FormData();
        fdName.top = new FormAttachment(0, 0);
        fdName.left = new FormAttachment(middle, 0);
        fdName.right = new FormAttachment(100, 0);
        wName.setLayoutData(fdName);
        
        wTabFolder = new CTabFolder(shell, SWT.BORDER);
		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
		
		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
	
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(BaseMessages.getString(PKG, "JobShell.Tab.General.Label"));
	
	
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);			
		props.setLook(wGeneralComp);
	
		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);

        
        // Insert Script?
        wlInsertScript = new Label(wGeneralComp, SWT.RIGHT);
        wlInsertScript.setText(BaseMessages.getString(PKG, "JobShell.InsertScript.Label"));
        props.setLook(wlInsertScript);
        fdlInsertScript = new FormData();
        fdlInsertScript.left = new FormAttachment(0, 0);
        fdlInsertScript.top = new FormAttachment(wName, margin);
        fdlInsertScript.right = new FormAttachment(middle, -margin);
        wlInsertScript.setLayoutData(fdlInsertScript);
        wInsertScript = new Button(wGeneralComp, SWT.CHECK);
        wInsertScript.setToolTipText(BaseMessages.getString(PKG, "JobShell.InsertScript.Tooltip"));
        props.setLook(wInsertScript);
        fdInsertScript = new FormData();
        fdInsertScript.left = new FormAttachment(middle, 0);
        fdInsertScript.top = new FormAttachment(wName, margin);
        fdInsertScript.right = new FormAttachment(100, 0);
        wInsertScript.setLayoutData(fdInsertScript);
        wInsertScript.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	ActiveInsertScript();
                jobEntry.setChanged();
            }
        });


        ///////////////////////
        // Filename line
        ///////////////////////
        wlFilename = new Label(wGeneralComp, SWT.RIGHT);
        wlFilename.setText(BaseMessages.getString(PKG, "JobShell.Filename.Label"));
        props.setLook(wlFilename);
        fdlFilename = new FormData();
        fdlFilename.left = new FormAttachment(0, 0);
        fdlFilename.top = new FormAttachment(wInsertScript, margin);
        fdlFilename.right = new FormAttachment(middle, 0);
        wlFilename.setLayoutData(fdlFilename);

        wbFilename = new Button(wGeneralComp, SWT.PUSH | SWT.CENTER);
        props.setLook(wbFilename);
        wbFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
        fdbFilename = new FormData();
        fdbFilename.top = new FormAttachment(wInsertScript, margin);
        fdbFilename.right = new FormAttachment(100, 0);
        wbFilename.setLayoutData(fdbFilename);

        wFilename = new TextVar(jobMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wFilename);
        wFilename.addModifyListener(lsMod);
        fdFilename = new FormData();
        fdFilename.left = new FormAttachment(middle, 0);
        fdFilename.right = new FormAttachment(wbFilename, -margin);
        fdFilename.top = new FormAttachment(wInsertScript, margin);
        wFilename.setLayoutData(fdFilename);
        
        ///////////////////////
        // Working dir line
        ///////////////////////
        wlWorkDirectory = new Label(wGeneralComp, SWT.RIGHT);
        wlWorkDirectory.setText(BaseMessages.getString(PKG, "JobShell.WorkingDirectory.Label"));
        props.setLook(wlWorkDirectory);
        fdlWorkDirectory = new FormData();
        fdlWorkDirectory.left = new FormAttachment(0, 0);
        fdlWorkDirectory.top = new FormAttachment(wFilename, margin);
        fdlWorkDirectory.right = new FormAttachment(middle, 0);
        wlWorkDirectory.setLayoutData(fdlWorkDirectory);

        wWorkDirectory = new TextVar(jobMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wWorkDirectory);
        wWorkDirectory.addModifyListener(lsMod);
        fdWorkDirectory = new FormData();
        fdWorkDirectory.left = new FormAttachment(middle, 0);
        fdWorkDirectory.right = new FormAttachment(wbFilename, -margin);
        fdWorkDirectory.top = new FormAttachment(wFilename, margin);
        wWorkDirectory.setLayoutData(fdWorkDirectory);        

        // ////////////////////////
        // START OF LOGGING GROUP
        //
        wLogging = new Group(wGeneralComp, SWT.SHADOW_NONE);
        props.setLook(wLogging);
        wLogging.setText(BaseMessages.getString(PKG, "JobShell.LogSettings.Group.Label"));

        FormLayout groupLayout = new FormLayout();
        groupLayout.marginWidth = 10;
        groupLayout.marginHeight = 10;

        wLogging.setLayout(groupLayout);

        // Set the logfile?
        wlSetLogfile = new Label(wLogging, SWT.RIGHT);
        wlSetLogfile.setText(BaseMessages.getString(PKG, "JobShell.Specify.Logfile.Label"));
        props.setLook(wlSetLogfile);
        fdlSetLogfile = new FormData();
        fdlSetLogfile.left = new FormAttachment(0, 0);
        fdlSetLogfile.top = new FormAttachment(0, margin);
        fdlSetLogfile.right = new FormAttachment(middle, -margin);
        wlSetLogfile.setLayoutData(fdlSetLogfile);
        wSetLogfile = new Button(wLogging, SWT.CHECK);
        props.setLook(wSetLogfile);
        fdSetLogfile = new FormData();
        fdSetLogfile.left = new FormAttachment(middle, 0);
        fdSetLogfile.top = new FormAttachment(0, margin);
        fdSetLogfile.right = new FormAttachment(100, 0);
        wSetLogfile.setLayoutData(fdSetLogfile);
        wSetLogfile.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setLogfile = !jobEntry.setLogfile;
                jobEntry.setChanged();
                setActive();
            }
        });
        // Append logfile?
        wlAppendLogfile = new Label(wLogging, SWT.RIGHT);
        wlAppendLogfile.setText(BaseMessages.getString(PKG, "JobShell.Append.Logfile.Label"));
        props.setLook(wlAppendLogfile);
        fdlAppendLogfile = new FormData();
        fdlAppendLogfile.left = new FormAttachment(0, 0);
        fdlAppendLogfile.top = new FormAttachment(wSetLogfile, margin);
        fdlAppendLogfile.right = new FormAttachment(middle, -margin);
        wlAppendLogfile.setLayoutData(fdlAppendLogfile);
        wAppendLogfile = new Button(wLogging, SWT.CHECK);
        wAppendLogfile.setToolTipText(BaseMessages.getString(PKG, "JobShell.Append.Logfile.Tooltip"));
        props.setLook(wAppendLogfile);
        fdAppendLogfile = new FormData();
        fdAppendLogfile.left = new FormAttachment(middle, 0);
        fdAppendLogfile.top = new FormAttachment(wSetLogfile, margin);
        fdAppendLogfile.right = new FormAttachment(100, 0);
        wAppendLogfile.setLayoutData(fdAppendLogfile);
        wAppendLogfile.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            }
        });


        // Set the logfile path + base-name
        wlLogfile = new Label(wLogging, SWT.RIGHT);
        wlLogfile.setText(BaseMessages.getString(PKG, "JobShell.NameOfLogfile.Label"));
        props.setLook(wlLogfile);
        fdlLogfile = new FormData();
        fdlLogfile.left = new FormAttachment(0, 0);
        fdlLogfile.top = new FormAttachment(wAppendLogfile, margin);
        fdlLogfile.right = new FormAttachment(middle, 0);
        wlLogfile.setLayoutData(fdlLogfile);
        wLogfile = new TextVar(jobMeta, wLogging, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wLogfile.setText("");
        props.setLook(wLogfile);
        fdLogfile = new FormData();
        fdLogfile.left = new FormAttachment(middle, 0);
        fdLogfile.top = new FormAttachment(wAppendLogfile, margin);
        fdLogfile.right = new FormAttachment(100, 0);
        wLogfile.setLayoutData(fdLogfile);

        // Set the logfile filename extention
        wlLogext = new Label(wLogging, SWT.RIGHT);
        wlLogext.setText(BaseMessages.getString(PKG, "JobShell.LogfileExtension.Label"));
        props.setLook(wlLogext);
        fdlLogext = new FormData();
        fdlLogext.left = new FormAttachment(0, 0);
        fdlLogext.top = new FormAttachment(wLogfile, margin);
        fdlLogext.right = new FormAttachment(middle, 0);
        wlLogext.setLayoutData(fdlLogext);
        wLogext = new TextVar(jobMeta, wLogging, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wLogext.setText("");
        props.setLook(wLogext);
        fdLogext = new FormData();
        fdLogext.left = new FormAttachment(middle, 0);
        fdLogext.top = new FormAttachment(wLogfile, margin);
        fdLogext.right = new FormAttachment(100, 0);
        wLogext.setLayoutData(fdLogext);

        // Add date to logfile name?
        wlAddDate = new Label(wLogging, SWT.RIGHT);
        wlAddDate.setText(BaseMessages.getString(PKG, "JobShell.Logfile.IncludeDate.Label"));
        props.setLook(wlAddDate);
        fdlAddDate = new FormData();
        fdlAddDate.left = new FormAttachment(0, 0);
        fdlAddDate.top = new FormAttachment(wLogext, margin);
        fdlAddDate.right = new FormAttachment(middle, -margin);
        wlAddDate.setLayoutData(fdlAddDate);
        wAddDate = new Button(wLogging, SWT.CHECK);
        props.setLook(wAddDate);
        fdAddDate = new FormData();
        fdAddDate.left = new FormAttachment(middle, 0);
        fdAddDate.top = new FormAttachment(wLogext, margin);
        fdAddDate.right = new FormAttachment(100, 0);
        wAddDate.setLayoutData(fdAddDate);
        wAddDate.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.addDate = !jobEntry.addDate;
                jobEntry.setChanged();
            }
        });

        // Add time to logfile name?
        wlAddTime = new Label(wLogging, SWT.RIGHT);
        wlAddTime.setText(BaseMessages.getString(PKG, "JobShell.Logfile.IncludeTime.Label"));
        props.setLook(wlAddTime);
        fdlAddTime = new FormData();
        fdlAddTime.left = new FormAttachment(0, 0);
        fdlAddTime.top = new FormAttachment(wAddDate, margin);
        fdlAddTime.right = new FormAttachment(middle, -margin);
        wlAddTime.setLayoutData(fdlAddTime);
        wAddTime = new Button(wLogging, SWT.CHECK);
        props.setLook(wAddTime);
        fdAddTime = new FormData();
        fdAddTime.left = new FormAttachment(middle, 0);
        fdAddTime.top = new FormAttachment(wAddDate, margin);
        fdAddTime.right = new FormAttachment(100, 0);
        wAddTime.setLayoutData(fdAddTime);
        wAddTime.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.addTime = !jobEntry.addTime;
                jobEntry.setChanged();
            }
        });

        wlLoglevel = new Label(wLogging, SWT.RIGHT);
        wlLoglevel.setText(BaseMessages.getString(PKG, "JobShell.Loglevel.Label"));
        props.setLook(wlLoglevel);
        fdlLoglevel = new FormData();
        fdlLoglevel.left = new FormAttachment(0, 0);
        fdlLoglevel.right = new FormAttachment(middle, -margin);
        fdlLoglevel.top = new FormAttachment(wAddTime, margin);
        wlLoglevel.setLayoutData(fdlLoglevel);
        wLoglevel = new CCombo(wLogging, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        wLoglevel.setItems(LogLevel.getLogLevelDescriptions());
        props.setLook(wLoglevel);
        fdLoglevel = new FormData();
        fdLoglevel.left = new FormAttachment(middle, 0);
        fdLoglevel.top = new FormAttachment(wAddTime, margin);
        fdLoglevel.right = new FormAttachment(100, 0);
        wLoglevel.setLayoutData(fdLoglevel);

        fdLogging = new FormData();
        fdLogging.left = new FormAttachment(0, margin);
        fdLogging.top = new FormAttachment(wWorkDirectory, margin);
        fdLogging.right = new FormAttachment(100, -margin);
        wLogging.setLayoutData(fdLogging);
        
        // ///////////////////////////////////////////////////////////
        // / END OF LOGGING GROUP
        // ///////////////////////////////////////////////////////////

        wlPrevious = new Label(wGeneralComp, SWT.RIGHT);
        wlPrevious.setText(BaseMessages.getString(PKG, "JobShell.Previous.Label"));
        props.setLook(wlPrevious);
        fdlPrevious = new FormData();
        fdlPrevious.left = new FormAttachment(0, 0);
        fdlPrevious.top = new FormAttachment(wLogging, margin * 3);
        fdlPrevious.right = new FormAttachment(middle, -margin);
        wlPrevious.setLayoutData(fdlPrevious);
        wPrevious = new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wPrevious);
        wPrevious.setSelection(jobEntry.argFromPrevious);
        wPrevious.setToolTipText(BaseMessages.getString(PKG, "JobShell.Previous.Tooltip"));
        fdPrevious = new FormData();
        fdPrevious.left = new FormAttachment(middle, 0);
        fdPrevious.top = new FormAttachment(wLogging, margin * 3);
        fdPrevious.right = new FormAttachment(100, 0);
        wPrevious.setLayoutData(fdPrevious);
        wPrevious.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.argFromPrevious = !jobEntry.argFromPrevious;
                jobEntry.setChanged();
                wlFields.setEnabled(!jobEntry.argFromPrevious);
                wFields.setEnabled(!jobEntry.argFromPrevious);
            }
        });

        wlEveryRow = new Label(wGeneralComp, SWT.RIGHT);
        wlEveryRow.setText(BaseMessages.getString(PKG, "JobShell.ExecForEveryInputRow.Label"));
        props.setLook(wlEveryRow);
        fdlEveryRow = new FormData();
        fdlEveryRow.left = new FormAttachment(0, 0);
        fdlEveryRow.top = new FormAttachment(wPrevious, margin * 3);
        fdlEveryRow.right = new FormAttachment(middle, -margin);
        wlEveryRow.setLayoutData(fdlEveryRow);
        wEveryRow = new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wEveryRow);
        wEveryRow.setSelection(jobEntry.execPerRow);
        wEveryRow.setToolTipText(BaseMessages.getString(PKG, "JobShell.ExecForEveryInputRow.Tooltip"));
        fdEveryRow = new FormData();
        fdEveryRow.left = new FormAttachment(middle, 0);
        fdEveryRow.top = new FormAttachment(wPrevious, margin * 3);
        fdEveryRow.right = new FormAttachment(100, 0);
        wEveryRow.setLayoutData(fdEveryRow);
        wEveryRow.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.execPerRow = !jobEntry.execPerRow;
                jobEntry.setChanged();
            }
        });

        wlFields = new Label(wGeneralComp, SWT.NONE);
        wlFields.setText(BaseMessages.getString(PKG, "JobShell.Fields.Label"));
        props.setLook(wlFields);
        fdlFields = new FormData();
        fdlFields.left = new FormAttachment(0, 0);
        fdlFields.top = new FormAttachment(wEveryRow, margin);
        wlFields.setLayoutData(fdlFields);

        final int FieldsCols = 1;
        int rows = jobEntry.arguments == null
                                             ? 1
                                             : (jobEntry.arguments.length == 0
                                                                              ? 0
                                                                              : jobEntry.arguments.length);
        final int FieldsRows = rows;

        ColumnInfo[] colinf = new ColumnInfo[FieldsCols];
        colinf[0] = new ColumnInfo(BaseMessages.getString(PKG, "JobShell.Fields.Argument.Label"),
                                   ColumnInfo.COLUMN_TYPE_TEXT, false);
        colinf[0].setUsingVariables(true);

        wFields = new TableView(jobMeta, wGeneralComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf,
                                FieldsRows, lsMod, props);

        fdFields = new FormData();
        fdFields.left = new FormAttachment(0, 0);
        fdFields.top = new FormAttachment(wlFields, margin);
        fdFields.right = new FormAttachment(100, 0);
        fdFields.bottom = new FormAttachment(100, -margin);
        wFields.setLayoutData(fdFields);

        wlFields.setEnabled(!jobEntry.argFromPrevious);
        wFields.setEnabled(!jobEntry.argFromPrevious);
        
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
 		
 		//////////////////////////////////////
		// START OF Script          TAB   ///
		/////////////////////////////////////
		
		
		
		wScriptTab=new CTabItem(wTabFolder, SWT.NONE);
		wScriptTab.setText(BaseMessages.getString(PKG, "JobShell.Tab.Script.Label"));

		FormLayout ScriptLayout = new FormLayout ();
		ScriptLayout.marginWidth  = 3;
		ScriptLayout.marginHeight = 3;
		
		wScriptComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wScriptComp);
 		wScriptComp.setLayout(ScriptLayout);
 		
        // Script line
        
 	   wScript=new Text(wScriptComp, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
       props.setLook(wScript);
       wScript.addModifyListener(lsMod);
       fdScript = new FormData();
       fdScript.left = new FormAttachment(0, margin);
       fdScript.top = new FormAttachment(wlScript, margin);
       fdScript.right = new FormAttachment(100, 0);
       fdScript.bottom = new FormAttachment(100, -margin);
       wScript.setLayoutData(fdScript);

 		
 		
 		
		fdScriptComp = new FormData();
		fdScriptComp.left  = new FormAttachment(0, 0);
		fdScriptComp.top   = new FormAttachment(0, 0);
		fdScriptComp.right = new FormAttachment(100, 0);
		fdScriptComp.bottom= new FormAttachment(100, 0);
		wScriptComp.setLayoutData(wScriptComp);

		wScriptComp.layout();
		wScriptTab.setControl(wScriptComp);


		/////////////////////////////////////////////////////////////
		/// END OF Script TAB
		/////////////////////////////////////////////////////////////
 		
 		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wName, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);


        // Some buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wTabFolder);

        // Add listeners
        lsCancel = new Listener()
        {
            public void handleEvent(Event e)
            {
                cancel();
            }
        };
        lsOK = new Listener()
        {
            public void handleEvent(Event e)
            {
                ok();
            }
        };

        wOK.addListener(SWT.Selection, lsOK);
        wCancel.addListener(SWT.Selection, lsCancel);

        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };
        wName.addSelectionListener(lsDef);
        wFilename.addSelectionListener(lsDef);

        wbFilename.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterExtensions(new String[] { "*.sh;*.bat;*.BAT", "*;*.*" });
                dialog.setFilterNames(FILEFORMATS);

                if (wFilename.getText() != null)
                {
                    dialog.setFileName(wFilename.getText());
                }

                if (dialog.open() != null)
                {
                    wFilename.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
                                      + dialog.getFileName());
                    wName.setText(dialog.getFileName());
                }
            }
        });

        // Detect [X] or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        getData();
        setActive();
        ActiveInsertScript();
        wTabFolder.setSelection(0);

        BaseStepDialog.setSize(shell);

        shell.open();
        props.setDialogSize(shell, "JobShellDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }
    private void ActiveInsertScript()
    {
    	wFilename.setEnabled(!wInsertScript.getSelection());
    	wlFilename.setEnabled(!wInsertScript.getSelection());
    	wbFilename.setEnabled(!wInsertScript.getSelection());
    	wScript.setEnabled(wInsertScript.getSelection());
    	// We can not use arguments !!!
    	if(wInsertScript.getSelection())
    	{
    		wFields.clearAll(false);
    		wFields.setEnabled(false);
    		wlFields.setEnabled(false);
    		wPrevious.setSelection(false);
    		wPrevious.setEnabled(false);
    		wlPrevious.setEnabled(false);
    		wEveryRow.setSelection(false);
    		wEveryRow.setEnabled(false);
    		wlEveryRow.setEnabled(false);
    	}else
    	{
    		wFields.setEnabled(true);	
    		wlFields.setEnabled(true);
    		wPrevious.setEnabled(true);
    		wlPrevious.setEnabled(true);
    		wEveryRow.setEnabled(true);
    		wlEveryRow.setEnabled(true);
    	}
    	
    }
    public void dispose()
    {
        WindowProperty winprop = new WindowProperty(shell);
        props.setScreen(winprop);
        shell.dispose();
    }

    public void setActive()
    {
        wlLogfile.setEnabled(jobEntry.setLogfile);
        wLogfile.setEnabled(jobEntry.setLogfile);

        wlLogext.setEnabled(jobEntry.setLogfile);
        wLogext.setEnabled(jobEntry.setLogfile);

        wlAddDate.setEnabled(jobEntry.setLogfile);
        wAddDate.setEnabled(jobEntry.setLogfile);

        wlAddTime.setEnabled(jobEntry.setLogfile);
        wAddTime.setEnabled(jobEntry.setLogfile);

        wlLoglevel.setEnabled(jobEntry.setLogfile);
        wLoglevel.setEnabled(jobEntry.setLogfile);
        
        wlAppendLogfile.setEnabled(jobEntry.setLogfile);
        wAppendLogfile.setEnabled(jobEntry.setLogfile); 
        
        if (jobEntry.setLogfile)
        {
            wLoglevel.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
        }
        else
        {
            wLoglevel.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
        }
    }

    public void getData()
    {
        if (jobEntry.getName() != null)
            wName.setText(jobEntry.getName());
        if (jobEntry.getFilename() != null)
            wFilename.setText(jobEntry.getFilename());
        if (jobEntry.getWorkDirectory() != null)
            wWorkDirectory.setText(jobEntry.getWorkDirectory());        
        
        if (jobEntry.arguments != null)
        {
            for (int i = 0; i < jobEntry.arguments.length; i++)
            {
                TableItem ti = wFields.table.getItem(i);
                if (jobEntry.arguments[i] != null)
                    ti.setText(1, jobEntry.arguments[i]);
            }
            wFields.setRowNums();
            wFields.optWidth(true);
        }
        wPrevious.setSelection(jobEntry.argFromPrevious);
        wEveryRow.setSelection(jobEntry.execPerRow);
        wSetLogfile.setSelection(jobEntry.setLogfile);
        if (jobEntry.logfile != null)
            wLogfile.setText(jobEntry.logfile);
        if (jobEntry.logext != null)
            wLogext.setText(jobEntry.logext);
        wAddDate.setSelection(jobEntry.addDate);
        wAddTime.setSelection(jobEntry.addTime);
        wAppendLogfile.setSelection(jobEntry.setAppendLogfile);
        if(jobEntry.logFileLevel != null) {
          wLoglevel.select(jobEntry.logFileLevel.getLevel());
        }
        
        wInsertScript.setSelection(jobEntry.insertScript);
        if (jobEntry.getScript() != null)
            wScript.setText(jobEntry.getScript());
    }

    private void cancel()
    {
        jobEntry.setChanged(backupChanged);
        jobEntry.setLogfile = backupLogfile;
        jobEntry.addDate = backupDate;
        jobEntry.addTime = backupTime;

        jobEntry = null;
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
        jobEntry.setFileName(wFilename.getText());
        jobEntry.setName(wName.getText());
        jobEntry.setWorkDirectory(wWorkDirectory.getText());

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

        jobEntry.logfile = wLogfile.getText();
        jobEntry.logext = wLogext.getText();
        if (wLoglevel.getSelectionIndex()>=0) {
            jobEntry.logFileLevel = LogLevel.values()[wLoglevel.getSelectionIndex()];
          } else {
            jobEntry.logFileLevel = LogLevel.BASIC;
          }
        jobEntry.setAppendLogfile = wAppendLogfile.getSelection();
        jobEntry.setScript(wScript.getText());
        jobEntry.insertScript=wInsertScript.getSelection();
        dispose();
    }
}