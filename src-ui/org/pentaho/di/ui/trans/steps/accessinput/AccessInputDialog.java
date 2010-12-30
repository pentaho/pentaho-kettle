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
import java.util.Map;
import java.util.Set;

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
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.accessinput.AccessInputField;
import org.pentaho.di.trans.steps.accessinput.AccessInputMeta;
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

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

public class AccessInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = AccessInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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
	private FormData     fdlFilename, fdbFilename, fdbdFilename, fdbeFilename, 
						fdbaFilename, fdFilename, fdbTablename;

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
	
	private Label wlExcludeFilemask;
	private TextVar wExcludeFilemask;
	private FormData fdlExcludeFilemask, fdExcludeFilemask;

	
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
	private FormData fdAdditionalGroup,fdlAddResult;
	private Group wOriginFiles,wAddFileResult;
	
	private FormData fdOriginFiles,fdFilenameField,fdlFilenameField,fdAddResult,fdAddFileResult;
    private Button wFileField,wAddResult;
    
    private Label wlFileField,wlFilenameField,wlAddResult;
    private CCombo wFilenameField;
    private FormData fdlFileField,fdFileField;
    
    
    private CTabItem     wAdditionalFieldsTab;
    private Composite   wAdditionalFieldsComp;
    private FormData	fdAdditionalFieldsComp;
    
    private Label	    wlShortFileFieldName;
    private FormData	fdlShortFileFieldName;
    private TextVar		wShortFileFieldName;
    private FormData    fdShortFileFieldName;
    private Label	    wlPathFieldName;
    private FormData	fdlPathFieldName;
    private TextVar		wPathFieldName;
    private FormData    fdPathFieldName;

    private Label	    wlIsHiddenName;
    private FormData	fdlIsHiddenName;
    private TextVar		wIsHiddenName;
    private FormData    fdIsHiddenName;
    private Label	    wlLastModificationTimeName;
    private FormData	fdlLastModificationTimeName;
    private TextVar		wLastModificationTimeName;
    private FormData    fdLastModificationTimeName;
    private Label	    wlUriName;
    private FormData	fdlUriName;
    private TextVar		wUriName;
    private FormData    fdUriName;
    private Label	    wlRootUriName;
    private FormData	fdlRootUriName;
    private TextVar		wRootUriName;
    private FormData    fdRootUriName;
    private Label	    wlExtensionFieldName;
    private FormData	fdlExtensionFieldName;
    private TextVar		wExtensionFieldName;
    private FormData    fdExtensionFieldName;
    private Label	    wlSizeFieldName;
    private FormData	fdlSizeFieldName;
    private TextVar		wSizeFieldName;
    private FormData    fdSizeFieldName;
    
	private int middle;
	private int margin;
	private ModifyListener lsMod;
    
	
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

		lsMod = new ModifyListener() 
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
		shell.setText(BaseMessages.getString(PKG, "AccessInputDialog.DialogTitle"));
		
		middle = props.getMiddlePct();
		margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
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
		wFileTab.setText(BaseMessages.getString(PKG, "AccessInputDialog.File.Tab"));
		
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
		wOriginFiles.setText(BaseMessages.getString(PKG, "AccessInputDialog.wOriginFiles.Label"));
		
		FormLayout OriginFilesgroupLayout = new FormLayout();
		OriginFilesgroupLayout.marginWidth = 10;
		OriginFilesgroupLayout.marginHeight = 10;
		wOriginFiles.setLayout(OriginFilesgroupLayout);
		
		//Is Filename defined in a Field		
		wlFileField = new Label(wOriginFiles, SWT.RIGHT);
		wlFileField.setText(BaseMessages.getString(PKG, "AccessInputDialog.FileField.Label"));
		props.setLook(wlFileField);
		fdlFileField = new FormData();
		fdlFileField.left = new FormAttachment(0, -margin);
		fdlFileField.top = new FormAttachment(0, margin);
		fdlFileField.right = new FormAttachment(middle, -2*margin);
		wlFileField.setLayoutData(fdlFileField);
		
		
		wFileField = new Button(wOriginFiles, SWT.CHECK);
		props.setLook(wFileField);
		wFileField.setToolTipText(BaseMessages.getString(PKG, "AccessInputDialog.FileField.Tooltip"));
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
        wlFilenameField.setText(BaseMessages.getString(PKG, "AccessInputDialog.wlFilenameField.Label"));
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
		wlFilename.setText(BaseMessages.getString(PKG, "AccessInputDialog.Filename.Label"));
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(wOriginFiles,margin);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbbFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbbFilename);
		wbbFilename.setText(BaseMessages.getString(PKG, "AccessInputDialog.FilenameBrowse.Button"));
		wbbFilename.setToolTipText(BaseMessages.getString(PKG, "System.Tooltip.BrowseForFileOrDirAndAdd"));
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(wOriginFiles, margin);
		wbbFilename.setLayoutData(fdbFilename);

		wbaFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbaFilename);
		wbaFilename.setText(BaseMessages.getString(PKG, "AccessInputDialog.FilenameAdd.Button"));
		wbaFilename.setToolTipText(BaseMessages.getString(PKG, "AccessInputDialog.FilenameAdd.Tooltip"));
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
		wlFilemask.setText(BaseMessages.getString(PKG, "AccessInputDialog.RegExp.Label"));
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

		wlExcludeFilemask = new Label(wFileComp, SWT.RIGHT);
		wlExcludeFilemask.setText(BaseMessages.getString(PKG, "AccessInputDialog.ExcludeFilemask.Label"));
		props.setLook(wlExcludeFilemask);
		fdlExcludeFilemask = new FormData();
		fdlExcludeFilemask.left = new FormAttachment(0, 0);
		fdlExcludeFilemask.top = new FormAttachment(wFilemask, margin);
		fdlExcludeFilemask.right = new FormAttachment(middle, -margin);
		wlExcludeFilemask.setLayoutData(fdlExcludeFilemask);
		wExcludeFilemask = new TextVar(transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wExcludeFilemask);
		wExcludeFilemask.addModifyListener(lsMod);
		fdExcludeFilemask = new FormData();
		fdExcludeFilemask.left = new FormAttachment(middle, 0);
		fdExcludeFilemask.top = new FormAttachment(wFilemask, margin);
		fdExcludeFilemask.right = new FormAttachment(wFilename, 0, SWT.RIGHT);
		wExcludeFilemask.setLayoutData(fdExcludeFilemask);

		
		// Filename list line
		wlFilenameList=new Label(wFileComp, SWT.RIGHT);
		wlFilenameList.setText(BaseMessages.getString(PKG, "AccessInputDialog.FilenameList.Label"));
 		props.setLook(wlFilenameList);
		fdlFilenameList=new FormData();
		fdlFilenameList.left = new FormAttachment(0, 0);
		fdlFilenameList.top  = new FormAttachment(wExcludeFilemask, margin);
		fdlFilenameList.right= new FormAttachment(middle, -margin);
		wlFilenameList.setLayoutData(fdlFilenameList);

		// Buttons to the right of the screen...
		wbdFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbdFilename);
		wbdFilename.setText(BaseMessages.getString(PKG, "AccessInputDialog.FilenameRemove.Button"));
		wbdFilename.setToolTipText(BaseMessages.getString(PKG, "AccessInputDialog.FilenameRemove.Tooltip"));
		fdbdFilename=new FormData();
		fdbdFilename.right = new FormAttachment(100, 0);
		fdbdFilename.top  = new FormAttachment (wExcludeFilemask, 40);
		wbdFilename.setLayoutData(fdbdFilename);

		wbeFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbeFilename);
		wbeFilename.setText(BaseMessages.getString(PKG, "AccessInputDialog.FilenameEdit.Button"));
		wbeFilename.setToolTipText(BaseMessages.getString(PKG, "AccessInputDialog.FilenameEdit.Tooltip"));
		fdbeFilename=new FormData();
		fdbeFilename.right = new FormAttachment(100, 0);
		fdbeFilename.top  = new FormAttachment (wbdFilename, margin);
		wbeFilename.setLayoutData(fdbeFilename);
		

		wbShowFiles=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbShowFiles);
		wbShowFiles.setText(BaseMessages.getString(PKG, "AccessInputDialog.ShowFiles.Button"));
		fdbShowFiles=new FormData();
		fdbShowFiles.left   = new FormAttachment(middle, 0);
		fdbShowFiles.bottom = new FormAttachment(100, 0);
		wbShowFiles.setLayoutData(fdbShowFiles);

		ColumnInfo[] colinfo=new ColumnInfo[5];
		colinfo[0]=new ColumnInfo(
          BaseMessages.getString(PKG, "AccessInputDialog.Files.Filename.Column"),
          ColumnInfo.COLUMN_TYPE_TEXT,
          false);
		colinfo[1]=new ColumnInfo(
          BaseMessages.getString(PKG, "AccessInputDialog.Files.Wildcard.Column"),
          ColumnInfo.COLUMN_TYPE_TEXT,
          false);
		colinfo[2]=new ColumnInfo(BaseMessages.getString(PKG, "AccessInputDialog.Files.ExcludeWildcard.Column"),ColumnInfo.COLUMN_TYPE_TEXT, false);
		
		colinfo[3]=new ColumnInfo(BaseMessages.getString(PKG, "AccessInputDialog.Required.Column"), 
				ColumnInfo.COLUMN_TYPE_CCOMBO,  AccessInputMeta.RequiredFilesDesc );
		colinfo[4]=new ColumnInfo(BaseMessages.getString(PKG, "AccessInputDialog.IncludeSubDirs.Column"), 
				ColumnInfo.COLUMN_TYPE_CCOMBO,  AccessInputMeta.RequiredFilesDesc  );
		
		
		colinfo[0].setUsingVariables(true);
		colinfo[1].setUsingVariables(true);
		colinfo[1].setToolTip(BaseMessages.getString(PKG, "AccessInputDialog.Files.Wildcard.Tooltip"));
		colinfo[2].setUsingVariables(true);
		colinfo[2].setToolTip(BaseMessages.getString(PKG, "AccessInputDialog.Files.ExcludeWildcard.Tooltip"));
		colinfo[3].setToolTip(BaseMessages.getString(PKG, "AccessInputDialog.Required.Tooltip"));
		colinfo[4].setToolTip(BaseMessages.getString(PKG, "AccessInputDialog.IncludeSubDirs.Tooltip"));
				
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
		fdFilenameList.top    = new FormAttachment(wExcludeFilemask, margin);
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
		wContentTab.setText(BaseMessages.getString(PKG, "AccessInputDialog.Content.Tab"));

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		wContentComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wContentComp);
		wContentComp.setLayout(contentLayout);
		
		wbbTablename=new Button(wContentComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbbTablename);
 		wbbTablename.setText(BaseMessages.getString(PKG, "AccessInputDialog.TableBrowse.Button"));
 		wbbTablename.setToolTipText(BaseMessages.getString(PKG, "AccessInputDialog.TableBrowse.Tooltip"));
		fdbTablename=new FormData();
		fdbTablename.right= new FormAttachment(100, 0);
		fdbTablename.top  = new FormAttachment(0, 0);
		wbbTablename.setLayoutData(fdbTablename);

		wbbTablename.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getTableName(); } } );
		
		wlTable=new Label(wContentComp, SWT.RIGHT);
        wlTable.setText(BaseMessages.getString(PKG, "AccessInputDialog.Table.Label"));
        props.setLook(wlTable);
        fdlTable=new FormData();
        fdlTable.left = new FormAttachment(0, 0);
        fdlTable.top  = new FormAttachment(0, margin);
        fdlTable.right= new FormAttachment(middle, -margin);
        wlTable.setLayoutData(fdlTable);
        wTable=new TextVar(transMeta,wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wTable.setToolTipText(BaseMessages.getString(PKG, "AccessInputDialog.Table.Tooltip"));
        props.setLook(wTable);
        wTable.addModifyListener(lsMod);
        fdTable=new FormData();
        fdTable.left = new FormAttachment(middle, 0);
        fdTable.top  = new FormAttachment(0, margin);
        fdTable.right= new FormAttachment(wbbTablename, -margin);
        wTable.setLayoutData(fdTable);       

		// /////////////////////////////////
		// START OF Additional Fields GROUP
		// /////////////////////////////////

		wAdditionalGroup = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wAdditionalGroup);
		wAdditionalGroup.setText(BaseMessages.getString(PKG, "AccessInputDialog.Group.AdditionalGroup.Label"));
		
		FormLayout additionalgroupLayout = new FormLayout();
		additionalgroupLayout.marginWidth = 10;
		additionalgroupLayout.marginHeight = 10;
		wAdditionalGroup.setLayout(additionalgroupLayout);

		wlInclFilename=new Label(wAdditionalGroup, SWT.RIGHT);
		wlInclFilename.setText(BaseMessages.getString(PKG, "AccessInputDialog.InclFilename.Label"));
 		props.setLook(wlInclFilename);
		fdlInclFilename=new FormData();
		fdlInclFilename.left = new FormAttachment(0, 0);
		fdlInclFilename.top  = new FormAttachment(wTable, 2*margin);
		fdlInclFilename.right= new FormAttachment(middle, -margin);
		wlInclFilename.setLayoutData(fdlInclFilename);
		wInclFilename=new Button(wAdditionalGroup, SWT.CHECK );
 		props.setLook(wInclFilename);
		wInclFilename.setToolTipText(BaseMessages.getString(PKG, "AccessInputDialog.InclFilename.Tooltip"));
		fdInclFilename=new FormData();
		fdInclFilename.left = new FormAttachment(middle, 0);
		fdInclFilename.top  = new FormAttachment(wTable, 2*margin);
		wInclFilename.setLayoutData(fdInclFilename);

		wlInclFilenameField=new Label(wAdditionalGroup, SWT.LEFT);
		wlInclFilenameField.setText(BaseMessages.getString(PKG, "AccessInputDialog.InclFilenameField.Label"));
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
		wlInclTablename.setText(BaseMessages.getString(PKG, "AccessInputDialog.InclTablename.Label"));
 		props.setLook(wlInclTablename);
		fdlInclTablename=new FormData();
		fdlInclTablename.left = new FormAttachment(0, 0);
		fdlInclTablename.top  = new FormAttachment(wInclFilenameField, margin);
		fdlInclTablename.right= new FormAttachment(middle, -margin);
		wlInclTablename.setLayoutData(fdlInclTablename);
		wInclTablename=new Button(wAdditionalGroup, SWT.CHECK );
 		props.setLook(wInclTablename);
		wInclTablename.setToolTipText(BaseMessages.getString(PKG, "AccessInputDialog.InclTablename.Tooltip"));
		fdInclTablename=new FormData();
		fdInclTablename.left = new FormAttachment(middle, 0);
		fdInclTablename.top  = new FormAttachment(wInclFilenameField, margin);
		wInclTablename.setLayoutData(fdInclTablename);
		
		
		wlInclTablenameField=new Label(wAdditionalGroup, SWT.LEFT);
		wlInclTablenameField.setText(BaseMessages.getString(PKG, "AccessInputDialog.InclTablenameField.Label"));
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
		wlInclRownum.setText(BaseMessages.getString(PKG, "AccessInputDialog.InclRownum.Label"));
 		props.setLook(wlInclRownum);
		fdlInclRownum=new FormData();
		fdlInclRownum.left = new FormAttachment(0, 0);
		fdlInclRownum.top  = new FormAttachment(wInclTablenameField, margin);
		fdlInclRownum.right= new FormAttachment(middle, -margin);
		wlInclRownum.setLayoutData(fdlInclRownum);
		wInclRownum=new Button(wAdditionalGroup, SWT.CHECK );
 		props.setLook(wInclRownum);
		wInclRownum.setToolTipText(BaseMessages.getString(PKG, "AccessInputDialog.InclRownum.Tooltip"));
		fdRownum=new FormData();
		fdRownum.left = new FormAttachment(middle, 0);
		fdRownum.top  = new FormAttachment(wInclTablenameField, margin);
		wInclRownum.setLayoutData(fdRownum);

		wlInclRownumField=new Label(wAdditionalGroup, SWT.RIGHT);
		wlInclRownumField.setText(BaseMessages.getString(PKG, "AccessInputDialog.InclRownumField.Label"));
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
		wlResetRownum.setText(BaseMessages.getString(PKG, "AccessInputDialog.ResetRownum.Label"));
 		props.setLook(wlResetRownum);
		fdlResetRownum=new FormData();
		fdlResetRownum.left = new FormAttachment(wInclRownum, margin);
		fdlResetRownum.top  = new FormAttachment(wInclRownumField, margin);
		wlResetRownum.setLayoutData(fdlResetRownum);
		wResetRownum=new Button(wAdditionalGroup, SWT.CHECK );
 		props.setLook(wResetRownum);
		wResetRownum.setToolTipText(BaseMessages.getString(PKG, "AccessInputDialog.ResetRownum.Tooltip"));
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
		wlLimit.setText(BaseMessages.getString(PKG, "AccessInputDialog.Limit.Label"));
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
		wAddFileResult.setText(BaseMessages.getString(PKG, "AccessInputDialog.wAddFileResult.Label"));
		
		FormLayout AddFileResultgroupLayout = new FormLayout();
		AddFileResultgroupLayout.marginWidth = 10;
		AddFileResultgroupLayout.marginHeight = 10;
		wAddFileResult.setLayout(AddFileResultgroupLayout);

		wlAddResult=new Label(wAddFileResult, SWT.RIGHT);
		wlAddResult.setText(BaseMessages.getString(PKG, "AccessInputDialog.AddResult.Label"));
 		props.setLook(wlAddResult);
		fdlAddResult=new FormData();
		fdlAddResult.left = new FormAttachment(0, 0);
		fdlAddResult.top  = new FormAttachment(wLimit, margin);
		fdlAddResult.right= new FormAttachment(middle, -margin);
		wlAddResult.setLayoutData(fdlAddResult);
		wAddResult=new Button(wAddFileResult, SWT.CHECK );
 		props.setLook(wAddResult);
		wAddResult.setToolTipText(BaseMessages.getString(PKG, "AccessInputDialog.AddResult.Tooltip"));
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
		wFieldsTab.setText(BaseMessages.getString(PKG, "AccessInputDialog.Fields.Tab"));
		
		FormLayout fieldsLayout = new FormLayout ();
		fieldsLayout.marginWidth  = Const.FORM_MARGIN;
		fieldsLayout.marginHeight = Const.FORM_MARGIN;
		
		wFieldsComp = new Composite(wTabFolder, SWT.NONE);
		wFieldsComp.setLayout(fieldsLayout);
 		props.setLook(wFieldsComp);
		
 		wGet=new Button(wFieldsComp, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "AccessInputDialog.GetFields.Button"));
		fdGet=new FormData();
		fdGet.left=new FormAttachment(50, 0);
		fdGet.bottom =new FormAttachment(100, 0);
		wGet.setLayoutData(fdGet);
		
		final int FieldsRows=input.getInputFields().length;
		
		ColumnInfo[] colinf=new ColumnInfo[]
            {
			 new ColumnInfo(BaseMessages.getString(PKG, "AccessInputDialog.FieldsTable.Name.Column"), ColumnInfo.COLUMN_TYPE_TEXT, false),
         new ColumnInfo(BaseMessages.getString(PKG, "AccessInputDialog.FieldsTable.Attribut.Column"),ColumnInfo.COLUMN_TYPE_TEXT,false),
		 new ColumnInfo(BaseMessages.getString(PKG, "AccessInputDialog.FieldsTable.Type.Column"),ColumnInfo.COLUMN_TYPE_CCOMBO,ValueMeta.getTypes(),true ),
		 new ColumnInfo(BaseMessages.getString(PKG, "AccessInputDialog.FieldsTable.Format.Column"),  ColumnInfo.COLUMN_TYPE_FORMAT, 3),
         new ColumnInfo(
         BaseMessages.getString(PKG, "AccessInputDialog.FieldsTable.Length.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         BaseMessages.getString(PKG, "AccessInputDialog.FieldsTable.Precision.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         BaseMessages.getString(PKG, "AccessInputDialog.FieldsTable.Currency.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         BaseMessages.getString(PKG, "AccessInputDialog.FieldsTable.Decimal.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         BaseMessages.getString(PKG, "AccessInputDialog.FieldsTable.Group.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         BaseMessages.getString(PKG, "AccessInputDialog.FieldsTable.TrimType.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         AccessInputField.trimTypeDesc,
         true ),
			 new ColumnInfo(
         BaseMessages.getString(PKG, "AccessInputDialog.FieldsTable.Repeat.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         new String[] { BaseMessages.getString(PKG, "System.Combo.Yes"), BaseMessages.getString(PKG, "System.Combo.No") },
         true ),
     
    };
		
		colinf[0].setUsingVariables(true);
		colinf[0].setToolTip(BaseMessages.getString(PKG, "AccessInputDialog.FieldsTable.Name.Column.Tooltip"));
		colinf[1].setUsingVariables(true);
		colinf[1].setToolTip(BaseMessages.getString(PKG, "AccessInputDialog.FieldsTable.Attribut.Column.Tooltip"));
		
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
		
        addAdditionalFieldsTab();
		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
		
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));

		wPreview=new Button(shell, SWT.PUSH);
		wPreview.setText(BaseMessages.getString(PKG, "AccessInputDialog.Button.PreviewRows"));
		
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
		
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
				wFilenameList.add(new String[] { wFilename.getText(), wFilemask.getText(), wExcludeFilemask.getText(),AccessInputMeta.RequiredFilesCode[0], AccessInputMeta.RequiredFilesCode[0]} );
				wFilename.setText("");
				wFilemask.setText("");
				wExcludeFilemask.setText("");
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
					wExcludeFilemask.setText(string[2]);
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
    			            EnterSelectionDialog esd = new EnterSelectionDialog(shell, files, BaseMessages.getString(PKG, "AccessInputDialog.FilesReadSelection.DialogTitle"), BaseMessages.getString(PKG, "AccessInputDialog.FilesReadSelection.DialogMessage"));
    			            esd.setViewOnly();
    			            esd.open();
    			        }
    					
    					else
    					{
    			            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
    			            mb.setMessage(BaseMessages.getString(PKG, "AccessInputDialog.NoFileFound.DialogMessage"));
    			            mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
    			            mb.open(); 
    					}
                    }
                    catch(KettleException ex)
                    {
                        new ErrorDialog(shell, BaseMessages.getString(PKG, "AccessInputDialog.ErrorParsingData.DialogTitle"), BaseMessages.getString(PKG, "AccessInputDialog.ErrorParsingData.DialogMessage"), ex);
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
					if (!Const.isEmpty(wFilemask.getText()) || !Const.isEmpty(wExcludeFilemask.getText())) 
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
						dialog.setFilterExtensions(new String[] {"*.mdb;*.MDB;*.accdb;*.ACCDB", "*"});
						if (wFilename.getText()!=null)
						{
							String fname = "";//StringUtil.environmentSubstitute(wFilename.getText());
							dialog.setFileName( fname );
						}
						
						dialog.setFilterNames(new String[] {BaseMessages.getString(PKG, "AccessInputDialog.FileType.AccessFiles"), 
								BaseMessages.getString(PKG, "System.FileType.AllFiles")});
						
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
	         String field=  wFilenameField.getText();
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
			 if(field!=null) wFilenameField.setText(field);
			
		 }catch(KettleException ke){
				new ErrorDialog(shell, BaseMessages.getString(PKG, "AccessInputDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "AccessInputDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
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

            	Database d = Database.open(new File(AccessInputMeta.getFilename(inputList.getFile(0))), true);			
    			String realTableName=transMeta.environmentSubstitute(meta.getTableName());
    			
    			Table t=null;
    			if(realTableName.startsWith(AccessInputMeta.PREFIX_SYSTEM))
    				t=d.getSystemTable(realTableName);
    			else
    				t=d.getTable(realTableName);

    			
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
    					fieldtype = ValueMetaInterface.TYPE_INTEGER;
    				else if(DataType.BOOLEAN.equals(c.getType()))
    					fieldtype = ValueMetaInterface.TYPE_BOOLEAN;
    				else if(DataType.BINARY.equals(c.getType()))
    					fieldtype = ValueMetaInterface.TYPE_BINARY;
    				else if((DataType.DOUBLE.equals(c.getType())) || (DataType.LONG.equals(c.getType()))
    						|| (DataType.BYTE.equals(c.getType())) || (DataType.NUMERIC.equals(c.getType())))
    					fieldtype = ValueMetaInterface.TYPE_NUMBER;
    				else if((DataType.FLOAT.equals(c.getType())) || (DataType.MONEY.equals(c.getType())) )
    					fieldtype = ValueMetaInterface.TYPE_BIGNUMBER;
    				else if((DataType.SHORT_DATE_TIME.equals(c.getType())))
    					fieldtype = ValueMetaInterface.TYPE_DATE;
    				else
    					fieldtype = ValueMetaInterface.TYPE_STRING;    				
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
        		 new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.Error.Title"), BaseMessages.getString(PKG, "AccessInputDialog.ErrorReadingFile.DialogMessage", e.toString()), e);
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
    			mb.setMessage(BaseMessages.getString(PKG, "AccessInputDialog.UnableToFindFields.DialogTitle"));
    			mb.setText(BaseMessages.getString(PKG, "AccessInputDialog.UnableToFindFields.DialogMessage"));
    			mb.open(); 
    		}
            
 
	}
	private void ActiveFileField()
	{
		wlFilenameField.setEnabled(wFileField.getSelection());
		wFilenameField.setEnabled(wFileField.getSelection());
			
		wlFilename.setEnabled(!wFileField.getSelection());
		wbbFilename.setEnabled(!wFileField.getSelection());
		wbaFilename.setEnabled(!wFileField.getSelection());		
		wFilename.setEnabled(!wFileField.getSelection());		
		wlFilemask.setEnabled(!wFileField.getSelection());		
		wFilemask.setEnabled(!wFileField.getSelection());	
		wlExcludeFilemask.setEnabled(!wFileField.getSelection());		
		wExcludeFilemask.setEnabled(!wFileField.getSelection());	
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
		wbbTablename.setEnabled(!wFileField.getSelection());
		wGet.setEnabled(!wFileField.getSelection());
		wLimit.setEnabled(!wFileField.getSelection());
		wlLimit.setEnabled(!wFileField.getSelection());
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
				wFilenameList.add(new String[] { in.getFileName()[i], in.getFileMask()[i] , in.getExludeFileMask()[i],in.getRequiredFilesDesc(in.getFileRequired()[i]), in.getRequiredFilesDesc(in.getIncludeSubFolders()[i])} );
			}	
			wFilenameList.removeEmptyRows();
			wFilenameList.setRowNums();
			wFilenameList.optWidth(true);
		}
		wInclFilename.setSelection(in.includeFilename());
		wInclTablename.setSelection(in.includeTablename());
		wInclRownum.setSelection(in.includeRowNumber());
		wAddResult.setSelection(in.isAddResultFile());
		
		wFileField.setSelection(in.isFileField());
		
		if (in.getTableName()!=null) wTable.setText(in.getTableName());
		if (in.getFilenameField()!=null) wInclFilenameField.setText(in.getFilenameField());
		if (in.getDynamicFilenameField()!=null) wFilenameField.setText(in.getDynamicFilenameField());
		
		
		if (in.gettablenameField()!=null) wInclTablenameField.setText(in.gettablenameField());
		if (in.getRowNumberField()!=null) wInclRownumField.setText(in.getRowNumberField());
		wResetRownum.setSelection(in.resetRowNumber());
		wLimit.setText(""+in.getRowLimit());

		logDebug(BaseMessages.getString(PKG, "AccessInputDialog.Log.GettingFieldsInfo"));
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
    			String rep      = field.isRepeated()?BaseMessages.getString(PKG, "System.Combo.Yes"):BaseMessages.getString(PKG, "System.Combo.No");
    			
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
        if(in.getShortFileNameField()!=null) wShortFileFieldName.setText(in.getShortFileNameField());
        if(in.getPathField()!=null) wPathFieldName.setText(in.getPathField());
        if(in.isHiddenField()!=null) wIsHiddenName.setText(in.isHiddenField());
        if(in.getLastModificationDateField()!=null) wLastModificationTimeName.setText(in.getLastModificationDateField());
        if(in.getUriField()!=null) wUriName.setText(in.getUriField());
        if(in.getRootUriField()!=null) wRootUriName.setText(in.getRootUriField());
        if(in.getExtensionField()!=null) wExtensionFieldName.setText(in.getExtensionField());
        if(in.getSizeField()!=null) wSizeFieldName.setText(in.getSizeField());


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
            new ErrorDialog(shell, BaseMessages.getString(PKG, "AccessInputDialog.ErrorParsingData.DialogTitle"), BaseMessages.getString(PKG, "AccessInputDialog.ErrorParsingData.DialogMessage"), e);
        }
		dispose();
	}
	
	private void getInfo(AccessInputMeta in) throws KettleException
	{
		stepname = wStepname.getText(); // return value

		// copy info to AccessInputMeta class (input)
		in.setRowLimit( Const.toLong(wLimit.getText(), 0L) );
		in.setTableName( wTable.getText() );
		in.setIncludeFilename( wInclFilename.getSelection() );
		in.setFilenameField( wInclFilenameField.getText() );
		in.setIncludeTablename( wInclTablename.getSelection() );
		in.setTablenameField( wInclTablenameField.getText() );
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
		in.setExcludeFileMask(wFilenameList.getItems(2) );
		in.setFileRequired(wFilenameList.getItems(3)); 
		in.setIncludeSubFolders(wFilenameList.getItems(4)); 

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
			field.setRepeated( BaseMessages.getString(PKG, "System.Combo.Yes").equalsIgnoreCase(item.getText(11)) );		
            
			in.getInputFields()[i] = field;
		}	
        in.setShortFileNameField(wShortFileFieldName.getText());
        in.setPathField(wPathFieldName.getText());
        in.setIsHiddenField(wIsHiddenName.getText());
        in.setLastModificationDateField(wLastModificationTimeName.getText());
        in.setUriField(wUriName.getText());
        in.setRootUriField(wRootUriName.getText());
        in.setExtensionField(wExtensionFieldName.getText());
        in.setSizeField(wSizeFieldName.getText());
	}
	

	
		
	// Preview the data
	private void preview()
	{
        try
        {
            // Create the Access input step
            AccessInputMeta oneMeta = new AccessInputMeta();
            getInfo(oneMeta);
            
            // check if the path is given
    		if (!checkInputTableName(oneMeta)) return;

        
            TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
            
            EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), BaseMessages.getString(PKG, "AccessInputDialog.NumberRows.DialogTitle"), BaseMessages.getString(PKG, "AccessInputDialog.NumberRows.DialogMessage"));
            
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
                    	EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"),  
                    			BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message"), loggingText, true );
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
            new ErrorDialog(shell, BaseMessages.getString(PKG, "AccessInputDialog.ErrorPreviewingData.DialogTitle"), BaseMessages.getString(PKG, "AccessInputDialog.ErrorPreviewingData.DialogMessage"), e);
       }
	}
	// check if the table name is given
	private boolean checkInputTableName(AccessInputMeta meta){
        if (meta.getTableName()==null || meta.getTableName().length()<1)
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
            mb.setMessage(BaseMessages.getString(PKG, "AccessInputDialog.TableMissing.DialogMessage"));
            mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
            mb.open(); 

            return false;
        }
        else
        {
        	return true;
        }
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
					accessDatabase = Database.open(new File(AccessInputMeta.getFilename(fileInputList.getFile(0))), true);

					// Get user tables
					//
					Set<String> settables = accessDatabase.getTableNames();
					
					// Get system tables
					
					 Table SystTable= accessDatabase.getSystemCatalog();
					 Map<String,Object>   rw; 
					 while (((rw = SystTable.getNextRow())!=null))
					 {
					     String name = (String) rw.get("Name");	
						 if (name.startsWith(AccessInputMeta.PREFIX_SYSTEM))   settables.add(name);
					 }
					

					String[] tablenames = (String[]) settables.toArray(new String[settables.size()]);
					Const.sortStrings(tablenames);
					EnterSelectionDialog dialog = new EnterSelectionDialog(shell, tablenames, BaseMessages.getString(PKG, "AccessInputDialog.Dialog.SelectATable.Title"), BaseMessages.getString(PKG, "AccessInputDialog.Dialog.SelectATable.Message"));
					String tablename = dialog.open();
					if (tablename != null) {
						wTable.setText(tablename);
					}
				} else {
					// The file not exists !
					throw new KettleException(BaseMessages.getString(PKG, "AccessInputMeta.Exception.FileDoesNotExist", KettleVFS
							.getFilename(fileInputList.getFile(0))));
				}				
			}
			else
			{
				// No file specified
				 MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
		            mb.setMessage(BaseMessages.getString(PKG, "AccessInputDialog.FilesMissing.DialogMessage"));
		            mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
		            mb.open(); 
			}
		}
		catch(Throwable e)
	    {
	        new ErrorDialog(shell, BaseMessages.getString(PKG, "AccessInputDialog.UnableToGetListOfTables.Title"), BaseMessages.getString(PKG, "AccessInputDialog.UnableToGetListOfTables.Message"), e);
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
	private void addAdditionalFieldsTab()
    {
    	// ////////////////////////
		// START OF ADDITIONAL FIELDS TAB ///
		// ////////////////////////
    	wAdditionalFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
    	wAdditionalFieldsTab.setText(BaseMessages.getString(PKG, "AccessInputDialog.AdditionalFieldsTab.TabTitle"));

    	wAdditionalFieldsComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wAdditionalFieldsComp);

		FormLayout fieldsLayout = new FormLayout();
		fieldsLayout.marginWidth = 3;
		fieldsLayout.marginHeight = 3;
		wAdditionalFieldsComp.setLayout(fieldsLayout);
		
		// ShortFileFieldName line
		wlShortFileFieldName = new Label(wAdditionalFieldsComp, SWT.RIGHT);
		wlShortFileFieldName.setText(BaseMessages.getString(PKG, "AccessInputDialog.ShortFileFieldName.Label"));
		props.setLook(wlShortFileFieldName);
		fdlShortFileFieldName = new FormData();
		fdlShortFileFieldName.left = new FormAttachment(0, 0);
		fdlShortFileFieldName.top = new FormAttachment(wInclRownumField, margin);
		fdlShortFileFieldName.right = new FormAttachment(middle, -margin);
		wlShortFileFieldName.setLayoutData(fdlShortFileFieldName);

		wShortFileFieldName = new TextVar(transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wShortFileFieldName);
		wShortFileFieldName.addModifyListener(lsMod);
		fdShortFileFieldName = new FormData();
		fdShortFileFieldName.left = new FormAttachment(middle, 0);
		fdShortFileFieldName.right = new FormAttachment(100, -margin);
		fdShortFileFieldName.top = new FormAttachment(wInclRownumField, margin);
		wShortFileFieldName.setLayoutData(fdShortFileFieldName);
		
		
		// ExtensionFieldName line
		wlExtensionFieldName = new Label(wAdditionalFieldsComp, SWT.RIGHT);
		wlExtensionFieldName.setText(BaseMessages.getString(PKG, "AccessInputDialog.ExtensionFieldName.Label"));
		props.setLook(wlExtensionFieldName);
		fdlExtensionFieldName = new FormData();
		fdlExtensionFieldName.left = new FormAttachment(0, 0);
		fdlExtensionFieldName.top = new FormAttachment(wShortFileFieldName, margin);
		fdlExtensionFieldName.right = new FormAttachment(middle, -margin);
		wlExtensionFieldName.setLayoutData(fdlExtensionFieldName);

		wExtensionFieldName = new TextVar(transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wExtensionFieldName);
		wExtensionFieldName.addModifyListener(lsMod);
		fdExtensionFieldName = new FormData();
		fdExtensionFieldName.left = new FormAttachment(middle, 0);
		fdExtensionFieldName.right = new FormAttachment(100, -margin);
		fdExtensionFieldName.top = new FormAttachment(wShortFileFieldName, margin);
		wExtensionFieldName.setLayoutData(fdExtensionFieldName);
		
		
		// PathFieldName line
		wlPathFieldName = new Label(wAdditionalFieldsComp, SWT.RIGHT);
		wlPathFieldName.setText(BaseMessages.getString(PKG, "AccessInputDialog.PathFieldName.Label"));
		props.setLook(wlPathFieldName);
		fdlPathFieldName = new FormData();
		fdlPathFieldName.left = new FormAttachment(0, 0);
		fdlPathFieldName.top = new FormAttachment(wExtensionFieldName, margin);
		fdlPathFieldName.right = new FormAttachment(middle, -margin);
		wlPathFieldName.setLayoutData(fdlPathFieldName);

		wPathFieldName = new TextVar(transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wPathFieldName);
		wPathFieldName.addModifyListener(lsMod);
		fdPathFieldName = new FormData();
		fdPathFieldName.left = new FormAttachment(middle, 0);
		fdPathFieldName.right = new FormAttachment(100, -margin);
		fdPathFieldName.top = new FormAttachment(wExtensionFieldName, margin);
		wPathFieldName.setLayoutData(fdPathFieldName);
		


  		// SizeFieldName line
		wlSizeFieldName = new Label(wAdditionalFieldsComp, SWT.RIGHT);
		wlSizeFieldName.setText(BaseMessages.getString(PKG, "AccessInputDialog.SizeFieldName.Label"));
		props.setLook(wlSizeFieldName);
		fdlSizeFieldName = new FormData();
		fdlSizeFieldName.left = new FormAttachment(0, 0);
		fdlSizeFieldName.top = new FormAttachment(wPathFieldName, margin);
		fdlSizeFieldName.right = new FormAttachment(middle, -margin);
		wlSizeFieldName.setLayoutData(fdlSizeFieldName);

		wSizeFieldName = new TextVar(transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSizeFieldName);
		wSizeFieldName.addModifyListener(lsMod);
		fdSizeFieldName = new FormData();
		fdSizeFieldName.left = new FormAttachment(middle, 0);
		fdSizeFieldName.right = new FormAttachment(100, -margin);
		fdSizeFieldName.top = new FormAttachment(wPathFieldName, margin);
		wSizeFieldName.setLayoutData(fdSizeFieldName);
		
		
		// IsHiddenName line
		wlIsHiddenName = new Label(wAdditionalFieldsComp, SWT.RIGHT);
		wlIsHiddenName.setText(BaseMessages.getString(PKG, "AccessInputDialog.IsHiddenName.Label"));
		props.setLook(wlIsHiddenName);
		fdlIsHiddenName = new FormData();
		fdlIsHiddenName.left = new FormAttachment(0, 0);
		fdlIsHiddenName.top = new FormAttachment(wSizeFieldName, margin);
		fdlIsHiddenName.right = new FormAttachment(middle, -margin);
		wlIsHiddenName.setLayoutData(fdlIsHiddenName);

		wIsHiddenName = new TextVar(transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wIsHiddenName);
		wIsHiddenName.addModifyListener(lsMod);
		fdIsHiddenName = new FormData();
		fdIsHiddenName.left = new FormAttachment(middle, 0);
		fdIsHiddenName.right = new FormAttachment(100, -margin);
		fdIsHiddenName.top = new FormAttachment(wSizeFieldName, margin);
		wIsHiddenName.setLayoutData(fdIsHiddenName);
		
		// LastModificationTimeName line
		wlLastModificationTimeName = new Label(wAdditionalFieldsComp, SWT.RIGHT);
		wlLastModificationTimeName.setText(BaseMessages.getString(PKG, "AccessInputDialog.LastModificationTimeName.Label"));
		props.setLook(wlLastModificationTimeName);
		fdlLastModificationTimeName = new FormData();
		fdlLastModificationTimeName.left = new FormAttachment(0, 0);
		fdlLastModificationTimeName.top = new FormAttachment(wIsHiddenName, margin);
		fdlLastModificationTimeName.right = new FormAttachment(middle, -margin);
		wlLastModificationTimeName.setLayoutData(fdlLastModificationTimeName);

		wLastModificationTimeName = new TextVar(transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wLastModificationTimeName);
		wLastModificationTimeName.addModifyListener(lsMod);
		fdLastModificationTimeName = new FormData();
		fdLastModificationTimeName.left = new FormAttachment(middle, 0);
		fdLastModificationTimeName.right = new FormAttachment(100, -margin);
		fdLastModificationTimeName.top = new FormAttachment(wIsHiddenName, margin);
		wLastModificationTimeName.setLayoutData(fdLastModificationTimeName);
		
		// UriName line
		wlUriName = new Label(wAdditionalFieldsComp, SWT.RIGHT);
		wlUriName.setText(BaseMessages.getString(PKG, "AccessInputDialog.UriName.Label"));
		props.setLook(wlUriName);
		fdlUriName = new FormData();
		fdlUriName.left = new FormAttachment(0, 0);
		fdlUriName.top = new FormAttachment(wLastModificationTimeName, margin);
		fdlUriName.right = new FormAttachment(middle, -margin);
		wlUriName.setLayoutData(fdlUriName);

		wUriName = new TextVar(transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wUriName);
		wUriName.addModifyListener(lsMod);
		fdUriName = new FormData();
		fdUriName.left = new FormAttachment(middle, 0);
		fdUriName.right = new FormAttachment(100, -margin);
		fdUriName.top = new FormAttachment(wLastModificationTimeName, margin);
		wUriName.setLayoutData(fdUriName);
		
		// RootUriName line
		wlRootUriName = new Label(wAdditionalFieldsComp, SWT.RIGHT);
		wlRootUriName.setText(BaseMessages.getString(PKG, "AccessInputDialog.RootUriName.Label"));
		props.setLook(wlRootUriName);
		fdlRootUriName = new FormData();
		fdlRootUriName.left = new FormAttachment(0, 0);
		fdlRootUriName.top = new FormAttachment(wUriName, margin);
		fdlRootUriName.right = new FormAttachment(middle, -margin);
		wlRootUriName.setLayoutData(fdlRootUriName);

		wRootUriName = new TextVar(transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wRootUriName);
		wRootUriName.addModifyListener(lsMod);
		fdRootUriName = new FormData();
		fdRootUriName.left = new FormAttachment(middle, 0);
		fdRootUriName.right = new FormAttachment(100, -margin);
		fdRootUriName.top = new FormAttachment(wUriName, margin);
		wRootUriName.setLayoutData(fdRootUriName);
	

		fdAdditionalFieldsComp = new FormData();
		fdAdditionalFieldsComp.left = new FormAttachment(0, 0);
		fdAdditionalFieldsComp.top = new FormAttachment(wStepname, margin);
		fdAdditionalFieldsComp.right = new FormAttachment(100, 0);
		fdAdditionalFieldsComp.bottom = new FormAttachment(100, 0);
		wAdditionalFieldsComp.setLayoutData(fdAdditionalFieldsComp);

		wAdditionalFieldsComp.layout();
		wAdditionalFieldsTab.setControl(wAdditionalFieldsComp);

		// ///////////////////////////////////////////////////////////
		// / END OF ADDITIONAL FIELDS TAB
		// ///////////////////////////////////////////////////////////

		
    	
    }
}