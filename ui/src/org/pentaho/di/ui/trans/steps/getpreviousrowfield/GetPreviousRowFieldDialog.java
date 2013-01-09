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

package org.pentaho.di.ui.trans.steps.getpreviousrowfield;
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
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.getpreviousrowfield.GetPreviousRowFieldMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

/**
 * return field value from previous row.
  * 
 * @author Samatar Hassan
 * @since 07 September 2008
 */
public class GetPreviousRowFieldDialog extends BaseStepDialog implements StepDialogInterface {
	private static Class<?> PKG = GetPreviousRowFieldMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label wlKey;

	private TableView wFields;

	private FormData fdlKey, fdKey;

	private GetPreviousRowFieldMeta input;
	
    private Map<String, Integer> inputFields;
    
    private ColumnInfo[] ciKey;

	public GetPreviousRowFieldDialog(Shell parent, Object in, TransMeta tr, String sname) {
		super(parent, (BaseStepMeta) in, tr, sname);
		input = (GetPreviousRowFieldMeta) in;
        inputFields =new HashMap<String, Integer>();
	}

	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX
				| SWT.MIN);
		props.setLook(shell);
		setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "GetPreviousRowFieldDialog.Shell.Title")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "GetPreviousRowFieldDialog.Stepname.Label")); //$NON-NLS-1$
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
		

		wlKey = new Label(shell, SWT.NONE);
		wlKey.setText(BaseMessages.getString(PKG, "GetPreviousRowFieldDialog.Fields.Label")); //$NON-NLS-1$
		props.setLook(wlKey);
		fdlKey = new FormData();
		fdlKey.left = new FormAttachment(0, 0);
		fdlKey.top = new FormAttachment(wStepname, 2*margin);
		wlKey.setLayoutData(fdlKey);

		int nrFieldCols = 2;
		int nrFieldRows = (input.getFieldInStream() != null ? input.getFieldInStream().length : 1);

		ciKey = new ColumnInfo[nrFieldCols];
		ciKey[0] = new ColumnInfo(
				BaseMessages.getString(PKG, "GetPreviousRowFieldDialog.ColumnInfo.InStreamField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false);//$NON-NLS-1$
		ciKey[1] = new ColumnInfo(
				BaseMessages.getString(PKG, "GetPreviousRowFieldDialog.ColumnInfo.OutStreamField"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$
		ciKey[1].setUsingVariables(true);
		wFields = new TableView(transMeta,shell, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
				ciKey, nrFieldRows, lsMod, props);

		fdKey = new FormData();
		fdKey.left = new FormAttachment(0, 0);
		fdKey.top = new FormAttachment(wlKey, margin);
		fdKey.right = new FormAttachment(100, -margin);
		fdKey.bottom = new FormAttachment(100, -30);
		wFields.setLayoutData(fdKey);

	     // 
        // Search the fields in the background
        //
        
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
                    	logError("It was not possible to get the fields from the previous step(s).");
                    }
                }
            }
        };
        new Thread(runnable).start();
        
		
		// THE BUTTONS
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		wGet = new Button(shell, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "GetPreviousRowFieldDialog.GetFields.Button")); //$NON-NLS-1$
		fdGet = new FormData();
		fdGet.right = new FormAttachment(100, 0);
		fdGet.top = new FormAttachment(wStepname, 3*middle);
		wGet.setLayoutData(fdGet);
		
		setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, null);

		// Add listeners
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};
		lsGet = new Listener() {
			public void handleEvent(Event e) {
				get();
			}
		};
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};


		wOK.addListener(SWT.Selection, lsOK);
		wGet.addListener(SWT.Selection, lsGet);
		wCancel.addListener(SWT.Selection, lsCancel);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		// Set the shell size, based upon previous time...
		setSize();

		getData();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
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
        
        shell.getDisplay().syncExec(new Runnable()
            {
                public void run()
                {
                    Set<String> keySet = fields.keySet();
                    List<String> entries = new ArrayList<String>(keySet);
                    
                    String fieldNames[] = (String[]) entries.toArray(new String[entries.size()]);

                    Const.sortStrings(fieldNames);
                    ciKey[0].setComboValues(fieldNames);
                }
            }
        );
    }
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData() {
		int i;
		if (input.getFieldInStream() != null) {
			for (i = 0; i < input.getFieldInStream().length; i++) {
				TableItem item = wFields.table.getItem(i);
				if (input.getFieldInStream()[i] != null) item.setText(1, input.getFieldInStream()[i]);
				if (input.getFieldOutStream()[i] != null) item.setText(2, input.getFieldOutStream()[i]);
			}
		}

		wStepname.selectAll();
		wFields.setRowNums();
		wFields.optWidth(true);
	}

	private void cancel() {
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	
	private void getInfo(GetPreviousRowFieldMeta inf) {
		int nrkeys = wFields.nrNonEmpty();

		inf.allocate(nrkeys);

		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "GetPreviousRowFieldDialog.Log.FoundFields", String.valueOf(nrkeys))); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < nrkeys; i++) {
			TableItem item = wFields.getNonEmpty(i);
			inf.getFieldInStream()[i] = item.getText(1);
			inf.getFieldOutStream()[i] = item.getText(2);
		}

		stepname = wStepname.getText(); // return value
	}

	private void ok() {
		if (Const.isEmpty(wStepname.getText()))
			return;

		// Get the information for the dialog into the input structure.
		getInfo(input);

		dispose();
	}


	private void get() {
		try {
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r != null) {
				TableItemInsertListener listener = new TableItemInsertListener() {
					public boolean tableItemInserted(TableItem tableItem,ValueMetaInterface v) {
						return true;
					}
				};

				BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1 }, new int[] {}, -1, -1, listener);

			}
		} catch (KettleException ke) {
			new ErrorDialog(
					shell,BaseMessages.getString(PKG, "GetPreviousRowFieldDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "GetPreviousRowFieldDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}