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

package org.pentaho.di.ui.trans.steps.mysqlbulkloader;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
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
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mysqlbulkloader.Messages;
import org.pentaho.di.trans.steps.mysqlbulkloader.MySQLBulkLoaderMeta;
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
 * Dialog class for the MySQL bulk loader step.
 * 
 */
public class MySQLBulkLoaderDialog extends BaseStepDialog implements StepDialogInterface
{
	private CCombo				wConnection;

    private Label               wlSchema;
    private TextVar             wSchema;
    private FormData            fdlSchema, fdSchema;

	private Label				wlTable;
	private Button				wbTable;
	private TextVar				wTable;
	private FormData			fdlTable, fdbTable, fdTable;

	private Label				wlFifoFile;
	private TextVar				wFifoFile;
	private FormData			fdlFifoFile, fdFifoFile;

	private Label				wlReplace;
	private Button				wReplace;
	private FormData			fdlReplace, fdReplace;

	private Label				wlIgnore;
	private Button				wIgnore;
	private FormData			fdlIgnore, fdIgnore;

	private Label				wlDelimiter;
	private Button       		wbDelimiter;
	private TextVar				wDelimiter;
	private FormData			fdlDelimiter, fdDelimiter;

	private Label				wlEnclosure;
	private TextVar				wEnclosure;
	private FormData			fdlEnclosure, fdEnclosure;

	private Label				wlEscapeChar;
	private TextVar				wEscapeChar;
	private FormData			fdlEscapeChar, fdEscapeChar;

	private Label				wlCharSet;
	private TextVar				wCharSet;
	private FormData			fdlCharSet, fdCharSet;
	
	private Label				wlBulkSize;
	private TextVar				wBulkSize;
	private FormData			fdlBulkSize, fdBulkSize;

    private Label               wlReturn;
    private TableView           wReturn;
    private FormData            fdlReturn, fdReturn;

	private Button				wGetLU;
	private FormData			fdGetLU;
	private Listener			lsGetLU;

	private Button     wDoMapping;
	private FormData   fdDoMapping;

	private MySQLBulkLoaderMeta	input;
	
	private ColumnInfo[] ciReturn ;
	
    private Map<String, Integer> inputFields;
	
	/**
	 * List of ColumnInfo that should have the field names of the selected database table
	 */
	private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();
	
	/*
    private static final String[] ALL_FILETYPES = new String[] {
        	Messages.getString("MySQLBulkLoaderDialog.Filetype.All") };
	*
	*/

	public MySQLBulkLoaderDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input = (MySQLBulkLoaderMeta) in;
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
		shell.setText(Messages.getString("MySQLBulkLoaderDialog.Shell.Title")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("MySQLBulkLoaderDialog.Stepname.Label")); //$NON-NLS-1$
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
        wlSchema.setText(Messages.getString("MySQLBulkLoaderDialog.TargetSchema.Label")); //$NON-NLS-1$
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
		wlTable.setText(Messages.getString("MySQLBulkLoaderDialog.TargetTable.Label")); //$NON-NLS-1$
 		props.setLook(wlTable);
		fdlTable = new FormData();
		fdlTable.left = new FormAttachment(0, 0);
		fdlTable.right = new FormAttachment(middle, -margin);
		fdlTable.top = new FormAttachment(wSchema, margin);
		wlTable.setLayoutData(fdlTable);
		
		wbTable = new Button(shell, SWT.PUSH | SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText(Messages.getString("MySQLBulkLoaderDialog.Browse.Button")); //$NON-NLS-1$
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

        // FifoFile line...
        wlFifoFile=new Label(shell, SWT.RIGHT);
        wlFifoFile.setText(Messages.getString("MySQLBulkLoaderDialog.FifoFile.Label")); //$NON-NLS-1$
        props.setLook(wlFifoFile);
        fdlFifoFile=new FormData();
        fdlFifoFile.left = new FormAttachment(0, 0);
        fdlFifoFile.right= new FormAttachment(middle, -margin);
        fdlFifoFile.top  = new FormAttachment(wTable, margin);
        wlFifoFile.setLayoutData(fdlFifoFile);
        wFifoFile=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wFifoFile);
        wFifoFile.addModifyListener(lsMod);
        fdFifoFile=new FormData();
        fdFifoFile.left = new FormAttachment(middle, 0);
        fdFifoFile.top  = new FormAttachment(wTable, margin);
        fdFifoFile.right= new FormAttachment(100, 0);
        wFifoFile.setLayoutData(fdFifoFile);

