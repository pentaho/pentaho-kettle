/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.core.dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.gui.GUIResource;


/**
 * Displays an HTML page.
 * 
 * @author Matt
 * @since 22-12-2005
 */
public class ShowBrowserDialog extends Dialog
{	
	private static Class<?> PKG = DatabaseDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private String       dialogTitle;
    private String       content;
		
    private Button       wOK;
    private FormData     fdOK;
    private Listener     lsOK;
    
	private Browser      wBrowser;
    private FormData     fdBrowser;
    
	private Shell  shell;
	private PropsUI props;
	
	private int prefWidth = -1;
	private int prefHeight = -1;
	
	private int buttonHeight = 30;

    public ShowBrowserDialog(Shell parent, String dialogTitle, String content)
    {
        super(parent, SWT.NONE);
        props=PropsUI.getInstance();
        this.dialogTitle = dialogTitle;
        this.content = content;
        prefWidth = -1;
        prefHeight = -1;
    }

	public void open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();
		
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.MAX | SWT.MIN);
		shell.setImage(GUIResource.getInstance().getImageSpoon());
		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(dialogTitle);
		
		int margin = Const.MARGIN;
		
		// Canvas
		wBrowser=new Browser(shell, SWT.NONE);
 		props.setLook(wBrowser);

		fdBrowser=new FormData();
		fdBrowser.left  = new FormAttachment(0, 0);
		fdBrowser.top   = new FormAttachment(0, margin);
		fdBrowser.right = new FormAttachment(100, 0);
		fdBrowser.bottom= new FormAttachment(100, -buttonHeight);
		wBrowser.setLayoutData(fdBrowser);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		fdOK=new FormData();
		fdOK.left       = new FormAttachment(50, 0);
		fdOK.bottom     = new FormAttachment(100, 0);
		wOK.setLayoutData(fdOK);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wOK.addListener    (SWT.Selection, lsOK     );
				
		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { ok(); } } );


		//shell.pack();
		if (prefWidth>0 && prefHeight>0)
		{
			shell.setSize(prefWidth, prefHeight);
			Rectangle r = shell.getClientArea();
			int diffx = prefWidth - r.width;
			int diffy = prefHeight - r.height;
			shell.setSize(prefWidth+diffx, prefHeight+diffy);
		}
		else
		{
			shell.setSize(400, 400);
		}

		getData();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
	}

	public void dispose()
	{
		shell.dispose();
	}
	
	public void getData()
	{
        wBrowser.setText(content);
	}
	
	private void ok()
	{
		dispose();
	}

}
