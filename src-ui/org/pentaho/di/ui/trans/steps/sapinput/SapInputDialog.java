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

package org.pentaho.di.ui.trans.steps.sapinput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.SAPR3DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.sapinput.SapInputMeta;
import org.pentaho.di.trans.steps.sapinput.SapOutputField;
import org.pentaho.di.trans.steps.sapinput.SapParameter;
import org.pentaho.di.trans.steps.sapinput.SapType;
import org.pentaho.di.trans.steps.sapinput.mock.SAPConnectionFactory;
import org.pentaho.di.trans.steps.sapinput.sap.SAPConnection;
import org.pentaho.di.trans.steps.sapinput.sap.SAPField;
import org.pentaho.di.trans.steps.sapinput.sap.SAPFunction;
import org.pentaho.di.trans.steps.sapinput.sap.SAPFunctionSignature;
import org.pentaho.di.trans.steps.sapinput.sap.SAPLibraryTester;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;


public class SapInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = SapInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CCombo       wConnection;

	private Label        wlFunction;
	private Text         wFunction;
	private Button       wbFunction;

	private Label        wlInput;
	private TableView    wInput;

	private Label        wlOutput;
	private TableView    wOutput;

	private Button wGet;
	private Listener lsGet;

	private SAPFunction function;
	private SapInputMeta input;
	
	// asc info
	private Button wAbout;
	private Link wAscLink;

	/**
	 * List of ColumnInfo that should have the field names of the selected database table
	 */
	private List<ColumnInfo> inputFieldColumns = new ArrayList<ColumnInfo>();
	
	/**
	 * List of ColumnInfo that should have the previous fields combo box
	 */
	private List<ColumnInfo> outputFieldColumns = new ArrayList<ColumnInfo>();
	
	/**
	 * all fields from the previous steps
	 */
	private RowMetaInterface prevFields = null;
	
	public SapInputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(SapInputMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, input);

        if (!SAPLibraryTester.isJCoLibAvailable()) {
        	int style = SWT.ICON_ERROR;
            
            MessageBox messageBox = new MessageBox(shell, style);
            messageBox.setMessage(BaseMessages.getString(PKG, "SapInputDialog.JCoLibNotFound"));
            messageBox.open();

            // dispose();
            // return stepname;
        }

        if (!SAPLibraryTester.isJCoImplAvailable()) {
        	int style = SWT.ICON_ERROR;
            
            MessageBox messageBox = new MessageBox(shell, style);
            messageBox.setMessage(BaseMessages.getString(PKG, "SapInputDialog.JCoImplNotFound"));
            messageBox.open();

            // dispose();
            // return stepname;
        }
        
		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};

		ModifyListener lsConnectionMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		backupChanged = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "SapInputDialog.shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "SapInputDialog.Stepname.Label")); //$NON-NLS-1$
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
		Control lastControl = wStepname;

		// Connection line
		//
		wConnection = addConnectionLine(shell, lastControl, middle, margin);
		List<String> items = new ArrayList<String>();
		for (DatabaseMeta dbMeta : transMeta.getDatabases()) {
			if (dbMeta.getDatabaseInterface() instanceof SAPR3DatabaseMeta) {
				items.add(dbMeta.getName());
			}
		}
		wConnection.setItems(items.toArray(new String[items.size()]));
		if (input.getDatabaseMeta()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsConnectionMod);
		lastControl = wConnection;
		
		// Function
		//
		wlFunction=new Label(shell, SWT.RIGHT);
		wlFunction.setText(BaseMessages.getString(PKG, "SapInputDialog.Function.Label")); //$NON-NLS-1$
 		props.setLook(wlFunction);
		FormData fdlFunction = new FormData();
		fdlFunction.left   = new FormAttachment(0, 0);
		fdlFunction.right  = new FormAttachment(middle, -margin);
		fdlFunction.top    = new FormAttachment(lastControl, margin);
		wlFunction.setLayoutData(fdlFunction);
		wbFunction = new Button(shell, SWT.PUSH);
		props.setLook(wbFunction);
		
		wbFunction.setText(BaseMessages.getString(PKG, "SapInputDialog.FindFunctionButton.Label")); //$NON-NLS-1$
		FormData fdbFunction = new FormData();
		fdbFunction.right  = new FormAttachment(100, 0);
		fdbFunction.top    = new FormAttachment(lastControl, margin);
		wbFunction.setLayoutData(fdbFunction);
		wbFunction.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getFunction(); }});
		
		wFunction=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFunction);
		wFunction.addModifyListener(lsMod);
		FormData fdFunction = new FormData();
		fdFunction.left   = new FormAttachment(middle, 0);
		fdFunction.right  = new FormAttachment(wbFunction, -margin);
		fdFunction.top    = new FormAttachment(lastControl, margin);
		wFunction.setLayoutData(fdFunction);
		lastControl = wFunction;

		// The parameter input fields...
		//
		wlInput=new Label(shell, SWT.NONE);
		wlInput.setText(BaseMessages.getString(PKG, "SapInputDialog.Input.Label")); //$NON-NLS-1$
 		props.setLook(wlInput);
		FormData fdlInput = new FormData();
		fdlInput.left  = new FormAttachment(0, 0);
		fdlInput.top   = new FormAttachment(lastControl, margin);
		wlInput.setLayoutData(fdlInput);

		ColumnInfo[] ciKey=new ColumnInfo[] {
			new ColumnInfo(BaseMessages.getString(PKG, "SapInputDialog.ColumnInfo.Field"),            ColumnInfo.COLUMN_TYPE_CCOMBO, new String[]{""},  false), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "SapInputDialog.ColumnInfo.SAPType"),          ColumnInfo.COLUMN_TYPE_CCOMBO, SapType.getDescriptions()),
			new ColumnInfo(BaseMessages.getString(PKG, "SapInputDialog.ColumnInfo.TableOrStruct"),    ColumnInfo.COLUMN_TYPE_TEXT, false,   false), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "SapInputDialog.ColumnInfo.SAPParameterName"), ColumnInfo.COLUMN_TYPE_TEXT, false,   false), //$NON-NLS-1$
		};
		inputFieldColumns.add(ciKey[0]);
		
		wInput=new TableView(transMeta, shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
						      ciKey, 
						      input.getParameters().size(),  
						      lsMod,
							  props
						      );

		FormData fdInput = new FormData();
		fdInput.left  = new FormAttachment(0, 0);
		fdInput.top   = new FormAttachment(wlInput, margin);
		fdInput.right = new FormAttachment(100, 0);
		fdInput.bottom= new FormAttachment(40, 0);
		wInput.setLayoutData(fdInput);
		lastControl = wInput;

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "SapInputDialog.GetFields.Button")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$
		wAbout=new Button(shell, SWT.PUSH);
		wAbout.setText(BaseMessages.getString(PKG, "SapInputDialog.About.Button"));
		setButtonPositions(new Button[] { wOK, wCancel, wAbout , wGet, }, margin, null);
		
		
		// The output fields...
		//
        wlOutput=new Label(shell, SWT.NONE);
        wlOutput.setText(BaseMessages.getString(PKG, "SapInputDialog.Output.Label")); //$NON-NLS-1$
        props.setLook(wlOutput);
        FormData fdlOutput = new FormData();
        fdlOutput.left  = new FormAttachment(0, 0);
        fdlOutput.top   = new FormAttachment(wInput, margin);
        wlOutput.setLayoutData(fdlOutput);
        
        ColumnInfo[] ciReturn=new ColumnInfo[] {
	        new ColumnInfo(BaseMessages.getString(PKG, "SapInputDialog.ColumnInfo.SAPField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[]{},  false), //$NON-NLS-1$
	        new ColumnInfo(BaseMessages.getString(PKG, "SapInputDialog.ColumnInfo.SAPType"),  ColumnInfo.COLUMN_TYPE_CCOMBO, SapType.getDescriptions(),  false), //$NON-NLS-1$
	        new ColumnInfo(BaseMessages.getString(PKG, "SapInputDialog.ColumnInfo.TableOrStruct"),  ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
	        new ColumnInfo(BaseMessages.getString(PKG, "SapInputDialog.ColumnInfo.NewName"),  ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
	        new ColumnInfo(BaseMessages.getString(PKG, "SapInputDialog.ColumnInfo.TargetType"), ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes()), //$NON-NLS-1$
        };
		outputFieldColumns.add(ciReturn[0]);
                
        wOutput=new TableView(transMeta, shell, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
                              ciReturn, 
                              input.getOutputFields().size(),  
                              lsMod,
                              props
                              );

		
        FormData fdOutput = new FormData();
        fdOutput.left  = new FormAttachment(0, 0);
        fdOutput.top   = new FormAttachment(wlOutput, margin);
        fdOutput.right = new FormAttachment(100, 0);
        fdOutput.bottom= new FormAttachment(wOK, -8*margin);
        wOutput.setLayoutData(fdOutput);
        lastControl = wOutput;

        
        this.wAscLink = new Link(this.shell, SWT.NONE);
        FormData fdAscLink = new FormData();
		fdAscLink.left = new FormAttachment(0, 0);
        fdAscLink.top = new FormAttachment(wOutput, margin);
        wAscLink.setLayoutData(fdAscLink);
		this.wAscLink.setText(BaseMessages.getString(PKG, "SapInputDialog.Provided.Info"));
		lastControl = wAscLink;
		
		
        // Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();       } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		Listener lsAbout    = new Listener() { public void handleEvent(Event e) { about();     } };
            
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wCancel.addListener(SWT.Selection, lsCancel);
		this.wAbout.addListener(SWT.Selection, lsAbout);
		
		
		this.wAscLink.addListener(SWT.Selection, new Listener() {
            public void handleEvent(final Event event) {
                Program.launch(event.text);
            }
        });
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wFunction.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();

		// Set the shell size, based upon previous time...
		setSize();
				
		input.setChanged(backupChanged);

		setComboValues();
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	protected void getFunction() {
		DatabaseMeta databaseMeta = transMeta.findDatabase(wConnection.getText());
		if (databaseMeta==null) {
			showDatabaseWarning(false);
			return;
		}
		SapFunctionBrowser browser = new SapFunctionBrowser(shell, transMeta, SWT.NONE, databaseMeta, wFunction.getText());
		function = browser.open();
		if (function!=null) {
			get();
		}
	}

	private void setComboValues() {
		Runnable fieldLoader = new Runnable() {
			public void run() {
				try {
					prevFields = transMeta.getPrevStepFields(stepname);
				} catch (KettleException e) {
					prevFields = new RowMeta();
					String msg = BaseMessages.getString(PKG, "SapInputDialog.DoMapping.UnableToFindInput");
					logError(msg);
				}
				String[] prevStepFieldNames = prevFields.getFieldNames();
				Arrays.sort(prevStepFieldNames);
				for (int i = 0; i < inputFieldColumns.size(); i++) {
					ColumnInfo colInfo = (ColumnInfo) outputFieldColumns.get(i);
					colInfo.setComboValues(prevStepFieldNames);
				}
			}
		};
		new Thread(fieldLoader).start();
	}
	
	
    /**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		logDebug(BaseMessages.getString(PKG, "SapInputDialog.Log.GettingKeyInfo")); //$NON-NLS-1$
		
		// The database connection name...
		//
		if (input.getDatabaseMeta()!=null)   wConnection.setText(input.getDatabaseMeta().getName());
		else if (transMeta.nrDatabases()==1)
		{
			wConnection.setText( transMeta.getDatabase(0).getName() );
		}
		
		// The name of the function to use
		//
		function = input.getFunction();
		if (input.getFunction()!=null) {
			wFunction.setText(Const.NVL(input.getFunction().getName(), "")); //$NON-NLS-1$
		}
		
		// The parameters...
		//
		for (int i=0;i<input.getParameters().size();i++) {
			SapParameter parameter = input.getParameters().get(i);
			TableItem item = wInput.table.getItem(i);
			int colnr=1;
			item.setText(colnr++, Const.NVL(parameter.getFieldName(), ""));
			item.setText(colnr++, parameter.getSapType().getDescription());
			item.setText(colnr++, Const.NVL(parameter.getTableName(), ""));
			item.setText(colnr++, Const.NVL(parameter.getParameterName(), ""));
		}
		wInput.setRowNums();
		wInput.optWidth(true);
		
		// The parameters...
		//
		for (int i=0;i<input.getOutputFields().size();i++) {
			SapOutputField outputField = input.getOutputFields().get(i);
			TableItem item = wOutput.table.getItem(i);
			int colnr=1;
			item.setText(colnr++, Const.NVL(outputField.getSapFieldName(), ""));
			item.setText(colnr++, outputField.getSapType().getDescription());
			item.setText(colnr++, Const.NVL(outputField.getTableName(), ""));
			item.setText(colnr++, Const.NVL(outputField.getNewName(), ""));
			item.setText(colnr++, ValueMeta.getTypeDesc(outputField.getTargetType()));
		}
		wOutput.setRowNums();
		wOutput.optWidth(true);
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

		stepname = wStepname.getText(); // return value

		if (transMeta.findDatabase(wConnection.getText())==null)
		{
			int answer = showDatabaseWarning(true);
			if (answer==SWT.CANCEL) {
				return;
			}
		}
		
		getInfo(input);
		
		dispose();
	}
	
	private void about() {
		new SapInputAboutDialog(SapInputDialog.this.shell).open();		
	}

	private int showDatabaseWarning(boolean includeCancel) {
		MessageBox mb = new MessageBox(shell, SWT.OK | ( includeCancel ? SWT.CANCEL : SWT.NONE ) | SWT.ICON_ERROR );
		mb.setMessage(BaseMessages.getString(PKG, "SapInputDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
		mb.setText(BaseMessages.getString(PKG, "SapInputDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
		return mb.open();
	}

	private void getInfo(SapInputMeta meta) {
		meta.setDatabaseMeta( transMeta.findDatabase(wConnection.getText()) );
		meta.setFunction(function);
		
		// Grab the parameters...
		//
		meta.getParameters().clear();
		int nrParameters = wInput.nrNonEmpty();
		for (int i=0;i<nrParameters;i++) {
			TableItem item = wInput.getNonEmpty(i);
			int colnr=1;
			String fieldName = item.getText(colnr++);
			SapType sapType = SapType.findTypeForDescription( item.getText(colnr++) );
			String tableName = item.getText(colnr++);
			String parameterName = item.getText(colnr++);
			meta.getParameters().add( new SapParameter(fieldName, sapType, tableName, parameterName) );
		}
		
		// and the output fields.
		//
		meta.getOutputFields().clear();
		int nrFields = wOutput.nrNonEmpty();
		for (int i=0;i<nrFields;i++) {
			TableItem item = wOutput.getNonEmpty(i);
			int colnr=1;
			String sapFieldName = item.getText(colnr++);
			SapType sapType = SapType.findTypeForDescription( item.getText(colnr++) );
			String tableName = item.getText(colnr++);
			String newName = item.getText(colnr++);
			int targetType = ValueMeta.getType( item.getText(colnr++) );
			meta.getOutputFields().add( new SapOutputField(sapFieldName, sapType, tableName, newName, targetType) );
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
                BaseStepDialog.getFieldsFromPrevious(r, wInput, 1, new int[] { 1, 3}, new int[] {}, -1, -1, listener);
			}

			DatabaseMeta databaseMeta = transMeta.findDatabase(wConnection.getText());
			if (databaseMeta==null) {
				showDatabaseWarning(false);
				return;
			}
			
			// Fill in the parameters too
			//
			if (function!=null) {
				
				wFunction.setText(function.getName());
				
				if (wInput.nrNonEmpty()!=0 || wOutput.nrNonEmpty()!=0) {
					MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
					mb.setMessage(BaseMessages.getString(PKG, "SapInputDialog.ClearInputOutput.DialogMessage")); //$NON-NLS-1$
					mb.setText(BaseMessages.getString(PKG, "SapInputDialog.ClearInputOutput.DialogTitle")); //$NON-NLS-1$
					int answer = mb.open();
					if (answer==SWT.NO) {
						return;
					}
				}
				
				wInput.clearAll(false);
				wOutput.clearAll(false);
				Cursor hourGlass = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
				SAPConnection sc = SAPConnectionFactory.create();
				try {
					shell.setCursor(hourGlass);
					sc.open(databaseMeta);
					SAPFunctionSignature signature = sc.getFunctionSignature(function);
					
					// Populate the input view
					// TODO: clean this up a bit, feels a bit messy
					//
					int rownr=0;
					for (SAPField field : signature.getInput()) {
						TableItem item;
						if (rownr==0) {
							item = wInput.table.getItem(0);
						} else {
							item = new TableItem(wInput.table, SWT.NONE);
						}
						rownr++;
						
						SapType type = getSapType(field);
						
						int colnr=1;
						item.setText(colnr++, Const.NVL(field.getName(), ""));
						item.setText(colnr++, type==null ? "" : type.getDescription());
						item.setText(colnr++, Const.NVL(field.getTable(), ""));
						item.setText(colnr++, Const.NVL(field.getName(), ""));
					}
					wInput.setRowNums();
					wInput.optWidth(true);

					// Get the output rows
					//
					rownr=0;
					for (SAPField field : signature.getOutput()) {
						TableItem item;
						if (rownr==0) {
							item = wOutput.table.getItem(0);
						} else {
							item = new TableItem(wOutput.table, SWT.NONE);
						}
						rownr++;
						
						SapType type = getSapType(field);
						
						int colnr=1;
						item.setText(colnr++, Const.NVL(field.getName(), ""));
						item.setText(colnr++, type==null ? "" : type.getDescription());
						item.setText(colnr++, Const.NVL(field.getTable(), ""));
						item.setText(colnr++, Const.NVL(field.getName(), ""));
						item.setText(colnr++, ValueMeta.getTypeDesc(ValueMetaInterface.TYPE_STRING)); // TODO: where do we get a data type from?
					}
					wOutput.setRowNums();
					wOutput.optWidth(true);

				} catch(Exception e) {
					throw new KettleException(e);
				} finally {
					sc.close();
					shell.setCursor(null);
					hourGlass.dispose();
				}
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "SapInputDialog.GetFieldsFailed.DialogTitle"), BaseMessages.getString(PKG, "SapInputDialog.GetFieldsFailed.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
	
	private SapType getSapType(SAPField field) {
		String type = field.getType();
		if (type!=null && type.startsWith("input_")) {
			type = type.substring("input_".length());
		} else if (type!=null && type.startsWith("output_")) {
			type = type.substring("output_".length());
		}
		return SapType.findTypeForCode(type);
	}

	public String toString()
	{
		return stepname;
	}
}
