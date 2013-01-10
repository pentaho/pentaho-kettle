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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * Dialog to enter a text. (descriptions etc.)
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class ShowMessageDialog extends Dialog
{
	private static Class<?> PKG = ShowMessageDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private String title, message;

    private Shell shell;
    private PropsUI props;

    private int flags;
    private int returnValue;

    private Shell parent;
    
    private boolean scroll;
    
    /** Timeout of dialog in seconds */
    private int timeOut;

    private List<Button> buttons;

    private List<SelectionAdapter> adapters;

    /**
     * Dialog to allow someone to show a text with an icon in front
     * 
     * @param parent The parent shell to use
     * @param flags the icon to show using SWT flags: SWT.ICON_WARNING, SWT.ICON_ERROR, ... Also SWT.OK, SWT.CANCEL is allowed.
     * @param title The dialog title
     * @param message The message to display
     * @param text The text to display or edit
     */
    public ShowMessageDialog(Shell parent, int flags, String title, String message)
    {
      this(parent, flags, title, message, false);
    }
    
    /**
     * Dialog to allow someone to show a text with an icon in front
     * 
     * @param parent The parent shell to use
     * @param flags the icon to show using SWT flags: SWT.ICON_WARNING, SWT.ICON_ERROR, ... Also SWT.OK, SWT.CANCEL is allowed.
     * @param title The dialog title
     * @param message The message to display
     * @param text The text to display or edit
     * @param scroll Set the dialog to a default size and enable scrolling
     */
    public ShowMessageDialog(Shell parent, int flags, String title, String message, boolean scroll)
    {
        super(parent, SWT.NONE);
        this.parent = parent;
        this.flags = flags;
        this.title = title;
        this.message = message;
        this.scroll = scroll;
        
        props = PropsUI.getInstance();
    }

    public int open()
    {
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
        
        props.setLook(shell);
        shell.setImage(GUIResource.getInstance().getImageSpoon());

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;
        shell.setLayout(formLayout);
        
        shell.setText(title);

        int margin = Const.MARGIN;
        boolean hasIcon = (flags & SWT.ICON_WARNING)!=0 || 
                          (flags & SWT.ICON_INFORMATION)!=0 || 
                          (flags & SWT.ICON_QUESTION)!=0 || 
                          (flags & SWT.ICON_ERROR)!=0 ||
                          (flags & SWT.ICON_WORKING)!=0; 
        
        Image image = null;
        if ((flags & SWT.ICON_WARNING)!=0) image = display.getSystemImage(SWT.ICON_WARNING);
        if ((flags & SWT.ICON_INFORMATION)!=0) image = display.getSystemImage(SWT.ICON_INFORMATION);
        if ((flags & SWT.ICON_QUESTION)!=0) image = display.getSystemImage(SWT.ICON_QUESTION);
        if ((flags & SWT.ICON_ERROR)!=0) image = display.getSystemImage(SWT.ICON_ERROR);
        if ((flags & SWT.ICON_WORKING)!=0) image = display.getSystemImage(SWT.ICON_WORKING);

        hasIcon = hasIcon && image!=null;
        Label wIcon=null;

        if (  hasIcon && image!=null )
        {
            wIcon = new Label(shell, SWT.NONE);
            props.setLook(wIcon);
            wIcon.setImage(image);
            FormData fdIcon = new FormData();
            fdIcon.left   = new FormAttachment(0,0);
            fdIcon.top    = new FormAttachment(0,0);
            fdIcon.right  = new FormAttachment(0,image.getBounds().width);
            fdIcon.bottom = new FormAttachment(0,image.getBounds().height);
            wIcon.setLayoutData(fdIcon);
        }
        
        // The message
        Text wlDesc;
        FormData fdlDesc = new FormData();
        
        if(scroll) {
          wlDesc = new Text(shell, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
          shell.setSize(550, 350);
          fdlDesc.bottom = new FormAttachment(100, -50);
          fdlDesc.right = new FormAttachment(100, 0);
        } else {
          wlDesc = new Text(shell, SWT.MULTI | SWT.READ_ONLY);
          fdlDesc.right = new FormAttachment(100, 0);
        }

        wlDesc.setText(message);
        props.setLook(wlDesc);
        
        if (hasIcon)
        {
            fdlDesc.left = new FormAttachment(wIcon, margin*2);
            fdlDesc.top = new FormAttachment(0, margin);
        }
        else
        {
            fdlDesc.left = new FormAttachment(0, 0);
            fdlDesc.top = new FormAttachment(0, margin);
        }
        
        wlDesc.setLayoutData(fdlDesc);

        buttons = new ArrayList<Button>();
        adapters = new ArrayList<SelectionAdapter>();
        
        if ( (flags & SWT.OK) !=0)
        {
            Button button = new Button(shell, SWT.PUSH);
            final String ok = BaseMessages.getString(PKG, "System.Button.OK");  //$NON-NLS-1$
            button.setText(ok);
            SelectionAdapter selectionAdapter = new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { quit(SWT.OK); } }; 
            button.addSelectionListener(selectionAdapter);
            adapters.add(selectionAdapter);
            buttons.add(button);
        }
        if ( (flags & SWT.CANCEL) !=0) 
        {
            Button button = new Button(shell, SWT.PUSH);
            button.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$
            SelectionAdapter selectionAdapter = new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { quit(SWT.CANCEL); } };
            button.addSelectionListener(selectionAdapter);
            adapters.add(selectionAdapter);
            buttons.add(button);
        }
        if ( (flags & SWT.YES) !=0)
        {
            Button button = new Button(shell, SWT.PUSH);
            button.setText(BaseMessages.getString(PKG, "System.Button.Yes")); //$NON-NLS-1$
            SelectionAdapter selectionAdapter = new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { quit(SWT.YES); } };
            button.addSelectionListener(selectionAdapter);
            adapters.add(selectionAdapter);
            buttons.add(button);
        }
        if ( (flags & SWT.NO) !=0) 
        {
            Button button = new Button(shell, SWT.PUSH);
            button.setText(BaseMessages.getString(PKG, "System.Button.No")); //$NON-NLS-1$
            SelectionAdapter selectionAdapter = new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { quit(SWT.NO); } };
            button.addSelectionListener(selectionAdapter);
            adapters.add(selectionAdapter);
            buttons.add(button);
        }
        
        BaseStepDialog.positionBottomButtons(shell, buttons.toArray(new Button[buttons.size()]), margin, wlDesc);

        // Detect [X] or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

        shell.layout();
        if(!scroll) {
          shell.pack(true);
        }
        
        final Button button = buttons.get(0);
        final SelectionAdapter selectionAdapter = adapters.get(0);
        final String ok = button.getText();
        long startTime = new Date().getTime();

        shell.open();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
            {
                display.sleep();
                
                if (timeOut>0)
                {
                    long time = new Date().getTime();
                    long diff = (time-startTime)/1000;
                    button.setText(ok+" ("+(timeOut-diff)+")"); //$NON-NLS-1$ //$NON-NLS-2$
                    
                    if (diff>=timeOut)
                    {
                       selectionAdapter.widgetSelected(null);
                    }
                }

            }
        }
        return returnValue;
    }

    public void dispose()
    {
        shell.dispose();
    }
    
    private void cancel()
    {
        if ((flags&SWT.NO)>0)
        {
            quit(SWT.NO); 
        }
        else 
        {
            quit(SWT.CANCEL);
        }
    }

    private void quit(int returnValue)
    {
        this.returnValue = returnValue;
        dispose();
    }

    /**
     * @return the timeOut
     */
    public int getTimeOut()
    {
        return timeOut;
    }

    /**
     * @param timeOut the timeOut to set
     */
    public void setTimeOut(int timeOut)
    {
        this.timeOut = timeOut;
    }

}
