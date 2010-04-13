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
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to enter a (single line) String.
 * 
 * @author Matt
 * @since 21-11-2004
 */
public class EnterStringDialog extends Dialog
{
	private static Class<?> PKG = EnterStringDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlString;
	private Text         wString;
	private TextVar      wStringVar;
	private TransMeta    transMeta;
	private boolean      allowVariables;
    private FormData     fdlString, fdString;
		
	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private Shell  shell;
	private SelectionAdapter lsDef;
	
	private String string;
	private String shellText;
	private String lineText;
	private PropsUI props;
	private boolean manditory;

	/**
	 * This constructs without allowing for variable substitution.  This constructor allows
	 * for backwards compatibility for objects that wish to create this object without
	 * variable substitution.
	 * 
	 * @param parent Parent gui object
	 * @param string The string to display in the dialog
	 * @param shellText
	 * @param lineText
	 */
	public EnterStringDialog(Shell parent, String string, String shellText, String lineText) {
	    this(parent, string, shellText, lineText, false, null);
	}
	
	/**
	 * Constructs with the ability to use environmental variable substitution.
	 * 
	 * @param parent Parent gui object
	 * @param string The string to display in the dialog
	 * @param shellText 
	 * @param lineText
	 * @param allowVariables Indicates to allow environmental substitution
	 * @param TransMeta This object has the has the environmental variables
	 */
    public EnterStringDialog(Shell parent, String string, String shellText, 
           String lineText, boolean allowVariables, TransMeta transMeta)
    {
        super(parent, SWT.NONE);
        this.props      = PropsUI.getInstance();
        this.string     = string;
        this.shellText  = shellText;
        this.lineText   = lineText;
        this.allowVariables = allowVariables;
        this.transMeta = transMeta;
    }
    
	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setImage(GUIResource.getInstance().getImageSpoon());
		shell.setText(shellText);

		int margin = Const.MARGIN;

		// The String line...
		wlString=new Label(shell, SWT.NONE);
		wlString.setText(lineText);
 		props.setLook(wlString);
		fdlString=new FormData();
		fdlString.left = new FormAttachment(0, 0);
		fdlString.top  = new FormAttachment(0, margin);
		wlString.setLayoutData(fdlString);
		if (allowVariables) {
		    wStringVar=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		    wStringVar.setText(string);
	        props.setLook(wStringVar);
		}
		else {
		    wString = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		    wString.setText(string);
	        props.setLook(wString);
		}
		
		fdString=new FormData();
		fdString.left = new FormAttachment(0, 0);
		fdString.top  = new FormAttachment(wlString, margin);
		fdString.right= new FormAttachment(100, -margin);
		
		if (allowVariables) {
		    wStringVar.setLayoutData(fdString);
		    wStringVar.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent arg0) {
                    setFlags();
                }
            });
        }		    
		else {
		    wString.setLayoutData(fdString);
		    wString.addModifyListener(new ModifyListener() {
		        public void modifyText(ModifyEvent arg0) {
		            setFlags();
		        }
		    });
		}
		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wString);
        
        // Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wOK.addListener    (SWT.Selection, lsOK     );
		wCancel.addListener(SWT.Selection, lsCancel );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		if (allowVariables) {
		    wStringVar.addSelectionListener(lsDef);
		} 
		else {
		    wString.addSelectionListener(lsDef);
		}
		
		
		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();
		
		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return string;
	}

	protected void setFlags() {
		String string = (allowVariables?wStringVar.getText():wString.getText());
		boolean enabled = !manditory || !Const.isEmpty(string);
		wOK.setEnabled(enabled);
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public void getData()
	{
	    if (allowVariables) {
	        wStringVar.setText(Const.NVL(string, ""));
	        wStringVar.selectAll();
	    }
	    else {
	        wString.setText(Const.NVL(string, ""));
	        wString.selectAll();
	    }
	    
        setFlags();
	}
	
	private void cancel()
	{
		string=null;
		dispose();
	}
	
	private void ok()
	{
		string = (allowVariables?wStringVar.getText():wString.getText()); 
		dispose();
	}

	/**
	 * @return the manditory
	 */
	public boolean isManditory() {
		return manditory;
	}

	/**
	 * @param manditory the manditory to set
	 */
	public void setManditory(boolean manditory) {
		this.manditory = manditory;
	}
}
