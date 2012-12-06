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

package org.pentaho.di.ui.trans.steps.xsdvalidator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.xsdvalidator.XsdValidatorMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class XsdValidatorDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = XsdValidatorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	

	private FormData    fdResultField,fdlXMLStream, fdXMLStream,fdTabFolder,fdOutputStringField,fdlOutputStringField
						,fdlFilename, fdbFilename,fdFilename,fdValidationMsg,fdIfXMLValid,fdIfXMLUnValid,
						fdXSDSource,fdlXSDSource,fdXMLSourceFile,fdlXMLSourceFile,fdXSDDefinedColumn,fdlXSDDefinedColumn;
	
	private LabelTextVar wResultField,wValidationMsg,wIfXMLValid,wIfXMLUnValid;
	
    private CCombo       wXMLStream,wXSDSource,wXSDDefinedColumn;
    
    private FormData fdlAddValidationMsg,fdAddValidationMsg;
    
 
	private Label wlXMLStream,wlFilename,wlAddValidationMsg,wlOutputStringField,wlXSDSource,
		wlXMLSourceFile,wlXSDDefinedColumn;
    
	private Button wbbFilename,wAddValidationMsg,wOutputStringField,wXMLSourceFile;

	private XsdValidatorMeta input;
	
	private Group wOutputFields,wXSD,wXML;
	private FormData fdOutputFields,fdXSD,fdXML;
	
	private TextVar wFilename;

	
	private CTabFolder   wTabFolder;
	
	private CTabItem     wGeneralTab;
	private Composite    wGeneralComp;
	private FormData     fdGeneralComp;
	
	private boolean gotPrevious=false;
	
	public XsdValidatorDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(XsdValidatorMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "XsdValidatorDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "XsdValidatorDialog.Stepname.Label")); //$NON-NLS-1$
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
		wGeneralTab.setText(BaseMessages.getString(PKG, "XsdValidatorDialog.GeneralTab.TabTitle"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
		

		// ////////////////////////
		// START OF XML GROUP
		// 

		wXML = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wXML);
		wXML.setText("XML source");
		
		FormLayout groupXML = new FormLayout();
		groupXML.marginWidth = 10;
		groupXML.marginHeight = 10;
		wXML.setLayout(groupXML);
		
		
		// XML Source = file ?
		wlXMLSourceFile = new Label(wXML, SWT.RIGHT);
		wlXMLSourceFile.setText(BaseMessages.getString(PKG, "XsdValidatorDialog.XMLSourceFile.Label"));
		props.setLook(wlXMLSourceFile);
		fdlXMLSourceFile = new FormData();
		fdlXMLSourceFile.left = new FormAttachment(0, 0);
		fdlXMLSourceFile.top = new FormAttachment(wStepname, 2*margin);
		fdlXMLSourceFile.right = new FormAttachment(middle, -margin);
		wlXMLSourceFile.setLayoutData(fdlXMLSourceFile);
		wXMLSourceFile = new Button(wXML, SWT.CHECK);
		props.setLook(wXMLSourceFile);
		wXMLSourceFile.setToolTipText(BaseMessages.getString(PKG, "XsdValidatorDialog.XMLSourceFile.Tooltip"));
		fdXMLSourceFile = new FormData();
		fdXMLSourceFile.left = new FormAttachment(middle, margin);
		fdXMLSourceFile.top = new FormAttachment(wStepname, 2*margin);
		wXMLSourceFile.setLayoutData(fdXMLSourceFile);
		
		
		// XML Stream Field
		wlXMLStream=new Label(wXML, SWT.RIGHT);
		wlXMLStream.setText(BaseMessages.getString(PKG, "XsdValidatorDialog.XMLStream.Label"));
        props.setLook(wlXMLStream);
        fdlXMLStream=new FormData();
        fdlXMLStream.left = new FormAttachment(0, 0);
        fdlXMLStream.top  = new FormAttachment(wXMLSourceFile, margin);
        fdlXMLStream.right= new FormAttachment(middle, -margin);
        wlXMLStream.setLayoutData(fdlXMLStream);
        wXMLStream=new CCombo(wXML, SWT.BORDER | SWT.READ_ONLY);
        wXMLStream.setEditable(true);
        props.setLook(wXMLStream);
        wXMLStream.addModifyListener(lsMod);
        fdXMLStream=new FormData();
        fdXMLStream.left = new FormAttachment(middle, margin);
        fdXMLStream.top  = new FormAttachment(wXMLSourceFile, margin);
        fdXMLStream.right= new FormAttachment(100, -margin);
        wXMLStream.setLayoutData(fdXMLStream);
        wXMLStream.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    PopulateFields();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );

        

    	fdXML = new FormData();
    	fdXML.left = new FormAttachment(0, margin);
    	fdXML.top = new FormAttachment(wStepname, margin);
    	fdXML.right = new FormAttachment(100, -margin);
    	wXML.setLayoutData(fdXML);
		
		// ///////////////////////////////////////////////////////////
		// / END OF XML GROUP
		// ///////////////////////////////////////////////////////////
        
        
		// ////////////////////////
		// START OF OutputFields GROUP
		// 

		wOutputFields = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wOutputFields);
		wOutputFields.setText("Output Fields");
		
		FormLayout groupLayout = new FormLayout();
		groupLayout.marginWidth = 10;
		groupLayout.marginHeight = 10;
		wOutputFields.setLayout(groupLayout);
		
		
	      // Output Fieldame
        wResultField = new LabelTextVar(transMeta,wOutputFields, BaseMessages.getString(PKG, "XsdValidatorDialog.ResultField.Label"), 
        		BaseMessages.getString(PKG, "XsdValidatorDialog.ResultField.Tooltip"));
        props.setLook(wResultField);
        wResultField .addModifyListener(lsMod);
        fdResultField  = new FormData();
        fdResultField .left = new FormAttachment(0, 0);
        fdResultField .top = new FormAttachment(wXML, margin);
        fdResultField .right = new FormAttachment(100, 0);
        wResultField .setLayoutData(fdResultField );
        
        // Output String Field ?
        wlOutputStringField = new Label(wOutputFields, SWT.RIGHT);
        wlOutputStringField.setText(BaseMessages.getString(PKG, "XsdValidatorDialog.OutputStringField.Label"));
		props.setLook(wlOutputStringField);
		fdlOutputStringField = new FormData();
		fdlOutputStringField.left = new FormAttachment(0, 0);
		fdlOutputStringField.top = new FormAttachment(wResultField, 2*margin);
		fdlOutputStringField.right = new FormAttachment(middle, -margin);
		wlOutputStringField.setLayoutData(fdlOutputStringField);
		wOutputStringField = new Button(wOutputFields, SWT.CHECK);
		props.setLook(wOutputStringField);
		wOutputStringField.setToolTipText(BaseMessages.getString(PKG, "XsdValidatorDialog.OutputStringField.Tooltip"));
		fdOutputStringField = new FormData();
		fdOutputStringField.left = new FormAttachment(middle, margin);
		fdOutputStringField.top = new FormAttachment(wResultField, 2*margin);
		wOutputStringField.setLayoutData(fdOutputStringField);
		wOutputStringField.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	activeOutputStringField();
            }
        });
        
	      // Output if XML is valid field
        wIfXMLValid = new LabelTextVar(transMeta,wOutputFields, BaseMessages.getString(PKG, "XsdValidatorDialog.IfXMLValid.Label"), 
        		BaseMessages.getString(PKG, "XsdValidatorDialog.IfXMLValid.Tooltip"));
        props.setLook(wIfXMLValid);
        wIfXMLValid .addModifyListener(lsMod);
        fdIfXMLValid  = new FormData();
        fdIfXMLValid .left = new FormAttachment(0, 0);
        fdIfXMLValid .top = new FormAttachment(wOutputStringField, margin);
        fdIfXMLValid .right = new FormAttachment(100, 0);
        wIfXMLValid .setLayoutData(fdIfXMLValid );
        
        
	      // Output if XML is not valid field
        wIfXMLUnValid = new LabelTextVar(transMeta,wOutputFields, BaseMessages.getString(PKG, "XsdValidatorDialog.IfXMLUnValid.Label"), 
        		BaseMessages.getString(PKG, "XsdValidatorDialog.IfXMLUnValid.Tooltip"));
        props.setLook(wIfXMLUnValid);
        wIfXMLUnValid .addModifyListener(lsMod);
        fdIfXMLUnValid  = new FormData();
        fdIfXMLUnValid .left = new FormAttachment(0, 0);
        fdIfXMLUnValid .top = new FormAttachment(wIfXMLValid, margin);
        fdIfXMLUnValid .right = new FormAttachment(100, 0);
        wIfXMLUnValid .setLayoutData(fdIfXMLUnValid );
        
        
        
        
        
        
        
        // Add validation message ?
        wlAddValidationMsg = new Label(wOutputFields, SWT.RIGHT);
        wlAddValidationMsg.setText(BaseMessages.getString(PKG, "XsdValidatorDialog.AddValidationMsg.Label"));
		props.setLook(wlAddValidationMsg);
		fdlAddValidationMsg = new FormData();
		fdlAddValidationMsg.left = new FormAttachment(0, 0);
		fdlAddValidationMsg.top = new FormAttachment(wIfXMLUnValid, 2*margin);
		fdlAddValidationMsg.right = new FormAttachment(middle, -margin);
		wlAddValidationMsg.setLayoutData(fdlAddValidationMsg);
		wAddValidationMsg = new Button(wOutputFields, SWT.CHECK);
		props.setLook(wAddValidationMsg);
		wAddValidationMsg.setToolTipText(BaseMessages.getString(PKG, "XsdValidatorDialog.AddValidationMsg.Tooltip"));
		fdAddValidationMsg = new FormData();
		fdAddValidationMsg.left = new FormAttachment(middle, margin);
		fdAddValidationMsg.top = new FormAttachment(wIfXMLUnValid, 2*margin);
		wAddValidationMsg.setLayoutData(fdAddValidationMsg);
		wAddValidationMsg.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	activeValidationMsg();
            }
        });
        
	      // Validation Msg Fieldame
        wValidationMsg = new LabelTextVar(transMeta,wOutputFields, BaseMessages.getString(PKG, "XsdValidatorDialog.ValidationMsg.Label"), 
        		BaseMessages.getString(PKG, "XsdValidatorDialog.ValidationMsg.Tooltip"));
        props.setLook(wValidationMsg);
        wValidationMsg.addModifyListener(lsMod);
        fdValidationMsg  = new FormData();
        fdValidationMsg .left = new FormAttachment(0, 0);
        fdValidationMsg .top = new FormAttachment(wAddValidationMsg, margin);
        fdValidationMsg .right = new FormAttachment(100, 0);
        wValidationMsg .setLayoutData(fdValidationMsg );
        
    	fdOutputFields = new FormData();
    	fdOutputFields.left = new FormAttachment(0, margin);
    	fdOutputFields.top = new FormAttachment(wXML, margin);
    	fdOutputFields.right = new FormAttachment(100, -margin);
		wOutputFields.setLayoutData(fdOutputFields);
		
		// ///////////////////////////////////////////////////////////
		// / END OF OUTPUT FIELDS GROUP
		// ///////////////////////////////////////////////////////////
		
		
		// ////////////////////////
		// START OF XSD GROUP
		// 

		wXSD = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wXSD);
		wXSD.setText("XML Schema Definition");
		
		FormLayout groupXSD = new FormLayout();
		groupXSD.marginWidth = 10;
		groupXSD.marginHeight = 10;
		wXSD.setLayout(groupLayout);
		
		
		
		// XSD Source?
		wlXSDSource=new Label(wXSD, SWT.RIGHT);
		wlXSDSource.setText(BaseMessages.getString(PKG, "XsdValidatorDialog.XSDSource.Label"));
        props.setLook(wlXSDSource);
        fdlXSDSource=new FormData();
        fdlXSDSource.left = new FormAttachment(0, 0);
        fdlXSDSource.top  = new FormAttachment(wStepname, margin);
        fdlXSDSource.right= new FormAttachment(middle, -margin);
        wlXSDSource.setLayoutData(fdlXSDSource);
        wXSDSource=new CCombo(wXSD, SWT.BORDER | SWT.READ_ONLY);
        wXSDSource.setEditable(true);
        props.setLook(wXSDSource);
        wXSDSource.addModifyListener(lsMod);
        fdXSDSource=new FormData();
        fdXSDSource.left = new FormAttachment(middle, margin);
        fdXSDSource.top  = new FormAttachment(wStepname, margin);
        fdXSDSource.right= new FormAttachment(100, -margin);
        wXSDSource.setLayoutData(fdXSDSource);
        wXSDSource.add(BaseMessages.getString(PKG, "XsdValidatorDialog.XSDSource.IS_A_FILE"));
        wXSDSource.add(BaseMessages.getString(PKG, "XsdValidatorDialog.XSDSource.IS_A_FIELD"));
        wXSDSource.add(BaseMessages.getString(PKG, "XsdValidatorDialog.XSDSource.NO_NEED"));
        wXSDSource.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	setXSDSource();
            }
        });
       

		// XSD Filename
		wlFilename = new Label(wXSD, SWT.RIGHT);
		wlFilename.setText(BaseMessages.getString(PKG, "XsdValidatorDialog.XSDFilename.Label"));
		props.setLook(wlFilename);
		fdlFilename = new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top = new FormAttachment(wXSDSource, margin);
		fdlFilename.right = new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbbFilename = new Button(wXSD, SWT.PUSH | SWT.CENTER);
		props.setLook(wbbFilename);
		wbbFilename.setText(BaseMessages.getString(PKG, "XsdValidatorDialog.FilenameBrowse.Button"));
		wbbFilename.setToolTipText(BaseMessages.getString(PKG, "System.Tooltip.BrowseForFileOrDirAndAdd"));
		fdbFilename = new FormData();
		fdbFilename.right = new FormAttachment(100, 0);
		fdbFilename.top = new FormAttachment(wXSDSource, margin);
		wbbFilename.setLayoutData(fdbFilename);

		wFilename = new TextVar(transMeta,wXSD, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename = new FormData();
		fdFilename.left = new FormAttachment(middle, margin);
		fdFilename.right = new FormAttachment(wbbFilename, -margin);
		fdFilename.top = new FormAttachment(wXSDSource, margin);
		wFilename.setLayoutData(fdFilename);

		
		// XSD file defined in a column
		wlXSDDefinedColumn=new Label(wXSD, SWT.RIGHT);
		wlXSDDefinedColumn.setText(BaseMessages.getString(PKG, "XsdValidatorDialog.XSDDefinedColumn.Label"));
        props.setLook(wlXSDDefinedColumn);
        fdlXSDDefinedColumn=new FormData();
        fdlXSDDefinedColumn.left = new FormAttachment(0, 0);
        fdlXSDDefinedColumn.top  = new FormAttachment(wFilename, 2*margin);
        fdlXSDDefinedColumn.right= new FormAttachment(middle, -margin);
        wlXSDDefinedColumn.setLayoutData(fdlXSDDefinedColumn);
        wXSDDefinedColumn=new CCombo(wXSD, SWT.BORDER | SWT.READ_ONLY);
        wXSDDefinedColumn.setEditable(true);
        props.setLook(wXSDDefinedColumn);
        wXSDDefinedColumn.addModifyListener(lsMod);
        fdXSDDefinedColumn=new FormData();
        fdXSDDefinedColumn.left = new FormAttachment(middle, margin);
        fdXSDDefinedColumn.top  = new FormAttachment(wFilename, 2*margin);
        fdXSDDefinedColumn.right= new FormAttachment(100, -margin);
        wXSDDefinedColumn.setLayoutData(fdXSDDefinedColumn);
        wXSDDefinedColumn.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    PopulateFields();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );
			

    	fdXSD = new FormData();
    	fdXSD.left = new FormAttachment(0, margin);
    	fdXSD.top = new FormAttachment(wOutputFields, margin);
    	fdXSD.right = new FormAttachment(100, -margin);
    	wXSD.setLayoutData(fdXSD);
		
		// ///////////////////////////////////////////////////////////
		// / END OF XSD GROUP
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
 				
 		
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////
	
 		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$

		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wTabFolder);


		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();          } };
		
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();              } };
		
		wCancel.addListener(SWT.Selection, lsCancel);


		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		
		
		
		
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
				
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] { "*xsd;*.XSD",
							"*" });
					if (wFilename.getText() != null) {
						String fname = transMeta.environmentSubstitute(wFilename.getText());
						dialog.setFileName(fname);
					}

					dialog.setFilterNames(new String[] {
							BaseMessages.getString(PKG, "XsdValidatorDialog.FileType"),
							BaseMessages.getString(PKG, "System.FileType.AllFiles") });

					if (dialog.open() != null) {
						String str = dialog.getFilterPath()
								+ System.getProperty("file.separator")
								+ dialog.getFileName();
						wFilename.setText(str);
					}
				}
			
		});
		
		
		
		
		
				
		wTabFolder.setSelection(0);
		
		// Set the shell size, based upon previous time...
		setSize();

		getData();
		activeValidationMsg();
		activeOutputStringField();
		setXSDSource();
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	private void setXSDSource()
	{
		if (wXSDSource.getSelectionIndex()==0)
		{
			// XSD source is a file, let user specify it
			wFilename.setEnabled(true);
			wlFilename.setEnabled(true);
			wbbFilename.setEnabled(true);
			
			wlXSDDefinedColumn.setEnabled(false);
			wXSDDefinedColumn.setEnabled(false);
		}
		else if (wXSDSource.getSelectionIndex()==1)
		{
			// XSD source is a file, let user specify field that contain it
			wFilename.setEnabled(false);
			wlFilename.setEnabled(false);
			wbbFilename.setEnabled(false);
			
			wlXSDDefinedColumn.setEnabled(true);
			wXSDDefinedColumn.setEnabled(true);
			
		}
		else
		{
			// XSD source is in the XML source
			wFilename.setEnabled(false);
			wlFilename.setEnabled(false);
			wbbFilename.setEnabled(false);
			
			wlXSDDefinedColumn.setEnabled(false);
			wXSDDefinedColumn.setEnabled(false);
		}
	}
	
	 private void PopulateFields()
	 {
		 if(!gotPrevious) {
			gotPrevious=true;
				
	         String fieldXML=wXMLStream.getText();
	         String fieldXSD=wXSDDefinedColumn.getText();
			 try{
		            wXMLStream.removeAll();
		            wXSDDefinedColumn.removeAll();
					
		            RowMetaInterface r = transMeta.getPrevStepFields(stepname);
					if(r!=null) {
						wXMLStream.setItems(r.getFieldNames());
						wXSDDefinedColumn.setItems(r.getFieldNames());
					}
			 }catch(KettleException ke){
				 new ErrorDialog(shell, BaseMessages.getString(PKG, "XsdValidatorDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "XsdValidatorDialogMod.FailedToGetFields.DialogMessage"), ke);
				}
			if(fieldXML!=null)  wXMLStream.setText(fieldXML);
			if(fieldXSD!=null)  wXSDDefinedColumn.setText(fieldXSD);
		 }
	 }
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
	
		if (input.getXSDFilename() != null) wFilename.setText( input.getXSDFilename());
		
		// XML source
		wXMLSourceFile.setSelection(input.getXMLSourceFile());
		if (input.getXMLStream() != null) wXMLStream.setText( input.getXMLStream());
		
		if (input.getXSDDefinedField() != null) wXSDDefinedColumn.setText( input.getXSDDefinedField());
		
		// Output Fields
		if (input.getResultfieldname() != null) wResultField.setText( input.getResultfieldname() );
		wAddValidationMsg.setSelection(input.useAddValidationMessage());
		if (input.getValidationMessageField() !=null) 
		{
			wValidationMsg.setText(input.getValidationMessageField());
		}
		else
		{
			wValidationMsg.setText("ValidationMsgField");
		}
		
		wOutputStringField.setSelection(input.getOutputStringField());
		
		if(input.getIfXmlValid()!=null) wIfXMLValid.setText(input.getIfXmlValid());
		if(input.getIfXmlInvalid()!=null) wIfXMLUnValid.setText(input.getIfXmlInvalid());
		
		
		if (input.getXSDSource()!=null)
		{
			if (input.getXSDSource().equals(input.SPECIFY_FILENAME))
			{
				wXSDSource.select(0);
			}
			else if  (input.getXSDSource().equals(input.SPECIFY_FIELDNAME))
			{
				wXSDSource.select(1);
			}
			else
			{
				wXSDSource.select(2);
			}
		}
		else
		{
			wXSDSource.select(0);
		}
		
		wStepname.selectAll();
	}

	private void activeValidationMsg()
	{
		wValidationMsg.setEnabled(wAddValidationMsg.getSelection());
	}
	
	private void activeOutputStringField()
	{
		wIfXMLValid.setEnabled(wOutputStringField.getSelection());	
		wIfXMLUnValid.setEnabled(wOutputStringField.getSelection());
		
	}
	

	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		stepname = wStepname.getText(); // return value

		input.setXSDfilename(wFilename.getText() );
		input.setResultfieldname(wResultField.getText() );
		input.setXMLStream(wXMLStream.getText() );
		input.setXSDDefinedField(wXSDDefinedColumn.getText() );
		
		
		input.setOutputStringField(wOutputStringField.getSelection());
		input.setAddValidationMessage(wAddValidationMsg.getSelection());
		input.setValidationMessageField(wValidationMsg.getText());
		input.setIfXMLValid(wIfXMLValid.getText());
		input.setIfXmlInvalid(wIfXMLUnValid.getText());
		
		input.setXMLSourceFile(wXMLSourceFile.getSelection());
		
		if (wXSDSource.getSelectionIndex()==0)
		{
			input.setXSDSource(input.SPECIFY_FILENAME);
		}
		else if (wXSDSource.getSelectionIndex()==1)
		{
			input.setXSDSource(input.SPECIFY_FIELDNAME);
		}
		else
		{
			input.setXSDSource(input.NO_NEED);
		}
				
		dispose();
	}
}