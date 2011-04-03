 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/


package org.pentaho.di.ui.trans.steps.synchronizeaftermerge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
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
import org.pentaho.di.trans.steps.synchronizeaftermerge.SynchronizeAfterMergeMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

public class SynchronizeAfterMergeDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = SynchronizeAfterMergeMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CCombo				wConnection;

	private Label				wlKey;
	private TableView			wKey;
	private FormData			fdlKey, fdKey;

    private Label               wlSchema;
    private TextVar             wSchema;
    private FormData            fdlSchema, fdSchema;
    private Button				wbSchema;  
    private FormData			fdbSchema;

	private Label				wlTable;
	private Button				wbTable;
	private TextVar				wTable;
	private FormData			fdlTable, fdbTable, fdTable;

	private Label				wlReturn;
	private TableView			wReturn;
	private FormData			fdlReturn, fdReturn;

	private Label				wlCommit;
	private Text				wCommit;
	private FormData			fdlCommit, fdCommit;

	private Button				wGetLU;
	private FormData			fdGetLU;
	private Listener			lsGetLU;
	
	private Label       		wlTableField;
	private CCombo       		wTableField;
	private FormData     		fdlTableField, fdTableField;
	
	private Label				wlTablenameInField;
	private Button				wTablenameInField;
	private FormData			fdlTablenameInField, fdTablenameInField;
	
	private Label        wlBatch;
	private Button       wBatch;
	private FormData     fdlBatch, fdBatch;
	
	private Label        wlPerformLookup;
	private Button       wPerformLookup;
	private FormData     fdlPerformLookup, fdPerformLookup;


	private Group wOperationOrder;
	private FormData fdOperationOrder;
	
	private Label       		wlOperationField;
	private CCombo       		wOperationField;
	private FormData     		fdlOperationField, fdOperationField;
	
    private Label               wlOrderInsert;
    private TextVar             wOrderInsert;
    private FormData            fdOrderInsert, fdlOrderInsert;
	
    private Label               wlOrderDelete;
    private TextVar             wOrderDelete;
    private FormData            fdOrderDelete, fdlOrderDelete;
    
    private Label               wlOrderUpdate;
    private TextVar             wOrderUpdate;
    private FormData            fdOrderUpdate, fdlOrderUpdate;
    
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wGeneralTab,wAdvancedTab;
	private Composite    wGeneralComp,wAdvancedComp;
	private FormData     fdGeneralComp,fdAdvancedComp;
    
	private SynchronizeAfterMergeMeta	input;
	
	 private Map<String, Integer> inputFields;
    
    private ColumnInfo[] ciKey;
    
    private ColumnInfo[] ciReturn;
    
	private String fieldNames[];
	
	private boolean gotPreviousFields=false;
	
	private Button     wDoMapping;
	private FormData   fdDoMapping;

	
	/**
	 * List of ColumnInfo that should have the field names of the selected database table
	 */
	private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();
    
	public SynchronizeAfterMergeDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input = (SynchronizeAfterMergeMeta) in;
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

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.Shell.Title")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.Stepname.Label")); //$NON-NLS-1$
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

		
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
 		
		
		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.GeneralTab.TabTitle"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
		
		// Connection line
		wConnection = addConnectionLine(wGeneralComp, wStepname, middle, margin);
		if (input.getDatabaseMeta()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);
		wConnection.addSelectionListener(lsSelection);

        // Schema line...
        wlSchema=new Label(wGeneralComp, SWT.RIGHT);
        wlSchema.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.TargetSchema.Label")); //$NON-NLS-1$
        props.setLook(wlSchema);
        fdlSchema=new FormData();
        fdlSchema.left = new FormAttachment(0, 0);
        fdlSchema.right= new FormAttachment(middle, -margin);
        fdlSchema.top  = new FormAttachment(wConnection, margin*2);
        wlSchema.setLayoutData(fdlSchema);
        
		wbSchema=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbSchema);
 		wbSchema.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
 		fdbSchema=new FormData();
 		fdbSchema.top  = new FormAttachment(wConnection, 2*margin);
 		fdbSchema.right= new FormAttachment(100, 0);
		wbSchema.setLayoutData(fdbSchema);

        wSchema=new TextVar(transMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wSchema);
        wSchema.addModifyListener(lsTableMod);
        fdSchema=new FormData();
        fdSchema.left = new FormAttachment(middle, 0);
        fdSchema.top  = new FormAttachment(wConnection, margin*2);
        fdSchema.right= new FormAttachment(wbSchema, -margin);
        wSchema.setLayoutData(fdSchema);

		// Table line...
		wlTable = new Label(wGeneralComp, SWT.RIGHT);
		wlTable.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.TargetTable.Label")); //$NON-NLS-1$
 		props.setLook(wlTable);
		fdlTable = new FormData();
		fdlTable.left = new FormAttachment(0, 0);
		fdlTable.right = new FormAttachment(middle, -margin);
		fdlTable.top = new FormAttachment(wbSchema, margin);
		wlTable.setLayoutData(fdlTable);

		wbTable = new Button(wGeneralComp, SWT.PUSH | SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.Browse.Button")); //$NON-NLS-1$
		fdbTable = new FormData();
		fdbTable.right = new FormAttachment(100, 0);
		fdbTable.top = new FormAttachment(wbSchema, margin);
		wbTable.setLayoutData(fdbTable);

		wTable = new TextVar(transMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTable);
		wTable.addModifyListener(lsTableMod);
		fdTable = new FormData();
		fdTable.left = new FormAttachment(middle, 0);
		fdTable.top = new FormAttachment(wbSchema, margin);
		fdTable.right = new FormAttachment(wbTable, -margin);
		wTable.setLayoutData(fdTable);

		// Commit line
		wlCommit = new Label(wGeneralComp, SWT.RIGHT);
		wlCommit.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.CommitSize.Label")); //$NON-NLS-1$
 		props.setLook(wlCommit);
		fdlCommit = new FormData();
		fdlCommit.left = new FormAttachment(0, 0);
		fdlCommit.top = new FormAttachment(wTable, margin);
		fdlCommit.right = new FormAttachment(middle, -margin);
		wlCommit.setLayoutData(fdlCommit);
		wCommit = new Text(wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wCommit);
		wCommit.addModifyListener(lsMod);
		fdCommit = new FormData();
		fdCommit.left = new FormAttachment(middle, 0);
		fdCommit.top = new FormAttachment(wTable, margin);
		fdCommit.right = new FormAttachment(100, 0);
		wCommit.setLayoutData(fdCommit);

		// UsePart update
		wlBatch=new Label(wGeneralComp, SWT.RIGHT);
		wlBatch.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.Batch.Label"));
 		props.setLook(wlBatch);
		fdlBatch=new FormData();
		fdlBatch.left  = new FormAttachment(0, 0);
		fdlBatch.top   = new FormAttachment(wCommit, margin);
		fdlBatch.right = new FormAttachment(middle, -margin);
		wlBatch.setLayoutData(fdlBatch);
		wBatch=new Button(wGeneralComp, SWT.CHECK);
		wBatch.setToolTipText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.Batch.Tooltip"));
 		props.setLook(wBatch);
		fdBatch=new FormData();
		fdBatch.left  = new FormAttachment(middle, 0);
		fdBatch.top   = new FormAttachment(wCommit, margin);
		fdBatch.right = new FormAttachment(100, 0);
		wBatch.setLayoutData(fdBatch);
		
		// TablenameInField line
		wlTablenameInField = new Label(wGeneralComp, SWT.RIGHT);
		wlTablenameInField.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.TablenameInField.Label")); //$NON-NLS-1$
 		props.setLook(wlTablenameInField);
		fdlTablenameInField = new FormData();
		fdlTablenameInField.left = new FormAttachment(0, 0);
		fdlTablenameInField.top = new FormAttachment(wBatch, margin);
		fdlTablenameInField.right = new FormAttachment(middle, -margin);
		wlTablenameInField.setLayoutData(fdlTablenameInField);
		wTablenameInField = new Button(wGeneralComp, SWT.CHECK);
		wTablenameInField.setToolTipText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.TablenameInField.Tooltip"));
 		props.setLook(wTablenameInField);
		fdTablenameInField = new FormData();
		fdTablenameInField.left = new FormAttachment(middle, 0);
		fdTablenameInField.top = new FormAttachment(wBatch, margin);
		fdTablenameInField.right = new FormAttachment(100, 0);
		wTablenameInField.setLayoutData(fdTablenameInField);
		wTablenameInField.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				activeTablenameField();
				input.setChanged();
			}
		}
	);
		
		wlTableField=new Label(wGeneralComp, SWT.RIGHT);
        wlTableField.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.TableField.Label"));
        props.setLook(wlTableField);
        fdlTableField=new FormData();
        fdlTableField.left = new FormAttachment(0, 0);
        fdlTableField.top  = new FormAttachment(wTablenameInField, margin);
        fdlTableField.right= new FormAttachment(middle, -margin);
        wlTableField.setLayoutData(fdlTableField);
        wTableField=new CCombo(wGeneralComp, SWT.BORDER | SWT.READ_ONLY);
        wTableField.setEditable(true);
        props.setLook(wTableField);
        wTableField.addModifyListener(lsMod);
        fdTableField=new FormData();
        fdTableField.left = new FormAttachment(middle, 0);
        fdTableField.top  = new FormAttachment(wTablenameInField, margin);
        fdTableField.right= new FormAttachment(100, 0);
        wTableField.setLayoutData(fdTableField);
        wTableField.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                getFields();
                shell.setCursor(null);
                busy.dispose();
            }
        }
    );  
        
		wlKey = new Label(wGeneralComp, SWT.NONE);
		wlKey.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.Keys.Label")); //$NON-NLS-1$
 		props.setLook(wlKey);
		fdlKey = new FormData();
		fdlKey.left = new FormAttachment(0, 0);
		fdlKey.top = new FormAttachment(wTableField, margin);
		wlKey.setLayoutData(fdlKey);

		int nrKeyCols = 4;
		int nrKeyRows = (input.getKeyStream() != null ? input.getKeyStream().length : 1);

		ciKey = new ColumnInfo[nrKeyCols];
		ciKey[0] = new ColumnInfo(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.ColumnInfo.TableField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciKey[1] = new ColumnInfo(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.ColumnInfo.Comparator"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "=", "<>", "<", "<=", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				">", ">=", "LIKE", "BETWEEN", "IS NULL", "IS NOT NULL" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		ciKey[2] = new ColumnInfo(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.ColumnInfo.StreamField1"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciKey[3] = new ColumnInfo(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.ColumnInfo.StreamField2"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		tableFieldColumns.add(ciKey[0]);
		wKey = new TableView(transMeta, wGeneralComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, ciKey,
				nrKeyRows, lsMod, props);

		wGet = new Button(wGeneralComp, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.GetFields.Button")); //$NON-NLS-1$
		fdGet = new FormData();
		fdGet.right = new FormAttachment(100, 0);
		fdGet.top = new FormAttachment(wlKey, margin);
		wGet.setLayoutData(fdGet);

		fdKey = new FormData();
		fdKey.left = new FormAttachment(0, 0);
		fdKey.top = new FormAttachment(wlKey, margin);
		fdKey.right = new FormAttachment(wGet, -margin);
		fdKey.bottom = new FormAttachment(wlKey, 160);
		wKey.setLayoutData(fdKey);

		// THE BUTTONS
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wSQL = new Button(shell, SWT.PUSH);
		wSQL.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.SQL.Button")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wSQL, wCancel }, margin, null);

		
		// THE UPDATE/INSERT TABLE
		wlReturn = new Label(wGeneralComp, SWT.NONE);
		wlReturn.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.UpdateFields.Label")); //$NON-NLS-1$
 		props.setLook(wlReturn);
		fdlReturn = new FormData();
		fdlReturn.left = new FormAttachment(0, 0);
		fdlReturn.top = new FormAttachment(wKey, margin);
		wlReturn.setLayoutData(fdlReturn);

		int UpInsCols = 3;
		int UpInsRows = (input.getUpdateLookup() != null ? input.getUpdateLookup().length : 1);

		ciReturn = new ColumnInfo[UpInsCols];
		ciReturn[0] = new ColumnInfo(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.ColumnInfo.TableField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciReturn[1] = new ColumnInfo(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.ColumnInfo.StreamField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciReturn[2] = new ColumnInfo(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.ColumnInfo.Update"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {"Y","N"}); //$NON-NLS-1$
		tableFieldColumns.add(ciReturn[0]);
		wReturn = new TableView(transMeta, wGeneralComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
				ciReturn, UpInsRows, lsMod, props);

		wGetLU = new Button(wGeneralComp, SWT.PUSH);
		wGetLU.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.GetAndUpdateFields.Label")); //$NON-NLS-1$
		fdGetLU = new FormData();
		fdGetLU.top   = new FormAttachment(wlReturn, margin);
		fdGetLU.right = new FormAttachment(100, 0);
		wGetLU.setLayoutData(fdGetLU);

		fdReturn = new FormData();
		fdReturn.left = new FormAttachment(0, 0);
		fdReturn.top = new FormAttachment(wlReturn, margin);
		fdReturn.right = new FormAttachment(wGetLU, -margin);
		fdReturn.bottom = new FormAttachment(100, -2*margin);
		wReturn.setLayoutData(fdReturn);
		
		
		wDoMapping = new Button(wGeneralComp, SWT.PUSH);
		wDoMapping.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.EditMapping.Label")); //$NON-NLS-1$
		fdDoMapping = new FormData();
		fdDoMapping.top   = new FormAttachment(wGetLU, margin);
		fdDoMapping.right = new FormAttachment(100, 0);
		wDoMapping.setLayoutData(fdDoMapping);

		wDoMapping.addListener(SWT.Selection, new Listener() { 	public void handleEvent(Event arg0) { generateMappings();}});

		
	    // 
        // Search the fields in the background
        //
        
        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                //  This is running in a new process: copy some KettleVariables info
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
        
		
		// Add listeners
		lsOK = new Listener()
		{
			public void handleEvent(Event e)
			{
				ok();
			}
		};
		lsGet = new Listener()
		{
			public void handleEvent(Event e)
			{
				get();
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

		
		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(0, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(100, 0);
		wGeneralComp.setLayoutData(fdGeneralComp);
		
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
 		props.setLook(wGeneralComp);
 		
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////

		//////////////////////////
		// START OF ADVANCED TAB   ///
		//////////////////////////
		
		wAdvancedTab=new CTabItem(wTabFolder, SWT.NONE);
		wAdvancedTab.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.AdvancedTab.TabTitle"));
		
		wAdvancedComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wAdvancedComp);

		FormLayout advancedLayout = new FormLayout();
		advancedLayout.marginWidth  = 3;
		advancedLayout.marginHeight = 3;
		wAdvancedComp.setLayout(advancedLayout);
 		
		// ///////////////////////////////
		// START OF OPERATION ORDER GROUP  //
		///////////////////////////////// 

		wOperationOrder = new Group(wAdvancedComp, SWT.SHADOW_NONE);
		props.setLook(wOperationOrder);
		wOperationOrder.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.OperationOrder.Label"));
		
		FormLayout OriginFilesgroupLayout = new FormLayout();
		OriginFilesgroupLayout.marginWidth = 10;
		OriginFilesgroupLayout.marginHeight = 10;
		wOperationOrder.setLayout(OriginFilesgroupLayout);
		
		wlOperationField=new Label(wOperationOrder, SWT.RIGHT);
        wlOperationField.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.OperationField.Label"));
        props.setLook(wlOperationField);
        fdlOperationField=new FormData();
        fdlOperationField.left = new FormAttachment(0, 0);
        fdlOperationField.top  = new FormAttachment(wTableField, margin);
        fdlOperationField.right= new FormAttachment(middle, -margin);
        wlOperationField.setLayoutData(fdlOperationField);
        wOperationField=new CCombo(wOperationOrder, SWT.BORDER | SWT.READ_ONLY);
        wOperationField.setEditable(true);
        props.setLook(wOperationField);
        wOperationField.addModifyListener(lsMod);
        fdOperationField=new FormData();
        fdOperationField.left = new FormAttachment(middle, 0);
        fdOperationField.top  = new FormAttachment(wTableField, margin);
        fdOperationField.right= new FormAttachment(100, 0);
        wOperationField.setLayoutData(fdOperationField);
        wOperationField.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                getFields();
                shell.setCursor(null);
                busy.dispose();
            }
        }
    );  
        

        // OrderInsert line...
        wlOrderInsert=new Label(wOperationOrder, SWT.RIGHT);
        wlOrderInsert.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.OrderInsert.Label")); //$NON-NLS-1$
        props.setLook(wlOrderInsert);
        fdlOrderInsert=new FormData();
        fdlOrderInsert.left = new FormAttachment(0, 0);
        fdlOrderInsert.right= new FormAttachment(middle, -margin);
        fdlOrderInsert.top  = new FormAttachment(wOperationField, margin);
        wlOrderInsert.setLayoutData(fdlOrderInsert);

        wOrderInsert=new TextVar(transMeta, wOperationOrder, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wOrderInsert.setToolTipText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.OrderInsert.ToolTip"));
        props.setLook(wOrderInsert);
        wOrderInsert.addModifyListener(lsMod);
        fdOrderInsert=new FormData();
        fdOrderInsert.left = new FormAttachment(middle, 0);
        fdOrderInsert.top  = new FormAttachment(wOperationField, margin);
        fdOrderInsert.right= new FormAttachment(100, 0);
        wOrderInsert.setLayoutData(fdOrderInsert);
        
        // OrderUpdate line...
        wlOrderUpdate=new Label(wOperationOrder, SWT.RIGHT);
        wlOrderUpdate.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.OrderUpdate.Label")); //$NON-NLS-1$
        props.setLook(wlOrderUpdate);
        fdlOrderUpdate=new FormData();
        fdlOrderUpdate.left = new FormAttachment(0, 0);
        fdlOrderUpdate.right= new FormAttachment(middle, -margin);
        fdlOrderUpdate.top  = new FormAttachment(wOrderInsert, margin);
        wlOrderUpdate.setLayoutData(fdlOrderUpdate);

        wOrderUpdate=new TextVar(transMeta, wOperationOrder, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wOrderUpdate.setToolTipText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.OrderUpdate.ToolTip"));
        props.setLook(wOrderUpdate);
        wOrderUpdate.addModifyListener(lsMod);
        fdOrderUpdate=new FormData();
        fdOrderUpdate.left = new FormAttachment(middle, 0);
        fdOrderUpdate.top  = new FormAttachment(wOrderInsert, margin);
        fdOrderUpdate.right= new FormAttachment(100, 0);
        wOrderUpdate.setLayoutData(fdOrderUpdate);

        
        // OrderDelete line...
        wlOrderDelete=new Label(wOperationOrder, SWT.RIGHT);
        wlOrderDelete.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.OrderDelete.Label")); //$NON-NLS-1$
        props.setLook(wlOrderDelete);
        fdlOrderDelete=new FormData();
        fdlOrderDelete.left = new FormAttachment(0, 0);
        fdlOrderDelete.right= new FormAttachment(middle, -margin);
        fdlOrderDelete.top  = new FormAttachment(wOrderUpdate, margin);
        wlOrderDelete.setLayoutData(fdlOrderDelete);

        wOrderDelete=new TextVar(transMeta, wOperationOrder, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wOrderDelete.setToolTipText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.OrderDelete.ToolTip"));
        props.setLook(wOrderDelete);
        wOrderDelete.addModifyListener(lsMod);
        fdOrderDelete=new FormData();
        fdOrderDelete.left = new FormAttachment(middle, 0);
        fdOrderDelete.top  = new FormAttachment(wOrderUpdate, margin);
        fdOrderDelete.right= new FormAttachment(100, 0);
        wOrderDelete.setLayoutData(fdOrderDelete);
        
		// Perform a lookup?
		wlPerformLookup=new Label(wOperationOrder, SWT.RIGHT);
		wlPerformLookup.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.PerformLookup.Label"));
 		props.setLook(wlPerformLookup);
		fdlPerformLookup=new FormData();
		fdlPerformLookup.left  = new FormAttachment(0, 0);
		fdlPerformLookup.top   = new FormAttachment(wOrderDelete, margin);
		fdlPerformLookup.right = new FormAttachment(middle, -margin);
		wlPerformLookup.setLayoutData(fdlPerformLookup);
		wPerformLookup=new Button(wOperationOrder, SWT.CHECK);
		wPerformLookup.setToolTipText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.PerformLookup.Tooltip"));
 		props.setLook(wPerformLookup);
		fdPerformLookup=new FormData();
		fdPerformLookup.left  = new FormAttachment(middle, 0);
		fdPerformLookup.top   = new FormAttachment(wOrderDelete, margin);
		fdPerformLookup.right = new FormAttachment(100, 0);
		wPerformLookup.setLayoutData(fdPerformLookup);
		
		fdOperationOrder = new FormData();
		fdOperationOrder.left = new FormAttachment(0, margin);
		fdOperationOrder.top = new FormAttachment(wStepname, margin);
		fdOperationOrder.right = new FormAttachment(100, -margin);
		wOperationOrder.setLayoutData(fdOperationOrder);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Operation order GROUP
		// ///////////////////////////////////////////////////////////		

		
		
		
		fdAdvancedComp=new FormData();
		fdAdvancedComp.left  = new FormAttachment(0, 0);
		fdAdvancedComp.top   = new FormAttachment(0, 0);
		fdAdvancedComp.right = new FormAttachment(100, 0);
		fdAdvancedComp.bottom= new FormAttachment(100, 0);
		wAdvancedComp.setLayoutData(fdAdvancedComp);
		
		wAdvancedComp.layout();
		wAdvancedTab.setControl(wAdvancedComp);
 		props.setLook(wAdvancedComp);
 		
		/////////////////////////////////////////////////////////////
		/// END OF ADVANCED TAB
		/////////////////////////////////////////////////////////////

		
		
		
		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
 		

		
		wOK.addListener(SWT.Selection, lsOK);
		wGet.addListener(SWT.Selection, lsGet);
		wGetLU.addListener(SWT.Selection, lsGetLU);
		wSQL.addListener(SWT.Selection, lsSQL);
		wCancel.addListener(SWT.Selection, lsCancel);

		lsDef = new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wStepname.addSelectionListener(lsDef);
        wSchema.addSelectionListener(lsDef);
        wTable.addSelectionListener(lsDef);
        wCommit.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter()
		{
			public void shellClosed(ShellEvent e)
			{
				cancel();
			}
		});
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

		wbTable.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				getTableName();
			}
		});

		wTabFolder.setSelection(0);
		
		// Set the shell size, based upon previous time...
		setSize();
		getData();
		setTableFieldCombo();
		activeTablenameField();
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
			new ErrorDialog(shell, BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.DoMapping.UnableToFindSourceFields.Title"), BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.DoMapping.UnableToFindSourceFields.Message"), e);
			return;
		}

		// refresh data
		input.setDatabaseMeta(transMeta.findDatabase(wConnection.getText()) );
		input.setTableName(transMeta.environmentSubstitute(wTable.getText()));
		StepMetaInterface stepMetaInterface = stepMeta.getStepMetaInterface();
		try {
			targetFields = stepMetaInterface.getRequiredFields(transMeta);
		} catch (KettleException e) {
			new ErrorDialog(shell, BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.DoMapping.UnableToFindTargetFields.Title"), BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.DoMapping.UnableToFindTargetFields.Message"), e);
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
				message+=BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.DoMapping.SomeSourceFieldsNotFound", missingSourceFields.toString())+Const.CR;
			}
			if (missingTargetFields.length()>0) {
				message+=BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.DoMapping.SomeTargetFieldsNotFound", missingSourceFields.toString())+Const.CR;
			}
			message+=Const.CR;
			message+=BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.DoMapping.SomeFieldsNotFoundContinue")+Const.CR;
			MessageDialog.setDefaultImage(GUIResource.getInstance().getImageSpoon());
			boolean goOn = MessageDialog.openConfirm(shell, BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.DoMapping.SomeFieldsNotFoundTitle"), message);
			if (!goOn) {
				return;
			}
		}
		EnterMappingDialog d = new EnterMappingDialog(SynchronizeAfterMergeDialog.this.shell, sourceFields.getFieldNames(), targetFields.getFieldNames(), mappings);
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
						db.shareVariablesWith(transMeta);
						try {
							db.connect();

							String schemaTable = ci	.getQuotedSchemaTableCombination(transMeta.environmentSubstitute(wSchema
											.getText()), transMeta.environmentSubstitute(wTable.getText()));
							RowMetaInterface r = db.getTableFields(schemaTable);
							if (null != r) {
								String[] fieldNames = r.getFieldNames();
								if (null != fieldNames) {
									for (int i = 0; i < tableFieldColumns
											.size(); i++) {
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
	protected void setComboBoxes()
    {
        // Something was changed in the row.
        //
		final Map<String, Integer> fields = new HashMap<String, Integer>();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);

        
        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);
        
        fieldNames = (String[]) entries.toArray(new String[entries.size()]);

        Const.sortStrings(fieldNames);
        ciKey[2].setComboValues(fieldNames);
        ciKey[3].setComboValues(fieldNames);
        ciReturn[1].setComboValues(fieldNames);
    }

	 private void activeTablenameField()
	 {
		wlTableField.setEnabled(wTablenameInField.getSelection()) ;
		wTableField.setEnabled(wTablenameInField.getSelection()) ;
		wlTable.setEnabled(!wTablenameInField.getSelection()) ;
		wTable.setEnabled(!wTablenameInField.getSelection()) ;
		wbTable.setEnabled(!wTablenameInField.getSelection()) ;
		wSQL.setEnabled(!wTablenameInField.getSelection()) ;
		
	 }
	 private void getFields()
	 {
		if(!gotPreviousFields)
		{
		 try{
			 String field=wTableField.getText();
			 String fieldoperation=wOperationField.getText();
			 RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			 if(r!=null)
			  {
				 wTableField.setItems(r.getFieldNames());
				 wOperationField.setItems(r.getFieldNames());
			  }
			 if(field!=null) wTableField.setText(field);
			 if(fieldoperation!=null) wOperationField.setText(fieldoperation);
		 	}catch(KettleException ke){
				new ErrorDialog(shell, BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.FailedToGetFields.DialogMessage"), ke);
			}
		 	gotPreviousFields=true;
		}
	 }
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		int i;
		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.Log.GettingKeyInfo")); //$NON-NLS-1$

		wCommit.setText("" + input.getCommitSize()); //$NON-NLS-1$
		wTablenameInField.setSelection(input.istablenameInField());
		if(input.gettablenameField()!=null) wTableField.setText(input.gettablenameField());
		wBatch.setSelection(input.useBatchUpdate());
		if(input.getOperationOrderField()!=null) wOperationField.setText(input.getOperationOrderField());
		if(input.getOrderInsert()!=null) wOrderInsert.setText(input.getOrderInsert());
		if(input.getOrderUpdate()!=null) wOrderUpdate.setText(input.getOrderUpdate());
		if(input.getOrderDelete()!=null) wOrderDelete.setText(input.getOrderDelete());
		wPerformLookup.setSelection(input.isPerformLookup());
		
		
		if (input.getKeyStream() != null)
			for (i = 0; i < input.getKeyStream().length; i++)
			{
				TableItem item = wKey.table.getItem(i);
				if (input.getKeyLookup()[i] != null)
					item.setText(1, input.getKeyLookup()[i]);
				if (input.getKeyCondition()[i] != null)
					item.setText(2, input.getKeyCondition()[i]);
				if (input.getKeyStream()[i] != null)
					item.setText(3, input.getKeyStream()[i]);
				if (input.getKeyStream2()[i] != null)
					item.setText(4, input.getKeyStream2()[i]);
			}

		if (input.getUpdateLookup() != null)
			for (i = 0; i < input.getUpdateLookup().length; i++)
			{
				TableItem item = wReturn.table.getItem(i);
				if (input.getUpdateLookup()[i] != null)
					item.setText(1, input.getUpdateLookup()[i]);
				if (input.getUpdateStream()[i] != null)
					item.setText(2, input.getUpdateStream()[i]);
				if (input.getUpdate()[i]==null||input.getUpdate()[i].booleanValue()) {
					item.setText(3,"Y");
				} else {
					item.setText(3,"N");
				}
			}

        if (input.getSchemaName() != null) wSchema.setText(input.getSchemaName());
		if (input.getTableName() != null) wTable.setText(input.getTableName());
		if (input.getDatabaseMeta() != null)
			wConnection.setText(input.getDatabaseMeta().getName());
		else
			if (transMeta.nrDatabases() == 1)
			{
				wConnection.setText(transMeta.getDatabase(0).getName());
			}

		wStepname.selectAll();
		wKey.setRowNums();
		wKey.optWidth(true);
		wReturn.setRowNums();
		wReturn.optWidth(true);

	}

	private void cancel()
	{
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	private void getInfo(SynchronizeAfterMergeMeta inf)
	{
		//Table ktable = wKey.table;
		int nrkeys = wKey.nrNonEmpty();
		int nrfields = wReturn.nrNonEmpty();

		inf.allocate(nrkeys, nrfields);

		inf.setCommitSize( Const.toInt(wCommit.getText(), 0) );
		inf.settablenameInField( wTablenameInField.getSelection() );
		inf.settablenameField(wTableField.getText());
		inf.setUseBatchUpdate( wBatch.getSelection() );
		inf.setPerformLookup(wPerformLookup.getSelection() );
		
		inf.setOperationOrderField(wOperationField.getText());
		inf.setOrderInsert(wOrderInsert.getText());
		inf.setOrderUpdate(wOrderUpdate.getText());
		inf.setOrderDelete(wOrderDelete.getText());
		
		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.Log.FoundKeys",nrkeys + "")); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < nrkeys; i++)
		{
			TableItem item = wKey.getNonEmpty(i);
			inf.getKeyLookup()[i] = item.getText(1);
			inf.getKeyCondition()[i] = item.getText(2);
			inf.getKeyStream()[i] = item.getText(3);
			inf.getKeyStream2()[i] = item.getText(4);
		}

		//Table ftable = wReturn.table;

		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.Log.FoundFields", nrfields + "")); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < nrfields; i++)
		{
			TableItem item = wReturn.getNonEmpty(i);
			inf.getUpdateLookup()[i] = item.getText(1);
			inf.getUpdateStream()[i] = item.getText(2);
			inf.getUpdate()[i] = Boolean.valueOf("Y".equals(item.getText(3)));
		}

        inf.setSchemaName( wSchema.getText() );
		inf.setTableName( wTable.getText() );
		inf.setDatabaseMeta(  transMeta.findDatabase(wConnection.getText()) );

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
			mb.setMessage(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
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
			if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.Log.LookingAtConnection") + inf.toString()); //$NON-NLS-1$

			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, transMeta.getDatabases());
      std.setSelectedSchemaAndTable(wSchema.getText(), wTable.getText());
			if (std.open())
			{
                wSchema.setText(Const.NVL(std.getSchemaName(), ""));
                wTable.setText(Const.NVL(std.getTableName(), ""));
                wTable.setFocus();
                setTableFieldCombo();
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}
	}

	private void get()
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
                        tableItem.setText(2, "=");
                        return true;
                    }
                };
                BaseStepDialog.getFieldsFromPrevious(r, wKey, 1, new int[] { 1, 3}, new int[] {}, -1, -1, listener);
			}
		}
		catch (KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.FailedToGetFields.DialogTitle"), //$NON-NLS-1$
					BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$
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
                        tableItem.setText(3, "Y");
                        return true;
                    }
                };
                BaseStepDialog.getFieldsFromPrevious(r, wReturn, 1, new int[] { 1, 2}, new int[] {}, -1, -1, listener);
			}
		}
		catch (KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.FailedToGetFields.DialogTitle"), //$NON-NLS-1$
					BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$
		}
	}
	// Generate code for create table...
	// Conversions done by Database
	private void create()
	{
		try
		{
			SynchronizeAfterMergeMeta info = new SynchronizeAfterMergeMeta();
			getInfo(info);

			String name = stepname; // new name might not yet be linked to other steps!
			StepMeta stepMeta = new StepMeta(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.StepMeta.Title"), name, info); //$NON-NLS-1$
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
					mb.setMessage(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.NoSQLNeeds.DialogMessage")); //$NON-NLS-1$
					mb.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.NoSQLNeeds.DialogTitle")); //$NON-NLS-1$
					mb.open();
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				mb.setMessage(sql.getError());
				mb.setText(BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.SQLError.DialogTitle")); //$NON-NLS-1$
				mb.open();
			}
		}
		catch (KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.CouldNotBuildSQL.DialogTitle"), //$NON-NLS-1$
					BaseMessages.getString(PKG, "SynchronizeAfterMergeDialog.CouldNotBuildSQL.DialogMessage"), ke); //$NON-NLS-1$
		}

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
							BaseMessages.getString(PKG,"SynchronizeAfterMergeDialog.AvailableSchemas.Title",wConnection.getText()), 
							BaseMessages.getString(PKG,"SynchronizeAfterMergeDialog.AvailableSchemas.Message",wConnection.getText()));
					String d=dialog.open();
					if (d!=null) 
					{
						wSchema.setText(Const.NVL(d.toString(), ""));
						setTableFieldCombo();
					}

				}else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
					mb.setMessage(BaseMessages.getString(PKG,"SynchronizeAfterMergeDialog.NoSchema.Error"));
					mb.setText(BaseMessages.getString(PKG,"SynchronizeAfterMergeDialog.GetSchemas.Error"));
					mb.open(); 
				}
			}
			catch(Exception e)
			{
				new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.Error.Title"), 
						BaseMessages.getString(PKG,"SynchronizeAfterMergeDialog.ErrorGettingSchemas"), e);
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
}