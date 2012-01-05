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
import org.pentaho.di.core.DescriptionInterface;
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.spoon.job.JobGraph;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * Dialog to enter a text. (descriptions etc.)
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class EnterTextDialog extends Dialog
{
	private static Class<?> PKG = EnterTextDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private String title, message;

    private Label wlDesc;
    private Text wDesc;
    private FormData fdlDesc, fdDesc;

    private Button wOK, wCancel;
    private Listener lsOK, lsCancel;

    private Shell parent, shell;
    private SelectionAdapter lsDef;
    private PropsUI props;
    private String text;
    private boolean fixed;
    private boolean readonly, modal, singleLine;
    private String origText;

    /**
     * Dialog to allow someone to show or enter a text
     * 
     * @param parent The parent shell to use
     * @param title The dialog title
     * @param message The message to display
     * @param text The text to display or edit
     * @param fixed true if you want the font to be in fixed-width
     */
    public EnterTextDialog(Shell parent, String title, String message, String text, boolean fixed)
    {
        this(parent, title, message, text);
        this.fixed = fixed;
    }

    /**
     * Dialog to allow someone to show or enter a text in variable width font
     * 
     * @param parent The parent shell to use
     * @param title The dialog title
     * @param message The message to display
     * @param text The text to display or edit
     */
    public EnterTextDialog(Shell parent, String title, String message, String text)
    {
        super(parent, SWT.NONE);
        this.parent = parent;
        props = PropsUI.getInstance();
        this.title = title;
        this.message = message;
        this.text = text;
        fixed = false;
        readonly = false;
        singleLine = false;
    }

    public void setReadOnly()
    {
        readonly = true;
    }

    public void setModal()
    {
        modal = true;
    }

    public void setSingleLine()
    {
        singleLine = true;
    }

    public String open()
    {
        Display display = parent.getDisplay();
        
        modal |= Const.isLinux(); // On Linux, this dialog seems to behave strangely except when shown modal
        
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN |  (modal?SWT.APPLICATION_MODAL:SWT.NONE));
        props.setLook(shell);
        shell.setImage(GUIResource.getInstance().getImageSpoon());

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(title);

        int margin = Const.MARGIN;

        // From step line
        wlDesc = new Label(shell, SWT.NONE);
        wlDesc.setText(message);
        props.setLook(wlDesc);
        fdlDesc = new FormData();
        fdlDesc.left = new FormAttachment(0, 0);
        fdlDesc.top = new FormAttachment(0, margin);
        wlDesc.setLayoutData(fdlDesc);

        if (singleLine)
            wDesc = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        else
            wDesc = new Text(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

        wDesc.setText("");
        if (fixed)
            props.setLook(wDesc, Props.WIDGET_STYLE_FIXED);
        else
            props.setLook(wDesc);
        fdDesc = new FormData();
        fdDesc.left = new FormAttachment(0, 0);
        fdDesc.top = new FormAttachment(wlDesc, margin);
        fdDesc.right = new FormAttachment(100, 0);
        fdDesc.bottom = new FormAttachment(100, -50);
        wDesc.setLayoutData(fdDesc);
        wDesc.setEditable(!readonly);

        // Some buttons
        if (!readonly)
        {
            wOK = new Button(shell, SWT.PUSH);
            wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
            wCancel = new Button(shell, SWT.PUSH);
            wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

            BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);

            // Add listeners
            lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
            lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

            wOK.addListener    (SWT.Selection, lsOK     );
            wCancel.addListener(SWT.Selection, lsCancel );
        }
        else
        {
            wOK = new Button(shell, SWT.PUSH);
            wOK.setText(BaseMessages.getString(PKG, "System.Button.Close"));

            BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK }, margin, null);

            // Add listeners
            lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
            wOK.addListener    (SWT.Selection, lsOK     );
        }

        lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
        wDesc.addSelectionListener(lsDef);

        // Detect [X] or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { checkCancel(e); } } );

        origText = text;
        getData();

        BaseStepDialog.setSize(shell);

        shell.open();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
        }
        return text;
    }

    public void dispose()
    {
        props.setScreen(new WindowProperty(shell));
        shell.dispose();
    }

    public void getData()
    {
        if (text != null) wDesc.setText(text);
        
        if (readonly) {
        	wOK.setFocus();
        }
        else {
        	wDesc.setFocus();
        }
    }

    public void checkCancel(ShellEvent e) {
   	 String newText = wDesc.getText();
		if (!newText.equals(origText))
		{
			int save = JobGraph.showChangedWarning(shell, title);
			if (save == SWT.CANCEL)
			{
				e.doit = false;
			}
			else if (save == SWT.YES)
			{
				ok();
			}
			else
			{
				cancel();
			}
		}
		else
		{
			cancel();
		}
    }
    
    private void cancel()
    {
        text = null;
        dispose();
    }

    private void ok()
    {
        text = wDesc.getText();
        dispose();
    }

    public static final void editDescription(Shell shell, DescriptionInterface descriptionInterface, String shellText, String message)
    {
        EnterTextDialog textDialog = new EnterTextDialog(shell, shellText, message, descriptionInterface.getDescription());
        String description = textDialog.open();
        if (description != null)
            descriptionInterface.setDescription(description);
    }

    public boolean isFixed()
    {
        return fixed;
    }

    public void setFixed(boolean fixed)
    {
        this.fixed = fixed;
    }
}
