/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.ui.trans.steps.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.sql.ExecSQLMeta;
import org.pentaho.di.trans.steps.sql.Messages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.spoon.job.JobGraph;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.steps.tableinput.SQLValuesHighlight;

public class ExecSQLDialog extends BaseStepDialog implements StepDialogInterface
{
	private CCombo wConnection;

	private Label wlSQL;

	private StyledTextComp wSQL;

	private FormData fdlSQL, fdSQL;

	private Label wlEachRow;
	private Button wEachRow;

	private Label wlSingleStatement;
	private Button wSingleStatement;

	private Label wlInsertField;

	private Text wInsertField;

	private FormData fdlInsertField, fdInsertField;

	private Label wlUpdateField;

	private Text wUpdateField;

	private FormData fdlUpdateField, fdUpdateField;

	private Label wlDeleteField;

	private Text wDeleteField;

	private FormData fdlDeleteField, fdDeleteField;

	private Label wlReadField;

	private Text wReadField;

	private FormData fdlReadField, fdReadField;

	private Label wlFields;

	private TableView wFields;

	private FormData fdlFields, fdFields;

    private Label        wlVariables;
    private Button       wVariables;
    private FormData     fdlVariables, fdVariables; 
    
	private ExecSQLMeta input;
	private boolean changedInDialog;
	
	private Label        wlPosition;
	private FormData     fdlPosition;

	private SQLValuesHighlight lineStyler = new SQLValuesHighlight();
	
    private Map<String, Integer> inputFields;
    
    private ColumnInfo[] colinf;
	
