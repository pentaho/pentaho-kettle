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

package be.ibridge.kettle.spoon.dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import be.ibridge.kettle.trans.step.BaseStepDialog;

public class LogSettingsDialog extends Dialog
{
	private LogWriter    log;

	private Label        wlLoglevel;
	private CCombo       wLoglevel;
	private FormData     fdlLoglevel, fdLoglevel;

	private Label        wlFilter;
	private Text         wFilter;
	private FormData     fdlFilter, fdFilter;

	private Label        wlTime;
	private Button       wTime;
	private FormData     fdlTime, fdTime;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private Shell         shell, parent;
	private Props         props;
	
	public LogSettingsDialog(Shell par, int style, LogWriter l, Props pr)
	{
			super(par, style);
			parent=par;
			log=l;
			props=pr;
	}

	public Object open()
	{
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Set logging parameters:");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filter line
		wlFilter=new Label(shell, SWT.RIGHT);
		wlFilter.setText("Select filter ");
 		props.setLook(wlFilter);
		fdlFilter=new FormData();
		fdlFilter.left = new FormAttachment(0, 0);
		fdlFilter.right= new FormAttachment(middle, -margin);
		fdlFilter.top  = new FormAttachment(0, margin);
		wlFilter.setLayoutData(fdlFilter);
		wFilter=new Text(shell, SWT.SINGLE | SWT.BORDER);
		
		wFilter.setText("");

 		props.setLook(wFilter);
		fdFilter=new FormData();
		fdFilter.left = new FormAttachment(middle, 0);
		fdFilter.top  = new FormAttachment(0, margin);
		fdFilter.right= new FormAttachment(100, 0);
		wFilter.setLayoutData(fdFilter);

		wlLoglevel=new Label(shell, SWT.RIGHT);
		wlLoglevel.setText("Loglevel ");
 		props.setLook(wlLoglevel);
		fdlLoglevel=new FormData();
		fdlLoglevel.left = new FormAttachment(0, 0);
		fdlLoglevel.right= new FormAttachment(middle, -margin);
		fdlLoglevel.top  = new FormAttachment(wFilter, margin);
		wlLoglevel.setLayoutData(fdlLoglevel);
		wLoglevel=new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		for (int i=0;i<LogWriter.logLevelDescription.length;i++) 
			wLoglevel.add(LogWriter.logLevelDescription[i]);
		wLoglevel.select( log.getLogLevel()+1); //+1: starts at -1	
		
 		props.setLook(wLoglevel);
		fdLoglevel=new FormData();
		fdLoglevel.left = new FormAttachment(middle, 0);
		fdLoglevel.top  = new FormAttachment(wFilter, margin);
		fdLoglevel.right= new FormAttachment(100, 0);
		wLoglevel.setLayoutData(fdLoglevel);

		// Time?
		wlTime=new Label(shell, SWT.RIGHT);
		wlTime.setText("Enable Time?");
 		props.setLook(wlTime);
		fdlTime=new FormData();
		fdlTime.left = new FormAttachment(0, 0);
		fdlTime.right= new FormAttachment(middle, -margin);
		fdlTime.top  = new FormAttachment(wLoglevel, margin);
		wlTime.setLayoutData(fdlTime);
		wTime=new Button(shell, SWT.CHECK);
 		props.setLook(wTime);
		fdTime=new FormData();
		fdTime.left = new FormAttachment(middle, 0);
		fdTime.top  = new FormAttachment(wLoglevel, margin);
		wTime.setLayoutData(fdTime);
		wTime.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					log.setTime( !log.getTime() );
				}
			}
		);

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wTime);
		
		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		SelectionAdapter lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wFilter.addSelectionListener( lsDef );
		//wLoglevel.addSelectionListener( lsDef );

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();
		
		getData();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return log;
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{	
		String filter = Const.NVL(log.getFilter(), props.getLogFilter());
		if (filter!=null) 
		{
			wFilter.setText(filter);
		}
		
		wLoglevel.select(log.getLogLevel());
		wTime.setSelection(log.getTime());
	}
	
	private void cancel()
	{
		log=null;
		dispose();
	}
	
	private void ok()
	{
		int idx=wLoglevel.getSelectionIndex();
		log.setLogLevel(idx);
		String filter = wFilter.getText();
		if (filter!=null && filter.length()>0)
		{
			log.setFilter(wFilter.getText());
		}
		else
		{
			log.setFilter(null); // clear filter
		}
		
		props.setLogFilter(wFilter.getText());
		props.setLogLevel(wLoglevel.getText());
		props.saveProps();
		
		dispose();
	}
}
