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
 * Created on 2-jul-2003
 *
 */

package org.pentaho.di.ui.trans.steps.databaselookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.trans.steps.databaselookup.DatabaseLookupMeta;
import org.pentaho.di.trans.steps.databaselookup.Messages;


public class DatabaseLookupDialog extends BaseStepDialog implements StepDialogInterface
{
	private CCombo       wConnection;

	private Label        wlCache;
	private Button       wCache;
	private FormData     fdlCache, fdCache;

	private Label        wlCachesize;
	private Text         wCachesize;
	private FormData     fdlCachesize, fdCachesize;

	private Label        wlKey;
	private TableView    wKey;
	private FormData     fdlKey, fdKey;

    private Label        wlSchema;
    private TextVar      wSchema;
    private FormData     fdlSchema, fdSchema;

	private Label        wlTable;
	private Button       wbTable;
	private TextVar      wTable;
	private FormData     fdlTable, fdbTable, fdTable;

	private Label        wlReturn;
	private TableView    wReturn;
	private FormData     fdlReturn, fdReturn;

	private Label        wlOrderBy;
	private Text         wOrderBy;
	private FormData     fdlOrderBy, fdOrderBy;

    private Label        wlFailMultiple;
    private Button       wFailMultiple;
    private FormData     fdlFailMultiple, fdFailMultiple;

    private Label        wlEatRows;
    private Button       wEatRows;
    private FormData     fdlEatRows, fdEatRows;

	private Button wGet, wGetLU;
	private Listener lsGet, lsGetLU;

	private DatabaseLookupMeta input;

	/**
	 * List of ColumnInfo that should have the field names of the selected database table
	 */
	private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();
	
	/**
	 * List of ColumnInfo that should have the previous fields combo box
	 */
	private List<ColumnInfo> fieldColumns = new ArrayList<ColumnInfo>();
	
	/**
	 * all fields from the previous steps
	 */
	private RowMetaInterface prevFields = null;
	
	public DatabaseLookupDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(DatabaseLookupMeta)in;
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
		
		FocusListener lsFocusLost = new FocusAdapter() {
			public void focusLost(FocusEvent arg0) {
				setTableFieldCombo();
			}
		};
		backupChanged = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("DatabaseLookupDialog.shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("DatabaseLookupDialog.Stepname.Label")); //$NON-NLS-1$
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

        // Schema line...
        wlSchema=new Label(shell, SWT.RIGHT);
        wlSchema.setText(Messages.getString("DatabaseLookupDialog.TargetSchema.Label")); //$NON-NLS-1$
        props.setLook(wlSchema);
        fdlSchema=new FormData();
        fdlSchema.left = new FormAttachment(0, 0);
        fdlSchema.right= new FormAttachment(middle, -margin);
        fdlSchema.top  = new FormAttachment(wConnection, margin*2);
        wlSchema.setLayoutData(fdlSchema);

        wSchema=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wSchema);
        wSchema.addModifyListener(lsMod);
        wSchema.addFocusListener(lsFocusLost);
        fdSchema=new FormData();
        fdSchema.left = new FormAttachment(middle, 0);
        fdSchema.top  = new FormAttachment(wConnection, margin*2);
        fdSchema.right= new FormAttachment(100, 0);
        wSchema.setLayoutData(fdSchema);

		// Table line...
		wlTable=new Label(shell, SWT.RIGHT);
		wlTable.setText(Messages.getString("DatabaseLookupDialog.Lookuptable.Label")); //$NON-NLS-1$
 		props.setLook(wlTable);
		fdlTable=new FormData();
		fdlTable.left = new FormAttachment(0, 0);
		fdlTable.right= new FormAttachment(middle, -margin);
		fdlTable.top  = new FormAttachment(wSchema, margin*2);
		wlTable.setLayoutData(fdlTable);

		wbTable=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText(Messages.getString("DatabaseLookupDialog.Browse.Button")); //$NON-NLS-1$
		fdbTable=new FormData();
		fdbTable.right= new FormAttachment(100, 0);
		fdbTable.top  = new FormAttachment(wSchema, margin);
		wbTable.setLayoutData(fdbTable);

