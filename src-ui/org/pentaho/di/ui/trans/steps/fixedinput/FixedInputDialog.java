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
 * Created on 18-mei-2003
 *
 */

package org.pentaho.di.ui.trans.steps.fixedinput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.fixedinput.FixedFileInputField;
import org.pentaho.di.trans.steps.fixedinput.FixedInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboValuesSelectionListener;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.trans.steps.fixedinput.Messages;


public class FixedInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private FixedInputMeta inputMeta;
	
	private TextVar      wFilename;
	private Button       wbbFilename; // Browse for a file
	private TextVar      wLineWidth;
	private Button       wLineFeedPresent;
	private TextVar      wBufferSize;
	private Button       wLazyConversion;
	private Button       wHeaderPresent;
	private TableView    wFields;

	private Button wRunningInParallel;
	
	private List<FixedFileInputField> fields;
    
	
	public FixedInputDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		inputMeta=(FixedInputMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
 		props.setLook(shell);
 		setShellImage(shell, inputMeta);
        
		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				inputMeta.setChanged();
			}
		};
		changed = inputMeta.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("FixedInputDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Step name line
		//
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("FixedInputDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		Control lastControl = wStepname;
		
		// Filename...
		//
		// The filename browse button
		//
        wbbFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
        props.setLook(wbbFilename);
        wbbFilename.setText(Messages.getString("System.Button.Browse"));
        wbbFilename.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
        FormData fdbFilename = new FormData();
        fdbFilename.top  = new FormAttachment(lastControl, margin);
        fdbFilename.right= new FormAttachment(100, 0);
        wbbFilename.setLayoutData(fdbFilename);

        // The field itself...
        //
		Label wlFilename = new Label(shell, SWT.RIGHT);
		wlFilename.setText(Messages.getString("FixedInputDialog.Filename.Label")); //$NON-NLS-1$
 		props.setLook(wlFilename);
		FormData fdlFilename = new FormData();
		fdlFilename.top  = new FormAttachment(lastControl, margin);
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);
		wFilename=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		FormData fdFilename = new FormData();
		fdFilename.top  = new FormAttachment(lastControl, margin);
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right= new FormAttachment(wbbFilename, -margin);
		wFilename.setLayoutData(fdFilename);
		lastControl = wFilename;
		
		// delimiter
		Label wlLineWidth = new Label(shell, SWT.RIGHT);
		wlLineWidth.setText(Messages.getString("FixedInputDialog.LineWidth.Label")); //$NON-NLS-1$
 		props.setLook(wlLineWidth);
		FormData fdlLineWidth = new FormData();
		fdlLineWidth.top  = new FormAttachment(lastControl, margin);
		fdlLineWidth.left = new FormAttachment(0, 0);
		fdlLineWidth.right= new FormAttachment(middle, -margin);
		wlLineWidth.setLayoutData(fdlLineWidth);
		wLineWidth=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLineWidth);
		wLineWidth.addModifyListener(lsMod);
		FormData fdLineWidth = new FormData();
		fdLineWidth.top  = new FormAttachment(lastControl, margin);
		fdLineWidth.left = new FormAttachment(middle, 0);
		fdLineWidth.right= new FormAttachment(100, 0);
		wLineWidth.setLayoutData(fdLineWidth);
		lastControl = wLineWidth;

		// delimiter
		Label wlLineFeedPresent = new Label(shell, SWT.RIGHT);
		wlLineFeedPresent.setText(Messages.getString("FixedInputDialog.LineFeedPresent.Label")); //$NON-NLS-1$
 		props.setLook(wlLineFeedPresent);
		FormData fdlLineFeedPresent = new FormData();
		fdlLineFeedPresent.top  = new FormAttachment(lastControl, margin);
		fdlLineFeedPresent.left = new FormAttachment(0, 0);
		fdlLineFeedPresent.right= new FormAttachment(middle, -margin);
		wlLineFeedPresent.setLayoutData(fdlLineFeedPresent);
		wLineFeedPresent=new Button(shell, SWT.CHECK);
 		props.setLook(wLineFeedPresent);
		FormData fdLineFeedPresent = new FormData();
		fdLineFeedPresent.top  = new FormAttachment(lastControl, margin);
		fdLineFeedPresent.left = new FormAttachment(middle, 0);
		fdLineFeedPresent.right= new FormAttachment(100, 0);
		wLineFeedPresent.setLayoutData(fdLineFeedPresent);
		lastControl = wLineFeedPresent;

		// bufferSize
		//
		Label wlBufferSize = new Label(shell, SWT.RIGHT);
		wlBufferSize.setText(Messages.getString("FixedInputDialog.BufferSize.Label")); //$NON-NLS-1$
 		props.setLook(wlBufferSize);
		FormData fdlBufferSize = new FormData();
		fdlBufferSize.top  = new FormAttachment(lastControl, margin);
		fdlBufferSize.left = new FormAttachment(0, 0);
		fdlBufferSize.right= new FormAttachment(middle, -margin);
		wlBufferSize.setLayoutData(fdlBufferSize);
		wBufferSize = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wBufferSize);
		wBufferSize.addModifyListener(lsMod);
		FormData fdBufferSize = new FormData();
		fdBufferSize.top  = new FormAttachment(lastControl, margin);
		fdBufferSize.left = new FormAttachment(middle, 0);
		fdBufferSize.right= new FormAttachment(100, 0);
		wBufferSize.setLayoutData(fdBufferSize);
		lastControl = wBufferSize;
		
		// performingLazyConversion?
		//
		Label wlLazyConversion = new Label(shell, SWT.RIGHT);
		wlLazyConversion.setText(Messages.getString("FixedInputDialog.LazyConversion.Label")); //$NON-NLS-1$
 		props.setLook(wlLazyConversion);
		FormData fdlLazyConversion = new FormData();
		fdlLazyConversion.top  = new FormAttachment(lastControl, margin);
		fdlLazyConversion.left = new FormAttachment(0, 0);
		fdlLazyConversion.right= new FormAttachment(middle, -margin);
		wlLazyConversion.setLayoutData(fdlLazyConversion);
		wLazyConversion = new Button(shell, SWT.CHECK);
 		props.setLook(wLazyConversion);
		FormData fdLazyConversion = new FormData();
		fdLazyConversion.top  = new FormAttachment(lastControl, margin);
		fdLazyConversion.left = new FormAttachment(middle, 0);
		fdLazyConversion.right= new FormAttachment(100, 0);
		wLazyConversion.setLayoutData(fdLazyConversion);
		lastControl = wLazyConversion;

		// header row?
		//
		Label wlHeaderPresent = new Label(shell, SWT.RIGHT);
		wlHeaderPresent.setText(Messages.getString("FixedInputDialog.HeaderPresent.Label")); //$NON-NLS-1$
 		props.setLook(wlHeaderPresent);
		FormData fdlHeaderPresent = new FormData();
		fdlHeaderPresent.top  = new FormAttachment(lastControl, margin);
		fdlHeaderPresent.left = new FormAttachment(0, 0);
		fdlHeaderPresent.right= new FormAttachment(middle, -margin);
		wlHeaderPresent.setLayoutData(fdlHeaderPresent);
		wHeaderPresent = new Button(shell, SWT.CHECK);
 		props.setLook(wHeaderPresent);
		FormData fdHeaderPresent = new FormData();
		fdHeaderPresent.top  = new FormAttachment(lastControl, margin);
		fdHeaderPresent.left = new FormAttachment(middle, 0);
		fdHeaderPresent.right= new FormAttachment(100, 0);
		wHeaderPresent.setLayoutData(fdHeaderPresent);
		lastControl = wHeaderPresent;

		// running in parallel?
		//
		Label wlRunningInParallel = new Label(shell, SWT.RIGHT);
		wlRunningInParallel.setText(Messages.getString("FixedInputDialog.RunningInParallel.Label")); //$NON-NLS-1$
 		props.setLook(wlRunningInParallel);
		FormData fdlRunningInParallel = new FormData();
		fdlRunningInParallel.top  = new FormAttachment(lastControl, margin);
		fdlRunningInParallel.left = new FormAttachment(0, 0);
		fdlRunningInParallel.right= new FormAttachment(middle, -margin);
		wlRunningInParallel.setLayoutData(fdlRunningInParallel);
		wRunningInParallel = new Button(shell, SWT.CHECK);
 		props.setLook(wRunningInParallel);
		FormData fdRunningInParallel = new FormData();
		fdRunningInParallel.top  = new FormAttachment(lastControl, margin);
		fdRunningInParallel.left = new FormAttachment(middle, 0);
		fdRunningInParallel.right= new FormAttachment(100, 0);
		wRunningInParallel.setLayoutData(fdRunningInParallel);
		lastControl = wRunningInParallel;
		
		// Some buttons first, so that the dialog scales nicely...
		//
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wPreview=new Button(shell, SWT.PUSH);
		wPreview.setText(Messages.getString("System.Button.Preview")); //$NON-NLS-1$
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(Messages.getString("System.Button.GetFields")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel , wPreview, wGet }, margin, null);


		// Fields
        ColumnInfo[] colinf=new ColumnInfo[]
            {
             new ColumnInfo(Messages.getString("FixedInputDialog.NameColumn.Column"),       ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("FixedInputDialog.TypeColumn.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO,  ValueMeta.getTypes(), true ),
             new ColumnInfo(Messages.getString("FixedInputDialog.FormatColumn.Column"),     ColumnInfo.COLUMN_TYPE_CCOMBO,  Const.getConversionFormats()),
             new ColumnInfo(Messages.getString("FixedInputDialog.WidthColumn.Column"),      ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("FixedInputDialog.LengthColumn.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("FixedInputDialog.PrecisionColumn.Column"),  ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("FixedInputDialog.CurrencyColumn.Column"),   ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("FixedInputDialog.DecimalColumn.Column"),    ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("FixedInputDialog.GroupColumn.Column"),      ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("FixedInputDialog.TrimColumn.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO,  ValueMeta.trimTypeDesc),
            };
        
        colinf[2].setComboValuesSelectionListener(new ComboValuesSelectionListener() {
    		
			public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
				String[] comboValues = new String[] { };
				int type = ValueMeta.getType( tableItem.getText(colNr-1) );
				switch(type) {
				case ValueMetaInterface.TYPE_DATE: comboValues = Const.getDateFormats(); break;
				case ValueMetaInterface.TYPE_INTEGER: 
				case ValueMetaInterface.TYPE_BIGNUMBER:
				case ValueMetaInterface.TYPE_NUMBER: comboValues = Const.getNumberFormats(); break;
				default: break;
				}
				return comboValues;
			}
		
		});

        
        wFields=new TableView(transMeta, shell, 
                              SWT.FULL_SELECTION | SWT.MULTI, 
                              colinf, 
                              1,  
                              lsMod,
                              props
                              );

        FormData fdFields = new FormData();
        fdFields.top   = new FormAttachment(lastControl, margin*2);
        fdFields.bottom= new FormAttachment(wOK, -margin*2);
        fdFields.left  = new FormAttachment(0, 0);
        fdFields.right = new FormAttachment(100, 0);
        wFields.setLayoutData(fdFields);
        
		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();   } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();       } };
		lsGet      = new Listener() { public void handleEvent(Event e) { getFixed(); } };
		lsPreview  = new Listener() { public void handleEvent(Event e) { preview();  } };
		
		wCancel.addListener (SWT.Selection, lsCancel );
		wOK.addListener     (SWT.Selection, lsOK     );
		wPreview.addListener(SWT.Selection, lsPreview);
		wGet.addListener    (SWT.Selection, lsGet    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wFilename.addSelectionListener( lsDef );
		wLineWidth.addSelectionListener( lsDef );
		wBufferSize.addSelectionListener( lsDef );
		
		// Listen to the browse button next to the file name
		wbbFilename.addSelectionListener(
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e) 
					{
						FileDialog dialog = new FileDialog(shell, SWT.OPEN);
						dialog.setFilterExtensions(new String[] { "*.txt", "*" });
						if (wFilename.getText()!=null)
						{
							String fname = transMeta.environmentSubstitute(wFilename.getText());
							dialog.setFileName( fname );
						}
						
						dialog.setFilterNames(new String[] {Messages.getString("System.FileType.TextFiles"), Messages.getString("System.FileType.AllFiles")});
						
						if (dialog.open()!=null)
						{
							String str = dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName();
							wFilename.setText(str);
						}
					}
				}
			);

		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		inputMeta.setChanged(changed);
	
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
		wFilename.setText(Const.NVL(inputMeta.getFilename(), ""));
		wLineWidth.setText(Const.NVL(inputMeta.getLineWidth(), ""));
		wLineFeedPresent.setSelection(inputMeta.isLineFeedPresent());
		wBufferSize.setText(Const.NVL(inputMeta.getBufferSize(), ""));
		wLazyConversion.setSelection(inputMeta.isLazyConversionActive());
		wHeaderPresent.setSelection(inputMeta.isHeaderPresent());
		wRunningInParallel.setSelection(inputMeta.isRunningInParallel());

		for (int i=0;i<inputMeta.getFieldDefinition().length;i++) {
			TableItem item = new TableItem(wFields.table, SWT.NONE);
			int colnr=1;
			FixedFileInputField field = inputMeta.getFieldDefinition()[i];
			
			item.setText(colnr++, Const.NVL(field.getName(), ""));
			item.setText(colnr++, ValueMeta.getTypeDesc(field.getType()));
			item.setText(colnr++, Const.NVL(field.getFormat(), ""));
			item.setText(colnr++, field.getWidth()>=0?Integer.toString(field.getWidth()):"") ;
			item.setText(colnr++, field.getLength()>=0?Integer.toString(field.getLength()):"") ;
			item.setText(colnr++, field.getPrecision()>=0?Integer.toString(field.getPrecision()):"") ;
			item.setText(colnr++, Const.NVL(field.getCurrency(), ""));
			item.setText(colnr++, Const.NVL(field.getDecimal(), ""));
			item.setText(colnr++, Const.NVL(field.getGrouping(), ""));
			item.setText(colnr++, ValueMeta.getTrimTypeCode(field.getTrimType()));
		}
		wFields.removeEmptyRows();
		wFields.setRowNums();
		wFields.optWidth(true);
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		inputMeta.setChanged(backupChanged);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText(); // return value

		getInfo(inputMeta);
		
		dispose();
	}
	
    private void getInfo(FixedInputMeta fixedInputMeta) {
    	
		fixedInputMeta.setFilename(wFilename.getText());
		fixedInputMeta.setLineWidth(wLineWidth.getText());
		fixedInputMeta.setBufferSize(wBufferSize.getText());
		fixedInputMeta.setLazyConversionActive(wLazyConversion.getSelection());
		fixedInputMeta.setHeaderPresent(wHeaderPresent.getSelection());
		fixedInputMeta.setLineFeedPresent(wLineFeedPresent.getSelection());
		fixedInputMeta.setRunningInParallel(wRunningInParallel.getSelection());

    	int nrNonEmptyFields = wFields.nrNonEmpty(); 
		fixedInputMeta.allocate(nrNonEmptyFields);

		for (int i=0;i<nrNonEmptyFields;i++) {
			TableItem item = wFields.getNonEmpty(i);
			int colnr=1;
			
			FixedFileInputField field = new FixedFileInputField();
			
			field.setName( item.getText(colnr++) );
			field.setType( ValueMeta.getType( item.getText(colnr++) ) );
			field.setFormat( item.getText(colnr++) );
			field.setWidth( Const.toInt(item.getText(colnr++), -1) );
			field.setLength( Const.toInt(item.getText(colnr++), -1) );
			field.setPrecision( Const.toInt(item.getText(colnr++), -1) );
			field.setCurrency( item.getText(colnr++) );
			field.setDecimal( item.getText(colnr++) );
			field.setGrouping( item.getText(colnr++) );
			field.setTrimType( ValueMeta.getTrimTypeByDesc(item.getText(colnr++)) ); 

			fixedInputMeta.getFieldDefinition()[i] = field;
		}
		wFields.removeEmptyRows();
		wFields.setRowNums();
		wFields.optWidth(true);
		
		fixedInputMeta.setChanged();
	}

	// Preview the data
    private void preview()
    {
    	// execute a complete preview transformation in the background.
    	// This is how we do it...
    	//
        FixedInputMeta oneMeta = new FixedInputMeta();
        getInfo(oneMeta);
        
        TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
        
        EnterNumberDialog numberDialog = new EnterNumberDialog(shell, 500, Messages.getString("FixedInputDialog.PreviewSize.DialogTitle"), Messages.getString("FixedInputDialog.PreviewSize.DialogMessage"));
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
                	EnterTextDialog etd = new EnterTextDialog(shell, Messages.getString("System.Dialog.PreviewError.Title"),  
                			Messages.getString("System.Dialog.PreviewError.Message"), loggingText, true );
                	etd.setReadOnly();
                	etd.open();
                }
            }
            
            PreviewRowsDialog prd =new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog.getPreviewRows(wStepname.getText()), loggingText);
            prd.open();
        }
    }

	private void getFixed()
	{
		final FixedInputMeta info = new FixedInputMeta();
		getInfo(info);
		
		Shell sh = new Shell(shell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);

        try
        {
    		List<String> rows = getFirst(info, 50);
    		fields = new ArrayList<FixedFileInputField>();
    		fields.addAll( Arrays.asList(info.getFieldDefinition()) );
    		
    		final FixedFileImportWizardPage1 page1 = new FixedFileImportWizardPage1("1", props, rows, fields);
    		page1.createControl(sh);
    		final FixedFileImportWizardPage2 page2 = new FixedFileImportWizardPage2("2", props, rows, fields);
    		page2.createControl(sh);
    
    		Wizard wizard = new Wizard() 
    		{
    			public boolean performFinish() 
    			{
    				wFields.clearAll(false);
    				
    				for (int i=0;i<fields.size();i++)
    				{
    					FixedFileInputField field = fields.get(i);
    					if (field.getWidth()>0)
    					{
    						TableItem item = new TableItem(wFields.table, SWT.NONE);
    						item.setText( 1,   field.getName());
    						item.setText( 2,""+ValueMeta.getTypeDesc(field.getType()) );
    						item.setText( 3,""+field.getFormat());
    						item.setText( 4,""+field.getWidth());
    						item.setText( 5,""+field.getLength());
    						item.setText( 6,""+field.getPrecision());
    						item.setText( 7,""+field.getCurrency());
    						item.setText( 8,""+field.getDecimal());
    						item.setText( 9,""+field.getGrouping());
    					}
    					
    				}
    				int size = wFields.table.getItemCount(); 
    				if (size==0)
    				{
    					new TableItem(wFields.table, SWT.NONE);
    				}
    
    				wFields.removeEmptyRows();				
    				wFields.setRowNums();
    				wFields.optWidth(true);
    				
    				inputMeta.setChanged();
    				
    				return true;
    			}
    		};
    				
    		wizard.addPage(page1);
    		wizard.addPage(page2);
    				
    		WizardDialog wd = new WizardDialog(shell, wizard);
    		wd.setMinimumPageSize(700,375);
        wd.updateSize();
    		wd.open();
        }
        catch(Exception e)
        {
            new ErrorDialog(shell, Messages.getString("FixedInputDialog.ErrorShowingFixedWizard.DialogTitle"), Messages.getString("FixedInputDialog.ErrorShowingFixedWizard.DialogMessage"), e);
        }
	}

	// Grab the first x lines from the given file...
	//
	private List<String> getFirst(FixedInputMeta meta, int limit) throws IOException, KettleValueException {

		List<String> lines = new ArrayList<String>();
		
        FixedInputMeta oneMeta = new FixedInputMeta();
        getInfo(oneMeta);
        
        // Add a single field with the width of the line...
        //
        int lineWidth = Integer.parseInt(oneMeta.getLineWidth());
        if (lineWidth<=0) {
        	throw new IOException("The width of a line can not be 0 or less.");
        }
        
        oneMeta.allocate(1);
        
        FixedFileInputField field = new FixedFileInputField();
        field.setName("Field1");
        field.setType(ValueMetaInterface.TYPE_STRING);
        field.setWidth(lineWidth);
        oneMeta.getFieldDefinition()[0] = field;
        
        TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
        
        TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { limit } );
        progressDialog.open();

        Trans trans = progressDialog.getTrans();
        String loggingText = progressDialog.getLoggingText();

        if (!progressDialog.isCancelled())
        {
            if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
            {
            	EnterTextDialog etd = new EnterTextDialog(shell, Messages.getString("System.Dialog.PreviewError.Title"),  
            			Messages.getString("System.Dialog.PreviewError.Message"), loggingText, true );
            	etd.setReadOnly();
            	etd.open();
            }
        }
        
        // The rows are in the transformation...
        //
        RowMetaInterface previewRowsMeta = progressDialog.getPreviewRowsMeta(wStepname.getText());
        List<Object[]> previewRowsData = progressDialog.getPreviewRows(wStepname.getText());
        for (int i=0;i<previewRowsData.size();i++) {
        	String line = previewRowsMeta.getString(previewRowsData.get(i), 0);
        	lines.add(line);
        }
        
        return lines;
	}
}
