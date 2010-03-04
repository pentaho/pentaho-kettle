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

package org.pentaho.di.ui.trans.steps.sapinput;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.sapinput.SapInputMeta;
import org.pentaho.di.trans.steps.sapinput.mock.SAPConnectionFactory;
import org.pentaho.di.trans.steps.sapinput.sap.SAPConnection;
import org.pentaho.di.trans.steps.sapinput.sap.SAPFunction;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


/**
 * Displays results of a search operation in a list of SAP functions
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class SapFunctionBrowser extends Dialog
{
	private static Class<?> PKG = SapInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label  wlFunction;
	private Text   wFunction;
	private Button wbFunction;

    private TableView wResult;

    private Button wOK;
    private Button wCancel;

    private Shell shell;
    private PropsUI props;

	private DatabaseMeta	sapConnection;
	private String	        searchString;
	private VariableSpace	space;
	private SAPFunction     function;
	private List<SAPFunction> functionList;

    public SapFunctionBrowser(Shell parent, VariableSpace space, int style, DatabaseMeta sapConnection, String searchString)
    {
        super(parent, style);
        this.space = space;
        this.sapConnection = sapConnection;
        this.searchString = searchString;
        props = PropsUI.getInstance();
        functionList = new ArrayList<SAPFunction>(); // Empty by default...
    }

    public SAPFunction open()
    {
        Shell parent = getParent();
        Display display = parent.getDisplay();
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
        props.setLook(shell);
		shell.setImage(GUIResource.getInstance().getImageSpoon());
		int middle = Const.MIDDLE_PCT;
		int margin = Const.MARGIN;

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "SapFunctionBrowser.Title"));

		// Function
		//
		wlFunction=new Label(shell, SWT.RIGHT);
		wlFunction.setText(BaseMessages.getString(PKG, "SapInputDialog.Function.Label")); //$NON-NLS-1$
 		props.setLook(wlFunction);
		FormData fdlFunction = new FormData();
		fdlFunction.left   = new FormAttachment(0, 0);
		fdlFunction.right  = new FormAttachment(middle, -margin);
		fdlFunction.top    = new FormAttachment(0, 0);
		wlFunction.setLayoutData(fdlFunction);
		wbFunction = new Button(shell, SWT.PUSH);
		props.setLook(wbFunction);
		
		wbFunction.setText(BaseMessages.getString(PKG, "SapInputDialog.FindFunctionButton.Label")); //$NON-NLS-1$
		FormData fdbFunction = new FormData();
		fdbFunction.right  = new FormAttachment(100, 0);
		fdbFunction.top    = new FormAttachment(0, 0);
		wbFunction.setLayoutData(fdbFunction);
		wbFunction.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) {
			searchString = wFunction.getText();
			getData(); 
		}});
		
		wFunction=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFunction);
		FormData fdFunction = new FormData();
		fdFunction.left   = new FormAttachment(middle, 0);
		fdFunction.right  = new FormAttachment(wbFunction, -margin);
		fdFunction.top    = new FormAttachment(0, margin);
		wFunction.setLayoutData(fdFunction);
		Control lastControl = wFunction;

		// The buttons at the bottom of the dialog
		//
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wOK.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { ok(); } });
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
        wCancel.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { cancel(); } });

        // Position the buttons...
        //
        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel, }, Const.MARGIN, null);

		// The search results...
		//
        ColumnInfo[] columns = new ColumnInfo[] {
        	new ColumnInfo(BaseMessages.getString(PKG, "SapFunctionBrowser.ResultView.Name.Column"), ColumnInfo.COLUMN_TYPE_TEXT, false, true),
        	new ColumnInfo(BaseMessages.getString(PKG, "SapFunctionBrowser.ResultView.Groupname.Column"), ColumnInfo.COLUMN_TYPE_TEXT, false, true),
        	new ColumnInfo(BaseMessages.getString(PKG, "SapFunctionBrowser.ResultView.Application.Column"), ColumnInfo.COLUMN_TYPE_TEXT, false, true),
        	new ColumnInfo(BaseMessages.getString(PKG, "SapFunctionBrowser.ResultView.Description.Column"), ColumnInfo.COLUMN_TYPE_TEXT, false, true),
        };

        wResult = new TableView(space, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, columns, 0, null, props);
        wResult.setSortable(true);

        FormData fdResults = new FormData();
        fdResults.left = new FormAttachment(0, 0);
        fdResults.top = new FormAttachment(lastControl, margin);
        fdResults.right = new FormAttachment(100, 0);
        fdResults.bottom = new FormAttachment(wOK, -3*margin);
        wResult.setLayoutData(fdResults);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

        getData();

        // Set the shell size, based upon previous time...
        BaseStepDialog.setSize(shell);

        shell.open();

        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
        }
        
        return function;
    }
    
    private void dispose() {
        WindowProperty winprop = new WindowProperty(shell);
        props.setScreen(winprop);
    	shell.dispose();
    }

    protected void cancel() {
    	function = null;
        dispose();
	}

	protected void ok() {
		function = null;
		int selectionIndex = wResult.getSelectionIndex();
		if (selectionIndex>=0 && selectionIndex<functionList.size()) {
			function = functionList.get(selectionIndex);
		}
		dispose();
	}
	
	protected void find(String searchString) {
		this.searchString = searchString;
		SAPConnection sc = SAPConnectionFactory.create();
		try {
			sc.open(sapConnection);
			functionList = new ArrayList<SAPFunction>(sc.getFunctions(searchString));
		} catch(Exception e) {
			new ErrorDialog(shell, 
					BaseMessages.getString(PKG, "SapFunctionBrowser.ExceptionDialog.ErrorDuringSearch.Title"), 
					BaseMessages.getString(PKG, "SapFunctionBrowser.ExceptionDialog.ErrorDuringSearch.Message"), 
					e
				);
		} finally {
			sc.close();
		}
	}

	/**
     * Copy information from the meta-data input to the dialog fields.
     */
    private void getData()
    {
        shell.getDisplay().asyncExec(new Runnable()
        {
            public void run()
            {
            	Cursor hourGlass = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
            
            	try  {
	            	shell.setCursor(hourGlass);
	            	if (Const.isEmpty(searchString)) {
	            		return;
	            	}
	            	wFunction.setText(searchString);
	            	find(searchString);
	            	
	            	// Clear out everything, always leaves one row
	            	//
	            	wResult.clearAll(false);
	            	
	            	for (int i=0;i<functionList.size();i++) {
	            		SAPFunction sapFunction = functionList.get(i);
	            		TableItem item;
	            		if (i==0) {
	            			item = wResult.table.getItem(0);
	            		} else {
	            			item = new TableItem(wResult.table, SWT.NONE);		
	            		}
	            		int colnr=1;
	            		item.setText(colnr++, Const.NVL(sapFunction.getName(), ""));
	            		item.setText(colnr++, Const.NVL(sapFunction.getGroup(), ""));
	            		item.setText(colnr++, Const.NVL(sapFunction.getApplication(), ""));
	            		item.setText(colnr++, Const.NVL(sapFunction.getDescription(), ""));
	            	}
	            	wResult.setRowNums();
	            	wResult.optWidth(true);
	            }
            	finally {
	            	shell.setCursor(null);
	            	hourGlass.dispose();
            	}
            } 
        });
    }
}
