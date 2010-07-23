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


package org.pentaho.di.ui.trans.steps.ldifinput;

import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashSet;

import netscape.ldap.LDAPAttribute;
import netscape.ldap.util.LDIF;
import netscape.ldap.util.LDIFAttributeContent;
import netscape.ldap.util.LDIFContent;
import netscape.ldap.util.LDIFRecord;

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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.ldifinput.LDIFInputField;
import org.pentaho.di.trans.steps.ldifinput.LDIFInputMeta;
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


public class LDIFInputDialog extends BaseStepDialog implements
		StepDialogInterface {
	
	private static Class<?> PKG = LDIFInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CTabFolder wTabFolder;

	private FormData fdTabFolder;

	private CTabItem wFileTab, wContentTab, wFieldsTab;

	private Composite wFileComp, wContentComp, wFieldsComp;

	private FormData fdFileComp, fdContentComp, fdFieldsComp,fdlAddResult;

	private Label wlFilename,wlAddResult;

	private Button wbbFilename; // Browse: add file or directory

	private Button wbdFilename; // Delete

	private Button wbeFilename; // Edit

	private Button wbaFilename; // Add or change

	private TextVar wFilename;

	private FormData fdlFilename, fdbFilename, fdbdFilename, fdbeFilename,
			fdbaFilename, fdFilename;

	private Label wlFilenameList;

	private TableView wFilenameList;

	private FormData fdlFilenameList, fdFilenameList;

	private Label wlFilemask;

	private TextVar wFilemask;

	private FormData fdlFilemask, fdFilemask;
	
	private Label wlExcludeFilemask;
	private TextVar wExcludeFilemask;
	private FormData fdlExcludeFilemask, fdExcludeFilemask;

	private Button wbShowFiles;

	private FormData fdbShowFiles;

	private Label wlInclFilename, wlInclDNField;

	private Button wInclFilename,wInclContentType,wInclDN;

	private FormData fdlInclFilename, fdInclFilename,fdInclContentType, fdInclDN, fdlInclDNField, fdlInclDN;

	private Label wlInclFilenameField,wlInclContentType, wlInclDN;

	private TextVar wInclFilenameField,wInclContentTypeField, wInclDNField;

	private FormData fdlInclFilenameField, fdInclFilenameField,fdlInclContentType, fdInclDNField;

	private Label wlInclRownum;

	private Button wInclRownum;

	private FormData fdlInclRownum, fdRownum;

	private Label wlInclRownumField;

	private TextVar wInclRownumField;

	private FormData fdlInclRownumField, fdInclRownumField;

	private Label wlLimit;

	private Text wLimit;

	private FormData fdlLimit, fdLimit;

	private TableView wFields;

	private FormData fdFields,fdAddFileResult,fdAddResult,fdInclContentTypeField,fdlInclContentTypeField;

	private LDIFInputMeta input;
	
	private Group wAddFileResult;
	
	 private Button wAddResult;
	 
	private Label        wlMultiValuedSeparator,wlInclContentTypeField;
	
	private TextVar      wMultiValuedSeparator;
	
	private FormData     fdlMultiValuedSeparator, fdMultiValuedSeparator;
	
	private FormData fdOriginFiles;
	private Group wOriginFiles;
	
    private Label wlFileField,wlFilenameField;
    private CCombo wFilenameField;
    private FormData fdlFileField,fdFileField;
    
	private FormData fdFilenameField,fdlFilenameField;
    private Button wFileField;
	
    private boolean gotPreviousField=false;
    
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

	public static final int dateLengths[] = new int[] { 23, 19, 14, 10, 10, 10,
			10, 8, 8, 8, 8, 6, 6 };

	public LDIFInputDialog(Shell parent, Object in, TransMeta transMeta,
			String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (LDIFInputMeta) in;
	}

	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX
				| SWT.MIN);
		props.setLook(shell);
		setShellImage(shell, input);

		lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "LDIFInputDialog.DialogTitle"));

		middle = props.getMiddlePct();
		margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.top = new FormAttachment(0, margin);
		fdlStepname.right = new FormAttachment(middle, -margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		wTabFolder = new CTabFolder(shell, SWT.BORDER);
		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

		// ////////////////////////
		// START OF FILE TAB ///
		// ////////////////////////
		wFileTab = new CTabItem(wTabFolder, SWT.NONE);
		wFileTab.setText(BaseMessages.getString(PKG, "LDIFInputDialog.File.Tab"));

		wFileComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wFileComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth = 3;
		fileLayout.marginHeight = 3;
		wFileComp.setLayout(fileLayout);
		
		// ///////////////////////////////
		// START OF Origin files GROUP  //
		///////////////////////////////// 

		wOriginFiles = new Group(wFileComp, SWT.SHADOW_NONE);
		props.setLook(wOriginFiles);
		wOriginFiles.setText(BaseMessages.getString(PKG, "LDIFInputDialog.wOriginFiles.Label"));
		
		FormLayout OriginFilesgroupLayout = new FormLayout();
		OriginFilesgroupLayout.marginWidth = 10;
		OriginFilesgroupLayout.marginHeight = 10;
		wOriginFiles.setLayout(OriginFilesgroupLayout);
		
		//Is Filename defined in a Field		
		wlFileField = new Label(wOriginFiles, SWT.RIGHT);
		wlFileField.setText(BaseMessages.getString(PKG, "LDIFInputDialog.FileField.Label"));
		props.setLook(wlFileField);
		fdlFileField = new FormData();
		fdlFileField.left = new FormAttachment(0, -margin);
		fdlFileField.top = new FormAttachment(0, margin);
		fdlFileField.right = new FormAttachment(middle, -2*margin);
		wlFileField.setLayoutData(fdlFileField);
		
		
		wFileField = new Button(wOriginFiles, SWT.CHECK);
		props.setLook(wFileField);
		wFileField.setToolTipText(BaseMessages.getString(PKG, "LDIFInputDialog.FileField.Tooltip"));
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
        wlFilenameField.setText(BaseMessages.getString(PKG, "LDIFInputDialog.wlFilenameField.Label"));
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
		wlFilename = new Label(wFileComp, SWT.RIGHT);
		wlFilename
				.setText(BaseMessages.getString(PKG, "LDIFInputDialog.Filename.Label"));
		props.setLook(wlFilename);
		fdlFilename = new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top = new FormAttachment(wOriginFiles, margin);
		fdlFilename.right = new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbbFilename = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbbFilename);
		wbbFilename.setText(BaseMessages.getString(PKG, "LDIFInputDialog.FilenameBrowse.Button"));
		wbbFilename.setToolTipText(BaseMessages.getString(PKG, "System.Tooltip.BrowseForFileOrDirAndAdd"));
		fdbFilename = new FormData();
		fdbFilename.right = new FormAttachment(100, 0);
		fdbFilename.top = new FormAttachment(wOriginFiles, margin);
		wbbFilename.setLayoutData(fdbFilename);

		wbaFilename = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbaFilename);
		wbaFilename.setText(BaseMessages.getString(PKG, "LDIFInputDialog.FilenameAdd.Button"));
		wbaFilename.setToolTipText(BaseMessages.getString(PKG, "LDIFInputDialog.FilenameAdd.Tooltip"));
		fdbaFilename = new FormData();
		fdbaFilename.right = new FormAttachment(wbbFilename, -margin);
		fdbaFilename.top = new FormAttachment(wOriginFiles, margin);
		wbaFilename.setLayoutData(fdbaFilename);

		wFilename = new TextVar(transMeta,wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename = new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right = new FormAttachment(wbaFilename, -margin);
		fdFilename.top = new FormAttachment(wOriginFiles, margin);
		wFilename.setLayoutData(fdFilename);

		wlFilemask = new Label(wFileComp, SWT.RIGHT);
		wlFilemask.setText(BaseMessages.getString(PKG, "LDIFInputDialog.RegExp.Label"));
		props.setLook(wlFilemask);
		fdlFilemask = new FormData();
		fdlFilemask.left = new FormAttachment(0, 0);
		fdlFilemask.top = new FormAttachment(wFilename, margin);
		fdlFilemask.right = new FormAttachment(middle, -margin);
		wlFilemask.setLayoutData(fdlFilemask);
		wFilemask = new TextVar(transMeta,wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFilemask);
		wFilemask.setToolTipText(BaseMessages.getString(PKG, "LDIFInputDialog.RegExp.Tooltip"));
		wFilemask.addModifyListener(lsMod);
		fdFilemask = new FormData();
		fdFilemask.left = new FormAttachment(middle, 0);
		fdFilemask.top = new FormAttachment(wFilename, margin);
		fdFilemask.right = new FormAttachment(100, 0);
		wFilemask.setLayoutData(fdFilemask);
		
		wlExcludeFilemask = new Label(wFileComp, SWT.RIGHT);
		wlExcludeFilemask.setText(BaseMessages.getString(PKG, "LDIFInputDialog.ExcludeFilemask.Label"));
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
		wlFilenameList = new Label(wFileComp, SWT.RIGHT);
		wlFilenameList.setText(BaseMessages.getString(PKG, "LDIFInputDialog.FilenameList.Label"));
		props.setLook(wlFilenameList);
		fdlFilenameList = new FormData();
		fdlFilenameList.left = new FormAttachment(0, 0);
		fdlFilenameList.top = new FormAttachment(wExcludeFilemask, margin);
		fdlFilenameList.right = new FormAttachment(middle, -margin);
		wlFilenameList.setLayoutData(fdlFilenameList);

		// Buttons to the right of the screen...
		wbdFilename = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbdFilename);
		wbdFilename.setText(BaseMessages.getString(PKG, "LDIFInputDialog.FilenameRemove.Button"));
		wbdFilename.setToolTipText(BaseMessages.getString(PKG, "LDIFInputDialog.FilenameRemove.Tooltip"));
		fdbdFilename = new FormData();
		fdbdFilename.right = new FormAttachment(100, 0);
		fdbdFilename.top = new FormAttachment(wExcludeFilemask, 40);
		wbdFilename.setLayoutData(fdbdFilename);

		wbeFilename = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbeFilename);
		wbeFilename.setText(BaseMessages.getString(PKG, "LDIFInputDialog.FilenameEdit.Button"));
		wbeFilename.setToolTipText(BaseMessages.getString(PKG, "LDIFInputDialog.FilenameEdit.Tooltip"));
		fdbeFilename = new FormData();
		fdbeFilename.right = new FormAttachment(100, 0);
		fdbeFilename.top = new FormAttachment(wbdFilename, margin);
		wbeFilename.setLayoutData(fdbeFilename);

		wbShowFiles = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbShowFiles);
		wbShowFiles.setText(BaseMessages.getString(PKG, "LDIFInputDialog.ShowFiles.Button"));
		fdbShowFiles = new FormData();
		fdbShowFiles.left = new FormAttachment(middle, 0);
		fdbShowFiles.bottom = new FormAttachment(100, 0);
		wbShowFiles.setLayoutData(fdbShowFiles);

		ColumnInfo[] colinfo = new ColumnInfo[5];
		colinfo[0] = new ColumnInfo(BaseMessages.getString(PKG, "LDIFInputDialog.Files.Filename.Column"),
				ColumnInfo.COLUMN_TYPE_TEXT, false);
		colinfo[1] = new ColumnInfo(BaseMessages.getString(PKG, "LDIFInputDialog.Files.Wildcard.Column"),
				ColumnInfo.COLUMN_TYPE_TEXT, false);
		colinfo[ 2]=new ColumnInfo(BaseMessages.getString(PKG, "LDIFInputDialog.Files.ExcludeWildcard.Column"),
				ColumnInfo.COLUMN_TYPE_TEXT, false);
		colinfo[3]=new ColumnInfo(BaseMessages.getString(PKG, "LDIFInputDialog.Required.Column"),        
				ColumnInfo.COLUMN_TYPE_CCOMBO,  LDIFInputMeta.RequiredFilesDesc );
		colinfo[4]=new ColumnInfo(BaseMessages.getString(PKG, "LDIFInputDialog.IncludeSubDirs.Column"),        
				ColumnInfo.COLUMN_TYPE_CCOMBO,  LDIFInputMeta.RequiredFilesDesc );

		colinfo[0].setUsingVariables(true);
		colinfo[1].setUsingVariables(true);
		colinfo[1].setToolTip(BaseMessages.getString(PKG, "LDIFInputDialog.Files.Wildcard.Tooltip"));
		colinfo[2].setToolTip(BaseMessages.getString(PKG, "LDIFInputDialog.Required.Tooltip"));
		colinfo[2].setUsingVariables(true);
		colinfo[2].setToolTip(BaseMessages.getString(PKG, "LDIFInputDialog.Files.ExcludeWildcard.Tooltip"));
		colinfo[4].setToolTip(BaseMessages.getString(PKG, "LDIFInputDialog.IncludeSubDirs.Tooltip"));

		wFilenameList = new TableView(transMeta,wFileComp, SWT.FULL_SELECTION
				| SWT.SINGLE | SWT.BORDER, colinfo, 2, lsMod, props);
		props.setLook(wFilenameList);
		fdFilenameList = new FormData();
		fdFilenameList.left = new FormAttachment(middle, 0);
		fdFilenameList.right = new FormAttachment(wbdFilename, -margin);
		fdFilenameList.top = new FormAttachment(wExcludeFilemask, margin);
		fdFilenameList.bottom = new FormAttachment(wbShowFiles, -margin);
		wFilenameList.setLayoutData(fdFilenameList);

		fdFileComp = new FormData();
		fdFileComp.left = new FormAttachment(0, 0);
		fdFileComp.top = new FormAttachment(0, 0);
		fdFileComp.right = new FormAttachment(100, 0);
		fdFileComp.bottom = new FormAttachment(100, 0);
		wFileComp.setLayoutData(fdFileComp);

		wFileComp.layout();
		wFileTab.setControl(wFileComp);

		// ///////////////////////////////////////////////////////////
		// / END OF FILE TAB
		// ///////////////////////////////////////////////////////////

		// ////////////////////////
		// START OF CONTENT TAB///
		// /
		wContentTab = new CTabItem(wTabFolder, SWT.NONE);
		wContentTab.setText(BaseMessages.getString(PKG, "LDIFInputDialog.Content.Tab"));

		FormLayout contentLayout = new FormLayout();
		contentLayout.marginWidth = 3;
		contentLayout.marginHeight = 3;

		wContentComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wContentComp);
		wContentComp.setLayout(contentLayout);

		wlInclFilename = new Label(wContentComp, SWT.RIGHT);
		wlInclFilename.setText(BaseMessages.getString(PKG, "LDIFInputDialog.InclFilename.Label"));
		props.setLook(wlInclFilename);
		fdlInclFilename = new FormData();
		fdlInclFilename.left = new FormAttachment(0, 0);
		fdlInclFilename.top = new FormAttachment(0, 2 * margin);
		fdlInclFilename.right = new FormAttachment(middle, -margin);
		wlInclFilename.setLayoutData(fdlInclFilename);
		wInclFilename = new Button(wContentComp, SWT.CHECK);
		props.setLook(wInclFilename);
		wInclFilename.setToolTipText(BaseMessages.getString(PKG, "LDIFInputDialog.InclFilename.Tooltip"));
		fdInclFilename = new FormData();
		fdInclFilename.left = new FormAttachment(middle, 0);
		fdInclFilename.top = new FormAttachment(0, 2 * margin);
		wInclFilename.setLayoutData(fdInclFilename);

		wlInclFilenameField = new Label(wContentComp, SWT.LEFT);
		wlInclFilenameField.setText(BaseMessages.getString(PKG, "LDIFInputDialog.InclFilenameField.Label"));
		props.setLook(wlInclFilenameField);
		fdlInclFilenameField = new FormData();
		fdlInclFilenameField.left = new FormAttachment(wInclFilename, margin);
		fdlInclFilenameField.top = new FormAttachment(0, 2 * margin);
		wlInclFilenameField.setLayoutData(fdlInclFilenameField);
		wInclFilenameField = new TextVar(transMeta,wContentComp, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		props.setLook(wInclFilenameField);
		wInclFilenameField.addModifyListener(lsMod);
		fdInclFilenameField = new FormData();
		fdInclFilenameField.left = new FormAttachment(wlInclFilenameField,
				margin);
		fdInclFilenameField.top = new FormAttachment(0, 2 * margin);
		fdInclFilenameField.right = new FormAttachment(100, 0);
		wInclFilenameField.setLayoutData(fdInclFilenameField);

		wlInclRownum = new Label(wContentComp, SWT.RIGHT);
		wlInclRownum.setText(BaseMessages.getString(PKG, "LDIFInputDialog.InclRownum.Label"));
		props.setLook(wlInclRownum);
		fdlInclRownum = new FormData();
		fdlInclRownum.left = new FormAttachment(0, 0);
		fdlInclRownum.top = new FormAttachment(wInclFilenameField, margin);
		fdlInclRownum.right = new FormAttachment(middle, -margin);
		wlInclRownum.setLayoutData(fdlInclRownum);
		wInclRownum = new Button(wContentComp, SWT.CHECK);
		props.setLook(wInclRownum);
		wInclRownum.setToolTipText(BaseMessages.getString(PKG, "LDIFInputDialog.InclRownum.Tooltip"));
		fdRownum = new FormData();
		fdRownum.left = new FormAttachment(middle, 0);
		fdRownum.top = new FormAttachment(wInclFilenameField, margin);
		wInclRownum.setLayoutData(fdRownum);

		wlInclRownumField = new Label(wContentComp, SWT.RIGHT);
		wlInclRownumField.setText(BaseMessages.getString(PKG, ("LDIFInputDialog.InclRownumField.Label")));
		props.setLook(wlInclRownumField);
		fdlInclRownumField = new FormData();
		fdlInclRownumField.left = new FormAttachment(wInclRownum, margin);
		fdlInclRownumField.top = new FormAttachment(wInclFilenameField, margin);
		wlInclRownumField.setLayoutData(fdlInclRownumField);
		wInclRownumField = new TextVar(transMeta,wContentComp, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		props.setLook(wInclRownumField);
		wInclRownumField.addModifyListener(lsMod);
		fdInclRownumField = new FormData();
		fdInclRownumField.left = new FormAttachment(wlInclRownumField, margin);
		fdInclRownumField.top = new FormAttachment(wInclFilenameField, margin);
		fdInclRownumField.right = new FormAttachment(100, 0);
		wInclRownumField.setLayoutData(fdInclRownumField);
		
		
		// Add content type field?
		wlInclContentType = new Label(wContentComp, SWT.RIGHT);
		wlInclContentType.setText(BaseMessages.getString(PKG, "LDIFInputDialog.InclContentType.Label"));
		props.setLook(wlInclContentType);
		fdlInclContentType = new FormData();
		fdlInclContentType.left = new FormAttachment(0, 0);
		fdlInclContentType.top = new FormAttachment(wInclRownumField, margin);
		fdlInclContentType.right = new FormAttachment(middle, -margin);
		wlInclContentType.setLayoutData(fdlInclContentType);
		wInclContentType = new Button(wContentComp, SWT.CHECK);
		props.setLook(wInclContentType);
		wInclContentType.setToolTipText(BaseMessages.getString(PKG, "LDIFInputDialog.InclContentType.Tooltip"));
		fdInclContentType = new FormData();
		fdInclContentType.left = new FormAttachment(middle, 0);
		fdInclContentType.top = new FormAttachment(wInclRownumField, margin);
		wInclContentType.setLayoutData(fdInclContentType);
		
		// Content type field name
		wlInclContentTypeField = new Label(wContentComp, SWT.LEFT);
		wlInclContentTypeField.setText(BaseMessages.getString(PKG, "LDIFInputDialog.InclContentTypeField.Label"));
		props.setLook(wlInclContentTypeField);
		fdlInclContentTypeField = new FormData();
		fdlInclContentTypeField.left = new FormAttachment(wInclContentType, margin);
		fdlInclContentTypeField.top = new FormAttachment(wInclRownumField,margin);
		wlInclContentTypeField.setLayoutData(fdlInclContentTypeField);
		wInclContentTypeField = new TextVar(transMeta,wContentComp, SWT.SINGLE | SWT.LEFT| SWT.BORDER);
		props.setLook(wInclContentTypeField);
		wInclContentTypeField.addModifyListener(lsMod);
		fdInclContentTypeField = new FormData();
		fdInclContentTypeField.left = new FormAttachment(wlInclContentTypeField,margin);
		fdInclContentTypeField.top = new FormAttachment(wInclRownumField, margin);
		fdInclContentTypeField.right = new FormAttachment(100, 0);
		wInclContentTypeField.setLayoutData(fdInclContentTypeField);


		
		// Add content type field?
		wlInclDN = new Label(wContentComp, SWT.RIGHT);
		wlInclDN.setText(BaseMessages.getString(PKG, "LDIFInputDialog.InclDN.Label"));
		props.setLook(wlInclDN);
		fdlInclDN = new FormData();
		fdlInclDN.left = new FormAttachment(0, 0);
		fdlInclDN.top = new FormAttachment(wInclContentTypeField, margin);
		fdlInclDN.right = new FormAttachment(middle, -margin);
		wlInclDN.setLayoutData(fdlInclDN);
		wInclDN = new Button(wContentComp, SWT.CHECK);
		props.setLook(wInclDN);
		wInclDN.setToolTipText(BaseMessages.getString(PKG, "LDIFInputDialog.InclDN.Tooltip"));
		fdInclDN = new FormData();
		fdInclDN.left = new FormAttachment(middle, 0);
		fdInclDN.top = new FormAttachment(wInclContentTypeField, margin);
		wInclDN.setLayoutData(fdInclDN);
		
		// Content type field name
		wlInclDNField = new Label(wContentComp, SWT.LEFT);
		wlInclDNField.setText(BaseMessages.getString(PKG, "LDIFInputDialog.InclDNField.Label"));
		props.setLook(wlInclDNField);
		fdlInclDNField = new FormData();
		fdlInclDNField.left = new FormAttachment(wInclDN, margin);
		fdlInclDNField.top = new FormAttachment(wInclContentTypeField,margin);
		wlInclDNField.setLayoutData(fdlInclDNField);
		wInclDNField = new TextVar(transMeta,wContentComp, SWT.SINGLE | SWT.LEFT| SWT.BORDER);
		props.setLook(wInclDNField);
		wInclDNField.addModifyListener(lsMod);
		fdInclDNField = new FormData();
		fdInclDNField.left = new FormAttachment(wlInclDNField,margin);
		fdInclDNField.top = new FormAttachment(wInclContentTypeField, margin);
		fdInclDNField.right = new FormAttachment(100, 0);
		wInclDNField.setLayoutData(fdInclDNField);

		
		
		// Limit to preview
		wlLimit = new Label(wContentComp, SWT.RIGHT);
		wlLimit.setText(BaseMessages.getString(PKG, "LDIFInputDialog.Limit.Label"));
		props.setLook(wlLimit);
		fdlLimit = new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.top = new FormAttachment(wInclDNField, margin);
		fdlLimit.right = new FormAttachment(middle, -margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit = new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit = new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.top = new FormAttachment(wInclDNField, margin);
		fdLimit.right = new FormAttachment(100, 0);
		wLimit.setLayoutData(fdLimit);
		
		
		// Multi valued field separator
		wlMultiValuedSeparator=new Label(wContentComp, SWT.RIGHT);
		wlMultiValuedSeparator.setText(BaseMessages.getString(PKG, "LDIFInputDialog.MultiValuedSeparator.Label"));
 		props.setLook(wlMultiValuedSeparator);
		fdlMultiValuedSeparator=new FormData();
		fdlMultiValuedSeparator.left = new FormAttachment(0, 0);
		fdlMultiValuedSeparator.top  = new FormAttachment(wLimit, margin);
		fdlMultiValuedSeparator.right= new FormAttachment(middle, -margin);
		wlMultiValuedSeparator.setLayoutData(fdlMultiValuedSeparator);
		wMultiValuedSeparator=new TextVar(transMeta,wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMultiValuedSeparator);
 		wMultiValuedSeparator.setToolTipText(BaseMessages.getString(PKG, "LDIFInputDialog.MultiValuedSeparator.Tooltip"));
		wMultiValuedSeparator.addModifyListener(lsMod);
		fdMultiValuedSeparator=new FormData();
		fdMultiValuedSeparator.left = new FormAttachment(middle, 0);
		fdMultiValuedSeparator.top  = new FormAttachment(wLimit, margin);
		fdMultiValuedSeparator.right= new FormAttachment(100, 0);
		wMultiValuedSeparator.setLayoutData(fdMultiValuedSeparator);
		
		
		// ///////////////////////////////
		// START OF AddFileResult GROUP  //
		///////////////////////////////// 

		wAddFileResult = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wAddFileResult);
		wAddFileResult.setText(BaseMessages.getString(PKG, "LDIFInputDialog.wAddFileResult.Label"));
		
		FormLayout AddFileResultgroupLayout = new FormLayout();
		AddFileResultgroupLayout.marginWidth = 10;
		AddFileResultgroupLayout.marginHeight = 10;
		wAddFileResult.setLayout(AddFileResultgroupLayout);

		wlAddResult=new Label(wAddFileResult, SWT.RIGHT);
		wlAddResult.setText(BaseMessages.getString(PKG, "LDIFInputDialog.AddResult.Label"));
 		props.setLook(wlAddResult);
		fdlAddResult=new FormData();
		fdlAddResult.left = new FormAttachment(0, 0);
		fdlAddResult.top  = new FormAttachment(wMultiValuedSeparator, margin);
		fdlAddResult.right= new FormAttachment(middle, -margin);
		wlAddResult.setLayoutData(fdlAddResult);
		wAddResult=new Button(wAddFileResult, SWT.CHECK );
 		props.setLook(wAddResult);
		wAddResult.setToolTipText(BaseMessages.getString(PKG, "LDIFInputDialog.AddResult.Tooltip"));
		fdAddResult=new FormData();
		fdAddResult.left = new FormAttachment(middle, 0);
		fdAddResult.top  = new FormAttachment(wMultiValuedSeparator, margin);
		wAddResult.setLayoutData(fdAddResult);

		fdAddFileResult = new FormData();
		fdAddFileResult.left = new FormAttachment(0, margin);
		fdAddFileResult.top = new FormAttachment(wMultiValuedSeparator, margin);
		fdAddFileResult.right = new FormAttachment(100, -margin);
		wAddFileResult.setLayoutData(fdAddFileResult);
			
		// ///////////////////////////////////////////////////////////
		// / END OF AddFileResult GROUP
		// ///////////////////////////////////////////////////////////	
       
       

		fdContentComp = new FormData();
		fdContentComp.left = new FormAttachment(0, 0);
		fdContentComp.top = new FormAttachment(0, 0);
		fdContentComp.right = new FormAttachment(100, 0);
		fdContentComp.bottom = new FormAttachment(100, 0);
		wContentComp.setLayoutData(fdContentComp);

		wContentComp.layout();
		wContentTab.setControl(wContentComp);

		// ///////////////////////////////////////////////////////////
		// / END OF CONTENT TAB
		// ///////////////////////////////////////////////////////////

		// Fields tab...
		//
		wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
		wFieldsTab.setText(BaseMessages.getString(PKG, "LDIFInputDialog.Fields.Tab"));

		FormLayout fieldsLayout = new FormLayout();
		fieldsLayout.marginWidth = Const.FORM_MARGIN;
		fieldsLayout.marginHeight = Const.FORM_MARGIN;

		wFieldsComp = new Composite(wTabFolder, SWT.NONE);
		wFieldsComp.setLayout(fieldsLayout);
		props.setLook(wFieldsComp);

		wGet = new Button(wFieldsComp, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "LDIFInputDialog.GetFields.Button"));
		fdGet = new FormData();
		fdGet.left = new FormAttachment(50, 0);
		fdGet.bottom = new FormAttachment(100, 0);
		wGet.setLayoutData(fdGet);

		final int FieldsRows = input.getInputFields().length;


		ColumnInfo[] colinf = new ColumnInfo[] {
				new ColumnInfo(BaseMessages.getString(PKG, "LDIFInputDialog.FieldsTable.Name.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false),
				new ColumnInfo(
						BaseMessages.getString(PKG, "LDIFInputDialog.FieldsTable.Attribut.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false),
				new ColumnInfo(BaseMessages.getString(PKG, "LDIFInputDialog.FieldsTable.Type.Column"),
						ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes(), true),
				new ColumnInfo(
						BaseMessages.getString(PKG, "LDIFInputDialog.FieldsTable.Format.Column"),
						ColumnInfo.COLUMN_TYPE_FORMAT, 3),
				new ColumnInfo(
						BaseMessages.getString(PKG, "LDIFInputDialog.FieldsTable.Length.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false),
				new ColumnInfo(
						BaseMessages.getString(PKG, "LDIFInputDialog.FieldsTable.Precision.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false),
				new ColumnInfo(
						BaseMessages.getString(PKG, "LDIFInputDialog.FieldsTable.Currency.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false),
				new ColumnInfo(
						BaseMessages.getString(PKG, "LDIFInputDialog.FieldsTable.Decimal.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false),
				new ColumnInfo(BaseMessages.getString(PKG, "LDIFInputDialog.FieldsTable.Group.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false),
				new ColumnInfo(
						BaseMessages.getString(PKG, "LDIFInputDialog.FieldsTable.TrimType.Column"),
						ColumnInfo.COLUMN_TYPE_CCOMBO,
						LDIFInputField.trimTypeDesc, true),
				new ColumnInfo(
						BaseMessages.getString(PKG, "LDIFInputDialog.FieldsTable.Repeat.Column"),
						ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {
								BaseMessages.getString(PKG, "System.Combo.Yes"),
								BaseMessages.getString(PKG, "System.Combo.No") }, true),

		};

		colinf[0].setUsingVariables(true);
		colinf[0].setToolTip(BaseMessages.getString(PKG, "LDIFInputDialog.FieldsTable.Name.Column.Tooltip"));
		colinf[1].setUsingVariables(true);
		colinf[1]
				.setToolTip(BaseMessages.getString(PKG, "LDIFInputDialog.FieldsTable.Attribut.Column.Tooltip"));

		wFields = new TableView(transMeta,wFieldsComp, SWT.FULL_SELECTION | SWT.MULTI,
				colinf, FieldsRows, lsMod, props);

		fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(0, 0);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(wGet, -margin);
		wFields.setLayoutData(fdFields);

		fdFieldsComp = new FormData();
		fdFieldsComp.left = new FormAttachment(0, 0);
		fdFieldsComp.top = new FormAttachment(0, 0);
		fdFieldsComp.right = new FormAttachment(100, 0);
		fdFieldsComp.bottom = new FormAttachment(100, 0);
		wFieldsComp.setLayoutData(fdFieldsComp);

		wFieldsComp.layout();
		wFieldsTab.setControl(wFieldsComp);
		
        addAdditionalFieldsTab();

		fdTabFolder = new FormData();
		fdTabFolder.left = new FormAttachment(0, 0);
		fdTabFolder.top = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom = new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));

		wPreview = new Button(shell, SWT.PUSH);
		wPreview.setText(BaseMessages.getString(PKG, "LDIFInputDialog.Button.PreviewRows"));

		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin,
				wTabFolder);

		// Add listeners
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};
		lsGet = new Listener() {
			public void handleEvent(Event e) {
				get();
			}
		};
		lsPreview = new Listener() {
			public void handleEvent(Event e) {
				preview();
			}
		};
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};

		wOK.addListener(SWT.Selection, lsOK);
		wGet.addListener(SWT.Selection, lsGet);
		wPreview.addListener(SWT.Selection, lsPreview);
		wCancel.addListener(SWT.Selection, lsCancel);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);
		wLimit.addSelectionListener(lsDef);
		wInclRownumField.addSelectionListener(lsDef);
		wInclFilenameField.addSelectionListener(lsDef);

		// Add the file to the list of files...
		SelectionAdapter selA = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				wFilenameList.add(new String[] { wFilename.getText(), wFilemask.getText(), wExcludeFilemask.getText(), LDIFInputMeta.RequiredFilesCode[0], LDIFInputMeta.RequiredFilesCode[0]} );
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
		wbdFilename.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				int idx[] = wFilenameList.getSelectionIndices();
				wFilenameList.remove(idx);
				wFilenameList.removeEmptyRows();
				wFilenameList.setRowNums();
			}
		});

		// Edit the selected file & remove from the list...
		wbeFilename.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				int idx = wFilenameList.getSelectionIndex();
				if (idx >= 0) {
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
		wbShowFiles.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					LDIFInputMeta tfii = new LDIFInputMeta();
					getInfo(tfii);
					FileInputList fileInputList = tfii.getFiles(transMeta);
					String files[] = fileInputList.getFileStrings();
					if (files != null && files.length > 0) {
						EnterSelectionDialog esd = new EnterSelectionDialog(
								shell,
								files,
								BaseMessages.getString(PKG, "LDIFInputDialog.FilesReadSelection.DialogTitle"),
								BaseMessages.getString(PKG, "LDIFInputDialog.FilesReadSelection.DialogMessage"));
						esd.setViewOnly();
						esd.open();
					} else {
						MessageBox mb = new MessageBox(shell, SWT.OK
								| SWT.ICON_ERROR);
						mb.setMessage(BaseMessages.getString(PKG, "LDIFInputDialog.NoFileFound.DialogMessage"));
						mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
						mb.open();
					}
				} catch (KettleException ex) {
					new ErrorDialog(
							shell,
							BaseMessages.getString(PKG, "LDIFInputDialog.ErrorParsingData.DialogTitle"),
							BaseMessages.getString(PKG, "LDIFInputDialog.ErrorParsingData.DialogMessage"), ex);
				}
			}
		});
		// Enable/disable the right fields to allow a filename to be added to
		// each row...
		wInclFilename.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setIncludeFilename();
			}
		});

		// Enable/disable the right fields to allow a row number to be added to
		// each row...
		wInclRownum.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setIncludeRownum();
			}
		});
		
		// Enable/disable the right fields to allow a content type to be added to
		// each row...
		wInclContentType.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setContenType();
			}
		});
		
		// Enable/disable the right fields to allow a content type to be added to
		// each row...
		wInclDN.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setDN();
			}
		});

		// Whenever something changes, set the tooltip to the expanded version
		// of the filename:
		wFilename.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				wFilename.setToolTipText(transMeta.environmentSubstitute(wFilename.getText()));
			}
		});

		// Listen to the Browse... button
		wbbFilename.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!Const.isEmpty(wFilemask.getText()) || !Const.isEmpty(wExcludeFilemask.getText())) 
				{
					DirectoryDialog dialog = new DirectoryDialog(shell,
							SWT.OPEN);
					if (wFilename.getText() != null) {
						String fpath = transMeta.environmentSubstitute(wFilename.getText());
						dialog.setFilterPath(fpath);
					}

					if (dialog.open() != null) {
						String str = dialog.getFilterPath();
						wFilename.setText(str);
					}
				} else {
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] { "*ldif;*.LDIF",
							"*" });
					if (wFilename.getText() != null) {
						String fname = transMeta.environmentSubstitute(wFilename.getText());
						dialog.setFileName(fname);
					}

					dialog.setFilterNames(new String[] {
							BaseMessages.getString(PKG, "LDIFInputDialog.FileType"),
							BaseMessages.getString(PKG, "System.FileType.AllFiles") });

					if (dialog.open() != null) {
						String str = dialog.getFilterPath()
								+ System.getProperty("file.separator")
								+ dialog.getFileName();
						wFilename.setText(str);
					}
				}
			}
		});

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		wTabFolder.setSelection(0);

		// Set the shell size, based upon previous time...
		setSize();
		getData(input);
		ActiveFileField();
		setContenType();
		setDN();
		input.setChanged(changed);
		wFields.optWidth(true);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
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
		wLimit.setEnabled(!wFileField.getSelection());
		wlLimit.setEnabled(!wFileField.getSelection());
	}
	
	 private void setFileField()
	 {
		 if(!gotPreviousField)
		 {
			 try{
		         String value=wFilenameField.getText();  
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
					gotPreviousField=true;
					if(value!=null) wFilenameField.setText(value);
			 }catch(KettleException ke){
					new ErrorDialog(shell, BaseMessages.getString(PKG, "LDIFInputDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "LDIFInputDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
				}
		 }
	 }
	private void get() {

		try {
			LDIFInputMeta meta = new LDIFInputMeta();
			getInfo(meta);

			FileInputList inputList = meta.getFiles(transMeta);
			// Clear Fields Grid
			wFields.removeAll();
			
			if (inputList.getFiles().size() > 0) {
				// Open the file (only first file)...
				
				LDIF InputLDIF = new LDIF(KettleVFS.getFilename(inputList.getFile(0)));

				HashSet<String> attributeSet = new HashSet<String>();

				for (LDIFRecord recordLDIF = InputLDIF.nextRecord(); recordLDIF != null; recordLDIF = InputLDIF.nextRecord()) {
					// Get LDIF Content
					LDIFContent contentLDIF = recordLDIF.getContent();
					
					if (contentLDIF.getType() == LDIFContent.ATTRIBUTE_CONTENT) 
					{
						// Get only ATTRIBUTE_CONTENT

						LDIFAttributeContent attrContentLDIF = (LDIFAttributeContent) contentLDIF;
						LDAPAttribute[] attributes_LDIF = attrContentLDIF.getAttributes();

						for (int j = 0; j < attributes_LDIF.length; j++) 
						{

							LDAPAttribute attribute_DIF = attributes_LDIF[j];

							String attributeName = attribute_DIF.getName();
							if (!attributeSet.contains(attributeName)) 
							{
								// Get attribut Name
								TableItem item = new TableItem(wFields.table,SWT.NONE);
								item.setText(1, attributeName);
								item.setText(2, attributeName);

								String attributeValue = GetValue(attributes_LDIF, attributeName);
								// Try to get the Type

								if (IsDate(attributeValue)) {
									item.setText(3, "Date");
								} else if (IsInteger(attributeValue)) {
									item.setText(3, "Integer");
								} else if (IsNumber(attributeValue)) {
									item.setText(3, "Number");
								} else {
									item.setText(3, "String");
								}
								attributeSet.add(attributeName);
							}
						}
					}
				}
			}

			wFields.removeEmptyRows();
			wFields.setRowNums();
			wFields.optWidth(true);
		} catch (KettleException e) {
			new ErrorDialog(
					shell,
					BaseMessages.getString(PKG, "LDIFInputMeta.ErrorRetrieveData.DialogTitle"),
					BaseMessages.getString(PKG, "LDIFInputMeta.ErrorRetrieveData.DialogMessage"),
					e);
		} catch (Exception e) {
			new ErrorDialog(
					shell,
					BaseMessages.getString(PKG, "LDIFInputMeta.ErrorRetrieveData.DialogTitle"),
					BaseMessages.getString(PKG, "LDIFInputMeta.ErrorRetrieveData.DialogMessage"),
					e);

		}
	}

	private boolean IsInteger(String str) {
		try {
			Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private boolean IsNumber(String str) {
		try {
			Float.parseFloat(str);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private boolean IsDate(String str) {
		// TODO: What about other dates? Maybe something for a CRQ
		try {
			SimpleDateFormat fdate = new SimpleDateFormat("yy-mm-dd");
			fdate.parse(str);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private String GetValue(LDAPAttribute[] attributes_LDIF,
			String AttributValue) {
		String Stringvalue = null;

		for (int j = 0; j < attributes_LDIF.length; j++) {
			LDAPAttribute attribute_DIF = attributes_LDIF[j];
			if (attribute_DIF.getName().equalsIgnoreCase(AttributValue)) {
				Enumeration<String> valuesLDIF = attribute_DIF.getStringValues();
				// Get the first occurence
				Stringvalue = valuesLDIF.nextElement();
			}
		}

		return Stringvalue;
	}


	public void setIncludeFilename() {
		wlInclFilenameField.setEnabled(wInclFilename.getSelection());
		wInclFilenameField.setEnabled(wInclFilename.getSelection());
	}

	public void setIncludeRownum() {
		wlInclRownumField.setEnabled(wInclRownum.getSelection());
		wInclRownumField.setEnabled(wInclRownum.getSelection());
	}

	/**
	 * Read the data from the TextFileInputMeta object and show it in this
	 * dialog.
	 * 
	 * @param in
	 *            The TextFileInputMeta object to obtain the data from.
	 */
	public void getData(LDIFInputMeta in) {
		
		wFileField.setSelection(in.isFileField());
		if (in.getDynamicFilenameField()!=null) wFilenameField.setText(in.getDynamicFilenameField());
		
		if (in.getFileName() != null) {
			wFilenameList.removeAll();
			for (int i=0;i<in.getFileName().length;i++) 
			{
				wFilenameList.add(new String[] { in.getFileName()[i], in.getFileMask()[i] ,in.getExludeFileMask()[i],
						in.getRequiredFilesDesc(in.getFileRequired()[i]), in.getRequiredFilesDesc(in.getIncludeSubFolders()[i])} );
			}
			wFilenameList.removeEmptyRows();
			wFilenameList.setRowNums();
			wFilenameList.optWidth(true);
		}
		wInclFilename.setSelection(in.includeFilename());
		wInclRownum.setSelection(in.includeRowNumber());
		wInclContentType.setSelection(in.includeContentType());
		wInclDN.setSelection(in.IncludeDN());
		
		if(in.getMultiValuedSeparator()!=null)	wMultiValuedSeparator.setText(in.getMultiValuedSeparator());


		if (in.getFilenameField() != null)
			wInclFilenameField.setText(in.getFilenameField());
		if (in.getRowNumberField() != null)
			wInclRownumField.setText(in.getRowNumberField());
		if (in.getContentTypeField() != null)
			wInclContentTypeField.setText(in.getContentTypeField());
		if (in.getDNField() != null)
			wInclDNField.setText(in.getDNField());
		
		wLimit.setText("" + in.getRowLimit());
		wAddResult.setSelection(in.AddToResultFilename());
		logDebug(BaseMessages.getString(PKG, "LDIFInputDialog.Log.GettingFieldsInfo"));
		for (int i = 0; i < in.getInputFields().length; i++) {
			LDIFInputField field = in.getInputFields()[i];

			if (field != null) {
				TableItem item = wFields.table.getItem(i);
				String name = field.getName();
				String xpath = field.getAttribut();
				String type = field.getTypeDesc();
				String format = field.getFormat();
				String length = "" + field.getLength();
				String prec = "" + field.getPrecision();
				String curr = field.getCurrencySymbol();
				String group = field.getGroupSymbol();
				String decim = field.getDecimalSymbol();
				String trim = field.getTrimTypeDesc();
				String rep = field.isRepeated() ? BaseMessages.getString(PKG, "System.Combo.Yes") : BaseMessages.getString(PKG, "System.Combo.No");

				if (name != null)
					item.setText(1, name);
				if (xpath != null)
					item.setText(2, xpath);
				if (type != null)
					item.setText(3, type);
				if (format != null)
					item.setText(4, format);
				if (length != null && !"-1".equals(length))
					item.setText(5, length);
				if (prec != null && !"-1".equals(prec))
					item.setText(6, prec);
				if (curr != null)
					item.setText(7, curr);
				if (decim != null)
					item.setText(8, decim);
				if (group != null)
					item.setText(9, group);
				if (trim != null)
					item.setText(10, trim);
				if (rep != null)
					item.setText(11, rep);
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
		setIncludeRownum();

		wStepname.selectAll();
	}

	private void cancel() {
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	private void ok() {
		try {
			getInfo(input);
		} catch (KettleException e) {
			new ErrorDialog(
					shell,
					BaseMessages.getString(PKG, "LDIFInputDialog.ErrorParsingData.DialogTitle"),
					BaseMessages.getString(PKG, "LDIFInputDialog.ErrorParsingData.DialogMessage"),
					e);
		}
		dispose();
	}
   private void setContenType()
   {
	   wlInclContentTypeField.setEnabled(wInclContentType.getSelection());
	   wInclContentTypeField.setEnabled(wInclContentType.getSelection());
   }
   private void setDN()
   {
	   wlInclDNField.setEnabled(wInclDN.getSelection());
	   wInclDNField.setEnabled(wInclDN.getSelection());
   }
	private void getInfo(LDIFInputMeta in) throws KettleException {
		stepname = wStepname.getText(); // return value
		
		// copy info to TextFileInputMeta class (input)
		in.setDynamicFilenameField( wFilenameField.getText() );
		in.setFileField(wFileField.getSelection() );
		
		in.setRowLimit(Const.toLong(wLimit.getText(), 0L));
		in.setFilenameField(wInclFilenameField.getText());
		in.setRowNumberField(wInclRownumField.getText());
		in.setContentTypeField(wInclContentTypeField.getText());
		in.setDNField(wInclDNField.getText());

		in.setIncludeFilename(wInclFilename.getSelection());
		in.setIncludeRowNumber(wInclRownum.getSelection());
		in.setIncludeContentType(wInclContentType.getSelection());
		in.setIncludeDN(wInclDN.getSelection());
		
		in.setAddToResultFilename(wAddResult.getSelection());
		in.setMultiValuedSeparator(wMultiValuedSeparator.getText());

		int nrFiles = wFilenameList.getItemCount();
		int nrFields = wFields.nrNonEmpty();

		in.allocate(nrFiles, nrFields);

		in.setFileName(wFilenameList.getItems(0));
		in.setFileMask(wFilenameList.getItems(1));
		in.setExcludeFileMask(wFilenameList.getItems(2));
		in.setFileRequired(wFilenameList.getItems(3));
		in.setIncludeSubFolders(wFilenameList.getItems(4));

		for (int i = 0; i < nrFields; i++) {
			LDIFInputField field = new LDIFInputField();

			TableItem item = wFields.getNonEmpty(i);

			field.setName(item.getText(1));
			field.setAttribut(item.getText(2));
			field.setType(ValueMeta.getType(item.getText(3)));
			field.setFormat(item.getText(4));
			field.setLength(Const.toInt(item.getText(5), -1));
			field.setPrecision(Const.toInt(item.getText(6), -1));
			field.setCurrencySymbol(item.getText(7));
			field.setDecimalSymbol(item.getText(8));
			field.setGroupSymbol(item.getText(9));
			field.setTrimType(LDIFInputField
					.getTrimTypeByDesc(item.getText(10)));
			field.setRepeated(BaseMessages.getString(PKG, "System.Combo.Yes")
					.equalsIgnoreCase(item.getText(11)));

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
	private void preview() {
		try {
			// Create the LDIF input step
			LDIFInputMeta oneMeta = new LDIFInputMeta();
			getInfo(oneMeta);

			TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
	            
			EnterNumberDialog numberDialog = new EnterNumberDialog(
					shell,
					props.getDefaultPreviewSize(),
					BaseMessages.getString(PKG, "LDIFInputDialog.NumberRows.DialogTitle"),
					BaseMessages.getString(PKG, "LDIFInputDialog.NumberRows.DialogMessage"));
			int previewSize = numberDialog.open();
			if (previewSize > 0) {
				TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(
						shell, previewMeta,
						new String[] { wStepname.getText() },
						new int[] { previewSize });
				progressDialog.open();

				if (!progressDialog.isCancelled()) {
					Trans trans = progressDialog.getTrans();
					String loggingText = progressDialog.getLoggingText();

					if (trans.getResult() != null
							&& trans.getResult().getNrErrors() > 0) {
						EnterTextDialog etd = new EnterTextDialog(
								shell,
								BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"),
								BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message"),
								loggingText, true);
						etd.setReadOnly();
						etd.open();
					}

				    PreviewRowsDialog prd = new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(),
							progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog
									.getPreviewRows(wStepname.getText()), loggingText);
					prd.open();
                    
				}
			}
		} catch (KettleException e) {
			new ErrorDialog(
					shell,
					BaseMessages.getString(PKG, "LDIFInputDialog.ErrorPreviewingData.DialogTitle"),
					BaseMessages.getString(PKG, "LDIFInputDialog.ErrorPreviewingData.DialogMessage"),e);
		}
	}

	public String toString() {
		return this.getClass().getName();
	}
	private void addAdditionalFieldsTab()
    {
    	// ////////////////////////
		// START OF ADDITIONAL FIELDS TAB ///
		// ////////////////////////
    	wAdditionalFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
    	wAdditionalFieldsTab.setText(BaseMessages.getString(PKG, "LDIFInputDialog.AdditionalFieldsTab.TabTitle"));

    	wAdditionalFieldsComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wAdditionalFieldsComp);

		FormLayout fieldsLayout = new FormLayout();
		fieldsLayout.marginWidth = 3;
		fieldsLayout.marginHeight = 3;
		wAdditionalFieldsComp.setLayout(fieldsLayout);
		
		// ShortFileFieldName line
		wlShortFileFieldName = new Label(wAdditionalFieldsComp, SWT.RIGHT);
		wlShortFileFieldName.setText(BaseMessages.getString(PKG, "LDIFInputDialog.ShortFileFieldName.Label"));
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
		wlExtensionFieldName.setText(BaseMessages.getString(PKG, "LDIFInputDialog.ExtensionFieldName.Label"));
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
		wlPathFieldName.setText(BaseMessages.getString(PKG, "LDIFInputDialog.PathFieldName.Label"));
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
		wlSizeFieldName.setText(BaseMessages.getString(PKG, "LDIFInputDialog.SizeFieldName.Label"));
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
		wlIsHiddenName.setText(BaseMessages.getString(PKG, "LDIFInputDialog.IsHiddenName.Label"));
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
		wlLastModificationTimeName.setText(BaseMessages.getString(PKG, "LDIFInputDialog.LastModificationTimeName.Label"));
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
		wlUriName.setText(BaseMessages.getString(PKG, "LDIFInputDialog.UriName.Label"));
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
		wlRootUriName.setText(BaseMessages.getString(PKG, "LDIFInputDialog.RootUriName.Label"));
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