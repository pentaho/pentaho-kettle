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
 
package org.pentaho.di.ui.trans.steps.symmetriccrypto.secretkeygenerator;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;


import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.symmetriccrypto.secretkeygenerator.SecretKeyGeneratorMeta;
import org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm.SymmetricCryptoMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;



/**
 * Generate secret key.
 * for symmetric algorithms
 * @author Samatar
 * @since 01-4-2011
 */

public class SecretKeyGeneratorDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = SecretKeyGeneratorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlStepname;
	private Text         wStepname;
    private FormData     fdlStepname, fdStepname;
    
	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;
	
	private Group wOutputFields;
	private Label wlSecretKeyField;
	private FormData fdlSecretKeyField;
	private Text wSecretKeyField;

	private FormData fdSecretKeyField;
	
	private Label wlSecretKeyLengthField;
	private FormData fdlSecretKeyLengthField;
	private Label wlAlgorithmField;
	private FormData fdlAlgorithmField;
	private Text wSecretKeyLengthField;
	private FormData fdSecretKeyLengthField;
	private Text wAlgorithmField;
	private FormData fdAlgorithmField;
	
    private Label        wlOutputKeyAsByinary;
    private Button       wOutputKeyAsByinary;
    private FormData     fdlOutputKeyAsByinary, fdOutputKeyAsByinary;

	
	private SecretKeyGeneratorMeta input;
	private boolean isReceivingInput=false;

	public SecretKeyGeneratorDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(SecretKeyGeneratorMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.DialogTitle"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;
		
		
		// See if the step receives input.
		//
		isReceivingInput = transMeta.findNrPrevSteps(stepMeta)>0;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
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
		
		// ///////////////////////////////
		// START OF OutputFields GROUP  //
		///////////////////////////////// 

		wOutputFields = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wOutputFields);
		wOutputFields.setText(BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.wOutputFields.Label"));
		
		FormLayout OutputFieldsgroupLayout = new FormLayout();
		OutputFieldsgroupLayout.marginWidth = 10;
		OutputFieldsgroupLayout.marginHeight = 10;
		wOutputFields.setLayout(OutputFieldsgroupLayout);
		
		// SecretKeyField fieldname ...
		wlSecretKeyField=new Label(wOutputFields, SWT.RIGHT);
		wlSecretKeyField.setText(BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.SecretKeyFieldName.Label")); //$NON-NLS-1$
 		props.setLook(wlSecretKeyField);
		fdlSecretKeyField=new FormData();
		fdlSecretKeyField.left = new FormAttachment(0, 0);
		fdlSecretKeyField.right= new FormAttachment(middle, -margin);
		fdlSecretKeyField.top  = new FormAttachment(wStepname, margin*2);
		wlSecretKeyField.setLayoutData(fdlSecretKeyField);
		wSecretKeyField=new Text(wOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wSecretKeyField.setToolTipText(BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.SecretKeyFieldName.Tooltip"));
 		props.setLook(wSecretKeyField);
		wSecretKeyField.addModifyListener(lsMod);
		fdSecretKeyField=new FormData();
		fdSecretKeyField.left = new FormAttachment(middle, 0);
		fdSecretKeyField.top  = new FormAttachment(wStepname, margin*2);
		fdSecretKeyField.right= new FormAttachment(100, 0);
		wSecretKeyField.setLayoutData(fdSecretKeyField);
		
		// AlgorithmField fieldname ...
		wlAlgorithmField=new Label(wOutputFields, SWT.RIGHT);
		wlAlgorithmField.setText(BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.Algorithm.Label")); //$NON-NLS-1$
 		props.setLook(wlAlgorithmField);
		fdlAlgorithmField=new FormData();
		fdlAlgorithmField.left = new FormAttachment(0, 0);
		fdlAlgorithmField.right= new FormAttachment(middle, -margin);
		fdlAlgorithmField.top  = new FormAttachment(wSecretKeyField, margin);
		wlAlgorithmField.setLayoutData(fdlAlgorithmField);
		wAlgorithmField=new Text(wOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wAlgorithmField.setToolTipText(BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.Algorithm.Tooltip"));
 		props.setLook(wAlgorithmField);
		wAlgorithmField.addModifyListener(lsMod);
		fdAlgorithmField=new FormData();
		fdAlgorithmField.left = new FormAttachment(middle, 0);
		fdAlgorithmField.top  = new FormAttachment(wSecretKeyField, margin);
		fdAlgorithmField.right= new FormAttachment(100, 0);
		wAlgorithmField.setLayoutData(fdAlgorithmField);
		
		// SecretKeyLengthField fieldname ...
		wlSecretKeyLengthField=new Label(wOutputFields, SWT.RIGHT);
		wlSecretKeyLengthField.setText(BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.SecretKeyLength.Label")); //$NON-NLS-1$
 		props.setLook(wlSecretKeyLengthField);
		fdlSecretKeyLengthField=new FormData();
		fdlSecretKeyLengthField.left = new FormAttachment(0, 0);
		fdlSecretKeyLengthField.right= new FormAttachment(middle, -margin);
		fdlSecretKeyLengthField.top  = new FormAttachment(wAlgorithmField, margin);
		wlSecretKeyLengthField.setLayoutData(fdlSecretKeyLengthField);
		wSecretKeyLengthField=new Text(wOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wSecretKeyLengthField.setToolTipText(BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.SecretKeyLength.Tooltip"));
 		props.setLook(wSecretKeyLengthField);
		wSecretKeyLengthField.addModifyListener(lsMod);
		fdSecretKeyLengthField=new FormData();
		fdSecretKeyLengthField.left = new FormAttachment(middle, 0);
		fdSecretKeyLengthField.top  = new FormAttachment(wAlgorithmField, margin);
		fdSecretKeyLengthField.right= new FormAttachment(100, 0);
		wSecretKeyLengthField.setLayoutData(fdSecretKeyLengthField);
		

        wlOutputKeyAsByinary=new Label(wOutputFields, SWT.RIGHT);
        wlOutputKeyAsByinary.setText(BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.OutputKeyAsByinary.Label")); //$NON-NLS-1$
        props.setLook(wlOutputKeyAsByinary);
        fdlOutputKeyAsByinary=new FormData();
        fdlOutputKeyAsByinary.left = new FormAttachment(0, 0);
        fdlOutputKeyAsByinary.top  = new FormAttachment(wSecretKeyLengthField, margin);
        fdlOutputKeyAsByinary.right= new FormAttachment(middle, -margin);
        wlOutputKeyAsByinary.setLayoutData(fdlOutputKeyAsByinary);
        wOutputKeyAsByinary=new Button(wOutputFields, SWT.CHECK );
        wOutputKeyAsByinary.setToolTipText(BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.OutputKeyAsByinary.Tooltip"));
        props.setLook(wOutputKeyAsByinary);
        fdOutputKeyAsByinary=new FormData();
        fdOutputKeyAsByinary.left = new FormAttachment(middle, 0);
        fdOutputKeyAsByinary.top  = new FormAttachment(wSecretKeyLengthField, margin);
        fdOutputKeyAsByinary.right= new FormAttachment(100, 0);
        wOutputKeyAsByinary.setLayoutData(fdOutputKeyAsByinary);
        wOutputKeyAsByinary.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent e) 
                {
                    input.setChanged();
                }
            }
        );
		FormData fdOutputFields = new FormData();
		fdOutputFields.left = new FormAttachment(0, margin);
		fdOutputFields.top = new FormAttachment(wStepname, 2*margin);
		fdOutputFields.right = new FormAttachment(100, -margin);
		wOutputFields.setLayoutData(fdOutputFields);
		
		// ///////////////////////////////////////////////////////////
		// / END OF OutputFields GROUP
		// ///////////////////////////////////////////////////////////		

		

		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.Fields.Label"));
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wOutputFields, margin);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsCols=4;
		final int FieldsRows=input.getAlgorithm().length;

		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo(BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.AlgorithmColumn.Column"), ColumnInfo.COLUMN_TYPE_CCOMBO, SymmetricCryptoMeta.TYPE_ALGORYTHM_CODE);
		colinf[0].setReadOnly(true);
		colinf[1]=new ColumnInfo(BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.SchemeColumn.Column"),       ColumnInfo.COLUMN_TYPE_TEXT, false);
		colinf[1].setUsingVariables(true);
		colinf[2]=new ColumnInfo(BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.SecretKeyLengthColumn.Column"),       ColumnInfo.COLUMN_TYPE_TEXT, false);
		colinf[2].setUsingVariables(true);
		colinf[3]=new ColumnInfo(BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.HowMany.Column"),       ColumnInfo.COLUMN_TYPE_TEXT, false);
		colinf[3].setUsingVariables(true);
		
		wFields=new TableView(transMeta, shell, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  FieldsRows,  
							  lsMod,
							  props
							  );

		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);          	
      
		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		wPreview=new Button(shell, SWT.PUSH);
		wPreview.setText(BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.Button.PreviewRows"));
		wPreview.setEnabled(!isReceivingInput);
		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, wFields);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		lsPreview  = new Listener() { public void handleEvent(Event e) { preview();   } };
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wPreview.addListener(SWT.Selection, lsPreview);
		wStepname.addSelectionListener( lsDef );

		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wStepname.setText(stepname);

		for (int i=0;i<input.getAlgorithm().length;i++)
		{
			TableItem item = wFields.table.getItem(i);
			String algorithm = input.getAlgorithm()[i];
			String scheme = input.getScheme()[i];
			String len = input.getSecretKeyLength()[i] ;
			String size = input.getSecretKeyCount()[i] ;
			
			if (algorithm!=null) item.setText(1, algorithm);
			if (scheme!=null) item.setText(2, scheme);
			else item.setText(2, algorithm);
			if (len!=null) item.setText(3, len);
			if (size!=null) item.setText(4, size);
		}

		wFields.setRowNums();
		wFields.optWidth(true);
		
		if(input.getSecretKeyFieldName()!=null) wSecretKeyField.setText(input.getSecretKeyFieldName());
		if(input.getAlgorithmFieldName()!=null) wAlgorithmField.setText(input.getAlgorithmFieldName());
		if(input.getSecretKeyLengthFieldName()!=null) wSecretKeyLengthField.setText(input.getSecretKeyLengthFieldName());
		wOutputKeyAsByinary.setSelection(input.isOutputKeyInBinary());
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
            new ErrorDialog(shell, "Error", "Error saving step informations", e);
        }
		dispose();
	}
	
	private void getInfo(SecretKeyGeneratorMeta in) throws KettleException
	{
 
		stepname = wStepname.getText(); // return value
		int count = wFields.nrNonEmpty();
		in.allocate(count);
		
		for (int i=0;i<count;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			in.getAlgorithm()[i]   = item.getText(1);
			in.getScheme()[i]   = item.getText(2);
			in.getSecretKeyLength()[i]   = item.getText(3);
			in.getSecretKeyCount()[i]   = item.getText(4);
		}
		in.setSecretKeyFieldName(wSecretKeyField.getText());
		in.setAlgorithmFieldName(wAlgorithmField.getText());
		in.setSecretKeyLengthFieldName(wSecretKeyLengthField.getText());
		in.setOutputKeyInBinary(wOutputKeyAsByinary.getSelection());
	}

	
	// Preview the data
	private void preview()
	{
        try
        {
            // Create the SecretKeyGeneratorMeta input step
            SecretKeyGeneratorMeta oneMeta = new SecretKeyGeneratorMeta();
            getInfo(oneMeta);
        
            TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
             EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.NumberRows.DialogTitle"), BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.NumberRows.DialogMessage"));
            
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
            new ErrorDialog(shell, BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.ErrorPreviewingData.DialogTitle"), BaseMessages.getString(PKG, "SecretKeyGeneratorDialog.ErrorPreviewingData.DialogMessage"), e);
       }
	}
}