		wTable=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTable);
		wTable.addModifyListener(lsMod);
		wTable.addFocusListener(lsFocusLost);
		fdTable=new FormData();
		fdTable.left = new FormAttachment(middle, 0);
		fdTable.top  = new FormAttachment(wSchema, margin*2);
		fdTable.right= new FormAttachment(wbTable, -margin);
		wTable.setLayoutData(fdTable);

		// Cache?
		wlCache=new Label(shell, SWT.RIGHT);
		wlCache.setText(Messages.getString("DatabaseLookupDialog.Cache.Label")); //$NON-NLS-1$
 		props.setLook(wlCache);
		fdlCache=new FormData();
		fdlCache.left = new FormAttachment(0, 0);
		fdlCache.right= new FormAttachment(middle, -margin);
		fdlCache.top  = new FormAttachment(wbTable, margin);
		wlCache.setLayoutData(fdlCache);
		wCache=new Button(shell, SWT.CHECK);
 		props.setLook(wCache);
		fdCache=new FormData();
		fdCache.left = new FormAttachment(middle, 0);
		fdCache.top  = new FormAttachment(wbTable, margin);
		wCache.setLayoutData(fdCache);
		wCache.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
					
					wCachesize.setEnabled(wCache.getSelection());
					wlCachesize.setEnabled(wCache.getSelection());
				}
			}
		);

		// Cache size line
		wlCachesize=new Label(shell, SWT.RIGHT);
		wlCachesize.setText(Messages.getString("DatabaseLookupDialog.Cachesize.Label")); //$NON-NLS-1$
 		props.setLook(wlCachesize);
		wlCachesize.setEnabled(input.isCached());
		fdlCachesize=new FormData();
		fdlCachesize.left   = new FormAttachment(0, 0);
		fdlCachesize.right  = new FormAttachment(middle, -margin);
		fdlCachesize.top    = new FormAttachment(wlCache, margin);
		wlCachesize.setLayoutData(fdlCachesize);
		wCachesize=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wCachesize);
		wCachesize.setEnabled(input.isCached());
		wCachesize.addModifyListener(lsMod);
		fdCachesize=new FormData();
		fdCachesize.left   = new FormAttachment(middle, 0);
		fdCachesize.right  = new FormAttachment(100, 0);
		fdCachesize.top    = new FormAttachment(wlCache, margin);
		wCachesize.setLayoutData(fdCachesize);


		wlKey=new Label(shell, SWT.NONE);
		wlKey.setText(Messages.getString("DatabaseLookupDialog.Keys.Label")); //$NON-NLS-1$
 		props.setLook(wlKey);
		fdlKey=new FormData();
		fdlKey.left  = new FormAttachment(0, 0);
		fdlKey.top   = new FormAttachment(wCachesize, margin);
		wlKey.setLayoutData(fdlKey);

		int nrKeyCols=4;
		int nrKeyRows=(input.getStreamKeyField1()!=null?input.getStreamKeyField1().length:1);

		ColumnInfo[] ciKey=new ColumnInfo[nrKeyCols];
		ciKey[0]=new ColumnInfo(Messages.getString("DatabaseLookupDialog.ColumnInfo.Tablefield"),   ColumnInfo.COLUMN_TYPE_CCOMBO, new String[]{""},  false); //$NON-NLS-1$
		ciKey[1]=new ColumnInfo(Messages.getString("DatabaseLookupDialog.ColumnInfo.Comparator"),   ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "=", "<>", "<", "<=", ">", ">=", "LIKE", "BETWEEN", "IS NULL", "IS NOT NULL" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$
		ciKey[2]=new ColumnInfo(Messages.getString("DatabaseLookupDialog.ColumnInfo.Field1"),       ColumnInfo.COLUMN_TYPE_CCOMBO, new String[]{""},   false); //$NON-NLS-1$
		ciKey[3]=new ColumnInfo(Messages.getString("DatabaseLookupDialog.ColumnInfo.Field2"),       ColumnInfo.COLUMN_TYPE_CCOMBO, new String[]{""},   false); //$NON-NLS-1$
		tableFieldColumns.add(ciKey[0]);
		fieldColumns.add(ciKey[2]);
		fieldColumns.add(ciKey[3]);
		wKey=new TableView(transMeta, shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
						      ciKey, 
						      nrKeyRows,  
						      lsMod,
							  props
						      );

		fdKey=new FormData();
		fdKey.left  = new FormAttachment(0, 0);
		fdKey.top   = new FormAttachment(wlKey, margin);
		fdKey.right = new FormAttachment(100, 0);
		fdKey.bottom= new FormAttachment(wlKey, 150);
		wKey.setLayoutData(fdKey);

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(Messages.getString("DatabaseLookupDialog.GetFields.Button")); //$NON-NLS-1$
		wGetLU=new Button(shell, SWT.PUSH);
		wGetLU.setText(Messages.getString("DatabaseLookupDialog.GetLookupFields.Button")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel , wGet, wGetLU}, margin, null);

		// OderBy line
		wlOrderBy=new Label(shell, SWT.RIGHT);
		wlOrderBy.setText(Messages.getString("DatabaseLookupDialog.Orderby.Label")); //$NON-NLS-1$
 		props.setLook(wlOrderBy);
		fdlOrderBy=new FormData();
		fdlOrderBy.left   = new FormAttachment(0, 0);
		fdlOrderBy.right  = new FormAttachment(middle, -margin);
		fdlOrderBy.bottom = new FormAttachment(wOK, -2*margin);
		wlOrderBy.setLayoutData(fdlOrderBy);
		wOrderBy=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wOrderBy);
		fdOrderBy=new FormData();
		fdOrderBy.left   = new FormAttachment(middle, 0);
		fdOrderBy.bottom = new FormAttachment(wOK, -2*margin);
		fdOrderBy.right  = new FormAttachment(100, 0);
		wOrderBy.setLayoutData(fdOrderBy);

        // FailMultiple?
        wlFailMultiple=new Label(shell, SWT.RIGHT);
        wlFailMultiple.setText(Messages.getString("DatabaseLookupDialog.FailMultiple.Label")); //$NON-NLS-1$
        props.setLook(wlFailMultiple);
        fdlFailMultiple=new FormData();
        fdlFailMultiple.left   = new FormAttachment(0, 0);
        fdlFailMultiple.right  = new FormAttachment(middle, -margin);
        fdlFailMultiple.bottom = new FormAttachment(wOrderBy, -margin);
        wlFailMultiple.setLayoutData(fdlFailMultiple);
        wFailMultiple=new Button(shell, SWT.CHECK);
        props.setLook(wFailMultiple);
        fdFailMultiple=new FormData();
        fdFailMultiple.left   = new FormAttachment(middle, 0);
        fdFailMultiple.bottom = new FormAttachment(wOrderBy, -margin);
        wFailMultiple.setLayoutData(fdFailMultiple);
        wFailMultiple.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    setFlags();
                }
            }
        );

        // EatRows?
        wlEatRows=new Label(shell, SWT.RIGHT);
        wlEatRows.setText(Messages.getString("DatabaseLookupDialog.EatRows.Label")); //$NON-NLS-1$
        props.setLook(wlEatRows);
        fdlEatRows=new FormData();
        fdlEatRows.left   = new FormAttachment(0, 0);
        fdlEatRows.right  = new FormAttachment(middle, -margin);
        fdlEatRows.bottom = new FormAttachment(wFailMultiple, -margin);
        wlEatRows.setLayoutData(fdlEatRows);
        wEatRows=new Button(shell, SWT.CHECK);
        props.setLook(wEatRows);
        fdEatRows=new FormData();
        fdEatRows.left   = new FormAttachment(middle, 0);
        fdEatRows.bottom = new FormAttachment(wFailMultiple, -margin);
        wEatRows.setLayoutData(fdEatRows);
        wEatRows.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    setFlags();
                }
            }
        );

        
        // THE UPDATE/INSERT TABLE
        wlReturn=new Label(shell, SWT.NONE);
        wlReturn.setText(Messages.getString("DatabaseLookupDialog.Return.Label")); //$NON-NLS-1$
        props.setLook(wlReturn);
        fdlReturn=new FormData();
        fdlReturn.left  = new FormAttachment(0, 0);
        fdlReturn.top   = new FormAttachment(wKey, margin);
        wlReturn.setLayoutData(fdlReturn);
        
        int UpInsCols=4;
        int UpInsRows= (input.getReturnValueField()!=null?input.getReturnValueField().length:1);
        
        ColumnInfo[] ciReturn=new ColumnInfo[UpInsCols];
        ciReturn[0]=new ColumnInfo(Messages.getString("DatabaseLookupDialog.ColumnInfo.Field"),    ColumnInfo.COLUMN_TYPE_CCOMBO,new String[]{},  false); //$NON-NLS-1$
        ciReturn[1]=new ColumnInfo(Messages.getString("DatabaseLookupDialog.ColumnInfo.Newname"), ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
        ciReturn[2]=new ColumnInfo(Messages.getString("DatabaseLookupDialog.ColumnInfo.Default"),  ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
        ciReturn[3]=new ColumnInfo(Messages.getString("DatabaseLookupDialog.ColumnInfo.Type"),     ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes()); //$NON-NLS-1$
        tableFieldColumns.add(ciReturn[0]);
        
        wReturn=new TableView(transMeta, shell, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
                              ciReturn, 
                              UpInsRows,  
                              lsMod,
                              props
                              );

        fdReturn=new FormData();
        fdReturn.left  = new FormAttachment(0, 0);
        fdReturn.top   = new FormAttachment(wlReturn, margin);
        fdReturn.right = new FormAttachment(100, 0);
        fdReturn.bottom= new FormAttachment(wEatRows, -margin);
        wReturn.setLayoutData(fdReturn);


        
		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();       } };
		lsGetLU    = new Listener() { public void handleEvent(Event e) { getlookup(); } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wGetLU.addListener (SWT.Selection, lsGetLU );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wTable.addSelectionListener( lsDef );
		wOrderBy.addSelectionListener( lsDef );
		wCachesize.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

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
		input.setChanged(backupChanged);

		setComboValues();
		setTableFieldCombo();
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	private void setComboValues() {
		Runnable fieldLoader = new Runnable() {
			public void run() {
				try {
					prevFields = transMeta.getPrevStepFields(stepname);
				} catch (KettleException e) {
					prevFields = new RowMeta();
					String msg = Messages.getString("DatabaseLookupDialog.DoMapping.UnableToFindInput");
					log.logError(toString(), msg);
				}
				String[] prevStepFieldNames = prevFields.getFieldNames();
				Arrays.sort(prevStepFieldNames);
				for (int i = 0; i < fieldColumns.size(); i++) {
					ColumnInfo colInfo = (ColumnInfo) fieldColumns.get(i);
					colInfo.setComboValues(prevStepFieldNames);
				}
			}
		};
		new Thread(fieldLoader).start();
	}
	
	private void setTableFieldCombo(){
		Runnable fieldLoader = new Runnable() {
			public void run() {
				if (!Const.isEmpty(wTable.getText())) {
					DatabaseMeta ci = transMeta.findDatabase(wConnection.getText());
					if (ci != null) {
						Database db = new Database(ci);
						db.shareVariablesWith(transMeta);
						try {
							db.connect();

							String schemaTable = ci
									.getQuotedSchemaTableCombination(wSchema
											.getText(), wTable.getText());
                            RowMetaInterface r = db.getTableFields(schemaTable);
							if (null != r) {
								String[] fieldNames = r.getFieldNames();
								if (null != fieldNames) {
									for (int i = 0; i < tableFieldColumns.size(); i++) 
                                    {
										ColumnInfo colInfo = (ColumnInfo) tableFieldColumns.get(i);
										colInfo.setComboValues(fieldNames);
									}
								}
							}
						} 
                        catch (Exception e) 
                        {
							for (int i = 0; i < tableFieldColumns.size(); i++) 
                            {
								ColumnInfo colInfo = (ColumnInfo) tableFieldColumns.get(i);
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
	
	private void setFlags()
    {
        wlOrderBy.setEnabled( !wFailMultiple.getSelection() );
        wOrderBy.setEnabled( !wFailMultiple.getSelection() );
    }

    /**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		int i;
		log.logDebug(toString(), Messages.getString("DatabaseLookupDialog.Log.GettingKeyInfo")); //$NON-NLS-1$
		
		wCache.setSelection(input.isCached());
		wCachesize.setText(""+input.getCacheSize()); //$NON-NLS-1$
		
		if (input.getStreamKeyField1()!=null)
		for (i=0;i<input.getStreamKeyField1().length;i++)
		{
			TableItem item = wKey.table.getItem(i);
			if (input.getTableKeyField()[i]   !=null) item.setText(1, input.getTableKeyField()[i]);
			if (input.getKeyCondition()[i]!=null) item.setText(2, input.getKeyCondition()[i]);
			if (input.getStreamKeyField1()[i]         !=null) item.setText(3, input.getStreamKeyField1()[i]);
			if (input.getStreamKeyField2()[i]        !=null) item.setText(4, input.getStreamKeyField2()[i]);
		}
		
		if (input.getReturnValueField()!=null)
		for (i=0;i<input.getReturnValueField().length;i++)
		{
			TableItem item = wReturn.table.getItem(i);
			if (input.getReturnValueField()[i]!=null     ) item.setText(1, input.getReturnValueField()[i]);
			if (input.getReturnValueNewName()[i]!=null && !input.getReturnValueNewName()[i].equals(input.getReturnValueField()[i]))
				item.setText(2, input.getReturnValueNewName()[i]);
			
			if (input.getReturnValueDefault()[i]!=null  ) item.setText(3, input.getReturnValueDefault()[i]);
			item.setText(4, ValueMeta.getTypeDesc(input.getReturnValueDefaultType()[i]));
		}
		
        if (input.getSchemaName()!=null) wSchema.setText( input.getSchemaName() );
		if (input.getTablename()!=null) wTable.setText( input.getTablename() );
		if (input.getDatabaseMeta()!=null)   wConnection.setText(input.getDatabaseMeta().getName());
		else if (transMeta.nrDatabases()==1)
		{
			wConnection.setText( transMeta.getDatabase(0).getName() );
		}
		if (input.getOrderByClause()!=null)      wOrderBy.setText(input.getOrderByClause());
		wFailMultiple.setSelection(input.isFailingOnMultipleResults());
		wEatRows.setSelection(input.isEatingRowOnLookupFailure());
        
		wStepname.selectAll();
		wKey.setRowNums();
		wKey.optWidth(true);
		wReturn.setRowNums();
		wReturn.optWidth(true);
        
        setFlags();
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

		int nrkeys = wKey.nrNonEmpty();
		int nrfields = wReturn.nrNonEmpty();
		
		input.allocate(nrkeys, nrfields);
		
		input.setCached( wCache.getSelection() );
		input.setCacheSize( Const.toInt(wCachesize.getText(), 0) );
		
		log.logDebug(toString(), Messages.getString("DatabaseLookupDialog.Log.FoundKeys",String.valueOf(nrkeys))); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0;i<nrkeys;i++)
		{
			TableItem item = wKey.getNonEmpty(i);
			input.getTableKeyField()[i]    = item.getText(1);
			input.getKeyCondition()[i]     = item.getText(2);
			input.getStreamKeyField1()[i]  = item.getText(3);
			input.getStreamKeyField2()[i]  = item.getText(4);
		}

		log.logDebug(toString(), Messages.getString("DatabaseLookupDialog.Log.FoundFields",String.valueOf(nrfields))); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0;i<nrfields;i++)
		{
			TableItem item        = wReturn.getNonEmpty(i);
			input.getReturnValueField()[i]        = item.getText(1);
			input.getReturnValueNewName()[i]    = item.getText(2);
			if (input.getReturnValueNewName()[i]==null || input.getReturnValueNewName()[i].length()==0)
				input.getReturnValueNewName()[i] = input.getReturnValueField()[i];
			
			input.getReturnValueDefault()[i]     = item.getText(3);
			input.getReturnValueDefaultType()[i] = ValueMeta.getType(item.getText(4));
			
			if (input.getReturnValueDefaultType()[i]<0)
			{
				input.getReturnValueDefaultType()[i]=ValueMetaInterface.TYPE_STRING;
			}
		}
		
        input.setSchemaName( wSchema.getText() ); 
		input.setTablename( wTable.getText() ); 
		input.setDatabaseMeta( transMeta.findDatabase(wConnection.getText()) );
		input.setOrderByClause( wOrderBy.getText() );
		input.setFailingOnMultipleResults( wFailMultiple.getSelection() );
		input.setEatingRowOnLookupFailure( wEatRows.getSelection() );
        
		stepname = wStepname.getText(); // return value

		if (transMeta.findDatabase(wConnection.getText())==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("DatabaseLookupDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("DatabaseLookupDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}
		
		dispose();
	}

	private void getTableName()
	{
		DatabaseMeta inf=null;
		// New class: SelectTableDialog
		int connr = wConnection.getSelectionIndex();
		if (connr>=0) inf = transMeta.getDatabase(connr);
		
		if (inf!=null)
		{
			log.logDebug(toString(), Messages.getString("DatabaseLookupDialog.Log.LookingAtConnection")+inf.toString()); //$NON-NLS-1$
		
			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, transMeta.getDatabases());
            std.setSplitSchemaAndTable(true);
            std.setSelectedSchema(wSchema.getText());
			std.setSelectedTable(wTable.getText());
			if (std.open() != null)
			{
                wSchema.setText(Const.NVL(std.getSchemaName(), ""));
				wTable.setText(Const.NVL(std.getTableName(), ""));
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("DatabaseLookupDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("DatabaseLookupDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open(); 
		}
	}

	private void get()
	{
		try
		{
            RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
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
			new ErrorDialog(shell, Messages.getString("DatabaseLookupDialog.GetFieldsFailed.DialogTitle"), Messages.getString("DatabaseLookupDialog.GetFieldsFailed.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
	
	private void getlookup()
	{
		DatabaseMeta ci = transMeta.findDatabase(wConnection.getText());
		if (ci!=null)
		{
			Database db = new Database(ci);
			db.shareVariablesWith(transMeta);
			try
			{
				db.connect();
				
				if (!Const.isEmpty( wTable.getText()) )
				{
                    String schemaTable = ci.getQuotedSchemaTableCombination(wSchema.getText(), wTable.getText());
                    RowMetaInterface r = db.getTableFields(schemaTable);
					if (r!=null)
					{
                        log.logDebug(toString(), Messages.getString("DatabaseLookupDialog.Log.FoundTableFields")+schemaTable+" --> "+r.toStringMeta()); //$NON-NLS-1$ //$NON-NLS-2$
                        BaseStepDialog.getFieldsFromPrevious(r, wReturn, 1, new int[] { 1, 2}, new int[] { 4 }, -1, -1, null);
					}
					else
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setMessage(Messages.getString("DatabaseLookupDialog.CouldNotReadTableInfo.DialogMessage")); //$NON-NLS-1$
						mb.setText(Messages.getString("DatabaseLookupDialog.CouldNotReadTableInfo.DialogTitle")); //$NON-NLS-1$
						mb.open(); 
					}
				}
			}
			catch(KettleException e)
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage(Messages.getString("DatabaseLookupDialog.ErrorOccurred.DialogMessage")+Const.CR+e.getMessage()); //$NON-NLS-1$
				mb.setText(Messages.getString("DatabaseLookupDialog.ErrorOccurred.DialogTitle")); //$NON-NLS-1$
				mb.open(); 
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("DatabaseLookupDialog.InvalidConnectionName.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("DatabaseLookupDialog.InvalidConnectionName.DialogTitle")); //$NON-NLS-1$
			mb.open(); 
		}
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
}
