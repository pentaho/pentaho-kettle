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

package org.pentaho.di.ui.trans.steps.getvariable;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.getvariable.GetVariableMeta;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;



public class GetVariableDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = GetVariableMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlStepname;
	private Text         wStepname;
    private FormData     fdlStepname, fdStepname;
    
	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;
	
	private GetVariableMeta input;
	
	private boolean isReceivingInput=false;

	public GetVariableDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(GetVariableMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "GetVariableDialog.DialogTitle"));
		
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

		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "GetVariableDialog.Fields.Label"));
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wStepname, margin);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsRows=input.getFieldName().length;
		
		ColumnInfo[] colinf=new ColumnInfo[]
           {
    		 new ColumnInfo(BaseMessages.getString(PKG, "GetVariableDialog.NameColumn.Column"),     ColumnInfo.COLUMN_TYPE_TEXT, false),
    	   	 new ColumnInfo(BaseMessages.getString(PKG, "GetVariableDialog.VariableColumn.Column"), ColumnInfo.COLUMN_TYPE_TEXT, false),
    		 new ColumnInfo(BaseMessages.getString(PKG, "System.Column.Type"),                      ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes() ),
    		 new ColumnInfo(BaseMessages.getString(PKG, "System.Column.Format"),                    ColumnInfo.COLUMN_TYPE_FORMAT, 3),
    		 new ColumnInfo(BaseMessages.getString(PKG, "System.Column.Length"),                    ColumnInfo.COLUMN_TYPE_TEXT,   false),
    		 new ColumnInfo(BaseMessages.getString(PKG, "System.Column.Precision"),                 ColumnInfo.COLUMN_TYPE_TEXT,   false),
    		 new ColumnInfo(BaseMessages.getString(PKG, "System.Column.Currency"),                  ColumnInfo.COLUMN_TYPE_TEXT,   false),
    	     new ColumnInfo(BaseMessages.getString(PKG, "System.Column.Decimal"),                   ColumnInfo.COLUMN_TYPE_TEXT,   false),
    		 new ColumnInfo(BaseMessages.getString(PKG, "System.Column.Group"),                     ColumnInfo.COLUMN_TYPE_TEXT,   false),
    		 new ColumnInfo(BaseMessages.getString(PKG, "GetVariableDialog.TrimType.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTrimTypeDescriptions()),
           };
        
        colinf[1].setToolTip(BaseMessages.getString(PKG, "GetVariableDialog.VariableColumn.Tooltip"));
        colinf[1].setUsingVariables(true);
        
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
	    wGet = new Button(this.shell, 8);
	    wGet.setText(BaseMessages.getString(PKG, "System.Button.GetVariables"));
	    wPreview = new Button(this.shell, 8);
	    wPreview.setText(BaseMessages.getString(PKG, "System.Button.Preview"));
		wPreview.setEnabled(!isReceivingInput);
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		setButtonPositions(new Button[] { wOK, wPreview, wGet, wCancel }, margin, wFields);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsGet = new Listener() { public void handleEvent(Event e) { getVariables(); } };
		lsPreview = new Listener() { public void handleEvent(Event e) { preview(); }  };
	    wGet.addListener(13, this.lsGet);
	    wPreview.addListener(13, this.lsPreview);
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
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
		
		for (int i=0;i<input.getFieldName().length;i++)
		{
			TableItem item = wFields.table.getItem(i);

			int index=1;
			item.setText(index++, Const.NVL(input.getFieldName()[i], ""));
			item.setText(index++, Const.NVL(input.getVariableString()[i], ""));
			item.setText(index++, ValueMeta.getTypeDesc(input.getFieldType()[i]));
			item.setText(index++, Const.NVL(input.getFieldFormat()[i], ""));
			item.setText(index++, input.getFieldLength()[i]<0?"":(""+input.getFieldLength()[i]));
			item.setText(index++, input.getFieldPrecision()[i]<0?"":(""+input.getFieldPrecision()[i]));
			item.setText(index++, Const.NVL(input.getCurrency()[i], ""));
			item.setText(index++, Const.NVL(input.getDecimal()[i], ""));
			item.setText(index++, Const.NVL(input.getGroup()[i], ""));
			item.setText(index++, ValueMeta.getTrimTypeDesc(input.getTrimType()[i]));
		}

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
	 private void getInfo(GetVariableMeta input)
	    throws KettleException
	  {

		stepname = wStepname.getText(); // return value
		//Table table = wFields.table;

		int count = wFields.nrNonEmpty();
		input.allocate(count);
		
		for (int i=0;i<count;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			
			int index=1;
			input.getFieldName()[i]   = item.getText(index++);
			input.getVariableString()[i]   = item.getText(index++);
			input.getFieldType()[i]   = ValueMeta.getType(item.getText(index++));
            input.getFieldFormat()[i] = item.getText(index++);
            input.getFieldLength()[i]    = Const.toInt( item.getText(index++), -1);
            input.getFieldPrecision()[i] = Const.toInt( item.getText(index++), -1);
            input.getCurrency()[i]    = item.getText(index++);
            input.getDecimal()[i]     = item.getText(index++);
            input.getGroup()[i]       = item.getText(index++);
            input.getTrimType()[i]    = ValueMeta.getTrimTypeByDesc(item.getText(index++));
		}
	  }
	

	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		try {
			 getInfo(input);
		} catch(KettleException e) {
			 new ErrorDialog(shell, "Error", "Error saving step information", e);
		}
		dispose();
	}
	// Preview the data
	private void preview()
	{
        try
        {
            // Create the Access input step
            GetVariableMeta oneMeta = new GetVariableMeta();
            getInfo(oneMeta);
        
            TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
            
            //EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), BaseMessages.getString(PKG, "GetVariableDialog.NumberRows.DialogTitle"), BaseMessages.getString(PKG, "GetVariableDialog.NumberRows.DialogMessage"));
            
            int previewSize = 1;//numberDialog.open();
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
            new ErrorDialog(shell, BaseMessages.getString(PKG, "GetVariableDialog.ErrorPreviewingData.DialogTitle"), 
            		BaseMessages.getString(PKG, "GetVariableDialog.ErrorPreviewingData.DialogMessage"), e);
       }
	}
	  private void getVariables() {
		  	
		   if(transMeta==null) return;
		    String[] key = transMeta.listVariables();
		    int size = key.length;
		    String[] val = new String[size];
		    wFields.removeAll();

		    for (int i = 0; i < size; i++)
		    {
		      val[i] = transMeta.environmentSubstitute(key[i]);
		      TableItem tableItem = new TableItem(wFields.table, 0);
		      tableItem.setText(1, key[i]);
		      tableItem.setText(2, "${" + key[i] + "}");
		      tableItem.setText(3, "String");
		    }
		    wFields.removeEmptyRows();
		    wFields.setRowNums();
		    wFields.optWidth(true);
		  }
}