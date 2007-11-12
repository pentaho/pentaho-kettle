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

package org.pentaho.di.ui.trans.steps.getfilesrowscount;

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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.getfilesrowscount.GetFilesRowsCountMeta;
import org.pentaho.di.trans.steps.getfilesrowscount.Messages;
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

public class GetFilesRowsCountDialog extends BaseStepDialog implements StepDialogInterface
{
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wFileTab, wContentTab;

	private Composite    wFileComp, wContentComp;
	private FormData     fdFileComp, fdContentComp;

	private Label        wlFilename;
	private Button       wbbFilename; // Browse: add file or directory
	
	private Button       wbdFilename; // Delete
	private Button       wbeFilename; // Edit
	private Button       wbaFilename; // Add or change
	private TextVar      wFilename;
	private FormData     fdlFilename, fdbFilename, fdbdFilename, fdbeFilename, fdbaFilename, fdFilename;

	private Label        wlFilenameList;
	private TableView    wFilenameList;
	private FormData     fdlFilenameList, fdFilenameList;

	private Label        wlFilemask;
	private TextVar      wFilemask;
	private FormData     fdlFilemask, fdFilemask;

	private Button       wbShowFiles;
	private FormData     fdbShowFiles;

	private Label        wlInclFilesCount;
	private Button       wInclFilesCount;
	private FormData     fdlInclFilesCount, fdFilesCount;

	private Label        wlInclFilesCountField;
	private TextVar      wInclFilesCountField;
	private FormData     fdlInclFilesCountField, fdInclFilesCountField;
	
	private Label        wlRowsCountField;
	private TextVar      wRowsCountField;
	private FormData     fdlRowsCountField, fdRowsCountField;
	
	private Label        wlRowSeparator;
	private TextVar      wRowSeparator;
	private FormData     fdlRowSeparator, fdRowSeparator;
	
	private Label        wlRowSeparatorFormat;
	private CCombo       wRowSeparatorFormat;
	private FormData     fdlRowSeparatorFormat, fdRowSeparatorFormat;
	
   
	private GetFilesRowsCountMeta input;
    
	private Group wAdditionalGroup,wFilesCountFieldGroup,wRowSeparatorGroup;
	private FormData fdAdditionalGroup,fdFilesCountFieldGroup,fdRowSeparatorGroup;
    
    

	public GetFilesRowsCountDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(GetFilesRowsCountMeta)in;
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
		shell.setText(Messages.getString("GetFilesRowsCountDialog.DialogTitle"));
		
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
		wFileTab.setText(Messages.getString("GetFilesRowsCountDialog.File.Tab"));
		
		wFileComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wFileComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth  = 3;
		fileLayout.marginHeight = 3;
		wFileComp.setLayout(fileLayout);

