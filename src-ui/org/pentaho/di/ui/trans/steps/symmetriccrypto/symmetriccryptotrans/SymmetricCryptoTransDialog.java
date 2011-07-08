 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
 /**
 * Symmetric algorithm 
 * Executes a SymmetricCryptoTrans on the values in the input stream. 
 * Selected calculated values can then be put on the output stream.
 * 
 * @author Samatar
 * @since 5-apr-2003
 *
 */

package org.pentaho.di.ui.trans.steps.symmetriccrypto.symmetriccryptotrans;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm.SymmetricCryptoMeta;
import org.pentaho.di.trans.steps.symmetriccrypto.symmetriccryptotrans.SymmetricCryptoTransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;




public class SymmetricCryptoTransDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = SymmetricCryptoTransMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$


	private Group wCryptoSettings;
	private FormData fdCryptoSettings;

	
	private Group wMessageGroup;
	private FormData fdMessageGroup;

    private Label        wlReadKeyAsBinary;
    private Button       wReadKeyAsBinary;
    private FormData     fdlReadKeyAsBinary, fdReadKeyAsBinary;


	private FormData    fdResultField,fdlMessage, fdField,fdTabFolder
						,fdlSecretKey,fdSecretKey,fdSecretKeyField,fdlSecretKeyField;	
	private LabelTextVar wResultField;
	private CCombo       wMessage,wSecretKeyField;
    private FormData fdlSecretKeyInField,fdSecretKeyInField;  
 
	private Label wlMessage,wlSecretKey,wlSecretKeyField,wlSecretKeyInField;
    
	private Button wSecretKeyInField; 

	private SymmetricCryptoTransMeta input;
	
	private Group wOutputField;
	private FormData fdOutputField;
	
	private TextVar wSecretKey;

	
	private CTabFolder   wTabFolder;
	
	private CTabItem     wGeneralTab;
	private Composite    wGeneralComp;
	private FormData     fdGeneralComp;
	
	private Label 		wlOperation;
	private CCombo 		wOperation;
	private FormData    fdlOperation;
	private FormData    fdOperation;
	
	private Label 		wlAlgorithm;
	private CCombo 		wAlgorithm;
	private FormData    fdlAlgorithm;
	private FormData    fdAlgorithm;
	
	private Label wlScheme;
	private FormData fdlScheme;
	private TextVar wScheme;
	private FormData fdScheme;
	
	
    private Label        wlOutputAsBinary;
    private Button       wOutputAsBinary;
    private FormData     fdlOutputAsBinary, fdOutputAsBinary;

	
	public SymmetricCryptoTransDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(SymmetricCryptoTransMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// SecretKey line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.Stepname.Label")); //$NON-NLS-1$
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
 		props.setLook(wTabFolder, PropsUI.WIDGET_STYLE_TAB);
 		
 		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		
		
		
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.GeneralTab.TabTitle"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
		

    	
		// ////////////////////////
		// START OF Crypto settings GROUP
		// 

		wCryptoSettings = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wCryptoSettings);
		wCryptoSettings.setText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.CryptoSettings.Group.Label"));
		
		FormLayout CryptoSettingsgroupLayout = new FormLayout();
		CryptoSettingsgroupLayout.marginWidth = 10;
		CryptoSettingsgroupLayout.marginHeight = 10;
		wCryptoSettings.setLayout(CryptoSettingsgroupLayout);



		// Operation
		wlOperation=new Label(wCryptoSettings, SWT.RIGHT);
		wlOperation.setText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.Operation.Label")); //$NON-NLS-1$
 		props.setLook(wlOperation);
		fdlOperation=new FormData();
		fdlOperation.left = new FormAttachment(0, 0);
		fdlOperation.right= new FormAttachment(middle, -margin);
		fdlOperation.top  = new FormAttachment(wStepname, margin);
		wlOperation.setLayoutData(fdlOperation);
		
		wOperation=new CCombo(wCryptoSettings, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wOperation);
 		wOperation.addModifyListener(lsMod);
		fdOperation=new FormData();
		fdOperation.left = new FormAttachment(middle, margin);
		fdOperation.top  = new FormAttachment(wStepname, margin);
		fdOperation.right= new FormAttachment(100, -margin);
		wOperation.setLayoutData(fdOperation);
		wOperation.setItems(SymmetricCryptoTransMeta.operationTypeDesc);
		wOperation.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				input.setChanged();
				
			}
		});
		
		// Algorithm
		wlAlgorithm=new Label(wCryptoSettings, SWT.RIGHT);
		wlAlgorithm.setText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.Algorithm.Label")); //$NON-NLS-1$
 		props.setLook(wlAlgorithm);
		fdlAlgorithm=new FormData();
		fdlAlgorithm.left = new FormAttachment(0, 0);
		fdlAlgorithm.right= new FormAttachment(middle, -margin);
		fdlAlgorithm.top  = new FormAttachment(wOperation, margin);
		wlAlgorithm.setLayoutData(fdlAlgorithm);
		
		wAlgorithm=new CCombo(wCryptoSettings, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wAlgorithm);
 		wAlgorithm.addModifyListener(lsMod);
		fdAlgorithm=new FormData();
		fdAlgorithm.left = new FormAttachment(middle, margin);
		fdAlgorithm.top  = new FormAttachment(wOperation, margin);
		fdAlgorithm.right= new FormAttachment(100, -margin);
		wAlgorithm.setLayoutData(fdAlgorithm);
		wAlgorithm.setItems(SymmetricCryptoMeta.TYPE_ALGORYTHM_CODE);
		wAlgorithm.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				input.setChanged();
				
			}
		});
		
	    
		// Scheme
		wlScheme = new Label(wCryptoSettings, SWT.RIGHT);
		wlScheme.setText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.Scheme.Label"));
		props.setLook(wlScheme);
		fdlScheme = new FormData();
		fdlScheme.left = new FormAttachment(0, 0);
		fdlScheme.top = new FormAttachment(wAlgorithm, margin);
		fdlScheme.right = new FormAttachment(middle, -margin);
		wlScheme.setLayoutData(fdlScheme);

		wScheme = new TextVar(transMeta, wCryptoSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wScheme);
		wScheme.addModifyListener(lsMod);
		fdScheme = new FormData();
		fdScheme.left = new FormAttachment(middle, margin);
		fdScheme.right = new FormAttachment(100, -margin);
		fdScheme.top = new FormAttachment(wAlgorithm, margin);
		wScheme.setLayoutData(fdScheme);
		
	
		// SecretKey
		wlSecretKey = new Label(wCryptoSettings, SWT.RIGHT);
		wlSecretKey.setText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.SecretKey.Label"));
		props.setLook(wlSecretKey);
		fdlSecretKey = new FormData();
		fdlSecretKey.left = new FormAttachment(0, 0);
		fdlSecretKey.top = new FormAttachment(wScheme, 2*margin);
		fdlSecretKey.right = new FormAttachment(middle, -margin);
		wlSecretKey.setLayoutData(fdlSecretKey);

		wSecretKey = new TextVar(transMeta, wCryptoSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSecretKey);
		wSecretKey.setEchoChar('*');
		wSecretKey.setToolTipText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.SecretKey.Tooltip"));
		wSecretKey.addModifyListener(lsMod);
		fdSecretKey = new FormData();
		fdSecretKey.left = new FormAttachment(middle, margin);
		fdSecretKey.right = new FormAttachment(100, -margin);
		fdSecretKey.top = new FormAttachment(wScheme, 2*margin);
		wSecretKey.setLayoutData(fdSecretKey);
				
		// Is secret key extracted from a field?
		wlSecretKeyInField = new Label(wCryptoSettings, SWT.RIGHT);
		wlSecretKeyInField.setText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.SecretKeyFileField.Label"));
		props.setLook(wlSecretKeyInField);
		fdlSecretKeyInField = new FormData();
		fdlSecretKeyInField.left = new FormAttachment(0, 0);
		fdlSecretKeyInField.top = new FormAttachment(wSecretKey, margin);
		fdlSecretKeyInField.right = new FormAttachment(middle, -margin);
		wlSecretKeyInField.setLayoutData(fdlSecretKeyInField);
		wSecretKeyInField = new Button(wCryptoSettings, SWT.CHECK);
		props.setLook(wSecretKeyInField);
		wSecretKeyInField.setToolTipText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.SecretKeyFileField.Tooltip"));
		fdSecretKeyInField = new FormData();
		fdSecretKeyInField.left = new FormAttachment(middle, margin);
		fdSecretKeyInField.top = new FormAttachment(wSecretKey, margin);
		wSecretKeyInField.setLayoutData(fdSecretKeyInField);
		
		SelectionAdapter lsXslFile = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	ActivewlSecretKeyField();
            	input.setChanged();
            }
        };
        wSecretKeyInField.addSelectionListener(lsXslFile);
		
		
		
		// If secret key defined in a Field
		wlSecretKeyField=new Label(wCryptoSettings, SWT.RIGHT);
        wlSecretKeyField.setText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.SecretKeyField.Label"));
        props.setLook(wlSecretKeyField);
        fdlSecretKeyField=new FormData();
        fdlSecretKeyField.left = new FormAttachment(0, 0);
        fdlSecretKeyField.top  = new FormAttachment(wSecretKeyInField, margin);
        fdlSecretKeyField.right= new FormAttachment(middle, -margin);
        wlSecretKeyField.setLayoutData(fdlSecretKeyField);
        wSecretKeyField=new CCombo(wCryptoSettings, SWT.BORDER | SWT.READ_ONLY);
        wSecretKeyField.setEditable(true);
        props.setLook(wSecretKeyField);
        wSecretKeyField.addModifyListener(lsMod);
        fdSecretKeyField=new FormData();
        fdSecretKeyField.left = new FormAttachment(middle, margin);
        fdSecretKeyField.top  = new FormAttachment(wSecretKeyInField, margin);
        fdSecretKeyField.right= new FormAttachment(100, -margin);
        wSecretKeyField.setLayoutData(fdSecretKeyField);
        wSecretKeyField.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    setSecretKeyFieldname();
                }
            }
        );
        

        wlReadKeyAsBinary=new Label(wCryptoSettings, SWT.RIGHT);
        wlReadKeyAsBinary.setText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.ReadKeyAsBinary.Label")); //$NON-NLS-1$
        props.setLook(wlReadKeyAsBinary);
        fdlReadKeyAsBinary=new FormData();
        fdlReadKeyAsBinary.left = new FormAttachment(0, 0);
        fdlReadKeyAsBinary.top  = new FormAttachment(wSecretKeyField, margin);
        fdlReadKeyAsBinary.right= new FormAttachment(middle, -margin);
        wlReadKeyAsBinary.setLayoutData(fdlReadKeyAsBinary);
        wReadKeyAsBinary=new Button(wCryptoSettings, SWT.CHECK );
        wReadKeyAsBinary.setToolTipText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.ReadKeyAsBinary.Tooltip"));
        props.setLook(wReadKeyAsBinary);
        fdReadKeyAsBinary=new FormData();
        fdReadKeyAsBinary.left = new FormAttachment(middle, margin);
        fdReadKeyAsBinary.top  = new FormAttachment(wSecretKeyField, margin);
        fdReadKeyAsBinary.right= new FormAttachment(100, -margin);
        wReadKeyAsBinary.setLayoutData(fdReadKeyAsBinary);
        wReadKeyAsBinary.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent e) 
                {
                    input.setChanged();
                }
            }
        );
        
		fdCryptoSettings = new FormData();
		fdCryptoSettings.left = new FormAttachment(0, margin);
		fdCryptoSettings.top = new FormAttachment(wStepname, margin);
		fdCryptoSettings.right = new FormAttachment(100, -margin);
		wCryptoSettings.setLayoutData(fdCryptoSettings);
		
		// ///////////////////////////////////////////////////////////
		// / END OF  Crypto settings GROUP
		// ///////////////////////////////////////////////////////////	
		

		// ////////////////////////
		// START OF Crypto settings GROUP
		// 

		wMessageGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wMessageGroup);
		wMessageGroup.setText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.Message.Group.Label"));
		
		FormLayout MessageGroupgroupLayout = new FormLayout();
		MessageGroupgroupLayout.marginWidth = 10;
		MessageGroupgroupLayout.marginHeight = 10;
		wMessageGroup.setLayout(MessageGroupgroupLayout);



		
    	// FieldName to evaluate
		wlMessage=new Label(wMessageGroup, SWT.RIGHT);
        wlMessage.setText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.Field.Label"));
        props.setLook(wlMessage);
        fdlMessage=new FormData();
        fdlMessage.left = new FormAttachment(0, 0);
        fdlMessage.top  = new FormAttachment(wCryptoSettings, margin);
        fdlMessage.right= new FormAttachment(middle, -margin);
        wlMessage.setLayoutData(fdlMessage);
        wMessage=new CCombo(wMessageGroup, SWT.BORDER | SWT.READ_ONLY);
        wMessage.setEditable(true);
        props.setLook(wMessage);
        wMessage.addModifyListener(lsMod);
        fdField=new FormData();
        fdField.left = new FormAttachment(middle, margin);
        fdField.top  = new FormAttachment(wCryptoSettings, margin);
        fdField.right= new FormAttachment(100, -margin);
        wMessage.setLayoutData(fdField);
        wMessage.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    setFieldname();
                }
            }
        );

      
		fdMessageGroup = new FormData();
		fdMessageGroup.left = new FormAttachment(0, margin);
		fdMessageGroup.top = new FormAttachment(wCryptoSettings, margin);
		fdMessageGroup.right = new FormAttachment(100, -margin);
		wMessageGroup.setLayoutData(fdMessageGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF  Crypto settings GROUP
		// ///////////////////////////////////////////////////////////	
		

    	// Step Output field grouping?
		// ////////////////////////
		// START OF Output Field GROUP
		// 

		wOutputField = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wOutputField);
		wOutputField.setText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.ResultField.Group.Label"));
		
		FormLayout outputfieldgroupLayout = new FormLayout();
		outputfieldgroupLayout.marginWidth = 10;
		outputfieldgroupLayout.marginHeight = 10;
		wOutputField.setLayout(outputfieldgroupLayout);
        
	      // Output Fieldame
        wResultField = new LabelTextVar(transMeta, wOutputField, 
        		BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.ResultField.Label"), 
        		BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.ResultField.Tooltip"));
        props.setLook(wResultField);
        wResultField .addModifyListener(lsMod);
        fdResultField  = new FormData();
        fdResultField .left = new FormAttachment(0, 0);
        fdResultField .top = new FormAttachment(wMessageGroup, margin);
        fdResultField .right = new FormAttachment(100, 0);
        wResultField .setLayoutData(fdResultField );
        

        wlOutputAsBinary=new Label(wOutputField, SWT.RIGHT);
        wlOutputAsBinary.setText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.OutputAsBinary.Label")); //$NON-NLS-1$
        props.setLook(wlOutputAsBinary);
        fdlOutputAsBinary=new FormData();
        fdlOutputAsBinary.left = new FormAttachment(0, 0);
        fdlOutputAsBinary.top  = new FormAttachment(wResultField, margin);
        fdlOutputAsBinary.right= new FormAttachment(middle, -margin);
        wlOutputAsBinary.setLayoutData(fdlOutputAsBinary);
        wOutputAsBinary=new Button(wOutputField, SWT.CHECK );
        wOutputAsBinary.setToolTipText(BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.OutputAsBinary.Tooltip"));
        props.setLook(wOutputAsBinary);
        fdOutputAsBinary=new FormData();
        fdOutputAsBinary.left = new FormAttachment(middle, margin);
        fdOutputAsBinary.top  = new FormAttachment(wResultField, margin);
        fdOutputAsBinary.right= new FormAttachment(100, 0);
        wOutputAsBinary.setLayoutData(fdOutputAsBinary);
        wOutputAsBinary.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent e) 
                {
                    input.setChanged();
                }
            }
        ); 
        
        
		fdOutputField = new FormData();
		fdOutputField.left = new FormAttachment(0, margin);
		fdOutputField.top = new FormAttachment(wMessageGroup, margin);
		fdOutputField.right = new FormAttachment(100, -margin);
		wOutputField.setLayoutData(fdOutputField);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Output Field GROUP
		// ///////////////////////////////////////////////////////////	
		
	        
		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(wOutputField, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(100, 0);
		wGeneralComp.setLayoutData(fdGeneralComp);
		
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
 		props.setLook(wGeneralComp);
 		
 		
 		
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
		wSecretKey.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				wSecretKey.setToolTipText(transMeta.environmentSubstitute(wSecretKey.getText()));
			}
		});

		
		wTabFolder.setSelection(0);
		
		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		ActivewlSecretKeyField();

		
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
		
	
	private void ActivewlSecretKeyField()
	{
       
		wSecretKeyField.setEnabled(wSecretKeyInField.getSelection());
		wlSecretKeyField.setEnabled(wSecretKeyInField.getSelection());
		wReadKeyAsBinary.setEnabled(wSecretKeyInField.getSelection());
		wlReadKeyAsBinary.setEnabled(wSecretKeyInField.getSelection());
		wSecretKey.setEnabled(!wSecretKeyInField.getSelection());
		wlSecretKey.setEnabled(!wSecretKeyInField.getSelection());
		
		
	}
	
	
 	 private void setSecretKeyFieldname()
	 {
		 try{
	        String field=wSecretKeyField.getText();  
	        wSecretKeyField.removeAll();
				
	        RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if(r!=null) wSecretKeyField.setItems(r.getFieldNames());
			if(field!=null) wSecretKeyField.setText(field);

		 }catch(KettleException ke){
				new ErrorDialog(shell, BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "SymmetricCryptoTransDialogMod.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
	 }
	 private void setFieldname()
	 {
		 try{
	         String field=  wMessage.getText();
			 wMessage.removeAll();
				
			 RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			 wMessage.setItems(r.getFieldNames());
			 if(field!=null) wMessage.setText(field);

		 }catch(KettleException ke){
				new ErrorDialog(shell, BaseMessages.getString(PKG, "SymmetricCryptoTransDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "SymmetricCryptoTransDialogMod.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
	 }
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wOperation.setText(SymmetricCryptoTransMeta.getOperationTypeDesc(input.getOperationType()));
		wAlgorithm.setText(Const.NVL(input.getAlgorithm(), SymmetricCryptoMeta.TYPE_ALGORYTHM_CODE[0]) );
		if (input.getMessageFied() != null) wMessage.setText( input.getMessageFied() );
		if (input.getResultfieldname() != null) wResultField.setText( input.getResultfieldname() );
		if (input.getSecretKey() != null) wSecretKey.setText( input.getSecretKey() );
		if (input.getSchema() != null) wScheme.setText( input.getSchema() );

		wSecretKeyInField.setSelection(input.isSecretKeyInField());
		wReadKeyAsBinary.setSelection(input.isReadKeyAsBinary());
		if (input.getSecretKeyField() != null) wSecretKeyField.setText( input.getSecretKeyField() );
		wOutputAsBinary.setSelection(input.isOutputResultAsBinary());

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
		stepname = wStepname.getText(); // return value
		input.setOperationType(SymmetricCryptoTransMeta.getOperationTypeByDesc(wOperation.getText()));
		input.setAlgorithm(wAlgorithm.getText() );
		input.setMessageField(wMessage.getText() );
		input.setSchema(wScheme.getText() );
		input.setSecretKey(wSecretKey.getText() );
		input.setSecretKeyInField(wSecretKeyInField.getSelection());
		input.setReadKeyAsBinary(wReadKeyAsBinary.getSelection());
		input.setsecretKeyField(wSecretKeyField.getText() );
		input.setOutputResultAsBinary(wOutputAsBinary.getSelection());
		input.setResultfieldname(wResultField.getText() );
		
		dispose();
	}
	
	
	
		
	public String toString()
	{
		return this.getClass().getName();
	}
}