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

 
package org.pentaho.di.ui.core.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.dialog.Messages;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;


/**
 * Displays the meta-data on the Values in a row as well as the Step origin of the Value.
 * 
 * @author Matt
 * @since 19-06-2003
 * 
 * This class has been deprecated as now it is being replaced by its XUL version.
 */
@Deprecated
public class StepFieldsDialog extends Dialog
{
	private Label        wlStepname;
	private Text         wStepname;
	private FormData     fdlStepname, fdStepname;
		
	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private Button wEdit, wCancel;
	private Listener lsEdit, lsCancel;

	private RowMetaInterface input;
	private Shell         shell;
	private PropsUI         props;
	private String        stepname;
	
	private SelectionAdapter lsDef;
	
	private VariableSpace variables;
    
	public StepFieldsDialog(Shell parent, VariableSpace space, int style, String stepname, RowMetaInterface input)
	{
			super(parent, style);
			this.stepname=stepname;
            this.input=input;
            this.variables = space;
			props=PropsUI.getInstance();			
	}

	public Object open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setImage(GUIResource.getInstance().getImageTransGraph());
		shell.setText(Messages.getString("StepFieldsDialog.Title"));
		
		int margin = Const.MARGIN;

		// Filename line
		wlStepname=new Label(shell, SWT.NONE);
		wlStepname.setText(Messages.getString("StepFieldsDialog.Name.Label"));
		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.READ_ONLY);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(wlStepname, margin);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(Messages.getString("StepFieldsDialog.Fields.Label"));
		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wlStepname, margin);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsRows=input.size();
		
		ColumnInfo[] colinf=new ColumnInfo[] {
			new ColumnInfo(Messages.getString("StepFieldsDialog.TableCol.Fieldname"),   ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
			new ColumnInfo(Messages.getString("StepFieldsDialog.TableCol.Type"),        ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
			new ColumnInfo(Messages.getString("StepFieldsDialog.TableCol.Length"),      ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
			new ColumnInfo(Messages.getString("StepFieldsDialog.TableCol.Precision"),   ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
			new ColumnInfo(Messages.getString("StepFieldsDialog.TableCol.Origin"), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
			new ColumnInfo(Messages.getString("StepFieldsDialog.TableCol.StorageType"), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
			new ColumnInfo(Messages.getString("StepFieldsDialog.TableCol.ConversionMask"), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
			new ColumnInfo(Messages.getString("StepFieldsDialog.TableCol.Decimal"), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
			new ColumnInfo(Messages.getString("StepFieldsDialog.TableCol.Group"), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
			new ColumnInfo(Messages.getString("StepFieldsDialog.TableCol.TrimType"), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
			new ColumnInfo(Messages.getString("StepFieldsDialog.TableCol.Comments"), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
		};
		
		wFields=new TableView(variables, shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
						      true, // read-only
						      null,
							  props
						      );
		wFields.optWidth(true);
		
		fdFields=new FormData();
		fdFields.left   = new FormAttachment(0, 0);
		fdFields.top    = new FormAttachment(wlFields, margin);
		fdFields.right  = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);

		wEdit=new Button(shell, SWT.PUSH);
		wEdit.setText(Messages.getString("StepFieldsDialog.Buttons.EditOrigin"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wEdit, wCancel }, margin, wFields);
		
		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsEdit       = new Listener() { public void handleEvent(Event e) { edit();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wEdit.addListener    (SWT.Selection, lsEdit    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { edit(); } };
		
		wStepname.addSelectionListener( lsDef );
				
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		
		wFields.table.addMouseListener(new MouseListener()
        {
            public void mouseDoubleClick(MouseEvent arg0)
            {
                edit();
            }

            public void mouseDown(MouseEvent arg0)
            {
            }

            public void mouseUp(MouseEvent arg0)
            {
            }
        });
		
		getData();
		
		BaseStepDialog.setSize(shell);

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
		int i;
		
		for (i=0;i<input.size();i++)
		{
			TableItem item = wFields.table.getItem(i);
			ValueMetaInterface v=input.getValueMeta(i);
			int idx=1;
			if (v.getName()!=null) item.setText(idx++, v.getName());
			item.setText(idx++, v.getTypeDesc());
			item.setText(idx++, v.getLength()<0?"-":""+v.getLength());
			item.setText(idx++, v.getPrecision()<0?"-":""+v.getPrecision());
			item.setText(idx++, Const.NVL(v.getOrigin(), ""));
			item.setText(idx++, ValueMeta.getStorageTypeCode(v.getStorageType()));
			item.setText(idx++, Const.NVL(v.getConversionMask(), ""));
			item.setText(idx++, Const.NVL(v.getDecimalSymbol(), ""));
			item.setText(idx++, Const.NVL(v.getGroupingSymbol(), ""));
			item.setText(idx++, ValueMeta.getTrimTypeDesc(v.getTrimType()));
			item.setText(idx++, Const.NVL(v.getComments(), ""));
			
		}
		wFields.optWidth(true);
	}
	
	private void cancel()
	{
		stepname=null;
		dispose();
	}
	
	private void edit()
	{
		int idx=wFields.table.getSelectionIndex();
		if (idx>=0)
		{
			stepname = wFields.table.getItem(idx).getText(5);
			dispose();
		}
		else
		{
			stepname = null;
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
            mb.setText(Messages.getString("StepFieldsDialog.OriginStep.Title")); //$NON-NLS-1$
            mb.setMessage(Messages.getString("StepFieldsDialog.OriginStep.Message")); //$NON-NLS-1$
            mb.open();
		}
		
		
	}
}