		// Filename line
		wlFilename=new Label(wFileComp, SWT.RIGHT);
		wlFilename.setText(Messages.getString("GetFilesRowsCountDialog.Filename.Label"));
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(0, 0);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbbFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbbFilename);
		wbbFilename.setText(Messages.getString("GetFilesRowsCountDialog.FilenameBrowse.Button"));
		wbbFilename.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(0, 0);
		wbbFilename.setLayoutData(fdbFilename);

		wbaFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbaFilename);
		wbaFilename.setText(Messages.getString("GetFilesRowsCountDialog.FilenameAdd.Button"));
		wbaFilename.setToolTipText(Messages.getString("GetFilesRowsCountDialog.FilenameAdd.Tooltip"));
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
		wlFilemask.setText(Messages.getString("GetFilesRowsCountDialog.RegExp.Label"));
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
		wlFilenameList.setText(Messages.getString("GetFilesRowsCountDialog.FilenameList.Label"));
 		props.setLook(wlFilenameList);
		fdlFilenameList=new FormData();
		fdlFilenameList.left = new FormAttachment(0, 0);
		fdlFilenameList.top  = new FormAttachment(wFilemask, margin);
		fdlFilenameList.right= new FormAttachment(middle, -margin);
		wlFilenameList.setLayoutData(fdlFilenameList);

		// Buttons to the right of the screen...
		wbdFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbdFilename);
		wbdFilename.setText(Messages.getString("GetFilesRowsCountDialog.FilenameRemove.Button"));
		wbdFilename.setToolTipText(Messages.getString("GetFilesRowsCountDialog.FilenameRemove.Tooltip"));
		fdbdFilename=new FormData();
		fdbdFilename.right = new FormAttachment(100, 0);
		fdbdFilename.top  = new FormAttachment (wFilemask, 40);
		wbdFilename.setLayoutData(fdbdFilename);

		wbeFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbeFilename);
		wbeFilename.setText(Messages.getString("GetFilesRowsCountDialog.FilenameEdit.Button"));
		wbeFilename.setToolTipText(Messages.getString("GetFilesRowsCountDialog.FilenameEdit.Tooltip"));
		fdbeFilename=new FormData();
		fdbeFilename.right = new FormAttachment(100, 0);
		fdbeFilename.top  = new FormAttachment (wbdFilename, margin);
		wbeFilename.setLayoutData(fdbeFilename);
		

		wbShowFiles=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbShowFiles);
		wbShowFiles.setText(Messages.getString("GetFilesRowsCountDialog.ShowFiles.Button"));
		fdbShowFiles=new FormData();
		fdbShowFiles.left   = new FormAttachment(middle, 0);
		fdbShowFiles.bottom = new FormAttachment(100, 0);
		wbShowFiles.setLayoutData(fdbShowFiles);

		ColumnInfo[] colinfo=new ColumnInfo[2];
		colinfo[ 0]=new ColumnInfo(
          Messages.getString("GetFilesRowsCountDialog.Files.Filename.Column"),
          ColumnInfo.COLUMN_TYPE_TEXT,
          false);
		colinfo[ 1]=new ColumnInfo(
          Messages.getString("GetFilesRowsCountDialog.Files.Wildcard.Column"),
          ColumnInfo.COLUMN_TYPE_TEXT,
          false);
		
		colinfo[0].setUsingVariables(true);
		colinfo[1].setUsingVariables(true);
		colinfo[1].setToolTip(Messages.getString("GetFilesRowsCountDialog.Files.Wildcard.Tooltip"));
				
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
		wContentTab.setText(Messages.getString("GetFilesRowsCountDialog.Content.Tab"));

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		wContentComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wContentComp);
		wContentComp.setLayout(contentLayout);
		
        
        
		// /////////////////////////////////
		// START OF Files Count Field GROUP
		// /////////////////////////////////

		wFilesCountFieldGroup = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wFilesCountFieldGroup);
		wFilesCountFieldGroup.setText(Messages.getString("GetFilesRowsCountDialog.Group.CountFilesFieldGroup.Label"));
		
		FormLayout countfilesfieldgroupLayout = new FormLayout();
		countfilesfieldgroupLayout.marginWidth = 10;
		countfilesfieldgroupLayout.marginHeight = 10;
		wFilesCountFieldGroup.setLayout(countfilesfieldgroupLayout);
		
		
		wlRowsCountField=new Label(wFilesCountFieldGroup, SWT.RIGHT);
		wlRowsCountField.setText(Messages.getString("GetFilesRowsCountDialog.RowsCountField.Label"));
 		props.setLook(wlRowsCountField);
		fdlRowsCountField=new FormData();
		fdlRowsCountField.left = new FormAttachment(wInclFilesCount, margin);
		fdlRowsCountField.top  = new FormAttachment(0, margin);
		wlRowsCountField.setLayoutData(fdlRowsCountField);
		wRowsCountField=new TextVar(transMeta,wFilesCountFieldGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wRowsCountField);
 		wRowsCountField.setToolTipText(Messages.getString("GetFilesRowsCountDialog.RowsCountField.Tooltip"));
 		wRowsCountField.addModifyListener(lsMod);
		fdRowsCountField=new FormData();
		fdRowsCountField.left = new FormAttachment(wlRowsCountField, margin);
		fdRowsCountField.top  = new FormAttachment(0, margin);
		fdRowsCountField.right= new FormAttachment(100, 0);
		wRowsCountField.setLayoutData(fdRowsCountField);
		
		fdFilesCountFieldGroup = new FormData();
		fdFilesCountFieldGroup.left = new FormAttachment(0, margin);
		fdFilesCountFieldGroup.top = new FormAttachment(0, margin);
		fdFilesCountFieldGroup.right = new FormAttachment(100, -margin);
		wFilesCountFieldGroup.setLayoutData(fdFilesCountFieldGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF ADDITIONNAL FIELDS  GROUP
		// ///////////////////////////////////////////////////////////
		
		// /////////////////////////////////
		// START OF Row separator GROUP
		// /////////////////////////////////

		wRowSeparatorGroup = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wRowSeparatorGroup);
		wRowSeparatorGroup.setText(Messages.getString("GetFilesRowsCountDialog.Group.RowSeparator.Label"));
		
		FormLayout rowseparatorgroupLayout = new FormLayout();
		rowseparatorgroupLayout.marginWidth = 10;
		rowseparatorgroupLayout.marginHeight = 10;
		wRowSeparatorGroup.setLayout(rowseparatorgroupLayout);
		
		

		
		wlRowSeparatorFormat=new Label(wRowSeparatorGroup, SWT.RIGHT);
        wlRowSeparatorFormat.setText(Messages.getString("GetFilesRowsCountDialog.RowSeparatorFormat.Label"));
        props.setLook(wlRowSeparatorFormat);
        fdlRowSeparatorFormat=new FormData();
        fdlRowSeparatorFormat.left = new FormAttachment(0, 0);
        fdlRowSeparatorFormat.top  = new FormAttachment(wFilesCountFieldGroup, margin);
        fdlRowSeparatorFormat.right= new FormAttachment(middle, -margin);
        wlRowSeparatorFormat.setLayoutData(fdlRowSeparatorFormat);
        wRowSeparatorFormat=new CCombo(wRowSeparatorGroup, SWT.BORDER | SWT.READ_ONLY);
        props.setLook(wRowSeparatorFormat);
        wRowSeparatorFormat.add(Messages.getString("GetFilesRowsCountDialog.RowSeparatorFormat.CR.Label"));
        wRowSeparatorFormat.add(Messages.getString("GetFilesRowsCountDialog.RowSeparatorFormat.LF.Label"));
        wRowSeparatorFormat.add(Messages.getString("GetFilesRowsCountDialog.RowSeparatorFormat.TAB.Label"));
        wRowSeparatorFormat.add(Messages.getString("GetFilesRowsCountDialog.RowSeparatorFormat.CUSTOM.Label"));
        wRowSeparatorFormat.select(0);
        wRowSeparatorFormat.addModifyListener(lsMod);
        fdRowSeparatorFormat=new FormData();
        fdRowSeparatorFormat.left = new FormAttachment(middle, 0);
        fdRowSeparatorFormat.top  = new FormAttachment(wFilesCountFieldGroup, margin);
        fdRowSeparatorFormat.right= new FormAttachment(100, 0);
        wRowSeparatorFormat.setLayoutData(fdRowSeparatorFormat);
        
        wRowSeparatorFormat.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				ActiveRowSeparator();
				
			}
		});

		
		
		wlRowSeparator=new Label(wRowSeparatorGroup, SWT.RIGHT);
		wlRowSeparator.setText(Messages.getString("GetFilesRowsCountDialog.RowSeparator.Label"));
 		props.setLook(wlRowSeparator);
		fdlRowSeparator=new FormData();
		fdlRowSeparator.left = new FormAttachment(wInclFilesCount, margin);
		fdlRowSeparator.top  = new FormAttachment(wRowSeparatorFormat, margin);
		wlRowSeparator.setLayoutData(fdlRowSeparator);
		wRowSeparator=new TextVar(transMeta,wRowSeparatorGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wRowSeparator);
 		wRowSeparator.setToolTipText(Messages.getString("GetFilesRowsCountDialog.RowSeparator.Tooltip"));
 		wRowSeparator.addModifyListener(lsMod);
		fdRowSeparator=new FormData();
		fdRowSeparator.left = new FormAttachment(wlRowSeparator, margin);
		fdRowSeparator.top  = new FormAttachment(wRowSeparatorFormat, margin);
		fdRowSeparator.right= new FormAttachment(100, 0);
		wRowSeparator.setLayoutData(fdRowSeparator);
		
		fdRowSeparatorGroup = new FormData();
		fdRowSeparatorGroup.left = new FormAttachment(0, margin);
		fdRowSeparatorGroup.top = new FormAttachment(wFilesCountFieldGroup, margin);
		fdRowSeparatorGroup.right = new FormAttachment(100, -margin);
		wRowSeparatorGroup.setLayoutData(fdRowSeparatorGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF ROW SEPARATOR GROUP
		// ///////////////////////////////////////////////////////////
		

		
        
		// /////////////////////////////////
		// START OF Additional Fields GROUP
		// /////////////////////////////////

		wAdditionalGroup = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wAdditionalGroup);
		wAdditionalGroup.setText(Messages.getString("GetFilesRowsCountDialog.Group.AdditionalGroup.Label"));
		
		FormLayout additionalgroupLayout = new FormLayout();
		additionalgroupLayout.marginWidth = 10;
		additionalgroupLayout.marginHeight = 10;
		wAdditionalGroup.setLayout(additionalgroupLayout);


		
		wlInclFilesCount=new Label(wAdditionalGroup, SWT.RIGHT);
		wlInclFilesCount.setText(Messages.getString("GetFilesRowsCountDialog.InclCountFiles.Label"));
 		props.setLook(wlInclFilesCount);
		fdlInclFilesCount=new FormData();
		fdlInclFilesCount.left = new FormAttachment(0, 0);
		fdlInclFilesCount.top  = new FormAttachment(wRowSeparatorGroup, margin);
		fdlInclFilesCount.right= new FormAttachment(middle, -margin);
		wlInclFilesCount.setLayoutData(fdlInclFilesCount);
		wInclFilesCount=new Button(wAdditionalGroup, SWT.CHECK );
 		props.setLook(wInclFilesCount);
 		wInclFilesCount.setToolTipText(Messages.getString("GetFilesRowsCountDialog.InclCountFiles.Tooltip"));
		fdFilesCount=new FormData();
		fdFilesCount.left = new FormAttachment(middle, 0);
		fdFilesCount.top  = new FormAttachment(wRowSeparatorGroup, margin);
		wInclFilesCount.setLayoutData(fdFilesCount);

		wlInclFilesCountField=new Label(wAdditionalGroup, SWT.RIGHT);
		wlInclFilesCountField.setText(Messages.getString("GetFilesRowsCountDialog.InclCountFilesField.Label"));
 		props.setLook(wlInclFilesCountField);
		fdlInclFilesCountField=new FormData();
		fdlInclFilesCountField.left = new FormAttachment(wInclFilesCount, margin);
		fdlInclFilesCountField.top  = new FormAttachment(wRowSeparatorGroup, margin);
		wlInclFilesCountField.setLayoutData(fdlInclFilesCountField);
		wInclFilesCountField=new TextVar(transMeta,wAdditionalGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclFilesCountField);
 		wInclFilesCountField.addModifyListener(lsMod);
		fdInclFilesCountField=new FormData();
		fdInclFilesCountField.left = new FormAttachment(wlInclFilesCountField, margin);
		fdInclFilesCountField.top  = new FormAttachment(wRowSeparatorGroup, margin);
		fdInclFilesCountField.right= new FormAttachment(100, 0);
		wInclFilesCountField.setLayoutData(fdInclFilesCountField);
		
	
		fdAdditionalGroup = new FormData();
		fdAdditionalGroup.left = new FormAttachment(0, margin);
		fdAdditionalGroup.top = new FormAttachment(wRowSeparatorGroup, margin);
		fdAdditionalGroup.right = new FormAttachment(100, -margin);
		wAdditionalGroup.setLayoutData(fdAdditionalGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF ADDITIONNAL FIELDS  GROUP
		// ///////////////////////////////////////////////////////////
		
		

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


		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
		
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));

		wPreview=new Button(shell, SWT.PUSH);
		wPreview.setText(Messages.getString("GetFilesRowsCountDialog.Button.PreviewRows"));
		
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));
		
		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, wTabFolder);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsPreview  = new Listener() { public void handleEvent(Event e) { preview();   } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();     } };
		
		wOK.addListener     (SWT.Selection, lsOK     );
		//wGet.addListener    (SWT.Selection, lsGet    );
		wPreview.addListener(SWT.Selection, lsPreview);
		wCancel.addListener (SWT.Selection, lsCancel );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wInclFilesCountField.addSelectionListener( lsDef );

		
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
                    	GetFilesRowsCountMeta tfii = new GetFilesRowsCountMeta();
    					getInfo(tfii);
    					FileInputList fileInputList = tfii.getFiles(transMeta);
    					String files[] = fileInputList.getFileStrings();
    					
    					if (files.length > 0)
    			        {
    			            EnterSelectionDialog esd = new EnterSelectionDialog(shell, files, Messages.getString("GetFilesRowsCountDialog.FilesReadSelection.DialogTitle"), Messages.getString("GetFilesRowsCountDialog.FilesReadSelection.DialogMessage"));
    			            esd.setViewOnly();
    			            esd.open();
    			        }
    					
    					else
    					{
    			            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
    			            mb.setMessage(Messages.getString("GetFilesRowsCountDialog.NoFileFound.DialogMessage"));
    			            mb.setText(Messages.getString("System.Dialog.Error.Title"));
    			            mb.open(); 
    					}
                    }
                    catch(KettleException ex)
                    {
                        new ErrorDialog(shell, Messages.getString("GetFilesRowsCountDialog.ErrorParsingData.DialogTitle"), Messages.getString("GetFilesRowsCountDialog.ErrorParsingData.DialogMessage"), ex);
                    }
				}
			}
		);
		
		
		// Enable/disable the right fields to allow a row number to be added to each row...
		wInclFilesCount.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					setIncludeRownum();
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
						dialog.setFilterExtensions(new String[] {"*"});
						if (wFilename.getText()!=null)
						{
							String fname = "";//StringUtil.environmentSubstitute(wFilename.getText());
							dialog.setFileName( fname );
						}
						
						dialog.setFilterNames(new String[] {Messages.getString("System.FileType.AllFiles")});
						
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
		ActiveRowSeparator();
		input.setChanged(changed);
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	private void ActiveRowSeparator()
	{
		if (wRowSeparatorFormat.getSelectionIndex()==3)
		{
			wRowSeparator.setEnabled(true);
			wlRowSeparator.setEnabled(true);
		}
		else
		{
			wRowSeparator.setEnabled(false);
			wlRowSeparator.setEnabled(false);
		}
		
		
	}
	
	public void setIncludeRownum()
	{
		wlInclFilesCountField.setEnabled(wInclFilesCount.getSelection());
		wInclFilesCountField.setEnabled(wInclFilesCount.getSelection());
	}
	
	

	/**
	 * Read the data from the GetFilesRowsCountMeta object and show it in this dialog.
	 * 
	 * @param in The GetFilesRowsCountMeta object to obtain the data from.
	 */
	public void getData(GetFilesRowsCountMeta in)
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
		wInclFilesCount.setSelection(in.includeCountFiles());

		if (in.getFilesCountFieldName()!=null) wInclFilesCountField.setText(in.getFilesCountFieldName());
		if (in.getRowsCountFieldName()!=null) 
		{
			wRowsCountField.setText(in.getRowsCountFieldName());
		}
		else
		{
			wRowsCountField.setText("rowscount");
		}
		if (in.getRowSeparatorFormat()!=null)
		{
			if (in.getRowSeparatorFormat().equals("CR"))
			{
				wRowSeparatorFormat.select(0);
			}
			else if (in.getRowSeparatorFormat().equals("LF"))
			{
				wRowSeparatorFormat.select(1);
			}
			else if (in.getRowSeparatorFormat().equals("TAB"))
			{
				wRowSeparatorFormat.select(2);
			}
			else
			{
				wRowSeparatorFormat.select(3);
			}
		}
		else
		{
			wRowSeparatorFormat.select(0);
		}
		
		
		if (in.getRowSeparator()!=null) wRowSeparator.setText(in.getRowSeparator());
		
		log.logDebug(toString(), Messages.getString("GetFilesRowsCountDialog.Log.GettingFieldsInfo"));
		
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
            new ErrorDialog(shell, Messages.getString("GetFilesRowsCountDialog.ErrorParsingData.DialogTitle"), Messages.getString("GetFilesRowsCountDialog.ErrorParsingData.DialogMessage"), e);
        }
		dispose();
	}
	
	private void getInfo(GetFilesRowsCountMeta in) throws KettleException
	{
		stepname = wStepname.getText(); // return value

		
		in.setIncludeCountFiles( wInclFilesCount.getSelection() );
		in.setIncludeFilesCountFieldName(wInclFilesCountField.getText() );
		in.setRowsCountFieldName(wRowsCountField.getText() );
		
		if (wRowSeparatorFormat.getSelectionIndex()==0)
		{
			in.setRowSeparatorFormat("CR");
		}
		else if (wRowSeparatorFormat.getSelectionIndex()==1)
		{
			in.setRowSeparatorFormat("LF");
		}

		else if (wRowSeparatorFormat.getSelectionIndex()==2)
		{
			in.setRowSeparatorFormat("TAB");
		}
		else
		{
			in.setRowSeparatorFormat("CUSTOM");
		}
		
		
		int nrFiles     = wFilenameList.getItemCount();

		in.allocate(nrFiles);

		in.setFileName( wFilenameList.getItems(0) );
		in.setFileMask( wFilenameList.getItems(1) );
		if (wRowSeparator.getText().length()>1)
		{
			if (wRowSeparator.getText().substring(0, 1).equals("\\"))
			{
				// Take the 2 first
				wRowSeparator.setText(wRowSeparator.getText().substring(0, 2));
			}
			else
			{
				wRowSeparator.setText(wRowSeparator.getText().substring(0, 1));
			}
		}
		in.setRowSeparator(wRowSeparator.getText());

	
	}
	

	
		
	// Preview the data
	private void preview()
	{
        try
        {
            // Create the XML input step
        	GetFilesRowsCountMeta oneMeta = new GetFilesRowsCountMeta();
            getInfo(oneMeta);
            
            TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
            
            EnterNumberDialog numberDialog = new EnterNumberDialog(shell, 500, Messages.getString("GetFilesRowsCountDialog.NumberRows.DialogTitle"), Messages.getString("GetFilesRowsCountDialog.NumberRows.DialogMessage"));
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
            new ErrorDialog(shell, Messages.getString("GetFilesRowsCountDialog.ErrorPreviewingData.DialogTitle"), Messages.getString("GetFilesRowsCountDialog.ErrorPreviewingData.DialogMessage"), e);
       }
	}
	
	
	public String toString()
	{
		return this.getClass().getName();
	}
	
}