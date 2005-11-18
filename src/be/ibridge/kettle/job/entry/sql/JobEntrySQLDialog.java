 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

 
/*
 * Created on 19-jun-2003
 *
 */

package be.ibridge.kettle.job.entry.sql;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.DatabaseDialog;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * This dialog allows you to edit the SQL job entry settings. (select the connection and the sql script to be executed)
 *  
 * @author Matt
 * @since  19-06-2003
 */
public class JobEntrySQLDialog extends Dialog implements JobEntryDialogInterface
{
	private LogWriter    log;

	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlConnection;
	private CCombo       wConnection;
	private Button		 wbConnection;
	private FormData     fdlConnection, fdbConnection, fdConnection;

	private Label        wlSQL;
	private Text         wSQL;
	private FormData     fdlSQL, fdSQL;

	private Label        wlPosition;
	private FormData     fdlPosition;
	
	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntrySQL    	jobentry;
	private JobMeta         jobinfo;
	private Shell       	shell;
	private Props       	props;

	private SelectionAdapter lsDef;

	private boolean changed;
	
	public JobEntrySQLDialog(Shell parent, JobEntrySQL je, Repository rep, JobMeta ji)
	{
		super(parent, SWT.NONE);
		props=Props.getInstance();
		log=LogWriter.getInstance();
		jobentry=je;
		jobinfo=ji;

		if (jobentry.getName() == null) jobentry.setName("SQL");
	}

	public JobEntryInterface open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				jobentry.setChanged();
			}
		};
		changed = jobentry.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("SQL Script");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText("Job Entry name ");
 		props.setLook(wlName);
		fdlName=new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.right= new FormAttachment(middle, 0);
		fdlName.top  = new FormAttachment(0, margin);
		wlName.setLayoutData(fdlName);
		wName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName=new FormData();
		fdName.left = new FormAttachment(middle, margin);
		fdName.top  = new FormAttachment(0, margin);
		fdName.right= new FormAttachment(100, 0);
		wName.setLayoutData(fdName);

		// Connection line
		wlConnection=new Label(shell, SWT.RIGHT);
		wlConnection.setText("Connection ");
 		props.setLook(wlConnection);
		fdlConnection=new FormData();
		fdlConnection.left = new FormAttachment(0, 0);
		fdlConnection.right= new FormAttachment(middle, 0);
		fdlConnection.top  = new FormAttachment(wName, margin);
		wlConnection.setLayoutData(fdlConnection);
		
		wbConnection=new Button(shell, SWT.PUSH);
		wbConnection.setText("&New...");
		wbConnection.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				DatabaseMeta ci = new DatabaseMeta();
				DatabaseDialog cid = new DatabaseDialog(shell, SWT.NONE, log, ci, props);
				if (cid.open()!=null)
				{
					wConnection.add(ci.getName());
					wConnection.select(wConnection.getItemCount()-1);
				}
			}
		});
		fdbConnection=new FormData();
		fdbConnection.right = new FormAttachment(100, 0);
		fdbConnection.top   = new FormAttachment(wName, margin);
		wbConnection.setLayoutData(fdbConnection);

		wConnection=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wConnection);
		for (int i=0;i<jobinfo.nrDatabases();i++)
		{
			DatabaseMeta ci = jobinfo.getDatabase(i);
			wConnection.add(ci.getName());
		}
		wConnection.select(0);
		wConnection.addModifyListener(lsMod);
		fdConnection=new FormData();
		fdConnection.left = new FormAttachment(middle, margin);
		fdConnection.top  = new FormAttachment(wName, margin);
		fdConnection.right= new FormAttachment(wbConnection, -margin);
		wConnection.setLayoutData(fdConnection);


		// Script line
		wlSQL=new Label(shell, SWT.NONE);
		wlSQL.setText("SQL Script: ");
 		props.setLook(wlSQL);
		fdlSQL=new FormData();
		fdlSQL.left = new FormAttachment(0, 0);
		fdlSQL.top  = new FormAttachment(wConnection, margin);
		wlSQL.setLayoutData(fdlSQL);
		wSQL=new Text(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
 		props.setLook(wSQL, Props.WIDGET_STYLE_FIXED);
		wSQL.addModifyListener(lsMod);
		fdSQL=new FormData();
		fdSQL.left   = new FormAttachment(0, 0);
		fdSQL.top    = new FormAttachment(wlSQL, margin);
		fdSQL.right  = new FormAttachment(100, -5);
		fdSQL.bottom = new FormAttachment(100, -50);
		wSQL.setLayoutData(fdSQL);

		wlPosition=new Label(shell, SWT.NONE);
		wlPosition.setText("Linenr: 0        ");
 		props.setLook(wlPosition);
		fdlPosition=new FormData();
		fdlPosition.left = new FormAttachment(0, 0);
		fdlPosition.top  = new FormAttachment(wSQL, margin);
		wlPosition.setLayoutData(fdlPosition);

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wlPosition);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wName.addSelectionListener( lsDef );
				
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		wSQL.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) 
			{
				int linenr = wSQL.getCaretLineNumber()+1;
				wlPosition.setText("Linenr: "+linenr+"   ");
			}
		})
		;
				
		getData();
		
		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobentry;
	}

	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (jobentry.getName() != null) wName.setText( jobentry.getName() );
		if (jobentry.getSQL()  != null) wSQL.setText( jobentry.getSQL() );
		DatabaseMeta dbinfo = jobentry.getDatabase(); 
		if (dbinfo!=null && dbinfo.getName()!=null) wConnection.setText(dbinfo.getName());
		else wConnection.setText("");
		
		wName.selectAll();
	}
	
	private void cancel()
	{
		jobentry.setChanged(changed);
		jobentry=null;
		dispose();
	}
	
	private void ok()
	{
		jobentry.setName(wName.getText());
		jobentry.setSQL(wSQL.getText());
		jobentry.setDatabase( jobinfo.findDatabase(wConnection.getText()));
		dispose();
	}
	
	
	public String toString()
	{
		return this.getClass().getName();
	}
}
