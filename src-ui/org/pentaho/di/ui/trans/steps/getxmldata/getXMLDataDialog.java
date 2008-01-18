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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import javax.xml.xpath.XPath;
import org.xml.sax.InputSource;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.trans.steps.getxmldata.getXMLDataMeta;
import org.pentaho.di.trans.steps.getxmldata.getXMLDataField;
import org.pentaho.di.trans.steps.getxmldata.Messages;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.core.vfs.KettleVFS;



public class getXMLDataDialog extends BaseStepDialog implements StepDialogInterface
{
	private static final String[] YES_NO_COMBO = new String[] { Messages.getString("System.Combo.No"), Messages.getString("System.Combo.Yes") };
	
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

	private getXMLDataMeta input;
	
	private boolean  gotEncodings = false;
	
	private     HashSet<String> list = new HashSet<String> ();
	
	private String parentNodeName;
	
	public static final int dateLengths[] = new int[]
		{
			23, 19, 14, 10, 10, 10, 10, 8, 8, 8, 8, 6, 6
		}
		;
	
	ArrayList<String> listpath = new ArrayList<String>();
	String precNodeName=null;

	
	public getXMLDataDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(getXMLDataMeta)in;
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
		shell.setText(Messages.getString("getXMLDataDialog.DialogTitle"));
		
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
		wFileTab.setText(Messages.getString("getXMLDataDialog.File.Tab"));
		
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
		wOutputField.setText(Messages.getString("getXMLDataDialog.wOutputField.Label"));
		
		FormLayout outputfieldgroupLayout = new FormLayout();
		outputfieldgroupLayout.marginWidth = 10;
		outputfieldgroupLayout.marginHeight = 10;
		wOutputField.setLayout(outputfieldgroupLayout);
		
