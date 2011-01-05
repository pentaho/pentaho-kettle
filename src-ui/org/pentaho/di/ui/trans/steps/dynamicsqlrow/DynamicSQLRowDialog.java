 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar HASSAN.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/



package org.pentaho.di.ui.trans.steps.dynamicsqlrow;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.graphics.Cursor;
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
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.dynamicsqlrow.DynamicSQLRowMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.steps.tableinput.SQLValuesHighlight;


public class DynamicSQLRowDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = DynamicSQLRowMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private boolean gotPreviousFields=false;
	
	private CCombo       wConnection;

	private Label        wlSQL;
	private StyledTextComp   wSQL;
	private FormData     fdlSQL, fdSQL;

	private Label        wlLimit;
	private Text         wLimit;
	private FormData     fdlLimit, fdLimit;
	
	private Label        wlOuter;
	private Button       wOuter;
	private FormData     fdlOuter, fdOuter;
	
	private Label        wluseVars;
	private Button       wuseVars;
	private FormData     fdluseVars, fduseVars;

	private Label        wlPosition;
	private FormData     fdlPosition;
	
	private Label        wlSQLFieldName;
	private CCombo       wSQLFieldName;
	private FormData     fdlSQLFieldName, fdSQLFieldName;
	
	private Label        wlqueryOnlyOnChange;
	private Button       wqueryOnlyOnChange;
	private FormData     fdlqueryOnlyOnChange, fdqueryOnlyOnChange;

	private DynamicSQLRowMeta input;
	
	
	public DynamicSQLRowDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(DynamicSQLRowMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "DynamicSQLRowDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "DynamicSQLRowDialog.Stepname.Label")); //$NON-NLS-1$
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
		
		// SQLFieldName field
		wlSQLFieldName=new Label(shell, SWT.RIGHT);
		wlSQLFieldName.setText(BaseMessages.getString(PKG, "DynamicSQLRowDialog.SQLFieldName.Label")); //$NON-NLS-1$
 		props.setLook(wlSQLFieldName);
		fdlSQLFieldName=new FormData();
		fdlSQLFieldName.left = new FormAttachment(0, 0);
		fdlSQLFieldName.right= new FormAttachment(middle, -margin);
		fdlSQLFieldName.top  = new FormAttachment(wConnection, 2*margin);
		wlSQLFieldName.setLayoutData(fdlSQLFieldName);
		wSQLFieldName=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
		wSQLFieldName.setEditable(true);
 		props.setLook(wSQLFieldName);
 		wSQLFieldName.addModifyListener(lsMod);
		fdSQLFieldName=new FormData();
		fdSQLFieldName.left = new FormAttachment(middle, 0);
		fdSQLFieldName.top  = new FormAttachment(wConnection, 2*margin);
		fdSQLFieldName.right= new FormAttachment(100, -margin);
		wSQLFieldName.setLayoutData(fdSQLFieldName);
		wSQLFieldName.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                get();
                shell.setCursor(null);
                busy.dispose();
            }
        }
    );
		
		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		// Limit the number of lines returns
		wlLimit=new Label(shell, SWT.RIGHT);
		wlLimit.setText(BaseMessages.getString(PKG, "DynamicSQLRowDialog.Limit.Label")); //$NON-NLS-1$
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left   = new FormAttachment(0, 0);
		fdlLimit.right  = new FormAttachment(middle, -margin);
		fdlLimit.top    = new FormAttachment(wSQLFieldName, margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left   = new FormAttachment(middle, 0);
		fdLimit.right  = new FormAttachment(100, 0);
		fdLimit.top    = new FormAttachment(wSQLFieldName, margin);
		wLimit.setLayoutData(fdLimit);

		// Outer join?
		wlOuter=new Label(shell, SWT.RIGHT);
		wlOuter.setText(BaseMessages.getString(PKG, "DynamicSQLRowDialog.Outerjoin.Label")); //$NON-NLS-1$
		wlOuter.setToolTipText(BaseMessages.getString(PKG, "DynamicSQLRowDialog.Outerjoin.Tooltip")); //$NON-NLS-1$
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
		// useVars ?
		wluseVars=new Label(shell, SWT.RIGHT);
		wluseVars.setText(BaseMessages.getString(PKG, "DynamicSQLRowDialog.useVarsjoin.Label")); //$NON-NLS-1$
		wluseVars.setToolTipText(BaseMessages.getString(PKG, "DynamicSQLRowDialog.useVarsjoin.Tooltip")); //$NON-NLS-1$
 		props.setLook(wluseVars);
		fdluseVars=new FormData();
		fdluseVars.left = new FormAttachment(0, 0);
		fdluseVars.right= new FormAttachment(middle, -margin);
		fdluseVars.top  = new FormAttachment(wOuter, margin);
		wluseVars.setLayoutData(fdluseVars);
		wuseVars=new Button(shell, SWT.CHECK);
 		props.setLook(wuseVars);
		wuseVars.setToolTipText(wluseVars.getToolTipText());
		fduseVars=new FormData();
		fduseVars.left = new FormAttachment(middle, 0);
		fduseVars.top  = new FormAttachment(wOuter, margin);
		wuseVars.setLayoutData(fduseVars);
		wuseVars.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);
		
		// queryOnlyOnChange ?
		wlqueryOnlyOnChange=new Label(shell, SWT.RIGHT);
		wlqueryOnlyOnChange.setText(BaseMessages.getString(PKG, "DynamicSQLRowDialog.queryOnlyOnChangejoin.Label")); //$NON-NLS-1$
		wlqueryOnlyOnChange.setToolTipText(BaseMessages.getString(PKG, "DynamicSQLRowDialog.queryOnlyOnChangejoin.Tooltip")); //$NON-NLS-1$
 		props.setLook(wlqueryOnlyOnChange);
		fdlqueryOnlyOnChange=new FormData();
		fdlqueryOnlyOnChange.left = new FormAttachment(0, 0);
		fdlqueryOnlyOnChange.right= new FormAttachment(middle, -margin);
		fdlqueryOnlyOnChange.top  = new FormAttachment(wuseVars, margin);
		wlqueryOnlyOnChange.setLayoutData(fdlqueryOnlyOnChange);
		wqueryOnlyOnChange=new Button(shell, SWT.CHECK);
 		props.setLook(wqueryOnlyOnChange);
		wqueryOnlyOnChange.setToolTipText(wlqueryOnlyOnChange.getToolTipText());
		fdqueryOnlyOnChange=new FormData();
		fdqueryOnlyOnChange.left = new FormAttachment(middle, 0);
		fdqueryOnlyOnChange.top  = new FormAttachment(wuseVars, margin);
		wqueryOnlyOnChange.setLayoutData(fdqueryOnlyOnChange);
		wqueryOnlyOnChange.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);

		
		
		// SQL editor...
		wlSQL=new Label(shell, SWT.NONE);
		wlSQL.setText(BaseMessages.getString(PKG, "DynamicSQLRowDialog.SQL.Label")); //$NON-NLS-1$
 		props.setLook(wlSQL);
		fdlSQL=new FormData();
		fdlSQL.left = new FormAttachment(0, 0);
		fdlSQL.top  = new FormAttachment(wqueryOnlyOnChange, margin);
		wlSQL.setLayoutData(fdlSQL);

		wSQL=new StyledTextComp(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
 		props.setLook(wSQL, Props.WIDGET_STYLE_FIXED);
		fdSQL=new FormData();
		fdSQL.left  = new FormAttachment(0, 0);
		fdSQL.top   = new FormAttachment(wlSQL, margin );
		fdSQL.right = new FormAttachment(100, 0);
		fdSQL.bottom= new FormAttachment(wOK, -4*margin);
		wSQL.setLayoutData(fdSQL);

		
		wSQL.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent arg0)
            {
                setPosition();
            }

	        }
	    );
			
		
		wSQL.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e) { setPosition(); }
			public void keyReleased(KeyEvent e) { setPosition(); }
			} 
		);
		wSQL.addFocusListener(new FocusAdapter(){
			public void focusGained(FocusEvent e) { setPosition(); }
			public void focusLost(FocusEvent e) { setPosition(); }
			}
		);
		wSQL.addMouseListener(new MouseAdapter(){
			public void mouseDoubleClick(MouseEvent e) { setPosition(); }
			public void mouseDown(MouseEvent e) { setPosition(); }
			public void mouseUp(MouseEvent e) { setPosition(); }
			}
		);
		
		wSQL.addModifyListener(lsMod);
		
		// Text Higlighting
		wSQL.addLineStyleListener(new SQLValuesHighlight());
		
		wlPosition=new Label(shell, SWT.NONE);
		//wlPosition.setText(BaseMessages.getString(PKG, "DynamicSQLRowDialog.Position.Label")); 
		props.setLook(wlPosition);
		fdlPosition=new FormData();
		fdlPosition.left  = new FormAttachment(0,0);
		fdlPosition.top = new FormAttachment(wSQL, margin);
		fdlPosition.right = new FormAttachment(100, 0);
		wlPosition.setLayoutData(fdlPosition);	
		
	

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
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

		wlPosition.setText(BaseMessages.getString(PKG, "DynamicSQLRowDialog.Position.Label",""+linenr, ""+colnr));

	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "DynamicSQLRowDialog.Log.GettingKeyInfo")); //$NON-NLS-1$
		
		wSQL.setText( Const.NVL(input.getSql(), ""));
		wLimit.setText(""+input.getRowLimit()); //$NON-NLS-1$
		wOuter.setSelection(input.isOuterJoin());
		wuseVars.setSelection(input.isVariableReplace());
		if(input.getSQLFieldName()!=null) wSQLFieldName.setText(input.getSQLFieldName());
		wqueryOnlyOnChange.setSelection(input.isQueryOnlyOnChange());
		if (input.getDatabaseMeta()!=null)   wConnection.setText(input.getDatabaseMeta().getName());
		else if (transMeta.nrDatabases()==1)
		{
			wConnection.setText( transMeta.getDatabase(0).getName() );
		}

		wStepname.selectAll();

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
		
		input.setRowLimit( Const.toInt( wLimit.getText(), 0) );
		input.setSql( wSQL.getText() );
		input.setSQLFieldName(wSQLFieldName.getText());
		input.setOuterJoin( wOuter.getSelection() );
		input.setVariableReplace(wuseVars.getSelection() );
		input.setQueryOnlyOnChange(wqueryOnlyOnChange.getSelection() );
		input.setDatabaseMeta( transMeta.findDatabase(wConnection.getText()) );

		stepname = wStepname.getText(); // return value

		if (transMeta.findDatabase(wConnection.getText())==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "DynamicSQLRowDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "DynamicSQLRowDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}
		
		dispose();
	}

	   private void get()
		{
	    	if(!gotPreviousFields)
	    	{
	    		gotPreviousFields=true;
				try
				{
					String sqlfield=wSQLFieldName.getText();
					wSQLFieldName.removeAll();
					RowMetaInterface r = transMeta.getPrevStepFields(stepname);
					if (r!=null)
					{
						wSQLFieldName.removeAll();
						wSQLFieldName.setItems(r.getFieldNames());
					}
					if(sqlfield!=null) wSQLFieldName.setText(sqlfield);
				}
				catch(KettleException ke)
				{
					new ErrorDialog(shell, BaseMessages.getString(PKG, "DynamicSQLRowDialog.FailedToGetFields.DialogTitle"), 
							BaseMessages.getString(PKG, "DynamicSQLRowDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
				}
	    	}
		}
}
