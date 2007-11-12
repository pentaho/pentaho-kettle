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

/*
 * Created on 14-07-2007
 *
 */

package org.pentaho.di.ui.trans.steps.accessinput;

import java.io.File;
import java.util.List;
import java.util.Set;

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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.accessinput.AccessInputField;
import org.pentaho.di.trans.steps.accessinput.AccessInputMeta;
import org.pentaho.di.trans.steps.accessinput.Messages;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.Column;

public class AccessInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wFileTab, wContentTab, wFieldsTab;

	private Composite    wFileComp, wContentComp, wFieldsComp;
	private FormData     fdFileComp, fdContentComp, fdFieldsComp;

	private Label        wlFilename;
	private Button       wbbFilename; // Browse: add file or directory
	private Button       wbbTablename; // Browse: add table	
	
	private Button       wbdFilename; // Delete
	private Button       wbeFilename; // Edit
	private Button       wbaFilename; // Add or change
	private TextVar      wFilename;
	private FormData     fdlFilename, fdbFilename, fdbdFilename, fdbeFilename, fdbaFilename, fdFilename, fdbTablename;

	private Label        wlFilenameList;
	private TableView    wFilenameList;
	private FormData     fdlFilenameList, fdFilenameList;

	private Label        wlFilemask;
	private TextVar         wFilemask;
	private FormData     fdlFilemask, fdFilemask;

	private Button       wbShowFiles;
	private FormData     fdbShowFiles;

	private Label        wlInclFilename;
	private Button       wInclFilename;
	private FormData     fdlInclFilename, fdInclFilename;

	private Label        wlInclFilenameField;
	private TextVar      wInclFilenameField;
	private FormData     fdlInclFilenameField, fdInclFilenameField;
	
	private Label        wlInclTablename;
	private Button       wInclTablename;
	private FormData     fdlInclTablename, fdInclTablename;

	private Label        wlInclTablenameField;
	private TextVar      wInclTablenameField;
	private FormData     fdlInclTablenameField, fdInclTablenameField;

	private Label        wlInclRownum;
	private Button       wInclRownum;
	private FormData     fdlInclRownum, fdRownum;

	private Label        wlInclRownumField;
	private TextVar      wInclRownumField;
	private FormData     fdlInclRownumField, fdInclRownumField;
	
	private Label        wlResetRownum;
	private Button       wResetRownum;
	private FormData     fdlResetRownum;
	
	
	private Label        wlLimit;
	private Text         wLimit;
	private FormData     fdlLimit, fdLimit;
   
	private TableView    wFields;
	private FormData     fdFields;

	private AccessInputMeta input;
	
    private Label        wlTable;
    private TextVar      wTable;
    private FormData     fdlTable, fdTable;
    
	private Group wAdditionalGroup;
	private FormData fdAdditionalGroup;
    
    
	
	public static final int dateLengths[] = new int[]
	{
		23, 19, 14, 10, 10, 10, 10, 8, 8, 8, 8, 6, 6
	};

	public AccessInputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(AccessInputMeta)in;
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
		changed = input.hasChanged();
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("AccessInputDialog.DialogTitle"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("System.Label.StepName"));
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.top  = new FormAttachment(0, margin);
		fdlStepname.right= new FormAttachment(middle, -margin);
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
		// START OF FILE TAB   ///
		//////////////////////////
		wFileTab=new CTabItem(wTabFolder, SWT.NONE);
		wFileTab.setText(Messages.getString("AccessInputDialog.File.Tab"));
		
		wFileComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wFileComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth  = 3;
		fileLayout.marginHeight = 3;
		wFileComp.setLayout(fileLayout);

		// Filename line
		wlFilename=new Label(wFileComp, SWT.RIGHT);
		wlFilename.setText(Messages.getString("AccessInputDialog.Filename.Label"));
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(0, 0);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbbFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbbFilename);
		wbbFilename.setText(Messages.getString("AccessInputDialog.FilenameBrowse.Button"));
		wbbFilename.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(0, 0);
		wbbFilename.setLayoutData(fdbFilename);

		wbaFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbaFilename);
		wbaFilename.setText(Messages.getString("AccessInputDialog.FilenameAdd.Button"));
		wbaFilename.setToolTipText(Messages.getString("AccessInputDialog.FilenameAdd.Tooltip"));
		fdbaFilename=new FormData();
		fdbaFilename.right= new FormAttachment(wbbFilename, -margin);
		fdbaFilename.top  = new FormAttachment(0, 0);
		wbaFilename.setLayoutData(fdbaFilename);

	        
		
		wFilename=new TextVar(transMeta,wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right= new FormAttachment(wbaFilename, -margin);
		fdFilename.top  = new FormAttachment(0, 0);
		wFilename.setLayoutData(fdFilename);

		wlFilemask=new Label(wFileComp, SWT.RIGHT);
		wlFilemask.setText(Messages.getString("AccessInputDialog.RegExp.Label"));
 		props.setLook(wlFilemask);
		fdlFilemask=new FormData();
		fdlFilemask.left = new FormAttachment(0, 0);
		fdlFilemask.top  = new FormAttachment(wFilename, margin);
		fdlFilemask.right= new FormAttachment(middle, -margin);
		wlFilemask.setLayoutData(fdlFilemask);
		wFilemask=new TextVar(transMeta,wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilemask);
		wFilemask.addModifyListener(lsMod);
		fdFilemask=new FormData();
		fdFilemask.left = new FormAttachment(middle, 0);
		fdFilemask.top  = new FormAttachment(wFilename, margin);
		fdFilemask.right= new FormAttachment(100, 0);
		wFilemask.setLayoutData(fdFilemask);

		// Filename list line
		wlFilenameList=new Label(wFileComp, SWT.RIGHT);
		wlFilenameList.setText(Messages.getString("AccessInputDialog.FilenameList.Label"));
 		props.setLook(wlFilenameList);
		fdlFilenameList=new FormData();
		fdlFilenameList.left = new FormAttachment(0, 0);
		fdlFilenameList.top  = new FormAttachment(wFilemask, margin);
		fdlFilenameList.right= new FormAttachment(middle, -margin);
		wlFilenameList.setLayoutData(fdlFilenameList);

		// Buttons to the right of the screen...
		wbdFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbdFilename);
		wbdFilename.setText(Messages.getString("AccessInputDialog.FilenameRemove.Button"));
		wbdFilename.setToolTipText(Messages.getString("AccessInputDialog.FilenameRemove.Tooltip"));
		fdbdFilename=new FormData();
		fdbdFilename.right = new FormAttachment(100, 0);
		fdbdFilename.top  = new FormAttachment (wFilemask, 40);
		wbdFilename.setLayoutData(fdbdFilename);

		wbeFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbeFilename);
		wbeFilename.setText(Messages.getString("AccessInputDialog.FilenameEdit.Button"));
		wbeFilename.setToolTipText(Messages.getString("AccessInputDialog.FilenameEdit.Tooltip"));
		fdbeFilename=new FormData();
		fdbeFilename.right = new FormAttachment(100, 0);
		fdbeFilename.top  = new FormAttachment (wbdFilename, margin);
		wbeFilename.setLayoutData(fdbeFilename);
		

		wbShowFiles=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbShowFiles);
		wbShowFiles.setText(Messages.getString("AccessInputDialog.ShowFiles.Button"));
		fdbShowFiles=new FormData();
		fdbShowFiles.left   = new FormAttachment(middle, 0);
		fdbShowFiles.bottom = new FormAttachment(100, 0);
		wbShowFiles.setLayoutData(fdbShowFiles);

		ColumnInfo[] colinfo=new ColumnInfo[2];
		colinfo[ 0]=new ColumnInfo(
          Messages.getString("AccessInputDialog.Files.Filename.Column"),
          ColumnInfo.COLUMN_TYPE_TEXT,
          false);
		colinfo[ 1]=new ColumnInfo(
          Messages.getString("AccessInputDialog.Files.Wildcard.Column"),
          ColumnInfo.COLUMN_TYPE_TEXT,
          false);
		
		colinfo[0].setUsingVariables(true);
		colinfo[1].setUsingVariables(true);
		colinfo[1].setToolTip(Messages.getString("AccessInputDialog.Files.Wildcard.Tooltip"));
				
		wFilenameList = new TableView(transMeta,wFileComp, 
						      SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, 
						      colinfo, 
						      2,  
						      lsMod,
							  props
						      );
 		props.setLook(wFilenameList);

 	
 		
		fdFilenameList=new FormData();
		fdFilenameList.left   = new FormAttachment(middle, 0);
		fdFilenameList.right  = new FormAttachment(wbdFilename, -margin);
		fdFilenameList.top    = new FormAttachment(wFilemask, margin);
		fdFilenameList.bottom = new FormAttachment(wbShowFiles, -margin);
		wFilenameList.setLayoutData(fdFilenameList);
		
	
		fdFileComp=new FormData();
		fdFileComp.left  = new FormAttachment(0, 0);
		fdFileComp.top   = new FormAttachment(0, 0);
		fdFileComp.right = new FormAttachment(100, 0);
		fdFileComp.bottom= new FormAttachment(100, 0);
		wFileComp.setLayoutData(fdFileComp);
	
		wFileComp.layout();
		wFileTab.setControl(wFileComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF FILE TAB
		/////////////////////////////////////////////////////////////

		//////////////////////////
		// START OF CONTENT TAB///
		///
		wContentTab=new CTabItem(wTabFolder, SWT.NONE);
		wContentTab.setText(Messages.getString("AccessInputDialog.Content.Tab"));

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		wContentComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wContentComp);
		wContentComp.setLayout(contentLayout);
		
		
		wlTable=new Label(wContentComp, SWT.RIGHT);
        wlTable.setText(Messages.getString("AccessInputDialog.Table.Label"));
        props.setLook(wlTable);
        fdlTable=new FormData();
        fdlTable.left = new FormAttachment(0, 0);
        fdlTable.top  = new FormAttachment(0, margin);
        fdlTable.right= new FormAttachment(middle, -margin);
        wlTable.setLayoutData(fdlTable);
        wTable=new TextVar(transMeta,wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wTable.setToolTipText(Messages.getString("AccessInputDialog.Table.Tooltip"));
        props.setLook(wTable);
        wTable.addModifyListener(lsMod);
        fdTable=new FormData();
        fdTable.left = new FormAttachment(middle, 0);
        fdTable.top  = new FormAttachment(0, margin);
        fdTable.right= new FormAttachment(100, -70);
        wTable.setLayoutData(fdTable);
        
        
		wbbTablename=new Button(wContentComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbbTablename);
 		wbbTablename.setText(Messages.getString("AccessInputDialog.FilenameBrowse.Button"));
 		wbbTablename.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
		fdbTablename=new FormData();
		fdbTablename.right= new FormAttachment(100, 0);
		fdbTablename.top  = new FormAttachment(0, 0);
		wbbTablename.setLayoutData(fdbTablename);

		wbbTablename.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getTableName(); } } );

		// /////////////////////////////////
		// START OF Additional Fields GROUP
		// /////////////////////////////////

		wAdditionalGroup = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wAdditionalGroup);
		wAdditionalGroup.setText(Messages.getString("AccessInputDialog.Group.AdditionalGroup.Label"));
		
		FormLayout additionalgroupLayout = new FormLayout();
		additionalgroupLayout.marginWidth = 10;
		additionalgroupLayout.marginHeight = 10;
		wAdditionalGroup.setLayout(additionalgroupLayout);

		wlInclFilename=new Label(wAdditionalGroup, SWT.RIGHT);
		wlInclFilename.setText(Messages.getString("AccessInputDialog.InclFilename.Label"));
 		props.setLook(wlInclFilename);
		fdlInclFilename=new FormData();
		fdlInclFilename.left = new FormAttachment(0, 0);
		fdlInclFilename.top  = new FormAttachment(wTable, 2*margin);
		fdlInclFilename.right= new FormAttachment(middle, -margin);
		wlInclFilename.setLayoutData(fdlInclFilename);
		wInclFilename=new Button(wAdditionalGroup, SWT.CHECK );
 		props.setLook(wInclFilename);
		wInclFilename.setToolTipText(Messages.getString("AccessInputDialog.InclFilename.Tooltip"));
		fdInclFilename=new FormData();
		fdInclFilename.left = new FormAttachment(middle, 0);
		fdInclFilename.top  = new FormAttachment(wTable, 2*margin);
		wInclFilename.setLayoutData(fdInclFilename);

		wlInclFilenameField=new Label(wAdditionalGroup, SWT.LEFT);
		wlInclFilenameField.setText(Messages.getString("AccessInputDialog.InclFilenameField.Label"));
 		props.setLook(wlInclFilenameField);
		fdlInclFilenameField=new FormData();
		fdlInclFilenameField.left = new FormAttachment(wInclFilename, margin);
		fdlInclFilenameField.top  = new FormAttachment(wTable, 2*margin);
		wlInclFilenameField.setLayoutData(fdlInclFilenameField);
		wInclFilenameField=new TextVar(transMeta,wAdditionalGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclFilenameField);
		wInclFilenameField.addModifyListener(lsMod);
		fdInclFilenameField=new FormData();
		fdInclFilenameField.left = new FormAttachment(wlInclFilenameField , margin);
		fdInclFilenameField.top  = new FormAttachment(wTable, 2*margin);
		fdInclFilenameField.right= new FormAttachment(100, 0);
		wInclFilenameField.setLayoutData(fdInclFilenameField);

		wlInclTablename=new Label(wAdditionalGroup, SWT.RIGHT);
		wlInclTablename.setText(Messages.getString("AccessInputDialog.InclTablename.Label"));
 		props.setLook(wlInclTablename);
		fdlInclTablename=new FormData();
		fdlInclTablename.left = new FormAttachment(0, 0);
		fdlInclTablename.top  = new FormAttachment(wInclFilenameField, margin);
		fdlInclTablename.right= new FormAttachment(middle, -margin);
		wlInclTablename.setLayoutData(fdlInclTablename);
		wInclTablename=new Button(wAdditionalGroup, SWT.CHECK );
 		props.setLook(wInclTablename);
		wInclTablename.setToolTipText(Messages.getString("AccessInputDialog.InclTablename.Tooltip"));
		fdInclTablename=new FormData();
		fdInclTablename.left = new FormAttachment(middle, 0);
		fdInclTablename.top  = new FormAttachment(wInclFilenameField, margin);
		wInclTablename.setLayoutData(fdInclTablename);
		
		
		wlInclTablenameField=new Label(wAdditionalGroup, SWT.LEFT);
		wlInclTablenameField.setText(Messages.getString("AccessInputDialog.InclTablenameField.Label"));
 		props.setLook(wlInclTablenameField);
		fdlInclTablenameField=new FormData();
		fdlInclTablenameField.left = new FormAttachment(wInclFilename, margin);
		fdlInclTablenameField.top  = new FormAttachment(wInclFilenameField, margin);
		wlInclTablenameField.setLayoutData(fdlInclTablenameField);
		wInclTablenameField=new TextVar(transMeta,wAdditionalGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclTablenameField);
		wInclTablenameField.addModifyListener(lsMod);
		fdInclTablenameField=new FormData();
		fdInclTablenameField.left = new FormAttachment(wlInclTablenameField, margin);
		fdInclTablenameField.top  = new FormAttachment(wInclFilenameField, margin);
		fdInclTablenameField.right= new FormAttachment(100, 0);
		wInclTablenameField.setLayoutData(fdInclTablenameField);

	
		wlInclRownum=new Label(wAdditionalGroup, SWT.RIGHT);
		wlInclRownum.setText(Messages.getString("AccessInputDialog.InclRownum.Label"));
 		props.setLook(wlInclRownum);
		fdlInclRownum=new FormData();
		fdlInclRownum.left = new FormAttachment(0, 0);
		fdlInclRownum.top  = new FormAttachment(wInclTablenameField, margin);
		fdlInclRownum.right= new FormAttachment(middle, -margin);
		wlInclRownum.setLayoutData(fdlInclRownum);
		wInclRownum=new Button(wAdditionalGroup, SWT.CHECK );
 		props.setLook(wInclRownum);
		wInclRownum.setToolTipText(Messages.getString("AccessInputDialog.InclRownum.Tooltip"));
		fdRownum=new FormData();
		fdRownum.left = new FormAttachment(middle, 0);
		fdRownum.top  = new FormAttachment(wInclTablenameField, margin);
		wInclRownum.setLayoutData(fdRownum);

		wlInclRownumField=new Label(wAdditionalGroup, SWT.RIGHT);
		wlInclRownumField.setText(Messages.getString("AccessInputDialog.InclRownumField.Label"));
 		props.setLook(wlInclRownumField);
		fdlInclRownumField=new FormData();
		fdlInclRownumField.left = new FormAttachment(wInclRownum, margin);
		fdlInclRownumField.top  = new FormAttachment(wInclTablenameField, margin);
		wlInclRownumField.setLayoutData(fdlInclRownumField);
		wInclRownumField=new TextVar(transMeta,wAdditionalGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclRownumField);
		wInclRownumField.addModifyListener(lsMod);
		fdInclRownumField=new FormData();
		fdInclRownumField.left = new FormAttachment(wlInclRownumField, margin);
		fdInclRownumField.top  = new FormAttachment(wInclTablenameField, margin);
		fdInclRownumField.right= new FormAttachment(100, 0);
		wInclRownumField.setLayoutData(fdInclRownumField);
		
		
		wlResetRownum=new Label(wAdditionalGroup, SWT.RIGHT);
		wlResetRownum.setText(Messages.getString("AccessInputDialog.ResetRownum.Label"));
 		props.setLook(wlResetRownum);
		fdlResetRownum=new FormData();
		fdlResetRownum.left = new FormAttachment(wInclRownum, margin);
		fdlResetRownum.top  = new FormAttachment(wInclRownumField, margin);
		wlResetRownum.setLayoutData(fdlResetRownum);
		wResetRownum=new Button(wAdditionalGroup, SWT.CHECK );
 		props.setLook(wResetRownum);
		wResetRownum.setToolTipText(Messages.getString("AccessInputDialog.ResetRownum.Tooltip"));
		fdRownum=new FormData();
		fdRownum.left = new FormAttachment(wlResetRownum, margin);
		fdRownum.top  = new FormAttachment(wInclRownumField, margin);	
		wResetRownum.setLayoutData(fdRownum);
		
		
		fdAdditionalGroup = new FormData();
		fdAdditionalGroup.left = new FormAttachment(0, margin);
		fdAdditionalGroup.top = new FormAttachment(wTable, margin);
		fdAdditionalGroup.right = new FormAttachment(100, -margin);
		wAdditionalGroup.setLayoutData(fdAdditionalGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF DESTINATION ADDRESS  GROUP
		// ///////////////////////////////////////////////////////////
		
		
		wlLimit=new Label(wContentComp, SWT.RIGHT);
		wlLimit.setText(Messages.getString("AccessInputDialog.Limit.Label"));
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.top  = new FormAttachment(wAdditionalGroup, 2*margin);
		fdlLimit.right= new FormAttachment(middle, -margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.top  = new FormAttachment(wAdditionalGroup, 2*margin);
		fdLimit.right= new FormAttachment(100, 0);
		wLimit.setLayoutData(fdLimit);
       
		fdContentComp = new FormData();
		fdContentComp.left  = new FormAttachment(0, 0);
		fdContentComp.top   = new FormAttachment(0, 0);
		fdContentComp.right = new FormAttachment(100, 0);
		fdContentComp.bottom= new FormAttachment(100, 0);
		wContentComp.setLayoutData(fdContentComp);

		wContentComp.layout();
		wContentTab.setControl(wContentComp);

		// ///////////////////////////////////////////////////////////
		// / END OF CONTENT TAB
		// ///////////////////////////////////////////////////////////


		// Fields tab...
		//
		wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
		wFieldsTab.setText(Messages.getString("AccessInputDialog.Fields.Tab"));
		
		FormLayout fieldsLayout = new FormLayout ();
		fieldsLayout.marginWidth  = Const.FORM_MARGIN;
		fieldsLayout.marginHeight = Const.FORM_MARGIN;
		
		wFieldsComp = new Composite(wTabFolder, SWT.NONE);
		wFieldsComp.setLayout(fieldsLayout);
 		props.setLook(wFieldsComp);
		
 		wGet=new Button(wFieldsComp, SWT.PUSH);
		wGet.setText(Messages.getString("AccessInputDialog.GetFields.Button"));
		fdGet=new FormData();
		fdGet.left=new FormAttachment(50, 0);
		fdGet.bottom =new FormAttachment(100, 0);
		wGet.setLayoutData(fdGet);
		
		final int FieldsRows=input.getInputFields().length;
		
		// Prepare a list of possible formats...
		String dats[] = Const.getDateFormats();
		String nums[] = Const.getNumberFormats();
		int totsize = dats.length + nums.length;
		String formats[] = new String[totsize];
		for (int x=0;x<dats.length;x++) formats[x] = dats[x];
		for (int x=0;x<nums.length;x++) formats[dats.length+x] = nums[x];
		
		
		ColumnInfo[] colinf=new ColumnInfo[]
            {
			 new ColumnInfo(
         Messages.getString("AccessInputDialog.FieldsTable.Name.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
         new ColumnInfo(Messages.getString("AccessInputDialog.FieldsTable.Attribut.Column"),ColumnInfo.COLUMN_TYPE_TEXT,false),
		 new ColumnInfo(Messages.getString("AccessInputDialog.FieldsTable.Type.Column"),ColumnInfo.COLUMN_TYPE_CCOMBO,ValueMeta.getTypes(),true ),
		 new ColumnInfo(Messages.getString("AccessInputDialog.FieldsTable.Format.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,formats),
         new ColumnInfo(
         Messages.getString("AccessInputDialog.FieldsTable.Length.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("AccessInputDialog.FieldsTable.Precision.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("AccessInputDialog.FieldsTable.Currency.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("AccessInputDialog.FieldsTable.Decimal.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("AccessInputDialog.FieldsTable.Group.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("AccessInputDialog.FieldsTable.TrimType.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         formats,//AccessInputField.trimTypeDesc,
         true ),
			 new ColumnInfo(
         Messages.getString("AccessInputDialog.FieldsTable.Repeat.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         new String[] { Messages.getString("System.Combo.Yes"), Messages.getString("System.Combo.No") },
         true ),
     
    };
		
		colinf[0].setUsingVariables(true);
		colinf[0].setToolTip(Messages.getString("AccessInputDialog.FieldsTable.Name.Column.Tooltip"));
		colinf[1].setUsingVariables(true);
		colinf[1].setToolTip(Messages.getString("AccessInputDialog.FieldsTable.Attribut.Column.Tooltip"));
		
		wFields=new TableView(transMeta,wFieldsComp, 
						      SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
						      lsMod,
							  props
						      );

		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(0, 0);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(wGet, -margin);
		wFields.setLayoutData(fdFields);

		fdFieldsComp=new FormData();
		fdFieldsComp.left  = new FormAttachment(0, 0);
		fdFieldsComp.top   = new FormAttachment(0, 0);
		fdFieldsComp.right = new FormAttachment(100, 0);
		fdFieldsComp.bottom= new FormAttachment(100, 0);
		wFieldsComp.setLayoutData(fdFieldsComp);
		
		wFieldsComp.layout();
		wFieldsTab.setControl(wFieldsComp);
		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
		
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));

		wPreview=new Button(shell, SWT.PUSH);
		wPreview.setText(Messages.getString("AccessInputDialog.Button.PreviewRows"));
		
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));
		
		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, wTabFolder);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();      } };
		lsPreview  = new Listener() { public void handleEvent(Event e) { preview();   } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();     } };
		
		wOK.addListener     (SWT.Selection, lsOK     );
		wGet.addListener    (SWT.Selection, lsGet    );
		wPreview.addListener(SWT.Selection, lsPreview);
		wCancel.addListener (SWT.Selection, lsCancel );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wLimit.addSelectionListener( lsDef );
		wInclRownumField.addSelectionListener( lsDef );
		wInclFilenameField.addSelectionListener( lsDef );
		wInclTablenameField.addSelectionListener( lsDef );
		
		// Add the file to the list of files...
		SelectionAdapter selA = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				wFilenameList.add(new String[] { wFilename.getText(), wFilemask.getText() } );
				wFilename.setText("");
				wFilemask.setText("");
				wFilenameList.removeEmptyRows();
				wFilenameList.setRowNums();
                wFilenameList.optWidth(true);
			}
		};
		wbaFilename.addSelectionListener(selA);
		wFilename.addSelectionListener(selA);
		
		// Delete files from the list of files...
		wbdFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				int idx[] = wFilenameList.getSelectionIndices();
				wFilenameList.remove(idx);
				wFilenameList.removeEmptyRows();
				wFilenameList.setRowNums();
			}
		});

		// Edit the selected file & remove from the list...
		wbeFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				int idx = wFilenameList.getSelectionIndex();
				if (idx>=0)
				{
					String string[] = wFilenameList.getItem(idx);
					wFilename.setText(string[0]);
					wFilemask.setText(string[1]);
					wFilenameList.remove(idx);
				}
				wFilenameList.removeEmptyRows();
				wFilenameList.setRowNums();
			}
		});

		// Show the files that are selected at this time...
		wbShowFiles.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
                    try
                    {
    					AccessInputMeta tfii = new AccessInputMeta();
    					getInfo(tfii);
    					FileInputList fileInputList = tfii.getFiles(transMeta);
    					String files[] = fileInputList.getFileStrings();
    					
    					if (files.length > 0)
    			        {
    			            EnterSelectionDialog esd = new EnterSelectionDialog(shell, files, Messages.getString("AccessInputDialog.FilesReadSelection.DialogTitle"), Messages.getString("AccessInputDialog.FilesReadSelection.DialogMessage"));
    			            esd.setViewOnly();
    			            esd.open();
    			        }
    					
    					else
    					{
    			            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
    			            mb.setMessage(Messages.getString("AccessInputDialog.NoFileFound.DialogMessage"));
    			            mb.setText(Messages.getString("System.Dialog.Error.Title"));
    			            mb.open(); 
    					}
                    }
                    catch(KettleException ex)
                    {
                        new ErrorDialog(shell, Messages.getString("AccessInputDialog.ErrorParsingData.DialogTitle"), Messages.getString("AccessInputDialog.ErrorParsingData.DialogMessage"), ex);
                    }
				}
			}
		);
		
			
		// Enable/disable the right fields to allow a filename to be added to each row...
		wInclFilename.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					setIncludeFilename();
				}
			}
		);
		
		// Enable/disable the right fields to allow a row number to be added to each row...
		wInclRownum.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					setIncludeRownum();
				}
			}
		);
		// Enable/disable the right fields to allow a table name to be added to each row...
		wInclTablename.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					setIncludeTablename();
				}
			}
		);
		// Whenever something changes, set the tooltip to the expanded version of the filename:
		wFilename.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wFilename.setToolTipText("");//StringUtil.environmentSubstitute( wFilename.getText() ) );
				}
			}
		);
		
		
		
		// Listen to the Browse... button
		wbbFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					if (wFilemask.getText()!=null && wFilemask.getText().length()>0) // A mask: a directory!
					{
						DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
						if (wFilename.getText()!=null)
						{
							String fpath = "";//StringUtil.environmentSubstitute(wFilename.getText());
							dialog.setFilterPath( fpath );
						}
						
						if (dialog.open()!=null)
						{
							String str= dialog.getFilterPath();
							wFilename.setText(str);
						}
					}
					else
					{
						FileDialog dialog = new FileDialog(shell, SWT.OPEN);
						dialog.setFilterExtensions(new String[] {"*mdb;*.MDB", "*"});
						if (wFilename.getText()!=null)
						{
							String fname = "";//StringUtil.environmentSubstitute(wFilename.getText());
							dialog.setFileName( fname );
						}
						
						dialog.setFilterNames(new String[] {Messages.getString("AccessInputDialog.FileType.AccessFiles"), Messages.getString("System.FileType.AllFiles")});
						
						if (dialog.open()!=null)
						{
							String str = dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName();
							wFilename.setText(str);
						}
					}
				}
			}
		);
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		wTabFolder.setSelection(0);

		// Set the shell size, based upon previous time...
		setSize();
		getData(input);
		input.setChanged(changed);
		wFields.optWidth(true);
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	private void get()
	{
		RowMetaInterface fields = new RowMeta();
		
        try
        {
        	
        	AccessInputMeta meta = new AccessInputMeta();
    		getInfo(meta);
        	
    		// Check if a table name is specified 
    		if (!checkInputTableName(meta)) return;
            
            FileInputList inputList = meta.getFiles(transMeta);

            if (inputList.getFiles().size()>0)
            {
                // Open the file (only first file)...

            	Database d = Database.open(new File(KettleVFS.getFilename(inputList.getFile(0))));			
    			Table t=d.getTable(meta.getTableName());
    			
    			// Get the list of columns
    			List<Column> col = t.getColumns();    			
    		
    			for (int i=0;i<col.size() ;i++)
    			{
    				String fieldname = null;
					int    fieldtype = ValueMetaInterface.TYPE_NONE;
					
    				Column c = (Column)col.get(i);
    				
    				// Get column name
    				fieldname=c.getName();
    				
    				
		            // Get column type and Map with PDI values    				
    				
    				if(DataType.INT.equals(c.getType()))
    				{
    					fieldtype = ValueMetaInterface.TYPE_INTEGER;
    				}
    				else if(DataType.BOOLEAN.equals(c.getType()))
    				{
    					fieldtype = ValueMetaInterface.TYPE_BOOLEAN;
    				}
    				else if(DataType.BINARY.equals(c.getType()))
    				{
    					fieldtype = ValueMetaInterface.TYPE_BINARY;
    				}
    				else if((DataType.DOUBLE.equals(c.getType())) || (DataType.LONG.equals(c.getType())) 
    						|| (DataType.NUMERIC.equals(c.getType()) ) )
    				{
    					fieldtype = ValueMetaInterface.TYPE_NUMBER;
    				}
    				else if((DataType.FLOAT.equals(c.getType())) || (DataType.MONEY.equals(c.getType())) 
    						|| (DataType.MEMO.equals(c.getType())) )
    				{
    					fieldtype = ValueMetaInterface.TYPE_BIGNUMBER;
    				}
    				else if((DataType.SHORT_DATE_TIME.equals(c.getType())))
    				{
    					fieldtype = ValueMetaInterface.TYPE_DATE;
    				}
    				else
    				{
    					fieldtype = ValueMetaInterface.TYPE_STRING;
    				}
    				
    				if (fieldname!=null && fieldtype!=ValueMetaInterface.TYPE_NONE)
					{
    					ValueMetaInterface field = new ValueMeta(fieldname, fieldtype);
    					if (fields.indexOfValue(field.getName())<0) fields.addValueMeta(field);
					}
						
    			}
    			
            }	
    					
    		}
            catch(Exception e)
    		{
        		 new ErrorDialog(shell, Messages.getString("System.Dialog.Error.Title"), Messages.getString("AccessInputDialog.ErrorReadingFile.DialogMessage", e.toString()), e);
    		} 

            
            if (fields.size()>0)
    		{
            	
            	// Clear Fields Grid
                wFields.removeAll();
                
    			for (int j=0;j<fields.size();j++)
    			{
    				ValueMetaInterface field = fields.getValueMeta(j);
    				wFields.add(new String[] { field.getName(), field.getName(),field.getTypeDesc(), "", "-1", "","","","","none", "N" } );
    			}
    			wFields.removeEmptyRows();
    			wFields.setRowNums();
    			wFields.optWidth(true);
    		}
    		else
    		{
    			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
    			mb.setMessage(Messages.getString("AccessInputDialog.UnableToFindFields.DialogTitle"));
    			mb.setText(Messages.getString("AccessInputDialog.UnableToFindFields.DialogMessage"));
    			mb.open(); 
    		}
            
 
	}

	public void setIncludeFilename()
	{
		wlInclFilenameField.setEnabled(wInclFilename.getSelection());
		wInclFilenameField.setEnabled(wInclFilename.getSelection());
	}

	public void setIncludeRownum()
	{
		wlInclRownumField.setEnabled(wInclRownum.getSelection());
		wInclRownumField.setEnabled(wInclRownum.getSelection());
		wlResetRownum.setEnabled(wInclRownum.getSelection());
		wResetRownum.setEnabled(wInclRownum.getSelection());
	}
	
	public void setIncludeTablename()
	{
		wlInclTablenameField.setEnabled(wInclTablename.getSelection());
		wInclTablenameField.setEnabled(wInclTablename.getSelection());
	}


	/**
	 * Read the data from the TextFileInputMeta object and show it in this dialog.
	 * 
	 * @param in The TextFileInputMeta object to obtain the data from.
	 */
	public void getData(AccessInputMeta in)
	{
		if (in.getFileName() !=null) 
		{
			wFilenameList.removeAll();
			for (int i=0;i<in.getFileName().length;i++) 
			{
				wFilenameList.add(new String[] { in.getFileName()[i], in.getFileMask()[i] } );
			}
			wFilenameList.removeEmptyRows();
			wFilenameList.setRowNums();
			wFilenameList.optWidth(true);
		}
		wInclFilename.setSelection(in.includeFilename());
		wInclTablename.setSelection(in.includeTablename());
		wInclRownum.setSelection(in.includeRowNumber());
		if (in.getTableName()!=null) wTable.setText(in.getTableName());
		if (in.getFilenameField()!=null) wInclFilenameField.setText(in.getFilenameField());
		if (in.gettablenameField()!=null) wInclTablenameField.setText(in.gettablenameField());
		if (in.getRowNumberField()!=null) wInclRownumField.setText(in.getRowNumberField());
		wResetRownum.setSelection(in.resetRowNumber());
		wLimit.setText(""+in.getRowLimit());

		log.logDebug(toString(), Messages.getString("AccessInputDialog.Log.GettingFieldsInfo"));
		for (int i=0;i<in.getInputFields().length;i++)
		{
		    AccessInputField field = in.getInputFields()[i];
		    
            if (field!=null)
            {
    			TableItem item  = wFields.table.getItem(i);
    			String name     = field.getName();
    			String xpath	= field.getColumn();
    			String type     = field.getTypeDesc();
    			String format   = field.getFormat();
    			String length   = ""+field.getLength();
    			String prec     = ""+field.getPrecision();
    			String curr     = field.getCurrencySymbol();
    			String group    = field.getGroupSymbol();
    			String decim    = field.getDecimalSymbol();
    			String trim     = field.getTrimTypeDesc();
    			String rep      = field.isRepeated()?Messages.getString("System.Combo.Yes"):Messages.getString("System.Combo.No");
    			
                if (name    !=null) item.setText( 1, name);
                if (xpath   !=null) item.setText( 2, xpath);
    			if (type    !=null) item.setText( 3, type);
    			if (format  !=null) item.setText( 4, format);
    			if (length  !=null && !"-1".equals(length)) item.setText( 5, length);
    			if (prec    !=null && !"-1".equals(prec)) item.setText( 6, prec);
    			if (curr    !=null) item.setText( 7, curr);
    			if (decim   !=null) item.setText( 8, decim);
    			if (group   !=null) item.setText( 9, group);
    			if (trim    !=null) item.setText(10, trim);
    			if (rep     !=null) item.setText(11, rep);                
            }
		}
        
        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth(true);

		setIncludeFilename();
		setIncludeTablename();
		setIncludeRownum();

		wStepname.selectAll();
	}

	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;
		
        try
        {
            getInfo(input);
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("AccessInputDialog.ErrorParsingData.DialogTitle"), Messages.getString("AccessInputDialog.ErrorParsingData.DialogMessage"), e);
        }
		dispose();
	}
	
	private void getInfo(AccessInputMeta in) throws KettleException
	{
		stepname = wStepname.getText(); // return value

		// copy info to TextFileInputMeta class (input)
		in.setRowLimit( Const.toLong(wLimit.getText(), 0L) );
		
		
		in.setTableName( wTable.getText() );
		
		in.setIncludeFilename( wInclFilename.getSelection() );
		in.setFilenameField( wInclFilenameField.getText() );
		
		in.setIncludeTablename( wInclTablename.getSelection() );
		in.setTablenameField( wInclTablenameField.getText() );
		
		in.setIncludeRowNumber( wInclRownum.getSelection() );
		in.setRowNumberField( wInclRownumField.getText() );
		
		in.setResetRowNumber( wResetRownum.getSelection() );
				

		
		int nrFiles     = wFilenameList.getItemCount();
		int nrFields    = wFields.nrNonEmpty();
         
		in.allocate(nrFiles, nrFields);

		in.setFileName( wFilenameList.getItems(0) );
		in.setFileMask( wFilenameList.getItems(1) );

		for (int i=0;i<nrFields;i++)
		{
		    AccessInputField field = new AccessInputField();
		    
			TableItem item  = wFields.getNonEmpty(i);
            
			field.setName( item.getText(1) );
			field.setColumn( item.getText(2) );
			field.setType(ValueMeta.getType(item.getText(3)));
			field.setFormat( item.getText(4) );
			field.setLength( Const.toInt(item.getText(5), -1) );
			field.setPrecision( Const.toInt(item.getText(6), -1) );
			field.setCurrencySymbol( item.getText(7) );
			field.setDecimalSymbol( item.getText(8) );
			field.setGroupSymbol( item.getText(9) );
			field.setTrimType( AccessInputField.getTrimTypeByDesc(item.getText(10)) );
			field.setRepeated( Messages.getString("System.Combo.Yes").equalsIgnoreCase(item.getText(11)) );		
            
			in.getInputFields()[i] = field;
		}	
	}
	

	
		
	// Preview the data
	private void preview()
	{
        try
        {
            // Create the XML input step
            AccessInputMeta oneMeta = new AccessInputMeta();
            getInfo(oneMeta);
            
            // check if the path is given
    		if (!checkInputTableName(oneMeta)) return;

        
            TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
            
            EnterNumberDialog numberDialog = new EnterNumberDialog(shell, 500, Messages.getString("AccessInputDialog.NumberRows.DialogTitle"), Messages.getString("AccessInputDialog.NumberRows.DialogMessage"));
            int previewSize = numberDialog.open();
            if (previewSize>0)
            {
                TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
                progressDialog.open();
                
                if (!progressDialog.isCancelled())
                {
                    Trans trans = progressDialog.getTrans();
                    String loggingText = progressDialog.getLoggingText();
                    
                    if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
                    {
                    	EnterTextDialog etd = new EnterTextDialog(shell, Messages.getString("System.Dialog.PreviewError.Title"),  
                    			Messages.getString("System.Dialog.PreviewError.Message"), loggingText, true );
                    	etd.setReadOnly();
                    	etd.open();
                    }
                             
                    PreviewRowsDialog prd = new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(),
							progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog
									.getPreviewRows(wStepname.getText()), loggingText);
					prd.open();
                    
                }
            }
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("AccessInputDialog.ErrorPreviewingData.DialogTitle"), Messages.getString("AccessInputDialog.ErrorPreviewingData.DialogMessage"), e);
       }
	}
	// check if the table name is given
	private boolean checkInputTableName(AccessInputMeta meta){
        if (meta.getTableName()==null || meta.getTableName().length()<1)
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
            mb.setMessage(Messages.getString("AccessInputDialog.TableMissing.DialogMessage"));
            mb.setText(Messages.getString("System.Dialog.Error.Title"));
            mb.open(); 

            return false;
        }
        else
        {
        	return true;
        }
	}
	public String toString()
	{
		return this.getClass().getName();
	}
	
	private void getTableName()
	{

		Database accessDatabase = null;
		
		try
		{

			AccessInputMeta meta = new AccessInputMeta();
			getInfo(meta);
			
			FileInputList fileInputList = meta.getFiles(transMeta);

			if (fileInputList.nrOfFiles()>0)
			{
			
				// Check the first file
			     if (fileInputList.getFile(0).exists()) {
					// Open the file (only first file) in readOnly ...
					//
					accessDatabase = Database.open(new File(KettleVFS.getFilename(fileInputList.getFile(0))));

					// Get user tables
					//
					Set<String> settables = accessDatabase.getTableNames();

					// Get system tables
					//
					// Table systablenames= accessDatabase.getSystemCatalog();

					String[] tablenames = (String[]) settables.toArray(new String[settables.size()]);

					EnterSelectionDialog dialog = new EnterSelectionDialog(shell, tablenames, Messages.getString("AccessInputDialog.Dialog.SelectATable.Title"), Messages.getString("AccessInputDialog.Dialog.SelectATable.Message"));
					String tablename = dialog.open();
					if (tablename != null) {
						wTable.setText(tablename);
					}
				} else {
					// The file not exists !
					throw new KettleException(Messages.getString("AccessInputMeta.Exception.FileDoesNotExist", KettleVFS
							.getFilename(fileInputList.getFile(0))));
				}				
			}
			else
			{
				// No file specified
				 MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
		            mb.setMessage(Messages.getString("AccessInputDialog.FilesMissing.DialogMessage"));
		            mb.setText(Messages.getString("System.Dialog.Error.Title"));
		            mb.open(); 
			}
		}
		catch(Throwable e)
	    {
	        new ErrorDialog(shell, Messages.getString("AccessInputDialog.UnableToGetListOfTables.Title"), Messages.getString("AccessInputDialog.UnableToGetListOfTables.Message"), e);
	    }
	    finally
	    {
	        // Don't forget to close the bugger.
	        try
	        {
	            if (accessDatabase!=null) accessDatabase.close();
	        }
	        catch(Exception e)
	        {}
        }
	}
}