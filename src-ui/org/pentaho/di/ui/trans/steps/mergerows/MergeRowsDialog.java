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

package org.pentaho.di.ui.trans.steps.mergerows;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.mergerows.MergeRowsMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class MergeRowsDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = MergeRowsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlReference;
	private CCombo       wReference;
	private FormData     fdlReference, fdReference;

	private Label        wlCompare;
	private CCombo       wCompare;
	private FormData     fdlCompare, fdCompare;
    
    private Label        wlFlagfield;
    private Text         wFlagfield;
    private FormData     fdlFlagfield, fdFlagfield;
    
    private Label        wlKeys;
    private TableView    wKeys;
    private Button       wbKeys;
    private FormData     fdlKeys, fdKeys, fdbKeys;

    private Label        wlValues;
    private TableView    wValues;
    private Button       wbValues;
    private FormData     fdlValues, fdValues, fdbValues;

	private MergeRowsMeta input;
	
	public MergeRowsDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(MergeRowsMeta)in;
     }

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
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
		shell.setText(BaseMessages.getString(PKG, "MergeRowsDialog.Shell.Label")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "MergeRowsDialog.Stepname.Label")); //$NON-NLS-1$
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

        // Get the previous steps...
        String previousSteps[] = transMeta.getPrevStepNames(stepname);
        
		// Send 'True' data to...
		wlReference=new Label(shell, SWT.RIGHT);
		wlReference.setText(BaseMessages.getString(PKG, "MergeRowsDialog.Reference.Label")); //$NON-NLS-1$
 		props.setLook(wlReference);
		fdlReference=new FormData();
		fdlReference.left = new FormAttachment(0, 0);
		fdlReference.right= new FormAttachment(middle, -margin);
		fdlReference.top  = new FormAttachment(wStepname, margin);
		wlReference.setLayoutData(fdlReference);
		wReference=new CCombo(shell, SWT.BORDER );
 		props.setLook(wReference);

		if (previousSteps!=null)
		{
			wReference.setItems( previousSteps );
		}
		
		wReference.addModifyListener(lsMod);
		fdReference=new FormData();
		fdReference.left = new FormAttachment(middle, 0);
		fdReference.top  = new FormAttachment(wStepname, margin);
		fdReference.right= new FormAttachment(100, 0);
		wReference.setLayoutData(fdReference);

		// Send 'False' data to...
		wlCompare=new Label(shell, SWT.RIGHT);
		wlCompare.setText(BaseMessages.getString(PKG, "MergeRowsDialog.Compare.Label")); //$NON-NLS-1$
 		props.setLook(wlCompare);
		fdlCompare=new FormData();
		fdlCompare.left = new FormAttachment(0, 0);
		fdlCompare.right= new FormAttachment(middle, -margin);
		fdlCompare.top  = new FormAttachment(wReference, margin);
		wlCompare.setLayoutData(fdlCompare);
		wCompare=new CCombo(shell, SWT.BORDER );
 		props.setLook(wCompare);

        if (previousSteps!=null)
        {
            wCompare.setItems( previousSteps );
        }	
        
		wCompare.addModifyListener(lsMod);
		fdCompare=new FormData();
        fdCompare.top  = new FormAttachment(wReference, margin);
		fdCompare.left = new FormAttachment(middle, 0);
		fdCompare.right= new FormAttachment(100, 0);
		wCompare.setLayoutData(fdCompare);

        
        // Stepname line
        wlFlagfield=new Label(shell, SWT.RIGHT);
        wlFlagfield.setText(BaseMessages.getString(PKG, "MergeRowsDialog.FlagField.Label")); //$NON-NLS-1$
        props.setLook(wlFlagfield);
        fdlFlagfield=new FormData();
        fdlFlagfield.left = new FormAttachment(0, 0);
        fdlFlagfield.right= new FormAttachment(middle, -margin);
        fdlFlagfield.top  = new FormAttachment(wCompare, margin);
        wlFlagfield.setLayoutData(fdlFlagfield);
        wFlagfield=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wFlagfield);
        wFlagfield.addModifyListener(lsMod);
        fdFlagfield=new FormData();
        fdFlagfield.top  = new FormAttachment(wCompare, margin);
        fdFlagfield.left = new FormAttachment(middle, 0);
        fdFlagfield.right= new FormAttachment(100, 0);
        wFlagfield.setLayoutData(fdFlagfield);

        
        // THE KEYS TO MATCH...
        wlKeys=new Label(shell, SWT.NONE);
        wlKeys.setText(BaseMessages.getString(PKG, "MergeRowsDialog.Keys.Label")); //$NON-NLS-1$
        props.setLook(wlKeys);
        fdlKeys=new FormData();
        fdlKeys.left  = new FormAttachment(0, 0);
        fdlKeys.top   = new FormAttachment(wFlagfield, margin);
        wlKeys.setLayoutData(fdlKeys);
        
        int nrKeyRows= (input.getKeyFields()!=null?input.getKeyFields().length:1);
        
        ColumnInfo[] ciKeys=new ColumnInfo[] {
            new ColumnInfo(BaseMessages.getString(PKG, "MergeRowsDialog.ColumnInfo.KeyField"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
        };
            
        wKeys=new TableView(transMeta, shell, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
                              ciKeys, 
                              nrKeyRows,  
                              lsMod,
                              props
                              );

        fdKeys = new FormData();
        fdKeys.top    = new FormAttachment(wlKeys, margin);
        fdKeys.left   = new FormAttachment(0,   0);
        fdKeys.bottom = new FormAttachment(100, -70);
        fdKeys.right  = new FormAttachment(50, -margin);
        wKeys.setLayoutData(fdKeys);

        wbKeys=new Button(shell, SWT.PUSH);
        wbKeys.setText(BaseMessages.getString(PKG, "MergeRowsDialog.KeyFields.Button")); //$NON-NLS-1$
        fdbKeys = new FormData();
        fdbKeys.top   = new FormAttachment(wKeys, margin);
        fdbKeys.left  = new FormAttachment(0, 0);
        fdbKeys.right = new FormAttachment(50, -margin);
        wbKeys.setLayoutData(fdbKeys);
        wbKeys.addSelectionListener(new SelectionAdapter()
            {
            
                public void widgetSelected(SelectionEvent e)
                {
                    getKeys();
                }
            }
        );


        // VALUES TO COMPARE
        wlValues=new Label(shell, SWT.NONE);
        wlValues.setText(BaseMessages.getString(PKG, "MergeRowsDialog.Values.Label")); //$NON-NLS-1$
        props.setLook(wlValues);
        fdlValues=new FormData();
        fdlValues.left  = new FormAttachment(50, 0);
        fdlValues.top   = new FormAttachment(wFlagfield, margin);
        wlValues.setLayoutData(fdlValues);
        
        int nrValueRows= (input.getValueFields()!=null?input.getValueFields().length:1);
        
        ColumnInfo[] ciValues=new ColumnInfo[] {
            new ColumnInfo(BaseMessages.getString(PKG, "MergeRowsDialog.ColumnInfo.ValueField"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
        };
            
        wValues=new TableView(transMeta, shell, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
                              ciValues, 
                              nrValueRows,  
                              lsMod,
                              props
                              );

        fdValues = new FormData();
        fdValues.top    = new FormAttachment(wlValues, margin);
        fdValues.left   = new FormAttachment(50,  0);
        fdValues.bottom = new FormAttachment(100, -70);
        fdValues.right  = new FormAttachment(100, 0);
        wValues.setLayoutData(fdValues);

        
        wbValues=new Button(shell, SWT.PUSH);
        wbValues.setText(BaseMessages.getString(PKG, "MergeRowsDialog.ValueFields.Button")); //$NON-NLS-1$
        fdbValues = new FormData();
        fdbValues.top   = new FormAttachment(wValues, margin);
        fdbValues.left  = new FormAttachment(50,  0);
        fdbValues.right = new FormAttachment(100, 0);
        wbValues.setLayoutData(fdbValues);
        wbValues.addSelectionListener(new SelectionAdapter()
                {
                
                    public void widgetSelected(SelectionEvent e)
                    {
                        getValues();
                    }
                }
            );
        
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wbKeys);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
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
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		List<StreamInterface> infoStreams = input.getStepIOMeta().getInfoStreams();
		
		wReference.setText(Const.NVL(infoStreams.get(0).getStepname(), ""));
		wCompare.setText(Const.NVL(infoStreams.get(1).getStepname(), ""));
        if (input.getFlagField() !=null ) wFlagfield.setText(input.getFlagField() ); 
        
        for (int i=0;i<input.getKeyFields().length;i++)
        {
            TableItem item = wKeys.table.getItem(i);
            if (input.getKeyFields()[i]!=null) item.setText(1, input.getKeyFields()[i]);
        }
        for (int i=0;i<input.getValueFields().length;i++)
        {
            TableItem item = wValues.table.getItem(i);
            if (input.getValueFields()[i]!=null) item.setText(1, input.getValueFields()[i]);
        }
        
        wStepname.selectAll();
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

		List<StreamInterface> infoStreams = input.getStepIOMeta().getInfoStreams();
		infoStreams.get(0).setStepMeta( transMeta.findStep( wReference.getText() ) );
		infoStreams.get(1).setStepMeta( transMeta.findStep( wCompare.getText() ) );
        input.setFlagField( wFlagfield.getText());

        int nrKeys   = wKeys.nrNonEmpty();
        int nrValues = wValues.nrNonEmpty();

        input.allocate(nrKeys, nrValues );
        
        for (int i=0;i<nrKeys;i++)
        {
            TableItem item = wKeys.getNonEmpty(i);
            input.getKeyFields()[i] = item.getText(1);
        }

        for (int i=0;i<nrValues;i++)
        {
            TableItem item = wValues.getNonEmpty(i);
            input.getValueFields()[i] = item.getText(1);
        }

		stepname = wStepname.getText(); // return value
		
		dispose();
	}
    
    private void getKeys()
    {
        try
        {
            StepMeta stepMeta = transMeta.findStep(wReference.getText());
            if (stepMeta!=null)
            {
                RowMetaInterface prev = transMeta.getStepFields(stepMeta);
                if (prev!=null)
                {
                    BaseStepDialog.getFieldsFromPrevious(prev, wKeys, 1, new int[] { 1 }, new int[] { }, -1, -1, null);
                }
            }
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "MergeRowsDialog.ErrorGettingFields.DialogTitle"), BaseMessages.getString(PKG, "MergeRowsDialog.ErrorGettingFields.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    private void getValues()
    {
        try
        {
            StepMeta stepMeta = transMeta.findStep(wReference.getText());
            if (stepMeta!=null)
            {
                RowMetaInterface prev = transMeta.getStepFields(stepMeta);
                if (prev!=null)
                {
                    BaseStepDialog.getFieldsFromPrevious(prev, wValues, 1, new int[] { 1 }, new int[] { }, -1, -1, null);
                }
            }
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "MergeRowsDialog.ErrorGettingFields.DialogTitle"), BaseMessages.getString(PKG, "MergeRowsDialog.ErrorGettingFields.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}
