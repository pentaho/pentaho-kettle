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

package org.pentaho.di.ui.trans.steps.jsonoutput;


import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.eclipse.swt.graphics.Point;
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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.jsonoutput.JsonOutputField;
import org.pentaho.di.trans.steps.jsonoutput.JsonOutputMeta;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;


public class JsonOutputDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = JsonOutputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private CTabFolder   wTabFolder;
    private FormData     fdTabFolder;
    
    private CTabItem     wGeneralTab, wFieldsTab;

    private FormData     fdGeneralComp, fdFieldsComp;

    private Label        wlEncoding;
    private ComboVar       wEncoding;
    private FormData     fdlEncoding, fdEncoding;

    private Label        wlOutputValue;
    private TextVar       wOutputValue;
    private FormData     fdlOutputValue, fdOutputValue;
    
    private Label        wlCompatibilityMode;
    private Button       wCompatibilityMode;
    private FormData     fdlCompatibilityMode, fdCompatibilityMode;
    

    private Label        wlBlocName;
    private TextVar       wBlocName;
    private FormData     fdlBlocName, fdBlocName;

    private Label        wlNrRowsInBloc;
    private TextVar       wNrRowsInBloc;
    private FormData     fdlNrRowsInBloc, fdNrRowsInBloc;
    
    private TableView    wFields;
    private FormData     fdFields;

    private JsonOutputMeta   input;
    
    private boolean      gotEncodings = false; 
    private boolean gotPreviousFields = false;
    
    private ColumnInfo[] colinf;
    
	private Label        wlAddToResult;
	private Button       wAddToResult;
	private FormData     fdlAddToResult, fdAddToResult;
	
	
	private Group wFileName;
	private FormData fdFileName;
	
	private Label        wlFilename;
	private Button       wbFilename;
	private TextVar      wFilename;
	private FormData     fdlFilename, fdbFilename, fdFilename;
	
	private Label        wlExtension;
	private TextVar      wExtension;
	private FormData     fdlExtension, fdExtension;
	
	private Label        wlServletOutput;
  private Button       wServletOutput;
  private FormData     fdlServletOutput, fdServletOutput;

	private Label        wlCreateParentFolder;
	private Button       wCreateParentFolder;
	private FormData     fdlCreateParentFolder, fdCreateParentFolder;
	
	private Label        wlDoNotOpenNewFileInit;
	private Button       wDoNotOpenNewFileInit;
	private FormData     fdlDoNotOpenNewFileInit, fdDoNotOpenNewFileInit;


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
	
	private Label 		wlOperation;
	private CCombo 		wOperation;
	private FormData    fdlOperation;
	private FormData    fdOperation;
	
	private Group wSettings;
	private FormData fdSettings;
        
	
    private Map<String, Integer> inputFields;
    
    public JsonOutputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
    {
        super(parent, (BaseStepMeta)in, transMeta, sname);
        input=(JsonOutputMeta)in;
        inputFields =new HashMap<String, Integer>();
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
        shell.setText(BaseMessages.getString(PKG, "JsonOutputDialog.DialogTitle"));
        
        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Stepname line
        wlStepname=new Label(shell, SWT.RIGHT);
        wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
        props.setLook(wlStepname);
        fdlStepname=new FormData();
        fdlStepname.left  = new FormAttachment(0, 0);
        fdlStepname.top   = new FormAttachment(0, margin);
        fdlStepname.right = new FormAttachment(middle, -margin);
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
        // START OF General TAB///
        ///
        wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
        wGeneralTab.setText(BaseMessages.getString(PKG, "JsonOutputDialog.GeneralTab.TabTitle"));
        

        FormLayout GeneralLayout = new FormLayout ();
        GeneralLayout.marginWidth  = 3;
        GeneralLayout.marginHeight = 3;
        
        Composite wGeneralComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wGeneralComp);
        wGeneralComp.setLayout(GeneralLayout);

        // Operation
		wlOperation=new Label(wGeneralComp, SWT.RIGHT);
		wlOperation.setText(BaseMessages.getString(PKG, "JsonOutputDialog.Operation.Label")); //$NON-NLS-1$
 		props.setLook(wlOperation);
		fdlOperation=new FormData();
		fdlOperation.left = new FormAttachment(0, 0);
		fdlOperation.right= new FormAttachment(middle, -margin);
		fdlOperation.top  = new FormAttachment(wNrRowsInBloc, margin);
		wlOperation.setLayoutData(fdlOperation);
		
		wOperation=new CCombo(wGeneralComp, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wOperation);
 		wOperation.addModifyListener(lsMod);
		fdOperation=new FormData();
		fdOperation.left = new FormAttachment(middle, 0);
		fdOperation.top  = new FormAttachment(wNrRowsInBloc, margin);
		fdOperation.right= new FormAttachment(100, -margin);
		wOperation.setLayoutData(fdOperation);
		wOperation.setItems(JsonOutputMeta.operationTypeDesc);
		wOperation.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				updateOperation();
				
			}
		});
       

    	
		// Connection grouping?
		// ////////////////////////
		// START OF Settings GROUP
		// 

		wSettings = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wSettings);
		wSettings.setText(BaseMessages.getString(PKG, "JsonOutputDialog.Group.Settings.Label"));
		
		FormLayout groupFileLayout = new FormLayout();
		groupFileLayout.marginWidth = 10;
		groupFileLayout.marginHeight = 10;
		wSettings.setLayout(groupFileLayout);
		

        wlBlocName=new Label(wSettings, SWT.RIGHT);
        wlBlocName.setText(BaseMessages.getString(PKG, "JsonOutputDialog.BlocName.Label"));
        props.setLook(wlBlocName);
        fdlBlocName=new FormData();
        fdlBlocName.left = new FormAttachment(0, 0);
        fdlBlocName.top  = new FormAttachment(wOperation, margin);
        fdlBlocName.right= new FormAttachment(middle, -margin);
        wlBlocName.setLayoutData(fdlBlocName);
        wBlocName=new TextVar(transMeta, wSettings, SWT.BORDER | SWT.READ_ONLY);
        wBlocName.setEditable(true);
        props.setLook(wBlocName);
        wBlocName.addModifyListener(lsMod);
        fdBlocName=new FormData();
        fdBlocName.left = new FormAttachment(middle, 0);
        fdBlocName.top  = new FormAttachment(wOperation, margin);
        fdBlocName.right= new FormAttachment(100, 0);
        wBlocName.setLayoutData(fdBlocName);
        
        wlNrRowsInBloc=new Label(wSettings, SWT.RIGHT);
        wlNrRowsInBloc.setText(BaseMessages.getString(PKG, "JsonOutputDialog.NrRowsInBloc.Label"));
        props.setLook(wlNrRowsInBloc);
        fdlNrRowsInBloc=new FormData();
        fdlNrRowsInBloc.left = new FormAttachment(0, 0);
        fdlNrRowsInBloc.top  = new FormAttachment(wBlocName, margin);
        fdlNrRowsInBloc.right= new FormAttachment(middle, -margin);
        wlNrRowsInBloc.setLayoutData(fdlNrRowsInBloc);
        wNrRowsInBloc=new TextVar(transMeta, wSettings, SWT.BORDER | SWT.READ_ONLY);
        wNrRowsInBloc.setToolTipText(BaseMessages.getString(PKG, "JsonOutputDialog.NrRowsInBloc.ToolTip"));
        wNrRowsInBloc.setEditable(true);
        props.setLook(wNrRowsInBloc);
        wNrRowsInBloc.addModifyListener(lsMod);
        fdNrRowsInBloc=new FormData();
        fdNrRowsInBloc.left = new FormAttachment(middle, 0);
        fdNrRowsInBloc.top  = new FormAttachment(wBlocName, margin);
        fdNrRowsInBloc.right= new FormAttachment(100, 0);
        wNrRowsInBloc.setLayoutData(fdNrRowsInBloc);
        
        wlOutputValue=new Label(wSettings, SWT.RIGHT);
        wlOutputValue.setText(BaseMessages.getString(PKG, "JsonOutputDialog.OutputValue.Label"));
        props.setLook(wlOutputValue);
        fdlOutputValue=new FormData();
        fdlOutputValue.left = new FormAttachment(0, 0);
        fdlOutputValue.top  = new FormAttachment(wNrRowsInBloc, margin);
        fdlOutputValue.right= new FormAttachment(middle, -margin);
        wlOutputValue.setLayoutData(fdlOutputValue);
        wOutputValue=new TextVar(transMeta, wSettings, SWT.BORDER | SWT.READ_ONLY);
        wOutputValue.setEditable(true);
        props.setLook(wOutputValue);
        wOutputValue.addModifyListener(lsMod);
        fdOutputValue=new FormData();
        fdOutputValue.left = new FormAttachment(middle, 0);
        fdOutputValue.top  = new FormAttachment(wNrRowsInBloc, margin);
        fdOutputValue.right= new FormAttachment(100, 0);
        wOutputValue.setLayoutData(fdOutputValue);

        ////////////////////////////  start of compatibility mode
        wlCompatibilityMode=new Label(wSettings, SWT.RIGHT);
        wlCompatibilityMode.setText(BaseMessages.getString(PKG, "JsonOutputDialog.CompatibilityMode.Label"));
        props.setLook(wlCompatibilityMode);
        fdlCompatibilityMode=new FormData();
        fdlCompatibilityMode.left = new FormAttachment(0, 0);
        fdlCompatibilityMode.top  = new FormAttachment(wOutputValue, margin);
        fdlCompatibilityMode.right= new FormAttachment(middle, -margin);
        wlCompatibilityMode.setLayoutData(fdlCompatibilityMode);
        wCompatibilityMode=new Button(wSettings, SWT.CHECK);
        wCompatibilityMode.setToolTipText(BaseMessages.getString(PKG, "JsonOutputDialog.CompatibilityMode.Tooltip"));
        props.setLook(wCompatibilityMode);
        fdCompatibilityMode=new FormData();
        fdCompatibilityMode.left = new FormAttachment(middle, 0);
        fdCompatibilityMode.top  = new FormAttachment(wOutputValue, margin);
        fdCompatibilityMode.right= new FormAttachment(100, 0);
        wCompatibilityMode.setLayoutData(fdCompatibilityMode);
        wCompatibilityMode.addSelectionListener(new SelectionAdapter() 
           {
              public void widgetSelected(SelectionEvent e) 
              {
                 input.setChanged();
              }
           }
        );

		fdSettings = new FormData();
		fdSettings.left = new FormAttachment(0, margin);
		fdSettings.top = new FormAttachment(wOperation, 2*margin);
		fdSettings.right = new FormAttachment(100, -margin);
		wSettings.setLayoutData(fdSettings);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Settings GROUP
		// ///////////////////////////////////////////////////////////

    	
		// Connection grouping?
		// ////////////////////////
		// START OF FileName GROUP
		// 

		wFileName = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wFileName);
		wFileName.setText(BaseMessages.getString(PKG, "JsonOutputDialog.Group.File.Label"));
		
		FormLayout groupfilenameayout = new FormLayout();
		groupfilenameayout.marginWidth = 10;
		groupfilenameayout.marginHeight = 10;
		wFileName.setLayout(groupfilenameayout);
		
		
		// Filename line
		wlFilename=new Label(wFileName, SWT.RIGHT);
		wlFilename.setText(BaseMessages.getString(PKG, "JsonOutputDialog.Filename.Label"));
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(wSettings, margin);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbFilename=new Button(wFileName, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFilename);
		wbFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(wSettings, 0);
		wbFilename.setLayoutData(fdbFilename);
		wbFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					FileDialog dialog = new FileDialog(shell, SWT.SAVE);
					dialog.setFilterExtensions(new String[] {"*.js", "*.JS", "*"});
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
		wFilename=new TextVar(transMeta, wFileName, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.top  = new FormAttachment(wOutputValue, margin);
		fdFilename.right= new FormAttachment(wbFilename, -margin);
		wFilename.setLayoutData(fdFilename);
		
		
		// Append to end of file?
		wlAppend=new Label(wFileName, SWT.RIGHT);
		wlAppend.setText(BaseMessages.getString(PKG, "JsonOutputDialog.Append.Label"));
 		props.setLook(wlAppend);
		fdlAppend=new FormData();
		fdlAppend.left = new FormAttachment(0, 0);
		fdlAppend.top  = new FormAttachment(wFilename, margin);
		fdlAppend.right= new FormAttachment(middle, -margin);
		wlAppend.setLayoutData(fdlAppend);
		wAppend=new Button(wFileName, SWT.CHECK);
		wAppend.setToolTipText(BaseMessages.getString(PKG, "JsonOutputDialog.Append.Tooltip"));
 		props.setLook(wAppend);
		fdAppend=new FormData();
		fdAppend.left = new FormAttachment(middle, 0);
		fdAppend.top  = new FormAttachment(wFilename, margin);
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
		
		// Create Parent Folder
		wlCreateParentFolder=new Label(wFileName, SWT.RIGHT);
		wlCreateParentFolder.setText(BaseMessages.getString(PKG, "JsonOutputDialog.CreateParentFolder.Label"));
 		props.setLook(wlCreateParentFolder);
		fdlCreateParentFolder=new FormData();
		fdlCreateParentFolder.left = new FormAttachment(0, 0);
		fdlCreateParentFolder.top  = new FormAttachment(wAppend, margin);
		fdlCreateParentFolder.right= new FormAttachment(middle, -margin);
		wlCreateParentFolder.setLayoutData(fdlCreateParentFolder);
		wCreateParentFolder=new Button(wFileName, SWT.CHECK );
		wCreateParentFolder.setToolTipText(BaseMessages.getString(PKG, "JsonOutputDialog.CreateParentFolder.Tooltip"));
 		props.setLook(wCreateParentFolder);
		fdCreateParentFolder=new FormData();
		fdCreateParentFolder.left = new FormAttachment(middle, 0);
		fdCreateParentFolder.top  = new FormAttachment(wAppend, margin);
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
		wlDoNotOpenNewFileInit.setText(BaseMessages.getString(PKG, "JsonOutputDialog.DoNotOpenNewFileInit.Label"));
 		props.setLook(wlDoNotOpenNewFileInit);
		fdlDoNotOpenNewFileInit=new FormData();
		fdlDoNotOpenNewFileInit.left = new FormAttachment(0, 0);
		fdlDoNotOpenNewFileInit.top  = new FormAttachment(wCreateParentFolder, margin);
		fdlDoNotOpenNewFileInit.right= new FormAttachment(middle, -margin);
		wlDoNotOpenNewFileInit.setLayoutData(fdlDoNotOpenNewFileInit);
		wDoNotOpenNewFileInit=new Button(wFileName, SWT.CHECK );
		wDoNotOpenNewFileInit.setToolTipText(BaseMessages.getString(PKG, "JsonOutputDialog.DoNotOpenNewFileInit.Tooltip"));
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
		

        wlEncoding=new Label(wFileName, SWT.RIGHT);
        wlEncoding.setText(BaseMessages.getString(PKG, "JsonOutputDialog.Encoding.Label"));
        props.setLook(wlEncoding);
        fdlEncoding=new FormData();
        fdlEncoding.left = new FormAttachment(0, 0);
        fdlEncoding.top  = new FormAttachment(wExtension, margin);
        fdlEncoding.right= new FormAttachment(middle, -margin);
        wlEncoding.setLayoutData(fdlEncoding);
        wEncoding=new ComboVar(transMeta, wFileName, SWT.BORDER | SWT.READ_ONLY);
        wEncoding.setEditable(true);
        props.setLook(wEncoding);
        wEncoding.addModifyListener(lsMod);
        fdEncoding=new FormData();
        fdEncoding.left = new FormAttachment(middle, 0);
        fdEncoding.top  = new FormAttachment(wExtension, margin);
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

    // Output to servlet (browser, ws)
    //
    wlServletOutput=new Label(wFileName, SWT.RIGHT);
    wlServletOutput.setText(BaseMessages.getString(PKG, "JsonOutputDialog.ServletOutput.Label"));
    props.setLook(wlServletOutput);
    fdlServletOutput=new FormData();
    fdlServletOutput.left = new FormAttachment(0, 0);
    fdlServletOutput.top  = new FormAttachment(wEncoding, margin);
    fdlServletOutput.right= new FormAttachment(middle, -margin);
    wlServletOutput.setLayoutData(fdlServletOutput);
    wServletOutput=new Button(wFileName, SWT.CHECK);
    wServletOutput.setToolTipText(BaseMessages.getString(PKG, "JsonOutputDialog.ServletOutput.Tooltip"));
    props.setLook(wServletOutput);
    fdServletOutput=new FormData();
    fdServletOutput.left = new FormAttachment(middle, 0);
    fdServletOutput.top  = new FormAttachment(wEncoding, margin);
    fdServletOutput.right= new FormAttachment(100, 0);
    wServletOutput.setLayoutData(fdServletOutput);
    wServletOutput.addSelectionListener(new SelectionAdapter() 
      {
        public void widgetSelected(SelectionEvent e) 
        {
          input.setChanged();
          setFlagsServletOption();
        }
      }
    );

        
		// Create multi-part file?
		wlAddDate=new Label(wFileName, SWT.RIGHT);
		wlAddDate.setText(BaseMessages.getString(PKG, "JsonOutputDialog.AddDate.Label"));
 		props.setLook(wlAddDate);
		fdlAddDate=new FormData();
		fdlAddDate.left = new FormAttachment(0, 0);
		fdlAddDate.top  = new FormAttachment(wServletOutput, margin);
		fdlAddDate.right= new FormAttachment(middle, -margin);
		wlAddDate.setLayoutData(fdlAddDate);
		wAddDate=new Button(wFileName, SWT.CHECK);
 		props.setLook(wAddDate);
		fdAddDate=new FormData();
		fdAddDate.left = new FormAttachment(middle, 0);
		fdAddDate.top  = new FormAttachment(wServletOutput, margin);
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
		wlAddTime.setText(BaseMessages.getString(PKG, "JsonOutputDialog.AddTime.Label"));
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
		

		

		wbShowFiles=new Button(wFileName, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbShowFiles);
		wbShowFiles.setText(BaseMessages.getString(PKG, "JsonOutputDialog.ShowFiles.Button"));
		fdbShowFiles=new FormData();
		fdbShowFiles.left = new FormAttachment(middle, 0);
		fdbShowFiles.top  = new FormAttachment(wAddTime, margin*2);
		wbShowFiles.setLayoutData(fdbShowFiles);
		wbShowFiles.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					JsonOutputMeta tfoi = new JsonOutputMeta();
					getInfo(tfoi);
					String files[] = tfoi.getFiles(transMeta.environmentSubstitute(wFilename.getText()));
					if (files!=null && files.length>0)
					{
						EnterSelectionDialog esd = new EnterSelectionDialog(shell, files, 
								BaseMessages.getString(PKG, "JsonOutputDialog.SelectOutputFiles.DialogTitle"), 
								BaseMessages.getString(PKG, "JsonOutputDialog.SelectOutputFiles.DialogMessage"));
						esd.setViewOnly();
						esd.open();
					}
					else
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setMessage(BaseMessages.getString(PKG, "JsonOutputDialog.NoFilesFound.DialogMessage"));
						mb.setText(BaseMessages.getString(PKG, "System.DialogTitle.Error"));
						mb.open(); 
					}
				}
			}
		);
		

		
		// Add File to the result files name
		wlAddToResult=new Label(wFileName, SWT.RIGHT);
		wlAddToResult.setText(BaseMessages.getString(PKG, "JsonOutputDialog.AddFileToResult.Label"));
		props.setLook(wlAddToResult);
		fdlAddToResult=new FormData();
		fdlAddToResult.left  = new FormAttachment(0, 0);
		fdlAddToResult.top   = new FormAttachment(wbShowFiles, margin);
		fdlAddToResult.right = new FormAttachment(middle, -margin);
		wlAddToResult.setLayoutData(fdlAddToResult);
		wAddToResult=new Button(wFileName, SWT.CHECK);
		wAddToResult.setToolTipText(BaseMessages.getString(PKG, "JsonOutputDialog.AddFileToResult.Tooltip"));
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
		fdFileName.top = new FormAttachment(wSettings, 2*margin);
		fdFileName.right = new FormAttachment(100, -margin);
		wFileName.setLayoutData(fdFileName);
		
		// ///////////////////////////////////////////////////////////
		// / END OF FileName GROUP
		// ///////////////////////////////////////////////////////////
        
        

        fdGeneralComp = new FormData();
        fdGeneralComp.left  = new FormAttachment(0, 0);
        fdGeneralComp.top   = new FormAttachment(wStepname, margin);
        fdGeneralComp.right = new FormAttachment(100, 0);
        fdGeneralComp.bottom= new FormAttachment(100, 0);
        wGeneralComp.setLayoutData(fdGeneralComp);

        wGeneralComp.layout();
        wGeneralTab.setControl(wGeneralComp);
        
        /////////////////////////////////////////////////////////////
        /// END OF General TAB
        /////////////////////////////////////////////////////////////

        // Fields tab...
        //
        wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
        wFieldsTab.setText(BaseMessages.getString(PKG, "JsonOutputDialog.FieldsTab.TabTitle"));
        
        FormLayout fieldsLayout = new FormLayout ();
        fieldsLayout.marginWidth  = Const.FORM_MARGIN;
        fieldsLayout.marginHeight = Const.FORM_MARGIN;
        
        Composite wFieldsComp = new Composite(wTabFolder, SWT.NONE);
        wFieldsComp.setLayout(fieldsLayout);
        props.setLook(wFieldsComp);

        wGet=new Button(wFieldsComp, SWT.PUSH);
        wGet.setText(BaseMessages.getString(PKG, "JsonOutputDialog.Get.Button"));
        wGet.setToolTipText(BaseMessages.getString(PKG, "JsonOutputDialog.Get.Tooltip"));


        setButtonPositions(new Button[] { wGet}, margin, null);

        final int FieldsRows=input.getOutputFields().length;
        
        colinf=new ColumnInfo[]
        {
            new ColumnInfo(BaseMessages.getString(PKG, "JsonOutputDialog.Fieldname.Column"),   ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
            new ColumnInfo(BaseMessages.getString(PKG, "JsonOutputDialog.ElementName.Column"), ColumnInfo.COLUMN_TYPE_TEXT,   false),
         };
        colinf[1].setUsingVariables(true);
        wFields=new TableView(transMeta, wFieldsComp, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
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
        
		  // 
        // Search the fields in the background
		
        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                StepMeta stepMeta = transMeta.findStep(stepname);
                if (stepMeta!=null)
                {
                    try
                    {
                    	RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
                       
                        // Remember these fields...
                        for (int i=0;i<row.size();i++)
                        {
                            inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
                        }
                        setComboBoxes();
                    }
                    catch(KettleException e)
                    {
                    	logError( BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
                    }
                }
            }
        };
        new Thread(runnable).start();

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
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        
        wCancel=new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        setButtonPositions(new Button[] { wOK, wCancel }, margin, wTabFolder);

        // Add listeners
        lsOK       = new Listener() { public void handleEvent(Event e) { ok();       } };
        lsGet      = new Listener() { public void handleEvent(Event e) { get();      } };
        lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();   } };
        
        wOK.addListener    (SWT.Selection, lsOK    );
        wGet.addListener   (SWT.Selection, lsGet   );
        wCancel.addListener(SWT.Selection, lsCancel);
        
        lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
        
        wStepname.addSelectionListener( lsDef );
        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

        lsResize = new Listener() 
        {
            public void handleEvent(Event event) 
            {
                Point size = shell.getSize();
                wFields.setSize(size.x-10, size.y-50);
                wFields.table.setSize(size.x-10, size.y-50);
                wFields.redraw();
            }
        };
        shell.addListener(SWT.Resize, lsResize);

        wTabFolder.setSelection(0);
        
        // Set the shell size, based upon previous time...
        setSize();
        
        getData();
        updateOperation();
        input.setChanged(changed);
        
        shell.open();
        while (!shell.isDisposed())
        {
                if (!display.readAndDispatch()) display.sleep();
        }
        return stepname;
    }
    
    protected void setFlagsServletOption() {
      boolean enableFilename = !wServletOutput.getSelection();
      wlFilename.setEnabled(enableFilename);
      wFilename.setEnabled(enableFilename);
      wlDoNotOpenNewFileInit.setEnabled(enableFilename);
      wDoNotOpenNewFileInit.setEnabled(enableFilename);
      wlCreateParentFolder.setEnabled(enableFilename);
      wCreateParentFolder.setEnabled(enableFilename);
      wlExtension.setEnabled(enableFilename);
      wExtension.setEnabled(enableFilename);
      wlAddDate.setEnabled(enableFilename);
      wAddDate.setEnabled(enableFilename);
      wlAddTime.setEnabled(enableFilename);
      wAddTime.setEnabled(enableFilename);
      wlAppend.setEnabled(enableFilename);
      wAppend.setEnabled(enableFilename);
      wbShowFiles.setEnabled(enableFilename);
      wlAddToResult.setEnabled(enableFilename);
      wAddToResult.setEnabled(enableFilename);
    }
    
    protected void setComboBoxes()
    {
        // Something was changed in the row.
        //
        final Map<String, Integer> fields = new HashMap<String, Integer>();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);
        
        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);

        String fieldNames[] = (String[]) entries.toArray(new String[entries.size()]);

        Const.sortStrings(fieldNames);
        colinf[0].setComboValues(fieldNames);
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
            else 
            	wEncoding.select(Const.indexOfString("UTF-8", wEncoding.getItems() ));
        }
    }


  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wBlocName.setText(Const.NVL(input.getJsonBloc(), ""));
    wNrRowsInBloc.setText(Const.NVL(input.getNrRowsInBloc(), ""));
    wEncoding.setText(Const.NVL(input.getEncoding(), ""));
    wOutputValue.setText(Const.NVL(input.getOutputValue(), ""));
    wCompatibilityMode.setSelection(input.isCompatibilityMode());
    wOperation.setText(JsonOutputMeta.getOperationTypeDesc(input.getOperationType()));
    wFilename.setText(Const.NVL(input.getFileName(), ""));
    wCreateParentFolder.setSelection(input.isCreateParentFolder());
    wExtension.setText(Const.NVL(input.getExtension(), "js"));
    wServletOutput.setSelection(input.isServletOutput());
    setFlagsServletOption();

    wAddDate.setSelection(input.isDateInFilename());
    wAddTime.setSelection(input.isTimeInFilename());
    wAppend.setSelection(input.isFileAppended());

    wEncoding.setText(Const.NVL(input.getEncoding(), ""));
    wAddToResult.setSelection(input.AddToResult());
    wDoNotOpenNewFileInit.setSelection(input.isDoNotOpenNewFileInit());

    if (isDebug())
      logDebug(BaseMessages.getString(PKG, "JsonOutputDialog.Log.GettingFieldsInfo"));

    for (int i = 0; i < input.getOutputFields().length; i++) {
      JsonOutputField field = input.getOutputFields()[i];

      TableItem item = wFields.table.getItem(i);
      item.setText(1, Const.NVL(field.getFieldName(), ""));
      item.setText(2, Const.NVL(field.getElementName(), ""));
    }

    wFields.optWidth(true);
    wStepname.selectAll();
  }
    
    private void cancel()
    {
        stepname=null;
        
        input.setChanged(backupChanged);

        dispose();
    }
    
    private void getInfo(JsonOutputMeta jsometa) {
      jsometa.setJsonBloc(wBlocName.getText());
      jsometa.setNrRowsInBloc(wNrRowsInBloc.getText());
      jsometa.setEncoding(wEncoding.getText());
      jsometa.setOutputValue(wOutputValue.getText());
      jsometa.setCompatibilityMode(wCompatibilityMode.getSelection());
      jsometa.setOperationType(JsonOutputMeta.getOperationTypeByDesc(wOperation.getText()));
      jsometa.setCreateParentFolder(wCreateParentFolder.getSelection());
      jsometa.setFileName(wFilename.getText());
      jsometa.setExtension(wExtension.getText());
      jsometa.setServletOutput(wServletOutput.getSelection());
      jsometa.setFileAppended(wAppend.getSelection());

      jsometa.setDateInFilename(wAddDate.getSelection());
      jsometa.setTimeInFilename(wAddTime.getSelection());
  
      jsometa.setEncoding(wEncoding.getText());
      jsometa.setAddToResult(wAddToResult.getSelection());
      jsometa.setDoNotOpenNewFileInit(wDoNotOpenNewFileInit.getSelection());
  
      int nrfields = wFields.nrNonEmpty();
  
      jsometa.allocate(nrfields);
  
      for (int i = 0; i < nrfields; i++) {
        JsonOutputField field = new JsonOutputField();
  
        TableItem item = wFields.getNonEmpty(i);
        field.setFieldName(item.getText(1));
        field.setElementName(item.getText(2));
        jsometa.getOutputFields()[i] = field;
      }
    }
    
    private void ok()
    {
		if (Const.isEmpty(wStepname.getText())) return;
		
        stepname = wStepname.getText(); // return value
        
        getInfo(input);
        
        dispose();
    }
    
    private void get()
    {
    	if(gotPreviousFields) return;
        try
        {
            RowMetaInterface r = transMeta.getPrevStepFields(stepname);
            if (r!=null)
            {
                BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1, 2 }, new int[] { 3 }, 5, 6, new TableItemInsertListener()
                    {
                        public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                        {
                            if (v.isNumber())
                            {
                                if (v.getLength()>0)
                                {
                                    int le=v.getLength();
                                    int pr=v.getPrecision();
                                    
                                    if (v.getPrecision()<=0)
                                    {
                                        pr=0;
                                    }
                                    
                                    String mask=" ";
                                    for (int m=0;m<le-pr;m++)
                                    {
                                        mask+="0";
                                    }
                                    if (pr>0) mask+=".";
                                    for (int m=0;m<pr;m++)
                                    {
                                        mask+="0";
                                    }
                                    tableItem.setText(4, mask);
                                }
                            }
                            return true;
                        }
                    }
                );
            }
        }
        catch(KettleException ke)
        {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"), BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), ke);
        }
    }
    
    private void updateOperation()
    {
      int opType = JsonOutputMeta.getOperationTypeByDesc(wOperation.getText());
    	boolean activeFile= opType!=JsonOutputMeta.OPERATION_TYPE_OUTPUT_VALUE;
    	
    	wlFilename.setEnabled(activeFile);
    	wFilename.setEnabled(activeFile);
    	wbFilename.setEnabled(activeFile);
    	wlExtension.setEnabled(activeFile);
    	wExtension.setEnabled(activeFile);
    	wlEncoding.setEnabled(activeFile);
    	wEncoding.setEnabled(activeFile);
    	wlAppend.setEnabled(activeFile);
    	wAppend.setEnabled(activeFile);
    	wlCreateParentFolder.setEnabled(activeFile);
    	wCreateParentFolder.setEnabled(activeFile);
    	wlDoNotOpenNewFileInit.setEnabled(activeFile);
    	wDoNotOpenNewFileInit.setEnabled(activeFile);
    	wlAddDate.setEnabled(activeFile);
    	wAddDate.setEnabled(activeFile);
    	wlAddTime.setEnabled(activeFile);
    	wAddTime.setEnabled(activeFile);
    	wlAddToResult.setEnabled(activeFile);
    	wAddToResult.setEnabled(activeFile);
    	wbShowFiles.setEnabled(activeFile);
    	
    	wlServletOutput.setEnabled(opType==JsonOutputMeta.OPERATION_TYPE_WRITE_TO_FILE || opType==JsonOutputMeta.OPERATION_TYPE_BOTH);
      wServletOutput.setEnabled(opType==JsonOutputMeta.OPERATION_TYPE_WRITE_TO_FILE || opType==JsonOutputMeta.OPERATION_TYPE_BOTH);
    	
    	boolean activeOutputValue= JsonOutputMeta.getOperationTypeByDesc(wOperation.getText())!=JsonOutputMeta.OPERATION_TYPE_WRITE_TO_FILE;
    	
    	wlOutputValue.setEnabled(activeOutputValue);
    	wOutputValue.setEnabled(activeOutputValue);
    	
    	setFlagsServletOption();
    }
}
