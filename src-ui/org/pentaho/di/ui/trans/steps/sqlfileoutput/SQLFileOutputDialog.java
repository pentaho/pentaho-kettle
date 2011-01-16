 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

 
/*
 * Created on 18-mei-2003
 *
 */


package org.pentaho.di.ui.trans.steps.sqlfileoutput;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
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
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.sqlfileoutput.SQLFileOutputMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class SQLFileOutputDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = SQLFileOutputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CCombo       wConnection;

    private Label        wlSchema;
    private TextVar      wSchema;
    private FormData     fdlSchema, fdSchema;

	private Label        wlTable;
	private Button       wbTable;
	private TextVar      wTable;
	private FormData     fdlTable, fdbTable, fdTable;


	private Label        wlTruncate;
	private Button       wTruncate;
	private FormData     fdlTruncate, fdTruncate;
	
	private Label        wlStartNewLine;
	private Button       wStartNewLine;
	private FormData     fdlStartNewLine, fdStartNewLine;
	
	private Label        wlAddToResult;
	private Button       wAddToResult;
	private FormData     fdlAddToResult, fdAddToResult;
	
	private Label        wlAddCreate;
	private Button       wAddCreate;
	private FormData     fdlAddCreate, fdAddCreate;
	
	private Group wGConnection,wFileName;
	private FormData fdGConnection, fdFileName;
	
	private Label        wlFilename;
	private Button       wbFilename;
	private TextVar      wFilename;
	private FormData     fdlFilename, fdbFilename, fdFilename;
	
	private Label        wlExtension;
	private TextVar      wExtension;
	private FormData     fdlExtension, fdExtension;

	private Label        wlAddStepnr;
	private Button       wAddStepnr;
	private FormData     fdlAddStepnr, fdAddStepnr;


	private Label        wlAddDate;
	private Button       wAddDate;
	private FormData     fdlAddDate, fdAddDate;

	private Label        wlAddTime;
	private Button       wAddTime;
	private FormData     fdlAddTime, fdAddTime;
	
	private Button       wbShowFiles;
	private FormData     fdbShowFiles;

	private Label        wlAppend;
	private Button       wAppend;
	private FormData     fdlAppend, fdAppend;
	
	private Label        wlSplitEvery;
	private Text          wSplitEvery;
	private FormData     fdlSplitEvery, fdSplitEvery;
	
    private Label        wlEncoding;
    private CCombo       wEncoding;
    private FormData     fdlEncoding, fdEncoding;
    
    private Label        wlFormat;
    private CCombo       wFormat;
    private FormData     fdlFormat, fdFormat;
    
    
    private boolean      gotEncodings = false; 
    
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;

	private CTabItem     wGeneralTab,wContentTab;
	private Composite    wGeneralComp,wContentComp;
	private FormData     fdGeneralComp,fdContentComp;
	
	private Label        wlCreateParentFolder;
	private Button       wCreateParentFolder;
	private FormData     fdlCreateParentFolder, fdCreateParentFolder;
	
	private Label        wlDoNotOpenNewFileInit;
	private Button       wDoNotOpenNewFileInit;
	private FormData     fdlDoNotOpenNewFileInit, fdDoNotOpenNewFileInit;
	
    private SQLFileOutputMeta input;
	
    
	public SQLFileOutputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(SQLFileOutputMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		backupChanged = input.hasChanged();
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.DialogTitle"));
		
		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
		
		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		
		
		
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.GeneralTab.TabTitle"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
		
		
		// Connection grouping?
		// ////////////////////////
		// START OF Connection GROUP
		// 

		wGConnection = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wGConnection);
		wGConnection.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.Group.ConnectionInfos.Label"));
		
		FormLayout groupLayout = new FormLayout();
		groupLayout.marginWidth = 10;
		groupLayout.marginHeight = 10;
		wGConnection.setLayout(groupLayout);

		// Connection line
		wConnection = addConnectionLine(wGConnection, wStepname, middle, margin);
		if (input.getDatabaseMeta()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);
		
			

		
        // Schema line...
        wlSchema=new Label(wGConnection, SWT.RIGHT);
        wlSchema.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.TargetSchema.Label")); //$NON-NLS-1$
        props.setLook(wlSchema);
        fdlSchema=new FormData();
        fdlSchema.left = new FormAttachment(0, 0);
        fdlSchema.right= new FormAttachment(middle, -margin);
        fdlSchema.top  = new FormAttachment(wConnection, margin);
        wlSchema.setLayoutData(fdlSchema);

        wSchema=new TextVar(transMeta, wGConnection, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wSchema);
        wSchema.addModifyListener(lsMod);
        wSchema.setToolTipText(BaseMessages.getString(PKG, "SQLFileOutputDialog.TargetSchema.Tooltip"));
        fdSchema=new FormData();
        fdSchema.left = new FormAttachment(middle, 0);
        fdSchema.top  = new FormAttachment(wConnection, margin);
        fdSchema.right= new FormAttachment(100, 0);
        wSchema.setLayoutData(fdSchema);

		// Table line...
		wlTable=new Label(wGConnection, SWT.RIGHT);
		wlTable.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.TargetTable.Label"));
 		props.setLook(wlTable);
		fdlTable=new FormData();
		fdlTable.left = new FormAttachment(0, 0);
		fdlTable.right= new FormAttachment(middle, -margin);
		fdlTable.top  = new FormAttachment(wSchema, margin);
		wlTable.setLayoutData(fdlTable);

		wbTable=new Button(wGConnection, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbTable=new FormData();
		fdbTable.right= new FormAttachment(100, 0);
		fdbTable.top  = new FormAttachment(wSchema, margin);
		wbTable.setLayoutData(fdbTable);

		wTable=new TextVar(transMeta, wGConnection, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTable);
 		wTable.setToolTipText(BaseMessages.getString(PKG, "SQLFileOutputDialog.TargetTable.Tooltip"));
		wTable.addModifyListener(lsMod);
		fdTable=new FormData();
		fdTable.top  = new FormAttachment(wSchema, margin);
		fdTable.left = new FormAttachment(middle, 0);
		fdTable.right= new FormAttachment(wbTable, -margin);
		wTable.setLayoutData(fdTable);
		
		fdGConnection = new FormData();
		fdGConnection.left = new FormAttachment(0, margin);
		fdGConnection.top = new FormAttachment(wStepname, margin);
		fdGConnection.right = new FormAttachment(100, -margin);
		wGConnection.setLayoutData(fdGConnection);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Connection GROUP
		// ///////////////////////////////////////////////////////////
        
		
		// Connection grouping?
		// ////////////////////////
		// START OF FileName GROUP
		// 

		wFileName = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wFileName);
		wFileName.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.Group.File.Label"));
		
		FormLayout groupFileLayout = new FormLayout();
		groupFileLayout.marginWidth = 10;
		groupFileLayout.marginHeight = 10;
		wFileName.setLayout(groupFileLayout);
		

		// Add Create table
		wlAddCreate=new Label(wFileName, SWT.RIGHT);
		wlAddCreate.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.CreateTable.Label"));
 		props.setLook(wlAddCreate);
		fdlAddCreate=new FormData();
		fdlAddCreate.left  = new FormAttachment(0, 0);
		fdlAddCreate.top   = new FormAttachment(wGConnection, margin);
		fdlAddCreate.right = new FormAttachment(middle, -margin);
		wlAddCreate.setLayoutData(fdlAddCreate);
		wAddCreate=new Button(wFileName, SWT.CHECK);
		wAddCreate.setToolTipText(BaseMessages.getString(PKG, "SQLFileOutputDialog.CreateTable.Tooltip"));
 		props.setLook(wAddCreate);
		fdAddCreate=new FormData();
		fdAddCreate.left  = new FormAttachment(middle, 0);
		fdAddCreate.top   = new FormAttachment(wGConnection, margin);
		fdAddCreate.right = new FormAttachment(100, 0);
		wAddCreate.setLayoutData(fdAddCreate);
		SelectionAdapter lsSelMod = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	ActiveTruncate();
            	input.setChanged();
            }
        };
        wAddCreate.addSelectionListener(lsSelMod);
		
		
		// Truncate table
		wlTruncate=new Label(wFileName, SWT.RIGHT);
		wlTruncate.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.TruncateTable.Label"));
 		props.setLook(wlTruncate);
		fdlTruncate=new FormData();
		fdlTruncate.left  = new FormAttachment(0, 0);
		fdlTruncate.top   = new FormAttachment(wAddCreate, margin);
		fdlTruncate.right = new FormAttachment(middle, -margin);
		wlTruncate.setLayoutData(fdlTruncate);
		wTruncate=new Button(wFileName, SWT.CHECK);
		wTruncate.setToolTipText(BaseMessages.getString(PKG, "SQLFileOutputDialog.TruncateTable.Tooltip"));
 		props.setLook(wTruncate);
		fdTruncate=new FormData();
		fdTruncate.left  = new FormAttachment(middle, 0);
		fdTruncate.top   = new FormAttachment(wAddCreate, margin);
		fdTruncate.right = new FormAttachment(100, 0);
		wTruncate.setLayoutData(fdTruncate);
		SelectionAdapter lsSelTMod = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                input.setChanged();
            }
        };
		wTruncate.addSelectionListener(lsSelTMod);

	
		// Start New Line For each statement
		wlStartNewLine=new Label(wFileName, SWT.RIGHT);
		wlStartNewLine.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.StartNewLine.Label"));
 		props.setLook(wlStartNewLine);
		fdlStartNewLine=new FormData();
		fdlStartNewLine.left  = new FormAttachment(0, 0);
		fdlStartNewLine.top   = new FormAttachment(wTruncate, margin);
		fdlStartNewLine.right = new FormAttachment(middle, -margin);
		wlStartNewLine.setLayoutData(fdlStartNewLine);
		wStartNewLine=new Button(wFileName, SWT.CHECK);
		wStartNewLine.setToolTipText(BaseMessages.getString(PKG, "SQLFileOutputDialog.StartNewLine.Label"));
 		props.setLook(wStartNewLine);
		fdStartNewLine=new FormData();
		fdStartNewLine.left  = new FormAttachment(middle, 0);
		fdStartNewLine.top   = new FormAttachment(wTruncate, margin);
		fdStartNewLine.right = new FormAttachment(100, 0);
		wStartNewLine.setLayoutData(fdStartNewLine);
		SelectionAdapter lsSelSMod = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                input.setChanged();
            }
        };
        wStartNewLine.addSelectionListener(lsSelSMod);

	
		
		
		// Filename line
		wlFilename=new Label(wFileName, SWT.RIGHT);
		wlFilename.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.Filename.Label"));
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(wStartNewLine, margin);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbFilename=new Button(wFileName, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFilename);
		wbFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(wStartNewLine, 0);
		wbFilename.setLayoutData(fdbFilename);

		wFilename=new TextVar(transMeta, wFileName, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.top  = new FormAttachment(wStartNewLine, margin);
		fdFilename.right= new FormAttachment(wbFilename, -margin);
		wFilename.setLayoutData(fdFilename);
		
		// Create Parent Folder
		wlCreateParentFolder=new Label(wFileName, SWT.RIGHT);
		wlCreateParentFolder.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.CreateParentFolder.Label"));
 		props.setLook(wlCreateParentFolder);
		fdlCreateParentFolder=new FormData();
		fdlCreateParentFolder.left = new FormAttachment(0, 0);
		fdlCreateParentFolder.top  = new FormAttachment(wFilename, margin);
		fdlCreateParentFolder.right= new FormAttachment(middle, -margin);
		wlCreateParentFolder.setLayoutData(fdlCreateParentFolder);
		wCreateParentFolder=new Button(wFileName, SWT.CHECK );
		wCreateParentFolder.setToolTipText(BaseMessages.getString(PKG, "SQLFileOutputDialog.CreateParentFolder.Tooltip"));
 		props.setLook(wCreateParentFolder);
		fdCreateParentFolder=new FormData();
		fdCreateParentFolder.left = new FormAttachment(middle, 0);
		fdCreateParentFolder.top  = new FormAttachment(wFilename, margin);
		fdCreateParentFolder.right= new FormAttachment(100, 0);
		wCreateParentFolder.setLayoutData(fdCreateParentFolder);
		wCreateParentFolder.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);
		

		// Open new File at Init
		wlDoNotOpenNewFileInit=new Label(wFileName, SWT.RIGHT);
		wlDoNotOpenNewFileInit.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.DoNotOpenNewFileInit.Label"));
 		props.setLook(wlDoNotOpenNewFileInit);
		fdlDoNotOpenNewFileInit=new FormData();
		fdlDoNotOpenNewFileInit.left = new FormAttachment(0, 0);
		fdlDoNotOpenNewFileInit.top  = new FormAttachment(wCreateParentFolder, margin);
		fdlDoNotOpenNewFileInit.right= new FormAttachment(middle, -margin);
		wlDoNotOpenNewFileInit.setLayoutData(fdlDoNotOpenNewFileInit);
		wDoNotOpenNewFileInit=new Button(wFileName, SWT.CHECK );
		wDoNotOpenNewFileInit.setToolTipText(BaseMessages.getString(PKG, "SQLFileOutputDialog.DoNotOpenNewFileInit.Tooltip"));
 		props.setLook(wDoNotOpenNewFileInit);
		fdDoNotOpenNewFileInit=new FormData();
		fdDoNotOpenNewFileInit.left = new FormAttachment(middle, 0);
		fdDoNotOpenNewFileInit.top  = new FormAttachment(wCreateParentFolder, margin);
		fdDoNotOpenNewFileInit.right= new FormAttachment(100, 0);
		wDoNotOpenNewFileInit.setLayoutData(fdDoNotOpenNewFileInit);
		wDoNotOpenNewFileInit.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);
		
		
		// Extension line
		wlExtension=new Label(wFileName, SWT.RIGHT);
		wlExtension.setText(BaseMessages.getString(PKG, "System.Label.Extension"));
 		props.setLook(wlExtension);
		fdlExtension=new FormData();
		fdlExtension.left = new FormAttachment(0, 0);
		fdlExtension.top  = new FormAttachment(wDoNotOpenNewFileInit, margin);
		fdlExtension.right= new FormAttachment(middle, -margin);
		wlExtension.setLayoutData(fdlExtension);
		
		wExtension=new TextVar(transMeta, wFileName, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wExtension);
 		wExtension.addModifyListener(lsMod);
 		fdExtension=new FormData();
		fdExtension.left = new FormAttachment(middle, 0);
		fdExtension.top  = new FormAttachment(wDoNotOpenNewFileInit, margin);
		fdExtension.right= new FormAttachment(100, -margin);
		wExtension.setLayoutData(fdExtension);
		
		// Create multi-part file?
		wlAddStepnr=new Label(wFileName, SWT.RIGHT);
		wlAddStepnr.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.AddStepnr.Label"));
 		props.setLook(wlAddStepnr);
		fdlAddStepnr=new FormData();
		fdlAddStepnr.left = new FormAttachment(0, 0);
		fdlAddStepnr.top  = new FormAttachment(wExtension, 2*margin);
		fdlAddStepnr.right= new FormAttachment(middle, -margin);
		wlAddStepnr.setLayoutData(fdlAddStepnr);
		wAddStepnr=new Button(wFileName, SWT.CHECK);
 		props.setLook(wAddStepnr);
		fdAddStepnr=new FormData();
		fdAddStepnr.left = new FormAttachment(middle, 0);
		fdAddStepnr.top  = new FormAttachment(wExtension, 2*margin);
		fdAddStepnr.right= new FormAttachment(100, 0);
		wAddStepnr.setLayoutData(fdAddStepnr);
		wAddStepnr.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);

	
		// Create multi-part file?
		wlAddDate=new Label(wFileName, SWT.RIGHT);
		wlAddDate.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.AddDate.Label"));
 		props.setLook(wlAddDate);
		fdlAddDate=new FormData();
		fdlAddDate.left = new FormAttachment(0, 0);
		fdlAddDate.top  = new FormAttachment(wAddStepnr, margin);
		fdlAddDate.right= new FormAttachment(middle, -margin);
		wlAddDate.setLayoutData(fdlAddDate);
		wAddDate=new Button(wFileName, SWT.CHECK);
 		props.setLook(wAddDate);
		fdAddDate=new FormData();
		fdAddDate.left = new FormAttachment(middle, 0);
		fdAddDate.top  = new FormAttachment(wAddStepnr, margin);
		fdAddDate.right= new FormAttachment(100, 0);
		wAddDate.setLayoutData(fdAddDate);
		wAddDate.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);
		// Create multi-part file?
		wlAddTime=new Label(wFileName, SWT.RIGHT);
		wlAddTime.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.AddTime.Label"));
 		props.setLook(wlAddTime);
		fdlAddTime=new FormData();
		fdlAddTime.left = new FormAttachment(0, 0);
		fdlAddTime.top  = new FormAttachment(wAddDate, margin);
		fdlAddTime.right= new FormAttachment(middle, -margin);
		wlAddTime.setLayoutData(fdlAddTime);
		wAddTime=new Button(wFileName, SWT.CHECK);
 		props.setLook(wAddTime);
		fdAddTime=new FormData();
		fdAddTime.left = new FormAttachment(middle, 0);
		fdAddTime.top  = new FormAttachment(wAddDate, margin);
		fdAddTime.right= new FormAttachment(100, 0);
		wAddTime.setLayoutData(fdAddTime);
		wAddTime.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);
		
		
		// Append to end of file?
		wlAppend=new Label(wFileName, SWT.RIGHT);
		wlAppend.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.Append.Label"));
 		props.setLook(wlAppend);
		fdlAppend=new FormData();
		fdlAppend.left = new FormAttachment(0, 0);
		fdlAppend.top  = new FormAttachment(wAddTime, margin);
		fdlAppend.right= new FormAttachment(middle, -margin);
		wlAppend.setLayoutData(fdlAppend);
		wAppend=new Button(wFileName, SWT.CHECK);
		wAppend.setToolTipText(BaseMessages.getString(PKG, "SQLFileOutputDialog.Append.Tooltip"));
 		props.setLook(wAppend);
		fdAppend=new FormData();
		fdAppend.left = new FormAttachment(middle, 0);
		fdAppend.top  = new FormAttachment(wAddTime, margin);
		fdAppend.right= new FormAttachment(100, 0);
		wAppend.setLayoutData(fdAppend);
		wAppend.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);
		
		
		
		
		wlSplitEvery=new Label(wFileName, SWT.RIGHT);
		wlSplitEvery.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.SplitEvery.Label"));
 		props.setLook(wlSplitEvery);
		fdlSplitEvery=new FormData();
		fdlSplitEvery.left = new FormAttachment(0, 0);
		fdlSplitEvery.top  = new FormAttachment(wAppend, margin);
		fdlSplitEvery.right= new FormAttachment(middle, -margin);
		wlSplitEvery.setLayoutData(fdlSplitEvery);
		
		wSplitEvery=new Text(wFileName, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wSplitEvery);
		wSplitEvery.addModifyListener(lsMod);
		fdSplitEvery=new FormData();
		fdSplitEvery.left = new FormAttachment(middle, 0);
		fdSplitEvery.top  = new FormAttachment(wAppend, margin);
		fdSplitEvery.right= new FormAttachment(100, 0);
		wSplitEvery.setLayoutData(fdSplitEvery);

		wbShowFiles=new Button(wFileName, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbShowFiles);
		wbShowFiles.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.ShowFiles.Button"));
		fdbShowFiles=new FormData();
		fdbShowFiles.left = new FormAttachment(middle, 0);
		fdbShowFiles.top  = new FormAttachment(wSplitEvery, margin*2);
		wbShowFiles.setLayoutData(fdbShowFiles);
		wbShowFiles.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					SQLFileOutputMeta tfoi = new SQLFileOutputMeta();
					getInfo(tfoi);
					String files[] = tfoi.getFiles(transMeta.environmentSubstitute(wFilename.getText()));
					if (files!=null && files.length>0)
					{
						EnterSelectionDialog esd = new EnterSelectionDialog(shell, files, BaseMessages.getString(PKG, "SQLFileOutputDialog.SelectOutputFiles.DialogTitle"), BaseMessages.getString(PKG, "SQLFileOutputDialog.SelectOutputFiles.DialogMessage"));
						esd.setViewOnly();
						esd.open();
					}
					else
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setMessage(BaseMessages.getString(PKG, "SQLFileOutputDialog.NoFilesFound.DialogMessage"));
						mb.setText(BaseMessages.getString(PKG, "System.DialogTitle.Error"));
						mb.open(); 
					}
				}
			}
		);
		

		
		// Add File to the result files name
		wlAddToResult=new Label(wFileName, SWT.RIGHT);
		wlAddToResult.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.AddFileToResult.Label"));
		props.setLook(wlAddToResult);
		fdlAddToResult=new FormData();
		fdlAddToResult.left  = new FormAttachment(0, 0);
		fdlAddToResult.top   = new FormAttachment(wbShowFiles, margin);
		fdlAddToResult.right = new FormAttachment(middle, -margin);
		wlAddToResult.setLayoutData(fdlAddToResult);
		wAddToResult=new Button(wFileName, SWT.CHECK);
		wAddToResult.setToolTipText(BaseMessages.getString(PKG, "SQLFileOutputDialog.AddFileToResult.Tooltip"));
 		props.setLook(wAddToResult);
		fdAddToResult=new FormData();
		fdAddToResult.left  = new FormAttachment(middle, 0);
		fdAddToResult.top   = new FormAttachment(wbShowFiles, margin);
		fdAddToResult.right = new FormAttachment(100, 0);
		wAddToResult.setLayoutData(fdAddToResult);
		SelectionAdapter lsSelR = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                input.setChanged();
            }
        };
		wAddToResult.addSelectionListener(lsSelR);

	
		
 		
 		

		
		
		fdFileName = new FormData();
		fdFileName.left = new FormAttachment(0, margin);
		fdFileName.top = new FormAttachment(wGConnection, 2*margin);
		fdFileName.right = new FormAttachment(100, -margin);
		wFileName.setLayoutData(fdFileName);
		
		// ///////////////////////////////////////////////////////////
		// / END OF FileName GROUP
		// ///////////////////////////////////////////////////////////
        
		
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

		//////////////////////////
		// START OF CONTENT TAB///
		///
		wContentTab=new CTabItem(wTabFolder, SWT.NONE);
		wContentTab.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.ContentTab.TabTitle"));

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		wContentComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wContentComp);
 		wContentComp.setLayout(contentLayout);
		
 		

		//	Prepare a list of possible formats...
		String dats[] = Const.getDateFormats();
		
 		// format
		wlFormat=new Label(wContentComp, SWT.RIGHT);
        wlFormat.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.DateFormat.Label"));
        props.setLook(wlFormat);
        fdlFormat=new FormData();
        fdlFormat.left = new FormAttachment(0, 0);
        fdlFormat.top  = new FormAttachment(0, margin);
        fdlFormat.right= new FormAttachment(middle, -margin);
        wlFormat.setLayoutData(fdlFormat);
        wFormat=new CCombo(wContentComp, SWT.BORDER | SWT.READ_ONLY);
        wFormat.setEditable(true);
        props.setLook(wFormat);
        wFormat.addModifyListener(lsMod);
        fdFormat=new FormData();
        fdFormat.left = new FormAttachment(middle, 0);
        fdFormat.top  = new FormAttachment(0, margin);
        fdFormat.right= new FormAttachment(100, 0);
        wFormat.setLayoutData(fdFormat);
        
        
        
        for (int x=0;x<dats.length;x++) wFormat.add(dats[x]);
        
        
 		
 		
 		// Encoding
		wlEncoding=new Label(wContentComp, SWT.RIGHT);
        wlEncoding.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.Encoding.Label"));
        props.setLook(wlEncoding);
        fdlEncoding=new FormData();
        fdlEncoding.left = new FormAttachment(0, 0);
        fdlEncoding.top  = new FormAttachment(wFormat, margin);
        fdlEncoding.right= new FormAttachment(middle, -margin);
        wlEncoding.setLayoutData(fdlEncoding);
        wEncoding=new CCombo(wContentComp, SWT.BORDER | SWT.READ_ONLY);
        wEncoding.setEditable(true);
        props.setLook(wEncoding);
        wEncoding.addModifyListener(lsMod);
        fdEncoding=new FormData();
        fdEncoding.left = new FormAttachment(middle, 0);
        fdEncoding.top  = new FormAttachment(wFormat, margin);
        fdEncoding.right= new FormAttachment(100, 0);
        wEncoding.setLayoutData(fdEncoding);
        wEncoding.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    setEncodings();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );
		
 		
 		
 		
 		fdContentComp = new FormData();
 		fdContentComp.left  = new FormAttachment(0, 0);
 		fdContentComp.top   = new FormAttachment(0, 0);
 		fdContentComp.right = new FormAttachment(100, 0);
 		fdContentComp.bottom= new FormAttachment(100, 0);
 		wContentComp.setLayoutData(wContentComp);

		wContentComp.layout();
		wContentTab.setControl(wContentComp);


		/////////////////////////////////////////////////////////////
		/// END OF CONTENT TAB
		/////////////////////////////////////////////////////////////


		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
	

		
 		
 		
 		
		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCreate=new Button(shell, SWT.PUSH);
		wCreate.setText(BaseMessages.getString(PKG, "System.Button.SQL"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
		
		setButtonPositions(new Button[] { wOK, wCreate, wCancel }, margin, wTabFolder);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsCreate   = new Listener() { public void handleEvent(Event e) { sql(); } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCreate.addListener(SWT.Selection, lsCreate);
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
        wSchema.addSelectionListener( lsDef );
		wTable.addSelectionListener( lsDef );
        
		wbTable.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					getTableName();
				}
			}
		);
		wbFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					FileDialog dialog = new FileDialog(shell, SWT.SAVE);
					dialog.setFilterExtensions(new String[] {"*.txt", "*.TXT", "*"});
					if (wFilename.getText()!=null)
					{
						dialog.setFileName(transMeta.environmentSubstitute(wFilename.getText()));
					}
					dialog.setFilterNames(new String[] {BaseMessages.getString(PKG, "System.FileType.TextFiles"), BaseMessages.getString(PKG, "System.FileType.CSVFiles"), BaseMessages.getString(PKG, "System.FileType.AllFiles")});
					if (dialog.open()!=null)
					{
						String extension = wExtension.getText();
						if ( extension != null && dialog.getFileName() != null &&
								dialog.getFileName().endsWith("." + extension) )
						{
							// The extension is filled in and matches the end 
							// of the selected file => Strip off the extension.
							String fileName = dialog.getFileName();
						    wFilename.setText(dialog.getFilterPath()+System.getProperty("file.separator")+
						    		          fileName.substring(0, fileName.length() - (extension.length()+1)));
						}
						else
						{
						    wFilename.setText(dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName());
						}
					}
				}
			}
		);
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		lsResize = new Listener() 
		{
			public void handleEvent(Event event) 
			{
				//Point size = shell.getSize();
				
			}
		};
		shell.addListener(SWT.Resize, lsResize);

		
		wTabFolder.setSelection(0);
		
		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		ActiveTruncate();
		input.setChanged(changed);//backupChanged);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	private void ActiveTruncate()
	{
       
        wlTruncate.setEnabled(!wAddCreate.getSelection());
        wTruncate.setEnabled(!wAddCreate.getSelection());
        if (wAddCreate.getSelection())
        	wTruncate.setSelection(false);
      
        
        
	}
	 private void setEncodings()
	    {
	        // Encoding of the text file:
	        if (!gotEncodings)
	        {
	            gotEncodings = true;
	            
	            wEncoding.removeAll();
	            List<Charset> values = new ArrayList<Charset>(Charset.availableCharsets().values());
	            for (int i=0;i<values.size();i++)
	            {
	                Charset charSet = (Charset)values.get(i);
	                wEncoding.add( charSet.displayName() );
	            }
	            
	            // Now select the default!
	            String defEncoding = Const.getEnvironmentVariable("file.encoding", "UTF-8");
	            int idx = Const.indexOfString(defEncoding, wEncoding.getItems() );
	            if (idx>=0) wEncoding.select( idx );
	        }
	    }
	

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
        if (input.getSchemaName() != null) wSchema.setText(input.getSchemaName());
		if (input.getTablename() != null) wTable.setText(input.getTablename());
		if (input.getDatabaseMeta() != null) wConnection.setText(input.getDatabaseMeta().getName());
		
		if (input.getFileName()  != null) wFilename.setText(input.getFileName());
		wCreateParentFolder.setSelection(input.isCreateParentFolder());
		if (input.getExtension() != null) 
		{
			wExtension.setText(input.getExtension());
		}
		else
		{
			wExtension.setText("sql");
		}
		
		if (input.getDateFormat()  != null) wFormat.setText(input.getDateFormat());
		
		wSplitEvery.setText(""+input.getSplitEvery());
		wAddDate.setSelection(input.isDateInFilename());
		wAddTime.setSelection(input.isTimeInFilename());
		wAppend.setSelection(input.isFileAppended());
		wAddStepnr.setSelection(input.isStepNrInFilename());
		
        wTruncate.setSelection( input.truncateTable() );
        wAddCreate.setSelection( input.createTable() );
        
        if (input.getEncoding()  !=null) wEncoding.setText(input.getEncoding());
        wAddToResult.setSelection( input.AddToResult() );
        wStartNewLine.setSelection( input.StartNewLine() );
        wDoNotOpenNewFileInit.setSelection( input.isDoNotOpenNewFileInit() );
        
        
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(backupChanged);
		dispose();
	}
	
	private void getInfo(SQLFileOutputMeta info)
	{
        info.setSchemaName( wSchema.getText() );
		info.setTablename( wTable.getText() );
		info.setDatabaseMeta(  transMeta.findDatabase(wConnection.getText()) );
		info.setTruncateTable( wTruncate.getSelection() );
		info.setCreateParentFolder(wCreateParentFolder.getSelection() );
		
		info.setCreateTable( wAddCreate.getSelection() );
		
		
		info.setFileName(   wFilename.getText() );
		info.setExtension(  wExtension.getText() );
		info.setDateFormat(  wFormat.getText() );
		info.setSplitEvery( Const.toInt(wSplitEvery.getText(), 0) );
		info.setFileAppended( wAppend.getSelection() );
		info.setStepNrInFilename( wAddStepnr.getSelection() );
		info.setDateInFilename( wAddDate.getSelection() );
		info.setTimeInFilename( wAddTime.getSelection() );
		
		info.setEncoding( wEncoding.getText() );
		info.setAddToResult( wAddToResult.getSelection() );
		info.setStartNewLine( wStartNewLine.getSelection() );
		info.setDoNotOpenNewFileInit( wDoNotOpenNewFileInit.getSelection() );
		
				

	}
	
	private void ok()
	{
		stepname = wStepname.getText(); // return value
		
		getInfo(input);

		if (input.getDatabaseMeta()==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "SQLFileOutputDialog.ConnectionError.DialogMessage"));
			mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
			mb.open();
			return;
		}
		
		dispose();
	}
	
	private void getTableName()
	{
		// New class: SelectTableDialog
		int connr = wConnection.getSelectionIndex();
		if (connr>=0)
		{
			DatabaseMeta inf = transMeta.getDatabase(connr);
						
			logDebug(BaseMessages.getString(PKG, "SQLFileOutputDialog.Log.LookingAtConnection", inf.toString()));
		
			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, transMeta.getDatabases());
            std.setSelectedSchemaAndTable(wSchema.getText(), wTable.getText());
			if (std.open())
			{
                wSchema.setText(Const.NVL(std.getSchemaName(), ""));
                wTable.setText(Const.NVL(std.getTableName(), ""));
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "SQLFileOutputDialog.ConnectionError2.DialogMessage"));
			mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
			mb.open(); 
		}
					
	}
	
	
	// Generate code for create table...
	// Conversions done by Database
	//
	private void sql()
	{
		try
		{
			SQLFileOutputMeta info = new SQLFileOutputMeta();
			getInfo(info);
			RowMetaInterface prev = transMeta.getPrevStepFields(stepname);
          
			StepMeta stepMeta = transMeta.findStep(stepname);
			
			SQLStatement sql = info.getSQLStatements(transMeta, stepMeta, prev);
			if (!sql.hasError())
			{
				if (sql.hasSQL())
				{
					SQLEditor sqledit = new SQLEditor(shell, SWT.NONE, info.getDatabaseMeta(), transMeta.getDbCache(), sql.getSQL());
					sqledit.open();
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
					mb.setMessage(BaseMessages.getString(PKG, "SQLFileOutputDialog.NoSQL.DialogMessage"));
					mb.setText(BaseMessages.getString(PKG, "SQLFileOutputDialog.NoSQL.DialogTitle"));
					mb.open(); 
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage(sql.getError());
				mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
				mb.open(); 
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "SQLFileOutputDialog.BuildSQLError.DialogTitle"), BaseMessages.getString(PKG, "SQLFileOutputDialog.BuildSQLError.DialogMessage"), ke);
		}
	}
}