	public ExecSQLDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (ExecSQLMeta) in;
	    inputFields =new HashMap<String, Integer>();
	}

	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
		props.setLook(shell);
		setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				changedInDialog = true;
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("ExecSQLDialog.Shell.Label")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("ExecSQLDialog.Stepname.Label")); //$NON-NLS-1$
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

		// Connection line
		wConnection = addConnectionLine(shell, wStepname, middle, margin);
		if (input.getDatabaseMeta() == null && transMeta.nrDatabases() == 1)
			wConnection.select(0);
		wConnection.addModifyListener(lsMod);

		// Table line...
		wlSQL = new Label(shell, SWT.LEFT);
		wlSQL.setText(Messages.getString("ExecSQLDialog.SQL.Label")); //$NON-NLS-1$
		props.setLook(wlSQL);
		fdlSQL = new FormData();
		fdlSQL.left = new FormAttachment(0, 0);
		fdlSQL.top = new FormAttachment(wConnection, margin * 2);
		wlSQL.setLayoutData(fdlSQL);

		wSQL = new StyledTextComp(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
		props.setLook(wSQL, Props.WIDGET_STYLE_FIXED);
		wSQL.addModifyListener(lsMod);
		wSQL.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				setPosition();
			}

		});

		wSQL.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				setPosition();
			}

			public void keyReleased(KeyEvent e) {
				setPosition();
			}
		});
		wSQL.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				setPosition();
			}

			public void focusLost(FocusEvent e) {
				setPosition();
			}
		});
		wSQL.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				setPosition();
			}

			public void mouseDown(MouseEvent e) {
				setPosition();
			}

			public void mouseUp(MouseEvent e) {
				setPosition();
			}
		});

		// Text Higlighting
		lineStyler = new SQLValuesHighlight();
		;
		wSQL.addLineStyleListener(lineStyler);

		
		// Some buttons
		//
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wGet = new Button(shell, SWT.PUSH);
		wGet.setText(Messages.getString("ExecSQLDialog.GetFields.Button")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$
		setButtonPositions(new Button[] { wOK, wCancel, wGet }, margin, null);

		// Build it up from the bottom up...
		// Read field
		//
		wlReadField = new Label(shell, SWT.RIGHT);
		wlReadField.setText(Messages.getString("ExecSQLDialog.ReadField.Label")); //$NON-NLS-1$
		props.setLook(wlReadField);
		fdlReadField = new FormData();
		fdlReadField.left = new FormAttachment(middle, margin);
		fdlReadField.right = new FormAttachment(middle * 2, -margin);
		fdlReadField.bottom = new FormAttachment(wOK, -3*margin);
		wlReadField.setLayoutData(fdlReadField);
		wReadField = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wReadField);
		wReadField.addModifyListener(lsMod);
		fdReadField = new FormData();
		fdReadField.left = new FormAttachment(middle * 2, 0);
		fdReadField.bottom = new FormAttachment(wOK, -3*margin);
		fdReadField.right = new FormAttachment(100, 0);
		wReadField.setLayoutData(fdReadField);

		// Delete field
		//
		wlDeleteField = new Label(shell, SWT.RIGHT);
		wlDeleteField.setText(Messages.getString("ExecSQLDialog.DeleteField.Label")); //$NON-NLS-1$
		props.setLook(wlDeleteField);
		fdlDeleteField = new FormData();
		fdlDeleteField.left = new FormAttachment(middle, margin);
		fdlDeleteField.right = new FormAttachment(middle * 2, -margin);
		fdlDeleteField.bottom = new FormAttachment(wReadField, -margin);
		wlDeleteField.setLayoutData(fdlDeleteField);
		wDeleteField = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wDeleteField);
		wDeleteField.addModifyListener(lsMod);
		fdDeleteField = new FormData();
		fdDeleteField.left = new FormAttachment(middle * 2, 0);
		fdDeleteField.bottom = new FormAttachment(wReadField, -margin);
		fdDeleteField.right = new FormAttachment(100, 0);
		wDeleteField.setLayoutData(fdDeleteField);

		// Update field
		//
		wlUpdateField = new Label(shell, SWT.RIGHT);
		wlUpdateField.setText(Messages.getString("ExecSQLDialog.UpdateField.Label")); //$NON-NLS-1$
		props.setLook(wlUpdateField);
		fdlUpdateField = new FormData();
		fdlUpdateField.left = new FormAttachment(middle, margin);
		fdlUpdateField.right = new FormAttachment(middle * 2, -margin);
		fdlUpdateField.bottom = new FormAttachment(wDeleteField, -margin);
		wlUpdateField.setLayoutData(fdlUpdateField);
		wUpdateField = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wUpdateField);
		wUpdateField.addModifyListener(lsMod);
		fdUpdateField = new FormData();
		fdUpdateField.left = new FormAttachment(middle * 2, 0);
		fdUpdateField.bottom = new FormAttachment(wDeleteField, -margin);
		fdUpdateField.right = new FormAttachment(100, 0);
		wUpdateField.setLayoutData(fdUpdateField);

		// insert field
		//
		wlInsertField = new Label(shell, SWT.RIGHT);
		wlInsertField.setText(Messages.getString("ExecSQLDialog.InsertField.Label")); //$NON-NLS-1$
		props.setLook(wlInsertField);
		fdlInsertField = new FormData();
		fdlInsertField.left = new FormAttachment(middle, margin);
		fdlInsertField.right = new FormAttachment(middle * 2, -margin);
		fdlInsertField.bottom= new FormAttachment(wUpdateField, -margin);
		wlInsertField.setLayoutData(fdlInsertField);
		wInsertField = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wInsertField);
		wInsertField.addModifyListener(lsMod);
		fdInsertField = new FormData();
		fdInsertField.left = new FormAttachment(middle * 2, 0);
		fdInsertField.bottom = new FormAttachment(wUpdateField, -margin);
		fdInsertField.right = new FormAttachment(100, 0);
		wInsertField.setLayoutData(fdInsertField);

		// Setup the "Parameters" label
		//
		wlFields = new Label(shell, SWT.NONE);
		wlFields.setText(Messages.getString("ExecSQLDialog.Fields.Label")); //$NON-NLS-1$
		props.setLook(wlFields);
		fdlFields = new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.right = new FormAttachment(middle, 0);
		fdlFields.bottom = new FormAttachment(wInsertField, -25);
		wlFields.setLayoutData(fdlFields);

		// Parameter fields...
		//
		final int FieldsRows = input.getArguments().length;

		colinf = new ColumnInfo[] { 
				new ColumnInfo( Messages.getString("ExecSQLDialog.ColumnInfo.ArgumentFieldname"), 
							    ColumnInfo.COLUMN_TYPE_CCOMBO, 
							    new String[] { "" }, 
							    false
							  ), 
			};

		wFields = new TableView(transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props);
		fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(middle, 0);
		fdFields.bottom = new FormAttachment(wOK, -3*margin);
		wFields.setLayoutData(fdFields);

		// For the "execute for each row" and "variable substitution" labels,
		// find their maximum width
		// and use that in the alignment
		//
		wlEachRow = new Label(shell, SWT.RIGHT);
		wlEachRow.setText(Messages.getString("ExecSQLDialog.EachRow.Label")); //$NON-NLS-1$
		wlEachRow.pack();
		wlSingleStatement= new Label(shell, SWT.RIGHT);
		wlSingleStatement.setText(Messages.getString("ExecSQLDialog.SingleStatement.Label")); //$NON-NLS-1$
		wlSingleStatement.pack();
		wlVariables = new Label(shell, SWT.RIGHT);
		wlVariables.setText(Messages.getString("ExecSQLDialog.ReplaceVariables")); //$NON-NLS-1$
		wlVariables.pack();
		Rectangle rEachRow = wlEachRow.getBounds();
		Rectangle rSingleStatement = wlSingleStatement.getBounds();
		Rectangle rVariables = wlVariables.getBounds();
		int width = Math.max(Math.max(rEachRow.width, rSingleStatement.width), rVariables.width) + 30;

		// Setup the "variable substitution" label and checkbox
		//
		props.setLook(wlVariables);
		fdlVariables = new FormData();
		fdlVariables.left = new FormAttachment(0, margin);
		fdlVariables.right = new FormAttachment(0, width);
		fdlVariables.bottom = new FormAttachment(wlFields, -2*margin);
		wlVariables.setLayoutData(fdlVariables);
		wVariables = new Button(shell, SWT.CHECK);
		props.setLook(wVariables);
		fdVariables = new FormData();
		fdVariables.left = new FormAttachment(wlVariables, margin);
		fdVariables.bottom = new FormAttachment(wlFields, -2*margin);
		fdVariables.right = new FormAttachment(middle, 0);
		wVariables.setLayoutData(fdVariables);
		// wVariables.addSelectionListener(new SelectionAdapter() { public void
		// widgetSelected(SelectionEvent arg0) { setSQLToolTip(); } });

		// Setup the "Single statement" label and checkbox
		//
		props.setLook(wlSingleStatement);
		FormData fdlSingleStatement = new FormData();
		fdlSingleStatement.left = new FormAttachment(0, margin);
		fdlSingleStatement.right = new FormAttachment(0, width);
		fdlSingleStatement.bottom = new FormAttachment(wVariables, -margin);
		wlSingleStatement.setLayoutData(fdlSingleStatement);
		wSingleStatement = new Button(shell, SWT.CHECK);
		props.setLook(wSingleStatement);
		FormData fdSingleStatement = new FormData();
		fdSingleStatement.left = new FormAttachment(wlEachRow, margin);
		fdSingleStatement.bottom = new FormAttachment(wVariables, -margin);
		fdSingleStatement.right = new FormAttachment(middle, 0);
		wSingleStatement.setLayoutData(fdSingleStatement);

		// Setup the "execute for each row" label and checkbox
		//
		props.setLook(wlEachRow);
		FormData fdlEachRow = new FormData();
		fdlEachRow.left = new FormAttachment(0, margin);
		fdlEachRow.right = new FormAttachment(0, width);
		fdlEachRow.bottom = new FormAttachment(wSingleStatement, -margin);
		wlEachRow.setLayoutData(fdlEachRow);
		wEachRow = new Button(shell, SWT.CHECK);
		props.setLook(wEachRow);
		FormData fdEachRow = new FormData();
		fdEachRow.left = new FormAttachment(wlEachRow, margin);
		fdEachRow.bottom = new FormAttachment(wSingleStatement, -margin);
		fdEachRow.right = new FormAttachment(middle, 0);
		wEachRow.setLayoutData(fdEachRow);


		// Position label under the SQL editor
		//
		wlPosition = new Label(shell, SWT.NONE);
		props.setLook(wlPosition);
		fdlPosition = new FormData();
		fdlPosition.left = new FormAttachment(0, 0);
		fdlPosition.right = new FormAttachment(100, 0);
		fdlPosition.bottom= new FormAttachment(wEachRow, -margin);
		wlPosition.setLayoutData(fdlPosition);

		// Finally, the SQL editor takes up all other space between the position and the SQL label  
		//
		fdSQL = new FormData();
		fdSQL.left = new FormAttachment(0, 0);
		fdSQL.top = new FormAttachment(wlSQL, margin);
		fdSQL.right = new FormAttachment(100, 0);
		fdSQL.bottom = new FormAttachment(wlPosition, -margin);
		wSQL.setLayoutData(fdSQL);

		
		// Search the fields in the background
		//
		final Runnable runnable = new Runnable() {
			public void run() {
				StepMeta stepMeta = transMeta.findStep(stepname);
				if (stepMeta != null) {
					try {
						RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);

						// Remember these fields...
						for (int i = 0; i < row.size(); i++) {
							inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
						}
						setComboBoxes();
					} catch (KettleException e) {
						LogWriter.getInstance().logError(toString(), Messages.getString("System.Dialog.GetFieldsFailed.Message"));
					}
				}
			}
		};
		new Thread(runnable).start();

		// Add listeners
		//
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};
		lsGet = new Listener() {
			public void handleEvent(Event e) {
				get();
			}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wGet.addListener(SWT.Selection, lsGet);
		wOK.addListener(SWT.Selection, lsOK);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);
		wEachRow.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				checkCancel(e);
			}
		});

		getData();
		changedInDialog = false; // for prompting if dialog is simply closed
		input.setChanged(changed);

		// Set the shell size, based upon previous time...
		setSize();

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}

	public void setPosition(){
		
		String scr = wSQL.getText();
		int linenr = wSQL.getLineAtOffset(wSQL.getCaretOffset())+1;
		int posnr  = wSQL.getCaretOffset();
				
		// Go back from position to last CR: how many positions?
		int colnr=0;
		while (posnr>0 && scr.charAt(posnr-1)!='\n' && scr.charAt(posnr-1)!='\r')
		{
			posnr--;
			colnr++;
		}
		wlPosition.setText(Messages.getString("ExecSQLDialog.Position.Label",""+linenr,""+colnr));

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
	public void getData() {
		if (input.getSql() != null)
			wSQL.setText(input.getSql());
		if (input.getDatabaseMeta() != null)
			wConnection.setText(input.getDatabaseMeta().getName());
		wEachRow.setSelection(input.isExecutedEachInputRow());
		wSingleStatement.setSelection(input.isSingleStatement());
		wVariables.setSelection(input.isReplaceVariables());

		if (input.getUpdateField() != null)
			wUpdateField.setText(input.getUpdateField());
		if (input.getInsertField() != null)
			wInsertField.setText(input.getInsertField());
		if (input.getDeleteField() != null)
			wDeleteField.setText(input.getDeleteField());
		if (input.getReadField() != null)
			wReadField.setText(input.getReadField());

		for (int i = 0; i < input.getArguments().length; i++) {
			TableItem item = wFields.table.getItem(i);
			if (input.getArguments()[i] != null)
				item.setText(1, input.getArguments()[i]);
		}

		wStepname.selectAll();
	}

	private void checkCancel(ShellEvent e)
	{
		if (changedInDialog)
		{
			int save = JobGraph.showChangedWarning(shell, wStepname.getText());
			if (save == SWT.CANCEL)
			{
				e.doit = false;
			}
			else if (save == SWT.YES)
			{
				ok();
			}
			else
			{
				cancel();
			}
		}
		else
		{
			cancel();
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
		// copy info to TextFileInputMeta class (input)
		input.setSql(wSQL.getText());
		input.setDatabaseMeta(transMeta.findDatabase(wConnection.getText()));
		input.setExecutedEachInputRow(wEachRow.getSelection());
		input.setVariableReplacementActive(wVariables.getSelection());
		input.setSingleStatement(wSingleStatement.getSelection());
		input.setInsertField(wInsertField.getText());
		input.setUpdateField(wUpdateField.getText());
		input.setDeleteField(wDeleteField.getText());
		input.setReadField(wReadField.getText());
		
		int nrargs = wFields.nrNonEmpty();
		input.allocate(nrargs);
		if (log.isDebug())
			log.logDebug(toString(), Messages.getString("ExecSQLDialog.Log.FoundArguments", +nrargs + "")); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < nrargs; i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			input.getArguments()[i] = item.getText(1);
		}

		if (input.getDatabaseMeta() == null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage(Messages.getString("ExecSQLDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("ExecSQLDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}

		dispose();
	}

	private void get()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r != null)
			{
				BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1 }, new int[] {}, -1, -1,
						null);
			}
		} catch (KettleException ke)
		{
			new ErrorDialog(
					shell,
					Messages.getString("ExecSQLDialog.FailedToGetFields.DialogTitle"), Messages.getString("ExecSQLDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
}
