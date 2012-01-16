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

package org.pentaho.di.ui.trans.steps.datagrid;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.datagrid.DataGridMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class DataGridDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = DataGridMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CTabFolder wTabFolder;
	private CTabItem wMetaTab, wDataTab;
	private Composite wMetaComp, wDataComp;
	
	private TableView    wFields;
	private TableView    wData;

	private DataGridMeta input;
	private DataGridMeta dataGridMeta;
	private ModifyListener	lsMod;

	public DataGridDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(DataGridMeta)in;
		
		dataGridMeta = (DataGridMeta) input.clone();
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
		shell.setText(BaseMessages.getString(PKG, "DataGridDialog.DialogTitle"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
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
		
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

		////////////////////////
		// START OF META TAB ///
		////////////////////////
		
		wMetaTab = new CTabItem(wTabFolder, SWT.NONE);
		wMetaTab.setText(BaseMessages.getString(PKG, "DataGridDialog.Meta.Label"));

		wMetaComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wMetaComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth = 3;
		fileLayout.marginHeight = 3;
		wMetaComp.setLayout(fileLayout);
				
		final int FieldsRows=input.getFieldName().length;
		
		ColumnInfo[] colinf=new ColumnInfo[] {
			new ColumnInfo(BaseMessages.getString(PKG, "DataGridDialog.Name.Column"),       ColumnInfo.COLUMN_TYPE_TEXT,   false),
			new ColumnInfo(BaseMessages.getString(PKG, "DataGridDialog.Type.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes() ),
			new ColumnInfo(BaseMessages.getString(PKG, "DataGridDialog.Format.Column"),     ColumnInfo.COLUMN_TYPE_CCOMBO, Const.getDateFormats() ),
			new ColumnInfo(BaseMessages.getString(PKG, "DataGridDialog.Length.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,   false),
			new ColumnInfo(BaseMessages.getString(PKG, "DataGridDialog.Precision.Column"),  ColumnInfo.COLUMN_TYPE_TEXT,   false), 
			new ColumnInfo(BaseMessages.getString(PKG, "DataGridDialog.Currency.Column"),   ColumnInfo.COLUMN_TYPE_TEXT,   false),
			new ColumnInfo(BaseMessages.getString(PKG, "DataGridDialog.Decimal.Column"),    ColumnInfo.COLUMN_TYPE_TEXT,   false),
			new ColumnInfo(BaseMessages.getString(PKG, "DataGridDialog.Group.Column"),      ColumnInfo.COLUMN_TYPE_TEXT,   false),
			new ColumnInfo(BaseMessages.getString(PKG, "DataGridDialog.Value.SetEmptyString"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { BaseMessages.getString(PKG, "System.Combo.Yes"), BaseMessages.getString(PKG, "System.Combo.No") }),
			
		};
		
		wFields=new TableView(transMeta, wMetaComp, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
						      lsMod,
							  props
						      );

		FormData fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(0, 0);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(100, 0);
		wFields.setLayoutData(fdFields);
		
		wMetaComp.layout();
		wMetaTab.setControl(wMetaComp);

		////////////////////////
		// START OF DATA TAB ///
		////////////////////////
		

		wDataTab = new CTabItem(wTabFolder, SWT.NONE);
		wDataTab.setText(BaseMessages.getString(PKG, "DataGridDialog.Data.Label"));

		wDataComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wDataComp);

		FormLayout filesettingLayout = new FormLayout();
		filesettingLayout.marginWidth = 3;
		filesettingLayout.marginHeight = 3;
		wDataComp.setLayout(fileLayout);

		addDataGrid(false);
		
		FormData fdDataComp = new FormData();
		fdDataComp.left = new FormAttachment(0, 0);
		fdDataComp.top = new FormAttachment(0, 0);
		fdDataComp.right = new FormAttachment(100, 0);
		fdDataComp.bottom = new FormAttachment(100, 0);
		wDataComp.setLayoutData(fdDataComp);

		wDataComp.layout();
		wDataTab.setControl(wDataComp);


		FormData fdTabFolder = new FormData();
		fdTabFolder.left = new FormAttachment(0, 0);
		fdTabFolder.top = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom = new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wPreview=new Button(shell, SWT.PUSH);
        wPreview.setText(BaseMessages.getString(PKG, "System.Button.Preview")); //$NON-NLS-1$
        wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
		
		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, wTabFolder);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
        lsPreview  = new Listener() { public void handleEvent(Event e) { preview(); } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
        wPreview.addListener (SWT.Selection, lsPreview);
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

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

		getData();
		wTabFolder.setSelection(0);

		wTabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addDataGrid(true);
			}
		});

		// Set the shell size, based upon previous time...
		setSize();
		
		input.setChanged(changed);
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	private void addDataGrid(boolean refresh) {
		
		if (refresh) {
			// retain changes made in the dialog...
			//
			getMetaInfo(dataGridMeta);
		}
		getMetaData();
		
		if (refresh) {
			// Retain the data edited in the dialog...
			//
			getDataInfo(dataGridMeta);

			// Clear out the data composite and redraw it completely...
			//
			for (Control control : wDataComp.getChildren()) control.dispose();
		}
		
		ColumnInfo[] columns = new ColumnInfo[dataGridMeta.getFieldName().length];
		for (int i=0;i<columns.length;i++) {
			columns[i] = new ColumnInfo(dataGridMeta.getFieldName()[i], ColumnInfo.COLUMN_TYPE_TEXT, false, false);
		}
		List<List<String>> lines = dataGridMeta.getDataLines();
		wData = new TableView(transMeta, wDataComp, SWT.NONE, columns, lines.size(), lsMod, props);
		wData.setSortable(false);
		
		for (int i=0;i<lines.size();i++) {
			List<String> line = lines.get(i);
			TableItem item = wData.table.getItem(i);
			
			for (int f=0;f<line.size();f++) {
				item.setText(f+1, Const.NVL(line.get(f), ""));
			}
		}
		
		wData.setRowNums();
		wData.optWidth(true);
		
		FormData fdData=new FormData();
		fdData.left  = new FormAttachment(0, 0);
		fdData.top   = new FormAttachment(0, 0);
		fdData.right = new FormAttachment(100, 0);
		fdData.bottom= new FormAttachment(100, 0);
		wData.setLayoutData(fdData);
			
		wTabFolder.layout(true, true);
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		getMetaData();
		addDataGrid(false);
	}
		
	private void getMetaData() {
		int nrfields=input.getFieldName().length;
		if(nrfields>wFields.table.getItemCount()) 
		{
			nrfields=wFields.table.getItemCount();
		}
		for (int i=0;i<nrfields;i++)
		{
			if (input.getFieldName()[i]!=null)
			{
				TableItem item = wFields.table.getItem(i);
				int col=1;
				
				item.setText(col++, input.getFieldName()[i]);
				String type   = input.getFieldType()[i];
				String format = input.getFieldFormat()[i];
                String length = input.getFieldLength()[i]<0?"":(""+input.getFieldLength()[i]);
                String prec   = input.getFieldPrecision()[i]<0?"":(""+input.getFieldPrecision()[i]);;
				String curr   = input.getCurrency()[i];
				String group  = input.getGroup()[i];
				String decim  = input.getDecimal()[i];

				item.setText(col++, Const.NVL(type  , ""));
				item.setText(col++, Const.NVL(format, ""));
				item.setText(col++, Const.NVL(length, ""));
				item.setText(col++, Const.NVL(prec  , ""));
				item.setText(col++, Const.NVL(curr  , ""));
				item.setText(col++, Const.NVL(decim , ""));
				item.setText(col++, Const.NVL(group , ""));
				item.setText(col++, input.isSetEmptyString()[i]?BaseMessages.getString(PKG, "System.Combo.Yes"):BaseMessages.getString(PKG, "System.Combo.No"));
				
			}
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
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText(); // return value
        
		getInfo(input);
		
		dispose();
	}
	
	private void getInfo(DataGridMeta meta) { 
		getMetaInfo(meta);
		getDataInfo(meta);
	}
	
	private void getMetaInfo(DataGridMeta meta) {
		int nrfields = wFields.nrNonEmpty();

		meta.allocate(nrfields);

		for (int i=0;i<nrfields;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			int col=1;
			meta.getFieldName()[i] = item.getText(col++);
			meta.getFieldType()[i] = item.getText(col++);
			meta.getFieldFormat()[i] = item.getText(col++);
			String slength = item.getText(col++);
			String sprec = item.getText(col++);
			meta.getCurrency()[i] = item.getText(col++);
			meta.getDecimal()[i] = item.getText(col++);
			meta.getGroup()[i] = item.getText(col++);
			
			try {
				meta.getFieldLength()[i] = Integer.parseInt(slength);
			} catch (Exception e) {
				meta.getFieldLength()[i] = -1;
			}
			try {
				meta.getFieldPrecision()[i] = Integer.parseInt(sprec);
			} catch (Exception e) {
				meta.getFieldPrecision()[i] = -1;
			}
			meta.isSetEmptyString()[i] = BaseMessages.getString(PKG, "System.Combo.Yes").equalsIgnoreCase(item.getText(col++));
		
			if(meta.isSetEmptyString()[i]) meta.getFieldType()[i]="String";
		}
	}
	
	private void getDataInfo(DataGridMeta meta) {
		List<List<String>> data = new ArrayList<List<String>>();
		
		int nrLines = wData.table.getItemCount();
		int nrFields = meta.getFieldName().length;

		for (int i=0;i<nrLines;i++)
		{
			List<String> line = new ArrayList<String>();
			TableItem item = wData.table.getItem(i);
			for (int f=0;f<nrFields;f++) {
				line.add(item.getText(f+1));
			}
			data.add(line);
		}
		
		meta.setDataLines(data);
	}

    /**
     * Preview the data generated by this step.
     * This generates a transformation using this step & a dummy and previews it.
     *
     */
    private void preview()
    {
        // Create the table input reader step...
        DataGridMeta oneMeta = new DataGridMeta();
        getInfo(oneMeta);
        
        TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
        
        EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), BaseMessages.getString(PKG, "DataGridDialog.EnterPreviewSize.Title"), BaseMessages.getString(PKG, "DataGridDialog.EnterPreviewSize.Message")); //$NON-NLS-1$ //$NON-NLS-2$
        int previewSize = numberDialog.open();
        if (previewSize>0)
        {
            TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
            progressDialog.open();

            Trans trans = progressDialog.getTrans();
            String loggingText = progressDialog.getLoggingText();

            if (!progressDialog.isCancelled())
            {
                if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
                {
                	EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"),  
                			BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message"), loggingText, true );
                	etd.setReadOnly();
                	etd.open();
                }
            }
            
            PreviewRowsDialog prd =new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog.getPreviewRows(wStepname.getText()), loggingText);
            prd.open();
        }
    }

}