        // Delimiter line...
        wlDelimiter=new Label(shell, SWT.RIGHT);
        wlDelimiter.setText(Messages.getString("MySQLBulkLoaderDialog.Delimiter.Label")); //$NON-NLS-1$
        props.setLook(wlDelimiter);
        fdlDelimiter=new FormData();
        fdlDelimiter.left = new FormAttachment(0, 0);
        fdlDelimiter.right= new FormAttachment(middle, -margin);
        fdlDelimiter.top  = new FormAttachment(wFifoFile, margin);
        wlDelimiter.setLayoutData(fdlDelimiter);
		wbDelimiter=new Button(shell, SWT.PUSH| SWT.CENTER);
        props.setLook(wbDelimiter);
        wbDelimiter.setText(Messages.getString("MySQLBulkLoaderDialog.Delimiter.Button"));
        FormData fdbDelimiter=new FormData();
        fdbDelimiter.top  = new FormAttachment(wFifoFile, margin);
        fdbDelimiter.right= new FormAttachment(100, 0);        
        wbDelimiter.setLayoutData(fdbDelimiter);
        wDelimiter=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wDelimiter);
        wDelimiter.addModifyListener(lsMod);
        fdDelimiter=new FormData();
        fdDelimiter.left = new FormAttachment(middle, 0);
        fdDelimiter.top  = new FormAttachment(wFifoFile, margin);
        fdDelimiter.right= new FormAttachment(wbDelimiter, -margin);
        wDelimiter.setLayoutData(fdDelimiter);
		// Allow the insertion of tabs as separator...
		wbDelimiter.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent se) 
				{
					Text t = wDelimiter.getTextWidget();
					if ( t != null )
					    t.insert("\t");
				}
			}
		);

        // Enclosure line...
        wlEnclosure=new Label(shell, SWT.RIGHT);
        wlEnclosure.setText(Messages.getString("MySQLBulkLoaderDialog.Enclosure.Label")); //$NON-NLS-1$
        props.setLook(wlEnclosure);
        fdlEnclosure=new FormData();
        fdlEnclosure.left = new FormAttachment(0, 0);
        fdlEnclosure.right= new FormAttachment(middle, -margin);
        fdlEnclosure.top  = new FormAttachment(wDelimiter, margin);
        wlEnclosure.setLayoutData(fdlEnclosure);
        wEnclosure=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wEnclosure);
        wEnclosure.addModifyListener(lsMod);
        fdEnclosure=new FormData();
        fdEnclosure.left = new FormAttachment(middle, 0);
        fdEnclosure.top  = new FormAttachment(wDelimiter, margin);
        fdEnclosure.right= new FormAttachment(100, 0);
        wEnclosure.setLayoutData(fdEnclosure);

        // EscapeChar line...
        wlEscapeChar=new Label(shell, SWT.RIGHT);
        wlEscapeChar.setText(Messages.getString("MySQLBulkLoaderDialog.EscapeChar.Label")); //$NON-NLS-1$
        props.setLook(wlEscapeChar);
        fdlEscapeChar=new FormData();
        fdlEscapeChar.left = new FormAttachment(0, 0);
        fdlEscapeChar.right= new FormAttachment(middle, -margin);
        fdlEscapeChar.top  = new FormAttachment(wEnclosure, margin);
        wlEscapeChar.setLayoutData(fdlEscapeChar);
        wEscapeChar=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wEscapeChar);
        wEscapeChar.addModifyListener(lsMod);
        fdEscapeChar=new FormData();
        fdEscapeChar.left = new FormAttachment(middle, 0);
        fdEscapeChar.top  = new FormAttachment(wEnclosure, margin);
        fdEscapeChar.right= new FormAttachment(100, 0);
        wEscapeChar.setLayoutData(fdEscapeChar);

        // CharSet line...
        wlCharSet=new Label(shell, SWT.RIGHT);
        wlCharSet.setText(Messages.getString("MySQLBulkLoaderDialog.CharSet.Label")); //$NON-NLS-1$
        props.setLook(wlCharSet);
        fdlCharSet=new FormData();
        fdlCharSet.left = new FormAttachment(0, 0);
        fdlCharSet.right= new FormAttachment(middle, -margin);
        fdlCharSet.top  = new FormAttachment(wEscapeChar, margin);
        wlCharSet.setLayoutData(fdlCharSet);
        wCharSet=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wCharSet);
        wCharSet.addModifyListener(lsMod);
        fdCharSet=new FormData();
        fdCharSet.left = new FormAttachment(middle, 0);
        fdCharSet.top  = new FormAttachment(wEscapeChar, margin);
        fdCharSet.right= new FormAttachment(100, 0);
        wCharSet.setLayoutData(fdCharSet);

        // BulkSize line...
        wlBulkSize=new Label(shell, SWT.RIGHT);
        wlBulkSize.setText(Messages.getString("MySQLBulkLoaderDialog.BulkSize.Label")); //$NON-NLS-1$
        props.setLook(wlBulkSize);
        fdlBulkSize=new FormData();
        fdlBulkSize.left = new FormAttachment(0, 0);
        fdlBulkSize.right= new FormAttachment(middle, -margin);
        fdlBulkSize.top  = new FormAttachment(wCharSet, margin);
        wlBulkSize.setLayoutData(fdlBulkSize);
        wBulkSize=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wBulkSize);
        wBulkSize.addModifyListener(lsMod);
        fdBulkSize=new FormData();
        fdBulkSize.left = new FormAttachment(middle, 0);
        fdBulkSize.top  = new FormAttachment(wCharSet, margin);
        fdBulkSize.right= new FormAttachment(100, 0);
        wBulkSize.setLayoutData(fdBulkSize);

        
        // Replace line...
		wlReplace = new Label(shell, SWT.RIGHT);
		wlReplace.setText(Messages.getString("MySQLBulkLoaderDialog.Replace.Label")); //$NON-NLS-1$
		props.setLook(wlReplace);
		fdlReplace = new FormData();
		fdlReplace.left = new FormAttachment(0, 0);
		fdlReplace.right = new FormAttachment(middle, -margin);
		fdlReplace.top = new FormAttachment(wBulkSize, margin * 2);
		wlReplace.setLayoutData(fdlReplace);

		wReplace = new Button(shell, SWT.CHECK | SWT.LEFT );
		props.setLook(wReplace);
		fdReplace = new FormData();
		fdReplace.left = new FormAttachment(middle, 0);
		fdReplace.top = new FormAttachment(wBulkSize, margin * 2);
		fdReplace.right = new FormAttachment(100, 0);
		wReplace.setLayoutData(fdReplace);
		wReplace.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (wReplace.getSelection()) wIgnore.setSelection(false);
			}
		});
		
        // Ignore line...
		wlIgnore = new Label(shell, SWT.RIGHT);
		wlIgnore.setText(Messages.getString("MySQLBulkLoaderDialog.Ignore.Label")); //$NON-NLS-1$
		props.setLook(wlIgnore);
		fdlIgnore = new FormData();
		fdlIgnore.left = new FormAttachment(0, 0);
		fdlIgnore.right = new FormAttachment(middle, -margin);
		fdlIgnore.top = new FormAttachment(wReplace, margin * 2);
		wlIgnore.setLayoutData(fdlIgnore);

		wIgnore = new Button(shell, SWT.CHECK | SWT.LEFT );
		props.setLook(wIgnore);
		fdIgnore = new FormData();
		fdIgnore.left = new FormAttachment(middle, 0);
		fdIgnore.top = new FormAttachment(wReplace, margin * 2);
		fdIgnore.right = new FormAttachment(100, 0);
		wIgnore.setLayoutData(fdIgnore);
		wIgnore.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (wIgnore.getSelection()) wReplace.setSelection(false);
			}
		});

        
		// THE BUTTONS
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wSQL = new Button(shell, SWT.PUSH);
		wSQL.setText(Messages.getString("MySQLBulkLoaderDialog.SQL.Button")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel , wSQL }, margin, null);

		// The field Table
		wlReturn = new Label(shell, SWT.NONE);
		wlReturn.setText(Messages.getString("MySQLBulkLoaderDialog.Fields.Label")); //$NON-NLS-1$
 		props.setLook(wlReturn);
		fdlReturn = new FormData();
		fdlReturn.left = new FormAttachment(0, 0);
		fdlReturn.top = new FormAttachment(wIgnore, margin);
		wlReturn.setLayoutData(fdlReturn);

		int UpInsCols = 3;
		int UpInsRows = (input.getFieldTable() != null ? input.getFieldTable().length : 1);

		ciReturn = new ColumnInfo[UpInsCols];
		ciReturn[0] = new ColumnInfo(Messages.getString("MySQLBulkLoaderDialog.ColumnInfo.TableField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciReturn[1] = new ColumnInfo(Messages.getString("MySQLBulkLoaderDialog.ColumnInfo.StreamField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciReturn[2] = new ColumnInfo(Messages.getString("MySQLBulkLoaderDialog.ColumnInfo.FormatOK"), ColumnInfo.COLUMN_TYPE_CCOMBO, MySQLBulkLoaderMeta.getFieldFormatTypeDescriptions(), true); // $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$

		tableFieldColumns.add(ciReturn[0]);
		wReturn = new TableView(transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
				ciReturn, UpInsRows, lsMod, props);

		wGetLU = new Button(shell, SWT.PUSH);
		wGetLU.setText(Messages.getString("MySQLBulkLoaderDialog.GetFields.Label")); //$NON-NLS-1$
		fdGetLU = new FormData();
		fdGetLU.top   = new FormAttachment(wlReturn, margin);
		fdGetLU.right = new FormAttachment(100, 0);
		wGetLU.setLayoutData(fdGetLU);
		
		wDoMapping = new Button(shell, SWT.PUSH);
		wDoMapping.setText(Messages.getString("MySQLBulkLoaderDialog.EditMapping.Label")); //$NON-NLS-1$
		fdDoMapping = new FormData();
		fdDoMapping.top   = new FormAttachment(wGetLU, margin);
		fdDoMapping.right = new FormAttachment(100, 0);
		wDoMapping.setLayoutData(fdDoMapping);

		wDoMapping.addListener(SWT.Selection, new Listener() { 	public void handleEvent(Event arg0) { generateMappings();}});

		
		fdReturn = new FormData();
		fdReturn.left = new FormAttachment(0, 0);
		fdReturn.top = new FormAttachment(wlReturn, margin);
		fdReturn.right = new FormAttachment(wDoMapping, -margin);
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
                        log.logError(toString(),Messages.getString("System.Dialog.GetFieldsFailed.Message"));
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
        wFifoFile.addSelectionListener(lsDef);
        wTable.addSelectionListener(lsDef);
        wDelimiter.addSelectionListener(lsDef);
        wEnclosure.addSelectionListener(lsDef);
        wCharSet.addSelectionListener(lsDef);
        wBulkSize.addSelectionListener(lsDef);

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
			new ErrorDialog(shell, Messages.getString("MySQLBulkLoaderDialog.DoMapping.UnableToFindSourceFields.Title"), Messages.getString("MySQLBulkLoaderDialog.DoMapping.UnableToFindSourceFields.Message"), e);
			return;
		}
		// refresh data
		input.setDatabaseMeta(transMeta.findDatabase(wConnection.getText()) );
		input.setTableName(transMeta.environmentSubstitute(wTable.getText()));
		StepMetaInterface stepMetaInterface = stepMeta.getStepMetaInterface();
		try {
			targetFields = stepMetaInterface.getRequiredFields(transMeta);
		} catch (KettleException e) {
			new ErrorDialog(shell, Messages.getString("MySQLBulkLoaderDialog.DoMapping.UnableToFindTargetFields.Title"), Messages.getString("MySQLBulkLoaderDialog.DoMapping.UnableToFindTargetFields.Message"), e);
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
				message+=Messages.getString("MySQLBulkLoaderDialog.DoMapping.SomeSourceFieldsNotFound", missingSourceFields.toString())+Const.CR;
			}
			if (missingTargetFields.length()>0) {
				message+=Messages.getString("MySQLBulkLoaderDialog.DoMapping.SomeTargetFieldsNotFound", missingSourceFields.toString())+Const.CR;
			}
			message+=Const.CR;
			message+=Messages.getString("MySQLBulkLoaderDialog.DoMapping.SomeFieldsNotFoundContinue")+Const.CR;
			MessageDialog.setDefaultImage(GUIResource.getInstance().getImageSpoon());
			boolean goOn = MessageDialog.openConfirm(shell, Messages.getString("MySQLBulkLoaderDialog.DoMapping.SomeFieldsNotFoundTitle"), message);
			if (!goOn) {
				return;
			}
		}
		EnterMappingDialog d = new EnterMappingDialog(MySQLBulkLoaderDialog.this.shell, sourceFields.getFieldNames(), targetFields.getFieldNames(), mappings);
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
				item.setText(3, MySQLBulkLoaderMeta.getFieldFormatTypeDescription(input.getFieldFormatType()[i]));
			}
			wReturn.setRowNums();
			wReturn.optWidth(true);
		}
	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		int i;
		if(log.isDebug()) log.logDebug(toString(), Messages.getString("MySQLBulkLoaderDialog.Log.GettingKeyInfo")); //$NON-NLS-1$

		wEnclosure.setText(Const.NVL(input.getEnclosure(), ""));   //$NON-NLS-1$
		wDelimiter.setText(Const.NVL(input.getDelimiter(), ""));   //$NON-NLS-1$
		wEscapeChar.setText(Const.NVL(input.getEscapeChar(), ""));   //$NON-NLS-1$
		wCharSet.setText(Const.NVL(input.getEncoding(), ""));   //$NON-NLS-1$
		wReplace.setSelection(input.isReplacingData());
		wIgnore.setSelection(input.isIgnoringErrors());
		wBulkSize.setText(Const.NVL(input.getBulkSize(), ""));

		if (input.getFieldTable() != null) {
			for (i = 0; i < input.getFieldTable().length; i++)
			{
				TableItem item = wReturn.table.getItem(i);
				if (input.getFieldTable()[i] != null)
					item.setText(1, input.getFieldTable()[i]);
				if (input.getFieldStream()[i] != null)
					item.setText(2, input.getFieldStream()[i]);
				item.setText(3, MySQLBulkLoaderMeta.getFieldFormatTypeDescription(input.getFieldFormatType()[i]));
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
		if (input.getFifoFileName() != null) wFifoFile.setText(input.getFifoFileName());
		
		wStepname.selectAll();
		wReturn.setRowNums();
		wReturn.optWidth(true);
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
	private void cancel()
	{
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	private void getInfo(MySQLBulkLoaderMeta inf)
	{
		int nrfields = wReturn.nrNonEmpty();

		inf.allocate(nrfields);

		inf.setEnclosure( wEnclosure.getText() );
		inf.setDelimiter( wDelimiter.getText() );
		inf.setEscapeChar( wEscapeChar.getText() );
		inf.setEncoding( wCharSet.getText() );
		inf.setReplacingData( wReplace.getSelection() );
		inf.setIgnoringErrors( wIgnore.getSelection() );
		inf.setBulkSize( wBulkSize.getText() );

		if(log.isDebug()) log.logDebug(toString(), Messages.getString("MySQLBulkLoaderDialog.Log.FoundFields", "" + nrfields)); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < nrfields; i++)
		{
			TableItem item = wReturn.getNonEmpty(i);
			inf.getFieldTable()[i] = item.getText(1);
			inf.getFieldStream()[i] = item.getText(2);
			inf.getFieldFormatType()[i] = MySQLBulkLoaderMeta.getFieldFormatType(item.getText(3));
		}

        inf.setSchemaName( wSchema.getText() );
		inf.setTableName( wTable.getText() );
		inf.setDatabaseMeta(  transMeta.findDatabase(wConnection.getText()) );
		inf.setFifoFileName(wFifoFile.getText());
		
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
			mb.setMessage(Messages.getString("MySQLBulkLoaderDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("MySQLBulkLoaderDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
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
			if(log.isDebug()) log.logDebug(toString(), Messages.getString("MySQLBulkLoaderDialog.Log.LookingAtConnection") + inf.toString()); //$NON-NLS-1$

			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, transMeta.getDatabases());
            std.setSelectedSchema(wSchema.getText());
            std.setSelectedTable(wTable.getText());
            std.setSplitSchemaAndTable(true);
			if (std.open() != null)
			{
                wSchema.setText(Const.NVL(std.getSchemaName(), ""));
                wTable.setText(Const.NVL(std.getTableName(), ""));
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage(Messages.getString("MySQLBulkLoaderDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("MySQLBulkLoaderDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
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
			new ErrorDialog(shell, Messages.getString("MySQLBulkLoaderDialog.FailedToGetFields.DialogTitle"), //$NON-NLS-1$
					Messages.getString("MySQLBulkLoaderDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$
		}
	}

	// Generate code for create table...
	// Conversions done by Database
	private void create()
	{
		try
		{
			MySQLBulkLoaderMeta info = new MySQLBulkLoaderMeta();
			getInfo(info);

			String name = stepname; // new name might not yet be linked to other steps!
			StepMeta stepMeta = new StepMeta(Messages.getString("MySQLBulkLoaderDialog.StepMeta.Title"), name, info); //$NON-NLS-1$
			RowMetaInterface prev = transMeta.getPrevStepFields(stepname);

			SQLStatement sql = info.getSQLStatements(transMeta, stepMeta, prev);
			if (!sql.hasError())
			{
				if (sql.hasSQL())
				{
					SQLEditor sqledit = new SQLEditor(shell, SWT.NONE, info.getDatabaseMeta(), transMeta.getDbCache(),
							sql.getSQL());
					sqledit.open();
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
					mb.setMessage(Messages.getString("MySQLBulkLoaderDialog.NoSQLNeeds.DialogMessage")); //$NON-NLS-1$
					mb.setText(Messages.getString("MySQLBulkLoaderDialog.NoSQLNeeds.DialogTitle")); //$NON-NLS-1$
					mb.open();
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				mb.setMessage(sql.getError());
				mb.setText(Messages.getString("MySQLBulkLoaderDialog.SQLError.DialogTitle")); //$NON-NLS-1$
				mb.open();
			}
		}
		catch (KettleException ke)
		{
			new ErrorDialog(shell, Messages.getString("MySQLBulkLoaderDialog.CouldNotBuildSQL.DialogTitle"), //$NON-NLS-1$
					Messages.getString("MySQLBulkLoaderDialog.CouldNotBuildSQL.DialogMessage"), ke); //$NON-NLS-1$
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
						Database db = new Database(ci);
						db.shareVariablesWith(transMeta);
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

	public String toString()
	{
		return this.getClass().getName();
	}
}
