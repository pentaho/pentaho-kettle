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

package org.pentaho.di.ui.trans.steps.rowsfromresult;

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
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.rowsfromresult.RowsFromResultMeta;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class RowsFromResultDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = RowsFromResultMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label wlFields;

	private TableView wFields;

	private FormData fdlFields, fdFields;

	private RowsFromResultMeta input;

	public RowsFromResultDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (RowsFromResultMeta) in;
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

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "RowsFromResultDialog.Shell.Title")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "RowsFromResultDialog.Stepname.Label")); //$NON-NLS-1$
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		wlFields = new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "RowsFromResultDialog.Fields.Label")); //$NON-NLS-1$
		props.setLook(wlFields);
		fdlFields = new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top = new FormAttachment(wStepname, margin);
		wlFields.setLayoutData(fdlFields);

		final int FieldsRows = input.getFieldname().length;

		ColumnInfo[] colinf = new ColumnInfo[] {
				new ColumnInfo(
						BaseMessages.getString(PKG, "RowsFromResultDialog.ColumnInfo.Fieldname"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
				new ColumnInfo(
						BaseMessages.getString(PKG, "RowsFromResultDialog.ColumnInfo.Type"), ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getAllTypes()), //$NON-NLS-1$
				new ColumnInfo(
						BaseMessages.getString(PKG, "RowsFromResultDialog.ColumnInfo.Length"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
				new ColumnInfo(
						BaseMessages.getString(PKG, "RowsFromResultDialog.ColumnInfo.Precision"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
		};

		wFields = new TableView(transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows,
				lsMod, props);

		// Some buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(wOK, -margin * 2);
		wFields.setLayoutData(fdFields);

		// Add listeners
		lsCancel = new Listener()
		{
			public void handleEvent(Event e)
			{
				cancel();
			}
		};
		lsOK = new Listener()
		{
			public void handleEvent(Event e)
			{
				ok();
			}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);

		lsDef = new SelectionAdapter()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter()
		{
			public void shellClosed(ShellEvent e)
			{
				cancel();
			}
		});

		// Set the shell size, based upon previous time...
		setSize();

		getData();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		wStepname.selectAll();

		for (int i = 0; i < input.getFieldname().length; i++)
		{
			TableItem item = wFields.table.getItem(i);
			item.setText(1, input.getFieldname()[i] == null ? "" : input.getFieldname()[i]);
			item.setText(2, ValueMeta.getTypeDesc(input.getType()[i]));
			int len = input.getLength()[i];
			int prc = input.getPrecision()[i];
			item.setText(3, len >= 0 ? "" + len : ""); //$NON-NLS-1$ //$NON-NLS-2$
			item.setText(4, prc >= 0 ? "" + prc : ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private void cancel()
	{
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText(); // return value
		int nrfields = wFields.nrNonEmpty();
		input.allocate(nrfields);
		for (int i = 0; i < nrfields; i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			input.getFieldname()[i] = item.getText(1);
			input.getType()[i] = ValueMeta.getType(item.getText(2));
			input.getLength()[i] = Const.toInt(item.getText(3), -1);
			input.getPrecision()[i] = Const.toInt(item.getText(4), -1);
		}
		dispose();
	}
}
