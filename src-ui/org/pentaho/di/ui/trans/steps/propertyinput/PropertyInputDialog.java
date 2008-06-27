/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar, Brahim and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/

/*
 * Created on 24-03-2008
 *
 */

package org.pentaho.di.ui.trans.steps.propertyinput;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Cursor;
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
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.propertyinput.PropertyInputField;
import org.pentaho.di.trans.steps.propertyinput.PropertyInputMeta;
import org.pentaho.di.trans.steps.propertyinput.Messages;
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

public class PropertyInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wFileTab, wContentTab, wFieldsTab;

	private Composite    wFileComp, wContentComp, wFieldsComp;
	private FormData     fdFileComp, fdContentComp, fdFieldsComp;

	private Label        wlFilename;
	private Button       wbbFilename; // Browse: add file or directory

	private Button       wbdFilename; // Delete
	private Button       wbeFilename; // Edit
	private Button       wbaFilename; // Add or change
	private TextVar      wFilename;
	private FormData     fdlFilename, fdbFilename, fdbdFilename, fdbeFilename, 
						fdbaFilename, fdFilename;

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

	private PropertyInputMeta input;
	
	private Group wAdditionalGroup;
	private FormData fdAdditionalGroup,fdlAddResult;
	private Group wOriginFiles,wAddFileResult;
	
	private FormData fdOriginFiles,fdFilenameField,fdlFilenameField,fdAddResult,fdAddFileResult;
    private Button wFileField,wAddResult;
    
    private Label wlFileField,wlFilenameField,wlAddResult;
    private CCombo wFilenameField;
    private FormData fdlFileField,fdFileField;
    
	
	public static final int dateLengths[] = new int[]
	{
		23, 19, 14, 10, 10, 10, 10, 8, 8, 8, 8, 6, 6
	};

	public PropertyInputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(PropertyInputMeta)in;
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
		shell.setText(Messages.getString("PropertyInputDialog.DialogTitle"));
		
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
		wFileTab.setText(Messages.getString("PropertyInputDialog.File.Tab"));
		
		wFileComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wFileComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth  = 3;
		fileLayout.marginHeight = 3;
		wFileComp.setLayout(fileLayout);
		
		
		// ///////////////////////////////
		// START OF Origin files GROUP  //
		///////////////////////////////// 

		wOriginFiles = new Group(wFileComp, SWT.SHADOW_NONE);
		props.setLook(wOriginFiles);
		wOriginFiles.setText(Messages.getString("PropertyInputDialog.wOriginFiles.Label"));
		
		FormLayout OriginFilesgroupLayout = new FormLayout();
		OriginFilesgroupLayout.marginWidth = 10;
		OriginFilesgroupLayout.marginHeight = 10;
		wOriginFiles.setLayout(OriginFilesgroupLayout);
		
		//Is Filename defined in a Field		
		wlFileField = new Label(wOriginFiles, SWT.RIGHT);
		wlFileField.setText(Messages.getString("PropertyInputDialog.FileField.Label"));
		props.setLook(wlFileField);
		fdlFileField = new FormData();
		fdlFileField.left = new FormAttachment(0, -margin);
		fdlFileField.top = new FormAttachment(0, margin);
		fdlFileField.right = new FormAttachment(middle, -2*margin);
		wlFileField.setLayoutData(fdlFileField);
		
		wFileField = new Button(wOriginFiles, SWT.CHECK);
		props.setLook(wFileField);
		wFileField.setToolTipText(Messages.getString("PropertyInputDialog.FileField.Tooltip"));
		fdFileField = new FormData();
		fdFileField.left = new FormAttachment(middle, -margin);
		fdFileField.top = new FormAttachment(0, margin);
		wFileField.setLayoutData(fdFileField);		
		SelectionAdapter lfilefield = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	ActiveFileField();
            	input.setChanged();
            }
        };
        wFileField.addSelectionListener(lfilefield);
        
		// Filename field
		wlFilenameField=new Label(wOriginFiles, SWT.RIGHT);
        wlFilenameField.setText(Messages.getString("PropertyInputDialog.wlFilenameField.Label"));
        props.setLook(wlFilenameField);
        fdlFilenameField=new FormData();
        fdlFilenameField.left = new FormAttachment(0, -margin);
        fdlFilenameField.top  = new FormAttachment(wFileField, margin);
        fdlFilenameField.right= new FormAttachment(middle, -2*margin);
        wlFilenameField.setLayoutData(fdlFilenameField);
        
        
        wFilenameField=new CCombo(wOriginFiles, SWT.BORDER | SWT.READ_ONLY);
        wFilenameField.setEditable(true);
        props.setLook(wFilenameField);
        wFilenameField.addModifyListener(lsMod);
        fdFilenameField=new FormData();
        fdFilenameField.left = new FormAttachment(middle, -margin);
        fdFilenameField.top  = new FormAttachment(wFileField, margin);
        fdFilenameField.right= new FormAttachment(100, -margin);
        wFilenameField.setLayoutData(fdFilenameField);
        wFilenameField.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    setFileField();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );           	
        
		fdOriginFiles = new FormData();
		fdOriginFiles.left = new FormAttachment(0, margin);
		fdOriginFiles.top = new FormAttachment(wFilenameList, margin);
		fdOriginFiles.right = new FormAttachment(100, -margin);
		wOriginFiles.setLayoutData(fdOriginFiles);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Origin files GROUP
		// ///////////////////////////////////////////////////////////		

		
		

		// Filename line
		wlFilename=new Label(wFileComp, SWT.RIGHT);
		wlFilename.setText(Messages.getString("PropertyInputDialog.Filename.Label"));
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(wOriginFiles,margin);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbbFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbbFilename);
		wbbFilename.setText(Messages.getString("PropertyInputDialog.FilenameBrowse.Button"));
		wbbFilename.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(wOriginFiles, margin);
		wbbFilename.setLayoutData(fdbFilename);

		wbaFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbaFilename);
		wbaFilename.setText(Messages.getString("PropertyInputDialog.FilenameAdd.Button"));
		wbaFilename.setToolTipText(Messages.getString("PropertyInputDialog.FilenameAdd.Tooltip"));
		fdbaFilename=new FormData();
		fdbaFilename.right= new FormAttachment(wbbFilename, -margin);
		fdbaFilename.top  = new FormAttachment(wOriginFiles, margin);
		wbaFilename.setLayoutData(fdbaFilename);

	        
		
		wFilename=new TextVar(transMeta,wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right= new FormAttachment(wbaFilename, -margin);
		fdFilename.top  = new FormAttachment(wOriginFiles, margin);
		wFilename.setLayoutData(fdFilename);

		wlFilemask=new Label(wFileComp, SWT.RIGHT);
		wlFilemask.setText(Messages.getString("PropertyInputDialog.RegExp.Label"));
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
		wlFilenameList.setText(Messages.getString("PropertyInputDialog.FilenameList.Label"));
 		props.setLook(wlFilenameList);
		fdlFilenameList=new FormData();
		fdlFilenameList.left = new FormAttachment(0, 0);
		fdlFilenameList.top  = new FormAttachment(wFilemask, margin);
		fdlFilenameList.right= new FormAttachment(middle, -margin);
		wlFilenameList.setLayoutData(fdlFilenameList);

		// Buttons to the right of the screen...
		wbdFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbdFilename);
		wbdFilename.setText(Messages.getString("PropertyInputDialog.FilenameRemove.Button"));
		wbdFilename.setToolTipText(Messages.getString("PropertyInputDialog.FilenameRemove.Tooltip"));
		fdbdFilename=new FormData();
		fdbdFilename.right = new FormAttachment(100, 0);
		fdbdFilename.top  = new FormAttachment (wFilemask, 40);
		wbdFilename.setLayoutData(fdbdFilename);

		wbeFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbeFilename);
		wbeFilename.setText(Messages.getString("PropertyInputDialog.FilenameEdit.Button"));
		wbeFilename.setToolTipText(Messages.getString("PropertyInputDialog.FilenameEdit.Tooltip"));
		fdbeFilename=new FormData();
		fdbeFilename.right = new FormAttachment(100, 0);
		fdbeFilename.top  = new FormAttachment (wbdFilename, margin);
		wbeFilename.setLayoutData(fdbeFilename);
		

		wbShowFiles=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbShowFiles);
		wbShowFiles.setText(Messages.getString("PropertyInputDialog.ShowFiles.Button"));
		fdbShowFiles=new FormData();
		fdbShowFiles.left   = new FormAttachment(middle, 0);
		fdbShowFiles.bottom = new FormAttachment(100, 0);
		wbShowFiles.setLayoutData(fdbShowFiles);

		ColumnInfo[] colinfo=new ColumnInfo[2];
		colinfo[ 0]=new ColumnInfo(
          Messages.getString("PropertyInputDialog.Files.Filename.Column"),
          ColumnInfo.COLUMN_TYPE_TEXT,
          false);
		colinfo[ 1]=new ColumnInfo(
          Messages.getString("PropertyInputDialog.Files.Wildcard.Column"),
          ColumnInfo.COLUMN_TYPE_TEXT,
          false);
		
		colinfo[0].setUsingVariables(true);
		colinfo[1].setUsingVariables(true);
		colinfo[1].setToolTip(Messages.getString("PropertyInputDialog.Files.Wildcard.Tooltip"));
				
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
		wContentTab.setText(Messages.getString("PropertyInputDialog.Content.Tab"));

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		wContentComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wContentComp);
		wContentComp.setLayout(contentLayout);
		
		// /////////////////////////////////
		// START OF Additional Fields GROUP
		// /////////////////////////////////

		wAdditionalGroup = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wAdditionalGroup);
		wAdditionalGroup.setText(Messages.getString("PropertyInputDialog.Group.AdditionalGroup.Label"));
		
		FormLayout additionalgroupLayout = new FormLayout();
		additionalgroupLayout.marginWidth = 10;
		additionalgroupLayout.marginHeight = 10;
		wAdditionalGroup.setLayout(additionalgroupLayout);

		wlInclFilename=new Label(wAdditionalGroup, SWT.RIGHT);
		wlInclFilename.setText(Messages.getString("PropertyInputDialog.InclFilename.Label"));
 		props.setLook(wlInclFilename);
		fdlInclFilename=new FormData();
		fdlInclFilename.left = new FormAttachment(0, 0);
		fdlInclFilename.top  = new FormAttachment(0, margin);
		fdlInclFilename.right= new FormAttachment(middle, -margin);
		wlInclFilename.setLayoutData(fdlInclFilename);
		wInclFilename=new Button(wAdditionalGroup, SWT.CHECK );
 		props.setLook(wInclFilename);
		wInclFilename.setToolTipText(Messages.getString("PropertyInputDialog.InclFilename.Tooltip"));
		fdInclFilename=new FormData();
		fdInclFilename.left = new FormAttachment(middle, 0);
		fdInclFilename.top  = new FormAttachment(0, margin);
		wInclFilename.setLayoutData(fdInclFilename);

		wlInclFilenameField=new Label(wAdditionalGroup, SWT.LEFT);
		wlInclFilenameField.setText(Messages.getString("PropertyInputDialog.InclFilenameField.Label"));
 		props.setLook(wlInclFilenameField);
		fdlInclFilenameField=new FormData();
		fdlInclFilenameField.left = new FormAttachment(wInclFilename, margin);
		fdlInclFilenameField.top  = new FormAttachment(0, margin);
		wlInclFilenameField.setLayoutData(fdlInclFilenameField);
		wInclFilenameField=new TextVar(transMeta,wAdditionalGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclFilenameField);
		wInclFilenameField.addModifyListener(lsMod);
		fdInclFilenameField=new FormData();
		fdInclFilenameField.left = new FormAttachment(wlInclFilenameField , margin);
		fdInclFilenameField.top  = new FormAttachment(0, margin);
		fdInclFilenameField.right= new FormAttachment(100, 0);
		wInclFilenameField.setLayoutData(fdInclFilenameField);
		

	
		wlInclRownum=new Label(wAdditionalGroup, SWT.RIGHT);
		wlInclRownum.setText(Messages.getString("PropertyInputDialog.InclRownum.Label"));
 		props.setLook(wlInclRownum);
		fdlInclRownum=new FormData();
		fdlInclRownum.left = new FormAttachment(0, 0);
		fdlInclRownum.top  = new FormAttachment(wInclFilenameField, margin);
		fdlInclRownum.right= new FormAttachment(middle, -margin);
		wlInclRownum.setLayoutData(fdlInclRownum);
		wInclRownum=new Button(wAdditionalGroup, SWT.CHECK );
 		props.setLook(wInclRownum);
		wInclRownum.setToolTipText(Messages.getString("PropertyInputDialog.InclRownum.Tooltip"));
		fdRownum=new FormData();
		fdRownum.left = new FormAttachment(middle, 0);
		fdRownum.top  = new FormAttachment(wInclFilenameField, margin);
		wInclRownum.setLayoutData(fdRownum);

		wlInclRownumField=new Label(wAdditionalGroup, SWT.RIGHT);
		wlInclRownumField.setText(Messages.getString("PropertyInputDialog.InclRownumField.Label"));
 		props.setLook(wlInclRownumField);
		fdlInclRownumField=new FormData();
		fdlInclRownumField.left = new FormAttachment(wInclRownum, margin);
		fdlInclRownumField.top  = new FormAttachment(wInclFilenameField, margin);
		wlInclRownumField.setLayoutData(fdlInclRownumField);
		wInclRownumField=new TextVar(transMeta,wAdditionalGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclRownumField);
		wInclRownumField.addModifyListener(lsMod);
		fdInclRownumField=new FormData();
		fdInclRownumField.left = new FormAttachment(wlInclRownumField, margin);
		fdInclRownumField.top  = new FormAttachment(wInclFilenameField, margin);
		fdInclRownumField.right= new FormAttachment(100, 0);
		wInclRownumField.setLayoutData(fdInclRownumField);
		
		
		wlResetRownum=new Label(wAdditionalGroup, SWT.RIGHT);
		wlResetRownum.setText(Messages.getString("PropertyInputDialog.ResetRownum.Label"));
 		props.setLook(wlResetRownum);
		fdlResetRownum=new FormData();
		fdlResetRownum.left = new FormAttachment(wInclRownum, margin);
		fdlResetRownum.top  = new FormAttachment(wInclRownumField, margin);
		wlResetRownum.setLayoutData(fdlResetRownum);
		wResetRownum=new Button(wAdditionalGroup, SWT.CHECK );
 		props.setLook(wResetRownum);
		wResetRownum.setToolTipText(Messages.getString("PropertyInputDialog.ResetRownum.Tooltip"));
		fdRownum=new FormData();
		fdRownum.left = new FormAttachment(wlResetRownum, margin);
		fdRownum.top  = new FormAttachment(wInclRownumField, margin);	
		wResetRownum.setLayoutData(fdRownum);
		
		
		fdAdditionalGroup = new FormData();
		fdAdditionalGroup.left = new FormAttachment(0, margin);
		fdAdditionalGroup.top = new FormAttachment(0, margin);
		fdAdditionalGroup.right = new FormAttachment(100, -margin);
		wAdditionalGroup.setLayoutData(fdAdditionalGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF DESTINATION ADDRESS  GROUP
		// ///////////////////////////////////////////////////////////
		
		
		wlLimit=new Label(wContentComp, SWT.RIGHT);
		wlLimit.setText(Messages.getString("PropertyInputDialog.Limit.Label"));
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
		
		// ///////////////////////////////
		// START OF AddFileResult GROUP  //
		///////////////////////////////// 

		wAddFileResult = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wAddFileResult);
		wAddFileResult.setText(Messages.getString("PropertyInputDialog.wAddFileResult.Label"));
		
		FormLayout AddFileResultgroupLayout = new FormLayout();
		AddFileResultgroupLayout.marginWidth = 10;
		AddFileResultgroupLayout.marginHeight = 10;
		wAddFileResult.setLayout(AddFileResultgroupLayout);

		wlAddResult=new Label(wAddFileResult, SWT.RIGHT);
		wlAddResult.setText(Messages.getString("PropertyInputDialog.AddResult.Label"));
 		props.setLook(wlAddResult);
		fdlAddResult=new FormData();
		fdlAddResult.left = new FormAttachment(0, 0);
		fdlAddResult.top  = new FormAttachment(wLimit, margin);
		fdlAddResult.right= new FormAttachment(middle, -margin);
		wlAddResult.setLayoutData(fdlAddResult);
		wAddResult=new Button(wAddFileResult, SWT.CHECK );
 		props.setLook(wAddResult);
		wAddResult.setToolTipText(Messages.getString("PropertyInputDialog.AddResult.Tooltip"));
		fdAddResult=new FormData();
		fdAddResult.left = new FormAttachment(middle, 0);
		fdAddResult.top  = new FormAttachment(wLimit, margin);
		wAddResult.setLayoutData(fdAddResult);

		fdAddFileResult = new FormData();
		fdAddFileResult.left = new FormAttachment(0, margin);
		fdAddFileResult.top = new FormAttachment(wLimit, margin);
		fdAddFileResult.right = new FormAttachment(100, -margin);
		wAddFileResult.setLayoutData(fdAddFileResult);
			
		// ///////////////////////////////////////////////////////////
		// / END OF AddFileResult GROUP
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


		// Fields tab...
		//
		wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
		wFieldsTab.setText(Messages.getString("PropertyInputDialog.Fields.Tab"));
		
		FormLayout fieldsLayout = new FormLayout ();
		fieldsLayout.marginWidth  = Const.FORM_MARGIN;
		fieldsLayout.marginHeight = Const.FORM_MARGIN;
		
		wFieldsComp = new Composite(wTabFolder, SWT.NONE);
		wFieldsComp.setLayout(fieldsLayout);
 		props.setLook(wFieldsComp);
		
 		wGet=new Button(wFieldsComp, SWT.PUSH);
		wGet.setText(Messages.getString("PropertyInputDialog.GetFields.Button"));
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
         Messages.getString("PropertyInputDialog.FieldsTable.Name.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
         //new ColumnInfo(Messages.getString("PropertyInputDialog.FieldsTable.Attribut.Column"),ColumnInfo.COLUMN_TYPE_TEXT,false),
         
         new ColumnInfo(Messages.getString("PropertyInputDialog.FieldsTable.Attribut.Column"),ColumnInfo.COLUMN_TYPE_CCOMBO,PropertyInputField.ColumnDesc, false),
         
		 new ColumnInfo(Messages.getString("PropertyInputDialog.FieldsTable.Type.Column"),ColumnInfo.COLUMN_TYPE_CCOMBO,ValueMeta.getTypes(),true ),
		 new ColumnInfo(Messages.getString("PropertyInputDialog.FieldsTable.Format.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,formats),
         new ColumnInfo(
         Messages.getString("PropertyInputDialog.FieldsTable.Length.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("PropertyInputDialog.FieldsTable.Precision.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("PropertyInputDialog.FieldsTable.Currency.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("PropertyInputDialog.FieldsTable.Decimal.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("PropertyInputDialog.FieldsTable.Group.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("PropertyInputDialog.FieldsTable.TrimType.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         formats,//PropertyInputField.trimTypeDesc,
         true ),
			 new ColumnInfo(
         Messages.getString("PropertyInputDialog.FieldsTable.Repeat.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         new String[] { Messages.getString("System.Combo.Yes"), Messages.getString("System.Combo.No") },
         true ),
     
    };
		
		colinf[0].setUsingVariables(true);
		colinf[0].setToolTip(Messages.getString("PropertyInputDialog.FieldsTable.Name.Column.Tooltip"));
		colinf[1].setToolTip(Messages.getString("PropertyInputDialog.FieldsTable.Attribut.Column.Tooltip"));
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
		wPreview.setText(Messages.getString("PropertyInputDialog.Button.PreviewRows"));
		
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
    					PropertyInputMeta tfii = new PropertyInputMeta();
    					getInfo(tfii);
    					FileInputList fileInputList = tfii.getFiles(transMeta);
    					String files[] = fileInputList.getFileStrings();
    					
    					if (files.length > 0)
    			        {
    			            EnterSelectionDialog esd = new EnterSelectionDialog(shell, files, Messages.getString("PropertyInputDialog.FilesReadSelection.DialogTitle"), Messages.getString("PropertyInputDialog.FilesReadSelection.DialogMessage"));
    			            esd.setViewOnly();
    			            esd.open();
    			        }
    					
    					else
    					{
    			            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
    			            mb.setMessage(Messages.getString("PropertyInputDialog.NoFileFound.DialogMessage"));
    			            mb.setText(Messages.getString("System.Dialog.Error.Title"));
    			            mb.open(); 
    					}
                    }
                    catch(KettleException ex)
                    {
                        new ErrorDialog(shell, Messages.getString("PropertyInputDialog.ErrorParsingData.DialogTitle"), Messages.getString("PropertyInputDialog.ErrorParsingData.DialogMessage"), ex);
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
						dialog.setFilterExtensions(new String[] {"*.properties;*.PROPERTIES", "*"});
						if (wFilename.getText()!=null)
						{
							String fname = "";
							dialog.setFileName( fname );
						}
						
						dialog.setFilterNames(new String[] {Messages.getString("PropertyInputDialog.FileType.PropertiesFiles"), Messages.getString("System.FileType.AllFiles")});
						
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
		ActiveFileField();
		wFields.optWidth(true);
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	 private void setFileField()
	 {
		 try{
	           
			 wFilenameField.removeAll();
				
			 RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r!=null)
				{
		             r.getFieldNames();
		             
		             for (int i=0;i<r.getFieldNames().length;i++)
						{	
		            	 wFilenameField.add(r.getFieldNames()[i]);					
							
						}
				}
			 
			
		 }catch(KettleException ke){
				new ErrorDialog(shell, Messages.getString("PropertyInputDialog.FailedToGetFields.DialogTitle"), Messages.getString("PropertyInputDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
	 }
	private void get()
	{
		RowMetaInterface fields = new RowMeta();
		
        try
        {
        	
        	PropertyInputMeta meta = new PropertyInputMeta();
    		getInfo(meta);
        	
            
            FileInputList inputList = meta.getFiles(transMeta);

            if (inputList.getFiles().size()>0)
            {

				ValueMetaInterface field = new ValueMeta(PropertyInputField.getColumnDesc(0), ValueMetaInterface.TYPE_STRING);
				fields.addValueMeta(field);
				field = new ValueMeta(PropertyInputField.getColumnDesc(1), ValueMetaInterface.TYPE_STRING);
				fields.addValueMeta(field);

            }	
    					
		}
        catch(Exception e)
		{
    		 new ErrorDialog(shell, Messages.getString("System.Dialog.Error.Title"), Messages.getString("PropertyInputDialog.ErrorReadingFile.DialogMessage", e.toString()), e);
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
    			mb.setMessage(Messages.getString("PropertyInputDialog.UnableToFindFields.DialogTitle"));
    			mb.setText(Messages.getString("PropertyInputDialog.UnableToFindFields.DialogMessage"));
    			mb.open(); 
    		}
            
 
	}
	private void ActiveFileField()
	{
		wlFilenameField.setEnabled(wFileField.getSelection());
		wlFilenameField.setEnabled(wFileField.getSelection());
			
		wlFilename.setEnabled(!wFileField.getSelection());
		wbbFilename.setEnabled(!wFileField.getSelection());
		wbaFilename.setEnabled(!wFileField.getSelection());		
		wFilename.setEnabled(!wFileField.getSelection());		
		wlFilemask.setEnabled(!wFileField.getSelection());		
		wFilemask.setEnabled(!wFileField.getSelection());		
		wlFilenameList.setEnabled(!wFileField.getSelection());		
		wbdFilename.setEnabled(!wFileField.getSelection());
		wbeFilename.setEnabled(!wFileField.getSelection());
		wbShowFiles.setEnabled(!wFileField.getSelection());
		wlFilenameList.setEnabled(!wFileField.getSelection());
		wFilenameList.setEnabled(!wFileField.getSelection());
		if(wFileField.getSelection()) wInclFilename.setSelection(false);
		wInclFilename.setEnabled(!wFileField.getSelection());
		wlInclFilename.setEnabled(!wFileField.getSelection());
		wLimit.setEnabled(!wFileField.getSelection());	
		wPreview.setEnabled(!wFileField.getSelection());
		wGet.setEnabled(!wFileField.getSelection());
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
	


	/**
	 * Read the data from the TextFileInputMeta object and show it in this dialog.
	 * 
	 * @param in The TextFileInputMeta object to obtain the data from.
	 */
	public void getData(PropertyInputMeta in)
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
		wInclRownum.setSelection(in.includeRowNumber());
		wAddResult.setSelection(in.isAddResultFile());
		
		wFileField.setSelection(in.isFileField());
		
		if (in.getFilenameField()!=null) wInclFilenameField.setText(in.getFilenameField());
		if (in.getDynamicFilenameField()!=null) wFilenameField.setText(in.getDynamicFilenameField());
		
		
		if (in.getRowNumberField()!=null) wInclRownumField.setText(in.getRowNumberField());
		wResetRownum.setSelection(in.resetRowNumber());
		wLimit.setText(""+in.getRowLimit());

		log.logDebug(toString(), Messages.getString("PropertyInputDialog.Log.GettingFieldsInfo"));
		for (int i=0;i<in.getInputFields().length;i++)
		{
		    PropertyInputField field = in.getInputFields()[i];
		    
            if (field!=null)
            {
    			TableItem item  = wFields.table.getItem(i);
    			String name     = field.getName();
    			String column	= field.getColumnDesc();
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
                if (column  !=null) item.setText( 2, column);
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
            new ErrorDialog(shell, Messages.getString("PropertyInputDialog.ErrorParsingData.DialogTitle"), Messages.getString("PropertyInputDialog.ErrorParsingData.DialogMessage"), e);
        }
		dispose();
	}
	
	private void getInfo(PropertyInputMeta in) throws KettleException
	{
		stepname = wStepname.getText(); // return value

		// copy info to PropertyInputMeta class (input)
		in.setRowLimit( Const.toLong(wLimit.getText(), 0L) );
		in.setIncludeFilename( wInclFilename.getSelection() );
		in.setFilenameField( wInclFilenameField.getText() );
		in.setIncludeRowNumber( wInclRownum.getSelection() );
		in.setAddResultFile( wAddResult.getSelection() );
		
		in.setDynamicFilenameField( wFilenameField.getText() );
		in.setFileField(wFileField.getSelection() );
		in.setRowNumberField( wInclRownumField.getText() );
		in.setResetRowNumber( wResetRownum.getSelection() );
		int nrFiles     = wFilenameList.getItemCount();
		int nrFields    = wFields.nrNonEmpty();
		in.allocate(nrFiles, nrFields);

		in.setFileName( wFilenameList.getItems(0) );
		in.setFileMask( wFilenameList.getItems(1) );

		for (int i=0;i<nrFields;i++)
		{
		    PropertyInputField field = new PropertyInputField();
		    
			TableItem item  = wFields.getNonEmpty(i);
            
			field.setName( item.getText(1) );
			field.setColumn( PropertyInputField.getColumnByDesc(item.getText(2)) );
			field.setType(ValueMeta.getType(item.getText(3)));
			field.setFormat( item.getText(4) );
			field.setLength( Const.toInt(item.getText(5), -1) );
			field.setPrecision( Const.toInt(item.getText(6), -1) );
			field.setCurrencySymbol( item.getText(7) );
			field.setDecimalSymbol( item.getText(8) );
			field.setGroupSymbol( item.getText(9) );
			field.setTrimType( PropertyInputField.getTrimTypeByDesc(item.getText(10)) );
			field.setRepeated( Messages.getString("System.Combo.Yes").equalsIgnoreCase(item.getText(11)) );		
            
			in.getInputFields()[i] = field;
		}	
	}
	

	
		
	// Preview the data
	private void preview()
	{
        try
        {
            
            PropertyInputMeta oneMeta = new PropertyInputMeta();
            getInfo(oneMeta);
      

        
            TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
            
            EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), Messages.getString("PropertyInputDialog.NumberRows.DialogTitle"), Messages.getString("PropertyInputDialog.NumberRows.DialogMessage"));
            
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
            new ErrorDialog(shell, Messages.getString("PropertyInputDialog.ErrorPreviewingData.DialogTitle"), Messages.getString("PropertyInputDialog.ErrorPreviewingData.DialogMessage"), e);
       }
	}

	public String toString()
	{
		return this.getClass().getName();
	}
	
	
}