		//Is XML string defined in a Field		
		wlXmlStreamField = new Label(wOutputField, SWT.RIGHT);
		wlXmlStreamField.setText(Messages.getString("getXMLDataDialog.wlXmlStreamField.Label"));
		props.setLook(wlXmlStreamField);
		fdlXMLStreamField = new FormData();
		fdlXMLStreamField.left = new FormAttachment(0, 0);
		fdlXMLStreamField.top = new FormAttachment(0, margin);
		fdlXMLStreamField.right = new FormAttachment(middle, -margin);
		wlXmlStreamField.setLayoutData(fdlXMLStreamField);
		
		
		wXMLStreamField = new Button(wOutputField, SWT.CHECK);
		props.setLook(wXMLStreamField);
		wXMLStreamField.setToolTipText(Messages.getString("getXMLDataDialog.wXmlStreamField.Tooltip"));
		fdXSDFileField = new FormData();
		fdXSDFileField.left = new FormAttachment(middle, margin);
		fdXSDFileField.top = new FormAttachment(0, margin);
		wXMLStreamField.setLayoutData(fdXSDFileField);		
		SelectionAdapter lsxmlstream = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	ActivewlXmlStreamField();
            	input.setChanged();
            }
        };
        wXMLStreamField.addSelectionListener(lsxmlstream);
        
        
        
        //Is XML source is a file?		
		wlXMLIsAFile = new Label(wOutputField, SWT.RIGHT);
		wlXMLIsAFile.setText(Messages.getString("getXMLDataDialog.XMLIsAFile.Label"));
		props.setLook(wlXMLIsAFile);
		fdlXMLIsAFile = new FormData();
		fdlXMLIsAFile.left = new FormAttachment(0, 0);
		fdlXMLIsAFile.top = new FormAttachment(wXMLStreamField, margin);
		fdlXMLIsAFile.right = new FormAttachment(middle, -margin);
		wlXMLIsAFile.setLayoutData(fdlXMLIsAFile);
		
		
		wXMLIsAFile = new Button(wOutputField, SWT.CHECK);
		props.setLook(wXMLIsAFile);
		wXMLIsAFile.setToolTipText(Messages.getString("getXMLDataDialog.XMLIsAFile.Tooltip"));
		fdXMLIsAFile = new FormData();
		fdXMLIsAFile.left = new FormAttachment(middle, margin);
		fdXMLIsAFile.top = new FormAttachment(wXMLStreamField, margin);
		wXMLIsAFile.setLayoutData(fdXMLIsAFile);
		SelectionAdapter lsxmlisafile = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	ActivewlXmlStreamField();
            	input.setChanged();
            }
        };
        wXMLIsAFile.addSelectionListener(lsxmlisafile);
        
        
        
        
		// If XML string defined in a Field
		wlXMLField=new Label(wOutputField, SWT.RIGHT);
        wlXMLField.setText(Messages.getString("getXMLDataDialog.wlXMLField.Label"));
        props.setLook(wlXMLField);
        fdlXMLField=new FormData();
        fdlXMLField.left = new FormAttachment(0, 0);
        fdlXMLField.top  = new FormAttachment(wXMLIsAFile, margin);
        fdlXMLField.right= new FormAttachment(middle, -margin);
        wlXMLField.setLayoutData(fdlXMLField);
        
        
        wXMLField=new CCombo(wOutputField, SWT.BORDER | SWT.READ_ONLY);
        wXMLField.setEditable(true);
        props.setLook(wXMLField);
        wXMLField.addModifyListener(lsMod);
        fdXMLField=new FormData();
        fdXMLField.left = new FormAttachment(middle, margin);
        fdXMLField.top  = new FormAttachment(wXMLIsAFile, margin);
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
		wlFilename.setText(Messages.getString("getXMLDataDialog.Filename.Label"));
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(wOutputField, margin);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbbFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbbFilename);
		wbbFilename.setText(Messages.getString("getXMLDataDialog.FilenameBrowse.Button"));
		wbbFilename.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(wOutputField, margin);
		wbbFilename.setLayoutData(fdbFilename);

		wbaFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbaFilename);
		wbaFilename.setText(Messages.getString("getXMLDataDialog.FilenameAdd.Button"));
		wbaFilename.setToolTipText(Messages.getString("getXMLDataDialog.FilenameAdd.Tooltip"));
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
		wlFilemask.setText(Messages.getString("getXMLDataDialog.RegExp.Label"));
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
		wlFilenameList.setText(Messages.getString("getXMLDataDialog.FilenameList.Label"));
 		props.setLook(wlFilenameList);
		fdlFilenameList=new FormData();
		fdlFilenameList.left = new FormAttachment(0, 0);
		fdlFilenameList.top  = new FormAttachment(wFilemask, margin);
		fdlFilenameList.right= new FormAttachment(middle, -margin);
		wlFilenameList.setLayoutData(fdlFilenameList);

		// Buttons to the right of the screen...
		wbdFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbdFilename);
		wbdFilename.setText(Messages.getString("getXMLDataDialog.FilenameRemove.Button"));
		wbdFilename.setToolTipText(Messages.getString("getXMLDataDialog.FilenameRemove.Tooltip"));
		fdbdFilename=new FormData();
		fdbdFilename.right = new FormAttachment(100, 0);
		fdbdFilename.top  = new FormAttachment (wFilemask, 40);
		wbdFilename.setLayoutData(fdbdFilename);

		wbeFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbeFilename);
		wbeFilename.setText(Messages.getString("getXMLDataDialog.FilenameEdit.Button"));
		wbeFilename.setToolTipText(Messages.getString("getXMLDataDialog.FilenameEdit.Tooltip"));
		fdbeFilename=new FormData();
		fdbeFilename.right = new FormAttachment(100, 0);
		fdbeFilename.left  = new FormAttachment(wbdFilename, 0, SWT.LEFT);
		fdbeFilename.top   = new FormAttachment (wbdFilename, margin);
		wbeFilename.setLayoutData(fdbeFilename);

		wbShowFiles=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbShowFiles);
		wbShowFiles.setText(Messages.getString("getXMLDataDialog.ShowFiles.Button"));
		fdbShowFiles=new FormData();
		fdbShowFiles.left   = new FormAttachment(middle, 0);
		fdbShowFiles.bottom = new FormAttachment(100, 0);
		wbShowFiles.setLayoutData(fdbShowFiles);

		ColumnInfo[] colinfo=new ColumnInfo[3];
		colinfo[ 0]=new ColumnInfo( Messages.getString("getXMLDataDialog.Files.Filename.Column"), ColumnInfo.COLUMN_TYPE_TEXT, false);
		colinfo[ 1]=new ColumnInfo( Messages.getString("getXMLDataDialog.Files.Wildcard.Column"),ColumnInfo.COLUMN_TYPE_TEXT, false );

		colinfo[0].setUsingVariables(true);
		colinfo[1].setUsingVariables(true);
		colinfo[1].setToolTip(Messages.getString("getXMLDataDialog.Files.Wildcard.Tooltip"));
		colinfo[2]=new ColumnInfo(Messages.getString("getXMLDataDialog.Required.Column"),ColumnInfo.COLUMN_TYPE_CCOMBO,  YES_NO_COMBO );
		colinfo[2].setToolTip(Messages.getString("getXMLDataDialog.Required.Tooltip"));		
		
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
		wContentTab.setText(Messages.getString("getXMLDataDialog.Content.Tab"));

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
		wXmlConf.setText(Messages.getString("getXMLDataDialog.wXmlConf.Label"));
		
		FormLayout XmlConfgroupLayout = new FormLayout();
		XmlConfgroupLayout.marginWidth = 10;
		XmlConfgroupLayout.marginHeight = 10;
		wXmlConf.setLayout(XmlConfgroupLayout);
		
		wbbLoopPathList=new Button(wXmlConf, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbbLoopPathList);
 		wbbLoopPathList.setText(Messages.getString("getXMLDataDialog.LoopPathList.Button"));
 		wbbLoopPathList.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
		fdbLoopPathList=new FormData();
		fdbLoopPathList.right= new FormAttachment(100, 0);
		fdbLoopPathList.top  = new FormAttachment(0, 0);
		wbbLoopPathList.setLayoutData(fdbLoopPathList);

		wbbLoopPathList.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getLoopPathList(); } } );

        
		
        wlLoopXPath=new Label(wXmlConf, SWT.RIGHT);
        wlLoopXPath.setText(Messages.getString("getXMLDataDialog.LoopXPath.Label"));
        props.setLook(wlLoopXPath);
        fdlLoopXPath=new FormData();
        fdlLoopXPath.left = new FormAttachment(0, 0);
        fdlLoopXPath.top  = new FormAttachment(0, margin);
        fdlLoopXPath.right= new FormAttachment(middle, -margin);
        wlLoopXPath.setLayoutData(fdlLoopXPath);
        wLoopXPath=new TextVar(transMeta,wXmlConf, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wLoopXPath.setToolTipText(Messages.getString("getXMLDataDialog.LoopXPath.Tooltip"));
        props.setLook(wLoopXPath);
        wLoopXPath.addModifyListener(lsMod);
        fdLoopXPath=new FormData();
        fdLoopXPath.left = new FormAttachment(middle, 0);
        fdLoopXPath.top  = new FormAttachment(0, margin);
        fdLoopXPath.right= new FormAttachment(wbbLoopPathList, -margin);
        wLoopXPath.setLayoutData(fdLoopXPath);
        

        
        wlEncoding=new Label(wXmlConf, SWT.RIGHT);
        wlEncoding.setText(Messages.getString("getXMLDataDialog.Encoding.Label"));
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
		wlNameSpaceAware.setText(Messages.getString("getXMLDataDialog.NameSpaceAware.Label"));
 		props.setLook(wlNameSpaceAware);
		fdlNameSpaceAware=new FormData();
		fdlNameSpaceAware.left = new FormAttachment(0, 0);
		fdlNameSpaceAware.top  = new FormAttachment(wEncoding, margin);
		fdlNameSpaceAware.right= new FormAttachment(middle, -margin);
		wlNameSpaceAware.setLayoutData(fdlNameSpaceAware);
		wNameSpaceAware=new Button(wXmlConf, SWT.CHECK );
 		props.setLook(wNameSpaceAware);
		wNameSpaceAware.setToolTipText(Messages.getString("getXMLDataDialog.NameSpaceAware.Tooltip"));
		fdNameSpaceAware=new FormData();
		fdNameSpaceAware.left = new FormAttachment(middle, 0);
		fdNameSpaceAware.top  = new FormAttachment(wEncoding, margin);
		wNameSpaceAware.setLayoutData(fdNameSpaceAware);
		
		// Validate XML?
		wlValidating=new Label(wXmlConf, SWT.RIGHT);
		wlValidating.setText(Messages.getString("getXMLDataDialog.Validating.Label"));
 		props.setLook(wlValidating);
		fdlValidating=new FormData();
		fdlValidating.left = new FormAttachment(0, 0);
		fdlValidating.top  = new FormAttachment(wNameSpaceAware, margin);
		fdlValidating.right= new FormAttachment(middle, -margin);
		wlValidating.setLayoutData(fdlValidating);
		wValidating=new Button(wXmlConf, SWT.CHECK );
 		props.setLook(wValidating);
		wValidating.setToolTipText(Messages.getString("getXMLDataDialog.Validating.Tooltip"));
		fdValidating=new FormData();
		fdValidating.left = new FormAttachment(middle, 0);
		fdValidating.top  = new FormAttachment(wNameSpaceAware, margin);
		wValidating.setLayoutData(fdValidating);
		
		 // use Token ?
		wluseToken=new Label(wXmlConf, SWT.RIGHT);
		wluseToken.setText(Messages.getString("getXMLDataDialog.useToken.Label"));
 		props.setLook(wluseToken);
		fdluseToken=new FormData();
		fdluseToken.left = new FormAttachment(0, 0);
		fdluseToken.top  = new FormAttachment(wValidating, margin);
		fdluseToken.right= new FormAttachment(middle, -margin);
		wluseToken.setLayoutData(fdluseToken);
		wuseToken=new Button(wXmlConf, SWT.CHECK );
 		props.setLook(wuseToken);
		wuseToken.setToolTipText(Messages.getString("getXMLDataDialog.useToken.Tooltip"));
		fduseToken=new FormData();
		fduseToken.left = new FormAttachment(middle, 0);
		fduseToken.top  = new FormAttachment(wValidating, margin);
		wuseToken.setLayoutData(fduseToken);
		
		wlLimit=new Label(wXmlConf, SWT.RIGHT);
		wlLimit.setText(Messages.getString("getXMLDataDialog.Limit.Label"));
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.top  = new FormAttachment(wuseToken, margin);
		fdlLimit.right= new FormAttachment(middle, -margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new Text(wXmlConf, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.top  = new FormAttachment(wuseToken, margin);
		fdLimit.right= new FormAttachment(100, 0);
		wLimit.setLayoutData(fdLimit);
		
		
		
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
		wAdditionalFields.setText(Messages.getString("getXMLDataDialog.wAdditionalFields.Label"));
		
		FormLayout AdditionalFieldsgroupLayout = new FormLayout();
		AdditionalFieldsgroupLayout.marginWidth = 10;
		AdditionalFieldsgroupLayout.marginHeight = 10;
		wAdditionalFields.setLayout(AdditionalFieldsgroupLayout);
		
        

		wlInclFilename=new Label(wAdditionalFields, SWT.RIGHT);
		wlInclFilename.setText(Messages.getString("getXMLDataDialog.InclFilename.Label"));
 		props.setLook(wlInclFilename);
		fdlInclFilename=new FormData();
		fdlInclFilename.left = new FormAttachment(0, 0);
		fdlInclFilename.top  = new FormAttachment(wXmlConf, 4*margin);
		fdlInclFilename.right= new FormAttachment(middle, -margin);
		wlInclFilename.setLayoutData(fdlInclFilename);
		wInclFilename=new Button(wAdditionalFields, SWT.CHECK );
 		props.setLook(wInclFilename);
		wInclFilename.setToolTipText(Messages.getString("getXMLDataDialog.InclFilename.Tooltip"));
		fdInclFilename=new FormData();
		fdInclFilename.left = new FormAttachment(middle, 0);
		fdInclFilename.top  = new FormAttachment(wXmlConf, 4*margin);
		wInclFilename.setLayoutData(fdInclFilename);

		wlInclFilenameField=new Label(wAdditionalFields, SWT.LEFT);
		wlInclFilenameField.setText(Messages.getString("getXMLDataDialog.InclFilenameField.Label"));
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
		wlInclRownum.setText(Messages.getString("getXMLDataDialog.InclRownum.Label"));
 		props.setLook(wlInclRownum);
		fdlInclRownum=new FormData();
		fdlInclRownum.left = new FormAttachment(0, 0);
		fdlInclRownum.top  = new FormAttachment(wInclFilenameField, margin);
		fdlInclRownum.right= new FormAttachment(middle, -margin);
		wlInclRownum.setLayoutData(fdlInclRownum);
		wInclRownum=new Button(wAdditionalFields, SWT.CHECK );
 		props.setLook(wInclRownum);
		wInclRownum.setToolTipText(Messages.getString("getXMLDataDialog.InclRownum.Tooltip"));
		fdRownum=new FormData();
		fdRownum.left = new FormAttachment(middle, 0);
		fdRownum.top  = new FormAttachment(wInclFilenameField, margin);
		wInclRownum.setLayoutData(fdRownum);

		wlInclRownumField=new Label(wAdditionalFields, SWT.RIGHT);
		wlInclRownumField.setText(Messages.getString("getXMLDataDialog.InclRownumField.Label"));
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
		wAddFileResult.setText(Messages.getString("getXMLDataDialog.wAddFileResult.Label"));
		
		FormLayout AddFileResultgroupLayout = new FormLayout();
		AddFileResultgroupLayout.marginWidth = 10;
		AddFileResultgroupLayout.marginHeight = 10;
		wAddFileResult.setLayout(AddFileResultgroupLayout);

		wlAddResult=new Label(wAddFileResult, SWT.RIGHT);
		wlAddResult.setText(Messages.getString("getXMLDataDialog.AddResult.Label"));
 		props.setLook(wlAddResult);
		fdlAddResult=new FormData();
		fdlAddResult.left = new FormAttachment(0, 0);
		fdlAddResult.top  = new FormAttachment(wAdditionalFields, margin);
		fdlAddResult.right= new FormAttachment(middle, -margin);
		wlAddResult.setLayoutData(fdlAddResult);
		wAddResult=new Button(wAddFileResult, SWT.CHECK );
 		props.setLook(wAddResult);
		wAddResult.setToolTipText(Messages.getString("getXMLDataDialog.AddResult.Tooltip"));
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
		wFieldsTab.setText(Messages.getString("getXMLDataDialog.Fields.Tab"));
		
		FormLayout fieldsLayout = new FormLayout ();
		fieldsLayout.marginWidth  = Const.FORM_MARGIN;
		fieldsLayout.marginHeight = Const.FORM_MARGIN;
		
		wFieldsComp = new Composite(wTabFolder, SWT.NONE);
		wFieldsComp.setLayout(fieldsLayout);
 		props.setLook(wFieldsComp);
		
		wGet=new Button(wFieldsComp, SWT.PUSH);
		wGet.setText(Messages.getString("getXMLDataDialog.GetFields.Button"));
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
         Messages.getString("getXMLDataDialog.FieldsTable.Name.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
         new ColumnInfo(
                 Messages.getString("getXMLDataDialog.FieldsTable.XPath.Column"),
                 ColumnInfo.COLUMN_TYPE_TEXT,
                 false),
    	 new ColumnInfo(
    	         Messages.getString("getXMLDataDialog.FieldsTable.Element.Column"),
    	         ColumnInfo.COLUMN_TYPE_CCOMBO,
    	         getXMLDataField.ElementTypeDesc,
    	         true ),
			 new ColumnInfo(
         Messages.getString("getXMLDataDialog.FieldsTable.Type.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         ValueMeta.getTypes(),
         true ),
			 new ColumnInfo(
         Messages.getString("getXMLDataDialog.FieldsTable.Format.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         formats),
			 new ColumnInfo(
         Messages.getString("getXMLDataDialog.FieldsTable.Length.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("getXMLDataDialog.FieldsTable.Precision.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("getXMLDataDialog.FieldsTable.Currency.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("getXMLDataDialog.FieldsTable.Decimal.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("getXMLDataDialog.FieldsTable.Group.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("getXMLDataDialog.FieldsTable.TrimType.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         getXMLDataField.trimTypeDesc,
         true ),
			 new ColumnInfo(
         Messages.getString("getXMLDataDialog.FieldsTable.Repeat.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         new String[] { Messages.getString("System.Combo.Yes"), Messages.getString("System.Combo.No") },
         true ),
     
    };
		
		colinf[0].setUsingVariables(true);
		colinf[0].setToolTip(Messages.getString("getXMLDataDialog.FieldsTable.Name.Column.Tooltip"));
		colinf[1].setUsingVariables(true);
		colinf[1].setToolTip(Messages.getString("getXMLDataDialog.FieldsTable.XPath.Column.Tooltip"));
		
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
		wPreview.setText(Messages.getString("getXMLDataDialog.Button.PreviewRows"));
		
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
    					getXMLDataMeta tfii = new getXMLDataMeta();
    					getInfo(tfii);
                        FileInputList fileInputList = tfii.getFiles(transMeta);
    					String files[] = fileInputList.getFileStrings();
    					if (files!=null && files.length>0)
    					{
    						EnterSelectionDialog esd = new EnterSelectionDialog(shell, files, Messages.getString("getXMLDataDialog.FilesReadSelection.DialogTitle"), Messages.getString("getXMLDataDialog.FilesReadSelection.DialogMessage"));
    						esd.setViewOnly();
    						esd.open();
    					}
    					else
    					{
    						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
    						mb.setMessage(Messages.getString("getXMLDataDialog.NoFileFound.DialogMessage"));
    						mb.setText(Messages.getString("System.Dialog.Error.Title"));
    						mb.open(); 
    					}
                    }
                    catch(KettleException ex)
                    {
                        new ErrorDialog(shell, Messages.getString("getXMLDataDialog.ErrorParsingData.DialogTitle"), Messages.getString("getXMLDataDialog.ErrorParsingData.DialogMessage"), ex);
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
		ActivewlXmlStreamField();
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
				new ErrorDialog(shell, Messages.getString("getXMLDataDialog.FailedToGetFields.DialogTitle"), Messages.getString("getXMLDataDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
	 }
	 
	private void ActivewlXmlStreamField()
	{		
		wlXMLField.setEnabled(wXMLStreamField.getSelection());
		wXMLField.setEnabled(wXMLStreamField.getSelection());
		wlXMLIsAFile.setEnabled(wXMLStreamField.getSelection());
		wXMLIsAFile.setEnabled(wXMLStreamField.getSelection());
		
			
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
		wPreview.setEnabled(!wXMLStreamField.getSelection());
		wGet.setEnabled(!wXMLStreamField.getSelection());		
	}
	private void getLoopPathList()
	{

		try
		{	
			getXMLDataMeta meta = new getXMLDataMeta ();
			getInfo(meta);
			FileInputList fileinputList = meta.getFiles(transMeta);
    	
	    	 if (fileinputList.nrOfFiles()>0)
            { 
				// Check the first file
				if (fileinputList.getFile(0).exists()) 
				{

           			// get encoding. By default UTF-8
   					String encodage="UTF-8";
   					if (!Const.isEmpty(meta.getEncoding())) encodage=meta.getEncoding();
   						
   					// Get Fields from the first file 
   					DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
   						Document document = builder.parse(new InputSource(new InputStreamReader(new FileInputStream(KettleVFS.getFilename(fileinputList.getFile(0))), encodage)));        
   	    
					
   					listpath.clear();
					parentNodeName="/";
   					listpath.add(parentNodeName);
   					
   					NodeList nodesr = document.getChildNodes();
   					HashSet<String> listr = new HashSet<String> ();
   				
   					for (int n = 0; n < nodesr.getLength(); n++) 
   					{
   				   	 Node node=nodesr.item(n);
   				   	 String nodename=node.getNodeName();
   				   	 if(!listr.contains(nodename))
   				     {
   				   		 listpath.add("/"+nodename);
   				    	 if(node.getChildNodes().getLength()>0) getLoopNodes(node);
   				    	 listr.add(nodename);
   				     }
   			  	 	 

   			  	
					String[] list_xpath = (String[]) listpath.toArray(new String[listpath.size()]);

					EnterSelectionDialog dialog = new EnterSelectionDialog(shell, list_xpath, Messages.getString("getXMLDataDialog.Dialog.SelectALoopPath.Title"), Messages.getString("getXMLDataDialog.Dialog.SelectALoopPath.Message"));
					String listxpaths = dialog.open();
					if (listxpaths != null) {
						wLoopXPath.setText(listxpaths);
					}


   				
   					}
				} else {
					// The file not exists !
					throw new KettleException(Messages.getString("getXMLDataDialog.Exception.FileDoesNotExist", KettleVFS
							.getFilename(fileinputList.getFile(0))));
					}
           		}
           
			else
			{
				// No file specified
				 MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
		            mb.setMessage(Messages.getString("getXMLDataDialog.FilesMissing.DialogMessage"));
		            mb.setText(Messages.getString("System.Dialog.Error.Title"));
		            mb.open(); 
			}

		}
		catch(Throwable e)
	    {
	        new ErrorDialog(shell, Messages.getString("getXMLDataDialog.UnableToGetListOfPaths.Title"), Messages.getString("getXMLDataDialog.UnableToGetListOfPaths.Message"), e);
	    }
	}
	private void get()
	{
        try
        {
        	list.clear();
        	getXMLDataMeta meta = new getXMLDataMeta();
        	getInfo(meta);
        	
        	//	 check if the path is given 
    		if (!checkLoopXPath(meta)) return;    	       
            
    		FileInputList inputList = meta.getFiles(transMeta);
            if(meta.getIsInFields())
            {
 			
            }           
            else if (inputList.getFiles().size()>0)
            {
                
            	wFields.removeAll();
            	// get encoding. By default UTF-8
    			String encodage="UTF-8";
    			if (!Const.isEmpty(meta.getEncoding()))
    			{
    				encodage=meta.getEncoding();
    			}
    			// Get Fields from the first file 
    			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    			Document document = builder.parse(new InputSource(new InputStreamReader(new FileInputStream(KettleVFS.getFilename(inputList.getFile(0))), encodage)));        
    	    	
    			XPath xpath =XPathFactory.newInstance().newXPath();

    			NodeList widgetNodes = (NodeList) xpath.evaluate(meta.getLoopXPath(), document,XPathConstants.NODESET);
    	        
    			if (widgetNodes.getLength() >0)
    			{
    				
    				for (int n = 0; n < widgetNodes.getLength(); n++) 
    				{
    					// Let's take current node
	    				Node widgetNode = widgetNodes.item(n);
    					setAttribute(widgetNode,widgetNode.getNodeName(),0,true);
    					setNodes(widgetNode,widgetNode.getNodeName(),0);
    				} // end loop Nodes
    			}
                wFields.removeEmptyRows();
                wFields.setRowNums();
                wFields.optWidth(true);
            }
            

        }     
        catch(Exception e)
        {
            new ErrorDialog(shell, Messages.getString("getXMLDataDialog.ErrorParsingData.DialogTitle"), Messages.getString("getXMLDataDialog.ErrorParsingData.DialogMessage"), e);
        }
	}
	private void getLoopNodes(Node node)
	{
		HashSet<String> listn = new HashSet<String> ();
		String NodeName=node.getNodeName();
		

		if(NodeName!=null)
		{
			if(!node.getParentNode().getNodeName().equals(precNodeName))
			{
				if(parentNodeName.equals("/")) 
					parentNodeName="/"+NodeName;
				else
					parentNodeName=parentNodeName+"/"+NodeName;
				
				precNodeName=node.getParentNode().getNodeName();
			}
			
			
			NodeList childNodes = node.getChildNodes();
			
			
			for (int c = 0; c < childNodes.getLength(); c++) 
			{
				Node child=childNodes.item(c);
				String childNodeName=child.getNodeName();
				
				if(childNodeName!=null && !childNodeName.equals("#text") && !listn.contains(childNodeName) 
						&& child.getChildNodes().getLength()> 0)
				{
					listn.add(childNodeName);
					
					
					//log.logBasic("current Node....", childNodeName);

					
					String completeNodeName=parentNodeName+"/"+childNodeName;
					// Add path to the list
					listpath.add(completeNodeName);
					getLoopNodes(child);
		
					
					
				}
			}
			
		}
		
		
		
	}
	private void setNodes(Node widgetNode,String NodeStart, int round)
	{
		int nn=0;
		nn=widgetNode.getChildNodes().getLength();

		if(nn>1)
		{
			// Fetch file to get the child nodes ...
			for (int i = 0; i < nn; i++) 
			{
				// Get node
				Node node=widgetNode.getChildNodes().item(i);
				// Get Node Name
				String nodename=node.getNodeName();
				
				// Put attribute
				setAttribute(node,NodeStart,round,false);
			
				// Check if we have child nodes ...
				int nbe=0;
				nbe=node.getChildNodes().getLength();
				if(nbe>1) 
				{
					for (int j = 0; j < nbe; j++) 
					{
						setNodes(node.getChildNodes().item(j),nodename,1);
					}
				}else{
					
					// so let's put the node name
					setNode(node,NodeStart, round);
					
				} // end if		
			}// end for
		}else
		{
			// Put attributes
			setAttribute(widgetNode,NodeStart,round,false);
			// Put nodes
			setNode(widgetNode,NodeStart, round);
		} // end if
	}

	private void setAttribute(Node node,String NodeStart,int round,boolean start)
	{
		// How many attribute can we find here...
		String nodename=node.getNodeName();
		int nbattribute=0;
		if(node.getAttributes()!=null) 	nbattribute=node.getAttributes().getLength();
		
		if(nbattribute>0)
		{
			// We find at least one attribute
			for (int a = 0; a < node.getAttributes().getLength(); a++) 
			{
				Node childnode=node.getAttributes().item(a);
				// Get Attribute Name
				String attributname=childnode.getNodeName();
				String attributnametxt=null;
				
				if(start) attributnametxt="@" + attributname;
				else
				{
					attributnametxt=nodename +"/@" + attributname;
					if(round>0) attributnametxt = NodeStart+ '/' + nodename +"/@" + attributname;
				}

				if(!list.contains(attributnametxt))
				{
		            TableItem item = new TableItem(wFields.table, SWT.NONE);
		            item.setText(1, attributname);
		            item.setText(2, attributnametxt);
		            item.setText(3, getXMLDataField.ElementTypeDesc[0]);
		            
		            // Get attribute value
		            String valueNode =childnode.getNodeValue();
		            
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
		            list.add(attributnametxt);
				}// end if
				
			}// end loop attribute
	            
		} // end if attribute
	}
	
	private void setNode(Node node,String NodeStart, int round)
	{
		// Get Node Name
		String nodename=node.getNodeName();
		String nodenametxt=nodename;
		if(round>0) nodenametxt  = NodeStart+ '/' + nodename;
		
		if(!list.contains(nodenametxt) && !nodename.equals("#text"))
		{	
            TableItem item = new TableItem(wFields.table, SWT.NONE);
            item.setText(1, nodename);
            item.setText(2, nodenametxt);
            item.setText(3, getXMLDataField.ElementTypeDesc[0]);

            // Get Node value
            String valueNode=null;
            valueNode=XMLHandler.getNodeValue( node); 

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
	public void getData(getXMLDataMeta in)
	{
		if (in.getFileName() !=null) 
		{
			wFilenameList.removeAll();
			for (int i=0;i<in.getFileName().length;i++) 
			{
				wFilenameList.add(new String[] { in.getFileName()[i], in.getFileMask()[i],in.getFileRequired()[i] } );
			}
			
			
			wFilenameList.removeEmptyRows();
			wFilenameList.setRowNums();
			wFilenameList.optWidth(true);
		}
		wInclFilename.setSelection(in.includeFilename());
		wInclRownum.setSelection(in.includeRowNumber());
		wAddResult.setSelection(in.addResultFile());
		wNameSpaceAware.setSelection(in.isNamespaceAware());
		wValidating.setSelection(in.isValidating());
		wuseToken.setSelection(in.isuseToken());
		
		wXMLStreamField.setSelection(in.getIsInFields());
		wXMLIsAFile.setSelection(in.getIsAFile());
		
		if (in.getXMLField()!=null) wXMLField.setText(in.getXMLField());
		
		if (in.getFilenameField()!=null) wInclFilenameField.setText(in.getFilenameField());
		if (in.getRowNumberField()!=null) wInclRownumField.setText(in.getRowNumberField());
		wLimit.setText(""+in.getRowLimit());
        if(in.getLoopXPath()!=null) wLoopXPath.setText(in.getLoopXPath());
        if (in.getEncoding()!=null) 
        {
        	wEncoding.setText(""+in.getEncoding());
        }else {        	
        	wEncoding.setText("UTF-8");        
        }
		
		log.logDebug(toString(), Messages.getString("getXMLDataDialog.Log.GettingFieldsInfo"));
		for (int i=0;i<in.getInputFields().length;i++)
		{
		    getXMLDataField field = in.getInputFields()[i];
		    
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
    			if (length  !=null && !"-1".equals(length  )) item.setText( 7, length  );
    			if (prec    !=null && !"-1".equals(prec    )) item.setText( 8, prec    );
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
        try
        {
            getInfo(input);
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("getXMLDataDialog.ErrorParsingData.DialogTitle"), Messages.getString("getXMLDataDialog.ErrorParsingData.DialogMessage"), e);
        }
		dispose();
	}
	
	private void getInfo(getXMLDataMeta in) throws KettleException
	{
		stepname = wStepname.getText(); // return value

		// copy info to TextFileInputMeta class (input)
		in.setRowLimit( Const.toLong(wLimit.getText(), 0L) );
        in.setLoopXPath(wLoopXPath.getText());
        in.setEncoding(wEncoding.getText());
		in.setFilenameField( wInclFilenameField.getText() );
		in.setRowNumberField( wInclRownumField.getText() );
		in.setAddResultFile( wAddResult.getSelection() );
				
		in.setIncludeFilename( wInclFilename.getSelection() );
		in.setIncludeRowNumber( wInclRownum.getSelection() );
		
		in.setNamespaceAware( wNameSpaceAware.getSelection() );
		in.setValidating( wValidating.getSelection() );
		in.setuseToken(wuseToken.getSelection() );
		
		in.setIsInFields(wXMLStreamField.getSelection());
		in.setIsAFile(wXMLIsAFile.getSelection());
		in.setXMLField(wXMLField.getText());
		
		int nrFiles     = wFilenameList.getItemCount();
		int nrFields    = wFields.nrNonEmpty();
         
		in.allocate(nrFiles, nrFields);

		in.setFileName( wFilenameList.getItems(0) );
		in.setFileMask( wFilenameList.getItems(1) );
		in.setFileRequired( wFilenameList.getItems(2) );

		for (int i=0;i<nrFields;i++)
		{
		    getXMLDataField field = new getXMLDataField();
		    
			TableItem item  = wFields.getNonEmpty(i);
            
			field.setName( item.getText(1) );
			field.setXPath( item.getText(2) );
			field.setElementType( getXMLDataField.getElementTypeByDesc(item.getText(3)) );
			field.setType( ValueMeta.getType(item.getText(4)) );
			field.setFormat( item.getText(5) );
			field.setLength( Const.toInt(item.getText(6), -1) );
			field.setPrecision( Const.toInt(item.getText(7), -1) );
			field.setCurrencySymbol( item.getText(8) );
			field.setDecimalSymbol( item.getText(9) );
			field.setGroupSymbol( item.getText(10) );
			field.setTrimType( getXMLDataField.getTrimTypeByDesc(item.getText(11)) );
			field.setRepeated( Messages.getString("System.Combo.Yes").equalsIgnoreCase(item.getText(12)) );		
            
			in.getInputFields()[i] = field;
		}		
 
	}
	
	// check if the loop xpath is given
	private boolean checkLoopXPath(getXMLDataMeta meta){
        if (meta.getLoopXPath()==null || meta.getLoopXPath().length()<1)
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
            mb.setMessage(Messages.getString("getXMLDataDialog.SpecifyRepeatingElement.DialogMessage"));
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
            getXMLDataMeta oneMeta = new getXMLDataMeta();
            getInfo(oneMeta);
            
            // check if the path is given
    		if (!checkLoopXPath(oneMeta)) return;
    		 TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
            
            EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), Messages.getString("getXMLDataDialog.NumberRows.DialogTitle"), Messages.getString("getXMLDataDialog.NumberRows.DialogMessage"));
            
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
            new ErrorDialog(shell, Messages.getString("getXMLDataDialog.ErrorPreviewingData.DialogTitle"), Messages.getString("getXMLDataDialog.ErrorPreviewingData.DialogMessage"), e);
        }
	}

	public String toString()
	{
		return this.getClass().getName();
	}
}
