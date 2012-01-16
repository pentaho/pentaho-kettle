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

package org.pentaho.di.ui.trans.steps.monetdbbulkloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.SourceToTargetMapping;
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
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.monetdbbulkloader.MonetDBBulkLoaderMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;


/**
 * Dialog class for the MonetDB bulk loader step.
 * 
 */
public class MonetDBBulkLoaderDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = MonetDBBulkLoaderMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CCombo				wConnection;

    private Label               wlSchema;
    private TextVar             wSchema;
    private FormData            fdlSchema, fdSchema;

	private Label				wlTable;
	private Button				wbTable;
	private TextVar				wTable;
	private FormData			fdlTable, fdbTable, fdTable;

	private Label				wlMClientPath;
	private Button				wbMClientPath;
	private TextVar				wMClientPath;
	private FormData			fdlMclientPath, fdbMclientPath, fdMclientPath;
	
	private Label				wlBufferSize;
	private TextVar				wBufferSize;
	private FormData			fdlBufferSize, fdBufferSize;

	private Label				wlLogFile;
	private Button				wbLogFile;
	private TextVar				wLogFile;
	private FormData			fdlLogFile, fdbLogFile, fdLogFile;

    private Label               wlEncoding;
    private Combo               wEncoding;
    private FormData            fdlEncoding, fdEncoding;

    private Label               wlReturn;
    private TableView           wReturn;
    private FormData            fdlReturn, fdReturn;

	private Button				wGetLU;
	private FormData			fdGetLU;
	private Listener			lsGetLU;
	
	private Button     wDoMapping;
	private FormData   fdDoMapping;

	private MonetDBBulkLoaderMeta	input;
	
	private ColumnInfo[] ciReturn ;
	
    private Map<String, Integer> inputFields;
	
	/**
	 * List of ColumnInfo that should have the field names of the selected database table
	 */
	private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();
	
    // These should not be translated, they are required to exist on all
    // platforms according to the documentation of "Charset".
    private static String[] encodings = { "",                //$NON-NLS-1$
    	                                  "US-ASCII",        //$NON-NLS-1$
    	                                  "ISO-8859-1",      //$NON-NLS-1$
    	                                  "UTF-8",           //$NON-NLS-1$
    	                                  "UTF-16BE",        //$NON-NLS-1$
    	                                  "UTF-16LE",        //$NON-NLS-1$
    	                                  "UTF-16" };        //$NON-NLS-1$

    private static final String[] ALL_FILETYPES = new String[] {
        	BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.Filetype.All") };


	public MonetDBBulkLoaderDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input = (MonetDBBulkLoaderMeta) in;
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
		FocusListener lsFocusLost = new FocusAdapter() {
			public void focusLost(FocusEvent arg0) {
				setTableFieldCombo();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.Shell.Title")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.Stepname.Label")); //$NON-NLS-1$
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
		if (input.getDatabaseMeta()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);

        // Schema line...
        wlSchema=new Label(shell, SWT.RIGHT);
        wlSchema.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.TargetSchema.Label")); //$NON-NLS-1$
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
		wlTable = new Label(shell, SWT.RIGHT);
		wlTable.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.TargetTable.Label")); //$NON-NLS-1$
 		props.setLook(wlTable);
		fdlTable = new FormData();
		fdlTable.left = new FormAttachment(0, 0);
		fdlTable.right = new FormAttachment(middle, -margin);
		fdlTable.top = new FormAttachment(wSchema, margin);
		wlTable.setLayoutData(fdlTable);
		
		wbTable = new Button(shell, SWT.PUSH | SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.Browse.Button")); //$NON-NLS-1$
		fdbTable = new FormData();
		fdbTable.right = new FormAttachment(100, 0);
		fdbTable.top = new FormAttachment(wSchema, margin);
		wbTable.setLayoutData(fdbTable);
		wTable = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTable);
		wTable.addModifyListener(lsMod);
		wTable.addFocusListener(lsFocusLost);
		fdTable = new FormData();
		fdTable.left = new FormAttachment(middle, 0);
		fdTable.top = new FormAttachment(wSchema, margin);
		fdTable.right = new FormAttachment(wbTable, -margin);
		wTable.setLayoutData(fdTable);

		// PsqlPath line...
		wlMClientPath = new Label(shell, SWT.RIGHT);
		wlMClientPath.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.MClientPath.Label")); //$NON-NLS-1$
 		props.setLook(wlMClientPath);
		fdlMclientPath = new FormData();
		fdlMclientPath.left = new FormAttachment(0, 0);
		fdlMclientPath.right = new FormAttachment(middle, -margin);
		fdlMclientPath.top = new FormAttachment(wTable, margin);
		wlMClientPath.setLayoutData(fdlMclientPath);
		
		wbMClientPath = new Button(shell, SWT.PUSH | SWT.CENTER);
 		props.setLook(wbMClientPath);
		wbMClientPath.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.Browse.Button")); //$NON-NLS-1$
		fdbMclientPath = new FormData();
		fdbMclientPath.right = new FormAttachment(100, 0);
		fdbMclientPath.top = new FormAttachment(wTable, margin);
		wbMClientPath.setLayoutData(fdbMclientPath);
		wMClientPath = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMClientPath);
		wMClientPath.addModifyListener(lsMod);
		fdMclientPath = new FormData();
		fdMclientPath.left = new FormAttachment(middle, 0);
		fdMclientPath.top = new FormAttachment(wTable, margin);
		fdMclientPath.right = new FormAttachment(wbMClientPath, -margin);
		wMClientPath.setLayoutData(fdMclientPath);
				

		// Buffer size file line
		wlBufferSize = new Label(shell, SWT.RIGHT);
		wlBufferSize.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.BufferSize.Label")); //$NON-NLS-1$
 		props.setLook(wlBufferSize);
		fdlBufferSize = new FormData();
		fdlBufferSize.left = new FormAttachment(0, 0);
		fdlBufferSize.top = new FormAttachment(wMClientPath, margin);
		fdlBufferSize.right = new FormAttachment(middle, -margin);
		wlBufferSize.setLayoutData(fdlBufferSize);
		wBufferSize = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wBufferSize);
		wBufferSize.addModifyListener(lsMod);
		fdBufferSize = new FormData();
		fdBufferSize.left = new FormAttachment(middle, 0);
		fdBufferSize.top = new FormAttachment(wMClientPath, margin);
		fdBufferSize.right = new FormAttachment(100, 0);
		wBufferSize.setLayoutData(fdBufferSize);						

		// Log file line
		wlLogFile = new Label(shell, SWT.RIGHT);
		wlLogFile.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.LogFile.Label")); //$NON-NLS-1$
 		props.setLook(wlLogFile);
		fdlLogFile = new FormData();
		fdlLogFile.left = new FormAttachment(0, 0);
		fdlLogFile.top = new FormAttachment(wBufferSize, margin);
		fdlLogFile.right = new FormAttachment(middle, -margin);
		wlLogFile.setLayoutData(fdlLogFile);
		wbLogFile = new Button(shell, SWT.PUSH | SWT.CENTER);
 		props.setLook(wbLogFile);
		wbLogFile.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.Browse.Button")); //$NON-NLS-1$
		fdbLogFile = new FormData();
		fdbLogFile.right = new FormAttachment(100, 0);
		fdbLogFile.top = new FormAttachment(wBufferSize, margin);
		wbLogFile.setLayoutData(fdbLogFile);
		wLogFile = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLogFile);
		wLogFile.addModifyListener(lsMod);
		fdLogFile = new FormData();
		fdLogFile.left = new FormAttachment(middle, 0);
		fdLogFile.top = new FormAttachment(wBufferSize, margin);
		fdLogFile.right = new FormAttachment(wbLogFile, -margin);
		wLogFile.setLayoutData(fdLogFile);		

		//
        // Control encoding line
        //
        // The drop down is editable as it may happen an encoding may not be present
        // on one machine, but you may want to use it on your execution server
        //
        wlEncoding=new Label(shell, SWT.RIGHT);
        wlEncoding.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.Encoding.Label"));
        props.setLook(wlEncoding);
        fdlEncoding=new FormData();
        fdlEncoding.left  = new FormAttachment(0, 0);
        fdlEncoding.top   = new FormAttachment(wLogFile, 3*margin);
        fdlEncoding.right = new FormAttachment(middle, -margin);
        wlEncoding.setLayoutData(fdlEncoding);
        wEncoding=new Combo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wEncoding.setToolTipText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.Encoding.Tooltip"));
        wEncoding.setItems(encodings);
        props.setLook(wEncoding);
        fdEncoding=new FormData();
        fdEncoding.left = new FormAttachment(middle, 0);
        fdEncoding.top  = new FormAttachment(wlLogFile, 3*margin);
        fdEncoding.right= new FormAttachment(100, 0);        
        wEncoding.setLayoutData(fdEncoding);
        wEncoding.addModifyListener(lsMod);

		// THE BUTTONS
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wSQL = new Button(shell, SWT.PUSH);
		wSQL.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.SQL.Button")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel , wSQL }, margin, null);

		// The field Table
		wlReturn = new Label(shell, SWT.NONE);
		wlReturn.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.Fields.Label")); //$NON-NLS-1$
 		props.setLook(wlReturn);
		fdlReturn = new FormData();
		fdlReturn.left = new FormAttachment(0, 0);
		fdlReturn.top = new FormAttachment(wEncoding, margin);
		wlReturn.setLayoutData(fdlReturn);

		int UpInsCols = 3;
		int UpInsRows = (input.getFieldTable() != null ? input.getFieldTable().length : 1);

		ciReturn = new ColumnInfo[UpInsCols];
		ciReturn[0] = new ColumnInfo(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.ColumnInfo.TableField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciReturn[1] = new ColumnInfo(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.ColumnInfo.StreamField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciReturn[2] = new ColumnInfo(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.ColumnInfo.FormatOK"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {"Y","N",}, true); // $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
		tableFieldColumns.add(ciReturn[0]);
		wReturn = new TableView(transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
				ciReturn, UpInsRows, lsMod, props);

		wGetLU = new Button(shell, SWT.PUSH);
		wGetLU.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.GetFields.Label")); //$NON-NLS-1$
		fdGetLU = new FormData();
		fdGetLU.top   = new FormAttachment(wlReturn, margin);
		fdGetLU.right = new FormAttachment(100, 0);
		wGetLU.setLayoutData(fdGetLU);

		wDoMapping = new Button(shell, SWT.PUSH);
		wDoMapping.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.EditMapping.Label")); //$NON-NLS-1$
		fdDoMapping = new FormData();
		fdDoMapping.top   = new FormAttachment(wGetLU, margin);
		fdDoMapping.right = new FormAttachment(100, 0);
		wDoMapping.setLayoutData(fdDoMapping);

		wDoMapping.addListener(SWT.Selection, new Listener() { 	public void handleEvent(Event arg0) { generateMappings();}});

		fdReturn = new FormData();
		fdReturn.left = new FormAttachment(0, 0);
		fdReturn.top = new FormAttachment(wlReturn, margin);
		fdReturn.right = new FormAttachment(wGetLU, -margin);
		fdReturn.bottom = new FormAttachment(wOK, -2*margin);
		wReturn.setLayoutData(fdReturn);
		
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
		

		wbMClientPath.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterExtensions(new String[] { "*" });
                if (wMClientPath.getText() != null)
                {
                    dialog.setFileName(wMClientPath.getText());
                }
                dialog.setFilterNames(ALL_FILETYPES);
                if (dialog.open() != null)
                {
                	wMClientPath.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
                                      + dialog.getFileName());
                }
            }
        });
			
		wbLogFile.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterExtensions(new String[] { "*" });
                if (wLogFile.getText() != null)
                {
                    dialog.setFileName(wLogFile.getText());
                }                
                dialog.setFilterNames(ALL_FILETYPES);
                if (dialog.open() != null)
                {
                	wLogFile.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
                                      + dialog.getFileName());
                }
            }
        });
		

		// Add listeners
		lsOK = new Listener()
		{
			public void handleEvent(Event e)
			{
				ok();
			}
		};
		lsGetLU = new Listener()
		{
			public void handleEvent(Event e)
			{
				getUpdate();
			}
		};
		lsSQL = new Listener()
		{
			public void handleEvent(Event e)
			{
				create();
			}
		};
		lsCancel = new Listener()
		{
			public void handleEvent(Event e)
			{
				cancel();
			}
		};

		wOK.addListener(SWT.Selection, lsOK);
		wGetLU.addListener(SWT.Selection, lsGetLU);
		wSQL.addListener(SWT.Selection, lsSQL);
		wCancel.addListener(SWT.Selection, lsCancel);

		lsDef = new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wStepname.addSelectionListener(lsDef);
        wSchema.addSelectionListener(lsDef);
        wTable.addSelectionListener(lsDef);
        wBufferSize.addSelectionListener(lsDef);
        wLogFile.addSelectionListener(lsDef);


		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter()
		{
			public void shellClosed(ShellEvent e)
			{
				cancel();
			}
		});


		wbTable.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				getTableName();
			}
		});

		// Set the shell size, based upon previous time...
		setSize();

		getData();
		setTableFieldCombo();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
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
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		int i;
		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.Log.GettingKeyInfo")); //$NON-NLS-1$

		wBufferSize.setText("" + input.getBufferSize());   //$NON-NLS-1$

		if (input.getFieldTable() != null) {
			for (i = 0; i < input.getFieldTable().length; i++)
			{
				TableItem item = wReturn.table.getItem(i);
				if (input.getFieldTable()[i] != null)
					item.setText(1, input.getFieldTable()[i]);
				if (input.getFieldStream()[i] != null)
					item.setText(2, input.getFieldStream()[i]);
				item.setText(3, input.getFieldFormatOk()[i]?"Y":"N");
			}
		}

		if (input.getDatabaseMeta() != null)
			wConnection.setText(input.getDatabaseMeta().getName());
		else
		{
			if (transMeta.nrDatabases() == 1)
			{
				wConnection.setText(transMeta.getDatabase(0).getName());
			}
		}
        if (input.getSchemaName() != null) wSchema.setText(input.getSchemaName());
		if (input.getTableName() != null) wTable.setText(input.getTableName());
		if (input.getMClientPath() != null) wMClientPath.setText(input.getMClientPath());
		if (input.getLogFile() != null) wLogFile.setText(input.getLogFile());
		if (input.getEncoding() != null) wEncoding.setText(input.getEncoding());
		
		wStepname.selectAll();
		wReturn.setRowNums();
		wReturn.optWidth(true);
	}
	
	private void cancel()
	{
		stepname = null;
		input.setChanged(changed);
		dispose();
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
        // return fields
        ciReturn[1].setComboValues(fieldNames);
    }
	private void getInfo(MonetDBBulkLoaderMeta inf)
	{
		int nrfields = wReturn.nrNonEmpty();

		inf.allocate(nrfields);

		inf.setBufferSize( wBufferSize.getText() );

		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.Log.FoundFields", "" + nrfields)); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < nrfields; i++)
		{
			TableItem item = wReturn.getNonEmpty(i);
			inf.getFieldTable()[i] = item.getText(1);
			inf.getFieldStream()[i] = item.getText(2);
			inf.getFieldFormatOk()[i] = "Y".equalsIgnoreCase(item.getText(3));
		}

        inf.setSchemaName( wSchema.getText() );
		inf.setTableName( wTable.getText() );
		inf.setDatabaseMeta(  transMeta.findDatabase(wConnection.getText()) );
		inf.setMClientPath( wMClientPath.getText() );
		inf.setLogFile( wLogFile.getText() );
		inf.setEncoding( wEncoding.getText() );

		stepname = wStepname.getText(); // return value
	}

	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		// Get the information for the dialog into the input structure.
		getInfo(input);

		if (input.getDatabaseMeta() == null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}

		dispose();
	}

	private void getTableName()
	{
		DatabaseMeta inf = null;
		// New class: SelectTableDialog
		int connr = wConnection.getSelectionIndex();
		if (connr >= 0)
			inf = transMeta.getDatabase(connr);

		if (inf != null)
		{
			if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.Log.LookingAtConnection") + inf.toString()); //$NON-NLS-1$

			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, transMeta.getDatabases());
      std.setSelectedSchemaAndTable(wSchema.getText(), wTable.getText());
			if (std.open())
			{
                wSchema.setText(Const.NVL(std.getSchemaName(), ""));
                wTable.setText(Const.NVL(std.getTableName(), ""));
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}
	}
	/**
	 * Reads in the fields from the previous steps and from the ONE next step and opens an 
	 * EnterMappingDialog with this information. After the user did the mapping, those information 
	 * is put into the Select/Rename table.
	 */
	private void generateMappings() {

		// Determine the source and target fields...
		//
		RowMetaInterface sourceFields;
		RowMetaInterface targetFields;

		try {
			sourceFields = transMeta.getPrevStepFields(stepMeta);
		} catch(KettleException e) {
			new ErrorDialog(shell, BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.DoMapping.UnableToFindSourceFields.Title"), BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.DoMapping.UnableToFindSourceFields.Message"), e);
			return;
		}
		// refresh data
		input.setDatabaseMeta(transMeta.findDatabase(wConnection.getText()) );
		input.setTableName(transMeta.environmentSubstitute(wTable.getText()));
		StepMetaInterface stepMetaInterface = stepMeta.getStepMetaInterface();
		try {
			targetFields = stepMetaInterface.getRequiredFields(transMeta);
		} catch (KettleException e) {
			new ErrorDialog(shell, BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.DoMapping.UnableToFindTargetFields.Title"), BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.DoMapping.UnableToFindTargetFields.Message"), e);
			return;
		}

		String[] inputNames = new String[sourceFields.size()];
		for (int i = 0; i < sourceFields.size(); i++) {
			ValueMetaInterface value = sourceFields.getValueMeta(i);
			inputNames[i] = value.getName()+
			     EnterMappingDialog.STRING_ORIGIN_SEPARATOR+value.getOrigin()+")";
		}

		// Create the existing mapping list...
		//
		List<SourceToTargetMapping> mappings = new ArrayList<SourceToTargetMapping>();
		StringBuffer missingSourceFields = new StringBuffer();
		StringBuffer missingTargetFields = new StringBuffer();

		int nrFields = wReturn.nrNonEmpty();
		for (int i = 0; i < nrFields ; i++) {
			TableItem item = wReturn.getNonEmpty(i);
			String source = item.getText(2);
			String target = item.getText(1);
			
			int sourceIndex = sourceFields.indexOfValue(source); 
			if (sourceIndex<0) {
				missingSourceFields.append(Const.CR + "   " + source+" --> " + target);
			}
			int targetIndex = targetFields.indexOfValue(target);
			if (targetIndex<0) {
				missingTargetFields.append(Const.CR + "   " + source+" --> " + target);
			}
			if (sourceIndex<0 || targetIndex<0) {
				continue;
			}

			SourceToTargetMapping mapping = new SourceToTargetMapping(sourceIndex, targetIndex);
			mappings.add(mapping);
		}

		// show a confirm dialog if some missing field was found
		//
		if (missingSourceFields.length()>0 || missingTargetFields.length()>0){
			
			String message="";
			if (missingSourceFields.length()>0) {
				message+=BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.DoMapping.SomeSourceFieldsNotFound", missingSourceFields.toString())+Const.CR;
			}
			if (missingTargetFields.length()>0) {
				message+=BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.DoMapping.SomeTargetFieldsNotFound", missingSourceFields.toString())+Const.CR;
			}
			message+=Const.CR;
			message+=BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.DoMapping.SomeFieldsNotFoundContinue")+Const.CR;
			MessageDialog.setDefaultImage(GUIResource.getInstance().getImageSpoon());
			boolean goOn = MessageDialog.openConfirm(shell, BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.DoMapping.SomeFieldsNotFoundTitle"), message);
			if (!goOn) {
				return;
			}
		}
		EnterMappingDialog d = new EnterMappingDialog(MonetDBBulkLoaderDialog.this.shell, sourceFields.getFieldNames(), targetFields.getFieldNames(), mappings);
		mappings = d.open();

		// mappings == null if the user pressed cancel
		//
		if (mappings!=null) {
			// Clear and re-populate!
			//
			wReturn.table.removeAll();
			wReturn.table.setItemCount(mappings.size());
			for (int i = 0; i < mappings.size(); i++) {
				SourceToTargetMapping mapping = (SourceToTargetMapping) mappings.get(i);
				TableItem item = wReturn.table.getItem(i);
				item.setText(2, sourceFields.getValueMeta(mapping.getSourcePosition()).getName());
				item.setText(1, targetFields.getValueMeta(mapping.getTargetPosition()).getName());
			}
			wReturn.setRowNums();
			wReturn.optWidth(true);
		}
	}
	private void getUpdate()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r != null)
			{
                TableItemInsertListener listener = new TableItemInsertListener()
                {
                    public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                    {
                    	if ( v.getType() == ValueMetaInterface.TYPE_DATE )
                    	{
                    		// The default is : format is OK for dates, see if this sticks later on...
                    		//
                    		tableItem.setText(3, "Y");
                    	}
                    	else
                    	{
                            tableItem.setText(3, "Y"); // default is OK too...
                    	}
                        return true;
                    }
                };
                BaseStepDialog.getFieldsFromPrevious(r, wReturn, 1, new int[] { 1, 2}, new int[] {}, -1, -1, listener);
			}
		}
		catch (KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.FailedToGetFields.DialogTitle"), //$NON-NLS-1$
					BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$
		}
	}

	// Generate code for create table...
	// Conversions done by Database
	private void create()
	{
		try
		{
			MonetDBBulkLoaderMeta info = new MonetDBBulkLoaderMeta();
			getInfo(info);

			String name = stepname; // new name might not yet be linked to other steps!
			StepMeta stepMeta = new StepMeta(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.StepMeta.Title"), name, info); //$NON-NLS-1$
			RowMetaInterface prev = transMeta.getPrevStepFields(stepname);

			SQLStatement sql = info.getSQLStatements(transMeta, stepMeta, prev);
			if (!sql.hasError())
			{
				if (sql.hasSQL())
				{
					SQLEditor sqledit = new SQLEditor(transMeta, shell, SWT.NONE, info.getDatabaseMeta(), transMeta.getDbCache(),
							sql.getSQL());
					sqledit.open();
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
					mb.setMessage(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.NoSQLNeeds.DialogMessage")); //$NON-NLS-1$
					mb.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.NoSQLNeeds.DialogTitle")); //$NON-NLS-1$
					mb.open();
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				mb.setMessage(sql.getError());
				mb.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.SQLError.DialogTitle")); //$NON-NLS-1$
				mb.open();
			}
		}
		catch (KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.CouldNotBuildSQL.DialogTitle"), //$NON-NLS-1$
					BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.CouldNotBuildSQL.DialogMessage"), ke); //$NON-NLS-1$
		}
	}
}