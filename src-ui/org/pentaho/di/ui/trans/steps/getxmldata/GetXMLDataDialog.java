/*************************************************************************************** 
 * Copyright (C) 2007 Samatar, Brahim.  All rights reserved. 
 * This software was developed by Samatar, Brahim and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar, Brahim.  
 * The Initial Developer is Samatar, Brahim.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/

package org.pentaho.di.ui.trans.steps.getxmldata;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
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
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.getxmldata.GetXMLDataField;
import org.pentaho.di.trans.steps.getxmldata.GetXMLDataMeta;
import org.pentaho.di.trans.steps.getxmldata.Messages;
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



public class GetXMLDataDialog extends BaseStepDialog implements StepDialogInterface
{

	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wFileTab, wContentTab, wFieldsTab;

	private Composite    wFileComp, wContentComp, wFieldsComp;
	private FormData     fdFileComp, fdContentComp, fdFieldsComp;

	private Label        wlFilename,wlXMLIsAFile;
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
	
	private Label        wluseToken;
	private Button       wuseToken;
	private FormData     fdluseToken, fduseToken;
	

	private FormData fdlXMLField, fdlXMLStreamField,fdlXMLIsAFile;
	private FormData    fdXMLField, fdXSDFileField;
	private FormData fdOutputField,fdXMLIsAFile,fdAdditionalFields,fdAddFileResult,fdXmlConf;
	private Label wlXMLField, wlXmlStreamField;
	private CCombo wXMLField;
	private Button wXMLStreamField,wXMLIsAFile;
 

	private Label        wlInclFilename;
	private Button       wInclFilename,wAddResult;
	private FormData     fdlInclFilename, fdInclFilename,fdAddResult,fdlAddResult;
	
	private Label        wlNameSpaceAware;
	private Button       wNameSpaceAware;
	private FormData     fdlNameSpaceAware, fdNameSpaceAware;
	
	private Label        wlreadUrl;
	private Button       wreadUrl;
	private FormData     fdlreadUrl, fdreadUrl;
	
	private Label        wlIgnoreComment;
	private Button       wIgnoreComment;
	private FormData     fdlIgnoreComment, fdIgnoreComment;
	
	private Label        wlValidating;
	private Button       wValidating;
	private FormData     fdlValidating, fdValidating;
	
	private Label        wlInclFilenameField;
	private TextVar      wInclFilenameField;
	private FormData     fdlInclFilenameField, fdInclFilenameField;

	private Label        wlInclRownum,wlAddResult;
	private Button       wInclRownum;
	private FormData     fdlInclRownum, fdRownum;

	private Label        wlInclRownumField;
	private TextVar      wInclRownumField;
	private FormData     fdlInclRownumField, fdInclRownumField;
	
	private Label        wlLimit;
	private Text         wLimit;
	private FormData     fdlLimit, fdLimit;

    private Label        wlLoopXPath;
    private TextVar      wLoopXPath;
    private FormData     fdlLoopXPath, fdLoopXPath;
 
    private Label        wlPrunePath;
    private TextVar      wPrunePath;
    private FormData     fdlPrunePath, fdPrunePath;
    
    private Label        wlEncoding;
    private CCombo       wEncoding;
    private FormData     fdlEncoding, fdEncoding;

   
	private TableView    wFields;
	private FormData     fdFields;
	
	private Group wOutputField;
	private Group wAdditionalFields;
	private Group wAddFileResult;
	private Group wXmlConf;
	
	private Button   wbbLoopPathList;
	private FormData fdbLoopPathList;
	
	// ignore empty files flag
	private Label        wlIgnoreEmptyFile;
	private Button       wIgnoreEmptyFile;
	private FormData     fdlIgnoreEmptyFile, fdIgnoreEmptyFile;
	

	 // do not fail if no files?
	private Label        wldoNotFailIfNoFile;
	private Button       wdoNotFailIfNoFile;
	private FormData     fdldoNotFailIfNoFile, fddoNotFailIfNoFile;

	private GetXMLDataMeta input;
	
	private boolean  gotEncodings = false;
	
	private     HashSet<String> list = new HashSet<String> ();
	
	public static final int dateLengths[] = new int[]
		{
			23, 19, 14, 10, 10, 10, 10, 8, 8, 8, 8, 6, 6
		}
		;
	
	ArrayList<String> listpath = new ArrayList<String>();
	String precNodeName=null;

	
	public GetXMLDataDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(GetXMLDataMeta)in;
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
		changed         = input.hasChanged();
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("GetXMLDataDialog.DialogTitle"));
		
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
		wFileTab.setText(Messages.getString("GetXMLDataDialog.File.Tab"));
		
		wFileComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wFileComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth  = 3;
		fileLayout.marginHeight = 3;
		wFileComp.setLayout(fileLayout);
		
		

		// ///////////////////////////////
		// START OF Output Field GROUP  //
		///////////////////////////////// 

		wOutputField = new Group(wFileComp, SWT.SHADOW_NONE);
		props.setLook(wOutputField);
		wOutputField.setText(Messages.getString("GetXMLDataDialog.wOutputField.Label"));
		
		FormLayout outputfieldgroupLayout = new FormLayout();
		outputfieldgroupLayout.marginWidth = 10;
		outputfieldgroupLayout.marginHeight = 10;
		wOutputField.setLayout(outputfieldgroupLayout);
		
		//Is XML string defined in a Field		
		wlXmlStreamField = new Label(wOutputField, SWT.RIGHT);
		wlXmlStreamField.setText(Messages.getString("GetXMLDataDialog.wlXmlStreamField.Label"));
		props.setLook(wlXmlStreamField);
		fdlXMLStreamField = new FormData();
		fdlXMLStreamField.left = new FormAttachment(0, -margin);
		fdlXMLStreamField.top = new FormAttachment(0, margin);
		fdlXMLStreamField.right = new FormAttachment(middle, -2*margin);
		wlXmlStreamField.setLayoutData(fdlXMLStreamField);
		
		
		wXMLStreamField = new Button(wOutputField, SWT.CHECK);
		props.setLook(wXMLStreamField);
		wXMLStreamField.setToolTipText(Messages.getString("GetXMLDataDialog.wXmlStreamField.Tooltip"));
		fdXSDFileField = new FormData();
		fdXSDFileField.left = new FormAttachment(middle, -margin);
		fdXSDFileField.top = new FormAttachment(0, margin);
		wXMLStreamField.setLayoutData(fdXSDFileField);		
		SelectionAdapter lsxmlstream = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	ActiveXmlStreamField();
            	input.setChanged();
            }
        };
        wXMLStreamField.addSelectionListener(lsxmlstream);
        
        
        
        //Is XML source is a file?		
		wlXMLIsAFile = new Label(wOutputField, SWT.RIGHT);
		wlXMLIsAFile.setText(Messages.getString("GetXMLDataDialog.XMLIsAFile.Label"));
		props.setLook(wlXMLIsAFile);
		fdlXMLIsAFile = new FormData();
		fdlXMLIsAFile.left = new FormAttachment(0, -margin);
		fdlXMLIsAFile.top = new FormAttachment(wXMLStreamField, margin);
		fdlXMLIsAFile.right = new FormAttachment(middle, -2*margin);
		wlXMLIsAFile.setLayoutData(fdlXMLIsAFile);
		
		wXMLIsAFile = new Button(wOutputField, SWT.CHECK);
		props.setLook(wXMLIsAFile);
		wXMLIsAFile.setToolTipText(Messages.getString("GetXMLDataDialog.XMLIsAFile.Tooltip"));
		fdXMLIsAFile = new FormData();
		fdXMLIsAFile.left = new FormAttachment(middle, -margin);
		fdXMLIsAFile.top = new FormAttachment(wXMLStreamField, margin);
		wXMLIsAFile.setLayoutData(fdXMLIsAFile);
		SelectionAdapter lsxmlisafile = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
        		if(wXMLIsAFile.getSelection())
        			wreadUrl.setSelection(false);
            	input.setChanged();
            }
        };
        wXMLIsAFile.addSelectionListener(lsxmlisafile);
        
        // read url as source ?
		wlreadUrl=new Label(wOutputField, SWT.RIGHT);
		wlreadUrl.setText(Messages.getString("GetXMLDataDialog.readUrl.Label"));
 		props.setLook(wlreadUrl);
		fdlreadUrl=new FormData();
		fdlreadUrl.left = new FormAttachment(0, -margin);
		fdlreadUrl.top  = new FormAttachment(wXMLIsAFile, margin);
		fdlreadUrl.right= new FormAttachment(middle, -2*margin);
		wlreadUrl.setLayoutData(fdlreadUrl);
		wreadUrl=new Button(wOutputField, SWT.CHECK );
 		props.setLook(wreadUrl);
		wreadUrl.setToolTipText(Messages.getString("GetXMLDataDialog.readUrl.Tooltip"));
		fdreadUrl=new FormData();
		fdreadUrl.left = new FormAttachment(middle, -margin);
		fdreadUrl.top  = new FormAttachment(wXMLIsAFile, margin);
		wreadUrl.setLayoutData(fdreadUrl);
		SelectionAdapter lsreadurl = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
        		if(wreadUrl.getSelection())
        			wXMLIsAFile.setSelection(false);
            	input.setChanged();
            }
        };
        wreadUrl.addSelectionListener(lsreadurl);
        
		// If XML string defined in a Field
		wlXMLField=new Label(wOutputField, SWT.RIGHT);
        wlXMLField.setText(Messages.getString("GetXMLDataDialog.wlXMLField.Label"));
        props.setLook(wlXMLField);
        fdlXMLField=new FormData();
        fdlXMLField.left = new FormAttachment(0, -margin);
        fdlXMLField.top  = new FormAttachment(wreadUrl, margin);
        fdlXMLField.right= new FormAttachment(middle, -2*margin);
        wlXMLField.setLayoutData(fdlXMLField);
        
        
        wXMLField=new CCombo(wOutputField, SWT.BORDER | SWT.READ_ONLY);
        wXMLField.setEditable(true);
        props.setLook(wXMLField);
        wXMLField.addModifyListener(lsMod);
        fdXMLField=new FormData();
        fdXMLField.left = new FormAttachment(middle, -margin);
        fdXMLField.top  = new FormAttachment(wreadUrl, margin);
        fdXMLField.right= new FormAttachment(100, -margin);
        wXMLField.setLayoutData(fdXMLField);
        wXMLField.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    setXMLStreamField();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );           	
        
		fdOutputField = new FormData();
		fdOutputField.left = new FormAttachment(0, margin);
		fdOutputField.top = new FormAttachment(wFilenameList, margin);
		fdOutputField.right = new FormAttachment(100, -margin);
		wOutputField.setLayoutData(fdOutputField);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Output Field GROUP
		// ///////////////////////////////////////////////////////////		

		// Filename line
		wlFilename=new Label(wFileComp, SWT.RIGHT);
		wlFilename.setText(Messages.getString("GetXMLDataDialog.Filename.Label"));
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(wOutputField, margin);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbbFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbbFilename);
		wbbFilename.setText(Messages.getString("GetXMLDataDialog.FilenameBrowse.Button"));
		wbbFilename.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(wOutputField, margin);
		wbbFilename.setLayoutData(fdbFilename);

		wbaFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbaFilename);
		wbaFilename.setText(Messages.getString("GetXMLDataDialog.FilenameAdd.Button"));
		wbaFilename.setToolTipText(Messages.getString("GetXMLDataDialog.FilenameAdd.Tooltip"));
		fdbaFilename=new FormData();
		fdbaFilename.right= new FormAttachment(wbbFilename, -margin);
		fdbaFilename.top  = new FormAttachment(wOutputField, margin);
		wbaFilename.setLayoutData(fdbaFilename);

		wFilename=new TextVar(transMeta,wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right= new FormAttachment(wbaFilename, -margin);
		fdFilename.top  = new FormAttachment(wOutputField, margin);
		wFilename.setLayoutData(fdFilename);

		wlFilemask=new Label(wFileComp, SWT.RIGHT);
		wlFilemask.setText(Messages.getString("GetXMLDataDialog.RegExp.Label"));
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
		wlFilenameList.setText(Messages.getString("GetXMLDataDialog.FilenameList.Label"));
 		props.setLook(wlFilenameList);
		fdlFilenameList=new FormData();
		fdlFilenameList.left = new FormAttachment(0, 0);
		fdlFilenameList.top  = new FormAttachment(wFilemask, margin);
		fdlFilenameList.right= new FormAttachment(middle, -margin);
		wlFilenameList.setLayoutData(fdlFilenameList);

		// Buttons to the right of the screen...
		wbdFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbdFilename);
		wbdFilename.setText(Messages.getString("GetXMLDataDialog.FilenameRemove.Button"));
		wbdFilename.setToolTipText(Messages.getString("GetXMLDataDialog.FilenameRemove.Tooltip"));
		fdbdFilename=new FormData();
		fdbdFilename.right = new FormAttachment(100, 0);
		fdbdFilename.top  = new FormAttachment (wFilemask, 40);
		wbdFilename.setLayoutData(fdbdFilename);

		wbeFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbeFilename);
		wbeFilename.setText(Messages.getString("GetXMLDataDialog.FilenameEdit.Button"));
		wbeFilename.setToolTipText(Messages.getString("GetXMLDataDialog.FilenameEdit.Tooltip"));
		fdbeFilename=new FormData();
		fdbeFilename.right = new FormAttachment(100, 0);
		fdbeFilename.left  = new FormAttachment(wbdFilename, 0, SWT.LEFT);
		fdbeFilename.top   = new FormAttachment (wbdFilename, margin);
		wbeFilename.setLayoutData(fdbeFilename);

		wbShowFiles=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbShowFiles);
		wbShowFiles.setText(Messages.getString("GetXMLDataDialog.ShowFiles.Button"));
		fdbShowFiles=new FormData();
		fdbShowFiles.left   = new FormAttachment(middle, 0);
		fdbShowFiles.bottom = new FormAttachment(100, 0);
		wbShowFiles.setLayoutData(fdbShowFiles);

		ColumnInfo[] colinfo=new ColumnInfo[3];
		colinfo[ 0]=new ColumnInfo( Messages.getString("GetXMLDataDialog.Files.Filename.Column"), ColumnInfo.COLUMN_TYPE_TEXT, false);
		colinfo[ 1]=new ColumnInfo( Messages.getString("GetXMLDataDialog.Files.Wildcard.Column"),ColumnInfo.COLUMN_TYPE_TEXT, false );

		colinfo[0].setUsingVariables(true);
		colinfo[1].setUsingVariables(true);
		colinfo[1].setToolTip(Messages.getString("GetXMLDataDialog.Files.Wildcard.Tooltip"));
		colinfo[2]=new ColumnInfo(Messages.getString("GetXMLDataDialog.Required.Column"),ColumnInfo.COLUMN_TYPE_CCOMBO,  GetXMLDataMeta.RequiredFilesDesc);
		colinfo[2].setToolTip(Messages.getString("GetXMLDataDialog.Required.Tooltip"));		
		
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
		wContentTab.setText(Messages.getString("GetXMLDataDialog.Content.Tab"));

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		wContentComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wContentComp);
		wContentComp.setLayout(contentLayout);
		
		// ///////////////////////////////
		// START OF XmlConf Field GROUP  //
		///////////////////////////////// 

		wXmlConf = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wXmlConf);
		wXmlConf.setText(Messages.getString("GetXMLDataDialog.wXmlConf.Label"));
		
		FormLayout XmlConfgroupLayout = new FormLayout();
		XmlConfgroupLayout.marginWidth = 10;
		XmlConfgroupLayout.marginHeight = 10;
		wXmlConf.setLayout(XmlConfgroupLayout);
		
		wbbLoopPathList=new Button(wXmlConf, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbbLoopPathList);
 		wbbLoopPathList.setText(Messages.getString("GetXMLDataDialog.LoopPathList.Button"));
 		wbbLoopPathList.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
		fdbLoopPathList=new FormData();
		fdbLoopPathList.right= new FormAttachment(100, 0);
		fdbLoopPathList.top  = new FormAttachment(0, 0);
		wbbLoopPathList.setLayoutData(fdbLoopPathList);

		wbbLoopPathList.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getLoopPathList(); } } );

        
		
        wlLoopXPath=new Label(wXmlConf, SWT.RIGHT);
        wlLoopXPath.setText(Messages.getString("GetXMLDataDialog.LoopXPath.Label"));
        props.setLook(wlLoopXPath);
        fdlLoopXPath=new FormData();
        fdlLoopXPath.left = new FormAttachment(0, 0);
        fdlLoopXPath.top  = new FormAttachment(0, margin);
        fdlLoopXPath.right= new FormAttachment(middle, -margin);
        wlLoopXPath.setLayoutData(fdlLoopXPath);
        wLoopXPath=new TextVar(transMeta,wXmlConf, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wLoopXPath.setToolTipText(Messages.getString("GetXMLDataDialog.LoopXPath.Tooltip"));
        props.setLook(wLoopXPath);
        wLoopXPath.addModifyListener(lsMod);
        fdLoopXPath=new FormData();
        fdLoopXPath.left = new FormAttachment(middle, 0);
        fdLoopXPath.top  = new FormAttachment(0, margin);
        fdLoopXPath.right= new FormAttachment(wbbLoopPathList, -margin);
        wLoopXPath.setLayoutData(fdLoopXPath);
        

        
        wlEncoding=new Label(wXmlConf, SWT.RIGHT);
        wlEncoding.setText(Messages.getString("GetXMLDataDialog.Encoding.Label"));
        props.setLook(wlEncoding);
        fdlEncoding=new FormData();
        fdlEncoding.left = new FormAttachment(0, 0);
        fdlEncoding.top  = new FormAttachment(wLoopXPath, margin);
        fdlEncoding.right= new FormAttachment(middle, -margin);
        wlEncoding.setLayoutData(fdlEncoding);
        wEncoding=new CCombo(wXmlConf, SWT.BORDER | SWT.READ_ONLY);
        wEncoding.setEditable(true);
        props.setLook(wEncoding);
        wEncoding.addModifyListener(lsMod);
        fdEncoding=new FormData();
        fdEncoding.left = new FormAttachment(middle, 0);
        fdEncoding.top  = new FormAttachment(wLoopXPath, margin);
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

        // Set Namespace aware ?
		wlNameSpaceAware=new Label(wXmlConf, SWT.RIGHT);
		wlNameSpaceAware.setText(Messages.getString("GetXMLDataDialog.NameSpaceAware.Label"));
 		props.setLook(wlNameSpaceAware);
		fdlNameSpaceAware=new FormData();
		fdlNameSpaceAware.left = new FormAttachment(0, 0);
		fdlNameSpaceAware.top  = new FormAttachment(wEncoding, margin);
		fdlNameSpaceAware.right= new FormAttachment(middle, -margin);
		wlNameSpaceAware.setLayoutData(fdlNameSpaceAware);
		wNameSpaceAware=new Button(wXmlConf, SWT.CHECK );
 		props.setLook(wNameSpaceAware);
		wNameSpaceAware.setToolTipText(Messages.getString("GetXMLDataDialog.NameSpaceAware.Tooltip"));
		fdNameSpaceAware=new FormData();
		fdNameSpaceAware.left = new FormAttachment(middle, 0);
		fdNameSpaceAware.top  = new FormAttachment(wEncoding, margin);
		wNameSpaceAware.setLayoutData(fdNameSpaceAware);
		
        // Ignore comments ?
		wlIgnoreComment=new Label(wXmlConf, SWT.RIGHT);
		wlIgnoreComment.setText(Messages.getString("GetXMLDataDialog.IgnoreComment.Label"));
 		props.setLook(wlIgnoreComment);
		fdlIgnoreComment=new FormData();
		fdlIgnoreComment.left = new FormAttachment(0, 0);
		fdlIgnoreComment.top  = new FormAttachment(wNameSpaceAware, margin);
		fdlIgnoreComment.right= new FormAttachment(middle, -margin);
		wlIgnoreComment.setLayoutData(fdlIgnoreComment);
		wIgnoreComment=new Button(wXmlConf, SWT.CHECK );
 		props.setLook(wIgnoreComment);
		wIgnoreComment.setToolTipText(Messages.getString("GetXMLDataDialog.IgnoreComment.Tooltip"));
		fdIgnoreComment=new FormData();
		fdIgnoreComment.left = new FormAttachment(middle, 0);
		fdIgnoreComment.top  = new FormAttachment(wNameSpaceAware, margin);
		wIgnoreComment.setLayoutData(fdIgnoreComment);
		
		
		// Validate XML?
		wlValidating=new Label(wXmlConf, SWT.RIGHT);
		wlValidating.setText(Messages.getString("GetXMLDataDialog.Validating.Label"));
 		props.setLook(wlValidating);
		fdlValidating=new FormData();
		fdlValidating.left = new FormAttachment(0, 0);
		fdlValidating.top  = new FormAttachment(wIgnoreComment, margin);
		fdlValidating.right= new FormAttachment(middle, -margin);
		wlValidating.setLayoutData(fdlValidating);
		wValidating=new Button(wXmlConf, SWT.CHECK );
 		props.setLook(wValidating);
		wValidating.setToolTipText(Messages.getString("GetXMLDataDialog.Validating.Tooltip"));
		fdValidating=new FormData();
		fdValidating.left = new FormAttachment(middle, 0);
		fdValidating.top  = new FormAttachment(wIgnoreComment, margin);
		wValidating.setLayoutData(fdValidating);
		
		 // use Token ?
		wluseToken=new Label(wXmlConf, SWT.RIGHT);
		wluseToken.setText(Messages.getString("GetXMLDataDialog.useToken.Label"));
 		props.setLook(wluseToken);
		fdluseToken=new FormData();
		fdluseToken.left = new FormAttachment(0, 0);
		fdluseToken.top  = new FormAttachment(wValidating, margin);
		fdluseToken.right= new FormAttachment(middle, -margin);
		wluseToken.setLayoutData(fdluseToken);
		wuseToken=new Button(wXmlConf, SWT.CHECK );
 		props.setLook(wuseToken);
		wuseToken.setToolTipText(Messages.getString("GetXMLDataDialog.useToken.Tooltip"));
		fduseToken=new FormData();
		fduseToken.left = new FormAttachment(middle, 0);
		fduseToken.top  = new FormAttachment(wValidating, margin);
		wuseToken.setLayoutData(fduseToken);
		
		 // Ignore Empty File
		wlIgnoreEmptyFile=new Label(wXmlConf, SWT.RIGHT);
		wlIgnoreEmptyFile.setText(Messages.getString("GetXMLDataDialog.IgnoreEmptyFile.Label"));
 		props.setLook(wlIgnoreEmptyFile);
		fdlIgnoreEmptyFile=new FormData();
		fdlIgnoreEmptyFile.left = new FormAttachment(0, 0);
		fdlIgnoreEmptyFile.top  = new FormAttachment(wuseToken, margin);
		fdlIgnoreEmptyFile.right= new FormAttachment(middle, -margin);
		wlIgnoreEmptyFile.setLayoutData(fdlIgnoreEmptyFile);
		wIgnoreEmptyFile=new Button(wXmlConf, SWT.CHECK );
 		props.setLook(wIgnoreEmptyFile);
		wIgnoreEmptyFile.setToolTipText(Messages.getString("GetXMLDataDialog.IgnoreEmptyFile.Tooltip"));
		fdIgnoreEmptyFile=new FormData();
		fdIgnoreEmptyFile.left = new FormAttachment(middle, 0);
		fdIgnoreEmptyFile.top  = new FormAttachment(wuseToken, margin);
		wIgnoreEmptyFile.setLayoutData(fdIgnoreEmptyFile);
		

		 // do not fail if no files?
		wldoNotFailIfNoFile=new Label(wXmlConf, SWT.RIGHT);
		wldoNotFailIfNoFile.setText(Messages.getString("GetXMLDataDialog.doNotFailIfNoFile.Label"));
 		props.setLook(wldoNotFailIfNoFile);
		fdldoNotFailIfNoFile=new FormData();
		fdldoNotFailIfNoFile.left = new FormAttachment(0, 0);
		fdldoNotFailIfNoFile.top  = new FormAttachment(wIgnoreEmptyFile, margin);
		fdldoNotFailIfNoFile.right= new FormAttachment(middle, -margin);
		wldoNotFailIfNoFile.setLayoutData(fdldoNotFailIfNoFile);
		wdoNotFailIfNoFile=new Button(wXmlConf, SWT.CHECK );
 		props.setLook(wdoNotFailIfNoFile);
		wdoNotFailIfNoFile.setToolTipText(Messages.getString("GetXMLDataDialog.doNotFailIfNoFile.Tooltip"));
		fddoNotFailIfNoFile=new FormData();
		fddoNotFailIfNoFile.left = new FormAttachment(middle, 0);
		fddoNotFailIfNoFile.top  = new FormAttachment(wIgnoreEmptyFile, margin);
		wdoNotFailIfNoFile.setLayoutData(fddoNotFailIfNoFile);

		wlLimit=new Label(wXmlConf, SWT.RIGHT);
		wlLimit.setText(Messages.getString("GetXMLDataDialog.Limit.Label"));
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.top  = new FormAttachment(wdoNotFailIfNoFile, margin);
		fdlLimit.right= new FormAttachment(middle, -margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new Text(wXmlConf, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.top  = new FormAttachment(wdoNotFailIfNoFile, margin);
		fdLimit.right= new FormAttachment(100, 0);
		wLimit.setLayoutData(fdLimit);
		
		// Prune path to handle large files (streaming mode)
		wlPrunePath=new Label(wXmlConf, SWT.RIGHT);
		wlPrunePath.setText(Messages.getString("GetXMLDataDialog.StreamingMode.Label"));
 		props.setLook(wlPrunePath);
		fdlPrunePath=new FormData();
		fdlPrunePath.left = new FormAttachment(0, 0);
		fdlPrunePath.top  = new FormAttachment(wLimit, margin);
		fdlPrunePath.right= new FormAttachment(middle, -margin);
		wlPrunePath.setLayoutData(fdlPrunePath);
		wPrunePath=new TextVar(transMeta,wXmlConf, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wPrunePath.setToolTipText(Messages.getString("GetXMLDataDialog.StreamingMode.Tooltip"));
 		props.setLook(wPrunePath);
		wPrunePath.addModifyListener(lsMod);
		fdPrunePath=new FormData();
		fdPrunePath.left = new FormAttachment(middle, 0);
		fdPrunePath.top  = new FormAttachment(wLimit, margin);
		fdPrunePath.right= new FormAttachment(100, 0);
		wPrunePath.setLayoutData(fdPrunePath);		
		
		fdXmlConf = new FormData();
		fdXmlConf.left = new FormAttachment(0, margin);
		fdXmlConf.top = new FormAttachment(0, margin);
		fdXmlConf.right = new FormAttachment(100, -margin);
		wXmlConf.setLayoutData(fdXmlConf);
		
		// ///////////////////////////////////////////////////////////
		// / END OF XmlConf Field GROUP
		// ///////////////////////////////////////////////////////////		

		
        
    	// ///////////////////////////////
		// START OF Additional Fields GROUP  //
		///////////////////////////////// 

		wAdditionalFields = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wAdditionalFields);
		wAdditionalFields.setText(Messages.getString("GetXMLDataDialog.wAdditionalFields.Label"));
		
		FormLayout AdditionalFieldsgroupLayout = new FormLayout();
		AdditionalFieldsgroupLayout.marginWidth = 10;
		AdditionalFieldsgroupLayout.marginHeight = 10;
		wAdditionalFields.setLayout(AdditionalFieldsgroupLayout);

		wlInclFilename=new Label(wAdditionalFields, SWT.RIGHT);
		wlInclFilename.setText(Messages.getString("GetXMLDataDialog.InclFilename.Label"));
 		props.setLook(wlInclFilename);
		fdlInclFilename=new FormData();
		fdlInclFilename.left = new FormAttachment(0, 0);
		fdlInclFilename.top  = new FormAttachment(wXmlConf, 4*margin);
		fdlInclFilename.right= new FormAttachment(middle, -margin);
		wlInclFilename.setLayoutData(fdlInclFilename);
		wInclFilename=new Button(wAdditionalFields, SWT.CHECK );
 		props.setLook(wInclFilename);
		wInclFilename.setToolTipText(Messages.getString("GetXMLDataDialog.InclFilename.Tooltip"));
		fdInclFilename=new FormData();
		fdInclFilename.left = new FormAttachment(middle, 0);
		fdInclFilename.top  = new FormAttachment(wXmlConf, 4*margin);
		wInclFilename.setLayoutData(fdInclFilename);

		wlInclFilenameField=new Label(wAdditionalFields, SWT.LEFT);
		wlInclFilenameField.setText(Messages.getString("GetXMLDataDialog.InclFilenameField.Label"));
 		props.setLook(wlInclFilenameField);
		fdlInclFilenameField=new FormData();
		fdlInclFilenameField.left = new FormAttachment(wInclFilename, margin);
		fdlInclFilenameField.top  = new FormAttachment(wLimit, 4*margin);
		wlInclFilenameField.setLayoutData(fdlInclFilenameField);
		wInclFilenameField=new TextVar(transMeta,wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclFilenameField);
		wInclFilenameField.addModifyListener(lsMod);
		fdInclFilenameField=new FormData();
		fdInclFilenameField.left = new FormAttachment(wlInclFilenameField, margin);
		fdInclFilenameField.top  = new FormAttachment(wLimit, 4*margin);
		fdInclFilenameField.right= new FormAttachment(100, 0);
		wInclFilenameField.setLayoutData(fdInclFilenameField);

		wlInclRownum=new Label(wAdditionalFields, SWT.RIGHT);
		wlInclRownum.setText(Messages.getString("GetXMLDataDialog.InclRownum.Label"));
 		props.setLook(wlInclRownum);
		fdlInclRownum=new FormData();
		fdlInclRownum.left = new FormAttachment(0, 0);
		fdlInclRownum.top  = new FormAttachment(wInclFilenameField, margin);
		fdlInclRownum.right= new FormAttachment(middle, -margin);
		wlInclRownum.setLayoutData(fdlInclRownum);
		wInclRownum=new Button(wAdditionalFields, SWT.CHECK );
 		props.setLook(wInclRownum);
		wInclRownum.setToolTipText(Messages.getString("GetXMLDataDialog.InclRownum.Tooltip"));
		fdRownum=new FormData();
		fdRownum.left = new FormAttachment(middle, 0);
		fdRownum.top  = new FormAttachment(wInclFilenameField, margin);
		wInclRownum.setLayoutData(fdRownum);

		wlInclRownumField=new Label(wAdditionalFields, SWT.RIGHT);
		wlInclRownumField.setText(Messages.getString("GetXMLDataDialog.InclRownumField.Label"));
 		props.setLook(wlInclRownumField);
		fdlInclRownumField=new FormData();
		fdlInclRownumField.left = new FormAttachment(wInclRownum, margin);
		fdlInclRownumField.top  = new FormAttachment(wInclFilenameField, margin);
		wlInclRownumField.setLayoutData(fdlInclRownumField);
		wInclRownumField=new TextVar(transMeta,wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclRownumField);
		wInclRownumField.addModifyListener(lsMod);
		fdInclRownumField=new FormData();
		fdInclRownumField.left = new FormAttachment(wlInclRownumField, margin);
		fdInclRownumField.top  = new FormAttachment(wInclFilenameField, margin);
		fdInclRownumField.right= new FormAttachment(100, 0);
		wInclRownumField.setLayoutData(fdInclRownumField);
		
		fdAdditionalFields = new FormData();
		fdAdditionalFields.left = new FormAttachment(0, margin);
		fdAdditionalFields.top = new FormAttachment(wXmlConf, margin);
		fdAdditionalFields.right = new FormAttachment(100, -margin);
		wAdditionalFields.setLayoutData(fdAdditionalFields);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Additional Fields GROUP
		// ///////////////////////////////////////////////////////////	


		// ///////////////////////////////
		// START OF AddFileResult GROUP  //
		///////////////////////////////// 

		wAddFileResult = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wAddFileResult);
		wAddFileResult.setText(Messages.getString("GetXMLDataDialog.wAddFileResult.Label"));
		
		FormLayout AddFileResultgroupLayout = new FormLayout();
		AddFileResultgroupLayout.marginWidth = 10;
		AddFileResultgroupLayout.marginHeight = 10;
		wAddFileResult.setLayout(AddFileResultgroupLayout);

		wlAddResult=new Label(wAddFileResult, SWT.RIGHT);
		wlAddResult.setText(Messages.getString("GetXMLDataDialog.AddResult.Label"));
 		props.setLook(wlAddResult);
		fdlAddResult=new FormData();
		fdlAddResult.left = new FormAttachment(0, 0);
		fdlAddResult.top  = new FormAttachment(wAdditionalFields, margin);
		fdlAddResult.right= new FormAttachment(middle, -margin);
		wlAddResult.setLayoutData(fdlAddResult);
		wAddResult=new Button(wAddFileResult, SWT.CHECK );
 		props.setLook(wAddResult);
		wAddResult.setToolTipText(Messages.getString("GetXMLDataDialog.AddResult.Tooltip"));
		fdAddResult=new FormData();
		fdAddResult.left = new FormAttachment(middle, 0);
		fdAddResult.top  = new FormAttachment(wAdditionalFields, margin);
		wAddResult.setLayoutData(fdAddResult);

		fdAddFileResult = new FormData();
		fdAddFileResult.left = new FormAttachment(0, margin);
		fdAddFileResult.top = new FormAttachment(wAdditionalFields, margin);
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
		wFieldsTab.setText(Messages.getString("GetXMLDataDialog.Fields.Tab"));
		
		FormLayout fieldsLayout = new FormLayout ();
		fieldsLayout.marginWidth  = Const.FORM_MARGIN;
		fieldsLayout.marginHeight = Const.FORM_MARGIN;
		
		wFieldsComp = new Composite(wTabFolder, SWT.NONE);
		wFieldsComp.setLayout(fieldsLayout);
 		props.setLook(wFieldsComp);
		
		wGet=new Button(wFieldsComp, SWT.PUSH);
		wGet.setText(Messages.getString("GetXMLDataDialog.GetFields.Button"));
		fdGet=new FormData();
		fdGet.left=new FormAttachment(50, 0);
		fdGet.bottom =new FormAttachment(100, 0);
		wGet.setLayoutData(fdGet);

		final int FieldsRows=input.getInputFields().length;
		
		ColumnInfo[] colinf=new ColumnInfo[]
            {
			 new ColumnInfo(
         Messages.getString("GetXMLDataDialog.FieldsTable.Name.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
         new ColumnInfo(
                 Messages.getString("GetXMLDataDialog.FieldsTable.XPath.Column"),
                 ColumnInfo.COLUMN_TYPE_TEXT,
                 false),
    	 new ColumnInfo(
    	         Messages.getString("GetXMLDataDialog.FieldsTable.Element.Column"),
    	         ColumnInfo.COLUMN_TYPE_CCOMBO,
    	         GetXMLDataField.ElementTypeDesc,
    	         true ),
			 new ColumnInfo(
         Messages.getString("GetXMLDataDialog.FieldsTable.Type.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         ValueMeta.getTypes(),
         true ),
			 new ColumnInfo(
         Messages.getString("GetXMLDataDialog.FieldsTable.Format.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         Const.getConversionFormats()),
			 new ColumnInfo(
         Messages.getString("GetXMLDataDialog.FieldsTable.Length.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("GetXMLDataDialog.FieldsTable.Precision.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("GetXMLDataDialog.FieldsTable.Currency.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("GetXMLDataDialog.FieldsTable.Decimal.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("GetXMLDataDialog.FieldsTable.Group.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("GetXMLDataDialog.FieldsTable.TrimType.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         GetXMLDataField.trimTypeDesc,
         true ),
			 new ColumnInfo(
         Messages.getString("GetXMLDataDialog.FieldsTable.Repeat.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         new String[] { Messages.getString("System.Combo.Yes"), Messages.getString("System.Combo.No") },
         true ),
     
    };
		
		colinf[0].setUsingVariables(true);
		colinf[0].setToolTip(Messages.getString("GetXMLDataDialog.FieldsTable.Name.Column.Tooltip"));
		colinf[1].setUsingVariables(true);
		colinf[1].setToolTip(Messages.getString("GetXMLDataDialog.FieldsTable.XPath.Column.Tooltip"));
		
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
		wPreview.setText(Messages.getString("GetXMLDataDialog.Button.PreviewRows"));
		
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
    					GetXMLDataMeta tfii = new GetXMLDataMeta();
    					getInfo(tfii);
                        FileInputList fileInputList = tfii.getFiles(transMeta);
    					String files[] = fileInputList.getFileStrings();
    					if (files!=null && files.length>0)
    					{
    						EnterSelectionDialog esd = new EnterSelectionDialog(shell, files, Messages.getString("GetXMLDataDialog.FilesReadSelection.DialogTitle"), Messages.getString("GetXMLDataDialog.FilesReadSelection.DialogMessage"));
    						esd.setViewOnly();
    						esd.open();
    					}
    					else
    					{
    						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
    						mb.setMessage(Messages.getString("GetXMLDataDialog.NoFileFound.DialogMessage"));
    						mb.setText(Messages.getString("System.Dialog.Error.Title"));
    						mb.open(); 
    					}
                    }
                    catch(KettleException ex)
                    {
                        new ErrorDialog(shell, Messages.getString("GetXMLDataDialog.ErrorParsingData.DialogTitle"), Messages.getString("GetXMLDataDialog.ErrorParsingData.DialogMessage"), ex);
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
					wFilename.setToolTipText(wFilename.getText());
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
							String fpath = transMeta.environmentSubstitute(wFilename.getText());
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
						dialog.setFilterExtensions(new String[] {"*.xml;*.XML", "*"});
						if (wFilename.getText()!=null)
						{
							String fname = transMeta.environmentSubstitute(wFilename.getText());
							dialog.setFileName( fname );
						}
						
						dialog.setFilterNames(new String[] {Messages.getString("System.FileType.XMLFiles"), Messages.getString("System.FileType.AllFiles")});
						
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
		ActiveXmlStreamField();
		setIncludeFilename();
		setIncludeRownum();
		input.setChanged(changed);
		wFields.optWidth(true);
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	 private void setXMLStreamField()
	 {
		 try{
	           
			 wXMLField.removeAll();
				
			 RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r!=null)
				{
		             r.getFieldNames();
		             
		             for (int i=0;i<r.getFieldNames().length;i++)
						{	
		            	 wXMLField.add(r.getFieldNames()[i]);					
							
						}
				}
		 }catch(KettleException ke){
				new ErrorDialog(shell, Messages.getString("GetXMLDataDialog.FailedToGetFields.DialogTitle"), Messages.getString("GetXMLDataDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
	 }
	 
	private void ActiveXmlStreamField()
	{		
		wlXMLField.setEnabled(wXMLStreamField.getSelection());
		wXMLField.setEnabled(wXMLStreamField.getSelection());
		wlXMLIsAFile.setEnabled(wXMLStreamField.getSelection());
		wXMLIsAFile.setEnabled(wXMLStreamField.getSelection());
		wlreadUrl.setEnabled(wXMLStreamField.getSelection());
		wreadUrl.setEnabled(wXMLStreamField.getSelection());
			
		wlFilename.setEnabled(!wXMLStreamField.getSelection());
		wbbFilename.setEnabled(!wXMLStreamField.getSelection());
		wbaFilename.setEnabled(!wXMLStreamField.getSelection());		
		wFilename.setEnabled(!wXMLStreamField.getSelection());		
		wlFilemask.setEnabled(!wXMLStreamField.getSelection());		
		wFilemask.setEnabled(!wXMLStreamField.getSelection());		
		wlFilenameList.setEnabled(!wXMLStreamField.getSelection());		
		wbdFilename.setEnabled(!wXMLStreamField.getSelection());
		wbeFilename.setEnabled(!wXMLStreamField.getSelection());
		wbShowFiles.setEnabled(!wXMLStreamField.getSelection());
		wlFilenameList.setEnabled(!wXMLStreamField.getSelection());
		wFilenameList.setEnabled(!wXMLStreamField.getSelection());
		wInclFilename.setEnabled(!wXMLStreamField.getSelection());
		wlInclFilename.setEnabled(!wXMLStreamField.getSelection());
		
		if(wXMLStreamField.getSelection())
		{
			wInclFilename.setSelection(false);
			wlInclFilenameField.setEnabled(false);
			wInclFilenameField.setEnabled(false);
		}else
		{
			wlInclFilenameField.setEnabled(wInclFilename.getSelection());
			wInclFilenameField.setEnabled(wInclFilename.getSelection());
		}
		
		if(wXMLStreamField.getSelection() && !wXMLIsAFile.getSelection())
		{
			wEncoding.setEnabled(false);
			wlEncoding.setEnabled(false);
		}
		else
		{
			wEncoding.setEnabled(true);
			wlEncoding.setEnabled(true);	
		}
		wAddResult.setEnabled(!wXMLStreamField.getSelection());
		wlAddResult.setEnabled(!wXMLStreamField.getSelection());
		wLimit.setEnabled(!wXMLStreamField.getSelection());	
		wlLimit.setEnabled(!wXMLStreamField.getSelection());
		wPreview.setEnabled(!wXMLStreamField.getSelection());
		wGet.setEnabled(!wXMLStreamField.getSelection());
		wbbLoopPathList.setEnabled(!wXMLStreamField.getSelection());
		wPrunePath.setEnabled(!wXMLStreamField.getSelection());
		wlPrunePath.setEnabled(!wXMLStreamField.getSelection());
	}
	@SuppressWarnings("unchecked")
	private void getLoopPathList()
	{
		try
		{	
			GetXMLDataMeta meta = new GetXMLDataMeta ();
			getInfo(meta);
			FileInputList fileinputList = meta.getFiles(transMeta);
    	
	    	 if (fileinputList.nrOfFiles()>0)
            { 
				// Check the first file
	    		 
				if (fileinputList.getFile(0).exists()) 
				{
					listpath.clear();
           			// get encoding. By default UTF-8
   					String encoding="UTF-8";
   					if (!Const.isEmpty(meta.getEncoding())) encoding=meta.getEncoding();
   					SAXReader reader = new SAXReader();
   	    			Document document  = reader.read( KettleVFS.getInputStream(fileinputList.getFile(0)), encoding);	
   	    			List<Node> nodes = document.selectNodes(document.getRootElement().getName());

   	    			 for (Node node : nodes) 
   	    			 {
   	    				 if(!listpath.contains(node.getPath()))
   	    				 {
   	    					 listpath.add(node.getPath());
   	    					 addLoopXPath(node);
   	    				 }
   	    			 }
					String[] list_xpath = (String[]) listpath.toArray(new String[listpath.size()]);

					EnterSelectionDialog dialog = new EnterSelectionDialog(shell, list_xpath, Messages.getString("GetXMLDataDialog.Dialog.SelectALoopPath.Title"), Messages.getString("GetXMLDataDialog.Dialog.SelectALoopPath.Message"));
					String listxpaths = dialog.open();
					
					if (listxpaths != null) wLoopXPath.setText(listxpaths);

				} else {
					// The file not exists !
					throw new KettleException(Messages.getString("GetXMLDataDialog.Exception.FileDoesNotExist", KettleVFS
							.getFilename(fileinputList.getFile(0))));
					}
           		}
           
			else
			{
				// No file specified
				 MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
		            mb.setMessage(Messages.getString("GetXMLDataDialog.FilesMissing.DialogMessage"));
		            mb.setText(Messages.getString("System.Dialog.Error.Title"));
		            mb.open(); 
			}
		}
		catch(Exception e)
	    {
	        new ErrorDialog(shell, Messages.getString("GetXMLDataDialog.UnableToGetListOfPaths.Title"), Messages.getString("GetXMLDataDialog.UnableToGetListOfPaths.Message"), e);
	    }
	}
	
	@SuppressWarnings("unchecked")
	private void get()
	{
        try
        {
        	list.clear();
        	GetXMLDataMeta meta = new GetXMLDataMeta();
        	getInfo(meta);
        	
        	//	 check if the path is given 
    		if (!checkLoopXPath(meta)) return;    	       
            
    		FileInputList inputList = meta.getFiles(transMeta);
    		
    		if (inputList.getFiles().size()>0)
            {    
            	wFields.removeAll();
            	// get encoding. By default UTF-8
    			String encoding="UTF-8";
    			if (!Const.isEmpty(meta.getEncoding()))
    			{
    				encoding=meta.getEncoding();
    			}
    			
    			SAXReader reader = new SAXReader();
    			Document document  = reader.read( KettleVFS.getInputStream(inputList.getFile(0)),encoding );	
    			String realXPath=transMeta.environmentSubstitute(meta.getLoopXPath());
    			List<Node> nodes = document.selectNodes(realXPath);
    			for (Node node : nodes) 
    			{
    			    setNodeField(node); 
    			    ChildNode(node);
    			}
    			 
                wFields.removeEmptyRows();
                wFields.setRowNums();
                wFields.optWidth(true);
            }
        }     
        catch(Exception e)
        {
            new ErrorDialog(shell, Messages.getString("GetXMLDataDialog.ErrorParsingData.DialogTitle"), Messages.getString("GetXMLDataDialog.ErrorParsingData.DialogMessage"), e);
        }
	}
	private boolean ChildNode(Node node)
	{
		 boolean rc=false; //true: we found child nodes
		 Element ce = (Element) node;
		 // List child 
		 for(int j=0;j<ce.nodeCount();j++)
		 {
			 Node cnode=ce.node(j);
			 if(!Const.isEmpty(cnode.getName()))
			 {
				 Element cce = (Element) cnode;
				 if(cce.nodeCount()>1)
				 {
					 if(ChildNode(cnode)==false){
						// We do not have child nodes ...
						 setNodeField(cnode);
						 rc=true;
					 }
				 }else
				 {
					 setNodeField(cnode);
					 rc=true;
				 }
			 } 
		 }
		 return rc;
	}
	private void addLoopXPath(Node node)
	{
		 Element ce = (Element) node;

		 // List child 
		 for(int j=0;j<ce.nodeCount();j++)
		 {
			 Node cnode=ce.node(j);

			 if(!Const.isEmpty(cnode.getName()))
			 {
				 Element cce = (Element) cnode;
				 if(!listpath.contains(cnode.getPath())) listpath.add(cnode.getPath());
				 // let's get child nodes
				 if(cce.nodeCount()>1) addLoopXPath(cnode);
			 }
		 } 
	}

	private void setAttributeField(Attribute attribute)
	{
		// Get Attribute Name
		String attributname=attribute.getName();
		String attributnametxt=cleanString(attribute.getPath());
		if(!Const.isEmpty(attributnametxt) && !list.contains(attribute.getPath()))
		{
            TableItem item = new TableItem(wFields.table, SWT.NONE);
            item.setText(1, attributname);
            item.setText(2, attributnametxt);
            item.setText(3, GetXMLDataField.ElementTypeDesc[0]);
            
            // Get attribute value
            String valueAttr =attribute.getText();
            
            // Try to get the Type
            if(IsDate(valueAttr))
            {
    			item.setText(4, "Date");
    			item.setText(5, "yyyy/MM/dd");
    			
            }
            else if(IsInteger(valueAttr))
    			item.setText(4, "Integer");
            else if(IsNumber(valueAttr))
    			item.setText(4, "Number");	    		          
            else
            	item.setText(4, "String");	    		            	
            list.add(attribute.getPath());
		}// end if
	            
	}
	private String cleanString(String inputstring)
	{
		String retval=inputstring;
		retval=retval.replace(wLoopXPath.getText(), "");
		while(retval.startsWith("/"))
		{
			retval=retval.substring(1, retval.length());
		}
		
		return retval;
	}
	
	@SuppressWarnings("unchecked")
	private void setNodeField(Node node)
	{
		Element e = (Element) node; 
		// get all attributes
		List<Attribute> lista = e.attributes(); 
		for(int i=0;i<lista.size();i++)
		{
			 setAttributeField(lista.get(i));
		}

		// Get Node Name
		String nodename=node.getName();
		String nodenametxt=cleanString(node.getPath());
		
		if(!Const.isEmpty(nodenametxt) && !list.contains(nodenametxt))
		{	
            TableItem item = new TableItem(wFields.table, SWT.NONE);
            item.setText(1, nodename);
            item.setText(2, nodenametxt);
            item.setText(3, GetXMLDataField.ElementTypeDesc[0]);

            // Get Node value
            String valueNode=node.getText();
            
			// Try to get the Type
            if(IsDate(valueNode))
            {
    			item.setText(4, "Date");
    			item.setText(5, "yyyy/MM/dd");
            }
            else if(IsInteger(valueNode))
    			item.setText(4, "Integer");
            else if(IsNumber(valueNode))
    			item.setText(4, "Number");	    		          
            else
            	item.setText(4, "String");
            
            list.add(nodenametxt);
           
		}// end if
	}
	
	private boolean IsInteger(String str)
	{
		  try 
		  {
		     Integer.parseInt(str);
		  }
		  catch(NumberFormatException e)   {return false; }
		  return true;
	}

	private boolean IsNumber(String str)
	{
		  try 
		  {
		     Float.parseFloat(str);
		  }
		  catch(Exception e)   {return false; }
		  return true;
	}

	private boolean IsDate(String str)
	{
		  // TODO: What about other dates? Maybe something for a CRQ
		  try 
		  {
		        SimpleDateFormat fdate = new SimpleDateFormat("yyyy/MM/dd");
		        fdate.setLenient(false);
		        fdate.parse(str);
		  }
		  catch(Exception e)   {return false; }
		  return true;
	}

	private void setEncodings()
    {
        // Encoding of the text file:
        if (!gotEncodings)
        {
            gotEncodings = true;
            
            wEncoding.removeAll();
            ArrayList<Charset> values = new ArrayList<Charset>(Charset.availableCharsets().values());
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
	
	public void setIncludeFilename()
	{
		wlInclFilenameField.setEnabled(wInclFilename.getSelection());
		wInclFilenameField.setEnabled(wInclFilename.getSelection());
	}

	public void setIncludeRownum()
	{
		wlInclRownumField.setEnabled(wInclRownum.getSelection());
		wInclRownumField.setEnabled(wInclRownum.getSelection());
	}

	/**
	 * Read the data from the TextFileInputMeta object and show it in this dialog.
	 * 
	 * @param in The TextFileInputMeta object to obtain the data from.
	 */
	public void getData(GetXMLDataMeta in)
	{
		if (in.getFileName() !=null) 
		{
			wFilenameList.removeAll();
			for (int i=0;i<in.getFileName().length;i++) 
			{
				wFilenameList.add(new String[] { in.getFileName()[i], in.getFileMask()[i],in.getRequiredFilesDesc(in.getFileRequired()[i]) } );
			}
			
			
			wFilenameList.removeEmptyRows();
			wFilenameList.setRowNums();
			wFilenameList.optWidth(true);
		}
		wInclFilename.setSelection(in.includeFilename());
		wInclRownum.setSelection(in.includeRowNumber());
		wAddResult.setSelection(in.addResultFile());
		wNameSpaceAware.setSelection(in.isNamespaceAware());
		wreadUrl.setSelection(in.isReadUrl());
		wIgnoreComment.setSelection(in.isIgnoreComments());
		wValidating.setSelection(in.isValidating());
		wuseToken.setSelection(in.isuseToken());
		wIgnoreEmptyFile.setSelection(in.isIgnoreEmptyFile());
		wdoNotFailIfNoFile.setSelection(in.isdoNotFailIfNoFile());
		wXMLStreamField.setSelection(in.isInFields());
		wXMLIsAFile.setSelection(in.getIsAFile());
		
		if (in.getXMLField()!=null) wXMLField.setText(in.getXMLField());
		
		if (in.getFilenameField()!=null) wInclFilenameField.setText(in.getFilenameField());
		if (in.getRowNumberField()!=null) wInclRownumField.setText(in.getRowNumberField());
		wLimit.setText(""+in.getRowLimit());
		if (in.getPrunePath()!=null) wPrunePath.setText(in.getPrunePath());
        if(in.getLoopXPath()!=null) wLoopXPath.setText(in.getLoopXPath());
        if (in.getEncoding()!=null) 
        {
        	wEncoding.setText(""+in.getEncoding());
        }else {        	
        	wEncoding.setText("UTF-8");        
        }
		
		log.logDebug(toString(), Messages.getString("GetXMLDataDialog.Log.GettingFieldsInfo"));
		for (int i=0;i<in.getInputFields().length;i++)
		{
		    GetXMLDataField field = in.getInputFields()[i];
		    
            if (field!=null)
            {
    			TableItem item = wFields.table.getItem(i);
    			String name     = field.getName();
    			String xpath	= field.getXPath();
    			String element  = field.getElementTypeDesc();
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
                if (element !=null) item.setText( 3, element);
    			if (type    !=null) item.setText( 4, type    );
    			if (format  !=null) item.setText( 5, format  );
    			if (length  !=null && !"-1".equals(length  )) item.setText( 6, length  );
    			if (prec    !=null && !"-1".equals(prec    )) item.setText( 7, prec    );
    			if (curr    !=null) item.setText( 8, curr    );
    			if (decim   !=null) item.setText( 9, decim   );
    			if (group   !=null) item.setText( 10, group   );
    			if (trim    !=null) item.setText( 11, trim    );
    			if (rep     !=null) item.setText(12, rep     );
                
            }
		}     
        
        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth(true);

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
        try
        {
            getInfo(input);
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("GetXMLDataDialog.ErrorParsingData.DialogTitle"), Messages.getString("GetXMLDataDialog.ErrorParsingData.DialogMessage"), e);
        }
		dispose();
	}
	
	private void getInfo(GetXMLDataMeta in) throws KettleException
	{
		stepname = wStepname.getText(); // return value

		// copy info to TextFileInputMeta class (input)
		in.setRowLimit( Const.toLong(wLimit.getText(), 0L) );
		in.setPrunePath(wPrunePath.getText());
        in.setLoopXPath(wLoopXPath.getText());
        in.setEncoding(wEncoding.getText());
		in.setFilenameField( wInclFilenameField.getText() );
		in.setRowNumberField( wInclRownumField.getText() );
		in.setAddResultFile( wAddResult.getSelection() );	
		in.setIncludeFilename( wInclFilename.getSelection() );
		in.setIncludeRowNumber( wInclRownum.getSelection() );
		in.setNamespaceAware( wNameSpaceAware.getSelection() );
		in.setReadUrl(wreadUrl.getSelection() );
		in.setIgnoreComments(wIgnoreComment.getSelection() );
		in.setValidating( wValidating.getSelection() );
		in.setuseToken(wuseToken.getSelection() );
		in.setIgnoreEmptyFile(wIgnoreEmptyFile.getSelection() );
		in.setdoNotFailIfNoFile(wdoNotFailIfNoFile.getSelection());
		
		in.setInFields(wXMLStreamField.getSelection());
		in.setIsAFile(wXMLIsAFile.getSelection());
		in.setXMLField(wXMLField.getText());
		
		int nrFiles     = wFilenameList.getItemCount();
		int nrFields    = wFields.nrNonEmpty();
        
		in.allocate(nrFiles, nrFields);
		in.setFileName( wFilenameList.getItems(0) );
		in.setFileMask( wFilenameList.getItems(1) );
		in.setFileRequired(wFilenameList.getItems(2));

		for (int i=0;i<nrFields;i++)
		{
		    GetXMLDataField field = new GetXMLDataField();
		    
			TableItem item  = wFields.getNonEmpty(i);
            
			field.setName( item.getText(1) );
			field.setXPath( item.getText(2) );
			field.setElementType( GetXMLDataField.getElementTypeByDesc(item.getText(3)) );
			field.setType( ValueMeta.getType(item.getText(4)) );
			field.setFormat( item.getText(5) );
			field.setLength( Const.toInt(item.getText(6), -1) );
			field.setPrecision( Const.toInt(item.getText(7), -1) );
			field.setCurrencySymbol( item.getText(8) );
			field.setDecimalSymbol( item.getText(9) );
			field.setGroupSymbol( item.getText(10) );
			field.setTrimType( GetXMLDataField.getTrimTypeByDesc(item.getText(11)) );
			field.setRepeated( Messages.getString("System.Combo.Yes").equalsIgnoreCase(item.getText(12)) );		
            
			in.getInputFields()[i] = field;
		}		
	}
	
	// check if the loop xpath is given
	private boolean checkLoopXPath(GetXMLDataMeta meta){
        if (meta.getLoopXPath()==null || meta.getLoopXPath().length()<1)
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
            mb.setMessage(Messages.getString("GetXMLDataDialog.SpecifyRepeatingElement.DialogMessage"));
            mb.setText(Messages.getString("System.Dialog.Error.Title"));
            mb.open();
            return false;
        }
        else
        {
        	return true;
        }
	}
	
	
	// Preview the data
	private void preview()
	{
        try
        {
            // Create the XML input step
            GetXMLDataMeta oneMeta = new GetXMLDataMeta();
            getInfo(oneMeta);
            
            // check if the path is given
    		if (!checkLoopXPath(oneMeta)) return;
    		 TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
            
            EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), Messages.getString("GetXMLDataDialog.NumberRows.DialogTitle"), Messages.getString("GetXMLDataDialog.NumberRows.DialogMessage"));
            
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
            new ErrorDialog(shell, Messages.getString("GetXMLDataDialog.ErrorPreviewingData.DialogTitle"), Messages.getString("GetXMLDataDialog.ErrorPreviewingData.DialogMessage"), e);
        }
	}

	public String toString()
	{
		return this.getClass().getName();
	}
}