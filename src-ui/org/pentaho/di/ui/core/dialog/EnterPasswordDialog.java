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

 

package org.pentaho.di.ui.core.dialog;
import org.eclipse.swt.SWT;
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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * A dialog that asks for a password.
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class EnterPasswordDialog extends Dialog
{
	private static Class<?> PKG = EnterPasswordDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String title, message;
		
	private Label        wlDesc;
	private Text         wDesc;
    private FormData     fdlDesc, fdDesc;
		
	private Button wOK, wCancel;
	private FormData fdOK, fdCancel;
	private Listener lsOK, lsCancel;

	private Shell  shell;
	private SelectionAdapter lsDef;
	private PropsUI props;
	
	private String description;
	private boolean readonly, modal;

    /**
     * @deprecated Use CT without the <i>props</i> parameter (at 2nd position)
     */
	public EnterPasswordDialog(Shell parent, PropsUI props, String title, String message, String description)
	{
		super(parent, SWT.NONE);
		this.props=props;
        this.title=title;
        this.message=message;
        this.description=description;
        this.readonly=false;
	}
	
    public EnterPasswordDialog(Shell parent, String title, String message, String description)
    {
        super(parent, SWT.NONE);
        this.props=PropsUI.getInstance();
        this.title=title;
        this.message=message;
        this.description=description;
        this.readonly=false;
    }
    
	public void setReadOnly()
	{
		readonly=true;
	}
	
	public void setModal()
	{
		modal=true;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN |  (modal?SWT.APPLICATION_MODAL:SWT.NONE));
 		props.setLook(shell);
		shell.setImage(GUIResource.getInstance().getImageLogoSmall());

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(title);
		
		int margin = Const.MARGIN;

		// From step line
		wlDesc=new Label(shell, SWT.NONE);
		wlDesc.setText(message);
 		props.setLook(wlDesc);
		fdlDesc=new FormData();
		fdlDesc.left = new FormAttachment(0, 0);
		fdlDesc.top  = new FormAttachment(0, margin);
		wlDesc.setLayoutData(fdlDesc);
		wDesc=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
		wDesc.setText("");
		wDesc.setEchoChar('*');
 		props.setLook(wDesc);
		fdDesc=new FormData();
		fdDesc.left  = new FormAttachment(0, 0);
		fdDesc.top   = new FormAttachment(wlDesc, margin);
		fdDesc.right = new FormAttachment(100, 0);
		fdDesc.bottom= new FormAttachment(100, -50);
		wDesc.setLayoutData(fdDesc);
		wDesc.setEditable(!readonly);

		// Some buttons
		if (!readonly)
		{
			wOK=new Button(shell, SWT.PUSH);
			wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
			wCancel=new Button(shell, SWT.PUSH);
			wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
			fdOK=new FormData();
			fdOK.left       = new FormAttachment(33, 0);
			fdOK.bottom     = new FormAttachment(100, 0);
			wOK.setLayoutData(fdOK);
			fdCancel=new FormData();
			fdCancel.left   = new FormAttachment(66, 0);
			fdCancel.bottom = new FormAttachment(100, 0);
			wCancel.setLayoutData(fdCancel);

			// Add listeners
			lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
			lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
			
			wOK.addListener    (SWT.Selection, lsOK     );
			wCancel.addListener(SWT.Selection, lsCancel );
		}
		else
		{
			wOK=new Button(shell, SWT.PUSH);
			wOK.setText(BaseMessages.getString(PKG, "System.Button.Close"));
			fdOK=new FormData();
			fdOK.left       = new FormAttachment(50, 0);
			fdOK.bottom     = new FormAttachment(100, 0);
			wOK.setLayoutData(fdOK);

			// Add listeners
			lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
			wOK.addListener    (SWT.Selection, lsOK     );
		}
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wDesc.addSelectionListener(lsDef);
		
		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		getData();
		
		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return description;
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public void getData()
	{
		if (description!=null) wDesc.setText(description);
	}
	
	private void cancel()
	{
		description=null;
		dispose();
	}
	
	private void ok()
	{
		description = wDesc.getText();
		dispose();
	}
}
