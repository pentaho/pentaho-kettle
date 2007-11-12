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
 * Created on 5-aug-2004
 *
 */

//import java.text.DateFormat;
//import java.util.Date;

package org.pentaho.di.ui.trans.steps.databasejoin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.databasejoin.DatabaseJoinMeta;
import org.pentaho.di.trans.steps.databasejoin.Messages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class DatabaseJoinDialog extends BaseStepDialog implements StepDialogInterface
{
	private CCombo       wConnection;

	private Label        wlSQL;
	private Text         wSQL;
	private FormData     fdlSQL, fdSQL;

	private Label        wlLimit;
	private Text         wLimit;
	private FormData     fdlLimit, fdLimit;
	
	private Label        wlOuter;
	private Button       wOuter;
	private FormData     fdlOuter, fdOuter;

	private Label        wlParam;
	private TableView    wParam;
	private FormData     fdlParam, fdParam;

	private Button wGet;
	private Listener lsGet;

	private DatabaseJoinMeta input;

	public DatabaseJoinDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(DatabaseJoinMeta)in;
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
		backupChanged = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("DatabaseJoinDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("DatabaseJoinDialog.Stepname.Label")); //$NON-NLS-1$
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

		// SQL editor...
		wlSQL=new Label(shell, SWT.NONE);
		wlSQL.setText(Messages.getString("DatabaseJoinDialog.SQL.Label")); //$NON-NLS-1$
 		props.setLook(wlSQL);
		fdlSQL=new FormData();
		fdlSQL.left = new FormAttachment(0, 0);
		fdlSQL.top  = new FormAttachment(wConnection, margin*2);
		wlSQL.setLayoutData(fdlSQL);

		wSQL=new Text(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
 		props.setLook(wSQL, Props.WIDGET_STYLE_FIXED);
		wSQL.addModifyListener(lsMod);
		fdSQL=new FormData();
		fdSQL.left  = new FormAttachment(0, 0);
		fdSQL.top   = new FormAttachment(wlSQL, margin  );
		fdSQL.right = new FormAttachment(100, 0);
		fdSQL.bottom= new FormAttachment(60, 0     );
		wSQL.setLayoutData(fdSQL);
		
		// Limit the number of lines returns
		wlLimit=new Label(shell, SWT.RIGHT);
		wlLimit.setText(Messages.getString("DatabaseJoinDialog.Limit.Label")); //$NON-NLS-1$
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left   = new FormAttachment(0, 0);
		fdlLimit.right  = new FormAttachment(middle, -margin);
		fdlLimit.top    = new FormAttachment(wSQL, margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left   = new FormAttachment(middle, 0);
		fdLimit.right  = new FormAttachment(100, 0);
		fdLimit.top    = new FormAttachment(wSQL, margin);
		wLimit.setLayoutData(fdLimit);

		// Outer join?
		wlOuter=new Label(shell, SWT.RIGHT);
		wlOuter.setText(Messages.getString("DatabaseJoinDialog.Outerjoin.Label")); //$NON-NLS-1$
		wlOuter.setToolTipText(Messages.getString("DatabaseJoinDialog.Outerjoin.Tooltip")); //$NON-NLS-1$
 		props.setLook(wlOuter);
		fdlOuter=new FormData();
		fdlOuter.left = new FormAttachment(0, 0);
		fdlOuter.right= new FormAttachment(middle, -margin);
		fdlOuter.top  = new FormAttachment(wLimit, margin);
		wlOuter.setLayoutData(fdlOuter);
		wOuter=new Button(shell, SWT.CHECK);
 		props.setLook(wOuter);
		wOuter.setToolTipText(wlOuter.getToolTipText());
		fdOuter=new FormData();
		fdOuter.left = new FormAttachment(middle, 0);
		fdOuter.top  = new FormAttachment(wLimit, margin);
		wOuter.setLayoutData(fdOuter);
		wOuter.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(Messages.getString("DatabaseJoinDialog.GetFields.Button")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel , wGet}, margin, null);

		// The parameters
		wlParam=new Label(shell, SWT.NONE);
		wlParam.setText(Messages.getString("DatabaseJoinDialog.Param.Label")); //$NON-NLS-1$
 		props.setLook(wlParam);
		fdlParam=new FormData();
		fdlParam.left  = new FormAttachment(0, 0);
		fdlParam.top   = new FormAttachment(wOuter, margin);
		wlParam.setLayoutData(fdlParam);

		int nrKeyCols=2;
		int nrKeyRows=(input.getParameterField()!=null?input.getParameterField().length:1);
		
		ColumnInfo[] ciKey=new ColumnInfo[nrKeyCols];
		ciKey[0]=new ColumnInfo(Messages.getString("DatabaseJoinDialog.ColumnInfo.ParameterFieldname"),  ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
		ciKey[1]=new ColumnInfo(Messages.getString("DatabaseJoinDialog.ColumnInfo.ParameterType"),       ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes() ); //$NON-NLS-1$
		
		wParam=new TableView(transMeta, shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
						      ciKey, 
						      nrKeyRows,  
						      lsMod,
							  props
						      );

		fdParam=new FormData();
		fdParam.left  = new FormAttachment(0, 0);
		fdParam.top   = new FormAttachment(wlParam, margin);
		fdParam.right = new FormAttachment(100, 0);
		fdParam.bottom= new FormAttachment(wOK, -2*margin);
		wParam.setLayoutData(fdParam);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();       } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wLimit.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		// Set the shell size, based upon previous time...
		setSize();
				
		getData();
		input.setChanged(backupChanged);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		int i;
		log.logDebug(toString(), Messages.getString("DatabaseJoinDialog.Log.GettingKeyInfo")); //$NON-NLS-1$
		
		wSQL.setText( Const.NVL(input.getSql(), ""));
		wLimit.setText(""+input.getRowLimit()); //$NON-NLS-1$
		wOuter.setSelection(input.isOuterJoin());
		
		if (input.getParameterField()!=null)
		for (i=0;i<input.getParameterField().length;i++)
		{
			TableItem item = wParam.table.getItem(i);
			if (input.getParameterField()[i]  !=null) item.setText(1, input.getParameterField()[i]);
			if (input.getParameterType() [i]  !=0   ) item.setText(2, ValueMeta.getTypeDesc( input.getParameterType()[i] ));
		}
		
		if (input.getDatabaseMeta()!=null)   wConnection.setText(input.getDatabaseMeta().getName());
		else if (transMeta.nrDatabases()==1)
		{
			wConnection.setText( transMeta.getDatabase(0).getName() );
		}

		wStepname.selectAll();
		wParam.setRowNums();
		wParam.optWidth(true);
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

		int nrparam  = wParam.nrNonEmpty();
		
		input.allocate(nrparam);
		
		input.setRowLimit( Const.toInt( wLimit.getText(), 0) );
		input.setSql( wSQL.getText() );
		
		input.setOuterJoin( wOuter.getSelection() );
		
		log.logDebug(toString(), Messages.getString("DatabaseJoinDialog.Log.ParametersFound")+nrparam+" parameters"); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0;i<nrparam;i++)
		{
			TableItem item = wParam.getNonEmpty(i);
			input.getParameterField()[i]   = item.getText(1);
			input.getParameterType() [i]   = ValueMeta.getType( item.getText(2) );
		}

		input.setDatabaseMeta( transMeta.findDatabase(wConnection.getText()) );

		stepname = wStepname.getText(); // return value

		if (transMeta.findDatabase(wConnection.getText())==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("DatabaseJoinDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("DatabaseJoinDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}
		
		dispose();
	}

	private void get()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
			{
                BaseStepDialog.getFieldsFromPrevious(r, wParam, 1, new int[] { 1 }, new int[] { 2 }, -1, -1, null);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, Messages.getString("DatabaseJoinDialog.GetFieldsFailed.DialogTitle"), Messages.getString("DatabaseJoinDialog.GetFieldsFailed.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
}
