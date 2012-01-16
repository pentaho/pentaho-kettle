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

package org.pentaho.di.ui.trans.steps.prioritizestreams;

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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.prioritizestreams.PrioritizeStreamsMeta;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class PrioritizeStreamsDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = PrioritizeStreamsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	 
	private String previousSteps[] ;
	private PrioritizeStreamsMeta input;

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;
	
	;
	
	public PrioritizeStreamsDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(PrioritizeStreamsMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
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
		shell.setText(BaseMessages.getString(PKG, "PrioritizeStreamsDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "PrioritizeStreamsDialog.Stepname.Label")); //$NON-NLS-1$
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
        previousSteps = transMeta.getPrevStepNames(stepname);
        
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "PrioritizeStreamsDialog.getPreviousSteps.Label"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, null);
        
	
        // Table with fields
		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "PrioritizeStreamsDialog.Fields.Label"));
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wStepname, margin);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsCols=1;
		final int FieldsRows=input.getStepName().length;
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo(BaseMessages.getString(PKG, "PrioritizeStreamsDialog.Fieldname.Column"),  ColumnInfo.COLUMN_TYPE_CCOMBO,  previousSteps, false);
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

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();    } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		
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
	
	private void get()
	{
		wFields.removeAll();
		Table table = wFields.table;

		for(int i=0;i<previousSteps.length;i++) {
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(0, ""+(i+1));
			ti.setText(1,previousSteps[i]);
		}
		wFields.removeEmptyRows();
        wFields.setRowNums();
		wFields.optWidth(true);
		
	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		Table table = wFields.table;
		if (input.getStepName().length>0) table.removeAll();
		for (int i=0;i<input.getStepName().length;i++)
		{
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(0, ""+(i+1));
			if(input.getStepName()[i]!=null) ti.setText(1, input.getStepName()[i]);
		}

		wFields.removeEmptyRows();
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
		
		int nrsteps = wFields.nrNonEmpty();
		input.allocate(nrsteps);
		for (int i=0;i<nrsteps;i++)
		{
			TableItem ti = wFields.getNonEmpty(i);
			StepMeta tm=transMeta.findStep(ti.getText(1));
			if(tm!=null) input.getStepName()[i] =tm.getName();

		}
		dispose();
	}
}
