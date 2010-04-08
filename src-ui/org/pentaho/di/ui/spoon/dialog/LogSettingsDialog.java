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
 * Created on 19-jun-2003
 *
 */

package org.pentaho.di.ui.spoon.dialog;
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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.Log4JLayoutInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class LogSettingsDialog extends Dialog
{
	private static Class<?> PKG = LogSettingsDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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
	private PropsUI         props;

    private Log4JLayoutInterface layout;
	
	public LogSettingsDialog(Shell par, int style, PropsUI pr)
	{
			super(par, style);
			parent=par;
			props=pr;
            
            layout = (Log4JLayoutInterface) LogWriter.getLayout();
	}

	public void open()
	{
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
 		props.setLook(shell);
		shell.setImage(GUIResource.getInstance().getImageLogoSmall());

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "LogSettingsDialog.Dialog.LoggingParameters.Title")); //Set logging parameters:
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filter line
		wlFilter=new Label(shell, SWT.RIGHT);
		wlFilter.setText(BaseMessages.getString(PKG, "LogSettingsDialog.Label.FilterSelection")); //Select filter 
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
		wlLoglevel.setText(BaseMessages.getString(PKG, "LogSettingsDialog.Label.LogLevel")); //Loglevel 
 		props.setLook(wlLoglevel);
		fdlLoglevel=new FormData();
		fdlLoglevel.left = new FormAttachment(0, 0);
		fdlLoglevel.right= new FormAttachment(middle, -margin);
		fdlLoglevel.top  = new FormAttachment(wFilter, margin);
		wlLoglevel.setLayoutData(fdlLoglevel);
		wLoglevel=new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wLoglevel.setItems(LogLevel.getLogLevelDescriptions());
 		props.setLook(wLoglevel);
		fdLoglevel=new FormData();
		fdLoglevel.left = new FormAttachment(middle, 0);
		fdLoglevel.top  = new FormAttachment(wFilter, margin);
		fdLoglevel.right= new FormAttachment(100, 0);
		wLoglevel.setLayoutData(fdLoglevel);

		// Time?
		wlTime=new Label(shell, SWT.RIGHT);
		wlTime.setText(BaseMessages.getString(PKG, "LogSettingsDialog.Label.EnableTime")); //Enable Time?
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

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

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

		BaseStepDialog.setSize(shell);

		getData();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
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
	  LogWriter logWriter = LogWriter.getInstance();
		String filter = Const.NVL(logWriter.getFilter(), props.getLogFilter());
		if (filter!=null) 
		{
			wFilter.setText(filter);
		}
		
		wLoglevel.select( DefaultLogLevel.getLogLevel().getLevel() );
		wTime.setSelection(layout.isTimeAdded());
	}
	
	private void cancel()
	{
		dispose();
	}
	
	private void ok()
	{
		int idx=wLoglevel.getSelectionIndex();
		DefaultLogLevel.setLogLevel(LogLevel.values()[idx]);
		String filter = wFilter.getText();
		if (Const.isEmpty(filter)) {
		  LogWriter.getInstance().setFilter(null); // clear filter
		} else {
		  LogWriter.getInstance().setFilter(wFilter.getText());
		}
		layout.setTimeAdded(wTime.getSelection());
        
		props.setLogFilter(wFilter.getText());
		props.setLogLevel(wLoglevel.getText());
		props.saveProps();
		
		dispose();
	}
}
