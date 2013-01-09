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

package org.pentaho.di.ui.trans.steps.delete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.delete.DeleteMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

/**
 * 
 * @author Tom
 * @since 28-March-2006
 *
 */
public class DeleteDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = DeleteMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CCombo       wConnection;

	private Label        wlKey;
	private TableView    wKey;
	private FormData     fdlKey, fdKey;

    private Label        wlSchema;
    private TextVar      wSchema;
    private FormData     fdlSchema, fdSchema;
    private FormData	fdbSchema;
    private Button		wbSchema;

	private Label        wlTable;
	private Button       wbTable;
	private TextVar         wTable;
	private FormData     fdlTable, fdbTable, fdTable;

	private Label        wlCommit;
	private Text         wCommit;
	private FormData     fdlCommit, fdCommit;
    
	private DeleteMeta input;
	
    private Map<String, Integer> inputFields;
    
    private ColumnInfo[] ciKey;

	/**
	 * List of ColumnInfo that should have the field names of the selected database table
	 */
	private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();
    
	public DeleteDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(DeleteMeta)in;
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
		ModifyListener lsTableMod = new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				input.setChanged();
				setTableFieldCombo();
			}
		};
		SelectionListener lsSelection = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e) 
			{
				input.setChanged();
				setTableFieldCombo();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "DeleteDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "DeleteDialog.Stepname.Label")); //$NON-NLS-1$
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

		// Connection line
		wConnection = addConnectionLine(shell, wStepname, middle, margin);
		if (input.getDatabaseMeta()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);
		wConnection.addSelectionListener(lsSelection);

        // Schema line...
        wlSchema=new Label(shell, SWT.RIGHT);
        wlSchema.setText(BaseMessages.getString(PKG, "DeleteDialog.TargetSchema.Label")); //$NON-NLS-1$
        props.setLook(wlSchema);
        fdlSchema=new FormData();
        fdlSchema.left = new FormAttachment(0, 0);
        fdlSchema.right= new FormAttachment(middle, -margin);
        fdlSchema.top  = new FormAttachment(wConnection, margin*2);
        wlSchema.setLayoutData(fdlSchema);
        
        wbSchema=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbSchema);
 		wbSchema.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
 		fdbSchema=new FormData();
 		fdbSchema.top  = new FormAttachment(wConnection, 2*margin);
 		fdbSchema.right= new FormAttachment(100, 0);
		wbSchema.setLayoutData(fdbSchema);

        wSchema=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wSchema);
        wSchema.addModifyListener(lsTableMod);
        fdSchema=new FormData();
        fdSchema.left = new FormAttachment(middle, 0);
        fdSchema.top  = new FormAttachment(wConnection, margin*2);
        fdSchema.right= new FormAttachment(wbSchema, -margin);
        wSchema.setLayoutData(fdSchema);

		// Table line...
		wlTable=new Label(shell, SWT.RIGHT);
		wlTable.setText(BaseMessages.getString(PKG, "DeleteDialog.TargetTable.Label")); //$NON-NLS-1$
 		props.setLook(wlTable);
		fdlTable=new FormData();
		fdlTable.left = new FormAttachment(0, 0);
		fdlTable.right= new FormAttachment(middle, -margin);
		fdlTable.top  = new FormAttachment(wbSchema, margin);
		wlTable.setLayoutData(fdlTable);

		wbTable=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText(BaseMessages.getString(PKG, "DeleteDialog.Browse.Button")); //$NON-NLS-1$
		fdbTable=new FormData();
		fdbTable.right= new FormAttachment(100, 0);
		fdbTable.top  = new FormAttachment(wbSchema, margin);
		wbTable.setLayoutData(fdbTable);

		wTable=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTable);
		wTable.addModifyListener(lsTableMod);
		fdTable=new FormData();
		fdTable.left = new FormAttachment(middle, 0);
		fdTable.top  = new FormAttachment(wbSchema, margin);
		fdTable.right= new FormAttachment(wbTable, -margin);
		wTable.setLayoutData(fdTable);

		// Commit line
		wlCommit = new Label(shell, SWT.RIGHT);
		wlCommit.setText(BaseMessages.getString(PKG, "DeleteDialog.Commit.Label")); //$NON-NLS-1$
 		props.setLook(wlCommit);
		fdlCommit = new FormData();
		fdlCommit.left = new FormAttachment(0, 0);
		fdlCommit.top = new FormAttachment(wTable, margin);
		fdlCommit.right = new FormAttachment(middle, -margin);
		wlCommit.setLayoutData(fdlCommit);
		wCommit = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wCommit);
		wCommit.addModifyListener(lsMod);
		fdCommit = new FormData();
		fdCommit.left = new FormAttachment(middle, 0);
		fdCommit.top = new FormAttachment(wTable, margin);
		fdCommit.right = new FormAttachment(100, 0);
		wCommit.setLayoutData(fdCommit);
        
       
		wlKey=new Label(shell, SWT.NONE);
		wlKey.setText(BaseMessages.getString(PKG, "DeleteDialog.Key.Label")); //$NON-NLS-1$
 		props.setLook(wlKey);
		fdlKey=new FormData();
		fdlKey.left  = new FormAttachment(0, 0);
		fdlKey.top   = new FormAttachment(wCommit, margin);
		wlKey.setLayoutData(fdlKey);

		int nrKeyCols=4;
		int nrKeyRows=(input.getKeyStream()!=null?input.getKeyStream().length:1);
		
		ciKey=new ColumnInfo[nrKeyCols];
		ciKey[0]=new ColumnInfo(BaseMessages.getString(PKG, "DeleteDialog.ColumnInfo.TableField"),    ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciKey[1]=new ColumnInfo(BaseMessages.getString(PKG, "DeleteDialog.ColumnInfo.Comparator"),    ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "=", "<>", "<", "<=", ">", ">=", "LIKE", "BETWEEN", "IS NULL", "IS NOT NULL" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$
		ciKey[2]=new ColumnInfo(BaseMessages.getString(PKG, "DeleteDialog.ColumnInfo.StreamField1"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciKey[3]=new ColumnInfo(BaseMessages.getString(PKG, "DeleteDialog.ColumnInfo.StreamField2"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		tableFieldColumns.add(ciKey[0]);
		wKey=new TableView(transMeta, shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
						      ciKey, 
						      nrKeyRows,  
						      lsMod,
						      props
						      );

		wGet = new Button(shell, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "DeleteDialog.GetFields.Button")); //$NON-NLS-1$
		fdGet = new FormData();
		fdGet.right = new FormAttachment(100, 0);
		fdGet.top = new FormAttachment(wlKey, margin);
		wGet.setLayoutData(fdGet);

		fdKey = new FormData();
		fdKey.left = new FormAttachment(0, 0);
		fdKey.top = new FormAttachment(wlKey, margin);
		fdKey.right = new FormAttachment(wGet, -margin);
		fdKey.bottom = new FormAttachment(100, -30);
		wKey.setLayoutData(fdKey);

		
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
		
		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();       } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
        wSchema.addSelectionListener( lsDef );
        wTable.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		wbSchema.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					getSchemaNames();
				}
			}
		);
		wbTable.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					getTableName();
				}
			}
		);

		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		setTableFieldCombo();
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
        
        String[] fieldNames= (String[]) entries.toArray(new String[entries.size()]);
        Const.sortStrings(fieldNames);
        // Key fields
        ciKey[2].setComboValues(fieldNames);
        ciKey[3].setComboValues(fieldNames);
    }


	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		int i;
		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "DeleteDialog.Log.GettingKeyInfo")); //$NON-NLS-1$
		
		wCommit.setText(""+input.getCommitSize()); //$NON-NLS-1$
        
		if (input.getKeyStream()!=null)
		for (i=0;i<input.getKeyStream().length;i++)
		{
			TableItem item = wKey.table.getItem(i);
			if (input.getKeyLookup()[i]   !=null) item.setText(1, input.getKeyLookup()[i]);
			if (input.getKeyCondition()[i]!=null) item.setText(2, input.getKeyCondition()[i]);
			if (input.getKeyStream()[i]         !=null) item.setText(3, input.getKeyStream()[i]);
			if (input.getKeyStream2()[i]        !=null) item.setText(4, input.getKeyStream2()[i]);
		}
				
        if (input.getSchemaName()!=null) wSchema.setText( input.getSchemaName() );
		if (input.getTableName()!=null) wTable.setText( input.getTableName() );
		if (input.getDatabaseMeta()!=null)   wConnection.setText(input.getDatabaseMeta().getName());
		else if (transMeta.nrDatabases()==1)
		{
			wConnection.setText( transMeta.getDatabase(0).getName() );
		}

		wStepname.selectAll();
		wKey.setRowNums();
		wKey.optWidth(true);
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	private void setTableFieldCombo(){
		Runnable fieldLoader = new Runnable() {
			public void run() {
				//clear
				for (int i = 0; i < tableFieldColumns.size(); i++) {
					ColumnInfo colInfo = (ColumnInfo) tableFieldColumns.get(i);
					colInfo.setComboValues(new String[] {});
				}
				if (!Const.isEmpty(wTable.getText())) {
					DatabaseMeta ci = transMeta.findDatabase(wConnection.getText());
					if (ci != null) {
						Database db = new Database(loggingObject, ci);
						try {
							db.connect();

							String schemaTable = ci	.getQuotedSchemaTableCombination(transMeta.environmentSubstitute(wSchema
											.getText()), transMeta.environmentSubstitute(wTable.getText()));
							RowMetaInterface r = db.getTableFields(schemaTable);
							if (null != r) {
								String[] fieldNames = r.getFieldNames();
								if (null != fieldNames) {
									for (int i = 0; i < tableFieldColumns.size(); i++) {
										ColumnInfo colInfo = (ColumnInfo) tableFieldColumns.get(i);
										colInfo.setComboValues(fieldNames);
									}
								}
							}
						} catch (Exception e) {
							for (int i = 0; i < tableFieldColumns.size(); i++) {
								ColumnInfo colInfo = (ColumnInfo) tableFieldColumns	.get(i);
								colInfo.setComboValues(new String[] {});
							}
							// ignore any errors here. drop downs will not be
							// filled, but no problem for the user
						}
					}
				}
			}
		};
		shell.getDisplay().asyncExec(fieldLoader);
	}
	private void getInfo(DeleteMeta inf)
	{
		//Table ktable = wKey.table;
		int nrkeys = wKey.nrNonEmpty();
		
		inf.allocate(nrkeys);
				
		inf.setCommitSize( Const.toInt( wCommit.getText(), 0) );
		
		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "DeleteDialog.Log.FoundKeys",String.valueOf(nrkeys))); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0;i<nrkeys;i++)
		{
			TableItem item = wKey.getNonEmpty(i);
			inf.getKeyLookup()[i]    = item.getText(1);
			inf.getKeyCondition()[i] = item.getText(2);
			inf.getKeyStream()[i]          = item.getText(3);
			inf.getKeyStream2()[i]         = item.getText(4);
		}

        inf.setSchemaName( wSchema.getText() ); 
		inf.setTableName( wTable.getText() ); 
		inf.setDatabaseMeta( transMeta.findDatabase(wConnection.getText()) );
        
		stepname = wStepname.getText(); // return value
	}


	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		// Get the information for the dialog into the input structure.
		getInfo(input);
		
		if (input.getDatabaseMeta()==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "DeleteDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "DeleteDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
			return;
		}
		
		dispose();
	}

	private void getSchemaNames()
	{
		DatabaseMeta databaseMeta = transMeta.findDatabase(wConnection.getText());
		if (databaseMeta!=null)
		{
			Database database = new Database(loggingObject, databaseMeta);
			try
			{
				database.connect();
				String schemas[] = database.getSchemas();
				
				if (null != schemas && schemas.length>0) {
					schemas=Const.sortStrings(schemas);	
					EnterSelectionDialog dialog = new EnterSelectionDialog(shell, schemas, 
							BaseMessages.getString(PKG,"DeleteDialog.AvailableSchemas.Title",wConnection.getText()), 
							BaseMessages.getString(PKG,"DeleteDialog.AvailableSchemas.Message",wConnection.getText()));
					String d=dialog.open();
					if (d!=null) 
					{
						wSchema.setText(Const.NVL(d.toString(), ""));
						setTableFieldCombo();
					}

				}else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
					mb.setMessage(BaseMessages.getString(PKG,"DeleteDialog.NoSchema.Error"));
					mb.setText(BaseMessages.getString(PKG,"DeleteDialog.GetSchemas.Error"));
					mb.open(); 
				}
			}
			catch(Exception e)
			{
				new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.Error.Title"), 
						BaseMessages.getString(PKG,"DeleteDialog.ErrorGettingSchemas"), e);
			}
			finally
			{
				if(database!=null) 
				{
					database.disconnect();
					database=null;
				}
			}
		}
	}
	
	private void getTableName()
	{
		DatabaseMeta inf=null;
		// New class: SelectTableDialog
		int connr = wConnection.getSelectionIndex();
		if (connr>=0) inf = transMeta.getDatabase(connr);
		
		if (inf!=null)
		{
			logDebug(BaseMessages.getString(PKG, "DeleteDialog.Log.LookingAtConnection")+inf.toString()); //$NON-NLS-1$
		
			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, transMeta.getDatabases());
      std.setSelectedSchemaAndTable(wSchema.getText(), wTable.getText());
			if (std.open())
			{
                wSchema.setText(Const.NVL(std.getSchemaName(), ""));
                wTable.setText(Const.NVL(std.getTableName(), ""));
                setTableFieldCombo();
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "DeleteDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "DeleteDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open(); 
		}
	}

	private void get()
	{
		try
		{
            RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null && !r.isEmpty())
			{
                TableItemInsertListener listener = new TableItemInsertListener()
                {
                    public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                    {
                        tableItem.setText(2, "=");
                        return true;
                    }
                };
                BaseStepDialog.getFieldsFromPrevious(r, wKey, 1, new int[] { 1, 3}, new int[] {}, -1, -1, listener);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "DeleteDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "DeleteDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
