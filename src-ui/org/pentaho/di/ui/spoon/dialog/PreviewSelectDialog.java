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
import java.util.List;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class PreviewSelectDialog extends Dialog
{
	private static Class<?> PKG = PreviewSelectDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlFields;
	
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private Button wPreview, wCancel;
	private Listener lsPreview, lsCancel;

	private Shell         shell;
	private TransMeta     trans;
	
	public String previewSteps[];
	public int    previewSizes[];
	
	private PropsUI props;
	
	public PreviewSelectDialog(Shell parent, int style, LogWriter l, PropsUI props, TransMeta tr)
	{
		super(parent, style);
		trans=tr;
		this.props=props;
		previewSteps=null;
		previewSizes=null;
	}

	public void open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
 		shell.setImage(GUIResource.getInstance().getImageSpoon());

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "PreviewSelectDialog.Dialog.PreviewSelection.Title")); //Preview selection screen
		shell.setImage(GUIResource.getInstance().getImageLogoSmall());
			
		int margin = Const.MARGIN;

		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "PreviewSelectDialog.Label.Steps")); //Steps: 
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(0, margin);
		wlFields.setLayoutData(fdlFields);
		
		List<StepMeta> usedSteps = trans.getUsedSteps(); 
		final int FieldsRows=usedSteps.size();
		
		ColumnInfo[] colinf = {
		  new ColumnInfo( BaseMessages.getString(PKG, "PreviewSelectDialog.Column.Stepname"), ColumnInfo.COLUMN_TYPE_TEXT, false, true ), //Stepname
		  new ColumnInfo( BaseMessages.getString(PKG, "PreviewSelectDialog.Column.PreviewSize"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //Preview size
		};
		
		wFields=new TableView(trans, shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
						      true, // read-only
						      null,
							  props
						      );

		fdFields=new FormData();
		fdFields.left   = new FormAttachment(0, 0);
		fdFields.top    = new FormAttachment(wlFields, margin);
		fdFields.right  = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);

		wPreview=new Button(shell, SWT.PUSH);
		wPreview.setText(BaseMessages.getString(PKG, "System.Button.Show"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Close"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wPreview, wCancel }, margin, null);
		
		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();  } };
		lsPreview  = new Listener() { public void handleEvent(Event e) { preview(); } };
		
		wCancel.addListener (SWT.Selection, lsCancel  );
		wPreview.addListener(SWT.Selection, lsPreview );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		BaseStepDialog.setSize(shell);

		getData();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
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
		String prSteps[] = props.getLastPreview();
		int    prSizes[] = props.getLastPreviewSize();
		String name;
		List<StepMeta> selectedSteps = trans.getSelectedSteps();
		List<StepMeta> usedSteps = trans.getUsedSteps();
		
		if (selectedSteps.size()==0) {
		
			int line=0;
			for (StepMeta stepMeta : usedSteps) {
				
				TableItem item = wFields.table.getItem(line++);
				name = stepMeta.getName();
				item.setText(1, stepMeta.getName());
				item.setText(2, "0");
	
				// Remember the last time...?
				for (int x=0;x<prSteps.length;x++)
				{
					if (prSteps[x].equalsIgnoreCase(name)) 
					{
						item.setText(2, ""+prSizes[x]);
					} 
				}
			}
		}
		else
		{		
			// No previous selection: set the selected steps to the default preview size
            //
			int line=0;
			for (StepMeta stepMeta : usedSteps)
			{
				TableItem item = wFields.table.getItem(line++);
				name = stepMeta.getName();
				item.setText(1, stepMeta.getName());
				item.setText(2, "");
	
				// Is the step selected?
				if (stepMeta.isSelected())
				{
					item.setText(2, ""+props.getDefaultPreviewSize());
				}
			}
		}
		
		wFields.optWidth(true);
	}
	
	private void cancel()
	{
		dispose();
	}
	
	private void preview()
	{
		int sels=0;
		for (int i=0;i<wFields.table.getItemCount();i++)
		{
			TableItem ti = wFields.table.getItem(i);
			int size =  Const.toInt(ti.getText(2), 0);
			if (size > 0) 
			{
				sels++;
			} 
		}
		
		previewSteps=new String[sels];
		previewSizes=new int   [sels];

		sels=0;		
		for (int i=0;i<wFields.table.getItemCount();i++)
		{
			TableItem ti = wFields.table.getItem(i);
			int size=Const.toInt(ti.getText(2), 0);

			if (size > 0) 
			{
				previewSteps[sels]=ti.getText(1);
				previewSizes[sels]=size;

				sels++;
			} 
		}
		
		props.setLastPreview(previewSteps, previewSizes);

		dispose();
	}
}
