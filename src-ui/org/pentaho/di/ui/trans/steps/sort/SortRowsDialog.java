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

package org.pentaho.di.ui.trans.steps.sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.sort.SortRowsMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.CheckBoxVar;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;



public class SortRowsDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = SortRowsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlSortDir;
	private Button       wbSortDir;
	private TextVar      wSortDir;
	private FormData     fdlSortDir, fdbSortDir, fdSortDir;

	private Label        wlPrefix;
	private Text         wPrefix;
	private FormData     fdlPrefix, fdPrefix;

    private Label        wlSortSize;
    private TextVar      wSortSize;
    private FormData     fdlSortSize, fdSortSize;

    private Label        wlFreeMemory;
    private TextVar      wFreeMemory;
    private FormData     fdlFreeMemory, fdFreeMemory;

    private Label        wlCompress;
    private CheckBoxVar  wCompress;
    private FormData     fdlCompress, fdCompress;

    private Label        wlUniqueRows;
    private Button       wUniqueRows;
    private FormData     fdlUniqueRows, fdUniqueRows;

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private SortRowsMeta input;
    private Map<String, Integer> inputFields;
    private ColumnInfo[] colinf;
	
	public SortRowsDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(SortRowsMeta)in;
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
				input. setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "SortRowsDialog.DialogTitle"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

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

		// Temp directory for sorting
		wlSortDir=new Label(shell, SWT.RIGHT);
		wlSortDir.setText(BaseMessages.getString(PKG, "SortRowsDialog.SortDir.Label"));
 		props.setLook(wlSortDir);
		fdlSortDir=new FormData();
		fdlSortDir.left = new FormAttachment(0, 0);
		fdlSortDir.right= new FormAttachment(middle, -margin);
		fdlSortDir.top  = new FormAttachment(wStepname, margin);
		wlSortDir.setLayoutData(fdlSortDir);

		wbSortDir=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbSortDir);
		wbSortDir.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbSortDir=new FormData();
		fdbSortDir.right= new FormAttachment(100, 0);
		fdbSortDir.top  = new FormAttachment(wStepname, margin);
		wbSortDir.setLayoutData(fdbSortDir);

		wSortDir=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wSortDir.setText("temp");
 		props.setLook(wSortDir);
		wSortDir.addModifyListener(lsMod);
		fdSortDir=new FormData();
		fdSortDir.left = new FormAttachment(middle, 0);
		fdSortDir.top  = new FormAttachment(wStepname, margin);
		fdSortDir.right= new FormAttachment(wbSortDir, -margin);
		wSortDir.setLayoutData(fdSortDir);
		
		wbSortDir.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				DirectoryDialog dd = new DirectoryDialog(shell, SWT.NONE);
				dd.setFilterPath(wSortDir.getText());
				String dir = dd.open();
				if (dir!=null)
				{
					wSortDir.setText(dir);
				}
			}
		});

		// Whenever something changes, set the tooltip to the expanded version:
		wSortDir.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wSortDir.setToolTipText(transMeta.environmentSubstitute( wSortDir.getText() ) );
				}
			}
		);

        // Prefix of temporary file
		wlPrefix=new Label(shell, SWT.RIGHT);
		wlPrefix.setText(BaseMessages.getString(PKG, "SortRowsDialog.Prefix.Label"));
 		props.setLook(wlPrefix);
		fdlPrefix=new FormData();
		fdlPrefix.left = new FormAttachment(0, 0);
		fdlPrefix.right= new FormAttachment(middle, -margin);
		fdlPrefix.top  = new FormAttachment(wbSortDir, margin*2);
		wlPrefix.setLayoutData(fdlPrefix);
		wPrefix=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wPrefix);
		wPrefix.addModifyListener(lsMod);
		fdPrefix=new FormData();
		fdPrefix.left  = new FormAttachment(middle, 0);
		fdPrefix.top   = new FormAttachment(wbSortDir, margin*2);
		fdPrefix.right = new FormAttachment(100, 0);
		wPrefix.setLayoutData(fdPrefix);
		wPrefix.setText("srt");

        // Maximum number of lines to keep in memory before using temporary files
        wlSortSize=new Label(shell, SWT.RIGHT);
        wlSortSize.setText(BaseMessages.getString(PKG, "SortRowsDialog.SortSize.Label"));
        props.setLook(wlSortSize);
        fdlSortSize=new FormData();
        fdlSortSize.left = new FormAttachment(0, 0);
        fdlSortSize.right= new FormAttachment(middle, -margin);
        fdlSortSize.top  = new FormAttachment(wPrefix, margin*2);
        wlSortSize.setLayoutData(fdlSortSize);
        wSortSize=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(wSortSize);
        wSortSize.addModifyListener(lsMod);
        fdSortSize=new FormData();
        fdSortSize.left  = new FormAttachment(middle, 0);
        fdSortSize.top   = new FormAttachment(wPrefix, margin*2);
        fdSortSize.right = new FormAttachment(100, 0);
        wSortSize.setLayoutData(fdSortSize);

        // Free Memory to keep
        wlFreeMemory=new Label(shell, SWT.RIGHT);
        wlFreeMemory.setText(BaseMessages.getString(PKG, "SortRowsDialog.FreeMemory.Label"));
        wlFreeMemory.setToolTipText(BaseMessages.getString(PKG, "SortRowsDialog.FreeMemory.ToolTip"));
        props.setLook(wlFreeMemory);
        fdlFreeMemory=new FormData();
        fdlFreeMemory.left = new FormAttachment(0, 0);
        fdlFreeMemory.right= new FormAttachment(middle, -margin);
        fdlFreeMemory.top  = new FormAttachment(wSortSize, margin*2);
        wlFreeMemory.setLayoutData(fdlFreeMemory);
        wFreeMemory=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        wFreeMemory.setToolTipText(BaseMessages.getString(PKG, "SortRowsDialog.FreeMemory.ToolTip"));
        props.setLook(wFreeMemory);
        wFreeMemory.addModifyListener(lsMod);
        fdFreeMemory=new FormData();
        fdFreeMemory.left  = new FormAttachment(middle, 0);
        fdFreeMemory.top   = new FormAttachment(wSortSize, margin*2);
        fdFreeMemory.right = new FormAttachment(100, 0);
        wFreeMemory.setLayoutData(fdFreeMemory);

        // Using compression for temporary files?
        wlCompress=new Label(shell, SWT.RIGHT);
        wlCompress.setText(BaseMessages.getString(PKG, "SortRowsDialog.Compress.Label"));
        props.setLook(wlCompress);
        fdlCompress=new FormData();
        fdlCompress.left = new FormAttachment(0, 0);
        fdlCompress.right= new FormAttachment(middle, -margin);
        fdlCompress.top  = new FormAttachment(wFreeMemory, margin*2);
        wlCompress.setLayoutData(fdlCompress);
        wCompress=new CheckBoxVar(transMeta, shell, SWT.CHECK, "");
        props.setLook(wCompress);
        fdCompress=new FormData();
        fdCompress.left  = new FormAttachment(middle, 0);
        fdCompress.top   = new FormAttachment(wFreeMemory, margin*2);
        fdCompress.right = new FormAttachment(100, 0);
        wCompress.setLayoutData(fdCompress);
        wCompress.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					log.logDetailed("SortRowsDialog", "Selection Listener for compress: " + wCompress.getSelection());
					input.setChanged();
				}
			}
        );

        // Using compression for temporary files?
        wlUniqueRows=new Label(shell, SWT.RIGHT);
        wlUniqueRows.setText(BaseMessages.getString(PKG, "SortRowsDialog.UniqueRows.Label"));
        props.setLook(wlUniqueRows);
        fdlUniqueRows=new FormData();
        fdlUniqueRows.left = new FormAttachment(0, 0);
        fdlUniqueRows.right= new FormAttachment(middle, -margin);
        fdlUniqueRows.top  = new FormAttachment(wCompress, margin);
        wlUniqueRows.setLayoutData(fdlUniqueRows);
        wUniqueRows=new Button(shell, SWT.CHECK);
        wUniqueRows.setToolTipText(BaseMessages.getString(PKG, "SortRowsDialog.UniqueRows.Tooltip"));
        props.setLook(wUniqueRows);
        fdUniqueRows=new FormData();
        fdUniqueRows.left  = new FormAttachment(middle, 0);
        fdUniqueRows.top   = new FormAttachment(wCompress, margin);
        fdUniqueRows.right = new FormAttachment(100, 0);
        wUniqueRows.setLayoutData(fdUniqueRows);


		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "System.Button.GetFields"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		setButtonPositions(new Button[] { wOK, wCancel , wGet }, margin, null);

        // Table with fields to sort and sort direction
		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "SortRowsDialog.Fields.Label"));
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wUniqueRows, margin);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsRows=input.getFieldName().length;
		
		colinf=new ColumnInfo[] {
				new ColumnInfo(BaseMessages.getString(PKG, "SortRowsDialog.Fieldname.Column"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
				new ColumnInfo(BaseMessages.getString(PKG, "SortRowsDialog.Ascending.Column"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { BaseMessages.getString(PKG, "System.Combo.Yes"), BaseMessages.getString(PKG, "System.Combo.No") } ),
				new ColumnInfo(BaseMessages.getString(PKG, "SortRowsDialog.CaseInsensitive.Column"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { BaseMessages.getString(PKG, "System.Combo.Yes"), BaseMessages.getString(PKG, "System.Combo.No") } ),
			};
		
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
		fdFields.bottom= new FormAttachment(wOK, -2*margin);
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
                    	logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
                    }
                }
            }
        };
        new Thread(runnable).start();


		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();    } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wSortDir.addSelectionListener( lsDef );
		wPrefix.addSelectionListener( lsDef );
		wSortSize.addSelectionListener( lsDef );
		wFreeMemory.addSelectionListener( lsDef );
		
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
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (input.getPrefix() != null) wPrefix.setText(input.getPrefix());
		if (input.getDirectory() != null) wSortDir.setText(input.getDirectory());
		wSortSize.setText(Const.NVL(input.getSortSize(), ""));
		wFreeMemory.setText(Const.NVL(input.getFreeMemoryLimit(), ""));
		wCompress.setSelection(input.getCompressFiles());
		wCompress.setVariableName(input.getCompressFilesVariable());
		wUniqueRows.setSelection(input.isOnlyPassingUniqueRows());
        
		Table table = wFields.table;
		if (input.getFieldName().length>0) table.removeAll();
		for (int i=0;i<input.getFieldName().length;i++)
		{
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(0, ""+(i+1));
			ti.setText(1, input.getFieldName()[i]);
			ti.setText(2, input.getAscending()[i]?BaseMessages.getString(PKG, "System.Combo.Yes"):BaseMessages.getString(PKG, "System.Combo.No"));
			ti.setText(3, input.getCaseSensitive()[i]?BaseMessages.getString(PKG, "System.Combo.Yes"):BaseMessages.getString(PKG, "System.Combo.No"));
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

		// copy info to SortRowsMeta class (input)
		input.setPrefix( wPrefix.getText() );
		input.setDirectory( wSortDir.getText() );
        input.setSortSize( wSortSize.getText() );
        input.setFreeMemoryLimit( wFreeMemory.getText() );
        log.logDetailed("Sort rows", "Compression is set to " + wCompress.getSelection());
        input.setCompressFiles(wCompress.getSelection());
        input.setCompressFilesVariable(wCompress.getVariableName());
        input.setOnlyPassingUniqueRows(wUniqueRows.getSelection());

		//Table table = wFields.table;
		int nrfields = wFields.nrNonEmpty();

		input.allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			TableItem ti = wFields.getNonEmpty(i);
			input.getFieldName()[i] = ti.getText(1);
			input.getAscending()[i] = BaseMessages.getString(PKG, "System.Combo.Yes").equalsIgnoreCase(ti.getText(2));
			input.getCaseSensitive()[i] = BaseMessages.getString(PKG, "System.Combo.Yes").equalsIgnoreCase(ti.getText(3));
		}
		
		dispose();
	}
	
	private void get()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
			{
                TableItemInsertListener insertListener = new TableItemInsertListener() 
                    {
                        public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                        {
                            tableItem.setText(2, BaseMessages.getString(PKG, "System.Combo.Yes"));
                            return true;
                        } 
                    };
                BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1 }, new int[] {}, -1, -1, insertListener);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"), BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), ke);
		}

	}
}
