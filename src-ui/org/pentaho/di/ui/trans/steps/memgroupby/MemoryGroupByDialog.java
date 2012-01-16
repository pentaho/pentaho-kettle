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

package org.pentaho.di.ui.trans.steps.memgroupby;

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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.memgroupby.MemoryGroupByMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class MemoryGroupByDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = MemoryGroupByMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlGroup;
	private TableView    wGroup;
	private FormData     fdlGroup, fdGroup;

	private Label        wlAgg;
	private TableView    wAgg;
	private FormData     fdlAgg, fdAgg;

    private Label        wlAlwaysAddResult;
    private Button       wAlwaysAddResult;
    private FormData     fdlAlwaysAddResult, fdAlwaysAddResult;

	private Button wGet, wGetAgg;
	private FormData fdGet, fdGetAgg;
	private Listener lsGet, lsGetAgg;

	private MemoryGroupByMeta input;

	private ColumnInfo[] ciKey;
	private ColumnInfo[] ciReturn;
	
    private Map<String, Integer> inputFields;

	public MemoryGroupByDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(MemoryGroupByMeta)in;
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
		backupChanged = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "MemoryGroupByDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;
		
		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "MemoryGroupByDialog.Stepname.Label")); //$NON-NLS-1$
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

        // Always pass a result rows as output
        //
        wlAlwaysAddResult=new Label(shell, SWT.RIGHT);
        wlAlwaysAddResult.setText(BaseMessages.getString(PKG, "MemoryGroupByDialog.AlwaysAddResult.Label")); //$NON-NLS-1$
        wlAlwaysAddResult.setToolTipText(BaseMessages.getString(PKG, "MemoryGroupByDialog.AlwaysAddResult.ToolTip")); //$NON-NLS-1$
        props.setLook(wlAlwaysAddResult);
        fdlAlwaysAddResult=new FormData();
        fdlAlwaysAddResult.left = new FormAttachment(0, 0);
        fdlAlwaysAddResult.top  = new FormAttachment(wStepname, margin);
        fdlAlwaysAddResult.right= new FormAttachment(middle, -margin);
        wlAlwaysAddResult.setLayoutData(fdlAlwaysAddResult);
        wAlwaysAddResult=new Button(shell, SWT.CHECK );
        wAlwaysAddResult.setToolTipText(BaseMessages.getString(PKG, "MemoryGroupByDialog.AlwaysAddResult.ToolTip")); //$NON-NLS-1$
        props.setLook(wAlwaysAddResult);
        fdAlwaysAddResult=new FormData();
        fdAlwaysAddResult.left = new FormAttachment(middle, 0);
        fdAlwaysAddResult.top  = new FormAttachment(wStepname, margin);
        fdAlwaysAddResult.right= new FormAttachment(100, 0);
        wAlwaysAddResult.setLayoutData(fdAlwaysAddResult);

        
		wlGroup=new Label(shell, SWT.NONE);
		wlGroup.setText(BaseMessages.getString(PKG, "MemoryGroupByDialog.Group.Label")); //$NON-NLS-1$
 		props.setLook(wlGroup);
		fdlGroup=new FormData();
		fdlGroup.left  = new FormAttachment(0, 0);
		fdlGroup.top   = new FormAttachment(wAlwaysAddResult, margin);
		wlGroup.setLayoutData(fdlGroup);

		int nrKeyCols=1;
		int nrKeyRows=(input.getGroupField()!=null?input.getGroupField().length:1);
		
		ciKey=new ColumnInfo[nrKeyCols];
		ciKey[0]=new ColumnInfo(BaseMessages.getString(PKG, "MemoryGroupByDialog.ColumnInfo.GroupField"),  
		ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false);
		
		wGroup=new TableView(transMeta, shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
						      ciKey, 
						      nrKeyRows,  
						      lsMod,
							  props
						      );

		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "MemoryGroupByDialog.GetFields.Button")); //$NON-NLS-1$
		fdGet = new FormData();
		fdGet.top   = new FormAttachment(wlGroup, margin);
		fdGet.right = new FormAttachment(100, 0);
		wGet.setLayoutData(fdGet);
		
		fdGroup=new FormData();
		fdGroup.left  = new FormAttachment(0, 0);
		fdGroup.top   = new FormAttachment(wlGroup, margin);
		fdGroup.right = new FormAttachment(wGet, -margin);
		fdGroup.bottom= new FormAttachment(45, 0);
		wGroup.setLayoutData(fdGroup);

		// THE Aggregate fields
		wlAgg=new Label(shell, SWT.NONE);
		wlAgg.setText(BaseMessages.getString(PKG, "MemoryGroupByDialog.Aggregates.Label")); //$NON-NLS-1$
 		props.setLook(wlAgg);
		fdlAgg=new FormData();
		fdlAgg.left  = new FormAttachment(0, 0);
		fdlAgg.top   = new FormAttachment(wGroup, margin);
		wlAgg.setLayoutData(fdlAgg);
		
		int UpInsCols=4;
		int UpInsRows= (input.getAggregateField()!=null?input.getAggregateField().length:1);
		
		ciReturn=new ColumnInfo[UpInsCols];
		ciReturn[0]=new ColumnInfo(BaseMessages.getString(PKG, "MemoryGroupByDialog.ColumnInfo.Name"),     ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
		ciReturn[1]=new ColumnInfo(BaseMessages.getString(PKG, "MemoryGroupByDialog.ColumnInfo.Subject"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false);
		ciReturn[2]=new ColumnInfo(BaseMessages.getString(PKG, "MemoryGroupByDialog.ColumnInfo.Type"),     ColumnInfo.COLUMN_TYPE_CCOMBO, MemoryGroupByMeta.typeGroupLongDesc); //$NON-NLS-1$
		ciReturn[3]=new ColumnInfo(BaseMessages.getString(PKG, "MemoryGroupByDialog.ColumnInfo.Value"), ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
		ciReturn[3].setToolTip(BaseMessages.getString(PKG, "MemoryGroupByDialog.ColumnInfo.Value.Tooltip"));
		ciReturn[3].setUsingVariables(true);
		
		wAgg=new TableView(transMeta, shell, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
							  ciReturn, 
							  UpInsRows,  
							  lsMod,
							  props
							  );

		wGetAgg=new Button(shell, SWT.PUSH);
		wGetAgg.setText(BaseMessages.getString(PKG, "MemoryGroupByDialog.GetLookupFields.Button")); //$NON-NLS-1$
		fdGetAgg = new FormData();
		fdGetAgg.top   = new FormAttachment(wlAgg, margin);
		fdGetAgg.right = new FormAttachment(100, 0);
		wGetAgg.setLayoutData(fdGetAgg);
		
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

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		fdAgg=new FormData();
		fdAgg.left  = new FormAttachment(0, 0);
		fdAgg.top   = new FormAttachment(wlAgg, margin);
		fdAgg.right = new FormAttachment(wGetAgg, -margin);
		fdAgg.bottom= new FormAttachment(wOK, -margin);
		wAgg.setLayoutData(fdAgg);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();       } };
		lsGetAgg   = new Listener() { public void handleEvent(Event e) { getAgg(); } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wGetAgg.addListener (SWT.Selection, lsGetAgg );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );



		// Set the shell size, based upon previous time...
		setSize();
				
		getData();
		input.setChanged(backupChanged);

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
        ciKey[0].setComboValues(fieldNames);
        ciReturn[1].setComboValues(fieldNames);
    }

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		int i;
		logDebug(BaseMessages.getString(PKG, "MemoryGroupByDialog.Log.GettingKeyInfo")); //$NON-NLS-1$
		
        wAlwaysAddResult.setSelection(input.isAlwaysGivingBackOneRow());
        
		if (input.getGroupField()!=null)
		for (i=0;i<input.getGroupField().length;i++)
		{
			TableItem item = wGroup.table.getItem(i);
			if (input.getGroupField()[i]   !=null) item.setText(1, input.getGroupField()[i]);
		}
		
		if (input.getAggregateField()!=null)
		for (i=0;i<input.getAggregateField().length;i++)
		{
			TableItem item = wAgg.table.getItem(i);
			if (input.getAggregateField()[i]!=null     ) item.setText(1, input.getAggregateField()[i]);
			if (input.getSubjectField()[i]!=null       ) item.setText(2, input.getSubjectField()[i]);
			item.setText(3, Const.NVL(MemoryGroupByMeta.getTypeDescLong(input.getAggregateType()[i]), ""));
			if (input.getValueField()[i]!=null       ) item.setText(4, input.getValueField()[i]);
		}
        
		wStepname.selectAll();
		wGroup.setRowNums();
		wGroup.optWidth(true);
		wAgg.setRowNums();
		wAgg.optWidth(true);
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(backupChanged);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		int sizegroup = wGroup.nrNonEmpty();
		int nrfields = wAgg.nrNonEmpty();

        input.setAlwaysGivingBackOneRow( wAlwaysAddResult.getSelection() );
        
		input.allocate(sizegroup, nrfields);
				
		for (int i=0;i<sizegroup;i++)
		{
			TableItem item = wGroup.getNonEmpty(i);
			input.getGroupField()[i]    = item.getText(1);
		}
		
		for (int i=0;i<nrfields;i++)
		{
			TableItem item      = wAgg.getNonEmpty(i);
			input.getAggregateField()[i]  = item.getText(1);		
			input.getSubjectField()[i]    = item.getText(2);		
			input.getAggregateType()[i]       = MemoryGroupByMeta.getType(item.getText(3));
			input.getValueField()[i]    = item.getText(4);
		}
		
		stepname = wStepname.getText();        
		
		dispose();
	}

	private void get()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null && !r.isEmpty())
			{
                BaseStepDialog.getFieldsFromPrevious(r, wGroup, 1, new int[] { 1 }, new int[] {}, -1, -1, null);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "MemoryGroupByDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "MemoryGroupByDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private void getAgg()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null && !r.isEmpty())
			{
                BaseStepDialog.getFieldsFromPrevious(r, wAgg, 1, new int[] { 1, 2 }, new int[] {}, -1, -1, null);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "MemoryGroupByDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "MemoryGroupByDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
