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

package org.pentaho.di.ui.spoon.dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


/**
 * Shows a dialog that allows you to select the steps you want to preview by entering a number of rows.
 *  
 * @author Matt
 *
 */
public class EnterPreviewRowsDialog extends Dialog
{
	private static Class<?> PKG = EnterPreviewRowsDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String       stepname;
		
	private Label        wlStepList;
	private List wStepList;
    private FormData     fdlStepList, fdStepList;
	
	private Button wShow, wClose;
	private Listener lsShow, lsClose;

	private Shell         shell;
	private java.util.List<String> stepNames;
	private java.util.List<RowMetaInterface> rowMetas;
	private java.util.List<java.util.List<Object[]>> rowDatas;
	private PropsUI 		  props;

	public EnterPreviewRowsDialog(Shell parent, int style, java.util.List<String> stepNames, java.util.List<RowMetaInterface> rowMetas, java.util.List<java.util.List<Object[]>> rowBuffers)
	{
		super(parent, style);
		this.stepNames=stepNames;
		this.rowDatas=rowBuffers;
        this.rowMetas = rowMetas;
		props=PropsUI.getInstance();
	}

	public Object open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "EnterPreviewRowsDialog.Dialog.PreviewStep.Title")); //Select the preview step:
		shell.setImage(GUIResource.getInstance().getImageLogoSmall());

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlStepList=new Label(shell, SWT.NONE);
		wlStepList.setText(BaseMessages.getString(PKG, "EnterPreviewRowsDialog.Dialog.PreviewStep.Message")); //Step name : 
 		props.setLook(wlStepList);
		fdlStepList=new FormData();
		fdlStepList.left = new FormAttachment(0, 0);
		fdlStepList.top  = new FormAttachment(0, margin);
		wlStepList.setLayoutData(fdlStepList);
		wStepList=new List(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		for (int i=0;i<stepNames.size();i++)
		{
			wStepList.add((String)stepNames.get(i)); 
		}
		wStepList.select(0);
 		props.setLook(wStepList);
		fdStepList=new FormData();
		fdStepList.left   = new FormAttachment(middle, 0);
		fdStepList.top    = new FormAttachment(0, margin);
		fdStepList.bottom = new FormAttachment(100, -60);
		fdStepList.right  = new FormAttachment(100, 0);
		wStepList.setLayoutData(fdStepList);
		wStepList.addSelectionListener(new SelectionAdapter()
		{
			public void widgetDefaultSelected(SelectionEvent arg0)
			{
				show();
			}
		});

		wShow=new Button(shell, SWT.PUSH);
		wShow.setText(BaseMessages.getString(PKG, "System.Button.Show"));

		wClose=new Button(shell, SWT.PUSH);
		wClose.setText(BaseMessages.getString(PKG, "System.Button.Close"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wShow, wClose }, margin, null);
		// Add listeners
		lsShow       = new Listener() { public void handleEvent(Event e) { show();     } };
		lsClose   = new Listener() { public void handleEvent(Event e) { close(); } };

		wShow.addListener (SWT.Selection, lsShow    );
		wClose.addListener(SWT.Selection, lsClose    );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { close(); } } );

		getData();

		BaseStepDialog.setSize(shell);

		// Immediately show the only preview entry
		if (stepNames.size()==1)
		{
			wStepList.select(0);
			show();
		}
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{	
	}

	private void close()
	{
		dispose();
	}
	
	private void show()
	{
		if (rowDatas.size()==0) return;
		
		int nr = wStepList.getSelectionIndex();

		java.util.List<Object[]> buffer = (java.util.List<Object[]>)rowDatas.get(nr);
        RowMetaInterface rowMeta = (RowMetaInterface)rowMetas.get(nr);
		String    name   = (String)stepNames.get(nr);
		
        if (rowMeta!=null && buffer!=null && buffer.size()>0)
        {
    		PreviewRowsDialog prd = new PreviewRowsDialog(shell,
    										Variables.getADefaultVariableSpace(),
    				                        SWT.NONE, name, rowMeta, buffer);
    		prd.open();
        }
        else
        {
        	MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
        	mb.setText(BaseMessages.getString(PKG, "EnterPreviewRowsDialog.Dialog.NoPreviewRowsFound.Title"));
        	mb.setMessage(BaseMessages.getString(PKG, "EnterPreviewRowsDialog.Dialog.NoPreviewRowsFound.Message"));
        	mb.open();
        }
	}
}
